package registration_precheck

import (
	"encoding/json"
	"errors"
	"fmt"
	"net/http"
	"net/url"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/external"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/services/register_aos/registration_common"
	"ntnx-api-guru-pc/guru-pc-api-service/test"
	"testing"

	"github.com/golang/mock/gomock"
	"github.com/golang/protobuf/proto"
	ergon_proto "github.com/nutanix-core/go-cache/ergon"
	ergonClientMocks "github.com/nutanix-core/go-cache/ergon/client/mocks"
	"github.com/nutanix-core/go-cache/util-go/uuid4"
	zeus_config "github.com/nutanix-core/go-cache/zeus/config"
	common_consts "github.com/nutanix-core/ntnx-api-guru/services/constants"
	mockErgon "github.com/nutanix-core/ntnx-api-guru/services/ergon/mocks"
	mockEvent "github.com/nutanix-core/ntnx-api-guru/services/events/mocks"
	mockGuruApi "github.com/nutanix-core/ntnx-api-guru/services/guru_api/mocks"
	mockZk "github.com/nutanix-core/ntnx-api-guru/services/zk/mocks"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
)

var TestParentTaskUuid = []byte{10, 204, 62, 133, 16, 76, 72, 134, 85, 211, 252, 41, 149, 19, 150, 228}

func TestNewRegistrationPrecheck(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	external.SetSingletonServices(nil, nil, nil, nil,
		nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil)
	_, err := New()
	assert.Equal(t, nil, err)
}

type mockServices struct {
	ErgonClient      *mockErgon.MockErgonClientIfc
	ZkClient         *mockZk.MockZkClientIfc
	EventForwarder   *mockEvent.MockEventForwarderIfc
	remoteRestclient *mockGuruApi.MockGuruApiClientIfc
	ErgonService     *ergonClientMocks.Ergon
}

func mockSingletons(t *testing.T) *mockServices {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockErgonClient := mockErgon.NewMockErgonClientIfc(ctrl)
	mockZkClient := mockZk.NewMockZkClientIfc(ctrl)
	mockEventForwarder := mockEvent.NewMockEventForwarderIfc(ctrl)
	mockGuruApiClient := mockGuruApi.NewMockGuruApiClientIfc(ctrl)
	mockErgonService := new(ergonClientMocks.Ergon)

	external.SetSingletonServices(mockErgonClient, nil, nil, mockZkClient, nil, nil, nil,
		nil, mockGuruApiClient, mockEventForwarder, nil, nil, mockErgonService, nil, nil, nil, nil)
	return &mockServices{
		ErgonClient:      mockErgonClient,
		ZkClient:         mockZkClient,
		EventForwarder:   mockEventForwarder,
		remoteRestclient: mockGuruApiClient,
		ErgonService:     mockErgonService,
	}
}

func TestExecuteRecoveredTask(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = true
	jobMetadata.ContextId = "context"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	registrationCommonUtil, _ := registration_common.New()

	registrationPrecheck := &RegistrationPrecheck{
		registration_common_util: registrationCommonUtil,
	}
	res := registrationPrecheck.Execute(jobVar)
	assert.Equal(t, consts.ErrorRollbackRecoverTask, res)
}

func TestExecute(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = false
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.ContextId = "context"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, ergon_proto.Task_kRunning, "", nil,
		nil, nil, nil).Return(nil)
	m.ErgonClient.EXPECT().UpdateTask(gomock.Any(), ergon_proto.Task_kRunning, consts.RegistrationPrecheckStartMessage,
		nil, nil, nil, nil).Return(nil)
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(nil, errors.New("error"))
	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, ergon_proto.Task_kFailed,
		"", &common_consts.TASK_COMPLETED_PERCENTAGE,
		nil, gomock.Any(), nil).Return(nil)
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	res := registrationPrecheck.Execute(jobVar)
	assert.Equal(t, guru_error.GetInternalError(consts.RegisterAosOpName), res)
}

func TestExecuteLocalPrecheckError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = false
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.ContextId = "context-id"
	jobMetadata.UseTrust = true
	jobMetadata.RemoteAddress = "remote-address"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	timeStamp := int64(1)
	nodeList := zeus_config.NodeList{}
	node := zeus_config.ConfigurationProto_Node{
		SoftwareVersion: proto.String("version"),
		ServiceVmId:     proto.Int64(1),
	}
	nodeList = append(nodeList, &node)
	clusterUuid := "cluster-uuid"
	configProto := zeus_config.ConfigurationProto{
		ClusterUuid:      &clusterUuid,
		LogicalTimestamp: &timeStamp,
		NodeList:         nodeList,
	}
	configProtoBytes, _ := proto.Marshal(&configProto)
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(configProtoBytes, nil)
	cluster := models.ClusterRegistrationPrecheckDTO{
		IsUpgrading:                    false,
		IsNTPSet:                       true,
		IsVersionCompatible:            true,
		ClusterUuid:                    "some-uuid",
		ClusterVersion:                 "some-version",
		IsPcDeploymentInProgress:       false,
		IsAOSVersionCompatibleWithPCDR: true,
		IsUnregistrationInProgress:     false,
		IsRegistrationInProgress:       false,
		IsRegistered:                   false,
		IsClusterBlacklistedOnPE:       false,
		IsClusterBlacklistedOnPC:       false,
	}
	clusterBytes, _ := json.Marshal(cluster)
	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, ergon_proto.Task_kRunning, "", nil,
		nil, nil, nil).Return(nil)
	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, ergon_proto.Task_kRunning, consts.RegistrationPrecheckStartMessage,
		nil, nil, nil, nil).Return(nil)
	m.RemoteRestClient.EXPECT().CallApi(
		jobMetadata.RemoteAddress, consts.EnvoyPort, consts.RegistrationPrecheckUrl, http.MethodGet, nil,
		gomock.Any(), gomock.Any(), nil, gomock.Any(), gomock.Any(), true).Return(clusterBytes, nil, nil)
	m.ErgonClient.EXPECT().UpdateTask(
		TestParentTaskUuid, consts.TaskStatusNone,
		consts.RegistrationAOSPrecheckCompleteMessage,
		&common_consts.TASK_COMPLETED_FIFTY_PERCENTAGE, nil,
		nil, nil,
	).Return(nil)
	responseCode := http.StatusUnauthorized
	m.RemoteRestClient.EXPECT().CallApi(
		consts.LocalHost, consts.EnvoyPort, consts.RegistrationPrecheckUrl, http.MethodGet, nil,
		gomock.Any(), gomock.Any(), nil, nil, nil, true).Return(nil, &responseCode, errors.New("error"))
	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, ergon_proto.Task_kFailed,
		"", gomock.Any(), nil, gomock.Any(),
		nil).Return(nil)
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	res := registrationPrecheck.Execute(jobVar)
	assert.Equal(t, guru_error.GetUnauthorizedRequestError(consts.RegisterAosOpName), res)
}

func TestExecuteUpdateTaskFailed(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = false
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.ContextId = "context-id"
	jobMetadata.UseTrust = true
	jobMetadata.RemoteAddress = "remote-address"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	timeStamp := int64(1)
	nodeList := zeus_config.NodeList{}
	node := zeus_config.ConfigurationProto_Node{
		SoftwareVersion: proto.String("version"),
		ServiceVmId:     proto.Int64(1),
	}
	nodeList = append(nodeList, &node)
	clusterUuid := "cluster-uuid"
	configProto := zeus_config.ConfigurationProto{
		ClusterUuid:      &clusterUuid,
		LogicalTimestamp: &timeStamp,
		NodeList:         nodeList,
	}
	configProtoBytes, _ := proto.Marshal(&configProto)
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(configProtoBytes, nil)
	cluster := models.ClusterRegistrationPrecheckDTO{
		IsUpgrading:                    false,
		IsNTPSet:                       true,
		IsVersionCompatible:            true,
		ClusterUuid:                    "some-uuid",
		ClusterVersion:                 "some-version",
		IsPcDeploymentInProgress:       false,
		IsAOSVersionCompatibleWithPCDR: true,
		IsUnregistrationInProgress:     false,
		IsRegistrationInProgress:       false,
		IsRegistered:                   false,
		IsClusterBlacklistedOnPE:       false,
		IsClusterBlacklistedOnPC:       false,
	}
	clusterBytes, _ := json.Marshal(cluster)
	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, ergon_proto.Task_kRunning, "", nil,
		nil, nil, nil).Return(nil)
	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, ergon_proto.Task_kRunning, consts.RegistrationPrecheckStartMessage,
		nil, nil, nil, nil).Return(nil)
	m.RemoteRestClient.EXPECT().CallApi(
		jobMetadata.RemoteAddress, consts.EnvoyPort, consts.RegistrationPrecheckUrl, http.MethodGet, nil,
		gomock.Any(), gomock.Any(), nil, gomock.Any(), gomock.Any(), true).Return(clusterBytes, nil, nil)
	zkNodePath := common_consts.CLUSTER_EXTERNAL_STATE_PATH + "/" + cluster.ClusterUuid
	m.ZkClient.EXPECT().ExistZkNode(zkNodePath, true).Return(false, nil)
	var emptyTaskList [][]byte
	m.ErgonClient.EXPECT().ListTask(nil, gomock.Any(), nil).Return(emptyTaskList, nil)
	zkNodePath = common_consts.REGISTRATION_MARKER_NODE_PATH + "/" + "some-uuid"
	m.ZkClient.EXPECT().ExistZkNode(zkNodePath, true).Return(false, nil)
	m.ErgonClient.EXPECT().UpdateTask(
		TestParentTaskUuid, consts.TaskStatusNone,
		consts.RegistrationAOSPrecheckCompleteMessage,
		&common_consts.TASK_COMPLETED_FIFTY_PERCENTAGE, nil,
		nil, nil,
	).Return(nil)
	cluster = models.ClusterRegistrationPrecheckDTO{
		IsUpgrading:                    false,
		IsNTPSet:                       false,
		IsVersionCompatible:            true,
		ClusterUuid:                    "",
		ClusterVersion:                 "",
		IsPcDeploymentInProgress:       false,
		IsAOSVersionCompatibleWithPCDR: false,
		IsUnregistrationInProgress:     false,
		IsRegistrationInProgress:       false,
		IsRegistered:                   false,
		IsClusterBlacklistedOnPE:       false,
		IsClusterBlacklistedOnPC:       false,
	}
	clusterBytes, _ = json.Marshal(cluster)
	m.RemoteRestClient.EXPECT().CallApi(
		consts.LocalHost, consts.EnvoyPort, consts.RegistrationPrecheckUrl, http.MethodGet, nil,
		gomock.Any(), gomock.Any(), nil, nil, nil, true).Return(clusterBytes, nil, nil)
	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, ergon_proto.Task_kSucceeded,
		consts.RegistrationPrecheckCompleteMessage, gomock.Any(), nil, gomock.Any(),
		nil).Return(errors.New("error"))
	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, ergon_proto.Task_kFailed, "",
		gomock.Any(), nil, gomock.Any(),
		nil).Return(nil)
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	res := registrationPrecheck.Execute(jobVar)
	assert.Equal(t, errors.New("error"), res)
}

