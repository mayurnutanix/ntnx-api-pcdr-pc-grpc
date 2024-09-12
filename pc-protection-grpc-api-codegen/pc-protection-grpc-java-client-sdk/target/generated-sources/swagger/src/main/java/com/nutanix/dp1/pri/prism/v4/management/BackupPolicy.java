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
 * Backup policy for the object store provided defining the rpo etc.
 */

@Data
@lombok.extern.slf4j.Slf4j
public class BackupPolicy implements java.io.Serializable, PriObjectTypeTypedObject {

  

  public BackupPolicy() {
    this.$objectType = this.initialize$objectType();
    this.$reserved = new java.util.LinkedHashMap<>();
    this.$reserved.put("$fv", this.initialize$fv());
    this.$unknownFields = new java.util.LinkedHashMap<>();

  }

  @lombok.Builder(builderMethodName = "BackupPolicyBuilder")
  public BackupPolicy(Integer rpoInMinutes) {
    this.$objectType = this.initialize$objectType();
    this.$reserved = new java.util.LinkedHashMap<>();
    this.$reserved.put("$fv", this.initialize$fv());
    this.$unknownFields = new java.util.LinkedHashMap<>();
    this.setRpoInMinutes(rpoInMinutes);
  }


  protected String initialize$objectType() {
    return "prism.v4.management.BackupPolicy";
  }


  protected String initialize$fv() {
    return "v4.r0.b1";
  }

  @JsonAnySetter
  private void setUndeserializedFields(String name, String value) {
    $unknownFields.put(name, value);
  }


  
  
  
    /**
    * RPO interval in minutes at which the backup will be taken

    */
    @javax.validation.constraints.Max(value = 1440)
    @javax.validation.constraints.Min(value = 60)
    
    
    @JsonProperty("rpoInMinutes")
    public Integer rpoInMinutes = null;
  
  
  
    
    
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
