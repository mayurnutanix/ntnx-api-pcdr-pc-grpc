/*
 * Copyright (c) 2024 Nutanix Inc. All rights reserved.
 * Author: harsh.seta@nutanix.com
 * This file contains logic of cert signing during registration between PE and PC
 */

package cert_signing_trust_setup

import (
	"encoding/json"
	"net/http"
	"net/url"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/external"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	pc_utils "ntnx-api-guru-pc/guru-pc-api-service/utils"

	ergon_proto "github.com/nutanix-core/go-cache/ergon"
	"github.com/nutanix-core/go-cache/infrastructure/cluster/client/genesis"
	genesisSvc "github.com/nutanix-core/go-cache/infrastructure/cluster/client/genesis"
	"github.com/nutanix-core/go-cache/util-go/uuid4"

	common_consts "github.com/nutanix-core/ntnx-api-guru/services/constants"
	"github.com/nutanix-core/ntnx-api-guru/services/utils"
	log "github.com/sirupsen/logrus"
)

func New() (CertSigningTrustSetupIfc, error) {
	certSigningTrustSetup := &CertSigningTrustSetup{}
	log.Debugf("%s Cert Signing Based Trust DTO successfully created : %+v",
		common_consts.REGISTRATION_LOGGER_PREFIX, certSigningTrustSetup)
	return certSigningTrustSetup, nil
}

func (certSigningTrustSetup *CertSigningTrustSetup) Execute(
	item *models.Job) error {

	contextId := item.JobMetadata.ContextId
	// If recovered task then rollback because recovery cannot happen
	// as credentials are lost
	if item.JobMetadata.IsRecoveredTask {
		log.Errorf("[%s] %s Task recovered in cert signing step "+
			"failing it because credentials lost before establishing trust",
			contextId, common_consts.REGISTRATION_LOGGER_PREFIX)
		return consts.ErrorRollbackRecoverTask
	}

	csrTaskUuid := item.JobMetadata.StepsToTaskMap[1]

	// start the progress of trust setup subtask
	external.Interfaces().ErgonService().UpdateTask(
		csrTaskUuid, ergon_proto.Task_kRunning, consts.TrustSetupStartMessage,
		nil, nil, nil, nil,
	)

	cert_signing_request, guruErr := certSigningTrustSetup.InitiateCSR(item)
	if guruErr != nil {
		log.Errorf("[%s] %s Failed to initiate CSR %v", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX, guruErr)

		// create error details for updating the ergon task
		errorDetails := guruErr.GetTaskErrorDetails()
		item.JobMetadata.ErrorDetails = errorDetails
		utils.FailTaskWithError(external.Interfaces().ErgonService(),
			csrTaskUuid, errorDetails)
		return guruErr
	}

	signed_cert_details, guruErr := certSigningTrustSetup.SignCertificate(item, cert_signing_request)
	if guruErr != nil {
		log.Errorf("[%s] %s Failed to sign certificate chain of remote cluster %v",
			contextId, common_consts.REGISTRATION_LOGGER_PREFIX, guruErr)

		// create error details for updating the ergon task
		errorDetails := guruErr.GetTaskErrorDetails()
		item.JobMetadata.ErrorDetails = errorDetails
		utils.FailTaskWithError(external.Interfaces().ErgonService(),
			csrTaskUuid, errorDetails)
		return guruErr
	}

	success, guruErr := certSigningTrustSetup.UpdateCaChain(item, signed_cert_details)
	if guruErr != nil || !*success {
		log.Errorf("[%s] %s Failed to update certificate chain of remote cluster %v",
			contextId, common_consts.REGISTRATION_LOGGER_PREFIX, guruErr)

		// create error details for updating the ergon task
		errorDetails := guruErr.GetTaskErrorDetails()
		item.JobMetadata.ErrorDetails = errorDetails
		utils.FailTaskWithError(external.Interfaces().ErgonService(),
			csrTaskUuid, errorDetails)
		return guruErr
	}

	// Complete csr task
	err := external.Interfaces().ErgonService().UpdateTask(
		csrTaskUuid, ergon_proto.Task_kSucceeded,
		consts.TrustSetupCompleteMessage,
		&common_consts.TASK_COMPLETED_PERCENTAGE, nil, nil, nil,
	)
	if err != nil {
		log.Errorf("[%s] %s Failed to update ergon task %s with error %s ",
			contextId, common_consts.REGISTRATION_LOGGER_PREFIX,
			uuid4.ToUuid4(csrTaskUuid).String(), err)

		// create error details for updating the ergon task
		errorDetails := guru_error.GetServiceUnavailableError(consts.RegisterAosOpName).GetTaskErrorDetails()
		item.JobMetadata.ErrorDetails = errorDetails
		utils.FailTaskWithError(external.Interfaces().ErgonService(),
			csrTaskUuid, errorDetails)
		return err
	}

	item.JobMetadata.StepsCompleted += 1

	// create internal opaque in json format
	internalOpaque, err := json.Marshal(item.JobMetadata)
	if err != nil {
		log.Errorf("[%s] %s Failed to marshal internal opaque for "+
			"task with uuid %s and with error %s", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX,
			uuid4.ToUuid4(item.JobMetadata.ParentTaskId).String(), err)
	}
	// Updating the internal opaque and updating the task completion percentage
	external.Interfaces().ErgonService().UpdateTask(
		item.JobMetadata.ParentTaskId, consts.TaskStatusNone,
		consts.TrustSetupCompleteMessage,
		nil, internalOpaque, nil, nil,
	)
	return nil
}

