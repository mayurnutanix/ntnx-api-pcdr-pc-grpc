package com.nutanix.clustermgmtserver.adapters.impl.v4.r0.b2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.protobuf.ByteString;
import com.nutanix.api.utils.json.JsonUtils;
import com.nutanix.cluster_management.ClusterManagementInterfaceProto;
import com.nutanix.clustermgmtserver.adapters.impl.BaseClusterResourceAdapterImpl;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceException;
import com.nutanix.clustermgmtserver.utils.ClustermgmtUtils;
import com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto;
import com.nutanix.insights.ifc.InsightsInterfaceProto;
import com.nutanix.prism.adapter.service.ZeusConfiguration;
import com.nutanix.zeus.protobuf.Configuration.ConfigurationProto;
import com.nutanix.zeus.protobuf.Configuration.ConfigurationProto.FaultToleranceState.PrepareForDesiredFTAck;
import dp1.clu.clustermgmt.v4.config.*;
import dp1.clu.clustermgmt.v4.operations.*;
import dp1.clu.clustermgmt.v4.stats.ClusterStats;
import dp1.clu.clustermgmt.v4.stats.HostStats;
import dp1.clu.clustermgmt.v4.stats.TimeValuePair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component(com.nutanix.clustermgmtserver.adapters.impl.v4.r0.b2.ClusterResourceAdapterImpl.ADAPTER_VERSION)
@Slf4j
public class ClusterResourceAdapterImpl extends BaseClusterResourceAdapterImpl {
  public static final String ADAPTER_VERSION = "v4.0.b2-clustermgmt-adapter";
  private final ObjectMapper mapper = JsonUtils.getObjectMapper();

  @Override
  public String getVersionOfAdapter() {
    return ADAPTER_VERSION;
  }

  @Override
  public PhysicalGpuProfile adaptIdfMetricsToPhysicalGpuProfile(List<InsightsInterfaceProto.MetricData> metricDataList, PhysicalGpuProfile physicalGpuProfile) {
    for (final InsightsInterfaceProto.MetricData metricData : metricDataList) {
      if (!metricData.hasName() || metricData.getValueListCount() <= 0) {
        continue;
      }
      switch (metricData.getName()) {
        case "vm_uuid_list":
          List<String> vmUuidList = new ArrayList<>();
          for (String vmUuid : metricData.getValueList(0).getValue().getStrList().getValueListList()) {
            vmUuidList.add(vmUuid);
          }
          physicalGpuProfile.setAllocatedVmExtIds(vmUuidList);
        default:
          break;
      }
    }
    PhysicalGpuConfig gpuConfig = getPhysicalGpuConfigForMetricData(metricDataList);
    physicalGpuProfile.setPhysicalGpuConfig(gpuConfig);

    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptIdfMetricsToPhysicalGpuProfile(metricDataList, physicalGpuProfile);
    }

