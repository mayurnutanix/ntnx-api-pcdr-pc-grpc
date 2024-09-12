package register_pc

import (
	"encoding/json"
	"net/http"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/test"
	"ntnx-api-guru-pc/guru-pc-api-service/utils"
	"testing"

	"github.com/golang/mock/gomock"
	"github.com/golang/protobuf/proto"
	aplos "github.com/nutanix-core/go-cache/aplos/sl_bufs"
	"github.com/nutanix-core/go-cache/ergon"
	"github.com/nutanix-core/go-cache/insights/insights_interface"
	common_consts "github.com/nutanix-core/ntnx-api-guru/services/constants"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
)

var TestRemoteConnectionId = "c994b411-808f-4775-9ce5-a261e3947782"
var TestCloudTrustId = "t994b411-808f-4775-9ce5-a261e3947782"
var TestRemoteClusterUuid = "02cd17ea-034e-4796-a1d8-be2d42a15d75"

func TestCheckInitiatorPcSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)

	getZkResp := aplos.PCEnvironmentConfig{
		PcEnvironmentInfo: &aplos.PCEnvironmentConfig_PCEnvironmentInfo{
			EnvironmentType: aplos.PCEnvironmentConfig_PCEnvironmentInfo_ONPREM.Enum(),
		},
	}
	marshalGetZkResp, _ := proto.Marshal(&getZkResp)
	m.ZkClient.EXPECT().GetZkNode(consts.PCEnvZkNodePath, true).
		Return(marshalGetZkResp, nil).AnyTimes()
	err := checkValidInitiatorPC()
	assert.Nil(t, err)
}

func TestVersionCompatibility_Master(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	err := checkGuruVersionCompatibility("master")

	assert.NoError(t, err)
}

func TestVersionCompatibility_Empty(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	err := checkGuruVersionCompatibility("")

	assert.Equal(t, guru_error.GetInternalError(consts.RegisterPCOpName), err)
}

func TestVersionCompatibility_2024_3(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	err := checkGuruVersionCompatibility("pc.2024.3")

	assert.NoError(t, err)
}

func TestVersionCompatibility_2024_2(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	err := checkGuruVersionCompatibility("pc.2024.2")

	assert.Equal(t, guru_error.GetPreconditionFailureError(
		"Remote PC version pc.2024.2 not supported, try after upgrading remote PC", consts.RegisterPCOpName), err)
}

func TestCesNotExist(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)

	clusterUuid := "123"
	m.ZkSession.On("Exist", consts.DomainManagerCES+"/"+clusterUuid, true).Return(false, nil, nil).Once()
	err := checkPcClusterExternalStateExist(clusterUuid)

	assert.Nil(t, err)
}

func TestCheckExistingTasksSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)

	taskArg := ergon.TaskListArg{
		OperationTypeList: []string{consts.OperationTypeMap[consts.REGISTER_PC]},
		StatusList:        []ergon.Task_Status{ergon.Task_kQueued, ergon.Task_kRunning},
	}
	response := &ergon.TaskListRet{
		TaskUuidList: nil,
	}
	m.ErgonService.On("TaskList", &taskArg).Return(response, nil).Once()
	err := checkExistingTasks()
	assert.Nil(t, err)
}

func TestCheckSelfRegistrationSuccess(t *testing.T) {
	job := &models.Job{
		JobMetadata: &models.JobMetadata{
			RemoteClusterId: "123",
			SelfClusterId:   "456",
		},
	}

	err := checkSelfRegistration(job)

	assert.Nil(t, err)
}

func TestCheckSelfRegistrationFail(t *testing.T) {
	job := &models.Job{
		JobMetadata: &models.JobMetadata{
			RemoteClusterId: "123",
			SelfClusterId:   "123",
		},
	}

	err := checkSelfRegistration(job)
	assert.Equal(t, guru_error.GetPreconditionFailureError(consts.ErrorPCSelfRegisterCheck, consts.RegisterPCOpName), err)
}

func TestCheckSelfRegistrationLocalIdNil(t *testing.T) {
	job := &models.Job{
		JobMetadata: &models.JobMetadata{
			RemoteClusterId: "123",
		},
	}

	err := checkSelfRegistration(job)
	assert.NotNil(t, err)
}

func TestCheckSelfRegistrationRemoteIdNil(t *testing.T) {
	job := &models.Job{
		JobMetadata: &models.JobMetadata{
			SelfClusterId: "123",
		},
	}

	err := checkSelfRegistration(job)
	assert.NotNil(t, err)
}

