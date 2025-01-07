/*
 * Copyright (c) 2021 Nutanix Inc. All rights reserved.
 *
 * Author: rakesh.falak@nutanix.com
 */

package com.nutanix.clustermgmtserver.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.nutanix.api.utils.json.JsonUtils;
import com.nutanix.api.utils.resilience.CircuitBreakerUtility;
import com.nutanix.clustermgmtserver.adapters.api.ClusterResourceAdapter;
import com.nutanix.clustermgmtserver.adapters.impl.ClustermgmtResourceAdapterImpl;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceException;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtZkConfigReadException;
import com.nutanix.clustermgmtserver.proxy.EntityDbProxyImpl;
import com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto;
import com.nutanix.insights.ifc.InsightsInterfaceProto;
import com.nutanix.insights.ifc.InsightsInterfaceProto.GetEntitiesWithMetricsRet;
import com.nutanix.insights.ifc.InsightsInterfaceProto.GetEntitiesWithMetricsArg;
import com.nutanix.insights.ifc.InsightsInterfaceProto.GetEntitiesRet;
import com.nutanix.insights.ifc.InsightsInterfaceProto.GetEntitiesArg;
import com.nutanix.net.RpcProto;
import com.nutanix.prism.adapter.service.ZeusConfiguration;
import com.nutanix.prism.base.zk.ProtobufZNodeManagementException;
import com.nutanix.prism.commands.multicluster.MulticlusterZeusConfigurationManagingZkImpl;
import com.nutanix.prism.commands.validation.ValidationUtils;
import com.nutanix.util.base.Pair;
import com.nutanix.util.base.UuidUtils;
import com.nutanix.zeus.protobuf.Configuration.ConfigurationProto;
import com.nutanix.zeus.protobuf.Configuration.ConfigurationProto.RSyslogConfig.LogServerConfig.Module.Priority;
import com.nutanix.zeus.protobuf.Configuration.ConfigurationProto.Node.AcropolisStatus;
import com.nutanix.zeus.protobuf.Configuration.ConfigurationProto.Node.AcropolisStatus.AcropolisNodeState;
import com.nutanix.zeus.protobuf.Configuration.ConfigurationProto.NetworkEntity.ProtocolType;
import com.nutanix.zeus.protobuf.Configuration.ConfigurationProto.RackableUnit;
import com.nutanix.zeus.protobuf.Configuration.ConfigurationProto.SnmpInfo.Protocol;
import com.nutanix.zeus.protobuf.Configuration.ConfigurationProto.SnmpInfo.SnmpVersion;
import com.nutanix.zeus.protobuf.Configuration.ConfigurationProto.SnmpInfo.User.AuthType;
import com.nutanix.zeus.protobuf.Configuration.ConfigurationProto.SnmpInfo.User.PrivType;
import com.nutanix.zeus.protobuf.Configuration.ConfigurationProto.Aegis.SmtpServerType;
import com.nutanix.zeus.protobuf.DomainFaultToleranceStateProto.DomainFaultToleranceState.Domain.Component.ComponentType;
import com.nutanix.zeus.protobuf.DomainFaultToleranceStateProto.DomainFaultToleranceState.Domain.DomainType;
import com.nutanix.zeus.protobuf.ReplicaPlacementConfigProto.ReplicaPlacementPolicy;
import dp1.clu.clustermgmt.v4.config.*;
import dp1.clu.clustermgmt.v4.operations.*;
import dp1.clu.common.v1.config.*;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import nutanix.ergon.ErgonInterface;
import nutanix.ergon.ErgonTypes;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

@Slf4j
public class ClustermgmtUtils {

  public static final String GENESIS_REQUEST_PATTERN =
    "'{'\".oid\":\"{0}\", \".method\":\"{1}\", \".kwargs\":{2}'}'";
  public static final String NODE_MANAGER = "NodeManager";
  public static final String CLUSTER_MANAGER = "ClusterManager";

  // API versions.
  public static final String V4_R0_A1_VERSION = "v4.0.a1";
  public static final String V4_R0_A2_VERSION = "v4.0.a2";
  public static final String V4_R0_B1_VERSION = "v4.0.b1";
  public static final String V4_R0_B2_VERSION = "v4.0.b2";
  public static final String V4_R0_VERSION = "v4.0";
  public static final String FULL_VERSION_HEADER_NAME = "Full-Version";
  public static final String LATEST_FULL_VERSION_HEADER_VALUE = V4_R0_VERSION;

  // Constants
  public static final String CVM_IP = "cvm_ip";
  public static final String HYPERVISOR_IP = "hypervisor_ip";
  public static final String IPMI_IP = "ipmi_ip";
  public static final String CVM_IPV6 = "cvm_ipv6";
  public static final String HYPERVISOR_IPV6 = "hypervisor_ipv6";
  public static final String IPMI_IPV6 = "ipmi_ipv6";
  public static final String HYPERVISOR_TYPE = "hypervisor_type";
  private static final Pattern FQDN_PATTERN = Pattern.compile("^(?!-)(?:[a-zA-Z0-9-]{1,63}(?<!-)\\.)+[a-zA-Z]{2,}$");
  public static final String RENAME_HOST = "renameHost";
  public static final String UPDATE_CLUSTER = "updateCluster";
  public static final String UPDATE_SNMP_STATUS = "updateSnmpStatus";
  public static final String GET_SNMP_CONFIG = "getSnmpConfig";
  public static final String ADD_SNMP_TRANSPORT = "addSnmpTransport";
  public static final String REMOVE_SNMP_TRANSPORT = "removeSnmpTransport";
  public static final String GET_SNMP_USER = "getSnmpUser";
  public static final String ADD_SNMP_USER = "addSnmpUser";
  public static final String UPDATE_SNMP_USER = "updateSnmpUser";
  public static final String DELETE_SNMP_USER = "deleteSnmpUser";
  public static final String GET_SNMP_TRAP = "getSnmpTrap";
  public static final String ADD_SNMP_TRAP = "addSnmpTrap";
  public static final String UPDATE_SNMP_TRAP = "updateSnmpTrap";
  public static final String DELETE_SNMP_TRAP = "deleteSnmpTrap";
  public static final String GET_RSYSLOG_SERVER = "getRsyslogServer";
  public static final String GET_RSYSLOG_CONFIG = "getRsyslogConfig";
  public static final String ADD_RSYSLOG_SERVER = "addRsyslogServer";
  public static final String UPDATE_RSYSLOG_SERVER = "updateRsyslogServer";
  public static final String DELETE_RSYSLOG_SERVER = "deleteRsyslogServer";
  public static final String DISCOVER_UNCONFIGURED_NODES = "discoverUnconfiguredNodes";
  public static final String GET_NETWORKING_DETAILS = "getNodeNetworkingDetails";
  public static final String ADD_NODE = "addNode";
  public static final String IS_HYPERVISOR_UPLOAD_REQ = "isHypervisorUploadRequired";
  public static final String VALIDATE_NODE = "validateNode";
  public static final String REMOVE_NODE = "removeNode";
  public static final String CLUSTER_DESTROY = "clusterDestroy";
  public static final String CLUSTER_ENTITY = "cluster";
  public static final String CLUSTER_UUID_ATTRIBUTE = "_cluster_uuid_";
  public static final String CLUSTER_PROFILE_ENTITY = "cluster_profile";
  public static final String STORAGE_SUMMARY_ENTITY = "storage_summary";
  public static final String CLUSTER_FAULT_TOLERANT_CAPACITY = "cluster_fault_tolerant_capacity";
  public static final String CLUSTER_PROFILE_NAME = "name";
  public static final String ENTER_HOST_MAINTENANCE = "enterHostMaintenance";
  public static final String EXIT_HOST_MAINTENANCE = "exitHostMaintenance";
  public static final String COMPUTE_NON_MIGRATABLE_VMS = "computeNonMigratableVms";

  // setting mapper object
  public static final ObjectMapper mapper = JsonUtils.getObjectMapper();

  // Genesis operation names.
  public static enum GenesisOp {
    DISCOVER_NODES("discover_unconfigured_nodes_async"),
    NODE_NETWORKING_DETAILS("get_cluster_networks_and_uplinks_for_nodes_async"),
    EXPAND_CLUSTER("expand_cluster"),
    ENTER_HOST_MAINTENANCE("host_enter_maintenance"),
    EXIT_HOST_MAINTENANCE("host_exit_maintenance"),
    COMPUTE_NON_MIGRATABLE_VMS("compute_non_migratable_vms"),
    IS_HYPERVISOR_UPLOAD_REQUIRED("get_hypervisors_for_upload"),
    IS_BUNDLE_COMPATIBLE("validate_hypervisor_bundle_compatibility"),
    REMOVE_NODES("remove_nodes"),
    VALIDATE_UPLINKS("validate_uplink_info_for_nodes");

    private String genesisMethodName;
    GenesisOp(final String genesisMethodName) {
      this.genesisMethodName = genesisMethodName;
    }
    public String getGenesisMethodName() {
      return genesisMethodName;
    }
  }

  //Circuit Breaker constants
  public static final Integer SLIDING_WINDOW_SIZE = 100;
  public static final Integer FAILURE_THRESHOLD = 50;//in percentage
  public static final Integer WAIT_DURATION_IN_OPEN_STATE = 60;//in seconds
  public static final Integer NUMBER_OF_CALLS_IN_HALF_OPEN_STATE = 10;

  public final static ImmutableList<String> hostAttributeList = ImmutableList.of(
    "node_name", "host_type", "cpu_model", "num_cpu_cores",
    "num_cpu_sockets", "num_cpu_threads", "capacity.cpu_capacity_hz", "_cluster_uuid_",
    "cluster_name", "hypervisor_full_name", "hypervisor_type", "num_vms",
    "gpu_driver_version", "host_gpu_list", "default_vhd_location", "default_vhd_container_id",
    "default_vhd_container_uuid", "default_vm_location", "default_vm_container_id", "default_vm_container_uuid",
    "reboot_pending", "failover_cluster_fqdn", "failover_cluster_node_status", "boot_time_usecs",
    "memory_size_bytes", "cpu_frequency_hz", "block_serial", "block_model_name", "host_maintenance_state", "node_status");

  public final static ImmutableList<String> clusterAttributeList = ImmutableList.of(
    "cluster_name", "external_data_services_ip", "cluster_arch", "service_list",
    "timezone", "hypervisor_types", "redundancy_factor", "ncc_version", "cluster_upgrade_status", "num_vms",
    "capacity.inefficient_vm_num", "encryption_option", "encryption_scope", "key_management_server", "encryption_in_transit",
    "name_server_ip_list", "ntp_server_ip_list", "smtp_server.server.ip_address", "smtp_server.server.port",
    "smtp_server.server.user_name", "smtp_server.email_address", "smtp_server.type", "cluster_profile_uuid",
    "is_available","backup_eligibility_score");

