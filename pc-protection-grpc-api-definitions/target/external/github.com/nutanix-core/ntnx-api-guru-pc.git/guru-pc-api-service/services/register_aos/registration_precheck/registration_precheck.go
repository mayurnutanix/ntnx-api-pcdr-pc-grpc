/*
 * Copyright (c) 2023 Nutanix Inc. All rights reserved.
 * Author: viraj.aute@nutanix.com
 * This file contains the logic of register_aos precheck
 */

package registration_precheck

import (
	"encoding/json"
	"fmt"
	"net/http"
	"net/url"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/services/register_aos/registration_common"
	pc_utils "ntnx-api-guru-pc/guru-pc-api-service/utils"

	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/external"

	"github.com/golang/protobuf/proto"
	ergon_proto "github.com/nutanix-core/go-cache/ergon"
	"github.com/nutanix-core/go-cache/util-go/uuid4"
	zeus_config "github.com/nutanix-core/go-cache/zeus/config"
	common_consts "github.com/nutanix-core/ntnx-api-guru/services/constants"
	"github.com/nutanix-core/ntnx-api-guru/services/utils"
	log "github.com/sirupsen/logrus"
)

func New() (RegistrationPrecheckIfc, error) {
	registrationCommonUtil, _ := registration_common.New()
	registrationPrecheck := &RegistrationPrecheck{
		registrationCommonUtil,
	}
	log.Debugf("%s Registration precheck DTO successfully created : %+v",
		common_consts.REGISTRATION_LOGGER_PREFIX, registrationPrecheck)
	return registrationPrecheck, nil
}

func (registrationPrecheck *RegistrationPrecheck) Execute(item *models.Job) error {

	// Fetch details from task details
	contextId := item.JobMetadata.ContextId

	// If recovered task then rollback because recovery cannot happen
	// as credentials are lost
	if item.JobMetadata.IsRecoveredTask {
		log.Errorf("[%s] %s Task recovered in registration precheck step "+
			"failing it because credentials lost before establishing trust",
			contextId, common_consts.REGISTRATION_LOGGER_PREFIX)
		return consts.ErrorRollbackRecoverTask
	}

	// Start the AOS cluster registration parent task
	external.Interfaces().ErgonService().UpdateTask(
		item.JobMetadata.ParentTaskId,
		ergon_proto.Task_kRunning, "",
		nil, nil, nil, nil,
	)

	// Start AOS cluster registration pre-check task.
	external.Interfaces().ErgonService().UpdateTask(
		item.JobMetadata.StepsToTaskMap[0],
		ergon_proto.Task_kRunning, consts.RegistrationPrecheckStartMessage,
		nil, nil, nil, nil,
	)

	// Execute prism element prechecks.
	guruErr := registrationPrecheck.ExecuteRemotePrecheck(item)
	if guruErr != nil {
		// create error details for updating the ergon task
		errorDetails := guruErr.GetTaskErrorDetails()
		item.JobMetadata.ErrorDetails = errorDetails
		utils.FailTaskWithError(external.Interfaces().ErgonService(),
			item.JobMetadata.StepsToTaskMap[0], errorDetails)
		return guruErr
	}

	// Update AOS cluster registration precheck task to 50%
	external.Interfaces().ErgonService().UpdateTask(
		item.JobMetadata.StepsToTaskMap[0], consts.TaskStatusNone,
		consts.RegistrationAOSPrecheckCompleteMessage,
		&common_consts.TASK_COMPLETED_FIFTY_PERCENTAGE, nil,
		nil, nil,
	)

	// Execute domain manager pre-checks.
	guruErr = registrationPrecheck.ExecuteLocalPrecheck(item)
	if guruErr != nil {
		// create error details for updating the ergon task
		errorDetails := guruErr.GetTaskErrorDetails()
		item.JobMetadata.ErrorDetails = errorDetails
		utils.FailTaskWithError(external.Interfaces().ErgonService(),
			item.JobMetadata.StepsToTaskMap[0], errorDetails)
		return guruErr
	}

	// Update AOS cluster registration precheck task to 100%
	err := external.Interfaces().ErgonService().UpdateTask(
		item.JobMetadata.StepsToTaskMap[0],
		ergon_proto.Task_kSucceeded, consts.RegistrationPrecheckCompleteMessage,
		nil, nil, nil, nil,
	)
	if err != nil {
		log.Errorf("[%s] %s Failed to update ergon task %s  with error: %v",
			contextId, common_consts.REGISTRATION_LOGGER_PREFIX,
			uuid4.ToUuid4(item.JobMetadata.StepsToTaskMap[0]).String(),
			err)

		// create error details for updating the ergon task
		errorDetails := guru_error.GetInternalError(consts.RegisterAosOpName).GetTaskErrorDetails()
		item.JobMetadata.ErrorDetails = errorDetails
		utils.FailTaskWithError(external.Interfaces().ErgonService(),
			item.JobMetadata.StepsToTaskMap[0], errorDetails)
		return err
	}

	log.Infof("[%s] %s  Registration precheck passed successfully", contextId,
		common_consts.REGISTRATION_LOGGER_PREFIX)

	// Create zk marker node with parent task uuid.
	err = registrationPrecheck.registration_common_util.CreateRegistrationMarkerNode(item)
	if err != nil {
		log.Errorf("[%s] %s Failed to update ergon task %s  with error: %v",
			contextId, common_consts.REGISTRATION_LOGGER_PREFIX,
			uuid4.ToUuid4(item.JobMetadata.StepsToTaskMap[0]).String(),
			err.Error())
		// create error details for updating the ergon task
		errorDetails := guru_error.GetInternalError(consts.RegisterAosOpName).GetTaskErrorDetails()
		item.JobMetadata.ErrorDetails = errorDetails
		utils.FailTaskWithError(external.Interfaces().ErgonService(),
			item.JobMetadata.StepsToTaskMap[0], errorDetails)
		return err
	}

	item.JobMetadata.StepsCompleted += 1
	// Below method holds all the post pre-check actions need to be performed.
	initiatePostPrecheckSteps(item)
	return nil
}

