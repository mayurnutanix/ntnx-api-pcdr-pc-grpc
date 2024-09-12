/**
 * @file unconfigure_connection_workflow_test.go
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

func TestUnconfigureConnectionWorkflowExecute(t *testing.T) {
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
		Name: consts.UNCONFIGURE_CONNECTION,
		JobMetadata: &models.JobMetadata{
			Name:          consts.UNCONFIGURE_CONNECTION,
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
	MockConnectionConfig := mocks.NewMockConnectionConfig(ctrl)
	state := &UnconfigureConnectionWorkflow{MockConnectionConfig}
	MockConnectionConfig.EXPECT().RunPreUnconfigureActions(&job).Return(nil)

	// Test
	err := state.Execute(0, &job)

	// Assert
	assert.Nil(t, err)

	MockConnectionConfig.EXPECT().Unconfigure(&job).Return(nil)

	err = state.Execute(1, &job)

	// Assert
	assert.Nil(t, err)
}

func TestUnconfigureConnectionWorkflowExecuteError(t *testing.T) {
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
		Name: consts.UNCONFIGURE_CONNECTION,
		JobMetadata: &models.JobMetadata{
			Name:            consts.UNCONFIGURE_CONNECTION,
			RemoteClusterId: "remote_cluster_id",
		},
		ParentTask: &ergon.Task{
			Uuid:             []byte{1, 2, 3},
			LogicalTimestamp: proto.Int64(0),
		},
	}
	m.ErgonService.On("TaskUpdate", mock.AnythingOfType("*ergon.TaskUpdateArg")).Return(retTask, nil)
	m.ErgonService.On("TaskUpdateBase", mock.AnythingOfType("*ergon.TaskUpdateArg")).Return(retTask, nil)
	m.ErgonService.On("TaskGet", []byte{1, 2, 3}).Return(retTask.Task, nil)
	MockConnectionConfig := mocks.NewMockConnectionConfig(ctrl)
	state := &UnconfigureConnectionWorkflow{MockConnectionConfig}
	MockConnectionConfig.EXPECT().RunPreUnconfigureActions(&job).Return(nil)

	// Test
	err := state.Execute(0, &job)

	// Assert
	assert.Nil(t, err)

	MockConnectionConfig.EXPECT().Unconfigure(&job).Return(assert.AnError)

	// Test
	err = state.Execute(1, &job)

	// Assert
	assert.Error(t, err)
}

func TestUnconfigureConnectionWorkflowIsTerminalRollbackTrue(t *testing.T) {
	// Setup
	state := &UnconfigureConnectionWorkflow{}
	job := models.Job{
		Name: consts.UNCONFIGURE_CONNECTION,
		JobMetadata: &models.JobMetadata{
			Name:          consts.UNCONFIGURE_CONNECTION,
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
