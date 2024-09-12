package consts

import "github.com/nutanix-core/go-cache/ergon"

// TODO: verify this with PM since this is consumer facing.

// Task Messages
const (
	TrustSetupResetCompleteMessage              = "Successfully reverted the trust setup between the entities"
	DeleteCESCompleteMessage                    = "Successfully Deleted the cluster external state entity and failed the registration task"
	TrustSetupStartMessage                      = "Trust setup between the entities is in progress."
	TrustSetupCompleteMessage                   = "Successfully setup trust between the entities."
	DeleteZkMarkerCompleteMessage               = "Successfully deleted the zk marker node"
	RegistrationRollbackSuccessMessage          = "Successfully reverted the changes made by the Registration operation."
	RegistrationRecoveryFailureTaskAbortMessage = "Aborting the Registration task because it cannot be recovered due to missing credentials."
	RegistrationChildTaskCreatedMessage         = "Successfully created the child tasks for the Registration operation."
	RegistrationPrecheckCompleteMessage         = "Successfully executed cluster registration prechecks"
	RegistrationAOSPrecheckCompleteMessage      = "Successfully performed AOS prechecks."
	RegistrationPrecheckStartMessage            = "Registration precheck operation is in progress."
	RegistrationProxyStartMessage               = "Cluster registration operation is in progress."
	RegistrationProxyCompleteMessage            = "Successfully proxy performed cluster registration operation."
	RegistrationSuccessMessage                  = "Successfully performed Registraion between the entities."
)

var (
	TaskStatusNone                  = ergon.Task_Status(0)
	ErrorMessageArg                 = "error_message"
	OperationArg                    = "operation"
	RegistrationOperationArg        = "Register"
	UnregistrationOperationArg      = "Unregister"
	GetDomainManagerOperationArg    = "Get Domain Manager"
	CreateDomainManagerOperationArg = "Create Domain Manager"
	ConfigureConnectionArg          = "Configure Connection"
	RequestETagArg                  = "request_etag"
	CurrentETagArg                  = "current_etag"
	ArgumentArg                     = "argument"
	ArgumentKeyArg                  = "argument_key"
	ArgumentValueArg                = "argument_value"
	ResourceIdArg                   = "extID"
	RemoteClusterIdArg              = "clusterExtId"
)

const (
	// RegisterTaskQualifiedEntityType This has to follow V4 model convention -
	// <v4 namespace>:<v4 module>:{<v4 submodule if applicable>}:<v4 resource name>
	DomainManagerQualifiedEntityType = "prism:management:domain_manager"
	ClusterQualifiedEntityType       = "clustermgmt:config:cluster"
	DomainManagerEntityName          = "domain_manager"
	ClusterEntityName                = "cluster"
)

// Task weights for register aos sub tasks
const (
	RegisterAOSPrecheckTaskWeight = 33
	RegisterAOSCsrTaskWeight      = 33
	RegisterAOSProxyTaskWeight    = 33
)
