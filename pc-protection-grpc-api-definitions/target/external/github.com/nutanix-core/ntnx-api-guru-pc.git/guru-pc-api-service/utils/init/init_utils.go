package init

import (
	"encoding/json"
	"fmt"
	"strconv"
	
	common_models "github.com/nutanix-core/go-cache/api/pe"
	aplos "github.com/nutanix-core/go-cache/aplos/sl_bufs"
	"github.com/nutanix-core/go-cache/ergon"
	"github.com/nutanix-core/go-cache/uhura"
	util_misc "github.com/nutanix-core/go-cache/util-go/misc"
	"github.com/nutanix-core/go-cache/util-go/uuid4"
	zeus_config "github.com/nutanix-core/go-cache/zeus/config"
	log "github.com/sirupsen/logrus"
	"google.golang.org/protobuf/proto"

	"ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/config"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/external"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/queue"
	"ntnx-api-guru-pc/guru-pc-api-service/utils"
	idf_utils "ntnx-api-guru-pc/guru-pc-api-service/utils/idf"
)

// PopulateJobQueue() fetches the tasks from ergon and enqueues the recovered jobs
func PopulateJobQueue(forwardQueue queue.Queue) {

	taskList, err := external.Interfaces().ErgonService().ListTask(
		nil, consts.GuruParentTasksList, []string{consts.GuruTaskComponent})
	if err != nil {
		log.Errorf("Failed to get ergon tasks for Guru with error : %v ", err)
		// TODO: to break / crash or retry with backoff. Ergon not responding.
	}

	log.Debugf("Fetched task list from ergon of size %d", len(taskList))

	for _, task := range taskList {
		var ergonTask *ergon.Task
		ergonTask, err := external.Interfaces().ErgonService().GetTaskByUuid(task)
		if err != nil {
			log.Errorf("Failed to fetch task status of %v with error %v",
				task, err)
			continue
		}

		if *ergonTask.Status != ergon.Task_kQueued &&
			*ergonTask.Status != ergon.Task_kRunning {
			continue
		}

		internalOpaque := ergonTask.InternalOpaque
		var jobMetadata models.JobMetadata
		jobVar, err := prepareRecoveredJob(internalOpaque, &jobMetadata, ergonTask)
		if err == nil {
			log.Infof("Enqueuing the recovered job with parent task id %s",
				uuid4.ToUuid4(jobVar.JobMetadata.ParentTaskId).String())
			log.Debugf("Enqueuing job with metadata %+v", jobVar.JobMetadata)
			forwardQueue.Enqueue(jobVar)
		} else {
			log.Errorf("Failed to unmarshal the internal opaque with error %s",
				err)
		}

	}
}

// prepareRecoveredJob() prepares the recovered job from the internal opaque of ergon task
func prepareRecoveredJob(internalOpaque []byte, jobMetadata *models.JobMetadata, task *ergon.Task) (*models.Job, error) {
	err := json.Unmarshal(internalOpaque, &jobMetadata)
	if err == nil {
		log.Infof("Recovered internal opaque %v", jobMetadata)
		credentialMap := make(map[string]string)
		credentialMap["username"] = ""
		credentialMap["password"] = ""
		jobMetadata.IsRecoveredTask = true
		jobVar := &models.Job{
			Name:          jobMetadata.Name,
			JobMetadata:   jobMetadata,
			CredentialMap: credentialMap,
			ParentTask:    task,
		}

		return jobVar, nil
	}

	taskUuid := uuid4.String(task.GetUuid())
	log.Errorf("Failed to unmarshal internal opaque for task %s", taskUuid)
	// Failing the task since current state of task is not feasible for any recovery.
	log.Errorf("Failing the task %s due to irrecoverable state", taskUuid)
	external.Interfaces().ErgonService().UpdateTask(task.GetUuid(), ergon.Task_kFailed,
		"Unable to recover task due to invalid task state", nil, nil,
		guru_error.GetInternalError("Operation").GetTaskErrorDetails(), nil)
	return nil, err
}

// RegisterAndStartIdfWatch() registers the IDF watcher and starts the IDF watch client
func RegisterAndStartIdfWatch() {

	// Register IDF watcher
	err := idf_utils.RegisterWatchClient()
	if err != nil {
		log.Errorf("Failure to register IDF watcher with error %s", err)
		return
	}

	// Register Watches
	idf_utils.RegisterWatches()

	// Start IDF watcher
	idf_utils.StartIDFWatchClient()
}

