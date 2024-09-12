package grpc

import (
	"ntnx-api-guru-pc/guru-pc-api-service/scheduler"
	"ntnx-api-guru-pc/guru-pc-api-service/services/trust_setup/exchange_root_certificate"
	"ntnx-api-guru-pc/guru-pc-api-service/utils"
	init_utils "ntnx-api-guru-pc/guru-pc-api-service/utils/init"
)

// initialiseWorkflows() executes workflows which needs to be done before server
// is ready to serve requests
func (server *GrpcServerImpl) initialiseWorkflows() {

	// BrownField Case :
	// In v3 API remote connection is created during connecting PC,
	// In v4 Cluster external State would be created, hence when guru comes up,
	// it is necessary to ensure Cluster external state is created for all connected PC
	utils.MigrateAllRemoteConnectionToPcClusterExternalState()

	// Set watchers on IDF
	init_utils.RegisterAndStartIdfWatch()

	// In case of incomplete guru tasks, on guru start up these tasks are recovered
	init_utils.PopulateJobQueue(server.forwardQueue)

	// Save Domain Manager Entity in IDF
	init_utils.SaveDomainManagerEntityInIdf()

	// TODO: Decouple these init workflows from the grpc server
	// Initialize Broadcast task manager. Check if we can start this in an init method in the package.
	// Check if we need a sync.Once to ensure that only one worker reader is running.
	go exchange_root_certificate.GetCertificateBroadcastTaskQueue().ExecuteTasks()

	// Schedule background cron jobs
	scheduler := scheduler.GetCronScheduler()
	scheduler.ScheduleAll()
}
