// Code generated by MockGen. DO NOT EDIT.
// Source: /opt/homebrew/Cellar/go/1.22.4/bin/pkg/mod/github.com/nutanix-core/go-cache@v0.0.0-20240616123053-5c075257d96c/infrastructure/cluster/client/genesis/json_interface.go

// Package mocks is a generated GoMock package.
package mocks

import (
	reflect "reflect"

	gomock "github.com/golang/mock/gomock"
)

// MockGenesisJsonRpcClientIfc is a mock of GenesisJsonRpcClientIfc interface.
type MockGenesisJsonRpcClientIfc struct {
	ctrl     *gomock.Controller
	recorder *MockGenesisJsonRpcClientIfcMockRecorder
}

// MockGenesisJsonRpcClientIfcMockRecorder is the mock recorder for MockGenesisJsonRpcClientIfc.
type MockGenesisJsonRpcClientIfcMockRecorder struct {
	mock *MockGenesisJsonRpcClientIfc
}

// NewMockGenesisJsonRpcClientIfc creates a new mock instance.
func NewMockGenesisJsonRpcClientIfc(ctrl *gomock.Controller) *MockGenesisJsonRpcClientIfc {
	mock := &MockGenesisJsonRpcClientIfc{ctrl: ctrl}
	mock.recorder = &MockGenesisJsonRpcClientIfcMockRecorder{mock}
	return mock
}

// EXPECT returns an object that allows the caller to indicate expected use.
func (m *MockGenesisJsonRpcClientIfc) EXPECT() *MockGenesisJsonRpcClientIfcMockRecorder {
	return m.recorder
}

// DisableService mocks base method.
func (m *MockGenesisJsonRpcClientIfc) DisableService(params interface{}) (string, error) {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "DisableService", params)
	ret0, _ := ret[0].(string)
	ret1, _ := ret[1].(error)
	return ret0, ret1
}

// DisableService indicates an expected call of DisableService.
func (mr *MockGenesisJsonRpcClientIfcMockRecorder) DisableService(params interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "DisableService", reflect.TypeOf((*MockGenesisJsonRpcClientIfc)(nil).DisableService), params)
}

// GetCmspStatus mocks base method.
func (m *MockGenesisJsonRpcClientIfc) GetCmspStatus(params interface{}) (string, error) {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "GetCmspStatus", params)
	ret0, _ := ret[0].(string)
	ret1, _ := ret[1].(error)
	return ret0, ret1
}

// GetCmspStatus indicates an expected call of GetCmspStatus.
func (mr *MockGenesisJsonRpcClientIfcMockRecorder) GetCmspStatus(params interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "GetCmspStatus", reflect.TypeOf((*MockGenesisJsonRpcClientIfc)(nil).GetCmspStatus), params)
}

// IsServiceRunning mocks base method.
func (m *MockGenesisJsonRpcClientIfc) IsServiceRunning(params interface{}) (string, error) {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "IsServiceRunning", params)
	ret0, _ := ret[0].(string)
	ret1, _ := ret[1].(error)
	return ret0, ret1
}

// IsServiceRunning indicates an expected call of IsServiceRunning.
func (mr *MockGenesisJsonRpcClientIfcMockRecorder) IsServiceRunning(params interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "IsServiceRunning", reflect.TypeOf((*MockGenesisJsonRpcClientIfc)(nil).IsServiceRunning), params)
}

// ModifyFirewall mocks base method.
func (m *MockGenesisJsonRpcClientIfc) ModifyFirewall(params interface{}) (string, error) {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "ModifyFirewall", params)
	ret0, _ := ret[0].(string)
	ret1, _ := ret[1].(error)
	return ret0, ret1
}

// ModifyFirewall indicates an expected call of ModifyFirewall.
func (mr *MockGenesisJsonRpcClientIfcMockRecorder) ModifyFirewall(params interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "ModifyFirewall", reflect.TypeOf((*MockGenesisJsonRpcClientIfc)(nil).ModifyFirewall), params)
}

// ServicesStart mocks base method.
func (m *MockGenesisJsonRpcClientIfc) ServicesStart(params interface{}) (string, error) {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "ServicesStart", params)
	ret0, _ := ret[0].(string)
	ret1, _ := ret[1].(error)
	return ret0, ret1
}

// ServicesStart indicates an expected call of ServicesStart.
func (mr *MockGenesisJsonRpcClientIfcMockRecorder) ServicesStart(params interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "ServicesStart", reflect.TypeOf((*MockGenesisJsonRpcClientIfc)(nil).ServicesStart), params)
}

// ServicesStop mocks base method.
func (m *MockGenesisJsonRpcClientIfc) ServicesStop(params interface{}) (string, error) {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "ServicesStop", params)
	ret0, _ := ret[0].(string)
	ret1, _ := ret[1].(error)
	return ret0, ret1
}

// ServicesStop indicates an expected call of ServicesStop.
func (mr *MockGenesisJsonRpcClientIfcMockRecorder) ServicesStop(params interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "ServicesStop", reflect.TypeOf((*MockGenesisJsonRpcClientIfc)(nil).ServicesStop), params)
}
