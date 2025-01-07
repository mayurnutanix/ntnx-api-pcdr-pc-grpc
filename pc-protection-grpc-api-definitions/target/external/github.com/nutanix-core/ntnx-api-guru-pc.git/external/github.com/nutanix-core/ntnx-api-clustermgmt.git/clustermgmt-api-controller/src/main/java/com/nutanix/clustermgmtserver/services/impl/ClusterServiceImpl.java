/*
 * Copyright (c) 2021 Nutanix Inc. All rights reserved.
 *
 * Author: rakesh.falak@nutanix.com
 */
package com.nutanix.clustermgmtserver.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.nutanix.api.processor.message.ProcessorUtils;
import com.nutanix.api.utils.json.JsonUtils;
import com.nutanix.api.utils.stats.query.StatsQueryResponse;
import com.nutanix.api.utils.stats.service.exception.StatsGatewayServiceException;
import com.nutanix.api.utils.task.ErgonTaskUtils;
import com.nutanix.api.utils.type.DateUtils;
import com.nutanix.cluster_management.ClusterManagementInterfaceProto;
import com.nutanix.clustermgmtserver.adapters.api.ClusterResourceAdapter;
import com.nutanix.clustermgmtserver.adapters.impl.v4.r0.a1.ClusterResourceAdapterImpl;
import com.nutanix.clustermgmtserver.adapters.impl.ClustermgmtResourceAdapterImpl;
import com.nutanix.clustermgmtserver.exceptions.*;
import com.nutanix.clustermgmtserver.odata.ClustermgmtOdataParser;
import com.nutanix.clustermgmtserver.proxy.CMSProxyImpl;
import com.nutanix.clustermgmtserver.proxy.EntityDbProxyImpl;
import com.nutanix.clustermgmtserver.proxy.ErgonProxyImpl;
import com.nutanix.clustermgmtserver.proxy.GenesisProxyImpl;
import com.nutanix.clustermgmtserver.services.api.ClusterService;
import com.nutanix.clustermgmtserver.utils.ClustermgmtUtils;
import com.nutanix.clustermgmtserver.utils.RequestContextHelper;
import com.nutanix.clustermgmtserver.utils.StatsUtils;
import com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto;
import com.nutanix.insights.ifc.InsightsInterfaceProto;
import com.nutanix.insights.ifc.InsightsInterfaceProto.Entity;
import com.nutanix.insights.ifc.InsightsInterfaceProto.GetEntitiesRet;
import com.nutanix.odata.core.uri.ParsedQuery;
import com.nutanix.prism.adapter.service.ZeusConfiguration;
import com.nutanix.prism.adapter.service.ApplianceConfiguration;
import com.nutanix.prism.commands.multicluster.MulticlusterZeusConfigurationManagingZkImpl;
import com.nutanix.prism.commands.validation.ValidationUtils;
import com.nutanix.util.base.Pair;
import com.nutanix.util.base.UuidUtils;
import com.nutanix.util.base.ValidationException;
import dp1.clu.clustermgmt.v4.config.*;
import dp1.clu.clustermgmt.v4.operations.*;
import dp1.clu.clustermgmt.v4.stats.ClusterStats;
import dp1.clu.clustermgmt.v4.stats.HostStats;
import dp1.clu.clustermgmt.v4.stats.TimeValuePair;
import dp1.clu.common.v1.stats.DownSamplingOperator;
import dp1.clu.prism.v4.config.TaskReference;
import lombok.extern.slf4j.Slf4j;
import nutanix.ergon.ErgonInterface;
import nutanix.ergon.ErgonTypes;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Slf4j
@Service
public class ClusterServiceImpl implements ClusterService {
  private static final String CLUSTER_ENTITY = "cluster";
  private static final String NODE_ENTITY = "node";
  private static final String HOST_GPU_ENTITY = "host_gpu";
  private static final String HOST_NIC_ENTITY = "host_nic";
  private static final String VIRTUAL_NIC_ENTITY = "vmkernel_nic";
  private static final String NETWORK_SWITCH_INTERFACE_ENTITY = "network_switch_interface";
  private static final String NET_INTF_NODE_UUID = "node";
  private static final String MANAGEMENT_SERVER_ENTITY = "management_server_info";
  private static final String CLUSTER_UUID_ATTRIBUTE = "_cluster_uuid_";
  private static final String CLUSTER_UUID_MANAGEMENT_SERVER_ATTR = "cluster_uuid";
  private static final String MANAGEMENT_SERVER_ADDRESS_ATTR = "address";
  private static final String MANAGEMENT_SERVER_EXTENSION_KEY_ATTR = "extension_key";
  private static final String MANAGEMENT_SERVER_TYPE_ATTR = "management_server_type";
  private static final String MANAGEMENT_SERVER_IN_USE_ATTR = "in_use";
  private static final String MANAGEMENT_SERVER_DRS_ENABLED_ATTR = "drs_enabled";
  private static final String HOST_GPU_NODE_UUID_ATTR = "node";
  private static final String HOST_GPU_CLUSTER_UUID_ATTR = "cluster";
  private static final String HOST_GPU_TYPE_ATTR = "gpu_type";
  private static final String GENESIS_SERVICE_NAME = "nutanix.infrastructure.cluster.genesis.GenesisRpcSvc";
  private static final Integer GENESIS_PORT = 2100;
  private static final String GENESIS_JSON_URL_PREFIX = "http://localhost:9444/v3/fanout_proxy?";
  private static final String UNKNOWN_HOST_UUID_STRING = "Unknown host UUID: ";
  private static final String HOST_UUID_STRING = "Host with uuid: ";
  private static final String NOT_BELONG_TO_CLUSTER_STRING = " doesn't belong to cluster: ";
  private static final String ASYNC_PROCESSOR_REQUEST_FAILED = "Failed to publish message to NATS";
  private static final String GENESIS_COMPONENT_NAME = "kGenesis";
  private static final String REMOTE_CLUSTER_UUID_STRING = "remote_cluster_uuid=";
  private static final String METHOD_AND_URL_STRING = "&method=POST&url_full_path=/jsonrpc";
  private static final String NIC_NODE_UUID_ATTR = "node";
  public static final String MASTER_CLUSTER_UUID_ATTRIBUTE = "_master_cluster_uuid_";
  private static final String CATEGORIES_LIST_ATTR = "category_id_list";
  private static final String ABAC_ENTITY = "abac_entity_capability";
  private static final String PLANNED_OUTAGE_COMPONENT_NAME = "Planned Outage";
  private static final String DEFAULT_ORDERBY_ATTR = "extId";
  private static final String NON_MIGRATABLE_VMS_ENTITY = "compute_non_migratable_vms_entity";
  private final EntityDbProxyImpl entityDbProxy;
  private final MulticlusterZeusConfigurationManagingZkImpl multiClusterZeusConfig;
  private final ClustermgmtResourceAdapterImpl clustermgmtResourceAdapter;
  private final ClusterResourceAdapter clusterResourceAdapter;
  private final ApplianceConfiguration applianceConfiguration;
  private final ClustermgmtOdataParser clustermgmtOdataParser;
  private final GenesisProxyImpl genesisProxy;
  private final ErgonProxyImpl ergonProxy;
  private final CMSProxyImpl cmsProxy;
  @Lazy
  private final ProcessorUtils processorUtils;
  private final ObjectMapper mapper = JsonUtils.getObjectMapper();

  @Autowired
  public ClusterServiceImpl(final EntityDbProxyImpl entityDbProxy,
                            final MulticlusterZeusConfigurationManagingZkImpl multiClusterZeusConfig,
                            final ClustermgmtResourceAdapterImpl clustermgmtResourceAdapter,
                            @Qualifier(ClusterResourceAdapterImpl.ADAPTER_VERSION) final ClusterResourceAdapter clusterResourceAdapter,
                            final ApplianceConfiguration applianceConfiguration,
                            final ClustermgmtOdataParser clustermgmtOdataParser,
                            final GenesisProxyImpl genesisProxy,
                            final ErgonProxyImpl ergonProxy,
                            final CMSProxyImpl cmsProxy,
                            @Lazy final ProcessorUtils processorUtils) {
    this.entityDbProxy = entityDbProxy;
    this.multiClusterZeusConfig = multiClusterZeusConfig;
    this.clustermgmtResourceAdapter = clustermgmtResourceAdapter;
    this.clusterResourceAdapter = clusterResourceAdapter;
    this.applianceConfiguration = applianceConfiguration;
    this.clustermgmtOdataParser = clustermgmtOdataParser;
    this.genesisProxy = genesisProxy;
    this.ergonProxy = ergonProxy;
    this.cmsProxy = cmsProxy;
    this.processorUtils = processorUtils;
  }

  @Override
  public Cluster getClusterEntity(String clusterUuid, String expand) throws ClustermgmtServiceException,
    ValidationException {
    Cluster clusterEntity;
    if (!StringUtils.isEmpty(expand)) {
      clusterEntity = new ClusterProjection();
    } else {
      clusterEntity = new Cluster();
    }
    clusterEntity.setExtId(clusterUuid);

    //get the cluster entity with clusterUuid and adapt to the entity type
    InsightsInterfaceProto.GetEntitiesWithMetricsRet getEntitiesWithMetricsRet =
      ClustermgmtUtils.getEntitiesWithMetricsRet(null, null,
        CLUSTER_ENTITY, entityDbProxy, ClustermgmtUtils.clusterAttributeList, clusterUuid, clustermgmtResourceAdapter, null, null, null);
    log.debug("The RPC response GetEntitiesWithMetricsRet obtained {}", getEntitiesWithMetricsRet);

    if (!CollectionUtils.isEmpty(getEntitiesWithMetricsRet.getGroupResultsListList())) {
      final InsightsInterfaceProto.EntityWithMetric entityWithMetric = getEntitiesWithMetricsRet.getGroupResultsList(0).getRawResults(0);
      final List<InsightsInterfaceProto.MetricData> metricDataList = entityWithMetric.getMetricDataListList();
      clusterEntity = clusterResourceAdapter.adaptIdfClusterMetricstoClusterEntity(metricDataList, clusterEntity);
    }
    else{
      throw new ClustermgmtNotFoundException("Unknown Cluster Uuid: " + clusterUuid);
    }

    ZeusConfiguration zkConfig;
    if (clusterEntity.getConfig() == null || clusterEntity.getConfig().getClusterFunction() == null) {
      throw new ClustermgmtServiceException("Failed to retrieve cluster function");
    }
    if (clusterEntity.getConfig().getClusterFunction().contains(ClusterFunctionRef.PRISM_CENTRAL)) {
      log.debug("PC Cluster");
      zkConfig = applianceConfiguration.getZeusConfig(true);
      log.debug("Categories not valid attribute for PC Cluster");
    }
    else {
      log.debug("PE Cluster");
      zkConfig = ClustermgmtUtils.getZkconfig(clusterUuid, multiClusterZeusConfig);
      // Set Categories Info
      log.debug("Fetching categories if any attached to the cluster");
      clusterEntity = setCategoriesInfo(clusterUuid, clusterEntity);
    }
    if (zkConfig != null)
      clusterEntity = clusterResourceAdapter.adaptZeusEntriestoClusterEntity(zkConfig, clusterEntity);

    // Set Management Server Info
    clusterEntity = setManagementServerInfo(clusterUuid, clusterEntity);

    // Expansion
    if (expand != null) {
      String filterString = "extId eq "+"'"+clusterUuid+"'";
      final ClustermgmtOdataParser.Result odataResult =
        clustermgmtOdataParser.parse("/clusters", filterString, null, null, null, expand);
      Map statsResponseMap = odataResult.getStatsGatewayResponseMap();
      log.debug("stats response map {}", statsResponseMap);
      List<Cluster> clusterList = new ArrayList<>();
      clusterList.add(clusterEntity);
      clusterList = clusterResourceAdapter.ProcessStatsResponseMapToClusters(clusterList, statsResponseMap);
      return clusterList.get(0);
    }
    // Set Http Proxy Info
    clusterEntity = setHttpProxyInfo(clusterUuid, clusterEntity);

    return clusterEntity;
  }

  @Override
  public Pair<Integer, List<Cluster>> getClusterEntities(Integer offset, Integer limit,
                                                               String filter, String orderBy,
                                                               String apply, String select, String expand)
    throws ClustermgmtServiceException, ValidationException {
    if(orderBy == null){
      orderBy = DEFAULT_ORDERBY_ATTR;
    }
    final ClustermgmtOdataParser.Result odataResult =
      clustermgmtOdataParser.parse("/clusters", filter, orderBy, apply, select, expand);
    List<InsightsInterfaceProto.QueryOrderBy> queryOrderBy = odataResult.getOrderBy();
    if(queryOrderBy != null && !CollectionUtils.isEmpty(queryOrderBy))
      return getEntities(offset, limit, odataResult.getFilter(), queryOrderBy.get(0), odataResult.getGroupBy(), select, expand, odataResult.getStatsGatewayResponseMap());
    return getEntities(offset, limit, odataResult.getFilter(), null, odataResult.getGroupBy(), select, expand, odataResult.getStatsGatewayResponseMap());
  }

  @Override
  public SnmpConfig getSnmpConfig(String clusterUuid) throws ClustermgmtServiceException {
    SnmpConfig snmpConfig = new SnmpConfig();
    ZeusConfiguration zkConfig;
    boolean config_present_in_idf;
    InsightsInterfaceProto.BooleanExpression where_clause;

    if (isMulticluster(clusterUuid)) {
      log.debug("PC Cluster");
      config_present_in_idf = true;
      where_clause = clustermgmtResourceAdapter.constructNotExistsBooleanExpression(MASTER_CLUSTER_UUID_ATTRIBUTE);
    }
    else{
      log.debug("PE Cluster");
      isRequestAllowed(clusterUuid, ClustermgmtUtils.GET_SNMP_CONFIG);
      // If Async update cluster is supported on the PE, then SNMP config will be present in IDF.
      config_present_in_idf = (getGenesisApiVersion(clusterUuid) >=
        GenesisInterfaceProto.GenesisApiVersion.kAsyncUpdateClusterSupportedVersion.getNumber());
      where_clause = clustermgmtResourceAdapter.adaptToBooleanExpression(
        MASTER_CLUSTER_UUID_ATTRIBUTE, InsightsInterfaceProto.ComparisonExpression.Operator.kEQ,
        InsightsInterfaceProto.DataValue.newBuilder().setStrValue(clusterUuid));
    }

    // SNMP user config is written to IDF. We can directly read it from IDF.
    if (config_present_in_idf) {
      log.debug("SNMP config for cluster {} is present in IDF", clusterUuid);
      GetEntitiesRet getEntitiesRet = validateAndGetClusterEntity(clusterUuid);
      boolean snmpStatus = ClustermgmtUtils.getSnmpStatusFromIdf(getEntitiesRet);
      snmpConfig.setIsEnabled(snmpStatus);

      List<SnmpUser> snmpUserList = ClustermgmtUtils.getSnmpUserEntities(
        null, where_clause, entityDbProxy, clustermgmtResourceAdapter, clusterResourceAdapter);
      if (!snmpUserList.isEmpty())
        snmpConfig.setUsers(snmpUserList);

      List<SnmpTrap> snmpTrapList = ClustermgmtUtils.getSnmpTrapEntities(
        null, where_clause, entityDbProxy, clustermgmtResourceAdapter, clusterResourceAdapter);
      if (!snmpTrapList.isEmpty())
        snmpConfig.setTraps(snmpTrapList);

      List<SnmpTransport> snmpTransportList = ClustermgmtUtils.getSnmpTransportEntities(
        null, where_clause, entityDbProxy, clustermgmtResourceAdapter, clusterResourceAdapter);
      if (!snmpTransportList.isEmpty())
        snmpConfig.setTransports(snmpTransportList);
    }
    else {
      log.debug("SNMP config for cluster {} is not present in IDF. Fetching config from Zeus", clusterUuid);
      zkConfig = ClustermgmtUtils.getZkconfig(clusterUuid, multiClusterZeusConfig);
      snmpConfig = clusterResourceAdapter.adaptZeusEntriestoSnmpInfo(zkConfig, snmpConfig);
    }
    log.debug("Snmp Config {}", snmpConfig);
    return snmpConfig;
  }

  @Override
  public ConfigCredentials getConfigCredentials(String clusterUuid)
    throws ClustermgmtServiceException {

    ConfigCredentials configCredentials = new ConfigCredentials();

    // Get Config credentials Arg.
    GenesisInterfaceProto.GetConfigCredentialsArg getConfigCredentialsArg =
      GenesisInterfaceProto.GetConfigCredentialsArg.newBuilder()
        .setClusterUuid(clusterUuid).build();

    GenesisInterfaceProto.GetConfigCredentialsRet getConfigCredentialsRet =
      genesisProxy.doExecute(GenesisProxyImpl.GenesisRpcName.GET_CONFIG_CREDENTIALS, getConfigCredentialsArg, RequestContextHelper.getRpcRequestContext());

    if(getConfigCredentialsRet.getError().getErrorType() !=
      GenesisInterfaceProto.GenesisGenericError.ErrorType.kNoError)
      throw new ClustermgmtServiceException("Failed to fetch credentials from Genesis");

    return clusterResourceAdapter.adaptGetConfigCredentialsRetToConfigCredentials(
      getConfigCredentialsRet, configCredentials);
  }

  @Override
  public List<RsyslogServer> getRsyslogServerConfig(String clusterUuid)
    throws ClustermgmtServiceException {
    boolean config_present_in_idf;
    InsightsInterfaceProto.BooleanExpression where_clause;

    if (isMulticluster(clusterUuid)) {
      log.debug("PC Cluster");
      config_present_in_idf = true;
      where_clause = clustermgmtResourceAdapter.constructNotExistsBooleanExpression(MASTER_CLUSTER_UUID_ATTRIBUTE);
    }
    else {
      log.debug("PE Cluster");
      isRequestAllowed(clusterUuid, ClustermgmtUtils.GET_RSYSLOG_CONFIG);
      // If Async update cluster is supported on the PE, then SNMP config will be present in IDF.
      config_present_in_idf = (getGenesisApiVersion(clusterUuid) >=
        GenesisInterfaceProto.GenesisApiVersion.kAsyncUpdateClusterSupportedVersion.getNumber());
      where_clause = clustermgmtResourceAdapter.adaptToBooleanExpression(
        MASTER_CLUSTER_UUID_ATTRIBUTE, InsightsInterfaceProto.ComparisonExpression.Operator.kEQ,
        InsightsInterfaceProto.DataValue.newBuilder().setStrValue(clusterUuid));
    }

    // Rsyslog config is written to IDF. We can directly read it from IDF.
    if (config_present_in_idf) {
      log.debug("Rsyslog server config for cluster {} is present in IDF", clusterUuid);
      return ClustermgmtUtils.getRsyslogServerEntities(
        null, where_clause, entityDbProxy, clustermgmtResourceAdapter, clusterResourceAdapter);
    }

    ZeusConfiguration zkConfig = ClustermgmtUtils.getZkconfig(clusterUuid, multiClusterZeusConfig);
    return clusterResourceAdapter.adaptZeusEntriestoRsyslogServer(zkConfig);
  }

  @Override
  public List<RackableUnit> getRackableUnits(String clusterUuid)
    throws ClustermgmtServiceException {
    ZeusConfiguration zkConfig;

    if (isMulticluster(clusterUuid)) {
      log.debug("PC Cluster");
      throw new ClustermgmtNotSupportedException("Rackable Unit list api not supported on PC cluster uuid: " + clusterUuid);
    }

    zkConfig = ClustermgmtUtils.getZkconfig(clusterUuid, multiClusterZeusConfig);
    return clusterResourceAdapter.adaptZeusEntriestoRackableUnits(zkConfig);
  }

  @Override
  public RackableUnit getRackableUnit(String clusterUuid, String rackableUnitUuid)
    throws ClustermgmtServiceException {
    ZeusConfiguration zkConfig;

    if (isMulticluster(clusterUuid)) {
      log.debug("PC Cluster");
      throw new ClustermgmtNotSupportedException("Rackable Unit api not supported on PC cluster uuid: " + clusterUuid);
    }

    zkConfig = ClustermgmtUtils.getZkconfig(clusterUuid, multiClusterZeusConfig);
    RackableUnit rackableUnit = clusterResourceAdapter.adaptZeusEntriestoRackableUnit(zkConfig, rackableUnitUuid);
    if (rackableUnit == null) {
      throw new ClustermgmtNotFoundException("Rackable Unit with uuid: " + rackableUnitUuid + " doesn't exist");
    }
    return rackableUnit;
  }

  @Override
  public List<DomainFaultTolerance> getDomainFaultToleranceStatus(String clusterUuid)
    throws ClustermgmtServiceException {
    ZeusConfiguration zkConfig;

    if (isMulticluster(clusterUuid)) {
      log.debug("PC Cluster");
      throw new ClustermgmtNotSupportedException("Domain Fault tolerance api not supported on PC cluster uuid: " + clusterUuid);
    }

    zkConfig = ClustermgmtUtils.getZkconfig(clusterUuid, multiClusterZeusConfig);
    return clusterResourceAdapter.adaptZeusEntriestoDomainFaultTolerance(zkConfig);
  }

  public boolean isMulticluster(String clusterUuid) throws ClustermgmtServiceException {
    // Validate Cluster Uuid
    GetEntitiesRet getEntitiesRet = validateAndGetClusterEntity(clusterUuid);

    Entity entity = getEntitiesRet.getEntity(0);
    for (InsightsInterfaceProto.NameTimeValuePair attribute : entity.getAttributeDataMapList()) {
      if (!attribute.hasName() || !attribute.hasValue()) {
        continue;
      }
      if(attribute.getName().equals("service_list")) {
        List<ClusterFunctionRef> clusterFunctions = new ArrayList<>();
        for (String function : attribute.getValue().getStrList().getValueListList())
          clusterFunctions.add(ClustermgmtUtils.clusterFunctionRefHashMap.get(function));
        if(clusterFunctions.contains(ClusterFunctionRef.PRISM_CENTRAL))
          return true;
      }
    }

    return false;
  }

  public Pair<Integer, List<Cluster>> getEntities(final Integer offset, final Integer limit,
                                                        InsightsInterfaceProto.BooleanExpression expression,
                                                        InsightsInterfaceProto.QueryOrderBy orderBy,
                                                        InsightsInterfaceProto.QueryGroupBy groupBy,
                                                        String select, String expand, Map statsResponseMap)
    throws ClustermgmtServiceException {
    List<Cluster> clusterEntities = new ArrayList<>();
    Integer totalEntityCount = null;
    int entityCount = 0;
    List<String> attributeList = ClustermgmtUtils.clusterAttributeList;
    if(select != null && groupBy != null){
      attributeList = ClustermgmtUtils.getAttributeListFromSelection(groupBy);
    }

    InsightsInterfaceProto.GetEntitiesWithMetricsRet getEntitiesWithMetricsRet =
      ClustermgmtUtils.getEntitiesWithMetricsRet(offset, limit,
        CLUSTER_ENTITY, entityDbProxy, attributeList, null, clustermgmtResourceAdapter, expression, orderBy, groupBy);
    log.debug("The RPC response GetEntitiesWithMetricsRet obtained {}", getEntitiesWithMetricsRet);

    if (!CollectionUtils.isEmpty(getEntitiesWithMetricsRet.getGroupResultsListList())) {
      for (final InsightsInterfaceProto.QueryGroupResult queryGroupResult : getEntitiesWithMetricsRet.getGroupResultsListList()) {
        final List<InsightsInterfaceProto.EntityWithMetric> entitiesWithMetric =
          queryGroupResult.getRawResultsList();
        entityCount += (int) queryGroupResult.getTotalEntityCount();

        for (final InsightsInterfaceProto.EntityWithMetric entityWithMetric : entitiesWithMetric) {
          final List<InsightsInterfaceProto.MetricData> metricDataList =
            entityWithMetric.getMetricDataListList();
          String clusterUuid = entityWithMetric.getEntityGuid().getEntityId();
          Cluster clusterEntity;
          if (!StringUtils.isEmpty(expand)) {
            clusterEntity = new ClusterProjection();
          } else {
            clusterEntity = new Cluster();
          }
          clusterEntity.setExtId(clusterUuid);
          clusterEntity = clusterResourceAdapter.adaptIdfClusterMetricstoClusterEntity(metricDataList, clusterEntity);
          if (select == null) {
            ZeusConfiguration zkConfig;
            if (clusterEntity.getConfig() == null || clusterEntity.getConfig().getClusterFunction() == null) {
              log.error("Failed to retrieve cluster function for cluster {}", clusterUuid);
              continue;
            }
            if (clusterEntity.getConfig().getClusterFunction().contains(ClusterFunctionRef.PRISM_CENTRAL)) {
              log.debug("PC Cluster");
              zkConfig = applianceConfiguration.getZeusConfig(true);
              log.debug("Categories not a valid attribute for PC Cluster");
            } else {
              log.debug("PE Cluster");
              zkConfig = ClustermgmtUtils.getZkconfig(clusterUuid, multiClusterZeusConfig);
              log.debug("Fetching categories if any attached to the cluster with uuid: {}", clusterUuid);
              // Set Categories Info
              clusterEntity = setCategoriesInfo(clusterUuid, clusterEntity);
            }
            if (zkConfig != null)
              clusterEntity = clusterResourceAdapter.adaptZeusEntriestoClusterEntity(zkConfig, clusterEntity);
            // Set Management Server Info
            clusterEntity = setManagementServerInfo(clusterUuid, clusterEntity);
            // Set Http Proxy Info
            clusterEntity = setHttpProxyInfo(clusterUuid, clusterEntity);
          }

          // Append entity
          clusterEntities.add(clusterEntity);
        }
      }
    }

    // Expansion
    if (statsResponseMap != null) {
      log.debug("stats response map {}", statsResponseMap);
      clusterEntities = clusterResourceAdapter.ProcessStatsResponseMapToClusters(clusterEntities, statsResponseMap);
    }
    if (entityCount != 0)
      totalEntityCount = entityCount;

    return new Pair<>(totalEntityCount,clusterEntities);
  }