func (registrationPrecheck *RegistrationPrecheck) FetchPCDetails(
	item *models.Job,
) (string, string, error) {

	contextId := item.JobMetadata.ContextId

	// Fetch cluster uuid and cluster version from zeus config
	zeusConfigurationData, err := external.Interfaces().ZkClient().GetZkNode(
		common_consts.ZEUS_CONFIG_NODE_PATH, true)
	if err != nil {
		log.Errorf("[%s] %s Failed get zeus configuration zk node with error : %s",
			contextId, common_consts.REGISTRATION_LOGGER_PREFIX, err)

		return "", "", err
	}

	zeusConfigurationDTO := &zeus_config.ConfigurationProto{}
	err = proto.Unmarshal(zeusConfigurationData, zeusConfigurationDTO)
	if err != nil {
		log.Errorf("[%s] %s Failed to unmarshall zeus configuration zk"+
			" node with error : %v", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX, err)

		return "", "", err
	}

	if zeusConfigurationDTO.ClusterUuid == nil {
		log.Errorf("[%s] %s  Error in zeus configuration DTO : %v", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX,
			consts.ErrorZeusClusterUuidNil)

		return "", "", consts.ErrorZeusClusterUuidNil
	}

	if zeusConfigurationDTO.NodeList == nil ||
		len(zeusConfigurationDTO.NodeList) == 0 ||
		zeusConfigurationDTO.NodeList[0].SoftwareVersion == nil {

		log.Errorf("[%s] %s Error in zeus configuration DTO : %v", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX,
			consts.ErrorZeusVersionNil)

		return "", "", consts.ErrorZeusVersionNil
	}
	return *zeusConfigurationDTO.ClusterUuid,
		*zeusConfigurationDTO.NodeList[0].SoftwareVersion, nil
}

