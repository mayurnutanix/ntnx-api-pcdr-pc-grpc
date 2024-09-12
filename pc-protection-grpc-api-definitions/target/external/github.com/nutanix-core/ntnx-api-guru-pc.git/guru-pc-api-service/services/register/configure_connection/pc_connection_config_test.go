package configure_connection

import (
	"errors"
	"fmt"
	"ntnx-api-guru-pc/generated-code/target/dto/models/prism/v4/management"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/mocks"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/test"
	"testing"

	"github.com/golang/mock/gomock"
	"github.com/golang/protobuf/proto"
	"github.com/nutanix-core/go-cache/ergon"
	zk "github.com/nutanix-core/go-cache/go-zookeeper"
	"github.com/nutanix-core/go-cache/insights/insights_interface"
	"github.com/nutanix-core/go-cache/zeus"
	"github.com/nutanix-core/ntnx-api-guru/services/constants"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
)

var errTest = errors.New("test error")
var TestUuid = "e994b411-808f-4775-9ce5-a261e3947782"
var TestRemoteConnectionId = "c994b411-808f-4775-9ce5-a261e3947782"
var TestCloudTrustId = "t994b411-808f-4775-9ce5-a261e3947782"
var (
	TestTaskUuid = []byte("2f0164ea-3705-4067-a00d-2953e187b324")
	TestTask     = &ergon.Task{
		Uuid:             TestTaskUuid,
		LogicalTimestamp: proto.Int64(1),
	}
)

func createJob() *models.Job {
	uuid := "2f0164ea-3705-4067-a00d-2953e187b324"
	job := &models.Job{
		JobMetadata: &models.JobMetadata{
			RemoteClusterType: management.CLUSTERTYPE_DOMAIN_MANAGER,
			Name:              consts.CONFIGURE_CONNECTION,
			ParentTaskId:      []byte(uuid),
			RemoteClusterId:   "7443776a-2d39-43ea-923e-e775a3a28cc8",
			RemoteAddress:     "127.0.0.1",
			RemoteClusterName: "PC_NAME",
		},
		Name:       consts.CONFIGURE_CONNECTION,
		ParentTask: TestTask,
	}
	return job
}

func createAZ(job *models.Job) *zeus.AvailabilityZonePhysical {
	displayName := "PC_NAME"
	region := consts.OnPremRegion
	az := &zeus.AvailabilityZonePhysical{
		Uuid:        &job.JobMetadata.RemoteClusterId,
		Name:        &displayName,
		Type:        zeus.AvailabilityZonePhysicalType_kPC.Enum(),
		Url:         &job.JobMetadata.RemoteClusterId,
		Region:      &region,
		DisplayName: &displayName,
	}
	return az
}

func TestConfigureEdit_CESAlreadyExists(t *testing.T) {
	// Arrange
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	job := createJob()
	m := test.MockSingletons(t)
	mockCes := test.GetMockCES()
	byteCes, _ := proto.Marshal(mockCes)
	zkStat := zk.Stat{
		Version: 1,
	}
	m.ZkSession.On("Create", consts.DomainManagerCES+"/"+job.JobMetadata.RemoteClusterId,
		mock.Anything, int32(0), zk.WorldACL(zk.PermAll), true).Return("", nil).Once()
	m.ZkSession.On("Get", consts.DomainManagerCES+"/"+job.JobMetadata.RemoteClusterId, true).
		Return(byteCes, &zkStat, nil)
	m.ZkSession.On("Exist", consts.DomainManagerCES+"/"+job.JobMetadata.RemoteClusterId, true).
		Return(true, nil, nil)
	m.ZkSession.On("Set", consts.DomainManagerCES+"/"+job.JobMetadata.RemoteClusterId, mock.Anything, int32(1), true).
		Return(nil, nil)
	mockPCDomain := mocks.NewMockPCDomain(ctrl)

	// Creating PCConnConfig object
	c := &PCConnConfig{
		pcDomain: mockPCDomain,
	}

	// Act
	err := c.Configure(job)

	// Assert
	assert.Nil(t, err)
}

