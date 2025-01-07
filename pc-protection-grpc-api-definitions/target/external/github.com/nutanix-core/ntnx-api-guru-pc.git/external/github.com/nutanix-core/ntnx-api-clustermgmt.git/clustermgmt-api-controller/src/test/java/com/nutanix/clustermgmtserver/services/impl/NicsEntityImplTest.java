package com.nutanix.clustermgmtserver.services.impl;

import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceException;
import com.nutanix.clustermgmtserver.proxy.EntityDbProxyImpl;
import com.nutanix.clustermgmtserver.utils.ClusterTestUtils;
import com.nutanix.clustermgmtserver.utils.HostTestUtils;
import com.nutanix.util.base.Pair;
import com.nutanix.clustermgmtserver.utils.TestConstantUtils;
import com.nutanix.clustermgmtserver.proxy.GenesisProxyImpl;
import dp1.clu.clustermgmt.v4.config.CategoryEntityReferences;
import dp1.clu.clustermgmt.v4.config.HostNic;
import dp1.clu.clustermgmt.v4.config.NetworkSwitchInterface;
import dp1.clu.clustermgmt.v4.config.VirtualNic;
import org.testng.annotations.Test;

import java.util.List;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/*
This file contains UTs for following API endpoints:
GET Host Nics (list)
GET Host Nic Entity
GET Host Nics (list) - irrespective of hosts
GET Virtual Nics (list)
GET Virtual Nic Entity
POST Associate Categories to Host Nic Entity
POST Disassociate Categories to Host Nic Entity
 */

public class NicsEntityImplTest extends ClusterServiceImplTest {

  public NicsEntityImplTest(){
    super();
  }

  /* UT for list Network Switch Interface entities */
  @Test
  public void getNetworkSwitchInterfaceListTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getNetworkSwitchIntfIdfEntityObj(HostTestUtils.NETWORK_SWITCH_INTF_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(HostTestUtils.getNetworkSwitchIntfIdfEntityMetricObj());
    List<NetworkSwitchInterface> networkSwitchInterfaces = clusterService.getSwitchInterfaceEntities(HostTestUtils.HOST_UUID);
    assertEquals(networkSwitchInterfaces.get(0).getAttachedHostUuid(), HostTestUtils.HOST_UUID);
  }

  /* UTs for GET Host Nics (list) associated to host on cluster */

  @Test
  public void getHostNicsEntityTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getHostNicIdfEntityObj(HostTestUtils.HOST_UUID, HostTestUtils.HOST_NICS_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(HostTestUtils.getHostNicIdfEntityMetricObj(HostTestUtils.HOST_UUID))
      .thenReturn(HostTestUtils.getNetworkSwitchIntfIdfEntityMetricObj());
    final Pair<Integer, List<HostNic>> response = clusterService.getHostNics(HostTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID, 2, 1, "contains(name, 'test_host_nic_name')", "name", null);
    assertEquals(response.right().get(0).getExtId(), HostTestUtils.HOST_NICS_UUID);
  }

