package utils

import (
	"encoding/json"
	"errors"
	common_config "ntnx-api-guru-pc/generated-code/target/protobuf/common/v1/config"
	domainManagerConfig "ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/config"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/external"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/test"
	"testing"

	"github.com/golang/mock/gomock"
	"github.com/golang/protobuf/proto"
	aplos "github.com/nutanix-core/go-cache/aplos/sl_bufs"
	"github.com/nutanix-core/go-cache/insights/insights_interface"
	zeusConfig "github.com/nutanix-core/go-cache/zeus/config"
	mockZk "github.com/nutanix-core/ntnx-api-guru/services/zk/mocks"
	"github.com/stretchr/testify/assert"
)

var (
	TestCmspPcDomainName     = "pcDomain"
	TestCmspSubnetMask       = "subnetMask"
	TestCmspDefaultGateway   = "defaultGateway"
	TestCmspInfraIpBlock     = []string{"192.168.5.2 192.168.5.64"}
	TestCmspType             = "type"
	TestGetCmspResponseValue = map[string]interface{}{
		".return": map[string]interface{}{
			"config": map[string]interface{}{
				"cmsp_config": map[string]interface{}{
					"pcDomain": TestCmspPcDomainName,
					"infraNetwork": map[string]interface{}{
						"netmaskIpv4Address": TestCmspSubnetMask,
						"gatewayIpv4Address": TestCmspDefaultGateway,
					},
					"infraIpBlock": TestCmspInfraIpBlock,
					"type":         TestCmspType,
				},
			},
		},
	}
	TestUhuruVmUuid            = "uhura_vm_uuid"
	TestUhuruVmUuid2           = "uhura_vm_uuid2"
	TestUhuruVmUuid3           = "uhura_vm_uuid3"
	TestPcExternalIp           = "external ip"
	TestThreeNodeZeusConfigDTO = &zeusConfig.ConfigurationProto{
		LogicalTimestamp: proto.Int64(0),
		SnmpInfo:         nil,
		NodeList: []*zeusConfig.ConfigurationProto_Node{
			{
				ServiceVmId:         proto.Int64(123),
				ServiceVmExternalIp: proto.String(TestPcExternalIp),
				UhuraUvmUuid:        proto.String(TestUhuruVmUuid),
			},
			{
				ServiceVmId:         proto.Int64(123),
				ServiceVmExternalIp: proto.String(TestPcExternalIp),
				UhuraUvmUuid:        proto.String(TestUhuruVmUuid2),
			},
			{
				ServiceVmId:         proto.Int64(123),
				ServiceVmExternalIp: proto.String(TestPcExternalIp),
				UhuraUvmUuid:        proto.String(TestUhuruVmUuid3),
			},
		},
	}
	TestGetCmspResponseValueByte, _ = json.Marshal(TestGetCmspResponseValue)
	TestGetCmspResponseValueString  = string(TestGetCmspResponseValueByte)
	TestGetCmspResponse             = map[string]interface{}{
		"value": TestGetCmspResponseValueString,
	}
	TestGetCmspResponseBytes, _ = json.Marshal(TestGetCmspResponse)
	ErrTest                     = errors.New("error")
	TestNumVcpus                = int64(6)
	TestDataDiskSizeBytes       = int64(100)
	TestMemorySizeBytes         = int64(10)
	TestContainerUuid           = "container_uuid"
	TestVmDetails               = &models.DomainManagerNodesInfo{
		VmDetailsList: []*models.VmDetails{
			{
				VmSpecs: &models.VmSpecs{
					NumVcpus:          proto.Int64(TestNumVcpus),
					ContainerExtId:    proto.String(TestContainerUuid),
					MemorySizeBytes:   proto.Int64(TestMemorySizeBytes),
					DataDiskSizeBytes: proto.Int64(TestDataDiskSizeBytes),
				},
			},
		},
	}
	TestVmDetailsBytes, _ = proto.Marshal(TestVmDetails)
)

