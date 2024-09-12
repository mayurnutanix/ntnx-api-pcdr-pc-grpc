package com.nutanix.clustermgmtserver.utils;

/*
 * Copyright (c) 2023 Nutanix Inc. All rights reserved
 *
 * Author: rakesh.falak@nutanix.com
 */

import com.nutanix.api.utils.task.ErgonTaskUtils;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceException;
import com.nutanix.prism.exception.idempotency.IdempotencySupportException;
import com.nutanix.prism.util.idempotency.IdempotencySupportService;
import com.nutanix.util.base.Pair;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.nutanix.prism.constants.IdempotencySupportConstants.NTNX_REQUEST_ID;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class IdempotentTokenFactoryTest {

  private IdempotencySupportService idempotencySupportService;

  private ClustermgmtIdempotentTokenFactory clustermgmtIdempotentTokenFactory;

  @BeforeMethod
  public void setUp() {
    idempotencySupportService = Mockito.mock(IdempotencySupportService.class);
    clustermgmtIdempotentTokenFactory = new ClustermgmtIdempotentTokenFactory(idempotencySupportService);
  }

  @AfterMethod
  public void tearDown() {
    TestConstantUtils.resetSecurityContextAuthentication();
  }

  private String mockUserUuid() {
    final String userUuid = TestConstantUtils.randomUUIDString();
    TestConstantUtils.setUserUUIDInSecurityContext(userUuid);
    return userUuid;
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void testGetIdempotentTaskInfoNullRequestId() throws ClustermgmtServiceException {
    final MockHttpServletRequest request = new MockHttpServletRequest();
    try {
      clustermgmtIdempotentTokenFactory.getIdempotentTaskInfo(request);
      Assert.fail("Expected getIdempotentTaskInfo to throw ClustermgmtServiceException");
    } catch (IdempotencySupportException e) {
      Assert.assertEquals("HTTP header NTNX-Request-Id is not found in the request", e.getMessage());
    }
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void testGetIdempotentTaskInfoEmptyRequestId() throws ClustermgmtServiceException {
    final MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(NTNX_REQUEST_ID, "");
    try {
      clustermgmtIdempotentTokenFactory.getIdempotentTaskInfo(request);
      Assert.fail("Expected getIdempotentTaskInfo to throw ClustermgmtServiceException");
    } catch (IdempotencySupportException e) {
      Assert.assertEquals("HTTP header NTNX-Request-Id is not found in the request", e.getMessage());
    }
  }

  @Test(expectedExceptions = IdempotencySupportException.class)
  public void testGetIdempotentTaskInfoNullUserId() throws IdempotencySupportException, ClustermgmtServiceException {
    final MockHttpServletRequest request = new MockHttpServletRequest();
    final String requestId = TestConstantUtils.randomUUIDString();
    request.addHeader(NTNX_REQUEST_ID, requestId);
    TestConstantUtils.resetSecurityContextAuthentication();
    new ClustermgmtIdempotentTokenFactory(idempotencySupportService).getIdempotentTaskInfo(request);
  }

  @Test(expectedExceptions = IdempotencySupportException.class)
  public void testGetIdempotentTaskInfoRetriesExhausted()
    throws IdempotencySupportException, ClustermgmtServiceException {
    final MockHttpServletRequest request = new MockHttpServletRequest();
    final String requestId = TestConstantUtils.randomUUIDString();
    request.addHeader(NTNX_REQUEST_ID, requestId);
    mockUserUuid();
    when(idempotencySupportService.fetchTaskOrReserveRequestId(any(), any(), any()))
      .thenThrow(new IdempotencySupportException("Oh no"));

    new ClustermgmtIdempotentTokenFactory(idempotencySupportService).getIdempotentTaskInfo(request);
  }

  @Test
  public void testGetIdempotentTaskInfoNullTaskJson()
    throws IdempotencySupportException, ClustermgmtServiceException {
    final MockHttpServletRequest request = new MockHttpServletRequest();
    final String requestId = TestConstantUtils.randomUUIDString();
    request.addHeader(NTNX_REQUEST_ID, requestId);
    final String userUuid = mockUserUuid();
    when(idempotencySupportService.fetchTaskOrReserveRequestId(any(), any(), any()))
      .thenReturn(null);

    final Pair<String, Boolean> taskInfo = clustermgmtIdempotentTokenFactory.getIdempotentTaskInfo(request);
    Assert.assertFalse(taskInfo.right());
    verify(idempotencySupportService, times(1)).updateTask(eq(requestId), eq(userUuid), any(), any());
  }

  @Test
  public void testGetIdempotentTaskInfoEmptyTaskJson()
    throws IdempotencySupportException, ClustermgmtServiceException {
    final MockHttpServletRequest request = new MockHttpServletRequest();
    final String requestId = TestConstantUtils.randomUUIDString();
    request.addHeader(NTNX_REQUEST_ID, requestId);
    final String userUuid = mockUserUuid();
    when(idempotencySupportService.fetchTaskOrReserveRequestId(any(), any(), any()))
      .thenReturn("");

    final Pair<String, Boolean> taskInfo = clustermgmtIdempotentTokenFactory.getIdempotentTaskInfo(request);
    Assert.assertFalse(taskInfo.right());
    verify(idempotencySupportService, times(1)).updateTask(eq(requestId), eq(userUuid), any(), any());
  }

  @Test
  public void testGetIdempotentTaskInfoValidTaskJson()
    throws IdempotencySupportException, ClustermgmtServiceException {
    final MockHttpServletRequest request = new MockHttpServletRequest();
    final String requestId = TestConstantUtils.randomUUIDString();
    request.addHeader(NTNX_REQUEST_ID, requestId);
    mockUserUuid();
    final String taskUuid = TestConstantUtils.randomUUIDString();
    when(idempotencySupportService.fetchTaskOrReserveRequestId(any(), any(), any()))
      .thenReturn(String.format("{\"extId\": \"%s\"}", ErgonTaskUtils.getTaskReferenceExtId(taskUuid)));

    final Pair<String, Boolean> taskInfo = clustermgmtIdempotentTokenFactory.getIdempotentTaskInfo(request);
    Assert.assertEquals(taskInfo.left(), taskUuid);
    Assert.assertTrue(taskInfo.right());
    verify(idempotencySupportService, times(0)).updateTask(any(), any(), any(), any());
  }

  @Test(expectedExceptions = ClustermgmtServiceException.class)
  public void testGetIdempotentTaskInfoInvalidTaskJson()
    throws IdempotencySupportException, ClustermgmtServiceException {
    final MockHttpServletRequest request = new MockHttpServletRequest();
    final String requestId = TestConstantUtils.randomUUIDString();
    request.addHeader(NTNX_REQUEST_ID, requestId);
    mockUserUuid();
    final String taskUuid = TestConstantUtils.randomUUIDString();
    when(idempotencySupportService.fetchTaskOrReserveRequestId(any(), any(), any()))
      .thenReturn(String.format("{'extId': '%s'}", taskUuid));

    clustermgmtIdempotentTokenFactory.getIdempotentTaskInfo(request);
  }

}
