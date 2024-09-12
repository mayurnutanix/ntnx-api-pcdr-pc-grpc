// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: proto3/common/v1/response/response.proto

package proto3.common.v1.response;

/**
 * <pre>
 * A HATEOAS style link for the response.  Each link contains a user-friendly name identifying the link and an address for retrieving the particular resource. 
 * </pre>
 *
 * Protobuf type {@code proto3.common.v1.response.ApiLink}
 */
public final class ApiLink extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:proto3.common.v1.response.ApiLink)
    ApiLinkOrBuilder {
private static final long serialVersionUID = 0L;
  // Use ApiLink.newBuilder() to construct.
  private ApiLink(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private ApiLink() {
    href_ = "";
    rel_ = "";
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new ApiLink();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private ApiLink(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    if (extensionRegistry == null) {
      throw new java.lang.NullPointerException();
    }
    com.google.protobuf.UnknownFieldSet.Builder unknownFields =
        com.google.protobuf.UnknownFieldSet.newBuilder();
    try {
      boolean done = false;
      while (!done) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            done = true;
            break;
          case 8010: {
            java.lang.String s = input.readStringRequireUtf8();

            href_ = s;
            break;
          }
          case 8018: {
            java.lang.String s = input.readStringRequireUtf8();

            rel_ = s;
            break;
          }
          default: {
            if (!parseUnknownField(
                input, unknownFields, extensionRegistry, tag)) {
              done = true;
            }
            break;
          }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(
          e).setUnfinishedMessage(this);
    } finally {
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return proto3.common.v1.response.Response.internal_static_proto3_common_v1_response_ApiLink_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return proto3.common.v1.response.Response.internal_static_proto3_common_v1_response_ApiLink_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            proto3.common.v1.response.ApiLink.class, proto3.common.v1.response.ApiLink.Builder.class);
  }

  public static final int HREF_FIELD_NUMBER = 1001;
  private volatile java.lang.Object href_;
  /**
   * <pre>
   * The URL at which the entity described by the link can be accessed. 
   * </pre>
   *
   * <code>string href = 1001;</code>
   * @return The href.
   */
  @java.lang.Override
  public java.lang.String getHref() {
    java.lang.Object ref = href_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      href_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * The URL at which the entity described by the link can be accessed. 
   * </pre>
   *
   * <code>string href = 1001;</code>
   * @return The bytes for href.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getHrefBytes() {
    java.lang.Object ref = href_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      href_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int REL_FIELD_NUMBER = 1002;
  private volatile java.lang.Object rel_;
  /**
   * <pre>
   * A name that identifies the relationship of the link to the object that is returned by the URL.  The unique value of &#92;"self&#92;" identifies the URL for the object. 
   * </pre>
   *
   * <code>string rel = 1002;</code>
   * @return The rel.
   */
  @java.lang.Override
  public java.lang.String getRel() {
    java.lang.Object ref = rel_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      rel_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * A name that identifies the relationship of the link to the object that is returned by the URL.  The unique value of &#92;"self&#92;" identifies the URL for the object. 
   * </pre>
   *
   * <code>string rel = 1002;</code>
   * @return The bytes for rel.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getRelBytes() {
    java.lang.Object ref = rel_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      rel_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  private byte memoizedIsInitialized = -1;
  @java.lang.Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  @java.lang.Override
  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (!getHrefBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1001, href_);
    }
    if (!getRelBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1002, rel_);
    }
    unknownFields.writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!getHrefBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1001, href_);
    }
    if (!getRelBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1002, rel_);
    }
    size += unknownFields.getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof proto3.common.v1.response.ApiLink)) {
      return super.equals(obj);
    }
    proto3.common.v1.response.ApiLink other = (proto3.common.v1.response.ApiLink) obj;

    if (!getHref()
        .equals(other.getHref())) return false;
    if (!getRel()
        .equals(other.getRel())) return false;
    if (!unknownFields.equals(other.unknownFields)) return false;
    return true;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    hash = (37 * hash) + HREF_FIELD_NUMBER;
    hash = (53 * hash) + getHref().hashCode();
    hash = (37 * hash) + REL_FIELD_NUMBER;
    hash = (53 * hash) + getRel().hashCode();
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static proto3.common.v1.response.ApiLink parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static proto3.common.v1.response.ApiLink parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static proto3.common.v1.response.ApiLink parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static proto3.common.v1.response.ApiLink parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static proto3.common.v1.response.ApiLink parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static proto3.common.v1.response.ApiLink parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static proto3.common.v1.response.ApiLink parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static proto3.common.v1.response.ApiLink parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static proto3.common.v1.response.ApiLink parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static proto3.common.v1.response.ApiLink parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static proto3.common.v1.response.ApiLink parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static proto3.common.v1.response.ApiLink parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(proto3.common.v1.response.ApiLink prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @java.lang.Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * <pre>
   * A HATEOAS style link for the response.  Each link contains a user-friendly name identifying the link and an address for retrieving the particular resource. 
   * </pre>
   *
   * Protobuf type {@code proto3.common.v1.response.ApiLink}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:proto3.common.v1.response.ApiLink)
      proto3.common.v1.response.ApiLinkOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return proto3.common.v1.response.Response.internal_static_proto3_common_v1_response_ApiLink_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return proto3.common.v1.response.Response.internal_static_proto3_common_v1_response_ApiLink_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              proto3.common.v1.response.ApiLink.class, proto3.common.v1.response.ApiLink.Builder.class);
    }

    // Construct using proto3.common.v1.response.ApiLink.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3
              .alwaysUseFieldBuilders) {
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      href_ = "";

      rel_ = "";

      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return proto3.common.v1.response.Response.internal_static_proto3_common_v1_response_ApiLink_descriptor;
    }

    @java.lang.Override
    public proto3.common.v1.response.ApiLink getDefaultInstanceForType() {
      return proto3.common.v1.response.ApiLink.getDefaultInstance();
    }

    @java.lang.Override
    public proto3.common.v1.response.ApiLink build() {
      proto3.common.v1.response.ApiLink result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public proto3.common.v1.response.ApiLink buildPartial() {
      proto3.common.v1.response.ApiLink result = new proto3.common.v1.response.ApiLink(this);
      result.href_ = href_;
      result.rel_ = rel_;
      onBuilt();
      return result;
    }

    @java.lang.Override
    public Builder clone() {
      return super.clone();
    }
    @java.lang.Override
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.setField(field, value);
    }
    @java.lang.Override
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return super.clearField(field);
    }
    @java.lang.Override
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return super.clearOneof(oneof);
    }
    @java.lang.Override
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, java.lang.Object value) {
      return super.setRepeatedField(field, index, value);
    }
    @java.lang.Override
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.addRepeatedField(field, value);
    }
    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof proto3.common.v1.response.ApiLink) {
        return mergeFrom((proto3.common.v1.response.ApiLink)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(proto3.common.v1.response.ApiLink other) {
      if (other == proto3.common.v1.response.ApiLink.getDefaultInstance()) return this;
      if (!other.getHref().isEmpty()) {
        href_ = other.href_;
        onChanged();
      }
      if (!other.getRel().isEmpty()) {
        rel_ = other.rel_;
        onChanged();
      }
      this.mergeUnknownFields(other.unknownFields);
      onChanged();
      return this;
    }

    @java.lang.Override
    public final boolean isInitialized() {
      return true;
    }

    @java.lang.Override
    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      proto3.common.v1.response.ApiLink parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (proto3.common.v1.response.ApiLink) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private java.lang.Object href_ = "";
    /**
     * <pre>
     * The URL at which the entity described by the link can be accessed. 
     * </pre>
     *
     * <code>string href = 1001;</code>
     * @return The href.
     */
    public java.lang.String getHref() {
      java.lang.Object ref = href_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        href_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     * The URL at which the entity described by the link can be accessed. 
     * </pre>
     *
     * <code>string href = 1001;</code>
     * @return The bytes for href.
     */
    public com.google.protobuf.ByteString
        getHrefBytes() {
      java.lang.Object ref = href_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        href_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * The URL at which the entity described by the link can be accessed. 
     * </pre>
     *
     * <code>string href = 1001;</code>
     * @param value The href to set.
     * @return This builder for chaining.
     */
    public Builder setHref(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      href_ = value;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The URL at which the entity described by the link can be accessed. 
     * </pre>
     *
     * <code>string href = 1001;</code>
     * @return This builder for chaining.
     */
    public Builder clearHref() {
      
      href_ = getDefaultInstance().getHref();
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The URL at which the entity described by the link can be accessed. 
     * </pre>
     *
     * <code>string href = 1001;</code>
     * @param value The bytes for href to set.
     * @return This builder for chaining.
     */
    public Builder setHrefBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      href_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object rel_ = "";
    /**
     * <pre>
     * A name that identifies the relationship of the link to the object that is returned by the URL.  The unique value of &#92;"self&#92;" identifies the URL for the object. 
     * </pre>
     *
     * <code>string rel = 1002;</code>
     * @return The rel.
     */
    public java.lang.String getRel() {
      java.lang.Object ref = rel_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        rel_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     * A name that identifies the relationship of the link to the object that is returned by the URL.  The unique value of &#92;"self&#92;" identifies the URL for the object. 
     * </pre>
     *
     * <code>string rel = 1002;</code>
     * @return The bytes for rel.
     */
    public com.google.protobuf.ByteString
        getRelBytes() {
      java.lang.Object ref = rel_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        rel_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * A name that identifies the relationship of the link to the object that is returned by the URL.  The unique value of &#92;"self&#92;" identifies the URL for the object. 
     * </pre>
     *
     * <code>string rel = 1002;</code>
     * @param value The rel to set.
     * @return This builder for chaining.
     */
    public Builder setRel(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      rel_ = value;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * A name that identifies the relationship of the link to the object that is returned by the URL.  The unique value of &#92;"self&#92;" identifies the URL for the object. 
     * </pre>
     *
     * <code>string rel = 1002;</code>
     * @return This builder for chaining.
     */
    public Builder clearRel() {
      
      rel_ = getDefaultInstance().getRel();
      onChanged();
      return this;
    }
    /**
     * <pre>
     * A name that identifies the relationship of the link to the object that is returned by the URL.  The unique value of &#92;"self&#92;" identifies the URL for the object. 
     * </pre>
     *
     * <code>string rel = 1002;</code>
     * @param value The bytes for rel to set.
     * @return This builder for chaining.
     */
    public Builder setRelBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      rel_ = value;
      onChanged();
      return this;
    }
    @java.lang.Override
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    @java.lang.Override
    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:proto3.common.v1.response.ApiLink)
  }

  // @@protoc_insertion_point(class_scope:proto3.common.v1.response.ApiLink)
  private static final proto3.common.v1.response.ApiLink DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new proto3.common.v1.response.ApiLink();
  }

  public static proto3.common.v1.response.ApiLink getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<ApiLink>
      PARSER = new com.google.protobuf.AbstractParser<ApiLink>() {
    @java.lang.Override
    public ApiLink parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new ApiLink(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<ApiLink> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<ApiLink> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public proto3.common.v1.response.ApiLink getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}
