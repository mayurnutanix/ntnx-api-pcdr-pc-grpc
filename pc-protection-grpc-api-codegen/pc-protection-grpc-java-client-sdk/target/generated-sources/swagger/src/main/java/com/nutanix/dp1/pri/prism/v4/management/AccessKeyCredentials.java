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

package com.nutanix.dp1.pri.prism.v4.management;

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
import com.nutanix.dp1.pri.deserializers.PriObjectTypeTypedObject;

import javax.validation.constraints.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nutanix.dp1.pri.deserializers.PriDeserializerUtils.*;


/**
 * Secret credentials model for the object store containing access key ID and secret access key.
 */

@Data
@lombok.extern.slf4j.Slf4j
public class AccessKeyCredentials implements java.io.Serializable, PriObjectTypeTypedObject {

  

  public AccessKeyCredentials() {
    this.$objectType = this.initialize$objectType();
    this.$reserved = new java.util.LinkedHashMap<>();
    this.$reserved.put("$fv", this.initialize$fv());
    this.$unknownFields = new java.util.LinkedHashMap<>();

  }

  @lombok.Builder(builderMethodName = "AccessKeyCredentialsBuilder")
  public AccessKeyCredentials(String accessKeyId, String secretAccessKey) {
    this.$objectType = this.initialize$objectType();
    this.$reserved = new java.util.LinkedHashMap<>();
    this.$reserved.put("$fv", this.initialize$fv());
    this.$unknownFields = new java.util.LinkedHashMap<>();
    this.setAccessKeyId(accessKeyId);
    this.setSecretAccessKey(secretAccessKey);
  }


  protected String initialize$objectType() {
    return "prism.v4.management.AccessKeyCredentials";
  }


  protected String initialize$fv() {
    return "v4.r0";
  }

  @JsonAnySetter
  private void setUndeserializedFields(String name, Object value) {
    $unknownFields.put(name, value);
  }


  
  
  
    /**
    * Access key Id for the object store provided for backup target.
    */
    @javax.validation.constraints.Size(min = 16, max = 128)
    
    
    @JsonProperty("accessKeyId")
    public String accessKeyId = null;
  
  
  
    /**
    * Secret access key for the object store provided for backup target.

    */
    
    
    @JsonProperty("secretAccessKey")
    public String secretAccessKey = null;
  
  
  
    
    
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
