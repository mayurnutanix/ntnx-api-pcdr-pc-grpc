package cert_signing_trust_setup

import (
	"encoding/json"
	"errors"
	_ "expvar"
	"fmt"
	_ "math"
	"net/http"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	test_consts "ntnx-api-guru-pc/guru-pc-api-service/services/register_aos/test-consts"
	"ntnx-api-guru-pc/guru-pc-api-service/test"
	"testing"

	"github.com/nutanix-core/go-cache/infrastructure/cluster/client/genesis"

	"github.com/golang/mock/gomock"
	ergon_proto "github.com/nutanix-core/go-cache/ergon"
	"github.com/nutanix-core/go-cache/util-go/uuid4"
	common_consts "github.com/nutanix-core/ntnx-api-guru/services/constants"
	"github.com/stretchr/testify/assert"
)

var (
	TEST_TASK_UUID         = []byte{11, 205, 61, 133, 16, 76, 72, 134, 85, 211, 252, 41, 149, 19, 150, 228}
	TEST_CHILD_TASK_1_UUID = []byte{11, 201, 61, 133, 16, 76, 72, 134, 85, 211, 252, 41, 149, 19, 150, 228}
	TEST_CHILD_TASK_2_UUID = []byte{11, 202, 61, 133, 16, 76, 72, 134, 85, 211, 252, 41, 149, 19, 150, 228}
	TEST_CHILD_TASK_3_UUID = []byte{11, 203, 61, 133, 16, 76, 72, 134, 85, 211, 252, 41, 149, 19, 150, 228}
	TEST_CLUSTER_UUID_BYTE = []byte{11, 101, 61, 133, 16, 76, 72, 134, 85, 211, 252, 41, 149, 19, 150, 228}
	TEST_PE_TASK_UUID_BYTE = []byte{11, 204, 61, 133, 16, 76, 72, 134, 85, 211, 252, 41, 149, 19, 150, 228}
	TEST_TRUST_CES_ZK_DATA = []byte{18, 36, 48, 48, 48, 54, 48, 98, 101, 56, 45, 51, 97, 49, 52, 45, 101, 50, 55, 100, 45, 48, 48, 48, 48, 45, 48, 48, 48, 48, 48, 48, 48, 48, 57, 48, 100, 48, 26, 16, 26, 14, 34, 12, 49, 48, 46, 52, 54, 46, 50, 55, 46, 49, 48, 56, 50, 16, 10, 12, 49, 48, 46, 52, 54, 46, 50, 55, 46, 49, 49, 54, 34, 0}

	TEST_TASK_UUID2       = []byte{1, 20, 6, 133, 16, 76, 72, 134, 85, 211, 252, 41, 149, 19, 150, 228}
	TestParentTaskUuid    = []byte{10, 204, 62, 133, 16, 76, 72, 134, 85, 211, 252, 41, 149, 19, 150, 228}
	TEST_TASK_UUID_STRING = uuid4.ToUuid4(TEST_TASK_UUID).String()
	TEST_CLUSTER_UUID     = uuid4.ToUuid4(TEST_CLUSTER_UUID_BYTE).String()
	TEST_PE_TASK_UUID     = uuid4.ToUuid4(TEST_PE_TASK_UUID_BYTE).String()

	TEST_COMPONENT                = consts.GuruTaskComponent
	TEST_ERGON_STATUS_RUNNING     = ergon_proto.Task_kRunning
	TEST_ERGON_STATUS_FAILED      = ergon_proto.Task_kFailed
	TEST_ERGON_STATUS_SUCCESS     = ergon_proto.Task_kSucceeded
	TEST_PERCENTAGE_COMPLETED_100 = common_consts.TASK_COMPLETED_PERCENTAGE

	TEST_TASK_CREATE_FAILURE_MESSAGE         = "Failed to create ergon task"
	TEST_ZK_EXIST_FAILURE_MESSAGE            = "Failed to check for zk node"
	TEST_ZK_CREATE_FAILURE_MESSAGE           = "Failed to create zk node"
	TEST_ZK_DELETE_FAILURE_MESSAGE           = "Failed to delete zk node"
	TEST_TASK_UPDATE_FAILURE_MESSAGE         = "Failed to update ergon task"
	TEST_GET_CSR_FAILURE_MESSAGE             = "Failed to connect to Prism Gateway"
	TEST_SIGN_CERTIFICATE_FAILURE_MESSAGE    = "Transport kError"
	TEST_UPDATE_CA_CHAIN_FAILURE_MESSAGE     = "Failed to connect to Prism Gateway"
	TEST_RESET_CA_CHAIN_FAILURE_MESSAGE      = "Failed to connect to Prism Gateway"
	TEST_ZK_NODE_FETCH_FAILURE               = "No such Zk node present"
	TEST_RESET_CA_CHAIN_MAIN_FAILURE_MESSAGE = fmt.Sprintf(
		"Failed to update Certificate chain of remote cluster with error: %s"+
			"and failed to reset certificate chain with error: %s",
		TEST_UPDATE_CA_CHAIN_FAILURE_MESSAGE, TEST_RESET_CA_CHAIN_FAILURE_MESSAGE,
	)
	TEST_ERROR_REGISTRATION_IN_PROGRESS_MESSAGE = consts.ERROR_REGISTRATION_IN_PROGRESS_MESSAGE

	TEST_USERNAME      = "username"
	TEST_PASSWORD      = "password"
	TEST_IP_ADDRESS    = "ipAddress"
	TEST_SVC_CERT_PATH = &common_consts.SVC_CERT_FILE_PATH
	TEST_SVC_KEY_PATH  = &common_consts.SVC_KEY_FILE_PATH
	TEST_CA_PEM_PATH   = &common_consts.CA_PEM_PATH
	TEST_EMPTY_STRING  = ""

	TEST_TASK_DTO = &models.TaskIdDTO{
		TaskUuid: TEST_PE_TASK_UUID,
	}
	TEST_TASK_DTO_BYTE, _ = json.Marshal(TEST_TASK_DTO)

	TEST_TASK_SUCCESS_STATUS = "SUCCEEDED"
	TEST_TASK_FAILED_STATUS  = "FAILED"
	TEST_ERROR               = errors.New("Test error occured")

	TEST_CONTEXT_ID         = "contextID"
	TEST_USE_TRUST_FALSE    = false
	TEST_REGISTRATION_TASKS = &models.RegistrationTaskUuidsDTO{}
)

func TestExecuteSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	r, _ := New()
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.TaskId = "task-id"
	jobMetadata.RemoteClusterId = "1.1.1.1"
	jobMetadata.ContextId = "context"
	jobMetadata.UseTrust = true
	jobMetadata.RemoteVersion = "5.15.0"
	jobMetadata.OperationStartTime = "2021-07-01T00:00:00Z"
	jobMetadata.ResourceExtId = "resource-ext-id"
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.StepsToTaskMap[1] = TestParentTaskUuid
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	// For starting  cert signing task
	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, ergon_proto.Task_kRunning, consts.TrustSetupStartMessage,
		nil, nil, nil, nil).Return(nil)

	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, ergon_proto.Task_kSucceeded,
		consts.TrustSetupCompleteMessage,
		&common_consts.TASK_COMPLETED_PERCENTAGE, nil, nil, nil,
	).Return(nil)

	m.ErgonClient.EXPECT().UpdateTask(jobMetadata.ParentTaskId, consts.TaskStatusNone,
		consts.TrustSetupCompleteMessage,
		nil, gomock.Any(), nil, nil,
	).Return(nil)
	// For calling initiate/get csr

	m.RemoteRestClient.EXPECT().CallApi(
		jobMetadata.RemoteAddress, gomock.Any(), consts.GetCsrUrl, http.MethodGet, nil,
		gomock.Any(), gomock.Any(), nil, gomock.Any(), gomock.Any(), true).Return(test_consts.TEST_GET_CSR_RESPONSE, nil, nil)

	// For signing certificate
	ErrorType := genesis.GenesisGenericError_kNoError
	ErrorRet := genesis.GenesisGenericError{
		ErrorType: &ErrorType,
		ErrorMsg:  nil,
		Timestamp: nil,
	}
	var certificatelist [][]byte
	certificatelist = append(certificatelist, TestParentTaskUuid)
	certificatelist = append(certificatelist, TestParentTaskUuid)
	signingchain := genesis.CAChain{
		CaCertificateList: certificatelist,
	}
	signCertRet := genesis.SignCertificateRet{
		Error:        &ErrorRet,
		SignedCert:   nil,
		SigningChain: &signingchain,
	}
	m.GenesisClient.EXPECT().SignCertificate(gomock.Any()).Return(&signCertRet, nil)

	// For updating csr

	m.RemoteRestClient.EXPECT().CallApi(
		"", gomock.Any(), consts.UpdateCertificateChainUrl, http.MethodPut, gomock.Any(),
		gomock.Any(), gomock.Any(), nil, gomock.Any(), gomock.Any(),
		true).Return(test_consts.TEST_UPDATE_CA_CHAIN_RESPONSE, nil, nil)

	err := r.Execute(jobVar)
	assert.Nil(t, err)
}

