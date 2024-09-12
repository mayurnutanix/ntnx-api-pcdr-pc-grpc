package idf

import (
	"encoding/json"
	"errors"
	"net/http"
	"ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/config"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/test"
	"ntnx-api-guru-pc/guru-pc-api-service/utils"
	"testing"
	"time"

	"github.com/golang/mock/gomock"
	common_models "github.com/nutanix-core/go-cache/api/pe"
	zk "github.com/nutanix-core/go-cache/go-zookeeper"
	"github.com/nutanix-core/go-cache/insights/insights_interface"
	"github.com/nutanix-core/go-cache/util-go/misc"
	"github.com/nutanix-core/ntnx-api-guru/services/constants"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
	"google.golang.org/protobuf/proto"
)

var (
	TestError = errors.New("Test error")
	TestStat  = &zk.Stat{
		Version: 1,
	}
	TestAttributeMap = []*insights_interface.NameTimeValuePair{
		{
			Name: proto.String("remote_connection_info.cluster_uuid"),
			Value: &insights_interface.DataValue{
				ValueType: &insights_interface.DataValue_StrValue{
					StrValue: "cluster_uuid",
				},
			},
		},
		{
			Name: proto.String("remote_connection_info.cluster_function"),
			Value: &insights_interface.DataValue{
				ValueType: &insights_interface.DataValue_Int64Value{
					Int64Value: 1,
				},
			},
		},
	}
	TestAttributeMapForPE = []*insights_interface.NameTimeValuePair{
		{
			Name: proto.String("remote_connection_info.cluster_uuid"),
			Value: &insights_interface.DataValue{
				ValueType: &insights_interface.DataValue_StrValue{
					StrValue: "cluster_uuid",
				},
			},
		},
		{
			Name: proto.String("remote_connection_info.cluster_function"),
			Value: &insights_interface.DataValue{
				ValueType: &insights_interface.DataValue_Int64Value{
					Int64Value: 0,
				},
			},
		},
	}
	TestFiredWatchInvalid = &insights_interface.FiredWatch{
		ChangedData: &insights_interface.FiredWatch_ChangedData{
			ChangedEntityData: &insights_interface.Entity{
				CreatedTimestampUsecs:  proto.Uint64(2234567890),
				ModifiedTimestampUsecs: proto.Uint64(1234567990),
			},
		},
	}
	TestFiredWatchCreate = &insights_interface.FiredWatch{
		ChangedData: &insights_interface.FiredWatch_ChangedData{
			ChangedEntityData: &insights_interface.Entity{
				EntityGuid: &insights_interface.EntityGuid{
					EntityTypeName: &consts.RemoteConnectionEntityType,
				},
				AttributeDataMap:       TestAttributeMap,
				CreatedTimestampUsecs:  proto.Uint64(1234567890),
				ModifiedTimestampUsecs: proto.Uint64(1234567890),
			},
		},
	}
	TestFiredWatchUpdate = &insights_interface.FiredWatch{
		ChangedData: &insights_interface.FiredWatch_ChangedData{
			ChangedEntityData: &insights_interface.Entity{
				EntityGuid: &insights_interface.EntityGuid{
					EntityTypeName: &consts.RemoteConnectionEntityType,
				},
				AttributeDataMap:       TestAttributeMap,
				CreatedTimestampUsecs:  proto.Uint64(1234567890),
				ModifiedTimestampUsecs: proto.Uint64(2234567990),
			},
		},
	}
	TestFiredWatchDelete = &insights_interface.FiredWatch{
		ChangedData: &insights_interface.FiredWatch_ChangedData{
			ChangedEntityData: &insights_interface.Entity{
				EntityGuid: &insights_interface.EntityGuid{
					EntityTypeName: &consts.RemoteConnectionEntityType,
				},
				AttributeDataMap:      TestAttributeMap,
				DeletedTimestampUsecs: proto.Uint64(1234567890),
			},
			PreviousEntityData: &insights_interface.Entity{
				EntityGuid: &insights_interface.EntityGuid{
					EntityTypeName: &consts.RemoteConnectionEntityType,
				},
				AttributeDataMap: TestAttributeMap,
			},
		},
	}
	TestFiredWatchDeletePERemoteConnection = &insights_interface.FiredWatch{
		ChangedData: &insights_interface.FiredWatch_ChangedData{
			ChangedEntityData: &insights_interface.Entity{
				EntityGuid: &insights_interface.EntityGuid{
					EntityTypeName: &consts.RemoteConnectionEntityType,
				},
				AttributeDataMap:      TestAttributeMap,
				DeletedTimestampUsecs: proto.Uint64(1234567890),
			},
			PreviousEntityData: &insights_interface.Entity{
				EntityGuid: &insights_interface.EntityGuid{
					EntityTypeName: &consts.RemoteConnectionEntityType,
					EntityId:       proto.String("entity_id"),
				},
				AttributeDataMap: TestAttributeMapForPE,
			},
		},
	}
	TestFiredWatchDeleteNilClusterUuid = &insights_interface.FiredWatch{
		ChangedData: &insights_interface.FiredWatch_ChangedData{
			ChangedEntityData: &insights_interface.Entity{
				EntityGuid: &insights_interface.EntityGuid{
					EntityTypeName: &consts.RemoteConnectionEntityType,
				},
				AttributeDataMap:      TestAttributeMap,
				DeletedTimestampUsecs: proto.Uint64(1234567890),
			},
			PreviousEntityData: &insights_interface.Entity{
				EntityGuid: &insights_interface.EntityGuid{
					EntityTypeName: &consts.RemoteConnectionEntityType,
				},
				AttributeDataMap: []*insights_interface.NameTimeValuePair{
					{
						Name: proto.String("remote_connection_info.cluster_function"),
						Value: &insights_interface.DataValue{
							ValueType: &insights_interface.DataValue_Int64Value{
								Int64Value: 1,
							},
						},
					},
				},
			},
		},
	}
	TestGetEntitiesRet = &insights_interface.GetEntitiesRet{
		Entity: []*insights_interface.Entity{
			{},
		},
	}
	TestZKPath = "/appliance/physical/connection/domain-manager/clusterexternalstate/cluster_uuid"
	TestZKstat = int32(1)
)

