package register_pc

import (
	"encoding/json"
	"errors"
	"fmt"
	"net/http"
	"ntnx-api-guru-pc/generated-code/target/dto/models/prism/v4/management"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	mocks "ntnx-api-guru-pc/guru-pc-api-service/mocks"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/services/register"
	"ntnx-api-guru-pc/guru-pc-api-service/services/register/configure_connection"
	test_consts "ntnx-api-guru-pc/guru-pc-api-service/services/register_aos/test-consts"
	"ntnx-api-guru-pc/guru-pc-api-service/services/trust_setup/exchange_root_certificate"
	"ntnx-api-guru-pc/guru-pc-api-service/test"
	"ntnx-api-guru-pc/guru-pc-api-service/utils"
	"testing"
	"time"

	"github.com/golang/mock/gomock"
	"github.com/golang/protobuf/proto"
	aplos "github.com/nutanix-core/go-cache/aplos/sl_bufs"
	"github.com/nutanix-core/go-cache/ergon"
	zk "github.com/nutanix-core/go-cache/go-zookeeper"
	"github.com/nutanix-core/go-cache/prism"
	"github.com/nutanix-core/go-cache/util-go/uuid4"
	common_consts "github.com/nutanix-core/ntnx-api-guru/services/constants"
	mockZk "github.com/nutanix-core/ntnx-api-guru/services/zk/mocks"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
)

var (
	TestParentTaskUuid     = []byte{10, 204, 62, 133, 16, 76, 72, 134, 85, 211, 252, 41, 149, 19, 150, 228}
	TestRequestID          = "1acc3e85-104c-4886-55d3-fc29951396e8"
	RegisteredPeZkNodePath = "/appliance/physical/clusterexternalstate"
	BroadcastedZkPath      = "/appliance/physical/domain-manager/root-cert-exchange/"
	TestTaskUuid           = []byte("2f0164ea-3705-4067-a00d-2953e187b324")
	TestTask               = &ergon.Task{
		Uuid:             TestTaskUuid,
		LogicalTimestamp: proto.Int64(1),
	}
)

func MockRegisterPcImpl(precheck register.Precheck) register.Register {
	return &RegisterPcImpl{
		ConfigureConnection: configure_connection.New(management.CLUSTERTYPE_DOMAIN_MANAGER),
		Precheck:            precheck,
	}
}

func TestExecutePreSteps_Success(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	ergonTask := ergon.Task{
		LogicalTimestamp: nil,
		Uuid:             TestParentTaskUuid,
	}
	var jobMetadata models.JobMetadata
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
		ParentTask:    &ergonTask,
	}
	mockPrecheck := mocks.NewMockPrecheck(ctrl)
	mockPrecheck.EXPECT().Execute(jobVar).Return(nil)
	registerPCService := MockRegisterPcImpl(mockPrecheck)
	err := registerPCService.RunPreActions(jobVar)

	assert.Nil(t, err)
}

func TestExecutePreSteps_PrecheckErr(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	ergonTask := ergon.Task{
		LogicalTimestamp: nil,
		Uuid:             TestParentTaskUuid,
	}
	var jobMetadata models.JobMetadata
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
		ParentTask:    &ergonTask,
	}
	mockPrecheck := mocks.NewMockPrecheck(ctrl)
	mockPrecheck.EXPECT().Execute(jobVar).Return(guru_error.GetInternalError(consts.RegisterPCOpName))

	registerPCService := MockRegisterPcImpl(mockPrecheck)
	err := registerPCService.RunPreActions(jobVar)

	assert.Error(t, err)
}

