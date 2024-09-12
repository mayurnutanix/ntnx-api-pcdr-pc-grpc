package com.nutanix.clustermgmtserver.adapters.impl.v4.r0.b1;

import com.google.protobuf.ByteString;
import com.nutanix.api.utils.type.DateUtils;
import com.nutanix.cluster_management.ClusterManagementInterfaceProto;
import com.nutanix.clustermgmtserver.adapters.api.ClusterResourceAdapter;
import com.nutanix.clustermgmtserver.adapters.impl.BaseClusterResourceAdapterImpl;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceException;
import com.nutanix.clustermgmtserver.utils.ClustermgmtUtils;
import com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto;
import com.nutanix.insights.ifc.InsightsInterfaceProto;
import com.nutanix.prism.adapter.service.ZeusConfiguration;
import com.nutanix.stats_gateway.graphql_interface.GroupsGraphqlInterfaceProto;
import com.nutanix.zeus.protobuf.Configuration;
import com.nutanix.zeus.protobuf.DomainFaultToleranceStateProto;
import dp1.clu.clustermgmt.v4.config.*;
import dp1.clu.clustermgmt.v4.stats.ClusterStats;
import dp1.clu.clustermgmt.v4.stats.HostStats;
import dp1.clu.clustermgmt.v4.stats.TimeValuePair;
import dp1.clu.common.v1.config.IPAddress;
import dp1.clu.common.v1.config.IPAddressOrFQDN;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.*;

@Component(com.nutanix.clustermgmtserver.adapters.impl.v4.r0.b1.ClusterResourceAdapterImpl.ADAPTER_VERSION)
@Slf4j
public class ClusterResourceAdapterImpl extends BaseClusterResourceAdapterImpl {
  public static final String ADAPTER_VERSION = "v4.0.b1-clustermgmt-adapter";

  @Autowired
  public ClusterResourceAdapterImpl(
    @Qualifier(
      com.nutanix.clustermgmtserver.adapters.impl.v4.r0.b2.ClusterResourceAdapterImpl.ADAPTER_VERSION
    ) final ClusterResourceAdapter nextChainAdapter) {
    this.nextChainAdapter = nextChainAdapter;
  }

  @Override
  public String getVersionOfAdapter() {
    return ADAPTER_VERSION;
  }

