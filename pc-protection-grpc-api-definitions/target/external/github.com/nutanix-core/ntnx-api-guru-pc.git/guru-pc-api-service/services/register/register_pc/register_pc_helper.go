/*
 * Copyright (c) 2024 Nutanix Inc. All rights reserved.
 * Author: anmol.garg@nutanix.com
 *
 * This file contains the RegisterPcInterface interface which can be implemented by any cluster type eg. MULTI_CLUSTER, WITNESS_VM etc.
 */

package register_pc

import (
	"encoding/json"
	"fmt"
	"net/http"
	ntnxApiGuruError "ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/error"

	common_config "ntnx-api-guru-pc/generated-code/target/dto/models/common/v1/config"
	dto "ntnx-api-guru-pc/generated-code/target/dto/models/prism/v4/management"
	"ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/management"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/grpc_error"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/external"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"time"

	"github.com/golang/protobuf/proto"
	"github.com/nutanix-core/go-cache/util-go/misc"
	"github.com/nutanix-core/go-cache/util-go/uuid4"
	"github.com/nutanix-core/go-cache/util-slbufs/util/sl_bufs/net"

	"ntnx-api-guru-pc/guru-pc-api-service/utils"

	log "github.com/sirupsen/logrus"

	"github.com/nutanix-core/go-cache/ergon"
)

const (
	V4AddRootCertApiPath         = "/api/prism/%s/management/domain-managers/%s/$actions/add-root-certificate"
)

func CreateRegisterPcTask(requestId string, batchTaskId string, requestContext *net.RpcRequestContext, resourceExtId string) (*ergon.Task, []*ntnxApiGuruError.AppMessage, error) {
	var batchTaskIdByte []byte
	if batchTaskId != "" {
		batchUuid, _ := uuid4.StringToUuid4(batchTaskId)
		batchTaskIdByte = batchUuid.RawBytes()
	}
	taskRequestContext := utils.CreateTaskRequestContextFromGrpcContext(requestContext)
	taskEntityIdList := []*ergon.EntityId{}
	domainManagerEntityId, err := utils.GetSelfDomainManagerEntity()
	if err != nil {
		log.Warnf("[%s] Failed while updating entities in register pc task: %s", requestId, err)
	} else {
		taskEntityIdList = append(taskEntityIdList, domainManagerEntityId)
	}
	taskCreateArg := ergon.TaskCreateArg{
		OperationType:                proto.String(consts.OperationTypeMap[consts.REGISTER_PC]),
		Message:                      proto.String(utils.GetTaskCreationMessage(consts.REGISTER_PC)),
		DisplayName:                  proto.String(consts.OperationNameMap[consts.REGISTER_PC]),
		Component:                    proto.String(consts.GuruTaskComponent),
		RequestId:                    &requestId,
		RequestContext:               &taskRequestContext,
		EntityList:                   taskEntityIdList,
		ParentTaskUuid:               batchTaskIdByte,
		DisableAutoClusterListUpdate: proto.Bool(true),
	}

	taskRet, err := external.Interfaces().ErgonClient().TaskCreate(&taskCreateArg)
	if err != nil {
		guruErr := guru_error.GetServiceUnavailableError(consts.RegisterPCOpName)
		return nil, []*ntnxApiGuruError.AppMessage{guruErr.ConvertToAppMessagePb()}, grpc_error.GetGrpcStatusUtilImpl().BuildGrpcError(guruErr)
	}

	task, err := utils.GetTask(taskRet.GetUuid())
	if err != nil {
		guruErr := guru_error.GetServiceUnavailableError(consts.RegisterPCOpName)
		return nil, []*ntnxApiGuruError.AppMessage{guruErr.ConvertToAppMessagePb()}, grpc_error.GetGrpcStatusUtilImpl().BuildGrpcError(guruErr)
	}

	// Update the task to create an entry in the request_identifier table
	// for idempotency control, so that future requests with same request id
	// return the same task
	err = external.Interfaces().IdempotencyService().UpdateTask(requestId, uuid4.ToUuid4(task.GetUuid()).String())
	if err != nil {
		log.Errorf("Failed to update the task in the request_identifer table for idempotency control with error - %s", err)
	}
	return task, nil, nil
}