  @Override
  public Host getHostEntity(String clusterUuid, String hostUuid)
    throws ClustermgmtServiceException {
    // Validate and check if cluster is PE/PC.
    if (isMulticluster(clusterUuid)) {
      log.debug("PC Cluster");
      throw new ClustermgmtNotSupportedException("Host Api is not supported on PC cluster uuid: " + clusterUuid);
    }

    Host hostEntity = new Host();
    hostEntity.setExtId(hostUuid);

    //get the host entity with hostUuid and adapt to the entity type
    InsightsInterfaceProto.GetEntitiesWithMetricsRet getEntitiesWithMetricsRet =
      ClustermgmtUtils.getEntitiesWithMetricsRet(null, null,
        NODE_ENTITY, entityDbProxy, ClustermgmtUtils.hostAttributeList, hostUuid, clustermgmtResourceAdapter, null, null, null);
    log.debug("The RPC response GetEntitiesWithMetricsRet obtained {}", getEntitiesWithMetricsRet);

    if (!CollectionUtils.isEmpty(getEntitiesWithMetricsRet.getGroupResultsListList())) {
      final InsightsInterfaceProto.EntityWithMetric entityWithMetric = getEntitiesWithMetricsRet.getGroupResultsList(0).getRawResults(0);
      final List<InsightsInterfaceProto.MetricData> metricDataList = entityWithMetric.getMetricDataListList();
      hostEntity = clusterResourceAdapter.adaptIdfHostMetricstoHostEntity(metricDataList,hostEntity);
    }
    else{
      throw new ClustermgmtNotFoundException(UNKNOWN_HOST_UUID_STRING + hostUuid);
    }

    if (hostEntity.getCluster() != null  && clusterUuid.equals(hostEntity.getCluster().getUuid())) {
      ZeusConfiguration zkConfig =
        ClustermgmtUtils.getZkconfig(clusterUuid, multiClusterZeusConfig);
      hostEntity = clusterResourceAdapter.adaptZeusEntriestoHostEntity(zkConfig, hostEntity, hostUuid);
    }
    else {
      throw new ClustermgmtNotFoundException(HOST_UUID_STRING + hostUuid + NOT_BELONG_TO_CLUSTER_STRING + clusterUuid);
    }

    return hostEntity;
  }

  @Override
  public Pair<Integer, List<Host> > getHostEntities(String clusterUuid,
                                                          Integer offset,
                                                          Integer limit,
                                                          String filter,
                                                          String orderBy,
                                                          String apply,
                                                          String select)
    throws ClustermgmtServiceException, ValidationException {

    // Validate and check if cluster is PE/PC.
    if (isMulticluster(clusterUuid)) {
      log.debug("PC Cluster");
      throw new ClustermgmtNotSupportedException("Host list api is not supported on PC cluster uuid: " + clusterUuid);
    }

    if(orderBy == null){
      orderBy = DEFAULT_ORDERBY_ATTR;
    }

    // Boolean expression from Odata filer
    final ClustermgmtOdataParser.Result odataResult =
      clustermgmtOdataParser.parse("/hosts", filter, orderBy, apply, select, null);

    List<InsightsInterfaceProto.QueryOrderBy> queryOrderBy = odataResult.getOrderBy();
    InsightsInterfaceProto.BooleanExpression filterExpression = odataResult.getFilter();
    InsightsInterfaceProto.QueryOrderBy sortQueryOrderBy = null;
    if(queryOrderBy != null && !CollectionUtils.isEmpty(queryOrderBy)) {
      sortQueryOrderBy = queryOrderBy.get(0);
    }

    // Boolean expression to get nodes belong to cluster with cluster_uuid
    InsightsInterfaceProto.DataValue.Builder value =
      InsightsInterfaceProto.DataValue.newBuilder().setStrValue(clusterUuid);
    InsightsInterfaceProto.BooleanExpression expression =
      clustermgmtResourceAdapter.adaptToBooleanExpression(CLUSTER_UUID_ATTRIBUTE,
        InsightsInterfaceProto.ComparisonExpression.Operator.kEQ,
        value);

    // Join Boolean Expression with Odate filter and above one.
    InsightsInterfaceProto.BooleanExpression joinExpression = expression;
    if (filterExpression != null)
      joinExpression = clustermgmtResourceAdapter.joinBooleanExpressions(
        expression, InsightsInterfaceProto.BooleanExpression.Operator.kAnd, filterExpression);

    // Get Entities
    return getHostEntitiesFromIdf(offset, limit, entityDbProxy, clustermgmtResourceAdapter, joinExpression, sortQueryOrderBy, odataResult.getGroupBy(), select);
  }

  public Cluster setCategoriesInfo(String clusterUuid, Cluster clusterEntity)
    throws ClustermgmtServiceException {

    // Boolean expression to get categories list attached to cluster with cluster_uuid
    InsightsInterfaceProto.DataValue.Builder value1 =
      InsightsInterfaceProto.DataValue.newBuilder().setStrValue(clusterUuid);
    InsightsInterfaceProto.BooleanExpression expression1 =
      clustermgmtResourceAdapter.adaptToBooleanExpression("kind_id",
        InsightsInterfaceProto.ComparisonExpression.Operator.kEQ,
        value1);

    InsightsInterfaceProto.DataValue.Builder value2 =
      InsightsInterfaceProto.DataValue.newBuilder().setStrValue(CLUSTER_ENTITY);
    InsightsInterfaceProto.BooleanExpression expression2 =
      clustermgmtResourceAdapter.adaptToBooleanExpression("kind",
        InsightsInterfaceProto.ComparisonExpression.Operator.kEQ,
        value2);

    // Join Boolean Expression
    InsightsInterfaceProto.BooleanExpression expression =
      clustermgmtResourceAdapter.joinBooleanExpressions(
        expression1, InsightsInterfaceProto.BooleanExpression.Operator.kAnd, expression2);

    // Get abac capability entity containing the category list for cluster
    List<String> rawColumns = Arrays.asList(CATEGORIES_LIST_ATTR);
    InsightsInterfaceProto.GetEntitiesWithMetricsRet getEntitiesWithMetricsRet =
      ClustermgmtUtils.getEntitiesWithMetricsRet(null, null,
        ABAC_ENTITY, entityDbProxy, rawColumns, null, clustermgmtResourceAdapter, expression, null, null);

    if (!CollectionUtils.isEmpty(getEntitiesWithMetricsRet.getGroupResultsListList())) {
      final InsightsInterfaceProto.EntityWithMetric entityWithMetric = getEntitiesWithMetricsRet.getGroupResultsList(0).getRawResults(0);
      final List<InsightsInterfaceProto.MetricData> metricDataList = entityWithMetric.getMetricDataListList();
      clusterEntity = clusterResourceAdapter.
        adaptCategoriesToClusterEntity(metricDataList, clusterEntity);
    }
    return clusterEntity;
  }

  public Cluster setManagementServerInfo(String clusterUuid, Cluster clusterEntity)
    throws ClustermgmtServiceException {
    // Management server info
    // Boolean expression to get management server info belong to cluster with cluster_uuid
    InsightsInterfaceProto.DataValue.Builder value =
      InsightsInterfaceProto.DataValue.newBuilder().setStrValue(clusterUuid);
    InsightsInterfaceProto.BooleanExpression expression =
      clustermgmtResourceAdapter.adaptToBooleanExpression(CLUSTER_UUID_MANAGEMENT_SERVER_ATTR,
        InsightsInterfaceProto.ComparisonExpression.Operator.kEQ,
        value);

    // Get Entities
    List<String> rawColoums = Arrays.asList(
      MANAGEMENT_SERVER_ADDRESS_ATTR, MANAGEMENT_SERVER_EXTENSION_KEY_ATTR,
      MANAGEMENT_SERVER_TYPE_ATTR, MANAGEMENT_SERVER_IN_USE_ATTR, MANAGEMENT_SERVER_DRS_ENABLED_ATTR);
    InsightsInterfaceProto.GetEntitiesWithMetricsRet getEntitiesWithMetricsRet =
      ClustermgmtUtils.getEntitiesWithMetricsRet(null, null,
        MANAGEMENT_SERVER_ENTITY, entityDbProxy, rawColoums, null, clustermgmtResourceAdapter, expression, null, null);

    if (!CollectionUtils.isEmpty(getEntitiesWithMetricsRet.getGroupResultsListList())) {
      final List<InsightsInterfaceProto.EntityWithMetric> entitiesWithMetric =
        getEntitiesWithMetricsRet.getGroupResultsList(0).getRawResultsList();
      clusterEntity = clusterResourceAdapter.
        adaptManagementServerIntoClusterEntity(entitiesWithMetric, clusterEntity);
    }
    return clusterEntity;
  }

  public Cluster setHttpProxyInfo(String clusterUuid, Cluster clusterEntity)
    throws ClustermgmtServiceException {

    // Query IDF to fetch the Http Proxy info.
    InsightsInterfaceProto.BooleanExpression where_clause;

    if (isMulticluster(clusterUuid)) {
      log.debug("PC Cluster");
      where_clause = clustermgmtResourceAdapter.constructNotExistsBooleanExpression(MASTER_CLUSTER_UUID_ATTRIBUTE);
    }
    else {
      log.debug("PE Cluster");
      where_clause = clustermgmtResourceAdapter.adaptToBooleanExpression(
        MASTER_CLUSTER_UUID_ATTRIBUTE, InsightsInterfaceProto.ComparisonExpression.Operator.kEQ,
        InsightsInterfaceProto.DataValue.newBuilder().setStrValue(clusterUuid));
    }

    // Get the HTTP proxy entities.
    List<HttpProxyConfig> httpProxyConfigList = ClustermgmtUtils.getHttpProxyEntities(
      where_clause, entityDbProxy, clustermgmtResourceAdapter, clusterResourceAdapter);
    if (!httpProxyConfigList.isEmpty()) {
      ClusterNetworkReference clusterNetworkReference = clusterEntity.getNetwork();
      if (clusterNetworkReference == null)
        clusterNetworkReference = new ClusterNetworkReference();
      clusterNetworkReference.setHttpProxyList(httpProxyConfigList);
      clusterEntity.setNetwork(clusterNetworkReference);
    }

    List<HttpProxyWhiteListConfig> httpProxyWhitelistConfigList = ClustermgmtUtils.getHttpProxyWhitelistEntities(
      where_clause, entityDbProxy, clustermgmtResourceAdapter, clusterResourceAdapter);
    if (!httpProxyWhitelistConfigList.isEmpty()) {
      ClusterNetworkReference clusterNetworkReference = clusterEntity.getNetwork();
      if (clusterNetworkReference == null)
        clusterNetworkReference = new ClusterNetworkReference();
      clusterNetworkReference.setHttpProxyWhiteList(httpProxyWhitelistConfigList);
      clusterEntity.setNetwork(clusterNetworkReference);
    }
    return clusterEntity;
  }

  @Override
  public Pair<Integer, List<HostGpu> > getHostGpus(String clusterUuid, String hostUuid, Integer limit, Integer page,
                                                         String filter, String orderBy, String selectString)
    throws ClustermgmtServiceException, ValidationException {

    // Validate and check if cluster is PE/PC.
    if (isMulticluster(clusterUuid)) {
      log.debug("PC Cluster");
      throw new ClustermgmtNotSupportedException("Host Gpu Api is not supported on PC cluster uuid: " + clusterUuid);
    }

    // Validate and get host entity
    validateAndGetHostEntity(hostUuid);

    // Validate host belongs to cluster
    if(!validateHostBelongsToCluster(hostUuid, clusterUuid)) {
      throw new ClustermgmtNotFoundException(HOST_UUID_STRING + hostUuid + NOT_BELONG_TO_CLUSTER_STRING + clusterUuid);
    }

    return getGpuEntitiesFromIDF(limit, page, filter, orderBy, hostUuid, HOST_GPU_NODE_UUID_ATTR, selectString);
  }

  public GetEntitiesRet validateAndGetClusterEntity(String clusterUuid) throws ClustermgmtServiceException{
    GetEntitiesRet getEntitiesRet =
      ClustermgmtUtils.getInsightsEntityRet(clusterUuid, CLUSTER_ENTITY,
        entityDbProxy, clustermgmtResourceAdapter);
    if (getEntitiesRet.getEntityCount() == 0) {
      throw new ClustermgmtNotFoundException("Unknown Cluster Uuid: " + clusterUuid);
    }
    return getEntitiesRet;
  }

  public GetEntitiesRet validateAndGetHostEntity(String hostUuid) throws ClustermgmtServiceException{
    GetEntitiesRet getEntitiesRet =
      ClustermgmtUtils.getInsightsEntityRet(hostUuid, NODE_ENTITY,
        entityDbProxy, clustermgmtResourceAdapter);
    if (getEntitiesRet.getEntityCount() == 0) {
      throw new ClustermgmtNotFoundException(UNKNOWN_HOST_UUID_STRING + hostUuid);
    }
    return getEntitiesRet;
  }

  public void validateAndGetHostNicEntity(String hostNicUuid) throws ClustermgmtServiceException{
    InsightsInterfaceProto.GetEntitiesWithMetricsRet getEntitiesWithMetricsRet =
    ClustermgmtUtils.getEntitiesWithMetricsRet(null, null,
      HOST_NIC_ENTITY, entityDbProxy, ClustermgmtUtils.hostNicAttributeList, hostNicUuid, clustermgmtResourceAdapter, null, null, null);

    if (CollectionUtils.isEmpty(getEntitiesWithMetricsRet.getGroupResultsListList())){
      throw new ClustermgmtNotFoundException("Unknown Host Nic Uuid: " + hostNicUuid);
    }
  }

  public boolean validateHostBelongsToCluster(String hostUuid, String clusterUuid) throws ClustermgmtServiceException {
    GetEntitiesRet getEntitiesRet =
      ClustermgmtUtils.getInsightsEntityRet(hostUuid, NODE_ENTITY,
        entityDbProxy, clustermgmtResourceAdapter);
    if (getEntitiesRet.getEntityCount() == 0) {
      throw new ClustermgmtNotFoundException(UNKNOWN_HOST_UUID_STRING + hostUuid);
    }
    Entity entity = getEntitiesRet.getEntity(0);
    for (InsightsInterfaceProto.NameTimeValuePair attribute : entity.getAttributeDataMapList()) {
      if (!attribute.hasName() || !attribute.hasValue()) {
        continue;
      }
      if(attribute.getName().equals("_cluster_uuid_")) {
        if(clusterUuid.equals(attribute.getValue().getStrValue()))
          return true;
      }
    }
    return false;
  }

  public Pair<Integer, List<HostGpu> > getGpuEntitiesFromIDF(Integer limit, Integer page, String filter,
                                                                   String orderBy, String uuid, String uuidType, String select)
    throws ClustermgmtServiceException, ValidationException {
    Integer totalEntityCount = null;
    List<HostGpu> hostGpuEntities = new ArrayList<>();
    List<String> attributeList = ClustermgmtUtils.hostGpuAttributeList;
    // Boolean expression from Odata filer
    final ClustermgmtOdataParser.Result odataResult =
      clustermgmtOdataParser.parse("/host-gpus", filter, orderBy, null, select, null);

    List<InsightsInterfaceProto.QueryOrderBy> queryOrderBy = odataResult.getOrderBy();
    InsightsInterfaceProto.BooleanExpression filterExpression = odataResult.getFilter();
    InsightsInterfaceProto.QueryGroupBy queryGroupBy = odataResult.getGroupBy();
    InsightsInterfaceProto.QueryOrderBy sortQueryOrderBy = null;
    if(queryOrderBy != null && !CollectionUtils.isEmpty(queryOrderBy)) {
      sortQueryOrderBy = queryOrderBy.get(0);
    }
    if(select != null && queryGroupBy != null){
      attributeList = ClustermgmtUtils.getAttributeListFromSelection(queryGroupBy);
    }
    InsightsInterfaceProto.BooleanExpression joinExpression = filterExpression;

    if (uuidType != null && uuid != null) {
      // Boolean expression to get host gpus belong to either host/cluster based on uuidType
      InsightsInterfaceProto.DataValue.Builder value =
        InsightsInterfaceProto.DataValue.newBuilder().setStrValue(uuid);
      InsightsInterfaceProto.BooleanExpression expression =
        clustermgmtResourceAdapter.adaptToBooleanExpression(uuidType,
          InsightsInterfaceProto.ComparisonExpression.Operator.kEQ,
          value);

      // Join Boolean Expression with Odate filter and above one.
      if (joinExpression != null)
        joinExpression = clustermgmtResourceAdapter.joinBooleanExpressions(
          expression, InsightsInterfaceProto.BooleanExpression.Operator.kAnd, joinExpression);
      else
        joinExpression = expression;
    }

    // Get Entities
    InsightsInterfaceProto.GetEntitiesWithMetricsRet getEntitiesWithMetricsRet =
      ClustermgmtUtils.getEntitiesWithMetricsRet(page, limit,
        HOST_GPU_ENTITY, entityDbProxy, attributeList, null, clustermgmtResourceAdapter, joinExpression, sortQueryOrderBy, null);
    log.debug("The RPC response GetEntitiesWithMetricsRet obtained {}", getEntitiesWithMetricsRet);

    if (!CollectionUtils.isEmpty(getEntitiesWithMetricsRet.getGroupResultsListList())) {
      totalEntityCount =
        (int) getEntitiesWithMetricsRet.getGroupResultsList(0).getTotalEntityCount();
      log.debug("The RPC response count {}", totalEntityCount);
      final List<InsightsInterfaceProto.EntityWithMetric> entitiesWithMetric =
        getEntitiesWithMetricsRet.getGroupResultsList(0).getRawResultsList();
      for (final InsightsInterfaceProto.EntityWithMetric entityWithMetric : entitiesWithMetric) {
        final List<InsightsInterfaceProto.MetricData> metricDataList =
          entityWithMetric.getMetricDataListList();
        String hostGpuUuid = entityWithMetric.getEntityGuid().getEntityId();

        HostGpu hostGpuEntity = new HostGpu();
        hostGpuEntity.setExtId(hostGpuUuid);
        hostGpuEntity = clusterResourceAdapter.adaptIdfHostGpuMetricstoHostGpuEntity(metricDataList, hostGpuEntity);
        hostGpuEntities.add(hostGpuEntity);
      }
    }
    return new Pair<>(totalEntityCount,hostGpuEntities);
  }

  @Override
  public Pair<Integer, List<HostGpu> > getClusterHostGpus(String clusterUuid, Integer limit,
                                                                Integer page, String filter, String orderBy, String selectString)
    throws ClustermgmtServiceException, ValidationException {

    // Validate and check if cluster is PE/PC.
    if (isMulticluster(clusterUuid)) {
      log.debug("PC Cluster");
      throw new ClustermgmtNotSupportedException("Host Gpu Api is not supported on PC cluster uuid: " + clusterUuid);
    }

    return getGpuEntitiesFromIDF(limit, page, filter, orderBy, clusterUuid, HOST_GPU_CLUSTER_UUID_ATTR, selectString);
  }

  private InsightsInterfaceProto.GetEntitiesWithMetricsRet queryGpuProfiles(String clusterUuid,
                                                                            String filter, String orderBy, String gpuType)
  throws ClustermgmtServiceException, ValidationException
  {
    // Boolean expression from Odata filer
    final ClustermgmtOdataParser.Result odataResult =
      clustermgmtOdataParser.parse("/host-gpus", filter, orderBy, null, null, null);

    List<InsightsInterfaceProto.QueryOrderBy> queryOrderBy = odataResult.getOrderBy();
    InsightsInterfaceProto.BooleanExpression filterExpression = odataResult.getFilter();
    InsightsInterfaceProto.QueryGroupBy queryGroupBy = odataResult.getGroupBy();
    InsightsInterfaceProto.QueryOrderBy sortQueryOrderBy = null;
    if(queryOrderBy != null && !CollectionUtils.isEmpty(queryOrderBy)) {
      sortQueryOrderBy = queryOrderBy.get(0);
    }

    InsightsInterfaceProto.BooleanExpression joinExpression = filterExpression;

    // Boolean expression to get host gpus belong to this cluster
    InsightsInterfaceProto.DataValue.Builder value1 =
      InsightsInterfaceProto.DataValue.newBuilder().setStrValue(clusterUuid);
    InsightsInterfaceProto.BooleanExpression expression1 =
      clustermgmtResourceAdapter.adaptToBooleanExpression(HOST_GPU_CLUSTER_UUID_ATTR,
        InsightsInterfaceProto.ComparisonExpression.Operator.kEQ,
        value1);

    // Boolean expression to get host gpus of type
    InsightsInterfaceProto.DataValue.Builder value2 =
      InsightsInterfaceProto.DataValue.newBuilder().setStrValue(gpuType);
    InsightsInterfaceProto.BooleanExpression expression2 =
      clustermgmtResourceAdapter.adaptToBooleanExpression(HOST_GPU_TYPE_ATTR,
        InsightsInterfaceProto.ComparisonExpression.Operator.kEQ,
        value2);

    // Join Boolean Expression
    InsightsInterfaceProto.BooleanExpression expression =
      clustermgmtResourceAdapter.joinBooleanExpressions(
        expression1, InsightsInterfaceProto.BooleanExpression.Operator.kAnd, expression2);

    // Join Boolean Expression with Odate filter and above one.
    if (joinExpression != null)
      expression = clustermgmtResourceAdapter.joinBooleanExpressions(
        expression, InsightsInterfaceProto.BooleanExpression.Operator.kAnd, joinExpression);     

    // Get Entities
    InsightsInterfaceProto.GetEntitiesWithMetricsRet getEntitiesWithMetricsRet =
    ClustermgmtUtils.getEntitiesWithMetricsRet(null, null,
      HOST_GPU_ENTITY, entityDbProxy, ClustermgmtUtils.hostGpuAttributeList, null, clustermgmtResourceAdapter, expression, sortQueryOrderBy, null);
    log.debug("The RPC response GetEntitiesWithMetricsRet obtained {}", getEntitiesWithMetricsRet);
    return getEntitiesWithMetricsRet;
  }

