/*
* Copyright (c) 2024 Nutanix Inc. All rights reserved.
*
* Author: @viraj-nutanix
*
* This file contains the utility functions for PC related operations.
 */

package utils

import (
	"encoding/json"
	"fmt"
	cluster_config "ntnx-api-guru-pc/generated-code/target/protobuf/clustermgmt/v4/config"
	common_config "ntnx-api-guru-pc/generated-code/target/protobuf/common/v1/config"
	"ntnx-api-guru-pc/generated-code/target/protobuf/common/v1/response"
	domainManagerConfig "ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/config"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/external"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/models/cmsp_status"
	"strings"

	"github.com/golang/protobuf/proto"
	aplos "github.com/nutanix-core/go-cache/aplos/sl_bufs"
	"github.com/nutanix-core/go-cache/ergon"
	genesisSvc "github.com/nutanix-core/go-cache/infrastructure/cluster/client/genesis"
	"github.com/nutanix-core/go-cache/insights/insights_interface"
	log "github.com/sirupsen/logrus"
)

// It fetches the PC environment data from zk node and returns it.
// args: None
// returns: PC environment data if successful
func FetchPcEnvironmentInfo() (*aplos.PCEnvironmentConfig_PCEnvironmentInfo, error) {
	data, err := external.Interfaces().ZkClient().GetZkNode(consts.PCEnvZkNodePath, true)
	if err != nil {
		return nil, fmt.Errorf("error fetching pc environment data from zk path %s: %w", consts.PCEnvZkNodePath, err)
	}
	envConfig := &aplos.PCEnvironmentConfig{}
	err = proto.Unmarshal(data, envConfig)
	if err != nil {
		return nil, fmt.Errorf("failed to unmarshalling pc environment data: %w", err)
	}
	return envConfig.PcEnvironmentInfo, nil
}

func IsNativeCloudPc() (bool, error) {
	envConfig, err := FetchPcEnvironmentInfo()
	if err != nil {
		return false, err
	}
	if envConfig.GetEnvironmentType() == aplos.PCEnvironmentConfig_PCEnvironmentInfo_NTNX_CLOUD &&
		envConfig.GetInstanceType() == aplos.PCEnvironmentConfig_PCEnvironmentInfo_NATIVE_PROVISIONED {
		return true, nil
	}
	return false, nil
}

func IsXiPc() (bool, error) {
	envConfig, err := FetchPcEnvironmentInfo()
	if err != nil {
		return false, err
	}
	if envConfig.GetEnvironmentType() == aplos.PCEnvironmentConfig_PCEnvironmentInfo_NTNX_CLOUD &&
		envConfig.GetCloudProviderInfo() == aplos.PCEnvironmentConfig_PCEnvironmentInfo_NTNX &&
		envConfig.GetInstanceType() == aplos.PCEnvironmentConfig_PCEnvironmentInfo_NTNX_PROVISIONED {
		return true, nil
	}
	return false, nil
}

func GetRegisteredPeList() ([]string, error) {
	registeredPeList, _, err := external.Interfaces().ZkSession().Children(consts.AOSClusterCES, true)
	if err != nil {
		log.Errorf("error fetching registered pe list from zk: %v", err)
		return nil, err
	}
	log.Debugf("fetched registered pe list: %v", registeredPeList)
	return registeredPeList, nil
}

func GetRegisteredPcList() ([]string, error) {
	pairedPcList, _, err := external.Interfaces().ZkSession().Children(consts.DomainManagerCES, true)
	if err != nil {
		log.Errorf("error fetching registered pc list from zk: %v", err)
		return nil, err
	}
	log.Debugf("fetched registered pc list: %v", pairedPcList)
	return pairedPcList, nil
}