func TestWatchCallbackSuccessOnCreate(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockWatcherCallback := &RemoteConnectionWatcherCallback{}
	m := test.MockSingletons(t)

	// For initial check of completed zk path exists
	m.ZkSession.On("Exist", gomock.Any(), true).Return(false, nil, nil).
		Times(1)

	// For checking if zk path exists in create recursive method
	m.ZkSession.On("Exist", "/appliance", true).
		Return(true, nil, nil).Once()
	m.ZkSession.On("Exist", "/appliance/physical", true).Return(true, nil, nil).Once()
	m.ZkSession.On("Exist", "/appliance/physical/connection", true).Return(true, nil, nil).Once()
	m.ZkSession.On("Exist", "/appliance/physical/connection/domain-manager", true).Return(true, nil, nil).Once()
	m.ZkSession.On("Exist", "/appliance/physical/connection/domain-manager/clusterexternalstate", true).Return(true, nil, nil).Once()
	m.ZkSession.On("Exist", TestZKPath, true).Return(false, nil, nil).
		Times(1)
	m.ZkSession.On("Create", TestZKPath, gomock.Any(),
		gomock.Any(), gomock.Any(), true).Return("", nil).Times(1)
	m.ZkSession.On("Exist", TestZKPath, true).Return(true, nil, nil).
		Times(1)

	err := mockWatcherCallback.WatchCallback(TestFiredWatchCreate)
	assert.Nil(t, err)
}

func TestWatchCallbackSuccessOnUpdate(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockWatcherCallback := &RemoteConnectionWatcherCallback{}
	m := test.MockSingletons(t)

	// Setup mock expectations
	m.ZkSession.On("Get", TestZKPath, true).Return(nil, TestStat, nil).
		Times(1)
	m.ZkSession.On("Set", TestZKPath, mock.Anything, int32(1), true).
		Return(nil, nil).Times(1)

	err := mockWatcherCallback.WatchCallback(TestFiredWatchUpdate)
	assert.Nil(t, err)
}

