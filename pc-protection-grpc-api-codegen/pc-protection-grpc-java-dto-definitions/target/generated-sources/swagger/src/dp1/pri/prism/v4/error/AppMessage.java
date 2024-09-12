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

package dp1.pri.prism.v4.error;

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
 * Message with associated severity describing status of the current operation.
 */

@Data
@JsonFilter("dp1.pri.prism.v4.error.AppMessageFilter")
@lombok.extern.slf4j.Slf4j
public class AppMessage implements java.io.Serializable, PriObjectTypeTypedObject {

  

  public AppMessage() {
    this.$objectType = this.initialize$objectType();
    this.$reserved = new java.util.LinkedHashMap<>();
    this.$reserved.put("$fv", this.initialize$fv());
    this.$unknownFields = new java.util.LinkedHashMap<>();

  }

  @lombok.Builder(builderMethodName = "AppMessageBuilder")
  public AppMessage(String message, dp1.pri.common.v1.config.MessageSeverity severity, String code, String locale, String errorGroup, java.util.Map<String, String> argumentsMap) {
    this.$objectType = this.initialize$objectType();
    this.$reserved = new java.util.LinkedHashMap<>();
    this.$reserved.put("$fv", this.initialize$fv());
    this.$unknownFields = new java.util.LinkedHashMap<>();
    this.setMessage(message);
    this.setSeverity(severity);
    this.setCode(code);
    this.setLocale(locale);
    this.setErrorGroup(errorGroup);
    this.setArgumentsMap(argumentsMap);
  }


  protected String initialize$objectType() {
    return "prism.v4.error.AppMessage";
  }


  protected String initialize$fv() {
    return "v4.r0.b1";
  }

  @JsonAnySetter
  private void setUndeserializedFields(String name, String value) {
    $unknownFields.put(name, value);
  }


  
  
  
    /**
    * The message string.
    */
    
    
    @JsonProperty("message")
    public String message = null;
  
  
  
    /**
    * 
    */
    
    
    @JsonProperty("severity")
    public dp1.pri.common.v1.config.MessageSeverity severity = null;
  
  
  
    /**
    * The code associated with this message.This string is typically prefixed by the namespace the endpoint belongs to. For example: VMM-40000
    */
    
    
    @JsonProperty("code")
    public String code = null;
  
  
  
    /**
    * Locale for this message. The default locale would be 'en-US'.
    */
    
    
    @JsonProperty("locale")
    public String locale = "en_US";
  
  
  
    /**
    * The error group associated with this message of severity ERROR.
    */
    
    
    @JsonProperty("errorGroup")
    public String errorGroup = null;
  
  
  
    /**
    * The map of argument name to value.
    */
    
    
    @JsonProperty("argumentsMap")
    public Map<String, String> argumentsMap = null;
  
  
  
    
    
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