func TestExecute_Success(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	prec := &RegisterPCPrecheckImp{}
	host, username, password := "1.1.1.1", "testUser", "testPassword"

	registerPcJob := &models.Job{
		Name: consts.REGISTER_PC,
		JobMetadata: &models.JobMetadata{
			SelfClusterId: "456",
			Name:          consts.REGISTER_PC,
			RemoteAddress: host,
		},
		CredentialMap: map[string]string{"password": password, "username": username},
	}
	taskArg := ergon.TaskListArg{
		OperationTypeList: []string{consts.OperationTypeMap[consts.REGISTER_PC]},
		StatusList:        []ergon.Task_Status{ergon.Task_kQueued, ergon.Task_kRunning},
	}
	taskListResponse := &ergon.TaskListRet{
		TaskUuidList: nil,
	}
	m.ErgonService.On("TaskList", &taskArg).Return(taskListResponse, nil).Once()
	m.ErgonService.On("TaskList", mock.Anything).Return(taskListResponse, nil).Once()
	m.ZkSession.On("Exist", consts.DomainManagerCES, true).Return(true, nil, nil).Once()
	m.ZkSession.On("Exist", consts.PCEnvZkNodePath, true).Return(true, nil, nil).Once()
	m.ZkSession.On("Exist", consts.DomainManagerCES+"/"+TestRemoteClusterUuid, true).Return(false, nil, nil).Once()
	m.ZkClient.EXPECT().GetZkNode(consts.PCEnvZkNodePath, true).Return(test.GetMockPCEnvConfig(), nil).AnyTimes()
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(test.GetMockZeusConfig(), nil).AnyTimes()
	m.DbClient.On("Query", mock.AnythingOfType("*insights_interface.GetEntitiesWithMetricsArg")).Return(nil, insights_interface.ErrNotFound)
	response := test.GetTestPrismCentralResponse()
	var responseModel *V3PrismCentralResponseModel
	json.Unmarshal([]byte(response), &responseModel)
	m.RemoteRestClient.EXPECT().CallApiBasicAuth(host, consts.EnvoyPort,
		consts.V3PrismCentralGetUrl, http.MethodGet,
		nil, nil, nil, nil, username, password).Return([]byte(response), nil, nil)

	err := prec.Execute(registerPcJob)
	assert.Nil(t, err)
}

func TestExecute_InvalidResponse(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	prec := &RegisterPCPrecheckImp{}
	host, username, password := "1.1.1.1", "testUser", "testPassword"

	registerPcJob := &models.Job{
		Name: consts.REGISTER_PC,
		JobMetadata: &models.JobMetadata{
			SelfClusterId: "456",
			Name:          consts.REGISTER_PC,
			RemoteAddress: host,
		},
		CredentialMap: map[string]string{"password": password, "username": username},
	}
	taskArg := ergon.TaskListArg{
		OperationTypeList: []string{consts.OperationTypeMap[consts.REGISTER_PC]},
		StatusList:        []ergon.Task_Status{ergon.Task_kQueued, ergon.Task_kRunning},
	}
	taskListResponse := &ergon.TaskListRet{
		TaskUuidList: nil,
	}
	m.ErgonService.On("TaskList", &taskArg).Return(taskListResponse, nil).Once()
	m.ZkSession.On("Exist", consts.DomainManagerCES, true).Return(true, nil, nil).Once()
	m.ZkSession.On("Exist", consts.PCEnvZkNodePath, true).Return(true, nil, nil).Once()
	m.ZkSession.On("Exist", consts.DomainManagerCES+"/"+TestRemoteClusterUuid, true).Return(false, nil, nil).Once()
	m.ZkClient.EXPECT().GetZkNode(consts.PCEnvZkNodePath, true).Return(test.GetMockPCEnvConfig(), nil).AnyTimes()
	m.ZkClient.EXPECT().GetZkNode("/appliance/physical/configuration", true).Return(nil, nil).AnyTimes()
	m.DbClient.On("Query", mock.AnythingOfType("*insights_interface.GetEntitiesWithMetricsArg")).Return(nil, insights_interface.ErrNotFound)
	response := test.GetInvalidPrismCentralResponseWithoutIPs()
	var responseModel *V3PrismCentralResponseModel
	json.Unmarshal([]byte(response), &responseModel)
	m.RemoteRestClient.EXPECT().CallApiBasicAuth(host, consts.EnvoyPort,
		consts.V3PrismCentralGetUrl, http.MethodGet,
		nil, nil, nil, nil, username, password).Return([]byte(response), nil, nil)

	err := prec.Execute(registerPcJob)
	assert.Equal(t, err, guru_error.GetInternalError(consts.RegisterPCOpName))
}

