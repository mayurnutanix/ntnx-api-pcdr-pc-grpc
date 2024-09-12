/*
 * Copyright (c) 2023 Nutanix Inc. All rights reserved.
 *
 * Author: ritik.nawal@nutanix.com
 */

package com.nutanix.clustermgmtserver.utils;

/*
Define attributes for injection of error in API flow.
 */

public class ErrorInjection {
  private static Boolean errorAfterRequestIdCreation;

  public static void setErrorAfterRequestIdCreation(Boolean errorAfterRequestIdCreation) {
    ErrorInjection.errorAfterRequestIdCreation = errorAfterRequestIdCreation;
  }

  public static Boolean getErrorAfterRequestIdCreation() {
    return errorAfterRequestIdCreation;
  }
}
