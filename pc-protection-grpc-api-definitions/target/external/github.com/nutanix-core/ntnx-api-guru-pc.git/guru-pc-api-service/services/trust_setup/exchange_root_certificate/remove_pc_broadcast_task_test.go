package exchange_root_certificate

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestBroadcastCertRemove(t *testing.T){
	lenBefore := GetCertificateBroadcastTaskQueue().GetQueueLength()
	BroadcastCertRemove("123")
	lenAfter := GetCertificateBroadcastTaskQueue().GetQueueLength()
	assert.Equal(t, lenAfter, lenBefore+1)
}

