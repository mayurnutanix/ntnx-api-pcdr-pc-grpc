// Code generated by MockGen. DO NOT EDIT.
// Source: guru-pc-api-service/services/trust_setup/cert_signing_trust_setup/interface.go

// Package mocks is a generated GoMock package.
package mocks

import (
	guru_error "ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	models "ntnx-api-guru-pc/guru-pc-api-service/models"
	reflect "reflect"

	gomock "github.com/golang/mock/gomock"
)

// MockCertSigningTrustSetupIfc is a mock of CertSigningTrustSetupIfc interface.
type MockCertSigningTrustSetupIfc struct {
	ctrl     *gomock.Controller
	recorder *MockCertSigningTrustSetupIfcMockRecorder
}

// MockCertSigningTrustSetupIfcMockRecorder is the mock recorder for MockCertSigningTrustSetupIfc.
type MockCertSigningTrustSetupIfcMockRecorder struct {
	mock *MockCertSigningTrustSetupIfc
}

// NewMockCertSigningTrustSetupIfc creates a new mock instance.
func NewMockCertSigningTrustSetupIfc(ctrl *gomock.Controller) *MockCertSigningTrustSetupIfc {
	mock := &MockCertSigningTrustSetupIfc{ctrl: ctrl}
	mock.recorder = &MockCertSigningTrustSetupIfcMockRecorder{mock}
	return mock
}

// EXPECT returns an object that allows the caller to indicate expected use.
func (m *MockCertSigningTrustSetupIfc) EXPECT() *MockCertSigningTrustSetupIfcMockRecorder {
	return m.recorder
}

// Execute mocks base method.
func (m *MockCertSigningTrustSetupIfc) Execute(item *models.Job) error {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "Execute", item)
	ret0, _ := ret[0].(error)
	return ret0
}

// Execute indicates an expected call of Execute.
func (mr *MockCertSigningTrustSetupIfcMockRecorder) Execute(item interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "Execute", reflect.TypeOf((*MockCertSigningTrustSetupIfc)(nil).Execute), item)
}

// InitiateCSR mocks base method.
func (m *MockCertSigningTrustSetupIfc) InitiateCSR(item *models.Job) (*models.ClusterTrustSetupDetailsDTO, guru_error.GuruErrorInterface) {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "InitiateCSR", item)
	ret0, _ := ret[0].(*models.ClusterTrustSetupDetailsDTO)
	ret1, _ := ret[1].(guru_error.GuruErrorInterface)
	return ret0, ret1
}

// InitiateCSR indicates an expected call of InitiateCSR.
func (mr *MockCertSigningTrustSetupIfcMockRecorder) InitiateCSR(item interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "InitiateCSR", reflect.TypeOf((*MockCertSigningTrustSetupIfc)(nil).InitiateCSR), item)
}

// ResetCaChain mocks base method.
func (m *MockCertSigningTrustSetupIfc) ResetCaChain(item *models.Job) (*bool, guru_error.GuruErrorInterface) {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "ResetCaChain", item)
	ret0, _ := ret[0].(*bool)
	ret1, _ := ret[1].(guru_error.GuruErrorInterface)
	return ret0, ret1
}

// ResetCaChain indicates an expected call of ResetCaChain.
func (mr *MockCertSigningTrustSetupIfcMockRecorder) ResetCaChain(item interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "ResetCaChain", reflect.TypeOf((*MockCertSigningTrustSetupIfc)(nil).ResetCaChain), item)
}

// Rollback mocks base method.
func (m *MockCertSigningTrustSetupIfc) Rollback(item *models.Job) {
	m.ctrl.T.Helper()
	m.ctrl.Call(m, "Rollback", item)
}

// Rollback indicates an expected call of Rollback.
func (mr *MockCertSigningTrustSetupIfcMockRecorder) Rollback(item interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "Rollback", reflect.TypeOf((*MockCertSigningTrustSetupIfc)(nil).Rollback), item)
}

// SignCertificate mocks base method.
func (m *MockCertSigningTrustSetupIfc) SignCertificate(item *models.Job, body *models.ClusterTrustSetupDetailsDTO) (*models.ClusterTrustSetupDetailsDTO, guru_error.GuruErrorInterface) {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "SignCertificate", item, body)
	ret0, _ := ret[0].(*models.ClusterTrustSetupDetailsDTO)
	ret1, _ := ret[1].(guru_error.GuruErrorInterface)
	return ret0, ret1
}

// SignCertificate indicates an expected call of SignCertificate.
func (mr *MockCertSigningTrustSetupIfcMockRecorder) SignCertificate(item, body interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "SignCertificate", reflect.TypeOf((*MockCertSigningTrustSetupIfc)(nil).SignCertificate), item, body)
}

// UpdateCaChain mocks base method.
func (m *MockCertSigningTrustSetupIfc) UpdateCaChain(item *models.Job, body *models.ClusterTrustSetupDetailsDTO) (*bool, guru_error.GuruErrorInterface) {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "UpdateCaChain", item, body)
	ret0, _ := ret[0].(*bool)
	ret1, _ := ret[1].(guru_error.GuruErrorInterface)
	return ret0, ret1
}

// UpdateCaChain indicates an expected call of UpdateCaChain.
func (mr *MockCertSigningTrustSetupIfcMockRecorder) UpdateCaChain(item, body interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "UpdateCaChain", reflect.TypeOf((*MockCertSigningTrustSetupIfc)(nil).UpdateCaChain), item, body)
}
