/*
 * Copyright (c) 2022 Nutanix Inc. All rights reserved.
 *
 * Author: ritik.nawal@nutanix.com
 */

package com.nutanix.clustermgmtserver.controllers;

import clustermgmt.v4.stats.ClustersApiControllerInterface;
import com.nutanix.api.utils.stats.service.exception.StatsGatewayServiceException;
import com.nutanix.api.utils.type.DateUtils;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtInvalidInputException;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceException;
import com.nutanix.clustermgmtserver.services.api.ClusterService;
import com.nutanix.clustermgmtserver.utils.ClustermgmtResponseFactory;
import com.nutanix.clustermgmtserver.utils.MetadataHateOsLinkUtils;
import com.nutanix.clustermgmtserver.utils.RpcErrorResponseHandler;
import dp1.clu.clustermgmt.v4.stats.ClusterStats;
import dp1.clu.clustermgmt.v4.stats.ClusterStatsApiResponse;
import dp1.clu.clustermgmt.v4.stats.HostStats;
import dp1.clu.clustermgmt.v4.stats.HostStatsApiResponse;
import dp1.clu.common.v1.response.ApiLink;
import dp1.clu.common.v1.stats.DownSamplingOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class StatsClusterApiControllerImpl implements ClustersApiControllerInterface {
  private static final String RPC_INVOKE_ERROR_MESSAGE = "Exception occurred while fetching stats through GraphQL Service";
  private ClusterService clusterService;
  private static final Integer DEFAULT_SAMPLING_INTERVAL = 30;
  public static final DownSamplingOperator DEFAULT_DOWNSAMPLING_OPERATOR = DownSamplingOperator.LAST;

  @Autowired
  public StatsClusterApiControllerImpl(final ClusterService clusterService) {
    this.clusterService = clusterService;
  }

  @Override
  public ResponseEntity<MappingJacksonValue> getClusterStats(String clusterExtId,
                                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime $startTime,
                                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime $endTime,
                                                             Integer $samplingInterval,
                                                             DownSamplingOperator $statType,
                                                             String $select,
                                                             Map<String, String> allQueryParams,
                                                             HttpServletRequest request,
                                                             HttpServletResponse response) {
    log.debug("Received query with params exdId {} $startTime: {} "+
        "$endTime: {} $samplingInterval: {} $statType: {} $select={}",
      clusterExtId, $startTime, $endTime, $samplingInterval,
      $statType, $select);

    HttpStatus httpStatus = HttpStatus.OK;
    ClusterStatsApiResponse clusterStatsApiResponse = new ClusterStatsApiResponse();

    Long startTimeEpoch=0L, endTimeEpoch=0L;
    Integer samplingInterval = DEFAULT_SAMPLING_INTERVAL;
    DownSamplingOperator samplingOperator = DEFAULT_DOWNSAMPLING_OPERATOR;

    // making samplingInterval>=30s allowable, otherwise default is taken
    if ($samplingInterval != null && $samplingInterval>DEFAULT_SAMPLING_INTERVAL) {
      samplingInterval = $samplingInterval;
    }

    if ($statType != null && $statType != DownSamplingOperator.$UNKNOWN) {
      samplingOperator = $statType;
    }

    try {

      if ($startTime == null || $endTime == null) {
        throw new ClustermgmtInvalidInputException("Both $startTime and $endTime have to be passed");
      } else {
        try {
          log.debug("Fetching time in epoch micros");
          startTimeEpoch = DateUtils.toEpochMicros($startTime);
          endTimeEpoch = DateUtils.toEpochMicros($endTime);
          Long currentSystemTimeEpoch = System.currentTimeMillis() * 1000L;
          if (startTimeEpoch > currentSystemTimeEpoch || endTimeEpoch > currentSystemTimeEpoch){
            throw new ClustermgmtInvalidInputException("$startTime or $endTime provided refer to time in future.");
          }else if(startTimeEpoch > endTimeEpoch){
            throw new ClustermgmtInvalidInputException("The provided $endTime occurs before $startTime.");
          }
        } catch (DateTimeParseException e) {
          throw new ClustermgmtInvalidInputException("Unable to parse timestamps with error: " + e.getMessage());
        }
        log.debug("Start time: {} End time: {}", startTimeEpoch, endTimeEpoch);
      }

      ClusterStats statsResponse = clusterService.getClusterStatsInfo(clusterExtId, samplingOperator, samplingInterval, startTimeEpoch ,endTimeEpoch, $select);
      clusterStatsApiResponse.setDataInWrapper(statsResponse);

      List<ApiLink> links = MetadataHateOsLinkUtils.getMetadataHateOsLinksForStatsEndpoint(ClustersApiControllerInterface.GET_CLUSTER_STATS_URI, clusterExtId);
      clusterStatsApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(links, false, false));

    } catch (ClustermgmtServiceException | StatsGatewayServiceException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      clusterStatsApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(clusterStatsApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> getHostStats(String clusterExtId,
                                                          String hostExtId,
                                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime $startTime,
                                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime $endTime,
                                                          Integer $samplingInterval,
                                                          DownSamplingOperator $statType,
                                                          String $select,
                                                          Map<String, String> allQueryParams,
                                                          HttpServletRequest request, HttpServletResponse response) {
    log.debug("Received query with params exdId {} $startTime: {} "+
        "$endTime: {} $samplingInterval: {} $statType: {} $select={}",
      clusterExtId, $startTime, $endTime, $samplingInterval,
      $statType, $select);

    HttpStatus httpStatus = HttpStatus.OK;
    HostStatsApiResponse hostStatsApiResponse = new HostStatsApiResponse();

    Long startTimeEpoch=0L, endTimeEpoch=0L;
    Integer samplingInterval = DEFAULT_SAMPLING_INTERVAL;
    DownSamplingOperator samplingOperator = DEFAULT_DOWNSAMPLING_OPERATOR;

    // making samplingInterval>=30s allowable, otherwise default is taken
    if ($samplingInterval != null && $samplingInterval>DEFAULT_SAMPLING_INTERVAL) {
      samplingInterval = $samplingInterval;
    }

    if ($statType != null && $statType != DownSamplingOperator.$UNKNOWN) {
      samplingOperator = $statType;
    }

    try {

      if ($startTime == null || $endTime == null) {
        throw new ClustermgmtInvalidInputException("Both $startTime and $endTime have to be passed");
      } else {
        try {
          log.debug("Fetching time in epoch micros");
          startTimeEpoch = DateUtils.toEpochMicros($startTime);
          endTimeEpoch = DateUtils.toEpochMicros($endTime);
          Long currentSystemTimeEpoch = System.currentTimeMillis() * 1000L;
          if (startTimeEpoch > currentSystemTimeEpoch || endTimeEpoch > currentSystemTimeEpoch){
            throw new ClustermgmtInvalidInputException("$startTime or $endTime provided refer to time in future.");
          }else if(startTimeEpoch > endTimeEpoch){
            throw new ClustermgmtInvalidInputException("The provided $endTime occurs before $startTime.");
          }
        } catch (DateTimeParseException e) {
          throw new ClustermgmtInvalidInputException("Unable to parse timestamps with error: " + e.getMessage());
        }
        log.debug("Start time: {} End time: {}", startTimeEpoch, endTimeEpoch);
      }

      HostStats statsResponse = clusterService.getHostStatsInfo(clusterExtId, hostExtId, samplingOperator, samplingInterval, startTimeEpoch ,endTimeEpoch, $select);
      hostStatsApiResponse.setDataInWrapper(statsResponse);

      List<ApiLink> links = MetadataHateOsLinkUtils.getMetadataHateOsLinksForStatsEndpoint(ClustersApiControllerInterface.GET_HOST_STATS_URI, clusterExtId, hostExtId);
      hostStatsApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(links, false, false));
    } catch (ClustermgmtServiceException | StatsGatewayServiceException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      hostStatsApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(hostStatsApiResponse), httpStatus);
  }
}
