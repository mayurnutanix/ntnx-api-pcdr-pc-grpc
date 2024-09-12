package external

import "github.com/nutanix-core/go-cache/insights/insights_interface"

type IdfWatcher interface {
	Register() error
	NewEntityWatchInfo(
		guid *insights_interface.EntityGuid, name string, get_current_state bool,
		return_previous_entity_state bool, cb insights_interface.InsightsWatchCb,
		filterExpression *insights_interface.BooleanExpression,
		watchMetric string) *insights_interface.EntityWatchInfo
	CompositeWatchOnEntitiesOfType(
		watchInfo *insights_interface.EntityWatchInfo, createWatch bool, updateWatch bool,
		deleteWatch bool) ([]*insights_interface.Entity, error)
	Start() error
	Reregister() ([]*insights_interface.RegisterWatchResult, error)
}