// It initialise the in-memory domain manager entity with default/nil values.
// args: None
// returns: domain manager entity with default values
func InitialiseDomainManagerEntity() *domainManagerConfig.DomainManager {
	return &domainManagerConfig.DomainManager{
		Base: &response.ExternalizableAbstractModel{},
		Config: &domainManagerConfig.DomainManagerClusterConfig{
			Base: &cluster_config.ClusterConfig{
				BuildInfo: &cluster_config.BuildInfo{},
			},
			BootstrapConfig: &domainManagerConfig.BootstrapConfig{
				EnvironmentInfo: &domainManagerConfig.EnvironmentInfo{},
			},
			ResourceConfig: &domainManagerConfig.DomainManagerResourceConfig{},
		},
		Network: &domainManagerConfig.DomainManagerNetwork{
			Base: &cluster_config.ClusterNetwork{
				ExternalAddress: &common_config.IPAddress{
					Ipv4: &common_config.IPv4Address{},
				},
				NameServers: &common_config.IPAddressOrFQDNArrayWrapper{
					Value: []*common_config.IPAddressOrFQDN{},
				},
				NtpServers: &common_config.IPAddressOrFQDNArrayWrapper{
					Value: []*common_config.IPAddressOrFQDN{},
				},
			},
			ExternalNetworks: &domainManagerConfig.ExternalNetworkArrayWrapper{
				Value: []*domainManagerConfig.ExternalNetwork{},
			},
		},
	}
}

// It initialise the in-memory domain manager nodes info with default/nil values.
// args: None
// returns: domain manager nodes info with default values
func InitialiseDomainManagerNodeInfo() *models.DomainManagerNodesInfo {
	return &models.DomainManagerNodesInfo{
		VmDetailsList: []*models.VmDetails{},
	}
}

// It populates the in-memory domain manager entity with the vm details fetched from IDF entity.
// args: domainManagerDetails - in-memory domain manager entity, domainManagerNodesInfo - in-memory domain manager nodes info
func PopulateVmDetails(domainManagerDetails *domainManagerConfig.DomainManager,
	domainManagerNodesInfo *models.DomainManagerNodesInfo) {

	if len(domainManagerNodesInfo.GetVmDetailsList()) > 1 {
		domainManagerDetails.ShouldEnableHighAvailability = proto.Bool(true)
	} else {
		domainManagerDetails.ShouldEnableHighAvailability = proto.Bool(false)
	}

	var totalVcpus, totalMemorySize, totalDiskSize int64
	containerExtIdMap := make(map[string]bool)
	var vmUuids []string

	for _, vmDetails := range domainManagerNodesInfo.GetVmDetailsList() {
		if vmDetails.GetVmSpecs() != nil {
			totalVcpus += vmDetails.GetVmSpecs().GetNumVcpus()
			totalMemorySize += vmDetails.GetVmSpecs().GetMemorySizeBytes()
			totalDiskSize += vmDetails.GetVmSpecs().GetDataDiskSizeBytes()
			containerExtIdMap[vmDetails.GetVmSpecs().GetContainerExtId()] = true
			vmUuids = append(vmUuids, vmDetails.GetVmSpecs().GetUuid())
		} else {
			log.Warnf("VM details are missing")
		}
	}
	var containerExtIds []string
	for containerExtId := range containerExtIdMap {
		containerExtIds = append(containerExtIds, containerExtId)
	}

	resourceConfig := domainManagerDetails.GetConfig().GetResourceConfig()
	resourceConfig.NumVcpus = proto.Int32(int32(totalVcpus))
	resourceConfig.MemorySizeBytes = proto.Int64(totalMemorySize)
	resourceConfig.DataDiskSizeBytes = proto.Int64(totalDiskSize)
	resourceConfig.ContainerExtIds = &domainManagerConfig.StringArrayWrapper{
		Value: containerExtIds,
	}
	domainManagerDetails.NodeExtIds = &domainManagerConfig.StringArrayWrapper{
		Value: vmUuids,
	}
}

