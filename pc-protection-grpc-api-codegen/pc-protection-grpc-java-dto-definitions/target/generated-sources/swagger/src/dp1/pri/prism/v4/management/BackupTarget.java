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

import java.time.OffsetDateTime;
import java.util.List;

import static dp1.pri.deserializers.PriDeserializerUtils.*;


/**
 * The backup target for the domain manager, which can be either a cluster or an object store.
 * 
 */
@EqualsAndHashCode(callSuper=true)
@Data
@JsonFilter("dp1.pri.prism.v4.management.BackupTargetFilter")
@lombok.extern.slf4j.Slf4j
public class BackupTarget extends dp1.pri.common.v1.response.ExternalizableAbstractModel implements java.io.Serializable, PriObjectTypeTypedObject {

  

  public BackupTarget() {
    super();

  }

  @lombok.Builder(builderMethodName = "BackupTargetBuilder")
  public BackupTarget(String tenantId, String extId, java.util.List<dp1.pri.common.v1.response.ApiLink> links, Object location, java.time.OffsetDateTime lastSyncTime, Boolean isBackupPaused, String backupPauseReason) {
    super(tenantId, extId, links);
    this.setLocationInWrapper(location);
    this.setLastSyncTime(lastSyncTime);
    this.setIsBackupPaused(isBackupPaused);
    this.setBackupPauseReason(backupPauseReason);
  }

  @Override 
  protected String initialize$objectType() {
    return "prism.v4.management.BackupTarget";
  }

  @Override 
  protected String initialize$fv() {
    return "v4.r0";
  }


  
    
    
    
    
    private OffsetDateTime lastSyncTime = null;
  
    /**
     * Represents the time when the domain manager was last synchronized or copied its 
configuration data to the backup target. This field is updated every 30 minutes.

     */
    public void setLastSyncTime(OffsetDateTime lastSyncTime) {
      if (this.lastSyncTime == null) {
        this.lastSyncTime = lastSyncTime;
      }
      else {
        log.warn("Read-only property lastSyncTime already contains a non-null value and cannot be set again");
      }
    }
  
    
    
    
    
    private Boolean isBackupPaused = null;
  
    /**
     * Whether the backup is paused on the given cluster or not.

     */
    public void setIsBackupPaused(Boolean isBackupPaused) {
      if (this.isBackupPaused == null) {
        this.isBackupPaused = isBackupPaused;
      }
      else {
        log.warn("Read-only property isBackupPaused already contains a non-null value and cannot be set again");
      }
    }
  
    
    
    
    
    private String backupPauseReason = null;
  
    /**
     * Specifies a reason why the backup might have paused. This will be empty if the isBackupPaused field is false.

     */
    public void setBackupPauseReason(String backupPauseReason) {
      if (this.backupPauseReason == null) {
        this.backupPauseReason = backupPauseReason;
      }
      else {
        log.warn("Read-only property backupPauseReason already contains a non-null value and cannot be set again");
      }
    }
  
  
    
    
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Getter
    private String $locationItemDiscriminator = null;
    
    public String get$locationItemDiscriminator() {
      // not required when setLocationInWrapper is renamed to setLocation
      if (this.location != null && this.$locationItemDiscriminator == null) {
        this.$locationItemDiscriminator = this.location.getDiscriminator();
      }
      return this.$locationItemDiscriminator;
    }
    
