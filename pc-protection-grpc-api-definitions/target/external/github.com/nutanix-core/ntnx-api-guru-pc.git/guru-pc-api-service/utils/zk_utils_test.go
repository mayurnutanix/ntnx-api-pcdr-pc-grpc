package utils

import (
	"errors"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/test"
	"testing"

	"github.com/golang/mock/gomock"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"

	zk "github.com/nutanix-core/go-cache/go-zookeeper"
)

func TestUpdateOrCreateZkNodeExistZkError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	path := consts.DomainManagerCES
	m.ZkSession.On("Exist", path, true).Return(false, nil, errors.New("error"))

	err := UpdateOrCreateZkNode(TestParentTaskUuid, path, true)

	assert.Equal(t, "checking for zk node: error", err.Error())
}

func TestUpdateOrCreateZkNodeSetZkError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	path := consts.DomainManagerCES
	stat := zk.Stat{
		Version: 0,
	}
	m.ZkSession.On("Exist", path, true).Return(true, &stat, nil)
	m.ZkSession.On("Set", path, TestParentTaskUuid, stat.Version, true).Return(&stat, errors.New("error"))

	err := UpdateOrCreateZkNode(TestParentTaskUuid, path, true)

	assert.Equal(t, "updating zk node: error", err.Error())
}

func TestUpdateOrCreateZkNodeSetZkSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	path := consts.DomainManagerCES
	stat := zk.Stat{
		Version: 0,
	}
	m.ZkSession.On("Exist", path, true).Return(true, &stat, nil)
	m.ZkSession.On("Set", path, TestParentTaskUuid, stat.Version, true).Return(&stat, nil)

	err := UpdateOrCreateZkNode(TestParentTaskUuid, path, true)

	assert.Nil(t, err)
}

func TestUpdateOrCreateZkNodeCreateZkNodeError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	path := consts.DomainManagerCES
	var zero int32 = 0
	m.ZkSession.On("Exist", path, true).Return(false, nil, nil)
	m.ZkSession.On("Create", path, TestParentTaskUuid, zero, zk.WorldACL(zk.PermAll),
		true).Return("", errors.New("error"))

	err := UpdateOrCreateZkNode(TestParentTaskUuid, path, true)

	assert.Equal(t, "creating zk node: creating zk node: error", err.Error())
}

func TestUpdateOrCreateZkNodeCreateFailure(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	path := consts.DomainManagerCES
	var zero int32 = 0
	m.ZkSession.On("Exist", path, true).Return(false, nil, nil)
	m.ZkSession.On("Create", path, TestParentTaskUuid, zero, zk.WorldACL(zk.PermAll),
		true).Return("", TestError)

	err := UpdateOrCreateZkNode(TestParentTaskUuid, path, false)

	assert.ErrorIs(t, err, TestError)
}

func TestUpdateOrCreateZkNodeCreateZkNodeSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	path := consts.DomainManagerCES
	var zero int32 = 0
	m.ZkSession.On("Exist", path, true).Return(false, nil, nil)
	m.ZkSession.On("Create", path, TestParentTaskUuid, zero, zk.WorldACL(zk.PermAll),
		true).Return("", nil)

	err := UpdateOrCreateZkNode(TestParentTaskUuid, path, false)

	assert.Nil(t, err)
}

func TestDeleteZkNodeFailure(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	path := consts.DomainManagerCES
	stat := zk.Stat{
		Version: 0,
	}
	m.ZkSession.On("Exist", path, true).Return(true, &stat, nil)
	m.ZkSession.On("Delete", path, stat.Version, true).Return(TestError)

	err := DeleteZkNode(path)
	assert.ErrorIs(t, err, TestError)
}

func TestDeleteZkNode_Success(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	path := consts.DomainManagerCES
	stat := zk.Stat{
		Version: 0,
	}
	m.ZkSession.On("Exist", path, true).Return(true, &stat, nil)
	m.ZkSession.On("Delete", path, stat.Version, true).Return(nil)

	err := DeleteZkNode(path)

	assert.Nil(t, err)
}

func TestDeleteZkNode_ExistZkError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	path := consts.DomainManagerCES
	errTest := errors.New("error")
	m.ZkSession.On("Exist", path, true).Return(false, nil, errTest)

	err := DeleteZkNode(path)

	assert.Equal(t, errTest, err)
}

