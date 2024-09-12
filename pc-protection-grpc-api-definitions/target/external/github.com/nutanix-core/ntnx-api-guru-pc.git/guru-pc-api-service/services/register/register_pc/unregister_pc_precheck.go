package register_pc

import (
	"fmt"

	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/external"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/services/register/configure_connection"
	"ntnx-api-guru-pc/guru-pc-api-service/utils"

	common_utils "github.com/nutanix-core/ntnx-api-guru/services/utils"

	log "github.com/sirupsen/logrus"
)

type UnregisterPCPrecheck struct {
}

func (precheck *UnregisterPCPrecheck) Execute(job *models.Job) guru_error.GuruErrorInterface {
	isUpgrading, err := utils.IsUpgradeInProgress()
	if err != nil {
		log.Errorf("[%s] PC-unregistration Cluster-Upgrade precheck raised error: %s", job.JobMetadata.ContextId, err)
		return guru_error.GetInternalError(consts.UnregisterPCOpName)
	}
	if isUpgrading {
		return guru_error.GetPreconditionFailureError(consts.UpgradeInProgressErr, consts.UnregisterPCOpName)
	}

	isScalingOut, err := utils.IsScaleoutInProgress()
	if err != nil {
		log.Errorf("[%s] PC-unregistration Scaleout precheck raised error: %s", job.JobMetadata.ContextId, err)
		return guru_error.GetInternalError(consts.UnregisterPCOpName)
	}
	if isScalingOut {
		return guru_error.GetPreconditionFailureError(consts.ErrorPCIsScaleoutInProgress.Error(), consts.UnregisterPCOpName)
	}

	// Check if any DR entities or workflows are still enabled
	guruError := configure_connection.CheckDRWorkflows(job.JobMetadata.RemoteClusterId)
	if guruError != nil {
		return guruError
	}

	// Check if remote cluster is reachable and compatible with source PC
	res, guruErr := callPrismCentralApi(true, job.JobMetadata.RemoteClusterId, "", "")
	if guruErr != nil {
		log.Warnf("[%s] PC-PC Unregistration is set local only,"+
			"will unregister on local only as remote cluster is not reachable",
			job.JobMetadata.ContextId)
		job.JobMetadata.IsLocalOnly = true
	}

	if !job.JobMetadata.IsLocalOnly && res != nil {
		job.JobMetadata.RemoteVersion = res.Resources.Version
		guruErr := checkGuruVersionCompatibility(job.JobMetadata.RemoteVersion)
		if guruErr != nil {
			return guruErr
		}
	}

	// Get the external IP of the remote cluster from CES
	cesPath := fmt.Sprintf("%s/%s", consts.DomainManagerCES, job.JobMetadata.RemoteClusterId)
	ces, err := common_utils.GetClusterExternalState(
		external.Interfaces().ZkSession(), job.JobMetadata.ContextId, cesPath)
	if err != nil {
		log.Warnf("[%s] Failed to retrived CES, PC-PC Unregistration is set local only, will unregister on local only",
			job.JobMetadata.ContextId)
		// Updating IsLocalOnly flag in job metadata as we don't have remote cluster details like external IP
		job.JobMetadata.IsLocalOnly = true
	} else {
		// Set remote address and cluster name in job metadata
		if ces.GetConfigDetails() != nil && ces.GetConfigDetails().ExternalIp == nil {
			job.JobMetadata.RemoteAddress = ces.GetConfigDetails().GetExternalIp()
		}
		if ces.GetClusterDetails() != nil && ces.GetClusterDetails().ClusterName != nil {
			job.JobMetadata.RemoteClusterName = ces.GetClusterDetails().GetClusterName()
		}

	}
	return nil
}
