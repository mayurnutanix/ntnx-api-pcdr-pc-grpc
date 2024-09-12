/*
* Copyright (c) 2024 Nutanix Inc. All rights reserved.
* Author: viraj.aute@nutanix.com
* This file contains the implementation of GET and LIST domain manager APIs.
 */

package get_domain_manager

import (
	"fmt"
	"ntnx-api-guru-pc/generated-code/target/protobuf/common/v1/config"
	domainManagerConfig "ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/config"
	ntnxApiGuruError "ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/error"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/grpc_error"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/external"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/utils"
	"ntnx-api-guru-pc/guru-pc-api-service/utils/idf"
	"strings"

	zeus_config "github.com/nutanix-core/go-cache/zeus/config"
	log "github.com/sirupsen/logrus"
	"google.golang.org/protobuf/proto"
)

// ListDomainManagers fetches domain manager details
// args: zeusConfig: zeus config proto
// returns: domainManagerList: list of domain managers
func ListDomainManagers(zeusConfig *zeus_config.ConfigurationProto,
) ([]*domainManagerConfig.DomainManager, []*ntnxApiGuruError.AppMessage, error) {

	domainManagerResponse, errorMsg, err := GetDomainManager(zeusConfig)
	if err != nil {
		return nil, errorMsg, err
	}

	return []*domainManagerConfig.DomainManager{domainManagerResponse.Entity}, nil, nil
}

// GetDomainManager fetches details from IDF and Zeus config and populates the response.
// args: domainManagerExtId: extId of the domain manager
// returns: domainManagerEntity: domain manager entity
func GetDomainManager(zeusConfig *zeus_config.ConfigurationProto) (
	*models.DomainManagerResponse, []*ntnxApiGuruError.AppMessage, error) {

	// Verify if zeus config is not nil
	if zeusConfig == nil {
		config, err := utils.FetchZeusConfig()
		if err != nil {
			guruErr := guru_error.GetInternalError(consts.GetDomainManagerOpName)
			return nil, []*ntnxApiGuruError.AppMessage{guruErr.ConvertToAppMessagePb()},
				grpc_error.GetGrpcStatusUtilImpl().BuildGrpcError(guruErr)
		} else {
			zeusConfig = config
		}
	}

	domainManagerExtId := zeusConfig.GetClusterUuid()
	domainManagerEntity := utils.InitialiseDomainManagerEntity()
	domainManagerNodeInfo := utils.InitialiseDomainManagerNodeInfo()
	domainManagerETag := &models.DomainManagerETag{}

	// Fetch details from idf domain manager entity
	err := PopulateDomainManagerFromIdf(domainManagerExtId, domainManagerEntity,
		domainManagerNodeInfo, domainManagerETag)
	if err != nil {
		guruErr := guru_error.GetInternalError(consts.GetDomainManagerOpName)
		return nil, []*ntnxApiGuruError.AppMessage{guruErr.ConvertToAppMessagePb()},
			grpc_error.GetGrpcStatusUtilImpl().BuildGrpcError(guruErr)
	}

	// Check if registered to hosting PE
	IsRegisteredToHostingPE, err := IsRegisteredToHostingPE(domainManagerEntity.GetHostingClusterExtId())
	if err != nil {
		log.Warnf("%s unable to verify hosting cluster registration status : %s",
			consts.GetDomainManagerLoggerPrefix, err)
	}
	domainManagerEntity.IsRegisteredWithHostingCluster = &IsRegisteredToHostingPE
	// Populate network config
	PopulateNetworkConfig(zeusConfig, domainManagerEntity, domainManagerNodeInfo)
	return &models.DomainManagerResponse{Entity: domainManagerEntity, Etag: domainManagerETag}, nil, nil
}