func TestExecuteCreateRegistrationMarkerNodeError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = false
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.ContextId = "context-id"
	jobMetadata.UseTrust = true
	jobMetadata.RemoteAddress = "remote-address"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	timeStamp := int64(1)
	nodeList := zeus_config.NodeList{}
	node := zeus_config.ConfigurationProto_Node{
		SoftwareVersion: proto.String("version"),
		ServiceVmId:     proto.Int64(1),
	}
	nodeList = append(nodeList, &node)
	clusterUuid := "cluster-uuid"
	configProto := zeus_config.ConfigurationProto{
		ClusterUuid:      &clusterUuid,
		LogicalTimestamp: &timeStamp,
		NodeList:         nodeList,
	}
	configProtoBytes, _ := proto.Marshal(&configProto)
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(configProtoBytes, nil)
	cluster := models.ClusterRegistrationPrecheckDTO{
		IsUpgrading:                    false,
		IsNTPSet:                       true,
		IsVersionCompatible:            true,
		ClusterUuid:                    "some-uuid",
		ClusterVersion:                 "some-version",
		IsPcDeploymentInProgress:       false,
		IsAOSVersionCompatibleWithPCDR: true,
		IsUnregistrationInProgress:     false,
		IsRegistrationInProgress:       false,
		IsRegistered:                   false,
		IsClusterBlacklistedOnPE:       false,
		IsClusterBlacklistedOnPC:       false,
	}
	clusterBytes, _ := json.Marshal(cluster)
	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, ergon_proto.Task_kRunning, "", nil,
		nil, nil, nil).Return(nil)
	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, ergon_proto.Task_kRunning, consts.RegistrationPrecheckStartMessage,
		nil, nil, nil, nil).Return(nil)
	m.RemoteRestClient.EXPECT().CallApi(
		jobMetadata.RemoteAddress, consts.EnvoyPort, consts.RegistrationPrecheckUrl, http.MethodGet, nil,
		gomock.Any(), gomock.Any(), nil, gomock.Any(), gomock.Any(), true).Return(clusterBytes, nil, nil)
	zkNodePath := common_consts.CLUSTER_EXTERNAL_STATE_PATH + "/" + cluster.ClusterUuid
	m.ZkClient.EXPECT().ExistZkNode(zkNodePath, true).Return(false, nil)
	var emptyTaskList [][]byte
	m.ErgonClient.EXPECT().ListTask(nil, gomock.Any(), nil).Return(emptyTaskList, nil)
	zkNodePath = common_consts.REGISTRATION_MARKER_NODE_PATH + "/" + "some-uuid"
	m.ZkClient.EXPECT().ExistZkNode(zkNodePath, true).Return(false, nil)
	m.ErgonClient.EXPECT().UpdateTask(
		TestParentTaskUuid, consts.TaskStatusNone,
		consts.RegistrationAOSPrecheckCompleteMessage,
		&common_consts.TASK_COMPLETED_FIFTY_PERCENTAGE, nil,
		nil, nil,
	).Return(nil)
	cluster = models.ClusterRegistrationPrecheckDTO{
		IsUpgrading:                    false,
		IsNTPSet:                       false,
		IsVersionCompatible:            true,
		ClusterUuid:                    "some-uuid",
		ClusterVersion:                 "some-version",
		IsPcDeploymentInProgress:       false,
		IsAOSVersionCompatibleWithPCDR: false,
		IsUnregistrationInProgress:     false,
		IsRegistrationInProgress:       false,
		IsRegistered:                   false,
		IsClusterBlacklistedOnPE:       false,
		IsClusterBlacklistedOnPC:       false,
	}
	clusterBytes, _ = json.Marshal(cluster)
	m.RemoteRestClient.EXPECT().CallApi(
		consts.LocalHost, consts.EnvoyPort, consts.RegistrationPrecheckUrl, http.MethodGet, nil,
		gomock.Any(), gomock.Any(), nil, gomock.Any(), gomock.Any(), true).Return(clusterBytes, nil, nil)
	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, ergon_proto.Task_kSucceeded,
		consts.RegistrationPrecheckCompleteMessage, gomock.Any(), nil, gomock.Any(),
		nil).Return(nil)
	zkPath := common_consts.REGISTRATION_MARKER_NODE_PATH + "/" + cluster.ClusterUuid + "/" + jobMetadata.TaskId
	m.ZkClient.EXPECT().CreateRecursive(zkPath, nil, true).Return(errors.New("error"))
	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, ergon_proto.Task_kFailed,
		"", gomock.Any(), nil, gomock.Any(), nil).Return(nil)
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	res := registrationPrecheck.Execute(jobVar)
	assert.Equal(t, errors.New("error"), res)
}

func TestFetchPCDetailsGetZkNodeError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = false
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.ContextId = "context-id"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(nil, errors.New("error"))
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	res1, res2, err := registrationPrecheck.FetchPCDetails(jobVar)
	assert.Equal(t, "", res1)
	assert.Equal(t, "", res2)
	assert.Equal(t, errors.New("error"), err)
}

func TestFetchPCDetailsUnMarshalError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = false
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.ContextId = "context-id"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(TestParentTaskUuid, nil)
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	res1, res2, _ := registrationPrecheck.FetchPCDetails(jobVar)
	assert.Equal(t, "", res1)
	assert.Equal(t, "", res2)
}

func TestFetchPCDetailsClusterUuidNil(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = false
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.ContextId = "context-id"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	configProto := zeus_config.ConfigurationProto{
		ClusterUuid:      nil,
		LogicalTimestamp: proto.Int64(1),
	}
	configProtoBytes, _ := proto.Marshal(&configProto)
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(configProtoBytes, nil)
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	res1, res2, err := registrationPrecheck.FetchPCDetails(jobVar)
	assert.Equal(t, "", res1)
	assert.Equal(t, "", res2)
	assert.Equal(t, consts.ErrorZeusClusterUuidNil, err)
}

func TestFetchPCDetailsZeusVersionNil(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = false
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.ContextId = "context-id"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	configProto := zeus_config.ConfigurationProto{
		ClusterUuid:      proto.String("cluster-uuid"),
		LogicalTimestamp: proto.Int64(1),
	}
	configProtoBytes, _ := proto.Marshal(&configProto)
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(configProtoBytes, nil)
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	res1, res2, err := registrationPrecheck.FetchPCDetails(jobVar)
	assert.Equal(t, "", res1)
	assert.Equal(t, "", res2)
	assert.Equal(t, consts.ErrorZeusVersionNil, err)
}

func TestFetchPCDetailsSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = false
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.ContextId = "context-id"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	timeStamp := int64(1)
	nodeList := zeus_config.NodeList{}
	node := zeus_config.ConfigurationProto_Node{
		SoftwareVersion: proto.String("version"),
		ServiceVmId:     proto.Int64(1),
	}
	nodeList = append(nodeList, &node)
	clusterUuid := "cluster-uuid"
	configProto := zeus_config.ConfigurationProto{
		ClusterUuid:      &clusterUuid,
		LogicalTimestamp: &timeStamp,
		NodeList:         nodeList,
	}
	configProtoBytes, _ := proto.Marshal(&configProto)
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(configProtoBytes, nil)
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	res1, res2, err := registrationPrecheck.FetchPCDetails(jobVar)
	assert.Equal(t, "cluster-uuid", res1)
	assert.Equal(t, "version", res2)
	assert.Nil(t, err)
}

