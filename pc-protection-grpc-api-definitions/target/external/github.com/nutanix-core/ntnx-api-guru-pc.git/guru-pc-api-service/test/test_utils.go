/*
* Copyright (c) 2024 Nutanix Inc. All rights reserved.
*
* Author: @moli237
*
* This file contains the utility functions for testing
 */

package test

import (
	"flag"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/grpc_error"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/external"
	"ntnx-api-guru-pc/guru-pc-api-service/mocks"
	"testing"

	"github.com/golang/mock/gomock"
	"github.com/golang/protobuf/proto"
	"github.com/stretchr/testify/mock"

	aplos "github.com/nutanix-core/go-cache/aplos/sl_bufs"
	ergonClientMocks "github.com/nutanix-core/go-cache/ergon/client/mocks"
	"github.com/nutanix-core/go-cache/insights/insights_interface"
	mockCpdb "github.com/nutanix-core/go-cache/nusights/util/db/mocks"
	"github.com/nutanix-core/go-cache/prism"
	uhura "github.com/nutanix-core/go-cache/uhura/client"
	mockUhura "github.com/nutanix-core/go-cache/uhura/client/mocks"
	zeus_config "github.com/nutanix-core/go-cache/zeus/config"
	zkSessionMocks "github.com/nutanix-core/go-cache/zeus/mocks"
	mockErgon "github.com/nutanix-core/ntnx-api-guru/services/ergon/mocks"
	mockEvent "github.com/nutanix-core/ntnx-api-guru/services/events/mocks"
	mockGuruApi "github.com/nutanix-core/ntnx-api-guru/services/guru_api/mocks"
	mockIdf "github.com/nutanix-core/ntnx-api-guru/services/idf/mocks"
	mockZk "github.com/nutanix-core/ntnx-api-guru/services/zk/mocks"
)

type MockServices struct {
	ErgonClient          *mockErgon.MockErgonClientIfc
	ZkClient             *mockZk.MockZkClientIfc
	EventForwarder       *mockEvent.MockEventForwarderIfc
	RemoteRestClient     *mockGuruApi.MockGuruApiClientIfc
	ZkSession            *zkSessionMocks.ZookeeperIfc
	IdfSession           *mocks.MockInsightsServiceInterface
	ErgonService         *ergonClientMocks.Ergon
	GenesisClient        *mocks.MockIGenesisRpcClient
	IdempotencySvc       *mocks.MockIdempotencyService
	GrpcStatusUtil       *mocks.MockGrpcStatusUtil
	Uuid                 *mocks.MockUuidInterface
	DbClient             *mockCpdb.CPDBClientInterface
	IdfClient            *mockIdf.MockIdfClientIfc
	IdfWatcher           *mocks.MockIdfWatcher
	GenesisJsonRpcClient *mocks.MockGenesisJsonRpcClientIfc
	RemoteUhuraClient    *mockUhura.UhuraClientInterface
}


var remoteUhuraClient *mockUhura.UhuraClientInterface
var remoteUhuraMap map[string]uhura.UhuraClientInterface

func SetMockUhuraClient(clusterUuid string) {
	if remoteUhuraMap == nil {
		remoteUhuraMap = make(map[string]uhura.UhuraClientInterface)
	}
	remoteUhuraClient = new(mockUhura.UhuraClientInterface)
	remoteUhuraMap[clusterUuid] = remoteUhuraClient
}

