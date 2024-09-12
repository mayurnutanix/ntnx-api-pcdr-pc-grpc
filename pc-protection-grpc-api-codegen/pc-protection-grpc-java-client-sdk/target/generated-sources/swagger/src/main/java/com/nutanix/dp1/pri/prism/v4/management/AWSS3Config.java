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

import java.util.List;
import java.util.Map;

import static com.nutanix.dp1.pri.deserializers.PriDeserializerUtils.*;


/**
 * The base model of S3 object store endpoint where domain manager is backed up.
 * 
 */
@EqualsAndHashCode(callSuper=true)
@Data
@lombok.extern.slf4j.Slf4j
public class AWSS3Config extends com.nutanix.dp1.pri.prism.v4.management.S3Config implements java.io.Serializable, PriObjectTypeTypedObject {

  

  public AWSS3Config() {
    super();

  }

  @lombok.Builder(builderMethodName = "AWSS3ConfigBuilder")
  public AWSS3Config(String bucketName, String region, Object credentials) {
    super(bucketName, region);
    this.setCredentialsInWrapper(credentials);
  }

  @Override 
  protected String initialize$objectType() {
    return "prism.v4.management.AWSS3Config";
  }

  @Override 
  protected String initialize$fv() {
    return "v4.r0.b1";
  }


  
  
    
    
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Getter
    private String $credentialsItemDiscriminator = null;
    
    public String get$credentialsItemDiscriminator() {
      // not required when setCredentialsInWrapper is renamed to setCredentials
      if (this.credentials != null && this.$credentialsItemDiscriminator == null) {
        this.$credentialsItemDiscriminator = this.credentials.getDiscriminator();
      }
      return this.$credentialsItemDiscriminator;
    }
    
    @Data
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = OneOfCredentialsWrapper.OneOfCredentialsWrapperJsonDeserializer.class)
    public static class OneOfCredentialsWrapper {
    
    
      public OneOfCredentialsWrapper() {
      }
    
    
    
      public OneOfCredentialsWrapper(com.nutanix.dp1.pri.prism.v4.management.AccessKeyCredentials data) {
        this.oneOfType12001 = data;
        this.discriminator = data.get$objectType();
        this.$objectType = data.get$objectType();
      }
    
      @com.nutanix.dp1.pri.annotations.PriJsonDeserializer
      private static class OneOfCredentialsWrapperJsonDeserializer extends com.nutanix.dp1.pri.deserializers.PriOneOfDeserializer<OneOfCredentialsWrapper>  {
        private static final com.fasterxml.jackson.databind.type.TypeFactory TYPE_FACTORY = com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance();
        private static final com.fasterxml.jackson.databind.JavaType ONE_OF_TYPE12001 = TYPE_FACTORY.constructType(com.nutanix.dp1.pri.prism.v4.management.AccessKeyCredentials.class);
    
        public OneOfCredentialsWrapperJsonDeserializer() {
          super(TYPE_FACTORY.constructType(OneOfCredentialsWrapper.class));
        }
    
        @Override
        protected void setDataObject(OneOfCredentialsWrapper oneOfObject, Object nestedObject) {
          if (oneOfObject == null) {
            throw new IllegalArgumentException("Instance of OneOfCredentialsWrapper provided is null");
          }
          if(ONE_OF_TYPE12001.getRawClass().isAssignableFrom(nestedObject.getClass())) {
            oneOfObject.setValue(nestedObject);
            if (oneOfObject.getValue() != null) {
                return;
            }
          }
    
          throw new IllegalArgumentException("Attempting to set unsupported object type in OneOfCredentialsWrapper:" + nestedObject.getClass().getName());
        }
    
        public String getPackagePrefix() {
          return "com.nutanix.dp1.pri";
        }
      }
    
      @com.fasterxml.jackson.annotation.JsonIgnore
      @lombok.Getter(lombok.AccessLevel.NONE)
      @lombok.Setter(lombok.AccessLevel.NONE)
      private com.nutanix.dp1.pri.prism.v4.management.AccessKeyCredentials oneOfType12001;
    
      @lombok.Setter(lombok.AccessLevel.NONE)
      private String discriminator;
    
    
      @com.fasterxml.jackson.annotation.JsonIgnore
      @lombok.Setter(lombok.AccessLevel.NONE)
      private String $objectType;
    
      @com.fasterxml.jackson.annotation.JsonGetter
      public Object getValue() {
        if(oneOfType12001 != null && oneOfType12001.get$objectType().equals(this.discriminator)) {
          return this.oneOfType12001;
        }
        throw new IllegalArgumentException("Unrecognized discriminator:" + this.discriminator);
      }
    
      public void setValue(Object value) {
        if(value == null) {
          log.warn("null passed to setValue function. OneOf's value will not be set.");
          return;
        }
    
        if (value instanceof com.nutanix.dp1.pri.prism.v4.management.AccessKeyCredentials) {
          this.oneOfType12001 = (com.nutanix.dp1.pri.prism.v4.management.AccessKeyCredentials) value;
          this.discriminator = this.oneOfType12001.get$objectType();
          this.$objectType = this.oneOfType12001.get$objectType();
          return;
        }
    
        throw new IllegalArgumentException("Attempting to set unsupported object type in OneOfCredentialsWrapper:" + value.getClass().getName());
    
      }
    
    }
  
    private OneOfCredentialsWrapper credentials = null;
  
    /**
     * @param value value of one of field credentials
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public void setCredentialsInWrapper(Object value) {
      if (value == null) {
        return;
      }
      if (this.credentials == null) {
        this.credentials = new OneOfCredentialsWrapper();
      }
      this.credentials.setValue(value);
      this.$credentialsItemDiscriminator = this.credentials.getDiscriminator();
    }
  
    /**
     * Get credentials in one of possible types :
     * <ul>
     * <li>com.nutanix.dp1.pri.prism.v4.management.AccessKeyCredentials</li>
     * </ul>
     * @return Object
     */
    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.nutanix.dp1.pri.serializers.PriOneOfSerializer.class)
    public Object getCredentials() {
      if (this.credentials == null) {
        log.debug("OneOf property credentials was never set. Returning null...");
        return null;
      }

      return this.credentials.getValue();
    }
  
  

}