func TestExecutePreRegistrationStepscheckExistingTasksresultNil(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	ergonTask := ergon.Task{
		LogicalTimestamp: nil,
		Uuid:             TestParentTaskUuid,
	}
	var jobMetadata models.JobMetadata
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
		ParentTask:    &ergonTask,
	}
	var TaskUuidList = [][]byte{
		TestParentTaskUuid,
		TestParentTaskUuid,
	}
	taskListRet := &ergon.TaskListRet{
		TaskUuidList: TaskUuidList,
	}
	m.ErgonService.On("TaskList", mock.AnythingOfType("*ergon.TaskListArg")).Return(taskListRet, nil)
	expectedErr := guru_error.GetPreconditionFailureError(fmt.Sprintf("Another registration operation is in progress. Please track the task : %s",
		uuid4.String(TestParentTaskUuid)), consts.RegisterPCOpName)

	registerPCService := NewRegisterPcImpl()
	err := registerPCService.RunPreActions(jobVar)

	assert.Equal(t, expectedErr, err)
}

func TestExecutePreRegistrationStepscheckInitiatorPCError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	ergonTask := ergon.Task{
		LogicalTimestamp: nil,
		Uuid:             TestParentTaskUuid,
	}
	var jobMetadata models.JobMetadata
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
		ParentTask:    &ergonTask,
	}
	var TaskUuidList = [][]byte{
		TestParentTaskUuid,
	}
	taskListRet := &ergon.TaskListRet{
		TaskUuidList: TaskUuidList,
	}
	m.ErgonService.On("TaskList", mock.AnythingOfType("*ergon.TaskListArg")).Return(taskListRet, nil)
	m.ZkClient.EXPECT().GetZkNode(consts.PCEnvZkNodePath, true).
		Return(nil, errors.New("error")).Times(1)

	registerPCService := NewRegisterPcImpl()
	err := registerPCService.RunPreActions(jobVar)

	assert.NotNil(t, err)
}

func TestExecutePreRegistrationStepscheckReachabilityError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	ergonTask := ergon.Task{
		LogicalTimestamp: nil,
		Uuid:             TestParentTaskUuid,
	}
	var jobMetadata models.JobMetadata
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
		ParentTask:    &ergonTask,
	}
	var TaskUuidList = [][]byte{
		TestParentTaskUuid,
	}
	taskListRet := &ergon.TaskListRet{
		TaskUuidList: TaskUuidList,
	}
	m.ErgonService.On("TaskList", mock.AnythingOfType("*ergon.TaskListArg")).Return(taskListRet, nil)
	environmentInfo := &aplos.PCEnvironmentConfig_PCEnvironmentInfo{
		EnvironmentType: aplos.PCEnvironmentConfig_PCEnvironmentInfo_ONPREM.Enum(),
	}
	GetZkRet := &aplos.PCEnvironmentConfig{
		PcEnvironmentInfo: environmentInfo,
		PcConfigMetadata:  nil,
	}
	marshalGetZkRet, _ := proto.Marshal(GetZkRet)
	m.ZkClient.EXPECT().GetZkNode(consts.PCEnvZkNodePath, true).
		Return(marshalGetZkRet, nil).AnyTimes()
	m.RemoteRestClient.EXPECT().CallApiBasicAuth(jobMetadata.RemoteAddress,
		consts.EnvoyPort, consts.V3PrismCentralGetUrl, http.MethodGet,
		nil, gomock.Any(), nil, nil,
		credentialMap["username"], credentialMap["password"]).Return(nil, nil, errors.New("error"))

	registerPCService := NewRegisterPcImpl()
	err := registerPCService.RunPreActions(jobVar)

	assert.Equal(t, guru_error.GetInternalError(consts.RegisterPCOpName), err)
}

func TestExecutePostRegistration_StepsCertBroadcastError(t *testing.T) {
	// Cert broadcast error should not be returned as error
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	var jobMetadata models.JobMetadata
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: nil,
		ParentTask:    TestTask,
	}
	m.ZkSession.On("Children", RegisteredPeZkNodePath, true).Return(nil, nil, errors.New("error"))
	m.ErgonService.On("TaskGet", TestTaskUuid).Return(TestTask, nil)
	m.ZkSession.On("Children", consts.AOSClusterCES, true).Return([]string{"uuid"}, nil, nil)
	m.ErgonService.On("TaskUpdate", mock.AnythingOfType("*ergon.TaskUpdateArg")).Return(nil, nil)
	m.EventForwarder.EXPECT().PushEvent(
		gomock.Any(), consts.EventTypeRegisterDomainManager, jobMetadata.TaskId,
		consts.EventNamespace, consts.PulseDomainManagerEntity, jobMetadata.SelfClusterId)
	registerPCService := NewRegisterPcImpl()
	err := registerPCService.RunPostActions(jobVar)

	assert.Nil(t, err)
}

