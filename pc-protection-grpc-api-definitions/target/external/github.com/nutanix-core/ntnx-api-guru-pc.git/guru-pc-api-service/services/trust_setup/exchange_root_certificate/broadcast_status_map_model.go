package exchange_root_certificate

import (
	"encoding/json"
	"fmt"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/external"
	"ntnx-api-guru-pc/guru-pc-api-service/utils"
	"time"

	zk "github.com/nutanix-core/go-cache/go-zookeeper"
	log "github.com/sirupsen/logrus"
)

type Operation int

const (
	Post Operation = iota + 1
	Delete
)

// this model is used to write the root-certificate-broadcast progress to zk.
type broadcastStatusMap struct {
	PcUuid         string          `json:"pc_uuid"`
	LastOp         Operation       `json:"last_op"`
	LastStartTime  time.Time       `json:"last_start_time"`
	LastUpdateTime time.Time       `json:"last_update_time"` // TODO: Should we use LogicalTimestamp for sequence update?
	LastEndTime    time.Time       `json:"last_end_time"`
	CertVersion    string          `json:"cert_version"` // TODO. Unimplemented. Might want to have this individually for each registered PE
	StatusMap      map[string]bool `json:"status_map"`
}

func NewBroadcastStatusMap(pcUuid string) *broadcastStatusMap {
	return &broadcastStatusMap{
		PcUuid:    pcUuid,
		StatusMap: make(map[string]bool),
	}
}

func GetBroadcastStatusMap(pcUuid string) (*broadcastStatusMap, error) {
	statusMap := NewBroadcastStatusMap(pcUuid)
	path := BroadcastZkPath + pcUuid
	bcastData, _, err := external.Interfaces().ZkSession().Get(path, true)
	if err == zk.ErrNoNode {
		log.Infof("no already broadcasted zk found for %s", pcUuid)
		return statusMap, nil
	}
	if err != nil {
		log.Errorf("error fetching already broadcaster pe uuid from zk: %v", err)
		return nil, err
	}
	err = json.Unmarshal(bcastData, statusMap)
	if err != nil {
		log.Errorf("error unmarshalling broadcasted pe data from zk: %v", err)
		return nil, err
	}
	return statusMap, nil
}

func (bsm *broadcastStatusMap) WasBroadcasted(peUuid string) bool {
	if status, ok := bsm.StatusMap[peUuid]; ok {
		return status
	}
	log.Debugf("didnt find %s entry in already broadcasted zk", peUuid)
	return false
}

func (bsm *broadcastStatusMap) UpdatePeStatus(peUuid string, status bool) {
	bsm.StatusMap[peUuid] = status
}

func (bsm *broadcastStatusMap) RemovePeStatus(peUuid string) {
	delete(bsm.StatusMap, peUuid)
}

func (bsm *broadcastStatusMap) CommitProgressToZk(isStart bool, isComplete bool, op Operation) error {
	log.Debugf("committing broadcast progress to zk")
	if isStart && isComplete {
		return fmt.Errorf("invalid broadcast zk commit operation")
	}
	path := BroadcastZkPath + bsm.PcUuid

	// check if to delete zk if op is DELETE
	if op == Delete && isComplete && len(bsm.StatusMap) == 0 {
		err := utils.DeleteZkNode(path)
		if err != nil {
			log.Errorf("error deleting broadcast zk node")
			return err
		}
		return nil
	}
	bsm.LastUpdateTime = time.Now().UTC()
	if isStart {
		bsm.LastStartTime = bsm.LastUpdateTime
	} else if isComplete {
		bsm.LastEndTime = bsm.LastUpdateTime
	}
	bsm.LastOp = op
	data, err := json.Marshal(bsm)
	if err != nil {
		log.Errorf("error marshalling data: %v", err)
		return err
	}
	err = utils.UpdateOrCreateZkNode(data, path, true)
	if err != nil {
		log.Errorf("error updating zk node: %v", err)
		return err
	}
	log.Debugf("successfuly committed broadcast progress to zk")
	return nil
}