// It populates the in-memory domain manager entity and domain manager nodes info
// with the values fetched from IDF entity.
// args: domainManagerIdfEntity - IDF entity of domain manager, domainManagerDetails - in-memory domain manager entity,	domainManagerNodesInfo - in-memory domain manager nodes info
func BuildDomainManagerDetailsFromIdfEntity(
	domainManagerIdfEntity *insights_interface.Entity,
	domainManagerDetails *domainManagerConfig.DomainManager,
	domainManagerNodesInfo *models.DomainManagerNodesInfo,
) {
	if domainManagerIdfEntity == nil {
		log.Warnf("Domain manager IDF entity does not exist")
		return
	}

	for _, nameValuePair := range domainManagerIdfEntity.GetAttributeDataMap() {

		config := domainManagerDetails.GetConfig()
		environmentInfo := config.GetBootstrapConfig().GetEnvironmentInfo()
		switch nameValuePair.GetName() {
		case consts.DomainManagerNodesInfo:
			if domainManagerNodesInfo != nil {
				nodeInfoBytes := nameValuePair.GetValue().GetBytesValue()
				err := proto.Unmarshal(nodeInfoBytes, domainManagerNodesInfo)
				if err != nil {
					log.Warnf("Failed to unmarshal node info : %s", err)
				} else {
					PopulateVmDetails(domainManagerDetails, domainManagerNodesInfo)
				}
			} else {
				log.Warnf("Domain manager nodes info is nil")
			}
		case consts.DomainManagerExtID:
			domainManagerDetails.Base.ExtId = proto.String(nameValuePair.GetValue().GetStrValue())
		case consts.DomainManagerName:
			config.Name = proto.String(nameValuePair.GetValue().GetStrValue())
		case consts.DomainManagerLockdownMode:
			config.Base.ShouldEnableLockdownMode = proto.Bool(nameValuePair.GetValue().GetBoolValue())
		case consts.DomainManagerVersion:
			config.Base.BuildInfo.Version = proto.String(nameValuePair.GetValue().GetStrValue())
		case consts.DomainManagerFlavour:
			config.Size = (domainManagerConfig.SizeMessage_Size)(domainManagerConfig.SizeMessage_Size_value[nameValuePair.GetValue().GetStrValue()]).Enum()
		case consts.DomainManagerEnvironmentType:
			environmentInfo.Type = (domainManagerConfig.EnvironmentTypeMessage_EnvironmentType)(domainManagerConfig.EnvironmentTypeMessage_EnvironmentType_value[nameValuePair.GetValue().GetStrValue()]).Enum()
		case consts.DomainManagerProviderType:
			environmentInfo.ProviderType = (domainManagerConfig.ProviderTypeMessage_ProviderType)(domainManagerConfig.ProviderTypeMessage_ProviderType_value[nameValuePair.GetValue().GetStrValue()]).Enum()
		case consts.DomainManagerInstanceType:
			environmentInfo.ProvisioningType = (domainManagerConfig.ProvisioningTypeMessage_ProvisioningType)(domainManagerConfig.ProvisioningTypeMessage_ProvisioningType_value[nameValuePair.GetValue().GetStrValue()]).Enum()
		case consts.DomainManagerNumVcpus:
			// This attribute will be populated by the nodes info byte present in IDF entity,
			// Hence ignoring this attribute
		case consts.DomainManagerMemorySizeBytes:
			// This attribute will be populated by the nodes info byte present in IDF entity,
			// Hence ignoring this attribute
		case consts.DomainManagerDiskCapacityBytes:
			// This attribute will be populated by the nodes info byte present in IDF entity,
			// Hence ignoring this attribute
		case consts.DomainManagerHostingClusterExtID:
			domainManagerDetails.HostingClusterExtId = proto.String(nameValuePair.GetValue().GetStrValue())
		default:
			log.Warnf("unknown attribute %s in Domain Manager entity", nameValuePair.GetName())
		}
	}
	log.Debugf("State of domain manager details after builing from IDF entity : %+v",
		domainManagerDetails)
	log.Debugf("State of domain manager nodes info after builing from IDF entity : %+v",
		domainManagerNodesInfo)
}

// It checks if the environment details are present in the domain manager entity.
// args: domainManagerEntity - IDF domain manager entity
// returns: true if environment details are present, false otherwise
func IsEnvironmentDetailsPresent(domainManagerEntity *insights_interface.Entity) bool {
	if domainManagerEntity == nil {
		return false
	}

	var isEnvironmentTypePresent, isProviderTypePresent, isInstanceObjPresent bool
	for _, nameValuePair := range domainManagerEntity.GetAttributeDataMap() {
		switch nameValuePair.GetName() {
		case consts.DomainManagerEnvironmentType:
			isEnvironmentTypePresent = true
		case consts.DomainManagerProviderType:
			isProviderTypePresent = true
		case consts.DomainManagerInstanceType:
			isInstanceObjPresent = true
		}
	}

	return isEnvironmentTypePresent && isProviderTypePresent && isInstanceObjPresent
}