  @Override
  public Pair<Integer, List<VirtualGpuProfile> > listVirtualGpuProfiles(String clusterUuid, Integer limit, Integer page, String filter, String orderBy)
    throws ClustermgmtServiceException, ValidationException {
    List<VirtualGpuProfile> virtualGpuProfileList = new ArrayList<>();
    Integer totalEntityCount = null;
    Integer currentCount = 0;
    Integer startItem = page * limit;
    Integer endItem = startItem + limit;
    // Validate and check if cluster is PE/PC.
    if (isMulticluster(clusterUuid)) {
      log.debug("PC Cluster");
      throw new ClustermgmtNotSupportedException("Host Gpu Apis is not supported on PC cluster uuid: " + clusterUuid);
    }

    InsightsInterfaceProto.GetEntitiesWithMetricsRet getEntitiesWithMetricsRet =
    queryGpuProfiles(clusterUuid, filter, orderBy, "kVirtual");


    if (!CollectionUtils.isEmpty(getEntitiesWithMetricsRet.getGroupResultsListList())) {
      final List<InsightsInterfaceProto.EntityWithMetric> entitiesWithMetric =
        getEntitiesWithMetricsRet.getGroupResultsList(0).getRawResultsList();
      totalEntityCount =
        (int) getEntitiesWithMetricsRet.getGroupResultsList(0).getTotalEntityCount();
      
      for (final InsightsInterfaceProto.EntityWithMetric entityWithMetric : entitiesWithMetric) {
        final List<InsightsInterfaceProto.MetricData> metricDataList =
          entityWithMetric.getMetricDataListList();
        VirtualGpuProfile tempVirtualGpuProfile = new VirtualGpuProfile();

        tempVirtualGpuProfile = clusterResourceAdapter.adaptIdfMetricsToVirtualGpuProfile(metricDataList, tempVirtualGpuProfile);
        if (currentCount >= startItem && currentCount < endItem) {
          virtualGpuProfileList.add(tempVirtualGpuProfile);
        }
        currentCount++;
      }
    }
    return new Pair<>(totalEntityCount, virtualGpuProfileList);
  }

  @Override
  public Pair<Integer, List<PhysicalGpuProfile> > listPhysicalGpuProfiles(String clusterUuid, Integer limit, Integer page, String filter, String orderBy)
    throws ClustermgmtServiceException, ValidationException {
    List<PhysicalGpuProfile> physicalGpuProfileList = new ArrayList<>();
    Integer totalEntityCount = null;
    Integer currentCount = 0;
    Integer startItem = page * limit;
    Integer endItem = startItem + limit;
    // Validate and check if cluster is PE/PC.
    if (isMulticluster(clusterUuid)) {
      log.debug("PC Cluster");
      throw new ClustermgmtNotSupportedException("Host Gpu Apis is not supported on PC cluster uuid: " + clusterUuid);
    }

    InsightsInterfaceProto.GetEntitiesWithMetricsRet getEntitiesWithMetricsRet =
    queryGpuProfiles(clusterUuid, filter, orderBy, "kPassthroughCompute");

    if (!CollectionUtils.isEmpty(getEntitiesWithMetricsRet.getGroupResultsListList())) {
      final List<InsightsInterfaceProto.EntityWithMetric> entitiesWithMetric =
        getEntitiesWithMetricsRet.getGroupResultsList(0).getRawResultsList();
      totalEntityCount =
        (int) getEntitiesWithMetricsRet.getGroupResultsList(0).getTotalEntityCount();
      for (final InsightsInterfaceProto.EntityWithMetric entityWithMetric : entitiesWithMetric) {
        final List<InsightsInterfaceProto.MetricData> metricDataList =
          entityWithMetric.getMetricDataListList();

        PhysicalGpuProfile tempPhysicalGpuProfile = new PhysicalGpuProfile();

        tempPhysicalGpuProfile = clusterResourceAdapter.adaptIdfMetricsToPhysicalGpuProfile(metricDataList, tempPhysicalGpuProfile);
        if (currentCount >= startItem && currentCount < endItem) {
          physicalGpuProfileList.add(tempPhysicalGpuProfile);;
        }
        currentCount++;
      }
    }

    InsightsInterfaceProto.GetEntitiesWithMetricsRet getEntitiesWithMetricsRet2 =
    queryGpuProfiles(clusterUuid, filter, orderBy, "kPassthroughGraphics");

    if (!CollectionUtils.isEmpty(getEntitiesWithMetricsRet2.getGroupResultsListList())) {
      final List<InsightsInterfaceProto.EntityWithMetric> entitiesWithMetric =
        getEntitiesWithMetricsRet2.getGroupResultsList(0).getRawResultsList();
      if (totalEntityCount == null) {
        totalEntityCount =
          (int) getEntitiesWithMetricsRet2.getGroupResultsList(0).getTotalEntityCount();
      } else {
        totalEntityCount +=
          (int) getEntitiesWithMetricsRet2.getGroupResultsList(0).getTotalEntityCount();
      }
      for (final InsightsInterfaceProto.EntityWithMetric entityWithMetric : entitiesWithMetric) {
        final List<InsightsInterfaceProto.MetricData> metricDataList =
          entityWithMetric.getMetricDataListList();
        PhysicalGpuProfile tempPhysicalGpuProfile = new PhysicalGpuProfile();

        tempPhysicalGpuProfile = clusterResourceAdapter.adaptIdfMetricsToPhysicalGpuProfile(metricDataList, tempPhysicalGpuProfile);
        if (currentCount >= startItem && currentCount < endItem) {
          physicalGpuProfileList.add(tempPhysicalGpuProfile);;
        }
        currentCount++;
      }
    }
    return new Pair<>(totalEntityCount, physicalGpuProfileList);
  }

  @Override
  public HostGpu getHostGpu(String clusterUuid, String hostUuid, String hostGpuUuid)
    throws ClustermgmtServiceException {
    // Validate and check if cluster is PE/PC.
    if (isMulticluster(clusterUuid)) {
      log.debug("PC Cluster");
      throw new ClustermgmtNotSupportedException("Host Gpu Api is supported on PC cluster uuid: " + clusterUuid);
    }

    // Validate and get host entity
    validateAndGetHostEntity(hostUuid);

    // Validate host belongs to cluster
    if(!validateHostBelongsToCluster(hostUuid, clusterUuid)) {
      throw new ClustermgmtNotFoundException(HOST_UUID_STRING + hostUuid + NOT_BELONG_TO_CLUSTER_STRING + clusterUuid);
    }

    HostGpu hostGpuEntity = new HostGpu();
    hostGpuEntity.setExtId(hostGpuUuid);

    //get the host gpu entity with hostGpuUuid and adapt to the entity type
    InsightsInterfaceProto.GetEntitiesWithMetricsRet getEntitiesWithMetricsRet =
      ClustermgmtUtils.getEntitiesWithMetricsRet(null, null,
        HOST_GPU_ENTITY, entityDbProxy, ClustermgmtUtils.hostGpuAttributeList, hostGpuUuid, clustermgmtResourceAdapter, null, null, null);
    log.debug("The RPC response GetEntitiesWithMetricsRet obtained {}", getEntitiesWithMetricsRet);

    if (!CollectionUtils.isEmpty(getEntitiesWithMetricsRet.getGroupResultsListList())) {
      final InsightsInterfaceProto.EntityWithMetric entityWithMetric = getEntitiesWithMetricsRet.getGroupResultsList(0).getRawResults(0);
      final List<InsightsInterfaceProto.MetricData> metricDataList = entityWithMetric.getMetricDataListList();
      hostGpuEntity = clusterResourceAdapter.adaptIdfHostGpuMetricstoHostGpuEntity(metricDataList, hostGpuEntity);
    }
    else{
      throw new ClustermgmtNotFoundException("Unknown Host Gpu Uuid: " + hostGpuUuid);
    }

    if (hostGpuEntity.getCluster().getUuid().equals(clusterUuid) && hostGpuEntity.getNodeUuid().equals(hostUuid)) {
      return hostGpuEntity;
    }
    else {
      throw new ClustermgmtNotFoundException("Host Gpu with uuid: " + hostGpuUuid + NOT_BELONG_TO_CLUSTER_STRING + clusterUuid +
        " and host: " + hostUuid);
    }
  }

  public void publishRpcMessageToNats(String clusterExtId,
                                      String rpcName,
                                      Message rpcArgs,
                                      String rpcRetType,
                                      ByteString pcTaskUuid,
                                      boolean isSync) throws ClustermgmtServiceException {
    boolean sendRpcRet = processorUtils.sendRemoteRpc(
      clusterExtId, GENESIS_SERVICE_NAME, GENESIS_PORT,
      rpcName,rpcArgs, rpcRetType, UuidUtils.getUUID(pcTaskUuid), false, isSync);
    if (!sendRpcRet) {
      log.info(ASYNC_PROCESSOR_REQUEST_FAILED);
      updateTaskBasedOnStatus(pcTaskUuid, 100, ErgonTypes.Task.Status.kFailed, ASYNC_PROCESSOR_REQUEST_FAILED);
      throw new ClustermgmtServiceException(ASYNC_PROCESSOR_REQUEST_FAILED);
    }
  }

  public void publishASyncJsonRpcMessageToNats(String clusterExtId,
                                               String pcTaskUuid,
                                               String payload) throws ClustermgmtServiceException {
    String url = GENESIS_JSON_URL_PREFIX + REMOTE_CLUSTER_UUID_STRING + clusterExtId + METHOD_AND_URL_STRING;
    boolean sendRpcRet = processorUtils.sendAsyncRemoteHttpRequest(pcTaskUuid, url, payload, new HttpHeaders(),
      HttpMethod.POST);
    if (!sendRpcRet) {
      log.info(ASYNC_PROCESSOR_REQUEST_FAILED);
      updateTaskBasedOnStatus(UuidUtils.getByteStringFromUUID(pcTaskUuid), 100, ErgonTypes.Task.Status.kFailed, ASYNC_PROCESSOR_REQUEST_FAILED);
      throw new ClustermgmtServiceException(ASYNC_PROCESSOR_REQUEST_FAILED);
    }
  }

  @Override
  public TaskReference renameHost(HostNameParam body, String clusterExtId, String hostExtId)
    throws ClustermgmtServiceException, ValidationException {
    TaskReference task = new TaskReference();
    String hostName = body.getName();
    String oldName = "";
    boolean isPc = isMulticluster(clusterExtId);
    if(isPc) {
      throw new ClustermgmtNotSupportedException("Host Rename is not supported on PC: " + clusterExtId);
    }

    // PC-PE Capability check
    if (!isPc) isRequestAllowed(clusterExtId, ClustermgmtUtils.RENAME_HOST);

    // Validate host belongs to cluster
    if(!validateHostBelongsToCluster(hostExtId, clusterExtId)) {
      throw new ClustermgmtNotFoundException(HOST_UUID_STRING + hostExtId + NOT_BELONG_TO_CLUSTER_STRING + clusterExtId);
    }

    // Get Entities for validating duplicate host name.
    InsightsInterfaceProto.GetEntitiesWithMetricsRet getEntitiesWithMetricsRet =
      ClustermgmtUtils.getEntitiesWithMetricsRet(null, null,
        NODE_ENTITY, entityDbProxy, Arrays.asList("node_name"), null, clustermgmtResourceAdapter, null, null, null);
    log.debug("The RPC response GetEntitiesWithMetricsRet obtained {}", getEntitiesWithMetricsRet);

    if (!CollectionUtils.isEmpty(getEntitiesWithMetricsRet.getGroupResultsListList())) {
      final List<InsightsInterfaceProto.EntityWithMetric> entitiesWithMetric =
        getEntitiesWithMetricsRet.getGroupResultsList(0).getRawResultsList();
      for (final InsightsInterfaceProto.EntityWithMetric entityWithMetric : entitiesWithMetric) {
        final List<InsightsInterfaceProto.MetricData> metricDataList =
          entityWithMetric.getMetricDataListList();
        String hostUuid = entityWithMetric.getEntityGuid().getEntityId();
        String nodeName = null;
        for (final InsightsInterfaceProto.MetricData metricData : metricDataList) {
          if (!metricData.hasName() || metricData.getValueListCount() <= 0) {
            continue;
          }
          switch (metricData.getName()) {
            case "node_name":
              nodeName = metricData.getValueList(0).getValue().getStrValue();
              break;
            default:
              break;
          }
        }

        if(hostUuid.equals(hostExtId))
          oldName = nodeName;

        if (!hostUuid.equals(hostExtId) && hostName.equalsIgnoreCase(nodeName)) {
          throw new ClustermgmtDuplicateHostNameException("Host name " + hostName +
            " already belongs to another host with uuid "
            + hostUuid + ". Please provide a different hostname.");
        }
        else if(hostUuid.equals(hostExtId) && hostName.equals(nodeName)) {
          throw new ClustermgmtDuplicateHostNameException("Input host with uuid " + hostUuid +
            " already has host name " + hostName);
        }
      }
    }

    // Cluster EntityId
    ErgonTypes.EntityId entityId =
      ErgonTypes.EntityId.newBuilder()
        .setEntityType(ErgonTypes.EntityId.Entity.kCluster)
        .setEntityId(UuidUtils.getByteStringFromUUID(clusterExtId)).build();

    // Create PC task
    ErgonInterface.TaskCreateArg createArg = ClustermgmtUtils.createTaskArg(null, GENESIS_COMPONENT_NAME,
      "Host Rename", "Host Rename", null,null, entityId,
      RequestContextHelper.getRpcRequestContext());
    ErgonInterface.TaskCreateRet taskCreateRet = ergonProxy.doExecute(ErgonProxyImpl.ErgonRpcName.TASK_CREATE, createArg);
    task.setExtId(taskCreateRet.getExtId());

    // Host Rename Arg
    GenesisInterfaceProto.UpdateHostnameArg updateHostnameArg =
      GenesisInterfaceProto.UpdateHostnameArg.newBuilder()
        .setHostname(hostName)
        .setHostUuid(UuidUtils.getByteStringFromUUID(hostExtId)).build();
    String rpcRetType = "com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto$UpdateHostnameRet";
    publishRpcMessageToNats(clusterExtId, "UpdateHostname", updateHostnameArg, rpcRetType, taskCreateRet.getUuid(), true);

    return task;
  }

  @Override
  public TaskReference updateCluster(Cluster body, String clusterExtId, String batchTask)
    throws ClustermgmtServiceException {
    TaskReference task = new TaskReference();
    boolean isPc = isMulticluster(clusterExtId);

    // PC-PE Capability check
    if (!isPc) isRequestAllowed(clusterExtId, ClustermgmtUtils.UPDATE_CLUSTER);

    // FQDN should not be available in name server - since it is the one that resolves the FQDN to IP Address
    if(ClustermgmtUtils.doesNameServerHaveFqdn(body)){
      throw new ClustermgmtInvalidInputException("The name server cannot have an FQDN value provided, as it is responsible for resolving all FQDN addresses");
    }

    // Cluster EntityId
    ErgonTypes.EntityId entityId = ClustermgmtUtils.getClusterEntityId(clusterExtId);

    // Update cluster Arg
    GenesisInterfaceProto.UpdateClusterArg.Builder updateClusterArgBuilder = GenesisInterfaceProto.UpdateClusterArg.newBuilder();
    GenesisInterfaceProto.UpdateClusterArg updateClusterArg = null;

    if (isPc) {
      // Populate Update cluster Arg
      updateClusterArg = clusterResourceAdapter.adaptClusterEntityToClusterUpdateArg(body, updateClusterArgBuilder, null);
      // Invoke Genesis RPC which returns task to track UpdateCluster
      GenesisInterfaceProto.UpdateClusterRet updateClusterRet =
              genesisProxy.doExecute(GenesisProxyImpl.GenesisRpcName.UPDATE_CLUSTER, updateClusterArg, RequestContextHelper.getRpcRequestContext());
      // Update the task to be returned
      if (updateClusterRet.hasTaskUuid()) {
        final String taskExtId = ErgonTaskUtils.getTaskReferenceExtId(UuidUtils.getUUID(updateClusterRet.getTaskUuid()));
        task.setExtId(taskExtId);
      }
    }
    else {
      ByteString batchTaskBytes = null;
      if (batchTask != null) {
        batchTaskBytes = UuidUtils.getByteStringFromUUID(batchTask);
      }

      // Check if updateCluster supports http proxy configuration.
      Integer genesisApiVersion = getGenesisApiVersion(clusterExtId);
      if (body.getNetwork() != null &&
        (body.getNetwork().getHttpProxyList() != null || body.getNetwork().getHttpProxyWhiteList() != null)) {
        if (genesisApiVersion < GenesisInterfaceProto.GenesisApiVersion.kPlannedOutageManagerSupportedVersion.getNumber()) {
          throw new ClustermgmtNotSupportedException(
            "Http Proxy configuration is not supported on cluster with uuid: " + clusterExtId);
        }
      }

      // Create PC task
      ErgonInterface.TaskCreateArg createArg = ClustermgmtUtils.createTaskArg(null, GENESIS_COMPONENT_NAME,
        "Update Cluster", "Update Cluster", batchTaskBytes,null, entityId,
        RequestContextHelper.getRpcRequestContext());
      ErgonInterface.TaskCreateRet taskCreateRet = ergonProxy.doExecute(ErgonProxyImpl.ErgonRpcName.TASK_CREATE, createArg);
      task.setExtId(taskCreateRet.getExtId());

      boolean is_async_update_cluster_supported = (genesisApiVersion >=
        GenesisInterfaceProto.GenesisApiVersion.kAsyncUpdateClusterSupportedVersion.getNumber());

      if (is_async_update_cluster_supported) {
        // Set Parent Task uuid in the UpdateClusterArg. Genesis on PE would create a child task
        updateClusterArg = clusterResourceAdapter.adaptClusterEntityToClusterUpdateArg(
          body, updateClusterArgBuilder, taskCreateRet.getUuid());

        String rpcRetType = "com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto$UpdateClusterRet";
        publishRpcMessageToNats(clusterExtId, "UpdateCluster", updateClusterArg,
                                rpcRetType, taskCreateRet.getUuid(), false);
      }
      else {
        // Make a synchronous call to Genesis on PE
        updateClusterArg = clusterResourceAdapter.adaptClusterEntityToClusterUpdateArg(body, updateClusterArgBuilder, null);

        String rpcRetType = "com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto$UpdateClusterSyncRet";
        publishRpcMessageToNats(clusterExtId, "UpdateCluster", updateClusterArg,
                                rpcRetType, taskCreateRet.getUuid(), true);
      }
    }

    return task;
  }

  @Override
  public TaskReference updateSnmpStatus(SnmpStatusParam status, String clusterExtId, String batchTask)
    throws ClustermgmtServiceException {
    TaskReference task = new TaskReference();
    boolean isPc = isMulticluster(clusterExtId);

    // PC-PE Capability check
    if (!isPc) isRequestAllowed(clusterExtId, ClustermgmtUtils.UPDATE_SNMP_STATUS);

    // Cluster EntityId
    ErgonTypes.EntityId entityId = ClustermgmtUtils.getClusterEntityId(clusterExtId);

    // Update Snmp Status arg
    GenesisInterfaceProto.UpdateSnmpStatusArg updateSnmpStatusArg = null;

    if(isPc) {
      // Populate Snmp Status arg.
      updateSnmpStatusArg = GenesisInterfaceProto.UpdateSnmpStatusArg.newBuilder()
        .setStatus(status.getIsEnabled()).build();

      // Invoke RPC
      GenesisInterfaceProto.UpdateSnmpStatusRet updateSnmpStatusRet =
        genesisProxy.doExecute(GenesisProxyImpl.GenesisRpcName.UPDATE_SNMP_STATUS, updateSnmpStatusArg, RequestContextHelper.getRpcRequestContext());

      // Update the task to be returned
      if (updateSnmpStatusRet.hasTaskUuid()) {
        final String taskExtId = ErgonTaskUtils.getTaskReferenceExtId(
          UuidUtils.getUUID(updateSnmpStatusRet.getTaskUuid()));
        task.setExtId(taskExtId);
      }
    }
    else {
      ByteString batchTaskBytes = null;
      if (batchTask != null) {
        batchTaskBytes = UuidUtils.getByteStringFromUUID(batchTask);
      }
      // Create PC task
      ErgonInterface.TaskCreateArg createArg = ClustermgmtUtils.createTaskArg(null, GENESIS_COMPONENT_NAME,
        "Update Snmp Status", "Update Snmp Status", batchTaskBytes,null, entityId,
        RequestContextHelper.getRpcRequestContext());
      ErgonInterface.TaskCreateRet taskCreateRet = ergonProxy.doExecute(ErgonProxyImpl.ErgonRpcName.TASK_CREATE, createArg);
      task.setExtId(taskCreateRet.getExtId());

      boolean is_async_update_snmp_status_supported = (getGenesisApiVersion(clusterExtId) >=
        GenesisInterfaceProto.GenesisApiVersion.kAsyncUpdateClusterSupportedVersion.getNumber());

      if (is_async_update_snmp_status_supported) {
        // Set Parent Task uuid in the updateSnmpStatusArg. Genesis on PE would create a child task
        updateSnmpStatusArg = GenesisInterfaceProto.UpdateSnmpStatusArg.newBuilder()
          .setStatus(status.getIsEnabled()).setParentTaskUuid(taskCreateRet.getUuid()).build();

        String rpcRetType = "com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto$UpdateSnmpStatusRet";
        publishRpcMessageToNats(clusterExtId, "UpdateSnmpStatus", updateSnmpStatusArg,
                                rpcRetType, taskCreateRet.getUuid(), false);
      }
      else {
        // Make a synchronous call to Genesis on PE.
        updateSnmpStatusArg = GenesisInterfaceProto.UpdateSnmpStatusArg.newBuilder()
          .setStatus(status.getIsEnabled()).build();
        String rpcRetType = "com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto$UpdateSnmpStatusRet";
        publishRpcMessageToNats(clusterExtId, "UpdateSnmpStatus", updateSnmpStatusArg,
                                rpcRetType, taskCreateRet.getUuid(), true);
      }
    }

    return task;
  }

  public void validateSnmpTransports(SnmpTransport transport)
    throws ValidationException {
    if (!ValidationUtils.isValidSystemPort(transport.getPort())) {
      throw new ValidationException(String.format(
        "Port %s specified is invalid", transport.getPort()));
    }
  }

  @Override
  public TaskReference addSnmpTransport(SnmpTransport body,
                                        String clusterExtId, String batchTask)
    throws ClustermgmtServiceException, ValidationException {
    TaskReference task = new TaskReference();
    boolean isPc = isMulticluster(clusterExtId);

    // PC-PE Capability check
    if (!isPc) isRequestAllowed(clusterExtId, ClustermgmtUtils.ADD_SNMP_TRANSPORT);

    // Validate Snmp transports provided
    validateSnmpTransports(body);

    // Cluster EntityId
    ErgonTypes.EntityId entityId = ClustermgmtUtils.getClusterEntityId(clusterExtId);

    // Add snmp transports arg
    GenesisInterfaceProto.AddSnmpTransportsArg.Builder builder = GenesisInterfaceProto.AddSnmpTransportsArg.newBuilder();
    GenesisInterfaceProto.AddSnmpTransportsArg addTransportsArg = null;


    if(isPc) {
      // Populate snmp transports arg.
      addTransportsArg = clusterResourceAdapter.adaptSnmpTransportsToAddTransportsArg(body, builder, null);

      // Invoke Genesis RPC which returns task to track addTransports
      GenesisInterfaceProto.AddSnmpTransportsRet addTransportsRet =
        genesisProxy.doExecute(GenesisProxyImpl.GenesisRpcName.ADD_SNMP_TRANSPORTS, addTransportsArg, RequestContextHelper.getRpcRequestContext());

      // Update the task to be returned
      if (addTransportsRet.hasTaskUuid()) {
        final String taskExtId = ErgonTaskUtils.getTaskReferenceExtId(UuidUtils.getUUID(addTransportsRet.getTaskUuid()));
        task.setExtId(taskExtId);
      }
    }
    else {
      ByteString batchTaskBytes = null;
      if (batchTask != null) {
        batchTaskBytes = UuidUtils.getByteStringFromUUID(batchTask);
      }
      // Create PC task
      ErgonInterface.TaskCreateArg createArg = ClustermgmtUtils.createTaskArg(null, GENESIS_COMPONENT_NAME,
        "Add Snmp Transports", "Add snmp transports", batchTaskBytes,null, entityId,
        RequestContextHelper.getRpcRequestContext());
      ErgonInterface.TaskCreateRet taskCreateRet = ergonProxy.doExecute(ErgonProxyImpl.ErgonRpcName.TASK_CREATE, createArg);
      task.setExtId(taskCreateRet.getExtId());

      boolean is_async_add_snmp_transport_supported = (getGenesisApiVersion(clusterExtId) >=
        GenesisInterfaceProto.GenesisApiVersion.kAsyncUpdateClusterSupportedVersion.getNumber());

      if (is_async_add_snmp_transport_supported) {
        // Set Parent Task uuid in addTransportsArg. Genesis on PE would create a child task
        addTransportsArg = clusterResourceAdapter.adaptSnmpTransportsToAddTransportsArg(
          body, builder, taskCreateRet.getUuid());

        String rpcRetType = "com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto$AddSnmpTransportsRet";
        publishRpcMessageToNats(clusterExtId, "AddSnmpTransports", addTransportsArg,
                                rpcRetType, taskCreateRet.getUuid(), false);
      }
      else {
        // Populate snmp transports arg.
        addTransportsArg = clusterResourceAdapter.adaptSnmpTransportsToAddTransportsArg(body, builder, null);

        // Make a synchronous call to Genesis on PE
        String rpcRetType = "com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto$AddSnmpTransportsRet";
        publishRpcMessageToNats(clusterExtId, "AddSnmpTransports", addTransportsArg,
          rpcRetType, taskCreateRet.getUuid(), true);
      }
    }

    return task;
  }

