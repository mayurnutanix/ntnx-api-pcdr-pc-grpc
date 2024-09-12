/*
 * Copyright (c) 2021 Nutanix Inc. All rights reserved.
 *
 * Author: rakesh.falak@nutanix.com
 */

package com.nutanix.clustermgmtserver.proxy;

import com.google.protobuf.ServiceException;
import com.nutanix.ergon.ErgonErrorProto;
import com.nutanix.net.Rpc;
import com.nutanix.net.RpcProto;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceException;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceRetryException;
import com.nutanix.clustermgmtserver.utils.TestConstantUtils;
import nutanix.ergon.ErgonInterface;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

public class ErgonProxyImplTest {

  @Mock
  private ErgonInterface.ErgonRpcSvc.BlockingInterface rpcService;

  private ErgonProxyImpl ergonProxy;

  @BeforeMethod
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    ergonProxy = new ErgonProxyImpl(TestConstantUtils.HOST_IP, TestConstantUtils.ERGON_PORT);
  }

  @Test
  public void testNumErgonRpcNames() {
    // Count of RPCs being exposed
    assertEquals(ErgonProxyImpl.ErgonRpcName.values().length, 3);
    assertEquals(ergonProxy.rpcServiceBiFunctionMap.size(),
      ErgonProxyImpl.ErgonRpcName.values().length);
  }

  @Test
  public void getRpcService() {
    assertNotNull(ergonProxy.getRpc());
  }

  @Test
  public void testGetTask() throws ServiceException, ClustermgmtServiceException {
    ergonProxy.setRpcService(rpcService);
    ErgonInterface.TaskGetRet taskGetRet = ErgonInterface.TaskGetRet.newBuilder().build();
    when(rpcService.taskGet(any(Rpc.class), any(ErgonInterface.TaskGetArg.class)))
      .thenReturn(taskGetRet);
    ErgonInterface.TaskGetArg taskGetArg = ErgonInterface.TaskGetArg.newBuilder().build();
    ErgonInterface.TaskGetRet rpcResponse =
      ergonProxy.doExecute(ErgonProxyImpl.ErgonRpcName.TASK_GET, taskGetArg);
    assertNotNull(rpcResponse);
    assertEquals(taskGetRet, rpcResponse);
  }

  @Test
  public void testCreateTask() throws ServiceException, ClustermgmtServiceException {
    ergonProxy.setRpcService(rpcService);
    ErgonInterface.TaskCreateRet taskCreateRet = ErgonInterface.TaskCreateRet.newBuilder().build();
    when(rpcService.taskCreate(any(Rpc.class), any(ErgonInterface.TaskCreateArg.class)))
      .thenReturn(taskCreateRet);
    ErgonInterface.TaskCreateArg taskCreateArg = ErgonInterface.TaskCreateArg.newBuilder().build();
    ErgonInterface.TaskCreateRet rpcResponse =
      ergonProxy.doExecute(ErgonProxyImpl.ErgonRpcName.TASK_CREATE, taskCreateArg);
    assertNotNull(rpcResponse);
    assertEquals(taskCreateRet, rpcResponse);
  }

  @Test
  public void testUpdateTask() throws ServiceException, ClustermgmtServiceException {
    ergonProxy.setRpcService(rpcService);
    ErgonInterface.TaskUpdateRet taskUpdateRet = ErgonInterface.TaskUpdateRet.newBuilder().build();
    when(rpcService.taskUpdate(any(Rpc.class), any(ErgonInterface.TaskUpdateArg.class)))
      .thenReturn(taskUpdateRet);
    ErgonInterface.TaskUpdateArg taskUpdateArg = ErgonInterface.TaskUpdateArg.newBuilder().build();
    ErgonInterface.TaskUpdateRet rpcResponse =
      ergonProxy.doExecute(ErgonProxyImpl.ErgonRpcName.TASK_UPDATE, taskUpdateArg);
    assertNotNull(rpcResponse);
    assertEquals(taskUpdateRet, rpcResponse);
  }


  @Test
  public void throwIfErrorWhenNoError() throws ClustermgmtServiceRetryException,
    ClustermgmtServiceException {

    final Rpc rpc = new Rpc();
    rpc.setResponseRpcStatus(RpcProto.RpcResponseHeader.RpcStatus.kNoError);
    ergonProxy.rpcStatus = rpc.getResponseRpcStatus();
    ergonProxy.throwIfError(rpc);
  }

  @Test(expectedExceptions = ClustermgmtServiceRetryException.class)
  public void throwIfErrorWhenTransportError() throws ClustermgmtServiceRetryException,
    ClustermgmtServiceException {

    final Rpc rpc = new Rpc();
    rpc.setResponseRpcStatus(RpcProto.RpcResponseHeader.RpcStatus.kTransportError);
    ergonProxy.rpcStatus = rpc.getResponseRpcStatus();
    ergonProxy.throwIfError(rpc);
  }

  @Test(expectedExceptions = ClustermgmtServiceRetryException.class)
  public void throwIfErrorWhenAppErrorWithRetry()
    throws ClustermgmtServiceRetryException, ClustermgmtServiceException {

    final Rpc rpc = new Rpc();
    rpc.setResponseRpcStatus(RpcProto.RpcResponseHeader.RpcStatus.kAppError);
    ergonProxy.rpcStatus = rpc.getResponseRpcStatus();
    rpc.setResponseAppError(ErgonErrorProto.ErgonError.Type.kRetry.getNumber());
    ergonProxy.throwIfError(rpc);
  }

  @Test
  public void throwIfErrorWhenAppErrorWithNotFoundWithRetry()
    throws ClustermgmtServiceException {

    final Rpc rpc = new Rpc();
    rpc.setResponseRpcStatus(RpcProto.RpcResponseHeader.RpcStatus.kAppError);
    ergonProxy.rpcStatus = rpc.getResponseRpcStatus();
    rpc.setResponseAppError(ErgonErrorProto.ErgonError.Type.kNotFound.getNumber());
    try {
      ergonProxy.throwIfError(rpc);
      fail();
    }
    catch (final ClustermgmtServiceException e) {
      assertEquals(e.getMessage(),
        "Unable to process the task as it is not synced to the PC");
    }
  }

  @Test
  public void throwIfErrorWhenAppErrorWithoutRetry() throws ClustermgmtServiceRetryException {

    final Rpc rpc = new Rpc();
    rpc.setResponseRpcStatus(RpcProto.RpcResponseHeader.RpcStatus.kAppError);
    ergonProxy.rpcStatus = rpc.getResponseRpcStatus();
    rpc.setResponseAppError(ErgonErrorProto.ErgonError.Type.kInvalidArgument.getNumber());
    rpc.setResponseErrorDetail("Invalid Argument");
    ergonProxy.rpcErrorDetail = rpc.getResponseErrorDetail();
    try {
      ergonProxy.throwIfError(rpc);
      fail();
    }
    catch (final ClustermgmtServiceException e) {
      assertEquals(e.getMessage(),
        "Application error kInvalidArgument raised: Invalid " +
          "Argument");
    }
  }

  @Test
  public void throwIfErrorDefaultCase() throws ClustermgmtServiceRetryException {

    final Rpc rpc = new Rpc();
    rpc.setResponseRpcStatus(RpcProto.RpcResponseHeader.RpcStatus.kCanceled);
    ergonProxy.rpcStatus = rpc.getResponseRpcStatus();
    rpc.setResponseErrorDetail("Request canceled");
    ergonProxy.rpcErrorDetail = rpc.getResponseErrorDetail();
    try {
      ergonProxy.throwIfError(rpc);
      fail();
    }
    catch (final ClustermgmtServiceException e) {
      assertEquals(e.getMessage(),
        "RPC error kCanceled raised: Request canceled");
    }
  }
}
