/*
 * Copyright (c) 2022 Nutanix Inc. All rights reserved.
 *
 * Author: ritik.nawal@nutanix.com
 */

package com.nutanix.clustermgmtserver.controllers;

import com.nutanix.api.utils.stats.service.exception.StatsGatewayServiceException;
import com.nutanix.api.utils.type.DateUtils;
import com.nutanix.clustermgmtserver.utils.*;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceException;
import com.nutanix.clustermgmtserver.services.api.ClusterService;
import com.nutanix.prism.service.EntityDbService;
import com.nutanix.prism.util.idempotency.IdempotencySupportService;
import dp1.clu.clustermgmt.v4.stats.ClusterStats;
import dp1.clu.clustermgmt.v4.stats.ClusterStatsApiResponse;
import dp1.clu.clustermgmt.v4.stats.HostStats;
import dp1.clu.clustermgmt.v4.stats.HostStatsApiResponse;
import dp1.clu.common.v1.response.ApiLink;
import lombok.extern.slf4j.Slf4j;
import mockit.Mock;
import mockit.MockUp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestExecutionListeners(MockitoTestExecutionListener.class)
@ActiveProfiles("test")
@Slf4j
public class StatsClusterApiControllerTest  extends AbstractTestNGSpringContextTests {
  @Autowired
  private TestRestTemplate testRestTemplate;

  @MockBean
  private ClusterService clusterService;

  @MockBean
  private EntityDbService entityDbService;

  @MockBean
  private ClustermgmtIdempotentTokenFactory clustermgmtIdempotentTokenFactory;

  @MockBean
  private IdempotencySupportService idempotencySupportService;

  private static final String CONFIG_BASE_URL = "/clustermgmt/v4/config/";
  private static final String STATS_BASE_URL = "/clustermgmt/v4/stats/";
  private static final String HATEOS_LINK_HREF_PREFIX = "https://localhost:9440/api";
  public static final String BAD_REQUEST_ERROR_STATUS_CODE = "400 BAD_REQUEST";
  public static final String QUERY_PARAMS_TIME_FORMAT = "?$startTime=%s&$endTime=%s&";
  private HttpHeaders httpHeaders;

  @AfterMethod
  public void tearDown() {
    reset(clusterService);
  }

  @BeforeClass
  public void setUp() {
    new MockUp<ClustermgmtResponseFactory>() {
      @Mock
      private String getBasePath() {
        return ClusterTestUtils.TEST_ARTIFACT_BASE_PATH;
      }
    };
  }

  @Test
  public void clusterStatsInfoTest() throws ClustermgmtServiceException, StatsGatewayServiceException {
    when(clusterService.getClusterStatsInfo(any(), any(), any(), any(), any(), any())).thenReturn(StatsTestUtils.getClusterStatsObj());
    ResponseEntity<ClusterStatsApiResponse> clusterStatsApiResponseResponseEntity =
      testRestTemplate.exchange( STATS_BASE_URL + "clusters/" + StatsTestUtils.CLUSTER_UUID + "?$startTime=2023-01-01T09:09:00Z&"+
    "$endTime=2023-01-03T09:09:00Z&$samplingInterval=90&$statType=SUM",
        HttpMethod.GET, null, ClusterStatsApiResponse.class);
    ClusterStats responseData = (ClusterStats) clusterStatsApiResponseResponseEntity.getBody().getData();
    List<ApiLink> links = clusterStatsApiResponseResponseEntity.getBody().getMetadata().getLinks();
    assertEquals(clusterStatsApiResponseResponseEntity.getStatusCode(), HttpStatus.OK);
    assertEquals(responseData.getExtId(), StatsTestUtils.CLUSTER_UUID);
    assertEquals(links.size(), 2);
    assertEquals(links.get(0).getRel(), "self");
    assertEquals(links.get(0).getHref(), HATEOS_LINK_HREF_PREFIX + STATS_BASE_URL + "clusters/" + StatsTestUtils.CLUSTER_UUID);
    assertEquals(links.get(1).getHref(), HATEOS_LINK_HREF_PREFIX + CONFIG_BASE_URL + "clusters/" + StatsTestUtils.CLUSTER_UUID);
  }

  @Test
  public void hostStatsInfoTest() throws ClustermgmtServiceException, StatsGatewayServiceException {
    when(clusterService.getHostStatsInfo(any(), any(), any(), any(), any(), any(), any())).thenReturn(StatsTestUtils.getHostStatsObj());
    ResponseEntity<HostStatsApiResponse> hostStatsApiResponseResponseEntity =
      testRestTemplate.exchange( STATS_BASE_URL + "clusters/" + StatsTestUtils.CLUSTER_UUID  + "/hosts/" + StatsTestUtils.HOST_UUID +
          "?$startTime=2023-01-01T09:09:00.000Z&$endTime=2023-01-03T09:09:00.000Z&$samplingInterval=90&$statType=SUM",
        HttpMethod.GET, null, HostStatsApiResponse.class);
    HostStats responseData =(HostStats) hostStatsApiResponseResponseEntity.getBody().getData();
    List<ApiLink> links = hostStatsApiResponseResponseEntity.getBody().getMetadata().getLinks();
    assertEquals(hostStatsApiResponseResponseEntity.getStatusCode(), HttpStatus.OK);
    assertEquals(responseData.getExtId(), StatsTestUtils.HOST_UUID);
    assertEquals(links.size(), 2);
    assertEquals(links.get(0).getRel(), "self");
    assertEquals(links.get(0).getHref(), HATEOS_LINK_HREF_PREFIX + STATS_BASE_URL + "clusters/" + StatsTestUtils.CLUSTER_UUID + "/hosts/" + StatsTestUtils.HOST_UUID);
    assertEquals(links.get(1).getHref(), HATEOS_LINK_HREF_PREFIX + CONFIG_BASE_URL + "clusters/" + StatsTestUtils.CLUSTER_UUID + "/hosts/" + StatsTestUtils.HOST_UUID);
  }