func CreateNewRegisterPcGuruJob(task *ergon.Task, args *management.RegisterArg, requestId string) *models.Job {
	credentialsMap := make(map[string]string)
	credentialsMap["username"] = args.GetBody().GetDomainManagerRemoteClusterSpecRemoteCluster().GetValue().
		GetRemoteCluster().GetCredentials().GetAuthentication().GetUsername()
	credentialsMap["password"] = args.GetBody().GetDomainManagerRemoteClusterSpecRemoteCluster().
		GetValue().GetRemoteCluster().GetCredentials().GetAuthentication().GetPassword()

	job := models.Job{
		CredentialMap: credentialsMap,
		Name:          consts.REGISTER_PC,
		ParentTask:    task,
		JobMetadata: &models.JobMetadata{
			RemoteAddress: utils.GetIpAddressOrFqdn(args.GetBody().GetDomainManagerRemoteClusterSpecRemoteCluster().
				GetValue().GetRemoteCluster().GetAddress()),
			TaskId:              uuid4.String(task.GetUuid()),
			ContextId:           requestId,
			SelfClusterId:       args.GetExtId(),
			SelfExternalAddress: consts.HostAddr,
			OperationStartTime:  time.Now().Format(consts.DateTimeFormat),
			RemoteClusterType:   dto.CLUSTERTYPE_DOMAIN_MANAGER,
		},
	}
	return &job
}

// Update the job with the self cluster details
func updateSelfClusterDetails(job *models.Job) guru_error.GuruErrorInterface {
	// Get the local environment info
	localEnvInfo, err := utils.FetchPcEnvironmentInfo()
	if err != nil {
		log.Errorf("[%s] error fetching pc environment info: %s", job.JobMetadata.ContextId, err)
		return guru_error.GetInternalError(consts.RegisterPCOpName)
	} else {
		job.JobMetadata.SelfEnvironmentType = localEnvInfo.GetEnvironmentType().String()
		job.JobMetadata.SelfInstanceType = localEnvInfo.GetInstanceType().String()
		job.JobMetadata.SelfProviderType = localEnvInfo.GetCloudProviderInfo().String()
	}

	// Get the local cluster details
	zeusConfig, err := utils.FetchZeusConfig()
	if err != nil {
		log.Errorf("[%s] error fetching zeus config: %s", job.JobMetadata.ContextId, err)
		return guru_error.GetInternalError(consts.RegisterPCOpName)
	} else {
		// Get cluster details
		job.JobMetadata.SelfClusterName = zeusConfig.GetClusterName()
		selfPCVersion, err := misc.GetPrismCentralVersion()
		if err != nil {
			log.Errorf("[%s] Error fetching prism central version: %s", job.JobMetadata.ContextId, err)
		} else {
			job.JobMetadata.SelfClusterVersion = selfPCVersion
		}
		job.JobMetadata.SelfClusterId = zeusConfig.GetClusterUuid()
		// Get the IP list
		selfIpList := []string{}
		list := zeusConfig.GetNodeList()
		for _, node := range list {
			selfIpList = append(selfIpList, node.GetSvmExternalIpList()...)
		}
		job.JobMetadata.SelfNodeIPAddresses = selfIpList
		log.Debugf("[%s] Self IP List: %+v", job.JobMetadata.ContextId, selfIpList)
	}
	return nil
}

// Update the job with the self cluster details
func updateRemoteClusterDetails(job *models.Job, response *V3PrismCentralResponseModel) {
	remoteIpList := []string{}
	for _, pcVm := range response.Resources.PCVMList {
		for _, nic := range pcVm.NICList {
			remoteIpList = append(remoteIpList, nic.IPList...)
		}
	}

	job.JobMetadata.RemoteNodeIPAddresses = remoteIpList
	if response.Resources.VirtualIP != "" {
		job.JobMetadata.RemoteAddress = response.Resources.VirtualIP
	}
	job.JobMetadata.RemoteClusterId = response.Resources.ClusterUuid
	job.JobMetadata.RemoteVersion = response.Resources.Version
	job.JobMetadata.RemoteEnvironmentType = response.Resources.PcEnvironment.EnvironmentType
	job.JobMetadata.RemoteInstanceType = response.Resources.PcEnvironment.InstanceType
	job.JobMetadata.RemoteProviderType = response.Resources.PcEnvironment.ProviderType
}

