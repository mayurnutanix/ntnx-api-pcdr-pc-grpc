package queue

import (
	"github.com/golang/mock/gomock"
	"github.com/stretchr/testify/assert"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/mocks"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"testing"
	"time"
)

func TestJobEnqueueAndDequeue(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockAdapter := mocks.NewMockAdapterIfc(ctrl)
	taskQueue := NewChannelQueue(*consts.NumWorkers, mockAdapter)
	jobItem := &models.Job{}

	// Enqueue a job
	taskQueue.Enqueue(jobItem)

	// Assert
	assert.Equal(t, 1, taskQueue.Size())

	// Dequeue a job
	dequeueItem, err := taskQueue.Dequeue()

	// Assert
	assert.Equal(t, dequeueItem, jobItem)
	assert.Zero(t, taskQueue.Size())
	assert.NoError(t, err)
}

func TestJobEnqueueWithActiveWorker(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	jobItem := &models.Job{}
	jobItem.Name = consts.REGISTER_AOS
	jobMetadata := &models.JobMetadata{}
	jobMetadata.ContextId = "1acc3e85-104c-4886-55d3-fc29951396e8"
	jobMetadata.ParentTaskId = []byte{10, 204, 62, 133, 16, 76, 72, 134, 85, 211, 252, 41, 149, 19, 150, 228}
	jobMetadata.StepsCompleted = 0
	jobMetadata.Rollback = false
	jobItem.JobMetadata = jobMetadata
	mockAdapter := mocks.NewMockAdapterIfc(ctrl)
	mockStateTransitionIfc := mocks.NewMockBaseIfc(ctrl)
	mockAdapter.EXPECT().GetJobStateTransitionInterface(jobItem).Return(mockStateTransitionIfc).AnyTimes()
	mockStateTransitionIfc.EXPECT().Execute(0, jobItem).Return(nil)
	mockStateTransitionIfc.EXPECT().IsTerminalForward(gomock.Any()).Return(true)
	taskQueue := NewChannelQueue(1, mockAdapter)
	taskQueue.StartWorkers()
	taskQueue.Enqueue(jobItem)
	// Wait for a second for workers to pick up the jobs.
	time.Sleep(100 * time.Millisecond)

	// Assert.
	assert.Zero(t, taskQueue.Size())

	// Post test steps.
	taskQueue.StopWorkers()
}

func TestRollbackJobEnqueueWithActiveWorker(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	jobItem := &models.Job{}
	jobItem.Name = consts.REGISTER_AOS
	jobMetadata := &models.JobMetadata{}
	jobMetadata.ContextId = "1acc3e85-104c-4886-55d3-fc29951396e8"
	jobMetadata.ParentTaskId = []byte{10, 204, 62, 133, 16, 76, 72, 134, 85, 211, 252, 41, 149, 19, 150, 228}
	jobMetadata.StepsCompleted = 0
	jobMetadata.Rollback = true
	jobItem.JobMetadata = jobMetadata
	err := guru_error.NewGuruErrorWithMessage("Custom error", 1000, nil)
	mockAdapter := mocks.NewMockAdapterIfc(ctrl)
	mockStateTransitionIfc := mocks.NewMockBaseIfc(ctrl)
	mockAdapter.EXPECT().GetJobStateTransitionInterface(gomock.Eq(jobItem)).Return(mockStateTransitionIfc).AnyTimes()
	mockStateTransitionIfc.EXPECT().Execute(0, gomock.Eq(jobItem)).Return(err).AnyTimes()
	taskQueue := NewChannelQueue(1, mockAdapter)
	taskQueue.StartWorkers()
	taskQueue.Enqueue(jobItem)
	// Wait for a second for workers to pick up the jobs.
	time.Sleep(100 * time.Millisecond)

	// Assert
	assert.Zero(t, taskQueue.Size())

	// Post test steps.
	taskQueue.StopWorkers()
}

func TestQueueStopWorkers(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockAdapter := mocks.NewMockAdapterIfc(ctrl)
	taskQueue := NewChannelQueue(*consts.NumWorkers, mockAdapter)
	taskQueue.StartWorkers()
	taskQueue.StopWorkers()

	// assert if all workers retreated after closing the channel.
	assert.True(t, taskQueue.workersRetreated)
}

func TestQueueStartWorkers(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockAdapter := mocks.NewMockAdapterIfc(ctrl)
	taskQueue := NewChannelQueue(*consts.NumWorkers, mockAdapter)
	taskQueue.StartWorkers()

	// assert if all workers spawned after starting.
	assert.True(t, taskQueue.workersSpawned)

	// Post test steps.
	taskQueue.StopWorkers()

	// assert if all workers retreated.
	assert.True(t, taskQueue.workersRetreated)
}