  @Override
  public TaskReference removeSnmpTransport(SnmpTransport body,
                                           String clusterExtId,
                                           String batchTask)
    throws ClustermgmtServiceException, ValidationException {
    TaskReference task = new TaskReference();
    boolean isPc = isMulticluster(clusterExtId);

    // PC-PE Capability check
    if (!isPc) isRequestAllowed(clusterExtId, ClustermgmtUtils.REMOVE_SNMP_TRANSPORT);

    // Validate Snmp transports provided
    validateSnmpTransports(body);

    // Cluster EntityId
    ErgonTypes.EntityId entityId = ClustermgmtUtils.getClusterEntityId(clusterExtId);

    // Remove snmp transports arg
    GenesisInterfaceProto.RemoveSnmpTransportsArg.Builder builder = GenesisInterfaceProto.RemoveSnmpTransportsArg.newBuilder();
    GenesisInterfaceProto.RemoveSnmpTransportsArg removeTransportsArg = null;

    if(isPc) {
      // Remove snmp transports arg
      removeTransportsArg = clusterResourceAdapter.adaptSnmpTransportsToRemoveTransportsArg(body, builder, null);

      // Invoke Genesis RPC which returns task to track removeTransports
      GenesisInterfaceProto.RemoveSnmpTransportsRet removeTransportsRet =
        genesisProxy.doExecute(GenesisProxyImpl.GenesisRpcName.REMOVE_SNMP_TRANSPORTS, removeTransportsArg, RequestContextHelper.getRpcRequestContext());

      // Update the task to be returned
      if (removeTransportsRet.hasTaskUuid()) {
        final String taskExtId = ErgonTaskUtils.getTaskReferenceExtId(UuidUtils.getUUID(
          removeTransportsRet.getTaskUuid()));
        task.setExtId(taskExtId);
      }
    }
    else {
      ByteString batchTaskBytes = null;
      if (batchTask != null) {
        batchTaskBytes = UuidUtils.getByteStringFromUUID(batchTask);
      }
      // Create PC task
      ErgonInterface.TaskCreateArg createArg = ClustermgmtUtils.createTaskArg(null, GENESIS_COMPONENT_NAME,
        "Remove Snmp Transports", "Remove snmp transports", batchTaskBytes,null, entityId,
        RequestContextHelper.getRpcRequestContext());
      ErgonInterface.TaskCreateRet taskCreateRet = ergonProxy.doExecute(ErgonProxyImpl.ErgonRpcName.TASK_CREATE, createArg);
      task.setExtId(taskCreateRet.getExtId());

      boolean is_async_remove_transports_supported = (getGenesisApiVersion(clusterExtId) >=
        GenesisInterfaceProto.GenesisApiVersion.kAsyncUpdateClusterSupportedVersion.getNumber());

      if (is_async_remove_transports_supported) {
        // Set Parent Task uuid in the RemoveTransportsArg. Genesis on PE would create a child task
        removeTransportsArg = clusterResourceAdapter.adaptSnmpTransportsToRemoveTransportsArg(
          body, builder, taskCreateRet.getUuid());

        String rpcRetType = "com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto$RemoveSnmpTransportsRet";
        publishRpcMessageToNats(clusterExtId, "RemoveSnmpTransports", removeTransportsArg,
                                rpcRetType, taskCreateRet.getUuid(), false);
      }
      else {
        // Make a synchronous call to Genesis on PE
        removeTransportsArg = clusterResourceAdapter.adaptSnmpTransportsToRemoveTransportsArg(body, builder, null);

        String rpcRetType = "com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto$RemoveSnmpTransportsRet";
        publishRpcMessageToNats(clusterExtId, "RemoveSnmpTransports", removeTransportsArg,
                                rpcRetType, taskCreateRet.getUuid(), false);
      }
    }

    return task;
  }

  public void validateSnmpUser(SnmpUser snmpUser) throws ValidationException {
    if (!ValidationUtils.containsAlphaNumUnicodeUnderscoreDash(snmpUser.getUsername())) {
      throw new ValidationException("Invalid username " + snmpUser.getUsername());
    }

    if (((snmpUser.getPrivKey() == null || snmpUser.getPrivKey().isEmpty()) &&
      snmpUser.getPrivType() != null)
      || (snmpUser.getPrivKey() != null && snmpUser.getPrivType() == null)) {
      throw new ValidationException(
        "Privilge type and key should be both specified");
    }
  }

  @Override
  public SnmpUser getSnmpUser(String clusterExtId, String userExtId)
    throws ClustermgmtServiceException {
    GenesisInterfaceProto.SnmpUser snmpUserProto;
    boolean config_present_in_idf;
    InsightsInterfaceProto.BooleanExpression where_clause;

    if (isMulticluster(clusterExtId)) {
      log.debug("PC Cluster");
      config_present_in_idf = true;
      where_clause = clustermgmtResourceAdapter.constructNotExistsBooleanExpression(MASTER_CLUSTER_UUID_ATTRIBUTE);
    }
    else{
      log.debug("PE Cluster");
      isRequestAllowed(clusterExtId, ClustermgmtUtils.GET_SNMP_USER);
      // If Async update cluster is supported on the PE, then SNMP config will be present in IDF.
      config_present_in_idf = (getGenesisApiVersion(clusterExtId) >=
        GenesisInterfaceProto.GenesisApiVersion.kAsyncUpdateClusterSupportedVersion.getNumber());
      where_clause = clustermgmtResourceAdapter.adaptToBooleanExpression(
        MASTER_CLUSTER_UUID_ATTRIBUTE, InsightsInterfaceProto.ComparisonExpression.Operator.kEQ,
        InsightsInterfaceProto.DataValue.newBuilder().setStrValue(clusterExtId));
    }

    // SNMP user config is written to IDF. We can directly read it from IDF.
    if (config_present_in_idf) {
      log.debug("SNMP User config for user {} on cluster {} is present in IDF", clusterExtId, userExtId);
      List<SnmpUser> snmpUsers = ClustermgmtUtils.getSnmpUserEntities(
        userExtId, where_clause, entityDbProxy, clustermgmtResourceAdapter, clusterResourceAdapter);
      if (snmpUsers.isEmpty())
        throw new ClustermgmtServiceException("SNMP User with uuid: " + userExtId + NOT_BELONG_TO_CLUSTER_STRING + clusterExtId);
      log.debug("SNMP User with uuid: {} on cluster {}: {}", userExtId, clusterExtId, snmpUsers.get(0));
      return snmpUsers.get(0);
    }

    // Snmp User Config is not present in IDF. We read it from Zeus config.
    ZeusConfiguration zkConfig = ClustermgmtUtils.getZkconfig(clusterExtId, multiClusterZeusConfig);

    List<SnmpUser> snmpUsers = clusterResourceAdapter.getSnmpUsers(zkConfig);
    for (SnmpUser snmpUser: snmpUsers)
      if(snmpUser.getExtId().equals(userExtId))
        return snmpUser;

    throw new ClustermgmtServiceException("SNMP User with uuid: " + userExtId + NOT_BELONG_TO_CLUSTER_STRING + clusterExtId);
  }

  @Override
  public TaskReference addSnmpUser(SnmpUser body, String clusterExtId, String batchTask)
    throws ClustermgmtServiceException, ValidationException {
    TaskReference task = new TaskReference();
    boolean isPc = isMulticluster(clusterExtId);

    // PC-PE Capability check
    if (!isPc) isRequestAllowed(clusterExtId, ClustermgmtUtils.ADD_SNMP_USER);

    // Validate Snmp User provided
    validateSnmpUser(body);

    // Cluster EntityId
    ErgonTypes.EntityId entityId = ClustermgmtUtils.getClusterEntityId(clusterExtId);

    // add snmp user arg
    GenesisInterfaceProto.AddSnmpUserArg.Builder builder = GenesisInterfaceProto.AddSnmpUserArg.newBuilder();
    GenesisInterfaceProto.AddSnmpUserArg addSnmpUserArg = null;

    if(isPc) {
      // Populate snmp user arg.
      addSnmpUserArg = clusterResourceAdapter.adaptSnmpUserToAddSnmpUserArg(body, builder, null);

      // Invoke Genesis RPC which returns task to track addSnmpUser
      GenesisInterfaceProto.AddSnmpUserRet addSnmpUserRet =
        genesisProxy.doExecute(GenesisProxyImpl.GenesisRpcName.ADD_SNMP_USER, addSnmpUserArg, RequestContextHelper.getRpcRequestContext());

      // Update the task to be returned
      if (addSnmpUserRet.hasTaskUuid()) {
        final String taskExtId = ErgonTaskUtils.getTaskReferenceExtId(UuidUtils.getUUID(addSnmpUserRet.getTaskUuid()));
        task.setExtId(taskExtId);
      }
    }
    else {
      ByteString batchTaskBytes = null;
      if (batchTask != null) {
        batchTaskBytes = UuidUtils.getByteStringFromUUID(batchTask);
      }
      // Create PC task
      ErgonInterface.TaskCreateArg createArg = ClustermgmtUtils.createTaskArg(null, GENESIS_COMPONENT_NAME,
        "Add Snmp User", "Add snmp user", batchTaskBytes,null, entityId,
        RequestContextHelper.getRpcRequestContext());
      ErgonInterface.TaskCreateRet taskCreateRet = ergonProxy.doExecute(ErgonProxyImpl.ErgonRpcName.TASK_CREATE, createArg);
      task.setExtId(taskCreateRet.getExtId());

      boolean is_async_add_snmp_user_supported = (getGenesisApiVersion(clusterExtId) >=
        GenesisInterfaceProto.GenesisApiVersion.kAsyncUpdateClusterSupportedVersion.getNumber());

      if (is_async_add_snmp_user_supported) {
        // Set Parent Task uuid in addSnmpUserArg. Genesis on PE would create a child task
        addSnmpUserArg = clusterResourceAdapter.adaptSnmpUserToAddSnmpUserArg(body, builder, taskCreateRet.getUuid());

        String rpcRetType = "com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto$AddSnmpUserRet";
        publishRpcMessageToNats(clusterExtId, "AddSnmpUser", addSnmpUserArg,
                                rpcRetType, taskCreateRet.getUuid(), false);
      }
      else {
        // Make a synchronous call to Genesis on PE.
        addSnmpUserArg = clusterResourceAdapter.adaptSnmpUserToAddSnmpUserArg(body, builder, null);

        String rpcRetType = "com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto$AddSnmpUserRet";
        publishRpcMessageToNats(clusterExtId, "AddSnmpUser", addSnmpUserArg,
                                rpcRetType, taskCreateRet.getUuid(), true);
      }
    }

    return task;
  }

  @Override
  public TaskReference updateSnmpUser(SnmpUser snmpUser, String clusterExtId, String userExtId, String batchTask)
    throws ClustermgmtServiceException, ValidationException {
    TaskReference task = new TaskReference();
    boolean isPc = isMulticluster(clusterExtId);

    // PC-PE Capability check
    if (!isPc) isRequestAllowed(clusterExtId, ClustermgmtUtils.UPDATE_SNMP_USER);

    // Validate Snmp User provided
    validateSnmpUser(snmpUser);

    // Set ExtId in SnmpUser Payload
    snmpUser.setExtId(userExtId);

    // Cluster EntityId
    ErgonTypes.EntityId entityId = ClustermgmtUtils.getClusterEntityId(clusterExtId);


    // update snmp user arg
    GenesisInterfaceProto.UpdateSnmpUserArg.Builder builder = GenesisInterfaceProto.UpdateSnmpUserArg.newBuilder();
    GenesisInterfaceProto.UpdateSnmpUserArg updateSnmpUserArg = null;

    if(isPc) {
      // update snmp user arg
      updateSnmpUserArg = clusterResourceAdapter.adaptSnmpUserToUpdateSnmpUserArg(snmpUser, builder, null);

      // Invoke Genesis RPC which returns task to track UpdateSnmpUser
      GenesisInterfaceProto.UpdateSnmpUserRet updateSnmpUserRet =
        genesisProxy.doExecute(GenesisProxyImpl.GenesisRpcName.UPDATE_SNMP_USER, updateSnmpUserArg, RequestContextHelper.getRpcRequestContext());

      // Update the task to be returned
      if (updateSnmpUserRet.hasTaskUuid()) {
        final String taskExtId = ErgonTaskUtils.getTaskReferenceExtId(UuidUtils.getUUID(
          updateSnmpUserRet.getTaskUuid()));
        task.setExtId(taskExtId);
      }
    }
    else {
      ByteString batchTaskBytes = null;
      if (batchTask != null) {
        batchTaskBytes = UuidUtils.getByteStringFromUUID(batchTask);
      }
      // Create PC task
      ErgonInterface.TaskCreateArg createArg = ClustermgmtUtils.createTaskArg(null, GENESIS_COMPONENT_NAME,
        "Update Snmp User", "Update snmp user", batchTaskBytes,null, entityId,
        RequestContextHelper.getRpcRequestContext());
      ErgonInterface.TaskCreateRet taskCreateRet = ergonProxy.doExecute(ErgonProxyImpl.ErgonRpcName.TASK_CREATE, createArg);
      task.setExtId(taskCreateRet.getExtId());

      boolean is_async_update_snmp_user_supported = (getGenesisApiVersion(clusterExtId) >=
        GenesisInterfaceProto.GenesisApiVersion.kAsyncUpdateClusterSupportedVersion.getNumber());

      if (is_async_update_snmp_user_supported) {
        // Set Parent Task uuid in the UpdateClusterArg. Genesis on PE would create a child task
        updateSnmpUserArg = clusterResourceAdapter.adaptSnmpUserToUpdateSnmpUserArg(
          snmpUser, builder, taskCreateRet.getUuid());

        String rpcRetType = "com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto$UpdateSnmpUserRet";
        publishRpcMessageToNats(clusterExtId, "UpdateSnmpUser", updateSnmpUserArg,
                                rpcRetType, taskCreateRet.getUuid(), false);
      }
      else {
        // Make a synchronous call to Genesis on PE
        updateSnmpUserArg = clusterResourceAdapter.adaptSnmpUserToUpdateSnmpUserArg(snmpUser, builder, null);

        String rpcRetType = "com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto$UpdateSnmpUserRet";
        publishRpcMessageToNats(clusterExtId, "UpdateSnmpUser", updateSnmpUserArg,
                                rpcRetType, taskCreateRet.getUuid(), true);
      }
    }

    return task;
  }

  @Override
  public TaskReference deleteSnmpUser(String clusterExtId, String userExtId, String batchTask)
    throws ClustermgmtServiceException, ValidationException {
    TaskReference task = new TaskReference();
    boolean isPc = isMulticluster(clusterExtId);

    // PC-PE Capability check
    if (!isPc) isRequestAllowed(clusterExtId, ClustermgmtUtils.DELETE_SNMP_USER);

    // Cluster EntityId
    ErgonTypes.EntityId entityId = ClustermgmtUtils.getClusterEntityId(clusterExtId);

    // delete snmp user arg
    GenesisInterfaceProto.DeleteSnmpUserArg.Builder builder = GenesisInterfaceProto.DeleteSnmpUserArg.newBuilder();
    GenesisInterfaceProto.DeleteSnmpUserArg deleteSnmpUserArg = null;

    if(isPc) {
      // Populate snmp user arg.
      deleteSnmpUserArg = clusterResourceAdapter.adaptSnmpUserToDeleteSnmpUserArg(userExtId, builder, null);

      // Invoke RPC
      GenesisInterfaceProto.DeleteSnmpUserRet deleteSnmpUserRet =
        genesisProxy.doExecute(GenesisProxyImpl.GenesisRpcName.DELETE_SNMP_USER, deleteSnmpUserArg, RequestContextHelper.getRpcRequestContext());

      // Update the task to be returned
      if (deleteSnmpUserRet.hasTaskUuid()) {
        final String taskExtId = ErgonTaskUtils.getTaskReferenceExtId(UuidUtils.getUUID(
          deleteSnmpUserRet.getTaskUuid()));
        task.setExtId(taskExtId);
      }
    }
    else {
      ByteString batchTaskBytes = null;
      if (batchTask != null) {
        batchTaskBytes = UuidUtils.getByteStringFromUUID(batchTask);
      }
      // Create PC task
      ErgonInterface.TaskCreateArg createArg = ClustermgmtUtils.createTaskArg(null, GENESIS_COMPONENT_NAME,
        "Delete Snmp User", "Delete snmp user", batchTaskBytes, null, entityId,
        RequestContextHelper.getRpcRequestContext());
      ErgonInterface.TaskCreateRet taskCreateRet = ergonProxy.doExecute(ErgonProxyImpl.ErgonRpcName.TASK_CREATE, createArg);
      task.setExtId(taskCreateRet.getExtId());

      boolean is_async_delete_snmp_user_supported = (getGenesisApiVersion(clusterExtId) >=
        GenesisInterfaceProto.GenesisApiVersion.kAsyncUpdateClusterSupportedVersion.getNumber());

      if(is_async_delete_snmp_user_supported) {
        // Set Parent Task uuid in the DeleteSnmpUserArg. Genesis on PE would create a child task
        deleteSnmpUserArg = clusterResourceAdapter.adaptSnmpUserToDeleteSnmpUserArg(
          userExtId, builder, taskCreateRet.getUuid());
        String rpcRetType = "com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto$DeleteSnmpUserRet";
        publishRpcMessageToNats(clusterExtId, "DeleteSnmpUser", deleteSnmpUserArg,
                                rpcRetType, taskCreateRet.getUuid(), false);
      }
      else {
        // Make a synchronous call to Genesis on PE
        deleteSnmpUserArg = clusterResourceAdapter.adaptSnmpUserToDeleteSnmpUserArg(userExtId, builder, null);

        String rpcRetType = "com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto$DeleteSnmpUserRet";
        publishRpcMessageToNats(clusterExtId, "DeleteSnmpUser", deleteSnmpUserArg,
                                rpcRetType, taskCreateRet.getUuid(), true);
      }
    }
    return task;
  }

  public void validateSnmpTrap(SnmpTrap snmpTrap) throws ValidationException {
    if (snmpTrap.getEngineId() != null) {
      if (!snmpTrap.getEngineId().toLowerCase().startsWith("0x")) {
        throw new ValidationException(
          "Engine id is an hex string, should start with '0x'");
      }
      // Engine id must be between 5 and 32 octates long
      // and in hexadecimal  format. It translates to 10 and 64 characters long
      // in String representation. Ensuring leading '0x' (indicating hexadecimal
      // format), engine id must be 12 and 66 characters long.
      if (snmpTrap.getEngineId().length() < 12
        || snmpTrap.getEngineId().length() > 66) {
        throw new ValidationException(
          "Engine id must be between 10 and 64 characters " +
            "(excluding leading '0x')");
      }
      // The number of digits for hexadecimal engine id cannot be odd
      if (snmpTrap.getEngineId().length() % 2 != 0) {
        throw new ValidationException(
          "The number of digits for hexadecimal engine id cannot be odd");
      }
    }

    if (snmpTrap.getPort() != null &&
      !ValidationUtils.isValidSystemPort(snmpTrap.getPort())) {
      throw new ValidationException(String.format(
        "Specified port no %s is invalid", snmpTrap.getPort()));
    }

    if (snmpTrap.getVersion() == SnmpTrapVersion.V2) {
      if (snmpTrap.getCommunityString() == null) {
        log.warn("Community string is not specified for snmp version 2");
      }
      if (snmpTrap.getUsername() != null) {
        log.warn("Trap username is not required for snmp version 2");
      }
    }
    if (snmpTrap.getVersion() == SnmpTrapVersion.V3) {
      if (snmpTrap.getCommunityString() != null) {
        log.warn("Community string is not required for snmp version 3");
      }
      if (snmpTrap.getUsername() == null) {
        throw new ValidationException(
          "Trap username is required for snmp version 3");
      }
    }
  }

  @Override
  public SnmpTrap getSnmpTrap(String clusterExtId, String trapExtId)
    throws ClustermgmtServiceException {
    GenesisInterfaceProto.SnmpTrap snmpTrapProto;
    boolean config_present_in_idf;
    InsightsInterfaceProto.BooleanExpression where_clause;

    if (isMulticluster(clusterExtId)) {
      log.debug("PC Cluster");
      config_present_in_idf = true;
      where_clause = clustermgmtResourceAdapter.constructNotExistsBooleanExpression(MASTER_CLUSTER_UUID_ATTRIBUTE);
    }
    else{
      log.debug("PE Cluster");
      isRequestAllowed(clusterExtId, ClustermgmtUtils.GET_SNMP_TRAP);
      // If Async update cluster is supported on the PE, then SNMP config will be present in IDF.
      config_present_in_idf = (getGenesisApiVersion(clusterExtId) >=
        GenesisInterfaceProto.GenesisApiVersion.kAsyncUpdateClusterSupportedVersion.getNumber());
      where_clause = clustermgmtResourceAdapter.adaptToBooleanExpression(
        MASTER_CLUSTER_UUID_ATTRIBUTE, InsightsInterfaceProto.ComparisonExpression.Operator.kEQ,
        InsightsInterfaceProto.DataValue.newBuilder().setStrValue(clusterExtId));
    }

    // SNMP trap config is written to IDF. We can directly read it from IDF.
    if (config_present_in_idf) {
      log.debug("Snmp Trap with uuid {} on cluster {} is present in IDF", trapExtId, clusterExtId);
      List<SnmpTrap> snmpTrapList = ClustermgmtUtils.getSnmpTrapEntities(
        trapExtId, where_clause, entityDbProxy, clustermgmtResourceAdapter, clusterResourceAdapter);
      if(snmpTrapList.isEmpty())
        throw new ClustermgmtServiceException("SNMP Trap with uuid: " + trapExtId + NOT_BELONG_TO_CLUSTER_STRING + clusterExtId);
      log.debug("Snmp Trap with uuid {} on cluster {}: {}", trapExtId, clusterExtId, snmpTrapList.get(0));
      return snmpTrapList.get(0);
    }

    // Snmp Trap Config is not present in IDF. We read it from Zeus config.
    ZeusConfiguration zkConfig = ClustermgmtUtils.getZkconfig(clusterExtId, multiClusterZeusConfig);

    List<SnmpTrap> snmpTraps = clusterResourceAdapter.getSnmpTraps(zkConfig);
    for (SnmpTrap snmpTrap: snmpTraps)
      if(snmpTrap.getExtId().equals(trapExtId))
        return snmpTrap;

    throw new ClustermgmtNotFoundException("SNMP Trap with uuid: " + trapExtId + NOT_BELONG_TO_CLUSTER_STRING + clusterExtId);
  }

