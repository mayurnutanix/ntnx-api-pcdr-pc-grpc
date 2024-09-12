/**
 * @file configure_connection_helper_test.go
 * @description
 * @author deepanshu.jain@nutanix.com
 * @copyright Copyright (c) 2024 Nutanix Inc. All rights reserved.
 */

package configure_connection

import (
	"encoding/json"
	"fmt"
	"net/http"
	"testing"
	"time"

	dto "ntnx-api-guru-pc/generated-code/target/dto/models/prism/v4/management"
	"ntnx-api-guru-pc/generated-code/target/protobuf/common/v1/config"
	"ntnx-api-guru-pc/generated-code/target/protobuf/common/v1/response"
	"ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/management"
	config_manager "ntnx-api-guru-pc/guru-pc-api-service/cache/config"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/models"
	"ntnx-api-guru-pc/guru-pc-api-service/test"
	"ntnx-api-guru-pc/guru-pc-api-service/utils"

	"github.com/golang/mock/gomock"
	goproto "github.com/golang/protobuf/proto"
	"github.com/nutanix-core/go-cache/ergon"
	zk "github.com/nutanix-core/go-cache/go-zookeeper"
	"github.com/nutanix-core/go-cache/insights/insights_interface"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
	"google.golang.org/protobuf/proto"
)

func TestCreateUnconfigureConnectionJob(t *testing.T){
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	// Arrange
	Task := &ergon.Task{
		Uuid: []byte{1, 2, 3, 4},
	}
	args := management.UnconfigureConnectionArg{
		ExtId: new(string),
		Body: &management.ConnectionUnconfigurationSpec{
			RemoteCluster: &management.ClusterReference{
				ExtId: new(string),
			},
		},
	}
	job := CreateUnconfigureConnectionJob(Task, "124", &args)
	assert.NotNil(t, job)
}

func TestCreateConfigureConnectionJob(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	// Arrange
	clusterExternalId := "clusterExternalId"
	Task := &ergon.Task{
		Uuid: []byte{1, 2, 3, 4},
	}
	args := &management.ConfigureConnectionArg{
		ExtId: new(string),
		Body: &management.ConnectionConfigurationSpec{
			RemoteCluster: &management.RemoteCluster{
				Base: &response.ExternalizableAbstractModel{
					ExtId: new(string),
				},
				Name: proto.String("TestCluster"),
				ExternalAddress: &config.IPAddressOrFQDN{
					Ipv4: &config.IPv4Address{
						Value: proto.String("127.0.0.1"),
					},
				},
				NodeIpAddresses: &config.IPAddressOrFQDNArrayWrapper{
					Value: []*config.IPAddressOrFQDN{
						{
							Ipv4: &config.IPv4Address{
								Value: proto.String("127.0.0.2"),
							},
						},
						{
							Ipv4: &config.IPv4Address{
								Value: proto.String("127.0.0.3"),
							},
						},
						{
							Ipv6: &config.IPv6Address{
								Value: proto.String("2001:db8:3333:4444:5555:6666:7777:8888"),
							},
						},
					},
				},
			},
		},
	}
	// Mocking AZ Entity
	metricName := "uuid"
	metricValue := insights_interface.TimeValuePair{
		Value: &insights_interface.DataValue{
			ValueType: &insights_interface.DataValue_StrValue{
				StrValue: "123",
			},
		},
	}
	metrticRet := insights_interface.MetricData{
		Name:      &metricName,
		ValueList: []*insights_interface.TimeValuePair{&metricValue},
	}
	entityRet := insights_interface.EntityWithMetric{
		MetricDataList: []*insights_interface.MetricData{&metrticRet},
	}
	azEntityRetList := []*insights_interface.EntityWithMetric{&entityRet}
	m.DbClient.Mock.On("Query", mock.Anything).Return(azEntityRetList, nil).Once()

	// Mocking DR Config Entity
	drConfigEntityRetList := []*insights_interface.EntityWithMetric{}
	m.DbClient.Mock.On("Query", mock.Anything).Return(drConfigEntityRetList, nil).Once()

	// Mock Sync Protection Rules
	prEntityRetList := []*insights_interface.EntityWithMetric{}
	m.DbClient.Mock.On("Query", mock.Anything).Return(prEntityRetList, nil).Once()
	expectedJob := &models.Job{
		JobMetadata: &models.JobMetadata{
			OperationStartTime:    time.Now().Format(consts.DateTimeFormat),
			ParentTaskId:          Task.GetUuid(),
			ContextId:             clusterExternalId,
			RemoteAddress:         "127.0.0.1",
			RemoteNodeIPAddresses: []string{"127.0.0.2", "127.0.0.3", "2001:db8:3333:4444:5555:6666:7777:8888"},
			Rollback:              false,
			Name:                  consts.CONFIGURE_CONNECTION,
			RemoteClusterName:     "TestCluster",
			RemoteClusterType:     dto.CLUSTERTYPE_DOMAIN_MANAGER,
		},
		Name:       consts.CONFIGURE_CONNECTION,
		ParentTask: Task,
	}
	job := CreateConfigureConnectionJob(Task, clusterExternalId, args)

	assert.Equal(t, expectedJob, job)

}