  @Override
  public HostNic adaptIdfHostNicMetricstoHostNicEntity(List<InsightsInterfaceProto.MetricData> metricDataList,
                                                       HostNic hostNicEntity) {
    for (final InsightsInterfaceProto.MetricData metricData : metricDataList) {
      if (!metricData.hasName() || metricData.getValueListCount() <= 0) {
        continue;
      }
      switch (metricData.getName()) {
        case "port_name":
          hostNicEntity.setName(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "description":
          hostNicEntity.setHostDescription(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "node":
          hostNicEntity.setNodeUuid(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "mac_address":
          hostNicEntity.setMacAddress(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "ipv4_addresses":
          List<IPAddress> ipv4AddrList = new ArrayList<>();
          for (String addr : metricData.getValueList(0).getValue().getStrList().getValueListList()) {
            ipv4AddrList.add(ClustermgmtUtils.createIpv4Ipv6Address(addr));
          }
          hostNicEntity.setIpv4Addresses(ipv4AddrList);
          break;
        case "ipv6_addresses":
          List<IPAddress> ipv6AddrList = new ArrayList<>();
          for (String addr : metricData.getValueList(0).getValue().getStrList().getValueListList()) {
            ipv6AddrList.add(ClustermgmtUtils.createIpv4Ipv6Address(addr));
          }
          hostNicEntity.setIpv6Addresses(ipv6AddrList);
          break;
        case "discovery_protocol":
          hostNicEntity.setDiscoveryProtocol(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "switch_dev_id":
          hostNicEntity.setSwitchDeviceId(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "switch_port_id":
          hostNicEntity.setSwitchPortId(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "switch_vlan_id":
          hostNicEntity.setSwitchVlanId(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "switch_mac_addr":
          hostNicEntity.setSwitchMacAddress(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "switch_mgmt_ip_address":
          String addr = metricData.getValueList(0).getValue().getStrValue();
          hostNicEntity.setSwitchManagementIp(ClustermgmtUtils.createIpv4Ipv6Address(addr));
          break;
        case "status":
          hostNicEntity.setInterfaceStatus(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "dhcp_enabled":
          hostNicEntity.setIsDhcpEnabled(metricData.getValueList(0).getValue().getBoolValue());
          break;
        case "link_speed_kbps":
          hostNicEntity.setLinkSpeedInKbps(metricData.getValueList(0).getValue().getInt64Value());
          break;
        case "mtu_bytes":
          hostNicEntity.setMtuInBytes(metricData.getValueList(0).getValue().getInt64Value());
          break;
        case "switch_hardware_platform":
          hostNicEntity.setSwitchVendorInfo(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "tx_ring_size":
          hostNicEntity.setTxRingSizeInBytes(metricData.getValueList(0).getValue().getInt64Value());
        case "rx_ring_size":
          hostNicEntity.setRxRingSizeInBytes(metricData.getValueList(0).getValue().getInt64Value());
        default:
          break;
      }
    }
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptIdfHostNicMetricstoHostNicEntity(metricDataList, hostNicEntity);
    }

    return hostNicEntity;
  }

  @Override
  public NetworkSwitchInterface adaptIdfSwitchIntfMetricstoNetworkSwitchInterfaceEntity(List<InsightsInterfaceProto.MetricData> metricDataList,
                                                                                        NetworkSwitchInterface switchIntfEntity) {
    for (final InsightsInterfaceProto.MetricData metricData : metricDataList) {
      if (!metricData.hasName() || metricData.getValueListCount() <= 0) {
        continue;
      }
      switch (metricData.getName()) {
        case "state_last_change_time":
          OffsetDateTime dateTimeObj = DateUtils.fromEpochMicros(metricData.getValueList(0).getValue().getInt64Value());
          switchIntfEntity.setLastChangeTime(dateTimeObj);
          break;
        case "type":
          switchIntfEntity.setSwitchInterfaceType(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "switch_management_address":
          String addr = metricData.getValueList(0).getValue().getStrValue();
          switchIntfEntity.setSwitchManagementAddress(ClustermgmtUtils.createIpv4Ipv6Address(addr));
          break;
        case "port_num":
          switchIntfEntity.setPort(metricData.getValueList(0).getValue().getInt64Value());
          break;
        case "index":
          switchIntfEntity.setIndex(metricData.getValueList(0).getValue().getInt64Value());
          break;
        case "speed_kbps":
          switchIntfEntity.setSpeedInKbps(metricData.getValueList(0).getValue().getInt64Value());
          break;
        case "mtu_bytes":
          switchIntfEntity.setMtuInBytes(metricData.getValueList(0).getValue().getInt64Value());
          break;
        case "description":
          switchIntfEntity.setSwitchInterfaceDescription(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "name":
          switchIntfEntity.setSwitchInterfaceName(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "host_nic_uuids":
          List<String> hostNicUuidsList = new ArrayList<>();
          for (String uuid : metricData.getValueList(0).getValue().getStrList().getValueListList()) {
            hostNicUuidsList.add(uuid);
          }
          switchIntfEntity.setAttachedHostNicUuids(hostNicUuidsList);
          break;
        case "physical_address":
          switchIntfEntity.setMacAddress(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "switch_uuid":
          switchIntfEntity.setSwitchUuid(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "node":
          switchIntfEntity.setAttachedHostUuid(metricData.getValueList(0).getValue().getStrValue());
          break;
        default:
          break;
      }
    }
    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptIdfSwitchIntfMetricstoNetworkSwitchInterfaceEntity(metricDataList, switchIntfEntity);
    }

    return switchIntfEntity;
  }

  @Override
  public VirtualNic adaptIdfVirtualNicMetricstoVirtualNicEntity(List<InsightsInterfaceProto.MetricData> metricDataList,
                                                                VirtualNic virtualNicEntity) {
    for (final InsightsInterfaceProto.MetricData metricData : metricDataList) {
      if (!metricData.hasName() || metricData.getValueListCount() <= 0) {
        continue;
      }
      switch (metricData.getName()) {
        case "name":
          virtualNicEntity.setName(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "description":
          virtualNicEntity.setHostDescription(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "node":
          virtualNicEntity.setNodeUuid(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "mac_address":
          virtualNicEntity.setMacAddress(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "ipv4_addresses":
          List<IPAddress> ipv4AddrList = new ArrayList<>();
          for (String addr : metricData.getValueList(0).getValue().getStrList().getValueListList()) {
            ipv4AddrList.add(ClustermgmtUtils.createIpv4Ipv6Address(addr));
          }
          virtualNicEntity.setIpv4Addresses(ipv4AddrList);
          break;
        case "ipv6_addresses":
          List<IPAddress> ipv6AddrList = new ArrayList<>();
          for (String addr : metricData.getValueList(0).getValue().getStrList().getValueListList()) {
            ipv6AddrList.add(ClustermgmtUtils.createIpv4Ipv6Address(addr));
          }
          virtualNicEntity.setIpv6Addresses(ipv6AddrList);
          break;
        case "status":
          virtualNicEntity.setInterfaceStatus(metricData.getValueList(0).getValue().getStrValue());
          break;
        case "dhcp_enabled":
          virtualNicEntity.setIsDhcpEnabled(metricData.getValueList(0).getValue().getBoolValue());
          break;
        case "link_speed_kbps":
          virtualNicEntity.setLinkSpeedInKbps(metricData.getValueList(0).getValue().getInt64Value());
          break;
        case "mtu_bytes":
          virtualNicEntity.setMtuInBytes(metricData.getValueList(0).getValue().getInt64Value());
          break;
        case "vlan_id":
          virtualNicEntity.setVlanId(metricData.getValueList(0).getValue().getInt64Value());
          break;
        case "host_nic_uuids":
          List<String> hostNicUuids = new ArrayList<>();
          for (String uuid : metricData.getValueList(0).getValue().getStrList().getValueListList()) {
            hostNicUuids.add(uuid);
          }
          virtualNicEntity.setHostNicsUuids(hostNicUuids);
          break;
        default:
          break;
      }
    }

    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptIdfVirtualNicMetricstoVirtualNicEntity(metricDataList, virtualNicEntity);
    }

    return virtualNicEntity;
  }

  @Override
  public ClusterStats adaptToClusterStats(ClusterStats clusterStatsResp, Map<String, List<TimeValuePair>> statsAttributes) {

    for (Map.Entry<String, List<TimeValuePair>> attribute : statsAttributes.entrySet()) {
      if (attribute.getValue() == null) continue;
      switch (attribute.getKey()) {
        case "controller_avg_io_latency_usecs":
          clusterStatsResp.setControllerAvgIoLatencyUsecs(attribute.getValue());
          break;
        case "capacity.controller_avg_io_latency_usecs.upper_buff":
          clusterStatsResp.setControllerAvgIoLatencyUsecsLowerBuf(attribute.getValue());
          break;
        case "capacity.controller_avg_io_latency_usecs.lower_buff":
          clusterStatsResp.setControllerAvgIoLatencyUsecsLowerBuf(attribute.getValue());
          break;
        case "controller_avg_read_io_latency_usecs":
          clusterStatsResp.setControllerAvgReadIoLatencyUsecs(attribute.getValue());
          break;
        case "capacity.controller_avg_read_io_latency_usecs.upper_buff":
          clusterStatsResp.setControllerAvgReadIoLatencyUsecsUpperBuf(attribute.getValue());
          break;
        case "capacity.controller_avg_read_io_latency_usecs.lower_buff":
          clusterStatsResp.setControllerAvgReadIoLatencyUsecsLowerBuf(attribute.getValue());
          break;
        case "controller_avg_write_io_latency_usecs":
          clusterStatsResp.setControllerAvgWriteIoLatencyUsecs(attribute.getValue());
          break;
        case "capacity.controller_avg_write_io_latency_usecs.upper_buff":
          clusterStatsResp.setControllerAvgWriteIoLatencyUsecsUpperBuf(attribute.getValue());
          break;
        case "capacity.controller_avg_write_io_latency_usecs.lower_buff":
          clusterStatsResp.setControllerAvgWriteIoLatencyUsecsLowerBuf(attribute.getValue());
          break;
        case "hypervisor_cpu_usage_ppm":
          clusterStatsResp.setHypervisorCpuUsagePpm(attribute.getValue());
          break;
        case "capacity.hypervisor_cpu_usage_ppm.upper_buff":
          clusterStatsResp.setHypervisorCpuUsagePpmUpperBuf(attribute.getValue());
          break;
        case "capacity.hypervisor_cpu_usage_ppm.lower_buff":
          clusterStatsResp.setHypervisorCpuUsagePpmLowerBuf(attribute.getValue());
          break;
        case "aggregate_hypervisor_memory_usage_ppm":
          clusterStatsResp.setAggregateHypervisorMemoryUsagePpm(attribute.getValue());
          break;
        case "capacity.aggregate_hypervisor_memory_usage_ppm.upper_buff":
          clusterStatsResp.setAggregateHypervisorMemoryUsagePpmUpperBuf(attribute.getValue());
          break;
        case "capacity.aggregate_hypervisor_memory_usage_ppm.lower_buff":
          clusterStatsResp.setAggregateHypervisorMemoryUsagePpmLowerBuf(attribute.getValue());
          break;
        case "overall_memory_usage_bytes":
          clusterStatsResp.setOverallMemoryUsageBytes(attribute.getValue());
          break;
        case "controller_num_iops":
          clusterStatsResp.setControllerNumIops(attribute.getValue());
          break;
        case "capacity.controller_num_iops.upper_buff":
          clusterStatsResp.setControllerNumIopsUpperBuf(attribute.getValue());
          break;
        case "capacity.controller_num_iops.lower_buff":
          clusterStatsResp.setControllerNumIopsLowerBuf(attribute.getValue());
          break;
        case "controller_num_read_iops":
          clusterStatsResp.setControllerNumReadIops(attribute.getValue());
          break;
        case "capacity.controller_num_read_iops.upper_buff":
          clusterStatsResp.setControllerNumReadIopsUpperBuf(attribute.getValue());
          break;
        case "capacity.controller_num_read_iops.lower_buff":
          clusterStatsResp.setControllerNumReadIopsLowerBuf(attribute.getValue());
          break;
        case "controller_num_write_iops":
          clusterStatsResp.setControllerNumWriteIops(attribute.getValue());
          break;
        case "capacity.controller_num_write_iops.upper_buff":
          clusterStatsResp.setControllerNumWriteIopsUpperBuf(attribute.getValue());
          break;
        case "capacity.controller_num_write_iops.lower_buff":
          clusterStatsResp.setControllerNumWriteIopsLowerBuf(attribute.getValue());
          break;
        case "controller_io_bandwidth_kBps":
          clusterStatsResp.setIoBandwidthKbps(attribute.getValue());
          break;
        case "capacity.controller_io_bandwidth_kBps.upper_buff":
          clusterStatsResp.setIoBandwidthKbpsUpperBuf(attribute.getValue());
          break;
        case "capacity.controller_io_bandwidth_kBps.lower_buff":
          clusterStatsResp.setIoBandwidthKbpsLowerBuf(attribute.getValue());
          break;
        case "controller_read_io_bandwidth_kBps":
          clusterStatsResp.setControllerReadIoBandwidthKbps(attribute.getValue());
          break;
        case "capacity.controller_read_io_bandwidth_kBps.upper_buff":
          clusterStatsResp.setControllerReadIoBandwidthKbpsUpperBuf(attribute.getValue());
          break;
        case "capacity.controller_read_io_bandwidth_kBps.lower_buff":
          clusterStatsResp.setControllerReadIoBandwidthKbpsLowerBuf(attribute.getValue());
          break;
        case "controller_write_io_bandwidth_kBps":
          clusterStatsResp.setControllerWriteIoBandwidthKbps(attribute.getValue());
          break;
        case "capacity.controller_write_io_bandwidth_kBps.upper_buff":
          clusterStatsResp.setControllerWriteIoBandwidthKbpsUpperBuf(attribute.getValue());
          break;
        case "capacity.controller_write_io_bandwidth_kBps.lower_buff":
          clusterStatsResp.setControllerWriteIoBandwidthKbpsLowerBuf(attribute.getValue());
          break;
        case "storage.usage_bytes":
          clusterStatsResp.setStorageUsageBytes(attribute.getValue());
          break;
        case "storage.capacity_bytes":
          clusterStatsResp.setStorageCapacityBytes(attribute.getValue());
          break;
        case "storage.free_bytes":
          clusterStatsResp.setFreePhysicalStorageBytes(attribute.getValue());
          break;
        case "storage.logical_usage_bytes":
          clusterStatsResp.setLogicalStorageUsageBytes(attribute.getValue());
          break;
        case "check.score":
          log.debug("Adapting NCC health check score for cluster.");
          clusterStatsResp.setHealthCheckScore(attribute.getValue());
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
  public HostStats adaptToHostStats(HostStats hostStatsResp, Map<String, List<TimeValuePair>> statsAttributes) {
    for (Map.Entry<String, List<TimeValuePair>> attribute : statsAttributes.entrySet()) {
      if (attribute.getValue() == null) continue;
      switch (attribute.getKey()) {
        case "controller_avg_io_latency_usecs":
          hostStatsResp.setControllerAvgIoLatencyUsecs(attribute.getValue());
          break;
        case "capacity.controller_avg_io_latency_usecs.upper_buff":
          hostStatsResp.setControllerAvgIoLatencyUsecsUpperBuf(attribute.getValue());
          break;
        case "capacity.controller_avg_io_latency_usecs.lower_buff":
          hostStatsResp.setControllerAvgIoLatencyUsecsLowerBuf(attribute.getValue());
          break;
        case "controller_avg_read_io_latency_usecs":
          hostStatsResp.setControllerAvgReadIoLatencyUsecs(attribute.getValue());
          break;
        case "capacity.controller_avg_read_io_latency_usecs.upper_buff":
          hostStatsResp.setControllerAvgReadIoLatencyUsecsUpperBuf(attribute.getValue());
          break;
        case "capacity.controller_avg_read_io_latency_usecs.lower_buff":
          hostStatsResp.setControllerAvgReadIoLatencyUsecsLowerBuf(attribute.getValue());
          break;
        case "controller_avg_write_io_latency_usecs":
          hostStatsResp.setControllerAvgWriteIoLatencyUsecs(attribute.getValue());
          break;
        case "capacity.controller_avg_write_io_latency_usecs.upper_buff":
          hostStatsResp.setControllerAvgWriteIoLatencyUsecsUpperBuf(attribute.getValue());
          break;
        case "capacity.controller_avg_write_io_latency_usecs.lower_buff":
          hostStatsResp.setControllerAvgWriteIoLatencyUsecsLowerBuf(attribute.getValue());
          break;
        case "hypervisor_cpu_usage_ppm":
          hostStatsResp.setHypervisorCpuUsagePpm(attribute.getValue());
          break;
        case "capacity.hypervisor_cpu_usage_ppm.upper_buff":
          hostStatsResp.setHypervisorCpuUsagePpmUpperBuf(attribute.getValue());
          break;
        case "capacity.hypervisor_cpu_usage_ppm.lower_buff":
          hostStatsResp.setHypervisorCpuUsagePpmLowerBuf(attribute.getValue());
          break;
        case "aggregate_hypervisor_memory_usage_ppm":
          hostStatsResp.setAggregateHypervisorMemoryUsagePpm(attribute.getValue());
          break;
        case "capacity.aggregate_hypervisor_memory_usage_ppm.upper_buff":
          hostStatsResp.setAggregateHypervisorMemoryUsagePpmUpperBuf(attribute.getValue());
          break;
        case "capacity.aggregate_hypervisor_memory_usage_ppm.lower_buff":
          hostStatsResp.setAggregateHypervisorMemoryUsagePpmLowerBuf(attribute.getValue());
          break;
        case "overall_memory_usage_ppm":
          hostStatsResp.setOverallMemoryUsagePpm(attribute.getValue());
          break;
        case "capacity.overall_memory_usage_ppm.upper_buff":
          hostStatsResp.setOverallMemoryUsagePpmUpperBuf(attribute.getValue());
          break;
        case "capacity.overall_memory_usage_ppm.lower_buff":
          hostStatsResp.setOverallMemoryUsagePpmLowerBuf(attribute.getValue());
          break;
        case "controller_num_iops":
          hostStatsResp.setControllerNumIops(attribute.getValue());
          break;
        case "capacity.controller_num_iops.upper_buff":
          hostStatsResp.setControllerNumIopsUpperBuf(attribute.getValue());
          break;
        case "capacity.controller_num_iops.lower_buff":
          hostStatsResp.setControllerNumIopsLowerBuf(attribute.getValue());
          break;
        case "controller_num_read_iops":
          hostStatsResp.setControllerNumReadIops(attribute.getValue());
          break;
        case "capacity.controller_num_read_iops.upper_buff":
          hostStatsResp.setControllerNumReadIopsUpperBuf(attribute.getValue());
          break;
        case "capacity.controller_num_read_iops.lower_buff":
          hostStatsResp.setControllerNumReadIopsLowerBuf(attribute.getValue());
          break;
        case "controller_num_write_iops":
          hostStatsResp.setControllerNumWriteIops(attribute.getValue());
          break;
        case "capacity.controller_num_write_iops.upper_buff":
          hostStatsResp.setControllerNumWriteIopsUpperBuf(attribute.getValue());
          break;
        case "capacity.controller_num_write_iops.lower_buff":
          hostStatsResp.setControllerNumWriteIopsLowerBuf(attribute.getValue());
          break;
        case "controller_io_bandwidth_kBps":
          hostStatsResp.setIoBandwidthKbps(attribute.getValue());
          break;
        case "capacity.controller_io_bandwidth_kBps.upper_buff":
          hostStatsResp.setIoBandwidthKbpsUpperBuf(attribute.getValue());
          break;
        case "capacity.controller_io_bandwidth_kBps.lower_buff":
          hostStatsResp.setIoBandwidthKbpsLowerBuf(attribute.getValue());
          break;
        case "controller_read_io_bandwidth_kBps":
          hostStatsResp.setControllerReadIoBandwidthKbps(attribute.getValue());
          break;
        case "capacity.controller_read_io_bandwidth_kBps.upper_buff":
          hostStatsResp.setControllerReadIoBandwidthKbpsUpperBuf(attribute.getValue());
          break;
        case "capacity.controller_read_io_bandwidth_kBps.lower_buff":
          hostStatsResp.setControllerReadIoBandwidthKbpsLowerBuf(attribute.getValue());
          break;
        case "controller_write_io_bandwidth_kBps":
          hostStatsResp.setControllerWriteIoBandwidthKbps(attribute.getValue());
          break;
        case "capacity.controller_write_io_bandwidth_kBps.upper_buff":
          hostStatsResp.setControllerWriteIoBandwidthKbpsUpperBuf(attribute.getValue());
          break;
        case "capacity.controller_write_io_bandwidth_kBps.lower_buff":
          hostStatsResp.setControllerWriteIoBandwidthKbpsLowerBuf(attribute.getValue());
          break;
        case "storage.usage_bytes":
          hostStatsResp.setStorageUsageBytes(attribute.getValue());
          break;
        case "storage.capacity_bytes":
          hostStatsResp.setStorageCapacityBytes(attribute.getValue());
          break;
        case "storage.free_bytes":
          hostStatsResp.setFreePhysicalStorageBytes(attribute.getValue());
          break;
        case "memory_size_bytes":
          hostStatsResp.setMemoryCapacityBytes(attribute.getValue());
          break;
        case "cpu.capacity_hz":
          hostStatsResp.setCpuCapacityHz(attribute.getValue());
          break;
        case "check.score":
          log.debug("Adapting NCC health check score for host.");
          hostStatsResp.setHealthCheckScore(attribute.getValue());
        default:
          break;
      }
    }

    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptToHostStats(hostStatsResp, statsAttributes);
    }

    return hostStatsResp;
  }

  @Override
  public ClusterManagementInterfaceProto.ClusterCreateArg adaptClusterCreateParamsToClusterCreateArg(Cluster body,
                                                                                                     ClusterManagementInterfaceProto.ClusterCreateArg.Builder clusterCreateArg) {

    //cluster name
    if (body.getName() != null) {
      clusterCreateArg.setClusterName(body.getName());
    }


    if (body.getNetwork() != null) {

      ClusterNetworkReference network = body.getNetwork();
      // cvm external address
      if (network.getExternalAddress() != null && network.getExternalAddress().hasIpv4()) {
        String cvmExtAdd = ClustermgmtUtils.getIpv4Address(network.getExternalAddress());
        clusterCreateArg.setClusterExternalIp(cvmExtAdd);
      }

      // name servers
      if (network.getNameServerIpList() != null) {
        List<String> dnsServerList = new ArrayList<>();
        for (IPAddressOrFQDN address : network.getNameServerIpList()) {
          if (address.hasIpv4()) {
            dnsServerList.add(address.getIpv4().getValue());
          } else {
            dnsServerList.add(address.getFqdn().getValue());
          }
        }
        clusterCreateArg.addAllNameServerIpList(dnsServerList);
      }

      //ntp servers
      if (network.getNtpServerIpList() != null) {
        List<String> ntpServerList = new ArrayList<>();
        for (IPAddressOrFQDN address : network.getNtpServerIpList()) {
          if (address.hasIpv4()) {
            ntpServerList.add(address.getIpv4().getValue());
          } else {
            ntpServerList.add(address.getFqdn().getValue());
          }
        }
        clusterCreateArg.addAllNtpServerList(ntpServerList);
      }

      //backplane params
      if (network.getBackplane() != null) {
        BackplaneNetworkParams backplaneSegParams = network.getBackplane();
        Boolean enableBackplaneSegmentation = backplaneSegParams.getIsSegmentationEnabled();
        clusterCreateArg.setEnableBackplaneSegmentation(enableBackplaneSegmentation);
        if (enableBackplaneSegmentation) {
          ClusterManagementInterfaceProto.ClusterCreateArg.BackplaneParams.Builder backplaneParams =
            ClusterManagementInterfaceProto.ClusterCreateArg.BackplaneParams.newBuilder();
          if (backplaneSegParams.getVlanTag() != null) {
            backplaneParams.setVlanId(backplaneSegParams.getVlanTag());
          }
          if (backplaneSegParams.getSubnet() != null) {
            backplaneParams.setSubnet(backplaneSegParams.getSubnet().getValue());
          }
          if (backplaneSegParams.getNetmask() != null) {
            backplaneParams.setNetmask(backplaneSegParams.getNetmask().getValue());
          }

          clusterCreateArg.setBackplaneParams(backplaneParams);
        }
      }

    }

    //container name
    if (body.getContainerName() != null) {
      clusterCreateArg.setContainerName(body.getContainerName());
    }

    if (body.getConfig() != null) {

      ClusterConfigReference config = body.getConfig();

      //redundancy factor
      if (config.getRedundancyFactor() != null) {
        clusterCreateArg.setRedundancyFactor(config.getRedundancyFactor());
      }

      //cluster function list
      if (config.getClusterFunction() != null) {
        List<Configuration.ConfigurationProto.ClusterFunctions> clusterFunctionList = new ArrayList<>();
        for (ClusterFunctionRef clusterFunction : config.getClusterFunction()) {
          Configuration.ConfigurationProto.ClusterFunctions clusterFunctionProtoType =
            ClustermgmtUtils.clusterFunctionProtoHashMapForCreateCluster.get(clusterFunction);
          clusterFunctionList.add(clusterFunctionProtoType);
        }
        clusterCreateArg.addAllClusterFunctionList(clusterFunctionList);
      }

      //domain awareness level
      if (config.getFaultToleranceState() != null) {
        FaultToleranceState faultToleranceState = config.getFaultToleranceState();
        if (faultToleranceState.getDomainAwarenessLevel() != null) {
          DomainFaultToleranceStateProto.DomainFaultToleranceState.Domain.DomainType domainType =
            ClustermgmtUtils.getKey(ClustermgmtUtils.domainAwarenessMap, config.getFaultToleranceState().getDomainAwarenessLevel());
          clusterCreateArg.setDomainAwarenessLevel(domainType);
        }
      }

    }

    // svm ips of the cluster
    if (body.getNodes() != null) {

      NodeReference nodes = body.getNodes();

      if (nodes.getNodeList() != null) {
        List<String> address = new ArrayList<>();
        List<NodeListItemReference> nodeList = nodes.getNodeList();
        for (NodeListItemReference node : nodeList) {
          if (node.getControllerVmIp().getIpv4() != null) {
            address.add(ClustermgmtUtils.getIpv4Address(node.getControllerVmIp()));
          }
        }
        clusterCreateArg.addAllSvmIps(address);
      }
    }

    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptClusterCreateParamsToClusterCreateArg(body, clusterCreateArg);
    }

    return clusterCreateArg.build();
  }

  @Override
  public GenesisInterfaceProto.UpdateCategoryAssociationsArg adaptUpdateCategoriesParamsToUpdateCategoryAssociationsArg(Set<String> categoryUuids,
                                                                                                                        String entityType,
                                                                                                                        String entityUuid,
                                                                                                                        String operationType,
                                                                                                                        GenesisInterfaceProto.UpdateCategoryAssociationsArg.Builder updateCategoryAssociationsArg) {
    if (!categoryUuids.isEmpty()) {
      updateCategoryAssociationsArg.addAllCategoryUuids(categoryUuids);
    }

    if (entityType != null) {
      updateCategoryAssociationsArg.setEntityType(entityType);
    }

    if (entityUuid != null) {
      updateCategoryAssociationsArg.setEntityUuid(entityUuid);
    }

    if (operationType != null) {
      if (operationType.equals("attach")) {
        updateCategoryAssociationsArg.setOperationType(GenesisInterfaceProto.UpdateCategoryAssociationsArg.OperationType.kOperationAttach);
      } else
        updateCategoryAssociationsArg.setOperationType(GenesisInterfaceProto.UpdateCategoryAssociationsArg.OperationType.kOperationDetach);
    }

    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptUpdateCategoriesParamsToUpdateCategoryAssociationsArg(categoryUuids, entityType, entityUuid, operationType, updateCategoryAssociationsArg);
    }

    return updateCategoryAssociationsArg.build();
  }

  @Override
  public Cluster adaptZeusEntriestoClusterEntity(ZeusConfiguration zkConfig, Cluster clusterEntity) {

    final Configuration.ConfigurationProto.Aegis aegis = zkConfig.getAegis().get();

    // Pulse Status
    PulseStatus pulseStatus = new PulseStatus();

    if (aegis.hasAutoSupportConfig()) {
      Configuration.ConfigurationProto.Aegis.AutoSupportConfig autoSupportConfig = aegis.getAutoSupportConfig();

      if (autoSupportConfig.hasEmailAsups()) {
        pulseStatus.isEnabled = autoSupportConfig.getEmailAsups().getValue();
      }
      if (autoSupportConfig.hasPiiScrubbingLevel()) {
        pulseStatus.piiScrubbingLevel = ClustermgmtUtils.piiScrubbingLevelMap.get(autoSupportConfig.getPiiScrubbingLevel());
      }

      clusterEntity.getConfig().setPulseStatus(pulseStatus);
    }

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

      // Pulse Status
      if (config.getPulseStatus() != null) {

        GenesisInterfaceProto.UpdateClusterArg.PulseStatus.Builder pulseStatus =
          GenesisInterfaceProto.UpdateClusterArg.PulseStatus.newBuilder();

        // Update Pulse Enabled
        Configuration.ConfigurationProto.Aegis.TimedBool.Builder pulseEnabled =
          Configuration.ConfigurationProto.Aegis.TimedBool.newBuilder();
        if (config.getPulseStatus().getIsEnabled() != null) {
          pulseEnabled.setValue(config.getPulseStatus().getIsEnabled());
          pulseStatus.setEmailAsups(pulseEnabled);

          if (Boolean.TRUE.equals(config.getPulseStatus().getIsEnabled()) && config.getPulseStatus().getPiiScrubbingLevel() != null) {
            Configuration.ConfigurationProto.Aegis.AutoSupportConfig.PIIScrubbingLevel piiScrubbingLevel =
              ClustermgmtUtils.getKey(ClustermgmtUtils.piiScrubbingLevelMap, config.getPulseStatus().getPiiScrubbingLevel());
            pulseStatus.setPiiScrubbingLevel(piiScrubbingLevel);
          }
        }

        // Update Pulse Status
        updateClusterArg.setPulseStatus(pulseStatus);
      }
    }

    if (adapterRegistry.canForwardToNextHandler(this.getClass())) {
      return nextChainAdapter.adaptClusterEntityToClusterUpdateArg(body, updateClusterArg, parentTaskUuid);
    }

    return updateClusterArg.build();
  }
}