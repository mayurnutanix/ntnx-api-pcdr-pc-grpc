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
 * REST response for all response codes in API path /prism/v4.0/management/domain-managers/{domainManagerExtId}/backup-targets-new Get operation
 */

@Data
@lombok.extern.slf4j.Slf4j
public class ListBackupTargetsApiResponse implements java.io.Serializable, PriObjectTypeTypedObject {

  

  public ListBackupTargetsApiResponse() {
    this.$objectType = this.initialize$objectType();
    this.$reserved = new java.util.LinkedHashMap<>();
    this.$reserved.put("$fv", this.initialize$fv());
    this.$unknownFields = new java.util.LinkedHashMap<>();

  }

  @lombok.Builder(builderMethodName = "ListBackupTargetsApiResponseBuilder")
  public ListBackupTargetsApiResponse(com.nutanix.dp1.pri.common.v1.response.ApiResponseMetadata metadata, Object data) {
    this.$objectType = this.initialize$objectType();
    this.$reserved = new java.util.LinkedHashMap<>();
    this.$reserved.put("$fv", this.initialize$fv());
    this.$unknownFields = new java.util.LinkedHashMap<>();
    this.setMetadata(metadata);
    this.setDataInWrapper(data);
  }


  protected String initialize$objectType() {
    return "prism.v4.management.ListBackupTargetsApiResponse";
  }


  protected String initialize$fv() {
    return "v4.r0";
  }

  @JsonAnySetter
  private void setUndeserializedFields(String name, Object value) {
    $unknownFields.put(name, value);
  }


  
  
  
    /**
    * 
    */
    
    
    @JsonProperty("metadata")
    public com.nutanix.dp1.pri.common.v1.response.ApiResponseMetadata metadata = null;
  
  
    
    
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Getter
    private String $dataItemDiscriminator = null;
    
    public String get$dataItemDiscriminator() {
      // not required when setDataInWrapper is renamed to setData
      if (this.data != null && this.$dataItemDiscriminator == null) {
        this.$dataItemDiscriminator = this.data.getDiscriminator();
      }
      return this.$dataItemDiscriminator;
    }
    