func TestCheckDRWorkflowsPRExist(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)

	pcUuid := "123"

	// Mocking AZ Entity
	metricName := "uuid"
	metricValue := insights_interface.TimeValuePair{
		Value: &insights_interface.DataValue{
			ValueType: &insights_interface.DataValue_StrValue{
				StrValue: "123",
			},
		},
	}
	metrticRet := insights_interface.MetricData{
		Name:      &metricName,
		ValueList: []*insights_interface.TimeValuePair{&metricValue},
	}
	entityRet := insights_interface.EntityWithMetric{
		MetricDataList: []*insights_interface.MetricData{&metrticRet},
	}
	azEntityRetList := []*insights_interface.EntityWithMetric{&entityRet}
	m.DbClient.Mock.On("Query", mock.Anything).Return(azEntityRetList, nil).Once()

	// Mocking DR Config Entity
	roleMetricName := "role"
	roleMetricValue := insights_interface.TimeValuePair{
		Value: &insights_interface.DataValue{
			ValueType: &insights_interface.DataValue_StrValue{
				StrValue: "kPrimary",
			},
		},
	}
	roleMetrticRet := insights_interface.MetricData{
		Name:      &roleMetricName,
		ValueList: []*insights_interface.TimeValuePair{&roleMetricValue},
	}
	prUuidMetricName := "protection_rule_uuid"
	prUuidmetricValue := insights_interface.TimeValuePair{
		Value: &insights_interface.DataValue{
			ValueType: &insights_interface.DataValue_StrValue{
				StrValue: "789",
			},
		},
	}
	prUuidMetricRet := insights_interface.MetricData{
		Name:      &prUuidMetricName,
		ValueList: []*insights_interface.TimeValuePair{&prUuidmetricValue},
	}
	drConfigEntityRet := insights_interface.EntityWithMetric{
		MetricDataList: []*insights_interface.MetricData{&roleMetrticRet, &prUuidMetricRet},
	}
	drConfigEntityRetList := []*insights_interface.EntityWithMetric{&drConfigEntityRet}
	m.DbClient.Mock.On("Query", mock.Anything).Return(drConfigEntityRetList, nil).Once()

	// Mock Sync Protection Rules
	prNameMetricName := "name"
	prNameMetricValue := insights_interface.TimeValuePair{
		Value: &insights_interface.DataValue{
			ValueType: &insights_interface.DataValue_StrValue{
				StrValue: "abc",
			},
		},
	}
	prNameMetrticRet := insights_interface.MetricData{
		Name:      &prNameMetricName,
		ValueList: []*insights_interface.TimeValuePair{&prNameMetricValue},
	}
	prConfigEntityRet := insights_interface.EntityWithMetric{
		MetricDataList: []*insights_interface.MetricData{&prNameMetrticRet},
	}
	prEntityRetList := []*insights_interface.EntityWithMetric{&prConfigEntityRet}
	m.DbClient.Mock.On("Query", mock.Anything).Return(prEntityRetList, nil).Once()

	err := CheckDRWorkflows(pcUuid)
	assert.NotNil(t, err)
}

