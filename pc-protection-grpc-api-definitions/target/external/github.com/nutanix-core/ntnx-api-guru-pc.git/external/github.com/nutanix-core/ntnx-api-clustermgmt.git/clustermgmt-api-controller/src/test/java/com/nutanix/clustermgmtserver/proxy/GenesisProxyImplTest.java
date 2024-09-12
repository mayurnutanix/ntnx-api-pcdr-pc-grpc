package com.nutanix.clustermgmtserver.proxy;

import com.google.protobuf.ServiceException;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceException;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceRetryException;
import com.nutanix.clustermgmtserver.utils.TestConstantUtils;
import com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto;
import com.nutanix.net.Rpc;
import com.nutanix.net.RpcProto;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;

public class GenesisProxyImplTest {

  @Mock
  private GenesisInterfaceProto.GenesisRpcSvc.BlockingInterface rpcService;

  private GenesisProxyImpl genesisProxy;

  @BeforeMethod
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    genesisProxy = new GenesisProxyImpl(TestConstantUtils.HOST_IP, TestConstantUtils.GENESIS_PORT);
  }

  @Test
  public void testNumGenesisRpcNames() {
    // Count of RPCs being exposed
    assertEquals(GenesisProxyImpl.GenesisRpcName.values().length, 15);
    assertEquals(genesisProxy.rpcServiceBiFunctionMap.size(),
      GenesisProxyImpl.GenesisRpcName.values().length);
  }

  @Test
  public void getRpcService() {
    assertNotNull(genesisProxy.getRpc());
  }

  @Test
  public void testUpdateCluster() throws ServiceException, ClustermgmtServiceException {
    genesisProxy.setRpcService(rpcService);
    GenesisInterfaceProto.UpdateClusterRet updateClusterRet = GenesisInterfaceProto.UpdateClusterRet.newBuilder().build();
    when(rpcService.updateCluster(any(Rpc.class), any(GenesisInterfaceProto.UpdateClusterArg.class)))
      .thenReturn(updateClusterRet);
    GenesisInterfaceProto.UpdateClusterArg updateClusterArg = GenesisInterfaceProto.UpdateClusterArg.newBuilder().build();
    GenesisInterfaceProto.UpdateClusterRet rpcResponse =
      genesisProxy.doExecute(GenesisProxyImpl.GenesisRpcName.UPDATE_CLUSTER, updateClusterArg, RpcProto.RpcRequestContext.getDefaultInstance());
    assertNotNull(rpcResponse);
    assertEquals(updateClusterRet, rpcResponse);
  }

  @Test
  public void throwIfErrorWhenNoError() throws ClustermgmtServiceRetryException,
    ClustermgmtServiceException {

    final Rpc rpc = new Rpc();
    rpc.setResponseRpcStatus(RpcProto.RpcResponseHeader.RpcStatus.kNoError);
    genesisProxy.rpcStatus = rpc.getResponseRpcStatus();
    genesisProxy.throwIfError(rpc);
  }

  @Test(expectedExceptions = ClustermgmtServiceRetryException.class)
  public void throwIfErrorWhenTransportError() throws ClustermgmtServiceRetryException,
    ClustermgmtServiceException {

    final Rpc rpc = new Rpc();
    rpc.setResponseRpcStatus(RpcProto.RpcResponseHeader.RpcStatus.kTransportError);
    genesisProxy.rpcStatus = rpc.getResponseRpcStatus();
    genesisProxy.throwIfError(rpc);
  }

  @Test
  public void throwIfErrorWhenAppErrorWithoutRetry() throws ClustermgmtServiceRetryException {

    final Rpc rpc = new Rpc();
    rpc.setResponseRpcStatus(RpcProto.RpcResponseHeader.RpcStatus.kAppError);
    genesisProxy.rpcStatus = rpc.getResponseRpcStatus();
    rpc.setResponseAppError(GenesisInterfaceProto.GenesisGenericError.ErrorType.kInputError.getNumber());
    rpc.setResponseErrorDetail("Invalid Argument");
    genesisProxy.rpcErrorDetail = rpc.getResponseErrorDetail();
    try {
      genesisProxy.throwIfError(rpc);
      fail();
    }
    catch (final ClustermgmtServiceException e) {
      assertEquals(e.getMessage(),
        "Application error kInputError raised: Invalid " +
          "Argument");
    }
  }

  @Test
  public void throwIfErrorDefaultCase() throws ClustermgmtServiceRetryException {

    final Rpc rpc = new Rpc();
    rpc.setResponseRpcStatus(RpcProto.RpcResponseHeader.RpcStatus.kCanceled);
    genesisProxy.rpcStatus = rpc.getResponseRpcStatus();
    rpc.setResponseErrorDetail("Request canceled");
    genesisProxy.rpcErrorDetail = rpc.getResponseErrorDetail();
    try {
      genesisProxy.throwIfError(rpc);
      fail();
    }
    catch (final ClustermgmtServiceException e) {
      assertEquals(e.getMessage(),
        "RPC error kCanceled raised: Request canceled");
    }
  }

  @Test(expectedExceptions = ClustermgmtServiceRetryException.class)
  public void throwIfErrorWhenAppErrorWithRetry()
    throws ClustermgmtServiceRetryException, ClustermgmtServiceException {

    final Rpc rpc = new Rpc();
    rpc.setResponseRpcStatus(RpcProto.RpcResponseHeader.RpcStatus.kAppError);
    genesisProxy.rpcStatus = rpc.getResponseRpcStatus();
    rpc.setResponseAppError(GenesisInterfaceProto.GenesisGenericError.ErrorType.kRetry.getNumber());
    genesisProxy.throwIfError(rpc);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void throwIfErrorWhenAppErrorWithNotFound()
    throws ClustermgmtServiceException {

    final Rpc rpc = new Rpc();
    rpc.setResponseRpcStatus(RpcProto.RpcResponseHeader.RpcStatus.kAppError);
    genesisProxy.rpcStatus = rpc.getResponseRpcStatus();
    rpc.setResponseAppError(GenesisInterfaceProto.GenesisGenericError.ErrorType.kNotFound.getNumber());
    genesisProxy.throwIfError(rpc);
  }
}
