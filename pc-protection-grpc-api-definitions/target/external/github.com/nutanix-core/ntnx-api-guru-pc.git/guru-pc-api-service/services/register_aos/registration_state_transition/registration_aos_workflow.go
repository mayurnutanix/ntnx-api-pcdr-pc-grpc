/*
* Copyright (c) 2024 Nutanix Inc. All rights reserved.
*
* Author: kshitij.kumar@nutanix.com
*
* This file defines structure and interface for Connect PE state transition service which is designated to undertake
* state toggle for a Connect PE (Registration) request. This interface embeds base state transition interface hence
* must implement all the methods of base interface. The exposed methods are consumed by the queue service to fulfil a
* queue item's job. Methods ExecuteForward() and ExecuteRollback() represent the segregation between ongoing request and
* a rollback request.
*
* This state transition interface consumes three separate interfaces -
* 1. registration_common_util : abstracts basic implementations needed for a registration request.
* 2. registration_precheck: abstracts implementation for different prechecks - local and remote.
* 3. registration_csr : abstracts implementation for trust domain federation between PC and PE.
* 4. registration_proxy : abstracts implementation for proxying and polling the request to PE.
*
 */

package registration_state_transition

import (
	"fmt"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/external"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/local"
	"ntnx-api-guru-pc/guru-pc-api-service/job_interfaces/base"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/services/register_aos/registration_common"
	"ntnx-api-guru-pc/guru-pc-api-service/services/register_aos/registration_precheck"
	"ntnx-api-guru-pc/guru-pc-api-service/services/register_aos/registration_proxy"
	"ntnx-api-guru-pc/guru-pc-api-service/services/trust_setup/cert_signing_trust_setup"
	pc_utils "ntnx-api-guru-pc/guru-pc-api-service/utils"

	"github.com/nutanix-core/ntnx-api-guru/services/utils"

	"github.com/nutanix-core/go-cache/ergon"
	"github.com/nutanix-core/go-cache/util-go/uuid4"
	common_consts "github.com/nutanix-core/ntnx-api-guru/services/constants"
	log "github.com/sirupsen/logrus"
)

type RegisterAOSWorkflow struct {
	registrationPrecheck   registration_precheck.RegistrationPrecheckIfc
	registrationProxy      registration_proxy.RegistrationProxyIfc
	registrationCsr        cert_signing_trust_setup.CertSigningTrustSetupIfc
	registrationCommonUtil registration_common.RegistrationCommonUtilIfc
}

type RegisterStateIfc interface {
	base.Workflow
}

func NewRegisterStateTransitionService() RegisterStateIfc {

	return &RegisterAOSWorkflow{
		registrationPrecheck:   local.GetInternalInterfaces().RegistrationPrecheckIfc(),
		registrationProxy:      local.GetInternalInterfaces().RegistrationProxyIfc(),
		registrationCsr:        local.GetInternalInterfaces().CertSigningTrustSetupIfc(),
		registrationCommonUtil: local.GetInternalInterfaces().RegistrationCommonUtilIfc(),
	}
}

func (registerService *RegisterAOSWorkflow) Execute(stepsCount int, jobItem *models.Job) error {
	if jobItem.JobMetadata.Rollback == false {
		log.Infof("[%s] %s Executing the job in forward mode",
			jobItem.JobMetadata.ContextId,
			common_consts.REGISTRATION_LOGGER_PREFIX)
		return registerService.ExecuteForward(stepsCount, jobItem)
	} else {
		log.Infof("[%s] %s Executing the job in rollback mode",
			jobItem.JobMetadata.ContextId,
			common_consts.REGISTRATION_LOGGER_PREFIX)
		return registerService.ExecuteRollback(stepsCount, jobItem)
	}
}

