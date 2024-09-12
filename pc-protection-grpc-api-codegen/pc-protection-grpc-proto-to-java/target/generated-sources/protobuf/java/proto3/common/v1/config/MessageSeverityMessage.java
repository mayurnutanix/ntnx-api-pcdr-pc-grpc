// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: proto3/common/v1/config/config.proto

package proto3.common.v1.config;

/**
 * <pre>
 * The message severity. 
 * </pre>
 *
 * Protobuf type {@code proto3.common.v1.config.MessageSeverityMessage}
 */
public final class MessageSeverityMessage extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:proto3.common.v1.config.MessageSeverityMessage)
    MessageSeverityMessageOrBuilder {
private static final long serialVersionUID = 0L;
  // Use MessageSeverityMessage.newBuilder() to construct.
  private MessageSeverityMessage(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private MessageSeverityMessage() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new MessageSeverityMessage();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private MessageSeverityMessage(
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
    return proto3.common.v1.config.Config.internal_static_proto3_common_v1_config_MessageSeverityMessage_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return proto3.common.v1.config.Config.internal_static_proto3_common_v1_config_MessageSeverityMessage_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            proto3.common.v1.config.MessageSeverityMessage.class, proto3.common.v1.config.MessageSeverityMessage.Builder.class);
  }

  /**
   * Protobuf enum {@code proto3.common.v1.config.MessageSeverityMessage.MessageSeverity}
   */
  public enum MessageSeverity
      implements com.google.protobuf.ProtocolMessageEnum {
    /**
     * <code>UNKNOWN = 0;</code>
     */
    UNKNOWN(0),
    /**
     * <code>REDACTED = 1;</code>
     */
    REDACTED(1),
    /**
     * <code>INFO = 1001;</code>
     */
    INFO(1001),
    /**
     * <code>WARNING = 1002;</code>
     */
    WARNING(1002),
    /**
     * <code>ERROR = 1003;</code>
     */
    ERROR(1003),
    UNRECOGNIZED(-1),
    ;

    /**
     * <code>UNKNOWN = 0;</code>
     */
    public static final int UNKNOWN_VALUE = 0;
    /**
     * <code>REDACTED = 1;</code>
     */
    public static final int REDACTED_VALUE = 1;
    /**
     * <code>INFO = 1001;</code>
     */
    public static final int INFO_VALUE = 1001;
    /**
     * <code>WARNING = 1002;</code>
     */
    public static final int WARNING_VALUE = 1002;
    /**
     * <code>ERROR = 1003;</code>
     */
    public static final int ERROR_VALUE = 1003;


    public final int getNumber() {
      if (this == UNRECOGNIZED) {
        throw new java.lang.IllegalArgumentException(
            "Can't get the number of an unknown enum value.");
      }
      return value;
    }

    /**
     * @param value The numeric wire value of the corresponding enum entry.
     * @return The enum associated with the given numeric wire value.
     * @deprecated Use {@link #forNumber(int)} instead.
     */
    @java.lang.Deprecated
    public static MessageSeverity valueOf(int value) {
      return forNumber(value);
    }

    /**
     * @param value The numeric wire value of the corresponding enum entry.
     * @return The enum associated with the given numeric wire value.
     */
    public static MessageSeverity forNumber(int value) {
      switch (value) {
        case 0: return UNKNOWN;
        case 1: return REDACTED;
        case 1001: return INFO;
        case 1002: return WARNING;
        case 1003: return ERROR;
        default: return null;
      }
    }

    public static com.google.protobuf.Internal.EnumLiteMap<MessageSeverity>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static final com.google.protobuf.Internal.EnumLiteMap<
        MessageSeverity> internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<MessageSeverity>() {
            public MessageSeverity findValueByNumber(int number) {
              return MessageSeverity.forNumber(number);
            }
          };

    public final com.google.protobuf.Descriptors.EnumValueDescriptor
        getValueDescriptor() {
      if (this == UNRECOGNIZED) {
        throw new java.lang.IllegalStateException(
            "Can't get the descriptor of an unrecognized enum value.");
      }
      return getDescriptor().getValues().get(ordinal());
    }
    public final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptorForType() {
      return getDescriptor();
    }
    public static final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptor() {
      return proto3.common.v1.config.MessageSeverityMessage.getDescriptor().getEnumTypes().get(0);
    }

    private static final MessageSeverity[] VALUES = values();

    public static MessageSeverity valueOf(
        com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
      if (desc.getType() != getDescriptor()) {
        throw new java.lang.IllegalArgumentException(
          "EnumValueDescriptor is not for this type.");
      }
      if (desc.getIndex() == -1) {
        return UNRECOGNIZED;
      }
      return VALUES[desc.getIndex()];
    }

    private final int value;

    private MessageSeverity(int value) {
      this.value = value;
    }

    // @@protoc_insertion_point(enum_scope:proto3.common.v1.config.MessageSeverityMessage.MessageSeverity)
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
    unknownFields.writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    size += unknownFields.getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof proto3.common.v1.config.MessageSeverityMessage)) {
      return super.equals(obj);
    }
    proto3.common.v1.config.MessageSeverityMessage other = (proto3.common.v1.config.MessageSeverityMessage) obj;

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
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static proto3.common.v1.config.MessageSeverityMessage parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static proto3.common.v1.config.MessageSeverityMessage parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static proto3.common.v1.config.MessageSeverityMessage parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static proto3.common.v1.config.MessageSeverityMessage parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static proto3.common.v1.config.MessageSeverityMessage parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static proto3.common.v1.config.MessageSeverityMessage parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static proto3.common.v1.config.MessageSeverityMessage parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static proto3.common.v1.config.MessageSeverityMessage parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static proto3.common.v1.config.MessageSeverityMessage parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static proto3.common.v1.config.MessageSeverityMessage parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static proto3.common.v1.config.MessageSeverityMessage parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static proto3.common.v1.config.MessageSeverityMessage parseFrom(
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
  public static Builder newBuilder(proto3.common.v1.config.MessageSeverityMessage prototype) {
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
   * The message severity. 
   * </pre>
   *
   * Protobuf type {@code proto3.common.v1.config.MessageSeverityMessage}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:proto3.common.v1.config.MessageSeverityMessage)
      proto3.common.v1.config.MessageSeverityMessageOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return proto3.common.v1.config.Config.internal_static_proto3_common_v1_config_MessageSeverityMessage_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return proto3.common.v1.config.Config.internal_static_proto3_common_v1_config_MessageSeverityMessage_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              proto3.common.v1.config.MessageSeverityMessage.class, proto3.common.v1.config.MessageSeverityMessage.Builder.class);
    }

    // Construct using proto3.common.v1.config.MessageSeverityMessage.newBuilder()
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
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return proto3.common.v1.config.Config.internal_static_proto3_common_v1_config_MessageSeverityMessage_descriptor;
    }

    @java.lang.Override
    public proto3.common.v1.config.MessageSeverityMessage getDefaultInstanceForType() {
      return proto3.common.v1.config.MessageSeverityMessage.getDefaultInstance();
    }

    @java.lang.Override
    public proto3.common.v1.config.MessageSeverityMessage build() {
      proto3.common.v1.config.MessageSeverityMessage result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public proto3.common.v1.config.MessageSeverityMessage buildPartial() {
      proto3.common.v1.config.MessageSeverityMessage result = new proto3.common.v1.config.MessageSeverityMessage(this);
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
      if (other instanceof proto3.common.v1.config.MessageSeverityMessage) {
        return mergeFrom((proto3.common.v1.config.MessageSeverityMessage)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(proto3.common.v1.config.MessageSeverityMessage other) {
      if (other == proto3.common.v1.config.MessageSeverityMessage.getDefaultInstance()) return this;
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
      proto3.common.v1.config.MessageSeverityMessage parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (proto3.common.v1.config.MessageSeverityMessage) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
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


    // @@protoc_insertion_point(builder_scope:proto3.common.v1.config.MessageSeverityMessage)
  }

  // @@protoc_insertion_point(class_scope:proto3.common.v1.config.MessageSeverityMessage)
  private static final proto3.common.v1.config.MessageSeverityMessage DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new proto3.common.v1.config.MessageSeverityMessage();
  }

  public static proto3.common.v1.config.MessageSeverityMessage getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<MessageSeverityMessage>
      PARSER = new com.google.protobuf.AbstractParser<MessageSeverityMessage>() {
    @java.lang.Override
    public MessageSeverityMessage parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new MessageSeverityMessage(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<MessageSeverityMessage> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<MessageSeverityMessage> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public proto3.common.v1.config.MessageSeverityMessage getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

