// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: proto3/prism/v4/management/DomainManagerBackups_service.proto

package proto3.prism.v4.management;

/**
 * <pre>
 * message containing all attributes expected in the listBackupTargets request
 * </pre>
 *
 * Protobuf type {@code proto3.prism.v4.management.ListBackupTargetsArg}
 */
public final class ListBackupTargetsArg extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:proto3.prism.v4.management.ListBackupTargetsArg)
    ListBackupTargetsArgOrBuilder {
private static final long serialVersionUID = 0L;
  // Use ListBackupTargetsArg.newBuilder() to construct.
  private ListBackupTargetsArg(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private ListBackupTargetsArg() {
    domainManagerExtId_ = "";
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new ListBackupTargetsArg();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private ListBackupTargetsArg(
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
          case 248010: {
            java.lang.String s = input.readStringRequireUtf8();

            domainManagerExtId_ = s;
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
    return proto3.prism.v4.management.DomainManagerBackupsServiceOuterClass.internal_static_proto3_prism_v4_management_ListBackupTargetsArg_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return proto3.prism.v4.management.DomainManagerBackupsServiceOuterClass.internal_static_proto3_prism_v4_management_ListBackupTargetsArg_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            proto3.prism.v4.management.ListBackupTargetsArg.class, proto3.prism.v4.management.ListBackupTargetsArg.Builder.class);
  }

  public static final int DOMAIN_MANAGER_EXT_ID_FIELD_NUMBER = 31001;
  private volatile java.lang.Object domainManagerExtId_;
  /**
   * <pre>
   * The unique identifier for the domain manager that is to be restored.
   * </pre>
   *
   * <code>string domain_manager_ext_id = 31001;</code>
   * @return The domainManagerExtId.
   */
  @java.lang.Override
  public java.lang.String getDomainManagerExtId() {
    java.lang.Object ref = domainManagerExtId_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      domainManagerExtId_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * The unique identifier for the domain manager that is to be restored.
   * </pre>
   *
   * <code>string domain_manager_ext_id = 31001;</code>
   * @return The bytes for domainManagerExtId.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getDomainManagerExtIdBytes() {
    java.lang.Object ref = domainManagerExtId_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      domainManagerExtId_ = b;
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
    if (!getDomainManagerExtIdBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 31001, domainManagerExtId_);
    }
    unknownFields.writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!getDomainManagerExtIdBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(31001, domainManagerExtId_);
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
    if (!(obj instanceof proto3.prism.v4.management.ListBackupTargetsArg)) {
      return super.equals(obj);
    }
    proto3.prism.v4.management.ListBackupTargetsArg other = (proto3.prism.v4.management.ListBackupTargetsArg) obj;

    if (!getDomainManagerExtId()
        .equals(other.getDomainManagerExtId())) return false;
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
    hash = (37 * hash) + DOMAIN_MANAGER_EXT_ID_FIELD_NUMBER;
    hash = (53 * hash) + getDomainManagerExtId().hashCode();
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static proto3.prism.v4.management.ListBackupTargetsArg parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static proto3.prism.v4.management.ListBackupTargetsArg parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static proto3.prism.v4.management.ListBackupTargetsArg parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static proto3.prism.v4.management.ListBackupTargetsArg parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static proto3.prism.v4.management.ListBackupTargetsArg parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static proto3.prism.v4.management.ListBackupTargetsArg parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static proto3.prism.v4.management.ListBackupTargetsArg parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static proto3.prism.v4.management.ListBackupTargetsArg parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static proto3.prism.v4.management.ListBackupTargetsArg parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static proto3.prism.v4.management.ListBackupTargetsArg parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static proto3.prism.v4.management.ListBackupTargetsArg parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static proto3.prism.v4.management.ListBackupTargetsArg parseFrom(
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
  public static Builder newBuilder(proto3.prism.v4.management.ListBackupTargetsArg prototype) {
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
   * message containing all attributes expected in the listBackupTargets request
   * </pre>
   *
   * Protobuf type {@code proto3.prism.v4.management.ListBackupTargetsArg}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:proto3.prism.v4.management.ListBackupTargetsArg)
      proto3.prism.v4.management.ListBackupTargetsArgOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return proto3.prism.v4.management.DomainManagerBackupsServiceOuterClass.internal_static_proto3_prism_v4_management_ListBackupTargetsArg_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return proto3.prism.v4.management.DomainManagerBackupsServiceOuterClass.internal_static_proto3_prism_v4_management_ListBackupTargetsArg_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              proto3.prism.v4.management.ListBackupTargetsArg.class, proto3.prism.v4.management.ListBackupTargetsArg.Builder.class);
    }

    // Construct using proto3.prism.v4.management.ListBackupTargetsArg.newBuilder()
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
      domainManagerExtId_ = "";

      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return proto3.prism.v4.management.DomainManagerBackupsServiceOuterClass.internal_static_proto3_prism_v4_management_ListBackupTargetsArg_descriptor;
    }

    @java.lang.Override
    public proto3.prism.v4.management.ListBackupTargetsArg getDefaultInstanceForType() {
      return proto3.prism.v4.management.ListBackupTargetsArg.getDefaultInstance();
    }

    @java.lang.Override
    public proto3.prism.v4.management.ListBackupTargetsArg build() {
      proto3.prism.v4.management.ListBackupTargetsArg result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public proto3.prism.v4.management.ListBackupTargetsArg buildPartial() {
      proto3.prism.v4.management.ListBackupTargetsArg result = new proto3.prism.v4.management.ListBackupTargetsArg(this);
      result.domainManagerExtId_ = domainManagerExtId_;
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
      if (other instanceof proto3.prism.v4.management.ListBackupTargetsArg) {
        return mergeFrom((proto3.prism.v4.management.ListBackupTargetsArg)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(proto3.prism.v4.management.ListBackupTargetsArg other) {
      if (other == proto3.prism.v4.management.ListBackupTargetsArg.getDefaultInstance()) return this;
      if (!other.getDomainManagerExtId().isEmpty()) {
        domainManagerExtId_ = other.domainManagerExtId_;
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
      proto3.prism.v4.management.ListBackupTargetsArg parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (proto3.prism.v4.management.ListBackupTargetsArg) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private java.lang.Object domainManagerExtId_ = "";
    /**
     * <pre>
     * The unique identifier for the domain manager that is to be restored.
     * </pre>
     *
     * <code>string domain_manager_ext_id = 31001;</code>
     * @return The domainManagerExtId.
     */
    public java.lang.String getDomainManagerExtId() {
      java.lang.Object ref = domainManagerExtId_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        domainManagerExtId_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     * The unique identifier for the domain manager that is to be restored.
     * </pre>
     *
     * <code>string domain_manager_ext_id = 31001;</code>
     * @return The bytes for domainManagerExtId.
     */
    public com.google.protobuf.ByteString
        getDomainManagerExtIdBytes() {
      java.lang.Object ref = domainManagerExtId_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        domainManagerExtId_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * The unique identifier for the domain manager that is to be restored.
     * </pre>
     *
     * <code>string domain_manager_ext_id = 31001;</code>
     * @param value The domainManagerExtId to set.
     * @return This builder for chaining.
     */
    public Builder setDomainManagerExtId(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      domainManagerExtId_ = value;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The unique identifier for the domain manager that is to be restored.
     * </pre>
     *
     * <code>string domain_manager_ext_id = 31001;</code>
     * @return This builder for chaining.
     */
    public Builder clearDomainManagerExtId() {
      
      domainManagerExtId_ = getDefaultInstance().getDomainManagerExtId();
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The unique identifier for the domain manager that is to be restored.
     * </pre>
     *
     * <code>string domain_manager_ext_id = 31001;</code>
     * @param value The bytes for domainManagerExtId to set.
     * @return This builder for chaining.
     */
    public Builder setDomainManagerExtIdBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      domainManagerExtId_ = value;
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


    // @@protoc_insertion_point(builder_scope:proto3.prism.v4.management.ListBackupTargetsArg)
  }

  // @@protoc_insertion_point(class_scope:proto3.prism.v4.management.ListBackupTargetsArg)
  private static final proto3.prism.v4.management.ListBackupTargetsArg DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new proto3.prism.v4.management.ListBackupTargetsArg();
  }

  public static proto3.prism.v4.management.ListBackupTargetsArg getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<ListBackupTargetsArg>
      PARSER = new com.google.protobuf.AbstractParser<ListBackupTargetsArg>() {
    @java.lang.Override
    public ListBackupTargetsArg parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new ListBackupTargetsArg(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<ListBackupTargetsArg> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<ListBackupTargetsArg> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public proto3.prism.v4.management.ListBackupTargetsArg getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