func MockSingletons(t *testing.T) *MockServices {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockErgonClient := mockErgon.NewMockErgonClientIfc(ctrl)
	mockZkSession := new(zkSessionMocks.ZookeeperIfc)
	mockZkClient := mockZk.NewMockZkClientIfc(ctrl)
	mockEventForwarder := mockEvent.NewMockEventForwarderIfc(ctrl)
	mockGuruApiClient := mockGuruApi.NewMockGuruApiClientIfc(ctrl)
	mockIdfSession := mocks.NewMockInsightsServiceInterface(ctrl)
	mockErgonService := new(ergonClientMocks.Ergon)
	mockGenesisService := mocks.NewMockIGenesisRpcClient(ctrl)
	mockIdempotencySvc := mocks.NewMockIdempotencyService(ctrl)
	mockGrpcUtil := mocks.NewMockGrpcStatusUtil(ctrl)
	grpc_error.SetGrpcStatusUtil(mockGrpcUtil)
	mockUuid := mocks.NewMockUuidInterface(ctrl)
	mockDbClient := new(mockCpdb.CPDBClientInterface)
	mockIdfClient := mockIdf.NewMockIdfClientIfc(ctrl)
	mockIdfWatcher := mocks.NewMockIdfWatcher(ctrl)
	mockGenesisRpcClient := mocks.NewMockGenesisJsonRpcClientIfc(ctrl)

	external.SetSingletonServices(mockErgonClient, mockIdfSession, mockIdfClient, mockZkClient,
		mockZkSession, mockUuid, nil, nil, mockGuruApiClient, mockEventForwarder, mockIdfWatcher,
		mockGenesisService, mockErgonService, mockDbClient, mockIdempotencySvc,
		mockGenesisRpcClient, remoteUhuraMap)
	return &MockServices{
		ErgonClient:          mockErgonClient,
		ZkClient:             mockZkClient,
		EventForwarder:       mockEventForwarder,
		RemoteRestClient:     mockGuruApiClient,
		ZkSession:            mockZkSession,
		IdfSession:           mockIdfSession,
		ErgonService:         mockErgonService,
		GenesisClient:        mockGenesisService,
		IdempotencySvc:       mockIdempotencySvc,
		GrpcStatusUtil:       mockGrpcUtil,
		Uuid:                 mockUuid,
		DbClient:             mockDbClient,
		IdfClient:            mockIdfClient,
		IdfWatcher:           mockIdfWatcher,
		GenesisJsonRpcClient: mockGenesisRpcClient,
		RemoteUhuraClient:    remoteUhuraClient,
	}
}

func MockFlagValues(t *testing.T) {
	// Mocking the flag values
	flag.Set("task-poll-initial-backoff", "0")
	flag.Set("task-poll-max-backoff", "0")
	flag.Parse()
}

func GetMockCES() *prism.ClusterExternalState{
    return &prism.ClusterExternalState{
		ClusterUuid: proto.String("cluster_uuid"),
		ClusterDetails: &prism.ClusterDetails{
			ClusterName:  proto.String("cluster_name"),
			Multicluster: proto.Bool(true),
			ContactInfo: &zeus_config.ConfigurationProto_NetworkEntity{
				AddressList: []string{"ip2"},
				Port:        proto.Int32(80),
			},
			ClusterFunctions: proto.Uint32(1),
		},
		ConfigDetails: &prism.ConfigDetails{
			ClusterFullyQualifiedDomainName: proto.String("fqdn"),
			ExternalIp:                      proto.String("ip1"),
		},
		PairingRole: prism.ClusterExternalState_kPairingInitiator.Enum(),
	}
}

func GetMockZeusConfig() []byte {
	var TestZeusConfigDTOBytes, _ = proto.Marshal(GetMockZeusProto())
	return TestZeusConfigDTOBytes
}

func GetMockZeusProto() *zeus_config.ConfigurationProto{
    return &zeus_config.ConfigurationProto{
		LogicalTimestamp: proto.Int64(0),
		NodeList: []*zeus_config.ConfigurationProto_Node{
			{
				ServiceVmId:     proto.Int64(123),
				UhuraUvmUuid:    proto.String("18142867-55f2-4f5e-8b8e-3d1e4b0ebc"),
				SoftwareVersion: proto.String("version"),
			},
		},
		Aegis: &zeus_config.ConfigurationProto_Aegis{
			AutoSupportConfig: &zeus_config.ConfigurationProto_Aegis_AutoSupportConfig{
				EmailAsups: &zeus_config.ConfigurationProto_Aegis_TimedBool{
					Value: proto.Bool(true),
				},
			},
		},
		PcClusterInfo: &zeus_config.ConfigurationProto_PCClusterInfo{
			HostInfo: &zeus_config.ConfigurationProto_PCClusterInfo_HostInfo{
				PrismElementList: []*zeus_config.ConfigurationProto_PCClusterInfo_HostInfo_PrismElement{
					{
						ClusterUuid: proto.String("4d5b88b0-3071-48df-87de-33a85118642c"),
					},
				},
			},
		},
		ReleaseVersion:    proto.String("version"),
		ClusterExternalIp: proto.String("external ip"),
		ClusterUuid:       proto.String("5d5b88b0-3071-48df-87de-33a85118642c"),
		ClusterName:       proto.String("PC_123.123.123.123"),
	}
}

