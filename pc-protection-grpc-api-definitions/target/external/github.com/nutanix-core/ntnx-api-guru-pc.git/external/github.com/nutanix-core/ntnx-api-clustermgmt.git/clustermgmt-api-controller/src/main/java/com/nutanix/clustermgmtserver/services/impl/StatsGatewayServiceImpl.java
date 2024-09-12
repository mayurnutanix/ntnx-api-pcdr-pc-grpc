/*
 * Copyright (c) 2024 Nutanix Inc. All rights reserved.
 *
 * Author: rakesh.falak@nutanix.com
 */

package com.nutanix.clustermgmtserver.services.impl;

import com.google.protobuf.BlockingRpcChannel;
import com.google.protobuf.ServiceException;
import com.nutanix.clustermgmtserver.utils.RequestContextHelper;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceException;
import com.nutanix.clustermgmtserver.services.api.StatsGatewayService;
import com.nutanix.net.Rpc;
import com.nutanix.net.RpcClientOnHttp;
import com.nutanix.net.RpcProto.RpcRequestContext;
import com.nutanix.stats_gateway.graphql_interface.GroupsGraphqlInterfaceProto.GroupsGraphqlArg;
import com.nutanix.stats_gateway.graphql_interface.GroupsGraphqlInterfaceProto.GroupsGraphqlRet;
import com.nutanix.stats_gateway.graphql_interface.GroupsGraphqlInterfaceProto.GroupsGraphqlRpcSvc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

@Slf4j
public class StatsGatewayServiceImpl implements StatsGatewayService {

  public static final int RETRY_DELAY = 2000;
  public static final int MAX_RETRY_DELAY = 60000;
  public static final int RETRY_BACKOFF_FACTOR = 2;

  private GroupsGraphqlRpcSvc.BlockingInterface rpcService;

  public StatsGatewayServiceImpl(final String host, final int port) {
    BlockingRpcChannel stub = new RpcClientOnHttp(host, port);
    this.rpcService = GroupsGraphqlRpcSvc.newBlockingStub(stub);
  }

  void setRpcService(GroupsGraphqlRpcSvc.BlockingInterface rpcService){
    this.rpcService = rpcService;
  }

  @Retryable(value=ServiceException.class,
    backoff=@Backoff(delay = RETRY_DELAY, multiplier = RETRY_BACKOFF_FACTOR, maxDelay = MAX_RETRY_DELAY))
  public GroupsGraphqlRet executeQuery(String query) throws ClustermgmtServiceException {
    log.debug("GraphQL Query: " + query);
    final GroupsGraphqlArg arg = GroupsGraphqlArg.newBuilder().setQuery(query).build();
    //final RpcRequestContext context = RequestContextHelper.getRpcRequestContext();
    final Rpc rpc = new Rpc();
    //rpc.getRequestHeader().setRequestContext(context);

    try {
      return this.rpcService.executeGraphql(rpc, arg);
    } catch (ServiceException e) {
      log.error("ServiceException occurred while invoking Stats Gateway RPC. Query: " + query + ", Details:", e);
      throw new ClustermgmtServiceException("Error occurred while querying the database service");
    }
  }
}