  public final static ImmutableList<String> hostGpuAttributeList =  ImmutableList.of(
    "node", "cluster", "cluster_name", "host_id",
    "num_vgpus_allocated", "vm_uuid_list", "gpu_type", "gpu_mode",
    "vendor_name", "device_id", "device_name", "sbdf", "in_use",
    "assignable", "numa_node", "guest_driver_version", "fraction",
    "license_list", "num_virtual_display_heads", "max_resolution", "frame_buffer_size_bytes",
    "max_instances_per_vm");

  public final static ImmutableList<String> hostNicAttributeList =  ImmutableList.of(
    "switch_mgmt_ip_address", "switch_port_id", "switch_dev_id", "mac_address",
    "ipv6_addresses", "ipv4_addresses", "discovery_protocol", "dhcp_enabled", "switch_vlan_id",
    "description", "mtu_bytes", "status", "link_speed_kbps", "switch_mac_addr",
    "node", "port_name", "switch_hardware_platform", "tx_ring_size", "rx_ring_size",
    "cluster_uuid", "link_capacity", "nic_profile_id", "supported_capabilities",
    "driver_version", "firmware_version");

  public final static ImmutableList<String> virtualNicAttributeList =  ImmutableList.of(
    "mac_address", "ipv6_addresses", "ipv4_addresses", "dhcp_enabled", "description",
    "mtu_bytes", "status", "link_speed_kbps", "name", "node", "host_nic_uuids",
    "vlan_id");

  public final static ImmutableList<String> networkSwitchIntfAttributeList =  ImmutableList.of(
    "state_last_change_time", "type", "switch_management_address", "port_num", "index",
    "speed_kbps", "mtu_bytes", "description", "name", "host_nic_uuids", "physical_address",
    "switch_uuid", "node");

  //required statistics attribute for cluster entity can be added below
  public final static ImmutableList<String> clusterStatsAttributeList =  ImmutableList.of(
    "controller_avg_io_latency_usecs","capacity.controller_avg_io_latency_usecs.upper_buff","capacity.controller_avg_io_latency_usecs.lower_buff",
    "controller_avg_read_io_latency_usecs","capacity.controller_avg_read_io_latency_usecs.upper_buff","capacity.controller_avg_read_io_latency_usecs.lower_buff",
    "controller_avg_write_io_latency_usecs","capacity.controller_avg_write_io_latency_usecs.upper_buff","capacity.controller_avg_write_io_latency_usecs.lower_buff",
    "hypervisor_cpu_usage_ppm","capacity.hypervisor_cpu_usage_ppm.upper_buff","capacity.hypervisor_cpu_usage_ppm.lower_buff",
    "aggregate_hypervisor_memory_usage_ppm","capacity.aggregate_hypervisor_memory_usage_ppm.upper_buff","capacity.aggregate_hypervisor_memory_usage_ppm.lower_buff",
    "controller_num_iops","capacity.controller_num_iops.upper_buff","capacity.controller_num_iops.lower_buff",
    "controller_num_read_iops","capacity.controller_num_read_iops.upper_buff","capacity.controller_num_read_iops.lower_buff",
    "controller_num_write_iops","capacity.controller_num_write_iops.upper_buff","capacity.controller_num_write_iops.lower_buff",
    "controller_io_bandwidth_kBps","capacity.controller_io_bandwidth_kBps.upper_buff","capacity.controller_io_bandwidth_kBps.lower_buff",
    "controller_read_io_bandwidth_kBps","capacity.controller_read_io_bandwidth_kBps.upper_buff","capacity.controller_read_io_bandwidth_kBps.lower_buff",
    "controller_write_io_bandwidth_kBps","capacity.controller_write_io_bandwidth_kBps.upper_buff","capacity.controller_write_io_bandwidth_kBps.lower_buff",
    "storage.usage_bytes","storage.capacity_bytes","storage.free_bytes","storage.logical_usage_bytes","overall_memory_usage_bytes",
    "check.score","storage.recycle_bin_usage_bytes","storage.snapshot_reclaimable_bytes","data_reduction.overall.saved_bytes", "data_reduction.overall.saving_ratio_ppm",
    "capacity.cpu_capacity_hz", "capacity.cpu_usage_hz", "capacity.memory_capacity_bytes", "power_consumption_instant_watt");

  //required statistics attribute for host entity can be added below
  public final static ImmutableList<String> hostStatsAttributeList =  ImmutableList.of(
    "controller_avg_io_latency_usecs","capacity.controller_avg_io_latency_usecs.upper_buff","capacity.controller_avg_io_latency_usecs.lower_buff",
    "controller_avg_read_io_latency_usecs","capacity.controller_avg_read_io_latency_usecs.upper_buff","capacity.controller_avg_read_io_latency_usecs.lower_buff",
    "controller_avg_write_io_latency_usecs","capacity.controller_avg_write_io_latency_usecs.upper_buff","capacity.controller_avg_write_io_latency_usecs.lower_buff",
    "hypervisor_cpu_usage_ppm","capacity.hypervisor_cpu_usage_ppm.upper_buff","capacity.hypervisor_cpu_usage_ppm.lower_buff",
    "aggregate_hypervisor_memory_usage_ppm","capacity.aggregate_hypervisor_memory_usage_ppm.upper_buff","capacity.aggregate_hypervisor_memory_usage_ppm.lower_buff",
    "overall_memory_usage_ppm","capacity.overall_memory_usage_ppm.upper_buff","capacity.overall_memory_usage_ppm.lower_buff",
    "controller_num_iops","capacity.controller_num_iops.upper_buff","capacity.controller_num_iops.lower_buff",
    "controller_num_read_iops","capacity.controller_num_read_iops.upper_buff","capacity.controller_num_read_iops.lower_buff",
    "controller_num_write_iops","capacity.controller_num_write_iops.upper_buff","capacity.controller_num_write_iops.lower_buff",
    "controller_io_bandwidth_kBps","capacity.controller_io_bandwidth_kBps.upper_buff","capacity.controller_io_bandwidth_kBps.lower_buff",
    "controller_read_io_bandwidth_kBps","capacity.controller_read_io_bandwidth_kBps.upper_buff","capacity.controller_read_io_bandwidth_kBps.lower_buff",
    "controller_write_io_bandwidth_kBps","capacity.controller_write_io_bandwidth_kBps.upper_buff","capacity.controller_write_io_bandwidth_kBps.lower_buff",
    "storage.usage_bytes","storage.capacity_bytes","storage.free_bytes","memory_size_bytes","cpu.capacity_hz",
    "check.score", "storage.logical_usage_bytes", "capacity.cpu_usage_hz", "overall_memory_usage_bytes", "power_consumption_instant_watt");

  //required attribute for compute_non_migratable_vms_entity can be added below
  public final static ImmutableList<String> nonMigratableVmsAttributeList =  ImmutableList.of(
    "non_migratable_vms_list");

  public static final String DEFAULT_COMMUNITY_STRING = "public";
  public static final String INSIGHTS_CAPABILITIES = "infra_capabilities";
  public static final String CAPS_JSON_ATTRIBUTE = "caps_json";

  public final static Pattern NUTANIX_SW_VERSION =
    Pattern.compile(new StringBuilder()
        .append("^               # Beginning of the line.\n")
        .append("([\\S&&[^-]]+)  # Platform version.\n")
        .append("-               # Separator after platform version.\n")
        .append("([\\S&&[^-]]+)  # Build type.\n")
        .append("-               # Separator after the build type.\n")
        .append("(\\S+)          # Software version.\n")
        .append("-               # Separator before commit id.\n")
        .append("([\\S&&[^-]]+)  # Git commit id.\n")
        .append("$               # End of the line.\n").toString(),
      Pattern.CASE_INSENSITIVE | Pattern.COMMENTS);

  // Group indices for various parts in the version string
  public final static int GROUP_IDX_BUILD_TYPE = 2;
  public final static int GROUP_IDX_VERSION = 3;
  public final static int GROUP_IDX_COMMIT_ID = 4;
  public final static short SHORT_COMMIT_ID_END_IDX = 6;

  public final static ImmutableMap<String, ClusterFunctionRef> clusterFunctionRefHashMap
    = new ImmutableMap.Builder<String, ClusterFunctionRef>()
    .put("AOS", ClusterFunctionRef.AOS)
    .put("PRISM_CENTRAL", ClusterFunctionRef.PRISM_CENTRAL)
    .put("CLOUD_DATA_GATEWAY", ClusterFunctionRef.CLOUD_DATA_GATEWAY)
    .put("AFS", ClusterFunctionRef.AFS)
    .put("ONE_NODE", ClusterFunctionRef.ONE_NODE)
    .put("TWO_NODE", ClusterFunctionRef.TWO_NODE)
    .put("ANALYTICS_PLATFORM", ClusterFunctionRef.ANALYTICS_PLATFORM)
    .build();

  public final static ImmutableMap<ClusterFunctionRef,ConfigurationProto.ClusterFunctions> clusterFunctionProtoHashMapForCreateCluster
    = new ImmutableMap.Builder<ClusterFunctionRef,ConfigurationProto.ClusterFunctions>()
    .put(ClusterFunctionRef.AOS, ConfigurationProto.ClusterFunctions.kNDFS)
    .put(ClusterFunctionRef.ONE_NODE, ConfigurationProto.ClusterFunctions.kOneNodeCluster)
    .put(ClusterFunctionRef.TWO_NODE, ConfigurationProto.ClusterFunctions.kTwoNodeCluster)
    .build();

  public final static ImmutableMap<String, HypervisorType> hypervisorTypeMap
    = new ImmutableMap.Builder<String, HypervisorType>()
    .put("kKvm", HypervisorType.AHV)
    .put("kHyperv", HypervisorType.HYPERV)
    .put("kVMware", HypervisorType.ESX)
    .put("kXen", HypervisorType.XEN)
    .put("kNativeHost", HypervisorType.NATIVEHOST)
    .put("kNull", HypervisorType.$UNKNOWN)
    .build();

  public final static ImmutableMap<HypervisorType, String> hypervisorTypeToStringMap
    = new ImmutableMap.Builder<HypervisorType, String>()
    .put(HypervisorType.AHV, "kvm")
    .put(HypervisorType.HYPERV, "hyperv")
    .put(HypervisorType.ESX, "esx")
    .put(HypervisorType.XEN, "xen")
     .put(HypervisorType.NATIVEHOST, "nativehost")
    .build();

  public final static ImmutableMap<Long, HypervisorType> LongTohypervisorTypeMap
    = new ImmutableMap.Builder<Long, HypervisorType>()
    .put(0L, HypervisorType.ESX)
    .put(1L, HypervisorType.XEN)
    .put(2L, HypervisorType.HYPERV)
    .put(3L, HypervisorType.AHV)
    .put(9998L, HypervisorType.NATIVEHOST)
    .put(9999L, HypervisorType.$UNKNOWN)
    .build();

  public final static ImmutableMap<String, HostTypeEnum> hostTypeMap
    = new ImmutableMap.Builder<String, HostTypeEnum>()
    .put("kHyperConverged", HostTypeEnum.HYPER_CONVERGED)
    .put("kComputeOnly", HostTypeEnum.COMPUTE_ONLY)
    .put("kNeverSchedulable", HostTypeEnum.STORAGE_ONLY)
    .build();

