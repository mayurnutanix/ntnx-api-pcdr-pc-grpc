/*
 * Copyright (c) 2021 Nutanix Inc. All rights reserved
 *
 * Author: rakesh.falak@nutanix.com
 */

package com.nutanix.clustermgmtserver.links;

import com.google.re2j.Pattern;
import com.nutanix.api.utils.links.PaginationLinkUtils;
import com.nutanix.api.utils.task.ErgonTaskUtils;
import com.nutanix.clustermgmtserver.utils.RequestContextHelper;
import dp1.clu.common.v1.response.ApiLink;
import dp1.clu.prism.v4.config.TaskReference;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApiLinkFactory {

  private static final String PRISM_GATEWAY_URL = "https://%s/api%s";
  private static final String TASK_URL = "https://%s/api/nutanix/v3/tasks/%s";
  private static final Pattern API_PATH_REGEX =
    Pattern.compile("/([-\\w]+)/([.\\w]+)/([-.\\w]+)(.*)");

  private ApiLinkFactory() {}

  /**
   * Returns the current HttpServletRequest.
   * @return the current request.
   */
  static HttpServletRequest getRequest() {
    final ServletRequestAttributes attrs = (ServletRequestAttributes)
      RequestContextHolder.currentRequestAttributes();
    return attrs.getRequest();
  }

  /**
   * Returns the list of ApiLinks for pagination query.
   * @param totalEntities task identifier.
   * @return Link to cancel the task.
   */
  public static List<ApiLink> paginationLinks(final Integer totalEntities, final Map<String, String> allQueryParams) {
    final HttpServletRequest request = getRequest();
    String completeUrl = request.getRequestURL().toString();
    boolean firstParam = true;
    for (Map.Entry<String, String> queryParam : allQueryParams.entrySet()) {
      if(firstParam) {
        completeUrl += "?";
      } else {
        completeUrl += "&";
      }
      firstParam = false;
      completeUrl += queryParam.getKey() + "=" + queryParam.getValue();
    }

    final Map<String, String> paginationLinkMap = PaginationLinkUtils.getPaginationLinks(totalEntities, completeUrl);
    List<ApiLink> apiLinkList = new ArrayList<>();
    paginationLinkMap.forEach((rel, url) -> apiLinkList.add(new ApiLink(url, rel)));
    return apiLinkList;
  }

  public static String getExpandedUrl(final String httpUrl,
                                       final Object... uriVariables) {

    return UriComponentsBuilder.fromHttpUrl(httpUrl)
      .buildAndExpand(uriVariables)
      .toUriString();
  }

  public static String getExpandedUrl(final String httpUrl,
                                       final Map<String, String> queryParams,
                                       final Object... uriVariables) {

    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(httpUrl);
    queryParams.forEach(builder::queryParam);
    return builder.buildAndExpand(uriVariables).toUriString();
  }

  public static ApiLink getHATEOASLink(
    final String linkTemplate, final String relation,
    final Object... uriVariables) {

    ApiLink apiLink = new ApiLink();
    apiLink.setHref(getExpandedUrl(linkTemplate, uriVariables));
    apiLink.setRel(relation);
    return apiLink;
  }

  public static ApiLink getHATEOASLink(
    final String linkTemplate, final String relation,
    final Map<String, String> queryParams, final Object... uriVariables) {

    ApiLink apiLink = new ApiLink();
    apiLink.setHref(getExpandedUrl(linkTemplate, queryParams, uriVariables));
    apiLink.setRel(relation);
    return apiLink;
  }

  public static String getPrismGatewayUrl(final String path) {
    return String.format(PRISM_GATEWAY_URL, RequestContextHelper.getHostname(),
      obtainUriComprisingFullVersion(path));
  }

  public static String getTaskUrl(final TaskReference task) {
    return String.format(TASK_URL, RequestContextHelper.getHostname(),
      ErgonTaskUtils.getTaskUuid(task.getExtId()));
  }

  private static String obtainUriComprisingFullVersion(final String uri) {
    final String versionHeader = RequestContextHelper.getVersionHeader();
    return uri.replaceAll(API_PATH_REGEX.toString(),
      String.format("/$1/%s/$3$4",
        StringUtils.isEmpty(versionHeader) ? "$2" : versionHeader));
  }

}
