// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: proto3/prism/v4/error/error.proto

package proto3.prism.v4.error;

/**
 * <pre>
 * This schema is generated from SchemaValidationErrorMessage.java
 * </pre>
 *
 * Protobuf type {@code proto3.prism.v4.error.SchemaValidationErrorMessage}
 */
public final class SchemaValidationErrorMessage extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:proto3.prism.v4.error.SchemaValidationErrorMessage)
    SchemaValidationErrorMessageOrBuilder {
private static final long serialVersionUID = 0L;
  // Use SchemaValidationErrorMessage.newBuilder() to construct.
  private SchemaValidationErrorMessage(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private SchemaValidationErrorMessage() {
    location_ = "";
    message_ = "";
    attributePath_ = "";
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new SchemaValidationErrorMessage();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private SchemaValidationErrorMessage(
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
          case 1610: {
            java.lang.String s = input.readStringRequireUtf8();

            location_ = s;
            break;
          }
          case 1618: {
            java.lang.String s = input.readStringRequireUtf8();

            message_ = s;
            break;
          }
          case 1626: {
            java.lang.String s = input.readStringRequireUtf8();

            attributePath_ = s;
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
    return proto3.prism.v4.error.Error.internal_static_proto3_prism_v4_error_SchemaValidationErrorMessage_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return proto3.prism.v4.error.Error.internal_static_proto3_prism_v4_error_SchemaValidationErrorMessage_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            proto3.prism.v4.error.SchemaValidationErrorMessage.class, proto3.prism.v4.error.SchemaValidationErrorMessage.Builder.class);
  }

  public static final int LOCATION_FIELD_NUMBER = 201;
  private volatile java.lang.Object location_;
  /**
   * <pre>
   * The part of the request that failed validation. Validation can fail for path, query parameters, and request body.
   * </pre>
   *
   * <code>string location = 201;</code>
   * @return The location.
   */
  @java.lang.Override
  public java.lang.String getLocation() {
    java.lang.Object ref = location_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      location_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * The part of the request that failed validation. Validation can fail for path, query parameters, and request body.
   * </pre>
   *
   * <code>string location = 201;</code>
   * @return The bytes for location.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getLocationBytes() {
    java.lang.Object ref = location_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      location_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int MESSAGE_FIELD_NUMBER = 202;
  private volatile java.lang.Object message_;
  /**
   * <pre>
   * The detailed message for the validation error.
   * </pre>
   *
   * <code>string message = 202;</code>
   * @return The message.
   */
  @java.lang.Override
  public java.lang.String getMessage() {
    java.lang.Object ref = message_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      message_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * The detailed message for the validation error.
   * </pre>
   *
   * <code>string message = 202;</code>
   * @return The bytes for message.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getMessageBytes() {
    java.lang.Object ref = message_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      message_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int ATTRIBUTE_PATH_FIELD_NUMBER = 203;
  private volatile java.lang.Object attributePath_;
  /**
   * <pre>
   * The path of the attribute that failed validation in the schema.
   * </pre>
   *
   * <code>string attribute_path = 203;</code>
   * @return The attributePath.
   */
  @java.lang.Override
  public java.lang.String getAttributePath() {
    java.lang.Object ref = attributePath_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      attributePath_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * The path of the attribute that failed validation in the schema.
   * </pre>
   *
   * <code>string attribute_path = 203;</code>
   * @return The bytes for attributePath.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getAttributePathBytes() {
    java.lang.Object ref = attributePath_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      attributePath_ = b;
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
    if (!getLocationBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 201, location_);
    }
    if (!getMessageBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 202, message_);
    }
    if (!getAttributePathBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 203, attributePath_);
    }
    unknownFields.writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!getLocationBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(201, location_);
    }
    if (!getMessageBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(202, message_);
    }
    if (!getAttributePathBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(203, attributePath_);
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
    if (!(obj instanceof proto3.prism.v4.error.SchemaValidationErrorMessage)) {
      return super.equals(obj);
    }
    proto3.prism.v4.error.SchemaValidationErrorMessage other = (proto3.prism.v4.error.SchemaValidationErrorMessage) obj;

    if (!getLocation()
        .equals(other.getLocation())) return false;
    if (!getMessage()
        .equals(other.getMessage())) return false;
    if (!getAttributePath()
        .equals(other.getAttributePath())) return false;
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
    hash = (37 * hash) + LOCATION_FIELD_NUMBER;
    hash = (53 * hash) + getLocation().hashCode();
    hash = (37 * hash) + MESSAGE_FIELD_NUMBER;
    hash = (53 * hash) + getMessage().hashCode();
    hash = (37 * hash) + ATTRIBUTE_PATH_FIELD_NUMBER;
    hash = (53 * hash) + getAttributePath().hashCode();
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static proto3.prism.v4.error.SchemaValidationErrorMessage parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static proto3.prism.v4.error.SchemaValidationErrorMessage parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static proto3.prism.v4.error.SchemaValidationErrorMessage parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static proto3.prism.v4.error.SchemaValidationErrorMessage parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static proto3.prism.v4.error.SchemaValidationErrorMessage parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static proto3.prism.v4.error.SchemaValidationErrorMessage parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static proto3.prism.v4.error.SchemaValidationErrorMessage parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static proto3.prism.v4.error.SchemaValidationErrorMessage parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static proto3.prism.v4.error.SchemaValidationErrorMessage parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static proto3.prism.v4.error.SchemaValidationErrorMessage parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static proto3.prism.v4.error.SchemaValidationErrorMessage parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static proto3.prism.v4.error.SchemaValidationErrorMessage parseFrom(
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
  public static Builder newBuilder(proto3.prism.v4.error.SchemaValidationErrorMessage prototype) {
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
   * This schema is generated from SchemaValidationErrorMessage.java
   * </pre>
   *
   * Protobuf type {@code proto3.prism.v4.error.SchemaValidationErrorMessage}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:proto3.prism.v4.error.SchemaValidationErrorMessage)
      proto3.prism.v4.error.SchemaValidationErrorMessageOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return proto3.prism.v4.error.Error.internal_static_proto3_prism_v4_error_SchemaValidationErrorMessage_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return proto3.prism.v4.error.Error.internal_static_proto3_prism_v4_error_SchemaValidationErrorMessage_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              proto3.prism.v4.error.SchemaValidationErrorMessage.class, proto3.prism.v4.error.SchemaValidationErrorMessage.Builder.class);
    }

    // Construct using proto3.prism.v4.error.SchemaValidationErrorMessage.newBuilder()
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
      location_ = "";

      message_ = "";

      attributePath_ = "";

      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return proto3.prism.v4.error.Error.internal_static_proto3_prism_v4_error_SchemaValidationErrorMessage_descriptor;
    }

    @java.lang.Override
    public proto3.prism.v4.error.SchemaValidationErrorMessage getDefaultInstanceForType() {
      return proto3.prism.v4.error.SchemaValidationErrorMessage.getDefaultInstance();
    }

    @java.lang.Override
    public proto3.prism.v4.error.SchemaValidationErrorMessage build() {
      proto3.prism.v4.error.SchemaValidationErrorMessage result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public proto3.prism.v4.error.SchemaValidationErrorMessage buildPartial() {
      proto3.prism.v4.error.SchemaValidationErrorMessage result = new proto3.prism.v4.error.SchemaValidationErrorMessage(this);
      result.location_ = location_;
      result.message_ = message_;
      result.attributePath_ = attributePath_;
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
      if (other instanceof proto3.prism.v4.error.SchemaValidationErrorMessage) {
        return mergeFrom((proto3.prism.v4.error.SchemaValidationErrorMessage)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(proto3.prism.v4.error.SchemaValidationErrorMessage other) {
      if (other == proto3.prism.v4.error.SchemaValidationErrorMessage.getDefaultInstance()) return this;
      if (!other.getLocation().isEmpty()) {
        location_ = other.location_;
        onChanged();
      }
      if (!other.getMessage().isEmpty()) {
        message_ = other.message_;
        onChanged();
      }
      if (!other.getAttributePath().isEmpty()) {
        attributePath_ = other.attributePath_;
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
      proto3.prism.v4.error.SchemaValidationErrorMessage parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (proto3.prism.v4.error.SchemaValidationErrorMessage) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private java.lang.Object location_ = "";
    /**
     * <pre>
     * The part of the request that failed validation. Validation can fail for path, query parameters, and request body.
     * </pre>
     *
     * <code>string location = 201;</code>
     * @return The location.
     */
    public java.lang.String getLocation() {
      java.lang.Object ref = location_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        location_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     * The part of the request that failed validation. Validation can fail for path, query parameters, and request body.
     * </pre>
     *
     * <code>string location = 201;</code>
     * @return The bytes for location.
     */
    public com.google.protobuf.ByteString
        getLocationBytes() {
      java.lang.Object ref = location_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        location_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * The part of the request that failed validation. Validation can fail for path, query parameters, and request body.
     * </pre>
     *
     * <code>string location = 201;</code>
     * @param value The location to set.
     * @return This builder for chaining.
     */
    public Builder setLocation(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      location_ = value;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The part of the request that failed validation. Validation can fail for path, query parameters, and request body.
     * </pre>
     *
     * <code>string location = 201;</code>
     * @return This builder for chaining.
     */
    public Builder clearLocation() {
      
      location_ = getDefaultInstance().getLocation();
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The part of the request that failed validation. Validation can fail for path, query parameters, and request body.
     * </pre>
     *
     * <code>string location = 201;</code>
     * @param value The bytes for location to set.
     * @return This builder for chaining.
     */
    public Builder setLocationBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      location_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object message_ = "";
    /**
     * <pre>
     * The detailed message for the validation error.
     * </pre>
     *
     * <code>string message = 202;</code>
     * @return The message.
     */
    public java.lang.String getMessage() {
      java.lang.Object ref = message_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        message_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     * The detailed message for the validation error.
     * </pre>
     *
     * <code>string message = 202;</code>
     * @return The bytes for message.
     */
    public com.google.protobuf.ByteString
        getMessageBytes() {
      java.lang.Object ref = message_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        message_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * The detailed message for the validation error.
     * </pre>
     *
     * <code>string message = 202;</code>
     * @param value The message to set.
     * @return This builder for chaining.
     */
    public Builder setMessage(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      message_ = value;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The detailed message for the validation error.
     * </pre>
     *
     * <code>string message = 202;</code>
     * @return This builder for chaining.
     */
    public Builder clearMessage() {
      
      message_ = getDefaultInstance().getMessage();
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The detailed message for the validation error.
     * </pre>
     *
     * <code>string message = 202;</code>
     * @param value The bytes for message to set.
     * @return This builder for chaining.
     */
    public Builder setMessageBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      message_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object attributePath_ = "";
    /**
     * <pre>
     * The path of the attribute that failed validation in the schema.
     * </pre>
     *
     * <code>string attribute_path = 203;</code>
     * @return The attributePath.
     */
    public java.lang.String getAttributePath() {
      java.lang.Object ref = attributePath_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        attributePath_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     * The path of the attribute that failed validation in the schema.
     * </pre>
     *
     * <code>string attribute_path = 203;</code>
     * @return The bytes for attributePath.
     */
    public com.google.protobuf.ByteString
        getAttributePathBytes() {
      java.lang.Object ref = attributePath_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        attributePath_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * The path of the attribute that failed validation in the schema.
     * </pre>
     *
     * <code>string attribute_path = 203;</code>
     * @param value The attributePath to set.
     * @return This builder for chaining.
     */
    public Builder setAttributePath(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      attributePath_ = value;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The path of the attribute that failed validation in the schema.
     * </pre>
     *
     * <code>string attribute_path = 203;</code>
     * @return This builder for chaining.
     */
    public Builder clearAttributePath() {
      
      attributePath_ = getDefaultInstance().getAttributePath();
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The path of the attribute that failed validation in the schema.
     * </pre>
     *
     * <code>string attribute_path = 203;</code>
     * @param value The bytes for attributePath to set.
     * @return This builder for chaining.
     */
    public Builder setAttributePathBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      attributePath_ = value;
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


    // @@protoc_insertion_point(builder_scope:proto3.prism.v4.error.SchemaValidationErrorMessage)
  }

  // @@protoc_insertion_point(class_scope:proto3.prism.v4.error.SchemaValidationErrorMessage)
  private static final proto3.prism.v4.error.SchemaValidationErrorMessage DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new proto3.prism.v4.error.SchemaValidationErrorMessage();
  }

  public static proto3.prism.v4.error.SchemaValidationErrorMessage getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<SchemaValidationErrorMessage>
      PARSER = new com.google.protobuf.AbstractParser<SchemaValidationErrorMessage>() {
    @java.lang.Override
    public SchemaValidationErrorMessage parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new SchemaValidationErrorMessage(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<SchemaValidationErrorMessage> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<SchemaValidationErrorMessage> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public proto3.prism.v4.error.SchemaValidationErrorMessage getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

