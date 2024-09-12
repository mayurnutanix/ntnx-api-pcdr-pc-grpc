/*
 * Copyright (c) 2023 Nutanix Inc. All rights reserved.
 *
 * Author: rakesh.falak@nutanix.com
 */

package com.nutanix.clustermgmtserver.utils;

import com.nutanix.api.utils.task.ErgonTaskUtils;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceException;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceRetryException;
import com.nutanix.odata.util.JsonUtils;
import com.nutanix.prism.exception.idempotency.IdempotencySupportException;
import com.nutanix.prism.util.idempotency.IdempotencySupportService;
import com.nutanix.util.base.Pair;
import dp1.clu.prism.v4.config.TaskReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

import static com.nutanix.prism.constants.IdempotencySupportConstants.NTNX_REQUEST_ID;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Slf4j
@Component
public class ClustermgmtIdempotentTokenFactory {

  private static final int DEFAULT_NUM_RETRIES = 3;
  private static final int DEFAULT_RETRY_DELAY_MILLISECONDS = 500;

  private final IdempotencySupportService idempotencySupportService;

  @Autowired
  public ClustermgmtIdempotentTokenFactory(final IdempotencySupportService idempotencySupportService) {
    this.idempotencySupportService = idempotencySupportService;
  }

  /**
   * Function to sleep for the default timeout value.
   */
  void sleep() throws InterruptedException {
    MILLISECONDS.sleep(DEFAULT_RETRY_DELAY_MILLISECONDS);
  }

  /**
   * Function to create TaskReference object from Task JSON String.
   * @param taskJson - String value of task JSON
   * @return TaskReference Object
   */
  static TaskReference getTaskReferenceFromJson(final String taskJson) {
    return JsonUtils.getType(taskJson, TaskReference.class);
  }

  /**
   * Function to get task uuid from IdempotencySupportService
   * and know if the mapping already existed. If no match is found, generate
   * a new task uuid, and register it to IdempotencySupportService.
   * @param request - Request Object
   * @return Task UUID, Flag for existence of mapping
   */
  @Retryable(value = IdempotencySupportException.class, maxAttempts = DEFAULT_NUM_RETRIES,
    backoff = @Backoff(delay = DEFAULT_RETRY_DELAY_MILLISECONDS))
  public Pair<String, Boolean> getIdempotentTaskInfo(final HttpServletRequest request)
    throws IdempotencySupportException, ClustermgmtServiceException {
    final String requestId = request.getHeader(NTNX_REQUEST_ID);
    if (requestId == null || requestId.trim().isEmpty()) {
      throw ClustermgmtServiceException.requestIdMissingError("HTTP header NTNX-Request-Id " +
        "is not found in the request");
    }
    final String userId = RequestContextHelper.getUserUUID();
    if (userId == null) {
      throw new IdempotencySupportException("User ID is not found in HTTP request");
    }

    RequestContextHelper.ParseErrorInjectionHeader();
    if(ErrorInjection.getErrorAfterRequestIdCreation()){
      throw new ClustermgmtServiceRetryException();
    }
    // Look up idempotency map
    final String taskReferenceJson = idempotencySupportService.fetchTaskOrReserveRequestId(requestId, userId, null);

    String taskUuid;
    boolean doesMappingExist;
    if (taskReferenceJson != null && !taskReferenceJson.isEmpty()) {
      TaskReference taskReference = getTaskReferenceFromJson(taskReferenceJson);
      if (taskReference != null) {
        taskUuid = ErgonTaskUtils.getTaskUuid(taskReference.getExtId());
        doesMappingExist = true;
      } else {
        log.error("Deserialization of task JSON queried from idempotency service failed.");
        throw new ClustermgmtServiceException("Deserialization of task JSON failed.");
      }
    } else {
      // Create new task UUID and register it to idempotency map
      taskUuid = UUID.randomUUID().toString();
      doesMappingExist = false;
      TaskReference taskReference = new TaskReference();
      taskReference.setExtId(ErgonTaskUtils.getTaskReferenceExtId(taskUuid));
      idempotencySupportService.updateTask(requestId, userId, taskUuid, taskReference);
    }

    return new Pair<>(taskUuid, doesMappingExist);
  }
}