func TestExecuteLocalPrecheckMakeLocalPCPrecheckError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = false
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.RemoteAddress = ""
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.UseTrust = false
	jobMetadata.ContextId = "context-id"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	res := registrationPrecheck.ExecuteLocalPrecheck(jobVar)
	assert.Equal(t, guru_error.GetInternalError(consts.RegisterAosOpName), res)
}

func TestExecuteLocalPrecheckValidateError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = false
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.RemoteAddress = ""
	jobMetadata.RemoteClusterId = "cluster-id"
	jobMetadata.RemoteVersion = "remote-version"
	jobMetadata.UseTrust = true
	jobMetadata.ContextId = "context-id"
	jobMetadata.SelfClusterVersion = "local-version"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	uri := consts.RegistrationPrecheckUrl
	port := consts.EnvoyPort
	method := http.MethodGet
	cluster := models.ClusterRegistrationPrecheckDTO{
		IsUpgrading:                    false,
		IsNTPSet:                       false,
		IsVersionCompatible:            false,
		ClusterUuid:                    "",
		ClusterVersion:                 "",
		IsPcDeploymentInProgress:       false,
		IsAOSVersionCompatibleWithPCDR: false,
		IsUnregistrationInProgress:     false,
		IsRegistrationInProgress:       false,
		IsRegistered:                   false,
		IsClusterBlacklistedOnPE:       false,
		IsClusterBlacklistedOnPC:       false,
	}
	clusterBytes, _ := json.Marshal(cluster)
	m.RemoteRestClient.EXPECT().CallApi(
		consts.LocalHost, port, uri, method, nil,
		gomock.Any(), gomock.Any(), nil, nil, nil, true).Return(clusterBytes, nil, nil)
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	res := registrationPrecheck.ExecuteLocalPrecheck(jobVar)
	errorMsg := "Prism Central with version local-version is incompatible with the remote cluster with version remote-version"
	assert.Equal(t, guru_error.GetPreconditionFailureError(errorMsg, consts.RegisterAosOpName), res)
}

func TestExecuteLocalPrecheckAlreadyregistered(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = false
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.RemoteAddress = ""
	jobMetadata.RemoteClusterId = "cluster-id"
	jobMetadata.RemoteVersion = "remote-version"
	jobMetadata.UseTrust = true
	jobMetadata.ContextId = "context-id"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	uri := consts.RegistrationPrecheckUrl
	port := consts.EnvoyPort
	method := http.MethodGet
	cluster := models.ClusterRegistrationPrecheckDTO{
		IsUpgrading:                    false,
		IsNTPSet:                       false,
		IsVersionCompatible:            true,
		ClusterUuid:                    "",
		ClusterVersion:                 "",
		IsPcDeploymentInProgress:       false,
		IsAOSVersionCompatibleWithPCDR: false,
		IsUnregistrationInProgress:     false,
		IsRegistrationInProgress:       false,
		IsRegistered:                   false,
		IsClusterBlacklistedOnPE:       false,
		IsClusterBlacklistedOnPC:       false,
	}
	clusterBytes, _ := json.Marshal(cluster)
	m.RemoteRestClient.EXPECT().CallApi(
		consts.LocalHost, port, uri, method, nil,
		gomock.Any(), gomock.Any(), nil, nil, nil, true).Return(clusterBytes, nil, nil)
	zkNodePath := common_consts.CLUSTER_EXTERNAL_STATE_PATH + "/" + jobMetadata.RemoteClusterId
	m.ZkClient.EXPECT().ExistZkNode(zkNodePath, true).Return(true, nil)
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	res := registrationPrecheck.ExecuteLocalPrecheck(jobVar)
	assert.Equal(t, guru_error.GetPreconditionFailureError(consts.ErrorPCAlreadyRegistered.Error(), consts.RegisterAosOpName), res)
}

func TestExecuteLocalPrecheckAlreadyregisteredError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = false
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.RemoteAddress = ""
	jobMetadata.RemoteClusterId = "cluster-id"
	jobMetadata.RemoteVersion = "remote-version"
	jobMetadata.UseTrust = true
	jobMetadata.ContextId = "context-id"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	uri := consts.RegistrationPrecheckUrl
	port := consts.EnvoyPort
	method := http.MethodGet
	cluster := models.ClusterRegistrationPrecheckDTO{
		IsUpgrading:                    false,
		IsNTPSet:                       false,
		IsVersionCompatible:            true,
		ClusterUuid:                    "",
		ClusterVersion:                 "",
		IsPcDeploymentInProgress:       false,
		IsAOSVersionCompatibleWithPCDR: false,
		IsUnregistrationInProgress:     false,
		IsRegistrationInProgress:       false,
		IsRegistered:                   false,
		IsClusterBlacklistedOnPE:       false,
		IsClusterBlacklistedOnPC:       false,
	}
	clusterBytes, _ := json.Marshal(cluster)
	m.RemoteRestClient.EXPECT().CallApi(
		consts.LocalHost, port, uri, method, nil,
		gomock.Any(), gomock.Any(), nil, nil, nil, true).Return(clusterBytes, nil, nil)
	zkNodePath := common_consts.CLUSTER_EXTERNAL_STATE_PATH + "/" + jobMetadata.RemoteClusterId
	m.ZkClient.EXPECT().ExistZkNode(zkNodePath, true).Return(false, errors.New("error"))
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	res := registrationPrecheck.ExecuteLocalPrecheck(jobVar)
	assert.Equal(t, guru_error.GetInternalError(consts.RegisterAosOpName), res)
}

func TestExecuteLocalPrecheckScaleOutInProgress(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = false
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.RemoteAddress = ""
	jobMetadata.RemoteClusterId = "cluster-id"
	jobMetadata.RemoteVersion = "remote-version"
	jobMetadata.UseTrust = true
	jobMetadata.ContextId = "context-id"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	uri := consts.RegistrationPrecheckUrl
	port := consts.EnvoyPort
	method := http.MethodGet
	cluster := models.ClusterRegistrationPrecheckDTO{
		IsUpgrading:                    false,
		IsNTPSet:                       false,
		IsVersionCompatible:            true,
		ClusterUuid:                    "",
		ClusterVersion:                 "",
		IsPcDeploymentInProgress:       false,
		IsAOSVersionCompatibleWithPCDR: false,
		IsUnregistrationInProgress:     false,
		IsRegistrationInProgress:       false,
		IsRegistered:                   false,
		IsClusterBlacklistedOnPE:       false,
		IsClusterBlacklistedOnPC:       false,
	}
	clusterBytes, _ := json.Marshal(cluster)
	m.RemoteRestClient.EXPECT().CallApi(
		consts.LocalHost, port, uri, method, nil,
		gomock.Any(), gomock.Any(), nil, nil, nil, true).Return(clusterBytes, nil, nil)
	zkNodePath := common_consts.CLUSTER_EXTERNAL_STATE_PATH + "/" + jobMetadata.RemoteClusterId
	m.ZkClient.EXPECT().ExistZkNode(zkNodePath, true).Return(false, nil)
	var ret [][]byte
	ret = append(ret, TestParentTaskUuid)
	m.ErgonClient.EXPECT().ListTask(nil, gomock.Any(), nil).Return(ret, nil)
	taskStatusRunning := ergon_proto.Task_Status(2)
	var ergonTask = ergon_proto.Task{Status: &taskStatusRunning}
	m.ErgonClient.EXPECT().GetTaskByUuid(TestParentTaskUuid).Return(&ergonTask, nil)
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	res := registrationPrecheck.ExecuteLocalPrecheck(jobVar)
	assert.Equal(t, guru_error.GetPreconditionFailureError(consts.ErrorPCIsScaleoutInProgress.Error(), consts.RegisterAosOpName), res)
}

func TestExecuteLocalPrecheckScaleOutInProgressError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = false
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.RemoteAddress = ""
	jobMetadata.RemoteClusterId = "cluster-id"
	jobMetadata.RemoteVersion = "remote-version"
	jobMetadata.UseTrust = true
	jobMetadata.ContextId = "context-id"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	uri := consts.RegistrationPrecheckUrl
	port := consts.EnvoyPort
	method := http.MethodGet
	cluster := models.ClusterRegistrationPrecheckDTO{
		IsUpgrading:                    false,
		IsNTPSet:                       false,
		IsVersionCompatible:            true,
		ClusterUuid:                    "",
		ClusterVersion:                 "",
		IsPcDeploymentInProgress:       false,
		IsAOSVersionCompatibleWithPCDR: false,
		IsUnregistrationInProgress:     false,
		IsRegistrationInProgress:       false,
		IsRegistered:                   false,
		IsClusterBlacklistedOnPE:       false,
		IsClusterBlacklistedOnPC:       false,
	}
	clusterBytes, _ := json.Marshal(cluster)
	m.RemoteRestClient.EXPECT().CallApi(
		consts.LocalHost, port, uri, method, nil,
		gomock.Any(), gomock.Any(), nil, nil, nil, true).Return(clusterBytes, nil, nil)
	zkNodePath := common_consts.CLUSTER_EXTERNAL_STATE_PATH + "/" + jobMetadata.RemoteClusterId
	m.ZkClient.EXPECT().ExistZkNode(zkNodePath, true).Return(false, nil)
	m.ErgonClient.EXPECT().ListTask(nil, gomock.Any(), nil).Return(nil, errors.New("error"))
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	res := registrationPrecheck.ExecuteLocalPrecheck(jobVar)
	assert.Equal(t, guru_error.GetInternalError(consts.RegisterAosOpName), res)
}

