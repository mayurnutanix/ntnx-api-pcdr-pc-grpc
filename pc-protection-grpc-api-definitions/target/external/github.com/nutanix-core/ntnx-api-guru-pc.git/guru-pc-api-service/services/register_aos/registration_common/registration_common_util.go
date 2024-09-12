/*
* Copyright (c) 2024 Nutanix Inc. All rights reserved.
*
* Author: kshitij.kumar@nutanix.com
*
* This file contains the common methods required for AOS cluster registration sub tasks
 */

package registration_common

import (
	"encoding/json"
	ntnxApiGuruError "ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/error"
	"ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/management"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/grpc_error"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/external"
	"ntnx-api-guru-pc/guru-pc-api-service/models"

	pc_utils "ntnx-api-guru-pc/guru-pc-api-service/utils"
	"time"

	"github.com/golang/protobuf/proto"
	"github.com/nutanix-core/go-cache/ergon"
	"github.com/nutanix-core/go-cache/util-slbufs/util/sl_bufs/net"

	"github.com/nutanix-core/go-cache/util-go/uuid4"
	common_consts "github.com/nutanix-core/ntnx-api-guru/services/constants"
	log "github.com/sirupsen/logrus"
)

type RegistrationCommonUtilIfc interface {
	ValidateRegistrationRequest(*management.RegisterArg, *models.RegisterAosRequestDetails) ([]*ntnxApiGuruError.AppMessage, error)
	CreateParentRegistrationTask(string, string, *net.RpcRequestContext, string) ([]byte, []*ntnxApiGuruError.AppMessage, error)
	CreateChildTasks(*models.Job) error
	PrepareRegisterAosJob(*models.RegisterAosRequestDetails, []byte, *net.RpcRequestContext) *models.Job
	CreateRegistrationMarkerNode(*models.Job) error
	DeleteRegistrationMarkerNode(*models.Job)
	ProcessRegistrationErrorState(*models.Job, string, int)
	CreateRegisterApiResponseDTO(job *models.Job) *management.RegisterRet
}

// Registration Precheck Task constants
const (
	RegistrationPrecheckTaskName            = "Register AOS cluster precheck on pc"
	RegistrationPrecheckTaskCreationMessage = "Created Registration Precheck Task"
	RegistrationPrecheckTaskDisplayName     = "Registration Precheck on Domain manager"
)

// CSR Task constants
const (
	CSRTaskName            = "Certificate signing request"
	CSRTaskCreationMessage = "Created CSR Task"
	CSRTaskDisplayName     = "Certificate signing from Domain manager"
)

// Cluster Registration Task constants
const (
	ClusterRegistrationTaskName            = "AOS cluster registration"
	ClusterRegistrationTaskDisplayName     = "Register AOS cluster to Domain manager"
	ClusterRegistrationTaskCreationMessage = "Created AOS cluster registration task"
)

type RegistrationCommonUtil struct {
}

func New() (RegistrationCommonUtilIfc, error) {
	return &RegistrationCommonUtil{}, nil
}

