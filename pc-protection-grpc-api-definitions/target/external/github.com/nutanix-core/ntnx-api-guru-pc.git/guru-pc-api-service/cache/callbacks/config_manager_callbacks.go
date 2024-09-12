package callbacks

import (
	config_manager "ntnx-api-guru-pc/guru-pc-api-service/cache/config"
	"ntnx-api-guru-pc/guru-pc-api-service/services/register/configure_connection"
	"ntnx-api-guru-pc/guru-pc-api-service/utils"
	init_utils "ntnx-api-guru-pc/guru-pc-api-service/utils/init"

	"github.com/golang/protobuf/proto"
	"github.com/google/go-cmp/cmp"
	zk "github.com/nutanix-core/go-cache/go-zookeeper"
	zeus_config "github.com/nutanix-core/go-cache/zeus/config"
	log "github.com/sirupsen/logrus"
)

type configOnChangeAction struct {
	oldValue *zeus_config.ConfigurationProto
	newValue *zeus_config.ConfigurationProto
}

func NewConfigOnChangeAction(oldValue *zeus_config.ConfigurationProto, newValue *zeus_config.ConfigurationProto) OnChangeAction {
	return &configOnChangeAction{
		oldValue: oldValue,
		newValue: newValue,
	}
}

// main callback method called by the zk watcher
func ConfigChangeCb(conn *zk.Conn, data []byte) {
	log.Infof("Callback to update config cache")
	updatedConfig := &zeus_config.ConfigurationProto{}
	// Unmarshal the bytes to the config proto.
	err := proto.Unmarshal(data, updatedConfig)
	if err != nil {
		log.Errorf("Failed to unmarshal config object")
		return
	}

	cache, err := config_manager.GetConfigurationCache()
	if err != nil {
		log.Errorf("error fetching current zeus cache: %s", err)
		config_manager.SetConfigurationCache(updatedConfig)
		log.Infof("Successfully updated config cache")
		log.Warn("unable to trigger zeus callback workflows")
		return
	}

	oldVal := cache.GetZeusConfig()

	config_manager.SetConfigurationCache(updatedConfig)
	log.Infof("Successfully updated config cache")

	// trigger in separate go-routine to free the watcher go-routine
	go NewConfigOnChangeAction(oldVal, updatedConfig).Execute()
}

func (oca *configOnChangeAction) Execute() {
	log.Info("Triggering zeus callback workflows")

	// PC Registration External IP ReConfig Workflow
	if oca.oldValue.GetClusterExternalIp() != oca.newValue.GetClusterExternalIp() {
		log.Infof("Found new external ip for local PC. Triggering PC Registration External IP Reconfig workflow.")
		configure_connection.ReconfigureExternalIp(oca.newValue.GetClusterExternalIp())
	}

	// Check if node list has changed (it will change incase of scaleout)
	oldZeusConfigNodeUuidList := utils.GetNodeListFromZeusConfig(oca.oldValue)
	newZeusConfigNodeUuidList := utils.GetNodeListFromZeusConfig(oca.newValue)

	if !cmp.Equal(oldZeusConfigNodeUuidList, newZeusConfigNodeUuidList) {
		log.Infof("Node list has been updated. Saving the updated node list in idf.")
		// The below invocation will also take care of
		// 1. re-computing everything from different sources
		// 2. fetching additional node's data from hosting PE
		// 3. re-computing memory, cpu and data disk
		// 4. fetching container ExtIds from host PE and update local IDF.
		init_utils.SaveDomainManagerEntityInIdf()
	}

	log.Info("Successfully triggerred zeus callback workflows")
}