  public final static ImmutableMap<DomainType, DomainAwarenessLevel> domainAwarenessMap
    = new ImmutableMap.Builder<DomainType, DomainAwarenessLevel>()
    .put(DomainType.kNode, DomainAwarenessLevel.NODE)
    .put(DomainType.kRack, DomainAwarenessLevel.RACK)
    .put(DomainType.kRackableUnit, DomainAwarenessLevel.BLOCK)
    .put(DomainType.kDisk, DomainAwarenessLevel.DISK)
    .build();

  public final static ImmutableMap<ConfigurationProto.OperationMode, OperationMode> operationModeMap
    = new ImmutableMap.Builder<ConfigurationProto.OperationMode, OperationMode>()
    .put(ConfigurationProto.OperationMode.kNormal, OperationMode.NORMAL)
    .put(ConfigurationProto.OperationMode.kOverride, OperationMode.OVERRIDE)
    .put(ConfigurationProto.OperationMode.kReadOnly, OperationMode.READ_ONLY)
    .put(ConfigurationProto.OperationMode.kStandAlone, OperationMode.STAND_ALONE)
    .put(ConfigurationProto.OperationMode.kSwitchToTwoNode, OperationMode.SWITCH_TO_TWO_NODE)
    .build();

  public final static ImmutableMap<ConfigurationProto.Aegis.AutoSupportConfig.PIIScrubbingLevel, PIIScrubbingLevel> piiScrubbingLevelMap
    = new ImmutableMap.Builder<ConfigurationProto.Aegis.AutoSupportConfig.PIIScrubbingLevel, PIIScrubbingLevel>()
    .put(ConfigurationProto.Aegis.AutoSupportConfig.PIIScrubbingLevel.kAll, PIIScrubbingLevel.ALL)
    .put(ConfigurationProto.Aegis.AutoSupportConfig.PIIScrubbingLevel.kAuto, PIIScrubbingLevel.DEFAULT)
    .put(ConfigurationProto.Aegis.AutoSupportConfig.PIIScrubbingLevel.kPartial, PIIScrubbingLevel.DEFAULT)
    .build();

  public final static ImmutableMap<SmtpServerType, SmtpType> smtpServerTypeMap
    = new ImmutableMap.Builder<SmtpServerType, SmtpType>()
    .put(SmtpServerType.kPlain, SmtpType.PLAIN)
    .put(SmtpServerType.kSSL, SmtpType.SSL)
    .put(SmtpServerType.kStartTLS, SmtpType.STARTTLS)
    .build();

  public final static ImmutableMap<String, SmtpType> smtpStrTypeMap
    = new ImmutableMap.Builder<String, SmtpType>()
    .put("PLAIN", SmtpType.PLAIN)
    .put("SSL", SmtpType.SSL)
    .put("STARTTLS", SmtpType.STARTTLS)
    .build();

  public final static ImmutableMap<String, HttpProxyType> httpProxyTypeStrMap
    = new ImmutableMap.Builder<String, HttpProxyType>()
    .put("HTTP", HttpProxyType.HTTP)
    .put("HTTPS", HttpProxyType.HTTPS)
    .put("SOCKS", HttpProxyType.SOCKS)
    .build();

  public final static ImmutableMap<String, HttpProxyWhiteListTargetType> httpProxyWhiteListTargetTypeStrMap
    = new ImmutableMap.Builder<String, HttpProxyWhiteListTargetType>()
    .put("IPV4_ADDRESS", HttpProxyWhiteListTargetType.IPV4_ADDRESS)
    .put("IPV6_ADDRESS", HttpProxyWhiteListTargetType.IPV6_ADDRESS)
    .put("IPV4_NETWORK_MASK", HttpProxyWhiteListTargetType.IPV4_NETWORK_MASK)
    .put("DOMAIN_NAME_SUFFIX", HttpProxyWhiteListTargetType.DOMAIN_NAME_SUFFIX)
    .put("HOST_NAME", HttpProxyWhiteListTargetType.HOST_NAME)
    .build();

  public final static ImmutableMap<String, StorageTierReference> storageTierMap
    = new ImmutableMap.Builder<String, StorageTierReference>()
    .put("SSD-SATA", StorageTierReference.SATA_SSD)
    .put("PCIE-SSD", StorageTierReference.PCIE_SSD)
    .put("DAS-SATA", StorageTierReference.HDD)
    .build();

  public final static ImmutableMap<AuthType, SnmpAuthType> snmpAuthTypeMap
    = new ImmutableMap.Builder<AuthType, SnmpAuthType>()
    .put(AuthType.kMD5, SnmpAuthType.MD5)
    .put(AuthType.kSHA, SnmpAuthType.SHA)
    .build();

  public final static ImmutableMap<PrivType, SnmpPrivType> snmpPrivTypeMap
    = new ImmutableMap.Builder<PrivType, SnmpPrivType>()
    .put(PrivType.kAES, SnmpPrivType.AES)
    .put(PrivType.kDES, SnmpPrivType.DES)
    .build();

  public final static ImmutableMap<Protocol, SnmpProtocol> snmpProtocolMap
    = new ImmutableMap.Builder<Protocol, SnmpProtocol>()
    .put(Protocol.kTCP, SnmpProtocol.TCP)
    .put(Protocol.kTCP6, SnmpProtocol.TCP6)
    .put(Protocol.kUDP, SnmpProtocol.UDP)
    .put(Protocol.kUDP6, SnmpProtocol.UDP6)
    .build();

  public final static ImmutableMap<SnmpVersion, SnmpTrapVersion> snmpTrapVersionMap
    = new ImmutableMap.Builder<SnmpVersion, SnmpTrapVersion>()
    .put(SnmpVersion.kV2, SnmpTrapVersion.V2)
    .put(SnmpVersion.kV3, SnmpTrapVersion.V3)
    .build();

  public final static ImmutableMap<ProtocolType, RsyslogNetworkProtocol> rsyslogNetworkProtocolMap
    = new ImmutableMap.Builder<ProtocolType, RsyslogNetworkProtocol>()
    .put(ProtocolType.kTcp, RsyslogNetworkProtocol.TCP)
    .put(ProtocolType.kUdp, RsyslogNetworkProtocol.UDP)
    .build();

  public final static ImmutableMap<Priority, RsyslogModuleLogSeverityLevel> rsyslogZeusConfigToSyslogMap
    = new ImmutableMap.Builder<Priority, RsyslogModuleLogSeverityLevel>()
    .put(Priority.kDebug, RsyslogModuleLogSeverityLevel.DEBUG)
    .put(Priority.kInfo, RsyslogModuleLogSeverityLevel.INFO)
    .put(Priority.kNotice, RsyslogModuleLogSeverityLevel.NOTICE)
    .put(Priority.kWarning, RsyslogModuleLogSeverityLevel.WARNING)
    .put(Priority.kError, RsyslogModuleLogSeverityLevel.ERROR)
    .put(Priority.kCritical, RsyslogModuleLogSeverityLevel.CRITICAL)
    .put(Priority.kAlert, RsyslogModuleLogSeverityLevel.ALERT)
    .put(Priority.kEmergency, RsyslogModuleLogSeverityLevel.EMERGENCY)
    .build();

  public final static ImmutableMap<RsyslogModuleLogSeverityLevel, Integer> syslogMapToPriorityNumber
    = new ImmutableMap.Builder<RsyslogModuleLogSeverityLevel, Integer>()
    .put(RsyslogModuleLogSeverityLevel.EMERGENCY, 0)
    .put(RsyslogModuleLogSeverityLevel.ALERT, 1)
    .put(RsyslogModuleLogSeverityLevel.CRITICAL, 2)
    .put(RsyslogModuleLogSeverityLevel.ERROR, 3)
    .put(RsyslogModuleLogSeverityLevel.WARNING, 4)
    .put(RsyslogModuleLogSeverityLevel.NOTICE, 5)
    .put(RsyslogModuleLogSeverityLevel.INFO, 6)
    .put(RsyslogModuleLogSeverityLevel.DEBUG, 7)
    .build();

  public final static ImmutableMap<String, RsyslogModuleName> rsyslogModuleNameMap
    = new ImmutableMap.Builder<String, RsyslogModuleName>()
    .put("cassandra", RsyslogModuleName.CASSANDRA)
    .put("calm", RsyslogModuleName.CALM)
    .put("acropolis", RsyslogModuleName.ACROPOLIS)
    .put("curator", RsyslogModuleName.CURATOR)
    .put("cerebro", RsyslogModuleName.CEREBRO)
    .put("genesis", RsyslogModuleName.GENESIS)
    .put("prism", RsyslogModuleName.PRISM)
    .put("stargate", RsyslogModuleName.STARGATE)
    .put("syslog_module", RsyslogModuleName.SYSLOG_MODULE)
    .put("zookeeper", RsyslogModuleName.ZOOKEEPER)
    .put("uhara", RsyslogModuleName.UHARA)
    .put("lazan", RsyslogModuleName.LAZAN)
    .put("api_audit", RsyslogModuleName.API_AUDIT)
    .put("audit", RsyslogModuleName.AUDIT)
    .put("epsilon", RsyslogModuleName.EPSILON)
    .put("minerva_cvm", RsyslogModuleName.MINERVA_CVM)
    .put("flow", RsyslogModuleName.FLOW)
    .put("flow_service_logs", RsyslogModuleName.FLOW_SERVICE_LOGS)
    .put("lcm", RsyslogModuleName.LCM)
    .put("aplos", RsyslogModuleName.APLOS)
    .build();

  public final static ImmutableMap<RsyslogNetworkProtocol, String> rsyslogNetworkProtocolStringImmutableMap
    = new ImmutableMap.Builder<RsyslogNetworkProtocol, String>()
    .put(RsyslogNetworkProtocol.TCP, "TCP")
    .put(RsyslogNetworkProtocol.UDP, "UDP")
    .put(RsyslogNetworkProtocol.RELP, "RELP")
    .build();

  public final static ImmutableMap<RackableUnit.Model, RackableUnitModel> rackableUnitModelMap
    = new ImmutableMap.Builder<RackableUnit.Model, RackableUnitModel>()
    .put(RackableUnit.Model.kDesktop, RackableUnitModel.DESKTOP)
    .put(RackableUnit.Model.kNX1020, RackableUnitModel.NX1020)
    .put(RackableUnit.Model.kNX1050, RackableUnitModel.NX1050)
    .put(RackableUnit.Model.kNX2000, RackableUnitModel.NX2000)
    .put(RackableUnit.Model.kNX3000, RackableUnitModel.NX3000)
    .put(RackableUnit.Model.kNX3050, RackableUnitModel.NX3050)
    .put(RackableUnit.Model.kNX3060, RackableUnitModel.NX3060)
    .put(RackableUnit.Model.kNX6020, RackableUnitModel.NX6020)
    .put(RackableUnit.Model.kNX6050, RackableUnitModel.NX6050)
    .put(RackableUnit.Model.kNX6060, RackableUnitModel.NX6060)
    .put(RackableUnit.Model.kNX6070, RackableUnitModel.NX6070)
    .put(RackableUnit.Model.kNX6080, RackableUnitModel.NX6080)
    .put(RackableUnit.Model.kNX7110, RackableUnitModel.NX7110)
    .put(RackableUnit.Model.kNX9040, RackableUnitModel.NX9040)
    .put(RackableUnit.Model.kUseLayout, RackableUnitModel.USELAYOUT)
    .put(RackableUnit.Model.kNull, RackableUnitModel.NULLVALUE)
    .build();