func TestExecuteLocalPrecheckCheckRegistrationInProgress(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = false
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.RemoteAddress = ""
	jobMetadata.RemoteClusterId = "cluster-id"
	jobMetadata.RemoteVersion = "remote-version"
	jobMetadata.UseTrust = true
	jobMetadata.ContextId = "context-id"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	uri := consts.RegistrationPrecheckUrl
	port := consts.EnvoyPort
	method := http.MethodGet
	cluster := models.ClusterRegistrationPrecheckDTO{
		IsUpgrading:                    false,
		IsNTPSet:                       false,
		IsVersionCompatible:            true,
		ClusterUuid:                    "",
		ClusterVersion:                 "",
		IsPcDeploymentInProgress:       false,
		IsAOSVersionCompatibleWithPCDR: false,
		IsUnregistrationInProgress:     false,
		IsRegistrationInProgress:       false,
		IsRegistered:                   false,
		IsClusterBlacklistedOnPE:       false,
		IsClusterBlacklistedOnPC:       false,
	}
	clusterBytes, _ := json.Marshal(cluster)
	m.RemoteRestClient.EXPECT().CallApi(
		consts.LocalHost, port, uri, method, nil,
		gomock.Any(), gomock.Any(), nil, nil, nil, true).Return(clusterBytes, nil, nil)
	zkNodePath := common_consts.CLUSTER_EXTERNAL_STATE_PATH + "/" + jobMetadata.RemoteClusterId
	m.ZkClient.EXPECT().ExistZkNode(zkNodePath, true).Return(false, nil)
	var ret [][]byte
	zkPath := common_consts.REGISTRATION_MARKER_NODE_PATH + "/" + jobMetadata.RemoteClusterId
	m.ErgonClient.EXPECT().ListTask(nil, gomock.Any(), nil).Return(ret, nil)
	m.ZkClient.EXPECT().ExistZkNode(zkPath, true).Return(true, nil)
	m.ZkClient.EXPECT().GetChildren(zkPath, true).Return([]string{uuid4.String(TestParentTaskUuid)}, nil)
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	res := registrationPrecheck.ExecuteLocalPrecheck(jobVar)
	erroMsg := fmt.Sprintf("Another registration operation for the same remote cluster is in progress. Please track the task : %s", uuid4.String(TestParentTaskUuid))
	assert.Equal(t, guru_error.GetPreconditionFailureError(erroMsg, consts.RegisterAosOpName), res)
}

func TestExecuteLocalPrecheckCheckRegistrationInProgressError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = false
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.RemoteAddress = ""
	jobMetadata.RemoteClusterId = "cluster-id"
	jobMetadata.RemoteVersion = "remote-version"
	jobMetadata.UseTrust = true
	jobMetadata.ContextId = "context-id"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	uri := consts.RegistrationPrecheckUrl
	port := consts.EnvoyPort
	method := http.MethodGet
	cluster := models.ClusterRegistrationPrecheckDTO{
		IsUpgrading:                    false,
		IsNTPSet:                       false,
		IsVersionCompatible:            true,
		ClusterUuid:                    "",
		ClusterVersion:                 "",
		IsPcDeploymentInProgress:       false,
		IsAOSVersionCompatibleWithPCDR: false,
		IsUnregistrationInProgress:     false,
		IsRegistrationInProgress:       false,
		IsRegistered:                   false,
		IsClusterBlacklistedOnPE:       false,
		IsClusterBlacklistedOnPC:       false,
	}
	clusterBytes, _ := json.Marshal(cluster)
	m.RemoteRestClient.EXPECT().CallApi(
		consts.LocalHost, port, uri, method, nil,
		gomock.Any(), gomock.Any(), nil, nil, nil, true).Return(clusterBytes, nil, nil)
	zkNodePath := common_consts.CLUSTER_EXTERNAL_STATE_PATH + "/" + jobMetadata.RemoteClusterId
	m.ZkClient.EXPECT().ExistZkNode(zkNodePath, true).Return(false, nil)
	var ret [][]byte
	zkPath := common_consts.REGISTRATION_MARKER_NODE_PATH + "/" + jobMetadata.RemoteClusterId
	m.ErgonClient.EXPECT().ListTask(nil, gomock.Any(), nil).Return(ret, nil)
	m.ZkClient.EXPECT().ExistZkNode(zkPath, true).Return(false, errors.New("error"))
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	res := registrationPrecheck.ExecuteLocalPrecheck(jobVar)
	assert.Equal(t, guru_error.GetInternalError(consts.RegisterAosOpName), res)
}

func TestExecuteLocalPrecheckSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = false
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.RemoteAddress = ""
	jobMetadata.RemoteClusterId = "cluster-id"
	jobMetadata.RemoteVersion = "remote-version"
	jobMetadata.UseTrust = true
	jobMetadata.ContextId = "context-id"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	uri := consts.RegistrationPrecheckUrl
	port := consts.EnvoyPort
	method := http.MethodGet
	cluster := models.ClusterRegistrationPrecheckDTO{
		IsUpgrading:                    false,
		IsNTPSet:                       false,
		IsVersionCompatible:            true,
		ClusterUuid:                    "",
		ClusterVersion:                 "",
		IsPcDeploymentInProgress:       false,
		IsAOSVersionCompatibleWithPCDR: false,
		IsUnregistrationInProgress:     false,
		IsRegistrationInProgress:       false,
		IsRegistered:                   false,
		IsClusterBlacklistedOnPE:       false,
		IsClusterBlacklistedOnPC:       false,
	}
	clusterBytes, _ := json.Marshal(cluster)
	m.RemoteRestClient.EXPECT().CallApi(
		consts.LocalHost, port, uri, method, nil,
		gomock.Any(), gomock.Any(), nil, nil, nil, true).Return(clusterBytes, nil, nil)
	zkNodePath := common_consts.CLUSTER_EXTERNAL_STATE_PATH + "/" + jobMetadata.RemoteClusterId
	m.ZkClient.EXPECT().ExistZkNode(zkNodePath, true).Return(false, nil)
	var ret [][]byte
	zkPath := common_consts.REGISTRATION_MARKER_NODE_PATH + "/" + jobMetadata.RemoteClusterId
	m.ErgonClient.EXPECT().ListTask(nil, gomock.Any(), nil).Return(ret, nil)
	m.ZkClient.EXPECT().ExistZkNode(zkPath, true).Return(false, nil)
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	res := registrationPrecheck.ExecuteLocalPrecheck(jobVar)
	assert.Nil(t, res)
}

func TestMakeLocalPCPrecheckCallPEClusterUuidNil(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = false
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.RemoteAddress = ""
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.UseTrust = false
	jobMetadata.ContextId = "context-id"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	res, err := registrationPrecheck.MakeLocalPCPrecheckCall(jobVar)
	assert.Nil(t, res)
	assert.Equal(t, guru_error.GetInternalError(consts.RegisterAosOpName), err)
}

func TestMakeLocalPrecheckCallPEClusterVersionNil(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = false
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.RemoteAddress = ""
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.UseTrust = false
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobMetadata.ContextId = "context-id"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	res, err := registrationPrecheck.MakeLocalPCPrecheckCall(jobVar)
	assert.Nil(t, res)
	assert.Equal(t, guru_error.GetInternalError(consts.RegisterAosOpName), err)
}

func TestMakeLocalPrecheckCallApiError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = false
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.RemoteAddress = ""
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.UseTrust = true
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobMetadata.RemoteVersion = "remote-version"
	jobMetadata.ContextId = "context-id"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	uri := consts.RegistrationPrecheckUrl
	port := consts.EnvoyPort
	method := http.MethodGet
	headerParams := make(map[string]string)
	queryParams := url.Values{}
	queryParams.Add("peClusterUuid", jobMetadata.RemoteClusterId)
	queryParams.Add("peNosVersion", jobMetadata.RemoteVersion)
	responseCode := http.StatusUnauthorized
	m.RemoteRestClient.EXPECT().CallApi(
		consts.LocalHost, port, uri, method, nil,
		headerParams, queryParams, nil, nil, nil, true).Return(nil, &responseCode, errors.New("error"))
	res, err := registrationPrecheck.MakeLocalPCPrecheckCall(jobVar)
	assert.Nil(t, res)
	assert.Equal(t, guru_error.GetUnauthorizedRequestError(consts.RegisterAosOpName), err)
}

