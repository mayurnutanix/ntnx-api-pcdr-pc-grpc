/*
 * Copyright (c) 2024 Nutanix Inc. All rights reserved.
 * Author : kshitij.kumar@nutanix.com
 * Defines constants related to different type of Jobs and Operations that Guru supports.
 */

package consts

type OperationType int

const (
	REGISTER_PC OperationType = iota
	UNREGISTER_PC
	REGISTER_AOS
	CONFIGURE_CONNECTION
	UNCONFIGURE_CONNECTION
	ADD_ROOT_CERTIFICATE
	REMOVE_ROOT_CERTIFICATE
)

// Task Operations that will be recovered
var (
	GuruParentTasksList = []string{
		OperationTypeMap[REGISTER_AOS], OperationTypeMap[UNREGISTER_PC], OperationTypeMap[REGISTER_PC],
		OperationTypeMap[CONFIGURE_CONNECTION], OperationTypeMap[UNCONFIGURE_CONNECTION]}
	GuruTaskComponent = "Guru"
)

const (
	RegisterStepsCount            = 4
	UnregisterStepsCount          = 4
	ConfigureConnectionStepsCount = 2
	RegisterAOSStepsCount         = 4
	RegisterAOSRollbackStepsCount = -1
	AddRootCertStepsCount         = 1
	RemoveRootCertStepsCount      = 1
)

var (
	RegisterOpName              = "Register"
	RegisterPCOpName            = "Register Prism Central"
	UnregisterPCOpName          = "Unregister Prism Central"
	RegisterAosOpName           = "Register Prism Element"
	ConfigureConnectionOpName   = "Configure Connection"
	UnconfigureConnectionOpName = "Unconfigure Connection"
	AddCertificateOpName        = "Add Root Certificate"
	RemoveCertificateOpName     = "Remove Root Certificate"
	PCDeploymentOpName          = "Domain Manager (Prism Central) Deployment"
	GetDomainManagerOpName      = "GET Domain Manager (Prism Central)"
	ListDomainManagerOpName     = "LIST Domain Manager (Prism Central)"
)

var OperationStepsMap = map[OperationType]int64{
	REGISTER_PC:             RegisterStepsCount,
	UNREGISTER_PC:           UnregisterStepsCount,
	REGISTER_AOS:            RegisterAOSStepsCount,
	CONFIGURE_CONNECTION:    ConfigureConnectionStepsCount,
	UNCONFIGURE_CONNECTION:  ConfigureConnectionStepsCount,
	ADD_ROOT_CERTIFICATE:    AddRootCertStepsCount,
	REMOVE_ROOT_CERTIFICATE: RemoveRootCertStepsCount,
}

var OperationNameMap = map[OperationType]string{
	REGISTER_PC:             RegisterPCOpName,
	UNREGISTER_PC:           UnregisterPCOpName,
	REGISTER_AOS:            RegisterAosOpName,
	CONFIGURE_CONNECTION:    ConfigureConnectionOpName,
	UNCONFIGURE_CONNECTION:  UnconfigureConnectionOpName,
	ADD_ROOT_CERTIFICATE:    AddCertificateOpName,
	REMOVE_ROOT_CERTIFICATE: RemoveCertificateOpName,
}

var OperationTypeMap = map[OperationType]string{
	REGISTER_PC:             "RegisterPC",
	UNREGISTER_PC:           "UnregisterPC",
	REGISTER_AOS:            "RegisterAOS",
	CONFIGURE_CONNECTION:    "ConfigureConnection",
	UNCONFIGURE_CONNECTION:  "UnconfigureConnection",
	ADD_ROOT_CERTIFICATE:    "AddRootCertificate",
	REMOVE_ROOT_CERTIFICATE: "RemoveRootCertificate",
}