func TestExecuteSignCertError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	r, _ := New()

	// For starting  cert signing task
	m.ErgonClient.EXPECT().UpdateTask(
		TestParentTaskUuid, ergon_proto.Task_kRunning, consts.TrustSetupStartMessage,
		nil, nil, nil, nil,
	).Return(nil)
	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, ergon_proto.Task_kFailed,
		"", &common_consts.TASK_COMPLETED_PERCENTAGE, nil, gomock.Any(), nil,
	).Return(nil)
	m.GenesisClient.EXPECT().SignCertificate(gomock.Any()).Return(nil, guru_error.GetInternalError(consts.RegisterAosOpName))

	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.TaskId = "task-id"
	jobMetadata.RemoteClusterId = "1.1.1.1"
	jobMetadata.ContextId = "context"
	jobMetadata.UseTrust = true
	jobMetadata.RemoteVersion = "5.15.0"
	jobMetadata.OperationStartTime = "2021-07-01T00:00:00Z"
	jobMetadata.ResourceExtId = "resource-ext-id"
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.StepsToTaskMap[1] = TestParentTaskUuid
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	m.RemoteRestClient.EXPECT().CallApi(
		jobMetadata.RemoteAddress, gomock.Any(), consts.GetCsrUrl, http.MethodGet, nil,
		gomock.Any(), gomock.Any(), nil, gomock.Any(), gomock.Any(), true).Return(test_consts.TEST_GET_CSR_RESPONSE, nil, nil)

	err := r.Execute(jobVar)
	assert.EqualError(t, guru_error.GetInternalError(consts.RegisterAosOpName), err.Error())
}

func TestExecuteErrorInitiateCSR(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	r, _ := New()
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.TaskId = "task-id"
	jobMetadata.RemoteClusterId = "1.1.1.1"
	jobMetadata.ContextId = "context"
	jobMetadata.UseTrust = true
	jobMetadata.RemoteAddress = "remote-address"
	jobMetadata.OperationStartTime = "2021-07-01T00:00:00Z"
	jobMetadata.ResourceExtId = "resource-ext-id"
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.StepsToTaskMap[1] = TestParentTaskUuid
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	// For starting  cert signing task
	m.ErgonClient.EXPECT().UpdateTask(jobMetadata.StepsToTaskMap[1], ergon_proto.Task_kRunning, consts.TrustSetupStartMessage,
		nil, nil, nil, nil).Return(nil)

	// For calling initiate/get csr
	responseCode := http.StatusUnauthorized
	m.RemoteRestClient.EXPECT().CallApi(
		jobMetadata.RemoteAddress, gomock.Any(), consts.GetCsrUrl, http.MethodGet, nil,
		gomock.Any(), gomock.Any(), nil, gomock.Any(), gomock.Any(), true).Return(nil, &responseCode, TEST_ERROR)
	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, ergon_proto.Task_kFailed, "",
		gomock.Any(), nil, gomock.Any(), nil).Return(nil)
	err := r.Execute(jobVar)
	assert.Equal(t, guru_error.GetUnauthorizedRequestError(consts.RegisterAosOpName), err)
}

func TestExecuteRecoveredTask(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.IsRecoveredTask = true
	r, _ := New()
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	res := r.Execute(jobVar)
	assert.Equal(t, consts.ErrorRollbackRecoverTask, res)
}

