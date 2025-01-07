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

package dp1.pri.prism.v4.management;

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
 * REST response for all response codes in API path /prism/v4.0/management/domain-managers/{domainManagerExtId}/backup-targets-new/{extId} Put operation
 */

@Data
@JsonFilter("dp1.pri.prism.v4.management.UpdateBackupTargetApiResponseFilter")
@lombok.extern.slf4j.Slf4j
public class UpdateBackupTargetApiResponse implements java.io.Serializable, PriObjectTypeTypedObject {

  

  public UpdateBackupTargetApiResponse() {
    this.$objectType = this.initialize$objectType();
    this.$reserved = new java.util.LinkedHashMap<>();
    this.$reserved.put("$fv", this.initialize$fv());
    this.$unknownFields = new java.util.LinkedHashMap<>();

  }

  @lombok.Builder(builderMethodName = "UpdateBackupTargetApiResponseBuilder")
  public UpdateBackupTargetApiResponse(dp1.pri.common.v1.response.ApiResponseMetadata metadata, Object data) {
    this.$objectType = this.initialize$objectType();
    this.$reserved = new java.util.LinkedHashMap<>();
    this.$reserved.put("$fv", this.initialize$fv());
    this.$unknownFields = new java.util.LinkedHashMap<>();
    this.setMetadata(metadata);
    this.setDataInWrapper(data);
  }


  protected String initialize$objectType() {
    return "prism.v4.management.UpdateBackupTargetApiResponse";
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
    public dp1.pri.common.v1.response.ApiResponseMetadata metadata = null;
  
  
    
    
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
    
    
      public OneOfDataWrapper() {
      }
    
    
    
      public OneOfDataWrapper(dp1.pri.prism.v4.config.TaskReference data) {
        this.oneOfType31007 = data;
        this.discriminator = data.get$objectType();
        this.$objectType = data.get$objectType();
      }
      public OneOfDataWrapper(dp1.pri.prism.v4.error.ErrorResponse data) {
        this.oneOfType400 = data;
        this.discriminator = data.get$objectType();
        this.$objectType = data.get$objectType();
      }
    
      @dp1.pri.annotations.PriJsonDeserializer
      private static class OneOfDataWrapperJsonDeserializer extends dp1.pri.deserializers.PriOneOfDeserializer<OneOfDataWrapper>  {
        private static final com.fasterxml.jackson.databind.type.TypeFactory TYPE_FACTORY = com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance();
        private static final com.fasterxml.jackson.databind.JavaType ONE_OF_TYPE31007 = TYPE_FACTORY.constructType(dp1.pri.prism.v4.config.TaskReference.class);
        private static final com.fasterxml.jackson.databind.JavaType ONE_OF_TYPE400 = TYPE_FACTORY.constructType(dp1.pri.prism.v4.error.ErrorResponse.class);
    
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
          return "dp1.pri";
        }
      }
    
      @com.fasterxml.jackson.annotation.JsonIgnore
      @lombok.Getter(lombok.AccessLevel.NONE)
      @lombok.Setter(lombok.AccessLevel.NONE)
      private dp1.pri.prism.v4.config.TaskReference oneOfType31007;
      @com.fasterxml.jackson.annotation.JsonIgnore
      @lombok.Getter(lombok.AccessLevel.NONE)
      @lombok.Setter(lombok.AccessLevel.NONE)
      private dp1.pri.prism.v4.error.ErrorResponse oneOfType400;
    
      @lombok.Setter(lombok.AccessLevel.NONE)
      private String discriminator;
    
    
      @com.fasterxml.jackson.annotation.JsonIgnore
      @lombok.Setter(lombok.AccessLevel.NONE)
      private String $objectType;
    
      @com.fasterxml.jackson.annotation.JsonGetter
      public Object getValue() {
        if(oneOfType31007 != null && oneOfType31007.get$objectType().equals(this.discriminator)) {
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
    
        if (value instanceof dp1.pri.prism.v4.config.TaskReference) {
          this.oneOfType31007 = (dp1.pri.prism.v4.config.TaskReference) value;
          this.discriminator = this.oneOfType31007.get$objectType();
          this.$objectType = this.oneOfType31007.get$objectType();
          return;
        }
        if (value instanceof dp1.pri.prism.v4.error.ErrorResponse) {
          this.oneOfType400 = (dp1.pri.prism.v4.error.ErrorResponse) value;
          this.discriminator = this.oneOfType400.get$objectType();
          this.$objectType = this.oneOfType400.get$objectType();
          return;
        }
    
        throw new IllegalArgumentException("Attempting to set unsupported object type in OneOfDataWrapper:" + value.getClass().getName());
    
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
     * <li>dp1.pri.prism.v4.config.TaskReference</li>
     * <li>dp1.pri.prism.v4.error.ErrorResponse</li>
     * </ul>
     * @return Object
     */
    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = dp1.pri.serializers.PriOneOfSerializer.class)
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
