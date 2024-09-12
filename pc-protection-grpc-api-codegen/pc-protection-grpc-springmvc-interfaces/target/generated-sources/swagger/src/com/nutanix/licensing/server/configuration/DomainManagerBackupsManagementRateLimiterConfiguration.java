/*
 * Generated file ..
 *
 * Product version: 0.0.1-SNAPSHOT
 *
 * Part of the PC Protection PC Client SDK
 *
 * (c) 2024 Nutanix Inc.  All rights reserved
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
    * Rate limiter configuration for CREATE_BACKUP_TARGET
    */
    @Bean(name="prism_management_createBackupTarget")
    public RateLimitOpConfig createBackupTargetRateLimitOpConfig() {
        RateLimitOpConfig rateLimitOpConfig = new RateLimitOpConfig();
        rateLimitOpConfig.setName("prism_management_createBackupTarget");
        Map<String, RateLimitConfigMapping> configMapping = new HashMap<>();

        RateLimitConfigMapping createBackupTargetXSmallConfigMapping = new RateLimitConfigMapping();
        createBackupTargetXSmallConfigMapping.setCount(3);
        createBackupTargetXSmallConfigMapping.setDuration(1);
        configMapping.put("XSmall",  createBackupTargetXSmallConfigMapping);

        RateLimitConfigMapping createBackupTargetSmallConfigMapping = new RateLimitConfigMapping();
        createBackupTargetSmallConfigMapping.setCount(3);
        createBackupTargetSmallConfigMapping.setDuration(1);
        configMapping.put("Small",  createBackupTargetSmallConfigMapping);

        RateLimitConfigMapping createBackupTargetLargeConfigMapping = new RateLimitConfigMapping();
        createBackupTargetLargeConfigMapping.setCount(3);
        createBackupTargetLargeConfigMapping.setDuration(1);
        configMapping.put("Large",  createBackupTargetLargeConfigMapping);

        RateLimitConfigMapping createBackupTargetXLargeConfigMapping = new RateLimitConfigMapping();
        createBackupTargetXLargeConfigMapping.setCount(3);
        createBackupTargetXLargeConfigMapping.setDuration(1);
        configMapping.put("XLarge",  createBackupTargetXLargeConfigMapping);

        rateLimitOpConfig.setSizeConfigMapping(configMapping);
        return rateLimitOpConfig;
    }
    /**
    * Rate limiter configuration for DELETE_BACKUP_TARGET_BY_ID
    */
    @Bean(name="prism_management_deleteBackupTargetById")
    public RateLimitOpConfig deleteBackupTargetByIdRateLimitOpConfig() {
        RateLimitOpConfig rateLimitOpConfig = new RateLimitOpConfig();
        rateLimitOpConfig.setName("prism_management_deleteBackupTargetById");
        Map<String, RateLimitConfigMapping> configMapping = new HashMap<>();

        RateLimitConfigMapping deleteBackupTargetByIdXSmallConfigMapping = new RateLimitConfigMapping();
        deleteBackupTargetByIdXSmallConfigMapping.setCount(3);
        deleteBackupTargetByIdXSmallConfigMapping.setDuration(1);
        configMapping.put("XSmall",  deleteBackupTargetByIdXSmallConfigMapping);

        RateLimitConfigMapping deleteBackupTargetByIdSmallConfigMapping = new RateLimitConfigMapping();
        deleteBackupTargetByIdSmallConfigMapping.setCount(3);
        deleteBackupTargetByIdSmallConfigMapping.setDuration(1);
        configMapping.put("Small",  deleteBackupTargetByIdSmallConfigMapping);

        RateLimitConfigMapping deleteBackupTargetByIdLargeConfigMapping = new RateLimitConfigMapping();
        deleteBackupTargetByIdLargeConfigMapping.setCount(3);
        deleteBackupTargetByIdLargeConfigMapping.setDuration(1);
        configMapping.put("Large",  deleteBackupTargetByIdLargeConfigMapping);

        RateLimitConfigMapping deleteBackupTargetByIdXLargeConfigMapping = new RateLimitConfigMapping();
        deleteBackupTargetByIdXLargeConfigMapping.setCount(3);
        deleteBackupTargetByIdXLargeConfigMapping.setDuration(1);
        configMapping.put("XLarge",  deleteBackupTargetByIdXLargeConfigMapping);

        rateLimitOpConfig.setSizeConfigMapping(configMapping);
        return rateLimitOpConfig;
    }
    /**
    * Rate limiter configuration for GET_BACKUP_TARGET_BY_ID
    */
    @Bean(name="prism_management_getBackupTargetById")
    public RateLimitOpConfig getBackupTargetByIdRateLimitOpConfig() {
        RateLimitOpConfig rateLimitOpConfig = new RateLimitOpConfig();
        rateLimitOpConfig.setName("prism_management_getBackupTargetById");
        Map<String, RateLimitConfigMapping> configMapping = new HashMap<>();

        RateLimitConfigMapping getBackupTargetByIdXSmallConfigMapping = new RateLimitConfigMapping();
        getBackupTargetByIdXSmallConfigMapping.setCount(5);
        getBackupTargetByIdXSmallConfigMapping.setDuration(1);
        configMapping.put("XSmall",  getBackupTargetByIdXSmallConfigMapping);

        RateLimitConfigMapping getBackupTargetByIdSmallConfigMapping = new RateLimitConfigMapping();
        getBackupTargetByIdSmallConfigMapping.setCount(5);
        getBackupTargetByIdSmallConfigMapping.setDuration(1);
        configMapping.put("Small",  getBackupTargetByIdSmallConfigMapping);

        RateLimitConfigMapping getBackupTargetByIdLargeConfigMapping = new RateLimitConfigMapping();
        getBackupTargetByIdLargeConfigMapping.setCount(5);
        getBackupTargetByIdLargeConfigMapping.setDuration(1);
        configMapping.put("Large",  getBackupTargetByIdLargeConfigMapping);

        RateLimitConfigMapping getBackupTargetByIdXLargeConfigMapping = new RateLimitConfigMapping();
        getBackupTargetByIdXLargeConfigMapping.setCount(5);
        getBackupTargetByIdXLargeConfigMapping.setDuration(1);
        configMapping.put("XLarge",  getBackupTargetByIdXLargeConfigMapping);

        rateLimitOpConfig.setSizeConfigMapping(configMapping);
        return rateLimitOpConfig;
    }
    /**
    * Rate limiter configuration for LIST_BACKUP_TARGETS
    */
    @Bean(name="prism_management_listBackupTargets")
    public RateLimitOpConfig listBackupTargetsRateLimitOpConfig() {
        RateLimitOpConfig rateLimitOpConfig = new RateLimitOpConfig();
        rateLimitOpConfig.setName("prism_management_listBackupTargets");
        Map<String, RateLimitConfigMapping> configMapping = new HashMap<>();

        RateLimitConfigMapping listBackupTargetsXSmallConfigMapping = new RateLimitConfigMapping();
        listBackupTargetsXSmallConfigMapping.setCount(5);
        listBackupTargetsXSmallConfigMapping.setDuration(1);
        configMapping.put("XSmall",  listBackupTargetsXSmallConfigMapping);

        RateLimitConfigMapping listBackupTargetsSmallConfigMapping = new RateLimitConfigMapping();
        listBackupTargetsSmallConfigMapping.setCount(5);
        listBackupTargetsSmallConfigMapping.setDuration(1);
        configMapping.put("Small",  listBackupTargetsSmallConfigMapping);

        RateLimitConfigMapping listBackupTargetsLargeConfigMapping = new RateLimitConfigMapping();
        listBackupTargetsLargeConfigMapping.setCount(5);
        listBackupTargetsLargeConfigMapping.setDuration(1);
        configMapping.put("Large",  listBackupTargetsLargeConfigMapping);

        RateLimitConfigMapping listBackupTargetsXLargeConfigMapping = new RateLimitConfigMapping();
        listBackupTargetsXLargeConfigMapping.setCount(5);
        listBackupTargetsXLargeConfigMapping.setDuration(1);
        configMapping.put("XLarge",  listBackupTargetsXLargeConfigMapping);

        rateLimitOpConfig.setSizeConfigMapping(configMapping);
        return rateLimitOpConfig;
    }
    /**
    * Rate limiter configuration for UPDATE_BACKUP_TARGET_BY_ID
    */
    @Bean(name="prism_management_updateBackupTargetById")
    public RateLimitOpConfig updateBackupTargetByIdRateLimitOpConfig() {
        RateLimitOpConfig rateLimitOpConfig = new RateLimitOpConfig();
        rateLimitOpConfig.setName("prism_management_updateBackupTargetById");
        Map<String, RateLimitConfigMapping> configMapping = new HashMap<>();

        RateLimitConfigMapping updateBackupTargetByIdXSmallConfigMapping = new RateLimitConfigMapping();
        updateBackupTargetByIdXSmallConfigMapping.setCount(3);
        updateBackupTargetByIdXSmallConfigMapping.setDuration(1);
        configMapping.put("XSmall",  updateBackupTargetByIdXSmallConfigMapping);

        RateLimitConfigMapping updateBackupTargetByIdSmallConfigMapping = new RateLimitConfigMapping();
        updateBackupTargetByIdSmallConfigMapping.setCount(3);
        updateBackupTargetByIdSmallConfigMapping.setDuration(1);
        configMapping.put("Small",  updateBackupTargetByIdSmallConfigMapping);

        RateLimitConfigMapping updateBackupTargetByIdLargeConfigMapping = new RateLimitConfigMapping();
        updateBackupTargetByIdLargeConfigMapping.setCount(3);
        updateBackupTargetByIdLargeConfigMapping.setDuration(1);
        configMapping.put("Large",  updateBackupTargetByIdLargeConfigMapping);

        RateLimitConfigMapping updateBackupTargetByIdXLargeConfigMapping = new RateLimitConfigMapping();
        updateBackupTargetByIdXLargeConfigMapping.setCount(3);
        updateBackupTargetByIdXLargeConfigMapping.setDuration(1);
        configMapping.put("XLarge",  updateBackupTargetByIdXLargeConfigMapping);

        rateLimitOpConfig.setSizeConfigMapping(configMapping);
        return rateLimitOpConfig;
    }
}

