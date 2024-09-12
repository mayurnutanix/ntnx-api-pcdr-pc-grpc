package utils

import (
	"encoding/json"
	"errors"
	"fmt"
	"net/http"
	ip_config "ntnx-api-guru-pc/generated-code/target/protobuf/common/v1/config"
	"ntnx-api-guru-pc/generated-code/target/protobuf/common/v1/response"
	"ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/config"
	ntnxApiGuruError "ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/error"
	"ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/management"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"testing"

	"github.com/golang/mock/gomock"
	"github.com/golang/protobuf/proto"
	"github.com/nutanix-core/go-cache/ergon"
	common_consts "github.com/nutanix-core/ntnx-api-guru/services/constants"
	"github.com/nutanix-core/ntnx-api-utils-go/taskutils"
	"github.com/stretchr/testify/assert"
)

func TestCreateHATEOSLinks(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	url := "url"
	rel := "rel"

	ret := AddToHateOASLinks(nil, url, rel)

	expectedResponse := response.ApiLinkArrayWrapper{
		Value: []*response.ApiLink{
			{
				Href: &url,
				Rel:  &rel,
			},
		},
	}
	assert.Equal(t, expectedResponse.Value, ret.Value)
}

func TestCreateMetadataFlags(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	var hasError = true
	var isPaginated = true

	result := CreateMetadataFlags(hasError, isPaginated)

	expectedResult := ip_config.FlagArrayWrapper{
		Value: []*ip_config.Flag{
			{
				Name:  &consts.HasError,
				Value: &hasError,
			},
			{
				Name:  &consts.IsPaginated,
				Value: &isPaginated,
			},
		},
	}
	assert.Equal(t, expectedResult.Value, result.Value)
}

func TestCreateResponseMetadata(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	var hasError = true
	var isPaginated = true
	var url = "url"
	var rel = "rel"

	result := CreateResponseMetadata(hasError, isPaginated, url, rel)

	expectedResult := response.ApiResponseMetadata{
		Flags: CreateMetadataFlags(hasError, isPaginated),
		Links: AddToHateOASLinks(nil, url, rel),
	}
	assert.Equal(t, expectedResult.Flags, result.Flags)
	assert.Equal(t, expectedResult.Links, result.Links)
}

func TestCreateTaskResponse(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	var taskUuid = "taskuuid"
	var hasError = true
	var isPaginated = true
	var rel = "rel"

	result := CreateRegisterTaskResponse(taskUuid, hasError, isPaginated, rel)

	taskExtId := taskutils.GetTaskReferenceExtIdFromTaskUuid(taskUuid)
	var url = CreateTaskUrl(taskExtId)
	expectedResult := management.RegisterRet{
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
	assert.Equal(t, expectedResult.Content, result.Content)
}

func TestCreateErrorMessageDetailsNil(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	var errorDetailsList []*models.ErrorDetails

	result := CreateErrorMessageDetails(errorDetailsList)

	var expectedError []*ergon.Error
	assert.Equal(t, expectedError, result)
}

func TestPopulateErrorArgsNil(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	var errorArgs map[string]string
	result := PopulateErrorArgs(errorArgs)
	assert.Nil(t, result)
}

func TestPopulateErrorArgs(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	errorArgs := make(map[string]string)
	errorArgs["key"] = "value"

	result := PopulateErrorArgs(errorArgs)

	expectedResult := []*ergon.ErrorArg{}
	expectedResult = append(expectedResult, &ergon.ErrorArg{
		Name:  proto.String("key"),
		Value: proto.String("value"),
	})
	assert.Equal(t, expectedResult, result)
}

func TestCreateErrorDetailsDTO(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	var errorCode int32 = 1
	errorMessage := "error message"
	operation := "operation"

	result := CreateErrorDetailsDTO(errorCode, errorMessage, operation)

	expectedError := []*models.ErrorDetails{
		{
			ErrorCode: errorCode,
			ErrorArgs: map[string]string{
				consts.ErrorMessageArg: errorMessage,
				consts.OperationArg:    operation,
			},
		},
	}
	assert.Equal(t, expectedError, result)
}

func TestExtractErrorMessageFromResponseUnauthorized(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	statusCode := http.StatusUnauthorized
	err := errors.New("")
	res := GetErrorMessageFromHttpResponse(err, &statusCode)
	assert.Equal(t, consts.ErrorUnauthorizedAccess, res)
}

func TestExtractErrorMessageFromResponseForbiddenAccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	statusCode := http.StatusForbidden
	err := errors.New("")
	res := GetErrorMessageFromHttpResponse(err, &statusCode)
	assert.Equal(t, consts.ErrorForbiddenAccess, res)
}

