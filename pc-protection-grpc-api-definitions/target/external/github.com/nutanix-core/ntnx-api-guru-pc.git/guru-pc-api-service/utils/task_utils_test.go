package utils

import (
	"errors"
	"testing"

	"ntnx-api-guru-pc/generated-code/target/dto/models/prism/v4/management"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/test"

	"github.com/golang/mock/gomock"
	"github.com/golang/protobuf/proto"
	"github.com/nutanix-core/go-cache/ergon"
	"github.com/nutanix-core/go-cache/util-slbufs/util/sl_bufs/net"
	common_consts "github.com/nutanix-core/ntnx-api-guru/services/constants"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
)

var (
	TestTaskUuid = []byte("2f0164ea-3705-4067-a00d-2953e187b324")
	TestTask     = &ergon.Task{
		Uuid:             TestTaskUuid,
		LogicalTimestamp: proto.Int64(1),
	}
	TestJob = &models.Job{
		JobMetadata: &models.JobMetadata{
			RemoteClusterType: management.CLUSTERTYPE_DOMAIN_MANAGER,
			Name:              consts.CONFIGURE_CONNECTION,
			ParentTaskId:      TestTaskUuid,
			RemoteClusterId:   "7443776a-2d39-43ea-923e-e775a3a28cc8",
			RemoteAddress:     "127.0.0.1",
			RemoteClusterName: "PC_NAME",
		},
		Name:       consts.CONFIGURE_CONNECTION,
		ParentTask: TestTask,
	}
)

func TestCreateTask_Error(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	m.ErgonService.On("TaskCreate",
		mock.AnythingOfType("*ergon.TaskCreateArg")).Return(nil, errors.New("error"))
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(test.GetMockZeusConfig(), nil)

	requestContext := new(net.RpcRequestContext)

	res, err := CreateJobTask(consts.REGISTER_AOS, "", requestContext, "", false, nil, false)

	assert.Nil(t, res)
	assert.EqualError(t, err, consts.CreateErgonTaskErr)
}

func TestCreateTask_Success(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	m := test.MockSingletons(t)
	createTaskRet := &ergon.TaskCreateRet{
		Uuid:  TestParentTaskUuid,
		ExtId: nil,
	}
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(test.GetMockZeusConfig(), nil)
	m.ErgonService.On("TaskCreate",
		mock.AnythingOfType("*ergon.TaskCreateArg")).Return(createTaskRet, nil)
	m.ErgonService.On("TaskGet", TestParentTaskUuid).Return(nil, nil)

	requestContext := new(net.RpcRequestContext)
	res, err := CreateJobTask(consts.REGISTER_AOS, "", requestContext, "", false, nil, false)

	assert.Nil(t, res)
	assert.Nil(t, err)
}

func TestGetTask_Error(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	m := test.MockSingletons(t)
	m.ErgonService.On("TaskGet", TestParentTaskUuid).Return(nil, errors.New("error"))

	res, err := GetTask(TestParentTaskUuid)

	assert.Nil(t, res)
	assert.EqualError(t, err, "error")
}

func TestGetTask_Success(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	m := test.MockSingletons(t)
	task := &ergon.Task{
		Uuid: TestParentTaskUuid,
	}
	m.ErgonService.On("TaskGet", TestParentTaskUuid).Return(task, nil)

	res, err := GetTask(TestParentTaskUuid)

	assert.NotNil(t, res)
	assert.Nil(t, err)
	assert.EqualValues(t, task, res)
}

func TestUpdateTask_ErrorGetTask(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	m := test.MockSingletons(t)
	taskUUID := TestParentTaskUuid
	m.ErgonService.On("TaskGet", taskUUID).Return(nil, assert.AnError)

	completionDetails := []*ergon.KeyValue{}
	err := UpdateTask(taskUUID, consts.TaskStatusNone, "", completionDetails)

	assert.EqualError(t, assert.AnError, err.Error())
}

func TestTaskUpdate_ErrorUpdateTaskBase(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	m := test.MockSingletons(t)
	taskUUID := TestParentTaskUuid
	task := &ergon.Task{
		Uuid: TestParentTaskUuid,
	}
	m.ErgonService.On("TaskGet", taskUUID).Return(task, nil)
	m.ErgonService.On("TaskUpdateBase", mock.AnythingOfType("*ergon.TaskUpdateArg")).Return(nil, assert.AnError)

	completionDetails := []*ergon.KeyValue{}
	err := UpdateTask(taskUUID, consts.TaskStatusNone, "", completionDetails)

	assert.Error(t, assert.AnError, err)
}

func TestTaskUpdate_Success(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	test.MockFlagValues(t)
	taskUUID := TestParentTaskUuid
	task := &ergon.Task{
		Uuid:             TestParentTaskUuid,
		LogicalTimestamp: proto.Int64(1),
	}
	updateTask := &ergon.TaskUpdateRet{
		Task: task,
	}
	m.ErgonService.On("TaskGet", taskUUID).Return(task, nil)
	m.ErgonService.On("TaskUpdateBase", mock.AnythingOfType("*ergon.TaskUpdateArg")).Return(updateTask, nil)

	completionDetails := []*ergon.KeyValue{}
	err := UpdateTask(taskUUID, consts.TaskStatusNone, "", completionDetails)

	assert.Nil(t, err)
}

func TestGetTaskEntityIdForClusterEntity(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	expected := &ergon.EntityId{
		QualifiedEntityType: proto.String(consts.ClusterQualifiedEntityType),
		EntityName:          proto.String(string(TestParentTaskUuid)),
		EntityId:            TestParentTaskUuid,
		EntityType:          ergon.EntityId_kCluster.Enum(),
	}

	entity := GetTaskEntityIdForClusterEntity([]byte(TestParentTaskUuid))

	assert.Equal(t, expected, entity)
}

func TestUpdateClusterListInTaskSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	m.ErgonService.On("TaskGet", TestTaskUuid).Return(TestTask, nil)
	m.ErgonService.On("TaskUpdate", mock.AnythingOfType("*ergon.TaskUpdateArg")).Return(nil, nil)

	err := UpdateClusterListInTask(TestJob, []string{"uuid"})
	assert.Nil(t, err)
}

func TestUpdateClusterListInTaskGetTaskError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	m.ErgonService.On("TaskGet", TestTaskUuid).Return(nil, TestError)
	err := UpdateClusterListInTask(TestJob, []string{"uuid"})
	assert.EqualError(t, TestError, err.Error())
}

func TestUpdateClusterListInTaskUpdateError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	m.ErgonService.On("TaskGet", TestTaskUuid).Return(TestTask, nil)
	m.ErgonService.On("TaskUpdate", mock.AnythingOfType("*ergon.TaskUpdateArg")).Return(nil, TestError)

	err := UpdateClusterListInTask(TestJob, []string{"uuid"})
	assert.EqualError(t, TestError, err.Error())
}

func TestFetchRegisteredClusterUuidsGetChildrenError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	m.ZkSession.On("Children", consts.AOSClusterCES, true).Return(nil, nil, TestError)
	clusterUuids, err := FetchRegisteredClusterUuids()
	assert.EqualError(t, TestError, err.Error())
	assert.Nil(t, clusterUuids)
}
