/*
 * Copyright (c) 2023 Nutanix Inc. All rights reserved.
 * Author: harsh.seta@nutanix.com
 * This file contains the interface of register_aos proxy
 */

package registration_proxy

import (
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/services/register_aos/registration_common"
)

// Proxy
type RegistrationProxyIfc interface {
	Execute(item *models.Job) error
	TriggerRegisterOnPe(item *models.Job) (string, guru_error.GuruErrorInterface)
	Rollback(item *models.Job)
	DeleteClusterState(item *models.Job) (*bool, error)
}

type RegistrationProxy struct {
	registration_common_util registration_common.RegistrationCommonUtilIfc
}
