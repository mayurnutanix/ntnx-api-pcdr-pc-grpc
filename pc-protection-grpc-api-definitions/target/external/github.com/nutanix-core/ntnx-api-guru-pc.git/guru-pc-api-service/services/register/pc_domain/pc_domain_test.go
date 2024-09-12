package pc_domain

import (
	"errors"
	"fmt"
	"ntnx-api-guru-pc/guru-pc-api-service/test"
	"testing"

	"github.com/golang/mock/gomock"
	"github.com/golang/protobuf/proto"
	"github.com/nutanix-core/go-cache/insights/insights_interface"
	"github.com/nutanix-core/go-cache/zeus"
	"github.com/stretchr/testify/assert"
)

var (
	TestUuid   = "e994b411-808f-4775-9ce5-a261e3947782"
	TestName   = "PC-Domain"
	TestRegion = "India"
	Testurl    = "https://www.example.com"
	TestType   = "Xi"
	ErrTest    = errors.New("Test Error")
)

func TestCreateSuccess(t *testing.T) {
	az := zeus.AvailabilityZonePhysical{
		Uuid: &TestUuid,
	}
	pcDomain := PCDomainImpl{}
	pcDomain.SetAZ(&az)
	pcDomain.Uuid = &TestUuid
	pcDomain.Name = &TestName
	pcDomain.Region = &TestRegion
	pcDomain.Url = &Testurl
	pcDomain.Type = zeus.AvailabilityZonePhysicalType_kCloud.Enum()

	m := test.MockSingletons(t)
	m.IdfClient.EXPECT().CreateEntity(gomock.Any()).Return(nil, nil)
	err := pcDomain.Create()
	assert.Nil(t, err)
}

func TestCreateFailure_CreateUpdateArgFailed(t *testing.T) {
	az := zeus.AvailabilityZonePhysical{
		Uuid: &TestUuid,
	}
	pcDomain := PCDomainImpl{}
	pcDomain.SetAZ(&az)

	pcDomain.Uuid = &TestUuid
	pcDomain.Name = &TestName
	pcDomain.Region = &TestRegion
	pcDomain.Url = &Testurl
	pcDomain.Type = zeus.AvailabilityZonePhysicalType_kCloud.Enum()

	m := test.MockSingletons(t)
	m.IdfClient.EXPECT().CreateEntity(gomock.Any()).Return(nil, ErrTest)
	err := pcDomain.Create()
	assert.EqualError(t, err, ErrTest.Error())
}

func TestGetByIdSuccess(t *testing.T) {
	az := zeus.AvailabilityZonePhysical{
		Uuid: &TestUuid,
	}
	pcDomain := PCDomainImpl{}
	pcDomain.SetAZ(&az)
	pcDomainEntity := insights_interface.Entity{
		EntityGuid: &insights_interface.EntityGuid{
			EntityTypeName: proto.String(TestName),
			EntityId:       proto.String(TestUuid),
		},
	}
	m := test.MockSingletons(t)
	m.IdfClient.EXPECT().GetEntityById(gomock.Any()).Return(&pcDomainEntity, nil)
	entity, err := pcDomain.GetById(TestUuid)
	assert.Nil(t, err)
	assert.Equal(t, pcDomainEntity, *entity)
}

func TestGetByIdFailure_GetEntityFailed(t *testing.T) {
	az := zeus.AvailabilityZonePhysical{
		Uuid: &TestUuid,
	}
	pcDomain := PCDomainImpl{}
	pcDomain.SetAZ(&az)
	m := test.MockSingletons(t)
	m.IdfClient.EXPECT().GetEntityById(gomock.Any()).Return(nil, ErrTest)
	_, err := pcDomain.GetById(TestUuid)
	assert.EqualError(t, err, ErrTest.Error())
}

func TestDeleteSuccess(t *testing.T) {
	az := zeus.AvailabilityZonePhysical{
		Uuid: &TestUuid,
	}
	pcDomain := PCDomainImpl{}
	pcDomain.SetAZ(&az)
	pcDomainEntity := insights_interface.Entity{
		EntityGuid: &insights_interface.EntityGuid{
			EntityTypeName: proto.String(TestName),
			EntityId:       proto.String(TestUuid),
		},
		CasValue: proto.Uint64(0),
	}
	m := test.MockSingletons(t)
	m.IdfClient.EXPECT().GetEntityById(gomock.Any()).Return(&pcDomainEntity, nil)
	m.IdfClient.EXPECT().DeleteEntityById(gomock.Any()).Return(nil, nil)
	err := pcDomain.Delete(TestUuid)
	assert.Nil(t, err)
}

func TestDeleteFailure_GetEntityFailed(t *testing.T) {
	az := zeus.AvailabilityZonePhysical{
		Uuid: &TestUuid,
	}
	pcDomain := PCDomainImpl{}
	pcDomain.SetAZ(&az)
	m := test.MockSingletons(t)
	m.IdfClient.EXPECT().GetEntityById(gomock.Any()).Return(nil, ErrTest)
	err := pcDomain.Delete(TestUuid)
	assert.EqualError(t, err, fmt.Sprintf("failed to get the PC Domain entity from IDF: %s : %s", TestUuid, ErrTest))
}

func TestDeleteFailure_DeletEntityFailed(t *testing.T) {
	az := zeus.AvailabilityZonePhysical{
		Uuid: &TestUuid,
	}
	pcDomain := PCDomainImpl{}
	pcDomain.SetAZ(&az)
	pcDomainEntity := insights_interface.Entity{
		EntityGuid: &insights_interface.EntityGuid{
			EntityTypeName: proto.String(TestName),
			EntityId:       proto.String(TestUuid),
		},
		CasValue: proto.Uint64(0),
	}
	m := test.MockSingletons(t)
	m.IdfClient.EXPECT().GetEntityById(gomock.Any()).Return(&pcDomainEntity, nil)
	m.IdfClient.EXPECT().DeleteEntityById(gomock.Any()).Return(nil, insights_interface.ErrIncorrectCasValue)
	err := pcDomain.Delete(TestUuid)
	assert.EqualError(t, err, fmt.Sprintf(
		"failed to delete the PC Domain entity from IDF: %s, error: %s",
		TestUuid, insights_interface.ErrIncorrectCasValue))
}
