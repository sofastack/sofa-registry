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

/** Protobuf type {@code ReceivedConfigDataPb} */
public final class ReceivedConfigDataPb extends com.google.protobuf.GeneratedMessageV3
    implements
    // @@protoc_insertion_point(message_implements:ReceivedConfigDataPb)
    ReceivedConfigDataPbOrBuilder {
  private static final long serialVersionUID = 0L;
  // Use ReceivedConfigDataPb.newBuilder() to construct.
  private ReceivedConfigDataPb(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }

  private ReceivedConfigDataPb() {
    dataId_ = "";
    group_ = "";
    instanceId_ = "";
    configuratorRegistIds_ = com.google.protobuf.LazyStringArrayList.EMPTY;
    version_ = 0L;
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
    return this.unknownFields;
  }

  private ReceivedConfigDataPb(
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

              dataId_ = s;
              break;
            }
          case 18:
            {
              java.lang.String s = input.readStringRequireUtf8();

              group_ = s;
              break;
            }
          case 26:
            {
              java.lang.String s = input.readStringRequireUtf8();

              instanceId_ = s;
              break;
            }
          case 34:
            {
              java.lang.String s = input.readStringRequireUtf8();
              if (!((mutable_bitField0_ & 0x00000008) == 0x00000008)) {
                configuratorRegistIds_ = new com.google.protobuf.LazyStringArrayList();
                mutable_bitField0_ |= 0x00000008;
              }
              configuratorRegistIds_.add(s);
              break;
            }
          case 42:
            {
              com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.Builder subBuilder = null;
              if (dataBox_ != null) {
                subBuilder = dataBox_.toBuilder();
              }
              dataBox_ =
                  input.readMessage(
                      com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.parser(),
                      extensionRegistry);
              if (subBuilder != null) {
                subBuilder.mergeFrom(dataBox_);
                dataBox_ = subBuilder.buildPartial();
              }

              break;
            }
          case 48:
            {
              version_ = input.readInt64();
              break;
            }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(e).setUnfinishedMessage(this);
    } finally {
      if (((mutable_bitField0_ & 0x00000008) == 0x00000008)) {
        configuratorRegistIds_ = configuratorRegistIds_.getUnmodifiableView();
      }
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }

  public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
    return com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPbOuterClass
        .internal_static_ReceivedConfigDataPb_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPbOuterClass
        .internal_static_ReceivedConfigDataPb_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb.class,
            com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb.Builder.class);
  }

  private int bitField0_;
  public static final int DATAID_FIELD_NUMBER = 1;
  private volatile java.lang.Object dataId_;
  /** <code>string dataId = 1;</code> */
  public java.lang.String getDataId() {
    java.lang.Object ref = dataId_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      dataId_ = s;
      return s;
    }
  }
  /** <code>string dataId = 1;</code> */
  public com.google.protobuf.ByteString getDataIdBytes() {
    java.lang.Object ref = dataId_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b =
          com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
      dataId_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int GROUP_FIELD_NUMBER = 2;
  private volatile java.lang.Object group_;
  /** <code>string group = 2;</code> */
  public java.lang.String getGroup() {
    java.lang.Object ref = group_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      group_ = s;
      return s;
    }
  }
  /** <code>string group = 2;</code> */
  public com.google.protobuf.ByteString getGroupBytes() {
    java.lang.Object ref = group_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b =
          com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
      group_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int INSTANCEID_FIELD_NUMBER = 3;
  private volatile java.lang.Object instanceId_;
  /** <code>string instanceId = 3;</code> */
  public java.lang.String getInstanceId() {
    java.lang.Object ref = instanceId_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      instanceId_ = s;
      return s;
    }
  }
  /** <code>string instanceId = 3;</code> */
  public com.google.protobuf.ByteString getInstanceIdBytes() {
    java.lang.Object ref = instanceId_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b =
          com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
      instanceId_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int CONFIGURATORREGISTIDS_FIELD_NUMBER = 4;
  private com.google.protobuf.LazyStringList configuratorRegistIds_;
  /** <code>repeated string configuratorRegistIds = 4;</code> */
  public com.google.protobuf.ProtocolStringList getConfiguratorRegistIdsList() {
    return configuratorRegistIds_;
  }
  /** <code>repeated string configuratorRegistIds = 4;</code> */
  public int getConfiguratorRegistIdsCount() {
    return configuratorRegistIds_.size();
  }
  /** <code>repeated string configuratorRegistIds = 4;</code> */
  public java.lang.String getConfiguratorRegistIds(int index) {
    return configuratorRegistIds_.get(index);
  }
  /** <code>repeated string configuratorRegistIds = 4;</code> */
  public com.google.protobuf.ByteString getConfiguratorRegistIdsBytes(int index) {
    return configuratorRegistIds_.getByteString(index);
  }

  public static final int DATABOX_FIELD_NUMBER = 5;
  private com.alipay.sofa.registry.common.model.client.pb.DataBoxPb dataBox_;
  /** <code>.DataBoxPb dataBox = 5;</code> */
  public boolean hasDataBox() {
    return dataBox_ != null;
  }
  /** <code>.DataBoxPb dataBox = 5;</code> */
  public com.alipay.sofa.registry.common.model.client.pb.DataBoxPb getDataBox() {
    return dataBox_ == null
        ? com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.getDefaultInstance()
        : dataBox_;
  }
  /** <code>.DataBoxPb dataBox = 5;</code> */
  public com.alipay.sofa.registry.common.model.client.pb.DataBoxPbOrBuilder getDataBoxOrBuilder() {
    return getDataBox();
  }

  public static final int VERSION_FIELD_NUMBER = 6;
  private long version_;
  /** <code>int64 version = 6;</code> */
  public long getVersion() {
    return version_;
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
    if (!getDataIdBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, dataId_);
    }
    if (!getGroupBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 2, group_);
    }
    if (!getInstanceIdBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 3, instanceId_);
    }
    for (int i = 0; i < configuratorRegistIds_.size(); i++) {
      com.google.protobuf.GeneratedMessageV3.writeString(
          output, 4, configuratorRegistIds_.getRaw(i));
    }
    if (dataBox_ != null) {
      output.writeMessage(5, getDataBox());
    }
    if (version_ != 0L) {
      output.writeInt64(6, version_);
    }
    unknownFields.writeTo(output);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!getDataIdBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, dataId_);
    }
    if (!getGroupBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, group_);
    }
    if (!getInstanceIdBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(3, instanceId_);
    }
    {
      int dataSize = 0;
      for (int i = 0; i < configuratorRegistIds_.size(); i++) {
        dataSize += computeStringSizeNoTag(configuratorRegistIds_.getRaw(i));
      }
      size += dataSize;
      size += 1 * getConfiguratorRegistIdsList().size();
    }
    if (dataBox_ != null) {
      size += com.google.protobuf.CodedOutputStream.computeMessageSize(5, getDataBox());
    }
    if (version_ != 0L) {
      size += com.google.protobuf.CodedOutputStream.computeInt64Size(6, version_);
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
    if (!(obj instanceof com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb)) {
      return super.equals(obj);
    }
    com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb other =
        (com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb) obj;

    boolean result = true;
    result = result && getDataId().equals(other.getDataId());
    result = result && getGroup().equals(other.getGroup());
    result = result && getInstanceId().equals(other.getInstanceId());
    result = result && getConfiguratorRegistIdsList().equals(other.getConfiguratorRegistIdsList());
    result = result && (hasDataBox() == other.hasDataBox());
    if (hasDataBox()) {
      result = result && getDataBox().equals(other.getDataBox());
    }
    result = result && (getVersion() == other.getVersion());
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
    hash = (37 * hash) + DATAID_FIELD_NUMBER;
    hash = (53 * hash) + getDataId().hashCode();
    hash = (37 * hash) + GROUP_FIELD_NUMBER;
    hash = (53 * hash) + getGroup().hashCode();
    hash = (37 * hash) + INSTANCEID_FIELD_NUMBER;
    hash = (53 * hash) + getInstanceId().hashCode();
    if (getConfiguratorRegistIdsCount() > 0) {
      hash = (37 * hash) + CONFIGURATORREGISTIDS_FIELD_NUMBER;
      hash = (53 * hash) + getConfiguratorRegistIdsList().hashCode();
    }
    if (hasDataBox()) {
      hash = (37 * hash) + DATABOX_FIELD_NUMBER;
      hash = (53 * hash) + getDataBox().hashCode();
    }
    hash = (37 * hash) + VERSION_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(getVersion());
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb parseFrom(
      java.nio.ByteBuffer data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb parseFrom(
      java.nio.ByteBuffer data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb parseFrom(
      byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb parseFrom(
      byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb parseFrom(
      java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb parseFrom(
      java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb
      parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb
      parseDelimitedFrom(
          java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb parseFrom(
      com.google.protobuf.CodedInputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb parseFrom(
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
      com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb prototype) {
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
  /** Protobuf type {@code ReceivedConfigDataPb} */
  public static final class Builder extends com.google.protobuf.GeneratedMessageV3.Builder<Builder>
      implements
      // @@protoc_insertion_point(builder_implements:ReceivedConfigDataPb)
      com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPbOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
      return com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPbOuterClass
          .internal_static_ReceivedConfigDataPb_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPbOuterClass
          .internal_static_ReceivedConfigDataPb_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb.class,
              com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb.Builder.class);
    }

    // Construct using
    // com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb.newBuilder()
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
      dataId_ = "";

      group_ = "";

      instanceId_ = "";

      configuratorRegistIds_ = com.google.protobuf.LazyStringArrayList.EMPTY;
      bitField0_ = (bitField0_ & ~0x00000008);
      if (dataBoxBuilder_ == null) {
        dataBox_ = null;
      } else {
        dataBox_ = null;
        dataBoxBuilder_ = null;
      }
      version_ = 0L;

      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
      return com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPbOuterClass
          .internal_static_ReceivedConfigDataPb_descriptor;
    }

    public com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb
        getDefaultInstanceForType() {
      return com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb
          .getDefaultInstance();
    }

    public com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb build() {
      com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb buildPartial() {
      com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb result =
          new com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb(this);
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      result.dataId_ = dataId_;
      result.group_ = group_;
      result.instanceId_ = instanceId_;
      if (((bitField0_ & 0x00000008) == 0x00000008)) {
        configuratorRegistIds_ = configuratorRegistIds_.getUnmodifiableView();
        bitField0_ = (bitField0_ & ~0x00000008);
      }
      result.configuratorRegistIds_ = configuratorRegistIds_;
      if (dataBoxBuilder_ == null) {
        result.dataBox_ = dataBox_;
      } else {
        result.dataBox_ = dataBoxBuilder_.build();
      }
      result.version_ = version_;
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
      if (other instanceof com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb) {
        return mergeFrom(
            (com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb) other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(
        com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb other) {
      if (other
          == com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb
              .getDefaultInstance()) return this;
      if (!other.getDataId().isEmpty()) {
        dataId_ = other.dataId_;
        onChanged();
      }
      if (!other.getGroup().isEmpty()) {
        group_ = other.group_;
        onChanged();
      }
      if (!other.getInstanceId().isEmpty()) {
        instanceId_ = other.instanceId_;
        onChanged();
      }
      if (!other.configuratorRegistIds_.isEmpty()) {
        if (configuratorRegistIds_.isEmpty()) {
          configuratorRegistIds_ = other.configuratorRegistIds_;
          bitField0_ = (bitField0_ & ~0x00000008);
        } else {
          ensureConfiguratorRegistIdsIsMutable();
          configuratorRegistIds_.addAll(other.configuratorRegistIds_);
        }
        onChanged();
      }
      if (other.hasDataBox()) {
        mergeDataBox(other.getDataBox());
      }
      if (other.getVersion() != 0L) {
        setVersion(other.getVersion());
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
      com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage =
            (com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb)
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

    private java.lang.Object dataId_ = "";
    /** <code>string dataId = 1;</code> */
    public java.lang.String getDataId() {
      java.lang.Object ref = dataId_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        dataId_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /** <code>string dataId = 1;</code> */
    public com.google.protobuf.ByteString getDataIdBytes() {
      java.lang.Object ref = dataId_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
            com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
        dataId_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /** <code>string dataId = 1;</code> */
    public Builder setDataId(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      dataId_ = value;
      onChanged();
      return this;
    }
    /** <code>string dataId = 1;</code> */
    public Builder clearDataId() {

      dataId_ = getDefaultInstance().getDataId();
      onChanged();
      return this;
    }
    /** <code>string dataId = 1;</code> */
    public Builder setDataIdBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);

      dataId_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object group_ = "";
    /** <code>string group = 2;</code> */
    public java.lang.String getGroup() {
      java.lang.Object ref = group_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        group_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /** <code>string group = 2;</code> */
    public com.google.protobuf.ByteString getGroupBytes() {
      java.lang.Object ref = group_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
            com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
        group_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /** <code>string group = 2;</code> */
    public Builder setGroup(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      group_ = value;
      onChanged();
      return this;
    }
    /** <code>string group = 2;</code> */
    public Builder clearGroup() {

      group_ = getDefaultInstance().getGroup();
      onChanged();
      return this;
    }
    /** <code>string group = 2;</code> */
    public Builder setGroupBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);

      group_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object instanceId_ = "";
    /** <code>string instanceId = 3;</code> */
    public java.lang.String getInstanceId() {
      java.lang.Object ref = instanceId_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        instanceId_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /** <code>string instanceId = 3;</code> */
    public com.google.protobuf.ByteString getInstanceIdBytes() {
      java.lang.Object ref = instanceId_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
            com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
        instanceId_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /** <code>string instanceId = 3;</code> */
    public Builder setInstanceId(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      instanceId_ = value;
      onChanged();
      return this;
    }
    /** <code>string instanceId = 3;</code> */
    public Builder clearInstanceId() {

      instanceId_ = getDefaultInstance().getInstanceId();
      onChanged();
      return this;
    }
    /** <code>string instanceId = 3;</code> */
    public Builder setInstanceIdBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);

      instanceId_ = value;
      onChanged();
      return this;
    }

    private com.google.protobuf.LazyStringList configuratorRegistIds_ =
        com.google.protobuf.LazyStringArrayList.EMPTY;

    private void ensureConfiguratorRegistIdsIsMutable() {
      if (!((bitField0_ & 0x00000008) == 0x00000008)) {
        configuratorRegistIds_ =
            new com.google.protobuf.LazyStringArrayList(configuratorRegistIds_);
        bitField0_ |= 0x00000008;
      }
    }
    /** <code>repeated string configuratorRegistIds = 4;</code> */
    public com.google.protobuf.ProtocolStringList getConfiguratorRegistIdsList() {
      return configuratorRegistIds_.getUnmodifiableView();
    }
    /** <code>repeated string configuratorRegistIds = 4;</code> */
    public int getConfiguratorRegistIdsCount() {
      return configuratorRegistIds_.size();
    }
    /** <code>repeated string configuratorRegistIds = 4;</code> */
    public java.lang.String getConfiguratorRegistIds(int index) {
      return configuratorRegistIds_.get(index);
    }
    /** <code>repeated string configuratorRegistIds = 4;</code> */
    public com.google.protobuf.ByteString getConfiguratorRegistIdsBytes(int index) {
      return configuratorRegistIds_.getByteString(index);
    }
    /** <code>repeated string configuratorRegistIds = 4;</code> */
    public Builder setConfiguratorRegistIds(int index, java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }
      ensureConfiguratorRegistIdsIsMutable();
      configuratorRegistIds_.set(index, value);
      onChanged();
      return this;
    }
    /** <code>repeated string configuratorRegistIds = 4;</code> */
    public Builder addConfiguratorRegistIds(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }
      ensureConfiguratorRegistIdsIsMutable();
      configuratorRegistIds_.add(value);
      onChanged();
      return this;
    }
    /** <code>repeated string configuratorRegistIds = 4;</code> */
    public Builder addAllConfiguratorRegistIds(java.lang.Iterable<java.lang.String> values) {
      ensureConfiguratorRegistIdsIsMutable();
      com.google.protobuf.AbstractMessageLite.Builder.addAll(values, configuratorRegistIds_);
      onChanged();
      return this;
    }
    /** <code>repeated string configuratorRegistIds = 4;</code> */
    public Builder clearConfiguratorRegistIds() {
      configuratorRegistIds_ = com.google.protobuf.LazyStringArrayList.EMPTY;
      bitField0_ = (bitField0_ & ~0x00000008);
      onChanged();
      return this;
    }
    /** <code>repeated string configuratorRegistIds = 4;</code> */
    public Builder addConfiguratorRegistIdsBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);
      ensureConfiguratorRegistIdsIsMutable();
      configuratorRegistIds_.add(value);
      onChanged();
      return this;
    }

    private com.alipay.sofa.registry.common.model.client.pb.DataBoxPb dataBox_ = null;
    private com.google.protobuf.SingleFieldBuilderV3<
            com.alipay.sofa.registry.common.model.client.pb.DataBoxPb,
            com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.Builder,
            com.alipay.sofa.registry.common.model.client.pb.DataBoxPbOrBuilder>
        dataBoxBuilder_;
    /** <code>.DataBoxPb dataBox = 5;</code> */
    public boolean hasDataBox() {
      return dataBoxBuilder_ != null || dataBox_ != null;
    }
    /** <code>.DataBoxPb dataBox = 5;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.DataBoxPb getDataBox() {
      if (dataBoxBuilder_ == null) {
        return dataBox_ == null
            ? com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.getDefaultInstance()
            : dataBox_;
      } else {
        return dataBoxBuilder_.getMessage();
      }
    }
    /** <code>.DataBoxPb dataBox = 5;</code> */
    public Builder setDataBox(com.alipay.sofa.registry.common.model.client.pb.DataBoxPb value) {
      if (dataBoxBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        dataBox_ = value;
        onChanged();
      } else {
        dataBoxBuilder_.setMessage(value);
      }

      return this;
    }
    /** <code>.DataBoxPb dataBox = 5;</code> */
    public Builder setDataBox(
        com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.Builder builderForValue) {
      if (dataBoxBuilder_ == null) {
        dataBox_ = builderForValue.build();
        onChanged();
      } else {
        dataBoxBuilder_.setMessage(builderForValue.build());
      }

      return this;
    }
    /** <code>.DataBoxPb dataBox = 5;</code> */
    public Builder mergeDataBox(com.alipay.sofa.registry.common.model.client.pb.DataBoxPb value) {
      if (dataBoxBuilder_ == null) {
        if (dataBox_ != null) {
          dataBox_ =
              com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.newBuilder(dataBox_)
                  .mergeFrom(value)
                  .buildPartial();
        } else {
          dataBox_ = value;
        }
        onChanged();
      } else {
        dataBoxBuilder_.mergeFrom(value);
      }

      return this;
    }
    /** <code>.DataBoxPb dataBox = 5;</code> */
    public Builder clearDataBox() {
      if (dataBoxBuilder_ == null) {
        dataBox_ = null;
        onChanged();
      } else {
        dataBox_ = null;
        dataBoxBuilder_ = null;
      }

      return this;
    }
    /** <code>.DataBoxPb dataBox = 5;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.Builder getDataBoxBuilder() {

      onChanged();
      return getDataBoxFieldBuilder().getBuilder();
    }
    /** <code>.DataBoxPb dataBox = 5;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.DataBoxPbOrBuilder
        getDataBoxOrBuilder() {
      if (dataBoxBuilder_ != null) {
        return dataBoxBuilder_.getMessageOrBuilder();
      } else {
        return dataBox_ == null
            ? com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.getDefaultInstance()
            : dataBox_;
      }
    }
    /** <code>.DataBoxPb dataBox = 5;</code> */
    private com.google.protobuf.SingleFieldBuilderV3<
            com.alipay.sofa.registry.common.model.client.pb.DataBoxPb,
            com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.Builder,
            com.alipay.sofa.registry.common.model.client.pb.DataBoxPbOrBuilder>
        getDataBoxFieldBuilder() {
      if (dataBoxBuilder_ == null) {
        dataBoxBuilder_ =
            new com.google.protobuf.SingleFieldBuilderV3<
                com.alipay.sofa.registry.common.model.client.pb.DataBoxPb,
                com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.Builder,
                com.alipay.sofa.registry.common.model.client.pb.DataBoxPbOrBuilder>(
                getDataBox(), getParentForChildren(), isClean());
        dataBox_ = null;
      }
      return dataBoxBuilder_;
    }

    private long version_;
    /** <code>int64 version = 6;</code> */
    public long getVersion() {
      return version_;
    }
    /** <code>int64 version = 6;</code> */
    public Builder setVersion(long value) {

      version_ = value;
      onChanged();
      return this;
    }
    /** <code>int64 version = 6;</code> */
    public Builder clearVersion() {

      version_ = 0L;
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

    // @@protoc_insertion_point(builder_scope:ReceivedConfigDataPb)
  }

  // @@protoc_insertion_point(class_scope:ReceivedConfigDataPb)
  private static final com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb
      DEFAULT_INSTANCE;

  static {
    DEFAULT_INSTANCE = new com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb();
  }

  public static com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb
      getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<ReceivedConfigDataPb> PARSER =
      new com.google.protobuf.AbstractParser<ReceivedConfigDataPb>() {
        public ReceivedConfigDataPb parsePartialFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
          return new ReceivedConfigDataPb(input, extensionRegistry);
        }
      };

  public static com.google.protobuf.Parser<ReceivedConfigDataPb> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<ReceivedConfigDataPb> getParserForType() {
    return PARSER;
  }

  public com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb
      getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }
}
