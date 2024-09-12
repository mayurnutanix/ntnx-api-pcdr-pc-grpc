/*
 * Copyright (c) 2021 Nutanix Inc. All rights reserved.
 *
 * Author: rakesh.fakak@nutanix.com
 */

package com.nutanix.clustermgmtserver.controllers;

import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.nutanix.clustermgmtserver.config.RateLimiterConfiguration;
import com.nutanix.odata.core.csdl.CsdlEntityBinding;
import com.nutanix.odata.core.edm.EdmProvider;
import com.nutanix.odata.core.service.ServiceMetadataProvider;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication(scanBasePackages = {
  "com.nutanix.clustermgmtserver", "dp1.clu.edm.clustermgmt.v4"},
  scanBasePackageClasses = {
    RateLimiterConfiguration.class
  },
  exclude = {
  })
public class ClustermgmtTestApplication {
  @Bean
  public EdmProvider edmProvider(List<CsdlEntityBinding> entityBindings) {
    return new EdmProvider(entityBindings);
  }

  @Bean
  public ServiceMetadataProvider serviceMetadataProvider(EdmProvider edmProvider) {
    return new ServiceMetadataProvider(edmProvider);
  }

  @Bean
  public Jackson2ObjectMapperBuilderCustomizer clustermgmtJsonCustomizer() {

    SimpleFilterProvider provider = new SimpleFilterProvider()
      .setFailOnUnknownId(false)
      .setDefaultFilter(SimpleBeanPropertyFilter.serializeAll());

    return  jacksonObjectMapperBuilder -> jacksonObjectMapperBuilder.filters(provider);
  }
}