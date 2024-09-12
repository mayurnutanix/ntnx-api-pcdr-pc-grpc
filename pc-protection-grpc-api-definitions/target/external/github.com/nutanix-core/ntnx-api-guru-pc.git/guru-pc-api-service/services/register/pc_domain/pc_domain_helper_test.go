package pc_domain

import (
	"ntnx-api-guru-pc/guru-pc-api-service/test"
	"testing"

	"github.com/nutanix-core/go-cache/insights/insights_interface"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
	"google.golang.org/protobuf/proto"
)

func TestCheckAzByUrlExistSuccess(t *testing.T) {
	m := test.MockSingletons(t)
	entities := []*insights_interface.EntityWithMetric{
		{
			MetricDataList: []*insights_interface.MetricData{
				{
					Name: proto.String("uuid"),
					ValueList: []*insights_interface.TimeValuePair{
						{
							Value: &insights_interface.DataValue{
								ValueType: &insights_interface.DataValue_StrValue{
									StrValue: TestUuid,
								},
							},
						},
					},
				},
			},
		},
	}
	m.DbClient.On("Query", mock.AnythingOfType("*insights_interface.GetEntitiesWithMetricsArg")).Return(entities, nil)
	azExists, _, err := CheckAzByUrlExist(TestUuid)
	assert.Nil(t, err)
	assert.True(t, azExists)
}

func TestCheckAzByUrlExistSuccess_AZDoesNotExist(t *testing.T) {
	m := test.MockSingletons(t)
	m.DbClient.On("Query", mock.AnythingOfType("*insights_interface.GetEntitiesWithMetricsArg")).Return(nil, insights_interface.ErrNotFound)
	azExists, entity, err := CheckAzByUrlExist(TestUuid)

	assert.Nil(t, entity)
	assert.Nil(t, err)
	assert.False(t, azExists)
}

func TestCheckAzByUrlExistFailure_QueryFailure(t *testing.T) {
	m := test.MockSingletons(t)
	m.DbClient.On("Query", mock.AnythingOfType("*insights_interface.GetEntitiesWithMetricsArg")).Return(nil, ErrTest)
	azExists, _, err := CheckAzByUrlExist(TestUuid)
	assert.EqualError(t, err, ErrTest.Error())
	assert.False(t, azExists)
}

func TestCheckAzByUrlExistFailure_UuidNotPresent(t *testing.T) {
	m := test.MockSingletons(t)
	entities := []*insights_interface.EntityWithMetric{
		{
			MetricDataList: []*insights_interface.MetricData{
				{
					Name: proto.String("abc"),
					ValueList: []*insights_interface.TimeValuePair{
						{
							Value: &insights_interface.DataValue{
								ValueType: &insights_interface.DataValue_StrValue{
									StrValue: TestName,
								},
							},
						},
					},
				},
			},
		},
	}
	m.DbClient.On("Query", mock.AnythingOfType("*insights_interface.GetEntitiesWithMetricsArg")).Return(entities, nil)
	azExists, entity, err := CheckAzByUrlExist(TestUuid)
	assert.Nil(t, entity)
	assert.NotNil(t, err)
	assert.False(t, azExists)
}
