/*
* Copyright (c) 2024 Nutanix Inc. All rights reserved.
*
* Author: @deepanshu-nutanix
*
* This file contains the utility functions for updating the job task
 */

package utils

import (
	"encoding/json"
	"fmt"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/external"
	"ntnx-api-guru-pc/guru-pc-api-service/models"

	"github.com/golang/protobuf/proto"
	"github.com/nutanix-core/go-cache/ergon"
	log "github.com/sirupsen/logrus"
)

func UpdateJobTask(job *models.Job, status *ergon.Task_Status, percent *int32, message string) error {
	internalOpaque, err := json.Marshal(job.JobMetadata)
	if err != nil {
		log.Warnf("[%s] Failed to marshal job metadata: %s", job.JobMetadata.ContextId, err)
		return err
	}
	taskUpdateArg := ergon.TaskUpdateArg{
		Uuid:               job.ParentTask.Uuid,
		PercentageComplete: percent,
		LogicalTimestamp:   job.ParentTask.LogicalTimestamp,
		Message:            proto.String(message),
		Status:             status,
		InternalOpaque:     internalOpaque,
	}

	if message != "" {
		getRet, err := external.Interfaces().ErgonClient().TaskGet(job.ParentTask.Uuid)
		if err != nil {
			log.Warnf("[%s] %s Failed to get task %s: %s",
				job.JobMetadata.ContextId, consts.RegisterPCLoggerPrefix, job.JobMetadata.ParentTaskId, err)
		} else {
			currentSubSteps := append(getRet.GetSubSteps(), &ergon.Step{
				Description: proto.String(message),
			})
			taskUpdateArg.SubSteps = currentSubSteps
			taskUpdateArg.Message = proto.String(message)
		}
	}
	ret, err := external.Interfaces().ErgonClient().TaskUpdate(&taskUpdateArg)
	if err != nil {
		log.Warnf("[%s] %s Failed to update task %s: %s",
			job.JobMetadata.ContextId, consts.RegisterPCLoggerPrefix, job.JobMetadata.ParentTaskId, err)
		return err
	}
	job.ParentTask = ret.GetTask()
	return nil
}

func UpdateJobTaskStatusOnError(job *models.Job, taskError error, message string) {
	if len(job.JobMetadata.ErrorDetails) == 0 {
		job.JobMetadata.ErrorDetails = guru_error.GetInternalError(consts.OperationNameMap[job.Name]).GetTaskErrorDetails()
	}
	// Updating task status to failed with error details
	args := ergon.TaskUpdateArg{
		Uuid:             job.ParentTask.GetUuid(),
		LogicalTimestamp: job.ParentTask.LogicalTimestamp,
		Status:           ergon.Task_kFailed.Enum(),
		ErrorDetails:     job.JobMetadata.ErrorDetails,
		Message:          proto.String(message),
	}
	taskUpdateRet, taskErr := external.Interfaces().ErgonClient().TaskUpdateBase(&args)
	if taskErr != nil || taskUpdateRet == nil {
		log.Warnf("[%s] %s Failed to update task %s: %s",
			job.JobMetadata.ContextId, consts.RegisterPCLoggerPrefix, job.JobMetadata.ParentTaskId, taskErr)
		return
	}
	job.ParentTask = taskUpdateRet.Task
	job.JobMetadata.OperationError = taskError.Error()
}

func GetJobCompletePercentage(job *models.Job, totalSteps int64) int32 {
	return int32((job.JobMetadata.StepsCompleted + 1) / (totalSteps + 1) * 100)
}

func UpdateInternalOpaqueOfParentTask(msg string, item *models.Job) error {
	internalOpaque, err := json.Marshal(item.JobMetadata)
	if err == nil {
		external.Interfaces().ErgonService().UpdateTask(item.JobMetadata.ParentTaskId, consts.TaskStatusNone,
			msg, nil, internalOpaque, nil, nil)
	} else {
		return fmt.Errorf("failed to marshal internal opaque for %w", err)
	}

	return nil
}
