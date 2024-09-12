package callbacks

// interface to perform some action on change of value of any object
// currently used to trigger callback workflows on change of cache objects
type OnChangeAction interface {
	Execute()
}