func (registrationPrecheck *RegistrationPrecheck) MakeRemotePEPrecheckCall(
	item *models.Job,
) (*models.ClusterRegistrationPrecheckDTO, guru_error.GuruErrorInterface) {

	// Fetch details from task details
	contextId := item.JobMetadata.ContextId
	useTrust := item.JobMetadata.UseTrust
	pcClusterUuid := item.JobMetadata.SelfClusterId
	pcVersion := item.JobMetadata.SelfClusterVersion

	clusterIpOrFqdn := item.JobMetadata.RemoteAddress
	uri := consts.RegistrationPrecheckUrl
	port := consts.EnvoyPort
	method := http.MethodGet
	headerParams := make(map[string]string)
	queryParams := url.Values{}
	apiClient := external.Interfaces().RemoteRestClient()
	var username, password string
	queryParams.Add("pcClusterUuid", pcClusterUuid)
	queryParams.Add("pcVersion", pcVersion)

	if !useTrust {
		username = item.CredentialMap["username"]
		password = item.CredentialMap["password"]
	}

	responseBody, responseCode, err := apiClient.CallApi(
		clusterIpOrFqdn, port, uri, method, nil,
		headerParams, queryParams, nil, &username, &password, useTrust)

	if err != nil {
		log.Errorf("[%s] %s Failure during remote precheck execution %s with error: %s",
			contextId, common_consts.REGISTRATION_LOGGER_PREFIX, uri,
			err.Error())

		// Extract error message from response body
		return nil, pc_utils.GetErrorFromStatusCode(responseCode, err, consts.RegisterAosOpName)
	}

	var clusterRegistrationPrecheckDTO models.ClusterRegistrationPrecheckDTO
	err = json.Unmarshal(responseBody, &clusterRegistrationPrecheckDTO)
	if err != nil {
		log.Errorf("[%s] %s Failed to unmarshal AOS cluster registration "+
			"precheck DTO with error: %v", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX, err)

		return nil, guru_error.GetInternalError(consts.RegisterAosOpName)
	}

	log.Debugf("[%s] %s Cluster Registration Precheck DTO unmarshalled %+v",
		contextId, common_consts.REGISTRATION_LOGGER_PREFIX,
		clusterRegistrationPrecheckDTO)

	log.Infof("[%s] %s  Successfully made AOS cluster registration precheck call to prism"+
		" element with cluster ip : %s", contextId,
		common_consts.REGISTRATION_LOGGER_PREFIX, item.JobMetadata.RemoteAddress)

	return &clusterRegistrationPrecheckDTO, nil
}

func (registrationPrecheck *RegistrationPrecheck) ValidatePEPrecheckResponse(
	item *models.Job,
	precheckDetailsDTO *models.ClusterRegistrationPrecheckDTO,
) guru_error.GuruErrorInterface {

	contextId := item.JobMetadata.ContextId

	if precheckDetailsDTO.IsUpgrading {
		log.Errorf("[%s] %s %v", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX,
			consts.ErrorPEIsUpgrading.Error())
		return guru_error.GetPreconditionFailureError(consts.ErrorPEIsUpgrading.Error(), consts.RegisterAosOpName)
	}
	log.Debugf("[%s] %s Prism Element's upgrade precheck passed", contextId,
		common_consts.REGISTRATION_LOGGER_PREFIX)

	if precheckDetailsDTO.IsClusterBlacklistedOnPE {
		log.Errorf("[%s] %s %v", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX,
			consts.ErrorPEIsBlacklisted.Error())
		return guru_error.GetPreconditionFailureError(consts.ErrorPEIsBlacklisted.Error(), consts.RegisterAosOpName)
	}
	log.Debugf("[%s] %s Prism Element's Blacklist precheck passed", contextId,
		common_consts.REGISTRATION_LOGGER_PREFIX)

	if precheckDetailsDTO.IsPcDeploymentInProgress {
		log.Errorf("[%s] %s %v", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX,
			consts.ErrorPEIsPcDeploymentInProgress.Error())
		return guru_error.GetPreconditionFailureError(consts.ErrorPEIsPcDeploymentInProgress.Error(), consts.RegisterAosOpName)
	}
	log.Debugf("[%s] %s Prism Element's prism central deployment is not in progress "+
		"precheck passed", contextId, common_consts.REGISTRATION_LOGGER_PREFIX)

	if precheckDetailsDTO.IsRegistered {
		log.Errorf("[%s] %s %v", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX,
			consts.ErrorPEIsRegistered.Error())
		return guru_error.GetPreconditionFailureError(consts.ErrorPEIsRegistered.Error(), consts.RegisterAosOpName)
	}
	log.Debugf("[%s] %s Prism Element's is not registered precheck passed",
		contextId, common_consts.REGISTRATION_LOGGER_PREFIX)

	if precheckDetailsDTO.IsUnregistrationInProgress {
		log.Errorf("[%s] %s %v", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX,
			consts.ErrorPEIsUnregistrationInProgress.Error())
		return guru_error.GetPreconditionFailureError(consts.ErrorPEIsUnregistrationInProgress.Error(), consts.RegisterAosOpName)
	}
	log.Debugf("[%s] %s Prism Element's unregistration in not progress "+
		"precheck passed", contextId, common_consts.REGISTRATION_LOGGER_PREFIX)

	if precheckDetailsDTO.IsRegistrationInProgress {
		log.Errorf("[%s] %s %v", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX,
			consts.ErrorPEIsRegistrationInProgress.Error())
		return guru_error.GetPreconditionFailureError(consts.ErrorPEIsRegistrationInProgress.Error(), consts.RegisterAosOpName)
	}
	log.Debugf("[%s] %s Prism Element's registration is in progress "+
		"precheck passed", contextId, common_consts.REGISTRATION_LOGGER_PREFIX)

	return nil
}

