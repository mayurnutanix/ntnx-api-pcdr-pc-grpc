/*
 * Copyright (c) 2022 Nutanix Inc. All rights reserved.
 *
 * Author: rakesh.falak@nutanix.com
 */

package com.nutanix.clustermgmtserver.utils;

import com.nutanix.clustermgmtserver.exceptions.ClustermgmtInvalidInputException;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceException;
import com.nutanix.net.RpcApacheHttpClient;
import com.nutanix.api.utils.stats.query.StatsQueryParams;
import com.nutanix.api.utils.stats.query.StatsQueryParams.Attribute;
import com.nutanix.api.utils.stats.query.StatsQueryParams.Sampling;
import com.nutanix.api.utils.stats.service.StatsService;
import com.nutanix.api.utils.stats.query.StatsQueryResponse;
import com.nutanix.api.utils.stats.service.exception.StatsGatewayServiceException;
import com.nutanix.api.utils.stats.service.graphql.GraphQlStatsService;
import com.nutanix.odata.core.uri.ParsedQuery;
import com.nutanix.odata.visitors.stats.StatsQueryEvaluator;
import dp1.clu.common.v1.stats.DownSamplingOperator;
import lombok.extern.slf4j.Slf4j;


import java.util.ArrayList;
import java.util.List;
@Slf4j
public class StatsUtils {
  private static final int DEFAULT_MAX_CONNECTION_PER_ROUTE = 5;
  private static final int MAX_TOTAL_CONNECTIONS = 20;
  private static final int VALIDATE_AFTER_INACTIVE_MSEC = 5000;
  private static final int KEEP_ALIVE_DURATION = 30;
  private static final String GRAPH_QL_SERVICE_IP = "127.0.0.1";
  private static final int GRAPH_QL_SERVICE_PORT = 8084;
  private static RpcApacheHttpClient rpcApacheHttpClient = new RpcApacheHttpClient(
    DEFAULT_MAX_CONNECTION_PER_ROUTE,
    MAX_TOTAL_CONNECTIONS, VALIDATE_AFTER_INACTIVE_MSEC, KEEP_ALIVE_DURATION);

  /*
   * Utility to run stats connector.
   */
  private static StatsQueryResponse fetchStats(StatsQueryParams statsQueryParams)
    throws StatsGatewayServiceException {
    StatsService statsGatewayService =
      new GraphQlStatsService(GRAPH_QL_SERVICE_IP, GRAPH_QL_SERVICE_PORT);
    return statsGatewayService.fetchStatsSync(rpcApacheHttpClient,
      statsQueryParams);
  }

  public static StatsQueryParams.Sampling convertDSOperator(DownSamplingOperator dsOperator) {
    switch (dsOperator) {
      case SUM:
        return StatsQueryParams.Sampling.SUM;
      case MIN:
        return StatsQueryParams.Sampling.MIN;
      case MAX:
        return StatsQueryParams.Sampling.MAX;
      case AVG:
        return StatsQueryParams.Sampling.AVG;
      case COUNT:
        return StatsQueryParams.Sampling.COUNT;
      case LAST:
      default:
        return StatsQueryParams.Sampling.LAST;
    }
  }
  /*
   * Utility function to drive stats connector to fetch statistics data
   * @param entityName - name of entity
   * @param extId - entity UUID
   * @param attributeList - list of metric attributes
   * @param statsType - type of sampling
   * @param samplingInterval - sampling interval in sec
   * @param startTime - sampling start time in msec
   * @param endTime - sampling end time in msec
   * @return StatsQueryResponse - stats query response
   * @throws ValidationException, StatsGatewayServiceException
   */
  public static StatsQueryResponse getStatsResp(String entityName, String extId,
                                                    List<String> attributeList, DownSamplingOperator statsType,
                                                    Integer samplingInterval, Long startTime, Long endTime,
                                                ParsedQuery parsedQuery)
    throws ClustermgmtServiceException, StatsGatewayServiceException {

    if (entityName == null || entityName.isEmpty() ||
      extId == null || extId.isEmpty()){
      throw new ClustermgmtInvalidInputException(
        "Stats query must specify entity name and UUID");
    }

    StatsQueryParams statsQueryParams = new StatsQueryParams();
    statsQueryParams.setQueryName("cluster_stats_query_" + entityName);
    statsQueryParams.setEntityName(entityName);

    List<String> entityIdList = new ArrayList<>();
    entityIdList.add(extId);
    statsQueryParams.setEntityIds(entityIdList);

    Sampling sampling = convertDSOperator(statsType);

    if (samplingInterval != null) {
      statsQueryParams.setDownSamplingIntervalSecs(samplingInterval.longValue());
    }
    if (startTime != null) {
      statsQueryParams.setStartTimeMsecs(startTime);
    }
    if (endTime != null) {
      statsQueryParams.setEndTimeMsecs(endTime);
    }

    List<String> attributesToFetch;
    if(parsedQuery==null) {

      attributesToFetch = new ArrayList<>(attributeList);
      statsQueryParams.setParsedStatsQueryParams(StatsQueryParams.ParsedStatsQueryParams.builder().build());
    }
    else{

      //$select applied over the attributes
      StatsQueryParams.ParsedStatsQueryParams parsedStatsQueryParams = StatsQueryEvaluator.getParsedStatsQueryParams(parsedQuery);
      List<String> selectedAttributeList = parsedStatsQueryParams.getAttributes();
      attributesToFetch = new ArrayList<>(selectedAttributeList);
      log.debug("Attributes selected to fetch stats: ");
      for (String attr: attributesToFetch){
        log.debug(attr);
      }
      statsQueryParams.setParsedStatsQueryParams(parsedStatsQueryParams);
    }

    List<Attribute> queryAttributeList = new ArrayList<>();
    for (String attribute : attributesToFetch) {
      StatsQueryParams.Attribute queryAttribute =
        new StatsQueryParams.Attribute();
      queryAttribute.setName(attribute);
      queryAttribute.setSamplingOperator(sampling);
      queryAttribute.setTimeSeries(true);
      queryAttributeList.add(queryAttribute);
    }
    statsQueryParams.setAttributes(queryAttributeList);

    statsQueryParams.setDisplayFilteredEntityCount(true);
    return fetchStats(statsQueryParams);

  }
}

