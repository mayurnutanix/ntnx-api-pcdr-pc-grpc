package utils

import (
	"encoding/json"
	"errors"
	"net/http"
	"testing"
	"time"

	dto "ntnx-api-guru-pc/generated-code/target/dto/models/prism/v4/management"
	common_config "ntnx-api-guru-pc/generated-code/target/protobuf/common/v1/config"
	"ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/management"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/errors/guru_error"
	"ntnx-api-guru-pc/guru-pc-api-service/test"

	common_models "github.com/nutanix-core/go-cache/api/pe"
	ergonproto "github.com/nutanix-core/go-cache/ergon"

	"github.com/golang/mock/gomock"
	"github.com/stretchr/testify/assert"
)

var TestParentTaskUuid = []byte{10, 204, 62, 133, 16, 76, 72, 134, 85, 211, 252, 41, 149, 19, 150, 228}

func TestGetIpAddressOrFqdn_Ipv4(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	clusterNetworkDetails := common_config.IPAddressOrFQDN{}
	Ipv4obj := common_config.IPv4Address{}
	ipvalue := "1.1.1.1"
	Ipv4obj.Value = &ipvalue
	clusterNetworkDetails.Ipv4 = &Ipv4obj

	assert.Equal(t, ipvalue, GetIpAddressOrFqdn(&clusterNetworkDetails))
}

func TestGetIpAddressOrFqdn_Ipv6(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	clusterNetworkDetails := common_config.IPAddressOrFQDN{}
	Ipv6obj := common_config.IPv6Address{}
	ipvalue := "1.1.1.1"
	Ipv6obj.Value = &ipvalue
	clusterNetworkDetails.Ipv6 = &Ipv6obj

	assert.Equal(t, ipvalue, GetIpAddressOrFqdn(&clusterNetworkDetails))
}

func TestGetIpAddressOrFqdn_GetFqdn(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	clusterNetworkDetails := common_config.IPAddressOrFQDN{}
	FqdnObj := common_config.FQDN{}
	fqdn := "1.1.1.1"
	FqdnObj.Value = &fqdn
	clusterNetworkDetails.Fqdn = &FqdnObj

	assert.Equal(t, fqdn, GetIpAddressOrFqdn(&clusterNetworkDetails))
}

func TestGetIpAddressOrFqdn_None(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	assert.Equal(t, "", GetIpAddressOrFqdn(nil))
}

func TestGetIpAddressOrFqdnRetNone(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	clusterNetworkDetails := common_config.IPAddressOrFQDN{}

	assert.Equal(t, "", GetIpAddressOrFqdn(&clusterNetworkDetails))
}

func TestExtractStatusFromErgonTask(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	responseMap := map[string]interface{}{
		"data": map[string]interface{}{
			"status": "pending",
		},
	}

	result := ExtractStatusFromErgonTask(&responseMap)

	assert.Equal(t, result, ergonproto.Task_Status(0))
}

func TestGetRemoteTaskStatus_Success(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	test.MockFlagValues(t)
	taskUuid := "taskUuid"
	remoteHost := "remoteHost"
	responseMap := map[string]interface{}{
		"data": map[string]interface{}{
			"status": "succeeded",
		},
	}
	TestResponseByte, _ := json.Marshal(responseMap)
	m.RemoteRestClient.EXPECT().CallApi(remoteHost, consts.EnvoyPort,
		gomock.Any(), http.MethodGet, nil, nil, nil, nil, nil, nil, true).
		Return(nil, nil, errors.New("error"))
	m.RemoteRestClient.EXPECT().CallApi(remoteHost, consts.EnvoyPort,
		gomock.Any(), http.MethodGet, nil, nil, nil, nil, nil, nil, true).
		Return(TestResponseByte, nil, nil)

	status, result := GetRemoteTaskStatus(taskUuid, remoteHost)

	assert.True(t, result)
	assert.Equal(t, ergonproto.Task_kSucceeded.Enum(), status.Enum())
}

func TestGetRemoteTaskStatus_Failure(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	test.MockFlagValues(t)
	taskUuid := "taskUuid"
	remoteHost := "remoteHost"
	responseMap := map[string]interface{}{
		"data": map[string]interface{}{
			"status": "running",
		},
	}
	TestResponseByte, _ := json.Marshal(responseMap)
	m.RemoteRestClient.EXPECT().CallApi(remoteHost, consts.EnvoyPort,
		gomock.Any(), http.MethodGet, nil, nil, nil, nil, nil, nil, true).
		Return(TestResponseByte, nil, nil).Times(5)

	status, result := GetRemoteTaskStatus(taskUuid, remoteHost)

	assert.False(t, result)
	assert.Nil(t, status)
}

func TestGetClusterEntityTypeFromMessageDomainManager(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	clusterEntityType := management.ClusterTypeMessage_ClusterType(1001)

	res := GetClusterEntityTypeFromMessage(clusterEntityType)

	assert.Equal(t, dto.CLUSTERTYPE_DOMAIN_MANAGER, res)
}

func TestGetClusterEntityTypeFromMessageAOS(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	clusterEntityType := management.ClusterTypeMessage_ClusterType(1002)

	res := GetClusterEntityTypeFromMessage(clusterEntityType)

	assert.Equal(t, dto.CLUSTERTYPE_AOS, res)
}

