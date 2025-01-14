// Code generated by MockGen. DO NOT EDIT.
// Source: guru-pc-api-service/services/register/pc_domain/pc_domain.go

// Package mocks is a generated GoMock package.
package mocks

import (
	reflect "reflect"

	gomock "github.com/golang/mock/gomock"
	insights_interface "github.com/nutanix-core/go-cache/insights/insights_interface"
	zeus "github.com/nutanix-core/go-cache/zeus"
)

// MockPCDomain is a mock of PCDomain interface.
type MockPCDomain struct {
	ctrl     *gomock.Controller
	recorder *MockPCDomainMockRecorder
}

// MockPCDomainMockRecorder is the mock recorder for MockPCDomain.
type MockPCDomainMockRecorder struct {
	mock *MockPCDomain
}

// NewMockPCDomain creates a new mock instance.
func NewMockPCDomain(ctrl *gomock.Controller) *MockPCDomain {
	mock := &MockPCDomain{ctrl: ctrl}
	mock.recorder = &MockPCDomainMockRecorder{mock}
	return mock
}

// EXPECT returns an object that allows the caller to indicate expected use.
func (m *MockPCDomain) EXPECT() *MockPCDomainMockRecorder {
	return m.recorder
}

// Create mocks base method.
func (m *MockPCDomain) Create() error {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "Create")
	ret0, _ := ret[0].(error)
	return ret0
}

// Create indicates an expected call of Create.
func (mr *MockPCDomainMockRecorder) Create() *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "Create", reflect.TypeOf((*MockPCDomain)(nil).Create))
}

// Delete mocks base method.
func (m *MockPCDomain) Delete(Uuid string) error {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "Delete", Uuid)
	ret0, _ := ret[0].(error)
	return ret0
}

// Delete indicates an expected call of Delete.
func (mr *MockPCDomainMockRecorder) Delete(Uuid interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "Delete", reflect.TypeOf((*MockPCDomain)(nil).Delete), Uuid)
}

// GetById mocks base method.
func (m *MockPCDomain) GetById(Uuid string) (*insights_interface.Entity, error) {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "GetById", Uuid)
	ret0, _ := ret[0].(*insights_interface.Entity)
	ret1, _ := ret[1].(error)
	return ret0, ret1
}

// GetById indicates an expected call of GetById.
func (mr *MockPCDomainMockRecorder) GetById(Uuid interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "GetById", reflect.TypeOf((*MockPCDomain)(nil).GetById), Uuid)
}

// SetAZ mocks base method.
func (m *MockPCDomain) SetAZ(az *zeus.AvailabilityZonePhysical) error {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "SetAZ", az)
	ret0, _ := ret[0].(error)
	return ret0
}

// SetAZ indicates an expected call of SetAZ.
func (mr *MockPCDomainMockRecorder) SetAZ(az interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "SetAZ", reflect.TypeOf((*MockPCDomain)(nil).SetAZ), az)
}
