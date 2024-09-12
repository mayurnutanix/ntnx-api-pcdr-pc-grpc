/*
 * Copyright (c) 2024 Nutanix Inc. All rights reserved.
 *
 * Author: deepanshu.jain@nutanix.com
 *
 * This file contains the implementation of ConfigureConnection interface for PC (MULTI_CLUSTER)
 * which is responsible for creating PC Domain and CES.
 */

package configure_connection

import (
	"fmt"
	"net"

	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/external"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/services/register/pc_domain"
	"ntnx-api-guru-pc/guru-pc-api-service/services/trust_setup/exchange_root_certificate"
	"ntnx-api-guru-pc/guru-pc-api-service/utils"
	idf_utils "ntnx-api-guru-pc/guru-pc-api-service/utils/idf"

	"github.com/nutanix-core/go-cache/prism"
	"github.com/nutanix-core/go-cache/zeus"
	zeus_config "github.com/nutanix-core/go-cache/zeus/config"
	common_utils "github.com/nutanix-core/ntnx-api-guru/services/utils"

	log "github.com/sirupsen/logrus"
	"google.golang.org/protobuf/proto"
)

type PCConnConfig struct {
	pcDomain pc_domain.PCDomain
}

func (pcc *PCConnConfig) RunPreConfigureActions(job *models.Job) error {
	isUpgrading, err := utils.IsUpgradeInProgress()
	if err != nil {
		log.Errorf("[%s] PC-registration Cluster-Upgrade precheck raised error: %s", job.JobMetadata.ContextId, err)
		return guru_error.GetInternalError(consts.ConfigureConnectionOpName)
	}
	if isUpgrading {
		return guru_error.GetPreconditionFailureError(consts.UpgradeInProgressErr, consts.ConfigureConnectionOpName)
	}
	isScalingOut, err := utils.IsScaleoutInProgress()
	if err != nil {
		log.Errorf("[%s] PC-registration Scaleout precheck raised error: %s", job.JobMetadata.ContextId, err)
		return guru_error.GetInternalError(consts.RegisterPCOpName)
	}
	if isScalingOut {
		return guru_error.GetPreconditionFailureError(consts.ErrorPCIsScaleoutInProgress.Error(), consts.RegisterPCOpName)
	}
	return nil
}

func (pcc *PCConnConfig) RunPreUnconfigureActions(job *models.Job) error {
	isUpgrading, err := utils.IsUpgradeInProgress()
	if err != nil {
		log.Errorf("[%s] PC-unregistration Cluster-Upgrade precheck raised error: %s", job.JobMetadata.ContextId, err)
		return guru_error.GetInternalError(consts.UnconfigureConnectionOpName)
	}
	if isUpgrading {
		message := "Cluster Upgrade is in progress. Try again once the upgrade finishes"
		return guru_error.GetPreconditionFailureError(message, consts.UnconfigureConnectionOpName)
	}
	isScalingOut, err := utils.IsScaleoutInProgress()
	if err != nil {
		log.Errorf("[%s] PC-unregistration Scaleout precheck raised error: %s", job.JobMetadata.ContextId, err)
		return guru_error.GetInternalError(consts.UnconfigureConnectionOpName)
	}
	if isScalingOut {
		return guru_error.GetPreconditionFailureError(consts.ErrorPCIsScaleoutInProgress.Error(), consts.UnconfigureConnectionOpName)
	}

	// Check if any DR entities or workflows exist
	guruErr := CheckDRWorkflows(job.JobMetadata.RemoteClusterId)
	if guruErr != nil {
		return guruErr
	}
	return nil
}

func (pcc *PCConnConfig) Configure(job *models.Job) error {

	// Check if it is a PUT call.
	// TODO: Add actual PUT API to ConfigureConnection
	// Currently this is being done to avoid regression in 2024.3 release
	// Cant change schema anymore for 2024.3
	connectionExist, err := checkIfConnectionExist(job.JobMetadata.RemoteClusterId)
	if err != nil {
		return fmt.Errorf("error checking for exisiting connection: %s", err)
	}
	if connectionExist {
		log.Infof("Found existing connection for cluster uuid %s. Updating connection.", job.JobMetadata.RemoteClusterId)
		return updateConnection(job.JobMetadata.RemoteClusterId, job.JobMetadata.RemoteAddress)
	}

	err = pcc.createPCDomain(job)
	if err != nil {
		return fmt.Errorf("%w", err)
	}

	err = pcc.createCES(job)
	if err != nil {
		return fmt.Errorf("creating CES: %w", err)
	}

	// Update cluster list (with registered PE's cluster uuid) in ergon task as
	// certificate chain is updated due to configure connection workflow
	registeredClusterUuids, err := utils.FetchRegisteredClusterUuids()
	if err != nil {
		log.Warnf("[%s] Failed to fetch registered cluster uuids: %s", job.JobMetadata.ContextId, err)
	} else {
		utils.UpdateClusterListInTask(job, registeredClusterUuids)
	}

	exchange_root_certificate.BroadcastCert(job.JobMetadata.RemoteClusterId)
	return nil
}

