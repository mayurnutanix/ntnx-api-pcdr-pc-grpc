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
 * An error response indicates that the operation has failed either due to a client error(4XX) or server error(5XX). Please look at the HTTP status code and namespace specific error code and error message for further details.
 */

@Data
@JsonFilter("dp1.pri.prism.v4.error.ErrorResponseFilter")
@lombok.extern.slf4j.Slf4j
public class ErrorResponse implements java.io.Serializable, PriObjectTypeTypedObject {

  

  public ErrorResponse() {
    this.$objectType = this.initialize$objectType();
    this.$reserved = new java.util.LinkedHashMap<>();
    this.$reserved.put("$fv", this.initialize$fv());
    this.$unknownFields = new java.util.LinkedHashMap<>();

  }

  @lombok.Builder(builderMethodName = "ErrorResponseBuilder")
  public ErrorResponse(Object error) {
    this.$objectType = this.initialize$objectType();
    this.$reserved = new java.util.LinkedHashMap<>();
    this.$reserved.put("$fv", this.initialize$fv());
    this.$unknownFields = new java.util.LinkedHashMap<>();
    this.setErrorInWrapper(error);
  }


  protected String initialize$objectType() {
    return "prism.v4.error.ErrorResponse";
  }


  protected String initialize$fv() {
    return "v4.r0.b1";
  }

  @JsonAnySetter
  private void setUndeserializedFields(String name, String value) {
    $unknownFields.put(name, value);
  }


  
  
    
    
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Getter
    private String $errorItemDiscriminator = null;
    
    public String get$errorItemDiscriminator() {
      // not required when setErrorInWrapper is renamed to setError
      if (this.error != null && this.$errorItemDiscriminator == null) {
        this.$errorItemDiscriminator = this.error.getDiscriminator();
      }
      return this.$errorItemDiscriminator;
    }
    
