/*
 * Copyright (c) 2023 Nutanix Inc. All rights reserved.
 * Author: viraj.aute@nutanix.com
 */

package models

type ClusterRegistrationPrecheckDTO struct {
	IsUpgrading                    bool   `json:"isUpgrading"`
	IsNTPSet                       bool   `json:"isNTPSet"`
	IsVersionCompatible            bool   `json:"isVersionCompatible"`
	ClusterUuid                    string `json:"clusterUuid"`
	ClusterVersion                 string `json:"clusterVersion"`
	IsPcDeploymentInProgress       bool   `json:"isPcDeploymentInProgress"`
	IsAOSVersionCompatibleWithPCDR bool   `json:"isAOSVersionCompatibleWithPCDR"`
	IsUnregistrationInProgress     bool   `json:"isUnregistrationInProgress"`
	IsRegistrationInProgress       bool   `json:"isRegistrationInProgress"`
	IsRegistered                   bool   `json:"isRegistered"`
	IsClusterBlacklistedOnPE       bool   `json:"isClusterBlacklistedOnPE"`
	IsClusterBlacklistedOnPC       bool   `json:"isClusterBlacklistedOnPC"`
}
