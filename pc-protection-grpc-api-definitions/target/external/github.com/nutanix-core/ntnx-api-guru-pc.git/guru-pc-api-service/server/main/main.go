/*
 * Generated file .go.
 *
 * Product version: 16.7.0-SNAPSHOT
 *
 * Part of the Guru - The new GoLang service which hosts API endpoints for Prism Central. This is Guru PC, specific to PC.
 *
 * (c) 2023 Nutanix Inc.  All rights reserved
 *
 */

package main

import (
	"context"
	"fmt"
	"ntnx-api-guru-pc/guru-pc-api-service/cache/callbacks"
	config_manager "ntnx-api-guru-pc/guru-pc-api-service/cache/config"
	"ntnx-api-guru-pc/guru-pc-api-service/grpc"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/external"
	"os/signal"
	"path/filepath"
	"runtime"
	"sync"
	"syscall"

	"github.com/nutanix-core/go-cache/util-go/tracer"
	"github.com/nutanix-core/go-cache/zeus"
	zeus_config "github.com/nutanix-core/go-cache/zeus/config"

	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"os"

	"github.com/natefinch/lumberjack"
	log "github.com/sirupsen/logrus"
)

var (
	waitGroup     sync.WaitGroup
	nutanixLogDir = os.Getenv(consts.LogDirEnvVar)
	logFilePath   = filepath.Join(nutanixLogDir, consts.LogFileName)
)

type CustomTextFormatter struct {
}

func (f *CustomTextFormatter) Format(entry *log.Entry) ([]byte, error) {

	fileName := filepath.Base(entry.Caller.File)
	line := entry.Caller.Line
	// Format the log message
	message := fmt.Sprintf("[%s] [%s] [%s:%d] %s\n",
		entry.Level.String(),                     // Log level
		entry.Time.Format("2006-01-02 15:04:05"), // Date-time in UTC.
		fileName,                                 // File name
		line,                                     // Line number
		entry.Message,                            // Log message
	)

	return []byte(message), nil
}

// InitializeLogrus initializes logrus with a formatter, output and level.
func initializeLogrus(logLevel log.Level) {
	log.SetReportCaller(true)
	log.SetLevel(logLevel)
	customFormatter := &CustomTextFormatter{}
	log.SetFormatter(customFormatter)

	l := &lumberjack.Logger{
		Filename:   logFilePath,
		MaxSize:    100,
		MaxBackups: 5,
		MaxAge:     28,
		LocalTime:  true,
	}
	log.SetOutput(l)
}

// Return logrus log level to set based on logLevel.
func getLogrusLogLevel(logLevel string) log.Level {
	parsedLevel, err := log.ParseLevel(logLevel)
	if err != nil {
		defaultLogLevel := log.InfoLevel
		log.WithError(err).Error("Failed to parse loglevel from flag")
		log.Info("Using default log level ", defaultLogLevel)
		return defaultLogLevel
	}
	log.Infof("Using log level %s", logLevel)
	return parsedLevel
}

func main() {
	closer := tracer.InitTracer(consts.GuruTraceName)
	if closer != nil {
		defer closer.Close()
	}
	if closer == nil {
		log.Errorf("Tracer is not initialized. Check if observability config file is available")
	}

	// Configure runtime parameters before spawning the server.
	maxCpuCores := runtime.GOMAXPROCS(*consts.GoMaxProcsValue)
	// If set to 0, it defaults to runtime.NumCPU
	if maxCpuCores > 0 {
		log.Infof("Configured maxprocs for server : %d", maxCpuCores)
	}

	grpcServer := grpc.NewServer()
	log.Infof("GRPC Server Listening at port %v", consts.GuruPort)
	go func() {
		grpcServer.Start(&waitGroup)
	}()

	c := make(chan os.Signal, 1)
	// We'll accept graceful shutdowns when quit via SIGINT (Ctrl+C)
	// SIGKILL, SIGQUIT or SIGTERM (Ctrl+/) will not be caught.
	// Genesis stop service utility does the following -
	// 1. Genesis attempts SIGTERM for a particular pid.
	// 2. Wait for service to exit for 5 seconds.
	// 3. Attempts a hard kill (kill -9) SIGKILL for the pid.
	signal.Notify(c, syscall.SIGHUP, syscall.SIGINT, syscall.SIGTERM, syscall.SIGQUIT)

	// Block until we receive our signal in channel.
	<-c

	// Create a deadline to wait for.
	_, cancel := context.WithTimeout(context.Background(), *consts.ServerStartupTimeout)
	defer cancel()

	log.Infof("Gracefully stopping the server...")
	grpcServer.Stop()
	// Explicitly marking the server instance as nil for force GC
	grpcServer = nil
	log.Infof("shutting down the server with pid : %d", os.Getpid())
	os.Exit(0)
}

func init() {
	// initialise zeus config cache which sets a watch on configuration.
	config_manager, err := config_manager.InitZeusConfigCache()
	if err != nil {
		log.Fatalf("Failed to load zeus config due to : %s", err)
	}

	// start watch on zeus
	go zeus.KeepWatchingContentsOf(external.Interfaces().ZkSessionConnection(),
		zeus_config.ZeusConfigZKPath, callbacks.ConfigChangeCb)

	// Check if external Ip is set, else fallback to local node Ip.
	switch {
	case config_manager.GetZeusConfig().GetClusterExternalIp() != "":
		consts.HostAddr = config_manager.GetZeusConfig().GetClusterExternalIp()
	case config_manager.GetZeusConfig().MustGetIp() != "":
		consts.HostAddr = config_manager.GetZeusConfig().MustGetIp()
	}

	// initialise gflags.
	consts.InitFlags()

	// initialise logrus.
	initializeLogrus(getLogrusLogLevel(*consts.LogLevel))
	log.Infof("Successfully initialised server with pid: %d", os.Getpid())
}
