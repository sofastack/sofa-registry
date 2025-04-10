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

/** Protobuf type {@code DeltaDigestPb} */
public final class DeltaDigestPb extends com.google.protobuf.GeneratedMessageV3
    implements
    // @@protoc_insertion_point(message_implements:DeltaDigestPb)
    DeltaDigestPbOrBuilder {
  private static final long serialVersionUID = 0L;
  // Use DeltaDigestPb.newBuilder() to construct.
  private DeltaDigestPb(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }

  private DeltaDigestPb() {
    publisherCount_ = 0L;
    dataCount_ = 0L;
    registerIdHashSum_ = 0L;
    registerIdHashXOR_ = 0L;
    registerTimestampSum_ = 0L;
    registerTimestampXOR_ = 0L;
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
    return this.unknownFields;
  }

  private DeltaDigestPb(
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
              publisherCount_ = input.readInt64();
              break;
            }
          case 16:
            {
              dataCount_ = input.readInt64();
              break;
            }
          case 24:
            {
              registerIdHashSum_ = input.readInt64();
              break;
            }
          case 32:
            {
              registerIdHashXOR_ = input.readInt64();
              break;
            }
          case 40:
            {
              registerTimestampSum_ = input.readInt64();
              break;
            }
          case 48:
            {
              registerTimestampXOR_ = input.readInt64();
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
    return com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPbOuterClass
        .internal_static_DeltaDigestPb_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPbOuterClass
        .internal_static_DeltaDigestPb_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb.class,
            com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb.Builder.class);
  }

  public static final int PUBLISHERCOUNT_FIELD_NUMBER = 1;
  private long publisherCount_;
  /** <code>int64 publisherCount = 1;</code> */
  public long getPublisherCount() {
    return publisherCount_;
  }

  public static final int DATACOUNT_FIELD_NUMBER = 2;
  private long dataCount_;
  /** <code>int64 dataCount = 2;</code> */
  public long getDataCount() {
    return dataCount_;
  }

  public static final int REGISTERIDHASHSUM_FIELD_NUMBER = 3;
  private long registerIdHashSum_;
  /** <code>int64 registerIdHashSum = 3;</code> */
  public long getRegisterIdHashSum() {
    return registerIdHashSum_;
  }

  public static final int REGISTERIDHASHXOR_FIELD_NUMBER = 4;
  private long registerIdHashXOR_;
  /** <code>int64 registerIdHashXOR = 4;</code> */
  public long getRegisterIdHashXOR() {
    return registerIdHashXOR_;
  }

  public static final int REGISTERTIMESTAMPSUM_FIELD_NUMBER = 5;
  private long registerTimestampSum_;
  /** <code>int64 registerTimestampSum = 5;</code> */
  public long getRegisterTimestampSum() {
    return registerTimestampSum_;
  }

  public static final int REGISTERTIMESTAMPXOR_FIELD_NUMBER = 6;
  private long registerTimestampXOR_;
  /** <code>int64 registerTimestampXOR = 6;</code> */
  public long getRegisterTimestampXOR() {
    return registerTimestampXOR_;
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
    if (publisherCount_ != 0L) {
      output.writeInt64(1, publisherCount_);
    }
    if (dataCount_ != 0L) {
      output.writeInt64(2, dataCount_);
    }
    if (registerIdHashSum_ != 0L) {
      output.writeInt64(3, registerIdHashSum_);
    }
    if (registerIdHashXOR_ != 0L) {
      output.writeInt64(4, registerIdHashXOR_);
    }
    if (registerTimestampSum_ != 0L) {
      output.writeInt64(5, registerTimestampSum_);
    }
    if (registerTimestampXOR_ != 0L) {
      output.writeInt64(6, registerTimestampXOR_);
    }
    unknownFields.writeTo(output);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (publisherCount_ != 0L) {
      size += com.google.protobuf.CodedOutputStream.computeInt64Size(1, publisherCount_);
    }
    if (dataCount_ != 0L) {
      size += com.google.protobuf.CodedOutputStream.computeInt64Size(2, dataCount_);
    }
    if (registerIdHashSum_ != 0L) {
      size += com.google.protobuf.CodedOutputStream.computeInt64Size(3, registerIdHashSum_);
    }
    if (registerIdHashXOR_ != 0L) {
      size += com.google.protobuf.CodedOutputStream.computeInt64Size(4, registerIdHashXOR_);
    }
    if (registerTimestampSum_ != 0L) {
      size += com.google.protobuf.CodedOutputStream.computeInt64Size(5, registerTimestampSum_);
    }
    if (registerTimestampXOR_ != 0L) {
      size += com.google.protobuf.CodedOutputStream.computeInt64Size(6, registerTimestampXOR_);
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
    if (!(obj instanceof com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb)) {
      return super.equals(obj);
    }
    com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb other =
        (com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb) obj;

    boolean result = true;
    result = result && (getPublisherCount() == other.getPublisherCount());
    result = result && (getDataCount() == other.getDataCount());
    result = result && (getRegisterIdHashSum() == other.getRegisterIdHashSum());
    result = result && (getRegisterIdHashXOR() == other.getRegisterIdHashXOR());
    result = result && (getRegisterTimestampSum() == other.getRegisterTimestampSum());
    result = result && (getRegisterTimestampXOR() == other.getRegisterTimestampXOR());
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
    hash = (37 * hash) + PUBLISHERCOUNT_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(getPublisherCount());
    hash = (37 * hash) + DATACOUNT_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(getDataCount());
    hash = (37 * hash) + REGISTERIDHASHSUM_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(getRegisterIdHashSum());
    hash = (37 * hash) + REGISTERIDHASHXOR_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(getRegisterIdHashXOR());
    hash = (37 * hash) + REGISTERTIMESTAMPSUM_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(getRegisterTimestampSum());
    hash = (37 * hash) + REGISTERTIMESTAMPXOR_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(getRegisterTimestampXOR());
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb parseFrom(
      java.nio.ByteBuffer data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb parseFrom(
      java.nio.ByteBuffer data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb parseFrom(
      byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb parseFrom(
      java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb parseFrom(
      java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb parseDelimitedFrom(
      java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb parseDelimitedFrom(
      java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb parseFrom(
      com.google.protobuf.CodedInputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb parseFrom(
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
      com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb prototype) {
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
  /** Protobuf type {@code DeltaDigestPb} */
  public static final class Builder extends com.google.protobuf.GeneratedMessageV3.Builder<Builder>
      implements
      // @@protoc_insertion_point(builder_implements:DeltaDigestPb)
      com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPbOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
      return com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPbOuterClass
          .internal_static_DeltaDigestPb_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPbOuterClass
          .internal_static_DeltaDigestPb_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb.class,
              com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb.Builder.class);
    }

    // Construct using com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb.newBuilder()
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
      publisherCount_ = 0L;

      dataCount_ = 0L;

      registerIdHashSum_ = 0L;

      registerIdHashXOR_ = 0L;

      registerTimestampSum_ = 0L;

      registerTimestampXOR_ = 0L;

      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
      return com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPbOuterClass
          .internal_static_DeltaDigestPb_descriptor;
    }

    public com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb
        getDefaultInstanceForType() {
      return com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb.getDefaultInstance();
    }

    public com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb build() {
      com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb buildPartial() {
      com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb result =
          new com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb(this);
      result.publisherCount_ = publisherCount_;
      result.dataCount_ = dataCount_;
      result.registerIdHashSum_ = registerIdHashSum_;
      result.registerIdHashXOR_ = registerIdHashXOR_;
      result.registerTimestampSum_ = registerTimestampSum_;
      result.registerTimestampXOR_ = registerTimestampXOR_;
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
      if (other instanceof com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb) {
        return mergeFrom((com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb) other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb other) {
      if (other
          == com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb.getDefaultInstance())
        return this;
      if (other.getPublisherCount() != 0L) {
        setPublisherCount(other.getPublisherCount());
      }
      if (other.getDataCount() != 0L) {
        setDataCount(other.getDataCount());
      }
      if (other.getRegisterIdHashSum() != 0L) {
        setRegisterIdHashSum(other.getRegisterIdHashSum());
      }
      if (other.getRegisterIdHashXOR() != 0L) {
        setRegisterIdHashXOR(other.getRegisterIdHashXOR());
      }
      if (other.getRegisterTimestampSum() != 0L) {
        setRegisterTimestampSum(other.getRegisterTimestampSum());
      }
      if (other.getRegisterTimestampXOR() != 0L) {
        setRegisterTimestampXOR(other.getRegisterTimestampXOR());
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
      com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage =
            (com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb)
                e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private long publisherCount_;
    /** <code>int64 publisherCount = 1;</code> */
    public long getPublisherCount() {
      return publisherCount_;
    }
    /** <code>int64 publisherCount = 1;</code> */
    public Builder setPublisherCount(long value) {

      publisherCount_ = value;
      onChanged();
      return this;
    }
    /** <code>int64 publisherCount = 1;</code> */
    public Builder clearPublisherCount() {

      publisherCount_ = 0L;
      onChanged();
      return this;
    }

    private long dataCount_;
    /** <code>int64 dataCount = 2;</code> */
    public long getDataCount() {
      return dataCount_;
    }
    /** <code>int64 dataCount = 2;</code> */
    public Builder setDataCount(long value) {

      dataCount_ = value;
      onChanged();
      return this;
    }
    /** <code>int64 dataCount = 2;</code> */
    public Builder clearDataCount() {

      dataCount_ = 0L;
      onChanged();
      return this;
    }

    private long registerIdHashSum_;
    /** <code>int64 registerIdHashSum = 3;</code> */
    public long getRegisterIdHashSum() {
      return registerIdHashSum_;
    }
    /** <code>int64 registerIdHashSum = 3;</code> */
    public Builder setRegisterIdHashSum(long value) {

      registerIdHashSum_ = value;
      onChanged();
      return this;
    }
    /** <code>int64 registerIdHashSum = 3;</code> */
    public Builder clearRegisterIdHashSum() {

      registerIdHashSum_ = 0L;
      onChanged();
      return this;
    }

    private long registerIdHashXOR_;
    /** <code>int64 registerIdHashXOR = 4;</code> */
    public long getRegisterIdHashXOR() {
      return registerIdHashXOR_;
    }
    /** <code>int64 registerIdHashXOR = 4;</code> */
    public Builder setRegisterIdHashXOR(long value) {

      registerIdHashXOR_ = value;
      onChanged();
      return this;
    }
    /** <code>int64 registerIdHashXOR = 4;</code> */
    public Builder clearRegisterIdHashXOR() {

      registerIdHashXOR_ = 0L;
      onChanged();
      return this;
    }

    private long registerTimestampSum_;
    /** <code>int64 registerTimestampSum = 5;</code> */
    public long getRegisterTimestampSum() {
      return registerTimestampSum_;
    }
    /** <code>int64 registerTimestampSum = 5;</code> */
    public Builder setRegisterTimestampSum(long value) {

      registerTimestampSum_ = value;
      onChanged();
      return this;
    }
    /** <code>int64 registerTimestampSum = 5;</code> */
    public Builder clearRegisterTimestampSum() {

      registerTimestampSum_ = 0L;
      onChanged();
      return this;
    }

    private long registerTimestampXOR_;
    /** <code>int64 registerTimestampXOR = 6;</code> */
    public long getRegisterTimestampXOR() {
      return registerTimestampXOR_;
    }
    /** <code>int64 registerTimestampXOR = 6;</code> */
    public Builder setRegisterTimestampXOR(long value) {

      registerTimestampXOR_ = value;
      onChanged();
      return this;
    }
    /** <code>int64 registerTimestampXOR = 6;</code> */
    public Builder clearRegisterTimestampXOR() {

      registerTimestampXOR_ = 0L;
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

    // @@protoc_insertion_point(builder_scope:DeltaDigestPb)
  }

  // @@protoc_insertion_point(class_scope:DeltaDigestPb)
  private static final com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb
      DEFAULT_INSTANCE;

  static {
    DEFAULT_INSTANCE = new com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb();
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<DeltaDigestPb> PARSER =
      new com.google.protobuf.AbstractParser<DeltaDigestPb>() {
        public DeltaDigestPb parsePartialFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
          return new DeltaDigestPb(input, extensionRegistry);
        }
      };

  public static com.google.protobuf.Parser<DeltaDigestPb> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<DeltaDigestPb> getParserForType() {
    return PARSER;
  }

  public com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }
}
