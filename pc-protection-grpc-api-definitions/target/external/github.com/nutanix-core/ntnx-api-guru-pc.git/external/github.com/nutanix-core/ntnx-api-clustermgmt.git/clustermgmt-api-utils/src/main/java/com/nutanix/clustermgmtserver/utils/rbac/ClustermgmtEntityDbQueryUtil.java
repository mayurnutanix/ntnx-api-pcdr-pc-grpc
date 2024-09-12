/*
 * Copyright (c) 2023 Nutanix Inc. All rights reserved.
 *
 * Author: rakesh.falak@nutanix.com
 */
package com.nutanix.clustermgmtserver.utils.rbac;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nutanix.insights.ifc.InsightsInterfaceProto;
import com.nutanix.prism.exception.EntityDbException;
import com.nutanix.prism.service.EntityDbService;

import lombok.extern.slf4j.Slf4j;


/**
 * Helper class to query entity DB for given entity type and post process response.
 */
@Slf4j
@Component("clustermgmtEntityDbQueryUtil")
public class ClustermgmtEntityDbQueryUtil {
  private final EntityDbService entityDbService;

  @Autowired
  public ClustermgmtEntityDbQueryUtil(final EntityDbService entityDbService) {
    this.entityDbService = entityDbService;
  }

  public InsightsInterfaceProto.GetEntitiesRet getEntityFromEntityDb(String entityName, String extId) {
    try {
      InsightsInterfaceProto.GetEntitiesRet getEntitiesRet = entityDbService.getEntities(
        InsightsInterfaceProto.GetEntitiesArg.newBuilder().addEntityGuidList(
          InsightsInterfaceProto.EntityGuid.newBuilder()
            .setEntityTypeName(entityName)
            .setEntityId(extId)
            .build())
          .build());

      return getEntitiesRet;
    } catch (EntityDbException ex) {
      log.error("Entity DB query failed", ex);
    }
    return null;
  }
}