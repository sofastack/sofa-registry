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

/** Protobuf type {@code DeltaReceivedDataBodyPb} */
public final class DeltaReceivedDataBodyPb extends com.google.protobuf.GeneratedMessageV3
    implements
    // @@protoc_insertion_point(message_implements:DeltaReceivedDataBodyPb)
    DeltaReceivedDataBodyPbOrBuilder {
  private static final long serialVersionUID = 0L;
  // Use DeltaReceivedDataBodyPb.newBuilder() to construct.
  private DeltaReceivedDataBodyPb(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }

  private DeltaReceivedDataBodyPb() {}

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
    return this.unknownFields;
  }

  private DeltaReceivedDataBodyPb(
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
                addPublishers_ =
                    com.google.protobuf.MapField.newMapField(
                        AddPublishersDefaultEntryHolder.defaultEntry);
                mutable_bitField0_ |= 0x00000001;
              }
              com.google.protobuf.MapEntry<
                      java.lang.String,
                      com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
                  addPublishers__ =
                      input.readMessage(
                          AddPublishersDefaultEntryHolder.defaultEntry.getParserForType(),
                          extensionRegistry);
              addPublishers_
                  .getMutableMap()
                  .put(addPublishers__.getKey(), addPublishers__.getValue());
              break;
            }
          case 18:
            {
              if (!((mutable_bitField0_ & 0x00000002) == 0x00000002)) {
                deletePublisherRegisterIds_ =
                    com.google.protobuf.MapField.newMapField(
                        DeletePublisherRegisterIdsDefaultEntryHolder.defaultEntry);
                mutable_bitField0_ |= 0x00000002;
              }
              com.google.protobuf.MapEntry<
                      java.lang.String,
                      com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb>
                  deletePublisherRegisterIds__ =
                      input.readMessage(
                          DeletePublisherRegisterIdsDefaultEntryHolder.defaultEntry
                              .getParserForType(),
                          extensionRegistry);
              deletePublisherRegisterIds_
                  .getMutableMap()
                  .put(
                      deletePublisherRegisterIds__.getKey(),
                      deletePublisherRegisterIds__.getValue());
              break;
            }
          case 26:
            {
              if (!((mutable_bitField0_ & 0x00000004) == 0x00000004)) {
                zoneDigests_ =
                    com.google.protobuf.MapField.newMapField(
                        ZoneDigestsDefaultEntryHolder.defaultEntry);
                mutable_bitField0_ |= 0x00000004;
              }
              com.google.protobuf.MapEntry<
                      java.lang.String,
                      com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb>
                  zoneDigests__ =
                      input.readMessage(
                          ZoneDigestsDefaultEntryHolder.defaultEntry.getParserForType(),
                          extensionRegistry);
              zoneDigests_.getMutableMap().put(zoneDigests__.getKey(), zoneDigests__.getValue());
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
    return com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPbOuterClass
        .internal_static_DeltaReceivedDataBodyPb_descriptor;
  }

  @SuppressWarnings({"rawtypes"})
  protected com.google.protobuf.MapField internalGetMapField(int number) {
    switch (number) {
      case 1:
        return internalGetAddPublishers();
      case 2:
        return internalGetDeletePublisherRegisterIds();
      case 3:
        return internalGetZoneDigests();
      default:
        throw new RuntimeException("Invalid map field number: " + number);
    }
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPbOuterClass
        .internal_static_DeltaReceivedDataBodyPb_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb.class,
            com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb.Builder.class);
  }

  public static final int ADDPUBLISHERS_FIELD_NUMBER = 1;

  private static final class AddPublishersDefaultEntryHolder {
    static final com.google.protobuf.MapEntry<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
        defaultEntry =
            com.google.protobuf.MapEntry
                .<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
                    newDefaultInstance(
                        com.alipay.sofa.registry.common.model.client.pb
                            .DeltaReceivedDataBodyPbOuterClass
                            .internal_static_DeltaReceivedDataBodyPb_AddPublishersEntry_descriptor,
                        com.google.protobuf.WireFormat.FieldType.STRING,
                        "",
                        com.google.protobuf.WireFormat.FieldType.MESSAGE,
                        com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb
                            .getDefaultInstance());
  }

  private com.google.protobuf.MapField<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
      addPublishers_;

  private com.google.protobuf.MapField<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
      internalGetAddPublishers() {
    if (addPublishers_ == null) {
      return com.google.protobuf.MapField.emptyMapField(
          AddPublishersDefaultEntryHolder.defaultEntry);
    }
    return addPublishers_;
  }

  public int getAddPublishersCount() {
    return internalGetAddPublishers().getMap().size();
  }
  /** <code>map&lt;string, .PushResourcesPb&gt; addPublishers = 1;</code> */
  public boolean containsAddPublishers(java.lang.String key) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    return internalGetAddPublishers().getMap().containsKey(key);
  }
  /** Use {@link #getAddPublishersMap()} instead. */
  @java.lang.Deprecated
  public java.util.Map<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
      getAddPublishers() {
    return getAddPublishersMap();
  }
  /** <code>map&lt;string, .PushResourcesPb&gt; addPublishers = 1;</code> */
  public java.util.Map<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
      getAddPublishersMap() {
    return internalGetAddPublishers().getMap();
  }
  /** <code>map&lt;string, .PushResourcesPb&gt; addPublishers = 1;</code> */
  public com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb getAddPublishersOrDefault(
      java.lang.String key,
      com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb defaultValue) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
        map = internalGetAddPublishers().getMap();
    return map.containsKey(key) ? map.get(key) : defaultValue;
  }
  /** <code>map&lt;string, .PushResourcesPb&gt; addPublishers = 1;</code> */
  public com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb getAddPublishersOrThrow(
      java.lang.String key) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
        map = internalGetAddPublishers().getMap();
    if (!map.containsKey(key)) {
      throw new java.lang.IllegalArgumentException();
    }
    return map.get(key);
  }

  public static final int DELETEPUBLISHERREGISTERIDS_FIELD_NUMBER = 2;

  private static final class DeletePublisherRegisterIdsDefaultEntryHolder {
    static final com.google.protobuf.MapEntry<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb>
        defaultEntry =
            com.google.protobuf.MapEntry
                .<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb>
                    newDefaultInstance(
                        com.alipay.sofa.registry.common.model.client.pb
                            .DeltaReceivedDataBodyPbOuterClass
                            .internal_static_DeltaReceivedDataBodyPb_DeletePublisherRegisterIdsEntry_descriptor,
                        com.google.protobuf.WireFormat.FieldType.STRING,
                        "",
                        com.google.protobuf.WireFormat.FieldType.MESSAGE,
                        com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb
                            .getDefaultInstance());
  }

  private com.google.protobuf.MapField<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb>
      deletePublisherRegisterIds_;

  private com.google.protobuf.MapField<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb>
      internalGetDeletePublisherRegisterIds() {
    if (deletePublisherRegisterIds_ == null) {
      return com.google.protobuf.MapField.emptyMapField(
          DeletePublisherRegisterIdsDefaultEntryHolder.defaultEntry);
    }
    return deletePublisherRegisterIds_;
  }

  public int getDeletePublisherRegisterIdsCount() {
    return internalGetDeletePublisherRegisterIds().getMap().size();
  }
  /** <code>map&lt;string, .RegisterIdsPb&gt; deletePublisherRegisterIds = 2;</code> */
  public boolean containsDeletePublisherRegisterIds(java.lang.String key) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    return internalGetDeletePublisherRegisterIds().getMap().containsKey(key);
  }
  /** Use {@link #getDeletePublisherRegisterIdsMap()} instead. */
  @java.lang.Deprecated
  public java.util.Map<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb>
      getDeletePublisherRegisterIds() {
    return getDeletePublisherRegisterIdsMap();
  }
  /** <code>map&lt;string, .RegisterIdsPb&gt; deletePublisherRegisterIds = 2;</code> */
  public java.util.Map<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb>
      getDeletePublisherRegisterIdsMap() {
    return internalGetDeletePublisherRegisterIds().getMap();
  }
  /** <code>map&lt;string, .RegisterIdsPb&gt; deletePublisherRegisterIds = 2;</code> */
  public com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb
      getDeletePublisherRegisterIdsOrDefault(
          java.lang.String key,
          com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb defaultValue) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb>
        map = internalGetDeletePublisherRegisterIds().getMap();
    return map.containsKey(key) ? map.get(key) : defaultValue;
  }
  /** <code>map&lt;string, .RegisterIdsPb&gt; deletePublisherRegisterIds = 2;</code> */
  public com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb
      getDeletePublisherRegisterIdsOrThrow(java.lang.String key) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb>
        map = internalGetDeletePublisherRegisterIds().getMap();
    if (!map.containsKey(key)) {
      throw new java.lang.IllegalArgumentException();
    }
    return map.get(key);
  }

  public static final int ZONEDIGESTS_FIELD_NUMBER = 3;

  private static final class ZoneDigestsDefaultEntryHolder {
    static final com.google.protobuf.MapEntry<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb>
        defaultEntry =
            com.google.protobuf.MapEntry
                .<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb>
                    newDefaultInstance(
                        com.alipay.sofa.registry.common.model.client.pb
                            .DeltaReceivedDataBodyPbOuterClass
                            .internal_static_DeltaReceivedDataBodyPb_ZoneDigestsEntry_descriptor,
                        com.google.protobuf.WireFormat.FieldType.STRING,
                        "",
                        com.google.protobuf.WireFormat.FieldType.MESSAGE,
                        com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb
                            .getDefaultInstance());
  }

  private com.google.protobuf.MapField<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb>
      zoneDigests_;

  private com.google.protobuf.MapField<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb>
      internalGetZoneDigests() {
    if (zoneDigests_ == null) {
      return com.google.protobuf.MapField.emptyMapField(ZoneDigestsDefaultEntryHolder.defaultEntry);
    }
    return zoneDigests_;
  }

  public int getZoneDigestsCount() {
    return internalGetZoneDigests().getMap().size();
  }
  /** <code>map&lt;string, .DeltaDigestPb&gt; zoneDigests = 3;</code> */
  public boolean containsZoneDigests(java.lang.String key) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    return internalGetZoneDigests().getMap().containsKey(key);
  }
  /** Use {@link #getZoneDigestsMap()} instead. */
  @java.lang.Deprecated
  public java.util.Map<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb>
      getZoneDigests() {
    return getZoneDigestsMap();
  }
  /** <code>map&lt;string, .DeltaDigestPb&gt; zoneDigests = 3;</code> */
  public java.util.Map<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb>
      getZoneDigestsMap() {
    return internalGetZoneDigests().getMap();
  }
  /** <code>map&lt;string, .DeltaDigestPb&gt; zoneDigests = 3;</code> */
  public com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb getZoneDigestsOrDefault(
      java.lang.String key,
      com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb defaultValue) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb>
        map = internalGetZoneDigests().getMap();
    return map.containsKey(key) ? map.get(key) : defaultValue;
  }
  /** <code>map&lt;string, .DeltaDigestPb&gt; zoneDigests = 3;</code> */
  public com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb getZoneDigestsOrThrow(
      java.lang.String key) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb>
        map = internalGetZoneDigests().getMap();
    if (!map.containsKey(key)) {
      throw new java.lang.IllegalArgumentException();
    }
    return map.get(key);
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
    com.google.protobuf.GeneratedMessageV3.serializeStringMapTo(
        output, internalGetAddPublishers(), AddPublishersDefaultEntryHolder.defaultEntry, 1);
    com.google.protobuf.GeneratedMessageV3.serializeStringMapTo(
        output,
        internalGetDeletePublisherRegisterIds(),
        DeletePublisherRegisterIdsDefaultEntryHolder.defaultEntry,
        2);
    com.google.protobuf.GeneratedMessageV3.serializeStringMapTo(
        output, internalGetZoneDigests(), ZoneDigestsDefaultEntryHolder.defaultEntry, 3);
    unknownFields.writeTo(output);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    for (java.util.Map.Entry<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
        entry : internalGetAddPublishers().getMap().entrySet()) {
      com.google.protobuf.MapEntry<
              java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
          addPublishers__ =
              AddPublishersDefaultEntryHolder.defaultEntry
                  .newBuilderForType()
                  .setKey(entry.getKey())
                  .setValue(entry.getValue())
                  .build();
      size += com.google.protobuf.CodedOutputStream.computeMessageSize(1, addPublishers__);
    }
    for (java.util.Map.Entry<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb>
        entry : internalGetDeletePublisherRegisterIds().getMap().entrySet()) {
      com.google.protobuf.MapEntry<
              java.lang.String, com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb>
          deletePublisherRegisterIds__ =
              DeletePublisherRegisterIdsDefaultEntryHolder.defaultEntry
                  .newBuilderForType()
                  .setKey(entry.getKey())
                  .setValue(entry.getValue())
                  .build();
      size +=
          com.google.protobuf.CodedOutputStream.computeMessageSize(2, deletePublisherRegisterIds__);
    }
    for (java.util.Map.Entry<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb>
        entry : internalGetZoneDigests().getMap().entrySet()) {
      com.google.protobuf.MapEntry<
              java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb>
          zoneDigests__ =
              ZoneDigestsDefaultEntryHolder.defaultEntry
                  .newBuilderForType()
                  .setKey(entry.getKey())
                  .setValue(entry.getValue())
                  .build();
      size += com.google.protobuf.CodedOutputStream.computeMessageSize(3, zoneDigests__);
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
    if (!(obj instanceof com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb)) {
      return super.equals(obj);
    }
    com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb other =
        (com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb) obj;

    boolean result = true;
    result = result && internalGetAddPublishers().equals(other.internalGetAddPublishers());
    result =
        result
            && internalGetDeletePublisherRegisterIds()
                .equals(other.internalGetDeletePublisherRegisterIds());
    result = result && internalGetZoneDigests().equals(other.internalGetZoneDigests());
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
    if (!internalGetAddPublishers().getMap().isEmpty()) {
      hash = (37 * hash) + ADDPUBLISHERS_FIELD_NUMBER;
      hash = (53 * hash) + internalGetAddPublishers().hashCode();
    }
    if (!internalGetDeletePublisherRegisterIds().getMap().isEmpty()) {
      hash = (37 * hash) + DELETEPUBLISHERREGISTERIDS_FIELD_NUMBER;
      hash = (53 * hash) + internalGetDeletePublisherRegisterIds().hashCode();
    }
    if (!internalGetZoneDigests().getMap().isEmpty()) {
      hash = (37 * hash) + ZONEDIGESTS_FIELD_NUMBER;
      hash = (53 * hash) + internalGetZoneDigests().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb parseFrom(
      java.nio.ByteBuffer data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb parseFrom(
      java.nio.ByteBuffer data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb parseFrom(
      byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb parseFrom(
      byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb parseFrom(
      java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb parseFrom(
      java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb
      parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb
      parseDelimitedFrom(
          java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb parseFrom(
      com.google.protobuf.CodedInputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb parseFrom(
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
      com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb prototype) {
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
  /** Protobuf type {@code DeltaReceivedDataBodyPb} */
  public static final class Builder extends com.google.protobuf.GeneratedMessageV3.Builder<Builder>
      implements
      // @@protoc_insertion_point(builder_implements:DeltaReceivedDataBodyPb)
      com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPbOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
      return com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPbOuterClass
          .internal_static_DeltaReceivedDataBodyPb_descriptor;
    }

    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapField internalGetMapField(int number) {
      switch (number) {
        case 1:
          return internalGetAddPublishers();
        case 2:
          return internalGetDeletePublisherRegisterIds();
        case 3:
          return internalGetZoneDigests();
        default:
          throw new RuntimeException("Invalid map field number: " + number);
      }
    }

    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapField internalGetMutableMapField(int number) {
      switch (number) {
        case 1:
          return internalGetMutableAddPublishers();
        case 2:
          return internalGetMutableDeletePublisherRegisterIds();
        case 3:
          return internalGetMutableZoneDigests();
        default:
          throw new RuntimeException("Invalid map field number: " + number);
      }
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPbOuterClass
          .internal_static_DeltaReceivedDataBodyPb_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb.class,
              com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb.Builder
                  .class);
    }

    // Construct using
    // com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb.newBuilder()
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
      internalGetMutableAddPublishers().clear();
      internalGetMutableDeletePublisherRegisterIds().clear();
      internalGetMutableZoneDigests().clear();
      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
      return com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPbOuterClass
          .internal_static_DeltaReceivedDataBodyPb_descriptor;
    }

    public com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb
        getDefaultInstanceForType() {
      return com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb
          .getDefaultInstance();
    }

    public com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb build() {
      com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb result =
          buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb buildPartial() {
      com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb result =
          new com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb(this);
      int from_bitField0_ = bitField0_;
      result.addPublishers_ = internalGetAddPublishers();
      result.addPublishers_.makeImmutable();
      result.deletePublisherRegisterIds_ = internalGetDeletePublisherRegisterIds();
      result.deletePublisherRegisterIds_.makeImmutable();
      result.zoneDigests_ = internalGetZoneDigests();
      result.zoneDigests_.makeImmutable();
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
      if (other
          instanceof com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb) {
        return mergeFrom(
            (com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb) other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(
        com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb other) {
      if (other
          == com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb
              .getDefaultInstance()) return this;
      internalGetMutableAddPublishers().mergeFrom(other.internalGetAddPublishers());
      internalGetMutableDeletePublisherRegisterIds()
          .mergeFrom(other.internalGetDeletePublisherRegisterIds());
      internalGetMutableZoneDigests().mergeFrom(other.internalGetZoneDigests());
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
      com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage =
            (com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb)
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

    private com.google.protobuf.MapField<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
        addPublishers_;

    private com.google.protobuf.MapField<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
        internalGetAddPublishers() {
      if (addPublishers_ == null) {
        return com.google.protobuf.MapField.emptyMapField(
            AddPublishersDefaultEntryHolder.defaultEntry);
      }
      return addPublishers_;
    }

    private com.google.protobuf.MapField<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
        internalGetMutableAddPublishers() {
      onChanged();
      ;
      if (addPublishers_ == null) {
        addPublishers_ =
            com.google.protobuf.MapField.newMapField(AddPublishersDefaultEntryHolder.defaultEntry);
      }
      if (!addPublishers_.isMutable()) {
        addPublishers_ = addPublishers_.copy();
      }
      return addPublishers_;
    }

    public int getAddPublishersCount() {
      return internalGetAddPublishers().getMap().size();
    }
    /** <code>map&lt;string, .PushResourcesPb&gt; addPublishers = 1;</code> */
    public boolean containsAddPublishers(java.lang.String key) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      return internalGetAddPublishers().getMap().containsKey(key);
    }
    /** Use {@link #getAddPublishersMap()} instead. */
    @java.lang.Deprecated
    public java.util.Map<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
        getAddPublishers() {
      return getAddPublishersMap();
    }
    /** <code>map&lt;string, .PushResourcesPb&gt; addPublishers = 1;</code> */
    public java.util.Map<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
        getAddPublishersMap() {
      return internalGetAddPublishers().getMap();
    }
    /** <code>map&lt;string, .PushResourcesPb&gt; addPublishers = 1;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb
        getAddPublishersOrDefault(
            java.lang.String key,
            com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb defaultValue) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      java.util.Map<
              java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
          map = internalGetAddPublishers().getMap();
      return map.containsKey(key) ? map.get(key) : defaultValue;
    }
    /** <code>map&lt;string, .PushResourcesPb&gt; addPublishers = 1;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb getAddPublishersOrThrow(
        java.lang.String key) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      java.util.Map<
              java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
          map = internalGetAddPublishers().getMap();
      if (!map.containsKey(key)) {
        throw new java.lang.IllegalArgumentException();
      }
      return map.get(key);
    }

    public Builder clearAddPublishers() {
      internalGetMutableAddPublishers().getMutableMap().clear();
      return this;
    }
    /** <code>map&lt;string, .PushResourcesPb&gt; addPublishers = 1;</code> */
    public Builder removeAddPublishers(java.lang.String key) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      internalGetMutableAddPublishers().getMutableMap().remove(key);
      return this;
    }
    /** Use alternate mutation accessors instead. */
    @java.lang.Deprecated
    public java.util.Map<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
        getMutableAddPublishers() {
      return internalGetMutableAddPublishers().getMutableMap();
    }
    /** <code>map&lt;string, .PushResourcesPb&gt; addPublishers = 1;</code> */
    public Builder putAddPublishers(
        java.lang.String key,
        com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb value) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      if (value == null) {
        throw new java.lang.NullPointerException();
      }
      internalGetMutableAddPublishers().getMutableMap().put(key, value);
      return this;
    }
    /** <code>map&lt;string, .PushResourcesPb&gt; addPublishers = 1;</code> */
    public Builder putAllAddPublishers(
        java.util.Map<
                java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
            values) {
      internalGetMutableAddPublishers().getMutableMap().putAll(values);
      return this;
    }

    private com.google.protobuf.MapField<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb>
        deletePublisherRegisterIds_;

    private com.google.protobuf.MapField<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb>
        internalGetDeletePublisherRegisterIds() {
      if (deletePublisherRegisterIds_ == null) {
        return com.google.protobuf.MapField.emptyMapField(
            DeletePublisherRegisterIdsDefaultEntryHolder.defaultEntry);
      }
      return deletePublisherRegisterIds_;
    }

    private com.google.protobuf.MapField<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb>
        internalGetMutableDeletePublisherRegisterIds() {
      onChanged();
      ;
      if (deletePublisherRegisterIds_ == null) {
        deletePublisherRegisterIds_ =
            com.google.protobuf.MapField.newMapField(
                DeletePublisherRegisterIdsDefaultEntryHolder.defaultEntry);
      }
      if (!deletePublisherRegisterIds_.isMutable()) {
        deletePublisherRegisterIds_ = deletePublisherRegisterIds_.copy();
      }
      return deletePublisherRegisterIds_;
    }

    public int getDeletePublisherRegisterIdsCount() {
      return internalGetDeletePublisherRegisterIds().getMap().size();
    }
    /** <code>map&lt;string, .RegisterIdsPb&gt; deletePublisherRegisterIds = 2;</code> */
    public boolean containsDeletePublisherRegisterIds(java.lang.String key) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      return internalGetDeletePublisherRegisterIds().getMap().containsKey(key);
    }
    /** Use {@link #getDeletePublisherRegisterIdsMap()} instead. */
    @java.lang.Deprecated
    public java.util.Map<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb>
        getDeletePublisherRegisterIds() {
      return getDeletePublisherRegisterIdsMap();
    }
    /** <code>map&lt;string, .RegisterIdsPb&gt; deletePublisherRegisterIds = 2;</code> */
    public java.util.Map<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb>
        getDeletePublisherRegisterIdsMap() {
      return internalGetDeletePublisherRegisterIds().getMap();
    }
    /** <code>map&lt;string, .RegisterIdsPb&gt; deletePublisherRegisterIds = 2;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb
        getDeletePublisherRegisterIdsOrDefault(
            java.lang.String key,
            com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb defaultValue) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb>
          map = internalGetDeletePublisherRegisterIds().getMap();
      return map.containsKey(key) ? map.get(key) : defaultValue;
    }
    /** <code>map&lt;string, .RegisterIdsPb&gt; deletePublisherRegisterIds = 2;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb
        getDeletePublisherRegisterIdsOrThrow(java.lang.String key) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb>
          map = internalGetDeletePublisherRegisterIds().getMap();
      if (!map.containsKey(key)) {
        throw new java.lang.IllegalArgumentException();
      }
      return map.get(key);
    }

    public Builder clearDeletePublisherRegisterIds() {
      internalGetMutableDeletePublisherRegisterIds().getMutableMap().clear();
      return this;
    }
    /** <code>map&lt;string, .RegisterIdsPb&gt; deletePublisherRegisterIds = 2;</code> */
    public Builder removeDeletePublisherRegisterIds(java.lang.String key) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      internalGetMutableDeletePublisherRegisterIds().getMutableMap().remove(key);
      return this;
    }
    /** Use alternate mutation accessors instead. */
    @java.lang.Deprecated
    public java.util.Map<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb>
        getMutableDeletePublisherRegisterIds() {
      return internalGetMutableDeletePublisherRegisterIds().getMutableMap();
    }
    /** <code>map&lt;string, .RegisterIdsPb&gt; deletePublisherRegisterIds = 2;</code> */
    public Builder putDeletePublisherRegisterIds(
        java.lang.String key, com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb value) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      if (value == null) {
        throw new java.lang.NullPointerException();
      }
      internalGetMutableDeletePublisherRegisterIds().getMutableMap().put(key, value);
      return this;
    }
    /** <code>map&lt;string, .RegisterIdsPb&gt; deletePublisherRegisterIds = 2;</code> */
    public Builder putAllDeletePublisherRegisterIds(
        java.util.Map<
                java.lang.String, com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb>
            values) {
      internalGetMutableDeletePublisherRegisterIds().getMutableMap().putAll(values);
      return this;
    }

    private com.google.protobuf.MapField<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb>
        zoneDigests_;

    private com.google.protobuf.MapField<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb>
        internalGetZoneDigests() {
      if (zoneDigests_ == null) {
        return com.google.protobuf.MapField.emptyMapField(
            ZoneDigestsDefaultEntryHolder.defaultEntry);
      }
      return zoneDigests_;
    }

    private com.google.protobuf.MapField<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb>
        internalGetMutableZoneDigests() {
      onChanged();
      ;
      if (zoneDigests_ == null) {
        zoneDigests_ =
            com.google.protobuf.MapField.newMapField(ZoneDigestsDefaultEntryHolder.defaultEntry);
      }
      if (!zoneDigests_.isMutable()) {
        zoneDigests_ = zoneDigests_.copy();
      }
      return zoneDigests_;
    }

    public int getZoneDigestsCount() {
      return internalGetZoneDigests().getMap().size();
    }
    /** <code>map&lt;string, .DeltaDigestPb&gt; zoneDigests = 3;</code> */
    public boolean containsZoneDigests(java.lang.String key) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      return internalGetZoneDigests().getMap().containsKey(key);
    }
    /** Use {@link #getZoneDigestsMap()} instead. */
    @java.lang.Deprecated
    public java.util.Map<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb>
        getZoneDigests() {
      return getZoneDigestsMap();
    }
    /** <code>map&lt;string, .DeltaDigestPb&gt; zoneDigests = 3;</code> */
    public java.util.Map<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb>
        getZoneDigestsMap() {
      return internalGetZoneDigests().getMap();
    }
    /** <code>map&lt;string, .DeltaDigestPb&gt; zoneDigests = 3;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb getZoneDigestsOrDefault(
        java.lang.String key,
        com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb defaultValue) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb>
          map = internalGetZoneDigests().getMap();
      return map.containsKey(key) ? map.get(key) : defaultValue;
    }
    /** <code>map&lt;string, .DeltaDigestPb&gt; zoneDigests = 3;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb getZoneDigestsOrThrow(
        java.lang.String key) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb>
          map = internalGetZoneDigests().getMap();
      if (!map.containsKey(key)) {
        throw new java.lang.IllegalArgumentException();
      }
      return map.get(key);
    }

    public Builder clearZoneDigests() {
      internalGetMutableZoneDigests().getMutableMap().clear();
      return this;
    }
    /** <code>map&lt;string, .DeltaDigestPb&gt; zoneDigests = 3;</code> */
    public Builder removeZoneDigests(java.lang.String key) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      internalGetMutableZoneDigests().getMutableMap().remove(key);
      return this;
    }
    /** Use alternate mutation accessors instead. */
    @java.lang.Deprecated
    public java.util.Map<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb>
        getMutableZoneDigests() {
      return internalGetMutableZoneDigests().getMutableMap();
    }
    /** <code>map&lt;string, .DeltaDigestPb&gt; zoneDigests = 3;</code> */
    public Builder putZoneDigests(
        java.lang.String key, com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb value) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      if (value == null) {
        throw new java.lang.NullPointerException();
      }
      internalGetMutableZoneDigests().getMutableMap().put(key, value);
      return this;
    }
    /** <code>map&lt;string, .DeltaDigestPb&gt; zoneDigests = 3;</code> */
    public Builder putAllZoneDigests(
        java.util.Map<
                java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb>
            values) {
      internalGetMutableZoneDigests().getMutableMap().putAll(values);
      return this;
    }

    public final Builder setUnknownFields(final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFieldsProto3(unknownFields);
    }

    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }

    // @@protoc_insertion_point(builder_scope:DeltaReceivedDataBodyPb)
  }

  // @@protoc_insertion_point(class_scope:DeltaReceivedDataBodyPb)
  private static final com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb
      DEFAULT_INSTANCE;

  static {
    DEFAULT_INSTANCE =
        new com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb();
  }

  public static com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb
      getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<DeltaReceivedDataBodyPb> PARSER =
      new com.google.protobuf.AbstractParser<DeltaReceivedDataBodyPb>() {
        public DeltaReceivedDataBodyPb parsePartialFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
          return new DeltaReceivedDataBodyPb(input, extensionRegistry);
        }
      };

  public static com.google.protobuf.Parser<DeltaReceivedDataBodyPb> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<DeltaReceivedDataBodyPb> getParserForType() {
    return PARSER;
  }

  public com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb
      getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }
}
