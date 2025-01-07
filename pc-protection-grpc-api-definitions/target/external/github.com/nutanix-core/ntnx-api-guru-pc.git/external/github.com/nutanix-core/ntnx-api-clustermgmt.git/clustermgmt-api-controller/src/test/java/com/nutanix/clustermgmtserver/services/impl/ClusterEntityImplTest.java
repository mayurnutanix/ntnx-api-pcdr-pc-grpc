package com.nutanix.clustermgmtserver.services.impl;

import com.nutanix.api.utils.task.ErgonTaskUtils;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceException;
import com.nutanix.clustermgmtserver.proxy.CMSProxyImpl;
import com.nutanix.clustermgmtserver.proxy.EntityDbProxyImpl;
import com.nutanix.clustermgmtserver.proxy.ErgonProxyImpl;
import com.nutanix.clustermgmtserver.proxy.GenesisProxyImpl;
import com.nutanix.clustermgmtserver.utils.ClusterTestUtils;
import com.nutanix.clustermgmtserver.utils.TestConstantUtils;
import com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto;
import com.nutanix.prism.base.zk.ProtobufZNodeManagementException;
import com.nutanix.stats_gateway.graphql_interface.GroupsGraphqlInterfaceProto;
import com.nutanix.util.base.Pair;
import dp1.clu.clustermgmt.v4.config.Cluster;
import dp1.clu.clustermgmt.v4.config.ClusterProjection;
import dp1.clu.clustermgmt.v4.config.ConfigCredentials;
import dp1.clu.clustermgmt.v4.config.DomainType;
import dp1.clu.clustermgmt.v4.config.CategoryEntityReferences;
import dp1.clu.prism.v4.config.TaskReference;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/*
This file contains UTs for following API endpoints:
GET Custer
GET Clusters (list)
PUT Update Cluster
POST Create Cluster
DELETE Destroy Cluster
POST Associate/Dissociate Category to Cluster
GET Domain Fault Tolerance Status
 */

public class ClusterEntityImplTest extends ClusterServiceImplTest {

  public ClusterEntityImplTest(){
    super();
  }

  /* UTs for GET Cluster entity */

