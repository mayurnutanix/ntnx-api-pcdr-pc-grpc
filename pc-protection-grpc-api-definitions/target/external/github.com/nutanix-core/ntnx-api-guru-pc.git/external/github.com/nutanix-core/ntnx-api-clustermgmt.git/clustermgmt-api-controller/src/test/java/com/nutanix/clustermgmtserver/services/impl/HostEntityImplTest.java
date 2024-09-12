package com.nutanix.clustermgmtserver.services.impl;

import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceException;
import com.nutanix.clustermgmtserver.proxy.EntityDbProxyImpl;
import com.nutanix.clustermgmtserver.proxy.ErgonProxyImpl;
import com.nutanix.clustermgmtserver.utils.ClusterTestUtils;
import com.nutanix.clustermgmtserver.utils.HostTestUtils;
import com.nutanix.util.base.Pair;
import dp1.clu.clustermgmt.v4.config.Host;
import dp1.clu.prism.v4.config.TaskReference;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/*
This file contains UTs for following API endpoints:
GET Host entity
GET Host entities(list)
POST Rename Host
GET all Hosts(list)
 */

public class HostEntityImplTest extends ClusterServiceImplTest {

  public HostEntityImplTest(){
    super();
  }

  /* UTs for GET Host entity */

  @Test
  public void getHostEntityTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getHostIdfEntityObj(HostTestUtils.HOST_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(HostTestUtils.buildGetEntitiesWithMetricsRetForHost(HostTestUtils.CLUSTER_UUID));
    when(multiClusterZeusConfig.getZeusConfiguration(any())).thenReturn(HostTestUtils.buildZeusConfiguration());
    assertEquals(clusterService.getHostEntity(HostTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID).getHostName(), HostTestUtils.HOST_NAME);
  }

  //empty metric object
  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getHostEntityTestInCaseOfException() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getHostIdfEntityObj(HostTestUtils.HOST_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(HostTestUtils.getEmptyMetricRetEntity());
    clusterService.getHostEntity(HostTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID);
  }

  //host does not belong to cluster
  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getHostEntityTestInCaseOfException2() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getEntityWhereHostDoesNotBelongToCluster(HostTestUtils.HOST_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(HostTestUtils.buildGetEntitiesWithMetricsRetForHost("5cafb340-dfd4-4eaf-ac8d-5ff91a230c7a"));
    when(multiClusterZeusConfig.getZeusConfiguration(any())).thenReturn(HostTestUtils.buildZeusConfiguration());
    clusterService.getHostEntity(HostTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getHostEntityTestInCaseOfPcException() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(HostTestUtils.CLUSTER_UUID));
    clusterService.getHostEntity(HostTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID);
  }


