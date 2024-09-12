/*
* Copyright (c) 2024 Nutanix Inc. All rights reserved.
*
* Author: @viraj-nutanix
*
* This file contains the utility functions for trust zk node.
 */

package utils

import (
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/external"

	"github.com/nutanix-core/go-cache/prism"
	zeus_config "github.com/nutanix-core/go-cache/zeus/config"
	common_consts "github.com/nutanix-core/ntnx-api-guru/services/constants"
	log "github.com/sirupsen/logrus"
)

func FetchTrustZkConfig(clusterUuid string) (*prism.ClusterExternalState, error) {
	// Fetch trust zk node.
	zkPath := common_consts.TRUST_SETUP_NODE_PATH + "/" + clusterUuid
	trustZk, err := external.Interfaces().ZkClient().GetZkNode(zkPath, true)
	if err != nil {
		log.Errorf("Unable to fetch trust zk node at path %s with %s", zkPath, err)
		return nil, err
	}

	trustState := new(prism.ClusterExternalState)
	err = trustState.XXX_Unmarshal(trustZk)
	if err != nil {
		log.Errorf("Failed to unmarshal the trust zk content at path %s with err : %s", zkPath, err)
		return nil, err
	}

	return trustState, nil
}

func FetchIpAddressFromTrustZkNode(clusterUuid string) (string, error) {
	zkData, err := FetchTrustZkConfig(clusterUuid)
	if err != nil {
		log.Errorf("Failed to get zk node for cluster %s : %s", clusterUuid, err)
		return "", consts.ErrorTrustZkNodeNotFoundFailure
	}

	if zkData.GetClusterDetails() == nil ||
		zkData.GetClusterDetails().GetContactInfo() == nil ||
		zkData.GetClusterDetails().GetContactInfo().GetAddressList() == nil ||
		len(zkData.GetClusterDetails().GetContactInfo().GetAddressList()) == 0 {
		log.Errorf("Failed to get CVM IP addresses from cluster"+
			" external state for cluster UUID %s.", clusterUuid)
		return "", consts.ErrorTrustZkNodeIncompleteDataFailure
	}

	ipAddresses := zkData.GetClusterDetails().GetContactInfo().GetAddressList()
	ip := ipAddresses[0]

	return ip, nil
}

func FetchZeusConfig() (*zeus_config.ConfigurationProto, error) {
	// Fetch cluster uuid and cluster version from zeus config
	zeusConfigurationData, err := external.Interfaces().ZkClient().GetZkNode(
		common_consts.ZEUS_CONFIG_NODE_PATH, true)
	if err != nil {
		return nil, err
	}

	zeusConfigurationDTO := &zeus_config.ConfigurationProto{}
	err = zeusConfigurationDTO.XXX_Unmarshal(zeusConfigurationData)
	if err != nil {
		return nil, err
	}

	return zeusConfigurationDTO, nil
}

func FetchHostingPEIdFromTrustZk() (string, error) {
	// Check if trust node has a child, if yes it need to be the hosting PE.
	trustList, err := external.Interfaces().ZkClient().
		GetChildren(common_consts.TRUST_SETUP_NODE_PATH, true)
	if err != nil {
		log.Errorf("Unable to list trust zk children nodes with error : %s", err)
		return "", consts.ErrorTrustZkNodeNotFoundFailure
	}

	if len(trustList) < 1 {
		log.Errorf("No children for trust zk found, invalid trust configuration")
		return "", consts.ErrorInvalidTrustZkNode
	}

	// return first element of list since for a domain manager, only one trust instance can exist.
	log.Infof("Trust configuration list : %v", trustList)
	return trustList[0], nil
}
