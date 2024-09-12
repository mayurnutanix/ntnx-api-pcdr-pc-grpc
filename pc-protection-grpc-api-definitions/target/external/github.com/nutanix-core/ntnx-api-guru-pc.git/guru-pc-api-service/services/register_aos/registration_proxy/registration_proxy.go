/*
 * Copyright (c) 2023 Nutanix Inc. All rights reserved.
 * Author: harsh.seta@nutanix.com
 * This file contains the logic of registration AOS cluster proxy
 */

package registration_proxy

import (
	"encoding/json"
	"net/http"
	"net/url"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/external"
	"ntnx-api-guru-pc/guru-pc-api-service/models"

	"ntnx-api-guru-pc/guru-pc-api-service/services/register_aos/registration_common"
	pc_utils "ntnx-api-guru-pc/guru-pc-api-service/utils"

	ergon_proto "github.com/nutanix-core/go-cache/ergon"
	"github.com/nutanix-core/go-cache/util-go/uuid4"
	common_consts "github.com/nutanix-core/ntnx-api-guru/services/constants"
	"github.com/nutanix-core/ntnx-api-guru/services/utils"
	log "github.com/sirupsen/logrus"
)

func New() (RegistrationProxyIfc, error) {
	registrationCommonUtil, _ := registration_common.New()
	registrationProxy := &RegistrationProxy{
		registrationCommonUtil,
	}

	return registrationProxy, nil
}

func (registrationProxy *RegistrationProxy) Execute(item *models.Job) error {

	// Fetch registration tasks and context id from task details
	contextId := item.JobMetadata.ContextId

	if item.JobMetadata.RemoteTaskId == "" {
		// Start cluster registration task
		external.Interfaces().ErgonService().UpdateTask(
			item.JobMetadata.StepsToTaskMap[2], ergon_proto.Task_kRunning,
			consts.RegistrationProxyStartMessage,
			nil, nil, nil, nil,
		)

		peTaskUuid, guruErr := registrationProxy.TriggerRegisterOnPe(item)
		if guruErr != nil {
			// create error details for updating the ergon task
			errorDetails := guruErr.GetTaskErrorDetails()
			item.JobMetadata.ErrorDetails = errorDetails
			log.Errorf("[%s] %s Failed to trigger registration on PE with error %s",
				contextId, common_consts.REGISTRATION_LOGGER_PREFIX, guruErr)
			utils.FailTaskWithError(external.Interfaces().ErgonService(),
				item.JobMetadata.StepsToTaskMap[2], errorDetails)

			return guruErr
		}

		log.Infof("[%s] %s AOS cluster registration task: %s", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX, peTaskUuid)
		item.JobMetadata.RemoteTaskId = peTaskUuid

		err := pc_utils.UpdateInternalOpaqueOfParentTask("", item)
		if err != nil {
			log.Errorf("[%s] %s Failed to update AOS cluster registration parent task"+
				" with uuid %s and with error %s", contextId,
				common_consts.REGISTRATION_LOGGER_PREFIX,
				uuid4.ToUuid4(item.JobMetadata.ParentTaskId).String(), err)
			return err
		}
	}

	peTaskUuidString := item.JobMetadata.RemoteTaskId
	peTaskUuid, _ := uuid4.StringToUuid4(peTaskUuidString)
	log.Infof("[%s] %s Polling on PE registration task uuid %s", contextId,
		common_consts.REGISTRATION_LOGGER_PREFIX, peTaskUuidString)

	peRegistrationTaskStatus, err := external.Interfaces().
		ErgonService().PollTask(peTaskUuid.RawBytes(), consts.ErgonPollTimeout)
	if err != nil {
		log.Errorf("[%s] %s Failed to poll PE ergon task %s with error %s", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX, peTaskUuid, err)
		// create error details for updating the ergon task
		errorDetails := guru_error.GetServiceUnavailableError(consts.RegisterAosOpName).GetTaskErrorDetails()
		item.JobMetadata.ErrorDetails = errorDetails
		utils.FailTaskWithError(external.Interfaces().ErgonService(),
			item.JobMetadata.StepsToTaskMap[2], errorDetails)
		return err
	}

	if *peRegistrationTaskStatus != ergon_proto.Task_kSucceeded {
		log.Errorf("[%s] %s PE registration task failed %s", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX, peTaskUuid)
		// create error details for updating the ergon task
		errorDetails := guru_error.GetInternalError(consts.RegisterAosOpName).GetTaskErrorDetails()
		item.JobMetadata.ErrorDetails = errorDetails
		utils.FailTaskWithError(external.Interfaces().ErgonService(),
			item.JobMetadata.StepsToTaskMap[2], errorDetails)
		return err
	}

	// Update registration proxy task.
	err = external.Interfaces().ErgonService().UpdateTask(
		item.JobMetadata.StepsToTaskMap[2],
		ergon_proto.Task_kSucceeded,
		consts.RegistrationProxyCompleteMessage,
		nil, nil, nil, nil,
	)
	if err != nil {
		log.Errorf("[%s] %s Failed to update ergon task %s  with error: %v",
			contextId, common_consts.REGISTRATION_LOGGER_PREFIX,
			uuid4.ToUuid4(item.JobMetadata.StepsToTaskMap[2]).String(),
			err.Error())

		// create error details for updating the ergon task
		errorDetails := guru_error.GetServiceUnavailableError(consts.RegisterAosOpName).GetTaskErrorDetails()
		item.JobMetadata.ErrorDetails = errorDetails
		utils.FailTaskWithError(external.Interfaces().ErgonService(),
			item.JobMetadata.StepsToTaskMap[2], errorDetails)
		return err
	}

	// Delete the zk node with the best effort.
	registrationProxy.registration_common_util.DeleteRegistrationMarkerNode(item)
	item.JobMetadata.IsOperationComplete = true
	item.JobMetadata.StepsCompleted += 1

	log.Infof("[%s] %s Succesfully executed AOS cluster registration parent task  : %s",
		contextId, common_consts.REGISTRATION_LOGGER_PREFIX,
		uuid4.ToUuid4(item.JobMetadata.ParentTaskId).String())

	// create internal opaque in json format
	internalOpaque, err := json.Marshal(item.JobMetadata)
	if err != nil {
		log.Errorf("[%s] %s Failed to marshal internal opaque for "+
			"task with uuid %s and with error %s", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX,
			uuid4.ToUuid4(item.JobMetadata.ParentTaskId).String(), err)
	}
	// Updating the internal opaque and updating the task completion percentage
	external.Interfaces().ErgonService().UpdateTask(
		item.JobMetadata.ParentTaskId, ergon_proto.Task_kSucceeded, consts.RegistrationSuccessMessage,
		nil, internalOpaque, nil, nil)

	// Push pulse data
	pc_utils.PushPulseDataForRegisterAOS(item, true)
	return nil
}

