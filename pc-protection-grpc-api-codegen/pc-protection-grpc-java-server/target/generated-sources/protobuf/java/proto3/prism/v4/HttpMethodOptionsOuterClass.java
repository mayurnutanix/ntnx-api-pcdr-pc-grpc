// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: proto3/prism/v4/http_method_options.proto

package proto3.prism.v4;

public final class HttpMethodOptionsOuterClass {
  private HttpMethodOptionsOuterClass() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
    registry.add(proto3.prism.v4.HttpMethodOptionsOuterClass.ntnxApiHttp);
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  public static final int NTNX_API_HTTP_FIELD_NUMBER = 1000;
  /**
   * <code>extend .google.protobuf.MethodOptions { ... }</code>
   */
  public static final
    com.google.protobuf.GeneratedMessage.GeneratedExtension<
      com.google.protobuf.DescriptorProtos.MethodOptions,
      proto3.prism.v4.HttpMethodOptions> ntnxApiHttp = com.google.protobuf.GeneratedMessage
          .newFileScopedGeneratedExtension(
        proto3.prism.v4.HttpMethodOptions.class,
        proto3.prism.v4.HttpMethodOptions.getDefaultInstance());
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_proto3_prism_v4_HttpMethodOptions_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_proto3_prism_v4_HttpMethodOptions_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n)proto3/prism/v4/http_method_options.pr" +
      "oto\022\017proto3.prism.v4\032 google/protobuf/de" +
      "scriptor.proto\"\210\001\n\021HttpMethodOptions\022\014\n\004" +
      "POST\030\001 \001(\t\022\r\n\005PATCH\030\002 \001(\t\022\013\n\003PUT\030\003 \001(\t\022\016" +
      "\n\006DELETE\030\004 \001(\t\022\013\n\003GET\030\005 \001(\t\022\014\n\004HEAD\030\006 \001(" +
      "\t\022\017\n\007OPTIONS\030\007 \001(\t\022\r\n\005TRACE\030\010 \001(\t:Z\n\rntn" +
      "x_api_http\022\036.google.protobuf.MethodOptio" +
      "ns\030\350\007 \001(\0132\".proto3.prism.v4.HttpMethodOp" +
      "tionsB$\n\017proto3.prism.v4P\001Z\017proto3/prism" +
      "/v4b\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.protobuf.DescriptorProtos.getDescriptor(),
        });
    internal_static_proto3_prism_v4_HttpMethodOptions_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_proto3_prism_v4_HttpMethodOptions_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_proto3_prism_v4_HttpMethodOptions_descriptor,
        new java.lang.String[] { "POST", "PATCH", "PUT", "DELETE", "GET", "HEAD", "OPTIONS", "TRACE", });
    ntnxApiHttp.internalInit(descriptor.getExtensions().get(0));
    com.google.protobuf.DescriptorProtos.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
