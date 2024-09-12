/*
 * Copyright (c) 2023 Nutanix Inc. All rights reserved.
 *
 * Author: ritik.nawal@nutanix.com
 */

package com.nutanix.clustermgmtserver.links;

import com.nutanix.clustermgmtserver.utils.ClustermgmtUtils;
import dp1.clu.common.v1.response.ApiLink;
import dp1.clu.prism.v4.config.TaskReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.testng.annotations.Test;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestExecutionListeners(MockitoTestExecutionListener.class)
@ActiveProfiles("test")
@Slf4j
public class ApiLinkFactoryTest {

  @Test
  public void testpaginationLinks(){
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/api/clustermgmt/v4/config/clusters");
    request.addHeader(ClustermgmtUtils.FULL_VERSION_HEADER_NAME,
      ClustermgmtUtils.LATEST_FULL_VERSION_HEADER_VALUE);
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    Map<String,String> queryParams = new HashMap<>();
    queryParams.put("$page", "3");
    queryParams.put("$limit", "10");
    Integer totalEntities = 5;
    List<ApiLink> links = ApiLinkFactory.paginationLinks(totalEntities, queryParams);
    assertEquals(links.size(), 3);
    assertEquals(links.get(1).getRel(), "self");
    assertEquals(links.get(1).getHref(), "http://localhost/api/clustermgmt/v4/config/clusters?$limit=10&$page=3");
  }

  @Test
  public void testGetPrismGatewayUrl() {
    RequestContextHolder.setRequestAttributes(
      new ServletRequestAttributes(new MockHttpServletRequest()));

    String path = "/clustermgmt/v4/config/clusters";
    String replacedPath = ApiLinkFactory.getPrismGatewayUrl(path);
    assertEquals(replacedPath, "https://localhost:9440/api/clustermgmt/v4/config/clusters");
  }

  @Test
  public void testGetTaskUrl() {
    RequestContextHolder.setRequestAttributes(
      new ServletRequestAttributes(new MockHttpServletRequest()));

    TaskReference task = new TaskReference();
    String taskUuid = "ZXJnb24=:cfb08be0-3f01-446c-6649-dc9303394c91";
    task.setExtId(taskUuid);
    String taskUrl = ApiLinkFactory.getTaskUrl(task);
    assertEquals(taskUrl , "https://localhost:9440/api/nutanix/v3/tasks/cfb08be0-3f01-446c-6649-dc9303394c91");
  }

  @Test
  public void testGetHATEOASLink() {
    String linkTemplate = "https://a/b/c/{d}";
    String relation = "SELF";
    String extId = "e";
    ApiLink apiLink = ApiLinkFactory.getHATEOASLink(
      linkTemplate, relation, extId);
    assertEquals(apiLink.getHref(), "https://a/b/c/e");
    assertEquals(apiLink.getRel(), "SELF");
  }

  @Test
  public void testGetHATEOASLinkWithQueryParams() {
    String linkTemplate = "https://a/b/c/{d}";
    String relation = "SELF";
    String extId = "e";
    ApiLink apiLink = ApiLinkFactory.getHATEOASLink(
      linkTemplate, relation,
      Collections.singletonMap("f", "g"), extId);
    assertEquals(apiLink.getHref(), "https://a/b/c/e?f=g");
    assertEquals(apiLink.getRel(), "SELF");
  }

}
