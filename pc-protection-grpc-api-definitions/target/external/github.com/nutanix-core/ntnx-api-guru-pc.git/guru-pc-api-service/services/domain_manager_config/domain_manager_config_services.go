package domain_manager_config

import (
	"context"
	"fmt"
	domainManagerConfig "ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/config"
	ntnxApiGuruError "ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/error"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/grpc_error"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/services/get_domain_manager"
	"ntnx-api-guru-pc/guru-pc-api-service/utils"
	pc_utils "ntnx-api-guru-pc/guru-pc-api-service/utils"

	log "github.com/sirupsen/logrus"
)

type DomainManagerConfigServiceServerImpl struct {
	domainManagerConfig.UnimplementedDomainManagerServiceServer
}

func NewDomainManagerConfigServiceServerImpl() *DomainManagerConfigServiceServerImpl {
	return &DomainManagerConfigServiceServerImpl{}
}

func (domainManagerService *DomainManagerConfigServiceServerImpl) GetDomainManagerById(
	ctx context.Context, arg *domainManagerConfig.GetDomainManagerByIdArg) (
	*domainManagerConfig.GetDomainManagerByIdRet, error) {

	log.Infof("Incoming request for domain manager GET for id: %s", arg.GetExtId())
	// Fetch the resource extId from request arg.
	domainManagerExtId := arg.GetExtId()
	// Fetch zeus config
	zeusConfig, err := utils.FetchZeusConfig()
	if err != nil {
		log.Errorf("%s failed to fetch zeus config zk node : %s",
			consts.GetDomainManagerLoggerPrefix, err)
		err = fmt.Errorf("failed to fetch domain manager config details : %w", err)
		guruErr := guru_error.GetInternalError(consts.GetDomainManagerOpName)
		return pc_utils.CreateGetDomainManagerErrorResponse(
				[]*ntnxApiGuruError.AppMessage{guruErr.ConvertToAppMessagePb()}),
			grpc_error.GetGrpcStatusUtilImpl().BuildGrpcError(guruErr)
	}

	// Verify if request is for a valid domain_manager resource
	if domainManagerExtId != zeusConfig.GetClusterUuid() {
		log.Errorf("%s invalid resource specified: %s",
			consts.GetDomainManagerLoggerPrefix, domainManagerExtId)
		guruErr := guru_error.GetResourceNotFoundError(consts.GetDomainManagerOpName, domainManagerExtId)
		return pc_utils.CreateGetDomainManagerErrorResponse(
				[]*ntnxApiGuruError.AppMessage{guruErr.ConvertToAppMessagePb()}),
			grpc_error.GetGrpcStatusUtilImpl().BuildGrpcError(guruErr)
	}

	response, errMsg, err := get_domain_manager.GetDomainManager(zeusConfig)
	if err != nil {
		log.Errorf("%s failed to get domain manager : %s",
			consts.GetDomainManagerLoggerPrefix, err)
		return pc_utils.CreateGetDomainManagerErrorResponse(errMsg), err
	}

	// Populate ETag in the response headers
	pc_utils.PopulateETagInHeaders(ctx, response.Etag)

	responseRet := pc_utils.CreateGetDomainManagerResponse(response.Entity)
	log.Infof("%s Returned response for domain manager GET for id: %s",
		consts.GetDomainManagerLoggerPrefix, arg.GetExtId())
	log.Debugf("%s Returning the GET response : %+v",
		consts.GetDomainManagerLoggerPrefix, responseRet)
	return responseRet, nil
}

func (domainManagerService *DomainManagerConfigServiceServerImpl) ListDomainManagers(
	ctx context.Context, args *domainManagerConfig.ListDomainManagersArg) (
	*domainManagerConfig.ListDomainManagersRet, error) {

	log.Infof("Incoming request for domain manager LIST API")
	// Fetch zeus config
	zeusConfig, err := utils.FetchZeusConfig()
	if err != nil {
		log.Errorf("failed to fetch zeus config zk node : %s", err)
		err = fmt.Errorf("failed to fetch domain manager config details : %w", err)
		guruErr := guru_error.GetInternalError(consts.ListDomainManagerOpName)
		return pc_utils.CreateListDomainManagerErrorResponse(
				[]*ntnxApiGuruError.AppMessage{guruErr.ConvertToAppMessagePb()}),
			grpc_error.GetGrpcStatusUtilImpl().BuildGrpcError(guruErr)
	}

	response, errMsg, err := get_domain_manager.ListDomainManagers(zeusConfig)
	if err != nil {
		log.Errorf("failed to get list of domain managers : %s", err)
		return pc_utils.CreateListDomainManagerErrorResponse(errMsg), err
	}

	responseRet := pc_utils.CreateListDomainManagerResponse(response)

	// truncates the response 
	err = pc_utils.TruncateListDomainManagerResponse(responseRet)
	if err != nil {
		log.Warnf("Failed to truncate LIST response with error %s", err)
	}

	log.Debugf("Returning the LIST response : %+v", responseRet)
	return responseRet, nil
}

// New function to handle createDomainManager POST requests
func (domainManagerService *DomainManagerConfigServiceServerImpl) CreateDomainManager(
	ctx context.Context, args *domainManagerConfig.CreateDomainManagerArg) (
	*domainManagerConfig.CreateDomainManagerRet, error) {

	guruErr := guru_error.GetUnsupportedOperationError(consts.PCDeploymentOpName)

	// Return 501 status code with error message
	return nil, grpc_error.GetGrpcStatusUtilImpl().BuildGrpcError(guruErr)
}
