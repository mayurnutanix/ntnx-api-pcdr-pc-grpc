// Code generated by MockGen. DO NOT EDIT.
// Source: guru-pc-api-service/interfaces/external/idf_watcher_interfaces.go

// Package mocks is a generated GoMock package.
package mocks

import (
	reflect "reflect"

	gomock "github.com/golang/mock/gomock"
	insights_interface "github.com/nutanix-core/go-cache/insights/insights_interface"
)

// MockIdfWatcher is a mock of IdfWatcher interface.
type MockIdfWatcher struct {
	ctrl     *gomock.Controller
	recorder *MockIdfWatcherMockRecorder
}

// MockIdfWatcherMockRecorder is the mock recorder for MockIdfWatcher.
type MockIdfWatcherMockRecorder struct {
	mock *MockIdfWatcher
}

// NewMockIdfWatcher creates a new mock instance.
func NewMockIdfWatcher(ctrl *gomock.Controller) *MockIdfWatcher {
	mock := &MockIdfWatcher{ctrl: ctrl}
	mock.recorder = &MockIdfWatcherMockRecorder{mock}
	return mock
}

// EXPECT returns an object that allows the caller to indicate expected use.
func (m *MockIdfWatcher) EXPECT() *MockIdfWatcherMockRecorder {
	return m.recorder
}

// CompositeWatchOnEntitiesOfType mocks base method.
func (m *MockIdfWatcher) CompositeWatchOnEntitiesOfType(watchInfo *insights_interface.EntityWatchInfo, createWatch, updateWatch, deleteWatch bool) ([]*insights_interface.Entity, error) {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "CompositeWatchOnEntitiesOfType", watchInfo, createWatch, updateWatch, deleteWatch)
	ret0, _ := ret[0].([]*insights_interface.Entity)
	ret1, _ := ret[1].(error)
	return ret0, ret1
}

// CompositeWatchOnEntitiesOfType indicates an expected call of CompositeWatchOnEntitiesOfType.
func (mr *MockIdfWatcherMockRecorder) CompositeWatchOnEntitiesOfType(watchInfo, createWatch, updateWatch, deleteWatch interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "CompositeWatchOnEntitiesOfType", reflect.TypeOf((*MockIdfWatcher)(nil).CompositeWatchOnEntitiesOfType), watchInfo, createWatch, updateWatch, deleteWatch)
}

// NewEntityWatchInfo mocks base method.
func (m *MockIdfWatcher) NewEntityWatchInfo(guid *insights_interface.EntityGuid, name string, get_current_state, return_previous_entity_state bool, cb insights_interface.InsightsWatchCb, filterExpression *insights_interface.BooleanExpression, watchMetric string) *insights_interface.EntityWatchInfo {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "NewEntityWatchInfo", guid, name, get_current_state, return_previous_entity_state, cb, filterExpression, watchMetric)
	ret0, _ := ret[0].(*insights_interface.EntityWatchInfo)
	return ret0
}

// NewEntityWatchInfo indicates an expected call of NewEntityWatchInfo.
func (mr *MockIdfWatcherMockRecorder) NewEntityWatchInfo(guid, name, get_current_state, return_previous_entity_state, cb, filterExpression, watchMetric interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "NewEntityWatchInfo", reflect.TypeOf((*MockIdfWatcher)(nil).NewEntityWatchInfo), guid, name, get_current_state, return_previous_entity_state, cb, filterExpression, watchMetric)
}

// Register mocks base method.
func (m *MockIdfWatcher) Register() error {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "Register")
	ret0, _ := ret[0].(error)
	return ret0
}

// Register indicates an expected call of Register.
func (mr *MockIdfWatcherMockRecorder) Register() *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "Register", reflect.TypeOf((*MockIdfWatcher)(nil).Register))
}

// Reregister mocks base method.
func (m *MockIdfWatcher) Reregister() ([]*insights_interface.RegisterWatchResult, error) {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "Reregister")
	ret0, _ := ret[0].([]*insights_interface.RegisterWatchResult)
	ret1, _ := ret[1].(error)
	return ret0, ret1
}

// Reregister indicates an expected call of Reregister.
func (mr *MockIdfWatcherMockRecorder) Reregister() *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "Reregister", reflect.TypeOf((*MockIdfWatcher)(nil).Reregister))
}

// Start mocks base method.
func (m *MockIdfWatcher) Start() error {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "Start")
	ret0, _ := ret[0].(error)
	return ret0
}

// Start indicates an expected call of Start.
func (mr *MockIdfWatcherMockRecorder) Start() *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "Start", reflect.TypeOf((*MockIdfWatcher)(nil).Start))
}