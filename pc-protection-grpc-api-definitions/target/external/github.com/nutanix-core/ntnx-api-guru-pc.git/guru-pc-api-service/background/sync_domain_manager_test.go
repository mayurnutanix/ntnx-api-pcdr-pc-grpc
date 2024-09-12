/*
* Copyright (c) 2024 Nutanix Inc. All rights reserved.
* Author: viraj.aute@nutanix.com
* It contains the unit tests od sync_domain_manager.go file
 */

package background

import (
	"encoding/json"
	"errors"
	"net/http"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/test"
	"testing"

	common_models "github.com/nutanix-core/go-cache/api/pe"
	aplos "github.com/nutanix-core/go-cache/aplos/sl_bufs"
	common_consts "github.com/nutanix-core/ntnx-api-guru/services/constants"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"

	"github.com/golang/mock/gomock"
	"github.com/golang/protobuf/proto"
	"github.com/nutanix-core/go-cache/prism"
	zeus_config "github.com/nutanix-core/go-cache/zeus/config"
)

var (
	TestTaskUuid           = []byte{11, 205, 61, 133, 16, 76, 72, 134, 85, 211, 252, 41, 149, 19, 150, 228}
	TestTaskUuid2          = []byte{11, 201, 61, 133, 16, 76, 72, 134, 85, 211, 252, 41, 149, 19, 150, 228}
	TestFilterEntityCount  = 1
	TestTotalEntityCount   = 2
	TestTotalGroupCount    = 3
	TestEntityType         = "vm"
	TestFilteredGroupCount = 1
	TestGroupsResponseDTO  = &common_models.GroupsGetEntitiesResponse{
		FilteredEntityCount: &TestFilterEntityCount,
		TotalEntityCount:    &TestTotalEntityCount,
		TotalGroupCount:     &TestTotalGroupCount,
		EntityType:          &TestEntityType,
		FilteredGroupCount:  &TestFilteredGroupCount,
		GroupResults: &[]*common_models.GroupsGroupResult{
			{
				GroupSummaries: nil,
				EntityResults: &[]*common_models.GroupsEntity{
					{
						EntityId: proto.String("4d5b88b0-3071-48df-87de-33a85118642c"),
						Data: &[]*common_models.GroupsFieldData{
							{
								Buckets: nil,
								Name:    proto.String("memory_size_bytes"),
								Values: &[]*common_models.GroupsTimevaluePair{
									{
										Values: &[]string{"123"},
									},
								},
							},
						},
					},
				},
			},
		},
	}
	TestGroupsResponseBytes, _ = json.Marshal(TestGroupsResponseDTO)
	TestZeusConfigDTO          = &zeus_config.ConfigurationProto{
		LogicalTimestamp: proto.Int64(0),
		NodeList: []*zeus_config.ConfigurationProto_Node{
			{
				ServiceVmId:  proto.Int64(123),
				UhuraUvmUuid: proto.String("18142867-55f2-4f5e-8b8e-3d1e4b0ebc"),
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
	TestZeusConfigBytes, _   = proto.Marshal(TestZeusConfigDTO)
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

func TestSyncDomainManagerWithIdfJobExecuteSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	clusterUuid := "ClusterUuid"
	test.SetMockUhuraClient(clusterUuid)
	m := test.MockSingletons(t)

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
	syncJob := NewSyncDomainManagerWithIdfJob()

	m.IdfSession.EXPECT().SendMsgWithTimeout(consts.IdfGetOperation,
		gomock.Any(), gomock.Any(), consts.IdfBackoffForRetries, gomock.Any()).
		Return(nil)
	m.ZkClient.EXPECT().GetChildren(common_consts.TRUST_SETUP_NODE_PATH, true).Return(trustList, nil)
	m.ZkClient.EXPECT().GetZkNode(common_consts.TRUST_SETUP_NODE_PATH+"/"+clusterUuid, true).Return(marshalGetZkResp, nil).Times(1)
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(TestZeusConfigBytes, nil).Times(1)
	statusCode := 200
	m.RemoteRestClient.EXPECT().CallApi(address, consts.EnvoyPort, consts.GroupsUrl,
		http.MethodPost, gomock.Any(), gomock.Any(), gomock.Any(), nil,
		nil, nil, true).Return(TestGroupsResponseBytes, &statusCode, nil).Times(2)
	m.RemoteUhuraClient.On("VmGet", mock.AnythingOfType("*uhura.VmGetArg")).Return(nil, errors.New("test error"))
	m.GenesisJsonRpcClient.EXPECT().GetCmspStatus(gomock.Any()).
		Return(TestGetCmspResponseValueString, nil).Times(1)
	m.ZkClient.EXPECT().GetZkNode(consts.PCEnvZkNodePath, true).Return(TestEnvironmentInfoBytes, nil).Times(1)
	m.IdfSession.EXPECT().SendMsgWithTimeout(consts.IdfUpdateOperation, gomock.Any(),
		gomock.Any(), consts.IdfBackoffForRetries, gomock.Any()).Return(nil)
	syncJob.Execute()
}

func TestSyncDomainManagerWithIdfJobGetTimeInterval(t *testing.T) {
	syncJob := NewSyncDomainManagerWithIdfJob()
	actualTimeInterval := syncJob.GetTimeInterval()
	assert.Equal(t, *consts.SyncDomainManagerWithIdfJobTimeInterval, actualTimeInterval)
}