func TestExecuteUpdateCaChainError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)

	r, _ := New()

	// For starting  cert signing task
	m.ErgonClient.EXPECT().UpdateTask(
		TestParentTaskUuid, ergon_proto.Task_kRunning, consts.TrustSetupStartMessage,
		nil, nil, nil, nil,
	).Return(nil)

	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, ergon_proto.Task_kFailed,
		"", &common_consts.TASK_COMPLETED_PERCENTAGE, nil, gomock.Any(), nil,
	).Return(nil)

	ErrorType := genesis.GenesisGenericError_kNoError
	ErrorRet := genesis.GenesisGenericError{
		ErrorType: &ErrorType,
		ErrorMsg:  nil,
		Timestamp: nil,
	}
	var certificatelist [][]byte
	certificatelist = append(certificatelist, TestParentTaskUuid)
	certificatelist = append(certificatelist, TestParentTaskUuid)
	signingchain := genesis.CAChain{
		CaCertificateList: certificatelist,
	}
	signCertRet := genesis.SignCertificateRet{
		Error:        &ErrorRet,
		SignedCert:   nil,
		SigningChain: &signingchain,
	}
	m.GenesisClient.EXPECT().SignCertificate(gomock.Any()).Return(&signCertRet, nil)
	responseCode := http.StatusUnauthorized
	m.RemoteRestClient.EXPECT().CallApi(
		"", gomock.Any(), consts.UpdateCertificateChainUrl, http.MethodPut, gomock.Any(),
		gomock.Any(), gomock.Any(), nil, gomock.Any(), gomock.Any(),
		true).Return(nil, &responseCode, TEST_ERROR)

	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.TaskId = "task-id"
	jobMetadata.RemoteClusterId = "1.1.1.1"
	jobMetadata.ContextId = "context"
	jobMetadata.UseTrust = true
	jobMetadata.RemoteVersion = "5.15.0"
	jobMetadata.OperationStartTime = "2021-07-01T00:00:00Z"
	jobMetadata.ResourceExtId = "resource-ext-id"
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.StepsToTaskMap[1] = TestParentTaskUuid
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	m.RemoteRestClient.EXPECT().CallApi(
		jobMetadata.RemoteAddress, gomock.Any(), consts.GetCsrUrl, http.MethodGet, nil,
		gomock.Any(), gomock.Any(), nil, gomock.Any(), gomock.Any(), true).Return(test_consts.TEST_GET_CSR_RESPONSE, nil, nil)

	err := r.Execute(jobVar)
	assert.Equal(t, guru_error.GetUnauthorizedRequestError(consts.RegisterAosOpName), err)
}

func TestExecuteErrorUpdateTask(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)

	r, _ := New()

	// For starting  cert signing task
	m.ErgonClient.EXPECT().UpdateTask(
		TestParentTaskUuid, ergon_proto.Task_kRunning, consts.TrustSetupStartMessage,
		nil, nil, nil, nil,
	).Return(nil)

	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, ergon_proto.Task_kSucceeded,
		consts.TrustSetupCompleteMessage, &common_consts.TASK_COMPLETED_PERCENTAGE, nil, nil, nil,
	).Return(TEST_ERROR)

	m.ErgonClient.EXPECT().UpdateTask(TestParentTaskUuid, ergon_proto.Task_kFailed, "", &common_consts.TASK_COMPLETED_PERCENTAGE,
		nil, gomock.Any(), nil).Return(nil)

	ErrorType := genesis.GenesisGenericError_kNoError
	ErrorRet := genesis.GenesisGenericError{
		ErrorType: &ErrorType,
		ErrorMsg:  nil,
		Timestamp: nil,
	}
	var certificatelist [][]byte
	certificatelist = append(certificatelist, TestParentTaskUuid)
	certificatelist = append(certificatelist, TestParentTaskUuid)
	signingchain := genesis.CAChain{
		CaCertificateList: certificatelist,
	}
	signCertRet := genesis.SignCertificateRet{
		Error:        &ErrorRet,
		SignedCert:   nil,
		SigningChain: &signingchain,
	}
	m.GenesisClient.EXPECT().SignCertificate(gomock.Any()).Return(&signCertRet, nil)
	m.RemoteRestClient.EXPECT().CallApi(
		"", gomock.Any(), consts.UpdateCertificateChainUrl, http.MethodPut, gomock.Any(),
		gomock.Any(), gomock.Any(), nil, gomock.Any(), gomock.Any(),
		true).Return(test_consts.TEST_UPDATE_CA_CHAIN_RESPONSE, nil, nil)

	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.TaskId = "task-id"
	jobMetadata.RemoteClusterId = "1.1.1.1"
	jobMetadata.ContextId = "context"
	jobMetadata.UseTrust = true
	jobMetadata.RemoteVersion = "5.15.0"
	jobMetadata.OperationStartTime = "2021-07-01T00:00:00Z"
	jobMetadata.ResourceExtId = "resource-ext-id"
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.StepsToTaskMap[1] = TestParentTaskUuid
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	m.RemoteRestClient.EXPECT().CallApi(
		jobMetadata.RemoteAddress, gomock.Any(), consts.GetCsrUrl, http.MethodGet, nil,
		gomock.Any(), gomock.Any(), nil, gomock.Any(), gomock.Any(), true).Return(test_consts.TEST_GET_CSR_RESPONSE, nil, nil)

	err := r.Execute(jobVar)
	assert.Equal(t, TEST_ERROR, err)
}