// Save domain manager entity in IDF at the time of guru service boot up, this
// is done so that GET domain manager API can directly call IDF and return the
// entity. It is necessary that domain manager entity is saved in IDF, hence we
// will fatal if this operation fails
// Domain Manager entity contains details which are fetched from
// 1) Hosting Prism Element
// 2) Zeus Configuration
// 3) Environment Zk node
// 4) Genesis RPC exposing CMSP status
func SaveDomainManagerEntityInIdf() {
	// Fetch IDF entity of Domain Manager, this is used to check the current
	// state of the Domain Manager entity in IDF
	domainManagerIdfEntity, err := idf_utils.FetchEntityFromIdf(
		consts.DomainManagerEntityName, "" /*entity id */)
	if err != nil && err != consts.ErrorIdfEntityNotFound {
		log.Fatalf("Failed to get domain manager entity from idf : %s", err)
	}

	// domainManagerDetails and domainManagerNodesInfo are used to store the
	// details in local memory.
	// Initialise the domain manager entity which is generated from the
	// yaml "prism/v4/management/management/DomainManager"
	domainManagerDetails := utils.InitialiseDomainManagerEntity()
	// Initialise the domain manager nodes info which is generated from the
	// proto file "guru-pc-api-service/models/domain_manager_nodes_info.pb.go"
	domainManagerNodesInfo := utils.InitialiseDomainManagerNodeInfo()

	// Populate the local details from the Domain Manager IDF entity if present
	if domainManagerIdfEntity != nil {
		utils.BuildDomainManagerDetailsFromIdfEntity(domainManagerIdfEntity,
			domainManagerDetails, domainManagerNodesInfo)
	}

	// Populate VM and Zeus data in in-memory domain manager entity
	err = populateVmAndZeusDetailsInDomainManager(domainManagerDetails, domainManagerNodesInfo)
	if err != nil {
		if err == consts.ErrorFetchZeusConfig || err == consts.ErrorInvalidZeusConfig {
			// If error is due to zeus config then should fatal because then PC is in bad state
			log.Fatalf("Error in zeus config details : %s", err)
		} else {
			// If error is due to failure in fetching VM details then log the error
			// as it could be due bad PE state and will be taken care of by the scheduler
			log.Errorf("failed to populate VM details : %s", err)
		}
	}

	// If environment details not present in IDF entity, populate environment
	// details in local domain manager
	if !utils.IsEnvironmentDetailsPresent(domainManagerIdfEntity) {
		log.Infof("Populating environment details in local domain manager entity")
		err = populateEnvironmentDetailsInDomainManager(domainManagerDetails)
		if err != nil {
			log.Fatalf("Failed to populate environment details : %s", err)
		}
	}

	log.Infof("State of in-memory domain manager details : %v", domainManagerDetails)
	log.Infof("State of in-memory domain manager nodes info : %v", domainManagerNodesInfo)

	// Update Domain Manager IDF entity using local domain manager details
	err = idf_utils.UpdateDomainManagerEntityInIdf(domainManagerIdfEntity,
		domainManagerDetails, domainManagerNodesInfo)
	if err != nil {
		log.Fatalf("Failed to update domain manager entity in idf : %s", err)
	}
	log.Infof("Successfully updated domain manager entity in IDF")
}

// It returns the size of the PC cluster based on the size in the zeus config
// args: zeusConfig - zeus configuration proto
// returns: size of the PC cluster
func getSizeFromZeusConfig(zeusConfig *zeus_config.ConfigurationProto) config.SizeMessage_Size {
	size := zeusConfig.GetPcClusterInfo().GetSize()
	switch size {
	case zeus_config.ConfigurationProto_PCClusterInfo_kTiny:
		return config.SizeMessage_STARTER
	case zeus_config.ConfigurationProto_PCClusterInfo_kSmall:
		return config.SizeMessage_SMALL
	case zeus_config.ConfigurationProto_PCClusterInfo_kLarge:
		return config.SizeMessage_LARGE
	case zeus_config.ConfigurationProto_PCClusterInfo_kXLarge:
		return config.SizeMessage_EXTRALARGE
	default:
		return config.SizeMessage_UNKNOWN
	}
}