    @Data
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = OneOfDataWrapper.OneOfDataWrapperJsonDeserializer.class)
    public static class OneOfDataWrapper {
    
      private static final java.util.List EMPTY_LIST = new java.util.ArrayList<>();
    
      public OneOfDataWrapper() {
      }
    
      public OneOfDataWrapper(java.util.List data) {
        if(data.isEmpty()) {
          this.discriminator = "EMPTY_LIST";
          this.$objectType = "EMPTY_LIST";
          return;
        }
        Class<?> cls = data.get(0).getClass();
        if(com.nutanix.dp1.pri.prism.v4.management.BackupTarget.class.equals(cls)) {
          this.oneOfType31007 = (java.util.List<com.nutanix.dp1.pri.prism.v4.management.BackupTarget>) data;
          this.discriminator = "List<prism.v4.management.BackupTarget>";
          this.$objectType = "List<prism.v4.management.BackupTarget>";
          return;
        }
        throw new IllegalArgumentException("Attempting to set unsupported object type in OneOfDataWrapper: List<" + cls.getName() + ">");
      }
    
    
      public OneOfDataWrapper(com.nutanix.dp1.pri.prism.v4.error.ErrorResponse data) {
        this.oneOfType400 = data;
        this.discriminator = data.get$objectType();
        this.$objectType = data.get$objectType();
      }
    
      @com.nutanix.dp1.pri.annotations.PriJsonDeserializer
      private static class OneOfDataWrapperJsonDeserializer extends com.nutanix.dp1.pri.deserializers.PriOneOfDeserializer<OneOfDataWrapper>  {
        private static final com.fasterxml.jackson.databind.type.TypeFactory TYPE_FACTORY = com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance();
        private static final com.fasterxml.jackson.databind.JavaType ONE_OF_TYPE31007 = TYPE_FACTORY.constructCollectionType(java.util.List.class, com.nutanix.dp1.pri.prism.v4.management.BackupTarget.class);
        private static final com.fasterxml.jackson.databind.JavaType ONE_OF_TYPE400 = TYPE_FACTORY.constructType(com.nutanix.dp1.pri.prism.v4.error.ErrorResponse.class);
    
        public OneOfDataWrapperJsonDeserializer() {
          super(TYPE_FACTORY.constructType(OneOfDataWrapper.class));
        }
    
        @Override
        protected void setDataObject(OneOfDataWrapper oneOfObject, Object nestedObject) {
          if (oneOfObject == null) {
            throw new IllegalArgumentException("Instance of OneOfDataWrapper provided is null");
          }
          if(ONE_OF_TYPE31007.getRawClass().isAssignableFrom(nestedObject.getClass())) {
            oneOfObject.setValue(nestedObject);
            if (oneOfObject.getValue() != null) {
                return;
            }
          }
          if(ONE_OF_TYPE400.getRawClass().isAssignableFrom(nestedObject.getClass())) {
            oneOfObject.setValue(nestedObject);
            if (oneOfObject.getValue() != null) {
                return;
            }
          }
    
          throw new IllegalArgumentException("Attempting to set unsupported object type in OneOfDataWrapper:" + nestedObject.getClass().getName());
        }
    
        public String getPackagePrefix() {
          return "com.nutanix.dp1.pri";
        }
      }
    
      @com.fasterxml.jackson.annotation.JsonIgnore
      @lombok.Getter(lombok.AccessLevel.NONE)
      @lombok.Setter(lombok.AccessLevel.NONE)
      @javax.validation.constraints.Size(max = 4)
      private java.util.List<com.nutanix.dp1.pri.prism.v4.management.BackupTarget> oneOfType31007;
      @com.fasterxml.jackson.annotation.JsonIgnore
      @lombok.Getter(lombok.AccessLevel.NONE)
      @lombok.Setter(lombok.AccessLevel.NONE)
      private com.nutanix.dp1.pri.prism.v4.error.ErrorResponse oneOfType400;
    
      @lombok.Setter(lombok.AccessLevel.NONE)
      private String discriminator;
    
    
      @com.fasterxml.jackson.annotation.JsonIgnore
      @lombok.Setter(lombok.AccessLevel.NONE)
      private String $objectType;
    
      @com.fasterxml.jackson.annotation.JsonGetter
      public Object getValue() {
        if("EMPTY_LIST".equals(this.discriminator)) {
          return EMPTY_LIST;
        }
        if("List<prism.v4.management.BackupTarget>".equals(this.discriminator)) {
          return this.oneOfType31007;
        }
        if(oneOfType400 != null && oneOfType400.get$objectType().equals(this.discriminator)) {
          return this.oneOfType400;
        }
        throw new IllegalArgumentException("Unrecognized discriminator:" + this.discriminator);
      }
    
      public void setValue(Object value) {
        if(value == null) {
          log.warn("null passed to setValue function. OneOf's value will not be set.");
          return;
        }
    
        if(setOneOfType31007(value)) {
          return;
        }
        if (value instanceof com.nutanix.dp1.pri.prism.v4.error.ErrorResponse) {
          this.oneOfType400 = (com.nutanix.dp1.pri.prism.v4.error.ErrorResponse) value;
          this.discriminator = this.oneOfType400.get$objectType();
          this.$objectType = this.oneOfType400.get$objectType();
          return;
        }
    
        throw new IllegalArgumentException("Attempting to set unsupported object type in OneOfDataWrapper:" + value.getClass().getName());
    
      }
    
      private boolean setOneOfType31007(Object value) {
        Class valueClass = com.nutanix.dp1.pri.prism.v4.management.BackupTarget.class;
        if(java.util.List.class.isAssignableFrom(value.getClass())) {
          // list try to match types if list is not empty.
          if (((java.util.List) value).size() > 0) { 
            if (valueClass.equals(((java.util.List) value).get(0).getClass())) {
              this.discriminator = "List<prism.v4.management.BackupTarget>";
              this.$objectType = "List<prism.v4.management.BackupTarget>";
              this.oneOfType31007 = (java.util.List<com.nutanix.dp1.pri.prism.v4.management.BackupTarget>) value;
              return true;
            }
            return false;
          } else {
            this.discriminator = "EMPTY_LIST";
            this.$objectType = "EMPTY_LIST";
            this.oneOfType31007 = (List<com.nutanix.dp1.pri.prism.v4.management.BackupTarget>) value;
            return true;
          }
        }
        return false;
      }
    
    }
  
    private OneOfDataWrapper data = null;
  
    /**
     * @param value value of one of field data
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public void setDataInWrapper(Object value) {
      if (value == null) {
        return;
      }
      if (this.data == null) {
        this.data = new OneOfDataWrapper();
      }
      this.data.setValue(value);
      this.$dataItemDiscriminator = this.data.getDiscriminator();
    }
  
    /**
     * Get data in one of possible types :
     * <ul>
     * <li>EMPTY_LIST</li>
     * <li>List(prism.v4.management.BackupTarget)</li>
     * <li>com.nutanix.dp1.pri.prism.v4.error.ErrorResponse</li>
     * </ul>
     * @return Object
     */
    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.nutanix.dp1.pri.serializers.PriOneOfSerializer.class)
    public Object getData() {
      if (this.data == null) {
        log.debug("OneOf property data was never set. Returning null...");
        return null;
      }
      return handleEtag(this.data.getValue(), this.get$reserved()); 
    }
  
  
  
    
    
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