func TestExtractErrorMessageFromResponseOperationNotSupported(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	statusCode := http.StatusNotFound
	err := errors.New("")
	res := GetErrorMessageFromHttpResponse(err, &statusCode)
	assert.Equal(t, consts.ErrorOperationNotSupported, res)
}

func TestExtractErrorMessageFromResponseOperationNotSupportedInternalServer(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	statusCode := http.StatusInternalServerError
	err := consts.ErrorActionNotSupportedOnPrismGateway
	res := GetErrorMessageFromHttpResponse(err, &statusCode)
	assert.Equal(t, consts.ErrorOperationNotSupported, res)
}

func TestExtractErrorMessageFromResponseInternalServerError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	statusCode := http.StatusInternalServerError
	err := errors.New("")
	res := GetErrorMessageFromHttpResponse(err, &statusCode)
	assert.Equal(t, consts.ErrorInternalServerError, res)
}

func TestExtractErrorMessageFromResponseServiceUnavailable(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	statusCode := http.StatusServiceUnavailable
	err := errors.New("")
	res := GetErrorMessageFromHttpResponse(err, &statusCode)
	assert.Equal(t, consts.ErrorRemoteClusterNotAvailable, res)
}

func TestExtractErrorMessageFromResponseDefaultResponse(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	statusCode := 600
	err := errors.New("")
	res := GetErrorMessageFromHttpResponse(err, &statusCode)
	assert.Equal(t, consts.ErrorInternalServerError, res)
}

func TestGetErrorMessageFromJsonResponseNilMessage(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	err := errors.New("")
	res := GetErrorMessageFromJsonResponse(err)
	assert.Equal(t, err, res)
}

func TestGetErrorMessageFromJsonResponse(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	err := errors.New("error")
	res := GetErrorMessageFromJsonResponse(err)
	assert.Equal(t, err, res)
}

func TestGetErrorMessageFromJsonResponse_WithMessage(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	type result struct {
		Message string `json:"message"`
	}
	expectedError := result{
		Message: "abc",
	}
	errBytes, _ := json.Marshal(expectedError)
	res := GetErrorMessageFromJsonResponse(errors.New(string(errBytes)))
	assert.Equal(t, "abc", res.Error())
}

func TestGetErrorFromHttpResponse_ResponseCodeNil(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	err := errors.New("")
	res := GetErrorFromStatusCode(nil, err, consts.RegisterOpName)
	assert.Equal(t, guru_error.GetInternalError(consts.RegisterOpName), res)
}

func TestGetErrorFromHttpResponse_Unreachable(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	err := fmt.Errorf("%w", common_consts.ErrorClientTimeout)
	res := GetErrorFromStatusCode(nil, err, consts.RegisterOpName)
	assert.Equal(t, guru_error.GetGatewayTimeoutError(consts.RegisterOpName), res)
}

func TestGetErrorFromHttpResponse_Unauthorized(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	err := errors.New("")
	status := http.StatusUnauthorized
	res := GetErrorFromStatusCode(&status, err, consts.RegisterOpName)
	assert.Equal(t, guru_error.GetUnauthorizedRequestError(consts.RegisterOpName), res)
}

func TestGetErrorFromHttpResponse_Forbidden(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	err := errors.New("")
	status := http.StatusForbidden
	res := GetErrorFromStatusCode(&status, err, consts.RegisterOpName)
	assert.Equal(t, guru_error.GetForbiddenRequestError(consts.RegisterOpName), res)
}

func TestGetErrorFromHttpResponse_ServiceUnavailable(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	err := errors.New("")
	status := http.StatusServiceUnavailable
	res := GetErrorFromStatusCode(&status, err, consts.RegisterOpName)
	assert.Equal(t, guru_error.GetServiceUnavailableError(consts.RegisterOpName), res)
}

func TestGetErrorFromHttpResponse_OperationNotSupported(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	err := errors.New("")
	status := http.StatusNotFound
	res := GetErrorFromStatusCode(&status, err, consts.RegisterOpName)
	assert.Equal(t, guru_error.GetOperationNotSupportedOnRemote(consts.RegisterOpName), res)
}

func TestGetErrorFromHttpResponse_InternalError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	err := errors.New("")
	status := http.StatusInternalServerError
	res := GetErrorFromStatusCode(&status, err, consts.RegisterOpName)
	assert.Equal(t, guru_error.GetInternalError(consts.RegisterOpName), res)
}

