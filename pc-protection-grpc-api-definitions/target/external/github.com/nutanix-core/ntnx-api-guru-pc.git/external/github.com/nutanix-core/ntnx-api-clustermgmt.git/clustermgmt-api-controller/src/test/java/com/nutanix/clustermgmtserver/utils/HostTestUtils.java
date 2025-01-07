/*
 * Copyright (c) 2021 Nutanix Inc. All rights reserved.
 *
 * Author: rakesh.falak@nutanix.com
 */
package com.nutanix.clustermgmtserver.utils;

import com.nutanix.insights.ifc.InsightsInterfaceProto;
import com.nutanix.prism.adapter.service.ZeusConfiguration;
import com.nutanix.prism.adapter.service.ZeusConfigurationImpl;
import com.nutanix.util.base.Pair;
import com.nutanix.zeus.protobuf.Configuration;
import dp1.clu.clustermgmt.v4.config.ClusterReference;
import dp1.clu.clustermgmt.v4.config.VirtualGpuProfile;
import dp1.clu.clustermgmt.v4.config.VirtualGpuConfig;
import dp1.clu.clustermgmt.v4.config.PhysicalGpuProfile;
import dp1.clu.clustermgmt.v4.config.PhysicalGpuConfig;
import dp1.clu.clustermgmt.v4.config.Host;
import dp1.clu.clustermgmt.v4.config.HostGpu;
import dp1.clu.clustermgmt.v4.config.HostNic;
import dp1.clu.clustermgmt.v4.config.VirtualNic;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class HostTestUtils {

  public static final String HOST_NAME =
    "test-host-name";

  public static final String HOST_UUID =
    "5cafbb40-dfd4-4eaf-ac8d-5ff91a230c7a";

  public static final String CLUSTER_UUID =
    "5cafbb40-dfd4-4eaf-ac8d-5ff91a230c7a";

  public static final String DISK_UUID =
    "5cafbb40-dfd4-4eaf-ac8d-5ff91a230c7a";

  public static final String RACKABLE_UNIT_UUID =
    "5cafbb40-dfd4-4eaf-ac8d-5ff91a230c7a";

  public static final String HOST_GPU_UUID =
    "5cafbb40-dfd4-4eaf-ac8d-5ff91a230c7a";

  public static final String HOST_NICS_UUID =
          "5cafbb40-dfd4-4eaf-ac8d-5ff91a230c7a";

  public static final String VIRTUAL_NICS_UUID =
    "5cafbb40-dfd4-4eaf-ac8d-5ff91a230c7a";

  public static final String NETWORK_SWITCH_INTF_UUID =
    "5cafbb40-dfd4-4eaf-ac8d-5ff91a230c7a";

  public static final Long HOST_PHYSICAL_GPU_DEVICE_ID = 1234L;

  public static final Long HOST_VIRTUAL_GPU_DEVICE_ID = 900L;

  public static final String CVM_IPV4 =
    "10.10.10.10";

  public static final String HOST_IPV4 =
    "10.10.10.10";

  public static Host getHostEntityObj() {
    Host hostEntity = new Host();
    hostEntity.setHostName(HOST_NAME);
    return hostEntity;
  }

  public static Pair<Integer, List<Host>> getHostEntitiesObj() {
    List<Host> hostEntityList = new ArrayList<>();
    Host hostEntity = new Host();
    hostEntity.setHostName(HOST_NAME);
    hostEntityList.add(hostEntity);
    Integer totalEntityCount = 1;
    return new Pair<>(totalEntityCount, hostEntityList);
  }

  public static Pair<Integer, List<HostGpu> > getHostGpusObj() {
    List<HostGpu> hostGpuEntities = new ArrayList<>();
    ClusterReference clusterReference = new ClusterReference();
    HostGpu hostGpuEntity = new HostGpu();
    clusterReference.setName("test_name");
    clusterReference.setUuid(CLUSTER_UUID);
    hostGpuEntity.setExtId(HOST_GPU_UUID);
    hostGpuEntity.setCluster(clusterReference);
    hostGpuEntities.add(hostGpuEntity);
    Integer totalEntityCount = 1;
    return new Pair<>(totalEntityCount, hostGpuEntities);
  }

  public static Pair<Integer, List<HostGpu> > getClusterHostGpusObj() {
    List<HostGpu> hostGpuEntities = new ArrayList<>();
    ClusterReference clusterReference = new ClusterReference();
    HostGpu hostGpuEntity = new HostGpu();
    clusterReference.setName("test_name");
    clusterReference.setUuid(CLUSTER_UUID);
    hostGpuEntity.setExtId(HOST_GPU_UUID);
    hostGpuEntity.setCluster(clusterReference);
    hostGpuEntities.add(hostGpuEntity);
    Integer totalEntityCount = 1;
    return new Pair<>(totalEntityCount, hostGpuEntities);
  }

  public static Pair<Integer, List<VirtualGpuProfile> > getVirtualGpuProfileTest() {
    List<VirtualGpuProfile> gpuProfileList = new ArrayList<>();
    VirtualGpuProfile gpuProfile = new VirtualGpuProfile();
    VirtualGpuConfig config = new VirtualGpuConfig();
    config.setDeviceId(HOST_VIRTUAL_GPU_DEVICE_ID);
    gpuProfile.setVirtualGpuConfig(config);
    gpuProfileList.add(gpuProfile);
    Integer totalEntityCount = 1;
    return new Pair<>(totalEntityCount, gpuProfileList);
  }

   public static Pair<Integer, List<PhysicalGpuProfile> > getPhysicalGpuProfileTest() {
    List<PhysicalGpuProfile> gpuProfileList = new ArrayList<>();
    PhysicalGpuProfile gpuProfile = new PhysicalGpuProfile();
    PhysicalGpuConfig config = new PhysicalGpuConfig();
    config.setDeviceId(HOST_PHYSICAL_GPU_DEVICE_ID);
    gpuProfile.setPhysicalGpuConfig(config);
    gpuProfileList.add(gpuProfile);
    Integer totalEntityCount = 1;
    return new Pair<>(totalEntityCount, gpuProfileList);
  }
 
  public static HostGpu getHostGpuObj() {
    HostGpu hostGpuEntity = new HostGpu();
    ClusterReference clusterReference = new ClusterReference();
    clusterReference.setName("test_name");
    clusterReference.setUuid(CLUSTER_UUID);
    hostGpuEntity.setCluster(clusterReference);
    hostGpuEntity.setExtId(HOST_GPU_UUID);
    return hostGpuEntity;
  }

  public static InsightsInterfaceProto.GetEntitiesRet getHostIdfEntityObj(String... nodeUuids) {
    List<String> hostGpus = new ArrayList<>();
    hostGpus.add("testGpu");
    InsightsInterfaceProto.GetEntitiesRet.Builder getEntitiesRet = InsightsInterfaceProto.GetEntitiesRet.newBuilder();
    Arrays.stream(nodeUuids).forEach(nodeUuid -> {
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("node_name")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue(HostTestUtils.HOST_NAME))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair2 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("_cluster_uuid_")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue(HostTestUtils.CLUSTER_UUID))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair3 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("host_type")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("kHyperConverged"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair4 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("cpu_model")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("testCpuModel"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair5 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("num_cpu_cores")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setInt64Value(1L))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair6 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("num_cpu_threads")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setInt64Value(1L))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair7 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("capacity.cpu_capacity_hz")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setInt64Value(1L))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair8 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("hypervisor_full_name")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("testHypervisorName"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair9 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("hypervisor_type")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("kKvm"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair10 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("num_vms")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setInt64Value(2L))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair12 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("num_cpu_sockets")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setInt64Value(2L))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair13 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("cluster_name")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("testName"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair14 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("test_name")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("testName"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair15 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("test_name")
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair16 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair17 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("testValue")).build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair18 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("default_case")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("default_value")).build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair19 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("host_gpu_list")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().
              setStrList(InsightsInterfaceProto.DataValue.StrList.newBuilder().addAllValueList(hostGpus))).build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair20 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("gpu_driver_version")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("testVersion")).build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair21 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("default_vhd_location")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("testDefaultVhdLoc")).build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair22 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("default_vhd_container_id")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("testVhdContainerId")).build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair23 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("default_vhd_container_uuid")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("testVhdContainerUuid")).build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair24 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("default_vm_location")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("testVmLoc")).build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair25 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("default_vm_container_id")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("testVmContainerId")).build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair26 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("default_vm_container_uuid")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("testVmContainerUuid")).build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair27 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("reboot_pending")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setInt64Value(1L)).build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair28 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("failover_cluster_fqdn")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("testFailoverFqdn")).build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair29 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("failover_cluster_node_status")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("testFailoverNodeStatus")).build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair30 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("boot_time_usecs")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setInt64Value(1L)).build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair31 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("memory_size_bytes")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setInt64Value(1L)).build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair32 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("cpu_frequency_hz")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setInt64Value(1L)).build();

      InsightsInterfaceProto.Entity entity = InsightsInterfaceProto.Entity.newBuilder()
        .addAttributeDataMap(nameTimeValuePair)
        .addAttributeDataMap(nameTimeValuePair2)
        .addAttributeDataMap(nameTimeValuePair3)
        .addAttributeDataMap(nameTimeValuePair4)
        .addAttributeDataMap(nameTimeValuePair5)
        .addAttributeDataMap(nameTimeValuePair6)
        .addAttributeDataMap(nameTimeValuePair7)
        .addAttributeDataMap(nameTimeValuePair8)
        .addAttributeDataMap(nameTimeValuePair9)
        .addAttributeDataMap(nameTimeValuePair10)
        .addAttributeDataMap(nameTimeValuePair12)
        .addAttributeDataMap(nameTimeValuePair13)
        .addAttributeDataMap(nameTimeValuePair14)
        .addAttributeDataMap(nameTimeValuePair15)
        .addAttributeDataMap(nameTimeValuePair16)
        .addAttributeDataMap(nameTimeValuePair17)
        .addAttributeDataMap(nameTimeValuePair18)
        .addAttributeDataMap(nameTimeValuePair19)
        .addAttributeDataMap(nameTimeValuePair20)
        .addAttributeDataMap(nameTimeValuePair21)
        .addAttributeDataMap(nameTimeValuePair22)
        .addAttributeDataMap(nameTimeValuePair23)
        .addAttributeDataMap(nameTimeValuePair24)
        .addAttributeDataMap(nameTimeValuePair25)
        .addAttributeDataMap(nameTimeValuePair26)
        .addAttributeDataMap(nameTimeValuePair27)
        .addAttributeDataMap(nameTimeValuePair28)
        .addAttributeDataMap(nameTimeValuePair29)
        .addAttributeDataMap(nameTimeValuePair30)
        .addAttributeDataMap(nameTimeValuePair31)
        .addAttributeDataMap(nameTimeValuePair32)
        .build();

      getEntitiesRet.addEntity(entity);
    });
    return getEntitiesRet.build();
  }

  public static InsightsInterfaceProto.GetEntitiesWithMetricsRet buildGetEntitiesWithMetricsRetForHost(String clusterUuid) {
    List<String> hostGpus = new ArrayList<>();
    hostGpus.add("testGpu");

    InsightsInterfaceProto.MetricData metric1 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("node_name")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue(HostTestUtils.HOST_NAME)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric2 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("host_type")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder().setStrValue("kHyperConverged")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric3 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("cpu_model")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("testModel")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric4 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("num_cpu_cores")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setInt64Value(1L)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric5 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("num_cpu_sockets")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setInt64Value(1L)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric6 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("num_cpu_threads")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setInt64Value(1L)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric7 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("capacity.cpu_capacity_hz")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setInt64Value(1L)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric8 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("_cluster_uuid_")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue(clusterUuid)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric9 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("cluster_name")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("testName")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric10 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("hypervisor_full_name")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("testName")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric11 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("hypervisor_type")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("kKvm")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric12 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("num_vms")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setInt64Value(1L)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric14 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("gpu_driver_version")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("testVersion")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric15 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("host_gpu_list")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder().
            setStrList(InsightsInterfaceProto.DataValue.StrList.newBuilder().addAllValueList(hostGpus))
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric16 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("default_vhd_location")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("testDefaultVhdLoc")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric17 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("default_vhd_container_id")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("testDefaultVhdId")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric18 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("default_vhd_container_uuid")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("testDefaultVhdUuid")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric19 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("default_vm_location")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("testDefaultVmLoc")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric20 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("default_vm_container_id")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("testDefaultVmContainerId")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric21 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("default_vm_container_uuid")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("testDefaultVmContainerUuid")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric22 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("reboot_pending")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setInt64Value(1L)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric23 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("failover_cluster_fqdn")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("test")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric24 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("failover_cluster_node_status")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("test")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric25 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("boot_time_usecs")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setInt64Value(1L)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric26 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("memory_size_bytes")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setInt64Value(1L)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric27 =
    InsightsInterfaceProto.MetricData.newBuilder()
      .setName("cpu_frequency_hz")
      .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
        .setValue(InsightsInterfaceProto.DataValue.newBuilder()
          .setInt64Value(1L)
          .build())
        .build())
      .build();
    InsightsInterfaceProto.MetricData metric28 =
    InsightsInterfaceProto.MetricData.newBuilder()
      .setName("testName")
      .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
        .setValue(InsightsInterfaceProto.DataValue.newBuilder()
          .setInt64Value(1L)
          .build())
        .build())
      .build();
    InsightsInterfaceProto.MetricData metric29 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("test_name")
        .build();
    InsightsInterfaceProto.MetricData metric30 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .build();
    InsightsInterfaceProto.MetricData metric31 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("X86_64")
            .build())
          .build())
        .build();

    return InsightsInterfaceProto.GetEntitiesWithMetricsRet.newBuilder()
      .addGroupResultsList(InsightsInterfaceProto.QueryGroupResult.newBuilder().
        addRawResults(
          InsightsInterfaceProto.EntityWithMetric.newBuilder()
            .setEntityGuid(
              InsightsInterfaceProto.EntityGuid.newBuilder().
                setEntityId(HOST_UUID).
                setEntityTypeName("node").
                build())
            .addMetricDataList(metric1)
            .addMetricDataList(metric2)
            .addMetricDataList(metric3)
            .addMetricDataList(metric4)
            .addMetricDataList(metric5)
            .addMetricDataList(metric6)
            .addMetricDataList(metric7)
            .addMetricDataList(metric8)
            .addMetricDataList(metric9)
            .addMetricDataList(metric10)
            .addMetricDataList(metric11)
            .addMetricDataList(metric12)
            .addMetricDataList(metric14)
            .addMetricDataList(metric15)
            .addMetricDataList(metric16)
            .addMetricDataList(metric17)
            .addMetricDataList(metric18)
            .addMetricDataList(metric19)
            .addMetricDataList(metric20)
            .addMetricDataList(metric21)
            .addMetricDataList(metric22)
            .addMetricDataList(metric23)
            .addMetricDataList(metric24)
            .addMetricDataList(metric25)
            .addMetricDataList(metric26)
            .addMetricDataList(metric27)
            .addMetricDataList(metric28)
            .addMetricDataList(metric29)
            .addMetricDataList(metric30)
            .addMetricDataList(metric31)
        ).setTotalEntityCount(1).build()).build();
  }

  public static InsightsInterfaceProto.GetEntitiesRet getHostIdfEntityObjWithNoEntity() {
    InsightsInterfaceProto.GetEntitiesRet.Builder getEntitiesRet = InsightsInterfaceProto.GetEntitiesRet.newBuilder();
    return getEntitiesRet.build();
  }

  public static ZeusConfiguration buildZeusConfiguration() {
    Configuration.ConfigurationProto.Builder cfgBuilder =
      Configuration.ConfigurationProto.newBuilder()
        .setLogicalTimestamp(1L);

    Configuration.ConfigurationProto.NetworkEntity.Builder networkEntity =
      Configuration.ConfigurationProto.NetworkEntity.newBuilder()
        .addAddressList("10.10.10.10")
        .setUsername("test");

    Configuration.ConfigurationProto.Node.AcropolisStatus.Builder acropolisStatus =
      Configuration.ConfigurationProto.Node.AcropolisStatus.newBuilder()
        .setIsHardwareVirtualized(true)
        .setNodeState(Configuration.ConfigurationProto.Node.AcropolisStatus.AcropolisNodeState.kAcropolisNormal)
        .setConnState(Configuration.ConfigurationProto.Node.AcropolisStatus.AcropolisConnectionState.kConnected);

    Configuration.ConfigurationProto.Node.DigitalCertificateMap.Builder digital =
      Configuration.ConfigurationProto.Node.DigitalCertificateMap.newBuilder()
        .setDigitalCertificateZkpath("/appliance/logical/certs/node-id/svm_certs/<device-uuid>/randomString");

    Configuration.ConfigurationProto.KeyManagementServer.Builder keyManagementServer =
      Configuration.ConfigurationProto.KeyManagementServer.newBuilder()
        .setKeyManagementServerName("test")
        .setKeyManagementServerUuid("randomString");

    Configuration.ConfigurationProto.Node.Builder nodeBuilder =
      Configuration.ConfigurationProto.Node.newBuilder()
        .setUuid(HOST_UUID)
        .setServiceVmExternalIp(CVM_IPV4)
        .setServiceVmId(1L)
        .setControllerVmBackplaneIp(CVM_IPV4)
        .setHypervisorKey(HOST_IPV4)
        .setIpmi(networkEntity)
        .addRdmaBackplaneIps("10.10.10.10")
        .setServiceVmNatIp("10.10.10.10")
        .setServiceVmNatPort(8080)
        .setMaintenanceMode(true)
        .addHostExternalIpList("10.10.10.10")
        .addHostExternalIpList("10.10.10.10")
        .addSvmExternalIpList(CVM_IPV4)
        .addSvmExternalIpList("2.2.2.2.2.2")
        .setRackableUnitUuid(RACKABLE_UNIT_UUID)
        .setIsDegraded(true)
        .setIsSecureBooted(true)
        .setAcropolisStatus(acropolisStatus)
        .setSvmCertificateSigningRequestZkpath("test")
        .addDigitalCertificateZkpathList("/appliance/logical/certs/node-id/svm_certs/<device-uuid>/randomString")
        .addDigitalCertificateMapList(digital);


    Configuration.ConfigurationProto.Disk.Builder diskBuilder =
      Configuration.ConfigurationProto.Disk.newBuilder()
        .setDiskId(1L)
        .setDiskUuid(DISK_UUID)
        .setMountPath("testMountPath")
        .setDiskSerialId("testSerialId")
        .setDiskSize(100L)
        .setStorageTier("SSD-SATA")
        .setNodeUuid(HOST_UUID);

    cfgBuilder.addNodeList(nodeBuilder);
    cfgBuilder.addDiskList(diskBuilder);
    cfgBuilder.addKeyManagementServerList(keyManagementServer);
    return new ZeusConfigurationImpl.Builder(cfgBuilder.build()).build();
  }

  public static ZeusConfiguration buildZeusConfigurationWithConditions() {
    Configuration.ConfigurationProto.Builder cfgBuilder =
      Configuration.ConfigurationProto.newBuilder()
        .setLogicalTimestamp(1L);

    Configuration.ConfigurationProto.NetworkEntity.Builder networkEntity =
      Configuration.ConfigurationProto.NetworkEntity.newBuilder()
        .addAddressList("10.10.10.10")
        .setUsername("test");

    Configuration.ConfigurationProto.Node.Builder nodeBuilder =
      Configuration.ConfigurationProto.Node.newBuilder()
        .setUuid(HOST_UUID)
        .setServiceVmId(1)
        .setServiceVmExternalIp(CVM_IPV4)
        .setHypervisorKey(HOST_IPV4)
        .setHypervisor(networkEntity);

    Configuration.ConfigurationProto.Disk.Builder diskBuilder =
      Configuration.ConfigurationProto.Disk.newBuilder()
        .setDiskId(1L)
        .setDiskUuid(DISK_UUID)
        .setMountPath("testMountPath")
        .setDiskSerialId("testSerialId")
        .setDiskSize(100L)
        .setStorageTier("SSD-SATA")
        .setNodeUuid(HOST_UUID);

    cfgBuilder.addNodeList(nodeBuilder);
    cfgBuilder.addDiskList(diskBuilder);
    return new ZeusConfigurationImpl.Builder(cfgBuilder.build()).build();
  }

  public static InsightsInterfaceProto.GetEntitiesRet getHostGpuIdfEntityObj(boolean isVirtual, String... hostGpuUuids) {
    List<String> licenseList = new ArrayList<>();
    licenseList.add("test_license");
    InsightsInterfaceProto.GetEntitiesRet.Builder getEntitiesRet = InsightsInterfaceProto.GetEntitiesRet.newBuilder();
    Arrays.stream(hostGpuUuids).forEach(hostGpuUuid -> {
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("cluster_name")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("test_cluster_name"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair2 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("node")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue(HostTestUtils.HOST_UUID))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair3 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("cluster")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue(HostTestUtils.CLUSTER_UUID))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair4 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("host_id")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("2"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair5 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("num_vgpus_allocated")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setInt64Value(1L))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair6;
      if (isVirtual) {
        nameTimeValuePair6 = InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("gpu_type")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("kVirtual"))
          .build();
      } else {
        nameTimeValuePair6 = InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("gpu_type")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("kPassthroughGraphics"))
          .build();
      }
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair7 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("gpu_mode")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("kUnused"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair8 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("vendor_name")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("test_vendor_name"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair9;
      if (isVirtual) {
        nameTimeValuePair9 = InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("device_id")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("" + HOST_VIRTUAL_GPU_DEVICE_ID))
          .build();
      } else {
        nameTimeValuePair9 = InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("device_id")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("" + HOST_PHYSICAL_GPU_DEVICE_ID))
          .build();
      }
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair10 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("device_name")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("test_device_name"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair11 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("sbdf")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("test_sbdf"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair12 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("in_use")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setInt64Value(1L))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair13 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("assignable")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setInt64Value(1L))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair14 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("numa_node")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("test_numa_node"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair15 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("guest_driver_version")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("test_version"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair16 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("fraction")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setInt64Value(1L))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair17 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("license_list")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().
              setStrList(InsightsInterfaceProto.DataValue.StrList.newBuilder().addAllValueList(licenseList))).build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair18 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("_cluster_uuid_")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue(HostTestUtils.CLUSTER_UUID))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair19 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("test_name")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("testName"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair20 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("test_name")
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair21 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair22 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("testValue")).build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair23 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("frame_buffer_size_bytes")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setInt64Value(1L))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair24 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("num_virtual_display_heads")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setInt64Value(1L))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair25 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("max_resolution")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("test_resolution"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair26 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("max_instances_per_vm")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setInt64Value(1L))
          .build();

      InsightsInterfaceProto.Entity entity = InsightsInterfaceProto.Entity.newBuilder()
        .addAttributeDataMap(nameTimeValuePair)
        .addAttributeDataMap(nameTimeValuePair2)
        .addAttributeDataMap(nameTimeValuePair3)
        .addAttributeDataMap(nameTimeValuePair4)
        .addAttributeDataMap(nameTimeValuePair5)
        .addAttributeDataMap(nameTimeValuePair6)
        .addAttributeDataMap(nameTimeValuePair7)
        .addAttributeDataMap(nameTimeValuePair8)
        .addAttributeDataMap(nameTimeValuePair9)
        .addAttributeDataMap(nameTimeValuePair10)
        .addAttributeDataMap(nameTimeValuePair11)
        .addAttributeDataMap(nameTimeValuePair12)
        .addAttributeDataMap(nameTimeValuePair13)
        .addAttributeDataMap(nameTimeValuePair14)
        .addAttributeDataMap(nameTimeValuePair15)
        .addAttributeDataMap(nameTimeValuePair16)
        .addAttributeDataMap(nameTimeValuePair17)
        .addAttributeDataMap(nameTimeValuePair18)
        .addAttributeDataMap(nameTimeValuePair19)
        .addAttributeDataMap(nameTimeValuePair20)
        .addAttributeDataMap(nameTimeValuePair21)
        .addAttributeDataMap(nameTimeValuePair22)
        .addAttributeDataMap(nameTimeValuePair23)
        .addAttributeDataMap(nameTimeValuePair24)
        .addAttributeDataMap(nameTimeValuePair25)
        .addAttributeDataMap(nameTimeValuePair26)
        .build();

      getEntitiesRet.addEntity(entity);
    });
    return getEntitiesRet.build();
  }

  public static InsightsInterfaceProto.GetEntitiesRet getEmptyEntity(String... hostGpuUuids) {
    InsightsInterfaceProto.GetEntitiesRet.Builder getEntitiesRet = InsightsInterfaceProto.GetEntitiesRet.newBuilder();
    return getEntitiesRet.build();
  }

  public static InsightsInterfaceProto.GetEntitiesWithMetricsRet getEmptyMetricRetEntity() {
    InsightsInterfaceProto.GetEntitiesWithMetricsRet.Builder getEntitiesWithMetricRet = InsightsInterfaceProto.GetEntitiesWithMetricsRet.newBuilder();
    return getEntitiesWithMetricRet.build();
  }

  public static InsightsInterfaceProto.GetEntitiesRet getEntityWhereHostDoesNotBelongToCluster(String... hostUuid) {
    List<String> serviceList = new ArrayList<>();
    serviceList.add("AOS");
    InsightsInterfaceProto.GetEntitiesRet.Builder getEntitiesRet = InsightsInterfaceProto.GetEntitiesRet.newBuilder();
    Arrays.stream(hostUuid).forEach(host -> {
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("_cluster_uuid_")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue(ClusterTestUtils.CLUSTER_UUID_2))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair2 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("service_list")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrList(InsightsInterfaceProto.DataValue.StrList.newBuilder().addAllValueList(serviceList)))
          .build();
      InsightsInterfaceProto.Entity entity = InsightsInterfaceProto.Entity.newBuilder()
        .addAttributeDataMap(nameTimeValuePair)
        .addAttributeDataMap(nameTimeValuePair2).build();

      getEntitiesRet.addEntity(entity);
    });
    return getEntitiesRet.build();
  }

  public static InsightsInterfaceProto.GetEntitiesWithMetricsRet getHostGpuIdfEntityMetricObj(boolean isVirtual) {
    List<String> licenseList = new ArrayList<>();
    licenseList.add("test_license");

    InsightsInterfaceProto.MetricData metric1 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("cluster_name")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("testClusterName")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric2 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("node")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder().setStrValue(HostTestUtils.HOST_UUID)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric3 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("cluster")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue(HostTestUtils.CLUSTER_UUID)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric4 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("host_id")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("2")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric5 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("num_vgpus_allocated")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setInt64Value(1L)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric6 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("vm_uuid_list")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder().
            setStrList(InsightsInterfaceProto.DataValue.StrList.newBuilder().addAllValueList(licenseList))
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric7;
    if (isVirtual) {
      metric7 = InsightsInterfaceProto.MetricData.newBuilder()
        .setName("gpu_type")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("kVirtual")
            .build())
          .build())
        .build();
    } else {
      metric7 = InsightsInterfaceProto.MetricData.newBuilder()
        .setName("gpu_type")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("kPassthroughGraphics")
            .build())
          .build())
        .build();
    }
    InsightsInterfaceProto.MetricData metric8 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("gpu_mode")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("kUnused")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric9 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("vendor_name")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("test_vendor_name")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric10;
    if (isVirtual) {
     metric10 = InsightsInterfaceProto.MetricData.newBuilder()
        .setName("device_id")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("" + HOST_VIRTUAL_GPU_DEVICE_ID)
            .build())
          .build())
        .build();
    } else {
     metric10 = InsightsInterfaceProto.MetricData.newBuilder()
        .setName("device_id")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("" + HOST_PHYSICAL_GPU_DEVICE_ID)
            .build())
          .build())
        .build();
    }
    InsightsInterfaceProto.MetricData metric11 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("device_name")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("test_device_name")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric12 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("sbdf")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("test_sbdf")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric13 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("in_use")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder().
            setInt64Value(1L)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric14 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("assignable")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setInt64Value(1L)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric15 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("numa_node")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder().
            setStrValue("test_numa_node")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric16 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("guest_driver_version")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("test_version")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric17 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("fraction")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setInt64Value(1L)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric18 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("license_list")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder().
            setStrList(InsightsInterfaceProto.DataValue.StrList.newBuilder().addAllValueList(licenseList))
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric19 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("num_virtual_display_heads")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setInt64Value(1L)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric20 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("max_resolution")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("test_resolution")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric21 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("frame_buffer_size_bytes")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setInt64Value(1L)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric22 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("test_name")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("test_name")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric23 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("test_name")
        .build();
    InsightsInterfaceProto.MetricData metric24 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .build();
    InsightsInterfaceProto.MetricData metric25 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("X86_64")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric26 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("max_instances_per_vm")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setInt64Value(1L)
            .build())
          .build())
        .build();

    InsightsInterfaceProto.QueryGroupResult.Builder queryGroupResult =
      InsightsInterfaceProto.QueryGroupResult.newBuilder().setTotalEntityCount(2);
    InsightsInterfaceProto.EntityWithMetric entityWithMetric =
      InsightsInterfaceProto.EntityWithMetric.newBuilder()
        .setEntityGuid(
          InsightsInterfaceProto.EntityGuid.newBuilder().
            setEntityId(HOST_GPU_UUID).
            setEntityTypeName("host_gpu").
            build())
        .addMetricDataList(metric1)
        .addMetricDataList(metric2)
        .addMetricDataList(metric3)
        .addMetricDataList(metric4)
        .addMetricDataList(metric5)
        .addMetricDataList(metric6)
        .addMetricDataList(metric7)
        .addMetricDataList(metric8)
        .addMetricDataList(metric9)
        .addMetricDataList(metric10)
        .addMetricDataList(metric11)
        .addMetricDataList(metric12)
        .addMetricDataList(metric13)
        .addMetricDataList(metric14)
        .addMetricDataList(metric15)
        .addMetricDataList(metric16)
        .addMetricDataList(metric17)
        .addMetricDataList(metric18)
        .addMetricDataList(metric19)
        .addMetricDataList(metric20)
        .addMetricDataList(metric21)
        .addMetricDataList(metric22)
        .addMetricDataList(metric23)
        .addMetricDataList(metric24)
        .addMetricDataList(metric25)
        .addMetricDataList(metric26)
        .build();
    queryGroupResult.addRawResults(entityWithMetric);
    queryGroupResult.addRawResults(entityWithMetric);

    return InsightsInterfaceProto.GetEntitiesWithMetricsRet
      .newBuilder().addGroupResultsList(queryGroupResult).build();
  }

  public static Pair<Integer, List<HostNic> > getHostNicsObj() {
    List<HostNic> hostNicEntities = new ArrayList<>();
    HostNic hostNicEntity = new HostNic();
    hostNicEntity.setName("test_name");
    hostNicEntity.setExtId(HOST_NICS_UUID);
    hostNicEntities.add(hostNicEntity);
    Integer totalEntityCount = 1;
    return new Pair<>(totalEntityCount, hostNicEntities);
  }

  public static InsightsInterfaceProto.GetEntitiesRet getHostNicIdfEntityObj(String hostUuid, String... hostNicUuids){
    List<String> ipAddressList = new ArrayList<>();
    ipAddressList.add("fe80::225:90ff:fecb:3a2c");
    List<String> supportedCapabilitiesList = new ArrayList<>();
    supportedCapabilitiesList.add("test_supported_capabilities");

    InsightsInterfaceProto.GetEntitiesRet.Builder getEntitiesRet = InsightsInterfaceProto.GetEntitiesRet.newBuilder();

    Arrays.stream(hostNicUuids).forEach(hostNicUuid -> {
    InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair1 =
      InsightsInterfaceProto.NameTimeValuePair.newBuilder()
        .setName("port_name")
        .setValue(InsightsInterfaceProto.DataValue.newBuilder().setStrValue("test_host_nic_name"))
        .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair3 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("description")
          .setValue(InsightsInterfaceProto.DataValue.newBuilder().setStrValue("this is sample test description"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair4 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("mac_address")
          .setValue(InsightsInterfaceProto.DataValue.newBuilder().setStrValue("00:25:90:d8:77:3f"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair5 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("ipv4_addresses")
          .setValue(InsightsInterfaceProto.DataValue.newBuilder().setStrList(InsightsInterfaceProto.DataValue.StrList.newBuilder().addAllValueList(ipAddressList)))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair6 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("ipv6_addresses")
          .setValue(InsightsInterfaceProto.DataValue.newBuilder().setStrList(InsightsInterfaceProto.DataValue.StrList.newBuilder().addAllValueList(ipAddressList)))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair7 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("status")
          .setValue(InsightsInterfaceProto.DataValue.newBuilder().setStrValue("test_interface_status"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair8 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("dhcp_enabled")
          .setValue(InsightsInterfaceProto.DataValue.newBuilder().setBoolValue(true))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair9 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("link_speed_kbps")
          .setValue(InsightsInterfaceProto.DataValue.newBuilder().setInt64Value(30))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair10 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("mtu_bytes")
          .setValue(InsightsInterfaceProto.DataValue.newBuilder().setInt64Value(1500))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair12 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("node")
          .setValue(InsightsInterfaceProto.DataValue.newBuilder().setStrValue(hostUuid))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair13 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("discovery_protocol")
          .setValue(InsightsInterfaceProto.DataValue.newBuilder().setStrValue("LLDP"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair14 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("switch_dev_id")
          .setValue(InsightsInterfaceProto.DataValue.newBuilder().setStrValue("3"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair15 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("switch_port_id")
          .setValue(InsightsInterfaceProto.DataValue.newBuilder().setStrValue("80"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair16 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("switch_mgmt_ip_address")
          .setValue(InsightsInterfaceProto.DataValue.newBuilder().setStrValue("fe80::225:90ff:fecb:3a2c"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair17 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("switch_vlan_id")
          .setValue(InsightsInterfaceProto.DataValue.newBuilder().setStrValue("2022"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair18 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("switch_mac_addr")
          .setValue(InsightsInterfaceProto.DataValue.newBuilder().setStrValue("00:25:90:d8:77:3f"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair19 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("_cluster_uuid_")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue(HostTestUtils.CLUSTER_UUID))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair20 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("tx_ring_size")
          .setValue(InsightsInterfaceProto.DataValue.newBuilder().setInt64Value(20))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair21 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("rx_ring_size")
          .setValue(InsightsInterfaceProto.DataValue.newBuilder().setInt64Value(10))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair22 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("cluster_uuid")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue(HostTestUtils.CLUSTER_UUID))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair23 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
        .setName("nic_profile_id")
        .setValue(
          InsightsInterfaceProto.DataValue.newBuilder().setStrValue("test_nic_profile_id"))
        .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair24 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
        .setName("link_capacity")
        .setValue(InsightsInterfaceProto.DataValue.newBuilder().setInt64Value(10))
        .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair25 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
        .setName("supported_capabilities")
        .setValue(
          InsightsInterfaceProto.DataValue.newBuilder().setStrList(
            InsightsInterfaceProto.DataValue.StrList.newBuilder().addAllValueList(supportedCapabilitiesList)))
        .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair26 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
        .setName("driver_version")
        .setValue(
          InsightsInterfaceProto.DataValue.newBuilder().setStrValue("test_driver_version"))
        .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair27 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
        .setName("firmware_version")
        .setValue(
          InsightsInterfaceProto.DataValue.newBuilder().setStrValue("test_firmware_version"))
        .build();

      InsightsInterfaceProto.Entity entity = InsightsInterfaceProto.Entity.newBuilder()
        .addAttributeDataMap(nameTimeValuePair1)
        .addAttributeDataMap(nameTimeValuePair3)
        .addAttributeDataMap(nameTimeValuePair4)
        .addAttributeDataMap(nameTimeValuePair5)
        .addAttributeDataMap(nameTimeValuePair6)
        .addAttributeDataMap(nameTimeValuePair7)
        .addAttributeDataMap(nameTimeValuePair8)
        .addAttributeDataMap(nameTimeValuePair9)
        .addAttributeDataMap(nameTimeValuePair10)
        .addAttributeDataMap(nameTimeValuePair12)
        .addAttributeDataMap(nameTimeValuePair13)
        .addAttributeDataMap(nameTimeValuePair14)
        .addAttributeDataMap(nameTimeValuePair15)
        .addAttributeDataMap(nameTimeValuePair16)
        .addAttributeDataMap(nameTimeValuePair17)
        .addAttributeDataMap(nameTimeValuePair18)
        .addAttributeDataMap(nameTimeValuePair19)
        .addAttributeDataMap(nameTimeValuePair20)
        .addAttributeDataMap(nameTimeValuePair21)
        .addAttributeDataMap(nameTimeValuePair22)
        .addAttributeDataMap(nameTimeValuePair23)
        .addAttributeDataMap(nameTimeValuePair24)
        .addAttributeDataMap(nameTimeValuePair25)
        .addAttributeDataMap(nameTimeValuePair26)
        .addAttributeDataMap(nameTimeValuePair27)
        .build();

      getEntitiesRet.addEntity(entity);
    });
    return getEntitiesRet.build();
  }

  public static InsightsInterfaceProto.GetEntitiesWithMetricsRet getHostNicIdfEntityMetricObj(String hostUuid) {
    List<String> ipAddressList = new ArrayList<>();
    ipAddressList.add("fe80::225:90ff:fecb:3a2c");
    List<String> supportedCapabilitiesList = new ArrayList<>();
    supportedCapabilitiesList.add("test_supported_capabilities");

    InsightsInterfaceProto.MetricData metric1 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("port_name")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("test_host_nic_name")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric3 =
    InsightsInterfaceProto.MetricData.newBuilder()
      .setName("description")
      .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
        .setValue(InsightsInterfaceProto.DataValue.newBuilder()
          .setStrValue("this is sample test description")
          .build())
        .build())
      .build();
    InsightsInterfaceProto.MetricData metric4 =
    InsightsInterfaceProto.MetricData.newBuilder()
      .setName("mac_address")
      .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
        .setValue(InsightsInterfaceProto.DataValue.newBuilder()
          .setStrValue("00:25:90:d8:77:3f")
          .build())
        .build())
      .build();
    InsightsInterfaceProto.MetricData metric5 =
    InsightsInterfaceProto.MetricData.newBuilder()
      .setName("ipv4_addresses")
      .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
        .setValue(InsightsInterfaceProto.DataValue.newBuilder()
          .setStrList(InsightsInterfaceProto.DataValue.StrList.newBuilder().addAllValueList(ipAddressList))
          .build())
        .build())
      .build();
    InsightsInterfaceProto.MetricData metric6 =
    InsightsInterfaceProto.MetricData.newBuilder()
      .setName("ipv6_addresses")
      .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
        .setValue(InsightsInterfaceProto.DataValue.newBuilder()
          .setStrList(InsightsInterfaceProto.DataValue.StrList.newBuilder().addAllValueList(ipAddressList))
          .build())
        .build())
      .build();
    InsightsInterfaceProto.MetricData metric7 =
    InsightsInterfaceProto.MetricData.newBuilder()
      .setName("status")
      .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
        .setValue(InsightsInterfaceProto.DataValue.newBuilder()
          .setStrValue("test_interface_status")
          .build())
        .build())
      .build();
    InsightsInterfaceProto.MetricData metric8 =
    InsightsInterfaceProto.MetricData.newBuilder()
      .setName("dhcp_enabled")
      .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
        .setValue(InsightsInterfaceProto.DataValue.newBuilder()
          .setBoolValue(true)
          .build())
        .build())
      .build();
    InsightsInterfaceProto.MetricData metric9 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("link_speed_kbps")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setInt64Value(30)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric10 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("mtu_bytes")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setInt64Value(1500)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric12 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("node")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue(hostUuid)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric13 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("discovery_protocol")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("LLDP")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric14 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("switch_dev_id")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("3")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric15 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("switch_port_id")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("80")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric16 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("switch_mgmt_ip_address")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("fe80::225:90ff:fecb:3a2c")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric17 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("switch_vlan_id")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("2022")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric18 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("switch_mac_addr")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("00:25:90:d8:77:3f")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric19 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("_cluster_uuid_")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue(HostTestUtils.CLUSTER_UUID)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric20 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("tx_ring_size")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setInt64Value(20)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric21 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("rx_ring_size")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setInt64Value(10)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric22 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("cluster_uuid")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue(HostTestUtils.CLUSTER_UUID)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric23 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("nic_profile_id")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("test_nic_profile_id")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric24 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("link_capacity")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setInt64Value(10)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric25 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("supported_capabilities")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrList(InsightsInterfaceProto.DataValue.StrList.newBuilder().addAllValueList(supportedCapabilitiesList))
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric26 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("driver_version")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("test_driver_version")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric27 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("firmware_version")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("test_firmware_version")
            .build())
          .build())
        .build();
    return InsightsInterfaceProto.GetEntitiesWithMetricsRet.newBuilder().setTotalGroupCount(2)
      .addGroupResultsList(InsightsInterfaceProto.QueryGroupResult.newBuilder().
        addRawResults(InsightsInterfaceProto.EntityWithMetric.newBuilder()
            .setEntityGuid(InsightsInterfaceProto.EntityGuid.newBuilder().
              setEntityId(HOST_NICS_UUID).
              setEntityTypeName("host_nic").
              build())
            .setEntityGuid(InsightsInterfaceProto.EntityGuid.newBuilder().
              setEntityId(HOST_NICS_UUID).
              setEntityTypeName("host_nic").
              build())
            .addMetricDataList(metric1)
            .addMetricDataList(metric3)
            .addMetricDataList(metric4)
            .addMetricDataList(metric5)
            .addMetricDataList(metric6)
            .addMetricDataList(metric7)
            .addMetricDataList(metric8)
            .addMetricDataList(metric9)
            .addMetricDataList(metric10)
            .addMetricDataList(metric12)
            .addMetricDataList(metric13)
            .addMetricDataList(metric14)
            .addMetricDataList(metric15)
            .addMetricDataList(metric16)
            .addMetricDataList(metric17)
            .addMetricDataList(metric18)
            .addMetricDataList(metric19)
            .addMetricDataList(metric20)
            .addMetricDataList(metric21)
            .addMetricDataList(metric22)
            .addMetricDataList(metric23)
            .addMetricDataList(metric24)
            .addMetricDataList(metric25)
            .addMetricDataList(metric26)
            .addMetricDataList(metric27)
        ).build()).build();
  }

  public static HostNic getHostNicObj() {
    HostNic hostNicEntity = new HostNic();
    hostNicEntity.setName("test_name");
    hostNicEntity.setExtId(HOST_NICS_UUID);
    return hostNicEntity;
  }

  public static Pair<Integer, List<VirtualNic> > getVirtualNicsObj() {
    List<VirtualNic> virtualNicEntities = new ArrayList<>();
    VirtualNic virtualNicEntity = new VirtualNic();
    virtualNicEntity.setName("test_name");
    virtualNicEntity.setExtId(VIRTUAL_NICS_UUID);
    virtualNicEntities.add(virtualNicEntity);
    Integer totalEntityCount = 1;
    return new Pair<>(totalEntityCount, virtualNicEntities);
  }

  public static InsightsInterfaceProto.GetEntitiesRet getVirtualNicIdfEntityObj(String hostUuid, String... virtualNicUuids){
    List<String> ipAddressList = new ArrayList<>();
    ipAddressList.add("fe80::225:90ff:fecb:3a2c");

    List<String> hostNicUuidsList = new ArrayList<>();
    hostNicUuidsList.add(HOST_NICS_UUID);

    InsightsInterfaceProto.GetEntitiesRet.Builder getEntitiesRet = InsightsInterfaceProto.GetEntitiesRet.newBuilder();

    Arrays.stream(virtualNicUuids).forEach(virtualNicUuid -> {
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair1 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("name")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("test_virtual_nic_name"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair2 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("description")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("this is sample test description"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair3 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("node")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue(hostUuid))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair4 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("mac_address")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("00:25:90:d8:77:3f"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair5 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("ipv4_addresses")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrList(InsightsInterfaceProto.DataValue.StrList.newBuilder().addAllValueList(ipAddressList)))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair6 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("ipv6_addresses")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrList(InsightsInterfaceProto.DataValue.StrList.newBuilder().addAllValueList(ipAddressList)))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair7 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("status")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("test_interface_status"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair8 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("dhcp_enabled")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setBoolValue(true))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair9 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("link_speed_kbps")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setInt64Value(30))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair10 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("mtu_bytes")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setInt64Value(1500))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair11 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("vlan_id")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setInt64Value(2022))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair12 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("host_nic_uuids")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrList(InsightsInterfaceProto.DataValue.StrList.newBuilder().addAllValueList(hostNicUuidsList)))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair13 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("_cluster_uuid_")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue(HostTestUtils.CLUSTER_UUID))
          .build();

      InsightsInterfaceProto.Entity entity = InsightsInterfaceProto.Entity.newBuilder()
        .addAttributeDataMap(nameTimeValuePair1)
        .addAttributeDataMap(nameTimeValuePair3)
        .addAttributeDataMap(nameTimeValuePair4)
        .addAttributeDataMap(nameTimeValuePair5)
        .addAttributeDataMap(nameTimeValuePair6)
        .addAttributeDataMap(nameTimeValuePair7)
        .addAttributeDataMap(nameTimeValuePair8)
        .addAttributeDataMap(nameTimeValuePair9)
        .addAttributeDataMap(nameTimeValuePair10)
        .addAttributeDataMap(nameTimeValuePair12)
        .addAttributeDataMap(nameTimeValuePair13)
        .build();

      getEntitiesRet.addEntity(entity);
    });
    return getEntitiesRet.build();
  }

  public static InsightsInterfaceProto.GetEntitiesWithMetricsRet getVirtualNicIdfEntityMetricObj(String hostUuid) {
    List<String> ipAddressList = new ArrayList<>();
    ipAddressList.add("fe80::225:90ff:fecb:3a2c");

    List<String> hostNicUuidsList = new ArrayList<>();
    hostNicUuidsList.add(HOST_NICS_UUID);

    InsightsInterfaceProto.MetricData metric1 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("name")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("test_virtual_nic_name")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric2 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("description")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("this is sample test description")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric3 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("mac_address")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("00:25:90:d8:77:3f")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric4 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("ipv4_addresses")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrList(InsightsInterfaceProto.DataValue.StrList.newBuilder().addAllValueList(ipAddressList))
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric5 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("ipv6_addresses")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrList(InsightsInterfaceProto.DataValue.StrList.newBuilder().addAllValueList(ipAddressList))
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric6 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("status")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("test_interface_status")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric7 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("dhcp_enabled")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setBoolValue(true)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric8 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("link_speed_kbps")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setInt64Value(30)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric9 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("mtu_bytes")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setInt64Value(1500)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric10 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("node")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue(hostUuid)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric11 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("vlan_id")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setInt64Value(2022)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric12 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("host_nic_uuids")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrList(InsightsInterfaceProto.DataValue.StrList.newBuilder().addAllValueList(hostNicUuidsList))
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric13 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("_cluster_uuid_")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue(HostTestUtils.CLUSTER_UUID)
            .build())
          .build())
        .build();
    return InsightsInterfaceProto.GetEntitiesWithMetricsRet.newBuilder().setTotalGroupCount(2)
      .addGroupResultsList(InsightsInterfaceProto.QueryGroupResult.newBuilder().
        addRawResults(InsightsInterfaceProto.EntityWithMetric.newBuilder()
          .setEntityGuid(InsightsInterfaceProto.EntityGuid.newBuilder().
            setEntityId(VIRTUAL_NICS_UUID).
            setEntityTypeName("virtual_nic").
            build())
          .setEntityGuid(InsightsInterfaceProto.EntityGuid.newBuilder().
            setEntityId(VIRTUAL_NICS_UUID).
            setEntityTypeName("virtual_nic").
            build())
          .addMetricDataList(metric1)
          .addMetricDataList(metric2)
          .addMetricDataList(metric3)
          .addMetricDataList(metric4)
          .addMetricDataList(metric5)
          .addMetricDataList(metric6)
          .addMetricDataList(metric7)
          .addMetricDataList(metric8)
          .addMetricDataList(metric9)
          .addMetricDataList(metric10)
          .addMetricDataList(metric11)
          .addMetricDataList(metric12)
          .addMetricDataList(metric13)
        ).build()).build();

  }

  public static VirtualNic getVirtualNicObj(){
    VirtualNic virtualNicEntity = new VirtualNic();
    virtualNicEntity.setName("test_name");
    virtualNicEntity.setExtId(VIRTUAL_NICS_UUID);
    return virtualNicEntity;
  }

  public static InsightsInterfaceProto.GetEntitiesRet getNetworkSwitchIntfIdfEntityObj(String... switchIntfUuids) {
    List<String> hostNicUuidsList = new ArrayList<>();
    hostNicUuidsList.add(HOST_NICS_UUID);
    InsightsInterfaceProto.GetEntitiesRet.Builder getEntitiesRet = InsightsInterfaceProto.GetEntitiesRet.newBuilder();

    Arrays.stream(switchIntfUuids).forEach(switchIntfUuid -> {
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair1 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("state_last_change_time")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("12:10:22"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair2 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("type")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("test_type"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair3 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("switch_management_address")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("00:25:90:d8:77:3f"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair4 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("port_num")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("80"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair5 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("index")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setInt64Value(220))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair6 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("name")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("test_switch_intf_name"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair7 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("description")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("this is sample test description"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair8 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("speed_kbps")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setInt64Value(30))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair9 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("mtu_bytes")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setInt64Value(1500))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair10 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("node")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue(HOST_UUID))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair11 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("switch_uuid")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue(HOST_UUID))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair12 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("physical_address")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("00:25:90:d8:77:3f"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair13 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("host_nic_uuids")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrList(InsightsInterfaceProto.DataValue.StrList.newBuilder().addAllValueList(hostNicUuidsList)))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair14 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("_cluster_uuid_")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue(HostTestUtils.CLUSTER_UUID))
          .build();

      InsightsInterfaceProto.Entity entity = InsightsInterfaceProto.Entity.newBuilder()
        .addAttributeDataMap(nameTimeValuePair1)
        .addAttributeDataMap(nameTimeValuePair3)
        .addAttributeDataMap(nameTimeValuePair4)
        .addAttributeDataMap(nameTimeValuePair5)
        .addAttributeDataMap(nameTimeValuePair6)
        .addAttributeDataMap(nameTimeValuePair7)
        .addAttributeDataMap(nameTimeValuePair8)
        .addAttributeDataMap(nameTimeValuePair9)
        .addAttributeDataMap(nameTimeValuePair10)
        .addAttributeDataMap(nameTimeValuePair12)
        .addAttributeDataMap(nameTimeValuePair13)
        .addAttributeDataMap(nameTimeValuePair14)
        .build();

      getEntitiesRet.addEntity(entity);
    });
    return getEntitiesRet.build();
  }

  public static InsightsInterfaceProto.GetEntitiesWithMetricsRet getNetworkSwitchIntfIdfEntityMetricObj() {

    List<String> hostNicUuidsList = new ArrayList<>();
    hostNicUuidsList.add(HOST_NICS_UUID);

    InsightsInterfaceProto.MetricData metric1 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("name")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("test_switch_intf_name")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric2 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("description")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("this is sample test description")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric3 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("speed_kbps")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setInt64Value(30)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric4 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("mtu_bytes")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setInt64Value(1500)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric5 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("node")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue(HOST_UUID)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric6 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("_cluster_uuid_")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue(HostTestUtils.CLUSTER_UUID)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric7 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("host_nic_uuids")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrList(InsightsInterfaceProto.DataValue.StrList.newBuilder().addAllValueList(hostNicUuidsList))
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric8 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("state_last_change_time")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("12:10:22")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric9 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("type")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("test_type")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric10 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("switch_management_address")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("00:25:90:d8:77:3f")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric11 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("port_num")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("80")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric12 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("index")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setInt64Value(220)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric13 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("physical_address")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("00:25:90:d8:77:3f")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric14 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("switch_uuid")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue(HOST_UUID)
            .build())
          .build())
        .build();
    return InsightsInterfaceProto.GetEntitiesWithMetricsRet.newBuilder().setTotalGroupCount(2)
      .addGroupResultsList(InsightsInterfaceProto.QueryGroupResult.newBuilder().
        addRawResults(InsightsInterfaceProto.EntityWithMetric.newBuilder()
          .setEntityGuid(InsightsInterfaceProto.EntityGuid.newBuilder().
            setEntityId(NETWORK_SWITCH_INTF_UUID).
            setEntityTypeName("network_switch_interface").
            build())
          .setEntityGuid(InsightsInterfaceProto.EntityGuid.newBuilder().
            setEntityId(NETWORK_SWITCH_INTF_UUID).
            setEntityTypeName("network_switch_interface").
            build())
          .addMetricDataList(metric1)
          .addMetricDataList(metric2)
          .addMetricDataList(metric3)
          .addMetricDataList(metric4)
          .addMetricDataList(metric5)
          .addMetricDataList(metric6)
          .addMetricDataList(metric7)
          .addMetricDataList(metric8)
          .addMetricDataList(metric9)
          .addMetricDataList(metric10)
          .addMetricDataList(metric11)
          .addMetricDataList(metric12)
          .addMetricDataList(metric13)
          .addMetricDataList(metric14)
        ).build()).build();
  }
}
