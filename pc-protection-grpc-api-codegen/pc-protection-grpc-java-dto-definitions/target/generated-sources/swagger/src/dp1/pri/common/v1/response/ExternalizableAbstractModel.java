/*
 * Generated file ..
 *
 * Product version: 17.0.0-SNAPSHOT
 *
 * Part of the PC Protection PC Client SDK
 *
 * (c) 2025 Nutanix Inc.  All rights reserved
 *
 */

package dp1.pri.common.v1.response;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.AccessLevel;
import com.nutanix.devplatform.models.PrettyModeViews.*;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.stream.Collectors;
import com.fasterxml.jackson.annotation.JsonFilter;
import dp1.pri.deserializers.PriObjectTypeTypedObject;

import javax.validation.constraints.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dp1.pri.deserializers.PriDeserializerUtils.*;


/**
 * A model that represents an object instance that is accessible through an API endpoint.  Instances of this type get an extId field that contains the globally unique identifier for that instance.  Externally accessible instances are always tenant aware and, therefore, extend the TenantAwareModel
 * 
 */
@EqualsAndHashCode(callSuper=true)
@Data
@JsonFilter("dp1.pri.common.v1.response.ExternalizableAbstractModelFilter")
@lombok.extern.slf4j.Slf4j
public class ExternalizableAbstractModel extends dp1.pri.common.v1.config.TenantAwareModel implements java.io.Serializable, PriObjectTypeTypedObject {

  

  public ExternalizableAbstractModel() {
    super();

  }

  @lombok.Builder(builderMethodName = "ExternalizableAbstractModelBuilder")
  public ExternalizableAbstractModel(String tenantId, String extId, java.util.List<dp1.pri.common.v1.response.ApiLink> links) {
    super(tenantId);
    this.setExtId(extId);
    this.setLinks(links);
  }

  @Override 
  protected String initialize$objectType() {
    return "common.v1.response.ExternalizableAbstractModel";
  }

  @Override 
  protected String initialize$fv() {
    return "v1.r0";
  }


  
    @javax.validation.constraints.Null
    @javax.validation.constraints.Pattern(regexp="^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$")
    @javax.validation.constraints.NotNull
    
    
    
    
    private String extId = null;
  
    /**
     * A globally unique identifier of an instance that is suitable for external consumption.

     */
    public void setExtId(String extId) {
      if (this.extId == null) {
        this.extId = extId;
      }
      else {
        log.warn("Read-only property extId already contains a non-null value and cannot be set again");
      }
    }
  
    @javax.validation.constraints.Size(min = 0, max = 20)
    
    
    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = dp1.pri.serializers.PriComplexListSerializer.class)
    
    private List<dp1.pri.common.v1.response.ApiLink> links = null;
  
    /**
     * A HATEOAS style link for the response.  Each link contains a user-friendly name identifying the link and an address for retrieving the particular resource.

     */
    public void setLinks(List<dp1.pri.common.v1.response.ApiLink> links) {
      if (this.links == null) {
        this.links = links;
      }
      else {
        log.warn("Read-only property links already contains a non-null value and cannot be set again");
      }
    }
  
  

}