// It populates details from zeus config in the in-memory domain manager entity
// args: zeusConfig - zeus configuration proto, domainManagerDetails - in-memory domain manager entity
func populateZeusConfigInDomainManagerDetails(
	zeusConfig *zeus_config.ConfigurationProto,
	domainManagerDetails *config.DomainManager) {

	domainManagerDetails.Base.ExtId = proto.String(zeusConfig.GetClusterUuid())
	if domainManagerDetails.GetConfig() != nil {
		config := domainManagerDetails.GetConfig()

		// Populating PC name
		config.Name = proto.String(zeusConfig.GetClusterName())

		// Populating version of the PC
		if config.GetBase() != nil && config.GetBase().GetBuildInfo() != nil {
			version, err := util_misc.GetMappedVersion(consts.VersionMapFilePath,
				zeusConfig.GetReleaseVersion(), "aos", "pc")
			if err != nil {
				log.Warnf("Failed to get the mapped release version %s", err)
			} else {
				config.Base.BuildInfo.Version = proto.String(version)
			}
		}

		// Populating the size of the PC
		domainManagerSize := getSizeFromZeusConfig(zeusConfig)
		config.Size = &domainManagerSize

		// Adding details about Pulse enablement based on Aegis in zeus config
		if zeusConfig.GetAegis() != nil &&
			zeusConfig.GetAegis().GetAutoSupportConfig() != nil &&
			zeusConfig.GetAegis().GetAutoSupportConfig().GetEmailAsups() != nil {
		}

	}

	// Populating the hosting PE's cluster ext id
	if zeusConfig.GetPcClusterInfo() != nil &&
		zeusConfig.GetPcClusterInfo().GetHostInfo() != nil &&
		len(zeusConfig.GetPcClusterInfo().GetHostInfo().GetPrismElementList()) > 0 {
		domainManagerDetails.HostingClusterExtId = proto.String(zeusConfig.
			GetPcClusterInfo().GetHostInfo().GetPrismElementList()[0].GetClusterUuid())
	}
}

// Fetch the domain manager VM details from the hosting cluster using a Groups
// call on the VM entity in PE IDF and saves VM and Zeus config details in the
// in-memory domain manager entity and domain manager nodes info
// args: domainManagerDetails - in-memory domain manager entity, domainManagerNodesInfo - in-memory domain manager nodes info
func populateVmAndZeusDetailsInDomainManager(
	domainManagerDetails *config.DomainManager,
	domainManagerNodesInfo *models.DomainManagerNodesInfo,
) error {

	// Fetch the zeus configuration to get the cluster uuid to get the uhura vm id
	zeusConfig, err := utils.FetchZeusConfig()
	if err != nil {
		log.Errorf("failed to get zeus configuration : %s", err)
		return consts.ErrorFetchZeusConfig
	}

	// Fetch the uhura vm uuid from the zeus configuration
	if len(zeusConfig.NodeList) == 0 {
		log.Errorf("invalid zeus configuration : %+v", zeusConfig)
		return consts.ErrorInvalidZeusConfig
	}

	// Populate the domain manager details from zeus config
	populateZeusConfigInDomainManagerDetails(zeusConfig, domainManagerDetails)

	// Fetch the trust zk node to get the hosting PE Cluster Uuid
	hostingPEId, err := utils.FetchHostingPEIdFromTrustZk()
	if err != nil {
		return fmt.Errorf("failed to get hosting pe cluster uuid : %w", err)
	}

	//Populate Hosting PE Cluster UUID in the domain manager entity, cluster uuid
	// from trust zk node is primary source of truth and zeus config is secondary
	domainManagerDetails.HostingClusterExtId = proto.String(hostingPEId)

	// Fetch the hosting PE Ip address from the trust zk node
	hostingClusterAddress, err := utils.FetchIpAddressFromTrustZkNode(hostingPEId)
	if err != nil {
		return fmt.Errorf("failed to get ip address from trust zk node : %w", err)
	}

	var uhuraVmUuids []string
	for _, node := range zeusConfig.NodeList {
		uhuraVmUuids = append(uhuraVmUuids, node.GetUhuraUvmUuid())
	}

	// Populate VM details in the in-memory domain manager entity using the Groups call
	// to the Hosting PE IDF.
	// attributes fetched: Container UUID.
	err = populateVmDetailsUsingGroupsCall(hostingClusterAddress,
		uhuraVmUuids, domainManagerDetails, domainManagerNodesInfo)
	if err != nil {
		log.Warnf("failed to fetch container id from hosting cluster IDF : %s", err)
	}

	// Populate Disk details in the in-memory domain manager entity using the Groups call
	// to the Hosting PE IDF.
	// attributes fetched: Disk size
	for _, vmUuid := range uhuraVmUuids {
		err = populateDiskDetailsUsingGroupsCall(hostingClusterAddress,
			vmUuid, domainManagerDetails, domainManagerNodesInfo)
		if err != nil {
			log.Warnf("failed to fetch disk size for vm %s from hosting cluster IDF: %s", vmUuid, err)
		}
	}

	// Populate VM details in the in-memory domain manager entity using the Uhura RPC
	// to the Hosting PE IDF.
	// attributes fetched: Num VCPUs, Memory Size, VM Name, Network ID
	err = populateVmDetailsUsingUhuraRpc(zeusConfig, domainManagerDetails, domainManagerNodesInfo)
	if err != nil {
		log.Warnf("failed to populate vm details using uhura rpc : %s", err)
	}
	utils.PopulateVmDetails(domainManagerDetails, domainManagerNodesInfo)
	log.Debugf("State of domain manager details after saving VM and Zeus details : %+v",
		domainManagerDetails)
	log.Debugf("State of domain manager node info after saving VM and Zeus details : %+v",
		domainManagerNodesInfo)

	return nil
}

