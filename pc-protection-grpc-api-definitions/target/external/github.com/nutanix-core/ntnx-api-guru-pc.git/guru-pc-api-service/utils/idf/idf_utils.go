package idf

import (
	"encoding/json"
	"errors"
	"fmt"
	"net/http"
	"net/url"
	"ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/config"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/external"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/utils"
	"time"

	common_models "github.com/nutanix-core/go-cache/api/pe"
	"github.com/nutanix-core/go-cache/insights/insights_interface"
	"github.com/nutanix-core/go-cache/util-go/misc"
	common_consts "github.com/nutanix-core/ntnx-api-guru/services/constants"
	common_utils "github.com/nutanix-core/ntnx-api-guru/services/utils"
	log "github.com/sirupsen/logrus"
	"google.golang.org/protobuf/proto"
)

type RemoteConnectionWatcherCallback struct {
	insights_interface.InsightsWatch
}

func (r *RemoteConnectionWatcherCallback) WatchCallback(fired *insights_interface.FiredWatch) error {

	log.Debugf("Received callback for remote connection watch, firedWatch %+v", fired)
	newEntity := fired.GetChangedData().GetChangedEntityData()
	callbackType, err := newEntity.WatchEntityOperation()

	if err != nil {
		log.Errorf("Get type of callback operation for entity"+
			" type %s : %v", newEntity.GetEntityGuid().GetEntityTypeName(), err)
		return err
	}

	log.Infof("Received %v callback on Remote Connection watcher", callbackType)

	switch callbackType {

	case insights_interface.EntityWatchCondition_kEntityCreate:
		return onRemoteConnectionCreate(newEntity)

	case insights_interface.EntityWatchCondition_kEntityUpdate:
		return onRemoteConnectionUpdate(newEntity)

	case insights_interface.EntityWatchCondition_kEntityDelete:
		if !*consts.EnableDeleteRCWatch {
			log.Warn("Delete Op on RC Watch is disabled. Please enable via gflag if required.")
			return nil
		}
		prevEntity := fired.GetChangedData().GetPreviousEntityData()
		// If remote connection entity is not of Prism Central then skip
		// cluster external state creation
		if !utils.IsRemoteConnectionOfPrismCentral(prevEntity) {
			log.Infof("Remote connection entity %s is not of Prism Central hence "+
				"skipping callback of remote connection update",
				prevEntity.GetEntityGuid().GetEntityId())
			return nil
		}

		clusterUuid, err := utils.FetchRemoteClusterUuidFromEntity(prevEntity)
		if err != nil {
			log.Errorf("Fetch cluster uuid with error %s", err)
			return err
		}
		return onRemoteConnectionDelete(clusterUuid)

	}
	return nil
}

func RegisterWatchClient() error {
	idfWatcher := external.Interfaces().IdfWatcher()
	log.Debugf("Watcher client : %+v", idfWatcher)

	// Register the watcher client
	for {
		err := idfWatcher.Register()
		if err != nil {
			duration := consts.IdfWatcherBackoffForRetries.Backoff()
			if duration == misc.Stop {
				return fmt.Errorf("exiting after max number of retries : %w", err)
			}
		} else {
			log.Infof("Registered IDF watch client")
			return nil
		}
	}
}

func RegisterWatches() {

	if !*consts.IsRemoteConnectionWatchEnabled {
		log.Infof("Remote connection watch is disabled")
		return
	}
	// Register watch on remote connection entity type
	log.Infof("Registering watch on remote connection entity")
	RegisterRemoteConnectionWatch(&consts.RemoteConnectionEntityType)
}

func RegisterRemoteConnectionWatch(entityName *string,
) ([]*insights_interface.Entity, error) {

	idfWatcher := external.Interfaces().IdfWatcher()
	guid := insights_interface.EntityGuid{}
	guid.EntityTypeName = entityName
	remoteConnectionCallback := &RemoteConnectionWatcherCallback{}
	watchName := consts.RemoteConnectionWatchName

	watchInfo := idfWatcher.NewEntityWatchInfo(&guid,
		watchName, true /* getCurrenetState */, true, /* return_previous_entity_state*/
		remoteConnectionCallback, nil /*filterExpression */, "" /* watchMetric */)
	listEntitiesChange, err := idfWatcher.CompositeWatchOnEntitiesOfType(watchInfo,
		true, true, true)

	if err != nil {
		log.Errorf("Register remote connection IDF watch "+
			" %s", err)
		return nil, err
	}

	log.Debugf("List of entities on which the watcher has been applied %+v",
		listEntitiesChange)

	return listEntitiesChange, nil
}