func (registerService *RegisterAOSWorkflow) ExecuteRollback(stepsCount int, item *models.Job) error {

	//TODO: map registration forward step to rollback steps
	if stepsCount == 3 {
		log.Infof("[%s] %s Deleting CES and failing cluster registration task ",
			item.JobMetadata.ContextId, common_consts.REGISTRATION_LOGGER_PREFIX)
		registerService.registrationProxy.Rollback(item)
		stepsCount -= 1
		item.JobMetadata.StepsCompleted = int64(stepsCount)

		pc_utils.UpdateInternalOpaqueOfParentTask(consts.DeleteCESCompleteMessage, item)
	}

	if stepsCount == 2 {
		log.Infof("[%s] %s Resetting CA chain",
			item.JobMetadata.ContextId, common_consts.REGISTRATION_LOGGER_PREFIX)
		registerService.registrationCsr.Rollback(item)
		stepsCount -= 1
		item.JobMetadata.StepsCompleted = int64(stepsCount)

		pc_utils.UpdateInternalOpaqueOfParentTask(consts.TrustSetupResetCompleteMessage, item)

	}

	if stepsCount == 1 || stepsCount == 0 {
		log.Infof("[%s] %s Failing parent task and cleanup zk marker",
			item.JobMetadata.ContextId, common_consts.REGISTRATION_LOGGER_PREFIX)

		stepsCount = -1
		item.JobMetadata.StepsCompleted = int64(stepsCount)
		registerService.registrationCommonUtil.DeleteRegistrationMarkerNode(item)

		pc_utils.UpdateInternalOpaqueOfParentTask(consts.DeleteZkMarkerCompleteMessage, item)
	}

	// Update registration task.
	err := utils.FailTaskWithError(external.Interfaces().ErgonService(),
		item.JobMetadata.ParentTaskId, item.JobMetadata.ErrorDetails)
	if err != nil {
		log.Errorf("[%s] %s Failed to update ergon task %s with error: %s",
			item.JobMetadata.ContextId, common_consts.REGISTRATION_LOGGER_PREFIX,
			uuid4.ToUuid4(item.JobMetadata.ParentTaskId).String(), err)

		return consts.ErrorRegistrationTaskExecution
	}

	log.Infof("[%s] %s Rollback of parent task %s successfully completed",
		item.JobMetadata.ContextId, common_consts.REGISTRATION_LOGGER_PREFIX,
		uuid4.ToUuid4(item.JobMetadata.ParentTaskId).String())

	return nil
}

func (registerService *RegisterAOSWorkflow) AbortIfInvalidRequest(item *models.Job) {
	if item.JobMetadata.StepsCompleted < 2 {
		external.Interfaces().ErgonService().UpdateTask(item.JobMetadata.ParentTaskId, ergon.Task_kAborted,
			consts.RegistrationRecoveryFailureTaskAbortMessage, nil, nil, nil, nil)
	}
}

// TODO : return terminal state (curr) - boolean if terminal reached or not.
func (registerService *RegisterAOSWorkflow) IsTerminalForward(currentStep int64) bool {
	return currentStep == consts.RegisterAOSStepsCount
}

func (registerService *RegisterAOSWorkflow) IsTerminalRollback(currentStep int64, item *models.Job) bool {
	return item.JobMetadata.Rollback && currentStep == consts.RegisterAOSRollbackStepsCount
}

func (registerService *RegisterAOSWorkflow) ExecuteForward(stepsCount int, jobItem *models.Job) error {
	switch stepsCount {

	case 0:
		// registerService.AbortIfInvalidRequest(jobItem)
		log.Infof("[%s] %s Attempting to invoke child task creation",
			jobItem.JobMetadata.ContextId,
			common_consts.REGISTRATION_LOGGER_PREFIX)
		err := registerService.registrationCommonUtil.CreateChildTasks(jobItem)
		if err != nil {
			registerService.registrationCommonUtil.ProcessRegistrationErrorState(jobItem,
				fmt.Sprintf("%s with error %s", consts.RegisterClusterStateErrors[stepsCount], err.Error()), stepsCount)
			return err
		}

		return err
	case 1:
		log.Infof("[%s] %s Attempting to execute registration prechecks",
			jobItem.JobMetadata.ContextId,
			common_consts.REGISTRATION_LOGGER_PREFIX)
		err := registerService.registrationPrecheck.Execute(jobItem)
		if err != nil {
			registerService.registrationCommonUtil.ProcessRegistrationErrorState(jobItem,
				fmt.Sprintf("%s with error %s", consts.RegisterClusterStateErrors[stepsCount], err.Error()), stepsCount)
			return err
		}

		return err
	case 2:
		log.Infof("[%s] %s Attempting to execute certificate signing request task",
			jobItem.JobMetadata.ContextId,
			common_consts.REGISTRATION_LOGGER_PREFIX)
		err := registerService.registrationCsr.Execute(jobItem)
		if err != nil {
			registerService.registrationCommonUtil.ProcessRegistrationErrorState(jobItem,
				fmt.Sprintf("%s with error %s", consts.RegisterClusterStateErrors[stepsCount], err.Error()), stepsCount)
			return err
		}

		return err
	case 3:
		log.Infof("[%s] %s Attempting to execute registration proxy task",
			jobItem.JobMetadata.ContextId,
			common_consts.REGISTRATION_LOGGER_PREFIX)
		err := registerService.registrationProxy.Execute(jobItem)
		if err != nil {
			registerService.registrationCommonUtil.ProcessRegistrationErrorState(jobItem,
				fmt.Sprintf("%s with error %s", consts.RegisterClusterStateErrors[stepsCount], err.Error()), stepsCount)
			return err
		}

		return err
	default:
		return nil
	}
}
