package com.nutanix.clustermgmtserver.services.impl;

import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceException;
import com.nutanix.clustermgmtserver.proxy.EntityDbProxyImpl;
import com.nutanix.clustermgmtserver.utils.ClusterTestUtils;
import com.nutanix.clustermgmtserver.utils.HostTestUtils;
import com.nutanix.util.base.Pair;
import dp1.clu.clustermgmt.v4.config.VirtualGpuProfile;
import dp1.clu.clustermgmt.v4.config.PhysicalGpuProfile;
import dp1.clu.clustermgmt.v4.config.HostGpu;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/*
This file contains UTs for following API endpoints:
GET Host Gpus on cluster associated to a host (list)
GET all Host Gpus on cluster (list)
GET all Host Gpu Profiles on cluster
GET Host Gpu entity
GET all Host Gpus (list)
 */

public class GpuEntityImplTest extends ClusterServiceImplTest {

  public GpuEntityImplTest(){
    super();
  }

  /* UTs for GET Host Gpus on cluster associated to a host (list) */

  @Test
  public void getHostGpusEntityTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getHostGpuIdfEntityObj(true, HostTestUtils.HOST_GPU_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(HostTestUtils.getHostGpuIdfEntityMetricObj(true));
    Pair<Integer, List<HostGpu>> response = clusterService.getHostGpus(HostTestUtils.CLUSTER_UUID,HostTestUtils.HOST_UUID,2,1, "(numberOfVgpusAllocated gt 4)", "numberOfVgpusAllocated", "numberOfVgpusAllocated");
    Long value = 1L;
    assertEquals(response.right().get(0).getNumberOfVgpusAllocated(), value);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getHostGpusEntityTestInCaseOfException() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    Pair<Integer, List<HostGpu>> response = clusterService.getHostGpus(HostTestUtils.CLUSTER_UUID,HostTestUtils.HOST_UUID,2,1, "(numberOfVgpusAllocated gt 4)", "numberOfVgpusAllocated", null);
  }

  /* UTs for GET all Host Gpus on cluster (list) */

  @Test
  public void getClusterHostGpusEntityTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getHostGpuIdfEntityObj(true, HostTestUtils.HOST_GPU_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(HostTestUtils.getHostGpuIdfEntityMetricObj(true));
    Pair<Integer, List<HostGpu>> response  = clusterService.getClusterHostGpus(HostTestUtils.CLUSTER_UUID,2,1, "(numberOfVgpusAllocated gt 4)", "numberOfVgpusAllocated", "numberOfVgpusAllocated");
    Long value = 1L;
    assertEquals(response.right().get(0).getNumberOfVgpusAllocated(), value);
  }

  /* UTs for GET all Host Gpu Profiles on cluster (list) */

  @Test
  public void getGpuVirtualProfilesTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getHostGpuIdfEntityObj(true, HostTestUtils.HOST_GPU_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(HostTestUtils.getHostGpuIdfEntityMetricObj(true));
    Pair<Integer, List<VirtualGpuProfile>> response = clusterService.listVirtualGpuProfiles(HostTestUtils.CLUSTER_UUID, 1, 0, null, null);
    assertEquals(response.right().get(0).getVirtualGpuConfig().getDeviceId(), HostTestUtils.HOST_VIRTUAL_GPU_DEVICE_ID);
  }

  @Test
  public void getGpuPhysicalProfilesTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getHostGpuIdfEntityObj(false, HostTestUtils.HOST_GPU_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(HostTestUtils.getHostGpuIdfEntityMetricObj(false));
    Pair<Integer, List<PhysicalGpuProfile>> response = clusterService.listPhysicalGpuProfiles(HostTestUtils.CLUSTER_UUID, 1, 0, null, null);
    assertEquals(response.right().get(0).getPhysicalGpuConfig().getDeviceId(), HostTestUtils.HOST_PHYSICAL_GPU_DEVICE_ID);
  }

  /* UTs for GET Host Gpu entity */

  @Test
  public void getHostGpuEntityTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getHostGpuIdfEntityObj(true, HostTestUtils.HOST_GPU_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(HostTestUtils.getHostGpuIdfEntityMetricObj(true));
    HostGpu response = clusterService.getHostGpu(HostTestUtils.CLUSTER_UUID,
      HostTestUtils.HOST_UUID, HostTestUtils.HOST_GPU_UUID);
    assertEquals(response.getExtId(), HostTestUtils.HOST_GPU_UUID);
  }

  //empty metric entity object
  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getHostGpuEntityTestInCaseOfException() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getHostGpuIdfEntityObj(true, HostTestUtils.HOST_GPU_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(HostTestUtils.getEmptyMetricRetEntity());
    clusterService.getHostGpu(HostTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID, HostTestUtils.HOST_GPU_UUID);
  }

  //host does not belong to cluster
  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getHostGpuEntityTestInCaseOfException2() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getEntityWhereHostDoesNotBelongToCluster(HostTestUtils.HOST_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(HostTestUtils.getHostGpuIdfEntityMetricObj(true));
    clusterService.getHostGpu(HostTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID, HostTestUtils.HOST_GPU_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getHostGpuEntityTestInCaseOfPcException() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(HostTestUtils.CLUSTER_UUID));
    clusterService.getHostGpu(HostTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID, HostTestUtils.HOST_GPU_UUID);
  }

  /* UTs for GET all Host Gpus on cluster */

  @Test
  public void getAllHostGpusEntityTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getHostGpuIdfEntityObj(true, HostTestUtils.HOST_GPU_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(HostTestUtils.getHostGpuIdfEntityMetricObj(true));
    Pair<Integer, List<HostGpu>> response  = clusterService.getAllHostGpus(2,1, "(numberOfVgpusAllocated gt 4)", "numberOfVgpusAllocated", "numberOfVgpusAllocated");
    Long value = 1L;
    assertEquals(response.right().get(0).getNumberOfVgpusAllocated(), value);
  }

}
