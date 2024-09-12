/*
* Copyright (c) 2024 Nutanix Inc. All rights reserved.
*
* Author: harsh.seta@nutanix.com
*
* This package contains the Guru-specific custom error interface,
* All the external facing errors have been made to type "GuruError"
* Going forward, all the errors should be of type "GuruError"
 */

package guru_error

import (
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"strconv"

	ntnxApiGuruError "ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/error"

	"github.com/nutanix-core/go-cache/ergon"
	"github.com/nutanix-core/go-cache/util-go/errors"
	log "github.com/sirupsen/logrus"
	"google.golang.org/grpc/codes"
	"google.golang.org/protobuf/proto"
)

var guruErrorCodeGrpcCodeMapping = map[int]codes.Code{
	consts.ErrorCodeInternalError:                codes.Internal,
	consts.ErrorCodeRequestArgumentNotFound:      codes.InvalidArgument,
	consts.ErrorCodeRequestArgumentInvalidFormat: codes.InvalidArgument,
	consts.ErrorCodeInvalidArgument:              codes.InvalidArgument,
	consts.ErrorCodeMissingETag:                  codes.InvalidArgument,
	consts.ErrorCodeInvalidETag:                  codes.InvalidArgument,
	consts.ErrorCodeInvalidRemoteEntity:          codes.InvalidArgument,
	consts.ErrorCodeETagNotMatched:               codes.FailedPrecondition,
	consts.ErrorCodeUnsupported:                  codes.Unimplemented,
	consts.ErrorCodeServiceUnavailable:           codes.Unavailable,
	consts.ErrorCodePreconditionFailed:           codes.FailedPrecondition,
	consts.ErrorCodeUnauthorizedRequest:          codes.Unauthenticated,
	consts.ErrorCodeForbiddenRequest:             codes.PermissionDenied,
	consts.ErrorCodeGatewayTimeout:               codes.DeadlineExceeded,
	consts.ErrorCodeNotSupportedOnRemote:         codes.NotFound,
	consts.ErrorCodeResourceNotFound:             codes.NotFound,
}

type GuruErrorInterface interface {
	errors.INtnxError
	GetTaskErrorDetails() []*ergon.Error
	ConvertToAppMessagePb() *ntnxApiGuruError.AppMessage
	GetArgMap() map[string]string
}

// GuruError defines a Guru specific custom error.
type GuruError struct {
	*errors.NtnxError
	argMap map[string]string
}

// Internal error.
type InternalError struct {
	*GuruError
}

// Invalid request error with a missing argument.
type RequestArgumentNotFoundError struct {
	*GuruError
}

// Invalid request error with an argument in an invalid format.
type RequestArgumentFormatInvalidError struct {
	*GuruError
}

// Invalid remote cluster uuid in the request
type InvalidArgument struct {
	*GuruError
}

// Missing Etag error.
type MissingEtagError struct {
	*GuruError
}

// Invalid Etag error.
type InvalidEtagError struct {
	*GuruError
}

// Invalid remote entity specified
type InvalidRemoteEntityError struct {
	*GuruError
}

// Etag mismatch error.
type EtagMismatchError struct {
	*GuruError
}

// Precondition failure
type PreconditionFailureError struct {
	*GuruError
}

// Unsupported operation
type UnsupportedOperationError struct {
	*GuruError
}

// Service unavailability
type ServiceUnavailableError struct {
	*GuruError
}

// Unauthorized request
type UnauthorizedRequestError struct {
	*GuruError
}

// Forbidden request
type ForbiddenRequestError struct {
	*GuruError
}

// Gateway timeout during  execution
type GatewayTimeoutError struct {
	*GuruError
}

// Operation Not supported on remote cluster
type OperationNotSupportedOnRemoteError struct {
	*GuruError
}

// Resource not found error
type ResourceNotFound struct {
	*GuruError
}

// This method creates the error details from the guru error
func (e *GuruError) GetTaskErrorDetails() []*ergon.Error {
	var errorArgs []*ergon.ErrorArg = []*ergon.ErrorArg{}
	for key, value := range e.argMap {
		errorArgs = append(errorArgs, &ergon.ErrorArg{
			Name:  proto.String(key),
			Value: proto.String(value),
		})
	}

	var errorDetails []*ergon.Error
	errorDetails = append(errorDetails, &ergon.Error{
		ErrorCode:      proto.Int32(int32(e.GetErrorCode())),
		ErrorNamespace: proto.String(consts.PrismErrorNamespace),
		ErrorPrefix:    proto.String(consts.PrismErrorPrefix),
		Args:           errorArgs,
	})

	return errorDetails
}

// This method converts the Guru error to an AppMessage proto which can be sent to the client.
func (e *GuruError) ConvertToAppMessagePb() *ntnxApiGuruError.AppMessage {
	return &ntnxApiGuruError.AppMessage{
		Code: proto.String(strconv.Itoa(e.GetErrorCode())),
		ArgumentsMap: &ntnxApiGuruError.StringMapWrapper{
			Value: e.argMap,
		},
	}
}

// This method gets the gRPC status code that maps to the Guru error type.
func GetGrpcCode(guruError GuruErrorInterface) codes.Code {
	if grpcCode, found := guruErrorCodeGrpcCodeMapping[guruError.GetErrorCode()]; found {
		return grpcCode
	} else {
		log.Warningf("Unable to map Guru error '%s' to gRPC code.", guruError)
		return codes.Internal
	}
}

// This method creates a new Guru error.
func NewGuruErrorWithMessage(errMsg string, errCode int, argMap map[string]string) *GuruError {
	return &GuruError{
		NtnxError: errors.NewNtnxError(errMsg, errCode),
		argMap:    argMap,
	}
}

