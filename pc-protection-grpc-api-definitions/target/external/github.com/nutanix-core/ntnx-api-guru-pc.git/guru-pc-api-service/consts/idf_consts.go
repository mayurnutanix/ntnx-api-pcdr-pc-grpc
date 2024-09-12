package consts

const (
	IdfGetOperation    = "GetEntities"
	IdfUpdateOperation = "UpdateEntity"
	IdfDeleteOperation = "DeleteEntity"
)

// Attributes of Domain Manager Idf schema
const (
	DomainManagerNodesInfo           = "nodes_info"
	DomainManagerExtID               = "ext_id"
	DomainManagerName                = "name"
	DomainManagerLockdownMode        = "should_enable_lockdown_mode"
	DomainManagerNodesCount          = "nodes_count"
	DomainManagerVersion             = "version"
	DomainManagerFlavour             = "flavour"
	DomainManagerEnvironmentType     = "environment_type"
	DomainManagerProviderType        = "provider_type"
	DomainManagerInstanceType        = "instance_type"
	DomainManagerIsPulseEnabled      = "is_pulse_enabled"
	DomainManagerHostingClusterExtID = "hosting_cluster_ext_id"
	DomainManagerNumVcpus            = "num_vcpus"
	DomainManagerMemorySizeBytes     = "memory_size_bytes"
	DomainManagerDiskCapacityBytes   = "data_disk_size_bytes"
)

// CMSP resource attributes
const (
	CMSPDomainName              = "cmsp_domain_name"
	CMSPNetworkSubnetMask       = "cmsp_network_subnet_mask"
	CMSPNetworkDefaultGatewayIp = "cmsp_network_default_gateway_ip"
	CMSPNetworkType             = "cmsp_network_type"
	CMSPPlatformIpBlockList     = "cmsp_platform_ip_block_list"
	CMSPVirtualNetworkUuid      = "cmsp_virtual_network_uuid"
)
