/*
* Copyright (c) 2024 Nutanix Inc. All rights reserved.
*
* Author: harsh.seta@nutanix.com
*
* This gRPC status builder utility is used to build gRPC status from GuruError
 */

package grpc_error

import (
	"strconv"
	"sync"

	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	guruError "ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"

	statusPb "google.golang.org/genproto/googleapis/rpc/status"

	"github.com/nutanix-core/ntnx-api-utils-go/errorutils"
	log "github.com/sirupsen/logrus"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

// singleton instance of GrpcStatusUtil
var (
	grpcStatusUtilImpl     GrpcStatusUtil
	grpcStatusUtilImplOnce sync.Once
)

func GetGrpcStatusUtilImpl() GrpcStatusUtil {
	grpcStatusUtilImplOnce.Do(func() {
		if grpcStatusUtilImpl == nil {
			grpcStatusUtilImpl = NewGrpcStatusUtilImpl()
		}
	})
	return grpcStatusUtilImpl
}

// For mocking purposes
func SetGrpcStatusUtil(grpcStatusUtil GrpcStatusUtil) {
	grpcStatusUtilImpl = grpcStatusUtil
}

type GrpcStatusUtil interface {
	BuildGrpcError(guruError.GuruErrorInterface) error
}

type GrpcStatusUtilImpl struct {
	errorutils.AppMessageBuilderInterface
}

func NewGrpcStatusUtilImpl() *GrpcStatusUtilImpl {
	return &GrpcStatusUtilImpl{
		AppMessageBuilderInterface: errorutils.NewAppMessageBuilder(),
	}
}

// This method builds a gRPC error with the given GuruError
// This gRPC error will be sent back to Adonis as a response
// The Client will see the appropriate error based on the mapping of Guru Error Code -> gRPC Code
func (e *GrpcStatusUtilImpl) BuildGrpcError(guruErr guruError.GuruErrorInterface) error {
	// Build a default internal error status
	internalErrStatus := &statusPb.Status{
		Code:    int32(codes.Internal),
		Message: guruErr.GetErrorDetail(),
	}
	appMessage, err := e.AppMessageBuilderInterface.BuildAppMessage(
		consts.EnglishLocale, consts.PrismErrorNamespace, consts.PrismErrorPrefix,
		strconv.Itoa(guruErr.GetErrorCode()), guruErr.ConvertToAppMessagePb().GetArgumentsMap().GetValue())
	if err != nil {
		log.Errorf("Failed to build app message: %s", err)
		appMessage, err = e.AppMessageBuilderInterface.BuildAppMessage(consts.EnglishLocale,
			consts.PrismErrorNamespace, consts.PrismErrorPrefix, strconv.Itoa(consts.ErrorCodeInternalError), nil)
		if err != nil {
			log.Errorf("Failed to build app message for internal error: %s", err)
			return status.ErrorProto(internalErrStatus)
		}
	}
	grpcStatus, err := errorutils.BuildGrpcStatus(int32(guruError.GetGrpcCode(guruErr)), appMessage)
	if err != nil {
		log.Errorf("Failed to build gRPC status from app message: %s", err)
		return status.ErrorProto(internalErrStatus)
	}
	return status.ErrorProto(grpcStatus)
}
