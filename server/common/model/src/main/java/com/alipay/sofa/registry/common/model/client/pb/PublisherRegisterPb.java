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

/** Protobuf type {@code PublisherRegisterPb} */
public final class PublisherRegisterPb extends com.google.protobuf.GeneratedMessageV3
    implements
    // @@protoc_insertion_point(message_implements:PublisherRegisterPb)
    PublisherRegisterPbOrBuilder {
  private static final long serialVersionUID = 0L;
  // Use PublisherRegisterPb.newBuilder() to construct.
  private PublisherRegisterPb(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }

  private PublisherRegisterPb() {
    dataList_ = java.util.Collections.emptyList();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
    return this.unknownFields;
  }

  private PublisherRegisterPb(
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
              if (!((mutable_bitField0_ & 0x00000001) == 0x00000001)) {
                dataList_ =
                    new java.util.ArrayList<
                        com.alipay.sofa.registry.common.model.client.pb.DataBoxPb>();
                mutable_bitField0_ |= 0x00000001;
              }
              dataList_.add(
                  input.readMessage(
                      com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.parser(),
                      extensionRegistry));
              break;
            }
          case 18:
            {
              com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb.Builder subBuilder =
                  null;
              if (baseRegister_ != null) {
                subBuilder = baseRegister_.toBuilder();
              }
              baseRegister_ =
                  input.readMessage(
                      com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb.parser(),
                      extensionRegistry);
              if (subBuilder != null) {
                subBuilder.mergeFrom(baseRegister_);
                baseRegister_ = subBuilder.buildPartial();
              }

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
        dataList_ = java.util.Collections.unmodifiableList(dataList_);
      }
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }

  public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
    return com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPbOuterClass
        .internal_static_PublisherRegisterPb_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPbOuterClass
        .internal_static_PublisherRegisterPb_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb.class,
            com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb.Builder.class);
  }

  private int bitField0_;
  public static final int DATALIST_FIELD_NUMBER = 1;
  private java.util.List<com.alipay.sofa.registry.common.model.client.pb.DataBoxPb> dataList_;
  /** <code>repeated .DataBoxPb dataList = 1;</code> */
  public java.util.List<com.alipay.sofa.registry.common.model.client.pb.DataBoxPb>
      getDataListList() {
    return dataList_;
  }
  /** <code>repeated .DataBoxPb dataList = 1;</code> */
  public java.util.List<
          ? extends com.alipay.sofa.registry.common.model.client.pb.DataBoxPbOrBuilder>
      getDataListOrBuilderList() {
    return dataList_;
  }
  /** <code>repeated .DataBoxPb dataList = 1;</code> */
  public int getDataListCount() {
    return dataList_.size();
  }
  /** <code>repeated .DataBoxPb dataList = 1;</code> */
  public com.alipay.sofa.registry.common.model.client.pb.DataBoxPb getDataList(int index) {
    return dataList_.get(index);
  }
  /** <code>repeated .DataBoxPb dataList = 1;</code> */
  public com.alipay.sofa.registry.common.model.client.pb.DataBoxPbOrBuilder getDataListOrBuilder(
      int index) {
    return dataList_.get(index);
  }

  public static final int BASEREGISTER_FIELD_NUMBER = 2;
  private com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb baseRegister_;
  /** <code>.BaseRegisterPb baseRegister = 2;</code> */
  public boolean hasBaseRegister() {
    return baseRegister_ != null;
  }
  /** <code>.BaseRegisterPb baseRegister = 2;</code> */
  public com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb getBaseRegister() {
    return baseRegister_ == null
        ? com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb.getDefaultInstance()
        : baseRegister_;
  }
  /** <code>.BaseRegisterPb baseRegister = 2;</code> */
  public com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPbOrBuilder
      getBaseRegisterOrBuilder() {
    return getBaseRegister();
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
    for (int i = 0; i < dataList_.size(); i++) {
      output.writeMessage(1, dataList_.get(i));
    }
    if (baseRegister_ != null) {
      output.writeMessage(2, getBaseRegister());
    }
    unknownFields.writeTo(output);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    for (int i = 0; i < dataList_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream.computeMessageSize(1, dataList_.get(i));
    }
    if (baseRegister_ != null) {
      size += com.google.protobuf.CodedOutputStream.computeMessageSize(2, getBaseRegister());
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
    if (!(obj instanceof com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb)) {
      return super.equals(obj);
    }
    com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb other =
        (com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb) obj;

    boolean result = true;
    result = result && getDataListList().equals(other.getDataListList());
    result = result && (hasBaseRegister() == other.hasBaseRegister());
    if (hasBaseRegister()) {
      result = result && getBaseRegister().equals(other.getBaseRegister());
    }
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
    if (getDataListCount() > 0) {
      hash = (37 * hash) + DATALIST_FIELD_NUMBER;
      hash = (53 * hash) + getDataListList().hashCode();
    }
    if (hasBaseRegister()) {
      hash = (37 * hash) + BASEREGISTER_FIELD_NUMBER;
      hash = (53 * hash) + getBaseRegister().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb parseFrom(
      java.nio.ByteBuffer data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb parseFrom(
      java.nio.ByteBuffer data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb parseFrom(
      byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb parseFrom(
      byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb parseFrom(
      java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb parseFrom(
      java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb
      parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb
      parseDelimitedFrom(
          java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb parseFrom(
      com.google.protobuf.CodedInputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb parseFrom(
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
      com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb prototype) {
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
  /** Protobuf type {@code PublisherRegisterPb} */
  public static final class Builder extends com.google.protobuf.GeneratedMessageV3.Builder<Builder>
      implements
      // @@protoc_insertion_point(builder_implements:PublisherRegisterPb)
      com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPbOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
      return com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPbOuterClass
          .internal_static_PublisherRegisterPb_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPbOuterClass
          .internal_static_PublisherRegisterPb_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb.class,
              com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb.Builder.class);
    }

    // Construct using
    // com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }

    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders) {
        getDataListFieldBuilder();
      }
    }

    public Builder clear() {
      super.clear();
      if (dataListBuilder_ == null) {
        dataList_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000001);
      } else {
        dataListBuilder_.clear();
      }
      if (baseRegisterBuilder_ == null) {
        baseRegister_ = null;
      } else {
        baseRegister_ = null;
        baseRegisterBuilder_ = null;
      }
      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
      return com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPbOuterClass
          .internal_static_PublisherRegisterPb_descriptor;
    }

    public com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb
        getDefaultInstanceForType() {
      return com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb
          .getDefaultInstance();
    }

    public com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb build() {
      com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb buildPartial() {
      com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb result =
          new com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb(this);
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      if (dataListBuilder_ == null) {
        if (((bitField0_ & 0x00000001) == 0x00000001)) {
          dataList_ = java.util.Collections.unmodifiableList(dataList_);
          bitField0_ = (bitField0_ & ~0x00000001);
        }
        result.dataList_ = dataList_;
      } else {
        result.dataList_ = dataListBuilder_.build();
      }
      if (baseRegisterBuilder_ == null) {
        result.baseRegister_ = baseRegister_;
      } else {
        result.baseRegister_ = baseRegisterBuilder_.build();
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
      if (other instanceof com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb) {
        return mergeFrom(
            (com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb) other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(
        com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb other) {
      if (other
          == com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb
              .getDefaultInstance()) return this;
      if (dataListBuilder_ == null) {
        if (!other.dataList_.isEmpty()) {
          if (dataList_.isEmpty()) {
            dataList_ = other.dataList_;
            bitField0_ = (bitField0_ & ~0x00000001);
          } else {
            ensureDataListIsMutable();
            dataList_.addAll(other.dataList_);
          }
          onChanged();
        }
      } else {
        if (!other.dataList_.isEmpty()) {
          if (dataListBuilder_.isEmpty()) {
            dataListBuilder_.dispose();
            dataListBuilder_ = null;
            dataList_ = other.dataList_;
            bitField0_ = (bitField0_ & ~0x00000001);
            dataListBuilder_ =
                com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders
                    ? getDataListFieldBuilder()
                    : null;
          } else {
            dataListBuilder_.addAllMessages(other.dataList_);
          }
        }
      }
      if (other.hasBaseRegister()) {
        mergeBaseRegister(other.getBaseRegister());
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
      com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage =
            (com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb)
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

    private java.util.List<com.alipay.sofa.registry.common.model.client.pb.DataBoxPb> dataList_ =
        java.util.Collections.emptyList();

    private void ensureDataListIsMutable() {
      if (!((bitField0_ & 0x00000001) == 0x00000001)) {
        dataList_ =
            new java.util.ArrayList<com.alipay.sofa.registry.common.model.client.pb.DataBoxPb>(
                dataList_);
        bitField0_ |= 0x00000001;
      }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
            com.alipay.sofa.registry.common.model.client.pb.DataBoxPb,
            com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.Builder,
            com.alipay.sofa.registry.common.model.client.pb.DataBoxPbOrBuilder>
        dataListBuilder_;

    /** <code>repeated .DataBoxPb dataList = 1;</code> */
    public java.util.List<com.alipay.sofa.registry.common.model.client.pb.DataBoxPb>
        getDataListList() {
      if (dataListBuilder_ == null) {
        return java.util.Collections.unmodifiableList(dataList_);
      } else {
        return dataListBuilder_.getMessageList();
      }
    }
    /** <code>repeated .DataBoxPb dataList = 1;</code> */
    public int getDataListCount() {
      if (dataListBuilder_ == null) {
        return dataList_.size();
      } else {
        return dataListBuilder_.getCount();
      }
    }
    /** <code>repeated .DataBoxPb dataList = 1;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.DataBoxPb getDataList(int index) {
      if (dataListBuilder_ == null) {
        return dataList_.get(index);
      } else {
        return dataListBuilder_.getMessage(index);
      }
    }
    /** <code>repeated .DataBoxPb dataList = 1;</code> */
    public Builder setDataList(
        int index, com.alipay.sofa.registry.common.model.client.pb.DataBoxPb value) {
      if (dataListBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureDataListIsMutable();
        dataList_.set(index, value);
        onChanged();
      } else {
        dataListBuilder_.setMessage(index, value);
      }
      return this;
    }
    /** <code>repeated .DataBoxPb dataList = 1;</code> */
    public Builder setDataList(
        int index,
        com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.Builder builderForValue) {
      if (dataListBuilder_ == null) {
        ensureDataListIsMutable();
        dataList_.set(index, builderForValue.build());
        onChanged();
      } else {
        dataListBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /** <code>repeated .DataBoxPb dataList = 1;</code> */
    public Builder addDataList(com.alipay.sofa.registry.common.model.client.pb.DataBoxPb value) {
      if (dataListBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureDataListIsMutable();
        dataList_.add(value);
        onChanged();
      } else {
        dataListBuilder_.addMessage(value);
      }
      return this;
    }
    /** <code>repeated .DataBoxPb dataList = 1;</code> */
    public Builder addDataList(
        int index, com.alipay.sofa.registry.common.model.client.pb.DataBoxPb value) {
      if (dataListBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureDataListIsMutable();
        dataList_.add(index, value);
        onChanged();
      } else {
        dataListBuilder_.addMessage(index, value);
      }
      return this;
    }
    /** <code>repeated .DataBoxPb dataList = 1;</code> */
    public Builder addDataList(
        com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.Builder builderForValue) {
      if (dataListBuilder_ == null) {
        ensureDataListIsMutable();
        dataList_.add(builderForValue.build());
        onChanged();
      } else {
        dataListBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /** <code>repeated .DataBoxPb dataList = 1;</code> */
    public Builder addDataList(
        int index,
        com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.Builder builderForValue) {
      if (dataListBuilder_ == null) {
        ensureDataListIsMutable();
        dataList_.add(index, builderForValue.build());
        onChanged();
      } else {
        dataListBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /** <code>repeated .DataBoxPb dataList = 1;</code> */
    public Builder addAllDataList(
        java.lang.Iterable<? extends com.alipay.sofa.registry.common.model.client.pb.DataBoxPb>
            values) {
      if (dataListBuilder_ == null) {
        ensureDataListIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(values, dataList_);
        onChanged();
      } else {
        dataListBuilder_.addAllMessages(values);
      }
      return this;
    }
    /** <code>repeated .DataBoxPb dataList = 1;</code> */
    public Builder clearDataList() {
      if (dataListBuilder_ == null) {
        dataList_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000001);
        onChanged();
      } else {
        dataListBuilder_.clear();
      }
      return this;
    }
    /** <code>repeated .DataBoxPb dataList = 1;</code> */
    public Builder removeDataList(int index) {
      if (dataListBuilder_ == null) {
        ensureDataListIsMutable();
        dataList_.remove(index);
        onChanged();
      } else {
        dataListBuilder_.remove(index);
      }
      return this;
    }
    /** <code>repeated .DataBoxPb dataList = 1;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.Builder getDataListBuilder(
        int index) {
      return getDataListFieldBuilder().getBuilder(index);
    }
    /** <code>repeated .DataBoxPb dataList = 1;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.DataBoxPbOrBuilder getDataListOrBuilder(
        int index) {
      if (dataListBuilder_ == null) {
        return dataList_.get(index);
      } else {
        return dataListBuilder_.getMessageOrBuilder(index);
      }
    }
    /** <code>repeated .DataBoxPb dataList = 1;</code> */
    public java.util.List<
            ? extends com.alipay.sofa.registry.common.model.client.pb.DataBoxPbOrBuilder>
        getDataListOrBuilderList() {
      if (dataListBuilder_ != null) {
        return dataListBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(dataList_);
      }
    }
    /** <code>repeated .DataBoxPb dataList = 1;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.Builder addDataListBuilder() {
      return getDataListFieldBuilder()
          .addBuilder(
              com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.getDefaultInstance());
    }
    /** <code>repeated .DataBoxPb dataList = 1;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.Builder addDataListBuilder(
        int index) {
      return getDataListFieldBuilder()
          .addBuilder(
              index,
              com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.getDefaultInstance());
    }
    /** <code>repeated .DataBoxPb dataList = 1;</code> */
    public java.util.List<com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.Builder>
        getDataListBuilderList() {
      return getDataListFieldBuilder().getBuilderList();
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
            com.alipay.sofa.registry.common.model.client.pb.DataBoxPb,
            com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.Builder,
            com.alipay.sofa.registry.common.model.client.pb.DataBoxPbOrBuilder>
        getDataListFieldBuilder() {
      if (dataListBuilder_ == null) {
        dataListBuilder_ =
            new com.google.protobuf.RepeatedFieldBuilderV3<
                com.alipay.sofa.registry.common.model.client.pb.DataBoxPb,
                com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.Builder,
                com.alipay.sofa.registry.common.model.client.pb.DataBoxPbOrBuilder>(
                dataList_,
                ((bitField0_ & 0x00000001) == 0x00000001),
                getParentForChildren(),
                isClean());
        dataList_ = null;
      }
      return dataListBuilder_;
    }

    private com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb baseRegister_ = null;
    private com.google.protobuf.SingleFieldBuilderV3<
            com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb,
            com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb.Builder,
            com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPbOrBuilder>
        baseRegisterBuilder_;
    /** <code>.BaseRegisterPb baseRegister = 2;</code> */
    public boolean hasBaseRegister() {
      return baseRegisterBuilder_ != null || baseRegister_ != null;
    }
    /** <code>.BaseRegisterPb baseRegister = 2;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb getBaseRegister() {
      if (baseRegisterBuilder_ == null) {
        return baseRegister_ == null
            ? com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb.getDefaultInstance()
            : baseRegister_;
      } else {
        return baseRegisterBuilder_.getMessage();
      }
    }
    /** <code>.BaseRegisterPb baseRegister = 2;</code> */
    public Builder setBaseRegister(
        com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb value) {
      if (baseRegisterBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        baseRegister_ = value;
        onChanged();
      } else {
        baseRegisterBuilder_.setMessage(value);
      }

      return this;
    }
    /** <code>.BaseRegisterPb baseRegister = 2;</code> */
    public Builder setBaseRegister(
        com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb.Builder builderForValue) {
      if (baseRegisterBuilder_ == null) {
        baseRegister_ = builderForValue.build();
        onChanged();
      } else {
        baseRegisterBuilder_.setMessage(builderForValue.build());
      }

      return this;
    }
    /** <code>.BaseRegisterPb baseRegister = 2;</code> */
    public Builder mergeBaseRegister(
        com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb value) {
      if (baseRegisterBuilder_ == null) {
        if (baseRegister_ != null) {
          baseRegister_ =
              com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb.newBuilder(
                      baseRegister_)
                  .mergeFrom(value)
                  .buildPartial();
        } else {
          baseRegister_ = value;
        }
        onChanged();
      } else {
        baseRegisterBuilder_.mergeFrom(value);
      }

      return this;
    }
    /** <code>.BaseRegisterPb baseRegister = 2;</code> */
    public Builder clearBaseRegister() {
      if (baseRegisterBuilder_ == null) {
        baseRegister_ = null;
        onChanged();
      } else {
        baseRegister_ = null;
        baseRegisterBuilder_ = null;
      }

      return this;
    }
    /** <code>.BaseRegisterPb baseRegister = 2;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb.Builder
        getBaseRegisterBuilder() {

      onChanged();
      return getBaseRegisterFieldBuilder().getBuilder();
    }
    /** <code>.BaseRegisterPb baseRegister = 2;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPbOrBuilder
        getBaseRegisterOrBuilder() {
      if (baseRegisterBuilder_ != null) {
        return baseRegisterBuilder_.getMessageOrBuilder();
      } else {
        return baseRegister_ == null
            ? com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb.getDefaultInstance()
            : baseRegister_;
      }
    }
    /** <code>.BaseRegisterPb baseRegister = 2;</code> */
    private com.google.protobuf.SingleFieldBuilderV3<
            com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb,
            com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb.Builder,
            com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPbOrBuilder>
        getBaseRegisterFieldBuilder() {
      if (baseRegisterBuilder_ == null) {
        baseRegisterBuilder_ =
            new com.google.protobuf.SingleFieldBuilderV3<
                com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb,
                com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb.Builder,
                com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPbOrBuilder>(
                getBaseRegister(), getParentForChildren(), isClean());
        baseRegister_ = null;
      }
      return baseRegisterBuilder_;
    }

    public final Builder setUnknownFields(final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFieldsProto3(unknownFields);
    }

    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }

    // @@protoc_insertion_point(builder_scope:PublisherRegisterPb)
  }

  // @@protoc_insertion_point(class_scope:PublisherRegisterPb)
  private static final com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb
      DEFAULT_INSTANCE;

  static {
    DEFAULT_INSTANCE = new com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb();
  }

  public static com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb
      getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<PublisherRegisterPb> PARSER =
      new com.google.protobuf.AbstractParser<PublisherRegisterPb>() {
        public PublisherRegisterPb parsePartialFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
          return new PublisherRegisterPb(input, extensionRegistry);
        }
      };

  public static com.google.protobuf.Parser<PublisherRegisterPb> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<PublisherRegisterPb> getParserForType() {
    return PARSER;
  }

  public com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb
      getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }
}
