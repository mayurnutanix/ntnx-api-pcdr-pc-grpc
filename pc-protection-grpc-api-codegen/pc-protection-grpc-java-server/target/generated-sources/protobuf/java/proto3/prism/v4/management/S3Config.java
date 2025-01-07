// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: proto3/prism/v4/management/management.proto

package proto3.prism.v4.management;

/**
 * <pre>
 * The endpoint of the object store s3 where domain manager is backed up. 
 * </pre>
 *
 * Protobuf type {@code proto3.prism.v4.management.S3Config}
 */
public final class S3Config extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:proto3.prism.v4.management.S3Config)
    S3ConfigOrBuilder {
private static final long serialVersionUID = 0L;
  // Use S3Config.newBuilder() to construct.
  private S3Config(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private S3Config() {
    bucketName_ = "";
    region_ = "";
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new S3Config();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private S3Config(
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
          case 8002: {
            java.lang.String s = input.readStringRequireUtf8();

            bucketName_ = s;
            break;
          }
          case 8010: {
            java.lang.String s = input.readStringRequireUtf8();

            region_ = s;
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
    return proto3.prism.v4.management.Management.internal_static_proto3_prism_v4_management_S3Config_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return proto3.prism.v4.management.Management.internal_static_proto3_prism_v4_management_S3Config_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            proto3.prism.v4.management.S3Config.class, proto3.prism.v4.management.S3Config.Builder.class);
  }

  public static final int BUCKET_NAME_FIELD_NUMBER = 1000;
  private volatile java.lang.Object bucketName_;
  /**
   * <pre>
   * The bucket name of the object store endpoint where backup data of domain manager is to be stored. 
   * </pre>
   *
   * <code>string bucket_name = 1000;</code>
   * @return The bucketName.
   */
  @java.lang.Override
  public java.lang.String getBucketName() {
    java.lang.Object ref = bucketName_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      bucketName_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * The bucket name of the object store endpoint where backup data of domain manager is to be stored. 
   * </pre>
   *
   * <code>string bucket_name = 1000;</code>
   * @return The bytes for bucketName.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getBucketNameBytes() {
    java.lang.Object ref = bucketName_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      bucketName_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int REGION_FIELD_NUMBER = 1001;
  private volatile java.lang.Object region_;
  /**
   * <pre>
   * The region name of the object store endpoint where backup data of domain manager is stored. 
   * </pre>
   *
   * <code>string region = 1001;</code>
   * @return The region.
   */
  @java.lang.Override
  public java.lang.String getRegion() {
    java.lang.Object ref = region_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      region_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * The region name of the object store endpoint where backup data of domain manager is stored. 
   * </pre>
   *
   * <code>string region = 1001;</code>
   * @return The bytes for region.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getRegionBytes() {
    java.lang.Object ref = region_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      region_ = b;
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
    if (!getBucketNameBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1000, bucketName_);
    }
    if (!getRegionBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1001, region_);
    }
    unknownFields.writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!getBucketNameBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1000, bucketName_);
    }
    if (!getRegionBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1001, region_);
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
    if (!(obj instanceof proto3.prism.v4.management.S3Config)) {
      return super.equals(obj);
    }
    proto3.prism.v4.management.S3Config other = (proto3.prism.v4.management.S3Config) obj;

    if (!getBucketName()
        .equals(other.getBucketName())) return false;
    if (!getRegion()
        .equals(other.getRegion())) return false;
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
    hash = (37 * hash) + BUCKET_NAME_FIELD_NUMBER;
    hash = (53 * hash) + getBucketName().hashCode();
    hash = (37 * hash) + REGION_FIELD_NUMBER;
    hash = (53 * hash) + getRegion().hashCode();
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static proto3.prism.v4.management.S3Config parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static proto3.prism.v4.management.S3Config parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static proto3.prism.v4.management.S3Config parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static proto3.prism.v4.management.S3Config parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static proto3.prism.v4.management.S3Config parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static proto3.prism.v4.management.S3Config parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static proto3.prism.v4.management.S3Config parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static proto3.prism.v4.management.S3Config parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static proto3.prism.v4.management.S3Config parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static proto3.prism.v4.management.S3Config parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static proto3.prism.v4.management.S3Config parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static proto3.prism.v4.management.S3Config parseFrom(
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
  public static Builder newBuilder(proto3.prism.v4.management.S3Config prototype) {
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
   * The endpoint of the object store s3 where domain manager is backed up. 
   * </pre>
   *
   * Protobuf type {@code proto3.prism.v4.management.S3Config}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:proto3.prism.v4.management.S3Config)
      proto3.prism.v4.management.S3ConfigOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return proto3.prism.v4.management.Management.internal_static_proto3_prism_v4_management_S3Config_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return proto3.prism.v4.management.Management.internal_static_proto3_prism_v4_management_S3Config_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              proto3.prism.v4.management.S3Config.class, proto3.prism.v4.management.S3Config.Builder.class);
    }

    // Construct using proto3.prism.v4.management.S3Config.newBuilder()
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
      bucketName_ = "";

      region_ = "";

      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return proto3.prism.v4.management.Management.internal_static_proto3_prism_v4_management_S3Config_descriptor;
    }

    @java.lang.Override
    public proto3.prism.v4.management.S3Config getDefaultInstanceForType() {
      return proto3.prism.v4.management.S3Config.getDefaultInstance();
    }

    @java.lang.Override
    public proto3.prism.v4.management.S3Config build() {
      proto3.prism.v4.management.S3Config result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public proto3.prism.v4.management.S3Config buildPartial() {
      proto3.prism.v4.management.S3Config result = new proto3.prism.v4.management.S3Config(this);
      result.bucketName_ = bucketName_;
      result.region_ = region_;
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
      if (other instanceof proto3.prism.v4.management.S3Config) {
        return mergeFrom((proto3.prism.v4.management.S3Config)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(proto3.prism.v4.management.S3Config other) {
      if (other == proto3.prism.v4.management.S3Config.getDefaultInstance()) return this;
      if (!other.getBucketName().isEmpty()) {
        bucketName_ = other.bucketName_;
        onChanged();
      }
      if (!other.getRegion().isEmpty()) {
        region_ = other.region_;
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
      proto3.prism.v4.management.S3Config parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (proto3.prism.v4.management.S3Config) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private java.lang.Object bucketName_ = "";
    /**
     * <pre>
     * The bucket name of the object store endpoint where backup data of domain manager is to be stored. 
     * </pre>
     *
     * <code>string bucket_name = 1000;</code>
     * @return The bucketName.
     */
    public java.lang.String getBucketName() {
      java.lang.Object ref = bucketName_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        bucketName_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     * The bucket name of the object store endpoint where backup data of domain manager is to be stored. 
     * </pre>
     *
     * <code>string bucket_name = 1000;</code>
     * @return The bytes for bucketName.
     */
    public com.google.protobuf.ByteString
        getBucketNameBytes() {
      java.lang.Object ref = bucketName_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        bucketName_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * The bucket name of the object store endpoint where backup data of domain manager is to be stored. 
     * </pre>
     *
     * <code>string bucket_name = 1000;</code>
     * @param value The bucketName to set.
     * @return This builder for chaining.
     */
    public Builder setBucketName(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      bucketName_ = value;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The bucket name of the object store endpoint where backup data of domain manager is to be stored. 
     * </pre>
     *
     * <code>string bucket_name = 1000;</code>
     * @return This builder for chaining.
     */
    public Builder clearBucketName() {
      
      bucketName_ = getDefaultInstance().getBucketName();
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The bucket name of the object store endpoint where backup data of domain manager is to be stored. 
     * </pre>
     *
     * <code>string bucket_name = 1000;</code>
     * @param value The bytes for bucketName to set.
     * @return This builder for chaining.
     */
    public Builder setBucketNameBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      bucketName_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object region_ = "";
    /**
     * <pre>
     * The region name of the object store endpoint where backup data of domain manager is stored. 
     * </pre>
     *
     * <code>string region = 1001;</code>
     * @return The region.
     */
    public java.lang.String getRegion() {
      java.lang.Object ref = region_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        region_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     * The region name of the object store endpoint where backup data of domain manager is stored. 
     * </pre>
     *
     * <code>string region = 1001;</code>
     * @return The bytes for region.
     */
    public com.google.protobuf.ByteString
        getRegionBytes() {
      java.lang.Object ref = region_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        region_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * The region name of the object store endpoint where backup data of domain manager is stored. 
     * </pre>
     *
     * <code>string region = 1001;</code>
     * @param value The region to set.
     * @return This builder for chaining.
     */
    public Builder setRegion(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      region_ = value;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The region name of the object store endpoint where backup data of domain manager is stored. 
     * </pre>
     *
     * <code>string region = 1001;</code>
     * @return This builder for chaining.
     */
    public Builder clearRegion() {
      
      region_ = getDefaultInstance().getRegion();
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The region name of the object store endpoint where backup data of domain manager is stored. 
     * </pre>
     *
     * <code>string region = 1001;</code>
     * @param value The bytes for region to set.
     * @return This builder for chaining.
     */
    public Builder setRegionBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      region_ = value;
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


    // @@protoc_insertion_point(builder_scope:proto3.prism.v4.management.S3Config)
  }

  // @@protoc_insertion_point(class_scope:proto3.prism.v4.management.S3Config)
  private static final proto3.prism.v4.management.S3Config DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new proto3.prism.v4.management.S3Config();
  }

  public static proto3.prism.v4.management.S3Config getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<S3Config>
      PARSER = new com.google.protobuf.AbstractParser<S3Config>() {
    @java.lang.Override
    public S3Config parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new S3Config(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<S3Config> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<S3Config> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public proto3.prism.v4.management.S3Config getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

