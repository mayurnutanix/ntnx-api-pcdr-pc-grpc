/**
 * @file cert_request_helper.go
 * @description This file contains the helper functions for processing the root certificate request.
 * @author deepanshu.jain@nutanix.com
 * @copyright Copyright (c) 2024 Nutanix Inc. All rights reserved.
 */

package exchange_root_certificate

import (
	"encoding/base64"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/external"
	"ntnx-api-guru-pc/guru-pc-api-service/utils"

	"github.com/nutanix-core/go-cache/ergon"
	"google.golang.org/protobuf/proto"

	genesisSvc "github.com/nutanix-core/go-cache/infrastructure/cluster/client/genesis"
	log "github.com/sirupsen/logrus"
)

func ProcessAddRootCertRequest(taskUUID []byte, rootCert *string, peerClusterUUID string) error {
	err := SaveRootCert(peerClusterUUID, rootCert)
	if err != nil {
		log.Errorf("Error while saving root cert : %s", err)
		return err
	}

	// Update the task status with success and root certificate
	completionDetails := make([]*ergon.KeyValue, 0)
	selfRootCert, err := utils.ReadFile(consts.RootCertPath)
	if err != nil {
		log.Errorf("Error while reading root cert : %s", err)
		return err
	}
	completionDetails = append(completionDetails, &ergon.KeyValue{
		Key: proto.String(consts.RootCert),
		Value: &ergon.KeyValue_StrData{
			StrData: *selfRootCert,
		},
	})
	utils.UpdateTask(taskUUID, ergon.Task_kRunning, "", completionDetails)

	return nil
}

// SaveRootCert saves the root certificate of the peer cluster at zookeeper - /appliance/logical/auth/ca.pem path
func SaveRootCert(peerClusterUUID string, rootCertificate *string) error {
	log.Infof("%s Saving root certificate for %s", consts.RegisterPCLoggerPrefix, peerClusterUUID)

	// encoding certificate to base64 before saving using genesis client
	// this is required as genesis client expects the certificate in base64 format
	// and the certificate will also be added to /home/cert/ca.pem file
	base64EncodedCertificate := base64.StdEncoding.EncodeToString([]byte(*rootCertificate))
	args := genesisSvc.AddCACertificateArg{
		CaCert: &genesisSvc.CACert{
			CaCertificate:    []byte(base64EncodedCertificate),
			OwnerClusterUuid: []byte(peerClusterUUID),
			Owner:            genesisSvc.CACert_kPC.Enum(),
		},
	}
	saveRootCertRes, err := external.Interfaces().GenesisClient().AddCACertificate(&args)

	if err != nil {
		log.Errorf("%s Error while genesis AddCACertificate for saving the peer root certificate for %s : %s",
			consts.RegisterPCLoggerPrefix, peerClusterUUID, err)
		return consts.ErrorCertAddError
	}

	if *saveRootCertRes.Error.ErrorType == genesisSvc.GenesisGenericError_kNoError {
		log.Infof("%s Successfully saved peer root certificate for %s", consts.RegisterPCLoggerPrefix, peerClusterUUID)
		return nil
	} else if *saveRootCertRes.Error.ErrorType == genesisSvc.GenesisGenericError_kRetry {
		//TODO: Add retry error code for saving root certificate
		log.Errorf("%s Error while saving the peer root certificate for %s : %+v - %+v",
			consts.RegisterPCLoggerPrefix, peerClusterUUID, saveRootCertRes.Error.ErrorType, saveRootCertRes.Error.ErrorMsg)
		return consts.ErrorGenesisRetry
	}
	log.Errorf("%s Error while saving the peer root certificate for %s : %v",
		consts.RegisterPCLoggerPrefix, peerClusterUUID, saveRootCertRes.Error.ErrorType)
	return consts.ErrorCertAddError
}

// DeleteRootCert deletes the root certificate of the peer cluster frm zookeeper - /appliance/logical/auth/ca.pem path
func DeleteRootCert(peerClusterUUID string) error {
	log.Infof("%s Deleting root certificate for %s", consts.RegisterPCLoggerPrefix, peerClusterUUID)

	args := genesisSvc.DeleteCACertificateArg{
		ClusterUuid: []byte(peerClusterUUID),
	}
	deleteRootCertRes, err := external.Interfaces().GenesisClient().DeleteCACertificate(&args)

	if err != nil {
		log.Errorf("%s Error while genesis DeleteCACertificate for delete the peer root certificate for %s : %s",
			consts.RegisterPCLoggerPrefix, peerClusterUUID, err.Error())
		return consts.ErrorCertDeleteError
	}

	if deleteRootCertRes.GetError().GetErrorType() == genesisSvc.GenesisGenericError_kNoError {
		log.Infof("%s Successfully deleted peer root certificate for %s", consts.RegisterPCLoggerPrefix, peerClusterUUID)
		return nil
	} else if deleteRootCertRes.GetError().GetErrorType() == genesisSvc.GenesisGenericError_kRetry {
		//TODO: Add retry error code for deleting root certificate
		log.Errorf("%s Error while deleting the peer root certificate for %s : %+v - %+v",
			consts.RegisterPCLoggerPrefix, peerClusterUUID, deleteRootCertRes.Error.ErrorType, deleteRootCertRes.Error.ErrorMsg)
		return consts.ErrorGenesisRetry
	}
	log.Errorf("%s Error while deleting the peer root certificate for %s : %+v",
		consts.RegisterPCLoggerPrefix, peerClusterUUID, deleteRootCertRes.Error.ErrorType)
	return consts.ErrorCertDeleteError

}