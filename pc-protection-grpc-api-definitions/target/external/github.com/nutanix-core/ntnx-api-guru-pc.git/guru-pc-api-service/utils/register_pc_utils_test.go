package utils

import (
	"errors"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/test"
	"testing"

	"github.com/golang/mock/gomock"
	"github.com/nutanix-core/go-cache/insights/insights_interface"
	"github.com/nutanix-core/go-cache/util-go/misc"
	"github.com/nutanix-core/ntnx-api-guru/services/idf"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
	"google.golang.org/protobuf/proto"
)

var (
	TestGetRemoteConnectionRet = &insights_interface.GetEntitiesRet{
		Entity: []*insights_interface.Entity{
			TestRemoteConnectionEntityClusterUuid,
		},
	}

	TestRemoteConnectionEntityClusterUuid = &insights_interface.Entity{
		EntityGuid: &insights_interface.EntityGuid{
			EntityTypeName: &consts.RemoteConnectionEntityType,
		},
		AttributeDataMap: []*insights_interface.NameTimeValuePair{
			{
				Name: proto.String("remote_connection_info.cluster_uuid"),
				Value: &insights_interface.DataValue{
					ValueType: &insights_interface.DataValue_StrValue{
						StrValue: "cluster_uuid",
					},
				},
			},
		},
	}
	TestRemoteConnectionEntityClusterUuidNil = &insights_interface.Entity{
		EntityGuid: &insights_interface.EntityGuid{
			EntityTypeName: &consts.RemoteConnectionEntityType,
		},
		AttributeDataMap: []*insights_interface.NameTimeValuePair{},
	}

	TestRemoteConnectionEntityFull = &insights_interface.Entity{
		EntityGuid: &insights_interface.EntityGuid{
			EntityTypeName: &consts.RemoteConnectionEntityType,
		},
		AttributeDataMap: []*insights_interface.NameTimeValuePair{
			{
				Name: proto.String("remote_connection_info.cluster_uuid"),
				Value: &insights_interface.DataValue{
					ValueType: &insights_interface.DataValue_StrValue{
						StrValue: "cluster_uuid",
					},
				},
			},
			{
				Name: proto.String("name"),
				Value: &insights_interface.DataValue{
					ValueType: &insights_interface.DataValue_StrValue{
						StrValue: "cluster_name",
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
			{
				Name: proto.String("remote_connection_info.node_address_list.ip"),
				Value: &insights_interface.DataValue{
					ValueType: &insights_interface.DataValue_StrList_{
						StrList: &insights_interface.DataValue_StrList{
							ValueList: []string{"ip1", "ip2"},
						},
					},
				},
			},
			{
				Name: proto.String("remote_connection_info.node_address_list.port"),
				Value: &insights_interface.DataValue{
					ValueType: &insights_interface.DataValue_Uint64List{
						Uint64List: &insights_interface.DataValue_UInt64List{
							ValueList: []uint64{80},
						},
					},
				},
			},
			{
				Name: proto.String("role"),
				Value: &insights_interface.DataValue{
					ValueType: &insights_interface.DataValue_Int64Value{
						Int64Value: 0,
					},
				},
			},
			{
				Name: proto.String("remote_connection_info.cluster_fqdn"),
				Value: &insights_interface.DataValue{
					ValueType: &insights_interface.DataValue_StrValue{
						StrValue: "fqdn",
					},
				},
			},
		},
	}

	TestError = errors.New("Test Error")
)

func TestMigrateAllRemoteConnectionToPcClusterExternalStateSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	// Setup mock service
	m := test.MockSingletons(t)
	m.ZkSession.On("Exist", mock.Anything, true).Return(true, nil, nil)
	// Setup mock expectations
	m.IdfSession.EXPECT().SendMsgWithTimeout(idf.GET_OPERATION, gomock.Any(),
		gomock.Any(), gomock.Any(), gomock.Any()).
		Do(
			func(operation string, arg interface{},
				response *insights_interface.GetEntitiesRet,
				backoff *misc.ExponentialBackoff, _ interface{}) {
				*response = *TestGetRemoteConnectionRet
			}).Return(nil).Times(1)

	m.ZkSession.On("Exist", mock.Anything, true).Return(false, nil, nil).Times(7)
	m.ZkSession.On("Create", mock.Anything, mock.Anything, mock.Anything, mock.Anything, true).Return("", nil).Times(6)

	err := MigrateAllRemoteConnectionToPcClusterExternalState()
	assert.Nil(t, err)
}

func TestMigrateAllRemoteConnectionToPcClusterExternalStateFailIDFCall(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	m := test.MockSingletons(t)

	// Setup mock expectations
	m.IdfSession.EXPECT().SendMsgWithTimeout(idf.GET_OPERATION, gomock.Any(),
		gomock.Any(), gomock.Any(), gomock.Any()).
		Return(TestError).Times(1)

	err := MigrateAllRemoteConnectionToPcClusterExternalState()
	assert.EqualError(t, err, consts.ErrorIdfRemoteConnectionGet.Error())
}

func TestMigrateAllRemoteConnectionToPcClusterExternalStateFailEmptyRCEntity(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	// Setup mock service
	m := test.MockSingletons(t)

	// Setup mock expectations
	m.IdfSession.EXPECT().SendMsgWithTimeout(idf.GET_OPERATION, gomock.Any(),
		gomock.Any(), gomock.Any(), gomock.Any()).Return(nil).Times(1)

	err := MigrateAllRemoteConnectionToPcClusterExternalState()
	assert.Nil(t, err)
}

func TestFetchRemoteClusterUuidFromEntitySuccess(t *testing.T) {

	clusterUuid, err := FetchRemoteClusterUuidFromEntity(TestRemoteConnectionEntityClusterUuid)
	assert.Nil(t, err)
	assert.Equal(t, "cluster_uuid", clusterUuid)
}

func TestFetchRemoteClusterUuidFromEntityFailClusterUuidNil(t *testing.T) {

	clusterUuid, err := FetchRemoteClusterUuidFromEntity(TestRemoteConnectionEntityClusterUuidNil)
	assert.EqualError(t, err, consts.ErrorRemoteClusterUuidNotFound.Error())
	assert.Equal(t, "", clusterUuid)
}

func TestAssembleAndCreateClusterExternalStateFromEntityFailCreateCluster(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	m.ZkSession.On("Exist", mock.Anything, true).Return(false, nil, TestError)
	err := AssembleAndCreateClusterExternalStateFromEntity(
		TestRemoteConnectionEntityClusterUuid)
	assert.EqualError(t, err, TestError.Error())
}

func TestAssembleAndCreateClusterExternalStateFromEntitySuccessCESAlreadyExist(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	m.ZkSession.On("Exist", mock.Anything, true).Return(true, nil, nil)
	err := AssembleAndCreateClusterExternalStateFromEntity(
		TestRemoteConnectionEntityClusterUuid)
	assert.Nil(t, err)
}

func TestAssemblePCClusterExternalStateEntitySuccess(t *testing.T) {

	expectedDTO := test.GetMockCES()

	clusterExternalStateDTO := AssemblePCClusterExternalStateEntity(
		TestRemoteConnectionEntityFull)

	assert.Equal(t, *expectedDTO, *clusterExternalStateDTO)
}

func TestIsRemoteConnectionOfPrismCentralTrue(t *testing.T) {
	isPrismCentral := IsRemoteConnectionOfPrismCentral(TestRemoteConnectionEntityFull)
	assert.True(t, isPrismCentral)
}

func TestIsRemoteConnectionOfPrismCentralFalse(t *testing.T) {
	rcEntity := &insights_interface.Entity{}
	isPrismCentral := IsRemoteConnectionOfPrismCentral(rcEntity)
	assert.False(t, isPrismCentral)
}
