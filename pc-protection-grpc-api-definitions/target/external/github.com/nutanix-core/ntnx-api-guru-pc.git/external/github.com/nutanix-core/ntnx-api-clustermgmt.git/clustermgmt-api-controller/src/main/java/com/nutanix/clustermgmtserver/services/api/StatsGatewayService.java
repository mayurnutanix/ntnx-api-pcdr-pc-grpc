/*
 * Copyright (c) 2024 Nutanix Inc. All rights reserved.
 *
 * Author: rakesh.falak@nutanix.com
 */

package com.nutanix.clustermgmtserver.services.api;

import com.google.protobuf.ServiceException;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceException;
import com.nutanix.stats_gateway.graphql_interface.GroupsGraphqlInterfaceProto.GroupsGraphqlRet;

public interface StatsGatewayService {
  GroupsGraphqlRet executeQuery(String query) throws ServiceException, ClustermgmtServiceException;
}