  @Override
  public TaskReference addSnmpTrap(SnmpTrap body, String clusterExtId, String batchTask)
    throws ClustermgmtServiceException, ValidationException {
    TaskReference task = new TaskReference();
    boolean isPc = isMulticluster(clusterExtId);

    // PC-PE Capability check
    if (!isPc) isRequestAllowed(clusterExtId, ClustermgmtUtils.ADD_SNMP_TRAP);

    // Validate Snmp User provided
    validateSnmpTrap(body);

    // Cluster EntityId
    ErgonTypes.EntityId entityId = ClustermgmtUtils.getClusterEntityId(clusterExtId);

    // add snmp trap arg
    GenesisInterfaceProto.AddSnmpTrapArg.Builder builder = GenesisInterfaceProto.AddSnmpTrapArg.newBuilder();
    GenesisInterfaceProto.AddSnmpTrapArg addSnmpTrapArg = null;

    if(isPc) {
      // Populate add snmp trap arg
      addSnmpTrapArg = clusterResourceAdapter.adaptSnmpTrapToAddSnmpTrapArg(body, builder, null);

      // Invoke RPC
      GenesisInterfaceProto.AddSnmpTrapRet addSnmpTrapRet =
        genesisProxy.doExecute(GenesisProxyImpl.GenesisRpcName.ADD_SNMP_TRAP, addSnmpTrapArg, RequestContextHelper.getRpcRequestContext());
      // Update the task to be returned
      if (addSnmpTrapRet.hasTaskUuid()) {
        final String taskExtId = ErgonTaskUtils.getTaskReferenceExtId(UuidUtils.getUUID(addSnmpTrapRet.getTaskUuid()));
        task.setExtId(taskExtId);
      }
    }
    else {
      ByteString batchTaskBytes = null;
      if (batchTask != null) {
        batchTaskBytes = UuidUtils.getByteStringFromUUID(batchTask);
      }
      // Create PC task
      ErgonInterface.TaskCreateArg createArg = ClustermgmtUtils.createTaskArg(null, GENESIS_COMPONENT_NAME,
        "Add Snmp Trap", "Add Snmp Trap", batchTaskBytes,null, entityId,
        RequestContextHelper.getRpcRequestContext());
      ErgonInterface.TaskCreateRet taskCreateRet = ergonProxy.doExecute(ErgonProxyImpl.ErgonRpcName.TASK_CREATE, createArg);
      task.setExtId(taskCreateRet.getExtId());

      boolean is_async_add_snmp_trap_supported = (getGenesisApiVersion(clusterExtId) >=
        GenesisInterfaceProto.GenesisApiVersion.kAsyncUpdateClusterSupportedVersion.getNumber());

      if (is_async_add_snmp_trap_supported) {
        // Set Parent Task uuid in the AddSnmpTrapArg. Genesis on PE would create a child task
        addSnmpTrapArg = clusterResourceAdapter.adaptSnmpTrapToAddSnmpTrapArg(
          body, builder, taskCreateRet.getUuid());
        String rpcRetType = "com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto$AddSnmpTrapRet";
        publishRpcMessageToNats(clusterExtId, "AddSnmpTrap", addSnmpTrapArg,
                                rpcRetType, taskCreateRet.getUuid(), false);
      }
      else {
        // Make a synchronous call to Genesis on PE
        addSnmpTrapArg = clusterResourceAdapter.adaptSnmpTrapToAddSnmpTrapArg(body, builder, null);

        String rpcRetType = "com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto$AddSnmpTrapRet";
        publishRpcMessageToNats(clusterExtId, "AddSnmpTrap", addSnmpTrapArg,
                                rpcRetType, taskCreateRet.getUuid(), true);
      }
    }
    return task;
  }

  @Override
  public TaskReference updateSnmpTrap(SnmpTrap snmpTrap, String clusterExtId, String trapExtId, String batchTask)
    throws ClustermgmtServiceException, ValidationException {
    TaskReference task = new TaskReference();
    boolean isPc = isMulticluster(clusterExtId);

    // PC-PE Capability check
    if (!isPc) isRequestAllowed(clusterExtId, ClustermgmtUtils.UPDATE_SNMP_TRAP);

    // Validate Snmp Trap provided
    validateSnmpTrap(snmpTrap);

    // Set TrapExtId in Snmp Trap Payload
    snmpTrap.setExtId(trapExtId);

    // Cluster EntityId
    ErgonTypes.EntityId entityId = ClustermgmtUtils.getClusterEntityId(clusterExtId);

    // update snmp user arg
    GenesisInterfaceProto.UpdateSnmpTrapArg.Builder builder = GenesisInterfaceProto.UpdateSnmpTrapArg.newBuilder();
    GenesisInterfaceProto.UpdateSnmpTrapArg updateSnmpTrapArg = null;

    if(isPc) {
      // Populate snmp user arg
      updateSnmpTrapArg = clusterResourceAdapter.adaptSnmpTrapToUpdateSnmpTrapArg(snmpTrap, builder, null);

      // Invoke Genesis RPC which returns task to track UpdateSnmpTrap
      GenesisInterfaceProto.UpdateSnmpTrapRet updateSnmpTrapRet =
        genesisProxy.doExecute(GenesisProxyImpl.GenesisRpcName.UPDATE_SNMP_TRAP, updateSnmpTrapArg, RequestContextHelper.getRpcRequestContext());
      // Update the task to be returned
      if (updateSnmpTrapRet.hasTaskUuid()) {
        final String taskExtId = ErgonTaskUtils.getTaskReferenceExtId(UuidUtils.getUUID(
          updateSnmpTrapRet.getTaskUuid()));
        task.setExtId(taskExtId);
      }
    }
    else {
      ByteString batchTaskBytes = null;
      if (batchTask != null) {
        batchTaskBytes = UuidUtils.getByteStringFromUUID(batchTask);
      }      // Create PC task
      ErgonInterface.TaskCreateArg createArg = ClustermgmtUtils.createTaskArg(null, GENESIS_COMPONENT_NAME,
        "Update Snmp Trap", "Update snmp trap", batchTaskBytes, null, entityId,
        RequestContextHelper.getRpcRequestContext());
      ErgonInterface.TaskCreateRet taskCreateRet = ergonProxy.doExecute(ErgonProxyImpl.ErgonRpcName.TASK_CREATE, createArg);
      task.setExtId(taskCreateRet.getExtId());

      boolean is_async_update_snmp_trap_supported = (getGenesisApiVersion(clusterExtId) >=
        GenesisInterfaceProto.GenesisApiVersion.kAsyncUpdateClusterSupportedVersion.getNumber());

      if (is_async_update_snmp_trap_supported) {
        // Set Parent Task uuid in the UpdateSnmpTrapArg. Genesis on PE would create a child task
        updateSnmpTrapArg = clusterResourceAdapter.adaptSnmpTrapToUpdateSnmpTrapArg(
          snmpTrap, builder, taskCreateRet.getUuid());

        String rpcRetType = "com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto$UpdateSnmpTrapRet";
        publishRpcMessageToNats(clusterExtId, "UpdateSnmpTrap", updateSnmpTrapArg,
                                rpcRetType, taskCreateRet.getUuid(), false);
      }
      else {
        // Make a synchronous call to Genesis on PE
        updateSnmpTrapArg = clusterResourceAdapter.adaptSnmpTrapToUpdateSnmpTrapArg(snmpTrap, builder, null);

        String rpcRetType = "com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto$UpdateSnmpTrapRet";
        publishRpcMessageToNats(clusterExtId, "UpdateSnmpTrap", updateSnmpTrapArg,
          rpcRetType, taskCreateRet.getUuid(), true);
      }
    }

    return task;
  }

  @Override
  public TaskReference deleteSnmpTrap(String clusterExtId, String trapExtId, String batchTask)
    throws ClustermgmtServiceException, ValidationException {
    TaskReference task = new TaskReference();
    boolean isPc = isMulticluster(clusterExtId);

    // PC-PE Capability check
    if (!isPc) isRequestAllowed(clusterExtId, ClustermgmtUtils.DELETE_SNMP_TRAP);

    // Cluster EntityId
    ErgonTypes.EntityId entityId = ClustermgmtUtils.getClusterEntityId(clusterExtId);

    // delete snmp trap arg
    GenesisInterfaceProto.DeleteSnmpTrapArg.Builder builder = GenesisInterfaceProto.DeleteSnmpTrapArg.newBuilder();
    GenesisInterfaceProto.DeleteSnmpTrapArg deleteSnmpTrapArg = null;

    if(isPc) {
      // delete snmp trap arg
      deleteSnmpTrapArg = clusterResourceAdapter.adaptSnmpTrapToDeleteSnmpTrapArg(trapExtId, builder, null);

      // Invoke Genesis RPC which returns task to track DeleteSnmpTrap
      GenesisInterfaceProto.DeleteSnmpTrapRet deleteSnmpTrapRet =
        genesisProxy.doExecute(GenesisProxyImpl.GenesisRpcName.DELETE_SNMP_TRAP, deleteSnmpTrapArg, RequestContextHelper.getRpcRequestContext());

      // Update the task to be returned
      if (deleteSnmpTrapRet.hasTaskUuid()) {
        final String taskExtId = ErgonTaskUtils.getTaskReferenceExtId(UuidUtils.getUUID(
          deleteSnmpTrapRet.getTaskUuid()));
        task.setExtId(taskExtId);
      }
    }
    else {
      ByteString batchTaskBytes = null;
      if (batchTask != null) {
        batchTaskBytes = UuidUtils.getByteStringFromUUID(batchTask);
      }
      // Create PC task
      ErgonInterface.TaskCreateArg createArg = ClustermgmtUtils.createTaskArg(null, GENESIS_COMPONENT_NAME,
        "Delete Snmp Trap", "Delete snmp trap", batchTaskBytes,null, entityId,
        RequestContextHelper.getRpcRequestContext());
      ErgonInterface.TaskCreateRet taskCreateRet = ergonProxy.doExecute(ErgonProxyImpl.ErgonRpcName.TASK_CREATE, createArg);
      task.setExtId(taskCreateRet.getExtId());

      boolean is_async_delete_snmp_trap_supported = (getGenesisApiVersion(clusterExtId) >=
        GenesisInterfaceProto.GenesisApiVersion.kAsyncUpdateClusterSupportedVersion.getNumber());

      if (is_async_delete_snmp_trap_supported) {
        // Set Parent Task uuid in the DeleteSnmpTrapArg. Genesis on PE would create a child task
        deleteSnmpTrapArg = clusterResourceAdapter.adaptSnmpTrapToDeleteSnmpTrapArg(
          trapExtId, builder, taskCreateRet.getUuid());

        String rpcRetType = "com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto$DeleteSnmpTrapRet";
        publishRpcMessageToNats(clusterExtId, "DeleteSnmpTrap", deleteSnmpTrapArg,
                                rpcRetType, taskCreateRet.getUuid(), false);
      }
      else {
        // Make a synchronous call to Genesis on PE
        deleteSnmpTrapArg = clusterResourceAdapter.adaptSnmpTrapToDeleteSnmpTrapArg(trapExtId, builder, null);

        String rpcRetType = "com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto$DeleteSnmpTrapRet";
        publishRpcMessageToNats(clusterExtId, "DeleteSnmpTrap", deleteSnmpTrapArg,
                                rpcRetType, taskCreateRet.getUuid(), true);
      }
    }

    return task;
  }

  void updateTaskBasedOnStatus(ByteString taskUuid,
                               int percent,
                               ErgonTypes.Task.Status status,
                               String errorMessage) throws ClustermgmtServiceException {
    // Task Get
    ErgonInterface.TaskGetArg taskGetArg =
      ErgonInterface.TaskGetArg.newBuilder()
        .addTaskUuidList(taskUuid)
        .setIncludeSubtaskUuids(false).build();
    ErgonInterface.TaskGetRet taskGetRet = ergonProxy.doExecute(ErgonProxyImpl.ErgonRpcName.TASK_GET, taskGetArg);

    if (taskGetRet != null) {
      // Update Task
      ErgonInterface.TaskUpdateArg.Builder taskUpdateArg =
        ErgonInterface.TaskUpdateArg.newBuilder()
          .setUuid(taskUuid)
          .setStatus(status)
          .setLogicalTimestamp(taskGetRet.getTaskList(0).getLogicalTimestamp())
          .setPercentageComplete(percent);

      if(status != ErgonTypes.Task.Status.kSucceeded && errorMessage != null) {
        ErgonTypes.MetaResponse.Builder response = ErgonTypes.MetaResponse.newBuilder()
          .setErrorDetail(errorMessage);
        taskUpdateArg.setResponse(response.build());
      }

      ErgonInterface.TaskUpdateRet taskUpdateRet =
        ergonProxy.doExecute(ErgonProxyImpl.ErgonRpcName.TASK_UPDATE, taskUpdateArg.build());
    }
    else {
      log.error("Failed in getting task {}", taskUuid);
    }
  }

  @Override
  public RsyslogServer getRsyslogServer(String clusterExtId, String rsyslogServerExtId)
    throws ClustermgmtServiceException {
    GenesisInterfaceProto.RemoteSyslogConfiguration remoteSyslogConfigurationProto;
    boolean config_present_in_idf;
    InsightsInterfaceProto.BooleanExpression where_clause;

    if (isMulticluster(clusterExtId)) {
      log.debug("PC Cluster");
      config_present_in_idf = true;
      where_clause = clustermgmtResourceAdapter.constructNotExistsBooleanExpression(MASTER_CLUSTER_UUID_ATTRIBUTE);
    }
    else{
      log.debug("PE Cluster");
      isRequestAllowed(clusterExtId, ClustermgmtUtils.GET_RSYSLOG_SERVER);
      // If Async update cluster is supported on the PE, then Rsyslog config will be present in IDF.
      config_present_in_idf = (getGenesisApiVersion(clusterExtId) >=
        GenesisInterfaceProto.GenesisApiVersion.kAsyncUpdateClusterSupportedVersion.getNumber());
      where_clause = clustermgmtResourceAdapter.adaptToBooleanExpression(
        MASTER_CLUSTER_UUID_ATTRIBUTE, InsightsInterfaceProto.ComparisonExpression.Operator.kEQ,
        InsightsInterfaceProto.DataValue.newBuilder().setStrValue(clusterExtId));
    }

    // Rsyslog config is written to IDF. We can directly read it from IDF.
    if (config_present_in_idf) {
      log.debug("Rsyslog server with uuid {} on cluster {} is present in IDF", rsyslogServerExtId, clusterExtId);
      List<RsyslogServer> rsyslogServerList = ClustermgmtUtils.getRsyslogServerEntities(
        rsyslogServerExtId, where_clause, entityDbProxy, clustermgmtResourceAdapter, clusterResourceAdapter);
      if (rsyslogServerList.isEmpty())
        throw new ClustermgmtServiceException(
          "Rsyslog server with uuid: " + rsyslogServerExtId + NOT_BELONG_TO_CLUSTER_STRING + clusterExtId);
      log.debug("Rsyslog server with uuid {} on cluster {}: {}",
        rsyslogServerExtId, clusterExtId, rsyslogServerList.get(0));
      return rsyslogServerList.get(0);
    }

    // Rsyslog Config is not present in IDF. We read it from Zeus config.
    ZeusConfiguration zkConfig = ClustermgmtUtils.getZkconfig(clusterExtId, multiClusterZeusConfig);
    List<RsyslogServer> rsyslogServers = clusterResourceAdapter.adaptZeusEntriestoRsyslogServer(zkConfig);
    for (RsyslogServer rsyslog: rsyslogServers) {
      if(rsyslog.getExtId().equals(rsyslogServerExtId)) {
        return rsyslog;
      }
    }
    throw new ClustermgmtNotFoundException("Rsyslog server with uuid: " + rsyslogServerExtId + NOT_BELONG_TO_CLUSTER_STRING + clusterExtId);
  }

  public void validateRsyslogServer(RsyslogServer rsyslogServer) throws ClustermgmtServiceException{
    if (rsyslogServer.getModules() != null) {
      List<RsyslogModuleName> rsyslogModuleNames = new ArrayList<>();
      for (RsyslogModuleItem rsyslogModuleItem : rsyslogServer.getModules()) {
        RsyslogModuleName name = rsyslogModuleItem.getName();
        if (rsyslogModuleNames.contains(name)) {
          throw new ClustermgmtInvalidInputException("Duplicate Rsyslog modules with name: " + name.fromEnum()
            + " found in payload");
        }
        rsyslogModuleNames.add(name);
      }
    }
  }

  @Override
  public TaskReference addRsyslogServer(RsyslogServer rsyslogServer, String clusterExtId, String batchTask)
    throws ClustermgmtServiceException, ValidationException {
    TaskReference task = new TaskReference();
    boolean isPc = isMulticluster(clusterExtId);

    // PC-PE Capability check
    if (!isPc) isRequestAllowed(clusterExtId, ClustermgmtUtils.ADD_RSYSLOG_SERVER);

    // Validate Rsyslog Server Payload
    validateRsyslogServer(rsyslogServer);

    // Cluster EntityId
    ErgonTypes.EntityId entityId = ClustermgmtUtils.getClusterEntityId(clusterExtId);

    // add rsyslog server arg
    GenesisInterfaceProto.CreateRemoteSyslogServerArg.Builder builder = GenesisInterfaceProto.CreateRemoteSyslogServerArg.newBuilder();
    GenesisInterfaceProto.CreateRemoteSyslogServerArg createRemoteSyslogServerArg = null;

    if(isPc) {
      // Populate Add Rsyslog server Arg.
      createRemoteSyslogServerArg = clusterResourceAdapter.adaptRsyslogServerToCreateRsyslogServerArg(
        rsyslogServer, builder, null);

      // Invoke Genesis RPC which returns task to track Add Rsyslog server.
      GenesisInterfaceProto.CreateRemoteSyslogServerRet createRemoteSyslogServerRet =
        genesisProxy.doExecute(GenesisProxyImpl.GenesisRpcName.CREATE_RSYSLOG_SERVER, createRemoteSyslogServerArg, RequestContextHelper.getRpcRequestContext());

      // Update the task to be returned
      if (createRemoteSyslogServerRet.hasTaskUuid()) {
        final String taskExtId = ErgonTaskUtils.getTaskReferenceExtId(
          UuidUtils.getUUID(createRemoteSyslogServerRet.getTaskUuid()));
        task.setExtId(taskExtId);
      }
    }
    else {
      ByteString batchTaskBytes = null;
      if (batchTask != null) {
        batchTaskBytes = UuidUtils.getByteStringFromUUID(batchTask);
      }      // Create PC task
      ErgonInterface.TaskCreateArg createArg = ClustermgmtUtils.createTaskArg(null, GENESIS_COMPONENT_NAME,
        "Add Rsyslog Server", "Add Rsyslog Server", batchTaskBytes,null, entityId,
        RequestContextHelper.getRpcRequestContext());
      ErgonInterface.TaskCreateRet taskCreateRet = ergonProxy.doExecute(ErgonProxyImpl.ErgonRpcName.TASK_CREATE, createArg);
      task.setExtId(taskCreateRet.getExtId());

      boolean is_async_rsyslog_ops_supported = (getGenesisApiVersion(clusterExtId) >=
        GenesisInterfaceProto.GenesisApiVersion.kAsyncUpdateClusterSupportedVersion.getNumber());

      if (is_async_rsyslog_ops_supported) {
        // Set Parent Task uuid in the CreateRemoteSyslogServerArg. Genesis on PE would create a child task.
        createRemoteSyslogServerArg = clusterResourceAdapter.adaptRsyslogServerToCreateRsyslogServerArg(
          rsyslogServer, builder, taskCreateRet.getUuid());

        String rpcRetType = "com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto$CreateRemoteSyslogServerRet";
        publishRpcMessageToNats(clusterExtId,"CreateRemoteSyslogServer", createRemoteSyslogServerArg,
          rpcRetType, taskCreateRet.getUuid(), false);
      }
      else {
        // Make a synchronous call to Genesis on PE
        createRemoteSyslogServerArg = clusterResourceAdapter.adaptRsyslogServerToCreateRsyslogServerArg(
          rsyslogServer, builder, null);

        String rpcRetType = "com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto$CreateRemoteSyslogServerRet";
        publishRpcMessageToNats(clusterExtId, "CreateRemoteSyslogServer", createRemoteSyslogServerArg,
          rpcRetType, taskCreateRet.getUuid(), true);
      }
    }

    return task;
  }

  @Override
  public TaskReference updateRsyslogServer(RsyslogServer rsyslogServer, String clusterExtId, String rsyslogServerExtId, String batchTask)
    throws ClustermgmtServiceException, ValidationException {
    TaskReference task = new TaskReference();
    boolean isPc = isMulticluster(clusterExtId);

    // PC-PE Capability check
    if (!isPc) isRequestAllowed(clusterExtId, ClustermgmtUtils.UPDATE_RSYSLOG_SERVER);

    // Set ExtId in Rsyslog server payload
    rsyslogServer.setExtId(rsyslogServerExtId);

    // Cluster EntityId
    ErgonTypes.EntityId entityId = ClustermgmtUtils.getClusterEntityId(clusterExtId);

    // Define update rsyslog server arg
    GenesisInterfaceProto.UpdateRemoteSyslogServerArg.Builder builder = GenesisInterfaceProto.UpdateRemoteSyslogServerArg.newBuilder();
    GenesisInterfaceProto.UpdateRemoteSyslogServerArg updateRemoteSyslogServerArg = null;

    if(isPc) {
      // Populate update rsyslog server arg
      updateRemoteSyslogServerArg = clusterResourceAdapter.adaptRsyslogServerToUpdateRsyslogServerArg(
        rsyslogServer, builder, null);

      // Invoke Genesis RPC which returns task to track Add Rsyslog server.
      GenesisInterfaceProto.UpdateRemoteSyslogServerRet updateRemoteSyslogServerRet =
        genesisProxy.doExecute(GenesisProxyImpl.GenesisRpcName.UPDATE_RSYSLOG_SERVER, updateRemoteSyslogServerArg, RequestContextHelper.getRpcRequestContext());

      // Update the task to be returned
      if (updateRemoteSyslogServerRet.hasTaskUuid()) {
        final String taskExtId = ErgonTaskUtils.getTaskReferenceExtId(
          UuidUtils.getUUID(updateRemoteSyslogServerRet.getTaskUuid()));
        task.setExtId(taskExtId);
      }
    }
    else {
      ByteString batchTaskBytes = null;
      if (batchTask != null) {
        batchTaskBytes = UuidUtils.getByteStringFromUUID(batchTask);
      }      // Create PC task
      ErgonInterface.TaskCreateArg createArg = ClustermgmtUtils.createTaskArg(
        null, GENESIS_COMPONENT_NAME, "Update Rsyslog Server", "Update Rsyslog Server", batchTaskBytes,null, entityId,
        RequestContextHelper.getRpcRequestContext());
      ErgonInterface.TaskCreateRet taskCreateRet = ergonProxy.doExecute(ErgonProxyImpl.ErgonRpcName.TASK_CREATE, createArg);
      task.setExtId(taskCreateRet.getExtId());

      boolean is_async_rsyslog_ops_supported = (getGenesisApiVersion(clusterExtId) >=
        GenesisInterfaceProto.GenesisApiVersion.kAsyncUpdateClusterSupportedVersion.getNumber());

      if (is_async_rsyslog_ops_supported) {
        // Set Parent Task uuid in the UpdateRemoteSyslogServerArg. Genesis on PE would create a child task.
        updateRemoteSyslogServerArg = clusterResourceAdapter.adaptRsyslogServerToUpdateRsyslogServerArg(
          rsyslogServer, builder, taskCreateRet.getUuid());

        String rpcRetType = "com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto$UpdateRemoteSyslogServerRet";
        publishRpcMessageToNats(clusterExtId,"UpdateRemoteSyslogServer", updateRemoteSyslogServerArg,
          rpcRetType, taskCreateRet.getUuid(), false);
      }
      else {
        // Make a synchronous call to Genesis on PE
        updateRemoteSyslogServerArg = clusterResourceAdapter.adaptRsyslogServerToUpdateRsyslogServerArg(
          rsyslogServer, builder, null);

        String rpcRetType = "com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto$UpdateRemoteSyslogServerRet";
        publishRpcMessageToNats(clusterExtId, "updateRemoteSyslogServer", updateRemoteSyslogServerArg,
          rpcRetType, taskCreateRet.getUuid(), true);
      }
    }

    return task;
  }