func (pcc *PCConnConfig) Unconfigure(job *models.Job) error {
	//Delete PC Domain
	exists, az, err := pc_domain.CheckAzByUrlExist(job.JobMetadata.RemoteClusterId)
	if err != nil {
		return fmt.Errorf("checking AZ exist: %w", err)
	}
	if exists {
		azUuid := az.GetEntityGuid().GetEntityId()

		// Delete Remote Connection if exists
		rcUuid, _ := az.GetString(consts.RemoteConnectionUuidAzName)
		if rcUuid != "" {
			idf_utils.DeleteEntityById(rcUuid, consts.RemoteConnectionEntityName)
		}

		// Delete Cloud Trust if exists
		cloudTrustUuid, _ := az.GetString(consts.CloudTrustUuidAzName)
		if cloudTrustUuid != "" {
			idf_utils.DeleteEntityById(cloudTrustUuid, consts.CloudTrustEnityName)
		}

		// Delete PC Domain
		err = pcc.pcDomain.Delete(azUuid)
		if err != nil {
			return fmt.Errorf("deleting PC Domain: %w", err)
		}
	}
	//Delete CES
	err = common_utils.DeleteClusterExternalState(external.Interfaces().ZkSession(),
		job.JobMetadata.ContextId, fmt.Sprintf("%s/%s", consts.DomainManagerCES, job.JobMetadata.RemoteClusterId))
	if err != nil {
		return fmt.Errorf("deleting CES: %w", err)
	}

	// Update cluster list (with registered PE's cluster uuid) in ergon task as
	// certificate chain is updated due to unconfigure connection workflow
	registeredClusterUuids, err := utils.FetchRegisteredClusterUuids()
	if err != nil {
		log.Warnf("[%s] Failed to fetch registered cluster uuids: %s", job.JobMetadata.ContextId, err)
	} else {
		utils.UpdateClusterListInTask(job, registeredClusterUuids)
	}
	exchange_root_certificate.BroadcastCertRemove(job.JobMetadata.RemoteClusterId)
	return nil
}

func (pcc *PCConnConfig) createCES(job *models.Job) error {
	clusterExternalStateDTO := createCESfromJob(job)
	log.Debugf("[%s] Creating CES for remote cluster", job.JobMetadata.ContextId)
	zkPath := fmt.Sprintf("%s/%s", consts.DomainManagerCES, job.JobMetadata.RemoteClusterId)

	err := common_utils.CreateClusterExternalState(external.Interfaces().ZkSession(),
		&clusterExternalStateDTO, "", zkPath)
	if err != nil {
		return err
	}
	return nil
}

func createCESfromJob(job *models.Job) prism.ClusterExternalState {
	remoteAddress := job.JobMetadata.RemoteAddress
	configDetails := new(prism.ConfigDetails)
	if net.ParseIP(remoteAddress) != nil {
		configDetails.ExternalIp = proto.String(remoteAddress)
	} else {
		configDetails.ClusterFullyQualifiedDomainName = proto.String(remoteAddress)
	}

	clusterExternalStateDTO := prism.ClusterExternalState{
		LogicalTimestamp: new(int64),
		ClusterUuid:      proto.String(job.JobMetadata.RemoteClusterId),
		ClusterDetails: &prism.ClusterDetails{
			ClusterName:  proto.String(job.JobMetadata.RemoteClusterName),
			Multicluster: proto.Bool(true),
			ContactInfo: &zeus_config.ConfigurationProto_NetworkEntity{
				AddressList: job.JobMetadata.RemoteNodeIPAddresses,
				Port:        proto.Int32(consts.EnvoyPort),
			},
			ClusterFunctions: proto.Uint32(uint32(*zeus_config.ConfigurationProto_kMulticluster.Enum())),
		},
		ConfigDetails: configDetails,
	}
	return clusterExternalStateDTO
}

func (pcc *PCConnConfig) createPCDomain(job *models.Job) error {
	az := zeus.AvailabilityZonePhysical{
		Uuid:        proto.String(job.JobMetadata.RemoteClusterId),
		Name:        proto.String(job.JobMetadata.RemoteClusterName),
		Type:        zeus.AvailabilityZonePhysicalType_kPC.Enum(),
		Url:         proto.String(job.JobMetadata.RemoteClusterId),
		DisplayName: proto.String(job.JobMetadata.RemoteClusterName),
		Region:      proto.String(consts.OnPremRegion),
	}
	pcc.pcDomain.SetAZ(&az)
	err := pcc.pcDomain.Create()
	if err != nil {
		return fmt.Errorf("creating PC Domain: %w", err)
	}

	return nil
}
