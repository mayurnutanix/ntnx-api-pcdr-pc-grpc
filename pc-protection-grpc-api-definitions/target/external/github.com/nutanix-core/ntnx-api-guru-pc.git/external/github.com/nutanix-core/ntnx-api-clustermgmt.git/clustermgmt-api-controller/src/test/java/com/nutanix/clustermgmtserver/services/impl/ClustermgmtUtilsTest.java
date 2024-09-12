package com.nutanix.clustermgmtserver.services.impl;

import com.nutanix.clustermgmtserver.proxy.ErgonProxyImpl;
import com.nutanix.clustermgmtserver.utils.ClusterTestUtils;
import com.nutanix.clustermgmtserver.utils.ClustermgmtUtils;
import com.nutanix.util.base.UuidUtils;
import nutanix.ergon.ErgonInterface;
import nutanix.ergon.ErgonTypes;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/*
This file contains UTs of functions defined in ClustermgmtUtils file.
 */

public class ClustermgmtUtilsTest extends ClusterServiceImplTest {

  @Test
  public void taskCreateUtilTest() throws Exception {
    ErgonInterface.TaskCreateArg taskCreateArg =
      ClustermgmtUtils.createTaskArg(UuidUtils.getByteStringFromUUID(ClusterTestUtils.TASK_UUID), null, null,
        null, UuidUtils.getByteStringFromUUID(ClusterTestUtils.TASK_UUID), 1, null, null);
    assertEquals(taskCreateArg.getUuid(), UuidUtils.getByteStringFromUUID(ClusterTestUtils.TASK_UUID));
  }

  @Test
  public void getKeyTest() throws Exception {
    assertEquals(ClustermgmtUtils.getKey(ClustermgmtUtils.hypervisorTypeToStringMap, "test"), null);
  }

  @Test
  public void updateTaskBasedOnStatusTest() throws Exception{
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_GET), any()))
      .thenReturn(ClusterTestUtils.getTaskRet());
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_UPDATE), any()))
      .thenReturn(ClusterTestUtils.getUpdateTaskRet());
    clusterService.updateTaskBasedOnStatus(UuidUtils.getByteStringFromUUID(ClusterTestUtils.TASK_UUID),50, ErgonTypes.Task.Status.kFailed, "task failed");
  }

  @Test
  public void updateTaskBasedOnStatusTestForNullCase() throws Exception{
    when(ergonProxy.doExecute(eq(ErgonProxyImpl.ErgonRpcName.TASK_GET), any()))
      .thenReturn(null);
    clusterService.updateTaskBasedOnStatus(UuidUtils.getByteStringFromUUID(ClusterTestUtils.TASK_UUID),50, ErgonTypes.Task.Status.kFailed, "task failed");
  }

}
