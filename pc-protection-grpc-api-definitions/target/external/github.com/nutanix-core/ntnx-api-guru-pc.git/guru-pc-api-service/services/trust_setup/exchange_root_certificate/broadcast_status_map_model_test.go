package exchange_root_certificate

import (
	"errors"
	"ntnx-api-guru-pc/guru-pc-api-service/test"
	"testing"

	"github.com/golang/mock/gomock"
	zk "github.com/nutanix-core/go-cache/go-zookeeper"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
)

func TestNewBroadcastStatusMap(t *testing.T) {
	bsm := NewBroadcastStatusMap("123")
	assert.Equal(t, "123", bsm.PcUuid)
}

func TestGetBroadcastStatusMap(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	pcUuid := "123"
	m.ZkSession.On("Get", BroadcastZkPath+pcUuid, true).Return(nil, nil, zk.ErrNoNode)
	mapRet := NewBroadcastStatusMap("123")
	bsm, err := GetBroadcastStatusMap(pcUuid)
	assert.Equal(t, mapRet, bsm)
	assert.Nil(t, err)
}

func TestGetBroadcastStatusMapMarshalError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	pcUuid := "123"
	m.ZkSession.On("Get", BroadcastZkPath+pcUuid, true).Return([]byte("{x:"), nil, nil)
	bsm, err := GetBroadcastStatusMap(pcUuid)
	assert.Nil(t, bsm)
	assert.NotNil(t, err)
}

func TestWasBroadcastedTrue(t *testing.T) {
	bsm := NewBroadcastStatusMap("123")
	bsm.StatusMap["456"] = true
	res := bsm.WasBroadcasted("456")
	assert.Equal(t, true, res)
}

func TestWasBroadcastedFalse(t *testing.T) {
	bsm := NewBroadcastStatusMap("123")
	res := bsm.WasBroadcasted("456")
	assert.Equal(t, false, res)
}

func TestUpdatePeStatus(t *testing.T) {
	bsm := NewBroadcastStatusMap("123")
	bsm.UpdatePeStatus("456", true)
	assert.True(t, bsm.StatusMap["456"])
}

func TestRemovePeStatus(t *testing.T) {
	bsm := NewBroadcastStatusMap("123")
	bsm.UpdatePeStatus("456", true)
	bsm.RemovePeStatus("456")
	status, ok := bsm.StatusMap["456"]
	assert.False(t, status)
	assert.False(t, ok)
}

func TestCommitProgressToZkUpdateOrCreateError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	pcUuid := "123"
	bsm := NewBroadcastStatusMap(pcUuid)
	m.ZkSession.On("Exist", BroadcastZkPath+pcUuid, true).Return(false, nil, errors.New("error")).Once()

	err := bsm.CommitProgressToZk(false, false, Post)

	assert.Equal(t, "checking for zk node: error", err.Error())
}

func TestCommitProgressToZkCreate(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	pcUuid := "123"
	bsm := NewBroadcastStatusMap(pcUuid)
	m.ZkSession.On("Exist", BroadcastZkPath+pcUuid, true).Return(false, nil, nil).Once()
	m.ZkSession.On("Create", BroadcastZkPath+pcUuid, mock.Anything,
		int32(0), zk.WorldACL(zk.PermAll), true).Return("", nil).Once()

	err := bsm.CommitProgressToZk(false, false, Post)

	assert.Nil(t, err)
}

func TestCommitProgressToZkStartCompleteError(t *testing.T) {
	pcUuid := "123"
	bsm := NewBroadcastStatusMap(pcUuid)
	err := bsm.CommitProgressToZk(true, true, Post)
	assert.NotNil(t, err)
}

func TestCommitProgressToZkDeleteZk(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	pcUuid := "123"
	bsm := NewBroadcastStatusMap(pcUuid)
	stat := zk.Stat{}
	m.ZkSession.On("Exist", BroadcastZkPath+pcUuid, true).Return(true, &stat, nil).Once()
	m.ZkSession.On("Delete", BroadcastZkPath+pcUuid, stat.Version, true).Return(nil).Once()
	err := bsm.CommitProgressToZk(false, true, Delete)
	assert.Nil(t, err)
}

func TestCommitProgressToZkDeleteZkError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	pcUuid := "123"
	bsm := NewBroadcastStatusMap(pcUuid)
	m.ZkSession.On("Exist", BroadcastZkPath+pcUuid, true).Return(false, nil, nil).Once()
	err := bsm.CommitProgressToZk(false, true, Delete)
	assert.NotNil(t, err)
}
