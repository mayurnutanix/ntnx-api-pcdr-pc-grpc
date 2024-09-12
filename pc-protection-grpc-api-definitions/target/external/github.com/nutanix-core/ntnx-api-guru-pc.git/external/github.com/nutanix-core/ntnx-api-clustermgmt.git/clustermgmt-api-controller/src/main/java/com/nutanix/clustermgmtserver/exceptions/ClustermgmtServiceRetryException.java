/*
 * Copyright (c) 2021 Nutanix Inc. All rights reserved.
 *
 * Author: rakesh.falak@nutanix.com
 */

package com.nutanix.clustermgmtserver.exceptions;

/**
 * Exception thrown when the Clustermgmt Service RPC Client should retry.
 */
public class ClustermgmtServiceRetryException extends ClustermgmtServiceException {

  public ClustermgmtServiceRetryException() {
    super("Error encountered accessing internal service, retry requested");
  }

  public int getStandardCode() {
    return ErrorCode.CLUSTERMGMT_SERVICE_RETRY.getStandardCode();
  }
}