func TestWatchCallbackSuccessOnDelete(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockWatcherCallback := &RemoteConnectionWatcherCallback{}
	m := test.MockSingletons(t)

	// Setup mock expectations
	m.ZkSession.On("Exist", TestZKPath, true).Return(true, TestStat, nil).
		Times(1)
	m.ZkSession.On("Delete", TestZKPath, TestZKstat, true).
		Return(nil).Times(1)

	err := mockWatcherCallback.WatchCallback(TestFiredWatchDelete)
	assert.Nil(t, err)
}

func TestWatchCallbackWhenPERemoteConnection(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockWatcherCallback := &RemoteConnectionWatcherCallback{}
	err := mockWatcherCallback.WatchCallback(TestFiredWatchDeletePERemoteConnection)
	assert.Nil(t, err)
}

func TestWatchCallbackFailureGetCallbackType(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockWatcherCallback := &RemoteConnectionWatcherCallback{}

	err := mockWatcherCallback.WatchCallback(TestFiredWatchInvalid)
	assert.NotNil(t, err)
}

func TestWatchCallbackFailureOnDeleteFetchUuid(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockWatcherCallback := &RemoteConnectionWatcherCallback{}
	originalFlag := *consts.EnableDeleteRCWatch
	*consts.EnableDeleteRCWatch = true
	err := mockWatcherCallback.WatchCallback(TestFiredWatchDeleteNilClusterUuid)
	assert.NotNil(t, err)
	*consts.EnableDeleteRCWatch = originalFlag
}

func TestRegisterWatchClientSucces(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	m := test.MockSingletons(t)
	m.IdfWatcher.EXPECT().Register().Return(nil).Times(1)

	err := RegisterWatchClient()
	assert.Nil(t, err)
}

func TestRegisterWatchClientFailure(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	m := test.MockSingletons(t)

	m.IdfWatcher.EXPECT().Register().Return(TestError).Times(4)

	err := RegisterWatchClient()
	assert.EqualError(t, err, "exiting after max number of retries : Test error")
}

func TestRegisterWatchesSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	m := test.MockSingletons(t)

	m.IdfWatcher.EXPECT().NewEntityWatchInfo(gomock.Any(), gomock.Any(),
		gomock.Any(), gomock.Any(), gomock.Any(), gomock.Any(),
		gomock.Any()).Return(nil).Times(1)
	m.IdfWatcher.EXPECT().CompositeWatchOnEntitiesOfType(gomock.Any(),
		true, true, true).Return(nil, nil).Times(1)

	RegisterWatches()
}

func TestRegisterRemoteConnectionWatchFailureCompositeWatchOnEntitiesOfType(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	m := test.MockSingletons(t)

	m.IdfWatcher.EXPECT().NewEntityWatchInfo(gomock.Any(), gomock.Any(),
		gomock.Any(), gomock.Any(), gomock.Any(), gomock.Any(),
		gomock.Any()).Return(nil).Times(1)
	m.IdfWatcher.EXPECT().CompositeWatchOnEntitiesOfType(gomock.Any(),
		true, true, true).Return(nil, TestError).Times(1)

	_, err := RegisterRemoteConnectionWatch(&consts.RemoteConnectionEntityType)
	assert.EqualError(t, err, TestError.Error())
}

func TestOnRemoteConnectionCreateFailureCreateCES(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	m := test.MockSingletons(t)

	m.ZkSession.On("Exist", TestZKPath, true).
		Return(false, nil, TestError).Times(1)

	err := onRemoteConnectionCreate(TestFiredWatchCreate.ChangedData.
		GetChangedEntityData())
	assert.EqualError(t, err, TestError.Error())
}

func TestOnRemoteConnectionCreateWhenPERemoteConnection(t *testing.T) {
	entity := &insights_interface.Entity{
		EntityGuid: &insights_interface.EntityGuid{
			EntityTypeName: &consts.RemoteConnectionEntityType,
			EntityId:       proto.String("entity_id"),
		},
		AttributeDataMap: TestAttributeMapForPE,
	}
	err := onRemoteConnectionCreate(entity)
	assert.Nil(t, err)
}

