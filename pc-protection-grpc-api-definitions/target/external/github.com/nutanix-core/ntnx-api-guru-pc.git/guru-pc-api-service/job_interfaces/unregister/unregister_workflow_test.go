/**
 * @file unregister_workflow_test.go
 * @description
 * @author deepanshu.jain@nutanix.com
 * @copyright Copyright (c) 2024 Nutanix Inc. All rights reserved.
 */

package unregister

import (
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/mocks"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/test"
	"testing"

	"github.com/golang/mock/gomock"
	"github.com/nutanix-core/go-cache/ergon"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
	"google.golang.org/protobuf/proto"
)

func TestUnregisterWorkflowExecute(t *testing.T) {
	// Setup
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	retTask := &ergon.TaskUpdateRet{
		Task: &ergon.Task{
			Uuid:             []byte{1, 2, 3},
			LogicalTimestamp: proto.Int64(1),
		},
	}
	job := models.Job{
		Name: consts.UNREGISTER_PC,
		JobMetadata: &models.JobMetadata{
			Name:          consts.UNREGISTER_PC,
			TaskId:        "task_id",
			SelfClusterId: "self_cluster_id",
		},
		ParentTask: &ergon.Task{
			Uuid:             []byte{1, 2, 3},
			LogicalTimestamp: proto.Int64(0),
		},
	}
	m.ErgonService.On("TaskUpdate", mock.AnythingOfType("*ergon.TaskUpdateArg")).Return(retTask, nil)
	m.ErgonService.On("TaskGet", []byte{1, 2, 3}).Return(retTask.Task, nil)
	m.EventForwarder.EXPECT().PushEvent(
		gomock.Any(), consts.EventTypeUnregister, job.JobMetadata.TaskId,
		consts.EventNamespace, consts.PulseDomainManagerEntity, job.JobMetadata.SelfClusterId)
	mockUnregisterSvc := mocks.NewMockUnregister(ctrl)
	state := &UnregisterWorkflow{mockUnregisterSvc}

	// Setup Step 0 - Precheck
	mockUnregisterSvc.EXPECT().RunPreActions(&job).Return(nil)

	// Test
	err := state.Execute(0, &job)

	// Assert
	assert.Nil(t, err)

	// Setup Step 1 - Unconfigure Entities
	mockUnregisterSvc.EXPECT().UnconfigureEntities(&job).Return(nil)

	// Test
	err = state.Execute(1, &job)

	// Assert
	assert.Nil(t, err)

	// Setup Step 2 - Remove Trust
	mockUnregisterSvc.EXPECT().RemoveTrust(&job).Return(nil)

	// Test Step 2 - Remove Trust
	err = state.Execute(2, &job)

	// Assert
	assert.Nil(t, err)

	// Setup Step 3 - Remove Trust
	mockUnregisterSvc.EXPECT().RunPostActions(&job).Return(nil)

	// Test Step 3 - Post actions
	err = state.Execute(3, &job)

	// Assert
	assert.Nil(t, err)
}

