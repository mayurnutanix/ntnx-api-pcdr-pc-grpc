/*
 * Copyright (c) 2024 Nutanix Inc. All rights reserved.
 *
 * Author: deepanshu.jain@nutanix.com
 *
 * Job state transition service for Remove Root Cert Job
 */

package unregister

import (
	"fmt"

	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/job_interfaces/base"
	jobUtils "ntnx-api-guru-pc/guru-pc-api-service/job_interfaces/utils"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/services/trust_setup/exchange_root_certificate"
	"ntnx-api-guru-pc/guru-pc-api-service/utils"

	"github.com/nutanix-core/go-cache/ergon"
	log "github.com/sirupsen/logrus"
)

type RemoveRootCertJobStateService struct {
}

func NewRemoveRootCertJobStateService() base.Workflow {
	return &RemoveRootCertJobStateService{}
}

func (state *RemoveRootCertJobStateService) Execute(stepsCount int, jobItem *models.Job) error {
	if jobItem.JobMetadata.Rollback {
		log.Infof("[%s] Rolling back the job %+v : Step %v", jobItem.JobMetadata.ContextId, jobItem.JobMetadata.Name, stepsCount)
		return nil
	} else {
		log.Infof("[%s] Executing %+v job in forward mode %v", jobItem.JobMetadata.ContextId, jobItem.JobMetadata.Name, stepsCount)
		return state.ExecuteForward(stepsCount, jobItem)
	}
}

func (state *RemoveRootCertJobStateService) ExecuteForward(stepsCount int, jobItem *models.Job) error {
	log.Infof("[%s] Executing %s job in forward mode, step %d", consts.OperationNameMap[jobItem.JobMetadata.Name], jobItem.JobMetadata.ContextId, stepsCount)
	switch stepsCount {
	case 0:
		utils.UpdateJobTask(jobItem, ergon.Task_kRunning.Enum(), nil, "Removing root certificate of remote cluster %s started")
		err := exchange_root_certificate.DeleteRootCert(jobItem.JobMetadata.RemoteClusterId)
		if err != nil {
			jobUtils.PostJobFailure(jobItem, err, fmt.Sprintf("Failed to execute remove root certificate for remote cluster %s", jobItem.JobMetadata.RemoteClusterId))
			return err
		}
		jobUtils.PostJobSuccess(jobItem, fmt.Sprintf("Removed certificate of remote cluster %s successfully", jobItem.JobMetadata.RemoteClusterId))
	}
	return nil
}

func (state *RemoveRootCertJobStateService) IsTerminalForward(currentStep int64) bool {
	return currentStep == consts.RemoveRootCertStepsCount
}

func (state *RemoveRootCertJobStateService) IsTerminalRollback(currentStep int64, item *models.Job) bool {
	return item.JobMetadata.Rollback
}