// Fetch details using Uhura RPC - VM Get
// Details fetched are: Num VCPUs, Memory Size, VM Name, Network ID
// args: zeusConfig - zeus configuration proto,
//	domainManagerDetails - in-memory domain manager entity,
//	domainManagerNodesInfo - in-memory domain manager nodes info
func populateVmDetailsUsingUhuraRpc(
	zeusConfig *zeus_config.ConfigurationProto,
	domainManagerDetails *config.DomainManager,
	domainManagerNodesInfo *models.DomainManagerNodesInfo) error {

	var uhuraVmUuids [][]byte
	for index, node := range zeusConfig.NodeList {
		if node.UhuraUvmUuid == nil {
			log.Errorf("uhura Vm Uuid not present in the zeus config for node index %v", index)
			return consts.ErrorInvalidZeusConfig
		}
		uhuraVmUuid, err := uuid4.StringToUuid4(node.GetUhuraUvmUuid())
		if err != nil {
			log.Errorf("uhura Vm Uuid present in an invalid format %s with error %s",
				node.GetUhuraUvmUuid(), err)
			return consts.ErrorInvalidZeusConfig
		}
		uhuraVmUuids = append(uhuraVmUuids, uhuraVmUuid.RawBytes())
	}

	hostingPEUuid, err := uuid4.StringToUuid4(domainManagerDetails.GetHostingClusterExtId())
	if err != nil {
		log.Errorf("hosting cluster uuid present in an invalid format %s with error %s",
			domainManagerDetails.GetHostingClusterExtId(), err)
		return consts.ErrorInvalidZeusConfig
	}

	// Create a remote uhura client for the hosting cluster
	remoteUhuraClient := external.Interfaces().RemoteUhuraClient(hostingPEUuid)
	if remoteUhuraClient == nil {
		return fmt.Errorf("Skipping uhura VmGet Rpc, because client not initialised")
	}
	vmGetArg := &uhura.VmGetArg{
		VmUuidList:       uhuraVmUuids,
		IncludeNicConfig: proto.Bool(true),
	}

	vmGetRet, err := remoteUhuraClient.VmGet(vmGetArg)
	if err != nil {
		return fmt.Errorf("failed to execute Uhura rpc of VM Get on the remote : %w", err)
	}
	log.Debugf("Uhura rpc response - VmGetRet %+v", vmGetRet)
	err = processUhuraVmGetResponse(vmGetRet, domainManagerDetails, domainManagerNodesInfo)
	if err != nil {
		return fmt.Errorf("failed to process uhura Vm Get response : %w", err)
	}

	log.Infof("Successfully fetched Uhura Rpc response.")
	return nil
}