func TestGetErrorFromHttpResponse_PrismGatewayNotFound(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	err := consts.ErrorActionNotSupportedOnPrismGateway
	status := http.StatusInternalServerError
	res := GetErrorFromStatusCode(&status, err, consts.RegisterOpName)
	assert.Equal(t, guru_error.GetOperationNotSupportedOnRemote(consts.RegisterOpName), res)
}

func TestGetErrorFromHttpResponse_Default(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	err := consts.ErrorActionNotSupportedOnPrismGateway
	status := 600
	res := GetErrorFromStatusCode(&status, err, consts.RegisterOpName)
	assert.Equal(t, guru_error.GetInternalError(consts.RegisterOpName), res)
}

func TestCreateAddRootCertificateResponse(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	var taskUuid = "e98feb6e-3559-5c93-a897-3635f7d0b3dd"
	taskExtId := taskutils.GetTaskReferenceExtIdFromTaskUuid(taskUuid)
	var url = CreateTaskUrl(taskExtId)

	res := CreateAddRootCertResponse(taskUuid)
	expectedResp := &management.AddRootCertificateRet{
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
	assert.Equal(t, expectedResp, res)
}

func TestCreateConfigureConnectionTaskResponse(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	CreateConfigureConnectionTaskResponse("")
}

func TestCreateRemoveRootCertificateResponse(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	CreateRemoveRootCertificateResponse("")
}

func TestCreateUnconfigureConnectionTaskResponse(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	CreateUnconfigureConnectionTaskResponse("")
}

func TestCreateUnregisterTaskResponse(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	CreateUnregisterTaskResponse("", false, false, consts.RelSelf)
}

func TestCreateRegisterErrorResponse(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	CreateRegisterErrorResponse(nil)
}

func TestCreateUnregisterErrorResponse(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	CreateUnregisterErrorResponse(nil)
}

func TestCreateCertExchangeErrorResponse(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	CreateAddRootCertificateErrorResponse(nil)
}

func TestCreateUnconfigureConnectionErrorResponse(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	CreateUnconfigureConnectionErrorResponse(nil)
}

func TestCreateRemoveRootCertErrorResponse(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	CreateRemoveRootCertErrorResponse(nil)
}

func TestCreateConfigureConnectionErrorResponse(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	CreateConfigureConnectionErrorResponse(nil)
}

func TestCreateGetDomainManagerErrorResponse(t *testing.T) {
	appMsg := []*ntnxApiGuruError.AppMessage{
		{
			Message: proto.String("error"),
		},
	}
	expectedResp := &config.GetDomainManagerByIdRet{
		Content: &config.GetDomainManagerApiResponse{
			Data: &config.GetDomainManagerApiResponse_ErrorResponseData{
				ErrorResponseData: getConfigErrorResponseData(appMsg),
			},
			Metadata: CreateResponseMetadata(true, false, "", ""),
		},
	}
	resp := CreateGetDomainManagerErrorResponse(appMsg)
	assert.Equal(t, expectedResp, resp)
}

func TestCreateListDomainManagerErrorResponse(t *testing.T) {
	appMsg := []*ntnxApiGuruError.AppMessage{
		{
			Message: proto.String("error"),
		},
	}
	expectedResp := &config.ListDomainManagersRet{
		Content: &config.ListDomainManagerApiResponse{
			Data: &config.ListDomainManagerApiResponse_ErrorResponseData{
				ErrorResponseData: getConfigErrorResponseData(appMsg),
			},
			Metadata: CreateResponseMetadata(true, false, "", ""),
		},
	}
	resp := CreateListDomainManagerErrorResponse(appMsg)
	assert.Equal(t, expectedResp, resp)
}

func TestCreateGetDomainManagerResponse(t *testing.T) {
	domainManager := InitialiseDomainManagerEntity()
	domainManager.Base.ExtId = proto.String("extId")
	url := "https://:9440/api/prism/v4.0.b1/config/domain-managers/extId"
	expectedResp := &config.GetDomainManagerByIdRet{
		Content: &config.GetDomainManagerApiResponse{
			Data: &config.GetDomainManagerApiResponse_DomainManagerData{
				DomainManagerData: &config.DomainManagerWrapper{
					Value: domainManager,
				},
			},
			Metadata: CreateResponseMetadata(false, false, url, consts.RelSelf),
		},
	}
	resp := CreateGetDomainManagerResponse(domainManager)
	assert.Equal(t, expectedResp, resp)
}

func TestCreateListDomainManagerResponse(t *testing.T) {
	domainManager := InitialiseDomainManagerEntity()
	domainManager.Base.ExtId = proto.String("extId")
	domainManagerList := []*config.DomainManager{domainManager}
	expectedResp := &config.ListDomainManagersRet{
		Content: &config.ListDomainManagerApiResponse{
			Data: &config.ListDomainManagerApiResponse_DomainManagerArrayData{
				DomainManagerArrayData: &config.DomainManagerArrayWrapper{
					Value: domainManagerList,
				},
			},
			Metadata: CreateListDomainManagerResponseMetadata(false, false, domainManagerList),
		},
	}
	resp := CreateListDomainManagerResponse(domainManagerList)
	assert.Equal(t, expectedResp, resp)
}

func TestGetErrorMessageFromHttpResponse_StatusNilErr(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	err := GetErrorMessageFromHttpResponse(nil, nil)

	assert.Equal(t, consts.ErrorInternalServerError, err)
}


func TestTruncateListDomainManagerResponse(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	listRet := CreateListDomainManagerResponse([]*config.DomainManager{
		{
			Base: &response.ExternalizableAbstractModel{
				ExtId: proto.String("abc"),
			},
			NodeExtIds: &config.StringArrayWrapper{
				Value: []string{"a", "b", "c"},
			},
		},
	})
	expectedListRet := listRet
	err := TruncateListDomainManagerResponse(listRet)
	assert.Equal(t, expectedListRet, listRet)
	assert.Nil(t, err)
}

func TestCreateNodeExtIdsUrlsSucces(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	nodeExtIds := []string{"extId1", "extId2"}
	domainManager := InitialiseDomainManagerEntity()
	domainManager.NodeExtIds = &config.StringArrayWrapper{
		Value: nodeExtIds,
	}
	urls := CreateNodeExtIdsUrls(domainManager)

	expectedUrls := []string{
		"https://:9440/api/vmm/v4.0.b1/ahv/config/vms/extId1",
		"https://:9440/api/vmm/v4.0.b1/ahv/config/vms/extId2",
	}
	assert.Equal(t, expectedUrls, urls)
}

func TestCreateContainerExtIdsUrls(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	containerExtIds := []string{"extId1", "extId2"}
	domainManager := InitialiseDomainManagerEntity()
	domainManager.Config.ResourceConfig.ContainerExtIds = &config.StringArrayWrapper{
		Value: containerExtIds,
	}
	urls := CreateContainerExtIdsUrls(domainManager)

	expectedUrls := []string{
		"https://:9440/api/clustermgmt/v4.0.b1/config/storage-containers/extId1",
		"https://:9440/api/clustermgmt/v4.0.b1/config/storage-containers/extId2",
	}
	assert.Equal(t, expectedUrls, urls)
}

func TestCreateGetDomainManagerResponseMetadata(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	var hasError = false
	var isPaginated = false
	containerExtIds := []string{"containerExtId1", "containerExtId2"}
	nodeExtIds := []string{"extId1", "extId2"}

	domainManager := InitialiseDomainManagerEntity()
	domainManager.Base.ExtId = proto.String("dmExtId")
	domainManager.Config.ResourceConfig.ContainerExtIds = &config.StringArrayWrapper{
		Value: containerExtIds,
	}
	domainManager.NodeExtIds = &config.StringArrayWrapper{
		Value: nodeExtIds,
	}
	result := CreateGetDomainManagerResponseMetadata(hasError, isPaginated, domainManager)

	expectedResult := &response.ApiResponseMetadata{
		Flags: CreateMetadataFlags(hasError, isPaginated),
		Links: &response.ApiLinkArrayWrapper{
			Value: []*response.ApiLink{
				{
					Href: proto.String("https://:9440/api/prism/v4.0.b1/config/domain-managers/dmExtId"),
					Rel:  proto.String(consts.RelSelf),
				},
				{
					Href: proto.String("https://:9440/api/vmm/v4.0.b1/ahv/config/vms/extId1"),
					Rel:  proto.String(consts.RelNodeExtId),
				},
				{
					Href: proto.String("https://:9440/api/vmm/v4.0.b1/ahv/config/vms/extId2"),
					Rel:  proto.String(consts.RelNodeExtId),
				},
				{
					Href: proto.String("https://:9440/api/clustermgmt/v4.0.b1/config/storage-containers/containerExtId1"),
					Rel:  proto.String(consts.RelContainerExtId),
				},
				{
					Href: proto.String("https://:9440/api/clustermgmt/v4.0.b1/config/storage-containers/containerExtId2"),
					Rel:  proto.String(consts.RelContainerExtId),
				},
			},
		},
	}
	assert.Equal(t, expectedResult, result)

}
