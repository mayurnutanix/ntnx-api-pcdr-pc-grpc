/*
 * Copyright (c) 2024 Nutanix Inc. All rights reserved.
 * Author : kshitij.kumar@nutanix.com
 * Defines abstract factory implementation for all the state transition interfaces. Each interface embedding this interface
 * shall implement the methods exposed here.
 */

package base

import "ntnx-api-guru-pc/guru-pc-api-service/models"

type Workflow interface {
	// Execute to be invoked by queue service for a particular job execution, first parameter represent the current
	// execution step, second paramter represent the Job object created for a request. This should return error if current
	// step errored out, else nil.
	Execute(int, *models.Job) error
	// IsTerminalForward is invoked by queue service to find the terminal state for a forward job. This information should
	// come from respective state transition machines abstracted from queue implementation.
	IsTerminalForward(int64) bool
	// IsTerminalRollback is invoked by queue service to find the terminal state for a job that is marked for rollback.
	// This information should come from respective state transition machines abstracted from queue implementation.
	IsTerminalRollback(int64, *models.Job) bool
}
