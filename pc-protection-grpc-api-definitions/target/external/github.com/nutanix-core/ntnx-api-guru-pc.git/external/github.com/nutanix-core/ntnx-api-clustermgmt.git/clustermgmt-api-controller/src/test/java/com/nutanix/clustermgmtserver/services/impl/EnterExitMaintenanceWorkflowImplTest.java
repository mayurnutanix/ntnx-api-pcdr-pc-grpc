package com.nutanix.clustermgmtserver.services.impl;

import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceException;
import com.nutanix.clustermgmtserver.proxy.EntityDbProxyImpl;
import com.nutanix.clustermgmtserver.proxy.ErgonProxyImpl;
import com.nutanix.clustermgmtserver.utils.ClusterTestUtils;
import com.nutanix.clustermgmtserver.utils.HostTestUtils;

import dp1.clu.clustermgmt.v4.config.*;
import dp1.clu.clustermgmt.v4.operations.*;
import dp1.clu.prism.v4.config.TaskReference;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/*
This file contains UTs for following API endpoints:
POST Enter Host Maintenance
POST Exit Host Maintenance
*/

public class EnterExitMaintenanceWorkflowImplTest extends ClusterServiceImplTest {

  public EnterExitMaintenanceWorkflowImplTest(){
    super();
  }

  @Test
  public void enterHostMaintenanceTestIpv4() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    String ipType = "ipv4";
    EnterHostMaintenanceSpec enterHostMaintenance = ClusterTestUtils.getEnterHostMaintenance(ipType);
    clusterService.enterHostMaintenance(ClusterTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID, enterHostMaintenance, ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void enterHostMaintenanceTestIpv6() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    String ipType = "ipv6";
    EnterHostMaintenanceSpec enterHostMaintenance = ClusterTestUtils.getEnterHostMaintenance(ipType);
    clusterService.enterHostMaintenance(ClusterTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID, enterHostMaintenance, ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void enterHostMaintenanceTestFqdn() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    String ipType = "fqdn";
    EnterHostMaintenanceSpec enterHostMaintenance = ClusterTestUtils.getEnterHostMaintenance(ipType);
    clusterService.enterHostMaintenance(ClusterTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID, enterHostMaintenance, ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void enterHostMaintenanceTestisMultiCluster() throws Exception{
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID));
    String ipType = "ipv4";
    EnterHostMaintenanceSpec enterHostMaintenance = ClusterTestUtils.getEnterHostMaintenance(ipType);
    clusterService.enterHostMaintenance(ClusterTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID, enterHostMaintenance, ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void enterHostMaintenanceTestNatsFail() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(false);
    String ipType = "ipv4";
    EnterHostMaintenanceSpec enterHostMaintenance = ClusterTestUtils.getEnterHostMaintenance(ipType);
    clusterService.enterHostMaintenance(ClusterTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID, enterHostMaintenance, ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void exitHostMaintenanceTestIpv4() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    String ipType = "ipv4";
    HostMaintenanceCommonSpec exitHostMaintenance = ClusterTestUtils.getExitHostMaintenance(ipType);
    clusterService.exitHostMaintenance(ClusterTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID, exitHostMaintenance, ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void exitHostMaintenanceTestIpv6() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    String ipType = "ipv6";
    HostMaintenanceCommonSpec exitHostMaintenance = ClusterTestUtils.getExitHostMaintenance(ipType);
    clusterService.exitHostMaintenance(ClusterTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID, exitHostMaintenance, ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void exitHostMaintenanceTestFqdn() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    String ipType = "fqdn";
    HostMaintenanceCommonSpec exitHostMaintenance = ClusterTestUtils.getExitHostMaintenance(ipType);
    clusterService.exitHostMaintenance(ClusterTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID, exitHostMaintenance, ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void exitHostMaintenanceTestisMultiCluster() throws Exception{
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID));
    String ipType = "ipv4";
    HostMaintenanceCommonSpec exitHostMaintenance = ClusterTestUtils.getExitHostMaintenance(ipType);
    clusterService.exitHostMaintenance(ClusterTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID, exitHostMaintenance, ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void exitHostMaintenanceTestNatsFail() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(false);
    String ipType = "ipv4";
    HostMaintenanceCommonSpec exitHostMaintenance = ClusterTestUtils.getExitHostMaintenance(ipType);
    clusterService.exitHostMaintenance(ClusterTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID, exitHostMaintenance, ClusterTestUtils.TASK_UUID);
  }
}