func (certSigningTrustSetup *CertSigningTrustSetup) InitiateCSR(
	item *models.Job) (*models.ClusterTrustSetupDetailsDTO, guru_error.GuruErrorInterface) {

	contextId := item.JobMetadata.ContextId
	useTrust := item.JobMetadata.UseTrust

	uri := consts.GetCsrUrl
	port := consts.EnvoyPort
	method := http.MethodGet
	headerParams := make(map[string]string)
	queryParams := url.Values{}
	clusterIpOrFqdn := item.JobMetadata.RemoteAddress
	var username, password string

	if !useTrust {
		username = item.CredentialMap["username"]
		password = item.CredentialMap["password"]
	}

	responseBody, responseCode, err := external.Interfaces().RemoteRestClient().CallApi(
		clusterIpOrFqdn, port, uri, method, nil,
		headerParams, queryParams, nil, &username, &password, useTrust)

	if err != nil {
		log.Errorf("[%s] %s Failed to invoke the endpoint %s %v", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX, uri, err)
		// Extract error message from response body
		return nil, pc_utils.GetErrorFromStatusCode(responseCode, err, consts.RegisterAosOpName)
	}
	log.Debugf("[%s] %s Response body of initiate certSigningTrustSetup %+v", contextId,
		common_consts.REGISTRATION_LOGGER_PREFIX, responseBody)

	var responseDTO models.ClusterTrustSetupDetailsDTO
	err = json.Unmarshal(responseBody, &responseDTO)
	if err != nil {
		log.Errorf("[%s] %s Failed to unmarshal with error %v", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX, err)
		return nil, guru_error.GetInternalError(consts.RegisterAosOpName)
	}
	log.Debugf("[%s] %s Response unmarshalled %+v", contextId,
		common_consts.REGISTRATION_LOGGER_PREFIX, responseDTO)
	log.Infof("[%s] %s Successfully fetched the cluster certificate "+
		"signing request", contextId, common_consts.REGISTRATION_LOGGER_PREFIX)
	return &responseDTO, nil
}

func (certSigningTrustSetup *CertSigningTrustSetup) SignCertificate(
	item *models.Job, body *models.ClusterTrustSetupDetailsDTO,
) (*models.ClusterTrustSetupDetailsDTO, guru_error.GuruErrorInterface) {

	contextId := item.JobMetadata.ContextId

	isCaCertificate := true
	args := genesisSvc.SignCertificateArg{
		CertificateSigningRequest: []byte(body.ClusterCertificateSigningRequest),
		IsCaCertificate:           &isCaCertificate,
	}
	signCertRet, err := external.Interfaces().GenesisClient().SignCertificate(&args)

	if err != nil {
		log.Errorf("[%s] %s Failed to call genesis SignCertificate RPC  %v", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX, err)
		return nil, guru_error.GetInternalError(consts.RegisterAosOpName)
	}

	if signCertRet == nil {
		log.Errorf("[%s] %s Failed to get signed certificate", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX)
		return nil, guru_error.GetInternalError(consts.RegisterAosOpName)
	}

	if *signCertRet.Error.ErrorType != genesis.GenesisGenericError_kNoError {
		log.Errorf("[%s] %s Failed to sign cluster cert with  error: %v", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX, signCertRet.Error.ErrorMsg)
		return nil, guru_error.GetInternalError(consts.RegisterAosOpName)
	}

	if len(signCertRet.SigningChain.CaCertificateList) > 2 {
		log.Errorf("[%s] %s Invalid ca chain received, expected two "+
			" certs but found %d", contextId, common_consts.REGISTRATION_LOGGER_PREFIX,
			len(signCertRet.SigningChain.CaCertificateList))

		return nil, guru_error.GetInternalError(consts.RegisterAosOpName)
	}

	intermediateCertificate := ""
	if len(signCertRet.SigningChain.CaCertificateList) > 1 {
		intermediateCertificate = string(signCertRet.SigningChain.CaCertificateList[1])
	}

	signCertDTO := models.ClusterTrustSetupDetailsDTO{
		SignedCertificate:           string(signCertRet.SignedCert),
		RootCertificate:             string(signCertRet.SigningChain.CaCertificateList[0]),
		IntermediateCertificate:     intermediateCertificate,
		CertificateOwnerClusterUUID: string(signCertRet.SigningChain.OwnerClusterUuid),
	}

	log.Debugf("[%s] %s Response DTO of sign certificate"+
		"signing request %+v", contextId,
		common_consts.REGISTRATION_LOGGER_PREFIX, signCertDTO)

	log.Infof("[%s] %s Successfully signed the cluster certificate",
		contextId, common_consts.REGISTRATION_LOGGER_PREFIX)
	return &signCertDTO, nil
}

