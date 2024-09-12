// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: proto3/common/v1/response/response.proto

package proto3.common.v1.response;

public interface ApiLinkOrBuilder extends
    // @@protoc_insertion_point(interface_extends:proto3.common.v1.response.ApiLink)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The URL at which the entity described by the link can be accessed. 
   * </pre>
   *
   * <code>string href = 1001;</code>
   * @return The href.
   */
  java.lang.String getHref();
  /**
   * <pre>
   * The URL at which the entity described by the link can be accessed. 
   * </pre>
   *
   * <code>string href = 1001;</code>
   * @return The bytes for href.
   */
  com.google.protobuf.ByteString
      getHrefBytes();

  /**
   * <pre>
   * A name that identifies the relationship of the link to the object that is returned by the URL.  The unique value of &#92;"self&#92;" identifies the URL for the object. 
   * </pre>
   *
   * <code>string rel = 1002;</code>
   * @return The rel.
   */
  java.lang.String getRel();
  /**
   * <pre>
   * A name that identifies the relationship of the link to the object that is returned by the URL.  The unique value of &#92;"self&#92;" identifies the URL for the object. 
   * </pre>
   *
   * <code>string rel = 1002;</code>
   * @return The bytes for rel.
   */
  com.google.protobuf.ByteString
      getRelBytes();
}