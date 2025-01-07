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
 * This schema is generated from SchemaValidationErrorMessage.java
 */

@Data
@JsonFilter("dp1.pri.prism.v4.error.SchemaValidationErrorMessageFilter")
@lombok.extern.slf4j.Slf4j
public class SchemaValidationErrorMessage implements java.io.Serializable, PriObjectTypeTypedObject {

  

  public SchemaValidationErrorMessage() {
    this.$objectType = this.initialize$objectType();
    this.$reserved = new java.util.LinkedHashMap<>();
    this.$reserved.put("$fv", this.initialize$fv());
    this.$unknownFields = new java.util.LinkedHashMap<>();

  }

  @lombok.Builder(builderMethodName = "SchemaValidationErrorMessageBuilder")
  public SchemaValidationErrorMessage(String location, String message, String attributePath) {
    this.$objectType = this.initialize$objectType();
    this.$reserved = new java.util.LinkedHashMap<>();
    this.$reserved.put("$fv", this.initialize$fv());
    this.$unknownFields = new java.util.LinkedHashMap<>();
    this.setLocation(location);
    this.setMessage(message);
    this.setAttributePath(attributePath);
  }


  protected String initialize$objectType() {
    return "prism.v4.error.SchemaValidationErrorMessage";
  }


  protected String initialize$fv() {
    return "v4.r0";
  }

  @JsonAnySetter
  private void setUndeserializedFields(String name, Object value) {
    $unknownFields.put(name, value);
  }


  
  
  
    /**
    * The part of the request that failed validation. Validation can fail for path, query parameters, and request body.
    */
    
    
    @JsonProperty("location")
    public String location = null;
  
  
  
    /**
    * The detailed message for the validation error.
    */
    
    
    @JsonProperty("message")
    public String message = null;
  
  
  
    /**
    * The path of the attribute that failed validation in the schema.
    */
    
    
    @JsonProperty("attributePath")
    public String attributePath = null;
  
  
  
    
    
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