func TestConfigureFail_CESCreateError(t *testing.T) {
	// Arrange
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	// Creating Job object
	job := createJob()
	az := createAZ(job)
	m := test.MockSingletons(t)
	m.ZkClient.EXPECT().GetZkNode(constants.ZEUS_CONFIG_NODE_PATH, true).Return(test.GetMockZeusConfig(), nil).AnyTimes()
	m.ZkSession.On("Create", mock.Anything, mock.Anything,
		int32(0), zk.WorldACL(zk.PermAll), true).Return("", errTest).Once()
	m.ZkSession.On("Exist", mock.Anything, true).Return(false, nil, nil)
	m.ZkSession.On("Exist", mock.Anything, true).Return(true, nil, nil)
	mockPCDomain := mocks.NewMockPCDomain(ctrl)
	mockPCDomain.EXPECT().SetAZ(az).Return(nil)
	mockPCDomain.EXPECT().Create().Return(nil)

	c := &PCConnConfig{
		pcDomain: mockPCDomain,
	}

	// Act
	err := c.Configure(job)

	// Assert
	assert.NotNil(t, err)
	assert.Equal(t, fmt.Errorf("creating CES: %w", errTest), err)
}

func TestConfigure_ErrorPCDomain(t *testing.T) {
	// Arrange
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	job := createJob()
	az := createAZ(job)
	m := test.MockSingletons(t)
	m.ZkSession.On("Exist", mock.Anything, true).Return(false, nil, nil)
	m.ZkSession.On("Exist", mock.Anything, true).Return(true, nil, nil)
	m.ZkClient.EXPECT().GetZkNode(constants.ZEUS_CONFIG_NODE_PATH, true).Return(test.GetMockZeusConfig(), nil).AnyTimes()
	mockPCDomain := mocks.NewMockPCDomain(ctrl)
	mockPCDomain.EXPECT().SetAZ(az).Return(nil)
	mockPCDomain.EXPECT().Create().Return(errors.New("configure-pc-connection: error while creating PC Domain"))

	c := &PCConnConfig{
		pcDomain: mockPCDomain,
	}

	// Act
	err := c.Configure(job)

	// Assert
	assert.NotNil(t, err)
	assert.Equal(t, "creating PC Domain: configure-pc-connection: error while creating PC Domain", err.Error())
}

func TestConfigure_Success(t *testing.T) {
	// Arrange
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	job := createJob()
	az := createAZ(job)
	m.ZkClient.EXPECT().GetZkNode(constants.ZEUS_CONFIG_NODE_PATH, true).Return(test.GetMockZeusConfig(), nil).AnyTimes()
	m.ZkSession.On("Create", mock.Anything, mock.Anything,
		int32(0), zk.WorldACL(zk.PermAll), true).Return("", nil).Times(6)
	m.ZkSession.On("Exist", mock.Anything, true).Return(false, nil, nil)
	m.ZkSession.On("Exist", mock.Anything, true).Return(true, nil, nil)
	m.ErgonService.On("TaskGet", TestTaskUuid).Return(TestTask, nil)
	m.ZkSession.On("Children", consts.AOSClusterCES, true).Return([]string{"uuid"}, nil, nil)
	m.ErgonService.On("TaskUpdate", mock.AnythingOfType("*ergon.TaskUpdateArg")).Return(nil, nil)
	mockPCDomain := mocks.NewMockPCDomain(ctrl)
	mockPCDomain.EXPECT().SetAZ(az).Return(nil)
	mockPCDomain.EXPECT().Create().Return(nil)

	// Act
	c := &PCConnConfig{
		pcDomain: mockPCDomain,
	}
	err := c.Configure(job)

	// Assert
	assert.Nil(t, err)
}

func TestUnconfigure_CESDeleteError(t *testing.T) {
	// Arrange
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	test.MockDRUnpairQuerySuccess(m)
	MockQueryResponse(m)
	job := createJob()
	m.IdfClient.EXPECT().GetEntityById(gomock.Any()).Return(nil, nil).AnyTimes()
	m.IdfClient.EXPECT().DeleteEntityById(gomock.Any()).Return(nil, nil).AnyTimes()
	m.ZkSession.On("Create", consts.DomainManagerCES+"/"+job.JobMetadata.RemoteClusterId,
		mock.Anything, int32(0), zk.WorldACL(zk.PermAll), true).Return("", nil).Once()
	m.ZkSession.On("Exist", consts.DomainManagerCES+"/"+job.JobMetadata.RemoteClusterId, true).
		Return(true, &zk.Stat{
			Version: 0,
		}, nil)
	m.ZkSession.On("Delete", consts.DomainManagerCES+"/"+job.JobMetadata.RemoteClusterId, int32(0), true).
		Return(errors.New("Error"))
	mockPCDomain := mocks.NewMockPCDomain(ctrl)
	mockPCDomain.EXPECT().Delete(gomock.Any()).Return(nil)
	c := &PCConnConfig{
		pcDomain: mockPCDomain,
	}

	// Act
	err := c.Unconfigure(job)

	// Assert
	assert.NotNil(t, err)
	assert.Equal(t, fmt.Errorf("deleting CES: %w", constants.ErrorClusterExternalStateDelete), err)
}

