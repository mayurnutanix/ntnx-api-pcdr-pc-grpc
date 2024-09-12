/*
 * Copyright (c) 2023 Nutanix Inc. All rights reserved.
 * Author: harsh.seta@nutanix.com
 */

package models

type RegisterPcPulseDTO struct {
	// Common fields for all async jobs.
	IsSuccess             bool
	FailureMessage        string
	OperationDurationSecs uint64
	ErrorCode             uint64

	// Fields explicit to PC-PC registration.
	RemoteClusterId       string
	RemoteClusterVersion  string
	RemoteEntityType      string // DOMAIN_MANAGER
	RemoteEnvironmentType string // ONPREM, NTNX_CLOUD
	RemoteProviderType    string // AZURE, NTNX, AWS
	RemoteInstanceType    string // NATIVE_PROVISIONED, NTNX_PROVISIONED
	LocalEnvironmentType  string // ONPREM, NTNX_CLOUD
	LocalProviderType     string // AZURE, NTNX, AWS
	LocalInstanceType     string // NATIVE_PROVISIONED, NTNX_PROVISIONED
}

type UnregisterPulseDTO struct {
	// Common fields for all async jobs.
	IsSuccess             bool
	FailureMessage        string
	OperationDurationSecs uint64
	ErrorCode             uint64

	// Fields explicit to PC-PC registration.
	RemoteClusterId  string
	RemoteEntityType string // DOMAIN_MANAGER
}