  @Test
  public void getClusterEntityTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(ClusterTestUtils.buildGetEntitiesWithMetricsRetForCluster())
      .thenReturn(ClusterTestUtils.buildGetEntitiesWithMetricsRetForCategories(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.buildGetEntitiesWithMetricsRetForManagementServer(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getHttpProxyIdfEntityObj(ClusterTestUtils.HTTP_PROXY_UUID))
      .thenReturn(ClusterTestUtils.getHttpProxyWhitelistIdfEntityObj(ClusterTestUtils.HTTP_PROXY_UUID));
    when(multiClusterZeusConfig.getZeusConfiguration(any())).thenReturn(ClusterTestUtils.buildZeusConfiguration());
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
            .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID));
    Cluster cluster = clusterService.getClusterEntity(ClusterTestUtils.CLUSTER_UUID, null);
    assertEquals(cluster.getName(), ClusterTestUtils.CLUSTER_NAME);
    assertEquals(cluster.getConfig().getBuildInfo().getVersion(), ClusterTestUtils.CLUSTER_VERSION);
    assertEquals(cluster.getCategories().size(), 1);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getClusterEntityTestWithException() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(ClusterTestUtils.getClusterEntityWithMetricRetWithNoEntity());
    clusterService.getClusterEntity(ClusterTestUtils.CLUSTER_UUID, null);
  }

  @Test
  public void getClusterEntityTestOnPc() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    when(applianceConfiguration.getZeusConfig(true)).thenReturn(ClusterTestUtils.buildZeusConfiguration());
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(ClusterTestUtils.buildGetEntitiesWithMetricsRetForManagementServer(ClusterTestUtils.CLUSTER_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(ClusterTestUtils.buildGetEntitiesWithMetricsRetForClusterForPC())
      .thenReturn(ClusterTestUtils.IdfEntityObjWithMetricRetWithNoEntity())
      .thenReturn(ClusterTestUtils.IdfEntityObjWithMetricRetWithNoEntity());
    assertEquals(clusterService.getClusterEntity(ClusterTestUtils.CLUSTER_UUID,null).getName(), ClusterTestUtils.CLUSTER_NAME);
  }

  @Test
  public void getClusterEntityTestWithOtherConditions() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID));
    when(multiClusterZeusConfig.getZeusConfiguration(any())).thenReturn(ClusterTestUtils.buildZeusConfigurationWithOtherConditions());
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(ClusterTestUtils.buildGetEntitiesWithMetricsRetForManagementServer(ClusterTestUtils.CLUSTER_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(ClusterTestUtils.buildGetEntitiesWithMetricsRetForCluster())
      .thenReturn(ClusterTestUtils.IdfEntityObjWithMetricRetWithNoEntity())
      .thenReturn(ClusterTestUtils.IdfEntityObjWithMetricRetWithNoEntity());
    Cluster clusterEntity = clusterService.getClusterEntity(ClusterTestUtils.CLUSTER_UUID,null);
    assertEquals(clusterEntity.getName(), ClusterTestUtils.CLUSTER_NAME);
    assertEquals((long)clusterEntity.getBackupEligibilityScore(), 2);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getClusterEntityTestInCaseOfZeusException() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID));
    when(multiClusterZeusConfig.getZeusConfiguration(any())).thenThrow(new ProtobufZNodeManagementException.BadVersionException());
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(ClusterTestUtils.buildGetEntitiesWithMetricsRetForCluster())
      .thenReturn(ClusterTestUtils.IdfEntityObjWithMetricRetWithNoEntity())
      .thenReturn(ClusterTestUtils.IdfEntityObjWithMetricRetWithNoEntity());
    assertEquals(clusterService.getClusterEntity(ClusterTestUtils.CLUSTER_UUID, null).getName(), ClusterTestUtils.CLUSTER_NAME);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getClusterEntityTestInCaseOfZeusException2() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID));
    when(multiClusterZeusConfig.getZeusConfiguration(any())).thenThrow(new ProtobufZNodeManagementException.BadVersionException());
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(ClusterTestUtils.buildGetEntitiesWithMetricsRetForCluster())
      .thenReturn(ClusterTestUtils.IdfEntityObjWithMetricRetWithNoEntity())
      .thenReturn(ClusterTestUtils.IdfEntityObjWithMetricRetWithNoEntity());
    assertEquals(clusterService.getClusterEntity(ClusterTestUtils.CLUSTER_UUID, null).getName(), ClusterTestUtils.CLUSTER_NAME);
  }

  /* UTs for GET Clusters list */

  @Test
  public void getClusterEntitiesTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(ClusterTestUtils.buildGetEntitiesWithMetricsRetForCluster())
      .thenReturn(ClusterTestUtils.IdfEntityObjWithMetricRetWithNoEntity())
      .thenReturn(ClusterTestUtils.IdfEntityObjWithMetricRetWithNoEntity());
    when(multiClusterZeusConfig.getZeusConfiguration(any())).thenReturn(ClusterTestUtils.buildZeusConfiguration());
    Pair<Integer, List<Cluster>> response = clusterService.getClusterEntities(0,2, "contains(name, 'test-cluster-name')", "name", null, "name", null);
    assertEquals(response.right().get(0).getName(), ClusterTestUtils.CLUSTER_NAME);
  }

  @Test
  public void getClusterEntitiesTestWithFilterOnUpgradeStatus() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(ClusterTestUtils.buildGetEntitiesWithMetricsRetForCluster())
      .thenReturn(ClusterTestUtils.IdfEntityObjWithMetricRetWithNoEntity())
      .thenReturn(ClusterTestUtils.IdfEntityObjWithMetricRetWithNoEntity());
    when(multiClusterZeusConfig.getZeusConfiguration(any())).thenReturn(ClusterTestUtils.buildZeusConfiguration());
    Pair<Integer, List<Cluster>> response = clusterService.getClusterEntities(0,2, "upgradeStatus eq Clustermgmt.Config.UpgradeStatus'SUCCEEDED'", "name", null, "name", null);
    assertEquals(response.right().get(0).getName(), ClusterTestUtils.CLUSTER_NAME);
  }

  @Test
  public void getClusterEntitiesTestWithFilterOnHypervisorTypes() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(ClusterTestUtils.buildGetEntitiesWithMetricsRetForCluster())
      .thenReturn(ClusterTestUtils.IdfEntityObjWithMetricRetWithNoEntity())
      .thenReturn(ClusterTestUtils.IdfEntityObjWithMetricRetWithNoEntity());
    when(multiClusterZeusConfig.getZeusConfiguration(any())).thenReturn(ClusterTestUtils.buildZeusConfiguration());
    Pair<Integer, List<Cluster>> response = clusterService.getClusterEntities(0,2, "config/hypervisorTypes/any(c:c eq Clustermgmt.Config.HypervisorType'AHV')", "name", null, "name", null);
    assertEquals(response.right().get(0).getName(), ClusterTestUtils.CLUSTER_NAME);
  }

  @Test
  public void getClusterEntitiesTestWithOrderByKeyManagementServerType() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(ClusterTestUtils.buildGetEntitiesWithMetricsRetForCluster())
      .thenReturn(ClusterTestUtils.IdfEntityObjWithMetricRetWithNoEntity())
      .thenReturn(ClusterTestUtils.IdfEntityObjWithMetricRetWithNoEntity());
    when(multiClusterZeusConfig.getZeusConfiguration(any())).thenReturn(ClusterTestUtils.buildZeusConfiguration());
    Pair<Integer, List<Cluster>> response = clusterService.getClusterEntities(0,2, null, "network/keyManagementServerType", null, null, null);
    assertEquals(response.right().get(0).getName(), ClusterTestUtils.CLUSTER_NAME);
  }

  @Test
  public void getClusterEntitiesTestWithMoreFilteringConditions() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(ClusterTestUtils.buildGetEntitiesWithMetricsRetForCluster())
      .thenReturn(ClusterTestUtils.IdfEntityObjWithMetricRetWithNoEntity())
      .thenReturn(ClusterTestUtils.IdfEntityObjWithMetricRetWithNoEntity());
    when(multiClusterZeusConfig.getZeusConfiguration(any())).thenReturn(ClusterTestUtils.buildZeusConfiguration());
    Pair<Integer, List<Cluster>> response = clusterService.getClusterEntities(0,2, "startswith(toupper(name), 'test')", null, null, null, null);
    assertEquals(response.right().size(), 1);
  }

  @Test
  public void getClusterEntitiesTestForPC() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(ClusterTestUtils.buildGetEntitiesWithMetricsRetForClusterForPC())
      .thenReturn(ClusterTestUtils.IdfEntityObjWithMetricRetWithNoEntity())
      .thenReturn(ClusterTestUtils.IdfEntityObjWithMetricRetWithNoEntity());
    when(multiClusterZeusConfig.getZeusConfiguration(any())).thenReturn(ClusterTestUtils.buildZeusConfiguration());
    Pair<Integer, List<Cluster>> response = clusterService.getClusterEntities(0,2, "contains(name, 'test-cluster-name')", "name", null, "name", null);
    assertEquals(response.right().get(0).getName(), ClusterTestUtils.CLUSTER_NAME);
  }

  @Test
  public void getClusterEntitiesTestWithoutSortingandFiltering() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(ClusterTestUtils.buildGetEntitiesWithMetricsRetForCluster())
      .thenReturn(ClusterTestUtils.buildGetEntitiesWithMetricsRetForManagementServer(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.IdfEntityObjWithMetricRetWithNoEntity())
      .thenReturn(ClusterTestUtils.IdfEntityObjWithMetricRetWithNoEntity());
    when(multiClusterZeusConfig.getZeusConfiguration(any())).thenReturn(ClusterTestUtils.buildZeusConfiguration());
    Pair<Integer, List<Cluster>> response = clusterService.getClusterEntities(null,null, null, null, null, null, null);
    assertEquals(response.right().get(0).getName(), ClusterTestUtils.CLUSTER_NAME);
  }

  @Test
  public void getClusterEntitiesTestWithExpand() throws Exception {
    final String data = "{\"cluster\":[{\"cluster_name\":[\"test-name-6\"],\"num_nodes\":[1]," +
                        "\"incarnationId\":null,\"buildType\":null,\"version\":[\"master\"],\"fullVersion\":null," +
                        "\"commitId\":null,\"shortCommitId\":null,\"hypervisor_types\":[\"kKvm\"]," +
                        "\"service_list\":[\"AOS\", \"ONE_NODE\"],\"timezone\":[\"UTC\"],\"redundancy_factor\":[2]," +
                        "\"clusterArch\":null,\"remoteSupport\":null,\"operationMode\":null,\"isLts\":null,\"clusterProfileExtId\":null," +
                        "\"tenantId\":null,\"_cluster_uuid_\":[\"5cafbb40-dfd4-4eaf-ac8d-5ff91a230c7a\"],\"cluster_profile\":{\"name\":[\"test_profile_1\"]," +
                        "\"description\":null,\"createTime\":null,\"last_update_time.seconds\":[1709559776],\"attached_clusters_count\":[1],\"drifted_clusters_count\":[1]," +
                        "\"clusterprofile_tenantId\":null,\"extId\":null}}],\"filtered_entity_count\":1,\"total_entity_count\":9}";
    GroupsGraphqlInterfaceProto.GroupsGraphqlRet groupsGraphqlRet =
      GroupsGraphqlInterfaceProto.GroupsGraphqlRet.newBuilder().setData(data)
      .build();
    when(statsGatewayService.executeQuery(anyString()))
      .thenReturn(groupsGraphqlRet);
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(ClusterTestUtils.buildGetEntitiesWithMetricsRetForCluster())
      .thenReturn(ClusterTestUtils.buildGetEntitiesWithMetricsRetForManagementServer(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.IdfEntityObjWithMetricRetWithNoEntity())
      .thenReturn(ClusterTestUtils.IdfEntityObjWithMetricRetWithNoEntity());
    when(multiClusterZeusConfig.getZeusConfiguration(any())).thenReturn(ClusterTestUtils.buildZeusConfiguration());
    Pair<Integer, List<Cluster>> response = clusterService.getClusterEntities(null,null, null, null, null, null, "clusterProfile");
    assertEquals(response.right().get(0).getName(), ClusterTestUtils.CLUSTER_NAME);
  }

  @Test
  public void getClusterEntitiesTestWithExpandForPC() throws Exception {
    final String data = "{\"cluster\":[{\"cluster_name\":[\"test-name-6\"],\"num_nodes\":[1]," +
      "\"incarnationId\":null,\"buildType\":null,\"version\":[\"master\"],\"fullVersion\":null," +
      "\"commitId\":null,\"shortCommitId\":null,\"hypervisor_types\":[\"kKvm\"]," +
      "\"service_list\":[\"AOS\", \"ONE_NODE\"],\"timezone\":[\"UTC\"],\"redundancy_factor\":[2]," +
      "\"clusterArch\":null,\"remoteSupport\":null,\"operationMode\":null,\"isLts\":null,\"clusterProfileExtId\":null," +
      "\"tenantId\":null,\"_cluster_uuid_\":[\"5cafbb40-dfd4-4eaf-ac8d-5ff91a230c7a\"],\"cluster_profile\":{\"name\":null," +
      "\"description\":null,\"createTime\":null,\"last_update_time.seconds\":null,\"attached_clusters_count\":null,\"drifted_clusters_count\":null," +
      "\"clusterprofile_tenantId\":null,\"extId\":null}}],\"filtered_entity_count\":1,\"total_entity_count\":9}";
    GroupsGraphqlInterfaceProto.GroupsGraphqlRet groupsGraphqlRet =
      GroupsGraphqlInterfaceProto.GroupsGraphqlRet.newBuilder().setData(data)
        .build();
    when(statsGatewayService.executeQuery(anyString()))
      .thenReturn(groupsGraphqlRet);
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(ClusterTestUtils.buildGetEntitiesWithMetricsRetForClusterForPC())
      .thenReturn(ClusterTestUtils.buildGetEntitiesWithMetricsRetForManagementServer(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.IdfEntityObjWithMetricRetWithNoEntity())
      .thenReturn(ClusterTestUtils.IdfEntityObjWithMetricRetWithNoEntity());
    when(multiClusterZeusConfig.getZeusConfiguration(any())).thenReturn(ClusterTestUtils.buildZeusConfiguration());
    Pair<Integer, List<Cluster>> response = clusterService.getClusterEntities(null,null, null, null, null, null, "clusterProfile");
    assertEquals(response.right().get(0).getName(), ClusterTestUtils.CLUSTER_NAME);
  }

  @Test
  public void getClusterEntityTestWithStorageSummaryExpand() throws Exception {
    final String data = "{\"cluster\":[{\"cluster_name\":[\"test-name-6\"],\"num_nodes\":[1]," +
            "\"incarnationId\":null,\"buildType\":null,\"version\":[\"master\"],\"fullVersion\":null," +
            "\"commitId\":null,\"shortCommitId\":null,\"hypervisor_types\":[\"kKvm\"]," +
            "\"service_list\":[\"AOS\", \"ONE_NODE\"],\"timezone\":[\"UTC\"],\"redundancy_factor\":[2]," +
            "\"clusterArch\":null,\"remoteSupport\":null,\"operationMode\":null,\"isLts\":null,\"clusterProfileExtId\":null," +
            "\"tenantId\":null,\"_cluster_uuid_\":[\"5cafbb40-dfd4-4eaf-ac8d-5ff91a230c7a\"]," +
            "\"storage_summary\":{\"cluster_fault_tolerant_capacity\":[\"5234725238178\"]," +
            "\"storagesummary_tenantId\":null,\"extId\":null}}]," +
            "\"filtered_entity_count\":1,\"total_entity_count\":9}";

    GroupsGraphqlInterfaceProto.GroupsGraphqlRet groupsGraphqlRet =
            GroupsGraphqlInterfaceProto.GroupsGraphqlRet.newBuilder().setData(data)
                    .build();
    when(statsGatewayService.executeQuery(anyString()))
            .thenReturn(groupsGraphqlRet);
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
            .thenReturn(ClusterTestUtils.buildGetEntitiesWithMetricsRetForCluster())
            .thenReturn(ClusterTestUtils.buildGetEntitiesWithMetricsRetForCategories(ClusterTestUtils.CLUSTER_UUID))
            .thenReturn(ClusterTestUtils.buildGetEntitiesWithMetricsRetForManagementServer(ClusterTestUtils.CLUSTER_UUID));
    when(multiClusterZeusConfig.getZeusConfiguration(any())).thenReturn(ClusterTestUtils.buildZeusConfiguration());
    ClusterProjection cluster = (ClusterProjection) clusterService.getClusterEntity(ClusterTestUtils.CLUSTER_UUID, "storageSummary");
    assertEquals(cluster.getStorageSummaryProjection().getClusterFaultTolerantCapacityInBytes(), new Long("5234725238178"));
  }

  /* UTs for PUT Update Cluster */

  @Test
  public void updateClusterTestOnPC() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    when(genesisProxy.doExecute(eq(GenesisProxyImpl.GenesisRpcName.UPDATE_CLUSTER), any(), any()))
      .thenReturn(ClusterTestUtils.getUpdateClusterRet());
    Cluster clusterEntity = ClusterTestUtils.getUpdateClusterEntityObj();
    TaskReference task = clusterService.updateCluster(clusterEntity, ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
    assertEquals(ErgonTaskUtils.getTaskUuid(task.getExtId()), ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void updateClusterTestOnPCFailureCase() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    when(genesisProxy.doExecute(eq(GenesisProxyImpl.GenesisRpcName.UPDATE_CLUSTER), any(), any()))
      .thenThrow(new ClustermgmtServiceException("Unknown error in genesis"));
    Cluster clusterEntity = ClusterTestUtils.getUpdateClusterEntityObj();
    TaskReference task = clusterService.updateCluster(clusterEntity,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void updateClusterTestOnPE() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    Cluster clusterEntity = ClusterTestUtils.getUpdateClusterEntityObj();
    TaskReference task = clusterService.updateCluster(clusterEntity,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void updateClusterTestOnPENatsFailed() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(false);
    Cluster clusterEntity = ClusterTestUtils.getUpdateClusterEntityObj();
    TaskReference task = clusterService.updateCluster(clusterEntity,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
  }

  /* UTs for POST Create Cluster */

  @Test
  public void clusterCreateTest() throws Exception {
    when(cmsProxy.doExecute(eq(CMSProxyImpl.CMSRpcName.CREATE_CLUSTER), any(), any()))
      .thenReturn(ClusterTestUtils.getClusterCreateRet());
    Cluster clusterCreateParams = ClusterTestUtils.getClusterCreateParams(false, false, false, false, false, false);
    clusterService.clusterCreate(clusterCreateParams, ClusterTestUtils.TASK_UUID,true);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void clusterCreateTestInCaseOfException() throws Exception {
    when(cmsProxy.doExecute(eq(CMSProxyImpl.CMSRpcName.CREATE_CLUSTER), any(), any()))
      .thenThrow(new ClustermgmtServiceException("Unknown error in cms service"));
    Cluster clusterCreateParams = ClusterTestUtils.getClusterCreateParams(false, false, false, false, false, false);
    clusterService.clusterCreate(clusterCreateParams, ClusterTestUtils.TASK_UUID,true);
  }

  //how to test for validation exceptions thrown i.e. assert thrown exception for a void function for create cluster
  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void clusterCreateTestInCaseOfValidationExceptions1() throws Exception {
    when(cmsProxy.doExecute(eq(CMSProxyImpl.CMSRpcName.CREATE_CLUSTER), any(), any()))
      .thenReturn(ClusterTestUtils.getClusterCreateRet());
    Cluster clusterCreateParams1 = ClusterTestUtils.getClusterCreateParams(true, false, false, false, false, false);
    clusterService.clusterCreate(clusterCreateParams1, ClusterTestUtils.TASK_UUID,true);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void clusterCreateTestInCaseOfValidationExceptions2() throws Exception {
    when(cmsProxy.doExecute(eq(CMSProxyImpl.CMSRpcName.CREATE_CLUSTER), any(), any()))
      .thenReturn(ClusterTestUtils.getClusterCreateRet());
    Cluster clusterCreateParams2 = ClusterTestUtils.getClusterCreateParams(false, true, false, false, false, false);
    clusterService.clusterCreate(clusterCreateParams2, ClusterTestUtils.TASK_UUID,true);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void clusterCreateTestInCaseOfValidationExceptions3() throws Exception {
    when(cmsProxy.doExecute(eq(CMSProxyImpl.CMSRpcName.CREATE_CLUSTER), any(), any()))
      .thenReturn(ClusterTestUtils.getClusterCreateRet());
    Cluster clusterCreateParams3 = ClusterTestUtils.getClusterCreateParams(false, false, true, false, false, false);
    clusterService.clusterCreate(clusterCreateParams3, ClusterTestUtils.TASK_UUID,true);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void clusterCreateTestInCaseOfValidationExceptions4() throws Exception {
    when(cmsProxy.doExecute(eq(CMSProxyImpl.CMSRpcName.CREATE_CLUSTER), any(), any()))
      .thenReturn(ClusterTestUtils.getClusterCreateRet());
    Cluster clusterCreateParams4 = ClusterTestUtils.getClusterCreateParams(false, false, false, true, false, false);
    clusterService.clusterCreate(clusterCreateParams4, ClusterTestUtils.TASK_UUID,true);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void clusterCreateTestInCaseOfValidationExceptions5() throws Exception {
    when(cmsProxy.doExecute(eq(CMSProxyImpl.CMSRpcName.CREATE_CLUSTER), any(), any()))
      .thenReturn(ClusterTestUtils.getClusterCreateRet());
    Cluster clusterCreateParams5 = ClusterTestUtils.getClusterCreateParams(false, false, false, false,true, false);
    clusterService.clusterCreate(clusterCreateParams5, ClusterTestUtils.TASK_UUID,true);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void clusterCreateTestInCaseOfValidationExceptions6() throws Exception {
    when(cmsProxy.doExecute(eq(CMSProxyImpl.CMSRpcName.CREATE_CLUSTER), any(), any()))
      .thenReturn(ClusterTestUtils.getClusterCreateRet());
    Cluster clusterCreateParams6 = ClusterTestUtils.getClusterCreateParams(false, false, false, false,false, true);
    clusterService.clusterCreate(clusterCreateParams6, ClusterTestUtils.TASK_UUID,true);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void clusterCreateTestInCaseOfValidationExceptions7() throws Exception {
    when(cmsProxy.doExecute(eq(CMSProxyImpl.CMSRpcName.CREATE_CLUSTER), any(), any()))
      .thenReturn(ClusterTestUtils.getClusterCreateRet());
    Cluster clusterCreateParams6 = ClusterTestUtils.getClusterCreateParamsWithNameServerFqdn();
    clusterService.clusterCreate(clusterCreateParams6, ClusterTestUtils.TASK_UUID,true);
  }
  /* UTs for DELETE Destroy Cluster */

  @Test
  public void clusterDestroyTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(cmsProxy.doExecute(eq(CMSProxyImpl.CMSRpcName.DESTROY_CLUSTER), any(), any()))
      .thenReturn(ClusterTestUtils.getClusterDestroyRet());
    clusterService.clusterDestroy(ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID, true);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void clusterDestroyTestInCaseOfException() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(cmsProxy.doExecute(eq(CMSProxyImpl.CMSRpcName.DESTROY_CLUSTER), any(), any()))
      .thenThrow(new ClustermgmtServiceException("Unknown error in cms service"));
    clusterService.clusterDestroy(ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID, false);
  }

  /* UTs for POST Associate/Dissociate Category to Cluster */

  @Test
  public void updateCategoryAssociationsForClusterEntityTest()  throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID));
    when(genesisProxy.doExecute(eq(GenesisProxyImpl.GenesisRpcName.UPDATE_CATEGORY_ASSOCIATIONS), any(), any()))
      .thenReturn(ClusterTestUtils.getUpdateCategoryAssociationsRet());
    String categoryUuid = TestConstantUtils.randomUUIDString();
    CategoryEntityReferences params = ClusterTestUtils.getUpdateCategoriesParams(categoryUuid);
    TaskReference task = clusterService.updateCategoryAssociationsForClusterEntity(params ,ClusterTestUtils.CLUSTER_UUID,"attach");
    assertEquals(ErgonTaskUtils.getTaskUuid(task.getExtId()), ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void updateCategoryAssociationsForClusterEntityTestInCaseOfException1()  throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    String categoryUuid = TestConstantUtils.randomUUIDString();
    CategoryEntityReferences params = ClusterTestUtils.getUpdateCategoriesParams(categoryUuid);
    TaskReference task = clusterService.updateCategoryAssociationsForClusterEntity(params ,ClusterTestUtils.CLUSTER_UUID,"attach");
    assertEquals(ErgonTaskUtils.getTaskUuid(task.getExtId()), ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void updateCategoryAssociationsForClusterEntityTestInCaseOfException2()  throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID));
    CategoryEntityReferences params = ClusterTestUtils.getUpdateCategoriesParams(null);
    TaskReference task = clusterService.updateCategoryAssociationsForClusterEntity(params ,ClusterTestUtils.CLUSTER_UUID,"attach");
    assertEquals(ErgonTaskUtils.getTaskUuid(task.getExtId()), ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void updateCategoryAssociationsForClusterEntityTestInCaseOfException() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID));
    when(genesisProxy.doExecute(eq(GenesisProxyImpl.GenesisRpcName.UPDATE_CATEGORY_ASSOCIATIONS), any(), any()))
      .thenThrow(new ClustermgmtServiceException("Unknown error in genesis service"));
    String categoryUuid = TestConstantUtils.randomUUIDString();
    CategoryEntityReferences params = ClusterTestUtils.getUpdateCategoriesParams(categoryUuid);
    clusterService.updateCategoryAssociationsForClusterEntity(params ,ClusterTestUtils.CLUSTER_UUID,"detach");
  }

  /* UTs for GET Domain Fault Tolerance Status */

  @Test
  public void getDomainFaultToleranceTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID));
    when(multiClusterZeusConfig.getZeusConfiguration(any())).thenReturn(ClusterTestUtils.buildZeusConfigurationWithDomainFaultStatus());
    assertEquals(clusterService.getDomainFaultToleranceStatus(ClusterTestUtils.CLUSTER_UUID).get(0).getType(), DomainType.CLUSTER);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getDomainFaultToleranceTestInCaseOfException() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    clusterService.getDomainFaultToleranceStatus(ClusterTestUtils.CLUSTER_UUID);
  }

  /* ClusterService.getCategoriesInfoTest() */
  @Test
  public void setCategoriesTestForCluster() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(ClusterTestUtils.buildGetEntitiesWithMetricsRetForCluster());
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(ClusterTestUtils.buildGetEntitiesWithMetricsRetForCategories(ClusterTestUtils.CLUSTER_UUID));
    Cluster cluster = new Cluster();
    cluster.setExtId(ClusterTestUtils.CLUSTER_UUID);
    cluster.setName(ClusterTestUtils.CLUSTER_NAME);
    clusterService.setCategoriesInfo(ClusterTestUtils.CLUSTER_UUID, cluster);
    assertEquals(cluster.getCategories().size(), 1);
  }

  /* UTs for GET Config Credentials */
  @Test
  public void getConfigCredentialsTest() throws Exception {
    when(genesisProxy.doExecute(eq(GenesisProxyImpl.GenesisRpcName.GET_CONFIG_CREDENTIALS), any(), any()))
      .thenReturn(ClusterTestUtils.getConfigCredentialsRet());
    ConfigCredentials credentials = clusterService.getConfigCredentials(ClusterTestUtils.CLUSTER_UUID);
    assertEquals(credentials.getSnmp().size(), 1);
    assertEquals(credentials.getSmtp().getUsername(), ClusterTestUtils.SMTP_SERVER_USERNAME);
    assertEquals(credentials.getSmtp().getPassword(), ClusterTestUtils.SMTP_SERVER_PASSWORD);
    assertEquals(credentials.getHttpProxy().getUsername(), ClusterTestUtils.HTTP_PROXY_USERNAME);
    assertEquals(credentials.getHttpProxy().getPassword(), ClusterTestUtils.HTTP_PROXY_PASSWORD);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getConfigCredentialsTestInCaseOfException() throws Exception {
    when(genesisProxy.doExecute(eq(GenesisProxyImpl.GenesisRpcName.GET_CONFIG_CREDENTIALS), any(), any()))
      .thenReturn(
        GenesisInterfaceProto.GetConfigCredentialsRet.newBuilder()
          .setError(GenesisInterfaceProto.GenesisGenericError.newBuilder().setErrorType(
              GenesisInterfaceProto.GenesisGenericError.ErrorType.kUnknownError)
            .setErrorMsg("Unknown error in genesis")
            .build())
          .build()
      );
    clusterService.getConfigCredentials(ClusterTestUtils.CLUSTER_UUID);
  }

}
