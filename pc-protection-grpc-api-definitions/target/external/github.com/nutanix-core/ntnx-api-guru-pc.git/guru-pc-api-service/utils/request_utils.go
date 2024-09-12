/*
* Copyright (c) 2024 Nutanix Inc. All rights reserved.
*
* Author: kshitij.kumar@nutanix.com
*
* This file contains utils for request processing and other actions on grpc request.
 */

package utils

import (
	"context"
	"fmt"
	grpcUtils "github.com/nutanix-core/go-cache/util-go/net/grpc"
	"google.golang.org/grpc/peer"
	"net"
	ntnxApiGuruError "ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/error"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/grpc_error"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/external"

	grpcUtil "github.com/nutanix-core/go-cache/util-go/net/grpc"
	"github.com/nutanix-core/go-cache/util-go/uuid4"

	utilNet "github.com/nutanix-core/go-cache/util-slbufs/util/sl_bufs/net"
	log "github.com/sirupsen/logrus"
	"google.golang.org/grpc/metadata"
)

func VerifyIfValidRequestForCurrentResource(resourceExtId string, operation string) guru_error.GuruErrorInterface {
	// Fetch zeus config of current domain manager resource.
	zeusConfig, err := FetchZeusConfig()
	if err != nil {
		log.Errorf("Failed to fetch zeus config with error - %s", err.Error())
		return guru_error.GetInternalError(operation)
	}

	clusterExtId := zeusConfig.GetClusterUuid()
	if clusterExtId != resourceExtId {
		log.Errorf("Invalid resource specified: %s", resourceExtId)
		return guru_error.GetInvalidArgumentError(operation, resourceExtId, consts.ResourceIdArg)
	}
	return nil
}

func ValidateAndExtractMetadata(ctx context.Context, operation string) (string, string, guru_error.GuruErrorInterface) {
	md, ok := metadata.FromIncomingContext(ctx)
	if !ok {
		return "", "", guru_error.GetInternalError(operation)
	}

	var requestId, batchTaskId string
	batchTaskIdList, ok := md[consts.NtnxBatchTaskId]
	if ok && len(batchTaskIdList) > 0 {
		// If batch task id is present, it's a batch request
		batchTaskId = batchTaskIdList[0]
		log.Infof("This request is a part of batch request with task uuid %s", batchTaskId)
	}
	// If batch task id is not present, it's not a batch request
	// Request Id to be extracted from NTNX-Request-Id
	requestIdList, ok := md[consts.NtnxRequestId]
	if !ok || len(requestIdList) == 0 {
		return "", "", guru_error.GetRequestArgumentNotFoundError(operation, consts.NtnxRequestId)
	}
	// Check if request id is in a valid format
	requestId = requestIdList[0]
	_, err := uuid4.StringToUuid4(requestId)
	if err != nil {
		return "", "", guru_error.GetRequestArgumentFormatInvalidError(operation, consts.NtnxRequestId)
	}
	return requestId, batchTaskId, nil
}

