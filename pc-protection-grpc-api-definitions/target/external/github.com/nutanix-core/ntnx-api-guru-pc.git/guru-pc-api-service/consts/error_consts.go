/*
 * Copyright (c) 2023 Nutanix Inc. All rights reserved.
 * Author: harsh.seta@nutanix.com
 * Error constants
 */

package consts

import "errors"

// Guru error codes
var (
	ErrorCodeInternalError                = 55000
	ErrorCodeRequestArgumentNotFound      = 55100
	ErrorCodeRequestArgumentInvalidFormat = 55101
	ErrorCodeInvalidArgument              = 55102
	ErrorCodeMissingETag                  = 55103
	ErrorCodeInvalidETag                  = 55104
	ErrorCodeInvalidRemoteEntity          = 55105
	ErrorCodeETagNotMatched               = 55200
	ErrorCodePreconditionFailed           = 55201
	ErrorCodeUnsupported                  = 55300
	ErrorCodeServiceUnavailable           = 55400
	ErrorCodeUnauthorizedRequest          = 55500
	ErrorCodeForbiddenRequest             = 55600
	ErrorCodeGatewayTimeout               = 55700
	ErrorCodeNotSupportedOnRemote         = 55800
	ErrorCodeResourceNotFound             = 55801
)

var (
	PrismErrorNamespace = "prism"
	PrismErrorPrefix    = "PRI"
	EnglishLocale       = "en_US"
)

// Log errors
const (
	ErrorCertSigningFailure          = "Error during certificate signing"
	ErrorRegistrationPrecheckFailure = "Error during registration prechecks"
)

// API errors
var (
	ErrorApiKeyFileMissing  = errors.New("missing client key")
	ErrorApiCertFileMissing = errors.New("missing client certificate")
)

// ZK errors
var (
	ErrorZkPathInvalid   = errors.New("zk: path is invalid")
	ErrorZkNoData        = errors.New("zk: data required for creating zk node")
	ErrorZkInvalidPaths  = errors.New("zk: parent path cannot be greater than node path")
	ErrorZkMisMatchPaths = errors.New("zk: parent path diverging from node path")
	ErrorZkNodeNotExists = errors.New("zk: node does not exists")
)

// IDF errors
var (
	ErrorIdfEntityExists    = errors.New("idf: entity already exists")
	ErrorIdfEntityNotExists = errors.New("idf: entity does not exists")
	ErrorIdfEntityIdNil     = errors.New("idf: entity id is required")
	ErrorIdfEntityTypeNil   = errors.New("idf: entity type is required")
	ErrorIdfInvalidArg      = errors.New("idf: argument provided is invalid")

	ErrorIdfRemoteConnectionGet    = errors.New("idf: Failed to fetch remote connection entity from IDF")
	ErrorRemoteClusterUuidNotFound = errors.New("idf: Failed to fetch remote cluster uuid from remote connection")
	ErrorIdfEntityNotFound         = errors.New("NotFound: 18")
)

// Ergon errors
var (
	ErrorErgonGetTask              = errors.New("ergon: task get error")
	ErrorErgonCreateTask           = errors.New("ergon: task creation error")
	ErrorErgonUnexpectedType       = errors.New("ergon: unexpected type")
	ErrorErgonTimeout              = errors.New("ergon: timeout while polling ")
	ErrorErgonTaskFailed           = errors.New("ergon: ergon task failed")
	ErrorErgonUpdateInternalOpaque = errors.New("ergon: Failed to update internal opaque of task")
)