func TestMakeLocalPrecheck(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = false
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.RemoteAddress = ""
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.UseTrust = true
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobMetadata.RemoteVersion = "remote-version"
	jobMetadata.ContextId = "context-id"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	uri := consts.RegistrationPrecheckUrl
	port := consts.EnvoyPort
	method := http.MethodGet
	headerParams := make(map[string]string)
	queryParams := url.Values{}
	queryParams.Add("peClusterUuid", jobMetadata.RemoteClusterId)
	queryParams.Add("peNosVersion", jobMetadata.RemoteVersion)
	cluster := models.ClusterRegistrationPrecheckDTO{
		IsUpgrading:                    false,
		IsNTPSet:                       false,
		IsVersionCompatible:            false,
		ClusterUuid:                    "",
		ClusterVersion:                 "",
		IsPcDeploymentInProgress:       false,
		IsAOSVersionCompatibleWithPCDR: false,
		IsUnregistrationInProgress:     false,
		IsRegistrationInProgress:       false,
		IsRegistered:                   false,
		IsClusterBlacklistedOnPE:       false,
		IsClusterBlacklistedOnPC:       false,
	}
	clusterBytes, _ := json.Marshal(cluster)
	m.RemoteRestClient.EXPECT().CallApi(
		consts.LocalHost, port, uri, method, nil,
		headerParams, queryParams, nil, nil, nil, true).Return(clusterBytes, nil, nil)
	res, err := registrationPrecheck.MakeLocalPCPrecheckCall(jobVar)
	assert.Equal(t, &cluster, res)
	assert.Nil(t, err)
}

func TestValidatePCPrecheckResponseIsUpgrading(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	var jobMetadata models.JobMetadata
	jobMetadata.RemoteVersion = "remote-version"
	jobMetadata.ContextId = "context-id"
	jobVar := &models.Job{
		JobMetadata: &jobMetadata,
	}
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	clusterRegistrationPrecheckDTO := &models.ClusterRegistrationPrecheckDTO{
		IsUpgrading: true,
	}
	err := registrationPrecheck.ValidatePCPrecheckResponse(jobVar, clusterRegistrationPrecheckDTO)
	assert.Equal(t, guru_error.GetPreconditionFailureError(consts.ErrorPCIsUpgrading.Error(), consts.RegisterAosOpName), err)
}

func TestValidatePCPrecheckResponseVersionIncompatible(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	var jobMetadata models.JobMetadata
	jobMetadata.RemoteVersion = "remote-version"
	jobMetadata.ContextId = "context-id"
	jobMetadata.SelfClusterVersion = "local-version"
	jobVar := &models.Job{
		JobMetadata: &jobMetadata,
	}
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	clusterRegistrationPrecheckDTO := &models.ClusterRegistrationPrecheckDTO{
		IsUpgrading:         false,
		IsVersionCompatible: false,
	}
	err := registrationPrecheck.ValidatePCPrecheckResponse(jobVar, clusterRegistrationPrecheckDTO)
	errorMsg := "Prism Central with version local-version is incompatible with the remote cluster with version remote-version"
	assert.Equal(t, guru_error.GetPreconditionFailureError(errorMsg, consts.RegisterAosOpName), err)
}

func TestValidatePCPrecheckResponseUnRegistrationInProgress(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	var jobMetadata models.JobMetadata
	jobMetadata.RemoteVersion = "remote-version"
	jobMetadata.ContextId = "context-id"
	jobVar := &models.Job{
		JobMetadata: &jobMetadata,
	}
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	clusterRegistrationPrecheckDTO := &models.ClusterRegistrationPrecheckDTO{
		IsUpgrading:                false,
		IsVersionCompatible:        true,
		IsUnregistrationInProgress: true,
	}
	err := registrationPrecheck.ValidatePCPrecheckResponse(jobVar, clusterRegistrationPrecheckDTO)
	assert.Equal(t, guru_error.GetPreconditionFailureError(consts.ErrorPCIsUnregistrationInProgress.Error(), consts.RegisterAosOpName), err)
}

func TestValidatePCPrecheckResponseBlacklisted(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	var jobMetadata models.JobMetadata
	jobMetadata.RemoteVersion = "remote-version"
	jobMetadata.ContextId = "context-id"
	jobVar := &models.Job{
		JobMetadata: &jobMetadata,
	}
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	clusterRegistrationPrecheckDTO := &models.ClusterRegistrationPrecheckDTO{
		IsUpgrading:                false,
		IsVersionCompatible:        true,
		IsUnregistrationInProgress: false,
		IsClusterBlacklistedOnPC:   true,
	}
	err := registrationPrecheck.ValidatePCPrecheckResponse(jobVar, clusterRegistrationPrecheckDTO)
	assert.Equal(t, guru_error.GetPreconditionFailureError(consts.ErrorPCIsBlacklisted.Error(), consts.RegisterAosOpName), err)
}

func TestValidatePcPrecheckResponseSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	var jobMetadata models.JobMetadata
	jobMetadata.RemoteVersion = "remote-version"
	jobMetadata.ContextId = "context-id"
	jobVar := &models.Job{
		JobMetadata: &jobMetadata,
	}
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	clusterRegistrationPrecheckDTO := &models.ClusterRegistrationPrecheckDTO{
		IsUpgrading:                false,
		IsVersionCompatible:        true,
		IsUnregistrationInProgress: false,
		IsClusterBlacklistedOnPC:   false,
	}
	err := registrationPrecheck.ValidatePCPrecheckResponse(jobVar, clusterRegistrationPrecheckDTO)
	assert.Nil(t, err)
}

func TestValidatePEPrecheckResponseIsUpgrading(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	var jobMetadata models.JobMetadata
	jobMetadata.RemoteVersion = "remote-version"
	jobMetadata.ContextId = "context-id"
	jobVar := &models.Job{
		JobMetadata: &jobMetadata,
	}
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	clusterRegistrationPrecheckDTO := &models.ClusterRegistrationPrecheckDTO{
		IsUpgrading: true,
	}
	err := registrationPrecheck.ValidatePEPrecheckResponse(jobVar, clusterRegistrationPrecheckDTO)
	assert.Equal(t, guru_error.GetPreconditionFailureError(consts.ErrorPEIsUpgrading.Error(), consts.RegisterAosOpName), err)
}

func TestValidatePEPrecheckResponsePcDeploymentInProgreess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	var jobMetadata models.JobMetadata
	jobMetadata.RemoteVersion = "remote-version"
	jobMetadata.ContextId = "context-id"
	jobVar := &models.Job{
		JobMetadata: &jobMetadata,
	}
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	clusterRegistrationPrecheckDTO := &models.ClusterRegistrationPrecheckDTO{
		IsUpgrading:              false,
		IsClusterBlacklistedOnPE: false,
		IsPcDeploymentInProgress: true,
	}
	err := registrationPrecheck.ValidatePEPrecheckResponse(jobVar, clusterRegistrationPrecheckDTO)
	assert.Equal(t, guru_error.GetPreconditionFailureError(consts.ErrorPEIsPcDeploymentInProgress.Error(), consts.RegisterAosOpName), err)
}

func TestValidatePEPrecheckResponseIsRegistered(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	var jobMetadata models.JobMetadata
	jobMetadata.RemoteVersion = "remote-version"
	jobMetadata.ContextId = "context-id"
	jobVar := &models.Job{
		JobMetadata: &jobMetadata,
	}
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	clusterRegistrationPrecheckDTO := &models.ClusterRegistrationPrecheckDTO{
		IsUpgrading:              false,
		IsClusterBlacklistedOnPE: false,
		IsPcDeploymentInProgress: false,
		IsRegistered:             true,
	}
	err := registrationPrecheck.ValidatePEPrecheckResponse(jobVar, clusterRegistrationPrecheckDTO)
	assert.Equal(t, guru_error.GetPreconditionFailureError(consts.ErrorPEIsRegistered.Error(), consts.RegisterAosOpName), err)
}

func TestValidatePEPrecheckResponseUnRegistrationInProgress(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	var jobMetadata models.JobMetadata
	jobMetadata.RemoteVersion = "remote-version"
	jobMetadata.ContextId = "context-id"
	jobVar := &models.Job{
		JobMetadata: &jobMetadata,
	}
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	clusterRegistrationPrecheckDTO := &models.ClusterRegistrationPrecheckDTO{
		IsUpgrading:                false,
		IsClusterBlacklistedOnPE:   false,
		IsPcDeploymentInProgress:   false,
		IsRegistered:               false,
		IsUnregistrationInProgress: true,
	}
	err := registrationPrecheck.ValidatePEPrecheckResponse(jobVar, clusterRegistrationPrecheckDTO)
	assert.Equal(t, guru_error.GetPreconditionFailureError(consts.ErrorPEIsUnregistrationInProgress.Error(), consts.RegisterAosOpName), err)
}

