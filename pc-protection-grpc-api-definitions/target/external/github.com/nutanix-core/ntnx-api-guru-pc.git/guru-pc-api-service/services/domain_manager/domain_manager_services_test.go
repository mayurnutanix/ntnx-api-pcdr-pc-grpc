package domain_manager

import (
	"context"
	"errors"
	"testing"
	"time"

	common_config "ntnx-api-guru-pc/generated-code/target/protobuf/common/v1/config"
	"ntnx-api-guru-pc/generated-code/target/protobuf/common/v1/response"
	"ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/config"
	ntnxApiGuruError "ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/error"
	"ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/management"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/grpc_error"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/external"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/local"
	mocks "ntnx-api-guru-pc/guru-pc-api-service/mocks"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/test"

	"github.com/golang/mock/gomock"
	"github.com/golang/protobuf/proto"
	"github.com/nutanix-core/go-cache/ergon"
	"github.com/nutanix-core/go-cache/infrastructure/cluster/client/genesis"
	"github.com/nutanix-core/go-cache/prism"
	"github.com/nutanix-core/go-cache/util-go/uuid4"
	zeus_config "github.com/nutanix-core/go-cache/zeus/config"
	common_consts "github.com/nutanix-core/ntnx-api-guru/services/constants"
	mockErgon "github.com/nutanix-core/ntnx-api-guru/services/ergon/mocks"
	mockZk "github.com/nutanix-core/ntnx-api-guru/services/zk/mocks"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
	"google.golang.org/grpc/metadata"
)

var (
	TaskUuidTest          = []byte{11, 205, 61, 133, 16, 76, 72, 134, 85, 211, 252, 41, 149, 19, 150, 228}
	ChildTaskUuidTest1    = []byte{11, 201, 61, 133, 16, 76, 72, 134, 85, 211, 252, 41, 149, 19, 150, 228}
	ChildTaskUuidTest2    = []byte{11, 202, 61, 133, 16, 76, 72, 134, 85, 211, 252, 41, 149, 19, 150, 228}
	ChildTaskUuidTest3    = []byte{11, 203, 61, 133, 16, 76, 72, 134, 85, 211, 252, 41, 149, 19, 150, 228}
	ClusterUuidByteTest   = []byte{11, 101, 61, 133, 16, 76, 72, 134, 85, 211, 252, 41, 149, 19, 150, 228}
	PeTaskUuidTest        = []byte{11, 204, 61, 133, 16, 76, 72, 134, 85, 211, 252, 41, 149, 19, 150, 228}
	TaskUuidTestString    = uuid4.ToUuid4(TaskUuidTest).String()
	PeTaskUuidTestString  = uuid4.ToUuid4(PeTaskUuidTest).String()
	ErrorCodeTest         = "100000"
	UsernameTest          = "username"
	PasswordTest          = "password"
	IpAddressTest         = "ipAddress"
	RequestIdTest         = "RequestIdTest"
	BatchIdTest           = "BatchIdTest"
	ErrorTest             = errors.New("test error occured")
	RegistrationTasksTest = &models.RegistrationTaskUuidsDTO{}
	RequestExtIdTest      = ""
	TestUuid              = "caafb6bb-a486-4ff7-9c73-33c59ecae52a"
	extId                 = "5d5b88b0-3071-48df-87de-33a85118642c"
	taskCreateRet         = ergon.TaskCreateRet{
		Uuid: TaskUuidTest,
	}
	Task = ergon.Task{Uuid: TaskUuidTest, ExtId: &RequestExtIdTest}
)

