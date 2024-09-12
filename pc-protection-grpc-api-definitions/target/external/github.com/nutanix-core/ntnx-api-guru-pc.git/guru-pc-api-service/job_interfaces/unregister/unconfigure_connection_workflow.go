/*
 * Copyright (c) 2024 Nutanix Inc. All rights reserved.
 *
 * Author: deepanshu.jain@nutanix.com
 *
 * This file defines structure and interface for Unconfigure Connection state transition service which is designated to undertake
 *  state toggle for unconfiguring an existing connection. This interface embeds base state transition interface hence must implement
 *  all the methods of base interface. The exposed methods are consumed by the queue service to fulfil a queue item's job.
 *  Methods ExecuteForward() and ExecuteRollback() represent the segregation between ongoing request and a rollback request.
 */

package unregister

import (
	"fmt"

	"ntnx-api-guru-pc/generated-code/target/dto/models/prism/v4/management"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/job_interfaces/base"
	jobUtils "ntnx-api-guru-pc/guru-pc-api-service/job_interfaces/utils"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/services/register/configure_connection"
	"ntnx-api-guru-pc/guru-pc-api-service/utils"

	"github.com/nutanix-core/go-cache/ergon"
	log "github.com/sirupsen/logrus"
)

type UnconfigureConnectionWorkflow struct {
	connCfgSvc configure_connection.ConnectionConfig
}

func NewUnconfigureConnectionTransitionSvc(remoteClusterType management.ClusterType) base.Workflow {
	connCfgSvc := configure_connection.New(remoteClusterType)
	return &UnconfigureConnectionWorkflow{
		connCfgSvc: connCfgSvc,
	}
}

func (state *UnconfigureConnectionWorkflow) Execute(stepsCount int, jobItem *models.Job) error {
	if jobItem.JobMetadata.Rollback {
		log.Debugf("[%s] %s Rolling back the job %+v : Step %v", jobItem.JobMetadata.ContextId, consts.RegisterPCLoggerPrefix, jobItem.JobMetadata.Name, stepsCount)
		return state.ExecuteRollback(stepsCount, jobItem)
	} else {
		log.Debugf("[%s] %s Executing %+v job in forward mode %v", jobItem.JobMetadata.ContextId, consts.RegisterPCLoggerPrefix, jobItem.JobMetadata.Name, stepsCount)
		return state.ExecuteForward(stepsCount, jobItem)
	}
}

func (state *UnconfigureConnectionWorkflow) ExecuteForward(stepsCount int, jobItem *models.Job) error {
	switch stepsCount {
	case 0:
		// Updating task status to KRunning
		log.Debugf("[%s] %s Unconfigure connection job - forward executed step %v",
			jobItem.JobMetadata.ContextId, consts.RegisterPCLoggerPrefix, stepsCount)
		utils.UpdateJobTask(jobItem, ergon.Task_kRunning.Enum(), nil, "Unconfigure connection started")
		err := state.connCfgSvc.RunPreUnconfigureActions(jobItem)
		if err != nil {
			jobUtils.PostJobFailure(jobItem, err, fmt.Sprintf("Failed to execute prechecks for unconfigure-connection for cluster uuid %s",
				jobItem.JobMetadata.RemoteClusterId))
			return err
		}
		jobUtils.PostStepSuccess(jobItem, fmt.Sprintf("Executed unconfigure-connection prechecks for cluster uuid %s successfully",
			jobItem.JobMetadata.RemoteClusterId))
		return nil
	case 1:
		// Updating task status to KRunning
		log.Debugf("[%s] %s Unconfigure connection job - forward executed step %v",
			jobItem.JobMetadata.ContextId, consts.RegisterPCLoggerPrefix, stepsCount)
		utils.UpdateJobTask(jobItem, ergon.Task_kRunning.Enum(), nil, "Unconfigure connection started")
		err := state.connCfgSvc.Unconfigure(jobItem)
		if err != nil {
			jobUtils.PostJobFailure(jobItem, err, fmt.Sprintf("Failed to unconfigure connection for cluster uuid %s",
				jobItem.JobMetadata.RemoteClusterId))
			return err
		}
		jobUtils.PostJobSuccess(jobItem, fmt.Sprintf("Executed unconfigure connection for cluster uuid %s successfully",
			jobItem.JobMetadata.RemoteClusterId))
		return nil
	}
	return nil
}

func (state *UnconfigureConnectionWorkflow) ExecuteRollback(stepsCount int, jobItem *models.Job) error {
	jobItem.JobMetadata.StepsCompleted -= 1
	return nil
}

func (state *UnconfigureConnectionWorkflow) IsTerminalForward(currentStep int64) bool {
	return currentStep == consts.ConfigureConnectionStepsCount
}

func (state *UnconfigureConnectionWorkflow) IsTerminalRollback(currentStep int64, item *models.Job) bool {
	return item.JobMetadata.Rollback
}
