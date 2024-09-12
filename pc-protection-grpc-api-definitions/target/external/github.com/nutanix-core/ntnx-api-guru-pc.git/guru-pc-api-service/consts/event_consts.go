/*
 * Copyright (c) 2023 Nutanix Inc. All rights reserved.
 * Author: harsh.seta@nutanix.com
 * Pulse event constants
 */

package consts

import "time"

var (
	EventNamespace                 = "guru"
	EventTypeRegisterAOS           = "register aos"
	EventTypeRegisterDomainManager = "register domain manager"
	EventTypeUnregister            = "unregister"
	DateTimeFormat                 = time.RFC3339
	PulseDomainManagerEntity       = "Multicluster"
)
