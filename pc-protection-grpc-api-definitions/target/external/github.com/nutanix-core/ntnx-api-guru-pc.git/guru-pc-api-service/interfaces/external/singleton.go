/*
* Copyright (c) 2024 Nutanix Inc. All rights reserved.
*
* Author: kshitij.kumar@nutanix.com
*
* This file contains all interfaces required for guru
 */

package external

import (
	"sync"
	"time"

	"github.com/nutanix-core/ntnx-api-guru/services/events"

	"github.com/golang/glog"
	"github.com/golang/protobuf/proto"
	ergonClient "github.com/nutanix-core/go-cache/ergon/client"
	zookeeper "github.com/nutanix-core/go-cache/go-zookeeper"
	genesisSvc "github.com/nutanix-core/go-cache/infrastructure/cluster/client/genesis"
	insights "github.com/nutanix-core/go-cache/insights/insights_interface"
	"github.com/nutanix-core/go-cache/nusights/util/db"
	uhura "github.com/nutanix-core/go-cache/uhura/client"
	utilMisc "github.com/nutanix-core/go-cache/util-go/misc"
	utilNet "github.com/nutanix-core/go-cache/util-go/net"
	"github.com/nutanix-core/go-cache/util-go/uuid4"
	"github.com/nutanix-core/go-cache/zeus"
	"github.com/nutanix-core/ntnx-api-guru/services/ergon"
	"github.com/nutanix-core/ntnx-api-guru/services/guru_api"
	"github.com/nutanix-core/ntnx-api-guru/services/idf"
	"github.com/nutanix-core/ntnx-api-guru/services/zk"
	log "github.com/sirupsen/logrus"

	"github.com/nutanix-core/ntnx-api-utils-go/idempotencyutils"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
)

type GuruInterfaces interface {
	ErgonService() ergon.ErgonClientIfc
	ErgonClient() ergonClient.Ergon
	IdfSvc() insights.InsightsServiceInterface
	IdfClient() idf.IdfClientIfc
	IdfWatcher() IdfWatcher
	DbClient() db.CPDBClientInterface
	ZkClient() zk.ZkClientIfc
	ZkSession() zeus.ZookeeperIfc
	ZkSessionConnection() *zookeeper.Conn
	ZkSessionObject() *zeus.ZookeeperSession
	Uuid() UuidInterface
	Proto() MarshalInterface // Marshal interface.
	Glog() GlogInterface
	RemoteRestClient() guru_api.GuruApiClientIfc
	EventForwarder() events.EventForwarderIfc
	GenesisClient() genesisSvc.IGenesisRpcClient
	IdempotencyService() idempotencyutils.IdempotencyService
	GenesisJsonRpcClient() genesisSvc.GenesisJsonRpcClientIfc
	RemoteUhuraClient(clusterUuid *uuid4.Uuid) uhura.UhuraClientInterface
	Cleanup()
}

type UuidInterface interface {
	New() (*uuid4.Uuid, error)
}

type uuidUtil struct {
}

func (s *uuidUtil) New() (*uuid4.Uuid, error) {
	uuid, err := uuid4.New()
	return uuid, err
}

type singletonService struct {
	ergonService ergon.ErgonClientIfc // A Ergon grpc.
	ergonClient  ergonClient.Ergon
	idfSvc       insights.InsightsServiceInterface // An Insights Service Client.
	idfWatcher   IdfWatcher                        // An Insights watcher client
	idfClient    idf.IdfClientIfc                  // An Idf client interface
	dbClient     db.CPDBClientInterface
	zkClient     zk.ZkClientIfc // A Zookeeper Session.
	// We need to export this field so that this can be set in unit test.
	uuid                 UuidInterface    // UUID interface. Needed for unit testing.
	marshal              MarshalInterface // proto.Marshal function.
	glog                 GlogInterface
	remoteRestclient     guru_api.GuruApiClientIfc
	zkSession            zeus.ZookeeperIfc
	zkSessionConnection  *zookeeper.Conn
	zkSessionObject      *zeus.ZookeeperSession
	eventForwarder       events.EventForwarderIfc
	genesisRpcClient     genesisSvc.IGenesisRpcClient
	genesisJsonRpcClient genesisSvc.GenesisJsonRpcClientIfc
	idempotencySvc       idempotencyutils.IdempotencyService
	remoteUhuraClientMap map[string]uhura.UhuraClientInterface
}

