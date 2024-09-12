/*
 * Copyright (c) 2023 Nutanix Inc. All rights reserved.
 * Author: harsh.seta@nutanix.com
 */

package models

type RegistrationPrecheckDetailsDTO struct {
	PEClusterUuid             *string
	PEVersion                 *string
	PCClusterUuid             *string
	PCVersion                 *string
	PERegistrationPrecheckDTO *ClusterRegistrationPrecheckDTO
	PCRegistrationPrecheckDTO *ClusterRegistrationPrecheckDTO
}
