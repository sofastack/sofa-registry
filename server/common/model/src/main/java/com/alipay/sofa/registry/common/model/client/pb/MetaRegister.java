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

/** Protobuf type {@code MetaRegister} */
public final class MetaRegister extends com.google.protobuf.GeneratedMessageV3
    implements
    // @@protoc_insertion_point(message_implements:MetaRegister)
    MetaRegisterOrBuilder {
  private static final long serialVersionUID = 0L;
  // Use MetaRegister.newBuilder() to construct.
  private MetaRegister(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }

  private MetaRegister() {
    application_ = "";
    revision_ = "";
    clientVersion_ = "";
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
    return this.unknownFields;
  }

  private MetaRegister(
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

              application_ = s;
              break;
            }
          case 18:
            {
              java.lang.String s = input.readStringRequireUtf8();

              revision_ = s;
              break;
            }
          case 26:
            {
              java.lang.String s = input.readStringRequireUtf8();

              clientVersion_ = s;
              break;
            }
          case 34:
            {
              if (!((mutable_bitField0_ & 0x00000008) == 0x00000008)) {
                baseParams_ =
                    com.google.protobuf.MapField.newMapField(
                        BaseParamsDefaultEntryHolder.defaultEntry);
                mutable_bitField0_ |= 0x00000008;
              }
              com.google.protobuf.MapEntry<
                      java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
                  baseParams__ =
                      input.readMessage(
                          BaseParamsDefaultEntryHolder.defaultEntry.getParserForType(),
                          extensionRegistry);
              baseParams_.getMutableMap().put(baseParams__.getKey(), baseParams__.getValue());
              break;
            }
          case 42:
            {
              if (!((mutable_bitField0_ & 0x00000010) == 0x00000010)) {
                services_ =
                    com.google.protobuf.MapField.newMapField(
                        ServicesDefaultEntryHolder.defaultEntry);
                mutable_bitField0_ |= 0x00000010;
              }
              com.google.protobuf.MapEntry<
                      java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaService>
                  services__ =
                      input.readMessage(
                          ServicesDefaultEntryHolder.defaultEntry.getParserForType(),
                          extensionRegistry);
              services_.getMutableMap().put(services__.getKey(), services__.getValue());
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
    return com.alipay.sofa.registry.common.model.client.pb.AppDiscoveryMetaPb
        .internal_static_MetaRegister_descriptor;
  }

  @SuppressWarnings({"rawtypes"})
  protected com.google.protobuf.MapField internalGetMapField(int number) {
    switch (number) {
      case 4:
        return internalGetBaseParams();
      case 5:
        return internalGetServices();
      default:
        throw new RuntimeException("Invalid map field number: " + number);
    }
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.alipay.sofa.registry.common.model.client.pb.AppDiscoveryMetaPb
        .internal_static_MetaRegister_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.alipay.sofa.registry.common.model.client.pb.MetaRegister.class,
            com.alipay.sofa.registry.common.model.client.pb.MetaRegister.Builder.class);
  }

  private int bitField0_;
  public static final int APPLICATION_FIELD_NUMBER = 1;
  private volatile java.lang.Object application_;
  /** <code>string application = 1;</code> */
  public java.lang.String getApplication() {
    java.lang.Object ref = application_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      application_ = s;
      return s;
    }
  }
  /** <code>string application = 1;</code> */
  public com.google.protobuf.ByteString getApplicationBytes() {
    java.lang.Object ref = application_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b =
          com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
      application_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int REVISION_FIELD_NUMBER = 2;
  private volatile java.lang.Object revision_;
  /** <code>string revision = 2;</code> */
  public java.lang.String getRevision() {
    java.lang.Object ref = revision_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      revision_ = s;
      return s;
    }
  }
  /** <code>string revision = 2;</code> */
  public com.google.protobuf.ByteString getRevisionBytes() {
    java.lang.Object ref = revision_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b =
          com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
      revision_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int CLIENTVERSION_FIELD_NUMBER = 3;
  private volatile java.lang.Object clientVersion_;
  /** <code>string clientVersion = 3;</code> */
  public java.lang.String getClientVersion() {
    java.lang.Object ref = clientVersion_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      clientVersion_ = s;
      return s;
    }
  }
  /** <code>string clientVersion = 3;</code> */
  public com.google.protobuf.ByteString getClientVersionBytes() {
    java.lang.Object ref = clientVersion_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b =
          com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
      clientVersion_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int BASEPARAMS_FIELD_NUMBER = 4;

  private static final class BaseParamsDefaultEntryHolder {
    static final com.google.protobuf.MapEntry<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
        defaultEntry =
            com.google.protobuf.MapEntry
                .<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
                    newDefaultInstance(
                        com.alipay.sofa.registry.common.model.client.pb.AppDiscoveryMetaPb
                            .internal_static_MetaRegister_BaseParamsEntry_descriptor,
                        com.google.protobuf.WireFormat.FieldType.STRING,
                        "",
                        com.google.protobuf.WireFormat.FieldType.MESSAGE,
                        com.alipay.sofa.registry.common.model.client.pb.StringList
                            .getDefaultInstance());
  }

  private com.google.protobuf.MapField<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
      baseParams_;

  private com.google.protobuf.MapField<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
      internalGetBaseParams() {
    if (baseParams_ == null) {
      return com.google.protobuf.MapField.emptyMapField(BaseParamsDefaultEntryHolder.defaultEntry);
    }
    return baseParams_;
  }

  public int getBaseParamsCount() {
    return internalGetBaseParams().getMap().size();
  }
  /** <code>map&lt;string, .StringList&gt; baseParams = 4;</code> */
  public boolean containsBaseParams(java.lang.String key) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    return internalGetBaseParams().getMap().containsKey(key);
  }
  /** Use {@link #getBaseParamsMap()} instead. */
  @java.lang.Deprecated
  public java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
      getBaseParams() {
    return getBaseParamsMap();
  }
  /** <code>map&lt;string, .StringList&gt; baseParams = 4;</code> */
  public java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
      getBaseParamsMap() {
    return internalGetBaseParams().getMap();
  }
  /** <code>map&lt;string, .StringList&gt; baseParams = 4;</code> */
  public com.alipay.sofa.registry.common.model.client.pb.StringList getBaseParamsOrDefault(
      java.lang.String key,
      com.alipay.sofa.registry.common.model.client.pb.StringList defaultValue) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
        map = internalGetBaseParams().getMap();
    return map.containsKey(key) ? map.get(key) : defaultValue;
  }
  /** <code>map&lt;string, .StringList&gt; baseParams = 4;</code> */
  public com.alipay.sofa.registry.common.model.client.pb.StringList getBaseParamsOrThrow(
      java.lang.String key) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
        map = internalGetBaseParams().getMap();
    if (!map.containsKey(key)) {
      throw new java.lang.IllegalArgumentException();
    }
    return map.get(key);
  }

  public static final int SERVICES_FIELD_NUMBER = 5;

  private static final class ServicesDefaultEntryHolder {
    static final com.google.protobuf.MapEntry<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaService>
        defaultEntry =
            com.google.protobuf.MapEntry
                .<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaService>
                    newDefaultInstance(
                        com.alipay.sofa.registry.common.model.client.pb.AppDiscoveryMetaPb
                            .internal_static_MetaRegister_ServicesEntry_descriptor,
                        com.google.protobuf.WireFormat.FieldType.STRING,
                        "",
                        com.google.protobuf.WireFormat.FieldType.MESSAGE,
                        com.alipay.sofa.registry.common.model.client.pb.MetaService
                            .getDefaultInstance());
  }

  private com.google.protobuf.MapField<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaService>
      services_;

  private com.google.protobuf.MapField<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaService>
      internalGetServices() {
    if (services_ == null) {
      return com.google.protobuf.MapField.emptyMapField(ServicesDefaultEntryHolder.defaultEntry);
    }
    return services_;
  }

  public int getServicesCount() {
    return internalGetServices().getMap().size();
  }
  /** <code>map&lt;string, .MetaService&gt; services = 5;</code> */
  public boolean containsServices(java.lang.String key) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    return internalGetServices().getMap().containsKey(key);
  }
  /** Use {@link #getServicesMap()} instead. */
  @java.lang.Deprecated
  public java.util.Map<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaService>
      getServices() {
    return getServicesMap();
  }
  /** <code>map&lt;string, .MetaService&gt; services = 5;</code> */
  public java.util.Map<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaService>
      getServicesMap() {
    return internalGetServices().getMap();
  }
  /** <code>map&lt;string, .MetaService&gt; services = 5;</code> */
  public com.alipay.sofa.registry.common.model.client.pb.MetaService getServicesOrDefault(
      java.lang.String key,
      com.alipay.sofa.registry.common.model.client.pb.MetaService defaultValue) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaService>
        map = internalGetServices().getMap();
    return map.containsKey(key) ? map.get(key) : defaultValue;
  }
  /** <code>map&lt;string, .MetaService&gt; services = 5;</code> */
  public com.alipay.sofa.registry.common.model.client.pb.MetaService getServicesOrThrow(
      java.lang.String key) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaService>
        map = internalGetServices().getMap();
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
    if (!getApplicationBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, application_);
    }
    if (!getRevisionBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 2, revision_);
    }
    if (!getClientVersionBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 3, clientVersion_);
    }
    com.google.protobuf.GeneratedMessageV3.serializeStringMapTo(
        output, internalGetBaseParams(), BaseParamsDefaultEntryHolder.defaultEntry, 4);
    com.google.protobuf.GeneratedMessageV3.serializeStringMapTo(
        output, internalGetServices(), ServicesDefaultEntryHolder.defaultEntry, 5);
    unknownFields.writeTo(output);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!getApplicationBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, application_);
    }
    if (!getRevisionBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, revision_);
    }
    if (!getClientVersionBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(3, clientVersion_);
    }
    for (java.util.Map.Entry<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
        entry : internalGetBaseParams().getMap().entrySet()) {
      com.google.protobuf.MapEntry<
              java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
          baseParams__ =
              BaseParamsDefaultEntryHolder.defaultEntry
                  .newBuilderForType()
                  .setKey(entry.getKey())
                  .setValue(entry.getValue())
                  .build();
      size += com.google.protobuf.CodedOutputStream.computeMessageSize(4, baseParams__);
    }
    for (java.util.Map.Entry<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaService>
        entry : internalGetServices().getMap().entrySet()) {
      com.google.protobuf.MapEntry<
              java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaService>
          services__ =
              ServicesDefaultEntryHolder.defaultEntry
                  .newBuilderForType()
                  .setKey(entry.getKey())
                  .setValue(entry.getValue())
                  .build();
      size += com.google.protobuf.CodedOutputStream.computeMessageSize(5, services__);
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
    if (!(obj instanceof com.alipay.sofa.registry.common.model.client.pb.MetaRegister)) {
      return super.equals(obj);
    }
    com.alipay.sofa.registry.common.model.client.pb.MetaRegister other =
        (com.alipay.sofa.registry.common.model.client.pb.MetaRegister) obj;

    boolean result = true;
    result = result && getApplication().equals(other.getApplication());
    result = result && getRevision().equals(other.getRevision());
    result = result && getClientVersion().equals(other.getClientVersion());
    result = result && internalGetBaseParams().equals(other.internalGetBaseParams());
    result = result && internalGetServices().equals(other.internalGetServices());
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
    hash = (37 * hash) + APPLICATION_FIELD_NUMBER;
    hash = (53 * hash) + getApplication().hashCode();
    hash = (37 * hash) + REVISION_FIELD_NUMBER;
    hash = (53 * hash) + getRevision().hashCode();
    hash = (37 * hash) + CLIENTVERSION_FIELD_NUMBER;
    hash = (53 * hash) + getClientVersion().hashCode();
    if (!internalGetBaseParams().getMap().isEmpty()) {
      hash = (37 * hash) + BASEPARAMS_FIELD_NUMBER;
      hash = (53 * hash) + internalGetBaseParams().hashCode();
    }
    if (!internalGetServices().getMap().isEmpty()) {
      hash = (37 * hash) + SERVICES_FIELD_NUMBER;
      hash = (53 * hash) + internalGetServices().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MetaRegister parseFrom(
      java.nio.ByteBuffer data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MetaRegister parseFrom(
      java.nio.ByteBuffer data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MetaRegister parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MetaRegister parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MetaRegister parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MetaRegister parseFrom(
      byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MetaRegister parseFrom(
      java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MetaRegister parseFrom(
      java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MetaRegister parseDelimitedFrom(
      java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MetaRegister parseDelimitedFrom(
      java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MetaRegister parseFrom(
      com.google.protobuf.CodedInputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MetaRegister parseFrom(
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
      com.alipay.sofa.registry.common.model.client.pb.MetaRegister prototype) {
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
  /** Protobuf type {@code MetaRegister} */
  public static final class Builder extends com.google.protobuf.GeneratedMessageV3.Builder<Builder>
      implements
      // @@protoc_insertion_point(builder_implements:MetaRegister)
      com.alipay.sofa.registry.common.model.client.pb.MetaRegisterOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
      return com.alipay.sofa.registry.common.model.client.pb.AppDiscoveryMetaPb
          .internal_static_MetaRegister_descriptor;
    }

    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapField internalGetMapField(int number) {
      switch (number) {
        case 4:
          return internalGetBaseParams();
        case 5:
          return internalGetServices();
        default:
          throw new RuntimeException("Invalid map field number: " + number);
      }
    }

    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapField internalGetMutableMapField(int number) {
      switch (number) {
        case 4:
          return internalGetMutableBaseParams();
        case 5:
          return internalGetMutableServices();
        default:
          throw new RuntimeException("Invalid map field number: " + number);
      }
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.alipay.sofa.registry.common.model.client.pb.AppDiscoveryMetaPb
          .internal_static_MetaRegister_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.alipay.sofa.registry.common.model.client.pb.MetaRegister.class,
              com.alipay.sofa.registry.common.model.client.pb.MetaRegister.Builder.class);
    }

    // Construct using com.alipay.sofa.registry.common.model.client.pb.MetaRegister.newBuilder()
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
      application_ = "";

      revision_ = "";

      clientVersion_ = "";

      internalGetMutableBaseParams().clear();
      internalGetMutableServices().clear();
      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
      return com.alipay.sofa.registry.common.model.client.pb.AppDiscoveryMetaPb
          .internal_static_MetaRegister_descriptor;
    }

    public com.alipay.sofa.registry.common.model.client.pb.MetaRegister
        getDefaultInstanceForType() {
      return com.alipay.sofa.registry.common.model.client.pb.MetaRegister.getDefaultInstance();
    }

    public com.alipay.sofa.registry.common.model.client.pb.MetaRegister build() {
      com.alipay.sofa.registry.common.model.client.pb.MetaRegister result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public com.alipay.sofa.registry.common.model.client.pb.MetaRegister buildPartial() {
      com.alipay.sofa.registry.common.model.client.pb.MetaRegister result =
          new com.alipay.sofa.registry.common.model.client.pb.MetaRegister(this);
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      result.application_ = application_;
      result.revision_ = revision_;
      result.clientVersion_ = clientVersion_;
      result.baseParams_ = internalGetBaseParams();
      result.baseParams_.makeImmutable();
      result.services_ = internalGetServices();
      result.services_.makeImmutable();
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
      if (other instanceof com.alipay.sofa.registry.common.model.client.pb.MetaRegister) {
        return mergeFrom((com.alipay.sofa.registry.common.model.client.pb.MetaRegister) other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.alipay.sofa.registry.common.model.client.pb.MetaRegister other) {
      if (other
          == com.alipay.sofa.registry.common.model.client.pb.MetaRegister.getDefaultInstance())
        return this;
      if (!other.getApplication().isEmpty()) {
        application_ = other.application_;
        onChanged();
      }
      if (!other.getRevision().isEmpty()) {
        revision_ = other.revision_;
        onChanged();
      }
      if (!other.getClientVersion().isEmpty()) {
        clientVersion_ = other.clientVersion_;
        onChanged();
      }
      internalGetMutableBaseParams().mergeFrom(other.internalGetBaseParams());
      internalGetMutableServices().mergeFrom(other.internalGetServices());
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
      com.alipay.sofa.registry.common.model.client.pb.MetaRegister parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage =
            (com.alipay.sofa.registry.common.model.client.pb.MetaRegister) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private int bitField0_;

    private java.lang.Object application_ = "";
    /** <code>string application = 1;</code> */
    public java.lang.String getApplication() {
      java.lang.Object ref = application_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        application_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /** <code>string application = 1;</code> */
    public com.google.protobuf.ByteString getApplicationBytes() {
      java.lang.Object ref = application_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
            com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
        application_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /** <code>string application = 1;</code> */
    public Builder setApplication(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      application_ = value;
      onChanged();
      return this;
    }
    /** <code>string application = 1;</code> */
    public Builder clearApplication() {

      application_ = getDefaultInstance().getApplication();
      onChanged();
      return this;
    }
    /** <code>string application = 1;</code> */
    public Builder setApplicationBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);

      application_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object revision_ = "";
    /** <code>string revision = 2;</code> */
    public java.lang.String getRevision() {
      java.lang.Object ref = revision_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        revision_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /** <code>string revision = 2;</code> */
    public com.google.protobuf.ByteString getRevisionBytes() {
      java.lang.Object ref = revision_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
            com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
        revision_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /** <code>string revision = 2;</code> */
    public Builder setRevision(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      revision_ = value;
      onChanged();
      return this;
    }
    /** <code>string revision = 2;</code> */
    public Builder clearRevision() {

      revision_ = getDefaultInstance().getRevision();
      onChanged();
      return this;
    }
    /** <code>string revision = 2;</code> */
    public Builder setRevisionBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);

      revision_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object clientVersion_ = "";
    /** <code>string clientVersion = 3;</code> */
    public java.lang.String getClientVersion() {
      java.lang.Object ref = clientVersion_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        clientVersion_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /** <code>string clientVersion = 3;</code> */
    public com.google.protobuf.ByteString getClientVersionBytes() {
      java.lang.Object ref = clientVersion_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
            com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
        clientVersion_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /** <code>string clientVersion = 3;</code> */
    public Builder setClientVersion(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      clientVersion_ = value;
      onChanged();
      return this;
    }
    /** <code>string clientVersion = 3;</code> */
    public Builder clearClientVersion() {

      clientVersion_ = getDefaultInstance().getClientVersion();
      onChanged();
      return this;
    }
    /** <code>string clientVersion = 3;</code> */
    public Builder setClientVersionBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);

      clientVersion_ = value;
      onChanged();
      return this;
    }

    private com.google.protobuf.MapField<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
        baseParams_;

    private com.google.protobuf.MapField<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
        internalGetBaseParams() {
      if (baseParams_ == null) {
        return com.google.protobuf.MapField.emptyMapField(
            BaseParamsDefaultEntryHolder.defaultEntry);
      }
      return baseParams_;
    }

    private com.google.protobuf.MapField<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
        internalGetMutableBaseParams() {
      onChanged();
      ;
      if (baseParams_ == null) {
        baseParams_ =
            com.google.protobuf.MapField.newMapField(BaseParamsDefaultEntryHolder.defaultEntry);
      }
      if (!baseParams_.isMutable()) {
        baseParams_ = baseParams_.copy();
      }
      return baseParams_;
    }

    public int getBaseParamsCount() {
      return internalGetBaseParams().getMap().size();
    }
    /** <code>map&lt;string, .StringList&gt; baseParams = 4;</code> */
    public boolean containsBaseParams(java.lang.String key) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      return internalGetBaseParams().getMap().containsKey(key);
    }
    /** Use {@link #getBaseParamsMap()} instead. */
    @java.lang.Deprecated
    public java.util.Map<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
        getBaseParams() {
      return getBaseParamsMap();
    }
    /** <code>map&lt;string, .StringList&gt; baseParams = 4;</code> */
    public java.util.Map<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
        getBaseParamsMap() {
      return internalGetBaseParams().getMap();
    }
    /** <code>map&lt;string, .StringList&gt; baseParams = 4;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.StringList getBaseParamsOrDefault(
        java.lang.String key,
        com.alipay.sofa.registry.common.model.client.pb.StringList defaultValue) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
          map = internalGetBaseParams().getMap();
      return map.containsKey(key) ? map.get(key) : defaultValue;
    }
    /** <code>map&lt;string, .StringList&gt; baseParams = 4;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.StringList getBaseParamsOrThrow(
        java.lang.String key) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
          map = internalGetBaseParams().getMap();
      if (!map.containsKey(key)) {
        throw new java.lang.IllegalArgumentException();
      }
      return map.get(key);
    }

    public Builder clearBaseParams() {
      internalGetMutableBaseParams().getMutableMap().clear();
      return this;
    }
    /** <code>map&lt;string, .StringList&gt; baseParams = 4;</code> */
    public Builder removeBaseParams(java.lang.String key) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      internalGetMutableBaseParams().getMutableMap().remove(key);
      return this;
    }
    /** Use alternate mutation accessors instead. */
    @java.lang.Deprecated
    public java.util.Map<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
        getMutableBaseParams() {
      return internalGetMutableBaseParams().getMutableMap();
    }
    /** <code>map&lt;string, .StringList&gt; baseParams = 4;</code> */
    public Builder putBaseParams(
        java.lang.String key, com.alipay.sofa.registry.common.model.client.pb.StringList value) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      if (value == null) {
        throw new java.lang.NullPointerException();
      }
      internalGetMutableBaseParams().getMutableMap().put(key, value);
      return this;
    }
    /** <code>map&lt;string, .StringList&gt; baseParams = 4;</code> */
    public Builder putAllBaseParams(
        java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
            values) {
      internalGetMutableBaseParams().getMutableMap().putAll(values);
      return this;
    }

    private com.google.protobuf.MapField<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaService>
        services_;

    private com.google.protobuf.MapField<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaService>
        internalGetServices() {
      if (services_ == null) {
        return com.google.protobuf.MapField.emptyMapField(ServicesDefaultEntryHolder.defaultEntry);
      }
      return services_;
    }

    private com.google.protobuf.MapField<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaService>
        internalGetMutableServices() {
      onChanged();
      ;
      if (services_ == null) {
        services_ =
            com.google.protobuf.MapField.newMapField(ServicesDefaultEntryHolder.defaultEntry);
      }
      if (!services_.isMutable()) {
        services_ = services_.copy();
      }
      return services_;
    }

    public int getServicesCount() {
      return internalGetServices().getMap().size();
    }
    /** <code>map&lt;string, .MetaService&gt; services = 5;</code> */
    public boolean containsServices(java.lang.String key) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      return internalGetServices().getMap().containsKey(key);
    }
    /** Use {@link #getServicesMap()} instead. */
    @java.lang.Deprecated
    public java.util.Map<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaService>
        getServices() {
      return getServicesMap();
    }
    /** <code>map&lt;string, .MetaService&gt; services = 5;</code> */
    public java.util.Map<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaService>
        getServicesMap() {
      return internalGetServices().getMap();
    }
    /** <code>map&lt;string, .MetaService&gt; services = 5;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.MetaService getServicesOrDefault(
        java.lang.String key,
        com.alipay.sofa.registry.common.model.client.pb.MetaService defaultValue) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaService>
          map = internalGetServices().getMap();
      return map.containsKey(key) ? map.get(key) : defaultValue;
    }
    /** <code>map&lt;string, .MetaService&gt; services = 5;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.MetaService getServicesOrThrow(
        java.lang.String key) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaService>
          map = internalGetServices().getMap();
      if (!map.containsKey(key)) {
        throw new java.lang.IllegalArgumentException();
      }
      return map.get(key);
    }

    public Builder clearServices() {
      internalGetMutableServices().getMutableMap().clear();
      return this;
    }
    /** <code>map&lt;string, .MetaService&gt; services = 5;</code> */
    public Builder removeServices(java.lang.String key) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      internalGetMutableServices().getMutableMap().remove(key);
      return this;
    }
    /** Use alternate mutation accessors instead. */
    @java.lang.Deprecated
    public java.util.Map<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaService>
        getMutableServices() {
      return internalGetMutableServices().getMutableMap();
    }
    /** <code>map&lt;string, .MetaService&gt; services = 5;</code> */
    public Builder putServices(
        java.lang.String key, com.alipay.sofa.registry.common.model.client.pb.MetaService value) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      if (value == null) {
        throw new java.lang.NullPointerException();
      }
      internalGetMutableServices().getMutableMap().put(key, value);
      return this;
    }
    /** <code>map&lt;string, .MetaService&gt; services = 5;</code> */
    public Builder putAllServices(
        java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaService>
            values) {
      internalGetMutableServices().getMutableMap().putAll(values);
      return this;
    }

    public final Builder setUnknownFields(final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFieldsProto3(unknownFields);
    }

    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }

    // @@protoc_insertion_point(builder_scope:MetaRegister)
  }

  // @@protoc_insertion_point(class_scope:MetaRegister)
  private static final com.alipay.sofa.registry.common.model.client.pb.MetaRegister
      DEFAULT_INSTANCE;

  static {
    DEFAULT_INSTANCE = new com.alipay.sofa.registry.common.model.client.pb.MetaRegister();
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MetaRegister getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<MetaRegister> PARSER =
      new com.google.protobuf.AbstractParser<MetaRegister>() {
        public MetaRegister parsePartialFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
          return new MetaRegister(input, extensionRegistry);
        }
      };

  public static com.google.protobuf.Parser<MetaRegister> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<MetaRegister> getParserForType() {
    return PARSER;
  }

  public com.alipay.sofa.registry.common.model.client.pb.MetaRegister getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }
}
