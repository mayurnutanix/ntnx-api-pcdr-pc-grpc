package com.nutanix.clustermgmtserver.exceptions;

import org.springframework.http.HttpStatus;

public class ClustermgmtServiceUnavailableException extends ClustermgmtServiceException{
  public ClustermgmtServiceUnavailableException(final String message) {
    super(message);
  }

  @Override
  public int getStandardCode() {
    return ErrorCode.CLUSTERMGMT_SERVICE_RPC_ERROR.getStandardCode();
  }

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.SERVICE_UNAVAILABLE;
  }
}
