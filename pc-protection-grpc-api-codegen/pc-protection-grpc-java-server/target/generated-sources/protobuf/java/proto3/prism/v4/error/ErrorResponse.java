// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: proto3/prism/v4/error/error.proto

package proto3.prism.v4.error;

/**
 * <pre>
 * An error response indicates that the operation has failed either due to a client error(4XX) or server error(5XX). Please look at the HTTP status code and namespace specific error code and error message for further details.
 * </pre>
 *
 * Protobuf type {@code proto3.prism.v4.error.ErrorResponse}
 */
public final class ErrorResponse extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:proto3.prism.v4.error.ErrorResponse)
    ErrorResponseOrBuilder {
private static final long serialVersionUID = 0L;
  // Use ErrorResponse.newBuilder() to construct.
  private ErrorResponse(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private ErrorResponse() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new ErrorResponse();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private ErrorResponse(
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
            proto3.prism.v4.error.AppMessageArrayWrapper.Builder subBuilder = null;
            if (errorCase_ == 201) {
              subBuilder = ((proto3.prism.v4.error.AppMessageArrayWrapper) error_).toBuilder();
            }
            error_ =
                input.readMessage(proto3.prism.v4.error.AppMessageArrayWrapper.parser(), extensionRegistry);
            if (subBuilder != null) {
              subBuilder.mergeFrom((proto3.prism.v4.error.AppMessageArrayWrapper) error_);
              error_ = subBuilder.buildPartial();
            }
            errorCase_ = 201;
            break;
          }
          case 1618: {
            proto3.prism.v4.error.SchemaValidationErrorWrapper.Builder subBuilder = null;
            if (errorCase_ == 202) {
              subBuilder = ((proto3.prism.v4.error.SchemaValidationErrorWrapper) error_).toBuilder();
            }
            error_ =
                input.readMessage(proto3.prism.v4.error.SchemaValidationErrorWrapper.parser(), extensionRegistry);
            if (subBuilder != null) {
              subBuilder.mergeFrom((proto3.prism.v4.error.SchemaValidationErrorWrapper) error_);
              error_ = subBuilder.buildPartial();
            }
            errorCase_ = 202;
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
    return proto3.prism.v4.error.Error.internal_static_proto3_prism_v4_error_ErrorResponse_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return proto3.prism.v4.error.Error.internal_static_proto3_prism_v4_error_ErrorResponse_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            proto3.prism.v4.error.ErrorResponse.class, proto3.prism.v4.error.ErrorResponse.Builder.class);
  }

  private int errorCase_ = 0;
  private java.lang.Object error_;
  public enum ErrorCase
      implements com.google.protobuf.Internal.EnumLite,
          com.google.protobuf.AbstractMessage.InternalOneOfEnum {
    APP_MESSAGE_ARRAY_ERROR(201),
    SCHEMA_VALIDATION_ERROR_ERROR(202),
    ERROR_NOT_SET(0);
    private final int value;
    private ErrorCase(int value) {
      this.value = value;
    }
    /**
     * @param value The number of the enum to look for.
     * @return The enum associated with the given number.
     * @deprecated Use {@link #forNumber(int)} instead.
     */
    @java.lang.Deprecated
    public static ErrorCase valueOf(int value) {
      return forNumber(value);
    }

    public static ErrorCase forNumber(int value) {
      switch (value) {
        case 201: return APP_MESSAGE_ARRAY_ERROR;
        case 202: return SCHEMA_VALIDATION_ERROR_ERROR;
        case 0: return ERROR_NOT_SET;
        default: return null;
      }
    }
    public int getNumber() {
      return this.value;
    }
  };

  public ErrorCase
  getErrorCase() {
    return ErrorCase.forNumber(
        errorCase_);
  }

  public static final int APP_MESSAGE_ARRAY_ERROR_FIELD_NUMBER = 201;
  /**
   * <pre>
   * 
   * </pre>
   *
   * <code>.proto3.prism.v4.error.AppMessageArrayWrapper app_message_array_error = 201;</code>
   * @return Whether the appMessageArrayError field is set.
   */
  @java.lang.Override
  public boolean hasAppMessageArrayError() {
    return errorCase_ == 201;
  }
  /**
   * <pre>
   * 
   * </pre>
   *
   * <code>.proto3.prism.v4.error.AppMessageArrayWrapper app_message_array_error = 201;</code>
   * @return The appMessageArrayError.
   */
  @java.lang.Override
  public proto3.prism.v4.error.AppMessageArrayWrapper getAppMessageArrayError() {
    if (errorCase_ == 201) {
       return (proto3.prism.v4.error.AppMessageArrayWrapper) error_;
    }
    return proto3.prism.v4.error.AppMessageArrayWrapper.getDefaultInstance();
  }
  /**
   * <pre>
   * 
   * </pre>
   *
   * <code>.proto3.prism.v4.error.AppMessageArrayWrapper app_message_array_error = 201;</code>
   */
  @java.lang.Override
  public proto3.prism.v4.error.AppMessageArrayWrapperOrBuilder getAppMessageArrayErrorOrBuilder() {
    if (errorCase_ == 201) {
       return (proto3.prism.v4.error.AppMessageArrayWrapper) error_;
    }
    return proto3.prism.v4.error.AppMessageArrayWrapper.getDefaultInstance();
  }

  public static final int SCHEMA_VALIDATION_ERROR_ERROR_FIELD_NUMBER = 202;
  /**
   * <pre>
   * 
   * </pre>
   *
   * <code>.proto3.prism.v4.error.SchemaValidationErrorWrapper schema_validation_error_error = 202;</code>
   * @return Whether the schemaValidationErrorError field is set.
   */
  @java.lang.Override
  public boolean hasSchemaValidationErrorError() {
    return errorCase_ == 202;
  }
  /**
   * <pre>
   * 
   * </pre>
   *
   * <code>.proto3.prism.v4.error.SchemaValidationErrorWrapper schema_validation_error_error = 202;</code>
   * @return The schemaValidationErrorError.
   */
  @java.lang.Override
  public proto3.prism.v4.error.SchemaValidationErrorWrapper getSchemaValidationErrorError() {
    if (errorCase_ == 202) {
       return (proto3.prism.v4.error.SchemaValidationErrorWrapper) error_;
    }
    return proto3.prism.v4.error.SchemaValidationErrorWrapper.getDefaultInstance();
  }
  /**
   * <pre>
   * 
   * </pre>
   *
   * <code>.proto3.prism.v4.error.SchemaValidationErrorWrapper schema_validation_error_error = 202;</code>
   */
  @java.lang.Override
  public proto3.prism.v4.error.SchemaValidationErrorWrapperOrBuilder getSchemaValidationErrorErrorOrBuilder() {
    if (errorCase_ == 202) {
       return (proto3.prism.v4.error.SchemaValidationErrorWrapper) error_;
    }
    return proto3.prism.v4.error.SchemaValidationErrorWrapper.getDefaultInstance();
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
    if (errorCase_ == 201) {
      output.writeMessage(201, (proto3.prism.v4.error.AppMessageArrayWrapper) error_);
    }
    if (errorCase_ == 202) {
      output.writeMessage(202, (proto3.prism.v4.error.SchemaValidationErrorWrapper) error_);
    }
    unknownFields.writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (errorCase_ == 201) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(201, (proto3.prism.v4.error.AppMessageArrayWrapper) error_);
    }
    if (errorCase_ == 202) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(202, (proto3.prism.v4.error.SchemaValidationErrorWrapper) error_);
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
    if (!(obj instanceof proto3.prism.v4.error.ErrorResponse)) {
      return super.equals(obj);
    }
    proto3.prism.v4.error.ErrorResponse other = (proto3.prism.v4.error.ErrorResponse) obj;

    if (!getErrorCase().equals(other.getErrorCase())) return false;
    switch (errorCase_) {
      case 201:
        if (!getAppMessageArrayError()
            .equals(other.getAppMessageArrayError())) return false;
        break;
      case 202:
        if (!getSchemaValidationErrorError()
            .equals(other.getSchemaValidationErrorError())) return false;
        break;
      case 0:
      default:
    }
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
    switch (errorCase_) {
      case 201:
        hash = (37 * hash) + APP_MESSAGE_ARRAY_ERROR_FIELD_NUMBER;
        hash = (53 * hash) + getAppMessageArrayError().hashCode();
        break;
      case 202:
        hash = (37 * hash) + SCHEMA_VALIDATION_ERROR_ERROR_FIELD_NUMBER;
        hash = (53 * hash) + getSchemaValidationErrorError().hashCode();
        break;
      case 0:
      default:
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static proto3.prism.v4.error.ErrorResponse parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static proto3.prism.v4.error.ErrorResponse parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static proto3.prism.v4.error.ErrorResponse parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static proto3.prism.v4.error.ErrorResponse parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static proto3.prism.v4.error.ErrorResponse parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static proto3.prism.v4.error.ErrorResponse parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static proto3.prism.v4.error.ErrorResponse parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static proto3.prism.v4.error.ErrorResponse parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static proto3.prism.v4.error.ErrorResponse parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static proto3.prism.v4.error.ErrorResponse parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static proto3.prism.v4.error.ErrorResponse parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static proto3.prism.v4.error.ErrorResponse parseFrom(
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
  public static Builder newBuilder(proto3.prism.v4.error.ErrorResponse prototype) {
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
   * An error response indicates that the operation has failed either due to a client error(4XX) or server error(5XX). Please look at the HTTP status code and namespace specific error code and error message for further details.
   * </pre>
   *
   * Protobuf type {@code proto3.prism.v4.error.ErrorResponse}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:proto3.prism.v4.error.ErrorResponse)
      proto3.prism.v4.error.ErrorResponseOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return proto3.prism.v4.error.Error.internal_static_proto3_prism_v4_error_ErrorResponse_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return proto3.prism.v4.error.Error.internal_static_proto3_prism_v4_error_ErrorResponse_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              proto3.prism.v4.error.ErrorResponse.class, proto3.prism.v4.error.ErrorResponse.Builder.class);
    }

    // Construct using proto3.prism.v4.error.ErrorResponse.newBuilder()
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
      errorCase_ = 0;
      error_ = null;
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return proto3.prism.v4.error.Error.internal_static_proto3_prism_v4_error_ErrorResponse_descriptor;
    }

    @java.lang.Override
    public proto3.prism.v4.error.ErrorResponse getDefaultInstanceForType() {
      return proto3.prism.v4.error.ErrorResponse.getDefaultInstance();
    }

    @java.lang.Override
    public proto3.prism.v4.error.ErrorResponse build() {
      proto3.prism.v4.error.ErrorResponse result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public proto3.prism.v4.error.ErrorResponse buildPartial() {
      proto3.prism.v4.error.ErrorResponse result = new proto3.prism.v4.error.ErrorResponse(this);
      if (errorCase_ == 201) {
        if (appMessageArrayErrorBuilder_ == null) {
          result.error_ = error_;
        } else {
          result.error_ = appMessageArrayErrorBuilder_.build();
        }
      }
      if (errorCase_ == 202) {
        if (schemaValidationErrorErrorBuilder_ == null) {
          result.error_ = error_;
        } else {
          result.error_ = schemaValidationErrorErrorBuilder_.build();
        }
      }
      result.errorCase_ = errorCase_;
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
      if (other instanceof proto3.prism.v4.error.ErrorResponse) {
        return mergeFrom((proto3.prism.v4.error.ErrorResponse)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(proto3.prism.v4.error.ErrorResponse other) {
      if (other == proto3.prism.v4.error.ErrorResponse.getDefaultInstance()) return this;
      switch (other.getErrorCase()) {
        case APP_MESSAGE_ARRAY_ERROR: {
          mergeAppMessageArrayError(other.getAppMessageArrayError());
          break;
        }
        case SCHEMA_VALIDATION_ERROR_ERROR: {
          mergeSchemaValidationErrorError(other.getSchemaValidationErrorError());
          break;
        }
        case ERROR_NOT_SET: {
          break;
        }
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
      proto3.prism.v4.error.ErrorResponse parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (proto3.prism.v4.error.ErrorResponse) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }
    private int errorCase_ = 0;
    private java.lang.Object error_;
    public ErrorCase
        getErrorCase() {
      return ErrorCase.forNumber(
          errorCase_);
    }

    public Builder clearError() {
      errorCase_ = 0;
      error_ = null;
      onChanged();
      return this;
    }


    private com.google.protobuf.SingleFieldBuilderV3<
        proto3.prism.v4.error.AppMessageArrayWrapper, proto3.prism.v4.error.AppMessageArrayWrapper.Builder, proto3.prism.v4.error.AppMessageArrayWrapperOrBuilder> appMessageArrayErrorBuilder_;
    /**
     * <pre>
     * 
     * </pre>
     *
     * <code>.proto3.prism.v4.error.AppMessageArrayWrapper app_message_array_error = 201;</code>
     * @return Whether the appMessageArrayError field is set.
     */
    @java.lang.Override
    public boolean hasAppMessageArrayError() {
      return errorCase_ == 201;
    }
    /**
     * <pre>
     * 
     * </pre>
     *
     * <code>.proto3.prism.v4.error.AppMessageArrayWrapper app_message_array_error = 201;</code>
     * @return The appMessageArrayError.
     */
    @java.lang.Override
    public proto3.prism.v4.error.AppMessageArrayWrapper getAppMessageArrayError() {
      if (appMessageArrayErrorBuilder_ == null) {
        if (errorCase_ == 201) {
          return (proto3.prism.v4.error.AppMessageArrayWrapper) error_;
        }
        return proto3.prism.v4.error.AppMessageArrayWrapper.getDefaultInstance();
      } else {
        if (errorCase_ == 201) {
          return appMessageArrayErrorBuilder_.getMessage();
        }
        return proto3.prism.v4.error.AppMessageArrayWrapper.getDefaultInstance();
      }
    }
    /**
     * <pre>
     * 
     * </pre>
     *
     * <code>.proto3.prism.v4.error.AppMessageArrayWrapper app_message_array_error = 201;</code>
     */
    public Builder setAppMessageArrayError(proto3.prism.v4.error.AppMessageArrayWrapper value) {
      if (appMessageArrayErrorBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        error_ = value;
        onChanged();
      } else {
        appMessageArrayErrorBuilder_.setMessage(value);
      }
      errorCase_ = 201;
      return this;
    }
    /**
     * <pre>
     * 
     * </pre>
     *
     * <code>.proto3.prism.v4.error.AppMessageArrayWrapper app_message_array_error = 201;</code>
     */
    public Builder setAppMessageArrayError(
        proto3.prism.v4.error.AppMessageArrayWrapper.Builder builderForValue) {
      if (appMessageArrayErrorBuilder_ == null) {
        error_ = builderForValue.build();
        onChanged();
      } else {
        appMessageArrayErrorBuilder_.setMessage(builderForValue.build());
      }
      errorCase_ = 201;
      return this;
    }
    /**
     * <pre>
     * 
     * </pre>
     *
     * <code>.proto3.prism.v4.error.AppMessageArrayWrapper app_message_array_error = 201;</code>
     */
    public Builder mergeAppMessageArrayError(proto3.prism.v4.error.AppMessageArrayWrapper value) {
      if (appMessageArrayErrorBuilder_ == null) {
        if (errorCase_ == 201 &&
            error_ != proto3.prism.v4.error.AppMessageArrayWrapper.getDefaultInstance()) {
          error_ = proto3.prism.v4.error.AppMessageArrayWrapper.newBuilder((proto3.prism.v4.error.AppMessageArrayWrapper) error_)
              .mergeFrom(value).buildPartial();
        } else {
          error_ = value;
        }
        onChanged();
      } else {
        if (errorCase_ == 201) {
          appMessageArrayErrorBuilder_.mergeFrom(value);
        }
        appMessageArrayErrorBuilder_.setMessage(value);
      }
      errorCase_ = 201;
      return this;
    }
    /**
     * <pre>
     * 
     * </pre>
     *
     * <code>.proto3.prism.v4.error.AppMessageArrayWrapper app_message_array_error = 201;</code>
     */
    public Builder clearAppMessageArrayError() {
      if (appMessageArrayErrorBuilder_ == null) {
        if (errorCase_ == 201) {
          errorCase_ = 0;
          error_ = null;
          onChanged();
        }
      } else {
        if (errorCase_ == 201) {
          errorCase_ = 0;
          error_ = null;
        }
        appMessageArrayErrorBuilder_.clear();
      }
      return this;
    }
    /**
     * <pre>
     * 
     * </pre>
     *
     * <code>.proto3.prism.v4.error.AppMessageArrayWrapper app_message_array_error = 201;</code>
     */
    public proto3.prism.v4.error.AppMessageArrayWrapper.Builder getAppMessageArrayErrorBuilder() {
      return getAppMessageArrayErrorFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * 
     * </pre>
     *
     * <code>.proto3.prism.v4.error.AppMessageArrayWrapper app_message_array_error = 201;</code>
     */
    @java.lang.Override
    public proto3.prism.v4.error.AppMessageArrayWrapperOrBuilder getAppMessageArrayErrorOrBuilder() {
      if ((errorCase_ == 201) && (appMessageArrayErrorBuilder_ != null)) {
        return appMessageArrayErrorBuilder_.getMessageOrBuilder();
      } else {
        if (errorCase_ == 201) {
          return (proto3.prism.v4.error.AppMessageArrayWrapper) error_;
        }
        return proto3.prism.v4.error.AppMessageArrayWrapper.getDefaultInstance();
      }
    }
    /**
     * <pre>
     * 
     * </pre>
     *
     * <code>.proto3.prism.v4.error.AppMessageArrayWrapper app_message_array_error = 201;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        proto3.prism.v4.error.AppMessageArrayWrapper, proto3.prism.v4.error.AppMessageArrayWrapper.Builder, proto3.prism.v4.error.AppMessageArrayWrapperOrBuilder> 
        getAppMessageArrayErrorFieldBuilder() {
      if (appMessageArrayErrorBuilder_ == null) {
        if (!(errorCase_ == 201)) {
          error_ = proto3.prism.v4.error.AppMessageArrayWrapper.getDefaultInstance();
        }
        appMessageArrayErrorBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            proto3.prism.v4.error.AppMessageArrayWrapper, proto3.prism.v4.error.AppMessageArrayWrapper.Builder, proto3.prism.v4.error.AppMessageArrayWrapperOrBuilder>(
                (proto3.prism.v4.error.AppMessageArrayWrapper) error_,
                getParentForChildren(),
                isClean());
        error_ = null;
      }
      errorCase_ = 201;
      onChanged();;
      return appMessageArrayErrorBuilder_;
    }

    private com.google.protobuf.SingleFieldBuilderV3<
        proto3.prism.v4.error.SchemaValidationErrorWrapper, proto3.prism.v4.error.SchemaValidationErrorWrapper.Builder, proto3.prism.v4.error.SchemaValidationErrorWrapperOrBuilder> schemaValidationErrorErrorBuilder_;
    /**
     * <pre>
     * 
     * </pre>
     *
     * <code>.proto3.prism.v4.error.SchemaValidationErrorWrapper schema_validation_error_error = 202;</code>
     * @return Whether the schemaValidationErrorError field is set.
     */
    @java.lang.Override
    public boolean hasSchemaValidationErrorError() {
      return errorCase_ == 202;
    }
    /**
     * <pre>
     * 
     * </pre>
     *
     * <code>.proto3.prism.v4.error.SchemaValidationErrorWrapper schema_validation_error_error = 202;</code>
     * @return The schemaValidationErrorError.
     */
    @java.lang.Override
    public proto3.prism.v4.error.SchemaValidationErrorWrapper getSchemaValidationErrorError() {
      if (schemaValidationErrorErrorBuilder_ == null) {
        if (errorCase_ == 202) {
          return (proto3.prism.v4.error.SchemaValidationErrorWrapper) error_;
        }
        return proto3.prism.v4.error.SchemaValidationErrorWrapper.getDefaultInstance();
      } else {
        if (errorCase_ == 202) {
          return schemaValidationErrorErrorBuilder_.getMessage();
        }
        return proto3.prism.v4.error.SchemaValidationErrorWrapper.getDefaultInstance();
      }
    }
    /**
     * <pre>
     * 
     * </pre>
     *
     * <code>.proto3.prism.v4.error.SchemaValidationErrorWrapper schema_validation_error_error = 202;</code>
     */
    public Builder setSchemaValidationErrorError(proto3.prism.v4.error.SchemaValidationErrorWrapper value) {
      if (schemaValidationErrorErrorBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        error_ = value;
        onChanged();
      } else {
        schemaValidationErrorErrorBuilder_.setMessage(value);
      }
      errorCase_ = 202;
      return this;
    }
    /**
     * <pre>
     * 
     * </pre>
     *
     * <code>.proto3.prism.v4.error.SchemaValidationErrorWrapper schema_validation_error_error = 202;</code>
     */
    public Builder setSchemaValidationErrorError(
        proto3.prism.v4.error.SchemaValidationErrorWrapper.Builder builderForValue) {
      if (schemaValidationErrorErrorBuilder_ == null) {
        error_ = builderForValue.build();
        onChanged();
      } else {
        schemaValidationErrorErrorBuilder_.setMessage(builderForValue.build());
      }
      errorCase_ = 202;
      return this;
    }
    /**
     * <pre>
     * 
     * </pre>
     *
     * <code>.proto3.prism.v4.error.SchemaValidationErrorWrapper schema_validation_error_error = 202;</code>
     */
    public Builder mergeSchemaValidationErrorError(proto3.prism.v4.error.SchemaValidationErrorWrapper value) {
      if (schemaValidationErrorErrorBuilder_ == null) {
        if (errorCase_ == 202 &&
            error_ != proto3.prism.v4.error.SchemaValidationErrorWrapper.getDefaultInstance()) {
          error_ = proto3.prism.v4.error.SchemaValidationErrorWrapper.newBuilder((proto3.prism.v4.error.SchemaValidationErrorWrapper) error_)
              .mergeFrom(value).buildPartial();
        } else {
          error_ = value;
        }
        onChanged();
      } else {
        if (errorCase_ == 202) {
          schemaValidationErrorErrorBuilder_.mergeFrom(value);
        }
        schemaValidationErrorErrorBuilder_.setMessage(value);
      }
      errorCase_ = 202;
      return this;
    }
    /**
     * <pre>
     * 
     * </pre>
     *
     * <code>.proto3.prism.v4.error.SchemaValidationErrorWrapper schema_validation_error_error = 202;</code>
     */
    public Builder clearSchemaValidationErrorError() {
      if (schemaValidationErrorErrorBuilder_ == null) {
        if (errorCase_ == 202) {
          errorCase_ = 0;
          error_ = null;
          onChanged();
        }
      } else {
        if (errorCase_ == 202) {
          errorCase_ = 0;
          error_ = null;
        }
        schemaValidationErrorErrorBuilder_.clear();
      }
      return this;
    }
    /**
     * <pre>
     * 
     * </pre>
     *
     * <code>.proto3.prism.v4.error.SchemaValidationErrorWrapper schema_validation_error_error = 202;</code>
     */
    public proto3.prism.v4.error.SchemaValidationErrorWrapper.Builder getSchemaValidationErrorErrorBuilder() {
      return getSchemaValidationErrorErrorFieldBuilder().getBuilder();
    }
    /**
     * <pre>
     * 
     * </pre>
     *
     * <code>.proto3.prism.v4.error.SchemaValidationErrorWrapper schema_validation_error_error = 202;</code>
     */
    @java.lang.Override
    public proto3.prism.v4.error.SchemaValidationErrorWrapperOrBuilder getSchemaValidationErrorErrorOrBuilder() {
      if ((errorCase_ == 202) && (schemaValidationErrorErrorBuilder_ != null)) {
        return schemaValidationErrorErrorBuilder_.getMessageOrBuilder();
      } else {
        if (errorCase_ == 202) {
          return (proto3.prism.v4.error.SchemaValidationErrorWrapper) error_;
        }
        return proto3.prism.v4.error.SchemaValidationErrorWrapper.getDefaultInstance();
      }
    }
    /**
     * <pre>
     * 
     * </pre>
     *
     * <code>.proto3.prism.v4.error.SchemaValidationErrorWrapper schema_validation_error_error = 202;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        proto3.prism.v4.error.SchemaValidationErrorWrapper, proto3.prism.v4.error.SchemaValidationErrorWrapper.Builder, proto3.prism.v4.error.SchemaValidationErrorWrapperOrBuilder> 
        getSchemaValidationErrorErrorFieldBuilder() {
      if (schemaValidationErrorErrorBuilder_ == null) {
        if (!(errorCase_ == 202)) {
          error_ = proto3.prism.v4.error.SchemaValidationErrorWrapper.getDefaultInstance();
        }
        schemaValidationErrorErrorBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            proto3.prism.v4.error.SchemaValidationErrorWrapper, proto3.prism.v4.error.SchemaValidationErrorWrapper.Builder, proto3.prism.v4.error.SchemaValidationErrorWrapperOrBuilder>(
                (proto3.prism.v4.error.SchemaValidationErrorWrapper) error_,
                getParentForChildren(),
                isClean());
        error_ = null;
      }
      errorCase_ = 202;
      onChanged();;
      return schemaValidationErrorErrorBuilder_;
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


    // @@protoc_insertion_point(builder_scope:proto3.prism.v4.error.ErrorResponse)
  }

  // @@protoc_insertion_point(class_scope:proto3.prism.v4.error.ErrorResponse)
  private static final proto3.prism.v4.error.ErrorResponse DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new proto3.prism.v4.error.ErrorResponse();
  }

  public static proto3.prism.v4.error.ErrorResponse getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<ErrorResponse>
      PARSER = new com.google.protobuf.AbstractParser<ErrorResponse>() {
    @java.lang.Override
    public ErrorResponse parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new ErrorResponse(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<ErrorResponse> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<ErrorResponse> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public proto3.prism.v4.error.ErrorResponse getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

