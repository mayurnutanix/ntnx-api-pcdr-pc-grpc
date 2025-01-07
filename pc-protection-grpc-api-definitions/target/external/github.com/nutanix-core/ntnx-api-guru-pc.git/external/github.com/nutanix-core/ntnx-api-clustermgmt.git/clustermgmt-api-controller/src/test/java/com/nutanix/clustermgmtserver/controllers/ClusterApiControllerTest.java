/*
 * Copyright (c) 2021 Nutanix Inc. All rights reserved.
 *
 * Author: rakesh.falak@nutanix.com
 */

package com.nutanix.clustermgmtserver.controllers;

import clustermgmt.v4.config.ClustersApiControllerInterface;
import com.nutanix.api.utils.task.ErgonTaskUtils;
import com.nutanix.clustermgmtserver.exceptions.*;
import com.nutanix.clustermgmtserver.services.api.ClusterService;
import com.nutanix.clustermgmtserver.utils.*;
import com.nutanix.prism.exception.idempotency.IdempotencySupportException;
import com.nutanix.prism.service.EntityDbService;
import com.nutanix.prism.util.idempotency.IdempotencySupportService;
import com.nutanix.util.base.Pair;
import com.nutanix.util.base.ValidationException;
import dp1.clu.clustermgmt.v4.config.*;
import dp1.clu.clustermgmt.v4.operations.*;
import dp1.clu.clustermgmt.v4.error.AppMessage;
import dp1.clu.clustermgmt.v4.error.ErrorResponse;
import dp1.clu.common.v1.response.ApiLink;
import dp1.clu.prism.v4.config.TaskReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;

import static org.mockito.Mockito.*;
import static org.springframework.http.HttpHeaders.IF_MATCH;
import org.testng.annotations.AfterMethod;
import mockit.Mock;
import mockit.MockUp;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestExecutionListeners(MockitoTestExecutionListener.class)
@ActiveProfiles("test")
@Slf4j
public class ClusterApiControllerTest extends AbstractTestNGSpringContextTests {
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

  private static final String CLUSTER_URL = "/clustermgmt/v4/config/clusters/";
  private static final String BASE_URL = "/clustermgmt/v4/config/";
  private static final String HATEOS_LINK_HREF_PREFIX = "https://localhost:9440/api";
  private static final String HATEOS_TASK_LINK = "https://localhost:9440/api/nutanix/v3/tasks/cfb08be0-3f01-446c-6649-dc9303394c91";
  private HttpHeaders httpHeaders;

  @AfterMethod
  public void tearDown() {
    reset(clusterService);
    reset(clustermgmtIdempotentTokenFactory);
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
  public void getClusterTest() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.getClusterEntity(any(), any())).thenReturn(ClusterTestUtils.getClusterEntityObj());

