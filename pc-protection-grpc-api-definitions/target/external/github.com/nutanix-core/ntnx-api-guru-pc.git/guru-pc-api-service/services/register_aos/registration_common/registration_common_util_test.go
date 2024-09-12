package registration_common

import (
	"context"
	"errors"
	common_config "ntnx-api-guru-pc/generated-code/target/protobuf/common/v1/config"
	ntnxApiGuruError "ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/error"
	"ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/management"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/test"
	"testing"

	"github.com/nutanix-core/go-cache/prism"
	grpcUtil "github.com/nutanix-core/go-cache/util-go/net/grpc"
	"github.com/nutanix-core/go-cache/util-slbufs/util/sl_bufs/net"
	zeus_config "github.com/nutanix-core/go-cache/zeus/config"
	"github.com/stretchr/testify/mock"
	"google.golang.org/grpc/metadata"

	"github.com/golang/mock/gomock"
	ergon_proto "github.com/nutanix-core/go-cache/ergon"
	"github.com/nutanix-core/go-cache/util-go/uuid4"
	common_consts "github.com/nutanix-core/ntnx-api-guru/services/constants"
	"github.com/stretchr/testify/assert"
)

var TestParentTaskUuid = []byte{10, 204, 62, 133, 16, 76, 72, 134, 85, 211, 252, 41, 149, 19, 150, 228}
var TestTaskUuidString = uuid4.ToUuid4(TestParentTaskUuid).String()
var ErgonStatusFailedTest = ergon_proto.Task_kFailed
var TestError = errors.New("error")

func TestNewRegistrationCommonUtil(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	registrationUtil, _ := New()

	expectedRegistrationUtil := &RegistrationCommonUtil{}
	assert.Equal(t, expectedRegistrationUtil, registrationUtil)
}

func GetRegisterArg() *management.RegisterArg {
	RequestExtIdTest := "ext-id"
	IpAddressTest := "ipAddress"
	UsernameTest := "username"
	PasswordTest := "password"
	return &management.RegisterArg{
		ExtId: &RequestExtIdTest,
		Body: &management.ClusterRegistrationSpec{
			RemoteCluster: &management.ClusterRegistrationSpec_AOSRemoteClusterSpecRemoteCluster{
				AOSRemoteClusterSpecRemoteCluster: &management.AOSRemoteClusterSpecWrapper{
					Value: &management.AOSRemoteClusterSpec{
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
					},
				},
			},
		},
	}
}

func GetRegisterArgClusterExtId() *management.RegisterArg {
	RequestExtIdTest := "ext-id"
	ClusterExtId := "cluster-ext-id"
	return &management.RegisterArg{
		ExtId: &RequestExtIdTest,
		Body: &management.ClusterRegistrationSpec{
			RemoteCluster: &management.ClusterRegistrationSpec_ClusterReferenceRemoteCluster{
				ClusterReferenceRemoteCluster: &management.ClusterReferenceWrapper{
					Value: &management.ClusterReference{
						ExtId: &ClusterExtId,
					},
				},
			},
		},
	}
}

func TestValidateRegistrationRequest(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	requestArg := GetRegisterArg()
	requestDetails := &models.RegisterAosRequestDetails{}
	contextId := "requestId"
	requestDetails.ContextID = &contextId
	requestDetails.ResourceExtId = requestArg.ExtId
	registrationUtil := &RegistrationCommonUtil{}

	appMessageList, err := registrationUtil.ValidateRegistrationRequest(requestArg, requestDetails)
	assert.Equal(t, []*ntnxApiGuruError.AppMessage(nil), appMessageList)
	assert.Equal(t, nil, err)
}

func TestValidateRegistrationRequestWithClusterUuid(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	requestArg := GetRegisterArgClusterExtId()
	requestDetails := &models.RegisterAosRequestDetails{}
	TestIp := "1.2.3.4"
	contextId := "requestId"
	requestDetails.ContextID = &contextId
	requestDetails.ResourceExtId = requestArg.ExtId
	registrationUtil := &RegistrationCommonUtil{}
	m := test.MockSingletons(t)
	trustState := &prism.ClusterExternalState{
		ClusterDetails: &prism.ClusterDetails{
			ContactInfo: &zeus_config.ConfigurationProto_NetworkEntity{
				AddressList: []string{TestIp},
			},
		},
	}
	var marshalBytes []byte
	marshalBytes, _ = trustState.XXX_Marshal(marshalBytes, true)

	m.ZkClient.EXPECT().GetZkNode(common_consts.TRUST_SETUP_NODE_PATH+"/cluster-ext-id", true).Return(marshalBytes, nil)
	appMessageList, err := registrationUtil.ValidateRegistrationRequest(requestArg, requestDetails)

	assert.Equal(t, *requestDetails.IPAddressOrFQDN, TestIp)
	assert.Equal(t, []*ntnxApiGuruError.AppMessage(nil), appMessageList)
	assert.Equal(t, nil, err)
}