func TestCheckDRWorkflowsSuccessNoDrWorkflows(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)

	pcUuid := "123"

	// Mocking AZ Entity
	metricName := "uuid"
	metricValue := insights_interface.TimeValuePair{
		Value: &insights_interface.DataValue{
			ValueType: &insights_interface.DataValue_StrValue{
				StrValue: "123",
			},
		},
	}
	metrticRet := insights_interface.MetricData{
		Name:      &metricName,
		ValueList: []*insights_interface.TimeValuePair{&metricValue},
	}
	entityRet := insights_interface.EntityWithMetric{
		MetricDataList: []*insights_interface.MetricData{&metrticRet},
	}
	azEntityRetList := []*insights_interface.EntityWithMetric{&entityRet}
	m.DbClient.Mock.On("Query", mock.Anything).Return(azEntityRetList, nil).Once()

	// Mocking DR Config Entity
	m.DbClient.Mock.On("Query", mock.Anything).Return(nil, insights_interface.ErrNotFound).Once()

	err := CheckDRWorkflows(pcUuid)
	assert.Nil(t, err)
}

func TestCallPutConfigurationApi(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	m.Uuid.EXPECT().New()
	remoteAddress := "remote-address"
	remoteClusterId := "remote-cluster-id"
	payload := dto.ConnectionConfigurationSpec{}
	url := fmt.Sprintf(consts.V4ConfigureConnectionApiPath, consts.ApiVersion, remoteClusterId)
	taskResponse := utils.TaskResponseModel{
		Data: struct {
			ExtId string "json:\"extId\""
		}{
			ExtId: "123",
		},
	}
	jsonRes, _ := json.Marshal(taskResponse)
	m.RemoteRestClient.EXPECT().CallApiCertBasedAuth(remoteAddress, gomock.Any(), url, http.MethodPost, gomock.Any(), gomock.Any(),
		nil, nil).Return(jsonRes, nil, nil)

	task, err := callPutConfigureConnectionApi(remoteClusterId, remoteAddress, &payload)

	assert.Equal(t, "123", task)
	assert.Nil(t, err)
}

func TestReconfigureExternalIp(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	m.Uuid.EXPECT().New()
	config_manager.SetConfigurationCache(test.GetMockZeusProto())
	remoteAddress := "remote-address"
	pcUuid := "123"
	zkStat := zk.Stat{
		Version: 1,
	}
	mockCes := test.GetMockCES()
	byteCes, _ := goproto.Marshal(mockCes)
	var registeredPcList []string
	registeredPcList = append(registeredPcList, pcUuid)
	m.ZkSession.On("Children", consts.DomainManagerCES, true).Return(registeredPcList,
		nil, nil).Once()
	m.ZkSession.On("Get", consts.DomainManagerCES+"/"+pcUuid, true).
		Return(byteCes, &zkStat, nil)
	m.ZkSession.On("Exist", consts.DomainManagerCES+"/"+pcUuid, true).
		Return(true, nil, nil)
	url := fmt.Sprintf(consts.V4ConfigureConnectionApiPath, consts.ApiVersion, pcUuid)
	taskResponse := utils.TaskResponseModel{
		Data: struct {
			ExtId string "json:\"extId\""
		}{
			ExtId: "123",
		},
	}
	jsonRes, _ := json.Marshal(taskResponse)
	m.RemoteRestClient.EXPECT().CallApiCertBasedAuth("ip1", gomock.Any(), url, http.MethodPost, gomock.Any(), gomock.Any(),
		nil, nil).Return(jsonRes, nil, nil)
	test.MockFlagValues(t)
	responseMap := map[string]interface{}{
		"data": map[string]interface{}{
			"status": "succeeded",
		},
	}
	TestResponseByte, _ := json.Marshal(responseMap)
	m.RemoteRestClient.EXPECT().CallApi("ip1", consts.EnvoyPort,
		gomock.Any(), http.MethodGet, nil, nil, nil, nil, nil, nil, true).
		Return(TestResponseByte, nil, nil)

	err := ReconfigureExternalIp(remoteAddress)
	assert.Nil(t, err)
}
