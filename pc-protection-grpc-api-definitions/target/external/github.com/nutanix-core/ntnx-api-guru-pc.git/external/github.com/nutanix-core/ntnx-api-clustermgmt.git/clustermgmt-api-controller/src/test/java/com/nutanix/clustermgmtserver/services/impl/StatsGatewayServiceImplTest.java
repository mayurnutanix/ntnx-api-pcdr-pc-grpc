/*
 * Copyright (c) 2024 Nutanix Inc. All rights reserved.
 *
 * Author: rakesh.falak@nutanix.com
 */

package com.nutanix.clustermgmtserver.services.impl;

import com.google.protobuf.ServiceException;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceException;
import com.nutanix.stats_gateway.graphql_interface.GroupsGraphqlInterfaceProto.GroupsGraphqlRet;
import com.nutanix.stats_gateway.graphql_interface.GroupsGraphqlInterfaceProto.GroupsGraphqlRpcSvc;
import org.mockito.Mockito;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class StatsGatewayServiceImplTest {

  private StatsGatewayServiceImpl statsGatewayService;

  private GroupsGraphqlRpcSvc.BlockingInterface rpcService;

  private static final String HOST = "127.0.0.1";
  private static final int PORT = 8084;

  @BeforeTest
  public void setUp() {
    statsGatewayService = new StatsGatewayServiceImpl(HOST, PORT);
    rpcService = Mockito.mock(GroupsGraphqlRpcSvc.BlockingInterface.class);
    statsGatewayService.setRpcService(rpcService);
  }

  @Test
  public void testExecuteQuery() throws ServiceException, ClustermgmtServiceException {
    final GroupsGraphqlRet expected = GroupsGraphqlRet.newBuilder().setData("foo").build();
    when(rpcService.executeGraphql(any(), any())).thenReturn(expected);

    final GroupsGraphqlRet actual = statsGatewayService.executeQuery("bar");

    assertEquals(actual, expected);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void testExecuteQueryServiceException() throws ServiceException, ClustermgmtServiceException {
    when(rpcService.executeGraphql(any(), any())).thenThrow(new ServiceException("oh no"));
    statsGatewayService.executeQuery("foo");
  }
}