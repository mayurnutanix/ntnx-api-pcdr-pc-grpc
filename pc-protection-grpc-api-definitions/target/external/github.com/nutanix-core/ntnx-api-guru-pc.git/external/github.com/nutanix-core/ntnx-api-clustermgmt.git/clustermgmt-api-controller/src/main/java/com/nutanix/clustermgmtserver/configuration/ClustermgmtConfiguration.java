/*
 * Copyright (c) 2021 Nutanix Inc. All rights reserved.
 *
 * Author: rakesh.falak@nutanix.com
 */

package com.nutanix.clustermgmtserver.configuration;

import com.nutanix.api.processor.message.ProcessorUtils;
import com.nutanix.api.utils.message.NATSMessageBusImpl;
import com.nutanix.clustermgmtserver.adapters.impl.ClustermgmtResourceAdapterImpl;
import com.nutanix.clustermgmtserver.proxy.CMSProxyImpl;
import com.nutanix.clustermgmtserver.proxy.EntityDbProxyImpl;
import com.nutanix.clustermgmtserver.proxy.ErgonProxyImpl;
import com.nutanix.clustermgmtserver.proxy.GenesisProxyImpl;
import com.nutanix.clustermgmtserver.services.impl.StatsGatewayServiceImpl;
import com.nutanix.prism.adapter.service.ApplianceConfiguration;
import com.nutanix.prism.commands.multicluster.MulticlusterZeusConfigurationManagingZkImpl;
import com.nutanix.prism.config.spring.ApplianceConfigurationServiceFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClustermgmtConfiguration {

  @Value("${clustermgmt.proxy.host:localhost}")
  private String clustermgmtProxyHost;
  @Value("${clustermgmt.proxy.port:2100}")
  private Integer clustermgmtProxyPort;
  @Value("${insights.proxy.port:2027}")
  private Integer insightsProxyPort;
  @Value("${ergon.proxy.port:2090}")
  private Integer ergonProxyPort;
  @Value("${messgebus.proxy.port:20042}")
  private Integer messageBusProxyPort;
  @Value("${cms.proxy.port:2104}")
  private Integer cmsProxyPort;

  @Value("${stats.gateway.service.host:localhost}")
  private String statsGatewayHost;

  @Value("${stats.gateway.service.port:8084}")
  private Integer statsGatewayPort;

  @Bean
  ClustermgmtResourceAdapterImpl clustermgmtResourceAdapter() { return new ClustermgmtResourceAdapterImpl(); }

  @Bean
  EntityDbProxyImpl clustermgmtEntityDbProxy() {
    return new EntityDbProxyImpl(clustermgmtProxyHost, insightsProxyPort);
  }

  @Bean
  ApplianceConfiguration clustermgmtApplianceConfiguration() {
    return ApplianceConfigurationServiceFactory.getSingletonInstance();
  }

  @Bean
  GenesisProxyImpl clustermgmtGenesisProxy() {
    return new GenesisProxyImpl(clustermgmtProxyHost, clustermgmtProxyPort);
  }

  @Bean
  ErgonProxyImpl clustermgmtErgonProxy() {
    return new ErgonProxyImpl(clustermgmtProxyHost, ergonProxyPort);
  }

  @Bean
  CMSProxyImpl clustermgmtCMSProxy() { return new CMSProxyImpl(clustermgmtProxyHost, cmsProxyPort); }

  @Bean
  StatsGatewayServiceImpl clustermgmtStatsGatewayService() {
    return new StatsGatewayServiceImpl(statsGatewayHost, statsGatewayPort);
  }
}