func TestIsNativeCloudPcGetZkNodeError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	m.ZkClient.EXPECT().GetZkNode(consts.PCEnvZkNodePath, true).
		Return(nil, ErrTest).Times(1)

	res, err := IsNativeCloudPc()
	assert.Equal(t, false, res)
	assert.ErrorIs(t, err, ErrTest)
}

func TestIsNativeCloudPcReturnTrue(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	pcEnvConfig := &aplos.PCEnvironmentConfig{
		PcEnvironmentInfo: &aplos.PCEnvironmentConfig_PCEnvironmentInfo{
			EnvironmentType: aplos.PCEnvironmentConfig_PCEnvironmentInfo_NTNX_CLOUD.Enum(),
			InstanceType:    aplos.PCEnvironmentConfig_PCEnvironmentInfo_NATIVE_PROVISIONED.Enum(),
		},
	}
	var marshalData []byte
	marshalData, _ = pcEnvConfig.XXX_Marshal(marshalData, true)

	m.ZkClient.EXPECT().GetZkNode(consts.PCEnvZkNodePath, true).
		Return(marshalData, nil).Times(1)

	res, err := IsNativeCloudPc()
	assert.Nil(t, err)
	assert.True(t, res)
}

func TestIsNativeCloudPcReturnFalse(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	pcEnvConfig := &aplos.PCEnvironmentConfig{
		PcEnvironmentInfo: &aplos.PCEnvironmentConfig_PCEnvironmentInfo{
			EnvironmentType: aplos.PCEnvironmentConfig_PCEnvironmentInfo_NTNX_CLOUD.Enum(),
			InstanceType:    aplos.PCEnvironmentConfig_PCEnvironmentInfo_NTNX_PROVISIONED.Enum().Enum(),
		},
	}
	var marshalData []byte
	marshalData, _ = pcEnvConfig.XXX_Marshal(marshalData, true)
	m.ZkClient.EXPECT().GetZkNode(consts.PCEnvZkNodePath, true).
		Return(marshalData, nil).Times(1)

	res, err := IsNativeCloudPc()
	assert.Nil(t, err)
	assert.False(t, res)
}

func TestIsNativeCloudPcMarshalFailure(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	m.ZkClient.EXPECT().GetZkNode(consts.PCEnvZkNodePath, true).
		Return([]byte{1, 2, 3}, nil).Times(1)

	res, err := IsNativeCloudPc()
	assert.False(t, res)
	assert.NotNil(t, err)
}

func TestIsXiPcGetZkNodeError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	m.ZkClient.EXPECT().GetZkNode(consts.PCEnvZkNodePath, true).
		Return(nil, ErrTest).Times(1)

	res, err := IsXiPc()
	assert.Equal(t, false, res)
	assert.ErrorIs(t, err, ErrTest)
}

func TestIsXiPcCloudPcReturnTrue(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	pcEnvConfig := &aplos.PCEnvironmentConfig{
		PcEnvironmentInfo: &aplos.PCEnvironmentConfig_PCEnvironmentInfo{
			EnvironmentType:   aplos.PCEnvironmentConfig_PCEnvironmentInfo_NTNX_CLOUD.Enum(),
			CloudProviderInfo: aplos.PCEnvironmentConfig_PCEnvironmentInfo_NTNX.Enum(),
			InstanceType:      aplos.PCEnvironmentConfig_PCEnvironmentInfo_NTNX_PROVISIONED.Enum(),
		},
	}
	var marshalData []byte
	marshalData, _ = pcEnvConfig.XXX_Marshal(marshalData, true)
	m.ZkClient.EXPECT().GetZkNode(consts.PCEnvZkNodePath, true).
		Return(marshalData, nil).Times(1)

	res, err := IsXiPc()

	assert.Nil(t, err)
	assert.True(t, res)
}

