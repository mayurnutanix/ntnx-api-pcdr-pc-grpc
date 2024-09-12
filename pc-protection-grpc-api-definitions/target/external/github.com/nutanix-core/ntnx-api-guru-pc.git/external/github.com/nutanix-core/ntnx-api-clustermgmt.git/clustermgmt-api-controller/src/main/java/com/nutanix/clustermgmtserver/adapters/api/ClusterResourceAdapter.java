/*
 * Copyright (c) 2021 Nutanix Inc. All rights reserved.
 *
 * Author: rakesh.falak@nutanix.com
 */
package com.nutanix.clustermgmtserver.adapters.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.protobuf.ByteString;
import com.nutanix.cluster_management.ClusterManagementInterfaceProto;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceException;
import com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto;
import com.nutanix.insights.ifc.InsightsInterfaceProto;
import com.nutanix.prism.adapter.service.ZeusConfiguration;
import com.nutanix.stats_gateway.graphql_interface.GroupsGraphqlInterfaceProto;
import dp1.clu.clustermgmt.v4.config.*;
import dp1.clu.clustermgmt.v4.operations.*;
import dp1.clu.clustermgmt.v4.stats.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ClusterResourceAdapter {
  public Cluster adaptIdfClusterMetricstoClusterEntity(final List<InsightsInterfaceProto.MetricData> metricDataList,
                                                             Cluster clusterEntity);
  public Cluster adaptZeusEntriestoClusterEntity(
    final ZeusConfiguration zkConfig, Cluster clusterEntity);
  public SnmpConfig adaptZeusEntriestoSnmpInfo(
    final ZeusConfiguration zkConfig, SnmpConfig snmpInfo);
  public List<SnmpUser> getSnmpUsers(ZeusConfiguration zkConfig);
  public List<SnmpTrap> getSnmpTraps(ZeusConfiguration zkConfig);
  public List<RsyslogServer> adaptZeusEntriestoRsyslogServer(
    final ZeusConfiguration zkConfig);
  public List<RackableUnit> adaptZeusEntriestoRackableUnits(
    final ZeusConfiguration zkConfig);
  public List<DomainFaultTolerance> adaptZeusEntriestoDomainFaultTolerance(
    final ZeusConfiguration zkConfig);
  public RackableUnit adaptZeusEntriestoRackableUnit(
    final ZeusConfiguration zkConfig, String rackableUnitUuid);
  public Host adaptZeusEntriestoHostEntity(final ZeusConfiguration zkConfig,
                                                 Host hostEntity,
                                                 final String hostUuid);
  public Host adaptIdfHostMetricstoHostEntity(final List<InsightsInterfaceProto.MetricData> metricDataList,
                                                             Host hostEntity);
  public Cluster adaptManagementServerIntoClusterEntity(
    final List<InsightsInterfaceProto.EntityWithMetric> entitiesWithMetric, Cluster clusterEntity);
  public HostGpu adaptIdfHostGpuMetricstoHostGpuEntity(final List<InsightsInterfaceProto.MetricData> metricDataList,
                                                    HostGpu hostGpuEntity);
  public PhysicalGpuProfile adaptIdfMetricsToPhysicalGpuProfile(final List<InsightsInterfaceProto.MetricData> metricDataList,
                                      PhysicalGpuProfile physicalGpuProfile);
  public VirtualGpuProfile adaptIdfMetricsToVirtualGpuProfile(final List<InsightsInterfaceProto.MetricData> metricDataList,
                                      VirtualGpuProfile virtualGpuProfile);
  public GenesisInterfaceProto.UpdateClusterArg adaptClusterEntityToClusterUpdateArg(Cluster body, GenesisInterfaceProto.UpdateClusterArg.Builder builder,
                                                                                     ByteString parentTaskUuid);

  public ConfigCredentials adaptGetConfigCredentialsRetToConfigCredentials(GenesisInterfaceProto.GetConfigCredentialsRet getConfigCredentialsRet, ConfigCredentials configCredentials);
  public GenesisInterfaceProto.AddSnmpTransportsArg adaptSnmpTransportsToAddTransportsArg(SnmpTransport snmpTransport, GenesisInterfaceProto.AddSnmpTransportsArg.Builder builder,
                                                                                          ByteString parentTaskUuid);
  public GenesisInterfaceProto.RemoveSnmpTransportsArg adaptSnmpTransportsToRemoveTransportsArg(SnmpTransport snmpTransport, GenesisInterfaceProto.RemoveSnmpTransportsArg.Builder builder,
                                                                                                ByteString parentTaskUuid);
  public GenesisInterfaceProto.AddSnmpUserArg adaptSnmpUserToAddSnmpUserArg(SnmpUser snmpUser, GenesisInterfaceProto.AddSnmpUserArg.Builder builder,
                                                                            ByteString parentTaskUuid);
  public GenesisInterfaceProto.UpdateSnmpUserArg adaptSnmpUserToUpdateSnmpUserArg(SnmpUser snmpUser, GenesisInterfaceProto.UpdateSnmpUserArg.Builder builder,
                                                                                  ByteString parentTaskUuid);
  public GenesisInterfaceProto.DeleteSnmpUserArg adaptSnmpUserToDeleteSnmpUserArg(String userExtId, GenesisInterfaceProto.DeleteSnmpUserArg.Builder builder,
                                                                                  ByteString parentTaskUuid);
  public SnmpUser adaptSnmpUserProtoToSnmpUserEntity(String userExtId, GenesisInterfaceProto.SnmpUser snmpUser);
  public GenesisInterfaceProto.AddSnmpTrapArg adaptSnmpTrapToAddSnmpTrapArg(SnmpTrap snmpTrap, GenesisInterfaceProto.AddSnmpTrapArg.Builder builder,
                                                                            ByteString parentTaskUuid);
  public GenesisInterfaceProto.UpdateSnmpTrapArg adaptSnmpTrapToUpdateSnmpTrapArg(SnmpTrap snmpTrap, GenesisInterfaceProto.UpdateSnmpTrapArg.Builder builder,
                                                                                  ByteString parentTaskUuid);
  public GenesisInterfaceProto.DeleteSnmpTrapArg adaptSnmpTrapToDeleteSnmpTrapArg(String trapExtId, GenesisInterfaceProto.DeleteSnmpTrapArg.Builder builder,
                                                                                  ByteString parentTaskUuid);
  public SnmpTrap adaptSnmpTrapProtoToSnmpTrapEntity(String trapExtId, GenesisInterfaceProto.SnmpTrap snmpTrap);
  public SnmpTransport adaptSnmpTransportProtoToSnmpTransportEntity(GenesisInterfaceProto.SnmpTransport snmpTransport);
  public RsyslogServer adaptRemoteSyslogConfigurationProtoToRsyslogServer(String rsyslogServerExtId,
                                                                          GenesisInterfaceProto.RemoteSyslogConfiguration remoteSyslogConfigurationProto);
  public HttpProxyConfig adaptHttpProxyConfigurationProtoToHttpProxy(String httpProxyExtId,
                                                                     GenesisInterfaceProto.HttpProxy httpProxyConfigurationProto);
  public HttpProxyWhiteListConfig adaptHttpProxyWhitelistConfigurationProtoToHttpProxyWhiteList(String httpProxyWhiteListExtId,
                                                                                                GenesisInterfaceProto.HttpProxyWhitelist httpProxyWhiteListConfigurationProto);
  public GenesisInterfaceProto.CreateRemoteSyslogServerArg adaptRsyslogServerToCreateRsyslogServerArg(RsyslogServer rsyslogServer,
                                                                                                      GenesisInterfaceProto.CreateRemoteSyslogServerArg.Builder builder,
                                                                                                      ByteString parentTaskUuid);
  public GenesisInterfaceProto.UpdateRemoteSyslogServerArg adaptRsyslogServerToUpdateRsyslogServerArg(RsyslogServer rsyslogServer,
                                                                                                      GenesisInterfaceProto.UpdateRemoteSyslogServerArg.Builder builder,
                                                                                                      ByteString parentTaskUuid);
  public GenesisInterfaceProto.DeleteRemoteSyslogServerArg adaptRsyslogServerNameToDeleteRsyslogServerArg(String rsyslogServerExtId,
                                                                                                          GenesisInterfaceProto.DeleteRemoteSyslogServerArg.Builder builder,
                                                                                                          ByteString parentTaskUuid);

  public String adaptDiscoverUnconfigureNodeToJsonRpcFormat(NodeDiscoveryParams discoverUnconfiguredNode, String pcTaskUuid);
  public String adaptNodeNetworkingDetailsToJsonRpcFormat(NodeDetails nodesNetworkingDetails, String pcTaskUuid);
  public String adaptAddNodeToJsonRpcFormat(ExpandClusterParams addNode, String pcTaskUuid);
  public String adaptHypervisorUploadParamToJsonRpcFormat(HypervisorUploadParam hypervisorUploadParam, String pcTaskUuid);
  public String adaptIsBundleCompatibleToJsonRpcFormat(BundleParam validateBundleParam, String pcTaskUuid);
  public String adaptRemoveNodeToJsonRpcFormat(NodeRemovalParams removeNode, String pcTaskUuid);
  public String adaptValidateUplinkToJsonRpcFormat(List<UplinkNode> nodeItems, String pcTaskUuid);
  public UnconfigureNodeDetails adaptJsonResponsetoUnconfiguredNodes(JsonNode jsonObject, UnconfigureNodeDetails unconfigureNodesResponse);
  public NodeNetworkingDetails adaptJsonResponsetoNetworkingDetails(JsonNode jsonObject, NodeNetworkingDetails networkingDetailResponsee);
  public HypervisorUploadInfo adaptJsonResponsetoHypervisorIso(JsonNode jsonObject, HypervisorUploadInfo hypervisorUploadInfoResponse);
  public HostNic adaptIdfHostNicMetricstoHostNicEntity(List<InsightsInterfaceProto.MetricData> metricDataList, HostNic hostNicEntity);
  public NetworkSwitchInterface adaptIdfSwitchIntfMetricstoNetworkSwitchInterfaceEntity (List < InsightsInterfaceProto.MetricData > metricDataList,
                                                                                               NetworkSwitchInterface switchIntfEntity);
  public VirtualNic adaptIdfVirtualNicMetricstoVirtualNicEntity(List<InsightsInterfaceProto.MetricData> metricDataList, VirtualNic virtualNicEntity);
  public ClusterStats adaptToClusterStats(ClusterStats clusterStatsResp, Map<String, List<TimeValuePair>> statsAttributes);
  public HostStats adaptToHostStats(HostStats hostStatsResp, Map<String, List<TimeValuePair>> statsAttributes);
  public ClusterManagementInterfaceProto.ClusterCreateArg adaptClusterCreateParamsToClusterCreateArg(Cluster body,
                                                                                                     ClusterManagementInterfaceProto.ClusterCreateArg.Builder clusterCreateArg);
  public GenesisInterfaceProto.UpdateCategoryAssociationsArg adaptUpdateCategoriesParamsToUpdateCategoryAssociationsArg(Set<String> categoryUuids,
                                                                                                                        String entityType,
                                                                                                                        String entityUuid,
                                                                                                                        String operationType, GenesisInterfaceProto.UpdateCategoryAssociationsArg.Builder updateCategoryAssociationsArg);
  public Cluster adaptCategoriesToClusterEntity(List<InsightsInterfaceProto.MetricData> metricDataList, Cluster cluster);

  public List<Cluster> ProcessStatsResponseMapToClusters(
    List<Cluster> clusters, Map statsResponseMap);
  public Cluster adaptStatsGatewayResponseToClusters(final String attr, final Object value,
                                                     Cluster cluster);
  public Cluster adaptStatsGatewayResponseToClusterProfileExpand(final Object value, Cluster cluster);
  public Cluster adaptStatsGatewayResponseToStorageSummaryExpand(final Object value, Cluster cluster);
  public List<NonCompatibleClusterReference> adaptJsonResponseToNonCompatibleClusters(JsonNode jsonObject, List<NonCompatibleClusterReference> nonCompatibleClusterReferenceList);
  public String adaptEnterHostMaintenanceToJsonRpcFormat(EnterHostMaintenanceSpec enterHostMaintenance, String hostExtId, String pcTaskUuid);
  public String adaptExitHostMaintenanceToJsonRpcFormat(HostMaintenanceCommonSpec exitHostMaintenance, String hostExtId, String pcTaskUuid);
  public String adaptComputeNonMigratableVmsToJsonRpcFormat(ComputeNonMigratableVmsSpec computeNonMigratableVms, String pcTaskUuid);
  public NonMigratableVmsResult adaptJsonResponseToNonMigratableVms(List<InsightsInterfaceProto.MetricData> metricDataList, NonMigratableVmsResult nonMigratableVmsResult) throws ClustermgmtServiceException;
}
