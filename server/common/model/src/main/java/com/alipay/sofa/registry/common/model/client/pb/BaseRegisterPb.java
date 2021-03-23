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

/** Protobuf type {@code BaseRegisterPb} */
public final class BaseRegisterPb extends com.google.protobuf.GeneratedMessageV3
    implements
    // @@protoc_insertion_point(message_implements:BaseRegisterPb)
    BaseRegisterPbOrBuilder {
  private static final long serialVersionUID = 0L;
  // Use BaseRegisterPb.newBuilder() to construct.
  private BaseRegisterPb(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }

  private BaseRegisterPb() {
    instanceId_ = "";
    zone_ = "";
    appName_ = "";
    dataId_ = "";
    group_ = "";
    processId_ = "";
    registId_ = "";
    clientId_ = "";
    dataInfoId_ = "";
    ip_ = "";
    port_ = 0;
    eventType_ = "";
    version_ = 0L;
    timestamp_ = 0L;
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
    return this.unknownFields;
  }

  private BaseRegisterPb(
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

              instanceId_ = s;
              break;
            }
          case 18:
            {
              java.lang.String s = input.readStringRequireUtf8();

              zone_ = s;
              break;
            }
          case 26:
            {
              java.lang.String s = input.readStringRequireUtf8();

              appName_ = s;
              break;
            }
          case 34:
            {
              java.lang.String s = input.readStringRequireUtf8();

              dataId_ = s;
              break;
            }
          case 42:
            {
              java.lang.String s = input.readStringRequireUtf8();

              group_ = s;
              break;
            }
          case 50:
            {
              java.lang.String s = input.readStringRequireUtf8();

              processId_ = s;
              break;
            }
          case 58:
            {
              java.lang.String s = input.readStringRequireUtf8();

              registId_ = s;
              break;
            }
          case 66:
            {
              java.lang.String s = input.readStringRequireUtf8();

              clientId_ = s;
              break;
            }
          case 74:
            {
              java.lang.String s = input.readStringRequireUtf8();

              dataInfoId_ = s;
              break;
            }
          case 82:
            {
              java.lang.String s = input.readStringRequireUtf8();

              ip_ = s;
              break;
            }
          case 88:
            {
              port_ = input.readInt32();
              break;
            }
          case 98:
            {
              java.lang.String s = input.readStringRequireUtf8();

              eventType_ = s;
              break;
            }
          case 104:
            {
              version_ = input.readInt64();
              break;
            }
          case 112:
            {
              timestamp_ = input.readInt64();
              break;
            }
          case 122:
            {
              if (!((mutable_bitField0_ & 0x00004000) == 0x00004000)) {
                attributes_ =
                    com.google.protobuf.MapField.newMapField(
                        AttributesDefaultEntryHolder.defaultEntry);
                mutable_bitField0_ |= 0x00004000;
              }
              com.google.protobuf.MapEntry<java.lang.String, java.lang.String> attributes__ =
                  input.readMessage(
                      AttributesDefaultEntryHolder.defaultEntry.getParserForType(),
                      extensionRegistry);
              attributes_.getMutableMap().put(attributes__.getKey(), attributes__.getValue());
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
    return com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPbOuterClass
        .internal_static_BaseRegisterPb_descriptor;
  }

  @SuppressWarnings({"rawtypes"})
  protected com.google.protobuf.MapField internalGetMapField(int number) {
    switch (number) {
      case 15:
        return internalGetAttributes();
      default:
        throw new RuntimeException("Invalid map field number: " + number);
    }
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPbOuterClass
        .internal_static_BaseRegisterPb_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb.class,
            com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb.Builder.class);
  }

  private int bitField0_;
  public static final int INSTANCEID_FIELD_NUMBER = 1;
  private volatile java.lang.Object instanceId_;
  /** <code>string instanceId = 1;</code> */
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
  /** <code>string instanceId = 1;</code> */
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

  public static final int ZONE_FIELD_NUMBER = 2;
  private volatile java.lang.Object zone_;
  /** <code>string zone = 2;</code> */
  public java.lang.String getZone() {
    java.lang.Object ref = zone_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      zone_ = s;
      return s;
    }
  }
  /** <code>string zone = 2;</code> */
  public com.google.protobuf.ByteString getZoneBytes() {
    java.lang.Object ref = zone_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b =
          com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
      zone_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int APPNAME_FIELD_NUMBER = 3;
  private volatile java.lang.Object appName_;
  /** <code>string appName = 3;</code> */
  public java.lang.String getAppName() {
    java.lang.Object ref = appName_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      appName_ = s;
      return s;
    }
  }
  /** <code>string appName = 3;</code> */
  public com.google.protobuf.ByteString getAppNameBytes() {
    java.lang.Object ref = appName_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b =
          com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
      appName_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int DATAID_FIELD_NUMBER = 4;
  private volatile java.lang.Object dataId_;
  /** <code>string dataId = 4;</code> */
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
  /** <code>string dataId = 4;</code> */
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

  public static final int GROUP_FIELD_NUMBER = 5;
  private volatile java.lang.Object group_;
  /** <code>string group = 5;</code> */
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
  /** <code>string group = 5;</code> */
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

  public static final int PROCESSID_FIELD_NUMBER = 6;
  private volatile java.lang.Object processId_;
  /** <code>string processId = 6;</code> */
  public java.lang.String getProcessId() {
    java.lang.Object ref = processId_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      processId_ = s;
      return s;
    }
  }
  /** <code>string processId = 6;</code> */
  public com.google.protobuf.ByteString getProcessIdBytes() {
    java.lang.Object ref = processId_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b =
          com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
      processId_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int REGISTID_FIELD_NUMBER = 7;
  private volatile java.lang.Object registId_;
  /** <code>string registId = 7;</code> */
  public java.lang.String getRegistId() {
    java.lang.Object ref = registId_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      registId_ = s;
      return s;
    }
  }
  /** <code>string registId = 7;</code> */
  public com.google.protobuf.ByteString getRegistIdBytes() {
    java.lang.Object ref = registId_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b =
          com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
      registId_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int CLIENTID_FIELD_NUMBER = 8;
  private volatile java.lang.Object clientId_;
  /** <code>string clientId = 8;</code> */
  public java.lang.String getClientId() {
    java.lang.Object ref = clientId_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      clientId_ = s;
      return s;
    }
  }
  /** <code>string clientId = 8;</code> */
  public com.google.protobuf.ByteString getClientIdBytes() {
    java.lang.Object ref = clientId_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b =
          com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
      clientId_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int DATAINFOID_FIELD_NUMBER = 9;
  private volatile java.lang.Object dataInfoId_;
  /** <code>string dataInfoId = 9;</code> */
  public java.lang.String getDataInfoId() {
    java.lang.Object ref = dataInfoId_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      dataInfoId_ = s;
      return s;
    }
  }
  /** <code>string dataInfoId = 9;</code> */
  public com.google.protobuf.ByteString getDataInfoIdBytes() {
    java.lang.Object ref = dataInfoId_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b =
          com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
      dataInfoId_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int IP_FIELD_NUMBER = 10;
  private volatile java.lang.Object ip_;
  /** <code>string ip = 10;</code> */
  public java.lang.String getIp() {
    java.lang.Object ref = ip_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      ip_ = s;
      return s;
    }
  }
  /** <code>string ip = 10;</code> */
  public com.google.protobuf.ByteString getIpBytes() {
    java.lang.Object ref = ip_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b =
          com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
      ip_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int PORT_FIELD_NUMBER = 11;
  private int port_;
  /** <code>int32 port = 11;</code> */
  public int getPort() {
    return port_;
  }

  public static final int EVENTTYPE_FIELD_NUMBER = 12;
  private volatile java.lang.Object eventType_;
  /** <code>string eventType = 12;</code> */
  public java.lang.String getEventType() {
    java.lang.Object ref = eventType_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      eventType_ = s;
      return s;
    }
  }
  /** <code>string eventType = 12;</code> */
  public com.google.protobuf.ByteString getEventTypeBytes() {
    java.lang.Object ref = eventType_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b =
          com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
      eventType_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int VERSION_FIELD_NUMBER = 13;
  private long version_;
  /** <code>int64 version = 13;</code> */
  public long getVersion() {
    return version_;
  }

  public static final int TIMESTAMP_FIELD_NUMBER = 14;
  private long timestamp_;
  /** <code>int64 timestamp = 14;</code> */
  public long getTimestamp() {
    return timestamp_;
  }

  public static final int ATTRIBUTES_FIELD_NUMBER = 15;

  private static final class AttributesDefaultEntryHolder {
    static final com.google.protobuf.MapEntry<java.lang.String, java.lang.String> defaultEntry =
        com.google.protobuf.MapEntry.<java.lang.String, java.lang.String>newDefaultInstance(
            com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPbOuterClass
                .internal_static_BaseRegisterPb_AttributesEntry_descriptor,
            com.google.protobuf.WireFormat.FieldType.STRING,
            "",
            com.google.protobuf.WireFormat.FieldType.STRING,
            "");
  }

  private com.google.protobuf.MapField<java.lang.String, java.lang.String> attributes_;

  private com.google.protobuf.MapField<java.lang.String, java.lang.String> internalGetAttributes() {
    if (attributes_ == null) {
      return com.google.protobuf.MapField.emptyMapField(AttributesDefaultEntryHolder.defaultEntry);
    }
    return attributes_;
  }

  public int getAttributesCount() {
    return internalGetAttributes().getMap().size();
  }
  /** <code>map&lt;string, string&gt; attributes = 15;</code> */
  public boolean containsAttributes(java.lang.String key) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    return internalGetAttributes().getMap().containsKey(key);
  }
  /** Use {@link #getAttributesMap()} instead. */
  @java.lang.Deprecated
  public java.util.Map<java.lang.String, java.lang.String> getAttributes() {
    return getAttributesMap();
  }
  /** <code>map&lt;string, string&gt; attributes = 15;</code> */
  public java.util.Map<java.lang.String, java.lang.String> getAttributesMap() {
    return internalGetAttributes().getMap();
  }
  /** <code>map&lt;string, string&gt; attributes = 15;</code> */
  public java.lang.String getAttributesOrDefault(
      java.lang.String key, java.lang.String defaultValue) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    java.util.Map<java.lang.String, java.lang.String> map = internalGetAttributes().getMap();
    return map.containsKey(key) ? map.get(key) : defaultValue;
  }
  /** <code>map&lt;string, string&gt; attributes = 15;</code> */
  public java.lang.String getAttributesOrThrow(java.lang.String key) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    java.util.Map<java.lang.String, java.lang.String> map = internalGetAttributes().getMap();
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
    if (!getInstanceIdBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, instanceId_);
    }
    if (!getZoneBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 2, zone_);
    }
    if (!getAppNameBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 3, appName_);
    }
    if (!getDataIdBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 4, dataId_);
    }
    if (!getGroupBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 5, group_);
    }
    if (!getProcessIdBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 6, processId_);
    }
    if (!getRegistIdBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 7, registId_);
    }
    if (!getClientIdBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 8, clientId_);
    }
    if (!getDataInfoIdBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 9, dataInfoId_);
    }
    if (!getIpBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 10, ip_);
    }
    if (port_ != 0) {
      output.writeInt32(11, port_);
    }
    if (!getEventTypeBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 12, eventType_);
    }
    if (version_ != 0L) {
      output.writeInt64(13, version_);
    }
    if (timestamp_ != 0L) {
      output.writeInt64(14, timestamp_);
    }
    com.google.protobuf.GeneratedMessageV3.serializeStringMapTo(
        output, internalGetAttributes(), AttributesDefaultEntryHolder.defaultEntry, 15);
    unknownFields.writeTo(output);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!getInstanceIdBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, instanceId_);
    }
    if (!getZoneBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, zone_);
    }
    if (!getAppNameBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(3, appName_);
    }
    if (!getDataIdBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(4, dataId_);
    }
    if (!getGroupBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(5, group_);
    }
    if (!getProcessIdBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(6, processId_);
    }
    if (!getRegistIdBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(7, registId_);
    }
    if (!getClientIdBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(8, clientId_);
    }
    if (!getDataInfoIdBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(9, dataInfoId_);
    }
    if (!getIpBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(10, ip_);
    }
    if (port_ != 0) {
      size += com.google.protobuf.CodedOutputStream.computeInt32Size(11, port_);
    }
    if (!getEventTypeBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(12, eventType_);
    }
    if (version_ != 0L) {
      size += com.google.protobuf.CodedOutputStream.computeInt64Size(13, version_);
    }
    if (timestamp_ != 0L) {
      size += com.google.protobuf.CodedOutputStream.computeInt64Size(14, timestamp_);
    }
    for (java.util.Map.Entry<java.lang.String, java.lang.String> entry :
        internalGetAttributes().getMap().entrySet()) {
      com.google.protobuf.MapEntry<java.lang.String, java.lang.String> attributes__ =
          AttributesDefaultEntryHolder.defaultEntry
              .newBuilderForType()
              .setKey(entry.getKey())
              .setValue(entry.getValue())
              .build();
      size += com.google.protobuf.CodedOutputStream.computeMessageSize(15, attributes__);
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
    if (!(obj instanceof com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb)) {
      return super.equals(obj);
    }
    com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb other =
        (com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb) obj;

    boolean result = true;
    result = result && getInstanceId().equals(other.getInstanceId());
    result = result && getZone().equals(other.getZone());
    result = result && getAppName().equals(other.getAppName());
    result = result && getDataId().equals(other.getDataId());
    result = result && getGroup().equals(other.getGroup());
    result = result && getProcessId().equals(other.getProcessId());
    result = result && getRegistId().equals(other.getRegistId());
    result = result && getClientId().equals(other.getClientId());
    result = result && getDataInfoId().equals(other.getDataInfoId());
    result = result && getIp().equals(other.getIp());
    result = result && (getPort() == other.getPort());
    result = result && getEventType().equals(other.getEventType());
    result = result && (getVersion() == other.getVersion());
    result = result && (getTimestamp() == other.getTimestamp());
    result = result && internalGetAttributes().equals(other.internalGetAttributes());
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
    hash = (37 * hash) + INSTANCEID_FIELD_NUMBER;
    hash = (53 * hash) + getInstanceId().hashCode();
    hash = (37 * hash) + ZONE_FIELD_NUMBER;
    hash = (53 * hash) + getZone().hashCode();
    hash = (37 * hash) + APPNAME_FIELD_NUMBER;
    hash = (53 * hash) + getAppName().hashCode();
    hash = (37 * hash) + DATAID_FIELD_NUMBER;
    hash = (53 * hash) + getDataId().hashCode();
    hash = (37 * hash) + GROUP_FIELD_NUMBER;
    hash = (53 * hash) + getGroup().hashCode();
    hash = (37 * hash) + PROCESSID_FIELD_NUMBER;
    hash = (53 * hash) + getProcessId().hashCode();
    hash = (37 * hash) + REGISTID_FIELD_NUMBER;
    hash = (53 * hash) + getRegistId().hashCode();
    hash = (37 * hash) + CLIENTID_FIELD_NUMBER;
    hash = (53 * hash) + getClientId().hashCode();
    hash = (37 * hash) + DATAINFOID_FIELD_NUMBER;
    hash = (53 * hash) + getDataInfoId().hashCode();
    hash = (37 * hash) + IP_FIELD_NUMBER;
    hash = (53 * hash) + getIp().hashCode();
    hash = (37 * hash) + PORT_FIELD_NUMBER;
    hash = (53 * hash) + getPort();
    hash = (37 * hash) + EVENTTYPE_FIELD_NUMBER;
    hash = (53 * hash) + getEventType().hashCode();
    hash = (37 * hash) + VERSION_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(getVersion());
    hash = (37 * hash) + TIMESTAMP_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(getTimestamp());
    if (!internalGetAttributes().getMap().isEmpty()) {
      hash = (37 * hash) + ATTRIBUTES_FIELD_NUMBER;
      hash = (53 * hash) + internalGetAttributes().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb parseFrom(
      java.nio.ByteBuffer data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb parseFrom(
      java.nio.ByteBuffer data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb parseFrom(
      byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb parseFrom(
      byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb parseFrom(
      java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb parseFrom(
      java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb parseDelimitedFrom(
      java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb parseDelimitedFrom(
      java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb parseFrom(
      com.google.protobuf.CodedInputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb parseFrom(
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
      com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb prototype) {
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
  /** Protobuf type {@code BaseRegisterPb} */
  public static final class Builder extends com.google.protobuf.GeneratedMessageV3.Builder<Builder>
      implements
      // @@protoc_insertion_point(builder_implements:BaseRegisterPb)
      com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPbOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
      return com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPbOuterClass
          .internal_static_BaseRegisterPb_descriptor;
    }

    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapField internalGetMapField(int number) {
      switch (number) {
        case 15:
          return internalGetAttributes();
        default:
          throw new RuntimeException("Invalid map field number: " + number);
      }
    }

    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapField internalGetMutableMapField(int number) {
      switch (number) {
        case 15:
          return internalGetMutableAttributes();
        default:
          throw new RuntimeException("Invalid map field number: " + number);
      }
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPbOuterClass
          .internal_static_BaseRegisterPb_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb.class,
              com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb.Builder.class);
    }

    // Construct using com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb.newBuilder()
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
      instanceId_ = "";

      zone_ = "";

      appName_ = "";

      dataId_ = "";

      group_ = "";

      processId_ = "";

      registId_ = "";

      clientId_ = "";

      dataInfoId_ = "";

      ip_ = "";

      port_ = 0;

      eventType_ = "";

      version_ = 0L;

      timestamp_ = 0L;

      internalGetMutableAttributes().clear();
      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
      return com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPbOuterClass
          .internal_static_BaseRegisterPb_descriptor;
    }

    public com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb
        getDefaultInstanceForType() {
      return com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb.getDefaultInstance();
    }

    public com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb build() {
      com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb buildPartial() {
      com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb result =
          new com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb(this);
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      result.instanceId_ = instanceId_;
      result.zone_ = zone_;
      result.appName_ = appName_;
      result.dataId_ = dataId_;
      result.group_ = group_;
      result.processId_ = processId_;
      result.registId_ = registId_;
      result.clientId_ = clientId_;
      result.dataInfoId_ = dataInfoId_;
      result.ip_ = ip_;
      result.port_ = port_;
      result.eventType_ = eventType_;
      result.version_ = version_;
      result.timestamp_ = timestamp_;
      result.attributes_ = internalGetAttributes();
      result.attributes_.makeImmutable();
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
      if (other instanceof com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb) {
        return mergeFrom((com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb) other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb other) {
      if (other
          == com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb.getDefaultInstance())
        return this;
      if (!other.getInstanceId().isEmpty()) {
        instanceId_ = other.instanceId_;
        onChanged();
      }
      if (!other.getZone().isEmpty()) {
        zone_ = other.zone_;
        onChanged();
      }
      if (!other.getAppName().isEmpty()) {
        appName_ = other.appName_;
        onChanged();
      }
      if (!other.getDataId().isEmpty()) {
        dataId_ = other.dataId_;
        onChanged();
      }
      if (!other.getGroup().isEmpty()) {
        group_ = other.group_;
        onChanged();
      }
      if (!other.getProcessId().isEmpty()) {
        processId_ = other.processId_;
        onChanged();
      }
      if (!other.getRegistId().isEmpty()) {
        registId_ = other.registId_;
        onChanged();
      }
      if (!other.getClientId().isEmpty()) {
        clientId_ = other.clientId_;
        onChanged();
      }
      if (!other.getDataInfoId().isEmpty()) {
        dataInfoId_ = other.dataInfoId_;
        onChanged();
      }
      if (!other.getIp().isEmpty()) {
        ip_ = other.ip_;
        onChanged();
      }
      if (other.getPort() != 0) {
        setPort(other.getPort());
      }
      if (!other.getEventType().isEmpty()) {
        eventType_ = other.eventType_;
        onChanged();
      }
      if (other.getVersion() != 0L) {
        setVersion(other.getVersion());
      }
      if (other.getTimestamp() != 0L) {
        setTimestamp(other.getTimestamp());
      }
      internalGetMutableAttributes().mergeFrom(other.internalGetAttributes());
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
      com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage =
            (com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb)
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

    private java.lang.Object instanceId_ = "";
    /** <code>string instanceId = 1;</code> */
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
    /** <code>string instanceId = 1;</code> */
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
    /** <code>string instanceId = 1;</code> */
    public Builder setInstanceId(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      instanceId_ = value;
      onChanged();
      return this;
    }
    /** <code>string instanceId = 1;</code> */
    public Builder clearInstanceId() {

      instanceId_ = getDefaultInstance().getInstanceId();
      onChanged();
      return this;
    }
    /** <code>string instanceId = 1;</code> */
    public Builder setInstanceIdBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);

      instanceId_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object zone_ = "";
    /** <code>string zone = 2;</code> */
    public java.lang.String getZone() {
      java.lang.Object ref = zone_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        zone_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /** <code>string zone = 2;</code> */
    public com.google.protobuf.ByteString getZoneBytes() {
      java.lang.Object ref = zone_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
            com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
        zone_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /** <code>string zone = 2;</code> */
    public Builder setZone(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      zone_ = value;
      onChanged();
      return this;
    }
    /** <code>string zone = 2;</code> */
    public Builder clearZone() {

      zone_ = getDefaultInstance().getZone();
      onChanged();
      return this;
    }
    /** <code>string zone = 2;</code> */
    public Builder setZoneBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);

      zone_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object appName_ = "";
    /** <code>string appName = 3;</code> */
    public java.lang.String getAppName() {
      java.lang.Object ref = appName_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        appName_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /** <code>string appName = 3;</code> */
    public com.google.protobuf.ByteString getAppNameBytes() {
      java.lang.Object ref = appName_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
            com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
        appName_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /** <code>string appName = 3;</code> */
    public Builder setAppName(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      appName_ = value;
      onChanged();
      return this;
    }
    /** <code>string appName = 3;</code> */
    public Builder clearAppName() {

      appName_ = getDefaultInstance().getAppName();
      onChanged();
      return this;
    }
    /** <code>string appName = 3;</code> */
    public Builder setAppNameBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);

      appName_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object dataId_ = "";
    /** <code>string dataId = 4;</code> */
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
    /** <code>string dataId = 4;</code> */
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
    /** <code>string dataId = 4;</code> */
    public Builder setDataId(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      dataId_ = value;
      onChanged();
      return this;
    }
    /** <code>string dataId = 4;</code> */
    public Builder clearDataId() {

      dataId_ = getDefaultInstance().getDataId();
      onChanged();
      return this;
    }
    /** <code>string dataId = 4;</code> */
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
    /** <code>string group = 5;</code> */
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
    /** <code>string group = 5;</code> */
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
    /** <code>string group = 5;</code> */
    public Builder setGroup(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      group_ = value;
      onChanged();
      return this;
    }
    /** <code>string group = 5;</code> */
    public Builder clearGroup() {

      group_ = getDefaultInstance().getGroup();
      onChanged();
      return this;
    }
    /** <code>string group = 5;</code> */
    public Builder setGroupBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);

      group_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object processId_ = "";
    /** <code>string processId = 6;</code> */
    public java.lang.String getProcessId() {
      java.lang.Object ref = processId_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        processId_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /** <code>string processId = 6;</code> */
    public com.google.protobuf.ByteString getProcessIdBytes() {
      java.lang.Object ref = processId_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
            com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
        processId_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /** <code>string processId = 6;</code> */
    public Builder setProcessId(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      processId_ = value;
      onChanged();
      return this;
    }
    /** <code>string processId = 6;</code> */
    public Builder clearProcessId() {

      processId_ = getDefaultInstance().getProcessId();
      onChanged();
      return this;
    }
    /** <code>string processId = 6;</code> */
    public Builder setProcessIdBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);

      processId_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object registId_ = "";
    /** <code>string registId = 7;</code> */
    public java.lang.String getRegistId() {
      java.lang.Object ref = registId_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        registId_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /** <code>string registId = 7;</code> */
    public com.google.protobuf.ByteString getRegistIdBytes() {
      java.lang.Object ref = registId_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
            com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
        registId_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /** <code>string registId = 7;</code> */
    public Builder setRegistId(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      registId_ = value;
      onChanged();
      return this;
    }
    /** <code>string registId = 7;</code> */
    public Builder clearRegistId() {

      registId_ = getDefaultInstance().getRegistId();
      onChanged();
      return this;
    }
    /** <code>string registId = 7;</code> */
    public Builder setRegistIdBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);

      registId_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object clientId_ = "";
    /** <code>string clientId = 8;</code> */
    public java.lang.String getClientId() {
      java.lang.Object ref = clientId_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        clientId_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /** <code>string clientId = 8;</code> */
    public com.google.protobuf.ByteString getClientIdBytes() {
      java.lang.Object ref = clientId_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
            com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
        clientId_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /** <code>string clientId = 8;</code> */
    public Builder setClientId(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      clientId_ = value;
      onChanged();
      return this;
    }
    /** <code>string clientId = 8;</code> */
    public Builder clearClientId() {

      clientId_ = getDefaultInstance().getClientId();
      onChanged();
      return this;
    }
    /** <code>string clientId = 8;</code> */
    public Builder setClientIdBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);

      clientId_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object dataInfoId_ = "";
    /** <code>string dataInfoId = 9;</code> */
    public java.lang.String getDataInfoId() {
      java.lang.Object ref = dataInfoId_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        dataInfoId_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /** <code>string dataInfoId = 9;</code> */
    public com.google.protobuf.ByteString getDataInfoIdBytes() {
      java.lang.Object ref = dataInfoId_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
            com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
        dataInfoId_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /** <code>string dataInfoId = 9;</code> */
    public Builder setDataInfoId(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      dataInfoId_ = value;
      onChanged();
      return this;
    }
    /** <code>string dataInfoId = 9;</code> */
    public Builder clearDataInfoId() {

      dataInfoId_ = getDefaultInstance().getDataInfoId();
      onChanged();
      return this;
    }
    /** <code>string dataInfoId = 9;</code> */
    public Builder setDataInfoIdBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);

      dataInfoId_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object ip_ = "";
    /** <code>string ip = 10;</code> */
    public java.lang.String getIp() {
      java.lang.Object ref = ip_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        ip_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /** <code>string ip = 10;</code> */
    public com.google.protobuf.ByteString getIpBytes() {
      java.lang.Object ref = ip_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
            com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
        ip_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /** <code>string ip = 10;</code> */
    public Builder setIp(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      ip_ = value;
      onChanged();
      return this;
    }
    /** <code>string ip = 10;</code> */
    public Builder clearIp() {

      ip_ = getDefaultInstance().getIp();
      onChanged();
      return this;
    }
    /** <code>string ip = 10;</code> */
    public Builder setIpBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);

      ip_ = value;
      onChanged();
      return this;
    }

    private int port_;
    /** <code>int32 port = 11;</code> */
    public int getPort() {
      return port_;
    }
    /** <code>int32 port = 11;</code> */
    public Builder setPort(int value) {

      port_ = value;
      onChanged();
      return this;
    }
    /** <code>int32 port = 11;</code> */
    public Builder clearPort() {

      port_ = 0;
      onChanged();
      return this;
    }

    private java.lang.Object eventType_ = "";
    /** <code>string eventType = 12;</code> */
    public java.lang.String getEventType() {
      java.lang.Object ref = eventType_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        eventType_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /** <code>string eventType = 12;</code> */
    public com.google.protobuf.ByteString getEventTypeBytes() {
      java.lang.Object ref = eventType_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
            com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
        eventType_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /** <code>string eventType = 12;</code> */
    public Builder setEventType(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      eventType_ = value;
      onChanged();
      return this;
    }
    /** <code>string eventType = 12;</code> */
    public Builder clearEventType() {

      eventType_ = getDefaultInstance().getEventType();
      onChanged();
      return this;
    }
    /** <code>string eventType = 12;</code> */
    public Builder setEventTypeBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);

      eventType_ = value;
      onChanged();
      return this;
    }

    private long version_;
    /** <code>int64 version = 13;</code> */
    public long getVersion() {
      return version_;
    }
    /** <code>int64 version = 13;</code> */
    public Builder setVersion(long value) {

      version_ = value;
      onChanged();
      return this;
    }
    /** <code>int64 version = 13;</code> */
    public Builder clearVersion() {

      version_ = 0L;
      onChanged();
      return this;
    }

    private long timestamp_;
    /** <code>int64 timestamp = 14;</code> */
    public long getTimestamp() {
      return timestamp_;
    }
    /** <code>int64 timestamp = 14;</code> */
    public Builder setTimestamp(long value) {

      timestamp_ = value;
      onChanged();
      return this;
    }
    /** <code>int64 timestamp = 14;</code> */
    public Builder clearTimestamp() {

      timestamp_ = 0L;
      onChanged();
      return this;
    }

    private com.google.protobuf.MapField<java.lang.String, java.lang.String> attributes_;

    private com.google.protobuf.MapField<java.lang.String, java.lang.String>
        internalGetAttributes() {
      if (attributes_ == null) {
        return com.google.protobuf.MapField.emptyMapField(
            AttributesDefaultEntryHolder.defaultEntry);
      }
      return attributes_;
    }

    private com.google.protobuf.MapField<java.lang.String, java.lang.String>
        internalGetMutableAttributes() {
      onChanged();
      ;
      if (attributes_ == null) {
        attributes_ =
            com.google.protobuf.MapField.newMapField(AttributesDefaultEntryHolder.defaultEntry);
      }
      if (!attributes_.isMutable()) {
        attributes_ = attributes_.copy();
      }
      return attributes_;
    }

    public int getAttributesCount() {
      return internalGetAttributes().getMap().size();
    }
    /** <code>map&lt;string, string&gt; attributes = 15;</code> */
    public boolean containsAttributes(java.lang.String key) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      return internalGetAttributes().getMap().containsKey(key);
    }
    /** Use {@link #getAttributesMap()} instead. */
    @java.lang.Deprecated
    public java.util.Map<java.lang.String, java.lang.String> getAttributes() {
      return getAttributesMap();
    }
    /** <code>map&lt;string, string&gt; attributes = 15;</code> */
    public java.util.Map<java.lang.String, java.lang.String> getAttributesMap() {
      return internalGetAttributes().getMap();
    }
    /** <code>map&lt;string, string&gt; attributes = 15;</code> */
    public java.lang.String getAttributesOrDefault(
        java.lang.String key, java.lang.String defaultValue) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      java.util.Map<java.lang.String, java.lang.String> map = internalGetAttributes().getMap();
      return map.containsKey(key) ? map.get(key) : defaultValue;
    }
    /** <code>map&lt;string, string&gt; attributes = 15;</code> */
    public java.lang.String getAttributesOrThrow(java.lang.String key) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      java.util.Map<java.lang.String, java.lang.String> map = internalGetAttributes().getMap();
      if (!map.containsKey(key)) {
        throw new java.lang.IllegalArgumentException();
      }
      return map.get(key);
    }

    public Builder clearAttributes() {
      internalGetMutableAttributes().getMutableMap().clear();
      return this;
    }
    /** <code>map&lt;string, string&gt; attributes = 15;</code> */
    public Builder removeAttributes(java.lang.String key) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      internalGetMutableAttributes().getMutableMap().remove(key);
      return this;
    }
    /** Use alternate mutation accessors instead. */
    @java.lang.Deprecated
    public java.util.Map<java.lang.String, java.lang.String> getMutableAttributes() {
      return internalGetMutableAttributes().getMutableMap();
    }
    /** <code>map&lt;string, string&gt; attributes = 15;</code> */
    public Builder putAttributes(java.lang.String key, java.lang.String value) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      if (value == null) {
        throw new java.lang.NullPointerException();
      }
      internalGetMutableAttributes().getMutableMap().put(key, value);
      return this;
    }
    /** <code>map&lt;string, string&gt; attributes = 15;</code> */
    public Builder putAllAttributes(java.util.Map<java.lang.String, java.lang.String> values) {
      internalGetMutableAttributes().getMutableMap().putAll(values);
      return this;
    }

    public final Builder setUnknownFields(final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFieldsProto3(unknownFields);
    }

    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }

    // @@protoc_insertion_point(builder_scope:BaseRegisterPb)
  }

  // @@protoc_insertion_point(class_scope:BaseRegisterPb)
  private static final com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb
      DEFAULT_INSTANCE;

  static {
    DEFAULT_INSTANCE = new com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb();
  }

  public static com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb
      getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<BaseRegisterPb> PARSER =
      new com.google.protobuf.AbstractParser<BaseRegisterPb>() {
        public BaseRegisterPb parsePartialFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
          return new BaseRegisterPb(input, extensionRegistry);
        }
      };

  public static com.google.protobuf.Parser<BaseRegisterPb> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<BaseRegisterPb> getParserForType() {
    return PARSER;
  }

  public com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb
      getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }
}