func (registrationUtil *RegistrationCommonUtil) ValidateRegistrationRequest(
	requestArg *management.RegisterArg, registerAosRequestBody *models.RegisterAosRequestDetails,
) ([]*ntnxApiGuruError.AppMessage, error) {
	remoteClusterUuid := ""
	useTrust := false
	var err error

	// Check if incoming request pertains to hosting PE cluster. Adonis's schema validation will ensure
	// a valid string is populated since it's a required field.
	if requestArg.GetBody().GetClusterReferenceRemoteCluster().GetValue().GetExtId() != "" {
		remoteClusterUuid = requestArg.GetBody().
			GetClusterReferenceRemoteCluster().GetValue().GetExtId()
		useTrust = true
		registerAosRequestBody.ClusterUuid = &remoteClusterUuid
		log.Infof("[%s] %s Incoming AOS cluster registration request with cluster-uuid %s",
			*registerAosRequestBody.ContextID, common_consts.REGISTRATION_LOGGER_PREFIX,
			remoteClusterUuid)
	}
	registerAosRequestBody.UseTrust = &useTrust
	var remoteIpOrFqdn string
	if useTrust {
		remoteIpOrFqdn, err = pc_utils.FetchIpAddressFromTrustZkNode(remoteClusterUuid)
		if err != nil {
			log.Errorf("[%s] %s %s", *registerAosRequestBody.ContextID,
				consts.ErrorTrustZkNodeFailure.Error(), err)
			guruErr := guru_error.GetInvalidArgumentError(consts.RegisterAosOpName, remoteClusterUuid, consts.RemoteClusterIdArg)
			return []*ntnxApiGuruError.AppMessage{guruErr.ConvertToAppMessagePb()}, grpc_error.GetGrpcStatusUtilImpl().BuildGrpcError(guruErr)
		}
		registerAosRequestBody.ClusterUuid = &remoteClusterUuid
	} else {
		remoteIpOrFqdn = pc_utils.GetIpAddressOrFqdn(
			requestArg.GetBody().GetAOSRemoteClusterSpecRemoteCluster().GetValue().GetRemoteCluster().GetAddress())
		remoteAuth :=
			requestArg.GetBody().GetAOSRemoteClusterSpecRemoteCluster().
				GetValue().
				GetRemoteCluster().GetCredentials()
		log.Debugf("[%s] %s Body of the request is %+v",
			*registerAosRequestBody.ContextID, common_consts.REGISTRATION_LOGGER_PREFIX, remoteAuth)
		registerAosRequestBody.Username = proto.String(remoteAuth.GetAuthentication().GetUsername())
		registerAosRequestBody.Password = proto.String(remoteAuth.GetAuthentication().GetPassword())
	}
	registerAosRequestBody.IPAddressOrFQDN = &remoteIpOrFqdn
	log.Infof("[%s] %s Remote IP is : %s", *registerAosRequestBody.ContextID,
		common_consts.REGISTRATION_LOGGER_PREFIX, remoteIpOrFqdn)

	return nil, nil
}

func (registrationUtil *RegistrationCommonUtil) CreateParentRegistrationTask(
	contextId string, batchTaskId string, requestContext *net.RpcRequestContext,
	clusterExtId string) ([]byte, []*ntnxApiGuruError.AppMessage, error) {

	var batchTaskIdByte []byte
	if batchTaskId != "" {
		batchUuid, _ := uuid4.StringToUuid4(batchTaskId)
		batchTaskIdByte = batchUuid.RawBytes()
	}

	taskRequestContext := pc_utils.CreateTaskRequestContextFromGrpcContext(requestContext)
	taskEntityIdList := []*ergon.EntityId{}
	taskEntity, err := pc_utils.GetSelfDomainManagerEntity()
	if err != nil {
		log.Warnf("[%s] %s Failed while updating entities in task: %s", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX, err)
	} else {
		taskEntityIdList = append(taskEntityIdList, taskEntity)
	}

	taskCreateArg := ergon.TaskCreateArg{
		OperationType:                proto.String(consts.OperationTypeMap[consts.REGISTER_AOS]),
		Message:                      proto.String(pc_utils.GetTaskCreationMessage(consts.REGISTER_AOS)),
		Component:                    proto.String(consts.GuruTaskComponent),
		DisplayName:                  proto.String(consts.OperationNameMap[consts.REGISTER_AOS]),
		InternalTask:                 proto.Bool(false),
		ParentTaskUuid:               batchTaskIdByte,
		RequestContext:               &taskRequestContext,
		RequestId:                    proto.String(contextId),
		EntityList:                   taskEntityIdList,
		DisableAutoClusterListUpdate: proto.Bool(true),
	}
	// Create ergon task for register AOS request.
	ergonTaskRet, err := external.Interfaces().ErgonClient().TaskCreate(&taskCreateArg)
	if err != nil {
		log.Errorf("[%s] %s %s with error: %s", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX,
			consts.CreateErgonTaskErr, err)
		guruErr := guru_error.GetServiceUnavailableError(consts.RegisterAosOpName)
		return nil, []*ntnxApiGuruError.AppMessage{guruErr.ConvertToAppMessagePb()}, grpc_error.GetGrpcStatusUtilImpl().BuildGrpcError(guruErr)
	}

	registrationTaskUuid := ergonTaskRet.GetUuid()
	taskUuid := uuid4.ToUuid4(registrationTaskUuid).String()
	log.Infof("[%s] %s Created registration parent task with task uuid %s",
		contextId, common_consts.REGISTRATION_LOGGER_PREFIX, taskUuid)

	// Update the task to create an entry in the request_identifier table
	// for idempotency control, so that future requests with same request id
	// return the same task
	err = external.Interfaces().IdempotencyService().UpdateTask(contextId, taskUuid)
	if err != nil {
		log.Errorf("%s Failed to update the task in the request_identifer table for idempotency control with error - %s",
			common_consts.REGISTRATION_LOGGER_PREFIX, err)
	}

	return registrationTaskUuid, nil, nil
}

