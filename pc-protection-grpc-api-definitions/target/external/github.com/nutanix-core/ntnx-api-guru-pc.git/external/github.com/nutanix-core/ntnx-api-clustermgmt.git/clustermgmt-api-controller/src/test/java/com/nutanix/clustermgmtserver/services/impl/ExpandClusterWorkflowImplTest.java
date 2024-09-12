package com.nutanix.clustermgmtserver.services.impl;

import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceException;
import com.nutanix.clustermgmtserver.proxy.CMSProxyImpl;
import com.nutanix.clustermgmtserver.proxy.EntityDbProxyImpl;
import com.nutanix.clustermgmtserver.proxy.ErgonProxyImpl;
import com.nutanix.clustermgmtserver.utils.ClusterTestUtils;
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
POST Discover Unconfigured Nodes
POST Check Hypervisor Requirements
POST Validate Node
POST Add Node (Expand Cluster)
POST Remove Node
 */

public class ExpandClusterWorkflowImplTest extends ClusterServiceImplTest {

  public ExpandClusterWorkflowImplTest(){
    super();
  }

  /* UTs for POST Discover Unconfigured Nodes */

  @Test
  public void discoverNodesTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    NodeDiscoveryParams discoverUnconfiguredNode = ClusterTestUtils.getDiscoverNodes();
    TaskReference task = clusterService.discoverUnconfiguredNodes(ClusterTestUtils.CLUSTER_UUID, discoverUnconfiguredNode);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void discoverNodesNatsFailTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(false);
    NodeDiscoveryParams discoverUnconfiguredNode = ClusterTestUtils.getDiscoverNodes();
    TaskReference task = clusterService.discoverUnconfiguredNodes(ClusterTestUtils.CLUSTER_UUID, discoverUnconfiguredNode);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void discoverNodesTestWithNoParam() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    NodeDiscoveryParams discoverUnconfiguredNode = ClusterTestUtils.getDiscoverNodesWithNoParam();
    TaskReference task = clusterService.discoverUnconfiguredNodes(ClusterTestUtils.CLUSTER_UUID, discoverUnconfiguredNode);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void discoverNodesTestOnPC() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    when(cmsProxy.doExecute(eq(CMSProxyImpl.CMSRpcName.DISCOVER_HOSTS), any(), any()))
      .thenReturn(ClusterTestUtils.getDiscoverHostsRet());
    NodeDiscoveryParams discoverUnconfiguredNode = ClusterTestUtils.getDiscoverNodes();
    TaskReference task = clusterService.discoverUnconfiguredNodes(ClusterTestUtils.CLUSTER_UUID, discoverUnconfiguredNode);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_COLON_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void discoverNodesTestOnPCInCaseOfException() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    when(cmsProxy.doExecute(eq(CMSProxyImpl.CMSRpcName.DISCOVER_HOSTS), any(), any()))
      .thenReturn(ClusterTestUtils.getDiscoverHostsRet());
    NodeDiscoveryParams discoverUnconfiguredNode = ClusterTestUtils.getDiscoverNodesWithNoParam();
    clusterService.discoverUnconfiguredNodes(ClusterTestUtils.CLUSTER_UUID, discoverUnconfiguredNode);
  }

  /* UTs for Fetch Node Networking Details */

  @Test
  public void getNodeNetworkingDetailsTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    NodeDetails discoverUnconfiguredNode = ClusterTestUtils.getNodeNetworkingDetails();
    TaskReference task = clusterService.getNodeNetworkingDetails(ClusterTestUtils.CLUSTER_UUID, discoverUnconfiguredNode);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getNodeNetworkingDetailsNatsFailTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(false);
    NodeDetails discoverUnconfiguredNode = ClusterTestUtils.getNodeNetworkingDetails();
    TaskReference task = clusterService.getNodeNetworkingDetails(ClusterTestUtils.CLUSTER_UUID, discoverUnconfiguredNode);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void getNodeNetworkingDetailsTestWithNoParam() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    NodeDetails discoverUnconfiguredNode = ClusterTestUtils.getNodeNetworkingDetailsWithNoParam();
    TaskReference task = clusterService.getNodeNetworkingDetails(ClusterTestUtils.CLUSTER_UUID, discoverUnconfiguredNode);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  /* UTs for POST Check Hypervisor Requirements */

  @Test
  public void isHypervisorRequiredTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    HypervisorUploadParam hypervisorUploadParam = ClusterTestUtils.getIsHypervisorReq();
    TaskReference task = clusterService.isHypervisorUploadRequired(ClusterTestUtils.CLUSTER_UUID, hypervisorUploadParam);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void isHypervisorRequiredNatsFailTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(false);
    HypervisorUploadParam hypervisorUploadParam = ClusterTestUtils.getIsHypervisorReq();
    TaskReference task = clusterService.isHypervisorUploadRequired(ClusterTestUtils.CLUSTER_UUID, hypervisorUploadParam);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void isHypervisorRequiredWithNoParamTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    HypervisorUploadParam hypervisorUploadParam = ClusterTestUtils.getIsHypervisorReqWithNoParam();
    TaskReference task = clusterService.isHypervisorUploadRequired(ClusterTestUtils.CLUSTER_UUID, hypervisorUploadParam);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  /* UTs for POST Validate Node */

  @Test
  public void validateNodeTestWithBundleParam() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    ValidateNodeParam validateNodeParam = new ValidateNodeParam();
    validateNodeParam.setSpecInWrapper(ClusterTestUtils.getValidateBundleParam());
    TaskReference task = clusterService.validateNode(ClusterTestUtils.CLUSTER_UUID, validateNodeParam);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void validateNodeTestWithLessBundleParam() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    ValidateNodeParam validateNodeParam = new ValidateNodeParam();
    validateNodeParam.setSpecInWrapper(ClusterTestUtils.getValidateBundleParamWithLessAttributes());
    TaskReference task = clusterService.validateNode(ClusterTestUtils.CLUSTER_UUID, validateNodeParam);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void validateNodeTestWithUplinkNode() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    ValidateNodeParam validateNodeParam = new ValidateNodeParam();
    List<UplinkNode> validateUplinkNodeItems = ClusterTestUtils.getValidateUplinkParam();
    validateNodeParam.setSpecInWrapper(validateUplinkNodeItems);
    TaskReference task = clusterService.validateNode(ClusterTestUtils.CLUSTER_UUID, validateNodeParam);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void validateNodeTestWithLessUplinkNodeParam() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    ValidateNodeParam validateNodeParam = new ValidateNodeParam();
    List<UplinkNode> validateUplinkNodeItems = ClusterTestUtils.getValidateUplinkParamWithNoParam();
    validateNodeParam.setSpecInWrapper(validateUplinkNodeItems);
    TaskReference task = clusterService.validateNode(ClusterTestUtils.CLUSTER_UUID, validateNodeParam);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void validateNodeNatsFailTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(false);
    ValidateNodeParam validateNodeParam = new ValidateNodeParam();
    validateNodeParam.setSpecInWrapper(ClusterTestUtils.getValidateBundleParam());
    TaskReference task = clusterService.validateNode(ClusterTestUtils.CLUSTER_UUID, validateNodeParam);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  /* UTs for POST Add Node (Expand Cluster) */

  @Test
  public void addNodeTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    ExpandClusterParams addNode = ClusterTestUtils.getAddNode();
    clusterService.addNode(ClusterTestUtils.CLUSTER_UUID, addNode, ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void addNodeTestNatsFail() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(false);
    ExpandClusterParams addNode = ClusterTestUtils.getAddNode();
    clusterService.addNode(ClusterTestUtils.CLUSTER_UUID, addNode, ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void addNodeTestWithNoParam() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    ExpandClusterParams addNode = ClusterTestUtils.getAddNodeWithNoParam();
    clusterService.addNode(ClusterTestUtils.CLUSTER_UUID, addNode, ClusterTestUtils.TASK_UUID);
  }

  /* UTs for POST Remove Node */

  @Test
  public void removeNodeTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    NodeRemovalParams removeNode = ClusterTestUtils.getRemoveNode();
    clusterService.removeNode(ClusterTestUtils.CLUSTER_UUID, removeNode, ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void removeNodeNatsFailTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(false);
    NodeRemovalParams removeNode = ClusterTestUtils.getRemoveNode();
    clusterService.removeNode(ClusterTestUtils.CLUSTER_UUID, removeNode, ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void removeNodeTestWithLessParam() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    NodeRemovalParams removeNode = ClusterTestUtils.getRemoveNodeWithLessParam();
    clusterService.removeNode(ClusterTestUtils.CLUSTER_UUID, removeNode, ClusterTestUtils.TASK_UUID);
  }

}
