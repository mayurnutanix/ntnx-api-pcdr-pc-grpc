/*
 * Copyright (c) 2022 Nutanix Inc. All rights reserved
 *
 * Author: rakesh.falak@nutanix.com
 */

package com.nutanix.clustermgmtserver.exceptions;

import org.springframework.http.HttpStatus;

public class ClustermgmtNotFoundException extends ClustermgmtServiceException {
  public ClustermgmtNotFoundException(String message) {
    super(message);
  }

  @Override
  public int getStandardCode() {
    return ErrorCode.CLUSTERMGMT_SERVICE_UNKNOWN_ENTITY.getStandardCode();
  }

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.NOT_FOUND;
  }
}
