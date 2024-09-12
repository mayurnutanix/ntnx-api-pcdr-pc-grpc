// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: proto3/prism/v4/management/management.proto

package proto3.prism.v4.management;

public interface ClusterReferenceOrBuilder extends
    // @@protoc_insertion_point(interface_extends:proto3.prism.v4.management.ClusterReference)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * Cluster UUID of a remote cluster.
   * </pre>
   *
   * <code>string ext_id = 2001;</code>
   * @return The extId.
   */
  java.lang.String getExtId();
  /**
   * <pre>
   * Cluster UUID of a remote cluster.
   * </pre>
   *
   * <code>string ext_id = 2001;</code>
   * @return The bytes for extId.
   */
  com.google.protobuf.ByteString
      getExtIdBytes();

  /**
   * <pre>
   * Name of the cluster.
   * </pre>
   *
   * <code>string name = 2002;</code>
   * @return The name.
   */
  java.lang.String getName();
  /**
   * <pre>
   * Name of the cluster.
   * </pre>
   *
   * <code>string name = 2002;</code>
   * @return The bytes for name.
   */
  com.google.protobuf.ByteString
      getNameBytes();
}