// PopulateDomainManagerFromIdf fetches domain manager entity from IDF and
// populates the in-memory domain manager entity.
// args: domainManagerExtId: extId of the domain manager,domainManagerEntity: in-memory domain manager entity
// returns: error
func PopulateDomainManagerFromIdf(domainManagerExtId string,
	domainManagerEntity *domainManagerConfig.DomainManager,
	domainManagerNodeInfo *models.DomainManagerNodesInfo,
	domainManagerETag *models.DomainManagerETag) error {

	// Fetch IDF domainManager entity
	idfDomainManager, err := idf.FetchEntityFromIdf(consts.DomainManagerEntityName,
		domainManagerExtId)
	if err != nil {
		return fmt.Errorf("error in fetching domainManager entity from IDF : %w", err)
	}

	// Populate domainManager entity
	utils.BuildDomainManagerDetailsFromIdfEntity(idfDomainManager,
		domainManagerEntity, domainManagerNodeInfo)

	log.Infof("%s Successfully populated domainManager entity from IDF",
		consts.GetDomainManagerLoggerPrefix)

	// Get current ETag from IDF entity
	utils.GetCurrentDomainManagerETag(idfDomainManager, domainManagerETag)
	return nil
}

// IsRegisteredToHostingPE checks if the PC is registered to hosting PE
// args: hostingPeId: hosting PE extId
// returns:  true if registered, false otherwise
func IsRegisteredToHostingPE(hostingPeId string) (bool, error) {
	if hostingPeId == "" {
		return false, consts.ErrNilHostingClusterExtId
	}

	// Fetch registered cluster list
	registeredList, err := external.Interfaces().ZkClient().GetChildren(
		consts.AOSClusterCES, false)
	if err != nil {
		return false, fmt.Errorf("error in fetching registered cluster list : %w", err)
	}

	// Check if hosting PE is registered
	for _, clusterUuid := range registeredList {
		if clusterUuid == hostingPeId {
			log.Infof("%s PC is registered to hosting PE %s",
				consts.GetDomainManagerLoggerPrefix, hostingPeId)
			return true, nil
		}
	}

	log.Infof("%s PC is not registered to hosting PE %s",
		consts.GetDomainManagerLoggerPrefix, hostingPeId)
	return false, nil
}

// PopulateNtpServerList populates NTP server list details in in-memory domain manager entity
// args: zeusConfig: zeus config proto, domainManagerEntity: in-memory domain manager entity
func PopulateNtpServerList(zeusConfig *zeus_config.ConfigurationProto,
	domainManagerEntity *domainManagerConfig.DomainManager) {

	ntpServerList := zeusConfig.GetNtpServerList()
	finalIpList := domainManagerEntity.GetNetwork().GetBase().GetNtpServers().GetValue()
	for i := 0; i < len(ntpServerList); i++ {
		finalIpList = append(finalIpList, &config.IPAddressOrFQDN{
			Fqdn: &config.FQDN{
				Value: &ntpServerList[i],
			},
		})
	}
	domainManagerEntity.Network.Base.NtpServers.Value = finalIpList
	log.Infof("%s Successfully populated NTP server list", consts.GetDomainManagerLoggerPrefix)
}

// PopulateNameServerList populates Name server list details in in-memory domain manager entity
// args: zeusConfig: zeus config proto, domainManagerEntity: in-memory domain manager entity
func PopulateNameServerList(zeusConfig *zeus_config.ConfigurationProto,
	domainManagerEntity *domainManagerConfig.DomainManager) {

	nameServerList := zeusConfig.GetNameServerIpList()
	finalNameServerList := domainManagerEntity.GetNetwork().GetBase().GetNameServers().GetValue()
	for i := 0; i < len(nameServerList); i++ {
		if nameServerList[i] == consts.LocalHostIp {
			continue
		}
		finalNameServerList = append(finalNameServerList, &config.IPAddressOrFQDN{
			Ipv4: &config.IPv4Address{
				Value: &nameServerList[i],
			},
		})
	}
	domainManagerEntity.Network.Base.NameServers.Value = finalNameServerList
	log.Infof("%s Successfully populated name server list", consts.GetDomainManagerLoggerPrefix)
}

