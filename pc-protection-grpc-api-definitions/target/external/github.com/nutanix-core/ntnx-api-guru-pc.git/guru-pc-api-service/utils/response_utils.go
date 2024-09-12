/*
* Copyright (c) 2024 Nutanix Inc. All rights reserved.
*
* Author: kshitij.kumar@nutanix.com
*
* This file contains response util methods
 */
package utils

import (
	"encoding/json"
	"errors"
	"fmt"
	"net/http"
	commonConfig "ntnx-api-guru-pc/generated-code/target/protobuf/common/v1/config"
	"ntnx-api-guru-pc/generated-code/target/protobuf/common/v1/response"
	"ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/config"
	ntnxApiGuruError "ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/error"
	"ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/management"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"reflect"

	"google.golang.org/protobuf/proto"
	"github.com/nutanix-core/go-cache/ergon"
	common_consts "github.com/nutanix-core/ntnx-api-guru/services/constants"
	"github.com/nutanix-core/ntnx-api-utils-go/protoutils"
	"github.com/nutanix-core/ntnx-api-utils-go/responseutils"
	"github.com/nutanix-core/ntnx-api-utils-go/taskutils"
	log "github.com/sirupsen/logrus"
)

func CreateRegisterTaskResponse(taskUuid string, hasError bool, isPaginated bool, rel string) *management.RegisterRet {
	taskExtId := taskutils.GetTaskReferenceExtIdFromTaskUuid(taskUuid)
	url := CreateTaskUrl(taskExtId)
	return &management.RegisterRet{
		Content: &management.RegisterApiResponse{
			Data: &management.RegisterApiResponse_TaskReferenceData{
				TaskReferenceData: &management.TaskReferenceWrapper{
					Value: &config.TaskReference{
						ExtId: proto.String(taskExtId),
					},
				},
			},
			Metadata: CreateResponseMetadata(hasError, isPaginated, url, consts.RelSelf),
		},
	}
}

func CreateUnregisterTaskResponse(taskUuid string, hasError bool, isPaginated bool, rel string) *management.UnregisterRet {
	taskExtId := taskutils.GetTaskReferenceExtIdFromTaskUuid(taskUuid)
	url := CreateTaskUrl(taskExtId)
	return &management.UnregisterRet{
		Content: &management.UnregisterApiResponse{
			Data: &management.UnregisterApiResponse_TaskReferenceData{
				TaskReferenceData: &management.TaskReferenceWrapper{
					Value: &config.TaskReference{
						ExtId: proto.String(taskExtId),
					},
				},
			},
			Metadata: CreateResponseMetadata(hasError, isPaginated, url, consts.RelSelf),
		},
	}
}

func CreateResponseMetadata(hasError bool, isPaginated bool, url string, rel string) *response.ApiResponseMetadata {
	var links *response.ApiLinkArrayWrapper
	if url != "" {
		links = AddToHateOASLinks(links, url, rel)
	}
	return &response.ApiResponseMetadata{
		Flags: CreateMetadataFlags(hasError, isPaginated),
		Links: links,
	}
}

func CreateMetadataFlags(hasError bool, isPaginated bool) *commonConfig.FlagArrayWrapper {
	return &commonConfig.FlagArrayWrapper{
		Value: []*commonConfig.Flag{
			{
				Name:  proto.String(consts.HasError),
				Value: proto.Bool(hasError),
			},
			{
				Name:  proto.String(consts.IsPaginated),
				Value: proto.Bool(isPaginated),
			},
		},
	}
}

// AddToHateOASLinks() adds a link to the hateoas links, if the linksWrapper is nil, it creates a new one
// args: linksWrapper: existing linksWrapper, url: url to add, rel: relation to add
func AddToHateOASLinks(linksWrapper *response.ApiLinkArrayWrapper, url string,
	rel string) *response.ApiLinkArrayWrapper {

	if linksWrapper == nil {
		// Create a new link wrapper
		return &response.ApiLinkArrayWrapper{
			Value: []*response.ApiLink{
				{
					Href: proto.String(url),
					Rel:  proto.String(rel),
				},
			},
		}
	}

	// Append the new link to the existing link wrapper
	linksWrapper.Value = append(linksWrapper.Value, &response.ApiLink{
		Href: proto.String(url),
		Rel:  proto.String(rel),
	})
	return linksWrapper
}