// Fetch details using Groups call on the VM entity in PE IDF
// Details fetched are: Container UUID.
// args: hostingClusterAddress, uhuraVmUuids,
//	domainManagerDetails - in-memory domain manager entity,
//	domainManagerNodesInfo - in-memory domain manager nodes info
func populateVmDetailsUsingGroupsCall(
	hostingClusterAddress string, uhuraVmUuids []string,
	domainManagerDetails *config.DomainManager,
	domainManagerNodesInfo *models.DomainManagerNodesInfo) error {

	// Prepare the payload to get the vm details
	vmDetailsRequest := utils.PrepareGroupsRequestPayload(
		consts.VmEntityType, uhuraVmUuids, []string{
			consts.VmAttributeContainerUuid,
		}, nil, nil, nil, &consts.VmGroupsQueryName,
	)

	// Make the Groups call to PE IDF to get the vm details
	response, err := idf_utils.GroupsCall(hostingClusterAddress, vmDetailsRequest)
	if err != nil {
		return err
	}

	// Process the response
	err = processVmGroupsResponse(response, uhuraVmUuids, domainManagerDetails,
		domainManagerNodesInfo)
	if err != nil {
		return fmt.Errorf("failed to process groups response : %w", err)
	}
	return nil
}

// Fetch details using Groups call on the Disk entity in PE IDF
// Details fetched are: Disk size.
// args: hostingClusterAddress - hosting cluster address,
//	uhuraVmUuid - uhura uvm uuid of a node
//	domainManagerDetails - in-memory domain manager entity,
//	domainManagerNodesInfo - in-memory domain manager nodes info
func populateDiskDetailsUsingGroupsCall(
	hostingClusterAddress string, uhuraVmUuid string,
	domainManagerDetails *config.DomainManager,
	domainManagerNodesInfo *models.DomainManagerNodesInfo) error {

	// Prepare the payload to get the vm details
	sortOrder := "DESCENDING"
	filterCriteria := fmt.Sprintf("vm==%s", uhuraVmUuid)
	vmDetailsRequest := utils.PrepareGroupsRequestPayload(
		consts.DiskEntityType, nil, []string{consts.DiskAttributeDiskCapacity},
		&consts.DiskAttributeDiskCapacity, &sortOrder, &filterCriteria, &consts.DiskGroupsQueryName,
	)

	// Make the Groups call to PE IDF to get the vm details
	response, err := idf_utils.GroupsCall(hostingClusterAddress, vmDetailsRequest)
	if err != nil {
		return err
	}

	// Process the response
	err = processDiskGroupsResponse(response, uhuraVmUuid, domainManagerDetails,
		domainManagerNodesInfo)
	if err != nil {
		return fmt.Errorf("failed to process groups response : %w", err)
	}

	return nil
}

// Fetch disk size from the groups call response and
// populate it in in-memory domain manager details and nodes info.
// args: response - Groups Response
//  uhuraVmUuid - uhura uvm uuid of a node
//  domainManagerDetails - domain manager details, 
//  domainManagerNodesInfo - domain manager nodes info,
// response - disk entities in a descending order of disk capacity
func processDiskGroupsResponse(
	response *common_models.GroupsGetEntitiesResponse,
	uhuraVmUuid string,
	domainManagerDetails *config.DomainManager,
	domainManagerNodesInfo *models.DomainManagerNodesInfo,
) error {

	// Validate the response
	if response == nil || response.GroupResults == nil || len(*response.GroupResults) == 0 ||
		(*response.GroupResults)[0].EntityResults == nil ||
		len(*((*response.GroupResults)[0]).EntityResults) == 0 ||
		(*((*response.GroupResults)[0]).EntityResults)[0].Data == nil ||
		len(*(*((*response.GroupResults)[0]).EntityResults)[0].Data) == 0 ||
		(*(*((*response.GroupResults)[0]).EntityResults)[0].Data)[0] == nil {

		log.Errorf("invalid Groups response from the hosting cluster. %+v", response)
		return consts.ErrorInvalidGroupsResponse
	}

	// Extract the entity results from the Groups results
	attribute := (*(*((*response.GroupResults)[0]).EntityResults)[0].Data)[0]
	if attribute == nil && *attribute.Name != consts.DiskAttributeDiskCapacity {
		log.Errorf("invalid Groups response from the hosting cluster. %+v", response)
		return consts.ErrorInvalidGroupsResponse
	}

	if attribute.Values == nil || len(*attribute.Values) == 0 ||
		(*attribute.Values)[0].Values == nil || len(*(*attribute.Values)[0].Values) == 0 {
		log.Errorf("invalid Groups response from the hosting cluster. %+v", response)
		return consts.ErrorInvalidGroupsResponse
	}

	value := (*(*attribute.Values)[0].Values)[0]
	diskCapacity, err := strconv.ParseInt(value, 10, 64)
	if err != nil {
		log.Warnf("failed to parse disk capacity with error - %s", err)
	} else {
		for index, nodeInfo := range domainManagerNodesInfo.GetVmDetailsList() {
			if uhuraVmUuid == nodeInfo.GetVmSpecs().GetUuid() {
				domainManagerNodesInfo.VmDetailsList[index].VmSpecs.DataDiskSizeBytes = proto.Int64(diskCapacity)
			}
		}
	}

	return nil
}