func StartIDFWatchClient() {
	log.Debugf("Starting IDF watcher")
	idfWatcher := external.Interfaces().IdfWatcher()
	go func() {
		for {
			// Start the watch
			errWatchStart := idfWatcher.Start()
			if errWatchStart == insights_interface.ErrClientAlreadyStarted {
				// Watch client already started hence exit
				log.Infof(" IDF watcher client already started")
				return
			} else if errWatchStart != nil {
				// Reregister the client and then start the watch, this is in
				// infinite loop as it is a critical function
				log.Errorf("Start IDF watcher client %s",
					errWatchStart)
				handleWatchError()
			}
		}
	}()
}

func handleWatchError() {
	idfWatcher := external.Interfaces().IdfWatcher()
	for {
		// Reregister the client, this is in infinite loop as it is a critical
		_, err := idfWatcher.Reregister()
		if err != nil {
			log.Errorf("Re-register IDF watch client %s",
				err)
			time.Sleep(1 * time.Second)
		} else {
			log.Infof("Successfully reregistered IDF watch client")
			break
		}
	}
}

func onRemoteConnectionCreate(rcEntity *insights_interface.Entity) error {
	log.Debugf("Received callback for remote connection creation %+v", rcEntity)
	// If remote connection entity is not of Prism Central then skip
	// cluster external state creation
	if !utils.IsRemoteConnectionOfPrismCentral(rcEntity) {
		log.Infof("Remote connection entity %s is not of Prism Central hence "+
			"skipping callback of remote connection creation",
			rcEntity.GetEntityGuid().GetEntityId())
		return nil
	}

	err := utils.AssembleAndCreateClusterExternalStateFromEntity(rcEntity)
	if err != nil {
		log.Errorf("Execute callback for remote connection creation %s", err)
		return err
	}
	log.Infof("Successfully executed callback for remote connection creation")
	return nil
}

func onRemoteConnectionUpdate(rcEntity *insights_interface.Entity) error {
	log.Debugf("Received callback for remote connection updation %+v", rcEntity)
	// If remote connection entity is not of Prism Central then skip
	// cluster external state creation
	if !utils.IsRemoteConnectionOfPrismCentral(rcEntity) {
		log.Infof("Remote connection entity %s is not of Prism Central hence "+
			"skipping callback of remote connection update",
			rcEntity.GetEntityGuid().GetEntityId())
		return nil
	}

	zkSession := external.Interfaces().ZkSession()
	clusterExternalStateDTO := utils.AssemblePCClusterExternalStateEntity(rcEntity)
	zkPath := consts.DomainManagerCES + "/" + *clusterExternalStateDTO.ClusterUuid
	err := common_utils.UpdateClusterExternalState(zkSession, clusterExternalStateDTO,
		"", zkPath)

	if err != nil {
		log.Errorf("%s Execute callback for remote connection "+
			" updation for path %s and with error %s ",
			common_consts.CONNECT_CLUSTER_LOGGER_PREFIX, zkPath, err)
		return err
	}

	log.Infof("Succesfully executed callback for remote connection updation")
	return nil

}

func onRemoteConnectionDelete(clusterUuid string) error {
	log.Debugf("Received callback for remote connection deletion for cluster %s",
		clusterUuid)

	zkSession := external.Interfaces().ZkSession()
	zkPath := consts.DomainManagerCES + "/" + clusterUuid

	err := common_utils.DeleteClusterExternalState(zkSession, "", zkPath)
	if err != nil {
		log.Errorf("Execute callback for remote connection deletion "+
			"  %s", err)
		return err
	}

	log.Infof("Succesfully executed callback for remote connection deletion")
	return nil
}

