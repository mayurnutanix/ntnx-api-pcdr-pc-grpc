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
 * Model which contains the information of the object store endpoint where 
 * backup is present. It contains information like objectstore 
 * endpoint address, endpoint flavour and last sync timestamp.
 * 
 */

@Data
@lombok.extern.slf4j.Slf4j
public class ObjectStoreLocation implements java.io.Serializable, PriObjectTypeTypedObject {

  

  public ObjectStoreLocation() {
    this.$objectType = this.initialize$objectType();
    this.$reserved = new java.util.LinkedHashMap<>();
    this.$reserved.put("$fv", this.initialize$fv());
    this.$unknownFields = new java.util.LinkedHashMap<>();

  }

  @lombok.Builder(builderMethodName = "ObjectStoreLocationBuilder")
  public ObjectStoreLocation(com.nutanix.dp1.pri.prism.v4.management.AWSS3Config providerConfig, com.nutanix.dp1.pri.prism.v4.management.BackupPolicy backupPolicy) {
    this.$objectType = this.initialize$objectType();
    this.$reserved = new java.util.LinkedHashMap<>();
    this.$reserved.put("$fv", this.initialize$fv());
    this.$unknownFields = new java.util.LinkedHashMap<>();
    this.setProviderConfig(providerConfig);
    this.setBackupPolicy(backupPolicy);
  }


  protected String initialize$objectType() {
    return "prism.v4.management.ObjectStoreLocation";
  }


  protected String initialize$fv() {
    return "v4.r0.b1";
  }

  @JsonAnySetter
  private void setUndeserializedFields(String name, String value) {
    $unknownFields.put(name, value);
  }


  
  
  
    /**
    * 
    */
    
    
    @JsonProperty("providerConfig")
    public com.nutanix.dp1.pri.prism.v4.management.AWSS3Config providerConfig = null;
  
  
  
    /**
    * 
    */
    
    
    @JsonProperty("backupPolicy")
    public com.nutanix.dp1.pri.prism.v4.management.BackupPolicy backupPolicy = null;
  
  
  
    
    
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