var (
	// A singleton through which to access all singleton services. In production
	// code it is set with InitSingletonService(). In unit tests, it is set with
	// MockSingletons() to mock out all singleton services.
	singleton            GuruInterfaces
	singletonServiceOnce sync.Once
	serialExecutorOnce   sync.Once
	ergonServiceOnce     sync.Once
	ergonClientOnce      sync.Once
	idfSvcOnce           sync.Once
	idfClientOnce        sync.Once
	idfWatcherOnce       sync.Once
	dbClientOnce         sync.Once
	zkclientOnce         sync.Once
	uuidOnce             sync.Once
	marshalOnce          sync.Once
	glogOnce             sync.Once
	remoteRestClient     sync.Once
	zkSessionOnce        sync.Once
	eventForwarderOnce   sync.Once
	ergonClientTimeout   = *consts.ErgonClientTimeoutSecs
	ergonClientRetry     = utilMisc.NewExponentialBackoff(
		time.Duration(*consts.ErgonRetryIntervalInitial)*time.Second,
		time.Duration(*consts.ErgonRetryIntervalMax)*time.Second,
		*consts.ErgonRetryMaxRetries)
	apiClientRetry = utilMisc.NewExponentialBackoff(
		time.Duration(*consts.ApiClientIntervalInitial)*time.Second,
		time.Duration(*consts.ApiClientIntervalMax)*time.Second,
		*consts.ApiClientMaxRetries)
	genesisRpcClientOnce     sync.Once
	genesisJsonRpcClientOnce sync.Once
	idempotencySvcOnce       sync.Once
	remoteUhuraClientOnce    sync.Once
)

func Interfaces() GuruInterfaces {
	singletonServiceOnce.Do(func() {
		if singleton == nil {
			singleton = &singletonService{}
		}
	})

	return singleton
}

func (s *singletonService) ErgonService() ergon.ErgonClientIfc {
	ergonServiceOnce.Do(func() {
		if s.ergonService == nil {
			s.ergonService = ergon.NewErgonClient(consts.ErgonBackoffForRetries)
		}
	})

	return s.ergonService
}

func (s *singletonService) ErgonClient() ergonClient.Ergon {
	ergonClientOnce.Do(func() {
		if s.ergonClient == nil {
			s.ergonClient = ergonClient.NewErgonServiceWithRetryAndTimeout(
				ergonClient.DefaultErgonAddr, ergonClient.DefaultErgonPort,
				ergonClientRetry, ergonClientTimeout)
		}
	})
	return s.ergonClient
}

func (s *singletonService) IdfSvc() insights.InsightsServiceInterface {
	idfSvcOnce.Do(func() {
		if s.idfSvc == nil {
			s.idfSvc = insights.NewInsightsServiceFromGflags()
		}
	})

	return s.idfSvc
}

func (s *singletonService) IdfWatcher() IdfWatcher {
	idfWatcherOnce.Do(func() {
		if s.idfWatcher == nil {
			watcherClientUuid, _ := uuid4.New()
			clientID := consts.GuruIdfWatcherClientName + "-" + watcherClientUuid.String()
			log.Infof("IDF watcher client ID :%v", clientID)
			s.idfWatcher = insights.NewWatchClient(clientID, s.idfSvc)
		}
	})
	return s.idfWatcher
}

func (s *singletonService) IdfClient() idf.IdfClientIfc {
	idfClientOnce.Do(func() {
		if s.idfClient == nil {
			s.idfClient = idf.NewIdfClient()
		}
	})

	return s.idfClient
}

func (s *singletonService) DbClient() db.CPDBClientInterface {
	dbClientOnce.Do(func() {
		if s.dbClient == nil {
			s.dbClient = db.NewCpDbServiceFromGflags()
		}
	})

	return s.dbClient
}

func (s *singletonService) ZkSession() zeus.ZookeeperIfc {
	zkSessionOnce.Do(func() {
		if s.zkSession == nil {
			var err error
			zkSession, err := zeus.NewZookeeperSessionFromEnvVar()
			if err != nil {
				log.Fatalf("Unable to initialise new zookeeper connection")
			}
			// TODO: Add session timeout, session closure and wait for session logic to ensure Guru is holding just one session.
			_ = zkSession.WaitForConnection()
			log.Infof("Initialized a Zookeeper session, ID = %d.", zkSession.Conn.SessionID())
			// Session close should be done while stopping the server.
			s.zkSession = zkSession
			s.zkSessionObject = zkSession
			s.zkSessionConnection = zkSession.Conn
		}
	})
	return s.zkSession
}

func (s *singletonService) ZkSessionConnection() *zookeeper.Conn {
	if s.zkSessionConnection == nil {
		s.initZkSession()
	}
	return s.zkSessionConnection
}

func (s *singletonService) initZkSession() {
	s.ZkSession()
}

func (s *singletonService) GenesisClient() genesisSvc.IGenesisRpcClient {
	genesisRpcClientOnce.Do(func() {
		if s.genesisRpcClient == nil {
			s.genesisRpcClient = &genesisSvc.GenesisRpcClient{
				Impl: utilNet.NewProtobufRPCClient(consts.LocalHost, uint16(consts.GenesisPort)),
			}
		}
	})
	return s.genesisRpcClient
}

func (s *singletonService) GenesisJsonRpcClient() genesisSvc.GenesisJsonRpcClientIfc {
	genesisJsonRpcClientOnce.Do(func() {
		if s.genesisJsonRpcClient == nil {
			s.genesisJsonRpcClient = genesisSvc.NewJsonRpcClient()
		}
	})
	return s.genesisJsonRpcClient
}

func (s *singletonService) Uuid() UuidInterface {
	uuidOnce.Do(func() {
		if s.uuid == nil {
			s.uuid = new(uuidUtil)
		}
	})

	return s.uuid
}