func TestValidateRegistrationRequestWithClusterUuidFailure(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	requestArg := GetRegisterArgClusterExtId()
	requestDetails := &models.RegisterAosRequestDetails{}
	m := test.MockSingletons(t)
	contextId := "requestId"
	requestDetails.ContextID = &contextId
	requestDetails.ResourceExtId = requestArg.ExtId
	registrationUtil := &RegistrationCommonUtil{}
	guruErr := guru_error.GetInvalidArgumentError(consts.RegisterAosOpName, "cluster-ext-id", consts.RemoteClusterIdArg)

	m.ZkClient.EXPECT().GetZkNode(common_consts.TRUST_SETUP_NODE_PATH+"/cluster-ext-id", true).Return(nil, TestError)
	m.GrpcStatusUtil.EXPECT().BuildGrpcError(guruErr).Return(TestError)

	appMessageList, err := registrationUtil.ValidateRegistrationRequest(requestArg, requestDetails)
	assert.Equal(t, []*ntnxApiGuruError.AppMessage{guruErr.ConvertToAppMessagePb()}, appMessageList)
	assert.EqualError(t, err, TestError.Error())
}

func TestCreateRegisterApiResponseDTO(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.TaskId = "task-id"
	jobMetadata.RemoteClusterId = "1.1.1.1"
	jobMetadata.ContextId = "context"
	jobMetadata.UseTrust = true
	jobMetadata.RemoteVersion = "5.15.0"
	jobMetadata.OperationStartTime = "2021-07-01T00:00:00Z"
	jobMetadata.ResourceExtId = "resource-ext-id"
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}

	registrationUtil := &RegistrationCommonUtil{}
	_ = registrationUtil.CreateRegisterApiResponseDTO(jobVar)
}

func TestCreateRegistrationMarkerNodeError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.TaskId = "task-id"
	jobMetadata.RemoteClusterId = "1.1.1.1"
	jobMetadata.ContextId = "context"
	jobMetadata.UseTrust = true
	jobMetadata.RemoteVersion = "5.15.0"
	jobMetadata.OperationStartTime = "2021-07-01T00:00:00Z"
	jobMetadata.ResourceExtId = "resource-ext-id"
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	zkPath := common_consts.REGISTRATION_MARKER_NODE_PATH +
		"/" + jobMetadata.RemoteClusterId + "/" + jobMetadata.TaskId
	m.ZkClient.EXPECT().CreateRecursive(zkPath, nil, true).Return(errors.New("error"))
	registrationUtil := &RegistrationCommonUtil{}

	result := registrationUtil.CreateRegistrationMarkerNode(jobVar)

	expectedError := errors.New("error")
	assert.Equal(t, expectedError, result)
}

func TestCreateRegistrationMarkerNode(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.TaskId = "task-id"
	jobMetadata.RemoteClusterId = "1.1.1.1"
	jobMetadata.ContextId = "context"
	jobMetadata.UseTrust = true
	jobMetadata.RemoteVersion = "5.15.0"
	jobMetadata.OperationStartTime = "2021-07-01T00:00:00Z"
	jobMetadata.ResourceExtId = "resource-ext-id"
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	zkPath := common_consts.REGISTRATION_MARKER_NODE_PATH +
		"/" + jobMetadata.RemoteClusterId + "/" + jobMetadata.TaskId
	m.ZkClient.EXPECT().CreateRecursive(zkPath, nil, true).Return(nil)
	registrationUtil := &RegistrationCommonUtil{}

	result := registrationUtil.CreateRegistrationMarkerNode(jobVar)

	assert.Equal(t, nil, result)
}