func TestValidatePEPrecheckResponseBlacklisted(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	var jobMetadata models.JobMetadata
	jobMetadata.RemoteVersion = "remote-version"
	jobMetadata.ContextId = "context-id"
	jobVar := &models.Job{
		JobMetadata: &jobMetadata,
	}
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	clusterRegistrationPrecheckDTO := &models.ClusterRegistrationPrecheckDTO{
		IsUpgrading:              false,
		IsClusterBlacklistedOnPE: true,
	}
	err := registrationPrecheck.ValidatePEPrecheckResponse(jobVar, clusterRegistrationPrecheckDTO)
	assert.Equal(t, guru_error.GetPreconditionFailureError(consts.ErrorPEIsBlacklisted.Error(), consts.RegisterAosOpName), err)
}

func TestValidatePEPrecheckResponseRegistrationInProgress(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	var jobMetadata models.JobMetadata
	jobMetadata.RemoteVersion = "remote-version"
	jobMetadata.ContextId = "context-id"
	jobVar := &models.Job{
		JobMetadata: &jobMetadata,
	}
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	clusterRegistrationPrecheckDTO := &models.ClusterRegistrationPrecheckDTO{
		IsUpgrading:                false,
		IsClusterBlacklistedOnPE:   false,
		IsPcDeploymentInProgress:   false,
		IsRegistered:               false,
		IsUnregistrationInProgress: false,
		IsRegistrationInProgress:   true,
	}
	err := registrationPrecheck.ValidatePEPrecheckResponse(jobVar, clusterRegistrationPrecheckDTO)
	assert.Equal(t, guru_error.GetPreconditionFailureError(consts.ErrorPEIsRegistrationInProgress.Error(), consts.RegisterAosOpName), err)
}

func TestValidatePEPrecheckResponseSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	var jobMetadata models.JobMetadata
	jobMetadata.RemoteVersion = "remote-version"
	jobMetadata.ContextId = "context-id"
	jobVar := &models.Job{
		JobMetadata: &jobMetadata,
	}
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	clusterRegistrationPrecheckDTO := &models.ClusterRegistrationPrecheckDTO{
		IsUpgrading:                false,
		IsClusterBlacklistedOnPE:   false,
		IsPcDeploymentInProgress:   false,
		IsRegistered:               false,
		IsUnregistrationInProgress: false,
		IsRegistrationInProgress:   false,
	}
	err := registrationPrecheck.ValidatePEPrecheckResponse(jobVar, clusterRegistrationPrecheckDTO)
	assert.Nil(t, err)
}

func TestCheckRegistrationInProgresserror(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	var jobMetadata models.JobMetadata
	jobMetadata.ContextId = "context-id"
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobVar := &models.Job{
		JobMetadata: &jobMetadata,
	}
	zkNodePath := common_consts.REGISTRATION_MARKER_NODE_PATH + "/" + "remote-cluster-id"
	m.ZkClient.EXPECT().ExistZkNode(zkNodePath, true).Return(false, errors.New("error"))
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	inProgress, msg, err := registrationPrecheck.CheckRegistrationInProgress(jobVar)
	assert.Equal(t, false, inProgress)
	assert.Equal(t, "", msg)
	assert.Equal(t, errors.New("error"), err)
}

func TestCheckRegistrationInProgressNotExists(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	var jobMetadata models.JobMetadata
	jobMetadata.ContextId = "context-id"
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobVar := &models.Job{
		JobMetadata: &jobMetadata,
	}
	zkNodePath := common_consts.REGISTRATION_MARKER_NODE_PATH + "/" + "remote-cluster-id"
	m.ZkClient.EXPECT().ExistZkNode(zkNodePath, true).Return(false, nil)
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	inProgress, msg, err := registrationPrecheck.CheckRegistrationInProgress(jobVar)
	assert.Equal(t, false, inProgress)
	assert.Equal(t, "", msg)
	assert.Equal(t, nil, err)
}
func TestCheckRegistrationGetChildrenError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	var jobMetadata models.JobMetadata
	jobMetadata.ContextId = "context-id"
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobVar := &models.Job{
		JobMetadata: &jobMetadata,
	}
	zkNodePath := common_consts.REGISTRATION_MARKER_NODE_PATH + "/" + "remote-cluster-id"
	m.ZkClient.EXPECT().ExistZkNode(zkNodePath, true).Return(true, nil)
	m.ZkClient.EXPECT().GetChildren(zkNodePath, true).Return(nil, errors.New("error"))
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	inProgress, msg, err := registrationPrecheck.CheckRegistrationInProgress(jobVar)
	assert.Equal(t, true, inProgress)
	assert.Equal(t, "", msg)
	assert.Equal(t, nil, err)
}

func TestCheckRegistrationGetChildrenNil(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	var jobMetadata models.JobMetadata
	jobMetadata.ContextId = "context-id"
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobVar := &models.Job{
		JobMetadata: &jobMetadata,
	}
	zkNodePath := common_consts.REGISTRATION_MARKER_NODE_PATH + "/" + "remote-cluster-id"
	m.ZkClient.EXPECT().ExistZkNode(zkNodePath, true).Return(true, nil)
	m.ZkClient.EXPECT().GetChildren(zkNodePath, true).Return(nil, nil)
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	inProgress, msg, err := registrationPrecheck.CheckRegistrationInProgress(jobVar)
	assert.Equal(t, true, inProgress)
	assert.Equal(t, "", msg)
	assert.Equal(t, nil, err)
}

func TestCheckRegistrationGetChildren(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	var jobMetadata models.JobMetadata
	jobMetadata.ContextId = "context-id"
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobVar := &models.Job{
		JobMetadata: &jobMetadata,
	}
	zkNodePath := common_consts.REGISTRATION_MARKER_NODE_PATH + "/" + "remote-cluster-id"
	m.ZkClient.EXPECT().ExistZkNode(zkNodePath, true).Return(true, nil)
	childrenNodes := make([]string, 0)
	childrenNodes = append(childrenNodes, "string")
	m.ZkClient.EXPECT().GetChildren(zkNodePath, true).Return(childrenNodes, nil)
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	inProgress, msg, err := registrationPrecheck.CheckRegistrationInProgress(jobVar)
	assert.Equal(t, true, inProgress)
	assert.Equal(t, "string", msg)
	assert.Equal(t, nil, err)
}

func TestCheckAlreadyRegisteredError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	var jobMetadata models.JobMetadata
	jobMetadata.ContextId = "context-id"
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobVar := &models.Job{
		JobMetadata: &jobMetadata,
	}
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	zkNodePath := common_consts.CLUSTER_EXTERNAL_STATE_PATH + "/" + "remote-cluster-id"
	m.ZkClient.EXPECT().ExistZkNode(zkNodePath, true).Return(false, errors.New("error"))
	registered, err := registrationPrecheck.CheckAlreadyRegistered(jobVar)
	assert.Equal(t, false, registered)
	assert.Equal(t, errors.New("error"), err)
}

func TestCheckAlreadyRegisteredRegistered(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	var jobMetadata models.JobMetadata
	jobMetadata.ContextId = "context-id"
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobVar := &models.Job{
		JobMetadata: &jobMetadata,
	}
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	zkNodePath := common_consts.CLUSTER_EXTERNAL_STATE_PATH + "/" + "remote-cluster-id"
	m.ZkClient.EXPECT().ExistZkNode(zkNodePath, true).Return(true, nil)
	registered, err := registrationPrecheck.CheckAlreadyRegistered(jobVar)
	assert.Equal(t, true, registered)
	assert.Equal(t, nil, err)
}

func TestCheckAlreadyRegisteredUnRegistered(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	var jobMetadata models.JobMetadata
	jobMetadata.ContextId = "context-id"
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobVar := &models.Job{
		JobMetadata: &jobMetadata,
	}
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	zkNodePath := common_consts.CLUSTER_EXTERNAL_STATE_PATH + "/" + "remote-cluster-id"
	m.ZkClient.EXPECT().ExistZkNode(zkNodePath, true).Return(false, nil)
	registered, err := registrationPrecheck.CheckAlreadyRegistered(jobVar)
	assert.Equal(t, false, registered)
	assert.Equal(t, nil, err)
}

func TestCheckScaleOutInProgressError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	var jobMetadata models.JobMetadata
	jobMetadata.ContextId = "context-id"
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobVar := &models.Job{
		JobMetadata: &jobMetadata,
	}
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	m.ErgonClient.EXPECT().ListTask(nil,
		[]string{consts.OperationPcAddNodeRequest}, nil).Return(nil, errors.New("error"))
	scaleOutInProgress, err := registrationPrecheck.CheckScaleOutInProgress(jobVar)
	assert.Equal(t, false, scaleOutInProgress)
	assert.Equal(t, errors.New("error"), err)
}

func TestCheckScaleOutInProgressScaleOutTaskByteNil(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	var jobMetadata models.JobMetadata
	jobMetadata.ContextId = "context-id"
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobVar := &models.Job{
		JobMetadata: &jobMetadata,
	}
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	m.ErgonClient.EXPECT().ListTask(nil,
		[]string{consts.OperationPcAddNodeRequest}, nil).Return(nil, nil)
	scaleOutInProgress, err := registrationPrecheck.CheckScaleOutInProgress(jobVar)
	assert.Equal(t, false, scaleOutInProgress)
	assert.Equal(t, nil, err)
}

