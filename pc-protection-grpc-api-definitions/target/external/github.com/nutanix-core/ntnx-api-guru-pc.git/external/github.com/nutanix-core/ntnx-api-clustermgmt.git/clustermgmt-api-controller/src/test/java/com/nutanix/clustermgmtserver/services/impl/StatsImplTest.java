package com.nutanix.clustermgmtserver.services.impl;

import com.nutanix.api.utils.stats.query.StatsQueryParams;
import com.nutanix.api.utils.stats.query.StatsQueryResponse;
import com.nutanix.api.utils.stats.service.exception.StatsGatewayServiceException;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceException;
import com.nutanix.clustermgmtserver.proxy.EntityDbProxyImpl;
import com.nutanix.clustermgmtserver.utils.*;
import dp1.clu.clustermgmt.v4.stats.ClusterStats;
import dp1.clu.clustermgmt.v4.stats.HostStats;
import dp1.clu.common.v1.stats.DownSamplingOperator;
import mockit.MockUp;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/*
This file contains UTs for following API endpoints:
GET Cluster stats
GET Host stats
 */

public class StatsImplTest extends ClusterServiceImplTest {

  public StatsImplTest(){
    super();
  }

  /* UTs for GET Cluster stats */

  @Test
  public void getClusterStatsInfoTest() throws Exception {
    String uuid = TestConstantUtils.randomUUIDString();

    new MockUp<StatsUtils>() {
      @mockit.Mock
      private StatsQueryResponse fetchStats(StatsQueryParams statsQueryParams)
        throws StatsGatewayServiceException {
        StatsQueryResponse resp = new StatsQueryResponse();
        resp.setEntityName("cluster");
        Map<String, StatsQueryResponse.AttributeStats> entityStatsMap = new HashMap<>();
        Map<String, List<StatsQueryResponse.TimeValuePair>> attrStatsMap = StatsTestUtils.getAttributeStatsMap(true,false);
        StatsQueryResponse.AttributeStats attributeStats = new StatsQueryResponse.AttributeStats();
        attributeStats.setAttributeStatsMap(attrStatsMap);
        entityStatsMap.put(uuid, attributeStats);
        resp.setEntityStatsMap(entityStatsMap);
        return resp;
      }
    };

    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(StatsTestUtils.CLUSTER_UUID));
    ClusterStats resource = clusterService.getClusterStatsInfo(uuid, DownSamplingOperator.AVG, 50, 100L, 100L, null);
    assertEquals(resource.getExtId(), uuid);
    assertEquals(resource.getStatType(), DownSamplingOperator.AVG);
    Long value = new Long(281683);
    assertEquals(resource.getControllerNumWriteIopsUpperBuf().get(0).getValue(), value);
    assertEquals(resource.getHealthCheckScore().get(0).getValue(), Long.valueOf("74"));
    assertEquals(resource.getRecycleBinUsageBytes().get(0).getValue(), Long.valueOf("0"));
    assertEquals(resource.getSnapshotCapacityBytes().get(0).getValue(), Long.valueOf("6438191104"));
    assertEquals(resource.getOverallSavingsBytes().get(0).getValue(), Long.valueOf("511778816"));
    assertEquals(resource.getOverallSavingsRatio().get(0).getValue(), Long.valueOf("14694282"));
    //any other assertions if required
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getClusterStatsInfoTestInCaseOfException1() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObjWithNoEntity());
    clusterService.getClusterStatsInfo(StatsTestUtils.CLUSTER_UUID, DownSamplingOperator.AVG, 50, 100L, 100L, null);
  }

  @Test(expectedExceptions = StatsGatewayServiceException.class)
  public void getClusterStatsInfoTestInCaseOfException2() throws Exception {
    new MockUp<StatsUtils>() {
      @mockit.Mock
      private StatsQueryResponse fetchStats(StatsQueryParams statsQueryParams)
        throws StatsGatewayServiceException {
        throw new StatsGatewayServiceException("Test error", new Exception());
      }
    };

    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(StatsTestUtils.CLUSTER_UUID));
    clusterService.getClusterStatsInfo(StatsTestUtils.CLUSTER_UUID, DownSamplingOperator.LAST, 50, 100L, 100L, null);
  }

  /* UTs for GET Host stats */

  @Test
  public void getHostStatsInfoTest() throws Exception {
    String uuid = StatsTestUtils.HOST_UUID;

    new MockUp<StatsUtils>() {
      @mockit.Mock
      private StatsQueryResponse fetchStats(StatsQueryParams statsQueryParams)
        throws StatsGatewayServiceException {
        StatsQueryResponse resp = new StatsQueryResponse();
        resp.setEntityName("node");
        Map<String, StatsQueryResponse.AttributeStats> entityStatsMap = new HashMap<>();
        Map<String, List<StatsQueryResponse.TimeValuePair>> attrStatsMap = StatsTestUtils.getAttributeStatsMap(false,true);
        StatsQueryResponse.AttributeStats attributeStats = new StatsQueryResponse.AttributeStats();
        attributeStats.setAttributeStatsMap(attrStatsMap);
        entityStatsMap.put(uuid, attributeStats);
        resp.setEntityStatsMap(entityStatsMap);
        return resp;
      }
    };

    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(StatsTestUtils.CLUSTER_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getHostIdfEntityObj(StatsTestUtils.HOST_UUID));
    HostStats resource = clusterService.getHostStatsInfo(StatsTestUtils.CLUSTER_UUID, uuid, DownSamplingOperator.AVG, 50, 100L, 100L, null);
    assertEquals(resource.getExtId(), uuid);
    assertEquals(resource.getStatType(), DownSamplingOperator.AVG);
    //any other assertions if required
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getHostStatsInfoTestInCaseOfException1() throws Exception {
    String uuid = TestConstantUtils.randomUUIDString();
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(StatsTestUtils.CLUSTER_UUID));
    clusterService.getHostStatsInfo(StatsTestUtils.CLUSTER_UUID, uuid, DownSamplingOperator.AVG, 50, 100L, 100L,null);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getHostStatsInfoTestInCaseOfException2() throws Exception {
    String uuid = TestConstantUtils.randomUUIDString();
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    clusterService.getHostStatsInfo(StatsTestUtils.CLUSTER_UUID, uuid, DownSamplingOperator.AVG, 50, 100L, 100L,null);
  }

  @Test(expectedExceptions = StatsGatewayServiceException.class)
  public void getHostStatsInfoTestInCaseOfException3() throws Exception {
    new MockUp<StatsUtils>() {
      @mockit.Mock
      private StatsQueryResponse fetchStats(StatsQueryParams statsQueryParams)
        throws StatsGatewayServiceException {
        throw new StatsGatewayServiceException("Test error", new Exception());
      }
    };
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(StatsTestUtils.CLUSTER_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getHostIdfEntityObj(StatsTestUtils.HOST_UUID));
    clusterService.getHostStatsInfo(StatsTestUtils.CLUSTER_UUID, StatsTestUtils.HOST_UUID, DownSamplingOperator.LAST, 50, 100L, 100L, null);
  }

  @Test
  public void getClusterStatsInfoTestInCaseOfSelection() throws Exception {
    String uuid = TestConstantUtils.randomUUIDString();

    new MockUp<StatsUtils>() {
      @mockit.Mock
      private StatsQueryResponse fetchStats(StatsQueryParams statsQueryParams)
        throws StatsGatewayServiceException {
        StatsQueryResponse resp = new StatsQueryResponse();
        resp.setEntityName("cluster");
        Map<String, StatsQueryResponse.AttributeStats> entityStatsMap = new HashMap<>();
        Map<String, List<StatsQueryResponse.TimeValuePair>> attrStatsMap = StatsTestUtils.getAttributeStatsMap(true,false);
        StatsQueryResponse.AttributeStats attributeStats = new StatsQueryResponse.AttributeStats();
        attributeStats.setAttributeStatsMap(attrStatsMap);
        entityStatsMap.put(uuid, attributeStats);
        resp.setEntityStatsMap(entityStatsMap);
        return resp;
      }
    };

    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(StatsTestUtils.CLUSTER_UUID));
    final String selectString = "controllerAvgIoLatencyUsecs,controllerNumReadIops,healthCheckScore";
    ClusterStats resource = clusterService.getClusterStatsInfo(uuid, DownSamplingOperator.AVG, 50, 100L, 100L, selectString);
    assertEquals(resource.getExtId(), uuid);
    assertEquals(resource.getStatType(), DownSamplingOperator.AVG);
    assertEquals(resource.getHealthCheckScore().size(), 1);
    assertEquals(resource.getControllerAvgIoLatencyUsecs().size(), 1);
    assertEquals(resource.getControllerNumReadIops().size(), 1);
  }

  @Test
  public void getHostStatsInfoTestInCaseOfSelection() throws Exception {
    String uuid = StatsTestUtils.HOST_UUID;

    new MockUp<StatsUtils>() {
      @mockit.Mock
      private StatsQueryResponse fetchStats(StatsQueryParams statsQueryParams)
        throws StatsGatewayServiceException {
        StatsQueryResponse resp = new StatsQueryResponse();
        resp.setEntityName("node");
        Map<String, StatsQueryResponse.AttributeStats> entityStatsMap = new HashMap<>();
        Map<String, List<StatsQueryResponse.TimeValuePair>> attrStatsMap = StatsTestUtils.getAttributeStatsMap(false,true);
        StatsQueryResponse.AttributeStats attributeStats = new StatsQueryResponse.AttributeStats();
        attributeStats.setAttributeStatsMap(attrStatsMap);
        entityStatsMap.put(uuid, attributeStats);
        resp.setEntityStatsMap(entityStatsMap);
        return resp;
      }
    };

    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(StatsTestUtils.CLUSTER_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getHostIdfEntityObj(StatsTestUtils.HOST_UUID));
    final String selectString = "hypervisorCpuUsagePpm,ioBandwidthKbps";
      HostStats resource = clusterService.getHostStatsInfo(StatsTestUtils.CLUSTER_UUID, uuid, DownSamplingOperator.AVG, 50, 100L, 100L, selectString);
    assertEquals(resource.getExtId(), uuid);
    assertEquals(resource.getStatType(), DownSamplingOperator.AVG);
    assertEquals(resource.getHypervisorCpuUsagePpm().size(), 1);
    assertEquals(resource.getIoBandwidthKbps().size(), 1);
    //any other assertions if required
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getClusterStatsInfoTestInCaseOfOdataParsingException() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(StatsTestUtils.CLUSTER_UUID));
    clusterService.getClusterStatsInfo(StatsTestUtils.CLUSTER_UUID, DownSamplingOperator.LAST, 50, 100L, 100L, "xyz");
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getHostStatsInfoTestInCaseOfOdataParsingException() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(StatsTestUtils.CLUSTER_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(HostTestUtils.getHostIdfEntityObj(StatsTestUtils.HOST_UUID));
    clusterService.getHostStatsInfo(StatsTestUtils.CLUSTER_UUID, StatsTestUtils.HOST_UUID, DownSamplingOperator.LAST, 50, 100L, 100L, "xyz");
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getClusterStatsInfoTestForPC() throws Exception {
    String uuid = TestConstantUtils.randomUUIDString();
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(StatsTestUtils.CLUSTER_UUID));
     clusterService.getClusterStatsInfo(uuid, DownSamplingOperator.AVG, 50, 100L, 100L, null);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getHostStatsInfoTestOnPC() throws Exception {
    String uuid = TestConstantUtils.randomUUIDString();
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(StatsTestUtils.CLUSTER_UUID));
    clusterService.getHostStatsInfo(StatsTestUtils.CLUSTER_UUID, uuid, DownSamplingOperator.AVG, 50, 100L, 100L,null);
  }
}
