/*
 * Copyright (c) 2024. Nutanix Inc. All rights reserved.
 * @Author: Ritik Nawal(ritik.nawal@nutanix.com)
 */
package com.nutanix.clustermgmtserver.config;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Slf4j
public class RateLimiterConfiguration implements WebMvcConfigurer{

  private RateLimiterRegistry rateLimiterRegistry;

  @Bean
  public RateLimiterRegistry rateLimiterRegistry() {
    this.rateLimiterRegistry = RateLimiterRegistry.ofDefaults();
    return this.rateLimiterRegistry;
  }

  @Bean(name = "pcSize")
  public String pcSize() {
    return "Small";
  }

}
