package exchange_root_certificate

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestBroadcastCert(t *testing.T){
	lenBefore := GetCertificateBroadcastTaskQueue().GetQueueLength()
	BroadcastCert("123")
	lenAfter := GetCertificateBroadcastTaskQueue().GetQueueLength()
	assert.Equal(t, lenAfter, lenBefore+1)
}

