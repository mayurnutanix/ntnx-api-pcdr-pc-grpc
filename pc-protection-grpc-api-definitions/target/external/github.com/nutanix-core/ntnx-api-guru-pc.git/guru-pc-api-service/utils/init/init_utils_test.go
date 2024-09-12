package init

import (
	"encoding/json"
	"errors"
	"net/http"
	"ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/config"
	domainManagerConfig "ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/config"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/mocks"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/test"
	"ntnx-api-guru-pc/guru-pc-api-service/utils"
	"testing"

	"github.com/golang/mock/gomock"
	"github.com/golang/protobuf/proto"
	common_models "github.com/nutanix-core/go-cache/api/pe"
	aplos "github.com/nutanix-core/go-cache/aplos/sl_bufs"
	"github.com/nutanix-core/go-cache/ergon"
	"github.com/nutanix-core/go-cache/prism"
	"github.com/nutanix-core/go-cache/uhura"
	"github.com/nutanix-core/go-cache/util-go/uuid4"
	zeus_config "github.com/nutanix-core/go-cache/zeus/config"
	common_consts "github.com/nutanix-core/ntnx-api-guru/services/constants"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
)

var (
	TestTaskUuid           = []byte{11, 205, 61, 133, 16, 76, 72, 134, 85, 211, 252, 41, 149, 19, 150, 228}
	TestTaskUuid2          = []byte{11, 201, 61, 133, 16, 76, 72, 134, 85, 211, 252, 41, 149, 19, 150, 228}
	TestNetworkUuid        = []byte{11, 209, 61, 133, 16, 76, 72, 134, 85, 211, 252, 41, 149, 19, 150, 228}
	TestFilterEntityCount  = 1
	TestTotalEntityCount   = 2
	TestTotalGroupCount    = 3
	TestVmEntityType       = "vm"
	TestUhuruVmUuid        = "4d5b88b0-3071-48df-87de-33a85118642c"
	TestUhuraVmUuidBytes   = []byte(TestUhuruVmUuid)
	TestContainerUuid      = "a8dac32b-5980-4f47-9f20-27acde7855c6"
	TestDiskCapacity       = "1231213"
	TestDiskCapacityInt    = 1231213
	TestMemorySizeMb       = int64(123)
	TestMemorySizeBytes    = TestMemorySizeMb * 1024 * 1024
	TestNumVcpus           = int64(6)
	TestVmName             = "vm-name"
	TestClusterUuid        = "39d96c8a-1f32-4fdc-9515-4699b3fc2ecc"
	TestFilteredGroupCount = 1
	TestVmGetRet           = &uhura.VmGetRet{
		VmInfoList: []*uhura.VmInfo{
			{
				VmUuid: TestUhuraVmUuidBytes,
				Config: &uhura.VmConfig{
					Name:         proto.String(TestVmName),
					MemorySizeMb: proto.Uint64(uint64(TestMemorySizeMb)),
					NumVcpus:     proto.Uint32(uint32(TestNumVcpus)),
					NicList: []*uhura.VmNicConfig{
						{
							NetworkUuid: TestNetworkUuid,
						},
					},
				},
			},
		},
	}
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
	TestClusterUuidAsUuid4, _    = uuid4.StringToUuid4(TestClusterUuid)
	TestVmGroupsResponseBytes, _ = json.Marshal(TestVmGroupsResponseDTO)
	TestDiskGroupsResponseDTO    = &common_models.GroupsGetEntitiesResponse{
		FilteredEntityCount: &TestFilterEntityCount,
		TotalEntityCount:    &TestTotalEntityCount,
		TotalGroupCount:     &TestTotalGroupCount,
		EntityType:          proto.String(consts.DiskEntityType),
		FilteredGroupCount:  &TestFilteredGroupCount,
		GroupResults: &[]*common_models.GroupsGroupResult{
			{
				GroupSummaries: nil,
				EntityResults: &[]*common_models.GroupsEntity{
					{
						EntityId: proto.String(TestUhuruVmUuid),
						Data: &[]*common_models.GroupsFieldData{
							{
								Name: proto.String(consts.DiskAttributeDiskCapacity),
								Values: &[]*common_models.GroupsTimevaluePair{
									{
										Values: &[]string{
											TestDiskCapacity,
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
	TestDiskGroupsResponseBytes, _ = json.Marshal(TestDiskGroupsResponseDTO)
	TestZeusConfigDTO              = &zeus_config.ConfigurationProto{
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
	TestZeusConfigBytes, _ = proto.Marshal(TestZeusConfigDTO)
	TestInternalOpaque     = []byte{123, 34, 78, 97, 109, 101, 34, 58, 49, 44, 34, 82, 111, 108, 108, 98, 97, 99, 107, 34, 58, 102, 97, 108, 115, 101, 44, 34, 83, 116, 101, 112, 115, 67, 111, 109, 112, 108, 101, 116, 101, 100, 34, 58, 51, 44, 34, 84, 97, 115, 107, 73, 100, 34, 58, 34, 99, 97, 51, 101, 50, 48, 100, 101, 45, 98, 49, 100, 54, 45, 52, 49, 56, 56, 45, 54, 49, 55, 98, 45, 48, 99, 102, 52, 51, 57, 101, 55, 51, 50, 48, 50, 34, 44, 34, 80, 97, 114, 101, 110, 116, 84, 97, 115, 107, 73, 100, 34, 58, 34, 121, 106, 52, 103, 51, 114, 72, 87, 81, 89, 104, 104, 101, 119, 122, 48, 79, 101, 99, 121, 65, 103, 61, 61, 34, 44, 34, 83, 116, 101, 112, 115, 84, 111, 84, 97, 115, 107, 77, 97, 112, 34, 58, 123, 34, 48, 34, 58, 34, 88, 110, 115, 69, 104, 114, 65, 50, 83, 66, 49, 75, 113, 81, 109, 116, 79, 112, 104, 98, 71, 65, 61, 61, 34, 44, 34, 49, 34, 58, 34, 89, 84, 52, 88, 117, 99, 74, 68, 83, 102, 120, 73, 54, 101, 55, 115, 119, 74, 106, 54, 111, 81, 61, 61, 34, 44, 34, 50, 34, 58, 34, 100, 57, 83, 52, 51, 116, 97, 113, 84, 108, 120, 79, 66, 50, 101, 65, 110, 106, 69, 49, 55, 103, 61, 61, 34, 125, 44, 34, 85, 115, 101, 84, 114, 117, 115, 116, 34, 58, 102, 97, 108, 115, 101, 44, 34, 79, 112, 101, 114, 97, 116, 105, 111, 110, 69, 114, 114, 111, 114, 34, 58, 34, 34, 44, 34, 69, 114, 114, 111, 114, 83, 116, 101, 112, 34, 58, 48, 44, 34, 69, 114, 114, 111, 114, 68, 101, 116, 97, 105, 108, 115, 34, 58, 110, 117, 108, 108, 44, 34, 82, 101, 109, 111, 116, 101, 84, 97, 115, 107, 73, 100, 34, 58, 34, 57, 101, 52, 54, 49, 55, 51, 102, 45, 50, 101, 50, 54, 45, 52, 52, 101, 57, 45, 56, 99, 98, 99, 45, 50, 54, 98, 51, 100, 101, 52, 52, 57, 99, 51, 55, 34, 44, 34, 73, 115, 79, 112, 101, 114, 97, 116, 105, 111, 110, 67, 111, 109, 112, 108, 101, 116, 101, 34, 58, 102, 97, 108, 115, 101, 44, 34, 67, 111, 110, 116, 101, 120, 116, 73, 100, 34, 58, 34, 57, 99, 53, 54, 102, 49, 56, 101, 45, 50, 51, 48, 52, 45, 52, 100, 52, 48, 45, 56, 55, 101, 98, 45, 57, 102, 49, 49, 97, 50, 98, 56, 100, 52, 48, 100, 34, 44, 34, 79, 112, 101, 114, 97, 116, 105, 111, 110, 83, 116, 97, 114, 116, 84, 105, 109, 101, 34, 58, 34, 50, 48, 50, 52, 45, 48, 53, 45, 48, 50, 84, 49, 55, 58, 53, 49, 58, 49, 55, 90, 34, 44, 34, 82, 101, 115, 111, 117, 114, 99, 101, 69, 120, 116, 73, 100, 34, 58, 34, 49, 98, 49, 49, 98, 100, 100, 54, 45, 53, 52, 56, 97, 45, 52, 56, 99, 55, 45, 98, 52, 100, 51, 45, 98, 53, 100, 100, 99, 53, 54, 98, 98, 50, 51, 50, 34, 44, 34, 73, 115, 82, 101, 99, 111, 118, 101, 114, 101, 100, 84, 97, 115, 107, 34, 58, 102, 97, 108, 115, 101, 44, 34, 82, 101, 109, 111, 116, 101, 67, 108, 117, 115, 116, 101, 114, 84, 121, 112, 101, 34, 58, 34, 36, 85, 78, 75, 78, 79, 87, 78, 34, 44, 34, 82, 101, 109, 111, 116, 101, 67, 108, 117, 115, 116, 101, 114, 73, 100, 34, 58, 34, 48, 48, 48, 54, 49, 55, 55, 57, 45, 49, 102, 99, 102, 45, 98, 100, 52, 97, 45, 52, 56, 55, 48, 45, 97, 99, 49, 102, 54, 98, 49, 54, 101, 98, 97, 51, 34, 44, 34, 82, 101, 109, 111, 116, 101, 86, 101, 114, 115, 105, 111, 110, 34, 58, 34, 109, 97, 115, 116, 101, 114, 34, 44, 34, 82, 101, 109, 111, 116, 101, 65, 100, 100, 114, 101, 115, 115, 34, 58, 34, 49, 48, 46, 52, 54, 46, 50, 55, 46, 49, 50, 53, 34, 44, 34, 82, 101, 109, 111, 116, 101, 67, 108, 117, 115, 116, 101, 114, 78, 97, 109, 101, 34, 58, 34, 34, 44, 34, 83, 101, 108, 102, 67, 108, 117, 115, 116, 101, 114, 84, 121, 112, 101, 34, 58, 34, 36, 85, 78, 75, 78, 79, 87, 78, 34, 44, 34, 83, 101, 108, 102, 67, 108, 117, 115, 116, 101, 114, 73, 100, 34, 58, 34, 49, 98, 49, 49, 98, 100, 100, 54, 45, 53, 52, 56, 97, 45, 52, 56, 99, 55, 45, 98, 52, 100, 51, 45, 98, 53, 100, 100, 99, 53, 54, 98, 98, 50, 51, 50, 34, 44, 34, 83, 101, 108, 102, 67, 108, 117, 115, 116, 101, 114, 86, 101, 114, 115, 105, 111, 110, 34, 58, 34, 101, 108, 56, 46, 53, 45, 111, 112, 116, 45, 109, 97, 115, 116, 101, 114, 45, 57, 55, 56, 51, 100, 99, 101, 99, 98, 52, 53, 50, 53, 57, 101, 51, 52, 98, 50, 57, 53, 55, 99, 97, 101, 53, 100, 100, 55, 101, 52, 49, 102, 98, 51, 50, 98, 49, 54, 56, 34, 44, 34, 83, 101, 108, 102, 69, 120, 116, 101, 114, 110, 97, 108, 65, 100, 100, 114, 101, 115, 115, 34, 58, 34, 34, 44, 34, 83, 101, 108, 102, 67, 108, 117, 115, 116, 101, 114, 78, 97, 109, 101, 34, 58, 34, 34, 44, 34, 82, 101, 113, 117, 101, 115, 116, 67, 111, 110, 116, 101, 120, 116, 34, 58, 123, 34, 117, 115, 101, 114, 95, 117, 117, 105, 100, 34, 58, 34, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 61, 61, 34, 44, 34, 117, 115, 101, 114, 95, 110, 97, 109, 101, 34, 58, 34, 97, 100, 109, 105, 110, 34, 44, 34, 117, 115, 101, 114, 95, 105, 112, 34, 58, 34, 49, 48, 46, 49, 51, 56, 46, 50, 51, 52, 46, 49, 56, 34, 125, 44, 34, 73, 115, 76, 111, 99, 97, 108, 79, 110, 108, 121, 34, 58, 102, 97, 108, 115, 101, 125}
	TestEnvironmentInfo    = &aplos.PCEnvironmentConfig{
		PcEnvironmentInfo: &aplos.PCEnvironmentConfig_PCEnvironmentInfo{
			EnvironmentType:   aplos.PCEnvironmentConfig_PCEnvironmentInfo_NTNX_CLOUD.Enum(),
			CloudProviderInfo: aplos.PCEnvironmentConfig_PCEnvironmentInfo_NTNX.Enum(),
			InstanceType:      aplos.PCEnvironmentConfig_PCEnvironmentInfo_NTNX_PROVISIONED.Enum(),
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
	TestGetCmspResponseValueString  = string(TestGetCmspResponseValueByte)
	TestGetCmspResponse             = map[string]interface{}{
		"value": TestGetCmspResponseValueString,
	}
	TestGetCmspResponseBytes, _ = json.Marshal(TestGetCmspResponse)
	TestEnvironmentInfoBytes, _ = proto.Marshal(TestEnvironmentInfo)
	ErrTest                     = errors.New("test error")
)

func testVmGetArg() *uhura.VmGetArg {
	var TestUhuraVmUuidList [][]byte
	TestUhuraVmUuidList = append(TestUhuraVmUuidList, TestUhuraVmUuidBytes)
	return &uhura.VmGetArg{
		VmUuidList:       TestUhuraVmUuidList,
		IncludeNicConfig: proto.Bool(true),
	}
}

func TestPopulateJobQueueSuccess(t *testing.T) {
	// Create a mock queue
	taskList := make([][]byte, 2)
	taskList[0] = TestTaskUuid
	taskList[1] = TestTaskUuid2

	task1 := &ergon.Task{
		Uuid:           TestTaskUuid,
		Status:         ergon.Task_kRunning.Enum(),
		InternalOpaque: TestInternalOpaque,
	}
	task2 := &ergon.Task{
		Uuid:   TestTaskUuid2,
		Status: ergon.Task_kAborted.Enum(),
	}

	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockQueue := mocks.NewMockQueue(ctrl)
	m := test.MockSingletons(t)
	m.ErgonClient.EXPECT().ListTask(
		nil, consts.GuruParentTasksList, []string{consts.GuruTaskComponent},
	).Return(taskList, nil)

	m.ErgonClient.EXPECT().GetTaskByUuid(TestTaskUuid).Return(task1, nil)
	m.ErgonClient.EXPECT().GetTaskByUuid(TestTaskUuid2).Return(task2, nil)
	mockQueue.EXPECT().Enqueue(gomock.Any())

	PopulateJobQueue(mockQueue)
}

func TestPopulateJobQueueListTaskFailure(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockQueue := mocks.NewMockQueue(ctrl)
	m := test.MockSingletons(t)
	m.ErgonClient.EXPECT().ListTask(
		nil, consts.GuruParentTasksList, []string{consts.GuruTaskComponent},
	).Return(nil, ErrTest)

	PopulateJobQueue(mockQueue)
}

func TestPopulateJobQueueGetTaskFailureAndUnmarshalFailure(t *testing.T) {
	// Create a mock queue
	taskList := make([][]byte, 2)
	taskList[0] = TestTaskUuid
	taskList[1] = TestTaskUuid2

	task1 := &ergon.Task{
		Uuid:           TestTaskUuid,
		Status:         ergon.Task_kRunning.Enum(),
		InternalOpaque: []byte("abc"),
	}

	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockQueue := mocks.NewMockQueue(ctrl)
	m := test.MockSingletons(t)
	m.ErgonClient.EXPECT().ListTask(
		nil, consts.GuruParentTasksList, []string{consts.GuruTaskComponent},
	).Return(taskList, nil)

	m.ErgonClient.EXPECT().GetTaskByUuid(TestTaskUuid).Return(task1, nil).Times(1)
	m.ErgonClient.EXPECT().GetTaskByUuid(TestTaskUuid2).Return(nil, ErrTest).Times(1)
	m.ErgonClient.EXPECT().UpdateTask(task1.Uuid, ergon.Task_kFailed,
		"Unable to recover task due to invalid task state", nil, nil,
		guru_error.GetInternalError("Operation").GetTaskErrorDetails(), nil).Return(nil)

	PopulateJobQueue(mockQueue)
}

func TestPopulateVmAndZeusDetailsInDomainManagerSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	test.SetMockUhuraClient(TestClusterUuid)
	m := test.MockSingletons(t)
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
	m.ZkClient.EXPECT().GetChildren(common_consts.TRUST_SETUP_NODE_PATH, true).Return(trustList, nil).Times(1)
	m.ZkClient.EXPECT().GetZkNode(common_consts.TRUST_SETUP_NODE_PATH+"/"+clusterUuid, true).Return(marshalGetZkResp, nil).Times(1)
	m.ZkClient.EXPECT().GetZkNode(common_consts.TRUST_SETUP_NODE_PATH+"/"+clusterUuid, true).Return(marshalGetZkResp, nil).Times(1)
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(TestZeusConfigBytes, nil).AnyTimes()
	statusCode := 200
	m.RemoteRestClient.EXPECT().CallApi(address, consts.EnvoyPort, consts.GroupsUrl,
		http.MethodPost, gomock.Any(), gomock.Any(), gomock.Any(), nil,
		nil, nil, true).Return(TestVmGroupsResponseBytes, &statusCode, nil).Times(1)
	m.RemoteRestClient.EXPECT().CallApi(address, consts.EnvoyPort, consts.GroupsUrl,
		http.MethodPost, gomock.Any(), gomock.Any(), gomock.Any(), nil,
		nil, nil, true).Return(TestDiskGroupsResponseBytes, &statusCode, nil).Times(1)

	m.RemoteUhuraClient.On("VmGet", mock.AnythingOfType("*uhura.VmGetArg")).Return(TestVmGetRet, nil)
	expectedDomainManagerDetails := utils.InitialiseDomainManagerEntity()
	expectedDomainManagerDetails.Base.ExtId = proto.String("5d5b88b0-3071-48df-87de-33a85118642c")
	expectedDomainManagerDetails.Config.Name = proto.String("Unnamed")
	size := config.SizeMessage_UNKNOWN
	expectedDomainManagerDetails.Config.Size = &size
	expectedDomainManagerDetails.HostingClusterExtId = proto.String(clusterUuid)
	expectedDomainManagerDetails.Config.ResourceConfig = &domainManagerConfig.DomainManagerResourceConfig{
		ContainerExtIds: &domainManagerConfig.StringArrayWrapper{
			Value: []string{TestContainerUuid},
		},
		MemorySizeBytes:   proto.Int64(TestMemorySizeBytes),
		NumVcpus:          proto.Int32(int32(TestNumVcpus)),
		DataDiskSizeBytes: proto.Int64(int64(TestDiskCapacityInt)),
	}
	expectedDomainManagerDetails.ShouldEnableHighAvailability = proto.Bool(false)
	expectedDomainManagerDetails.NodeExtIds = &domainManagerConfig.StringArrayWrapper{
		Value: []string{"4d5b88b0-3071-48df-87de-33a85118642c"},
	}

	expectedDomainManagerNodesInfo := utils.InitialiseDomainManagerNodeInfo()
	vmDetails := &models.VmDetails{
		VmSpecs: &models.VmSpecs{
			Uuid:              proto.String(TestUhuruVmUuid),
			ContainerExtId:    proto.String(TestContainerUuid),
			DataDiskSizeBytes: proto.Int64(int64(TestDiskCapacityInt)),
			MemorySizeBytes:   proto.Int64(TestMemorySizeBytes),
			NumVcpus:          proto.Int64(TestNumVcpus),
			VmName:            proto.String(TestVmName),
		},
	}
	expectedDomainManagerNodesInfo.NetworkExtId = proto.String(uuid4.ToUuid4(TestNetworkUuid).String())
	expectedDomainManagerNodesInfo.VmDetailsList = append(expectedDomainManagerNodesInfo.VmDetailsList, vmDetails)

	domainManagerDetails := utils.InitialiseDomainManagerEntity()
	domainManagerNodesInfo := utils.InitialiseDomainManagerNodeInfo()
	err := populateVmAndZeusDetailsInDomainManager(domainManagerDetails, domainManagerNodesInfo)

	assert.Equal(t, expectedDomainManagerDetails, domainManagerDetails)
	assert.Equal(t, expectedDomainManagerNodesInfo, domainManagerNodesInfo)
	assert.Nil(t, err)
}

func TestPopulateVmAndZeusDetailsInDomainManagerZkGetChildrenFailure(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	m := test.MockSingletons(t)
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(TestZeusConfigBytes, nil).Times(1)
	m.ZkClient.EXPECT().GetChildren(common_consts.TRUST_SETUP_NODE_PATH, true).Return(nil, ErrTest).Times(1)
	expectedDomainManagerDetails := utils.InitialiseDomainManagerEntity()
	expectedDomainManagerNodesInfo := utils.InitialiseDomainManagerNodeInfo()
	expectedDomainManagerDetails.GetConfig().Name = proto.String("Unnamed")
	expectedDomainManagerDetails.HostingClusterExtId = proto.String("4d5b88b0-3071-48df-87de-33a85118642c")
	expectedDomainManagerDetails.Base.ExtId = proto.String("5d5b88b0-3071-48df-87de-33a85118642c")
	size := config.SizeMessage_UNKNOWN
	expectedDomainManagerDetails.Config.Size = &size

	domainManagerDetails := utils.InitialiseDomainManagerEntity()
	domainManagerNodesInfo := utils.InitialiseDomainManagerNodeInfo()
	err := populateVmAndZeusDetailsInDomainManager(domainManagerDetails, domainManagerNodesInfo)
	assert.NotNil(t, err)
	assert.Equal(t, expectedDomainManagerDetails, domainManagerDetails)
	assert.Equal(t, expectedDomainManagerNodesInfo, domainManagerNodesInfo)
}

func TestPopulateVmAndZeusDetailsInDomainManagerZkGetDataFailure(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	m := test.MockSingletons(t)
	clusterUuid := TestClusterUuid
	var trustList []string
	trustList = append(trustList, clusterUuid)

	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(TestZeusConfigBytes, nil).Times(1)
	m.ZkClient.EXPECT().GetChildren(common_consts.TRUST_SETUP_NODE_PATH, true).Return(trustList, nil).Times(1)
	m.ZkClient.EXPECT().GetZkNode(common_consts.TRUST_SETUP_NODE_PATH+"/"+clusterUuid, true).Return(nil, ErrTest).Times(1)
	m.ZkClient.EXPECT().GetZkNode(common_consts.TRUST_SETUP_NODE_PATH+"/"+clusterUuid, true).Return(nil, ErrTest).Times(1)
	expectedDomainManagerDetails := utils.InitialiseDomainManagerEntity()
	expectedDomainManagerNodesInfo := utils.InitialiseDomainManagerNodeInfo()
	expectedDomainManagerDetails.GetConfig().Name = proto.String("Unnamed")
	expectedDomainManagerDetails.HostingClusterExtId = &clusterUuid
	expectedDomainManagerDetails.Base.ExtId = proto.String("5d5b88b0-3071-48df-87de-33a85118642c")
	size := config.SizeMessage_UNKNOWN
	expectedDomainManagerDetails.Config.Size = &size

	domainManagerDetails := utils.InitialiseDomainManagerEntity()
	domainManagerNodesInfo := utils.InitialiseDomainManagerNodeInfo()
	err := populateVmAndZeusDetailsInDomainManager(domainManagerDetails, domainManagerNodesInfo)
	assert.NotNil(t, err)
	assert.Equal(t, expectedDomainManagerDetails, domainManagerDetails)
	assert.Equal(t, expectedDomainManagerNodesInfo, domainManagerNodesInfo)
}

func TestPopulateVmAndZeusDetailsInDomainManagerFetchZeusConfigFailure(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	m := test.MockSingletons(t)
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(nil, ErrTest).Times(1)
	expectedDomainManagerDetails := utils.InitialiseDomainManagerEntity()
	expectedDomainManagerNodesInfo := utils.InitialiseDomainManagerNodeInfo()

	domainManagerDetails := utils.InitialiseDomainManagerEntity()
	domainManagerNodesInfo := utils.InitialiseDomainManagerNodeInfo()
	err := populateVmAndZeusDetailsInDomainManager(domainManagerDetails, domainManagerNodesInfo)
	assert.NotNil(t, err)
	assert.Equal(t, expectedDomainManagerDetails, domainManagerDetails)
	assert.Equal(t, expectedDomainManagerNodesInfo, domainManagerNodesInfo)
}

func TestPopulateVmAndZeusDetailsInDomainManagerInvalidZeusConfig(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	m := test.MockSingletons(t)
	zeusConfigurationDTO := &zeus_config.ConfigurationProto{}
	zeusConfigurationDTO.LogicalTimestamp = proto.Int64(0)
	marshalZeusConfig, _ := proto.Marshal(zeusConfigurationDTO)

	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(marshalZeusConfig, nil).Times(1)
	expectedDomainManagerDetails := utils.InitialiseDomainManagerEntity()
	expectedDomainManagerNodesInfo := utils.InitialiseDomainManagerNodeInfo()

	domainManagerDetails := utils.InitialiseDomainManagerEntity()
	domainManagerNodesInfo := utils.InitialiseDomainManagerNodeInfo()
	err := populateVmAndZeusDetailsInDomainManager(domainManagerDetails, domainManagerNodesInfo)
	assert.NotNil(t, err)
	assert.Equal(t, expectedDomainManagerDetails, domainManagerDetails)
	assert.Equal(t, expectedDomainManagerNodesInfo, domainManagerNodesInfo)
}

func TestPopulateVmAndZeusDetailsInDomainManagerGroupsCallFailure(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	test.SetMockUhuraClient(TestClusterUuid)
	m := test.MockSingletons(t)
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
	m.ZkClient.EXPECT().GetChildren(common_consts.TRUST_SETUP_NODE_PATH, true).Return(trustList, nil).Times(1)
	m.ZkClient.EXPECT().GetZkNode(common_consts.TRUST_SETUP_NODE_PATH+"/"+clusterUuid, true).Return(marshalGetZkResp, nil).Times(1)
	m.ZkClient.EXPECT().GetZkNode(common_consts.TRUST_SETUP_NODE_PATH+"/"+clusterUuid, true).Return(marshalGetZkResp, nil).Times(1)
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(TestZeusConfigBytes, nil).Times(1)
	statusCode := 503
	m.RemoteRestClient.EXPECT().CallApi(address, consts.EnvoyPort, consts.GroupsUrl, http.MethodPost, gomock.Any(), gomock.Any(), gomock.Any(), nil, nil, nil, true).Return(nil, &statusCode, ErrTest).Times(2)
	m.RemoteUhuraClient.On("VmGet", mock.AnythingOfType("*uhura.VmGetArg")).Return(nil, ErrTest)
	expectedDomainManagerDetails := utils.InitialiseDomainManagerEntity()
	expectedDomainManagerDetails.Config.Name = proto.String("Unnamed")
	expectedDomainManagerDetails.ShouldEnableHighAvailability = proto.Bool(false)
	expectedDomainManagerDetails.NodeExtIds = &domainManagerConfig.StringArrayWrapper{}
	expectedDomainManagerDetails.Config.ResourceConfig = &domainManagerConfig.DomainManagerResourceConfig{
		ContainerExtIds:   &domainManagerConfig.StringArrayWrapper{},
		MemorySizeBytes:   proto.Int64(0),
		NumVcpus:          proto.Int32(0),
		DataDiskSizeBytes: proto.Int64(0),
	}
	expectedDomainManagerDetails.HostingClusterExtId = &clusterUuid
	expectedDomainManagerDetails.Base.ExtId = proto.String("5d5b88b0-3071-48df-87de-33a85118642c")
	size := config.SizeMessage_UNKNOWN
	expectedDomainManagerDetails.Config.Size = &size
	expectedDomainManagerNodesInfo := utils.InitialiseDomainManagerNodeInfo()
	domainManagerDetails := utils.InitialiseDomainManagerEntity()
	domainManagerNodesInfo := utils.InitialiseDomainManagerNodeInfo()
	err := populateVmAndZeusDetailsInDomainManager(domainManagerDetails, domainManagerNodesInfo)
	assert.Nil(t, err)
	assert.Equal(t, expectedDomainManagerDetails, domainManagerDetails)
	assert.Equal(t, expectedDomainManagerNodesInfo, domainManagerNodesInfo)
}

func TestPopulateVmAndZeusDetailsInDomainManagerProcessGroupsCallResponseFailure(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	test.SetMockUhuraClient(TestClusterUuid)
	m := test.MockSingletons(t)
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
	m.ZkClient.EXPECT().GetChildren(common_consts.TRUST_SETUP_NODE_PATH, true).Return(trustList, nil).Times(1)
	m.ZkClient.EXPECT().GetZkNode(common_consts.TRUST_SETUP_NODE_PATH+"/"+clusterUuid, true).Return(marshalGetZkResp, nil).Times(1)
	m.ZkClient.EXPECT().GetZkNode(common_consts.TRUST_SETUP_NODE_PATH+"/"+clusterUuid, true).Return(marshalGetZkResp, nil).Times(1)
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(TestZeusConfigBytes, nil).Times(1)
	statusCode := 200
	groupsResponse := common_models.GroupsGetEntitiesResponse{}
	marshalGroupsResp, _ := json.Marshal(groupsResponse)
	m.RemoteRestClient.EXPECT().CallApi(address, consts.EnvoyPort, consts.GroupsUrl, http.MethodPost, gomock.Any(), gomock.Any(), gomock.Any(), nil, nil, nil, true).Return(marshalGroupsResp, &statusCode, nil).Times(2)
	m.RemoteUhuraClient.On("VmGet", mock.AnythingOfType("*uhura.VmGetArg")).Return(&uhura.VmGetRet{}, nil)
	expectedDomainManagerDetails := utils.InitialiseDomainManagerEntity()
	expectedDomainManagerDetails.Config.Name = proto.String("Unnamed")
	expectedDomainManagerDetails.ShouldEnableHighAvailability = proto.Bool(false)
	expectedDomainManagerDetails.NodeExtIds = &domainManagerConfig.StringArrayWrapper{}
	expectedDomainManagerDetails.Config.ResourceConfig = &domainManagerConfig.DomainManagerResourceConfig{
		ContainerExtIds:   &domainManagerConfig.StringArrayWrapper{},
		MemorySizeBytes:   proto.Int64(0),
		NumVcpus:          proto.Int32(0),
		DataDiskSizeBytes: proto.Int64(0),
	}
	expectedDomainManagerDetails.HostingClusterExtId = &clusterUuid
	expectedDomainManagerDetails.Base.ExtId = proto.String("5d5b88b0-3071-48df-87de-33a85118642c")
	size := config.SizeMessage_UNKNOWN
	expectedDomainManagerDetails.Config.Size = &size
	expectedDomainManagerNodesInfo := utils.InitialiseDomainManagerNodeInfo()
	domainManagerDetails := utils.InitialiseDomainManagerEntity()
	domainManagerNodesInfo := utils.InitialiseDomainManagerNodeInfo()
	err := populateVmAndZeusDetailsInDomainManager(domainManagerDetails, domainManagerNodesInfo)
	assert.Nil(t, err)
	assert.Equal(t, expectedDomainManagerDetails, domainManagerDetails)
	assert.Equal(t, expectedDomainManagerNodesInfo, domainManagerNodesInfo)
}

func TestSaveDomainManagerEntityInIdfSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	test.SetMockUhuraClient(TestClusterUuid)
	m := test.MockSingletons(t)

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
	m.IdfSession.EXPECT().SendMsgWithTimeout(consts.IdfUpdateOperation, gomock.Any(),
		gomock.Any(), consts.IdfBackoffForRetries, gomock.Any()).Return(nil)

	SaveDomainManagerEntityInIdf()
}

func TestSaveDomainManagerEntityInIdfFailedToGetCmspStatus(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	test.SetMockUhuraClient(TestClusterUuid)
	m := test.MockSingletons(t)

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
	m.GenesisJsonRpcClient.EXPECT().GetCmspStatus(gomock.Any()).Return("", ErrTest).Times(1)
	m.ZkClient.EXPECT().GetZkNode(consts.PCEnvZkNodePath, true).Return(TestEnvironmentInfoBytes, nil).Times(1)
	m.IdfSession.EXPECT().SendMsgWithTimeout(consts.IdfUpdateOperation, gomock.Any(),
		gomock.Any(), consts.IdfBackoffForRetries, gomock.Any()).Return(nil)

	SaveDomainManagerEntityInIdf()
}

func TestSaveDomainManagerEntityInIdfFailureToPopulateEnvInfo(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)

	m.IdfSession.EXPECT().SendMsgWithTimeout(consts.IdfGetOperation,
		gomock.Any(), gomock.Any(), consts.IdfBackoffForRetries, gomock.Any()).
		Return(nil)
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(TestZeusConfigBytes, nil).Times(1)
	m.ZkClient.EXPECT().GetChildren(common_consts.TRUST_SETUP_NODE_PATH, true).Return(nil, ErrTest).Times(1)
	m.GenesisJsonRpcClient.EXPECT().GetCmspStatus(gomock.Any()).Return(TestGetCmspResponseValueString, nil).Times(1)
	m.ZkClient.EXPECT().GetZkNode(consts.PCEnvZkNodePath, true).Return(TestEnvironmentInfoBytes, nil).Times(1)
	m.IdfSession.EXPECT().SendMsgWithTimeout(consts.IdfUpdateOperation,
		gomock.Any(), gomock.Any(), consts.IdfBackoffForRetries, gomock.Any()).
		Return(nil)

	SaveDomainManagerEntityInIdf()
}

func TestGetSizeFromZeusConfigKTiny(t *testing.T) {
	zeusConfig := &zeus_config.ConfigurationProto{
		PcClusterInfo: &zeus_config.ConfigurationProto_PCClusterInfo{
			Size: zeus_config.ConfigurationProto_PCClusterInfo_kTiny.Enum(),
		},
	}
	size := getSizeFromZeusConfig(zeusConfig)
	assert.Equal(t, config.SizeMessage_STARTER, size)
}

func TestGetSizeFromZeusConfigKSmall(t *testing.T) {
	zeusConfig := &zeus_config.ConfigurationProto{
		PcClusterInfo: &zeus_config.ConfigurationProto_PCClusterInfo{
			Size: zeus_config.ConfigurationProto_PCClusterInfo_kSmall.Enum(),
		},
	}
	size := getSizeFromZeusConfig(zeusConfig)
	assert.Equal(t, config.SizeMessage_SMALL, size)
}

func TestGetSizeFromZeusConfigKLarge(t *testing.T) {
	zeusConfig := &zeus_config.ConfigurationProto{
		PcClusterInfo: &zeus_config.ConfigurationProto_PCClusterInfo{
			Size: zeus_config.ConfigurationProto_PCClusterInfo_kLarge.Enum(),
		},
	}
	size := getSizeFromZeusConfig(zeusConfig)
	assert.Equal(t, config.SizeMessage_LARGE, size)
}

func TestGetSizeFromZeusConfigKExtraLarge(t *testing.T) {
	zeusConfig := &zeus_config.ConfigurationProto{
		PcClusterInfo: &zeus_config.ConfigurationProto_PCClusterInfo{
			Size: zeus_config.ConfigurationProto_PCClusterInfo_kXLarge.Enum(),
		},
	}
	size := getSizeFromZeusConfig(zeusConfig)
	assert.Equal(t, config.SizeMessage_EXTRALARGE, size)
}

func TestSetEnvironmentDetailsInDomainManagerCase1(t *testing.T) {
	environmentDetails := &aplos.PCEnvironmentConfig_PCEnvironmentInfo{
		EnvironmentType:   aplos.PCEnvironmentConfig_PCEnvironmentInfo_ONPREM.Enum(),
		CloudProviderInfo: aplos.PCEnvironmentConfig_PCEnvironmentInfo_AZURE.Enum(),
		InstanceType:      aplos.PCEnvironmentConfig_PCEnvironmentInfo_NATIVE_PROVISIONED.Enum(),
	}
	domainManagerDetails := utils.InitialiseDomainManagerEntity()
	setEnvironmentDetailsInDomainManager(environmentDetails, domainManagerDetails)

	environmentInfo := domainManagerDetails.GetConfig().GetBootstrapConfig().GetEnvironmentInfo()
	assert.Equal(t, config.EnvironmentTypeMessage_ONPREM,
		environmentInfo.GetType())
	assert.Equal(t, config.ProviderTypeMessage_AZURE,
		environmentInfo.GetProviderType())
	assert.Equal(t, config.ProvisioningTypeMessage_NATIVE,
		environmentInfo.GetProvisioningType())
}

func TestSetEnvironmentDetailsInDomainManagerProviderTypeAWS(t *testing.T) {
	environmentDetails := &aplos.PCEnvironmentConfig_PCEnvironmentInfo{
		CloudProviderInfo: aplos.PCEnvironmentConfig_PCEnvironmentInfo_AWS.Enum(),
	}
	domainManagerDetails := utils.InitialiseDomainManagerEntity()
	setEnvironmentDetailsInDomainManager(environmentDetails, domainManagerDetails)

	environmentInfo := domainManagerDetails.GetConfig().GetBootstrapConfig().GetEnvironmentInfo()
	assert.Equal(t, config.ProviderTypeMessage_AWS,
		environmentInfo.GetProviderType())
}

func TestSetEnvironmentDetailsInDomainManagerProviderTypeGCP(t *testing.T) {
	environmentDetails := &aplos.PCEnvironmentConfig_PCEnvironmentInfo{
		CloudProviderInfo: aplos.PCEnvironmentConfig_PCEnvironmentInfo_GCP.Enum(),
	}
	domainManagerDetails := utils.InitialiseDomainManagerEntity()
	setEnvironmentDetailsInDomainManager(environmentDetails, domainManagerDetails)

	environmentInfo := domainManagerDetails.GetConfig().GetBootstrapConfig().GetEnvironmentInfo()
	assert.Equal(t, config.ProviderTypeMessage_GCP,
		environmentInfo.GetProviderType())
}

func TestSetEnvironmentDetailsInDomainManagerProviderTypeVSPHERE(t *testing.T) {
	environmentDetails := &aplos.PCEnvironmentConfig_PCEnvironmentInfo{
		CloudProviderInfo: aplos.PCEnvironmentConfig_PCEnvironmentInfo_VSPHERE.Enum(),
	}
	domainManagerDetails := utils.InitialiseDomainManagerEntity()
	setEnvironmentDetailsInDomainManager(environmentDetails, domainManagerDetails)

	environmentInfo := domainManagerDetails.GetConfig().GetBootstrapConfig().GetEnvironmentInfo()
	assert.Equal(t, config.ProviderTypeMessage_VSPHERE,
		environmentInfo.GetProviderType())
}

func TestPopulateVmDetailsUsingUhuraRpcEmptyUhuraVmUuid(t *testing.T) {
	testZeusConfigDTO := &zeus_config.ConfigurationProto{
		LogicalTimestamp: proto.Int64(0),
		NodeList: []*zeus_config.ConfigurationProto_Node{
			{
				ServiceVmId: proto.Int64(123),
			},
		},
	}
	err := populateVmDetailsUsingUhuraRpc(testZeusConfigDTO, nil, nil)
	assert.Error(t, err, consts.ErrorInvalidZeusConfig.Error())
}

func TestPopulateVmDetailsUsingUhuraRpcInvalidUhuraVmUuid(t *testing.T) {
	testZeusConfigDTO := &zeus_config.ConfigurationProto{
		LogicalTimestamp: proto.Int64(0),
		NodeList: []*zeus_config.ConfigurationProto_Node{
			{
				ServiceVmId:  proto.Int64(123),
				UhuraUvmUuid: proto.String("abc"),
			},
		},
	}
	err := populateVmDetailsUsingUhuraRpc(testZeusConfigDTO, nil, nil)
	assert.Error(t, err, consts.ErrorInvalidZeusConfig.Error())
}

func TestVmProcessGroupsResponse_Success(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	entityId := "18142867-55f2-4f5e-8b8e-3d1e4b0ebc"
	containerUuid := "container_uuid"
	response := common_models.GroupsGetEntitiesResponse{
		GroupResults: &[]*common_models.GroupsGroupResult{
			{
				EntityResults: &[]*common_models.GroupsEntity{
					{
						EntityId: proto.String(entityId),
						Data: &[]*common_models.GroupsFieldData{
							{
								Name: proto.String(consts.VmAttributeContainerUuid),
								Values: &[]*common_models.GroupsTimevaluePair{
									{
										Values: &[]string{containerUuid},
									},
								},
							},
							{
								Name:   proto.String("attribute"),
								Values: &[]*common_models.GroupsTimevaluePair{},
							},
						},
					},
					{
						EntityId: proto.String("entity_id2"),
					},
				},
			},
		},
	}

	expectedDomainManagerNodeInfo := utils.InitialiseDomainManagerNodeInfo()
	vmDetails := &models.VmDetails{
		VmSpecs: &models.VmSpecs{
			ContainerExtId: proto.String(containerUuid),
			Uuid:           proto.String(entityId),
		},
	}
	expectedDomainManagerNodeInfo.VmDetailsList =
		append(expectedDomainManagerNodeInfo.GetVmDetailsList(), vmDetails)

	domainManagerDetails := utils.InitialiseDomainManagerEntity()
	domainManagerNodeInfo := utils.InitialiseDomainManagerNodeInfo()
	uhuraVmUuids := []string{entityId}
	err := processVmGroupsResponse(&response, uhuraVmUuids, domainManagerDetails,
		domainManagerNodeInfo)
	assert.Nil(t, err)
	assert.Equal(t, expectedDomainManagerNodeInfo, domainManagerNodeInfo)
}

func TestProcessVmGroupsResponse_NilResponse(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	response := common_models.GroupsGetEntitiesResponse{
		GroupResults: &[]*common_models.GroupsGroupResult{
			{
				EntityResults: nil,
			},
		},
	}

	expectedDomainManagerDetails := utils.InitialiseDomainManagerEntity()
	expectedDomainManagerNodeInfo := utils.InitialiseDomainManagerNodeInfo()

	domainManagerDetails := utils.InitialiseDomainManagerEntity()
	domainManagerNodeInfo := utils.InitialiseDomainManagerNodeInfo()
	uhuraVmUuids := []string{"18142867-55f2-4f5e-8b8e-3d1e4b0ebc"}
	err := processVmGroupsResponse(&response, uhuraVmUuids, domainManagerDetails, domainManagerNodeInfo)
	assert.EqualError(t, err, consts.ErrorInvalidGroupsResponse.Error())
	assert.Equal(t, expectedDomainManagerDetails, domainManagerDetails)
	assert.Equal(t, expectedDomainManagerNodeInfo, domainManagerNodeInfo)
}

func TestProcessUhuraVmGetResponse_NilResponse(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	err := processUhuraVmGetResponse(nil, nil, nil)
	assert.Error(t, err, consts.ErrorInvalidUhuraResponse.Error())

	uhuraResponse := &uhura.VmGetRet{
		VmInfoList: nil,
	}

	err = processUhuraVmGetResponse(uhuraResponse, nil, nil)
	assert.Error(t, err, consts.ErrorInvalidUhuraResponse.Error())

	uhuraResponse = &uhura.VmGetRet{
		VmInfoList: []*uhura.VmInfo{
			{
				Config: nil,
			},
		},
	}

	err = processUhuraVmGetResponse(uhuraResponse, nil, nil)
	assert.Error(t, err, consts.ErrorInvalidUhuraResponse.Error())

	uhuraResponse = &uhura.VmGetRet{
		VmInfoList: []*uhura.VmInfo{
			{
				Config: &uhura.VmConfig{
					NicList: []*uhura.VmNicConfig{},
				},
			},
		},
	}

	err = processUhuraVmGetResponse(uhuraResponse, nil, nil)
	assert.Error(t, err, consts.ErrorInvalidUhuraResponse.Error())

	uhuraResponse = &uhura.VmGetRet{
		VmInfoList: []*uhura.VmInfo{
			{
				Config: &uhura.VmConfig{
					NicList: []*uhura.VmNicConfig{
						{
							MacAddr: []byte("abc"),
						},
					},
				},
			},
		},
	}
	err = processUhuraVmGetResponse(uhuraResponse, nil, nil)
	assert.Error(t, err, consts.ErrorInvalidUhuraResponse.Error())
}


func TestProcessDiskGroupsResponse_NilResponse(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	err := processDiskGroupsResponse(nil, TestUhuruVmUuid, nil, nil)
	assert.Error(t, err, consts.ErrorInvalidGroupsResponse.Error())

	groupsResponse := &common_models.GroupsGetEntitiesResponse{
		GroupResults: &[]*common_models.GroupsGroupResult{},
	}
	err = processDiskGroupsResponse(groupsResponse, TestUhuruVmUuid, nil, nil)
	assert.Error(t, err, consts.ErrorInvalidGroupsResponse.Error())

	groupsResponse = &common_models.GroupsGetEntitiesResponse{
		GroupResults: &[]*common_models.GroupsGroupResult{
			{
				EntityResults: &[]*common_models.GroupsEntity{},
			},
		},
	}
	err = processDiskGroupsResponse(groupsResponse, TestUhuruVmUuid, nil, nil)
	assert.Error(t, err, consts.ErrorInvalidGroupsResponse.Error())

	groupsResponse = &common_models.GroupsGetEntitiesResponse{
		GroupResults: &[]*common_models.GroupsGroupResult{
			{
				EntityResults: &[]*common_models.GroupsEntity{
					{
						EntityId: proto.String(TestUhuruVmUuid),
						Data: &[]*common_models.GroupsFieldData{},
					},
				},
			},
		},
	}
	err = processDiskGroupsResponse(groupsResponse, TestUhuruVmUuid, nil, nil)
	assert.Error(t, err, consts.ErrorInvalidGroupsResponse.Error())
}
