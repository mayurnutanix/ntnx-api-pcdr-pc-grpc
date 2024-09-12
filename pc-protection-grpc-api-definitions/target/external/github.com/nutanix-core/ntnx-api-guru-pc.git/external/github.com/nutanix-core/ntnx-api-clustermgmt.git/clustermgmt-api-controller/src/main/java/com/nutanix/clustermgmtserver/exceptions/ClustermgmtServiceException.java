/*
 * Copyright (c) 2021 Nutanix Inc. All rights reserved.
 *
 * Author: rakesh.falak@nutanix.com
 */

package com.nutanix.clustermgmtserver.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown to indicate that Cliustermgmt Service call failed.
 */
public class ClustermgmtServiceException extends Exception {

  private final ErrorCode errorCode;

  @Getter
  private final HttpStatus httpStatus;

  public ClustermgmtServiceException(final String message) {
    super(message);
    this.errorCode = ErrorCode.CLUSTERMGMT_SERVICE_RPC_ERROR;
    this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
  }

  public ClustermgmtServiceException(final String message, final ErrorCode errorCode, final HttpStatus httpStatus) {
    super(message);
    this.errorCode = errorCode;
    this.httpStatus = httpStatus;
  }

  /**
   * Returns a ClustermgmtNotFoundException with the given message.
   *
   * This factory method was kept for legacy reasons. It is not meant to be a model for other
   * exception classes.
   * @param message A message for the ClustermgmtNotFoundException.
   * @return A ClustermgmtNotFoundException instance.
   */
  public static ClustermgmtNotFoundException notFound(String message) {
    return new ClustermgmtNotFoundException(message);
  }

  /**
   * Returns the standard error code for this exception.
   * @return the standard error code for this exception.
   */
  public int getStandardCode() {
    return this.errorCode.getStandardCode();
  }

  /**
   * Returns the HttpStatus to use when instances of this exception reach controller layer.
   * @return Internal server error. Subclass may return other values.
   */
  public HttpStatus getHttpStatus() {
    return this.httpStatus;
  }

  public static ClustermgmtServiceException requestIdMissingError(final String message) {
    return new ClustermgmtServiceException(message, ErrorCode.CLUSTERMGMT_IDEMPOTENCY_ERROR, HttpStatus.BAD_REQUEST);
  }

  public static ClustermgmtServiceException etagMismatch(final String message) {
    return new ClustermgmtServiceException(message, ErrorCode.CLUSTERMGMT_ETAG_MISMATCH, HttpStatus.PRECONDITION_FAILED);
  }
}
