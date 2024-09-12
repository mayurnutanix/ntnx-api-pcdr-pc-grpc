/**
 * @file add_root_certificate_workflow_test.go
 * @description
 * @author deepanshu.jain@nutanix.com
 * @copyright Copyright (c) 2024 Nutanix Inc. All rights reserved.
 */

package register

import (
	"encoding/base64"
	"testing"

	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/test"
	"ntnx-api-guru-pc/guru-pc-api-service/utils"

	"github.com/nutanix-core/go-cache/ergon"
	"github.com/nutanix-core/go-cache/infrastructure/cluster/client/genesis"

	"github.com/golang/mock/gomock"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
	"google.golang.org/protobuf/proto"
)

func TestAddRootCertificateWorkflowExecuteSuccess(t *testing.T) {
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
		Name: consts.ADD_ROOT_CERTIFICATE,
		JobMetadata: &models.JobMetadata{
			Name:            consts.ADD_ROOT_CERTIFICATE,
			TaskId:          "task_id",
			SelfClusterId:   "self_cluster_id",
			RemoteClusterId: "remote_cluster_id",
			RootCert:        proto.String("root_cert"),
		},
		ParentTask: &ergon.Task{
			Uuid:             []byte{1, 2, 3},
			LogicalTimestamp: proto.Int64(0),
		},
	}

	matcher := mock.MatchedBy(func(arg *genesis.AddCACertificateArg) bool {
		assert.Equal(t, "remote_cluster_id", string(arg.GetCaCert().GetOwnerClusterUuid()))
		assert.Equal(t, base64.StdEncoding.EncodeToString([]byte(*job.JobMetadata.RootCert)), string(arg.GetCaCert().GetCaCertificate()))
		return true
	})
	ret := &genesis.AddCACertificateRet{
		Error: &genesis.GenesisGenericError{
			ErrorType: genesis.GenesisGenericError_kNoError.Enum(),
		},
	}
	old := utils.OsReadFileFunc
	defer func() { utils.OsReadFileFunc = old }()
	utils.OsReadFileFunc = func(path string) ([]byte, error) {
		return []byte{}, nil
	}
	m.ErgonService.On("TaskUpdateBase", mock.AnythingOfType("*ergon.TaskUpdateArg")).Return(retTask, nil)
	m.ErgonService.On("TaskUpdate", mock.AnythingOfType("*ergon.TaskUpdateArg")).Return(retTask, nil)
	m.ErgonService.On("TaskGet", []byte{1, 2, 3}).Return(retTask.Task, nil)
	m.GenesisClient.EXPECT().AddCACertificate(matcher).Return(ret, nil)

	state := &AddRootCertJobStateService{}
	err := state.Execute(0, &job)

	assert.Nil(t, err)
}

func TestAddRootCertificateWorkflowExecuteFailure(t *testing.T) {
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
		Name: consts.ADD_ROOT_CERTIFICATE,
		JobMetadata: &models.JobMetadata{
			Name:            consts.ADD_ROOT_CERTIFICATE,
			TaskId:          "task_id",
			SelfClusterId:   "self_cluster_id",
			RemoteClusterId: "remote_cluster_id",
			RootCert:        proto.String("root_cert"),
		},
		ParentTask: &ergon.Task{
			Uuid:             []byte{1, 2, 3},
			LogicalTimestamp: proto.Int64(0),
		},
	}
	m.ErgonService.On("TaskUpdate", mock.AnythingOfType("*ergon.TaskUpdateArg")).Return(retTask, nil)
	m.ErgonService.On("TaskUpdateBase", mock.AnythingOfType("*ergon.TaskUpdateArg")).Return(retTask, nil)
	m.GenesisClient.EXPECT().AddCACertificate(gomock.Any()).Return(nil, assert.AnError)
	state := &AddRootCertJobStateService{}

	err := state.Execute(0, &job)

	assert.Error(t, err)
}

func TestAddRootCertificateWorkflowIsTerminalRollbackFalse(t *testing.T) {
	// Setup
	state := &AddRootCertJobStateService{}
	job := models.Job{
		Name: consts.ADD_ROOT_CERTIFICATE,
		JobMetadata: &models.JobMetadata{
			Name:          consts.ADD_ROOT_CERTIFICATE,
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

func TestAddRootCertificateWorkflowIsTerminalRollbackTrue(t *testing.T) {
	// Setup
	state := &AddRootCertJobStateService{}
	job := models.Job{
		Name: consts.ADD_ROOT_CERTIFICATE,
		JobMetadata: &models.JobMetadata{
			Name:          consts.ADD_ROOT_CERTIFICATE,
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