func TestOnRemoteConnectionUpdateFailureGet(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	m.ZkSession.On("Get", TestZKPath, true).Return(nil, nil, TestError).
		Times(1)

	err := onRemoteConnectionUpdate(TestFiredWatchUpdate.ChangedData.
		GetChangedEntityData())
	assert.EqualError(t, err, constants.ErrorClusterExternalStateGet.Error())
}

func TestOnRemoteConnectionUpdateWhenPERemoteConnection(t *testing.T) {
	entity := &insights_interface.Entity{
		EntityGuid: &insights_interface.EntityGuid{
			EntityTypeName: &consts.RemoteConnectionEntityType,
			EntityId:       proto.String("entity_id"),
		},
		AttributeDataMap: TestAttributeMapForPE,
	}
	err := onRemoteConnectionUpdate(entity)
	assert.Nil(t, err)
}

func TestOnRemoteConnectionDeleteFailureExist(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	m := test.MockSingletons(t)
	m.ZkSession.On("Exist", TestZKPath, true).Return(false, nil, TestError).
		Times(1)

	err := onRemoteConnectionDelete("cluster_uuid")
	assert.EqualError(t, err, constants.ErrorClusterExternalStateExistsCheck.Error())
}

func TestStartIDFWatchClientSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	m := test.MockSingletons(t)

	m.IdfWatcher.EXPECT().Start().Return(nil).AnyTimes()

	StartIDFWatchClient()
	time.Sleep(1 * time.Second)
}

func TestStartIDFWatchClientFailureAlreadyStarted(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	m := test.MockSingletons(t)

	m.IdfWatcher.EXPECT().Start().
		Return(insights_interface.ErrClientAlreadyStarted).Times(1)

	StartIDFWatchClient()
	time.Sleep(1 * time.Second)
}

func TestStartIDFWatchClientFailureToStart(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	m := test.MockSingletons(t)

	m.IdfWatcher.EXPECT().Start().Return(TestError).Times(1)
	m.IdfWatcher.EXPECT().Reregister().Return(nil, nil).Times(1)
	m.IdfWatcher.EXPECT().Start().Return(nil).AnyTimes()

	StartIDFWatchClient()
	time.Sleep(1 * time.Second)
}

func TestHandleWatchErrorFailureReregister(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	m := test.MockSingletons(t)

	m.IdfWatcher.EXPECT().Reregister().Return(nil, TestError).Times(1)
	m.IdfWatcher.EXPECT().Reregister().Return(nil, nil).Times(1)

	handleWatchError()
}

func TestGroupsCallSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	ipAddress := "ip_address"
	payload := &common_models.GroupsGetEntitiesRequest{}
	m := test.MockSingletons(t)

	groupsResp := &common_models.GroupsGetEntitiesResponse{}
	marshalGroupsResp, _ := json.Marshal(groupsResp)

	statusCode := 200
	m.RemoteRestClient.EXPECT().CallApi(ipAddress, consts.EnvoyPort, consts.GroupsUrl, http.MethodPost, gomock.Any(), gomock.Any(), gomock.Any(), nil, nil, nil, true).Return(marshalGroupsResp, &statusCode, nil).Times(1)
	resp, err := GroupsCall(ipAddress, payload)
	assert.Nil(t, err)
	assert.Equal(t, groupsResp, resp)
}

func TestGroupsCallFailure(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	ipAddress := "ip_address"
	payload := &common_models.GroupsGetEntitiesRequest{}
	m := test.MockSingletons(t)

	statusCode := 503
	m.RemoteRestClient.EXPECT().CallApi(ipAddress, consts.EnvoyPort, consts.GroupsUrl, http.MethodPost, gomock.Any(),
		gomock.Any(), gomock.Any(), nil, nil, nil, true).Return(nil, &statusCode, TestError).Times(1)
	resp, err := GroupsCall(ipAddress, payload)
	assert.EqualError(t, err, consts.ErrorRemoteClusterNotAvailable.Error())
	assert.Equal(t, resp, &common_models.GroupsGetEntitiesResponse{})
}

