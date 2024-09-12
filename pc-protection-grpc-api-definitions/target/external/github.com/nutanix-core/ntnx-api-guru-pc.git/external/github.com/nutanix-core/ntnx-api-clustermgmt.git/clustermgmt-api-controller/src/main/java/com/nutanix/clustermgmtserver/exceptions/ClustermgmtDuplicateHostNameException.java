package com.nutanix.clustermgmtserver.exceptions;

import org.springframework.http.HttpStatus;

public class ClustermgmtDuplicateHostNameException extends ClustermgmtServiceException{
  public ClustermgmtDuplicateHostNameException(String message) {
    super(message);
  }

  @Override
  public int getStandardCode() {
    return ErrorCode.CLUSTERMGMT_SERVICE_DUPLICATE_HOST_NAME_ERROR.getStandardCode();
  }

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.INTERNAL_SERVER_ERROR;
  }
}
