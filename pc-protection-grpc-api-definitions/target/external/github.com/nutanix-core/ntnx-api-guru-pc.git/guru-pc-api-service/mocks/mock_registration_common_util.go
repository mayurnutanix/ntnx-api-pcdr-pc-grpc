// Code generated by MockGen. DO NOT EDIT.
// Source: guru-pc-api-service/services/register_aos/registration_common/registration_common_util.go

// Package mocks is a generated GoMock package.
package mocks

import (
	ntnxApiGuruError "ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/error"
	management "ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/management"
	models "ntnx-api-guru-pc/guru-pc-api-service/models"
	reflect "reflect"

	gomock "github.com/golang/mock/gomock"
	net "github.com/nutanix-core/go-cache/util-slbufs/util/sl_bufs/net"
)

// MockRegistrationCommonUtilIfc is a mock of RegistrationCommonUtilIfc interface.
type MockRegistrationCommonUtilIfc struct {
	ctrl     *gomock.Controller
	recorder *MockRegistrationCommonUtilIfcMockRecorder
}

// MockRegistrationCommonUtilIfcMockRecorder is the mock recorder for MockRegistrationCommonUtilIfc.
type MockRegistrationCommonUtilIfcMockRecorder struct {
	mock *MockRegistrationCommonUtilIfc
}

// NewMockRegistrationCommonUtilIfc creates a new mock instance.
func NewMockRegistrationCommonUtilIfc(ctrl *gomock.Controller) *MockRegistrationCommonUtilIfc {
	mock := &MockRegistrationCommonUtilIfc{ctrl: ctrl}
	mock.recorder = &MockRegistrationCommonUtilIfcMockRecorder{mock}
	return mock
}

// EXPECT returns an object that allows the caller to indicate expected use.
func (m *MockRegistrationCommonUtilIfc) EXPECT() *MockRegistrationCommonUtilIfcMockRecorder {
	return m.recorder
}

// CreateChildTasks mocks base method.
func (m *MockRegistrationCommonUtilIfc) CreateChildTasks(arg0 *models.Job) error {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "CreateChildTasks", arg0)
	ret0, _ := ret[0].(error)
	return ret0
}

// CreateChildTasks indicates an expected call of CreateChildTasks.
func (mr *MockRegistrationCommonUtilIfcMockRecorder) CreateChildTasks(arg0 interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "CreateChildTasks", reflect.TypeOf((*MockRegistrationCommonUtilIfc)(nil).CreateChildTasks), arg0)
}

// CreateParentRegistrationTask mocks base method.
func (m *MockRegistrationCommonUtilIfc) CreateParentRegistrationTask(arg0, arg1 string, arg2 *net.RpcRequestContext, arg3 string) ([]byte, []*ntnxApiGuruError.AppMessage, error) {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "CreateParentRegistrationTask", arg0, arg1, arg2, arg3)
	ret0, _ := ret[0].([]byte)
	ret1, _ := ret[1].([]*ntnxApiGuruError.AppMessage)
	ret2, _ := ret[2].(error)
	return ret0, ret1, ret2
}

// CreateParentRegistrationTask indicates an expected call of CreateParentRegistrationTask.
func (mr *MockRegistrationCommonUtilIfcMockRecorder) CreateParentRegistrationTask(arg0, arg1, arg2, arg3 interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "CreateParentRegistrationTask", reflect.TypeOf((*MockRegistrationCommonUtilIfc)(nil).CreateParentRegistrationTask), arg0, arg1, arg2, arg3)
}

// CreateRegisterApiResponseDTO mocks base method.
func (m *MockRegistrationCommonUtilIfc) CreateRegisterApiResponseDTO(job *models.Job) *management.RegisterRet {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "CreateRegisterApiResponseDTO", job)
	ret0, _ := ret[0].(*management.RegisterRet)
	return ret0
}

// CreateRegisterApiResponseDTO indicates an expected call of CreateRegisterApiResponseDTO.
func (mr *MockRegistrationCommonUtilIfcMockRecorder) CreateRegisterApiResponseDTO(job interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "CreateRegisterApiResponseDTO", reflect.TypeOf((*MockRegistrationCommonUtilIfc)(nil).CreateRegisterApiResponseDTO), job)
}

