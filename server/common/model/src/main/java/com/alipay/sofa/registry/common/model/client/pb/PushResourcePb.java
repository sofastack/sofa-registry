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

/** Protobuf type {@code PushResourcePb} */
public final class PushResourcePb extends com.google.protobuf.GeneratedMessageV3
    implements
    // @@protoc_insertion_point(message_implements:PushResourcePb)
    PushResourcePbOrBuilder {
  private static final long serialVersionUID = 0L;
  // Use PushResourcePb.newBuilder() to construct.
  private PushResourcePb(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }

  private PushResourcePb() {
    registerId_ = "";
    version_ = 0L;
    timestamp_ = 0L;
    registerIdHash_ = 0;
    resources_ = java.util.Collections.emptyList();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
    return this.unknownFields;
  }

  private PushResourcePb(
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

              registerId_ = s;
              break;
            }
          case 16:
            {
              version_ = input.readInt64();
              break;
            }
          case 24:
            {
              timestamp_ = input.readInt64();
              break;
            }
          case 32:
            {
              registerIdHash_ = input.readInt32();
              break;
            }
          case 42:
            {
              if (!((mutable_bitField0_ & 0x00000010) == 0x00000010)) {
                resources_ =
                    new java.util.ArrayList<
                        com.alipay.sofa.registry.common.model.client.pb.DataBoxPb>();
                mutable_bitField0_ |= 0x00000010;
              }
              resources_.add(
                  input.readMessage(
                      com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.parser(),
                      extensionRegistry));
              break;
            }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(e).setUnfinishedMessage(this);
    } finally {
      if (((mutable_bitField0_ & 0x00000010) == 0x00000010)) {
        resources_ = java.util.Collections.unmodifiableList(resources_);
      }
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }

  public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
    return com.alipay.sofa.registry.common.model.client.pb.PushResourcePbOuterClass
        .internal_static_PushResourcePb_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.alipay.sofa.registry.common.model.client.pb.PushResourcePbOuterClass
        .internal_static_PushResourcePb_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.alipay.sofa.registry.common.model.client.pb.PushResourcePb.class,
            com.alipay.sofa.registry.common.model.client.pb.PushResourcePb.Builder.class);
  }

  private int bitField0_;
  public static final int REGISTERID_FIELD_NUMBER = 1;
  private volatile java.lang.Object registerId_;
  /** <code>string registerId = 1;</code> */
  public java.lang.String getRegisterId() {
    java.lang.Object ref = registerId_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      registerId_ = s;
      return s;
    }
  }
  /** <code>string registerId = 1;</code> */
  public com.google.protobuf.ByteString getRegisterIdBytes() {
    java.lang.Object ref = registerId_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b =
          com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
      registerId_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int VERSION_FIELD_NUMBER = 2;
  private long version_;
  /** <code>int64 version = 2;</code> */
  public long getVersion() {
    return version_;
  }

  public static final int TIMESTAMP_FIELD_NUMBER = 3;
  private long timestamp_;
  /** <code>int64 timestamp = 3;</code> */
  public long getTimestamp() {
    return timestamp_;
  }

  public static final int REGISTERIDHASH_FIELD_NUMBER = 4;
  private int registerIdHash_;
  /** <code>int32 registerIdHash = 4;</code> */
  public int getRegisterIdHash() {
    return registerIdHash_;
  }

  public static final int RESOURCES_FIELD_NUMBER = 5;
  private java.util.List<com.alipay.sofa.registry.common.model.client.pb.DataBoxPb> resources_;
  /** <code>repeated .DataBoxPb resources = 5;</code> */
  public java.util.List<com.alipay.sofa.registry.common.model.client.pb.DataBoxPb>
      getResourcesList() {
    return resources_;
  }
  /** <code>repeated .DataBoxPb resources = 5;</code> */
  public java.util.List<
          ? extends com.alipay.sofa.registry.common.model.client.pb.DataBoxPbOrBuilder>
      getResourcesOrBuilderList() {
    return resources_;
  }
  /** <code>repeated .DataBoxPb resources = 5;</code> */
  public int getResourcesCount() {
    return resources_.size();
  }
  /** <code>repeated .DataBoxPb resources = 5;</code> */
  public com.alipay.sofa.registry.common.model.client.pb.DataBoxPb getResources(int index) {
    return resources_.get(index);
  }
  /** <code>repeated .DataBoxPb resources = 5;</code> */
  public com.alipay.sofa.registry.common.model.client.pb.DataBoxPbOrBuilder getResourcesOrBuilder(
      int index) {
    return resources_.get(index);
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
    if (!getRegisterIdBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, registerId_);
    }
    if (version_ != 0L) {
      output.writeInt64(2, version_);
    }
    if (timestamp_ != 0L) {
      output.writeInt64(3, timestamp_);
    }
    if (registerIdHash_ != 0) {
      output.writeInt32(4, registerIdHash_);
    }
    for (int i = 0; i < resources_.size(); i++) {
      output.writeMessage(5, resources_.get(i));
    }
    unknownFields.writeTo(output);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!getRegisterIdBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, registerId_);
    }
    if (version_ != 0L) {
      size += com.google.protobuf.CodedOutputStream.computeInt64Size(2, version_);
    }
    if (timestamp_ != 0L) {
      size += com.google.protobuf.CodedOutputStream.computeInt64Size(3, timestamp_);
    }
    if (registerIdHash_ != 0) {
      size += com.google.protobuf.CodedOutputStream.computeInt32Size(4, registerIdHash_);
    }
    for (int i = 0; i < resources_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream.computeMessageSize(5, resources_.get(i));
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
    if (!(obj instanceof com.alipay.sofa.registry.common.model.client.pb.PushResourcePb)) {
      return super.equals(obj);
    }
    com.alipay.sofa.registry.common.model.client.pb.PushResourcePb other =
        (com.alipay.sofa.registry.common.model.client.pb.PushResourcePb) obj;

    boolean result = true;
    result = result && getRegisterId().equals(other.getRegisterId());
    result = result && (getVersion() == other.getVersion());
    result = result && (getTimestamp() == other.getTimestamp());
    result = result && (getRegisterIdHash() == other.getRegisterIdHash());
    result = result && getResourcesList().equals(other.getResourcesList());
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
    hash = (37 * hash) + REGISTERID_FIELD_NUMBER;
    hash = (53 * hash) + getRegisterId().hashCode();
    hash = (37 * hash) + VERSION_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(getVersion());
    hash = (37 * hash) + TIMESTAMP_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(getTimestamp());
    hash = (37 * hash) + REGISTERIDHASH_FIELD_NUMBER;
    hash = (53 * hash) + getRegisterIdHash();
    if (getResourcesCount() > 0) {
      hash = (37 * hash) + RESOURCES_FIELD_NUMBER;
      hash = (53 * hash) + getResourcesList().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.alipay.sofa.registry.common.model.client.pb.PushResourcePb parseFrom(
      java.nio.ByteBuffer data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.PushResourcePb parseFrom(
      java.nio.ByteBuffer data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.PushResourcePb parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.PushResourcePb parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.PushResourcePb parseFrom(
      byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.PushResourcePb parseFrom(
      byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.PushResourcePb parseFrom(
      java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.PushResourcePb parseFrom(
      java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.PushResourcePb parseDelimitedFrom(
      java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.PushResourcePb parseDelimitedFrom(
      java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.PushResourcePb parseFrom(
      com.google.protobuf.CodedInputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.PushResourcePb parseFrom(
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
      com.alipay.sofa.registry.common.model.client.pb.PushResourcePb prototype) {
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
  /** Protobuf type {@code PushResourcePb} */
  public static final class Builder extends com.google.protobuf.GeneratedMessageV3.Builder<Builder>
      implements
      // @@protoc_insertion_point(builder_implements:PushResourcePb)
      com.alipay.sofa.registry.common.model.client.pb.PushResourcePbOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
      return com.alipay.sofa.registry.common.model.client.pb.PushResourcePbOuterClass
          .internal_static_PushResourcePb_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.alipay.sofa.registry.common.model.client.pb.PushResourcePbOuterClass
          .internal_static_PushResourcePb_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.alipay.sofa.registry.common.model.client.pb.PushResourcePb.class,
              com.alipay.sofa.registry.common.model.client.pb.PushResourcePb.Builder.class);
    }

    // Construct using com.alipay.sofa.registry.common.model.client.pb.PushResourcePb.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }

    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders) {
        getResourcesFieldBuilder();
      }
    }

    public Builder clear() {
      super.clear();
      registerId_ = "";

      version_ = 0L;

      timestamp_ = 0L;

      registerIdHash_ = 0;

      if (resourcesBuilder_ == null) {
        resources_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000010);
      } else {
        resourcesBuilder_.clear();
      }
      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
      return com.alipay.sofa.registry.common.model.client.pb.PushResourcePbOuterClass
          .internal_static_PushResourcePb_descriptor;
    }

    public com.alipay.sofa.registry.common.model.client.pb.PushResourcePb
        getDefaultInstanceForType() {
      return com.alipay.sofa.registry.common.model.client.pb.PushResourcePb.getDefaultInstance();
    }

    public com.alipay.sofa.registry.common.model.client.pb.PushResourcePb build() {
      com.alipay.sofa.registry.common.model.client.pb.PushResourcePb result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public com.alipay.sofa.registry.common.model.client.pb.PushResourcePb buildPartial() {
      com.alipay.sofa.registry.common.model.client.pb.PushResourcePb result =
          new com.alipay.sofa.registry.common.model.client.pb.PushResourcePb(this);
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      result.registerId_ = registerId_;
      result.version_ = version_;
      result.timestamp_ = timestamp_;
      result.registerIdHash_ = registerIdHash_;
      if (resourcesBuilder_ == null) {
        if (((bitField0_ & 0x00000010) == 0x00000010)) {
          resources_ = java.util.Collections.unmodifiableList(resources_);
          bitField0_ = (bitField0_ & ~0x00000010);
        }
        result.resources_ = resources_;
      } else {
        result.resources_ = resourcesBuilder_.build();
      }
      result.bitField0_ = to_bitField0_;
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
      if (other instanceof com.alipay.sofa.registry.common.model.client.pb.PushResourcePb) {
        return mergeFrom((com.alipay.sofa.registry.common.model.client.pb.PushResourcePb) other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.alipay.sofa.registry.common.model.client.pb.PushResourcePb other) {
      if (other
          == com.alipay.sofa.registry.common.model.client.pb.PushResourcePb.getDefaultInstance())
        return this;
      if (!other.getRegisterId().isEmpty()) {
        registerId_ = other.registerId_;
        onChanged();
      }
      if (other.getVersion() != 0L) {
        setVersion(other.getVersion());
      }
      if (other.getTimestamp() != 0L) {
        setTimestamp(other.getTimestamp());
      }
      if (other.getRegisterIdHash() != 0) {
        setRegisterIdHash(other.getRegisterIdHash());
      }
      if (resourcesBuilder_ == null) {
        if (!other.resources_.isEmpty()) {
          if (resources_.isEmpty()) {
            resources_ = other.resources_;
            bitField0_ = (bitField0_ & ~0x00000010);
          } else {
            ensureResourcesIsMutable();
            resources_.addAll(other.resources_);
          }
          onChanged();
        }
      } else {
        if (!other.resources_.isEmpty()) {
          if (resourcesBuilder_.isEmpty()) {
            resourcesBuilder_.dispose();
            resourcesBuilder_ = null;
            resources_ = other.resources_;
            bitField0_ = (bitField0_ & ~0x00000010);
            resourcesBuilder_ =
                com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders
                    ? getResourcesFieldBuilder()
                    : null;
          } else {
            resourcesBuilder_.addAllMessages(other.resources_);
          }
        }
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
      com.alipay.sofa.registry.common.model.client.pb.PushResourcePb parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage =
            (com.alipay.sofa.registry.common.model.client.pb.PushResourcePb)
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

    private java.lang.Object registerId_ = "";
    /** <code>string registerId = 1;</code> */
    public java.lang.String getRegisterId() {
      java.lang.Object ref = registerId_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        registerId_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /** <code>string registerId = 1;</code> */
    public com.google.protobuf.ByteString getRegisterIdBytes() {
      java.lang.Object ref = registerId_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
            com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
        registerId_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /** <code>string registerId = 1;</code> */
    public Builder setRegisterId(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      registerId_ = value;
      onChanged();
      return this;
    }
    /** <code>string registerId = 1;</code> */
    public Builder clearRegisterId() {

      registerId_ = getDefaultInstance().getRegisterId();
      onChanged();
      return this;
    }
    /** <code>string registerId = 1;</code> */
    public Builder setRegisterIdBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);

      registerId_ = value;
      onChanged();
      return this;
    }

    private long version_;
    /** <code>int64 version = 2;</code> */
    public long getVersion() {
      return version_;
    }
    /** <code>int64 version = 2;</code> */
    public Builder setVersion(long value) {

      version_ = value;
      onChanged();
      return this;
    }
    /** <code>int64 version = 2;</code> */
    public Builder clearVersion() {

      version_ = 0L;
      onChanged();
      return this;
    }

    private long timestamp_;
    /** <code>int64 timestamp = 3;</code> */
    public long getTimestamp() {
      return timestamp_;
    }
    /** <code>int64 timestamp = 3;</code> */
    public Builder setTimestamp(long value) {

      timestamp_ = value;
      onChanged();
      return this;
    }
    /** <code>int64 timestamp = 3;</code> */
    public Builder clearTimestamp() {

      timestamp_ = 0L;
      onChanged();
      return this;
    }

    private int registerIdHash_;
    /** <code>int32 registerIdHash = 4;</code> */
    public int getRegisterIdHash() {
      return registerIdHash_;
    }
    /** <code>int32 registerIdHash = 4;</code> */
    public Builder setRegisterIdHash(int value) {

      registerIdHash_ = value;
      onChanged();
      return this;
    }
    /** <code>int32 registerIdHash = 4;</code> */
    public Builder clearRegisterIdHash() {

      registerIdHash_ = 0;
      onChanged();
      return this;
    }

    private java.util.List<com.alipay.sofa.registry.common.model.client.pb.DataBoxPb> resources_ =
        java.util.Collections.emptyList();

    private void ensureResourcesIsMutable() {
      if (!((bitField0_ & 0x00000010) == 0x00000010)) {
        resources_ =
            new java.util.ArrayList<com.alipay.sofa.registry.common.model.client.pb.DataBoxPb>(
                resources_);
        bitField0_ |= 0x00000010;
      }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
            com.alipay.sofa.registry.common.model.client.pb.DataBoxPb,
            com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.Builder,
            com.alipay.sofa.registry.common.model.client.pb.DataBoxPbOrBuilder>
        resourcesBuilder_;

    /** <code>repeated .DataBoxPb resources = 5;</code> */
    public java.util.List<com.alipay.sofa.registry.common.model.client.pb.DataBoxPb>
        getResourcesList() {
      if (resourcesBuilder_ == null) {
        return java.util.Collections.unmodifiableList(resources_);
      } else {
        return resourcesBuilder_.getMessageList();
      }
    }
    /** <code>repeated .DataBoxPb resources = 5;</code> */
    public int getResourcesCount() {
      if (resourcesBuilder_ == null) {
        return resources_.size();
      } else {
        return resourcesBuilder_.getCount();
      }
    }
    /** <code>repeated .DataBoxPb resources = 5;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.DataBoxPb getResources(int index) {
      if (resourcesBuilder_ == null) {
        return resources_.get(index);
      } else {
        return resourcesBuilder_.getMessage(index);
      }
    }
    /** <code>repeated .DataBoxPb resources = 5;</code> */
    public Builder setResources(
        int index, com.alipay.sofa.registry.common.model.client.pb.DataBoxPb value) {
      if (resourcesBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureResourcesIsMutable();
        resources_.set(index, value);
        onChanged();
      } else {
        resourcesBuilder_.setMessage(index, value);
      }
      return this;
    }
    /** <code>repeated .DataBoxPb resources = 5;</code> */
    public Builder setResources(
        int index,
        com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.Builder builderForValue) {
      if (resourcesBuilder_ == null) {
        ensureResourcesIsMutable();
        resources_.set(index, builderForValue.build());
        onChanged();
      } else {
        resourcesBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /** <code>repeated .DataBoxPb resources = 5;</code> */
    public Builder addResources(com.alipay.sofa.registry.common.model.client.pb.DataBoxPb value) {
      if (resourcesBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureResourcesIsMutable();
        resources_.add(value);
        onChanged();
      } else {
        resourcesBuilder_.addMessage(value);
      }
      return this;
    }
    /** <code>repeated .DataBoxPb resources = 5;</code> */
    public Builder addResources(
        int index, com.alipay.sofa.registry.common.model.client.pb.DataBoxPb value) {
      if (resourcesBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureResourcesIsMutable();
        resources_.add(index, value);
        onChanged();
      } else {
        resourcesBuilder_.addMessage(index, value);
      }
      return this;
    }
    /** <code>repeated .DataBoxPb resources = 5;</code> */
    public Builder addResources(
        com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.Builder builderForValue) {
      if (resourcesBuilder_ == null) {
        ensureResourcesIsMutable();
        resources_.add(builderForValue.build());
        onChanged();
      } else {
        resourcesBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /** <code>repeated .DataBoxPb resources = 5;</code> */
    public Builder addResources(
        int index,
        com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.Builder builderForValue) {
      if (resourcesBuilder_ == null) {
        ensureResourcesIsMutable();
        resources_.add(index, builderForValue.build());
        onChanged();
      } else {
        resourcesBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /** <code>repeated .DataBoxPb resources = 5;</code> */
    public Builder addAllResources(
        java.lang.Iterable<? extends com.alipay.sofa.registry.common.model.client.pb.DataBoxPb>
            values) {
      if (resourcesBuilder_ == null) {
        ensureResourcesIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(values, resources_);
        onChanged();
      } else {
        resourcesBuilder_.addAllMessages(values);
      }
      return this;
    }
    /** <code>repeated .DataBoxPb resources = 5;</code> */
    public Builder clearResources() {
      if (resourcesBuilder_ == null) {
        resources_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000010);
        onChanged();
      } else {
        resourcesBuilder_.clear();
      }
      return this;
    }
    /** <code>repeated .DataBoxPb resources = 5;</code> */
    public Builder removeResources(int index) {
      if (resourcesBuilder_ == null) {
        ensureResourcesIsMutable();
        resources_.remove(index);
        onChanged();
      } else {
        resourcesBuilder_.remove(index);
      }
      return this;
    }
    /** <code>repeated .DataBoxPb resources = 5;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.Builder getResourcesBuilder(
        int index) {
      return getResourcesFieldBuilder().getBuilder(index);
    }
    /** <code>repeated .DataBoxPb resources = 5;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.DataBoxPbOrBuilder getResourcesOrBuilder(
        int index) {
      if (resourcesBuilder_ == null) {
        return resources_.get(index);
      } else {
        return resourcesBuilder_.getMessageOrBuilder(index);
      }
    }
    /** <code>repeated .DataBoxPb resources = 5;</code> */
    public java.util.List<
            ? extends com.alipay.sofa.registry.common.model.client.pb.DataBoxPbOrBuilder>
        getResourcesOrBuilderList() {
      if (resourcesBuilder_ != null) {
        return resourcesBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(resources_);
      }
    }
    /** <code>repeated .DataBoxPb resources = 5;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.Builder addResourcesBuilder() {
      return getResourcesFieldBuilder()
          .addBuilder(
              com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.getDefaultInstance());
    }
    /** <code>repeated .DataBoxPb resources = 5;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.Builder addResourcesBuilder(
        int index) {
      return getResourcesFieldBuilder()
          .addBuilder(
              index,
              com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.getDefaultInstance());
    }
    /** <code>repeated .DataBoxPb resources = 5;</code> */
    public java.util.List<com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.Builder>
        getResourcesBuilderList() {
      return getResourcesFieldBuilder().getBuilderList();
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
            com.alipay.sofa.registry.common.model.client.pb.DataBoxPb,
            com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.Builder,
            com.alipay.sofa.registry.common.model.client.pb.DataBoxPbOrBuilder>
        getResourcesFieldBuilder() {
      if (resourcesBuilder_ == null) {
        resourcesBuilder_ =
            new com.google.protobuf.RepeatedFieldBuilderV3<
                com.alipay.sofa.registry.common.model.client.pb.DataBoxPb,
                com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.Builder,
                com.alipay.sofa.registry.common.model.client.pb.DataBoxPbOrBuilder>(
                resources_,
                ((bitField0_ & 0x00000010) == 0x00000010),
                getParentForChildren(),
                isClean());
        resources_ = null;
      }
      return resourcesBuilder_;
    }

    public final Builder setUnknownFields(final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFieldsProto3(unknownFields);
    }

    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }

    // @@protoc_insertion_point(builder_scope:PushResourcePb)
  }

  // @@protoc_insertion_point(class_scope:PushResourcePb)
  private static final com.alipay.sofa.registry.common.model.client.pb.PushResourcePb
      DEFAULT_INSTANCE;

  static {
    DEFAULT_INSTANCE = new com.alipay.sofa.registry.common.model.client.pb.PushResourcePb();
  }

  public static com.alipay.sofa.registry.common.model.client.pb.PushResourcePb
      getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<PushResourcePb> PARSER =
      new com.google.protobuf.AbstractParser<PushResourcePb>() {
        public PushResourcePb parsePartialFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
          return new PushResourcePb(input, extensionRegistry);
        }
      };

  public static com.google.protobuf.Parser<PushResourcePb> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<PushResourcePb> getParserForType() {
    return PARSER;
  }

  public com.alipay.sofa.registry.common.model.client.pb.PushResourcePb
      getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }
}