func TestIsXiPcCloudPcReturnFalse(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	pcEnvConfig := &aplos.PCEnvironmentConfig{
		PcEnvironmentInfo: &aplos.PCEnvironmentConfig_PCEnvironmentInfo{
			EnvironmentType:   aplos.PCEnvironmentConfig_PCEnvironmentInfo_NTNX_CLOUD.Enum(),
			CloudProviderInfo: aplos.PCEnvironmentConfig_PCEnvironmentInfo_AWS.Enum(),
			InstanceType:      aplos.PCEnvironmentConfig_PCEnvironmentInfo_NTNX_PROVISIONED.Enum(),
		},
	}
	var marshalData []byte
	marshalData, _ = pcEnvConfig.XXX_Marshal(marshalData, true)
	m.ZkClient.EXPECT().GetZkNode(consts.PCEnvZkNodePath, true).
		Return(marshalData, nil).Times(1)
	res, err := IsXiPc()

	assert.Nil(t, err)
	assert.False(t, res)
}

func TestIsXiPcMarshalFailure(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	m.ZkClient.EXPECT().GetZkNode(consts.PCEnvZkNodePath, true).
		Return([]byte{1, 2, 3}, nil).Times(1)
	res, err := IsXiPc()
	assert.False(t, res)
	assert.NotNil(t, err)
}

func TestGetRegisteredPeListSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockZkSession := mockZk.NewMockZookeeperIfc(ctrl)
	mockZkClient := mockZk.NewMockZkClientIfc(ctrl)
	external.SetSingletonServices(nil, nil, nil, mockZkClient,
		mockZkSession, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil)
	registerPeList := []string{"a", "b", "c"}

	mockZkClient.EXPECT().GetZkSession().Return(mockZkSession).AnyTimes()
	mockZkSession.EXPECT().Children(consts.AOSClusterCES, true).Return(registerPeList, nil, nil)
	list, err := GetRegisteredPeList()

	assert.Nil(t, err)
	assert.Equal(t, list, registerPeList)
}

func TestGetRegisteredPeListFailure(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockZkSession := mockZk.NewMockZookeeperIfc(ctrl)
	mockZkClient := mockZk.NewMockZkClientIfc(ctrl)
	external.SetSingletonServices(nil, nil, nil, mockZkClient,
		mockZkSession, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil)

	mockZkClient.EXPECT().GetZkSession().Return(mockZkSession).AnyTimes()
	mockZkSession.EXPECT().Children(consts.AOSClusterCES, true).Return(nil, nil, ErrTest)
	_, err := GetRegisteredPeList()

	assert.ErrorIs(t, err, ErrTest)
}

func TestGetRegisteredPcListSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockZkSession := mockZk.NewMockZookeeperIfc(ctrl)
	mockZkClient := mockZk.NewMockZkClientIfc(ctrl)
	external.SetSingletonServices(nil, nil, nil, mockZkClient,
		mockZkSession, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil)
	pairedPcList := []string{"a", "b", "c"}

	mockZkClient.EXPECT().GetZkSession().Return(mockZkSession).AnyTimes()
	mockZkSession.EXPECT().Children(consts.DomainManagerCES, true).Return(pairedPcList, nil, nil)
	list, err := GetRegisteredPcList()

	assert.Nil(t, err)
	assert.Equal(t, list, pairedPcList)
}

func TestGetRegisteredPcListFailure(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockZkSession := mockZk.NewMockZookeeperIfc(ctrl)
	mockZkClient := mockZk.NewMockZkClientIfc(ctrl)
	external.SetSingletonServices(nil, nil, nil, mockZkClient,
		mockZkSession, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil)

	mockZkClient.EXPECT().GetZkSession().Return(mockZkSession).AnyTimes()
	mockZkSession.EXPECT().Children(consts.DomainManagerCES, true).Return(nil, nil, ErrTest)
	_, err := GetRegisteredPcList()

	assert.ErrorIs(t, err, ErrTest)
}

func TestGetRegisteredPcListError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)

	m.ZkSession.On("Children", consts.DomainManagerCES, true).Return(nil, nil, errors.New("error"))
	res, err := GetRegisteredPcList()

	assert.Nil(t, res)
	assert.EqualError(t, err, ErrTest.Error())
}

