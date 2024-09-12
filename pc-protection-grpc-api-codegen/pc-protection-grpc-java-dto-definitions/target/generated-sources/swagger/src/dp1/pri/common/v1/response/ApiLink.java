/*
 * Generated file ..
 *
 * Product version: 0.0.1-SNAPSHOT
 *
 * Part of the PC Protection PC Client SDK
 *
 * (c) 2024 Nutanix Inc.  All rights reserved
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dp1.pri.deserializers.PriDeserializerUtils.*;


/**
 * A HATEOAS style link for the response.  Each link contains a user-friendly name identifying the link and an address for retrieving the particular resource.
 * 
 */

@Data
@JsonFilter("dp1.pri.common.v1.response.ApiLinkFilter")
@lombok.extern.slf4j.Slf4j
public class ApiLink implements java.io.Serializable, PriObjectTypeTypedObject {

  

  public ApiLink() {
    this.$objectType = this.initialize$objectType();
    this.$reserved = new java.util.LinkedHashMap<>();
    this.$reserved.put("$fv", this.initialize$fv());
    this.$unknownFields = new java.util.LinkedHashMap<>();

  }

  @lombok.Builder(builderMethodName = "ApiLinkBuilder")
  public ApiLink(String href, String rel) {
    this.$objectType = this.initialize$objectType();
    this.$reserved = new java.util.LinkedHashMap<>();
    this.$reserved.put("$fv", this.initialize$fv());
    this.$unknownFields = new java.util.LinkedHashMap<>();
    this.setHref(href);
    this.setRel(rel);
  }


  protected String initialize$objectType() {
    return "common.v1.response.ApiLink";
  }


  protected String initialize$fv() {
    return "v1.r0.b1";
  }

  @JsonAnySetter
  private void setUndeserializedFields(String name, String value) {
    $unknownFields.put(name, value);
  }


  
  
  
    /**
    * The URL at which the entity described by the link can be accessed.

    */
    
    
    @JsonProperty("href")
    public String href = null;
  
  
  
    /**
    * A name that identifies the relationship of the link to the object that is returned by the URL.  The unique value of "self" identifies the URL for the object.

    */
    
    
    @JsonProperty("rel")
    public String rel = null;
  
  
  
    
    
    @Getter
    
    @JsonView({StandardView.class})
    
    protected final Map<String, Object> $reserved;
  
  
  
    
    
    @Getter
    
    @JsonView({StandardView.class})
    
    protected final String $objectType;
  
  
  
    
    
    @Getter
    
    @JsonView({StandardView.class})
    
    protected final Map<String, Object> $unknownFields;
  
  

}
