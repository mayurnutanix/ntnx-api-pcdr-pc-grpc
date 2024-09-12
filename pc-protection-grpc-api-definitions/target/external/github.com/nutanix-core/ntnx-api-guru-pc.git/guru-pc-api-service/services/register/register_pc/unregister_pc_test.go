package register_pc

import (
	"errors"
	"fmt"
	"net/http"
	"ntnx-api-guru-pc/generated-code/target/dto/models/prism/v4/management"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	test_consts "ntnx-api-guru-pc/guru-pc-api-service/services/register_aos/test-consts"
	"ntnx-api-guru-pc/guru-pc-api-service/test"
	"ntnx-api-guru-pc/guru-pc-api-service/utils"
	"testing"

	"github.com/golang/mock/gomock"
	"github.com/golang/protobuf/proto"
	"github.com/nutanix-core/go-cache/ergon"
	zk "github.com/nutanix-core/go-cache/go-zookeeper"
	"github.com/nutanix-core/go-cache/infrastructure/cluster/client/genesis"
	"github.com/nutanix-core/go-cache/insights/insights_interface"
	"github.com/nutanix-core/go-cache/prism"
	"github.com/nutanix-core/go-cache/util-go/uuid4"
	common_consts "github.com/nutanix-core/ntnx-api-guru/services/constants"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
)

var unregisterPCService = NewUnregisterPCImpl()

var (
	TestTaskRet = &ergon.TaskUpdateRet{
		Task: TestTask,
	}
)

func TestRunPreActionsCESDoesntExists(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	jobMetadata := models.JobMetadata{
		ParentTaskId:      TestParentTaskUuid,
		Name:              consts.UNREGISTER_PC,
		RemoteClusterType: management.CLUSTERTYPE_DOMAIN_MANAGER,
		RemoteClusterId:   "remote-cluster-id",
	}
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
		ParentTask: &ergon.Task{
			LogicalTimestamp: nil,
			Uuid:             TestParentTaskUuid,
		},
	}
	test.MockDRUnpairQuerySuccess(m)
	taskArg := ergon.TaskListArg{
		OperationTypeList: []string{"kPrismCentralAddNodeRequest"},
		StatusList:        []ergon.Task_Status{ergon.Task_kQueued, ergon.Task_kRunning},
	}
	taskListResponse := &ergon.TaskListRet{
		TaskUuidList: nil,
	}
	m.ErgonService.On("TaskList", &taskArg).Return(taskListResponse, nil).Once()
	m.ZkSession.On("Exist", "/appliance/physical/connection/domain-manager/clusterexternalstate/remote-cluster-id", true).Return(true, nil, nil)
	m.ZkSession.On("Get", "/appliance/physical/connection/domain-manager/clusterexternalstate/remote-cluster-id", true).Return(nil, nil, errors.New("error zk exists"))
	m.ZkClient.EXPECT().GetZkNode("/appliance/logical/pc_environment/config", true).Return(nil, errors.New("error zk get"))
	m.RemoteRestClient.EXPECT().FanoutProxy(http.MethodGet, gomock.Any(), http.MethodGet, jobMetadata.RemoteClusterId, gomock.Any()).Return(nil, nil, errors.New("error"))
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(test.GetMockZeusConfig(), nil).AnyTimes()
	err := unregisterPCService.RunPreActions(jobVar)

	assert.Nil(t, err)
	assert.True(t, jobVar.JobMetadata.IsLocalOnly)
}