func (registrationPrecheck *RegistrationPrecheck) ExecuteRemotePrecheck(
	item *models.Job) guru_error.GuruErrorInterface {

	// Fetch details from task details
	contextId := item.JobMetadata.ContextId

	pcClusterUuid, pcVersion, err := registrationPrecheck.FetchPCDetails(item)
	if err != nil {
		log.Errorf("[%s] %s %s with error: %v", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX,
			consts.ErrorFetchPCDetails.Error(), err)
		return guru_error.GetInternalError(consts.RegisterAosOpName)
	}

	item.JobMetadata.SelfClusterId = pcClusterUuid
	item.JobMetadata.SelfClusterVersion = pcVersion

	peClusterRegistrationPrecheckDTO, guruErr :=
		registrationPrecheck.MakeRemotePEPrecheckCall(item)
	if guruErr != nil {
		log.Errorf("[%s] %s Failed to make registration precheck api call to "+
			"prism element with cluster ip : %s with error: %v", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX,
			item.JobMetadata.RemoteAddress, err)
		return guruErr
	}

	guruErr = registrationPrecheck.ValidatePEPrecheckResponse(item, peClusterRegistrationPrecheckDTO)
	if guruErr != nil {
		log.Errorf("[%s] %s %v", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX,
			consts.ErrorRegistrationPEPrecheck.Error())
		return guruErr
	}

	item.JobMetadata.RemoteVersion = peClusterRegistrationPrecheckDTO.ClusterVersion
	item.JobMetadata.RemoteClusterId = peClusterRegistrationPrecheckDTO.ClusterUuid

	log.Infof("[%s] %s  Successfully performed prism element's "+
		" registration precheck", contextId,
		common_consts.REGISTRATION_LOGGER_PREFIX)
	return nil
}

func (registrationPrecheck *RegistrationPrecheck) MakeLocalPCPrecheckCall(
	item *models.Job,
) (*models.ClusterRegistrationPrecheckDTO, guru_error.GuruErrorInterface) {

	contextId := item.JobMetadata.ContextId
	peClusterUuid := item.JobMetadata.RemoteClusterId
	peVersion := item.JobMetadata.RemoteVersion

	uri := consts.RegistrationPrecheckUrl
	port := consts.EnvoyPort
	method := http.MethodGet
	headerParams := make(map[string]string)
	queryParams := url.Values{}
	apiClient := external.Interfaces().RemoteRestClient()

	if peClusterUuid == "" {
		log.Errorf("[%s] %s Failed to invoke the endpoint %s with error: %v",
			contextId, common_consts.REGISTRATION_LOGGER_PREFIX, uri,
			consts.ErrorPEClusterUuidNil)
		return nil, guru_error.GetInternalError(consts.RegisterAosOpName)
	}

	if peVersion == "" {
		log.Errorf("[%s] %s Failed to invoke the endpoint %s with error: %v",
			contextId, common_consts.REGISTRATION_LOGGER_PREFIX, uri,
			consts.ErrorPEClusterVersionNil)
		return nil, guru_error.GetInternalError(consts.RegisterAosOpName)
	}

	queryParams.Add("peClusterUuid", peClusterUuid)
	queryParams.Add("peNosVersion", peVersion)

	responseBody, responseCode, err := apiClient.CallApi(
		consts.LocalHost, port, uri, method, nil,
		headerParams, queryParams, nil, nil, nil, true)
	if err != nil {
		log.Errorf("[%s] %s Failure during local precheck execution %s with error: %v",
			contextId, common_consts.REGISTRATION_LOGGER_PREFIX, uri, err)
		// Extract error message from response body
		return nil, pc_utils.GetErrorFromStatusCode(responseCode, err, consts.RegisterAosOpName)
	}

	var clusterRegistrationPrecheckDTO models.ClusterRegistrationPrecheckDTO
	err = json.Unmarshal(responseBody, &clusterRegistrationPrecheckDTO)
	if err != nil {
		log.Errorf("[%s] %s Failed to unmarshal with error %v", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX, err)

		return nil, guru_error.GetInternalError(consts.RegisterAosOpName)
	}

	log.Debugf("[%s] %s Cluster Registration Precheck DTO unmarshalled %v",
		contextId, common_consts.REGISTRATION_LOGGER_PREFIX,
		clusterRegistrationPrecheckDTO)
	log.Infof("[%s] %s  Successfully made registration precheck call to prism"+
		" central", contextId, common_consts.REGISTRATION_LOGGER_PREFIX)
	return &clusterRegistrationPrecheckDTO, nil
}