  @Test
  public void getHostEntityTestWithExtraConditions() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getHostIdfEntityObj(HostTestUtils.HOST_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(HostTestUtils.buildGetEntitiesWithMetricsRetForHost(HostTestUtils.CLUSTER_UUID));
    when(multiClusterZeusConfig.getZeusConfiguration(any())).thenReturn(HostTestUtils.buildZeusConfigurationWithConditions());
    assertEquals(clusterService.getHostEntity(HostTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID).getHostName(), HostTestUtils.HOST_NAME);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getHostEntityTestInCaseOfExceptions() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getHostIdfEntityObjWithNoEntity());
    clusterService.getHostEntity(HostTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID);
  }

  /* UTs for GET Host entities(list) */

  @Test
  public void getHostEntitiesTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(HostTestUtils.buildGetEntitiesWithMetricsRetForHost(HostTestUtils.CLUSTER_UUID));
    when(multiClusterZeusConfig.getZeusConfiguration(any())).thenReturn(HostTestUtils.buildZeusConfiguration());
    Pair<Integer, List<Host>> response = clusterService.getHostEntities(HostTestUtils.CLUSTER_UUID,0,1, "contains(hostName, 'test-host-name')", "hostName", null, "hostName");
    assertEquals(response.right().get(0).getHostName(), HostTestUtils.HOST_NAME);
  }

  @Test
  public void getHostEntitiesWithoutSelectTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(HostTestUtils.buildGetEntitiesWithMetricsRetForHost(HostTestUtils.CLUSTER_UUID));
    when(multiClusterZeusConfig.getZeusConfiguration(any())).thenReturn(HostTestUtils.buildZeusConfiguration());
    Pair<Integer, List<Host>> response = clusterService.getHostEntities(HostTestUtils.CLUSTER_UUID,0,1, "contains(hostName, 'test-host-name') and (numberOfCpuCores le 1)", "hostName", null, null);
    assertEquals(response.right().get(0).getHostName(), HostTestUtils.HOST_NAME);
  }

  @Test
  public void getHostEntitiesTestWithMoreFilteringConditions() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(HostTestUtils.buildGetEntitiesWithMetricsRetForHost(HostTestUtils.CLUSTER_UUID));
    when(multiClusterZeusConfig.getZeusConfiguration(any())).thenReturn(HostTestUtils.buildZeusConfiguration());
    Pair<Integer, List<Host>> response = clusterService.getHostEntities(HostTestUtils.CLUSTER_UUID,0,1, "endswith(tolower(hostName), 'name')", null, null, null);
    assertEquals(response.right().size(), 1);
  }

  @Test
  public void getHostEntitiesTestWithSelect() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(HostTestUtils.buildGetEntitiesWithMetricsRetForHost(HostTestUtils.CLUSTER_UUID));
    when(multiClusterZeusConfig.getZeusConfiguration(any())).thenReturn(HostTestUtils.buildZeusConfiguration());
    Pair<Integer, List<Host>> response = clusterService.getHostEntities(HostTestUtils.CLUSTER_UUID,0,1, null, null, null, "hostName");
    assertEquals(response.right().get(0).getHostName(), HostTestUtils.HOST_NAME);
  }

  /* UTs for POST Rename Host */

  @Test
  public void renameHostTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getHostIdfEntityObj(HostTestUtils.HOST_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(HostTestUtils.getHostIdfEntityObj(HostTestUtils.HOST_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(HostTestUtils.buildGetEntitiesWithMetricsRetForHost(HostTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    TaskReference task = clusterService.renameHost(ClusterTestUtils.getHostNameParam("new-host-name"), HostTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void renameHostNatsFailTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getHostIdfEntityObj(HostTestUtils.HOST_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(HostTestUtils.getHostIdfEntityObj(HostTestUtils.HOST_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(HostTestUtils.buildGetEntitiesWithMetricsRetForHost(HostTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(false);
    TaskReference task = clusterService.renameHost(ClusterTestUtils.getHostNameParam("new-host-name"), HostTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void renameHostTestWithDuplicateHostName() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getHostIdfEntityObj(HostTestUtils.HOST_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(HostTestUtils.getHostIdfEntityObj(HostTestUtils.HOST_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(HostTestUtils.buildGetEntitiesWithMetricsRetForHost(HostTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    TaskReference task = clusterService.renameHost(ClusterTestUtils.getHostNameParam("test-host-name"), HostTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  /* UTs for GET all Hosts(list) */

  @Test
  public void getAllHostEntitiesTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(HostTestUtils.buildGetEntitiesWithMetricsRetForHost(HostTestUtils.CLUSTER_UUID));
    when(multiClusterZeusConfig.getZeusConfiguration(any())).thenReturn(HostTestUtils.buildZeusConfiguration());
    Pair<Integer, List<Host>> response = clusterService.getAllHostEntities(0,1, "contains(hostName, 'test-host-name')", "hostName", null, "hostName");
    assertEquals(response.right().get(0).getHostName(), HostTestUtils.HOST_NAME);
  }

  @Test
  public void getAllHostEntitiesWithoutSelectTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(HostTestUtils.buildGetEntitiesWithMetricsRetForHost(HostTestUtils.CLUSTER_UUID));
    when(multiClusterZeusConfig.getZeusConfiguration(any())).thenReturn(HostTestUtils.buildZeusConfiguration());
    Pair<Integer, List<Host>> response = clusterService.getAllHostEntities(0,1, "contains(hostName, 'test-host-name')", "hostName", null, null);
    assertEquals(response.right().get(0).getHostName(), HostTestUtils.HOST_NAME);
  }

}
