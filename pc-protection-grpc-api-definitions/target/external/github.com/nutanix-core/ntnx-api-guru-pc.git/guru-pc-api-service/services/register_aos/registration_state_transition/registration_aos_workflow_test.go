package registration_state_transition

import (
	"errors"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/external"
	"ntnx-api-guru-pc/guru-pc-api-service/test"
	"testing"

	"github.com/nutanix-core/go-cache/ergon"

	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	mocks "ntnx-api-guru-pc/guru-pc-api-service/mocks"
	"ntnx-api-guru-pc/guru-pc-api-service/models"

	"github.com/golang/mock/gomock"
	"github.com/nutanix-core/ntnx-api-guru/services/constants"
	mockErgon "github.com/nutanix-core/ntnx-api-guru/services/ergon/mocks"
	"github.com/stretchr/testify/assert"
)

var (
	ParentTaskUuidTest       = []byte{12, 213, 12}
	ErrorChildTaskCreateTest = errors.New("failure in creating child tasks")
	ErrorPrechecksTest       = errors.New("failure during prechecks")
	ErrorTrustSetupTest      = errors.New("failure during csr")
	ErrorPrefixTest          = "error prefix"
	ContextIdTest            = "context-id"
)

func TestExecuteForwardSuccessWhenStepCount0(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockRegistrationCommon := mocks.NewMockRegistrationCommonUtilIfc(ctrl)
	r := &RegisterAOSWorkflow{
		registrationCommonUtil: mockRegistrationCommon,
	}
	item := models.Job{
		JobMetadata: &models.JobMetadata{
			Rollback: false,
		},
	}
	mockRegistrationCommon.EXPECT().CreateChildTasks(&item).Return(nil)
	stepsCount := 0
	err := r.Execute(stepsCount, &item)
	assert.Nil(t, err)
}

func TestExecuteForwardFailureWhenStepCount0(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockRegistrationCommon := mocks.NewMockRegistrationCommonUtilIfc(ctrl)
	r := &RegisterAOSWorkflow{
		registrationCommonUtil: mockRegistrationCommon,
	}
	item := models.Job{
		JobMetadata: &models.JobMetadata{
			Rollback: false,
		},
	}
	stepsCount := 0
	mockRegistrationCommon.EXPECT().CreateChildTasks(
		&item).Return(ErrorChildTaskCreateTest)
	mockRegistrationCommon.EXPECT().ProcessRegistrationErrorState(
		&item, gomock.Any(), stepsCount)
	err := r.Execute(stepsCount, &item)
	assert.EqualError(t, ErrorChildTaskCreateTest, err.Error())
}

func TestExecuteForwardSuccessWhenStepCount1(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockRegistrationPrechecks := mocks.NewMockRegistrationPrecheckIfc(ctrl)
	r := &RegisterAOSWorkflow{
		registrationPrecheck: mockRegistrationPrechecks,
	}
	item := models.Job{
		JobMetadata: &models.JobMetadata{
			Rollback: false,
		},
	}
	mockRegistrationPrechecks.EXPECT().Execute(&item).Return(nil)
	stepsCount := 1
	err := r.Execute(stepsCount, &item)
	assert.Nil(t, err)
}

func TestExecuteForwardFailureWhenStepCount1(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockRegistrationCommon := mocks.NewMockRegistrationCommonUtilIfc(ctrl)
	mockRegistrationPrechecks := mocks.NewMockRegistrationPrecheckIfc(ctrl)
	r := &RegisterAOSWorkflow{
		registrationPrecheck:   mockRegistrationPrechecks,
		registrationCommonUtil: mockRegistrationCommon,
	}
	item := models.Job{
		JobMetadata: &models.JobMetadata{
			Rollback: false,
		},
	}
	stepsCount := 1
	mockRegistrationPrechecks.EXPECT().Execute(&item).Return(ErrorPrechecksTest)
	mockRegistrationCommon.EXPECT().ProcessRegistrationErrorState(
		&item, gomock.Any(), stepsCount)
	err := r.Execute(stepsCount, &item)
	assert.EqualError(t, ErrorPrechecksTest, err.Error())
}

func TestExecuteForwardSuccessWhenStepCount2(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockRegistrationCSR := mocks.NewMockCertSigningTrustSetupIfc(ctrl)

	r := &RegisterAOSWorkflow{
		registrationCsr: mockRegistrationCSR,
	}
	item := models.Job{
		JobMetadata: &models.JobMetadata{
			Rollback: false,
		},
	}
	mockRegistrationCSR.EXPECT().Execute(&item).Return(nil)
	stepsCount := 2
	err := r.Execute(stepsCount, &item)
	assert.Nil(t, err)
}

