package exchange_root_certificate

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestGetTestType(t *testing.T){
	task := BaseBroadcastTaskImpl{
		taskType: BROADCAST_ALL,
	}
	ret := task.GetTaskType()
	assert.Equal(t, ret, BROADCAST_ALL)
}

func TestGetClusterUuid(t *testing.T){
	task := BaseBroadcastTaskImpl{
		remoteClusterUuid: "123",
	}
	ret := task.GetClusterUuid()
	assert.Equal(t, ret, "123")
}

func TestIsSimilarTaskTrue(t *testing.T){
	task1 := BaseBroadcastTaskImpl{
		remoteClusterUuid: "123",
		taskType: NEW_PC,
	}
	task2 := &newPcBroadcastTask{
		BaseBroadcastTaskImpl: BaseBroadcastTaskImpl{
			remoteClusterUuid: "123",
			taskType: NEW_PC,
		},
	}
	ret:= task1.IsSimilarTask(task2)
	assert.True(t, ret)
}

func TestIsSimilarTaskFalse(t *testing.T){
	task1 := BaseBroadcastTaskImpl{
		remoteClusterUuid: "123",
		taskType: NEW_PC,
	}
	task2 := &newPcBroadcastTask{
		BaseBroadcastTaskImpl: BaseBroadcastTaskImpl{
			remoteClusterUuid: "456",
			taskType: NEW_PC,
		},
	}
	ret:= task1.IsSimilarTask(task2)
	assert.False(t, ret)
}

func TestIsComplimentaryTaskTrue(t *testing.T){
	task1 := BaseBroadcastTaskImpl{
		remoteClusterUuid: "123",
		taskType: NEW_PC,
	}
	task2 := &newPcBroadcastTask{
		BaseBroadcastTaskImpl: BaseBroadcastTaskImpl{
			remoteClusterUuid: "123",
			taskType: REMOVE_PC,
		},
	}
	ret:= task1.IsComplementaryTask(task2)
	assert.True(t, ret)
}

func TestIsComplimentaryTaskTrue2(t *testing.T){
	task1 := BaseBroadcastTaskImpl{
		remoteClusterUuid: "123",
		taskType: REMOVE_PC,
	}
	task2 := &newPcBroadcastTask{
		BaseBroadcastTaskImpl: BaseBroadcastTaskImpl{
			remoteClusterUuid: "123",
			taskType: NEW_PC,
		},
	}
	ret:= task1.IsComplementaryTask(task2)
	assert.True(t, ret)
}

func TestIsComplimentaryTaskFalse(t *testing.T){
	task1 := BaseBroadcastTaskImpl{
		remoteClusterUuid: "456",
		taskType: NEW_PC,
	}
	task2 := &newPcBroadcastTask{
		BaseBroadcastTaskImpl: BaseBroadcastTaskImpl{
			remoteClusterUuid: "123",
			taskType: REMOVE_PC,
		},
	}
	ret:= task1.IsComplementaryTask(task2)
	assert.False(t, ret)
}