func (registrationUtil *RegistrationCommonUtil) CreateChildTasks(item *models.Job) error {

	// Fetch register_aos tasks and context id from task details
	contextId := item.JobMetadata.ContextId

	// If a recovered task then rollback because recovery cannot happen
	// as credentials are lost
	if item.JobMetadata.IsRecoveredTask {
		log.Errorf("[%s] %s Task recovered in create child task step "+
			"failing it because credentials lost before establishing trust",
			contextId, common_consts.REGISTRATION_LOGGER_PREFIX)
		return consts.ErrorRollbackRecoverTask
	}

	requestContext := item.JobMetadata.RequestContext
	if requestContext == nil {
		log.Errorf("[%s] Can not proceed with task %s as its context is lost",
			contextId, common_consts.REGISTRATION_LOGGER_PREFIX)
		return consts.ErrorGenericServer
	}

	taskRequestContext := pc_utils.CreateTaskRequestContextFromGrpcContext(requestContext)
	taskEntityIdList := []*ergon.EntityId{}
	taskEntityId, err := pc_utils.GetSelfDomainManagerEntity()
	if err != nil {
		log.Warnf("[%s] %s Failure to update domain manager entity in task: %s",
			contextId, common_consts.REGISTRATION_LOGGER_PREFIX, err)
	} else {
		taskEntityIdList = append(taskEntityIdList, taskEntityId)
	}
	precheckTaskCreateArg := ergon.TaskCreateArg{
		OperationType:                proto.String(RegistrationPrecheckTaskName),
		Message:                      proto.String(RegistrationPrecheckTaskCreationMessage),
		Component:                    proto.String(consts.GuruTaskComponent),
		DisplayName:                  proto.String(RegistrationPrecheckTaskDisplayName),
		InternalTask:                 proto.Bool(true),
		ParentTaskUuid:               item.JobMetadata.ParentTaskId,
		RequestContext:               &taskRequestContext,
		EntityList:                   taskEntityIdList,
		DisableAutoClusterListUpdate: proto.Bool(false),
		TotalWeight:                  proto.Uint64(consts.RegisterAOSPrecheckTaskWeight),
	}

	taskCreateRet, err := external.Interfaces().ErgonClient().TaskCreate(&precheckTaskCreateArg)
	if err != nil {
		log.Errorf("[%s] %s %s with error: %s", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX,
			consts.ErrorCreateRegistrationPrecheckTask.Error(), err)
		item.JobMetadata.ErrorStep = 0
		return err
	}

	precheckTaskUuidByte := taskCreateRet.GetUuid()
	log.Infof("[%s] %s Registration precheck task successfully created with "+
		"task uuid %s", contextId, common_consts.REGISTRATION_LOGGER_PREFIX,
		uuid4.ToUuid4(precheckTaskUuidByte).String())

	csrTaskCreateArg := ergon.TaskCreateArg{
		OperationType:                proto.String(CSRTaskName),
		Message:                      proto.String(CSRTaskCreationMessage),
		Component:                    proto.String(consts.GuruTaskComponent),
		DisplayName:                  proto.String(CSRTaskDisplayName),
		InternalTask:                 proto.Bool(true),
		ParentTaskUuid:               item.JobMetadata.ParentTaskId,
		RequestContext:               &taskRequestContext,
		EntityList:                   taskEntityIdList,
		DisableAutoClusterListUpdate: proto.Bool(true),
		TotalWeight:                  proto.Uint64(consts.RegisterAOSCsrTaskWeight),
	}
	csrTaskCreateRet, err := external.Interfaces().ErgonClient().TaskCreate(&csrTaskCreateArg)
	if err != nil {
		log.Errorf("[%s] %s %s with error: %s", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX,
			consts.ErrorCreateCsrTask.Error(), err)
		item.JobMetadata.ErrorStep = 0
		return err
	}

	csrTaskUuidByte := csrTaskCreateRet.GetUuid()
	log.Infof("[%s] %s CSR task successfully created with task uuid %s",
		contextId, common_consts.REGISTRATION_LOGGER_PREFIX,
		uuid4.ToUuid4(csrTaskUuidByte).String())

	clusterRegisterTaskCreateArg := ergon.TaskCreateArg{
		OperationType:                proto.String(ClusterRegistrationTaskName),
		Message:                      proto.String(ClusterRegistrationTaskCreationMessage),
		Component:                    proto.String(consts.GuruTaskComponent),
		DisplayName:                  proto.String(ClusterRegistrationTaskDisplayName),
		InternalTask:                 proto.Bool(true),
		ParentTaskUuid:               item.JobMetadata.ParentTaskId,
		RequestContext:               &taskRequestContext,
		EntityList:                   taskEntityIdList,
		DisableAutoClusterListUpdate: proto.Bool(true),
		TotalWeight:                  proto.Uint64(consts.RegisterAOSProxyTaskWeight),
	}
	clusterRegisterTaskCreateRet, err := external.Interfaces().ErgonClient().TaskCreate(&clusterRegisterTaskCreateArg)
	if err != nil {
		log.Errorf("[%s] %s %s with error: %s", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX,
			consts.ErrorCreateClusterRegistrationTask.Error(), err)
		item.JobMetadata.ErrorStep = 0
		return err
	}

	clusterRegistrationTaskUuidByte := clusterRegisterTaskCreateRet.GetUuid()
	log.Infof("[%s] %s Cluster registration proxy task successfully created with task uuid %s",
		contextId, common_consts.REGISTRATION_LOGGER_PREFIX,
		uuid4.ToUuid4(clusterRegistrationTaskUuidByte).String())

	// Populate item metadata with child task uuid.
	item.JobMetadata.StepsToTaskMap[0] = precheckTaskUuidByte
	item.JobMetadata.StepsToTaskMap[1] = csrTaskUuidByte
	item.JobMetadata.StepsToTaskMap[2] = clusterRegistrationTaskUuidByte
	item.JobMetadata.StepsCompleted += 1
	err = pc_utils.UpdateInternalOpaqueOfParentTask(
		consts.RegistrationChildTaskCreatedMessage, item)
	if err != nil {
		log.Errorf("[%s] %s Failed to update register_aos parent task"+
			" with uuid %s and with error %s", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX,
			uuid4.ToUuid4(item.JobMetadata.ParentTaskId).String(), err)
		return err
	}

	return nil
}

