package registration_proxy

import (
	"encoding/json"
	"errors"
	"net/http"
	"net/url"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/external"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/test"
	"testing"

	ergon_proto "github.com/nutanix-core/go-cache/ergon"
	"github.com/nutanix-core/go-cache/util-go/uuid4"
	"github.com/nutanix-core/ntnx-api-guru/services/constants"
	mockErgon "github.com/nutanix-core/ntnx-api-guru/services/ergon/mocks"
	mockEvent "github.com/nutanix-core/ntnx-api-guru/services/events/mocks"
	mockGuruApi "github.com/nutanix-core/ntnx-api-guru/services/guru_api/mocks"
	mockZk "github.com/nutanix-core/ntnx-api-guru/services/zk/mocks"

	"github.com/golang/mock/gomock"
	"github.com/stretchr/testify/assert"
)

var TestParentTaskUuid = []byte{10, 204, 62, 133, 16, 76, 72, 134, 85, 211, 252, 41, 149, 19, 150, 228}
var TestTaskUuidString = uuid4.ToUuid4(TestParentTaskUuid).String()

type mockServices struct {
	ErgonClient      *mockErgon.MockErgonClientIfc
	ZkClient         *mockZk.MockZkClientIfc
	EventForwarder   *mockEvent.MockEventForwarderIfc
	remoteRestclient *mockGuruApi.MockGuruApiClientIfc
}

func mockSingletons(t *testing.T) *mockServices {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockErgonClient := mockErgon.NewMockErgonClientIfc(ctrl)
	mockZkClient := mockZk.NewMockZkClientIfc(ctrl)
	mockEventForwarder := mockEvent.NewMockEventForwarderIfc(ctrl)
	mockGuruApiClient := mockGuruApi.NewMockGuruApiClientIfc(ctrl)
	external.SetSingletonServices(mockErgonClient, nil, nil, mockZkClient, nil, nil, nil,
		nil, mockGuruApiClient, mockEventForwarder, nil, nil, nil, nil, nil, nil, nil)
	return &mockServices{
		ErgonClient:      mockErgonClient,
		ZkClient:         mockZkClient,
		EventForwarder:   mockEventForwarder,
		remoteRestclient: mockGuruApiClient,
	}
}

func TestNewRegistrationProxy(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	external.SetSingletonServices(nil, nil, nil, nil,
		nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil)
	_, err := New()
	assert.Nil(t, err)
}

func TestExecuteError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = true
	jobMetadata.ContextId = "context"
	jobMetadata.RemoteTaskId = ""
	jobMetadata.RemoteAddress = "Remote-address"
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.StepsToTaskMap[1] = TestParentTaskUuid
	jobMetadata.StepsToTaskMap[2] = TestParentTaskUuid
	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, ergon_proto.Task_kRunning,
		consts.RegistrationProxyStartMessage, nil, nil, nil, nil).Return(nil)
	responseCode := http.StatusUnauthorized
	m.RemoteRestClient.EXPECT().CallApi(
		jobMetadata.RemoteAddress, consts.EnvoyPort, consts.ProxyRegistrationUrl,
		http.MethodPost, gomock.Any(), nil, nil, nil, nil, nil, true,
	).Return(nil, &responseCode, errors.New("error"))
	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, ergon_proto.Task_kFailed, "", &constants.TASK_COMPLETED_PERCENTAGE,
		nil, gomock.Any(), nil).Return(nil)
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	registrationProxy, _ := New()
	err := registrationProxy.Execute(jobVar)
	assert.Equal(t, guru_error.GetUnauthorizedRequestError(consts.RegisterAosOpName), err)
}

func TestExecutePollTaskError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = true
	jobMetadata.ContextId = "context"
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.RemoteTaskId = ""
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.StepsToTaskMap[1] = TestParentTaskUuid
	jobMetadata.StepsToTaskMap[2] = TestParentTaskUuid
	resp := models.TaskIdDTO{
		TaskUuid: TestTaskUuidString,
	}
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	respBytes, _ := json.Marshal(resp)
	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, ergon_proto.Task_kRunning,
		consts.RegistrationProxyStartMessage, nil, nil, nil, nil).Return(nil)
	m.RemoteRestClient.EXPECT().CallApi(
		jobMetadata.RemoteAddress, consts.EnvoyPort, consts.ProxyRegistrationUrl,
		http.MethodPost, gomock.Any(), nil, nil, nil, nil, nil, true,
	).Return(respBytes, nil, nil)
	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, consts.TaskStatusNone, "",
		nil, gomock.Any(), nil, nil).Return(nil)
	m.ErgonClient.EXPECT().PollTask(TestParentTaskUuid, consts.ErgonPollTimeout).Return(nil, errors.New("error"))
	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, ergon_proto.Task_kFailed, "",
		&constants.TASK_COMPLETED_PERCENTAGE, nil, gomock.Any(),
		nil).Return(nil)
	registrationProxy, _ := New()
	err := registrationProxy.Execute(jobVar)
	assert.Equal(t, errors.New("error"), err)
}