func TestInitiateCSRErrorUnmarshalling(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	r, _ := New()
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.TaskId = "task-id"
	jobMetadata.RemoteClusterId = "1.1.1.1"
	jobMetadata.ContextId = "context"
	jobMetadata.UseTrust = false
	jobMetadata.RemoteVersion = "5.15.0"
	jobMetadata.OperationStartTime = "2021-07-01T00:00:00Z"
	jobMetadata.ResourceExtId = "resource-ext-id"
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	// For calling initiate/get csr
	m.RemoteRestClient.EXPECT().CallApi(
		jobMetadata.RemoteAddress, gomock.Any(), consts.GetCsrUrl, http.MethodGet, nil,
		gomock.Any(), gomock.Any(), nil, gomock.Any(), gomock.Any(),
		false).Return(nil, nil, nil)
	trustSetupDetails, err := r.InitiateCSR(jobVar)
	assert.NotNil(t, err.Error())
	assert.Nil(t, trustSetupDetails)
}

func TestUpdateCaChain(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	r, _ := New()
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.TaskId = "task-id"
	jobMetadata.RemoteClusterId = "1.1.1.1"
	jobMetadata.ContextId = "context"
	jobMetadata.UseTrust = true
	jobMetadata.RemoteAddress = "remote-address"
	jobMetadata.OperationStartTime = "2021-07-01T00:00:00Z"
	jobMetadata.ResourceExtId = "resource-ext-id"
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.StepsToTaskMap[1] = TestParentTaskUuid
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	responseCode := http.StatusUnauthorized
	m.RemoteRestClient.EXPECT().CallApi(jobMetadata.RemoteAddress, gomock.Any(), consts.UpdateCertificateChainUrl, http.MethodPut, nil,
		gomock.Any(), gomock.Any(), nil, gomock.Any(), gomock.Any(), jobMetadata.UseTrust).Return(nil, &responseCode, errors.New("error"))
	res, err := r.UpdateCaChain(jobVar, nil)
	assert.Nil(t, res)
	assert.Equal(t, guru_error.GetUnauthorizedRequestError(consts.RegisterAosOpName), err)
}

func TestUpdateCaChainUnmarshalError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	r, _ := New()
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.TaskId = "task-id"
	jobMetadata.RemoteClusterId = "1.1.1.1"
	jobMetadata.ContextId = "context"
	jobMetadata.UseTrust = true
	jobMetadata.RemoteAddress = "remote-address"
	jobMetadata.OperationStartTime = "2021-07-01T00:00:00Z"
	jobMetadata.ResourceExtId = "resource-ext-id"
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.StepsToTaskMap[1] = TestParentTaskUuid
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	m.RemoteRestClient.EXPECT().CallApi(jobMetadata.RemoteAddress, gomock.Any(), consts.UpdateCertificateChainUrl, http.MethodPut, nil,
		gomock.Any(), gomock.Any(), nil, gomock.Any(), gomock.Any(), jobMetadata.UseTrust).Return(TestParentTaskUuid, nil, nil)
	res, _ := r.UpdateCaChain(jobVar, nil)
	assert.Nil(t, res)
}

