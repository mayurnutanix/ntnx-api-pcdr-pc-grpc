package com.nutanix.clustermgmtserver.versioning_registry;

import com.nutanix.clustermgmtserver.adapters.api.ClusterResourceAdapter;
import com.nutanix.clustermgmtserver.adapters.impl.BaseClusterResourceAdapterImpl;
import com.nutanix.clustermgmtserver.utils.RequestContextHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class VersionAwareClustermgmtAdapterRegistry {
  private final ApplicationContext context;
  private final Map<String, Class<? extends ClusterResourceAdapter>> versionToAdapterMap =
    new HashMap<>();

  @Autowired
  public VersionAwareClustermgmtAdapterRegistry(final ApplicationContext context) {
    this.context = context;
  }

  @PostConstruct
  public void setUpRegistry() {
    Map<String, BaseClusterResourceAdapterImpl> beanMap =
      context.getBeansOfType(BaseClusterResourceAdapterImpl.class);
    // The version string corresponding to each StorageResourceAdapter is
    // of type "<version>-storage-adapter". In order to extract the version,
    // split is done based on "-".
    beanMap.forEach((key, value) -> {
      versionToAdapterMap.put(value.getVersionOfAdapter().split("-")[0],
        value.getClass());
      value.setAdapterRegistry(this);
    });
    log.info("Version to StorageResourceAdapter map loaded successfully {}", versionToAdapterMap);
  }

  public boolean canForwardToNextHandler(Class<? extends ClusterResourceAdapter> adapter) {
    // If the client version is higher than that of PC, the call will be dropped
    // at the Adonis layer.
    // Otherwise, traverse the handler chain from v4.0.a1 -> .... -> client version.
    String version = RequestContextHelper.getVersionHeader();
    boolean result = versionToAdapterMap.get(version).equals(adapter);
    log.debug("Adapter {} can handle the version {} -> {}", adapter, version, result);
    return !result;
  }
}
