/**
 * @file remove_root_certificate_workflow_test.go
 * @description
 * @author deepanshu.jain@nutanix.com
 * @copyright Copyright (c) 2024 Nutanix Inc. All rights reserved.
 */

package unregister

import (
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/test"
	"testing"

	"github.com/golang/mock/gomock"
	"github.com/nutanix-core/go-cache/ergon"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
	"google.golang.org/protobuf/proto"
)

func TestRemoveRootCertificateWorkflowExecuteSuccess(t *testing.T) {
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
		Name: consts.REMOVE_ROOT_CERTIFICATE,
		JobMetadata: &models.JobMetadata{
			Name:            consts.REMOVE_ROOT_CERTIFICATE,
			TaskId:          "task_id",
			SelfClusterId:   "self_cluster_id",
			RemoteClusterId: "remote_cluster_id",
		},
		ParentTask: &ergon.Task{
			Uuid:             []byte{1, 2, 3},
			LogicalTimestamp: proto.Int64(0),
		},
	}
	m.ErgonService.On("TaskUpdate", mock.AnythingOfType("*ergon.TaskUpdateArg")).Return(retTask, nil)
	m.GenesisClient.EXPECT().DeleteCACertificate(gomock.Any()).Return(nil, nil)
	m.ErgonService.On("TaskGet", []byte{1, 2, 3}).Return(retTask.Task, nil)
	state := &RemoveRootCertJobStateService{}

	err := state.Execute(0, &job)

	assert.Nil(t, err)
}

func TestRemoveRootCertificateWorkflowExecuteFailure(t *testing.T) {
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
		Name: consts.REMOVE_ROOT_CERTIFICATE,
		JobMetadata: &models.JobMetadata{
			Name:            consts.REMOVE_ROOT_CERTIFICATE,
			TaskId:          "task_id",
			SelfClusterId:   "self_cluster_id",
			RemoteClusterId: "remote_cluster_id",
		},
		ParentTask: &ergon.Task{
			Uuid:             []byte{1, 2, 3},
			LogicalTimestamp: proto.Int64(0),
		},
	}
	m.ErgonService.On("TaskUpdate", mock.AnythingOfType("*ergon.TaskUpdateArg")).Return(retTask, nil)
	m.ErgonService.On("TaskUpdateBase", mock.AnythingOfType("*ergon.TaskUpdateArg")).Return(retTask, nil)
	m.GenesisClient.EXPECT().DeleteCACertificate(gomock.Any()).Return(nil, assert.AnError)
	m.ErgonService.On("TaskGet", []byte{1, 2, 3}).Return(retTask.Task, nil)
	state := &RemoveRootCertJobStateService{}

	err := state.Execute(0, &job)

	assert.Error(t, err)
}

func TestRemoveRootCertificateWorkflowIsTerminalRollbackFalse(t *testing.T) {
	// Setup
	state := &RemoveRootCertJobStateService{}
	job := models.Job{
		Name: consts.REMOVE_ROOT_CERTIFICATE,
		JobMetadata: &models.JobMetadata{
			Name:          consts.REMOVE_ROOT_CERTIFICATE,
			TaskId:        "task_id",
			SelfClusterId: "self_cluster_id",
			Rollback:      false,
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

func TestRemoveRootCertificateWorkflowIsTerminalRollbackTrue(t *testing.T) {
	// Setup
	state := &RemoveRootCertJobStateService{}
	job := models.Job{
		Name: consts.REMOVE_ROOT_CERTIFICATE,
		JobMetadata: &models.JobMetadata{
			Name:          consts.REMOVE_ROOT_CERTIFICATE,
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
