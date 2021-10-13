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

/** Protobuf type {@code ReceivedDataPb} */
public final class ReceivedDataPb extends com.google.protobuf.GeneratedMessageV3
    implements
    // @@protoc_insertion_point(message_implements:ReceivedDataPb)
    ReceivedDataPbOrBuilder {
  private static final long serialVersionUID = 0L;
  // Use ReceivedDataPb.newBuilder() to construct.
  private ReceivedDataPb(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }

  private ReceivedDataPb() {
    dataId_ = "";
    group_ = "";
    instanceId_ = "";
    segment_ = "";
    scope_ = "";
    subscriberRegistIds_ = com.google.protobuf.LazyStringArrayList.EMPTY;
    version_ = 0L;
    localZone_ = "";
    encoding_ = "";
    body_ = com.google.protobuf.ByteString.EMPTY;
    originBodySize_ = 0;
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
    return this.unknownFields;
  }

  private ReceivedDataPb(
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
                subscriberRegistIds_ = new com.google.protobuf.LazyStringArrayList();
                mutable_bitField0_ |= 0x00000020;
              }
              subscriberRegistIds_.add(s);
              break;
            }
          case 58:
            {
              if (!((mutable_bitField0_ & 0x00000040) == 0x00000040)) {
                data_ =
                    com.google.protobuf.MapField.newMapField(DataDefaultEntryHolder.defaultEntry);
                mutable_bitField0_ |= 0x00000040;
              }
              com.google.protobuf.MapEntry<
                      java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
                  data__ =
                      input.readMessage(
                          DataDefaultEntryHolder.defaultEntry.getParserForType(),
                          extensionRegistry);
              data_.getMutableMap().put(data__.getKey(), data__.getValue());
              break;
            }
          case 64:
            {
              version_ = input.readInt64();
              break;
            }
          case 74:
            {
              java.lang.String s = input.readStringRequireUtf8();

              localZone_ = s;
              break;
            }
          case 82:
            {
              java.lang.String s = input.readStringRequireUtf8();

              encoding_ = s;
              break;
            }
          case 90:
            {
              body_ = input.readBytes();
              break;
            }
          case 96:
            {
              originBodySize_ = input.readInt32();
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
        subscriberRegistIds_ = subscriberRegistIds_.getUnmodifiableView();
      }
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }

  public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
    return com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPbOuterClass
        .internal_static_ReceivedDataPb_descriptor;
  }

  @SuppressWarnings({"rawtypes"})
  protected com.google.protobuf.MapField internalGetMapField(int number) {
    switch (number) {
      case 7:
        return internalGetData();
      default:
        throw new RuntimeException("Invalid map field number: " + number);
    }
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPbOuterClass
        .internal_static_ReceivedDataPb_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb.class,
            com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb.Builder.class);
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

  public static final int SUBSCRIBERREGISTIDS_FIELD_NUMBER = 6;
  private com.google.protobuf.LazyStringList subscriberRegistIds_;
  /** <code>repeated string subscriberRegistIds = 6;</code> */
  public com.google.protobuf.ProtocolStringList getSubscriberRegistIdsList() {
    return subscriberRegistIds_;
  }
  /** <code>repeated string subscriberRegistIds = 6;</code> */
  public int getSubscriberRegistIdsCount() {
    return subscriberRegistIds_.size();
  }
  /** <code>repeated string subscriberRegistIds = 6;</code> */
  public java.lang.String getSubscriberRegistIds(int index) {
    return subscriberRegistIds_.get(index);
  }
  /** <code>repeated string subscriberRegistIds = 6;</code> */
  public com.google.protobuf.ByteString getSubscriberRegistIdsBytes(int index) {
    return subscriberRegistIds_.getByteString(index);
  }

  public static final int DATA_FIELD_NUMBER = 7;

  private static final class DataDefaultEntryHolder {
    static final com.google.protobuf.MapEntry<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
        defaultEntry =
            com.google.protobuf.MapEntry
                .<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
                    newDefaultInstance(
                        com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPbOuterClass
                            .internal_static_ReceivedDataPb_DataEntry_descriptor,
                        com.google.protobuf.WireFormat.FieldType.STRING,
                        "",
                        com.google.protobuf.WireFormat.FieldType.MESSAGE,
                        com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb
                            .getDefaultInstance());
  }

  private com.google.protobuf.MapField<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
      data_;

  private com.google.protobuf.MapField<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
      internalGetData() {
    if (data_ == null) {
      return com.google.protobuf.MapField.emptyMapField(DataDefaultEntryHolder.defaultEntry);
    }
    return data_;
  }

  public int getDataCount() {
    return internalGetData().getMap().size();
  }
  /** <code>map&lt;string, .DataBoxesPb&gt; data = 7;</code> */
  public boolean containsData(java.lang.String key) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    return internalGetData().getMap().containsKey(key);
  }
  /** Use {@link #getDataMap()} instead. */
  @java.lang.Deprecated
  public java.util.Map<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
      getData() {
    return getDataMap();
  }
  /** <code>map&lt;string, .DataBoxesPb&gt; data = 7;</code> */
  public java.util.Map<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
      getDataMap() {
    return internalGetData().getMap();
  }
  /** <code>map&lt;string, .DataBoxesPb&gt; data = 7;</code> */
  public com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb getDataOrDefault(
      java.lang.String key,
      com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb defaultValue) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
        map = internalGetData().getMap();
    return map.containsKey(key) ? map.get(key) : defaultValue;
  }
  /** <code>map&lt;string, .DataBoxesPb&gt; data = 7;</code> */
  public com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb getDataOrThrow(
      java.lang.String key) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
        map = internalGetData().getMap();
    if (!map.containsKey(key)) {
      throw new java.lang.IllegalArgumentException();
    }
    return map.get(key);
  }

  public static final int VERSION_FIELD_NUMBER = 8;
  private long version_;
  /** <code>int64 version = 8;</code> */
  public long getVersion() {
    return version_;
  }

  public static final int LOCALZONE_FIELD_NUMBER = 9;
  private volatile java.lang.Object localZone_;
  /** <code>string localZone = 9;</code> */
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
  /** <code>string localZone = 9;</code> */
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

  public static final int ENCODING_FIELD_NUMBER = 10;
  private volatile java.lang.Object encoding_;
  /** <code>string encoding = 10;</code> */
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
  /** <code>string encoding = 10;</code> */
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

  public static final int BODY_FIELD_NUMBER = 11;
  private com.google.protobuf.ByteString body_;
  /** <code>bytes body = 11;</code> */
  public com.google.protobuf.ByteString getBody() {
    return body_;
  }

  public static final int ORIGINBODYSIZE_FIELD_NUMBER = 12;
  private int originBodySize_;
  /** <code>int32 originBodySize = 12;</code> */
  public int getOriginBodySize() {
    return originBodySize_;
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
    for (int i = 0; i < subscriberRegistIds_.size(); i++) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 6, subscriberRegistIds_.getRaw(i));
    }
    com.google.protobuf.GeneratedMessageV3.serializeStringMapTo(
        output, internalGetData(), DataDefaultEntryHolder.defaultEntry, 7);
    if (version_ != 0L) {
      output.writeInt64(8, version_);
    }
    if (!getLocalZoneBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 9, localZone_);
    }
    if (!getEncodingBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 10, encoding_);
    }
    if (!body_.isEmpty()) {
      output.writeBytes(11, body_);
    }
    if (originBodySize_ != 0) {
      output.writeInt32(12, originBodySize_);
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
      for (int i = 0; i < subscriberRegistIds_.size(); i++) {
        dataSize += computeStringSizeNoTag(subscriberRegistIds_.getRaw(i));
      }
      size += dataSize;
      size += 1 * getSubscriberRegistIdsList().size();
    }
    for (java.util.Map.Entry<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
        entry : internalGetData().getMap().entrySet()) {
      com.google.protobuf.MapEntry<
              java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
          data__ =
              DataDefaultEntryHolder.defaultEntry
                  .newBuilderForType()
                  .setKey(entry.getKey())
                  .setValue(entry.getValue())
                  .build();
      size += com.google.protobuf.CodedOutputStream.computeMessageSize(7, data__);
    }
    if (version_ != 0L) {
      size += com.google.protobuf.CodedOutputStream.computeInt64Size(8, version_);
    }
    if (!getLocalZoneBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(9, localZone_);
    }
    if (!getEncodingBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(10, encoding_);
    }
    if (!body_.isEmpty()) {
      size += com.google.protobuf.CodedOutputStream.computeBytesSize(11, body_);
    }
    if (originBodySize_ != 0) {
      size += com.google.protobuf.CodedOutputStream.computeInt32Size(12, originBodySize_);
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
    if (!(obj instanceof com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb)) {
      return super.equals(obj);
    }
    com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb other =
        (com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb) obj;

    boolean result = true;
    result = result && getDataId().equals(other.getDataId());
    result = result && getGroup().equals(other.getGroup());
    result = result && getInstanceId().equals(other.getInstanceId());
    result = result && getSegment().equals(other.getSegment());
    result = result && getScope().equals(other.getScope());
    result = result && getSubscriberRegistIdsList().equals(other.getSubscriberRegistIdsList());
    result = result && internalGetData().equals(other.internalGetData());
    result = result && (getVersion() == other.getVersion());
    result = result && getLocalZone().equals(other.getLocalZone());
    result = result && getEncoding().equals(other.getEncoding());
    result = result && getBody().equals(other.getBody());
    result = result && (getOriginBodySize() == other.getOriginBodySize());
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
    if (getSubscriberRegistIdsCount() > 0) {
      hash = (37 * hash) + SUBSCRIBERREGISTIDS_FIELD_NUMBER;
      hash = (53 * hash) + getSubscriberRegistIdsList().hashCode();
    }
    if (!internalGetData().getMap().isEmpty()) {
      hash = (37 * hash) + DATA_FIELD_NUMBER;
      hash = (53 * hash) + internalGetData().hashCode();
    }
    hash = (37 * hash) + VERSION_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(getVersion());
    hash = (37 * hash) + LOCALZONE_FIELD_NUMBER;
    hash = (53 * hash) + getLocalZone().hashCode();
    hash = (37 * hash) + ENCODING_FIELD_NUMBER;
    hash = (53 * hash) + getEncoding().hashCode();
    hash = (37 * hash) + BODY_FIELD_NUMBER;
    hash = (53 * hash) + getBody().hashCode();
    hash = (37 * hash) + ORIGINBODYSIZE_FIELD_NUMBER;
    hash = (53 * hash) + getOriginBodySize();
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb parseFrom(
      java.nio.ByteBuffer data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb parseFrom(
      java.nio.ByteBuffer data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb parseFrom(
      byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb parseFrom(
      byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb parseFrom(
      java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb parseFrom(
      java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb parseDelimitedFrom(
      java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb parseDelimitedFrom(
      java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb parseFrom(
      com.google.protobuf.CodedInputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb parseFrom(
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
      com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb prototype) {
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
  /** Protobuf type {@code ReceivedDataPb} */
  public static final class Builder extends com.google.protobuf.GeneratedMessageV3.Builder<Builder>
      implements
      // @@protoc_insertion_point(builder_implements:ReceivedDataPb)
      com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPbOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
      return com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPbOuterClass
          .internal_static_ReceivedDataPb_descriptor;
    }

    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapField internalGetMapField(int number) {
      switch (number) {
        case 7:
          return internalGetData();
        default:
          throw new RuntimeException("Invalid map field number: " + number);
      }
    }

    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapField internalGetMutableMapField(int number) {
      switch (number) {
        case 7:
          return internalGetMutableData();
        default:
          throw new RuntimeException("Invalid map field number: " + number);
      }
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPbOuterClass
          .internal_static_ReceivedDataPb_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb.class,
              com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb.Builder.class);
    }

    // Construct using com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb.newBuilder()
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

      subscriberRegistIds_ = com.google.protobuf.LazyStringArrayList.EMPTY;
      bitField0_ = (bitField0_ & ~0x00000020);
      internalGetMutableData().clear();
      version_ = 0L;

      localZone_ = "";

      encoding_ = "";

      body_ = com.google.protobuf.ByteString.EMPTY;

      originBodySize_ = 0;

      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
      return com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPbOuterClass
          .internal_static_ReceivedDataPb_descriptor;
    }

    public com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb
        getDefaultInstanceForType() {
      return com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb.getDefaultInstance();
    }

    public com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb build() {
      com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb buildPartial() {
      com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb result =
          new com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb(this);
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      result.dataId_ = dataId_;
      result.group_ = group_;
      result.instanceId_ = instanceId_;
      result.segment_ = segment_;
      result.scope_ = scope_;
      if (((bitField0_ & 0x00000020) == 0x00000020)) {
        subscriberRegistIds_ = subscriberRegistIds_.getUnmodifiableView();
        bitField0_ = (bitField0_ & ~0x00000020);
      }
      result.subscriberRegistIds_ = subscriberRegistIds_;
      result.data_ = internalGetData();
      result.data_.makeImmutable();
      result.version_ = version_;
      result.localZone_ = localZone_;
      result.encoding_ = encoding_;
      result.body_ = body_;
      result.originBodySize_ = originBodySize_;
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
      if (other instanceof com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb) {
        return mergeFrom((com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb) other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb other) {
      if (other
          == com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb.getDefaultInstance())
        return this;
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
      if (!other.subscriberRegistIds_.isEmpty()) {
        if (subscriberRegistIds_.isEmpty()) {
          subscriberRegistIds_ = other.subscriberRegistIds_;
          bitField0_ = (bitField0_ & ~0x00000020);
        } else {
          ensureSubscriberRegistIdsIsMutable();
          subscriberRegistIds_.addAll(other.subscriberRegistIds_);
        }
        onChanged();
      }
      internalGetMutableData().mergeFrom(other.internalGetData());
      if (other.getVersion() != 0L) {
        setVersion(other.getVersion());
      }
      if (!other.getLocalZone().isEmpty()) {
        localZone_ = other.localZone_;
        onChanged();
      }
      if (!other.getEncoding().isEmpty()) {
        encoding_ = other.encoding_;
        onChanged();
      }
      if (other.getBody() != com.google.protobuf.ByteString.EMPTY) {
        setBody(other.getBody());
      }
      if (other.getOriginBodySize() != 0) {
        setOriginBodySize(other.getOriginBodySize());
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
      com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage =
            (com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb)
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

    private com.google.protobuf.LazyStringList subscriberRegistIds_ =
        com.google.protobuf.LazyStringArrayList.EMPTY;

    private void ensureSubscriberRegistIdsIsMutable() {
      if (!((bitField0_ & 0x00000020) == 0x00000020)) {
        subscriberRegistIds_ = new com.google.protobuf.LazyStringArrayList(subscriberRegistIds_);
        bitField0_ |= 0x00000020;
      }
    }
    /** <code>repeated string subscriberRegistIds = 6;</code> */
    public com.google.protobuf.ProtocolStringList getSubscriberRegistIdsList() {
      return subscriberRegistIds_.getUnmodifiableView();
    }
    /** <code>repeated string subscriberRegistIds = 6;</code> */
    public int getSubscriberRegistIdsCount() {
      return subscriberRegistIds_.size();
    }
    /** <code>repeated string subscriberRegistIds = 6;</code> */
    public java.lang.String getSubscriberRegistIds(int index) {
      return subscriberRegistIds_.get(index);
    }
    /** <code>repeated string subscriberRegistIds = 6;</code> */
    public com.google.protobuf.ByteString getSubscriberRegistIdsBytes(int index) {
      return subscriberRegistIds_.getByteString(index);
    }
    /** <code>repeated string subscriberRegistIds = 6;</code> */
    public Builder setSubscriberRegistIds(int index, java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }
      ensureSubscriberRegistIdsIsMutable();
      subscriberRegistIds_.set(index, value);
      onChanged();
      return this;
    }
    /** <code>repeated string subscriberRegistIds = 6;</code> */
    public Builder addSubscriberRegistIds(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }
      ensureSubscriberRegistIdsIsMutable();
      subscriberRegistIds_.add(value);
      onChanged();
      return this;
    }
    /** <code>repeated string subscriberRegistIds = 6;</code> */
    public Builder addAllSubscriberRegistIds(java.lang.Iterable<java.lang.String> values) {
      ensureSubscriberRegistIdsIsMutable();
      com.google.protobuf.AbstractMessageLite.Builder.addAll(values, subscriberRegistIds_);
      onChanged();
      return this;
    }
    /** <code>repeated string subscriberRegistIds = 6;</code> */
    public Builder clearSubscriberRegistIds() {
      subscriberRegistIds_ = com.google.protobuf.LazyStringArrayList.EMPTY;
      bitField0_ = (bitField0_ & ~0x00000020);
      onChanged();
      return this;
    }
    /** <code>repeated string subscriberRegistIds = 6;</code> */
    public Builder addSubscriberRegistIdsBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);
      ensureSubscriberRegistIdsIsMutable();
      subscriberRegistIds_.add(value);
      onChanged();
      return this;
    }

    private com.google.protobuf.MapField<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
        data_;

    private com.google.protobuf.MapField<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
        internalGetData() {
      if (data_ == null) {
        return com.google.protobuf.MapField.emptyMapField(DataDefaultEntryHolder.defaultEntry);
      }
      return data_;
    }

    private com.google.protobuf.MapField<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
        internalGetMutableData() {
      onChanged();
      ;
      if (data_ == null) {
        data_ = com.google.protobuf.MapField.newMapField(DataDefaultEntryHolder.defaultEntry);
      }
      if (!data_.isMutable()) {
        data_ = data_.copy();
      }
      return data_;
    }

    public int getDataCount() {
      return internalGetData().getMap().size();
    }
    /** <code>map&lt;string, .DataBoxesPb&gt; data = 7;</code> */
    public boolean containsData(java.lang.String key) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      return internalGetData().getMap().containsKey(key);
    }
    /** Use {@link #getDataMap()} instead. */
    @java.lang.Deprecated
    public java.util.Map<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
        getData() {
      return getDataMap();
    }
    /** <code>map&lt;string, .DataBoxesPb&gt; data = 7;</code> */
    public java.util.Map<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
        getDataMap() {
      return internalGetData().getMap();
    }
    /** <code>map&lt;string, .DataBoxesPb&gt; data = 7;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb getDataOrDefault(
        java.lang.String key,
        com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb defaultValue) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
          map = internalGetData().getMap();
      return map.containsKey(key) ? map.get(key) : defaultValue;
    }
    /** <code>map&lt;string, .DataBoxesPb&gt; data = 7;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb getDataOrThrow(
        java.lang.String key) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
          map = internalGetData().getMap();
      if (!map.containsKey(key)) {
        throw new java.lang.IllegalArgumentException();
      }
      return map.get(key);
    }

    public Builder clearData() {
      internalGetMutableData().getMutableMap().clear();
      return this;
    }
    /** <code>map&lt;string, .DataBoxesPb&gt; data = 7;</code> */
    public Builder removeData(java.lang.String key) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      internalGetMutableData().getMutableMap().remove(key);
      return this;
    }
    /** Use alternate mutation accessors instead. */
    @java.lang.Deprecated
    public java.util.Map<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
        getMutableData() {
      return internalGetMutableData().getMutableMap();
    }
    /** <code>map&lt;string, .DataBoxesPb&gt; data = 7;</code> */
    public Builder putData(
        java.lang.String key, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb value) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      if (value == null) {
        throw new java.lang.NullPointerException();
      }
      internalGetMutableData().getMutableMap().put(key, value);
      return this;
    }
    /** <code>map&lt;string, .DataBoxesPb&gt; data = 7;</code> */
    public Builder putAllData(
        java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
            values) {
      internalGetMutableData().getMutableMap().putAll(values);
      return this;
    }

    private long version_;
    /** <code>int64 version = 8;</code> */
    public long getVersion() {
      return version_;
    }
    /** <code>int64 version = 8;</code> */
    public Builder setVersion(long value) {

      version_ = value;
      onChanged();
      return this;
    }
    /** <code>int64 version = 8;</code> */
    public Builder clearVersion() {

      version_ = 0L;
      onChanged();
      return this;
    }

    private java.lang.Object localZone_ = "";
    /** <code>string localZone = 9;</code> */
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
    /** <code>string localZone = 9;</code> */
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
    /** <code>string localZone = 9;</code> */
    public Builder setLocalZone(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      localZone_ = value;
      onChanged();
      return this;
    }
    /** <code>string localZone = 9;</code> */
    public Builder clearLocalZone() {

      localZone_ = getDefaultInstance().getLocalZone();
      onChanged();
      return this;
    }
    /** <code>string localZone = 9;</code> */
    public Builder setLocalZoneBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);

      localZone_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object encoding_ = "";
    /** <code>string encoding = 10;</code> */
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
    /** <code>string encoding = 10;</code> */
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
    /** <code>string encoding = 10;</code> */
    public Builder setEncoding(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      encoding_ = value;
      onChanged();
      return this;
    }
    /** <code>string encoding = 10;</code> */
    public Builder clearEncoding() {

      encoding_ = getDefaultInstance().getEncoding();
      onChanged();
      return this;
    }
    /** <code>string encoding = 10;</code> */
    public Builder setEncodingBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);

      encoding_ = value;
      onChanged();
      return this;
    }

    private com.google.protobuf.ByteString body_ = com.google.protobuf.ByteString.EMPTY;
    /** <code>bytes body = 11;</code> */
    public com.google.protobuf.ByteString getBody() {
      return body_;
    }
    /** <code>bytes body = 11;</code> */
    public Builder setBody(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }

      body_ = value;
      onChanged();
      return this;
    }
    /** <code>bytes body = 11;</code> */
    public Builder clearBody() {

      body_ = getDefaultInstance().getBody();
      onChanged();
      return this;
    }

    private int originBodySize_;
    /** <code>int32 originBodySize = 12;</code> */
    public int getOriginBodySize() {
      return originBodySize_;
    }
    /** <code>int32 originBodySize = 12;</code> */
    public Builder setOriginBodySize(int value) {

      originBodySize_ = value;
      onChanged();
      return this;
    }
    /** <code>int32 originBodySize = 12;</code> */
    public Builder clearOriginBodySize() {

      originBodySize_ = 0;
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

    // @@protoc_insertion_point(builder_scope:ReceivedDataPb)
  }

  // @@protoc_insertion_point(class_scope:ReceivedDataPb)
  private static final com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb
      DEFAULT_INSTANCE;

  static {
    DEFAULT_INSTANCE = new com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb();
  }

  public static com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb
      getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<ReceivedDataPb> PARSER =
      new com.google.protobuf.AbstractParser<ReceivedDataPb>() {
        public ReceivedDataPb parsePartialFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
          return new ReceivedDataPb(input, extensionRegistry);
        }
      };

  public static com.google.protobuf.Parser<ReceivedDataPb> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<ReceivedDataPb> getParserForType() {
    return PARSER;
  }

  public com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb
      getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }
}
