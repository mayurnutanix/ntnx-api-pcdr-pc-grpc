/*
 * Copyright (c) 2023 Nutanix Inc. All rights reserved.
 *
 * Author: ritik.nawal@nutanix.com
 */

package com.nutanix.clustermgmtserver.utils;

import clustermgmt.v4.config.ClustersApiControllerInterface;
import com.nutanix.clustermgmtserver.links.ApiLinkFactory;
import dp1.clu.common.v1.response.ApiLink;
import dp1.clu.prism.v4.config.TaskReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MetadataHateOsLinkUtils {

  private static final String SELF_LINK_RELATION = "self";
  private static final String SNMP_LINK_RELATION = "snmp";
  private static final String ADD_SNMP_USERS_LINK_RELATION = "add-snmp-users";
  private static final String ADD_SNMP_TRAPS_LINK_RELATION = "add-snmp-traps";
  private static final String RSYSLOG_SERVERS_LINK_RELATION = "rsyslog-servers";
  private static final String RACKABLE_UNITS_LINK_RELATION = "rackable-units";
  private static final String FAULT_TOLERANCE_LINK_RELATION = "fault-tolerance-status";
  private static final String HOSTS_SELF_LINK_RELATION = "hosts";
  private static final String ALL_HOST_GPUS_LINK_RELATION = "host-gpus";
  private static final String VIRTUAL_GPU_PROFILES_LINK_RELATION = "virtual-gpu-profiles";
  private static final String PHYSICAL_GPU_PROFILES_LINK_RELATION = "physical-gpu-profiles";
  private static final String HOST_GPUS_LINK_RELATION = "host-gpus";
  private static final String HOST_NICS_LINK_RELATION = "host-nics";
  private static final String VIRTUAL_NICS_LINK_RELATION = "virtual_nics";
  private static final String DISCOVER_NODES_LINK_RELATION = "discover-unconfigured-nodes";
  private static final String FETCH_NETWORKING_LINK_RELATION = "fetch-node-networking-details";
  private static final String EXPAND_CLUSTER_LINK_RELATION = "expand-cluster";
  private static final String CHECK_HYP_REQ_LINK_RELATION = "check-hypervisor-requirements";
  private static final String REMOVE_NODE_LINK_RELATION = "remove-node";
  private static final String VALIDATE_NODE_LINK_RELATION = "validate-node";
  private static final String SNMP_UPDATE_LINK_RELATION = "update-status";
  private static final String SNMP_ADD_TRANSPORT_LINK_RELATION = "add-transport";
  private static final String SNMP_REMOVE_TRANSPORT_RELATION = "remove-transport";
  private static final String ASSOCIATE_CATEGORIES_LINK_RELATION = "associate-categories";
  private static final String DISASSOCIATE_CATEGORIES_LINK_RELATION = "disassociate-categories";
  private static final String ENTER_HOST_MAINTENANCE_LINK_RELATION = "enter-host-maintenance";
  private static final String EXIT_HOST_MAINTENANCE_LINK_RELATION = "exit-host-maintenance";
  private static final String COMPUTE_NON_MIGRATABLE_VMS_LINK_RELATION = "compute-non-migratable-vms";

  /* get self link*/
  public static List<ApiLink> getMetadataHateOsLinksForSelf(final String path, final Object... uriVariables){
    String selfLinkTemplate = ApiLinkFactory.getPrismGatewayUrl(path);
    //self link
    ApiLink selfLink = ApiLinkFactory.getHATEOASLink(selfLinkTemplate, SELF_LINK_RELATION, uriVariables);
    List<ApiLink> linkList = new ArrayList<>();
    linkList.add(selfLink);
    return linkList;
  }

  /* link for ergon task */
  public static List<ApiLink> getMetadataHateOsLinkForTask(final TaskReference task){
    String location = ApiLinkFactory.getTaskUrl(task);
    ApiLink link = ApiLinkFactory.getHATEOASLink(location, SELF_LINK_RELATION);
    return Collections.singletonList(link);
  }

  /* links for stats apis */
  public static List<ApiLink> getMetadataHateOsLinksForStatsEndpoint(final String path, final Object... uriVariables){
    String selfLinkTemplate = ApiLinkFactory.getPrismGatewayUrl(path);
    //self link
    ApiLink selfLink = ApiLinkFactory.getHATEOASLink(selfLinkTemplate, SELF_LINK_RELATION, uriVariables);
    Object[] vars = Arrays.stream(uriVariables).toArray();
    String entityLinkTemplate;
    ApiLink entityLink;
    if (vars.length == 1){
      entityLinkTemplate = ApiLinkFactory.getPrismGatewayUrl(ClustersApiControllerInterface.GET_CLUSTER_BY_ID_URI);
      entityLink = ApiLinkFactory.getHATEOASLink(entityLinkTemplate, "cluster", vars[0]);
    }
    else{
      entityLinkTemplate = ApiLinkFactory.getPrismGatewayUrl(ClustersApiControllerInterface.GET_HOST_BY_ID_URI);
      entityLink = ApiLinkFactory.getHATEOASLink(entityLinkTemplate, "host", vars[0], vars[1]);
    }
    List<ApiLink> linkList = new ArrayList<>();
    linkList.add(selfLink);
    linkList.add(entityLink);
    return linkList;
  }

  /*links for /clusters/{extId} endpoint*/
  public static List<ApiLink> getMetadataHateOsLinksForClustersEndpoint(final String extId){
    String selfLinkTemplate = ApiLinkFactory.getPrismGatewayUrl(ClustersApiControllerInterface.GET_CLUSTER_BY_ID_URI);
    ApiLink selfLink = ApiLinkFactory.getHATEOASLink(selfLinkTemplate, SELF_LINK_RELATION, extId);
    String snmpLinkTemplate = ApiLinkFactory.getPrismGatewayUrl(ClustersApiControllerInterface.GET_SNMP_CONFIG_BY_CLUSTER_ID_URI);
    ApiLink snmpLink = ApiLinkFactory.getHATEOASLink(snmpLinkTemplate, SNMP_LINK_RELATION, extId);
    String rsyslogServersLinkTemplate = ApiLinkFactory.getPrismGatewayUrl(ClustersApiControllerInterface.LIST_RSYSLOG_SERVERS_BY_CLUSTER_ID_URI);
    ApiLink rsyslogServersLink = ApiLinkFactory.getHATEOASLink(rsyslogServersLinkTemplate, RSYSLOG_SERVERS_LINK_RELATION, extId);
    String rackableUnitsLinkTemplate = ApiLinkFactory.getPrismGatewayUrl(ClustersApiControllerInterface.LIST_RACKABLE_UNITS_BY_CLUSTER_ID_URI);
    ApiLink rackableUnitsLink = ApiLinkFactory.getHATEOASLink(rackableUnitsLinkTemplate, RACKABLE_UNITS_LINK_RELATION, extId);
    String faultToleranceStatusTemplate = ApiLinkFactory.getPrismGatewayUrl(ClustersApiControllerInterface.GET_FAULT_TOLERANCE_STATUS_BY_CLUSTER_ID_URI);
    ApiLink faultToleranceStatusLink = ApiLinkFactory.getHATEOASLink(faultToleranceStatusTemplate, FAULT_TOLERANCE_LINK_RELATION, extId);
    String hostsLinkTemplate = ApiLinkFactory.getPrismGatewayUrl(ClustersApiControllerInterface.LIST_HOSTS_BY_CLUSTER_ID_URI);
    ApiLink hostsLink = ApiLinkFactory.getHATEOASLink(hostsLinkTemplate, HOSTS_SELF_LINK_RELATION, extId);
    String virtualGpuProfilesTemplate = ApiLinkFactory.getPrismGatewayUrl(ClustersApiControllerInterface.LIST_VIRTUAL_GPU_PROFILES_URI);
    ApiLink virtualGpuProfilesLink = ApiLinkFactory.getHATEOASLink(virtualGpuProfilesTemplate, VIRTUAL_GPU_PROFILES_LINK_RELATION, extId);
    String physicalGpuProfilesTemplate = ApiLinkFactory.getPrismGatewayUrl(ClustersApiControllerInterface.LIST_PHYSICAL_GPU_PROFILES_URI);
    ApiLink physicalGpuProfilesLink = ApiLinkFactory.getHATEOASLink(physicalGpuProfilesTemplate, PHYSICAL_GPU_PROFILES_LINK_RELATION, extId);
    String discoverNodesLinkTemplate = ApiLinkFactory.getPrismGatewayUrl(ClustersApiControllerInterface.DISCOVER_UNCONFIGURED_NODES_URI);
    ApiLink discoverNodesLink = ApiLinkFactory.getHATEOASLink(discoverNodesLinkTemplate, DISCOVER_NODES_LINK_RELATION, extId);
    String fetchNetworkingLinkTemplate = ApiLinkFactory.getPrismGatewayUrl(ClustersApiControllerInterface.FETCH_NODE_NETWORKING_DETAILS_URI);
    ApiLink fetchNetworkingLink = ApiLinkFactory.getHATEOASLink(fetchNetworkingLinkTemplate, FETCH_NETWORKING_LINK_RELATION, extId);
    String expandClusterLinkTemplate = ApiLinkFactory.getPrismGatewayUrl(ClustersApiControllerInterface.EXPAND_CLUSTER_URI);
    ApiLink expandClusterLink = ApiLinkFactory.getHATEOASLink(expandClusterLinkTemplate, EXPAND_CLUSTER_LINK_RELATION, extId);
    String removeNodeLinkTemplate = ApiLinkFactory.getPrismGatewayUrl(ClustersApiControllerInterface.REMOVE_NODE_URI);
    ApiLink removeNodeLink = ApiLinkFactory.getHATEOASLink(removeNodeLinkTemplate, REMOVE_NODE_LINK_RELATION, extId);
    String checkHypervisorReqdLinkTemplate = ApiLinkFactory.getPrismGatewayUrl(ClustersApiControllerInterface.CHECK_HYPERVISOR_REQUIREMENTS_URI);
    ApiLink checkHypervisorReqdLink = ApiLinkFactory.getHATEOASLink(checkHypervisorReqdLinkTemplate, CHECK_HYP_REQ_LINK_RELATION, extId);
    String validateNodeLinkTemplate = ApiLinkFactory.getPrismGatewayUrl(ClustersApiControllerInterface.VALIDATE_NODE_URI);
    ApiLink validateNodeLink = ApiLinkFactory.getHATEOASLink(validateNodeLinkTemplate, VALIDATE_NODE_LINK_RELATION, extId);

    List<ApiLink> allLinks = new ArrayList<>();
    allLinks.add(selfLink);
    allLinks.add(snmpLink);
    allLinks.add(rsyslogServersLink);
    allLinks.add(rackableUnitsLink);
    allLinks.add(faultToleranceStatusLink);
    allLinks.add(hostsLink);
    allLinks.add(virtualGpuProfilesLink);
    allLinks.add(physicalGpuProfilesLink);
    allLinks.add(discoverNodesLink);
    allLinks.add(fetchNetworkingLink);
    allLinks.add(expandClusterLink);
    allLinks.add(removeNodeLink);
    allLinks.add(checkHypervisorReqdLink);
    allLinks.add(validateNodeLink);
    return allLinks;
  }

  /*links for /clusters/{clusterUuid}/hosts/{extId} endpoint*/
  public static List<ApiLink> getMetadataHateOsLinksForHostsEndpoint(final String clusterUuid, final String extId) {
    String selfLinkTemplate = ApiLinkFactory.getPrismGatewayUrl(ClustersApiControllerInterface.GET_HOST_BY_ID_URI);
    ApiLink selfLink = ApiLinkFactory.getHATEOASLink(selfLinkTemplate, SELF_LINK_RELATION, clusterUuid, extId);
    String hostNicsTemplate = ApiLinkFactory.getPrismGatewayUrl(ClustersApiControllerInterface.LIST_HOST_NICS_BY_HOST_ID_URI);
    ApiLink hostNicsLink = ApiLinkFactory.getHATEOASLink(hostNicsTemplate, HOST_NICS_LINK_RELATION, clusterUuid, extId);
    String virtualNicsTemplate = ApiLinkFactory.getPrismGatewayUrl(ClustersApiControllerInterface.LIST_VIRTUAL_NICS_BY_HOST_ID_URI);
    ApiLink virtualNicsLink = ApiLinkFactory.getHATEOASLink(virtualNicsTemplate, VIRTUAL_NICS_LINK_RELATION, clusterUuid, extId);
    String enterHostMaintenanceTemplate = ApiLinkFactory.getPrismGatewayUrl(clustermgmt.v4.operations.ClustersApiControllerInterface.ENTER_HOST_MAINTENANCE_URI);
    ApiLink enterHostMaintenanceLink = ApiLinkFactory.getHATEOASLink(enterHostMaintenanceTemplate, ENTER_HOST_MAINTENANCE_LINK_RELATION, clusterUuid, extId);
    String exitHostMaintenanceTemplate = ApiLinkFactory.getPrismGatewayUrl(clustermgmt.v4.operations.ClustersApiControllerInterface.EXIT_HOST_MAINTENANCE_URI);
    ApiLink exitHostMaintenanceLink = ApiLinkFactory.getHATEOASLink(exitHostMaintenanceTemplate, EXIT_HOST_MAINTENANCE_LINK_RELATION, clusterUuid, extId);
    String computeNonMigratableVmsTemplate = ApiLinkFactory.getPrismGatewayUrl(ClustersApiControllerInterface.COMPUTE_NON_MIGRATABLE_VMS_URI);
    ApiLink computeNonMigratableVmsLink = ApiLinkFactory.getHATEOASLink(computeNonMigratableVmsTemplate, COMPUTE_NON_MIGRATABLE_VMS_LINK_RELATION, clusterUuid, extId);

    List<ApiLink> allLinks = new ArrayList<>();
    allLinks.add(selfLink);
    allLinks.add(hostNicsLink);
    allLinks.add(virtualNicsLink);
    allLinks.add(enterHostMaintenanceLink);
    allLinks.add(exitHostMaintenanceLink);
    allLinks.add(computeNonMigratableVmsLink);

    return allLinks;
  }

  public static List<ApiLink> getMetadataHateOsLinksForHostNicEndpoint(final String clusterUuid, final String hostUuid, final String extId) {
    String selfLinkTemplate = ApiLinkFactory.getPrismGatewayUrl(ClustersApiControllerInterface.GET_HOST_NIC_BY_ID_URI);
    ApiLink selfLink = ApiLinkFactory.getHATEOASLink(selfLinkTemplate, SELF_LINK_RELATION, clusterUuid, hostUuid, extId);
    String categoriesAssociationTemplate = ApiLinkFactory.getPrismGatewayUrl(ClustersApiControllerInterface.ASSOCIATE_CATEGORIES_TO_HOST_NIC_URI);
    ApiLink categoriesAssociationLink = ApiLinkFactory.getHATEOASLink(categoriesAssociationTemplate, ASSOCIATE_CATEGORIES_LINK_RELATION, clusterUuid, hostUuid, extId);
    String categoriesDisassociationTemplate = ApiLinkFactory.getPrismGatewayUrl(ClustersApiControllerInterface.DISASSOCIATE_CATEGORIES_FROM_HOST_NIC_URI);
    ApiLink categoriesDisassociationLink = ApiLinkFactory.getHATEOASLink(categoriesDisassociationTemplate, DISASSOCIATE_CATEGORIES_LINK_RELATION, clusterUuid, hostUuid, extId);

    List<ApiLink> allLinks = new ArrayList<>();
    allLinks.add(selfLink);
    allLinks.add(categoriesAssociationLink);
    allLinks.add(categoriesDisassociationLink);

    return allLinks;
  }

  /*links for /clusters/{clusterUuid}/snmp endpoint*/
  public static List<ApiLink> getMetadataHateOsLinksForSnmpEndpoint(final String clusterUuid){
    String selfLinkTemplate = ApiLinkFactory.getPrismGatewayUrl(ClustersApiControllerInterface.GET_SNMP_CONFIG_BY_CLUSTER_ID_URI);
    ApiLink selfLink = ApiLinkFactory.getHATEOASLink(selfLinkTemplate, SELF_LINK_RELATION, clusterUuid);
    String snmpUsersLinkTemplate = ApiLinkFactory.getPrismGatewayUrl(ClustersApiControllerInterface.CREATE_SNMP_USER_URI);
    ApiLink snmpUsersLink = ApiLinkFactory.getHATEOASLink(snmpUsersLinkTemplate, ADD_SNMP_USERS_LINK_RELATION, clusterUuid);
    String snmpTrapsLinkTemplate = ApiLinkFactory.getPrismGatewayUrl(ClustersApiControllerInterface.CREATE_SNMP_TRAP_URI);
    ApiLink snmpTrapsLink = ApiLinkFactory.getHATEOASLink(snmpTrapsLinkTemplate, ADD_SNMP_TRAPS_LINK_RELATION, clusterUuid);
    String updateStatusLinkTemplate = ApiLinkFactory.getPrismGatewayUrl(ClustersApiControllerInterface.UPDATE_SNMP_STATUS_URI);
    ApiLink updateStatusLink = ApiLinkFactory.getHATEOASLink(updateStatusLinkTemplate, SNMP_UPDATE_LINK_RELATION, clusterUuid);
    String addSnmpTransportLinkTemplate = ApiLinkFactory.getPrismGatewayUrl(ClustersApiControllerInterface.ADD_SNMP_TRANSPORT_URI);
    ApiLink addSnmpTransportLink = ApiLinkFactory.getHATEOASLink(addSnmpTransportLinkTemplate, SNMP_ADD_TRANSPORT_LINK_RELATION, clusterUuid);
    String removeSnmpTransportLinkTemplate = ApiLinkFactory.getPrismGatewayUrl(ClustersApiControllerInterface.REMOVE_SNMP_TRANSPORT_URI);
    ApiLink removeSnmpTransportLinkTemplateLink = ApiLinkFactory.getHATEOASLink(removeSnmpTransportLinkTemplate, SNMP_REMOVE_TRANSPORT_RELATION, clusterUuid);

    List<ApiLink> allLinks = new ArrayList<>();
    allLinks.add(selfLink);
    allLinks.add(snmpUsersLink);
    allLinks.add(snmpTrapsLink);
    allLinks.add(updateStatusLink);
    allLinks.add(addSnmpTransportLink);
    allLinks.add(removeSnmpTransportLinkTemplateLink);

    return allLinks;
  }
}