func CreateTaskUrl(taskExtId string) string {
	hostPort := fmt.Sprintf("%s:%v", consts.HostAddr, consts.EnvoyPort)
	return responseutils.GetTaskUri(hostPort, *consts.TasksApiVersion, taskExtId)
}

// CreateGetDomainManagerUrl() creates the hateos url for the get domain manager api
// args: getResponse: get domain manager response
func CreateGetDomainManagerUrl(getResponse *config.DomainManager) string {
	hostPort := fmt.Sprintf("%s:%v", consts.HostAddr, consts.EnvoyPort)
	relativePath := fmt.Sprintf(consts.GetDomainManagerUrlSuffix, getResponse.Base.GetExtId())
	return responseutils.GetFullUri(relativePath, hostPort, *consts.ConfigModuleVersion)
}

// CreateNodeExtIdsUrls() creates the hateos urls to get vms(node ext id)
// args: getResponse: get domain manager response
func CreateNodeExtIdsUrls(getResponse *config.DomainManager) []string {
	if getResponse.GetNodeExtIds() == nil {
		log.Warnf("Failed to populate hateos links for node ext ids : NodeExtIds not found")
		return nil
	}
	var nodeExtIdsUrls []string
	hostPort := fmt.Sprintf("%s:%v", consts.HostAddr, consts.EnvoyPort)
	for _, nodeExtId := range getResponse.GetNodeExtIds().GetValue() {
		relativePath := fmt.Sprintf(consts.GetNodeExtIdUrlSuffix, nodeExtId)
		nodeExtIdsUrls = append(nodeExtIdsUrls, responseutils.GetFullUri(relativePath, hostPort, *consts.VmmApiVersion))
	}
	return nodeExtIdsUrls
}

// CreateContainerExtIdsUrls() creates the hateos urls to get containers
// args: getResponse: get domain manager response
func CreateContainerExtIdsUrls(getResponse *config.DomainManager) []string {
	if getResponse.GetConfig() == nil || getResponse.GetConfig().GetResourceConfig() == nil ||
		getResponse.GetConfig().GetResourceConfig().GetContainerExtIds() == nil {
		log.Warnf("Failed to populate hateos links for containers : ContainerExtIds not found")
		return nil
	}
	var containerExtIds []string
	hostPort := fmt.Sprintf("%s:%v", consts.HostAddr, consts.EnvoyPort)
	for _, containerExtId := range getResponse.GetConfig().GetResourceConfig().GetContainerExtIds().GetValue() {
		relativePath := fmt.Sprintf(consts.GetContainerExtIdUrlSuffix, containerExtId)
		containerExtIds = append(containerExtIds, responseutils.GetFullUri(relativePath, hostPort, *consts.ClusterMgmtApiVersion))
	}
	return containerExtIds
}

// CreateListDomainManagerResponseMetadata() creates the metadata for the list domain manager api
// args: hasError: if the response has error, isPaginated: if the response is paginated,
// listResponse: response of list domain manager api
func CreateListDomainManagerResponseMetadata(hasError bool, isPaginated bool,
	listResponse []*config.DomainManager) *response.ApiResponseMetadata {

	getDomainMangerUrl := CreateGetDomainManagerUrl(listResponse[0])
	links := AddToHateOASLinks(nil, getDomainMangerUrl, consts.RelSelf)
	AddToHateOASLinks(links, getDomainMangerUrl, consts.RelFirst)
	AddToHateOASLinks(links, getDomainMangerUrl, consts.RelLast)
	return &response.ApiResponseMetadata{
		Flags:                 CreateMetadataFlags(hasError, isPaginated),
		Links:                 links,
		TotalAvailableResults: proto.Int32(int32(len(listResponse))),
	}
}

