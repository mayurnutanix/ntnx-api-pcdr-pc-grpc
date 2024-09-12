/*
* Copyright (c) 2024 Nutanix Inc. All rights reserved.
*
* Author: harshvardhan.maheshw@nutanix.com
*/

package com.nutanix.clustermgmtserver.controllers;

import clustermgmt.v4.operations.ClustersApiControllerInterface;
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
public class OperationsApiControllerTest extends AbstractTestNGSpringContextTests {
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

  private static final String CLUSTER_URL = "/clustermgmt/v4/operations/clusters/";
  private static final String BASE_URL = "/clustermgmt/v4/operations/";
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
  public void enterHostMaintenanceTest() throws ClustermgmtServiceException, ValidationException, IdempotencySupportException {
    final String taskUuid = ClusterTestUtils.TASK_COLON_UUID;
    when(clustermgmtIdempotentTokenFactory.getIdempotentTaskInfo(any()))
      .thenReturn(new Pair<>(taskUuid, true));
    doNothing().when(clusterService).enterHostMaintenance(any(), any(), any(), any());

    EnterHostMaintenanceSpec enterHostMaintenance = new EnterHostMaintenanceSpec();
    HttpEntity<EnterHostMaintenanceSpec> enterHostMaintenanceHttpEntity = new HttpEntity<>(
      enterHostMaintenance, httpHeaders);
    ResponseEntity<EnterHostMaintenanceApiResponse> enterHostMaintenanceTaskResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/hosts/" + TestConstantUtils.randomUUIDString()
          + "/$actions/enter-host-maintenance",
        HttpMethod.POST, enterHostMaintenanceHttpEntity, EnterHostMaintenanceApiResponse.class);
    TaskReference responseData = (TaskReference) enterHostMaintenanceTaskResponseEntity.getBody().getData();
    assertEquals(enterHostMaintenanceTaskResponseEntity.getStatusCode(), HttpStatus.ACCEPTED);
  }

  @Test
  public void enterHostMaintenanceInCaseOfException() throws ClustermgmtServiceException, ValidationException, IdempotencySupportException {
    doThrow(new ClustermgmtNotFoundException("Unknown cluster Uuid")).when(clusterService).enterHostMaintenance(any(), any(), any(), any());
    when(clustermgmtIdempotentTokenFactory.getIdempotentTaskInfo(any()))
      .thenReturn(new Pair<>(TestConstantUtils.randomUUIDString(), false));
    EnterHostMaintenanceSpec enterHostMaintenance = new EnterHostMaintenanceSpec();
    HttpEntity<EnterHostMaintenanceSpec> enterHostMaintenanceHttpEntity = new HttpEntity<>(
      enterHostMaintenance, httpHeaders);
    ResponseEntity<EnterHostMaintenanceApiResponse> enterHostMaintenanceTaskResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/hosts/" + TestConstantUtils.randomUUIDString()
          + "/$actions/enter-host-maintenance",
        HttpMethod.POST, enterHostMaintenanceHttpEntity, EnterHostMaintenanceApiResponse.class);
    EnterHostMaintenanceApiResponse responseData = (EnterHostMaintenanceApiResponse) enterHostMaintenanceTaskResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }

  @Test
  public void exitHostMaintenanceTest() throws ClustermgmtServiceException, ValidationException, IdempotencySupportException {
    final String taskUuid = ClusterTestUtils.TASK_COLON_UUID;
    when(clustermgmtIdempotentTokenFactory.getIdempotentTaskInfo(any()))
      .thenReturn(new Pair<>(taskUuid, true));
    doNothing().when(clusterService).exitHostMaintenance(any(), any(), any(), any());

    HostMaintenanceCommonSpec exitHostMaintenance = new HostMaintenanceCommonSpec();
    HttpEntity<HostMaintenanceCommonSpec> exitHostMaintenanceHttpEntity = new HttpEntity<>(
      exitHostMaintenance, httpHeaders);
    ResponseEntity<ExitHostMaintenanceApiResponse> exitHostMaintenanceTaskResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/hosts/" + TestConstantUtils.randomUUIDString()
          + "/$actions/exit-host-maintenance",
        HttpMethod.POST, exitHostMaintenanceHttpEntity, ExitHostMaintenanceApiResponse.class);
    TaskReference responseData = (TaskReference) exitHostMaintenanceTaskResponseEntity.getBody().getData();
    assertEquals(exitHostMaintenanceTaskResponseEntity.getStatusCode(), HttpStatus.ACCEPTED);
  }

  @Test
  public void exitHostMaintenanceInCaseOfException() throws ClustermgmtServiceException, ValidationException, IdempotencySupportException {
    doThrow(new ClustermgmtNotFoundException("Unknown cluster Uuid")).when(clusterService).exitHostMaintenance(any(), any(), any(), any());
    when(clustermgmtIdempotentTokenFactory.getIdempotentTaskInfo(any()))
      .thenReturn(new Pair<>(TestConstantUtils.randomUUIDString(), false));
    HostMaintenanceCommonSpec exitHostMaintenance = new HostMaintenanceCommonSpec();
    HttpEntity<HostMaintenanceCommonSpec> exitHostMaintenanceHttpEntity = new HttpEntity<>(
      exitHostMaintenance, httpHeaders);
    ResponseEntity<ExitHostMaintenanceApiResponse> exitHostMaintenanceTaskResponseEntity =
      testRestTemplate.exchange( CLUSTER_URL + TestConstantUtils.randomUUIDString() + "/hosts/" + TestConstantUtils.randomUUIDString()
          + "/$actions/exit-host-maintenance",
        HttpMethod.POST, exitHostMaintenanceHttpEntity, ExitHostMaintenanceApiResponse.class);
    ExitHostMaintenanceApiResponse responseData = (ExitHostMaintenanceApiResponse) exitHostMaintenanceTaskResponseEntity.getBody();
    assertTrue(responseData.getData() instanceof ErrorResponse);
  }
}