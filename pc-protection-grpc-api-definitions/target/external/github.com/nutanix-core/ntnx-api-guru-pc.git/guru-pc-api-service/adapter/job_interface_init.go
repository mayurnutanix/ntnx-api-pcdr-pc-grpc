/*
 * Copyright (c) 2024 Nutanix Inc. All rights reserved.
 * Author : kshitij.kumar@nutanix.com
 * Defines adapter interface which acts as a collaborator for queue and corresponding state transition interfaces.
 */

package adapter

import (
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/job_interfaces/base"
	"ntnx-api-guru-pc/guru-pc-api-service/job_interfaces/register"
	"ntnx-api-guru-pc/guru-pc-api-service/job_interfaces/unregister"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/services/register_aos/registration_state_transition"
)

func (a Adapter) GetJobStateTransitionInterface(job *models.Job) base.Workflow {
	switch job.Name {
	case consts.REGISTER_AOS:
		return registration_state_transition.NewRegisterStateTransitionService()
	case consts.CONFIGURE_CONNECTION:
		return register.NewConfigureConnectionTransitionSvc(job.JobMetadata.RemoteClusterType)
	case consts.UNCONFIGURE_CONNECTION:
		return unregister.NewUnconfigureConnectionTransitionSvc(job.JobMetadata.RemoteClusterType)
	case consts.UNREGISTER_PC:
		return unregister.NewUnregisterStateTransitionSvc(job.JobMetadata.RemoteClusterType)
	case consts.REGISTER_PC:
		return register.NewRegisterStateTransitionService(job.JobMetadata.RemoteClusterType)
	case consts.ADD_ROOT_CERTIFICATE:
		return register.NewAddRootCertJobStateService()
	case consts.REMOVE_ROOT_CERTIFICATE:
		return unregister.NewRemoveRootCertJobStateService()
	}
	return nil
}
