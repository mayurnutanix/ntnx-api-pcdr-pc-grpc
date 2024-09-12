/*
 * Copyright (c) 2023 Nutanix Inc. All rights reserved.
 * Author: viraj.aute@nutanix.com
 * GRPC server for guru service which serves at port 8422. As part of server init, following operations
 * are invoked -
 * 1. A job queue is initialised and workers are configured to execute the jobs.
 * 2. When main program sends a stop signal, Stop() method below initiates the job channel closure and handles other cleanups.
 */
package grpc

import (
	"net"
	"ntnx-api-guru-pc/guru-pc-api-service/middleware"
	"strconv"
	"sync"

	log "github.com/sirupsen/logrus"
	"google.golang.org/grpc"

	"github.com/nutanix-core/go-cache/util-go/tracer"
	domainManagerConfig "ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/config"
	"ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/management"
	"ntnx-api-guru-pc/guru-pc-api-service/adapter"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/external"
	"ntnx-api-guru-pc/guru-pc-api-service/queue"
	"ntnx-api-guru-pc/guru-pc-api-service/services/domain_manager"
	"ntnx-api-guru-pc/guru-pc-api-service/services/domain_manager_config"
)

type GrpcServer interface {
	// Start the server. waitGroup will be used to track execution of the server.
	Start(waitGroup *sync.WaitGroup)
	Stop()
}

type GrpcServerImpl struct {
	port                                 uint64
	listener                             net.Listener
	gserver                              *grpc.Server
	forwardQueue                         *queue.ChannelQueue
	domainManagerServiceServerImpl       *domain_manager.DomainManagerServiceServerImpl
	domainManagerConfigServiceServerImpl *domain_manager_config.DomainManagerConfigServiceServerImpl
	accessLogger                         middleware.AccessLoggerIfc
}

// NewServer creates a new GRPC server that services can be exported with.
// The connections are secured by mTLS. Errors are fatal.
func NewServer() (server GrpcServer) {
	adapterIfc := adapter.Adapter{}
	s := &GrpcServerImpl{
		port:         consts.GuruPort,
		forwardQueue: queue.NewChannelQueue(*consts.NumWorkers, adapterIfc),
	}

	// Open the access file to log request. In case file open fails, just log an error, but let the server start.
	s.accessLogger = middleware.NewAccessLogger()

	// Enabling tracing OpenTelemetry.
	unaryTrace, streamTrace := tracer.GrpcServerTraceOptions()
	log.Infof("Tracer opts : %v %v", unaryTrace, streamTrace)
	var serverOpts []grpc.ServerOption
	if streamTrace != nil {
		streamOpt := grpc.StreamInterceptor(streamTrace)
		serverOpts = append(serverOpts, streamOpt)
	}

	chain := grpc.ChainUnaryInterceptor(unaryTrace, s.UnaryLoggerInterceptor)
	serverOpts = append(serverOpts, chain)
	log.Infof("Server opts : %+v", serverOpts)
	// TODO: Enable support for mTLS based on use-case.
	s.gserver = grpc.NewServer(serverOpts...)
	err := s.initialiseInterfaces()
	if err != nil {
		s.cleanServerMemory()
		log.Fatalf("Failed to initialise interfaces with error : %s", err)
	}

	// Start the init workflows.
	s.initialiseWorkflows()
	log.Infof("Starting up the job workers")
	s.forwardQueue.StartWorkers()
	s.registerServices()
	return s
}

func (server *GrpcServerImpl) cleanServerMemory() {
	// make the variables, slices and other data objects as nil and leave it to GC for sweep-scan.
	server.gserver = nil
	server.listener = nil
}

func (server *GrpcServerImpl) initialiseInterfaces() (err error) {
	server.domainManagerServiceServerImpl = domain_manager.NewDomainManagerServiceServerImpl(
		server.forwardQueue)
	server.domainManagerConfigServiceServerImpl = domain_manager_config.NewDomainManagerConfigServiceServerImpl()

	return nil
}

// registerServices is a central place for the grpc services that need to be
// registered with the server before it is started.
func (server *GrpcServerImpl) registerServices() {
	log.Infof("Registering services...")
	management.RegisterDomainManagerServiceServer(server.gserver, server.domainManagerServiceServerImpl)
	domainManagerConfig.RegisterDomainManagerServiceServer(server.gserver, server.domainManagerConfigServiceServerImpl)
}

// Start listening and serve. Errors are fatal (todo).
func (server *GrpcServerImpl) Start(waitGroup *sync.WaitGroup) {
	port := ":" + strconv.FormatUint(server.port, 10)
	log.Infof("Starting Guru gRPC server on %s.", port)
	var err error
	server.listener, err = net.Listen("tcp", port)
	if err != nil {
		log.Fatalf("Failed to listen: %v.", err)
	}
	log.Infof("Guru gRPC server listening on %s.", port)

	waitGroup.Add(1)
	go func() {
		if err := server.gserver.Serve(server.listener); err != nil {
			log.Fatalf("Failed to serve: %v.", err)
		}
		waitGroup.Done()
	}()
}

// Stop the server.
func (server *GrpcServerImpl) Stop() {
	// Stop signals all workers to stop and waits for them to finish.
	log.Infof("Stopping workers...")
	server.forwardQueue.StopWorkers()
	//TODO: Add a method implementing plugin which will perform actions right before stopping the grpc server
	// i.e. Persisting the states, committing to db etc.
	if server.gserver != nil {
		// Stops the server from accepting new connections and RPCs.
		// Waits for all the ongoing requests to complete.
		// Waits for serving threads to be ready to exit to ensure no new connections are created after this.
		server.gserver.GracefulStop()
	}

	// Close all the initialised external session instances i.e. ZK and idf sessions.
	// This is needed to ensure Guru is gracefully closing all the connections when
	// server is being stopped.
	// 1. close Zk session
	external.Interfaces().Cleanup()

	// Close the writer.
	server.accessLogger.Close()
}