func TestUpdateCaChainUnmarshalErrorWithOutTrust(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	r, _ := New()
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.TaskId = "task-id"
	jobMetadata.RemoteClusterId = "1.1.1.1"
	jobMetadata.ContextId = "context"
	jobMetadata.UseTrust = false
	jobMetadata.RemoteAddress = "remote-address"
	jobMetadata.OperationStartTime = "2021-07-01T00:00:00Z"
	jobMetadata.ResourceExtId = "resource-ext-id"
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.StepsToTaskMap[1] = TestParentTaskUuid
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	m.RemoteRestClient.EXPECT().CallApi(jobMetadata.RemoteAddress, gomock.Any(), consts.UpdateCertificateChainUrl, http.MethodPut, nil,
		gomock.Any(), gomock.Any(), nil, gomock.Any(), gomock.Any(), jobMetadata.UseTrust).Return(TestParentTaskUuid, nil, nil)
	res, _ := r.UpdateCaChain(jobVar, nil)
	assert.Nil(t, res)
}

func TestUpdateCaChainSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	r, _ := New()
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.TaskId = "task-id"
	jobMetadata.RemoteClusterId = "1.1.1.1"
	jobMetadata.ContextId = "context"
	jobMetadata.UseTrust = true
	jobMetadata.RemoteAddress = "remote-address"
	jobMetadata.OperationStartTime = "2021-07-01T00:00:00Z"
	jobMetadata.ResourceExtId = "resource-ext-id"
	jobMetadata.ParentTaskId = TestParentTaskUuid
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.StepsToTaskMap[1] = TestParentTaskUuid
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	responseBody := models.PrimitiveDTO{Value: true}
	marhshalledResp, _ := json.Marshal(responseBody)
	m.RemoteRestClient.EXPECT().CallApi(jobMetadata.RemoteAddress, gomock.Any(), consts.UpdateCertificateChainUrl, http.MethodPut, nil,
		gomock.Any(), gomock.Any(), nil, gomock.Any(), gomock.Any(), jobMetadata.UseTrust).Return(marhshalledResp, nil, nil)
	res, err := r.UpdateCaChain(jobVar, nil)
	assert.Equal(t, true, *res)
	assert.Nil(t, err)
}

func TestResetCaChainError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	r, _ := New()
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.UseTrust = true
	jobMetadata.RemoteAddress = "remote-address"
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.StepsToTaskMap[1] = TestParentTaskUuid
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	responseCode := http.StatusUnauthorized
	m.RemoteRestClient.EXPECT().CallApi(jobMetadata.RemoteAddress, gomock.Any(), consts.ResetCertificateChainUrl, http.MethodPut, nil,
		gomock.Any(), gomock.Any(), nil, gomock.Any(), gomock.Any(), false).Return(nil, &responseCode, errors.New("error"))
	m.RemoteRestClient.EXPECT().CallApi(
		jobMetadata.RemoteAddress, gomock.Any(), consts.ResetCertificateChainUrl, http.MethodPut, nil,
		gomock.Any(), gomock.Any(), nil, gomock.Any(), gomock.Any(), true).Return(nil, &responseCode, errors.New("error"))
	res, err := r.ResetCaChain(jobVar)
	assert.Nil(t, res)
	assert.Equal(t, guru_error.GetUnauthorizedRequestError(consts.RegisterAosOpName), err)
}

func TestResetCaChainErrorWithOutTrust(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	r, _ := New()
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.UseTrust = false
	jobMetadata.RemoteAddress = "remote-address"
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.StepsToTaskMap[1] = TestParentTaskUuid
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	responseCode := http.StatusUnauthorized
	m.RemoteRestClient.EXPECT().CallApi(jobMetadata.RemoteAddress, gomock.Any(), consts.ResetCertificateChainUrl, http.MethodPut, nil,
		gomock.Any(), gomock.Any(), nil, gomock.Any(), gomock.Any(), false).Return(nil, &responseCode, errors.New("error"))
	m.RemoteRestClient.EXPECT().CallApi(
		jobMetadata.RemoteAddress, gomock.Any(), consts.ResetCertificateChainUrl, http.MethodPut, nil,
		gomock.Any(), gomock.Any(), nil, gomock.Any(), gomock.Any(), true).Return(nil, &responseCode, errors.New("error"))
	res, err := r.ResetCaChain(jobVar)
	assert.Nil(t, res)
	assert.Equal(t, guru_error.GetUnauthorizedRequestError(consts.RegisterAosOpName), err)
}

