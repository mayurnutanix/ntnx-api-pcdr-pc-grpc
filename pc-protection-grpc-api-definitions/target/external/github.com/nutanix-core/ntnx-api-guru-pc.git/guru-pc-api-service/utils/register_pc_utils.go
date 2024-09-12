package utils

import (
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/external"

	"github.com/nutanix-core/go-cache/insights/insights_interface"
	"github.com/nutanix-core/go-cache/prism"
	zeus_config "github.com/nutanix-core/go-cache/zeus/config"
	common_consts "github.com/nutanix-core/ntnx-api-guru/services/constants"
	"github.com/nutanix-core/ntnx-api-guru/services/idf"
	common_utils "github.com/nutanix-core/ntnx-api-guru/services/utils"
	log "github.com/sirupsen/logrus"
)

// Function to create a Cluster external state from Remote connection
func MigrateAllRemoteConnectionToPcClusterExternalState() error {

	idfSvc := external.Interfaces().IdfSvc()

	// Fetch RC entity
	getRemoteConnectionRet := &insights_interface.GetEntitiesRet{}
	getRemoteConnectionArg := idf.CreateIdfGetEntitiesArg(
		&consts.RemoteConnectionEntityType, nil)

	log.Debugf("Get remote connection entity Arg %+v", getRemoteConnectionArg)

	err := idfSvc.SendMsgWithTimeout(idf.GET_OPERATION, getRemoteConnectionArg,
		getRemoteConnectionRet, consts.IdfBackoffForRetries,
		common_consts.IDF_TIMEOUT)

	if err != nil {
		log.Errorf("%s Failed to fetch remote connection entities from IDF "+
			"with error %s", consts.RegisterPCLoggerPrefix, err)
		return consts.ErrorIdfRemoteConnectionGet
	}

	if getRemoteConnectionRet.Entity == nil {
		log.Infof("%s No remote connection entities exists in IDF ",
			consts.RegisterPCLoggerPrefix)
		return nil
	}

	for _, rcEntity := range getRemoteConnectionRet.Entity {

		// If remote connection entity is not of Prism Central then skip cluster
		// external state creation
		if !IsRemoteConnectionOfPrismCentral(rcEntity) {
			log.Infof("%s Remote connection entity %s is not of Prism Central"+
				" so skipping cluster external state creation",
				consts.RegisterPCLoggerPrefix, rcEntity.GetEntityGuid().GetEntityId())
			continue
		}

		// In case cluster external state is not created from remote
		// connection then guru should crash as connectivity between PCs would
		// be dependent on this
		err := AssembleAndCreateClusterExternalStateFromEntity(rcEntity)
		if err != nil {
			log.Errorf("Failed to create connection object for existing" +
				" Remote Connection entity")
		}
	}

	log.Infof("%s Migration of Remote Connection to Cluster external State completed",
		consts.RegisterPCLoggerPrefix)
	return nil
}

func IsRemoteConnectionOfPrismCentral(rcEntity *insights_interface.Entity) bool {
	for _, attribute := range rcEntity.AttributeDataMap {
		if attribute.GetName() == "remote_connection_info.cluster_function" {
			remoteClusterFunc := attribute.GetValue().GetInt64Value()
			if remoteClusterFunc == 1 {
				return true
			}
		}
	}
	return false
}

func FetchRemoteClusterUuidFromEntity(rcEntity *insights_interface.Entity) (
	string, error) {
	var remoteClusterUuid string
	for _, attribute := range rcEntity.AttributeDataMap {
		if attribute.GetName() == "remote_connection_info.cluster_uuid" {
			remoteClusterUuid = attribute.GetValue().GetStrValue()
		}
	}
	if remoteClusterUuid == "" {
		log.Errorf("%s Remote cluster uuid not present for remote"+
			" connection %v ",
			common_consts.CONNECT_CLUSTER_LOGGER_PREFIX, rcEntity)

		return "", consts.ErrorRemoteClusterUuidNotFound
	}

	return remoteClusterUuid, nil
}