  @Test
  public void basicValidationsTestForStats() throws Exception{
    //Test1: $startTime or $endTime missing from query params
    ResponseEntity<ClusterStatsApiResponse> clusterStatsApiResponseResponseEntity =
      testRestTemplate.exchange( STATS_BASE_URL + "clusters/" + StatsTestUtils.CLUSTER_UUID + "?$startTime=2023-01-01T09:09:00.000Z&"+
          "$samplingInterval=90&$statType=SUM",
        HttpMethod.GET, null, ClusterStatsApiResponse.class);
    assertEquals(clusterStatsApiResponseResponseEntity.getStatusCode().toString(),  BAD_REQUEST_ERROR_STATUS_CODE);
    //assertEquals(clusterStatsApiResponseResponseEntity.getBody().getData().toString(), ERR_MSG1);
    ResponseEntity<HostStatsApiResponse> hostStatsApiResponseResponseEntity =
      testRestTemplate.exchange( STATS_BASE_URL + "clusters/" + StatsTestUtils.CLUSTER_UUID  + "/hosts/" + StatsTestUtils.HOST_UUID +
          "?$endTime=2023-01-03T09:09:00.000Z&$samplingInterval=90&$statType=SUM",
        HttpMethod.GET, null, HostStatsApiResponse.class);
    assertEquals(hostStatsApiResponseResponseEntity.getStatusCode().toString(), BAD_REQUEST_ERROR_STATUS_CODE);

    //Test2: Invalid format of $startTime/$endTime passed
     clusterStatsApiResponseResponseEntity =
      testRestTemplate.exchange( STATS_BASE_URL + "clusters/" + StatsTestUtils.CLUSTER_UUID + "?$startTime=2023-01-01T09:09:00.?Z&"+
          "$endTime=2023-01-03T09:09:00.000Z&$samplingInterval=90&$statType=SUM",
        HttpMethod.GET, null, ClusterStatsApiResponse.class);
    assertEquals(clusterStatsApiResponseResponseEntity.getStatusCode().toString(), BAD_REQUEST_ERROR_STATUS_CODE);
    hostStatsApiResponseResponseEntity =
      testRestTemplate.exchange( STATS_BASE_URL + "clusters/" + StatsTestUtils.CLUSTER_UUID  + "/hosts/" + StatsTestUtils.HOST_UUID +
          "?$startTime=2023-01-01T09:09:00.000Z&$endTime=2023-01-03T09:09:00.00T&$samplingInterval=90&$statType=SUM",
        HttpMethod.GET, null, HostStatsApiResponse.class);
    assertEquals(hostStatsApiResponseResponseEntity.getStatusCode().toString(), BAD_REQUEST_ERROR_STATUS_CODE);

    //Test3: Timestamps exceed the current system time
    Long currentSystemTimeEpoch = System.currentTimeMillis() * 1000L;
    String startTime = "2023-03-30T09:09:00.00Z";
    String endTime = DateUtils.fromEpochMicros(currentSystemTimeEpoch + 1000000000L).toString();
    String timestampQueryParams = String.format(QUERY_PARAMS_TIME_FORMAT, startTime, endTime);
    clusterStatsApiResponseResponseEntity =
      testRestTemplate.exchange( STATS_BASE_URL + "clusters/" + StatsTestUtils.CLUSTER_UUID + timestampQueryParams  +"$samplingInterval=90&$statType=SUM",
        HttpMethod.GET, null, ClusterStatsApiResponse.class);
    assertEquals(clusterStatsApiResponseResponseEntity.getStatusCode().toString(), BAD_REQUEST_ERROR_STATUS_CODE);
    hostStatsApiResponseResponseEntity =
      testRestTemplate.exchange( STATS_BASE_URL + "clusters/" + StatsTestUtils.CLUSTER_UUID  + "/hosts/" + StatsTestUtils.HOST_UUID +
          timestampQueryParams + "$samplingInterval=90&$statType=SUM",
        HttpMethod.GET, null, HostStatsApiResponse.class);
    assertEquals(hostStatsApiResponseResponseEntity.getStatusCode().toString(), BAD_REQUEST_ERROR_STATUS_CODE);

    //Test4: $startTime > $endTime
    //Long currentSystemTimeEpoch = System.currentTimeMillis() * 1000L;
    startTime = "2023-03-30T09:09:00.00Z";
    endTime = "2023-03-30T08:09:00.00Z";
    timestampQueryParams = String.format(QUERY_PARAMS_TIME_FORMAT, startTime, endTime);
    clusterStatsApiResponseResponseEntity =
      testRestTemplate.exchange( STATS_BASE_URL + "clusters/" + StatsTestUtils.CLUSTER_UUID + timestampQueryParams  +"$samplingInterval=90&$statType=SUM",
        HttpMethod.GET, null, ClusterStatsApiResponse.class);
    assertEquals(clusterStatsApiResponseResponseEntity.getStatusCode().toString(), BAD_REQUEST_ERROR_STATUS_CODE);
    hostStatsApiResponseResponseEntity =
      testRestTemplate.exchange( STATS_BASE_URL + "clusters/" + StatsTestUtils.CLUSTER_UUID  + "/hosts/" + StatsTestUtils.HOST_UUID +
          timestampQueryParams + "$samplingInterval=90&$statType=SUM",
        HttpMethod.GET, null, HostStatsApiResponse.class);
    assertEquals(hostStatsApiResponseResponseEntity.getStatusCode().toString(), BAD_REQUEST_ERROR_STATUS_CODE);
  }
}
