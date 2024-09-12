package com.nutanix.clustermgmtserver.utils;

import com.nutanix.insights.ifc.InsightsInterfaceProto;
import com.nutanix.util.base.UuidUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClusterTestUtilsForMvc {
  public static final String CLUSTER_UUID =
    "5cafbb40-dfd4-4eaf-ac8d-5ff91a230c7a";

  public static final String TASK_UUID =
    "5cafbb40-dfd4-4eaf-ac8d-5ff91a230c7a";

  public static final String CLUSTER_PROFILE_UUID =
    "5cafbb40-dfd4-4eaf-ac8d-5ff91a230c7a";


  public static final String USER_UUID = "57115848-711b-4f03-b94c-3b3e2e014639";

  public static InsightsInterfaceProto.GetEntitiesRet getClusterIdfEntityObj(String... clusterUuids) {
    List<String> serviceList = new ArrayList<>();
    serviceList.add("AOS");
    List<String> hypervisorTypes = new ArrayList<>();
    hypervisorTypes.add("kKvm");

    InsightsInterfaceProto.GetEntitiesRet.Builder getEntitiesRet = InsightsInterfaceProto.GetEntitiesRet.newBuilder();
    Arrays.stream(clusterUuids).forEach(clusterUuid ->{
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("cluster_name")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrValue("test"))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair2 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("service_list")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrList(InsightsInterfaceProto.DataValue.StrList.newBuilder().addAllValueList(serviceList)))
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
            InsightsInterfaceProto.DataValue.newBuilder().setStrList(InsightsInterfaceProto.DataValue.StrList.newBuilder().addAllValueList(hypervisorTypes)))
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
        .build();

      getEntitiesRet.addEntity(entity);
    });
    return getEntitiesRet.build();
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
            InsightsInterfaceProto.DataValue.newBuilder().setBytesValue(UuidUtils.getByteStringFromUUID(ClusterTestUtilsForMvc.USER_UUID)))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair2 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("entity_list")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setStrList(InsightsInterfaceProto.DataValue.StrList.newBuilder().addAllValueList(entityList)))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair3 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("random")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setBytesValue(UuidUtils.getByteStringFromUUID(ClusterTestUtilsForMvc.USER_UUID)))
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

  public static InsightsInterfaceProto.GetEntitiesRet getClusterProfileIdfEntityObj(String... clusterProfileUuids) {
    InsightsInterfaceProto.GetEntitiesRet.Builder getEntitiesRet = InsightsInterfaceProto.GetEntitiesRet.newBuilder();
    Arrays.stream(clusterProfileUuids).forEach(clusterUuid ->{
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("request_context.user_uuid")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setBytesValue(UuidUtils.getByteStringFromUUID(ClusterTestUtilsForMvc.USER_UUID)))
          .build();
      InsightsInterfaceProto.NameTimeValuePair nameTimeValuePair2 =
        InsightsInterfaceProto.NameTimeValuePair.newBuilder()
          .setName("random")
          .setValue(
            InsightsInterfaceProto.DataValue.newBuilder().setBytesValue(UuidUtils.getByteStringFromUUID(ClusterTestUtilsForMvc.USER_UUID)))
          .build();

      InsightsInterfaceProto.Entity entity = InsightsInterfaceProto.Entity.newBuilder()
        .addAttributeDataMap(nameTimeValuePair)
        .addAttributeDataMap(nameTimeValuePair2)
        .build();

      getEntitiesRet.addEntity(entity);
    });
    return getEntitiesRet.build();
  }
}