func TestGroupsCallUnmarshalFailure(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	ipAddress := "ip_address"
	payload := &common_models.GroupsGetEntitiesRequest{}
	m := test.MockSingletons(t)

	response := []byte{}
	statusCode := 200
	m.RemoteRestClient.EXPECT().CallApi(
		ipAddress, consts.EnvoyPort, consts.GroupsUrl, http.MethodPost,
		gomock.Any(), gomock.Any(), gomock.Any(), nil, nil, nil, true,
	).Return(response, &statusCode, nil).Times(1)
	resp, err := GroupsCall(ipAddress, payload)
	assert.NotNil(t, err)
	assert.Equal(t, resp, &common_models.GroupsGetEntitiesResponse{})
}

func TestFetchEntityFromIdfSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)

	m.IdfSession.EXPECT().SendMsgWithTimeout(consts.IdfGetOperation,
		gomock.Any(), gomock.Any(), consts.IdfBackoffForRetries,
		gomock.Any()).Do(
		func(operation string, arg interface{},
			response *insights_interface.GetEntitiesRet,
			backoff *misc.ExponentialBackoff, _ interface{}) {
			*response = *TestGetEntitiesRet
		}).Return(nil).Times(1)

	entity_name := "entity_name"
	entity_id := "entity_id"

	entity, err := FetchEntityFromIdf(entity_name, entity_id)
	assert.Nil(t, err)
	assert.Equal(t, &insights_interface.Entity{}, entity)
}

func TestFetchEntityFromIdfEntityNotFound(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)

	m.IdfSession.EXPECT().SendMsgWithTimeout(consts.IdfGetOperation,
		gomock.Any(), gomock.Any(), consts.IdfBackoffForRetries,
		gomock.Any()).Return(consts.ErrorIdfEntityNotFound).Times(1)

	entity_name := "entity_name"
	entity_id := "entity_id"

	entity, err := FetchEntityFromIdf(entity_name, entity_id)
	assert.EqualError(t, consts.ErrorIdfEntityNotFound, err.Error())
	assert.Nil(t, entity)
}

func TestFetchEntityFromIdfGetFailure(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)

	m.IdfSession.EXPECT().SendMsgWithTimeout(consts.IdfGetOperation,
		gomock.Any(), gomock.Any(), consts.IdfBackoffForRetries,
		gomock.Any()).Return(TestError).Times(1)

	entity_name := "entity_name"
	entity_id := "entity_id"

	entity, err := FetchEntityFromIdf(entity_name, entity_id)
	assert.ErrorIs(t, err, TestError)
	assert.Nil(t, entity)
}

func TestFetchEntityFromIdfNullEntity(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)

	m.IdfSession.EXPECT().SendMsgWithTimeout(consts.IdfGetOperation,
		gomock.Any(), gomock.Any(), consts.IdfBackoffForRetries,
		gomock.Any()).Return(nil).Times(1)

	entity_name := "entity_name"
	entity_id := "entity_id"

	entity, err := FetchEntityFromIdf(entity_name, entity_id)
	assert.EqualError(t, err, consts.ErrorIdfEntityNotFound.Error())
	assert.Nil(t, entity)
}

func TestUpdateDomainManagerEntityInIdfSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)

	m.IdfSession.EXPECT().SendMsgWithTimeout(consts.IdfUpdateOperation,
		gomock.Any(), gomock.Any(), consts.IdfBackoffForRetries,
		gomock.Any()).Return(nil).Times(1)

	domainManagerDetails := utils.InitialiseDomainManagerEntity()
	domainManagerNodesInfo := utils.InitialiseDomainManagerNodeInfo()

	domainManagerDetails.GetConfig().GetBootstrapConfig().EnvironmentInfo = &config.EnvironmentInfo{
		Type:             config.EnvironmentTypeMessage_ONPREM.Enum(),
		ProviderType:     config.ProviderTypeMessage_AZURE.Enum(),
		ProvisioningType: config.ProvisioningTypeMessage_NATIVE.Enum(),
	}

	err := UpdateDomainManagerEntityInIdf(nil, domainManagerDetails, domainManagerNodesInfo)
	assert.Nil(t, err)
}

