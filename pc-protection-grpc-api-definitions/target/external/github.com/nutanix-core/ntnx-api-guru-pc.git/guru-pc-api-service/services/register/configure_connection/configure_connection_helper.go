/*
 * Copyright (c) 2024 Nutanix Inc. All rights reserved.
 *
 * Author: deepanshu.jain@nutanix.com
 *
 * This file contains functions for creating a configure connection task and a configure connection job.
 * The task is created using an external Ergon service, while the job is created based on the provided configure connection request.
 */

package configure_connection

import (
	"encoding/json"
	"fmt"
	"net/http"
	common_config "ntnx-api-guru-pc/generated-code/target/dto/models/common/v1/config"
	dto "ntnx-api-guru-pc/generated-code/target/dto/models/prism/v4/management"
	"ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/management"
	config_manager "ntnx-api-guru-pc/guru-pc-api-service/cache/config"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/external"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/services/register/pc_domain"
	"ntnx-api-guru-pc/guru-pc-api-service/utils"
	pc_utils "ntnx-api-guru-pc/guru-pc-api-service/utils"
	"time"

	"github.com/nutanix-core/go-cache/ergon"
	"github.com/nutanix-core/go-cache/insights/insights_interface"
	"github.com/nutanix-core/go-cache/insights/insights_interface/query"
	common_utils "github.com/nutanix-core/ntnx-api-guru/services/utils"
	log "github.com/sirupsen/logrus"
)

func CreateConfigureConnectionJob(
	task *ergon.Task, requestId string, args *management.ConfigureConnectionArg) *models.Job {
	remoteNodeIPList := []string{}
	remoteAddress := pc_utils.GetIpAddressOrFqdn(args.GetBody().GetRemoteCluster().GetExternalAddress())
	remoteNodeIpAddresses := args.GetBody().GetRemoteCluster().GetNodeIpAddresses().GetValue()
	for _, value := range remoteNodeIpAddresses {
		if value.GetIpv4() != nil {
			remoteNodeIPList = append(remoteNodeIPList, value.GetIpv4().GetValue())
		} else if value.GetIpv6() != nil {
			remoteNodeIPList = append(remoteNodeIPList, value.GetIpv6().GetValue())
		}
	}
	operationStartTime := time.Now().Format(consts.DateTimeFormat)
	jobMetadata := &models.JobMetadata{
		OperationStartTime:    operationStartTime,
		ParentTaskId:          task.GetUuid(),
		ContextId:             requestId,
		RemoteAddress:         remoteAddress,
		RemoteNodeIPAddresses: remoteNodeIPList,
		Rollback:              false,
		Name:                  consts.CONFIGURE_CONNECTION,
		RemoteClusterName:     args.GetBody().GetRemoteCluster().GetName(),
		RemoteClusterId:       args.GetBody().GetRemoteCluster().GetBase().GetExtId(),
		RemoteClusterType:     dto.CLUSTERTYPE_DOMAIN_MANAGER,
	}

	configureConnectionJob := models.Job{
		JobMetadata: jobMetadata,
		Name:        consts.CONFIGURE_CONNECTION,
		ParentTask:  task,
	}
	return &configureConnectionJob
}

func CreateUnconfigureConnectionJob(task *ergon.Task, requestId string, args *management.UnconfigureConnectionArg) *models.Job {
	operationStartTime := time.Now().Format(consts.DateTimeFormat)
	jobMetadata := &models.JobMetadata{
		OperationStartTime: operationStartTime,
		ParentTaskId:       task.GetUuid(),
		ContextId:          requestId,
		Name:               consts.UNCONFIGURE_CONNECTION,
		RemoteClusterId:    args.GetBody().GetRemoteCluster().GetExtId(),
		SelfClusterId:      args.GetExtId(),
		StepsCompleted:     0,
		RemoteClusterType:  dto.CLUSTERTYPE_DOMAIN_MANAGER,
	}
	unconfigureConnectionJob := models.Job{
		Name:        consts.UNCONFIGURE_CONNECTION,
		JobMetadata: jobMetadata,
		ParentTask:  task,
	}
	return &unconfigureConnectionJob
}

