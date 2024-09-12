package register_pc

import (
	"encoding/json"
	"errors"
	"fmt"
	"net/http"
	"testing"
	"time"

	dto "ntnx-api-guru-pc/generated-code/target/dto/models/prism/v4/management"
	common_config "ntnx-api-guru-pc/generated-code/target/protobuf/common/v1/config"
	ntnxApiGuruError "ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/error"
	"ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/management"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/test"
	"ntnx-api-guru-pc/guru-pc-api-service/utils"

	"github.com/golang/mock/gomock"
	"github.com/golang/protobuf/proto"
	"github.com/nutanix-core/go-cache/ergon"
	"github.com/nutanix-core/go-cache/util-go/uuid4"
	"github.com/nutanix-core/go-cache/util-slbufs/util/sl_bufs/net"
	common_consts "github.com/nutanix-core/ntnx-api-guru/services/constants"
	task_utils "github.com/nutanix-core/ntnx-api-utils-go/taskutils"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
)

var (
	IpAddressTest        = "ipAddress"
	UsernameTest         = "username"
	PasswordTest         = "password"
	RequestExtIdTest     = ""
	TestUuid             = "caafb6bb-a486-4ff7-9c73-33c59ecae52a"
	TestRemoteUuid       = "daafb6bb-a486-4ff7-9c73-33c59ecae52b"
	TestSelfDomainName   = "PC_12.1.1.1"
	TestRemoteDomainName = "PC_11.1.1.1"
)

func GetRegisterArg() *management.RegisterArg {
	return &management.RegisterArg{
		ExtId: &RequestExtIdTest,
		Body: &management.ClusterRegistrationSpec{
			RemoteCluster: &management.ClusterRegistrationSpec_DomainManagerRemoteClusterSpecRemoteCluster{
				DomainManagerRemoteClusterSpecRemoteCluster: &management.DomainManagerRemoteClusterSpecWrapper{
					Value: &management.DomainManagerRemoteClusterSpec{
						RemoteCluster: &management.RemoteClusterSpec{
							Address: &common_config.IPAddressOrFQDN{
								Ipv4: &common_config.IPv4Address{
									Value: &IpAddressTest,
								},
							},
							Credentials: &management.Credentials{
								Authentication: &common_config.BasicAuth{
									Username: &UsernameTest,
									Password: &PasswordTest,
								},
							},
						},
						CloudType: management.DomainManagerCloudTypeMessage_ONPREM_CLOUD.Enum(),
					},
				},
			},
		},
	}
}

func TestCreateNewRegisterPcTaskError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	requestContext := net.RpcRequestContext{
		UserUuid:       nil,
		UserName:       nil,
		UserSessionId:  nil,
		UserGroupUuids: nil,
	}
	m := test.MockSingletons(t)
	requestId := "requestId"
	resourceExtId := "resourceExtId"
	batchTaskId := TestUuid
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(test.GetMockZeusConfig(), nil)
	m.ErgonService.On("TaskCreate",
		mock.AnythingOfType("*ergon.TaskCreateArg")).Return(nil, assert.AnError)
	guruErr := guru_error.GetServiceUnavailableError(consts.RegisterPCOpName)
	m.GrpcStatusUtil.EXPECT().BuildGrpcError(guruErr).Return(assert.AnError)

	res, appMessageList, err := CreateRegisterPcTask(requestId, batchTaskId, &requestContext, resourceExtId)
	assert.Nil(t, res)
	assert.Equal(t, []*ntnxApiGuruError.AppMessage{guruErr.ConvertToAppMessagePb()}, appMessageList)
	assert.Equal(t, assert.AnError, err)
}

func TestCreateNewRegisterPcTaskGetTaskError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	requestContext := net.RpcRequestContext{
		UserUuid:       nil,
		UserName:       nil,
		UserSessionId:  nil,
		UserGroupUuids: nil,
	}
	m := test.MockSingletons(t)
	TaskCreateArg := ergon.TaskCreateRet{
		Uuid:  TestParentTaskUuid,
		ExtId: nil,
	}
	requestId := "requestId"
	resourceExtId := "resourceExtId"
	batchTaskId := TestUuid
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(test.GetMockZeusConfig(), nil)
	m.ErgonService.On("TaskCreate",
		mock.AnythingOfType("*ergon.TaskCreateArg")).Return(&TaskCreateArg, nil)
	m.ErgonService.On("TaskGet", TestParentTaskUuid).Return(nil, assert.AnError)
	guruErr := guru_error.GetServiceUnavailableError(consts.RegisterPCOpName)
	m.GrpcStatusUtil.EXPECT().BuildGrpcError(guruErr).Return(assert.AnError)

	res, appMessageList, err := CreateRegisterPcTask(requestId, batchTaskId, &requestContext, resourceExtId)
	assert.Nil(t, res)
	assert.Equal(t, []*ntnxApiGuruError.AppMessage{guruErr.ConvertToAppMessagePb()}, appMessageList)
	assert.Equal(t, assert.AnError, err)
}

