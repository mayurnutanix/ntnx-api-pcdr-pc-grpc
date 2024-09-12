/*
 * Copyright (c) 2024 Nutanix Inc. All rights reserved.
 *
 * Author: harshvardhan.maheshw@nutanix.com
 */

package com.nutanix.clustermgmtserver.controllers;

import clustermgmt.v4.operations.ClustersApiControllerInterface;
import com.nutanix.api.utils.task.ErgonTaskUtils;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtGenericException;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceException;
import com.nutanix.clustermgmtserver.services.api.ClusterService;
import com.nutanix.clustermgmtserver.utils.*;
import com.nutanix.prism.exception.idempotency.IdempotencySupportException;
import com.nutanix.util.base.Pair;
import com.nutanix.util.base.ValidationException;
import dp1.clu.clustermgmt.v4.config.*;
import dp1.clu.clustermgmt.v4.operations.*;
import dp1.clu.prism.v4.config.TaskReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.RestController;
import static org.springframework.http.HttpHeaders.ETAG;
import static org.springframework.http.HttpHeaders.IF_MATCH;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class OperationsApiControllerImpl implements ClustersApiControllerInterface {
  private static final String RPC_INVOKE_ERROR_MESSAGE = "Exception occurred while invoking rpc";
  private ClusterService clusterService;
  private final ClustermgmtIdempotentTokenFactory clustermgmtIdempotentTokenFactory;

  @Autowired
  public OperationsApiControllerImpl(final ClusterService clusterService,
                                     final ClustermgmtIdempotentTokenFactory clustermgmtIdempotentTokenFactory) {
    this.clusterService = clusterService;
    this.clustermgmtIdempotentTokenFactory = clustermgmtIdempotentTokenFactory;
  }

  @Override
  public ResponseEntity<MappingJacksonValue> enterHostMaintenance(EnterHostMaintenanceSpec body,
                                                                  String clusterExtId, String hostExtId,
                                                                  Map<String, String> allQueryParams,
                                                                  HttpServletRequest request,
                                                                  HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.ACCEPTED;
    EnterHostMaintenanceApiResponse enterHostMaintenanceApiResponse = new EnterHostMaintenanceApiResponse();
    try {
      final Pair<String, Boolean> taskInfo = clustermgmtIdempotentTokenFactory.getIdempotentTaskInfo(request);
      final String taskUuid = taskInfo.left();
      final boolean taskAlreadyExists = taskInfo.right();
      if (!taskAlreadyExists) {
        clusterService.enterHostMaintenance(clusterExtId, hostExtId, body, taskUuid);
      }
      TaskReference task = new TaskReference(ErgonTaskUtils.getTaskReferenceExtId(taskUuid));
      enterHostMaintenanceApiResponse.setDataInWrapper(task);
      enterHostMaintenanceApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(MetadataHateOsLinkUtils.getMetadataHateOsLinkForTask(task),false,false));
    }
    catch (ClustermgmtServiceException | ValidationException | IdempotencySupportException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      enterHostMaintenanceApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(enterHostMaintenanceApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> exitHostMaintenance(HostMaintenanceCommonSpec body,
                                                                String clusterExtId, String hostExtId,
                                                                Map<String, String> allQueryParams,
                                                                HttpServletRequest request,
                                                                HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.ACCEPTED;
    ExitHostMaintenanceApiResponse exitHostMaintenanceApiResponse = new ExitHostMaintenanceApiResponse();
    try {
      final Pair<String, Boolean> taskInfo = clustermgmtIdempotentTokenFactory.getIdempotentTaskInfo(request);
      final String taskUuid = taskInfo.left();
      final boolean taskAlreadyExists = taskInfo.right();
      if (!taskAlreadyExists) {
        clusterService.exitHostMaintenance(clusterExtId, hostExtId, body, taskUuid);
      }
      TaskReference task = new TaskReference(ErgonTaskUtils.getTaskReferenceExtId(taskUuid));
      exitHostMaintenanceApiResponse.setDataInWrapper(task);
      exitHostMaintenanceApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(MetadataHateOsLinkUtils.getMetadataHateOsLinkForTask(task),false,false));
    }
    catch (ClustermgmtServiceException | ValidationException | IdempotencySupportException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      exitHostMaintenanceApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(exitHostMaintenanceApiResponse), httpStatus);
  }
}
