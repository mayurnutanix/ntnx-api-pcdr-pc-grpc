package utils

import (
	"testing"

	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/test"

	"github.com/golang/mock/gomock"
	"github.com/golang/protobuf/proto"
	"github.com/nutanix-core/go-cache/ergon"
	"github.com/stretchr/testify/assert"
)

func TestPushPulseDataForRegisterPC_Success(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	job := &models.Job{
		JobMetadata: &models.JobMetadata{
			ContextId:             "contextId",
			RemoteVersion:         "remoteVersion",
			RemoteClusterId:       "remoteClusterId",
			RemoteEnvironmentType: "remoteEnvironmentType",
			RemoteProviderType:    "remoteProviderType",
			RemoteInstanceType:    "remoteInstanceType",
			SelfEnvironmentType:   "selfEnvironmentType",
			SelfProviderType:      "selfProviderType",
			SelfInstanceType:      "selfInstanceType",
		},
	}

	m.EventForwarder.EXPECT().PushEvent(
		gomock.Any(), consts.EventTypeRegisterDomainManager, job.JobMetadata.TaskId,
		consts.EventNamespace, consts.PulseDomainManagerEntity, job.JobMetadata.SelfClusterId)
	err := RegisterPulseEvent(job, true)
	assert.Nil(t, err)
}

func TestPushPulseDataForRegisterPC_Failure(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	job := &models.Job{
		JobMetadata: &models.JobMetadata{
			ContextId:             "contextId",
			RemoteVersion:         "remoteVersion",
			RemoteClusterId:       "remoteClusterId",
			RemoteEnvironmentType: "remoteEnvironmentType",
			RemoteProviderType:    "remoteProviderType",
			RemoteInstanceType:    "remoteInstanceType",
			SelfEnvironmentType:   "selfEnvironmentType",
			SelfProviderType:      "selfProviderType",
			SelfInstanceType:      "selfInstanceType",
			OperationError:        "failureMessage",
			ErrorDetails: []*ergon.Error{
				{
					ErrorCode: proto.Int32(55000),
				},
			},
		},
	}

	m.EventForwarder.EXPECT().PushEvent(
		gomock.Any(), consts.EventTypeRegisterDomainManager, job.JobMetadata.TaskId,
		consts.EventNamespace, consts.PulseDomainManagerEntity, job.JobMetadata.SelfClusterId).Return(TestError)
	err := RegisterPulseEvent(job, false)

	assert.EqualError(t, err, TestError.Error())
}

func TestPushPulseDataForRegisterAOS_Success(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	job := &models.Job{
		JobMetadata: &models.JobMetadata{
			ContextId:       "contextId",
			RemoteVersion:   "remoteVersion",
			RemoteClusterId: "remoteClusterId",
			UseTrust:        true,
		},
	}

	m.EventForwarder.EXPECT().PushEvent(
		gomock.Any(), consts.EventTypeRegisterAOS, job.JobMetadata.TaskId,
		consts.EventNamespace, consts.PulseDomainManagerEntity, job.JobMetadata.SelfClusterId)
	err := PushPulseDataForRegisterAOS(job, true)

	assert.Nil(t, err)
}

func TestPushPulseDataForRegisterAOS_Failure(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	job := &models.Job{
		JobMetadata: &models.JobMetadata{
			ContextId:       "contextId",
			RemoteVersion:   "remoteVersion",
			RemoteClusterId: "remoteClusterId",
			UseTrust:        true,
			OperationError:  "failureMessage",
			ErrorDetails: []*ergon.Error{
				{
					ErrorCode: proto.Int32(55000),
				},
			},
		},
	}

	m.EventForwarder.EXPECT().PushEvent(
		gomock.Any(), consts.EventTypeRegisterAOS, job.JobMetadata.TaskId,
		consts.EventNamespace, consts.PulseDomainManagerEntity, job.JobMetadata.SelfClusterId).Return(TestError)
	err := PushPulseDataForRegisterAOS(job, false)

	assert.EqualError(t, err, TestError.Error())
}

func TestUnregisterPulseEvent_Success(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	job := &models.Job{
		JobMetadata: &models.JobMetadata{
			ContextId:       "contextId",
			RemoteClusterId: "remoteClusterId",
		},
	}

	m.EventForwarder.EXPECT().PushEvent(
		gomock.Any(), consts.EventTypeUnregister, job.JobMetadata.TaskId,
		consts.EventNamespace, consts.PulseDomainManagerEntity, job.JobMetadata.SelfClusterId)
	UnregisterPulseEvent(job, true)
}

