/*
 * Copyright (c) 2024 Nutanix Inc. All rights reserved.
 * Author: anmol.garg@nutanix.com
 *
 * This file contains the Implementation of Precheck interface.
 */

package register_pc

import (
	"encoding/json"
	"fmt"
	"reflect"

	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/external"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/services/register/pc_domain"
	"ntnx-api-guru-pc/guru-pc-api-service/utils"

	"github.com/nutanix-core/go-cache/ergon"
	zk "github.com/nutanix-core/go-cache/go-zookeeper"
	"github.com/nutanix-core/go-cache/util-go/misc"
	"github.com/nutanix-core/go-cache/util-go/uuid4"
	log "github.com/sirupsen/logrus"
)

/*
List of prechecks
1. Should not be xi pc
2. Duplicate entity?
3. remote reachable and credentials validation. Check v4 compat
4. Check for existing queued or running tasks
*/

const PCDRCompatibilityJsonPath = "/home/nutanix/prism/webapps/PrismGateway/WEB-INF/classes/prism_central_dr_compatibility.json"

type PCDRCompatibilityValueModel struct {
	MinVersion string `json:"minimum"`
	MaxVersion string `json:"maximum"`
	Exceptions struct {
		Versions []string `json:"versions"`
	} `json:"exceptions"`
}

type RegisterPCPrecheckImp struct {
}

type V3PrismCentralResponseModel struct {
	Resources struct {
		Version       string `json:"version"`
		ClusterUuid   string `json:"cluster_uuid"`
		PcEnvironment struct {
			EnvironmentType string `json:"environmentType"`
			ProviderType    string `json:"providerType"`
			InstanceType    string `json:"instanceType"`
		} `json:"pc_environment"`
		PCVMList []struct {
			NICList []struct {
				IPList []string `json:"ip_list"`
			} `json:"nic_list"`
		} `json:"pc_vm_list"`
		VirtualIP string `json:"virtual_ip"`
	} `json:"resources"`
}

func checkValidInitiatorPC() guru_error.GuruErrorInterface {
	result, err := utils.IsNativeCloudPc()
	if err != nil {
		log.Errorf("error checking for pc type: %s", err)
		return guru_error.GetInternalError(consts.RegisterPCOpName)
	}
	if result {
		return guru_error.GetPreconditionFailureError(consts.ErrorPCRegisterCloudPCInitiator, consts.RegisterPCOpName)
	}
	result, err = utils.IsXiPc()
	if err != nil {
		log.Errorf("error checking for pc type: %s", err)
		return guru_error.GetInternalError(consts.RegisterPCOpName)
	}
	if result {
		return guru_error.GetPreconditionFailureError(consts.ErrorPCRegisterCloudPCInitiator, consts.RegisterPCOpName)
	}
	log.Debug("Found inititaor as an onprem cloud.")
	return nil
}

// Checks if the remote PC is on a version which supports Guru service
func checkGuruVersionCompatibility(version string) guru_error.GuruErrorInterface {
	if version == "" {
		log.Errorf("remote version is empty")
		return guru_error.GetInternalError(consts.RegisterPCOpName)
	}
	if version == "master" {
		log.Info("Remote version is master. Returning true for version compatbility.")
		return nil
	}
	res := misc.CompareVersions(utils.GetPCReleaseVersion(version),
		utils.GetPCReleaseVersion(consts.MinGuruSupportedPCVersion))
	if res == -1 {
		return guru_error.GetPreconditionFailureError(
			fmt.Sprintf("Remote PC version %s not supported, try after upgrading remote PC",
				version), consts.RegisterPCOpName)
	}
	return nil
}

