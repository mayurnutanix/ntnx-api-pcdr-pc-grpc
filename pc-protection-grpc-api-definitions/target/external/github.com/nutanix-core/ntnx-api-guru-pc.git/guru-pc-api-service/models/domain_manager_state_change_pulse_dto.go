/*
 * Copyright (c) 2023 Nutanix Inc. All rights reserved.
 * Author: harsh.seta@nutanix.com
 */

package models

type DomainManagerStateChangePulseDTO struct {
	// Common fields for all async jobs
	IsSuccess             bool
	FailureMessage        string
	OperationDurationSecs int64
	// Field explicit to put endpoint of Domain Manager object
	StateChangeFields []string // List of PC attributes being modified
}
