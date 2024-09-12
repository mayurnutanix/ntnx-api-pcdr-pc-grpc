package get_domain_manager

import (
	"errors"
	commonConfig "ntnx-api-guru-pc/generated-code/target/protobuf/common/v1/config"
	domainManagerConfig "ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/config"
	ntnxApiGuruError "ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/error"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/test"
	"ntnx-api-guru-pc/guru-pc-api-service/utils"
	"testing"

	"github.com/golang/mock/gomock"
	"github.com/golang/protobuf/proto"
	"github.com/nutanix-core/go-cache/insights/insights_interface"
	"github.com/nutanix-core/go-cache/util-go/misc"
	zeusConfig "github.com/nutanix-core/go-cache/zeus/config"
	commonConsts "github.com/nutanix-core/ntnx-api-guru/services/constants"
	"github.com/stretchr/testify/assert"
)

var (
	TestUhuruVmUuid       = "uhura_vm_uuid"
	TestPeClusterUuid     = "pe_cluster_uuid"
	TestVersion           = "version"
	TestPcExternalIp      = "external ip"
	TestFqdn              = "fqdn"
	TestPcClusterUuid     = "cluster_uuid"
	TestNumVcpus          = int64(6)
	TestDataDiskSizeBytes = int64(100)
	TestMemorySizeBytes   = int64(10)
	TestNetworkExtId      = "networkId"
	TestContainerUuid     = "container_uuid"
	TestDefaultSubnetId   = "172.16.0.0/255.240.0.0"
	TestSubnetMask        = "172.16.0.0/2.3.4.5"
	TestZeusConfigDTO     = &zeusConfig.ConfigurationProto{
		LogicalTimestamp: proto.Int64(0),
		SnmpInfo:         nil,
		NodeList: []*zeusConfig.ConfigurationProto_Node{
			{
				ServiceVmId:         proto.Int64(123),
				ServiceVmExternalIp: proto.String(TestPcExternalIp),
				UhuraUvmUuid:        proto.String(TestUhuruVmUuid),
			},
		},
		Aegis: &zeusConfig.ConfigurationProto_Aegis{
			AutoSupportConfig: &zeusConfig.ConfigurationProto_Aegis_AutoSupportConfig{
				EmailAsups: &zeusConfig.ConfigurationProto_Aegis_TimedBool{
					Value: proto.Bool(true),
				},
			},
		},
		PcClusterInfo: &zeusConfig.ConfigurationProto_PCClusterInfo{
			HostInfo: &zeusConfig.ConfigurationProto_PCClusterInfo_HostInfo{
				PrismElementList: []*zeusConfig.ConfigurationProto_PCClusterInfo_HostInfo_PrismElement{
					{
						ClusterUuid: proto.String(TestPeClusterUuid),
					},
				},
			},
		},
		ReleaseVersion:                  proto.String(TestVersion),
		ClusterExternalIp:               proto.String(TestPcExternalIp),
		ClusterFullyQualifiedDomainName: proto.String(TestFqdn),
		ClusterUuid:                     proto.String(TestPcClusterUuid),
		ExternalSubnet:                  proto.String(TestSubnetMask),
	}
	TestErr                = errors.New("test error")
	TestZeusConfigBytes, _ = proto.Marshal(TestZeusConfigDTO)
	TestVmDetails          = &models.DomainManagerNodesInfo{
		VmDetailsList: []*models.VmDetails{
			{
				VmSpecs: &models.VmSpecs{
					NumVcpus:          proto.Int64(TestNumVcpus),
					Uuid:              proto.String(TestUhuruVmUuid),
					ContainerExtId:    proto.String(TestContainerUuid),
					MemorySizeBytes:   proto.Int64(TestMemorySizeBytes),
					DataDiskSizeBytes: proto.Int64(TestDataDiskSizeBytes),
				},
			},
		},
		NetworkExtId: proto.String(TestNetworkExtId),
	}
	TestVmDetailsBytes, _   = proto.Marshal(TestVmDetails)
	TestDomainManagerUuid   = "domain_manager_uuid"
	TestCmspDomainName      = "domain_name"
	TestCmspDefaultIp       = "default_ip"
	TestCmspSubnetMask      = "subnet_mask"
	TestCmspPlatformBlockIp = "ip1 ip2"
	TestEnvType             = "ONPREM"
	TestPcFlavour           = "SMALL"
	TestIsPulseEnabled      = true
	TestPcName              = "pc_name"
	TestNodeCount           = int64(1)
	TestProviderType        = "NTNX"
	TestInstanceObj         = "NTNX"
	TestIsRegisteredToPe    = true
	TestGetEntitiesRet      = &insights_interface.GetEntitiesRet{
		Entity: []*insights_interface.Entity{
			{
				EntityGuid: &insights_interface.EntityGuid{
					EntityTypeName: proto.String("domain_manager"),
					EntityId:       proto.String(TestDomainManagerUuid),
				},
				CasValue: proto.Uint64(0),
				AttributeDataMap: []*insights_interface.NameTimeValuePair{
					{
						Name: proto.String("cmsp_domain_name"),
						Value: &insights_interface.DataValue{
							ValueType: &insights_interface.DataValue_StrValue{
								StrValue: TestCmspDomainName,
							},
						},
					},
					{
						Name: proto.String("cmsp_network_default_gateway_ip"),
						Value: &insights_interface.DataValue{
							ValueType: &insights_interface.DataValue_StrValue{
								StrValue: TestCmspDefaultIp,
							},
						},
					},
					{
						Name: proto.String("cmsp_network_subnet_mask"),
						Value: &insights_interface.DataValue{
							ValueType: &insights_interface.DataValue_StrValue{
								StrValue: TestCmspSubnetMask,
							},
						},
					},
					{
						Name: proto.String("cmsp_platform_ip_block_list"),
						Value: &insights_interface.DataValue{
							ValueType: &insights_interface.DataValue_StrList_{
								StrList: &insights_interface.DataValue_StrList{
									ValueList: []string{TestCmspPlatformBlockIp},
								},
							},
						},
					},
					{
						Name: proto.String("data_disk_size_bytes"),
						Value: &insights_interface.DataValue{
							ValueType: &insights_interface.DataValue_Int64Value{
								Int64Value: TestDataDiskSizeBytes,
							},
						},
					},
					{
						Name: proto.String("environment_type"),
						Value: &insights_interface.DataValue{
							ValueType: &insights_interface.DataValue_StrValue{
								StrValue: TestEnvType,
							},
						},
					},
					{
						Name: proto.String("ext_id"),
						Value: &insights_interface.DataValue{
							ValueType: &insights_interface.DataValue_StrValue{
								StrValue: TestPcClusterUuid,
							},
						},
					},
					{
						Name: proto.String("flavour"),
						Value: &insights_interface.DataValue{
							ValueType: &insights_interface.DataValue_StrValue{
								StrValue: TestPcFlavour,
							},
						},
					},
					{
						Name: proto.String("hosting_cluster_ext_id"),
						Value: &insights_interface.DataValue{
							ValueType: &insights_interface.DataValue_StrValue{
								StrValue: TestPeClusterUuid,
							},
						},
					},
					{
						Name: proto.String("instance_type"),
						Value: &insights_interface.DataValue{
							ValueType: &insights_interface.DataValue_StrValue{
								StrValue: TestInstanceObj,
							},
						},
					},
					{
						Name: proto.String("is_pulse_enabled"),
						Value: &insights_interface.DataValue{
							ValueType: &insights_interface.DataValue_BoolValue{
								BoolValue: TestIsPulseEnabled,
							},
						},
					},
					{
						Name: proto.String("memory_size_bytes"),
						Value: &insights_interface.DataValue{
							ValueType: &insights_interface.DataValue_Int64Value{
								Int64Value: TestMemorySizeBytes,
							},
						},
					},
					{
						Name: proto.String("name"),
						Value: &insights_interface.DataValue{
							ValueType: &insights_interface.DataValue_StrValue{
								StrValue: TestPcName,
							},
						},
					},
					{
						Name: proto.String("nodes_count"),
						Value: &insights_interface.DataValue{
							ValueType: &insights_interface.DataValue_Int64Value{
								Int64Value: TestNodeCount,
							},
						},
					},
					{
						Name: proto.String("nodes_info"),
						Value: &insights_interface.DataValue{
							ValueType: &insights_interface.DataValue_BytesValue{
								BytesValue: TestVmDetailsBytes,
							},
						},
					},
					{
						Name: proto.String("num_vcpus"),
						Value: &insights_interface.DataValue{
							ValueType: &insights_interface.DataValue_Int64Value{
								Int64Value: TestNumVcpus,
							},
						},
					},
					{
						Name: proto.String("provider_type"),
						Value: &insights_interface.DataValue{
							ValueType: &insights_interface.DataValue_StrValue{
								StrValue: TestProviderType,
							},
						},
					},
					{
						Name: proto.String("instance"),
						Value: &insights_interface.DataValue{
							ValueType: &insights_interface.DataValue_StrValue{
								StrValue: TestProviderType,
							},
						},
					},
					{
						Name: proto.String("version"),
						Value: &insights_interface.DataValue{
							ValueType: &insights_interface.DataValue_StrValue{
								StrValue: TestVersion,
							},
						},
					},
				},
			},
		},
	}
	TestRegisteredPeList = []string{TestPeClusterUuid}
)

func TestListDomainManagersSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)

	m.ZkClient.EXPECT().GetZkNode(commonConsts.ZEUS_CONFIG_NODE_PATH, true).Return(TestZeusConfigBytes, nil)
	m.IdfSession.EXPECT().SendMsgWithTimeout(consts.IdfGetOperation, gomock.Any(),
		gomock.Any(), consts.IdfBackoffForRetries, gomock.Any()).
		Do(func(operation string, arg interface{},
			response *insights_interface.GetEntitiesRet,
			backoff *misc.ExponentialBackoff, _ interface{}) {
			*response = *TestGetEntitiesRet
		}).Return(nil)
	m.ZkClient.EXPECT().GetChildren(consts.AOSClusterCES, false).Return(TestRegisteredPeList, nil)

	expectedDomainManagerDetails := utils.InitialiseDomainManagerEntity()
	expectedDomainManagerDetails.Base.ExtId = &TestPcClusterUuid
	expectedDomainManagerDetails.IsRegisteredWithHostingCluster = &TestIsRegisteredToPe
	expectedDomainManagerDetails.HostingClusterExtId = &TestPeClusterUuid
	expectedConfig := expectedDomainManagerDetails.GetConfig()
	expectedConfig.Base.BuildInfo.Version = &TestVersion
	expectedConfig.Name = &TestPcName
	expectedConfig.Size = domainManagerConfig.SizeMessage_SMALL.Enum()
	expectedConfig.ResourceConfig = &domainManagerConfig.DomainManagerResourceConfig{
		NumVcpus:          proto.Int32(int32(TestNumVcpus)),
		MemorySizeBytes:   &TestMemorySizeBytes,
		DataDiskSizeBytes: &TestDataDiskSizeBytes,
		ContainerExtIds: &domainManagerConfig.StringArrayWrapper{
			Value: []string{TestContainerUuid},
		},
	}
	expectedDomainManagerDetails.Network.ExternalNetworks.Value = []*domainManagerConfig.ExternalNetwork{
		{
			NetworkExtId: proto.String(TestNetworkExtId),
			Base: &domainManagerConfig.BaseNetwork{
				SubnetMask: &commonConfig.IPAddressOrFQDN{
					Ipv4: &commonConfig.IPv4Address{
						Value: proto.String("2.3.4.5"),
					},
				},
				IpRanges: &commonConfig.IpRangeArrayWrapper{
					Value: []*commonConfig.IpRange{
						{
							Begin: &commonConfig.IPAddress{
								Ipv4: &commonConfig.IPv4Address{
									Value: &TestPcExternalIp,
								},
							},
							End: &commonConfig.IPAddress{
								Ipv4: &commonConfig.IPv4Address{
									Value: &TestPcExternalIp,
								},
							},
						},
					},
				},
			},
		},
	}
	expectedDomainManagerDetails.ShouldEnableHighAvailability = proto.Bool(false)
	expectedEnv := expectedConfig.GetBootstrapConfig().GetEnvironmentInfo()
	expectedEnv.Type = domainManagerConfig.EnvironmentTypeMessage_ONPREM.Enum()
	expectedEnv.ProviderType = domainManagerConfig.ProviderTypeMessage_NTNX.Enum()
	expectedEnv.ProvisioningType = domainManagerConfig.ProvisioningTypeMessage_NTNX.Enum()
	expectedDomainManagerDetails.Network.Base.ExternalAddress.Ipv4.Value = &TestPcExternalIp
	expectedRespRet := []*domainManagerConfig.DomainManager{expectedDomainManagerDetails}
	expectedDomainManagerDetails.Network.Base.Fqdn = &TestFqdn
	expectedDomainManagerDetails.NodeExtIds = &domainManagerConfig.StringArrayWrapper{
		Value: []string{TestUhuruVmUuid},
	}
	resp, appMsg, err := ListDomainManagers(nil)

	assert.Nil(t, err)
	assert.Nil(t, appMsg)
	assert.Equal(t, expectedRespRet, resp)
}

func TestGetDomainManagerSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)

	m.ZkClient.EXPECT().GetZkNode(commonConsts.ZEUS_CONFIG_NODE_PATH, true).Return(TestZeusConfigBytes, nil)
	m.IdfSession.EXPECT().SendMsgWithTimeout(consts.IdfGetOperation, gomock.Any(),
		gomock.Any(), consts.IdfBackoffForRetries, gomock.Any()).
		Do(func(operation string, arg interface{},
			response *insights_interface.GetEntitiesRet,
			backoff *misc.ExponentialBackoff, _ interface{}) {
			*response = *TestGetEntitiesRet
		}).Return(nil)
	m.ZkClient.EXPECT().GetChildren(consts.AOSClusterCES, false).Return(TestRegisteredPeList, nil)

	expectedDomainManagerDetails := utils.InitialiseDomainManagerEntity()
	expectedDomainManagerDetails.Base.ExtId = &TestPcClusterUuid
	expectedDomainManagerDetails.IsRegisteredWithHostingCluster = &TestIsRegisteredToPe
	expectedDomainManagerDetails.HostingClusterExtId = &TestPeClusterUuid
	expectedConfig := expectedDomainManagerDetails.GetConfig()
	expectedConfig.Base.BuildInfo.Version = &TestVersion
	expectedConfig.Name = &TestPcName
	expectedConfig.Size = domainManagerConfig.SizeMessage_SMALL.Enum()
	expectedConfig.ResourceConfig = &domainManagerConfig.DomainManagerResourceConfig{
		NumVcpus:          proto.Int32(int32(TestNumVcpus)),
		MemorySizeBytes:   &TestMemorySizeBytes,
		DataDiskSizeBytes: &TestDataDiskSizeBytes,
		ContainerExtIds: &domainManagerConfig.StringArrayWrapper{
			Value: []string{TestContainerUuid},
		},
	}
	expectedDomainManagerDetails.Network.ExternalNetworks.Value = []*domainManagerConfig.ExternalNetwork{
		{
			NetworkExtId: proto.String(TestNetworkExtId),
			Base: &domainManagerConfig.BaseNetwork{
				SubnetMask: &commonConfig.IPAddressOrFQDN{
					Ipv4: &commonConfig.IPv4Address{
						Value: proto.String("2.3.4.5"),
					},
				},
				IpRanges: &commonConfig.IpRangeArrayWrapper{
					Value: []*commonConfig.IpRange{
						{
							Begin: &commonConfig.IPAddress{
								Ipv4: &commonConfig.IPv4Address{
									Value: &TestPcExternalIp,
								},
							},
							End: &commonConfig.IPAddress{
								Ipv4: &commonConfig.IPv4Address{
									Value: &TestPcExternalIp,
								},
							},
						},
					},
				},
			},
		},
	}
	expectedDomainManagerDetails.ShouldEnableHighAvailability = proto.Bool(false)
	expectedEnv := expectedConfig.GetBootstrapConfig().GetEnvironmentInfo()
	expectedEnv.Type = domainManagerConfig.EnvironmentTypeMessage_ONPREM.Enum()
	expectedEnv.ProviderType = domainManagerConfig.ProviderTypeMessage_NTNX.Enum()
	expectedEnv.ProvisioningType = domainManagerConfig.ProvisioningTypeMessage_NTNX.Enum()
	expectedDomainManagerDetails.Network.Base.ExternalAddress.Ipv4.Value = &TestPcExternalIp
	expectedDomainManagerDetails.Network.Base.Fqdn = &TestFqdn
	expectedDomainManagerDetails.NodeExtIds = &domainManagerConfig.StringArrayWrapper{
		Value: []string{TestUhuruVmUuid},
	}

	actualDomainManagerResponse, appMsg, err := GetDomainManager(nil)
	assert.Nil(t, err)
	assert.Nil(t, appMsg)
	assert.Equal(t, expectedDomainManagerDetails, actualDomainManagerResponse.Entity)
}

