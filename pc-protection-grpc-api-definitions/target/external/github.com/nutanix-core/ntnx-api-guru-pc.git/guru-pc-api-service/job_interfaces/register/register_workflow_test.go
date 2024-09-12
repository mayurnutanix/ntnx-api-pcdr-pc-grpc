/**
 * @file register_workflow_test.go
 * @description
 * @author deepanshu.jain@nutanix.com
 * @copyright Copyright (c) 2024 Nutanix Inc. All rights reserved.
 */

package register

import (
	"testing"

	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/mocks"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/test"

	"github.com/golang/mock/gomock"
	"github.com/nutanix-core/go-cache/ergon"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
	"google.golang.org/protobuf/proto"
)

func TestRegisterWorkflowExecute(t *testing.T) {
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
		Name: consts.REGISTER_PC,
		JobMetadata: &models.JobMetadata{
			Name:          consts.REGISTER_PC,
			TaskId:        "task_id",
			SelfClusterId: "self_cluster_id",
		},
		ParentTask: &ergon.Task{
			Uuid:             []byte{1, 2, 3},
			LogicalTimestamp: proto.Int64(0),
		},
	}

	m.EventForwarder.EXPECT().PushEvent(
		gomock.Any(), consts.EventTypeRegisterDomainManager, job.JobMetadata.TaskId,
		consts.EventNamespace, consts.PulseDomainManagerEntity, job.JobMetadata.SelfClusterId)
	mockRegisterSvc := mocks.NewMockRegister(ctrl)
	mockUnregisterSvc := mocks.NewMockUnregister(ctrl)
	state := &RegisterWorkflow{mockRegisterSvc, mockUnregisterSvc}

	// Setup Step 0 - Precheck
	mockRegisterSvc.EXPECT().RunPreActions(&job).Return(nil)
	matcherArg := mock.MatchedBy(func(arg *ergon.TaskUpdateArg) bool {
		return arg.GetMessage() == "Executing prechecks for register operation" &&
			arg.GetStatus() == *ergon.Task_kRunning.Enum()
	})
	m.ErgonService.On("TaskUpdate", matcherArg).Return(retTask, nil)
	matcherArg = mock.MatchedBy(func(arg *ergon.TaskUpdateArg) bool {
		return arg.GetMessage() == "Successfully completed pre-registration steps" &&
			arg.GetPercentageComplete() == 40
	})
	m.ErgonService.On("TaskGet", []byte{1, 2, 3}).Return(retTask.Task, nil)
	m.ErgonService.On("TaskUpdate", matcherArg).Return(retTask, nil)

	err := state.Execute(0, &job)

	assert.Nil(t, err)

	// Setup Step 1 - Setup trust
	mockRegisterSvc.EXPECT().SetupTrust(&job).Return(nil)
	matcherArg = mock.MatchedBy(func(arg *ergon.TaskUpdateArg) bool {
		return arg.GetMessage() == "Successfully completed setup trust step" &&
			arg.GetPercentageComplete() == 60
	})
	m.ErgonService.On("TaskUpdate", matcherArg).Return(retTask, nil)

	err = state.Execute(1, &job)

	assert.Nil(t, err)

	// Test Step 2 - Configure Entities
	mockRegisterSvc.EXPECT().ConfigureEntities(&job).Return(nil)
	matcherArg = mock.MatchedBy(func(arg *ergon.TaskUpdateArg) bool {
		return arg.GetMessage() == "Successfully completed configured entities step" &&
			arg.GetPercentageComplete() == 80
	})
	m.ErgonService.On("TaskGet", []byte{1, 2, 3}).Return(retTask.Task, nil)
	m.ErgonService.On("TaskUpdate", matcherArg).Return(retTask, nil)

	err = state.Execute(2, &job)

	assert.Nil(t, err)

	// Setup Step 3 - Post actions
	mockRegisterSvc.EXPECT().RunPostActions(&job).Return(nil)
	matcherArg = mock.MatchedBy(func(arg *ergon.TaskUpdateArg) bool {
		return arg.GetMessage() == "Successfully completed PC-PC Registration" &&
			arg.GetStatus() == *ergon.Task_kSucceeded.Enum()
	})
	m.ErgonService.On("TaskGet", []byte{1, 2, 3}).Return(retTask.Task, nil)

	m.ErgonService.On("TaskUpdate", matcherArg).Return(retTask, nil)

	err = state.Execute(3, &job)

	assert.Nil(t, err)
}

