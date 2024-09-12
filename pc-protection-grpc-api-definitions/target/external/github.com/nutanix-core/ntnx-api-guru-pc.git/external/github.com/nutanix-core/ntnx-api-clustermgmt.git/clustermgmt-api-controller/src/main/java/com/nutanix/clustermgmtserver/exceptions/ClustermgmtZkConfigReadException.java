/*
 * Copyright (c) 2022 Nutanix Inc. All rights reserved
 *
 * Author: rakesh.falak@nutanix.com
 */
package com.nutanix.clustermgmtserver.exceptions;

import org.springframework.http.HttpStatus;

public class ClustermgmtZkConfigReadException extends ClustermgmtServiceException{
  public ClustermgmtZkConfigReadException(String message) {
    super(message);
  }

  @Override
  public int getStandardCode() {
    return ErrorCode.CLUSTERMGMT_ZKCONFIG_ERROR.getStandardCode();
  }

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.INTERNAL_SERVER_ERROR;
  }
}