func TestExecuteForwardFailureWhenStepCount2(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockRegistrationCommon := mocks.NewMockRegistrationCommonUtilIfc(ctrl)
	mockRegistrationCSR := mocks.NewMockCertSigningTrustSetupIfc(ctrl)
	r := &RegisterAOSWorkflow{
		registrationCsr:        mockRegistrationCSR,
		registrationCommonUtil: mockRegistrationCommon,
	}
	item := models.Job{
		JobMetadata: &models.JobMetadata{
			Rollback: false,
		},
	}
	stepsCount := 2
	mockRegistrationCSR.EXPECT().Execute(&item).Return(ErrorTrustSetupTest)
	mockRegistrationCommon.EXPECT().ProcessRegistrationErrorState(
		&item, gomock.Any(), stepsCount)
	err := r.Execute(stepsCount, &item)
	assert.EqualError(t, ErrorTrustSetupTest, err.Error())
}

func TestExecuteForwardSuccessWhenStepCount3(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockRegistrationProxy := mocks.NewMockRegistrationProxyIfc(ctrl)
	r := &RegisterAOSWorkflow{
		registrationProxy: mockRegistrationProxy,
	}
	item := models.Job{
		JobMetadata: &models.JobMetadata{
			Rollback: false,
		},
	}
	mockRegistrationProxy.EXPECT().Execute(&item).Return(nil)
	stepsCount := 3
	err := r.Execute(stepsCount, &item)
	assert.Nil(t, err)
}

func TestExecuteForwardFailureWhenStepCount3(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockRegistrationCommon := mocks.NewMockRegistrationCommonUtilIfc(ctrl)
	mockRegistrationProxy := mocks.NewMockRegistrationProxyIfc(ctrl)
	r := &RegisterAOSWorkflow{
		registrationProxy:      mockRegistrationProxy,
		registrationCommonUtil: mockRegistrationCommon,
	}
	item := models.Job{
		JobMetadata: &models.JobMetadata{
			Rollback: false,
		},
	}
	stepsCount := 3
	mockRegistrationProxy.EXPECT().Execute(&item).Return(ErrorTrustSetupTest)
	mockRegistrationCommon.EXPECT().ProcessRegistrationErrorState(
		&item, gomock.Any(), stepsCount)
	err := r.Execute(stepsCount, &item)
	assert.EqualError(t, ErrorTrustSetupTest, err.Error())
}

func TestExecuteForwardFailureWhenStepCountDefault(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	r := &RegisterAOSWorkflow{}
	item := models.Job{
		JobMetadata: &models.JobMetadata{
			Rollback: false,
		},
	}
	stepsCount := 4
	err := r.Execute(stepsCount, &item)
	assert.Nil(t, err)
}

func TestExecuteRollbackSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockErgonClient := mockErgon.NewMockErgonClientIfc(ctrl)
	mockRegistrationProxy := mocks.NewMockRegistrationProxyIfc(ctrl)
	mockRegistrationCommon := mocks.NewMockRegistrationCommonUtilIfc(ctrl)
	mockRegistrationPrecheck := mocks.NewMockRegistrationPrecheckIfc(ctrl)
	mockRegistrationCsr := mocks.NewMockCertSigningTrustSetupIfc(ctrl)
	external.SetSingletonServices(mockErgonClient, nil, nil, nil,
		nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil)
	r := &RegisterAOSWorkflow{
		registrationCommonUtil: mockRegistrationCommon,
		registrationPrecheck:   mockRegistrationPrecheck,
		registrationProxy:      mockRegistrationProxy,
		registrationCsr:        mockRegistrationCsr,
	}
	item := models.Job{
		JobMetadata: &models.JobMetadata{
			Rollback:        true,
			StepsCompleted:  3,
			ContextId:       ContextIdTest,
			ParentTaskId:    ParentTaskUuidTest,
			RemoteClusterId: "remote-cluster-id",
		},
	}
	mockErgonClient.EXPECT().UpdateTask(
		ParentTaskUuidTest, consts.TaskStatusNone, gomock.Any(), nil, gomock.Any(), nil, nil).Return(nil).Times(3)
	mockErgonClient.EXPECT().UpdateTask(
		ParentTaskUuidTest, ergon.Task_kFailed, "",
		&constants.TASK_COMPLETED_PERCENTAGE, nil, gomock.Any(), nil).Return(nil)

	mockRegistrationProxy.EXPECT().Rollback(&item)
	//mockRegistrationProxy.EXPECT().DeleteClusterState(&item)
	mockRegistrationCsr.EXPECT().Rollback(&item)
	mockRegistrationCommon.EXPECT().DeleteRegistrationMarkerNode(&item)

	stepsCount := 3
	err := r.Execute(stepsCount, &item)
	assert.Nil(t, err)
}