func TestUpdateDomainManagerEntityInIdfFailure(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)

	m.IdfSession.EXPECT().SendMsgWithTimeout(consts.IdfUpdateOperation,
		gomock.Any(), gomock.Any(), consts.IdfBackoffForRetries,
		gomock.Any()).Return(TestError).Times(1)

	domainManagerDetails := utils.InitialiseDomainManagerEntity()
	domainManagerNodesInfo := utils.InitialiseDomainManagerNodeInfo()

	domainManagerDetails.GetConfig().GetBootstrapConfig().EnvironmentInfo = &config.EnvironmentInfo{
		Type:             config.EnvironmentTypeMessage_ONPREM.Enum(),
		ProviderType:     config.ProviderTypeMessage_AZURE.Enum(),
		ProvisioningType: config.ProvisioningTypeMessage_NATIVE.Enum(),
	}

	err := UpdateDomainManagerEntityInIdf(nil, domainManagerDetails, domainManagerNodesInfo)
	assert.ErrorIs(t, err, TestError)
}

func TestGetEntityById_Success(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	m := test.MockSingletons(t)
	args := BuildGetEntityArgById("TestUuid", "TestName")
	m.IdfClient.EXPECT().GetEntityById(args).Return(nil, nil)

	entity, err := GetEntityById("TestUuid", "TestName")
	assert.Nil(t, entity)
	assert.Nil(t, err)
}

func TestDeleteEntityById_GetEntityByIdError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	m := test.MockSingletons(t)
	getArgs := BuildGetEntityArgById("TestUuid", "TestName")
	m.IdfClient.EXPECT().GetEntityById(getArgs).Return(nil, assert.AnError)

	err := DeleteEntityById("TestUuid", "TestName")
	assert.Error(t, err)
}

func TestDeleteEntityById_DeleteEntityError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	m := test.MockSingletons(t)
	getArgs := BuildGetEntityArgById("TestUuid", "TestEntityName")
	m.IdfClient.EXPECT().GetEntityById(getArgs).Return(&insights_interface.Entity{
		CasValue: proto.Uint64(0),
	}, nil)
	deleteArgs := BuildDeleteEntityArgById("TestUuid", 1, "TestEntityName")
	m.IdfClient.EXPECT().DeleteEntityById(deleteArgs).Return(nil, assert.AnError)

	err := DeleteEntityById("TestUuid", "TestEntityName")
	assert.EqualError(t, assert.AnError, err.Error())
}

func TestDeleteEntityById_Success(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	m := test.MockSingletons(t)
	getArgs := BuildGetEntityArgById("TestUuid", "TestEntityName")
	m.IdfClient.EXPECT().GetEntityById(getArgs).Return(&insights_interface.Entity{
		CasValue: proto.Uint64(0),
	}, nil)
	deleteArgs := BuildDeleteEntityArgById("TestUuid", 1, "TestEntityName")
	m.IdfClient.EXPECT().DeleteEntityById(deleteArgs).Return(nil, nil)

	err := DeleteEntityById("TestUuid", "TestEntityName")
	assert.Nil(t, err)
}

func TestCreateDataArgInt32(t *testing.T) {
	int32Value := int32(1)
	dataArg := createDataArg("intValue", int32Value)
	assert.Equal(t, int64(int32Value), dataArg.GetAttributeData().GetValue().GetInt64Value())
	assert.Equal(t, "intValue", dataArg.GetAttributeData().GetName())
}

func TestCreateDataArgInt64(t *testing.T) {
	int64Value := int64(1)
	dataArg := createDataArg("intValue", int64Value)
	assert.Equal(t, int64Value, dataArg.GetAttributeData().GetValue().GetInt64Value())
	assert.Equal(t, "intValue", dataArg.GetAttributeData().GetName())
}

func TestCreateDataArgBool(t *testing.T) {
	boolValue := true
	dataArg := createDataArg("boolValue", boolValue)
	assert.Equal(t, boolValue, dataArg.GetAttributeData().GetValue().GetBoolValue())
	assert.Equal(t, "boolValue", dataArg.GetAttributeData().GetName())
}

func TestCreateDataArgUint64(t *testing.T) {
	uint64Value := uint64(1)
	dataArg := createDataArg("uint64Value", uint64Value)
	assert.Equal(t, uint64Value, dataArg.GetAttributeData().GetValue().GetUint64Value())
	assert.Equal(t, "uint64Value", dataArg.GetAttributeData().GetName())
}