func (registrationUtil *RegistrationCommonUtil) CreateRegistrationMarkerNode(
	item *models.Job) error {
	contextId := item.JobMetadata.ContextId
	peClusterUuid := item.JobMetadata.RemoteClusterId

	zkPath := common_consts.REGISTRATION_MARKER_NODE_PATH +
		"/" + peClusterUuid + "/" + item.JobMetadata.TaskId

	err := external.Interfaces().ZkClient().CreateRecursive(zkPath, nil,
		true)
	if err != nil {
		log.Errorf("[%s] %s %s with path : %s and with error: %s",
			contextId, common_consts.REGISTRATION_LOGGER_PREFIX,
			consts.ErrorRegistrationMarkerNode.Error(), zkPath, err)
		return err
	}

	log.Infof("[%s] %s Created zk marker node at path %s", contextId,
		common_consts.REGISTRATION_LOGGER_PREFIX, zkPath)
	return nil
}

func (registrationUtil *RegistrationCommonUtil) DeleteRegistrationMarkerNode(item *models.Job) {
	contextId := item.JobMetadata.ContextId
	peClusterUuid := item.JobMetadata.RemoteClusterId

	taskUuidByte := item.JobMetadata.ParentTaskId
	taskUuid := uuid4.ToUuid4(taskUuidByte).String()

	if peClusterUuid == "" {
		log.Errorf("[%s] %s Cannot perform delete zk marker node because"+
			" peClusterUuid is nil", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX)
	} else {
		sourcePath := common_consts.REGISTRATION_MARKER_NODE_PATH
		zkPath := common_consts.REGISTRATION_MARKER_NODE_PATH + "/" +
			peClusterUuid + "/" + taskUuid

		log.Infof("[%s] %s Deleting the zk marker node at path %v", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX, zkPath)
		zkDeleteErr := external.Interfaces().ZkClient().DeleteRecursive(sourcePath,
			zkPath, true)
		if zkDeleteErr != nil {
			log.Errorf("[%s] %s Failed to delete zk marker node at path %s error %s",
				contextId, common_consts.REGISTRATION_LOGGER_PREFIX, zkPath,
				zkDeleteErr.Error(),
			)
		}
	}
}

