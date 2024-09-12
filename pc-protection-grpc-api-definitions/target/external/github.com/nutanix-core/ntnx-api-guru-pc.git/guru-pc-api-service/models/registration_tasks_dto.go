/*
 * Copyright (c) 2023 Nutanix Inc. All rights reserved.
 * Author: viraj.aute@nutanix.com
 */

package models

type RegistrationTaskUuidsDTO struct {
	registrationParentTaskUuid  []byte
	precheckTaskUuid            []byte
	csrTaskUuid                 []byte
	clusterRegistrationTaskUuid []byte
}

func (r *RegistrationTaskUuidsDTO) GetRegistrationParentTaskUuid() []byte {
	return r.registrationParentTaskUuid
}

func (r *RegistrationTaskUuidsDTO) GetPrecheckTaskUuid() []byte {
	return r.precheckTaskUuid
}

func (r *RegistrationTaskUuidsDTO) GetCSRTaskUuid() []byte {
	return r.csrTaskUuid
}

func (r *RegistrationTaskUuidsDTO) GetClusterRegistrationTaskUuid() []byte {
	return r.clusterRegistrationTaskUuid
}

func (r *RegistrationTaskUuidsDTO) SetRegistrationParentTaskUuid(taskUuid []byte) {
	r.registrationParentTaskUuid = taskUuid
}

func (r *RegistrationTaskUuidsDTO) SetPrecheckTaskUuid(taskUuid []byte) {
	r.precheckTaskUuid = taskUuid
}

func (r *RegistrationTaskUuidsDTO) SetCSRTaskUuid(taskUuid []byte) {
	r.csrTaskUuid = taskUuid
}

func (r *RegistrationTaskUuidsDTO) SetClusterRegistrationTaskUuid(taskUuid []byte) {
	r.clusterRegistrationTaskUuid = taskUuid
}
