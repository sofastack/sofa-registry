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

/** Protobuf type {@code SyncConfigRequestPb} */
public final class SyncConfigRequestPb extends com.google.protobuf.GeneratedMessageV3
    implements
    // @@protoc_insertion_point(message_implements:SyncConfigRequestPb)
    SyncConfigRequestPbOrBuilder {
  private static final long serialVersionUID = 0L;
  // Use SyncConfigRequestPb.newBuilder() to construct.
  private SyncConfigRequestPb(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }

  private SyncConfigRequestPb() {
    dataCenter_ = "";
    zone_ = "";
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
    return this.unknownFields;
  }

  private SyncConfigRequestPb(
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

              dataCenter_ = s;
              break;
            }
          case 18:
            {
              java.lang.String s = input.readStringRequireUtf8();

              zone_ = s;
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
    return com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPbOuterClass
        .internal_static_SyncConfigRequestPb_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPbOuterClass
        .internal_static_SyncConfigRequestPb_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb.class,
            com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb.Builder.class);
  }

  public static final int DATACENTER_FIELD_NUMBER = 1;
  private volatile java.lang.Object dataCenter_;
  /** <code>string dataCenter = 1;</code> */
  public java.lang.String getDataCenter() {
    java.lang.Object ref = dataCenter_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      dataCenter_ = s;
      return s;
    }
  }
  /** <code>string dataCenter = 1;</code> */
  public com.google.protobuf.ByteString getDataCenterBytes() {
    java.lang.Object ref = dataCenter_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b =
          com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
      dataCenter_ = b;
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

  private byte memoizedIsInitialized = -1;

  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
    if (!getDataCenterBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, dataCenter_);
    }
    if (!getZoneBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 2, zone_);
    }
    unknownFields.writeTo(output);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!getDataCenterBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, dataCenter_);
    }
    if (!getZoneBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, zone_);
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
    if (!(obj instanceof com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb)) {
      return super.equals(obj);
    }
    com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb other =
        (com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb) obj;

    boolean result = true;
    result = result && getDataCenter().equals(other.getDataCenter());
    result = result && getZone().equals(other.getZone());
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
    hash = (37 * hash) + DATACENTER_FIELD_NUMBER;
    hash = (53 * hash) + getDataCenter().hashCode();
    hash = (37 * hash) + ZONE_FIELD_NUMBER;
    hash = (53 * hash) + getZone().hashCode();
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb parseFrom(
      java.nio.ByteBuffer data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb parseFrom(
      java.nio.ByteBuffer data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb parseFrom(
      byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb parseFrom(
      byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb parseFrom(
      java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb parseFrom(
      java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb
      parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb
      parseDelimitedFrom(
          java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb parseFrom(
      com.google.protobuf.CodedInputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb parseFrom(
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
      com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb prototype) {
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
  /** Protobuf type {@code SyncConfigRequestPb} */
  public static final class Builder extends com.google.protobuf.GeneratedMessageV3.Builder<Builder>
      implements
      // @@protoc_insertion_point(builder_implements:SyncConfigRequestPb)
      com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPbOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
      return com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPbOuterClass
          .internal_static_SyncConfigRequestPb_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPbOuterClass
          .internal_static_SyncConfigRequestPb_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb.class,
              com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb.Builder.class);
    }

    // Construct using
    // com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb.newBuilder()
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
      dataCenter_ = "";

      zone_ = "";

      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
      return com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPbOuterClass
          .internal_static_SyncConfigRequestPb_descriptor;
    }

    public com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb
        getDefaultInstanceForType() {
      return com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb
          .getDefaultInstance();
    }

    public com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb build() {
      com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb buildPartial() {
      com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb result =
          new com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb(this);
      result.dataCenter_ = dataCenter_;
      result.zone_ = zone_;
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
      if (other instanceof com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb) {
        return mergeFrom(
            (com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb) other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(
        com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb other) {
      if (other
          == com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb
              .getDefaultInstance()) return this;
      if (!other.getDataCenter().isEmpty()) {
        dataCenter_ = other.dataCenter_;
        onChanged();
      }
      if (!other.getZone().isEmpty()) {
        zone_ = other.zone_;
        onChanged();
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
      com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage =
            (com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb)
                e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private java.lang.Object dataCenter_ = "";
    /** <code>string dataCenter = 1;</code> */
    public java.lang.String getDataCenter() {
      java.lang.Object ref = dataCenter_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        dataCenter_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /** <code>string dataCenter = 1;</code> */
    public com.google.protobuf.ByteString getDataCenterBytes() {
      java.lang.Object ref = dataCenter_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
            com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
        dataCenter_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /** <code>string dataCenter = 1;</code> */
    public Builder setDataCenter(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      dataCenter_ = value;
      onChanged();
      return this;
    }
    /** <code>string dataCenter = 1;</code> */
    public Builder clearDataCenter() {

      dataCenter_ = getDefaultInstance().getDataCenter();
      onChanged();
      return this;
    }
    /** <code>string dataCenter = 1;</code> */
    public Builder setDataCenterBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);

      dataCenter_ = value;
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

    public final Builder setUnknownFields(final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFieldsProto3(unknownFields);
    }

    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }

    // @@protoc_insertion_point(builder_scope:SyncConfigRequestPb)
  }

  // @@protoc_insertion_point(class_scope:SyncConfigRequestPb)
  private static final com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb
      DEFAULT_INSTANCE;

  static {
    DEFAULT_INSTANCE = new com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb();
  }

  public static com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb
      getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<SyncConfigRequestPb> PARSER =
      new com.google.protobuf.AbstractParser<SyncConfigRequestPb>() {
        public SyncConfigRequestPb parsePartialFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
          return new SyncConfigRequestPb(input, extensionRegistry);
        }
      };

  public static com.google.protobuf.Parser<SyncConfigRequestPb> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<SyncConfigRequestPb> getParserForType() {
    return PARSER;
  }

  public com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb
      getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }
}
