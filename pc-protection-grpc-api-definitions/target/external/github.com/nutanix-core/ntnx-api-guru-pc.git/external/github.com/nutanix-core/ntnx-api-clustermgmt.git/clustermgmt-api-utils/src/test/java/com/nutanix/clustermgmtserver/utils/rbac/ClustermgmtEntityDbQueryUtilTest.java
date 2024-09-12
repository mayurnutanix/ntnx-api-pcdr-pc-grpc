/*
 * Copyright (c) 2023 Nutanix Inc. All rights reserved.
 *
 * Author: rakesh.falak@nutanix.com
 */

package com.nutanix.clustermgmtserver.utils.rbac;

import com.nutanix.insights.ifc.InsightsInterfaceProto;
import com.nutanix.prism.exception.EntityDbException;
import com.nutanix.prism.service.EntityDbService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestExecutionListeners(MockitoTestExecutionListener.class)
@ActiveProfiles("test")
public class ClustermgmtEntityDbQueryUtilTest extends AbstractTestNGSpringContextTests {
    public static final String CLUSTER_UUID =
      "5cafbb40-dfd4-4eaf-ac8d-5ff91a230c7a";
    @MockBean
    private EntityDbService entityDbService;

    ClustermgmtEntityDbQueryUtil clustermgmtEntityDbQueryUtil;

    @BeforeMethod
    public void setUp() {
        clustermgmtEntityDbQueryUtil = new ClustermgmtEntityDbQueryUtil(entityDbService);
    }

    @AfterMethod
    public void afterTestCleanup() {
        Mockito.reset(entityDbService);
    }

    @Test
    public void getEntityFromEntityDbTest() throws Exception {
        when(entityDbService.getEntities(any())).thenReturn(InsightsInterfaceProto.GetEntitiesRet.newBuilder().build());
        InsightsInterfaceProto.GetEntitiesRet getEntitiesRet =
          clustermgmtEntityDbQueryUtil.getEntityFromEntityDb("cluster", CLUSTER_UUID);
        assertNotNull(getEntitiesRet);
    }

    @Test
    public void getEntityFromEntityDbTestExceptionCase() throws Exception{
        when(entityDbService.getEntities(any())).thenThrow(new EntityDbException("test"));
        InsightsInterfaceProto.GetEntitiesRet getEntitiesRet =
          clustermgmtEntityDbQueryUtil.getEntityFromEntityDb("cluster", CLUSTER_UUID);
        assertNull(getEntitiesRet);
    }
}