func ValidateIncomingRequest(ctx context.Context, resourceExtId string, operation string) (
	*utilNet.RpcRequestContext, string, string, string, []*ntnxApiGuruError.AppMessage, error) {

	// Extract metadata from context
	requestId, batchTaskId, guruErr := ValidateAndExtractMetadata(ctx, operation)
	if guruErr != nil {
		return nil, "", "", "", []*ntnxApiGuruError.AppMessage{guruErr.ConvertToAppMessagePb()},
			grpc_error.GetGrpcStatusUtilImpl().BuildGrpcError(guruErr)
	}

	// Verify if the request is valid for the current resource
	guruErr = VerifyIfValidRequestForCurrentResource(resourceExtId, operation)
	if guruErr != nil {
		return nil, "", "", "",
			[]*ntnxApiGuruError.AppMessage{guruErr.ConvertToAppMessagePb()},
			grpc_error.GetGrpcStatusUtilImpl().BuildGrpcError(guruErr)
	}

	// Extract gRPC request context from ctx.
	requestContext, err := grpcUtil.GetRequestContextFromContext(ctx)
	if err != nil {
		errorResp := fmt.Sprintf("error fetching context from grpc request %s", err)
		log.Errorf(errorResp)
		guruErr := guru_error.GetInternalError(operation)
		return nil, "", "", "", []*ntnxApiGuruError.AppMessage{guruErr.ConvertToAppMessagePb()},
			grpc_error.GetGrpcStatusUtilImpl().BuildGrpcError(guruErr)
	}

	// Idempotency check for the request
	taskUuid, err := external.Interfaces().IdempotencyService().GetTask(requestId)
	if err != nil {
		errorResp := fmt.Sprintf("Failed to do an idempotency check for request id %s with error: %s", requestId, err)
		log.Errorf(errorResp)
		guruErr := guru_error.GetInternalError(operation)
		return nil, "", "", "", []*ntnxApiGuruError.AppMessage{guruErr.ConvertToAppMessagePb()},
			grpc_error.GetGrpcStatusUtilImpl().BuildGrpcError(guruErr)
	}

	if taskUuid != nil {
		log.Infof("Request with requestId %s is already processed with task uuid %s", requestId, *taskUuid)
		return requestContext, requestId, batchTaskId, *taskUuid, nil, nil
	}

	return requestContext, requestId, batchTaskId, "", nil, nil
}

func CreateTaskRequestContextFromGrpcContext(requestContext *utilNet.RpcRequestContext) utilNet.RpcRequestContext {
	taskRequestContext := utilNet.RpcRequestContext{
		UserName:       requestContext.UserName,
		UserUuid:       requestContext.UserUuid,
		UserGroupUuids: requestContext.UserGroupUuids,
		UserSessionId:  requestContext.UserSessionId,
	}

	return taskRequestContext
}

func FetchRequestIdFromMetadata(ctx context.Context) string {
	// Extract requestId from context metadata
	md, _ := metadata.FromIncomingContext(ctx)
	requestIdList, _ := md[consts.XRequestId]
	// Return if any type of the request-Id is populated, empty string if none is present.
	switch {
	case len(requestIdList) > 0:
		return requestIdList[0]
	default:
		return ""
	}
}

func FetchHostName(ctx context.Context) string {
	if p, ok := peer.FromContext(ctx); ok {
		if ip, _, err := net.SplitHostPort(p.Addr.String()); err == nil {
			return ip
		}
	}
	return ""
}

func fetchUsernameFromMetadata(ctx context.Context) string {
	// Fetch request context from incoming context.
	requestContext, err := grpcUtils.GetRequestContextFromContext(ctx)
	if err != nil {
		log.Errorf("Failed to extract context : %s", err)
		return ""
	}

	return requestContext.GetUserName()
}

func fetchIssuerFromToken(md metadata.MD) string {
	serviceToken, ok := md[consts.XNtnxServiceToken]
	if !ok {
		log.Errorf("Could not extract ntnx service token from context")
		return ""
	}

	if len(serviceToken) == 0 {
		log.Errorf("Invalid service token in metadata")
		return ""
	}

	decodedToken, err := DecodeJwt(serviceToken[0])
	if err != nil {
		log.Errorf("Failed to decode JWT with error : %s", err)
		return ""
	}

	iss, ok := decodedToken["iss"]
	if !ok {
		log.Errorf("No valid issuer found")
		return ""
	}

	return iss.(string)
}

func FetchUsername(ctx context.Context) string {
	username := fetchUsernameFromMetadata(ctx)
	if username == "" {
		md, ok := metadata.FromIncomingContext(ctx)
		if !ok {
			log.Errorf("Unable to extract metadata from context")
			return ""
		}
		username = fetchIssuerFromToken(md)
	}

	return username
}