func TestUnregisterWorkflowExecuteError(t *testing.T) {
	// Setup
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	retTask := &ergon.TaskUpdateRet{
		Task: &ergon.Task{
			Uuid:             []byte{1, 2, 3},
			LogicalTimestamp: proto.Int64(1),
		},
	}
	job := models.Job{
		Name: consts.UNREGISTER_PC,
		JobMetadata: &models.JobMetadata{
			Name:          consts.UNREGISTER_PC,
			TaskId:        "task_id",
			SelfClusterId: "self_cluster_id",
		},
		ParentTask: &ergon.Task{
			Uuid:             []byte{1, 2, 3},
			LogicalTimestamp: proto.Int64(0),
		},
	}
	m.ErgonService.On("TaskUpdate", mock.AnythingOfType("*ergon.TaskUpdateArg")).Return(retTask, nil)
	m.ErgonService.On("TaskUpdateBase", mock.AnythingOfType("*ergon.TaskUpdateArg")).Return(retTask, nil)
	m.ErgonService.On("TaskGet", []byte{1, 2, 3}).Return(retTask.Task, nil)
	m.EventForwarder.EXPECT().PushEvent(
		gomock.Any(), consts.EventTypeUnregister, job.JobMetadata.TaskId,
		consts.EventNamespace, consts.PulseDomainManagerEntity, job.JobMetadata.SelfClusterId).Times(4)
	mockUnregisterSvc := mocks.NewMockUnregister(ctrl)
	state := &UnregisterWorkflow{mockUnregisterSvc}

	// Setup Step 0 - Precheck
	mockUnregisterSvc.EXPECT().RunPreActions(&job).Return(assert.AnError)

	// Test
	err := state.Execute(0, &job)

	// Assert
	assert.Error(t, err, assert.AnError)

	// Setup Step 1 - Unconfigure Entities
	mockUnregisterSvc.EXPECT().UnconfigureEntities(&job).Return(assert.AnError)

	// Test
	err = state.Execute(1, &job)

	// Assert
	assert.Error(t, err)

	// Setup Step 2 - Remove Trust
	mockUnregisterSvc.EXPECT().RemoveTrust(&job).Return(assert.AnError)

	// Test Step 2 - Remove Trust
	err = state.Execute(2, &job)

	// Assert
	assert.Error(t, err)

	// Setup Step 3 - Remove Trust
	mockUnregisterSvc.EXPECT().RunPostActions(&job).Return(assert.AnError)

	// Test Step 3 - Post actions
	err = state.Execute(3, &job)

	// Assert
	assert.Error(t, err)
}

func TestUnregisterWorkflowExecuteRollback(t *testing.T) {
	// Setup
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	job := models.Job{
		Name: consts.UNREGISTER_PC,
		JobMetadata: &models.JobMetadata{
			Name:          consts.UNREGISTER_PC,
			TaskId:        "task_id",
			SelfClusterId: "self_cluster_id",
			Rollback:      true,
		},
		ParentTask: &ergon.Task{
			Uuid:             []byte{1, 2, 3},
			LogicalTimestamp: proto.Int64(0),
		},
	}
	mockUnregisterSvc := mocks.NewMockUnregister(ctrl)
	state := &UnregisterWorkflow{mockUnregisterSvc}

	// Test
	err := state.Execute(0, &job)

	// Assert
	assert.Nil(t, err)
}

func TestUnregisterWorkflowIsTerminalForward(t *testing.T) {
	// Setup
	state := &UnregisterWorkflow{}

	// Test
	result := state.IsTerminalForward(3)

	// Assert
	assert.False(t, result)
}

func TestUnregisterWorkflowIsTerminalForwardTrue(t *testing.T) {
	// Setup
	state := &UnregisterWorkflow{}

	// Test
	result := state.IsTerminalForward(consts.UnregisterStepsCount)

	// Assert
	assert.True(t, result)
}

func TestUnregisterWorkflowIsTerminalRollback(t *testing.T) {
	// Setup
	state := &UnregisterWorkflow{}
	job := models.Job{
		Name: consts.UNREGISTER_PC,
		JobMetadata: &models.JobMetadata{
			Name:          consts.UNREGISTER_PC,
			TaskId:        "task_id",
			SelfClusterId: "self_cluster_id",
		},
		ParentTask: &ergon.Task{
			Uuid:             []byte{1, 2, 3},
			LogicalTimestamp: proto.Int64(0),
		},
	}

	// Test
	result := state.IsTerminalRollback(3, &job)

	// Assert
	assert.False(t, result)
}

func TestUnregisterWorkflowIsTerminalRollbackTrue(t *testing.T) {
	// Setup
	state := &UnregisterWorkflow{}
	job := models.Job{
		Name: consts.UNREGISTER_PC,
		JobMetadata: &models.JobMetadata{
			Name:          consts.UNREGISTER_PC,
			TaskId:        "task_id",
			SelfClusterId: "self_cluster_id",
			Rollback:      true,
		},
		ParentTask: &ergon.Task{
			Uuid:             []byte{1, 2, 3},
			LogicalTimestamp: proto.Int64(0),
		},
	}

	// Test
	result := state.IsTerminalRollback(3, &job)

	// Assert
	assert.True(t, result)
}