func (registrationUtil *RegistrationCommonUtil) PrepareRegisterAosJob(
	requestDetails *models.RegisterAosRequestDetails, parentTaskId []byte,
	requestContext *net.RpcRequestContext) *models.Job {
	registrationTaskUuidString := uuid4.ToUuid4(parentTaskId).String()
	operationStartTime := time.Now().Format(consts.DateTimeFormat)
	jobMetadata := models.JobMetadata{
		Name:               consts.REGISTER_AOS,
		Rollback:           false,
		StepsCompleted:     0,
		TaskId:             registrationTaskUuidString,
		ParentTaskId:       parentTaskId,
		StepsToTaskMap:     map[int][]byte{},
		RemoteAddress:      *requestDetails.IPAddressOrFQDN,
		UseTrust:           *requestDetails.UseTrust,
		ContextId:          *requestDetails.ContextID,
		OperationStartTime: operationStartTime,
		ResourceExtId:      *requestDetails.ResourceExtId,
		IsRecoveredTask:    false,
		RequestContext:     requestContext,
	}

	log.Infof("[%s] %s Job metadata for current job %v", *requestDetails.ContextID,
		common_consts.REGISTRATION_LOGGER_PREFIX, jobMetadata)
	internalOpaque, err := json.Marshal(&jobMetadata)
	if err == nil {
		// Updating the internal opaque in parent task is the best effort.
		log.Infof("[%s] %s Updating the parent task %s with internal opaque ",
			*requestDetails.ContextID, common_consts.REGISTRATION_LOGGER_PREFIX,
			registrationTaskUuidString)
		err := external.Interfaces().ErgonService().UpdateTask(jobMetadata.ParentTaskId,
			consts.TaskStatusNone, "", nil, internalOpaque, nil, nil)
		if err != nil {
			log.Errorf("[%s] %s Could not update internal opaque for task %s with error %s",
				*requestDetails.ContextID, common_consts.REGISTRATION_LOGGER_PREFIX,
				registrationTaskUuidString, err)
		}
	} else {
		// This update of internal opaque will be reattempted at a later stage, so dont fail here.
		log.Errorf("[%s] %s Failed to marshal the object %v to json with error %s",
			*requestDetails.ContextID, common_consts.REGISTRATION_LOGGER_PREFIX, jobMetadata, err)
	}

	credentialMap := make(map[string]string)
	if !*requestDetails.UseTrust {
		credentialMap["username"] = *requestDetails.Username
		credentialMap["password"] = *requestDetails.Password
	}

	registerAosClusterJob := models.Job{
		Name:          consts.REGISTER_AOS,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}

	return &registerAosClusterJob
}

func (registrationUtil *RegistrationCommonUtil) CreateRegisterApiResponseDTO(item *models.Job) *management.RegisterRet {
	hasError := false
	isPaginated := false
	// Create a response dto
	response := pc_utils.CreateRegisterTaskResponse(item.JobMetadata.TaskId, hasError, isPaginated, consts.RelSelf)
	return response
}

func (registrationUtil *RegistrationCommonUtil) ProcessRegistrationErrorState(
	item *models.Job, errorMessage string, stepsCount int) {

	log.Errorf("[%s] %s %s",
		item.JobMetadata.ContextId,
		common_consts.REGISTRATION_LOGGER_PREFIX, errorMessage)
	item.JobMetadata.ErrorStep = stepsCount
	item.JobMetadata.OperationError = errorMessage

	pc_utils.UpdateInternalOpaqueOfParentTask("", item)

	// Push the pulse data
	pc_utils.PushPulseDataForRegisterAOS(item, false)
}
