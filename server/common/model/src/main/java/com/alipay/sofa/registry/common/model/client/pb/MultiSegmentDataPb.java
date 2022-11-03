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

/** Protobuf type {@code MultiSegmentDataPb} */
public final class MultiSegmentDataPb extends com.google.protobuf.GeneratedMessageV3
    implements
    // @@protoc_insertion_point(message_implements:MultiSegmentDataPb)
    MultiSegmentDataPbOrBuilder {
  private static final long serialVersionUID = 0L;
  // Use MultiSegmentDataPb.newBuilder() to construct.
  private MultiSegmentDataPb(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }

  private MultiSegmentDataPb() {
    segment_ = "";
    zipData_ = com.google.protobuf.ByteString.EMPTY;
    encoding_ = "";
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(UnusedPrivateParameter unused) {
    return new MultiSegmentDataPb();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
    return this.unknownFields;
  }

  private MultiSegmentDataPb(
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
          case 10:
            {
              java.lang.String s = input.readStringRequireUtf8();

              segment_ = s;
              break;
            }
          case 18:
            {
              zipData_ = input.readBytes();
              break;
            }
          case 26:
            {
              if (!((mutable_bitField0_ & 0x00000001) != 0)) {
                unzipData_ =
                    com.google.protobuf.MapField.newMapField(
                        UnzipDataDefaultEntryHolder.defaultEntry);
                mutable_bitField0_ |= 0x00000001;
              }
              com.google.protobuf.MapEntry<
                      java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
                  unzipData__ =
                      input.readMessage(
                          UnzipDataDefaultEntryHolder.defaultEntry.getParserForType(),
                          extensionRegistry);
              unzipData_.getMutableMap().put(unzipData__.getKey(), unzipData__.getValue());
              break;
            }
          case 34:
            {
              java.lang.String s = input.readStringRequireUtf8();

              encoding_ = s;
              break;
            }
          case 40:
            {
              version_ = input.readInt64();
              break;
            }
          case 50:
            {
              if (!((mutable_bitField0_ & 0x00000002) != 0)) {
                pushDataCount_ =
                    com.google.protobuf.MapField.newMapField(
                        PushDataCountDefaultEntryHolder.defaultEntry);
                mutable_bitField0_ |= 0x00000002;
              }
              com.google.protobuf.MapEntry<java.lang.String, java.lang.Integer> pushDataCount__ =
                  input.readMessage(
                      PushDataCountDefaultEntryHolder.defaultEntry.getParserForType(),
                      extensionRegistry);
              pushDataCount_
                  .getMutableMap()
                  .put(pushDataCount__.getKey(), pushDataCount__.getValue());
              break;
            }
          default:
            {
              if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                done = true;
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
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }

  public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
    return com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPbOuterClass
        .internal_static_MultiSegmentDataPb_descriptor;
  }

  @SuppressWarnings({"rawtypes"})
  @java.lang.Override
  protected com.google.protobuf.MapField internalGetMapField(int number) {
    switch (number) {
      case 3:
        return internalGetUnzipData();
      case 6:
        return internalGetPushDataCount();
      default:
        throw new RuntimeException("Invalid map field number: " + number);
    }
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPbOuterClass
        .internal_static_MultiSegmentDataPb_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb.class,
            com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb.Builder.class);
  }

  public static final int SEGMENT_FIELD_NUMBER = 1;
  private volatile java.lang.Object segment_;
  /**
   * <code>string segment = 1;</code>
   *
   * @return The segment.
   */
  @java.lang.Override
  public java.lang.String getSegment() {
    java.lang.Object ref = segment_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      segment_ = s;
      return s;
    }
  }
  /**
   * <code>string segment = 1;</code>
   *
   * @return The bytes for segment.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString getSegmentBytes() {
    java.lang.Object ref = segment_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b =
          com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
      segment_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int ZIPDATA_FIELD_NUMBER = 2;
  private com.google.protobuf.ByteString zipData_;
  /**
   * <code>bytes zipData = 2;</code>
   *
   * @return The zipData.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString getZipData() {
    return zipData_;
  }

  public static final int UNZIPDATA_FIELD_NUMBER = 3;

  private static final class UnzipDataDefaultEntryHolder {
    static final com.google.protobuf.MapEntry<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
        defaultEntry =
            com.google.protobuf.MapEntry
                .<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
                    newDefaultInstance(
                        com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPbOuterClass
                            .internal_static_MultiSegmentDataPb_UnzipDataEntry_descriptor,
                        com.google.protobuf.WireFormat.FieldType.STRING,
                        "",
                        com.google.protobuf.WireFormat.FieldType.MESSAGE,
                        com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb
                            .getDefaultInstance());
  }

  private com.google.protobuf.MapField<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
      unzipData_;

  private com.google.protobuf.MapField<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
      internalGetUnzipData() {
    if (unzipData_ == null) {
      return com.google.protobuf.MapField.emptyMapField(UnzipDataDefaultEntryHolder.defaultEntry);
    }
    return unzipData_;
  }

  public int getUnzipDataCount() {
    return internalGetUnzipData().getMap().size();
  }
  /** <code>map&lt;string, .DataBoxesPb&gt; unzipData = 3;</code> */
  @java.lang.Override
  public boolean containsUnzipData(java.lang.String key) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    return internalGetUnzipData().getMap().containsKey(key);
  }
  /** Use {@link #getUnzipDataMap()} instead. */
  @java.lang.Override
  @java.lang.Deprecated
  public java.util.Map<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
      getUnzipData() {
    return getUnzipDataMap();
  }
  /** <code>map&lt;string, .DataBoxesPb&gt; unzipData = 3;</code> */
  @java.lang.Override
  public java.util.Map<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
      getUnzipDataMap() {
    return internalGetUnzipData().getMap();
  }
  /** <code>map&lt;string, .DataBoxesPb&gt; unzipData = 3;</code> */
  @java.lang.Override
  public com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb getUnzipDataOrDefault(
      java.lang.String key,
      com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb defaultValue) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
        map = internalGetUnzipData().getMap();
    return map.containsKey(key) ? map.get(key) : defaultValue;
  }
  /** <code>map&lt;string, .DataBoxesPb&gt; unzipData = 3;</code> */
  @java.lang.Override
  public com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb getUnzipDataOrThrow(
      java.lang.String key) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
        map = internalGetUnzipData().getMap();
    if (!map.containsKey(key)) {
      throw new java.lang.IllegalArgumentException();
    }
    return map.get(key);
  }

  public static final int ENCODING_FIELD_NUMBER = 4;
  private volatile java.lang.Object encoding_;
  /**
   * <code>string encoding = 4;</code>
   *
   * @return The encoding.
   */
  @java.lang.Override
  public java.lang.String getEncoding() {
    java.lang.Object ref = encoding_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      encoding_ = s;
      return s;
    }
  }
  /**
   * <code>string encoding = 4;</code>
   *
   * @return The bytes for encoding.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString getEncodingBytes() {
    java.lang.Object ref = encoding_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b =
          com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
      encoding_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int VERSION_FIELD_NUMBER = 5;
  private long version_;
  /**
   * <code>int64 version = 5;</code>
   *
   * @return The version.
   */
  @java.lang.Override
  public long getVersion() {
    return version_;
  }

  public static final int PUSHDATACOUNT_FIELD_NUMBER = 6;

  private static final class PushDataCountDefaultEntryHolder {
    static final com.google.protobuf.MapEntry<java.lang.String, java.lang.Integer> defaultEntry =
        com.google.protobuf.MapEntry.<java.lang.String, java.lang.Integer>newDefaultInstance(
            com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPbOuterClass
                .internal_static_MultiSegmentDataPb_PushDataCountEntry_descriptor,
            com.google.protobuf.WireFormat.FieldType.STRING,
            "",
            com.google.protobuf.WireFormat.FieldType.INT32,
            0);
  }

  private com.google.protobuf.MapField<java.lang.String, java.lang.Integer> pushDataCount_;

  private com.google.protobuf.MapField<java.lang.String, java.lang.Integer>
      internalGetPushDataCount() {
    if (pushDataCount_ == null) {
      return com.google.protobuf.MapField.emptyMapField(
          PushDataCountDefaultEntryHolder.defaultEntry);
    }
    return pushDataCount_;
  }

  public int getPushDataCountCount() {
    return internalGetPushDataCount().getMap().size();
  }
  /** <code>map&lt;string, int32&gt; pushDataCount = 6;</code> */
  @java.lang.Override
  public boolean containsPushDataCount(java.lang.String key) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    return internalGetPushDataCount().getMap().containsKey(key);
  }
  /** Use {@link #getPushDataCountMap()} instead. */
  @java.lang.Override
  @java.lang.Deprecated
  public java.util.Map<java.lang.String, java.lang.Integer> getPushDataCount() {
    return getPushDataCountMap();
  }
  /** <code>map&lt;string, int32&gt; pushDataCount = 6;</code> */
  @java.lang.Override
  public java.util.Map<java.lang.String, java.lang.Integer> getPushDataCountMap() {
    return internalGetPushDataCount().getMap();
  }
  /** <code>map&lt;string, int32&gt; pushDataCount = 6;</code> */
  @java.lang.Override
  public int getPushDataCountOrDefault(java.lang.String key, int defaultValue) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    java.util.Map<java.lang.String, java.lang.Integer> map = internalGetPushDataCount().getMap();
    return map.containsKey(key) ? map.get(key) : defaultValue;
  }
  /** <code>map&lt;string, int32&gt; pushDataCount = 6;</code> */
  @java.lang.Override
  public int getPushDataCountOrThrow(java.lang.String key) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    java.util.Map<java.lang.String, java.lang.Integer> map = internalGetPushDataCount().getMap();
    if (!map.containsKey(key)) {
      throw new java.lang.IllegalArgumentException();
    }
    return map.get(key);
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
  public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
    if (!getSegmentBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, segment_);
    }
    if (!zipData_.isEmpty()) {
      output.writeBytes(2, zipData_);
    }
    com.google.protobuf.GeneratedMessageV3.serializeStringMapTo(
        output, internalGetUnzipData(), UnzipDataDefaultEntryHolder.defaultEntry, 3);
    if (!getEncodingBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 4, encoding_);
    }
    if (version_ != 0L) {
      output.writeInt64(5, version_);
    }
    com.google.protobuf.GeneratedMessageV3.serializeStringMapTo(
        output, internalGetPushDataCount(), PushDataCountDefaultEntryHolder.defaultEntry, 6);
    unknownFields.writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!getSegmentBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, segment_);
    }
    if (!zipData_.isEmpty()) {
      size += com.google.protobuf.CodedOutputStream.computeBytesSize(2, zipData_);
    }
    for (java.util.Map.Entry<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
        entry : internalGetUnzipData().getMap().entrySet()) {
      com.google.protobuf.MapEntry<
              java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
          unzipData__ =
              UnzipDataDefaultEntryHolder.defaultEntry
                  .newBuilderForType()
                  .setKey(entry.getKey())
                  .setValue(entry.getValue())
                  .build();
      size += com.google.protobuf.CodedOutputStream.computeMessageSize(3, unzipData__);
    }
    if (!getEncodingBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(4, encoding_);
    }
    if (version_ != 0L) {
      size += com.google.protobuf.CodedOutputStream.computeInt64Size(5, version_);
    }
    for (java.util.Map.Entry<java.lang.String, java.lang.Integer> entry :
        internalGetPushDataCount().getMap().entrySet()) {
      com.google.protobuf.MapEntry<java.lang.String, java.lang.Integer> pushDataCount__ =
          PushDataCountDefaultEntryHolder.defaultEntry
              .newBuilderForType()
              .setKey(entry.getKey())
              .setValue(entry.getValue())
              .build();
      size += com.google.protobuf.CodedOutputStream.computeMessageSize(6, pushDataCount__);
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
    if (!(obj instanceof com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb)) {
      return super.equals(obj);
    }
    com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb other =
        (com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb) obj;

    if (!getSegment().equals(other.getSegment())) return false;
    if (!getZipData().equals(other.getZipData())) return false;
    if (!internalGetUnzipData().equals(other.internalGetUnzipData())) return false;
    if (!getEncoding().equals(other.getEncoding())) return false;
    if (getVersion() != other.getVersion()) return false;
    if (!internalGetPushDataCount().equals(other.internalGetPushDataCount())) return false;
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
    hash = (37 * hash) + SEGMENT_FIELD_NUMBER;
    hash = (53 * hash) + getSegment().hashCode();
    hash = (37 * hash) + ZIPDATA_FIELD_NUMBER;
    hash = (53 * hash) + getZipData().hashCode();
    if (!internalGetUnzipData().getMap().isEmpty()) {
      hash = (37 * hash) + UNZIPDATA_FIELD_NUMBER;
      hash = (53 * hash) + internalGetUnzipData().hashCode();
    }
    hash = (37 * hash) + ENCODING_FIELD_NUMBER;
    hash = (53 * hash) + getEncoding().hashCode();
    hash = (37 * hash) + VERSION_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(getVersion());
    if (!internalGetPushDataCount().getMap().isEmpty()) {
      hash = (37 * hash) + PUSHDATACOUNT_FIELD_NUMBER;
      hash = (53 * hash) + internalGetPushDataCount().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb parseFrom(
      java.nio.ByteBuffer data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb parseFrom(
      java.nio.ByteBuffer data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb parseFrom(
      byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb parseFrom(
      byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb parseFrom(
      java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb parseFrom(
      java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb
      parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb
      parseDelimitedFrom(
          java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb parseFrom(
      com.google.protobuf.CodedInputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(
        PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() {
    return newBuilder();
  }

  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }

  public static Builder newBuilder(
      com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }

  @java.lang.Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /** Protobuf type {@code MultiSegmentDataPb} */
  public static final class Builder extends com.google.protobuf.GeneratedMessageV3.Builder<Builder>
      implements
      // @@protoc_insertion_point(builder_implements:MultiSegmentDataPb)
      com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPbOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
      return com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPbOuterClass
          .internal_static_MultiSegmentDataPb_descriptor;
    }

    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapField internalGetMapField(int number) {
      switch (number) {
        case 3:
          return internalGetUnzipData();
        case 6:
          return internalGetPushDataCount();
        default:
          throw new RuntimeException("Invalid map field number: " + number);
      }
    }

    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapField internalGetMutableMapField(int number) {
      switch (number) {
        case 3:
          return internalGetMutableUnzipData();
        case 6:
          return internalGetMutablePushDataCount();
        default:
          throw new RuntimeException("Invalid map field number: " + number);
      }
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPbOuterClass
          .internal_static_MultiSegmentDataPb_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb.class,
              com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb.Builder.class);
    }

    // Construct using
    // com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb.newBuilder()
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

    @java.lang.Override
    public Builder clear() {
      super.clear();
      segment_ = "";

      zipData_ = com.google.protobuf.ByteString.EMPTY;

      internalGetMutableUnzipData().clear();
      encoding_ = "";

      version_ = 0L;

      internalGetMutablePushDataCount().clear();
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
      return com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPbOuterClass
          .internal_static_MultiSegmentDataPb_descriptor;
    }

    @java.lang.Override
    public com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb
        getDefaultInstanceForType() {
      return com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb
          .getDefaultInstance();
    }

    @java.lang.Override
    public com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb build() {
      com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb buildPartial() {
      com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb result =
          new com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb(this);
      int from_bitField0_ = bitField0_;
      result.segment_ = segment_;
      result.zipData_ = zipData_;
      result.unzipData_ = internalGetUnzipData();
      result.unzipData_.makeImmutable();
      result.encoding_ = encoding_;
      result.version_ = version_;
      result.pushDataCount_ = internalGetPushDataCount();
      result.pushDataCount_.makeImmutable();
      onBuilt();
      return result;
    }

    @java.lang.Override
    public Builder clone() {
      return super.clone();
    }

    @java.lang.Override
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field, java.lang.Object value) {
      return super.setField(field, value);
    }

    @java.lang.Override
    public Builder clearField(com.google.protobuf.Descriptors.FieldDescriptor field) {
      return super.clearField(field);
    }

    @java.lang.Override
    public Builder clearOneof(com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return super.clearOneof(oneof);
    }

    @java.lang.Override
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field, int index, java.lang.Object value) {
      return super.setRepeatedField(field, index, value);
    }

    @java.lang.Override
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field, java.lang.Object value) {
      return super.addRepeatedField(field, value);
    }

    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb) {
        return mergeFrom(
            (com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb) other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(
        com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb other) {
      if (other
          == com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb
              .getDefaultInstance()) return this;
      if (!other.getSegment().isEmpty()) {
        segment_ = other.segment_;
        onChanged();
      }
      if (other.getZipData() != com.google.protobuf.ByteString.EMPTY) {
        setZipData(other.getZipData());
      }
      internalGetMutableUnzipData().mergeFrom(other.internalGetUnzipData());
      if (!other.getEncoding().isEmpty()) {
        encoding_ = other.encoding_;
        onChanged();
      }
      if (other.getVersion() != 0L) {
        setVersion(other.getVersion());
      }
      internalGetMutablePushDataCount().mergeFrom(other.internalGetPushDataCount());
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
      com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage =
            (com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb)
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

    private java.lang.Object segment_ = "";
    /**
     * <code>string segment = 1;</code>
     *
     * @return The segment.
     */
    public java.lang.String getSegment() {
      java.lang.Object ref = segment_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        segment_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string segment = 1;</code>
     *
     * @return The bytes for segment.
     */
    public com.google.protobuf.ByteString getSegmentBytes() {
      java.lang.Object ref = segment_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
            com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
        segment_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string segment = 1;</code>
     *
     * @param value The segment to set.
     * @return This builder for chaining.
     */
    public Builder setSegment(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      segment_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string segment = 1;</code>
     *
     * @return This builder for chaining.
     */
    public Builder clearSegment() {

      segment_ = getDefaultInstance().getSegment();
      onChanged();
      return this;
    }
    /**
     * <code>string segment = 1;</code>
     *
     * @param value The bytes for segment to set.
     * @return This builder for chaining.
     */
    public Builder setSegmentBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);

      segment_ = value;
      onChanged();
      return this;
    }

    private com.google.protobuf.ByteString zipData_ = com.google.protobuf.ByteString.EMPTY;
    /**
     * <code>bytes zipData = 2;</code>
     *
     * @return The zipData.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getZipData() {
      return zipData_;
    }
    /**
     * <code>bytes zipData = 2;</code>
     *
     * @param value The zipData to set.
     * @return This builder for chaining.
     */
    public Builder setZipData(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }

      zipData_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>bytes zipData = 2;</code>
     *
     * @return This builder for chaining.
     */
    public Builder clearZipData() {

      zipData_ = getDefaultInstance().getZipData();
      onChanged();
      return this;
    }

    private com.google.protobuf.MapField<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
        unzipData_;

    private com.google.protobuf.MapField<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
        internalGetUnzipData() {
      if (unzipData_ == null) {
        return com.google.protobuf.MapField.emptyMapField(UnzipDataDefaultEntryHolder.defaultEntry);
      }
      return unzipData_;
    }

    private com.google.protobuf.MapField<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
        internalGetMutableUnzipData() {
      onChanged();
      ;
      if (unzipData_ == null) {
        unzipData_ =
            com.google.protobuf.MapField.newMapField(UnzipDataDefaultEntryHolder.defaultEntry);
      }
      if (!unzipData_.isMutable()) {
        unzipData_ = unzipData_.copy();
      }
      return unzipData_;
    }

    public int getUnzipDataCount() {
      return internalGetUnzipData().getMap().size();
    }
    /** <code>map&lt;string, .DataBoxesPb&gt; unzipData = 3;</code> */
    @java.lang.Override
    public boolean containsUnzipData(java.lang.String key) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      return internalGetUnzipData().getMap().containsKey(key);
    }
    /** Use {@link #getUnzipDataMap()} instead. */
    @java.lang.Override
    @java.lang.Deprecated
    public java.util.Map<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
        getUnzipData() {
      return getUnzipDataMap();
    }
    /** <code>map&lt;string, .DataBoxesPb&gt; unzipData = 3;</code> */
    @java.lang.Override
    public java.util.Map<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
        getUnzipDataMap() {
      return internalGetUnzipData().getMap();
    }
    /** <code>map&lt;string, .DataBoxesPb&gt; unzipData = 3;</code> */
    @java.lang.Override
    public com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb getUnzipDataOrDefault(
        java.lang.String key,
        com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb defaultValue) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
          map = internalGetUnzipData().getMap();
      return map.containsKey(key) ? map.get(key) : defaultValue;
    }
    /** <code>map&lt;string, .DataBoxesPb&gt; unzipData = 3;</code> */
    @java.lang.Override
    public com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb getUnzipDataOrThrow(
        java.lang.String key) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
          map = internalGetUnzipData().getMap();
      if (!map.containsKey(key)) {
        throw new java.lang.IllegalArgumentException();
      }
      return map.get(key);
    }

    public Builder clearUnzipData() {
      internalGetMutableUnzipData().getMutableMap().clear();
      return this;
    }
    /** <code>map&lt;string, .DataBoxesPb&gt; unzipData = 3;</code> */
    public Builder removeUnzipData(java.lang.String key) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      internalGetMutableUnzipData().getMutableMap().remove(key);
      return this;
    }
    /** Use alternate mutation accessors instead. */
    @java.lang.Deprecated
    public java.util.Map<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
        getMutableUnzipData() {
      return internalGetMutableUnzipData().getMutableMap();
    }
    /** <code>map&lt;string, .DataBoxesPb&gt; unzipData = 3;</code> */
    public Builder putUnzipData(
        java.lang.String key, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb value) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      if (value == null) {
        throw new java.lang.NullPointerException();
      }
      internalGetMutableUnzipData().getMutableMap().put(key, value);
      return this;
    }
    /** <code>map&lt;string, .DataBoxesPb&gt; unzipData = 3;</code> */
    public Builder putAllUnzipData(
        java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
            values) {
      internalGetMutableUnzipData().getMutableMap().putAll(values);
      return this;
    }

    private java.lang.Object encoding_ = "";
    /**
     * <code>string encoding = 4;</code>
     *
     * @return The encoding.
     */
    public java.lang.String getEncoding() {
      java.lang.Object ref = encoding_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        encoding_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string encoding = 4;</code>
     *
     * @return The bytes for encoding.
     */
    public com.google.protobuf.ByteString getEncodingBytes() {
      java.lang.Object ref = encoding_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
            com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
        encoding_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string encoding = 4;</code>
     *
     * @param value The encoding to set.
     * @return This builder for chaining.
     */
    public Builder setEncoding(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      encoding_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string encoding = 4;</code>
     *
     * @return This builder for chaining.
     */
    public Builder clearEncoding() {

      encoding_ = getDefaultInstance().getEncoding();
      onChanged();
      return this;
    }
    /**
     * <code>string encoding = 4;</code>
     *
     * @param value The bytes for encoding to set.
     * @return This builder for chaining.
     */
    public Builder setEncodingBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);

      encoding_ = value;
      onChanged();
      return this;
    }

    private long version_;
    /**
     * <code>int64 version = 5;</code>
     *
     * @return The version.
     */
    @java.lang.Override
    public long getVersion() {
      return version_;
    }
    /**
     * <code>int64 version = 5;</code>
     *
     * @param value The version to set.
     * @return This builder for chaining.
     */
    public Builder setVersion(long value) {

      version_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>int64 version = 5;</code>
     *
     * @return This builder for chaining.
     */
    public Builder clearVersion() {

      version_ = 0L;
      onChanged();
      return this;
    }

    private com.google.protobuf.MapField<java.lang.String, java.lang.Integer> pushDataCount_;

    private com.google.protobuf.MapField<java.lang.String, java.lang.Integer>
        internalGetPushDataCount() {
      if (pushDataCount_ == null) {
        return com.google.protobuf.MapField.emptyMapField(
            PushDataCountDefaultEntryHolder.defaultEntry);
      }
      return pushDataCount_;
    }

    private com.google.protobuf.MapField<java.lang.String, java.lang.Integer>
        internalGetMutablePushDataCount() {
      onChanged();
      ;
      if (pushDataCount_ == null) {
        pushDataCount_ =
            com.google.protobuf.MapField.newMapField(PushDataCountDefaultEntryHolder.defaultEntry);
      }
      if (!pushDataCount_.isMutable()) {
        pushDataCount_ = pushDataCount_.copy();
      }
      return pushDataCount_;
    }

    public int getPushDataCountCount() {
      return internalGetPushDataCount().getMap().size();
    }
    /** <code>map&lt;string, int32&gt; pushDataCount = 6;</code> */
    @java.lang.Override
    public boolean containsPushDataCount(java.lang.String key) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      return internalGetPushDataCount().getMap().containsKey(key);
    }
    /** Use {@link #getPushDataCountMap()} instead. */
    @java.lang.Override
    @java.lang.Deprecated
    public java.util.Map<java.lang.String, java.lang.Integer> getPushDataCount() {
      return getPushDataCountMap();
    }
    /** <code>map&lt;string, int32&gt; pushDataCount = 6;</code> */
    @java.lang.Override
    public java.util.Map<java.lang.String, java.lang.Integer> getPushDataCountMap() {
      return internalGetPushDataCount().getMap();
    }
    /** <code>map&lt;string, int32&gt; pushDataCount = 6;</code> */
    @java.lang.Override
    public int getPushDataCountOrDefault(java.lang.String key, int defaultValue) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      java.util.Map<java.lang.String, java.lang.Integer> map = internalGetPushDataCount().getMap();
      return map.containsKey(key) ? map.get(key) : defaultValue;
    }
    /** <code>map&lt;string, int32&gt; pushDataCount = 6;</code> */
    @java.lang.Override
    public int getPushDataCountOrThrow(java.lang.String key) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      java.util.Map<java.lang.String, java.lang.Integer> map = internalGetPushDataCount().getMap();
      if (!map.containsKey(key)) {
        throw new java.lang.IllegalArgumentException();
      }
      return map.get(key);
    }

    public Builder clearPushDataCount() {
      internalGetMutablePushDataCount().getMutableMap().clear();
      return this;
    }
    /** <code>map&lt;string, int32&gt; pushDataCount = 6;</code> */
    public Builder removePushDataCount(java.lang.String key) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      internalGetMutablePushDataCount().getMutableMap().remove(key);
      return this;
    }
    /** Use alternate mutation accessors instead. */
    @java.lang.Deprecated
    public java.util.Map<java.lang.String, java.lang.Integer> getMutablePushDataCount() {
      return internalGetMutablePushDataCount().getMutableMap();
    }
    /** <code>map&lt;string, int32&gt; pushDataCount = 6;</code> */
    public Builder putPushDataCount(java.lang.String key, int value) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }

      internalGetMutablePushDataCount().getMutableMap().put(key, value);
      return this;
    }
    /** <code>map&lt;string, int32&gt; pushDataCount = 6;</code> */
    public Builder putAllPushDataCount(java.util.Map<java.lang.String, java.lang.Integer> values) {
      internalGetMutablePushDataCount().getMutableMap().putAll(values);
      return this;
    }

    @java.lang.Override
    public final Builder setUnknownFields(final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    @java.lang.Override
    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }

    // @@protoc_insertion_point(builder_scope:MultiSegmentDataPb)
  }

  // @@protoc_insertion_point(class_scope:MultiSegmentDataPb)
  private static final com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb
      DEFAULT_INSTANCE;

  static {
    DEFAULT_INSTANCE = new com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb();
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb
      getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<MultiSegmentDataPb> PARSER =
      new com.google.protobuf.AbstractParser<MultiSegmentDataPb>() {
        @java.lang.Override
        public MultiSegmentDataPb parsePartialFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
          return new MultiSegmentDataPb(input, extensionRegistry);
        }
      };

  public static com.google.protobuf.Parser<MultiSegmentDataPb> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<MultiSegmentDataPb> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb
      getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }
}