func TestExecute_VersionNotSupported(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	prec := &RegisterPCPrecheckImp{}
	host, username, password := "1.1.1.1", "testUser", "testPassword"

	registerPcJob := &models.Job{
		Name: consts.REGISTER_PC,
		JobMetadata: &models.JobMetadata{
			SelfClusterId: "456",
			Name:          consts.REGISTER_PC,
			RemoteAddress: host,
		},
		CredentialMap: map[string]string{"password": password, "username": username},
	}
	taskArg := ergon.TaskListArg{
		OperationTypeList: []string{consts.OperationTypeMap[consts.REGISTER_PC]},
		StatusList:        []ergon.Task_Status{ergon.Task_kQueued, ergon.Task_kRunning},
	}
	taskListResponse := &ergon.TaskListRet{
		TaskUuidList: nil,
	}
	m.ErgonService.On("TaskList", &taskArg).Return(taskListResponse, nil).Once()
	m.ZkSession.On("Exist", consts.DomainManagerCES, true).Return(true, nil, nil).Once()
	m.ZkSession.On("Exist", consts.PCEnvZkNodePath, true).Return(true, nil, nil).Once()
	m.ZkSession.On("Exist", consts.DomainManagerCES+"/"+TestRemoteClusterUuid, true).Return(false, nil, nil).Once()
	m.ZkClient.EXPECT().GetZkNode(consts.PCEnvZkNodePath, true).Return(test.GetMockPCEnvConfig(), nil).AnyTimes()
	m.ZkClient.EXPECT().GetZkNode(common_consts.ZEUS_CONFIG_NODE_PATH, true).Return(test.GetMockZeusConfig(), nil).AnyTimes()
	m.DbClient.On("Query", mock.AnythingOfType("*insights_interface.GetEntitiesWithMetricsArg")).Return(nil, insights_interface.ErrNotFound)
	response := test.GetTestPrismCentralResponseWithInvalidVersionPC()
	var responseModel *V3PrismCentralResponseModel
	json.Unmarshal([]byte(response), &responseModel)
	m.RemoteRestClient.EXPECT().CallApiBasicAuth(host, consts.EnvoyPort,
		consts.V3PrismCentralGetUrl, http.MethodGet,
		nil, nil, nil, nil, username, password).Return([]byte(response), nil, nil)

	err := prec.Execute(registerPcJob)
	assert.Equal(t, err, guru_error.GetPreconditionFailureError(
		"Remote PC version pc.2024.1 not supported, try after upgrading remote PC", consts.RegisterPCOpName))
}

func TestDRVersionCompatibilitySuccess(t *testing.T) {
	old := utils.OsReadFileFunc
	defer func() { utils.OsReadFileFunc = old }()
	utils.OsReadFileFunc = func(path string) ([]byte, error) {
		content, err := old("./../../../test/pcdr.json")
		return content, err
	}
	err := checkDRVersionCompatibility("pc.2024.2", "pc.2024.1")
	assert.Nil(t, err)
}

func TestDRVersionCompatibilityExceptedVersion(t *testing.T) {
	old := utils.OsReadFileFunc
	defer func() { utils.OsReadFileFunc = old }()
	utils.OsReadFileFunc = func(path string) ([]byte, error) {
		content, err := old("./../../../test/pcdr.json")
		return content, err
	}
	err := checkDRVersionCompatibility("pc.2024.2", "pc.2023.1")
	assert.Nil(t, err)
}

func TestDRVersionCompatibilityFalse(t *testing.T) {
	old := utils.OsReadFileFunc
	defer func() { utils.OsReadFileFunc = old }()
	utils.OsReadFileFunc = func(path string) ([]byte, error) {
		content, err := old("./../../../test/pcdr.json")
		return content, err
	}
	err := checkDRVersionCompatibility("pc.2024.2", "pc.2022.9")
	assert.NotNil(t, err)
}

func TestDRVersionCompatibilityFalse2(t *testing.T) {
	old := utils.OsReadFileFunc
	defer func() { utils.OsReadFileFunc = old }()
	utils.OsReadFileFunc = func(path string) ([]byte, error) {
		content, err := old("./../../../test/pcdr.json")
		return content, err
	}
	err := checkDRVersionCompatibility("pc.2024.2.0.6", "pc.2022.9")
	assert.NotNil(t, err)
}

func TestDRVersionCompatibilityNotInJsonMap(t *testing.T) {
	old := utils.OsReadFileFunc
	defer func() { utils.OsReadFileFunc = old }()
	utils.OsReadFileFunc = func(path string) ([]byte, error) {
		content, err := old("./../../../test/pcdr.json")
		return content, err
	}
	err := checkDRVersionCompatibility("pc.2021.2.0.6", "pc.2022.9")
	assert.NotNil(t, err)
}