func GroupsCall(
	ipAddress string, payload *common_models.GroupsGetEntitiesRequest,
) (*common_models.GroupsGetEntitiesResponse, error) {

	uri := consts.GroupsUrl
	port := consts.EnvoyPort
	method := http.MethodPost
	headerParams := make(map[string]string)
	queryParams := url.Values{}
	apiClient := external.Interfaces().RemoteRestClient()

	responseBody, statusCode, err := apiClient.CallApi(
		ipAddress, port, uri, method, payload,
		headerParams, queryParams, nil, nil, nil, true)
	if err != nil {
		return &common_models.GroupsGetEntitiesResponse{}, utils.GetErrorMessageFromHttpResponse(err, statusCode)
	}

	var response common_models.GroupsGetEntitiesResponse
	err = json.Unmarshal(responseBody, &response)
	if err != nil {
		log.Errorf("Failed to unmarshal with error %s", err)
		return &common_models.GroupsGetEntitiesResponse{}, err
	}

	log.Debugf("Groups Get VM entity query Response unmarshalled %+v", response)
	log.Infof("Successfully fetched the VM details from the hosting cluster with Ip %s.", ipAddress)
	return &response, nil
}

// It fetches the entity from IDF based on the entity name and entity id.
// args: entityName: entity name, entityId: entity id
// returns: idf entity if successful
func FetchEntityFromIdf(entityName string, entityId string) (
	*insights_interface.Entity, error) {

	var entityIdPtr *string
	if entityId != "" {
		entityIdPtr = &entityId
	}
	getArg := &insights_interface.GetEntitiesArg{
		EntityGuidList: []*insights_interface.EntityGuid{
			{
				EntityTypeName: proto.String(entityName),
				EntityId:       entityIdPtr,
			},
		},
	}
	getRet := &insights_interface.GetEntitiesRet{}
	err := external.Interfaces().IdfSvc().SendMsgWithTimeout(consts.IdfGetOperation,
		getArg, getRet, consts.IdfBackoffForRetries,
		common_consts.IDF_TIMEOUT)
	if err != nil {
		if errors.Is(err, consts.ErrorIdfEntityNotFound) {
			return nil, consts.ErrorIdfEntityNotFound
		} else {
			return nil, fmt.Errorf("failed to fetch idf entity : %w", err)
		}
	}

	if len(getRet.Entity) == 0 {
		log.Infof("Entity not found in IDF")
		return nil, consts.ErrorIdfEntityNotFound
	}

	log.Infof("Successfully fetched entity %s from IDF", entityName)
	log.Debugf("Successfully fetched entity %s from IDF %+v", entityName,
		getRet.Entity[0])
	return getRet.Entity[0], nil
}

