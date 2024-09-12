package utils

import (
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/utils"

	"github.com/nutanix-core/go-cache/ergon"

	log "github.com/sirupsen/logrus"
)

func PostStepSuccess(job *models.Job, message string) {
	log.Infof("[%s] %s - %s", consts.OperationNameMap[job.Name], job.JobMetadata.ContextId, message)
	job.JobMetadata.StepsCompleted += 1
	utils.UpdateJobTask(job, nil, getJobCompletePercentage(job, consts.OperationStepsMap[job.Name]), message)
}

func PostJobSuccess(job *models.Job, message string) {
	log.Infof("[%s] %s - %s", job.JobMetadata.ContextId, consts.OperationNameMap[job.Name], message)
	job.JobMetadata.StepsCompleted += 1
	utils.UpdateJobTask(job, ergon.Task_kSucceeded.Enum(), nil, message)
	utils.PulseEvent(job, true)
}

func PostJobFailure(job *models.Job, err error, message string) {
	log.Errorf("[%s] %s - %s", consts.OperationNameMap[job.Name], job.JobMetadata.ContextId, message)
	utils.UpdateJobTaskStatusOnError(job, err, message)
	utils.PulseEvent(job, false)
}

func getJobCompletePercentage(job *models.Job, totalSteps int64) *int32 {
	percent := int32(float32(job.JobMetadata.StepsCompleted+1) / float32(totalSteps+1) * 100)
	return &percent
}