// Checks if the PCs being registered are version compatible. PCs are version compatible
// if the difference between their major release versions isnt greater than 2.
func checkDRVersionCompatibility(localVersion string, remoteVersion string) guru_error.GuruErrorInterface {
	if localVersion == "master" || remoteVersion == "master" {
		return nil
	}
	content, err := utils.OsReadFileFunc(PCDRCompatibilityJsonPath)
	if err != nil {
		log.Errorf("error reading PC DR compatibility json file from path %s: %s", PCDRCompatibilityJsonPath, err)
		return guru_error.GetInternalError(consts.RegisterPCOpName)
	}
	versionMap := map[string]PCDRCompatibilityValueModel{}
	err = json.Unmarshal(content, &versionMap)
	if err != nil {
		log.Errorf("error unmarshalling PC-DR Compatibility JSON: %s", err)
		return guru_error.GetInternalError(consts.RegisterPCOpName)
	}
	localVersion, err = utils.GetPCMajorReleaseVersion(localVersion)
	if err != nil {
		log.Errorf("getting local PC version: %s", err)
		return guru_error.GetInternalError(consts.RegisterPCOpName)
	}
	remoteVersion, err = utils.GetPCMajorReleaseVersion(remoteVersion)
	if err != nil {
		log.Errorf("getting remote PC version: %s", err)
		return guru_error.GetInternalError(consts.RegisterPCOpName)
	}
	if val, ok := versionMap[localVersion]; ok {
		checkForException := false
		errorMessage := ""
		res := misc.CompareVersions(remoteVersion, val.MaxVersion)
		if res == 1 {
			errorMessage = fmt.Sprintf("remote version %s is greater than the maximum supported version %s",
				remoteVersion, val.MaxVersion)
			log.Error(errorMessage)
			checkForException = true
		}
		res = misc.CompareVersions(remoteVersion, val.MinVersion)
		if res == -1 {
			errorMessage = fmt.Sprintf("remote version %s is less than the minimum required version %s",
				remoteVersion, val.MinVersion)
			log.Error(errorMessage)
			checkForException = true
		}
		if checkForException {
			for _, exceptedVersion := range val.Exceptions.Versions {
				if exceptedVersion == remoteVersion {
					log.Info("found remote version in excepted version list")
					return nil
				}
			}
			return guru_error.GetPreconditionFailureError(errorMessage, consts.RegisterPCOpName)
		}
	} else {
		// Cant find local version in map
		log.Errorf("cant find local PC version in PC DR Compatibility JSON")
		return guru_error.GetInternalError(consts.RegisterPCOpName)
	}
	return nil
}

func checkReachability(host string, username string, password string) (*V3PrismCentralResponseModel, guru_error.GuruErrorInterface) {
	// call v3/prism_central to fetch cluster uuid. Same will act as credentials validation.
	// check version also. if remote has guru enabled version.

	response, err := callPrismCentralApi(false, host, username, password)
	if err != nil {
		return nil, err
	}
	if isValidResponse(response) {
		return response, nil
	}
	return nil, guru_error.GetInternalError(consts.RegisterPCOpName)
}

func isValidResponse(response *V3PrismCentralResponseModel) bool {
	return isNotEmpty(response.Resources.ClusterUuid) &&
		isNotEmpty(response.Resources.Version) &&
		isNotEmpty(response.Resources.PcEnvironment.EnvironmentType) &&
		isNotEmpty(response.Resources.PcEnvironment.ProviderType) &&
		isNotEmpty(response.Resources.PcEnvironment.InstanceType) &&
		isNotEmpty(response.Resources.VirtualIP) &&
		isNotEmptyList(response.Resources.PCVMList) &&
		isNotEmptyList(response.Resources.PCVMList[0].NICList) &&
		isNotEmptyList(response.Resources.PCVMList[0].NICList[0].IPList)
}

func isNotEmpty(s string) bool {
	return s != ""
}

func isNotEmptyList(list interface{}) bool {
	return reflect.ValueOf(list).Len() > 0
}

func checkPcClusterExternalStateExist(uuid string) guru_error.GuruErrorInterface {
	// todo : call configureconnectionhelper checkifconnectionexist from this
	zkPath := fmt.Sprintf("%s/%s", consts.DomainManagerCES, uuid)
	exist, _, err := external.Interfaces().ZkSession().Exist(zkPath, true)
	if err != nil && err != zk.ErrNoNode {
		log.Errorf("Failed to read ZooKeeper node {%s}: %v.", zkPath, err)
		return guru_error.GetInternalError(consts.RegisterPCOpName)
	}
	if exist {
		log.Debugf("Found zknode at path %s", zkPath)
		return guru_error.GetPreconditionFailureError(consts.ErrorPCAlreadyRegistered.Error(), consts.RegisterPCOpName)
	}
	log.Debugf("zknode at path %s not found", zkPath)
	return nil
}

func checkExistingTasks() guru_error.GuruErrorInterface {
	log.Debugf("Checking for existing queued or running PC-PC Registration tasks")
	taskArg := ergon.TaskListArg{
		OperationTypeList: []string{consts.OperationTypeMap[consts.REGISTER_PC]},
		StatusList:        []ergon.Task_Status{ergon.Task_kQueued, ergon.Task_kRunning},
	}
	tasksRet, err := external.Interfaces().ErgonClient().TaskList(&taskArg)
	if err != nil {
		log.Errorf("Error fetching ergon tasks: %s", err)
		return guru_error.GetServiceUnavailableError(consts.RegisterPCOpName)
	}
	if len(tasksRet.GetTaskUuidList()) > 1 {
		log.Errorf("Found more than one running or queued PC-PC Registration tasks: %s", tasksRet.String())
		return guru_error.GetPreconditionFailureError(fmt.Sprintf("Another registration operation is in progress. Please track the task : %s",
			uuid4.String(tasksRet.GetTaskUuidList()[0])), consts.RegisterPCOpName)
	}
	log.Debugf("No existing Register Domain Manager tasks found.")
	return nil
}

