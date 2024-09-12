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

package unregister

import (
	"fmt"

	"ntnx-api-guru-pc/generated-code/target/dto/models/prism/v4/management"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/local"
	"ntnx-api-guru-pc/guru-pc-api-service/job_interfaces/base"
	jobInterfaceUtils "ntnx-api-guru-pc/guru-pc-api-service/job_interfaces/utils"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/services/register"
	"ntnx-api-guru-pc/guru-pc-api-service/utils"

	"github.com/nutanix-core/go-cache/ergon"
	log "github.com/sirupsen/logrus"
)

type UnregisterWorkflow struct {
	unregisterSvc register.Unregister
}

type UnregisterJobStateTransitionIfc interface {
	base.Workflow
}

func NewUnregisterStateTransitionSvc(remoteClusterType management.ClusterType) UnregisterJobStateTransitionIfc {
	unregisterSvc := local.GetInternalInterfaces().UnregisterSvc(remoteClusterType)
	return &UnregisterWorkflow{unregisterSvc}
}

func (state *UnregisterWorkflow) Execute(stepsCount int, jobItem *models.Job) error {
	if !jobItem.JobMetadata.Rollback {
		return state.ExecuteForward(stepsCount, jobItem)
	} else {
		return state.ExecuteRollback(stepsCount, jobItem)
	}
}

func (state *UnregisterWorkflow) ExecuteForward(stepsCount int, jobItem *models.Job) error {
	log.Infof("[%s] Executing unregister job in forward mode, step %d", jobItem.JobMetadata.ContextId, stepsCount)
	switch stepsCount {
	case 0:
		// Precheck steps
		utils.UpdateJobTask(jobItem, ergon.Task_kRunning.Enum(), nil, "Unregistering cluster started")
		err := state.unregisterSvc.RunPreActions(jobItem)
		if err != nil {
			jobInterfaceUtils.PostJobFailure(jobItem, err, "Failed to execute Pre unregistration steps")
			return err
		}
		jobInterfaceUtils.PostStepSuccess(jobItem, "Precheck completed successfully")
		return nil
	case 1:
		// Unconfigure Entities
		err := state.unregisterSvc.UnconfigureEntities(jobItem)
		if err != nil {
			jobInterfaceUtils.PostJobFailure(jobItem, err, "Failed to unconfigure entities")
			return err
		}
		jobInterfaceUtils.PostStepSuccess(jobItem, "Successfully unconfigured entities")
	case 2:
		// Remove trust
		err := state.unregisterSvc.RemoveTrust(jobItem)
		if err != nil {
			jobInterfaceUtils.PostJobFailure(jobItem, err, "Failed to revoke trust")
			return err
		}
		jobInterfaceUtils.PostStepSuccess(jobItem, "Successfully revoked trust")
	case 3:
		// Postcheck steps
		err := state.unregisterSvc.RunPostActions(jobItem)
		if err != nil {
			jobInterfaceUtils.PostJobFailure(jobItem, err, "Unregistering cluster postcheck failed")
			return err
		}
		jobInterfaceUtils.PostJobSuccess(jobItem, fmt.Sprintf("Unregister cluster %s completed successfully", jobItem.JobMetadata.RemoteClusterId))
	}
	return nil
}

func (state *UnregisterWorkflow) ExecuteRollback(stepsCount int, jobItem *models.Job) error {
	return nil
}

func (state *UnregisterWorkflow) IsTerminalForward(currentStep int64) bool {
	return currentStep == consts.UnregisterStepsCount
}

func (state *UnregisterWorkflow) IsTerminalRollback(currentStep int64, item *models.Job) bool {
	// No rollback steps for unregister
	return item.JobMetadata.Rollback
}
