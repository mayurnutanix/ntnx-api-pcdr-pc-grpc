// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: proto3/prism/v4/management/management.proto

package proto3.prism.v4.management;

public interface ObjectStoreLocationOrBuilder extends
    // @@protoc_insertion_point(interface_extends:proto3.prism.v4.management.ObjectStoreLocation)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * 
   * </pre>
   *
   * <code>.proto3.prism.v4.management.AWSS3Config provider_config = 1000;</code>
   * @return Whether the providerConfig field is set.
   */
  boolean hasProviderConfig();
  /**
   * <pre>
   * 
   * </pre>
   *
   * <code>.proto3.prism.v4.management.AWSS3Config provider_config = 1000;</code>
   * @return The providerConfig.
   */
  proto3.prism.v4.management.AWSS3Config getProviderConfig();
  /**
   * <pre>
   * 
   * </pre>
   *
   * <code>.proto3.prism.v4.management.AWSS3Config provider_config = 1000;</code>
   */
  proto3.prism.v4.management.AWSS3ConfigOrBuilder getProviderConfigOrBuilder();

  /**
   * <pre>
   * 
   * </pre>
   *
   * <code>.proto3.prism.v4.management.BackupPolicy backup_policy = 1001;</code>
   * @return Whether the backupPolicy field is set.
   */
  boolean hasBackupPolicy();
  /**
   * <pre>
   * 
   * </pre>
   *
   * <code>.proto3.prism.v4.management.BackupPolicy backup_policy = 1001;</code>
   * @return The backupPolicy.
   */
  proto3.prism.v4.management.BackupPolicy getBackupPolicy();
  /**
   * <pre>
   * 
   * </pre>
   *
   * <code>.proto3.prism.v4.management.BackupPolicy backup_policy = 1001;</code>
   */
  proto3.prism.v4.management.BackupPolicyOrBuilder getBackupPolicyOrBuilder();
}