func (registrationPrecheck *RegistrationPrecheck) ValidatePCPrecheckResponse(
	item *models.Job, precheckDetailsDTO *models.ClusterRegistrationPrecheckDTO,
) guru_error.GuruErrorInterface {

	contextId := item.JobMetadata.ContextId
	peVersion := item.JobMetadata.RemoteVersion
	pcVersion := item.JobMetadata.SelfClusterVersion

	if precheckDetailsDTO.IsUpgrading {
		log.Errorf("[%s] %s %v", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX,
			consts.ErrorPCIsUpgrading.Error())
		return guru_error.GetPreconditionFailureError(consts.ErrorPCIsUpgrading.Error(), consts.RegisterAosOpName)
	}
	log.Debugf("[%s] %s Prism central's upgrade precheck passed", contextId,
		common_consts.REGISTRATION_LOGGER_PREFIX)

	if !precheckDetailsDTO.IsVersionCompatible {
		log.Errorf("[%s] %s %v prism element version : %s prism central version : %s",
			contextId, common_consts.REGISTRATION_LOGGER_PREFIX,
			"Prism Central is not compatible with the Prism element", peVersion, pcVersion)
		return guru_error.GetPreconditionFailureError(fmt.Sprintf("Prism Central with version %s is incompatible with the remote cluster with version %s",
			pcVersion, peVersion), consts.RegisterAosOpName)
	}
	log.Debugf("[%s] %s Prism central's version compatiblity precheck passed",
		contextId, common_consts.REGISTRATION_LOGGER_PREFIX)

	if precheckDetailsDTO.IsUnregistrationInProgress {
		log.Errorf("[%s] %s %v", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX,
			consts.ErrorPCIsUnregistrationInProgress.Error())
		return guru_error.GetPreconditionFailureError(consts.ErrorPCIsUnregistrationInProgress.Error(), consts.RegisterAosOpName)
	}
	log.Debugf("[%s] %s Prism central's prism central deployment in progress "+
		"precheck passed", contextId, common_consts.REGISTRATION_LOGGER_PREFIX)

	if precheckDetailsDTO.IsClusterBlacklistedOnPC {
		log.Errorf("[%s] %s %v", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX,
			consts.ErrorPCIsBlacklisted.Error())
		return guru_error.GetPreconditionFailureError(consts.ErrorPCIsBlacklisted.Error(), consts.RegisterAosOpName)
	}
	log.Debugf("[%s] %s Prism central's prism central blacklist "+
		"precheck passed", contextId, common_consts.REGISTRATION_LOGGER_PREFIX)

	return nil
}

