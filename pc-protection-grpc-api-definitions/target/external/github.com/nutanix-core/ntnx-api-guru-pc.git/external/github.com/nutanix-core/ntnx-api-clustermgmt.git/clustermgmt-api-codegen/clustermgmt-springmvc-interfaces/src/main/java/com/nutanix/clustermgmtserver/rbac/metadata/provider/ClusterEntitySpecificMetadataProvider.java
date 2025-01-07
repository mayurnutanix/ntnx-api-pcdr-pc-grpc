package com.nutanix.clustermgmtserver.rbac.metadata.provider;

import com.nutanix.api.utils.exceptions.PlatformResponseException.RbacAuthorizationException;
import com.nutanix.clustermgmtserver.utils.rbac.ClustermgmtEntityDbQueryUtil;
import com.nutanix.insights.ifc.InsightsInterfaceProto;
import com.nutanix.prism.adapter.service.ApplianceConfiguration;
import com.nutanix.prism.rbac.MetadataArguments;
import com.nutanix.prism.rbac.MetadataContextProvider;
import com.nutanix.prism.rbac.RbacConstants;
import com.nutanix.prism.rbac.util.RbacUrlUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ClusterEntitySpecificMetadataProvider implements MetadataContextProvider{
  private final ClustermgmtEntityDbQueryUtil entityDbQueryUtil;
  @Autowired
  public ClusterEntitySpecificMetadataProvider(final ClustermgmtEntityDbQueryUtil entityDbQueryUtil) {
    this.entityDbQueryUtil = entityDbQueryUtil;
  }

  @Override
  public Map<String, Object> getMetadata(MetadataArguments metadataArguments, ApplianceConfiguration applianceConfiguration) {
    Map<String, Object> map = new HashMap<>();

    if (metadataArguments == null) {
      String errMsg  = "Null instance of MetadataArguments in 'getMetadata'";
      log.warn(errMsg);
      throw new RbacAuthorizationException(errMsg, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    String requestUrl = metadataArguments.getRequestUrl();
    List<String> entityUuidList =
      RbacUrlUtil.getEntityUuidsFromUrl(requestUrl);
    if (entityUuidList.isEmpty()) {
      String errMsg = String.format("Unable to determine the entity uuid from requestUrl %s", requestUrl);
      log.error(errMsg);
      throw new RbacAuthorizationException(errMsg, HttpStatus.BAD_REQUEST);
    }

    String entityUuid = entityUuidList.get(0);
    log.debug("Entity uuid is {}", entityUuid);

    InsightsInterfaceProto.GetEntitiesRet getEntitiesRet = entityDbQueryUtil.getEntityFromEntityDb("cluster", entityUuid);
    if(getEntitiesRet == null || getEntitiesRet.getEntityCount() == 0) {
      log.debug("Unknown cluster uuid {}", entityUuid);
      return map;
    }

    log.debug("RequestUrl {} Entity Uuid {}",
      requestUrl, entityUuid);
    map.put(RbacConstants.UUID_KEY, entityUuid);
    map.put(RbacConstants.CLUSTER_KEY, entityUuid);
    return map;
  }
}