// PopulateNetworkConfig populates network config details in in-memory domain manager entity
// args: zeusConfig: zeus config proto, domainManagerEntity: in-memory domain manager entity
func PopulateNetworkConfig(zeusConfig *zeus_config.ConfigurationProto,
	domainManagerEntity *domainManagerConfig.DomainManager,
	domainManagerNodeInfo *models.DomainManagerNodesInfo) {

	// Populate FQDN
	if zeusConfig.GetClusterFullyQualifiedDomainName() != "" {
		domainManagerEntity.Network.Base.Fqdn = proto.String(zeusConfig.GetClusterFullyQualifiedDomainName())
	}

	// Populate external IP address
	if zeusConfig.GetClusterExternalIp() != "" {
		domainManagerEntity.Network.Base.ExternalAddress.Ipv4.Value = proto.String(zeusConfig.GetClusterExternalIp())
	}

	// Initialise External Network details object
	externalNetwork := &domainManagerConfig.ExternalNetwork{
		Base: &domainManagerConfig.BaseNetwork{
			IpRanges: &config.IpRangeArrayWrapper{
				Value: []*config.IpRange{},
			},
		},
	}

	// Populating the network Ext Id
	if domainManagerNodeInfo.GetNetworkExtId() != "" {
		externalNetwork.NetworkExtId = proto.String(domainManagerNodeInfo.GetNetworkExtId())
	}
	
	// Populating external default gateway Ip
	if zeusConfig.GetDefaultGatewayIp() != "" {
		externalNetwork.Base.DefaultGateway = &config.IPAddressOrFQDN{
			Ipv4: &config.IPv4Address{
				Value: proto.String(zeusConfig.GetDefaultGatewayIp()),
			},
		}
	}

	// Populating external subnet mask
	if zeusConfig.GetExternalSubnet() != "" {
		extSubnet := zeusConfig.GetExternalSubnet()
		extSubnetTokens := strings.Split(extSubnet, "/")
		if len(extSubnetTokens) == 2 {
			externalNetwork.Base.SubnetMask = &config.IPAddressOrFQDN{
				Ipv4: &config.IPv4Address{
					Value: proto.String(extSubnetTokens[1]),
				},
			}
		}
	}

	// Populating external network ip range list
	// Ip range list contains the node Ips of the PC and is stored in the following way
	// IP range list = {nodeIp1... nodeIp1,
	//                  nodeIp2... nodeIp2,
	//                  nodeIp3... nodeIp3}
	for _, node := range zeusConfig.GetNodeList() {
		if node.GetServiceVmExternalIp() != "" {
			ipRange := &config.IpRange{
				Begin: &config.IPAddress{
					Ipv4: &config.IPv4Address{
						Value: proto.String(node.GetServiceVmExternalIp()),
					},
				},
				End: &config.IPAddress{
					Ipv4: &config.IPv4Address{
						Value: proto.String(node.GetServiceVmExternalIp()),
					},
				},
			}
			externalNetwork.Base.IpRanges.Value =
				append(externalNetwork.Base.IpRanges.Value, ipRange)
		}
	}

	if len(domainManagerEntity.Network.ExternalNetworks.Value) == 0 {
		domainManagerEntity.Network.ExternalNetworks.Value =
			[]*domainManagerConfig.ExternalNetwork{externalNetwork}
	} else {
		domainManagerEntity.Network.ExternalNetworks.Value[0] = externalNetwork
	}

	// Populate NTP server list
	PopulateNtpServerList(zeusConfig, domainManagerEntity)

	// Populate Name server list in response.
	PopulateNameServerList(zeusConfig, domainManagerEntity)

	log.Infof("%s Successfully populated network config details", consts.GetDomainManagerLoggerPrefix)
}