func TestResetCaChainUnMarshalError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	r, _ := New()
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.UseTrust = true
	jobMetadata.RemoteAddress = "remote-address"
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.StepsToTaskMap[1] = TestParentTaskUuid
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	m.RemoteRestClient.EXPECT().CallApi(jobMetadata.RemoteAddress, gomock.Any(), consts.ResetCertificateChainUrl, http.MethodPut, nil,
		gomock.Any(), gomock.Any(), nil, gomock.Any(), gomock.Any(), false).Return(TestParentTaskUuid, nil, nil)
	res, _ := r.ResetCaChain(jobVar)
	assert.Nil(t, res)
}

func TestResetCaChainSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	r, _ := New()
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.UseTrust = true
	jobMetadata.RemoteAddress = "remote-address"
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.StepsToTaskMap[1] = TestParentTaskUuid
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	responseBody := models.PrimitiveDTO{Value: true}
	marhshalledResp, _ := json.Marshal(responseBody)
	m.RemoteRestClient.EXPECT().CallApi(jobMetadata.RemoteAddress, gomock.Any(), consts.ResetCertificateChainUrl, http.MethodPut, nil,
		gomock.Any(), gomock.Any(), nil, gomock.Any(), gomock.Any(), false).Return(marhshalledResp, nil, nil)
	res, err := r.ResetCaChain(jobVar)
	assert.Equal(t, true, *res)
	assert.Nil(t, err)
}

func TestRollBack(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	r, _ := New()
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.UseTrust = true
	jobMetadata.RemoteAddress = "remote-address"
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.StepsToTaskMap[1] = TestParentTaskUuid
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	responseCode := http.StatusUnauthorized
	m.RemoteRestClient.EXPECT().CallApi(jobMetadata.RemoteAddress, gomock.Any(), consts.ResetCertificateChainUrl, http.MethodPut, nil,
		gomock.Any(), gomock.Any(), nil, gomock.Any(), gomock.Any(), false).Return(nil, &responseCode, errors.New("error"))
	m.RemoteRestClient.EXPECT().CallApi(
		jobMetadata.RemoteAddress, gomock.Any(), consts.ResetCertificateChainUrl, http.MethodPut, nil,
		gomock.Any(), gomock.Any(), nil, gomock.Any(), gomock.Any(), true).Return(nil, &responseCode, errors.New("error"))
	r.Rollback(jobVar)
}

func TestSignCertificateError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	r, _ := New()
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.UseTrust = true
	jobMetadata.ContextId = "context-id"
	jobMetadata.RemoteAddress = "remote-address"
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.StepsToTaskMap[1] = TestParentTaskUuid
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	body := models.ClusterTrustSetupDetailsDTO{
		ClusterCertificateSigningRequest: "",
	}
	m.GenesisClient.EXPECT().SignCertificate(gomock.Any()).Return(nil, errors.New("error"))
	res, err := r.SignCertificate(jobVar, &body)
	assert.Nil(t, res)
	assert.Equal(t, guru_error.GetInternalError(consts.RegisterAosOpName), err)
}

func TestSignCertificateSignCertNil(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	r, _ := New()
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.UseTrust = true
	jobMetadata.ContextId = "context-id"
	jobMetadata.RemoteAddress = "remote-address"
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.StepsToTaskMap[1] = TestParentTaskUuid
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	body := models.ClusterTrustSetupDetailsDTO{
		ClusterCertificateSigningRequest: "",
	}
	m.GenesisClient.EXPECT().SignCertificate(gomock.Any()).Return(nil, nil)
	res, err := r.SignCertificate(jobVar, &body)
	assert.Nil(t, res)
	assert.Equal(t, guru_error.GetInternalError(consts.RegisterAosOpName), err)
}

