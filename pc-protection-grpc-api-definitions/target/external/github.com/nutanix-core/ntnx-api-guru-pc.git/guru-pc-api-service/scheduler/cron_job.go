package scheduler

import "time"

type CronJob interface{
	Execute()
	GetTimeInterval() time.Duration
}