func TestRunPreActionsCESExists(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	test.MockDRUnpairQuerySuccess(m)
	credentialMap := make(map[string]string)
	jobMetadata := models.JobMetadata{
		Name:              consts.UNREGISTER_PC,
		RemoteClusterType: management.CLUSTERTYPE_DOMAIN_MANAGER,
		RemoteClusterId:   "remote-cluster-id",
	}
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
		ParentTask: &ergon.Task{
			LogicalTimestamp: nil,
			Uuid:             TestParentTaskUuid,
		},
	}
	var ces = prism.ClusterExternalState{
		LogicalTimestamp: new(int64),
		ClusterUuid:      new(string),
		ConfigDetails: &prism.ConfigDetails{
			ExternalIp: proto.String("remoteIp"),
		},
		ClusterDetails: &prism.ClusterDetails{
			ClusterName: proto.String("remoteClusterName"),
		},
	}
	response := `{
		"resources": {
			"version": "master"
		}
	}`
	taskArg := ergon.TaskListArg{
		OperationTypeList: []string{"kPrismCentralAddNodeRequest"},
		StatusList:        []ergon.Task_Status{ergon.Task_kQueued, ergon.Task_kRunning},
	}
	taskListResponse := &ergon.TaskListRet{
		TaskUuidList: nil,
	}
	m.ErgonService.On("TaskList", &taskArg).Return(taskListResponse, nil).Once()
	cesProto, _ := proto.Marshal(&ces)
	m.ZkSession.On("Exist", "/appliance/physical/connection/domain-manager/clusterexternalstate/remote-cluster-id", true).Return(true, nil, nil)
	m.ZkSession.On("Get", "/appliance/physical/connection/domain-manager/clusterexternalstate/remote-cluster-id", true).Return(cesProto, nil, nil)
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(test.GetMockZeusConfig(), nil).AnyTimes()
	m.ZkClient.EXPECT().GetZkNode("/appliance/logical/pc_environment/config", true).Return(nil, errors.New("error zk get"))
	m.RemoteRestClient.EXPECT().FanoutProxy(http.MethodGet, gomock.Any(), http.MethodGet, jobMetadata.RemoteClusterId, gomock.Any()).Return([]byte(response), nil, nil)
	err := unregisterPCService.RunPreActions(jobVar)

	assert.Nil(t, err)
	assert.False(t, jobVar.JobMetadata.IsLocalOnly)
}

func TestRunPreActionsCESError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	test.MockDRUnpairQuerySuccess(m)
	credentialMap := make(map[string]string)
	jobMetadata := models.JobMetadata{
		Name:              consts.UNREGISTER_PC,
		RemoteClusterType: management.CLUSTERTYPE_DOMAIN_MANAGER,
		RemoteClusterId:   "remote-cluster-id",
	}
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
		ParentTask: &ergon.Task{
			LogicalTimestamp: nil,
			Uuid:             TestParentTaskUuid,
		},
	}
	taskArg := ergon.TaskListArg{
		OperationTypeList: []string{"kPrismCentralAddNodeRequest"},
		StatusList:        []ergon.Task_Status{ergon.Task_kQueued, ergon.Task_kRunning},
	}
	taskListResponse := &ergon.TaskListRet{
		TaskUuidList: nil,
	}
	m.ErgonService.On("TaskList", &taskArg).Return(taskListResponse, nil).Once()
	m.ZkSession.On("Exist", "/appliance/physical/connection/domain-manager/clusterexternalstate/remote-cluster-id", true).Return(true, nil, nil)
	m.ZkSession.On("Get", "/appliance/physical/connection/domain-manager/clusterexternalstate/remote-cluster-id", true).Return(nil, nil, errors.New("error zk get"))
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(test.GetMockZeusConfig(), nil).AnyTimes()
	m.ZkClient.EXPECT().GetZkNode("/appliance/logical/pc_environment/config", true).Return(nil, errors.New("error zk get"))
	m.RemoteRestClient.EXPECT().FanoutProxy(http.MethodGet, gomock.Any(), http.MethodGet, jobMetadata.RemoteClusterId, "prism_central").Return(nil, nil, errors.New("error"))
	err := unregisterPCService.RunPreActions(jobVar)

	assert.Nil(t, err)
	assert.True(t, jobVar.JobMetadata.IsLocalOnly)
}

func TestUnconfigureEntitiesAPIError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	jobMetadata := models.JobMetadata{
		Name:              consts.UNREGISTER_PC,
		RemoteClusterType: management.CLUSTERTYPE_DOMAIN_MANAGER,
		RemoteClusterId:   "remote-cluster-id",
	}
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
		ParentTask: &ergon.Task{
			LogicalTimestamp: nil,
			Uuid:             TestParentTaskUuid,
		},
	}
	m.Uuid.EXPECT().New().Return(uuid4.StringToUuid4(TestRequestID))
	headers := map[string]string{}
	headers[consts.NtnxRequestId] = TestRequestID
	url := fmt.Sprintf(V4UnconfigureConnectionPath, consts.ApiVersion, jobMetadata.RemoteClusterId)
	m.RemoteRestClient.EXPECT().CallApiCertBasedAuth(jobMetadata.RemoteAddress, gomock.Any(), url, http.MethodPost, gomock.Any(), headers,
		nil, nil).Return(nil, nil, errors.New("error"))

	err := unregisterPCService.UnconfigureEntities(jobVar)

	assert.Equal(t, guru_error.GetInternalError(consts.UnregisterPCOpName), err)
}