    @Data
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = OneOfLocationWrapper.OneOfLocationWrapperJsonDeserializer.class)
    public static class OneOfLocationWrapper {
    
    
      public OneOfLocationWrapper() {
      }
    
    
    
      public OneOfLocationWrapper(dp1.pri.prism.v4.management.ClusterLocation data) {
        this.oneOfType12001 = data;
        this.discriminator = data.get$objectType();
        this.$objectType = data.get$objectType();
      }
      public OneOfLocationWrapper(dp1.pri.prism.v4.management.ObjectStoreLocation data) {
        this.oneOfType12002 = data;
        this.discriminator = data.get$objectType();
        this.$objectType = data.get$objectType();
      }
    
      @dp1.pri.annotations.PriJsonDeserializer
      private static class OneOfLocationWrapperJsonDeserializer extends dp1.pri.deserializers.PriOneOfDeserializer<OneOfLocationWrapper>  {
        private static final com.fasterxml.jackson.databind.type.TypeFactory TYPE_FACTORY = com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance();
        private static final com.fasterxml.jackson.databind.JavaType ONE_OF_TYPE12001 = TYPE_FACTORY.constructType(dp1.pri.prism.v4.management.ClusterLocation.class);
        private static final com.fasterxml.jackson.databind.JavaType ONE_OF_TYPE12002 = TYPE_FACTORY.constructType(dp1.pri.prism.v4.management.ObjectStoreLocation.class);
    
        public OneOfLocationWrapperJsonDeserializer() {
          super(TYPE_FACTORY.constructType(OneOfLocationWrapper.class));
        }
    
        @Override
        protected void setDataObject(OneOfLocationWrapper oneOfObject, Object nestedObject) {
          if (oneOfObject == null) {
            throw new IllegalArgumentException("Instance of OneOfLocationWrapper provided is null");
          }
          if(ONE_OF_TYPE12001.getRawClass().isAssignableFrom(nestedObject.getClass())) {
            oneOfObject.setValue(nestedObject);
            if (oneOfObject.getValue() != null) {
                return;
            }
          }
          if(ONE_OF_TYPE12002.getRawClass().isAssignableFrom(nestedObject.getClass())) {
            oneOfObject.setValue(nestedObject);
            if (oneOfObject.getValue() != null) {
                return;
            }
          }
    
          throw new IllegalArgumentException("Attempting to set unsupported object type in OneOfLocationWrapper:" + nestedObject.getClass().getName());
        }
    
        public String getPackagePrefix() {
          return "dp1.pri";
        }
      }
    
      @com.fasterxml.jackson.annotation.JsonIgnore
      @lombok.Getter(lombok.AccessLevel.NONE)
      @lombok.Setter(lombok.AccessLevel.NONE)
      private dp1.pri.prism.v4.management.ClusterLocation oneOfType12001;
      @com.fasterxml.jackson.annotation.JsonIgnore
      @lombok.Getter(lombok.AccessLevel.NONE)
      @lombok.Setter(lombok.AccessLevel.NONE)
      private dp1.pri.prism.v4.management.ObjectStoreLocation oneOfType12002;
    
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
        if(oneOfType12002 != null && oneOfType12002.get$objectType().equals(this.discriminator)) {
          return this.oneOfType12002;
        }
        throw new IllegalArgumentException("Unrecognized discriminator:" + this.discriminator);
      }
    
      public void setValue(Object value) {
        if(value == null) {
          log.warn("null passed to setValue function. OneOf's value will not be set.");
          return;
        }
    
        if (value instanceof dp1.pri.prism.v4.management.ClusterLocation) {
          this.oneOfType12001 = (dp1.pri.prism.v4.management.ClusterLocation) value;
          this.discriminator = this.oneOfType12001.get$objectType();
          this.$objectType = this.oneOfType12001.get$objectType();
          return;
        }
        if (value instanceof dp1.pri.prism.v4.management.ObjectStoreLocation) {
          this.oneOfType12002 = (dp1.pri.prism.v4.management.ObjectStoreLocation) value;
          this.discriminator = this.oneOfType12002.get$objectType();
          this.$objectType = this.oneOfType12002.get$objectType();
          return;
        }
    
        throw new IllegalArgumentException("Attempting to set unsupported object type in OneOfLocationWrapper:" + value.getClass().getName());
    
      }
    
    }
  
    private OneOfLocationWrapper location = null;
  
    /**
     * @param value value of one of field location
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public void setLocationInWrapper(Object value) {
      if (value == null) {
        return;
      }
      if (this.location == null) {
        this.location = new OneOfLocationWrapper();
      }
      this.location.setValue(value);
      this.$locationItemDiscriminator = this.location.getDiscriminator();
    }
  
    /**
     * Get location in one of possible types :
     * <ul>
     * <li>dp1.pri.prism.v4.management.ClusterLocation</li>
     * <li>dp1.pri.prism.v4.management.ObjectStoreLocation</li>
     * </ul>
     * @return Object
     */
    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = dp1.pri.serializers.PriOneOfSerializer.class)
    public Object getLocation() {
      if (this.location == null) {
        log.debug("OneOf property location was never set. Returning null...");
        return null;
      }

      return this.location.getValue();
    }
  
  

}