  public final static ImmutableMap<DomainType, dp1.clu.clustermgmt.v4.config.DomainType> domainTypeMap
    = new ImmutableMap.Builder<DomainType, dp1.clu.clustermgmt.v4.config.DomainType>()
    .put(DomainType.kUnknown, dp1.clu.clustermgmt.v4.config.DomainType.$UNKNOWN)
    .put(DomainType.kNode, dp1.clu.clustermgmt.v4.config.DomainType.NODE)
    .put(DomainType.kRackableUnit, dp1.clu.clustermgmt.v4.config.DomainType.RACKABLE_UNIT)
    .put(DomainType.kRack, dp1.clu.clustermgmt.v4.config.DomainType.RACK)
    .put(DomainType.kCluster, dp1.clu.clustermgmt.v4.config.DomainType.CLUSTER)
    .put(DomainType.kDisk, dp1.clu.clustermgmt.v4.config.DomainType.DISK)
    .put(DomainType.kCustom, dp1.clu.clustermgmt.v4.config.DomainType.CUSTOM)
    .build();

  public final static ImmutableMap<ComponentType, dp1.clu.clustermgmt.v4.config.ComponentType> componentTypeMap
    = new ImmutableMap.Builder<ComponentType, dp1.clu.clustermgmt.v4.config.ComponentType>()
    .put(ComponentType.kUnknown, dp1.clu.clustermgmt.v4.config.ComponentType.$UNKNOWN)
    .put(ComponentType.kCassandraRing, dp1.clu.clustermgmt.v4.config.ComponentType.CASSANDRA_RING)
    .put(ComponentType.kErasureCodeStripSize, dp1.clu.clustermgmt.v4.config.ComponentType.ERASURE_CODE_STRIP_SIZE)
    .put(ComponentType.kExtentGroupReplicas, dp1.clu.clustermgmt.v4.config.ComponentType.EXTENT_GROUP_REPLICAS)
    .put(ComponentType.kFreeSpace, dp1.clu.clustermgmt.v4.config.ComponentType.FREE_SPACE)
    .put(ComponentType.kOplogEpisodes, dp1.clu.clustermgmt.v4.config.ComponentType.OPLOG_EPISODES)
    .put(ComponentType.kStargateHealth, dp1.clu.clustermgmt.v4.config.ComponentType.STARGATE_HEALTH)
    .put(ComponentType.kStaticConfig, dp1.clu.clustermgmt.v4.config.ComponentType.STATIC_CONFIG)
    .put(ComponentType.kZookeeperInstances, dp1.clu.clustermgmt.v4.config.ComponentType.ZOOKEPER_INSTANCES)
    .build();

  public final static ImmutableMap<String, ManagementServerType> managementServerType
    = new ImmutableMap.Builder<String, ManagementServerType>()
    .put("vcenter", ManagementServerType.VCENTER)
    .build();

  public final static ImmutableMap<AcropolisNodeState, HypervisorState> hypervisorState
    = new ImmutableMap.Builder<AcropolisNodeState, HypervisorState>()
    .put(AcropolisNodeState.kAcropolisNormal, HypervisorState.ACROPOLIS_NORMAL)
    .put(AcropolisNodeState.kEnteredMaintenanceMode, HypervisorState.ENTERED_MAINTENANCE_MODE)
    .put(AcropolisNodeState.kEnteringMaintenanceMode, HypervisorState.ENTERING_MAINTENANCE_MODE)
    .put(AcropolisNodeState.kEnteringMaintenanceModeFromHAFailover, HypervisorState.ENTERING_MAINTENANCE_MODE_FROM_HA_FAILOVER)
    .put(AcropolisNodeState.kHAFailoverSource, HypervisorState.HA_FAILOVER_SOURCE)
    .put(AcropolisNodeState.kHAFailoverTarget, HypervisorState.HA_FAILOVER_TARGET)
    .put(AcropolisNodeState.kHAHealingSource, HypervisorState.HA_HEALING_SOURCE)
    .put(AcropolisNodeState.kHAHealingTarget, HypervisorState.HA_HEALING_TARGET)
    .put(AcropolisNodeState.kReservedForHAFailover, HypervisorState.RESERVED_FOR_HA_FAILOVER)
    .put(AcropolisNodeState.kReservingForHAFailover, HypervisorState.RESERVING_FOR_HA_FAILOVER)
    .build();

  public final static ImmutableMap<AcropolisStatus.AcropolisConnectionState, AcropolisConnectionState> acropolisConnectionState
    = new ImmutableMap.Builder<AcropolisStatus.AcropolisConnectionState, AcropolisConnectionState>()
    .put(AcropolisStatus.AcropolisConnectionState.kConnected, AcropolisConnectionState.CONNECTED)
    .put(AcropolisStatus.AcropolisConnectionState.kDisconnected, AcropolisConnectionState.DISCONNECTED)
    .build();

  public final static ImmutableMap<String, GpuType> gpuTypeMap
    = new ImmutableMap.Builder<String, GpuType>()
    .put("kPassthroughGraphics", GpuType.PASSTHROUGH_GRAPHICS)
    .put("kPassthroughCompute", GpuType.PASSTHROUGH_COMPUTE)
    .put("kVirtual", GpuType.VIRTUAL)
    .build();

  public final static ImmutableMap<String, GpuMode> gpuModeMap
    = new ImmutableMap.Builder<String, GpuMode>()
    .put("kUnused", GpuMode.UNUSED)
    .put("kUsedForPassthrough", GpuMode.USED_FOR_PASSTHROUGH)
    .put("kUsedForVirtual", GpuMode.USED_FOR_VIRTUAL)
    .build();

  public final static ImmutableMap<AddressType, String> addressTypeMap
    = new ImmutableMap.Builder<AddressType, String>()
    .put(AddressType.IPV4, "IPv4")
    .put(AddressType.IPV6, "IPv6")
    .build();

  public final static ImmutableMap<String, Integer> a2capabilityMap
    = new ImmutableMap.Builder<String, Integer>()
    .put(GET_SNMP_CONFIG, GenesisInterfaceProto.GenesisApiVersion.kPutCallSupportedVersion.getNumber())
    .put(GET_SNMP_USER, GenesisInterfaceProto.GenesisApiVersion.kPutCallSupportedVersion.getNumber())
    .put(GET_SNMP_TRAP, GenesisInterfaceProto.GenesisApiVersion.kPutCallSupportedVersion.getNumber())
    .put(GET_RSYSLOG_SERVER, GenesisInterfaceProto.GenesisApiVersion.kPutCallSupportedVersion.getNumber())
    .put(GET_RSYSLOG_CONFIG, GenesisInterfaceProto.GenesisApiVersion.kPutCallSupportedVersion.getNumber())
    .put(RENAME_HOST, GenesisInterfaceProto.GenesisApiVersion.kPutCallSupportedVersion.getNumber())
    .put(UPDATE_CLUSTER, GenesisInterfaceProto.GenesisApiVersion.kPutCallSupportedVersion.getNumber())
    .put(UPDATE_SNMP_STATUS, GenesisInterfaceProto.GenesisApiVersion.kPutCallSupportedVersion.getNumber())
    .put(ADD_SNMP_TRANSPORT, GenesisInterfaceProto.GenesisApiVersion.kPutCallSupportedVersion.getNumber())
    .put(REMOVE_SNMP_TRANSPORT, GenesisInterfaceProto.GenesisApiVersion.kPutCallSupportedVersion.getNumber())
    .put(ADD_SNMP_USER, GenesisInterfaceProto.GenesisApiVersion.kPutCallSupportedVersion.getNumber())
    .put(UPDATE_SNMP_USER, GenesisInterfaceProto.GenesisApiVersion.kPutCallSupportedVersion.getNumber())
    .put(DELETE_SNMP_USER, GenesisInterfaceProto.GenesisApiVersion.kPutCallSupportedVersion.getNumber())
    .put(ADD_SNMP_TRAP, GenesisInterfaceProto.GenesisApiVersion.kPutCallSupportedVersion.getNumber())
    .put(UPDATE_SNMP_TRAP, GenesisInterfaceProto.GenesisApiVersion.kPutCallSupportedVersion.getNumber())
    .put(DELETE_SNMP_TRAP, GenesisInterfaceProto.GenesisApiVersion.kPutCallSupportedVersion.getNumber())
    .put(ADD_RSYSLOG_SERVER, GenesisInterfaceProto.GenesisApiVersion.kPutCallSupportedVersion.getNumber())
    .put(UPDATE_RSYSLOG_SERVER, GenesisInterfaceProto.GenesisApiVersion.kPutCallSupportedVersion.getNumber())
    .put(DELETE_RSYSLOG_SERVER, GenesisInterfaceProto.GenesisApiVersion.kPutCallSupportedVersion.getNumber())
    .put(DISCOVER_UNCONFIGURED_NODES, GenesisInterfaceProto.GenesisApiVersion.kPutCallSupportedVersion.getNumber())
    .put(GET_NETWORKING_DETAILS, GenesisInterfaceProto.GenesisApiVersion.kPutCallSupportedVersion.getNumber())
    .put(ADD_NODE, GenesisInterfaceProto.GenesisApiVersion.kPutCallSupportedVersion.getNumber())
    .put(IS_HYPERVISOR_UPLOAD_REQ, GenesisInterfaceProto.GenesisApiVersion.kPutCallSupportedVersion.getNumber())
    .put(VALIDATE_NODE, GenesisInterfaceProto.GenesisApiVersion.kPutCallSupportedVersion.getNumber())
    .put(REMOVE_NODE, GenesisInterfaceProto.GenesisApiVersion.kPutCallSupportedVersion.getNumber())
    .build();