func TestDeleteZkNode_ExistZkFalse(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	path := consts.DomainManagerCES
	m.ZkSession.On("Exist", path, true).Return(false, nil, nil)

	err := DeleteZkNode(path)

	assert.NotNil(t, err)
	assert.Equal(t, consts.ErrorZkNodeNotExists, err)
}

// When only the leaf node needs to be created
func TestCreateZkNodeRecursiveLeafNode(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	path := consts.DomainManagerCES
	var zero int32 = 0
	m.ZkSession.On("Create", path, TestParentTaskUuid, zero, zk.WorldACL(zk.PermAll),
		true).Return("", zk.ErrNoNode).Times(1)
	m.ZkSession.On("Exist", mock.Anything, true).Return(true, nil, nil).Times(4)
	m.ZkSession.On("Exist", path, true).Return(false, nil, nil).Times(1)
	m.ZkSession.On("Create", path, TestParentTaskUuid, zero, zk.WorldACL(zk.PermAll),
		true).Return("", nil).Times(1)

	err := createZkNodeRecursive(TestParentTaskUuid, path)
	assert.Nil(t, err)
}

// When all nodes needs to be created
func TestCreateZkNodeRecursiveAllNode(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	path := consts.DomainManagerCES
	var zero int32 = 0
	m.ZkSession.On("Create", path, TestParentTaskUuid, zero, zk.WorldACL(zk.PermAll),
		true).Return("", zk.ErrNoNode).Times(1)
	m.ZkSession.On("Exist", mock.Anything, true).Return(false, nil, nil).Times(5)
	m.ZkSession.On("Create", mock.Anything, mock.Anything, zero, zk.WorldACL(zk.PermAll),
		true).Return("", nil).Times(5)

	err := createZkNodeRecursive(TestParentTaskUuid, path)
	assert.Nil(t, err)
}

func TestCreateZkNodeRecursiveAlreadyExist(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	path := consts.DomainManagerCES
	m.ZkSession.On("Create", path, TestParentTaskUuid, int32(0), zk.WorldACL(zk.PermAll),
		true).Return("", nil).Times(1)

	err := createZkNodeRecursive(TestParentTaskUuid, path)
	assert.Nil(t, err)
}

func TestCreateZkNodeRecursiveErrorInExist(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	path := consts.DomainManagerCES
	var zero int32 = 0
	m.ZkSession.On("Create", path, TestParentTaskUuid, zero, zk.WorldACL(zk.PermAll),
		true).Return("", zk.ErrNoNode).Times(1)
	m.ZkSession.On("Exist", mock.Anything, true).Return(false, nil, TestError).Times(1)

	err := createZkNodeRecursive(TestParentTaskUuid, path)
	assert.ErrorIs(t, err, TestError)
}

// When error in creating non leaf zk node
func TestCreateZkNodeRecursiveErrorInCreateAll(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	path := consts.DomainManagerCES
	var zero int32 = 0
	m.ZkSession.On("Create", path, TestParentTaskUuid, zero, zk.WorldACL(zk.PermAll),
		true).Return("", zk.ErrNoNode).Times(1)
	m.ZkSession.On("Exist", mock.Anything, true).Return(false, nil, nil).Times(1)
	m.ZkSession.On("Create", mock.Anything, mock.Anything, zero, zk.WorldACL(zk.PermAll),
		true).Return("", TestError).Times(1)

	err := createZkNodeRecursive(TestParentTaskUuid, path)
	assert.ErrorIs(t, err, TestError)
}

// When error in creating leaf zk node
func TestCreateZkNodeRecursiveErrorInCreateLeaf(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	path := consts.DomainManagerCES
	var zero int32 = 0
	m.ZkSession.On("Create", path, TestParentTaskUuid, zero, zk.WorldACL(zk.PermAll),
		true).Return("", zk.ErrNoNode).Times(1)
	m.ZkSession.On("Exist", mock.Anything, true).Return(true, nil, nil).Times(4)
	m.ZkSession.On("Exist", path, true).Return(false, nil, nil).Times(1)
	m.ZkSession.On("Create", mock.Anything, mock.Anything, zero, zk.WorldACL(zk.PermAll),
		true).Return("", TestError).Times(1)

	err := createZkNodeRecursive(TestParentTaskUuid, path)
	assert.ErrorIs(t, err, TestError)
}
