package exchange_root_certificate

import (
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"sync"

	log "github.com/sirupsen/logrus"
)

var (
	taskManagerInstance *certificateBroadcastTaskQueue
	taskManagerOnce     sync.Once
)

type BroadcastTaskType int

const (
	NEW_PC BroadcastTaskType = iota + 1
	REMOVE_PC
	NEW_PE
	BROADCAST_ALL
)

type BroadcastTask interface {
	Execute()
	GetTaskType() BroadcastTaskType
	GetClusterUuid() string                      // Returns the cluster UUID of the remote PC or PE involved
	IsComplementaryTask(task BroadcastTask) bool // checks whether the current task is complimentary to task given in arg
	IsSimilarTask(task BroadcastTask) bool       // checks whether the current task is similar to task given in arg
}

type certificateBroadcastTaskQueue struct {
	taskList      []BroadcastTask
	taskListLock  sync.Mutex
	taskAvailable *sync.Cond
}

func GetCertificateBroadcastTaskQueue() *certificateBroadcastTaskQueue {
	taskManagerOnce.Do(func() {
		if taskManagerInstance == nil {
			taskManagerInstance = &certificateBroadcastTaskQueue{}
		}
		taskManagerInstance.taskAvailable = sync.NewCond(&taskManagerInstance.taskListLock)
	})
	return taskManagerInstance
}

func (manager *certificateBroadcastTaskQueue) Submit(task BroadcastTask) {
	manager.taskListLock.Lock()
	defer manager.taskListLock.Unlock()

	// This feature is not enabled by default
	if *consts.EnableBroadcastQueueFiltering {
		// Check for similar tasks
		for i := 0; i < len(manager.taskList); i++ {
			if manager.taskList[i].IsSimilarTask(task) {
				log.Warn("Found similar broadcast task in the queue. Ignoring this task.") // TODO: Enhance logging
				return
			}
		}

		// Check for complimentary tasks
		for i := 0; i < len(manager.taskList); i++ {
			if manager.taskList[i].IsComplementaryTask(task) {
				log.Warn("Found complimentary broadcast task in the queue. Deleting old task.") // TODO: Enhance logging
				manager.taskList = append(manager.taskList[:i], manager.taskList[i+1:]...)
				return
			}
		}
	}

	manager.taskList = append(manager.taskList, task)
	manager.taskAvailable.Signal()
}

func (manager *certificateBroadcastTaskQueue) ExecuteTasks() {
	for {
		manager.taskListLock.Lock()

		// looping to prevent spurious or broadcast wakeup
		for len(manager.taskList) == 0 {
			manager.taskAvailable.Wait()
		}

		task := manager.taskList[0]
		manager.taskList = manager.taskList[1:]

		manager.taskListLock.Unlock()

		task.Execute()
	}
}

func (manager *certificateBroadcastTaskQueue) GetQueueLength() (int) {
	manager.taskListLock.Lock()
	defer manager.taskListLock.Unlock()
	return len(manager.taskList)
}
