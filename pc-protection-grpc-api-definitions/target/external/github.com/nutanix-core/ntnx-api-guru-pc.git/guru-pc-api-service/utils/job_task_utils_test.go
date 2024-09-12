package utils

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

var job = &models.Job{
	JobMetadata: &models.JobMetadata{},
	ParentTask: &ergon.Task{
		Uuid:             []byte{1, 2, 3, 4, 5, 6, 7, 8, 9, 10},
		LogicalTimestamp: proto.Int64(0),
	},
	Name: consts.REGISTER_PC,
}

func TestUpdateJobTask_Success(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	m := test.MockSingletons(t)
	taskUpdateRet := &ergon.TaskUpdateRet{
		Task: &ergon.Task{
			Uuid:             []byte{1, 2, 3, 4, 5, 6, 7, 8, 9, 10},
			LogicalTimestamp: proto.Int64(1),
		},
	}
	taskGetRet := &ergon.Task{
		Uuid:             []byte{1, 2, 3, 4, 5, 6, 7, 8, 9, 10},
		LogicalTimestamp: proto.Int64(1),
	}

	m.ErgonService.On("TaskGet", []byte{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}).Return(taskGetRet, nil)
	m.ErgonService.On("TaskUpdate",
		mock.AnythingOfType("*ergon.TaskUpdateArg")).Return(taskUpdateRet, nil)

	UpdateJobTask(job, ergon.Task_kRunning.Enum(), nil, "message")

	assert.NotNil(t, job.ParentTask)
	assert.Equal(t, job.ParentTask, taskUpdateRet.Task)
}

func TestUpdateJobTask_Error(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	m := test.MockSingletons(t)
	taskGetRet := &ergon.Task{
		Uuid:             []byte{1, 2, 3, 4, 5, 6, 7, 8, 9, 10},
		LogicalTimestamp: proto.Int64(1),
	}
	m.ErgonService.On("TaskGet", []byte{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}).Return(taskGetRet, nil)
	m.ErgonService.On("TaskUpdate",
		mock.AnythingOfType("*ergon.TaskUpdateArg")).Return(nil, assert.AnError)

	err := UpdateJobTask(job, ergon.Task_kRunning.Enum(), nil, "message")

	assert.Equal(t, assert.AnError, err)
}

func TestUpdateJobTaskStatusOnError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	m := test.MockSingletons(t)
	taskUpdateRet := &ergon.TaskUpdateRet{
		Task: &ergon.Task{
			Uuid:             []byte{1, 2, 3, 4, 5, 6, 7, 8, 9, 10},
			LogicalTimestamp: proto.Int64(1),
		},
	}
	m.ErgonService.On("TaskUpdateBase",
		mock.AnythingOfType("*ergon.TaskUpdateArg")).Return(taskUpdateRet, nil)

	UpdateJobTaskStatusOnError(job, assert.AnError, "message")

	assert.NotNil(t, job.JobMetadata.ErrorDetails)
	assert.Equal(t, *job.JobMetadata.ErrorDetails[0].ErrorCode, int32(consts.ErrorCodeInternalError))
	assert.Equal(t, job.JobMetadata.OperationError, assert.AnError.Error())
}

func TestUpdateJobTaskStatusOnError_WithCustomErrorCode(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	var customErrorJob = &models.Job{
		JobMetadata: &models.JobMetadata{
			ErrorDetails: []*ergon.Error{
				{
					ErrorCode: proto.Int32(int32(consts.ErrorCodePreconditionFailed)),
				},
			},
		},
		ParentTask: &ergon.Task{
			Uuid:             []byte{1, 2, 3, 4, 5, 6, 7, 8, 9, 10},
			LogicalTimestamp: proto.Int64(0),
		},
		Name: consts.REGISTER_PC,
	}
	m := test.MockSingletons(t)
	taskUpdateRet := &ergon.TaskUpdateRet{
		Task: &ergon.Task{
			Uuid:             []byte{1, 2, 3, 4, 5, 6, 7, 8, 9, 10},
			LogicalTimestamp: proto.Int64(1),
		},
	}
	m.ErgonService.On("TaskUpdateBase",
		mock.AnythingOfType("*ergon.TaskUpdateArg")).Return(taskUpdateRet, nil)

	UpdateJobTaskStatusOnError(customErrorJob, assert.AnError, "message")

	assert.NotNil(t, customErrorJob.JobMetadata.ErrorDetails)
	assert.Equal(t, *customErrorJob.JobMetadata.ErrorDetails[0].ErrorCode, int32(consts.ErrorCodePreconditionFailed))
	assert.Equal(t, customErrorJob.JobMetadata.OperationError, assert.AnError.Error())
}

func TestUpdateInternalOpaqueOfParentTask(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	msg := "Update task"
	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, consts.TaskStatusNone,
		msg, nil, gomock.Any(), nil, nil).Return(nil)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = true
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}

	result := UpdateInternalOpaqueOfParentTask(msg, jobVar)

	assert.Equal(t, result, nil)
}
