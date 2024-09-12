/*
 * Copyright (c) 2021 Nutanix Inc. All rights reserved.
 *
 * Author: rakesh.falak@nutanix.com
 */

package com.nutanix.clustermgmtserver.utils;


import com.fasterxml.jackson.databind.JsonNode;
import com.nutanix.api.utils.json.JsonUtils;
import com.nutanix.cluster_management.ClusterManagementInterfaceProto;
import dp1.clu.common.v1.config.*;
import dp1.clu.prism.v4.config.TaskReference;
import com.google.protobuf.ByteString;
import com.nutanix.infrastructure.cluster.genesis.GenesisInterfaceProto;
import com.nutanix.insights.ifc.InsightsInterfaceProto;
import com.nutanix.insights.ifc.InsightsInterfaceProto.DataValue.StrList;
import com.nutanix.prism.adapter.service.ZeusConfiguration;
import com.nutanix.prism.adapter.service.ZeusConfigurationImpl;
import com.nutanix.util.base.Pair;
import com.nutanix.util.base.UuidUtils;
import com.nutanix.zeus.protobuf.Configuration;
import com.nutanix.zeus.protobuf.DomainFaultToleranceStateProto;
import com.nutanix.zeus.protobuf.MessageEntityProto;
import dp1.clu.clustermgmt.v4.config.*;
import dp1.clu.clustermgmt.v4.operations.*;
import lombok.extern.slf4j.Slf4j;
import nutanix.ergon.ErgonInterface;
import nutanix.ergon.ErgonTypes;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.Deflater;

@Slf4j
public class ClusterTestUtils {

  public static final String TEST_ARTIFACT_BASE_PATH = "src/test/java/com/nutanix/clustermgmtserver/resources";

  public static final String CLUSTER_NAME =
    "test-cluster-name";

  public static final String CLUSTER_UUID =
    "5cafbb40-dfd4-4eaf-ac8d-5ff91a230c7a";

  public static final String CLUSTER_UUID_2 =
    "6cafbb40-dfd4-4eaf-ac8d-5df91a230c7a";

  public static final String MANAGEMENT_SERVER_UUID =
    "5cafbb40-dfd4-4eaf-ac8d-5ff91a230c7a";

  public static final String RACKABLE_UNIT_UUID =
    "5cafbb40-dfd4-4eaf-ac8d-5ff91a230c7a";

  public static final String TASK_UUID =
    "cfb08be0-3f01-446c-6649-dc9303394c91";

  public static final String CHILD_TASK_UUID =
    "5cafbb40-dfd4-4eaf-ac8d-5ff91a230c7a";

  public static final String RESOURCE_ID =
    "5cafbb40-dfd4-4eaf-ac8d-5ff91a230c7a";

  public static final Integer RACKABLE_UNIT_ID = 2;

  public static final String CVM_IPV4 =
    "10.10.10.10";

  public static final String HOST_IPV4 =
    "10.10.10.10";

  public static final Boolean SNMP_STATUS = true;

  public static final String SNMP_USERNAME = "testSnmpUserName";

  public static final String SNMP_AUTH_KEY = "testSnmpAuthKey";

  public static final String SNMP_PRIV_KEY = "testSnmpPrivKey";

  public static final String SNMP_USER_UUID = "57115848-711b-4f03-b94c-3b3e2e014639";

  public static final String SNMP_TRAP_UUID = "57115848-711b-4f03-b94c-3b3e2e014639";

  public static final String SNMP_TRANSPORT_UUID = "57115848-711b-4f03-b94c-3b3e2e014639";

  public static final Integer SNMP_PORT = 8080;

  public static final String RSYSLOG_SERVER_NAME = "testRsyslogServerName";

  public static final String RSYSLOG_SERVER_UUID = "57115848-711b-4f03-b94c-3b3e2e014639";

  public static final String RSYSLOG_MODULE_NAME = "acropolis";

  public static final String SNMP_TRAP_ADDRESS = "10.10.10.10";

  public static final String USER_UUID = "57115848-711b-4f03-b94c-3b3e2e014639";

  public static final String TASK_COLON_UUID =
    "ZXJnb24=:cfb08be0-3f01-446c-6649-dc9303394c91";

  public static final String CLUSTER_VERSION =
    "2024.1";

  public static final String CATEGORY_EXT_ID =
    "70f77736-b5e8-52d0-b587-9e58afc90d3e";

  public static final String CLUSTER_PROFILE_UUID =
    "70f77736-b5e8-52d0-b587-9e58afc90d3e";

  public static final String CLUSTER_PROFILE_NAME =
    "test-profile-name";

  public static final String HTTP_PROXY_UUID =
    "70f77736-b5e8-52d0-b587-9e58afc90d3e";

  public static final String HTTP_PROXY_ADDRESS =
    "1.2.3.4";

  public static final String HTTP_PROXY_NAME = "proxy1";

  public static final String HTTP_PROXY_USERNAME = "testHttpProxyUsername";

  public static final String HTTP_PROXY_PASSWORD = "testHttpProxyPassword";

  public static final String SMTP_SERVER_USERNAME = "testSmtpUsername";

  public static final String SMTP_SERVER_PASSWORD = "testSmtpPassword";

  private static final int MAX_BUFFER_SIZE_IN_BYTE = 1024;

  private static final double DEFAULT_CHUNKS = 5.0;

  public static Cluster getClusterEntityObj() {
    Cluster clusterEntity = new Cluster();
    clusterEntity.setName(CLUSTER_NAME);
    return clusterEntity;
  }

  public static Pair<Integer, List<Cluster>> getClusterEntitiesObj() {
    List<Cluster> clusterEntityList = new ArrayList<>();
    Cluster clusterEntity = new Cluster();
    clusterEntity.setName(CLUSTER_NAME);
    clusterEntityList.add(clusterEntity);
    Integer totalEntityCount = 1;
    return new Pair<>(totalEntityCount,clusterEntityList);
  }

  public static SnmpConfig getSnmpConfigObj() {
    SnmpConfig snmpConfig = new SnmpConfig();
    snmpConfig.setIsEnabled(true);
    return snmpConfig;
  }

  public static ConfigCredentials getConfigCredentialsObj() {
    ConfigCredentials configCredentials = new ConfigCredentials();

    BasicAuthenticationCredentials smtpCredentials = new BasicAuthenticationCredentials();
    smtpCredentials.setUsername(SMTP_SERVER_USERNAME);
    smtpCredentials.setPassword(SMTP_SERVER_PASSWORD);
    configCredentials.setSmtp(smtpCredentials);

    BasicAuthenticationCredentials httpCredentials = new BasicAuthenticationCredentials();
    httpCredentials.setUsername(HTTP_PROXY_USERNAME);
    httpCredentials.setPassword(HTTP_PROXY_PASSWORD);
    configCredentials.setHttpProxy(httpCredentials);

    SnmpUserCredentials snmpUserCredentials = new SnmpUserCredentials();
    snmpUserCredentials.setUsername(SNMP_USERNAME);
    snmpUserCredentials.setAuthKey(SNMP_AUTH_KEY);
    snmpUserCredentials.setPrivKey(SNMP_PRIV_KEY);
    List<SnmpUserCredentials> snmpUserCredentialsList = new ArrayList<>();
    snmpUserCredentialsList.add(snmpUserCredentials);
    configCredentials.setSnmp(snmpUserCredentialsList);

    return configCredentials;
  }

  public static List<RsyslogServer> getRsyslogConfigObj() {
    List<RsyslogServer> rsyslogServers = new ArrayList<>();
    RsyslogServer rsyslogServer = new RsyslogServer();
    rsyslogServer.setServerName(RSYSLOG_SERVER_NAME);
    rsyslogServers.add(rsyslogServer);
    return rsyslogServers;
  }

  public static List<RackableUnit> getRackableUnitsObj() {
    List<RackableUnit> rackableUnits = new ArrayList<>();
    RackableUnit rackableUnit = new RackableUnit();
    rackableUnit.setExtId(RACKABLE_UNIT_UUID);
    rackableUnits.add(rackableUnit);
    return rackableUnits;
  }

  public static RackableUnit getRackableUnitObj() {
    RackableUnit rackableUnit = new RackableUnit();
    rackableUnit.setExtId(RACKABLE_UNIT_UUID);
    return rackableUnit;
  }

  public static List<DomainFaultTolerance> getDomainFaultToleranceObj() {
    List<DomainFaultTolerance> domainFaultTolerances = new ArrayList<>();
    DomainFaultTolerance domainFaultTolerance = new DomainFaultTolerance();
    domainFaultTolerance.setType(DomainType.CLUSTER);
    domainFaultTolerances.add(domainFaultTolerance);
    return domainFaultTolerances;
  }