  @Override
  public TaskReference deleteRsyslogServer(String rsyslogServerExtId, String clusterExtId, String batchTask)
    throws ClustermgmtServiceException, ValidationException {
    TaskReference task = new TaskReference();
    boolean isPc = isMulticluster(clusterExtId);

    // PC-PE Capability check
    if (!isPc) isRequestAllowed(clusterExtId, ClustermgmtUtils.DELETE_RSYSLOG_SERVER);

    // Cluster EntityId
    ErgonTypes.EntityId entityId = ClustermgmtUtils.getClusterEntityId(clusterExtId);

    // delete rsyslog server arg
    GenesisInterfaceProto.DeleteRemoteSyslogServerArg.Builder builder = GenesisInterfaceProto.DeleteRemoteSyslogServerArg.newBuilder();
    GenesisInterfaceProto.DeleteRemoteSyslogServerArg deleteRemoteSyslogServerArg = null;

    if (isPc) {
      // Populate update rsyslog server arg
      deleteRemoteSyslogServerArg = clusterResourceAdapter.adaptRsyslogServerNameToDeleteRsyslogServerArg(
        rsyslogServerExtId, builder, null);

      // Invoke Genesis RPC which returns task to track Add Rsyslog server.
      GenesisInterfaceProto.DeleteRemoteSyslogServerRet deleteRemoteSyslogServerRet =
        genesisProxy.doExecute(GenesisProxyImpl.GenesisRpcName.DELETE_RSYSLOG_SERVER, deleteRemoteSyslogServerArg, RequestContextHelper.getRpcRequestContext());

      // Update the task to be returned
      if (deleteRemoteSyslogServerRet.hasTaskUuid()) {
        final String taskExtId = ErgonTaskUtils.getTaskReferenceExtId(
          UuidUtils.getUUID(deleteRemoteSyslogServerRet.getTaskUuid()));
        task.setExtId(taskExtId);
      }
    }
    else {
      ByteString batchTaskBytes = null;
      if (batchTask != null) {
        batchTaskBytes = UuidUtils.getByteStringFromUUID(batchTask);
      }
      // Create PC task
      ErgonInterface.TaskCreateArg createArg = ClustermgmtUtils.createTaskArg(
        null, GENESIS_COMPONENT_NAME, "Delete Rsyslog Server", "Delete Rsyslog Server", batchTaskBytes, null, entityId,
        RequestContextHelper.getRpcRequestContext());
      ErgonInterface.TaskCreateRet taskCreateRet = ergonProxy.doExecute(ErgonProxyImpl.ErgonRpcName.TASK_CREATE, createArg);
      task.setExtId(taskCreateRet.getExtId());

      boolean is_async_rsyslog_ops_supported = (getGenesisApiVersion(clusterExtId) >=
        GenesisInterfaceProto.GenesisApiVersion.kAsyncUpdateClusterSupportedVersion.getNumber());

      if (is_async_rsyslog_ops_supported) {
        // Set Parent Task uuid in the DeleteRemoteSyslogServerArg. Genesis on PE would create a child task.
        deleteRemoteSyslogServerArg = clusterResourceAdapter.adaptRsyslogServerNameToDeleteRsyslogServerArg(
          rsyslogServerExtId, builder, taskCreateRet.getUuid());

        String rpcRetType = "com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto$DeleteRemoteSyslogServerRet";
        publishRpcMessageToNats(clusterExtId,"DeleteRemoteSyslogServer", deleteRemoteSyslogServerArg,
          rpcRetType, taskCreateRet.getUuid(), false);
      }
      else {
        // Make a synchronous call to Genesis on PE
        deleteRemoteSyslogServerArg = clusterResourceAdapter.adaptRsyslogServerNameToDeleteRsyslogServerArg(
          rsyslogServerExtId, builder, null);

        String rpcRetType = "com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto$DeleteRemoteSyslogServerRet";
        publishRpcMessageToNats(clusterExtId, "deleteRemoteSyslogServer", deleteRemoteSyslogServerArg,
          rpcRetType, taskCreateRet.getUuid(), true);
      }
    }
    return task;
  }

  @Override
  public TaskReference discoverUnconfiguredNodes(String clusterExtId, NodeDiscoveryParams discoverUnconfiguredNode)
    throws ClustermgmtServiceException, ValidationException {
    TaskReference task = new TaskReference();

    boolean isPc = isMulticluster(clusterExtId);
    if(isPc) {
      if(discoverUnconfiguredNode.getIpFilterList() == null){
        String err = "SVM ips of unconfigured nodes to be discovered not provided.";
        throw new ClustermgmtInvalidInputException(err);
      }

      ClusterManagementInterfaceProto.DiscoverHostsArg.Builder discoverHostsArgBuilder = ClusterManagementInterfaceProto.DiscoverHostsArg.newBuilder();
      List<String> cvm_ips;
      if (discoverUnconfiguredNode.getAddressType() == AddressType.IPV6) {
        cvm_ips = ClustermgmtUtils.getIpv6AddressList(discoverUnconfiguredNode.getIpFilterList());
      }
      else {
        cvm_ips = ClustermgmtUtils.getIpv4AddressList(discoverUnconfiguredNode.getIpFilterList());
      }
      discoverHostsArgBuilder.addAllSvmIps(cvm_ips);
      ClusterManagementInterfaceProto.DiscoverHostsArg discoverHostsArg = discoverHostsArgBuilder.build();
      log.debug("Args to send to Discover RPC: {}\n", discoverHostsArg);

      ClusterManagementInterfaceProto.DiscoverHostsRet discoverHostsRet=
        cmsProxy.doExecute(CMSProxyImpl.CMSRpcName.DISCOVER_HOSTS, discoverHostsArg, RequestContextHelper.getRpcRequestContext());
      if(discoverHostsRet.hasTaskUuid()){
        final String taskExtId = ErgonTaskUtils.getTaskReferenceExtId(UuidUtils.getUUID(discoverHostsRet.getTaskUuid()));
        task.setExtId(taskExtId);
      }
    }
    else {

      // PC-PE Capability check
      isRequestAllowed(clusterExtId, ClustermgmtUtils.DISCOVER_UNCONFIGURED_NODES);

      // Cluster EntityId
      ErgonTypes.EntityId entityId = ClustermgmtUtils.getClusterEntityId(clusterExtId);

      // Create PC task
      ErgonInterface.TaskCreateArg createArg = ClustermgmtUtils.createTaskArg(null, GENESIS_COMPONENT_NAME,
        "Discover Unconfigured Nodes", "Discover Unconfigured Nodes", null, null, entityId,
        RequestContextHelper.getRpcRequestContext());
      ErgonInterface.TaskCreateRet taskCreateRet = ergonProxy.doExecute(ErgonProxyImpl.ErgonRpcName.TASK_CREATE, createArg);
      task.setExtId(taskCreateRet.getExtId());

      String taskUuid = UuidUtils.getUUID(taskCreateRet.getUuid());
      log.info("UUID string {}", UuidUtils.getUUID(taskCreateRet.getUuid()));

      // update discover unconfigured arg
      final String payload =
        clusterResourceAdapter.adaptDiscoverUnconfigureNodeToJsonRpcFormat(
          discoverUnconfiguredNode, taskUuid);

      // NATS Call
      publishASyncJsonRpcMessageToNats(clusterExtId, taskUuid, payload);
    }

    return task;
  }

  @Override
  public TaskReference getNodeNetworkingDetails(String clusterExtId, NodeDetails nodesNetworkingDetails)
    throws ClustermgmtServiceException, ValidationException {
    TaskReference task = new TaskReference();

    boolean isPc = isMulticluster(clusterExtId);
    if(isPc) {
      throw new ClustermgmtServiceException("Get Node Networking Details Api is not supported on PC: " + clusterExtId);
    }

    // PC-PE Capability check
    isRequestAllowed(clusterExtId, ClustermgmtUtils.GET_NETWORKING_DETAILS);

    // Cluster EntityId
    ErgonTypes.EntityId entityId = ClustermgmtUtils.getClusterEntityId(clusterExtId);

    // Create PC task
    ErgonInterface.TaskCreateArg createArg = ClustermgmtUtils.createTaskArg(null, GENESIS_COMPONENT_NAME,
      "Fetching cluster networks and uplinks", "Fetching cluster networks and uplinks", null,null, entityId,
      RequestContextHelper.getRpcRequestContext());
    ErgonInterface.TaskCreateRet taskCreateRet = ergonProxy.doExecute(ErgonProxyImpl.ErgonRpcName.TASK_CREATE, createArg);
    task.setExtId(taskCreateRet.getExtId());

    String taskUuid = UuidUtils.getUUID(taskCreateRet.getUuid());

    // update discover unconfigured arg
    final String payload =
      clusterResourceAdapter.adaptNodeNetworkingDetailsToJsonRpcFormat(
        nodesNetworkingDetails, taskUuid);

    // NATS Call
    publishASyncJsonRpcMessageToNats(clusterExtId, taskUuid, payload);

    return task;
  }

  public Pair<Integer, List<Host>> getHostEntitiesFromIdf(final Integer offset,
                                                                final Integer limit,
                                                                EntityDbProxyImpl entityDbProxy,
                                                                ClustermgmtResourceAdapterImpl clustermgmtResourceAdapter,
                                                                InsightsInterfaceProto.BooleanExpression expression,
                                                                InsightsInterfaceProto.QueryOrderBy orderBy,
                                                                InsightsInterfaceProto.QueryGroupBy groupBy,
                                                                String select) throws ClustermgmtServiceException {
    // Get Entities
    List<Host> hostEntities = new ArrayList<>();
    Integer totalEntityCount = null;
    int entityCount = 0;
    List<String> attributeList = ClustermgmtUtils.hostAttributeList;
    if(select != null && groupBy != null){
      attributeList = ClustermgmtUtils.getAttributeListFromSelection(groupBy);
    }

    InsightsInterfaceProto.GetEntitiesWithMetricsRet getEntitiesWithMetricsRet =
      ClustermgmtUtils.getEntitiesWithMetricsRet(offset, limit,
        NODE_ENTITY, entityDbProxy, attributeList, null, clustermgmtResourceAdapter, expression, orderBy, groupBy);
    log.debug("The RPC response GetEntitiesWithMetricsRet obtained {}", getEntitiesWithMetricsRet);

    if (!CollectionUtils.isEmpty(getEntitiesWithMetricsRet.getGroupResultsListList())) {
      for (final InsightsInterfaceProto.QueryGroupResult queryGroupResult : getEntitiesWithMetricsRet.getGroupResultsListList()) {
        entityCount += (int) queryGroupResult.getTotalEntityCount();
        log.debug("The RPC response count {}", totalEntityCount);

        final List<InsightsInterfaceProto.EntityWithMetric> entitiesWithMetric =
          queryGroupResult.getRawResultsList();
        for (final InsightsInterfaceProto.EntityWithMetric entityWithMetric : entitiesWithMetric) {
          final List<InsightsInterfaceProto.MetricData> metricDataList =
            entityWithMetric.getMetricDataListList();
          String hostUuid = entityWithMetric.getEntityGuid().getEntityId();

          Host hostEntity = new Host();
          hostEntity.setExtId(hostUuid);
          hostEntity = clusterResourceAdapter.adaptIdfHostMetricstoHostEntity(metricDataList, hostEntity);
          if (select == null) {
            if (hostEntity.getCluster() != null) {
              ZeusConfiguration zkConfig =
                ClustermgmtUtils.getZkconfig(hostEntity.getCluster().getUuid(), multiClusterZeusConfig);
              hostEntity = clusterResourceAdapter.adaptZeusEntriestoHostEntity(zkConfig, hostEntity, hostUuid);
            } else {
              throw new ClustermgmtNotFoundException("Failed to find cluster reference for host : " + hostUuid);
            }
          }
          // Append entity
          hostEntities.add(hostEntity);
        }
      }
    }
    if (entityCount != 0)
      totalEntityCount = entityCount;

    return new Pair<>(totalEntityCount,hostEntities);
  }

  @Override
  public Pair<Integer, List<Host>> getAllHostEntities(Integer offset,
                                                            Integer limit,
                                                            String filter,
                                                            String orderBy,
                                                            String apply,
                                                            String select)
    throws ClustermgmtServiceException, ValidationException {

    if(orderBy == null){
      orderBy = DEFAULT_ORDERBY_ATTR;
    }

    // Boolean expression from Odata filer
    final ClustermgmtOdataParser.Result odataResult =
      clustermgmtOdataParser.parse("/hosts", filter, orderBy, apply, select, null);

    List<InsightsInterfaceProto.QueryOrderBy> queryOrderBy = odataResult.getOrderBy();
    InsightsInterfaceProto.BooleanExpression filterExpression = odataResult.getFilter();
    InsightsInterfaceProto.QueryOrderBy sortQueryOrderBy = null;
    if(queryOrderBy != null && !CollectionUtils.isEmpty(queryOrderBy)) {
      sortQueryOrderBy = queryOrderBy.get(0);
    }

    // Get Entities
    return getHostEntitiesFromIdf(offset, limit, entityDbProxy, clustermgmtResourceAdapter, filterExpression, sortQueryOrderBy, odataResult.getGroupBy(), select);
  }

  @Override
  public Pair<Integer, List<HostGpu> > getAllHostGpus(Integer limit,
                                                            Integer page,
                                                            String filter,
                                                            String orderBy,
                                                            String selectString)
    throws ClustermgmtServiceException, ValidationException {
    return getGpuEntitiesFromIDF(limit, page, filter, orderBy, null, null, selectString);
  }

  @Override
  public void addNode(String clusterExtId, ExpandClusterParams addNode, String taskUuid)
    throws ClustermgmtServiceException, ValidationException {

    if(isMulticluster(clusterExtId)) {
      throw new ClustermgmtServiceException("Add node API is not supported on PC: " + clusterExtId);
    }

    // PC-PE Capability check
    isRequestAllowed(clusterExtId, ClustermgmtUtils.ADD_NODE);

    // Cluster EntityId
    ErgonTypes.EntityId entityId = ClustermgmtUtils.getClusterEntityId(clusterExtId);

    // Create PC task
    ErgonInterface.TaskCreateArg createArg = ClustermgmtUtils.createTaskArg(UuidUtils.getByteStringFromUUID(taskUuid),
      GENESIS_COMPONENT_NAME, "Expand Cluster", "Expand Cluster", null,null,
      entityId, RequestContextHelper.getRpcRequestContext());
    ErgonInterface.TaskCreateRet taskCreateRet = ergonProxy.doExecute(ErgonProxyImpl.ErgonRpcName.TASK_CREATE, createArg);

    // update discover unconfigured arg
    final String payload =
      clusterResourceAdapter.adaptAddNodeToJsonRpcFormat(
        addNode, taskUuid);

    // NATS Call
    publishASyncJsonRpcMessageToNats(clusterExtId, taskUuid, payload);
  }

  @Override
  public TaskReference isHypervisorUploadRequired(String clusterExtId, HypervisorUploadParam hypervisorUploadParam)
    throws ClustermgmtServiceException, ValidationException {
    TaskReference task = new TaskReference();

    boolean isPc = isMulticluster(clusterExtId);
    if(isPc) {
      throw new ClustermgmtServiceException("Hypervisor Upload required API is not supported on PC: " + clusterExtId);
    }

    // PC-PE Capability check
    isRequestAllowed(clusterExtId, ClustermgmtUtils.IS_HYPERVISOR_UPLOAD_REQ);

    // Cluster EntityId
    ErgonTypes.EntityId entityId = ClustermgmtUtils.getClusterEntityId(clusterExtId);

    // Create PC task
    ErgonInterface.TaskCreateArg createArg = ClustermgmtUtils.createTaskArg(null, GENESIS_COMPONENT_NAME,
      "Hypervisor Image Upload Check", "Hypervisor Image Upload Check", null,null, entityId,
      RequestContextHelper.getRpcRequestContext());
    ErgonInterface.TaskCreateRet taskCreateRet = ergonProxy.doExecute(ErgonProxyImpl.ErgonRpcName.TASK_CREATE, createArg);
    task.setExtId(taskCreateRet.getExtId());

    String taskUuid = UuidUtils.getUUID(taskCreateRet.getUuid());

    // update discover unconfigured arg
    final String payload =
      clusterResourceAdapter.adaptHypervisorUploadParamToJsonRpcFormat(
        hypervisorUploadParam, taskUuid);

    // NATS Call
    publishASyncJsonRpcMessageToNats(clusterExtId, taskUuid, payload);

    return task;
  }

  @Override
  public TaskReference validateNode(String clusterExtId, ValidateNodeParam body) throws ClustermgmtServiceException {
    TaskReference task = new TaskReference();

    boolean isPc = isMulticluster(clusterExtId);
    if(isPc) {
      throw new ClustermgmtServiceException("Verify Hypervisor Bundle API is not supported on PC: " + clusterExtId);
    }

    // PC-PE Capability check
    isRequestAllowed(clusterExtId, ClustermgmtUtils.VALIDATE_NODE);

    // Cluster EntityId
    ErgonTypes.EntityId entityId = ClustermgmtUtils.getClusterEntityId(clusterExtId);

    final Object spec = body.getSpec();
    if (spec instanceof BundleParam) {
      // Create PC task
      ErgonInterface.TaskCreateArg createArg = ClustermgmtUtils.createTaskArg(null, GENESIS_COMPONENT_NAME,
        "Verify Bundle", "Verify Bundle", null,null, entityId,
        RequestContextHelper.getRpcRequestContext());
      ErgonInterface.TaskCreateRet taskCreateRet = ergonProxy.doExecute(ErgonProxyImpl.ErgonRpcName.TASK_CREATE, createArg);
      task.setExtId(taskCreateRet.getExtId());

      String taskUuid = UuidUtils.getUUID(taskCreateRet.getUuid());

      // update discover unconfigured arg
      final String payload =
        clusterResourceAdapter.adaptIsBundleCompatibleToJsonRpcFormat(
          (BundleParam)spec, taskUuid);
      // NATS Call
      publishASyncJsonRpcMessageToNats(clusterExtId, taskUuid, payload);
    }
    else if(spec instanceof List && ((List)spec).size()>0 && (((List)spec).get(0) instanceof UplinkNode)) {
      // Create PC task
      ErgonInterface.TaskCreateArg createArg = ClustermgmtUtils.createTaskArg(null, GENESIS_COMPONENT_NAME,
        "Validate Uplinks Info", "Validate Uplinks Info", null,null, entityId,
        RequestContextHelper.getRpcRequestContext());
      ErgonInterface.TaskCreateRet taskCreateRet = ergonProxy.doExecute(ErgonProxyImpl.ErgonRpcName.TASK_CREATE, createArg);
      task.setExtId(taskCreateRet.getExtId());

      String taskUuid = UuidUtils.getUUID(taskCreateRet.getUuid());

      // update discover unconfigured arg
      final String payload =
        clusterResourceAdapter.adaptValidateUplinkToJsonRpcFormat(
          (List<UplinkNode>)spec, taskUuid);
      // NATS Call
      publishASyncJsonRpcMessageToNats(clusterExtId, taskUuid, payload);
    }

    return task;
  }

  @Override
  public void removeNode(String clusterExtId, NodeRemovalParams removeNode, String taskUuid)
    throws ClustermgmtServiceException, ValidationException {

    boolean isPc = isMulticluster(clusterExtId);
    if(isPc) {
      throw new ClustermgmtServiceException("Remove node API is not supported on PC: " + clusterExtId);
    }

    // PC-PE Capability check
    isRequestAllowed(clusterExtId, ClustermgmtUtils.REMOVE_NODE);

    // Cluster EntityId
    ErgonTypes.EntityId entityId = ClustermgmtUtils.getClusterEntityId(clusterExtId);

    // Create PC task
    ErgonInterface.TaskCreateArg createArg = ClustermgmtUtils.createTaskArg(UuidUtils.getByteStringFromUUID(taskUuid)
      , GENESIS_COMPONENT_NAME, "Remove Node", "Remove Node", null,null,
      entityId, RequestContextHelper.getRpcRequestContext());
    ErgonInterface.TaskCreateRet taskCreateRet = ergonProxy.doExecute(ErgonProxyImpl.ErgonRpcName.TASK_CREATE, createArg);

    // update discover unconfigured arg
    final String payload =
      clusterResourceAdapter.adaptRemoveNodeToJsonRpcFormat(
        removeNode, taskUuid);

    // NATS Call
    publishASyncJsonRpcMessageToNats(clusterExtId, taskUuid, payload);
  }

  private Integer getGenesisApiVersion(final String... clusterRefs)
    throws ClustermgmtServiceException {
    int version = 0;
    String clusterId = "";
    if (clusterRefs.length > 0) {
      clusterId = clusterRefs[0];
    }

    InsightsInterfaceProto.GetEntitiesRet getEntitiesRet =
      ClustermgmtUtils.getInsightsEntityRet(clusterId, ClustermgmtUtils.INSIGHTS_CAPABILITIES,
        entityDbProxy, clustermgmtResourceAdapter);

    if (getEntitiesRet.getEntityCount() != 0) {
      InsightsInterfaceProto.Entity entity = getEntitiesRet.getEntity(0);
      version = readGenesisApiVersion(entity);
    }
    return version;
  }

  int readGenesisApiVersion(final InsightsInterfaceProto.Entity entity) {
    int genesisApiVersion = 0;
    String capsJsonString =
      entity.getAttributeDataMapList()
        .stream()
        .filter(nameTimeValuePair ->
          nameTimeValuePair.getName().equals(ClustermgmtUtils.CAPS_JSON_ATTRIBUTE))
        .findFirst()
        .map(nameTimeValuePair -> nameTimeValuePair.getValue().getStrValue())
        .orElse("");

    try {
      JsonNode capsJson = mapper.readTree(capsJsonString);
      JsonNode genesisCapabilities =
        capsJson.get("cluster_capabilities").get("genesis_capabilities");
      if ((genesisCapabilities != null) &&
        (genesisCapabilities.get("genesis_version") != null)) {
        genesisApiVersion = genesisCapabilities.get("genesis_version").asInt();
      }
      else {
        log.info("Genesis API Capability version not found");
      }
    }
    catch (Exception e) {
      log.error("Encountered error while deserializing capabilities " +
        "JSON response string {}", capsJsonString);
    }
    return genesisApiVersion;
  }

  void isRequestAllowed(String clusterExtId, String method) throws ClustermgmtServiceException {
    // PC-PE Capability check
    String fullVersion = RequestContextHelper.getVersionHeader();
    Integer version = getGenesisApiVersion(clusterExtId);

    if (ClustermgmtUtils.capabilityMap != null && ClustermgmtUtils.capabilityMap.containsKey(fullVersion)) {
      Map<String, Integer> versionMap = ClustermgmtUtils.capabilityMap.get(fullVersion);
      if(versionMap != null && versionMap.containsKey(method)){
        Integer requiredVersion = versionMap.get(method);
        String debugLogString = String.format("Required version for method %s is %d", method, requiredVersion);
        log.debug(debugLogString);
        if (requiredVersion != null && version < requiredVersion) {
          throw new ClustermgmtNotSupportedException("Upgrade to the compatible release is not done for cluster: " + clusterExtId);
        }
      }
    } else {
      throw new ClustermgmtNotSupportedException("Capability Map doesn't contain the mapping for version: " + fullVersion
        + " for cluster: " + clusterExtId);
    }
    log.debug("The request is allowed for method {}", method);

  }

  ErgonInterface.TaskGetRet getTask(String taskExtId) throws ClustermgmtServiceException{
    // Task Get
    ErgonInterface.TaskGetArg.Builder taskGetArg =
      ErgonInterface.TaskGetArg.newBuilder()
        .addTaskUuidList(UuidUtils.getByteStringFromUUID(taskExtId))
        .setIncludeSubtaskUuids(true);

    return ergonProxy.doExecute(ErgonProxyImpl.ErgonRpcName.TASK_GET, taskGetArg.build());
  }

  public JsonNode getJsonObjFromTaskResponse(ErgonTypes.Task task) throws
    ClustermgmtServiceException {
    ByteString response = task.getResponse().getRet().getEmbedded();
    JsonNode jsonNode = null;
    try {
      jsonNode = mapper.readTree(response.toStringUtf8());
    }
    catch (Exception e) {
      throw new ClustermgmtServiceException("Error in parsing json output from task " + e.getMessage());
    }
    return jsonNode;
  }