// Method for checking if any DR workflows are enabled for the remote site being unregistered
func CheckDRWorkflows(remotePcUuid string) guru_error.GuruErrorInterface {
	// Fetch AZ Entity
	exist, azEntity, err := pc_domain.CheckAzByUrlExist(remotePcUuid)
	if err != nil {
		log.Errorf("error checking for AZ entity: %s", err)
		return guru_error.GetInternalError(consts.UnregisterPCOpName)
	}
	if !exist {
		return guru_error.GetResourceNotFoundError(consts.UnregisterPCOpName, remotePcUuid)
	}

	// Check for stale stretch params.
	whereClause := query.ALL(query.EQ(query.COL("availability_zone_uuid"), query.STR(azEntity.MustGetString("uuid"))))
	q, err := query.QUERY("PCRegistration:getStaleStretchParams").SELECT("role", "protection_rule_uuid").
		FROM("entity_dr_config").WHERE(whereClause).Proto()
	if err != nil {
		log.Errorf("error building query: %s", err)
		return guru_error.GetInternalError(consts.UnregisterPCOpName)
	}
	queryArg := &insights_interface.GetEntitiesWithMetricsArg{Query: q}
	results, err := external.Interfaces().DbClient().Query(queryArg)
	if err != nil && insights_interface.ErrNotFound.Equals(err) {
		log.Infof("No DR entities found for registered PC %s", remotePcUuid)
		return nil
	}
	if err != nil {
		log.Errorf("error querying data: %s", err)
		return guru_error.GetInternalError(consts.UnregisterPCOpName)
	}
	roleToEntityCountMap := make(map[string]int)
	var prUuidList []string
	for _, result := range results {
		role, err := result.GetString("role")
		if err != nil || role == "" {
			log.Errorf("error fetching role from entity: %s", err)
			return guru_error.GetInternalError(consts.UnregisterPCOpName)
		}
		if count, ok := roleToEntityCountMap[role]; ok {
			roleToEntityCountMap[role] = count + 1
		} else {
			roleToEntityCountMap[role] = 1
		}
		prUuid, err := result.GetString("protection_rule_uuid")
		if err != nil || prUuid == "" {
			log.Errorf("error fetching protection rule uuid from entity: %s", err)
			return guru_error.GetInternalError(consts.UnregisterPCOpName)
		}
		prUuidList = append(prUuidList, prUuid)
	}

	syncProtectionPolicyCount := 0
	if count, ok := roleToEntityCountMap["kPrimary"]; ok {
		syncProtectionPolicyCount += count
	}
	if count, ok := roleToEntityCountMap["kIndependent"]; ok {
		syncProtectionPolicyCount += count
	}

	if syncProtectionPolicyCount > 0 {
		return guru_error.GetPreconditionFailureError(
			fmt.Sprintf(`%d synchronously protected entities are still being unprotected 
			on Local PC-Domain. Wait for 10 minutes for unprotection to complete before 
			attempting unregistration again.`, syncProtectionPolicyCount),
			consts.UnregisterPCOpName,
		)
	}

	secondaryConfigCount := 0
	if count, ok := roleToEntityCountMap["kSecondary"]; ok {
		secondaryConfigCount += count
	}

	if secondaryConfigCount > 0 {
		return guru_error.GetPreconditionFailureError(
			fmt.Sprintf(`stale secondary stretch configuration is still present on the Local 
			PC-Domain for %d synchronously protected entities. To resolve the issue reach out 
			to Nutanix Support and reference internal KB 12219`, secondaryConfigCount),
			consts.UnregisterPCOpName,
		)
	}

	// Check for synchronous Protection rules
	whereClause = query.AND(query.AND(query.IN(query.COL("uuid"), query.STR_LIST(prUuidList...)),
		query.CONTAINS(query.COL("ordered_availability_zone_list.availability_zone_url"),
			query.STR_LIST(remotePcUuid))),
		query.CONTAINS(query.COL("recovery_point_objective_secs"), query.INT64_LIST(0)))
	q, err = query.QUERY("PCRegistration:getSyncProtectionRules").SELECT("name").
		FROM("protection_rule").WHERE(whereClause).Proto()
	if err != nil {
		log.Errorf("error building query: %s", err)
		return guru_error.GetInternalError(consts.UnregisterPCOpName)
	}
	queryArg = &insights_interface.GetEntitiesWithMetricsArg{Query: q}
	results, err = external.Interfaces().DbClient().Query(queryArg)
	if err != nil && insights_interface.ErrNotFound.Equals(err) {
		log.Infof("No protection rules found for registered PC %s", remotePcUuid)
		return nil
	}
	if err != nil {
		log.Errorf("error querying data: %s", err)
		return guru_error.GetInternalError(consts.UnregisterPCOpName)
	}
	var prNameList []string
	for _, result := range results {
		prName, err := result.GetString("name")
		if err != nil || prName == "" {
			log.Errorf("error fetching protection rule name from entity: %s", err)
			return guru_error.GetInternalError(consts.UnregisterPCOpName)
		}
		prNameList = append(prNameList, prName)
	}

	if len(prNameList) > 0 {
		return guru_error.GetPreconditionFailureError(
			fmt.Sprintf(`found %d Synchronous Protection Policies %v referring to remote 
			PC-Domain being unregistered. Modify or delete the Protection Policies before 
			attempting unregistration.`, len(prNameList), prNameList),
			consts.UnregisterPCOpName)
	}

	return nil
}

func checkIfConnectionExist(clusterUuid string) (bool, error) {
	path := fmt.Sprintf("%s/%s", consts.DomainManagerCES, clusterUuid)
	exist, _, err := external.Interfaces().ZkSession().Exist(path, true)
	if err != nil {
		log.Errorf("error checking for zknode %s: %s", path, err)
		return false, err
	}
	return exist, nil
}

