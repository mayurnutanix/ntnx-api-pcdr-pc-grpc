// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: proto3/common/v1/config/config.proto

package proto3.common.v1.config;

/**
 * <pre>
 * 
 * </pre>
 *
 * Protobuf type {@code proto3.common.v1.config.Message}
 */
public final class Message extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:proto3.common.v1.config.Message)
    MessageOrBuilder {
private static final long serialVersionUID = 0L;
  // Use Message.newBuilder() to construct.
  private Message(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private Message() {
    code_ = "";
    message_ = "";
    locale_ = "";
    severity_ = 0;
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new Message();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private Message(
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

            code_ = s;
            break;
          }
          case 8018: {
            java.lang.String s = input.readStringRequireUtf8();

            message_ = s;
            break;
          }
          case 8026: {
            java.lang.String s = input.readStringRequireUtf8();

            locale_ = s;
            break;
          }
          case 8032: {
            int rawValue = input.readEnum();

            severity_ = rawValue;
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
    return proto3.common.v1.config.Config.internal_static_proto3_common_v1_config_Message_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return proto3.common.v1.config.Config.internal_static_proto3_common_v1_config_Message_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            proto3.common.v1.config.Message.class, proto3.common.v1.config.Message.Builder.class);
  }

  public static final int CODE_FIELD_NUMBER = 1001;
  private volatile java.lang.Object code_;
  /**
   * <pre>
   * A code that uniquely identifies a message. 
   * </pre>
   *
   * <code>string code = 1001;</code>
   * @return The code.
   */
  @java.lang.Override
  public java.lang.String getCode() {
    java.lang.Object ref = code_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      code_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * A code that uniquely identifies a message. 
   * </pre>
   *
   * <code>string code = 1001;</code>
   * @return The bytes for code.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getCodeBytes() {
    java.lang.Object ref = code_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      code_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int MESSAGE_FIELD_NUMBER = 1002;
  private volatile java.lang.Object message_;
  /**
   * <pre>
   * The description of the message. 
   * </pre>
   *
   * <code>string message = 1002;</code>
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
   * The description of the message. 
   * </pre>
   *
   * <code>string message = 1002;</code>
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

  public static final int LOCALE_FIELD_NUMBER = 1003;
  private volatile java.lang.Object locale_;
  /**
   * <pre>
   * The locale for the message description. 
   * </pre>
   *
   * <code>string locale = 1003;</code>
   * @return The locale.
   */
  @java.lang.Override
  public java.lang.String getLocale() {
    java.lang.Object ref = locale_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      locale_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * The locale for the message description. 
   * </pre>
   *
   * <code>string locale = 1003;</code>
   * @return The bytes for locale.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getLocaleBytes() {
    java.lang.Object ref = locale_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      locale_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int SEVERITY_FIELD_NUMBER = 1004;
  private int severity_;
  /**
   * <pre>
   * 
   * </pre>
   *
   * <code>.proto3.common.v1.config.MessageSeverityMessage.MessageSeverity severity = 1004;</code>
   * @return The enum numeric value on the wire for severity.
   */
  @java.lang.Override public int getSeverityValue() {
    return severity_;
  }
  /**
   * <pre>
   * 
   * </pre>
   *
   * <code>.proto3.common.v1.config.MessageSeverityMessage.MessageSeverity severity = 1004;</code>
   * @return The severity.
   */
  @java.lang.Override public proto3.common.v1.config.MessageSeverityMessage.MessageSeverity getSeverity() {
    @SuppressWarnings("deprecation")
    proto3.common.v1.config.MessageSeverityMessage.MessageSeverity result = proto3.common.v1.config.MessageSeverityMessage.MessageSeverity.valueOf(severity_);
    return result == null ? proto3.common.v1.config.MessageSeverityMessage.MessageSeverity.UNRECOGNIZED : result;
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
    if (!getCodeBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1001, code_);
    }
    if (!getMessageBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1002, message_);
    }
    if (!getLocaleBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1003, locale_);
    }
    if (severity_ != proto3.common.v1.config.MessageSeverityMessage.MessageSeverity.UNKNOWN.getNumber()) {
      output.writeEnum(1004, severity_);
    }
    unknownFields.writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!getCodeBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1001, code_);
    }
    if (!getMessageBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1002, message_);
    }
    if (!getLocaleBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1003, locale_);
    }
    if (severity_ != proto3.common.v1.config.MessageSeverityMessage.MessageSeverity.UNKNOWN.getNumber()) {
      size += com.google.protobuf.CodedOutputStream
        .computeEnumSize(1004, severity_);
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
    if (!(obj instanceof proto3.common.v1.config.Message)) {
      return super.equals(obj);
    }
    proto3.common.v1.config.Message other = (proto3.common.v1.config.Message) obj;

    if (!getCode()
        .equals(other.getCode())) return false;
    if (!getMessage()
        .equals(other.getMessage())) return false;
    if (!getLocale()
        .equals(other.getLocale())) return false;
    if (severity_ != other.severity_) return false;
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
    hash = (37 * hash) + CODE_FIELD_NUMBER;
    hash = (53 * hash) + getCode().hashCode();
    hash = (37 * hash) + MESSAGE_FIELD_NUMBER;
    hash = (53 * hash) + getMessage().hashCode();
    hash = (37 * hash) + LOCALE_FIELD_NUMBER;
    hash = (53 * hash) + getLocale().hashCode();
    hash = (37 * hash) + SEVERITY_FIELD_NUMBER;
    hash = (53 * hash) + severity_;
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static proto3.common.v1.config.Message parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static proto3.common.v1.config.Message parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static proto3.common.v1.config.Message parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static proto3.common.v1.config.Message parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static proto3.common.v1.config.Message parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static proto3.common.v1.config.Message parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static proto3.common.v1.config.Message parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static proto3.common.v1.config.Message parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static proto3.common.v1.config.Message parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static proto3.common.v1.config.Message parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static proto3.common.v1.config.Message parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static proto3.common.v1.config.Message parseFrom(
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
  public static Builder newBuilder(proto3.common.v1.config.Message prototype) {
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
   * 
   * </pre>
   *
   * Protobuf type {@code proto3.common.v1.config.Message}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:proto3.common.v1.config.Message)
      proto3.common.v1.config.MessageOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return proto3.common.v1.config.Config.internal_static_proto3_common_v1_config_Message_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return proto3.common.v1.config.Config.internal_static_proto3_common_v1_config_Message_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              proto3.common.v1.config.Message.class, proto3.common.v1.config.Message.Builder.class);
    }

    // Construct using proto3.common.v1.config.Message.newBuilder()
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
      code_ = "";

      message_ = "";

      locale_ = "";

      severity_ = 0;

      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return proto3.common.v1.config.Config.internal_static_proto3_common_v1_config_Message_descriptor;
    }

    @java.lang.Override
    public proto3.common.v1.config.Message getDefaultInstanceForType() {
      return proto3.common.v1.config.Message.getDefaultInstance();
    }

    @java.lang.Override
    public proto3.common.v1.config.Message build() {
      proto3.common.v1.config.Message result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public proto3.common.v1.config.Message buildPartial() {
      proto3.common.v1.config.Message result = new proto3.common.v1.config.Message(this);
      result.code_ = code_;
      result.message_ = message_;
      result.locale_ = locale_;
      result.severity_ = severity_;
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
      if (other instanceof proto3.common.v1.config.Message) {
        return mergeFrom((proto3.common.v1.config.Message)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(proto3.common.v1.config.Message other) {
      if (other == proto3.common.v1.config.Message.getDefaultInstance()) return this;
      if (!other.getCode().isEmpty()) {
        code_ = other.code_;
        onChanged();
      }
      if (!other.getMessage().isEmpty()) {
        message_ = other.message_;
        onChanged();
      }
      if (!other.getLocale().isEmpty()) {
        locale_ = other.locale_;
        onChanged();
      }
      if (other.severity_ != 0) {
        setSeverityValue(other.getSeverityValue());
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
      proto3.common.v1.config.Message parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (proto3.common.v1.config.Message) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private java.lang.Object code_ = "";
    /**
     * <pre>
     * A code that uniquely identifies a message. 
     * </pre>
     *
     * <code>string code = 1001;</code>
     * @return The code.
     */
    public java.lang.String getCode() {
      java.lang.Object ref = code_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        code_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     * A code that uniquely identifies a message. 
     * </pre>
     *
     * <code>string code = 1001;</code>
     * @return The bytes for code.
     */
    public com.google.protobuf.ByteString
        getCodeBytes() {
      java.lang.Object ref = code_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        code_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * A code that uniquely identifies a message. 
     * </pre>
     *
     * <code>string code = 1001;</code>
     * @param value The code to set.
     * @return This builder for chaining.
     */
    public Builder setCode(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      code_ = value;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * A code that uniquely identifies a message. 
     * </pre>
     *
     * <code>string code = 1001;</code>
     * @return This builder for chaining.
     */
    public Builder clearCode() {
      
      code_ = getDefaultInstance().getCode();
      onChanged();
      return this;
    }
    /**
     * <pre>
     * A code that uniquely identifies a message. 
     * </pre>
     *
     * <code>string code = 1001;</code>
     * @param value The bytes for code to set.
     * @return This builder for chaining.
     */
    public Builder setCodeBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      code_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object message_ = "";
    /**
     * <pre>
     * The description of the message. 
     * </pre>
     *
     * <code>string message = 1002;</code>
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
     * The description of the message. 
     * </pre>
     *
     * <code>string message = 1002;</code>
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
     * The description of the message. 
     * </pre>
     *
     * <code>string message = 1002;</code>
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
     * The description of the message. 
     * </pre>
     *
     * <code>string message = 1002;</code>
     * @return This builder for chaining.
     */
    public Builder clearMessage() {
      
      message_ = getDefaultInstance().getMessage();
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The description of the message. 
     * </pre>
     *
     * <code>string message = 1002;</code>
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

    private java.lang.Object locale_ = "";
    /**
     * <pre>
     * The locale for the message description. 
     * </pre>
     *
     * <code>string locale = 1003;</code>
     * @return The locale.
     */
    public java.lang.String getLocale() {
      java.lang.Object ref = locale_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        locale_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     * The locale for the message description. 
     * </pre>
     *
     * <code>string locale = 1003;</code>
     * @return The bytes for locale.
     */
    public com.google.protobuf.ByteString
        getLocaleBytes() {
      java.lang.Object ref = locale_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        locale_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * The locale for the message description. 
     * </pre>
     *
     * <code>string locale = 1003;</code>
     * @param value The locale to set.
     * @return This builder for chaining.
     */
    public Builder setLocale(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      locale_ = value;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The locale for the message description. 
     * </pre>
     *
     * <code>string locale = 1003;</code>
     * @return This builder for chaining.
     */
    public Builder clearLocale() {
      
      locale_ = getDefaultInstance().getLocale();
      onChanged();
      return this;
    }
    /**
     * <pre>
     * The locale for the message description. 
     * </pre>
     *
     * <code>string locale = 1003;</code>
     * @param value The bytes for locale to set.
     * @return This builder for chaining.
     */
    public Builder setLocaleBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      locale_ = value;
      onChanged();
      return this;
    }

    private int severity_ = 0;
    /**
     * <pre>
     * 
     * </pre>
     *
     * <code>.proto3.common.v1.config.MessageSeverityMessage.MessageSeverity severity = 1004;</code>
     * @return The enum numeric value on the wire for severity.
     */
    @java.lang.Override public int getSeverityValue() {
      return severity_;
    }
    /**
     * <pre>
     * 
     * </pre>
     *
     * <code>.proto3.common.v1.config.MessageSeverityMessage.MessageSeverity severity = 1004;</code>
     * @param value The enum numeric value on the wire for severity to set.
     * @return This builder for chaining.
     */
    public Builder setSeverityValue(int value) {
      
      severity_ = value;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * 
     * </pre>
     *
     * <code>.proto3.common.v1.config.MessageSeverityMessage.MessageSeverity severity = 1004;</code>
     * @return The severity.
     */
    @java.lang.Override
    public proto3.common.v1.config.MessageSeverityMessage.MessageSeverity getSeverity() {
      @SuppressWarnings("deprecation")
      proto3.common.v1.config.MessageSeverityMessage.MessageSeverity result = proto3.common.v1.config.MessageSeverityMessage.MessageSeverity.valueOf(severity_);
      return result == null ? proto3.common.v1.config.MessageSeverityMessage.MessageSeverity.UNRECOGNIZED : result;
    }
    /**
     * <pre>
     * 
     * </pre>
     *
     * <code>.proto3.common.v1.config.MessageSeverityMessage.MessageSeverity severity = 1004;</code>
     * @param value The severity to set.
     * @return This builder for chaining.
     */
    public Builder setSeverity(proto3.common.v1.config.MessageSeverityMessage.MessageSeverity value) {
      if (value == null) {
        throw new NullPointerException();
      }
      
      severity_ = value.getNumber();
      onChanged();
      return this;
    }
    /**
     * <pre>
     * 
     * </pre>
     *
     * <code>.proto3.common.v1.config.MessageSeverityMessage.MessageSeverity severity = 1004;</code>
     * @return This builder for chaining.
     */
    public Builder clearSeverity() {
      
      severity_ = 0;
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


    // @@protoc_insertion_point(builder_scope:proto3.common.v1.config.Message)
  }

  // @@protoc_insertion_point(class_scope:proto3.common.v1.config.Message)
  private static final proto3.common.v1.config.Message DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new proto3.common.v1.config.Message();
  }

  public static proto3.common.v1.config.Message getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<Message>
      PARSER = new com.google.protobuf.AbstractParser<Message>() {
    @java.lang.Override
    public Message parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new Message(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<Message> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<Message> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public proto3.common.v1.config.Message getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}