  @Override
  public TaskResponse getTaskResponse(String taskExtId, TaskResponseType taskResponseType) throws ClustermgmtServiceException {
    TaskResponse taskResponse = new TaskResponse();
    taskResponse.setExtId(taskExtId);
    taskResponse.setTaskResponseType(taskResponseType);
    String md5sum = null;

    ErgonInterface.TaskGetRet parentTask = getTask(taskExtId);
    if (parentTask.getTaskListCount() <= 0) {
      throw new ClustermgmtNotFoundException("Invalid Task UUID passed, task uuid: " + taskExtId);
    }

    if(parentTask.getTaskList(0).getStatus() == ErgonTypes.Task.Status.kQueued ||
      parentTask.getTaskList(0).getStatus() == ErgonTypes.Task.Status.kRunning) {
      throw new ClustermgmtServiceException("Task provided: {} is not marked as completed." + taskExtId);
    }

    ErgonTypes.Task childTask;
    if(parentTask.getTaskList(0).getSubtaskUuidListCount() <= 0) {
      log.debug("PC workflow usecase");
      childTask = parentTask.getTaskList(0);
    } else {
      ByteString childTaskUuid = parentTask.getTaskList(0).getSubtaskUuidList(0);
      ErgonInterface.TaskGetRet childTaskObj = getTask(UuidUtils.getUUID(childTaskUuid));
      childTask = childTaskObj.getTaskList(0);
    }

    if (childTask.getCompletionDetailsCount() <= 0) {
      log.warn("Completion details not exist in child task {}", childTask.getExtId());
    } else {
      String key = childTask.getCompletionDetails(0).getKey();
      if(key.equals("search_type")) {
        String requestType = childTask.getCompletionDetails(0).getStrData();
        if (!requestType.equals(taskResponseType.fromEnum())) {
          throw new ClustermgmtServiceException("Search type " + taskResponseType.fromEnum() +
            " doesn't match with task uuid provided " + taskExtId);
        }
      }
      else if(key.equals("hypervisor_md5sum")) {
        md5sum = childTask.getCompletionDetails(0).getStrData();
      }
    }

    switch (taskResponseType) {
      case UNCONFIGURED_NODES:
        UnconfigureNodeDetails unconfigureNodesResponse =
          clusterResourceAdapter.adaptJsonResponsetoUnconfiguredNodes(getJsonObjFromTaskResponse(childTask),
            new UnconfigureNodeDetails());
        taskResponse.setResponseInWrapper(unconfigureNodesResponse);
        break;
      case NETWORKING_DETAILS:
        NodeNetworkingDetails networkingDetailResponsee =
          clusterResourceAdapter.adaptJsonResponsetoNetworkingDetails(getJsonObjFromTaskResponse(childTask),
            new NodeNetworkingDetails());
        taskResponse.setResponseInWrapper(networkingDetailResponsee);
        break;
      case HYPERVISOR_UPLOAD_INFO:
        HypervisorUploadInfo hypervisorUploadInfoResponse =
          clusterResourceAdapter.adaptJsonResponsetoHypervisorIso(getJsonObjFromTaskResponse(childTask),
            new HypervisorUploadInfo());
        taskResponse.setResponseInWrapper(hypervisorUploadInfoResponse);
        break;
      case VALIDATE_BUNDLE_INFO:
        ValidateBundleInfo validateBundleInfo = new ValidateBundleInfo();
        if(md5sum != null)
          validateBundleInfo.setMd5Sum(md5sum);
        taskResponse.setResponseInWrapper(validateBundleInfo);
        break;
      case NON_COMPATIBLE_CLUSTERS:
        if (childTask.getCompletionDetailsCount() <= 0) {
          throw new ClustermgmtServiceException("Completion Details not exist in task: " + taskExtId);
        }
        List<NonCompatibleClusterReference> nonCompliantClustersReference = new ArrayList<>();
        clusterResourceAdapter.adaptJsonResponseToNonCompatibleClusters(getJsonObjFromTaskResponse(childTask),
          nonCompliantClustersReference);
        taskResponse.setResponseInWrapper(nonCompliantClustersReference);
        break;
      default:
        break;
    }
    return taskResponse;
  }
  public List<NetworkSwitchInterface> getSwitchInterfaceEntities(String hostUuid)
          throws ClustermgmtServiceException {
    // get the Network Switch Interface mappings from IDF

    InsightsInterfaceProto.BooleanExpression expression = null;
    if (hostUuid != null) {
    // Boolean expression to get Network Switch Interface Entities with NodeUuid equals Names.Attributes.NET_INTF_NODE_UUID
    InsightsInterfaceProto.DataValue.Builder value =
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue(hostUuid);// get value of node_uuid
    expression = clustermgmtResourceAdapter.adaptToBooleanExpression(NET_INTF_NODE_UUID,
                    InsightsInterfaceProto.ComparisonExpression.Operator.kEQ,
                    value);
    }
    InsightsInterfaceProto.GetEntitiesWithMetricsRet getEntitiesWithMetricsRet =
            ClustermgmtUtils.getEntitiesWithMetricsRet(null, null , NETWORK_SWITCH_INTERFACE_ENTITY, entityDbProxy,
              ClustermgmtUtils.networkSwitchIntfAttributeList, null,clustermgmtResourceAdapter,expression ,null, null);
    log.debug("The RPC response GetEntitiesWithMetricsRet obtained {}", getEntitiesWithMetricsRet);
    List<NetworkSwitchInterface> switchIntfEntities = new ArrayList<>();
    if (!CollectionUtils.isEmpty(getEntitiesWithMetricsRet.getGroupResultsListList())) {
      final List<InsightsInterfaceProto.EntityWithMetric> entitiesWithMetric =
              getEntitiesWithMetricsRet.getGroupResultsList(0).getRawResultsList();
      for (final InsightsInterfaceProto.EntityWithMetric entityWithMetric : entitiesWithMetric) {
        final List<InsightsInterfaceProto.MetricData> metricDataList =
          entityWithMetric.getMetricDataListList();
        NetworkSwitchInterface switchIntfEntity = new NetworkSwitchInterface();
        String switchInterfaceUuid = entityWithMetric.getEntityGuid().getEntityId();
        switchIntfEntity.setExtId(switchInterfaceUuid);
        switchIntfEntity = clusterResourceAdapter.adaptIdfSwitchIntfMetricstoNetworkSwitchInterfaceEntity(metricDataList, switchIntfEntity);
        switchIntfEntities.add(switchIntfEntity);
      }
    }
    return switchIntfEntities;
  }

  public List<NetworkSwitchInterface> getAttachedSwitchInterfaceEntitiesForHostNic(String hostNicUuid,
                                                                                         List<NetworkSwitchInterface> switchIntfEntities){
    List<NetworkSwitchInterface> attachedSwitchIntfList = new ArrayList<>();

    if (!switchIntfEntities.isEmpty()){
      for (NetworkSwitchInterface switchIntfEntity: switchIntfEntities){
        final List<String> hostNicUuids = switchIntfEntity.getAttachedHostNicUuids();
        if (!hostNicUuids.isEmpty() && hostNicUuids.contains(hostNicUuid)) {
          attachedSwitchIntfList.add(switchIntfEntity);
        }
      }
    }

    return attachedSwitchIntfList;
  }

  public Pair<Integer, List<HostNic> > getHostNicEntitiesFromIDF(Integer limit, Integer page, String filter,
                                                                   String orderBy,String select, String uuid, String uuidType)
          throws ClustermgmtServiceException, ValidationException {
    Integer totalEntityCount = null;
    List<HostNic> hostNicEntities = new ArrayList<>();
    List<String> attributeList = ClustermgmtUtils.hostNicAttributeList;
    // Boolean expression from Odata filer
    final ClustermgmtOdataParser.Result odataResult =
            clustermgmtOdataParser.parse("/host-nics", filter, orderBy, null, select, null);

    List<InsightsInterfaceProto.QueryOrderBy> queryOrderBy = odataResult.getOrderBy();
    InsightsInterfaceProto.BooleanExpression filterExpression = odataResult.getFilter();
    InsightsInterfaceProto.QueryGroupBy queryGroupBy = odataResult.getGroupBy();
    InsightsInterfaceProto.QueryOrderBy sortQueryOrderBy = null;
    if(queryOrderBy != null && !CollectionUtils.isEmpty(queryOrderBy)) {
      sortQueryOrderBy = queryOrderBy.get(0);
    }
    if(select != null && queryGroupBy != null){
      attributeList = ClustermgmtUtils.getAttributeListFromSelection(queryGroupBy);
    }
    InsightsInterfaceProto.BooleanExpression joinExpression = filterExpression;

    if (uuidType != null && uuid != null) {
      // Boolean expression to get host nics belong to either host/cluster based on uuid_type
      InsightsInterfaceProto.DataValue.Builder value =
              InsightsInterfaceProto.DataValue.newBuilder().setStrValue(uuid);
      InsightsInterfaceProto.BooleanExpression expression =
              clustermgmtResourceAdapter.adaptToBooleanExpression(uuidType,
                      InsightsInterfaceProto.ComparisonExpression.Operator.kEQ,
                      value);

      // Join Boolean Expression with Odate filter and above one.
      if (joinExpression != null)
        joinExpression = clustermgmtResourceAdapter.joinBooleanExpressions(
                expression, InsightsInterfaceProto.BooleanExpression.Operator.kAnd, joinExpression);
      else
        joinExpression = expression;
    }

    // Get Entities
    InsightsInterfaceProto.GetEntitiesWithMetricsRet getEntitiesWithMetricsRet =
      ClustermgmtUtils.getEntitiesWithMetricsRet(page, limit,
        HOST_NIC_ENTITY, entityDbProxy, attributeList, null,clustermgmtResourceAdapter, joinExpression, sortQueryOrderBy, null);
    log.debug("The RPC response GetEntitiesWithMetricsRet obtained {}", getEntitiesWithMetricsRet);

    if (!CollectionUtils.isEmpty(getEntitiesWithMetricsRet.getGroupResultsListList())) {
      totalEntityCount =
              (int) getEntitiesWithMetricsRet.getGroupResultsList(0).getTotalEntityCount();
      log.debug("The RPC response count {}", totalEntityCount);
      final List<InsightsInterfaceProto.EntityWithMetric> entitiesWithMetric =
              getEntitiesWithMetricsRet.getGroupResultsList(0).getRawResultsList();

      //get list of all switch interface entities with attachedHostUuid = host Uuid(uuid field)
      List<NetworkSwitchInterface> switchIntfEntities = getSwitchInterfaceEntities(uuid);

      for (final InsightsInterfaceProto.EntityWithMetric entityWithMetric : entitiesWithMetric) {
        final List<InsightsInterfaceProto.MetricData> metricDataList =
                entityWithMetric.getMetricDataListList();
        String hostNicUuid = entityWithMetric.getEntityGuid().getEntityId();

        HostNic hostNicEntity = new HostNic();
        hostNicEntity.setExtId(hostNicUuid);
        hostNicEntity = clusterResourceAdapter.adaptIdfHostNicMetricstoHostNicEntity(metricDataList, hostNicEntity);

        List<NetworkSwitchInterface> attachedSwitchIntfList =
          getAttachedSwitchInterfaceEntitiesForHostNic(hostNicUuid, switchIntfEntities);

        hostNicEntity.setAttachedSwitchInterfaceList(attachedSwitchIntfList);
        hostNicEntities.add(hostNicEntity);
      }
    }
    return new Pair<>(totalEntityCount,hostNicEntities);
  }

  @Override
  public Pair<Integer, List<HostNic> > getHostNics(String clusterUuid, String hostUuid, Integer limit, Integer page,
                                                         String filter, String orderBy, String selectString)
          throws ClustermgmtServiceException, ValidationException {
    // Validate and check if cluster is PE/PC.
    if (isMulticluster(clusterUuid)) {
      log.debug("PC Cluster");
      throw new ClustermgmtNotSupportedException("Host Nic Api is not supported on PC cluster uuid: " + clusterUuid);
    }

    // Validate and get host entity
    validateAndGetHostEntity(hostUuid);

    // Validate host belongs to cluster
    if(!validateHostBelongsToCluster(hostUuid, clusterUuid)) {
      throw new ClustermgmtNotFoundException(HOST_UUID_STRING + hostUuid + NOT_BELONG_TO_CLUSTER_STRING + clusterUuid);
    }

    if(orderBy == null){
      orderBy = DEFAULT_ORDERBY_ATTR;
    }

    return getHostNicEntitiesFromIDF(limit, page, filter, orderBy, selectString, hostUuid, NIC_NODE_UUID_ATTR);
  }

  @Override
  public Pair<Integer, List<HostNic> > getAllHostNics(Integer limit, Integer page,
                                                String filter, String orderBy, String selectString)
          throws ClustermgmtServiceException, ValidationException {
    Integer totalEntityCount = null;
    List<HostNic> hostNicEntities = new ArrayList<>();
    List<String> attributeList = ClustermgmtUtils.hostNicAttributeList;
    // Boolean expression from Odata filer
    final ClustermgmtOdataParser.Result odataResult =
            clustermgmtOdataParser.parse("/host-nics", filter, orderBy, null, selectString, null);

    List<InsightsInterfaceProto.QueryOrderBy> queryOrderBy = odataResult.getOrderBy();
    InsightsInterfaceProto.BooleanExpression filterExpression = odataResult.getFilter();
    InsightsInterfaceProto.QueryGroupBy queryGroupBy = odataResult.getGroupBy();
    InsightsInterfaceProto.QueryOrderBy sortQueryOrderBy = null;
    if(queryOrderBy != null && !CollectionUtils.isEmpty(queryOrderBy)) {
      sortQueryOrderBy = queryOrderBy.get(0);
    }
    if(selectString != null && queryGroupBy != null){
      attributeList = ClustermgmtUtils.getAttributeListFromSelection(queryGroupBy);
    }
    InsightsInterfaceProto.BooleanExpression joinExpression = filterExpression;
    
    // Get Entities
    InsightsInterfaceProto.GetEntitiesWithMetricsRet getEntitiesWithMetricsRet =
      ClustermgmtUtils.getEntitiesWithMetricsRet(page, limit,
        HOST_NIC_ENTITY, entityDbProxy, attributeList, null, clustermgmtResourceAdapter, joinExpression, sortQueryOrderBy, null);
    log.debug("The RPC response GetEntitiesWithMetricsRet obtained {}", getEntitiesWithMetricsRet);

    if (!CollectionUtils.isEmpty(getEntitiesWithMetricsRet.getGroupResultsListList())) {
      totalEntityCount =
              (int) getEntitiesWithMetricsRet.getGroupResultsList(0).getTotalEntityCount();
      log.debug("The RPC response count {}", totalEntityCount);
      final List<InsightsInterfaceProto.EntityWithMetric> entitiesWithMetric =
              getEntitiesWithMetricsRet.getGroupResultsList(0).getRawResultsList();

      //get list of all switch interface entities with attachedHostUuid = host Uuid(uuid field)
      List<NetworkSwitchInterface> switchIntfEntities = getSwitchInterfaceEntities(null);

      for (final InsightsInterfaceProto.EntityWithMetric entityWithMetric : entitiesWithMetric) {
        final List<InsightsInterfaceProto.MetricData> metricDataList =
                entityWithMetric.getMetricDataListList();
        String hostNicUuid = entityWithMetric.getEntityGuid().getEntityId();

        HostNic hostNicEntity = new HostNic();
        hostNicEntity.setExtId(hostNicUuid);
        hostNicEntity = clusterResourceAdapter.adaptIdfHostNicMetricstoHostNicEntity(metricDataList, hostNicEntity);

        List<NetworkSwitchInterface> attachedSwitchIntfList =
          getAttachedSwitchInterfaceEntitiesForHostNic(hostNicUuid, switchIntfEntities);

        hostNicEntity.setAttachedSwitchInterfaceList(attachedSwitchIntfList);
        hostNicEntities.add(hostNicEntity);
      }
    }
    return new Pair<>(totalEntityCount, hostNicEntities);
  }

  @Override
  public HostNic getHostNic(String clusterUuid, String hostUuid, String hostNicUuid)
    throws ClustermgmtServiceException{

    // Validate and check if cluster is PE/PC.
    if (isMulticluster(clusterUuid)) {
      log.debug("PC Cluster");
      throw new ClustermgmtNotSupportedException("Host Nic Api is not supported on PC cluster uuid: " + clusterUuid);
    }

    // Validate and get host entity
    validateAndGetHostEntity(hostUuid);

    // Validate host belongs to cluster
    if(!validateHostBelongsToCluster(hostUuid, clusterUuid)) {
      throw new ClustermgmtNotFoundException(HOST_UUID_STRING + hostUuid + NOT_BELONG_TO_CLUSTER_STRING + clusterUuid);
    }

    HostNic hostNicEntity = new HostNic();
    hostNicEntity.setExtId(hostNicUuid);

    //get the host nic entity with hostNicUuid and adapt to the entity type
    InsightsInterfaceProto.GetEntitiesWithMetricsRet getEntitiesWithMetricsRet =
      ClustermgmtUtils.getEntitiesWithMetricsRet(null, null,
        HOST_NIC_ENTITY, entityDbProxy, ClustermgmtUtils.hostNicAttributeList, hostNicUuid, clustermgmtResourceAdapter, null, null, null);
    log.debug("The RPC response GetEntitiesWithMetricsRet obtained {}", getEntitiesWithMetricsRet);

    if (!CollectionUtils.isEmpty(getEntitiesWithMetricsRet.getGroupResultsListList())){
      final InsightsInterfaceProto.EntityWithMetric entityWithMetric = getEntitiesWithMetricsRet.getGroupResultsList(0).getRawResults(0);

      final List<InsightsInterfaceProto.MetricData> metricDataList = entityWithMetric.getMetricDataListList();

      hostNicEntity = clusterResourceAdapter.adaptIdfHostNicMetricstoHostNicEntity(metricDataList, hostNicEntity);

      //get list of all switch interface entities with attachedHostUuid = host Uuid
      List<NetworkSwitchInterface> switchIntfEntities = getSwitchInterfaceEntities(hostUuid);

      List<NetworkSwitchInterface> attachedSwitchIntfList =
        getAttachedSwitchInterfaceEntitiesForHostNic(hostNicUuid, switchIntfEntities);

      hostNicEntity.setAttachedSwitchInterfaceList(attachedSwitchIntfList);
    }
    else{
      throw new ClustermgmtNotFoundException("Unknown Host Nic Uuid: " + hostNicUuid);
    }

    if (hostNicEntity.getNodeUuid().equals(hostUuid)) {
      return hostNicEntity;
    }
    else {
      throw new ClustermgmtNotFoundException("Host Nic with uuid: " + hostNicUuid + NOT_BELONG_TO_CLUSTER_STRING + clusterUuid +
        " and host: " + hostUuid);
    }
  }

  public Pair<Integer, List<VirtualNic> > getVirtualNicEntitiesFromIDF(Integer limit, Integer page, String filter,
                                                                   String orderBy, String select, String uuid, String uuidType)
    throws ClustermgmtServiceException, ValidationException {
    Integer totalEntityCount = null;
    List<VirtualNic> virtualNicEntities = new ArrayList<>();
    List<String> attributeList = ClustermgmtUtils.virtualNicAttributeList;

    // Boolean expression from Odata filer
    final ClustermgmtOdataParser.Result odataResult =
      clustermgmtOdataParser.parse("/virtual-nics", filter, orderBy, null, select, null);

    List<InsightsInterfaceProto.QueryOrderBy> queryOrderBy = odataResult.getOrderBy();
    InsightsInterfaceProto.BooleanExpression filterExpression = odataResult.getFilter();
    InsightsInterfaceProto.QueryGroupBy queryGroupBy = odataResult.getGroupBy();
    InsightsInterfaceProto.QueryOrderBy sortQueryOrderBy = null;
    if (queryOrderBy != null && !CollectionUtils.isEmpty(queryOrderBy)) {
      sortQueryOrderBy = queryOrderBy.get(0);
    }
    if(select != null && queryGroupBy != null){
      attributeList = ClustermgmtUtils.getAttributeListFromSelection(queryGroupBy);
    }
    InsightsInterfaceProto.BooleanExpression joinExpression = filterExpression;

    if (uuidType != null && uuid != null) {
      // Boolean expression to get virtual nics belong to either host/cluster based on uuid_type
      InsightsInterfaceProto.DataValue.Builder value =
        InsightsInterfaceProto.DataValue.newBuilder().setStrValue(uuid);
      InsightsInterfaceProto.BooleanExpression expression =
        clustermgmtResourceAdapter.adaptToBooleanExpression(uuidType,
          InsightsInterfaceProto.ComparisonExpression.Operator.kEQ,
          value);

      // Join Boolean Expression with Odate filter and above one.
      if (joinExpression != null)
        joinExpression = clustermgmtResourceAdapter.joinBooleanExpressions(
          expression, InsightsInterfaceProto.BooleanExpression.Operator.kAnd, joinExpression);
      else
        joinExpression = expression;
    }

    // Get Entities
    InsightsInterfaceProto.GetEntitiesWithMetricsRet getEntitiesWithMetricsRet =
      ClustermgmtUtils.getEntitiesWithMetricsRet(page, limit,
        VIRTUAL_NIC_ENTITY, entityDbProxy, attributeList, null,clustermgmtResourceAdapter, joinExpression, sortQueryOrderBy, null);
    log.debug("The RPC response GetEntitiesWithMetricsRet obtained {}", getEntitiesWithMetricsRet);

    if (!CollectionUtils.isEmpty(getEntitiesWithMetricsRet.getGroupResultsListList())) {
      totalEntityCount =
        (int) getEntitiesWithMetricsRet.getGroupResultsList(0).getTotalEntityCount();
      log.debug("The RPC response count {}", totalEntityCount);
      final List<InsightsInterfaceProto.EntityWithMetric> entitiesWithMetric =
        getEntitiesWithMetricsRet.getGroupResultsList(0).getRawResultsList();
      for (final InsightsInterfaceProto.EntityWithMetric entityWithMetric : entitiesWithMetric) {
        final List<InsightsInterfaceProto.MetricData> metricDataList =
          entityWithMetric.getMetricDataListList();
        String virtualNicUuid = entityWithMetric.getEntityGuid().getEntityId();

        VirtualNic virtualNicEntity = new VirtualNic();
        virtualNicEntity.setExtId(virtualNicUuid);
        virtualNicEntity = clusterResourceAdapter.adaptIdfVirtualNicMetricstoVirtualNicEntity(metricDataList, virtualNicEntity);
        virtualNicEntities.add(virtualNicEntity);
      }
    }
    return new Pair<>(totalEntityCount,virtualNicEntities);
  }

  @Override
  public Pair<Integer, List<VirtualNic> > getVirtualNics(String clusterUuid, String hostUuid, Integer limit, Integer page,
                                                         String filter, String orderBy, String selectString)
    throws ClustermgmtServiceException, ValidationException {
    // Validate and check if cluster is PE/PC.
    if (isMulticluster(clusterUuid)) {
      log.debug("PC Cluster");
      throw new ClustermgmtNotSupportedException("Virtual Nic Api is not supported on PC cluster uuid: " + clusterUuid);
    }

    // Validate and get host entity
    validateAndGetHostEntity(hostUuid);

    // Validate host belongs to cluster
    if(!validateHostBelongsToCluster(hostUuid, clusterUuid)) {
      throw new ClustermgmtNotFoundException(HOST_UUID_STRING + hostUuid + NOT_BELONG_TO_CLUSTER_STRING + clusterUuid);
    }

    if(orderBy == null){
      orderBy = DEFAULT_ORDERBY_ATTR;
    }

    return getVirtualNicEntitiesFromIDF(limit, page, filter, orderBy, selectString, hostUuid, NIC_NODE_UUID_ATTR);
  }

  @Override
  public VirtualNic getVirtualNic(String clusterUuid, String hostUuid, String virtualNicUuid)
    throws ClustermgmtServiceException {

    // Validate and check if cluster is PE/PC.
    if (isMulticluster(clusterUuid)) {
      log.debug("PC Cluster");
      throw new ClustermgmtNotSupportedException("Virtual Nic Api is not supported on PC cluster uuid: " + clusterUuid);
    }

    // Validate and get host entity
    validateAndGetHostEntity(hostUuid);

    // Validate host belongs to cluster
    if(!validateHostBelongsToCluster(hostUuid, clusterUuid)) {
      throw new ClustermgmtNotFoundException(HOST_UUID_STRING + hostUuid + NOT_BELONG_TO_CLUSTER_STRING + clusterUuid);
    }

    VirtualNic virtualNicEntity = new VirtualNic();
    virtualNicEntity.setExtId(virtualNicUuid);

    //get the virtual nic entity with virtualNicUuid and adapt to the entity type
    InsightsInterfaceProto.GetEntitiesWithMetricsRet getEntitiesWithMetricsRet =
      ClustermgmtUtils.getEntitiesWithMetricsRet(null, null,
        VIRTUAL_NIC_ENTITY, entityDbProxy, ClustermgmtUtils.virtualNicAttributeList, virtualNicUuid, clustermgmtResourceAdapter, null, null, null);
    log.debug("The RPC response GetEntitiesWithMetricsRet obtained {}", getEntitiesWithMetricsRet);

    if (!CollectionUtils.isEmpty(getEntitiesWithMetricsRet.getGroupResultsListList())) {
      final InsightsInterfaceProto.EntityWithMetric entityWithMetric = getEntitiesWithMetricsRet.getGroupResultsList(0).getRawResults(0);
      final List<InsightsInterfaceProto.MetricData> metricDataList = entityWithMetric.getMetricDataListList();
      virtualNicEntity = clusterResourceAdapter.adaptIdfVirtualNicMetricstoVirtualNicEntity(metricDataList, virtualNicEntity);
    }
    else{
      throw new ClustermgmtNotFoundException("Unknown Virtual Nic Uuid: " + virtualNicUuid);
    }

    if (virtualNicEntity.getNodeUuid().equals(hostUuid)) {
      return virtualNicEntity;
    }
    else {
      throw new ClustermgmtNotFoundException("Virtual Nic with uuid: " + virtualNicUuid + NOT_BELONG_TO_CLUSTER_STRING + clusterUuid +
        " and host: " + hostUuid);
    }
  }