// It creates the data arg for the given name and value based on the value type.
// args: name: attribute name, value: attribute value
// returns: data arg if successful
func createDataArg(name string, value interface{}) *insights_interface.AttributeDataArg {
	dataValue := &insights_interface.DataValue{}

	switch val := value.(type) {
	case string:
		dataValue.ValueType = &insights_interface.DataValue_StrValue{
			StrValue: val,
		}
	case int32:
		dataValue.ValueType = &insights_interface.DataValue_Int64Value{
			Int64Value: int64(val),
		}
	case int64:
		dataValue.ValueType = &insights_interface.DataValue_Int64Value{
			Int64Value: val,
		}
	case bool:
		dataValue.ValueType = &insights_interface.DataValue_BoolValue{
			BoolValue: val,
		}
	case uint64:
		dataValue.ValueType = &insights_interface.DataValue_Uint64Value{
			Uint64Value: val,
		}
	case float32:
		dataValue.ValueType = &insights_interface.DataValue_FloatValue{
			FloatValue: val,
		}
	case float64:
		dataValue.ValueType = &insights_interface.DataValue_DoubleValue{
			DoubleValue: val,
		}
	case []byte:
		dataValue.ValueType = &insights_interface.DataValue_BytesValue{
			BytesValue: val,
		}
	case []string:
		dataValue.ValueType = &insights_interface.DataValue_StrList_{
			StrList: &insights_interface.DataValue_StrList{
				ValueList: val,
			},
		}
	case []int64:
		dataValue.ValueType = &insights_interface.DataValue_Int64List_{
			Int64List: &insights_interface.DataValue_Int64List{
				ValueList: val,
			},
		}
	case []bool:
		dataValue.ValueType = &insights_interface.DataValue_BoolList_{
			BoolList: &insights_interface.DataValue_BoolList{
				ValueList: val,
			},
		}
	case []uint64:
		dataValue.ValueType = &insights_interface.DataValue_Uint64List{
			Uint64List: &insights_interface.DataValue_UInt64List{
				ValueList: val,
			},
		}
	case []float32:
		dataValue.ValueType = &insights_interface.DataValue_FloatList_{
			FloatList: &insights_interface.DataValue_FloatList{
				ValueList: val,
			},
		}
	case []float64:
		dataValue.ValueType = &insights_interface.DataValue_DoubleList_{
			DoubleList: &insights_interface.DataValue_DoubleList{
				ValueList: val,
			},
		}
	case [][]byte:
		dataValue.ValueType = &insights_interface.DataValue_BytesList_{
			BytesList: &insights_interface.DataValue_BytesList{
				ValueList: val,
			},
		}
	default:
		log.Errorf("Trying to build insights field for unknown type. %v", val)
		dataValue = nil
		return nil
	}

	dataArg := &insights_interface.AttributeDataArg{
		AttributeData: &insights_interface.AttributeData{
			Name:  proto.String(name),
			Value: dataValue,
		},
	}
	return dataArg
}

// It creates the ip range from the given begin and end ip.
// args: beginIp: begin ip, endIp: end ip
// returns: ip range
func makeIpRange(beginIp string, endIp string) string {
	return beginIp + " " + endIp
}

// It creates the update entity arg from the in-memory domain manager entities
// args: domainManagerEntity: current idf domain manager entity, domainManagerDetails: domain manager details, domainManagerNodesInfo: domain manager nodes info
// returns: update entity arg
func createIdfUpdateEntityArgForDomainManager(
	domainManagerEntity *insights_interface.Entity,
	domainManagerDetails *config.DomainManager,
	domainManagerNodesInfo *models.DomainManagerNodesInfo,
) *insights_interface.UpdateEntityArg {

	var casValue uint64
	if domainManagerEntity == nil {
		casValue = 0
	} else {
		casValue = domainManagerEntity.GetCasValue()
		casValue++
	}

	var entityUuid string
	if domainManagerEntity == nil {
		entityUuid = domainManagerDetails.GetBase().GetExtId()
	} else {
		entityUuid = domainManagerEntity.GetEntityGuid().GetEntityId()
	}

	attributeDataArgList := []*insights_interface.AttributeDataArg{}
	addAttribute := func(name string, value interface{}) {
		dataArg := createDataArg(name, value)
		if dataArg == nil {
			log.Errorf("failed to create data arg for attribute %s", name)
		} else {
			attributeDataArgList = append(attributeDataArgList, dataArg)
		}
	}

	// Add nodes info attribute
	if domainManagerNodesInfo != nil {
		// "nodes_info" field expects a serialized proto message, in a bytes format.
		// the seriablized proto message is the DomainManagerNodesInfo struct.
		// "protos/domain_manager_nodes_info.proto"
		nodesInfoBytes, err := proto.Marshal(domainManagerNodesInfo)
		if err != nil {
			log.Warnf("Failed to marshal nodes info with error %s", err)
		} else {
			addAttribute(consts.DomainManagerNodesInfo, nodesInfoBytes)
		}
	} else {
		log.Warnf("Nodes info is nil hence not updated in IDF Domain Manager")
	}

	// Add environment info attributes
	envInfo := domainManagerDetails.GetConfig().GetBootstrapConfig().GetEnvironmentInfo()
	addAttribute(consts.DomainManagerEnvironmentType, envInfo.GetType().String())
	addAttribute(consts.DomainManagerProviderType, envInfo.GetProviderType().String())
	addAttribute(consts.DomainManagerInstanceType, envInfo.GetProvisioningType().String())

	// Add remaining domain manager attributes
	domainManagerConfig := domainManagerDetails.GetConfig()
	addAttribute(consts.DomainManagerExtID, domainManagerDetails.GetBase().GetExtId())
	addAttribute(consts.DomainManagerName, domainManagerConfig.GetName())
	addAttribute(consts.DomainManagerVersion, domainManagerConfig.GetBase().GetBuildInfo().GetVersion())
	addAttribute(consts.DomainManagerFlavour, domainManagerConfig.GetSize().Enum().String())
	addAttribute(consts.DomainManagerHostingClusterExtID, domainManagerDetails.GetHostingClusterExtId())

	updateEntityArg := &insights_interface.UpdateEntityArg{
		EntityGuid: &insights_interface.EntityGuid{
			EntityTypeName: proto.String(consts.DomainManagerEntityName),
			EntityId:       proto.String(entityUuid),
		},
		AttributeDataArgList: attributeDataArgList,
		CasValue:             proto.Uint64(casValue),
	}

	log.Debugf("Update entity arg for domain manager : %+v", updateEntityArg)
	return updateEntityArg
}

