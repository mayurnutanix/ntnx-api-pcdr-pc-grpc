package com.nutanix.clustermgmtserver.services.impl;

import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceException;
import com.nutanix.clustermgmtserver.proxy.EntityDbProxyImpl;
import com.nutanix.clustermgmtserver.utils.ClusterTestUtils;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/*
This file contains UTs for following API endpoints:
GET Rackable Units (list)
GET Rackable Units
 */

public class RackableUnitEntityImplTest extends ClusterServiceImplTest {

  public RackableUnitEntityImplTest(){
    super();
  }

  /* UTs for GET Rackable Units (list) */

  @Test
  public void getRackableUnitsTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID));
    when(multiClusterZeusConfig.getZeusConfiguration(any())).thenReturn(ClusterTestUtils.buildZeusConfigurationWithRackableUnit());
    assertEquals(clusterService.getRackableUnits(ClusterTestUtils.CLUSTER_UUID).get(0).getExtId(), ClusterTestUtils.RACKABLE_UNIT_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getRackableUnitsTestInCaseOfException() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    clusterService.getRackableUnits(ClusterTestUtils.CLUSTER_UUID);
  }

  /* UTs for GET Rackable Unit */

  @Test
  public void getRackableUnitTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID));
    when(multiClusterZeusConfig.getZeusConfiguration(any())).thenReturn(ClusterTestUtils.buildZeusConfigurationWithRackableUnit());
    assertEquals(clusterService.getRackableUnit(ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.RACKABLE_UNIT_UUID).getExtId(), ClusterTestUtils.RACKABLE_UNIT_UUID);
  }


  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getRackableUnitTestWithException() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    clusterService.getRackableUnit(ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.RACKABLE_UNIT_UUID);
  }

  @Test
  public void getRackableUnitTestWithOtherConditions() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID));
    when(multiClusterZeusConfig.getZeusConfiguration(any())).thenReturn(ClusterTestUtils.buildZeusConfigurationWithRackableUnitWithConditions());
    assertEquals(clusterService.getRackableUnit(ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.RACKABLE_UNIT_UUID).getExtId(), ClusterTestUtils.RACKABLE_UNIT_UUID);
  }

}
