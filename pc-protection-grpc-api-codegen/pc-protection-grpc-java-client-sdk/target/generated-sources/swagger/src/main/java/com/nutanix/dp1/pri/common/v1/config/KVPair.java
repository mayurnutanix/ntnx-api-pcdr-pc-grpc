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


/**
 * A map describing a set of keys and their corresponding values.
 * 
 */

@Data
@lombok.extern.slf4j.Slf4j
public class KVPair implements java.io.Serializable, PriObjectTypeTypedObject {

  

  public KVPair() {
    this.$objectType = this.initialize$objectType();
    this.$reserved = new java.util.LinkedHashMap<>();
    this.$reserved.put("$fv", this.initialize$fv());
    this.$unknownFields = new java.util.LinkedHashMap<>();

  }

  @lombok.Builder(builderMethodName = "KVPairBuilder")
  public KVPair(String name, Object value) {
    this.$objectType = this.initialize$objectType();
    this.$reserved = new java.util.LinkedHashMap<>();
    this.$reserved.put("$fv", this.initialize$fv());
    this.$unknownFields = new java.util.LinkedHashMap<>();
    this.setName(name);
    this.setValueInWrapper(value);
  }


  protected String initialize$objectType() {
    return "common.v1.config.KVPair";
  }


  protected String initialize$fv() {
    return "v1.r0.b1";
  }

  @JsonAnySetter
  private void setUndeserializedFields(String name, String value) {
    $unknownFields.put(name, value);
  }


  
  
  
    /**
    * The key of the key-value pair.

    */
    @javax.validation.constraints.Size(min = 3, max = 128)
    
    
    @JsonProperty("name")
    public String name = null;
  
  
    
    
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Getter
    private String $valueItemDiscriminator = null;
    
    public String get$valueItemDiscriminator() {
      // not required when setValueInWrapper is renamed to setValue
      if (this.value != null && this.$valueItemDiscriminator == null) {
        this.$valueItemDiscriminator = this.value.getDiscriminator();
      }
      return this.$valueItemDiscriminator;
    }
    