func (registrationPrecheck *RegistrationPrecheck) CheckRegistrationInProgress(
	item *models.Job) (bool, string, error) {

	// Fetch details from task details
	contextId := item.JobMetadata.ContextId
	peClusterUuid := item.JobMetadata.RemoteClusterId

	zkNodePath := common_consts.REGISTRATION_MARKER_NODE_PATH + "/" + peClusterUuid
	exists, err := external.Interfaces().ZkClient().ExistZkNode(zkNodePath, true)

	if err != nil {
		log.Errorf("[%s] %s Failed to check existance of zknode for path %s "+
			"with error: %v", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX, zkNodePath, err)

		return false, "", err
	}

	if exists {
		log.Errorf("[%s] %s Another registration operation for the same remote cluster is in progress.",
			contextId, common_consts.REGISTRATION_LOGGER_PREFIX)

		// If zk marker node exists, look for task uuid which is the child node of marker node.
		childrenNodes, err := external.Interfaces().ZkClient().GetChildren(zkNodePath, true)
		if err != nil {
			log.Errorf("[%s] %s Failed to check the child zk node for path %s with error %s",
				contextId, common_consts.REGISTRATION_LOGGER_PREFIX,
				zkNodePath, err)
			return true, "", nil
		}

		if childrenNodes != nil && len(childrenNodes) > 0 {
			taskUuid := childrenNodes[0]
			return true, taskUuid, nil
		}
		return true, "", nil
	}

	log.Debugf("[%s] %s Prism central's is register in progress"+
		" precheck passed", contextId,
		common_consts.REGISTRATION_LOGGER_PREFIX)

	return false, "", nil
}

func (registrationPrecheck *RegistrationPrecheck) CheckAlreadyRegistered(
	item *models.Job,
) (bool, error) {

	// Fetch details from task details
	contextId := item.JobMetadata.ContextId
	peClusterUuid := item.JobMetadata.RemoteClusterId

	zkNodePath := common_consts.CLUSTER_EXTERNAL_STATE_PATH + "/" + peClusterUuid
	exists, err := external.Interfaces().ZkClient().ExistZkNode(zkNodePath, true)

	if err != nil {
		log.Errorf("[%s] %s Failed to check existance of zknode for path %s "+
			"with error: %v", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX, zkNodePath, err)

		return false, err
	}

	if exists {
		log.Errorf("[%s] %s %v",
			contextId, common_consts.REGISTRATION_LOGGER_PREFIX,
			consts.ErrorPCAlreadyRegistered.Error())

		return true, nil
	}

	log.Debugf("[%s] %s Prism central's already registered precheck passed",
		contextId, common_consts.REGISTRATION_LOGGER_PREFIX)

	return false, nil
}

func (registrationPrecheck *RegistrationPrecheck) CheckScaleOutInProgress(
	item *models.Job,
) (bool, error) {

	// Fetch details from task details
	contextId := item.JobMetadata.ContextId

	scaleoutTaskListByte, err := external.Interfaces().ErgonService().ListTask(nil,
		[]string{consts.OperationPcAddNodeRequest}, nil)

	if err != nil {
		log.Errorf("[%s] %s Failed to fetch scaleout task list with error : %v",
			contextId, common_consts.REGISTRATION_LOGGER_PREFIX, err)

		return false, err
	}

	if len(scaleoutTaskListByte) == 0 {
		return false, nil
	} else {
		for _, taskUuid := range scaleoutTaskListByte {
			task, err := external.Interfaces().ErgonService().GetTaskByUuid(taskUuid)
			if err != nil {
				log.Errorf("[%s] %s Failed to fetch task details of task %v"+
					" with error : %v", contextId,
					common_consts.REGISTRATION_LOGGER_PREFIX,
					uuid4.ToUuid4(taskUuid).String(), err)
				return false, err
			}
			if task.GetStatus() == ergon_proto.Task_kFailed ||
				task.GetStatus() == ergon_proto.Task_kAborted ||
				task.GetStatus() == ergon_proto.Task_kSucceeded {

				continue
			}

			log.Errorf("[%s] %s %s ", common_consts.REGISTRATION_LOGGER_PREFIX,
				contextId, consts.ErrorPCIsScaleoutInProgress.Error())
			return true, nil
		}
	}

	return false, nil
}

