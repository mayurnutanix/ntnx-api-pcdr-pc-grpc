package com.nutanix.clustermgmtserver.adapters.impl.v4.r0.b2;

import com.nutanix.clustermgmtserver.adapters.api.ClusterResourceAdapter;
import com.nutanix.clustermgmtserver.adapters.impl.BaseClusterResourceAdapterImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component(com.nutanix.clustermgmtserver.adapters.impl.v4.r0.b2.ClusterResourceAdapterImpl.ADAPTER_VERSION)
@Slf4j
public class ClusterResourceAdapterImpl extends BaseClusterResourceAdapterImpl {
  public static final String ADAPTER_VERSION = "v4.0.b2-clustermgmt-adapter";

  @Autowired
  public ClusterResourceAdapterImpl(
    @Qualifier(
      com.nutanix.clustermgmtserver.adapters.impl.v4.r0.ClusterResourceAdapterImpl.ADAPTER_VERSION
    ) final ClusterResourceAdapter nextChainAdapter) {
    this.nextChainAdapter = nextChainAdapter;
  }

  @Override
  public String getVersionOfAdapter() {
    return ADAPTER_VERSION;
  }

}