func TestExecuteRollbackFailTaskFailure(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockErgonClient := mockErgon.NewMockErgonClientIfc(ctrl)
	mockRegistrationProxy := mocks.NewMockRegistrationProxyIfc(ctrl)
	mockRegistrationCommon := mocks.NewMockRegistrationCommonUtilIfc(ctrl)
	mockRegistrationPrecheck := mocks.NewMockRegistrationPrecheckIfc(ctrl)
	mockRegistrationCsr := mocks.NewMockCertSigningTrustSetupIfc(ctrl)
	external.SetSingletonServices(mockErgonClient, nil, nil, nil,
		nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil)
	r := &RegisterAOSWorkflow{
		registrationCommonUtil: mockRegistrationCommon,
		registrationPrecheck:   mockRegistrationPrecheck,
		registrationProxy:      mockRegistrationProxy,
		registrationCsr:        mockRegistrationCsr,
	}
	item := models.Job{
		JobMetadata: &models.JobMetadata{
			Rollback:       true,
			StepsCompleted: 3,
			ContextId:      ContextIdTest,
			ParentTaskId:   ParentTaskUuidTest,
		},
	}
	mockErgonClient.EXPECT().UpdateTask(
		ParentTaskUuidTest, consts.TaskStatusNone, gomock.Any(), nil, gomock.Any(), nil, nil).Return(nil).Times(3)
	mockErgonClient.EXPECT().UpdateTask(
		ParentTaskUuidTest, ergon.Task_kFailed, "",
		&constants.TASK_COMPLETED_PERCENTAGE, nil, gomock.Any(), nil).Return(errors.New(""))
	mockRegistrationProxy.EXPECT().Rollback(&item)
	mockRegistrationCsr.EXPECT().Rollback(&item)
	mockRegistrationCommon.EXPECT().DeleteRegistrationMarkerNode(&item)
	stepsCount := 3
	err := r.Execute(stepsCount, &item)
	assert.EqualError(t, consts.ErrorRegistrationTaskExecution, err.Error())
}

func TestAbortIfInvalidRequest(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)

	r := &RegisterAOSWorkflow{}
	item := models.Job{
		JobMetadata: &models.JobMetadata{
			StepsCompleted: 1,
			ParentTaskId:   ParentTaskUuidTest,
		},
	}
	m.ErgonClient.EXPECT().UpdateTask(item.JobMetadata.ParentTaskId, ergon.Task_kAborted,
		consts.RegistrationRecoveryFailureTaskAbortMessage, nil, nil, nil, nil)
	r.AbortIfInvalidRequest(&item)
}

func TestIsTerminalForward(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	r := &RegisterAOSWorkflow{}
	currentStep := 2
	isTerminal := r.IsTerminalForward(int64(currentStep))
	assert.Equal(t, false, isTerminal)
}

func TestIsTerminalForwardWhenTerminalState(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	r := &RegisterAOSWorkflow{}
	currentStep := 4
	isTerminal := r.IsTerminalForward(int64(currentStep))
	assert.Equal(t, true, isTerminal)
}

func TestIsTerminalRollback(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	r := &RegisterAOSWorkflow{}
	item := models.Job{
		JobMetadata: &models.JobMetadata{
			StepsCompleted: 1,
			ParentTaskId:   ParentTaskUuidTest,
		},
	}
	currentStep := 2
	isTerminal := r.IsTerminalRollback(int64(currentStep), &item)
	assert.Equal(t, false, isTerminal)
}

func TestIsTerminalRollbackWhenRollbackCompleted(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	r := &RegisterAOSWorkflow{}
	item := models.Job{
		JobMetadata: &models.JobMetadata{
			StepsCompleted: 1,
			ParentTaskId:   ParentTaskUuidTest,
			Rollback:       true,
		},
	}
	currentStep := -1
	isTerminal := r.IsTerminalRollback(int64(currentStep), &item)
	assert.Equal(t, true, isTerminal)
}
