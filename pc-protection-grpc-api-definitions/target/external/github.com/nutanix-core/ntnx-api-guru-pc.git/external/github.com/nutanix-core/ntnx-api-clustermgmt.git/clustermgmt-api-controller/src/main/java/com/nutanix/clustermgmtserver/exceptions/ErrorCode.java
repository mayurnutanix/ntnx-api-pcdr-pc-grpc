/*
 * Copyright (c) 2021 Nutanix Inc. All rights reserved.
 *
 * Author: rakesh.falak@nutanix.com
 */

package com.nutanix.clustermgmtserver.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Error Codes associated with Clustermgmt Service to be used in Exception Handling.
 */
@AllArgsConstructor
public enum ErrorCode {

  CLUSTERMGMT_SERVICE_RPC_ERROR(10001, "Cluster Management service error"),
  CLUSTERMGMT_SERVICE_RETRY(10002,
    "Failed while retrying to reach Cluster Management service"),
  CLUSTERMGMT_IDEMPOTENCY_ERROR(10003, "Empty/invalid request id header"),
  CLUSTERMGMT_ETAG_MISMATCH(10004, "Provided Etag doesn't match"),
  CLUSTERMGMT_SERVICE_UNKNOWN_ENTITY(10005, "Cluster Management service can't find entity"),
  CLUSTERMGMT_INVALID_INPUT(10006, "Invalid input"),
  CLUSTERMGMT_ODATA_ERROR(10007,"The odata library failed to generate IDF expression"),
  CLUSTERMGMT_SERVICE_NOT_SUPPORTED_ENTITY(10008, "Cluster Management service doesn't support this entity"),
  CLUSTERMGMT_ZKCONFIG_ERROR(10009, "Failure in reading zeus config database"),
  CLUSTERMGMT_SERVICE_DUPLICATE_HOST_NAME_ERROR(10010,"Duplicate host name found");

  @Getter
  private final int standardCode;
  @Getter
  private final String description;

  @Override
  public String toString() {
    return this.getStandardCode() + " : " + this.getDescription();
  }
}