/*
 * Copyright (c) 2021 Nutanix Inc. All rights reserved.
 *
 * Author: rakesh.falak@nutanix.com
 */

package com.nutanix.clustermgmtserver.proxy;

import com.google.protobuf.ServiceException;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtGenericException;
import com.nutanix.insights.ifc.InsightsInterfaceProto;
import com.nutanix.net.Rpc;
import com.nutanix.net.RpcProto;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceException;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceRetryException;
import com.nutanix.clustermgmtserver.utils.TestConstantUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

public class EntityDbProxyImplTest {

  @Mock
  private InsightsInterfaceProto.InsightsRpcSvc.BlockingInterface rpcService;

  private EntityDbProxyImpl entityDbProxy;

  @BeforeMethod
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    entityDbProxy = new EntityDbProxyImpl(TestConstantUtils.HOST_IP, TestConstantUtils.INSIGHTS_PORT);
  }

  @Test
  public void testNumEntityDbRpcNames() {
    // Count of RPCs being exposed
    assertEquals(EntityDbProxyImpl.EntityDbRpcName.values().length, 2);
    assertEquals(entityDbProxy.rpcServiceBiFunctionMap.size(),
      EntityDbProxyImpl.EntityDbRpcName.values().length);
  }

  @Test
  public void getRpcService() {
    assertNotNull(entityDbProxy.getRpc());
  }

  @Test
  public void testGetEntities() throws ServiceException, ClustermgmtServiceException {
    entityDbProxy.setRpcService(rpcService);
    InsightsInterfaceProto.GetEntitiesRet getEntitiesRet =
      InsightsInterfaceProto.GetEntitiesRet.newBuilder().build();
    when(rpcService.getEntities(any(Rpc.class), any(InsightsInterfaceProto.GetEntitiesArg.class)))
      .thenReturn(getEntitiesRet);
    InsightsInterfaceProto.GetEntitiesArg getEntitiesArg =
      InsightsInterfaceProto.GetEntitiesArg.newBuilder().build();
    InsightsInterfaceProto.GetEntitiesRet rpcResponse =
      entityDbProxy.doExecute(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES, getEntitiesArg);
    assertNotNull(rpcResponse);
    assertEquals(getEntitiesRet, rpcResponse);
  }

  @Test
  public void testGetEntitiesWithMetrics() throws ServiceException, ClustermgmtServiceException {
    entityDbProxy.setRpcService(rpcService);
    InsightsInterfaceProto.GetEntitiesWithMetricsRet getEntitiesWithMetricsRet =
      InsightsInterfaceProto.GetEntitiesWithMetricsRet.newBuilder().build();
    when(rpcService.getEntitiesWithMetrics(
      any(Rpc.class), any(InsightsInterfaceProto.GetEntitiesWithMetricsArg.class)))
      .thenReturn(getEntitiesWithMetricsRet);
    InsightsInterfaceProto.GetEntitiesWithMetricsArg getEntitiesWithMetricsArg =
      InsightsInterfaceProto.GetEntitiesWithMetricsArg.newBuilder().build();
    InsightsInterfaceProto.GetEntitiesWithMetricsRet rpcResponse =
      entityDbProxy.doExecute(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS,
        getEntitiesWithMetricsArg);
    assertNotNull(rpcResponse);
    assertEquals(getEntitiesWithMetricsRet, rpcResponse);
  }

  @Test(expectedExceptions = ClustermgmtGenericException.class)
  public void testGetEntitiesInCaseOfServiceException()
    throws ServiceException, ClustermgmtServiceException {
    entityDbProxy.setRpcService(rpcService);
    when(rpcService.getEntities(any(Rpc.class), any(InsightsInterfaceProto.GetEntitiesArg.class)))
      .thenThrow(new ServiceException("Service exception"));
    InsightsInterfaceProto.GetEntitiesArg getEntitiesArg =
      InsightsInterfaceProto.GetEntitiesArg.newBuilder().build();
    entityDbProxy.doExecute(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES, getEntitiesArg);
  }

  @Test(expectedExceptions = ClustermgmtGenericException.class)
  public void testGetEntitiesInCaseOfRetryException()
    throws ClustermgmtServiceException, ServiceException {
    when(rpcService.getEntities(any(Rpc.class), any(InsightsInterfaceProto.GetEntitiesArg.class)))
      .thenAnswer(invocationOnMock -> {
        throw new ServiceException("RPC error");
      });
    InsightsInterfaceProto.GetEntitiesArg getEntitiesArg =
      InsightsInterfaceProto.GetEntitiesArg.newBuilder().build();
    entityDbProxy.doExecute(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES, getEntitiesArg);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void testDoExecuteInCaseOfInvalidRpcMethodName() throws ClustermgmtServiceException {
    InsightsInterfaceProto.GetEntitiesArg getEntitiesArg =
      InsightsInterfaceProto.GetEntitiesArg.newBuilder().build();
    entityDbProxy.doExecute("randomRpcName", getEntitiesArg,
      RpcProto.RpcRequestContext.newBuilder().build());
  }

  @Test
  public void throwIfErrorWhenNoError() throws ClustermgmtServiceRetryException,
    ClustermgmtServiceException {

    final Rpc rpc = new Rpc();
    rpc.setResponseRpcStatus(RpcProto.RpcResponseHeader.RpcStatus.kNoError);
    entityDbProxy.rpcStatus = rpc.getResponseRpcStatus();
    entityDbProxy.throwIfError(rpc);
  }

  @Test(expectedExceptions = ClustermgmtServiceRetryException.class)
  public void throwIfErrorWhenTransportError() throws ClustermgmtServiceRetryException,
    ClustermgmtServiceException {

    final Rpc rpc = new Rpc();
    rpc.setResponseRpcStatus(RpcProto.RpcResponseHeader.RpcStatus.kTransportError);
    entityDbProxy.rpcStatus = rpc.getResponseRpcStatus();
    entityDbProxy.throwIfError(rpc);
  }

  @Test(expectedExceptions = ClustermgmtServiceRetryException.class)
  public void throwIfErrorWhenTimeout() throws ClustermgmtServiceRetryException,
    ClustermgmtServiceException {

    final Rpc rpc = new Rpc();
    rpc.setResponseRpcStatus(RpcProto.RpcResponseHeader.RpcStatus.kTimeout);
    entityDbProxy.rpcStatus = rpc.getResponseRpcStatus();
    entityDbProxy.throwIfError(rpc);
  }

  @Test(expectedExceptions = ClustermgmtServiceRetryException.class)
  public void throwIfErrorWhenAppErrorWithRetry()
    throws ClustermgmtServiceRetryException, ClustermgmtServiceException {

    final Rpc rpc = new Rpc();
    rpc.setResponseRpcStatus(RpcProto.RpcResponseHeader.RpcStatus.kAppError);
    entityDbProxy.rpcStatus = rpc.getResponseRpcStatus();
    rpc.setResponseAppError(InsightsInterfaceProto.InsightsErrorProto.Type.kRetry.getNumber());
    entityDbProxy.throwIfError(rpc);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void throwIfErrorWhenAppErrorWithNotFound()
    throws ClustermgmtServiceRetryException, ClustermgmtServiceException {

    final Rpc rpc = new Rpc();
    rpc.setResponseRpcStatus(RpcProto.RpcResponseHeader.RpcStatus.kAppError);
    entityDbProxy.rpcStatus = rpc.getResponseRpcStatus();
    rpc.setResponseAppError(InsightsInterfaceProto.InsightsErrorProto.Type.kNotFound.getNumber());
    entityDbProxy.throwIfError(rpc);
  }

  @Test
  public void throwIfErrorWhenAppErrorWithoutRetry()
    throws ClustermgmtServiceRetryException {

    final Rpc rpc = new Rpc();
    rpc.setResponseRpcStatus(RpcProto.RpcResponseHeader.RpcStatus.kAppError);
    entityDbProxy.rpcStatus = rpc.getResponseRpcStatus();
    rpc.setResponseAppError(InsightsInterfaceProto.InsightsErrorProto.Type.kInvalidRequest.getNumber());
    rpc.setResponseErrorDetail("Invalid Request");
    entityDbProxy.rpcErrorDetail = rpc.getResponseErrorDetail();
    try {
      entityDbProxy.throwIfError(rpc);
      fail();
    }
    catch (final ClustermgmtServiceException e) {
      assertEquals(e.getMessage(),
        "Application error kInvalidRequest raised: Invalid Request");
    }
  }

  @Test
  public void throwIfErrorDefaultCase() throws ClustermgmtServiceRetryException {

    final Rpc rpc = new Rpc();
    rpc.setResponseRpcStatus(RpcProto.RpcResponseHeader.RpcStatus.kCanceled);
    entityDbProxy.rpcStatus = rpc.getResponseRpcStatus();
    rpc.setResponseErrorDetail("Request canceled");
    entityDbProxy.rpcErrorDetail = rpc.getResponseErrorDetail();
    try {
      entityDbProxy.throwIfError(rpc);
      fail();
    }
    catch (final ClustermgmtServiceException e) {
      assertEquals(e.getMessage(),
        "RPC error kCanceled raised: Request canceled");
    }
  }
}