  public final static ImmutableMap<String, Integer> b1capabilityMap
    = new ImmutableMap.Builder<String, Integer>()
    .put(GET_SNMP_CONFIG, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(GET_SNMP_USER, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(GET_SNMP_TRAP, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(GET_RSYSLOG_SERVER, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(GET_RSYSLOG_CONFIG, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(RENAME_HOST, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(UPDATE_CLUSTER, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(UPDATE_SNMP_STATUS, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(ADD_SNMP_TRANSPORT, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(REMOVE_SNMP_TRANSPORT, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(ADD_SNMP_USER, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(UPDATE_SNMP_USER, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(DELETE_SNMP_USER, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(ADD_SNMP_TRAP, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(UPDATE_SNMP_TRAP, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(DELETE_SNMP_TRAP, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(ADD_RSYSLOG_SERVER, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(UPDATE_RSYSLOG_SERVER, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(DELETE_RSYSLOG_SERVER, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(DISCOVER_UNCONFIGURED_NODES, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(GET_NETWORKING_DETAILS, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(ADD_NODE, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(IS_HYPERVISOR_UPLOAD_REQ, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(VALIDATE_NODE, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(REMOVE_NODE, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(CLUSTER_DESTROY, GenesisInterfaceProto.GenesisApiVersion.kAsyncUpdateClusterSupportedVersion.getNumber())
    .build();

  public final static ImmutableMap<String, Integer> b2capabilityMap
    = new ImmutableMap.Builder<String, Integer>()
    .put(GET_SNMP_CONFIG, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(GET_SNMP_USER, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(GET_SNMP_TRAP, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(GET_RSYSLOG_SERVER, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(GET_RSYSLOG_CONFIG, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(RENAME_HOST, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(UPDATE_CLUSTER, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(UPDATE_SNMP_STATUS, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(ADD_SNMP_TRANSPORT, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(REMOVE_SNMP_TRANSPORT, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(ADD_SNMP_USER, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(UPDATE_SNMP_USER, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(DELETE_SNMP_USER, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(ADD_SNMP_TRAP, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(UPDATE_SNMP_TRAP, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(DELETE_SNMP_TRAP, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(ADD_RSYSLOG_SERVER, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(UPDATE_RSYSLOG_SERVER, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(DELETE_RSYSLOG_SERVER, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(DISCOVER_UNCONFIGURED_NODES, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(GET_NETWORKING_DETAILS, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(ADD_NODE, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(IS_HYPERVISOR_UPLOAD_REQ, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(VALIDATE_NODE, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(REMOVE_NODE, GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber())
    .put(CLUSTER_DESTROY, GenesisInterfaceProto.GenesisApiVersion.kAsyncUpdateClusterSupportedVersion.getNumber())
    .build();

  public final static ImmutableMap<String, Integer> r0capabilityMap = new ImmutableMap.Builder<String, Integer>()
    .put(ENTER_HOST_MAINTENANCE, GenesisInterfaceProto.GenesisApiVersion.kPlannedOutageManagerSupportedVersion.getNumber())
    .put(EXIT_HOST_MAINTENANCE, GenesisInterfaceProto.GenesisApiVersion.kPlannedOutageManagerSupportedVersion.getNumber())
    .put(COMPUTE_NON_MIGRATABLE_VMS, GenesisInterfaceProto.GenesisApiVersion.kPlannedOutageManagerSupportedVersion.getNumber())
    .putAll(b2capabilityMap).build();

  public final static ImmutableMap<String, ImmutableMap<String, Integer> > capabilityMap
    = new ImmutableMap.Builder<String, ImmutableMap<String, Integer> >()
    .put(V4_R0_A2_VERSION, a2capabilityMap)
    .put(V4_R0_B1_VERSION, b1capabilityMap)
    .put(V4_R0_B2_VERSION, b2capabilityMap)
    .put(V4_R0_VERSION, r0capabilityMap)
    .build();

  public final static ImmutableMap<String, EncryptionScopeInfo> encryptionScopeMap
    = new ImmutableMap.Builder<String, EncryptionScopeInfo>()
    .put("cluster", EncryptionScopeInfo.CLUSTER)
    .put("container", EncryptionScopeInfo.CONTAINER)
    .build();

  public final static ImmutableMap<String, EncryptionOptionInfo> encryptionOptionMap
    = new ImmutableMap.Builder<String, EncryptionOptionInfo>()
    .put("HW", EncryptionOptionInfo.HARDWARE)
    .put("SW", EncryptionOptionInfo.SOFTWARE)
    .put("SW+HW", EncryptionOptionInfo.SOFTWARE_AND_HARDWARE)
    .build();

  public final static ImmutableMap<String, UpgradeStatus> upgradeStatusMap
    = new ImmutableMap.Builder<String, UpgradeStatus>()
    .put("kPending", UpgradeStatus.PENDING)
    .put("kDownloading", UpgradeStatus.DOWNLOADING)
    .put("kQueued", UpgradeStatus.QUEUED)
    .put("kPreupgrade", UpgradeStatus.PREUPGRADE)
    .put("kUpgrading", UpgradeStatus.UPGRADING)
    .put("kSucceeded", UpgradeStatus.SUCCEEDED)
    .put("kFailed", UpgradeStatus.FAILED)
    .put("kCancelled", UpgradeStatus.CANCELLED)
    .put("kScheduled", UpgradeStatus.SCHEDULED)
    .build();

  public final static ImmutableMap<String, KeyManagementServerType> keyManagementServerTypeMap
    = new ImmutableMap.Builder<String, KeyManagementServerType>()
    .put("prism_central", KeyManagementServerType.PRISM_CENTRAL)
    .put("external", KeyManagementServerType.EXTERNAL)
    .put("local", KeyManagementServerType.LOCAL)
    .build();

  public final static ImmutableMap<String, EncryptionStatus> encryptionStatusMap
    = new ImmutableMap.Builder<String, EncryptionStatus>()
    .put("1", EncryptionStatus.ENABLED)
    .put("0", EncryptionStatus.DISABLED)
    .build();

  public final static ImmutableMap<String, NodeStatus> nodeStatusMap
    = new ImmutableMap.Builder<String, NodeStatus>()
    .put("kNormal", NodeStatus.NORMAL)
    .put("kToBeRemoved", NodeStatus.TO_BE_REMOVED)
    .put("kOkToBeRemoved", NodeStatus.OK_TO_BE_REMOVED)
    .put("kNewNode", NodeStatus.NEW_NODE)
    .put("kToBePreProtected", NodeStatus.TO_BE_PREPROTECTED)
    .put("kPreProtected", NodeStatus.PREPROTECTED)
    .build();

  /*
  This straight forward mapping can not be user as of now due to existing inconsistencty in zeus config proto
  desired_replica_placement_policy_id and current_replica_placement_policy_id
  are defined as int64 in FaultToleranceState instead of 'zeus.ReplicaPlacementPolicy.ID' in
  zeus proto. Once Zeus proto is fixed, we will move to this map to avoid manual errors in mappings
  public final static ImmutableMap<ReplicaPlacementPolicy.Id, ClusterFaultToleranceRef> ClusterFaultToleranceMap
          = new ImmutableMap.Builder<ReplicaPlacementPolicy.Id, ClusterFaultToleranceRef>()
          .put(ReplicaPlacementPolicy.Id.kRF1, ClusterFaultToleranceRef.CFT_0N_AND_0D)
          .put(ReplicaPlacementPolicy.Id.kRF2, ClusterFaultToleranceRef.CFT_1N_OR_1D)
          .put(ReplicaPlacementPolicy.Id.kRF3, ClusterFaultToleranceRef.CFT_2N_OR_2D)
          .put(ReplicaPlacementPolicy.Id.kRF3_1NOr2D, ClusterFaultToleranceRef.CFT_1N_OR_2D)
          .put(ReplicaPlacementPolicy.Id.kRF3_Adaptive, ClusterFaultToleranceRef.CFT_1N_AND_1D)
          .build();
   */
  public final static ImmutableMap<Long, ClusterFaultToleranceRef> ClusterFaultToleranceMap
          = new ImmutableMap.Builder<Long, ClusterFaultToleranceRef>()
          .put(Long.valueOf(1), ClusterFaultToleranceRef.CFT_0N_AND_0D)
          .put(Long.valueOf(2), ClusterFaultToleranceRef.CFT_1N_OR_1D)
          .put(Long.valueOf(3), ClusterFaultToleranceRef.CFT_2N_OR_2D)
          .put(Long.valueOf(5), ClusterFaultToleranceRef.CFT_1N_AND_1D)
          .build();

  public static final String ZPROTOBUF_ATTR = "__zprotobuf__";

  public static final String SNMP_USER_ENTITY = "snmp_user";

  public static final String SNMP_TRAP_ENTITY = "snmp_trap";

  public static final String SNMP_TRANSPORT_ENTITY = "snmp_transport";

  public static final String RSYSLOG_ENTITY = "remote_syslog_configuration";

  public static final String HTTP_PROXY_ENTITY = "http_proxy";

  public static final String HTTP_PROXY_WHITELIST_ENTITY = "http_proxy_whitelist";

  public static ZeusConfiguration getZkconfig(String clusterUuid,
                                              MulticlusterZeusConfigurationManagingZkImpl multiClusterZeusConfig)
    throws ClustermgmtServiceException {
    try {
      ZeusConfiguration zeusConfig =
        multiClusterZeusConfig.getZeusConfiguration(clusterUuid);
      return zeusConfig;
    }
    catch (ProtobufZNodeManagementException e) {
      throw new ClustermgmtZkConfigReadException("Failed to read zeus database " + e.getMessage());
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ClustermgmtZkConfigReadException("Failed to read zeus database " + e.getMessage());
    }
  }

  public static GetEntitiesWithMetricsRet getEntitiesWithMetricsRet(final Integer offset,
                                                                    final Integer limit,
                                                                    final String entityType,
                                                                    EntityDbProxyImpl entityDbProxy,
                                                                    final List<String> rawColumns,
                                                                    final String entityExtId,
                                                                    ClustermgmtResourceAdapterImpl clustermgmtResourceAdapter,
                                                                    InsightsInterfaceProto.BooleanExpression expression,
                                                                    InsightsInterfaceProto.QueryOrderBy orderBy,
                                                                    InsightsInterfaceProto.QueryGroupBy groupBy)
    throws ClustermgmtServiceException {
    GetEntitiesWithMetricsArg getEntitiesWithMetricsArg =
      clustermgmtResourceAdapter.adaptToGetEntitiesWithMetricsArg(entityExtId, entityType, offset, limit, rawColumns,
        orderBy, expression, groupBy);
    GetEntitiesWithMetricsRet getEntitiesWithMetricsRet =
      entityDbProxy.doExecute(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES_WITH_METRICS,
        getEntitiesWithMetricsArg);
    return getEntitiesWithMetricsRet;
  }

  public static GetEntitiesRet getInsightsEntityRet(final String extId,
                                                    final String entityType,
                                                    EntityDbProxyImpl entityDbProxy,
                                                    ClustermgmtResourceAdapterImpl clustermgmtResourceAdapter)
    throws ClustermgmtServiceException {
    GetEntitiesArg getEntitiesArg = clustermgmtResourceAdapter.adaptToGetEntitiesArg(extId, entityType);
    log.debug("The RPC Request GetEntitiesArg  {}", getEntitiesArg);
    GetEntitiesRet getEntitiesRet =
      entityDbProxy.doExecute(EntityDbProxyImpl.EntityDbRpcName.GET_ENTITIES, getEntitiesArg);
    log.debug("The RPC response GetEntitiesRet obtained {}", getEntitiesRet);
    return getEntitiesRet;
  }

  public static ErgonInterface.TaskCreateArg createTaskArg(final ByteString taskUuid,
                                                           final String componentName,
                                                           final String operationType,
                                                           final String displayName,
                                                           final ByteString parentTaskUuid,
                                                           final Integer subtaskSequenceId,
                                                           final ErgonTypes.EntityId entityId,
                                                           final RpcProto.RpcRequestContext requestContext) {
    final ErgonInterface.TaskCreateArg.Builder tBuilder =
      ErgonInterface.TaskCreateArg.newBuilder()
        .setPercentageComplete(0).setStatus(ErgonTypes.Task.Status.kQueued);

    if (taskUuid != null && !taskUuid.isEmpty()) {
      tBuilder.setUuid(taskUuid);
    }
    if (!StringUtils.isEmpty(componentName)) {
      tBuilder.setComponent(componentName);
    }
    if (!StringUtils.isEmpty(operationType)) {
      tBuilder.setOperationType(operationType);
    }
    if (!StringUtils.isEmpty(displayName)) {
      tBuilder.setDisplayName(displayName);
    }
    if (parentTaskUuid != null && !parentTaskUuid.isEmpty()) {
      tBuilder.setParentTaskUuid(parentTaskUuid);
    }
    if (subtaskSequenceId != null) {
      tBuilder.setSubtaskSequenceId(subtaskSequenceId);
    }
    if (entityId != null)
      tBuilder.addEntityList(entityId);
    if (requestContext != null)
      tBuilder.setRequestContext(requestContext);
    return tBuilder.build();
  }

  public static <K, V> K getKey(Map<K, V> map, V value)
  {
    for (Map.Entry<K, V> entry: map.entrySet())
    {
      if (value!= null && value.equals(entry.getValue())) {
        return entry.getKey();
      }
    }
    return null;
  }

  public static String calculateEtag(Object object) throws ClustermgmtServiceException {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(bos);
      oos.writeObject(object);
      oos.flush();
      byte[] data = bos.toByteArray();
      byte[] messageDigest = md.digest(data);
      StringBuilder hexString = new StringBuilder();
      for (byte b : messageDigest) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1) {
          hexString.append('0');
        }
        hexString.append(hex);
      }

      return hexString.toString();
    }
    catch (NoSuchAlgorithmException | IOException e) {
      log.debug("Exception occured in calculating e-tag: {}", e.getMessage());
      throw new ClustermgmtServiceException("Exception caused in calculating Etag");
    }
  }

  public static boolean passesRegexPattern(String data, Pattern pattern) {
    if (data == null) {
      return false;
    } else {
      Matcher matcher = pattern.matcher(data);
      return matcher.matches();
    }
  }

  public static boolean isFqdnString(String data) {
    return passesRegexPattern(data, FQDN_PATTERN);
  }

  public static ErgonTypes.EntityId getClusterEntityId(String clusterExtId) {
    ErgonTypes.EntityId entityId =
      ErgonTypes.EntityId.newBuilder()
        .setEntityType(ErgonTypes.EntityId.Entity.kCluster)
        .setEntityId(UuidUtils.getByteStringFromUUID(clusterExtId)).build();
    return entityId;
  }

  public static IPAddress createIpv4Ipv6Address(String address) {
    IPAddress ipAddress = new IPAddress();
    if (ValidationUtils.isIpv4Address(address)){
      IPv4Address iPv4Address = new IPv4Address();
      iPv4Address.setValue(address);
      ipAddress.setIpv4(iPv4Address);
    }
    else if(ValidationUtils.isIpv6Address(address)){
      IPv6Address iPv6Address = new IPv6Address();
      iPv6Address.setValue(address);
      ipAddress.setIpv6(iPv6Address);
    }
    return ipAddress;
  }

  public static IPAddressOrFQDN createIpv4Ipv6OrFqdnAddress(String address) {
    IPAddressOrFQDN ipAddress = new IPAddressOrFQDN();
    if (ValidationUtils.isIpv4Address(address)){
      IPv4Address iPv4Address = new IPv4Address();
      iPv4Address.setValue(address);
      ipAddress.setIpv4(iPv4Address);
    }
    else if(isFqdnString(address)) {
      FQDN fqdn = new FQDN();
      fqdn.setValue(address);
      ipAddress.setFqdn(fqdn);
    }
    else if(ValidationUtils.isIpv6Address(address)){
      IPv6Address iPv6Address = new IPv6Address();
      iPv6Address.setValue(address);
      ipAddress.setIpv6(iPv6Address);
    }
    return ipAddress;
  }

  public static String getIpv4Address(IPAddress ipAddress) {
    if(ipAddress.hasIpv4())
      return ipAddress.getIpv4().getValue();
    return null;
  }

  public static String getIpv6Address(IPAddress ipAddress) {
    if(ipAddress.hasIpv6())
      return ipAddress.getIpv6().getValue();
    return null;
  }

  public static String getAddress(IPAddress ipAddress) {
    if(ipAddress.hasIpv4())
      return ipAddress.getIpv4().getValue();
    else if(ipAddress.hasIpv6())
      return ipAddress.getIpv6().getValue();
    return null;
  }

  public static String getIpAddressOrFqdn(IPAddressOrFQDN address) {
    if(address.hasIpv4())
      return address.getIpv4().getValue();
    else if(address.hasIpv6())
      return address.getIpv6().getValue();
    else if(address.hasFqdn())
      return address.getFqdn().getValue();
    return null;
  }

  public static List<String> getIpv4AddressList(List<IPAddress> ipAddress) {
    List<String> ipv4 = new ArrayList<>();
    for(IPAddress ipAddress1: ipAddress) {
      ipv4.add(ipAddress1.getIpv4().getValue());
    }
    return ipv4;
  }

  public static List<String> getIpv6AddressList(List<IPAddress> ipAddress) {
    List<String> ipv6 = new ArrayList<>();
    for(IPAddress ipAddress1: ipAddress) {
      ipv6.add(ipAddress1.getIpv6().getValue());
    }
    return ipv6;
  }

  public static List<String> getAllAddressList(List<IPAddressOrFQDN> ipAddress) {
    List<String> ipAddressList = new ArrayList<>();
    for(IPAddressOrFQDN ipAddressOrFQDN: ipAddress) {
      if(ipAddressOrFQDN.hasIpv4()) {
        ipAddressList.add(ipAddressOrFQDN.getIpv4().getValue());
      }
      else if(ipAddressOrFQDN.hasIpv6()) {
        ipAddressList.add(ipAddressOrFQDN.getIpv6().getValue());
      }
      if(ipAddressOrFQDN.hasFqdn()) {
        ipAddressList.add(ipAddressOrFQDN.getFqdn().getValue());
      }
    }
    return ipAddressList;
  }

  public static IPv4Address createIpv4Address(String ip) {
    IPv4Address iPv4Address = new IPv4Address();
    iPv4Address.setValue(ip);
    return iPv4Address;
  }

  public static IPv6Address createIpv6Address(String ip) {
    IPv6Address iPv6Address = new IPv6Address();
    iPv6Address.setValue(ip);
    return iPv6Address;
  }

  public static List<String> getAttributeListFromSelection(InsightsInterfaceProto.QueryGroupBy queryGroupBy){
    log.debug("Selecting attributes for fetching from requested entity\n");
    List<InsightsInterfaceProto.QueryRawColumn> rawColumnsList = queryGroupBy.getRawColumnsList();
    List<String> attributes = new ArrayList<>();
    for (InsightsInterfaceProto.QueryRawColumn field : rawColumnsList) {
      attributes.add(field.getColumn());
      log.debug("Attribute: {}\n",field.getColumn());
    }
    return attributes;
  }

  public static Pair<Boolean, String> validateClusterCreateParams(Cluster params){

    //extId is not required for PC based create workflow
    if(params.getExtId() != null) {
      return new Pair(false, "ExtId is only a read-only field.Cluster uuid would be created in backend operation for the workflow");
    }

    //check is there are ipv6 address provided in network/nameServerIpList and network/ntpServerIpList
    if(params.getNetwork() != null) {
      if (!CollectionUtils.isEmpty(params.getNetwork().getNameServerIpList())) {
        for (IPAddressOrFQDN address : params.getNetwork().getNameServerIpList()) {
          if (address.hasIpv6()) {
            return new Pair(false, "IPv6 address is not supported for the name/ntp servers");
          }
        }
      }
      if (!CollectionUtils.isEmpty(params.getNetwork().getNtpServerIpList())) {
        for (IPAddressOrFQDN address : params.getNetwork().getNtpServerIpList()) {
          if (address.hasIpv6()) {
            return new Pair(false, "IPv6 address is not supported for the name/ntp servers");
          }
        }
      }
    }

    //validate config/clusterFunction - only allowed values AOS, ONE_NODE, TWO_NODE
    List<ClusterFunctionRef> clusterFunctionForCreateCluster = new ArrayList<>();
    clusterFunctionForCreateCluster.add(ClusterFunctionRef.ONE_NODE);
    clusterFunctionForCreateCluster.add(ClusterFunctionRef.TWO_NODE);
    clusterFunctionForCreateCluster.add(ClusterFunctionRef.AOS);

    if(params.getConfig() != null) {
      if (!CollectionUtils.isEmpty(params.getConfig().getClusterFunction())) {
        for (ClusterFunctionRef clusterFunction : params.getConfig().getClusterFunction()) {
          if (!clusterFunctionForCreateCluster.contains(clusterFunction)) {
            return new Pair(false, "For create cluster operation allowed cluster functions are: AOS, ONE_NODE & TWO_NODE only");
          }
        }
      }
      if (params.getConfig().getFaultToleranceState() != null) {
        if (params.getConfig().getFaultToleranceState().getCurrentClusterFaultTolerance() != null) {
          return new Pair(false, "CurrentClusterFaultTolerance is a read-only field. It would be updated based on the current cluster state");
        }
      }
    }

    //validate network/externalAddress - only IPv4 address is allowed
    if(params.getNetwork() != null) {
      IPAddress cvmExtAdd = params.getNetwork().getExternalAddress();
      if (cvmExtAdd != null && cvmExtAdd.hasIpv6()) {
        return new Pair(false, "IPv6 address is not supported for the cvm external address");
      }
    }

    //validate nodes/nodeList/ControllerVmIp - only IPv4 address is allowed
    if(params.getNodes() != null) {
      List<NodeListItemReference> nodeList = params.getNodes().getNodeList();
      if (!CollectionUtils.isEmpty(nodeList)) {
        for (NodeListItemReference nodeItem : nodeList) {
          if (nodeItem.getControllerVmIp().hasIpv6()) {
            return new Pair(false, "IPv6 address is not supported for the controller vm ip");
          } else if (nodeItem.getControllerVmIp() == null) {
            return new Pair(false, "Controller vm ip for node is required for cluster create operation");
          }
        }
      }
    }
    else{
      return new Pair(false, "Node information(contoller vm ips) of nodes to be included in cluster create operation not provided");
    }

    return new Pair(true, "None");
  }

  public static CircuitBreaker getCircuitBreakerInstance(String serviceName){
    CircuitBreaker circuitBreaker = CircuitBreakerUtility.getCircuitBreaker(SLIDING_WINDOW_SIZE/* Sliding window size */,
      FAILURE_THRESHOLD/* Failure threshold */,WAIT_DURATION_IN_OPEN_STATE/* Open State Wait duration */,
      NUMBER_OF_CALLS_IN_HALF_OPEN_STATE/* Calls in Half Open State */,serviceName/* Backend Service Name */);
    return circuitBreaker;
  }

  public static Long parseStringToLongValue(String value) throws ClustermgmtServiceException{
    Long statValue;
    try{
      if(value.contains(".")){
        Double statValueInDouble = Double.parseDouble(value);
        //if statValueInDouble>Long.MAX_VALUE(e.g. Double.MAX_VALUE), then statValue = Long.MAX_VALUE below
        //this would lead to wrong data being diplayed by API
        //the below would be the integer part of the double value
        statValue = statValueInDouble.longValue();
      }
      else{
        statValue = Long.parseLong(value);
      }
    }catch(NumberFormatException e){
      throw new ClustermgmtServiceException(e.toString());
    }

    return statValue;
  }

  public static ByteString decompress(final ByteString inputString) {
    final byte[] inputBytes = inputString.toByteArray();
    final int inputBytesLength = inputBytes.length;

    final Inflater inflater = new Inflater();
    inflater.setInput(inputBytes, 0, inputBytesLength);

    final ByteArrayOutputStream outputStream =
      new ByteArrayOutputStream(inputBytes.length);
    try {
      final byte[] buffer = new byte[1024];
      while (!inflater.finished()) {
        final int count = inflater.inflate(buffer);
        outputStream.write(buffer, 0, count);
      }
      outputStream.close();
    } catch (final DataFormatException | IOException ex) {
      log.warn("Error in uncompressing alert proto", ex);
    }

    inflater.end();
    final ByteString result = ByteString.copyFrom(outputStream.toByteArray());
    log.debug("Input Bytestring {} decompressed into {}", inputString, result);
    return result;
  }

  public static boolean getSnmpStatusFromIdf(
    GetEntitiesRet getClusterEntitiesRet) {
    boolean snmpStatus = false;
    InsightsInterfaceProto.Entity entity = getClusterEntitiesRet.getEntity(0);
    for (InsightsInterfaceProto.NameTimeValuePair attribute : entity.getAttributeDataMapList()) {
      if (!attribute.hasName() || !attribute.hasValue()) {
        continue;
      }
      if(attribute.getName().equals("snmp_status")) {
        snmpStatus = (attribute.getValue().getInt64Value() == 1);
      }
    }
    log.debug("SnmpStatus extracted from Cluster entry in IDF {}: {}", getClusterEntitiesRet, snmpStatus);
    return snmpStatus;
  }

  /**
   * Helper method to extract compressed SnmpUser proto from IDF metric data.
   *
   * @param metricData metric data from which snmpUser proto needs to be
   *                   extracted
   * @return deserialized, uncompressed snmpUser proto
   */
  public static GenesisInterfaceProto.SnmpUser extractSnmpUserProto(
    final InsightsInterfaceProto.MetricData metricData)
    throws InvalidProtocolBufferException {
    final ByteString decompressedProto =
      decompress(metricData.getValueList(0).getValue().getBytesValue());
    return GenesisInterfaceProto.SnmpUser.parseFrom(decompressedProto);
  }

  public static List<SnmpUser> getSnmpUserEntities(
    final String userExtId, final InsightsInterfaceProto.BooleanExpression where_clause,
    final EntityDbProxyImpl entityDbProxy, final ClustermgmtResourceAdapterImpl clustermgmtResourceAdapter,
    final ClusterResourceAdapter clusterResourceAdapter)
    throws ClustermgmtServiceException {
    List<SnmpUser> snmpUsers = new ArrayList<>();

    InsightsInterfaceProto.GetEntitiesWithMetricsRet getEntitiesWithMetricsRet =
      ClustermgmtUtils.getEntitiesWithMetricsRet(null, null,
        SNMP_USER_ENTITY, entityDbProxy, ImmutableList.of(ZPROTOBUF_ATTR), userExtId, clustermgmtResourceAdapter,
        where_clause, null, null);
    log.debug("The RPC response GetEntitiesWithMetricsRet for SnmpUser obtained {}", getEntitiesWithMetricsRet);

    if (!CollectionUtils.isEmpty(getEntitiesWithMetricsRet.getGroupResultsListList())) {
      for (final InsightsInterfaceProto.QueryGroupResult queryGroupResult : getEntitiesWithMetricsRet.getGroupResultsListList()) {
        final List<InsightsInterfaceProto.EntityWithMetric> entitiesWithMetric =
          queryGroupResult.getRawResultsList();

        for (final InsightsInterfaceProto.EntityWithMetric entityWithMetric : entitiesWithMetric) {
          final List<InsightsInterfaceProto.MetricData> metricDataList =
            entityWithMetric.getMetricDataListList();
          try {
            GenesisInterfaceProto.SnmpUser snmpUserProto =
              ClustermgmtUtils.extractSnmpUserProto(metricDataList.get(0));
            log.debug("SnmpUserProto extracted {}", snmpUserProto);
            snmpUsers.add(
              clusterResourceAdapter.adaptSnmpUserProtoToSnmpUserEntity(
                entityWithMetric.getEntityGuid().getEntityId(), snmpUserProto
              )
            );
          }
          catch (final InvalidProtocolBufferException ex) {
            throw new ClustermgmtServiceException("Failed to parse Snmpuser proto {}");
          }
        }
      }
    }
    log.debug("SnmpUsers with uuid {}: {}", userExtId, snmpUsers);
    return snmpUsers;
  }

  /**
   * Helper method to extract compressed SnmpTrap proto from IDF metric data.
   *
   * @param metricData metric data from which snmpTrap proto needs to be
   *                   extracted
   * @return deserialized, uncompressed snmpTrap proto
   */
  public static GenesisInterfaceProto.SnmpTrap extractSnmpTrapProto(
    final InsightsInterfaceProto.MetricData metricData)
    throws InvalidProtocolBufferException {
    final ByteString decompressedProto =
      decompress(metricData.getValueList(0).getValue().getBytesValue());
    return GenesisInterfaceProto.SnmpTrap.parseFrom(decompressedProto);
  }

  public static List<SnmpTrap> getSnmpTrapEntities(
    final String trapExtId, final InsightsInterfaceProto.BooleanExpression where_clause,
    final EntityDbProxyImpl entityDbProxy, final ClustermgmtResourceAdapterImpl clustermgmtResourceAdapter,
    final ClusterResourceAdapter clusterResourceAdapter)
    throws ClustermgmtServiceException {
    List<SnmpTrap> snmpTraps = new ArrayList<>();

    InsightsInterfaceProto.GetEntitiesWithMetricsRet getEntitiesWithMetricsRet =
      ClustermgmtUtils.getEntitiesWithMetricsRet(null, null,
        SNMP_TRAP_ENTITY, entityDbProxy, ImmutableList.of(ZPROTOBUF_ATTR), trapExtId, clustermgmtResourceAdapter,
        where_clause, null, null);
    log.debug("The RPC response GetEntitiesWithMetricsRet for SnmpTrap obtained {}", getEntitiesWithMetricsRet);

    if (!CollectionUtils.isEmpty(getEntitiesWithMetricsRet.getGroupResultsListList())) {
      for (final InsightsInterfaceProto.QueryGroupResult queryGroupResult : getEntitiesWithMetricsRet.getGroupResultsListList()) {
        final List<InsightsInterfaceProto.EntityWithMetric> entitiesWithMetric =
          queryGroupResult.getRawResultsList();

        for (final InsightsInterfaceProto.EntityWithMetric entityWithMetric : entitiesWithMetric) {
          final List<InsightsInterfaceProto.MetricData> metricDataList =
            entityWithMetric.getMetricDataListList();
          try {
            GenesisInterfaceProto.SnmpTrap snmpTrapProto =
              ClustermgmtUtils.extractSnmpTrapProto(metricDataList.get(0));
            log.debug("SnmpUserProto extracted {}", snmpTrapProto);
            snmpTraps.add(
              clusterResourceAdapter.adaptSnmpTrapProtoToSnmpTrapEntity(
                entityWithMetric.getEntityGuid().getEntityId(), snmpTrapProto
              )
            );
          }
          catch (final InvalidProtocolBufferException ex) {
            throw new ClustermgmtServiceException("Failed to parse SnmpTrap proto {}");
          }
        }
      }
    }
    log.debug("Snmp traps with uuid {}: {}", trapExtId, snmpTraps);
    return snmpTraps;
  }

  /**
   * Helper method to extract compressed SnmpTransport proto from IDF metric data.
   *
   * @param metricData metric data from which snmpTransport proto needs to be
   *                   extracted
   * @return deserialized, uncompressed snmpTransport proto
   */
  public static GenesisInterfaceProto.SnmpTransport extractSnmpTransportProto(
    final InsightsInterfaceProto.MetricData metricData)
    throws InvalidProtocolBufferException {
    final ByteString decompressedProto =
      decompress(metricData.getValueList(0).getValue().getBytesValue());
    return GenesisInterfaceProto.SnmpTransport.parseFrom(decompressedProto);
  }

  public static List<SnmpTransport> getSnmpTransportEntities(
    final String transportExtId, final InsightsInterfaceProto.BooleanExpression where_clause,
    final EntityDbProxyImpl entityDbProxy, final ClustermgmtResourceAdapterImpl clustermgmtResourceAdapter,
    final ClusterResourceAdapter clusterResourceAdapter)
    throws ClustermgmtServiceException {
    List<SnmpTransport> snmpTransportList = new ArrayList<>();

    InsightsInterfaceProto.GetEntitiesWithMetricsRet getEntitiesWithMetricsRet =
      ClustermgmtUtils.getEntitiesWithMetricsRet(null, null,
        SNMP_TRANSPORT_ENTITY, entityDbProxy, ImmutableList.of(ZPROTOBUF_ATTR), transportExtId,
        clustermgmtResourceAdapter, where_clause, null, null);
    log.debug("The RPC response GetEntitiesWithMetricsRet for SnmpTransport obtained {}", getEntitiesWithMetricsRet);

    if (!CollectionUtils.isEmpty(getEntitiesWithMetricsRet.getGroupResultsListList())) {
      for (final InsightsInterfaceProto.QueryGroupResult queryGroupResult : getEntitiesWithMetricsRet.getGroupResultsListList()) {
        final List<InsightsInterfaceProto.EntityWithMetric> entitiesWithMetric =
          queryGroupResult.getRawResultsList();

        for (final InsightsInterfaceProto.EntityWithMetric entityWithMetric : entitiesWithMetric) {
          final List<InsightsInterfaceProto.MetricData> metricDataList =
            entityWithMetric.getMetricDataListList();
          try {
            GenesisInterfaceProto.SnmpTransport snmpTransportProto =
              ClustermgmtUtils.extractSnmpTransportProto(metricDataList.get(0));
            log.debug("SnmpTransportProto extracted {}", snmpTransportProto);
            snmpTransportList.add(
              clusterResourceAdapter.adaptSnmpTransportProtoToSnmpTransportEntity(snmpTransportProto)
            );
          }
          catch (final InvalidProtocolBufferException ex) {
            throw new ClustermgmtServiceException("Failed to parse SnmpTransport proto {}");
          }
        }
      }
    }
    log.debug("Snmp transports with uuid {}: {}", transportExtId, snmpTransportList);
    return snmpTransportList;
  }

  /**
   * Helper method to extract compressed RemoteSyslogConfiguration proto from
   * IDF metric data.
   *
   * @param metricData metric data from which RemoteSyslogConfiguration proto
   *                  needs to be extracted
   * @return deserialized, uncompressed RemoteSyslogConfiguration proto
   */
  public static GenesisInterfaceProto.RemoteSyslogConfiguration extractRemoteSyslogConfigurationProto(
    final InsightsInterfaceProto.MetricData metricData)
    throws InvalidProtocolBufferException {
    final ByteString decompressedProto =
      decompress(metricData.getValueList(0).getValue().getBytesValue());
    return GenesisInterfaceProto.RemoteSyslogConfiguration.parseFrom(decompressedProto);
  }

  public static List<RsyslogServer> getRsyslogServerEntities(
    final String rsyslogServerExtId, final InsightsInterfaceProto.BooleanExpression where_clause,
    final EntityDbProxyImpl entityDbProxy, final ClustermgmtResourceAdapterImpl clustermgmtResourceAdapter,
    final ClusterResourceAdapter clusterResourceAdapter)
    throws ClustermgmtServiceException {
    List<RsyslogServer> rsyslogServerList = new ArrayList<>();

    InsightsInterfaceProto.GetEntitiesWithMetricsRet getEntitiesWithMetricsRet =
      ClustermgmtUtils.getEntitiesWithMetricsRet(null, null,
        RSYSLOG_ENTITY, entityDbProxy, ImmutableList.of(ZPROTOBUF_ATTR), rsyslogServerExtId, clustermgmtResourceAdapter,
        where_clause, null, null);
    log.debug("The RPC response GetEntitiesWithMetricsRet for RsyslogServer obtained {}", getEntitiesWithMetricsRet);

    if (!CollectionUtils.isEmpty(getEntitiesWithMetricsRet.getGroupResultsListList())) {
      for (final InsightsInterfaceProto.QueryGroupResult queryGroupResult : getEntitiesWithMetricsRet.getGroupResultsListList()) {
        final List<InsightsInterfaceProto.EntityWithMetric> entitiesWithMetric =
          queryGroupResult.getRawResultsList();

        for (final InsightsInterfaceProto.EntityWithMetric entityWithMetric : entitiesWithMetric) {
          final List<InsightsInterfaceProto.MetricData> metricDataList =
            entityWithMetric.getMetricDataListList();
          try {
            GenesisInterfaceProto.RemoteSyslogConfiguration remoteSyslogConfigurationProto =
              ClustermgmtUtils.extractRemoteSyslogConfigurationProto(metricDataList.get(0));
            log.debug("RemoteSyslogConfigurationProto extracted {}", remoteSyslogConfigurationProto);
            rsyslogServerList.add(
              clusterResourceAdapter.adaptRemoteSyslogConfigurationProtoToRsyslogServer(
                entityWithMetric.getEntityGuid().getEntityId(), remoteSyslogConfigurationProto
              )
            );
          }
          catch (final InvalidProtocolBufferException ex) {
            throw new ClustermgmtServiceException("Failed to parse Snmpuser proto {}");
          }
        }
      }
    }
    log.debug("Rsyslog servers with uuid {}: {}", rsyslogServerExtId, rsyslogServerList);
    return rsyslogServerList;
  }

  /**
   * Helper method to extract compressed HttpProxy proto from
   * IDF metric data.
   *
   * @param metricData metric data from which HttpProxy proto
   *                  needs to be extracted
   * @return deserialized, uncompressed HttpProxy proto
   */
  public static GenesisInterfaceProto.HttpProxy extractHttpProxyProto(
    final InsightsInterfaceProto.MetricData metricData)
    throws InvalidProtocolBufferException {
    final ByteString decompressedProto =
      decompress(metricData.getValueList(0).getValue().getBytesValue());
    return GenesisInterfaceProto.HttpProxy.parseFrom(decompressedProto);
  }

  public static List<HttpProxyConfig> getHttpProxyEntities(
    final InsightsInterfaceProto.BooleanExpression where_clause, final EntityDbProxyImpl entityDbProxy,
    final ClustermgmtResourceAdapterImpl clustermgmtResourceAdapter, final ClusterResourceAdapter clusterResourceAdapter)
    throws ClustermgmtServiceException {
    List<HttpProxyConfig> httpProxyList = new ArrayList<>();

    InsightsInterfaceProto.GetEntitiesWithMetricsRet getEntitiesWithMetricsRet =
      ClustermgmtUtils.getEntitiesWithMetricsRet(null, null,
        HTTP_PROXY_ENTITY, entityDbProxy, ImmutableList.of(ZPROTOBUF_ATTR), null, clustermgmtResourceAdapter,
        where_clause, null, null);
    log.debug("The RPC response GetEntitiesWithMetricsRet for Http Proxy obtained {}", getEntitiesWithMetricsRet);

    if (!CollectionUtils.isEmpty(getEntitiesWithMetricsRet.getGroupResultsListList())) {
      for (final InsightsInterfaceProto.QueryGroupResult queryGroupResult : getEntitiesWithMetricsRet.getGroupResultsListList()) {
        final List<InsightsInterfaceProto.EntityWithMetric> entitiesWithMetric =
          queryGroupResult.getRawResultsList();

        for (final InsightsInterfaceProto.EntityWithMetric entityWithMetric : entitiesWithMetric) {
          final List<InsightsInterfaceProto.MetricData> metricDataList =
            entityWithMetric.getMetricDataListList();
          try {
            GenesisInterfaceProto.HttpProxy httpProxyConfigurationProto =
              ClustermgmtUtils.extractHttpProxyProto(metricDataList.get(0));
            log.debug("HttpProxyProto extracted {}", httpProxyConfigurationProto);
            httpProxyList.add(
              clusterResourceAdapter.adaptHttpProxyConfigurationProtoToHttpProxy(
                entityWithMetric.getEntityGuid().getEntityId(), httpProxyConfigurationProto
              )
            );
          }
          catch (final InvalidProtocolBufferException ex) {
            throw new ClustermgmtServiceException("Failed to parse HttpProxy proto " + ex.getMessage());
          }
        }
      }
    }
    log.debug("Http proxies {}", httpProxyList);

    return httpProxyList;
  }

  /**
   * Helper method to extract compressed HttpProxyWhitelist proto from
   * IDF metric data.
   *
   * @param metricData metric data from which HttpProxyWhitelist proto
   *                  needs to be extracted
   * @return deserialized, uncompressed HttpProxyWhitelist proto
   */
  public static GenesisInterfaceProto.HttpProxyWhitelist extractHttpProxyWhitelistProto(
    final InsightsInterfaceProto.MetricData metricData)
    throws InvalidProtocolBufferException {
    final ByteString decompressedProto =
      decompress(metricData.getValueList(0).getValue().getBytesValue());
    return GenesisInterfaceProto.HttpProxyWhitelist.parseFrom(decompressedProto);
  }

  public static List<HttpProxyWhiteListConfig> getHttpProxyWhitelistEntities(
    final InsightsInterfaceProto.BooleanExpression where_clause,
    final EntityDbProxyImpl entityDbProxy, final ClustermgmtResourceAdapterImpl clustermgmtResourceAdapter,
    final ClusterResourceAdapter clusterResourceAdapter)
    throws ClustermgmtServiceException {
    List<HttpProxyWhiteListConfig> httpProxyWhiteList = new ArrayList<>();

    InsightsInterfaceProto.GetEntitiesWithMetricsRet getEntitiesWithMetricsRet =
      ClustermgmtUtils.getEntitiesWithMetricsRet(null, null,
        HTTP_PROXY_WHITELIST_ENTITY, entityDbProxy, ImmutableList.of(ZPROTOBUF_ATTR), null,
        clustermgmtResourceAdapter, where_clause, null, null);
    log.debug("The RPC response GetEntitiesWithMetricsRet for Http Proxy WhiteList obtained {}", getEntitiesWithMetricsRet);

    if (!CollectionUtils.isEmpty(getEntitiesWithMetricsRet.getGroupResultsListList())) {
      for (final InsightsInterfaceProto.QueryGroupResult queryGroupResult : getEntitiesWithMetricsRet.getGroupResultsListList()) {
        final List<InsightsInterfaceProto.EntityWithMetric> entitiesWithMetric =
          queryGroupResult.getRawResultsList();

        for (final InsightsInterfaceProto.EntityWithMetric entityWithMetric : entitiesWithMetric) {
          final List<InsightsInterfaceProto.MetricData> metricDataList =
            entityWithMetric.getMetricDataListList();
          try {
            GenesisInterfaceProto.HttpProxyWhitelist httpProxyWhitelistConfigurationProto =
              ClustermgmtUtils.extractHttpProxyWhitelistProto(metricDataList.get(0));
            log.debug("HttpProxyWhitelist Proto extracted {}", httpProxyWhitelistConfigurationProto);
            httpProxyWhiteList.add(
              clusterResourceAdapter.adaptHttpProxyWhitelistConfigurationProtoToHttpProxyWhiteList(
                entityWithMetric.getEntityGuid().getEntityId(), httpProxyWhitelistConfigurationProto
              )
            );
          }
          catch (final InvalidProtocolBufferException ex) {
            throw new ClustermgmtServiceException("Failed to parse HttpProxyWhitelist proto {}");
          }
        }
      }
    }
    log.debug("Http proxy Whitelist: {}", httpProxyWhiteList);

    return httpProxyWhiteList;
  }

  public static JsonNode getJsonObjFromTaskInternalOpaque(ErgonTypes.Task task) throws
    ClustermgmtServiceException {
      ByteString response = task.getInternalOpaque();
      JsonNode jsonNode = null;
      try {
        jsonNode = mapper.readTree(response.toStringUtf8());
      }
      catch (Exception e) {
        throw new ClustermgmtServiceException("Error in parsing json output from task " + e.getMessage());
      }
      return jsonNode;
    }

  public static Boolean doesNameServerHaveFqdn(Cluster payload){
    if(payload != null){
      if(payload.getNetwork() != null){
        List<IPAddressOrFQDN> ipList;
        if(payload.getNetwork().getNameServerIpList() != null){
           ipList = payload.getNetwork().getNameServerIpList();
           for(IPAddressOrFQDN ip: ipList){
             if(ip.hasFqdn()){
               return true;
             }
           }
        }
      }
    }
    return false;
  }

}