func TestCheckScaleOutInProgressErrorGetTask(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	var jobMetadata models.JobMetadata
	jobMetadata.ContextId = "context-id"
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobVar := &models.Job{
		JobMetadata: &jobMetadata,
	}
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	taskList := make([][]byte, 0)
	taskList = append(taskList, TestParentTaskUuid)
	m.ErgonClient.EXPECT().ListTask(nil,
		[]string{consts.OperationPcAddNodeRequest}, nil).Return(taskList, nil)
	m.ErgonClient.EXPECT().GetTaskByUuid(TestParentTaskUuid).Return(nil, errors.New("error"))
	scaleOutInProgress, err := registrationPrecheck.CheckScaleOutInProgress(jobVar)
	assert.Equal(t, false, scaleOutInProgress)
	assert.Equal(t, errors.New("error"), err)
}

func TestCheckScaleOutInProgressTaskStatusRunning(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	var jobMetadata models.JobMetadata
	jobMetadata.ContextId = "context-id"
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobVar := &models.Job{
		JobMetadata: &jobMetadata,
	}
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	var ergonTask = ergon_proto.Task{Status: &consts.TaskStatusNone}
	taskList := make([][]byte, 0)
	taskList = append(taskList, TestParentTaskUuid)
	m.ErgonClient.EXPECT().ListTask(nil,
		[]string{consts.OperationPcAddNodeRequest}, nil).Return(taskList, nil)
	m.ErgonClient.EXPECT().GetTaskByUuid(gomock.Any()).Return(&ergonTask, nil)
	scaleOutInProgress, err := registrationPrecheck.CheckScaleOutInProgress(jobVar)
	assert.Equal(t, true, scaleOutInProgress)
	assert.Equal(t, nil, err)
}

func TestCheckScaleOutInProgressTaskStatusFailed(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	var jobMetadata models.JobMetadata
	jobMetadata.ContextId = "context-id"
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobVar := &models.Job{
		JobMetadata: &jobMetadata,
	}
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	taskStatusFailed := ergon_proto.Task_Status(6)
	var ergonTask = ergon_proto.Task{Status: &taskStatusFailed}
	taskList := make([][]byte, 0)
	taskList = append(taskList, TestParentTaskUuid)
	m.ErgonClient.EXPECT().ListTask(nil,
		[]string{consts.OperationPcAddNodeRequest}, nil).Return(taskList, nil)
	m.ErgonClient.EXPECT().GetTaskByUuid(gomock.Any()).Return(&ergonTask, nil)
	scaleOutInProgress, err := registrationPrecheck.CheckScaleOutInProgress(jobVar)
	assert.Equal(t, false, scaleOutInProgress)
	assert.Equal(t, nil, err)
}

func TestExecuteRemotePrecheckError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	var jobMetadata models.JobMetadata
	jobMetadata.ContextId = "context-id"
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobVar := &models.Job{
		JobMetadata: &jobMetadata,
	}
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(nil, errors.New("error"))
	err := registrationPrecheck.ExecuteRemotePrecheck(jobVar)
	assert.Equal(t, guru_error.GetInternalError(consts.RegisterAosOpName), err)
}

func TestExecuteRemotePrecheckRemotePEPrecheckCallFail(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = false
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.ContextId = "context-id"
	jobMetadata.UseTrust = true
	jobMetadata.RemoteAddress = "remote-address"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	timeStamp := int64(1)
	nodeList := zeus_config.NodeList{}
	node := zeus_config.ConfigurationProto_Node{
		SoftwareVersion: proto.String("version"),
		ServiceVmId:     proto.Int64(1),
	}
	nodeList = append(nodeList, &node)
	clusterUuid := "cluster-uuid"
	configProto := zeus_config.ConfigurationProto{
		ClusterUuid:      &clusterUuid,
		LogicalTimestamp: &timeStamp,
		NodeList:         nodeList,
	}
	configProtoBytes, _ := proto.Marshal(&configProto)
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(configProtoBytes, nil)
	responseCode := http.StatusUnauthorized
	m.RemoteRestClient.EXPECT().CallApi(
		jobMetadata.RemoteAddress, gomock.Any(), consts.RegistrationPrecheckUrl, http.MethodGet, nil,
		gomock.Any(), gomock.Any(), nil, gomock.Any(), gomock.Any(), true).Return(nil, &responseCode, errors.New("error"))
	m.RemoteRestClient.EXPECT().CallApi(
		jobMetadata.RemoteAddress, gomock.Any(), consts.RegistrationPrecheckUrl, http.MethodGet, nil,
		gomock.Any(), gomock.Any(), nil, gomock.Any(), gomock.Any(), true)
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}

	err := registrationPrecheck.ExecuteRemotePrecheck(jobVar)
	assert.Equal(t, guru_error.GetUnauthorizedRequestError(consts.RegisterAosOpName), err)
}

func TestExecuteRemotePrecheckRemotePEPrecheckPEUpgrading(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = false
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.ContextId = "context-id"
	jobMetadata.UseTrust = true
	jobMetadata.RemoteAddress = "remote-address"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	timeStamp := int64(1)
	nodeList := zeus_config.NodeList{}
	node := zeus_config.ConfigurationProto_Node{
		SoftwareVersion: proto.String("version"),
		ServiceVmId:     proto.Int64(1),
	}
	nodeList = append(nodeList, &node)
	clusterUuid := "cluster-uuid"
	configProto := zeus_config.ConfigurationProto{
		ClusterUuid:      &clusterUuid,
		LogicalTimestamp: &timeStamp,
		NodeList:         nodeList,
	}
	configProtoBytes, _ := proto.Marshal(&configProto)
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(configProtoBytes, nil)
	cluster := models.ClusterRegistrationPrecheckDTO{
		IsUpgrading:                    true,
		IsNTPSet:                       true,
		IsVersionCompatible:            true,
		ClusterUuid:                    "some-uuid",
		ClusterVersion:                 "some-version",
		IsPcDeploymentInProgress:       false,
		IsAOSVersionCompatibleWithPCDR: true,
		IsUnregistrationInProgress:     false,
		IsRegistrationInProgress:       false,
		IsRegistered:                   false,
		IsClusterBlacklistedOnPE:       false,
		IsClusterBlacklistedOnPC:       false,
	}
	clusterBytes, _ := json.Marshal(cluster)
	m.RemoteRestClient.EXPECT().CallApi(
		jobMetadata.RemoteAddress, gomock.Any(), consts.RegistrationPrecheckUrl, http.MethodGet, nil,
		gomock.Any(), gomock.Any(), nil, gomock.Any(), gomock.Any(), true).Return(clusterBytes, nil, nil)
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	err := registrationPrecheck.ExecuteRemotePrecheck(jobVar)
	assert.Equal(t, guru_error.GetPreconditionFailureError(consts.ErrorPEIsUpgrading.Error(), consts.RegisterAosOpName), err)
}

func TestExecuteRemotePrecheckRemotePEPrecheckSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = false
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.ContextId = "context-id"
	jobMetadata.UseTrust = true
	jobMetadata.RemoteAddress = "remote-address"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	timeStamp := int64(1)
	nodeList := zeus_config.NodeList{}
	node := zeus_config.ConfigurationProto_Node{
		SoftwareVersion: proto.String("version"),
		ServiceVmId:     proto.Int64(1),
	}
	nodeList = append(nodeList, &node)
	clusterUuid := "cluster-uuid"
	configProto := zeus_config.ConfigurationProto{
		ClusterUuid:      &clusterUuid,
		LogicalTimestamp: &timeStamp,
		NodeList:         nodeList,
	}
	configProtoBytes, _ := proto.Marshal(&configProto)
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(configProtoBytes, nil)
	cluster := models.ClusterRegistrationPrecheckDTO{
		IsUpgrading:                    false,
		IsNTPSet:                       true,
		IsVersionCompatible:            true,
		ClusterUuid:                    "some-uuid",
		ClusterVersion:                 "some-version",
		IsPcDeploymentInProgress:       false,
		IsAOSVersionCompatibleWithPCDR: true,
		IsUnregistrationInProgress:     false,
		IsRegistrationInProgress:       false,
		IsRegistered:                   false,
		IsClusterBlacklistedOnPE:       false,
		IsClusterBlacklistedOnPC:       false,
	}
	clusterBytes, _ := json.Marshal(cluster)
	m.RemoteRestClient.EXPECT().CallApi(
		jobMetadata.RemoteAddress, gomock.Any(), consts.RegistrationPrecheckUrl, http.MethodGet, nil,
		gomock.Any(), gomock.Any(), nil, gomock.Any(), gomock.Any(), true).Return(clusterBytes, nil, nil)
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	err := registrationPrecheck.ExecuteRemotePrecheck(jobVar)
	assert.Nil(t, err)
}