    return physicalGpuProfile;
  }

  public PhysicalGpuConfig getPhysicalGpuConfigForMetricData(List<InsightsInterfaceProto.MetricData> metricDataList) {
    PhysicalGpuConfig gpuConfig = new PhysicalGpuConfig();
    for (final InsightsInterfaceProto.MetricData metricData : metricDataList) {
      if (!metricData.hasName() || metricData.getValueListCount() <= 0) {
        continue;
      }
      switch (metricData.getName()) {
        case "gpu_type":
          gpuConfig.setType(ClustermgmtUtils.gpuTypeMap.get(metricData.getValueList(0).getValue().getStrValue()));
          break;
        case "gpu_mode":
          gpuConfig.setMode(ClustermgmtUtils.gpuModeMap.get(metricData.getValueList(0).getValue().getStrValue()));
          break;
        case "vendor_name":
          gpuConfig.setVendorName(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "device_id":
          gpuConfig.setDeviceId(metricData.getValueList(0).getValue().getInt64Value());
          break;
        case "device_name":
          gpuConfig.setDeviceName(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "sbdf":
          gpuConfig.setSbdf(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "in_use":
          gpuConfig.setInUse(metricData.getValueList(0).getValue().getInt64Value() != 0);
          break;
        case "assignable":
          gpuConfig.setAssignable(metricData.getValueList(0).getValue().getInt64Value());
          break;
        case "numa_node":
          gpuConfig.setNumaNode(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "frame_buffer_size_bytes":
          gpuConfig.setFrameBufferSizeBytes(metricData.getValueList(0).getValue().getInt64Value());
          break;
        default:
          break;
      }
    }
    return gpuConfig;
  }

  @Override
  public VirtualGpuProfile adaptIdfMetricsToVirtualGpuProfile(List<InsightsInterfaceProto.MetricData> metricDataList, VirtualGpuProfile virtualGpuProfile) {
    for (final InsightsInterfaceProto.MetricData metricData : metricDataList) {
      if (!metricData.hasName() || metricData.getValueListCount() <= 0) {
        continue;
      }
      switch (metricData.getName()) {
        case "vm_uuid_list":
          List<String> vmUuidList = new ArrayList<>();
          for (String vmUuid : metricData.getValueList(0).getValue().getStrList().getValueListList()) {
            vmUuidList.add(vmUuid);
          }
          virtualGpuProfile.setAllocatedVmExtIds(vmUuidList);
        default:
          break;
      }
    }
    VirtualGpuConfig gpuConfig = getVirtualGpuConfigForMetricData(metricDataList);
    virtualGpuProfile.setVirtualGpuConfig(gpuConfig);

    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptIdfMetricsToVirtualGpuProfile(metricDataList, virtualGpuProfile);
    }

    return virtualGpuProfile;
  }

  public VirtualGpuConfig getVirtualGpuConfigForMetricData(List<InsightsInterfaceProto.MetricData> metricDataList) {
    VirtualGpuConfig gpuConfig = new VirtualGpuConfig();
    for (final InsightsInterfaceProto.MetricData metricData : metricDataList) {
      if (!metricData.hasName() || metricData.getValueListCount() <= 0) {
        continue;
      }
      switch (metricData.getName()) {
        case "gpu_type":
          gpuConfig.setType(ClustermgmtUtils.gpuTypeMap.get(metricData.getValueList(0).getValue().getStrValue()));
          break;
        case "vendor_name":
          gpuConfig.setVendorName(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "device_id":
          gpuConfig.setDeviceId(metricData.getValueList(0).getValue().getInt64Value());
          break;
        case "device_name":
          gpuConfig.setDeviceName(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "sbdf":
          gpuConfig.setSbdf(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "in_use":
          gpuConfig.setInUse(metricData.getValueList(0).getValue().getInt64Value() != 0);
          break;
        case "assignable":
          gpuConfig.setAssignable(metricData.getValueList(0).getValue().getInt64Value());
          break;
        case "numa_node":
          gpuConfig.setNumaNode(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "guest_driver_version":
          gpuConfig.setGuestDriverVersion(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "fraction":
          gpuConfig.setFraction(metricData.getValueList(0).getValue().getInt64Value());
          break;
        case "license_list":
          List<String> licenseList = new ArrayList<>();
          for (String license : metricData.getValueList(0).getValue().getStrList().getValueListList()) {
            licenseList.add(license);
          }
          gpuConfig.setLicenses(licenseList);
          break;
        case "num_virtual_display_heads":
          gpuConfig.setNumberOfVirtualDisplayHeads(metricData.getValueList(0).getValue().getInt64Value());
          break;
        case "max_resolution":
          gpuConfig.setMaxResolution(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "frame_buffer_size_bytes":
          gpuConfig.setFrameBufferSizeBytes(metricData.getValueList(0).getValue().getInt64Value());
          break;
        default:
          break;
      }
    }
    return gpuConfig;
  }

  @Override
  public HostGpu adaptIdfHostGpuMetricstoHostGpuEntity(List<InsightsInterfaceProto.MetricData> metricDataList,
                                                             HostGpu hostGpuEntity) {
    ClusterReference clusterReference = new ClusterReference();
    for (final InsightsInterfaceProto.MetricData metricData : metricDataList) {
      if (!metricData.hasName() || metricData.getValueListCount() <= 0) {
        continue;
      }
      switch (metricData.getName()) {
        case "node":
          hostGpuEntity.setNodeUuid(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "cluster":
          clusterReference.setUuid(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "cluster_name":
          clusterReference.setName(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "host_id":
          hostGpuEntity.setNodeId(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "num_vgpus_allocated":
          hostGpuEntity.setNumberOfVgpusAllocated(metricData.getValueList(0).getValue().getInt64Value());
          break;
        default:
          break;
      }
    }
    hostGpuEntity.setCluster(clusterReference);
    hostGpuEntity.setConfig(getVirtualGpuConfigForMetricData(metricDataList));

    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptIdfHostGpuMetricstoHostGpuEntity(metricDataList, hostGpuEntity);
    }

    return hostGpuEntity;
  }

  @Override
  public Cluster adaptCategoriesToClusterEntity(List<InsightsInterfaceProto.MetricData> metricDataList, Cluster cluster) {
    log.debug("Request to adapt categories IDF response to categories attr schema");
    String clusterUuid = cluster.getExtId();
    for (final InsightsInterfaceProto.MetricData metricData : metricDataList) {
      log.debug("Value List Count for categories metric data attr: {}", String.valueOf(metricData.getValueListCount()));
      if(!metricData.hasName() || metricData.getValueListCount() <= 0){
        continue;
      }
      else if(metricData.getName().equals("category_id_list")){
        if(metricData.getValueListCount() <= 0){
          log.debug("No categories attached to cluster with uuid: "+clusterUuid);
          return cluster;
        }
        List<String> categories = new ArrayList<>();
        for(String category: metricData.getValueList(0).getValue().getStrList().getValueListList()){
          categories.add(category);
        }
        if(!categories.isEmpty()) {
          log.debug("Categories attached to cluster uuid:" + clusterUuid + "\n");
          log.debug(categories.toString());
          cluster.setCategories(categories);
        }
      }
    }
    return cluster;
  }

  @Override
  public Cluster adaptIdfClusterMetricstoClusterEntity(final List<InsightsInterfaceProto.MetricData> metricDataList,
                                                       Cluster clusterEntity) {
    for(final InsightsInterfaceProto.MetricData metricData : metricDataList) {
      if(!metricData.hasName() || metricData.getValueListCount() <= 0) {
        continue;
      }
      switch(metricData.getName()) {
        case "cluster_profile_uuid":
          clusterEntity.setClusterProfileExtId(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "is_available":
          clusterEntity.getConfig().setIsAvailable(metricData.getValueList(0).getValue().getBoolValue());
          break;
        case "backup_eligibility_score":
          clusterEntity.setBackupEligibilityScore(metricData.getValueList(0).getValue().getInt64Value());
          break;
      }
    }

    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptIdfClusterMetricstoClusterEntity(metricDataList, clusterEntity);
    }
    return clusterEntity;
  }

  @Override
  public ConfigCredentials adaptGetConfigCredentialsRetToConfigCredentials(
    GenesisInterfaceProto.GetConfigCredentialsRet getConfigCredentialsRet, ConfigCredentials configCredentials) {
    if (getConfigCredentialsRet.hasSmtpCredentials()) {
      BasicAuthenticationCredentials smtpCredentials = new BasicAuthenticationCredentials();
      smtpCredentials.setUsername(getConfigCredentialsRet.getSmtpCredentials().getUsername());
      smtpCredentials.setPassword(getConfigCredentialsRet.getSmtpCredentials().getPassword());
      configCredentials.setSmtp(smtpCredentials);
    }

    List<SnmpUserCredentials> snmpUserCredentialsList = new ArrayList<>();
    for (GenesisInterfaceProto.GetConfigCredentialsRet.SnmpUserCred snmpCredentials : getConfigCredentialsRet.getSnmpUserCredentialsList()) {
      SnmpUserCredentials snmpCred = new SnmpUserCredentials();
      snmpCred.setUsername(snmpCredentials.getUsername());
      snmpCred.setAuthKey(snmpCredentials.getAuthKey());
      snmpCred.setPrivKey(snmpCredentials.getPrivKey());

      snmpUserCredentialsList.add(snmpCred);
    }
    configCredentials.setSnmp(snmpUserCredentialsList);

    if (getConfigCredentialsRet.hasHttpProxyCredentials()) {
      BasicAuthenticationCredentials httpProxyCredentials = new BasicAuthenticationCredentials();
      httpProxyCredentials.setUsername(getConfigCredentialsRet.getHttpProxyCredentials().getUsername());
      httpProxyCredentials.setPassword(getConfigCredentialsRet.getHttpProxyCredentials().getPassword());
      configCredentials.setHttpProxy(httpProxyCredentials);
    }

    return configCredentials;
  }

  public List<Cluster> ProcessStatsResponseMapToClusters(List<Cluster> clusters, Map statsResponseMap) {
    if (statsResponseMap == null || statsResponseMap.get(ClustermgmtUtils.CLUSTER_ENTITY) == null) {
      log.error("Stats gateway response doesn't contain the key cluster.");
      return clusters;
    }
    Object value = statsResponseMap.get(ClustermgmtUtils.CLUSTER_ENTITY);
    // For single entry, value is of type Map and for multiple entries it's List
    if (value == null || !((value instanceof Map) || (value instanceof List))) {
      log.error("Stats gateway response doesn't contain the cluster table information.");
      return clusters;
    }

    Map<String, Cluster> clusterMap = new HashMap<>();
    Map<String, Integer> clusterIndexMap = new HashMap<>();
    Integer index = 0;
    for (Cluster cluster : clusters) {
      clusterMap.put(cluster.getExtId(), cluster);
      clusterIndexMap.put(cluster.getExtId(), index++);
    }

    if (value instanceof Map) {
      Cluster cluster;
      Map<String, Object> attrMap = (Map) value;
      // To fetch the original cluster with all the attributes except expand attributes populated.
      if (attrMap.containsKey(ClustermgmtUtils.CLUSTER_UUID_ATTRIBUTE)) {
        Object val = attrMap.get(ClustermgmtUtils.CLUSTER_UUID_ATTRIBUTE);
        cluster = clusterMap.get(((List) val).get(0).toString());
        if (cluster == null) {
          log.warn("cluster {} doesn't exist in map", ((List) val).get(0).toString());
          return clusters;
        }
        for (Map.Entry<String, Object> entry : attrMap.entrySet()) {
          cluster = adaptStatsGatewayResponseToClusters(entry.getKey(), entry.getValue(), cluster);
        }
        // Update the cluster object with the expanded properties.
        clusters.set(clusterIndexMap.get(cluster.getExtId()), cluster);
      }
      else {
        log.error("Stats gateway response doesn't contain the Cluster UUID attribute.");
      }
    }
    else {
      // List of maps for multiple Clusters.
      ((List) value).forEach(attrMap -> {
        Cluster cluster;
        // To fetch the original cluster with all the attributes except expand attributes populated.
        if (((Map<String, Object>) attrMap).containsKey(ClustermgmtUtils.CLUSTER_UUID_ATTRIBUTE)) {
          Object val = ((Map<String, Object>) attrMap).get(ClustermgmtUtils.CLUSTER_UUID_ATTRIBUTE);
          cluster = clusterMap.get(((List) val).get(0).toString());
          if (cluster == null) {
            log.warn("cluster {} doesn't exist in map", ((List) val).get(0).toString());
            return;
          }
          for (Map.Entry<String, Object> entry : ((Map<String, Object>) attrMap).entrySet()) {
            cluster = adaptStatsGatewayResponseToClusters(entry.getKey(), entry.getValue(), cluster);
          }
          // Update the cluster object with the expanded properties.
          clusters.set(clusterIndexMap.get(cluster.getExtId()), cluster);
        }
        else {
          log.error("Stats gateway response doesn't contain the Cluster UUID attribute.");
        }
      });
    }
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.ProcessStatsResponseMapToClusters(clusters, statsResponseMap);
    }
    return clusters;
  }

  @Override
  public Cluster adaptStatsGatewayResponseToClusters(final String attr, final Object value,
                                                     Cluster cluster) {
    if (value == null) {
      return cluster;
    } else if (attr.equals(ClustermgmtUtils.CLUSTER_PROFILE_ENTITY)) {
      cluster = adaptStatsGatewayResponseToClusterProfileExpand(value, cluster);
    } else if (attr.equals(ClustermgmtUtils.STORAGE_SUMMARY_ENTITY)) {
      cluster = adaptStatsGatewayResponseToStorageSummaryExpand(value, cluster);
    }

    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptStatsGatewayResponseToClusters(attr, value, cluster);
    }
    return cluster;
  }

  @Override
  public Cluster adaptStatsGatewayResponseToClusterProfileExpand(final Object value, Cluster cluster) {
    Map<String, Object> attrMap = (Map) value;
    ClusterProjection clusterProjection = (ClusterProjection) cluster;
    ClusterProfileProjection clusterProfileProjection =
      new ClusterProfileProjection();

    if(attrMap.containsKey(ClustermgmtUtils.CLUSTER_PROFILE_NAME) &&
      attrMap.get(ClustermgmtUtils.CLUSTER_PROFILE_NAME) != null) {
      clusterProfileProjection.setName(((List) attrMap.get(ClustermgmtUtils.CLUSTER_PROFILE_NAME)).get(0)
        .toString());
    }
    clusterProjection.setClusterProfileProjection(clusterProfileProjection);

    if(adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptStatsGatewayResponseToClusterProfileExpand(value, cluster);
    }
    return cluster;
  }

  @Override
  public Cluster adaptStatsGatewayResponseToStorageSummaryExpand(final Object value, Cluster cluster) {
    Map<String, Object> attrMap = (Map) value;
    ClusterProjection clusterProjection = (ClusterProjection) cluster;
    StorageSummaryProjection storageSummaryProjection =
            new StorageSummaryProjection();

    if (attrMap.containsKey(ClustermgmtUtils.CLUSTER_FAULT_TOLERANT_CAPACITY) &&
            attrMap.get(ClustermgmtUtils.CLUSTER_FAULT_TOLERANT_CAPACITY) != null) {
      log.debug("Cluster fault tolerant capacity {}", attrMap.get(ClustermgmtUtils.CLUSTER_FAULT_TOLERANT_CAPACITY).toString());

      storageSummaryProjection.setClusterFaultTolerantCapacity(Integer.parseInt((String) ((List<?>) attrMap.get(ClustermgmtUtils.CLUSTER_FAULT_TOLERANT_CAPACITY)).get(0)));
    }
    clusterProjection.setStorageSummaryProjection(storageSummaryProjection);

    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptStatsGatewayResponseToStorageSummaryExpand(value, cluster);
    }
    return cluster;
  }

  @Override
  public List<NonCompatibleClusterReference> adaptJsonResponseToNonCompatibleClusters(JsonNode jsonObject, List<NonCompatibleClusterReference> nonCompatibleClusterReferenceList) {
    jsonObject.fields().forEachRemaining(entry -> {
      NonCompatibleClusterReference nonCompliantClusterReference = new NonCompatibleClusterReference();
      List<ConfigType> settings = new ArrayList<>();
      entry.getValue().forEach(valueNode -> settings.add(ConfigType.fromString(valueNode.asText())));
      nonCompliantClusterReference.setClusterExtId(entry.getKey());
      nonCompliantClusterReference.setConfigDrifts(settings);
      nonCompatibleClusterReferenceList.add(nonCompliantClusterReference);
    });
    if(adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptJsonResponseToNonCompatibleClusters(jsonObject, nonCompatibleClusterReferenceList);
    }
    return nonCompatibleClusterReferenceList;
  }

  @Override
  public Cluster adaptZeusEntriestoClusterEntity(ZeusConfiguration zkConfig, Cluster clusterEntity) {

    final ConfigurationProto configuration = zkConfig.getConfiguration();

    FaultToleranceState faultToleranceState;
    if (clusterEntity.getConfig().getFaultToleranceState() != null) {
      faultToleranceState = clusterEntity.getConfig().getFaultToleranceState();
    } else {
      faultToleranceState = new FaultToleranceState();
    }

    // Set Cluster Fault tolerance
    if(configuration.hasClusterFaultToleranceState()) {
      if (zkConfig.getFaultToleranceState().hasDesiredReplicaPlacementPolicyId()) {
        faultToleranceState.setDesiredClusterFaultTolerance(
                ClustermgmtUtils.ClusterFaultToleranceMap.get(
                        zkConfig.getFaultToleranceState().getDesiredReplicaPlacementPolicyId()));
      }
      if (zkConfig.getFaultToleranceState().hasCurrentReplicaPlacementPolicyId()) {
        faultToleranceState.setCurrentClusterFaultTolerance(
                ClustermgmtUtils.ClusterFaultToleranceMap.get(
                        zkConfig.getFaultToleranceState().getCurrentReplicaPlacementPolicyId()));
      }
    }
    if (zkConfig.getFaultToleranceState().hasPrepareFtAck()) {
      final Integer prepartFtAck = zkConfig.getFaultToleranceState().getPrepareFtAck();
      log.debug("ftprepareAck = " + String.valueOf(prepartFtAck));
      RedundancyStatusDetails redundancystataus;
      if (faultToleranceState.getRedundancyStatus() != null) {
        redundancystataus = faultToleranceState.getRedundancyStatus();
      } else {
        redundancystataus = new RedundancyStatusDetails();
      }

      if ((prepartFtAck & PrepareForDesiredFTAck.kZookeeperPrepareDone_VALUE) == 0) {
        redundancystataus.setIsZookeeperPreparationDone(false);
      } else {
        redundancystataus.setIsZookeeperPreparationDone(true);
      }
      if ((prepartFtAck & PrepareForDesiredFTAck.kCassandraPrepareDone_VALUE) == 0) {
        redundancystataus.setIsCassandraPreparationDone(false);
      } else {
        redundancystataus.setIsCassandraPreparationDone(true);
      }
      faultToleranceState.setRedundancyStatus(redundancystataus);
    }
    clusterEntity.getConfig().setFaultToleranceState(faultToleranceState);

    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptZeusEntriestoClusterEntity(zkConfig, clusterEntity);
    }

    return clusterEntity;
  }

  @Override
  public GenesisInterfaceProto.UpdateClusterArg adaptClusterEntityToClusterUpdateArg(Cluster body,
                                                                                     GenesisInterfaceProto.UpdateClusterArg.Builder updateClusterArg,
                                                                                     ByteString parentTaskUuid) {
    if (body.getConfig() != null) {
      ClusterConfigReference config = body.getConfig();

      // Update cluster Fault Tolerance
      if (config.getFaultToleranceState() != null
              && config.getFaultToleranceState().getDesiredClusterFaultTolerance() != null) {
        updateClusterArg.setClusterFaultToleranceId(
                ClustermgmtUtils.getKey(ClustermgmtUtils.ClusterFaultToleranceMap,
                        config.getFaultToleranceState().getDesiredClusterFaultTolerance()));
      }
    }

    // cluster network
    if (body.getNetwork() != null) {
      ClusterNetworkReference network = body.getNetwork();

      // HTTP Proxy server.
      if (network.getHttpProxyList() != null) {
        List<GenesisInterfaceProto.HttpProxy> httpProxyServers = new ArrayList<>();
        for (HttpProxyConfig httpProxyServer : network.getHttpProxyList()) {
          GenesisInterfaceProto.HttpProxy.Builder httpProxyServerBuilder =
            GenesisInterfaceProto.HttpProxy.newBuilder();
          httpProxyServerBuilder.setAddress(ClustermgmtUtils.getAddress(httpProxyServer.getIpAddress()));
          if (httpProxyServer.getPort() != null) {
            httpProxyServerBuilder.setPort(httpProxyServer.getPort());
          }
          if (httpProxyServer.getUsername() != null) {
            httpProxyServerBuilder.setUsername(httpProxyServer.getUsername());
          }
          if (httpProxyServer.getPassword() != null) {
            httpProxyServerBuilder.setPassword(httpProxyServer.getPassword());
          }
          if (httpProxyServer.getName() != null) {
            httpProxyServerBuilder.setName(httpProxyServer.getName());
          }
          if (httpProxyServer.getProxyTypes() != null) {
            for (HttpProxyType proxyType: httpProxyServer.getProxyTypes()) {
              String proxyTypeString = ClustermgmtUtils.getKey(ClustermgmtUtils.httpProxyTypeStrMap, proxyType);
              httpProxyServerBuilder.addProxyType(proxyTypeString);
            }
          }
          httpProxyServers.add(httpProxyServerBuilder.build());
        }
        updateClusterArg.setHttpProxyList(GenesisInterfaceProto.UpdateClusterArg.http_proxy_msg.newBuilder().
          addAllHttpProxyList(httpProxyServers).build());
      }

      // Http Proxy Whitelist.
      if (network.getHttpProxyWhiteList() != null) {
        log.debug("Http Proxy Whitelist: {}", network.getHttpProxyWhiteList());
        List<GenesisInterfaceProto.HttpProxyWhitelist> httpProxyWhiteLists = new ArrayList<>();
        for (HttpProxyWhiteListConfig httpProxyWhiteList : network.getHttpProxyWhiteList()) {
          GenesisInterfaceProto.HttpProxyWhitelist.Builder httpProxyWhiteListBuilder =
            GenesisInterfaceProto.HttpProxyWhitelist.newBuilder();
          if (httpProxyWhiteList.getTarget() != null) {
            httpProxyWhiteListBuilder.setTarget(httpProxyWhiteList.getTarget());
          }
          if (httpProxyWhiteList.getTargetType() != null) {
            String targetType = ClustermgmtUtils.getKey(ClustermgmtUtils.httpProxyWhiteListTargetTypeStrMap, httpProxyWhiteList.getTargetType());
            httpProxyWhiteListBuilder.setTargetType(targetType);
          }
          httpProxyWhiteLists.add(httpProxyWhiteListBuilder.build());
        }
        updateClusterArg.setHttpProxyWhitelist(GenesisInterfaceProto.UpdateClusterArg.http_proxy_whitelist_msg.newBuilder().
          addAllHttpProxyWhitelist(httpProxyWhiteLists).build());
      }

    }

    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptClusterEntityToClusterUpdateArg(body, updateClusterArg, parentTaskUuid);
    }

    return updateClusterArg.build();
  }

  @Override
  public ClusterManagementInterfaceProto.ClusterCreateArg adaptClusterCreateParamsToClusterCreateArg(Cluster body,
                                                                                                     ClusterManagementInterfaceProto.ClusterCreateArg.Builder clusterCreateArg) {
    if (body.getConfig() != null) {
      ClusterConfigReference config = body.getConfig();

      // Update cluster Fault Tolerance
      if (config.getFaultToleranceState() != null
              && config.getFaultToleranceState().getDesiredClusterFaultTolerance() != null) {
        clusterCreateArg.setClusterFaultToleranceId(
                ClustermgmtUtils.getKey(ClustermgmtUtils.ClusterFaultToleranceMap,
                        config.getFaultToleranceState().getDesiredClusterFaultTolerance()));
      }
    }
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptClusterCreateParamsToClusterCreateArg(body, clusterCreateArg);
    }

    return clusterCreateArg.build();
  }
  
  @Override
  public ClusterStats adaptToClusterStats(ClusterStats clusterStatsResp, Map<String, List<TimeValuePair>> statsAttributes) {
    for (Map.Entry<String, List<TimeValuePair>> attribute : statsAttributes.entrySet()) {
      if (attribute.getValue() == null) continue;
      switch (attribute.getKey()) {
        case "storage.recycle_bin_usage_bytes":
          clusterStatsResp.setRecycleBinUsageBytes(attribute.getValue());
          break;
        case "storage.snapshot_reclaimable_bytes":
          clusterStatsResp.setSnapshotCapacityBytes(attribute.getValue());
          break;
        case "data_reduction.saved_bytes":
          clusterStatsResp.setOverallSavingsBytes(attribute.getValue());
          break;
        case "data_reduction.overall.saving_ratio_ppm":
          clusterStatsResp.setOverallSavingsRatio(attribute.getValue());
          break;
        case "capacity.cpu_capacity_hz":
          clusterStatsResp.setCpuCapacityHz(attribute.getValue());
          break;
        case "capacity.cpu_usage_hz":
          clusterStatsResp.setCpuUsageHz(attribute.getValue());
          break;
        case "capacity.memory_capacity_bytes":
          clusterStatsResp.setMemoryCapacityBytes(attribute.getValue());
          break;
        default:
          break;
      }
    }
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptToClusterStats(clusterStatsResp, statsAttributes);
    }
    return clusterStatsResp;
  }


  @Override
  public String adaptEnterHostMaintenanceToJsonRpcFormat(EnterHostMaintenanceSpec enterHostMaintenance, String hostExtId, String pcTaskUuid) {
    final ObjectNode params = JsonUtils.getObjectNode();
    if (enterHostMaintenance.getShouldAcquireShutdownToken() != null) {
      params.put("acquire_shutdown_token", enterHostMaintenance.getShouldAcquireShutdownToken());
    }
    if (enterHostMaintenance.getShouldRollbackOnFailure() != null) {
      params.put("rollback_on_failure", enterHostMaintenance.getShouldRollbackOnFailure());
    }
    if (enterHostMaintenance.getShouldShutdownNonMigratableUvms() != null) {
      params.put("shutdown_pinned_uvms", enterHostMaintenance.getShouldShutdownNonMigratableUvms());
    }
    if (enterHostMaintenance.getVcenterInfo() != null) {
      final ObjectNode hypervisorParams = JsonUtils.getObjectNode();
      final ObjectNode vcenterInfo = JsonUtils.getObjectNode();
      if (enterHostMaintenance.getVcenterInfo().getAddress() != null) {
        if (enterHostMaintenance.getVcenterInfo().getAddress().hasIpv4())
          vcenterInfo.put("vcenter_ip", enterHostMaintenance.getVcenterInfo().getAddress().getIpv4().getValue());
        else if (enterHostMaintenance.getVcenterInfo().getAddress().hasIpv6())
          vcenterInfo.put("vcenter_ip", enterHostMaintenance.getVcenterInfo().getAddress().getIpv6().getValue());
        else if (enterHostMaintenance.getVcenterInfo().getAddress().hasFqdn())
          vcenterInfo.put("vcenter_ip", enterHostMaintenance.getVcenterInfo().getAddress().getFqdn().getValue());
      }
      vcenterInfo.put("username", enterHostMaintenance.getVcenterInfo().getCredentials().getUsername());
      vcenterInfo.put("password", enterHostMaintenance.getVcenterInfo().getCredentials().getPassword());
      hypervisorParams.set("esx_params", vcenterInfo);
      params.set("hypervisor_params", hypervisorParams);
    }
    if (enterHostMaintenance.getTimeoutSeconds() != null) {
      params.put("timeout", enterHostMaintenance.getTimeoutSeconds());
    }
    if (enterHostMaintenance.getClientKey() != null) {
      params.put("client_key", enterHostMaintenance.getClientKey());
    }
    if (hostExtId != null) {
      params.put("host_uuid", hostExtId);
    }
    if (pcTaskUuid != null) {
      params.put("parent_task_uuid", pcTaskUuid);
    }
    final ObjectNode paramDict = JsonUtils.getObjectNode();
    paramDict.set("params", params);

    final String payLoad = MessageFormat.format(
      ClustermgmtUtils.GENESIS_REQUEST_PATTERN, ClustermgmtUtils.CLUSTER_MANAGER,
      ClustermgmtUtils.GenesisOp.ENTER_HOST_MAINTENANCE.getGenesisMethodName(),
      JsonUtils.toJsonString(paramDict));
    return payLoad;
  }

  @Override
  public HttpProxyConfig adaptHttpProxyConfigurationProtoToHttpProxy(
    String httpProxyExtId, GenesisInterfaceProto.HttpProxy httpProxyConfigurationProto) {
    HttpProxyConfig httpProxy = new HttpProxyConfig();

    if (httpProxyConfigurationProto.hasAddress())
      httpProxy.setIpAddress(ClustermgmtUtils.createIpv4Ipv6Address(httpProxyConfigurationProto.getAddress()));
    if (httpProxyConfigurationProto.hasPort())
      httpProxy.setPort(httpProxyConfigurationProto.getPort());
    if (httpProxyConfigurationProto.hasUsername())
      httpProxy.setUsername(httpProxyConfigurationProto.getUsername());
    if (httpProxyConfigurationProto.hasPassword())
      httpProxy.setPassword(httpProxyConfigurationProto.getPassword());
    if (httpProxyConfigurationProto.hasName())
      httpProxy.setName(httpProxyConfigurationProto.getName());
    List<HttpProxyType> httpProxyTypes = new ArrayList<>();
    for (String proxyType : httpProxyConfigurationProto.getProxyTypeList()) {
      httpProxyTypes.add(ClustermgmtUtils.httpProxyTypeStrMap.get(proxyType));
    }
    httpProxy.setProxyTypes(httpProxyTypes);

    return httpProxy;
  }

  @Override
  public HttpProxyWhiteListConfig adaptHttpProxyWhitelistConfigurationProtoToHttpProxyWhiteList(
    String httpProxyWhiteListExtId, GenesisInterfaceProto.HttpProxyWhitelist httpProxyWhiteListConfigurationProto) {
    HttpProxyWhiteListConfig httpProxyWhiteList = new HttpProxyWhiteListConfig();

    if (httpProxyWhiteListConfigurationProto.hasTarget())
      httpProxyWhiteList.setTarget(httpProxyWhiteListConfigurationProto.getTarget());
    if (httpProxyWhiteListConfigurationProto.hasTargetType())
      httpProxyWhiteList.setTargetType(ClustermgmtUtils.httpProxyWhiteListTargetTypeStrMap.get(
        httpProxyWhiteListConfigurationProto.getTargetType()));

    return httpProxyWhiteList;
  }

  @Override
  public String adaptExitHostMaintenanceToJsonRpcFormat(HostMaintenanceCommonSpec exitHostMaintenance, String hostExtId, String pcTaskUuid) {
    final ObjectNode params = JsonUtils.getObjectNode();
    if (exitHostMaintenance.getVcenterInfo() != null) {
      final ObjectNode hypervisorParams = JsonUtils.getObjectNode();
      final ObjectNode vcenterInfo = JsonUtils.getObjectNode();
      if (exitHostMaintenance.getVcenterInfo().getAddress() != null) {
        if (exitHostMaintenance.getVcenterInfo().getAddress().hasIpv4())
          vcenterInfo.put("vcenter_ip", exitHostMaintenance.getVcenterInfo().getAddress().getIpv4().getValue());
        else if (exitHostMaintenance.getVcenterInfo().getAddress().hasIpv6())
          vcenterInfo.put("vcenter_ip", exitHostMaintenance.getVcenterInfo().getAddress().getIpv6().getValue());
        else if (exitHostMaintenance.getVcenterInfo().getAddress().hasFqdn())
          vcenterInfo.put("vcenter_ip", exitHostMaintenance.getVcenterInfo().getAddress().getFqdn().getValue());
      }
      vcenterInfo.put("username", exitHostMaintenance.getVcenterInfo().getCredentials().getUsername());
      vcenterInfo.put("password", exitHostMaintenance.getVcenterInfo().getCredentials().getPassword());
      hypervisorParams.set("esx_params", vcenterInfo);
      params.set("hypervisor_params", hypervisorParams);
    }
    if (exitHostMaintenance.getTimeoutSeconds() != null) {
      params.put("timeout", exitHostMaintenance.getTimeoutSeconds());
    }
    if (exitHostMaintenance.getClientKey() != null) {
      params.put("client_key", exitHostMaintenance.getClientKey());
    }
    if (hostExtId != null) {
      params.put("host_uuid", hostExtId);
    }
    if (pcTaskUuid != null) {
      params.put("parent_task_uuid", pcTaskUuid);
    }
    final ObjectNode paramDict = JsonUtils.getObjectNode();
    paramDict.set("params", params);

    final String payLoad = MessageFormat.format(
      ClustermgmtUtils.GENESIS_REQUEST_PATTERN, ClustermgmtUtils.CLUSTER_MANAGER,
      ClustermgmtUtils.GenesisOp.EXIT_HOST_MAINTENANCE.getGenesisMethodName(),
      JsonUtils.toJsonString(paramDict));
    return payLoad;
  }

  @Override
  public String adaptComputeNonMigratableVmsToJsonRpcFormat(ComputeNonMigratableVmsSpec computeNonMigratableVms, String pcTaskUuid) {
    final ObjectNode params = JsonUtils.getObjectNode();
    if (computeNonMigratableVms.getHosts() != null) {
      final ArrayNode hostUuids = JsonUtils.getArrayNode();
      List<String> hosts = computeNonMigratableVms.getHosts();
      for (String hostUuid : hosts) {
        hostUuids.add(hostUuid);
      }
      params.set("hosts", hostUuids);
    }
    if (computeNonMigratableVms.getVcenterInfo() != null) {
      final ObjectNode hypervisorParams = JsonUtils.getObjectNode();
      final ObjectNode vcenterInfo = JsonUtils.getObjectNode();
      if (computeNonMigratableVms.getVcenterInfo().getAddress() != null) {
        if (computeNonMigratableVms.getVcenterInfo().getAddress().hasIpv4())
          vcenterInfo.put("vcenter_ip", computeNonMigratableVms.getVcenterInfo().getAddress().getIpv4().getValue());
        else if (computeNonMigratableVms.getVcenterInfo().getAddress().hasIpv6())
          vcenterInfo.put("vcenter_ip", computeNonMigratableVms.getVcenterInfo().getAddress().getIpv6().getValue());
        else if (computeNonMigratableVms.getVcenterInfo().getAddress().hasFqdn())
          vcenterInfo.put("vcenter_ip", computeNonMigratableVms.getVcenterInfo().getAddress().getFqdn().getValue());
      }
      vcenterInfo.put("username", computeNonMigratableVms.getVcenterInfo().getCredentials().getUsername());
      vcenterInfo.put("password", computeNonMigratableVms.getVcenterInfo().getCredentials().getPassword());
      hypervisorParams.set("esx_params", vcenterInfo);
      params.set("hypervisor_params", hypervisorParams);
    }
    if (pcTaskUuid != null) {
      params.put("parent_task_uuid", pcTaskUuid);
    }

    final ObjectNode paramDict = JsonUtils.getObjectNode();
    paramDict.set("params", params);

    final String payLoad = MessageFormat.format(
      ClustermgmtUtils.GENESIS_REQUEST_PATTERN, ClustermgmtUtils.CLUSTER_MANAGER,
      ClustermgmtUtils.GenesisOp.COMPUTE_NON_MIGRATABLE_VMS.getGenesisMethodName(),
      JsonUtils.toJsonString(paramDict));
    return payLoad;
  }

  @Override
  public NonMigratableVmsResult adaptJsonResponseToNonMigratableVms(List<InsightsInterfaceProto.MetricData> metricDataList, NonMigratableVmsResult nonMigratableVmsResult)
    throws ClustermgmtServiceException {
    for(final InsightsInterfaceProto.MetricData metricData : metricDataList) {
      if (!metricData.hasName() || metricData.getValueListCount() <= 0) {
        continue;
      }
      switch (metricData.getName()) {
        case "non_migratable_vms_list":
          List<NonMigratableVmInfo> nonMigratableVms = new ArrayList<>();
          String idfListStr = metricData.getValueList(0).getValue().getStrValue();
          try {
            JsonNode nonMigratableVmsNode = mapper.readTree(idfListStr);
            for(JsonNode nonMigratableResponseNode : nonMigratableVmsNode){
              NonMigratableVmInfo nonMigratableVmInfo = new NonMigratableVmInfo();
              try {
                if (nonMigratableResponseNode.has("name"))
                  nonMigratableVmInfo.setVmName(nonMigratableResponseNode.get("name").asText());
                if (nonMigratableResponseNode.has("uuid"))
                  nonMigratableVmInfo.setVmUuid(nonMigratableResponseNode.get("uuid").asText());
                if (nonMigratableResponseNode.has("reason"))
                  nonMigratableVmInfo.setNonMigratableVmReason(nonMigratableResponseNode.get("reason").asText());
                nonMigratableVms.add(nonMigratableVmInfo);
                nonMigratableVmsResult.setVms(nonMigratableVms);
              }
              catch (Exception e) {
                throw new ClustermgmtServiceException("Error in getting non-migratable-vms fields from nonMigratableResponseNode " + e.getMessage());
              }
            }
          }
          catch (Exception e) {
            throw new ClustermgmtServiceException("Error in parsing json output from non_migratable_vms_list " + e.getMessage());
          }
        default:
          break;
      }
    }
    return nonMigratableVmsResult;
  }

  public HostStats adaptToHostStats(HostStats hostStatsResp, Map<String, List<TimeValuePair>> statsAttributes) {
    for (Map.Entry<String, List<TimeValuePair>> attribute : statsAttributes.entrySet()) {
      if (attribute.getValue() == null) continue;
      switch (attribute.getKey()) {
        case "storage.logical_usage_bytes":
          hostStatsResp.setLogicalStorageUsageBytes(attribute.getValue());
          break;
        case "capacity.cpu_usage_hz":
          hostStatsResp.setCpuUsageHz(attribute.getValue());
          break;
        case "overall_memory_usage_bytes":
          hostStatsResp.setOverallMemoryUsageBytes(attribute.getValue());
          break;
        default:
          break;
      }
    }

    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptToHostStats(hostStatsResp, statsAttributes);
    }

    return hostStatsResp;
  }

}