type MarshalInterface interface {
	Marshal(m proto.Message) ([]byte, error)
}

type marshalUtil struct {
}

func (s *marshalUtil) Marshal(msg proto.Message) ([]byte, error) {
	return proto.Marshal(msg)
}

func (s *singletonService) Proto() MarshalInterface {
	marshalOnce.Do(func() {
		if s.marshal == nil {
			s.marshal = new(marshalUtil)
		}
	})

	return s.marshal
}

type GlogInterface interface {
	Fatalln(args ...interface{})
	Fatalf(format string, args ...interface{})
}

type glogUtil struct {
}

func (s *glogUtil) Fatalln(args ...interface{}) {
	glog.Fatalln(args...)
}

func (s *glogUtil) Fatalf(format string, args ...interface{}) {
	glog.Fatalf(format, args...)
}

func (s *singletonService) Glog() GlogInterface {
	glogOnce.Do(func() {
		if s.glog == nil {
			s.glog = new(glogUtil)
		}
	})

	return s.glog
}

func (s *singletonService) RemoteRestClient() guru_api.GuruApiClientIfc {
	remoteRestClient.Do(func() {
		if s.remoteRestclient == nil {
			s.remoteRestclient, _ =
				guru_api.NewGuruApiClient(apiClientRetry, *consts.ApiClientTimeout)
		}
	})

	return s.remoteRestclient
}

func (s *singletonService) ZkClient() zk.ZkClientIfc {
	zkclientOnce.Do(func() {
		if s.zkClient == nil {
			s.zkClient = zk.NewZkClient(*consts.ZkTimeout)
		}
	})

	return s.zkClient
}

func (s *singletonService) EventForwarder() events.EventForwarderIfc {
	eventForwarderOnce.Do(func() {
		if s.eventForwarder == nil {
			s.eventForwarder = events.NewEventForwarder()
		}
	})

	return s.eventForwarder
}

func (s *singletonService) IdempotencyService() idempotencyutils.IdempotencyService {
	idempotencySvcOnce.Do(func() {
		if s.idempotencySvc == nil {
			s.idempotencySvc = idempotencyutils.NewIdempotencySupportServiceImpl(Interfaces().IdfSvc())
		}
	})
	return s.idempotencySvc
}

func (s *singletonService) RemoteUhuraClient(clusterUuid *uuid4.Uuid) uhura.UhuraClientInterface {
	remoteUhuraClientOnce.Do(func() {
		if s.remoteUhuraClientMap == nil {
			s.remoteUhuraClientMap = make(map[string]uhura.UhuraClientInterface)
		}
	})
	if _, ok := s.remoteUhuraClientMap[clusterUuid.String()]; !ok {
		s.remoteUhuraClientMap[clusterUuid.String()] = uhura.NewRemoteUhuraService(Interfaces().ZkSession().(*zeus.ZookeeperSession), clusterUuid, nil, nil, nil, nil)
	}
	client, _ := s.remoteUhuraClientMap[clusterUuid.String()]
	return client
}

func (s *singletonService) ZkSessionObject() *zeus.ZookeeperSession {
	if s.zkSessionObject == nil {
		s.initZkSession()
	}
	return s.zkSessionObject
}

func (s *singletonService) Cleanup() {
	// Close sessions i.e zk
	log.Infof("Explicitly closing the zookeeper session")
	s.ZkSession().Close()
}

func SetSingletonServices(ergonServiceIfc ergon.ErgonClientIfc,
	idfSvcIfc insights.InsightsServiceInterface,
	idfClientIfc idf.IdfClientIfc,
	zkClientIfc zk.ZkClientIfc,
	zkSessionIfc zeus.ZookeeperIfc,
	uuidIfc UuidInterface,
	protoIfc MarshalInterface,
	glogIfc GlogInterface,
	guruApiClientIfc guru_api.GuruApiClientIfc,
	eventForwarderIfc events.EventForwarderIfc,
	idfWatcher IdfWatcher,
	geneisRpcClientIfc genesisSvc.IGenesisRpcClient,
	ergonClient ergonClient.Ergon,
	dbClient db.CPDBClientInterface,
	idempotencySvc idempotencyutils.IdempotencyService,
	genesisJsonRpcClient genesisSvc.GenesisJsonRpcClientIfc,
	remoteUhuraClientMap map[string]uhura.UhuraClientInterface) {

	singleton = &singletonService{
		ergonService:         ergonServiceIfc,
		idfSvc:               idfSvcIfc,
		idfClient:            idfClientIfc,
		zkClient:             zkClientIfc,
		uuid:                 uuidIfc,
		marshal:              protoIfc,
		glog:                 glogIfc,
		remoteRestclient:     guruApiClientIfc,
		zkSession:            zkSessionIfc,
		eventForwarder:       eventForwarderIfc,
		idfWatcher:           idfWatcher,
		genesisRpcClient:     geneisRpcClientIfc,
		ergonClient:          ergonClient,
		dbClient:             dbClient,
		idempotencySvc:       idempotencySvc,
		genesisJsonRpcClient: genesisJsonRpcClient,
		remoteUhuraClientMap: remoteUhuraClientMap,
	}
}