// currently only used to update external IP of the remote pc on the local PC
func updateConnection(clusterUuid string, remoteAddress string) error {
	path := fmt.Sprintf("%s/%s", consts.DomainManagerCES, clusterUuid)
	ces, err := common_utils.GetClusterExternalState(external.Interfaces().ZkSession(), "update-connection", path)
	if err != nil {
		return fmt.Errorf("error fetching cluster external state for cluster %s: %s", clusterUuid, err)
	}
	ces.ConfigDetails.ExternalIp = &remoteAddress
	err = common_utils.UpdateClusterExternalState(external.Interfaces().ZkSession(), ces, "update-connection", path)
	if err != nil {
		return fmt.Errorf("error updating cluster external state for cluster %s: %s", clusterUuid, err)
	}
	return nil
}

// Triggered when the external ip of the local PC changes
// This will update the ip of local pc on all registered remote PCs
func ReconfigureExternalIp(newIp string) error {
	registeredPCs, err := pc_utils.GetRegisteredPcList()
	if err != nil {
		log.Errorf("error fetching registered pc list: %s", err)
		return err
	}
	configManager, err := config_manager.GetConfigurationCache()
	if err != nil {
		log.Errorf("error fetching zeus config: %s", err)
		return err
	}
	localClusterUuid := configManager.GetZeusConfig().GetClusterUuid()
	payload := &dto.ConnectionConfigurationSpec{
		RemoteCluster: &dto.RemoteCluster{
			ExtId: &localClusterUuid,
			ExternalAddress: &common_config.IPAddressOrFQDN{
				Ipv4: &common_config.IPv4Address{Value: &newIp},
			},
		},
	}
	var unsuccessfullClusters []string
	ipToUuidMap := make(map[string]string)
	ipToTaskMap := make(map[string]string)
	// call configure connection api
	for _, registeredPc := range registeredPCs {
		path := fmt.Sprintf("%s/%s", consts.DomainManagerCES, registeredPc)
		ces, err := common_utils.GetClusterExternalState(external.Interfaces().ZkSession(),
			"external-ip-reconfig-trigger", path)
		if err != nil {
			log.Errorf("error fetching CES for PC %s: %s", registeredPc, err)
			unsuccessfullClusters = append(unsuccessfullClusters, registeredPc)
			// should continue for other pcs
			continue
		}
		ipToUuidMap[ces.GetConfigDetails().GetExternalIp()] = registeredPc
		taskId, err := callPutConfigureConnectionApi(registeredPc, ces.GetConfigDetails().GetExternalIp(), payload)
		if err != nil {
			log.Errorf("error calling put configure connection api for cluster %s: %s", registeredPc, err)
			unsuccessfullClusters = append(unsuccessfullClusters, registeredPc)
			continue
		}
		ipToTaskMap[ces.GetConfigDetails().GetExternalIp()] = taskId
	}
	// poll task
	for ip, task := range ipToTaskMap {
		taskStatus, completed := utils.GetRemoteTaskStatus(task, ip)
		if !completed {
			log.Errorf("error polling for task %s for cluster %s", task, ipToUuidMap[ip])
			unsuccessfullClusters = append(unsuccessfullClusters, ipToUuidMap[ip])
			continue
		}
		if *taskStatus != ergon.Task_kSucceeded {
			log.Errorf("PUT configure connection task %s for cluster %s failed: %v", task, ipToUuidMap[ip], *taskStatus)
			unsuccessfullClusters = append(unsuccessfullClusters, ipToUuidMap[ip])
			continue
		}
		log.Infof("successfully reconfigured connection for cluster %s", ipToUuidMap[ip])
	}
	if len(unsuccessfullClusters) > 0 {
		return fmt.Errorf("error reconfiguring connection for clusters: %v", unsuccessfullClusters)
	}
	return nil
}

func callPutConfigureConnectionApi(clusterUuid string, remoteIp string, spec *dto.ConnectionConfigurationSpec) (string, error) {
	url := fmt.Sprintf(consts.V4ConfigureConnectionApiPath, consts.ApiVersion, clusterUuid)
	headers := make(map[string]string)
	uuid, _ := external.Interfaces().Uuid().New()
	headers[consts.NtnxRequestId] = uuid.String()
	response, responseCode, err := external.Interfaces().RemoteRestClient().CallApiCertBasedAuth(
		remoteIp, consts.EnvoyPort, url, http.MethodPost, spec, headers, nil, nil)
	if err != nil {
		return "", fmt.Errorf("error calling PUT configure connection api: %d %s", *responseCode, err)
	}

	// Unmarshal response
	taskResponse := utils.TaskResponseModel{}
	err = json.Unmarshal(response, &taskResponse)
	if err != nil {
		return "", fmt.Errorf("Failed to unmarshal task response: %s", err)
	}
	return taskResponse.Data.ExtId, nil
}
