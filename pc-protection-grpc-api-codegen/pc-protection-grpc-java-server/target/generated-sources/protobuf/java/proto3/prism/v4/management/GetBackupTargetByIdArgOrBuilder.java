// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: proto3/prism/v4/management/DomainManagerBackups_service.proto

package proto3.prism.v4.management;

public interface GetBackupTargetByIdArgOrBuilder extends
    // @@protoc_insertion_point(interface_extends:proto3.prism.v4.management.GetBackupTargetByIdArg)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The unique identifier for the domain manager that is to be restored.
   * </pre>
   *
   * <code>string domain_manager_ext_id = 31001;</code>
   * @return The domainManagerExtId.
   */
  java.lang.String getDomainManagerExtId();
  /**
   * <pre>
   * The unique identifier for the domain manager that is to be restored.
   * </pre>
   *
   * <code>string domain_manager_ext_id = 31001;</code>
   * @return The bytes for domainManagerExtId.
   */
  com.google.protobuf.ByteString
      getDomainManagerExtIdBytes();

  /**
   * <pre>
   * A globally unique identifier of an instance that is suitable for external consumption.
   * </pre>
   *
   * <code>string ext_id = 31002;</code>
   * @return The extId.
   */
  java.lang.String getExtId();
  /**
   * <pre>
   * A globally unique identifier of an instance that is suitable for external consumption.
   * </pre>
   *
   * <code>string ext_id = 31002;</code>
   * @return The bytes for extId.
   */
  com.google.protobuf.ByteString
      getExtIdBytes();
}