func (certSigningTrustSetup *CertSigningTrustSetup) UpdateCaChain(
	item *models.Job, body *models.ClusterTrustSetupDetailsDTO) (
	*bool, guru_error.GuruErrorInterface) {

	contextId := item.JobMetadata.ContextId
	useTrust := item.JobMetadata.UseTrust

	clusterIpOrFqdn := item.JobMetadata.RemoteAddress
	uri := consts.UpdateCertificateChainUrl
	port := consts.EnvoyPort
	method := http.MethodPut
	headerParams := make(map[string]string)
	queryParams := url.Values{}
	var username, password string

	if !useTrust {
		username = item.CredentialMap["username"]
		password = item.CredentialMap["password"]
	}

	responseBody, responseCode, err := external.Interfaces().RemoteRestClient().CallApi(
		clusterIpOrFqdn, port, uri, method, body,
		headerParams, queryParams, nil, &username, &password, useTrust)

	if err != nil {
		log.Errorf("[%s] %s Failed to invoke the endpoint %v", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX, err)
		// Extract error message from response body
		return nil, pc_utils.GetErrorFromStatusCode(responseCode, err, consts.RegisterAosOpName)
	}
	log.Debugf("[%s] %s Response body of update ca chain %+v", contextId,
		common_consts.REGISTRATION_LOGGER_PREFIX, responseBody)
	var unmarshalledResp models.PrimitiveDTO
	err = json.Unmarshal(responseBody, &unmarshalledResp)
	if err != nil {
		log.Errorf("[%s] %s Failed to unmarshal with error %v", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX, err)
		return nil, guru_error.GetInternalError(consts.RegisterAosOpName)
	}
	log.Debugf("[%s] %s Unmmarshalled response of update ca chain %+v",
		contextId, common_consts.REGISTRATION_LOGGER_PREFIX, unmarshalledResp)
	success := unmarshalledResp.Value
	log.Infof("[%s] %s Remote Certificate chain updated successfully %v",
		contextId, common_consts.REGISTRATION_LOGGER_PREFIX, success)
	return &success, nil
}

func (certSigningTrustSetup *CertSigningTrustSetup) ResetCaChain(
	item *models.Job) (*bool, guru_error.GuruErrorInterface) {

	contextId := item.JobMetadata.ContextId
	useTrust := item.JobMetadata.UseTrust

	clusterIpOrFqdn := item.JobMetadata.RemoteAddress
	uri := consts.ResetCertificateChainUrl
	port := consts.EnvoyPort
	method := http.MethodPut
	headerParams := make(map[string]string)
	queryParams := url.Values{}
	var username, password string

	if !useTrust {
		username = item.CredentialMap["username"]
		password = item.CredentialMap["password"]
	}

	// Try Reset CA Chain with credentials
	responseBody, responseCode, err := external.Interfaces().RemoteRestClient().CallApi(
		clusterIpOrFqdn, port, uri, method, nil,
		headerParams, queryParams, nil, &username, &password, false)
	if err != nil {
		log.Errorf("[%s] %s Failure while resetting certificate chain using crendentials: %v",
			contextId, common_consts.REGISTRATION_LOGGER_PREFIX, err)

		// Try Reset CA Chain with certs
		responseBody, responseCode, err = external.Interfaces().RemoteRestClient().CallApi(
			clusterIpOrFqdn, port, uri, method, nil,
			headerParams, queryParams, nil, &username, &password, true)
		if err != nil {
			log.Errorf("[%s] %s Failure while resetting certificate chain using certs: %v",
				contextId, common_consts.REGISTRATION_LOGGER_PREFIX, err)

			// Extract error message from response body
			return nil, pc_utils.GetErrorFromStatusCode(responseCode, err, consts.RegisterAosOpName)
		}
	}
	var unmarshalledResp models.PrimitiveDTO
	err = json.Unmarshal(responseBody, &unmarshalledResp)
	if err != nil {
		log.Errorf("[%s] %s Failed to unmarshal with error %v", contextId,
			common_consts.REGISTRATION_LOGGER_PREFIX, err)
		return nil, guru_error.GetInternalError(consts.RegisterAosOpName)
	}

	log.Debugf("[%s] %s Unmmarshalled response of update ca chain %+v",
		contextId, common_consts.REGISTRATION_LOGGER_PREFIX, unmarshalledResp)
	success := unmarshalledResp.Value
	log.Infof("[%s] %s Successfully reset the certificate chain of remote cluster %v",
		contextId, common_consts.REGISTRATION_LOGGER_PREFIX, clusterIpOrFqdn)
	return &success, nil
}

func (certSigningTrustSetup *CertSigningTrustSetup) Rollback(
	item *models.Job) {

	success, err := certSigningTrustSetup.ResetCaChain(item)
	if err != nil || !*success {
		log.Errorf("[%s] %s Failure to rollback trust setup changes with error %s",
			item.JobMetadata.ContextId, common_consts.REGISTRATION_LOGGER_PREFIX, err)
	}
}