func callAddRootCertificateApi(rootCert *string, job *models.Job) (string, guru_error.GuruErrorInterface) {
	spec := dto.NewRootCertificateAddSpec()
	spec.RootCertificate = rootCert
	spec.ExtId = &job.JobMetadata.SelfClusterId
	url := fmt.Sprintf(V4AddRootCertApiPath, consts.ApiVersion, job.JobMetadata.RemoteClusterId)
	headers := make(map[string]string)
	uuid, _ := external.Interfaces().Uuid().New()
	headers[consts.NtnxRequestId] = uuid.String()
	response, responseCode, err := external.Interfaces().RemoteRestClient().CallApiBasicAuth(
		job.JobMetadata.RemoteAddress, consts.EnvoyPort, url, http.MethodPost, &spec,
		headers, nil, nil, job.CredentialMap["username"], job.CredentialMap["password"])
	if err != nil {
		return "", utils.GetErrorFromStatusCode(responseCode, err, consts.RegisterPCOpName)
	}
	taskResponse := utils.TaskResponseModel{}
	err = json.Unmarshal(response, &taskResponse)
	if err != nil {
		return "", guru_error.GetInternalError(consts.RegisterPCOpName)
	}
	return taskResponse.Data.ExtId, nil
}

func callConfigureConnectionApi(metadata *models.JobMetadata) (string, guru_error.GuruErrorInterface) {
	url := fmt.Sprintf(consts.V4ConfigureConnectionApiPath, consts.ApiVersion, metadata.RemoteClusterId)
	spec := createConfigureConnectionSpec(metadata)
	headers := make(map[string]string)
	uuid, _ := external.Interfaces().Uuid().New()
	headers[consts.NtnxRequestId] = uuid.String()
	response, responseCode, err := external.Interfaces().RemoteRestClient().CallApiCertBasedAuth(
		metadata.RemoteAddress, consts.EnvoyPort, url, http.MethodPost, spec, headers, nil, nil)
	if err != nil {
		return "", utils.GetErrorFromStatusCode(responseCode, err, consts.RegisterPCOpName)
	}

	// Unmarshal response
	taskResponse := utils.TaskResponseModel{}
	err = json.Unmarshal(response, &taskResponse)
	if err != nil {
		log.Errorf("[%s] Failed to unmarshal task response %s", metadata.ContextId, err)
		return "", guru_error.GetInternalError(consts.RegisterPCOpName)
	}
	return taskResponse.Data.ExtId, nil
}

// TODO: @deepanshu-nutanix replace this with domain manager api once api is available
func callPrismCentralApi(useTrust bool, hostOrClusterUuid string, username string, password string) (*V3PrismCentralResponseModel, guru_error.GuruErrorInterface) {
	// call v3/prism_central to fetch cluster uuid. Same will act as credentials validation.
	// check version also. if remote has guru enabled version.
	var response []byte
	var responseCode *int
	var err error
	if useTrust {
		response, responseCode, err = external.Interfaces().RemoteRestClient().FanoutProxy(
			http.MethodGet, nil, http.MethodGet, hostOrClusterUuid, "prism_central")
	} else {
		response, responseCode, err = external.Interfaces().RemoteRestClient().CallApiBasicAuth(
			hostOrClusterUuid, consts.EnvoyPort, consts.V3PrismCentralGetUrl, http.MethodGet,
			nil, nil, nil, nil, username, password,
		)
	}
	if err != nil {
		log.Errorf("error calling get v3 prism central api on remote pc: %v", err)
		return nil, utils.GetErrorFromStatusCode(responseCode, err, consts.RegisterPCOpName)
	}
	var responseModel V3PrismCentralResponseModel
	err = json.Unmarshal(response, &responseModel)
	if err != nil {
		log.Errorf("error unmarshalling get v3 prism central response: %v", err)
		return nil, guru_error.GetInternalError(consts.RegisterPCOpName)
	}
	return &responseModel, nil
}