func checkSelfRegistration(job *models.Job) guru_error.GuruErrorInterface {
	if job.JobMetadata.SelfClusterId == "" {
		log.Errorf("[%s] Could not find local cluster uuid in metadata", job.JobMetadata.ContextId)
		return guru_error.GetInternalError(consts.RegisterPCOpName)
	}
	if job.JobMetadata.RemoteClusterId == "" {
		log.Errorf("[%s] Could not find remote cluster uuid in metadata", job.JobMetadata.ContextId)
		return guru_error.GetInternalError(consts.RegisterPCOpName)
	}
	if job.JobMetadata.RemoteClusterId == job.JobMetadata.SelfClusterId {
		return guru_error.GetPreconditionFailureError(consts.ErrorPCSelfRegisterCheck, consts.RegisterPCOpName)
	}
	return nil
}

func (domainManagerService *RegisterPCPrecheckImp) Execute(job *models.Job) guru_error.GuruErrorInterface {
	guruErr := checkExistingTasks()
	if guruErr != nil {
		return guruErr
	}
	log.Debugf("[%s] Precheck 1: Checking for existing tasks done.", job.JobMetadata.ContextId)
	guruErr = checkValidInitiatorPC()
	if guruErr != nil {
		return guruErr
	}
	log.Debugf("[%s] Precheck 2: Checking initiator PC type done.", job.JobMetadata.ContextId)
	response, guruErr := checkReachability(job.JobMetadata.RemoteAddress,
		job.CredentialMap["username"], job.CredentialMap["password"])
	if guruErr != nil {
		return guruErr
	}

	// update remote pc meta data from prism central v3 api response
	updateRemoteClusterDetails(job, response)
	// update local pc meta data from environment info zk node
	guruErr = updateSelfClusterDetails(job)
	if guruErr != nil {
		return guruErr
	}

	log.Debugf("[%s] Precheck 3: Checking reachability of remote PC done.", job.JobMetadata.ContextId)

	guruErr = checkSelfRegistration(job)
	if guruErr != nil {
		return guruErr
	}

	log.Debugf("[%s] Precheck 4: Checking self registration done.", job.JobMetadata.ContextId)

	azExist, _, err := pc_domain.CheckAzByUrlExist(response.Resources.ClusterUuid)
	if err != nil {
		log.Errorf("[%s] Unable to check for existing AZ entity: %v", job.JobMetadata.ContextId, err)
		return guru_error.GetInternalError(consts.RegisterPCOpName)
	}
	if azExist {
		return guru_error.GetPreconditionFailureError(consts.ErrorPCAlreadyRegistered.Error(), consts.RegisterPCOpName)
	}
	log.Debugf("[%s] Precheck 5: Checking for existing AZ entity done.", job.JobMetadata.ContextId)
	guruErr = checkPcClusterExternalStateExist(response.Resources.ClusterUuid)
	if guruErr != nil {
		return guruErr
	}

	log.Debugf("[%s] Precheck 6: Checking for existing CSE Zk node done.", job.JobMetadata.ContextId)
	guruErr = checkGuruVersionCompatibility(job.JobMetadata.RemoteVersion)
	if guruErr != nil {
		return guruErr
	}
	log.Debugf("[%s] Precheck 7: Checking version compatibility done.", job.JobMetadata.ContextId)

	guruErr = checkDRVersionCompatibility(job.JobMetadata.SelfClusterVersion, job.JobMetadata.RemoteVersion)
	if guruErr != nil {
		return guruErr
	}
	log.Debugf("[%s] Precheck 8: Checking DR version compatibility done.", job.JobMetadata.ContextId)

	isUpgrading, err := utils.IsUpgradeInProgress()
	if err != nil {
		log.Errorf("[%s] PC-registration Cluster-Upgrade precheck raised error: %s", job.JobMetadata.ContextId, err)
		return guru_error.GetInternalError(consts.RegisterPCOpName)
	}
	if isUpgrading {
		return guru_error.GetPreconditionFailureError(consts.UpgradeInProgressErr, consts.RegisterPCOpName)
	}
	log.Debugf("[%s] Precheck 9: Cluster Upgrade check done.", job.JobMetadata.ContextId)

	isScalingOut, err := utils.IsScaleoutInProgress()
	if err != nil {
		log.Errorf("[%s] PC-registration Scaleout precheck raised error: %s", job.JobMetadata.ContextId, err)
		return guru_error.GetInternalError(consts.RegisterPCOpName)
	}
	if isScalingOut {
		return guru_error.GetPreconditionFailureError(consts.ErrorPCIsScaleoutInProgress.Error(), consts.RegisterPCOpName)
	}
	log.Debugf("[%s] Precheck 10: Cluster Scaleout check done.", job.JobMetadata.ContextId)

	return nil
}