// Cluster State Error
var (
	ErrorClusterUuidNil                    = errors.New("CES: cluster uuid is required")
	ErrorClusterUuidInvalid                = errors.New("CES: cluster uuid is invalid")
	ErrorClusterRegistrationRoleNil        = errors.New("CES: registration role is required")
	ErrorClusterRegistrationRoleInvalid    = errors.New("CES: registration role is invalid")
	ErrorClusterConfigDetailsNil           = errors.New("CES: config details are required")
	ErrorClusterConfigDetailsEmpty         = errors.New("CES: external ip or fqdn is required")
	ErrorClusterExternalIpInvalid          = errors.New("CES: external ip is invalid")
	ErrorClusterFqdnInvalid                = errors.New("CES: fqdn is invalid")
	ErrorClusterNotExists                  = errors.New("CES: cluster external state does not exists")
	ErrorClusterUuidMisMatch               = errors.New("CES: cluster uuid in path and request body does not match")
	ErrorClusterExternalStateDelete        = errors.New("CES: cluster external state delete failed")
	ErrorClusterExternalStateCreate        = errors.New("CES: cluster external state create failed")
	ErrorClusterExternalStateAlreadyExists = errors.New("CES: cluster external state already exists")
)

// PC Domain error
var (
	ErrorPCDomainDelete = errors.New("pc-domain: failed to delete PC domain")
)

// Master Orchestrator errors
var (
	ErrorPollTimeout       = errors.New("connection: timeout while polling ")
	ErrorPollError         = errors.New("connection: error in poll api call ")
	ErrorPollCompletedInfo = errors.New("connection: error in fetching completed task info")
)

// Registration errors
var (
	ERROR_REGISTRATION_IN_PROGRESS_MESSAGE = "Ongoing registration already in progress"
	ERROR_NULL_REMOTE_CREDENTIALS          = "Remote credentials can not be null"
	ERROR_REGISTRATION_INITIATION_MESSAGE  = "Failed to initiate registration on domain manager"
	ERROR_FETCH_IP_FROM_TRUST_ZK_NODE      = "Failure while fetching Ip address from trust zk node with error"
	ErrorInvalidRemoteEntitySpecified      = "Invalid remote config specified"
	ErrorTaskRecoveryFailure               = "Unable to recover task due to invalid task state"

	ErrorGenericServer                    = errors.New("encountered issue on server while processing the request")
	ErrorNullRemoteClusterDetails         = errors.New("Remote cluster details can not be null")
	ErrorInvalidClusterUuid               = errors.New("Remote cluster-uuid provided is invalid")
	ErrorTrustZkNodeFailure               = errors.New("Could not fetch the Ip address for the cluster uuid provided")
	ErrorTrustZkNodeNotFoundFailure       = errors.New("Failed to fetch the trust zk node for the cluster uuid provided")
	ErrorTrustZkNodeParseFailure          = errors.New("Failure while parsing the trust zk node for the cluster uuid provided")
	ErrorTrustZkNodeIncompleteDataFailure = errors.New("Ip address field not found while parsing the trust zk node the cluster uuid provided")
	ErrorInvalidTrustZkNode               = errors.New("Invalid trust zk config")
	ErrorCreateChildTasks                 = errors.New("Failure during registration child task creation")
	ErrorRegistrationTaskExecution        = errors.New("Failure during registration task execution")
	ErrorRegistrationChildTaskExecution   = errors.New("Failure during registration child task execution")
	ErrorCreateRegistrationPrecheckTask   = errors.New("Failure to create registration precheck task")
	ErrorCreateCsrTask                    = errors.New("Failed to create certificate signing request task")
	ErrorCreateClusterRegistrationTask    = errors.New("Failed to create cluster registration task")

	ErrorRegistrationPrecheckTaskExecution = errors.New("Failure during AOS registration precheck task execution")
	ErrorCsrTaskExecution                  = errors.New("Failure during certificate signing request task execution")
	ErrorClusterRegistrationTask           = errors.New("Failure during AOS cluster registration task execution")
	ErrorRegistrationMarkerNode            = errors.New("Failed to create register AOS cluster zk marker node")

	ErrorClusterIpNil = errors.New("Prism element cluster Ip is nil")

	ErrorRegistrationPeTaskPoll = errors.New("PE register task polling failed")
	ErrorRegistrationPeTrigger  = errors.New("PE register task failed")

	ErrorContextID           = errors.New("Failure during context ID creation")
	ErrorRollbackRecoverTask = errors.New("Recovered task cannot be completed as credentials lost")
)