    final String clusterUuid = TestConstantUtils.randomUUIDString();
    ResponseEntity<GetClusterApiResponse> getClusterApiResponse=
      testRestTemplate.exchange(CLUSTER_URL + clusterUuid,
        HttpMethod.GET, null, GetClusterApiResponse.class);
    Cluster responseData = (Cluster) getClusterApiResponse.getBody().getData();
    List<ApiLink> links = getClusterApiResponse.getBody().getMetadata().getLinks();
    assertEquals(responseData.getName(), ClusterTestUtils.CLUSTER_NAME);
    assertEquals(getClusterApiResponse.getStatusCode(), HttpStatus.OK);
    assertEquals(links.size(), 14);
    assertEquals(links.get(0).getRel(), "self");
    assertEquals(links.get(0).getHref(), HATEOS_LINK_HREF_PREFIX + CLUSTER_URL + clusterUuid);
  }

  @Test
  public void getClusterTestInCaseOfException() throws ClustermgmtServiceException, ValidationException {
    final ErrorCode errorCode = ErrorCode.CLUSTERMGMT_SERVICE_ERROR;
    final String clusterUuid = TestConstantUtils.randomUUIDString();
    when(clusterService.getClusterEntity(any(), any())).
      thenThrow(new ClustermgmtServiceException("unkmnow cluster uuid: " + clusterUuid));

    ResponseEntity<GetClusterApiResponse> getClusterApiResponse=
      testRestTemplate.exchange(CLUSTER_URL + clusterUuid,
        HttpMethod.GET, null, GetClusterApiResponse.class);
    GetClusterApiResponse responseData = getClusterApiResponse.getBody();
    final ErrorResponse errorResponse = (ErrorResponse) responseData.getData();
    List<AppMessage> messageList = (List<AppMessage>) errorResponse.getError();
    final AppMessage appMessage = messageList.get(0);
    assertEquals(appMessage.getCode(), "CLU-" + errorCode.getStandardCode());
    assertEquals(appMessage.getArgumentsMap().size(), 1);
    assertEquals(appMessage.getErrorGroup(), "CLUSTERMGMT_SERVICE_ERROR");
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void getClustersTest() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.getClusterEntities(any(), any(), any(), any(), any(), any(), any())).thenReturn(ClusterTestUtils.getClusterEntitiesObj());
    final List<String> queryStrings = Arrays.asList("$page=0", "$page=-1", "$limit=-1", "$limit=501", "$limit=500");
    for(String queryString: queryStrings) {
      ResponseEntity<ListClustersApiResponse> getClustersApiResponse =
        testRestTemplate.exchange(ClustersApiControllerInterface.LIST_CLUSTERS_URI + "?" + queryString,
          HttpMethod.GET, null, ListClustersApiResponse.class);
      List<Cluster> responseData = (List<Cluster>) getClustersApiResponse.getBody().getData();
      assertEquals(getClustersApiResponse.getStatusCode(), HttpStatus.OK);
      assertEquals(responseData.get(0).getName(), ClusterTestUtils.CLUSTER_NAME);
    }
  }

  @Test
  public void getClustersTestInCaseOfValidationException() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.getClusterEntities(any(), any(), any(), any(), any(), any(), any())).
      thenThrow(new ValidationException("Failure in parsing Odata"));
    ResponseEntity<ListClustersApiResponse> getClustersApiResponse =
      testRestTemplate.exchange(ClustersApiControllerInterface.LIST_CLUSTERS_URI,
        HttpMethod.GET, null, ListClustersApiResponse.class);
    ListClustersApiResponse responseData = getClustersApiResponse.getBody();
    final ErrorResponse errorResponse = (ErrorResponse) responseData.getData();
    List<AppMessage> messageList = (List<AppMessage>) errorResponse.getError();
    final AppMessage appMessage = messageList.get(0);
    assertEquals(appMessage.getCode(), "CLU-" + "10006");
    assertEquals(appMessage.getArgumentsMap().size(), 1);
    assertEquals(appMessage.getErrorGroup(), "CLUSTERMGMT_INVALID_INPUT");
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void getClustersTestInCaseOfException() throws ClustermgmtServiceException, ValidationException {
    final ErrorCode errorCode = ErrorCode.CLUSTERMGMT_ZKCONFIG_ERROR;
    when(clusterService.getClusterEntities(any(), any(), any(), any(), any(), any(), any())).
      thenThrow(new ClustermgmtZkConfigReadException("Failed to read zeus config"));
    ResponseEntity<ListClustersApiResponse> getClustersApiResponse =
      testRestTemplate.exchange(ClustersApiControllerInterface.LIST_CLUSTERS_URI,
        HttpMethod.GET, null, ListClustersApiResponse.class);
    ListClustersApiResponse responseData = getClustersApiResponse.getBody();
    final ErrorResponse errorResponse = (ErrorResponse) responseData.getData();
    List<AppMessage> messageList = (List<AppMessage>) errorResponse.getError();
    final AppMessage appMessage = messageList.get(0);
    assertEquals(appMessage.getCode(), "CLU-" + errorCode.getStandardCode());
    assertEquals(appMessage.getArgumentsMap().size(), 1);
    assertEquals(appMessage.getErrorGroup(), "CLUSTERMGMT_ZKCONFIG_READ_ERROR");
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void getSnmpTest() throws ClustermgmtServiceException {
    when(clusterService.getSnmpConfig(any())).thenReturn(ClusterTestUtils.getSnmpConfigObj());
    final String clusterUuid = TestConstantUtils.randomUUIDString();

    ResponseEntity<GetSnmpConfigByClusterIdApiResponse> getSnmpApiResponse =
      testRestTemplate.exchange(CLUSTER_URL + clusterUuid + "/snmp",
        HttpMethod.GET, null, GetSnmpConfigByClusterIdApiResponse.class);
    SnmpConfig responseData = (SnmpConfig) getSnmpApiResponse.getBody().getData();
    List<ApiLink> links = getSnmpApiResponse.getBody().getMetadata().getLinks();
    assertEquals(getSnmpApiResponse.getStatusCode(), HttpStatus.OK);
    assertEquals(responseData.getIsEnabled(), ClusterTestUtils.SNMP_STATUS);
    assertEquals(links.size(), 6);
    assertEquals(links.get(0).getRel(), "self");
    assertEquals(links.get(0).getHref(), HATEOS_LINK_HREF_PREFIX + CLUSTER_URL + clusterUuid + "/snmp");
  }

  @Test
  public void getSnmpTestInCaseOfException() throws ClustermgmtServiceException {
    final ErrorCode errorCode = ErrorCode.CLUSTERMGMT_SERVICE_UNKNOWN_ENTITY;
    final String clusterUuid = TestConstantUtils.randomUUIDString();
    when(clusterService.getSnmpConfig(any())).
      thenThrow(new ClustermgmtNotFoundException("unkmnow cluster uuid: " + clusterUuid));

    ResponseEntity<GetSnmpConfigByClusterIdApiResponse> getSnmpApiResponse =
      testRestTemplate.exchange(CLUSTER_URL + clusterUuid + "/snmp",
        HttpMethod.GET, null, GetSnmpConfigByClusterIdApiResponse.class);
    GetSnmpConfigByClusterIdApiResponse responseData = getSnmpApiResponse.getBody();
    final ErrorResponse errorResponse = (ErrorResponse) responseData.getData();
    List<AppMessage> messageList = (List<AppMessage>) errorResponse.getError();
    final AppMessage appMessage = messageList.get(0);
    assertEquals(appMessage.getCode(), "CLU-" + errorCode.getStandardCode());
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void getConfigCredentialsTest() throws ClustermgmtServiceException {
    when(clusterService.getConfigCredentials(any())).thenReturn(ClusterTestUtils.getConfigCredentialsObj());
    final String clusterUuid = TestConstantUtils.randomUUIDString();

    ResponseEntity<GetConfigCredentialsByClusterIdApiResponse> getConfigCredentialsApiResponse =
      testRestTemplate.exchange(CLUSTER_URL + clusterUuid + "/credentials",
        HttpMethod.GET, null, GetConfigCredentialsByClusterIdApiResponse.class);
    ConfigCredentials responseData = (ConfigCredentials) getConfigCredentialsApiResponse.getBody().getData();
    assertEquals(getConfigCredentialsApiResponse.getStatusCode(), HttpStatus.OK);
    assertEquals(responseData.getHttpProxy().getUsername(), ClusterTestUtils.HTTP_PROXY_USERNAME);
  }

  @Test
  public void getConfigCredentialsTestInCaseOfException() throws ClustermgmtServiceException {
    final ErrorCode errorCode = ErrorCode.CLUSTERMGMT_SERVICE_UNKNOWN_ENTITY;
    final String clusterUuid = TestConstantUtils.randomUUIDString();
    when(clusterService.getConfigCredentials(any())).
      thenThrow(new ClustermgmtNotFoundException("unkmnow cluster uuid: " + clusterUuid));

    ResponseEntity<GetConfigCredentialsByClusterIdApiResponse> getConfigCredentialsApiResponse =
      testRestTemplate.exchange(CLUSTER_URL + clusterUuid + "/credentials",
        HttpMethod.GET, null, GetConfigCredentialsByClusterIdApiResponse.class);
    GetConfigCredentialsByClusterIdApiResponse responseData = getConfigCredentialsApiResponse.getBody();
    final ErrorResponse errorResponse = (ErrorResponse) responseData.getData();
    List<AppMessage> messageList = (List<AppMessage>) errorResponse.getError();
    final AppMessage appMessage = messageList.get(0);
    assertEquals(appMessage.getCode(), "CLU-" + errorCode.getStandardCode());
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void getRyslogServerTest() throws ClustermgmtServiceException {
    when(clusterService.getRsyslogServerConfig(any())).thenReturn(ClusterTestUtils.getRsyslogConfigObj());
    final String clusterUuid = TestConstantUtils.randomUUIDString();

    ResponseEntity<ListRsyslogServersByClusterIdApiResponse> getRsyslogServer =
      testRestTemplate.exchange(CLUSTER_URL + clusterUuid + "/rsyslog-servers",
        HttpMethod.GET, null, ListRsyslogServersByClusterIdApiResponse.class);
    List<RsyslogServer> responseData = (List<RsyslogServer>) getRsyslogServer.getBody().getData();
    assertEquals(getRsyslogServer.getStatusCode(), HttpStatus.OK);
    assertEquals(responseData.get(0).getServerName(), ClusterTestUtils.RSYSLOG_SERVER_NAME);
  }

  @Test
  public void getRyslogServerTestInCaseOfException() throws ClustermgmtServiceException {
    final ErrorCode errorCode = ErrorCode.CLUSTERMGMT_SERVICE_UNKNOWN_ENTITY;
    final String clusterUuid = TestConstantUtils.randomUUIDString();
    when(clusterService.getRsyslogServerConfig(any())).
      thenThrow(new ClustermgmtNotFoundException("unknown cluster uuid: " + clusterUuid));

    ResponseEntity<ListRsyslogServersByClusterIdApiResponse> getRsyslogServer =
      testRestTemplate.exchange(CLUSTER_URL + clusterUuid + "/rsyslog-servers",
        HttpMethod.GET, null, ListRsyslogServersByClusterIdApiResponse.class);
    ListRsyslogServersByClusterIdApiResponse responseData = getRsyslogServer.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
    final ErrorResponse errorResponse = (ErrorResponse) responseData.getData();
    List<AppMessage> messageList = (List<AppMessage>) errorResponse.getError();
    final AppMessage appMessage = messageList.get(0);
    assertEquals(appMessage.getCode(), "CLU-" + errorCode.getStandardCode());
  }

  @Test
  public void getRackableUnitsTest() throws ClustermgmtServiceException {
    when(clusterService.getRackableUnits(any())).thenReturn(ClusterTestUtils.getRackableUnitsObj());
    final String clusterUuid = TestConstantUtils.randomUUIDString();

    ResponseEntity<ListRackableUnitsByClusterIdApiResponse> getRackableUnitsResponseResponseEntity =
      testRestTemplate.exchange(CLUSTER_URL + clusterUuid + "/rackable-units",
        HttpMethod.GET, null, ListRackableUnitsByClusterIdApiResponse.class);
    List<RackableUnit> responseData = (List<RackableUnit>) getRackableUnitsResponseResponseEntity.getBody().getData();
    assertEquals(getRackableUnitsResponseResponseEntity.getStatusCode(), HttpStatus.OK);
    assertEquals(responseData.get(0).getExtId(), ClusterTestUtils.RACKABLE_UNIT_UUID);
  }

  @Test
  public void getRackableUnitsTestInCaseOfException() throws ClustermgmtServiceException {
    final ErrorCode errorCode = ErrorCode.CLUSTERMGMT_SERVICE_UNKNOWN_ENTITY;
    final String clusterUuid = TestConstantUtils.randomUUIDString();
    when(clusterService.getRackableUnits(any())).
      thenThrow(new ClustermgmtNotFoundException("unkmnow cluster uuid: " + clusterUuid));

    ResponseEntity<ListRackableUnitsByClusterIdApiResponse> getRackableUnitsResponseResponseEntity =
      testRestTemplate.exchange(CLUSTER_URL + clusterUuid + "/rackable-units",
        HttpMethod.GET, null, ListRackableUnitsByClusterIdApiResponse.class);
    ListRackableUnitsByClusterIdApiResponse responseData = getRackableUnitsResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
    final ErrorResponse errorResponse = (ErrorResponse) responseData.getData();
    List<AppMessage> messageList = (List<AppMessage>) errorResponse.getError();
    final AppMessage appMessage = messageList.get(0);
    assertEquals(appMessage.getCode(), "CLU-" + errorCode.getStandardCode());
  }

  @Test
  public void getRackableUnitTest() throws ClustermgmtServiceException {
    when(clusterService.getRackableUnit(any(), any())).thenReturn(ClusterTestUtils.getRackableUnitObj());
    final String clusterUuid = TestConstantUtils.randomUUIDString();
    final String rackableUnitUuid = TestConstantUtils.randomUUIDString();

    ResponseEntity<GetRackableUnitApiResponse> getRackableUnitResponseResponseEntity =
      testRestTemplate.exchange(CLUSTER_URL + clusterUuid + "/rackable-units/" + rackableUnitUuid,
        HttpMethod.GET, null, GetRackableUnitApiResponse.class);
    RackableUnit responseData = (RackableUnit) getRackableUnitResponseResponseEntity.getBody().getData();
    List<ApiLink> links = getRackableUnitResponseResponseEntity.getBody().getMetadata().getLinks();
    assertEquals(getRackableUnitResponseResponseEntity.getStatusCode(), HttpStatus.OK);
    assertEquals(responseData.getExtId(), ClusterTestUtils.RACKABLE_UNIT_UUID);
    assertEquals(links.size(), 1);
    assertEquals(links.get(0).getRel(), "self");
    assertEquals(links.get(0).getHref(), HATEOS_LINK_HREF_PREFIX + CLUSTER_URL + clusterUuid + "/rackable-units/" + rackableUnitUuid);
  }

  @Test
  public void getRackableUnitTestInCaseOfException() throws ClustermgmtServiceException {
    final ErrorCode errorCode = ErrorCode.CLUSTERMGMT_SERVICE_UNKNOWN_ENTITY;
    final String clusterUuid = TestConstantUtils.randomUUIDString();
    final String rackableUnitUuid = TestConstantUtils.randomUUIDString();
    when(clusterService.getRackableUnit(any(), any())).
      thenThrow(new ClustermgmtNotFoundException("unkmnow cluster uuid: " + clusterUuid));

    ResponseEntity<GetRackableUnitApiResponse> getRackableUnitResponseResponseEntity =
      testRestTemplate.exchange(CLUSTER_URL + clusterUuid + "/rackable-units/" + rackableUnitUuid,
        HttpMethod.GET, null, GetRackableUnitApiResponse.class);
    GetRackableUnitApiResponse responseData = getRackableUnitResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
    final ErrorResponse errorResponse = (ErrorResponse) responseData.getData();
    List<AppMessage> messageList = (List<AppMessage>) errorResponse.getError();
    final AppMessage appMessage = messageList.get(0);
    assertEquals(appMessage.getCode(), "CLU-" + errorCode.getStandardCode());
  }

  @Test
  public void getDomainFaultToleranceStatusTest() throws ClustermgmtServiceException {
    when(clusterService.getDomainFaultToleranceStatus(any())).thenReturn(ClusterTestUtils.getDomainFaultToleranceObj());
    final String clusterUuid = TestConstantUtils.randomUUIDString();

    ResponseEntity<GetFaultToleranceStatusByClusterIdApiResponse> getDomainFaultToleranceResponseResponseEntity =
      testRestTemplate.exchange(CLUSTER_URL + clusterUuid + "/fault-tolerance-status",
        HttpMethod.GET, null, GetFaultToleranceStatusByClusterIdApiResponse.class);
    MultiDomainFaultToleranceStatus responseData =
            (MultiDomainFaultToleranceStatus) getDomainFaultToleranceResponseResponseEntity.getBody().getData();
    assertEquals(getDomainFaultToleranceResponseResponseEntity.getStatusCode(), HttpStatus.OK);
    assertEquals(responseData.getMultiDomainFaultToleranceStatus().get(0).getType(), DomainType.CLUSTER);
  }

  @Test
  public void getDomainFaultToleranceStatusTestInCaseOfException() throws ClustermgmtServiceException {
    final ErrorCode errorCode = ErrorCode.CLUSTERMGMT_SERVICE_UNKNOWN_ENTITY;
    final String clusterUuid = TestConstantUtils.randomUUIDString();
    when(clusterService.getDomainFaultToleranceStatus(any())).
      thenThrow(new ClustermgmtNotFoundException("unkmnow cluster uuid: " + clusterUuid));

    ResponseEntity<GetFaultToleranceStatusByClusterIdApiResponse> getDomainFaultToleranceResponseResponseEntity =
      testRestTemplate.exchange(CLUSTER_URL + clusterUuid + "/fault-tolerance-status",
        HttpMethod.GET, null, GetFaultToleranceStatusByClusterIdApiResponse.class);
    GetFaultToleranceStatusByClusterIdApiResponse responseData =
      getDomainFaultToleranceResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
    final ErrorResponse errorResponse = (ErrorResponse) responseData.getData();
    List<AppMessage> messageList = (List<AppMessage>) errorResponse.getError();
    final AppMessage appMessage = messageList.get(0);
    assertEquals(appMessage.getCode(), "CLU-" + errorCode.getStandardCode());
  }

  @Test
  public void getHostTest() throws ClustermgmtServiceException {
    when(clusterService.getHostEntity(any(), any())).thenReturn(HostTestUtils.getHostEntityObj());
    String clusterUuid = TestConstantUtils.randomUUIDString();
    String hostUuid = TestConstantUtils.randomUUIDString();
    ResponseEntity<GetHostApiResponse> getHostApiResponse =
      testRestTemplate.exchange(CLUSTER_URL + clusterUuid +"/hosts/" +
          hostUuid,
        HttpMethod.GET, null, GetHostApiResponse.class);
    Host responseData = (Host)getHostApiResponse.getBody().getData();
    List<ApiLink> links = getHostApiResponse.getBody().getMetadata().getLinks();
    assertEquals(getHostApiResponse.getStatusCode(), HttpStatus.OK);
    assertEquals(responseData.getHostName(), HostTestUtils.HOST_NAME);
    assertEquals(links.size(), 6);
    assertEquals(links.get(0).getRel(), "self");
    assertEquals(links.get(0).getHref(), HATEOS_LINK_HREF_PREFIX + CLUSTER_URL + clusterUuid +"/hosts/" +
      hostUuid);
  }

  @Test
  public void getHostTestInCaseOfException() throws ClustermgmtServiceException {
    final ErrorCode errorCode = ErrorCode.CLUSTERMGMT_SERVICE_UNKNOWN_ENTITY;
    final String hostUuid = TestConstantUtils.randomUUIDString();
    when(clusterService.getHostEntity(any(), any())).
      thenThrow(new ClustermgmtNotFoundException("unkmnow host uuid: " + hostUuid));

    ResponseEntity<GetHostApiResponse> getHostApiResponse =
      testRestTemplate.exchange(CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/hosts/" + hostUuid,
        HttpMethod.GET, null, GetHostApiResponse.class);
    GetHostApiResponse responseData = getHostApiResponse.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
    final ErrorResponse errorResponse = (ErrorResponse) responseData.getData();
    List<AppMessage> messageList = (List<AppMessage>) errorResponse.getError();
    final AppMessage appMessage = messageList.get(0);
    assertEquals(appMessage.getCode(), "CLU-" + errorCode.getStandardCode());
  }

  @Test
  public void getHostsTest() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.getHostEntities(any(), any(), any(), any(), any(), any(), any())).thenReturn(HostTestUtils.getHostEntitiesObj());

    final List<String> queryStrings = Arrays.asList("$page=0", "$page=-1", "$limit=-1", "$limit=501", "$limit=500");
    for(String queryString: queryStrings) {
      ResponseEntity<ListHostsByClusterIdApiResponse> getHostsApiResponse =
        testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/hosts/?" + queryString,
          HttpMethod.GET, null, ListHostsByClusterIdApiResponse.class);
      List<Host> responseData = (List<Host>) getHostsApiResponse.getBody().getData();
      assertEquals(getHostsApiResponse.getStatusCode(), HttpStatus.OK);
      assertEquals(responseData.get(0).getHostName(), HostTestUtils.HOST_NAME);
    }
  }

  @Test
  public void getHostsTestInCaseOfException() throws ClustermgmtServiceException, ValidationException {
    final ErrorCode errorCode = ErrorCode.CLUSTERMGMT_ZKCONFIG_ERROR;
    when(clusterService.getHostEntities(any(), any(), any(),any(),any(), any(), any())).
      thenThrow(new ClustermgmtZkConfigReadException("Failed to read zeus config"));

    ResponseEntity<ListHostsByClusterIdApiResponse> getHostsApiResponse =
      testRestTemplate.exchange(CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/hosts",
        HttpMethod.GET, null, ListHostsByClusterIdApiResponse.class);
    ListHostsByClusterIdApiResponse responseData = getHostsApiResponse.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
    final ErrorResponse errorResponse = (ErrorResponse) responseData.getData();
    List<AppMessage> messageList = (List<AppMessage>) errorResponse.getError();
    final AppMessage appMessage = messageList.get(0);
    assertEquals(appMessage.getCode(), "CLU-" + errorCode.getStandardCode());
  }

  @Test
  public void getHostsTestInCaseOfValidationException() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.getHostEntities(any(), any(), any(), any(), any(), any(), any())).
      thenThrow(new ValidationException("Failure in parsing Odata request"));

    ResponseEntity<ListHostsByClusterIdApiResponse> getHostsApiResponse =
      testRestTemplate.exchange(CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/hosts",
        HttpMethod.GET, null, ListHostsByClusterIdApiResponse.class);
    ListHostsByClusterIdApiResponse responseData = getHostsApiResponse.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
    final ErrorResponse errorResponse = (ErrorResponse) responseData.getData();
    List<AppMessage> messageList = (List<AppMessage>) errorResponse.getError();
    final AppMessage appMessage = messageList.get(0);
    log.info("Error code is {}", appMessage.getCode());
    assertEquals(appMessage.getCode(), "CLU-" + "10006");
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void getHostGpusTest() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.getHostGpus(any(), any(), any(), any(), any(), any(), any())).thenReturn(HostTestUtils.getHostGpusObj());
    final List<String> queryStrings = Arrays.asList("$page=0", "$page=-1", "$limit=-1", "$limit=501", "$limit=500", "$select=nodeUuid");
    for(String queryString: queryStrings) {
      String url = CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/hosts/" + TestConstantUtils.randomUUIDString()
        + "/host-gpus?" + queryString;
      ResponseEntity<ListHostGpusByHostIdApiResponse> getHostGpusResponseResponseEntity =
        testRestTemplate.exchange(url,
          HttpMethod.GET, null, ListHostGpusByHostIdApiResponse.class);
      List<HostGpu> responseData = (List<HostGpu>) getHostGpusResponseResponseEntity.getBody().getData();
      assertEquals(getHostGpusResponseResponseEntity.getStatusCode(), HttpStatus.OK);
      assertEquals(responseData.get(0).getExtId(), HostTestUtils.HOST_GPU_UUID);
    }
  }

  @Test
  public void getHostGpusTestInCaseOfException() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.getHostGpus(any(), any(), any(), any(), any(),any(), any())).
      thenThrow(new ClustermgmtNotFoundException("unkmnow host gpu uuid"));
    String url = CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/hosts/" + TestConstantUtils.randomUUIDString() + "/host-gpus";
    ResponseEntity<ListHostGpusByHostIdApiResponse> getHostGpusResponseResponseEntity =
      testRestTemplate.exchange(url,
        HttpMethod.GET, null, ListHostGpusByHostIdApiResponse.class);
    ListHostGpusByHostIdApiResponse responseData = getHostGpusResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void getHostGpusTestInCaseOfValidationException() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.getHostGpus(any(), any(), any(), any(), any(), any(), any())).
      thenThrow(new ValidationException("Failure in parsing Odata request"));

    String url = CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/hosts/" + TestConstantUtils.randomUUIDString() + "/host-gpus";
    ResponseEntity<ListHostGpusByHostIdApiResponse> getHostGpusResponseResponseEntity =
      testRestTemplate.exchange(url,
        HttpMethod.GET, null, ListHostGpusByHostIdApiResponse.class);
    ListHostGpusByHostIdApiResponse responseData = getHostGpusResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void getClusterHostGpusTest() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.getClusterHostGpus(any(), any(), any(),any(), any(), any())).thenReturn(HostTestUtils.getClusterHostGpusObj());
    final List<String> queryStrings = Arrays.asList("$page=0", "$page=-1", "$limit=-1", "$limit=501", "$limit=500", "$select=nodeUuid");
    for(String queryString: queryStrings) {
      ResponseEntity<ListHostGpusByClusterIdApiResponse> getHostGpusResponseResponseEntity =
        testRestTemplate.exchange(CLUSTER_URL + TestConstantUtils.randomUUIDString()
            + "/host-gpus?" + queryString,
          HttpMethod.GET, null, ListHostGpusByClusterIdApiResponse.class);
      List<HostGpu> responseData = (List<HostGpu>) getHostGpusResponseResponseEntity.getBody().getData();
      assertEquals(getHostGpusResponseResponseEntity.getStatusCode(), HttpStatus.OK);
      assertEquals(responseData.get(0).getExtId(), HostTestUtils.HOST_GPU_UUID);
    }
  }

  @Test
  public void getClusterHostGpusTestInCaseOfException() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.getClusterHostGpus(any(), any(), any(), any(), any(), any())).
      thenThrow(new ClustermgmtNotFoundException("unkmnow host gpu uuid"));
    String url = CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/host-gpus";
    ResponseEntity<ListHostGpusByClusterIdApiResponse> getHostGpusResponseResponseEntity =
      testRestTemplate.exchange(url,
        HttpMethod.GET, null, ListHostGpusByClusterIdApiResponse.class);
    ListHostGpusByClusterIdApiResponse responseData = getHostGpusResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void getClusterHostGpusTestInCaseOfValidationException() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.getClusterHostGpus(any(), any(), any(), any(), any(), any())).
      thenThrow(new ValidationException("Failure in parsing Odata request"));

    String url = CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/host-gpus";
    ResponseEntity<ListHostGpusByClusterIdApiResponse> getHostGpusResponseResponseEntity =
      testRestTemplate.exchange(url,
        HttpMethod.GET, null, ListHostGpusByClusterIdApiResponse.class);
    ListHostGpusByClusterIdApiResponse responseData = getHostGpusResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void getVirtualGpuProfilesTest() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.listVirtualGpuProfiles(any(), any(), any(), any(), any())).thenReturn(HostTestUtils.getVirtualGpuProfileTest());
    final List<String> queryStrings = Arrays.asList("$page=0", "$page=-1", "$limit=-1", "$limit=501", "$limit=500", "$orderby=deviceId", "$filter=deviceId eq " + HostTestUtils.HOST_VIRTUAL_GPU_DEVICE_ID);
    for(String queryString: queryStrings) {
      ResponseEntity<ListVirtualGpuProfilesApiResponse> getVirtualGpuProfilesResponseResponseEntity =
      testRestTemplate.exchange(CLUSTER_URL + TestConstantUtils.randomUUIDString()
          + "/virtual-gpu-profiles?" + queryString,
        HttpMethod.GET, null, ListVirtualGpuProfilesApiResponse.class);
      List<VirtualGpuProfile> responseData =
        (List<VirtualGpuProfile>) getVirtualGpuProfilesResponseResponseEntity.getBody().getData();
      assertEquals(getVirtualGpuProfilesResponseResponseEntity.getStatusCode(), HttpStatus.OK);
      assertEquals(responseData.get(0).getVirtualGpuConfig().getDeviceId(), HostTestUtils.HOST_VIRTUAL_GPU_DEVICE_ID);
    }
  }

  @Test
  public void getVirtualGpuProfilesTestInCaseOfException() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.listVirtualGpuProfiles(any(), any(), any(), any(), any())).
      thenThrow(new ClustermgmtNotFoundException("some exception"));
    ResponseEntity<ListVirtualGpuProfilesApiResponse> getVirtualGpuProfilesResponseResponseEntity =
    testRestTemplate.exchange(CLUSTER_URL + TestConstantUtils.randomUUIDString()
        + "/virtual-gpu-profiles",
      HttpMethod.GET, null, ListVirtualGpuProfilesApiResponse.class);
    ListVirtualGpuProfilesApiResponse responseData = getVirtualGpuProfilesResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void getVirtualGpuProfilesTestInCaseOfValidationException() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.listVirtualGpuProfiles(any(), any(), any(), any(), any())).
      thenThrow(new ValidationException("some exception"));
    ResponseEntity<ListVirtualGpuProfilesApiResponse> getVirtualGpuProfilesResponseResponseEntity =
    testRestTemplate.exchange(CLUSTER_URL + TestConstantUtils.randomUUIDString()
        + "/virtual-gpu-profiles",
      HttpMethod.GET, null, ListVirtualGpuProfilesApiResponse.class);
    ListVirtualGpuProfilesApiResponse responseData = getVirtualGpuProfilesResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void getPhysicalGpuProfilesTest() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.listPhysicalGpuProfiles(any(), any(), any(), any(), any())).thenReturn(HostTestUtils.getPhysicalGpuProfileTest());
    final List<String> queryStrings = Arrays.asList("$page=0", "$page=-1", "$limit=-1", "$limit=501", "$limit=500", "$orderby=deviceId", "$filter=deviceId eq " + HostTestUtils.HOST_PHYSICAL_GPU_DEVICE_ID);
    for(String queryString: queryStrings) {
      ResponseEntity<ListPhysicalGpuProfilesApiResponse> getPhysicalGpuProfilesResponseResponseEntity =
        testRestTemplate.exchange(CLUSTER_URL + TestConstantUtils.randomUUIDString()
            + "/physical-gpu-profiles?" + queryString,
          HttpMethod.GET, null, ListPhysicalGpuProfilesApiResponse.class);
      List<PhysicalGpuProfile> responseData =
        (List<PhysicalGpuProfile>) getPhysicalGpuProfilesResponseResponseEntity.getBody().getData();
      assertEquals(getPhysicalGpuProfilesResponseResponseEntity.getStatusCode(), HttpStatus.OK);
      assertEquals(responseData.get(0).getPhysicalGpuConfig().getDeviceId(), HostTestUtils.HOST_PHYSICAL_GPU_DEVICE_ID);
    }
  }

  @Test
  public void getPhysicalGpuProfilesTestInCaseOfException() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.listPhysicalGpuProfiles(any(), any(), any(), any(), any())).
      thenThrow(new ClustermgmtNotFoundException("some exception"));
    ResponseEntity<ListPhysicalGpuProfilesApiResponse> getPhysicalGpuProfilesResponseResponseEntity =
      testRestTemplate.exchange(CLUSTER_URL + TestConstantUtils.randomUUIDString()
          + "/physical-gpu-profiles",
        HttpMethod.GET, null, ListPhysicalGpuProfilesApiResponse.class);
    ListPhysicalGpuProfilesApiResponse responseData = getPhysicalGpuProfilesResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void getPhysicalGpuProfilesTestInCaseOfValidationException() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.listPhysicalGpuProfiles(any(), any(), any(), any(), any())).
      thenThrow(new ValidationException("some exception"));
    ResponseEntity<ListPhysicalGpuProfilesApiResponse> getPhysicalGpuProfilesResponseResponseEntity =
      testRestTemplate.exchange(CLUSTER_URL + TestConstantUtils.randomUUIDString()
          + "/physical-gpu-profiles",
        HttpMethod.GET, null, ListPhysicalGpuProfilesApiResponse.class);
    ListPhysicalGpuProfilesApiResponse responseData = getPhysicalGpuProfilesResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void getHostGpuTest() throws ClustermgmtServiceException {
    when(clusterService.getHostGpu(any(), any(), any())).thenReturn(HostTestUtils.getHostGpuObj());
    String clusterUuid = TestConstantUtils.randomUUIDString();
    String hostUuid = TestConstantUtils.randomUUIDString();
    String gpuUuid = TestConstantUtils.randomUUIDString();
    ResponseEntity<GetHostGpuApiResponse> getHostGpuResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + clusterUuid + "/hosts/"
          + hostUuid + "/host-gpus/" + gpuUuid,
        HttpMethod.GET, null, GetHostGpuApiResponse.class);
    HostGpu responseData = (HostGpu) getHostGpuResponseResponseEntity.getBody().getData();
    List<ApiLink> links = getHostGpuResponseResponseEntity.getBody().getMetadata().getLinks();
    assertEquals(getHostGpuResponseResponseEntity.getStatusCode(), HttpStatus.OK);
    assertEquals(responseData.getExtId(), HostTestUtils.HOST_GPU_UUID);
    assertEquals(links.size(), 1);
    assertEquals(links.get(0).getRel(), "self");
    assertEquals(links.get(0).getHref(), HATEOS_LINK_HREF_PREFIX + CLUSTER_URL + clusterUuid + "/hosts/"
      + hostUuid + "/host-gpus/" + gpuUuid);
  }

  @Test
  public void updateClusterTest() throws ClustermgmtServiceException, ValidationException {
    Cluster clusterEntity = ClusterTestUtils.getClusterEntityObj();
    when(clusterService.getClusterEntity(any(), any())).thenReturn(clusterEntity);
    String etag = ClustermgmtUtils.calculateEtag(clusterEntity);
    HttpHeaders headers = new HttpHeaders();
    headers.add(IF_MATCH, etag);
    HttpEntity<Cluster> clusterEntityHttpEntity = new HttpEntity<>(
      clusterEntity, headers);

    when(clusterService.updateCluster(any(), any(), any())).thenReturn(ClusterTestUtils.getTaskWithColonUuid());
    ResponseEntity<UpdateClusterApiResponse> updateClusterTaskApiResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString(),
        HttpMethod.PUT, clusterEntityHttpEntity, UpdateClusterApiResponse.class);
    TaskReference responseData = (TaskReference) updateClusterTaskApiResponseResponseEntity.getBody().getData();
    assertEquals(updateClusterTaskApiResponseResponseEntity.getStatusCode(), HttpStatus.ACCEPTED);
    assertEquals(responseData.getExtId(), ClusterTestUtils.TASK_COLON_UUID);
    List<ApiLink> links = updateClusterTaskApiResponseResponseEntity.getBody().getMetadata().getLinks();
    assertEquals(links.get(0).getHref(), HATEOS_TASK_LINK);
  }

  @Test
  public void updateClusterTestInCaseOfCasException() throws ClustermgmtServiceException, ValidationException {
    Cluster clusterEntity = ClusterTestUtils.getClusterEntityObj();
    when(clusterService.getClusterEntity(any(), any())).thenReturn(clusterEntity);
    HttpHeaders headers = new HttpHeaders();
    headers.add(IF_MATCH, "random");
    HttpEntity<Cluster> clusterEntityHttpEntity = new HttpEntity<>(
      clusterEntity, headers);

    when(clusterService.updateCluster(any(), any(), any())).thenReturn(ClusterTestUtils.getTaskWithColonUuid());
    ResponseEntity<UpdateClusterApiResponse> updateClusterTaskApiResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString(),
        HttpMethod.PUT, clusterEntityHttpEntity, UpdateClusterApiResponse.class);
    UpdateClusterApiResponse responseData = (UpdateClusterApiResponse) updateClusterTaskApiResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void updateClusterTestInCaseOfException() throws ClustermgmtServiceException, ValidationException {
    Cluster clusterEntity = ClusterTestUtils.getClusterEntityObj();
    when(clusterService.getClusterEntity(any(), any())).thenReturn(clusterEntity);
    String etag = ClustermgmtUtils.calculateEtag(clusterEntity);
    HttpHeaders headers = new HttpHeaders();
    headers.add(IF_MATCH, etag);
    HttpEntity<Cluster> clusterEntityHttpEntity = new HttpEntity<>(
      clusterEntity, headers);

    when(clusterService.updateCluster(any(), any(), any())).thenThrow(
      new ClustermgmtNotFoundException("Unknown cluster Uuid"));
    ResponseEntity<UpdateClusterApiResponse> updateClusterTaskApiResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString(),
        HttpMethod.PUT, clusterEntityHttpEntity, UpdateClusterApiResponse.class);
    UpdateClusterApiResponse responseData = (UpdateClusterApiResponse) updateClusterTaskApiResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void updateClusterTestInCaseOfEmptyEtag() throws ClustermgmtServiceException, ValidationException {
    Cluster clusterEntity = ClusterTestUtils.getClusterEntityObj();
    when(clusterService.getClusterEntity(any(), any())).thenReturn(clusterEntity);
    HttpHeaders headers = new HttpHeaders();
    headers.add(IF_MATCH, "");
    HttpEntity<Cluster> clusterEntityHttpEntity = new HttpEntity<>(
      clusterEntity, headers);
    when(clusterService.updateCluster(any(), any(), any())).thenReturn(ClusterTestUtils.getTaskWithColonUuid());
    ResponseEntity<UpdateClusterApiResponse> updateClusterTaskApiResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString(),
        HttpMethod.PUT, clusterEntityHttpEntity, UpdateClusterApiResponse.class);
    UpdateClusterApiResponse responseData = (UpdateClusterApiResponse) updateClusterTaskApiResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
    assertEquals(updateClusterTaskApiResponseResponseEntity.getStatusCodeValue(), 412);
  }

  @Test
  public void updateSnmpStatusTest() throws ClustermgmtServiceException {
    HttpEntity<SnmpStatusParam> snmpStatusParamHttpEntity = new HttpEntity<>(
      ClusterTestUtils.getSnmpStatusParam(), httpHeaders);
    when(clusterService.updateSnmpStatus(any(), any(), any())).thenReturn(ClusterTestUtils.getTaskWithColonUuid());
    ResponseEntity<UpdateSnmpStatusApiResponse> updateSnmpStatusTaskApiResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/snmp/$actions/update-status",
        HttpMethod.POST, snmpStatusParamHttpEntity, UpdateSnmpStatusApiResponse.class);
    TaskReference responseData = (TaskReference) updateSnmpStatusTaskApiResponseResponseEntity.getBody().getData();
    assertEquals(updateSnmpStatusTaskApiResponseResponseEntity.getStatusCode(), HttpStatus.ACCEPTED);
    assertEquals(responseData.getExtId(), ClusterTestUtils.TASK_COLON_UUID);
    List<ApiLink> links = updateSnmpStatusTaskApiResponseResponseEntity.getBody().getMetadata().getLinks();
    assertEquals(links.get(0).getHref(), HATEOS_TASK_LINK);
  }

  @Test
  public void updateSnmpStatusTestInCaseOfException() throws ClustermgmtServiceException {
    HttpEntity<SnmpStatusParam> snmpStatusParamHttpEntity = new HttpEntity<>(
      ClusterTestUtils.getSnmpStatusParam(), httpHeaders);
    when(clusterService.updateSnmpStatus(any(), any(), any())).thenThrow(
      new ClustermgmtNotFoundException("Unknown cluster Uuid")
    );
    ResponseEntity<UpdateSnmpStatusApiResponse> updateSnmpStatusTaskApiResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/snmp/$actions/update-status",
        HttpMethod.POST, snmpStatusParamHttpEntity, UpdateSnmpStatusApiResponse.class);
    UpdateSnmpStatusApiResponse responseData = (UpdateSnmpStatusApiResponse) updateSnmpStatusTaskApiResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void addSnmpTransportTest() throws ClustermgmtServiceException, ValidationException {
    SnmpTransport snmpTransport = new SnmpTransport();
    HttpEntity<SnmpTransport> snmpTransportHttpEntity = new HttpEntity<>(
      snmpTransport, httpHeaders);
    when(clusterService.addSnmpTransport(any(), any(), any())).thenReturn(ClusterTestUtils.getTaskWithColonUuid());
    ResponseEntity<AddSnmpTransportsApiResponse> addSnmpTransportsTaskApiResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/snmp/$actions/add-transports",
        HttpMethod.POST, snmpTransportHttpEntity, AddSnmpTransportsApiResponse.class);
    TaskReference responseData = (TaskReference) addSnmpTransportsTaskApiResponseResponseEntity.getBody().getData();
    assertEquals(addSnmpTransportsTaskApiResponseResponseEntity.getStatusCode(), HttpStatus.ACCEPTED);
    assertEquals(responseData.getExtId(), ClusterTestUtils.TASK_COLON_UUID);
    List<ApiLink> links = addSnmpTransportsTaskApiResponseResponseEntity.getBody().getMetadata().getLinks();
    assertEquals(links.get(0).getHref(), HATEOS_TASK_LINK);
  }

  @Test
  public void addSnmpTransportTestInCaseOfException() throws ClustermgmtServiceException, ValidationException {
    SnmpTransport snmpTransport = new SnmpTransport();
    HttpEntity<SnmpTransport> snmpTransportHttpEntity = new HttpEntity<>(
      snmpTransport, httpHeaders);

    when(clusterService.addSnmpTransport(any(), any(), any())).thenThrow(
      new ClustermgmtNotFoundException("Unknown cluster Uuid")
    );
    ResponseEntity<AddSnmpTransportsApiResponse> addSnmpTransportsTaskApiResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/snmp/$actions/add-transports",
        HttpMethod.POST, snmpTransportHttpEntity, AddSnmpTransportsApiResponse.class);
    AddSnmpTransportsApiResponse responseData = (AddSnmpTransportsApiResponse) addSnmpTransportsTaskApiResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void removeSnmpTransportTest() throws ClustermgmtServiceException, ValidationException {
    SnmpTransport snmpTransport = new SnmpTransport();
    HttpEntity<SnmpTransport> snmpTransportHttpEntity = new HttpEntity<>(
      snmpTransport, httpHeaders);

    when(clusterService.removeSnmpTransport(any(), any(), any())).thenReturn(ClusterTestUtils.getTaskWithColonUuid());
    ResponseEntity<RemoveSnmpTransportsApiResponse> removeSnmpTransportsTaskApiResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/snmp/$actions/remove-transports",
        HttpMethod.POST, snmpTransportHttpEntity, RemoveSnmpTransportsApiResponse.class);
    TaskReference responseData = (TaskReference) removeSnmpTransportsTaskApiResponseResponseEntity.getBody().getData();
    assertEquals(removeSnmpTransportsTaskApiResponseResponseEntity.getStatusCode(), HttpStatus.ACCEPTED);
    assertEquals(responseData.getExtId(), ClusterTestUtils.TASK_COLON_UUID);
    List<ApiLink> links = removeSnmpTransportsTaskApiResponseResponseEntity.getBody().getMetadata().getLinks();
    assertEquals(links.get(0).getHref(), HATEOS_TASK_LINK);
  }

  @Test
  public void removeSnmpTransportTestInCaseOfException() throws ClustermgmtServiceException, ValidationException {
    SnmpTransport snmpTransport = new SnmpTransport();
    HttpEntity<SnmpTransport> snmpTransportHttpEntity = new HttpEntity<>(
      snmpTransport, httpHeaders);

    when(clusterService.addSnmpTransport(any(), any(), any())).thenThrow(
      new ClustermgmtNotFoundException("Unknown cluster Uuid"
    ));
    ResponseEntity<RemoveSnmpTransportsApiResponse> removeSnmpTransportsTaskApiResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/snmp/$actions/add-transports",
        HttpMethod.POST, snmpTransportHttpEntity, RemoveSnmpTransportsApiResponse.class);
    RemoveSnmpTransportsApiResponse responseData = (RemoveSnmpTransportsApiResponse) removeSnmpTransportsTaskApiResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void addSnmpUserTest() throws ClustermgmtServiceException, ValidationException {
    SnmpUser snmpUser = new SnmpUser();
    HttpEntity<SnmpUser> snmpUserHttpEntity = new HttpEntity<>(
      snmpUser, httpHeaders);
    when(clusterService.addSnmpUser(any(), any(), any())).thenReturn(ClusterTestUtils.getTaskWithColonUuid());
    ResponseEntity<CreateSnmpUserApiResponse> addSnmpUserTaskApiResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/snmp/users",
        HttpMethod.POST, snmpUserHttpEntity, CreateSnmpUserApiResponse.class);
    TaskReference responseData = (TaskReference) addSnmpUserTaskApiResponseResponseEntity.getBody().getData();
    assertEquals(addSnmpUserTaskApiResponseResponseEntity.getStatusCode(), HttpStatus.ACCEPTED);
    assertEquals(responseData.getExtId(), ClusterTestUtils.TASK_COLON_UUID);
    List<ApiLink> links = addSnmpUserTaskApiResponseResponseEntity.getBody().getMetadata().getLinks();
    assertEquals(links.get(0).getHref(), HATEOS_TASK_LINK);
  }

  @Test
  public void addSnmpUserTestInCaseOfException() throws ClustermgmtServiceException, ValidationException {
    SnmpUser snmpUser = new SnmpUser();
    HttpEntity<SnmpUser> snmpUserHttpEntity = new HttpEntity<>(
      snmpUser, httpHeaders);

    when(clusterService.addSnmpUser(any(), any(), any())).thenThrow(
      new ClustermgmtNotFoundException("Unknown cluster Uuid"));
    ResponseEntity<CreateSnmpUserApiResponse> addSnmpUserTaskApiResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/snmp/users",
        HttpMethod.POST, snmpUserHttpEntity, CreateSnmpUserApiResponse.class);
    CreateSnmpUserApiResponse responseData = (CreateSnmpUserApiResponse) addSnmpUserTaskApiResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void updateSnmpUserTest() throws ClustermgmtServiceException, ValidationException { ;
    SnmpUser snmpUser = ClusterTestUtils.getSnmpUser();
    when(clusterService.getSnmpUser(any(), any())).thenReturn(snmpUser);
    String etag = ClustermgmtUtils.calculateEtag(snmpUser);
    HttpHeaders headers = new HttpHeaders();
    headers.add(IF_MATCH, etag);
    HttpEntity<SnmpUser> snmpUserHttpEntity = new HttpEntity<>(
      snmpUser, headers);
    when(clusterService.updateSnmpUser(any(), any(), any(), any())).thenReturn(ClusterTestUtils.getTaskWithColonUuid());
    ResponseEntity<UpdateSnmpUserApiResponse> updateSnmpUserTaskApiResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/snmp/users/" +
          ClusterTestUtils.SNMP_USER_UUID,
        HttpMethod.PUT, snmpUserHttpEntity, UpdateSnmpUserApiResponse.class);
    TaskReference responseData = (TaskReference) updateSnmpUserTaskApiResponseResponseEntity.getBody().getData();
    assertEquals(updateSnmpUserTaskApiResponseResponseEntity.getStatusCode(), HttpStatus.ACCEPTED);
    assertEquals(responseData.getExtId(), ClusterTestUtils.TASK_COLON_UUID);
    List<ApiLink> links = updateSnmpUserTaskApiResponseResponseEntity.getBody().getMetadata().getLinks();
    assertEquals(links.get(0).getHref(), HATEOS_TASK_LINK);
  }

  @Test
  public void updateSnmpUserTestInCaseOfException() throws ClustermgmtServiceException, ValidationException {
    SnmpUser snmpUser = ClusterTestUtils.getSnmpUser();
    when(clusterService.getSnmpUser(any(), any())).thenReturn(snmpUser);
    HttpHeaders headers = new HttpHeaders();
    String etag = ClustermgmtUtils.calculateEtag(snmpUser);
    headers.add(IF_MATCH, etag);
    HttpEntity<SnmpUser> snmpUserHttpEntity = new HttpEntity<>(
      snmpUser, headers);

    when(clusterService.updateSnmpUser(any(), any(), any(), any())).thenThrow(
      new ClustermgmtNotFoundException("Unknown cluster Uuid"));
    ResponseEntity<UpdateSnmpUserApiResponse> updateSnmpUserTaskApiResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/snmp/users/" +
          ClusterTestUtils.SNMP_USER_UUID,
        HttpMethod.PUT, snmpUserHttpEntity, UpdateSnmpUserApiResponse.class);
    UpdateSnmpUserApiResponse responseData = (UpdateSnmpUserApiResponse) updateSnmpUserTaskApiResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void updateSnmpUserTestInCaseOfCasException() throws ClustermgmtServiceException, ValidationException {
    SnmpUser snmpUser = ClusterTestUtils.getSnmpUser();
    when(clusterService.getSnmpUser(any(), any())).thenReturn(snmpUser);
    HttpHeaders headers = new HttpHeaders();
    headers.add(IF_MATCH, "random");
    HttpEntity<SnmpUser> snmpUserHttpEntity = new HttpEntity<>(
      snmpUser, headers);
    when(clusterService.updateSnmpUser(any(), any(), any(), any())).thenReturn(ClusterTestUtils.getTaskWithColonUuid());
    ResponseEntity<UpdateSnmpUserApiResponse> updateSnmpUserTaskApiResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/snmp/users/" +
          ClusterTestUtils.SNMP_USER_UUID,
        HttpMethod.PUT, snmpUserHttpEntity, UpdateSnmpUserApiResponse.class);
    UpdateSnmpUserApiResponse responseData = (UpdateSnmpUserApiResponse) updateSnmpUserTaskApiResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void deleteSnmpUserTest() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.deleteSnmpUser(any(), any(), any())).thenReturn(ClusterTestUtils.getTaskWithColonUuid());
    ResponseEntity<DeleteSnmpUserApiResponse> deleteSnmpUserTaskApiResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/snmp/users/" +
          ClusterTestUtils.SNMP_USER_UUID,
        HttpMethod.DELETE, null, DeleteSnmpUserApiResponse.class);
    TaskReference responseData = (TaskReference) deleteSnmpUserTaskApiResponseResponseEntity.getBody().getData();
    assertEquals(deleteSnmpUserTaskApiResponseResponseEntity.getStatusCode(), HttpStatus.ACCEPTED);
    assertEquals(responseData.getExtId(), ClusterTestUtils.TASK_COLON_UUID);
    List<ApiLink> links = deleteSnmpUserTaskApiResponseResponseEntity.getBody().getMetadata().getLinks();
    assertEquals(links.get(0).getHref(), HATEOS_TASK_LINK);
  }

  @Test
  public void deleteSnmpUserTestInCaseOfException() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.deleteSnmpUser(any(), any(), any())).thenThrow(
      new ClustermgmtNotFoundException("Unknown cluster Uuid"));
    ResponseEntity<DeleteSnmpUserApiResponse> deleteSnmpUserTaskApiResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/snmp/users/" +
        ClusterTestUtils.SNMP_USER_UUID,
        HttpMethod.DELETE, null, DeleteSnmpUserApiResponse.class);
    DeleteSnmpUserApiResponse responseData = (DeleteSnmpUserApiResponse) deleteSnmpUserTaskApiResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void addSnmpTrapTest() throws ClustermgmtServiceException, ValidationException {
    SnmpTrap snmpTrap = new SnmpTrap();
    HttpEntity<SnmpTrap> snmpUserHttpEntity = new HttpEntity<>(
      snmpTrap, httpHeaders);
    when(clusterService.addSnmpTrap(any(), any(), any())).thenReturn(ClusterTestUtils.getTaskWithColonUuid());
    ResponseEntity<CreateSnmpTrapApiResponse> addSnmpTrapTaskApiResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/snmp/traps",
        HttpMethod.POST, snmpUserHttpEntity, CreateSnmpTrapApiResponse.class);
    TaskReference responseData = (TaskReference) addSnmpTrapTaskApiResponseResponseEntity.getBody().getData();
    assertEquals(addSnmpTrapTaskApiResponseResponseEntity.getStatusCode(), HttpStatus.ACCEPTED);
    assertEquals(responseData.getExtId(), ClusterTestUtils.TASK_COLON_UUID);
    List<ApiLink> links = addSnmpTrapTaskApiResponseResponseEntity.getBody().getMetadata().getLinks();
    assertEquals(links.get(0).getHref(), HATEOS_TASK_LINK);
  }

  @Test
  public void addSnmpTrapTestInCaseOfException() throws ClustermgmtServiceException, ValidationException {
    SnmpTrap snmpTrap = new SnmpTrap();
    HttpEntity<SnmpTrap> snmpUserHttpEntity = new HttpEntity<>(
      snmpTrap, httpHeaders);

    when(clusterService.addSnmpTrap(any(), any(), any())).thenThrow(
      new ClustermgmtNotFoundException("Unknown cluster Uuid"));
    ResponseEntity<CreateSnmpTrapApiResponse> addSnmpTrapTaskApiResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/snmp/traps",
        HttpMethod.POST, snmpUserHttpEntity, CreateSnmpTrapApiResponse.class);
    CreateSnmpTrapApiResponse responseData = (CreateSnmpTrapApiResponse) addSnmpTrapTaskApiResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void updateSnmpTrapTest() throws ClustermgmtServiceException, ValidationException {
    SnmpTrap snmpTrap = ClusterTestUtils.getSnmpTrap();
    when(clusterService.getSnmpTrap(any(), any())).thenReturn(snmpTrap);
    String etag = ClustermgmtUtils.calculateEtag(snmpTrap);
    HttpHeaders headers = new HttpHeaders();
    headers.add(IF_MATCH, etag);
    HttpEntity<SnmpTrap> snmpUserHttpEntity = new HttpEntity<>(
      snmpTrap, headers);
    when(clusterService.updateSnmpTrap(any(), any(), any(), any())).thenReturn(ClusterTestUtils.getTaskWithColonUuid());
    ResponseEntity<UpdateSnmpTrapApiResponse> updateSnmpTrapTaskApiResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/snmp/traps/" +
          ClusterTestUtils.SNMP_TRAP_UUID,
        HttpMethod.PUT, snmpUserHttpEntity, UpdateSnmpTrapApiResponse.class);
    TaskReference responseData = (TaskReference) updateSnmpTrapTaskApiResponseResponseEntity.getBody().getData();
    assertEquals(updateSnmpTrapTaskApiResponseResponseEntity.getStatusCode(), HttpStatus.ACCEPTED);
    assertEquals(responseData.getExtId(), ClusterTestUtils.TASK_COLON_UUID);
    List<ApiLink> links = updateSnmpTrapTaskApiResponseResponseEntity.getBody().getMetadata().getLinks();
    assertEquals(links.get(0).getHref(), HATEOS_TASK_LINK);
  }

  @Test
  public void updateSnmpTrapTestInCaseOfException() throws ClustermgmtServiceException, ValidationException {
    SnmpTrap snmpTrap = ClusterTestUtils.getSnmpTrap();
    when(clusterService.getSnmpTrap(any(), any())).thenReturn(snmpTrap);
    String etag = ClustermgmtUtils.calculateEtag(snmpTrap);
    HttpHeaders headers = new HttpHeaders();
    headers.add(IF_MATCH, etag);
    HttpEntity<SnmpTrap> snmpUserHttpEntity = new HttpEntity<>(
      snmpTrap, headers);

    when(clusterService.updateSnmpTrap(any(), any(), any(), any())).thenThrow(
      new ClustermgmtNotFoundException("Unknown cluster Uuid"));
    ResponseEntity<UpdateSnmpTrapApiResponse> updateSnmpTrapTaskApiResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/snmp/traps/" +
          ClusterTestUtils.SNMP_TRAP_UUID,
        HttpMethod.PUT, snmpUserHttpEntity, UpdateSnmpTrapApiResponse.class);
    UpdateSnmpTrapApiResponse responseData = (UpdateSnmpTrapApiResponse) updateSnmpTrapTaskApiResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void updateSnmpTrapTestInCaseOfCasException() throws ClustermgmtServiceException, ValidationException {
    SnmpTrap snmpTrap = ClusterTestUtils.getSnmpTrap();
    when(clusterService.getSnmpTrap(any(), any())).thenReturn(snmpTrap);
    HttpHeaders headers = new HttpHeaders();
    headers.add(IF_MATCH, "random");
    HttpEntity<SnmpTrap> snmpUserHttpEntity = new HttpEntity<>(
      snmpTrap, headers);
    when(clusterService.updateSnmpTrap(any(), any(), any(), any())).thenReturn(ClusterTestUtils.getTaskWithColonUuid());
    ResponseEntity<UpdateSnmpTrapApiResponse> updateSnmpTrapTaskApiResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/snmp/traps/" +
          ClusterTestUtils.SNMP_TRAP_UUID,
        HttpMethod.PUT, snmpUserHttpEntity, UpdateSnmpTrapApiResponse.class);
    UpdateSnmpTrapApiResponse responseData = (UpdateSnmpTrapApiResponse) updateSnmpTrapTaskApiResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void deleteSnmpTrapTest() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.deleteSnmpTrap(any(), any(), any())).thenReturn(ClusterTestUtils.getTaskWithColonUuid());
    ResponseEntity<DeleteSnmpTrapApiResponse> deleteSnmpTrapTaskApiResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/snmp/traps/" +
          ClusterTestUtils.SNMP_TRAP_UUID,
        HttpMethod.DELETE, null, DeleteSnmpTrapApiResponse.class);
    TaskReference responseData = (TaskReference) deleteSnmpTrapTaskApiResponseResponseEntity.getBody().getData();
    assertEquals(deleteSnmpTrapTaskApiResponseResponseEntity.getStatusCode(), HttpStatus.ACCEPTED);
    assertEquals(responseData.getExtId(), ClusterTestUtils.TASK_COLON_UUID);
    List<ApiLink> links = deleteSnmpTrapTaskApiResponseResponseEntity.getBody().getMetadata().getLinks();
    assertEquals(links.get(0).getHref(), HATEOS_TASK_LINK);
  }

  @Test
  public void deleteSnmpTrapTestInCaseOfException() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.deleteSnmpTrap(any(), any(), any())).thenThrow(
      new ClustermgmtNotFoundException("Unknown cluster Uuid"));
    ResponseEntity<DeleteSnmpTrapApiResponse> deleteSnmpTrapTaskApiResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/snmp/traps/"
          + ClusterTestUtils.SNMP_TRAP_UUID,
        HttpMethod.DELETE, null, DeleteSnmpTrapApiResponse.class);
    DeleteSnmpTrapApiResponse responseData = (DeleteSnmpTrapApiResponse) deleteSnmpTrapTaskApiResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void addRsyslogServerTest() throws ClustermgmtServiceException, ValidationException {
    RsyslogServer rsyslogServer = new RsyslogServer();
    HttpEntity<RsyslogServer> rsyslogServerHttpEntity = new HttpEntity<>(
      rsyslogServer, httpHeaders);
    when(clusterService.addRsyslogServer(any(), any(), any())).thenReturn(ClusterTestUtils.getTaskWithColonUuid());
    ResponseEntity<CreateRsyslogServerApiResponse> addRsyslogServerTaskApiResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/rsyslog-servers",
        HttpMethod.POST, rsyslogServerHttpEntity, CreateRsyslogServerApiResponse.class);
    TaskReference responseData = (TaskReference) addRsyslogServerTaskApiResponseResponseEntity.getBody().getData();
    assertEquals(addRsyslogServerTaskApiResponseResponseEntity.getStatusCode(), HttpStatus.ACCEPTED);
    assertEquals(responseData.getExtId(), ClusterTestUtils.TASK_COLON_UUID);
    List<ApiLink> links = addRsyslogServerTaskApiResponseResponseEntity.getBody().getMetadata().getLinks();
    assertEquals(links.get(0).getHref(), HATEOS_TASK_LINK);
  }

  @Test
  public void addRsyslogServerTestInCaseOfException() throws ClustermgmtServiceException, ValidationException {
    RsyslogServer rsyslogServer = new RsyslogServer();
    HttpEntity<RsyslogServer> rsyslogServerHttpEntity = new HttpEntity<>(
      rsyslogServer, httpHeaders);

    when(clusterService.addRsyslogServer(any(), any(), any())).thenThrow(
      new ClustermgmtNotFoundException("Unknown cluster Uuid"));
    ResponseEntity<CreateRsyslogServerApiResponse> addRsyslogServerTaskApiResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/rsyslog-servers",
        HttpMethod.POST, rsyslogServerHttpEntity, CreateRsyslogServerApiResponse.class);
    CreateRsyslogServerApiResponse responseData = (CreateRsyslogServerApiResponse) addRsyslogServerTaskApiResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void updateRsyslogServerTest() throws ClustermgmtServiceException, ValidationException {
    RsyslogServer rsyslogServer = ClusterTestUtils.getRsyslogServer();
    when(clusterService.getRsyslogServer(any(), any())).thenReturn(rsyslogServer);
    String etag = ClustermgmtUtils.calculateEtag(rsyslogServer);
    HttpHeaders headers = new HttpHeaders();
    headers.add(IF_MATCH, etag);
    HttpEntity<RsyslogServer> rsyslogServerHttpEntity = new HttpEntity<>(
      rsyslogServer, headers);

    when(clusterService.updateRsyslogServer(any(), any(), any(), any())).thenReturn(ClusterTestUtils.getTaskWithColonUuid());
    ResponseEntity<UpdateRsyslogServerApiResponse> updateRsyslogServerTaskApiResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/rsyslog-servers/" +
          ClusterTestUtils.RSYSLOG_SERVER_UUID,
        HttpMethod.PUT, rsyslogServerHttpEntity, UpdateRsyslogServerApiResponse.class);
    TaskReference responseData = (TaskReference) updateRsyslogServerTaskApiResponseResponseEntity.getBody().getData();
    assertEquals(updateRsyslogServerTaskApiResponseResponseEntity.getStatusCode(), HttpStatus.ACCEPTED);
    assertEquals(responseData.getExtId(), ClusterTestUtils.TASK_COLON_UUID);
    List<ApiLink> links = updateRsyslogServerTaskApiResponseResponseEntity.getBody().getMetadata().getLinks();
    assertEquals(links.get(0).getHref(), HATEOS_TASK_LINK);
  }

  @Test
  public void updateRsyslogServerTestInCaseOfException() throws ClustermgmtServiceException, ValidationException {
    RsyslogServer rsyslogServer = ClusterTestUtils.getRsyslogServer();
    when(clusterService.getRsyslogServer(any(), any())).thenReturn(rsyslogServer);
    String etag = ClustermgmtUtils.calculateEtag(rsyslogServer);
    HttpHeaders headers = new HttpHeaders();
    headers.add(IF_MATCH, etag);
    HttpEntity<RsyslogServer> rsyslogServerHttpEntity = new HttpEntity<>(
      rsyslogServer, headers);

    when(clusterService.updateRsyslogServer(any(), any(), any(), any())).thenThrow(
      new ClustermgmtNotFoundException("Unknown cluster Uuid"));
    ResponseEntity<UpdateRsyslogServerApiResponse> updateRsyslogServerTaskApiResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/rsyslog-servers/" +
          ClusterTestUtils.RSYSLOG_SERVER_UUID,
        HttpMethod.PUT, rsyslogServerHttpEntity, UpdateRsyslogServerApiResponse.class);
    UpdateRsyslogServerApiResponse responseData = (UpdateRsyslogServerApiResponse) updateRsyslogServerTaskApiResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void updateRsyslogServerTestInCaseOfCasException() throws ClustermgmtServiceException, ValidationException {
    RsyslogServer rsyslogServer = ClusterTestUtils.getRsyslogServer();
    when(clusterService.getRsyslogServer(any(), any())).thenReturn(rsyslogServer);
    HttpHeaders headers = new HttpHeaders();
    headers.add(IF_MATCH, "random");
    HttpEntity<RsyslogServer> rsyslogServerHttpEntity = new HttpEntity<>(
      rsyslogServer, headers);

    when(clusterService.updateRsyslogServer(any(), any(), any(), any())).thenReturn(ClusterTestUtils.getTaskWithColonUuid());
    ResponseEntity<UpdateRsyslogServerApiResponse> updateRsyslogServerTaskApiResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/rsyslog-servers/" +
          ClusterTestUtils.RSYSLOG_SERVER_UUID,
        HttpMethod.PUT, rsyslogServerHttpEntity, UpdateRsyslogServerApiResponse.class);
    UpdateRsyslogServerApiResponse responseData = (UpdateRsyslogServerApiResponse) updateRsyslogServerTaskApiResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void deleteRsyslogServerTest() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.deleteRsyslogServer(any(), any(), any())).thenReturn(ClusterTestUtils.getTaskWithColonUuid());
    ResponseEntity<DeleteRsyslogServerApiResponse> deleteRsyslogServerTaskApiResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/rsyslog-servers/" +
          ClusterTestUtils.RSYSLOG_SERVER_UUID,
        HttpMethod.DELETE, null, DeleteRsyslogServerApiResponse.class);
    TaskReference responseData = (TaskReference) deleteRsyslogServerTaskApiResponseResponseEntity.getBody().getData();
    assertEquals(deleteRsyslogServerTaskApiResponseResponseEntity.getStatusCode(), HttpStatus.ACCEPTED);
    assertEquals(responseData.getExtId(), ClusterTestUtils.TASK_COLON_UUID);
    List<ApiLink> links = deleteRsyslogServerTaskApiResponseResponseEntity.getBody().getMetadata().getLinks();
    assertEquals(links.get(0).getHref(), HATEOS_TASK_LINK);
  }

  @Test
  public void deleteRsyslogServerTestInCaseOfException() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.deleteRsyslogServer(any(), any(), any())).thenThrow(
      new ClustermgmtNotFoundException("Unknown cluster Uuid"));
    ResponseEntity<DeleteRsyslogServerApiResponse> deleteRsyslogServerTaskApiResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/rsyslog-servers/" +
          ClusterTestUtils.RSYSLOG_SERVER_UUID,
        HttpMethod.DELETE, null, DeleteRsyslogServerApiResponse.class);
    DeleteRsyslogServerApiResponse responseData = (DeleteRsyslogServerApiResponse) deleteRsyslogServerTaskApiResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void renameHostTest() throws ClustermgmtServiceException, ValidationException {
    HttpEntity<HostNameParam> hostNameParamHttpEntity = new HttpEntity<>(
      ClusterTestUtils.getHostNameParam("new-host-name"), httpHeaders);
    when(clusterService.renameHost(any(), any(), any())).thenReturn(ClusterTestUtils.getTaskWithColonUuid());
    ResponseEntity<RenameHostApiResponse> hostRenameResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/hosts/"
          + TestConstantUtils.randomUUIDString() + "/$actions/rename-host",
        HttpMethod.POST, hostNameParamHttpEntity, RenameHostApiResponse.class);
    TaskReference responseData = (TaskReference) hostRenameResponseResponseEntity.getBody().getData();
    assertEquals(hostRenameResponseResponseEntity.getStatusCode(), HttpStatus.ACCEPTED);
    assertEquals(responseData.getExtId(), ClusterTestUtils.TASK_COLON_UUID);
    List<ApiLink> links = hostRenameResponseResponseEntity.getBody().getMetadata().getLinks();
    assertEquals(links.get(0).getHref(), HATEOS_TASK_LINK);
  }

  @Test
  public void renameHostTestInCaseOfException() throws ClustermgmtServiceException, ValidationException {
    HttpEntity<HostNameParam> hostNameParamHttpEntity = new HttpEntity<>(
      ClusterTestUtils.getHostNameParam("new-host-name"), httpHeaders);
    when(clusterService.renameHost(any(), any(), any())).thenThrow(
      new ClustermgmtNotFoundException("Unknown cluster Uuid"));
    ResponseEntity<RenameHostApiResponse> hostRenameResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/hosts/"
          + TestConstantUtils.randomUUIDString() + "/$actions/rename-host",
        HttpMethod.POST, hostNameParamHttpEntity, RenameHostApiResponse.class);
    RenameHostApiResponse responseData = (RenameHostApiResponse) hostRenameResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void getRsyslogServerTest() throws ClustermgmtServiceException {
    when(clusterService.getRsyslogServer(any(), any())).thenReturn(ClusterTestUtils.getRsyslogServer());
    String clusterUuid = TestConstantUtils.randomUUIDString();
    ResponseEntity<GetRsyslogServerApiResponse> getRsyslogServerResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + clusterUuid + "/rsyslog-servers/testRsyslogServerName",
        HttpMethod.GET, null, GetRsyslogServerApiResponse.class);
    RsyslogServer responseData = (RsyslogServer) getRsyslogServerResponseResponseEntity.getBody().getData();
    List<ApiLink> links = getRsyslogServerResponseResponseEntity.getBody().getMetadata().getLinks();
    assertEquals(getRsyslogServerResponseResponseEntity.getStatusCode(), HttpStatus.OK);
    assertEquals(responseData.getServerName(), ClusterTestUtils.RSYSLOG_SERVER_NAME);
    assertEquals(links.size(), 1);
    assertEquals(links.get(0).getRel(), "self");
    assertEquals(links.get(0).getHref(), HATEOS_LINK_HREF_PREFIX + CLUSTER_URL + clusterUuid + "/rsyslog-servers/testRsyslogServerName");
  }

  @Test
  public void getRsyslogServerTestInCaseOfException() throws ClustermgmtServiceException {
    when(clusterService.getRsyslogServer(any(), any())).thenThrow(
      new ClustermgmtNotFoundException("Unknown cluster Uuid")
    );
    ResponseEntity<GetRsyslogServerApiResponse> getRsyslogServerResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/rsyslog-servers/testRsyslogServerName",
        HttpMethod.GET, null, GetRsyslogServerApiResponse.class);
    GetRsyslogServerApiResponse responseData = (GetRsyslogServerApiResponse) getRsyslogServerResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void getSnmpUserTest() throws ClustermgmtServiceException {
    when(clusterService.getSnmpUser(any(), any())).thenReturn(ClusterTestUtils.getSnmpUser());
    String clusterUuid = TestConstantUtils.randomUUIDString();
    ResponseEntity<GetSnmpUserApiResponse> getSnmpUserResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + clusterUuid  + "/snmp/users/testSnmpUserName",
        HttpMethod.GET, null, GetSnmpUserApiResponse.class);
    SnmpUser responseData = (SnmpUser) getSnmpUserResponseResponseEntity.getBody().getData();
    List<ApiLink> links = getSnmpUserResponseResponseEntity.getBody().getMetadata().getLinks();
    assertEquals(getSnmpUserResponseResponseEntity.getStatusCode(), HttpStatus.OK);
    assertEquals(responseData.getUsername(), ClusterTestUtils.SNMP_USERNAME);
    assertEquals(links.size(), 1);
    assertEquals(links.get(0).getRel(), "self");
    assertEquals(links.get(0).getHref(), HATEOS_LINK_HREF_PREFIX + CLUSTER_URL + clusterUuid + "/snmp/users/testSnmpUserName");
  }

  @Test
  public void getSnmpUserTestInCaseOfException() throws ClustermgmtServiceException {
    when(clusterService.getSnmpUser(any(), any())).thenThrow(
      new ClustermgmtNotFoundException("Unknown cluster Uuid")
    );
    ResponseEntity<GetSnmpUserApiResponse> getSnmpUserResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/snmp/users/testSnmpUserName",
        HttpMethod.GET, null, GetSnmpUserApiResponse.class);
    GetSnmpUserApiResponse responseData = (GetSnmpUserApiResponse) getSnmpUserResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void getSnmpTrapTest() throws ClustermgmtServiceException {
    when(clusterService.getSnmpTrap(any(), any())).thenReturn(ClusterTestUtils.getSnmpTrap());
    String clusterUuid = TestConstantUtils.randomUUIDString();
    ResponseEntity<GetSnmpTrapApiResponse> getSnmpTrapResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + clusterUuid + "/snmp/traps/testSnmpTrapAddress",
        HttpMethod.GET, null, GetSnmpTrapApiResponse.class);
    SnmpTrap responseData = (SnmpTrap) getSnmpTrapResponseResponseEntity.getBody().getData();
    List<ApiLink> links = getSnmpTrapResponseResponseEntity.getBody().getMetadata().getLinks();
    assertEquals(getSnmpTrapResponseResponseEntity.getStatusCode(), HttpStatus.OK);
    assertEquals(responseData.getAddress().getIpv4().getValue(), ClusterTestUtils.SNMP_TRAP_ADDRESS);
    assertEquals(links.size(), 1);
    assertEquals(links.get(0).getRel(), "self");
    assertEquals(links.get(0).getHref(), HATEOS_LINK_HREF_PREFIX + CLUSTER_URL + clusterUuid + "/snmp/traps/testSnmpTrapAddress");
  }

  @Test
  public void getSnmpTrapTestInCaseOfException() throws ClustermgmtServiceException {
    when(clusterService.getSnmpTrap(any(), any())).thenThrow(
      new ClustermgmtNotFoundException("Unknown cluster Uuid")
    );
    ResponseEntity<GetSnmpTrapApiResponse> getSnmpTrapResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/snmp/traps/testSnmpTrapAddress",
        HttpMethod.GET, null, GetSnmpTrapApiResponse.class);
    GetSnmpTrapApiResponse responseData = (GetSnmpTrapApiResponse) getSnmpTrapResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void getAllHostsTest() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.getAllHostEntities(any(), any(), any(), any(), any(), any())).thenReturn(HostTestUtils.getHostEntitiesObj());

    final List<String> queryStrings = Arrays.asList("$page=0", "$page=-1", "$limit=-1", "$limit=501", "$limit=500");
    for(String queryString: queryStrings) {
      ResponseEntity<ListHostsApiResponse> getAllHostsResponseResponseEntity =
        testRestTemplate.exchange(   ClustersApiControllerInterface.LIST_HOSTS_URI + "?" +  queryString,
          HttpMethod.GET, null, ListHostsApiResponse.class);
      List<Host> responseData = (List<Host>) getAllHostsResponseResponseEntity.getBody().getData();
      assertEquals(getAllHostsResponseResponseEntity.getStatusCode(), HttpStatus.OK);
      assertEquals(responseData.get(0).getHostName(), HostTestUtils.HOST_NAME);
    }
  }

  @Test
  public void getAllHostsTestInCaseOfException() throws ClustermgmtServiceException, ValidationException {
    final ErrorCode errorCode = ErrorCode.CLUSTERMGMT_ZKCONFIG_ERROR;
    when(clusterService.getAllHostEntities(any(), any(), any(),any(), any(), any())).
      thenThrow(new ClustermgmtZkConfigReadException("Failed to read zeus config"));
    ResponseEntity<ListHostsApiResponse> getAllHostsResponseResponseEntity =
      testRestTemplate.exchange(ClustersApiControllerInterface.LIST_HOSTS_URI,
        HttpMethod.GET, null, ListHostsApiResponse.class);
    ListHostsApiResponse responseData = getAllHostsResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
    final ErrorResponse errorResponse = (ErrorResponse) responseData.getData();
    List<AppMessage> messageList = (List<AppMessage>) errorResponse.getError();
    final AppMessage appMessage = messageList.get(0);
    assertEquals(appMessage.getCode(), "CLU-" + errorCode.getStandardCode());
  }

  @Test
  public void getAllHostsTestInCaseOfValidationException() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.getAllHostEntities(any(), any(), any(), any(), any(), any())).
      thenThrow(new ValidationException("Failure in parsing Odata request"));
    ResponseEntity<ListHostsApiResponse> getAllHostsResponseResponseEntity =
      testRestTemplate.exchange(ClustersApiControllerInterface.LIST_HOSTS_URI,
        HttpMethod.GET, null, ListHostsApiResponse.class);
    ListHostsApiResponse responseData = getAllHostsResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
    final ErrorResponse errorResponse = (ErrorResponse) responseData.getData();
    List<AppMessage> messageList = (List<AppMessage>) errorResponse.getError();
    final AppMessage appMessage = messageList.get(0);
    log.info("Error code is {}", appMessage.getCode());
    assertEquals(appMessage.getCode(), "CLU-" + "10006");
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void getAllHostGpusTest() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.getAllHostGpus(any(), any(), any(),any(), any())).thenReturn(HostTestUtils.getClusterHostGpusObj());
    final List<String> queryStrings = Arrays.asList("$page=0", "$page=-1", "$limit=-1", "$limit=501", "$limit=500", "$select=nodeUuid");
    for(String queryString: queryStrings) {
      ResponseEntity<ListHostGpusApiResponse> getAllHostGpusResponseResponseEntity =
        testRestTemplate.exchange(ClustersApiControllerInterface.LIST_HOST_GPUS_URI + "?" + queryString,
          HttpMethod.GET, null, ListHostGpusApiResponse.class);
      List<HostGpu> responseData = (List<HostGpu>) getAllHostGpusResponseResponseEntity.getBody().getData();
      assertEquals(getAllHostGpusResponseResponseEntity.getStatusCode(), HttpStatus.OK);
      assertEquals(responseData.get(0).getExtId(), HostTestUtils.HOST_GPU_UUID);
    }
  }

  @Test
  public void getAllHostGpusTestInCaseOfException() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.getAllHostGpus(any(), any(), any(), any(), any())).
      thenThrow(new ClustermgmtNotFoundException("unkmnow host gpu uuid"));
    String url = ClustersApiControllerInterface.LIST_HOST_GPUS_URI;
    ResponseEntity<ListHostGpusByClusterIdApiResponse> getHostGpusResponseResponseEntity =
      testRestTemplate.exchange(url,
        HttpMethod.GET, null, ListHostGpusByClusterIdApiResponse.class);
    ListHostGpusByClusterIdApiResponse responseData = getHostGpusResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void getAllHostGpusTestInCaseOfValidationException() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.getAllHostGpus(any(), any(), any(), any(), any())).
      thenThrow(new ValidationException("Failure in parsing Odata request"));
    String url = ClustersApiControllerInterface.LIST_HOST_GPUS_URI;
    ResponseEntity<ListHostGpusApiResponse> getAllHostGpusResponseResponseEntity =
      testRestTemplate.exchange(url,
        HttpMethod.GET, null, ListHostGpusApiResponse.class);
    ListHostGpusApiResponse responseData = getAllHostGpusResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void discoverNodeTest() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.discoverUnconfiguredNodes(any(), any())).thenReturn(ClusterTestUtils.getTaskWithColonUuid());
    NodeDiscoveryParams discoverUnconfiguredNode = new NodeDiscoveryParams();
    HttpEntity<NodeDiscoveryParams> discoverUnconfiguredNodeHttpEntity = new HttpEntity<>(
      discoverUnconfiguredNode, httpHeaders);
    ResponseEntity<DiscoverUnconfiguredNodesApiResponse> discoverNodeTaskApiResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() +
          "/$actions/discover-unconfigured-nodes",
        HttpMethod.POST, discoverUnconfiguredNodeHttpEntity, DiscoverUnconfiguredNodesApiResponse.class);
    TaskReference responseData = (TaskReference) discoverNodeTaskApiResponseResponseEntity.getBody().getData();
    assertEquals(discoverNodeTaskApiResponseResponseEntity.getStatusCode(), HttpStatus.ACCEPTED);
    assertEquals(responseData.getExtId(), ClusterTestUtils.TASK_COLON_UUID);
    List<ApiLink> links = discoverNodeTaskApiResponseResponseEntity.getBody().getMetadata().getLinks();
    assertEquals(links.get(0).getHref(), HATEOS_TASK_LINK);
  }

  @Test
  public void discoverNodeTestInCaseOfException() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.discoverUnconfiguredNodes(any(), any())).thenThrow(
      new ClustermgmtNotFoundException("Unknown cluster Uuid"));
    NodeDiscoveryParams discoverUnconfiguredNode = new NodeDiscoveryParams();
    HttpEntity<NodeDiscoveryParams> discoverUnconfiguredNodeHttpEntity = new HttpEntity<>(
      discoverUnconfiguredNode, httpHeaders);
    ResponseEntity<DiscoverUnconfiguredNodesApiResponse> discoverNodeTaskApiResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() +
          "/$actions/discover-unconfigured-nodes",
        HttpMethod.POST, discoverUnconfiguredNodeHttpEntity, DiscoverUnconfiguredNodesApiResponse.class);
    DiscoverUnconfiguredNodesApiResponse responseData = (DiscoverUnconfiguredNodesApiResponse) discoverNodeTaskApiResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void nodeNetworkingDetailsTest() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.getNodeNetworkingDetails(any(), any())).thenReturn(ClusterTestUtils.getTaskWithColonUuid());
    NodeDetails networkingDetails = new NodeDetails();
    HttpEntity<NodeDetails> networkingDetailsHttpEntity = new HttpEntity<>(
      networkingDetails, httpHeaders);
    ResponseEntity<FetchNodeNetworkingDetailsApiResponse> nodeNetworkingTaskApiResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() +
          "/$actions/fetch-node-networking-details",
        HttpMethod.POST, networkingDetailsHttpEntity, FetchNodeNetworkingDetailsApiResponse.class);
    TaskReference responseData = (TaskReference) nodeNetworkingTaskApiResponseResponseEntity.getBody().getData();
    assertEquals(nodeNetworkingTaskApiResponseResponseEntity.getStatusCode(), HttpStatus.ACCEPTED);
    assertEquals(responseData.getExtId(), ClusterTestUtils.TASK_COLON_UUID);
    List<ApiLink> links = nodeNetworkingTaskApiResponseResponseEntity.getBody().getMetadata().getLinks();
    assertEquals(links.get(0).getHref(), HATEOS_TASK_LINK);
  }

  @Test
  public void nodeNetworkingDetailsInCaseOfException() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.getNodeNetworkingDetails(any(), any())).thenThrow(
      new ClustermgmtNotFoundException("Unknown cluster Uuid"));
    NodeDetails networkingDetails = new NodeDetails();
    HttpEntity<NodeDetails> networkingDetailsHttpEntity = new HttpEntity<>(
      networkingDetails, httpHeaders);
    ResponseEntity<FetchNodeNetworkingDetailsApiResponse> nodeNetworkingTaskApiResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() +
          "/$actions/fetch-node-networking-details",
        HttpMethod.POST, networkingDetailsHttpEntity, FetchNodeNetworkingDetailsApiResponse.class);
    FetchNodeNetworkingDetailsApiResponse responseData = (FetchNodeNetworkingDetailsApiResponse) nodeNetworkingTaskApiResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void expandClusterTest() throws ClustermgmtServiceException, ValidationException, IdempotencySupportException {
    final String taskUuid = ClusterTestUtils.TASK_COLON_UUID;
    when(clustermgmtIdempotentTokenFactory.getIdempotentTaskInfo(any()))
      .thenReturn(new Pair<>(taskUuid, true));
    doNothing().when(clusterService).addNode(any(), any(), any());

    ExpandClusterParams addNode = new ExpandClusterParams();
    HttpEntity<ExpandClusterParams> addNodeHttpEntity = new HttpEntity<>(
      addNode, httpHeaders);
    ResponseEntity<ExpandClusterApiResponse> addNodeTaskResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() +
          "/$actions/expand-cluster",
        HttpMethod.POST, addNodeHttpEntity, ExpandClusterApiResponse.class);
    TaskReference responseData = (TaskReference) addNodeTaskResponseResponseEntity.getBody().getData();
    assertEquals(addNodeTaskResponseResponseEntity.getStatusCode(), HttpStatus.ACCEPTED);
  }

  @Test
  public void expandClusterInCaseOfException() throws ClustermgmtServiceException, ValidationException, IdempotencySupportException {
    doThrow(new ClustermgmtNotFoundException("Unknown cluster Uuid")).when(clusterService).addNode(any(), any(), any());
    when(clustermgmtIdempotentTokenFactory.getIdempotentTaskInfo(any()))
      .thenReturn(new Pair<>(TestConstantUtils.randomUUIDString(), false));
    ExpandClusterParams addNode = new ExpandClusterParams();
    HttpEntity<ExpandClusterParams> addNodeHttpEntity = new HttpEntity<>(
      addNode, httpHeaders);
    ResponseEntity<ExpandClusterApiResponse> addNodeTaskResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() +
          "/$actions/expand-cluster",
        HttpMethod.POST, addNodeHttpEntity, ExpandClusterApiResponse.class);
    ExpandClusterApiResponse responseData = (ExpandClusterApiResponse) addNodeTaskResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void isHypervisorUploadRequiredTest() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.isHypervisorUploadRequired(any(), any())).thenReturn(ClusterTestUtils.getTaskWithColonUuid());
    HypervisorUploadParam hypervisorUploadParam = new HypervisorUploadParam();
    HttpEntity<HypervisorUploadParam> hypervisorUploadParamHttpEntity = new HttpEntity<>(
      hypervisorUploadParam, httpHeaders);
    ResponseEntity<CheckHypervisorRequirementsApiResponse> hypervisorUplpadRequiredTaskResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() +
          "/$actions/check-hypervisor-requirements",
        HttpMethod.POST, hypervisorUploadParamHttpEntity, CheckHypervisorRequirementsApiResponse.class);
    TaskReference responseData = (TaskReference) hypervisorUplpadRequiredTaskResponseResponseEntity.getBody().getData();
    assertEquals(hypervisorUplpadRequiredTaskResponseResponseEntity.getStatusCode(), HttpStatus.ACCEPTED);
    assertEquals(responseData.getExtId(), ClusterTestUtils.TASK_COLON_UUID);
    List<ApiLink> links = hypervisorUplpadRequiredTaskResponseResponseEntity.getBody().getMetadata().getLinks();
    assertEquals(links.get(0).getHref(), HATEOS_TASK_LINK);
  }

  @Test
  public void isHypervisorUploadRequiredTestInCaseOfException() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.isHypervisorUploadRequired(any(), any())).thenThrow(
      new ClustermgmtNotFoundException("Unknown cluster Uuid"));
    HypervisorUploadParam hypervisorUploadParam = new HypervisorUploadParam();
    HttpEntity<HypervisorUploadParam> hypervisorUploadParamHttpEntity = new HttpEntity<>(
      hypervisorUploadParam, httpHeaders);
    ResponseEntity<CheckHypervisorRequirementsApiResponse> hypervisorUplpadRequiredTaskResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() +
          "/$actions/check-hypervisor-requirements",
        HttpMethod.POST, hypervisorUploadParamHttpEntity, CheckHypervisorRequirementsApiResponse.class);
    CheckHypervisorRequirementsApiResponse responseData = (CheckHypervisorRequirementsApiResponse) hypervisorUplpadRequiredTaskResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void validateNodeTest() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.validateNode(any(), any())).thenReturn(ClusterTestUtils.getTaskWithColonUuid());
    ValidateNodeParam validateNodeParam = new ValidateNodeParam();
    HttpEntity<ValidateNodeParam> validateNodeParamHttpEntity = new HttpEntity<>(
      validateNodeParam, httpHeaders);
    ResponseEntity<ValidateNodeApiResponse> validateNodeTaskResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() +
          "/$actions/validate-node",
        HttpMethod.POST, validateNodeParamHttpEntity, ValidateNodeApiResponse.class);
    TaskReference responseData = (TaskReference) validateNodeTaskResponseResponseEntity.getBody().getData();
    assertEquals(validateNodeTaskResponseResponseEntity.getStatusCode(), HttpStatus.ACCEPTED);
    assertEquals(responseData.getExtId(), ClusterTestUtils.TASK_COLON_UUID);
    List<ApiLink> links = validateNodeTaskResponseResponseEntity.getBody().getMetadata().getLinks();
    assertEquals(links.get(0).getHref(), HATEOS_TASK_LINK);
  }

  @Test
  public void validateNodeTestInCaseOfException() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.validateNode(any(), any())).thenThrow(
      new ClustermgmtNotFoundException("Unknown cluster Uuid"));
    ValidateNodeParam validateNodeParam = new ValidateNodeParam();
    HttpEntity<ValidateNodeParam> validateNodeParamHttpEntity = new HttpEntity<>(
      validateNodeParam, httpHeaders);
    ResponseEntity<ValidateNodeApiResponse> validateNodeTaskResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() +
          "/$actions/validate-node",
        HttpMethod.POST, validateNodeParamHttpEntity, ValidateNodeApiResponse.class);
    ValidateNodeApiResponse responseData = (ValidateNodeApiResponse) validateNodeTaskResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void removeNodeTest() throws ClustermgmtServiceException, ValidationException, IdempotencySupportException {
    final String taskUuid = ClusterTestUtils.TASK_COLON_UUID;
    when(clustermgmtIdempotentTokenFactory.getIdempotentTaskInfo(any()))
      .thenReturn(new Pair<>(taskUuid, true));
    doNothing().when(clusterService).removeNode(any(), any(), any());
    NodeRemovalParams removeNode = new NodeRemovalParams();
    HttpEntity<NodeRemovalParams> removeNodeHttpEntity = new HttpEntity<>(
      removeNode, httpHeaders);
    ResponseEntity<RemoveNodeApiResponse> removeNodeTaskResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() +
          "/$actions/remove-node",
        HttpMethod.POST, removeNodeHttpEntity, RemoveNodeApiResponse.class);
    TaskReference responseData = (TaskReference) removeNodeTaskResponseResponseEntity.getBody().getData();
    assertEquals(removeNodeTaskResponseResponseEntity.getStatusCode(), HttpStatus.ACCEPTED);
  }

  @Test
  public void removeNodeTestInCaseOfException() throws ClustermgmtServiceException, ValidationException, IdempotencySupportException {
    doThrow(new ClustermgmtNotFoundException("Unknown cluster Uuid")).when(clusterService).removeNode(any(), any(), any());
    when(clustermgmtIdempotentTokenFactory.getIdempotentTaskInfo(any()))
      .thenReturn(new Pair<>(TestConstantUtils.randomUUIDString(), false));
    NodeRemovalParams removeNode = new NodeRemovalParams();
    HttpEntity<NodeRemovalParams> removeNodeHttpEntity = new HttpEntity<>(
      removeNode, httpHeaders);
    ResponseEntity<RemoveNodeApiResponse> removeNodeTaskResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() +
          "/$actions/remove-node",
        HttpMethod.POST, removeNodeHttpEntity, RemoveNodeApiResponse.class);
    RemoveNodeApiResponse responseData = (RemoveNodeApiResponse) removeNodeTaskResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void getSearchTest() throws ClustermgmtServiceException {
    when(clusterService.getTaskResponse(any(), any())).thenReturn(new TaskResponse());
    ResponseEntity<FetchTaskApiResponse> searchResponseResponseEntity =
      testRestTemplate.exchange( "/clustermgmt/v4/config/task-response/" + TestConstantUtils.randomUUIDString() + "?taskResponseType=UNCONFIGURED_NODES",
        HttpMethod.GET, null, FetchTaskApiResponse.class);
    TaskResponse responseData = (TaskResponse) searchResponseResponseEntity.getBody().getData();
    assertEquals(searchResponseResponseEntity.getStatusCode(), HttpStatus.OK);
  }

  @Test
  public void getSearchTestInCaseOfException() throws ClustermgmtServiceException {
    when(clusterService.getTaskResponse(any(), any())).thenThrow(
      new ClustermgmtServiceException("Unknown task uuid")
    );
    ResponseEntity<FetchTaskApiResponse> searchResponseResponseEntity =
      testRestTemplate.exchange( "/clustermgmt/v4/config/task-response/" + TestConstantUtils.randomUUIDString() + "?taskResponseType=UNCONFIGURED_NODES",
        HttpMethod.GET, null, FetchTaskApiResponse.class);
    FetchTaskApiResponse responseData = (FetchTaskApiResponse) searchResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void getSearchTestWhenQueryParamNotPassed() throws ClustermgmtServiceException {
    when(clusterService.getTaskResponse(any(), any())).thenReturn(new TaskResponse());
    ResponseEntity<FetchTaskApiResponse> searchResponseResponseEntity =
      testRestTemplate.exchange( "/clustermgmt/v4/config/task-response/" + TestConstantUtils.randomUUIDString(),
        HttpMethod.GET, null, FetchTaskApiResponse.class);
    TaskResponse responseData = (TaskResponse) searchResponseResponseEntity.getBody().getData();
    assertEquals(searchResponseResponseEntity.getStatusCode(), HttpStatus.BAD_REQUEST);
  }

  @Test
  public void getHostNicsTest() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.getHostNics(any(), any(), any(), any(), any(), any(), any())).thenReturn(HostTestUtils.getHostNicsObj());
    final List<String> queryStrings = Arrays.asList("$page=0", "$page=-1", "$limit=-1", "$limit=501", "$limit=500", "$select=name,nodeUuid");
    for(String queryString: queryStrings) {
      String url = CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/hosts/" + TestConstantUtils.randomUUIDString()
              + "/host-nics?" + queryString;
      ResponseEntity<ListHostNicsByHostIdApiResponse> getHostNicsResponseResponseEntity =
              testRestTemplate.exchange(url,
                      HttpMethod.GET, null, ListHostNicsByHostIdApiResponse.class);
      List<HostNic> responseData = (List<HostNic>) getHostNicsResponseResponseEntity.getBody().getData();
      assertEquals(getHostNicsResponseResponseEntity.getStatusCode(), HttpStatus.OK);
      assertEquals(responseData.get(0).getExtId(), HostTestUtils.HOST_NICS_UUID);
    }
  }

  @Test
  public void getAllHostNicsTest() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.getAllHostNics(any(), any(), any(), any(), any())).thenReturn(HostTestUtils.getHostNicsObj());
    final List<String> queryStrings = Arrays.asList("$page=0", "$page=-1", "$limit=-1", "$limit=501", "$limit=500", "$select=name,nodeUuid");
    for(String queryString: queryStrings) {
      String url = BASE_URL + "/host-nics?" + queryString;
      ResponseEntity<ListHostNicsApiResponse> getNicsResponseResponseEntity =
              testRestTemplate.exchange(url,
                      HttpMethod.GET, null, ListHostNicsApiResponse.class);
      List<HostNic> responseData = (List<HostNic>) getNicsResponseResponseEntity.getBody().getData();
      assertEquals(getNicsResponseResponseEntity.getStatusCode(), HttpStatus.OK);
      assertEquals(responseData.get(0).getExtId(), HostTestUtils.HOST_NICS_UUID);
    }
  }

  @Test
  public void getAllHostNicsTestInCaseOfException() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.getAllHostNics(any(), any(), any(), any(), any())).
            thenThrow(new ClustermgmtNotFoundException("unknown error"));
    String url = BASE_URL + "/host-nics";
    ResponseEntity<ListHostNicsApiResponse> getNicsResponseResponseEntity =
            testRestTemplate.exchange(url,
                    HttpMethod.GET, null, ListHostNicsApiResponse.class);
    ListHostNicsApiResponse responseData = getNicsResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void getHostNicsTestInCaseOfException() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.getHostNics(any(), any(), any(), any(), any(),any(), any())).
            thenThrow(new ClustermgmtNotFoundException("unknown host nic uuid"));
    String url = CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/hosts/" + TestConstantUtils.randomUUIDString() + "/host-nics";
    ResponseEntity<ListHostNicsByHostIdApiResponse> getHostNicsResponseResponseEntity =
            testRestTemplate.exchange(url,
                    HttpMethod.GET, null, ListHostNicsByHostIdApiResponse.class);
    ListHostNicsByHostIdApiResponse responseData = getHostNicsResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void getHostNicsTestInCaseOfValidationException() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.getHostNics(any(), any(), any(), any(), any(), any(), any())).
            thenThrow(new ValidationException("Failure in parsing Odata request"));

    String url = CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/hosts/" + TestConstantUtils.randomUUIDString() + "/host-nics";
    ResponseEntity<ListHostNicsByHostIdApiResponse> getHostNicsResponseResponseEntity =
            testRestTemplate.exchange(url,
                    HttpMethod.GET, null, ListHostNicsByHostIdApiResponse.class);
    ListHostNicsByHostIdApiResponse responseData = getHostNicsResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void getHostNicTest() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.getHostNic(any(), any(), any())).thenReturn(HostTestUtils.getHostNicObj());
    String clusterUuid = TestConstantUtils.randomUUIDString();
    String hostUuid = TestConstantUtils.randomUUIDString();
    String nicUuid = TestConstantUtils.randomUUIDString();
    ResponseEntity<GetHostNicApiResponse> getHostNicResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + clusterUuid + "/hosts/"
          + hostUuid + "/host-nics/" + nicUuid,
        HttpMethod.GET, null, GetHostNicApiResponse.class);
    HostNic responseData = (HostNic) getHostNicResponseResponseEntity.getBody().getData();
    List<ApiLink> links = getHostNicResponseResponseEntity.getBody().getMetadata().getLinks();
    assertEquals(getHostNicResponseResponseEntity.getStatusCode(), HttpStatus.OK);
    assertEquals(responseData.getExtId(), HostTestUtils.HOST_NICS_UUID);
    assertEquals(links.size(), 3);
    assertEquals(links.get(0).getRel(), "self");
    assertEquals(links.get(0).getHref(), HATEOS_LINK_HREF_PREFIX + CLUSTER_URL + clusterUuid + "/hosts/"
      + hostUuid + "/host-nics/" + nicUuid);
  }

  @Test
  public void getVirtualNicsTest() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.getVirtualNics(any(), any(), any(), any(), any(), any(), any())).thenReturn(HostTestUtils.getVirtualNicsObj());
    final List<String> queryStrings = Arrays.asList("$page=0", "$page=-1", "$limit=-1", "$limit=501", "$limit=500", "$select=name,nodeUuid");
    for(String queryString: queryStrings) {
      String url = CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/hosts/" + TestConstantUtils.randomUUIDString()
        + "/virtual-nics?" + queryString;
      ResponseEntity<ListVirtualNicsByHostIdApiResponse> getVirtualNicsResponseResponseEntity =
        testRestTemplate.exchange(url,
          HttpMethod.GET, null, ListVirtualNicsByHostIdApiResponse.class);
      List<VirtualNic> responseData = (List<VirtualNic>) getVirtualNicsResponseResponseEntity.getBody().getData();
      assertEquals(getVirtualNicsResponseResponseEntity.getStatusCode(), HttpStatus.OK);
      assertEquals(responseData.get(0).getExtId(), HostTestUtils.VIRTUAL_NICS_UUID);
    }
  }

  @Test
  public void getVirtualNicsTestInCaseOfException() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.getVirtualNics(any(), any(), any(), any(), any(),any(), any())).
      thenThrow(new ClustermgmtNotFoundException("unknown virtual nic uuid"));
    String url = CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/hosts/" + TestConstantUtils.randomUUIDString() + "/virtual-nics";
    ResponseEntity<ListVirtualNicsByHostIdApiResponse> getVirtualNicsResponseResponseEntity =
      testRestTemplate.exchange(url,
        HttpMethod.GET, null, ListVirtualNicsByHostIdApiResponse.class);
    ListVirtualNicsByHostIdApiResponse responseData = getVirtualNicsResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void getVirtualNicsTestInCaseOfValidationException() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.getVirtualNics(any(), any(), any(), any(), any(), any() ,any())).
      thenThrow(new ValidationException("Failure in parsing Odata request"));

    String url = CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/hosts/" + TestConstantUtils.randomUUIDString() + "/virtual-nics";
    ResponseEntity<ListVirtualNicsByHostIdApiResponse> getVirtualNicsResponseResponseEntity =
      testRestTemplate.exchange(url,
        HttpMethod.GET, null, ListVirtualNicsByHostIdApiResponse.class);
    ListVirtualNicsByHostIdApiResponse responseData = getVirtualNicsResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void getVirtualNicTest() throws ClustermgmtServiceException, ValidationException {
    when(clusterService.getVirtualNic(any(), any(), any())).thenReturn(HostTestUtils.getVirtualNicObj());
    String clusterUuid = TestConstantUtils.randomUUIDString();
    String hostUuid = TestConstantUtils.randomUUIDString();
    String nicUuid = TestConstantUtils.randomUUIDString();
    ResponseEntity<GetVirtualNicApiResponse> getVirtualNicResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + clusterUuid + "/hosts/"
          + hostUuid + "/virtual-nics/" + nicUuid,
        HttpMethod.GET, null, GetVirtualNicApiResponse.class);
    VirtualNic responseData = (VirtualNic) getVirtualNicResponseResponseEntity.getBody().getData();
    List<ApiLink> links = getVirtualNicResponseResponseEntity.getBody().getMetadata().getLinks();
    assertEquals(getVirtualNicResponseResponseEntity.getStatusCode(), HttpStatus.OK);
    assertEquals(responseData.getExtId(), HostTestUtils.VIRTUAL_NICS_UUID);
    assertEquals(links.size(), 1);
    assertEquals(links.get(0).getRel(), "self");
    assertEquals(links.get(0).getHref(), HATEOS_LINK_HREF_PREFIX + CLUSTER_URL + clusterUuid + "/hosts/"
      + hostUuid + "/virtual-nics/" + nicUuid);
  }

  @Test
  public void createClusterTest() throws ClustermgmtServiceException, ValidationException, IdempotencySupportException {
    final String taskUuid = ClusterTestUtils.TASK_UUID;
    Cluster clusterCreateParams = new Cluster();
    when(clustermgmtIdempotentTokenFactory.getIdempotentTaskInfo(any()))
      .thenReturn(new Pair<>(taskUuid, true));
    HttpEntity<Cluster> clusterCreateHttpEntity = new HttpEntity<>(
      clusterCreateParams, httpHeaders);
    doNothing().when(clusterService).clusterCreate(any(), any(),any());
    ResponseEntity<CreateClusterApiResponse> clusterCreateTaskResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL,
        HttpMethod.POST, clusterCreateHttpEntity,CreateClusterApiResponse.class);
    assertEquals(clusterCreateTaskResponseResponseEntity.getStatusCode(), HttpStatus.ACCEPTED);
    TaskReference responseData = (TaskReference) clusterCreateTaskResponseResponseEntity.getBody().getData();
    List<ApiLink> links = clusterCreateTaskResponseResponseEntity.getBody().getMetadata().getLinks();
    assertEquals(links.get(0).getHref(), HATEOS_TASK_LINK);
    assertEquals(clusterCreateTaskResponseResponseEntity.getStatusCode(), HttpStatus.ACCEPTED);
    assertEquals(ErgonTaskUtils.getTaskUuid(responseData.getExtId()), ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void destroyClusterTest() throws ClustermgmtServiceException, IdempotencySupportException {
    final String taskUuid = ClusterTestUtils.TASK_UUID;
    when(clustermgmtIdempotentTokenFactory.getIdempotentTaskInfo(any()))
      .thenReturn(new Pair<>(taskUuid, true));
    doNothing().when(clusterService).clusterDestroy(any(), any(), any());
    ResponseEntity<DeleteClusterApiResponse> clusterDestroyTaskResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + ClusterTestUtils.CLUSTER_UUID,
        HttpMethod.DELETE, null, DeleteClusterApiResponse.class);
    TaskReference responseData = (TaskReference) clusterDestroyTaskResponseResponseEntity.getBody().getData();
    List<ApiLink> links = clusterDestroyTaskResponseResponseEntity.getBody().getMetadata().getLinks();
    assertEquals(links.get(0).getHref(), HATEOS_TASK_LINK);
    assertEquals(clusterDestroyTaskResponseResponseEntity.getStatusCode(), HttpStatus.ACCEPTED);
    assertEquals(ErgonTaskUtils.getTaskUuid(responseData.getExtId()), ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void createClusterTestInCaseOfException() throws ClustermgmtServiceException, IdempotencySupportException{
    Cluster clusterCreateParams = new Cluster();
    when(clustermgmtIdempotentTokenFactory.getIdempotentTaskInfo(any()))
      .thenReturn(new Pair<>(TestConstantUtils.randomUUIDString(), false));
    HttpEntity<Cluster> clusterCreateHttpEntity = new HttpEntity<>(
      clusterCreateParams, httpHeaders);
    doThrow(new ClustermgmtInvalidInputException("Invalid Input")).when(clusterService).clusterCreate(any(), any(),any());
    ResponseEntity<CreateClusterApiResponse> clusterCreateTaskResponseResponseEntity =
      testRestTemplate.exchange(CLUSTER_URL,
        HttpMethod.POST, clusterCreateHttpEntity,CreateClusterApiResponse.class);
    assertEquals(clusterCreateTaskResponseResponseEntity.getStatusCode().toString(), "400 BAD_REQUEST");
  }

  @Test
  public void destroyClusterTestInCaseOfException() throws ClustermgmtServiceException, IdempotencySupportException {
    when(clustermgmtIdempotentTokenFactory.getIdempotentTaskInfo(any()))
      .thenReturn(new Pair<>(TestConstantUtils.randomUUIDString(), false));
    ClustermgmtServiceUnavailableException e = new ClustermgmtServiceUnavailableException("connection refused");
    ClustermgmtGenericException exception = new ClustermgmtGenericException(e);
    doThrow(exception).when(clusterService).clusterDestroy(any(), any(), any());
    ResponseEntity<DeleteClusterApiResponse> clusterDestroyTaskResponseResponseEntity =
      testRestTemplate.exchange(CLUSTER_URL + ClusterTestUtils.CLUSTER_UUID,
        HttpMethod.DELETE, null, DeleteClusterApiResponse.class);
    assertEquals(clusterDestroyTaskResponseResponseEntity.getStatusCode().toString(), "503 SERVICE_UNAVAILABLE");
  }

  @Test
  public void associateCategoryToClusterTest() throws ClustermgmtServiceException {
    CategoryEntityReferences categoriesParams = new CategoryEntityReferences();
    HttpEntity<CategoryEntityReferences> associateCategoryHttpEntity = new HttpEntity<>(
      categoriesParams, httpHeaders);
    when(clusterService.updateCategoryAssociationsForClusterEntity(any(), any(), any())).thenReturn(ClusterTestUtils.getTaskWithColonUuid());
    ResponseEntity<AssociateCategoriesToClusterApiResponse> associateCategoryTaskResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + ClusterTestUtils.CLUSTER_UUID + "/$actions/associate-categories",
        HttpMethod.POST, associateCategoryHttpEntity, AssociateCategoriesToClusterApiResponse.class);
    TaskReference responseData = (TaskReference) associateCategoryTaskResponseResponseEntity.getBody().getData();
    List<ApiLink> links = associateCategoryTaskResponseResponseEntity.getBody().getMetadata().getLinks();
    assertEquals(links.get(0).getHref(), HATEOS_TASK_LINK);
    assertEquals(associateCategoryTaskResponseResponseEntity.getStatusCode(), HttpStatus.ACCEPTED);
    assertEquals(ErgonTaskUtils.getTaskUuid(responseData.getExtId()), ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void associateCategoryToClusterTestInCaseOfException() throws ClustermgmtServiceException {
    CategoryEntityReferences categoriesParams = new CategoryEntityReferences();
    HttpEntity<CategoryEntityReferences> associateCategoryHttpEntity = new HttpEntity<>(
      categoriesParams, httpHeaders);
    when(clusterService.updateCategoryAssociationsForClusterEntity(any(), any(), any())).
      thenThrow(new ClustermgmtInvalidInputException("No categories to update"));
    ResponseEntity<AssociateCategoriesToClusterApiResponse> associateCategoryTaskResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + ClusterTestUtils.CLUSTER_UUID + "/$actions/associate-categories",
        HttpMethod.POST, associateCategoryHttpEntity, AssociateCategoriesToClusterApiResponse.class);
    assertEquals(associateCategoryTaskResponseResponseEntity.getStatusCode().toString(), "400 BAD_REQUEST");
  }

  @Test
  public void disassociateCategoryFromClusterTest() throws ClustermgmtServiceException {
    CategoryEntityReferences categoriesParams = new CategoryEntityReferences();
    HttpEntity<CategoryEntityReferences> associateCategoryHttpEntity = new HttpEntity<>(
      categoriesParams, httpHeaders);
    when(clusterService.updateCategoryAssociationsForClusterEntity(any(), any(), any())).thenReturn(ClusterTestUtils.getTaskWithColonUuid());
    ResponseEntity<DisassociateCategoriesFromClusterApiResponse> disassociateCategoryTaskResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + ClusterTestUtils.CLUSTER_UUID + "/$actions/disassociate-categories",
        HttpMethod.POST, associateCategoryHttpEntity, DisassociateCategoriesFromClusterApiResponse.class);
    TaskReference responseData = (TaskReference) disassociateCategoryTaskResponseResponseEntity.getBody().getData();
    List<ApiLink> links = disassociateCategoryTaskResponseResponseEntity.getBody().getMetadata().getLinks();
    assertEquals(links.get(0).getHref(), HATEOS_TASK_LINK);
    assertEquals(disassociateCategoryTaskResponseResponseEntity.getStatusCode(), HttpStatus.ACCEPTED);
    assertEquals(ErgonTaskUtils.getTaskUuid(responseData.getExtId()), ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void disassociateCategoryFromClusterTestInCaseOfException() throws ClustermgmtServiceException {
    CategoryEntityReferences categoriesParams = new CategoryEntityReferences();
    HttpEntity<CategoryEntityReferences> associateCategoryHttpEntity = new HttpEntity<>(
      categoriesParams, httpHeaders);
    when(clusterService.updateCategoryAssociationsForClusterEntity(any(), any(), any())).
      thenThrow(new ClustermgmtInvalidInputException("No categories to update"));
    ResponseEntity<DisassociateCategoriesFromClusterApiResponse> disassociateCategoryTaskResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + ClusterTestUtils.CLUSTER_UUID + "/$actions/disassociate-categories",
        HttpMethod.POST, associateCategoryHttpEntity, DisassociateCategoriesFromClusterApiResponse.class);
    assertEquals(disassociateCategoryTaskResponseResponseEntity.getStatusCode().toString(), "400 BAD_REQUEST");
  }

  @Test
  public void associateCategoriesToHostNicTest() throws ClustermgmtServiceException {
    CategoryEntityReferences categoriesParams = new CategoryEntityReferences();
    HttpEntity<CategoryEntityReferences> associateCategoryHttpEntity = new HttpEntity<>(
      categoriesParams, httpHeaders);
    when(clusterService.updateCategoryAssociationsForHostNicEntity(any(), any(), any(), any(), any())).thenReturn(ClusterTestUtils.getTaskWithColonUuid());
    ResponseEntity<AssociateCategoriesToHostNicApiResponse> associateHostNicTaskResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + ClusterTestUtils.CLUSTER_UUID + "/hosts/" + TestConstantUtils.randomUUIDString() +
      "/host-nics/" + TestConstantUtils.randomUUIDString() +"/$actions/associate-categories",
        HttpMethod.POST, associateCategoryHttpEntity, AssociateCategoriesToHostNicApiResponse.class);
    TaskReference responseData = (TaskReference) associateHostNicTaskResponseResponseEntity.getBody().getData();
    List<ApiLink> links = associateHostNicTaskResponseResponseEntity.getBody().getMetadata().getLinks();
    assertEquals(links.get(0).getHref(), HATEOS_TASK_LINK);
    assertEquals(associateHostNicTaskResponseResponseEntity.getStatusCode(), HttpStatus.ACCEPTED);
    assertEquals(ErgonTaskUtils.getTaskUuid(responseData.getExtId()), ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void associateCategoriesToHostNicTestInCaseOfException() throws ClustermgmtServiceException {
    CategoryEntityReferences categoriesParams = new CategoryEntityReferences();
    HttpEntity<CategoryEntityReferences> associateCategoryHttpEntity = new HttpEntity<>(
      categoriesParams, httpHeaders);
    when(clusterService.updateCategoryAssociationsForHostNicEntity(any(), any(), any(), any(), any())).
      thenThrow(new ClustermgmtInvalidInputException("No categories to update"));
    ResponseEntity<AssociateCategoriesToHostNicApiResponse> associateCategoryTaskResponseResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + ClusterTestUtils.CLUSTER_UUID + "/hosts/" + TestConstantUtils.randomUUIDString() +
      "/host-nics/" + TestConstantUtils.randomUUIDString() +"/$actions/associate-categories",
        HttpMethod.POST, associateCategoryHttpEntity, AssociateCategoriesToHostNicApiResponse.class);
    assertEquals(associateCategoryTaskResponseResponseEntity.getStatusCode().toString(), "400 BAD_REQUEST");
  }

  @Test
  public void disassociateCategoriesFromHostNicTest() throws ClustermgmtServiceException {
    CategoryEntityReferences categoriesParams = new CategoryEntityReferences();
    HttpEntity<CategoryEntityReferences> associateCategoryHttpEntity = new HttpEntity<>(
      categoriesParams, httpHeaders);
    when(clusterService.updateCategoryAssociationsForHostNicEntity(any(), any(), any(), any(), any())).thenReturn(ClusterTestUtils.getTaskWithColonUuid());
    ResponseEntity<DisassociateCategoriesFromHostNicApiResponse> disassociateCategoryTaskResponseResponseEntity =
    testRestTemplate.exchange( CLUSTER_URL + ClusterTestUtils.CLUSTER_UUID + "/hosts/" + TestConstantUtils.randomUUIDString() +
    "/host-nics/" + TestConstantUtils.randomUUIDString() +"/$actions/disassociate-categories",
        HttpMethod.POST, associateCategoryHttpEntity, DisassociateCategoriesFromHostNicApiResponse.class);
    TaskReference responseData = (TaskReference) disassociateCategoryTaskResponseResponseEntity.getBody().getData();
    List<ApiLink> links = disassociateCategoryTaskResponseResponseEntity.getBody().getMetadata().getLinks();
    assertEquals(links.get(0).getHref(), HATEOS_TASK_LINK);
    assertEquals(disassociateCategoryTaskResponseResponseEntity.getStatusCode(), HttpStatus.ACCEPTED);
    assertEquals(ErgonTaskUtils.getTaskUuid(responseData.getExtId()), ClusterTestUtils.TASK_UUID);
  }

  @Test
  public void disassociateCategoriesFromHostNicTestInCaseOfException() throws ClustermgmtServiceException {
    CategoryEntityReferences categoriesParams = new CategoryEntityReferences();
    HttpEntity<CategoryEntityReferences> associateCategoryHttpEntity = new HttpEntity<>(
      categoriesParams, httpHeaders);
    when(clusterService.updateCategoryAssociationsForHostNicEntity(any(), any(), any(), any(), any())).
      thenThrow(new ClustermgmtInvalidInputException("No categories to update"));
    ResponseEntity<DisassociateCategoriesFromHostNicApiResponse> disassociateCategoryTaskResponseResponseEntity =
    testRestTemplate.exchange( CLUSTER_URL + ClusterTestUtils.CLUSTER_UUID + "/hosts/" + TestConstantUtils.randomUUIDString() +
    "/host-nics/" + TestConstantUtils.randomUUIDString() +"/$actions/disassociate-categories",
        HttpMethod.POST, associateCategoryHttpEntity, DisassociateCategoriesFromHostNicApiResponse.class);
    assertEquals(disassociateCategoryTaskResponseResponseEntity.getStatusCode().toString(), "400 BAD_REQUEST");
  }

  @Test
  public void computeNonMigratableVmsTest() throws ClustermgmtServiceException, ValidationException, IdempotencySupportException, ClustermgmtGenericException {
    final String taskUuid = ClusterTestUtils.TASK_COLON_UUID;
    when(clustermgmtIdempotentTokenFactory.getIdempotentTaskInfo(any()))
      .thenReturn(new Pair<>(taskUuid, true));
    doNothing().when(clusterService).computeNonMigratableVms(any(), any(), any());

    ComputeNonMigratableVmsSpec computeNonMigratableVms = new ComputeNonMigratableVmsSpec();
    HttpEntity<ComputeNonMigratableVmsSpec> computeNonMigratableVmsHttpEntity = new HttpEntity<>(
      computeNonMigratableVms, httpHeaders);
    ResponseEntity<ComputeNonMigratableVmsApiResponse> computeNonMigratableVmsTaskResponseResponseEntity =
      testRestTemplate.exchange(CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/$actions/compute-non-migratable-vms",
        HttpMethod.POST, computeNonMigratableVmsHttpEntity, ComputeNonMigratableVmsApiResponse.class);
    TaskReference responseData = (TaskReference) computeNonMigratableVmsTaskResponseResponseEntity.getBody().getData();
    assertEquals(computeNonMigratableVmsTaskResponseResponseEntity.getStatusCode(), HttpStatus.ACCEPTED);
  }

  @Test
  public void computeNonMigratableVmsTestInCaseOfException() throws ClustermgmtServiceException, ValidationException, IdempotencySupportException, ClustermgmtGenericException {
    doThrow(new ClustermgmtNotFoundException("Compute Non Migratable VMs is not supported on PC cluster uuid")).when(clusterService).computeNonMigratableVms(any(), any(), any());
    when(clustermgmtIdempotentTokenFactory.getIdempotentTaskInfo(any()))
      .thenReturn(new Pair<>(TestConstantUtils.randomUUIDString(), false));
    ComputeNonMigratableVmsSpec computeNonMigratableVms = new ComputeNonMigratableVmsSpec();
    HttpEntity<ComputeNonMigratableVmsSpec> computeNonMigratableVmsHttpEntity = new HttpEntity<>(
      computeNonMigratableVms, httpHeaders);
    ResponseEntity<ComputeNonMigratableVmsApiResponse> computeNonMigratableVmsTaskResponseResponseEntity =
      testRestTemplate.exchange(CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/$actions/compute-non-migratable-vms",
        HttpMethod.POST, computeNonMigratableVmsHttpEntity, ComputeNonMigratableVmsApiResponse.class);
    ComputeNonMigratableVmsApiResponse responseData = (ComputeNonMigratableVmsApiResponse) computeNonMigratableVmsTaskResponseResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void getNonMigratableVmsResultTest() throws ClustermgmtServiceException, ValidationException, ClustermgmtGenericException {
    NonMigratableVmsResult nonMigratableVmsResult = new NonMigratableVmsResult();
    when(clusterService.getNonMigratableVmsResult(any())).thenReturn(nonMigratableVmsResult);
    String url = CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/non-migratable-vms-result/" + TestConstantUtils.randomUUIDString();
    ResponseEntity<GetNonMigratableVmsResultApiResponse> getNonMigratableVmsResultApiResponseEntity = testRestTemplate.exchange(
      url, HttpMethod.GET, null, GetNonMigratableVmsResultApiResponse.class);
    assertEquals(getNonMigratableVmsResultApiResponseEntity.getStatusCode(), HttpStatus.OK);
  }

  @Test
  public void getNonMigratableVmsResultTestInCaseOfException() throws ClustermgmtServiceException, ValidationException, ClustermgmtGenericException {
    when(clusterService.getNonMigratableVmsResult(any())).thenThrow(new ClustermgmtNotFoundException("Unknown NonMigratableVmsResult Uuid"));
    String url = CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/non-migratable-vms-result/" + TestConstantUtils.randomUUIDString();
    ResponseEntity<GetNonMigratableVmsResultApiResponse> getNonMigratableVmsResultApiResponseEntity = testRestTemplate.exchange(
      url, HttpMethod.GET, null, GetNonMigratableVmsResultApiResponse.class);
    GetNonMigratableVmsResultApiResponse responseData  = (GetNonMigratableVmsResultApiResponse) getNonMigratableVmsResultApiResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

}