// It updates the domain manager entity in IDF with the given domain manager details and nodes info.
// args: domainManagerEntity: current idf domain manager entity, domainManagerDetails: domain manager details, domainManagerNodesInfo: domain manager nodes info
// returns: error if failed to update
func UpdateDomainManagerEntityInIdf(
	domainManagerEntity *insights_interface.Entity,
	domainManagerDetails *config.DomainManager,
	domainManagerNodesInfo *models.DomainManagerNodesInfo) error {

	updateRet := &insights_interface.UpdateEntityRet{}
	updateArg := createIdfUpdateEntityArgForDomainManager(domainManagerEntity,
		domainManagerDetails, domainManagerNodesInfo)

	err := external.Interfaces().IdfSvc().SendMsgWithTimeout(consts.IdfUpdateOperation,
		updateArg, updateRet, consts.IdfBackoffForRetries, common_consts.IDF_TIMEOUT)
	if err != nil {
		return fmt.Errorf("failure while updating domain manager entity : %w", err)
	}
	return nil
}

func DeleteEntityById(uuid string, entityName string) error {
	log.Debugf("Attempting to delete the entity %s with UUID: %s", entityName, uuid)
	if uuid != "" {
		entity, err := GetEntityById(uuid, entityName)
		if err != nil {
			log.Errorf("Failed to get entity with uuid %s and entityName %s", uuid, entityName)
			return err
		}
		_, err = external.Interfaces().IdfClient().DeleteEntityById(
			BuildDeleteEntityArgById(uuid, entity.GetCasValue()+1, entityName))
		if err != nil {
			log.Errorf("Failed to delete entity with uuid %s and entityName %s", uuid, entityName)
			return err
		}
	}
	return nil
}

func GetEntityById(uuid string, entityName string) (*insights_interface.Entity, error) {
	log.Debugf("Attempting to get the PC Domain entity by UUID: %s", uuid)
	getEntityArg := BuildGetEntityArgById(uuid, entityName)
	entity, err := external.Interfaces().IdfClient().GetEntityById(getEntityArg)
	if err != nil {
		log.Errorf("error while getting the %s entity by UUID: %s, error: %s", entityName, uuid, err)
		return nil, err
	}

	log.Debugf("%s entity by UUID %s - %+v", entityName, uuid, entity)
	return entity, nil
}

func BuildGetEntityArgById(Uuid string, entityName string) *insights_interface.GetEntitiesArg {
	getByIdArgs := &insights_interface.GetEntitiesArg{
		EntityGuidList: []*insights_interface.EntityGuid{
			{
				EntityTypeName: proto.String(entityName),
				EntityId:       &Uuid,
			},
		},
	}
	return getByIdArgs
}

func BuildDeleteEntityArgById(Uuid string, CasValue uint64, entityName string) *insights_interface.DeleteEntityArg {
	deleteArg := &insights_interface.DeleteEntityArg{
		EntityGuid: &insights_interface.EntityGuid{
			EntityTypeName: proto.String(entityName),
			EntityId:       &Uuid,
		},
		CasValue: &CasValue,
	}
	return deleteArg
}
