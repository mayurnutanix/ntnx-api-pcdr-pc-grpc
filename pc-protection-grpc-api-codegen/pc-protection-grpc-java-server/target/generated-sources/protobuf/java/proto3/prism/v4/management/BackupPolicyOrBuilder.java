// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: proto3/prism/v4/management/management.proto

package proto3.prism.v4.management;

public interface BackupPolicyOrBuilder extends
    // @@protoc_insertion_point(interface_extends:proto3.prism.v4.management.BackupPolicy)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * RPO interval in minutes at which the backup will be taken 
   * </pre>
   *
   * <code>int32 rpo_in_minutes = 1000;</code>
   * @return The rpoInMinutes.
   */
  int getRpoInMinutes();
}
