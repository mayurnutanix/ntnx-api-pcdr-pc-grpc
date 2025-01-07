/*
 * Generated file ..
 *
 * Product version: 17.0.0-SNAPSHOT
 *
 * Part of the PC Protection PC Client SDK
 *
 * (c) 2025 Nutanix Inc.  All rights reserved
 *
 */

package com.nutanix.licensing.server.configuration;

import com.nutanix.api.utils.ratelimit.RateLimitConfigMapping;
import com.nutanix.api.utils.ratelimit.RateLimitOpConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class DomainManagerBackupsManagementRateLimiterConfiguration {


    /**
    * Rate limiter configuration for CREATE_BACKUP_TARGET1
    */
    @Bean(name="prism_management_createBackupTarget1")
    public RateLimitOpConfig createBackupTarget1RateLimitOpConfig() {
        RateLimitOpConfig rateLimitOpConfig = new RateLimitOpConfig();
        rateLimitOpConfig.setName("prism_management_createBackupTarget1");
        rateLimitOpConfig.setMethod("POST");
        rateLimitOpConfig.setPath("/prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets-new");
        Map<String, RateLimitConfigMapping> configMapping = new HashMap<>();

        RateLimitConfigMapping createBackupTarget1XSmallConfigMapping = new RateLimitConfigMapping();
        createBackupTarget1XSmallConfigMapping.setCount(3);
        createBackupTarget1XSmallConfigMapping.setDuration(1);
        configMapping.put("XSmall",  createBackupTarget1XSmallConfigMapping);

        RateLimitConfigMapping createBackupTarget1SmallConfigMapping = new RateLimitConfigMapping();
        createBackupTarget1SmallConfigMapping.setCount(3);
        createBackupTarget1SmallConfigMapping.setDuration(1);
        configMapping.put("Small",  createBackupTarget1SmallConfigMapping);

        RateLimitConfigMapping createBackupTarget1LargeConfigMapping = new RateLimitConfigMapping();
        createBackupTarget1LargeConfigMapping.setCount(3);
        createBackupTarget1LargeConfigMapping.setDuration(1);
        configMapping.put("Large",  createBackupTarget1LargeConfigMapping);

        RateLimitConfigMapping createBackupTarget1XLargeConfigMapping = new RateLimitConfigMapping();
        createBackupTarget1XLargeConfigMapping.setCount(3);
        createBackupTarget1XLargeConfigMapping.setDuration(1);
        configMapping.put("XLarge",  createBackupTarget1XLargeConfigMapping);

        rateLimitOpConfig.setSizeConfigMapping(configMapping);
        return rateLimitOpConfig;
    }
    /**
    * Rate limiter configuration for DELETE_BACKUP_TARGET_BY_ID1
    */
    @Bean(name="prism_management_deleteBackupTargetById1")
    public RateLimitOpConfig deleteBackupTargetById1RateLimitOpConfig() {
        RateLimitOpConfig rateLimitOpConfig = new RateLimitOpConfig();
        rateLimitOpConfig.setName("prism_management_deleteBackupTargetById1");
        rateLimitOpConfig.setMethod("DELETE");
        rateLimitOpConfig.setPath("/prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets-new/{extId}");
        Map<String, RateLimitConfigMapping> configMapping = new HashMap<>();

        RateLimitConfigMapping deleteBackupTargetById1XSmallConfigMapping = new RateLimitConfigMapping();
        deleteBackupTargetById1XSmallConfigMapping.setCount(3);
        deleteBackupTargetById1XSmallConfigMapping.setDuration(1);
        configMapping.put("XSmall",  deleteBackupTargetById1XSmallConfigMapping);

        RateLimitConfigMapping deleteBackupTargetById1SmallConfigMapping = new RateLimitConfigMapping();
        deleteBackupTargetById1SmallConfigMapping.setCount(3);
        deleteBackupTargetById1SmallConfigMapping.setDuration(1);
        configMapping.put("Small",  deleteBackupTargetById1SmallConfigMapping);

        RateLimitConfigMapping deleteBackupTargetById1LargeConfigMapping = new RateLimitConfigMapping();
        deleteBackupTargetById1LargeConfigMapping.setCount(3);
        deleteBackupTargetById1LargeConfigMapping.setDuration(1);
        configMapping.put("Large",  deleteBackupTargetById1LargeConfigMapping);

        RateLimitConfigMapping deleteBackupTargetById1XLargeConfigMapping = new RateLimitConfigMapping();
        deleteBackupTargetById1XLargeConfigMapping.setCount(3);
        deleteBackupTargetById1XLargeConfigMapping.setDuration(1);
        configMapping.put("XLarge",  deleteBackupTargetById1XLargeConfigMapping);

        rateLimitOpConfig.setSizeConfigMapping(configMapping);
        return rateLimitOpConfig;
    }
    /**
    * Rate limiter configuration for GET_BACKUP_TARGET_BY_ID1
    */
    @Bean(name="prism_management_getBackupTargetById1")
    public RateLimitOpConfig getBackupTargetById1RateLimitOpConfig() {
        RateLimitOpConfig rateLimitOpConfig = new RateLimitOpConfig();
        rateLimitOpConfig.setName("prism_management_getBackupTargetById1");
        rateLimitOpConfig.setMethod("GET");
        rateLimitOpConfig.setPath("/prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets-new/{extId}");
        Map<String, RateLimitConfigMapping> configMapping = new HashMap<>();

        RateLimitConfigMapping getBackupTargetById1XSmallConfigMapping = new RateLimitConfigMapping();
        getBackupTargetById1XSmallConfigMapping.setCount(5);
        getBackupTargetById1XSmallConfigMapping.setDuration(1);
        configMapping.put("XSmall",  getBackupTargetById1XSmallConfigMapping);

        RateLimitConfigMapping getBackupTargetById1SmallConfigMapping = new RateLimitConfigMapping();
        getBackupTargetById1SmallConfigMapping.setCount(5);
        getBackupTargetById1SmallConfigMapping.setDuration(1);
        configMapping.put("Small",  getBackupTargetById1SmallConfigMapping);

        RateLimitConfigMapping getBackupTargetById1LargeConfigMapping = new RateLimitConfigMapping();
        getBackupTargetById1LargeConfigMapping.setCount(5);
        getBackupTargetById1LargeConfigMapping.setDuration(1);
        configMapping.put("Large",  getBackupTargetById1LargeConfigMapping);

        RateLimitConfigMapping getBackupTargetById1XLargeConfigMapping = new RateLimitConfigMapping();
        getBackupTargetById1XLargeConfigMapping.setCount(5);
        getBackupTargetById1XLargeConfigMapping.setDuration(1);
        configMapping.put("XLarge",  getBackupTargetById1XLargeConfigMapping);

        rateLimitOpConfig.setSizeConfigMapping(configMapping);
        return rateLimitOpConfig;
    }
    /**
    * Rate limiter configuration for LIST_BACKUP_TARGETS1
    */
    @Bean(name="prism_management_listBackupTargets1")
    public RateLimitOpConfig listBackupTargets1RateLimitOpConfig() {
        RateLimitOpConfig rateLimitOpConfig = new RateLimitOpConfig();
        rateLimitOpConfig.setName("prism_management_listBackupTargets1");
        rateLimitOpConfig.setMethod("GET");
        rateLimitOpConfig.setPath("/prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets-new");
        Map<String, RateLimitConfigMapping> configMapping = new HashMap<>();

        RateLimitConfigMapping listBackupTargets1XSmallConfigMapping = new RateLimitConfigMapping();
        listBackupTargets1XSmallConfigMapping.setCount(5);
        listBackupTargets1XSmallConfigMapping.setDuration(1);
        configMapping.put("XSmall",  listBackupTargets1XSmallConfigMapping);

        RateLimitConfigMapping listBackupTargets1SmallConfigMapping = new RateLimitConfigMapping();
        listBackupTargets1SmallConfigMapping.setCount(5);
        listBackupTargets1SmallConfigMapping.setDuration(1);
        configMapping.put("Small",  listBackupTargets1SmallConfigMapping);

        RateLimitConfigMapping listBackupTargets1LargeConfigMapping = new RateLimitConfigMapping();
        listBackupTargets1LargeConfigMapping.setCount(5);
        listBackupTargets1LargeConfigMapping.setDuration(1);
        configMapping.put("Large",  listBackupTargets1LargeConfigMapping);

        RateLimitConfigMapping listBackupTargets1XLargeConfigMapping = new RateLimitConfigMapping();
        listBackupTargets1XLargeConfigMapping.setCount(5);
        listBackupTargets1XLargeConfigMapping.setDuration(1);
        configMapping.put("XLarge",  listBackupTargets1XLargeConfigMapping);

        rateLimitOpConfig.setSizeConfigMapping(configMapping);
        return rateLimitOpConfig;
    }
    /**
    * Rate limiter configuration for UPDATE_BACKUP_TARGET_BY_ID1
    */
    @Bean(name="prism_management_updateBackupTargetById1")
    public RateLimitOpConfig updateBackupTargetById1RateLimitOpConfig() {
        RateLimitOpConfig rateLimitOpConfig = new RateLimitOpConfig();
        rateLimitOpConfig.setName("prism_management_updateBackupTargetById1");
        rateLimitOpConfig.setMethod("PUT");
        rateLimitOpConfig.setPath("/prism/v4/management/domain-managers/{domainManagerExtId}/backup-targets-new/{extId}");
        Map<String, RateLimitConfigMapping> configMapping = new HashMap<>();

        RateLimitConfigMapping updateBackupTargetById1XSmallConfigMapping = new RateLimitConfigMapping();
        updateBackupTargetById1XSmallConfigMapping.setCount(3);
        updateBackupTargetById1XSmallConfigMapping.setDuration(1);
        configMapping.put("XSmall",  updateBackupTargetById1XSmallConfigMapping);

        RateLimitConfigMapping updateBackupTargetById1SmallConfigMapping = new RateLimitConfigMapping();
        updateBackupTargetById1SmallConfigMapping.setCount(3);
        updateBackupTargetById1SmallConfigMapping.setDuration(1);
        configMapping.put("Small",  updateBackupTargetById1SmallConfigMapping);

        RateLimitConfigMapping updateBackupTargetById1LargeConfigMapping = new RateLimitConfigMapping();
        updateBackupTargetById1LargeConfigMapping.setCount(3);
        updateBackupTargetById1LargeConfigMapping.setDuration(1);
        configMapping.put("Large",  updateBackupTargetById1LargeConfigMapping);

        RateLimitConfigMapping updateBackupTargetById1XLargeConfigMapping = new RateLimitConfigMapping();
        updateBackupTargetById1XLargeConfigMapping.setCount(3);
        updateBackupTargetById1XLargeConfigMapping.setDuration(1);
        configMapping.put("XLarge",  updateBackupTargetById1XLargeConfigMapping);

        rateLimitOpConfig.setSizeConfigMapping(configMapping);
        return rateLimitOpConfig;
    }
}