func (registrationProxy *RegistrationProxy) TriggerRegisterOnPe(
	item *models.Job) (string, guru_error.GuruErrorInterface) {

	contextId := item.JobMetadata.ContextId
	peIpOrFqdn := item.JobMetadata.RemoteAddress
	pcIP := consts.HostAddr

	payload := &models.ClusterExternalDetailsDTO{
		UseTrust:    true,
		IpAddresses: []string{pcIP},
		Port:        consts.EnvoyPort,
		ClusterUuid: item.JobMetadata.SelfClusterId,
	}
	res, responseCode, err := external.Interfaces().RemoteRestClient().CallApi(
		peIpOrFqdn, consts.EnvoyPort, consts.ProxyRegistrationUrl,
		http.MethodPost, payload, nil, nil, nil, nil, nil, true,
	)
	if err != nil {
		log.Errorf("[%s] %s Failed to call registration API on PE: %s",
			contextId, common_consts.REGISTRATION_LOGGER_PREFIX, err)
		// Extract error message from response body
		return "", pc_utils.GetErrorFromStatusCode(responseCode, err, consts.RegisterAosOpName)
	}

	var unmarshalledResp models.TaskIdDTO
	err = json.Unmarshal(res, &unmarshalledResp)
	if err != nil {
		log.Errorf("[%s] %s Failed to unmarshal with error %s", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX, err)
		return "", guru_error.GetInternalError(consts.RegisterAosOpName)
	}
	return unmarshalledResp.TaskUuid, nil
}

func (registrationProxy *RegistrationProxy) DeleteClusterState(
	item *models.Job) (*bool, error) {
	// Delete the entities created during cluster registration
	// on Prism Central
	// i.e it deletes the cluster external state, cluster data state,
	// and internal user

	contextId := item.JobMetadata.ContextId
	hostIp := consts.LocalHost
	uri := consts.ClusterExternalStateUrl + "/" + item.JobMetadata.RemoteClusterId
	port := consts.EnvoyPort
	method := http.MethodDelete
	headerParams := make(map[string]string)
	queryParams := url.Values{}
	queryParams.Add("skipCleanups", "false")
	queryParams.Add("skipPrechecks", "true")

	responseBody, responseCode, err := external.Interfaces().RemoteRestClient().CallApi(
		hostIp, port, uri, method, nil, headerParams, queryParams, nil, nil, nil, true)
	if err != nil {
		log.Errorf("[%s] %s Failure while deleting cluster external state: %v",
			contextId, common_consts.REGISTRATION_LOGGER_PREFIX, err)
		// Extract error message from response body
		return nil, pc_utils.GetErrorFromStatusCode(responseCode, err, consts.RegisterAosOpName)
	}
	var unmarshalledResp models.PrimitiveDTO
	err = json.Unmarshal(responseBody, &unmarshalledResp)
	if err != nil {
		log.Errorf("[%s] %s Failed to unmarshal with error %v", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX, err)
		return nil, guru_error.GetInternalError(consts.RegisterAosOpName)
	}

	log.Debugf("[%s] %s Unmmarshalled response of Delete cluster external state %+v",
		contextId, common_consts.REGISTRATION_LOGGER_PREFIX, unmarshalledResp)
	success := unmarshalledResp.Value
	log.Infof("[%s] %s Status of Delete cluster external state :%v",
		contextId, common_consts.REGISTRATION_LOGGER_PREFIX, success)
	return &success, nil
}

func (registrationProxy *RegistrationProxy) Rollback(
	item *models.Job) {

	success, err := registrationProxy.DeleteClusterState(item)
	if err != nil || !*success {
		log.Errorf("[%s] %s Failure while rollback - delete cluster external state failed: %v",
			item.JobMetadata.ContextId, common_consts.REGISTRATION_LOGGER_PREFIX, err)
	}
}
