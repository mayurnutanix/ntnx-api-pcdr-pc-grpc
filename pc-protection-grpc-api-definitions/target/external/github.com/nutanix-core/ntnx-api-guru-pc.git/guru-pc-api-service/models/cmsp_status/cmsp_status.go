package cmsp_status

type GetCmspStatusArg struct {
	Oid    string                 `json:".oid"`
	Kwargs map[string]interface{} `json:".kwargs"`
	Method string                 `json:".method"`
}

type GetCmspStatusResponseData struct {
	Value string `json:"value"`
}

type GetCmspStatusResponse struct {
	Return Return `json:".return"`
}

type Return struct {
	Config     Config `json:"config"`
	Status     string `json:"status"`
	EnableTask string `json:"enable_task"`
	Health     Health `json:"health"`
}

type Health struct {
	MessageList   []Message `json:"message"`
	OverallHealth bool      `json:"overall_health"`
	StatusCode    int       `json:"status_code"`
}

type Message struct {
	Health  bool   `json:"health"`
	Message string `json:"message"`
	Name    string `json:"name"`
}
type Config struct {
	DepMessage         string               `json:"DepMsg"`
	CmspConfig         CmspConfig           `json:"cmsp_config"`
	ClusterName        string               `json:"ClusterName"`
	ClusterUUID        string               `json:"ClusterUUID"`
	ClusterDescription string               `json:"ClusterDescription"`
	DepPercent         int                  `json:"DepPercent"`
	DepStatus          int                  `json:"DepStatus"`
	ClusterVersion     string               `json:"ClusterVersion"`
	PlatformVersion    string               `json:"PlatformVersion"`
	HostVersion        string               `json:"HostVersion"`
	MasterIPs          []string             `json:"MasterIPs"`
	EtcdIPs            []string             `json:"EtcdIPs"`
	WorkerIPs          []string             `json:"WorkerIPs"`
	EnvoyIPs           []string             `json:"EnvoyIPs"`
	ExternalMasterIP   string               `json:"ExternalMasterIP"`
	MspDNSIP           string               `json:"MspDNSIP"`
	LockDownStatus     bool                 `json:"LockDownStatus"`
	ControllerVersion  string               `json:"ControllerVersion"`
	Applications       []string             `json:"Applications"`
	Registry           StoragePluginWrapper `json:"Registry"`
	Etcd               StoragePluginWrapper `json:"Etcd"`
}

type StoragePluginWrapper struct {
	StoragePlugin string `json:"StoragePlugin"`
}

type CmspConfig struct {
	CmspArgs       string        `json:"cmspArgs"`
	PcDomain       string        `json:"pcDomain"`
	InfraNetwork   InfraNetwork  `json:"infraNetwork"`
	InfraIpBlock   []string      `json:"infraIpBlock"`
	Type           string        `json:"type"`
	PlatformClient string        `json:"platformClient"`
	RegistryImage  RegistryImage `json:"registryImage"`
}

type RegistryImage struct {
	Path   string `json:"path"`
	Source string `json:"source"`
}

type InfraNetwork struct {
	NetmaskIpv4Address string `json:"netmaskIpv4Address"`
	GatewayIpv4Address string `json:"gatewayIpv4Address"`
	VirtualNetworkUuid string `json:"virtualNetworkUuid"`
}