func TestUnconfigure_PCDomainDeleteError(t *testing.T) {
	// Arrange
	ctrl := gomock.NewController(t)
	m := test.MockSingletons(t)
	MockQueryResponse(m)
	defer ctrl.Finish()
	job := createJob()
	mockPCDomain := mocks.NewMockPCDomain(ctrl)
	m.IdfClient.EXPECT().GetEntityById(gomock.Any()).Return(nil, nil).AnyTimes()
	m.IdfClient.EXPECT().DeleteEntityById(gomock.Any()).Return(nil, nil).AnyTimes()
	mockPCDomain.EXPECT().Delete(TestUuid).Return(errors.New("error"))
	c := &PCConnConfig{
		pcDomain: mockPCDomain,
	}

	// Act
	err := c.Unconfigure(job)

	// Assert
	assert.NotNil(t, err)
	assert.Equal(t, "deleting PC Domain: error", err.Error())
}

func TestUnconfigureSuccess(t *testing.T) {
	// Arrange
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	job := createJob()
	m := test.MockSingletons(t)
	test.MockDRUnpairQuerySuccess(m)
	MockQueryResponse(m)
	m.IdfClient.EXPECT().GetEntityById(gomock.Any()).Return(nil, nil).AnyTimes()
	m.IdfClient.EXPECT().DeleteEntityById(gomock.Any()).Return(nil, nil).AnyTimes()
	m.ZkSession.On("Exist", consts.DomainManagerCES+"/"+job.JobMetadata.RemoteClusterId, mock.Anything).
		Return(true, &zk.Stat{
			Version: int32(0),
		}, nil).Once()
	m.ZkSession.On("Delete", consts.DomainManagerCES+"/"+job.JobMetadata.RemoteClusterId, int32(0), true).
		Return(nil)
	m.ErgonService.On("TaskGet", TestTaskUuid).Return(TestTask, nil)
	m.ZkSession.On("Children", consts.AOSClusterCES, true).Return([]string{"uuid"}, nil, nil)
	m.ErgonService.On("TaskUpdate", mock.AnythingOfType("*ergon.TaskUpdateArg")).Return(nil, nil)
	mockPCDomain := mocks.NewMockPCDomain(ctrl)
	mockPCDomain.EXPECT().Delete(gomock.Any()).Return(nil)
	c := &PCConnConfig{
		pcDomain: mockPCDomain,
	}

	// Act
	err := c.Unconfigure(job)

	// Assert
	assert.Nil(t, err)
}

func MockQueryResponse(m *test.MockServices) {
	entities := []*insights_interface.EntityWithMetric{
		{
			MetricDataList: []*insights_interface.MetricData{
				{
					Name: proto.String("uuid"),
					ValueList: []*insights_interface.TimeValuePair{
						{
							Value: &insights_interface.DataValue{
								ValueType: &insights_interface.DataValue_StrValue{
									StrValue: TestUuid,
								},
							},
						},
					},
				},
				{
					Name: proto.String(consts.CloudTrustUuidAzName),
					ValueList: []*insights_interface.TimeValuePair{
						{
							Value: &insights_interface.DataValue{
								ValueType: &insights_interface.DataValue_StrValue{
									StrValue: TestCloudTrustId,
								},
							},
						},
					},
				},
				{
					Name: proto.String(consts.RemoteConnectionUuidAzName),
					ValueList: []*insights_interface.TimeValuePair{
						{
							Value: &insights_interface.DataValue{
								ValueType: &insights_interface.DataValue_StrValue{
									StrValue: TestRemoteConnectionId,
								},
							},
						},
					},
				},
			},
			EntityGuid: &insights_interface.EntityGuid{
				EntityTypeName: proto.String("AvailabilityZonePhysical"),
				EntityId:       proto.String(TestUuid),
			},
		},
	}
	m.DbClient.On("Query", mock.AnythingOfType("*insights_interface.GetEntitiesWithMetricsArg")).Return(entities, nil)
}
