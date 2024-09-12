package scheduler

import (
	"fmt"
	"ntnx-api-guru-pc/guru-pc-api-service/background"
	"sync"
	"time"

	"github.com/go-co-op/gocron"
	log "github.com/sirupsen/logrus"
)

var (
	schedulerInstance     *CronScheduler
	schedulerInstanceOnce sync.Once
)

type CronScheduler struct {
	scheduler *gocron.Scheduler
}

// GetCronScheduler() returns the singleton instance of CronScheduler
func GetCronScheduler() *CronScheduler {
	schedulerInstanceOnce.Do(func() {
		if schedulerInstance == nil {
			schedulerInstance = &CronScheduler{
				scheduler: gocron.NewScheduler(time.UTC),
			}
		}
		schedulerInstance.scheduler.StartAsync()
	})
	return schedulerInstance
}

// Schedule() schedules a cron job
// args: job - CronJob to be scheduled
func (svc *CronScheduler) Schedule(job CronJob) error {
	_, err := svc.scheduler.Every(job.GetTimeInterval()).Do(job.Execute)
	if err != nil {
		return fmt.Errorf("creating cron schedule: %w", err)
	}
	return nil
}

// ScheduleAll() schedules all the background jobs
func (svc *CronScheduler) ScheduleAll() {
	// Schedule Sync Domain Manager With Idf job
	err := svc.Schedule(background.NewSyncDomainManagerWithIdfJob())
	if err != nil {
		log.Warnf("Failed to schedule Sync Domain Manager With Idf Job : %v", err)
	}
}
