/*
* Copyright (c) 2024 Nutanix Inc. All rights reserved.
*
* Author: kshitij.kumar@nutanix.com
* This defines a job struct which will be associated with every async operation that Guru is designated to perform.
  This job construct is an uber level entity whereas job metadata attribute can be defined separately for different
  types of operations.
*/

package models

import (
	"ntnx-api-guru-pc/guru-pc-api-service/consts"

	"github.com/nutanix-core/go-cache/ergon"
)

type Job struct {
	Name          consts.OperationType // TODO: make this enum type and store somewhere else.
	JobMetadata   *JobMetadata
	CredentialMap map[string]string
	ParentTask    *ergon.Task
}
