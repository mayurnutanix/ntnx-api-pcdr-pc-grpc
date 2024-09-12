/*
 * Copyright (c) 2021 Nutanix Inc. All rights reserved.
 *
 * Author: rakesh.falak@nutanix.com
 */

package com.nutanix.clustermgmtserver.controllers;

import clustermgmt.v4.config.ClustersApiControllerInterface;
import com.nutanix.api.utils.task.ErgonTaskUtils;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtGenericException;
import com.nutanix.clustermgmtserver.exceptions.ClustermgmtServiceException;
import com.nutanix.clustermgmtserver.services.api.ClusterService;
import com.nutanix.clustermgmtserver.utils.*;
import com.nutanix.prism.exception.idempotency.IdempotencySupportException;
import com.nutanix.util.base.Pair;
import com.nutanix.util.base.ValidationException;
import dp1.clu.clustermgmt.v4.config.*;
import dp1.clu.prism.v4.config.TaskReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.RestController;
import static org.springframework.http.HttpHeaders.ETAG;
import static org.springframework.http.HttpHeaders.IF_MATCH;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class ClusterApiControllerImpl implements ClustersApiControllerInterface {
  private static final String RPC_INVOKE_ERROR_MESSAGE = "Exception occurred while invoking rpc";
  private ClusterService clusterService;
  private static final String DEFAULT_PAGE_SIZE = "50";
  private static final String MAX_PAGE_SIZE = "100";
  private static final String ETAG_EXCEPTION_MSG = "Etag Precondition check failed";
  private static final String BATCH_TASK_HEADER = "NTNX-Batch-Task-Id";
  private final ClustermgmtIdempotentTokenFactory clustermgmtIdempotentTokenFactory;

  @Autowired
  public ClusterApiControllerImpl(final ClusterService clusterService,
                                  final ClustermgmtIdempotentTokenFactory clustermgmtIdempotentTokenFactory) {
    this.clusterService = clusterService;
    this.clustermgmtIdempotentTokenFactory = clustermgmtIdempotentTokenFactory;
  }

  @Override
  public ResponseEntity<MappingJacksonValue> getClusterById(String uuid, String expand,
                                                     Map<String, String> allQueryParams,
                                                     HttpServletRequest request,
                                                     HttpServletResponse response) {
    HttpStatus httpStatus;
    GetClusterApiResponse getClusterApiResponse = new GetClusterApiResponse();
    try {
      Cluster clusterEntity = clusterService.getClusterEntity(uuid, expand);

      // Etag
      String etag = ClustermgmtUtils.calculateEtag(clusterEntity);
      response.setHeader(ETAG, etag);
      httpStatus = EtagUtils.getHttpStatus(request, etag);

      if (httpStatus != HttpStatus.NOT_MODIFIED) {
        getClusterApiResponse.setDataInWrapper(clusterEntity);
        getClusterApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(MetadataHateOsLinkUtils.getMetadataHateOsLinksForClustersEndpoint(uuid), false,false));
      }
    } catch (ClustermgmtServiceException | ClustermgmtGenericException | ValidationException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      getClusterApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(getClusterApiResponse), httpStatus);
  }

  public String getCombinedFilterForListCall(String filter, HttpServletRequest request) {
    String combinedFiler = filter;
    if (filter != null && request.getAttribute("OdataFilter") != null) {
      combinedFiler = filter + " and " + request.getAttribute("OdataFilter");
    }
    else if (request.getAttribute("OdataFilter") != null) {
      combinedFiler = (String) request.getAttribute("OdataFilter");
    }
    log.info("combined filter {}", combinedFiler);
    return combinedFiler;
  }

  @Override
  public ResponseEntity<MappingJacksonValue> listClusters(Integer page, Integer limit,
                                                         String filter, String orderby,
                                                         String apply, String expand,
                                                         String select,
                                                         Map<String, String> allQueryParams,
                                                         HttpServletRequest request,
                                                         HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.OK;
    ListClustersApiResponse listClustersApiResponse = new ListClustersApiResponse();
    if (page == null || page < 0) {
      // default page number
      page = 0;
      allQueryParams.put("$page", Integer.toString(page));
    }
    if (limit == null || limit <= 0 || limit > Integer.valueOf(MAX_PAGE_SIZE)) {
      // default number of records
      limit = Integer.valueOf(DEFAULT_PAGE_SIZE);
      allQueryParams.put("$limit", Integer.toString(limit));
    }

    try {
      Pair<Integer, List<Cluster>> clusters = clusterService.getClusterEntities(
        page * limit, limit, getCombinedFilterForListCall(filter, request), orderby, apply, select, expand);
      listClustersApiResponse.setDataInWrapper(clusters.right());
      listClustersApiResponse.setMetadata(
        ClustermgmtResponseFactory.createListMetadata(clusters.left(), allQueryParams));
    } catch (ClustermgmtServiceException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      listClustersApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    } catch (ValidationException e) {
      listClustersApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = HttpStatus.BAD_REQUEST;
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(listClustersApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> getSnmpConfigByClusterId(String clusterUuid,
                                                     Map<String, String> allQueryParams,
                                                     HttpServletRequest request,
                                                     HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.OK;
    GetSnmpConfigByClusterIdApiResponse getSnmpConfigByClusterIdApiResponse = new GetSnmpConfigByClusterIdApiResponse();
    try {
      SnmpConfig snmpConfig = clusterService.getSnmpConfig(clusterUuid);
      getSnmpConfigByClusterIdApiResponse.setDataInWrapper(snmpConfig);
      getSnmpConfigByClusterIdApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(
        MetadataHateOsLinkUtils.getMetadataHateOsLinksForSnmpEndpoint(clusterUuid),
        false,false));
    } catch (ClustermgmtServiceException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      getSnmpConfigByClusterIdApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(getSnmpConfigByClusterIdApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> getConfigCredentialsByClusterId(String clusterUuid,
                                                                  Map<String, String> allQueryParams,
                                                                  HttpServletRequest request,
                                                                  HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.OK;
    GetConfigCredentialsByClusterIdApiResponse getConfigCredentialsByClusterIdApiResponse = new GetConfigCredentialsByClusterIdApiResponse();
    try {
      ConfigCredentials configCredentials = clusterService.getConfigCredentials(clusterUuid);
      getConfigCredentialsByClusterIdApiResponse.setDataInWrapper(configCredentials);
    } catch (ClustermgmtServiceException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      getConfigCredentialsByClusterIdApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(getConfigCredentialsByClusterIdApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> listRsyslogServersByClusterId(String clusterUuid,
                                                               Map<String, String> allQueryParams,
                                                               HttpServletRequest request,
                                                               HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.OK;
    ListRsyslogServersByClusterIdApiResponse listRsyslogServersByClusterIdApiResponse = new ListRsyslogServersByClusterIdApiResponse();
    try {
      List<RsyslogServer> rsyslogServers = clusterService.getRsyslogServerConfig(clusterUuid);
      listRsyslogServersByClusterIdApiResponse.setDataInWrapper(rsyslogServers);
    } catch (ClustermgmtServiceException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      listRsyslogServersByClusterIdApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(listRsyslogServersByClusterIdApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> listRackableUnitsByClusterId(String clusterUuid,
                                                              Map<String, String> allQueryParams,
                                                              HttpServletRequest request,
                                                              HttpServletResponse response) {

    HttpStatus httpStatus = HttpStatus.OK;
    ListRackableUnitsByClusterIdApiResponse listRackableUnitsByClusterIdApiResponse = new ListRackableUnitsByClusterIdApiResponse();
    try {
      List<RackableUnit> rackableUnits = clusterService.getRackableUnits(clusterUuid);
      listRackableUnitsByClusterIdApiResponse.setDataInWrapper(rackableUnits);
    } catch (ClustermgmtServiceException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      listRackableUnitsByClusterIdApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(listRackableUnitsByClusterIdApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> getRackableUnitById(String clusterUuid,
                                                             String rackableUnitExtId,
                                                             Map<String, String> allQueryParams,
                                                             HttpServletRequest request,
                                                             HttpServletResponse response) {

    HttpStatus httpStatus = HttpStatus.OK;
    GetRackableUnitApiResponse getRackableUnitApiResponse = new GetRackableUnitApiResponse();
    try {
      RackableUnit rackableUnit = clusterService.getRackableUnit(clusterUuid, rackableUnitExtId);
      getRackableUnitApiResponse.setDataInWrapper(rackableUnit);
      getRackableUnitApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(
        MetadataHateOsLinkUtils.getMetadataHateOsLinksForSelf(ClustersApiControllerInterface.GET_RACKABLE_UNIT_BY_ID_URI, clusterUuid, rackableUnitExtId),
        false,false));
    } catch (ClustermgmtServiceException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      getRackableUnitApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(getRackableUnitApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> getFaultToleranceStatusByClusterId(String clusterUuid,
                                                                           Map<String, String> allQueryParams,
                                                                           HttpServletRequest request,
                                                                           HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.OK;
    GetFaultToleranceStatusByClusterIdApiResponse getFaultToleranceStatusByClusterIdApiResponse = new GetFaultToleranceStatusByClusterIdApiResponse();
    try {
      List<DomainFaultTolerance> domainFaultTolerances
        = clusterService.getDomainFaultToleranceStatus(clusterUuid);
      MultiDomainFaultToleranceStatus multiDomainFaultToleranceStatus = new MultiDomainFaultToleranceStatus();
      multiDomainFaultToleranceStatus.setMultiDomainFaultToleranceStatus(domainFaultTolerances);
      getFaultToleranceStatusByClusterIdApiResponse.setDataInWrapper(multiDomainFaultToleranceStatus);
    } catch (ClustermgmtServiceException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      getFaultToleranceStatusByClusterIdApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(getFaultToleranceStatusByClusterIdApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> getHostById(String clusterUuid, String hostExtId,
                                                     Map<String, String> allQueryParams,
                                                     HttpServletRequest request,
                                                     HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.OK;
    GetHostApiResponse getHostApiResponse = new GetHostApiResponse();
    try {
      Host hostEntity = clusterService.getHostEntity(clusterUuid, hostExtId);
      getHostApiResponse.setDataInWrapper(hostEntity);
      getHostApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(MetadataHateOsLinkUtils.getMetadataHateOsLinksForHostsEndpoint(clusterUuid, hostExtId), false,false));
    } catch (ClustermgmtServiceException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      getHostApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(getHostApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> listHostsByClusterId(String uuid,
                                                      Integer page,
                                                      Integer limit,
                                                      String filter,
                                                      String orderby,
                                                      String apply,
                                                      String select,
                                                      Map<String, String> allQueryParams,
                                                      HttpServletRequest request,
                                                      HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.OK;
    ListHostsByClusterIdApiResponse listHostsByClusterIdApiResponse = new ListHostsByClusterIdApiResponse();
    if (page == null || page < 0) {
      // default page number
      page = 0;
      allQueryParams.put("$page", Integer.toString(page));
    }
    if (limit == null || limit <= 0 || limit > Integer.valueOf(MAX_PAGE_SIZE)) {
      // default number of records
      limit = Integer.valueOf(DEFAULT_PAGE_SIZE);
      allQueryParams.put("$limit", Integer.toString(limit));
    }

    try {
      Pair<Integer, List<Host>> hosts = clusterService.getHostEntities(uuid,page * limit, limit, filter, orderby, apply, select);
      listHostsByClusterIdApiResponse.setDataInWrapper(hosts.right());
      listHostsByClusterIdApiResponse.setMetadata(
        ClustermgmtResponseFactory.createListMetadata(hosts.left(), allQueryParams));
    } catch (ClustermgmtServiceException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      listHostsByClusterIdApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    } catch (ValidationException e) {
      listHostsByClusterIdApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = HttpStatus.BAD_REQUEST;
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(listHostsByClusterIdApiResponse), httpStatus);
  }


  @Override
  public ResponseEntity<MappingJacksonValue> listHostGpusByHostId(String clusterExtId,
                                                         String hostExtId,
                                                         Integer page,
                                                         Integer limit,
                                                         String filter,
                                                         String orderby,
                                                         String select,
                                                         Map<String, String> allQueryParams,
                                                         HttpServletRequest request,
                                                         HttpServletResponse response) {
    if (page == null || page < 0) {
      // default page number
      page = 0;
      allQueryParams.put("$page", Integer.toString(page));
    }
    if (limit == null || limit <= 0 || limit > Integer.valueOf(MAX_PAGE_SIZE)) {
      // default number of records
      limit = Integer.valueOf(DEFAULT_PAGE_SIZE);
      allQueryParams.put("$limit", Integer.toString(limit));
    }

    HttpStatus httpStatus = HttpStatus.OK;
    ListHostGpusByHostIdApiResponse listHostGpusByHostIdApiResponse = new ListHostGpusByHostIdApiResponse();
    try {
      Pair<Integer, List<HostGpu>> hostGpuEntities = clusterService.getHostGpus(clusterExtId, hostExtId, limit, page, filter, orderby, select);
      listHostGpusByHostIdApiResponse.setDataInWrapper(hostGpuEntities.right());
      listHostGpusByHostIdApiResponse.setMetadata(
        ClustermgmtResponseFactory.createListMetadata(hostGpuEntities.left(), allQueryParams));
    } catch (ClustermgmtServiceException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      listHostGpusByHostIdApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    } catch (ValidationException e) {
      listHostGpusByHostIdApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = HttpStatus.BAD_REQUEST;
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(listHostGpusByHostIdApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> listHostGpusByClusterId(String clusterExtId,
                                                                  Integer page, Integer limit,
                                                                  String filter,
                                                                  String orderby,
                                                                  String select,
                                                                  Map<String, String> allQueryParams,
                                                                  HttpServletRequest request,
                                                                  HttpServletResponse response) {
    if (page == null || page < 0) {
      // default page number
      page = 0;
      allQueryParams.put("$page", Integer.toString(page));
    }
    if (limit == null || limit <= 0 || limit > Integer.valueOf(MAX_PAGE_SIZE)) {
      // default number of records
      limit = Integer.valueOf(DEFAULT_PAGE_SIZE);
      allQueryParams.put("$limit", Integer.toString(limit));
    }

    HttpStatus httpStatus = HttpStatus.OK;
    ListHostGpusByClusterIdApiResponse listHostGpusByClusterIdApiResponse = new ListHostGpusByClusterIdApiResponse();
    try {
      Pair<Integer, List<HostGpu>> hostGpuEntities = clusterService.getClusterHostGpus(clusterExtId, limit, page, filter, orderby, select);
      listHostGpusByClusterIdApiResponse.setDataInWrapper(hostGpuEntities.right());
      listHostGpusByClusterIdApiResponse.setMetadata(
        ClustermgmtResponseFactory.createListMetadata(hostGpuEntities.left(), allQueryParams));
    } catch (ClustermgmtServiceException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      listHostGpusByClusterIdApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    } catch (ValidationException e) {
      listHostGpusByClusterIdApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = HttpStatus.BAD_REQUEST;
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(listHostGpusByClusterIdApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> listPhysicalGpuProfiles(String clusterExtId,
                                                            Integer page, Integer limit,
                                                            Map<String, String> allQueryParams,
                                                            HttpServletRequest request,
                                                            HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.OK;
    ListPhysicalGpuProfilesApiResponse listPhysicalGpuProfilesApiResponse = new ListPhysicalGpuProfilesApiResponse();
    if (page == null || page < 0) {
      // default page number
      page = 0;
      allQueryParams.put("$page", Integer.toString(page));
    }
    if (limit == null || limit <= 0 || limit > Integer.valueOf(MAX_PAGE_SIZE)) {
      // default number of records
      limit = Integer.valueOf(DEFAULT_PAGE_SIZE);
      allQueryParams.put("$limit", Integer.toString(limit));
    }
    try {
      Pair<Integer, List<PhysicalGpuProfile>> physicalGpuProfileList =
         clusterService.listPhysicalGpuProfiles(clusterExtId, limit, page);
      listPhysicalGpuProfilesApiResponse.setDataInWrapper(physicalGpuProfileList.right());
      listPhysicalGpuProfilesApiResponse.setMetadata(
        ClustermgmtResponseFactory.createListMetadata(physicalGpuProfileList.left(), allQueryParams));
    } catch (ClustermgmtServiceException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      listPhysicalGpuProfilesApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    } catch (ValidationException e) {
      listPhysicalGpuProfilesApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = HttpStatus.BAD_REQUEST;
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(listPhysicalGpuProfilesApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> listVirtualGpuProfiles(String clusterExtId,
                                                            Integer page, Integer limit,
                                                            Map<String, String> allQueryParams,
                                                            HttpServletRequest request,
                                                            HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.OK;
    ListVirtualGpuProfilesApiResponse listVirtualGpuProfilesApiResponse = new ListVirtualGpuProfilesApiResponse();
    if (page == null || page < 0) {
      // default page number
      page = 0;
      allQueryParams.put("$page", Integer.toString(page));
    }
    if (limit == null || limit <= 0 || limit > Integer.valueOf(MAX_PAGE_SIZE)) {
      // default number of records
      limit = Integer.valueOf(DEFAULT_PAGE_SIZE);
      allQueryParams.put("$limit", Integer.toString(limit));
    }
    try {
      Pair<Integer, List<VirtualGpuProfile>> virtualGpuProfileList =
         clusterService.listVirtualGpuProfiles(clusterExtId, limit, page);
      listVirtualGpuProfilesApiResponse.setDataInWrapper(virtualGpuProfileList.right());
      listVirtualGpuProfilesApiResponse.setMetadata(
        ClustermgmtResponseFactory.createListMetadata(virtualGpuProfileList.left(), allQueryParams));
    } catch (ClustermgmtServiceException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      listVirtualGpuProfilesApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    } catch (ValidationException e) {
      listVirtualGpuProfilesApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = HttpStatus.BAD_REQUEST;
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(listVirtualGpuProfilesApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> getHostGpuById(String clusterExtId, String hostExtId,
                                                        String hostGpuExtId, Map<String, String> allQueryParams,
                                                        HttpServletRequest request, HttpServletResponse response) {

    HttpStatus httpStatus = HttpStatus.OK;
    GetHostGpuApiResponse getHostGpuApiResponse = new GetHostGpuApiResponse();
    try {
      HostGpu hostGpuEntity = clusterService.getHostGpu(clusterExtId, hostExtId, hostGpuExtId);
      getHostGpuApiResponse.setDataInWrapper(hostGpuEntity);
      getHostGpuApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(
        MetadataHateOsLinkUtils.getMetadataHateOsLinksForSelf(ClustersApiControllerInterface.GET_HOST_GPU_BY_ID_URI, clusterExtId, hostExtId, hostGpuExtId),
        false,false));
    } catch (ClustermgmtServiceException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      getHostGpuApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(getHostGpuApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> renameHost(HostNameParam body,
                                                        String clusterExtId,
                                                        String hostExtId,
                                                        Map<String, String> allQueryParams,
                                                        HttpServletRequest request,
                                                        HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.ACCEPTED;
    RenameHostApiResponse renameHostApiResponse = new RenameHostApiResponse();
    try {
      TaskReference task = clusterService.renameHost(body, clusterExtId, hostExtId);
      renameHostApiResponse.setDataInWrapper(task);
      renameHostApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(MetadataHateOsLinkUtils.getMetadataHateOsLinkForTask(task), false, false));
    } catch (ClustermgmtServiceException | ValidationException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      renameHostApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(renameHostApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> updateClusterById(Cluster body,
                                                           String clusterExtId,
                                                           Map<String, String> allQueryParams,
                                                           HttpServletRequest request,
                                                           HttpServletResponse response) {

    HttpStatus httpStatus = HttpStatus.ACCEPTED;
    UpdateClusterApiResponse updateClusterApiResponse = new UpdateClusterApiResponse();
    try {
      // Validate Etag
      Cluster clusterEntity = clusterService.getClusterEntity(clusterExtId, null);
      String givenEtag = request.getHeader(IF_MATCH);
      String etag = ClustermgmtUtils.calculateEtag(clusterEntity);
      if (!givenEtag.equals(etag)) {
        throw ClustermgmtServiceException.etagMismatch(ETAG_EXCEPTION_MSG);
      }

      // Get batch task
      String batchTask = request.getHeader(BATCH_TASK_HEADER);
      TaskReference task = clusterService.updateCluster(body, clusterExtId, batchTask);
      updateClusterApiResponse.setDataInWrapper(task);
      updateClusterApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(MetadataHateOsLinkUtils.getMetadataHateOsLinkForTask(task), false, false));
    }
    catch (ClustermgmtServiceException | ClustermgmtGenericException | ValidationException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      updateClusterApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(updateClusterApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> updateSnmpStatus(SnmpStatusParam body,
                                                              String clusterExtId,
                                                              Map<String, String> allQueryParams,
                                                              HttpServletRequest request,
                                                              HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.ACCEPTED;
    UpdateSnmpStatusApiResponse updateSnmpStatusApiResponse = new UpdateSnmpStatusApiResponse();
    try {
      // Get batch task
      String batchTask = request.getHeader(BATCH_TASK_HEADER);
      TaskReference task = clusterService.updateSnmpStatus(body, clusterExtId, batchTask);
      updateSnmpStatusApiResponse.setDataInWrapper(task);
      updateSnmpStatusApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(MetadataHateOsLinkUtils.getMetadataHateOsLinkForTask(task), false,false));
    }
    catch (ClustermgmtServiceException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      updateSnmpStatusApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(updateSnmpStatusApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> addSnmpTransport(SnmpTransport body,
                                                              String clusterExtId,
                                                              Map<String, String> allQueryParams,
                                                              HttpServletRequest request,
                                                              HttpServletResponse response) {

    HttpStatus httpStatus = HttpStatus.ACCEPTED;
    AddSnmpTransportsApiResponse addSnmpTransportsTaskApiResponse = new AddSnmpTransportsApiResponse();
    try {
      // Get batch task
      String batchTask = request.getHeader(BATCH_TASK_HEADER);
      TaskReference task = clusterService.addSnmpTransport(body, clusterExtId, batchTask);
      addSnmpTransportsTaskApiResponse.setDataInWrapper(task);
      addSnmpTransportsTaskApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(MetadataHateOsLinkUtils.getMetadataHateOsLinkForTask(task),false,false));
    }
    catch (ClustermgmtServiceException | ValidationException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      addSnmpTransportsTaskApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(addSnmpTransportsTaskApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> removeSnmpTransport(SnmpTransport body,
                                                                 String clusterExtId,
                                                                 Map<String, String> allQueryParams,
                                                                 HttpServletRequest request,
                                                                 HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.ACCEPTED;
    RemoveSnmpTransportsApiResponse removeSnmpTransportsTaskApiResponse = new RemoveSnmpTransportsApiResponse();
    try {
      String batchTask = request.getHeader(BATCH_TASK_HEADER);
      TaskReference task = clusterService.removeSnmpTransport(body, clusterExtId, batchTask);
      removeSnmpTransportsTaskApiResponse.setDataInWrapper(task);
      removeSnmpTransportsTaskApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(MetadataHateOsLinkUtils.getMetadataHateOsLinkForTask(task),false,false));
    }
    catch (ClustermgmtServiceException | ValidationException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      removeSnmpTransportsTaskApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(removeSnmpTransportsTaskApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> createSnmpUser(SnmpUser body,
                                                         String clusterExtId,
                                                         Map<String, String> allQueryParams,
                                                         HttpServletRequest request,
                                                         HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.ACCEPTED;
    CreateSnmpUserApiResponse createSnmpUserApiResponse = new CreateSnmpUserApiResponse();
    try {
      // Get batch task
      String batchTask = request.getHeader(BATCH_TASK_HEADER);
      TaskReference task = clusterService.addSnmpUser(body, clusterExtId, batchTask);
      createSnmpUserApiResponse.setDataInWrapper(task);
      createSnmpUserApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(MetadataHateOsLinkUtils.getMetadataHateOsLinkForTask(task),false,false));
    }
    catch (ClustermgmtServiceException | ValidationException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      createSnmpUserApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(createSnmpUserApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> getSnmpUserById(String clusterExtId,
                                                         String userExtId,
                                                         Map<String, String> allQueryParams,
                                                         HttpServletRequest request,
                                                         HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.OK;
    GetSnmpUserApiResponse getSnmpUserApiResponse = new GetSnmpUserApiResponse();
    try {
      SnmpUser snmpUser = clusterService.getSnmpUser(clusterExtId, userExtId);
      // Etag
      String etag = ClustermgmtUtils.calculateEtag(snmpUser);
      response.setHeader(ETAG, etag);
      httpStatus = EtagUtils.getHttpStatus(request, etag);

      if (httpStatus != HttpStatus.NOT_MODIFIED) {
        getSnmpUserApiResponse.setDataInWrapper(snmpUser);
        getSnmpUserApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(
          MetadataHateOsLinkUtils.getMetadataHateOsLinksForSelf(ClustersApiControllerInterface.GET_SNMP_USER_BY_ID_URI, clusterExtId, userExtId),
          false,false));
      }
    } catch (ClustermgmtServiceException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      getSnmpUserApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(getSnmpUserApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> updateSnmpUserById(SnmpUser body,
                                                            String clusterExtId,
                                                            String userExtId,
                                                            Map<String, String> allQueryParams,
                                                            HttpServletRequest request,
                                                            HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.ACCEPTED;
    UpdateSnmpUserApiResponse updateSnmpUserApiResponse = new UpdateSnmpUserApiResponse();
    try {
      // Validate Etag
      SnmpUser snmpUser = clusterService.getSnmpUser(clusterExtId, userExtId);
      String givenEtag = request.getHeader(IF_MATCH);
      String etag = ClustermgmtUtils.calculateEtag(snmpUser);
      if (!givenEtag.equals(etag)) {
        throw ClustermgmtServiceException.etagMismatch(ETAG_EXCEPTION_MSG);
      }

      // Get batch task
      String batchTask = request.getHeader(BATCH_TASK_HEADER);
      TaskReference task = clusterService.updateSnmpUser(body, clusterExtId, userExtId, batchTask);
      updateSnmpUserApiResponse.setDataInWrapper(task);
      updateSnmpUserApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(MetadataHateOsLinkUtils.getMetadataHateOsLinkForTask(task),false,false));
    }
    catch (ClustermgmtServiceException | ValidationException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      updateSnmpUserApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(updateSnmpUserApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> deleteSnmpUserById(String clusterExtId,
                                                            String userExtId,
                                                            Map<String, String> allQueryParams,
                                                            HttpServletRequest request,
                                                            HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.ACCEPTED;
    DeleteSnmpUserApiResponse deleteSnmpUserApiResponse = new DeleteSnmpUserApiResponse();
    try {
      String batchTask = request.getHeader(BATCH_TASK_HEADER);
      TaskReference task = clusterService.deleteSnmpUser(clusterExtId, userExtId, batchTask);
      deleteSnmpUserApiResponse.setDataInWrapper(task);
      deleteSnmpUserApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(MetadataHateOsLinkUtils.getMetadataHateOsLinkForTask(task),false,false));
    }
    catch (ClustermgmtServiceException | ValidationException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      deleteSnmpUserApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(deleteSnmpUserApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> getSnmpTrapById(String clusterExtId,
                                                         String trapExtId,
                                                         Map<String, String> allQueryParams,
                                                         HttpServletRequest request,
                                                         HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.OK;
    GetSnmpTrapApiResponse getSnmpTrapApiResponse = new GetSnmpTrapApiResponse();
    try {
      SnmpTrap snmpTrap = clusterService.getSnmpTrap(clusterExtId, trapExtId);
      // Etag
      String etag = ClustermgmtUtils.calculateEtag(snmpTrap);
      response.setHeader(ETAG, etag);
      httpStatus = EtagUtils.getHttpStatus(request, etag);

      if (httpStatus != HttpStatus.NOT_MODIFIED) {
        getSnmpTrapApiResponse.setDataInWrapper(snmpTrap);
        getSnmpTrapApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(
          MetadataHateOsLinkUtils.getMetadataHateOsLinksForSelf(ClustersApiControllerInterface.GET_SNMP_TRAP_BY_ID_URI, clusterExtId, trapExtId),
          false,false));
      }
    } catch (ClustermgmtServiceException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      getSnmpTrapApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(getSnmpTrapApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> createSnmpTrap(SnmpTrap body,
                                                         String clusterExtId,
                                                         Map<String, String> allQueryParams,
                                                         HttpServletRequest request,
                                                         HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.ACCEPTED;
    CreateSnmpTrapApiResponse createSnmpTrapApiResponse = new CreateSnmpTrapApiResponse();
    try {
      // Get batch task
      String batchTask = request.getHeader(BATCH_TASK_HEADER);
      TaskReference task = clusterService.addSnmpTrap(body, clusterExtId, batchTask);
      createSnmpTrapApiResponse.setDataInWrapper(task);
      createSnmpTrapApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(MetadataHateOsLinkUtils.getMetadataHateOsLinkForTask(task),false,false));
    }
    catch (ClustermgmtServiceException | ValidationException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      createSnmpTrapApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(createSnmpTrapApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> updateSnmpTrapById(SnmpTrap body,
                                                            String clusterExtId,
                                                            String trapExtId,
                                                            Map<String, String> allQueryParams,
                                                            HttpServletRequest request,
                                                            HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.ACCEPTED;
    UpdateSnmpTrapApiResponse updateSnmpTrapApiResponse = new UpdateSnmpTrapApiResponse();
    try {
      // Validate Etag
      SnmpTrap snmpTrap = clusterService.getSnmpTrap(clusterExtId, trapExtId);
      String givenEtag = request.getHeader(IF_MATCH);
      String etag = ClustermgmtUtils.calculateEtag(snmpTrap);
      if (!givenEtag.equals(etag)) {
        throw ClustermgmtServiceException.etagMismatch(ETAG_EXCEPTION_MSG);
      }
      // Get batch task
      String batchTask = request.getHeader(BATCH_TASK_HEADER);
      TaskReference task = clusterService.updateSnmpTrap(body, clusterExtId, trapExtId, batchTask);
      updateSnmpTrapApiResponse.setDataInWrapper(task);
      updateSnmpTrapApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(MetadataHateOsLinkUtils.getMetadataHateOsLinkForTask(task),false,false));
    }
    catch (ClustermgmtServiceException | ValidationException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      updateSnmpTrapApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(updateSnmpTrapApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> deleteSnmpTrapById(String clusterExtId,
                                                            String trapExtId,
                                                            Map<String, String> allQueryParams,
                                                            HttpServletRequest request,
                                                            HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.ACCEPTED;
    DeleteSnmpTrapApiResponse deleteSnmpTrapApiResponse = new DeleteSnmpTrapApiResponse();
    try {
      String batchTask = request.getHeader(BATCH_TASK_HEADER);
      TaskReference task = clusterService.deleteSnmpTrap(clusterExtId, trapExtId, batchTask);
      deleteSnmpTrapApiResponse.setDataInWrapper(task);
      deleteSnmpTrapApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(MetadataHateOsLinkUtils.getMetadataHateOsLinkForTask(task),false,false));
    }
    catch (ClustermgmtServiceException | ValidationException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      deleteSnmpTrapApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(deleteSnmpTrapApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> getRsyslogServerById(String clusterExtId,
                                                              String rsyslogServerExtId,
                                                              Map<String, String> allQueryParams,
                                                              HttpServletRequest request,
                                                              HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.OK;
    GetRsyslogServerApiResponse getRsyslogServerResponse = new GetRsyslogServerApiResponse();
    try {
      RsyslogServer rsyslog = clusterService.getRsyslogServer(clusterExtId, rsyslogServerExtId);
      // Etag
      String etag = ClustermgmtUtils.calculateEtag(rsyslog);
      response.setHeader(ETAG, etag);
      httpStatus = EtagUtils.getHttpStatus(request, etag);

      if (httpStatus != HttpStatus.NOT_MODIFIED) {
        getRsyslogServerResponse.setDataInWrapper(rsyslog);
        getRsyslogServerResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(
          MetadataHateOsLinkUtils.getMetadataHateOsLinksForSelf(ClustersApiControllerInterface.GET_RSYSLOG_SERVER_BY_ID_URI, clusterExtId, rsyslogServerExtId),
          false,false));
      }
    } catch (ClustermgmtServiceException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      getRsyslogServerResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(getRsyslogServerResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> createRsyslogServer(RsyslogServer body,
                                                              String clusterExtId,
                                                              Map<String, String> allQueryParams,
                                                              HttpServletRequest request,
                                                              HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.ACCEPTED;
    CreateRsyslogServerApiResponse createRsyslogServerApiResponse = new CreateRsyslogServerApiResponse();
    try {
      // Get batch task
      String batchTask = request.getHeader(BATCH_TASK_HEADER);
      TaskReference task = clusterService.addRsyslogServer(body, clusterExtId, batchTask);
      createRsyslogServerApiResponse.setDataInWrapper(task);
      createRsyslogServerApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(MetadataHateOsLinkUtils.getMetadataHateOsLinkForTask(task),false,false));
    }
    catch (ClustermgmtServiceException | ValidationException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      createRsyslogServerApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(createRsyslogServerApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> updateRsyslogServerById(RsyslogServer body,
                                                                 String clusterExtId,
                                                                 String rsyslogServerExtId,
                                                                 Map<String, String> allQueryParams,
                                                                 HttpServletRequest request,
                                                                 HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.ACCEPTED;
    UpdateRsyslogServerApiResponse updateRsyslogServerApiResponse = new UpdateRsyslogServerApiResponse();
    try {
      // Validate Etag
      RsyslogServer rsyslogServer = clusterService.getRsyslogServer(clusterExtId, rsyslogServerExtId);
      String givenEtag = request.getHeader(IF_MATCH);
      String etag = ClustermgmtUtils.calculateEtag(rsyslogServer);
      if (!givenEtag.equals(etag)) {
        throw ClustermgmtServiceException.etagMismatch(ETAG_EXCEPTION_MSG);
      }
      // Get batch task
      String batchTask = request.getHeader(BATCH_TASK_HEADER);
      TaskReference task = clusterService.updateRsyslogServer(body, clusterExtId, rsyslogServerExtId, batchTask);
      updateRsyslogServerApiResponse.setDataInWrapper(task);
      updateRsyslogServerApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(MetadataHateOsLinkUtils.getMetadataHateOsLinkForTask(task),false,false));
    }
    catch (ClustermgmtServiceException | ValidationException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      updateRsyslogServerApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(updateRsyslogServerApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> deleteRsyslogServerById(String clusterExtId,
                                                                 String rsyslogServerExtId,
                                                                 Map<String, String> allQueryParams,
                                                                 HttpServletRequest request,
                                                                 HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.ACCEPTED;
    DeleteRsyslogServerApiResponse deleteRsyslogServerApiResponse = new DeleteRsyslogServerApiResponse();
    try {
      // Get batch task
      String batchTask = request.getHeader(BATCH_TASK_HEADER);
      TaskReference task = clusterService.deleteRsyslogServer(rsyslogServerExtId, clusterExtId, batchTask);
      deleteRsyslogServerApiResponse.setDataInWrapper(task);
      deleteRsyslogServerApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(MetadataHateOsLinkUtils.getMetadataHateOsLinkForTask(task),false,false));
    } catch (ClustermgmtServiceException | ValidationException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      deleteRsyslogServerApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(deleteRsyslogServerApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> listHosts(Integer page, Integer limit,
                                                         String filter, String orderby,
                                                         String apply,
                                                         String select,
                                                         Map<String, String> allQueryParams,
                                                         HttpServletRequest request,
                                                         HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.OK;
    ListHostsApiResponse listHostsApiResponse = new ListHostsApiResponse();
    if (page == null || page < 0) {
      // default page number
      page = 0;
      allQueryParams.put("$page", Integer.toString(page));
    }
    if (limit == null || limit <= 0 || limit > Integer.valueOf(MAX_PAGE_SIZE)) {
      // default number of records
      limit = Integer.valueOf(DEFAULT_PAGE_SIZE);
      allQueryParams.put("$limit", Integer.toString(limit));
    }

    try {
      Pair<Integer, List<Host>> hosts = clusterService.getAllHostEntities(page * limit, limit,
        getCombinedFilterForListCall(filter, request), orderby, apply, select);
      listHostsApiResponse.setDataInWrapper(hosts.right());
      listHostsApiResponse.setMetadata(
        ClustermgmtResponseFactory.createListMetadata(hosts.left(), allQueryParams));
    } catch (ClustermgmtServiceException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      listHostsApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    } catch (ValidationException e) {
      listHostsApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = HttpStatus.BAD_REQUEST;
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(listHostsApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> listHostGpus(Integer page,
                                                            Integer limit,
                                                            String filter,
                                                            String orderby,
                                                            String select,
                                                            Map<String, String> allQueryParams,
                                                            HttpServletRequest request,
                                                            HttpServletResponse response) {
    if (page == null || page < 0) {
      // default page number
      page = 0;
      allQueryParams.put("$page", Integer.toString(page));
    }
    if (limit == null || limit <= 0 || limit > Integer.valueOf(MAX_PAGE_SIZE)) {
      // default number of records
      limit = Integer.valueOf(DEFAULT_PAGE_SIZE);
      allQueryParams.put("$limit", Integer.toString(limit));
    }

    HttpStatus httpStatus = HttpStatus.OK;
    ListHostGpusApiResponse listHostGpusApiResponse = new ListHostGpusApiResponse();
    try {
      Pair<Integer, List<HostGpu>> hostGpuEntities = clusterService.getAllHostGpus(limit, page,
        getCombinedFilterForListCall(filter, request), orderby, select);
      listHostGpusApiResponse.setDataInWrapper(hostGpuEntities.right());
      listHostGpusApiResponse.setMetadata(
        ClustermgmtResponseFactory.createListMetadata(hostGpuEntities.left(), allQueryParams));
    } catch (ClustermgmtServiceException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      listHostGpusApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    } catch (ValidationException e) {
      listHostGpusApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = HttpStatus.BAD_REQUEST;
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(listHostGpusApiResponse), httpStatus);
  }

  public ResponseEntity<MappingJacksonValue> discoverUnconfiguredNodes(NodeDiscoveryParams body,
                                                                       String clusterExtId,
                                                                       Map<String, String> allQueryParams,
                                                                       HttpServletRequest request,
                                                                       HttpServletResponse response) {

    HttpStatus httpStatus = HttpStatus.ACCEPTED;
    DiscoverUnconfiguredNodesApiResponse discoverUnconfiguredNodesApiResponse = new DiscoverUnconfiguredNodesApiResponse();
    try {
      TaskReference task = clusterService.discoverUnconfiguredNodes(clusterExtId, body);
      discoverUnconfiguredNodesApiResponse.setDataInWrapper(task);
      discoverUnconfiguredNodesApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(MetadataHateOsLinkUtils.getMetadataHateOsLinkForTask(task),false,false));
    }
    catch (ClustermgmtServiceException | ValidationException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      discoverUnconfiguredNodesApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(discoverUnconfiguredNodesApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> fetchNodeNetworkingDetails(NodeDetails body,
                                                                    String clusterExtId,
                                                                    Map<String, String> allQueryParams,
                                                                    HttpServletRequest request,
                                                                    HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.ACCEPTED;
    FetchNodeNetworkingDetailsApiResponse fetchNodeNetworkingDetailsApiResponse =
      new FetchNodeNetworkingDetailsApiResponse();
    try {
      TaskReference task = clusterService.getNodeNetworkingDetails(clusterExtId, body);
      fetchNodeNetworkingDetailsApiResponse.setDataInWrapper(task);
      fetchNodeNetworkingDetailsApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(MetadataHateOsLinkUtils.getMetadataHateOsLinkForTask(task),false,false));
    }
    catch (ClustermgmtServiceException | ValidationException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      fetchNodeNetworkingDetailsApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(fetchNodeNetworkingDetailsApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> expandCluster(ExpandClusterParams body,
                                                     String clusterExtId,
                                                     Map<String, String> allQueryParams,
                                                     HttpServletRequest request,
                                                     HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.ACCEPTED;
    ExpandClusterApiResponse expandClusterApiResponse = new ExpandClusterApiResponse();
    try {
      final Pair<String, Boolean> taskInfo = clustermgmtIdempotentTokenFactory.getIdempotentTaskInfo(request);
      final String taskUuid = taskInfo.left();
      final boolean taskAlreadyExists = taskInfo.right();
      if (!taskAlreadyExists) {
        clusterService.addNode(clusterExtId, body, taskUuid);
      }
      TaskReference task = new TaskReference(ErgonTaskUtils.getTaskReferenceExtId(taskUuid));
      expandClusterApiResponse.setDataInWrapper(task);
      expandClusterApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(MetadataHateOsLinkUtils.getMetadataHateOsLinkForTask(task),false,false));
    }
    catch (ClustermgmtServiceException | ValidationException | IdempotencySupportException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      expandClusterApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(expandClusterApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> checkHypervisorRequirements(HypervisorUploadParam body,
                                                                        String clusterExtId,
                                                                        Map<String, String> allQueryParams,
                                                                        HttpServletRequest request,
                                                                        HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.ACCEPTED;
    CheckHypervisorRequirementsApiResponse checkHypervisorRequirementsApiResponse = new CheckHypervisorRequirementsApiResponse();
    try {
      TaskReference task = clusterService.isHypervisorUploadRequired(clusterExtId, body);
      checkHypervisorRequirementsApiResponse.setDataInWrapper(task);
      checkHypervisorRequirementsApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(MetadataHateOsLinkUtils.getMetadataHateOsLinkForTask(task),false,false));
    }
    catch (ClustermgmtServiceException | ValidationException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      checkHypervisorRequirementsApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(checkHypervisorRequirementsApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> removeNode(NodeRemovalParams body, String clusterExtId,
                                                        Map<String, String> allQueryParams,
                                                        HttpServletRequest request,
                                                        HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.ACCEPTED;
    RemoveNodeApiResponse removeNodeApiResponse = new RemoveNodeApiResponse();
    try {
      final Pair<String, Boolean> taskInfo = clustermgmtIdempotentTokenFactory.getIdempotentTaskInfo(request);
      final String taskUuid = taskInfo.left();
      final boolean taskAlreadyExists = taskInfo.right();
      if (!taskAlreadyExists) {
        clusterService.removeNode(clusterExtId, body, taskUuid);
      }
      TaskReference task = new TaskReference(ErgonTaskUtils.getTaskReferenceExtId(taskUuid));
      removeNodeApiResponse.setDataInWrapper(task);
      removeNodeApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(MetadataHateOsLinkUtils.getMetadataHateOsLinkForTask(task),false,false));
    }
    catch (ClustermgmtServiceException | ValidationException | IdempotencySupportException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      removeNodeApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(removeNodeApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> validateNode(ValidateNodeParam body,
                                                          String clusterExtId,
                                                          Map<String, String> allQueryParams,
                                                          HttpServletRequest request,
                                                          HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.ACCEPTED;
    ValidateNodeApiResponse validateNodeApiResponse = new ValidateNodeApiResponse();
    try {
      TaskReference task = clusterService.validateNode(clusterExtId, body);
      validateNodeApiResponse.setDataInWrapper(task);
      validateNodeApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(MetadataHateOsLinkUtils.getMetadataHateOsLinkForTask(task),false,false));
    }
    catch (ClustermgmtServiceException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      validateNodeApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(validateNodeApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> fetchTaskResponse(SearchParams body,
                                                        String taskExtId,
                                                        Map<String, String> allQueryParams,
                                                        HttpServletRequest request,
                                                        HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.OK;
    FetchTaskApiResponse fetchTaskApiResponse = new FetchTaskApiResponse();
    try {
      SearchResponse searchResponse = clusterService.getSearchResponse(taskExtId, body.getSearchType());
      fetchTaskApiResponse.setDataInWrapper(searchResponse);
    } catch (ClustermgmtServiceException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      fetchTaskApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(fetchTaskApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> listHostNicsByHostId(String clusterExtId,
                                                         String hostExtId,
                                                         Integer page,
                                                         Integer limit,
                                                         String filter,
                                                         String orderby,
                                                         String select,
                                                         Map<String, String> allQueryParams,
                                                         HttpServletRequest request,
                                                         HttpServletResponse response) {
    if (page == null || page < 0) {
      // default page number
      page = 0;
      allQueryParams.put("$page", Integer.toString(page));
    }
    if (limit == null || limit <= 0 || limit > Integer.valueOf(MAX_PAGE_SIZE)) {
      // default number of records
      limit = Integer.valueOf(DEFAULT_PAGE_SIZE);
      allQueryParams.put("$limit", Integer.toString(limit));
    }

    HttpStatus httpStatus = HttpStatus.OK;
    ListHostNicsByHostIdApiResponse listHostNicsByHostIdApiResponse = new ListHostNicsByHostIdApiResponse();
    try {
      Pair<Integer, List<HostNic>> hostNicEntities = clusterService.getHostNics(clusterExtId, hostExtId, limit, page, filter, orderby, select);
      listHostNicsByHostIdApiResponse.setDataInWrapper(hostNicEntities.right());
      listHostNicsByHostIdApiResponse.setMetadata(
              ClustermgmtResponseFactory.createListMetadata(hostNicEntities.left(), allQueryParams));
    } catch (ClustermgmtServiceException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      listHostNicsByHostIdApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    } catch (ValidationException e) {
      listHostNicsByHostIdApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = HttpStatus.BAD_REQUEST;
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(listHostNicsByHostIdApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> listHostNics(Integer page,
                                                        Integer limit,
                                                        String filter,
                                                        String orderby,
                                                        String select,
                                                        Map<String, String> allQueryParams,
                                                        HttpServletRequest request,
                                                        HttpServletResponse response) {
    if (page == null || page < 0) {
      // default page number
      page = 0;
      allQueryParams.put("$page", Integer.toString(page));
    }
    if (limit == null || limit <= 0 || limit > Integer.valueOf(MAX_PAGE_SIZE)) {
      // default number of records
      limit = Integer.valueOf(DEFAULT_PAGE_SIZE);
      allQueryParams.put("$limit", Integer.toString(limit));
    }

    HttpStatus httpStatus = HttpStatus.OK;
    ListHostNicsApiResponse listHostNicsResponse = new ListHostNicsApiResponse();
    try {
      Pair<Integer, List<HostNic>> hostNicEntities = clusterService.getAllHostNics(limit, page, filter, orderby, select);
      listHostNicsResponse.setDataInWrapper(hostNicEntities.right());
      listHostNicsResponse.setMetadata(
              ClustermgmtResponseFactory.createListMetadata(hostNicEntities.left(), allQueryParams));
    } catch (ClustermgmtServiceException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      listHostNicsResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    } catch (ValidationException e) {
      log.error("Failed to validate the request");
      listHostNicsResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = HttpStatus.BAD_REQUEST;
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(listHostNicsResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> getHostNicById(String clusterExtId, String hostExtId, String hostNicExtId,
                                                        Map<String, String> allQueryParams,
                                                        HttpServletRequest request, HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.OK;
    GetHostNicApiResponse getHostNicApiResponse = new GetHostNicApiResponse();
    try {
      HostNic hostNicEntity = clusterService.getHostNic(clusterExtId, hostExtId, hostNicExtId);
      getHostNicApiResponse.setDataInWrapper(hostNicEntity);
      getHostNicApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(
        MetadataHateOsLinkUtils.getMetadataHateOsLinksForHostNicEndpoint(clusterExtId, hostExtId, hostNicExtId),false,false));
    } catch (ClustermgmtServiceException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      getHostNicApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(getHostNicApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> listVirtualNicsByHostId(String clusterExtId,
                                                         String hostExtId,
                                                         Integer page,
                                                         Integer limit,
                                                         String filter,
                                                         String orderby,
                                                            String select,
                                                         Map<String, String> allQueryParams,
                                                         HttpServletRequest request,
                                                         HttpServletResponse response) {
    if (page == null || page < 0) {
      // default page number
      page = 0;
      allQueryParams.put("$page", Integer.toString(page));
    }
    if (limit == null || limit <= 0 || limit > Integer.valueOf(MAX_PAGE_SIZE)) {
      // default number of records
      limit = Integer.valueOf(DEFAULT_PAGE_SIZE);
      allQueryParams.put("$limit", Integer.toString(limit));
    }

    HttpStatus httpStatus = HttpStatus.OK;
    ListVirtualNicsByHostIdApiResponse listVirtualNicsByHostIdApiResponse = new ListVirtualNicsByHostIdApiResponse();
    try {
      Pair<Integer, List<VirtualNic>> virtualNicEntities = clusterService.getVirtualNics(clusterExtId, hostExtId, limit, page, filter, orderby, select);
      listVirtualNicsByHostIdApiResponse.setDataInWrapper(virtualNicEntities.right());
      listVirtualNicsByHostIdApiResponse.setMetadata(
        ClustermgmtResponseFactory.createListMetadata(virtualNicEntities.left(), allQueryParams));
    } catch (ClustermgmtServiceException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      listVirtualNicsByHostIdApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    } catch (ValidationException e) {
      listVirtualNicsByHostIdApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = HttpStatus.BAD_REQUEST;
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(listVirtualNicsByHostIdApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> getVirtualNicById(String clusterExtId, String hostExtId, String virtualNicExtId,
                                                           Map<String, String> allQueryParams,
                                                           HttpServletRequest request, HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.OK;
    GetVirtualNicApiResponse getVirtualNicApiResponse = new GetVirtualNicApiResponse();
    try {
      VirtualNic virtualNicEntity = clusterService.getVirtualNic(clusterExtId, hostExtId, virtualNicExtId);
      getVirtualNicApiResponse.setDataInWrapper(virtualNicEntity);
      getVirtualNicApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(
        MetadataHateOsLinkUtils.getMetadataHateOsLinksForSelf(ClustersApiControllerInterface.GET_VIRTUAL_NIC_BY_ID_URI, clusterExtId, hostExtId, virtualNicExtId),
        false,false));
    } catch (ClustermgmtServiceException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      getVirtualNicApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(getVirtualNicApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> createCluster(Cluster body, Boolean $dryrun, Map<String, String> allQueryParams,
                                                           HttpServletRequest request, HttpServletResponse response)
  {
    HttpStatus httpStatus = HttpStatus.ACCEPTED;
    CreateClusterApiResponse clusterCreateApiResponse = new CreateClusterApiResponse();
    try{
      final Pair<String, Boolean> taskInfo = clustermgmtIdempotentTokenFactory.getIdempotentTaskInfo(request);
      final String taskUuid = taskInfo.left();
      final boolean taskAlreadyExists = taskInfo.right();
      if (!taskAlreadyExists) {
        clusterService.clusterCreate(body, taskUuid, $dryrun);
      }
      TaskReference task = new TaskReference(ErgonTaskUtils.getTaskReferenceExtId(taskUuid));
      clusterCreateApiResponse.setDataInWrapper(task);
      clusterCreateApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(MetadataHateOsLinkUtils.getMetadataHateOsLinkForTask(task), false, false));
    }
    catch (ClustermgmtServiceException | IdempotencySupportException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      clusterCreateApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(clusterCreateApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> deleteClusterById(String extId, Boolean $dryrun,
                                                            Map<String, String> allQueryParams,
                                                            HttpServletRequest request,
                                                            HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.ACCEPTED;
    DeleteClusterApiResponse deleteClusterApiResponse = new DeleteClusterApiResponse();
    try{
      final Pair<String, Boolean> taskInfo = clustermgmtIdempotentTokenFactory.getIdempotentTaskInfo(request);
      final String taskUuid = taskInfo.left();
      final boolean taskAlreadyExists = taskInfo.right();
      if (!taskAlreadyExists) {
        clusterService.clusterDestroy(extId, taskUuid, $dryrun);
      }
      TaskReference task = new TaskReference(ErgonTaskUtils.getTaskReferenceExtId(taskUuid));
      deleteClusterApiResponse.setDataInWrapper(task);
      deleteClusterApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(MetadataHateOsLinkUtils.getMetadataHateOsLinkForTask(task), false, false));
    }
    catch(ClustermgmtServiceException | IdempotencySupportException | ClustermgmtGenericException e){
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      deleteClusterApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(deleteClusterApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> associateCategoriesToCluster(CategoryEntityReferences body,
                                                                          String clusterExtId,
                                                                          Map<String, String> allQueryParams,
                                                                          HttpServletRequest request,
                                                                          HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.ACCEPTED;
    AssociateCategoriesToClusterApiResponse associateCategoriesToClusterApiResponse = new AssociateCategoriesToClusterApiResponse();
    try{
      TaskReference task = clusterService.updateCategoryAssociationsForClusterEntity(body, clusterExtId, "attach");
      associateCategoriesToClusterApiResponse.setDataInWrapper(task);
      associateCategoriesToClusterApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(MetadataHateOsLinkUtils.getMetadataHateOsLinkForTask(task), false, false));
    }
    catch(ClustermgmtServiceException | ClustermgmtGenericException e){
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      associateCategoriesToClusterApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(associateCategoriesToClusterApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> disassociateCategoriesFromCluster(CategoryEntityReferences body,
                                                                           String clusterExtId,
                                                                           Map<String, String> allQueryParams,
                                                                           HttpServletRequest request,
                                                                           HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.ACCEPTED;
    DisassociateCategoriesFromClusterApiResponse disassociateCategoriesFromClusterApiResponse = new DisassociateCategoriesFromClusterApiResponse();
    try{
      TaskReference task = clusterService.updateCategoryAssociationsForClusterEntity(body, clusterExtId, "detach");
      disassociateCategoriesFromClusterApiResponse.setDataInWrapper(task);
      disassociateCategoriesFromClusterApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(MetadataHateOsLinkUtils.getMetadataHateOsLinkForTask(task), false, false));
    }
    catch(ClustermgmtServiceException | ClustermgmtGenericException e){
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      disassociateCategoriesFromClusterApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(disassociateCategoriesFromClusterApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> associateCategoriesToHostNic(CategoryEntityReferences body,
                                                                        String clusterExtId,
                                                                        String hostExtId,
                                                                        String nicExtId,
                                                                        Map<String, String> allQueryParams,
                                                                        HttpServletRequest request,
                                                                        HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.ACCEPTED;
    AssociateCategoriesToHostNicApiResponse associateCategoriesToHostNicApiResponse = new AssociateCategoriesToHostNicApiResponse();
    try{
      TaskReference task = clusterService.updateCategoryAssociationsForHostNicEntity(body, nicExtId, clusterExtId, hostExtId, "attach");
      associateCategoriesToHostNicApiResponse.setDataInWrapper(task);
      associateCategoriesToHostNicApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(MetadataHateOsLinkUtils.getMetadataHateOsLinkForTask(task), false, false));
    }
    catch(ClustermgmtServiceException | ClustermgmtGenericException e){
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      associateCategoriesToHostNicApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(associateCategoriesToHostNicApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> disassociateCategoriesFromHostNic(CategoryEntityReferences body,
                                                                          String clusterExtId,
                                                                          String hostExtId,
                                                                          String nicExtId,
                                                                          Map<String, String> allQueryParams,
                                                                          HttpServletRequest request,
                                                                          HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.ACCEPTED;
    DisassociateCategoriesFromHostNicApiResponse disassociateCategoriesFromHostNicApiResponse = new DisassociateCategoriesFromHostNicApiResponse();
    try{
      TaskReference task = clusterService.updateCategoryAssociationsForHostNicEntity(body, nicExtId, clusterExtId, hostExtId, "detach");
      disassociateCategoriesFromHostNicApiResponse.setDataInWrapper(task);
      disassociateCategoriesFromHostNicApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(MetadataHateOsLinkUtils.getMetadataHateOsLinkForTask(task), false, false));
    }
    catch(ClustermgmtServiceException | ClustermgmtGenericException e){
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      disassociateCategoriesFromHostNicApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(disassociateCategoriesFromHostNicApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> computeNonMigratableVms(ComputeNonMigratableVmsSpec body,
                                                                      String clusterExtId,
                                                                      Map<String, String> allQueryParams,
                                                                      HttpServletRequest request,
                                                                      HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.ACCEPTED;
    ComputeNonMigratableVmsApiResponse computeNonMigratableVmsApiResponse = new ComputeNonMigratableVmsApiResponse();
    try {
      final Pair<String, Boolean> taskInfo = clustermgmtIdempotentTokenFactory.getIdempotentTaskInfo(request);
      final String taskUuid = taskInfo.left();
      final boolean taskAlreadyExists = taskInfo.right();
      if (!taskAlreadyExists) {
        clusterService.computeNonMigratableVms(clusterExtId, body, taskUuid);
      }
      TaskReference task = new TaskReference(ErgonTaskUtils.getTaskReferenceExtId(taskUuid));
      computeNonMigratableVmsApiResponse.setDataInWrapper(task);
      computeNonMigratableVmsApiResponse.setMetadata(ClustermgmtResponseFactory.createMetadataFor(MetadataHateOsLinkUtils.getMetadataHateOsLinkForTask(task),false,false));
    }
    catch (ClustermgmtServiceException | ValidationException | IdempotencySupportException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      computeNonMigratableVmsApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(computeNonMigratableVmsApiResponse), httpStatus);
  }

  @Override
  public ResponseEntity<MappingJacksonValue> getNonMigratableVmsResultById(String clusterExtId, String extId,
                                                                      Map<String, String> allQueryParams,
                                                                      HttpServletRequest request,
                                                                      HttpServletResponse response) {
    HttpStatus httpStatus = HttpStatus.OK;
    GetNonMigratableVmsResultApiResponse getNonMigratableVmsResultApiResponse = new GetNonMigratableVmsResultApiResponse();
    try {
      NonMigratableVmsResult nonMigratableVmsResult = clusterService.getNonMigratableVmsResult(extId);
      getNonMigratableVmsResultApiResponse.setDataInWrapper(nonMigratableVmsResult);
    } catch (ClustermgmtServiceException | ValidationException | ClustermgmtGenericException e) {
      log.error(RPC_INVOKE_ERROR_MESSAGE, e);
      getNonMigratableVmsResultApiResponse.setDataInWrapper(ClustermgmtResponseFactory.createStandardErrorResponse(e));
      httpStatus = RpcErrorResponseHandler.getHttpStatusFromException(e);
    }
    response.setStatus(httpStatus.value());
    return new ResponseEntity<>(new MappingJacksonValue(getNonMigratableVmsResultApiResponse), httpStatus);
  }
}