func TestCreateNewRegisterPcTask_IdempotencyFailure(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	requestContext := net.RpcRequestContext{
		UserUuid:       nil,
		UserName:       nil,
		UserSessionId:  nil,
		UserGroupUuids: nil,
	}
	m := test.MockSingletons(t)
	TaskCreateArg := ergon.TaskCreateRet{
		Uuid:  TestParentTaskUuid,
		ExtId: nil,
	}
	Task := ergon.Task{
		LogicalTimestamp: nil,
		Uuid:             TestParentTaskUuid,
	}
	requestId := "requestId"
	resourceExtId := "resourceExtId"
	batchTaskId := TestUuid
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(test.GetMockZeusConfig(), nil)
	m.ErgonService.On("TaskCreate",
		mock.AnythingOfType("*ergon.TaskCreateArg")).Return(&TaskCreateArg, nil)
	m.ErgonService.On("TaskGet", TestParentTaskUuid).Return(&Task, nil)
	m.IdempotencySvc.EXPECT().UpdateTask(gomock.Any(), gomock.Any()).Return(assert.AnError)
	m.IdfSession.EXPECT().SendMsgWithTimeout(gomock.Any(), gomock.Any(), gomock.Any(), gomock.Any(), gomock.Any()).Return(assert.AnError)

	res, appMessageList, err := CreateRegisterPcTask(requestId, batchTaskId, &requestContext, resourceExtId)
	assert.Equal(t, &Task, res)
	assert.Equal(t, []*ntnxApiGuruError.AppMessage(nil), appMessageList)
	assert.Nil(t, err)
}

func TestCallAddRootCertApiError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = true
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.RemoteAddress = "remote-address"
	jobMetadata.ContextId = "context-id"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	m.Uuid.EXPECT().New().Return(uuid4.StringToUuid4(TestRequestID))
	headers := map[string]string{}
	headers[consts.NtnxRequestId] = TestRequestID

	url := fmt.Sprintf(V4AddRootCertApiPath, consts.ApiVersion, jobMetadata.RemoteClusterId)
	m.RemoteRestClient.EXPECT().CallApiBasicAuth(jobMetadata.RemoteAddress, consts.EnvoyPort,
		url, http.MethodPost, gomock.Any(), headers,
		nil, nil, "", "").Return(nil, nil, errors.New("error"))
	res, err := callAddRootCertificateApi(nil, jobVar)
	assert.Equal(t, "", res)
	assert.Equal(t, err, guru_error.GetInternalError(consts.RegisterPCOpName))
}

func TestCallCertExchangeApi(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = true
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.RemoteAddress = "remote-address"
	jobMetadata.ContextId = "context-id"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	m.Uuid.EXPECT().New().Return(uuid4.StringToUuid4(TestRequestID))
	headers := map[string]string{}
	headers[consts.NtnxRequestId] = TestRequestID

	url := fmt.Sprintf(V4AddRootCertApiPath, consts.ApiVersion, jobMetadata.RemoteClusterId)
	responseModel := utils.TaskResponseModel{}
	responseModelMarshal, _ := json.Marshal(responseModel)
	m.RemoteRestClient.EXPECT().CallApiBasicAuth(jobMetadata.RemoteAddress, consts.EnvoyPort,
		url, http.MethodPost, gomock.Any(), headers,
		nil, nil, "", "").Return(responseModelMarshal, nil, nil)
	taskUuid, err := callAddRootCertificateApi(nil, jobVar)
	assert.Equal(t, responseModel.Data.ExtId, taskUuid)
	assert.Nil(t, err)
}

func TestCreateNewRegisterPcGuruJob(t *testing.T) {
	arg := GetRegisterArg()
	Task := ergon.Task{
		LogicalTimestamp: nil,
		Uuid:             TestParentTaskUuid,
	}
	res := CreateNewRegisterPcGuruJob(&Task, arg, "")
	credentialMap := make(map[string]string)
	credentialMap["username"] = UsernameTest
	credentialMap["password"] = PasswordTest
	expectedJob := &models.Job{
		CredentialMap: credentialMap,
		Name:          consts.REGISTER_PC,
		ParentTask:    &Task,
		JobMetadata: &models.JobMetadata{
			RemoteAddress:       IpAddressTest,
			TaskId:              uuid4.String(TestParentTaskUuid),
			ContextId:           "",
			SelfClusterId:       RequestExtIdTest,
			SelfExternalAddress: consts.HostAddr,
			OperationStartTime:  time.Now().Format(consts.DateTimeFormat),
			RemoteClusterType:   dto.CLUSTERTYPE_DOMAIN_MANAGER,
		},
	}
	assert.Equal(t, *expectedJob, *res)
}

