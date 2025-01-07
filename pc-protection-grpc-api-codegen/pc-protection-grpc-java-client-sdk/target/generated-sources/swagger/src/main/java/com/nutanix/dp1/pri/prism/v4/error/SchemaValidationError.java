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

package com.nutanix.dp1.pri.prism.v4.error;

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

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nutanix.dp1.pri.deserializers.PriDeserializerUtils.*;


/**
 * This schema is generated from SchemaValidationError.java
 */

@Data
@lombok.extern.slf4j.Slf4j
public class SchemaValidationError implements java.io.Serializable, PriObjectTypeTypedObject {

  

  public SchemaValidationError() {
    this.$objectType = this.initialize$objectType();
    this.$reserved = new java.util.LinkedHashMap<>();
    this.$reserved.put("$fv", this.initialize$fv());
    this.$unknownFields = new java.util.LinkedHashMap<>();

  }

  @lombok.Builder(builderMethodName = "SchemaValidationErrorBuilder")
  public SchemaValidationError(java.time.OffsetDateTime timestamp, Integer statusCode, String error, String path, java.util.List<com.nutanix.dp1.pri.prism.v4.error.SchemaValidationErrorMessage> validationErrorMessages) {
    this.$objectType = this.initialize$objectType();
    this.$reserved = new java.util.LinkedHashMap<>();
    this.$reserved.put("$fv", this.initialize$fv());
    this.$unknownFields = new java.util.LinkedHashMap<>();
    this.setTimestamp(timestamp);
    this.setStatusCode(statusCode);
    this.setError(error);
    this.setPath(path);
    this.setValidationErrorMessages(validationErrorMessages);
  }


  protected String initialize$objectType() {
    return "prism.v4.error.SchemaValidationError";
  }


  protected String initialize$fv() {
    return "v4.r0";
  }

  @JsonAnySetter
  private void setUndeserializedFields(String name, Object value) {
    $unknownFields.put(name, value);
  }


  
  
  
    /**
    * Timestamp of the response.
    */
    
    
    @JsonProperty("timestamp")
    public OffsetDateTime timestamp = null;
  
  
  
    /**
    * The HTTP status code of the response.
    */
    
    
    @JsonProperty("statusCode")
    public Integer statusCode = null;
  
  
  
    /**
    * The generic error message for the response.
    */
    
    
    @JsonProperty("error")
    public String error = null;
  
  
  
    /**
    * API path on which the request was made.
    */
    
    
    @JsonProperty("path")
    public String path = null;
  
  
  
    /**
    * List of validation error messages
    */
    
    
    @JsonProperty("validationErrorMessages")
    public List<com.nutanix.dp1.pri.prism.v4.error.SchemaValidationErrorMessage> validationErrorMessages = null;
  
  
  
    
    
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
