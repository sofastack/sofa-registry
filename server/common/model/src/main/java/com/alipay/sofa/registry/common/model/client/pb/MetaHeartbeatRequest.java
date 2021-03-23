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

/** Protobuf type {@code MetaHeartbeatRequest} */
public final class MetaHeartbeatRequest extends com.google.protobuf.GeneratedMessageV3
    implements
    // @@protoc_insertion_point(message_implements:MetaHeartbeatRequest)
    MetaHeartbeatRequestOrBuilder {
  private static final long serialVersionUID = 0L;
  // Use MetaHeartbeatRequest.newBuilder() to construct.
  private MetaHeartbeatRequest(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }

  private MetaHeartbeatRequest() {
    revisions_ = com.google.protobuf.LazyStringArrayList.EMPTY;
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
    return this.unknownFields;
  }

  private MetaHeartbeatRequest(
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
              if (!((mutable_bitField0_ & 0x00000001) == 0x00000001)) {
                revisions_ = new com.google.protobuf.LazyStringArrayList();
                mutable_bitField0_ |= 0x00000001;
              }
              revisions_.add(s);
              break;
            }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(e).setUnfinishedMessage(this);
    } finally {
      if (((mutable_bitField0_ & 0x00000001) == 0x00000001)) {
        revisions_ = revisions_.getUnmodifiableView();
      }
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }

  public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
    return com.alipay.sofa.registry.common.model.client.pb.AppDiscoveryMetaPb
        .internal_static_MetaHeartbeatRequest_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.alipay.sofa.registry.common.model.client.pb.AppDiscoveryMetaPb
        .internal_static_MetaHeartbeatRequest_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequest.class,
            com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequest.Builder.class);
  }

  public static final int REVISIONS_FIELD_NUMBER = 1;
  private com.google.protobuf.LazyStringList revisions_;
  /** <code>repeated string revisions = 1;</code> */
  public com.google.protobuf.ProtocolStringList getRevisionsList() {
    return revisions_;
  }
  /** <code>repeated string revisions = 1;</code> */
  public int getRevisionsCount() {
    return revisions_.size();
  }
  /** <code>repeated string revisions = 1;</code> */
  public java.lang.String getRevisions(int index) {
    return revisions_.get(index);
  }
  /** <code>repeated string revisions = 1;</code> */
  public com.google.protobuf.ByteString getRevisionsBytes(int index) {
    return revisions_.getByteString(index);
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
    for (int i = 0; i < revisions_.size(); i++) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, revisions_.getRaw(i));
    }
    unknownFields.writeTo(output);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    {
      int dataSize = 0;
      for (int i = 0; i < revisions_.size(); i++) {
        dataSize += computeStringSizeNoTag(revisions_.getRaw(i));
      }
      size += dataSize;
      size += 1 * getRevisionsList().size();
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
    if (!(obj instanceof com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequest)) {
      return super.equals(obj);
    }
    com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequest other =
        (com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequest) obj;

    boolean result = true;
    result = result && getRevisionsList().equals(other.getRevisionsList());
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
    if (getRevisionsCount() > 0) {
      hash = (37 * hash) + REVISIONS_FIELD_NUMBER;
      hash = (53 * hash) + getRevisionsList().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequest parseFrom(
      java.nio.ByteBuffer data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequest parseFrom(
      java.nio.ByteBuffer data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequest parseFrom(
      byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequest parseFrom(
      byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequest parseFrom(
      java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequest parseFrom(
      java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequest
      parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequest
      parseDelimitedFrom(
          java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequest parseFrom(
      com.google.protobuf.CodedInputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequest parseFrom(
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
      com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequest prototype) {
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
  /** Protobuf type {@code MetaHeartbeatRequest} */
  public static final class Builder extends com.google.protobuf.GeneratedMessageV3.Builder<Builder>
      implements
      // @@protoc_insertion_point(builder_implements:MetaHeartbeatRequest)
      com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequestOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
      return com.alipay.sofa.registry.common.model.client.pb.AppDiscoveryMetaPb
          .internal_static_MetaHeartbeatRequest_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.alipay.sofa.registry.common.model.client.pb.AppDiscoveryMetaPb
          .internal_static_MetaHeartbeatRequest_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequest.class,
              com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequest.Builder.class);
    }

    // Construct using
    // com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequest.newBuilder()
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
      revisions_ = com.google.protobuf.LazyStringArrayList.EMPTY;
      bitField0_ = (bitField0_ & ~0x00000001);
      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
      return com.alipay.sofa.registry.common.model.client.pb.AppDiscoveryMetaPb
          .internal_static_MetaHeartbeatRequest_descriptor;
    }

    public com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequest
        getDefaultInstanceForType() {
      return com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequest
          .getDefaultInstance();
    }

    public com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequest build() {
      com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequest result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequest buildPartial() {
      com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequest result =
          new com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequest(this);
      int from_bitField0_ = bitField0_;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        revisions_ = revisions_.getUnmodifiableView();
        bitField0_ = (bitField0_ & ~0x00000001);
      }
      result.revisions_ = revisions_;
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
      if (other instanceof com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequest) {
        return mergeFrom(
            (com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequest) other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(
        com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequest other) {
      if (other
          == com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequest
              .getDefaultInstance()) return this;
      if (!other.revisions_.isEmpty()) {
        if (revisions_.isEmpty()) {
          revisions_ = other.revisions_;
          bitField0_ = (bitField0_ & ~0x00000001);
        } else {
          ensureRevisionsIsMutable();
          revisions_.addAll(other.revisions_);
        }
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
      com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequest parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage =
            (com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequest)
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

    private com.google.protobuf.LazyStringList revisions_ =
        com.google.protobuf.LazyStringArrayList.EMPTY;

    private void ensureRevisionsIsMutable() {
      if (!((bitField0_ & 0x00000001) == 0x00000001)) {
        revisions_ = new com.google.protobuf.LazyStringArrayList(revisions_);
        bitField0_ |= 0x00000001;
      }
    }
    /** <code>repeated string revisions = 1;</code> */
    public com.google.protobuf.ProtocolStringList getRevisionsList() {
      return revisions_.getUnmodifiableView();
    }
    /** <code>repeated string revisions = 1;</code> */
    public int getRevisionsCount() {
      return revisions_.size();
    }
    /** <code>repeated string revisions = 1;</code> */
    public java.lang.String getRevisions(int index) {
      return revisions_.get(index);
    }
    /** <code>repeated string revisions = 1;</code> */
    public com.google.protobuf.ByteString getRevisionsBytes(int index) {
      return revisions_.getByteString(index);
    }
    /** <code>repeated string revisions = 1;</code> */
    public Builder setRevisions(int index, java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }
      ensureRevisionsIsMutable();
      revisions_.set(index, value);
      onChanged();
      return this;
    }
    /** <code>repeated string revisions = 1;</code> */
    public Builder addRevisions(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }
      ensureRevisionsIsMutable();
      revisions_.add(value);
      onChanged();
      return this;
    }
    /** <code>repeated string revisions = 1;</code> */
    public Builder addAllRevisions(java.lang.Iterable<java.lang.String> values) {
      ensureRevisionsIsMutable();
      com.google.protobuf.AbstractMessageLite.Builder.addAll(values, revisions_);
      onChanged();
      return this;
    }
    /** <code>repeated string revisions = 1;</code> */
    public Builder clearRevisions() {
      revisions_ = com.google.protobuf.LazyStringArrayList.EMPTY;
      bitField0_ = (bitField0_ & ~0x00000001);
      onChanged();
      return this;
    }
    /** <code>repeated string revisions = 1;</code> */
    public Builder addRevisionsBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);
      ensureRevisionsIsMutable();
      revisions_.add(value);
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

    // @@protoc_insertion_point(builder_scope:MetaHeartbeatRequest)
  }

  // @@protoc_insertion_point(class_scope:MetaHeartbeatRequest)
  private static final com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequest
      DEFAULT_INSTANCE;

  static {
    DEFAULT_INSTANCE = new com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequest();
  }

  public static com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequest
      getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<MetaHeartbeatRequest> PARSER =
      new com.google.protobuf.AbstractParser<MetaHeartbeatRequest>() {
        public MetaHeartbeatRequest parsePartialFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
          return new MetaHeartbeatRequest(input, extensionRegistry);
        }
      };

  public static com.google.protobuf.Parser<MetaHeartbeatRequest> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<MetaHeartbeatRequest> getParserForType() {
    return PARSER;
  }

  public com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatRequest
      getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }
}
