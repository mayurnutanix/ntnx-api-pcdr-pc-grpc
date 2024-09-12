/*
 * Copyright (c) 2023 Nutanix Inc. All rights reserved.
 * Author: viraj.aute@nutanix.com
 * Universal constants
 */

package consts

import (
	"fmt"
	"time"

	utilmisc "github.com/nutanix-core/go-cache/util-go/misc"
)

// /////////////////////////////////////////////////////////////////////////////
// Service Ports
// /////////////////////////////////////////////////////////////////////////////
const (
	GuruPort    = 8422
	EnvoyPort   = 9440
	GenesisPort = 2100
)

// /////////////////////////////////////////////////////////////////////////////
// Log related constants
// /////////////////////////////////////////////////////////////////////////////
const (
	LogDirEnvVar           = "NUTANIX_LOG_DIR"
	AccessFileName         = "guru_access.log"
	LogFileName            = "guru.INFO"
	RegisterPCLoggerPrefix = "[REGISTER_PC]"
)

// /////////////////////////////////////////////////////////////////////////////
// Request response related constants
// /////////////////////////////////////////////////////////////////////////////
const (
	RelSelf                   = "self"
	RelLast                   = "last"
	RelFirst                  = "first"
	RelNodeExtId              = "nodeExtId"
	RelContainerExtId         = "containerExtId"
	UrlPrefix                 = "api/prism/v4.0.a1/management"
	RequestDetails            = "requestDetails"
	TaskUuids                 = "taskUuids"
	NtnxBatchTaskId           = "ntnx-batch-task-id"
	NtnxRequestId             = "ntnx-request-id"
	XRequestId                = "x-request-id"
	OperationPcAddNodeRequest = "kPrismCentralAddNodeRequest"
	XNtnxServiceToken         = "x-ntnx-service-token"
)

var (
	HasError      = "hasError"
	IsPaginated   = "isPaginated"
	IfMatchHeader = "if-match"
)

var (
	RemoteConnectionEntityType = "remote_connection"
)

const (
	DomainManagerCES = "/appliance/physical/connection/domain-manager/clusterexternalstate"
	PCEnvZkNodePath  = "/appliance/logical/pc_environment/config"
	AOSClusterCES    = "/appliance/physical/clusterexternalstate"
)

// /////////////////////////////////////////////////////////////////////////////
// URL constants
// /////////////////////////////////////////////////////////////////////////////
const (
	TasksUrl                   = "/api/prism/v4.0.b1/config/tasks"
	MulticlusterBaseUrl        = "/PrismGateway/services/rest/v1/multicluster"
	GroupsUrl                  = "/api/nutanix/v3/groups"
	GenesisSvcUrl              = "/PrismGateway/services/rest/v1/genesis"
	ApiVersion                 = "v4.0.b1"
	V3PrismCentralGetUrl       = "/api/nutanix/v3/prism_central"
	GetDomainManagerUrlSuffix  = "/prism/v4/config/domain-managers/%s"
	GetNodeExtIdUrlSuffix      = "/vmm/v4/ahv/config/vms/%s"
	GetContainerExtIdUrlSuffix = "/clustermgmt/v4/config/storage-containers/%s"
)

var (
	UpdateCertificateChainUrl = fmt.Sprintf("%v/certificate_chain", MulticlusterBaseUrl)
	ResetCertificateChainUrl  = fmt.Sprintf("%v/reset_certificate_chain", MulticlusterBaseUrl)
	GetCsrUrl                 = fmt.Sprintf("%v/certificate_signing_request", MulticlusterBaseUrl)
	ProxyRegistrationUrl      = fmt.Sprintf("%v/prism_central/register", MulticlusterBaseUrl)
	RegistrationPrecheckUrl   = fmt.Sprintf("%v/cluster_registration_pre_check", MulticlusterBaseUrl)
	ClusterExternalStateUrl   = fmt.Sprintf("%v/cluster_external_state", MulticlusterBaseUrl)
	V4ConfigureConnectionApiPath = "/api/prism/%s/management/domain-managers/%s/$actions/configure-connection"
)