func TestGetDomainManagerSuccessFailedToPopulateIsRegisteredToHostingPE(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)

	m.ZkClient.EXPECT().GetZkNode(commonConsts.ZEUS_CONFIG_NODE_PATH, true).Return(TestZeusConfigBytes, nil)
	m.IdfSession.EXPECT().SendMsgWithTimeout(consts.IdfGetOperation, gomock.Any(),
		gomock.Any(), consts.IdfBackoffForRetries, gomock.Any()).
		Do(func(operation string, arg interface{},
			response *insights_interface.GetEntitiesRet,
			backoff *misc.ExponentialBackoff, _ interface{}) {
			*response = *TestGetEntitiesRet
		}).Return(nil)
	m.ZkClient.EXPECT().GetChildren(consts.AOSClusterCES, false).Return(nil, TestErr)

	expectedDomainManagerDetails := utils.InitialiseDomainManagerEntity()
	expectedDomainManagerDetails.Base.ExtId = &TestPcClusterUuid
	expectedDomainManagerDetails.IsRegisteredWithHostingCluster = proto.Bool(false)
	expectedDomainManagerDetails.HostingClusterExtId = &TestPeClusterUuid
	expectedConfig := expectedDomainManagerDetails.GetConfig()
	expectedConfig.Base.BuildInfo.Version = &TestVersion
	expectedConfig.Name = &TestPcName
	expectedConfig.Size = domainManagerConfig.SizeMessage_SMALL.Enum()
	expectedConfig.ResourceConfig = &domainManagerConfig.DomainManagerResourceConfig{
		NumVcpus:          proto.Int32(int32(TestNumVcpus)),
		MemorySizeBytes:   &TestMemorySizeBytes,
		DataDiskSizeBytes: &TestDataDiskSizeBytes,
		ContainerExtIds: &domainManagerConfig.StringArrayWrapper{
			Value: []string{TestContainerUuid},
		},
	}
	expectedDomainManagerDetails.Network.ExternalNetworks.Value = []*domainManagerConfig.ExternalNetwork{
		{
			NetworkExtId: proto.String(TestNetworkExtId),
			Base: &domainManagerConfig.BaseNetwork{
				SubnetMask: &commonConfig.IPAddressOrFQDN{
					Ipv4: &commonConfig.IPv4Address{
						Value: proto.String("2.3.4.5"),
					},
				},
				IpRanges: &commonConfig.IpRangeArrayWrapper{
					Value: []*commonConfig.IpRange{
						{
							Begin: &commonConfig.IPAddress{
								Ipv4: &commonConfig.IPv4Address{
									Value: &TestPcExternalIp,
								},
							},
							End: &commonConfig.IPAddress{
								Ipv4: &commonConfig.IPv4Address{
									Value: &TestPcExternalIp,
								},
							},
						},
					},
				},
			},
		},
	}
	expectedDomainManagerDetails.ShouldEnableHighAvailability = proto.Bool(false)
	expectedEnv := expectedConfig.GetBootstrapConfig().GetEnvironmentInfo()
	expectedEnv.Type = domainManagerConfig.EnvironmentTypeMessage_ONPREM.Enum()
	expectedEnv.ProviderType = domainManagerConfig.ProviderTypeMessage_NTNX.Enum()
	expectedEnv.ProvisioningType = domainManagerConfig.ProvisioningTypeMessage_NTNX.Enum()
	expectedDomainManagerDetails.Network.Base.ExternalAddress.Ipv4.Value = &TestPcExternalIp
	expectedDomainManagerDetails.Network.Base.Fqdn = &TestFqdn
	expectedDomainManagerDetails.NodeExtIds = &domainManagerConfig.StringArrayWrapper{
		Value: []string{TestUhuruVmUuid},
	}

	actualDomainManagerResponse, appMsg, err := GetDomainManager(nil)
	assert.Nil(t, err)
	assert.Nil(t, appMsg)
	assert.Equal(t, expectedDomainManagerDetails, actualDomainManagerResponse.Entity)
}