func TestMakeRemotePEPrecheckError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	var jobMetadata models.JobMetadata
	jobMetadata.ContextId = "context-id"
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobMetadata.UseTrust = true
	jobMetadata.SelfClusterVersion = "cluster-version"
	jobMetadata.SelfClusterId = "cluster-id"
	jobMetadata.RemoteAddress = "1.1.1.1"
	jobVar := &models.Job{
		JobMetadata: &jobMetadata,
	}
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	responseCode := http.StatusUnauthorized
	m.RemoteRestClient.EXPECT().CallApi(
		jobMetadata.RemoteAddress, gomock.Any(), consts.RegistrationPrecheckUrl, http.MethodGet, nil,
		gomock.Any(), gomock.Any(), nil, gomock.Any(), gomock.Any(), true).Return(nil, &responseCode, errors.New("error"))
	_, err := registrationPrecheck.MakeRemotePEPrecheckCall(jobVar)
	assert.Equal(t, guru_error.GetUnauthorizedRequestError(consts.RegisterAosOpName), err)
}

func TestMakeRemotePEPrecheckWithOutTrust(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	var jobMetadata models.JobMetadata
	jobMetadata.ContextId = "context-id"
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobMetadata.UseTrust = false
	jobMetadata.SelfClusterVersion = "cluster-version"
	jobMetadata.SelfClusterId = "cluster-id"
	jobMetadata.RemoteAddress = "1.1.1.1"
	jobVar := &models.Job{
		JobMetadata: &jobMetadata,
	}
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	responseCode := http.StatusUnauthorized
	m.RemoteRestClient.EXPECT().CallApi(
		jobMetadata.RemoteAddress, gomock.Any(), consts.RegistrationPrecheckUrl, http.MethodGet, nil,
		gomock.Any(), gomock.Any(), nil, gomock.Any(), gomock.Any(), false).Return(nil, &responseCode, errors.New("error"))
	_, err := registrationPrecheck.MakeRemotePEPrecheckCall(jobVar)
	assert.Equal(t, guru_error.GetUnauthorizedRequestError(consts.RegisterAosOpName), err)
}

func TestMakeRemotePEPrecheckUnMarshalError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	var jobMetadata models.JobMetadata
	jobMetadata.ContextId = "context-id"
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobMetadata.UseTrust = true
	jobMetadata.SelfClusterVersion = "cluster-version"
	jobMetadata.SelfClusterId = "cluster-id"
	jobMetadata.RemoteAddress = "remote-address"
	jobVar := &models.Job{
		JobMetadata: &jobMetadata,
	}
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	m.RemoteRestClient.EXPECT().CallApi(
		jobMetadata.RemoteAddress, gomock.Any(), consts.RegistrationPrecheckUrl, http.MethodGet, nil,
		gomock.Any(), gomock.Any(), nil, gomock.Any(), gomock.Any(), true).Return(TestParentTaskUuid, nil, nil)
	res, _ := registrationPrecheck.MakeRemotePEPrecheckCall(jobVar)
	assert.Nil(t, res)
}

func TestMakeRemotePEPrecheck(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	var jobMetadata models.JobMetadata
	jobMetadata.ContextId = "context-id"
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobMetadata.UseTrust = true
	jobMetadata.SelfClusterVersion = "cluster-version"
	jobMetadata.SelfClusterId = "cluster-id"
	jobMetadata.RemoteAddress = "remote-address"
	jobVar := &models.Job{
		JobMetadata: &jobMetadata,
	}
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	cluster := models.ClusterRegistrationPrecheckDTO{
		IsUpgrading:                    false,
		IsNTPSet:                       true,
		IsVersionCompatible:            true,
		ClusterUuid:                    "some-uuid",
		ClusterVersion:                 "some-version",
		IsPcDeploymentInProgress:       false,
		IsAOSVersionCompatibleWithPCDR: true,
		IsUnregistrationInProgress:     false,
		IsRegistrationInProgress:       false,
		IsRegistered:                   false,
		IsClusterBlacklistedOnPE:       false,
		IsClusterBlacklistedOnPC:       false,
	}
	clusterBytes, _ := json.Marshal(cluster)
	m.RemoteRestClient.EXPECT().CallApi(
		jobMetadata.RemoteAddress, gomock.Any(), consts.RegistrationPrecheckUrl, http.MethodGet, nil,
		gomock.Any(), gomock.Any(), nil, gomock.Any(), gomock.Any(), true).Return(clusterBytes, nil, nil)
	res, err := registrationPrecheck.MakeRemotePEPrecheckCall(jobVar)
	assert.Nil(t, err)
	assert.Equal(t, &cluster, res)
}

func TestInitiatePostPrecheckStepsAllTaskGetNone(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	var jobMetadata models.JobMetadata
	jobMetadata.ContextId = "context-id"
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobMetadata.UseTrust = true
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.SelfClusterVersion = "cluster-version"
	jobMetadata.SelfClusterId = "cluster-id"
	jobMetadata.RemoteAddress = "remote-address"
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.StepsToTaskMap[1] = TestParentTaskUuid
	jobMetadata.StepsToTaskMap[2] = TestParentTaskUuid
	jobVar := &models.Job{
		JobMetadata: &jobMetadata,
	}
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(test.GetMockZeusConfig(), nil)
	m.ErgonService.On("TaskGet", jobMetadata.StepsToTaskMap[1]).Return(nil, errors.New("error"))
	m.ErgonService.On("TaskGet", jobMetadata.StepsToTaskMap[2]).Return(nil, errors.New("error"))
	m.ErgonService.On("TaskGet", jobMetadata.ParentTaskId).Return(nil, errors.New("error"))
	m.ErgonClient.EXPECT().UpdateTask(jobMetadata.ParentTaskId, consts.TaskStatusNone, consts.RegistrationPrecheckCompleteMessage,
		nil, gomock.Any(), nil, nil).Return(nil)
	initiatePostPrecheckSteps(jobVar)
	m.ErgonService.AssertExpectations(t)
}

func TestInitiatePostPrecheckStepsAllTaskUpdateError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	var jobMetadata models.JobMetadata
	jobMetadata.ContextId = "context-id"
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobMetadata.UseTrust = true
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.SelfClusterVersion = "cluster-version"
	jobMetadata.SelfClusterId = "cluster-id"
	jobMetadata.RemoteAddress = "remote-address"
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.StepsToTaskMap[1] = TestParentTaskUuid
	jobMetadata.StepsToTaskMap[2] = TestParentTaskUuid
	jobVar := &models.Job{
		JobMetadata: &jobMetadata,
	}
	Task := ergon_proto.Task{
		LogicalTimestamp: nil,
	}
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(test.GetMockZeusConfig(), nil)
	m.ErgonService.On("TaskGet", jobMetadata.StepsToTaskMap[1]).Return(&Task, nil)
	m.ErgonService.On("TaskUpdate", mock.Anything).Return(nil, errors.New("error"))
	m.ErgonService.On("TaskGet", jobMetadata.StepsToTaskMap[2]).Return(&Task, nil)
	m.ErgonService.On("TaskUpdate", mock.Anything).Return(nil, errors.New("error"))
	m.ErgonService.On("TaskGet", jobMetadata.ParentTaskId).Return(&Task, nil)
	m.ErgonService.On("TaskUpdate", mock.Anything).Return(nil, errors.New("error"))
	m.ErgonClient.EXPECT().UpdateTask(jobMetadata.ParentTaskId, consts.TaskStatusNone, consts.RegistrationPrecheckCompleteMessage,
		nil, gomock.Any(), nil, nil).Return(nil)
	initiatePostPrecheckSteps(jobVar)
	m.ErgonService.AssertExpectations(t)
}

func TestInitiatePostPrecheckStepsAllTaskGetNotNone(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	var jobMetadata models.JobMetadata
	jobMetadata.ContextId = "context-id"
	jobMetadata.RemoteClusterId = "1c919e38-fc9b-460c-8b22-fef650b71511"
	jobMetadata.UseTrust = true
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.SelfClusterVersion = "cluster-version"
	jobMetadata.SelfClusterId = "cluster-id"
	jobMetadata.RemoteAddress = "remote-address"
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.StepsToTaskMap[1] = TestParentTaskUuid
	jobMetadata.StepsToTaskMap[2] = TestParentTaskUuid
	jobVar := &models.Job{
		JobMetadata: &jobMetadata,
	}
	Task := ergon_proto.Task{
		LogicalTimestamp: nil,
	}
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(test.GetMockZeusConfig(), nil)
	m.ErgonService.On("TaskGet", jobMetadata.StepsToTaskMap[1]).Return(&Task, nil)
	m.ErgonService.On("TaskUpdate", mock.Anything).Return(nil, nil)
	m.ErgonService.On("TaskGet", jobMetadata.StepsToTaskMap[2]).Return(&Task, nil)
	m.ErgonService.On("TaskUpdate", mock.Anything).Return(nil, nil)
	m.ErgonService.On("TaskGet", jobMetadata.ParentTaskId).Return(&Task, nil)
	m.ErgonService.On("TaskUpdate", mock.Anything).Return(nil, nil)
	m.ErgonClient.EXPECT().UpdateTask(jobMetadata.ParentTaskId, consts.TaskStatusNone, consts.RegistrationPrecheckCompleteMessage,
		nil, gomock.Any(), nil, nil).Return(nil)
	initiatePostPrecheckSteps(jobVar)
	m.ErgonService.AssertExpectations(t)
}