// It returns the IP block list in []*common_config.IpRange type
// args: ipRangeList - list of IP ranges in string format
// returns: IP block list in []*common_config.IpRange type
func FetchListOfIpRangeFromStringList(ipRangeList []string) []*common_config.IpRange {
	platformIpBlockList := []*common_config.IpRange{}
	for _, ipRange := range ipRangeList {
		ipRangeSplit := strings.Split(ipRange, " ")
		if len(ipRangeSplit) != 2 {
			log.Warnf("invalid ip range %s", ipRange)
			continue
		}
		platformIpBlockList = append(platformIpBlockList, &common_config.IpRange{
			Begin: &common_config.IPAddress{
				Ipv4: &common_config.IPv4Address{
					Value: proto.String(ipRangeSplit[0]),
				},
			},
			End: &common_config.IPAddress{
				Ipv4: &common_config.IPv4Address{
					Value: proto.String(ipRangeSplit[1]),
				},
			},
		})
	}
	return platformIpBlockList
}

// It fetches the CMSP status from the Genesis service.
// args: None
// returns: CMSP status map if successful
func GetCmspStatus() (*cmsp_status.GetCmspStatusResponse, error) {
	getCmspArg := &genesisSvc.GetCmspStatus{}
	response, err := external.Interfaces().GenesisJsonRpcClient().GetCmspStatus(getCmspArg)
	if err != nil {
		return nil, fmt.Errorf("failed to get cmsp status : %w", err)
	}

	cmspResponse := cmsp_status.GetCmspStatusResponse{}
	err = json.Unmarshal([]byte(response), &cmspResponse)
	if err != nil {
		return nil, fmt.Errorf("failed to unmarshal getCmspStatus response : %w", err)
	}

	log.Debugf("Response body of get cmsp status %+v", cmspResponse)
	return &cmspResponse, nil
}

// Takes the PC version string and converts to PC Major Version.
// Returns 2023.4 for pc.2023.4.0.1/2023.4.0.1
// Input has to be of the format pc.xxxx.x*.****
func GetPCMajorReleaseVersion(version string) (string, error) {
	const prefix = "pc."
	version = strings.TrimPrefix(version, prefix)
	splitVersions := strings.Split(version, ".")
	if len(splitVersions) < 2 {
		return "", fmt.Errorf("error parsing PC version. Atlease one decimal expected")
	}
	return (splitVersions[0] + "." + splitVersions[1]), nil
}

// Trims pc prefix ("pc.") from the
// release version ("pc.2024.3") if it exists
func GetPCReleaseVersion(version string) string {
	const prefix = "pc."
	return strings.TrimPrefix(version, prefix)
}

// Checks if a cluster upgrade is in progress
func IsUpgradeInProgress() (bool, error) {
	config, err := FetchZeusConfig()
	if err != nil {
		log.Errorf("error fetching zeus config: %s", err)
		return false, err
	}
	return config.IsClusterUpgrading(), nil
}

// Checks if a PC-Scaleout is in progress
func IsScaleoutInProgress() (bool, error) {
	log.Debugf("Checking for existing queued or running PC scaleout tasks")
	taskArg := ergon.TaskListArg{
		OperationTypeList: []string{"kPrismCentralAddNodeRequest"},
		StatusList:        []ergon.Task_Status{ergon.Task_kQueued, ergon.Task_kRunning},
	}
	tasksRet, err := external.Interfaces().ErgonClient().TaskList(&taskArg)
	if err != nil {
		log.Errorf("Error fetching PC scaleout ergon tasks: %s", err)
		return false, err
	}
	if len(tasksRet.GetTaskUuidList()) >= 1 {
		log.Infof("Found more than one running or queued PC scaleout tasks: %s", tasksRet.String())
		return true, nil
	}
	log.Debugf("No existing PC scaleout tasks found.")
	return false, nil
}