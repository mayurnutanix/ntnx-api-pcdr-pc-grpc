/*
* Copyright (c) 2024 Nutanix Inc. All rights reserved.
*
* Author: harsh.seta@nutanix.com
*
 */

package models

import (
	domainManagerConfig "ntnx-api-guru-pc/generated-code/target/protobuf/prism/v4/config"
)

type DomainManagerResponse struct {
	Entity *domainManagerConfig.DomainManager
	Etag   *DomainManagerETag
}