func TestGetRegisteredPc(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	m.ZkSession.On("Children", consts.DomainManagerCES, true).Return(nil, nil, nil)
	res, err := GetRegisteredPcList()
	assert.Nil(t, res)
	assert.Nil(t, err)
}

func TestIsEnvironmentDetailsPresent(t *testing.T) {
	envType := consts.DomainManagerEnvironmentType
	providerType := consts.DomainManagerProviderType
	instanceType := consts.DomainManagerInstanceType

	domainManagerEntity := &insights_interface.Entity{
		AttributeDataMap: []*insights_interface.NameTimeValuePair{
			{
				Name: &envType,
			},
			{
				Name: &providerType,
			},
			{
				Name: &instanceType,
			},
		},
	}
	isPresent := IsEnvironmentDetailsPresent(domainManagerEntity)
	assert.True(t, isPresent)
}

func TestInitialiseDomainManagerEntity(t *testing.T) {
	domainManager := InitialiseDomainManagerEntity()
	assert.NotNil(t, domainManager)
}

func TestInitialiseDomainManagerNodeInfo(t *testing.T) {
	domainManagerNodeInfo := InitialiseDomainManagerNodeInfo()
	assert.NotNil(t, domainManagerNodeInfo)
}

func TestBuildDomainManagerDetailsFromIdfEntity(t *testing.T) {
	diskCap := 1118240620544
	entity := &insights_interface.Entity{
		AttributeDataMap: []*insights_interface.NameTimeValuePair{
			{
				Name: proto.String(consts.DomainManagerEnvironmentType),
				Value: &insights_interface.DataValue{
					ValueType: &insights_interface.DataValue_StrValue{
						StrValue: domainManagerConfig.EnvironmentTypeMessage_NTNX_CLOUD.Enum().String(),
					},
				},
			},
			{
				Name: proto.String(consts.DomainManagerProviderType),
				Value: &insights_interface.DataValue{
					ValueType: &insights_interface.DataValue_StrValue{
						StrValue: domainManagerConfig.ProviderTypeMessage_AZURE.Enum().String(),
					},
				},
			},
			{
				Name: proto.String(consts.DomainManagerInstanceType),
				Value: &insights_interface.DataValue{
					ValueType: &insights_interface.DataValue_StrValue{
						StrValue: domainManagerConfig.ProvisioningTypeMessage_NTNX.String(),
					},
				},
			},
			{
				Name: proto.String(consts.DomainManagerDiskCapacityBytes),
				Value: &insights_interface.DataValue{
					ValueType: &insights_interface.DataValue_Int64Value{
						Int64Value: int64(diskCap),
					},
				},
			},
			{
				Name: proto.String(consts.DomainManagerExtID),
				Value: &insights_interface.DataValue{
					ValueType: &insights_interface.DataValue_StrValue{
						StrValue: "929a8983-463a-47ad-ac23-74541bd3459b",
					},
				},
			},
			{
				Name: proto.String(consts.DomainManagerFlavour),
				Value: &insights_interface.DataValue{
					ValueType: &insights_interface.DataValue_StrValue{
						StrValue: "SMALL",
					},
				},
			},
			{
				Name: proto.String(consts.DomainManagerHostingClusterExtID),
				Value: &insights_interface.DataValue{
					ValueType: &insights_interface.DataValue_StrValue{
						StrValue: "0006187c-4052-ec4a-0000-000000005676",
					},
				},
			},
			{
				Name: proto.String(consts.DomainManagerInstanceType),
				Value: &insights_interface.DataValue{
					ValueType: &insights_interface.DataValue_StrValue{
						StrValue: "NTNX_PROVISIONED",
					},
				},
			},
			{
				Name: proto.String(consts.DomainManagerIsPulseEnabled),
				Value: &insights_interface.DataValue{
					ValueType: &insights_interface.DataValue_BoolValue{
						BoolValue: true,
					},
				},
			},
			{
				Name: proto.String(consts.DomainManagerMemorySizeBytes),
				Value: &insights_interface.DataValue{
					ValueType: &insights_interface.DataValue_Int64Value{
						Int64Value: 30064771072,
					},
				},
			},
			{
				Name: proto.String(consts.DomainManagerName),
				Value: &insights_interface.DataValue{
					ValueType: &insights_interface.DataValue_StrValue{
						StrValue: "PC_10.36.240.82",
					},
				},
			},
			{
				Name: proto.String(consts.DomainManagerNodesCount),
				Value: &insights_interface.DataValue{
					ValueType: &insights_interface.DataValue_Int64Value{
						Int64Value: 1,
					},
				},
			},
			{
				Name: proto.String(consts.DomainManagerNumVcpus),
				Value: &insights_interface.DataValue{
					ValueType: &insights_interface.DataValue_Int64Value{
						Int64Value: 6,
					},
				},
			},
			{
				Name: proto.String(consts.DomainManagerVersion),
				Value: &insights_interface.DataValue{
					ValueType: &insights_interface.DataValue_StrValue{
						StrValue: "el8.5-opt-master-2224c451b6b166893c827f4d1ee762768bc04569",
					},
				},
			},
			{
				Name: proto.String(consts.DomainManagerNodesInfo),
				Value: &insights_interface.DataValue{
					ValueType: &insights_interface.DataValue_BytesValue{
						BytesValue: TestVmDetailsBytes,
					},
				},
			},
		},
	}
	domainManagerDetails := InitialiseDomainManagerEntity()
	domainManagerNodeInfo := InitialiseDomainManagerNodeInfo()
	BuildDomainManagerDetailsFromIdfEntity(entity,
		domainManagerDetails, domainManagerNodeInfo)
}

