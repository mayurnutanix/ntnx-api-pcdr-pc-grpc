/*
 * Copyright (c) 2024 Nutanix Inc. All rights reserved.
 *
 * Author: deepanshu.jain@nutanix.com
 *
 * This file defines structure and interface for Configure Connection state transition service which is designated to undertake
 *  state toggle for a Configure Connection request. This interface embeds base state transition interface hence must implement
 *  all the methods of base interface. The exposed methods are consumed by the queue service to fulfil a queue item's job.
 *  Methods ExecuteForward() and ExecuteRollback() represent the segregation between ongoing request and a rollback request.
 */

package register

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
	"google.golang.org/protobuf/proto"
)

type ConfigureConnectionWorkflow struct {
	connCfgSvc configure_connection.ConnectionConfig
}

type ConfigureConnectionStateTransitionIfc interface {
	base.Workflow
}

func NewConfigureConnectionTransitionSvc(remoteClusterType management.ClusterType) ConfigureConnectionStateTransitionIfc {
	connectionCfgSvc := configure_connection.New(remoteClusterType)
	return &ConfigureConnectionWorkflow{
		connCfgSvc: connectionCfgSvc,
	}
}

func (state *ConfigureConnectionWorkflow) Execute(stepsCount int, jobItem *models.Job) error {
	if jobItem.JobMetadata.Rollback {
		log.Infof("[%s] %s Rolling back the job %+v : Step %v", jobItem.JobMetadata.ContextId, consts.RegisterPCLoggerPrefix, jobItem.JobMetadata.Name, stepsCount)
		return state.ExecuteRollback(stepsCount, jobItem)
	} else {
		log.Infof("[%s] %s Executing %+v job in forward mode %v", jobItem.JobMetadata.ContextId, consts.RegisterPCLoggerPrefix, jobItem.JobMetadata.Name, stepsCount)
		return state.ExecuteForward(stepsCount, jobItem)
	}
}

func (state *ConfigureConnectionWorkflow) ExecuteForward(stepsCount int, jobItem *models.Job) error {
	switch stepsCount {
	case 0:
		utils.UpdateJobTask(jobItem, ergon.Task_kRunning.Enum(), nil, "")
		log.Debugf("[%s] %s Configure connection job - forward executed step %v",
			jobItem.JobMetadata.ContextId, consts.RegisterPCLoggerPrefix, stepsCount)
		err := state.connCfgSvc.RunPreConfigureActions(jobItem)
		if err != nil {
			utils.UpdateJobTaskStatusOnError(jobItem, err, "Failed to configure connection")
			log.Errorf("[%s] Failed to execute configure connection connect cluster %s", jobItem.JobMetadata.ContextId, err)
			return err
		}
		jobUtils.PostStepSuccess(jobItem, fmt.Sprintf("Executed configure-connection prechecks for cluster uuid %s successfully",
			jobItem.JobMetadata.RemoteClusterId))
		return nil
	case 1:
		log.Debugf("[%s] %s Configure connection job - forward executed step %v",
			jobItem.JobMetadata.ContextId, consts.RegisterPCLoggerPrefix, stepsCount)
		err := state.connCfgSvc.Configure(jobItem)
		if err != nil {
			utils.UpdateJobTaskStatusOnError(jobItem, err, "Failed to configure connection")
			log.Errorf("[%s] Failed to execute configure connection connect cluster %s",
				jobItem.JobMetadata.ContextId, err)
			return err
		}

		// TODO: @deepanshu-nutanix Remove once Get Domain Manager API is available
		// set remote pc name from task completion details
		completionDetails := make([]*ergon.KeyValue, 0)
		zeusConfig, err := utils.FetchZeusConfig()
		if err == nil {
			completionDetails = append(completionDetails, &ergon.KeyValue{
				Key: proto.String(consts.PcNameKey),
				Value: &ergon.KeyValue_StrData{
					StrData: zeusConfig.GetClusterName(),
				},
			})
		}
		jobItem.JobMetadata.StepsCompleted += 1
		utils.UpdateTask(jobItem.ParentTask.GetUuid(),
			ergon.Task_kSucceeded, "Successfully completed configure connection", completionDetails)
		log.Infof("[%s] %s Configure connection job finished.", jobItem.JobMetadata.ContextId, consts.RegisterPCLoggerPrefix)
		return nil
	default:
		return nil
	}
}

func (state *ConfigureConnectionWorkflow) ExecuteRollback(stepsCount int, jobItem *models.Job) error {
	jobMetadata := jobItem.JobMetadata

	log.Infof("[%s] %s Executing configure connection in rollack - executed step %d", jobMetadata.ContextId,
		consts.RegisterPCLoggerPrefix, stepsCount)
	switch stepsCount {
	case 0:
		err := state.connCfgSvc.Unconfigure(jobItem)
		if err != nil {
			log.Errorf("[%s] %s Execute rollback  of configure connection cluster %s",
				jobItem.JobMetadata.ContextId, consts.RegisterPCLoggerPrefix, err)
			return err
		}
		log.Infof("[%s] %s configure connection rollack finished successfully.",
			jobItem.JobMetadata.ContextId, consts.RegisterPCLoggerPrefix)
		jobItem.JobMetadata.StepsCompleted -= 1
		return nil
	default:
		return nil
	}
}

func (state *ConfigureConnectionWorkflow) AbortIfInvalidRequest(item *models.Job) {
	// TODO: Implement as suits the need.
}

func (state *ConfigureConnectionWorkflow) IsTerminalForward(currentStep int64) bool {
	return currentStep == consts.ConfigureConnectionStepsCount
}

func (state *ConfigureConnectionWorkflow) IsTerminalRollback(currentStep int64, item *models.Job) bool {
	return item.JobMetadata.Rollback && currentStep == -1
}
