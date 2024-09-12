/*
 * Copyright (c) 2023 Nutanix Inc. All rights reserved.
 * Author: harsh.seta@nutanix.com
 * Gflags for guru
 */

package consts

import (
	"flag"
	"time"
)

// /////////////////////////////////////////////////////////////////////////////
// Runtime configuration flags
// /////////////////////////////////////////////////////////////////////////////
var (
	// Ideally we should set LimitGomaxprocs to 2 - limiting Guru's cpu consumption to 2 cores.
	// 1 core - 1 system thread for all the routines and other is reserved in case a GC cycle is happening.
	// Currently keeping 0 and leaving an option to toggle it later if need be.
	GoMaxProcsValue = flag.Int("max-system-cores", 0, "Number of cores which Go runtime is allowed "+
		"to consume for CPU allocation purposes")
	EnableAccessLogging = flag.Bool("enable-access-logs", true, "Whether to enable access logging")
)

// /////////////////////////////////////////////////////////////////////////////
// Server configuration flags
// /////////////////////////////////////////////////////////////////////////////
var (
	NumWorkers           = flag.Int("num-workers", 5, "Number of worker go routine in guru")
	QueueSize            = flag.Int("queue-size", 1000, "Queue size of guru")
	ServerStartupTimeout = flag.Duration("server-startup-timeout", time.Second*15,
		"the duration for which the server gracefully wait for existing connections to finish - e.g. 15s or 1m")
	LogLevel = flag.String("log-level", "INFO", "Logging level for guru service. To enable debug "+
		"logs, set this value to DEBUG")
	IsRemoteConnectionWatchEnabled = flag.Bool("enable-rc-watch", true, "Gflag to enable or "+
		"disable remote connection watch")
	EnableDeleteRCWatch = flag.Bool("enable-delete-rc-watch", false, "Flag to enable watch"+
		"on RC entity for delete op to delete corresponding CES zknode")
	EnableBroadcastQueueFiltering = flag.Bool(
		"enable-broadcast-queue-filtering",
		false,
		"If enabled, the broadcast task queue will support ignoring similar and complimentary tasks ")
)

// /////////////////////////////////////////////////////////////////////////////
// Rest Client flags
// /////////////////////////////////////////////////////////////////////////////
var (
	ApiClientIntervalInitial = flag.Int("api-client-initial-backoff", 5, "Initial retry for API "+
		"client in Guru service in seconds.")
	ApiClientIntervalMax = flag.Int("api-client-max-backoff", 200, "Maximum time for the "+
		"retries for API client in Guru service in seconds.")
	ApiClientMaxRetries = flag.Int("api-client-max-retries", 5, "Maximum retries for API "+
		"client in Guru service.")
	ApiClientTimeout = flag.Int("api-client-timeout", 30, "The number of seconds after which "+
		"API client times out for a request.")
	ApiPollIntervalInitial = flag.Int("task-poll-initial-backoff", 2, "Polling time for initial "+
		"retry for API client in Guru service in seconds.")
	ApiPollIntervalMax = flag.Int("task-poll-max-backoff", 200, "Maximum polling time for "+
		"the retries for API client in Guru service in seconds.")
	ApiPollMaxRetries = flag.Int("task-poll-max-retries", 5, "Maximum retries for polling"+
		" for API client in Guru service.")
)

// /////////////////////////////////////////////////////////////////////////////
// IDF Client flags
// /////////////////////////////////////////////////////////////////////////////
var (
	IdfRetryIntervalInitial = flag.Int("idf-client-initial-backoff", 5, "Polling time for "+
		"initial retry for IDF client in Guru service in seconds.")
	IdfRetryIntervalMax = flag.Int("idf-client-max-backoff", 200, "Maximum polling "+
		"time for the retries for IDF client in Guru service in seconds.")
	IdfRetryMaxRetries = flag.Int("idf-client-max-retries", 5, "Maximum retries for "+
		"polling for IDF client in Guru service.")
	IdfWatcherRetryIntervalInitial = flag.Int("idf-watch-initial-backoff", 1, "Polling time for "+
		"initial retry for IDF watcher client in Guru service in seconds.")
	IdfWatcherRetryIntervalMax = flag.Int("idf-watch-max-backoff", 10, "Maximum polling time"+
		" for the retries for IDF watcher client in Guru service in seconds.")
	IdfWatcherRetryMaxRetries = flag.Int("idf-watch-max-retries", 3, "Maximum retries for "+
		"polling for IDF watcher client in Guru service.")
)

// /////////////////////////////////////////////////////////////////////////////
// Ergon Client flags
// /////////////////////////////////////////////////////////////////////////////
var (
	ErgonRetryIntervalInitial = flag.Int("ergon-client-initial-backoff", 2, "Polling time for "+
		"initial retry for Ergon client in Guru service in seconds.")
	ErgonRetryIntervalMax = flag.Int("ergon-client-max-backoff", 200, "Maximum polling time "+
		"for the retries for Ergon client in Guru service in seconds.")
	ErgonRetryMaxRetries = flag.Int("ergon-client-max-retries", 5, "Maximum retries for "+
		"polling for Ergon client in Guru service.")
	ErgonPollTimeout = flag.Uint64("ergon-poll-timeout", 300, "Polling timeout for Ergon "+
		"client in Guru service in seconds.")
	ErgonClientTimeoutSecs = flag.Int64("ergon-client-timeout", 60,
		"The number of seconds after which Ergon client times out for a request.")
	TasksApiVersion = flag.String("tasks-api-version", "v4.0.b1", "Version to be used for v4 tasks "+
		"endpoint hateOAS link.")
	VmmApiVersion = flag.String("vmm-api-version", "v4.0.b1", "Version to be used for v4 vmm "+
		"endpoint hateOAS link.")
	ClusterMgmtApiVersion = flag.String("cluster-mgmt-api-version", "v4.0.b1", "Version to be used"+
		" for v4 cluster-mgmt endpoint hateOAS link.")
	ConfigModuleVersion = flag.String("config-module-version", "v4.0.b1", "Version to be used for guru config module "+
		"endpoint hateOAS link.")
)

// /////////////////////////////////////////////////////////////////////////////
// Background Jobs flags
// /////////////////////////////////////////////////////////////////////////////
var (
	SyncDomainManagerWithIdfJobTimeInterval = flag.Duration("sync-domain-manager-with-idf-job-time-interval",
		1*time.Hour, "Time interval for syncing Domain Manager IDF entity. Expects value in duration string format.")
)

// /////////////////////////////////////////////////////////////////////////////
// Zookeeper Client flags
// /////////////////////////////////////////////////////////////////////////////
var (
	// ZkTimeout Keeping timeout same as default ZK session timeout.
	ZkTimeout = flag.Int("zk-client-timeout", 20, "Maximum retries for polling for Zookeeper client "+
		"in Guru service.")
)

// /////////////////////////////////////////////////////////////////////////////
// Miscellaneous
// /////////////////////////////////////////////////////////////////////////////
var (
	JwtExpriryInSeconds = flag.Int("jwt-expiry-time", 180, "JWT token expiry time in seconds.")
)

func InitFlags() {
	flag.Parse()
	flag.Set("logtostderr", "true")
}
