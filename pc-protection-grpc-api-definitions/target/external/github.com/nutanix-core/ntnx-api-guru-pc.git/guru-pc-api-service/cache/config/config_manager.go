/*
* Copyright (c) 2024 Nutanix Inc. All rights reserved.
* Author: kshitij.kumar@nutanix.com
* This file contains initialisation of global cache which maintains state of zeus configuration.
 */

package config

import (
	"fmt"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/external"
	"sync"

	zeus_config "github.com/nutanix-core/go-cache/zeus/config"
	log "github.com/sirupsen/logrus"
)

type CacheIfc interface {
	GetZeusConfig() *zeus_config.ConfigurationProto
}

var (
	config CacheIfc
	lock   sync.Mutex
)

type configCache struct {
	config *zeus_config.ConfigurationProto
}

func GetConfigurationCache() (CacheIfc, error) {
	if config != nil {
		return config, nil
	}
	config, err := InitZeusConfigCache()
	if err != nil {
		log.Errorf("error while init zeus config: %s", err)
		return nil, err
	}
	return config, nil
}

// Used by the callback to update the global variable
func SetConfigurationCache(newProto *zeus_config.ConfigurationProto) {
	lock.Lock()
	defer lock.Unlock()
	configObj := &configCache{}
	configObj.config = newProto
	// set global variable
	config = configObj
}

// Returns a pointer to the cache object
// meant for read-only purpose
// the caller must not edit the object variables
func (c *configCache) GetZeusConfig() *zeus_config.ConfigurationProto {
	return c.config
}

func InitZeusConfigCache() (CacheIfc, error) {
	log.Info("Received trigger to initialise zeus config cache")
	if config != nil {
		return config, nil
	}
	maxRetries := 5
	for config == nil && maxRetries > 0 {
		func() {
			lock.Lock()
			defer lock.Unlock()

			if config != nil {
				return
			}

			zeusConfig, err := zeus_config.NewConfiguration(external.Interfaces().ZkSessionObject(), nil)
			if err != nil {
				log.Errorf("error fetching new zeus config: %s", err)
			}
			configObj := &configCache{}
			configObj.config = zeusConfig.ConfigProto
			// set global variable
			config = configObj
			log.Info("Initialized zeus config cache")
		}()
		maxRetries--
	}
	if config != nil {
		return config, nil
	}
	return nil, fmt.Errorf("error initializing new zeus config")
}
