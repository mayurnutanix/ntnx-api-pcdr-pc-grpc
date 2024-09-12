/*
* Copyright (c) 2024 Nutanix Inc. All rights reserved.
* Author: viraj.aute@nutanix.com
* It contains the background job which updates the domain manager entity in IDF
 */

package background

import (
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	init_utils "ntnx-api-guru-pc/guru-pc-api-service/utils/init"
	"time"
)

type SyncDomainManagerWithIdfJob struct {
	timeInterval time.Duration
}

// NewSyncDomainManagerWithIdfJob() creates a new instance of SyncDomainManagerWithIdfJob
func NewSyncDomainManagerWithIdfJob() *SyncDomainManagerWithIdfJob {
	return &SyncDomainManagerWithIdfJob{
		timeInterval: *consts.SyncDomainManagerWithIdfJobTimeInterval,
	}
}

// Execute() triggers the func which updates the domain manager entity in IDF
func (job *SyncDomainManagerWithIdfJob) Execute() {
	init_utils.SaveDomainManagerEntityInIdf()
}

// GetTimeInterval() returns the time interval for the periodic job
func (job *SyncDomainManagerWithIdfJob) GetTimeInterval() time.Duration {
	return job.timeInterval
}
