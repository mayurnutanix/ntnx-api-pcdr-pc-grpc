/*
* Copyright (c) 2024 Nutanix Inc. All rights reserved.
*
* Author: @moli237
*
* This file has zookeeper related utility functions
 */

package utils

import (
	"fmt"
	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/external"
	"strings"

	zk "github.com/nutanix-core/go-cache/go-zookeeper"
	log "github.com/sirupsen/logrus"
)

func DeleteZkNode(path string) error {
	exist, stat, err := external.Interfaces().ZkSession().Exist(path, true)
	if err != nil {
		log.Errorf("error checking for zk node %s: %v", path, err)
		return err
	}
	if !exist {
		return consts.ErrorZkNodeNotExists
	}
	err = external.Interfaces().ZkSession().Delete(path, stat.Version, true)
	if err != nil {
		log.Errorf("error deleting zk node %s: %v", path, err)
		return err
	}
	return nil
}

// Updates a zknode if present. Creates it if not present.
func UpdateOrCreateZkNode(data []byte, path string, createRecursively bool) error {
	exist, stat, err := external.Interfaces().ZkSession().Exist(path, true)
	if err != nil {
		return fmt.Errorf("checking for zk node: %w", err)
	}
	if exist {
		log.Debugf("zk node %s already exists. updating content", path)
		_, err := external.Interfaces().ZkSession().Set(path, data, stat.Version, true)
		if err != nil {
			return fmt.Errorf("updating zk node: %w", err)
		}
		log.Debugf("successfully updated zk node %s", path)
	} else {
		log.Debugf("zk node %s doesnt exist. creating new node", path)
		if createRecursively {
			err := createZkNodeRecursive(data, path)
			if err != nil {
				return fmt.Errorf("creating zk node: %w", err)
			}
		} else {
			_, err := external.Interfaces().ZkSession().Create(path, data, 0, zk.WorldACL(zk.PermAll), true)
			if err != nil {
				return fmt.Errorf("creating zk node: %w", err)
			}
		}
		log.Debugf("successfully created zk node %s", path)
	}
	return nil
}

// Creates ZkNode at given path, recursively creating parent nodes if they don't exist.
func createZkNodeRecursive(data []byte, path string) error {
	_, err := external.Interfaces().ZkSession().Create(path, data, 0, zk.WorldACL(zk.PermAll), true)
	if err != nil && err != zk.ErrNoNode {
		return fmt.Errorf("creating zk node: %w", err)
	} else if err == nil {
		log.Debugf("successfully created zk node %s", path)
		return nil
	}

	zkPaths := strings.Split(path, "/")[1:]

	var currPath string
	for _, zkPath := range zkPaths {
		currPath += ("/" + zkPath)
		exist, _, err := external.Interfaces().ZkSession().Exist(currPath, true)
		if err != nil {
			return fmt.Errorf("checking for zk parent node: %w", err)
		}
		if exist {
			continue
		}
		if currPath == path {
			_, err := external.Interfaces().ZkSession().Create(path, data, 0, zk.WorldACL(zk.PermAll), true)
			if err != nil {
				return fmt.Errorf("creating final zk parent node: %w", err)
			}
		} else {
			// Create empty node
			_, err := external.Interfaces().ZkSession().Create(currPath, []byte{}, 0, zk.WorldACL(zk.PermAll), true)
			if err != nil {
				return fmt.Errorf("creating empty parent zk node %s: %w", currPath, err)
			}
		}
	}
	return nil
}
