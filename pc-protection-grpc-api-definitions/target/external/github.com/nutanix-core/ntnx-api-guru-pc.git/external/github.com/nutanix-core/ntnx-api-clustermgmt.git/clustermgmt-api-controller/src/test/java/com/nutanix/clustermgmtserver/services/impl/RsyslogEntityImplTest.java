package com.nutanix.clustermgmtserver.services.impl;

import com.nutanix.api.utils.task.ErgonTaskUtils;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceException;
import com.nutanix.clustermgmtserver.proxy.EntityDbProxyImpl;
import com.nutanix.clustermgmtserver.proxy.ErgonProxyImpl;
import com.nutanix.clustermgmtserver.proxy.GenesisProxyImpl;
import com.nutanix.clustermgmtserver.utils.ClusterTestUtils;
import dp1.clu.clustermgmt.v4.config.RsyslogModuleItem;
import dp1.clu.clustermgmt.v4.config.RsyslogModuleName;
import dp1.clu.clustermgmt.v4.config.RsyslogServer;
import dp1.clu.prism.v4.config.TaskReference;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/*
This file contains UTs for following API endpoints:
GET Rsyslog servers(list)
GET Rsyslog server
POST Create Rsyslog server entity
PUT Update Rsyslog server entity
DELETE Remove Rsyslog server entity
 */

public class RsyslogEntityImplTest extends ClusterServiceImplTest{

  public RsyslogEntityImplTest(){
    super();
  }

  /* UTs for GET Rsyslog servers(list) */