// Registration Precheck errors
var (
	ErrorApiClientNil     = errors.New("Api client is nil ")
	ErrorErgonClientNil   = errors.New("Ergon client is nil ")
	ErrorZkClientNil      = errors.New("Zk client is nil ")
	ErrorGenesisClientNil = errors.New("Genesis client is nil ")
	ErrorTaskUuidInvalid  = errors.New("Task UUID is nil or invalid")

	ErrorPEClusterUuidNil    = errors.New("Prism element cluster uuid is nil")
	ErrorPEClusterVersionNil = errors.New("Prism element cluster version is nil")
	ErrorPCClusterUuidNil    = errors.New("Domain manager cluster uuid is nil")
	ErrorPCClusterVersionNil = errors.New("Domain manager cluster version is nil")

	ErrorRegistrationPEPrecheck = errors.New("Prism element precheck failed")
	ErrorRegistrationPCPrecheck = errors.New("Domain manager precheck failed")
	ErrorRegistrationPrecheck   = errors.New("Registration precheck failed")
	ErrorFetchPCDetails         = errors.New("Failed to fetch domain manager detail")

	ErrorPEIsUpgrading                = errors.New("Upgrade is in progress on the remote cluster. Please wait for it to finish and retry the operation")
	ErrorPEIsBlacklisted              = errors.New("The remote cluster is blacklisted for registration")
	ErrorPEIsPcDeploymentInProgress   = errors.New("A deployment is in progress on the remote cluster. Please wait for it to finish and retry the operation")
	ErrorPEIsUnregistrationInProgress = errors.New("Unregistration is in progress on the remote entity. Please wait for it to finish and retry the operation")
	ErrorPEIsRegistrationInProgress   = errors.New("Another registration is in progress on the remote cluster. Please wait for it to finish and retry the operation")
	ErrorPEIsRegistered               = errors.New("Remote cluster is already registered with a Prism Central")

	ErrorPCIsUpgrading                = errors.New("Upgrade is in progress on this Prism Central. Please wait for it to finish and retry the operation")
	ErrorPCIsUnregistrationInProgress = errors.New("Unregistration is in progress on the Prism Central. Please wait for it to finish and retry the operation")
	ErrorPCIsBlacklisted              = errors.New("The remote cluster is blacklisted for registration")
	ErrorPCAlreadyRegistered          = errors.New("This remote cluster is already registered to the Prism Central")
	ErrorPCIsScaleoutInProgress       = errors.New("Scaleout is in progress on this Prism Central. Please wait for it to finish and retry the operation")

	ErrorPCCheckRegistrationInProgress = errors.New("Failed to check if registration in progress on Prism Central")
	ErrorPCCheckRegistered             = errors.New("Failed to check if already registered to the prism element")
	ErrorPCCheckScaleoutInProgress     = errors.New("Failed to check if scale out in progress on Prism Central")
	ErrorInterfacesNil                 = errors.New("Interfaces not initialised ")

	ErrorPCRegistrationInProgress   = "found exisiting PC-PC Registration tasks"
	ErrorPCRegisterCloudPCInitiator = "initiator can't be Cloud PC"
	ErrorPCRegisterXiPCInitiator    = "initiator can't be Xi PC"
	ErrorPCSelfRegisterCheck        = "found remote cluster uuid same as local cluster uuid"
)

var (
	ErrorInvalidCaChain = errors.New("Invalid ca chain received")
)

// Execute Registration  errors
var (
	ErrorTriggerRegistrationOnPE = errors.New("Failed to trigger registration on PE")
	ErrorClusterRegistrationOnPE = errors.New("Failure during cluster registration on PE")
	ErrorDeleteZkMarkerNode      = errors.New("Failed to delete registration zk marker node")
)

