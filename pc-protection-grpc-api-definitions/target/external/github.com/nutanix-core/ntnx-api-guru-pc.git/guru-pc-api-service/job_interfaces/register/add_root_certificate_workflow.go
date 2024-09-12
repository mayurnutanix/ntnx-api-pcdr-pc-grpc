/*
 * Copyright (c) 2024 Nutanix Inc. All rights reserved.
 *
 * Author: deepanshu.jain@nutanix.com
 *
 * Job state transition service for Add Root Cert Job
 */

package register

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

type AddRootCertJobStateService struct {
}

func NewAddRootCertJobStateService() base.Workflow {
	return &AddRootCertJobStateService{}
}

func (state *AddRootCertJobStateService) Execute(stepsCount int, jobItem *models.Job) error {
	if jobItem.JobMetadata.Rollback {
		log.Infof("[%s] Rolling back the job %s : Step %v", jobItem.JobMetadata.ContextId, consts.OperationNameMap[jobItem.JobMetadata.Name], stepsCount)
		return nil
	} else {
		log.Infof("[%s] Executing %s job in forward mode %v", jobItem.JobMetadata.ContextId, consts.OperationNameMap[jobItem.JobMetadata.Name], stepsCount)
		return state.ExecuteForward(stepsCount, jobItem)
	}
}

func (state *AddRootCertJobStateService) ExecuteForward(stepsCount int, jobItem *models.Job) error {
	log.Infof("[%s] Executing %s job in forward mode, step %d", consts.OperationNameMap[jobItem.JobMetadata.Name], jobItem.JobMetadata.ContextId, stepsCount)
	switch stepsCount {
	case 0:
		utils.UpdateJobTask(jobItem, ergon.Task_kRunning.Enum(), nil, "")
		err := exchange_root_certificate.ProcessAddRootCertRequest(jobItem.ParentTask.Uuid, jobItem.JobMetadata.RootCert, jobItem.JobMetadata.RemoteClusterId)
		if err != nil {
			jobUtils.PostJobFailure(jobItem, err, fmt.Sprintf("Failed to add root certificate for cluster %s.", jobItem.JobMetadata.RemoteClusterId))
			return err
		}
		jobUtils.PostJobSuccess(jobItem, fmt.Sprintf("Add root certificate for cluster %s completed successfully", jobItem.JobMetadata.RemoteClusterId))
	default:
		return nil
	}
	return nil
}

func (state *AddRootCertJobStateService) IsTerminalForward(currentStep int64) bool {
	return currentStep == consts.AddRootCertStepsCount
}

func (state *AddRootCertJobStateService) IsTerminalRollback(currentStep int64, item *models.Job) bool {
	return item.JobMetadata.Rollback
}