  @Override
  public ClusterStats getClusterStatsInfo(String extId,
                                          DownSamplingOperator statsType,
                                          Integer samplingInterval,
                                          Long startTime,
                                          Long endTime,
                                          String selectString) throws StatsGatewayServiceException, ClustermgmtServiceException {
    // Validate and check if cluster is PE/PC.
    if (isMulticluster(extId)) {
      log.debug("PC Cluster");
      throw new ClustermgmtNotSupportedException("Cluster Stats Api is not supported on PC cluster uuid: " + extId);
    }

    //Validate the cluster entity
    GetEntitiesRet getEntitiesRet =
      ClustermgmtUtils.getInsightsEntityRet(extId, CLUSTER_ENTITY,
        entityDbProxy, clustermgmtResourceAdapter);
    if (getEntitiesRet.getEntityCount() == 0) {
      throw new ClustermgmtNotFoundException("Unknown Cluster Uuid: " + extId);
    }
    log.debug("Fetching Cluster stats");
    ClusterStats clusterStats = new ClusterStats();
    clusterStats.setExtId(extId);

    ParsedQuery parsedQuery;
    if(selectString==null){
      parsedQuery=null;
    }
    else{
      try {
        parsedQuery = clustermgmtOdataParser.parseStatsUri("/clusters", null, null, null, null, selectString, null);
      }
      catch(ValidationException e){
        throw new ClustermgmtOdataException("Failed to parse the select string: " + e.getMessage());
      }
      log.debug(String.valueOf(parsedQuery.getSelectInfo()));
    }

    StatsQueryResponse clusterStatsResp;
    try {

      clusterStatsResp = StatsUtils.getStatsResp(CLUSTER_ENTITY, extId,
        ClustermgmtUtils.clusterStatsAttributeList, statsType, samplingInterval, startTime, endTime, parsedQuery);

      log.debug("The entity for which stats are requested: ", clusterStatsResp.getEntityName());
    }
    catch(StatsGatewayServiceException e){
      throw new StatsGatewayServiceException("Unable to fetch stats due to exception: "+e.getMessage(), e);

    }

    Map<String,StatsQueryResponse.AttributeStats> entityStats =  clusterStatsResp.getEntityStatsMap();

    if(!entityStats.isEmpty()) {
      Map<String,List<TimeValuePair>> statsAttributesMap = new HashMap<>();
      if (entityStats.containsKey(extId)) {
        StatsQueryResponse.AttributeStats statsAttributes = entityStats.get(extId);
        for (Map.Entry<String, List<StatsQueryResponse.TimeValuePair>> entry : statsAttributes.getAttributeStatsMap().entrySet()) {
          List<TimeValuePair> timeSeriesData = new ArrayList<>();
          for (StatsQueryResponse.TimeValuePair timeValuePair : entry.getValue()) {
            Long time = timeValuePair.getTime();//assuming the value is in microEpochs
            List<String> values = timeValuePair.getValues();
            TimeValuePair timeValuePairListItem = new TimeValuePair();
            timeValuePairListItem.setTimestamp(DateUtils.fromEpochMicros(time));
            String value = values.get(0);
            Long statValue = ClustermgmtUtils.parseStringToLongValue(value);
            timeValuePairListItem.setValue(statValue);
            timeSeriesData.add(timeValuePairListItem);
          }
          statsAttributesMap.put(entry.getKey(), timeSeriesData);
        }
        clusterStats = clusterResourceAdapter.adaptToClusterStats(clusterStats, statsAttributesMap);
      }
    }

    log.debug("Returning Cluster stats");
    return clusterStats;
  }

  @Override
  public HostStats getHostStatsInfo(String clusterUuid,
                                    String hostUuid,
                                    DownSamplingOperator statsType,
                                    Integer samplingInterval,
                                    Long startTime,
                                    Long endTime, String selectString) throws StatsGatewayServiceException, ClustermgmtServiceException {
    // Validate and check if cluster is PE/PC.
    if (isMulticluster(clusterUuid)) {
      log.debug("PC Cluster");
      throw new ClustermgmtNotSupportedException("Host Stats Api is not supported on PC cluster uuid: " + clusterUuid);
    }

    // Validate host belongs to cluster
    if(!validateHostBelongsToCluster(hostUuid, clusterUuid)) {
      throw new ClustermgmtNotFoundException(HOST_UUID_STRING + hostUuid + NOT_BELONG_TO_CLUSTER_STRING + clusterUuid);
    }

    log.debug("Fetching Host stats");
    HostStats hostStats = new HostStats();
    hostStats.setExtId(hostUuid);

    ParsedQuery parsedQuery;
    if(selectString==null){
      parsedQuery=null;
    }
    else {
      try {
        parsedQuery = clustermgmtOdataParser.parseStatsUri("/hosts", null,null, null,null, selectString, null);
      } catch (ValidationException e) {
        throw new ClustermgmtOdataException("Failed to parse the select string: " + e.getMessage());
      }
      log.debug(String.valueOf(parsedQuery.getSelectInfo()));
    }

    StatsQueryResponse hostStatsResp;
    try {
      hostStatsResp = StatsUtils.getStatsResp(NODE_ENTITY, hostUuid,
        ClustermgmtUtils.hostStatsAttributeList, statsType, samplingInterval, startTime, endTime, parsedQuery);

      log.debug("The entity for which stats are requested: ", hostStatsResp.getEntityName());
    }
    catch(StatsGatewayServiceException e){
      throw new StatsGatewayServiceException("Unable to fetch stats due to exception: "+e.getMessage(), e);
    }

    Map<String,StatsQueryResponse.AttributeStats> entityStats =  hostStatsResp.getEntityStatsMap();

    if(!entityStats.isEmpty()) {
      Map<String,List<TimeValuePair>> statsAttributesMap = new HashMap<>();
      if (entityStats.containsKey(hostUuid)) {
        StatsQueryResponse.AttributeStats statsAttributes = entityStats.get(hostUuid);
        for (Map.Entry<String, List<StatsQueryResponse.TimeValuePair>> entry : statsAttributes.getAttributeStatsMap().entrySet()) {
          List<TimeValuePair> timeSeriesData = new ArrayList<>();
          for (StatsQueryResponse.TimeValuePair timeValuePair : entry.getValue()) {
            Long time = timeValuePair.getTime(); //assuming the value is in microEpochs
            List<String> values = timeValuePair.getValues();
            TimeValuePair timeValuePairListItem = new TimeValuePair();
            timeValuePairListItem.setTimestamp(DateUtils.fromEpochMicros(time));
            String value = values.get(0);
            Long statValue = ClustermgmtUtils.parseStringToLongValue(value);
            timeValuePairListItem.setValue(statValue);
            timeSeriesData.add(timeValuePairListItem);
          }
          statsAttributesMap.put(entry.getKey(), timeSeriesData);
        }
        hostStats = clusterResourceAdapter.adaptToHostStats(hostStats,statsAttributesMap);
      }
    }

    log.debug("Returning Host stats");
    return hostStats;
  }

  @Override
  public void clusterCreate(Cluster params, String taskUuid, Boolean $dryrun) throws ClustermgmtServiceException {
    ClusterManagementInterfaceProto.ClusterCreateArg.Builder clusterCreateArgBuilder = ClusterManagementInterfaceProto.ClusterCreateArg.newBuilder();

    Pair<Boolean, String> validationResponse = ClustermgmtUtils.validateClusterCreateParams(params);

    if(!validationResponse.left()){
      String msg = validationResponse.right();
      throw new ClustermgmtInvalidInputException(msg);
    }

    // FQDN should not be available in name server - since it is the one that resolves the FQDN to IP Address
    if(ClustermgmtUtils.doesNameServerHaveFqdn(params)){
      throw new ClustermgmtInvalidInputException("The name server cannot have an FQDN value provided, as it is responsible for resolving all FQDN addresses");
    }

    clusterCreateArgBuilder.setTaskUuid(UuidUtils.getByteStringFromUUID(taskUuid));
    if($dryrun != null)
    {
      clusterCreateArgBuilder.setPrechecksOnly($dryrun);
    }
    else
    {
      clusterCreateArgBuilder.setPrechecksOnly(false);
    }

    ClusterManagementInterfaceProto.ClusterCreateArg clusterCreateArgs = clusterResourceAdapter.adaptClusterCreateParamsToClusterCreateArg(params, clusterCreateArgBuilder);
    log.debug("Args to send to Create RPC: {}\n", clusterCreateArgs);


    ClusterManagementInterfaceProto.ClusterCreateRet clusterCreateRet =
        cmsProxy.doExecute(CMSProxyImpl.CMSRpcName.CREATE_CLUSTER, clusterCreateArgs, RequestContextHelper.getRpcRequestContext());

  }

  @Override
  public void clusterDestroy(String clusterUuid, String taskUuid, Boolean runPrechecksOnlyFlag) throws ClustermgmtServiceException {
    // PC-PE Capability check
    isRequestAllowed(clusterUuid, ClustermgmtUtils.CLUSTER_DESTROY);

    // Rpc Payload
    ClusterManagementInterfaceProto.ClusterDestroyArg.Builder clusterDestroyArgBuilder = ClusterManagementInterfaceProto.ClusterDestroyArg.newBuilder();
    clusterDestroyArgBuilder.setClusterUuid(clusterUuid);
    clusterDestroyArgBuilder.setTaskUuid(UuidUtils.getByteStringFromUUID(taskUuid));

    log.debug("Provided run prechecks only flag as query param.");

    if(runPrechecksOnlyFlag != null){
      clusterDestroyArgBuilder.setPrechecksOnly(runPrechecksOnlyFlag);
    }
    else{
      clusterDestroyArgBuilder.setPrechecksOnly(ClusterManagementInterfaceProto.ClusterDestroyArg.getDefaultInstance().hasPrechecksOnly());
    }

    ClusterManagementInterfaceProto.ClusterDestroyArg clusterDestroyArg = clusterDestroyArgBuilder.build();
    log.debug("Args to send to Destroy RPC: {}\n", clusterDestroyArg);

    ClusterManagementInterfaceProto.ClusterDestroyRet clusterDestroyRet=
      cmsProxy.doExecute(CMSProxyImpl.CMSRpcName.DESTROY_CLUSTER, clusterDestroyArg, RequestContextHelper.getRpcRequestContext());
  }

  /*
   * Updates category association for the requested entity
   *
   * @param entityReferences - list of category extIds passed in to payload in API request.
   *
   * @param entityUuid - UUID of the entity(cluster/host) to which categories needs to be updated.
   *
   * @param entityType - type of entity(cluster/node) which categories needs to be updated.
   *
   * @param operationType - Operation type either attach or detach.
   */
  @Override
  public TaskReference updateCategoryAssociationsForClusterEntity(CategoryEntityReferences params,
                                                          String clusterUuid,
                                                          String operationType) throws ClustermgmtServiceException {

    // Validate and check if cluster is PE/PC.
    if (isMulticluster(clusterUuid)) {
      log.debug("PC Cluster");
      throw new ClustermgmtNotSupportedException("Updating categories is not supported on PC cluster uuid: " + clusterUuid);
    }

    //Validate the cluster entity
    GetEntitiesRet getEntitiesRet =
      ClustermgmtUtils.getInsightsEntityRet(clusterUuid, CLUSTER_ENTITY,
        entityDbProxy, clustermgmtResourceAdapter);
    if (getEntitiesRet.getEntityCount() == 0) {
      throw new ClustermgmtNotFoundException("Unknown Cluster Uuid: " + clusterUuid);
    }

    TaskReference task = new TaskReference();
    Set<String> categoryUuids = new HashSet<>();
    for(final String categoryId: params.getCategories()){
      categoryUuids.add(categoryId);
    }
    if(categoryUuids.isEmpty()){
      log.debug("Empty categories list passed to update");
      throw new ClustermgmtInvalidInputException("No categories to update");
    }

    log.debug("Received request to " + operationType +  " categories to the cluster with uuid: " + clusterUuid + " with " + categoryUuids + "\n");

    GenesisInterfaceProto.UpdateCategoryAssociationsArg.Builder updateCategoryAssociationsArgBuilder
      = GenesisInterfaceProto.UpdateCategoryAssociationsArg.newBuilder();

    GenesisInterfaceProto.UpdateCategoryAssociationsArg updateCategoryAssociationsArgs
      = clusterResourceAdapter.adaptUpdateCategoriesParamsToUpdateCategoryAssociationsArg(categoryUuids,
      "cluster", clusterUuid, operationType, updateCategoryAssociationsArgBuilder);

    try{
      GenesisInterfaceProto.UpdateCategoryAssociationsRet updateCategoryAssociationsRet
        = genesisProxy.doExecute(GenesisProxyImpl.GenesisRpcName.UPDATE_CATEGORY_ASSOCIATIONS, updateCategoryAssociationsArgs, RequestContextHelper.getRpcRequestContext());
      if(updateCategoryAssociationsRet.hasTaskUuid()){
        final String taskExtId = ErgonTaskUtils.getTaskReferenceExtId(UuidUtils.getUUID(updateCategoryAssociationsRet.getTaskUuid()));
        task.setExtId(taskExtId);
      }
    }
    catch(ClustermgmtServiceException e){
      log.debug("Exception encountered: ", e);
      throw new ClustermgmtServiceException(e.toString());
    }

    return task;
  }

  @Override
  public TaskReference updateCategoryAssociationsForHostNicEntity(CategoryEntityReferences params,  String hostNicUuid, String clusterUuid,
                                                                  String hostUuid, String operationType) throws ClustermgmtServiceException {

    // Validate and check if cluster is PE/PC.
    if (isMulticluster(clusterUuid)) {
      log.debug("PC Cluster");
      throw new ClustermgmtNotSupportedException("Updating categories is not supported on PC cluster uuid: " + clusterUuid);
    }

    //Validate the cluster entity
    GetEntitiesRet getEntitiesRet =
      ClustermgmtUtils.getInsightsEntityRet(clusterUuid, CLUSTER_ENTITY,
        entityDbProxy, clustermgmtResourceAdapter);
    if (getEntitiesRet.getEntityCount() == 0) {
      throw new ClustermgmtNotFoundException("Unknown Cluster Uuid: " + clusterUuid);
    }

    // Validate and get host entity
    validateAndGetHostEntity(hostUuid);
    // Validate host belongs to cluster
    if(!validateHostBelongsToCluster(hostUuid, clusterUuid)) {
      throw new ClustermgmtNotFoundException(HOST_UUID_STRING + hostUuid + NOT_BELONG_TO_CLUSTER_STRING + clusterUuid);
    }

    // validate host nic uuid
    validateAndGetHostNicEntity(hostNicUuid);

    TaskReference task = new TaskReference();
    Set<String> categoryUuids = new HashSet<>();
    for(final String categoryId: params.getCategories()){
      categoryUuids.add(categoryId);
    }
    if(categoryUuids.isEmpty()){
      log.debug("Empty categories list passed to update");
      throw new ClustermgmtInvalidInputException("No categories to update");
    }

    log.debug("Received request to " + operationType +  " categories to the host_nic with uuid: " + hostNicUuid + " with " + categoryUuids + "\n");

    GenesisInterfaceProto.UpdateCategoryAssociationsArg.Builder updateCategoryAssociationsArgBuilder
      = GenesisInterfaceProto.UpdateCategoryAssociationsArg.newBuilder();

    GenesisInterfaceProto.UpdateCategoryAssociationsArg updateCategoryAssociationsArgs
      = clusterResourceAdapter.adaptUpdateCategoriesParamsToUpdateCategoryAssociationsArg(categoryUuids,
      "host_nic", hostNicUuid, operationType, updateCategoryAssociationsArgBuilder);

    try{
      GenesisInterfaceProto.UpdateCategoryAssociationsRet updateCategoryAssociationsRet
        = genesisProxy.doExecute(GenesisProxyImpl.GenesisRpcName.UPDATE_CATEGORY_ASSOCIATIONS, updateCategoryAssociationsArgs, RequestContextHelper.getRpcRequestContext());
      if(updateCategoryAssociationsRet.hasTaskUuid()){
        final String taskExtId = ErgonTaskUtils.getTaskReferenceExtId(UuidUtils.getUUID(updateCategoryAssociationsRet.getTaskUuid()));
        task.setExtId(taskExtId);
      }
    }
    catch(ClustermgmtServiceException e){
      log.debug("Exception encountered: ", e);
      throw new ClustermgmtServiceException(e.toString());
    }

    return task;
  }
  
  @Override
  public void enterHostMaintenance(String clusterExtId, String hostExtId, EnterHostMaintenanceSpec enterHostMaintenance, String taskUuid)
    throws ClustermgmtServiceException, ValidationException {

    if(isMulticluster(clusterExtId)) {
      throw new ClustermgmtServiceException("Enter host maintenance API is not supported on PC: " + clusterExtId);
    }

    // PC-PE Capability check
    isRequestAllowed(clusterExtId, ClustermgmtUtils.ENTER_HOST_MAINTENANCE);

    // Validate and get host entity
    validateAndGetHostEntity(hostExtId);

    // Validate host belongs to cluster
    if(!validateHostBelongsToCluster(hostExtId, clusterExtId)) {
      throw new ClustermgmtNotFoundException(HOST_UUID_STRING + hostExtId + NOT_BELONG_TO_CLUSTER_STRING + clusterExtId);
    }

    // Cluster EntityId
    ErgonTypes.EntityId entityId = ClustermgmtUtils.getClusterEntityId(clusterExtId);

    // Create PC task
    ErgonInterface.TaskCreateArg createArg = ClustermgmtUtils.createTaskArg(UuidUtils.getByteStringFromUUID(taskUuid),
      PLANNED_OUTAGE_COMPONENT_NAME, "Enter Host Maintenance", "Enter Host Maintenance", null, null,
      entityId, RequestContextHelper.getRpcRequestContext());
    ErgonInterface.TaskCreateRet taskCreateRet = ergonProxy.doExecute(ErgonProxyImpl.ErgonRpcName.TASK_CREATE, createArg);

    // update discover unconfigured arg
    final String payload =
      clusterResourceAdapter.adaptEnterHostMaintenanceToJsonRpcFormat(
        enterHostMaintenance, hostExtId, taskUuid);

    // NATS Call
    publishASyncJsonRpcMessageToNats(clusterExtId, taskUuid, payload);
  }

  @Override
  public void exitHostMaintenance(String clusterExtId, String hostExtId, HostMaintenanceCommonSpec exitHostMaintenance, String taskUuid)
    throws ClustermgmtServiceException, ValidationException {

    if(isMulticluster(clusterExtId)) {
      throw new ClustermgmtServiceException("Exit host maintenance API is not supported on PC: " + clusterExtId);
    }

    // PC-PE Capability check
    isRequestAllowed(clusterExtId, ClustermgmtUtils.EXIT_HOST_MAINTENANCE);

    // Validate and get host entity
    validateAndGetHostEntity(hostExtId);

    // Validate host belongs to cluster
    if(!validateHostBelongsToCluster(hostExtId, clusterExtId)) {
      throw new ClustermgmtNotFoundException(HOST_UUID_STRING + hostExtId + NOT_BELONG_TO_CLUSTER_STRING + clusterExtId);
    }

    // Cluster EntityId
    ErgonTypes.EntityId entityId = ClustermgmtUtils.getClusterEntityId(clusterExtId);

    // Create PC task
    ErgonInterface.TaskCreateArg createArg = ClustermgmtUtils.createTaskArg(UuidUtils.getByteStringFromUUID(taskUuid),
      PLANNED_OUTAGE_COMPONENT_NAME, "Exit Host Maintenance", "Exit Host Maintenance", null, null,
      entityId, RequestContextHelper.getRpcRequestContext());
    ErgonInterface.TaskCreateRet taskCreateRet = ergonProxy.doExecute(ErgonProxyImpl.ErgonRpcName.TASK_CREATE, createArg);

    // update discover unconfigured arg
    final String payload =
      clusterResourceAdapter.adaptExitHostMaintenanceToJsonRpcFormat(
        exitHostMaintenance, hostExtId, taskUuid);

    // NATS Call
    publishASyncJsonRpcMessageToNats(clusterExtId, taskUuid, payload);
  }

  @Override
  public void computeNonMigratableVms(String clusterExtId, ComputeNonMigratableVmsSpec computeNonMigratableVms, String taskUuid)
    throws ClustermgmtServiceException, ValidationException {

    if (isMulticluster(clusterExtId)) {
      throw new ClustermgmtServiceException("Compute Non Migratable VMs is not supported on PC cluster uuid: " + clusterExtId);
    }

    // PC-PE Capability check
    isRequestAllowed(clusterExtId, ClustermgmtUtils.COMPUTE_NON_MIGRATABLE_VMS);

    // Cluster EntityId
    ErgonTypes.EntityId entityId = ClustermgmtUtils.getClusterEntityId(clusterExtId);

    // Create PC task
    ErgonInterface.TaskCreateArg createArg = ClustermgmtUtils.createTaskArg(UuidUtils.getByteStringFromUUID(taskUuid),
    GENESIS_COMPONENT_NAME, "Compute Non Migratable VMs", "Compute Non Migratable VMs", null, null,
    entityId, RequestContextHelper.getRpcRequestContext());
    ErgonInterface.TaskCreateRet taskCreateRet = ergonProxy.doExecute(ErgonProxyImpl.ErgonRpcName.TASK_CREATE, createArg);

    // update discover unconfigured arg
    final String payload =
      clusterResourceAdapter.adaptComputeNonMigratableVmsToJsonRpcFormat(
        computeNonMigratableVms, taskUuid);

    // NATS Call
    publishASyncJsonRpcMessageToNats(clusterExtId, taskUuid, payload);
  }

  @Override
  public NonMigratableVmsResult getNonMigratableVmsResult(String extId)
    throws ClustermgmtServiceException, ValidationException {
    List<String> attributeList = ClustermgmtUtils.nonMigratableVmsAttributeList;
    InsightsInterfaceProto.GetEntitiesWithMetricsRet getEntitiesWithMetricsRet =
      ClustermgmtUtils.getEntitiesWithMetricsRet(null, null, 
        NON_MIGRATABLE_VMS_ENTITY, entityDbProxy, attributeList, extId, clustermgmtResourceAdapter, null, null, null);

    log.debug("The RPC response GetEntitiesWithMetricsRet obtained {}", getEntitiesWithMetricsRet);

    NonMigratableVmsResult nonMigratableVmsResult = new NonMigratableVmsResult();
    nonMigratableVmsResult.setExtId(extId);
    if (!CollectionUtils.isEmpty(getEntitiesWithMetricsRet.getGroupResultsListList())) {
      final InsightsInterfaceProto.EntityWithMetric entityWithMetric = getEntitiesWithMetricsRet.getGroupResultsList(0).getRawResults(0);
      final List<InsightsInterfaceProto.MetricData> metricDataList = entityWithMetric.getMetricDataListList();
      nonMigratableVmsResult = clusterResourceAdapter.adaptJsonResponseToNonMigratableVms(metricDataList, nonMigratableVmsResult);
    }
    else{
      throw new ClustermgmtNotFoundException("Unknown NonMigratableVmsResult Uuid: " + extId);
    }
    return nonMigratableVmsResult;
  }

}
