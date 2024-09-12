/*
 * Copyright (c) 2024 Nutanix Inc. All rights reserved.
 *
 * Author: deepanshu.jain@nutanix.com
 * This file contains the PC Domain DTO which is used to create, update and delete PC Domain entity in IDF.
 */

package pc_domain

import (
	"fmt"

	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/external"
	idf_utils "ntnx-api-guru-pc/guru-pc-api-service/utils/idf"

	"github.com/nutanix-core/go-cache/insights/insights_interface"
	"github.com/nutanix-core/go-cache/zeus"
	"github.com/nutanix-core/ntnx-api-guru/services/constants"

	log "github.com/sirupsen/logrus"
)

type PCDomain interface {
	Create() error
	Delete(Uuid string) error
	GetById(Uuid string) (*insights_interface.Entity, error)
	SetAZ(az *zeus.AvailabilityZonePhysical) error
}

type PCDomainImpl struct {
	*zeus.AvailabilityZonePhysical
}

func (pcDomain *PCDomainImpl) SetAZ(az *zeus.AvailabilityZonePhysical) error {
	pcDomain.AvailabilityZonePhysical = az
	return nil
}

func (pcDomain *PCDomainImpl) Create() error {
	log.Debugf("Attempting to save the PC Domain entity to IDF: %+v", pcDomain.Uuid)
	updateEntityArg, err := createIdfUpdateEntityArg(*pcDomain, constants.DEFAULT_CAS_VALUE)
	if err != nil {
		log.Errorf("%s error while while deleting PC for PC Domain: %+v, error: %s", consts.RegisterPCLoggerPrefix, pcDomain.Uuid, err)
		return err
	}

	_, err = external.Interfaces().IdfClient().CreateEntity(updateEntityArg)
	if err != nil {
		log.Errorf("%s error while creating pc domain entity: %+v, error: %s", consts.RegisterPCLoggerPrefix, pcDomain.Uuid, err)
		return err
	}

	log.Debugf("%s created pc domain entity to idf: %+v", consts.RegisterPCLoggerPrefix, pcDomain.GetUuid())
	return nil
}

func (pcDomain *PCDomainImpl) GetById(Uuid string) (*insights_interface.Entity, error) {
	log.Debugf("Attempting to get the PC Domain entity by UUID: %s", Uuid)
	getEntityArg := idf_utils.BuildGetEntityArgById(Uuid, consts.PCDomainEntityName)
	pcDomainEntity, err := external.Interfaces().IdfClient().GetEntityById(getEntityArg)
	if err != nil {
		log.Errorf("%s error while getting the PC Domain entity by UUID: %s, error: %s", consts.RegisterPCLoggerPrefix, Uuid, err)
		return nil, err
	}

	log.Debugf("%s successfully got the PC Domain entity by UUID: %s", consts.RegisterPCLoggerPrefix, Uuid)
	return pcDomainEntity, nil
}

func (pcDomain *PCDomainImpl) Delete(Uuid string) error {
	log.Debugf("%s Attempting to delete the PC Domain entity from IDF: %s", consts.RegisterPCLoggerPrefix, Uuid)

	getPcDomain, err := pcDomain.GetById(Uuid)
	if err != nil {
		return fmt.Errorf("failed to get the PC Domain entity from IDF: %s : %w", Uuid, err)
	}

	deleteEntityArg := idf_utils.BuildDeleteEntityArgById(Uuid, getPcDomain.GetCasValue()+1, consts.PCDomainEntityName)
	_, err = external.Interfaces().IdfClient().DeleteEntityById(deleteEntityArg)
	if err != nil {
		if err == insights_interface.ErrIncorrectCasValue {
			//TODO: Add retry logic here
			log.Errorf("%s error Incorrect Cas Value while deleting PC Domain: %+v, error: %s", consts.RegisterPCLoggerPrefix, Uuid, err)
		}
		log.Errorf("%s error while deleting PC Domain: %+v, error: %s", consts.RegisterPCLoggerPrefix, Uuid, err)
		return fmt.Errorf("failed to delete the PC Domain entity from IDF: %s, error: %w", Uuid, err)
	}

	log.Debugf("successfully deleted the PC Domain entity from IDF: %s", Uuid)
	return nil
}