func TestCreateParentRegistrationTaskCreateFailure_Idempotency(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	contextId := "context-id"
	batchTaskId := TestTaskUuidString
	clusterExtId := "cluster-ext-id"
	TaskRet := ergon_proto.TaskCreateRet{
		Uuid:  TestParentTaskUuid,
		ExtId: nil,
	}
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(test.GetMockZeusConfig(), nil)
	m.ErgonService.On("TaskCreate",
		mock.AnythingOfType("*ergon.TaskCreateArg")).Return(&TaskRet, nil)
	registrationUtil := &RegistrationCommonUtil{}
	requestContext := new(net.RpcRequestContext)
	m.IdempotencySvc.EXPECT().UpdateTask(gomock.Any(), gomock.Any()).Return(TestError)
	m.IdfSession.EXPECT().SendMsgWithTimeout(gomock.Any(), gomock.Any(), gomock.Any(), gomock.Any(), gomock.Any()).Return(TestError)

	result, appMessageList, err := registrationUtil.CreateParentRegistrationTask(contextId, batchTaskId, requestContext, clusterExtId)

	assert.Equal(t, TestParentTaskUuid, result)
	assert.Equal(t, []*ntnxApiGuruError.AppMessage(nil), appMessageList)
	assert.Nil(t, err)
	m.ErgonService.AssertExpectations(t)
}

func TestCreateParentRegistrationTaskCreateTaskError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	contextId := "context-id"
	m.ErgonService.On("TaskCreate",
		mock.AnythingOfType("*ergon.TaskCreateArg")).Return(nil, errors.New("error"))
	registrationUtil := &RegistrationCommonUtil{}
	requestContext := new(net.RpcRequestContext)
	guruError := guru_error.GetServiceUnavailableError(consts.RegisterAosOpName)
	m.GrpcStatusUtil.EXPECT().BuildGrpcError(guruError).Return(TestError)
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(test.GetMockZeusConfig(), nil)

	result, appMessageList, err := registrationUtil.CreateParentRegistrationTask(contextId, "", requestContext, "")

	assert.Equal(t, []byte(nil), result)
	assert.Equal(t, []*ntnxApiGuruError.AppMessage{guruError.ConvertToAppMessagePb()}, appMessageList)
	assert.EqualError(t, err, TestError.Error())
	m.ErgonService.AssertExpectations(t)
}

func TestPrepareRegisterAosJobUseTrustSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	password := "password"
	username := "username"
	ipaddress := "ip-address"
	contextId := "context-id"
	clusterUuid := "cluster-uuid"
	resourceExtId := "resource-ext-id"
	useTrust := true
	requestDetails := &models.RegisterAosRequestDetails{
		TaskUuid:        TestParentTaskUuid,
		UseTrust:        &useTrust,
		Password:        &password,
		Username:        &username,
		IPAddressOrFQDN: &ipaddress,
		ContextID:       &contextId,
		ClusterUuid:     &clusterUuid,
		ResourceExtId:   &resourceExtId,
	}
	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, consts.TaskStatusNone,
		"", nil, gomock.Any(), nil, nil).Return(nil)
	registrationUtil := &RegistrationCommonUtil{}
	pairs := map[string]string{
		consts.NtnxBatchTaskId: "BatchIdTest",
		consts.NtnxRequestId:   "RequestIdTest",
	}
	md := metadata.New(pairs)
	ctx := metadata.NewIncomingContext(context.Background(), md)
	requestContext, _ := grpcUtil.GetRequestContextFromContext(ctx)
	result := registrationUtil.PrepareRegisterAosJob(requestDetails, TestParentTaskUuid, requestContext)
	assert.NotNil(t, result)
}

func TestPrepareRegisterAosJobWithOutTrust(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	password := "password"
	username := "username"
	ipaddress := "ip-address"
	contextId := "context-id"
	clusterUuid := "cluster-uuid"
	resourceExtId := "resource-ext-id"
	useTrust := false
	requestDetails := &models.RegisterAosRequestDetails{
		TaskUuid:        TestParentTaskUuid,
		UseTrust:        &useTrust,
		Password:        &password,
		Username:        &username,
		IPAddressOrFQDN: &ipaddress,
		ContextID:       &contextId,
		ClusterUuid:     &clusterUuid,
		ResourceExtId:   &resourceExtId,
	}
	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, consts.TaskStatusNone,
		"", nil, gomock.Any(), nil, nil).Return(nil)
	registrationUtil := &RegistrationCommonUtil{}
	pairs := map[string]string{
		consts.NtnxBatchTaskId: "BatchIdTest",
		consts.NtnxRequestId:   "RequestIdTest",
	}
	md := metadata.New(pairs)
	ctx := metadata.NewIncomingContext(context.Background(), md)
	requestContext, _ := grpcUtil.GetRequestContextFromContext(ctx)
	result := registrationUtil.PrepareRegisterAosJob(requestDetails, TestParentTaskUuid, requestContext)
	assert.NotNil(t, result)
}

