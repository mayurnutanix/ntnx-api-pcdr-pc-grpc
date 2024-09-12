/*
* Copyright (c) 2024 Nutanix Inc. All rights reserved.
*
* Author: kshitij.kumar@nutanix.com
* This defines job metadata structure associated with a job. This struct contains all the essential attributes which will
  be consumed during a request lifetime. These can be accessed and modified across the states for an operation. Further,
  this object will also be persisted in corresponding ergon task and will act as source of truth for a request execution.
*/

package models

import (
	"ntnx-api-guru-pc/generated-code/target/dto/models/prism/v4/management"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"

	"github.com/nutanix-core/go-cache/ergon"
	"github.com/nutanix-core/go-cache/util-slbufs/util/sl_bufs/net"
)

// TODO: Keep this as base, and have separate metadata models for different entity/job types.
type JobMetadata struct {
	Name                consts.OperationType `json:"Name"`
	Rollback            bool                 `json:"Rollback"`
	StepsCompleted      int64                `json:"StepsCompleted"`
	TaskId              string               `json:"TaskId"`
	ParentTaskId        []byte               `json:"ParentTaskId"`
	StepsToTaskMap      map[int][]byte       `json:"StepsToTaskMap"`
	UseTrust            bool                 `json:"UseTrust"`
	OperationError      string               `json:"OperationError"`
	ErrorStep           int                  `json:"ErrorStep"`
	ErrorDetails        []*ergon.Error      `json:"ErrorDetails"`
	RemoteTaskId        string               `json:"RemoteTaskId"`
	IsOperationComplete bool                 `json:"IsOperationComplete"`
	ContextId           string               `json:"ContextId"`
	OperationStartTime  string               `json:"OperationStartTime"`
	ResourceExtId       string               `json:"ResourceExtId"`
	IsRecoveredTask     bool                 `json:"IsRecoveredTask"`
	// Remote cluster metadata
	RemoteClusterType     management.ClusterType `json:"RemoteClusterType"`
	RemoteClusterId       string                 `json:"RemoteClusterId"`
	RemoteVersion         string                 `json:"RemoteVersion"`
	RemoteAddress         string                 `json:"RemoteAddress"`
	RemoteClusterName     string                 `json:"RemoteClusterName"`
	RemoteEnvironmentType string                 `json:"RemoteEnvironmentType"`
	RemoteProviderType    string                 `json:"RemoteProviderType"`
	RemoteInstanceType    string                 `json:"RemoteInstanceType"`
	RemoteNodeIPAddresses []string               `json:"RemoteNodeIPAddresses"`
	// Local cluster metadata
	SelfClusterType     management.ClusterType `json:"SelfClusterType"`
	SelfClusterId       string                 `json:"SelfClusterId"`
	SelfClusterVersion  string                 `json:"SelfClusterVersion"`
	SelfExternalAddress string                 `json:"SelfExternalAddress"`
	SelfEnvironmentType string                 `json:"SelfEnvironmentType"`
	SelfProviderType    string                 `json:"SelfProviderType"`
	SelfInstanceType    string                 `json:"SelfInstanceType"`
	SelfClusterName     string                 `json:"SelfClusterName"`
	SelfNodeIPAddresses []string               `json:"SelfNodeIPAddresses"`
	RequestContext      *net.RpcRequestContext `json:"RequestContext"`
	IsLocalOnly         bool                   `json:"IsLocalOnly"`
	RootCert            *string                `json:"RootCertDict"`
}
