/*
 * Copyright (c) 2021 Nutanix Inc. All rights reserved.
 *
 * Author: rakesh.falak@nutanix.com
 */
package com.nutanix.clustermgmtserver.services.api;

import com.nutanix.api.utils.stats.service.exception.StatsGatewayServiceException;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtGenericException;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceException;
import com.nutanix.prism.exception.idempotency.IdempotencySupportException;
import com.nutanix.util.base.Pair;
import com.nutanix.util.base.ValidationException;
import dp1.clu.clustermgmt.v4.config.*;
import dp1.clu.clustermgmt.v4.operations.*;
import dp1.clu.clustermgmt.v4.stats.*;
import dp1.clu.common.v1.stats.DownSamplingOperator;
import dp1.clu.prism.v4.config.TaskReference;

import java.util.List;

public interface ClusterService {
  Cluster getClusterEntity(String clusterUuid, String expand) throws ClustermgmtServiceException, ValidationException;
  Pair<Integer, List<Cluster>> getClusterEntities(Integer offset, Integer limit,
                                                     String filter, String orderBy, String apply, String select, String expand)
    throws ClustermgmtServiceException, ValidationException;
  SnmpConfig getSnmpConfig(String clusterUuid) throws ClustermgmtServiceException;
  ConfigCredentials getConfigCredentials(String clusterUuid) throws ClustermgmtServiceException;
  List<RsyslogServer> getRsyslogServerConfig(String clusterUuid)
    throws ClustermgmtServiceException;
  List<RackableUnit> getRackableUnits(String clusterUuid)
    throws ClustermgmtServiceException;
  List<DomainFaultTolerance> getDomainFaultToleranceStatus(String clusterUuid)
    throws ClustermgmtServiceException;
  RackableUnit getRackableUnit(String clusterUuid, String rackableUnitUuid)
    throws ClustermgmtServiceException;
  Host getHostEntity(String clusterUuid, String hostUuid) throws ClustermgmtServiceException;
  Pair<Integer, List<Host> > getHostEntities(String clusterUuid, final Integer offset, final Integer limit, String filter, String orderBy, String apply, String select)
    throws ClustermgmtServiceException, ValidationException;
  Pair<Integer, List<HostGpu> > getHostGpus(String clusterUuid, String hostUuid, Integer limit, Integer page, String filter, String orderBy, String selectString)
    throws ClustermgmtServiceException, ValidationException;
  Pair<Integer, List<HostGpu> > getClusterHostGpus(String clusterUuid, Integer limit, Integer page, String filter, String orderBy, String selectString)
    throws ClustermgmtServiceException, ValidationException;
  Pair<Integer, List<VirtualGpuProfile>> listVirtualGpuProfiles(String clusterUuid, Integer limit, Integer page, String filter, String orderBy) throws ClustermgmtServiceException, ValidationException;
  Pair<Integer, List<PhysicalGpuProfile>> listPhysicalGpuProfiles(String clusterUuid, Integer limit, Integer page, String filter, String orderBy) throws ClustermgmtServiceException, ValidationException;
  HostGpu getHostGpu(String clusterUuid, String hostUuid, String hostGpuUuid) throws ClustermgmtServiceException;
  TaskReference renameHost(HostNameParam body, String clusterExtId, String hostExtId) throws ClustermgmtServiceException, ValidationException;
  TaskReference updateCluster(Cluster body, String clusterExtId, String batchTask) throws ClustermgmtServiceException;
  TaskReference updateSnmpStatus(SnmpStatusParam status, String clusterExtId, String batchTask) throws ClustermgmtServiceException;
  TaskReference addSnmpTransport(SnmpTransport body, String clusterExtId, String batchTask) throws ClustermgmtServiceException, ValidationException;
  TaskReference removeSnmpTransport(SnmpTransport body, String clusterExtId, String batchTask) throws ClustermgmtServiceException, ValidationException;
  SnmpUser getSnmpUser(String clusterExtId, String userExtId) throws ClustermgmtServiceException;
  TaskReference addSnmpUser(SnmpUser body, String clusterExtId, String batchTask) throws ClustermgmtServiceException, ValidationException;
  TaskReference updateSnmpUser(SnmpUser body, String clusterExtId, String userExtId, String batchTask) throws ClustermgmtServiceException, ValidationException;
  TaskReference deleteSnmpUser(String clusterExtId, String userExtId, String batchTask) throws ClustermgmtServiceException, ValidationException;
  SnmpTrap getSnmpTrap(String clusterExtId, String trapExtId) throws ClustermgmtServiceException;
  TaskReference addSnmpTrap(SnmpTrap body, String clusterExtId, String batchTask) throws ClustermgmtServiceException, ValidationException;
  TaskReference updateSnmpTrap(SnmpTrap body, String clusterExtId, String trapExtId, String batchTask) throws ClustermgmtServiceException, ValidationException;
  TaskReference deleteSnmpTrap(String clusterExtId, String trapExtId, String batchTask) throws ClustermgmtServiceException, ValidationException;
  RsyslogServer getRsyslogServer(String clusterExtId, String rsyslogServerExtId) throws ClustermgmtServiceException;
  TaskReference addRsyslogServer(RsyslogServer body, String clusterExtId, String batchTask) throws ClustermgmtServiceException, ValidationException;
  TaskReference updateRsyslogServer(RsyslogServer body, String clusterExtId, String rsyslogServerExtId, String batchTask) throws ClustermgmtServiceException, ValidationException;
  TaskReference deleteRsyslogServer(String rsyslogServerExtId, String clusterExtId, String batchTask) throws ClustermgmtServiceException, ValidationException;
  Pair<Integer, List<Host> > getAllHostEntities(final Integer offset, final Integer limit, String filter, String orderBy, String apply, String select)
    throws ClustermgmtServiceException, ValidationException;
  Pair<Integer, List<HostGpu> > getAllHostGpus(Integer limit, Integer page, String filter, String orderBy, String selectString)
    throws ClustermgmtServiceException, ValidationException;
  TaskReference discoverUnconfiguredNodes(String clusterExtId, NodeDiscoveryParams discoverUnconfiguredNode) throws ClustermgmtServiceException, ValidationException;
  TaskReference getNodeNetworkingDetails(String clusterExtId,  NodeDetails nodesNetworkingDetails)
    throws ClustermgmtServiceException, ValidationException;
  void addNode(String clusterExtId,  ExpandClusterParams addNode, String taskUuid) throws ClustermgmtServiceException, ValidationException;
  TaskReference isHypervisorUploadRequired(String clusterExtId,  HypervisorUploadParam hypervisorUploadParam) throws ClustermgmtServiceException, ValidationException;
  void removeNode(String clusterExtId, NodeRemovalParams removeNode, String taskUuid) throws ClustermgmtServiceException, ValidationException;
  TaskReference validateNode(String clusterExtId, ValidateNodeParam body) throws ClustermgmtServiceException;
  TaskResponse getTaskResponse(final String taskExtId, TaskResponseType taskResponseType) throws
    ClustermgmtServiceException;
  Pair<Integer, List<HostNic> > getHostNics(String clusterUuid, String hostUuid, Integer limit, Integer page, String filter, String orderBy, String selectString)
    throws ClustermgmtServiceException, ValidationException;
  Pair<Integer, List<HostNic> > getAllHostNics(Integer limit, Integer page, String filter, String orderBy, String selectString)
    throws ClustermgmtServiceException, ValidationException;
  HostNic getHostNic(String clusterUuid, String hostUuid, String hostNicUuid) throws ClustermgmtServiceException;
  Pair<Integer, List<VirtualNic> > getVirtualNics(String clusterUuid, String hostUuid, Integer limit, Integer page, String filter, String orderBy, String selectString)
    throws ClustermgmtServiceException, ValidationException;
  VirtualNic getVirtualNic(String clusterUuid, String hostUuid, String virtualNicUuid) throws ClustermgmtServiceException;
  ClusterStats getClusterStatsInfo(final String extId, final DownSamplingOperator statsType, final Integer samplingInterval,  final Long startTime, final Long endTime, final String selectString)  throws
    StatsGatewayServiceException, ClustermgmtServiceException;
  HostStats getHostStatsInfo(final String clusterUuid, final String hostUuid, final DownSamplingOperator statsType, final Integer samplingInterval,  final Long startTime, final Long endTime, final String selectString)  throws
    StatsGatewayServiceException, ClustermgmtServiceException;
  void clusterCreate(Cluster params, String taskUuid, Boolean $dryRun) throws ClustermgmtServiceException;
  void clusterDestroy(String clusterUuid, String taskUuid, Boolean runPrechecksOnlyFlag) throws ClustermgmtServiceException;
  TaskReference updateCategoryAssociationsForClusterEntity(CategoryEntityReferences params, String clusterUuid, String operationType) throws ClustermgmtServiceException;
  TaskReference updateCategoryAssociationsForHostNicEntity(CategoryEntityReferences params, String hostNicUuid, String clusterUuid, String hostUuid, String operationType) throws ClustermgmtServiceException;
  void enterHostMaintenance(String clusterExtId, String hostExtId, EnterHostMaintenanceSpec enterHostMaintenance, String taskUuid)
    throws ClustermgmtServiceException, ValidationException, IdempotencySupportException, ClustermgmtGenericException;
  void exitHostMaintenance(String clusterExtId, String hostExtId, HostMaintenanceCommonSpec exitHostMaintenance, String taskUuid)
    throws ClustermgmtServiceException, ValidationException, IdempotencySupportException, ClustermgmtGenericException;
  void computeNonMigratableVms(String clusterUuid, ComputeNonMigratableVmsSpec computeNonMigratableVms, String taskUuid)
    throws ClustermgmtServiceException, ValidationException, IdempotencySupportException, ClustermgmtGenericException;
  NonMigratableVmsResult getNonMigratableVmsResult(String extId)
    throws ClustermgmtServiceException, ValidationException;
}