func TestExecutePostRegistrationSteps(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	jobMetadata := models.JobMetadata{
		RemoteClusterId:       "remote-cluster-id",
		TaskId:                "task-id",
		OperationStartTime:    time.Now().Format(consts.DateTimeFormat),
		RemoteVersion:         "remoteVersion",
		RemoteEnvironmentType: "remoteEnvironmentType",
		RemoteProviderType:    "remoteProviderType",
		RemoteInstanceType:    "remoteInstanceType",
		SelfEnvironmentType:   "selfEnvironmentType",
		SelfProviderType:      "selfProviderType",
		SelfInstanceType:      "selfInstanceType",
	}
	ergonTask := ergon.Task{
		LogicalTimestamp: nil,
		Uuid:             TestTaskUuid,
	}
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: nil,
		ParentTask:    &ergonTask,
	}
	mockZkSession := mockZk.NewMockZookeeperIfc(ctrl)
	m.ZkClient.EXPECT().GetZkSession().Return(mockZkSession).AnyTimes()
	TaskUpdateRet := &ergon.TaskUpdateRet{
		Task: nil,
	}
	m.ErgonService.On("TaskUpdate", mock.AnythingOfType("*ergon.TaskUpdateArg")).Return(TaskUpdateRet, nil)
	var registeredPeList = []string{"string1", "string2"}
	m.ZkSession.On("Children", RegisteredPeZkNodePath, true).Return(registeredPeList, nil, nil)
	m.ErgonService.On("TaskGet", TestTaskUuid).Return(TestTask, nil)
	m.ZkSession.On("Children", consts.AOSClusterCES, true).Return([]string{"uuid"}, nil, nil)
	m.ErgonService.On("TaskUpdate", mock.AnythingOfType("*ergon.TaskUpdateArg")).Return(nil, nil)
	path := BroadcastedZkPath + jobMetadata.RemoteClusterId
	statusMap := exchange_root_certificate.NewBroadcastStatusMap("remote-cluster-id")
	statusMap.StatusMap["string1"] = true
	statusMap.StatusMap["string2"] = true
	statusMapMarshal, _ := json.Marshal(statusMap)
	m.ZkSession.On("Get", path, true).Return(statusMapMarshal, nil, nil)
	getZkStat := zk.Stat{
		Version: 0,
	}
	m.ZkSession.On("Exist", path, true).Return(true, &getZkStat, nil)
	m.ZkSession.On("Set", path, mock.Anything, getZkStat.Version, true).Return(nil, nil)
	m.EventForwarder.EXPECT().PushEvent(
		gomock.Any(), consts.EventTypeRegisterDomainManager, jobMetadata.TaskId,
		consts.EventNamespace, consts.PulseDomainManagerEntity, jobMetadata.SelfClusterId)

	registerPCService := NewRegisterPcImpl()
	err := registerPCService.RunPostActions(jobVar)

	assert.Nil(t, err)
}

func TestConfigureEntities_callConfigureConnectionApiError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	var jobMetadata models.JobMetadata
	jobMetadata.RemoteAddress = "remote-address"
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: nil,
		ParentTask:    nil,
	}
	url := fmt.Sprintf(consts.V4ConfigureConnectionApiPath, consts.ApiVersion, jobMetadata.RemoteClusterId)
	m.Uuid.EXPECT().New().Return(uuid4.StringToUuid4(TestRequestID))
	headers := map[string]string{}
	headers[consts.NtnxRequestId] = TestRequestID

	m.RemoteRestClient.EXPECT().CallApiCertBasedAuth(jobMetadata.RemoteAddress, gomock.Any(), url, http.MethodPost, gomock.Any(), headers,
		nil, nil).Return(nil, nil, errors.New("error"))

	registerPCService := NewRegisterPcImpl()
	err := registerPCService.ConfigureEntities(jobVar)

	assert.Equal(t, guru_error.GetInternalError(consts.RegisterPCOpName), err)
}