func (registrationPrecheck *RegistrationPrecheck) ExecuteLocalPrecheck(
	item *models.Job) guru_error.GuruErrorInterface {

	// Fetch details from task details
	contextId := item.JobMetadata.ContextId

	pcClusterRegistrationPrecheckDTO, guruErr :=
		registrationPrecheck.MakeLocalPCPrecheckCall(item)
	if guruErr != nil {
		log.Errorf("[%s] %s Failed to make registration precheck api call to "+
			"prism central with error: %v", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX, guruErr)
		return guruErr
	}

	guruErr = registrationPrecheck.ValidatePCPrecheckResponse(item, pcClusterRegistrationPrecheckDTO)
	if guruErr != nil {
		log.Errorf("[%s] %s %v", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX,
			consts.ErrorRegistrationPCPrecheck.Error())
		return guruErr
	}

	// check for already registered
	isRegistered, err :=
		registrationPrecheck.CheckAlreadyRegistered(item)
	if err != nil {
		log.Errorf("[%s] %s %s with error : %v", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX,
			consts.ErrorPCCheckRegistered.Error(), err)
		return guru_error.GetInternalError(consts.RegisterAosOpName)
	}
	if isRegistered {
		log.Errorf("[%s] %s %v", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX,
			consts.ErrorRegistrationPCPrecheck.Error())

		return guru_error.GetPreconditionFailureError(consts.ErrorPCAlreadyRegistered.Error(), consts.RegisterAosOpName)
	}

	// check for scaleout in progress
	isScaleOutInPrpgress, err :=
		registrationPrecheck.CheckScaleOutInProgress(item)
	if err != nil {
		log.Errorf("[%s] %s %s with error : %v", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX,
			consts.ErrorPCCheckScaleoutInProgress.Error(), err)
		return guru_error.GetInternalError(consts.RegisterAosOpName)
	}
	if isScaleOutInPrpgress {
		log.Errorf("[%s] %s %v", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX,
			consts.ErrorRegistrationPCPrecheck.Error())

		return guru_error.GetPreconditionFailureError(consts.ErrorPCIsScaleoutInProgress.Error(), consts.RegisterAosOpName)
	}

	// check if AOS cluster registration in progress
	isRegistrationInProgress, taskUuid, err :=
		registrationPrecheck.CheckRegistrationInProgress(item)
	if err != nil {
		log.Errorf("[%s] %s %s with error: %v", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX,
			consts.ErrorPCCheckRegistrationInProgress.Error(),
			err)
		return guru_error.GetInternalError(consts.RegisterAosOpName)
	}
	if isRegistrationInProgress {
		log.Errorf("[%s] %s %v", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX,
			consts.ErrorRegistrationPCPrecheck.Error())

		return guru_error.GetPreconditionFailureError(fmt.Sprintf("Another registration operation for the same remote cluster is in progress. Please track the task : %s",
			taskUuid), consts.RegisterAosOpName)
	}

	log.Infof("[%s] %s  Successfully performed prism central's registration"+
		" precheck", contextId, common_consts.REGISTRATION_LOGGER_PREFIX)
	return nil
}

