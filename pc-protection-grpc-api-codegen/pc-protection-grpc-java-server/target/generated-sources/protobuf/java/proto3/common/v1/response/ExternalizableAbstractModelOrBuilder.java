// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: proto3/common/v1/response/response.proto

package proto3.common.v1.response;

public interface ExternalizableAbstractModelOrBuilder extends
    // @@protoc_insertion_point(interface_extends:proto3.common.v1.response.ExternalizableAbstractModel)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * 
   * </pre>
   *
   * <code>.proto3.common.v1.config.TenantAwareModel tenant_info = 1001;</code>
   * @return Whether the tenantInfo field is set.
   */
  boolean hasTenantInfo();
  /**
   * <pre>
   * 
   * </pre>
   *
   * <code>.proto3.common.v1.config.TenantAwareModel tenant_info = 1001;</code>
   * @return The tenantInfo.
   */
  proto3.common.v1.config.TenantAwareModel getTenantInfo();
  /**
   * <pre>
   * 
   * </pre>
   *
   * <code>.proto3.common.v1.config.TenantAwareModel tenant_info = 1001;</code>
   */
  proto3.common.v1.config.TenantAwareModelOrBuilder getTenantInfoOrBuilder();

  /**
   * <pre>
   * A globally unique identifier of an instance that is suitable for external consumption. 
   * </pre>
   *
   * <code>string ext_id = 1002;</code>
   * @return The extId.
   */
  java.lang.String getExtId();
  /**
   * <pre>
   * A globally unique identifier of an instance that is suitable for external consumption. 
   * </pre>
   *
   * <code>string ext_id = 1002;</code>
   * @return The bytes for extId.
   */
  com.google.protobuf.ByteString
      getExtIdBytes();

  /**
   * <pre>
   * A HATEOAS style link for the response.  Each link contains a user-friendly name identifying the link and an address for retrieving the particular resource. 
   * </pre>
   *
   * <code>repeated .proto3.common.v1.response.ApiLink links = 1003;</code>
   */
  java.util.List<proto3.common.v1.response.ApiLink> 
      getLinksList();
  /**
   * <pre>
   * A HATEOAS style link for the response.  Each link contains a user-friendly name identifying the link and an address for retrieving the particular resource. 
   * </pre>
   *
   * <code>repeated .proto3.common.v1.response.ApiLink links = 1003;</code>
   */
  proto3.common.v1.response.ApiLink getLinks(int index);
  /**
   * <pre>
   * A HATEOAS style link for the response.  Each link contains a user-friendly name identifying the link and an address for retrieving the particular resource. 
   * </pre>
   *
   * <code>repeated .proto3.common.v1.response.ApiLink links = 1003;</code>
   */
  int getLinksCount();
  /**
   * <pre>
   * A HATEOAS style link for the response.  Each link contains a user-friendly name identifying the link and an address for retrieving the particular resource. 
   * </pre>
   *
   * <code>repeated .proto3.common.v1.response.ApiLink links = 1003;</code>
   */
  java.util.List<? extends proto3.common.v1.response.ApiLinkOrBuilder> 
      getLinksOrBuilderList();
  /**
   * <pre>
   * A HATEOAS style link for the response.  Each link contains a user-friendly name identifying the link and an address for retrieving the particular resource. 
   * </pre>
   *
   * <code>repeated .proto3.common.v1.response.ApiLink links = 1003;</code>
   */
  proto3.common.v1.response.ApiLinkOrBuilder getLinksOrBuilder(
      int index);
}