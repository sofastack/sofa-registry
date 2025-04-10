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

/** Protobuf type {@code DeltaReceivedDataPb} */
public final class DeltaReceivedDataPb extends com.google.protobuf.GeneratedMessageV3
    implements
    // @@protoc_insertion_point(message_implements:DeltaReceivedDataPb)
    DeltaReceivedDataPbOrBuilder {
  private static final long serialVersionUID = 0L;
  // Use DeltaReceivedDataPb.newBuilder() to construct.
  private DeltaReceivedDataPb(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }

  private DeltaReceivedDataPb() {
    dataId_ = "";
    group_ = "";
    instanceId_ = "";
    segment_ = "";
    scope_ = "";
    subscriberRegisterIds_ = com.google.protobuf.LazyStringArrayList.EMPTY;
    version_ = 0L;
    localZone_ = "";
    isDelta_ = false;
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
    return this.unknownFields;
  }

  private DeltaReceivedDataPb(
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

              segment_ = s;
              break;
            }
          case 42:
            {
              java.lang.String s = input.readStringRequireUtf8();

              scope_ = s;
              break;
            }
          case 50:
            {
              java.lang.String s = input.readStringRequireUtf8();
              if (!((mutable_bitField0_ & 0x00000020) == 0x00000020)) {
                subscriberRegisterIds_ = new com.google.protobuf.LazyStringArrayList();
                mutable_bitField0_ |= 0x00000020;
              }
              subscriberRegisterIds_.add(s);
              break;
            }
          case 56:
            {
              version_ = input.readInt64();
              break;
            }
          case 66:
            {
              java.lang.String s = input.readStringRequireUtf8();

              localZone_ = s;
              break;
            }
          case 72:
            {
              isDelta_ = input.readBool();
              break;
            }
          case 82:
            {
              com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb.Builder
                  subBuilder = null;
              if (deltaBody_ != null) {
                subBuilder = deltaBody_.toBuilder();
              }
              deltaBody_ =
                  input.readMessage(
                      com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb
                          .parser(),
                      extensionRegistry);
              if (subBuilder != null) {
                subBuilder.mergeFrom(deltaBody_);
                deltaBody_ = subBuilder.buildPartial();
              }

              break;
            }
          case 90:
            {
              com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb.Builder
                  subBuilder = null;
              if (fullBody_ != null) {
                subBuilder = fullBody_.toBuilder();
              }
              fullBody_ =
                  input.readMessage(
                      com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb
                          .parser(),
                      extensionRegistry);
              if (subBuilder != null) {
                subBuilder.mergeFrom(fullBody_);
                fullBody_ = subBuilder.buildPartial();
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
      if (((mutable_bitField0_ & 0x00000020) == 0x00000020)) {
        subscriberRegisterIds_ = subscriberRegisterIds_.getUnmodifiableView();
      }
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }

  public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
    return com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPbOuterClass
        .internal_static_DeltaReceivedDataPb_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPbOuterClass
        .internal_static_DeltaReceivedDataPb_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPb.class,
            com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPb.Builder.class);
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

  public static final int SEGMENT_FIELD_NUMBER = 4;
  private volatile java.lang.Object segment_;
  /** <code>string segment = 4;</code> */
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
  /** <code>string segment = 4;</code> */
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

  public static final int SCOPE_FIELD_NUMBER = 5;
  private volatile java.lang.Object scope_;
  /** <code>string scope = 5;</code> */
  public java.lang.String getScope() {
    java.lang.Object ref = scope_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      scope_ = s;
      return s;
    }
  }
  /** <code>string scope = 5;</code> */
  public com.google.protobuf.ByteString getScopeBytes() {
    java.lang.Object ref = scope_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b =
          com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
      scope_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int SUBSCRIBERREGISTERIDS_FIELD_NUMBER = 6;
  private com.google.protobuf.LazyStringList subscriberRegisterIds_;
  /** <code>repeated string subscriberRegisterIds = 6;</code> */
  public com.google.protobuf.ProtocolStringList getSubscriberRegisterIdsList() {
    return subscriberRegisterIds_;
  }
  /** <code>repeated string subscriberRegisterIds = 6;</code> */
  public int getSubscriberRegisterIdsCount() {
    return subscriberRegisterIds_.size();
  }
  /** <code>repeated string subscriberRegisterIds = 6;</code> */
  public java.lang.String getSubscriberRegisterIds(int index) {
    return subscriberRegisterIds_.get(index);
  }
  /** <code>repeated string subscriberRegisterIds = 6;</code> */
  public com.google.protobuf.ByteString getSubscriberRegisterIdsBytes(int index) {
    return subscriberRegisterIds_.getByteString(index);
  }

  public static final int VERSION_FIELD_NUMBER = 7;
  private long version_;
  /** <code>int64 version = 7;</code> */
  public long getVersion() {
    return version_;
  }

  public static final int LOCALZONE_FIELD_NUMBER = 8;
  private volatile java.lang.Object localZone_;
  /** <code>string localZone = 8;</code> */
  public java.lang.String getLocalZone() {
    java.lang.Object ref = localZone_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      localZone_ = s;
      return s;
    }
  }
  /** <code>string localZone = 8;</code> */
  public com.google.protobuf.ByteString getLocalZoneBytes() {
    java.lang.Object ref = localZone_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b =
          com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
      localZone_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int ISDELTA_FIELD_NUMBER = 9;
  private boolean isDelta_;
  /** <code>bool isDelta = 9;</code> */
  public boolean getIsDelta() {
    return isDelta_;
  }

  public static final int DELTABODY_FIELD_NUMBER = 10;
  private com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb deltaBody_;
  /** <code>.DeltaReceivedDataBodyPb deltaBody = 10;</code> */
  public boolean hasDeltaBody() {
    return deltaBody_ != null;
  }
  /** <code>.DeltaReceivedDataBodyPb deltaBody = 10;</code> */
  public com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb getDeltaBody() {
    return deltaBody_ == null
        ? com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb
            .getDefaultInstance()
        : deltaBody_;
  }
  /** <code>.DeltaReceivedDataBodyPb deltaBody = 10;</code> */
  public com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPbOrBuilder
      getDeltaBodyOrBuilder() {
    return getDeltaBody();
  }

  public static final int FULLBODY_FIELD_NUMBER = 11;
  private com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb fullBody_;
  /** <code>.FullReceivedDataBodyPb fullBody = 11;</code> */
  public boolean hasFullBody() {
    return fullBody_ != null;
  }
  /** <code>.FullReceivedDataBodyPb fullBody = 11;</code> */
  public com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb getFullBody() {
    return fullBody_ == null
        ? com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb
            .getDefaultInstance()
        : fullBody_;
  }
  /** <code>.FullReceivedDataBodyPb fullBody = 11;</code> */
  public com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPbOrBuilder
      getFullBodyOrBuilder() {
    return getFullBody();
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
    if (!getSegmentBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 4, segment_);
    }
    if (!getScopeBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 5, scope_);
    }
    for (int i = 0; i < subscriberRegisterIds_.size(); i++) {
      com.google.protobuf.GeneratedMessageV3.writeString(
          output, 6, subscriberRegisterIds_.getRaw(i));
    }
    if (version_ != 0L) {
      output.writeInt64(7, version_);
    }
    if (!getLocalZoneBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 8, localZone_);
    }
    if (isDelta_ != false) {
      output.writeBool(9, isDelta_);
    }
    if (deltaBody_ != null) {
      output.writeMessage(10, getDeltaBody());
    }
    if (fullBody_ != null) {
      output.writeMessage(11, getFullBody());
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
    if (!getSegmentBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(4, segment_);
    }
    if (!getScopeBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(5, scope_);
    }
    {
      int dataSize = 0;
      for (int i = 0; i < subscriberRegisterIds_.size(); i++) {
        dataSize += computeStringSizeNoTag(subscriberRegisterIds_.getRaw(i));
      }
      size += dataSize;
      size += 1 * getSubscriberRegisterIdsList().size();
    }
    if (version_ != 0L) {
      size += com.google.protobuf.CodedOutputStream.computeInt64Size(7, version_);
    }
    if (!getLocalZoneBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(8, localZone_);
    }
    if (isDelta_ != false) {
      size += com.google.protobuf.CodedOutputStream.computeBoolSize(9, isDelta_);
    }
    if (deltaBody_ != null) {
      size += com.google.protobuf.CodedOutputStream.computeMessageSize(10, getDeltaBody());
    }
    if (fullBody_ != null) {
      size += com.google.protobuf.CodedOutputStream.computeMessageSize(11, getFullBody());
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
    if (!(obj instanceof com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPb)) {
      return super.equals(obj);
    }
    com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPb other =
        (com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPb) obj;

    boolean result = true;
    result = result && getDataId().equals(other.getDataId());
    result = result && getGroup().equals(other.getGroup());
    result = result && getInstanceId().equals(other.getInstanceId());
    result = result && getSegment().equals(other.getSegment());
    result = result && getScope().equals(other.getScope());
    result = result && getSubscriberRegisterIdsList().equals(other.getSubscriberRegisterIdsList());
    result = result && (getVersion() == other.getVersion());
    result = result && getLocalZone().equals(other.getLocalZone());
    result = result && (getIsDelta() == other.getIsDelta());
    result = result && (hasDeltaBody() == other.hasDeltaBody());
    if (hasDeltaBody()) {
      result = result && getDeltaBody().equals(other.getDeltaBody());
    }
    result = result && (hasFullBody() == other.hasFullBody());
    if (hasFullBody()) {
      result = result && getFullBody().equals(other.getFullBody());
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
    hash = (37 * hash) + DATAID_FIELD_NUMBER;
    hash = (53 * hash) + getDataId().hashCode();
    hash = (37 * hash) + GROUP_FIELD_NUMBER;
    hash = (53 * hash) + getGroup().hashCode();
    hash = (37 * hash) + INSTANCEID_FIELD_NUMBER;
    hash = (53 * hash) + getInstanceId().hashCode();
    hash = (37 * hash) + SEGMENT_FIELD_NUMBER;
    hash = (53 * hash) + getSegment().hashCode();
    hash = (37 * hash) + SCOPE_FIELD_NUMBER;
    hash = (53 * hash) + getScope().hashCode();
    if (getSubscriberRegisterIdsCount() > 0) {
      hash = (37 * hash) + SUBSCRIBERREGISTERIDS_FIELD_NUMBER;
      hash = (53 * hash) + getSubscriberRegisterIdsList().hashCode();
    }
    hash = (37 * hash) + VERSION_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(getVersion());
    hash = (37 * hash) + LOCALZONE_FIELD_NUMBER;
    hash = (53 * hash) + getLocalZone().hashCode();
    hash = (37 * hash) + ISDELTA_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashBoolean(getIsDelta());
    if (hasDeltaBody()) {
      hash = (37 * hash) + DELTABODY_FIELD_NUMBER;
      hash = (53 * hash) + getDeltaBody().hashCode();
    }
    if (hasFullBody()) {
      hash = (37 * hash) + FULLBODY_FIELD_NUMBER;
      hash = (53 * hash) + getFullBody().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPb parseFrom(
      java.nio.ByteBuffer data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPb parseFrom(
      java.nio.ByteBuffer data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPb parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPb parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPb parseFrom(
      byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPb parseFrom(
      byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPb parseFrom(
      java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPb parseFrom(
      java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPb
      parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPb
      parseDelimitedFrom(
          java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPb parseFrom(
      com.google.protobuf.CodedInputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPb parseFrom(
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
      com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPb prototype) {
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
  /** Protobuf type {@code DeltaReceivedDataPb} */
  public static final class Builder extends com.google.protobuf.GeneratedMessageV3.Builder<Builder>
      implements
      // @@protoc_insertion_point(builder_implements:DeltaReceivedDataPb)
      com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPbOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
      return com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPbOuterClass
          .internal_static_DeltaReceivedDataPb_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPbOuterClass
          .internal_static_DeltaReceivedDataPb_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPb.class,
              com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPb.Builder.class);
    }

    // Construct using
    // com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPb.newBuilder()
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

      segment_ = "";

      scope_ = "";

      subscriberRegisterIds_ = com.google.protobuf.LazyStringArrayList.EMPTY;
      bitField0_ = (bitField0_ & ~0x00000020);
      version_ = 0L;

      localZone_ = "";

      isDelta_ = false;

      if (deltaBodyBuilder_ == null) {
        deltaBody_ = null;
      } else {
        deltaBody_ = null;
        deltaBodyBuilder_ = null;
      }
      if (fullBodyBuilder_ == null) {
        fullBody_ = null;
      } else {
        fullBody_ = null;
        fullBodyBuilder_ = null;
      }
      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
      return com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPbOuterClass
          .internal_static_DeltaReceivedDataPb_descriptor;
    }

    public com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPb
        getDefaultInstanceForType() {
      return com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPb
          .getDefaultInstance();
    }

    public com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPb build() {
      com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPb result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPb buildPartial() {
      com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPb result =
          new com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPb(this);
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      result.dataId_ = dataId_;
      result.group_ = group_;
      result.instanceId_ = instanceId_;
      result.segment_ = segment_;
      result.scope_ = scope_;
      if (((bitField0_ & 0x00000020) == 0x00000020)) {
        subscriberRegisterIds_ = subscriberRegisterIds_.getUnmodifiableView();
        bitField0_ = (bitField0_ & ~0x00000020);
      }
      result.subscriberRegisterIds_ = subscriberRegisterIds_;
      result.version_ = version_;
      result.localZone_ = localZone_;
      result.isDelta_ = isDelta_;
      if (deltaBodyBuilder_ == null) {
        result.deltaBody_ = deltaBody_;
      } else {
        result.deltaBody_ = deltaBodyBuilder_.build();
      }
      if (fullBodyBuilder_ == null) {
        result.fullBody_ = fullBody_;
      } else {
        result.fullBody_ = fullBodyBuilder_.build();
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
      if (other instanceof com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPb) {
        return mergeFrom(
            (com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPb) other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(
        com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPb other) {
      if (other
          == com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPb
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
      if (!other.getSegment().isEmpty()) {
        segment_ = other.segment_;
        onChanged();
      }
      if (!other.getScope().isEmpty()) {
        scope_ = other.scope_;
        onChanged();
      }
      if (!other.subscriberRegisterIds_.isEmpty()) {
        if (subscriberRegisterIds_.isEmpty()) {
          subscriberRegisterIds_ = other.subscriberRegisterIds_;
          bitField0_ = (bitField0_ & ~0x00000020);
        } else {
          ensureSubscriberRegisterIdsIsMutable();
          subscriberRegisterIds_.addAll(other.subscriberRegisterIds_);
        }
        onChanged();
      }
      if (other.getVersion() != 0L) {
        setVersion(other.getVersion());
      }
      if (!other.getLocalZone().isEmpty()) {
        localZone_ = other.localZone_;
        onChanged();
      }
      if (other.getIsDelta() != false) {
        setIsDelta(other.getIsDelta());
      }
      if (other.hasDeltaBody()) {
        mergeDeltaBody(other.getDeltaBody());
      }
      if (other.hasFullBody()) {
        mergeFullBody(other.getFullBody());
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
      com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPb parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage =
            (com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPb)
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

    private java.lang.Object segment_ = "";
    /** <code>string segment = 4;</code> */
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
    /** <code>string segment = 4;</code> */
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
    /** <code>string segment = 4;</code> */
    public Builder setSegment(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      segment_ = value;
      onChanged();
      return this;
    }
    /** <code>string segment = 4;</code> */
    public Builder clearSegment() {

      segment_ = getDefaultInstance().getSegment();
      onChanged();
      return this;
    }
    /** <code>string segment = 4;</code> */
    public Builder setSegmentBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);

      segment_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object scope_ = "";
    /** <code>string scope = 5;</code> */
    public java.lang.String getScope() {
      java.lang.Object ref = scope_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        scope_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /** <code>string scope = 5;</code> */
    public com.google.protobuf.ByteString getScopeBytes() {
      java.lang.Object ref = scope_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
            com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
        scope_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /** <code>string scope = 5;</code> */
    public Builder setScope(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      scope_ = value;
      onChanged();
      return this;
    }
    /** <code>string scope = 5;</code> */
    public Builder clearScope() {

      scope_ = getDefaultInstance().getScope();
      onChanged();
      return this;
    }
    /** <code>string scope = 5;</code> */
    public Builder setScopeBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);

      scope_ = value;
      onChanged();
      return this;
    }

    private com.google.protobuf.LazyStringList subscriberRegisterIds_ =
        com.google.protobuf.LazyStringArrayList.EMPTY;

    private void ensureSubscriberRegisterIdsIsMutable() {
      if (!((bitField0_ & 0x00000020) == 0x00000020)) {
        subscriberRegisterIds_ =
            new com.google.protobuf.LazyStringArrayList(subscriberRegisterIds_);
        bitField0_ |= 0x00000020;
      }
    }
    /** <code>repeated string subscriberRegisterIds = 6;</code> */
    public com.google.protobuf.ProtocolStringList getSubscriberRegisterIdsList() {
      return subscriberRegisterIds_.getUnmodifiableView();
    }
    /** <code>repeated string subscriberRegisterIds = 6;</code> */
    public int getSubscriberRegisterIdsCount() {
      return subscriberRegisterIds_.size();
    }
    /** <code>repeated string subscriberRegisterIds = 6;</code> */
    public java.lang.String getSubscriberRegisterIds(int index) {
      return subscriberRegisterIds_.get(index);
    }
    /** <code>repeated string subscriberRegisterIds = 6;</code> */
    public com.google.protobuf.ByteString getSubscriberRegisterIdsBytes(int index) {
      return subscriberRegisterIds_.getByteString(index);
    }
    /** <code>repeated string subscriberRegisterIds = 6;</code> */
    public Builder setSubscriberRegisterIds(int index, java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }
      ensureSubscriberRegisterIdsIsMutable();
      subscriberRegisterIds_.set(index, value);
      onChanged();
      return this;
    }
    /** <code>repeated string subscriberRegisterIds = 6;</code> */
    public Builder addSubscriberRegisterIds(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }
      ensureSubscriberRegisterIdsIsMutable();
      subscriberRegisterIds_.add(value);
      onChanged();
      return this;
    }
    /** <code>repeated string subscriberRegisterIds = 6;</code> */
    public Builder addAllSubscriberRegisterIds(java.lang.Iterable<java.lang.String> values) {
      ensureSubscriberRegisterIdsIsMutable();
      com.google.protobuf.AbstractMessageLite.Builder.addAll(values, subscriberRegisterIds_);
      onChanged();
      return this;
    }
    /** <code>repeated string subscriberRegisterIds = 6;</code> */
    public Builder clearSubscriberRegisterIds() {
      subscriberRegisterIds_ = com.google.protobuf.LazyStringArrayList.EMPTY;
      bitField0_ = (bitField0_ & ~0x00000020);
      onChanged();
      return this;
    }
    /** <code>repeated string subscriberRegisterIds = 6;</code> */
    public Builder addSubscriberRegisterIdsBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);
      ensureSubscriberRegisterIdsIsMutable();
      subscriberRegisterIds_.add(value);
      onChanged();
      return this;
    }

    private long version_;
    /** <code>int64 version = 7;</code> */
    public long getVersion() {
      return version_;
    }
    /** <code>int64 version = 7;</code> */
    public Builder setVersion(long value) {

      version_ = value;
      onChanged();
      return this;
    }
    /** <code>int64 version = 7;</code> */
    public Builder clearVersion() {

      version_ = 0L;
      onChanged();
      return this;
    }

    private java.lang.Object localZone_ = "";
    /** <code>string localZone = 8;</code> */
    public java.lang.String getLocalZone() {
      java.lang.Object ref = localZone_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        localZone_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /** <code>string localZone = 8;</code> */
    public com.google.protobuf.ByteString getLocalZoneBytes() {
      java.lang.Object ref = localZone_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
            com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
        localZone_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /** <code>string localZone = 8;</code> */
    public Builder setLocalZone(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      localZone_ = value;
      onChanged();
      return this;
    }
    /** <code>string localZone = 8;</code> */
    public Builder clearLocalZone() {

      localZone_ = getDefaultInstance().getLocalZone();
      onChanged();
      return this;
    }
    /** <code>string localZone = 8;</code> */
    public Builder setLocalZoneBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);

      localZone_ = value;
      onChanged();
      return this;
    }

    private boolean isDelta_;
    /** <code>bool isDelta = 9;</code> */
    public boolean getIsDelta() {
      return isDelta_;
    }
    /** <code>bool isDelta = 9;</code> */
    public Builder setIsDelta(boolean value) {

      isDelta_ = value;
      onChanged();
      return this;
    }
    /** <code>bool isDelta = 9;</code> */
    public Builder clearIsDelta() {

      isDelta_ = false;
      onChanged();
      return this;
    }

    private com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb deltaBody_ =
        null;
    private com.google.protobuf.SingleFieldBuilderV3<
            com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb,
            com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb.Builder,
            com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPbOrBuilder>
        deltaBodyBuilder_;
    /** <code>.DeltaReceivedDataBodyPb deltaBody = 10;</code> */
    public boolean hasDeltaBody() {
      return deltaBodyBuilder_ != null || deltaBody_ != null;
    }
    /** <code>.DeltaReceivedDataBodyPb deltaBody = 10;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb getDeltaBody() {
      if (deltaBodyBuilder_ == null) {
        return deltaBody_ == null
            ? com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb
                .getDefaultInstance()
            : deltaBody_;
      } else {
        return deltaBodyBuilder_.getMessage();
      }
    }
    /** <code>.DeltaReceivedDataBodyPb deltaBody = 10;</code> */
    public Builder setDeltaBody(
        com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb value) {
      if (deltaBodyBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        deltaBody_ = value;
        onChanged();
      } else {
        deltaBodyBuilder_.setMessage(value);
      }

      return this;
    }
    /** <code>.DeltaReceivedDataBodyPb deltaBody = 10;</code> */
    public Builder setDeltaBody(
        com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb.Builder
            builderForValue) {
      if (deltaBodyBuilder_ == null) {
        deltaBody_ = builderForValue.build();
        onChanged();
      } else {
        deltaBodyBuilder_.setMessage(builderForValue.build());
      }

      return this;
    }
    /** <code>.DeltaReceivedDataBodyPb deltaBody = 10;</code> */
    public Builder mergeDeltaBody(
        com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb value) {
      if (deltaBodyBuilder_ == null) {
        if (deltaBody_ != null) {
          deltaBody_ =
              com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb.newBuilder(
                      deltaBody_)
                  .mergeFrom(value)
                  .buildPartial();
        } else {
          deltaBody_ = value;
        }
        onChanged();
      } else {
        deltaBodyBuilder_.mergeFrom(value);
      }

      return this;
    }
    /** <code>.DeltaReceivedDataBodyPb deltaBody = 10;</code> */
    public Builder clearDeltaBody() {
      if (deltaBodyBuilder_ == null) {
        deltaBody_ = null;
        onChanged();
      } else {
        deltaBody_ = null;
        deltaBodyBuilder_ = null;
      }

      return this;
    }
    /** <code>.DeltaReceivedDataBodyPb deltaBody = 10;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb.Builder
        getDeltaBodyBuilder() {

      onChanged();
      return getDeltaBodyFieldBuilder().getBuilder();
    }
    /** <code>.DeltaReceivedDataBodyPb deltaBody = 10;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPbOrBuilder
        getDeltaBodyOrBuilder() {
      if (deltaBodyBuilder_ != null) {
        return deltaBodyBuilder_.getMessageOrBuilder();
      } else {
        return deltaBody_ == null
            ? com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb
                .getDefaultInstance()
            : deltaBody_;
      }
    }
    /** <code>.DeltaReceivedDataBodyPb deltaBody = 10;</code> */
    private com.google.protobuf.SingleFieldBuilderV3<
            com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb,
            com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb.Builder,
            com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPbOrBuilder>
        getDeltaBodyFieldBuilder() {
      if (deltaBodyBuilder_ == null) {
        deltaBodyBuilder_ =
            new com.google.protobuf.SingleFieldBuilderV3<
                com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb,
                com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb.Builder,
                com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPbOrBuilder>(
                getDeltaBody(), getParentForChildren(), isClean());
        deltaBody_ = null;
      }
      return deltaBodyBuilder_;
    }

    private com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb fullBody_ = null;
    private com.google.protobuf.SingleFieldBuilderV3<
            com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb,
            com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb.Builder,
            com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPbOrBuilder>
        fullBodyBuilder_;
    /** <code>.FullReceivedDataBodyPb fullBody = 11;</code> */
    public boolean hasFullBody() {
      return fullBodyBuilder_ != null || fullBody_ != null;
    }
    /** <code>.FullReceivedDataBodyPb fullBody = 11;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb getFullBody() {
      if (fullBodyBuilder_ == null) {
        return fullBody_ == null
            ? com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb
                .getDefaultInstance()
            : fullBody_;
      } else {
        return fullBodyBuilder_.getMessage();
      }
    }
    /** <code>.FullReceivedDataBodyPb fullBody = 11;</code> */
    public Builder setFullBody(
        com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb value) {
      if (fullBodyBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        fullBody_ = value;
        onChanged();
      } else {
        fullBodyBuilder_.setMessage(value);
      }

      return this;
    }
    /** <code>.FullReceivedDataBodyPb fullBody = 11;</code> */
    public Builder setFullBody(
        com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb.Builder
            builderForValue) {
      if (fullBodyBuilder_ == null) {
        fullBody_ = builderForValue.build();
        onChanged();
      } else {
        fullBodyBuilder_.setMessage(builderForValue.build());
      }

      return this;
    }
    /** <code>.FullReceivedDataBodyPb fullBody = 11;</code> */
    public Builder mergeFullBody(
        com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb value) {
      if (fullBodyBuilder_ == null) {
        if (fullBody_ != null) {
          fullBody_ =
              com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb.newBuilder(
                      fullBody_)
                  .mergeFrom(value)
                  .buildPartial();
        } else {
          fullBody_ = value;
        }
        onChanged();
      } else {
        fullBodyBuilder_.mergeFrom(value);
      }

      return this;
    }
    /** <code>.FullReceivedDataBodyPb fullBody = 11;</code> */
    public Builder clearFullBody() {
      if (fullBodyBuilder_ == null) {
        fullBody_ = null;
        onChanged();
      } else {
        fullBody_ = null;
        fullBodyBuilder_ = null;
      }

      return this;
    }
    /** <code>.FullReceivedDataBodyPb fullBody = 11;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb.Builder
        getFullBodyBuilder() {

      onChanged();
      return getFullBodyFieldBuilder().getBuilder();
    }
    /** <code>.FullReceivedDataBodyPb fullBody = 11;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPbOrBuilder
        getFullBodyOrBuilder() {
      if (fullBodyBuilder_ != null) {
        return fullBodyBuilder_.getMessageOrBuilder();
      } else {
        return fullBody_ == null
            ? com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb
                .getDefaultInstance()
            : fullBody_;
      }
    }
    /** <code>.FullReceivedDataBodyPb fullBody = 11;</code> */
    private com.google.protobuf.SingleFieldBuilderV3<
            com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb,
            com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb.Builder,
            com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPbOrBuilder>
        getFullBodyFieldBuilder() {
      if (fullBodyBuilder_ == null) {
        fullBodyBuilder_ =
            new com.google.protobuf.SingleFieldBuilderV3<
                com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb,
                com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb.Builder,
                com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPbOrBuilder>(
                getFullBody(), getParentForChildren(), isClean());
        fullBody_ = null;
      }
      return fullBodyBuilder_;
    }

    public final Builder setUnknownFields(final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFieldsProto3(unknownFields);
    }

    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }

    // @@protoc_insertion_point(builder_scope:DeltaReceivedDataPb)
  }

  // @@protoc_insertion_point(class_scope:DeltaReceivedDataPb)
  private static final com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPb
      DEFAULT_INSTANCE;

  static {
    DEFAULT_INSTANCE = new com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPb();
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPb
      getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<DeltaReceivedDataPb> PARSER =
      new com.google.protobuf.AbstractParser<DeltaReceivedDataPb>() {
        public DeltaReceivedDataPb parsePartialFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
          return new DeltaReceivedDataPb(input, extensionRegistry);
        }
      };

  public static com.google.protobuf.Parser<DeltaReceivedDataPb> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<DeltaReceivedDataPb> getParserForType() {
    return PARSER;
  }

  public com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataPb
      getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }
}
