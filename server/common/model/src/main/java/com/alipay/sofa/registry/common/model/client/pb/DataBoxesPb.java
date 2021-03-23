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

/** Protobuf type {@code DataBoxesPb} */
public final class DataBoxesPb extends com.google.protobuf.GeneratedMessageV3
    implements
    // @@protoc_insertion_point(message_implements:DataBoxesPb)
    DataBoxesPbOrBuilder {
  private static final long serialVersionUID = 0L;
  // Use DataBoxesPb.newBuilder() to construct.
  private DataBoxesPb(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }

  private DataBoxesPb() {
    data_ = java.util.Collections.emptyList();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
    return this.unknownFields;
  }

  private DataBoxesPb(
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
                data_ =
                    new java.util.ArrayList<
                        com.alipay.sofa.registry.common.model.client.pb.DataBoxPb>();
                mutable_bitField0_ |= 0x00000001;
              }
              data_.add(
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
      if (((mutable_bitField0_ & 0x00000001) == 0x00000001)) {
        data_ = java.util.Collections.unmodifiableList(data_);
      }
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }

  public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
    return com.alipay.sofa.registry.common.model.client.pb.DataBoxesPbOuterClass
        .internal_static_DataBoxesPb_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.alipay.sofa.registry.common.model.client.pb.DataBoxesPbOuterClass
        .internal_static_DataBoxesPb_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb.class,
            com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb.Builder.class);
  }

  public static final int DATA_FIELD_NUMBER = 1;
  private java.util.List<com.alipay.sofa.registry.common.model.client.pb.DataBoxPb> data_;
  /** <code>repeated .DataBoxPb data = 1;</code> */
  public java.util.List<com.alipay.sofa.registry.common.model.client.pb.DataBoxPb> getDataList() {
    return data_;
  }
  /** <code>repeated .DataBoxPb data = 1;</code> */
  public java.util.List<
          ? extends com.alipay.sofa.registry.common.model.client.pb.DataBoxPbOrBuilder>
      getDataOrBuilderList() {
    return data_;
  }
  /** <code>repeated .DataBoxPb data = 1;</code> */
  public int getDataCount() {
    return data_.size();
  }
  /** <code>repeated .DataBoxPb data = 1;</code> */
  public com.alipay.sofa.registry.common.model.client.pb.DataBoxPb getData(int index) {
    return data_.get(index);
  }
  /** <code>repeated .DataBoxPb data = 1;</code> */
  public com.alipay.sofa.registry.common.model.client.pb.DataBoxPbOrBuilder getDataOrBuilder(
      int index) {
    return data_.get(index);
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
    for (int i = 0; i < data_.size(); i++) {
      output.writeMessage(1, data_.get(i));
    }
    unknownFields.writeTo(output);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    for (int i = 0; i < data_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream.computeMessageSize(1, data_.get(i));
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
    if (!(obj instanceof com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb)) {
      return super.equals(obj);
    }
    com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb other =
        (com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb) obj;

    boolean result = true;
    result = result && getDataList().equals(other.getDataList());
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
    if (getDataCount() > 0) {
      hash = (37 * hash) + DATA_FIELD_NUMBER;
      hash = (53 * hash) + getDataList().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb parseFrom(
      java.nio.ByteBuffer data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb parseFrom(
      java.nio.ByteBuffer data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb parseFrom(
      byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb parseFrom(
      java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb parseFrom(
      java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb parseDelimitedFrom(
      java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb parseDelimitedFrom(
      java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb parseFrom(
      com.google.protobuf.CodedInputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb parseFrom(
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
      com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb prototype) {
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
  /** Protobuf type {@code DataBoxesPb} */
  public static final class Builder extends com.google.protobuf.GeneratedMessageV3.Builder<Builder>
      implements
      // @@protoc_insertion_point(builder_implements:DataBoxesPb)
      com.alipay.sofa.registry.common.model.client.pb.DataBoxesPbOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
      return com.alipay.sofa.registry.common.model.client.pb.DataBoxesPbOuterClass
          .internal_static_DataBoxesPb_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.alipay.sofa.registry.common.model.client.pb.DataBoxesPbOuterClass
          .internal_static_DataBoxesPb_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb.class,
              com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb.Builder.class);
    }

    // Construct using com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }

    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders) {
        getDataFieldBuilder();
      }
    }

    public Builder clear() {
      super.clear();
      if (dataBuilder_ == null) {
        data_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000001);
      } else {
        dataBuilder_.clear();
      }
      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
      return com.alipay.sofa.registry.common.model.client.pb.DataBoxesPbOuterClass
          .internal_static_DataBoxesPb_descriptor;
    }

    public com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb getDefaultInstanceForType() {
      return com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb.getDefaultInstance();
    }

    public com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb build() {
      com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb buildPartial() {
      com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb result =
          new com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb(this);
      int from_bitField0_ = bitField0_;
      if (dataBuilder_ == null) {
        if (((bitField0_ & 0x00000001) == 0x00000001)) {
          data_ = java.util.Collections.unmodifiableList(data_);
          bitField0_ = (bitField0_ & ~0x00000001);
        }
        result.data_ = data_;
      } else {
        result.data_ = dataBuilder_.build();
      }
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
      if (other instanceof com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb) {
        return mergeFrom((com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb) other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb other) {
      if (other == com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb.getDefaultInstance())
        return this;
      if (dataBuilder_ == null) {
        if (!other.data_.isEmpty()) {
          if (data_.isEmpty()) {
            data_ = other.data_;
            bitField0_ = (bitField0_ & ~0x00000001);
          } else {
            ensureDataIsMutable();
            data_.addAll(other.data_);
          }
          onChanged();
        }
      } else {
        if (!other.data_.isEmpty()) {
          if (dataBuilder_.isEmpty()) {
            dataBuilder_.dispose();
            dataBuilder_ = null;
            data_ = other.data_;
            bitField0_ = (bitField0_ & ~0x00000001);
            dataBuilder_ =
                com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders
                    ? getDataFieldBuilder()
                    : null;
          } else {
            dataBuilder_.addAllMessages(other.data_);
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
      com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage =
            (com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private int bitField0_;

    private java.util.List<com.alipay.sofa.registry.common.model.client.pb.DataBoxPb> data_ =
        java.util.Collections.emptyList();

    private void ensureDataIsMutable() {
      if (!((bitField0_ & 0x00000001) == 0x00000001)) {
        data_ =
            new java.util.ArrayList<com.alipay.sofa.registry.common.model.client.pb.DataBoxPb>(
                data_);
        bitField0_ |= 0x00000001;
      }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
            com.alipay.sofa.registry.common.model.client.pb.DataBoxPb,
            com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.Builder,
            com.alipay.sofa.registry.common.model.client.pb.DataBoxPbOrBuilder>
        dataBuilder_;

    /** <code>repeated .DataBoxPb data = 1;</code> */
    public java.util.List<com.alipay.sofa.registry.common.model.client.pb.DataBoxPb> getDataList() {
      if (dataBuilder_ == null) {
        return java.util.Collections.unmodifiableList(data_);
      } else {
        return dataBuilder_.getMessageList();
      }
    }
    /** <code>repeated .DataBoxPb data = 1;</code> */
    public int getDataCount() {
      if (dataBuilder_ == null) {
        return data_.size();
      } else {
        return dataBuilder_.getCount();
      }
    }
    /** <code>repeated .DataBoxPb data = 1;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.DataBoxPb getData(int index) {
      if (dataBuilder_ == null) {
        return data_.get(index);
      } else {
        return dataBuilder_.getMessage(index);
      }
    }
    /** <code>repeated .DataBoxPb data = 1;</code> */
    public Builder setData(
        int index, com.alipay.sofa.registry.common.model.client.pb.DataBoxPb value) {
      if (dataBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureDataIsMutable();
        data_.set(index, value);
        onChanged();
      } else {
        dataBuilder_.setMessage(index, value);
      }
      return this;
    }
    /** <code>repeated .DataBoxPb data = 1;</code> */
    public Builder setData(
        int index,
        com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.Builder builderForValue) {
      if (dataBuilder_ == null) {
        ensureDataIsMutable();
        data_.set(index, builderForValue.build());
        onChanged();
      } else {
        dataBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /** <code>repeated .DataBoxPb data = 1;</code> */
    public Builder addData(com.alipay.sofa.registry.common.model.client.pb.DataBoxPb value) {
      if (dataBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureDataIsMutable();
        data_.add(value);
        onChanged();
      } else {
        dataBuilder_.addMessage(value);
      }
      return this;
    }
    /** <code>repeated .DataBoxPb data = 1;</code> */
    public Builder addData(
        int index, com.alipay.sofa.registry.common.model.client.pb.DataBoxPb value) {
      if (dataBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureDataIsMutable();
        data_.add(index, value);
        onChanged();
      } else {
        dataBuilder_.addMessage(index, value);
      }
      return this;
    }
    /** <code>repeated .DataBoxPb data = 1;</code> */
    public Builder addData(
        com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.Builder builderForValue) {
      if (dataBuilder_ == null) {
        ensureDataIsMutable();
        data_.add(builderForValue.build());
        onChanged();
      } else {
        dataBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /** <code>repeated .DataBoxPb data = 1;</code> */
    public Builder addData(
        int index,
        com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.Builder builderForValue) {
      if (dataBuilder_ == null) {
        ensureDataIsMutable();
        data_.add(index, builderForValue.build());
        onChanged();
      } else {
        dataBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /** <code>repeated .DataBoxPb data = 1;</code> */
    public Builder addAllData(
        java.lang.Iterable<? extends com.alipay.sofa.registry.common.model.client.pb.DataBoxPb>
            values) {
      if (dataBuilder_ == null) {
        ensureDataIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(values, data_);
        onChanged();
      } else {
        dataBuilder_.addAllMessages(values);
      }
      return this;
    }
    /** <code>repeated .DataBoxPb data = 1;</code> */
    public Builder clearData() {
      if (dataBuilder_ == null) {
        data_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000001);
        onChanged();
      } else {
        dataBuilder_.clear();
      }
      return this;
    }
    /** <code>repeated .DataBoxPb data = 1;</code> */
    public Builder removeData(int index) {
      if (dataBuilder_ == null) {
        ensureDataIsMutable();
        data_.remove(index);
        onChanged();
      } else {
        dataBuilder_.remove(index);
      }
      return this;
    }
    /** <code>repeated .DataBoxPb data = 1;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.Builder getDataBuilder(
        int index) {
      return getDataFieldBuilder().getBuilder(index);
    }
    /** <code>repeated .DataBoxPb data = 1;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.DataBoxPbOrBuilder getDataOrBuilder(
        int index) {
      if (dataBuilder_ == null) {
        return data_.get(index);
      } else {
        return dataBuilder_.getMessageOrBuilder(index);
      }
    }
    /** <code>repeated .DataBoxPb data = 1;</code> */
    public java.util.List<
            ? extends com.alipay.sofa.registry.common.model.client.pb.DataBoxPbOrBuilder>
        getDataOrBuilderList() {
      if (dataBuilder_ != null) {
        return dataBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(data_);
      }
    }
    /** <code>repeated .DataBoxPb data = 1;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.Builder addDataBuilder() {
      return getDataFieldBuilder()
          .addBuilder(
              com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.getDefaultInstance());
    }
    /** <code>repeated .DataBoxPb data = 1;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.Builder addDataBuilder(
        int index) {
      return getDataFieldBuilder()
          .addBuilder(
              index,
              com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.getDefaultInstance());
    }
    /** <code>repeated .DataBoxPb data = 1;</code> */
    public java.util.List<com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.Builder>
        getDataBuilderList() {
      return getDataFieldBuilder().getBuilderList();
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
            com.alipay.sofa.registry.common.model.client.pb.DataBoxPb,
            com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.Builder,
            com.alipay.sofa.registry.common.model.client.pb.DataBoxPbOrBuilder>
        getDataFieldBuilder() {
      if (dataBuilder_ == null) {
        dataBuilder_ =
            new com.google.protobuf.RepeatedFieldBuilderV3<
                com.alipay.sofa.registry.common.model.client.pb.DataBoxPb,
                com.alipay.sofa.registry.common.model.client.pb.DataBoxPb.Builder,
                com.alipay.sofa.registry.common.model.client.pb.DataBoxPbOrBuilder>(
                data_,
                ((bitField0_ & 0x00000001) == 0x00000001),
                getParentForChildren(),
                isClean());
        data_ = null;
      }
      return dataBuilder_;
    }

    public final Builder setUnknownFields(final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFieldsProto3(unknownFields);
    }

    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }

    // @@protoc_insertion_point(builder_scope:DataBoxesPb)
  }

  // @@protoc_insertion_point(class_scope:DataBoxesPb)
  private static final com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb DEFAULT_INSTANCE;

  static {
    DEFAULT_INSTANCE = new com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb();
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<DataBoxesPb> PARSER =
      new com.google.protobuf.AbstractParser<DataBoxesPb>() {
        public DataBoxesPb parsePartialFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
          return new DataBoxesPb(input, extensionRegistry);
        }
      };

  public static com.google.protobuf.Parser<DataBoxesPb> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<DataBoxesPb> getParserForType() {
    return PARSER;
  }

  public com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }
}
