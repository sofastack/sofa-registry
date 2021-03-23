/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.registry.common.model.client.pb;

/** Protobuf type {@code RegisterResponsePb} */
public final class RegisterResponsePb extends com.google.protobuf.GeneratedMessageV3
    implements
    // @@protoc_insertion_point(message_implements:RegisterResponsePb)
    RegisterResponsePbOrBuilder {
  private static final long serialVersionUID = 0L;
  // Use RegisterResponsePb.newBuilder() to construct.
  private RegisterResponsePb(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }

  private RegisterResponsePb() {
    success_ = false;
    registId_ = "";
    version_ = 0L;
    refused_ = false;
    message_ = "";
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
    return this.unknownFields;
  }

  private RegisterResponsePb(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    if (extensionRegistry == null) {
      throw new java.lang.NullPointerException();
    }
    int mutable_bitField0_ = 0;
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
          default:
            {
              if (!parseUnknownFieldProto3(input, unknownFields, extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
          case 8:
            {
              success_ = input.readBool();
              break;
            }
          case 18:
            {
              java.lang.String s = input.readStringRequireUtf8();

              registId_ = s;
              break;
            }
          case 24:
            {
              version_ = input.readInt64();
              break;
            }
          case 32:
            {
              refused_ = input.readBool();
              break;
            }
          case 42:
            {
              java.lang.String s = input.readStringRequireUtf8();

              message_ = s;
              break;
            }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(e).setUnfinishedMessage(this);
    } finally {
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }

  public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
    return com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePbOuterClass
        .internal_static_RegisterResponsePb_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePbOuterClass
        .internal_static_RegisterResponsePb_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb.class,
            com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb.Builder.class);
  }

  public static final int SUCCESS_FIELD_NUMBER = 1;
  private boolean success_;
  /** <code>bool success = 1;</code> */
  public boolean getSuccess() {
    return success_;
  }

  public static final int REGISTID_FIELD_NUMBER = 2;
  private volatile java.lang.Object registId_;
  /** <code>string registId = 2;</code> */
  public java.lang.String getRegistId() {
    java.lang.Object ref = registId_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      registId_ = s;
      return s;
    }
  }
  /** <code>string registId = 2;</code> */
  public com.google.protobuf.ByteString getRegistIdBytes() {
    java.lang.Object ref = registId_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b =
          com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
      registId_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int VERSION_FIELD_NUMBER = 3;
  private long version_;
  /** <code>int64 version = 3;</code> */
  public long getVersion() {
    return version_;
  }

  public static final int REFUSED_FIELD_NUMBER = 4;
  private boolean refused_;
  /** <code>bool refused = 4;</code> */
  public boolean getRefused() {
    return refused_;
  }

  public static final int MESSAGE_FIELD_NUMBER = 5;
  private volatile java.lang.Object message_;
  /** <code>string message = 5;</code> */
  public java.lang.String getMessage() {
    java.lang.Object ref = message_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      message_ = s;
      return s;
    }
  }
  /** <code>string message = 5;</code> */
  public com.google.protobuf.ByteString getMessageBytes() {
    java.lang.Object ref = message_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b =
          com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
      message_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  private byte memoizedIsInitialized = -1;

  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
    if (success_ != false) {
      output.writeBool(1, success_);
    }
    if (!getRegistIdBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 2, registId_);
    }
    if (version_ != 0L) {
      output.writeInt64(3, version_);
    }
    if (refused_ != false) {
      output.writeBool(4, refused_);
    }
    if (!getMessageBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 5, message_);
    }
    unknownFields.writeTo(output);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (success_ != false) {
      size += com.google.protobuf.CodedOutputStream.computeBoolSize(1, success_);
    }
    if (!getRegistIdBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, registId_);
    }
    if (version_ != 0L) {
      size += com.google.protobuf.CodedOutputStream.computeInt64Size(3, version_);
    }
    if (refused_ != false) {
      size += com.google.protobuf.CodedOutputStream.computeBoolSize(4, refused_);
    }
    if (!getMessageBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(5, message_);
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
    if (!(obj instanceof com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb)) {
      return super.equals(obj);
    }
    com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb other =
        (com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb) obj;

    boolean result = true;
    result = result && (getSuccess() == other.getSuccess());
    result = result && getRegistId().equals(other.getRegistId());
    result = result && (getVersion() == other.getVersion());
    result = result && (getRefused() == other.getRefused());
    result = result && getMessage().equals(other.getMessage());
    result = result && unknownFields.equals(other.unknownFields);
    return result;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    hash = (37 * hash) + SUCCESS_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashBoolean(getSuccess());
    hash = (37 * hash) + REGISTID_FIELD_NUMBER;
    hash = (53 * hash) + getRegistId().hashCode();
    hash = (37 * hash) + VERSION_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(getVersion());
    hash = (37 * hash) + REFUSED_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashBoolean(getRefused());
    hash = (37 * hash) + MESSAGE_FIELD_NUMBER;
    hash = (53 * hash) + getMessage().hashCode();
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb parseFrom(
      java.nio.ByteBuffer data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb parseFrom(
      java.nio.ByteBuffer data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb parseFrom(
      byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb parseFrom(
      byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb parseFrom(
      java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb parseFrom(
      java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb
      parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb
      parseDelimitedFrom(
          java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb parseFrom(
      com.google.protobuf.CodedInputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(
        PARSER, input, extensionRegistry);
  }

  public Builder newBuilderForType() {
    return newBuilder();
  }

  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }

  public static Builder newBuilder(
      com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }

  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /** Protobuf type {@code RegisterResponsePb} */
  public static final class Builder extends com.google.protobuf.GeneratedMessageV3.Builder<Builder>
      implements
      // @@protoc_insertion_point(builder_implements:RegisterResponsePb)
      com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePbOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
      return com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePbOuterClass
          .internal_static_RegisterResponsePb_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePbOuterClass
          .internal_static_RegisterResponsePb_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb.class,
              com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb.Builder.class);
    }

    // Construct using
    // com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }

    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders) {}
    }

    public Builder clear() {
      super.clear();
      success_ = false;

      registId_ = "";

      version_ = 0L;

      refused_ = false;

      message_ = "";

      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
      return com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePbOuterClass
          .internal_static_RegisterResponsePb_descriptor;
    }

    public com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb
        getDefaultInstanceForType() {
      return com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb
          .getDefaultInstance();
    }

    public com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb build() {
      com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb buildPartial() {
      com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb result =
          new com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb(this);
      result.success_ = success_;
      result.registId_ = registId_;
      result.version_ = version_;
      result.refused_ = refused_;
      result.message_ = message_;
      onBuilt();
      return result;
    }

    public Builder clone() {
      return (Builder) super.clone();
    }

    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field, java.lang.Object value) {
      return (Builder) super.setField(field, value);
    }

    public Builder clearField(com.google.protobuf.Descriptors.FieldDescriptor field) {
      return (Builder) super.clearField(field);
    }

    public Builder clearOneof(com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return (Builder) super.clearOneof(oneof);
    }

    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field, int index, java.lang.Object value) {
      return (Builder) super.setRepeatedField(field, index, value);
    }

    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field, java.lang.Object value) {
      return (Builder) super.addRepeatedField(field, value);
    }

    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb) {
        return mergeFrom(
            (com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb) other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(
        com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb other) {
      if (other
          == com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb
              .getDefaultInstance()) return this;
      if (other.getSuccess() != false) {
        setSuccess(other.getSuccess());
      }
      if (!other.getRegistId().isEmpty()) {
        registId_ = other.registId_;
        onChanged();
      }
      if (other.getVersion() != 0L) {
        setVersion(other.getVersion());
      }
      if (other.getRefused() != false) {
        setRefused(other.getRefused());
      }
      if (!other.getMessage().isEmpty()) {
        message_ = other.message_;
        onChanged();
      }
      this.mergeUnknownFields(other.unknownFields);
      onChanged();
      return this;
    }

    public final boolean isInitialized() {
      return true;
    }

    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage =
            (com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb)
                e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private boolean success_;
    /** <code>bool success = 1;</code> */
    public boolean getSuccess() {
      return success_;
    }
    /** <code>bool success = 1;</code> */
    public Builder setSuccess(boolean value) {

      success_ = value;
      onChanged();
      return this;
    }
    /** <code>bool success = 1;</code> */
    public Builder clearSuccess() {

      success_ = false;
      onChanged();
      return this;
    }

    private java.lang.Object registId_ = "";
    /** <code>string registId = 2;</code> */
    public java.lang.String getRegistId() {
      java.lang.Object ref = registId_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        registId_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /** <code>string registId = 2;</code> */
    public com.google.protobuf.ByteString getRegistIdBytes() {
      java.lang.Object ref = registId_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
            com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
        registId_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /** <code>string registId = 2;</code> */
    public Builder setRegistId(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      registId_ = value;
      onChanged();
      return this;
    }
    /** <code>string registId = 2;</code> */
    public Builder clearRegistId() {

      registId_ = getDefaultInstance().getRegistId();
      onChanged();
      return this;
    }
    /** <code>string registId = 2;</code> */
    public Builder setRegistIdBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);

      registId_ = value;
      onChanged();
      return this;
    }

    private long version_;
    /** <code>int64 version = 3;</code> */
    public long getVersion() {
      return version_;
    }
    /** <code>int64 version = 3;</code> */
    public Builder setVersion(long value) {

      version_ = value;
      onChanged();
      return this;
    }
    /** <code>int64 version = 3;</code> */
    public Builder clearVersion() {

      version_ = 0L;
      onChanged();
      return this;
    }

    private boolean refused_;
    /** <code>bool refused = 4;</code> */
    public boolean getRefused() {
      return refused_;
    }
    /** <code>bool refused = 4;</code> */
    public Builder setRefused(boolean value) {

      refused_ = value;
      onChanged();
      return this;
    }
    /** <code>bool refused = 4;</code> */
    public Builder clearRefused() {

      refused_ = false;
      onChanged();
      return this;
    }

    private java.lang.Object message_ = "";
    /** <code>string message = 5;</code> */
    public java.lang.String getMessage() {
      java.lang.Object ref = message_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        message_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /** <code>string message = 5;</code> */
    public com.google.protobuf.ByteString getMessageBytes() {
      java.lang.Object ref = message_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
            com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
        message_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /** <code>string message = 5;</code> */
    public Builder setMessage(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      message_ = value;
      onChanged();
      return this;
    }
    /** <code>string message = 5;</code> */
    public Builder clearMessage() {

      message_ = getDefaultInstance().getMessage();
      onChanged();
      return this;
    }
    /** <code>string message = 5;</code> */
    public Builder setMessageBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);

      message_ = value;
      onChanged();
      return this;
    }

    public final Builder setUnknownFields(final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFieldsProto3(unknownFields);
    }

    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }

    // @@protoc_insertion_point(builder_scope:RegisterResponsePb)
  }

  // @@protoc_insertion_point(class_scope:RegisterResponsePb)
  private static final com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb
      DEFAULT_INSTANCE;

  static {
    DEFAULT_INSTANCE = new com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb();
  }

  public static com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb
      getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<RegisterResponsePb> PARSER =
      new com.google.protobuf.AbstractParser<RegisterResponsePb>() {
        public RegisterResponsePb parsePartialFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
          return new RegisterResponsePb(input, extensionRegistry);
        }
      };

  public static com.google.protobuf.Parser<RegisterResponsePb> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<RegisterResponsePb> getParserForType() {
    return PARSER;
  }

  public com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb
      getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }
}
