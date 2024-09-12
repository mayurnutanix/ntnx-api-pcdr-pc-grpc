package exchange_root_certificate

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestBroadcastAllCert(t *testing.T){
	lenBefore := GetCertificateBroadcastTaskQueue().GetQueueLength()
	BroadcastAllCert()
	lenAfter := GetCertificateBroadcastTaskQueue().GetQueueLength()
	assert.Equal(t, lenAfter, lenBefore+1)
}

