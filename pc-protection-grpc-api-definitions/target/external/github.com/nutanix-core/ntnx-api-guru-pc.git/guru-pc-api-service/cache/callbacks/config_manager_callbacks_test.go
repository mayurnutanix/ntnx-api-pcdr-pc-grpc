package callbacks

import (
	"encoding/json"
	"net/http"
	config_manager "ntnx-api-guru-pc/guru-pc-api-service/cache/config"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/test"
	"testing"

	"github.com/golang/mock/gomock"
	"github.com/golang/protobuf/proto"
	common_models "github.com/nutanix-core/go-cache/api/pe"
	aplos "github.com/nutanix-core/go-cache/aplos/sl_bufs"
	zk "github.com/nutanix-core/go-cache/go-zookeeper"
	"github.com/nutanix-core/go-cache/prism"
	"github.com/nutanix-core/go-cache/uhura"
	zeus_config "github.com/nutanix-core/go-cache/zeus/config"
	common_consts "github.com/nutanix-core/ntnx-api-guru/services/constants"
	"github.com/stretchr/testify/mock"
)

var (
	TestFilterEntityCount  = 1
	TestTotalEntityCount   = 2
	TestTotalGroupCount    = 3
	TestFilteredGroupCount = 1
	TestContainerUuid      = "a8dac32b-5980-4f47-9f20-27acde7855c6"
	TestVmEntityType       = "vm"
	TestUhuruVmUuid        = "4d5b88b0-3071-48df-87de-33a85118642c"
	TestZeusConfigDTO      = &zeus_config.ConfigurationProto{
		LogicalTimestamp: proto.Int64(0),
		NodeList: []*zeus_config.ConfigurationProto_Node{
			{
				ServiceVmId:  proto.Int64(123),
				UhuraUvmUuid: proto.String(TestUhuruVmUuid),
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
		ClusterExternalIp: proto.String("external ip"),
		ClusterUuid:       proto.String("5d5b88b0-3071-48df-87de-33a85118642c"),
	}
	TestZeusConfigBytes, _  = proto.Marshal(TestZeusConfigDTO)
	TestClusterUuid         = "39d96c8a-1f32-4fdc-9515-4699b3fc2ecc"
	TestVmGroupsResponseDTO = &common_models.GroupsGetEntitiesResponse{
		FilteredEntityCount: &TestFilterEntityCount,
		TotalEntityCount:    &TestTotalEntityCount,
		TotalGroupCount:     &TestTotalGroupCount,
		EntityType:          &TestVmEntityType,
		FilteredGroupCount:  &TestFilteredGroupCount,
		GroupResults: &[]*common_models.GroupsGroupResult{
			{
				GroupSummaries: nil,
				EntityResults: &[]*common_models.GroupsEntity{
					{
						EntityId: proto.String(TestUhuruVmUuid),
						Data: &[]*common_models.GroupsFieldData{
							{
								Name: proto.String(consts.VmAttributeContainerUuid),
								Values: &[]*common_models.GroupsTimevaluePair{
									{
										Values: &[]string{
											TestContainerUuid,
										},
									},
								},
							},
						},
					},
				},
			},
		},
	}
	TestCmspPcDomainName     = "pcDomain"
	TestCmspSubnetMask       = "subnetMask"
	TestCmspDefaultGateway   = "defaultGateway"
	TestCmspInfraIpBlock     = []string{"192.168.5.2 192.168.5.64"}
	TestCmspType             = "type"
	TestCmspVirtualUuid      = "virtualUuid"
	TestGetCmspResponseValue = map[string]interface{}{
		".return": map[string]interface{}{
			"config": map[string]interface{}{
				"cmsp_config": map[string]interface{}{
					"pcDomain": TestCmspPcDomainName,
					"infraNetwork": map[string]interface{}{
						"netmaskIpv4Address": TestCmspSubnetMask,
						"gatewayIpv4Address": TestCmspDefaultGateway,
						"virtualNetworkUuid": TestCmspVirtualUuid,
					},
					"infraIpBlock": TestCmspInfraIpBlock,
					"type":         TestCmspType,
				},
			},
		},
	}
	TestGetCmspResponseValueByte, _ = json.Marshal(TestGetCmspResponseValue)
	TestVmGroupsResponseBytes, _    = json.Marshal(TestVmGroupsResponseDTO)
	TestGetCmspResponseValueString  = string(TestGetCmspResponseValueByte)
	TestEnvironmentInfo             = &aplos.PCEnvironmentConfig{
		PcEnvironmentInfo: &aplos.PCEnvironmentConfig_PCEnvironmentInfo{
			EnvironmentType:   aplos.PCEnvironmentConfig_PCEnvironmentInfo_NTNX_CLOUD.Enum(),
			CloudProviderInfo: aplos.PCEnvironmentConfig_PCEnvironmentInfo_NTNX.Enum(),
			InstanceType:      aplos.PCEnvironmentConfig_PCEnvironmentInfo_NTNX_PROVISIONED.Enum(),
		},
	}
	TestEnvironmentInfoBytes, _ = proto.Marshal(TestEnvironmentInfo)
)

func TestConfigChangeCb(t *testing.T) {
	config_manager.SetConfigurationCache(test.GetMockZeusProto())
	ConfigChangeCb(&zk.Conn{}, test.GetMockZeusConfig())
}

func TestExecuteWorkflows(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	test.SetMockUhuraClient(TestClusterUuid)
	m := test.MockSingletons(t)
	// mocks for SaveDomainManagerEntityInIdf
	clusterUuid := TestClusterUuid
	address := "address"
	var trustList, addressList []string
	trustList = append(trustList, clusterUuid)
	addressList = append(addressList, address)
	trustState := new(prism.ClusterExternalState)
	trustState.ClusterDetails = &prism.ClusterDetails{
		ContactInfo: &zeus_config.ConfigurationProto_NetworkEntity{
			AddressList: addressList,
		},
	}
	marshalGetZkResp, _ := proto.Marshal(trustState)

	m.IdfSession.EXPECT().SendMsgWithTimeout(consts.IdfGetOperation,
		gomock.Any(), gomock.Any(), consts.IdfBackoffForRetries, gomock.Any()).
		Return(nil)
	m.ZkClient.EXPECT().GetChildren(common_consts.TRUST_SETUP_NODE_PATH, true).Return(trustList, nil)
	m.ZkClient.EXPECT().GetZkNode(common_consts.TRUST_SETUP_NODE_PATH+"/"+clusterUuid, true).Return(marshalGetZkResp, nil).Times(1)
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(TestZeusConfigBytes, nil).Times(1)
	statusCode := 200
	m.RemoteRestClient.EXPECT().CallApi(address, consts.EnvoyPort, consts.GroupsUrl,
		http.MethodPost, gomock.Any(), gomock.Any(), gomock.Any(), nil,
		nil, nil, true).Return(TestVmGroupsResponseBytes, &statusCode, nil).Times(2)
	m.RemoteUhuraClient.On("VmGet", mock.AnythingOfType("*uhura.VmGetArg")).Return(&uhura.VmGetRet{}, nil)
	m.GenesisJsonRpcClient.EXPECT().GetCmspStatus(gomock.Any()).
		Return(TestGetCmspResponseValueString, nil).Times(1)
	m.ZkClient.EXPECT().GetZkNode(consts.PCEnvZkNodePath, true).Return(TestEnvironmentInfoBytes, nil).Times(1)
	// m.IdfSession.EXPECT().SendMsgWithTimeout(consts.IdfUpdateOperation, gomock.Any(),
	// 	gomock.Any(), consts.IdfBackoffForRetries, gomock.Any()).Return(nil)

	NewConfigOnChangeAction(test.GetMockZeusProto(), test.GetMockZeusProto()).Execute()
}
