/*
 * Copyright (c) 2024 Nutanix Inc. All rights reserved.
 * Author : kshitij.kumar@nutanix.com
 * Defines Queue implementation to handle async jobs. The queue comprises of a buffered channel with configurable size,
 * number of workers and a queue interface exposing the primitive methods - enqueue, dequeue and size.
 * Implementation of this queue interface is abstracted from operation type, and is generic for any async operation. This
 * queue interface uses adapter to interact with various state transition machines.
 *
 * This queue instance is initialised by grpc_server during server init. Size of the channel and number of workers
 * are configured based on gflag. Operations such as enqueue, dequeue are thread safe in a way as Go runtime ensures
 * that at a time, only one routine will be allowed to publish to or subscribe message from channel.
 * Workers after finishing their jobs return back to the pool and keep waiting for new jobs to arrive.
 *
 * The workerRoutine function listens for messages from job channel. The Stop method closes the job channel,
 * signaling all workers to stop and waiting for them to finish using wg.Wait().
 */

package queue

import (
	"context"
	"ntnx-api-guru-pc/guru-pc-api-service/adapter"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"sync"

	"github.com/nutanix-core/go-cache/util-go/uuid4"

	log "github.com/sirupsen/logrus"
)

type (
	Queue interface {
		Enqueue(item *models.Job)
		Dequeue() (*models.Job, error)
		Size() int
	}
)

type ChannelQueue struct {
	jobs             chan *models.Job
	workerCount      int
	wg               sync.WaitGroup     // TODO: remove this.
	ctx              context.Context    //TODO: what to do with this.
	cancelFunc       context.CancelFunc // TODO: what to do with this.
	name             string             // TODO: remove.
	stop             chan struct{}
	adapterIfc       adapter.AdapterIfc
	workersRetreated bool
	workersSpawned   bool
}

func (q *ChannelQueue) Enqueue(item *models.Job) {
	// Publish item to the channel.
	q.jobs <- item
}

func (q *ChannelQueue) Size() int {
	return len(q.jobs)
}

func (q *ChannelQueue) Dequeue() (*models.Job, error) {
	item := <-q.jobs
	return item, nil
}

// StopWorkers : Stop signals all workers to stop and waits for them to finish.
func (q *ChannelQueue) StopWorkers() {
	log.Infof("Stopping the workers...")
	close(q.jobs)
	q.wg.Wait()
	q.workersRetreated = true
}

func NewChannelQueue(workerCount int, adapterIfc adapter.AdapterIfc) *ChannelQueue {
	ctx, cancel := context.WithCancel(context.Background())
	return &ChannelQueue{
		jobs:             make(chan *models.Job, *consts.QueueSize),
		ctx:              ctx,
		cancelFunc:       cancel,
		workerCount:      workerCount,
		adapterIfc:       adapterIfc,
		workersSpawned:   false,
		workersRetreated: false,
	}
}

// StartWorkers starts the worker goroutines
func (q *ChannelQueue) StartWorkers() {
	log.Infof("Starting %d workers for guru", q.workerCount)
	for i := 0; i < q.workerCount; i++ {
		q.wg.Add(1)
		log.Infof("Initiating worker routine %+v", i)
		go q.workerRoutine(i)
	}
	q.workersSpawned = true
}

// workerRoutine is the function that each worker goroutine runs
func (q *ChannelQueue) workerRoutine(workerID int) {
	defer q.wg.Done()
	for {
		select {
		case item, ok := <-q.jobs:
			if !ok {
				log.Infof("Worker %d exiting because of no jobs in queue", workerID)
				return
			}

			log.Infof("[%s] Worker %d processing job with task id %s",
				item.JobMetadata.ContextId, workerID,
				item.JobMetadata.TaskId)

			// TODO: Separate "job name" to "state transition" mapping in config.
			jobMetadata := item.JobMetadata
			log.Infof("[%s] Job metadata for current job is %v",
				item.JobMetadata.ContextId, jobMetadata)
			// TODO: replace with interface aligned with the job item.
			err := q.adapterIfc.GetJobStateTransitionInterface(item).Execute(int(item.JobMetadata.StepsCompleted), item)
			if err == nil {
				if !item.JobMetadata.Rollback {
					log.Infof("[%s] Successfully executed step %d",
						item.JobMetadata.ContextId, item.JobMetadata.StepsCompleted)
				}

				if q.adapterIfc.GetJobStateTransitionInterface(item).IsTerminalForward(item.JobMetadata.StepsCompleted) {
					log.Infof("[%s] Successfully performed all the steps for job %s with parent task uuid %s",
						item.JobMetadata.ContextId,
						consts.OperationNameMap[item.Name],
						item.JobMetadata.TaskId)
					continue
				}

				if q.adapterIfc.GetJobStateTransitionInterface(item).IsTerminalRollback(item.JobMetadata.StepsCompleted, item) {
					log.Infof("[%s] Successfully performed rollback for job"+
						" with parent task id : %s", item.JobMetadata.ContextId,
						uuid4.ToUuid4(item.JobMetadata.ParentTaskId).String())
					continue
				}

				log.Debugf("[%s] Enquing the job with incremented steps %v",
					item.JobMetadata.ContextId, item.JobMetadata)
				q.Enqueue(item)
			} else {
				log.Errorf("[%s] Error in executing current step %d with %s",
					item.JobMetadata.ContextId, item.JobMetadata.StepsCompleted, err)
				if !item.JobMetadata.Rollback {
					item.JobMetadata.Rollback = true
				} else {
					// TODO: Handle this - do we ignore this or do we retry on this failure.
					log.Errorf("[%s] Incurred failure in rollback, ignoring and NOT enqueuing job again",
						item.JobMetadata.ContextId)
					continue
				}

				log.Debugf("[%s] Enquing the job with decremented steps %v",
					item.JobMetadata.ContextId, item.JobMetadata)
				q.Enqueue(item)
			}
			log.Infof("This worker %d is done executing, returning to the pool",
				workerID)
		}
	}
}
