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

/** Protobuf type {@code MultiReceivedDataPb} */
public final class MultiReceivedDataPb extends com.google.protobuf.GeneratedMessageV3
    implements
    // @@protoc_insertion_point(message_implements:MultiReceivedDataPb)
    MultiReceivedDataPbOrBuilder {
  private static final long serialVersionUID = 0L;
  // Use MultiReceivedDataPb.newBuilder() to construct.
  private MultiReceivedDataPb(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }

  private MultiReceivedDataPb() {
    dataId_ = "";
    group_ = "";
    instanceId_ = "";
    scope_ = "";
    subscriberRegistIds_ = com.google.protobuf.LazyStringArrayList.EMPTY;
    localSegment_ = "";
    localZone_ = "";
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(UnusedPrivateParameter unused) {
    return new MultiReceivedDataPb();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
    return this.unknownFields;
  }

  private MultiReceivedDataPb(
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

              scope_ = s;
              break;
            }
          case 42:
            {
              java.lang.String s = input.readStringRequireUtf8();
              if (!((mutable_bitField0_ & 0x00000001) != 0)) {
                subscriberRegistIds_ = new com.google.protobuf.LazyStringArrayList();
                mutable_bitField0_ |= 0x00000001;
              }
              subscriberRegistIds_.add(s);
              break;
            }
          case 50:
            {
              java.lang.String s = input.readStringRequireUtf8();

              localSegment_ = s;
              break;
            }
          case 58:
            {
              java.lang.String s = input.readStringRequireUtf8();

              localZone_ = s;
              break;
            }
          case 66:
            {
              if (!((mutable_bitField0_ & 0x00000002) != 0)) {
                multiData_ =
                    com.google.protobuf.MapField.newMapField(
                        MultiDataDefaultEntryHolder.defaultEntry);
                mutable_bitField0_ |= 0x00000002;
              }
              com.google.protobuf.MapEntry<
                      java.lang.String,
                      com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb>
                  multiData__ =
                      input.readMessage(
                          MultiDataDefaultEntryHolder.defaultEntry.getParserForType(),
                          extensionRegistry);
              multiData_.getMutableMap().put(multiData__.getKey(), multiData__.getValue());
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
    return com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPbOuterClass
        .internal_static_MultiReceivedDataPb_descriptor;
  }

  @SuppressWarnings({"rawtypes"})
  @java.lang.Override
  protected com.google.protobuf.MapField internalGetMapField(int number) {
    switch (number) {
      case 8:
        return internalGetMultiData();
      default:
        throw new RuntimeException("Invalid map field number: " + number);
    }
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPbOuterClass
        .internal_static_MultiReceivedDataPb_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPb.class,
            com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPb.Builder.class);
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

  public static final int SCOPE_FIELD_NUMBER = 4;
  private volatile java.lang.Object scope_;
  /**
   * <code>string scope = 4;</code>
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
   * <code>string scope = 4;</code>
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

  public static final int SUBSCRIBERREGISTIDS_FIELD_NUMBER = 5;
  private com.google.protobuf.LazyStringList subscriberRegistIds_;
  /**
   * <code>repeated string subscriberRegistIds = 5;</code>
   *
   * @return A list containing the subscriberRegistIds.
   */
  public com.google.protobuf.ProtocolStringList getSubscriberRegistIdsList() {
    return subscriberRegistIds_;
  }
  /**
   * <code>repeated string subscriberRegistIds = 5;</code>
   *
   * @return The count of subscriberRegistIds.
   */
  public int getSubscriberRegistIdsCount() {
    return subscriberRegistIds_.size();
  }
  /**
   * <code>repeated string subscriberRegistIds = 5;</code>
   *
   * @param index The index of the element to return.
   * @return The subscriberRegistIds at the given index.
   */
  public java.lang.String getSubscriberRegistIds(int index) {
    return subscriberRegistIds_.get(index);
  }
  /**
   * <code>repeated string subscriberRegistIds = 5;</code>
   *
   * @param index The index of the value to return.
   * @return The bytes of the subscriberRegistIds at the given index.
   */
  public com.google.protobuf.ByteString getSubscriberRegistIdsBytes(int index) {
    return subscriberRegistIds_.getByteString(index);
  }

  public static final int LOCALSEGMENT_FIELD_NUMBER = 6;
  private volatile java.lang.Object localSegment_;
  /**
   * <code>string localSegment = 6;</code>
   *
   * @return The localSegment.
   */
  @java.lang.Override
  public java.lang.String getLocalSegment() {
    java.lang.Object ref = localSegment_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      localSegment_ = s;
      return s;
    }
  }
  /**
   * <code>string localSegment = 6;</code>
   *
   * @return The bytes for localSegment.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString getLocalSegmentBytes() {
    java.lang.Object ref = localSegment_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b =
          com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
      localSegment_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int LOCALZONE_FIELD_NUMBER = 7;
  private volatile java.lang.Object localZone_;
  /**
   * <code>string localZone = 7;</code>
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
   * <code>string localZone = 7;</code>
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

  public static final int MULTIDATA_FIELD_NUMBER = 8;

  private static final class MultiDataDefaultEntryHolder {
    static final com.google.protobuf.MapEntry<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb>
        defaultEntry =
            com.google.protobuf.MapEntry
                .<java.lang.String,
                    com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb>
                    newDefaultInstance(
                        com.alipay.sofa.registry.common.model.client.pb
                            .MultiReceivedDataPbOuterClass
                            .internal_static_MultiReceivedDataPb_MultiDataEntry_descriptor,
                        com.google.protobuf.WireFormat.FieldType.STRING,
                        "",
                        com.google.protobuf.WireFormat.FieldType.MESSAGE,
                        com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb
                            .getDefaultInstance());
  }

  private com.google.protobuf.MapField<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb>
      multiData_;

  private com.google.protobuf.MapField<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb>
      internalGetMultiData() {
    if (multiData_ == null) {
      return com.google.protobuf.MapField.emptyMapField(MultiDataDefaultEntryHolder.defaultEntry);
    }
    return multiData_;
  }

  public int getMultiDataCount() {
    return internalGetMultiData().getMap().size();
  }
  /** <code>map&lt;string, .MultiSegmentDataPb&gt; multiData = 8;</code> */
  @java.lang.Override
  public boolean containsMultiData(java.lang.String key) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    return internalGetMultiData().getMap().containsKey(key);
  }
  /** Use {@link #getMultiDataMap()} instead. */
  @java.lang.Override
  @java.lang.Deprecated
  public java.util.Map<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb>
      getMultiData() {
    return getMultiDataMap();
  }
  /** <code>map&lt;string, .MultiSegmentDataPb&gt; multiData = 8;</code> */
  @java.lang.Override
  public java.util.Map<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb>
      getMultiDataMap() {
    return internalGetMultiData().getMap();
  }
  /** <code>map&lt;string, .MultiSegmentDataPb&gt; multiData = 8;</code> */
  @java.lang.Override
  public com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb getMultiDataOrDefault(
      java.lang.String key,
      com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb defaultValue) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    java.util.Map<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb>
        map = internalGetMultiData().getMap();
    return map.containsKey(key) ? map.get(key) : defaultValue;
  }
  /** <code>map&lt;string, .MultiSegmentDataPb&gt; multiData = 8;</code> */
  @java.lang.Override
  public com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb getMultiDataOrThrow(
      java.lang.String key) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    java.util.Map<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb>
        map = internalGetMultiData().getMap();
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
    if (!getScopeBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 4, scope_);
    }
    for (int i = 0; i < subscriberRegistIds_.size(); i++) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 5, subscriberRegistIds_.getRaw(i));
    }
    if (!getLocalSegmentBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 6, localSegment_);
    }
    if (!getLocalZoneBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 7, localZone_);
    }
    com.google.protobuf.GeneratedMessageV3.serializeStringMapTo(
        output, internalGetMultiData(), MultiDataDefaultEntryHolder.defaultEntry, 8);
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
    if (!getScopeBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(4, scope_);
    }
    {
      int dataSize = 0;
      for (int i = 0; i < subscriberRegistIds_.size(); i++) {
        dataSize += computeStringSizeNoTag(subscriberRegistIds_.getRaw(i));
      }
      size += dataSize;
      size += 1 * getSubscriberRegistIdsList().size();
    }
    if (!getLocalSegmentBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(6, localSegment_);
    }
    if (!getLocalZoneBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(7, localZone_);
    }
    for (java.util.Map.Entry<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb>
        entry : internalGetMultiData().getMap().entrySet()) {
      com.google.protobuf.MapEntry<
              java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb>
          multiData__ =
              MultiDataDefaultEntryHolder.defaultEntry
                  .newBuilderForType()
                  .setKey(entry.getKey())
                  .setValue(entry.getValue())
                  .build();
      size += com.google.protobuf.CodedOutputStream.computeMessageSize(8, multiData__);
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
    if (!(obj instanceof com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPb)) {
      return super.equals(obj);
    }
    com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPb other =
        (com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPb) obj;

    if (!getDataId().equals(other.getDataId())) return false;
    if (!getGroup().equals(other.getGroup())) return false;
    if (!getInstanceId().equals(other.getInstanceId())) return false;
    if (!getScope().equals(other.getScope())) return false;
    if (!getSubscriberRegistIdsList().equals(other.getSubscriberRegistIdsList())) return false;
    if (!getLocalSegment().equals(other.getLocalSegment())) return false;
    if (!getLocalZone().equals(other.getLocalZone())) return false;
    if (!internalGetMultiData().equals(other.internalGetMultiData())) return false;
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
    hash = (37 * hash) + SCOPE_FIELD_NUMBER;
    hash = (53 * hash) + getScope().hashCode();
    if (getSubscriberRegistIdsCount() > 0) {
      hash = (37 * hash) + SUBSCRIBERREGISTIDS_FIELD_NUMBER;
      hash = (53 * hash) + getSubscriberRegistIdsList().hashCode();
    }
    hash = (37 * hash) + LOCALSEGMENT_FIELD_NUMBER;
    hash = (53 * hash) + getLocalSegment().hashCode();
    hash = (37 * hash) + LOCALZONE_FIELD_NUMBER;
    hash = (53 * hash) + getLocalZone().hashCode();
    if (!internalGetMultiData().getMap().isEmpty()) {
      hash = (37 * hash) + MULTIDATA_FIELD_NUMBER;
      hash = (53 * hash) + internalGetMultiData().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPb parseFrom(
      java.nio.ByteBuffer data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPb parseFrom(
      java.nio.ByteBuffer data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPb parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPb parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPb parseFrom(
      byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPb parseFrom(
      byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPb parseFrom(
      java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPb parseFrom(
      java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPb
      parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPb
      parseDelimitedFrom(
          java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPb parseFrom(
      com.google.protobuf.CodedInputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPb parseFrom(
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
      com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPb prototype) {
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
  /** Protobuf type {@code MultiReceivedDataPb} */
  public static final class Builder extends com.google.protobuf.GeneratedMessageV3.Builder<Builder>
      implements
      // @@protoc_insertion_point(builder_implements:MultiReceivedDataPb)
      com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPbOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
      return com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPbOuterClass
          .internal_static_MultiReceivedDataPb_descriptor;
    }

    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapField internalGetMapField(int number) {
      switch (number) {
        case 8:
          return internalGetMultiData();
        default:
          throw new RuntimeException("Invalid map field number: " + number);
      }
    }

    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapField internalGetMutableMapField(int number) {
      switch (number) {
        case 8:
          return internalGetMutableMultiData();
        default:
          throw new RuntimeException("Invalid map field number: " + number);
      }
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPbOuterClass
          .internal_static_MultiReceivedDataPb_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPb.class,
              com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPb.Builder.class);
    }

    // Construct using
    // com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPb.newBuilder()
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

      scope_ = "";

      subscriberRegistIds_ = com.google.protobuf.LazyStringArrayList.EMPTY;
      bitField0_ = (bitField0_ & ~0x00000001);
      localSegment_ = "";

      localZone_ = "";

      internalGetMutableMultiData().clear();
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
      return com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPbOuterClass
          .internal_static_MultiReceivedDataPb_descriptor;
    }

    @java.lang.Override
    public com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPb
        getDefaultInstanceForType() {
      return com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPb
          .getDefaultInstance();
    }

    @java.lang.Override
    public com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPb build() {
      com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPb result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPb buildPartial() {
      com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPb result =
          new com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPb(this);
      int from_bitField0_ = bitField0_;
      result.dataId_ = dataId_;
      result.group_ = group_;
      result.instanceId_ = instanceId_;
      result.scope_ = scope_;
      if (((bitField0_ & 0x00000001) != 0)) {
        subscriberRegistIds_ = subscriberRegistIds_.getUnmodifiableView();
        bitField0_ = (bitField0_ & ~0x00000001);
      }
      result.subscriberRegistIds_ = subscriberRegistIds_;
      result.localSegment_ = localSegment_;
      result.localZone_ = localZone_;
      result.multiData_ = internalGetMultiData();
      result.multiData_.makeImmutable();
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
      if (other instanceof com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPb) {
        return mergeFrom(
            (com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPb) other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(
        com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPb other) {
      if (other
          == com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPb
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
      if (!other.getLocalSegment().isEmpty()) {
        localSegment_ = other.localSegment_;
        onChanged();
      }
      if (!other.getLocalZone().isEmpty()) {
        localZone_ = other.localZone_;
        onChanged();
      }
      internalGetMutableMultiData().mergeFrom(other.internalGetMultiData());
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
      com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPb parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage =
            (com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPb)
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

    private java.lang.Object scope_ = "";
    /**
     * <code>string scope = 4;</code>
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
     * <code>string scope = 4;</code>
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
     * <code>string scope = 4;</code>
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
     * <code>string scope = 4;</code>
     *
     * @return This builder for chaining.
     */
    public Builder clearScope() {

      scope_ = getDefaultInstance().getScope();
      onChanged();
      return this;
    }
    /**
     * <code>string scope = 4;</code>
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
     * <code>repeated string subscriberRegistIds = 5;</code>
     *
     * @return A list containing the subscriberRegistIds.
     */
    public com.google.protobuf.ProtocolStringList getSubscriberRegistIdsList() {
      return subscriberRegistIds_.getUnmodifiableView();
    }
    /**
     * <code>repeated string subscriberRegistIds = 5;</code>
     *
     * @return The count of subscriberRegistIds.
     */
    public int getSubscriberRegistIdsCount() {
      return subscriberRegistIds_.size();
    }
    /**
     * <code>repeated string subscriberRegistIds = 5;</code>
     *
     * @param index The index of the element to return.
     * @return The subscriberRegistIds at the given index.
     */
    public java.lang.String getSubscriberRegistIds(int index) {
      return subscriberRegistIds_.get(index);
    }
    /**
     * <code>repeated string subscriberRegistIds = 5;</code>
     *
     * @param index The index of the value to return.
     * @return The bytes of the subscriberRegistIds at the given index.
     */
    public com.google.protobuf.ByteString getSubscriberRegistIdsBytes(int index) {
      return subscriberRegistIds_.getByteString(index);
    }
    /**
     * <code>repeated string subscriberRegistIds = 5;</code>
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
     * <code>repeated string subscriberRegistIds = 5;</code>
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
     * <code>repeated string subscriberRegistIds = 5;</code>
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
     * <code>repeated string subscriberRegistIds = 5;</code>
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
     * <code>repeated string subscriberRegistIds = 5;</code>
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

    private java.lang.Object localSegment_ = "";
    /**
     * <code>string localSegment = 6;</code>
     *
     * @return The localSegment.
     */
    public java.lang.String getLocalSegment() {
      java.lang.Object ref = localSegment_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        localSegment_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string localSegment = 6;</code>
     *
     * @return The bytes for localSegment.
     */
    public com.google.protobuf.ByteString getLocalSegmentBytes() {
      java.lang.Object ref = localSegment_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
            com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
        localSegment_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string localSegment = 6;</code>
     *
     * @param value The localSegment to set.
     * @return This builder for chaining.
     */
    public Builder setLocalSegment(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      localSegment_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string localSegment = 6;</code>
     *
     * @return This builder for chaining.
     */
    public Builder clearLocalSegment() {

      localSegment_ = getDefaultInstance().getLocalSegment();
      onChanged();
      return this;
    }
    /**
     * <code>string localSegment = 6;</code>
     *
     * @param value The bytes for localSegment to set.
     * @return This builder for chaining.
     */
    public Builder setLocalSegmentBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);

      localSegment_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object localZone_ = "";
    /**
     * <code>string localZone = 7;</code>
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
     * <code>string localZone = 7;</code>
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
     * <code>string localZone = 7;</code>
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
     * <code>string localZone = 7;</code>
     *
     * @return This builder for chaining.
     */
    public Builder clearLocalZone() {

      localZone_ = getDefaultInstance().getLocalZone();
      onChanged();
      return this;
    }
    /**
     * <code>string localZone = 7;</code>
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

    private com.google.protobuf.MapField<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb>
        multiData_;

    private com.google.protobuf.MapField<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb>
        internalGetMultiData() {
      if (multiData_ == null) {
        return com.google.protobuf.MapField.emptyMapField(MultiDataDefaultEntryHolder.defaultEntry);
      }
      return multiData_;
    }

    private com.google.protobuf.MapField<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb>
        internalGetMutableMultiData() {
      onChanged();
      ;
      if (multiData_ == null) {
        multiData_ =
            com.google.protobuf.MapField.newMapField(MultiDataDefaultEntryHolder.defaultEntry);
      }
      if (!multiData_.isMutable()) {
        multiData_ = multiData_.copy();
      }
      return multiData_;
    }

    public int getMultiDataCount() {
      return internalGetMultiData().getMap().size();
    }
    /** <code>map&lt;string, .MultiSegmentDataPb&gt; multiData = 8;</code> */
    @java.lang.Override
    public boolean containsMultiData(java.lang.String key) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      return internalGetMultiData().getMap().containsKey(key);
    }
    /** Use {@link #getMultiDataMap()} instead. */
    @java.lang.Override
    @java.lang.Deprecated
    public java.util.Map<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb>
        getMultiData() {
      return getMultiDataMap();
    }
    /** <code>map&lt;string, .MultiSegmentDataPb&gt; multiData = 8;</code> */
    @java.lang.Override
    public java.util.Map<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb>
        getMultiDataMap() {
      return internalGetMultiData().getMap();
    }
    /** <code>map&lt;string, .MultiSegmentDataPb&gt; multiData = 8;</code> */
    @java.lang.Override
    public com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb getMultiDataOrDefault(
        java.lang.String key,
        com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb defaultValue) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      java.util.Map<
              java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb>
          map = internalGetMultiData().getMap();
      return map.containsKey(key) ? map.get(key) : defaultValue;
    }
    /** <code>map&lt;string, .MultiSegmentDataPb&gt; multiData = 8;</code> */
    @java.lang.Override
    public com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb getMultiDataOrThrow(
        java.lang.String key) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      java.util.Map<
              java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb>
          map = internalGetMultiData().getMap();
      if (!map.containsKey(key)) {
        throw new java.lang.IllegalArgumentException();
      }
      return map.get(key);
    }

    public Builder clearMultiData() {
      internalGetMutableMultiData().getMutableMap().clear();
      return this;
    }
    /** <code>map&lt;string, .MultiSegmentDataPb&gt; multiData = 8;</code> */
    public Builder removeMultiData(java.lang.String key) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      internalGetMutableMultiData().getMutableMap().remove(key);
      return this;
    }
    /** Use alternate mutation accessors instead. */
    @java.lang.Deprecated
    public java.util.Map<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb>
        getMutableMultiData() {
      return internalGetMutableMultiData().getMutableMap();
    }
    /** <code>map&lt;string, .MultiSegmentDataPb&gt; multiData = 8;</code> */
    public Builder putMultiData(
        java.lang.String key,
        com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb value) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      if (value == null) {
        throw new java.lang.NullPointerException();
      }
      internalGetMutableMultiData().getMutableMap().put(key, value);
      return this;
    }
    /** <code>map&lt;string, .MultiSegmentDataPb&gt; multiData = 8;</code> */
    public Builder putAllMultiData(
        java.util.Map<
                java.lang.String,
                com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb>
            values) {
      internalGetMutableMultiData().getMutableMap().putAll(values);
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

    // @@protoc_insertion_point(builder_scope:MultiReceivedDataPb)
  }

  // @@protoc_insertion_point(class_scope:MultiReceivedDataPb)
  private static final com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPb
      DEFAULT_INSTANCE;

  static {
    DEFAULT_INSTANCE = new com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPb();
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPb
      getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<MultiReceivedDataPb> PARSER =
      new com.google.protobuf.AbstractParser<MultiReceivedDataPb>() {
        @java.lang.Override
        public MultiReceivedDataPb parsePartialFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
          return new MultiReceivedDataPb(input, extensionRegistry);
        }
      };

  public static com.google.protobuf.Parser<MultiReceivedDataPb> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<MultiReceivedDataPb> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPb
      getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }
}
