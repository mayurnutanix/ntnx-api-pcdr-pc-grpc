// Code generated by MockGen. DO NOT EDIT.
// Source: job_interfaces/base_workflow.go

// Package mocks is a generated GoMock package.
package mocks

import (
	models "ntnx-api-guru-pc/guru-pc-api-service/models"
	reflect "reflect"

	gomock "github.com/golang/mock/gomock"
)

// MockBaseIfc is a mock of BaseIfc interface.
type MockBaseIfc struct {
	ctrl     *gomock.Controller
	recorder *MockBaseIfcMockRecorder
}

// MockBaseIfcMockRecorder is the mock recorder for MockBaseIfc.
type MockBaseIfcMockRecorder struct {
	mock *MockBaseIfc
}

// NewMockBaseIfc creates a new mock instance.
func NewMockBaseIfc(ctrl *gomock.Controller) *MockBaseIfc {
	mock := &MockBaseIfc{ctrl: ctrl}
	mock.recorder = &MockBaseIfcMockRecorder{mock}
	return mock
}

// EXPECT returns an object that allows the caller to indicate expected use.
func (m *MockBaseIfc) EXPECT() *MockBaseIfcMockRecorder {
	return m.recorder
}

// Execute mocks base method.
func (m *MockBaseIfc) Execute(arg0 int, arg1 *models.Job) error {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "Execute", arg0, arg1)
	ret0, _ := ret[0].(error)
	return ret0
}

// Execute indicates an expected call of Execute.
func (mr *MockBaseIfcMockRecorder) Execute(arg0, arg1 interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "Execute", reflect.TypeOf((*MockBaseIfc)(nil).Execute), arg0, arg1)
}

// IsTerminalForward mocks base method.
func (m *MockBaseIfc) IsTerminalForward(arg0 int64) bool {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "IsTerminalForward", arg0)
	ret0, _ := ret[0].(bool)
	return ret0
}

// IsTerminalForward indicates an expected call of IsTerminalForward.
func (mr *MockBaseIfcMockRecorder) IsTerminalForward(arg0 interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "IsTerminalForward", reflect.TypeOf((*MockBaseIfc)(nil).IsTerminalForward), arg0)
}

// IsTerminalRollback mocks base method.
func (m *MockBaseIfc) IsTerminalRollback(arg0 int64, arg1 *models.Job) bool {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "IsTerminalRollback", arg0, arg1)
	ret0, _ := ret[0].(bool)
	return ret0
}

// IsTerminalRollback indicates an expected call of IsTerminalRollback.
func (mr *MockBaseIfcMockRecorder) IsTerminalRollback(arg0, arg1 interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "IsTerminalRollback", reflect.TypeOf((*MockBaseIfc)(nil).IsTerminalRollback), arg0, arg1)
}
