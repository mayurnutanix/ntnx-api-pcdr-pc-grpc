package utils

import (
	"ntnx-api-guru-pc/generated-code/target/dto/models/prism/v4/management"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/external"
	"ntnx-api-guru-pc/guru-pc-api-service/models"

	common_consts "github.com/nutanix-core/ntnx-api-guru/services/constants"
	log "github.com/sirupsen/logrus"
)

func PulseEvent(job *models.Job, isSuccess bool) {
	switch job.Name {
	case consts.UNREGISTER_PC:
		UnregisterPulseEvent(job, isSuccess)
	case consts.REGISTER_PC:
		RegisterPulseEvent(job, isSuccess)
	case consts.REGISTER_AOS:
		PushPulseDataForRegisterAOS(job, isSuccess)
	default:
		log.Debugf("[%s] Pulse event not implemented for job %s",
			job.JobMetadata.ContextId, consts.OperationNameMap[job.Name])
	}
}

func RegisterPulseEvent(job *models.Job, isSuccess bool) error {
	pulseDTO := models.RegisterPcPulseDTO{
		IsSuccess:             isSuccess,
		OperationDurationSecs: CalculateTimeDiffFromNow(job.JobMetadata.OperationStartTime),
		RemoteClusterVersion:  job.JobMetadata.RemoteVersion,
		RemoteClusterId:       job.JobMetadata.RemoteClusterId,
		RemoteEntityType:      management.CLUSTERTYPE_DOMAIN_MANAGER.GetName(),
		RemoteEnvironmentType: job.JobMetadata.RemoteEnvironmentType,
		RemoteProviderType:    job.JobMetadata.RemoteProviderType,
		RemoteInstanceType:    job.JobMetadata.RemoteInstanceType,
		LocalEnvironmentType:  job.JobMetadata.SelfEnvironmentType,
		LocalProviderType:     job.JobMetadata.SelfProviderType,
		LocalInstanceType:     job.JobMetadata.SelfInstanceType,
	}
	// Add failure message and error code for failure case
	if !isSuccess {
		pulseDTO.ErrorCode, pulseDTO.FailureMessage = fetchFailureMessageFromJob(job)
	}
	// Push Pulse Event
	err := external.Interfaces().EventForwarder().PushEvent(
		&pulseDTO, consts.EventTypeRegisterDomainManager,
		job.JobMetadata.TaskId, consts.EventNamespace,
		consts.PulseDomainManagerEntity, job.JobMetadata.SelfClusterId,
	)
	if err != nil {
		log.Errorf("[%s] failed to publish pulse event with error %s",
			job.JobMetadata.ContextId, err)
		return err
	}
	log.Infof("[%s] Successfully published the register pc event to pulse DB", job.JobMetadata.ContextId)
	return nil
}

func UnregisterPulseEvent(job *models.Job, isSuccess bool) {
	pulseDTO := models.UnregisterPulseDTO{
		IsSuccess:             isSuccess,
		OperationDurationSecs: CalculateTimeDiffFromNow(job.JobMetadata.OperationStartTime),
		RemoteClusterId:       job.JobMetadata.RemoteClusterId,
		RemoteEntityType:      management.CLUSTERTYPE_DOMAIN_MANAGER.GetName(),
	}
	// Add failure message and error code for failure case
	if !isSuccess {
		pulseDTO.ErrorCode, pulseDTO.FailureMessage = fetchFailureMessageFromJob(job)
	}
	// Push Pulse Event
	err := external.Interfaces().EventForwarder().PushEvent(
		&pulseDTO, consts.EventTypeUnregister,
		job.JobMetadata.TaskId, consts.EventNamespace,
		consts.PulseDomainManagerEntity, job.JobMetadata.SelfClusterId,
	)
	if err != nil {
		log.Errorf("[%s] failed to publish pulse event with error %s",
			job.JobMetadata.ContextId, err)
		return
	}
	log.Infof("[%s] Successfully published the unregister event to pulse DB", job.JobMetadata.ContextId)
}

func PushPulseDataForRegisterAOS(job *models.Job, isSuccess bool) error {
	// Push the pulse data
	pulseDTO := models.RegisterAosPulseDTO{
		IsSuccess:             isSuccess,
		OperationDurationSecs: CalculateTimeDiffFromNow(job.JobMetadata.OperationStartTime),
		IsHosting:             job.JobMetadata.UseTrust,
		RemoteClusterVersion:  job.JobMetadata.RemoteVersion,
		RemoteClusterId:       job.JobMetadata.RemoteClusterId,
		RemoteEntityType:      management.CLUSTERTYPE_AOS.GetName(),
	}
	// Add failure message and error code for failure case
	if !isSuccess {
		pulseDTO.ErrorCode, pulseDTO.FailureMessage = fetchFailureMessageFromJob(job)
	}
	err := external.Interfaces().EventForwarder().PushEvent(
		&pulseDTO, consts.EventTypeRegisterAOS,
		job.JobMetadata.TaskId, consts.EventNamespace,
		consts.PulseDomainManagerEntity, job.JobMetadata.ResourceExtId)
	if err != nil {
		log.Errorf("[%s] failed to publish pulse event with error %s",
			job.JobMetadata.ContextId, err)
		return err
	}
	log.Infof("[%s] %s Successfully published the event to pulse DB.",
		job.JobMetadata.ContextId, common_consts.REGISTRATION_LOGGER_PREFIX)
	return nil
}

// fetch the error code and failure message field from job
func fetchFailureMessageFromJob(job *models.Job) (uint64, string) {
	var errorCode uint64
	failureMessage := job.JobMetadata.OperationError
	if job.JobMetadata.ErrorDetails != nil && len(job.JobMetadata.ErrorDetails) > 0 {
		errorCode = uint64(job.JobMetadata.ErrorDetails[0].GetErrorCode())
		args := job.JobMetadata.ErrorDetails[0].GetArgs()
		if args != nil {
			for _, arg := range args {
				if arg.GetName() == consts.ErrorMessageArg {
					failureMessage = arg.GetValue()
				}
			}
		}
	}

	return errorCode, failureMessage
}