func TestExecuteTaskNotSucceeded(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = true
	jobMetadata.ContextId = "context"
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.RemoteTaskId = ""
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.StepsToTaskMap[1] = TestParentTaskUuid
	jobMetadata.StepsToTaskMap[2] = TestParentTaskUuid
	resp := models.TaskIdDTO{
		TaskUuid: TestTaskUuidString,
	}
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	respBytes, _ := json.Marshal(resp)
	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, ergon_proto.Task_kRunning,
		consts.RegistrationProxyStartMessage, nil, nil, nil, nil).Return(nil)
	m.RemoteRestClient.EXPECT().CallApi(
		jobMetadata.RemoteAddress, consts.EnvoyPort, consts.ProxyRegistrationUrl,
		http.MethodPost, gomock.Any(), nil, nil, nil, nil, nil, true,
	).Return(respBytes, nil, nil)
	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, consts.TaskStatusNone, "",
		nil, gomock.Any(), nil, nil).Return(nil)
	var status = ergon_proto.Task_kFailed
	m.ErgonClient.EXPECT().PollTask(TestParentTaskUuid, consts.ErgonPollTimeout).Return(&status, nil)
	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, ergon_proto.Task_kFailed, "",
		&constants.TASK_COMPLETED_PERCENTAGE, nil, gomock.Any(),
		nil).Return(nil)
	registrationProxy, _ := New()
	err := registrationProxy.Execute(jobVar)
	assert.Nil(t, err)
}

func TestExecuteParentTaskUpdateFailed(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = true
	jobMetadata.ContextId = "context"
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.RemoteTaskId = ""
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.StepsToTaskMap[1] = TestParentTaskUuid
	jobMetadata.StepsToTaskMap[2] = TestParentTaskUuid
	resp := models.TaskIdDTO{
		TaskUuid: TestTaskUuidString,
	}
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	respBytes, _ := json.Marshal(resp)
	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, ergon_proto.Task_kRunning,
		consts.RegistrationProxyStartMessage, nil, nil, nil, nil).Return(nil)
	m.RemoteRestClient.EXPECT().CallApi(
		jobMetadata.RemoteAddress, consts.EnvoyPort, consts.ProxyRegistrationUrl,
		http.MethodPost, gomock.Any(), nil, nil, nil, nil, nil, true,
	).Return(respBytes, nil, nil)
	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, consts.TaskStatusNone, "",
		nil, gomock.Any(), nil, nil).Return(nil)
	var status = ergon_proto.Task_kSucceeded
	m.ErgonClient.EXPECT().PollTask(TestParentTaskUuid, consts.ErgonPollTimeout).Return(&status, nil)
	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, ergon_proto.Task_kSucceeded, consts.RegistrationProxyCompleteMessage,
		nil, nil, nil, nil).Return(errors.New("error"))
	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, ergon_proto.Task_kFailed, "",
		&constants.TASK_COMPLETED_PERCENTAGE, nil, gomock.Any(),
		nil).Return(nil)
	registrationProxy, _ := New()
	err := registrationProxy.Execute(jobVar)
	assert.Equal(t, errors.New("error"), err)
}

func TestExecute(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = true
	jobMetadata.ContextId = "context"
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.RemoteTaskId = ""
	jobMetadata.RemoteClusterId = ""
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.StepsToTaskMap[1] = TestParentTaskUuid
	jobMetadata.StepsToTaskMap[2] = TestParentTaskUuid
	resp := models.TaskIdDTO{
		TaskUuid: TestTaskUuidString,
	}
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	respBytes, _ := json.Marshal(resp)
	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, ergon_proto.Task_kRunning,
		consts.RegistrationProxyStartMessage, nil, nil, nil, nil).Return(nil)
	m.RemoteRestClient.EXPECT().CallApi(
		jobMetadata.RemoteAddress, consts.EnvoyPort, consts.ProxyRegistrationUrl,
		http.MethodPost, gomock.Any(), nil, nil, nil, nil, nil, true,
	).Return(respBytes, nil, nil)
	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, consts.TaskStatusNone, "",
		nil, gomock.Any(), nil, nil).Return(nil)
	var status = ergon_proto.Task_kSucceeded
	m.ErgonClient.EXPECT().PollTask(TestParentTaskUuid, consts.ErgonPollTimeout).Return(&status, nil)
	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, ergon_proto.Task_kSucceeded, consts.RegistrationProxyCompleteMessage,
		nil, nil, nil, nil).Return(nil)
	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, ergon_proto.Task_kSucceeded, consts.RegistrationSuccessMessage,
		nil, gomock.Any(), nil, nil).Return(nil)
	m.EventForwarder.EXPECT().PushEvent(gomock.Any(), consts.EventTypeRegisterAOS, gomock.Any(), consts.EventNamespace, consts.PulseDomainManagerEntity, gomock.Any()).Return(errors.New("error"))
	registrationProxy, _ := New()
	err := registrationProxy.Execute(jobVar)
	assert.Nil(t, err)
}

