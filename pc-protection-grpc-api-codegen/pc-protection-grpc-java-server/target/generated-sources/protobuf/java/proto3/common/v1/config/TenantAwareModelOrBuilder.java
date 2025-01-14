// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: proto3/common/v1/config/config.proto

package proto3.common.v1.config;

public interface TenantAwareModelOrBuilder extends
    // @@protoc_insertion_point(interface_extends:proto3.common.v1.config.TenantAwareModel)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * A globally unique identifier that represents the tenant that owns this entity. The system automatically assigns it, and it and is immutable from an API consumer perspective (some use cases may cause this Id to change - For instance, a use case may require the transfer of ownership of the entity, but these cases are handled automatically on the server). 
   * </pre>
   *
   * <code>string tenant_id = 1001;</code>
   * @return The tenantId.
   */
  java.lang.String getTenantId();
  /**
   * <pre>
   * A globally unique identifier that represents the tenant that owns this entity. The system automatically assigns it, and it and is immutable from an API consumer perspective (some use cases may cause this Id to change - For instance, a use case may require the transfer of ownership of the entity, but these cases are handled automatically on the server). 
   * </pre>
   *
   * <code>string tenant_id = 1001;</code>
   * @return The bytes for tenantId.
   */
  com.google.protobuf.ByteString
      getTenantIdBytes();
}
