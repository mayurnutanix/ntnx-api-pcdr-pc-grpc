/*
 * Copyright (c) 2021 Nutanix Inc. All rights reserved.
 *
 * Author: rakesh.falak@nutanix.com
 */

package com.nutanix.clustermgmtserver.utils;

import com.nutanix.clustermgmtserver.exceptions.*;
import com.nutanix.util.base.ValidationException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Utility to handle the error response from the RPC Calls.
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RpcErrorResponseHandler {
  /**
   * Function to return the appropriate HTTP status code for the error response.
   * @param e The exception object.
   * @return HTTP status code for the response.
   */
  public static HttpStatus getHttpStatusFromException(final Exception e) {
    if (e instanceof ValidationException || e instanceof ClustermgmtInvalidInputException) {
      return HttpStatus.BAD_REQUEST;
    }else if (e instanceof ClustermgmtServiceException) {
      return ((ClustermgmtServiceException) e).getHttpStatus();
    } else if(e instanceof ClustermgmtGenericException){
      if(e.getCause()!=null && e.getCause() instanceof ClustermgmtServiceUnavailableException){
        return HttpStatus.SERVICE_UNAVAILABLE;
      }
      return HttpStatus.INTERNAL_SERVER_ERROR;
    }else {
      return HttpStatus.INTERNAL_SERVER_ERROR;
    }
  }
}
