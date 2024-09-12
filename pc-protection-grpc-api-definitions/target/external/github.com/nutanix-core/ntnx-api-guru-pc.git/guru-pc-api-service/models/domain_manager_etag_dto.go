/*
* Copyright (c) 2024 Nutanix Inc. All rights reserved.
*
* Author: harsh.seta@nutanix.com
*
 */

package models

type DomainManagerETag struct {
	LastModifiedTimestamp uint64 
	CasValue              uint64
}