func TestFetchListOfIpRangeFromStringList(t *testing.T) {
	ipRangeList := []string{"192.168.5.2 192.168.5.64"}
	expectedIpBlockList := []*common_config.IpRange{
		{
			Begin: &common_config.IPAddress{
				Ipv4: &common_config.IPv4Address{
					Value: proto.String("192.168.5.2"),
				},
			},
			End: &common_config.IPAddress{
				Ipv4: &common_config.IPv4Address{
					Value: proto.String("192.168.5.64"),
				},
			},
		},
	}

	ipBlockList := FetchListOfIpRangeFromStringList(ipRangeList)
	assert.Equal(t, expectedIpBlockList, ipBlockList)
}

func TestGetMajorRelaseVersion(t *testing.T) {
	s, err := GetPCMajorReleaseVersion("pc.2023.4.0.1")
	assert.Equal(t, "2023.4", s)
	assert.Nil(t, err)
}

func TestGetCmspStatusSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	m.GenesisJsonRpcClient.EXPECT().GetCmspStatus(gomock.Any()).
		Return(TestGetCmspResponseValueString, nil).Times(1)
	res, err := GetCmspStatus()
	cmspConfig := res.Return.Config.CmspConfig
	assert.Nil(t, err)
	assert.Equal(t, TestCmspPcDomainName, cmspConfig.PcDomain)
	assert.Equal(t, TestCmspSubnetMask, cmspConfig.InfraNetwork.NetmaskIpv4Address)
	assert.Equal(t, TestCmspDefaultGateway, cmspConfig.InfraNetwork.GatewayIpv4Address)
	assert.Equal(t, TestCmspInfraIpBlock, cmspConfig.InfraIpBlock)
	assert.Equal(t, TestCmspType, cmspConfig.Type)
}

func TestGetCmspStatusFailureToGetCmspStatus(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	m.GenesisJsonRpcClient.EXPECT().GetCmspStatus(gomock.Any()).
		Return("", ErrTest).Times(1)
	res, err := GetCmspStatus()
	assert.Nil(t, res)
	assert.EqualError(t, err, "failed to get cmsp status : error")
}

func TestGetCmspStatusFailureBadResponse(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	m.GenesisJsonRpcClient.EXPECT().GetCmspStatus(gomock.Any()).
		Return("nil", nil).Times(1)
	res, err := GetCmspStatus()
	assert.Nil(t, res)
	assert.NotNil(t, err)
}
