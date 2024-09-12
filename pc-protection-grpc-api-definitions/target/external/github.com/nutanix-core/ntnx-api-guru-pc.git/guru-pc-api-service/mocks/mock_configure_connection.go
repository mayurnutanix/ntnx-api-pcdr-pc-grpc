// Code generated by MockGen. DO NOT EDIT.
// Source: ./guru-pc-api-service/services/register/configure_connection/interface.go

// Package mocks is a generated GoMock package.
package mocks

import (
	models "ntnx-api-guru-pc/guru-pc-api-service/models"
	reflect "reflect"

	gomock "github.com/golang/mock/gomock"
)

// MockConnectionConfig is a mock of ConnectionConfig interface.
type MockConnectionConfig struct {
	ctrl     *gomock.Controller
	recorder *MockConnectionConfigMockRecorder
}

// MockConnectionConfigMockRecorder is the mock recorder for MockConnectionConfig.
type MockConnectionConfigMockRecorder struct {
	mock *MockConnectionConfig
}

// NewMockConnectionConfig creates a new mock instance.
func NewMockConnectionConfig(ctrl *gomock.Controller) *MockConnectionConfig {
	mock := &MockConnectionConfig{ctrl: ctrl}
	mock.recorder = &MockConnectionConfigMockRecorder{mock}
	return mock
}

// EXPECT returns an object that allows the caller to indicate expected use.
func (m *MockConnectionConfig) EXPECT() *MockConnectionConfigMockRecorder {
	return m.recorder
}

// Configure mocks base method.
func (m *MockConnectionConfig) Configure(jobMetadata *models.Job) error {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "Configure", jobMetadata)
	ret0, _ := ret[0].(error)
	return ret0
}

// Configure indicates an expected call of Configure.
func (mr *MockConnectionConfigMockRecorder) Configure(jobMetadata interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "Configure", reflect.TypeOf((*MockConnectionConfig)(nil).Configure), jobMetadata)
}

// RunPreConfigureActions mocks base method.
func (m *MockConnectionConfig) RunPreConfigureActions(jobMetadata *models.Job) error {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "RunPreConfigureActions", jobMetadata)
	ret0, _ := ret[0].(error)
	return ret0
}

// RunPreConfigureActions indicates an expected call of RunPreConfigureActions.
func (mr *MockConnectionConfigMockRecorder) RunPreConfigureActions(jobMetadata interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "RunPreConfigureActions", reflect.TypeOf((*MockConnectionConfig)(nil).RunPreConfigureActions), jobMetadata)
}

// RunPreUnconfigureActions mocks base method.
func (m *MockConnectionConfig) RunPreUnconfigureActions(jobMetadata *models.Job) error {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "RunPreUnconfigureActions", jobMetadata)
	ret0, _ := ret[0].(error)
	return ret0
}

// RunPreUnconfigureActions indicates an expected call of RunPreUnconfigureActions.
func (mr *MockConnectionConfigMockRecorder) RunPreUnconfigureActions(jobMetadata interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "RunPreUnconfigureActions", reflect.TypeOf((*MockConnectionConfig)(nil).RunPreUnconfigureActions), jobMetadata)
}

// Unconfigure mocks base method.
func (m *MockConnectionConfig) Unconfigure(jobMetadata *models.Job) error {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "Unconfigure", jobMetadata)
	ret0, _ := ret[0].(error)
	return ret0
}

// Unconfigure indicates an expected call of Unconfigure.
func (mr *MockConnectionConfigMockRecorder) Unconfigure(jobMetadata interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "Unconfigure", reflect.TypeOf((*MockConnectionConfig)(nil).Unconfigure), jobMetadata)
}