func TestRemoveTrust_CallRemoveRootCertAPIError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	old := utils.OsReadFileFunc
	defer func() { utils.OsReadFileFunc = old }()
	certificateBytes := []byte(test_consts.TestCertificate)
	retBytes := []byte(test_consts.TestPrivateKey)
	utils.OsReadFileFunc = func(path string) ([]byte, error) {
		if path == consts.EntityPrivateKey {
			return retBytes, nil
		}
		return certificateBytes, nil
	}
	timeStamp := int64(1)
	getZkResp := prism.ClusterExternalState{
		LogicalTimestamp: &timeStamp,
		ClusterUuid:      nil,
	}
	marshalGetZkResp, _ := proto.Marshal(&getZkResp)
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(marshalGetZkResp, nil)
	var jobMetadata models.JobMetadata
	jobMetadata.RemoteAddress = "remote-address"
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobVar := &models.Job{
		Name:        jobMetadata.Name,
		JobMetadata: &jobMetadata,
		ParentTask:  nil,
	}
	m.Uuid.EXPECT().New().Return(uuid4.StringToUuid4(TestRequestID))
	headers := map[string]string{}
	headers[consts.NtnxRequestId] = TestRequestID
	url := fmt.Sprintf(V4RemoveRootCertAPIPath, consts.ApiVersion, jobMetadata.RemoteClusterId)
	m.RemoteRestClient.EXPECT().CallApiCertBasedAuth(jobMetadata.RemoteAddress, gomock.Any(), url, http.MethodPost, gomock.Any(), headers,
		nil, nil).Return(nil, nil, errors.New("error"))

	err := unregisterPCService.RemoveTrust(jobVar)

	assert.Equal(t, guru_error.GetInternalError(consts.UnregisterPCOpName), err)
}

func TestUnconfigureEntities_Success(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	jobMetadata := models.JobMetadata{
		Name:              consts.UNREGISTER_PC,
		RemoteClusterType: management.CLUSTERTYPE_DOMAIN_MANAGER,
		RemoteClusterId:   "remote-cluster-id",
	}
	jobVar := &models.Job{
		Name:        jobMetadata.Name,
		JobMetadata: &jobMetadata,
		ParentTask: &ergon.Task{
			LogicalTimestamp: proto.Int64(0),
			Uuid:             TestParentTaskUuid,
		},
	}
	m.Uuid.EXPECT().New().Return(uuid4.StringToUuid4(TestRequestID))
	headers := map[string]string{}
	headers[consts.NtnxRequestId] = TestRequestID
	url := fmt.Sprintf(V4UnconfigureConnectionPath, consts.ApiVersion, jobMetadata.RemoteClusterId)
	m.RemoteRestClient.EXPECT().CallApiCertBasedAuth(jobMetadata.RemoteAddress, gomock.Any(), url, http.MethodPost, gomock.Any(), headers,
		nil, nil).Return(test.GetMockTaskReferenceModelResponse(), nil, nil)
	m.RemoteRestClient.EXPECT().CallApi(
		jobMetadata.RemoteAddress, consts.EnvoyPort, "/api/prism/v4.0.b1/config/tasks/ZXJnb24=:278bbfc2-280f-529b-882b-14ea649b9a95", http.MethodGet, nil,
		gomock.Any(), gomock.Any(), nil, gomock.Any(), gomock.Any(), true).Return(test.GetMockTaskSuccessResponse(), nil, nil).AnyTimes()
	m.DbClient.On("Query", mock.AnythingOfType("*insights_interface.GetEntitiesWithMetricsArg")).Return(nil, insights_interface.ErrNotFound)
	m.ErgonService.On("TaskUpdate", mock.AnythingOfType("*ergon.TaskUpdateArg")).Return(TestTaskRet, nil)
	m.ErgonService.On("TaskGet", TestTaskUuid).Return(TestTask, nil)
	m.ZkSession.On("Children", consts.AOSClusterCES, true).Return([]string{"uuid"}, nil, nil)
	m.ErgonService.On("TaskUpdate", mock.AnythingOfType("*ergon.TaskUpdateArg")).Return(nil, nil)
	stat := zk.Stat{
		Version: 0,
	}
	path := consts.DomainManagerCES + "/" + jobMetadata.RemoteClusterId
	m.ZkSession.On("Exist", path, true).Return(true, &stat, nil)
	m.ZkSession.On("Delete", path, stat.Version, true).Return(nil)

	err := unregisterPCService.UnconfigureEntities(jobVar)

	assert.Nil(t, err)
}