func GetRegisterAOSArg() *management.RegisterArg {
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

func GetRegisterEmpty() *management.RegisterArg {
	return &management.RegisterArg{
		ExtId: &RequestExtIdTest,
		Body: &management.ClusterRegistrationSpec{
			RemoteCluster: nil,
		},
	}
}

func GetRegisterDomainMangerArg() *management.RegisterArg {
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

func GetRegisterResponse(uuid string) *management.RegisterRet {
	return &management.RegisterRet{
		Content: &management.RegisterApiResponse{
			Data: &management.RegisterApiResponse_TaskReferenceData{
				TaskReferenceData: &management.TaskReferenceWrapper{
					Value: &config.TaskReference{
						ExtId: &uuid,
					},
				},
			},
		},
	}
}

func GetRegisterErrorResponse(errorCode *string, argMap map[string]string) *management.RegisterRet {
	return &management.RegisterRet{
		Content: &management.RegisterApiResponse{
			Data: &management.RegisterApiResponse_ErrorResponseData{
				ErrorResponseData: &management.ErrorResponseWrapper{
					Value: &ntnxApiGuruError.ErrorResponse{
						Error: &ntnxApiGuruError.ErrorResponse_AppMessageArrayError{
							AppMessageArrayError: &ntnxApiGuruError.AppMessageArrayWrapper{
								Value: []*ntnxApiGuruError.AppMessage{
									{
										Code: errorCode,
										ArgumentsMap: &ntnxApiGuruError.StringMapWrapper{
											Value: argMap,
										},
									},
								},
							},
						},
					},
				},
			},
		},
	}
}

func TestRegisterFailure_ValidationFailed(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockJobQueue := mocks.NewMockQueue(ctrl)
	mockRegistrationCommon := mocks.NewMockRegistrationCommonUtilIfc(ctrl)
	r := NewDomainManagerServiceServerImpl(mockJobQueue)
	mockGrpcUtil := mocks.NewMockGrpcStatusUtil(ctrl)
	grpc_error.SetGrpcStatusUtil(mockGrpcUtil)
	mockZkClient := mockZk.NewMockZkClientIfc(ctrl)
	external.SetSingletonServices(nil, nil, nil, mockZkClient,
		nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil)
	local.SetSingletonServices(nil, nil, mockRegistrationCommon, nil)
	regArg := GetRegisterAOSArg()
	pairs := map[string]string{
		consts.XRequestId: RequestIdTest,
	}
	// Create metadata object from the key-value pairs
	md := metadata.New(pairs)
	expectedError := guru_error.GetRequestArgumentNotFoundError(consts.RegisterOpName, consts.NtnxRequestId)
	ctx := metadata.NewIncomingContext(context.Background(), md)
	mockGrpcUtil.EXPECT().BuildGrpcError(gomock.Any()).Return(ErrorTest)

	response, err := r.Register(ctx, regArg)

	assert.Equal(t, []*ntnxApiGuruError.AppMessage([]*ntnxApiGuruError.AppMessage{expectedError.ConvertToAppMessagePb()}), response.GetContent().GetErrorResponseData().GetValue().GetAppMessageArrayError().GetValue())
	assert.EqualError(t, err, ErrorTest.Error())
}

func TestRegisterFailure_TaskAlreadyCreated(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockJobQueue := mocks.NewMockQueue(ctrl)
	mockRegistrationCommon := mocks.NewMockRegistrationCommonUtilIfc(ctrl)
	r := NewDomainManagerServiceServerImpl(mockJobQueue)
	m := test.MockSingletons(t)
	local.SetSingletonServices(nil, nil, mockRegistrationCommon, nil)
	regArg := GetRegisterAOSArg()
	pairs := map[string]string{
		consts.NtnxRequestId:   TestUuid,
		consts.NtnxBatchTaskId: TestUuid,
		"token_claims":         "{\"userUUID\":\"caafb6bb-a486-4ff7-9c73-33c59ecae52a\",\"username\":\"admin\"}",
	}
	// Create metadata object from the key-value pairs
	md := metadata.New(pairs)
	zeusConfig := &zeus_config.ConfigurationProto{
		ClusterUuid:      proto.String(RequestExtIdTest),
		LogicalTimestamp: proto.Int64(0),
	}
	var marshalData []byte
	marshalData, _ = zeusConfig.XXX_Marshal(marshalData, true)

	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(marshalData, nil)
	m.IdempotencySvc.EXPECT().GetTask(TestUuid).Return(&TestUuid, nil)
	// m.IdfSession.EXPECT().SendMsgWithTimeout(gomock.Any(), gomock.Any(), gomock.Any(), gomock.Any(), gomock.Any()).Return(nil)
	// expectedError := guru_error.GetInvalidRequest(consts.GrpcContextNoNtnxRequestIdErr)
	ctx := metadata.NewIncomingContext(context.Background(), md)
	// appMessageList := []*ntnxApiGuruError.AppMessage{expectedError.ConvertToAppMessagePb()}
	// mockRegistrationCommon.EXPECT().ValidateRegistrationRequest(regArg, gomock.Any()).Return(appMessageList, ErrorTest)
	response, err := r.Register(ctx, regArg)

	assert.Equal(t, response.GetContent().GetTaskReferenceData().GetValue().GetExtId(), "ZXJnb24=:"+TestUuid)
	assert.Nil(t, err)
}

func TestRegisterFailure_ValidationFailure2(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockJobQueue := mocks.NewMockQueue(ctrl)
	mockRegistrationCommon := mocks.NewMockRegistrationCommonUtilIfc(ctrl)
	r := NewDomainManagerServiceServerImpl(mockJobQueue)
	m := test.MockSingletons(t)
	local.SetSingletonServices(nil, nil, mockRegistrationCommon, nil)
	regArg := GetRegisterAOSArg()
	pairs := map[string]string{
		consts.NtnxRequestId:   TestUuid,
		consts.NtnxBatchTaskId: TestUuid,
		"token_claims":         "{\"userUUID\":\"caafb6bb-a486-4ff7-9c73-33c59ecae52a\",\"username\":\"admin\"}",
	}
	// Create metadata object from the key-value pairs
	md := metadata.New(pairs)
	zeusConfig := &zeus_config.ConfigurationProto{
		ClusterUuid:      proto.String(RequestExtIdTest),
		LogicalTimestamp: proto.Int64(0),
	}
	var marshalData []byte
	marshalData, _ = zeusConfig.XXX_Marshal(marshalData, true)

	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(marshalData, nil)
	m.IdempotencySvc.EXPECT().GetTask(TestUuid).Return(nil, nil)
	// m.IdfSession.EXPECT().SendMsgWithTimeout(gomock.Any(), gomock.Any(), gomock.Any(), gomock.Any(), gomock.Any()).Return(nil)
	expectedError := guru_error.GetInternalError(consts.RegisterAosOpName)
	ctx := metadata.NewIncomingContext(context.Background(), md)
	appMessageList := []*ntnxApiGuruError.AppMessage{expectedError.ConvertToAppMessagePb()}
	mockRegistrationCommon.EXPECT().ValidateRegistrationRequest(regArg, gomock.Any()).Return(appMessageList, ErrorTest)
	response, err := r.Register(ctx, regArg)

	assert.Equal(t, []*ntnxApiGuruError.AppMessage([]*ntnxApiGuruError.AppMessage{
		expectedError.ConvertToAppMessagePb()}),
		response.GetContent().GetErrorResponseData().GetValue().GetAppMessageArrayError().GetValue())
	assert.EqualError(t, err, ErrorTest.Error())
}

func TestRegisterAOSFailure_TaskCreateFailure(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockJobQueue := mocks.NewMockQueue(ctrl)
	mockRegistrationCommon := mocks.NewMockRegistrationCommonUtilIfc(ctrl)
	r := NewDomainManagerServiceServerImpl(mockJobQueue)
	m := test.MockSingletons(t)
	local.SetSingletonServices(nil, nil, mockRegistrationCommon, nil)
	regArg := GetRegisterAOSArg()
	pairs := map[string]string{
		consts.NtnxRequestId:   TestUuid,
		consts.NtnxBatchTaskId: TestUuid,
		"token_claims":         "{\"userUUID\":\"caafb6bb-a486-4ff7-9c73-33c59ecae52a\",\"username\":\"admin\"}",
	}
	// Create metadata object from the key-value pairs
	md := metadata.New(pairs)
	zeusConfig := &zeus_config.ConfigurationProto{
		ClusterUuid:      proto.String(RequestExtIdTest),
		LogicalTimestamp: proto.Int64(0),
	}
	var marshalData []byte
	marshalData, _ = zeusConfig.XXX_Marshal(marshalData, true)

	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(marshalData, nil)
	m.IdempotencySvc.EXPECT().GetTask(TestUuid).Return(nil, nil)
	expectedError := guru_error.GetInternalError(consts.RegisterAosOpName)
	ctx := metadata.NewIncomingContext(context.Background(), md)
	appMessageList := []*ntnxApiGuruError.AppMessage{expectedError.ConvertToAppMessagePb()}
	mockRegistrationCommon.EXPECT().ValidateRegistrationRequest(regArg, gomock.Any()).Return(nil, nil)
	mockRegistrationCommon.EXPECT().CreateParentRegistrationTask(TestUuid, TestUuid, gomock.Any(), RequestExtIdTest).Return(nil, appMessageList, ErrorTest)
	response, err := r.Register(ctx, regArg)

	assert.Equal(t, []*ntnxApiGuruError.AppMessage([]*ntnxApiGuruError.AppMessage{expectedError.ConvertToAppMessagePb()}), response.GetContent().GetErrorResponseData().GetValue().GetAppMessageArrayError().GetValue())
	assert.EqualError(t, err, ErrorTest.Error())
}

func TestRegisterAOSSucess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockJobQueue := mocks.NewMockQueue(ctrl)
	mockRegistrationCommon := mocks.NewMockRegistrationCommonUtilIfc(ctrl)
	r := NewDomainManagerServiceServerImpl(mockJobQueue)
	m := test.MockSingletons(t)
	local.SetSingletonServices(nil, nil, mockRegistrationCommon, nil)
	regArg := GetRegisterAOSArg()
	pairs := map[string]string{
		consts.NtnxRequestId:   TestUuid,
		consts.NtnxBatchTaskId: TestUuid,
		"token_claims":         "{\"userUUID\":\"caafb6bb-a486-4ff7-9c73-33c59ecae52a\",\"username\":\"admin\"}",
	}
	// Create metadata object from the key-value pairs
	md := metadata.New(pairs)
	zeusConfig := &zeus_config.ConfigurationProto{
		ClusterUuid:      proto.String(RequestExtIdTest),
		LogicalTimestamp: proto.Int64(0),
	}
	var marshalData []byte
	marshalData, _ = zeusConfig.XXX_Marshal(marshalData, true)

	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(marshalData, nil)
	m.IdempotencySvc.EXPECT().GetTask(TestUuid).Return(nil, nil)
	ctx := metadata.NewIncomingContext(context.Background(), md)
	mockRegistrationCommon.EXPECT().ValidateRegistrationRequest(regArg, gomock.Any()).Return(nil, nil)
	mockRegistrationCommon.EXPECT().CreateParentRegistrationTask(TestUuid, TestUuid, gomock.Any(), RequestExtIdTest).Return([]byte(TestUuid), nil, nil)
	registerJob := &models.Job{
		JobMetadata: &models.JobMetadata{
			TaskId: TestUuid,
		},
	}
	mockRegistrationCommon.EXPECT().PrepareRegisterAosJob(gomock.Any(), gomock.Any(), gomock.Any()).Return(registerJob)
	mockRegistrationCommon.EXPECT().CreateRegisterApiResponseDTO(registerJob).Return(GetRegisterResponse(TestUuid))
	mockJobQueue.EXPECT().Enqueue(gomock.Any())
	response, err := r.Register(ctx, regArg)

	assert.Equal(t, response.GetContent().GetTaskReferenceData().GetValue().GetExtId(), TestUuid)
	assert.Nil(t, err)
}

func TestRegisterDomainManagerSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	c := context.Background()
	m := test.MockSingletons(t)
	pairs := map[string]string{
		consts.NtnxRequestId:   TestUuid,
		consts.NtnxBatchTaskId: TestUuid,
		"token_claims":         "{\"userUUID\":\"caafb6bb-a486-4ff7-9c73-33c59ecae52a\",\"username\":\"admin\"}",
	}
	// Create metadata object from the key-value pairs
	md := metadata.New(pairs)
	c = metadata.NewIncomingContext(c, md)
	regArg := GetRegisterDomainMangerArg()
	timeStamp := int64(1)
	getZkResp := prism.ClusterExternalState{
		LogicalTimestamp: &timeStamp,
		ClusterUuid:      &RequestExtIdTest,
	}
	marshalGetZkResp, _ := proto.Marshal(&getZkResp)
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(marshalGetZkResp, nil)
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(test.GetMockZeusConfig(), nil)
	m.ErgonService.On("TaskCreate",
		mock.AnythingOfType("*ergon.TaskCreateArg")).Return(&taskCreateRet, nil)
	Task := ergon.Task{Uuid: TaskUuidTest, ExtId: &RequestExtIdTest}
	m.ErgonService.On("TaskGet", TaskUuidTest).Return(&Task, nil)
	m.ErgonService.On("TaskUpdateBase", mock.AnythingOfType("*ergon.TaskUpdateArg")).Return(nil, nil)
	m.IdempotencySvc.EXPECT().GetTask(TestUuid).Return(nil, nil)
	m.IdempotencySvc.EXPECT().UpdateTask(TestUuid, gomock.Any()).Return(nil)
	mockJobQueue := mocks.NewMockQueue(ctrl)
	mockJobQueue.EXPECT().Enqueue(gomock.Any())
	r := NewDomainManagerServiceServerImpl(mockJobQueue)

	res, err := r.Register(c, regArg)

	assert.NotNil(t, res)
	assert.Nil(t, err)
}