  @Test
  public void getRsyslogServerConfigOnPCTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(ClusterTestUtils.getRemoteSyslogConfigurationIdfEntitiesObj(ClusterTestUtils.RSYSLOG_SERVER_UUID));
    RsyslogServer rsyslogServer1 = clusterService.getRsyslogServerConfig(ClusterTestUtils.CLUSTER_UUID).get(0);
    assertEquals(rsyslogServer1.getExtId(), ClusterTestUtils.RSYSLOG_SERVER_UUID);
    assertEquals(rsyslogServer1.getServerName(), ClusterTestUtils.RSYSLOG_SERVER_NAME);
  }

  @Test
  public void getRsyslogServerConfigOnPETest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRetWithoutAsyncSupport(ClusterTestUtils.CLUSTER_UUID));
    when(multiClusterZeusConfig.getZeusConfiguration(any())).thenReturn(ClusterTestUtils.buildZeusConfigurationWithRsylogConfig());
    assertEquals(clusterService.getRsyslogServerConfig(ClusterTestUtils.CLUSTER_UUID).get(0).getExtId(), ClusterTestUtils.RSYSLOG_SERVER_UUID);
  }

  /* UTs for GET Rsyslog server entity */

  @Test
  public void getRsyslogServerOnPCTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(ClusterTestUtils.getRemoteSyslogConfigurationIdfEntitiesObj(ClusterTestUtils.RSYSLOG_SERVER_UUID));
    assertEquals(clusterService.getRsyslogServer(ClusterTestUtils.CLUSTER_UUID,ClusterTestUtils.RSYSLOG_SERVER_UUID).getExtId(),
      ClusterTestUtils.RSYSLOG_SERVER_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getRsyslogServerOnPCTestFail() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(ClusterTestUtils.IdfEntityObjWithMetricRetWithNoEntity());
    assertEquals(clusterService.getRsyslogServer(ClusterTestUtils.CLUSTER_UUID,ClusterTestUtils.RSYSLOG_SERVER_UUID).getExtId(),
      ClusterTestUtils.RSYSLOG_SERVER_UUID);
  }

  @Test
  public void getRsyslogServerOnPETest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRetWithoutAsyncSupport(ClusterTestUtils.CLUSTER_UUID));
    when(multiClusterZeusConfig.getZeusConfiguration(any())).thenReturn(ClusterTestUtils.buildZeusConfigurationWithRsylogConfig());
    assertEquals(clusterService.getRsyslogServer(ClusterTestUtils.CLUSTER_UUID,ClusterTestUtils.RSYSLOG_SERVER_UUID).getExtId(),
      ClusterTestUtils.RSYSLOG_SERVER_UUID);
  }

  /* UTs for POST Create Rsyslog server entity */

  @Test
  public void addRsyslogServerOnPETest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));;
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    RsyslogServer rsyslogServer = ClusterTestUtils.getRsyslogServer();
    TaskReference task = clusterService.addRsyslogServer(rsyslogServer,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void addRsyslogServerOnPEWithoutAsyncSupportTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRetWithoutAsyncSupport(ClusterTestUtils.CLUSTER_UUID));;
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    RsyslogServer rsyslogServer = ClusterTestUtils.getRsyslogServer();
    TaskReference task = clusterService.addRsyslogServer(rsyslogServer,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void addRsyslogServerOnPEnatsFailTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));;
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(false);
    RsyslogServer rsyslogServer = ClusterTestUtils.getRsyslogServer();
    TaskReference task = clusterService.addRsyslogServer(rsyslogServer,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void addRsyslogServerOnPCTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    when(genesisProxy.doExecute(eq(GenesisProxyImpl.GenesisRpcName.CREATE_RSYSLOG_SERVER), any(), any()))
      .thenReturn(ClusterTestUtils.getCreateRsyslogServerRet());
    RsyslogServer rsyslogServer = ClusterTestUtils.getRsyslogServer();
    TaskReference task = clusterService.addRsyslogServer(rsyslogServer, ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
    assertEquals(ErgonTaskUtils.getTaskUuid(task.getExtId()), ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void addRsyslogServerOnPCTestFailureCase() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    when(genesisProxy.doExecute(eq(GenesisProxyImpl.GenesisRpcName.CREATE_RSYSLOG_SERVER), any(), any()))
      .thenThrow(new ClustermgmtServiceException("Unknown error in genesis"));
    RsyslogServer rsyslogServer = ClusterTestUtils.getRsyslogServer();
    TaskReference task = clusterService.addRsyslogServer(rsyslogServer,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
  }

  /* UTs for PUT Update Rsyslog server entity */

  @Test
  public void updateRsyslogServerOnPETest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));;
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    RsyslogServer rsyslogServer = ClusterTestUtils.getRsyslogServer();
    TaskReference task = clusterService.updateRsyslogServer(rsyslogServer,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.RSYSLOG_SERVER_NAME, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void updateRsyslogServerOnPEWithoutAsyncSupportTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRetWithoutAsyncSupport(ClusterTestUtils.CLUSTER_UUID));;
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    RsyslogServer rsyslogServer = ClusterTestUtils.getRsyslogServer();
    TaskReference task = clusterService.updateRsyslogServer(rsyslogServer,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.RSYSLOG_SERVER_NAME, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void updateRsyslogServerOnPEnatsFailTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));;
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(false);
    RsyslogServer rsyslogServer = ClusterTestUtils.getRsyslogServer();
    TaskReference task = clusterService.updateRsyslogServer(rsyslogServer,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.RSYSLOG_SERVER_NAME, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void updateRsyslogServerOnPCTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    when(genesisProxy.doExecute(eq(GenesisProxyImpl.GenesisRpcName.UPDATE_RSYSLOG_SERVER), any(), any()))
      .thenReturn(ClusterTestUtils.getUpdateRsyslogServerRet());
    RsyslogServer rsyslogServer = ClusterTestUtils.getRsyslogServer();
    TaskReference task = clusterService.updateRsyslogServer(rsyslogServer,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.RSYSLOG_SERVER_NAME, ClusterTestUtils.TASK_UUID);
    assertEquals(ErgonTaskUtils.getTaskUuid(task.getExtId()), ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void updateRsyslogServerOnPCTestFailureCase() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    when(genesisProxy.doExecute(eq(GenesisProxyImpl.GenesisRpcName.UPDATE_RSYSLOG_SERVER), any(), any()))
      .thenThrow(new ClustermgmtServiceException("Unknown error in genesis"));
    RsyslogServer rsyslogServer = ClusterTestUtils.getRsyslogServer();
    TaskReference task = clusterService.updateRsyslogServer(rsyslogServer,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.RSYSLOG_SERVER_NAME, ClusterTestUtils.TASK_UUID);
  }

  /* UTs for DELETE Remove Rsyslog server entity*/

  @Test
  public void deleteRsyslogServerOnPETest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    TaskReference task = clusterService.deleteRsyslogServer(ClusterTestUtils.SNMP_TRAP_ADDRESS, ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void deleteRsyslogServerOnPEWithoutAsyncSupportTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRetWithoutAsyncSupport(ClusterTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    TaskReference task = clusterService.deleteRsyslogServer(ClusterTestUtils.SNMP_TRAP_ADDRESS, ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void deleteRsyslogServerOnPEnatsFailTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(false);
    TaskReference task = clusterService.deleteRsyslogServer(ClusterTestUtils.SNMP_TRAP_ADDRESS, ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void deleteRsyslogServerOnPCTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    when(genesisProxy.doExecute(eq(GenesisProxyImpl.GenesisRpcName.DELETE_RSYSLOG_SERVER), any(), any()))
      .thenReturn(ClusterTestUtils.getDeleteRsyslogServerRet());
    TaskReference task = clusterService.deleteRsyslogServer(ClusterTestUtils.SNMP_TRAP_ADDRESS, ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
    assertEquals(ErgonTaskUtils.getTaskUuid(task.getExtId()), ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void deleteRsyslogServerOnPCTestFailureCase() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    when(genesisProxy.doExecute(eq(GenesisProxyImpl.GenesisRpcName.DELETE_RSYSLOG_SERVER), any(), any()))
      .thenThrow(new ClustermgmtServiceException("Unknown error in genesis"));
    TaskReference task = clusterService.deleteRsyslogServer(ClusterTestUtils.SNMP_TRAP_ADDRESS, ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
  }

}