func TestPrepareRegisterAosJobWithOutTrustUpdateTaskFail(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	password := "password"
	username := "username"
	ipaddress := "ip-address"
	contextId := "context-id"
	clusterUuid := "cluster-uuid"
	resourceExtId := "resource-ext-id"
	useTrust := false
	requestDetails := &models.RegisterAosRequestDetails{
		TaskUuid:        TestParentTaskUuid,
		UseTrust:        &useTrust,
		Password:        &password,
		Username:        &username,
		IPAddressOrFQDN: &ipaddress,
		ContextID:       &contextId,
		ClusterUuid:     &clusterUuid,
		ResourceExtId:   &resourceExtId,
	}
	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, consts.TaskStatusNone,
		"", nil, gomock.Any(), nil, nil).Return(errors.New("error"))
	registrationUtil := &RegistrationCommonUtil{}
	pairs := map[string]string{
		consts.NtnxBatchTaskId: "BatchIdTest",
		consts.NtnxRequestId:   "RequestIdTest",
	}
	md := metadata.New(pairs)
	ctx := metadata.NewIncomingContext(context.Background(), md)
	requestContext, _ := grpcUtil.GetRequestContextFromContext(ctx)
	result := registrationUtil.PrepareRegisterAosJob(requestDetails, TestParentTaskUuid, requestContext)
	assert.NotNil(t, result)
}

func TestDeleteRegistrationMarkerNodepeClusterUuidNil(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.TaskId = "task-id"
	jobMetadata.RemoteClusterId = ""
	jobMetadata.ContextId = "context"
	jobMetadata.UseTrust = true
	jobMetadata.RemoteVersion = "5.15.0"
	jobMetadata.OperationStartTime = "2021-07-01T00:00:00Z"
	jobMetadata.ResourceExtId = "resource-ext-id"
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}

	registrationUtil := &RegistrationCommonUtil{}
	registrationUtil.DeleteRegistrationMarkerNode(jobVar)
}

func TestDeleteRegistrationMarkerNodezkDeleteError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.TaskId = "task-id"
	jobMetadata.RemoteClusterId = "1.1.1.1"
	jobMetadata.ContextId = "context"
	jobMetadata.UseTrust = true
	jobMetadata.RemoteVersion = "5.15.0"
	jobMetadata.OperationStartTime = "2021-07-01T00:00:00Z"
	jobMetadata.ResourceExtId = "resource-ext-id"
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	zkPath := common_consts.REGISTRATION_MARKER_NODE_PATH + "/" +
		jobMetadata.RemoteClusterId + "/" + TestTaskUuidString
	m.ZkClient.EXPECT().DeleteRecursive(common_consts.REGISTRATION_MARKER_NODE_PATH, zkPath,
		true).Return(errors.New("error"))
	registrationUtil := &RegistrationCommonUtil{}

	registrationUtil.DeleteRegistrationMarkerNode(jobVar)
}

func TestDeleteRegistrationMarkerNode(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.TaskId = "task-id"
	jobMetadata.RemoteClusterId = "1.1.1.1"
	jobMetadata.ContextId = "context"
	jobMetadata.UseTrust = true
	jobMetadata.RemoteVersion = "5.15.0"
	jobMetadata.OperationStartTime = "2021-07-01T00:00:00Z"
	jobMetadata.ResourceExtId = "resource-ext-id"
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	zkPath := common_consts.REGISTRATION_MARKER_NODE_PATH + "/" +
		jobMetadata.RemoteClusterId + "/" + TestTaskUuidString
	m.ZkClient.EXPECT().DeleteRecursive(common_consts.REGISTRATION_MARKER_NODE_PATH,
		zkPath, true).Return(nil)
	registrationUtil := &RegistrationCommonUtil{}

	registrationUtil.DeleteRegistrationMarkerNode(jobVar)
}

func TestCreateChildTasksRequestContextNil(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.ContextId = "context"
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	registrationCommonUtil := RegistrationCommonUtil{}
	res := registrationCommonUtil.CreateChildTasks(jobVar)
	assert.Equal(t, consts.ErrorGenericServer, res)
}

func TestCreateChildTasksPrecheckTaskCreateError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	requestContext := new(net.RpcRequestContext)
	var jobMetadata models.JobMetadata
	jobMetadata.ContextId = "context"
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.RequestContext = requestContext
	jobMetadata.IsRecoveredTask = false
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(test.GetMockZeusConfig(), nil)
	m.ErgonService.On("TaskCreate", mock.AnythingOfType("*ergon.TaskCreateArg")).Return(
		nil, errors.New("error"))
	registrationUtil := &RegistrationCommonUtil{}
	res := registrationUtil.CreateChildTasks(jobVar)
	assert.Equal(t, errors.New("error"), res)
	m.ErgonService.AssertExpectations(t)
}

