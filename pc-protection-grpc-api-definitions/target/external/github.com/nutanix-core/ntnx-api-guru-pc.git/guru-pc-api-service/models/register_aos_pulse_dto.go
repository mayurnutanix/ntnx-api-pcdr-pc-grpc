/*
 * Copyright (c) 2023 Nutanix Inc. All rights reserved.
 * Author: harsh.seta@nutanix.com
 */

package models

type RegisterAosPulseDTO struct {
	// Common fields for all async jobs.
	IsSuccess             bool
	FailureMessage        string
	OperationDurationSecs uint64
	ErrorCode             uint64

	// Fields explicit to AOS cluster registration.
	RemoteClusterId      string
	RemoteClusterVersion string
	RemoteEntityType     string // Remote entity types: [AOS, DOMAIN_MANAGER]
	IsHosting            bool   // Denotes hosting relationship for: [PC-PE, PC-SMSP]
}
