/*
 * Copyright (c) 2021 Nutanix Inc. All rights reserved.
 *
 * Author: rakesh.falak@nutanix.com
 */
package com.nutanix.clustermgmtserver.utils;

import com.nutanix.api.utils.auth.IAMTokenClaims;
import com.nutanix.clustermgmtserver.adapters.impl.BaseClusterResourceAdapterImpl;
import com.nutanix.clustermgmtserver.versioning_registry.VersionAwareClustermgmtAdapterRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.when;

public class TestConstantUtils {
  public static final String HOST_IP = "127.0.0.1";
  public static final int INSIGHTS_PORT = 2027;
  public static final int ERGON_PORT = 2090;
  public static final int GENESIS_PORT = 2100;
  public static final int CMS_PORT = 2104;

  public static String randomUUIDString() {
    return UUID.randomUUID().toString();
  }

  public static com.nutanix.clustermgmtserver.adapters.impl.v4.r0.a1.ClusterResourceAdapterImpl clusterResourceAdapterV40A1;
  public static com.nutanix.clustermgmtserver.adapters.impl.v4.r0.a2.ClusterResourceAdapterImpl clusterResourceAdapterV40A2;

  public static com.nutanix.clustermgmtserver.adapters.impl.v4.r0.b1.ClusterResourceAdapterImpl clusterResourceAdapterV40B1;

  public static com.nutanix.clustermgmtserver.adapters.impl.v4.r0.b2.ClusterResourceAdapterImpl clusterResourceAdapterV40B2;

  public static com.nutanix.clustermgmtserver.adapters.impl.v4.r0.ClusterResourceAdapterImpl clusterResourceAdapterV40R0;

  static {
    clusterResourceAdapterV40R0 = new com.nutanix.clustermgmtserver.adapters.impl.v4.r0.ClusterResourceAdapterImpl();
    clusterResourceAdapterV40B2 = new com.nutanix.clustermgmtserver.adapters.impl.v4.r0.b2.ClusterResourceAdapterImpl(
      clusterResourceAdapterV40R0);
    clusterResourceAdapterV40B1 = new com.nutanix.clustermgmtserver.adapters.impl.v4.r0.b1.ClusterResourceAdapterImpl(
      clusterResourceAdapterV40B2);
    clusterResourceAdapterV40A2 = new com.nutanix.clustermgmtserver.adapters.impl.v4.r0.a2.ClusterResourceAdapterImpl(
      clusterResourceAdapterV40B1);
    clusterResourceAdapterV40A1 = new com.nutanix.clustermgmtserver.adapters.impl.v4.r0.a1.ClusterResourceAdapterImpl(
      clusterResourceAdapterV40A2);
  }

  public static VersionAwareClustermgmtAdapterRegistry getAdapterRegistry(final ApplicationContext context) {
    Map<String, BaseClusterResourceAdapterImpl> map = new HashMap<>();
    map.put(com.nutanix.clustermgmtserver.adapters.impl.v4.r0.a1.ClusterResourceAdapterImpl.ADAPTER_VERSION,
      clusterResourceAdapterV40A1);
    map.put(com.nutanix.clustermgmtserver.adapters.impl.v4.r0.a2.ClusterResourceAdapterImpl.ADAPTER_VERSION,
      clusterResourceAdapterV40A2);
    map.put(com.nutanix.clustermgmtserver.adapters.impl.v4.r0.b1.ClusterResourceAdapterImpl.ADAPTER_VERSION,
      clusterResourceAdapterV40B1);
    map.put(com.nutanix.clustermgmtserver.adapters.impl.v4.r0.b2.ClusterResourceAdapterImpl.ADAPTER_VERSION,
      clusterResourceAdapterV40B2);
    map.put(com.nutanix.clustermgmtserver.adapters.impl.v4.r0.ClusterResourceAdapterImpl.ADAPTER_VERSION,
      clusterResourceAdapterV40R0);
    when(context.getBeansOfType(BaseClusterResourceAdapterImpl.class)).thenReturn(map);

    VersionAwareClustermgmtAdapterRegistry adapterRegistry =
      new VersionAwareClustermgmtAdapterRegistry(context);
    adapterRegistry.setUpRegistry();
    return adapterRegistry;
  }

  /**
   * Set the given user UUID in the authentication object of the security context.
   * @param userUUID The user UUID to set.
   */
  public static void setUserUUIDInSecurityContext(final String userUUID) {
    final IAMTokenClaims iamTokenClaims = IAMTokenClaims.builder()
      .userUUID(userUUID)
      .build();
    final Authentication auth = new TestingAuthenticationToken(iamTokenClaims, null);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  /**
   * Reset the authentication in the security context.
   */
  public static void resetSecurityContextAuthentication() {
    SecurityContextHolder.getContext().setAuthentication(null);
  }
}
