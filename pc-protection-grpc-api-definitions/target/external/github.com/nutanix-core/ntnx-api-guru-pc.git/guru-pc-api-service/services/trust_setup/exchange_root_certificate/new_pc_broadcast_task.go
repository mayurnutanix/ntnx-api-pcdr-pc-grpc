package exchange_root_certificate

import (
	log "github.com/sirupsen/logrus"
)

type newPcBroadcastTask struct {
	BaseBroadcastTaskImpl
}

func (bt *newPcBroadcastTask) Execute() {
	err := startRootCertBroadcast(bt.GetClusterUuid())
	if err != nil {
		log.Errorf("error broadcasting PC %s cert to registered PE clusters: %v", bt.GetClusterUuid(), err)
	}
	log.Infof("Completed PE cert broadcast for PC %s", bt.GetClusterUuid())
}

// function to be called from registration workflow
func BroadcastCert(remoteClusterUuid string) {
	task := &newPcBroadcastTask{
		BaseBroadcastTaskImpl: BaseBroadcastTaskImpl{
			remoteClusterUuid: remoteClusterUuid,
			taskType: NEW_PC,
		},
	}
	GetCertificateBroadcastTaskQueue().Submit(task)
}
