/*
 * Copyright (c) 2024 Nutanix Inc. All rights reserved.
 * Author: deepanshu.jain@nutanix.com
 *
 * PC Domain Helper Util functions
 */

package pc_domain

import (
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/external"

	constants "ntnx-api-guru-pc/guru-pc-api-service/consts"

	"github.com/nutanix-core/go-cache/insights/insights_interface"
	"github.com/nutanix-core/go-cache/insights/insights_interface/query"
	log "github.com/sirupsen/logrus"
	"google.golang.org/protobuf/proto"
)

// CreateIdfUpdateEntityArg creates an update entity argument for IDF.
func createIdfUpdateEntityArg(pcDomain PCDomainImpl, CasValue uint64) (*insights_interface.UpdateEntityArg, error) {
	log.Debug("Creating UpdateEntityArg for PC domain: ", pcDomain.Uuid)
	attributeDataArgList := []*insights_interface.AttributeDataArg{}

	addAttribute := func(name string, value string) {
		dataArg := createDataArg(name, value)
		attributeDataArgList = append(attributeDataArgList, dataArg)
	}

	if pcDomain.Name != nil {
		addAttribute("name", *pcDomain.Name)
	}

	if pcDomain.Region != nil {
		addAttribute("region", *pcDomain.Region)
	}

	if pcDomain.Type != nil {
		addAttribute("type", pcDomain.Type.Enum().String())
	}

	if pcDomain.Url != nil {
		addAttribute("url", *pcDomain.Url)
	}

	if pcDomain.Uuid != nil {
		addAttribute("uuid", *pcDomain.Uuid)
	}

	updateArg := &insights_interface.UpdateEntityArg{
		EntityGuid: &insights_interface.EntityGuid{
			EntityTypeName: proto.String(constants.PCDomainEntityName),
			EntityId:       pcDomain.Uuid,
		},
		AttributeDataArgList: attributeDataArgList,
		CasValue:             &CasValue,
	}

	err := insights_interface.AddSerializedProto(pcDomain.AvailabilityZonePhysical, updateArg)
	if err != nil {
		return nil, err
	}

	return updateArg, nil
}

func createDataArg(name string, value string) *insights_interface.AttributeDataArg {
	dataArg := &insights_interface.AttributeDataArg{
		AttributeData: &insights_interface.AttributeData{
			Name: proto.String(name),
			Value: &insights_interface.DataValue{
				ValueType: &insights_interface.DataValue_StrValue{
					StrValue: value,
				},
			},
		},
	}
	return dataArg
}

func CheckAzByUrlExist(url string) (bool, *insights_interface.EntityWithMetric, error) {
	whereClause := query.ALL(query.EQ(query.COL("url"), query.STR(url)))
	q, err := query.QUERY("CheckAzByURLQuery").SELECT("uuid", consts.RemoteConnectionUuidAzName, consts.CloudTrustUuidAzName).FROM(constants.PCDomainEntityName).
		WHERE(whereClause).Proto()
	if err != nil {
		log.Errorf("error while creating query: %v", err)
		return false, nil, err
	}
	queryArg := &insights_interface.GetEntitiesWithMetricsArg{Query: q}
	entities, err := external.Interfaces().DbClient().Query(queryArg)

	if err != nil && insights_interface.ErrNotFound.Equals(err) {
		log.Debugf("AZ with url %s doesn't exist: %v", url, err)
		return false, nil, nil
	} else if err != nil {
		log.Errorf("error fetching AZ: %v", err)
		return false, nil, err
	}
	entity := entities[0]
	azUuid, err := entity.GetValue("uuid")
	if err != nil {
		log.Errorf("error while reading entity uuid: %v", err)
		return false, nil, err
	}
	log.Debugf("found AZ entity having uuid %s with url %s with entity spec %v",
		azUuid, url, entity)
	return true, entity, nil
}