func TestCreateDataArgFloat32(t *testing.T) {
	float32Value := float32(1.0)
	dataArg := createDataArg("float32Value", float32Value)
	assert.Equal(t, float32Value, dataArg.GetAttributeData().GetValue().GetFloatValue())
	assert.Equal(t, "float32Value", dataArg.GetAttributeData().GetName())
}

func TestCreateDataArgFloat64(t *testing.T) {
	float64Value := float64(1.0)
	dataArg := createDataArg("float64Value", float64Value)
	assert.Equal(t, float64Value, dataArg.GetAttributeData().GetValue().GetDoubleValue())
	assert.Equal(t, "float64Value", dataArg.GetAttributeData().GetName())
}

func TestCreateDataArgStringList(t *testing.T) {
	stringList := []string{"value1", "value2"}
	dataArg := createDataArg("stringList", stringList)
	assert.Equal(t, stringList, dataArg.GetAttributeData().GetValue().GetStrList().GetValueList())
	assert.Equal(t, "stringList", dataArg.GetAttributeData().GetName())
}

func TestCreateDataArgInt64List(t *testing.T) {
	int64List := []int64{1, 2}
	dataArg := createDataArg("int64List", int64List)
	assert.Equal(t, int64List, dataArg.GetAttributeData().GetValue().GetInt64List().GetValueList())
	assert.Equal(t, "int64List", dataArg.GetAttributeData().GetName())
}

func TestCreateDataArgBoolList(t *testing.T) {
	boolList := []bool{true, false}
	dataArg := createDataArg("boolList", boolList)
	assert.Equal(t, boolList, dataArg.GetAttributeData().GetValue().GetBoolList().GetValueList())
	assert.Equal(t, "boolList", dataArg.GetAttributeData().GetName())
}

func TestCreateDataArgUint64List(t *testing.T) {
	uint64List := []uint64{1, 2}
	dataArg := createDataArg("uint64List", uint64List)
	assert.Equal(t, uint64List, dataArg.GetAttributeData().GetValue().GetUint64List().GetValueList())
	assert.Equal(t, "uint64List", dataArg.GetAttributeData().GetName())
}

func TestCreateDataArgFloat32Lis(t *testing.T) {
	float32List := []float32{1.0, 2.0}
	dataArg := createDataArg("float32List", float32List)
	assert.Equal(t, float32List, dataArg.GetAttributeData().GetValue().GetFloatList().GetValueList())
	assert.Equal(t, "float32List", dataArg.GetAttributeData().GetName())
}

func TestCreateDataArgFloat64List(t *testing.T) {
	float64List := []float64{1.0, 2.0}
	dataArg := createDataArg("float64List", float64List)
	assert.Equal(t, float64List, dataArg.GetAttributeData().GetValue().GetDoubleList().GetValueList())
	assert.Equal(t, "float64List", dataArg.GetAttributeData().GetName())
}

func TestCreateDataArgByteList(t *testing.T) {
	byteList := [][]byte{[]byte{1, 2}, []byte{3, 4}}
	dataArg := createDataArg("byteList", byteList)
	assert.Equal(t, byteList, dataArg.GetAttributeData().GetValue().GetBytesList().GetValueList())
	assert.Equal(t, "byteList", dataArg.GetAttributeData().GetName())
}

func TestCreateDataArgDefault(t *testing.T) {
	mapValue := map[string]string{"key": "value"}
	dataArg := createDataArg("mapValue", mapValue)
	assert.Nil(t, dataArg)
}

func TestCreateIdfUpdateEntityArgForDomainManagerNilIdfEntity(t *testing.T) {

	domainManagerDetails := utils.InitialiseDomainManagerEntity()
	domainManagerEntity := &insights_interface.Entity{
		CasValue: proto.Uint64(0),
		EntityGuid: &insights_interface.EntityGuid{
			EntityId: proto.String("entity_id"),
		},
	}
	entityArg := createIdfUpdateEntityArgForDomainManager(domainManagerEntity, domainManagerDetails, nil)
	assert.NotNil(t, entityArg)
}

func TestMakeIpRange(t *testing.T) {
	beginIp := "beginIp"
	endIp := "endIp"
	ipRange := makeIpRange(beginIp, endIp)
	assert.Equal(t, beginIp+" "+endIp, ipRange)
}