func TestDeleteClusterStateError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = true
	jobMetadata.ContextId = "context"
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	uri := consts.ClusterExternalStateUrl + "/" + jobMetadata.RemoteClusterId
	responseCode := http.StatusUnauthorized
	m.RemoteRestClient.EXPECT().CallApi(
		consts.LocalHost, consts.EnvoyPort, uri,
		http.MethodDelete, gomock.Any(), gomock.Any(), gomock.Any(), nil, nil, nil, true).Return(nil, &responseCode, errors.New("error"))
	registrationProxy, _ := New()
	res, err := registrationProxy.DeleteClusterState(jobVar)
	assert.Nil(t, res)
	assert.Equal(t, guru_error.GetUnauthorizedRequestError(consts.RegisterAosOpName), err)
}

func TestDeleteClusterStateUnMarshalError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = true
	jobMetadata.ContextId = "context"
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	uri := consts.ClusterExternalStateUrl + "/" + jobMetadata.RemoteClusterId
	m.RemoteRestClient.EXPECT().CallApi(
		consts.LocalHost, consts.EnvoyPort, uri,
		http.MethodDelete, gomock.Any(), gomock.Any(), gomock.Any(), nil, nil, nil, true).Return(TestParentTaskUuid, nil, nil)
	registrationProxy, _ := New()
	res, _ := registrationProxy.DeleteClusterState(jobVar)
	assert.Nil(t, res)
}

func TestDeleteClusterStateSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = true
	jobMetadata.ContextId = "context"
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	uri := consts.ClusterExternalStateUrl + "/" + jobMetadata.RemoteClusterId
	resp := models.PrimitiveDTO{
		Value: true,
	}
	respBytes, _ := json.Marshal(resp)
	queryParams := url.Values{}
	queryParams.Add("skipCleanups", "false")
	queryParams.Add("skipPrechecks", "true")
	m.RemoteRestClient.EXPECT().CallApi(
		consts.LocalHost, consts.EnvoyPort, uri,
		http.MethodDelete, nil, gomock.Any(), queryParams, nil, nil, nil, true).Return(respBytes, nil, nil)
	registrationProxy, _ := New()
	res, err := registrationProxy.DeleteClusterState(jobVar)
	expectedRes := true
	assert.Equal(t, &expectedRes, res)
	assert.Nil(t, err)
}

func TestTriggerRegisterOnPeError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = true
	jobMetadata.ContextId = "context"
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobMetadata.RemoteAddress = "remote-address"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	responseCode := http.StatusUnauthorized
	m.RemoteRestClient.EXPECT().CallApi(
		jobMetadata.RemoteAddress, consts.EnvoyPort, consts.ProxyRegistrationUrl,
		http.MethodPost, gomock.Any(), nil, nil, nil, nil, nil, true,
	).Return(nil, &responseCode, errors.New("error"))
	registrationProxy, _ := New()
	res, err := registrationProxy.TriggerRegisterOnPe(jobVar)
	assert.Equal(t, "", res)
	assert.Equal(t, guru_error.GetUnauthorizedRequestError(consts.RegisterAosOpName), err)
}

func TestTriggerRegisterOnPeUnMarshalError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = true
	jobMetadata.ContextId = "context"
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobMetadata.RemoteAddress = "remote-address"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	m.RemoteRestClient.EXPECT().CallApi(
		jobMetadata.RemoteAddress, consts.EnvoyPort, consts.ProxyRegistrationUrl,
		http.MethodPost, gomock.Any(), nil, nil, nil, nil, nil, true,
	).Return(TestParentTaskUuid, nil, nil)
	registrationProxy, _ := New()
	res, _ := registrationProxy.TriggerRegisterOnPe(jobVar)
	assert.Equal(t, "", res)
}

func TestTriggerRegisterOnPeSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = true
	jobMetadata.ContextId = "context"
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobMetadata.RemoteAddress = "remote-address"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	resp := models.TaskIdDTO{
		TaskUuid: TestTaskUuidString,
	}
	respBytes, _ := json.Marshal(resp)
	m.RemoteRestClient.EXPECT().CallApi(
		jobMetadata.RemoteAddress, consts.EnvoyPort, consts.ProxyRegistrationUrl,
		http.MethodPost, gomock.Any(), nil, nil, nil, nil, nil, true,
	).Return(respBytes, nil, nil)
	registrationProxy, _ := New()
	res, err := registrationProxy.TriggerRegisterOnPe(jobVar)
	assert.Equal(t, TestTaskUuidString, res)
	assert.Nil(t, err)
}

func TestRollback(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = true
	jobMetadata.ContextId = "context"
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	responseCode := http.StatusUnauthorized
	uri := consts.ClusterExternalStateUrl + "/" + jobMetadata.RemoteClusterId
	queryParams := url.Values{}
	queryParams.Add("skipCleanups", "false")
	queryParams.Add("skipPrechecks", "true")
	m.RemoteRestClient.EXPECT().CallApi(
		consts.LocalHost, consts.EnvoyPort, uri,
		http.MethodDelete, nil, gomock.Any(), queryParams, nil, nil, nil, true).Return(nil, &responseCode, errors.New("error"))
	registrationProxy, _ := New()
	registrationProxy.Rollback(jobVar)
}
