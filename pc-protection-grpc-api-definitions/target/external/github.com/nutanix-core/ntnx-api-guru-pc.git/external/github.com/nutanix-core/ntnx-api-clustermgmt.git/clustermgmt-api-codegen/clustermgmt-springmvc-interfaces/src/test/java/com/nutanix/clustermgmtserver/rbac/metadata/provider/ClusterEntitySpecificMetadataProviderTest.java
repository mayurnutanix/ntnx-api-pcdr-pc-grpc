package com.nutanix.clustermgmtserver.rbac.metadata.provider;

import com.nutanix.api.utils.exceptions.PlatformResponseException.RbacAuthorizationException;
import com.nutanix.clustermgmtserver.utils.ClusterTestUtilsForMvc;
import com.nutanix.clustermgmtserver.utils.rbac.ClustermgmtEntityDbQueryUtil;
import com.nutanix.prism.rbac.MetadataArguments;
import com.nutanix.prism.rbac.RbacConstants;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class ClusterEntitySpecificMetadataProviderTest {
  public final static String PRISM_GATEWAY_URL = "https://localhost:%s/api";
  public static final String V4_R0_A1_VERSION = "v4.0.a1";
  public static final String CLUSTERMGMT_CONTROLLER_BASE_URL_WITH_FULL_VERSION =
    "/clustermgmt/" + V4_R0_A1_VERSION + "/config/cluster";
  private ClusterEntitySpecificMetadataProvider provider;

  @Mock
  private ClustermgmtEntityDbQueryUtil clustermgmtEntityDbQueryUtil;

  @BeforeMethod
  public void setUp() {
    clustermgmtEntityDbQueryUtil = mock(ClustermgmtEntityDbQueryUtil.class);
    provider = new ClusterEntitySpecificMetadataProvider(clustermgmtEntityDbQueryUtil);
  }

  @Test
  public void testGetMetadata() throws Exception {
    MetadataArguments metadataArguments = new MetadataArguments();
    metadataArguments.setRequestUrl(
      String.format(PRISM_GATEWAY_URL, 9080) +
        CLUSTERMGMT_CONTROLLER_BASE_URL_WITH_FULL_VERSION + "/" +
        ClusterTestUtilsForMvc.CLUSTER_UUID);

    when(clustermgmtEntityDbQueryUtil.getEntityFromEntityDb("cluster", ClusterTestUtilsForMvc.CLUSTER_UUID))
      .thenReturn(ClusterTestUtilsForMvc.getClusterIdfEntityObj(ClusterTestUtilsForMvc.CLUSTER_UUID));
    Map<String, Object> metadataMap = provider.getMetadata(metadataArguments, null);
    assertNotNull(metadataMap);
    assertTrue(metadataMap.containsKey(RbacConstants.UUID_KEY));
    assertEquals(metadataMap.get(RbacConstants.UUID_KEY),
      ClusterTestUtilsForMvc.CLUSTER_UUID);
  }

  @Test(expectedExceptions = RbacAuthorizationException.class)
  public void testGetMetadataWithNullParam() throws Exception {
    when(clustermgmtEntityDbQueryUtil.getEntityFromEntityDb("cluster", ClusterTestUtilsForMvc.CLUSTER_UUID))
      .thenReturn(ClusterTestUtilsForMvc.getClusterIdfEntityObj(ClusterTestUtilsForMvc.CLUSTER_UUID));
    provider.getMetadata(null, null);
  }

  @Test(expectedExceptions = RbacAuthorizationException.class)
  public void testGetMetadataWithNoUuid() throws Exception {
    MetadataArguments metadataArguments = new MetadataArguments();
    metadataArguments.setRequestUrl(
      String.format(PRISM_GATEWAY_URL, 9080) +
        CLUSTERMGMT_CONTROLLER_BASE_URL_WITH_FULL_VERSION);
    when(clustermgmtEntityDbQueryUtil.getEntityFromEntityDb("cluster", ClusterTestUtilsForMvc.CLUSTER_UUID))
      .thenReturn(ClusterTestUtilsForMvc.getClusterIdfEntityObj(ClusterTestUtilsForMvc.CLUSTER_UUID));
    provider.getMetadata(metadataArguments, null);
  }

  @Test
  public void testGetMetadataWithNoEntityInIDF() throws Exception {
    MetadataArguments metadataArguments = new MetadataArguments();
    metadataArguments.setRequestUrl(
      String.format(PRISM_GATEWAY_URL, 9080) +
        CLUSTERMGMT_CONTROLLER_BASE_URL_WITH_FULL_VERSION + "/" +
        ClusterTestUtilsForMvc.CLUSTER_UUID);
    when(clustermgmtEntityDbQueryUtil.getEntityFromEntityDb("cluster", ClusterTestUtilsForMvc.CLUSTER_UUID))
      .thenReturn(null);
    Map<String, Object> metadataMap = provider.getMetadata(metadataArguments, null);
    assertNotNull(metadataMap);;
  }
}