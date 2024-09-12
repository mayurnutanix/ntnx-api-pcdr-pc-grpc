// Copyright (c) 2022 Nutanix Inc. All rights reserved.
// Author: viraj.aute@nutanix.com
//
// Common Utility functions for guru service
package utils

import (
	"encoding/json"
	"fmt"
	"net/http"
	"strings"
	"time"
	"unicode"

	dto "ntnx-api-guru-pc/generated-code/target/dto/models/prism/v4/management"
	common_config "ntnx-api-guru-pc/generated-code/target/protobuf/common/v1/config"
	"ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/management"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/external"

	common_models "github.com/nutanix-core/go-cache/api/pe"
	"github.com/nutanix-core/go-cache/ergon"
	"github.com/nutanix-core/go-cache/util-go/misc"
	zeus_config "github.com/nutanix-core/go-cache/zeus/config"

	log "github.com/sirupsen/logrus"
)

func ExtractStatusFromErgonTask(
	responseMap *map[string]interface{},
) ergon.Task_Status {
	taskInfo, _ :=
		(*responseMap)["data"].(map[string]interface{})
	status, _ := taskInfo["status"].(string)
	status = strings.Title(strings.ToLower(status))
	ergonStatus :=
		ergon.Task_Status(ergon.Task_Status_value["k"+status])
	log.Infof("Task status of polling on acceptor %v", ergonStatus.String())
	return ergonStatus
}

func GetRemoteTaskStatus(
	taskUuid string,
	remoteHost string,
) (*ergon.Task_Status, bool) {

	// Utility function to get TaskReference extId from Nutanix uuid.
	// TaskReference extId is of the format taskPrefix:uuid.
	// Send Api request
	url := consts.TasksUrl + "/" + taskUuid
	log.Infof("Polling on url %s", url)

	backoff := misc.NewExponentialBackoff(
		time.Duration(*consts.ApiPollIntervalInitial)*time.Second,
		time.Duration(*consts.ApiPollIntervalMax)*time.Second,
		*consts.ApiPollMaxRetries)

	retry := 0
	for {
		retry++
		log.Infof("Polling ergon task %s retry number %d", taskUuid, retry)
		duration := backoff.Backoff()
		if duration == misc.Stop {
			break
		}

		response, _, err := external.Interfaces().RemoteRestClient().CallApi(remoteHost, consts.EnvoyPort,
			url, http.MethodGet, nil, nil, nil, nil, nil, nil, true)

		if err != nil {
			log.Debugf("Polling task %s with retry %d", taskUuid, retry)
			continue
		}
		// Extract Status
		var responseMap map[string]interface{}
		json.Unmarshal(response, &responseMap)
		ergonStatus := ExtractStatusFromErgonTask(&responseMap)

		if !isTerminalStatus(ergonStatus) {
			log.Debugf("Task %s status is %s", taskUuid, ergonStatus.String())
			continue
		}

		return &ergonStatus, true
	}

	return nil, false
}

func isTerminalStatus(status ergon.Task_Status) bool {
	switch status {
	case ergon.Task_kFailed, ergon.Task_kSucceeded, ergon.Task_kAborted, ergon.Task_kSuspended:
		return true
	default:
		return false
	}
}

type KeyValue struct {
	Key   string `json:"name"`
	Value string `json:"value"`
}

func GetRemoteTaskCompletionDetails(
	taskExtId string,
	remoteHost string,
	operation string,
) ([]*KeyValue, guru_error.GuruErrorInterface) {

	response, guruErr := GetRemoteTask(taskExtId, remoteHost, operation)

	if guruErr != nil {
		return nil, guruErr
	}
	// Extract Completion Details
	TaskResponseModel := struct {
		Data struct {
			CompletionDetails []*KeyValue `json:"completionDetails"`
		} `json:"data"`
	}{}

	err := json.Unmarshal(response, &TaskResponseModel)
	if err != nil {
		log.Errorf("error unmarshalling remote task %s", err)
		return nil, guru_error.GetInternalError(consts.RegisterPCOpName)
	}
	return TaskResponseModel.Data.CompletionDetails, nil
}