func TestRegisterFailure_NotFound(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	c := context.Background()
	pairs := map[string]string{
		consts.NtnxRequestId:   TestUuid,
		consts.NtnxBatchTaskId: TestUuid,
		"token_claims":         "{\"userUUID\":\"caafb6bb-a486-4ff7-9c73-33c59ecae52a\",\"username\":\"admin\"}",
	}
	// Create metadata object from the key-value pairs
	md := metadata.New(pairs)
	c = metadata.NewIncomingContext(c, md)
	regArg := GetRegisterEmpty()
	mockZkClient := mockZk.NewMockZkClientIfc(ctrl)
	mockErgonClient := mockErgon.NewMockErgonService(ctrl)
	mockIdfSession := mocks.NewMockInsightsServiceInterface(ctrl)
	mockIdempotencySvc := mocks.NewMockIdempotencyService(ctrl)
	mockGrpcUtil := mocks.NewMockGrpcStatusUtil(ctrl)
	grpc_error.SetGrpcStatusUtil(mockGrpcUtil)
	external.SetSingletonServices(nil, mockIdfSession, nil, mockZkClient,
		nil, nil, nil, nil, nil, nil, nil, nil, mockErgonClient, nil, mockIdempotencySvc, nil, nil)
	timeStamp := int64(1)
	getZkResp := prism.ClusterExternalState{
		LogicalTimestamp: &timeStamp,
		ClusterUuid:      &RequestExtIdTest,
	}
	marshalGetZkResp, _ := proto.Marshal(&getZkResp)
	mockZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(marshalGetZkResp, nil)
	mockIdempotencySvc.EXPECT().GetTask(TestUuid).Return(nil, nil)
	mockGrpcUtil.EXPECT().BuildGrpcError(gomock.Any()).Return(ErrorTest)
	mockJobQueue := mocks.NewMockQueue(ctrl)
	r := NewDomainManagerServiceServerImpl(mockJobQueue)
	res, err := r.Register(c, regArg)
	assert.NotNil(t, res)
	assert.NotNil(t, err)
}

