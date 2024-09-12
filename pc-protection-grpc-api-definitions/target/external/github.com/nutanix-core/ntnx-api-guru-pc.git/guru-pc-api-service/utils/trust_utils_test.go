package utils

import (
	"errors"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/test"
	"testing"

	"github.com/golang/mock/gomock"
	"github.com/golang/protobuf/proto"
	"github.com/nutanix-core/go-cache/prism"
	zeus_config "github.com/nutanix-core/go-cache/zeus/config"
	common_consts "github.com/nutanix-core/ntnx-api-guru/services/constants"
	"github.com/stretchr/testify/assert"
)

func TestFetchTrustZkConfig(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	clusterUuid := "ClusterUuid"
	m.ZkClient.EXPECT().GetZkNode(common_consts.TRUST_SETUP_NODE_PATH+"/"+clusterUuid, true).Return(nil, nil)
	res, err := FetchTrustZkConfig(clusterUuid)
	assert.NotNil(t, res)
	assert.Nil(t, err)
}

func TestFetchTrustZkConfigFailure_GetZkNodeFailed(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	clusterUuid := "ClusterUuid"
	m.ZkClient.EXPECT().GetZkNode(common_consts.TRUST_SETUP_NODE_PATH+"/"+clusterUuid, true).Return(nil, TestError)
	res, err := FetchTrustZkConfig(clusterUuid)
	assert.Nil(t, res)
	assert.EqualError(t, err, TestError.Error())
}

func TestFetchTrustZkConfigFailure_UnmarshalFailed(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	clusterUuid := "ClusterUuid"
	m.ZkClient.EXPECT().GetZkNode(common_consts.TRUST_SETUP_NODE_PATH+"/"+clusterUuid, true).Return([]byte{1,1,2}, nil)
	res, err := FetchTrustZkConfig(clusterUuid)
	assert.Nil(t, res)
	assert.NotNil(t, err)
}

func TestFetchIpAddressFromTrustZkNode_GetZkNodeFailed(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	clusterUuid := "ClusterUuid"
	m.ZkClient.EXPECT().GetZkNode(common_consts.TRUST_SETUP_NODE_PATH+"/"+clusterUuid, true).Return(nil, TestError)
	res, err := FetchIpAddressFromTrustZkNode(clusterUuid)
	assert.Equal(t, res, "")
	assert.EqualError(t, err, consts.ErrorTrustZkNodeNotFoundFailure.Error())
}

func TestFetchIpAddressFromTrustZkNodezkDataNil(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	clusterUuid := "ClusterUuid"
	m.ZkClient.EXPECT().GetZkNode(common_consts.TRUST_SETUP_NODE_PATH+"/"+clusterUuid, true).Return(nil, nil)
	res, err := FetchIpAddressFromTrustZkNode(clusterUuid)
	assert.Equal(t, "", res)
	assert.Equal(t, consts.ErrorTrustZkNodeIncompleteDataFailure, err)
}

func TestFetchIpAddressFromTrustZkNode(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	clusterUuid := "ClusterUuid"
	var addressList []string
	addressList = append(addressList, "address")
	trustState := new(prism.ClusterExternalState)
	trustState.ClusterDetails = &prism.ClusterDetails{
		ContactInfo: &zeus_config.ConfigurationProto_NetworkEntity{
			AddressList: addressList,
		},
	}
	marshalGetZkResp, _ := proto.Marshal(trustState)
	m.ZkClient.EXPECT().GetZkNode(common_consts.TRUST_SETUP_NODE_PATH+"/"+clusterUuid, true).Return(marshalGetZkResp, nil)
	res, err := FetchIpAddressFromTrustZkNode(clusterUuid)
	assert.Equal(t, "address", res)
	assert.Nil(t, err)
}

func TestFetchHostingPEIdFromTrustZkGetChildrenError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	m.ZkClient.EXPECT().GetChildren(common_consts.TRUST_SETUP_NODE_PATH, true).Return(nil, errors.New("error"))
	res, err := FetchHostingPEIdFromTrustZk()
	assert.Equal(t, "", res)
	assert.Equal(t, consts.ErrorTrustZkNodeNotFoundFailure, err)
}

func TestFetchHostingPEIdFromTrustZkNoChildren(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	var trustList []string
	m.ZkClient.EXPECT().GetChildren(common_consts.TRUST_SETUP_NODE_PATH, true).Return(trustList, nil)
	res, err := FetchHostingPEIdFromTrustZk()
	assert.Equal(t, "", res)
	assert.Equal(t, consts.ErrorInvalidTrustZkNode, err)
}

func TestFetchHostingPEIdFromTrustZk(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	var trustList []string
	trustList = append(trustList, "trust-list")
	m.ZkClient.EXPECT().GetChildren(common_consts.TRUST_SETUP_NODE_PATH, true).Return(trustList, nil)
	res, err := FetchHostingPEIdFromTrustZk()
	assert.Equal(t, "trust-list", res)
	assert.Nil(t, err)
}