// CreateGetDomainManagerResponseMetadata() creates the metadata for the get domain manager api
// args: hasError: if the response has error, isPaginated: if the response is paginated,
// getResponse: response of get domain manager api
func CreateGetDomainManagerResponseMetadata(hasError bool, isPaginated bool,
	getResponse *config.DomainManager) *response.ApiResponseMetadata {

	getDomainMangerUrl := CreateGetDomainManagerUrl(getResponse)
	links := AddToHateOASLinks(nil, getDomainMangerUrl, consts.RelSelf)

	nodeUrls := CreateNodeExtIdsUrls(getResponse)
	for _, nodeUrl := range nodeUrls {
		AddToHateOASLinks(links, nodeUrl, consts.RelNodeExtId)
	}

	containerUrls := CreateContainerExtIdsUrls(getResponse)
	for _, containerUrl := range containerUrls {
		AddToHateOASLinks(links, containerUrl, consts.RelContainerExtId)
	}

	return &response.ApiResponseMetadata{
		Flags: CreateMetadataFlags(hasError, isPaginated),
		Links: links,
	}
}

func PopulateErrorArgs(errorArgs map[string]string) []*ergon.ErrorArg {
	if errorArgs == nil || len(errorArgs) == 0 {
		return nil
	}
	errorArgDTO := []*ergon.ErrorArg{}
	for key, val := range errorArgs {
		errorArgDTO = append(errorArgDTO, &ergon.ErrorArg{
			Name:  proto.String(key),
			Value: proto.String(val),
		})
	}
	return errorArgDTO
}

func CreateErrorMessageDetails(
	errorDetailsList []*models.ErrorDetails,
) []*ergon.Error {
	var errors []*ergon.Error
	for _, errorDetails := range errorDetailsList {
		errors = append(errors, &ergon.Error{
			ErrorCode:      proto.Int32(errorDetails.ErrorCode),
			ErrorNamespace: proto.String(consts.PrismErrorNamespace),
			ErrorPrefix:    proto.String(consts.PrismErrorPrefix),
			Args:           PopulateErrorArgs(errorDetails.ErrorArgs),
		})
	}
	return errors
}

func CreateErrorDetailsDTO(
	errorCode int32,
	errorMessage string,
	operation string,
) []*models.ErrorDetails {
	return []*models.ErrorDetails{
		{
			ErrorCode: errorCode,
			ErrorArgs: map[string]string{
				consts.ErrorMessageArg: errorMessage,
				consts.OperationArg:    operation,
			},
		},
	}
}

func CreateAddRootCertResponse(taskUuid string) *management.AddRootCertificateRet {
	taskExtId := taskutils.GetTaskReferenceExtIdFromTaskUuid(taskUuid)
	url := CreateTaskUrl(taskExtId)
	return &management.AddRootCertificateRet{
		Content: &management.AddRootCertificateApiResponse{
			Data: &management.AddRootCertificateApiResponse_TaskReferenceData{
				TaskReferenceData: &management.TaskReferenceWrapper{
					Value: &config.TaskReference{
						ExtId: proto.String(taskExtId),
					},
				},
			},
			Metadata: CreateResponseMetadata(false, false, url, consts.RelSelf),
		},
	}
}

func CreateConfigureConnectionTaskResponse(taskUuid string) *management.ConfigureConnectionRet {
	taskExtId := taskutils.GetTaskReferenceExtIdFromTaskUuid(taskUuid)
	url := CreateTaskUrl(taskExtId)
	return &management.ConfigureConnectionRet{
		Content: &management.ConfigureConnectionApiResponse{
			Data: &management.ConfigureConnectionApiResponse_TaskReferenceData{
				TaskReferenceData: &management.TaskReferenceWrapper{
					Value: &config.TaskReference{
						ExtId: proto.String(taskExtId),
					},
				},
			},
			Metadata: CreateResponseMetadata(false, false, url, consts.RelSelf),
		},
	}
}