func TestGetDomainManagerFetchZeusConfigError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)

	m.ZkClient.EXPECT().GetZkNode(commonConsts.ZEUS_CONFIG_NODE_PATH, true).Return(nil, TestErr)
	m.GrpcStatusUtil.EXPECT().BuildGrpcError(gomock.Any()).Return(TestErr)

	expectedGuruError := guru_error.GetInternalError(consts.GetDomainManagerOpName)
	expectedAppMsg := []*ntnxApiGuruError.AppMessage{expectedGuruError.ConvertToAppMessagePb()}

	response, appMsg, err := GetDomainManager(nil)
	assert.Nil(t, response)
	assert.Equal(t, expectedAppMsg, appMsg)
	assert.Equal(t, TestErr, err)
}

func TestGetDomainManagerFailure(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)

	m.ZkClient.EXPECT().GetZkNode(commonConsts.ZEUS_CONFIG_NODE_PATH, true).Return(TestZeusConfigBytes, nil)
	m.IdfSession.EXPECT().SendMsgWithTimeout(consts.IdfGetOperation, gomock.Any(),
		gomock.Any(), consts.IdfBackoffForRetries, gomock.Any()).Return(TestErr)
	m.GrpcStatusUtil.EXPECT().BuildGrpcError(gomock.Any()).Return(TestErr)

	expectedGuruError := guru_error.GetInternalError(consts.GetDomainManagerOpName)
	expectedAppMsg := []*ntnxApiGuruError.AppMessage{expectedGuruError.ConvertToAppMessagePb()}

	response, appMsg, err := GetDomainManager(nil)
	assert.Nil(t, response)
	assert.Equal(t, expectedAppMsg, appMsg)
	assert.Equal(t, TestErr, err)
}

