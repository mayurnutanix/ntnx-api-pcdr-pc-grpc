package config

import (
	"ntnx-api-guru-pc/guru-pc-api-service/test"
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestGetCache(t *testing.T){
	SetConfigurationCache(test.GetMockZeusProto())
	cache, err := GetConfigurationCache()
	assert.Nil(t, err)
	assert.NotNil(t, cache)
}

func TestGetZeus(t *testing.T){
	SetConfigurationCache(test.GetMockZeusProto())
	cache, _ := GetConfigurationCache()
	zeus := cache.GetZeusConfig()
	assert.NotNil(t, zeus)
}