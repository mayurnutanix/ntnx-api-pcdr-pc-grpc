package com.nutanix.clustermgmtserver.services.impl;

import com.nutanix.api.utils.task.ErgonTaskUtils;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceException;
import com.nutanix.clustermgmtserver.proxy.EntityDbProxyImpl;
import com.nutanix.clustermgmtserver.proxy.ErgonProxyImpl;
import com.nutanix.clustermgmtserver.proxy.GenesisProxyImpl;
import com.nutanix.clustermgmtserver.utils.ClusterTestUtils;
import com.nutanix.util.base.ValidationException;
import dp1.clu.clustermgmt.v4.config.*;
import dp1.clu.prism.v4.config.TaskReference;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/*
This file contains UTs for following API endpoints:
GET Snmp Config
POST Update Snmp status
POST Add Snmp transports
POST Remove Snmp transports
GET Snmp User
POST Add Snmp User
PUT Update Snmp User
DELETE Remove Snmp User
GET Snmp Trap
POST Create Snmp Trap
PUT Update Snmp Trap
DELETE Remove Snmp Trap
 */

public class SnmpEntityImplTest extends ClusterServiceImplTest{

  public SnmpEntityImplTest(){
    super();
  }

  /* UTs for GET Snmp config */

  @Test
  public void getSnmpConfigTestOnPE() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRetWithoutAsyncSupport(ClusterTestUtils.CLUSTER_UUID));
    when(multiClusterZeusConfig.getZeusConfiguration(any())).thenReturn(ClusterTestUtils.buildZeusConfigurationWithSnmpInfo());
    assertEquals(clusterService.getSnmpConfig(ClusterTestUtils.CLUSTER_UUID).getIsEnabled(), ClusterTestUtils.SNMP_STATUS);
    assertEquals(clusterService.getSnmpConfig(ClusterTestUtils.CLUSTER_UUID).getUsers().get(0).getExtId(),
      ClusterTestUtils.SNMP_USER_UUID);
    assertEquals(clusterService.getSnmpConfig(ClusterTestUtils.CLUSTER_UUID).getTransports().get(0).getPort(),
      ClusterTestUtils.SNMP_PORT);
    assertEquals(clusterService.getSnmpConfig(ClusterTestUtils.CLUSTER_UUID).getTraps().get(0).getExtId(),
      ClusterTestUtils.SNMP_TRAP_UUID);
  }

  @Test
  public void getSnmpConfigTestWithOtherConditionsOnPE() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRetWithoutAsyncSupport(ClusterTestUtils.CLUSTER_UUID));
    when(multiClusterZeusConfig.getZeusConfiguration(any())).thenReturn(ClusterTestUtils.buildZeusConfigurationWithSnmpInfoWithConditions());
    assertEquals(clusterService.getSnmpConfig(ClusterTestUtils.CLUSTER_UUID).getIsEnabled(), Boolean.FALSE);
  }

  @Test
  public void getSnmpConfigOnPCTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));

    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(ClusterTestUtils.getSnmpUserIdfEntityObj(ClusterTestUtils.SNMP_USER_UUID))
      .thenReturn(ClusterTestUtils.getSnmpTrapIdfEntityObj(ClusterTestUtils.SNMP_TRAP_UUID))
      .thenReturn(ClusterTestUtils.getSnmpTransportIdfEntityObj(ClusterTestUtils.SNMP_TRANSPORT_UUID));
    SnmpConfig snmpConfig = clusterService.getSnmpConfig(ClusterTestUtils.CLUSTER_UUID);
    assertEquals(snmpConfig.getIsEnabled(), ClusterTestUtils.SNMP_STATUS);
    assertEquals(snmpConfig.getUsers().get(0).getUsername(), ClusterTestUtils.SNMP_USERNAME);
    assertEquals(snmpConfig.getUsers().get(0).getAuthType(), SnmpAuthType.SHA);
    assertEquals(snmpConfig.getUsers().get(0).getExtId(), ClusterTestUtils.SNMP_USER_UUID);
    assertEquals(snmpConfig.getTraps().get(0).getExtId(), ClusterTestUtils.SNMP_TRAP_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getSnmpConfigTestInCaseOfException() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObjWithNoEntity());
    clusterService.getSnmpConfig(ClusterTestUtils.CLUSTER_UUID);
  }

  /* UTs for POST Update Snmp status */

  @Test
  public void updateSnmpStatusOnPETest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));;
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    TaskReference task = clusterService.updateSnmpStatus(new SnmpStatusParam(true),ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void updateSnmpStatusOnPEWithoutAsyncSupportTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRetWithoutAsyncSupport(ClusterTestUtils.CLUSTER_UUID));;
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    TaskReference task = clusterService.updateSnmpStatus(new SnmpStatusParam(true),ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void updateSnmpStatusOnPEnatsFailTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));;
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(false);
    TaskReference task = clusterService.updateSnmpStatus(new SnmpStatusParam(true),ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void updateSnmpStatusOnPCTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    when(genesisProxy.doExecute(eq(GenesisProxyImpl.GenesisRpcName.UPDATE_SNMP_STATUS), any(), any()))
      .thenReturn(ClusterTestUtils.getSnmpStatusRet());
    TaskReference task = clusterService.updateSnmpStatus(new SnmpStatusParam(true),ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
    assertEquals(ErgonTaskUtils.getTaskUuid(task.getExtId()), ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void updateSnmpStatusOnPCTestFailureCase() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_UPDATE), any()))
      .thenReturn(ClusterTestUtils.getUpdateTaskRet());
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_GET), any()))
      .thenReturn(ClusterTestUtils.getTaskRet());
    when(genesisProxy.doExecute(eq(GenesisProxyImpl.GenesisRpcName.UPDATE_SNMP_STATUS), any(), any()))
      .thenThrow(new ClustermgmtServiceException("Unknown error in genesis"));
    TaskReference task = clusterService.updateSnmpStatus(new SnmpStatusParam(true),ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
  }

  /* UTs for POST Add Snmp transports */

  @Test
  public void addSnmpTransportOnPETest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));;
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    SnmpTransport snmpTransport = ClusterTestUtils.getSnmpTransport(true);
    TaskReference task = clusterService.addSnmpTransport(snmpTransport,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void addSnmpTransportOnPEWithoutAsyncSupportTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRetWithoutAsyncSupport(ClusterTestUtils.CLUSTER_UUID));;
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    SnmpTransport snmpTransport = ClusterTestUtils.getSnmpTransport(true);
    TaskReference task = clusterService.addSnmpTransport(snmpTransport,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void addSnmpTransportOnPEnatsFailtest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));;
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(false);
    SnmpTransport snmpTransport = ClusterTestUtils.getSnmpTransport(true);
    TaskReference task = clusterService.addSnmpTransport(snmpTransport,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void addSnmpTransportOnPCTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    when(genesisProxy.doExecute(eq(GenesisProxyImpl.GenesisRpcName.ADD_SNMP_TRANSPORTS), any(), any()))
      .thenReturn(ClusterTestUtils.getAddSnmpTransportsRet());
    SnmpTransport snmpTransport = ClusterTestUtils.getSnmpTransport(true);
    TaskReference task = clusterService.addSnmpTransport(snmpTransport,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
    assertEquals(ErgonTaskUtils.getTaskUuid(task.getExtId()), ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void addSnmpTransportOnPCTestFailureCase() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    when(genesisProxy.doExecute(eq(GenesisProxyImpl.GenesisRpcName.ADD_SNMP_TRANSPORTS), any(), any()))
      .thenThrow(new ClustermgmtServiceException("Unknown error in genesis"));
    SnmpTransport snmpTransport = ClusterTestUtils.getSnmpTransport(true);
    TaskReference task = clusterService.addSnmpTransport(snmpTransport,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ValidationException.class)
  public void addSnmpTransportOnPCTestInCaseOfInvalidPort() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    when(genesisProxy.doExecute(eq(GenesisProxyImpl.GenesisRpcName.ADD_SNMP_TRANSPORTS), any(), any()))
      .thenReturn(ClusterTestUtils.getAddSnmpTransportsRet());
    SnmpTransport snmpTransport = ClusterTestUtils.getSnmpTransport(false);
    clusterService.addSnmpTransport(snmpTransport,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
  }

  /* UTs for POST Remove Snmp Transport */

  @Test
  public void removeSnmpTransportOnPETest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));;
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    SnmpTransport snmpTransport = ClusterTestUtils.getSnmpTransport(true);
    TaskReference task = clusterService.removeSnmpTransport(
      snmpTransport,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void removeSnmpTransportOnPEWithoutAsyncSupportTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRetWithoutAsyncSupport(ClusterTestUtils.CLUSTER_UUID));;
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    SnmpTransport snmpTransport = ClusterTestUtils.getSnmpTransport(true);
    TaskReference task = clusterService.removeSnmpTransport(
      snmpTransport,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void removeSnmpTransportOnPEnatsFailTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));;
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(false);
    SnmpTransport snmpTransport = ClusterTestUtils.getSnmpTransport(true);
    TaskReference task = clusterService.removeSnmpTransport(
      snmpTransport,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void removeSnmpTransportOnPCTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    when(genesisProxy.doExecute(eq(GenesisProxyImpl.GenesisRpcName.REMOVE_SNMP_TRANSPORTS), any(), any()))
      .thenReturn(ClusterTestUtils.getRemoveSnmpTransportsRet());
    SnmpTransport snmpTransport = ClusterTestUtils.getSnmpTransport(true);
    TaskReference task = clusterService.removeSnmpTransport(
      snmpTransport, ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
    assertEquals(ErgonTaskUtils.getTaskUuid(task.getExtId()), ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void removeSnmpTransportOnPCTestFailureCase() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    when(genesisProxy.doExecute(eq(GenesisProxyImpl.GenesisRpcName.REMOVE_SNMP_TRANSPORTS), any(), any()))
      .thenThrow(new ClustermgmtServiceException("Unknown error in genesis"));
    SnmpTransport snmpTransport = ClusterTestUtils.getSnmpTransport(true);
    TaskReference task = clusterService.removeSnmpTransport(
      snmpTransport, ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  /* UTs for GET Snmp User */

  @Test
  public void getSnmpUserOnPCTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(ClusterTestUtils.getSnmpUserIdfEntityObj(ClusterTestUtils.SNMP_USER_UUID));
    assertEquals(clusterService.getSnmpUser(ClusterTestUtils.CLUSTER_UUID,ClusterTestUtils.SNMP_USER_UUID).getExtId(),
      ClusterTestUtils.SNMP_USER_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getSnmpUserOnPcTestFail() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(ClusterTestUtils.IdfEntityObjWithMetricRetWithNoEntity());
    clusterService.getSnmpUser(ClusterTestUtils.CLUSTER_UUID,ClusterTestUtils.SNMP_USER_UUID);
  }

  @Test
  public void getSnmpUserOnPEWithoutAsyncSupportTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRetWithoutAsyncSupport(ClusterTestUtils.CLUSTER_UUID));
    when(multiClusterZeusConfig.getZeusConfiguration(any())).thenReturn(ClusterTestUtils.buildZeusConfigurationWithSnmpInfo());
    assertEquals(clusterService.getSnmpUser(ClusterTestUtils.CLUSTER_UUID,ClusterTestUtils.SNMP_USER_UUID).getExtId(),
      ClusterTestUtils.SNMP_USER_UUID);
  }

  /* UTs for POST Add Snmp User */

  @Test
  public void addSnmpUserOnPETest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));;
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    SnmpUser snmpUser = ClusterTestUtils.getSnmpUser();
    TaskReference task = clusterService.addSnmpUser(snmpUser,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void addSnmpUserOnPEWithoutAsyncSupportTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRetWithoutAsyncSupport(ClusterTestUtils.CLUSTER_UUID));;
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    SnmpUser snmpUser = ClusterTestUtils.getSnmpUser();
    TaskReference task = clusterService.addSnmpUser(snmpUser,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void addSnmpUserOnPEnatsFailTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));;
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(false);
    SnmpUser snmpUser = ClusterTestUtils.getSnmpUser();
    TaskReference task = clusterService.addSnmpUser(snmpUser,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void addSnmpUserOnPCTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    when(genesisProxy.doExecute(eq(GenesisProxyImpl.GenesisRpcName.ADD_SNMP_USER), any(), any()))
      .thenReturn(ClusterTestUtils.getAddSnmpUserRet());
    SnmpUser snmpUser = ClusterTestUtils.getSnmpUser();
    TaskReference task = clusterService.addSnmpUser(snmpUser,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
    assertEquals(ErgonTaskUtils.getTaskUuid(task.getExtId()), ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void addSnmpUserOnPCTestFailureCase() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    when(genesisProxy.doExecute(eq(GenesisProxyImpl.GenesisRpcName.ADD_SNMP_USER), any(), any()))
      .thenThrow(new ClustermgmtServiceException("Unknown error in genesis"));
    SnmpUser snmpUser = ClusterTestUtils.getSnmpUser();
    TaskReference task = clusterService.addSnmpUser(snmpUser,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ValidationException.class)
  public void addSnmpUserOnPCTestInCaseOfUserValidationFailure1() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    when(genesisProxy.doExecute(eq(GenesisProxyImpl.GenesisRpcName.ADD_SNMP_USER), any(), any()))
      .thenReturn(ClusterTestUtils.getAddSnmpUserRet());
    SnmpUser snmpUser = ClusterTestUtils.getInvalidSnmpUser(true, false);
    clusterService.addSnmpUser(snmpUser,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ValidationException.class)
  public void addSnmpUserOnPCTestInCaseOfUserValidationFailure2() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    when(genesisProxy.doExecute(eq(GenesisProxyImpl.GenesisRpcName.ADD_SNMP_USER), any(), any()))
      .thenReturn(ClusterTestUtils.getAddSnmpUserRet());
    SnmpUser snmpUser = ClusterTestUtils.getInvalidSnmpUser(false, true);
    clusterService.addSnmpUser(snmpUser,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
  }

  /* UTs for POST Update Snmp User */

  @Test
  public void updateSnmpUserOnPETest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));;
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    SnmpUser snmpUser = ClusterTestUtils.getSnmpUser();
    TaskReference task = clusterService.updateSnmpUser(snmpUser,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.SNMP_USERNAME, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void updateSnmpUserOnPEWithoutAsyncSupportTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRetWithoutAsyncSupport(ClusterTestUtils.CLUSTER_UUID));;
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    SnmpUser snmpUser = ClusterTestUtils.getSnmpUser();
    TaskReference task = clusterService.updateSnmpUser(snmpUser,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.SNMP_USERNAME, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void updateSnmpUserOnPEnatsFailTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));;
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(false);
    SnmpUser snmpUser = ClusterTestUtils.getSnmpUser();
    TaskReference task = clusterService.updateSnmpUser(snmpUser,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.SNMP_USERNAME, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void updateSnmpUserOnPCTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    when(genesisProxy.doExecute(eq(GenesisProxyImpl.GenesisRpcName.UPDATE_SNMP_USER), any(), any()))
      .thenReturn(ClusterTestUtils.getUpdateSnmpUserRet());
    SnmpUser snmpUser = ClusterTestUtils.getSnmpUser();
    TaskReference task = clusterService.updateSnmpUser(snmpUser,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.SNMP_USERNAME, ClusterTestUtils.TASK_UUID);
    assertEquals(ErgonTaskUtils.getTaskUuid(task.getExtId()), ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void updateSnmpUserOnPCTestFailureCase() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    when(genesisProxy.doExecute(eq(GenesisProxyImpl.GenesisRpcName.UPDATE_SNMP_USER), any(), any()))
      .thenThrow(new ClustermgmtServiceException("Unknown error in genesis"));
    SnmpUser snmpUser = ClusterTestUtils.getSnmpUser();
    TaskReference task = clusterService.updateSnmpUser(snmpUser,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.SNMP_USERNAME, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  /* UTs for DELETE Remove Snmp User */

  @Test
  public void deleteSnmpUserOnPETest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));;
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    TaskReference task = clusterService.deleteSnmpUser(
      ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.SNMP_USERNAME, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void deleteSnmpUserOnPEWithoutAsyncSupportTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRetWithoutAsyncSupport(ClusterTestUtils.CLUSTER_UUID));;
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    TaskReference task = clusterService.deleteSnmpUser(
      ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.SNMP_USERNAME, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void deleteSnmpUserOnPEnatsFailTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));;
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(false);
    TaskReference task = clusterService.deleteSnmpUser(
      ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.SNMP_USERNAME, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void deleteSnmpUserOnPCTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    when(genesisProxy.doExecute(eq(GenesisProxyImpl.GenesisRpcName.DELETE_SNMP_USER), any(), any()))
      .thenReturn(ClusterTestUtils.getDeleteSnmpUserRet());
    TaskReference task = clusterService.deleteSnmpUser(
      ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.SNMP_USERNAME, ClusterTestUtils.TASK_UUID);
    assertEquals(ErgonTaskUtils.getTaskUuid(task.getExtId()), ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void deleteSnmpUserOnPCTestFailureCase() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_UPDATE), any()))
      .thenReturn(ClusterTestUtils.getUpdateTaskRet());
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_GET), any()))
      .thenReturn(ClusterTestUtils.getTaskRet());
    when(genesisProxy.doExecute(eq(GenesisProxyImpl.GenesisRpcName.DELETE_SNMP_USER), any(), any()))
      .thenThrow(new ClustermgmtServiceException("Unknown error in genesis"));
    TaskReference task = clusterService.deleteSnmpUser(
      ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.SNMP_USERNAME, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }


  /* UTs for GET Snmp Trap */

  @Test
  public void getSnmpTrapOnPCTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(ClusterTestUtils.getSnmpTrapIdfEntityObj(ClusterTestUtils.SNMP_TRAP_UUID));
    assertEquals(clusterService.getSnmpTrap(ClusterTestUtils.CLUSTER_UUID,ClusterTestUtils.SNMP_TRAP_UUID).getExtId(),
      ClusterTestUtils.SNMP_TRAP_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void getSnmpTrapOnPcTestFail() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS), any()))
      .thenReturn(ClusterTestUtils.IdfEntityObjWithMetricRetWithNoEntity());
    clusterService.getSnmpTrap(ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.SNMP_TRAP_UUID);
  }

  @Test
  public void getSnmpTrapOnPEWithoutAsyncSupportTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRetWithoutAsyncSupport(ClusterTestUtils.CLUSTER_UUID));
    when(multiClusterZeusConfig.getZeusConfiguration(any())).thenReturn(ClusterTestUtils.buildZeusConfigurationWithSnmpInfo());
    assertEquals(clusterService.getSnmpTrap(ClusterTestUtils.CLUSTER_UUID,ClusterTestUtils.SNMP_TRAP_UUID).getExtId(),
      ClusterTestUtils.SNMP_TRAP_UUID);
  }

  /* UTs for POST Create Snmp Trap */

  @Test
  public void addSnmpTrapOnPETest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));;
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    SnmpTrap snmpTrap = ClusterTestUtils.getSnmpTrap();
    TaskReference task = clusterService.addSnmpTrap(snmpTrap,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void addSnmpTrapOnPEWithoutAsyncSupportTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRetWithoutAsyncSupport(ClusterTestUtils.CLUSTER_UUID));;
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    SnmpTrap snmpTrap = ClusterTestUtils.getSnmpTrap();
    TaskReference task = clusterService.addSnmpTrap(snmpTrap,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void addSnmpTrapOnPEnatsFailTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));;
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(false);
    SnmpTrap snmpTrap = ClusterTestUtils.getSnmpTrap();
    TaskReference task = clusterService.addSnmpTrap(snmpTrap,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void addSnmpTrapOnPCTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    when(genesisProxy.doExecute(eq(GenesisProxyImpl.GenesisRpcName.ADD_SNMP_TRAP), any(), any()))
      .thenReturn(ClusterTestUtils.getAddSnmpTrapRet());
    SnmpTrap snmpTrap = ClusterTestUtils.getSnmpTrap();
    TaskReference task = clusterService.addSnmpTrap(snmpTrap,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
    assertEquals(ErgonTaskUtils.getTaskUuid(task.getExtId()), ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void addSnmpTrapOnPCTestFailureCase() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    when(genesisProxy.doExecute(eq(GenesisProxyImpl.GenesisRpcName.ADD_SNMP_TRAP), any(), any()))
      .thenThrow(new ClustermgmtServiceException("Unknown error in genesis"));
    SnmpTrap snmpTrap = ClusterTestUtils.getSnmpTrap();
    TaskReference task = clusterService.addSnmpTrap(snmpTrap,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ValidationException.class)
  public void addSnmpTrapInCaseOfValidationFailure1() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    SnmpTrap snmpTrap1 = ClusterTestUtils.getInvalidSnmpTrapEntity(true, false, false, false, false);
    clusterService.addSnmpTrap(snmpTrap1,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ValidationException.class)
  public void addSnmpTrapInCaseOfValidationFailure2() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    SnmpTrap snmpTrap2 = ClusterTestUtils.getInvalidSnmpTrapEntity(false, true, false, false, false);
    clusterService.addSnmpTrap(snmpTrap2,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ValidationException.class)
  public void addSnmpTrapInCaseOfValidationFailure3() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    SnmpTrap snmpTrap3 = ClusterTestUtils.getInvalidSnmpTrapEntity(false, false, true, false, false);
    clusterService.addSnmpTrap(snmpTrap3,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ValidationException.class)
  public void addSnmpTrapInCaseOfValidationFailure4() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    SnmpTrap snmpTrap4 = ClusterTestUtils.getInvalidSnmpTrapEntity(false, false, false, true, false);
    clusterService.addSnmpTrap(snmpTrap4,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ValidationException.class)
  public void addSnmpTrapInCaseOfValidationFailure5() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));
    SnmpTrap snmpTrap5 = ClusterTestUtils.getInvalidSnmpTrapEntity(false, false, false, false, true);
    clusterService.addSnmpTrap(snmpTrap5,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.TASK_UUID);
  }

  /* UTs for PUT Update Snmp Trap */

  @Test
  public void updateSnmpTrapOnPETest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));;
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    SnmpTrap snmpTrap = ClusterTestUtils.getSnmpTrap();
    TaskReference task = clusterService.updateSnmpTrap(snmpTrap,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.SNMP_TRAP_ADDRESS, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void updateSnmpTrapOnPEWithoutAsyncSupportTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRetWithoutAsyncSupport(ClusterTestUtils.CLUSTER_UUID));;
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    SnmpTrap snmpTrap = ClusterTestUtils.getSnmpTrap();
    TaskReference task = clusterService.updateSnmpTrap(snmpTrap,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.SNMP_TRAP_ADDRESS, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void updateSnmpTrapOnPEnatsFailTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));;
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(false);
    SnmpTrap snmpTrap = ClusterTestUtils.getSnmpTrap();
    TaskReference task = clusterService.updateSnmpTrap(snmpTrap,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.SNMP_TRAP_ADDRESS, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void updateSnmpTrapOnPCTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    when(genesisProxy.doExecute(eq(GenesisProxyImpl.GenesisRpcName.UPDATE_SNMP_TRAP), any(), any()))
      .thenReturn(ClusterTestUtils.getUpdateSnmpTrapRet());
    SnmpTrap snmpTrap = ClusterTestUtils.getSnmpTrap();
    TaskReference task = clusterService.updateSnmpTrap(snmpTrap,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.SNMP_TRAP_ADDRESS, ClusterTestUtils.TASK_UUID);
    assertEquals(ErgonTaskUtils.getTaskUuid(task.getExtId()), ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void updateSnmpTrapOnPCTestFailureCase() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    when(genesisProxy.doExecute(eq(GenesisProxyImpl.GenesisRpcName.UPDATE_SNMP_TRAP), any(), any()))
      .thenThrow(new ClustermgmtServiceException("Unknown error in genesis"));
    SnmpTrap snmpTrap = ClusterTestUtils.getSnmpTrap();
    TaskReference task = clusterService.updateSnmpTrap(snmpTrap,ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.SNMP_TRAP_ADDRESS, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  /* UTs for DELETE Remove Snmp Trap */

  @Test
  public void deleteSnmpTrapOnPETest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));;
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    TaskReference task = clusterService.deleteSnmpTrap(
      ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.SNMP_TRAP_ADDRESS, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void deleteSnmpTrapOnPEWithoutAsyncSupportTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRetWithoutAsyncSupport(ClusterTestUtils.CLUSTER_UUID));;
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(true);
    TaskReference task = clusterService.deleteSnmpTrap(
      ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.SNMP_TRAP_ADDRESS, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void deleteSnmpTrapOnPEnatsFailTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityObj(ClusterTestUtils.CLUSTER_UUID))
      .thenReturn(ClusterTestUtils.getCapabilitiesEntitiesRet(ClusterTestUtils.CLUSTER_UUID));;
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_CREATE), any()))
      .thenReturn(ClusterTestUtils.getCreateTaskRet());
    when(messageBus.publishMessage(any(),any())).thenReturn(false);
    TaskReference task = clusterService.deleteSnmpTrap(
      ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.SNMP_TRAP_ADDRESS, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void deleteSnmpTrapOnPCTest() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    when(genesisProxy.doExecute(eq(GenesisProxyImpl.GenesisRpcName.DELETE_SNMP_TRAP), any(), any()))
      .thenReturn(ClusterTestUtils.getDeleteSnmpTrapRet());
    TaskReference task = clusterService.deleteSnmpTrap(
      ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.SNMP_TRAP_ADDRESS, ClusterTestUtils.TASK_UUID);
    assertEquals(ErgonTaskUtils.getTaskUuid(task.getExtId()), ClusterTestUtils.TASK_UUID);
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void deleteSnmpTrapOnPCTestFailureCase() throws Exception {
    when(entityDbProxy.doExecute(eq(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES), any()))
      .thenReturn(ClusterTestUtils.getClusterIdfEntityPcObj(ClusterTestUtils.CLUSTER_UUID));
    when(genesisProxy.doExecute(eq(GenesisProxyImpl.GenesisRpcName.DELETE_SNMP_TRAP), any(), any()))
      .thenThrow(new ClustermgmtServiceException("Unknown error in genesis"));
    TaskReference task = clusterService.deleteSnmpTrap(
      ClusterTestUtils.CLUSTER_UUID, ClusterTestUtils.SNMP_TRAP_ADDRESS, ClusterTestUtils.TASK_UUID);
    assertEquals(task.getExtId(), ClusterTestUtils.TASK_UUID);
  }

}