func TestRemoveRootCertificateVerifyRequestError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	body := management.RootCertRemoveSpec{ClusterExtId: &extId}
	arg := management.RemoveRootCertificateArg{
		ExtId: &extId,
		Body:  &body,
	}
	pairs := map[string]string{
		consts.XRequestId: RequestIdTest,
	}
	// Create metadata object from the key-value pairs
	md := metadata.New(pairs)
	ctx := metadata.NewIncomingContext(context.Background(), md)
	m := test.MockSingletons(t)
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(nil, errors.New("error"))
	m.GrpcStatusUtil.EXPECT().BuildGrpcError(gomock.Any()).Return(ErrorTest)
	m.GenesisClient.EXPECT().DeleteCACertificate(gomock.Any()).Return(nil, nil)
	mockJobQueue := mocks.NewMockQueue(ctrl)
	r := NewDomainManagerServiceServerImpl(mockJobQueue)
	res, err := r.RemoveRootCertificate(ctx, &arg)

	assert.NotNil(t, res)
	assert.NotNil(t, err)
}

func TestRemoveRootCertificateDeleteRootCertZkError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	body := management.RootCertRemoveSpec{ClusterExtId: &extId}
	arg := management.RemoveRootCertificateArg{
		ExtId: &extId,
		Body:  &body,
	}
	pairs := map[string]string{
		consts.XRequestId: RequestIdTest,
	}
	// Create metadata object from the key-value pairs
	md := metadata.New(pairs)
	ctx := metadata.NewIncomingContext(context.Background(), md)
	m := test.MockSingletons(t)
	m.GrpcStatusUtil.EXPECT().BuildGrpcError(gomock.Any()).Return(ErrorTest)
	mockJobQueue := mocks.NewMockQueue(ctrl)
	r := NewDomainManagerServiceServerImpl(mockJobQueue)

	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(nil, ErrorTest)
	res, err := r.RemoveRootCertificate(ctx, &arg)

	assert.NotNil(t, res)
	assert.NotNil(t, err)
}

func TestRemoveRootCertificateSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)

	body := management.RootCertRemoveSpec{ClusterExtId: &extId}
	args := management.RemoveRootCertificateArg{
		ExtId: &extId,
		Body:  &body,
	}
	pairs := map[string]string{
		consts.XRequestId:    RequestIdTest,
		consts.NtnxRequestId: TestUuid,
		"token_claims":       "{\"userUUID\":\"caafb6bb-a486-4ff7-9c73-33c59ecae52a\",\"username\":\"admin\"}",
	}
	// Create metadata object from the key-value pairs
	md := metadata.New(pairs)
	ctx := metadata.NewIncomingContext(context.Background(), md)
	mockJobQueue := mocks.NewMockQueue(ctrl)
	r := NewDomainManagerServiceServerImpl(mockJobQueue)

	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(test.GetMockZeusConfig(), nil).AnyTimes()
	errToReturn := genesis.GenesisGenericError_kNoError
	genesisError := genesis.GenesisGenericError{
		ErrorType: &errToReturn,
	}
	deleteCertRet := genesis.DeleteCACertificateRet{
		Error: &genesisError,
	}
	mockJobQueue.EXPECT().Enqueue(gomock.Any())
	m.ErgonService.On("TaskCreate",
		mock.AnythingOfType("*ergon.TaskCreateArg")).Return(&taskCreateRet, nil)
	m.ErgonService.On("TaskGet", TaskUuidTest).Return(&Task, nil)
	m.ErgonService.On("TaskUpdateBase", mock.AnythingOfType("*ergon.TaskUpdateArg")).Return(nil, nil)
	m.IdempotencySvc.EXPECT().GetTask(TestUuid).Return(nil, nil)
	m.GenesisClient.EXPECT().DeleteCACertificate(gomock.Any()).Return(&deleteCertRet, nil)

	res, err := r.RemoveRootCertificate(ctx, &args)
	time.Sleep(1 * time.Second)

	assert.NotNil(t, res)
	assert.Nil(t, err)
}

