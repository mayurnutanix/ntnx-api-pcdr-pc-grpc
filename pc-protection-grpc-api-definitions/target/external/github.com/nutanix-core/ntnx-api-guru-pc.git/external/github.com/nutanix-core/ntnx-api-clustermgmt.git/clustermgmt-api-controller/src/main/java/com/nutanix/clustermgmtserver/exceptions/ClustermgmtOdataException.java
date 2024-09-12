package com.nutanix.clustermgmtserver.exceptions;

import org.springframework.http.HttpStatus;

public class ClustermgmtOdataException extends ClustermgmtServiceException{
  public ClustermgmtOdataException(String message) {
    super(message);
  }

  @Override
  public int getStandardCode() {
    return ErrorCode.CLUSTERMGMT_ODATA_ERROR.getStandardCode();
  }
}
