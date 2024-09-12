/*
 * Copyright (c) 2022 Nutanix Inc. All rights reserved
 *
 * Author: rakesh.falak@nutanix.com
 */
package com.nutanix.clustermgmtserver.exceptions;

import org.springframework.http.HttpStatus;

public class ClustermgmtNotSupportedException extends ClustermgmtServiceException{
  public ClustermgmtNotSupportedException(String message) {
    super(message);
  }

  @Override
  public int getStandardCode() {
    return ErrorCode.CLUSTERMGMT_SERVICE_NOT_SUPPORTED_ENTITY.getStandardCode();
  }

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.BAD_REQUEST;
  }
}