func TestUnregisterPulseEvent_Failure(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	job := &models.Job{
		JobMetadata: &models.JobMetadata{
			ContextId:       "contextId",
			RemoteClusterId: "remoteClusterId",
			OperationError:  "failureMessage",
			ErrorDetails: []*ergon.Error{
				{
					ErrorCode: proto.Int32(55000),
				},
			},
		},
	}

	m.EventForwarder.EXPECT().PushEvent(
		gomock.Any(), consts.EventTypeUnregister, job.JobMetadata.TaskId,
		consts.EventNamespace, consts.PulseDomainManagerEntity, job.JobMetadata.SelfClusterId).Return(TestError)
	UnregisterPulseEvent(job, false)
}

func TestPulseEvent_UnregisterPcSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	job := &models.Job{
		JobMetadata: &models.JobMetadata{
			ContextId:       "contextId",
			RemoteClusterId: "remoteClusterId",
		},
		Name: consts.UNREGISTER_PC,
	}

	m.EventForwarder.EXPECT().PushEvent(
		gomock.Any(), consts.EventTypeUnregister, job.JobMetadata.TaskId,
		consts.EventNamespace, consts.PulseDomainManagerEntity, job.JobMetadata.SelfClusterId)
	PulseEvent(job, true)
}

func TestPulseEvent_RegisterPcSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	job := &models.Job{
		JobMetadata: &models.JobMetadata{
			ContextId:       "contextId",
			RemoteClusterId: "remoteClusterId",
		},
		Name: consts.REGISTER_PC,
	}

	m.EventForwarder.EXPECT().PushEvent(
		gomock.Any(), consts.EventTypeRegisterDomainManager, job.JobMetadata.TaskId,
		consts.EventNamespace, consts.PulseDomainManagerEntity, job.JobMetadata.SelfClusterId)
	PulseEvent(job, true)
}

func TestPulseEvent_RegisterAOSSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	job := &models.Job{
		JobMetadata: &models.JobMetadata{
			ContextId:       "contextId",
			RemoteClusterId: "remoteClusterId",
		},
		Name: consts.REGISTER_AOS,
	}

	m.EventForwarder.EXPECT().PushEvent(
		gomock.Any(), consts.EventTypeRegisterAOS, job.JobMetadata.TaskId,
		consts.EventNamespace, consts.PulseDomainManagerEntity, job.JobMetadata.SelfClusterId)
	PulseEvent(job, true)
}

func TestPulseEvent_DefaultCaseSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	job := &models.Job{
		JobMetadata: &models.JobMetadata{
			ContextId:       "contextId",
			RemoteClusterId: "remoteClusterId",
		},
		Name: consts.ADD_ROOT_CERTIFICATE,
	}

	PulseEvent(job, true)
}

func TestFetchFailureMessageFromJob_Success(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	job := &models.Job{
		JobMetadata: &models.JobMetadata{
			OperationError:  "failureMessage",
			ErrorDetails: []*ergon.Error{
				{
					ErrorCode: proto.Int32(55201),
					Args: []*ergon.ErrorArg{
						{
							Name: proto.String(consts.ErrorMessageArg),
							Value: proto.String(consts.ErrorPCAlreadyRegistered.Error()),
						},
					},
				},
			},
		},
	}
	errorCode, failureMessage := fetchFailureMessageFromJob(job)
	assert.Equal(t, uint64(55201), errorCode)
	assert.Equal(t, consts.ErrorPCAlreadyRegistered.Error(), failureMessage)
}

func TestFetchFailureMessageFromJob_EmptyErrorMessage(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	job := &models.Job{
		JobMetadata: &models.JobMetadata{
			OperationError:  "failureMessage",
			ErrorDetails: []*ergon.Error{
				{
					ErrorCode: proto.Int32(55000),
					Args: []*ergon.ErrorArg{
						{
							Name: proto.String(consts.ArgumentArg),
							Value: proto.String(consts.CurrentETagArg),
						},
					},
				},
			},
		},
	}
	errorCode, failureMessage := fetchFailureMessageFromJob(job)
	assert.Equal(t, uint64(55000), errorCode)
	assert.Equal(t, "failureMessage", failureMessage)
}
