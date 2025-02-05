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

/** Protobuf type {@code AppList} */
public final class AppList extends com.google.protobuf.GeneratedMessageV3
    implements
    // @@protoc_insertion_point(message_implements:AppList)
    AppListOrBuilder {
  private static final long serialVersionUID = 0L;
  // Use AppList.newBuilder() to construct.
  private AppList(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }

  private AppList() {
    version_ = 0L;
    apps_ = com.google.protobuf.LazyStringArrayList.EMPTY;
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
    return this.unknownFields;
  }

  private AppList(
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
          case 8:
            {
              version_ = input.readInt64();
              break;
            }
          case 18:
            {
              java.lang.String s = input.readStringRequireUtf8();
              if (!((mutable_bitField0_ & 0x00000002) == 0x00000002)) {
                apps_ = new com.google.protobuf.LazyStringArrayList();
                mutable_bitField0_ |= 0x00000002;
              }
              apps_.add(s);
              break;
            }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(e).setUnfinishedMessage(this);
    } finally {
      if (((mutable_bitField0_ & 0x00000002) == 0x00000002)) {
        apps_ = apps_.getUnmodifiableView();
      }
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }

  public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
    return com.alipay.sofa.registry.common.model.client.pb.AppDiscoveryMetaPb
        .internal_static_AppList_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.alipay.sofa.registry.common.model.client.pb.AppDiscoveryMetaPb
        .internal_static_AppList_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.alipay.sofa.registry.common.model.client.pb.AppList.class,
            com.alipay.sofa.registry.common.model.client.pb.AppList.Builder.class);
  }

  private int bitField0_;
  public static final int VERSION_FIELD_NUMBER = 1;
  private long version_;
  /** <code>int64 version = 1;</code> */
  public long getVersion() {
    return version_;
  }

  public static final int APPS_FIELD_NUMBER = 2;
  private com.google.protobuf.LazyStringList apps_;
  /** <code>repeated string apps = 2;</code> */
  public com.google.protobuf.ProtocolStringList getAppsList() {
    return apps_;
  }
  /** <code>repeated string apps = 2;</code> */
  public int getAppsCount() {
    return apps_.size();
  }
  /** <code>repeated string apps = 2;</code> */
  public java.lang.String getApps(int index) {
    return apps_.get(index);
  }
  /** <code>repeated string apps = 2;</code> */
  public com.google.protobuf.ByteString getAppsBytes(int index) {
    return apps_.getByteString(index);
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
    if (version_ != 0L) {
      output.writeInt64(1, version_);
    }
    for (int i = 0; i < apps_.size(); i++) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 2, apps_.getRaw(i));
    }
    unknownFields.writeTo(output);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (version_ != 0L) {
      size += com.google.protobuf.CodedOutputStream.computeInt64Size(1, version_);
    }
    {
      int dataSize = 0;
      for (int i = 0; i < apps_.size(); i++) {
        dataSize += computeStringSizeNoTag(apps_.getRaw(i));
      }
      size += dataSize;
      size += 1 * getAppsList().size();
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
    if (!(obj instanceof com.alipay.sofa.registry.common.model.client.pb.AppList)) {
      return super.equals(obj);
    }
    com.alipay.sofa.registry.common.model.client.pb.AppList other =
        (com.alipay.sofa.registry.common.model.client.pb.AppList) obj;

    boolean result = true;
    result = result && (getVersion() == other.getVersion());
    result = result && getAppsList().equals(other.getAppsList());
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
    hash = (37 * hash) + VERSION_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(getVersion());
    if (getAppsCount() > 0) {
      hash = (37 * hash) + APPS_FIELD_NUMBER;
      hash = (53 * hash) + getAppsList().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.alipay.sofa.registry.common.model.client.pb.AppList parseFrom(
      java.nio.ByteBuffer data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.AppList parseFrom(
      java.nio.ByteBuffer data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.AppList parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.AppList parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.AppList parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.AppList parseFrom(
      byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.AppList parseFrom(
      java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.AppList parseFrom(
      java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.AppList parseDelimitedFrom(
      java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.AppList parseDelimitedFrom(
      java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.AppList parseFrom(
      com.google.protobuf.CodedInputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.AppList parseFrom(
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
      com.alipay.sofa.registry.common.model.client.pb.AppList prototype) {
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
  /** Protobuf type {@code AppList} */
  public static final class Builder extends com.google.protobuf.GeneratedMessageV3.Builder<Builder>
      implements
      // @@protoc_insertion_point(builder_implements:AppList)
      com.alipay.sofa.registry.common.model.client.pb.AppListOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
      return com.alipay.sofa.registry.common.model.client.pb.AppDiscoveryMetaPb
          .internal_static_AppList_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.alipay.sofa.registry.common.model.client.pb.AppDiscoveryMetaPb
          .internal_static_AppList_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.alipay.sofa.registry.common.model.client.pb.AppList.class,
              com.alipay.sofa.registry.common.model.client.pb.AppList.Builder.class);
    }

    // Construct using com.alipay.sofa.registry.common.model.client.pb.AppList.newBuilder()
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
      version_ = 0L;

      apps_ = com.google.protobuf.LazyStringArrayList.EMPTY;
      bitField0_ = (bitField0_ & ~0x00000002);
      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
      return com.alipay.sofa.registry.common.model.client.pb.AppDiscoveryMetaPb
          .internal_static_AppList_descriptor;
    }

    public com.alipay.sofa.registry.common.model.client.pb.AppList getDefaultInstanceForType() {
      return com.alipay.sofa.registry.common.model.client.pb.AppList.getDefaultInstance();
    }

    public com.alipay.sofa.registry.common.model.client.pb.AppList build() {
      com.alipay.sofa.registry.common.model.client.pb.AppList result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public com.alipay.sofa.registry.common.model.client.pb.AppList buildPartial() {
      com.alipay.sofa.registry.common.model.client.pb.AppList result =
          new com.alipay.sofa.registry.common.model.client.pb.AppList(this);
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      result.version_ = version_;
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        apps_ = apps_.getUnmodifiableView();
        bitField0_ = (bitField0_ & ~0x00000002);
      }
      result.apps_ = apps_;
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
      if (other instanceof com.alipay.sofa.registry.common.model.client.pb.AppList) {
        return mergeFrom((com.alipay.sofa.registry.common.model.client.pb.AppList) other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.alipay.sofa.registry.common.model.client.pb.AppList other) {
      if (other == com.alipay.sofa.registry.common.model.client.pb.AppList.getDefaultInstance())
        return this;
      if (other.getVersion() != 0L) {
        setVersion(other.getVersion());
      }
      if (!other.apps_.isEmpty()) {
        if (apps_.isEmpty()) {
          apps_ = other.apps_;
          bitField0_ = (bitField0_ & ~0x00000002);
        } else {
          ensureAppsIsMutable();
          apps_.addAll(other.apps_);
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
      com.alipay.sofa.registry.common.model.client.pb.AppList parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage =
            (com.alipay.sofa.registry.common.model.client.pb.AppList) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private int bitField0_;

    private long version_;
    /** <code>int64 version = 1;</code> */
    public long getVersion() {
      return version_;
    }
    /**
     * <code>int64 version = 1;</code>
     *
     * @param value value
     * @return Builder
     */
    public Builder setVersion(long value) {

      version_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>int64 version = 1;</code>
     *
     * @return Builder
     */
    public Builder clearVersion() {

      version_ = 0L;
      onChanged();
      return this;
    }

    private com.google.protobuf.LazyStringList apps_ =
        com.google.protobuf.LazyStringArrayList.EMPTY;

    private void ensureAppsIsMutable() {
      if (!((bitField0_ & 0x00000002) == 0x00000002)) {
        apps_ = new com.google.protobuf.LazyStringArrayList(apps_);
        bitField0_ |= 0x00000002;
      }
    }
    /** <code>repeated string apps = 2;</code> */
    public com.google.protobuf.ProtocolStringList getAppsList() {
      return apps_.getUnmodifiableView();
    }
    /** <code>repeated string apps = 2;</code> */
    public int getAppsCount() {
      return apps_.size();
    }
    /** <code>repeated string apps = 2;</code> */
    public java.lang.String getApps(int index) {
      return apps_.get(index);
    }
    /** <code>repeated string apps = 2;</code> */
    public com.google.protobuf.ByteString getAppsBytes(int index) {
      return apps_.getByteString(index);
    }
    /**
     * <code>repeated string apps = 2;</code>
     *
     * @param index index
     * @param value value
     * @return Builder
     */
    public Builder setApps(int index, java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }
      ensureAppsIsMutable();
      apps_.set(index, value);
      onChanged();
      return this;
    }
    /** <code>repeated string apps = 2;</code> */
    public Builder addApps(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }
      ensureAppsIsMutable();
      apps_.add(value);
      onChanged();
      return this;
    }
    /** <code>repeated string apps = 2;</code> */
    public Builder addAllApps(java.lang.Iterable<java.lang.String> values) {
      ensureAppsIsMutable();
      com.google.protobuf.AbstractMessageLite.Builder.addAll(values, apps_);
      onChanged();
      return this;
    }
    /** <code>repeated string apps = 2;</code> */
    public Builder clearApps() {
      apps_ = com.google.protobuf.LazyStringArrayList.EMPTY;
      bitField0_ = (bitField0_ & ~0x00000002);
      onChanged();
      return this;
    }
    /** <code>repeated string apps = 2;</code> */
    public Builder addAppsBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);
      ensureAppsIsMutable();
      apps_.add(value);
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

    // @@protoc_insertion_point(builder_scope:AppList)
  }

  // @@protoc_insertion_point(class_scope:AppList)
  private static final com.alipay.sofa.registry.common.model.client.pb.AppList DEFAULT_INSTANCE;

  static {
    DEFAULT_INSTANCE = new com.alipay.sofa.registry.common.model.client.pb.AppList();
  }

  public static com.alipay.sofa.registry.common.model.client.pb.AppList getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<AppList> PARSER =
      new com.google.protobuf.AbstractParser<AppList>() {
        public AppList parsePartialFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
          return new AppList(input, extensionRegistry);
        }
      };

  public static com.google.protobuf.Parser<AppList> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<AppList> getParserForType() {
    return PARSER;
  }

  public com.alipay.sofa.registry.common.model.client.pb.AppList getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }
}
