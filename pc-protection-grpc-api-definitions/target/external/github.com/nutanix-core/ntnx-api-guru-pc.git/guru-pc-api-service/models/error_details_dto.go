/*
* Copyright (c) 2024 Nutanix Inc. All rights reserved.
*
* Author: harsh.seta@nutanix.com
 */

package models

type ErrorDetails struct {
	ErrorCode int32             `json:"ErrorCodes"`
	ErrorArgs map[string]string `json:"ErrorArgs"`
}
