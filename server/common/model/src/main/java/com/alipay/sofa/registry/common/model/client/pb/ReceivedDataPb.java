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
    localZone_ = "";
    encoding_ = "";
    body_ = com.google.protobuf.ByteString.EMPTY;
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(UnusedPrivateParameter unused) {
    return new ReceivedDataPb();
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
              if (!((mutable_bitField0_ & 0x00000001) != 0)) {
                subscriberRegistIds_ = new com.google.protobuf.LazyStringArrayList();
                mutable_bitField0_ |= 0x00000001;
              }
              subscriberRegistIds_.add(s);
              break;
            }
          case 58:
            {
              if (!((mutable_bitField0_ & 0x00000002) != 0)) {
                data_ =
                    com.google.protobuf.MapField.newMapField(DataDefaultEntryHolder.defaultEntry);
                mutable_bitField0_ |= 0x00000002;
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
          case 106:
            {
              if (!((mutable_bitField0_ & 0x00000004) != 0)) {
                pushDataCount_ =
                    com.google.protobuf.MapField.newMapField(
                        PushDataCountDefaultEntryHolder.defaultEntry);
                mutable_bitField0_ |= 0x00000004;
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
      if (((mutable_bitField0_ & 0x00000001) != 0)) {
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
  @java.lang.Override
  protected com.google.protobuf.MapField internalGetMapField(int number) {
    switch (number) {
      case 7:
        return internalGetData();
      case 13:
        return internalGetPushDataCount();
      default:
        throw new RuntimeException("Invalid map field number: " + number);
    }
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPbOuterClass
        .internal_static_ReceivedDataPb_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb.class,
            com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb.Builder.class);
  }

  public static final int DATAID_FIELD_NUMBER = 1;
  private volatile java.lang.Object dataId_;
  /**
   * <code>string dataId = 1;</code>
   *
   * @return The dataId.
   */
  @java.lang.Override
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
  /**
   * <code>string dataId = 1;</code>
   *
   * @return The bytes for dataId.
   */
  @java.lang.Override
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
  /**
   * <code>string group = 2;</code>
   *
   * @return The group.
   */
  @java.lang.Override
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
  /**
   * <code>string group = 2;</code>
   *
   * @return The bytes for group.
   */
  @java.lang.Override
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
  /**
   * <code>string instanceId = 3;</code>
   *
   * @return The instanceId.
   */
  @java.lang.Override
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
  /**
   * <code>string instanceId = 3;</code>
   *
   * @return The bytes for instanceId.
   */
  @java.lang.Override
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
  /**
   * <code>string segment = 4;</code>
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
   * <code>string segment = 4;</code>
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

  public static final int SCOPE_FIELD_NUMBER = 5;
  private volatile java.lang.Object scope_;
  /**
   * <code>string scope = 5;</code>
   *
   * @return The scope.
   */
  @java.lang.Override
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
  /**
   * <code>string scope = 5;</code>
   *
   * @return The bytes for scope.
   */
  @java.lang.Override
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
  /**
   * <code>repeated string subscriberRegistIds = 6;</code>
   *
   * @return A list containing the subscriberRegistIds.
   */
  public com.google.protobuf.ProtocolStringList getSubscriberRegistIdsList() {
    return subscriberRegistIds_;
  }
  /**
   * <code>repeated string subscriberRegistIds = 6;</code>
   *
   * @return The count of subscriberRegistIds.
   */
  public int getSubscriberRegistIdsCount() {
    return subscriberRegistIds_.size();
  }
  /**
   * <code>repeated string subscriberRegistIds = 6;</code>
   *
   * @param index The index of the element to return.
   * @return The subscriberRegistIds at the given index.
   */
  public java.lang.String getSubscriberRegistIds(int index) {
    return subscriberRegistIds_.get(index);
  }
  /**
   * <code>repeated string subscriberRegistIds = 6;</code>
   *
   * @param index The index of the value to return.
   * @return The bytes of the subscriberRegistIds at the given index.
   */
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
  @java.lang.Override
  public boolean containsData(java.lang.String key) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    return internalGetData().getMap().containsKey(key);
  }
  /** Use {@link #getDataMap()} instead. */
  @java.lang.Override
  @java.lang.Deprecated
  public java.util.Map<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
      getData() {
    return getDataMap();
  }
  /** <code>map&lt;string, .DataBoxesPb&gt; data = 7;</code> */
  @java.lang.Override
  public java.util.Map<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
      getDataMap() {
    return internalGetData().getMap();
  }
  /** <code>map&lt;string, .DataBoxesPb&gt; data = 7;</code> */
  @java.lang.Override
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
  @java.lang.Override
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
  /**
   * <code>int64 version = 8;</code>
   *
   * @return The version.
   */
  @java.lang.Override
  public long getVersion() {
    return version_;
  }

  public static final int LOCALZONE_FIELD_NUMBER = 9;
  private volatile java.lang.Object localZone_;
  /**
   * <code>string localZone = 9;</code>
   *
   * @return The localZone.
   */
  @java.lang.Override
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
  /**
   * <code>string localZone = 9;</code>
   *
   * @return The bytes for localZone.
   */
  @java.lang.Override
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
  /**
   * <code>string encoding = 10;</code>
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
   * <code>string encoding = 10;</code>
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

  public static final int BODY_FIELD_NUMBER = 11;
  private com.google.protobuf.ByteString body_;
  /**
   * <code>bytes body = 11;</code>
   *
   * @return The body.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString getBody() {
    return body_;
  }

  public static final int ORIGINBODYSIZE_FIELD_NUMBER = 12;
  private int originBodySize_;
  /**
   * <code>int32 originBodySize = 12;</code>
   *
   * @return The originBodySize.
   */
  @java.lang.Override
  public int getOriginBodySize() {
    return originBodySize_;
  }

  public static final int PUSHDATACOUNT_FIELD_NUMBER = 13;

  private static final class PushDataCountDefaultEntryHolder {
    static final com.google.protobuf.MapEntry<java.lang.String, java.lang.Integer> defaultEntry =
        com.google.protobuf.MapEntry.<java.lang.String, java.lang.Integer>newDefaultInstance(
            com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPbOuterClass
                .internal_static_ReceivedDataPb_PushDataCountEntry_descriptor,
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
  /** <code>map&lt;string, int32&gt; pushDataCount = 13;</code> */
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
  /** <code>map&lt;string, int32&gt; pushDataCount = 13;</code> */
  @java.lang.Override
  public java.util.Map<java.lang.String, java.lang.Integer> getPushDataCountMap() {
    return internalGetPushDataCount().getMap();
  }
  /** <code>map&lt;string, int32&gt; pushDataCount = 13;</code> */
  @java.lang.Override
  public int getPushDataCountOrDefault(java.lang.String key, int defaultValue) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    java.util.Map<java.lang.String, java.lang.Integer> map = internalGetPushDataCount().getMap();
    return map.containsKey(key) ? map.get(key) : defaultValue;
  }
  /** <code>map&lt;string, int32&gt; pushDataCount = 13;</code> */
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
    com.google.protobuf.GeneratedMessageV3.serializeStringMapTo(
        output, internalGetPushDataCount(), PushDataCountDefaultEntryHolder.defaultEntry, 13);
    unknownFields.writeTo(output);
  }

  @java.lang.Override
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
    for (java.util.Map.Entry<java.lang.String, java.lang.Integer> entry :
        internalGetPushDataCount().getMap().entrySet()) {
      com.google.protobuf.MapEntry<java.lang.String, java.lang.Integer> pushDataCount__ =
          PushDataCountDefaultEntryHolder.defaultEntry
              .newBuilderForType()
              .setKey(entry.getKey())
              .setValue(entry.getValue())
              .build();
      size += com.google.protobuf.CodedOutputStream.computeMessageSize(13, pushDataCount__);
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

    if (!getDataId().equals(other.getDataId())) return false;
    if (!getGroup().equals(other.getGroup())) return false;
    if (!getInstanceId().equals(other.getInstanceId())) return false;
    if (!getSegment().equals(other.getSegment())) return false;
    if (!getScope().equals(other.getScope())) return false;
    if (!getSubscriberRegistIdsList().equals(other.getSubscriberRegistIdsList())) return false;
    if (!internalGetData().equals(other.internalGetData())) return false;
    if (getVersion() != other.getVersion()) return false;
    if (!getLocalZone().equals(other.getLocalZone())) return false;
    if (!getEncoding().equals(other.getEncoding())) return false;
    if (!getBody().equals(other.getBody())) return false;
    if (getOriginBodySize() != other.getOriginBodySize()) return false;
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
    if (!internalGetPushDataCount().getMap().isEmpty()) {
      hash = (37 * hash) + PUSHDATACOUNT_FIELD_NUMBER;
      hash = (53 * hash) + internalGetPushDataCount().hashCode();
    }
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

  @java.lang.Override
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

  @java.lang.Override
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
        case 13:
          return internalGetPushDataCount();
        default:
          throw new RuntimeException("Invalid map field number: " + number);
      }
    }

    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapField internalGetMutableMapField(int number) {
      switch (number) {
        case 7:
          return internalGetMutableData();
        case 13:
          return internalGetMutablePushDataCount();
        default:
          throw new RuntimeException("Invalid map field number: " + number);
      }
    }

    @java.lang.Override
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

    @java.lang.Override
    public Builder clear() {
      super.clear();
      dataId_ = "";

      group_ = "";

      instanceId_ = "";

      segment_ = "";

      scope_ = "";

      subscriberRegistIds_ = com.google.protobuf.LazyStringArrayList.EMPTY;
      bitField0_ = (bitField0_ & ~0x00000001);
      internalGetMutableData().clear();
      version_ = 0L;

      localZone_ = "";

      encoding_ = "";

      body_ = com.google.protobuf.ByteString.EMPTY;

      originBodySize_ = 0;

      internalGetMutablePushDataCount().clear();
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
      return com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPbOuterClass
          .internal_static_ReceivedDataPb_descriptor;
    }

    @java.lang.Override
    public com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb
        getDefaultInstanceForType() {
      return com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb.getDefaultInstance();
    }

    @java.lang.Override
    public com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb build() {
      com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb buildPartial() {
      com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb result =
          new com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb(this);
      int from_bitField0_ = bitField0_;
      result.dataId_ = dataId_;
      result.group_ = group_;
      result.instanceId_ = instanceId_;
      result.segment_ = segment_;
      result.scope_ = scope_;
      if (((bitField0_ & 0x00000001) != 0)) {
        subscriberRegistIds_ = subscriberRegistIds_.getUnmodifiableView();
        bitField0_ = (bitField0_ & ~0x00000001);
      }
      result.subscriberRegistIds_ = subscriberRegistIds_;
      result.data_ = internalGetData();
      result.data_.makeImmutable();
      result.version_ = version_;
      result.localZone_ = localZone_;
      result.encoding_ = encoding_;
      result.body_ = body_;
      result.originBodySize_ = originBodySize_;
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
          bitField0_ = (bitField0_ & ~0x00000001);
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
    /**
     * <code>string dataId = 1;</code>
     *
     * @return The dataId.
     */
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
    /**
     * <code>string dataId = 1;</code>
     *
     * @return The bytes for dataId.
     */
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
    /**
     * <code>string dataId = 1;</code>
     *
     * @param value The dataId to set.
     * @return This builder for chaining.
     */
    public Builder setDataId(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      dataId_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string dataId = 1;</code>
     *
     * @return This builder for chaining.
     */
    public Builder clearDataId() {

      dataId_ = getDefaultInstance().getDataId();
      onChanged();
      return this;
    }
    /**
     * <code>string dataId = 1;</code>
     *
     * @param value The bytes for dataId to set.
     * @return This builder for chaining.
     */
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
    /**
     * <code>string group = 2;</code>
     *
     * @return The group.
     */
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
    /**
     * <code>string group = 2;</code>
     *
     * @return The bytes for group.
     */
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
    /**
     * <code>string group = 2;</code>
     *
     * @param value The group to set.
     * @return This builder for chaining.
     */
    public Builder setGroup(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      group_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string group = 2;</code>
     *
     * @return This builder for chaining.
     */
    public Builder clearGroup() {

      group_ = getDefaultInstance().getGroup();
      onChanged();
      return this;
    }
    /**
     * <code>string group = 2;</code>
     *
     * @param value The bytes for group to set.
     * @return This builder for chaining.
     */
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
    /**
     * <code>string instanceId = 3;</code>
     *
     * @return The instanceId.
     */
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
    /**
     * <code>string instanceId = 3;</code>
     *
     * @return The bytes for instanceId.
     */
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
    /**
     * <code>string instanceId = 3;</code>
     *
     * @param value The instanceId to set.
     * @return This builder for chaining.
     */
    public Builder setInstanceId(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      instanceId_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string instanceId = 3;</code>
     *
     * @return This builder for chaining.
     */
    public Builder clearInstanceId() {

      instanceId_ = getDefaultInstance().getInstanceId();
      onChanged();
      return this;
    }
    /**
     * <code>string instanceId = 3;</code>
     *
     * @param value The bytes for instanceId to set.
     * @return This builder for chaining.
     */
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
    /**
     * <code>string segment = 4;</code>
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
     * <code>string segment = 4;</code>
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
     * <code>string segment = 4;</code>
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
     * <code>string segment = 4;</code>
     *
     * @return This builder for chaining.
     */
    public Builder clearSegment() {

      segment_ = getDefaultInstance().getSegment();
      onChanged();
      return this;
    }
    /**
     * <code>string segment = 4;</code>
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

    private java.lang.Object scope_ = "";
    /**
     * <code>string scope = 5;</code>
     *
     * @return The scope.
     */
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
    /**
     * <code>string scope = 5;</code>
     *
     * @return The bytes for scope.
     */
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
    /**
     * <code>string scope = 5;</code>
     *
     * @param value The scope to set.
     * @return This builder for chaining.
     */
    public Builder setScope(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      scope_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string scope = 5;</code>
     *
     * @return This builder for chaining.
     */
    public Builder clearScope() {

      scope_ = getDefaultInstance().getScope();
      onChanged();
      return this;
    }
    /**
     * <code>string scope = 5;</code>
     *
     * @param value The bytes for scope to set.
     * @return This builder for chaining.
     */
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
      if (!((bitField0_ & 0x00000001) != 0)) {
        subscriberRegistIds_ = new com.google.protobuf.LazyStringArrayList(subscriberRegistIds_);
        bitField0_ |= 0x00000001;
      }
    }
    /**
     * <code>repeated string subscriberRegistIds = 6;</code>
     *
     * @return A list containing the subscriberRegistIds.
     */
    public com.google.protobuf.ProtocolStringList getSubscriberRegistIdsList() {
      return subscriberRegistIds_.getUnmodifiableView();
    }
    /**
     * <code>repeated string subscriberRegistIds = 6;</code>
     *
     * @return The count of subscriberRegistIds.
     */
    public int getSubscriberRegistIdsCount() {
      return subscriberRegistIds_.size();
    }
    /**
     * <code>repeated string subscriberRegistIds = 6;</code>
     *
     * @param index The index of the element to return.
     * @return The subscriberRegistIds at the given index.
     */
    public java.lang.String getSubscriberRegistIds(int index) {
      return subscriberRegistIds_.get(index);
    }
    /**
     * <code>repeated string subscriberRegistIds = 6;</code>
     *
     * @param index The index of the value to return.
     * @return The bytes of the subscriberRegistIds at the given index.
     */
    public com.google.protobuf.ByteString getSubscriberRegistIdsBytes(int index) {
      return subscriberRegistIds_.getByteString(index);
    }
    /**
     * <code>repeated string subscriberRegistIds = 6;</code>
     *
     * @param index The index to set the value at.
     * @param value The subscriberRegistIds to set.
     * @return This builder for chaining.
     */
    public Builder setSubscriberRegistIds(int index, java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }
      ensureSubscriberRegistIdsIsMutable();
      subscriberRegistIds_.set(index, value);
      onChanged();
      return this;
    }
    /**
     * <code>repeated string subscriberRegistIds = 6;</code>
     *
     * @param value The subscriberRegistIds to add.
     * @return This builder for chaining.
     */
    public Builder addSubscriberRegistIds(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }
      ensureSubscriberRegistIdsIsMutable();
      subscriberRegistIds_.add(value);
      onChanged();
      return this;
    }
    /**
     * <code>repeated string subscriberRegistIds = 6;</code>
     *
     * @param values The subscriberRegistIds to add.
     * @return This builder for chaining.
     */
    public Builder addAllSubscriberRegistIds(java.lang.Iterable<java.lang.String> values) {
      ensureSubscriberRegistIdsIsMutable();
      com.google.protobuf.AbstractMessageLite.Builder.addAll(values, subscriberRegistIds_);
      onChanged();
      return this;
    }
    /**
     * <code>repeated string subscriberRegistIds = 6;</code>
     *
     * @return This builder for chaining.
     */
    public Builder clearSubscriberRegistIds() {
      subscriberRegistIds_ = com.google.protobuf.LazyStringArrayList.EMPTY;
      bitField0_ = (bitField0_ & ~0x00000001);
      onChanged();
      return this;
    }
    /**
     * <code>repeated string subscriberRegistIds = 6;</code>
     *
     * @param value The bytes of the subscriberRegistIds to add.
     * @return This builder for chaining.
     */
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
    @java.lang.Override
    public boolean containsData(java.lang.String key) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      return internalGetData().getMap().containsKey(key);
    }
    /** Use {@link #getDataMap()} instead. */
    @java.lang.Override
    @java.lang.Deprecated
    public java.util.Map<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
        getData() {
      return getDataMap();
    }
    /** <code>map&lt;string, .DataBoxesPb&gt; data = 7;</code> */
    @java.lang.Override
    public java.util.Map<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
        getDataMap() {
      return internalGetData().getMap();
    }
    /** <code>map&lt;string, .DataBoxesPb&gt; data = 7;</code> */
    @java.lang.Override
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
    @java.lang.Override
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
    /**
     * <code>int64 version = 8;</code>
     *
     * @return The version.
     */
    @java.lang.Override
    public long getVersion() {
      return version_;
    }
    /**
     * <code>int64 version = 8;</code>
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
     * <code>int64 version = 8;</code>
     *
     * @return This builder for chaining.
     */
    public Builder clearVersion() {

      version_ = 0L;
      onChanged();
      return this;
    }

    private java.lang.Object localZone_ = "";
    /**
     * <code>string localZone = 9;</code>
     *
     * @return The localZone.
     */
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
    /**
     * <code>string localZone = 9;</code>
     *
     * @return The bytes for localZone.
     */
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
    /**
     * <code>string localZone = 9;</code>
     *
     * @param value The localZone to set.
     * @return This builder for chaining.
     */
    public Builder setLocalZone(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      localZone_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string localZone = 9;</code>
     *
     * @return This builder for chaining.
     */
    public Builder clearLocalZone() {

      localZone_ = getDefaultInstance().getLocalZone();
      onChanged();
      return this;
    }
    /**
     * <code>string localZone = 9;</code>
     *
     * @param value The bytes for localZone to set.
     * @return This builder for chaining.
     */
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
    /**
     * <code>string encoding = 10;</code>
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
     * <code>string encoding = 10;</code>
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
     * <code>string encoding = 10;</code>
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
     * <code>string encoding = 10;</code>
     *
     * @return This builder for chaining.
     */
    public Builder clearEncoding() {

      encoding_ = getDefaultInstance().getEncoding();
      onChanged();
      return this;
    }
    /**
     * <code>string encoding = 10;</code>
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

    private com.google.protobuf.ByteString body_ = com.google.protobuf.ByteString.EMPTY;
    /**
     * <code>bytes body = 11;</code>
     *
     * @return The body.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getBody() {
      return body_;
    }
    /**
     * <code>bytes body = 11;</code>
     *
     * @param value The body to set.
     * @return This builder for chaining.
     */
    public Builder setBody(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }

      body_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>bytes body = 11;</code>
     *
     * @return This builder for chaining.
     */
    public Builder clearBody() {

      body_ = getDefaultInstance().getBody();
      onChanged();
      return this;
    }

    private int originBodySize_;
    /**
     * <code>int32 originBodySize = 12;</code>
     *
     * @return The originBodySize.
     */
    @java.lang.Override
    public int getOriginBodySize() {
      return originBodySize_;
    }
    /**
     * <code>int32 originBodySize = 12;</code>
     *
     * @param value The originBodySize to set.
     * @return This builder for chaining.
     */
    public Builder setOriginBodySize(int value) {

      originBodySize_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>int32 originBodySize = 12;</code>
     *
     * @return This builder for chaining.
     */
    public Builder clearOriginBodySize() {

      originBodySize_ = 0;
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
    /** <code>map&lt;string, int32&gt; pushDataCount = 13;</code> */
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
    /** <code>map&lt;string, int32&gt; pushDataCount = 13;</code> */
    @java.lang.Override
    public java.util.Map<java.lang.String, java.lang.Integer> getPushDataCountMap() {
      return internalGetPushDataCount().getMap();
    }
    /** <code>map&lt;string, int32&gt; pushDataCount = 13;</code> */
    @java.lang.Override
    public int getPushDataCountOrDefault(java.lang.String key, int defaultValue) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      java.util.Map<java.lang.String, java.lang.Integer> map = internalGetPushDataCount().getMap();
      return map.containsKey(key) ? map.get(key) : defaultValue;
    }
    /** <code>map&lt;string, int32&gt; pushDataCount = 13;</code> */
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
    /** <code>map&lt;string, int32&gt; pushDataCount = 13;</code> */
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
    /** <code>map&lt;string, int32&gt; pushDataCount = 13;</code> */
    public Builder putPushDataCount(java.lang.String key, int value) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }

      internalGetMutablePushDataCount().getMutableMap().put(key, value);
      return this;
    }
    /** <code>map&lt;string, int32&gt; pushDataCount = 13;</code> */
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
        @java.lang.Override
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

  @java.lang.Override
  public com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb
      getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }
}