func TestRegisterWorkflowExecuteError(t *testing.T) {
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
		Name: consts.REGISTER_PC,
		JobMetadata: &models.JobMetadata{
			Name:          consts.REGISTER_PC,
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
	m.EventForwarder.EXPECT().PushEvent(
		gomock.Any(), consts.EventTypeRegisterDomainManager, job.JobMetadata.TaskId,
		consts.EventNamespace, consts.PulseDomainManagerEntity, job.JobMetadata.SelfClusterId).Times(4)
	m.ErgonService.On("TaskGet", []byte{1, 2, 3}).Return(retTask.Task, nil)
	m.ZkSession.On("Children", consts.AOSClusterCES, true).Return([]string{"uuid"}, nil, nil)
	m.ErgonService.On("TaskUpdate", mock.AnythingOfType("*ergon.TaskUpdateArg")).Return(nil, nil)
	mockRegisterSvc := mocks.NewMockRegister(ctrl)
	mockUnregisterSvc := mocks.NewMockUnregister(ctrl)
	state := &RegisterWorkflow{mockRegisterSvc, mockUnregisterSvc}

	// Setup Step 0 - Precheck
	mockRegisterSvc.EXPECT().RunPreActions(&job).Return(assert.AnError)

	err := state.Execute(0, &job)

	assert.Error(t, err, assert.AnError)

	// Setup Step 1 - Setup Trust
	mockRegisterSvc.EXPECT().SetupTrust(&job).Return(assert.AnError)

	err = state.Execute(1, &job)

	assert.Error(t, err)

	// Setup Step 2 - Configure Entities
	mockRegisterSvc.EXPECT().ConfigureEntities(&job).Return(assert.AnError)

	err = state.Execute(2, &job)

	assert.Error(t, err)

	// Setup Step 3 - RunPostActions
	mockRegisterSvc.EXPECT().RunPostActions(&job).Return(assert.AnError)

	err = state.Execute(3, &job)

	assert.Error(t, err)
}

func TestRegisterWorkflowExecuteRollback(t *testing.T) {
	// Setup
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	job := models.Job{
		Name: consts.REGISTER_PC,
		JobMetadata: &models.JobMetadata{
			Name:          consts.REGISTER_PC,
			TaskId:        "task_id",
			SelfClusterId: "self_cluster_id",
			Rollback:      true,
		},
		ParentTask: &ergon.Task{
			Uuid:             []byte{1, 2, 3},
			LogicalTimestamp: proto.Int64(0),
		},
	}
	mockRegisterSvc := mocks.NewMockRegister(ctrl)
	mockUnregisterSvc := mocks.NewMockUnregister(ctrl)
	state := &RegisterWorkflow{mockRegisterSvc, mockUnregisterSvc}

	// Setup Step 0 - Precheck

	// Test
	err := state.Execute(0, &job)

	// Assert
	assert.Nil(t, err)

	// Setup Step 1 - Remove Trust
	mockUnregisterSvc.EXPECT().RemoveTrust(&job).Return(nil)

	// Test
	err = state.Execute(1, &job)

	// Assert
	assert.Nil(t, err)

	// Setup Step 2 - Unconfigure entities
	mockUnregisterSvc.EXPECT().UnconfigureEntities(&job).Return(nil)

	err = state.Execute(2, &job)

	assert.Nil(t, err)

	// Test Step 3 - Post actions
	err = state.Execute(3, &job)

	assert.Nil(t, err)
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

	mockRegisterSvc := mocks.NewMockRegister(ctrl)
	mockUnregisterSvc := mocks.NewMockUnregister(ctrl)
	state := &RegisterWorkflow{mockRegisterSvc, mockUnregisterSvc}

	// Test
	err := state.Execute(0, &job)

	// Assert
	assert.Nil(t, err)
}

func TestRegisterWorkflowIsTerminalForward(t *testing.T) {
	// Setup
	state := &RegisterWorkflow{}

	// Test
	result := state.IsTerminalForward(3)

	// Assert
	assert.False(t, result)
}

func TestRegisterWorkflowIsTerminalForwardTrue(t *testing.T) {
	// Setup
	state := &RegisterWorkflow{}

	// Test
	result := state.IsTerminalForward(consts.RegisterStepsCount)

	// Assert
	assert.True(t, result)
}

func TestRegisterWorkflowIsTerminalRollback(t *testing.T) {
	// Setup
	state := &RegisterWorkflow{}
	job := models.Job{
		Name: consts.REGISTER_PC,
		JobMetadata: &models.JobMetadata{
			Name:          consts.REGISTER_PC,
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

func TestRegisterWorkflowIsTerminalRollbackTrue(t *testing.T) {
	// Setup
	state := &RegisterWorkflow{}
	job := models.Job{
		Name: consts.REGISTER_PC,
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
	result := state.IsTerminalRollback(-1, &job)

	// Assert
	assert.True(t, result)
}