func CreateRemoveRootCertificateResponse(taskUuid string) *management.RemoveRootCertificateRet {
	taskExtId := taskutils.GetTaskReferenceExtIdFromTaskUuid(taskUuid)
	url := CreateTaskUrl(taskExtId)
	return &management.RemoveRootCertificateRet{
		Content: &management.RemoveRootCertificateApiResponse{
			Data: &management.RemoveRootCertificateApiResponse_TaskReferenceData{
				TaskReferenceData: &management.TaskReferenceWrapper{
					Value: &config.TaskReference{
						ExtId: proto.String(taskExtId),
					},
				},
			},
			Metadata: CreateResponseMetadata(false, false, url, consts.RelSelf),
		},
	}
}

func CreateUnconfigureConnectionTaskResponse(taskUuid string) *management.UnconfigureConnectionRet {
	taskExtId := taskutils.GetTaskReferenceExtIdFromTaskUuid(taskUuid)
	url := CreateTaskUrl(taskExtId)
	return &management.UnconfigureConnectionRet{
		Content: &management.UnconfigureConnectionApiResponse{
			Data: &management.UnconfigureConnectionApiResponse_TaskReferenceData{
				TaskReferenceData: &management.TaskReferenceWrapper{
					Value: &config.TaskReference{
						ExtId: proto.String(taskExtId),
					},
				},
			},
			Metadata: CreateResponseMetadata(false, false, url, consts.RelSelf),
		},
	}
}

// In case of 4XX / 5XX, the API client returns an error
// with the response in the form of a json string.
// The response from Prism Gateway in case of 4XX and 5XX
// has a message field.
// This function extracts the message.
func GetErrorMessageFromJsonResponse(err error) error {
	var result struct {
		Message string `json:"message"`
	}
	unmarshalErr := json.Unmarshal([]byte(err.Error()), &result)
	if unmarshalErr != nil {
		log.Errorf("Failed to unmarshal error response with error: %s", unmarshalErr)
		return err
	}
	if result.Message != "" {
		return fmt.Errorf("%s", result.Message)
	}
	return err
}

// This function returns the appropriate Guru Error from the http response
// based on the status code and the error message from the API client.
func GetErrorFromStatusCode(statusCode *int, err error, operation string) guru_error.GuruErrorInterface {
	// For the case when the remote cluster Address is not reachable, the status code returned
	// by the http client is nil. So, checking for the error message in that case.
	if statusCode == nil {
		if err != nil && errors.Is(err, common_consts.ErrorClientTimeout) {
			return guru_error.GetGatewayTimeoutError(operation)
		} else {
			return guru_error.GetInternalError(operation)
		}
	}

	switch *statusCode {
	case http.StatusUnauthorized:
		return guru_error.GetUnauthorizedRequestError(operation)
	case http.StatusForbidden:
		return guru_error.GetForbiddenRequestError(operation)
	case http.StatusServiceUnavailable:
		return guru_error.GetServiceUnavailableError(operation)
	case http.StatusNotFound, http.StatusMethodNotAllowed:
		return guru_error.GetOperationNotSupportedOnRemote(operation)
	case http.StatusInternalServerError:
		// In case of making requests to a remote cluster <= 6.8,
		// and the endpoint does not exist on the remote cluster, the response
		// is a json string with a specific message field.
		// So, checking for that specific message.
		err = GetErrorMessageFromJsonResponse(err)
		if err.Error() == consts.ErrorActionNotSupportedOnPrismGateway.Error() {
			return guru_error.GetOperationNotSupportedOnRemote(operation)
		} else {
			return guru_error.GetInternalError(operation)
		}
	default:
		return guru_error.GetInternalError(operation)
	}
}