func GetMockPCEnvConfig() []byte {
	var TestPCEnvConfig = &aplos.PCEnvironmentConfig{
		PcEnvironmentInfo: &aplos.PCEnvironmentConfig_PCEnvironmentInfo{
			EnvironmentType:   aplos.PCEnvironmentConfig_PCEnvironmentInfo_ONPREM.Enum(),
			CloudProviderInfo: aplos.PCEnvironmentConfig_PCEnvironmentInfo_NTNX.Enum(),
			InstanceType:      aplos.PCEnvironmentConfig_PCEnvironmentInfo_NTNX_PROVISIONED.Enum(),
		},
	}
	var TestPCEnvConfigBytes, _ = proto.Marshal(TestPCEnvConfig)
	return TestPCEnvConfigBytes
}

func MockDRUnpairQuerySuccess(m *MockServices) {
	// Mocking AZ Entity
	metricName := "uuid"
	metricValue := insights_interface.TimeValuePair{
		Value: &insights_interface.DataValue{
			ValueType: &insights_interface.DataValue_StrValue{
				StrValue: "123",
			},
		},
	}
	metrticRet := insights_interface.MetricData{
		Name:      &metricName,
		ValueList: []*insights_interface.TimeValuePair{&metricValue},
	}
	entityRet := insights_interface.EntityWithMetric{
		MetricDataList: []*insights_interface.MetricData{&metrticRet},
	}
	azEntityRetList := []*insights_interface.EntityWithMetric{&entityRet}
	m.DbClient.Mock.On("Query", mock.Anything).Return(azEntityRetList, nil).Once()

	// Mocking DR Config Entity
	drConfigEntityRetList := []*insights_interface.EntityWithMetric{}
	m.DbClient.Mock.On("Query", mock.Anything).Return(drConfigEntityRetList, nil).Once()

	// Mock Sync Protection Rules
	prEntityRetList := []*insights_interface.EntityWithMetric{}
	m.DbClient.Mock.On("Query", mock.Anything).Return(prEntityRetList, nil).Once()
}

func GetInvalidPrismCentralResponseWithoutIPs() string {
	response := `{
    "resources": {
        "should_auto_register": false,
        "is_registered_to_hosting_pe": true,
        "cmsp_config": {
            "platform_ip_block_list": [
                "192.168.5.2 192.168.5.64"
            ],
            "platform_network_configuration": {
                "subnet_mask": "255.255.255.0",
                "type": "kPrivateNetwork",
                "default_gateway": "192.168.5.1"
            },
            "pc_domain_name": "msp.pc-ccdi.nutanix.com",
            "cmsp_args": "{\"cmsp_pod_network\": \"10.100.0.0/16\", \"cmsp_service_ip_network\": \"10.200.32.0/24\"}"
        },
        "pc_environment": {
            "providerType": "NTNX",
            "environmentType": "ONPREM",
            "instanceType": "NTNX_PROVISIONED"
        },
        "pc_size": "kSmall",
        "version": "master",
        "pc_vm_list": [
            {
                "vm_name": "auto_pc_6667219357f2f32c40e46a6c0",
                "data_disk_size_bytes": 536870912000,
                "dns_server_ip_list": [
                    "127.0.0.1",
                    "10.40.64.15",
                    "10.40.64.16"
                ],
                "container_uuid": "28f9c852-fc3a-4ee2-ab3b-84eec0c2a1f0",
                "num_sockets": 6,
                "memory_size_bytes": 34359738368,
                "status": "NORMAL",
                "power_state": "ON",
                "vm_uuid": "a46fac8a-92f7-40a3-b10b-204fdf82578e",
                "ntp_server_list": [
                    "0.centos.pool.ntp.org",
                    "2.centos.pool.ntp.org",
                    "3.centos.pool.ntp.org",
                    "1.centos.pool.ntp.org"
                ],
                "cluster_reference": {
                    "kind": "cluster",
                    "uuid": "00061a8c-0d21-8d81-0000-000000019de0"
                }
            }
        ],
        "cluster_uuid": "02cd17ea-034e-4796-a1d8-be2d42a15d75",
        "type": "PC",
        "virtual_ip": "10.46.154.130"
    }
}`
	return response
}
func GetTestPrismCentralResponse() string {
	response := `{
    "resources": {
        "should_auto_register": false,
        "is_registered_to_hosting_pe": true,
        "cmsp_config": {
            "platform_ip_block_list": [
                "192.168.5.2 192.168.5.64"
            ],
            "platform_network_configuration": {
                "subnet_mask": "255.255.255.0",
                "type": "kPrivateNetwork",
                "default_gateway": "192.168.5.1"
            },
            "pc_domain_name": "msp.pc-ccdi.nutanix.com",
            "cmsp_args": "{\"cmsp_pod_network\": \"10.100.0.0/16\", \"cmsp_service_ip_network\": \"10.200.32.0/24\"}"
        },
        "pc_environment": {
            "providerType": "NTNX",
            "environmentType": "ONPREM",
            "instanceType": "NTNX_PROVISIONED"
        },
        "pc_size": "kSmall",
        "version": "master",
        "pc_vm_list": [
            {
                "vm_name": "auto_pc_6667219357f2f32c40e46a6c0",
                "data_disk_size_bytes": 536870912000,
                "dns_server_ip_list": [
                    "127.0.0.1",
                    "10.40.64.15",
                    "10.40.64.16"
                ],
                "container_uuid": "28f9c852-fc3a-4ee2-ab3b-84eec0c2a1f0",
                "nic_list": [
                    {
                        "network_configuration": {
                            "subnet_mask": "255.255.248.0",
                            "network_uuid": "f98323c9-a1f9-43ec-8c3c-0a7b3b00625c",
                            "default_gateway": "10.46.152.1"
                        },
                        "ip_list": [
                            "10.46.154.129"
                        ]
                    }
                ],
                "num_sockets": 6,
                "memory_size_bytes": 34359738368,
                "status": "NORMAL",
                "power_state": "ON",
                "vm_uuid": "a46fac8a-92f7-40a3-b10b-204fdf82578e",
                "ntp_server_list": [
                    "0.centos.pool.ntp.org",
                    "2.centos.pool.ntp.org",
                    "3.centos.pool.ntp.org",
                    "1.centos.pool.ntp.org"
                ],
                "cluster_reference": {
                    "kind": "cluster",
                    "uuid": "00061a8c-0d21-8d81-0000-000000019de0"
                }
            }
        ],
        "cluster_uuid": "02cd17ea-034e-4796-a1d8-be2d42a15d75",
        "type": "PC",
        "virtual_ip": "10.46.154.130"
    }
}`
	return response
}

