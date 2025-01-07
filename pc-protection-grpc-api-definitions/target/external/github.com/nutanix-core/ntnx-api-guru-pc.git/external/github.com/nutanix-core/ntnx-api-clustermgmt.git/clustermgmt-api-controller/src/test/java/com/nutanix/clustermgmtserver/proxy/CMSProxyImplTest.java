package com.nutanix.clustermgmtserver.proxy;

import com.google.protobuf.ServiceException;
import com.nutanix.cluster_management.ClusterManagementErrorProto;
import com.nutanix.cluster_management.ClusterManagementInterfaceProto;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceException;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceRetryException;
import com.nutanix.clustermgmtserver.utils.TestConstantUtils;
import com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto;
import com.nutanix.net.Rpc;
import com.nutanix.net.RpcProto;
import com.nutanix.net.RpcProto.RpcRequestContext;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

public class CMSProxyImplTest {

  @Mock
  private ClusterManagementInterfaceProto.ClusterManagementRpcSvc.BlockingInterface rpcService;

  private CMSProxyImpl cmsProxy;

  @BeforeMethod
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    cmsProxy = new CMSProxyImpl(TestConstantUtils.HOST_IP, TestConstantUtils.CMS_PORT);
  }

  @Test
  public void testNumCmsRpcNames() {
    // Count of RPCs being exposed
    assertEquals(CMSProxyImpl.CMSRpcName.values().length, 3);
    assertEquals(cmsProxy.rpcServiceBiFunctionMap.size(),
      CMSProxyImpl.CMSRpcName.values().length);
  }

  @Test
  public void getRpcService() {
    assertNotNull(cmsProxy.getRpc());
  }

  @Test
  public void testCreateCluster() throws ServiceException, ClustermgmtServiceException{
    cmsProxy.setRpcService(rpcService);
    ClusterManagementInterfaceProto.ClusterCreateRet createclusterRet =  ClusterManagementInterfaceProto.ClusterCreateRet.newBuilder().build();
    when(rpcService.clusterCreate(any(Rpc.class), any(ClusterManagementInterfaceProto.ClusterCreateArg.class)))
      .thenReturn(createclusterRet);
    ClusterManagementInterfaceProto.ClusterCreateArg createclusterArgs = ClusterManagementInterfaceProto.ClusterCreateArg.newBuilder().build();
    ClusterManagementInterfaceProto.ClusterCreateRet rpcResponse =
      cmsProxy.doExecute(CMSProxyImpl.CMSRpcName.CREATE_CLUSTER, createclusterArgs, cmsProxy.getEmptyRequestContextForRpc());
    assertNotNull(rpcResponse);
    assertEquals(createclusterRet, rpcResponse);
  }

  @Test
  public void throwIfErrorWhenNoError() throws ClustermgmtServiceException {
    final Rpc rpc = new Rpc();
    rpc.setResponseRpcStatus(RpcProto.RpcResponseHeader.RpcStatus.kNoError);
    cmsProxy.rpcStatus = rpc.getResponseRpcStatus();
    cmsProxy.throwIfError(rpc);
  }

  @Test(expectedExceptions = ClustermgmtServiceRetryException.class)
  public void throwIfErrorWhenTransportError() throws ClustermgmtServiceException {
    final Rpc rpc = new Rpc();
    rpc.setResponseRpcStatus(RpcProto.RpcResponseHeader.RpcStatus.kTransportError);
    cmsProxy.rpcStatus = rpc.getResponseRpcStatus();
    cmsProxy.throwIfError(rpc);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void throwIfErrorDefaultCase() throws ClustermgmtServiceException{
    final Rpc rpc = new Rpc();
    rpc.setResponseRpcStatus(RpcProto.RpcResponseHeader.RpcStatus.kCanceled);
    cmsProxy.rpcStatus = rpc.getResponseRpcStatus();
    rpc.setResponseErrorDetail("Request canceled");
    cmsProxy.rpcErrorDetail = rpc.getResponseErrorDetail();
    cmsProxy.throwIfError(rpc);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void throwIfErrorWhenAppErrorInvalidArg() throws ClustermgmtServiceException{
    final Rpc rpc = new Rpc();
    rpc.setResponseRpcStatus(RpcProto.RpcResponseHeader.RpcStatus.kAppError);
    cmsProxy.rpcStatus = rpc.getResponseRpcStatus();
    rpc.setResponseAppError(ClusterManagementErrorProto.ClusterManagementError.Type.kInvalidArgument.getNumber());
    cmsProxy.throwIfError(rpc);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void throwIfErrorWhenAppErrorUncaughtException() throws ClustermgmtServiceException{
    final Rpc rpc = new Rpc();
    rpc.setResponseRpcStatus(RpcProto.RpcResponseHeader.RpcStatus.kAppError);
    cmsProxy.rpcStatus = rpc.getResponseRpcStatus();
    rpc.setResponseAppError(ClusterManagementErrorProto.ClusterManagementError.Type.kUncaughtException.getNumber());
    cmsProxy.throwIfError(rpc);
  }

  @Test(expectedExceptions = ClustermgmtServiceRetryException.class)
  public void throwIfErrorWhenAppErrorWithRetry() throws ClustermgmtServiceException {
    final Rpc rpc = new Rpc();
    rpc.setResponseRpcStatus(RpcProto.RpcResponseHeader.RpcStatus.kAppError);
    cmsProxy.rpcStatus = rpc.getResponseRpcStatus();
    rpc.setResponseAppError(ClusterManagementErrorProto.ClusterManagementError.Type.kRetry.getNumber());
    cmsProxy.throwIfError(rpc);
  }
}