// This function returns the appropriate error message from the http response
// based on the status code and the error message from the API client.
func GetErrorMessageFromHttpResponse(err error, statusCode *int) error {
	// For the case when the remote cluster Address is not reachable, the status code returned
	// by the http client is nil. So, checking for the error message in that case.
	if statusCode == nil {
		if err != nil && errors.Is(err, common_consts.ErrorClientTimeout) {
			return consts.ErrorRemoteClusterNotReachable
		} else {
			return consts.ErrorInternalServerError
		}
	}

	switch *statusCode {
	case http.StatusUnauthorized:
		return consts.ErrorUnauthorizedAccess
	case http.StatusForbidden:
		return consts.ErrorForbiddenAccess
	case http.StatusNotFound:
		return consts.ErrorOperationNotSupported
	case http.StatusInternalServerError:
		// In case of making requests to a remote cluster <= 6.8,
		// and the endpoint does not exist on the remote cluster, the response
		// is a json string with a specific message field.
		// So, checking for that specific message.
		err = GetErrorMessageFromJsonResponse(err)
		if err.Error() == consts.ErrorActionNotSupportedOnPrismGateway.Error() {
			return consts.ErrorOperationNotSupported
		} else {
			return consts.ErrorInternalServerError
		}
	case http.StatusServiceUnavailable:
		return consts.ErrorRemoteClusterNotAvailable
	default:
		return consts.ErrorInternalServerError
	}
}

func getConfigErrorResponseData(appMessageList []*ntnxApiGuruError.AppMessage) *config.ErrorResponseWrapper {
	return &config.ErrorResponseWrapper{
		Value: &ntnxApiGuruError.ErrorResponse{
			Error: &ntnxApiGuruError.ErrorResponse_AppMessageArrayError{
				AppMessageArrayError: &ntnxApiGuruError.AppMessageArrayWrapper{
					Value: appMessageList,
				},
			},
		},
	}
}

func getManagementErrorResponseData(appMessageList []*ntnxApiGuruError.AppMessage) *management.ErrorResponseWrapper {
	return &management.ErrorResponseWrapper{
		Value: &ntnxApiGuruError.ErrorResponse{
			Error: &ntnxApiGuruError.ErrorResponse_AppMessageArrayError{
				AppMessageArrayError: &ntnxApiGuruError.AppMessageArrayWrapper{
					Value: appMessageList,
				},
			},
		},
	}
}

func CreateUnregisterErrorResponse(appMessageList []*ntnxApiGuruError.AppMessage) *management.UnregisterRet {
	return &management.UnregisterRet{
		Content: &management.UnregisterApiResponse{
			Data: &management.UnregisterApiResponse_ErrorResponseData{
				ErrorResponseData: getManagementErrorResponseData(appMessageList),
			},
			Metadata: CreateResponseMetadata(true, false, "", ""),
		},
	}
}

func CreateRegisterErrorResponse(appMessageList []*ntnxApiGuruError.AppMessage) *management.RegisterRet {
	return &management.RegisterRet{
		Content: &management.RegisterApiResponse{
			Data: &management.RegisterApiResponse_ErrorResponseData{
				ErrorResponseData: getManagementErrorResponseData(appMessageList),
			},
			Metadata: CreateResponseMetadata(true, false, "", ""),
		},
	}
}

func CreateAddRootCertificateErrorResponse(appMessageList []*ntnxApiGuruError.AppMessage) *management.AddRootCertificateRet {
	return &management.AddRootCertificateRet{
		Content: &management.AddRootCertificateApiResponse{
			Data: &management.AddRootCertificateApiResponse_ErrorResponseData{
				ErrorResponseData: getManagementErrorResponseData(appMessageList),
			},
			Metadata: CreateResponseMetadata(true, false, "", ""),
		},
	}
}

func CreateUnconfigureConnectionErrorResponse(appMessageList []*ntnxApiGuruError.AppMessage) *management.UnconfigureConnectionRet {
	return &management.UnconfigureConnectionRet{
		Content: &management.UnconfigureConnectionApiResponse{
			Data: &management.UnconfigureConnectionApiResponse_ErrorResponseData{
				ErrorResponseData: getManagementErrorResponseData(appMessageList),
			},
			Metadata: CreateResponseMetadata(true, false, "", ""),
		},
	}
}

func CreateRemoveRootCertErrorResponse(appMessageList []*ntnxApiGuruError.AppMessage) *management.RemoveRootCertificateRet {
	return &management.RemoveRootCertificateRet{
		Content: &management.RemoveRootCertificateApiResponse{
			Data: &management.RemoveRootCertificateApiResponse_ErrorResponseData{
				ErrorResponseData: getManagementErrorResponseData(appMessageList),
			},
			Metadata: CreateResponseMetadata(true, false, "", ""),
		},
	}
}