func pollTask(taskUuid, remoteAddress string) guru_error.GuruErrorInterface {
	// Poll for task completion
	taskStatus, result := utils.GetRemoteTaskStatus(taskUuid, remoteAddress)
	if !result {
		log.Errorf("remote task fetching failure")
		return guru_error.GetInternalError(consts.RegisterPCOpName)
	}
	if *taskStatus == ergon.Task_kSucceeded {
		log.Infof("Remote task %s completed successfully", taskUuid)
	} else {
		log.Errorf("remote task failed with status: %s", taskStatus.String())
		return guru_error.GetInternalError(consts.RegisterPCOpName)
	}
	return nil
}

func createConfigureConnectionSpec(metadata *models.JobMetadata) *dto.ConnectionConfigurationSpec {
	spec := dto.NewConnectionConfigurationSpec()
	spec.RemoteCluster = &dto.RemoteCluster{
		ExtId: &metadata.SelfClusterId,
		ExternalAddress: &common_config.IPAddressOrFQDN{
			Ipv4: &common_config.IPv4Address{Value: &metadata.SelfExternalAddress},
		},
		Name:            &metadata.SelfClusterName,
		NodeIpAddresses: []common_config.IPAddressOrFQDN{},
	}
	if metadata.SelfNodeIPAddresses != nil {
		for _, ip := range metadata.SelfNodeIPAddresses {
			nodeIpAddress := common_config.IPAddressOrFQDN{
				Ipv4: &common_config.IPv4Address{
					Value: proto.String(ip),
				},
			}
			spec.RemoteCluster.NodeIpAddresses = append(spec.RemoteCluster.NodeIpAddresses, nodeIpAddress)
		}
	}
	log.Debugf("Configure connection spec for remote: %+v", spec)
	return spec
}

// Update the job with the self and remote cluster details as affected entities
func updateAffectedEntitiesInTask(job *models.Job) error {
	selfEntity := &ergon.EntityId{
		QualifiedEntityType: proto.String(consts.DomainManagerQualifiedEntityType),
		EntityName:          proto.String(job.JobMetadata.SelfClusterName),
		EntityId:            []byte(job.JobMetadata.SelfClusterId),
	}
	remoteEnity := &ergon.EntityId{
		QualifiedEntityType: proto.String(consts.DomainManagerQualifiedEntityType),
		EntityName:          proto.String(job.JobMetadata.RemoteClusterName),
		EntityId:            []byte(job.JobMetadata.RemoteClusterId),
	}

	taskUpdateArg := ergon.TaskUpdateArg{
		Uuid:             job.ParentTask.Uuid,
		LogicalTimestamp: job.ParentTask.LogicalTimestamp,
		EntityList:       []*ergon.EntityId{selfEntity, remoteEnity},
	}
	ret, err := external.Interfaces().ErgonClient().TaskUpdate(&taskUpdateArg)
	if err != nil {
		log.Warnf("[%s] %s Failed to update task %s: %s",
			job.JobMetadata.ContextId, consts.RegisterPCLoggerPrefix, job.JobMetadata.ParentTaskId, err)
		return err
	}

	job.ParentTask = ret.GetTask()
	return nil
}

func CreateAddRootCertJob(args *management.AddRootCertificateArg, addRootCertificateTask *ergon.Task, requestId string) *models.Job {
	var job = &models.Job{
		JobMetadata: &models.JobMetadata{
			OperationStartTime: time.Now().Format(consts.DateTimeFormat),
			ParentTaskId:       addRootCertificateTask.GetUuid(),
			TaskId:             uuid4.String(addRootCertificateTask.GetUuid()),
			ContextId:          requestId,
			Name:               consts.ADD_ROOT_CERTIFICATE,
			SelfClusterId:      args.GetExtId(),
			RootCert:           args.GetBody().RootCertificate,
			RemoteClusterId:    args.GetBody().GetBase().GetExtId(),
		},
		ParentTask: addRootCertificateTask,
		Name:       consts.ADD_ROOT_CERTIFICATE,
	}
	return job
}