func TestSignCertificateSignCertErrorTypeInputError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	r, _ := New()
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.UseTrust = true
	jobMetadata.ContextId = "context-id"
	jobMetadata.RemoteAddress = "remote-address"
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.StepsToTaskMap[1] = TestParentTaskUuid
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	body := models.ClusterTrustSetupDetailsDTO{
		ClusterCertificateSigningRequest: "",
	}
	errorMsg := "error"
	ErrorType := genesis.GenesisGenericError_kInputError
	ErrorRet := genesis.GenesisGenericError{
		ErrorType: &ErrorType,
		ErrorMsg:  &errorMsg,
		Timestamp: nil,
	}
	signCertRet := genesis.SignCertificateRet{
		Error:        &ErrorRet,
		SignedCert:   nil,
		SigningChain: nil,
	}
	m.GenesisClient.EXPECT().SignCertificate(gomock.Any()).Return(&signCertRet, nil)
	res, err := r.SignCertificate(jobVar, &body)
	assert.Nil(t, res)
	assert.Equal(t, guru_error.GetInternalError(consts.RegisterAosOpName), err)
}

func TestSignCertificateSignCertInvalidCaChain(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	r, _ := New()
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.UseTrust = true
	jobMetadata.ContextId = "context-id"
	jobMetadata.RemoteAddress = "remote-address"
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.StepsToTaskMap[1] = TestParentTaskUuid
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	body := models.ClusterTrustSetupDetailsDTO{
		ClusterCertificateSigningRequest: "",
	}
	errorMsg := "error"
	ErrorType := genesis.GenesisGenericError_kNoError
	ErrorRet := genesis.GenesisGenericError{
		ErrorType: &ErrorType,
		ErrorMsg:  &errorMsg,
		Timestamp: nil,
	}
	var certificatelist [][]byte
	certificatelist = append(certificatelist, TestParentTaskUuid)
	certificatelist = append(certificatelist, TestParentTaskUuid)
	certificatelist = append(certificatelist, TestParentTaskUuid)
	signingchain := genesis.CAChain{
		CaCertificateList: certificatelist,
	}
	signCertRet := genesis.SignCertificateRet{
		Error:        &ErrorRet,
		SignedCert:   nil,
		SigningChain: &signingchain,
	}
	m.GenesisClient.EXPECT().SignCertificate(gomock.Any()).Return(&signCertRet, nil)
	res, err := r.SignCertificate(jobVar, &body)
	assert.Nil(t, res)
	assert.Equal(t, guru_error.GetInternalError(consts.RegisterAosOpName), err)
}

func TestSignCertificateSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	r, _ := New()
	credentialMap := make(map[string]string)
	credentialMap["username"] = ""
	credentialMap["password"] = ""
	var jobMetadata models.JobMetadata
	jobMetadata.UseTrust = true
	jobMetadata.ContextId = "context-id"
	jobMetadata.RemoteAddress = "remote-address"
	jobMetadata.StepsToTaskMap = make(map[int][]byte)
	jobMetadata.StepsToTaskMap[0] = TestParentTaskUuid
	jobMetadata.StepsToTaskMap[1] = TestParentTaskUuid
	jobVar := &models.Job{
		Name:          jobMetadata.Name,
		JobMetadata:   &jobMetadata,
		CredentialMap: credentialMap,
	}
	body := models.ClusterTrustSetupDetailsDTO{
		ClusterCertificateSigningRequest: "",
	}
	errorMsg := "error"
	ErrorType := genesis.GenesisGenericError_kNoError
	ErrorRet := genesis.GenesisGenericError{
		ErrorType: &ErrorType,
		ErrorMsg:  &errorMsg,
		Timestamp: nil,
	}
	var certificatelist [][]byte
	certificatelist = append(certificatelist, TestParentTaskUuid)
	certificatelist = append(certificatelist, TestParentTaskUuid)
	signingchain := genesis.CAChain{
		CaCertificateList: certificatelist,
	}
	signCertRet := genesis.SignCertificateRet{
		Error:        &ErrorRet,
		SignedCert:   nil,
		SigningChain: &signingchain,
	}
	m.GenesisClient.EXPECT().SignCertificate(gomock.Any()).Return(&signCertRet, nil)
	res, err := r.SignCertificate(jobVar, &body)
	assert.NotNil(t, res)
	assert.Nil(t, err)
}
