// Code generated by MockGen. DO NOT EDIT.
// Source: guru-pc-api-service/errors/grpc_error/grpc_error.go

// Package mocks is a generated GoMock package.
package mocks

import (
	guru_error "ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	reflect "reflect"

	gomock "github.com/golang/mock/gomock"
)

// MockGrpcStatusUtil is a mock of GrpcStatusUtil interface.
type MockGrpcStatusUtil struct {
	ctrl     *gomock.Controller
	recorder *MockGrpcStatusUtilMockRecorder
}

// MockGrpcStatusUtilMockRecorder is the mock recorder for MockGrpcStatusUtil.
type MockGrpcStatusUtilMockRecorder struct {
	mock *MockGrpcStatusUtil
}

// NewMockGrpcStatusUtil creates a new mock instance.
func NewMockGrpcStatusUtil(ctrl *gomock.Controller) *MockGrpcStatusUtil {
	mock := &MockGrpcStatusUtil{ctrl: ctrl}
	mock.recorder = &MockGrpcStatusUtilMockRecorder{mock}
	return mock
}

// EXPECT returns an object that allows the caller to indicate expected use.
func (m *MockGrpcStatusUtil) EXPECT() *MockGrpcStatusUtilMockRecorder {
	return m.recorder
}

// BuildGrpcError mocks base method.
func (m *MockGrpcStatusUtil) BuildGrpcError(arg0 guru_error.GuruErrorInterface) error {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "BuildGrpcError", arg0)
	ret0, _ := ret[0].(error)
	return ret0
}

// BuildGrpcError indicates an expected call of BuildGrpcError.
func (mr *MockGrpcStatusUtilMockRecorder) BuildGrpcError(arg0 interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "BuildGrpcError", reflect.TypeOf((*MockGrpcStatusUtil)(nil).BuildGrpcError), arg0)
}