  public static InsightsInterfaceProto.GetEntitiesRet getClusterIdfEntityObj(String... clusterUuids) {
    List<String> serviceList = new ArrayList<>();
    serviceList.add("AOS");
    List<String> hypervisorTypes = new ArrayList<>();
    hypervisorTypes.add("kKvm");
    List<String> nameServerIpList = new ArrayList<>();
    nameServerIpList.add("1.2.3.4");
    List<String> ntpServerIpList = new ArrayList<>();
    ntpServerIpList.add("1.2.3.5");

    InsightsInterfaceProto.GetEntitiesRet.Builder getEntitiesRet = InsightsInterfaceProto.GetEntitiesRet.newBuilder();
    Arrays.stream(clusterUuids).forEach(clusterUuid ->{
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("cluster_name")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue(ClusterTestUtils.CLUSTER_NAME))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair2 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("service_list")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrList(StrList.newBuilder().addAllValueList(serviceList)))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair3 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("external_data_services_ip")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("10.10.10.10"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair4 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("external_ip_address")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("10.10.10.10"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair5 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("timezone")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("testTimeZone"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair6 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("hypervisor_types")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrList(StrList.newBuilder().addAllValueList(hypervisorTypes)))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair7 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("redundancy_factor")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setInt64Value(1L))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair8 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("ncc_version")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("testNccVersion")).build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair9 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("cluster_arch")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("X86_64")).build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair10 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("test_name")
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair11 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair12 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("testValue")).build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair13 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("default_case")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("default_value")).build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair14 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("name_server_ip_list")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrList(StrList.newBuilder().addAllValueList(nameServerIpList))
          ).build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair15 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("ntp_server_ip_list")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrList(StrList.newBuilder().addAllValueList(ntpServerIpList))
          ).build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair16 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("smtp_server.server.ip_address")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("1.2.3.4")
          ).build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair17 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("smtp_server.server.port")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setInt64Value(8080)
          ).build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair18 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("smtp_server.server.user_name")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue(SMTP_SERVER_USERNAME)
          ).build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair19 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("smtp_server.email_address")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("abc@nutanix.com")
          ).build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair20 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("smtp_server.type")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("SSL")
          ).build();
      InsightsInterfaceProto.Entity entity = InsightsInterfaceProto.Entity.newBuilder()
        .addAttributeDataMap(nameTimeValuePair)
        .addAttributeDataMap(nameTimeValuePair2)
        .addAttributeDataMap(nameTimeValuePair3)
        .addAttributeDataMap(nameTimeValuePair4)
        .addAttributeDataMap(nameTimeValuePair5)
        .addAttributeDataMap(nameTimeValuePair6)
        .addAttributeDataMap(nameTimeValuePair7)
        .addAttributeDataMap(nameTimeValuePair8)
        .addAttributeDataMap(nameTimeValuePair9)
        .addAttributeDataMap(nameTimeValuePair10)
        .addAttributeDataMap(nameTimeValuePair11)
        .addAttributeDataMap(nameTimeValuePair12)
        .addAttributeDataMap(nameTimeValuePair13)
        .addAttributeDataMap(nameTimeValuePair14)
        .addAttributeDataMap(nameTimeValuePair15)
        .addAttributeDataMap(nameTimeValuePair16)
        .addAttributeDataMap(nameTimeValuePair17)
        .addAttributeDataMap(nameTimeValuePair18)
        .addAttributeDataMap(nameTimeValuePair19)
        .addAttributeDataMap(nameTimeValuePair20)
        .build();

      getEntitiesRet.addEntity(entity);
    });
    return getEntitiesRet.build();
  }

  public static InsightsInterfaceProto.GetEntitiesRet getClusterIdfEntityObjWithNoEntity() {
    InsightsInterfaceProto.GetEntitiesRet.Builder getEntitiesRet = InsightsInterfaceProto.GetEntitiesRet.newBuilder();
    return getEntitiesRet.build();
  }

  public static InsightsInterfaceProto.GetEntitiesWithMetricsRet getClusterEntityWithMetricRetWithNoEntity() {
    InsightsInterfaceProto.GetEntitiesWithMetricsRet.Builder getEntitiesWithMetricRet = InsightsInterfaceProto.GetEntitiesWithMetricsRet.newBuilder();
    return getEntitiesWithMetricRet.build();
  }

  public static InsightsInterfaceProto.GetEntitiesRet getClusterIdfEntityPcObj(String... clusterUuids) {
    List<String> serviceList = new ArrayList<>();
    serviceList.add("PRISM_CENTRAL");
    List<String> hypervisorTypes = new ArrayList<>();
    hypervisorTypes.add("kKvm");

    InsightsInterfaceProto.GetEntitiesRet.Builder getEntitiesRet = InsightsInterfaceProto.GetEntitiesRet.newBuilder();
    Arrays.stream(clusterUuids).forEach(clusterUuid ->{
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("cluster_name")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue(ClusterTestUtils.CLUSTER_NAME))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair2 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("service_list")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrList(StrList.newBuilder().addAllValueList(serviceList)))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair3 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("external_data_services_ip")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("10.10.10.10"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair4 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("external_ip_address")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("10.10.10.10"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair5 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("timezone")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("testTimeZone"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair6 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("hypervisor_types")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrList(StrList.newBuilder().addAllValueList(hypervisorTypes)))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair7 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("redundancy_factor")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setInt64Value(1L))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair9 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("cluster_arch")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("PPC64LE")).build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair10 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("test_name")
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair11 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair12 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("testValue")).build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair13 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("snmp_status")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setInt64Value(1L))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair14 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("default_case")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("default_value")).build();

      InsightsInterfaceProto.Entity entity = InsightsInterfaceProto.Entity.newBuilder()
        .addAttributeDataMap(nameTimeValuePair)
        .addAttributeDataMap(nameTimeValuePair2)
        .addAttributeDataMap(nameTimeValuePair3)
        .addAttributeDataMap(nameTimeValuePair4)
        .addAttributeDataMap(nameTimeValuePair5)
        .addAttributeDataMap(nameTimeValuePair6)
        .addAttributeDataMap(nameTimeValuePair7)
        .addAttributeDataMap(nameTimeValuePair9)
        .addAttributeDataMap(nameTimeValuePair10)
        .addAttributeDataMap(nameTimeValuePair11)
        .addAttributeDataMap(nameTimeValuePair12)
        .addAttributeDataMap(nameTimeValuePair13)
        .addAttributeDataMap(nameTimeValuePair14)
        .build();

      getEntitiesRet.addEntity(entity);
    });
    return getEntitiesRet.build();
  }

  public static InsightsInterfaceProto.GetEntitiesWithMetricsRet buildGetEntitiesWithMetricsRetForCluster() {
    List<String> serviceList = new ArrayList<>();
    serviceList.add("AOS");
    List<String> hypervisorTypes = new ArrayList<>();
    hypervisorTypes.add("kKvm");
    List<String> nameServerIpList = new ArrayList<>();
    nameServerIpList.add("1.2.3.4");
    List<String> ntpServerIpList = new ArrayList<>();
    ntpServerIpList.add("1.2.3.4");
    List<String> encryptionScope = new ArrayList<>();
    List<String> encryptionOption = new ArrayList<>();
    encryptionScope.add("cluster");
    encryptionOption.add("HW");
    InsightsInterfaceProto.MetricData metric1 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("cluster_name")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue(ClusterTestUtils.CLUSTER_NAME)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric2 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("service_list")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder().setStrList(StrList.newBuilder().addAllValueList(serviceList))
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric3 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("external_data_services_ip")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("10.10.10.10")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric4 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("timezone")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("testTimeZone")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric5 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("hypervisor_types")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder().setStrList(StrList.newBuilder().addAllValueList(hypervisorTypes))
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric6 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("redundancy_factor")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setInt64Value(1L)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric7 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("ncc_version")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("testNccVersion")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric8 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("cluster_arch")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("X86_64")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric9 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("test_name")
        .build();
    InsightsInterfaceProto.MetricData metric10 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .build();
    InsightsInterfaceProto.MetricData metric11 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("X86_64")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric12 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("default_case")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("default_value")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric13 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("cluster_upgrade_status")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("SUCCEEDED")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric14 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("key_management_server")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("LOCAL")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric15 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("cluster_profile_uuid")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue(ClusterTestUtils.CLUSTER_PROFILE_UUID)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric16 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("cluster_profile_name")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue(ClusterTestUtils.CLUSTER_PROFILE_NAME)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric17 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("is_available")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setBoolValue(true)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric18 =
            InsightsInterfaceProto.MetricData.newBuilder()
                    .setName("backup_eligibility_score")
                    .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
                            .setValue(InsightsInterfaceProto.DataValue.newBuilder()
                                    .setInt64Value(2)
                                    .build())
                            .build())
                    .build();
    InsightsInterfaceProto.MetricData metric19 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("num_vms")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setInt64Value(1)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric20 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("capacity.inefficient_vm_num")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setInt64Value(1)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric21 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("encryption_option")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder().setStrList(StrList.newBuilder().addAllValueList(encryptionOption))
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric22 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("encryption_scope")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder().setStrList(StrList.newBuilder().addAllValueList(encryptionScope))
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric23 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("encryption_in_transit")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("test_in_transit")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric24 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("name_server_ip_list")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder().setStrList(StrList.newBuilder().addAllValueList(nameServerIpList))
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric25 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("ntp_server_ip_list")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder().setStrList(StrList.newBuilder().addAllValueList(ntpServerIpList))
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric26 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("smtp_server.server.ip_address")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("1.2.3.4")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric27 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("smtp_server.server.user_name")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue(ClusterTestUtils.SMTP_SERVER_USERNAME)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric28 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("smtp_server.email_address")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("abc@nutanix.com")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric29 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("smtp_server.type")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("SSL")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric30 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("mtp_server.server.port")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setInt64Value(9090)
            .build())
          .build())
        .build();
    return InsightsInterfaceProto.GetEntitiesWithMetricsRet.newBuilder().setTotalGroupCount(1)
      .addGroupResultsList(InsightsInterfaceProto.QueryGroupResult.newBuilder().
        addRawResults(
          InsightsInterfaceProto.EntityWithMetric.newBuilder()
            .setEntityGuid(
              InsightsInterfaceProto.EntityGuid.newBuilder().
                setEntityId(CLUSTER_UUID).
                setEntityTypeName("cluster").
                build())
            .addMetricDataList(metric1)
            .addMetricDataList(metric2)
            .addMetricDataList(metric3)
            .addMetricDataList(metric4)
            .addMetricDataList(metric5)
            .addMetricDataList(metric6)
            .addMetricDataList(metric7)
            .addMetricDataList(metric8)
            .addMetricDataList(metric9)
            .addMetricDataList(metric10)
            .addMetricDataList(metric11)
            .addMetricDataList(metric12)
            .addMetricDataList(metric13)
            .addMetricDataList(metric14)
            .addMetricDataList(metric15)
            .addMetricDataList(metric16)
            .addMetricDataList(metric17)
            .addMetricDataList(metric18)
            .addMetricDataList(metric19)
            .addMetricDataList(metric20)
            .addMetricDataList(metric21)
            .addMetricDataList(metric22)
            .addMetricDataList(metric23)
            .addMetricDataList(metric24)
            .addMetricDataList(metric25)
            .addMetricDataList(metric26)
            .addMetricDataList(metric27)
            .addMetricDataList(metric28)
            .addMetricDataList(metric29)
            .addMetricDataList(metric30)
        ).setTotalEntityCount(1).build()).build();
  }

  public static InsightsInterfaceProto.GetEntitiesWithMetricsRet buildGetEntitiesWithMetricsRetForNonMigratableVms() {
    String nonMigratableVmsListString = "[{\"name\": \"pinned_vm_test\", \"uuid\": \"53af3b7f-a67b-429f-a73c-972558c4a2ca\", \"reason\": \"Some VM \\\"pinned_vm_test\\\" on host \'xyz\' cannot be migrated. Also testing \\\"qoutes in end\\\"\"}]";
    InsightsInterfaceProto.MetricData metric1 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("non_migratable_vms_list")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue(nonMigratableVmsListString)
            .build())
          .build())
        .build();
    return InsightsInterfaceProto.GetEntitiesWithMetricsRet.newBuilder().setTotalGroupCount(1)
    .addGroupResultsList(InsightsInterfaceProto.QueryGroupResult.newBuilder().
      addRawResults(
        InsightsInterfaceProto.EntityWithMetric.newBuilder()
          .setEntityGuid(
            InsightsInterfaceProto.EntityGuid.newBuilder().
              setEntityId(CLUSTER_UUID).
              setEntityTypeName("cluster").
              build())
          .addMetricDataList(metric1)
      ).setTotalEntityCount(1).build()).build();
  }
  
  public static InsightsInterfaceProto.GetEntitiesWithMetricsRet buildGetEntitiesWithMetricsRetForClusterForPC() {
    List<String> serviceList = new ArrayList<>();
    serviceList.add("PRISM_CENTRAL");
    List<String> hypervisorTypes = new ArrayList<>();
    hypervisorTypes.add("kKvm");

    InsightsInterfaceProto.MetricData metric1 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("cluster_name")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue(ClusterTestUtils.CLUSTER_NAME)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric2 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("service_list")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder().setStrList(StrList.newBuilder().addAllValueList(serviceList))
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric3 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("external_data_services_ip")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("10.10.10.10")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric4 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("timezone")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("testTimeZone")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric5 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("hypervisor_types")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder().setStrList(StrList.newBuilder().addAllValueList(hypervisorTypes))
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric6 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("redundancy_factor")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setInt64Value(1L)
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric7 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("ncc_version")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("testNccVersion")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric8 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("cluster_arch")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("PPC64LE")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric9 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("test_name")
        .build();
    InsightsInterfaceProto.MetricData metric10 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .build();
    InsightsInterfaceProto.MetricData metric11 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("X86_64")
            .build())
          .build())
        .build();
    InsightsInterfaceProto.MetricData metric12 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("default_case")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("default_value")
            .build())
          .build())
        .build();

    return InsightsInterfaceProto.GetEntitiesWithMetricsRet.newBuilder().setTotalGroupCount(1)
      .addGroupResultsList(InsightsInterfaceProto.QueryGroupResult.newBuilder().
        addRawResults(
          InsightsInterfaceProto.EntityWithMetric.newBuilder()
            .setEntityGuid(
              InsightsInterfaceProto.EntityGuid.newBuilder().
                setEntityId(CLUSTER_UUID).
                setEntityTypeName("cluster").
                build())
            .addMetricDataList(metric1)
            .addMetricDataList(metric2)
            .addMetricDataList(metric3)
            .addMetricDataList(metric4)
            .addMetricDataList(metric5)
            .addMetricDataList(metric6)
            .addMetricDataList(metric7)
            .addMetricDataList(metric8)
            .addMetricDataList(metric9)
            .addMetricDataList(metric10)
            .addMetricDataList(metric11)
            .addMetricDataList(metric12)
        ).setTotalEntityCount(1).build()).build();
  }

  public static InsightsInterfaceProto.GetEntitiesWithMetricsRet buildGetEntitiesWithMetricsRetForManagementServer(String... clusterUuids) {
    InsightsInterfaceProto.QueryGroupResult.Builder queryGroupResult = InsightsInterfaceProto.QueryGroupResult.newBuilder().setTotalEntityCount(5);
    Arrays.stream(clusterUuids).forEach(clusterUuid -> {
      InsightsInterfaceProto.MetricData address =
        InsightsInterfaceProto.MetricData.newBuilder()
          .setName("address")
          .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
            .setValue(InsightsInterfaceProto.DataValue.newBuilder()
              .setStrValue("10.10.10.10")
              .build())
            .build())
          .build();
      InsightsInterfaceProto.MetricData extensionKey =
        InsightsInterfaceProto.MetricData.newBuilder()
          .setName("extension_key")
          .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
            .setValue(InsightsInterfaceProto.DataValue.newBuilder()
              .setStrValue("test_extension_key")
              .build())
            .build())
          .build();
      InsightsInterfaceProto.MetricData managementServerType =
        InsightsInterfaceProto.MetricData.newBuilder()
          .setName("management_server_type")
          .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
            .setValue(InsightsInterfaceProto.DataValue.newBuilder()
              .setStrValue("vcenter")
              .build())
            .build())
          .build();
      InsightsInterfaceProto.MetricData inUse =
        InsightsInterfaceProto.MetricData.newBuilder()
          .setName("in_use")
          .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
            .setValue(InsightsInterfaceProto.DataValue.newBuilder()
              .setBoolValue(true)
              .build())
            .build())
          .build();
      InsightsInterfaceProto.MetricData drsEnabled =
        InsightsInterfaceProto.MetricData.newBuilder()
          .setName("drs_enabled")
          .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
            .setValue(InsightsInterfaceProto.DataValue.newBuilder()
              .setBoolValue(true)
              .build())
            .build())
          .build();
      InsightsInterfaceProto.MetricData defaultCase =
        InsightsInterfaceProto.MetricData.newBuilder()
          .setName("default")
          .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
            .setValue(InsightsInterfaceProto.DataValue.newBuilder()
              .setBoolValue(true)
              .build())
            .build())
          .build();

      InsightsInterfaceProto.EntityWithMetric entityWithMetric =
        InsightsInterfaceProto.EntityWithMetric.newBuilder()
          .addMetricDataList(address)
          .addMetricDataList(extensionKey)
          .addMetricDataList(managementServerType)
          .addMetricDataList(inUse)
          .addMetricDataList(drsEnabled)
          .addMetricDataList(defaultCase)
          .build();
      queryGroupResult.addRawResults(entityWithMetric);
    });
    return InsightsInterfaceProto.GetEntitiesWithMetricsRet.newBuilder().addGroupResultsList(queryGroupResult).build();
  }

  public static ZeusConfiguration buildZeusConfiguration() {
    Configuration.ConfigurationProto.Builder cfgBuilder =
      Configuration.ConfigurationProto.newBuilder()
        .setLogicalTimestamp(1L);

    Configuration.ConfigurationProto.Node.Builder nodeBuilder =
      Configuration.ConfigurationProto.Node.newBuilder()
        .setServiceVmExternalIp(CVM_IPV4)
        .setServiceVmId(1L)
        .setControllerVmBackplaneIp(CVM_IPV4)
        .setHypervisorKey(HOST_IPV4);

    Configuration.ConfigurationProto.SSHKey.Builder sshKey =
      Configuration.ConfigurationProto.SSHKey.newBuilder()
      .setPubKey("testPubKey")
      .setKeyId("testKeyId");

    Configuration.ConfigurationProto.NetworkEntity.Builder smtp =
      Configuration.ConfigurationProto.NetworkEntity.newBuilder()
      .addAddressList("10.10.10.10")
      .setPort(8080)
      .setUsername("testUsername")
      .setPassword("testPassword");

    Configuration.ConfigurationProto.FaultToleranceState faultToleranceState =
      Configuration.ConfigurationProto.FaultToleranceState.newBuilder()
      .setCurrentMaxFaultTolerance(1)
      .setDesiredMaxFaultTolerance(2)
      .setDesiredFaultToleranceLevel(DomainFaultToleranceStateProto.DomainFaultToleranceState.Domain.DomainType.kCluster)
      .setDesiredReplicaPlacementPolicyId(5)
      .setCurrentReplicaPlacementPolicyId(2)
      .build();

    Configuration.ConfigurationProto.Aegis.Builder aegis =
      Configuration.ConfigurationProto.Aegis.newBuilder()
      .setSmtpServer(smtp)
      .setFromAddress("10.10.10.10")
      .setSmtpServerType(Configuration.ConfigurationProto.Aegis.SmtpServerType.kSSL)
      .setRemoteSupport(Configuration.ConfigurationProto.Aegis.TimedBool.newBuilder().setValue(true).build());

    Configuration.ConfigurationProto.NetworkConfig backPlaneNetworkConfig =
      Configuration.ConfigurationProto.NetworkConfig.newBuilder()
          .setVlanId(10)
          .setSubnet("1.1.1.1/2.2.2.2")
          .build();

    cfgBuilder.addNodeList(nodeBuilder);
    cfgBuilder.setReleaseVersion("el7.3-release-fraser-2024.1-stable-00234ddab4a0908723459aae50403d21ace6f6e2");
    cfgBuilder.addSshKeyList(sshKey);
    cfgBuilder.setExternalSubnet("255.0.0.0");
    cfgBuilder.setInternalSubnet("255.0.0.1");
    cfgBuilder.setNfsSubnetWhitelist("255.0.0.0,255.0.0.1");
    cfgBuilder.addNtpServerList("10.10.10.10");
    cfgBuilder.addNameServerIpList("10.10.10.10");
    cfgBuilder.setAegis(aegis);
    cfgBuilder.setClusterMasqueradingIp("10.10.10.10");
    cfgBuilder.setClusterMasqueradingPort(8080);
    cfgBuilder.setPasswordRemoteLoginEnabled(true);
    cfgBuilder.setClusterOperationMode(Configuration.ConfigurationProto.OperationMode.kNormal);
    cfgBuilder.setIsLts(true);
    cfgBuilder.setClusterFaultToleranceState(faultToleranceState);
    cfgBuilder.setClusterExternalIp("10.10.10.10");
    cfgBuilder.setClusterExternalIpv6("10.10.10.10.10.10");
    cfgBuilder.setClusterIncarnationId(1598250612223890L);
    cfgBuilder.setBackplaneNetworkConfig(backPlaneNetworkConfig);
    return new ZeusConfigurationImpl.Builder(cfgBuilder.build()).build();
  }

  public static ZeusConfiguration buildZeusConfigurationWithOtherConditions() {
    Configuration.ConfigurationProto.Builder cfgBuilder =
      Configuration.ConfigurationProto.newBuilder()
        .setLogicalTimestamp(1L);

    Configuration.ConfigurationProto.NetworkEntity.Builder network =
      Configuration.ConfigurationProto.NetworkEntity.newBuilder()
        .addAddressList("10.10.10.10");

    Configuration.ConfigurationProto.Node.Builder nodeBuilder =
      Configuration.ConfigurationProto.Node.newBuilder()
        .addSvmExternalIpList(CVM_IPV4)
        .setServiceVmId(1L)
        .setControllerVmBackplaneIp(CVM_IPV4)
        .setHypervisor(network);

    Configuration.ConfigurationProto.SSHKey.Builder sshKey =
      Configuration.ConfigurationProto.SSHKey.newBuilder()
        .setPubKey("testPubKey")
        .setKeyId("testKeyId");

    Configuration.ConfigurationProto.FaultToleranceState.Builder fault =
      Configuration.ConfigurationProto.FaultToleranceState.newBuilder();

    Configuration.ConfigurationProto.Aegis.Builder aegis =
      Configuration.ConfigurationProto.Aegis.newBuilder()
        .setFromAddress("10.10.10.10");

    cfgBuilder.addNodeList(nodeBuilder);
    cfgBuilder.addSshKeyList(sshKey);
    cfgBuilder.setClusterFaultToleranceState(fault);
    cfgBuilder.setAegis(aegis);
    return new ZeusConfigurationImpl.Builder(cfgBuilder.build()).build();
  }

  public static ZeusConfiguration buildZeusConfigurationWithSnmpInfo() {
    Configuration.ConfigurationProto.Builder cfgBuilder =
      Configuration.ConfigurationProto.newBuilder()
        .setLogicalTimestamp(1L);

    // USER
    Configuration.ConfigurationProto.SnmpInfo.User snmpUser =
      Configuration.ConfigurationProto.SnmpInfo.User.newBuilder()
      .setUsername(SNMP_USERNAME)
      .setUuid(SNMP_USER_UUID)
      .setAuthKey("testAuthKey")
      .setPrivKey("testPrivKey")
      .setAuthType(Configuration.ConfigurationProto.SnmpInfo.User.AuthType.kSHA)
      .setPrivType(Configuration.ConfigurationProto.SnmpInfo.User.PrivType.kAES).build();

    // Transport
    Configuration.ConfigurationProto.SnmpInfo.Transport snmpTransport =
      Configuration.ConfigurationProto.SnmpInfo.Transport.newBuilder()
      .setPort(8080)
      .setProtocol(Configuration.ConfigurationProto.SnmpInfo.Protocol.kTCP).build();

    // Network Entity
    Configuration.ConfigurationProto.NetworkEntity.Builder server =
      Configuration.ConfigurationProto.NetworkEntity.newBuilder()
        .addAddressList("10.10.10.10")
        .setPort(8080);

    // Trap
    Configuration.ConfigurationProto.SnmpInfo.TrapSink snmpTrap =
      Configuration.ConfigurationProto.SnmpInfo.TrapSink.newBuilder()
      .setTrapEngineId("testEngineId")
      .setUuid(SNMP_TRAP_UUID)
      .setProtocol(Configuration.ConfigurationProto.SnmpInfo.Protocol.kTCP)
      .setIsInform(true)
      .setReceiverName("testRecieverName")
      .setTrapUsername(SNMP_USERNAME)
      .setTrapAddress(server)
      .setVersion(Configuration.ConfigurationProto.SnmpInfo.SnmpVersion.kV2)
      .setCommunityString("Plain").build();

    Configuration.ConfigurationProto.SnmpInfo.Builder snmpBuilder =
      Configuration.ConfigurationProto.SnmpInfo.newBuilder()
      .setEnabled(true)
      .addUserList(snmpUser)
      .addTransportList(snmpTransport)
      .addTrapSinkList(snmpTrap);

    cfgBuilder.setSnmpInfo(snmpBuilder);
    return new ZeusConfigurationImpl.Builder(cfgBuilder.build()).build();
  }

  public static ZeusConfiguration buildZeusConfigurationWithSnmpInfoWithConditions() {
    Configuration.ConfigurationProto.Builder cfgBuilder =
      Configuration.ConfigurationProto.newBuilder()
        .setLogicalTimestamp(1L);

    // USER
    Configuration.ConfigurationProto.SnmpInfo.User snmpUser =
      Configuration.ConfigurationProto.SnmpInfo.User.newBuilder()
        .setUsername("test").build();

    // Transport
    Configuration.ConfigurationProto.SnmpInfo.Transport snmpTransport =
      Configuration.ConfigurationProto.SnmpInfo.Transport.newBuilder()
        .setPort(8080).build();

    // Trap
    Configuration.ConfigurationProto.SnmpInfo.TrapSink snmpTrap =
      Configuration.ConfigurationProto.SnmpInfo.TrapSink.newBuilder()
        .setVersion(Configuration.ConfigurationProto.SnmpInfo.SnmpVersion.kV2).build();

    Configuration.ConfigurationProto.SnmpInfo.Builder snmpBuilder =
      Configuration.ConfigurationProto.SnmpInfo.newBuilder()
        .setEnabled(false)
        .addUserList(snmpUser)
        .addTransportList(snmpTransport)
        .addTrapSinkList(snmpTrap);

    cfgBuilder.setSnmpInfo(snmpBuilder);
    return new ZeusConfigurationImpl.Builder(cfgBuilder.build()).build();
  }

  public static ZeusConfiguration buildZeusConfigurationWithRsylogConfig() {
    Configuration.ConfigurationProto.Builder cfgBuilder =
      Configuration.ConfigurationProto.newBuilder()
        .setLogicalTimestamp(1L);

    // Network Entity
    Configuration.ConfigurationProto.NetworkEntity.Builder server =
      Configuration.ConfigurationProto.NetworkEntity.newBuilder()
        .addAddressList("10.10.10.10")
        .setProtocol(Configuration.ConfigurationProto.NetworkEntity.ProtocolType.kTcp)
        .setPort(8080);

    // Module
    Configuration.ConfigurationProto.RSyslogConfig.LogServerConfig.Module moduele =
      Configuration.ConfigurationProto.RSyslogConfig.LogServerConfig.Module.newBuilder()
      .setName(RSYSLOG_MODULE_NAME)
      .setPriority(Configuration.ConfigurationProto.RSyslogConfig.LogServerConfig.Module.Priority.kInfo)
      .setMonitor(true).build();

    // LogServerConfig
    Configuration.ConfigurationProto.RSyslogConfig.LogServerConfig logServerConfig =
      Configuration.ConfigurationProto.RSyslogConfig.LogServerConfig.newBuilder()
      .setName(ClusterTestUtils.RSYSLOG_SERVER_NAME)
      .setUuid(ClusterTestUtils.RSYSLOG_SERVER_UUID)
      .addModules(moduele)
      .setServer(server).build();

    // Rsyslog Config
    Configuration.ConfigurationProto.RSyslogConfig.Builder builder =
      Configuration.ConfigurationProto.RSyslogConfig.newBuilder()
      .addLogServerConfigList(logServerConfig);

    cfgBuilder.setRsyslogConfig(builder);
    return new ZeusConfigurationImpl.Builder(cfgBuilder.build()).build();
  }

  public static ZeusConfiguration buildZeusConfigurationWithRsylogConfigWithConditions() {
    Configuration.ConfigurationProto.Builder cfgBuilder =
      Configuration.ConfigurationProto.newBuilder()
        .setLogicalTimestamp(1L);

    // Module
    Configuration.ConfigurationProto.RSyslogConfig.LogServerConfig.Module moduele =
      Configuration.ConfigurationProto.RSyslogConfig.LogServerConfig.Module.newBuilder()
        .setName(RSYSLOG_MODULE_NAME)
        .setPriority(Configuration.ConfigurationProto.RSyslogConfig.LogServerConfig.Module.Priority.kInfo)
        .setMonitor(true).build();

    // LogServerConfig
    Configuration.ConfigurationProto.RSyslogConfig.LogServerConfig logServerConfig =
      Configuration.ConfigurationProto.RSyslogConfig.LogServerConfig.newBuilder()
        .addModules(moduele).build();

    // Rsyslog Config
    Configuration.ConfigurationProto.RSyslogConfig.Builder builder =
      Configuration.ConfigurationProto.RSyslogConfig.newBuilder()
        .addLogServerConfigList(logServerConfig);

    cfgBuilder.setRsyslogConfig(builder);
    return new ZeusConfigurationImpl.Builder(cfgBuilder.build()).build();
  }

  public static ZeusConfiguration buildZeusConfigurationWithRackableUnit() {
    Configuration.ConfigurationProto.Builder cfgBuilder =
      Configuration.ConfigurationProto.newBuilder()
        .setLogicalTimestamp(1L);

    // Node
    Configuration.ConfigurationProto.Node.Builder node =
      Configuration.ConfigurationProto.Node.newBuilder()
      .setRackableUnitId(RACKABLE_UNIT_ID)
      .setServiceVmId(2)
      .setUuid(CLUSTER_UUID)
      .setNodePosition(2);

    // Rackbale Unit
    Configuration.ConfigurationProto.RackableUnit.Builder builder =
      Configuration.ConfigurationProto.RackableUnit.newBuilder()
      .setRackableUnitId(RACKABLE_UNIT_ID)
      .setRackableUnitModel(Configuration.ConfigurationProto.RackableUnit.Model.kDesktop)
      .setRackableUnitUuid(RACKABLE_UNIT_UUID)
      .setRackableUnitModelName("testModelName")
      .setRackableUnitSerial("10-10-10-10")
      .setRackUuid(RACKABLE_UNIT_UUID)
      .setRackId(1L);

    cfgBuilder.addRackableUnitList(builder);
    cfgBuilder.addNodeList(node);
    return new ZeusConfigurationImpl.Builder(cfgBuilder.build()).build();
  }

  public static ZeusConfiguration buildZeusConfigurationWithRackableUnitWithConditions() {
    Configuration.ConfigurationProto.Builder cfgBuilder =
      Configuration.ConfigurationProto.newBuilder()
        .setLogicalTimestamp(1L);

    // Node
    Configuration.ConfigurationProto.Node.Builder node =
      Configuration.ConfigurationProto.Node.newBuilder()
        .setRackableUnitId(1)
        .setServiceVmId(2)
        .setUuid(CLUSTER_UUID)
        .setNodePosition(2);

    // Rackbale Unit
    Configuration.ConfigurationProto.RackableUnit.Builder builder =
      Configuration.ConfigurationProto.RackableUnit.newBuilder()
        .setRackableUnitId(RACKABLE_UNIT_ID)
        .setRackableUnitUuid(RACKABLE_UNIT_UUID);

    cfgBuilder.addRackableUnitList(builder);
    cfgBuilder.addNodeList(node);
    return new ZeusConfigurationImpl.Builder(cfgBuilder.build()).build();
  }

  public static ZeusConfiguration buildZeusConfigurationWithDomainFaultStatus() {
    Configuration.ConfigurationProto.Builder cfgBuilder =
      Configuration.ConfigurationProto.newBuilder()
        .setLogicalTimestamp(1L);

    MessageEntityProto.MessageEntity.AttributeList attributeList = MessageEntityProto.MessageEntity.AttributeList.newBuilder()
      .setAttribute("test")
      .setValue("test").build();

    MessageEntityProto.MessageEntity messageEntity = MessageEntityProto.MessageEntity.newBuilder()
      .setMessageId("test")
      .addAttributeList(attributeList).build();

    DomainFaultToleranceStateProto.DomainFaultToleranceState.Domain.Component component =
      DomainFaultToleranceStateProto.DomainFaultToleranceState.Domain.Component.newBuilder()
      .setComponentType(DomainFaultToleranceStateProto.DomainFaultToleranceState.Domain.Component.ComponentType.kStaticConfig)
      .setMaxFaultsTolerated(1)
      .setLastUpdateSecs(1)
      .setUnderComputation(true)
      .setToleranceDetailsMessage(messageEntity).build();

    DomainFaultToleranceStateProto.DomainFaultToleranceState.Domain.Builder domain =
      DomainFaultToleranceStateProto.DomainFaultToleranceState.Domain.newBuilder()
      .setDomainType(DomainFaultToleranceStateProto.DomainFaultToleranceState.Domain.DomainType.kCluster)
      .addComponents(component);

    DomainFaultToleranceStateProto.DomainFaultToleranceState domainFaultToleranceState =
      DomainFaultToleranceStateProto.DomainFaultToleranceState.newBuilder()
      .addDomains(domain).build();

    cfgBuilder.setDomainFaultToleranceState(domainFaultToleranceState);
    return new ZeusConfigurationImpl.Builder(cfgBuilder.build()).build();
  }

  public static TaskReference getTask() {
    TaskReference task = new TaskReference();
    task.setExtId(TASK_UUID);
    return task;
  }

  public static ErgonInterface.TaskCreateRet getCreateTaskRet() {
    ErgonInterface.TaskCreateRet taskCreateRet = ErgonInterface.TaskCreateRet.newBuilder()
      .setExtId(TASK_UUID)
      .setUuid(UuidUtils.getByteStringFromUUID(TASK_UUID)).build();
    return taskCreateRet;
  }

  public static ErgonInterface.TaskUpdateRet getUpdateTaskRet() {
    ErgonInterface.TaskUpdateRet taskUpdateRet = ErgonInterface.TaskUpdateRet.newBuilder().build();
    return taskUpdateRet;
  }

  public static ErgonInterface.TaskGetRet getTaskRet() {
    ErgonTypes.KeyValue keyValue = ErgonTypes.KeyValue.newBuilder()
      .setKey("search_type")
      .setStrData(SearchType.UNCONFIGURED_NODES.fromEnum()).build();
    ErgonTypes.Task task = ErgonTypes.Task.newBuilder().
      setLogicalTimestamp(1L)
      .addSubtaskUuidList(UuidUtils.getByteStringFromUUID(CHILD_TASK_UUID))
      .setStatus(ErgonTypes.Task.Status.kSucceeded)
      .build();
    ErgonInterface.TaskGetRet taskCreateRet = ErgonInterface.TaskGetRet.newBuilder()
      .addTaskList(task).build();
    return taskCreateRet;
  }

  public static ErgonInterface.TaskGetRet getSearchRet(SearchType searchType){
    String jsonString = null;
    switch (searchType) {
      case UNCONFIGURED_NODES:
        jsonString = "{\"discovered_nodes\": [{\"foundation_version\": \"foundation-4.5.4.2-94510908\", \"rackable_unit_serial\": \"15SM13430121\", \"node_uuid\": \"b62a7103-77c7-4f53-a863-f443d3e27f5e\", \"rackable_unit_max_nodes\": 4, \"current_network_interface\": \"eth0\", \"node_position\": \"D\", \"ip\": \"10.40.145.104\", \"svm_ip\": \"10.40.145.104\", \"current_cvm_vlan_tag\": null, \"is_secure_booted\": false, \"nos_version\": \"5.18.1.2\", \"cluster_id\": \"\", \"cpu_type\": [\"Intel\", \"62\"], \"hypervisor\": \"kvm\", \"hypervisor_version\": \"el7.nutanix.20201105.1001814\", \"attributes\": {\"lcm_family\": \"smc_gen_9\", \"maybe_1GbE_only\": true, \"is_model_supported\": true, \"robo_mixed_hypervisor\": true}, \"rackable_unit_model\": \"NX-1065S\", \"arch\": \"x86_64\", \"cvm_ipv6\": \"10.40.145.104\", \"hypervisor_ip\": \"10.40.145.104\", \"hypervisor_ipv6\": \"10.40.145.104\", \"ipmi_ip\": \"10.40.145.104\", \"ipmi_ipv6\": \"10.40.145.104\"}]}";
        break;
      case NETWORKING_DETAILS:
        jsonString = "{\"network_info\": {\"HCI\": [{\"hypervisor_type\": 3, \"name\": \"br0\", \"networks\": [\"Management\"]}], \"SO\": []}, \"uplinks\": {\"10.47.240.250\": [{\"mac\": \"0c:c4:7a:bc:73:97\", \"name\": \"eth3\"}, {\"mac\": \"0c:c4:7a:bc:73:96\", \"name\": \"eth2\"}, {\"mac\": \"0c:c4:7a:64:cc:d9\", \"name\": \"eth1\"}, {\"mac\": \"0c:c4:7a:64:cc:d8\", \"name\": \"eth0\"}]}, \"warnings\": []}";
        break;
      case HYPERVISOR_UPLOAD_INFO:
        jsonString = "{\"msg\": null, \"node_list\": [{\"hypervisor_upload_required\": true, \"is_imaging_mandatory\": false, \"node_uuid\": \"803ae457-320a-41c6-ad89-480c08fc825b\", \"available_hypervisor_iso_error\": null, \"required_hypervisor\": \"kvm\", \"is_node_compatible\": true, \"hypervisor_md5sum\": \"test-md5sum\"}]}";
        break;
      case NON_COMPATIBLE_CLUSTERS:
        jsonString = "{\"000612d3-2598-748a-591c-ac1f6b18ef5e\":[\"NTP_SERVER\"]}";
        break;

    }
    ErgonTypes.KeyValue keyValue = ErgonTypes.KeyValue.newBuilder()
      .setKey("search_type")
      .setStrData(searchType.fromEnum()).build();
    try {
      JsonNode jsonObject = JsonUtils.getObjectMapper().readTree(jsonString);
      ErgonTypes.PayloadOrEmbeddedValue payloadOrEmbeddedValue = ErgonTypes.PayloadOrEmbeddedValue.newBuilder().
        setEmbedded(ByteString.copyFromUtf8(jsonObject.toString())).build();
      ErgonTypes.MetaResponse metaResponse = ErgonTypes.MetaResponse.newBuilder().
        setRet(payloadOrEmbeddedValue).build();
      ErgonTypes.Task task = ErgonTypes.Task.newBuilder().setLogicalTimestamp(1L)
        .addCompletionDetails(0, keyValue)
        .setResponse(metaResponse).build();
      ErgonInterface.TaskGetRet taskCreateRet = ErgonInterface.TaskGetRet.newBuilder()
        .addTaskList(task).build();
      return taskCreateRet;
    }
    catch (Exception e) {
      return ErgonInterface.TaskGetRet.newBuilder().build();
    }
  }

  public static ErgonInterface.TaskGetRet getSearchRetWithEmptyResponse(SearchType searchType) {
    String jsonString = null;
    switch (searchType) {
      case UNCONFIGURED_NODES:
        jsonString = "{\"discovered_nodes\": [{\"attributes\": {}}]}";
        break;
      case NETWORKING_DETAILS:
        jsonString = "{\"network_info\": {\"HCI\": [], \"SO\": [{\"hypervisor_type\": 3, \"name\": \"br0\", \"networks\": [\"Management\"]}]}, \"uplinks\": {\"10.47.240.250\": []}}";
        break;
      case HYPERVISOR_UPLOAD_INFO:
        jsonString = "{\"msg\": null, \"node_list\": []}";
        break;
      case NON_COMPATIBLE_CLUSTERS:
        jsonString = "{}";
    }
    try {
      ErgonTypes.KeyValue keyValue = ErgonTypes.KeyValue.newBuilder()
        .setKey("search_type")
        .setStrData(searchType.fromEnum()).build();
      JsonNode jsonObject = JsonUtils.getObjectMapper().readTree(jsonString);
      ErgonTypes.PayloadOrEmbeddedValue payloadOrEmbeddedValue = ErgonTypes.PayloadOrEmbeddedValue.newBuilder().
        setEmbedded(ByteString.copyFromUtf8(jsonObject.toString())).build();
      ErgonTypes.MetaResponse metaResponse = ErgonTypes.MetaResponse.newBuilder().
        setRet(payloadOrEmbeddedValue).build();
      ErgonTypes.Task task = ErgonTypes.Task.newBuilder().setLogicalTimestamp(1L)
        .setResponse(metaResponse)
        .addCompletionDetails(keyValue).build();
      ErgonInterface.TaskGetRet taskCreateRet = ErgonInterface.TaskGetRet.newBuilder()
        .addTaskList(task).build();
      return taskCreateRet;
    }
    catch(Exception e) {
      return ErgonInterface.TaskGetRet.newBuilder().build();
    }
  }

  /**
   * Computer a smaller input buffer size to read the input string to avoid
   * unnecessarily allocate extra memory in case of small input string.
   *
   * @param totalBytes total number of byte in input string
   * @return buffer size in byte for reading input string
   */
  private static int computeBufferSize(int totalBytes) {
    return Math.min((int) Math.ceil(totalBytes / DEFAULT_CHUNKS + 1),
      MAX_BUFFER_SIZE_IN_BYTE);
  }

  /**
   * Compress the byte string.
   *
   * @param inputString byte string
   * @return zipped ByteString or empty ByteString in error case
   */
  @Nullable
  public static ByteString compress(final ByteString inputString) {
    Deflater compressor = new Deflater();

    int inputBufferSize = computeBufferSize(inputString.size());
    byte[] result = new byte[inputBufferSize];
    byte[] inputBuffer = new byte[inputBufferSize];
    try (InputStream inputStream = inputString.newInput();
         ByteString.Output output = ByteString.newOutput(
           inputStream.available())) {
      int bytesRead;
      while ((bytesRead = inputStream.read(inputBuffer)) != -1) {
        compressor.setInput(inputBuffer, 0, bytesRead);
        while (!compressor.needsInput()) {
          int compressedDataLength = compressor.deflate(result);
          output.write(result, 0, compressedDataLength);
        }
      }
      compressor.finish();
      while (!compressor.finished()) {
        int compressedDataLength = compressor.deflate(result);
        output.write(result, 0, compressedDataLength);
      }
      compressor.end();
      return output.toByteString();
    } catch (IOException ex) {
      log.error("Error in compressing input", ex);
      return null;
    }
  }

  public static RsyslogServer getRsyslogServer() {
    RsyslogServer rsyslogServer = new RsyslogServer();
    rsyslogServer.setServerName(RSYSLOG_SERVER_NAME);
    rsyslogServer.setExtId(RSYSLOG_SERVER_UUID);
    rsyslogServer.setIpAddress(createIpAddress("10.10.10.10",null));
    rsyslogServer.setPort(8080);
    rsyslogServer.setNetworkProtocol(RsyslogNetworkProtocol.TCP);
    List<RsyslogModuleItem> rsyslogModuleItems = new ArrayList<>();
    RsyslogModuleItem rsyslogModuleItem = new RsyslogModuleItem();
    rsyslogModuleItem.setName(RsyslogModuleName.ACROPOLIS);
    rsyslogModuleItem.setShouldLogMonitorFiles(true);
    rsyslogModuleItem.setLogSeverityLevel(RsyslogModuleLogSeverityLevel.ALERT);
    rsyslogModuleItems.add(rsyslogModuleItem);
    rsyslogServer.setModules(rsyslogModuleItems);
    return rsyslogServer;
  }

  public static InsightsInterfaceProto.GetEntitiesWithMetricsRet getRemoteSyslogConfigurationIdfEntitiesObj(String remoteSyslogServerUuid) {

    GenesisInterfaceProto.RemoteSyslogConfiguration.SyslogModule syslogModule =
      GenesisInterfaceProto.RemoteSyslogConfiguration.SyslogModule.newBuilder()
        .setModuleName(RSYSLOG_MODULE_NAME)
        .setLogSeverityLevel(1)
        .setMonitor(true)
        .build();
    GenesisInterfaceProto.RemoteSyslogConfiguration remoteSyslogConfiguration =
      GenesisInterfaceProto.RemoteSyslogConfiguration.newBuilder()
        .setIpAddress("10.10.10.10")
        .setPort(8080)
        .setServerName(RSYSLOG_SERVER_NAME)
        .setNetworkProtocol("TCP")
        .addModules(syslogModule)
        .build();

    InsightsInterfaceProto.MetricData metric1 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName(ClustermgmtUtils.ZPROTOBUF_ATTR)
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setBytesValue(compress(remoteSyslogConfiguration.toByteString()))
            .build())
          .build())
        .build();

    return InsightsInterfaceProto.GetEntitiesWithMetricsRet.newBuilder().setTotalGroupCount(1)
      .addGroupResultsList(InsightsInterfaceProto.QueryGroupResult.newBuilder().
        addRawResults(
          InsightsInterfaceProto.EntityWithMetric.newBuilder()
            .setEntityGuid(
              InsightsInterfaceProto.EntityGuid.newBuilder().
                setEntityId("57115848-711b-4f03-b94c-3b3e2e014639").
                setEntityTypeName(ClustermgmtUtils.RSYSLOG_ENTITY).
                build())
            .addMetricDataList(metric1)
        ).setTotalEntityCount(1).build()).build();
  }

  public static SnmpUser getSnmpUser() {
    SnmpUser snmpUser = new SnmpUser();
    snmpUser.setUsername(SNMP_USERNAME);
    snmpUser.setPrivKey("testKey");
    snmpUser.setPrivType(SnmpPrivType.AES);
    snmpUser.setAuthKey("testKey");
    snmpUser.setAuthType(SnmpAuthType.SHA);
    snmpUser.setExtId(ClusterTestUtils.SNMP_USER_UUID);
    return snmpUser;
  }

  public static InsightsInterfaceProto.GetEntitiesWithMetricsRet getSnmpUserIdfEntityObj(String snmpUserUuid) {

    GenesisInterfaceProto.SnmpUser snmpUserProto =
      GenesisInterfaceProto.SnmpUser.newBuilder()
        .setUsername(SNMP_USERNAME)
        .setAuthType("SHA")
        .setPrivType("AES")
        .build();
    InsightsInterfaceProto.MetricData metric1 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName(ClustermgmtUtils.ZPROTOBUF_ATTR)
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setBytesValue(compress(snmpUserProto.toByteString()))
            .build())
          .build())
        .build();

    return InsightsInterfaceProto.GetEntitiesWithMetricsRet.newBuilder().setTotalGroupCount(1)
      .addGroupResultsList(InsightsInterfaceProto.QueryGroupResult.newBuilder().
        addRawResults(
          InsightsInterfaceProto.EntityWithMetric.newBuilder()
            .setEntityGuid(
              InsightsInterfaceProto.EntityGuid.newBuilder().
                setEntityId(snmpUserUuid).
                setEntityTypeName(ClustermgmtUtils.SNMP_USER_ENTITY).
                build())
            .addMetricDataList(metric1)
        ).setTotalEntityCount(1).build()).build();
  }

  public static InsightsInterfaceProto.GetEntitiesWithMetricsRet IdfEntityObjWithMetricRetWithNoEntity() {
    InsightsInterfaceProto.GetEntitiesWithMetricsRet.Builder getEntitiesWithMetricRet = InsightsInterfaceProto.GetEntitiesWithMetricsRet.newBuilder();
    return getEntitiesWithMetricRet.build();
  }

  public static SnmpTrap getSnmpTrap() {
    SnmpTrap snmpTrap = new SnmpTrap();
    snmpTrap.setAddress(createIpAddress(SNMP_TRAP_ADDRESS, null));
    snmpTrap.setVersion(SnmpTrapVersion.V2);
    snmpTrap.setUsername("testUserName");
    snmpTrap.setPort(8080);
    snmpTrap.setProtocol(SnmpProtocol.TCP);
    snmpTrap.setShouldInform(true);
    snmpTrap.setCommunityString("testCommunityString");
    snmpTrap.setRecieverName("testRecieverName");
    snmpTrap.setEngineId("0xtestEngineId");
    snmpTrap.setExtId(ClusterTestUtils.SNMP_TRAP_UUID);
    return snmpTrap;
  }

  public static SnmpTrap getInvalidSnmpTrapEntity(Boolean engineIdLen, Boolean engineIdParity,
                                                  Boolean engineIdStartConstraint, Boolean invalidPort,
                                                  Boolean isV3Version) {
    SnmpTrap snmpTrap = new SnmpTrap();
    snmpTrap.setAddress(createIpAddress(SNMP_TRAP_ADDRESS, null));
    if (isV3Version){
      snmpTrap.setVersion(SnmpTrapVersion.V3);
    }else{
      snmpTrap.setVersion(SnmpTrapVersion.V2);
    }
    if(isV3Version.equals(false)) snmpTrap.setUsername("testUserName");
    if(invalidPort.equals(true)){
      snmpTrap.setPort(65538);
    }else{
      snmpTrap.setPort(8080);
    }
    snmpTrap.setProtocol(SnmpProtocol.TCP);
    snmpTrap.setShouldInform(true);
    snmpTrap.setCommunityString("testCommunityString");
    snmpTrap.setRecieverName("testRecieverName");
    if(engineIdStartConstraint.equals(true)){
      snmpTrap.setEngineId("abtestEngineId");
    }else if(engineIdLen.equals(true)){
      snmpTrap.setEngineId("0xEngineId");
    }else if(engineIdParity.equals(true)){
      snmpTrap.setEngineId("0xtestEngineId1");
    }else{
      snmpTrap.setEngineId("0xtestEngineId");
    }
    snmpTrap.setExtId(ClusterTestUtils.SNMP_TRAP_UUID);
    return snmpTrap;
  }

  public static InsightsInterfaceProto.GetEntitiesWithMetricsRet getSnmpTrapIdfEntityObj(String snmpTrapUuid) {

    GenesisInterfaceProto.SnmpTrap snmpTrapProto =
      GenesisInterfaceProto.SnmpTrap.newBuilder()
        .setAddress(SNMP_TRAP_ADDRESS)
        .setVersion("V2")
        .setPort(SNMP_PORT)
        .setUsername(SNMP_USERNAME)
        .setProtocol("TCP")
        .setReceiverName("testRecieverName")
        .setEngineId("0xtestEngineId")
        .setCommunityString("testCommunityString")
        .setIsInform(true)
        .build();
    InsightsInterfaceProto.MetricData metric1 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName(ClustermgmtUtils.ZPROTOBUF_ATTR)
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setBytesValue(compress(snmpTrapProto.toByteString()))
            .build())
          .build())
        .build();

    return InsightsInterfaceProto.GetEntitiesWithMetricsRet.newBuilder().setTotalGroupCount(1)
      .addGroupResultsList(InsightsInterfaceProto.QueryGroupResult.newBuilder().
        addRawResults(
          InsightsInterfaceProto.EntityWithMetric.newBuilder()
            .setEntityGuid(
              InsightsInterfaceProto.EntityGuid.newBuilder().
                setEntityId(snmpTrapUuid).
                setEntityTypeName(ClustermgmtUtils.SNMP_TRAP_ENTITY).
                build())
            .addMetricDataList(metric1)
        ).setTotalEntityCount(1).build()).build();
  }

  public static InsightsInterfaceProto.GetEntitiesWithMetricsRet getSnmpTransportIdfEntityObj(String snmpTransportUuid) {

    GenesisInterfaceProto.SnmpTransport snmpTransportProto =
      GenesisInterfaceProto.SnmpTransport.newBuilder()
        .setProtocol("TCP")
        .setPort(SNMP_PORT)
        .build();
    InsightsInterfaceProto.MetricData metric1 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName(ClustermgmtUtils.ZPROTOBUF_ATTR)
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setBytesValue(compress(snmpTransportProto.toByteString()))
            .build())
          .build())
        .build();

    return InsightsInterfaceProto.GetEntitiesWithMetricsRet.newBuilder().setTotalGroupCount(1)
      .addGroupResultsList(InsightsInterfaceProto.QueryGroupResult.newBuilder().
        addRawResults(
          InsightsInterfaceProto.EntityWithMetric.newBuilder()
            .setEntityGuid(
              InsightsInterfaceProto.EntityGuid.newBuilder().
                setEntityId(snmpTransportUuid).
                setEntityTypeName(ClustermgmtUtils.SNMP_TRANSPORT_ENTITY).
                build())
            .addMetricDataList(metric1)
        ).setTotalEntityCount(1).build()).build();
  }

  public static InsightsInterfaceProto.GetEntitiesWithMetricsRet getHttpProxyIdfEntityObj(String httpProxyUuid) {
  GenesisInterfaceProto.HttpProxy httpProxyProto =
    GenesisInterfaceProto.HttpProxy.newBuilder()
      .setName(HTTP_PROXY_NAME)
      .setAddress(HTTP_PROXY_ADDRESS)
      .setPort(8080)
      .setUsername(HTTP_PROXY_USERNAME)
      .setPassword(HTTP_PROXY_PASSWORD)
      .addProxyType("HTTP")
      .addProxyType("HTTPS")
      .build();
    InsightsInterfaceProto.MetricData metric1 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName(ClustermgmtUtils.ZPROTOBUF_ATTR)
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setBytesValue(compress(httpProxyProto.toByteString()))
            .build())
          .build())
        .build();

    return InsightsInterfaceProto.GetEntitiesWithMetricsRet.newBuilder().setTotalGroupCount(1)
      .addGroupResultsList(InsightsInterfaceProto.QueryGroupResult.newBuilder().
        addRawResults(
          InsightsInterfaceProto.EntityWithMetric.newBuilder()
            .setEntityGuid(
              InsightsInterfaceProto.EntityGuid.newBuilder().
                setEntityId(httpProxyUuid).
                setEntityTypeName(ClustermgmtUtils.HTTP_PROXY_ENTITY).
                build())
            .addMetricDataList(metric1)
        ).setTotalEntityCount(1).build()).build();
  }

  public static InsightsInterfaceProto.GetEntitiesWithMetricsRet getHttpProxyWhitelistIdfEntityObj(String httpProxyWhitelistUuid) {

    GenesisInterfaceProto.HttpProxyWhitelist httpProxyWhitelistProto =
      GenesisInterfaceProto.HttpProxyWhitelist.newBuilder()
        .setTarget("1.2.3.4")
        .setTargetType("IPV4_ADDRESS")
        .build();
    InsightsInterfaceProto.MetricData metric1 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName(ClustermgmtUtils.ZPROTOBUF_ATTR)
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setBytesValue(compress(httpProxyWhitelistProto.toByteString()))
            .build())
          .build())
        .build();

    return InsightsInterfaceProto.GetEntitiesWithMetricsRet.newBuilder().setTotalGroupCount(1)
      .addGroupResultsList(InsightsInterfaceProto.QueryGroupResult.newBuilder().
        addRawResults(
          InsightsInterfaceProto.EntityWithMetric.newBuilder()
            .setEntityGuid(
              InsightsInterfaceProto.EntityGuid.newBuilder().
                setEntityId(httpProxyWhitelistUuid).
                setEntityTypeName(ClustermgmtUtils.HTTP_PROXY_WHITELIST_ENTITY).
                build())
            .addMetricDataList(metric1)
        ).setTotalEntityCount(1).build()).build();
  }

  public static ErgonInterface.TaskCreateRet getErgonTask() {
    return ErgonInterface.TaskCreateRet.newBuilder().setExtId(TASK_UUID).build();
  }

  public static Cluster getUpdateClusterEntityObj() {
    Cluster clusterEntity = new Cluster();
    clusterEntity.setName(CLUSTER_NAME);

    ClusterNetworkReference clusterNetworkReference = new ClusterNetworkReference();
    List<IPAddressOrFQDN> ipAddresses = new ArrayList<>();
    ipAddresses.add(ClustermgmtUtils.createIpv4Ipv6OrFqdnAddress("1.1.1.1"));
    ipAddresses.add(ClustermgmtUtils.createIpv4Ipv6OrFqdnAddress("2.3.4.2"));
    clusterNetworkReference.setNtpServerIpList(ipAddresses);
    clusterNetworkReference.setNameServerIpList(ipAddresses);
    clusterNetworkReference.setNfsSubnetWhitelist(Arrays.asList("1.1.1.1","2.2.2.2"));
    SmtpServerRef smtpServerRef = new SmtpServerRef();
    smtpServerRef.setType(SmtpType.PLAIN);
    smtpServerRef.setEmailAddress("testEmail.com");
    SmtpNetwork smtpNetwork = new SmtpNetwork();
    smtpNetwork.setPort(8080);
    smtpNetwork.setUsername("testName");
    smtpNetwork.setPassword("testPassword");
    smtpNetwork.setIpAddress(createIpAddressOrFqdn("10.10.10.10", null, null));
    smtpServerRef.setServer(smtpNetwork);
    clusterNetworkReference.setSmtpServer(smtpServerRef);
    clusterNetworkReference.setExternalAddress(createIpAddress("10.10.10.10", "2001:0db8:85a3:0000:0000:8a2e:0370:7334"));
    clusterNetworkReference.setExternalDataServiceIp(createIpAddress("10.10.10.10", null));
    HttpProxyConfig httpProxyRef = new HttpProxyConfig();
    httpProxyRef.setName("testName");
    httpProxyRef.setIpAddress(createIpAddress("1.2.3.4", null));
    httpProxyRef.setPort(8080);
    httpProxyRef.setUsername(HTTP_PROXY_USERNAME);
    httpProxyRef.setPassword(HTTP_PROXY_PASSWORD);
    List<HttpProxyType> httpProxyTypeList = new ArrayList<>();
    httpProxyTypeList.add(HttpProxyType.HTTP);
    httpProxyTypeList.add(HttpProxyType.HTTPS);
    httpProxyRef.setProxyTypes(httpProxyTypeList);
    List<HttpProxyConfig> httpProxyConfigList = new ArrayList<>();
    httpProxyConfigList.add(httpProxyRef);
    clusterNetworkReference.setHttpProxyList(httpProxyConfigList);
    HttpProxyWhiteListConfig httpProxyWhiteListConfig = new HttpProxyWhiteListConfig();
    httpProxyWhiteListConfig.setTarget("1.1.1.1");
    httpProxyWhiteListConfig.setTargetType(HttpProxyWhiteListTargetType.IPV4_ADDRESS);
    List<HttpProxyWhiteListConfig> httpProxyWhiteListConfigList = new ArrayList<>();
    httpProxyWhiteListConfigList.add(httpProxyWhiteListConfig);
    clusterNetworkReference.setHttpProxyWhiteList(httpProxyWhiteListConfigList);
    clusterEntity.setNetwork(clusterNetworkReference);

    ClusterConfigReference clusterConfigReference = new ClusterConfigReference();
    clusterConfigReference.setOperationMode(OperationMode.NORMAL);
    clusterConfigReference.setRedundancyFactor(1L);
    FaultToleranceState faultToleranceState = new FaultToleranceState();
    faultToleranceState.setDomainAwarenessLevel(DomainAwarenessLevel.BLOCK);
    faultToleranceState.setDesiredClusterFaultTolerance(ClusterFaultToleranceRef.CFT_1N_OR_1D);
    clusterConfigReference.setFaultToleranceState(faultToleranceState);
    List<PublicKey> publicKeys = new ArrayList<>();
    PublicKey publicKey = new PublicKey();
    publicKey.setKey("testKey");
    publicKey.setName("testName");
    publicKeys.add(publicKey);
    clusterConfigReference.setAuthorizedPublicKeyList(publicKeys);
    clusterEntity.setConfig(clusterConfigReference);
    return clusterEntity;
  }

  public static GenesisInterfaceProto.UpdateClusterRet getUpdateClusterRet() {
    GenesisInterfaceProto.UpdateClusterRet updateClusterRet = GenesisInterfaceProto.UpdateClusterRet.
            newBuilder().setTaskUuid(UuidUtils.getByteStringFromUUID(TASK_UUID)).build();
    return updateClusterRet;
  }

  public static GenesisInterfaceProto.UpdateSnmpStatusRet getSnmpStatusRet() {
    GenesisInterfaceProto.UpdateSnmpStatusRet updateSnmpStatusRet = GenesisInterfaceProto.UpdateSnmpStatusRet.newBuilder().setError(
      GenesisInterfaceProto.GenesisGenericError.newBuilder().setErrorType(
        GenesisInterfaceProto.GenesisGenericError.ErrorType.kNoError)
        .setErrorMsg("No Error")).setTaskUuid(UuidUtils.getByteStringFromUUID(TASK_UUID)).build();
    return updateSnmpStatusRet;
  }

  public static GenesisInterfaceProto.AddSnmpTransportsRet getAddSnmpTransportsRet() {
    GenesisInterfaceProto.AddSnmpTransportsRet addTransportsRet = GenesisInterfaceProto.AddSnmpTransportsRet.newBuilder().setError(
      GenesisInterfaceProto.GenesisGenericError.newBuilder().setErrorType(
        GenesisInterfaceProto.GenesisGenericError.ErrorType.kNoError)
        .setErrorMsg("No Error")).setTaskUuid(UuidUtils.getByteStringFromUUID(TASK_UUID)).build();
    return addTransportsRet;
  }

  public static GenesisInterfaceProto.RemoveSnmpTransportsRet getRemoveSnmpTransportsRet() {
    GenesisInterfaceProto.RemoveSnmpTransportsRet removeTransportsRet = GenesisInterfaceProto.RemoveSnmpTransportsRet.newBuilder().setError(
      GenesisInterfaceProto.GenesisGenericError.newBuilder().setErrorType(
        GenesisInterfaceProto.GenesisGenericError.ErrorType.kNoError)
        .setErrorMsg("No Error")).setTaskUuid(UuidUtils.getByteStringFromUUID(TASK_UUID)).build();
    return removeTransportsRet;
  }

  public static SnmpTransport getSnmpTransport(Boolean isValidPort) {
    SnmpTransport snmpTransport = new SnmpTransport();
    snmpTransport.setProtocol(SnmpProtocol.TCP);
    if(isValidPort.equals(true)) snmpTransport.setPort(8080);
    else{
      snmpTransport.setPort(9999999);
    }
    return snmpTransport;
  }

  public static GenesisInterfaceProto.AddSnmpUserRet getAddSnmpUserRet() {
    GenesisInterfaceProto.AddSnmpUserRet addSnmpUserRet = GenesisInterfaceProto.AddSnmpUserRet.newBuilder().setError(
      GenesisInterfaceProto.GenesisGenericError.newBuilder().setErrorType(
        GenesisInterfaceProto.GenesisGenericError.ErrorType.kNoError)
        .setErrorMsg("No Error")).setTaskUuid(UuidUtils.getByteStringFromUUID(TASK_UUID)).build();
    return addSnmpUserRet;
  }

  public static GenesisInterfaceProto.UpdateSnmpUserRet getUpdateSnmpUserRet() {
    GenesisInterfaceProto.UpdateSnmpUserRet updateSnmpUserRet = GenesisInterfaceProto.UpdateSnmpUserRet.newBuilder().setError(
      GenesisInterfaceProto.GenesisGenericError.newBuilder().setErrorType(
        GenesisInterfaceProto.GenesisGenericError.ErrorType.kNoError)
        .setErrorMsg("No Error")).setTaskUuid(UuidUtils.getByteStringFromUUID(TASK_UUID)).build();
    return updateSnmpUserRet;
  }

  public static GenesisInterfaceProto.DeleteSnmpUserRet getDeleteSnmpUserRet() {
    GenesisInterfaceProto.DeleteSnmpUserRet deleteSnmpUserRet = GenesisInterfaceProto.DeleteSnmpUserRet.newBuilder().setError(
      GenesisInterfaceProto.GenesisGenericError.newBuilder().setErrorType(
        GenesisInterfaceProto.GenesisGenericError.ErrorType.kNoError)
        .setErrorMsg("No Error")).setTaskUuid(UuidUtils.getByteStringFromUUID(TASK_UUID)).build();
    return deleteSnmpUserRet;
  }

  public static GenesisInterfaceProto.AddSnmpTrapRet getAddSnmpTrapRet() {
    GenesisInterfaceProto.AddSnmpTrapRet addSnmpTrapRet = GenesisInterfaceProto.AddSnmpTrapRet.newBuilder().setError(
      GenesisInterfaceProto.GenesisGenericError.newBuilder().setErrorType(
        GenesisInterfaceProto.GenesisGenericError.ErrorType.kNoError)
        .setErrorMsg("No Error")).setTaskUuid(UuidUtils.getByteStringFromUUID(TASK_UUID)).build();
    return addSnmpTrapRet;
  }

  public static GenesisInterfaceProto.UpdateSnmpTrapRet getUpdateSnmpTrapRet() {
    GenesisInterfaceProto.UpdateSnmpTrapRet updateSnmpTrapRet = GenesisInterfaceProto.UpdateSnmpTrapRet.newBuilder().setError(
      GenesisInterfaceProto.GenesisGenericError.newBuilder().setErrorType(
        GenesisInterfaceProto.GenesisGenericError.ErrorType.kNoError)
        .setErrorMsg("No Error")).setTaskUuid(UuidUtils.getByteStringFromUUID(TASK_UUID)).build();
    return updateSnmpTrapRet;
  }

  public static GenesisInterfaceProto.DeleteSnmpTrapRet getDeleteSnmpTrapRet() {
    GenesisInterfaceProto.DeleteSnmpTrapRet deleteSnmpTrapRet = GenesisInterfaceProto.DeleteSnmpTrapRet.newBuilder().setError(
      GenesisInterfaceProto.GenesisGenericError.newBuilder().setErrorType(
        GenesisInterfaceProto.GenesisGenericError.ErrorType.kNoError)
        .setErrorMsg("No Error")).setTaskUuid(UuidUtils.getByteStringFromUUID(TASK_UUID)).build();
    return deleteSnmpTrapRet;
  }

  public static GenesisInterfaceProto.CreateRemoteSyslogServerRet getCreateRsyslogServerRet() {
    GenesisInterfaceProto.CreateRemoteSyslogServerRet createRemoteSyslogServerRet =
      GenesisInterfaceProto.CreateRemoteSyslogServerRet.newBuilder().setError(
      GenesisInterfaceProto.GenesisGenericError.newBuilder().setErrorType(
        GenesisInterfaceProto.GenesisGenericError.ErrorType.kNoError)
        .setErrorMsg("No Error")).setTaskUuid(UuidUtils.getByteStringFromUUID(TASK_UUID)).build();
    return createRemoteSyslogServerRet;
  }

  public static GenesisInterfaceProto.UpdateRemoteSyslogServerRet getUpdateRsyslogServerRet() {
    GenesisInterfaceProto.UpdateRemoteSyslogServerRet updateRemoteSyslogServerRet =
      GenesisInterfaceProto.UpdateRemoteSyslogServerRet.newBuilder().setError(
        GenesisInterfaceProto.GenesisGenericError.newBuilder().setErrorType(
          GenesisInterfaceProto.GenesisGenericError.ErrorType.kInputError)
          .setErrorMsg("Error")).setTaskUuid(UuidUtils.getByteStringFromUUID(TASK_UUID)).build();
    return updateRemoteSyslogServerRet;
  }

  public static GenesisInterfaceProto.DeleteRemoteSyslogServerRet getDeleteRsyslogServerRet() {
    GenesisInterfaceProto.DeleteRemoteSyslogServerRet deleteRemoteSyslogServerRet =
      GenesisInterfaceProto.DeleteRemoteSyslogServerRet.newBuilder().setError(
        GenesisInterfaceProto.GenesisGenericError.newBuilder().setErrorType(
          GenesisInterfaceProto.GenesisGenericError.ErrorType.kNoError)
          .setErrorMsg("No Error")).setTaskUuid(UuidUtils.getByteStringFromUUID(TASK_UUID)).build();
    return deleteRemoteSyslogServerRet;
  }

  public static NodeDiscoveryParams getDiscoverNodes() {
    List<IPAddress> tempList = new ArrayList<>();
    tempList.add(createIpAddress("10.10.10.10", null));
    List<String> tempStrList = new ArrayList<>();
    tempStrList.add("test");
    NodeDiscoveryParams discoverUnconfiguredNode = new NodeDiscoveryParams();
    discoverUnconfiguredNode.setAddressType(AddressType.IPV4);
    discoverUnconfiguredNode.setIsManualDiscovery(true);
    discoverUnconfiguredNode.setInterfaceFilterList(tempStrList);
    discoverUnconfiguredNode.setTimeout(2L);
    discoverUnconfiguredNode.setIpFilterList(tempList);
    discoverUnconfiguredNode.setUuidFilterList(tempStrList);
    return discoverUnconfiguredNode;
  }

  public static NodeDiscoveryParams getDiscoverNodesWithNoParam() {
    NodeDiscoveryParams discoverUnconfiguredNode = new NodeDiscoveryParams();
    return discoverUnconfiguredNode;
  }

  public static NodeDetails getNodeNetworkingDetails() {
    NodeDetails networkingDetails = new NodeDetails();
    List<NodeListNetworkingDetails> nodeListNetworkingDetailsArrayList = new ArrayList<>();
    NodeListNetworkingDetails nodeListNetworkingDetails = new NodeListNetworkingDetails();
    nodeListNetworkingDetails.setBlockId("test");
    nodeListNetworkingDetails.setCurrentNetworkInterface("eth0");
    nodeListNetworkingDetails.setNodeUuid(HostTestUtils.HOST_UUID);
    nodeListNetworkingDetails.setNodePosition("A");
    nodeListNetworkingDetails.setCvmIp(createIpAddress("10.10.10.10", "2001:0db8:85a3:0000:0000:8a2e:0370:7334"));
    nodeListNetworkingDetails.setHypervisorIp(createIpAddress("10.10.10.10", "2001:0db8:85a3:0000:0000:8a2e:0370:7334"));
    nodeListNetworkingDetails.setHypervisorType(HypervisorType.AHV);
    nodeListNetworkingDetails.setIpmiIp(createIpAddress("10.10.10.10", "2001:0db8:85a3:0000:0000:8a2e:0370:7334"));
    nodeListNetworkingDetails.setHypervisorVersion("5.6");
    nodeListNetworkingDetails.setIsComputeOnly(true);
    nodeListNetworkingDetails.setIsLightCompute(true);
    nodeListNetworkingDetails.setModel("NX-8080");
    nodeListNetworkingDetails.setNosVersion("5.6");
    nodeListNetworkingDetails.setIsRoboMixedHypervisor(false);
    List<DigitalCertificateMapReference> digitalCertificateMapReferences = new ArrayList<>();
    DigitalCertificateMapReference digitalCertificateMapReference = new DigitalCertificateMapReference();
    digitalCertificateMapReference.setKey("test");
    digitalCertificateMapReferences.add(digitalCertificateMapReference);
    nodeListNetworkingDetails.setDigitalCertificateMapList(digitalCertificateMapReferences);
    nodeListNetworkingDetailsArrayList.add(nodeListNetworkingDetails);
    networkingDetails.setNodeList(nodeListNetworkingDetailsArrayList);
    networkingDetails.setRequestType("expand-cluster");
    return networkingDetails;
  }

  public static NodeDetails getNodeNetworkingDetailsWithNoParam() {
    NodeDetails networkingDetails = new NodeDetails();
    List<NodeListNetworkingDetails> nodeListNetworkingDetailsArrayList = new ArrayList<>();
    NodeListNetworkingDetails nodeListNetworkingDetails = new NodeListNetworkingDetails();
    List<DigitalCertificateMapReference> digitalCertificateMapReferences = new ArrayList<>();
    DigitalCertificateMapReference digitalCertificateMapReference = new DigitalCertificateMapReference();
    digitalCertificateMapReferences.add(digitalCertificateMapReference);
    nodeListNetworkingDetails.setDigitalCertificateMapList(digitalCertificateMapReferences);
    nodeListNetworkingDetailsArrayList.add(nodeListNetworkingDetails);
    networkingDetails.setNodeList(nodeListNetworkingDetailsArrayList);
    return networkingDetails;
  }

  public static ExpandClusterParams getAddNode() {
    ExpandClusterParams addNode = new ExpandClusterParams();
    addNode.setShouldSkipAddNode(true);
    addNode.setShouldSkipPreExpandChecks(true);

    NodeParam nodeParam = new NodeParam();
    nodeParam.setHypervSku("test");
    List<NodeItem> nodeItemList = new ArrayList<>();
    NodeItem nodeItem = new NodeItem();
    nodeItem.setBlockId("NX-8080");
    nodeItem.setNodeUuid(HostTestUtils.HOST_UUID);
    nodeItem.setNodePosition("A");
    nodeItem.setCvmIp(createIpAddress("10.10.10.10", null));
    nodeItem.setCurrentNetworkInterface("eth0");
    nodeItem.setHypervisorIp(createIpAddress("10.10.10.10", null));
    nodeItem.setModel("NX-8080");
    nodeItem.setHypervisorHostname("test");
    nodeItem.setHypervisorType(HypervisorType.AHV);
    nodeItem.setHypervisorVersion("5.6");
    nodeItem.setIpmiIp(createIpAddress("10.10.10.10", null));
    nodeItem.setIsLightCompute(true);
    nodeItem.setNosVersion("5.6");
    List<DigitalCertificateMapReference> digitalCertificateMapReferences = new ArrayList<>();
    DigitalCertificateMapReference digitalCertificateMapReference = new DigitalCertificateMapReference();
    digitalCertificateMapReference.setKey("test");
    digitalCertificateMapReferences.add(digitalCertificateMapReference);
    nodeItem.setDigitalCertificateMapList(digitalCertificateMapReferences);
    nodeItemList.add(nodeItem);
    nodeParam.setNodeList(nodeItemList);

    List<ComputeNodeItem> computeNodeItems = new ArrayList<>();
    ComputeNodeItem computeNodeItem = new ComputeNodeItem();
    computeNodeItem.setNodeUuid(HostTestUtils.HOST_UUID);
    computeNodeItem.setNodePosition("A");
    computeNodeItem.setBlockId("test");
    computeNodeItem.setHypervisorIp(createIpAddress("10.10.10.10", null));
    computeNodeItem.setIpmiIp(createIpAddress("10.10.10.10", null));
    computeNodeItem.setDigitalCertificateMapList(digitalCertificateMapReferences);
    computeNodeItem.setHypervisorHostname("test");
    computeNodeItems.add(computeNodeItem);
    nodeParam.setComputeNodeList(computeNodeItems);
    nodeParam.setShouldSkipHostNetworking(true);
    addNode.setNodeParams(nodeParam);

    ConfigParams extraParam = new ConfigParams();
    HypervCredentials hyperVCredAddNode = new HypervCredentials();
    UserInfo userNamePassword = new UserInfo();
    userNamePassword.setPassword("test");
    userNamePassword.setUserName("test");
    hyperVCredAddNode.setDomainDetails(userNamePassword);
    hyperVCredAddNode.setFailoverClusterDetails(userNamePassword);
    extraParam.setIsComputeOnly(false);
    extraParam.setIsNosCompatible(false);
    extraParam.setIsNeverScheduleable(false);
    extraParam.setShouldSkipDiscovery(false);
    extraParam.setShouldSkipImaging(false);
    extraParam.setTargetHypervisor("5.6");
    extraParam.setShouldValidateRackAwareness(false);
    extraParam.setHyperv(hyperVCredAddNode);
    addNode.setConfigParams(extraParam);

    return addNode;
  }

  public static ExpandClusterParams getAddNodeWithNoParam() {
    ExpandClusterParams addNode = new ExpandClusterParams();
    addNode.setShouldSkipAddNode(true);
    addNode.setShouldSkipPreExpandChecks(true);

    NodeParam nodeParam = new NodeParam();
    nodeParam.setHypervSku("test");
    List<NodeItem> nodeItemList = new ArrayList<>();
    NodeItem nodeItem = new NodeItem();
    List<ComputeNodeItem> computeNodeItems = new ArrayList<>();
    ComputeNodeItem computeNodeItem = new ComputeNodeItem();
    List<DigitalCertificateMapReference> digitalCertificateMapReferences = new ArrayList<>();
    DigitalCertificateMapReference digitalCertificateMapReference = new DigitalCertificateMapReference();
    digitalCertificateMapReferences.add(digitalCertificateMapReference);
    nodeItem.setDigitalCertificateMapList(digitalCertificateMapReferences);
    nodeItemList.add(nodeItem);
    computeNodeItems.add(computeNodeItem);
    nodeParam.setNodeList(nodeItemList);
    nodeParam.setComputeNodeList(computeNodeItems);
    addNode.setNodeParams(nodeParam);

    ConfigParams extraParam = new ConfigParams();
    HypervCredentials hyperVCredAddNode = new HypervCredentials();
    UserInfo userNamePassword = new UserInfo();
    hyperVCredAddNode.setDomainDetails(userNamePassword);
    hyperVCredAddNode.setFailoverClusterDetails(userNamePassword);
    addNode.setConfigParams(extraParam);

    return addNode;
  }

  public static HypervisorUploadParam getIsHypervisorReq() {
    HypervisorUploadParam hypervisorUploadParam = new HypervisorUploadParam();

    List<HypervisorUploadNodeListItem> hypervisorUploadNodeListItems = new ArrayList<>();
    HypervisorUploadNodeListItem hypervisorUploadNodeListItem = new HypervisorUploadNodeListItem();
    hypervisorUploadNodeListItem.setHypervisorType(HypervisorType.AHV);
    hypervisorUploadNodeListItem.setNodeUuid(HostTestUtils.HOST_UUID);
    hypervisorUploadNodeListItem.setHypervisorVersion("5.6");
    hypervisorUploadNodeListItem.setIsRoboMixedHypervisor(false);
    hypervisorUploadNodeListItem.setBlockId("NX-8080");
    hypervisorUploadNodeListItem.setModel("NX-8080");
    hypervisorUploadNodeListItem.setIsLightCompute(false);
    hypervisorUploadNodeListItem.setNosVersion("5.6");
    hypervisorUploadNodeListItem.setIsMinimumComputeNode(false);
    hypervisorUploadNodeListItems.add(hypervisorUploadNodeListItem);

    hypervisorUploadParam.setNodeList(hypervisorUploadNodeListItems);
    return hypervisorUploadParam;
  }

  public static HypervisorUploadParam getIsHypervisorReqWithNoParam() {
    HypervisorUploadParam hypervisorUploadParam = new HypervisorUploadParam();

    List<HypervisorUploadNodeListItem> hypervisorUploadNodeListItems = new ArrayList<>();
    HypervisorUploadNodeListItem hypervisorUploadNodeListItem = new HypervisorUploadNodeListItem();
    hypervisorUploadNodeListItems.add(hypervisorUploadNodeListItem);

    hypervisorUploadParam.setNodeList(hypervisorUploadNodeListItems);
    return hypervisorUploadParam;
  }

  public static List<UplinkNode> getValidateUplinkParam() {
    List<UplinkNode> validateUplinkNodeItems = new ArrayList<>();
    UplinkNode validateUplinkNodeItem = new UplinkNode();

    List<String> networkList = new ArrayList<>();
    List<UplinksField> uplinksFields = new ArrayList<>();
    UplinksField uplinksField = new UplinksField();
    uplinksField.setValue("test");
    uplinksField.setName("test");
    uplinksField.setMac("test");
    uplinksFields.add(uplinksField);

    networkList.add("test");
    Uplinks uplinks = new Uplinks();
    uplinks.setActive(uplinksFields);
    uplinks.setStandby(uplinksFields);
    List<UplinkNetworkItem> validateUplinkNetworkItemList = new ArrayList<>();
    UplinkNetworkItem validateUplinkNetworkItem = new UplinkNetworkItem();
    validateUplinkNetworkItem.setName("test");
    validateUplinkNetworkItem.setNetworks(networkList);
    validateUplinkNetworkItem.setUplinks(uplinks);
    validateUplinkNetworkItemList.add(validateUplinkNetworkItem);
    validateUplinkNodeItem.setCvmIp(createIpAddress("10.10.10.10", null));
    validateUplinkNodeItem.setHypervisorIp(createIpAddress("10.10.10.10", null));
    validateUplinkNodeItem.setNetworks(validateUplinkNetworkItemList);
    validateUplinkNodeItems.add(validateUplinkNodeItem);
    return validateUplinkNodeItems;
  }

  public static List<UplinkNode> getValidateUplinkParamWithNoParam() {
    List<UplinkNode> validateUplinkNodeItems = new ArrayList<>();
    UplinkNode validateUplinkNodeItem = new UplinkNode();
    List<UplinksField> uplinksFields = new ArrayList<>();
    UplinksField uplinksField = new UplinksField();
    uplinksFields.add(uplinksField);

    Uplinks uplinks = new Uplinks();
    uplinks.setActive(uplinksFields);
    uplinks.setStandby(uplinksFields);
    List<UplinkNetworkItem> validateUplinkNetworkItemList = new ArrayList<>();
    UplinkNetworkItem validateUplinkNetworkItem = new UplinkNetworkItem();
    validateUplinkNetworkItemList.add(validateUplinkNetworkItem);
    validateUplinkNodeItems.add(validateUplinkNodeItem);
    return validateUplinkNodeItems;
  }

  public static NodeRemovalParams getRemoveNode() {
    NodeRemovalParams removeNode = new NodeRemovalParams();
    List<String> nodeUuids = new ArrayList<>();
    nodeUuids.add(HostTestUtils.HOST_UUID);
    removeNode.setShouldSkipRemove(false);
    removeNode.setShouldSkipPrechecks(false);
    removeNode.setNodeUuids(nodeUuids);
    NodeRemovalExtraParam removeNodeExtraParam = new NodeRemovalExtraParam();
    removeNodeExtraParam.setShouldSkipAddCheck(true);
    removeNodeExtraParam.setShouldSkipSpaceCheck(true);
    removeNodeExtraParam.setShouldSkipUpgradeCheck(true);
    removeNode.setExtraParams(removeNodeExtraParam);
    return removeNode;
  }

  public static NodeRemovalParams getRemoveNodeWithLessParam() {
    NodeRemovalParams removeNode = new NodeRemovalParams();
    NodeRemovalExtraParam removeNodeExtraParam = new NodeRemovalExtraParam();
    removeNode.setExtraParams(removeNodeExtraParam);
    return removeNode;
  }

  public static BundleParam getValidateBundleParam() {
    BundleParam validateBundleParam = new BundleParam();

    List<NodeInfo> nodeItemList = new ArrayList<>();
    NodeInfo nodeItem = new NodeInfo();
    nodeItem.setBlockId("NX-8080");
    nodeItem.setNodeUuid(HostTestUtils.HOST_UUID);
    nodeItem.setNodePosition("A");
    nodeItem.setCvmIp(createIpAddress("10.10.10.10", null));
    nodeItem.setCurrentNetworkInterface("eth0");
    nodeItem.setHypervisorIp(createIpAddress("10.10.10.10", null));
    nodeItem.setModel("NX-8080");
    nodeItem.setHypervisorHostname("test");
    nodeItem.setHypervisorType(HypervisorType.AHV);
    nodeItem.setHypervisorVersion("5.6");
    nodeItem.setIpmiIp(createIpAddress("10.10.10.10", null));
    nodeItem.setIsLightCompute(true);
    nodeItem.setNosVersion("5.6");
    List<DigitalCertificateMapReference> digitalCertificateMapReferences = new ArrayList<>();
    DigitalCertificateMapReference digitalCertificateMapReference = new DigitalCertificateMapReference();
    digitalCertificateMapReference.setKey("test");
    digitalCertificateMapReferences.add(digitalCertificateMapReference);
    nodeItem.setDigitalCertificateMapList(digitalCertificateMapReferences);
    nodeItemList.add(nodeItem);
    validateBundleParam.setNodeList(nodeItemList);
    BundleInfo validateBundleInfo = new BundleInfo();
    validateBundleInfo.setName("test");
    validateBundleParam.setBundleInfo(validateBundleInfo);
    return validateBundleParam;
  }

  public static BundleParam getValidateBundleParamWithLessAttributes() {
    BundleParam validateBundleParam = new BundleParam();

    List<NodeInfo> nodeItemList = new ArrayList<>();
    NodeInfo nodeItem = new NodeInfo();
    List<DigitalCertificateMapReference> digitalCertificateMapReferences = new ArrayList<>();
    DigitalCertificateMapReference digitalCertificateMapReference = new DigitalCertificateMapReference();
    digitalCertificateMapReferences.add(digitalCertificateMapReference);
    nodeItem.setDigitalCertificateMapList(digitalCertificateMapReferences);
    nodeItemList.add(nodeItem);
    validateBundleParam.setNodeList(nodeItemList);
    BundleInfo validateBundleInfo = new BundleInfo();
    validateBundleParam.setBundleInfo(validateBundleInfo);
    return validateBundleParam;
  }

  public static InsightsInterfaceProto.GetEntitiesRet getCapabilitiesEntitiesRet(String... clusterUuids) {
    InsightsInterfaceProto.GetEntitiesRet.Builder getEntitiesRet = InsightsInterfaceProto.GetEntitiesRet.newBuilder();
    Arrays.stream(clusterUuids).forEach(clusterUuid  -> {
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("caps_json")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue(
              "{\n" +
                "    \"cluster_uuid\":\"" + clusterUuid + "\"," +
                "    \"cluster_capabilities\": {\n" +
                "        \"genesis_capabilities\": {\n" +
                "            \"genesis_version\": " +
                GenesisInterfaceProto.GenesisApiVersion.kPlannedOutageManagerSupportedVersion.getNumber() + "\n" +
                "        }\n" +
                "    }, \n" +
                "    \"logical_timestamp\": 1, \n" +
                "    \"version_info\": {\n" +
                "        \"current_version\": \"master\"\n" +
                "    }\n" +
                "}").build())
          .build();
        InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair2 =
          InsightsInterfaceProto.NameTimeValuePair.newBuilder()
            .setName("_cluster_uuid_")
            .setValue(
              InsightsInterfaceProto.DataValue.newBuilder().setStrValue(HostTestUtils.HOST_UUID)).build();
      InsightsInterfaceProto.Entity entity = InsightsInterfaceProto.Entity.newBuilder()
        .addAttributeDataMap(nameTimeValuePair)
        .addAttributeDataMap(nameTimeValuePair2)
        .setMasterClusterUuid(clusterUuid)
        .build();

      getEntitiesRet.addEntity(entity);
    });
    return getEntitiesRet.build();
  }

  public static InsightsInterfaceProto.GetEntitiesRet getCapabilitiesEntitiesRetWithoutAsyncSupport(String... clusterUuids) {
    InsightsInterfaceProto.GetEntitiesRet.Builder getEntitiesRet = InsightsInterfaceProto.GetEntitiesRet.newBuilder();
    Arrays.stream(clusterUuids).forEach(clusterUuid  -> {
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("caps_json")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue(
              "{\n" +
                "    \"cluster_uuid\":\"" + clusterUuid + "\"," +
                "    \"cluster_capabilities\": {\n" +
                "        \"genesis_capabilities\": {\n" +
                "            \"genesis_version\": " +
                GenesisInterfaceProto.GenesisApiVersion.kAddNodeSupportedVersion.getNumber() + "\n" +
                "        }\n" +
                "    }, \n" +
                "    \"logical_timestamp\": 1, \n" +
                "    \"version_info\": {\n" +
                "        \"current_version\": \"master\"\n" +
                "    }\n" +
                "}").build())
          .build();
      InsightsInterfaceProto.Entity entity = InsightsInterfaceProto.Entity.newBuilder()
        .addAttributeDataMap(nameTimeValuePair)
        .setMasterClusterUuid(clusterUuid)
        .build();

      getEntitiesRet.addEntity(entity);
    });
    return getEntitiesRet.build();
  }

  public static HostNameParam getHostNameParam(String hostName) {
    HostNameParam hostNameParam = new HostNameParam();
    hostNameParam.setName(hostName);
    return hostNameParam;
  }

  public static SnmpStatusParam getSnmpStatusParam() {
    SnmpStatusParam snmpStatusParam = new SnmpStatusParam();
    snmpStatusParam.setIsEnabled(true);
    return snmpStatusParam;
  }

  public static Cluster getClusterCreateParams(Boolean clusterFunctionFlag,
    Boolean ntpServerLstFlag, Boolean cvmIpLstFlag, Boolean cvmExtAddFlag, Boolean extIdFlag,
                                               Boolean currentCftFlag){
    Cluster clusterCreateParams = new Cluster();
    clusterCreateParams.setName(CLUSTER_NAME);
    if(extIdFlag) {
      clusterCreateParams.setExtId(CLUSTER_UUID);
    }
    ClusterConfigReference config = new ClusterConfigReference();
    List<ClusterFunctionRef> clusterFunctionRefList = new ArrayList<>();
    if(clusterFunctionFlag){
      clusterFunctionRefList.add(ClusterFunctionRef.AFS);
    }
    else{
      clusterFunctionRefList.add(ClusterFunctionRef.TWO_NODE);
    }
    config.setClusterFunction(clusterFunctionRefList);
    FaultToleranceState faultToleranceState = new FaultToleranceState();
    faultToleranceState.setDomainAwarenessLevel(DomainAwarenessLevel.RACK);
    if(currentCftFlag){
      faultToleranceState.setCurrentClusterFaultTolerance(ClusterFaultToleranceRef.CFT_1N_OR_1D);
    }else{
      faultToleranceState.setDesiredClusterFaultTolerance(ClusterFaultToleranceRef.CFT_1N_OR_1D);
    }
    config.setFaultToleranceState(faultToleranceState);
    config.setRedundancyFactor(2L);
    clusterCreateParams.setConfig(config);
    List<NodeListItemReference> nodeList = new ArrayList<>();
    NodeListItemReference nodeItem = new NodeListItemReference();
    IPAddress controllerVmIp = new IPAddress();
    if(cvmIpLstFlag){
      IPv6Address address = new IPv6Address("fe80::5054:ff:fe04:7866", 128);
      controllerVmIp.setIpv6(address);
    }
    else{
      IPv4Address address = new IPv4Address("10.15.99.5", 32);
      controllerVmIp.setIpv4(address);
    }
    nodeItem.setControllerVmIp(controllerVmIp);
    nodeList.add(nodeItem);
    NodeReference nodes = new NodeReference();
    nodes.setNodeList(nodeList);
    clusterCreateParams.setNodes(nodes);
    List<IPAddressOrFQDN> ipAddressOrFQDNList = new ArrayList<>();
    IPAddressOrFQDN obj = new IPAddressOrFQDN();
    FQDN fqdn1 = new FQDN("0.centos.pool.ntp.org");
    obj.setFqdn(fqdn1);
    ipAddressOrFQDNList.add(obj);
    IPAddressOrFQDN obj1 = new IPAddressOrFQDN();
    if(ntpServerLstFlag){
      obj1.setIpv6(ClustermgmtUtils.createIpv6Address("fe80::5054:ff:fe04:7866"));
    }
    else{
      obj1.setIpv4(ClustermgmtUtils.createIpv4Address("10.15.99.5"));
    }
    ipAddressOrFQDNList.add(obj1);
    ClusterNetworkReference clusterNetworkReference = new ClusterNetworkReference();
    clusterNetworkReference.setNtpServerIpList(ipAddressOrFQDNList);
    if(cvmExtAddFlag){
      IPAddress address = new IPAddress();
      IPv6Address ipv6Add = new IPv6Address("fe80::5054:ff:fe04:7866", 128);
      address.setIpv6(ipv6Add);
      clusterNetworkReference.setExternalAddress(address);
    }
    clusterCreateParams.setNetwork(clusterNetworkReference);
    return clusterCreateParams;
  }

  public static ClusterManagementInterfaceProto.ClusterCreateRet getClusterCreateRet() {
    ClusterManagementInterfaceProto.ClusterCreateRet clusterCreateRet = ClusterManagementInterfaceProto.ClusterCreateRet.
      newBuilder().setTaskUuid(UuidUtils.getByteStringFromUUID(TASK_UUID)).build();
    return clusterCreateRet;
  }

  public static ClusterManagementInterfaceProto.ClusterDestroyRet getClusterDestroyRet() {
    ClusterManagementInterfaceProto.ClusterDestroyRet clusterDestroyRet = ClusterManagementInterfaceProto.ClusterDestroyRet.
      newBuilder().setTaskUuid(UuidUtils.getByteStringFromUUID(TASK_UUID)).build();
    return clusterDestroyRet;
  }

  public static InsightsInterfaceProto.GetEntitiesRet getTaskIdfEntityObj(String... taskUuids) {
    List<String> entityList = new ArrayList<>();
    entityList.add("kCluster:AAXrcd3/u2pZHKwfaxjvXg==");
    InsightsInterfaceProto.GetEntitiesRet.Builder getEntitiesRet = InsightsInterfaceProto.GetEntitiesRet.newBuilder();
    Arrays.stream(taskUuids).forEach(clusterUuid ->{
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("request_context.user_uuid")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setBytesValue(UuidUtils.getByteStringFromUUID(ClusterTestUtils.USER_UUID)))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair2 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("entity_list")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrList(StrList.newBuilder().addAllValueList(entityList)))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair3 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("random")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setBytesValue(UuidUtils.getByteStringFromUUID(ClusterTestUtils.USER_UUID)))
          .build();

      InsightsInterfaceProto.Entity entity = InsightsInterfaceProto.Entity.newBuilder()
        .addAttributeDataMap(nameTimeValuePair)
        .addAttributeDataMap(nameTimeValuePair2)
        .addAttributeDataMap(nameTimeValuePair3)
        .build();

      getEntitiesRet.addEntity(entity);
    });
    return getEntitiesRet.build();
  }

  public static TaskReference getTaskWithColonUuid() {
    TaskReference task = new TaskReference();
    task.setExtId(TASK_COLON_UUID);
    return task;
  }

  public static IPAddress createIpAddress(String ipv4, String ipv6) {
    IPAddress ipAddress = new IPAddress();
    if(ipv4 != null) {
      IPv4Address iPv4Address = new IPv4Address();
      iPv4Address.setValue(ipv4);
      ipAddress.setIpv4(iPv4Address);
    }
    if(ipv6 != null) {
      IPv6Address iPv6Address = new IPv6Address();
      iPv6Address.setValue(ipv6);
      ipAddress.setIpv6(iPv6Address);
    }
    return ipAddress;
  }

  public static IPAddressOrFQDN createIpAddressOrFqdn(String ipv4, String ipv6, String fqdn) {
    IPAddressOrFQDN ipAddress = new IPAddressOrFQDN();
    if(ipv4 != null) {
      IPv4Address iPv4Address = new IPv4Address();
      iPv4Address.setValue(ipv4);
      ipAddress.setIpv4(iPv4Address);
    }
    if(ipv6 != null) {
      IPv6Address iPv6Address = new IPv6Address();
      iPv6Address.setValue(ipv6);
      ipAddress.setIpv6(iPv6Address);
    }
    if(fqdn != null) {
      FQDN fqdn1 = new FQDN();
      fqdn1.setValue(fqdn);
      ipAddress.setFqdn(fqdn1);
    }
    return ipAddress;
  }

  public static GenesisInterfaceProto.UpdateCategoryAssociationsRet getUpdateCategoryAssociationsRet() {
    GenesisInterfaceProto.UpdateCategoryAssociationsRet updateCategoryAssociationsRet = GenesisInterfaceProto.UpdateCategoryAssociationsRet.
      newBuilder().setTaskUuid(UuidUtils.getByteStringFromUUID(TASK_UUID)).build();
    return updateCategoryAssociationsRet;
  }

  public static CategoryEntityReferences getUpdateCategoriesParams(String categoryUuid){
    CategoryEntityReferences params = new CategoryEntityReferences();
    List<String> categories = new ArrayList<>();
    if(categoryUuid != null){
      categories.add(categoryUuid);
      params.setCategories(categories);
    }
    params.setCategories(categories);
    return params;
  }

  public static InsightsInterfaceProto.GetEntitiesWithMetricsRet buildGetEntitiesWithMetricsRetForCategories(String clusterUuid){

    List<String> categories = new ArrayList<>();
    categories.add(TestConstantUtils.randomUUIDString());

    String abac_entity_uuid = TestConstantUtils.randomUUIDString();

    InsightsInterfaceProto.MetricData metric1 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("kind")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue("cluster")
            .build())
          .build())
        .build();

    InsightsInterfaceProto.MetricData metric2 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("kind_id")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder()
            .setStrValue(clusterUuid)
            .build())
          .build())
        .build();

    InsightsInterfaceProto.MetricData metric3 =
      InsightsInterfaceProto.MetricData.newBuilder()
        .setName("category_id_list")
        .addValueList(InsightsInterfaceProto.TimeValuePair.newBuilder()
          .setValue(InsightsInterfaceProto.DataValue.newBuilder().setStrList(StrList.newBuilder().addAllValueList(categories))
            .build())
          .build())
        .build();

    return InsightsInterfaceProto.GetEntitiesWithMetricsRet.newBuilder().setTotalGroupCount(1)
      .addGroupResultsList(InsightsInterfaceProto.QueryGroupResult.newBuilder().
        addRawResults(
          InsightsInterfaceProto.EntityWithMetric.newBuilder()
            .setEntityGuid(
              InsightsInterfaceProto.EntityGuid.newBuilder().
                setEntityId(abac_entity_uuid).
                setEntityTypeName("abac_entity_capability").
                build())
            .addMetricDataList(metric1)
            .addMetricDataList(metric2)
            .addMetricDataList(metric3)
        ).setTotalEntityCount(1).build()).build();

  }

  public static GenesisInterfaceProto.GetConfigCredentialsRet getConfigCredentialsRet() {
    GenesisInterfaceProto.GetConfigCredentialsRet getConfigCredentialsRet = (
      GenesisInterfaceProto.GetConfigCredentialsRet.newBuilder()
        .setError(GenesisInterfaceProto.GenesisGenericError.newBuilder().setErrorType(
          GenesisInterfaceProto.GenesisGenericError.ErrorType.kNoError)
        .setErrorMsg("No Error"))
        .setSmtpCredentials(
          GenesisInterfaceProto.GetConfigCredentialsRet.SmtpCred.newBuilder()
            .setUsername(SMTP_SERVER_USERNAME)
            .setPassword(SMTP_SERVER_PASSWORD)
        )
        .setHttpProxyCredentials(
          GenesisInterfaceProto.GetConfigCredentialsRet.HttpProxyCred.newBuilder()
            .setUsername(HTTP_PROXY_USERNAME)
            .setPassword(HTTP_PROXY_PASSWORD)
        )
        .addSnmpUserCredentials(
          GenesisInterfaceProto.GetConfigCredentialsRet.SnmpUserCred.newBuilder()
            .setUsername(SNMP_USERNAME)
            .setAuthKey(SNMP_AUTH_KEY)
            .setPrivKey(SNMP_PRIV_KEY)
        )
    ).build();
    return getConfigCredentialsRet;
  }

  public static SnmpUser getInvalidSnmpUser(Boolean invalidUsername, Boolean nullPrivType) {
    SnmpUser snmpUser = new SnmpUser();
    if(invalidUsername.equals(true)) {
      snmpUser.setUsername("%._768%%prd");
    }else{
      snmpUser.setUsername(SNMP_USERNAME);
    }
    snmpUser.setPrivKey("testKey");
    if(nullPrivType.equals(true)) {
      snmpUser.setPrivType(null);
    }else{
      snmpUser.setPrivType(SnmpPrivType.AES);
    }
    snmpUser.setExtId(ClusterTestUtils.SNMP_USER_UUID);
    return snmpUser;
  }


  public static ClusterManagementInterfaceProto.DiscoverHostsRet getDiscoverHostsRet() {
    ClusterManagementInterfaceProto.DiscoverHostsRet discoverHostsRet = ClusterManagementInterfaceProto.DiscoverHostsRet.
      newBuilder().setTaskUuid(UuidUtils.getByteStringFromUUID(TASK_UUID)).build();
    return discoverHostsRet;
  }

  public static EnterHostMaintenanceSpec getEnterHostMaintenance(String ipType){
    EnterHostMaintenanceSpec enterHostMaintenanceSpec = new EnterHostMaintenanceSpec();
    IPAddressOrFQDN address = new IPAddressOrFQDN();
    if (ipType.equals("ipv4")) {
      IPv4Address iPv4Address = new IPv4Address();
      iPv4Address.setValue("1.1.1.1");
      address.setIpv4(iPv4Address);
    }
    else if (ipType.equals("ipv6")) {
      IPv6Address iPv6Address = new IPv6Address("fe80::5054:ff:fe04:7866", 128);
      address.setIpv6(iPv6Address);
    }
    else {
      FQDN fqdn = new FQDN("0.centos.pool.ntp.org");
      address.setFqdn(fqdn);
    }
    VcenterInfo vcenterInfo = new VcenterInfo();
    vcenterInfo.setAddress(address);
    VcenterCredentials credentials = new VcenterCredentials();
    credentials.setUsername("testUsername");
    credentials.setPassword("testPassword");
    vcenterInfo.setCredentials(credentials);
    enterHostMaintenanceSpec.setVcenterInfo(vcenterInfo);
    enterHostMaintenanceSpec.setTimeoutSeconds(2L);
    enterHostMaintenanceSpec.setClientKey("testKey");
    return enterHostMaintenanceSpec;
  }

  public static HostMaintenanceCommonSpec getExitHostMaintenance(String ipType){
    HostMaintenanceCommonSpec hostMaintenanceCommonSpec = new HostMaintenanceCommonSpec();
    IPAddressOrFQDN address = new IPAddressOrFQDN();
    if (ipType.equals("ipv4")) {
      IPv4Address iPv4Address = new IPv4Address();
      iPv4Address.setValue("1.1.1.1");
      address.setIpv4(iPv4Address);
    }
    else if (ipType.equals("ipv6")) {
      IPv6Address iPv6Address = new IPv6Address("fe80::5054:ff:fe04:7866", 128);
      address.setIpv6(iPv6Address);
    }
    else {
      FQDN fqdn = new FQDN("0.centos.pool.ntp.org");
      address.setFqdn(fqdn);
    }
    VcenterInfo vcenterInfo = new VcenterInfo();
    vcenterInfo.setAddress(address);
    VcenterCredentials credentials = new VcenterCredentials();
    credentials.setUsername("testUsername");
    credentials.setPassword("testPassword");
    vcenterInfo.setCredentials(credentials);
    hostMaintenanceCommonSpec.setVcenterInfo(vcenterInfo);
    hostMaintenanceCommonSpec.setTimeoutSeconds(2L);
    hostMaintenanceCommonSpec.setClientKey("testKey");
    return hostMaintenanceCommonSpec;
  }

  public static ComputeNonMigratableVmsSpec getNonMigratableVms(String ipType){
    ComputeNonMigratableVmsSpec computeNonMigratableVmsSpec = new ComputeNonMigratableVmsSpec();
    IPAddressOrFQDN address = new IPAddressOrFQDN();
    List<String> hosts = new ArrayList<>();
    String hostUuid1 = TestConstantUtils.randomUUIDString();
    hosts.add(hostUuid1);
    String hostUuid2 = TestConstantUtils.randomUUIDString();
    hosts.add(hostUuid2);
    String hostUuid3 = TestConstantUtils.randomUUIDString();
    hosts.add(hostUuid3);
    computeNonMigratableVmsSpec.setHosts(hosts);
    if (ipType.equals("ipv4")) {
      IPv4Address iPv4Address = new IPv4Address();
      iPv4Address.setValue("1.1.1.1");
      address.setIpv4(iPv4Address);
    }
    else if (ipType.equals("ipv6")) {
      IPv6Address iPv6Address = new IPv6Address("fe80::5054:ff:fe04:7866", 128);
      address.setIpv6(iPv6Address);
    }
    else {
      FQDN fqdn = new FQDN("0.centos.pool.ntp.org");
      address.setFqdn(fqdn);
    }
    VcenterInfo vcenterInfo = new VcenterInfo();
    vcenterInfo.setAddress(address);
    VcenterCredentials credentials = new VcenterCredentials();
    credentials.setUsername("testUsername");
    credentials.setPassword("testPassword");
    vcenterInfo.setCredentials(credentials);
    computeNonMigratableVmsSpec.setVcenterInfo(vcenterInfo);
    return computeNonMigratableVmsSpec;
  }

  public static InsightsInterfaceProto.GetEntitiesWithMetricsRet getNonMigratableVmsResultWithMetricRetWithNoEntity(){
    InsightsInterfaceProto.GetEntitiesWithMetricsRet.Builder getEntitiesWithMetricRet = InsightsInterfaceProto.GetEntitiesWithMetricsRet.newBuilder();
    return getEntitiesWithMetricRet.build();
  }
}
