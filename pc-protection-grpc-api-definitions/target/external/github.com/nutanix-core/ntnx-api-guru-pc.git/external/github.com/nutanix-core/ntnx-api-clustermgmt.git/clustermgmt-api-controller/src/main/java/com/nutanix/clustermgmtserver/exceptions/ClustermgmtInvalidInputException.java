package com.nutanix.clustermgmtserver.exceptions;

import org.springframework.http.HttpStatus;

public class ClustermgmtInvalidInputException extends ClustermgmtServiceException{
  public ClustermgmtInvalidInputException(final String message) {
    super(message);
  }

  @Override
  public int getStandardCode() {
    return ErrorCode.CLUSTERMGMT_INVALID_INPUT.getStandardCode();
  }

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.BAD_REQUEST;
  }
}
