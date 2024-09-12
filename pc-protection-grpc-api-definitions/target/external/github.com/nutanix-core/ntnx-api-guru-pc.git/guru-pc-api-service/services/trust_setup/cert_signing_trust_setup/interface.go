/*
 * Copyright (c) 2024 Nutanix Inc. All rights reserved.
 * Author: harsh.seta@nutanix.com
 * This file contains interface of cert signing during registration between AOS cluster and PC
 */

package cert_signing_trust_setup

import (
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
)

// Interface for csr signing trust setup
type CertSigningTrustSetupIfc interface {
	Execute(item *models.Job) error
	InitiateCSR(item *models.Job) (*models.ClusterTrustSetupDetailsDTO, guru_error.GuruErrorInterface)
	SignCertificate(
		item *models.Job,
		body *models.ClusterTrustSetupDetailsDTO,
	) (*models.ClusterTrustSetupDetailsDTO, guru_error.GuruErrorInterface)
	UpdateCaChain(
		item *models.Job,
		body *models.ClusterTrustSetupDetailsDTO,
	) (*bool, guru_error.GuruErrorInterface)
	ResetCaChain(item *models.Job) (*bool,  guru_error.GuruErrorInterface)
	Rollback(item *models.Job)
}

type CertSigningTrustSetup struct {
}
