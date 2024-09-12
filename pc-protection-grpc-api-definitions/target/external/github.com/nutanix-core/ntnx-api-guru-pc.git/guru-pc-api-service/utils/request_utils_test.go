package utils

import (
	"context"
	"testing"

	"github.com/golang/mock/gomock"
	"github.com/golang/protobuf/proto"
	"github.com/stretchr/testify/assert"
	"google.golang.org/grpc/metadata"

	utilNet "github.com/nutanix-core/go-cache/util-slbufs/util/sl_bufs/net"
	zeusconfig "github.com/nutanix-core/go-cache/zeus/config"
	commonconsts "github.com/nutanix-core/ntnx-api-guru/services/constants"
	ntnxApiGuruError "ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/error"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/test"
)

var (
	TestUuid  = "caafb6bb-a486-4ff7-9c73-33c59ecae52a"
	TestToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3MjE4MDk5NzAsImlhdCI6MTcyMTgwOTY3MCwiaXNzIjoiR3VydVNlcnZpY2UifQ" +
		".YzBZR0lychgSyBcod7KDriADjxM5xUbcxtp-Fewts6o-EIxRgtkruLakmuyqpZhjV2XUq4vct3NfkAU0AKZUmO7-ewJ6hrytvUyPmuPeMJ" +
		"9fxmrpCHTlBExhIf30a-oA9NT6H-nn-5wV7fQ8T3tTq5LU5o3Ed-ZWm3ZKX6EItLMyffmZxf1vljcVWa5vqFv3nVqoIv43ktcCYBllpiFyA" +
		"EiQzQ9G0YhUFUg6DKZ1cQMLPNi1MCmbJGqgbrSvistW39rgAW6Redeg5cjUXSc-Qv1M1piIav-3p3R1su7dEuxHP-Yo9565o4EZTbWoVi5" +
		"4w85tnLYmAfdO1jjnQ-jn9A"
)

func TestCreateTaskRequestContextFromGrpcContext(t *testing.T) {
	requestContext := utilNet.RpcRequestContext{
		UserUuid:       nil,
		UserName:       nil,
		UserSessionId:  nil,
		UserGroupUuids: nil,
	}
	res := CreateTaskRequestContextFromGrpcContext(&requestContext)

	expectedRes := utilNet.RpcRequestContext{
		UserName:       requestContext.UserName,
		UserUuid:       requestContext.UserUuid,
		UserGroupUuids: requestContext.UserGroupUuids,
		UserSessionId:  requestContext.UserSessionId,
	}
	assert.Equal(t, expectedRes, res)
}

func TestVerifyIfValidRequestForCurrentResourceFailure_GetZkNodeFailed(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	m.ZkClient.EXPECT().GetZkNode(commonconsts.ZEUS_CONFIG_NODE_PATH, true).Return(nil, TestError)
	err := VerifyIfValidRequestForCurrentResource(TestUuid, consts.RegisterOpName)
	expectedError := guru_error.GetInternalError(consts.RegisterOpName)
	assert.Equal(t, expectedError, err)
}

func TestVerifyIfValidRequestForCurrentResource_WrongExtId(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	zeusConfig := &zeusconfig.ConfigurationProto{
		ClusterUuid:      proto.String("abc"),
		LogicalTimestamp: proto.Int64(0),
	}
	var marshalData []byte
	marshalData, _ = zeusConfig.XXX_Marshal(marshalData, true)
	m.ZkClient.EXPECT().GetZkNode(commonconsts.ZEUS_CONFIG_NODE_PATH, true).Return(marshalData, nil)
	err := VerifyIfValidRequestForCurrentResource("extId", consts.RegisterOpName)
	expectedError := guru_error.GetInvalidArgumentError(consts.RegisterOpName, "extId", consts.ResourceIdArg)
	assert.Equal(t, expectedError, err)
}

func TestVerifyIfValidRequestForCurrentResourceSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	zeusConfig := &zeusconfig.ConfigurationProto{
		ClusterUuid:      proto.String(TestUuid),
		LogicalTimestamp: proto.Int64(0),
	}
	var marshalData []byte
	marshalData, _ = zeusConfig.XXX_Marshal(marshalData, true)
	m.ZkClient.EXPECT().GetZkNode(commonconsts.ZEUS_CONFIG_NODE_PATH, true).Return(marshalData, nil)
	err := VerifyIfValidRequestForCurrentResource(TestUuid, consts.RegisterOpName)
	assert.Nil(t, err)
}

func TestValidateAndExtractMetadataSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	pairs := map[string]string{
		consts.NtnxRequestId:   TestUuid,
		consts.NtnxBatchTaskId: TestUuid,
	}
	// Create metadata object from the key-value pairs
	md := metadata.New(pairs)
	ctx := metadata.NewIncomingContext(context.Background(), md)

	requestId, batchTaskId, err := ValidateAndExtractMetadata(ctx, consts.RegisterOpName)

	assert.Equal(t, TestUuid, requestId)
	assert.Equal(t, TestUuid, batchTaskId)
	assert.Nil(t, err)
}

