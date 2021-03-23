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

/** Protobuf type {@code MetaService} */
public final class MetaService extends com.google.protobuf.GeneratedMessageV3
    implements
    // @@protoc_insertion_point(message_implements:MetaService)
    MetaServiceOrBuilder {
  private static final long serialVersionUID = 0L;
  // Use MetaService.newBuilder() to construct.
  private MetaService(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }

  private MetaService() {
    id_ = "";
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
    return this.unknownFields;
  }

  private MetaService(
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

              id_ = s;
              break;
            }
          case 26:
            {
              if (!((mutable_bitField0_ & 0x00000002) == 0x00000002)) {
                params_ =
                    com.google.protobuf.MapField.newMapField(ParamsDefaultEntryHolder.defaultEntry);
                mutable_bitField0_ |= 0x00000002;
              }
              com.google.protobuf.MapEntry<
                      java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
                  params__ =
                      input.readMessage(
                          ParamsDefaultEntryHolder.defaultEntry.getParserForType(),
                          extensionRegistry);
              params_.getMutableMap().put(params__.getKey(), params__.getValue());
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
        .internal_static_MetaService_descriptor;
  }

  @SuppressWarnings({"rawtypes"})
  protected com.google.protobuf.MapField internalGetMapField(int number) {
    switch (number) {
      case 3:
        return internalGetParams();
      default:
        throw new RuntimeException("Invalid map field number: " + number);
    }
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.alipay.sofa.registry.common.model.client.pb.AppDiscoveryMetaPb
        .internal_static_MetaService_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.alipay.sofa.registry.common.model.client.pb.MetaService.class,
            com.alipay.sofa.registry.common.model.client.pb.MetaService.Builder.class);
  }

  private int bitField0_;
  public static final int ID_FIELD_NUMBER = 1;
  private volatile java.lang.Object id_;
  /** <code>string id = 1;</code> */
  public java.lang.String getId() {
    java.lang.Object ref = id_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      id_ = s;
      return s;
    }
  }
  /** <code>string id = 1;</code> */
  public com.google.protobuf.ByteString getIdBytes() {
    java.lang.Object ref = id_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b =
          com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
      id_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int PARAMS_FIELD_NUMBER = 3;

  private static final class ParamsDefaultEntryHolder {
    static final com.google.protobuf.MapEntry<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
        defaultEntry =
            com.google.protobuf.MapEntry
                .<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
                    newDefaultInstance(
                        com.alipay.sofa.registry.common.model.client.pb.AppDiscoveryMetaPb
                            .internal_static_MetaService_ParamsEntry_descriptor,
                        com.google.protobuf.WireFormat.FieldType.STRING,
                        "",
                        com.google.protobuf.WireFormat.FieldType.MESSAGE,
                        com.alipay.sofa.registry.common.model.client.pb.StringList
                            .getDefaultInstance());
  }

  private com.google.protobuf.MapField<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
      params_;

  private com.google.protobuf.MapField<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
      internalGetParams() {
    if (params_ == null) {
      return com.google.protobuf.MapField.emptyMapField(ParamsDefaultEntryHolder.defaultEntry);
    }
    return params_;
  }

  public int getParamsCount() {
    return internalGetParams().getMap().size();
  }
  /** <code>map&lt;string, .StringList&gt; params = 3;</code> */
  public boolean containsParams(java.lang.String key) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    return internalGetParams().getMap().containsKey(key);
  }
  /** Use {@link #getParamsMap()} instead. */
  @java.lang.Deprecated
  public java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
      getParams() {
    return getParamsMap();
  }
  /** <code>map&lt;string, .StringList&gt; params = 3;</code> */
  public java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
      getParamsMap() {
    return internalGetParams().getMap();
  }
  /** <code>map&lt;string, .StringList&gt; params = 3;</code> */
  public com.alipay.sofa.registry.common.model.client.pb.StringList getParamsOrDefault(
      java.lang.String key,
      com.alipay.sofa.registry.common.model.client.pb.StringList defaultValue) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
        map = internalGetParams().getMap();
    return map.containsKey(key) ? map.get(key) : defaultValue;
  }
  /** <code>map&lt;string, .StringList&gt; params = 3;</code> */
  public com.alipay.sofa.registry.common.model.client.pb.StringList getParamsOrThrow(
      java.lang.String key) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
        map = internalGetParams().getMap();
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
    if (!getIdBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, id_);
    }
    com.google.protobuf.GeneratedMessageV3.serializeStringMapTo(
        output, internalGetParams(), ParamsDefaultEntryHolder.defaultEntry, 3);
    unknownFields.writeTo(output);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!getIdBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, id_);
    }
    for (java.util.Map.Entry<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
        entry : internalGetParams().getMap().entrySet()) {
      com.google.protobuf.MapEntry<
              java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
          params__ =
              ParamsDefaultEntryHolder.defaultEntry
                  .newBuilderForType()
                  .setKey(entry.getKey())
                  .setValue(entry.getValue())
                  .build();
      size += com.google.protobuf.CodedOutputStream.computeMessageSize(3, params__);
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
    if (!(obj instanceof com.alipay.sofa.registry.common.model.client.pb.MetaService)) {
      return super.equals(obj);
    }
    com.alipay.sofa.registry.common.model.client.pb.MetaService other =
        (com.alipay.sofa.registry.common.model.client.pb.MetaService) obj;

    boolean result = true;
    result = result && getId().equals(other.getId());
    result = result && internalGetParams().equals(other.internalGetParams());
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
    hash = (37 * hash) + ID_FIELD_NUMBER;
    hash = (53 * hash) + getId().hashCode();
    if (!internalGetParams().getMap().isEmpty()) {
      hash = (37 * hash) + PARAMS_FIELD_NUMBER;
      hash = (53 * hash) + internalGetParams().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MetaService parseFrom(
      java.nio.ByteBuffer data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MetaService parseFrom(
      java.nio.ByteBuffer data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MetaService parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MetaService parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MetaService parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MetaService parseFrom(
      byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MetaService parseFrom(
      java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MetaService parseFrom(
      java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MetaService parseDelimitedFrom(
      java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MetaService parseDelimitedFrom(
      java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MetaService parseFrom(
      com.google.protobuf.CodedInputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MetaService parseFrom(
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
      com.alipay.sofa.registry.common.model.client.pb.MetaService prototype) {
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
  /** Protobuf type {@code MetaService} */
  public static final class Builder extends com.google.protobuf.GeneratedMessageV3.Builder<Builder>
      implements
      // @@protoc_insertion_point(builder_implements:MetaService)
      com.alipay.sofa.registry.common.model.client.pb.MetaServiceOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
      return com.alipay.sofa.registry.common.model.client.pb.AppDiscoveryMetaPb
          .internal_static_MetaService_descriptor;
    }

    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapField internalGetMapField(int number) {
      switch (number) {
        case 3:
          return internalGetParams();
        default:
          throw new RuntimeException("Invalid map field number: " + number);
      }
    }

    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapField internalGetMutableMapField(int number) {
      switch (number) {
        case 3:
          return internalGetMutableParams();
        default:
          throw new RuntimeException("Invalid map field number: " + number);
      }
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.alipay.sofa.registry.common.model.client.pb.AppDiscoveryMetaPb
          .internal_static_MetaService_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.alipay.sofa.registry.common.model.client.pb.MetaService.class,
              com.alipay.sofa.registry.common.model.client.pb.MetaService.Builder.class);
    }

    // Construct using com.alipay.sofa.registry.common.model.client.pb.MetaService.newBuilder()
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
      id_ = "";

      internalGetMutableParams().clear();
      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
      return com.alipay.sofa.registry.common.model.client.pb.AppDiscoveryMetaPb
          .internal_static_MetaService_descriptor;
    }

    public com.alipay.sofa.registry.common.model.client.pb.MetaService getDefaultInstanceForType() {
      return com.alipay.sofa.registry.common.model.client.pb.MetaService.getDefaultInstance();
    }

    public com.alipay.sofa.registry.common.model.client.pb.MetaService build() {
      com.alipay.sofa.registry.common.model.client.pb.MetaService result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public com.alipay.sofa.registry.common.model.client.pb.MetaService buildPartial() {
      com.alipay.sofa.registry.common.model.client.pb.MetaService result =
          new com.alipay.sofa.registry.common.model.client.pb.MetaService(this);
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      result.id_ = id_;
      result.params_ = internalGetParams();
      result.params_.makeImmutable();
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
      if (other instanceof com.alipay.sofa.registry.common.model.client.pb.MetaService) {
        return mergeFrom((com.alipay.sofa.registry.common.model.client.pb.MetaService) other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.alipay.sofa.registry.common.model.client.pb.MetaService other) {
      if (other == com.alipay.sofa.registry.common.model.client.pb.MetaService.getDefaultInstance())
        return this;
      if (!other.getId().isEmpty()) {
        id_ = other.id_;
        onChanged();
      }
      internalGetMutableParams().mergeFrom(other.internalGetParams());
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
      com.alipay.sofa.registry.common.model.client.pb.MetaService parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage =
            (com.alipay.sofa.registry.common.model.client.pb.MetaService) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private int bitField0_;

    private java.lang.Object id_ = "";
    /** <code>string id = 1;</code> */
    public java.lang.String getId() {
      java.lang.Object ref = id_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        id_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /** <code>string id = 1;</code> */
    public com.google.protobuf.ByteString getIdBytes() {
      java.lang.Object ref = id_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
            com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
        id_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /** <code>string id = 1;</code> */
    public Builder setId(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      id_ = value;
      onChanged();
      return this;
    }
    /** <code>string id = 1;</code> */
    public Builder clearId() {

      id_ = getDefaultInstance().getId();
      onChanged();
      return this;
    }
    /** <code>string id = 1;</code> */
    public Builder setIdBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);

      id_ = value;
      onChanged();
      return this;
    }

    private com.google.protobuf.MapField<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
        params_;

    private com.google.protobuf.MapField<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
        internalGetParams() {
      if (params_ == null) {
        return com.google.protobuf.MapField.emptyMapField(ParamsDefaultEntryHolder.defaultEntry);
      }
      return params_;
    }

    private com.google.protobuf.MapField<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
        internalGetMutableParams() {
      onChanged();
      ;
      if (params_ == null) {
        params_ = com.google.protobuf.MapField.newMapField(ParamsDefaultEntryHolder.defaultEntry);
      }
      if (!params_.isMutable()) {
        params_ = params_.copy();
      }
      return params_;
    }

    public int getParamsCount() {
      return internalGetParams().getMap().size();
    }
    /** <code>map&lt;string, .StringList&gt; params = 3;</code> */
    public boolean containsParams(java.lang.String key) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      return internalGetParams().getMap().containsKey(key);
    }
    /** Use {@link #getParamsMap()} instead. */
    @java.lang.Deprecated
    public java.util.Map<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
        getParams() {
      return getParamsMap();
    }
    /** <code>map&lt;string, .StringList&gt; params = 3;</code> */
    public java.util.Map<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
        getParamsMap() {
      return internalGetParams().getMap();
    }
    /** <code>map&lt;string, .StringList&gt; params = 3;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.StringList getParamsOrDefault(
        java.lang.String key,
        com.alipay.sofa.registry.common.model.client.pb.StringList defaultValue) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
          map = internalGetParams().getMap();
      return map.containsKey(key) ? map.get(key) : defaultValue;
    }
    /** <code>map&lt;string, .StringList&gt; params = 3;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.StringList getParamsOrThrow(
        java.lang.String key) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
          map = internalGetParams().getMap();
      if (!map.containsKey(key)) {
        throw new java.lang.IllegalArgumentException();
      }
      return map.get(key);
    }

    public Builder clearParams() {
      internalGetMutableParams().getMutableMap().clear();
      return this;
    }
    /** <code>map&lt;string, .StringList&gt; params = 3;</code> */
    public Builder removeParams(java.lang.String key) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      internalGetMutableParams().getMutableMap().remove(key);
      return this;
    }
    /** Use alternate mutation accessors instead. */
    @java.lang.Deprecated
    public java.util.Map<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
        getMutableParams() {
      return internalGetMutableParams().getMutableMap();
    }
    /** <code>map&lt;string, .StringList&gt; params = 3;</code> */
    public Builder putParams(
        java.lang.String key, com.alipay.sofa.registry.common.model.client.pb.StringList value) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      if (value == null) {
        throw new java.lang.NullPointerException();
      }
      internalGetMutableParams().getMutableMap().put(key, value);
      return this;
    }
    /** <code>map&lt;string, .StringList&gt; params = 3;</code> */
    public Builder putAllParams(
        java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
            values) {
      internalGetMutableParams().getMutableMap().putAll(values);
      return this;
    }

    public final Builder setUnknownFields(final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFieldsProto3(unknownFields);
    }

    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }

    // @@protoc_insertion_point(builder_scope:MetaService)
  }

  // @@protoc_insertion_point(class_scope:MetaService)
  private static final com.alipay.sofa.registry.common.model.client.pb.MetaService DEFAULT_INSTANCE;

  static {
    DEFAULT_INSTANCE = new com.alipay.sofa.registry.common.model.client.pb.MetaService();
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MetaService getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<MetaService> PARSER =
      new com.google.protobuf.AbstractParser<MetaService>() {
        public MetaService parsePartialFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
          return new MetaService(input, extensionRegistry);
        }
      };

  public static com.google.protobuf.Parser<MetaService> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<MetaService> getParserForType() {
    return PARSER;
  }

  public com.alipay.sofa.registry.common.model.client.pb.MetaService getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }
}