func TestCallConfigureConnectionApi(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = true
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.RemoteAddress = "remote-address"
	jobMetadata.RemoteClusterId = "remote-cluster-id"
	jobMetadata.SelfClusterId = "self-cluster-id"
	jobMetadata.SelfExternalAddress = "self-external-address"
	jobMetadata.SelfNodeIPAddresses = []string{"127.0.0.1", "127.0.0.2"}
	jobMetadata.RemoteClusterType = dto.CLUSTERTYPE_DOMAIN_MANAGER
	jobMetadata.ContextId = "context-id"
	url := fmt.Sprintf(consts.V4ConfigureConnectionApiPath, consts.ApiVersion, jobMetadata.RemoteClusterId)
	spec := createConfigureConnectionSpec(&jobMetadata)
	responseModel := struct {
		Data struct {
			ExtId string `json:"extId"`
		} `json:"data"`
	}{}
	responseModel.Data.ExtId = "ext-id"
	m.Uuid.EXPECT().New().Return(uuid4.StringToUuid4(TestRequestID))
	headers := map[string]string{}
	headers[consts.NtnxRequestId] = TestRequestID

	responseModelMarshal, _ := json.Marshal(responseModel)
	m.RemoteRestClient.EXPECT().CallApiCertBasedAuth(jobMetadata.RemoteAddress, consts.EnvoyPort, url,
		http.MethodPost, spec, headers, nil, nil).Return(responseModelMarshal, nil, nil)
	taskUuidWithHeader :=
		task_utils.GetTaskReferenceExtIdFromTaskUuid(responseModel.Data.ExtId)
	// Send Api request
	callApiUrl := consts.TasksUrl + "/" + taskUuidWithHeader
	callApiResponse := &map[string]interface{}{
		"data": map[string]interface{}{
			"status": "Running",
		},
	}
	callApiResponseMarshal, _ := json.Marshal(callApiResponse)
	m.RemoteRestClient.EXPECT().CallApi(jobMetadata.RemoteAddress, consts.EnvoyPort,
		callApiUrl, http.MethodGet, nil, nil, nil, nil,
		nil, nil, true).Return(callApiResponseMarshal, nil, nil).AnyTimes()
	res, err := callConfigureConnectionApi(&jobMetadata)
	assert.Equal(t, "ext-id", res)
	assert.Nil(t, err)
}

func TestPollTaskNotSucceded(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	test.MockFlagValues(t)
	extId := "ext-id"
	host := "localhost"
	// Send Api request
	url := consts.TasksUrl + "/" + extId
	response := &map[string]interface{}{
		"data": map[string]interface{}{
			"status": "Failed",
		},
	}

	callApiResponseMarshal, _ := json.Marshal(response)
	m.RemoteRestClient.EXPECT().CallApi(host, consts.EnvoyPort,
		url, http.MethodGet, nil, nil, nil, nil,
		nil, nil, true).Return(callApiResponseMarshal, nil, nil)

	err := pollTask("ext-id", host)
	assert.Equal(t, guru_error.GetInternalError(consts.RegisterPCOpName), err)
}

func TestUpdateJobTaskEntityForDomainManager(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	taskUpdateRet := &ergon.TaskUpdateRet{
		Task: &ergon.Task{
			Uuid:             []byte{1, 2, 3, 4, 5, 6, 7, 8, 9, 10},
			LogicalTimestamp: proto.Int64(1),
		},
	}
	m.ErgonService.On("TaskUpdate",
		mock.AnythingOfType("*ergon.TaskUpdateArg")).Return(taskUpdateRet, nil)
	jobMetadata := models.JobMetadata{
		ParentTaskId:      TestParentTaskUuid,
		ContextId:         TestUuid,
		RemoteClusterId:   TestRemoteUuid,
		RemoteClusterName: TestRemoteDomainName,
		SelfClusterId:     TestUuid,
		SelfClusterName:   TestSelfDomainName,
	}
	jobVar := &models.Job{
		Name:        jobMetadata.Name,
		JobMetadata: &jobMetadata,
		ParentTask: &ergon.Task{
			Uuid:             TestParentTaskUuid,
			LogicalTimestamp: proto.Int64(0),
		},
	}

	err := updateAffectedEntitiesInTask(jobVar)

	assert.Equal(t, err, nil)
}
