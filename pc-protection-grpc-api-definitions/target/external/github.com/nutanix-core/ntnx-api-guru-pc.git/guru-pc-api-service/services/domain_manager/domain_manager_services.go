/*
* Copyright (c) 2023 Nutanix Inc. All rights reserved.
*
* Author: harsh.seta@nutanix.com
*
* This file contains the Prism Central RPCs
 */

package domain_manager

import (
	"context"

	dto "ntnx-api-guru-pc/generated-code/target/dto/models/prism/v4/management"
	ntnxApiGuruError "ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/error"
	"ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/management"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/grpc_error"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/local"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/queue"
	"ntnx-api-guru-pc/guru-pc-api-service/services/register/configure_connection"
	"ntnx-api-guru-pc/guru-pc-api-service/services/register/register_pc"
	pc_utils "ntnx-api-guru-pc/guru-pc-api-service/utils"
	"time"

	"github.com/nutanix-core/go-cache/util-go/uuid4"
	common_consts "github.com/nutanix-core/ntnx-api-guru/services/constants"
	log "github.com/sirupsen/logrus"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

type DomainManagerServiceServerImpl struct {
	management.UnimplementedDomainManagerServiceServer
	jobQueue queue.Queue
}

func NewDomainManagerServiceServerImpl(
	jobQueue queue.Queue) *DomainManagerServiceServerImpl {
	return &DomainManagerServiceServerImpl{
		jobQueue: jobQueue,
	}
}

func (domainManagerService *DomainManagerServiceServerImpl) Register(
	ctx context.Context, registerArg *management.RegisterArg) (*management.RegisterRet, error) {

	// Validate the Register Request
	requestContext, requestId, batchTaskId, taskId, appMessageList, guruError :=
		pc_utils.ValidateIncomingRequest(ctx, registerArg.GetExtId(), consts.RegisterOpName)
	if guruError != nil {
		log.Errorf("Error occured while validating the incoming request %+v", guruError)
		return pc_utils.CreateRegisterErrorResponse(appMessageList), guruError
	}

	if taskId != "" {
		return pc_utils.CreateRegisterTaskResponse(taskId, false, false, consts.RelSelf), nil
	}

	switch {
	case registerArg.GetBody().GetAOSRemoteClusterSpecRemoteCluster() != nil ||
		registerArg.GetBody().GetClusterReferenceRemoteCluster() != nil:
		log.Infof("%s Incoming request for PC-PE registration",
			common_consts.REGISTRATION_LOGGER_PREFIX)

		requestDetails := &models.RegisterAosRequestDetails{}
		contextId := requestId
		requestDetails.ContextID = &contextId
		requestDetails.ResourceExtId = registerArg.ExtId

		registrationCommon := local.GetInternalInterfaces().RegistrationCommonUtilIfc()
		// Validate incoming Register request.
		appMessageList, guruError := registrationCommon.ValidateRegistrationRequest(registerArg, requestDetails)
		if guruError != nil {
			log.Errorf("[%s] %s Failed to register with error : %s", contextId,
				common_consts.REGISTRATION_LOGGER_PREFIX,
				guruError.Error())
			return pc_utils.CreateRegisterErrorResponse(appMessageList), guruError
		}

		// Create Registration task
		registrationTaskUuids := &models.RegistrationTaskUuidsDTO{}
		registrationParentTask, appMessageList, guruError :=
			registrationCommon.CreateParentRegistrationTask(
				contextId, batchTaskId, requestContext, registerArg.GetExtId())
		if guruError != nil {
			return pc_utils.CreateRegisterErrorResponse(appMessageList), guruError
		}
		registrationTaskUuids.SetRegistrationParentTaskUuid(registrationParentTask)
		log.Infof("Registration task uuids : %v", registrationTaskUuids)

		// Add task details to context.
		ctx = context.WithValue(ctx, consts.RequestDetails, requestDetails)
		ctx = context.WithValue(ctx, consts.TaskUuids, registrationTaskUuids)

		// Execute registration request for remote AOS cluster.
		registerAosClusterJob := registrationCommon.
			PrepareRegisterAosJob(requestDetails, registrationParentTask, requestContext)
		domainManagerService.jobQueue.Enqueue(registerAosClusterJob)
		response := registrationCommon.CreateRegisterApiResponseDTO(registerAosClusterJob)
		log.Infof("[%s] %s Returning Registration response  : %v",
			contextId, common_consts.REGISTRATION_LOGGER_PREFIX, response)
		return response, nil
	case registerArg.GetBody().GetDomainManagerRemoteClusterSpecRemoteCluster() != nil:
		log.Infof("%s Received PC-PC Registration request: %s", consts.RegisterPCLoggerPrefix, requestId)

		// Create PC Registration task
		task, appMessageList, guruError := register_pc.CreateRegisterPcTask(requestId, batchTaskId, requestContext, registerArg.GetExtId())
		if guruError != nil {
			log.Errorf("Error creating task for PC-PC Registration: %s", guruError.Error())
			return pc_utils.CreateRegisterErrorResponse(appMessageList), guruError
		}
		log.Infof("Created task for PC-PC Registration: %s", *task.ExtId)

		// Create and Execute the PC Registration Job
		job := register_pc.CreateNewRegisterPcGuruJob(task, registerArg, requestId)
		domainManagerService.jobQueue.Enqueue(job)
		response := pc_utils.CreateRegisterTaskResponse(job.JobMetadata.TaskId, false, false, consts.RelSelf)
		return response, nil
	}

	// return default 400 response.
	guruErr := guru_error.GetInvalidRemoteEntityError(consts.RegisterOpName)
	err := grpc_error.GetGrpcStatusUtilImpl().BuildGrpcError(guruErr)
	return pc_utils.CreateRegisterErrorResponse([]*ntnxApiGuruError.AppMessage{guruErr.ConvertToAppMessagePb()}), err
}

// Internal API to Add root certificate
func (domainManagerService *DomainManagerServiceServerImpl) AddRootCertificate(ctx context.Context, args *management.AddRootCertificateArg) (*management.AddRootCertificateRet, error) {

	log.Infof("Incoming request for Add Root Certificate for cluster-id: %s", args.GetExtId())
	// Validate the Register Request
	requestContext, requestId, _, taskId, appMessageList, guruError :=
		pc_utils.ValidateIncomingRequest(ctx, args.GetExtId(), consts.AddCertificateOpName)
	if guruError != nil {
		return pc_utils.CreateAddRootCertificateErrorResponse(appMessageList), guruError
	}
	// Verify if request is for a valid domain_manager resource
	guruErr := pc_utils.VerifyIfValidRequestForCurrentResource(args.GetExtId(), consts.AddCertificateOpName)
	if guruErr != nil {
		log.Errorf("[%s] Error occured while validating the incoming remove"+
			"add root certificate", requestId)
		err := grpc_error.GetGrpcStatusUtilImpl().BuildGrpcError(guruErr)
		return pc_utils.CreateAddRootCertificateErrorResponse([]*ntnxApiGuruError.AppMessage{guruErr.ConvertToAppMessagePb()}), err
	}
	if taskId != "" {
		return pc_utils.CreateAddRootCertResponse(taskId), nil
	}
	// Create a task for add root certificate
	addRootCertificateTask, err := pc_utils.CreateJobTask(consts.ADD_ROOT_CERTIFICATE,
		requestId, requestContext, args.GetExtId(), true, nil, false)
	if err != nil {
		guruErr := guru_error.GetServiceUnavailableError(consts.AddCertificateOpName)
		err := grpc_error.GetGrpcStatusUtilImpl().BuildGrpcError(guruErr)
		return pc_utils.CreateAddRootCertificateErrorResponse([]*ntnxApiGuruError.AppMessage{guruErr.ConvertToAppMessagePb()}), err
	}

	job := register_pc.CreateAddRootCertJob(args, addRootCertificateTask, requestId)
	domainManagerService.jobQueue.Enqueue(job)

	return pc_utils.CreateAddRootCertResponse(uuid4.String(addRootCertificateTask.GetUuid())), nil
}

// Internal API to remove root certificate
func (domainManagerService *DomainManagerServiceServerImpl) RemoveRootCertificate(ctx context.Context,
	args *management.RemoveRootCertificateArg) (*management.RemoveRootCertificateRet, error) {
	log.Infof("Incoming request for Remove Root Certificate for remote cluster-id: %s", args.Body.GetClusterExtId())

	// Validate the incoming request
	requestContext, requestId, _, taskId, appMessageList, guruError := pc_utils.ValidateIncomingRequest(ctx, args.GetExtId(), consts.RemoveCertificateOpName)
	if guruError != nil {
		log.Infof("Error occured while validating the incoming request %+v", guruError.Error())
		return pc_utils.CreateRemoveRootCertErrorResponse(appMessageList), guruError
	}
	if taskId != "" {
		return pc_utils.CreateRemoveRootCertificateResponse(taskId), nil
	}
	removeRootCertificateTask, err := pc_utils.CreateJobTask(consts.REMOVE_ROOT_CERTIFICATE,
		requestId, requestContext, args.GetExtId(), true, nil, false)
	if err != nil {
		guruErr := guru_error.GetServiceUnavailableError(consts.RemoveCertificateOpName)
		err := grpc_error.GetGrpcStatusUtilImpl().BuildGrpcError(guruErr)
		return pc_utils.CreateRemoveRootCertErrorResponse([]*ntnxApiGuruError.AppMessage{guruErr.ConvertToAppMessagePb()}), err
	}

	var job = models.Job{
		JobMetadata: &models.JobMetadata{
			OperationStartTime: time.Now().Format(consts.DateTimeFormat),
			ParentTaskId:       removeRootCertificateTask.GetUuid(),
			TaskId:             uuid4.String(removeRootCertificateTask.GetUuid()),
			ContextId:          requestId,
			Name:               consts.REMOVE_ROOT_CERTIFICATE,
			RemoteClusterId:    args.GetBody().GetClusterExtId(),
			SelfClusterId:      args.GetExtId(),
		},
		ParentTask: removeRootCertificateTask,
		Name:       consts.REMOVE_ROOT_CERTIFICATE,
	}
	domainManagerService.jobQueue.Enqueue(&job)
	response := pc_utils.CreateRemoveRootCertificateResponse(uuid4.String(removeRootCertificateTask.GetUuid()))
	return response, nil
}

// Internal API to configure connection
func (domainManagerService *DomainManagerServiceServerImpl) ConfigureConnection(ctx context.Context, args *management.ConfigureConnectionArg) (*management.ConfigureConnectionRet, error) {
	log.Debugf("Incoming request for Configure Connection %+v", args)
	remoteClusterUUID := args.GetBody().GetRemoteCluster().GetBase().GetExtId()
	// Validate the incoming request
	requestContext, requestId, _, _, appMessageList, guruError := pc_utils.ValidateIncomingRequest(ctx, args.GetExtId(), consts.ConfigureConnectionOpName)
	if guruError != nil {
		log.Errorf("Error occured while validating the incoming request %+v", guruError.Error())
		return pc_utils.CreateConfigureConnectionErrorResponse(appMessageList), guruError
	}

	// Create a configure-connection ergon task with metadata
	configureConnectionTask, err := pc_utils.CreateJobTask(consts.CONFIGURE_CONNECTION,
		requestId, requestContext, remoteClusterUUID, true, nil, true)

	if err != nil {
		log.Errorf("%s Failed to create configure connection task with error : %s", requestId, err.Error())
		guruErr := guru_error.GetServiceUnavailableError(consts.ConfigureConnectionOpName)
		err := grpc_error.GetGrpcStatusUtilImpl().BuildGrpcError(guruErr)
		return pc_utils.CreateConfigureConnectionErrorResponse([]*ntnxApiGuruError.AppMessage{guruErr.ConvertToAppMessagePb()}), err
	}

	// Create a job and enqueue it to the task queue
	configureConnectionJob := configure_connection.CreateConfigureConnectionJob(configureConnectionTask, requestId, args)
	log.Debugf("[%s] Configure connection job : %+v", requestId, configureConnectionJob)
	domainManagerService.jobQueue.Enqueue(configureConnectionJob)

	// Create a response dto
	response := pc_utils.CreateConfigureConnectionTaskResponse(uuid4.String(configureConnectionTask.GetUuid()))
	return response, nil
}

// Internal API to unconfigure connection
func (domainManagerService *DomainManagerServiceServerImpl) UnconfigureConnection(ctx context.Context, args *management.UnconfigureConnectionArg) (*management.UnconfigureConnectionRet, error) {

	// Validate the incoming request
	requestContext, requestId, _, _, appMessageList, guruError := pc_utils.ValidateIncomingRequest(ctx, args.GetExtId(), consts.UnconfigureConnectionOpName)
	if guruError != nil {
		log.Errorf("Error occured while validating the incoming request %+v", guruError.Error())
		return pc_utils.CreateUnconfigureConnectionErrorResponse(appMessageList), guruError
	}

	// Create a unconfigure-connection ergon task with metadata
	unconfigureConnectionTask, err := pc_utils.CreateJobTask(consts.UNCONFIGURE_CONNECTION,
		requestId, requestContext, args.GetExtId(), true, nil, true)
	if err != nil {
		log.Errorf("[%s] %s Failed to create unconfigure connection task with error : %s", requestId, consts.RegisterPCLoggerPrefix, err)
		guruErr := guru_error.GetServiceUnavailableError(consts.UnconfigureConnectionOpName)
		err := grpc_error.GetGrpcStatusUtilImpl().BuildGrpcError(guruErr)
		return pc_utils.CreateUnconfigureConnectionErrorResponse([]*ntnxApiGuruError.AppMessage{guruErr.ConvertToAppMessagePb()}), err
	}

	// Create a unconfigure connection job and enqueue it to the task queue
	unconfigureConnectionJob := configure_connection.CreateUnconfigureConnectionJob(unconfigureConnectionTask, requestId, args)
	log.Debugf("[%s] Unconfigure Connection Job : %+v", requestId, unconfigureConnectionJob)
	domainManagerService.jobQueue.Enqueue(unconfigureConnectionJob)

	// Create a response dto
	response := pc_utils.CreateUnconfigureConnectionTaskResponse(uuid4.String(unconfigureConnectionTask.GetUuid()))
	return response, nil
}

func (domainManagerService *DomainManagerServiceServerImpl) Unregister(ctx context.Context, args *management.UnregisterArg) (*management.UnregisterRet, error) {
	remoteClusterUUID := args.GetBody().GetExtId()

	// Validate the Register Request
	requestContext, requestId, _, taskId, appMessageList, guruError :=
		pc_utils.ValidateIncomingRequest(ctx, args.GetExtId(), consts.UnregisterPCOpName)
	if guruError != nil {
		log.Errorf("Error occured while validating the incoming request %+v", guruError.Error())
		return pc_utils.CreateUnregisterErrorResponse(appMessageList), guruError
	}

	if taskId != "" {
		return pc_utils.CreateUnregisterTaskResponse(taskId, false, false, consts.RelSelf), nil
	}

	//  Only accept unregister request if the remote cluster is a domain manager (PC)
	if !pc_utils.IsDomainManager(remoteClusterUUID) {
		log.Errorf("[%s] Invalid cluster %s for unregister operation", remoteClusterUUID, requestId)
		return nil, status.Errorf(codes.InvalidArgument, "invalid cluster id, not found")
	}

	unregistrationTask, err := pc_utils.CreateJobTask(consts.UNREGISTER_PC,
		requestId, requestContext, args.GetExtId(), false, nil, true)
	if err != nil {
		guruErr := guru_error.GetServiceUnavailableError(consts.UnregisterPCOpName)
		err := grpc_error.GetGrpcStatusUtilImpl().BuildGrpcError(guruErr)
		return pc_utils.CreateUnregisterErrorResponse([]*ntnxApiGuruError.AppMessage{guruErr.ConvertToAppMessagePb()}), err
	}
	unregistrationTaskUUID := unregistrationTask.GetUuid()
	log.Infof("[%s] Created unregister task with UUID %s ", requestId, uuid4.String(unregistrationTaskUUID))

	jobMetadata := &models.JobMetadata{
		OperationStartTime: time.Now().Format(consts.DateTimeFormat),
		ParentTaskId:       unregistrationTaskUUID,
		TaskId:             uuid4.String(unregistrationTaskUUID),
		ContextId:          requestId,
		Name:               consts.UNREGISTER_PC,
		RemoteClusterId:    remoteClusterUUID,
		RemoteClusterType:  dto.CLUSTERTYPE_DOMAIN_MANAGER,
		SelfClusterId:      args.GetExtId(),
	}

	unregisterJob := models.Job{
		JobMetadata: jobMetadata,
		Name:        consts.UNREGISTER_PC,
		ParentTask:  unregistrationTask,
	}
	domainManagerService.jobQueue.Enqueue(&unregisterJob)

	return pc_utils.CreateUnregisterTaskResponse(uuid4.String(unregistrationTaskUUID), false, false, consts.RelSelf), nil
}
