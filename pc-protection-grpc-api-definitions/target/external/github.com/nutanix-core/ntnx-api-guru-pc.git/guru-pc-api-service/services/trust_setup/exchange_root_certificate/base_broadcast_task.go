package exchange_root_certificate

type BaseBroadcastTaskImpl struct {
	taskType          BroadcastTaskType
	remoteClusterUuid string
}

func (t *BaseBroadcastTaskImpl) GetTaskType() BroadcastTaskType {
	return t.taskType
}

func (t *BaseBroadcastTaskImpl) GetClusterUuid() string {
	return t.remoteClusterUuid
}

func (t *BaseBroadcastTaskImpl) IsComplementaryTask(task BroadcastTask) bool {
	if t.GetClusterUuid() == task.GetClusterUuid() {
		if t.taskType == NEW_PC && task.GetTaskType() == REMOVE_PC {
			return true
		}
		if t.taskType == REMOVE_PC && task.GetTaskType() == NEW_PC {
			return true
		}
	}
	return false
}

func (t *BaseBroadcastTaskImpl) IsSimilarTask(task BroadcastTask) bool {
	return t.GetClusterUuid() == task.GetClusterUuid() && t.GetTaskType() == task.GetTaskType()
}