func TestGetClusterEntityTypeFromMessageUnknown(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	clusterEntityType := management.ClusterTypeMessage_ClusterType(0)

	res := GetClusterEntityTypeFromMessage(clusterEntityType)

	assert.Equal(t, dto.CLUSTERTYPE_UNKNOWN, res)
}

func TestPrepareGroupsRequestPayload(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	entityIds := make([]string, 0)
	entityIds = append(entityIds, "id")
	attributesList := make([]*common_models.GroupsRequestedAttribute, 0)
	attributesList = append(attributesList, &common_models.GroupsRequestedAttribute{
		Attribute: "id",
	})
	expectedRes := &common_models.GroupsGetEntitiesRequest{
		EntityType:            "",
		EntityIds:             &entityIds,
		GroupMemberAttributes: &attributesList,
	}

	res := PrepareGroupsRequestPayload("", entityIds, entityIds, nil, nil, nil, nil)

	assert.Equal(t, expectedRes, res)
}

func TestCalculateTimeDiffFromNow(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	StartTime := time.Now()
	StartTimeString := StartTime.Format(consts.DateTimeFormat)

	result := CalculateTimeDiffFromNow(StartTimeString)

	expectedTimeDiff := uint64((time.Since(StartTime)).Seconds())
	assert.Equal(t, expectedTimeDiff, result)
}

func TestToTitleCase(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	str := "test_foo"

	result := ToTitleCase(str)

	assert.Equal(t, "Test Foo", result)
}

func TestIsDomainManagerTrue(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)

	clusterUuid := "clusterUuid"
	path := consts.DomainManagerCES + "/" + clusterUuid
	m.ZkClient.EXPECT().ExistZkNode(path, true).Return(true, nil)

	result := IsDomainManager(clusterUuid)
	assert.True(t, result)
}

func TestIsDomainManagerFalse(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)

	clusterUuid := "clusterUuid"
	path := consts.DomainManagerCES + "/" + clusterUuid
	m.ZkClient.EXPECT().ExistZkNode(path, true).Return(true, TestError)

	result := IsDomainManager(clusterUuid)
	assert.False(t, result)
}

func TestGetRemoteTaskSucces(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	test.MockFlagValues(t)
	taskUuid := "taskUuid"
	remoteHost := "remoteHost"
	operation := "POST"
	responseMap := map[string]interface{}{
		"data": map[string]interface{}{
			"status": "succeeded",
		},
	}
	TestResponseByte, _ := json.Marshal(responseMap)
	m.RemoteRestClient.EXPECT().CallApi(remoteHost, consts.EnvoyPort,
		gomock.Any(), http.MethodGet, nil, nil, nil, nil, nil, nil, true).
		Return(TestResponseByte, nil, nil)

	response, err := GetRemoteTask(taskUuid, remoteHost, operation)
	assert.Equal(t, TestResponseByte, response)
	assert.Nil(t, err)
}

func TestGetRemoteTaskFailure(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	test.MockFlagValues(t)
	taskUuid := "taskUuid"
	remoteHost := "remoteHost"
	operation := "POST"
	m.RemoteRestClient.EXPECT().CallApi(remoteHost, consts.EnvoyPort,
		gomock.Any(), http.MethodGet, nil, nil, nil, nil, nil, nil, true).
		Return(nil, nil, errors.New("error"))

	response, err := GetRemoteTask(taskUuid, remoteHost, operation)
	assert.Nil(t, response)
	assert.EqualError(t, err, guru_error.GetInternalError(operation).Error())
}

func TestGetRemoteTaskCompletionDetailsSucces(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	taskExtId := "taskExtId"
	remoteHost := "remoteHost"
	operation := "POST"
	responseMap := map[string]interface{}{
		"data": map[string]interface{}{
			"completionDetails": []*KeyValue{
				{
					Key:   "key",
					Value: "value",
				},
			},
		},
	}
	TestResponseByte, _ := json.Marshal(responseMap)
	m.RemoteRestClient.EXPECT().CallApi(remoteHost, consts.EnvoyPort,
		gomock.Any(), http.MethodGet, nil, nil, nil, nil, nil, nil, true).
		Return(TestResponseByte, nil, nil)

	response, err := GetRemoteTaskCompletionDetails(taskExtId, remoteHost, operation)
	assert.Equal(t, responseMap["data"].(map[string]interface{})["completionDetails"], response)
	assert.Nil(t, err)
}

func TestGetRemoteTaskCompletionDetailsFailure(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	m := test.MockSingletons(t)
	taskExtId := "taskExtId"
	remoteHost := "remoteHost"
	operation := "POST"

	m.RemoteRestClient.EXPECT().CallApi(remoteHost, consts.EnvoyPort,
		gomock.Any(), http.MethodGet, nil, nil, nil, nil, nil, nil, true).
		Return(nil, nil, TestError)

	response, err := GetRemoteTaskCompletionDetails(taskExtId, remoteHost, operation)
	assert.Nil(t, response)
	assert.EqualError(t, err, guru_error.GetInternalError(operation).Error())
}

func TestGetNodeListFromZeusConfig(t *testing.T) {
	nodeList := GetNodeListFromZeusConfig(TestThreeNodeZeusConfigDTO)
	assert.Equal(t, []string{"uhura_vm_uuid", "uhura_vm_uuid2", "uhura_vm_uuid3"}, nodeList)
}
