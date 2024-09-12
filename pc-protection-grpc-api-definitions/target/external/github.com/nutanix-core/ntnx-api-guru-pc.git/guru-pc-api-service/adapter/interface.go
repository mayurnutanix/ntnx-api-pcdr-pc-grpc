package adapter

import (
	"ntnx-api-guru-pc/guru-pc-api-service/job_interfaces/base"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
)

type AdapterIfc interface {
	GetJobStateTransitionInterface(job *models.Job) base.Workflow
}

type Adapter struct {
}

func New() AdapterIfc {
	return &Adapter{}
}
