package com.nutanix.clustermgmtserver.services.impl;

import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceException;
import com.nutanix.clustermgmtserver.proxy.EntityDbProxyImpl;
import com.nutanix.clustermgmtserver.proxy.ErgonProxyImpl;
import com.nutanix.clustermgmtserver.utils.ClusterTestUtils;
import com.nutanix.clustermgmtserver.utils.HostTestUtils;

import dp1.clu.clustermgmt.v4.config.*;
import dp1.clu.prism.v4.config.TaskReference;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/*
This file contains UTs for following API endpoints:
POST Compute Non Migratable VMs
GET Non Migratable VMs Result
*/

public class ComputeNonMigratableWorkflowImplTest extends ClusterServiceImplTest {

  public ComputeNonMigratableWorkflowImplTest(){
    super();
  }

  @Test
  public void computeNonMigratableVmsTestIpv4() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    String ipType = "ipv4";
    ComputeNonMigratableVmsSpec computeNonMigratableVms = ClusterTestUtils.getNonMigratableVms(ipType);
    clusterService.computeNonMigratableVms(ClusterTestUtils.CLUSTER_UUID, computeNonMigratableVms, ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void computeNonMigratableVmsTestIpv6() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    String ipType = "ipv6";
    ComputeNonMigratableVmsSpec computeNonMigratableVms = ClusterTestUtils.getNonMigratableVms(ipType);
    clusterService.computeNonMigratableVms(ClusterTestUtils.CLUSTER_UUID, computeNonMigratableVms, ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void computeNonMigratableVmsTestFqdn() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    String ipType = "fqdn";
    ComputeNonMigratableVmsSpec computeNonMigratableVms = ClusterTestUtils.getNonMigratableVms(ipType);
    clusterService.computeNonMigratableVms(ClusterTestUtils.CLUSTER_UUID, computeNonMigratableVms, ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void computeNonMigratableVmsTestNatsFail() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(false);
    String ipType = "ipv4";
    ComputeNonMigratableVmsSpec computeNonMigratableVms = ClusterTestUtils.getNonMigratableVms(ipType);
    clusterService.computeNonMigratableVms(ClusterTestUtils.CLUSTER_UUID, computeNonMigratableVms, ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void computeNonMigratableVmsTestisMultiCluster() throws Exception{
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID));
    String ipType = "ipv4";
    ComputeNonMigratableVmsSpec computeNonMigratableVms = ClusterTestUtils.getNonMigratableVms(ipType);
    clusterService.computeNonMigratableVms(ClusterTestUtils.CLUSTER_UUID, computeNonMigratableVms, ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void getNonMigratableVmsResultTest() throws Exception{
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(ClusterTestUtils.buildGetEntitiesWithMetricsRetForNonMigratableVms());
    clusterService.getNonMigratableVmsResult(ClusterTestUtils.RESOURCE_ID);
}

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getNonMigratableVmsResultTestNoEntity() throws Exception{
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(ClusterTestUtils.getNonMigratableVmsResultWithMetricRetWithNoEntity());
    clusterService.getNonMigratableVmsResult(ClusterTestUtils.RESOURCE_ID);
  }

}