func TestValidateAndExtractMetadata_RequestIdNotPresent(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	pairs := map[string]string{
		consts.NtnxBatchTaskId: TestUuid,
	}
	// Create metadata object from the key-value pairs
	md := metadata.New(pairs)
	ctx := metadata.NewIncomingContext(context.Background(), md)

	requestId, batchTaskId, err := ValidateAndExtractMetadata(ctx, consts.RegisterOpName)

	assert.Equal(t, "", requestId)
	assert.Equal(t, "", batchTaskId)
	assert.Equal(t, guru_error.GetRequestArgumentNotFoundError(consts.RegisterOpName, consts.NtnxRequestId), err)
}

func TestValidateAndExtractMetadata_RequestIdInvalidFormat(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	pairs := map[string]string{
		consts.NtnxBatchTaskId: TestUuid,
		consts.NtnxRequestId:   "abc",
	}
	// Create metadata object from the key-value pairs
	md := metadata.New(pairs)
	ctx := metadata.NewIncomingContext(context.Background(), md)

	requestId, batchTaskId, err := ValidateAndExtractMetadata(ctx, consts.RegisterOpName)

	assert.Equal(t, "", requestId)
	assert.Equal(t, "", batchTaskId)
	assert.Equal(t, guru_error.GetRequestArgumentFormatInvalidError(consts.RegisterOpName, consts.NtnxRequestId), err)
}

func TestValidateIncomingRequestSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)

	pairs := map[string]string{
		consts.NtnxRequestId:   TestUuid,
		consts.NtnxBatchTaskId: TestUuid,
		"token_claims":         "{\"userUUID\":\"caafb6bb-a486-4ff7-9c73-33c59ecae52a\",\"username\":\"admin\"}",
	}
	// Create metadata object from the key-value pairs
	md := metadata.New(pairs)
	ctx := metadata.NewIncomingContext(context.Background(), md)
	resourceExtId := TestUuid
	zeusConfig := &zeusconfig.ConfigurationProto{
		ClusterUuid:      proto.String(TestUuid),
		LogicalTimestamp: proto.Int64(0),
	}
	var marshalData []byte
	marshalData, _ = zeusConfig.XXX_Marshal(marshalData, true)

	m.ZkClient.EXPECT().GetZkNode(commonconsts.ZEUS_CONFIG_NODE_PATH, true).Return(marshalData, nil)
	m.IdempotencySvc.EXPECT().GetTask(TestUuid).Return(&TestUuid, nil)
	m.IdfSession.EXPECT().SendMsgWithTimeout(gomock.Any(), gomock.Any(), gomock.Any(), gomock.Any(), gomock.Any()).Return(nil)
	_, requestId, batchTaskId, taskId, appMessageList, err := ValidateIncomingRequest(ctx, resourceExtId, consts.RegisterOpName)

	assert.Equal(t, TestUuid, requestId)
	assert.Equal(t, TestUuid, batchTaskId)
	assert.Equal(t, TestUuid, taskId)
	assert.Equal(t, []*ntnxApiGuruError.AppMessage(nil), appMessageList)
	assert.Nil(t, err)
}

func TestValidateIncomingRequestFailure_ValidateMetadata(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)

	pairs := map[string]string{
		"token_claims": "{\"userUUID\":\"caafb6bb-a486-4ff7-9c73-33c59ecae52a\",\"username\":\"admin\"}",
	}
	// Create metadata object from the key-value pairs
	md := metadata.New(pairs)
	ctx := metadata.NewIncomingContext(context.Background(), md)
	resourceExtId := TestUuid
	expectedErr := guru_error.GetRequestArgumentNotFoundError(consts.RegisterOpName, consts.NtnxRequestId)
	m.GrpcStatusUtil.EXPECT().BuildGrpcError(expectedErr).Return(TestError)
	grpcContext, requestId, batchTaskId, taskId, appMessageList, err := ValidateIncomingRequest(ctx, resourceExtId, consts.RegisterOpName)
	var requestContext *utilNet.RpcRequestContext

	assert.Equal(t, requestContext, grpcContext)
	assert.Equal(t, "", requestId)
	assert.Equal(t, "", batchTaskId)
	assert.Equal(t, "", taskId)
	assert.Equal(t, []*ntnxApiGuruError.AppMessage([]*ntnxApiGuruError.AppMessage{expectedErr.ConvertToAppMessagePb()}), appMessageList)
	assert.EqualError(t, err, TestError.Error())
}