func CreateConfigureConnectionErrorResponse(appMessageList []*ntnxApiGuruError.AppMessage) *management.ConfigureConnectionRet {
	return &management.ConfigureConnectionRet{
		Content: &management.ConfigureConnectionApiResponse{
			Data: &management.ConfigureConnectionApiResponse_ErrorResponseData{
				ErrorResponseData: getManagementErrorResponseData(appMessageList),
			},
			Metadata: CreateResponseMetadata(true, false, "", ""),
		},
	}
}

// CreateErrorResponse creates a get domain api error response with the given appMessageList
// args: appMessageList: list of app messages
// returns: *management.GetDomainManagerRet
func CreateGetDomainManagerErrorResponse(appMessageList []*ntnxApiGuruError.AppMessage,
) *config.GetDomainManagerByIdRet {
	return &config.GetDomainManagerByIdRet{
		Content: &config.GetDomainManagerApiResponse{
			Data: &config.GetDomainManagerApiResponse_ErrorResponseData{
				ErrorResponseData: getConfigErrorResponseData(appMessageList),
			},
			Metadata: CreateResponseMetadata(true, false, "", ""),
		},
	}
}

// CreateListDomainManagerErrorResponse creates a list domain api error response with the given appMessageList
// args: appMessageList: list of app messages
// returns: *management.ListDomainManagersRet
func CreateListDomainManagerErrorResponse(appMessageList []*ntnxApiGuruError.AppMessage,
) *config.ListDomainManagersRet {
	return &config.ListDomainManagersRet{
		Content: &config.ListDomainManagerApiResponse{
			Data: &config.ListDomainManagerApiResponse_ErrorResponseData{
				ErrorResponseData: getConfigErrorResponseData(appMessageList),
			},
			Metadata: CreateResponseMetadata(true, false, "", ""),
		},
	}
}

// CreateGetDomainManagerResponse creates the response for GetDomainManager API
// args: response - in-memory domain manager entity
// returns: GetDomainManagerRet response
func CreateGetDomainManagerResponse(response *config.DomainManager,
) *config.GetDomainManagerByIdRet {
	return &config.GetDomainManagerByIdRet{
		Content: &config.GetDomainManagerApiResponse{
			Data: &config.GetDomainManagerApiResponse_DomainManagerData{
				DomainManagerData: &config.DomainManagerWrapper{
					Value: response,
				},
			},
			Metadata: CreateGetDomainManagerResponseMetadata(false, false, response),
		},
	}
}

// CreateListDomainManagerResponse creates the response for ListDomainManagers API
// args: response - in-memory domain manager entity list
// returns: ListDomainManagersRet response
func CreateListDomainManagerResponse(response []*config.DomainManager,
) *config.ListDomainManagersRet {
	return &config.ListDomainManagersRet{
		Content: &config.ListDomainManagerApiResponse{
			Data: &config.ListDomainManagerApiResponse_DomainManagerArrayData{
				DomainManagerArrayData: &config.DomainManagerArrayWrapper{
					Value: response,
				},
			},
			Metadata: CreateListDomainManagerResponseMetadata(false, false, response),
		},
	}
}

// Returns a truncated response
// Parameters for truncation: maxSize - 1, maxDepth - 1
//  1. maxSize as 1 denotes that the response will be truncated to have the number of
//     List elements as only 1.
//  2. maxDepth denotes truncation will be applied only on the outermost layer of the
//     response content.
//
// Since LIST Domain managers always returns 1 Domain Manager, this truncation becomes a no-op.
func TruncateListDomainManagerResponse(response *config.ListDomainManagersRet) error {
	protoRet, err := protoutils.TruncateListProtoWithTypeHints(
		response.Content, consts.DomainManagerListMaxSize, consts.DomainManagerListMaxDepth,
		reflect.TypeOf(&commonConfig.Flag{}), reflect.TypeOf(&commonConfig.KVPair{}))
	if err != nil {
		return err
	}
	result := &config.ListDomainManagerApiResponse{}
	proto.Merge(result, proto.Message(protoRet))
	response.Content = result
	return nil
}
