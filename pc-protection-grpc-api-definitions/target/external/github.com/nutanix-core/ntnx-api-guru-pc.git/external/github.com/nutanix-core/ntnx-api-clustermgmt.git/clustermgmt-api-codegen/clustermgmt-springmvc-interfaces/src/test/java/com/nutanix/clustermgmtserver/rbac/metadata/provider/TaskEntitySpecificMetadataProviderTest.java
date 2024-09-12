package com.nutanix.clustermgmtserver.rbac.metadata.provider;

import com.nutanix.clustermgmtserver.utils.ClusterTestUtilsForMvc;
import com.nutanix.clustermgmtserver.utils.ClusterTestUtilsForMvc;
import com.nutanix.clustermgmtserver.utils.rbac.ClustermgmtEntityDbQueryUtil;
import com.nutanix.prism.rbac.MetadataArguments;
import com.nutanix.prism.rbac.RbacConstants;
import org.mockito.Mock;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;
import static org.testng.Assert.assertNotNull;

public class TaskEntitySpecificMetadataProviderTest {
  public final static String PRISM_GATEWAY_URL = "https://localhost:%s/api";
  public static final String V4_R0_A1_VERSION = "v4.0.a1";
  public static final String CLUSTERMGMT_CONTROLLER_TASK_BASE_URL_WITH_FULL_VERSION =
    "/clustermgmt/" + V4_R0_A1_VERSION + "/config/tasks";
  private TaskEntitySpecificMetadataProvider provider;

  @Mock
  private ClustermgmtEntityDbQueryUtil clustermgmtEntityDbQueryUtil;

  @BeforeMethod
  public void setUp() {
    clustermgmtEntityDbQueryUtil = mock(ClustermgmtEntityDbQueryUtil.class);
    provider = new TaskEntitySpecificMetadataProvider(clustermgmtEntityDbQueryUtil);
  }

  @AfterMethod
  public void tearDown() {
    reset(clustermgmtEntityDbQueryUtil);
  }

  @Test
  public void testGetMetadata() throws Exception {
    MetadataArguments metadataArguments = new MetadataArguments();
    metadataArguments.setRequestUrl(
      String.format(PRISM_GATEWAY_URL, 9080) +
        CLUSTERMGMT_CONTROLLER_TASK_BASE_URL_WITH_FULL_VERSION + "/" +
        ClusterTestUtilsForMvc.TASK_UUID);

    when(clustermgmtEntityDbQueryUtil.getEntityFromEntityDb("task", ClusterTestUtilsForMvc.TASK_UUID))
      .thenReturn(ClusterTestUtilsForMvc.getTaskIdfEntityObj(ClusterTestUtilsForMvc.TASK_UUID));
    Map<String, Object> metadataMap = provider.getMetadata(metadataArguments, null);
    assertNotNull(metadataMap);
  }

  @Test
  public void testGetMetadataWithNullParam() throws Exception {
    when(clustermgmtEntityDbQueryUtil.getEntityFromEntityDb("task", ClusterTestUtilsForMvc.TASK_UUID))
      .thenReturn(ClusterTestUtilsForMvc.getTaskIdfEntityObj(ClusterTestUtilsForMvc.TASK_UUID));
    Map<String, Object> metadataMap = provider.getMetadata(null, null);
    assertNotNull(metadataMap);
  }

  @Test
  public void testGetMetadataWithNoUuid() throws Exception {
    MetadataArguments metadataArguments = new MetadataArguments();
    metadataArguments.setRequestUrl(
      String.format(PRISM_GATEWAY_URL, 9080) +
        CLUSTERMGMT_CONTROLLER_TASK_BASE_URL_WITH_FULL_VERSION);
    when(clustermgmtEntityDbQueryUtil.getEntityFromEntityDb("task", ClusterTestUtilsForMvc.TASK_UUID))
      .thenReturn(ClusterTestUtilsForMvc.getTaskIdfEntityObj(ClusterTestUtilsForMvc.TASK_UUID));
    Map<String, Object> metadataMap = provider.getMetadata(metadataArguments, null);
    assertNotNull(metadataMap);;
  }

  @Test
  public void testGetMetadataWithNoEntityInIDF() throws Exception {
    MetadataArguments metadataArguments = new MetadataArguments();
    metadataArguments.setRequestUrl(
      String.format(PRISM_GATEWAY_URL, 9080) +
        CLUSTERMGMT_CONTROLLER_TASK_BASE_URL_WITH_FULL_VERSION + "/" +
        ClusterTestUtilsForMvc.TASK_UUID);
    when(clustermgmtEntityDbQueryUtil.getEntityFromEntityDb("task", ClusterTestUtilsForMvc.TASK_UUID))
      .thenReturn(null);
    Map<String, Object> metadataMap = provider.getMetadata(metadataArguments, null);
    assertNotNull(metadataMap);;
  }
}