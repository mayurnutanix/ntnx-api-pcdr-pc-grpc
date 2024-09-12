package exchange_root_certificate

import (
	log "github.com/sirupsen/logrus"
)

type broadcastAllTask struct {
	BaseBroadcastTaskImpl
}

func (t *broadcastAllTask) Execute() {
	err := startAllRootCertBroadcast()
	if err != nil {
		log.Errorf("error broadcasting PC certs to registered PE clusters: %v", err)
		return
	}
	log.Infof("Completed PC cert broadcast to registered PEs")
}

// function to be called from gocron scheduler
func BroadcastAllCert() {
	task := &broadcastAllTask{
		BaseBroadcastTaskImpl: BaseBroadcastTaskImpl{
			taskType: BROADCAST_ALL,
		},
	}
	GetCertificateBroadcastTaskQueue().Submit(task)
}