func TestCreateChildTasksCsrTaskCreateError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	requestContext := new(net.RpcRequestContext)
	var jobMetadata models.JobMetadata
	jobMetadata.ContextId = "context"
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.RequestContext = requestContext
	jobMetadata.IsRecoveredTask = false
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	taskRet := ergon_proto.TaskCreateRet{
		Uuid:  TestParentTaskUuid,
		ExtId: nil,
	}
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(test.GetMockZeusConfig(), nil)
	m.ErgonService.On("TaskCreate", mock.AnythingOfType("*ergon.TaskCreateArg")).Return(
		&taskRet, nil).Times(1)
	m.ErgonService.On("TaskCreate", mock.AnythingOfType("*ergon.TaskCreateArg")).Return(
		nil, errors.New("error"))
	registrationUtil := &RegistrationCommonUtil{}

	err := registrationUtil.CreateChildTasks(jobVar)

	assert.Equal(t, errors.New("error"), err)
	m.ErgonService.AssertExpectations(t)
}

func TestCreateChildTasksClusterRegistrationTaskCreateError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	requestContext := new(net.RpcRequestContext)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.ContextId = "context"
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.RequestContext = requestContext
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	taskRet := ergon_proto.TaskCreateRet{
		Uuid:  TestParentTaskUuid,
		ExtId: nil,
	}
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(test.GetMockZeusConfig(), nil)
	m.ErgonService.On("TaskCreate", mock.AnythingOfType("*ergon.TaskCreateArg")).Return(
		&taskRet, nil).Times(2)
	m.ErgonService.On("TaskCreate", mock.AnythingOfType("*ergon.TaskCreateArg")).Return(
		nil, errors.New("error"))
	registrationUtil := &RegistrationCommonUtil{}

	err := registrationUtil.CreateChildTasks(jobVar)

	assert.Equal(t, errors.New("error"), err)
	m.ErgonService.AssertExpectations(t)
}

func TestCreateChildTasksSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	requestContext := new(net.RpcRequestContext)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.ContextId = "context"
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.RequestContext = requestContext
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	TaskRet := ergon_proto.TaskCreateRet{
		Uuid:  TestParentTaskUuid,
		ExtId: nil,
	}
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(test.GetMockZeusConfig(), nil)
	m.ErgonService.On("TaskCreate",
		mock.AnythingOfType("*ergon.TaskCreateArg")).Return(&TaskRet, nil).Times(3)
	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, consts.TaskStatusNone,
		consts.RegistrationChildTaskCreatedMessage, nil, gomock.Any(), nil, nil)
	registrationUtil := &RegistrationCommonUtil{}

	err := registrationUtil.CreateChildTasks(jobVar)

	assert.Equal(t, nil, err)
	m.ErgonService.AssertExpectations(t)
}

func TestCreateChildTasksRecoverTask(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.ContextId = "context"
	jobMetadata.IsRecoveredTask = true
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	registrationUtil := &RegistrationCommonUtil{}

	err := registrationUtil.CreateChildTasks(jobVar)

	assert.Equal(t, consts.ErrorRollbackRecoverTask, err)
}

func TestProcessRegistrationErrorStateError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.ContextId = "context"
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.TaskId = TestTaskUuidString
	jobMetadata.ResourceExtId = "resource-ext-id"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, consts.TaskStatusNone,
		"", nil, gomock.Any(), nil, nil).Return(nil)
	m.EventForwarder.EXPECT().PushEvent(gomock.Any(), consts.EventTypeRegisterAOS,
		jobMetadata.TaskId, consts.EventNamespace, consts.PulseDomainManagerEntity,
		jobMetadata.ResourceExtId).Return(errors.New("error"))
	registrationUtil := &RegistrationCommonUtil{}

	registrationUtil.ProcessRegistrationErrorState(jobVar, "errorMessage", 1)
}

func TestProcessRegistrationErrorStateSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.ContextId = "context"
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.TaskId = TestTaskUuidString
	jobMetadata.ResourceExtId = "resource-ext-id"
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, consts.TaskStatusNone,
		"", nil, gomock.Any(), nil, nil).Return(nil)
	m.EventForwarder.EXPECT().PushEvent(gomock.Any(), consts.EventTypeRegisterAOS,
		jobMetadata.TaskId, consts.EventNamespace, consts.PulseDomainManagerEntity,
		jobMetadata.ResourceExtId).Return(nil)
	registrationUtil := &RegistrationCommonUtil{}

	registrationUtil.ProcessRegistrationErrorState(jobVar, "errorMessage", 1)
}