func TestIsRegisteredToHostingPEFailureNilId(t *testing.T) {
	isRegistered, err := IsRegisteredToHostingPE("")
	assert.EqualError(t, err, consts.ErrNilHostingClusterExtId.Error())
	assert.False(t, isRegistered)
}

func TestIsRegisteredToHostingPENotRegistered(t *testing.T) {
	m := test.MockSingletons(t)
	m.ZkClient.EXPECT().GetChildren(consts.AOSClusterCES, false).Return(TestRegisteredPeList, nil)

	isRegistered, err := IsRegisteredToHostingPE("invalid_pe_id")
	assert.Nil(t, err)
	assert.False(t, isRegistered)
}

func TestPopulateNetworkConfig(t *testing.T) {

	domainManager := utils.InitialiseDomainManagerEntity()
	domainManagerNodeInfo := utils.InitialiseDomainManagerNodeInfo()
	PopulateNetworkConfig(TestZeusConfigDTO, domainManager, domainManagerNodeInfo)
	assert.Equal(t, "2.3.4.5", domainManager.GetNetwork().GetExternalNetworks().GetValue()[0].GetBase().GetSubnetMask().GetIpv4().GetValue())
}

func TestPopulateNtpServerList(t *testing.T) {
	expectedNtpServerList := []*commonConfig.IPAddressOrFQDN{
		{
			Fqdn: &commonConfig.FQDN{Value: proto.String("ntp1")},
		},
		{
			Fqdn: &commonConfig.FQDN{Value: proto.String("ntp2")},
		},
	}

	zeusConfig := &zeusConfig.ConfigurationProto{
		NtpServerList: []string{"ntp1", "ntp2"},
	}
	domainManager := utils.InitialiseDomainManagerEntity()

	PopulateNtpServerList(zeusConfig, domainManager)
	assert.Equal(t, expectedNtpServerList, domainManager.GetNetwork().GetBase().GetNtpServers().GetValue())
}

func TestPopulateNameServerList(t *testing.T) {
	expectedNameServerList := []*commonConfig.IPAddressOrFQDN{
		{
			Ipv4: &commonConfig.IPv4Address{Value: proto.String("ip1")},
		},
		{
			Ipv4: &commonConfig.IPv4Address{Value: proto.String("ip2")},
		},
	}

	zeusConfig := &zeusConfig.ConfigurationProto{
		NameServerIpList: []string{"ip1", "ip2", consts.LocalHostIp},
	}
	domainManager := utils.InitialiseDomainManagerEntity()

	PopulateNameServerList(zeusConfig, domainManager)
	assert.Equal(t, expectedNameServerList, domainManager.GetNetwork().GetBase().GetNameServers().GetValue())
}