func AssembleAndCreateClusterExternalStateFromEntity(
	rcEntity *insights_interface.Entity) error {

	zkSession := external.Interfaces().ZkSession()

	// Create cluster external state zk node
	pCClusterExternalStateDTO := AssemblePCClusterExternalStateEntity(rcEntity)
	log.Infof("Cluster external State DTO %v", pCClusterExternalStateDTO)
	clusterUuid := pCClusterExternalStateDTO.GetClusterUuid()
	zkPath := consts.DomainManagerCES + "/" + clusterUuid

	err := common_utils.CreateClusterExternalState(zkSession, pCClusterExternalStateDTO,
		"", zkPath)
	if err != nil {

		if err == common_consts.ErrorZkNodeExists {
			log.Infof("Cluster external state already created for %s ",
				clusterUuid)
			return nil
		}

		log.Errorf("%s Failed to create cluster external state for "+
			"PC with uuid %s at path %s and with error %s ",
			common_consts.CONNECT_CLUSTER_LOGGER_PREFIX,
			clusterUuid, zkPath, err)
		return err
	}
	return nil
}

func AssemblePCClusterExternalStateEntity(rcEntity *insights_interface.Entity) *prism.ClusterExternalState {

	clusterExternalStateDTO := new(prism.ClusterExternalState)
	clusterExternalStateDTO.ClusterDetails = new(prism.ClusterDetails)
	clusterExternalStateDTO.ConfigDetails = new(prism.ConfigDetails)
	clusterExternalStateDTO.ClusterDetails.ContactInfo = new(zeus_config.ConfigurationProto_NetworkEntity)
	isMulticluster := true
	for _, attribute := range rcEntity.AttributeDataMap {

		switch attribute.GetName() {
		case "remote_connection_info.cluster_uuid":
			remotePCClusterUuid := attribute.GetValue().GetStrValue()
			log.Debugf("Matched the attribute name - %s", attribute.GetName())
			clusterExternalStateDTO.ClusterUuid = &remotePCClusterUuid

		case "name":
			remotePCName := attribute.GetValue().GetStrValue()
			log.Debugf("Matched the attribute name - %s", attribute.GetName())
			clusterExternalStateDTO.ClusterDetails.ClusterName = &remotePCName

		// Cluster Function is an enum, when remote is a PC(multicluster)
		// it's value is 1
		case "remote_connection_info.cluster_function":
			remoteClusterFunc := attribute.GetValue().GetInt64Value()
			log.Debugf("Matched the attribute name - %s", attribute.GetName())
			if remoteClusterFunc != 1 {
				isMulticluster = false
			}
			clusterFunctionPc := uint32(remoteClusterFunc)
			clusterExternalStateDTO.ClusterDetails.ClusterFunctions = &clusterFunctionPc
			clusterExternalStateDTO.ClusterDetails.Multicluster = &isMulticluster

		// External IP are must for multi node PC, it may or may not be present for single node PC.
		// So if the address list has only 1 IP then it has to node IP and
		// if it has more than  1 IP then first IP has to be external IP
		case "remote_connection_info.node_address_list.ip":
			remotePCNodeIpList := attribute.GetValue().GetStrList().GetValueList()
			log.Debugf("Matched the attribute name - %s", attribute.GetName())
			if len(remotePCNodeIpList) > 1 {
				remoteExternalIp := remotePCNodeIpList[0]
				clusterExternalStateDTO.ConfigDetails.ExternalIp = &remoteExternalIp
				remotePCNodeIpList = remotePCNodeIpList[1:]
			}
			clusterExternalStateDTO.ClusterDetails.ContactInfo.AddressList = remotePCNodeIpList

		case "remote_connection_info.node_address_list.port":
			ports := attribute.GetValue().GetUint64List().GetValueList()
			log.Debugf("Matched the attribute name - %s", attribute.GetName())
			remotePcPort := int32(ports[0])
			clusterExternalStateDTO.ClusterDetails.ContactInfo.Port = &remotePcPort

		case "role":
			clusterExternalStateDTO.PairingRole = new(prism.ClusterExternalState_PairingRole)
			localPCRole := attribute.GetValue().GetInt64Value()
			log.Debugf("Matched the attribute name - %s", attribute.GetName())
			switch localPCRole {
			case 0:
				// INITIATOR CASE
				initiator := prism.ClusterExternalState_kPairingInitiator
				clusterExternalStateDTO.PairingRole = &initiator
			case 1:
				// ACCEPTOR CASE
				acceptor := prism.ClusterExternalState_kPairingAcceptor
				clusterExternalStateDTO.PairingRole = &acceptor
			}

		case "remote_connection_info.cluster_fqdn":
			remotePCFqdn := attribute.GetValue().GetStrValue()
			log.Debugf("Matched the attribute name - %s", attribute.GetName())
			clusterExternalStateDTO.ConfigDetails.ClusterFullyQualifiedDomainName = &remotePCFqdn
		}

	}
	log.Debugf("Cluster External State DTO %v", clusterExternalStateDTO)

	return clusterExternalStateDTO
}
