package register

import (
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
)

type Register interface {
	RunPreActions(job *models.Job) error
	SetupTrust(job *models.Job) error
	ConfigureEntities(job *models.Job) error
	RunPostActions(job *models.Job) error
}

type Unregister interface {
	RunPreActions(job *models.Job) error
	UnconfigureEntities(job *models.Job) error
	RemoveTrust(job *models.Job) error
	RunPostActions(job *models.Job) error
}

type Precheck interface {
	Execute(job *models.Job) guru_error.GuruErrorInterface
}
