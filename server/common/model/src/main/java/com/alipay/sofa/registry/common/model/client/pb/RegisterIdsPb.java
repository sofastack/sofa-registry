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

/** Protobuf type {@code RegisterIdsPb} */
public final class RegisterIdsPb extends com.google.protobuf.GeneratedMessageV3
    implements
    // @@protoc_insertion_point(message_implements:RegisterIdsPb)
    RegisterIdsPbOrBuilder {
  private static final long serialVersionUID = 0L;
  // Use RegisterIdsPb.newBuilder() to construct.
  private RegisterIdsPb(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }

  private RegisterIdsPb() {
    registerIds_ = com.google.protobuf.LazyStringArrayList.EMPTY;
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
    return this.unknownFields;
  }

  private RegisterIdsPb(
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
          case 10:
            {
              java.lang.String s = input.readStringRequireUtf8();
              if (!((mutable_bitField0_ & 0x00000001) == 0x00000001)) {
                registerIds_ = new com.google.protobuf.LazyStringArrayList();
                mutable_bitField0_ |= 0x00000001;
              }
              registerIds_.add(s);
              break;
            }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(e).setUnfinishedMessage(this);
    } finally {
      if (((mutable_bitField0_ & 0x00000001) == 0x00000001)) {
        registerIds_ = registerIds_.getUnmodifiableView();
      }
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }

  public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
    return com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPbOuterClass
        .internal_static_RegisterIdsPb_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPbOuterClass
        .internal_static_RegisterIdsPb_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb.class,
            com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb.Builder.class);
  }

  public static final int REGISTERIDS_FIELD_NUMBER = 1;
  private com.google.protobuf.LazyStringList registerIds_;
  /** <code>repeated string registerIds = 1;</code> */
  public com.google.protobuf.ProtocolStringList getRegisterIdsList() {
    return registerIds_;
  }
  /** <code>repeated string registerIds = 1;</code> */
  public int getRegisterIdsCount() {
    return registerIds_.size();
  }
  /** <code>repeated string registerIds = 1;</code> */
  public java.lang.String getRegisterIds(int index) {
    return registerIds_.get(index);
  }
  /** <code>repeated string registerIds = 1;</code> */
  public com.google.protobuf.ByteString getRegisterIdsBytes(int index) {
    return registerIds_.getByteString(index);
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
    for (int i = 0; i < registerIds_.size(); i++) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, registerIds_.getRaw(i));
    }
    unknownFields.writeTo(output);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    {
      int dataSize = 0;
      for (int i = 0; i < registerIds_.size(); i++) {
        dataSize += computeStringSizeNoTag(registerIds_.getRaw(i));
      }
      size += dataSize;
      size += 1 * getRegisterIdsList().size();
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
    if (!(obj instanceof com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb)) {
      return super.equals(obj);
    }
    com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb other =
        (com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb) obj;

    boolean result = true;
    result = result && getRegisterIdsList().equals(other.getRegisterIdsList());
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
    if (getRegisterIdsCount() > 0) {
      hash = (37 * hash) + REGISTERIDS_FIELD_NUMBER;
      hash = (53 * hash) + getRegisterIdsList().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb parseFrom(
      java.nio.ByteBuffer data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb parseFrom(
      java.nio.ByteBuffer data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb parseFrom(
      byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb parseFrom(
      java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb parseFrom(
      java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb parseDelimitedFrom(
      java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb parseDelimitedFrom(
      java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb parseFrom(
      com.google.protobuf.CodedInputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb parseFrom(
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
      com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb prototype) {
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
  /** Protobuf type {@code RegisterIdsPb} */
  public static final class Builder extends com.google.protobuf.GeneratedMessageV3.Builder<Builder>
      implements
      // @@protoc_insertion_point(builder_implements:RegisterIdsPb)
      com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPbOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
      return com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPbOuterClass
          .internal_static_RegisterIdsPb_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPbOuterClass
          .internal_static_RegisterIdsPb_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb.class,
              com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb.Builder.class);
    }

    // Construct using com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb.newBuilder()
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
      registerIds_ = com.google.protobuf.LazyStringArrayList.EMPTY;
      bitField0_ = (bitField0_ & ~0x00000001);
      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
      return com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPbOuterClass
          .internal_static_RegisterIdsPb_descriptor;
    }

    public com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb
        getDefaultInstanceForType() {
      return com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb.getDefaultInstance();
    }

    public com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb build() {
      com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb buildPartial() {
      com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb result =
          new com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb(this);
      int from_bitField0_ = bitField0_;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        registerIds_ = registerIds_.getUnmodifiableView();
        bitField0_ = (bitField0_ & ~0x00000001);
      }
      result.registerIds_ = registerIds_;
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
      if (other instanceof com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb) {
        return mergeFrom((com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb) other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb other) {
      if (other
          == com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb.getDefaultInstance())
        return this;
      if (!other.registerIds_.isEmpty()) {
        if (registerIds_.isEmpty()) {
          registerIds_ = other.registerIds_;
          bitField0_ = (bitField0_ & ~0x00000001);
        } else {
          ensureRegisterIdsIsMutable();
          registerIds_.addAll(other.registerIds_);
        }
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
      com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage =
            (com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb)
                e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private int bitField0_;

    private com.google.protobuf.LazyStringList registerIds_ =
        com.google.protobuf.LazyStringArrayList.EMPTY;

    private void ensureRegisterIdsIsMutable() {
      if (!((bitField0_ & 0x00000001) == 0x00000001)) {
        registerIds_ = new com.google.protobuf.LazyStringArrayList(registerIds_);
        bitField0_ |= 0x00000001;
      }
    }
    /** <code>repeated string registerIds = 1;</code> */
    public com.google.protobuf.ProtocolStringList getRegisterIdsList() {
      return registerIds_.getUnmodifiableView();
    }
    /** <code>repeated string registerIds = 1;</code> */
    public int getRegisterIdsCount() {
      return registerIds_.size();
    }
    /** <code>repeated string registerIds = 1;</code> */
    public java.lang.String getRegisterIds(int index) {
      return registerIds_.get(index);
    }
    /** <code>repeated string registerIds = 1;</code> */
    public com.google.protobuf.ByteString getRegisterIdsBytes(int index) {
      return registerIds_.getByteString(index);
    }
    /** <code>repeated string registerIds = 1;</code> */
    public Builder setRegisterIds(int index, java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }
      ensureRegisterIdsIsMutable();
      registerIds_.set(index, value);
      onChanged();
      return this;
    }
    /** <code>repeated string registerIds = 1;</code> */
    public Builder addRegisterIds(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }
      ensureRegisterIdsIsMutable();
      registerIds_.add(value);
      onChanged();
      return this;
    }
    /** <code>repeated string registerIds = 1;</code> */
    public Builder addAllRegisterIds(java.lang.Iterable<java.lang.String> values) {
      ensureRegisterIdsIsMutable();
      com.google.protobuf.AbstractMessageLite.Builder.addAll(values, registerIds_);
      onChanged();
      return this;
    }
    /** <code>repeated string registerIds = 1;</code> */
    public Builder clearRegisterIds() {
      registerIds_ = com.google.protobuf.LazyStringArrayList.EMPTY;
      bitField0_ = (bitField0_ & ~0x00000001);
      onChanged();
      return this;
    }
    /** <code>repeated string registerIds = 1;</code> */
    public Builder addRegisterIdsBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);
      ensureRegisterIdsIsMutable();
      registerIds_.add(value);
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

    // @@protoc_insertion_point(builder_scope:RegisterIdsPb)
  }

  // @@protoc_insertion_point(class_scope:RegisterIdsPb)
  private static final com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb
      DEFAULT_INSTANCE;

  static {
    DEFAULT_INSTANCE = new com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb();
  }

  public static com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<RegisterIdsPb> PARSER =
      new com.google.protobuf.AbstractParser<RegisterIdsPb>() {
        public RegisterIdsPb parsePartialFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
          return new RegisterIdsPb(input, extensionRegistry);
        }
      };

  public static com.google.protobuf.Parser<RegisterIdsPb> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<RegisterIdsPb> getParserForType() {
    return PARSER;
  }

  public com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }
}