func TestConfigureConnectionVerifyRequestError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	configureConnectionSpec := &management.ConnectionConfigurationSpec{
		RemoteCluster: &management.RemoteCluster{
			Base: &response.ExternalizableAbstractModel{
				ExtId: &extId,
			},
			NodeIpAddresses: &common_config.IPAddressOrFQDNArrayWrapper{},
			ExternalAddress: &common_config.IPAddressOrFQDN{
				Ipv4: &common_config.IPv4Address{
					Value: &IpAddressTest,
				},
			},
		},
	}
	body := configureConnectionSpec
	args := management.ConfigureConnectionArg{
		ExtId: &extId,
		Body:  body,
	}
	m := test.MockSingletons(t)
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(nil, ErrorTest)
	expectedError := guru_error.GetInternalError(consts.ConfigureConnectionOpName)
	m.GrpcStatusUtil.EXPECT().BuildGrpcError(expectedError).Return(ErrorTest)
	mockJobQueue := mocks.NewMockQueue(ctrl)
	r := NewDomainManagerServiceServerImpl(mockJobQueue)
	pairs := map[string]string{
		consts.NtnxRequestId:   TestUuid,
		consts.NtnxBatchTaskId: TestUuid,
		"token_claims":         "{\"userUUID\":\"caafb6bb-a486-4ff7-9c73-33c59ecae52a\",\"username\":\"admin\"}",
	}
	md := metadata.New(pairs)
	ctx := metadata.NewIncomingContext(context.Background(), md)

	res, err := r.ConfigureConnection(ctx, &args)
	assert.NotNil(t, res)
	assert.NotNil(t, err)
}

func TestConfigureConnectionCreateConfigureConnectionError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	pairs := map[string]string{
		consts.NtnxRequestId:   TestUuid,
		consts.NtnxBatchTaskId: TestUuid,
		"token_claims":         "{\"userUUID\":\"caafb6bb-a486-4ff7-9c73-33c59ecae52a\",\"username\":\"admin\"}",
	}
	md := metadata.New(pairs)
	ctx := metadata.NewIncomingContext(context.Background(), md)
	configureConnectionSpec := &management.ConnectionConfigurationSpec{
		RemoteCluster: &management.RemoteCluster{
			Base: &response.ExternalizableAbstractModel{
				ExtId: &extId,
			},
			ExternalAddress: &common_config.IPAddressOrFQDN{
				Ipv4: &common_config.IPv4Address{
					Value: &IpAddressTest,
				},
			},
			NodeIpAddresses: &common_config.IPAddressOrFQDNArrayWrapper{},
		},
	}

	args := management.ConfigureConnectionArg{
		ExtId: &extId,
		Body:  configureConnectionSpec,
	}
	mockJobQueue := mocks.NewMockQueue(ctrl)
	m.ErgonService.On("TaskCreate",
		mock.AnythingOfType("*ergon.TaskCreateArg")).Return(nil, ErrorTest)
	m.ErgonService.On("TaskGet", TaskUuidTest).Return(nil, nil)
	m.ErgonService.On("TaskUpdateBase", mock.AnythingOfType("*ergon.TaskUpdateArg")).Return(nil, nil)
	m.IdempotencySvc.EXPECT().GetTask(TestUuid).Return(nil, nil)
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(test.GetMockZeusConfig(), nil).Times(2)

	m.GrpcStatusUtil.EXPECT().BuildGrpcError(gomock.Any()).Return(ErrorTest)
	r := NewDomainManagerServiceServerImpl(mockJobQueue)
	res, err := r.ConfigureConnection(ctx, &args)
	assert.NotNil(t, res)
	assert.NotNil(t, err)
}

func TestConfigureConnectionSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	pairs := map[string]string{
		consts.NtnxRequestId:   TestUuid,
		consts.NtnxBatchTaskId: TestUuid,
		"token_claims":         "{\"userUUID\":\"caafb6bb-a486-4ff7-9c73-33c59ecae52a\",\"username\":\"admin\"}",
	}
	md := metadata.New(pairs)
	ctx := metadata.NewIncomingContext(context.Background(), md)
	configureConnectionSpec := &management.ConnectionConfigurationSpec{
		RemoteCluster: &management.RemoteCluster{
			Base: &response.ExternalizableAbstractModel{
				ExtId: &extId,
			},
			ExternalAddress: &common_config.IPAddressOrFQDN{
				Ipv4: &common_config.IPv4Address{
					Value: &IpAddressTest,
				},
			},
			NodeIpAddresses: &common_config.IPAddressOrFQDNArrayWrapper{},
		},
	}
	args := management.ConfigureConnectionArg{
		ExtId: &extId,
		Body:  configureConnectionSpec,
	}
	mockJobQueue := mocks.NewMockQueue(ctrl)
	mockJobQueue.EXPECT().Enqueue(gomock.Any())
	m.ErgonService.On("TaskCreate",
		mock.AnythingOfType("*ergon.TaskCreateArg")).Return(&taskCreateRet, nil)
	m.ErgonService.On("TaskGet", TaskUuidTest).Return(&Task, nil)
	m.ErgonService.On("TaskUpdateBase", mock.AnythingOfType("*ergon.TaskUpdateArg")).Return(nil, nil)
	m.IdempotencySvc.EXPECT().GetTask(TestUuid).Return(nil, nil)
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(test.GetMockZeusConfig(), nil).Times(2)

	r := NewDomainManagerServiceServerImpl(mockJobQueue)
	res, err := r.ConfigureConnection(ctx, &args)
	assert.NotNil(t, res)
	assert.Nil(t, err)
}