func TestRemoveTrust_Success(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	old := utils.OsReadFileFunc
	defer func() { utils.OsReadFileFunc = old }()
	certificateBytes := []byte(test_consts.TestCertificate)
	retBytes := []byte(test_consts.TestPrivateKey)
	utils.OsReadFileFunc = func(path string) ([]byte, error) {
		if path == consts.EntityPrivateKey {
			return retBytes, nil
		}
		return certificateBytes, nil
	}
	timeStamp := int64(1)
	getZkResp := prism.ClusterExternalState{
		LogicalTimestamp: &timeStamp,
		ClusterUuid:      nil,
	}
	marshalGetZkResp, _ := proto.Marshal(&getZkResp)
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(marshalGetZkResp, nil)
	var jobMetadata models.JobMetadata
	jobMetadata.RemoteAddress = "remote-address"
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobVar := &models.Job{
		Name:        jobMetadata.Name,
		JobMetadata: &jobMetadata,
		ParentTask: &ergon.Task{
			LogicalTimestamp: proto.Int64(0),
			Uuid:             TestParentTaskUuid,
		},
	}
	m.Uuid.EXPECT().New().Return(uuid4.StringToUuid4(TestRequestID))
	headers := map[string]string{}
	headers[consts.NtnxRequestId] = TestRequestID
	url := fmt.Sprintf(V4RemoveRootCertAPIPath, consts.ApiVersion, jobMetadata.RemoteClusterId)
	m.RemoteRestClient.EXPECT().CallApiCertBasedAuth(jobMetadata.RemoteAddress, gomock.Any(), url, http.MethodPost, gomock.Any(), headers,
		nil, nil).Return(test.GetMockTaskReferenceModelResponse(), nil, nil)
	m.RemoteRestClient.EXPECT().CallApi(
		jobMetadata.RemoteAddress, consts.EnvoyPort, "/api/prism/v4.0.b1/config/tasks/ZXJnb24=:278bbfc2-280f-529b-882b-14ea649b9a95", http.MethodGet, nil,
		gomock.Any(), gomock.Any(), nil, gomock.Any(), gomock.Any(), true).Return(test.GetMockTaskSuccessResponse(), nil, nil).AnyTimes()
	ret := genesis.DeleteCACertificateRet{}
	m.GenesisClient.EXPECT().DeleteCACertificate(gomock.Any()).Return(&ret, nil)
	err := unregisterPCService.RemoveTrust(jobVar)

	assert.Nil(t, err)
}

func TestRunPostActions_Success(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	jobMetadata := models.JobMetadata{
		Name:              consts.UNREGISTER_PC,
		RemoteClusterType: management.CLUSTERTYPE_DOMAIN_MANAGER,
		RemoteClusterId:   "remote-cluster-id",
	}
	jobVar := &models.Job{
		Name:        jobMetadata.Name,
		JobMetadata: &jobMetadata,
		ParentTask: &ergon.Task{
			LogicalTimestamp: proto.Int64(0),
			Uuid:             TestParentTaskUuid,
		},
	}
	m.ErgonService.On("TaskGet", TestParentTaskUuid).Return(TestTask, nil)
	m.ZkSession.On("Children", consts.AOSClusterCES, true).Return([]string{"uuid"}, nil, nil)
	m.ErgonService.On("TaskUpdate", mock.AnythingOfType("*ergon.TaskUpdateArg")).Return(nil, nil)
	err := unregisterPCService.RunPostActions(jobVar)

	assert.Nil(t, err)
}
