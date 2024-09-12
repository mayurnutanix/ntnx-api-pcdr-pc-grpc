/*
 * Copyright (c) 2022 Nutanix Inc. All rights reserved.
 *
 * Author: ritik.nawal@nutanix.com
 */

package com.nutanix.clustermgmtserver.utils;

import com.nutanix.api.utils.stats.query.StatsQueryResponse;
import dp1.clu.clustermgmt.v4.stats.ClusterStats;
import dp1.clu.clustermgmt.v4.stats.HostStats;
import dp1.clu.common.v1.stats.DownSamplingOperator;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class StatsTestUtils {

  public static final String TEST_ARTIFACT_BASE_PATH = "src/test/java/com/nutanix/clustermgmtserver/resources";

  public static final String CLUSTER_UUID =
    "5cafbb40-dfd4-4eaf-ac8d-5ff91a230c7a";

  public static final String HOST_UUID =
    "19b0bb40-dfd4-4eaf-ac8d-5ff91a230c7a";

  public static ClusterStats getClusterStatsObj(){
    ClusterStats statsObj = new ClusterStats();
    statsObj.setExtId(CLUSTER_UUID);
    statsObj.setStatType(DownSamplingOperator.AVG);
    return statsObj;
  }

  public static HostStats getHostStatsObj(){
    HostStats statsObj = new HostStats();
    statsObj.setExtId(HOST_UUID);
    statsObj.setStatType(DownSamplingOperator.AVG);
    return statsObj;
  }

  public static Map<String, List<StatsQueryResponse.TimeValuePair>> getAttributeStatsMap(Boolean clusterStats, Boolean hostStats){
    Map<String, List<StatsQueryResponse.TimeValuePair>> attributeStatsMap = new HashMap<>();
    attributeStatsMap.put("controller_avg_io_latency_usecs", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
    attributeStatsMap.put("capacity.controller_avg_io_latency_usecs.upper_buff", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
    attributeStatsMap.put("capacity.controller_avg_io_latency_usecs.lower_buff", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
    attributeStatsMap.put("controller_avg_read_io_latency_usecs", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
    attributeStatsMap.put("capacity.controller_avg_read_io_latency_usecs.upper_buff", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
    attributeStatsMap.put("capacity.controller_avg_read_io_latency_usecs.lower_buff", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
    attributeStatsMap.put("controller_avg_write_io_latency_usecs", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
    attributeStatsMap.put("capacity.controller_avg_write_io_latency_usecs.upper_buff", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
    attributeStatsMap.put("capacity.controller_avg_write_io_latency_usecs.lower_buff", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
    attributeStatsMap.put("hypervisor_cpu_usage_ppm", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
    attributeStatsMap.put("capacity.hypervisor_cpu_usage_ppm.upper_buff", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
    attributeStatsMap.put("capacity.hypervisor_cpu_usage_ppm.lower_buff", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
    attributeStatsMap.put("aggregate_hypervisor_memory_usage_ppm", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
    attributeStatsMap.put("capacity.aggregate_hypervisor_memory_usage_ppm.upper_buff", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
    attributeStatsMap.put("capacity.aggregate_hypervisor_memory_usage_ppm.lower_buff", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
    attributeStatsMap.put("controller_num_iops", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
    attributeStatsMap.put("capacity.controller_num_iops.upper_buff", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
    attributeStatsMap.put("capacity.controller_num_iops.lower_buff", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
    attributeStatsMap.put("controller_num_read_iops", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
    attributeStatsMap.put("capacity.controller_num_read_iops.upper_buff", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
    attributeStatsMap.put("capacity.controller_num_read_iops.lower_buff", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
    attributeStatsMap.put("controller_num_write_iops", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
    attributeStatsMap.put("capacity.controller_num_write_iops.upper_buff", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "281683.011016685"))));
    attributeStatsMap.put("capacity.controller_num_write_iops.lower_buff", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
    attributeStatsMap.put("controller_io_bandwidth_kBps", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
    attributeStatsMap.put("capacity.controller_io_bandwidth_kBps.upper_buff", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
    attributeStatsMap.put("capacity.controller_io_bandwidth_kBps.lower_buff", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
    attributeStatsMap.put("controller_read_io_bandwidth_kBps", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
    attributeStatsMap.put("capacity.controller_read_io_bandwidth_kBps.upper_buff", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
    attributeStatsMap.put("capacity.controller_read_io_bandwidth_kBps.lower_buff", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
    attributeStatsMap.put("controller_write_io_bandwidth_kBps", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
    attributeStatsMap.put("capacity.controller_write_io_bandwidth_kBps.upper_buff", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
    attributeStatsMap.put("capacity.controller_write_io_bandwidth_kBps.lower_buff", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
    attributeStatsMap.put("storage.usage_bytes", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
    attributeStatsMap.put("storage.capacity_bytes", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
    attributeStatsMap.put("storage.free_bytes", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
    attributeStatsMap.put("check.score", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "74"))));
    attributeStatsMap.put("storage.recycle_bin_usage_bytes", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "0"))));
    attributeStatsMap.put("storage.snapshot_reclaimable_bytes", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "6438191104"))));
    attributeStatsMap.put("data_reduction.saved_bytes", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "511778816"))));
    attributeStatsMap.put("data_reduction.overall.saving_ratio_ppm", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "14694282"))));

    if(clusterStats){
      attributeStatsMap.put("overall_memory_usage_bytes", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
      attributeStatsMap.put("storage.logical_usage_bytes", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
    }

    if(hostStats){
      attributeStatsMap.put("overall_memory_usage_ppm", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
      attributeStatsMap.put("capacity.overall_memory_usage_ppm.upper_buff", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
      attributeStatsMap.put("capacity.overall_memory_usage_ppm.lower_buff", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
      attributeStatsMap.put("memory_size_bytes", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
      attributeStatsMap.put("cpu.capacity_hz", new ArrayList<>(Collections.singletonList(new StatsQueryResponse.TimeValuePair(-1L, "100"))));
    }
    return attributeStatsMap;
  }
}
