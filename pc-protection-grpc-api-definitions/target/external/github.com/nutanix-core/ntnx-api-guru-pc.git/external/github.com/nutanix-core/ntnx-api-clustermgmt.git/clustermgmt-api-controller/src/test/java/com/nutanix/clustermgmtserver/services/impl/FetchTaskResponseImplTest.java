package com.nutanix.clustermgmtserver.services.impl;

import com.nutanix.clustermgmtserver.proxy.ErgonProxyImpl;
import com.nutanix.clustermgmtserver.utils.ClusterTestUtils;
import dp1.clu.clustermgmt.v4.config.TaskResponse;
import dp1.clu.clustermgmt.v4.config.TaskResponseType;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/*
This file contains UTs for following API endpoints:
POST Fetch Task Response
 */

public class FetchTaskResponseImplTest extends ClusterServiceImplTest {

  public FetchTaskResponseImplTest(){
    super();
  }

  @Test
  public void getSearchTestForDiscoverNodes() throws Exception {
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_GET), any()))
      .thenReturn(ClusterTestUtils.getTaskRet())
      .thenReturn(ClusterTestUtils.getSearchRet(TaskResponseType.UNCONFIGURED_NODES));
    TaskResponse taskResponse = clusterService.getTaskResponse(ClusterTestUtils.TASK_UUID, TaskResponseType.UNCONFIGURED_NODES);
    assertEquals(taskResponse.getExtId(), ClusterTestUtils.TASK_UUID);
    assertEquals(taskResponse.getTaskResponseType(), TaskResponseType.UNCONFIGURED_NODES);
  }

  @Test
  public void getSearchTestForNodeNetworkingDetails() throws Exception {
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_GET), any()))
      .thenReturn(ClusterTestUtils.getTaskRet())
      .thenReturn(ClusterTestUtils.getSearchRet(TaskResponseType.NETWORKING_DETAILS));
    TaskResponse taskResponse = clusterService.getTaskResponse(ClusterTestUtils.TASK_UUID, TaskResponseType.NETWORKING_DETAILS);
    assertEquals(taskResponse.getTaskResponseType(), TaskResponseType.NETWORKING_DETAILS);
  }

  @Test
  public void getSearchTestForHypervisorUploadParam() throws Exception {
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_GET), any()))
      .thenReturn(ClusterTestUtils.getTaskRet())
      .thenReturn(ClusterTestUtils.getSearchRet(TaskResponseType.HYPERVISOR_UPLOAD_INFO));
    TaskResponse taskResponse = clusterService.getTaskResponse(ClusterTestUtils.TASK_UUID, TaskResponseType.HYPERVISOR_UPLOAD_INFO);
    assertEquals(taskResponse.getTaskResponseType(), TaskResponseType.HYPERVISOR_UPLOAD_INFO);
  }

  @Test
  public void getSearchTestForDiscoverNodesWithEmptyRes() throws Exception {
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_GET), any()))
      .thenReturn(ClusterTestUtils.getTaskRet())
      .thenReturn(ClusterTestUtils.getSearchRetWithEmptyResponse(TaskResponseType.UNCONFIGURED_NODES));
    TaskResponse taskResponse = clusterService.getTaskResponse(ClusterTestUtils.TASK_UUID, TaskResponseType.UNCONFIGURED_NODES);
    assertEquals(taskResponse.getTaskResponseType(), TaskResponseType.UNCONFIGURED_NODES);
  }

  @Test
  public void getSearchTestForNodeNetworkingDetailsWithEmptyRes() throws Exception {
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_GET), any()))
      .thenReturn(ClusterTestUtils.getTaskRet())
      .thenReturn(ClusterTestUtils.getSearchRetWithEmptyResponse(TaskResponseType.NETWORKING_DETAILS));
    TaskResponse taskResponse = clusterService.getTaskResponse(ClusterTestUtils.TASK_UUID, TaskResponseType.NETWORKING_DETAILS);
    assertEquals(taskResponse.getTaskResponseType(), TaskResponseType.NETWORKING_DETAILS);
  }

  @Test
  public void getSearchTestForHypervisorUploadParamWithEmptyRes() throws Exception {
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_GET), any()))
      .thenReturn(ClusterTestUtils.getTaskRet())
      .thenReturn(ClusterTestUtils.getSearchRetWithEmptyResponse(TaskResponseType.HYPERVISOR_UPLOAD_INFO));
    TaskResponse taskResponse = clusterService.getTaskResponse(ClusterTestUtils.TASK_UUID, TaskResponseType.HYPERVISOR_UPLOAD_INFO);
    assertEquals(taskResponse.getTaskResponseType(), TaskResponseType.HYPERVISOR_UPLOAD_INFO);
  }

  @Test
  public void getSearchTestForNonCompatibleClusters() throws Exception {
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_GET), any()))
      .thenReturn(ClusterTestUtils.getTaskRet())
      .thenReturn(ClusterTestUtils.getSearchRetWithEmptyResponse(TaskResponseType.NON_COMPATIBLE_CLUSTERS));
    TaskResponse taskResponse = clusterService.getTaskResponse(ClusterTestUtils.TASK_UUID, TaskResponseType.NON_COMPATIBLE_CLUSTERS);
    assertEquals(taskResponse.getTaskResponseType(), TaskResponseType.NON_COMPATIBLE_CLUSTERS);
  }

  @Test
  public void getSearchTestForNonCompatibleClustersWithEmptyRes() throws Exception {
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_GET), any()))
      .thenReturn(ClusterTestUtils.getTaskRet())
      .thenReturn(ClusterTestUtils.getSearchRet(TaskResponseType.NON_COMPATIBLE_CLUSTERS));
    TaskResponse taskResponse = clusterService.getTaskResponse(ClusterTestUtils.TASK_UUID, TaskResponseType.NON_COMPATIBLE_CLUSTERS);
    assertEquals(taskResponse.getTaskResponseType(), TaskResponseType.NON_COMPATIBLE_CLUSTERS);
  }

  @Test
  public void getSearchTestForValidateBundleInfo() throws Exception {
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_GET), any()))
      .thenReturn(ClusterTestUtils.getTaskRet());
    TaskResponse taskResponse = clusterService.getTaskResponse(ClusterTestUtils.TASK_UUID, TaskResponseType.VALIDATE_BUNDLE_INFO);
    assertEquals(taskResponse.getTaskResponseType(), TaskResponseType.VALIDATE_BUNDLE_INFO);
  }

}