// Fetch Num VCPUs, Memory Size, VM Name, Network ID from the groups call response and
// populate it in in-memory domain manager details and nodes info.
// args:
//  response - Uhura VmGet Response
//  domainManagerDetails - domain manager details, 
//  domainManagerNodesInfo - domain manager nodes info, 
func processUhuraVmGetResponse(
	response *uhura.VmGetRet,
	domainManagerDetails *config.DomainManager,
	domainManagerNodesInfo *models.DomainManagerNodesInfo,
) error {
	// Validate the response
	if response == nil || len(response.VmInfoList) == 0 {
		log.Errorf("invalid response from the hosting cluster from the Uhura Vm Get rpc - %+v", response)
		return consts.ErrorInvalidUhuraResponse
	}

	for index, vmInfo := range response.GetVmInfoList() {
		if vmInfo == nil || vmInfo.GetConfig() == nil ||
			len(vmInfo.GetConfig().GetNicList()) == 0 || len(domainManagerNodesInfo.GetVmDetailsList()) < index+1 {

			log.Errorf("invalid response from the hosting cluster from the Uhura Vm Get rpc - %+v", response)
			return consts.ErrorInvalidUhuraResponse
		}

		memorySizeBytes := vmInfo.GetConfig().GetMemorySizeMb() * 1024 * 1024
		domainManagerNodesInfo.VmDetailsList[index].VmSpecs.MemorySizeBytes = proto.Int64(int64(memorySizeBytes))
		domainManagerNodesInfo.VmDetailsList[index].VmSpecs.NumVcpus = proto.Int64(int64(vmInfo.GetConfig().GetNumVcpus()))
		domainManagerNodesInfo.VmDetailsList[index].VmSpecs.VmName = proto.String(vmInfo.GetConfig().GetName())

		networkExtId := vmInfo.GetConfig().GetNicList()[0].GetNetworkUuid()
		domainManagerNodesInfo.NetworkExtId = proto.String(uuid4.ToUuid4(networkExtId).String())
	}
	return nil
}

// Fetch container uuid from the groups call response and
// populate it in in-memory domain manager details and nodes info.
// args: 
//  response - groups call response, 
//  domainManagerDetails - domain manager details, 
//  domainManagerNodesInfo - domain manager nodes info
func processVmGroupsResponse(
	response *common_models.GroupsGetEntitiesResponse,
	uhuraVmUuids []string,
	domainManagerDetails *config.DomainManager,
	domainManagerNodesInfo *models.DomainManagerNodesInfo,
) error {

	// Validate the response
	if response == nil || response.GroupResults == nil || len(*response.GroupResults) == 0 ||
		(*response.GroupResults)[0].EntityResults == nil ||
		len(*((*response.GroupResults)[0]).EntityResults) == 0 ||
		(*((*response.GroupResults)[0]).EntityResults)[0].Data == nil {

		log.Errorf("invalid response from the hosting cluster. %+v", response)
		return consts.ErrorInvalidGroupsResponse
	}

	// Extract the entity results from the Groups results
	entityResults := *((*response.GroupResults)[0]).EntityResults
	var vmDetailsList []*models.VmDetails
	for index, entityResult := range entityResults {
		if (entityResult.Data) == nil {
			continue
		}

		data := *entityResult.Data
		entityUuid := *entityResult.EntityId
		vmDetails := &models.VmDetails{
			VmSpecs: &models.VmSpecs{
				Uuid: proto.String(entityUuid),
			},
		}
		for _, attribute := range data {
			if attribute.Values == nil || len(*attribute.Values) == 0 ||
				(*attribute.Values)[0].Values == nil || len(*(*attribute.Values)[0].Values) == 0 {
				continue
			}
			value := (*(*attribute.Values)[0].Values)[0]
			log.Debugf("Attribute name: %s, value: %+v", *attribute.Name, value)
			switch {
			case *attribute.Name == consts.VmAttributeContainerUuid:
				vmDetails.VmSpecs.ContainerExtId = proto.String(value)
				vmDetails.VmSpecs.Uuid = proto.String(uhuraVmUuids[index])
			}
		}
		vmDetailsList = append(vmDetailsList, vmDetails)
	}
	domainManagerNodesInfo.VmDetailsList = vmDetailsList
	return nil
}

