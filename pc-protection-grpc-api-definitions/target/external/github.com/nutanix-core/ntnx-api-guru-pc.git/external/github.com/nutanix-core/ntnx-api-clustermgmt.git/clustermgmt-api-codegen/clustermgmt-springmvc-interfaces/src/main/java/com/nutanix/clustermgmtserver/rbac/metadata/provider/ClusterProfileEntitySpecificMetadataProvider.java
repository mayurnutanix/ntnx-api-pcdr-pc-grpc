package com.nutanix.clustermgmtserver.rbac.metadata.provider;

  import com.google.protobuf.ByteString;
  import com.nutanix.clustermgmtserver.utils.rbac.ClustermgmtEntityDbQueryUtil;
  import com.nutanix.insights.ifc.InsightsInterfaceProto;
  import com.nutanix.prism.adapter.service.ApplianceConfiguration;
  import com.nutanix.prism.rbac.MetadataArguments;
  import com.nutanix.prism.rbac.MetadataContextProvider;
  import com.nutanix.prism.rbac.RbacConstants;
  import com.nutanix.prism.rbac.util.RbacUrlUtil;
  import com.nutanix.util.base.UuidUtils;
  import lombok.extern.slf4j.Slf4j;
  import org.springframework.beans.factory.annotation.Autowired;
  import org.springframework.stereotype.Component;

  import java.util.*;

@Slf4j
@Component
public class ClusterProfileEntitySpecificMetadataProvider implements MetadataContextProvider {
  private final ClustermgmtEntityDbQueryUtil entityDbQueryUtil;
  @Autowired
  public ClusterProfileEntitySpecificMetadataProvider(final ClustermgmtEntityDbQueryUtil entityDbQueryUtil) {
    this.entityDbQueryUtil = entityDbQueryUtil;
  }

  @Override
  public Map<String, Object> getMetadata(MetadataArguments metadataArguments, ApplianceConfiguration applianceConfiguration) {
    Map<String, Object> map = new HashMap<>();

    if (metadataArguments == null) {
      log.warn("Null instance of MetadataArguments in 'getMetadata'");
      return map;
    }

    String requestUrl = metadataArguments.getRequestUrl();
    List<String> entityUuidList =
      RbacUrlUtil.getEntityUuidsFromUrl(requestUrl);
    if (entityUuidList.isEmpty()) {
      log.error("Unable to determine the entity uuid from requestUrl {}", requestUrl);
      return map;
    }

    String entityUuid = entityUuidList.get(0);
    log.debug("Entity uuid is {}", entityUuid);

    InsightsInterfaceProto.GetEntitiesRet getEntitiesRet = entityDbQueryUtil.getEntityFromEntityDb("cluster_profile", entityUuid);
    if(getEntitiesRet == null || getEntitiesRet.getEntityCount() == 0) {
      log.debug("Unknown cluster profile uuid {}", entityUuid);
      return map;
    }

    log.debug("RequestUrl {} Entity Uuid {}",
      requestUrl, entityUuid);

    // Cluster profile uuid
    map.put(RbacConstants.UUID_KEY, entityUuid);
    // Owner uuid
    for (InsightsInterfaceProto.NameTimeValuePair attribute : getEntitiesRet.getEntity(0).getAttributeDataMapList()) {
      if (!attribute.hasName() || !attribute.hasValue()) {
        continue;
      }
      if(attribute.getName().equals("created_by")) {
        map.put(RbacConstants.OWNER_KEY, attribute.getValue().getStrValue());
      }
    }
    return map;
  }
}