func initiatePostPrecheckSteps(jobItem *models.Job) {
	// Update PE's cluster-uuid in Proxy registration task which will trigger action on  PE. This is for complying with
	// ergon guidelines where entity has to be recorded if it is undergoing state change.
	taskDomainManagerEntity := pc_utils.GetTaskEntityIdForClusterEntity([]byte(jobItem.JobMetadata.RemoteClusterId))
	taskEntityList := []*ergon_proto.EntityId{
		taskDomainManagerEntity,
	}
	taskClusterEntity, err := pc_utils.GetSelfDomainManagerEntity()
	if err != nil {
		log.Warnf("[%s] %s Failed to update domain manager entity in task: %s",
			jobItem.JobMetadata.ContextId, common_consts.REGISTRATION_LOGGER_PREFIX, err)
	} else {
		taskEntityList = append(taskEntityList, taskClusterEntity)
	}

	// Append remote cluster uuid to the entity list.
	var taskClusterList [][]byte
	remoteClusterUuid, err := uuid4.StringToUuid4(jobItem.JobMetadata.RemoteClusterId)
	if err != nil {
		log.Warnf("Failed to convert remote cluster uuid %s to uuid4 with"+
			" error  %s", jobItem.JobMetadata.RemoteClusterId, err)
	} else {
		taskClusterList = append(taskClusterList, remoteClusterUuid.RawBytes())
	}

	// Update CSR task with Entity "Cluster"
	taskResponse, err := external.Interfaces().ErgonClient().TaskGet(jobItem.JobMetadata.StepsToTaskMap[1])
	if err != nil {
		log.Errorf("Failed to get task with id %s error %s", uuid4.ToUuid4(
			jobItem.JobMetadata.StepsToTaskMap[1]).String(), err)
	} else {
		// Keeping else block to avoid null conditions in case taskResponse is nil.
		taskLogicalTimestamp := taskResponse.GetLogicalTimestamp() + int64(1)
		taskUpdateArg := ergon_proto.TaskUpdateArg{
			EntityList:       taskEntityList,
			Uuid:             jobItem.JobMetadata.StepsToTaskMap[1],
			LogicalTimestamp: proto.Int64(taskLogicalTimestamp),
			ClusterList:      taskClusterList,
		}

		_, err = external.Interfaces().ErgonClient().TaskUpdate(&taskUpdateArg)
		if err != nil {
			log.Errorf("Unable to update entity list %v in task  due to %s", taskEntityList,
				uuid4.ToUuid4(jobItem.JobMetadata.StepsToTaskMap[1]).String())
		}
	}

	// Update registration proxy task.
	taskResponse, err = external.Interfaces().ErgonClient().TaskGet(jobItem.JobMetadata.StepsToTaskMap[2])
	if err != nil {
		log.Errorf("Failed to get task with id %s error %s", uuid4.ToUuid4(
			jobItem.JobMetadata.StepsToTaskMap[2]).String(), err)
	} else {
		// Keeping else block to avoid null conditions in case taskResponse is nil.
		taskLogicalTimestamp := taskResponse.GetLogicalTimestamp() + int64(1)
		taskUpdateArg := ergon_proto.TaskUpdateArg{
			EntityList:       taskEntityList,
			Uuid:             jobItem.JobMetadata.StepsToTaskMap[2],
			LogicalTimestamp: proto.Int64(taskLogicalTimestamp),
			ClusterList:      taskClusterList,
		}

		_, err = external.Interfaces().ErgonClient().TaskUpdate(&taskUpdateArg)
		if err != nil {
			log.Errorf("Unable to update entity list %v in task due to %s", taskEntityList,
				uuid4.ToUuid4(jobItem.JobMetadata.StepsToTaskMap[2]).String())
		}
	}

	// Update parent task with the best effort.
	parentTaskRet, err := external.Interfaces().ErgonClient().TaskGet(jobItem.JobMetadata.ParentTaskId)
	if err != nil {
		log.Errorf("Failed to get task with id %s error %s", uuid4.ToUuid4(
			jobItem.JobMetadata.ParentTaskId).String(), err)
	} else {
		parentTaskLogicalTimestamp := parentTaskRet.GetLogicalTimestamp() + int64(1)
		parentTaskUpdateArg := ergon_proto.TaskUpdateArg{
			EntityList:       taskEntityList,
			Uuid:             jobItem.JobMetadata.ParentTaskId,
			LogicalTimestamp: proto.Int64(parentTaskLogicalTimestamp),
			ClusterList:      taskClusterList,
		}

		_, err = external.Interfaces().ErgonClient().TaskUpdate(&parentTaskUpdateArg)
		if err != nil {
			log.Errorf("Unable to update entity list %v in task %s due to %s", taskEntityList,
				uuid4.ToUuid4(jobItem.JobMetadata.ParentTaskId).String(), err)
		}
	}

	// Update internal opaque in parent task with the best effort.
	internalOpaque, err := json.Marshal(jobItem.JobMetadata)
	if err != nil {
		log.Errorf("[%s] %s Failed to marshal internal opaque for "+
			"task with uuid %s and with error %s", jobItem.JobMetadata.ContextId,
			common_consts.REGISTRATION_LOGGER_PREFIX,
			uuid4.ToUuid4(jobItem.JobMetadata.ParentTaskId).String(), err)
	}

	external.Interfaces().ErgonService().UpdateTask(
		jobItem.JobMetadata.ParentTaskId, consts.TaskStatusNone, consts.RegistrationPrecheckCompleteMessage,
		nil, internalOpaque, nil, nil)
}