func GetTestPrismCentralResponseWithInvalidVersionPC() string {
	response := `{
    "resources": {
        "should_auto_register": false,
        "is_registered_to_hosting_pe": true,
        "cmsp_config": {
            "platform_ip_block_list": [
                "192.168.5.2 192.168.5.64"
            ],
            "platform_network_configuration": {
                "subnet_mask": "255.255.255.0",
                "type": "kPrivateNetwork",
                "default_gateway": "192.168.5.1"
            },
            "pc_domain_name": "msp.pc-ccdi.nutanix.com",
            "cmsp_args": "{\"cmsp_pod_network\": \"10.100.0.0/16\", \"cmsp_service_ip_network\": \"10.200.32.0/24\"}"
        },
        "pc_environment": {
            "providerType": "NTNX",
            "environmentType": "ONPREM",
            "instanceType": "NTNX_PROVISIONED"
        },
        "pc_size": "kSmall",
        "version": "pc.2024.1",
        "pc_vm_list": [
            {
                "vm_name": "auto_pc_6667219357f2f32c40e46a6c0",
                "data_disk_size_bytes": 536870912000,
                "dns_server_ip_list": [
                    "127.0.0.1",
                    "10.40.64.15",
                    "10.40.64.16"
                ],
                "container_uuid": "28f9c852-fc3a-4ee2-ab3b-84eec0c2a1f0",
                "nic_list": [
                    {
                        "network_configuration": {
                            "subnet_mask": "255.255.248.0",
                            "network_uuid": "f98323c9-a1f9-43ec-8c3c-0a7b3b00625c",
                            "default_gateway": "10.46.152.1"
                        },
                        "ip_list": [
                            "10.46.154.129"
                        ]
                    }
                ],
                "num_sockets": 6,
                "memory_size_bytes": 34359738368,
                "status": "NORMAL",
                "power_state": "ON",
                "vm_uuid": "a46fac8a-92f7-40a3-b10b-204fdf82578e",
                "ntp_server_list": [
                    "0.centos.pool.ntp.org",
                    "2.centos.pool.ntp.org",
                    "3.centos.pool.ntp.org",
                    "1.centos.pool.ntp.org"
                ],
                "cluster_reference": {
                    "kind": "cluster",
                    "uuid": "00061a8c-0d21-8d81-0000-000000019de0"
                }
            }
        ],
        "cluster_uuid": "02cd17ea-034e-4796-a1d8-be2d42a15d75",
        "type": "PC",
        "virtual_ip": "10.46.154.130"
    }
}`
	return response
}

