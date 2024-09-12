/**
 * @file unregister_pc_helper.go
 * @description This file contains the helper methods for unregister PC
 * @author deepanshu.jain@nutanix.com
 * @copyright Copyright (c) 2024 Nutanix Inc. All rights reserved.
 */

package register_pc

import (
	"encoding/json"
	"fmt"
	"net/http"
	dto "ntnx-api-guru-pc/generated-code/target/dto/models/prism/v4/management"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/external"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/utils"

	log "github.com/sirupsen/logrus"
)

const (
	V4RemoveRootCertAPIPath     = "/api/prism/%s/management/domain-managers/%s/$actions/remove-root-certificate"
	V4UnconfigureConnectionPath = "/api/prism/%s/management/domain-managers/%s/$actions/unconfigure-connection"
)

func callRemoveRootCertificateAPI(job *models.Job) (string, guru_error.GuruErrorInterface) {
	spec := dto.NewRootCertRemoveSpec()
	spec.ClusterExtId = &job.JobMetadata.SelfClusterId
	url := fmt.Sprintf(V4RemoveRootCertAPIPath, consts.ApiVersion, job.JobMetadata.RemoteClusterId)
	headers := make(map[string]string)
	uuid, _ := external.Interfaces().Uuid().New()
	headers[consts.NtnxRequestId] = uuid.String()
	// Call the remove root cert API
	response, responseCode, err := external.Interfaces().RemoteRestClient().CallApiCertBasedAuth(job.JobMetadata.RemoteAddress,
		consts.EnvoyPort, url, http.MethodPost, &spec, headers, nil, nil)
	if err != nil {
		log.Errorf("[%s] Error calling remove root cert api: %s", job.JobMetadata.ContextId, err)
		return "", utils.GetErrorFromStatusCode(responseCode, err, consts.UnregisterPCOpName)
	}
	// Unmarshal the response
	taskResponse := utils.TaskResponseModel{}
	err = json.Unmarshal(response, &taskResponse)
	if err != nil {
		log.Errorf("[%s] Error while unmarshalling remove root cert api response %s", job.JobMetadata.ContextId, err)
		return "", guru_error.GetInternalError(consts.UnregisterPCOpName)
	}
	return taskResponse.Data.ExtId, nil
}

func callUnconfigureConnectionApi(job *models.Job) (string, guru_error.GuruErrorInterface) {
	spec := dto.NewConnectionUnconfigurationSpec()
	spec.RemoteCluster = &dto.ClusterReference{}
	spec.RemoteCluster.ExtId = &job.JobMetadata.SelfClusterId
	url := fmt.Sprintf(V4UnconfigureConnectionPath, consts.ApiVersion, job.JobMetadata.RemoteClusterId)
	headers := make(map[string]string)
	uuid, _ := external.Interfaces().Uuid().New()
	headers[consts.NtnxRequestId] = uuid.String()

	// Call the Unconfigure connection API
	response, responseCode, err := external.Interfaces().RemoteRestClient().CallApiCertBasedAuth(
		job.JobMetadata.RemoteAddress, consts.EnvoyPort, url, http.MethodPost, spec, headers, nil, nil)
	if err != nil {
		log.Errorf("[%s] Error while calling unconfigure connection API %s", job.JobMetadata.ContextId, err)
		return "", utils.GetErrorFromStatusCode(responseCode, err, consts.UnregisterPCOpName)
	}
	// Unmarshal the response
	taskResponse := utils.TaskResponseModel{}
	err = json.Unmarshal(response, &taskResponse)
	if err != nil {
		log.Errorf("[%s] Error while unmarshalling unconfigure connection API response %s", job.JobMetadata.ContextId, err)
		return "", guru_error.GetInternalError(consts.UnregisterPCOpName)
	}
	return taskResponse.Data.ExtId, nil
}
