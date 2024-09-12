/*
 * Copyright (c) 2024 Nutanix Inc. All rights reserved.
 * Author: deepanshu.jain@nutanix.com
 *
 * This file contains the RegisterPcInterface interface which can be implemented by any cluster type eg. MULTI_CLUSTER, WITNESS_VM etc.
 */

package register_pc

import (
	"ntnx-api-guru-pc/generated-code/target/dto/models/prism/v4/management"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/services/register"
	"ntnx-api-guru-pc/guru-pc-api-service/services/register/configure_connection"
	"ntnx-api-guru-pc/guru-pc-api-service/services/trust_setup/exchange_root_certificate"
	"ntnx-api-guru-pc/guru-pc-api-service/utils"
	pc_utils "ntnx-api-guru-pc/guru-pc-api-service/utils"

	log "github.com/sirupsen/logrus"
)

type RegisterPcImpl struct {
	ConfigureConnection configure_connection.ConnectionConfig
	Precheck            register.Precheck
}

func NewRegisterPcImpl() register.Register {
	return &RegisterPcImpl{
		ConfigureConnection: configure_connection.New(management.CLUSTERTYPE_DOMAIN_MANAGER),
		Precheck:            &RegisterPCPrecheckImp{},
	}
}

// Executes and performs certain prechecks as part of PC-PC Registration
func (registerPcSvc *RegisterPcImpl) RunPreActions(job *models.Job) error {
	log.Infof("[%s] Performing PC-PC Registration Pre Actions", job.JobMetadata.ContextId)
	err := registerPcSvc.Precheck.Execute(job)
	if err != nil {
		job.JobMetadata.ErrorDetails = err.GetTaskErrorDetails()
		return err
	}
	log.Infof("[%s] PC-PC Registration Prechecks completed successfully.", job.JobMetadata.ContextId)
	return nil
}

// SetupTrust establishes trust between two PCs by exchanging their root certificates
// Call AddRootCertificate API on remote PC
// Poll the task to completion
// Save the remote PC root certificate
func (registerPcSvc *RegisterPcImpl) SetupTrust(job *models.Job) error {
	log.Infof("[%s] Setting up PC-PC trust for registration", job.JobMetadata.ContextId)
	rootCert, err := pc_utils.ReadFile(consts.RootCertPath)
	if err != nil {
		log.Errorf("[%s] Failure while fetching local pc root cert: %s", job.JobMetadata.ContextId, err)
		job.JobMetadata.ErrorDetails = guru_error.GetInternalError(consts.RegisterPCOpName).GetTaskErrorDetails()
		return err
	}

	taskId, guruErr := callAddRootCertificateApi(rootCert, job)
	if guruErr != nil {
		job.JobMetadata.ErrorDetails = guruErr.GetTaskErrorDetails()
		return guruErr
	}
	log.Infof("[%s] Successfully created add root cert task: %s", job.JobMetadata.ContextId, taskId)
	guruErr = pollTask(taskId, job.JobMetadata.RemoteAddress)
	if guruErr != nil {
		job.JobMetadata.ErrorDetails = guruErr.GetTaskErrorDetails()
		return guruErr
	}
	details, guruErr := pc_utils.GetRemoteTaskCompletionDetails(taskId, job.JobMetadata.RemoteAddress, consts.RegisterPCOpName)
	if guruErr != nil {
		job.JobMetadata.ErrorDetails = guruErr.GetTaskErrorDetails()
		return guruErr
	}

	for _, detail := range details {
		if detail.Key == consts.RootCert {
			rootCertificate := detail.Value
			log.Infof("[%s] Saving remote pc root cert", job.JobMetadata.ContextId)
			err = exchange_root_certificate.SaveRootCert(job.JobMetadata.RemoteClusterId, &rootCertificate)
			if err != nil {
				log.Errorf("[%s] Failure while saving remote pc root cert: %s", job.JobMetadata.ContextId, err)
				guruErr := guru_error.GetInternalError(consts.RegisterPCOpName)
				job.JobMetadata.ErrorDetails = guruErr.GetTaskErrorDetails()
				return guruErr
			}
			break
		}
	}
	log.Infof("[%s] successfully setup trust for pc-pc registration", job.JobMetadata.ContextId)
	return nil
}

func (registerPcSvc *RegisterPcImpl) ConfigureEntities(job *models.Job) error {
	log.Infof("[%s] Configuring PC-PC Registration", job.JobMetadata.ContextId)
	// Call configure connection API on remote pc
	taskId, guruErr := callConfigureConnectionApi(job.JobMetadata)
	if guruErr != nil {
		log.Errorf("[%s] Failure while calling configure connection api on remote pc: %s", job.JobMetadata.ContextId, guruErr)
		job.JobMetadata.ErrorDetails = guruErr.GetTaskErrorDetails()
		return guruErr
	}

	guruErr = pollTask(taskId, job.JobMetadata.RemoteAddress)
	if guruErr != nil {
		log.Errorf("[%s] Failure while polling configure connection task: %s", job.JobMetadata.ContextId, guruErr)
		job.JobMetadata.ErrorDetails = guruErr.GetTaskErrorDetails()
		return guruErr
	}

	// todo: there is an extra task call being made here. we can remove that
	details, guruErr := pc_utils.GetRemoteTaskCompletionDetails(taskId, job.JobMetadata.RemoteAddress, consts.RegisterPCOpName)
	if guruErr != nil {
		log.Errorf("[%s] Failure while getting remote task completion details: %s", job.JobMetadata.ContextId, guruErr)
		job.JobMetadata.ErrorDetails = guruErr.GetTaskErrorDetails()
		return guruErr
	}

	// TODO: @deepanshu-nutanix Remove once Get Domain Manager API is available
	// Extract remote pc name from task completion details
	for _, detail := range details {
		if detail.Key == consts.PcNameKey {
			job.JobMetadata.RemoteClusterName = detail.Value
			break
		}
	}

	err := updateAffectedEntitiesInTask(job)
	if err != nil {
		log.Warnf("[%s] Failure while updating entities in task: %s", job.JobMetadata.ContextId, err)
	}

	log.Debugf("[%s] Configuring PC Registration Connection locally", job.JobMetadata.ContextId)
	// Configuring connection on local pc after remote pc is configured
	err = registerPcSvc.ConfigureConnection.Configure(job)
	if err != nil {
		log.Errorf("[%s] Failure while configuring local entities: %s", job.JobMetadata.ContextId, err)
		guruErr := guru_error.GetInternalError(consts.RegisterPCOpName)
		job.JobMetadata.ErrorDetails = guruErr.GetTaskErrorDetails()
		return guruErr
	}

	log.Infof("[%s] Done configuring entities for PC-PC registration", job.JobMetadata.ContextId)
	return nil
}

// Call PE Cert Broadcast Method. Mark the task as successful
func (registerPcSvc *RegisterPcImpl) RunPostActions(job *models.Job) error {
	// Update cluster list (with registered PE's cluster uuid) in ergon task as
	// certificate chain is updated due to register PC workflow
	registeredClusterUuids, err := utils.FetchRegisteredClusterUuids()
	if err != nil {
		log.Warnf("[%s] Failed to fetch registered cluster uuids: %s", job.JobMetadata.ContextId, err)
	} else {
		utils.UpdateClusterListInTask(job, registeredClusterUuids)
	}
	exchange_root_certificate.BroadcastCert(job.JobMetadata.RemoteClusterId)
	return nil
}