func TestSetupTrust_GetRootCertificateError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	old := utils.OsReadFileFunc
	defer func() { utils.OsReadFileFunc = old }()
	utils.OsReadFileFunc = func(path string) ([]byte, error) {
		return nil, errors.New("error")
	}
	var jobMetadata models.JobMetadata
	jobMetadata.RemoteAddress = "remote-address"
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: nil,
		ParentTask:    nil,
	}

	registerPCService := NewRegisterPcImpl()
	err := registerPCService.SetupTrust(jobVar)

	assert.Equal(t, "error", err.Error())
}

func TestSetup_Trust_CallAddRootCertificateApiError(t *testing.T) {
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
	credentialMap := make(map[string]string)
	credentialMap["username"] = "username"
	credentialMap["password"] = "password"
	var jobMetadata models.JobMetadata
	jobMetadata.RemoteAddress = "remote-address"
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobMetadata.ContextId = "context-id"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
		ParentTask:    nil,
	}
	m.Uuid.EXPECT().New().Return(uuid4.StringToUuid4(TestRequestID))
	headers := map[string]string{}
	headers[consts.NtnxRequestId] = TestRequestID
	url := fmt.Sprintf(V4AddRootCertApiPath, consts.ApiVersion, jobMetadata.RemoteClusterId)
	m.RemoteRestClient.EXPECT().CallApiBasicAuth(jobMetadata.RemoteAddress, consts.EnvoyPort, url,
		http.MethodPost, gomock.Any(), headers, nil, nil,
		credentialMap["username"], credentialMap["password"]).Return(nil, nil, errors.New("error"))

	registerPCService := NewRegisterPcImpl()
	err := registerPCService.SetupTrust(jobVar)

	assert.Equal(t, guru_error.GetInternalError(consts.RegisterPCOpName), err)
}

func TestSetupTrust_Success(t *testing.T) {
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
	credentialMap := make(map[string]string)
	credentialMap["username"] = "username"
	credentialMap["password"] = "password"
	var jobMetadata models.JobMetadata = models.JobMetadata{
		Name:            consts.REGISTER_PC,
		RemoteAddress:   "127.0.0.1",
		RemoteClusterId: "remote-cluster-id",
		ContextId:       "context-id",
	}

	jobMetadata.RemoteAddress = "remote-address"
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobMetadata.ContextId = "context-id"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
		ParentTask: &ergon.Task{
			Uuid:             TestParentTaskUuid,
			LogicalTimestamp: &timeStamp,
		},
	}
	m.Uuid.EXPECT().New().Return(uuid4.StringToUuid4(TestRequestID))
	headers := map[string]string{}
	headers[consts.NtnxRequestId] = TestRequestID
	url := fmt.Sprintf(V4AddRootCertApiPath, consts.ApiVersion, jobMetadata.RemoteClusterId)
	m.RemoteRestClient.EXPECT().CallApiBasicAuth(jobMetadata.RemoteAddress, consts.EnvoyPort, url,
		http.MethodPost, gomock.Any(), headers, nil, nil,
		credentialMap["username"], credentialMap["password"]).Return(test.GetMockTaskReferenceModelResponse(), nil, nil)

	// Send Api request
	callApiUrl := consts.TasksUrl + "/" + "ZXJnb24=:278bbfc2-280f-529b-882b-14ea649b9a95"

	m.RemoteRestClient.EXPECT().CallApi(jobMetadata.RemoteAddress, consts.EnvoyPort,
		callApiUrl, http.MethodGet, nil, nil, nil, nil,
		nil, nil, true).Return(test.GetMockTaskSuccessResponse(), nil, nil).Times(2)

	registerPCService := NewRegisterPcImpl()
	err := registerPCService.SetupTrust(jobVar)

	assert.Nil(t, err)
}