    @Data
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = OneOfErrorWrapper.OneOfErrorWrapperJsonDeserializer.class)
    public static class OneOfErrorWrapper {
    
      private static final java.util.List EMPTY_LIST = new java.util.ArrayList<>();
    
      public OneOfErrorWrapper() {
      }
    
      public OneOfErrorWrapper(java.util.List data) {
        if(data.isEmpty()) {
          this.discriminator = "EMPTY_LIST";
          this.$objectType = "EMPTY_LIST";
          return;
        }
        Class<?> cls = data.get(0).getClass();
        if(dp1.pri.prism.v4.error.AppMessage.class.equals(cls)) {
          this.oneOfType201 = (java.util.List<dp1.pri.prism.v4.error.AppMessage>) data;
          this.discriminator = "List<prism.v4.error.AppMessage>";
          this.$objectType = "List<prism.v4.error.AppMessage>";
          return;
        }
        throw new IllegalArgumentException("Attempting to set unsupported object type in OneOfErrorWrapper: List<" + cls.getName() + ">");
      }
    
    
      public OneOfErrorWrapper(dp1.pri.prism.v4.error.SchemaValidationError data) {
        this.oneOfType202 = data;
        this.discriminator = data.get$objectType();
        this.$objectType = data.get$objectType();
      }
    
      @dp1.pri.annotations.PriJsonDeserializer
      private static class OneOfErrorWrapperJsonDeserializer extends dp1.pri.deserializers.PriOneOfDeserializer<OneOfErrorWrapper>  {
        private static final com.fasterxml.jackson.databind.type.TypeFactory TYPE_FACTORY = com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance();
        private static final com.fasterxml.jackson.databind.JavaType ONE_OF_TYPE201 = TYPE_FACTORY.constructCollectionType(java.util.List.class, dp1.pri.prism.v4.error.AppMessage.class);
        private static final com.fasterxml.jackson.databind.JavaType ONE_OF_TYPE202 = TYPE_FACTORY.constructType(dp1.pri.prism.v4.error.SchemaValidationError.class);
    
        public OneOfErrorWrapperJsonDeserializer() {
          super(TYPE_FACTORY.constructType(OneOfErrorWrapper.class));
        }
    
        @Override
        protected void setDataObject(OneOfErrorWrapper oneOfObject, Object nestedObject) {
          if (oneOfObject == null) {
            throw new IllegalArgumentException("Instance of OneOfErrorWrapper provided is null");
          }
          if(ONE_OF_TYPE201.getRawClass().isAssignableFrom(nestedObject.getClass())) {
            oneOfObject.setValue(nestedObject);
            if (oneOfObject.getValue() != null) {
                return;
            }
          }
          if(ONE_OF_TYPE202.getRawClass().isAssignableFrom(nestedObject.getClass())) {
            oneOfObject.setValue(nestedObject);
            if (oneOfObject.getValue() != null) {
                return;
            }
          }
    
          throw new IllegalArgumentException("Attempting to set unsupported object type in OneOfErrorWrapper:" + nestedObject.getClass().getName());
        }
    
        public String getPackagePrefix() {
          return "dp1.pri";
        }
      }
    
      @com.fasterxml.jackson.annotation.JsonIgnore
      @lombok.Getter(lombok.AccessLevel.NONE)
      @lombok.Setter(lombok.AccessLevel.NONE)
      private java.util.List<dp1.pri.prism.v4.error.AppMessage> oneOfType201;
      @com.fasterxml.jackson.annotation.JsonIgnore
      @lombok.Getter(lombok.AccessLevel.NONE)
      @lombok.Setter(lombok.AccessLevel.NONE)
      private dp1.pri.prism.v4.error.SchemaValidationError oneOfType202;
    
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
        if("List<prism.v4.error.AppMessage>".equals(this.discriminator)) {
          return this.oneOfType201;
        }
        if(oneOfType202 != null && oneOfType202.get$objectType().equals(this.discriminator)) {
          return this.oneOfType202;
        }
        throw new IllegalArgumentException("Unrecognized discriminator:" + this.discriminator);
      }
    
      public void setValue(Object value) {
        if(value == null) {
          log.warn("null passed to setValue function. OneOf's value will not be set.");
          return;
        }
    
        if(setOneOfType201(value)) {
          return;
        }
        if (value instanceof dp1.pri.prism.v4.error.SchemaValidationError) {
          this.oneOfType202 = (dp1.pri.prism.v4.error.SchemaValidationError) value;
          this.discriminator = this.oneOfType202.get$objectType();
          this.$objectType = this.oneOfType202.get$objectType();
          return;
        }
    
        throw new IllegalArgumentException("Attempting to set unsupported object type in OneOfErrorWrapper:" + value.getClass().getName());
    
      }
    
      private boolean setOneOfType201(Object value) {
        Class valueClass = dp1.pri.prism.v4.error.AppMessage.class;
        if(java.util.List.class.isAssignableFrom(value.getClass())) {
          // list try to match types if list is not empty.
          if (((java.util.List) value).size() > 0) { 
            if (valueClass.equals(((java.util.List) value).get(0).getClass())) {
              this.discriminator = "List<prism.v4.error.AppMessage>";
              this.$objectType = "List<prism.v4.error.AppMessage>";
              this.oneOfType201 = (java.util.List<dp1.pri.prism.v4.error.AppMessage>) value;
              return true;
            }
            return false;
          } else {
            this.discriminator = "EMPTY_LIST";
            this.$objectType = "EMPTY_LIST";
            this.oneOfType201 = (List<dp1.pri.prism.v4.error.AppMessage>) value;
            return true;
          }
        }
        return false;
      }
    
    }
  
    private OneOfErrorWrapper error = null;
  
    /**
     * @param value value of one of field error
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public void setErrorInWrapper(Object value) {
      if (value == null) {
        return;
      }
      if (this.error == null) {
        this.error = new OneOfErrorWrapper();
      }
      this.error.setValue(value);
      this.$errorItemDiscriminator = this.error.getDiscriminator();
    }
  
    /**
     * Get error in one of possible types :
     * <ul>
     * <li>EMPTY_LIST</li>
     * <li>List(prism.v4.error.AppMessage)</li>
     * <li>dp1.pri.prism.v4.error.SchemaValidationError</li>
     * </ul>
     * @return Object
     */
    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = dp1.pri.serializers.PriOneOfSerializer.class)
    public Object getError() {
      if (this.error == null) {
        log.debug("OneOf property error was never set. Returning null...");
        return null;
      }

      return this.error.getValue();
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