// It populated environment details in the in-memory domain manager entity,
// which is fetched from the environment zk node
// args: domainManagerDetails - in-memory domain manager entity
// returns: error if failed to fetch details from environment zk node
func populateEnvironmentDetailsInDomainManager(
	domainManagerDetails *config.DomainManager,
) error {

	// Fetch the Domain Manager details from the environment zk node
	environmentDetails, err := utils.FetchPcEnvironmentInfo()
	if err != nil {
		return fmt.Errorf("failed to fetch details from environment zk node : %w", err)
	}

	setEnvironmentDetailsInDomainManager(environmentDetails, domainManagerDetails)
	log.Infof("Successfully populated environment info")
	log.Debugf("State of local domain manager details after populating environment details : %+v",
		domainManagerDetails)
	return nil
}

// It sets the environment details in the in-memory domain manager entity from
// the environment details fetched from the environment zk node
// args: environmentDetails - environment details fetched from the environment zk node, domainManagerDetails - in-memory domain manager entity
func setEnvironmentDetailsInDomainManager(
	environmentDetails *aplos.PCEnvironmentConfig_PCEnvironmentInfo,
	domainManagerDetails *config.DomainManager) {

	environmentInfo := domainManagerDetails.GetConfig().GetBootstrapConfig().GetEnvironmentInfo()
	switch environmentDetails.GetEnvironmentType() {
	case aplos.PCEnvironmentConfig_PCEnvironmentInfo_NTNX_CLOUD:
		environmentInfo.Type = config.EnvironmentTypeMessage_NTNX_CLOUD.Enum()
	case aplos.PCEnvironmentConfig_PCEnvironmentInfo_ONPREM:
		environmentInfo.Type = config.EnvironmentTypeMessage_ONPREM.Enum()
	default:
		environmentInfo.Type = config.EnvironmentTypeMessage_UNKNOWN.Enum()
	}

	switch environmentDetails.GetCloudProviderInfo() {
	case aplos.PCEnvironmentConfig_PCEnvironmentInfo_NTNX:
		environmentInfo.ProviderType = config.ProviderTypeMessage_NTNX.Enum()
	case aplos.PCEnvironmentConfig_PCEnvironmentInfo_AZURE:
		environmentInfo.ProviderType = config.ProviderTypeMessage_AZURE.Enum()
	case aplos.PCEnvironmentConfig_PCEnvironmentInfo_AWS:
		environmentInfo.ProviderType = config.ProviderTypeMessage_AWS.Enum()
	case aplos.PCEnvironmentConfig_PCEnvironmentInfo_GCP:
		environmentInfo.ProviderType = config.ProviderTypeMessage_GCP.Enum()
	case aplos.PCEnvironmentConfig_PCEnvironmentInfo_VSPHERE:
		environmentInfo.ProviderType = config.ProviderTypeMessage_VSPHERE.Enum()
	default:
		environmentInfo.ProviderType = config.ProviderTypeMessage_UNKNOWN.Enum()
	}

	switch environmentDetails.GetInstanceType() {
	case aplos.PCEnvironmentConfig_PCEnvironmentInfo_NTNX_PROVISIONED:
		environmentInfo.ProvisioningType = config.ProvisioningTypeMessage_NTNX.Enum()
	case aplos.PCEnvironmentConfig_PCEnvironmentInfo_NATIVE_PROVISIONED:
		environmentInfo.ProvisioningType = config.ProvisioningTypeMessage_NATIVE.Enum()
	default:
		environmentInfo.ProvisioningType = config.ProvisioningTypeMessage_UNKNOWN.Enum()
	}
}
