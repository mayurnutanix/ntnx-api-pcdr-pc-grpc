/*
 * Copyright (c) 2024 Nutanix Inc. All rights reserved.
 * Author: deepanshu.jain@nutanix.com
 * This file contains the PC (Domain Manager) Implementation of UnregisterIfc interface
 */

package register_pc

import (
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/services/register"
	"ntnx-api-guru-pc/guru-pc-api-service/services/register/configure_connection"
	"ntnx-api-guru-pc/guru-pc-api-service/services/trust_setup/exchange_root_certificate"
	xchgcert "ntnx-api-guru-pc/guru-pc-api-service/services/trust_setup/exchange_root_certificate"
	"ntnx-api-guru-pc/guru-pc-api-service/utils"

	log "github.com/sirupsen/logrus"
)

type UnregisterPCImpl struct {
	ConfigureConnection configure_connection.ConnectionConfig
	Precheck            register.Precheck
}

func NewUnregisterPCImpl() register.Unregister {
	return &UnregisterPCImpl{
		ConfigureConnection: &configure_connection.PCConnConfig{},
		Precheck:            &UnregisterPCPrecheck{},
	}

}

// Executes and performs certain prechecks as part of PC-PC Unregistration
func (unregisterPcSvc *UnregisterPCImpl) RunPreActions(job *models.Job) error {
	updateSelfClusterDetails(job)
	err := unregisterPcSvc.Precheck.Execute(job)
	if err != nil {
		log.Errorf("[%s] Error in PC-PC Unregistration Prechecks: %s", job.JobMetadata.ContextId, err)
		job.JobMetadata.ErrorDetails = err.GetTaskErrorDetails()
		return err
	}
	log.Infof("[%s] PC-PC Unregistration Prechecks completed successfully.", job.JobMetadata.ContextId)
	return nil
}

// Remove remote and local entities for PC-PC Unregistration
func (unregisterPcSvc *UnregisterPCImpl) UnconfigureEntities(job *models.Job) error {
	log.Infof("[%s] Unconfiguring Remote entities PC-PC Registration", job.JobMetadata.ContextId)
	if !job.JobMetadata.IsLocalOnly {
		// Calling Unconfigure connection on the remote pc
		taskId, guruErr := callUnconfigureConnectionApi(job)
		if guruErr != nil {
			log.Errorf("[%s] Failure while calling unconfigure connection remote api: %s", job.JobMetadata.ContextId, guruErr)
			job.JobMetadata.ErrorDetails = guruErr.GetTaskErrorDetails()
			return guruErr
		}
		guruErr = pollTask(taskId, job.JobMetadata.RemoteAddress)
		if guruErr != nil {
			job.JobMetadata.ErrorDetails = guruErr.GetTaskErrorDetails()
			return guruErr
		}
	}

	// Update the affected entities in the task
	err := updateAffectedEntitiesInTask(job)
	if err != nil {
		log.Warnf("[%s] Failure while updating entities in task: %s", job.JobMetadata.ContextId, err)
	}

	// Unconfigure the connection on the local pc
	err = unregisterPcSvc.ConfigureConnection.Unconfigure(job)
	if err != nil {
		log.Errorf("[%s] Failure while unconfiguring local entities: %s", job.JobMetadata.ContextId, err)
		guruErr := guru_error.GetInternalError(consts.UnregisterPCOpName)
		job.JobMetadata.ErrorDetails = guruErr.GetTaskErrorDetails()
		return guruErr
	}

	log.Infof("[%s] Successfully unconfigured entities for PC-PC Unregistration : %s",
		job.JobMetadata.ContextId, job.JobMetadata.RemoteClusterId)
	return nil
}

// Remove remote and local trust for PC-PC Unregistration
func (unregisterPcSvc *UnregisterPCImpl) RemoveTrust(job *models.Job) error {
	if !job.JobMetadata.IsLocalOnly {
		// Delete the root certificate from the remote pc, can't poll the task after remote certificate deletion,
		// hence it's best effort
		_, guruErr := callRemoveRootCertificateAPI(job)
		if guruErr != nil {
			job.JobMetadata.ErrorDetails = guruErr.GetTaskErrorDetails()
			log.Errorf("[%s] Failure while calling remove root certificate api: %s", job.JobMetadata.ContextId, guruErr)
			return guruErr
		}
	}

	log.Infof("[%s] Removing trust entities for PC-PC unregistration", job.JobMetadata.ContextId)
	// Delete the root certificate from the local pc
	guruErr := exchange_root_certificate.DeleteRootCert(job.JobMetadata.RemoteClusterId)
	if guruErr != nil {
		log.Errorf("[%s] Failure while deleting root certificate: %s", job.JobMetadata.ContextId, guruErr)
	}
	log.Infof("[%s] Successfully unconfigured local entities PC-PC unregistration", job.JobMetadata.ContextId)
	return nil
}

// Executes and performs certain post registration steps as part of PC-PC Unregistration
func (unregisterPcSvc *UnregisterPCImpl) RunPostActions(job *models.Job) error {
	// Update cluster list (with registered PE's cluster uuid) in ergon task as
	// certificate chain is updated due to unregister PC workflow
	registeredClusterUuids, err := utils.FetchRegisteredClusterUuids()
	if err != nil {
		log.Warnf("[%s] Failed to fetch registered cluster uuids: %s", job.JobMetadata.ContextId, err)
	} else {
		utils.UpdateClusterListInTask(job, registeredClusterUuids)
	}

	// Broadcast delete cert on Registered PEs
	xchgcert.BroadcastCertRemove(job.JobMetadata.RemoteClusterId)
	log.Debugf("[%s] Marking the PC-PC Unregistration task in Succeeded state", job.JobMetadata.ContextId)
	return nil
}
