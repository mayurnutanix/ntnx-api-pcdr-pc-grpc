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

package com.nutanix.dp1.pri.common.v1.config;

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




@Data
@lombok.extern.slf4j.Slf4j
public class Message implements java.io.Serializable, PriObjectTypeTypedObject {

  

  public Message() {
    this.$objectType = this.initialize$objectType();
    this.$reserved = new java.util.LinkedHashMap<>();
    this.$reserved.put("$fv", this.initialize$fv());
    this.$unknownFields = new java.util.LinkedHashMap<>();

  }

  @lombok.Builder(builderMethodName = "MessageBuilder")
  public Message(String code, String message, String locale, com.nutanix.dp1.pri.common.v1.config.MessageSeverity severity) {
    this.$objectType = this.initialize$objectType();
    this.$reserved = new java.util.LinkedHashMap<>();
    this.$reserved.put("$fv", this.initialize$fv());
    this.$unknownFields = new java.util.LinkedHashMap<>();
    this.setCode(code);
    this.setMessage(message);
    this.setLocale(locale);
    this.setSeverity(severity);
  }


  protected String initialize$objectType() {
    return "common.v1.config.Message";
  }


  protected String initialize$fv() {
    return "v1.r0";
  }

  @JsonAnySetter
  private void setUndeserializedFields(String name, Object value) {
    $unknownFields.put(name, value);
  }


  
  
  
    /**
    * A code that uniquely identifies a message.

    */
    
    
    @JsonProperty("code")
    public String code = null;
  
  
  
    /**
    * The description of the message.

    */
    
    
    @JsonProperty("message")
    public String message = null;
  
  
  
    /**
    * The locale for the message description.

    */
    
    
    @JsonProperty("locale")
    public String locale = "en_US";
  
  
  
    /**
    * 
    */
    
    
    @JsonProperty("severity")
    public com.nutanix.dp1.pri.common.v1.config.MessageSeverity severity = null;
  
  
  
    
    
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
