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

/** Protobuf type {@code SyncConfigResponsePb} */
public final class SyncConfigResponsePb extends com.google.protobuf.GeneratedMessageV3
    implements
    // @@protoc_insertion_point(message_implements:SyncConfigResponsePb)
    SyncConfigResponsePbOrBuilder {
  private static final long serialVersionUID = 0L;
  // Use SyncConfigResponsePb.newBuilder() to construct.
  private SyncConfigResponsePb(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }

  private SyncConfigResponsePb() {
    availableSegments_ = com.google.protobuf.LazyStringArrayList.EMPTY;
    retryInterval_ = 0;
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
    return this.unknownFields;
  }

  private SyncConfigResponsePb(
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
              com.alipay.sofa.registry.common.model.client.pb.ResultPb.Builder subBuilder = null;
              if (result_ != null) {
                subBuilder = result_.toBuilder();
              }
              result_ =
                  input.readMessage(
                      com.alipay.sofa.registry.common.model.client.pb.ResultPb.parser(),
                      extensionRegistry);
              if (subBuilder != null) {
                subBuilder.mergeFrom(result_);
                result_ = subBuilder.buildPartial();
              }

              break;
            }
          case 18:
            {
              java.lang.String s = input.readStringRequireUtf8();
              if (!((mutable_bitField0_ & 0x00000002) == 0x00000002)) {
                availableSegments_ = new com.google.protobuf.LazyStringArrayList();
                mutable_bitField0_ |= 0x00000002;
              }
              availableSegments_.add(s);
              break;
            }
          case 24:
            {
              retryInterval_ = input.readInt32();
              break;
            }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(e).setUnfinishedMessage(this);
    } finally {
      if (((mutable_bitField0_ & 0x00000002) == 0x00000002)) {
        availableSegments_ = availableSegments_.getUnmodifiableView();
      }
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }

  public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
    return com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePbOuterClass
        .internal_static_SyncConfigResponsePb_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePbOuterClass
        .internal_static_SyncConfigResponsePb_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb.class,
            com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb.Builder.class);
  }

  private int bitField0_;
  public static final int RESULT_FIELD_NUMBER = 1;
  private com.alipay.sofa.registry.common.model.client.pb.ResultPb result_;
  /** <code>.ResultPb result = 1;</code> */
  public boolean hasResult() {
    return result_ != null;
  }
  /** <code>.ResultPb result = 1;</code> */
  public com.alipay.sofa.registry.common.model.client.pb.ResultPb getResult() {
    return result_ == null
        ? com.alipay.sofa.registry.common.model.client.pb.ResultPb.getDefaultInstance()
        : result_;
  }
  /** <code>.ResultPb result = 1;</code> */
  public com.alipay.sofa.registry.common.model.client.pb.ResultPbOrBuilder getResultOrBuilder() {
    return getResult();
  }

  public static final int AVAILABLESEGMENTS_FIELD_NUMBER = 2;
  private com.google.protobuf.LazyStringList availableSegments_;
  /** <code>repeated string availableSegments = 2;</code> */
  public com.google.protobuf.ProtocolStringList getAvailableSegmentsList() {
    return availableSegments_;
  }
  /** <code>repeated string availableSegments = 2;</code> */
  public int getAvailableSegmentsCount() {
    return availableSegments_.size();
  }
  /** <code>repeated string availableSegments = 2;</code> */
  public java.lang.String getAvailableSegments(int index) {
    return availableSegments_.get(index);
  }
  /** <code>repeated string availableSegments = 2;</code> */
  public com.google.protobuf.ByteString getAvailableSegmentsBytes(int index) {
    return availableSegments_.getByteString(index);
  }

  public static final int RETRYINTERVAL_FIELD_NUMBER = 3;
  private int retryInterval_;
  /** <code>int32 retryInterval = 3;</code> */
  public int getRetryInterval() {
    return retryInterval_;
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
    if (result_ != null) {
      output.writeMessage(1, getResult());
    }
    for (int i = 0; i < availableSegments_.size(); i++) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 2, availableSegments_.getRaw(i));
    }
    if (retryInterval_ != 0) {
      output.writeInt32(3, retryInterval_);
    }
    unknownFields.writeTo(output);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (result_ != null) {
      size += com.google.protobuf.CodedOutputStream.computeMessageSize(1, getResult());
    }
    {
      int dataSize = 0;
      for (int i = 0; i < availableSegments_.size(); i++) {
        dataSize += computeStringSizeNoTag(availableSegments_.getRaw(i));
      }
      size += dataSize;
      size += 1 * getAvailableSegmentsList().size();
    }
    if (retryInterval_ != 0) {
      size += com.google.protobuf.CodedOutputStream.computeInt32Size(3, retryInterval_);
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
    if (!(obj instanceof com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb)) {
      return super.equals(obj);
    }
    com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb other =
        (com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb) obj;

    boolean result = true;
    result = result && (hasResult() == other.hasResult());
    if (hasResult()) {
      result = result && getResult().equals(other.getResult());
    }
    result = result && getAvailableSegmentsList().equals(other.getAvailableSegmentsList());
    result = result && (getRetryInterval() == other.getRetryInterval());
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
    if (hasResult()) {
      hash = (37 * hash) + RESULT_FIELD_NUMBER;
      hash = (53 * hash) + getResult().hashCode();
    }
    if (getAvailableSegmentsCount() > 0) {
      hash = (37 * hash) + AVAILABLESEGMENTS_FIELD_NUMBER;
      hash = (53 * hash) + getAvailableSegmentsList().hashCode();
    }
    hash = (37 * hash) + RETRYINTERVAL_FIELD_NUMBER;
    hash = (53 * hash) + getRetryInterval();
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb parseFrom(
      java.nio.ByteBuffer data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb parseFrom(
      java.nio.ByteBuffer data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb parseFrom(
      byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb parseFrom(
      byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb parseFrom(
      java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb parseFrom(
      java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb
      parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb
      parseDelimitedFrom(
          java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb parseFrom(
      com.google.protobuf.CodedInputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb parseFrom(
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
      com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb prototype) {
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
  /** Protobuf type {@code SyncConfigResponsePb} */
  public static final class Builder extends com.google.protobuf.GeneratedMessageV3.Builder<Builder>
      implements
      // @@protoc_insertion_point(builder_implements:SyncConfigResponsePb)
      com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePbOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
      return com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePbOuterClass
          .internal_static_SyncConfigResponsePb_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePbOuterClass
          .internal_static_SyncConfigResponsePb_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb.class,
              com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb.Builder.class);
    }

    // Construct using
    // com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb.newBuilder()
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
      if (resultBuilder_ == null) {
        result_ = null;
      } else {
        result_ = null;
        resultBuilder_ = null;
      }
      availableSegments_ = com.google.protobuf.LazyStringArrayList.EMPTY;
      bitField0_ = (bitField0_ & ~0x00000002);
      retryInterval_ = 0;

      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
      return com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePbOuterClass
          .internal_static_SyncConfigResponsePb_descriptor;
    }

    public com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb
        getDefaultInstanceForType() {
      return com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb
          .getDefaultInstance();
    }

    public com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb build() {
      com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb buildPartial() {
      com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb result =
          new com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb(this);
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      if (resultBuilder_ == null) {
        result.result_ = result_;
      } else {
        result.result_ = resultBuilder_.build();
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        availableSegments_ = availableSegments_.getUnmodifiableView();
        bitField0_ = (bitField0_ & ~0x00000002);
      }
      result.availableSegments_ = availableSegments_;
      result.retryInterval_ = retryInterval_;
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
      if (other instanceof com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb) {
        return mergeFrom(
            (com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb) other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(
        com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb other) {
      if (other
          == com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb
              .getDefaultInstance()) return this;
      if (other.hasResult()) {
        mergeResult(other.getResult());
      }
      if (!other.availableSegments_.isEmpty()) {
        if (availableSegments_.isEmpty()) {
          availableSegments_ = other.availableSegments_;
          bitField0_ = (bitField0_ & ~0x00000002);
        } else {
          ensureAvailableSegmentsIsMutable();
          availableSegments_.addAll(other.availableSegments_);
        }
        onChanged();
      }
      if (other.getRetryInterval() != 0) {
        setRetryInterval(other.getRetryInterval());
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
      com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage =
            (com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb)
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

    private com.alipay.sofa.registry.common.model.client.pb.ResultPb result_ = null;
    private com.google.protobuf.SingleFieldBuilderV3<
            com.alipay.sofa.registry.common.model.client.pb.ResultPb,
            com.alipay.sofa.registry.common.model.client.pb.ResultPb.Builder,
            com.alipay.sofa.registry.common.model.client.pb.ResultPbOrBuilder>
        resultBuilder_;
    /** <code>.ResultPb result = 1;</code> */
    public boolean hasResult() {
      return resultBuilder_ != null || result_ != null;
    }
    /** <code>.ResultPb result = 1;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.ResultPb getResult() {
      if (resultBuilder_ == null) {
        return result_ == null
            ? com.alipay.sofa.registry.common.model.client.pb.ResultPb.getDefaultInstance()
            : result_;
      } else {
        return resultBuilder_.getMessage();
      }
    }
    /** <code>.ResultPb result = 1;</code> */
    public Builder setResult(com.alipay.sofa.registry.common.model.client.pb.ResultPb value) {
      if (resultBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        result_ = value;
        onChanged();
      } else {
        resultBuilder_.setMessage(value);
      }

      return this;
    }
    /** <code>.ResultPb result = 1;</code> */
    public Builder setResult(
        com.alipay.sofa.registry.common.model.client.pb.ResultPb.Builder builderForValue) {
      if (resultBuilder_ == null) {
        result_ = builderForValue.build();
        onChanged();
      } else {
        resultBuilder_.setMessage(builderForValue.build());
      }

      return this;
    }
    /** <code>.ResultPb result = 1;</code> */
    public Builder mergeResult(com.alipay.sofa.registry.common.model.client.pb.ResultPb value) {
      if (resultBuilder_ == null) {
        if (result_ != null) {
          result_ =
              com.alipay.sofa.registry.common.model.client.pb.ResultPb.newBuilder(result_)
                  .mergeFrom(value)
                  .buildPartial();
        } else {
          result_ = value;
        }
        onChanged();
      } else {
        resultBuilder_.mergeFrom(value);
      }

      return this;
    }
    /** <code>.ResultPb result = 1;</code> */
    public Builder clearResult() {
      if (resultBuilder_ == null) {
        result_ = null;
        onChanged();
      } else {
        result_ = null;
        resultBuilder_ = null;
      }

      return this;
    }
    /** <code>.ResultPb result = 1;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.ResultPb.Builder getResultBuilder() {

      onChanged();
      return getResultFieldBuilder().getBuilder();
    }
    /** <code>.ResultPb result = 1;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.ResultPbOrBuilder getResultOrBuilder() {
      if (resultBuilder_ != null) {
        return resultBuilder_.getMessageOrBuilder();
      } else {
        return result_ == null
            ? com.alipay.sofa.registry.common.model.client.pb.ResultPb.getDefaultInstance()
            : result_;
      }
    }
    /** <code>.ResultPb result = 1;</code> */
    private com.google.protobuf.SingleFieldBuilderV3<
            com.alipay.sofa.registry.common.model.client.pb.ResultPb,
            com.alipay.sofa.registry.common.model.client.pb.ResultPb.Builder,
            com.alipay.sofa.registry.common.model.client.pb.ResultPbOrBuilder>
        getResultFieldBuilder() {
      if (resultBuilder_ == null) {
        resultBuilder_ =
            new com.google.protobuf.SingleFieldBuilderV3<
                com.alipay.sofa.registry.common.model.client.pb.ResultPb,
                com.alipay.sofa.registry.common.model.client.pb.ResultPb.Builder,
                com.alipay.sofa.registry.common.model.client.pb.ResultPbOrBuilder>(
                getResult(), getParentForChildren(), isClean());
        result_ = null;
      }
      return resultBuilder_;
    }

    private com.google.protobuf.LazyStringList availableSegments_ =
        com.google.protobuf.LazyStringArrayList.EMPTY;

    private void ensureAvailableSegmentsIsMutable() {
      if (!((bitField0_ & 0x00000002) == 0x00000002)) {
        availableSegments_ = new com.google.protobuf.LazyStringArrayList(availableSegments_);
        bitField0_ |= 0x00000002;
      }
    }
    /** <code>repeated string availableSegments = 2;</code> */
    public com.google.protobuf.ProtocolStringList getAvailableSegmentsList() {
      return availableSegments_.getUnmodifiableView();
    }
    /** <code>repeated string availableSegments = 2;</code> */
    public int getAvailableSegmentsCount() {
      return availableSegments_.size();
    }
    /** <code>repeated string availableSegments = 2;</code> */
    public java.lang.String getAvailableSegments(int index) {
      return availableSegments_.get(index);
    }
    /** <code>repeated string availableSegments = 2;</code> */
    public com.google.protobuf.ByteString getAvailableSegmentsBytes(int index) {
      return availableSegments_.getByteString(index);
    }
    /** <code>repeated string availableSegments = 2;</code> */
    public Builder setAvailableSegments(int index, java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }
      ensureAvailableSegmentsIsMutable();
      availableSegments_.set(index, value);
      onChanged();
      return this;
    }
    /** <code>repeated string availableSegments = 2;</code> */
    public Builder addAvailableSegments(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }
      ensureAvailableSegmentsIsMutable();
      availableSegments_.add(value);
      onChanged();
      return this;
    }
    /** <code>repeated string availableSegments = 2;</code> */
    public Builder addAllAvailableSegments(java.lang.Iterable<java.lang.String> values) {
      ensureAvailableSegmentsIsMutable();
      com.google.protobuf.AbstractMessageLite.Builder.addAll(values, availableSegments_);
      onChanged();
      return this;
    }
    /** <code>repeated string availableSegments = 2;</code> */
    public Builder clearAvailableSegments() {
      availableSegments_ = com.google.protobuf.LazyStringArrayList.EMPTY;
      bitField0_ = (bitField0_ & ~0x00000002);
      onChanged();
      return this;
    }
    /** <code>repeated string availableSegments = 2;</code> */
    public Builder addAvailableSegmentsBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);
      ensureAvailableSegmentsIsMutable();
      availableSegments_.add(value);
      onChanged();
      return this;
    }

    private int retryInterval_;
    /** <code>int32 retryInterval = 3;</code> */
    public int getRetryInterval() {
      return retryInterval_;
    }
    /** <code>int32 retryInterval = 3;</code> */
    public Builder setRetryInterval(int value) {

      retryInterval_ = value;
      onChanged();
      return this;
    }
    /** <code>int32 retryInterval = 3;</code> */
    public Builder clearRetryInterval() {

      retryInterval_ = 0;
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

    // @@protoc_insertion_point(builder_scope:SyncConfigResponsePb)
  }

  // @@protoc_insertion_point(class_scope:SyncConfigResponsePb)
  private static final com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb
      DEFAULT_INSTANCE;

  static {
    DEFAULT_INSTANCE = new com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb();
  }

  public static com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb
      getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<SyncConfigResponsePb> PARSER =
      new com.google.protobuf.AbstractParser<SyncConfigResponsePb>() {
        public SyncConfigResponsePb parsePartialFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
          return new SyncConfigResponsePb(input, extensionRegistry);
        }
      };

  public static com.google.protobuf.Parser<SyncConfigResponsePb> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<SyncConfigResponsePb> getParserForType() {
    return PARSER;
  }

  public com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb
      getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }
}
