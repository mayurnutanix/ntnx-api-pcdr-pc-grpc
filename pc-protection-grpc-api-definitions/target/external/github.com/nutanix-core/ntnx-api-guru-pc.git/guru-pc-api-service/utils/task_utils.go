/*
* Copyright (c) 2024 Nutanix Inc. All rights reserved.
*
* Author: kshitij.kumar@nutanix.com
*
* This file contains utils related to ergon functionalities in Guru.
 */

package utils

import (
	"errors"
	"fmt"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/external"
	"ntnx-api-guru-pc/guru-pc-api-service/models"

	"github.com/nutanix-core/go-cache/util-go/uuid4"
	"github.com/nutanix-core/go-cache/util-slbufs/util/sl_bufs/net"

	"github.com/golang/protobuf/proto"
	"github.com/nutanix-core/go-cache/ergon"
	log "github.com/sirupsen/logrus"
)

func CreateJobTask(operation consts.OperationType, requestId string, requestContext *net.RpcRequestContext,
	clusterExtId string, isUiHiddenTask bool, parentTask []byte,
	disableAutoClusterListUpdate bool) (*ergon.Task, error) {

	taskRequestContext := CreateTaskRequestContextFromGrpcContext(requestContext)
	taskEntityIdList := []*ergon.EntityId{}
	entityId, err := GetSelfDomainManagerEntity()
	if err != nil {
		log.Warnf("[%s] Failed to get self domain manager entity with error: %s", requestId, err)
	} else {
		taskEntityIdList = append(taskEntityIdList, entityId)
	}

	taskCreateArg := ergon.TaskCreateArg{
		Component:                    proto.String(consts.GuruTaskComponent),
		OperationType:                proto.String(consts.OperationTypeMap[operation]),
		DisplayName:                  proto.String(consts.OperationNameMap[operation]),
		Message:                      proto.String(GetTaskCreationMessage(operation)),
		Status:                       ergon.Task_kQueued.Enum(),
		RequestId:                    proto.String(requestId),
		RequestContext:               &taskRequestContext,
		EntityList:                   taskEntityIdList,
		UiHiddenTask:                 proto.Bool(isUiHiddenTask),
		ParentTaskUuid:               parentTask,
		DisableAutoClusterListUpdate: proto.Bool(disableAutoClusterListUpdate),
	}

	createTaskRet, err := external.Interfaces().ErgonClient().TaskCreate(&taskCreateArg)
	if err != nil {
		log.Errorf("[%s] Failed to create task for %s with error: %s", requestId, consts.OperationTypeMap[operation], err)
		return nil, errors.New(consts.CreateErgonTaskErr)
	}

	log.Infof("[%s] Created %s task with task uuid %s",
		requestId, consts.OperationTypeMap[operation], uuid4.String(createTaskRet.GetUuid()))
	task, err := GetTask(createTaskRet.GetUuid())
	if err != nil {
		return nil, err
	}
	return task, nil
}

func GetTask(taskUuid []byte) (*ergon.Task, error) {
	log.Infof("Fetching task from ergon with task uuid: %s", uuid4.String(taskUuid))
	task, err := external.Interfaces().ErgonClient().TaskGet(taskUuid)
	if err != nil {
		log.Errorf("Error fetching task: %s", err)
		return nil, err
	}
	return task, nil
}

func GetSelfDomainManagerEntity() (*ergon.EntityId, error) {
	// TODO: @kshitij-kumar-ngt Remove once cache is available
	zeusConfig, err := FetchZeusConfig()
	if err != nil {
		log.Errorf("Failed to fetch zeus config: %s", err)
		return nil, err
	}
	return &ergon.EntityId{
		QualifiedEntityType: proto.String(consts.DomainManagerQualifiedEntityType),
		EntityName:          proto.String(zeusConfig.GetClusterName()),
		EntityId:            []byte(zeusConfig.GetClusterUuid()),
	}, nil
}

func GetTaskEntityIdForClusterEntity(entityId []byte) *ergon.EntityId {
	return &ergon.EntityId{
		QualifiedEntityType: proto.String(consts.ClusterQualifiedEntityType),
		EntityName:          proto.String(string(entityId)),
		EntityId:            entityId,
		EntityType:          ergon.EntityId_kCluster.Enum(),
	}
}

func UpdateTask(taskUUID []byte, status ergon.Task_Status, message string, completionDetails []*ergon.KeyValue) error {
	task, err := GetTask(taskUUID)
	if err != nil {
		log.Warnf("Failed to fetch task %s: %s", taskUUID, err)
		return err
	}

	args := ergon.TaskUpdateArg{
		Uuid:              taskUUID,
		LogicalTimestamp:  task.LogicalTimestamp,
		Status:            status.Enum(),
		Message:           &message,
		CompletionDetails: completionDetails,
	}
	_, err = external.Interfaces().ErgonClient().TaskUpdateBase(&args)
	if err != nil {
		log.Warnf("Failed to update task %s: %s", string(taskUUID), err)
		return err
	}
	return nil
}

func GetTaskCreationMessage(operation consts.OperationType) string {
	return fmt.Sprintf("Created %s Task", consts.OperationNameMap[operation])
}

type TaskResponseModel struct {
	Data struct {
		ExtId string `json:"extId"`
	} `json:"data"`
}

func FetchRegisteredClusterUuids() ([]string, error) {
	clusterUuids, _, err := external.Interfaces().ZkSession().Children(consts.AOSClusterCES, true)
	if err != nil {
		log.Warnf("Failed to get cluster list from zk: %s", err)
		return nil, err
	}
	return clusterUuids, nil
}

// UpdateClusterListInTask updates the cluster list in the parent task
func UpdateClusterListInTask(job *models.Job, clusterUuids []string) error {

	task, err := external.Interfaces().ErgonClient().TaskGet(job.ParentTask.Uuid)
	if err != nil {
		log.Warnf("[%s] Failed to get parent task from ergon: %s", job.JobMetadata.ContextId, err)
		return err
	}
	var clusterListBytes [][]byte
	for _, clusterUuid := range clusterUuids {
		uuid, err := uuid4.StringToUuid4(clusterUuid)
		if err != nil {
			log.Warnf("[%s] Failed to convert cluster uuid to bytes: %s", job.JobMetadata.ContextId, err)
			continue
		}
		clusterListBytes = append(clusterListBytes, uuid.RawBytes())
	}
	taskLogicalTimestamp := task.GetLogicalTimestamp() + int64(1)

	// Update cluster list in ergon task
	arg := ergon.TaskUpdateArg{
		Uuid:             task.Uuid,
		ClusterList:      clusterListBytes,
		LogicalTimestamp: proto.Int64(taskLogicalTimestamp),
	}
	_, err = external.Interfaces().ErgonClient().TaskUpdate(&arg)
	if err != nil {
		log.Warnf("[%s] Failed to update cluster list in ergon task: %s", job.JobMetadata.ContextId, err)
		return err
	}
	return nil
}