// /////////////////////////////////////////////////////////////////////////////
// IDF constants
// /////////////////////////////////////////////////////////////////////////////
var (
	PCDomainEntityName         = "availability_zone_physical"
	RemoteConnectionUuidAzName = "credentials.remote_connection_uuid"
	CloudTrustUuidAzName       = "cloud_trust_uuid"
	RemoteConnectionEntityName = "remote_connection"
	CloudTrustEnityName        = "cloud_trust"
)

var (
	LocalHost   = "localhost"
	LocalHostIp = "127.0.0.1"
	HostAddr    string // will be initialised dynamically with PC IP during init.
)

var (
	IdfBackoffForRetries = utilmisc.NewExponentialBackoff(
		time.Duration(*IdfRetryIntervalInitial)*time.Second,
		time.Duration(*IdfRetryIntervalMax)*time.Second,
		*IdfRetryMaxRetries)
	IdfWatcherBackoffForRetries = utilmisc.NewExponentialBackoff(
		time.Duration(*IdfWatcherRetryIntervalInitial)*time.Second,
		time.Duration(*IdfWatcherRetryIntervalMax)*time.Second,
		*IdfWatcherRetryMaxRetries)
	ErgonBackoffForRetries = utilmisc.NewExponentialBackoff(
		time.Duration(*ErgonRetryIntervalInitial)*time.Second,
		time.Duration(*ErgonRetryIntervalMax)*time.Second,
		*ErgonRetryMaxRetries)
	GuruIdfWatcherClientName  = "GuruIDFWatcher"
	RemoteConnectionWatchName = "RemoteConnectionWatch"
)

var EmptyString = ""

// /////////////////////////////////////////////////////////////////////////////
// Cert related constants
// /////////////////////////////////////////////////////////////////////////////
const (
	// RootCertPath : Root certificate is stored at the following path on CVMs.
	RootCertPath = "/home/certs/root.crt"
	// IcaCertPath : The path to the intermediate CA public key.
	IcaCertPath = "/home/certs/ica.crt"
	// EntityCertPath : The path to the end entity public key.
	EntityCertPath = "/home/certs/GuruService/GuruService.crt"
	// EntityPrivateKey : The path to the end entity public key.
	EntityPrivateKey = "/home/certs/GuruService/GuruService.key"
)

// Regions for PC Domain
const (
	OnPremRegion = "OnPrem"
	NamePrefixPC = "PC_"
)

const (
	RootCert  = "root_cert"
	PcNameKey = "pc_name"
)

// JWT constants
const (
	ExpirationTime = "exp"
	Issuer         = "iss"
)

// These are the PCVM resource fields that are queried from the hosting cluster VM table
var (
	VmEntityType             = "vm"
	VmAttributeMemory        = "memory_size_bytes"
	VmAttributeDiskCapacity  = "capacity_bytes"
	VmAttributeVcpus         = "num_vcpus"
	VmAttributeName          = "vm_name"
	VmAttributeContainerUuid = "container_uuids"
	VmGroupsQueryName        = "guru:domainManagerContainerId"
)

// These are the PCVM resource fields that are queried from the hosting cluster Disk table
var (
	DiskEntityType            = "virtual_disk"
	DiskAttributeDiskCapacity = "capacity_bytes"
	DiskGroupsQueryName        = "guru:domainManagerDiskSize"
)

// /////////////////////////////////////////////////////////////////////////////
// Logger constants
// /////////////////////////////////////////////////////////////////////////////
const (
	RecoveryLoggerPrefix         = "[RECOVERY]"
	GetDomainManagerLoggerPrefix = "[GET_DOMAIN_MANAGER]"
	GuruTraceName                = "guru-pc"
)

// /////////////////////////////////////////////////////////////////////////////
// Version Mapping constants
// /////////////////////////////////////////////////////////////////////////////
const (
	MinGuruSupportedPCVersion = "2024.3"
	VersionMapFilePath        = "/home/nutanix/config/versions/version_mapping.json"
)

// /////////////////////////////////////////////////////////////////////////////
// Truncation constants
// /////////////////////////////////////////////////////////////////////////////
const (
	DomainManagerListMaxSize  = 1
	DomainManagerListMaxDepth = 1
)

const (
	GuruServiceName = "GuruService"
)
