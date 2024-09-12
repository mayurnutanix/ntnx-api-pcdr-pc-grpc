/*
 * Copyright (c) 2024 Nutanix Inc. All rights reserved.
 *
 * Author: deepanshu.jain@nutanix.com
 *
 * This file contains the ConfigureConnection interface which can be implement by any cluster type eg. MULTI_CLUSTER, WITNESS_VM etc.
 * Implement Connect and Disconnect methods for the cluster type.
 * Add an entry in NewConfigureConnection method to create ConfigureConnection object for the cluster type.
 */

package configure_connection

import (
	"ntnx-api-guru-pc/generated-code/target/dto/models/prism/v4/management"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/services/register/pc_domain"

	log "github.com/sirupsen/logrus"
)

type ConnectionConfig interface {
	RunPreConfigureActions(jobMetadata *models.Job) error
	Configure(jobMetadata *models.Job) error
	RunPreUnconfigureActions(jobMetadata *models.Job) error
	Unconfigure(jobMetadata *models.Job) error
}

func New(clusterType management.ClusterType) (configureConnection ConnectionConfig) {
	switch clusterType {
	case management.CLUSTERTYPE_DOMAIN_MANAGER:
		configureConnection = &PCConnConfig{
			pcDomain: &pc_domain.PCDomainImpl{},
		}
	default:
		log.Errorf("configure connection not implemented for: %s", clusterType.GetName())
	}
	return configureConnection
}