    @Data
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = OneOfValueWrapper.OneOfValueWrapperJsonDeserializer.class)
    public static class OneOfValueWrapper {
    
      private static final java.util.List EMPTY_LIST = new java.util.ArrayList<>();
      private static final java.util.Map EMPTY_MAP = java.util.Collections.emptyMap();
    
      public OneOfValueWrapper() {
      }
    
      public OneOfValueWrapper(java.util.List data) {
        if(data.isEmpty()) {
          this.discriminator = "EMPTY_LIST";
          this.$objectType = "EMPTY_LIST";
          return;
        }
        Class<?> cls = data.get(0).getClass();
        if(String.class.equals(cls)) {
          this.oneOfType1005 = (java.util.List<String>) data;
          this.discriminator = "List<String>";
          this.$objectType = "List<String>";
          return;
        }
        if(com.nutanix.dp1.pri.common.v1.config.MapOfStringWrapper.class.equals(cls)) {
          this.oneOfType1007 = (java.util.List<com.nutanix.dp1.pri.common.v1.config.MapOfStringWrapper>) data;
          this.discriminator = "List<common.v1.config.MapOfStringWrapper>";
          this.$objectType = "List<common.v1.config.MapOfStringWrapper>";
          return;
        }
        if(Integer.class.equals(cls)) {
          this.oneOfType1008 = (java.util.List<Integer>) data;
          this.discriminator = "List<Integer>";
          this.$objectType = "List<Integer>";
          return;
        }
        throw new IllegalArgumentException("Attempting to set unsupported object type in OneOfValueWrapper: List<" + cls.getName() + ">");
      }
    
      public OneOfValueWrapper(java.util.Map data) {
        if(data.isEmpty()) {
          this.discriminator = "EMPTY_MAP";
          this.$objectType = "EMPTY_MAP";
          return;
        }
        Class<?> cls = data.values().toArray()[0].getClass();
        if(String.class.equals(cls)) {
          this.oneOfType1006 = (java.util.Map<String, String>) data;
          this.discriminator = "Map<String, String>";
          this.$objectType = "Map<String, String>";
          return;
        }
        throw new IllegalArgumentException("Attempting to set unsupported object type in OneOfValueWrapper: Map<String, " + cls.getName() + ">");
      }
    
      public OneOfValueWrapper(String data) {
        this.oneOfType1002 = data;
        this.discriminator = "String";
        this.$objectType = "String";
      }
      public OneOfValueWrapper(Integer data) {
        this.oneOfType1003 = data;
        this.discriminator = "Integer";
        this.$objectType = "Integer";
      }
      public OneOfValueWrapper(Boolean data) {
        this.oneOfType1004 = data;
        this.discriminator = "Boolean";
        this.$objectType = "Boolean";
      }
    
      @com.nutanix.dp1.pri.annotations.PriJsonDeserializer
      private static class OneOfValueWrapperJsonDeserializer extends com.nutanix.dp1.pri.deserializers.PriOneOfDeserializer<OneOfValueWrapper>  {
        private static final com.fasterxml.jackson.databind.type.TypeFactory TYPE_FACTORY = com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance();
        private static final com.fasterxml.jackson.databind.JavaType ONE_OF_TYPE1002 = TYPE_FACTORY.constructType(String.class);
        private static final com.fasterxml.jackson.databind.JavaType ONE_OF_TYPE1003 = TYPE_FACTORY.constructType(Integer.class);
        private static final com.fasterxml.jackson.databind.JavaType ONE_OF_TYPE1004 = TYPE_FACTORY.constructType(Boolean.class);
        private static final com.fasterxml.jackson.databind.JavaType ONE_OF_TYPE1005 = TYPE_FACTORY.constructCollectionType(java.util.List.class, String.class);
        private static final com.fasterxml.jackson.databind.JavaType ONE_OF_TYPE1006 = TYPE_FACTORY.constructMapType(java.util.HashMap.class, String.class, String.class);
        private static final com.fasterxml.jackson.databind.JavaType ONE_OF_TYPE1007 = TYPE_FACTORY.constructCollectionType(java.util.List.class, com.nutanix.dp1.pri.common.v1.config.MapOfStringWrapper.class);
        private static final com.fasterxml.jackson.databind.JavaType ONE_OF_TYPE1008 = TYPE_FACTORY.constructCollectionType(java.util.List.class, Integer.class);
    
        public OneOfValueWrapperJsonDeserializer() {
          super(TYPE_FACTORY.constructType(OneOfValueWrapper.class));
        }
    
        @Override
        protected void setDataObject(OneOfValueWrapper oneOfObject, Object nestedObject) {
          if (oneOfObject == null) {
            throw new IllegalArgumentException("Instance of OneOfValueWrapper provided is null");
          }
          if(ONE_OF_TYPE1002.getRawClass().isAssignableFrom(nestedObject.getClass())) {
            oneOfObject.setValue(nestedObject);
            if (oneOfObject.getValue() != null) {
                return;
            }
          }
          if(ONE_OF_TYPE1003.getRawClass().isAssignableFrom(nestedObject.getClass())) {
            oneOfObject.setValue(nestedObject);
            if (oneOfObject.getValue() != null) {
                return;
            }
          }
          if(ONE_OF_TYPE1004.getRawClass().isAssignableFrom(nestedObject.getClass())) {
            oneOfObject.setValue(nestedObject);
            if (oneOfObject.getValue() != null) {
                return;
            }
          }
          if(ONE_OF_TYPE1005.getRawClass().isAssignableFrom(nestedObject.getClass())) {
            oneOfObject.setValue(nestedObject);
            if (oneOfObject.getValue() != null) {
                return;
            }
          }
          if(ONE_OF_TYPE1006.getRawClass().isAssignableFrom(nestedObject.getClass())) {
            oneOfObject.setValue(nestedObject);
            if (oneOfObject.getValue() != null) {
                return;
            }
          }
          if(ONE_OF_TYPE1007.getRawClass().isAssignableFrom(nestedObject.getClass())) {
            oneOfObject.setValue(nestedObject);
            if (oneOfObject.getValue() != null) {
                return;
            }
          }
          if(ONE_OF_TYPE1008.getRawClass().isAssignableFrom(nestedObject.getClass())) {
            oneOfObject.setValue(nestedObject);
            if (oneOfObject.getValue() != null) {
                return;
            }
          }
    
          throw new IllegalArgumentException("Attempting to set unsupported object type in OneOfValueWrapper:" + nestedObject.getClass().getName());
        }
    
        public String getPackagePrefix() {
          return "com.nutanix.dp1.pri";
        }
      }
    
      @com.fasterxml.jackson.annotation.JsonIgnore
      @lombok.Getter(lombok.AccessLevel.NONE)
      @lombok.Setter(lombok.AccessLevel.NONE)
      private String oneOfType1002;
      @com.fasterxml.jackson.annotation.JsonIgnore
      @lombok.Getter(lombok.AccessLevel.NONE)
      @lombok.Setter(lombok.AccessLevel.NONE)
      private Integer oneOfType1003;
      @com.fasterxml.jackson.annotation.JsonIgnore
      @lombok.Getter(lombok.AccessLevel.NONE)
      @lombok.Setter(lombok.AccessLevel.NONE)
      private Boolean oneOfType1004;
      @com.fasterxml.jackson.annotation.JsonIgnore
      @lombok.Getter(lombok.AccessLevel.NONE)
      @lombok.Setter(lombok.AccessLevel.NONE)
      @javax.validation.constraints.Size(min = 0, max = 100)
      private java.util.List<String> oneOfType1005;
      @com.fasterxml.jackson.annotation.JsonIgnore
      @lombok.Getter(lombok.AccessLevel.NONE)
      @lombok.Setter(lombok.AccessLevel.NONE)
      private Map<String, String> oneOfType1006;
      @com.fasterxml.jackson.annotation.JsonIgnore
      @lombok.Getter(lombok.AccessLevel.NONE)
      @lombok.Setter(lombok.AccessLevel.NONE)
      @javax.validation.constraints.Size(min = 0, max = 20)
      private java.util.List<com.nutanix.dp1.pri.common.v1.config.MapOfStringWrapper> oneOfType1007;
      @com.fasterxml.jackson.annotation.JsonIgnore
      @lombok.Getter(lombok.AccessLevel.NONE)
      @lombok.Setter(lombok.AccessLevel.NONE)
      @javax.validation.constraints.Size(min = 0, max = 100)
      private java.util.List<Integer> oneOfType1008;
    
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
        if("EMPTY_MAP".equals(this.discriminator)) {
          return EMPTY_MAP;
        }
        if("String".equals(this.discriminator)) {
          return this.oneOfType1002;
        }
        if("Integer".equals(this.discriminator)) {
          return this.oneOfType1003;
        }
        if("Boolean".equals(this.discriminator)) {
          return this.oneOfType1004;
        }
        if("List<String>".equals(this.discriminator)) {
          return this.oneOfType1005;
        }
        if("Map<String, String>".equals(this.discriminator)) {
          return this.oneOfType1006;
        }
        if("List<common.v1.config.MapOfStringWrapper>".equals(this.discriminator)) {
          return this.oneOfType1007;
        }
        if("List<Integer>".equals(this.discriminator)) {
          return this.oneOfType1008;
        }
        throw new IllegalArgumentException("Unrecognized discriminator:" + this.discriminator);
      }
    
      public void setValue(Object value) {
        if(value == null) {
          log.warn("null passed to setValue function. OneOf's value will not be set.");
          return;
        }
    
        if (value instanceof String) {
          this.oneOfType1002 = (String) value;
          this.discriminator = "String";
          this.$objectType = "String";
          return;
        }
        if (value instanceof Integer) {
          this.oneOfType1003 = (Integer) value;
          this.discriminator = "Integer";
          this.$objectType = "Integer";
          return;
        }
        if (value instanceof Boolean) {
          this.oneOfType1004 = (Boolean) value;
          this.discriminator = "Boolean";
          this.$objectType = "Boolean";
          return;
        }
        if(setOneOfType1005(value)) {
          return;
        }
        if(setOneOfType1006(value)) {
          return;
        }
        if(setOneOfType1007(value)) {
          return;
        }
        if(setOneOfType1008(value)) {
          return;
        }
    
        throw new IllegalArgumentException("Attempting to set unsupported object type in OneOfValueWrapper:" + value.getClass().getName());
    
      }
    
      private boolean setOneOfType1005(Object value) {
        Class valueClass = String.class;
        if(java.util.List.class.isAssignableFrom(value.getClass())) {
          // list try to match types if list is not empty.
          if (((java.util.List) value).size() > 0) { 
            if (valueClass.equals(((java.util.List) value).get(0).getClass())) {
              this.discriminator = "List<String>";
              this.$objectType = "List<String>";
              this.oneOfType1005 = (java.util.List<String>) value;
              return true;
            }
            return false;
          } else {
            this.discriminator = "EMPTY_LIST";
            this.$objectType = "EMPTY_LIST";
            this.oneOfType1005 = (List<String>) value;
            return true;
          }
        }
        return false;
      }
    
      private boolean setOneOfType1006(Object value) {
        Class valueClass = String.class;
        if(java.util.Map.class.isAssignableFrom(value.getClass())) {
          // map try to match types if map is not empty.
          if (!((java.util.Map) value).isEmpty()) {
            // Do some extra type checking
            if (valueClass.equals(((java.util.Map) value).values().toArray()[0].getClass())) {
              this.discriminator = "Map<String, String>";
              this.$objectType = "Map<String, String>";
              this.oneOfType1006 = (Map<String, String>) value;
              return true;
            }
            return false;
          } else {
            this.discriminator = "EMPTY_MAP";
            this.$objectType = "EMPTY_MAP";
            this.oneOfType1006 = (Map<String, String>) value;
            return true;
          }
        }
        return false;
      }
    
      private boolean setOneOfType1007(Object value) {
        Class valueClass = com.nutanix.dp1.pri.common.v1.config.MapOfStringWrapper.class;
        if(java.util.List.class.isAssignableFrom(value.getClass())) {
          // list try to match types if list is not empty.
          if (((java.util.List) value).size() > 0) { 
            if (valueClass.equals(((java.util.List) value).get(0).getClass())) {
              this.discriminator = "List<common.v1.config.MapOfStringWrapper>";
              this.$objectType = "List<common.v1.config.MapOfStringWrapper>";
              this.oneOfType1007 = (java.util.List<com.nutanix.dp1.pri.common.v1.config.MapOfStringWrapper>) value;
              return true;
            }
            return false;
          } else {
            this.discriminator = "EMPTY_LIST";
            this.$objectType = "EMPTY_LIST";
            this.oneOfType1007 = (List<com.nutanix.dp1.pri.common.v1.config.MapOfStringWrapper>) value;
            return true;
          }
        }
        return false;
      }
    
      private boolean setOneOfType1008(Object value) {
        Class valueClass = Integer.class;
        if(java.util.List.class.isAssignableFrom(value.getClass())) {
          // list try to match types if list is not empty.
          if (((java.util.List) value).size() > 0) { 
            if (valueClass.equals(((java.util.List) value).get(0).getClass())) {
              this.discriminator = "List<Integer>";
              this.$objectType = "List<Integer>";
              this.oneOfType1008 = (java.util.List<Integer>) value;
              return true;
            }
            return false;
          } else {
            this.discriminator = "EMPTY_LIST";
            this.$objectType = "EMPTY_LIST";
            this.oneOfType1008 = (List<Integer>) value;
            return true;
          }
        }
        return false;
      }
    
    }
  
    private OneOfValueWrapper value = null;
  
    /**
     * @param value value of one of field value
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public void setValueInWrapper(Object value) {
      if (value == null) {
        return;
      }
      if (this.value == null) {
        this.value = new OneOfValueWrapper();
      }
      this.value.setValue(value);
      this.$valueItemDiscriminator = this.value.getDiscriminator();
    }
  
    /**
     * Get value in one of possible types :
     * <ul>
     * <li>EMPTY_LIST</li>
     * <li>String</li>
     * <li>Integer</li>
     * <li>Boolean</li>
     * <li>List(String)</li>
     * <li>List(common.v1.config.MapOfStringWrapper)</li>
     * <li>List(Integer)</li>
     * </ul>
     * @return Object
     */
    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.nutanix.dp1.pri.serializers.PriOneOfSerializer.class)
    public Object getValue() {
      if (this.value == null) {
        log.debug("OneOf property value was never set. Returning null...");
        return null;
      }

      return this.value.getValue();
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
