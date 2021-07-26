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

/** Protobuf type {@code ReceivedDataBodyPb} */
public final class ReceivedDataBodyPb extends com.google.protobuf.GeneratedMessageV3
    implements
    // @@protoc_insertion_point(message_implements:ReceivedDataBodyPb)
    ReceivedDataBodyPbOrBuilder {
  private static final long serialVersionUID = 0L;
  // Use ReceivedDataBodyPb.newBuilder() to construct.
  private ReceivedDataBodyPb(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }

  private ReceivedDataBodyPb() {}

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
    return this.unknownFields;
  }

  private ReceivedDataBodyPb(
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
          case 58:
            {
              if (!((mutable_bitField0_ & 0x00000001) == 0x00000001)) {
                data_ =
                    com.google.protobuf.MapField.newMapField(DataDefaultEntryHolder.defaultEntry);
                mutable_bitField0_ |= 0x00000001;
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
    return com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPbOuterClass
        .internal_static_ReceivedDataBodyPb_descriptor;
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
    return com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPbOuterClass
        .internal_static_ReceivedDataBodyPb_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPb.class,
            com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPb.Builder.class);
  }

  public static final int DATA_FIELD_NUMBER = 7;

  private static final class DataDefaultEntryHolder {
    static final com.google.protobuf.MapEntry<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
        defaultEntry =
            com.google.protobuf.MapEntry
                .<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
                    newDefaultInstance(
                        com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPbOuterClass
                            .internal_static_ReceivedDataBodyPb_DataEntry_descriptor,
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
        output, internalGetData(), DataDefaultEntryHolder.defaultEntry, 7);
    unknownFields.writeTo(output);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
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
    size += unknownFields.getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPb)) {
      return super.equals(obj);
    }
    com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPb other =
        (com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPb) obj;

    boolean result = true;
    result = result && internalGetData().equals(other.internalGetData());
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
    if (!internalGetData().getMap().isEmpty()) {
      hash = (37 * hash) + DATA_FIELD_NUMBER;
      hash = (53 * hash) + internalGetData().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPb parseFrom(
      java.nio.ByteBuffer data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPb parseFrom(
      java.nio.ByteBuffer data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPb parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPb parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPb parseFrom(
      byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPb parseFrom(
      byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPb parseFrom(
      java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPb parseFrom(
      java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPb
      parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPb
      parseDelimitedFrom(
          java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPb parseFrom(
      com.google.protobuf.CodedInputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPb parseFrom(
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
      com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPb prototype) {
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
  /** Protobuf type {@code ReceivedDataBodyPb} */
  public static final class Builder extends com.google.protobuf.GeneratedMessageV3.Builder<Builder>
      implements
      // @@protoc_insertion_point(builder_implements:ReceivedDataBodyPb)
      com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPbOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
      return com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPbOuterClass
          .internal_static_ReceivedDataBodyPb_descriptor;
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
      return com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPbOuterClass
          .internal_static_ReceivedDataBodyPb_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPb.class,
              com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPb.Builder.class);
    }

    // Construct using
    // com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPb.newBuilder()
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
      internalGetMutableData().clear();
      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
      return com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPbOuterClass
          .internal_static_ReceivedDataBodyPb_descriptor;
    }

    public com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPb
        getDefaultInstanceForType() {
      return com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPb
          .getDefaultInstance();
    }

    public com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPb build() {
      com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPb result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPb buildPartial() {
      com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPb result =
          new com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPb(this);
      int from_bitField0_ = bitField0_;
      result.data_ = internalGetData();
      result.data_.makeImmutable();
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
      if (other instanceof com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPb) {
        return mergeFrom(
            (com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPb) other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(
        com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPb other) {
      if (other
          == com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPb
              .getDefaultInstance()) return this;
      internalGetMutableData().mergeFrom(other.internalGetData());
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
      com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPb parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage =
            (com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPb)
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

    public final Builder setUnknownFields(final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFieldsProto3(unknownFields);
    }

    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }

    // @@protoc_insertion_point(builder_scope:ReceivedDataBodyPb)
  }

  // @@protoc_insertion_point(class_scope:ReceivedDataBodyPb)
  private static final com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPb
      DEFAULT_INSTANCE;

  static {
    DEFAULT_INSTANCE = new com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPb();
  }

  public static com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPb
      getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<ReceivedDataBodyPb> PARSER =
      new com.google.protobuf.AbstractParser<ReceivedDataBodyPb>() {
        public ReceivedDataBodyPb parsePartialFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
          return new ReceivedDataBodyPb(input, extensionRegistry);
        }
      };

  public static com.google.protobuf.Parser<ReceivedDataBodyPb> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<ReceivedDataBodyPb> getParserForType() {
    return PARSER;
  }

  public com.alipay.sofa.registry.common.model.client.pb.ReceivedDataBodyPb
      getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }
}