func GetMockTaskSuccessResponse() []byte {
	taskResponse := `{
		"data": {
			"extId": "ZXJnb24=:278bbfc2-280f-529b-882b-14ea649b9a95",
			"operation": "Register",
			"operationDescription": "Register PC",
			"createdTime": "2024-06-07T11:11:34.839373Z",
			"startedTime": "2024-06-07T11:11:34.85353Z",
			"completedTime": "2024-06-07T11:11:42.078697Z",
			"progressPercentage": 100,
			"entitiesAffected": [
				{
					"extId": "148ee646-25d0-46f6-9977-61ad02acc5e1",
					"rel": "prism:management:domain_manager",
					"name": "domain_manager",
					"$reserved": {
						"$fv": "v4.r0.b1"
					},
					"$objectType": "prism.v4.config.EntityReference"
				}
			],
			"isCancelable": false,
			"lastUpdatedTime": "2024-06-07T11:11:42.078696Z",
			"isBackgroundTask": false,
			"$reserved": {
				"$fv": "v4.r0.b1"
			},
			"$objectType": "prism.v4.config.Task",
			"status": "SUCCEEDED",
			"ownedBy": {
				"extId": "00000000-0000-0000-0000-000000000000",
				"name": "admin",
				"$reserved": {
					"$fv": "v4.r0.b1"
				},
				"$objectType": "prism.v4.config.OwnerReference"
			}
		},
		"$reserved": {
			"$fv": "v4.r0.b1"
		},
		"$objectType": "prism.v4.config.GetTaskApiResponse",
		"metadata": {
			"flags": [
				{
					"$reserved": {
						"$fv": "v1.r0.b1"
					},
					"$objectType": "common.v1.config.Flag",
					"name": "hasError",
					"value": false
				},
				{
					"$reserved": {
						"$fv": "v1.r0.b1"
					},
					"$objectType": "common.v1.config.Flag",
					"name": "isPaginated",
					"value": false
				}
			],
			"$reserved": {
				"$fv": "v1.r0.b1"
			},
			"$objectType": "common.v1.response.ApiResponseMetadata",
			"links": [
				{
					"$reserved": {
						"$fv": "v1.r0.b1"
					},
					"$objectType": "common.v1.response.ApiLink",
					"href": "https://10.36.240.63:9440/api/prism/v4.0.b1/config/tasks/ZXJnb24=:d006c6e3-23c7-59d9-a888-f8f20214e468",
					"rel": "self"
				}
			],
			"totalAvailableResults": 0,
			"extraInfo": [
				{
					"value": 0,
					"$reserved": {
						"$fv": "v1.r0.b1"
					},
					"$objectType": "common.v1.config.KVPair",
					"name": "numSubTasks"
				},
				{
					"value": 1,
					"name": "numEntitiesAffected"
				},
				{
					"value": 0,
					"name": "numClusterExtIds"
				}
			]
		}
	}`
	return []byte(taskResponse)
}

func GetMockTaskReferenceModelResponse() []byte {
	response := `{
    "data": {
        "$reserved": {
            "$fv": "v4.r0.b1"
        },
        "$objectType": "prism.v4.config.TaskReference",
        "extId": "ZXJnb24=:278bbfc2-280f-529b-882b-14ea649b9a95"
    },
    "$reserved": {
        "$fv": "v4.r0.b1"
    },
    "$objectType": "prism.v4.management.RegisterApiResponse",
    "metadata": {
        "flags": [
            {
                "$reserved": {
                    "$fv": "v1.r0.b1"
                },
                "$objectType": "common.v1.config.Flag",
                "name": "hasError",
                "value": false
            },
            {
                "$reserved": {
                    "$fv": "v1.r0.b1"
                },
                "$objectType": "common.v1.config.Flag",
                "name": "isPaginated",
                "value": false
            }
        ],
        "$reserved": {
            "$fv": "v1.r0.b1"
        },
        "$objectType": "common.v1.response.ApiResponseMetadata",
        "links": [
            {
                "$reserved": {
                    "$fv": "v1.r0.b1"
                },
                "$objectType": "common.v1.response.ApiLink",
                "href": "https://10.15.4.16:9440/api/prism/v4.0.b1/config/tasks/ZXJnb24=:278bbfc2-280f-529b-882b-14ea649b9a95",
                "rel": "self"
            }
        ]
    }
}`
	return []byte(response)
}