// CreateRegistrationMarkerNode mocks base method.
func (m *MockRegistrationCommonUtilIfc) CreateRegistrationMarkerNode(arg0 *models.Job) error {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "CreateRegistrationMarkerNode", arg0)
	ret0, _ := ret[0].(error)
	return ret0
}

// CreateRegistrationMarkerNode indicates an expected call of CreateRegistrationMarkerNode.
func (mr *MockRegistrationCommonUtilIfcMockRecorder) CreateRegistrationMarkerNode(arg0 interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "CreateRegistrationMarkerNode", reflect.TypeOf((*MockRegistrationCommonUtilIfc)(nil).CreateRegistrationMarkerNode), arg0)
}

// DeleteRegistrationMarkerNode mocks base method.
func (m *MockRegistrationCommonUtilIfc) DeleteRegistrationMarkerNode(arg0 *models.Job) {
	m.ctrl.T.Helper()
	m.ctrl.Call(m, "DeleteRegistrationMarkerNode", arg0)
}

// DeleteRegistrationMarkerNode indicates an expected call of DeleteRegistrationMarkerNode.
func (mr *MockRegistrationCommonUtilIfcMockRecorder) DeleteRegistrationMarkerNode(arg0 interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "DeleteRegistrationMarkerNode", reflect.TypeOf((*MockRegistrationCommonUtilIfc)(nil).DeleteRegistrationMarkerNode), arg0)
}

// PrepareRegisterAosJob mocks base method.
func (m *MockRegistrationCommonUtilIfc) PrepareRegisterAosJob(arg0 *models.RegisterAosRequestDetails, arg1 []byte, arg2 *net.RpcRequestContext) *models.Job {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "PrepareRegisterAosJob", arg0, arg1, arg2)
	ret0, _ := ret[0].(*models.Job)
	return ret0
}

// PrepareRegisterAosJob indicates an expected call of PrepareRegisterAosJob.
func (mr *MockRegistrationCommonUtilIfcMockRecorder) PrepareRegisterAosJob(arg0, arg1, arg2 interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "PrepareRegisterAosJob", reflect.TypeOf((*MockRegistrationCommonUtilIfc)(nil).PrepareRegisterAosJob), arg0, arg1, arg2)
}

// ProcessRegistrationErrorState mocks base method.
func (m *MockRegistrationCommonUtilIfc) ProcessRegistrationErrorState(arg0 *models.Job, arg1 string, arg2 int) {
	m.ctrl.T.Helper()
	m.ctrl.Call(m, "ProcessRegistrationErrorState", arg0, arg1, arg2)
}

// ProcessRegistrationErrorState indicates an expected call of ProcessRegistrationErrorState.
func (mr *MockRegistrationCommonUtilIfcMockRecorder) ProcessRegistrationErrorState(arg0, arg1, arg2 interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "ProcessRegistrationErrorState", reflect.TypeOf((*MockRegistrationCommonUtilIfc)(nil).ProcessRegistrationErrorState), arg0, arg1, arg2)
}

// ValidateRegistrationRequest mocks base method.
func (m *MockRegistrationCommonUtilIfc) ValidateRegistrationRequest(arg0 *management.RegisterArg, arg1 *models.RegisterAosRequestDetails) ([]*ntnxApiGuruError.AppMessage, error) {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "ValidateRegistrationRequest", arg0, arg1)
	ret0, _ := ret[0].([]*ntnxApiGuruError.AppMessage)
	ret1, _ := ret[1].(error)
	return ret0, ret1
}

// ValidateRegistrationRequest indicates an expected call of ValidateRegistrationRequest.
func (mr *MockRegistrationCommonUtilIfcMockRecorder) ValidateRegistrationRequest(arg0, arg1 interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "ValidateRegistrationRequest", reflect.TypeOf((*MockRegistrationCommonUtilIfc)(nil).ValidateRegistrationRequest), arg0, arg1)
}