func GetRemoteTask(taskExtId string, remoteHost string, operation string) ([]byte, guru_error.GuruErrorInterface) {
	url := fmt.Sprintf("%s/%s", consts.TasksUrl, taskExtId)
	log.Infof("Polling ergon task %s retry number", taskExtId)
	response, responseCode, err := external.Interfaces().RemoteRestClient().CallApi(remoteHost, consts.EnvoyPort,
		url, http.MethodGet, nil, nil, nil, nil, nil, nil, true)
	if err != nil {
		return nil, GetErrorFromStatusCode(responseCode, err, operation)
	}
	return response, nil
}

func GetIpAddressOrFqdn(clusterNetworkDetails *common_config.IPAddressOrFQDN) string {

	if clusterNetworkDetails == nil {
		return ""
	}
	if clusterNetworkDetails.GetIpv4() != nil ||
		clusterNetworkDetails.GetIpv4().GetValue() != "" {
		return clusterNetworkDetails.GetIpv4().GetValue()
	}
	if clusterNetworkDetails.GetIpv6() != nil ||
		clusterNetworkDetails.GetIpv6().GetValue() != "" {
		return clusterNetworkDetails.GetIpv6().GetValue()
	}
	if clusterNetworkDetails.GetFqdn() != nil ||
		clusterNetworkDetails.GetFqdn().GetValue() != "" {
		return clusterNetworkDetails.GetFqdn().GetValue()
	}
	return ""
}

func CalculateTimeDiffFromNow(startTime string) uint64 {
	operationEndTime := time.Now()
	operationStartTime, _ := time.Parse(consts.DateTimeFormat, startTime)
	return uint64((operationEndTime.Sub(operationStartTime)).Seconds())
}

func GetClusterEntityTypeFromMessage(clusterEntityType management.ClusterTypeMessage_ClusterType) dto.ClusterType {
	switch clusterEntityType {
	case management.ClusterTypeMessage_DOMAIN_MANAGER:
		return dto.CLUSTERTYPE_DOMAIN_MANAGER
	case management.ClusterTypeMessage_AOS:
		return dto.CLUSTERTYPE_AOS
	}
	return dto.CLUSTERTYPE_UNKNOWN
}

func PrepareGroupsRequestPayload(entityName string, entityIds []string,
	attributes []string, sortAttribute *string, sortOrder *string, filterCriteria *string,
	query *string) *common_models.GroupsGetEntitiesRequest {

	attributesList := make([]*common_models.GroupsRequestedAttribute, 0)
	for _, attribute := range attributes {
		attributesList = append(attributesList, &common_models.GroupsRequestedAttribute{
			Attribute: attribute,
		})
	}

	request := &common_models.GroupsGetEntitiesRequest{
		EntityType:               entityName,
		GroupMemberAttributes:    &attributesList,
		GroupMemberSortAttribute: sortAttribute,
		GroupMemberSortOrder:     (*common_models.GroupsSortOrder)(sortOrder),
		FilterCriteria:           filterCriteria,
		QueryName:                query,
	}
	if entityIds != nil {
		request.EntityIds = &entityIds
	}
	return request
}

func ToTitleCase(s string) string {
	// Split the string by underscores
	words := strings.Split(s, "_")

	// Convert each word to title case
	for i, word := range words {
		if len(word) > 0 {
			words[i] = string(unicode.ToUpper(rune(word[0]))) + strings.ToLower(word[1:])
		}
	}

	// Join the words with spaces
	return strings.Join(words, " ")
}

func IsDomainManager(remoteClusterUUID string) bool {
	isDomainManager, err := external.Interfaces().ZkClient().ExistZkNode(
		fmt.Sprintf("%s/%s", consts.DomainManagerCES, remoteClusterUUID), true)
	if err != nil {
		log.Errorf("error checking for zk node %s: %v", consts.DomainManagerCES, err)
		return false
	}
	return isDomainManager
}

// GetNodeListFromZeusConfig() returns the list of node uuids from the zeus config
func GetNodeListFromZeusConfig(zeusConfig *zeus_config.ConfigurationProto) []string {
	vmUuidList := []string{}
	for _, node := range zeusConfig.GetNodeList() {
		vmUuidList = append(vmUuidList, node.GetUhuraUvmUuid())
	}
	return vmUuidList
}
