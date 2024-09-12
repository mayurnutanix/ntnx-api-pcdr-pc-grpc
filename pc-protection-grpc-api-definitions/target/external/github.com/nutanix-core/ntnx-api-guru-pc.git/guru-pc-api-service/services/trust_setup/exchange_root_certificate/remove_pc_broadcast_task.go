package exchange_root_certificate

import (
	log "github.com/sirupsen/logrus"
)

type removePcBroadcastTask struct {
	BaseBroadcastTaskImpl
}

func (t *removePcBroadcastTask) Execute() {
	err := deleteCertBroadcast(t.GetClusterUuid())
	if err != nil {
		log.Errorf("error broadcasting pc %s cert to registered pe clusters: %v", t.GetClusterUuid(), err)
	}
	log.Infof("Completed delete PE cert broadcast for PC %s", t.GetClusterUuid())
}

// function to be called from unregistration workflow
func BroadcastCertRemove(remoteClusterUuid string) {
	task := &removePcBroadcastTask{
		BaseBroadcastTaskImpl: BaseBroadcastTaskImpl{
			remoteClusterUuid: remoteClusterUuid,
			taskType:          REMOVE_PC,
		},
	}
	GetCertificateBroadcastTaskQueue().Submit(task)
}