func NewGuruError(errCode int, argMap map[string]string) *GuruError {
	return &GuruError{
		NtnxError: errors.NewNtnxError("", errCode),
		argMap:    argMap,
	}
}

func (e *GuruError) GetArgMap() map[string]string {
	return e.argMap
}

func GetInternalError(operation string) *InternalError {
	argMap := map[string]string{
		consts.OperationArg: operation,
	}
	err := &InternalError{
		GuruError: NewGuruError(consts.ErrorCodeInternalError, argMap),
	}
	return err
}

func GetRequestArgumentNotFoundError(operation string, arg string) *RequestArgumentNotFoundError {
	argMap := map[string]string{
		consts.ArgumentArg:  arg,
		consts.OperationArg: operation,
	}
	err := &RequestArgumentNotFoundError{
		GuruError: NewGuruError(consts.ErrorCodeRequestArgumentNotFound, argMap),
	}
	return err
}

func GetRequestArgumentFormatInvalidError(operation string, arg string) *RequestArgumentFormatInvalidError {
	argMap := map[string]string{
		consts.ArgumentArg:  arg,
		consts.OperationArg: operation,
	}
	err := &RequestArgumentFormatInvalidError{
		GuruError: NewGuruError(consts.ErrorCodeRequestArgumentInvalidFormat, argMap),
	}
	return err
}

func GetInvalidArgumentError(operation string, value string, arg string) *InvalidArgument {
	argMap := map[string]string{
		consts.ArgumentKeyArg:   arg,
		consts.ArgumentValueArg: value,
		consts.OperationArg:     operation,
	}
	err := &InvalidArgument{
		GuruError: NewGuruError(consts.ErrorCodeInvalidArgument, argMap),
	}
	return err
}

func GetMissingEtagError(operation string) *MissingEtagError {
	argMap := map[string]string{
		consts.OperationArg: operation,
	}
	err := &MissingEtagError{
		GuruError: NewGuruError(consts.ErrorCodeMissingETag, argMap),
	}
	return err
}

func GetInvalidEtagError(operation string, requestETag string) *InvalidEtagError {
	argMap := map[string]string{
		consts.RequestETagArg: requestETag,
		consts.OperationArg:   operation,
	}
	err := &InvalidEtagError{
		GuruError: NewGuruError(consts.ErrorCodeInvalidETag, argMap),
	}
	return err
}

func GetInvalidRemoteEntityError(operation string) *InvalidRemoteEntityError {
	argMap := map[string]string{
		consts.OperationArg: operation,
	}
	err := &InvalidRemoteEntityError{
		GuruError: NewGuruError(consts.ErrorCodeInvalidETag, argMap),
	}
	return err
}

func GetETagMismatchError(operation string, requestETag string, currentETag string) *EtagMismatchError {
	argMap := map[string]string{
		consts.CurrentETagArg: currentETag,
		consts.RequestETagArg: requestETag,
		consts.OperationArg:   operation,
	}
	err := &EtagMismatchError{
		GuruError: NewGuruError(consts.ErrorCodeETagNotMatched, argMap),
	}
	return err
}

func GetPreconditionFailureError(errMsg string, operation string) *PreconditionFailureError {
	argMap := map[string]string{
		consts.OperationArg:    operation,
		consts.ErrorMessageArg: errMsg,
	}
	err := &PreconditionFailureError{
		GuruError: NewGuruError(consts.ErrorCodePreconditionFailed, argMap),
	}
	return err
}

func GetUnsupportedOperationError(operation string) *UnsupportedOperationError {
	argMap := map[string]string{
		consts.OperationArg: operation,
	}
	err := &UnsupportedOperationError{
		GuruError: NewGuruError(consts.ErrorCodeUnsupported, argMap),
	}
	return err
}

func GetServiceUnavailableError(operation string) *ServiceUnavailableError {
	argMap := map[string]string{
		consts.OperationArg: operation,
	}
	err := &ServiceUnavailableError{
		GuruError: NewGuruError(consts.ErrorCodeServiceUnavailable, argMap),
	}
	return err
}

func GetUnauthorizedRequestError(operation string) *UnauthorizedRequestError {
	argMap := map[string]string{
		consts.OperationArg: operation,
	}
	err := &UnauthorizedRequestError{
		GuruError: NewGuruError(consts.ErrorCodeUnauthorizedRequest, argMap),
	}
	return err
}

func GetForbiddenRequestError(operation string) *ForbiddenRequestError {
	argMap := map[string]string{
		consts.OperationArg: operation,
	}
	err := &ForbiddenRequestError{
		GuruError: NewGuruError(consts.ErrorCodeForbiddenRequest, argMap),
	}
	return err
}

func GetGatewayTimeoutError(operation string) *GatewayTimeoutError {
	argMap := map[string]string{
		consts.OperationArg: operation,
	}
	err := &GatewayTimeoutError{
		GuruError: NewGuruError(consts.ErrorCodeGatewayTimeout, argMap),
	}
	return err
}

func GetOperationNotSupportedOnRemote(operation string) *OperationNotSupportedOnRemoteError {
	argMap := map[string]string{
		consts.OperationArg: operation,
	}
	err := &OperationNotSupportedOnRemoteError{
		GuruError: NewGuruError(consts.ErrorCodeNotSupportedOnRemote, argMap),
	}
	return err
}

func GetResourceNotFoundError(operation string, extId string) *ResourceNotFound {
	argMap := map[string]string{
		consts.OperationArg:  operation,
		consts.ResourceIdArg: extId,
	}
	err := &ResourceNotFound{
		GuruError: NewGuruError(consts.ErrorCodeResourceNotFound, argMap),
	}
	return err
}
