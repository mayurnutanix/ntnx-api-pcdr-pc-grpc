package com.nutanix.clustermgmtserver.services.impl;

import com.nutanix.clustermgmtserver.proxy.ErgonProxyImpl;
import com.nutanix.clustermgmtserver.utils.ClusterTestUtils;
import dp1.clu.clustermgmt.v4.config.SearchResponse;
import dp1.clu.clustermgmt.v4.config.SearchType;
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
      .thenReturn(ClusterTestUtils.getSearchRet(SearchType.UNCONFIGURED_NODES));
    SearchResponse searchResponse = clusterService.getSearchResponse(ClusterTestUtils.TASK_UUID, SearchType.UNCONFIGURED_NODES);
    assertEquals(searchResponse.getSearchType(), SearchType.UNCONFIGURED_NODES);
  }

  @Test
  public void getSearchTestForNodeNetworkingDetails() throws Exception {
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_GET), any()))
      .thenReturn(ClusterTestUtils.getTaskRet())
      .thenReturn(ClusterTestUtils.getSearchRet(SearchType.NETWORKING_DETAILS));
    SearchResponse searchResponse = clusterService.getSearchResponse(ClusterTestUtils.TASK_UUID, SearchType.NETWORKING_DETAILS);
    assertEquals(searchResponse.getSearchType(), SearchType.NETWORKING_DETAILS);
  }

  @Test
  public void getSearchTestForHypervisorUploadParam() throws Exception {
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_GET), any()))
      .thenReturn(ClusterTestUtils.getTaskRet())
      .thenReturn(ClusterTestUtils.getSearchRet(SearchType.HYPERVISOR_UPLOAD_INFO));
    SearchResponse searchResponse = clusterService.getSearchResponse(ClusterTestUtils.TASK_UUID, SearchType.HYPERVISOR_UPLOAD_INFO);
    assertEquals(searchResponse.getSearchType(), SearchType.HYPERVISOR_UPLOAD_INFO);
  }

  @Test
  public void getSearchTestForDiscoverNodesWithEmptyRes() throws Exception {
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_GET), any()))
      .thenReturn(ClusterTestUtils.getTaskRet())
      .thenReturn(ClusterTestUtils.getSearchRetWithEmptyResponse(SearchType.UNCONFIGURED_NODES));
    SearchResponse searchResponse = clusterService.getSearchResponse(ClusterTestUtils.TASK_UUID, SearchType.UNCONFIGURED_NODES);
    assertEquals(searchResponse.getSearchType(), SearchType.UNCONFIGURED_NODES);
  }

  @Test
  public void getSearchTestForNodeNetworkingDetailsWithEmptyRes() throws Exception {
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_GET), any()))
      .thenReturn(ClusterTestUtils.getTaskRet())
      .thenReturn(ClusterTestUtils.getSearchRetWithEmptyResponse(SearchType.NETWORKING_DETAILS));
    SearchResponse searchResponse = clusterService.getSearchResponse(ClusterTestUtils.TASK_UUID, SearchType.NETWORKING_DETAILS);
    assertEquals(searchResponse.getSearchType(), SearchType.NETWORKING_DETAILS);
  }

  @Test
  public void getSearchTestForHypervisorUploadParamWithEmptyRes() throws Exception {
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_GET), any()))
      .thenReturn(ClusterTestUtils.getTaskRet())
      .thenReturn(ClusterTestUtils.getSearchRetWithEmptyResponse(SearchType.HYPERVISOR_UPLOAD_INFO));
    SearchResponse searchResponse = clusterService.getSearchResponse(ClusterTestUtils.TASK_UUID, SearchType.HYPERVISOR_UPLOAD_INFO);
    assertEquals(searchResponse.getSearchType(), SearchType.HYPERVISOR_UPLOAD_INFO);
  }

  @Test
  public void getSearchTestForNonCompatibleClusters() throws Exception {
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_GET), any()))
      .thenReturn(ClusterTestUtils.getTaskRet())
      .thenReturn(ClusterTestUtils.getSearchRetWithEmptyResponse(SearchType.NON_COMPATIBLE_CLUSTERS));
    SearchResponse searchResponse = clusterService.getSearchResponse(ClusterTestUtils.TASK_UUID, SearchType.NON_COMPATIBLE_CLUSTERS);
    assertEquals(searchResponse.getSearchType(), SearchType.NON_COMPATIBLE_CLUSTERS);
  }

  @Test
  public void getSearchTestForNonCompatibleClustersWithEmptyRes() throws Exception {
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_GET), any()))
      .thenReturn(ClusterTestUtils.getTaskRet())
      .thenReturn(ClusterTestUtils.getSearchRet(SearchType.NON_COMPATIBLE_CLUSTERS));
    SearchResponse searchResponse = clusterService.getSearchResponse(ClusterTestUtils.TASK_UUID, SearchType.NON_COMPATIBLE_CLUSTERS);
    assertEquals(searchResponse.getSearchType(), SearchType.NON_COMPATIBLE_CLUSTERS);
  }

  @Test
  public void getSearchTestForValidateBundleInfo() throws Exception {
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_GET), any()))
      .thenReturn(ClusterTestUtils.getTaskRet());
    SearchResponse searchResponse = clusterService.getSearchResponse(ClusterTestUtils.TASK_UUID, SearchType.VALIDATE_BUNDLE_INFO);
    assertEquals(searchResponse.getSearchType(), SearchType.VALIDATE_BUNDLE_INFO);
  }

}