// Zeus Configuration errors
var (
	ErrorZeusClusterUuidNil = errors.New("Cluster uuid is nil in zeus configuration")
	ErrorZeusVersionNil     = errors.New("Cluster version is nil in zeus configuration")
	ErrorFetchZeusConfig    = errors.New("failed to fetch zeus configuration")
	ErrorInvalidZeusConfig  = errors.New("invalid zeus configuration")
)

// Cert Util Errors
var (
	ErrorCertDecode            = errors.New("cert: can not decode certificate")
	ErrorJwtExpired            = errors.New("cert: JWT is expired")
	ErrorJWTValidation         = errors.New("cert: JWT validation failed")
	ErrorCreatingJWT           = errors.New("cert: can not create JWT")
	ErrorCertMismatch          = errors.New("cert: root cert mismatch")
	ErrorBadChainLen           = errors.New("cert: unexpected length of cert chain")
	ErrorLeafCertDecode        = errors.New("cert: can not decode leaf certificate")
	ErrorPemCertDecode         = errors.New("cert: can not decode PEM certificate")
	ErrorPrivateKeyDecode      = errors.New("cert: can not decode private key")
	ErrorCertChainVerification = errors.New("cert: can not verify certificate chain")
	ErrorIssuerNotFound        = errors.New("cert: issuer not available in JWT")
	ErrorCertAddError          = errors.New("cert: failed to save peer root certificate")
	ErrorCertDeleteError       = errors.New("cert: failed to delete peer root certificate")
	ErrorGenesisRetry          = errors.New("cert: genesis retry error")
)

var (
	RegisterClusterStateErrors = []string{
		"Error while creating the child tasks",
		"Error while executing registration pre-checks",
		"Error while executing certificate signing request",
		"Error while executing registration proxy",
	}
)

var (
	ErrorStartIDFWatcher                  = errors.New("Failed to start idf watcher")
	ErrorActionNotSupportedOnPrismGateway = errors.New("This action requires one or more features that are not enabled or available on this cluster")
	ErrorInvalidGroupsResponse            = errors.New("Invalid response received from the groups call.")
	ErrorInvalidUhuraResponse             = errors.New("Invalid response received from the Uhura rpc.")
)

// Error messages in case of 4XX / 5XX
var (
	ErrorUnauthorizedAccess        = errors.New("Unauthorized access. Please check the credentials")
	ErrorForbiddenAccess           = errors.New("Forbidden access. Please check the permissions")
	ErrorOperationNotSupported     = errors.New("This operation is not supported on the remote cluster")
	ErrorInternalServerError       = errors.New("Internal server error. Please retry later")
	ErrorRemoteClusterNotAvailable = errors.New("The remote cluster is unavailable. Please retry later")
	ErrorRemoteClusterNotReachable = errors.New("Please check the connectivity with remote cluster and retry")
)

var (
	InternalServerErrorErr             = "Internal server error. Please retry later"
	InvalidResourceSpecifiedErr        = "Resource Id specified with request is invalid"
	GrpcContextNoMetadataErr           = "No metadata found in the context"
	GrpcContextNoNtnxRequestIdErr      = "Required field NTNX-Request-Id not found in the request"
	GrpcContextInvalidNtnxRequestIdErr = "Required field NTNX-Request-Id in the request is not a valid UUID"
	GrpcContextNoRequestIdErr          = "Required field X-Request-Id not found in the batch request"
	InvalidRemoteCredentialsErr        = "Invalid remote credentials"
	CreateErgonTaskErr                 = "Failure during task creation"
	UpgradeInProgressErr               = "Cluster Upgrade is in progress. Try again once the upgrade finishes"
	ScaleOutInProgressErr              = "PC Scaleout is in progress. Try again once the scaleout finishes."
)

// Get Domain manager API errors
var (
	ErrorInvalidResourceExtId = errors.New("invalid resource extId")
	ErrNilHostingClusterExtId = errors.New("hostingClusterExtId is empty")
	ErrNilConfigDetails       = errors.New("config details are missing")
)