func TestUnconfigureConnectionGetRequestContextError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	pairs := map[string]string{
		consts.XRequestId: RequestIdTest,
		"token_claims":    "{\"userUUID\":\"caafb6bb-a486-4ff7-9c73-33c59ecae52a\",\"username\":\"admin\"}",
	}
	body := management.UnconfigureConnectionArg{ExtId: &extId,
		Body: &management.ConnectionUnconfigurationSpec{
			RemoteCluster: &management.ClusterReference{
				ExtId: &TaskUuidTestString,
			},
		},
	}
	mockZkClient := mockZk.NewMockZkClientIfc(ctrl)
	mockIdempotencySvc := mocks.NewMockIdempotencyService(ctrl)
	mockGrpcClient := mocks.NewMockGrpcStatusUtil(ctrl)
	grpc_error.SetGrpcStatusUtil(mockGrpcClient)
	external.SetSingletonServices(nil, nil, nil, mockZkClient,
		nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, mockIdempotencySvc, nil, nil)

	//Create metadata object from the key-value pairs
	md := metadata.New(pairs)
	ctx := metadata.NewIncomingContext(context.Background(), md)
	mockJobQueue := mocks.NewMockQueue(ctrl)
	mockGrpcClient.EXPECT().BuildGrpcError(gomock.Any()).Return(ErrorTest)
	r := NewDomainManagerServiceServerImpl(mockJobQueue)
	res, err := r.UnconfigureConnection(ctx, &body)
	assert.NotNil(t, res)
	assert.NotNil(t, err)
}

func TestUnconfigureConnectionCreateTaskError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	pairs := map[string]string{
		consts.NtnxRequestId:   TestUuid,
		consts.NtnxBatchTaskId: TestUuid,
		"token_claims":         "{\"userUUID\":\"caafb6bb-a486-4ff7-9c73-33c59ecae52a\",\"username\":\"admin\"}",
	}
	md := metadata.New(pairs)
	ctx := metadata.NewIncomingContext(context.Background(), md)

	body := management.UnconfigureConnectionArg{ExtId: &extId, Body: &management.ConnectionUnconfigurationSpec{
		RemoteCluster: &management.ClusterReference{
			ExtId: &extId,
		},
	}}
	mockGrpcClient := mocks.NewMockGrpcStatusUtil(ctrl)
	grpc_error.SetGrpcStatusUtil(mockGrpcClient)
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(test.GetMockZeusConfig(), nil).Times(2)
	m.ErgonService.On("TaskCreate",
		mock.AnythingOfType("*ergon.TaskCreateArg")).Return(nil, assert.AnError)
	m.IdempotencySvc.EXPECT().GetTask(TestUuid).Return(nil, nil)
	mockGrpcClient.EXPECT().BuildGrpcError(gomock.Any()).Return(ErrorTest)
	mockJobQueue := mocks.NewMockQueue(ctrl)
	r := NewDomainManagerServiceServerImpl(mockJobQueue)

	res, err := r.UnconfigureConnection(ctx, &body)

	assert.NotNil(t, res)
	assert.NotNil(t, err)
}

func TestUnconfigureConnectionSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	pairs := map[string]string{
		consts.NtnxRequestId:   TestUuid,
		consts.NtnxBatchTaskId: TestUuid,
		"token_claims":         "{\"userUUID\":\"caafb6bb-a486-4ff7-9c73-33c59ecae52a\",\"username\":\"admin\"}",
	}
	md := metadata.New(pairs)
	ctx := metadata.NewIncomingContext(context.Background(), md)
	body := management.UnconfigureConnectionArg{
		ExtId: &extId,
		Body: &management.ConnectionUnconfigurationSpec{
			RemoteCluster: &management.ClusterReference{
				ExtId: &extId,
			},
		},
	}
	m.ErgonService.On("TaskCreate",
		mock.AnythingOfType("*ergon.TaskCreateArg")).Return(&taskCreateRet, nil)
	m.ErgonService.On("TaskGet", TaskUuidTest).Return(&Task, nil)
	m.ErgonService.On("TaskUpdateBase", mock.AnythingOfType("*ergon.TaskUpdateArg")).Return(nil, nil)
	m.IdempotencySvc.EXPECT().GetTask(TestUuid).Return(nil, nil)
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(test.GetMockZeusConfig(), nil).Times(2)
	mockJobQueue := mocks.NewMockQueue(ctrl)
	mockJobQueue.EXPECT().Enqueue(gomock.Any())
	r := NewDomainManagerServiceServerImpl(mockJobQueue)

	res, err := r.UnconfigureConnection(ctx, &body)

	assert.NotNil(t, res)
	assert.Nil(t, err)
}

func TestUnregisterSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	pairs := map[string]string{
		consts.NtnxRequestId:   TestUuid,
		consts.NtnxBatchTaskId: TestUuid,
		"token_claims":         "{\"userUUID\":\"caafb6bb-a486-4ff7-9c73-33c59ecae52a\",\"username\":\"admin\"}",
	}
	md := metadata.New(pairs)
	ctx := metadata.NewIncomingContext(context.Background(), md)
	body := management.UnregisterArg{
		ExtId: &extId,
		Body: &management.ClusterReference{
			ExtId: &extId,
		},
	}
	m.ErgonService.On("TaskGet", TaskUuidTest).Return(&Task, nil)
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).
		Return(test.GetMockZeusConfig(), nil).Times(2)
	m.ZkClient.EXPECT().ExistZkNode(consts.AOSClusterCES+"/"+extId, true).Return(false, nil)
	m.ZkClient.EXPECT().ExistZkNode(consts.DomainManagerCES+"/"+extId, true).Return(true, nil)
	m.IdempotencySvc.EXPECT().GetTask(TestUuid).Return(nil, nil)
	m.IdempotencySvc.EXPECT().UpdateTask(TestUuid, gomock.Any()).Return(nil)
	m.IdempotencySvc.EXPECT().GetTask(TestUuid).Return(nil, nil)
	m.ErgonService.On("TaskCreate",
		mock.AnythingOfType("*ergon.TaskCreateArg")).Return(&taskCreateRet, nil)
	mockJobQueue := mocks.NewMockQueue(ctrl)
	mockJobQueue.EXPECT().Enqueue(gomock.Any())
	r := NewDomainManagerServiceServerImpl(mockJobQueue)
	res, err := r.Unregister(ctx, &body)
	assert.NotNil(t, res)
	assert.Nil(t, err)
}

func TestAddRootCertificateGetZkError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	pairs := map[string]string{
		consts.XRequestId:      RequestIdTest,
		consts.NtnxRequestId:   TestUuid,
		consts.NtnxBatchTaskId: BatchIdTest,
	}

	//Create metadata object from the key-value pairs
	md := metadata.New(pairs)
	ctx := metadata.NewIncomingContext(context.Background(), md)
	regArg := &management.AddRootCertificateArg{ExtId: &extId}
	mockZkClient := mockZk.NewMockZkClientIfc(ctrl)
	mockGrpcClient := mocks.NewMockGrpcStatusUtil(ctrl)
	grpc_error.SetGrpcStatusUtil(mockGrpcClient)
	external.SetSingletonServices(nil, nil, nil, mockZkClient,
		nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil)
	mockZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(nil, errors.New("error")).AnyTimes()
	mockGrpcClient.EXPECT().BuildGrpcError(gomock.Any()).Return(ErrorTest).AnyTimes()
	mockJobQueue := mocks.NewMockQueue(ctrl)
	r := NewDomainManagerServiceServerImpl(mockJobQueue)
	res, err := r.AddRootCertificate(ctx, regArg)
	assert.NotNil(t, res)
	assert.NotNil(t, err)
}

func TestAddRootCertificateError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	pairs := map[string]string{
		consts.XRequestId:      RequestIdTest,
		consts.NtnxRequestId:   TestUuid,
		consts.NtnxBatchTaskId: BatchIdTest,
	}

	//Create metadata object from the key-value pairs
	md := metadata.New(pairs)
	ctx := metadata.NewIncomingContext(context.Background(), md)
	args := &management.AddRootCertificateArg{ExtId: &extId}
	timeStamp := int64(1)
	getZkResp := prism.ClusterExternalState{
		LogicalTimestamp: &timeStamp,
		ClusterUuid:      &RequestExtIdTest,
	}
	m.IdempotencySvc.EXPECT().GetTask(TestUuid).Return(nil, nil)
	marshalData, _ := proto.Marshal(&getZkResp)
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(marshalData, nil)
	m.GrpcStatusUtil.EXPECT().BuildGrpcError(gomock.Any()).Return(ErrorTest)
	mockJobQueue := mocks.NewMockQueue(ctrl)
	r := NewDomainManagerServiceServerImpl(mockJobQueue)
	res, err := r.AddRootCertificate(ctx, args)
	assert.NotNil(t, res)
	assert.NotNil(t, err)
}
