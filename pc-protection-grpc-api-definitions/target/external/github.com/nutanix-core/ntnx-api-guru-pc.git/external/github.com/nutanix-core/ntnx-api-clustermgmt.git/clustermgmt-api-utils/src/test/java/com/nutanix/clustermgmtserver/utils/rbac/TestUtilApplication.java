/*
 * Copyright (c) 2023 Nutanix Inc. All rights reserved.
 *
 * Author: rakesh.falak@nutanix.com
 */

package com.nutanix.clustermgmtserver.utils.rbac;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
  "com.nutanix.clustermgmtserver.utils"
},
  exclude = {
  })
public class TestUtilApplication {
}