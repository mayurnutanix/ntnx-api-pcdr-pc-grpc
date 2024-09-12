/*
* Copyright (c) 2024 Nutanix Inc. All rights reserved.
*
* Author: kshitij.kumar@nutanix.com
*
* This file defines structure for Register PC state transition service which is designated to undertake
  state toggle for a Register PC request. The exposed methods are consumed by the queue service to fulfil a queue item's job.
  Methods ExecuteForward() and ExecuteRollback() represent the segregation between ongoing request and a rollback request.
*/

package register

import (
	"ntnx-api-guru-pc/generated-code/target/dto/models/prism/v4/management"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/local"
	"ntnx-api-guru-pc/guru-pc-api-service/job_interfaces/base"
	"ntnx-api-guru-pc/guru-pc-api-service/job_interfaces/utils"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/services/register"
	pc_utils "ntnx-api-guru-pc/guru-pc-api-service/utils"

	"github.com/nutanix-core/go-cache/ergon"
	log "github.com/sirupsen/logrus"
)

type RegisterWorkflow struct {
	registerSvc   register.Register
	unregisterSvc register.Unregister
}

func NewRegisterStateTransitionService(remtoteEntityType management.ClusterType) base.Workflow {
	registerSvc := local.GetInternalInterfaces().RegisterSvc(remtoteEntityType)
	unregisterSvc := local.GetInternalInterfaces().UnregisterSvc(remtoteEntityType)
	return &RegisterWorkflow{
		registerSvc:   registerSvc,
		unregisterSvc: unregisterSvc,
	}
}

func (state *RegisterWorkflow) Execute(stepsCount int, jobItem *models.Job) error {
	if !jobItem.JobMetadata.Rollback {
		return state.ExecuteForward(stepsCount, jobItem)
	} else {
		return state.ExecuteRollback(stepsCount, jobItem)
	}
}

func (state *RegisterWorkflow) ExecuteForward(stepsCount int, jobItem *models.Job) error {
	switch stepsCount {
	case 0:
		// Perform Pre-Registration Steps
		pc_utils.UpdateJobTask(jobItem, ergon.Task_kRunning.Enum(), nil, "Executing prechecks for register operation")
		err := state.registerSvc.RunPreActions(jobItem)
		if err != nil {
			utils.PostJobFailure(jobItem, err, "Failed during pre-checks step")
			return err
		}
		utils.PostStepSuccess(jobItem, "Successfully completed pre-registration steps")
	case 1:
		// Setup Trust
		err := state.registerSvc.SetupTrust(jobItem)
		if err != nil {
			utils.PostJobFailure(jobItem, err, "Failed during setup trust step")
			return err
		}
		utils.PostStepSuccess(jobItem, "Successfully completed setup trust step")
	case 2:
		// Configure Entities
		err := state.registerSvc.ConfigureEntities(jobItem)
		if err != nil {
			utils.PostJobFailure(jobItem, err, "Failed during Configure entities step")
			return err
		}
		utils.PostStepSuccess(jobItem, "Successfully completed configured entities step")
	case 3:
		// Post Registration Steps
		err := state.registerSvc.RunPostActions(jobItem)
		if err != nil {
			utils.PostJobFailure(jobItem, err, "Register operation failed at Post Actions")
			return err
		}
		utils.PostJobSuccess(jobItem, "Successfully completed PC-PC Registration")
	default:
		return nil
	}
	return nil
}

func (state *RegisterWorkflow) ExecuteRollback(stepsCount int, jobItem *models.Job) error {
	switch stepsCount {
	case 0:
		log.Infof("[%s] Register rollback successful", jobItem.JobMetadata.ContextId)
		jobItem.JobMetadata.StepsCompleted -= 1
		return nil
	case 1:
		// Remove trust
		err := state.unregisterSvc.RemoveTrust(jobItem)
		if err != nil {
			log.Errorf("[%s] Register rollback: remove trust: %s", jobItem.JobMetadata.ContextId, err)
			return err
		}
		jobItem.JobMetadata.StepsCompleted -= 1
		return nil
	case 2:
		// Unconfigure Entities
		err := state.unregisterSvc.UnconfigureEntities(jobItem)
		if err != nil {
			log.Errorf("[%s] Register rollback: unconfigure entities: %s", jobItem.JobMetadata.ContextId, err)
			return err
		}
		jobItem.JobMetadata.StepsCompleted -= 1
		return nil
	case 3:
		// Perform Post-Registration Steps
		// state.unregisterSvc.RunPostActions(jobItem)
		jobItem.JobMetadata.StepsCompleted -= 1
		return nil
	default:
		return nil
	}
}

func (state *RegisterWorkflow) AbortIfInvalidRequest(item *models.Job) {
	// TODO: Implement as suits the need.
}

func (state *RegisterWorkflow) IsTerminalForward(currentStep int64) bool {
	return currentStep == consts.RegisterStepsCount
}

func (state *RegisterWorkflow) IsTerminalRollback(currentStep int64, item *models.Job) bool {
	return item.JobMetadata.Rollback && currentStep == -1
}