  @Test
  public void getHostNicsEntityTestWithSelection() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getHostNicIdfEntityObj(HostTestUtils.HOST_UUID, HostTestUtils.HOST_NICS_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(HostTestUtils.getHostNicIdfEntityMetricObj(HostTestUtils.HOST_UUID))
      .thenReturn(HostTestUtils.getNetworkSwitchIntfIdfEntityMetricObj());
    final Pair<Integer, List<HostNic>> response = clusterService.getHostNics(HostTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID, 2, 1, null, null, "nodeUuid,macAddress,switchPortId");
    assertEquals(response.right().get(0).getExtId(), HostTestUtils.HOST_NICS_UUID);
    assertEquals(response.right().get(0).getMacAddress(), "00:25:90:d8:77:3f");
    Long txRingSize = response.right().get(0).getTxRingSizeInBytes();
    Long val = Long.valueOf(20);
    assertEquals(txRingSize, val);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getHostNicsEntityTestInCaseOfException1() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    clusterService.getHostNics(HostTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID, 2, 1, "contains(name, 'test_host_nic_name')", "name",null);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getHostNicsEntityTestInCaseOfException2() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getEntityWhereHostDoesNotBelongToCluster(HostTestUtils.HOST_UUID));
    clusterService.getHostNics(HostTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID, 2, 1, "contains(name, 'test_host_nic_name')", "name", null);
  }

  @Test
  public void getHostNicsTestWithoutSortingandFilteringandSelection() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getHostNicIdfEntityObj(HostTestUtils.HOST_UUID, HostTestUtils.HOST_NICS_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(HostTestUtils.getHostNicIdfEntityMetricObj(HostTestUtils.HOST_NICS_UUID))
      .thenReturn(HostTestUtils.getNetworkSwitchIntfIdfEntityMetricObj());
    Pair<Integer, List<HostNic>> response = clusterService.getHostNics(HostTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID, 2, 1,null, null, null);
    assertEquals(response.right().get(0).getExtId(), HostTestUtils.HOST_NICS_UUID);
  }

  /* UTs for GET Host Nic Entity associated to host on cluster */

  @Test
  public void getHostNicEntityTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getHostNicIdfEntityObj(HostTestUtils.HOST_UUID, HostTestUtils.HOST_NICS_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(HostTestUtils.getHostNicIdfEntityMetricObj(HostTestUtils.HOST_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(HostTestUtils.getNetworkSwitchIntfIdfEntityMetricObj());
    HostNic response = clusterService.getHostNic(HostTestUtils.CLUSTER_UUID,
      HostTestUtils.HOST_UUID, HostTestUtils.HOST_NICS_UUID);
    assertEquals(response.getExtId(), HostTestUtils.HOST_NICS_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getHostNicEntityTestInCaseOfException1() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    clusterService.getHostNic(HostTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID, HostTestUtils.HOST_NICS_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getHostNicEntityTestInCaseOfException2() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getEntityWhereHostDoesNotBelongToCluster(HostTestUtils.HOST_UUID));
    clusterService.getHostNic(HostTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID, HostTestUtils.HOST_NICS_UUID);
  }

  //empty metric object
  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getHostNicEntityTestInCaseOfException3() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getHostNicIdfEntityObj(HostTestUtils.HOST_UUID, HostTestUtils.HOST_NICS_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(HostTestUtils.getEmptyMetricRetEntity());
    clusterService.getHostNic(HostTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID, HostTestUtils.HOST_NICS_UUID);
  }

  // Test to get an invalid HOST NIC ENTITY
  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getHostNicEntityTestInCaseOfException() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getHostNicIdfEntityObj(HostTestUtils.HOST_UUID, HostTestUtils.HOST_NICS_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(HostTestUtils.getHostNicIdfEntityMetricObj("5caf4b40-dfd4-4eaf-ac8d-5ff91a230c7a"))
      .thenReturn(HostTestUtils.getNetworkSwitchIntfIdfEntityMetricObj());
    clusterService.getHostNic(HostTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID, HostTestUtils.HOST_NICS_UUID);
  }

  /* UTs for GET Host Nics (list) irrespective of hosts */

  @Test
  public void getAllHostNicsTest() throws Exception {
    List<String> supportedCapabilitiesList = new ArrayList<>();
    supportedCapabilitiesList.add("test_supported_capabilities");
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getHostNicIdfEntityObj(HostTestUtils.HOST_UUID, HostTestUtils.HOST_NICS_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(HostTestUtils.getHostNicIdfEntityMetricObj(HostTestUtils.HOST_UUID))
      .thenReturn(HostTestUtils.getNetworkSwitchIntfIdfEntityMetricObj());
    Pair<Integer, List<HostNic>> response = clusterService.getAllHostNics(2, 1, null, "name", null);
    assertEquals(response.right().get(0).getExtId(), HostTestUtils.HOST_NICS_UUID);
    assertEquals(response.right().get(0).getExtId(), HostTestUtils.HOST_NICS_UUID);
    assertEquals(response.right().get(0).getClusterUuid(), HostTestUtils.CLUSTER_UUID);
    assertEquals(response.right().get(0).getNicProfileId(), "test_nic_profile_id");
    Long linkCapacityInMbps = response.right().get(0).getLinkCapacityInMbps();
    Long val = Long.valueOf(10);
    assertEquals(linkCapacityInMbps, val);
    assertEquals(response.right().get(0).getSupportedCapabilitiesList(), supportedCapabilitiesList);
    assertEquals(response.right().get(0).getDriverVersion(), "test_driver_version");
    assertEquals(response.right().get(0).getFirmwareVersion(), "test_firmware_version");
  }

  @Test
  public void getAllHostNicsTestWithFiltering() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getHostNicIdfEntityObj(HostTestUtils.HOST_UUID, HostTestUtils.HOST_NICS_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(HostTestUtils.getHostNicIdfEntityMetricObj(HostTestUtils.HOST_UUID))
      .thenReturn(HostTestUtils.getNetworkSwitchIntfIdfEntityMetricObj());
    Pair<Integer, List<HostNic>> response = clusterService.getAllHostNics(2, 1, "contains(name, 'test_host_nic_name')", "name", null);
    assertEquals(response.right().get(0).getExtId(), HostTestUtils.HOST_NICS_UUID);
  }

  /* UTs for GET Virtual Nics (list) associated to host on cluster */

  @Test
  public void getVirtualNicsEntityTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getVirtualNicIdfEntityObj(HostTestUtils.HOST_UUID, HostTestUtils.VIRTUAL_NICS_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(HostTestUtils.getVirtualNicIdfEntityMetricObj(HostTestUtils.HOST_UUID));
    final Pair<Integer, List<VirtualNic>> response =
      clusterService.getVirtualNics(HostTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID, 2, 1, "contains(name, 'test_virtual_nic_name')", "name",null);
    assertEquals(response.right().get(0).getExtId(), HostTestUtils.VIRTUAL_NICS_UUID);
  }

  @Test
  public void getVirtualNicsEntityTestWithSelection() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getVirtualNicIdfEntityObj(HostTestUtils.HOST_UUID, HostTestUtils.VIRTUAL_NICS_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(HostTestUtils.getVirtualNicIdfEntityMetricObj(HostTestUtils.HOST_UUID));
    final Pair<Integer, List<VirtualNic>> response =
      clusterService.getVirtualNics(HostTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID, 2, 1, "contains(name, 'test_virtual_nic_name')", "name","name");
    assertEquals(response.right().get(0).getExtId(), HostTestUtils.VIRTUAL_NICS_UUID);
    assertEquals(response.right().get(0).getName(), "test_virtual_nic_name");
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getVirtualNicsEntityTestInCaseOfException1() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    clusterService.getVirtualNics(HostTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID, 2, 1, "contains(name, 'test_virtual_nic_name')", "name", null);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getVirtualNicsEntityTestInCaseOfException2() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getEntityWhereHostDoesNotBelongToCluster(HostTestUtils.HOST_UUID));
    clusterService.getVirtualNics(HostTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID, 2, 1, "contains(name, 'test_virtual_nic_name')", "name", null);
  }

  @Test
  public void getVirtualNicsTestWithoutSortingandFilteringandSelection() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getVirtualNicIdfEntityObj(HostTestUtils.HOST_UUID, HostTestUtils.VIRTUAL_NICS_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(HostTestUtils.getVirtualNicIdfEntityMetricObj(HostTestUtils.HOST_UUID));
    Pair<Integer, List<VirtualNic>> response = clusterService.getVirtualNics(HostTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID, 2, 1,null, null, null);
    assertEquals(response.right().get(0).getExtId(), HostTestUtils.VIRTUAL_NICS_UUID);
  }

  /* UTs for GET Virtual Nic Entity associated to host on cluster */

  @Test
  public void getVirtualNicEntityTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getVirtualNicIdfEntityObj(HostTestUtils.HOST_UUID, HostTestUtils.VIRTUAL_NICS_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(HostTestUtils.getVirtualNicIdfEntityMetricObj(HostTestUtils.HOST_UUID));
    VirtualNic response = clusterService.getVirtualNic(HostTestUtils.CLUSTER_UUID,
      HostTestUtils.HOST_UUID, HostTestUtils.VIRTUAL_NICS_UUID);
    assertEquals(response.getExtId(), HostTestUtils.VIRTUAL_NICS_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getVirtualNicEntityTestInCaseOfException1() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    clusterService.getVirtualNic(HostTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID, HostTestUtils.VIRTUAL_NICS_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getVirtualNicEntityTestInCaseOfException2() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getEntityWhereHostDoesNotBelongToCluster(HostTestUtils.HOST_UUID));
    clusterService.getVirtualNic(HostTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID, HostTestUtils.VIRTUAL_NICS_UUID);
  }

  //empty metric object
  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getVirtualNicEntityTestInCaseOfException3() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getVirtualNicIdfEntityObj(HostTestUtils.HOST_UUID, HostTestUtils.VIRTUAL_NICS_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(HostTestUtils.getEmptyMetricRetEntity());
    clusterService.getVirtualNic(HostTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID, HostTestUtils.VIRTUAL_NICS_UUID);
  }

  // Test to get an invalid VIRTUAL NIC ENTITY
  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getVirtualNicEntityTestInCaseOfException() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getVirtualNicIdfEntityObj(HostTestUtils.HOST_UUID, HostTestUtils.VIRTUAL_NICS_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(HostTestUtils.getVirtualNicIdfEntityMetricObj("5caf4b40-dfd4-4eaf-ac8d-5ff91a230c7a"));
    clusterService.getVirtualNic(HostTestUtils.CLUSTER_UUID, HostTestUtils.HOST_UUID, HostTestUtils.VIRTUAL_NICS_UUID);
  }

  /* UTs for category association to Host Nic Entity */

  // Test to attach category to host nic
  @Test
  public void updateCategoryAssociationsForHostNicEntityTest1() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getHostNicIdfEntityObj(HostTestUtils.HOST_UUID, HostTestUtils.HOST_NICS_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(HostTestUtils.getHostNicIdfEntityMetricObj(HostTestUtils.HOST_UUID))
      .thenReturn(HostTestUtils.getNetworkSwitchIntfIdfEntityMetricObj());
    when(genesisProxy.doExecute(eq(GenesisProxyImpl.GenesisRpcName.UPDATE_CATEGORY_ASSOCIATIONS), any(), any()))
      .thenReturn(ClusterTestUtils.getUpdateCategoryAssociationsRet());
    String categoryUuid = TestConstantUtils.randomUUIDString();
    String hostUuid = HostTestUtils.HOST_UUID;
    String hostNicUuid = HostTestUtils.HOST_NICS_UUID;
    CategoryEntityReferences params = ClusterTestUtils.getUpdateCategoriesParams(categoryUuid);
    clusterService.updateCategoryAssociationsForHostNicEntity(params, hostUuid, ClusterTestUtils.CLUSTER_UUID, hostNicUuid, "attach");
  }

  // Test to detach category to host nic
  @Test
  public void updateCategoryAssociationsForHostNicEntityTest2() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getHostNicIdfEntityObj(HostTestUtils.HOST_UUID, HostTestUtils.HOST_NICS_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(HostTestUtils.getHostNicIdfEntityMetricObj(HostTestUtils.HOST_UUID))
      .thenReturn(HostTestUtils.getNetworkSwitchIntfIdfEntityMetricObj());
    when(genesisProxy.doExecute(eq(GenesisProxyImpl.GenesisRpcName.UPDATE_CATEGORY_ASSOCIATIONS), any(), any()))
      .thenReturn(ClusterTestUtils.getUpdateCategoryAssociationsRet());
    String categoryUuid = TestConstantUtils.randomUUIDString();
    String hostUuid = HostTestUtils.HOST_UUID;
    String hostNicUuid = HostTestUtils.HOST_NICS_UUID;
    CategoryEntityReferences params = ClusterTestUtils.getUpdateCategoriesParams(categoryUuid);
    clusterService.updateCategoryAssociationsForHostNicEntity(params, hostUuid, ClusterTestUtils.CLUSTER_UUID, hostNicUuid, "detach");
  }

  // Test when host does not belong to the cluster
  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void updateCategoryAssociationsForHostNicEntityTestInCaseOfException1() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getEntityWhereHostDoesNotBelongToCluster(HostTestUtils.HOST_UUID));
    String categoryUuid = TestConstantUtils.randomUUIDString();
    String hostUuid = TestConstantUtils.randomUUIDString();
    String hostNicUuid = TestConstantUtils.randomUUIDString();
    CategoryEntityReferences params = ClusterTestUtils.getUpdateCategoriesParams(categoryUuid);
    clusterService.updateCategoryAssociationsForHostNicEntity(params, hostUuid, ClusterTestUtils.CLUSTER_UUID, hostNicUuid, "attach");
  }

  // Test when NIC does not belong to the host
  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void updateCategoryAssociationsForHostNicEntityTestInCaseOfException2() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    String categoryUuid = TestConstantUtils.randomUUIDString();
    String hostUuid = HostTestUtils.HOST_UUID;
    String hostNicUuid = HostTestUtils.HOST_NICS_UUID;
    CategoryEntityReferences params = ClusterTestUtils.getUpdateCategoriesParams(categoryUuid);
    clusterService.updateCategoryAssociationsForHostNicEntity(params, hostUuid, ClusterTestUtils.CLUSTER_UUID, hostNicUuid, "attach");
  }
}
