/*
 * Copyright (c) 2023 Nutanix Inc. All rights reserved.
 * Author: viraj.aute@nutanix.com
 * This file contains the interface of precheck for AOS cluster registration.
 */

package registration_precheck

import (
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/services/register_aos/registration_common"
)

// Precheck
type RegistrationPrecheckIfc interface {
	Execute(item *models.Job) error
	FetchPCDetails(
		item *models.Job,
	) (string, string, error)
	MakeRemotePEPrecheckCall(
		item *models.Job,
	) (*models.ClusterRegistrationPrecheckDTO, guru_error.GuruErrorInterface)
	ValidatePEPrecheckResponse(
		item *models.Job,
		precheckDetailsDTO *models.ClusterRegistrationPrecheckDTO,
	) guru_error.GuruErrorInterface
	ExecuteRemotePrecheck(
		item *models.Job,
	) guru_error.GuruErrorInterface
	MakeLocalPCPrecheckCall(
		item *models.Job,
	) (*models.ClusterRegistrationPrecheckDTO, guru_error.GuruErrorInterface)
	ValidatePCPrecheckResponse(
		item *models.Job,
		precheckDetailsDTO *models.ClusterRegistrationPrecheckDTO,
	) guru_error.GuruErrorInterface
	CheckRegistrationInProgress(
		item *models.Job) (bool, string, error)
	CheckAlreadyRegistered(
		item *models.Job) (bool, error)
	CheckScaleOutInProgress(
		item *models.Job) (bool, error)
	ExecuteLocalPrecheck(
		item *models.Job,
	) guru_error.GuruErrorInterface
}

type RegistrationPrecheck struct {
	registration_common_util registration_common.RegistrationCommonUtilIfc
}
