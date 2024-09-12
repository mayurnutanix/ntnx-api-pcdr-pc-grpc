// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: proto3/common/v1/config/config.proto

package proto3.common.v1.config;

public interface MapOfStringWrapperOrBuilder extends
    // @@protoc_insertion_point(interface_extends:proto3.common.v1.config.MapOfStringWrapper)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * A map with string keys and values. 
   * </pre>
   *
   * <code>map&lt;string, string&gt; map = 1001;</code>
   */
  int getMapCount();
  /**
   * <pre>
   * A map with string keys and values. 
   * </pre>
   *
   * <code>map&lt;string, string&gt; map = 1001;</code>
   */
  boolean containsMap(
      java.lang.String key);
  /**
   * Use {@link #getMapMap()} instead.
   */
  @java.lang.Deprecated
  java.util.Map<java.lang.String, java.lang.String>
  getMap();
  /**
   * <pre>
   * A map with string keys and values. 
   * </pre>
   *
   * <code>map&lt;string, string&gt; map = 1001;</code>
   */
  java.util.Map<java.lang.String, java.lang.String>
  getMapMap();
  /**
   * <pre>
   * A map with string keys and values. 
   * </pre>
   *
   * <code>map&lt;string, string&gt; map = 1001;</code>
   */

  java.lang.String getMapOrDefault(
      java.lang.String key,
      java.lang.String defaultValue);
  /**
   * <pre>
   * A map with string keys and values. 
   * </pre>
   *
   * <code>map&lt;string, string&gt; map = 1001;</code>
   */

  java.lang.String getMapOrThrow(
      java.lang.String key);
}
