// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: proto3/prism/v4/management/management.proto

package proto3.prism.v4.management;

public interface S3ConfigOrBuilder extends
    // @@protoc_insertion_point(interface_extends:proto3.prism.v4.management.S3Config)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The bucket name of the object store endpoint where backup data of domain manager is to be stored. 
   * </pre>
   *
   * <code>string bucket_name = 1000;</code>
   * @return The bucketName.
   */
  java.lang.String getBucketName();
  /**
   * <pre>
   * The bucket name of the object store endpoint where backup data of domain manager is to be stored. 
   * </pre>
   *
   * <code>string bucket_name = 1000;</code>
   * @return The bytes for bucketName.
   */
  com.google.protobuf.ByteString
      getBucketNameBytes();

  /**
   * <pre>
   * The region name of the object store endpoint where backup data of domain manager is stored. 
   * </pre>
   *
   * <code>string region = 1001;</code>
   * @return The region.
   */
  java.lang.String getRegion();
  /**
   * <pre>
   * The region name of the object store endpoint where backup data of domain manager is stored. 
   * </pre>
   *
   * <code>string region = 1001;</code>
   * @return The bytes for region.
   */
  com.google.protobuf.ByteString
      getRegionBytes();
}