func TestValidateIncomingRequestFailure_VerifyResourceExtId(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)

	pairs := map[string]string{
		consts.NtnxRequestId:   TestUuid,
		consts.NtnxBatchTaskId: TestUuid,
		"token_claims":         "{\"userUUID\":\"caafb6bb-a486-4ff7-9c73-33c59ecae52a\",\"username\":\"admin\"}",
	}
	// Create metadata object from the key-value pairs
	md := metadata.New(pairs)
	ctx := metadata.NewIncomingContext(context.Background(), md)
	resourceExtId := TestUuid
	expectedError := guru_error.GetInternalError(consts.RegisterOpName)
	m.GrpcStatusUtil.EXPECT().BuildGrpcError(expectedError).Return(TestError)
	m.ZkClient.EXPECT().GetZkNode(commonconsts.ZEUS_CONFIG_NODE_PATH, true).Return(nil, TestError)
	grpcContext, requestId, batchTaskId, taskId, appMessageList, err := ValidateIncomingRequest(ctx, resourceExtId, consts.RegisterOpName)
	var requestContext *utilNet.RpcRequestContext
	assert.Equal(t, requestContext, grpcContext)
	assert.Equal(t, "", requestId)
	assert.Equal(t, "", batchTaskId)
	assert.Equal(t, "", taskId)
	assert.Equal(t, []*ntnxApiGuruError.AppMessage([]*ntnxApiGuruError.AppMessage{expectedError.ConvertToAppMessagePb()}), appMessageList)
	assert.EqualError(t, err, TestError.Error())
}

func TestValidateIncomingRequestFailure_GetRequestContext(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)

	pairs := map[string]string{
		consts.NtnxRequestId:   TestUuid,
		consts.NtnxBatchTaskId: TestUuid,
	}
	// Create metadata object from the key-value pairs
	md := metadata.New(pairs)
	ctx := metadata.NewIncomingContext(context.Background(), md)
	resourceExtId := TestUuid
	zeusConfig := &zeusconfig.ConfigurationProto{
		ClusterUuid:      proto.String(TestUuid),
		LogicalTimestamp: proto.Int64(0),
	}
	var marshalData []byte
	marshalData, _ = zeusConfig.XXX_Marshal(marshalData, true)
	expectedError := guru_error.GetInternalError(consts.RegisterOpName)
	m.ZkClient.EXPECT().GetZkNode(commonconsts.ZEUS_CONFIG_NODE_PATH, true).Return(marshalData, nil)
	m.GrpcStatusUtil.EXPECT().BuildGrpcError(expectedError).Return(TestError)
	grpcContext, requestId, batchTaskId, taskId, appMessageList, err := ValidateIncomingRequest(ctx, resourceExtId, consts.RegisterOpName)
	var requestContext *utilNet.RpcRequestContext
	assert.Equal(t, requestContext, grpcContext)
	assert.Equal(t, "", requestId)
	assert.Equal(t, "", batchTaskId)
	assert.Equal(t, "", taskId)
	assert.Equal(t, []*ntnxApiGuruError.AppMessage([]*ntnxApiGuruError.AppMessage{expectedError.ConvertToAppMessagePb()}), appMessageList)
	assert.EqualError(t, err, TestError.Error())
}

func TestFetchRequestIdFromMetadataReturnsValidRequestId(t *testing.T) {
	metadataMap := make(map[string]string)
	metadataMap[consts.XRequestId] = TestUuid
	md := metadata.New(metadataMap)
	ctx := metadata.NewIncomingContext(context.Background(), md)
	response := FetchRequestIdFromMetadata(ctx)

	assert.Equal(t, response, TestUuid)
}

func TestFetchRequestIdFromMetadataReturnsEmpty(t *testing.T) {
	var metadataMap map[string]string
	md := metadata.New(metadataMap)
	ctx := metadata.NewIncomingContext(context.Background(), md)
	response := FetchRequestIdFromMetadata(ctx)

	assert.Equal(t, response, "")
}

func TestFetchUsernameFromMetadataReturnsEmpty(t *testing.T) {
	response := fetchUsernameFromMetadata(nil)
	assert.Equal(t, response, "")
}

func TestFetchIssuerFromTokenReturnsEmpty(t *testing.T) {
	var metadataMap map[string]string
	md := metadata.New(metadataMap)
	response := fetchIssuerFromToken(md)

	assert.Equal(t, response, "")
}

func TestFetchIssuerFromTokenEmptyTokenList(t *testing.T) {
	metadataMap := make(map[string]string)
	metadataMap[consts.XNtnxServiceToken] = ""
	md := metadata.New(metadataMap)
	response := fetchIssuerFromToken(md)

	assert.Equal(t, response, "")
}

func TestFetchIssuerFromTokenValid(t *testing.T) {
	metadataMap := make(map[string]string)
	metadataMap[consts.XNtnxServiceToken] = TestToken
	md := metadata.New(metadataMap)
	response := fetchIssuerFromToken(md)

	assert.Equal(t, response, consts.GuruServiceName)
}
