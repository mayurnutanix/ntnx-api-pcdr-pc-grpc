/*
 * Copyright (c) 2023 Nutanix Inc. All rights reserved.
 * Author: harsh.seta@nutanix.com
 */

package models

type RegisterAosRequestDetails struct {
	TaskUuid        []byte
	UseTrust        *bool
	Password        *string
	Username        *string
	IPAddressOrFQDN *string
	ContextID       *string
	ClusterUuid     *string
	ResourceExtId   *string
}
