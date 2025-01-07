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

package com.nutanix.dp1.pri.common.v1.response;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nutanix.dp1.pri.deserializers.PriDeserializerUtils.*;


/**
 * The metadata associated with an API response. This value is always present and minimally contains the self-link for the API request that produced this response. It also contains pagination data for the paginated requests.
 * 
 */

@Data
@lombok.extern.slf4j.Slf4j
public class ApiResponseMetadata implements java.io.Serializable, PriObjectTypeTypedObject {

  

  public ApiResponseMetadata() {
    this.$objectType = this.initialize$objectType();
    this.$reserved = new java.util.LinkedHashMap<>();
    this.$reserved.put("$fv", this.initialize$fv());
    this.$unknownFields = new java.util.LinkedHashMap<>();

  }

  @lombok.Builder(builderMethodName = "ApiResponseMetadataBuilder")
  public ApiResponseMetadata(java.util.List<com.nutanix.dp1.pri.common.v1.config.Flag> flags, java.util.List<com.nutanix.dp1.pri.common.v1.response.ApiLink> links, Integer totalAvailableResults, java.util.List<com.nutanix.dp1.pri.common.v1.config.Message> messages, java.util.List<com.nutanix.dp1.pri.common.v1.config.KVPair> extraInfo) {
    this.$objectType = this.initialize$objectType();
    this.$reserved = new java.util.LinkedHashMap<>();
    this.$reserved.put("$fv", this.initialize$fv());
    this.$unknownFields = new java.util.LinkedHashMap<>();
    this.setFlags(flags);
    this.setLinks(links);
    this.setTotalAvailableResults(totalAvailableResults);
    this.setMessages(messages);
    this.setExtraInfo(extraInfo);
  }


  protected String initialize$objectType() {
    return "common.v1.response.ApiResponseMetadata";
  }


  protected String initialize$fv() {
    return "v1.r0";
  }

  @JsonAnySetter
  private void setUndeserializedFields(String name, Object value) {
    $unknownFields.put(name, value);
  }


  
  
    
    private ApiResponseMetadataFlags flags = new ApiResponseMetadataFlags();
    
    @Data
    public static class ApiResponseMetadataFlags extends com.nutanix.dp1.pri.models.PriAbstractBoundedMap<java.lang.Boolean> {
    
      private final String $objectType = "common.v1.response.ApiResponseMetadata.ApiResponseMetadataflags";
    
      public ApiResponseMetadataFlags() {
        super(java.lang.Boolean.class);
        init(java.util.Arrays.asList("hasError","isPaginated","isTruncated"), false);
      }
    
    }
    
    public java.util.List<com.nutanix.dp1.pri.common.v1.config.Flag> getFlags() {
      if (this.flags == null) {
        this.flags = new ApiResponseMetadataFlags();
      }
      java.util.List<com.nutanix.dp1.pri.common.v1.config.Flag> retVal = new java.util.LinkedList<>();
      this.flags.getKeys().forEach(key -> {
        java.lang.Boolean value = this.flags.get(key);
        com.nutanix.dp1.pri.common.v1.config.Flag item = com.nutanix.dp1.pri.common.v1.config.Flag.FlagBuilder().name(key).value(value).build();
        retVal.add(item);
      });
      return retVal;
    }
    
    public void setFlags(java.util.List<com.nutanix.dp1.pri.common.v1.config.Flag> flags) {
      if (this.flags == null) {
        this.flags = new ApiResponseMetadataFlags();
      }
      if (flags != null) {
        flags.forEach(item -> this.flags.put(item.getName(), item.getValue()));
      }
    }
    
    
  
    /**
    * An array of HATEOAS style links for the response that may also include pagination links for list operations.

    */
    @javax.validation.constraints.Size(min = 0, max = 20)
    
    
    @JsonProperty("links")
    public List<com.nutanix.dp1.pri.common.v1.response.ApiLink> links = null;
  
  
  
    /**
    * The total number of entities that are available on the server for this type.

    */
    
    
    @JsonProperty("totalAvailableResults")
    public Integer totalAvailableResults = null;
  
  
  
    /**
    * Information, Warning or Error messages that might provide additional contextual information related to the operation.

    */
    @javax.validation.constraints.Size(min = 0, max = 20)
    
    
    @JsonProperty("messages")
    public List<com.nutanix.dp1.pri.common.v1.config.Message> messages = null;
  
  
  
    /**
    * An array of entity-specific metadata

    */
    @javax.validation.constraints.Size(min = 0, max = 20)
    
    
    @JsonProperty("extraInfo")
    public List<com.nutanix.dp1.pri.common.v1.config.KVPair> extraInfo = null;
  
  
  
    
    
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
