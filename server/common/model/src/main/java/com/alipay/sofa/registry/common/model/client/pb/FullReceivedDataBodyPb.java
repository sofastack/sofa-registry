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

/** Protobuf type {@code FullReceivedDataBodyPb} */
public final class FullReceivedDataBodyPb extends com.google.protobuf.GeneratedMessageV3
    implements
    // @@protoc_insertion_point(message_implements:FullReceivedDataBodyPb)
    FullReceivedDataBodyPbOrBuilder {
  private static final long serialVersionUID = 0L;
  // Use FullReceivedDataBodyPb.newBuilder() to construct.
  private FullReceivedDataBodyPb(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }

  private FullReceivedDataBodyPb() {
    encoding_ = "";
    body_ = com.google.protobuf.ByteString.EMPTY;
    originBodySize_ = 0;
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
    return this.unknownFields;
  }

  private FullReceivedDataBodyPb(
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

              encoding_ = s;
              break;
            }
          case 18:
            {
              body_ = input.readBytes();
              break;
            }
          case 24:
            {
              originBodySize_ = input.readInt32();
              break;
            }
          case 34:
            {
              if (!((mutable_bitField0_ & 0x00000008) == 0x00000008)) {
                data_ =
                    com.google.protobuf.MapField.newMapField(DataDefaultEntryHolder.defaultEntry);
                mutable_bitField0_ |= 0x00000008;
              }
              com.google.protobuf.MapEntry<
                      java.lang.String,
                      com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
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
    return com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPbOuterClass
        .internal_static_FullReceivedDataBodyPb_descriptor;
  }

  @SuppressWarnings({"rawtypes"})
  protected com.google.protobuf.MapField internalGetMapField(int number) {
    switch (number) {
      case 4:
        return internalGetData();
      default:
        throw new RuntimeException("Invalid map field number: " + number);
    }
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPbOuterClass
        .internal_static_FullReceivedDataBodyPb_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb.class,
            com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb.Builder.class);
  }

  private int bitField0_;
  public static final int ENCODING_FIELD_NUMBER = 1;
  private volatile java.lang.Object encoding_;
  /** <code>string encoding = 1;</code> */
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
  /** <code>string encoding = 1;</code> */
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

  public static final int BODY_FIELD_NUMBER = 2;
  private com.google.protobuf.ByteString body_;
  /** <code>bytes body = 2;</code> */
  public com.google.protobuf.ByteString getBody() {
    return body_;
  }

  public static final int ORIGINBODYSIZE_FIELD_NUMBER = 3;
  private int originBodySize_;
  /** <code>int32 originBodySize = 3;</code> */
  public int getOriginBodySize() {
    return originBodySize_;
  }

  public static final int DATA_FIELD_NUMBER = 4;

  private static final class DataDefaultEntryHolder {
    static final com.google.protobuf.MapEntry<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
        defaultEntry =
            com.google.protobuf.MapEntry
                .<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
                    newDefaultInstance(
                        com.alipay.sofa.registry.common.model.client.pb
                            .FullReceivedDataBodyPbOuterClass
                            .internal_static_FullReceivedDataBodyPb_DataEntry_descriptor,
                        com.google.protobuf.WireFormat.FieldType.STRING,
                        "",
                        com.google.protobuf.WireFormat.FieldType.MESSAGE,
                        com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb
                            .getDefaultInstance());
  }

  private com.google.protobuf.MapField<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
      data_;

  private com.google.protobuf.MapField<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
      internalGetData() {
    if (data_ == null) {
      return com.google.protobuf.MapField.emptyMapField(DataDefaultEntryHolder.defaultEntry);
    }
    return data_;
  }

  public int getDataCount() {
    return internalGetData().getMap().size();
  }
  /** <code>map&lt;string, .PushResourcesPb&gt; data = 4;</code> */
  public boolean containsData(java.lang.String key) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    return internalGetData().getMap().containsKey(key);
  }
  /** Use {@link #getDataMap()} instead. */
  @java.lang.Deprecated
  public java.util.Map<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
      getData() {
    return getDataMap();
  }
  /** <code>map&lt;string, .PushResourcesPb&gt; data = 4;</code> */
  public java.util.Map<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
      getDataMap() {
    return internalGetData().getMap();
  }
  /** <code>map&lt;string, .PushResourcesPb&gt; data = 4;</code> */
  public com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb getDataOrDefault(
      java.lang.String key,
      com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb defaultValue) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
        map = internalGetData().getMap();
    return map.containsKey(key) ? map.get(key) : defaultValue;
  }
  /** <code>map&lt;string, .PushResourcesPb&gt; data = 4;</code> */
  public com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb getDataOrThrow(
      java.lang.String key) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
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
    if (!getEncodingBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, encoding_);
    }
    if (!body_.isEmpty()) {
      output.writeBytes(2, body_);
    }
    if (originBodySize_ != 0) {
      output.writeInt32(3, originBodySize_);
    }
    com.google.protobuf.GeneratedMessageV3.serializeStringMapTo(
        output, internalGetData(), DataDefaultEntryHolder.defaultEntry, 4);
    unknownFields.writeTo(output);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!getEncodingBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, encoding_);
    }
    if (!body_.isEmpty()) {
      size += com.google.protobuf.CodedOutputStream.computeBytesSize(2, body_);
    }
    if (originBodySize_ != 0) {
      size += com.google.protobuf.CodedOutputStream.computeInt32Size(3, originBodySize_);
    }
    for (java.util.Map.Entry<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
        entry : internalGetData().getMap().entrySet()) {
      com.google.protobuf.MapEntry<
              java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
          data__ =
              DataDefaultEntryHolder.defaultEntry
                  .newBuilderForType()
                  .setKey(entry.getKey())
                  .setValue(entry.getValue())
                  .build();
      size += com.google.protobuf.CodedOutputStream.computeMessageSize(4, data__);
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
    if (!(obj instanceof com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb)) {
      return super.equals(obj);
    }
    com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb other =
        (com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb) obj;

    boolean result = true;
    result = result && getEncoding().equals(other.getEncoding());
    result = result && getBody().equals(other.getBody());
    result = result && (getOriginBodySize() == other.getOriginBodySize());
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
    hash = (37 * hash) + ENCODING_FIELD_NUMBER;
    hash = (53 * hash) + getEncoding().hashCode();
    hash = (37 * hash) + BODY_FIELD_NUMBER;
    hash = (53 * hash) + getBody().hashCode();
    hash = (37 * hash) + ORIGINBODYSIZE_FIELD_NUMBER;
    hash = (53 * hash) + getOriginBodySize();
    if (!internalGetData().getMap().isEmpty()) {
      hash = (37 * hash) + DATA_FIELD_NUMBER;
      hash = (53 * hash) + internalGetData().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb parseFrom(
      java.nio.ByteBuffer data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb parseFrom(
      java.nio.ByteBuffer data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb parseFrom(
      byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb parseFrom(
      byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb parseFrom(
      java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb parseFrom(
      java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb
      parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb
      parseDelimitedFrom(
          java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb parseFrom(
      com.google.protobuf.CodedInputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb parseFrom(
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
      com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb prototype) {
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
  /** Protobuf type {@code FullReceivedDataBodyPb} */
  public static final class Builder extends com.google.protobuf.GeneratedMessageV3.Builder<Builder>
      implements
      // @@protoc_insertion_point(builder_implements:FullReceivedDataBodyPb)
      com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPbOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
      return com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPbOuterClass
          .internal_static_FullReceivedDataBodyPb_descriptor;
    }

    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapField internalGetMapField(int number) {
      switch (number) {
        case 4:
          return internalGetData();
        default:
          throw new RuntimeException("Invalid map field number: " + number);
      }
    }

    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapField internalGetMutableMapField(int number) {
      switch (number) {
        case 4:
          return internalGetMutableData();
        default:
          throw new RuntimeException("Invalid map field number: " + number);
      }
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPbOuterClass
          .internal_static_FullReceivedDataBodyPb_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb.class,
              com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb.Builder.class);
    }

    // Construct using
    // com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb.newBuilder()
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
      encoding_ = "";

      body_ = com.google.protobuf.ByteString.EMPTY;

      originBodySize_ = 0;

      internalGetMutableData().clear();
      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
      return com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPbOuterClass
          .internal_static_FullReceivedDataBodyPb_descriptor;
    }

    public com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb
        getDefaultInstanceForType() {
      return com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb
          .getDefaultInstance();
    }

    public com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb build() {
      com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb result =
          buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb buildPartial() {
      com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb result =
          new com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb(this);
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      result.encoding_ = encoding_;
      result.body_ = body_;
      result.originBodySize_ = originBodySize_;
      result.data_ = internalGetData();
      result.data_.makeImmutable();
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
      if (other instanceof com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb) {
        return mergeFrom(
            (com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb) other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(
        com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb other) {
      if (other
          == com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb
              .getDefaultInstance()) return this;
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
      com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage =
            (com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb)
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

    private java.lang.Object encoding_ = "";
    /** <code>string encoding = 1;</code> */
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
    /** <code>string encoding = 1;</code> */
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
    /** <code>string encoding = 1;</code> */
    public Builder setEncoding(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      encoding_ = value;
      onChanged();
      return this;
    }
    /** <code>string encoding = 1;</code> */
    public Builder clearEncoding() {

      encoding_ = getDefaultInstance().getEncoding();
      onChanged();
      return this;
    }
    /** <code>string encoding = 1;</code> */
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
    /** <code>bytes body = 2;</code> */
    public com.google.protobuf.ByteString getBody() {
      return body_;
    }
    /** <code>bytes body = 2;</code> */
    public Builder setBody(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }

      body_ = value;
      onChanged();
      return this;
    }
    /** <code>bytes body = 2;</code> */
    public Builder clearBody() {

      body_ = getDefaultInstance().getBody();
      onChanged();
      return this;
    }

    private int originBodySize_;
    /** <code>int32 originBodySize = 3;</code> */
    public int getOriginBodySize() {
      return originBodySize_;
    }
    /** <code>int32 originBodySize = 3;</code> */
    public Builder setOriginBodySize(int value) {

      originBodySize_ = value;
      onChanged();
      return this;
    }
    /** <code>int32 originBodySize = 3;</code> */
    public Builder clearOriginBodySize() {

      originBodySize_ = 0;
      onChanged();
      return this;
    }

    private com.google.protobuf.MapField<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
        data_;

    private com.google.protobuf.MapField<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
        internalGetData() {
      if (data_ == null) {
        return com.google.protobuf.MapField.emptyMapField(DataDefaultEntryHolder.defaultEntry);
      }
      return data_;
    }

    private com.google.protobuf.MapField<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
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
    /** <code>map&lt;string, .PushResourcesPb&gt; data = 4;</code> */
    public boolean containsData(java.lang.String key) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      return internalGetData().getMap().containsKey(key);
    }
    /** Use {@link #getDataMap()} instead. */
    @java.lang.Deprecated
    public java.util.Map<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
        getData() {
      return getDataMap();
    }
    /** <code>map&lt;string, .PushResourcesPb&gt; data = 4;</code> */
    public java.util.Map<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
        getDataMap() {
      return internalGetData().getMap();
    }
    /** <code>map&lt;string, .PushResourcesPb&gt; data = 4;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb getDataOrDefault(
        java.lang.String key,
        com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb defaultValue) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      java.util.Map<
              java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
          map = internalGetData().getMap();
      return map.containsKey(key) ? map.get(key) : defaultValue;
    }
    /** <code>map&lt;string, .PushResourcesPb&gt; data = 4;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb getDataOrThrow(
        java.lang.String key) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      java.util.Map<
              java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
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
    /** <code>map&lt;string, .PushResourcesPb&gt; data = 4;</code> */
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
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
        getMutableData() {
      return internalGetMutableData().getMutableMap();
    }
    /** <code>map&lt;string, .PushResourcesPb&gt; data = 4;</code> */
    public Builder putData(
        java.lang.String key,
        com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb value) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      if (value == null) {
        throw new java.lang.NullPointerException();
      }
      internalGetMutableData().getMutableMap().put(key, value);
      return this;
    }
    /** <code>map&lt;string, .PushResourcesPb&gt; data = 4;</code> */
    public Builder putAllData(
        java.util.Map<
                java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
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

    // @@protoc_insertion_point(builder_scope:FullReceivedDataBodyPb)
  }

  // @@protoc_insertion_point(class_scope:FullReceivedDataBodyPb)
  private static final com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb
      DEFAULT_INSTANCE;

  static {
    DEFAULT_INSTANCE = new com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb();
  }

  public static com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb
      getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<FullReceivedDataBodyPb> PARSER =
      new com.google.protobuf.AbstractParser<FullReceivedDataBodyPb>() {
        public FullReceivedDataBodyPb parsePartialFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
          return new FullReceivedDataBodyPb(input, extensionRegistry);
        }
      };

  public static com.google.protobuf.Parser<FullReceivedDataBodyPb> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<FullReceivedDataBodyPb> getParserForType() {
    return PARSER;
  }

  public com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb
      getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }
}
