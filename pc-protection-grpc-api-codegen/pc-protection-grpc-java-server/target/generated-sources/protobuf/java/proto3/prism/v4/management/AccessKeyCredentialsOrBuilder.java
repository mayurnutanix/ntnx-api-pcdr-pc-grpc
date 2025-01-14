// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: proto3/prism/v4/management/management.proto

package proto3.prism.v4.management;

public interface AccessKeyCredentialsOrBuilder extends
    // @@protoc_insertion_point(interface_extends:proto3.prism.v4.management.AccessKeyCredentials)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * Access key for the object store provided for backup target.
   * </pre>
   *
   * <code>string access_key_id = 1000;</code>
   * @return The accessKeyId.
   */
  java.lang.String getAccessKeyId();
  /**
   * <pre>
   * Access key for the object store provided for backup target.
   * </pre>
   *
   * <code>string access_key_id = 1000;</code>
   * @return The bytes for accessKeyId.
   */
  com.google.protobuf.ByteString
      getAccessKeyIdBytes();

  /**
   * <pre>
   * Secret access key ID for the object store provided for backup target. 
   * </pre>
   *
   * <code>string secret_access_key = 1001;</code>
   * @return The secretAccessKey.
   */
  java.lang.String getSecretAccessKey();
  /**
   * <pre>
   * Secret access key ID for the object store provided for backup target. 
   * </pre>
   *
   * <code>string secret_access_key = 1001;</code>
   * @return The bytes for secretAccessKey.
   */
  com.google.protobuf.ByteString
      getSecretAccessKeyBytes();
}
