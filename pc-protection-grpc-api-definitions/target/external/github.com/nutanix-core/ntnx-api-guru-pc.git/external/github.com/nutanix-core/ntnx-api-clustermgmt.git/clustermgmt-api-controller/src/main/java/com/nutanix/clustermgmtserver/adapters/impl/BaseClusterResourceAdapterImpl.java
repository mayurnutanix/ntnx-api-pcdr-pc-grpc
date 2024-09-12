package com.nutanix.clustermgmtserver.adapters.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.protobuf.ByteString;
import com.nutanix.cluster_management.ClusterManagementInterfaceProto;
import com.nutanix.clustermgmtserver.adapters.api.ClusterResourceAdapter;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceException;
import com.nutanix.clustermgmtserver.versioning_registry.VersionAwareClustermgmtAdapterRegistry;
import com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto;
import com.nutanix.insights.ifc.InsightsInterfaceProto;
import com.nutanix.prism.adapter.service.ZeusConfiguration;
import com.nutanix.stats_gateway.graphql_interface.GroupsGraphqlInterfaceProto;
import dp1.clu.clustermgmt.v4.config.*;
import dp1.clu.clustermgmt.v4.operations.*;
import dp1.clu.clustermgmt.v4.stats.ClusterStats;
import dp1.clu.clustermgmt.v4.stats.HostStats;
import dp1.clu.clustermgmt.v4.stats.TimeValuePair;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class BaseClusterResourceAdapterImpl implements ClusterResourceAdapter {
  // The next adapter implementation in the handler chain.
  @Setter
  protected ClusterResourceAdapter nextChainAdapter;

  // The version adapter registry to provide information w.r.t
  // version compatibility of the given adapter class.
  @Setter
  protected VersionAwareClustermgmtAdapterRegistry adapterRegistry;

  // An abstract method to get version corresponding to the adapter implementation.
  // The version string must be of type "<version>-storage-adapter".
  public abstract String getVersionOfAdapter();

  @Override
  public Cluster adaptZeusEntriestoClusterEntity(ZeusConfiguration zkConfig,
                                                       Cluster clusterEntity) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptZeusEntriestoClusterEntity(zkConfig, clusterEntity);
    }
    return clusterEntity;
  }

  @Override
  public Cluster adaptIdfClusterMetricstoClusterEntity(List<InsightsInterfaceProto.MetricData> metricDataList, Cluster clusterEntity) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptIdfClusterMetricstoClusterEntity(metricDataList, clusterEntity);
    }
    return clusterEntity;
  }

  @Override
  public SnmpConfig adaptZeusEntriestoSnmpInfo(ZeusConfiguration zkConfig, SnmpConfig snmpInfo) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptZeusEntriestoSnmpInfo(zkConfig, snmpInfo);
    }
    return snmpInfo;
  }

  @Override
  public List<SnmpUser> getSnmpUsers(ZeusConfiguration zkConfig) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.getSnmpUsers(zkConfig);
    }
    return new ArrayList<>();
  }

  @Override
  public List<SnmpTrap> getSnmpTraps(ZeusConfiguration zkConfig) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.getSnmpTraps(zkConfig);
    }
    return new ArrayList<>();
  }

  @Override
  public List<RsyslogServer> adaptZeusEntriestoRsyslogServer(ZeusConfiguration zkConfig) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptZeusEntriestoRsyslogServer(zkConfig);
    }
    return new ArrayList<>();
  }

  @Override
  public List<RackableUnit> adaptZeusEntriestoRackableUnits(ZeusConfiguration zkConfig) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptZeusEntriestoRackableUnits(zkConfig);
    }
    return new ArrayList<>();
  }

  @Override
  public List<DomainFaultTolerance> adaptZeusEntriestoDomainFaultTolerance(ZeusConfiguration zkConfig) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptZeusEntriestoDomainFaultTolerance(zkConfig);
    }
    return new ArrayList<>();
  }

  @Override
  public RackableUnit adaptZeusEntriestoRackableUnit(ZeusConfiguration zkConfig, String rackableUnitUuid) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptZeusEntriestoRackableUnit(zkConfig, rackableUnitUuid);
    }
    return new RackableUnit();
  }

  @Override
  public Host adaptZeusEntriestoHostEntity(ZeusConfiguration zkConfig, Host hostEntity, String hostUuid) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptZeusEntriestoHostEntity(zkConfig, hostEntity, hostUuid);
    }
    return hostEntity;
  }

  @Override
  public Host adaptIdfHostMetricstoHostEntity(List<InsightsInterfaceProto.MetricData> metricDataList, Host hostEntity) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptIdfHostMetricstoHostEntity(metricDataList, hostEntity);
    }
    return hostEntity;
  }

  @Override
  public Cluster adaptManagementServerIntoClusterEntity(List<InsightsInterfaceProto.EntityWithMetric> entitiesWithMetric, Cluster clusterEntity) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptManagementServerIntoClusterEntity(entitiesWithMetric, clusterEntity);
    }
    return clusterEntity;
  }

  @Override
  public HostGpu adaptIdfHostGpuMetricstoHostGpuEntity(List<InsightsInterfaceProto.MetricData> metricDataList, HostGpu hostGpuEntity) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptIdfHostGpuMetricstoHostGpuEntity(metricDataList, hostGpuEntity);
    }
    return hostGpuEntity;
  }

  @Override
  public PhysicalGpuProfile adaptIdfMetricsToPhysicalGpuProfile(List<InsightsInterfaceProto.MetricData> metricDataList, PhysicalGpuProfile physicalGpuProfile) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptIdfMetricsToPhysicalGpuProfile(metricDataList, physicalGpuProfile);
    }
    return physicalGpuProfile;
  }

  @Override
  public VirtualGpuProfile adaptIdfMetricsToVirtualGpuProfile(List<InsightsInterfaceProto.MetricData> metricDataList, VirtualGpuProfile virtualGpuProfile) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptIdfMetricsToVirtualGpuProfile(metricDataList, virtualGpuProfile);
    }
    return virtualGpuProfile;
  }

  @Override
  public GenesisInterfaceProto.UpdateClusterArg adaptClusterEntityToClusterUpdateArg(Cluster body,
                                                                                     GenesisInterfaceProto.UpdateClusterArg.Builder updateClusterArg,
                                                                                     ByteString parentTaskUuid) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptClusterEntityToClusterUpdateArg(body, updateClusterArg, parentTaskUuid);
    }
    return updateClusterArg.build();
  }

  @Override
  public GenesisInterfaceProto.AddSnmpTransportsArg adaptSnmpTransportsToAddTransportsArg(SnmpTransport snmpTransport,
                                                                                          GenesisInterfaceProto.AddSnmpTransportsArg.Builder builder,
                                                                                          ByteString parentTaskUuid) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptSnmpTransportsToAddTransportsArg(snmpTransport, builder, parentTaskUuid);
    }
    return builder.build();
  }

  @Override
  public GenesisInterfaceProto.RemoveSnmpTransportsArg adaptSnmpTransportsToRemoveTransportsArg(SnmpTransport snmpTransport,
                                                                                                GenesisInterfaceProto.RemoveSnmpTransportsArg.Builder builder,
                                                                                                ByteString parentTaskUuid) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptSnmpTransportsToRemoveTransportsArg(snmpTransport, builder, parentTaskUuid);
    }
    return builder.build();
  }

  @Override
  public GenesisInterfaceProto.AddSnmpUserArg adaptSnmpUserToAddSnmpUserArg(SnmpUser snmpUser,
                                                                            GenesisInterfaceProto.AddSnmpUserArg.Builder builder,
                                                                            ByteString parentTaskUuid) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptSnmpUserToAddSnmpUserArg(snmpUser, builder, parentTaskUuid);
    }
    return builder.build();
  }

  @Override
  public GenesisInterfaceProto.UpdateSnmpUserArg adaptSnmpUserToUpdateSnmpUserArg(SnmpUser snmpUser,
                                                                                  GenesisInterfaceProto.UpdateSnmpUserArg.Builder builder,
                                                                                  ByteString parentTaskUuid) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptSnmpUserToUpdateSnmpUserArg(snmpUser, builder, parentTaskUuid);
    }
    return builder.build();
  }

  @Override
  public GenesisInterfaceProto.DeleteSnmpUserArg adaptSnmpUserToDeleteSnmpUserArg(String userExtId,
                                                                                  GenesisInterfaceProto.DeleteSnmpUserArg.Builder builder,
                                                                                  ByteString parentTaskUuid) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptSnmpUserToDeleteSnmpUserArg(userExtId, builder, parentTaskUuid);
    }
    return builder.build();
  }

  @Override
  public SnmpUser adaptSnmpUserProtoToSnmpUserEntity(String userExtId, GenesisInterfaceProto.SnmpUser snmpUser) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptSnmpUserProtoToSnmpUserEntity(userExtId, snmpUser);
    }
    return new SnmpUser();
  }

  @Override
  public GenesisInterfaceProto.AddSnmpTrapArg adaptSnmpTrapToAddSnmpTrapArg(SnmpTrap snmpTrap,
                                                                            GenesisInterfaceProto.AddSnmpTrapArg.Builder builder,
                                                                            ByteString parentTaskUuid) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptSnmpTrapToAddSnmpTrapArg(snmpTrap, builder, parentTaskUuid);
    }
    return builder.build();
  }

  @Override
  public GenesisInterfaceProto.UpdateSnmpTrapArg adaptSnmpTrapToUpdateSnmpTrapArg(SnmpTrap snmpTrap,
                                                                                  GenesisInterfaceProto.UpdateSnmpTrapArg.Builder builder,
                                                                                  ByteString parentTaskUuid) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptSnmpTrapToUpdateSnmpTrapArg(snmpTrap, builder, parentTaskUuid);
    }
    return builder.build();
  }

  @Override
  public GenesisInterfaceProto.DeleteSnmpTrapArg adaptSnmpTrapToDeleteSnmpTrapArg(String trapExtId,
                                                                                  GenesisInterfaceProto.DeleteSnmpTrapArg.Builder builder,
                                                                                  ByteString parentTaskUuid) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptSnmpTrapToDeleteSnmpTrapArg(trapExtId, builder, parentTaskUuid);
    }
    return builder.build();
  }

  @Override
  public SnmpTrap adaptSnmpTrapProtoToSnmpTrapEntity(String trapExtId, GenesisInterfaceProto.SnmpTrap snmpTrap) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptSnmpTrapProtoToSnmpTrapEntity(trapExtId, snmpTrap);
    }
    return new SnmpTrap();
  }

  @Override
  public SnmpTransport adaptSnmpTransportProtoToSnmpTransportEntity(
    GenesisInterfaceProto.SnmpTransport snmpTransport) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptSnmpTransportProtoToSnmpTransportEntity(snmpTransport);
    }
    return new SnmpTransport();
  }

  @Override
  public RsyslogServer adaptRemoteSyslogConfigurationProtoToRsyslogServer(
    String rsyslogServerExtId, GenesisInterfaceProto.RemoteSyslogConfiguration remoteSyslogConfigurationProto) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptRemoteSyslogConfigurationProtoToRsyslogServer(rsyslogServerExtId, remoteSyslogConfigurationProto);
    }
    return new RsyslogServer();
  }

  @Override
  public HttpProxyConfig adaptHttpProxyConfigurationProtoToHttpProxy(String httpProxyExtId,
                                                                     GenesisInterfaceProto.HttpProxy httpProxyConfigurationProto) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptHttpProxyConfigurationProtoToHttpProxy(httpProxyExtId, httpProxyConfigurationProto);
    }
    return new HttpProxyConfig();
  }

  @Override
  public HttpProxyWhiteListConfig adaptHttpProxyWhitelistConfigurationProtoToHttpProxyWhiteList(
    String httpProxyWhiteListExtId, GenesisInterfaceProto.HttpProxyWhitelist httpProxyWhiteListConfigurationProto) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptHttpProxyWhitelistConfigurationProtoToHttpProxyWhiteList(
        httpProxyWhiteListExtId, httpProxyWhiteListConfigurationProto);
    }
    return new HttpProxyWhiteListConfig();
  }

  @Override
  public GenesisInterfaceProto.CreateRemoteSyslogServerArg adaptRsyslogServerToCreateRsyslogServerArg(RsyslogServer rsyslogServer,
                                                                                                      GenesisInterfaceProto.CreateRemoteSyslogServerArg.Builder builder,
                                                                                                      ByteString parentTaskUuid) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptRsyslogServerToCreateRsyslogServerArg(rsyslogServer, builder, parentTaskUuid);
    }
    return builder.build();
  }

  @Override
  public GenesisInterfaceProto.UpdateRemoteSyslogServerArg adaptRsyslogServerToUpdateRsyslogServerArg(RsyslogServer rsyslogServer,
                                                                                                      GenesisInterfaceProto.UpdateRemoteSyslogServerArg.Builder builder,
                                                                                                      ByteString parentTaskUuid) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptRsyslogServerToUpdateRsyslogServerArg(rsyslogServer, builder, parentTaskUuid);
    }
    return builder.build();
  }

  @Override
  public GenesisInterfaceProto.DeleteRemoteSyslogServerArg adaptRsyslogServerNameToDeleteRsyslogServerArg(String rsyslogServerExtId,
                                                                                                          GenesisInterfaceProto.DeleteRemoteSyslogServerArg.Builder builder,
                                                                                                          ByteString parentTaskUuid) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptRsyslogServerNameToDeleteRsyslogServerArg(rsyslogServerExtId, builder, parentTaskUuid);
    }
    return builder.build();
  }

  @Override
  public ConfigCredentials adaptGetConfigCredentialsRetToConfigCredentials(
    GenesisInterfaceProto.GetConfigCredentialsRet getConfigCredentialsRet, ConfigCredentials configCredentials) {

    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptGetConfigCredentialsRetToConfigCredentials(getConfigCredentialsRet, configCredentials);
    }
    return configCredentials;
  }

  @Override
  public String adaptDiscoverUnconfigureNodeToJsonRpcFormat(NodeDiscoveryParams discoverUnconfiguredNode, String pcTaskUuid) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptDiscoverUnconfigureNodeToJsonRpcFormat(discoverUnconfiguredNode, pcTaskUuid);
    }
    return null;
  }

  @Override
  public String adaptNodeNetworkingDetailsToJsonRpcFormat(NodeDetails nodesNetworkingDetails, String pcTaskUuid) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptNodeNetworkingDetailsToJsonRpcFormat(nodesNetworkingDetails, pcTaskUuid);
    }
    return null;
  }

  @Override
  public String adaptAddNodeToJsonRpcFormat(ExpandClusterParams addNode, String pcTaskUuid) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptAddNodeToJsonRpcFormat(addNode, pcTaskUuid);
    }
    return null;
  }

  @Override
  public String adaptHypervisorUploadParamToJsonRpcFormat(HypervisorUploadParam hypervisorUploadParam, String pcTaskUuid) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptHypervisorUploadParamToJsonRpcFormat(hypervisorUploadParam, pcTaskUuid);
    }
    return null;
  }

  @Override
  public String adaptIsBundleCompatibleToJsonRpcFormat(BundleParam validateBundleParam, String pcTaskUuid) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptIsBundleCompatibleToJsonRpcFormat(validateBundleParam, pcTaskUuid);
    }
    return null;
  }

  @Override
  public String adaptRemoveNodeToJsonRpcFormat(NodeRemovalParams removeNode, String pcTaskUuid) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptRemoveNodeToJsonRpcFormat(removeNode, pcTaskUuid);
    }
    return null;
  }

  @Override
  public String adaptValidateUplinkToJsonRpcFormat(List<UplinkNode> nodeItems, String pcTaskUuid) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptValidateUplinkToJsonRpcFormat(nodeItems, pcTaskUuid);
    }
    return null;
  }

  @Override
  public UnconfigureNodeDetails adaptJsonResponsetoUnconfiguredNodes(JsonNode jsonObject, UnconfigureNodeDetails unconfigureNodesResponse) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptJsonResponsetoUnconfiguredNodes(jsonObject, unconfigureNodesResponse);
    }
    return null;
  }

  @Override
  public NodeNetworkingDetails adaptJsonResponsetoNetworkingDetails(JsonNode jsonObject, NodeNetworkingDetails networkingDetailResponsee) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptJsonResponsetoNetworkingDetails(jsonObject, networkingDetailResponsee);
    }
    return null;
  }

  @Override
  public HypervisorUploadInfo adaptJsonResponsetoHypervisorIso(JsonNode jsonObject, HypervisorUploadInfo hypervisorUploadInfoResponse) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptJsonResponsetoHypervisorIso(jsonObject, hypervisorUploadInfoResponse);
    }
    return null;
  }

  @Override
  public HostNic adaptIdfHostNicMetricstoHostNicEntity(List<InsightsInterfaceProto.MetricData> metricDataList, HostNic hostNicEntity) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptIdfHostNicMetricstoHostNicEntity(metricDataList, hostNicEntity);
    }
    return hostNicEntity;
  }

  @Override
  public NetworkSwitchInterface adaptIdfSwitchIntfMetricstoNetworkSwitchInterfaceEntity(List<InsightsInterfaceProto.MetricData> metricDataList, NetworkSwitchInterface switchIntfEntity) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptIdfSwitchIntfMetricstoNetworkSwitchInterfaceEntity(metricDataList, switchIntfEntity);
    }
    return switchIntfEntity;
  }

  @Override
  public VirtualNic adaptIdfVirtualNicMetricstoVirtualNicEntity(List<InsightsInterfaceProto.MetricData> metricDataList, VirtualNic virtualNicEntity) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptIdfVirtualNicMetricstoVirtualNicEntity(metricDataList, virtualNicEntity);
    }
    return virtualNicEntity;
  }

  @Override
  public ClusterStats adaptToClusterStats(ClusterStats clusterStatsResp, Map<String, List<TimeValuePair>> statsAttributes) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptToClusterStats(clusterStatsResp, statsAttributes);
    }
    return clusterStatsResp;
  }

  @Override
  public HostStats adaptToHostStats(HostStats hostStatsResp, Map<String, List<TimeValuePair>> statsAttributes) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptToHostStats(hostStatsResp, statsAttributes);
    }
    return hostStatsResp;
  }

  @Override
  public ClusterManagementInterfaceProto.ClusterCreateArg adaptClusterCreateParamsToClusterCreateArg(Cluster body, ClusterManagementInterfaceProto.ClusterCreateArg.Builder clusterCreateArg) {
    if(adapterRegistry.canForwardToNextHandler(this.getClass())){
      return nextChainAdapter.adaptClusterCreateParamsToClusterCreateArg(body, clusterCreateArg);
    }
    return clusterCreateArg.build();
  }

  @Override
  public GenesisInterfaceProto.UpdateCategoryAssociationsArg adaptUpdateCategoriesParamsToUpdateCategoryAssociationsArg(
    Set<String> categoryUuids, String entityType, String entityUuid, String operationType,
    GenesisInterfaceProto.UpdateCategoryAssociationsArg.Builder updateCategoryAssociationsArg) {
    if(adapterRegistry.canForwardToNextHandler(this.getClass())){
      return nextChainAdapter.adaptUpdateCategoriesParamsToUpdateCategoryAssociationsArg(categoryUuids, entityType, entityUuid, operationType, updateCategoryAssociationsArg);
    }
    return updateCategoryAssociationsArg.build();
  }

  @Override
  public Cluster adaptCategoriesToClusterEntity(List<InsightsInterfaceProto.MetricData> metricDataList, Cluster cluster) {
    if(adapterRegistry.canForwardToNextHandler(this.getClass())){
      return nextChainAdapter.adaptCategoriesToClusterEntity(metricDataList, cluster);
    }
    return cluster;
  }

  @Override
  public List<Cluster> ProcessStatsResponseMapToClusters(
    List<Cluster> clusters, Map statsResponseMap) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.ProcessStatsResponseMapToClusters(clusters, statsResponseMap);
    }
    return clusters;
  }

  @Override
  public Cluster adaptStatsGatewayResponseToClusters(String attr, Object value, Cluster cluster) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptStatsGatewayResponseToClusters(attr, value, cluster);
    }
    return cluster;
  }

  @Override
  public Cluster adaptStatsGatewayResponseToClusterProfileExpand(Object value, Cluster cluster) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptStatsGatewayResponseToClusterProfileExpand(value, cluster);
    }
    return cluster;
  }

  @Override
  public Cluster adaptStatsGatewayResponseToStorageSummaryExpand(Object value, Cluster cluster) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptStatsGatewayResponseToStorageSummaryExpand(value, cluster);
    }
    return cluster;
  }

  @Override
  public List<NonCompatibleClusterReference> adaptJsonResponseToNonCompatibleClusters(JsonNode jsonObject, List<NonCompatibleClusterReference> nonCompatibleClusterReferenceList) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptJsonResponseToNonCompatibleClusters(jsonObject, nonCompatibleClusterReferenceList);
    }
    return nonCompatibleClusterReferenceList;
  }

  @Override
  public String adaptEnterHostMaintenanceToJsonRpcFormat(EnterHostMaintenanceSpec enterHostMaintenance, String hostExtId, String pcTaskUuid) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptEnterHostMaintenanceToJsonRpcFormat(enterHostMaintenance, hostExtId, pcTaskUuid);
    }
    return null;
  }

  @Override
  public String adaptExitHostMaintenanceToJsonRpcFormat(HostMaintenanceCommonSpec exitHostMaintenance, String hostExtId, String pcTaskUuid) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptExitHostMaintenanceToJsonRpcFormat(exitHostMaintenance, hostExtId, pcTaskUuid);
    }
    return null;
  }

  @Override
  public String adaptComputeNonMigratableVmsToJsonRpcFormat(ComputeNonMigratableVmsSpec computeNonMigratableVms, String pcTaskUuid) {
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptComputeNonMigratableVmsToJsonRpcFormat(computeNonMigratableVms, pcTaskUuid);
    }
    return null;
  }

  @Override
  public NonMigratableVmsResult adaptJsonResponseToNonMigratableVms(List<InsightsInterfaceProto.MetricData> metricDataList, NonMigratableVmsResult nonMigratableVmsResult)
    throws ClustermgmtServiceException{
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptJsonResponseToNonMigratableVms(metricDataList, nonMigratableVmsResult);
    }
    return nonMigratableVmsResult;
  }
}
