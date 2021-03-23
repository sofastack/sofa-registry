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

/** Protobuf type {@code GetRevisionsResponse} */
public final class GetRevisionsResponse extends com.google.protobuf.GeneratedMessageV3
    implements
    // @@protoc_insertion_point(message_implements:GetRevisionsResponse)
    GetRevisionsResponseOrBuilder {
  private static final long serialVersionUID = 0L;
  // Use GetRevisionsResponse.newBuilder() to construct.
  private GetRevisionsResponse(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }

  private GetRevisionsResponse() {
    statusCode_ = 0;
    message_ = "";
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
    return this.unknownFields;
  }

  private GetRevisionsResponse(
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
                revisions_ =
                    com.google.protobuf.MapField.newMapField(
                        RevisionsDefaultEntryHolder.defaultEntry);
                mutable_bitField0_ |= 0x00000001;
              }
              com.google.protobuf.MapEntry<
                      java.lang.String,
                      com.alipay.sofa.registry.common.model.client.pb.MetaRegister>
                  revisions__ =
                      input.readMessage(
                          RevisionsDefaultEntryHolder.defaultEntry.getParserForType(),
                          extensionRegistry);
              revisions_.getMutableMap().put(revisions__.getKey(), revisions__.getValue());
              break;
            }
          case 16:
            {
              statusCode_ = input.readInt32();
              break;
            }
          case 26:
            {
              java.lang.String s = input.readStringRequireUtf8();

              message_ = s;
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
        .internal_static_GetRevisionsResponse_descriptor;
  }

  @SuppressWarnings({"rawtypes"})
  protected com.google.protobuf.MapField internalGetMapField(int number) {
    switch (number) {
      case 1:
        return internalGetRevisions();
      default:
        throw new RuntimeException("Invalid map field number: " + number);
    }
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.alipay.sofa.registry.common.model.client.pb.AppDiscoveryMetaPb
        .internal_static_GetRevisionsResponse_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse.class,
            com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse.Builder.class);
  }

  private int bitField0_;
  public static final int REVISIONS_FIELD_NUMBER = 1;

  private static final class RevisionsDefaultEntryHolder {
    static final com.google.protobuf.MapEntry<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaRegister>
        defaultEntry =
            com.google.protobuf.MapEntry
                .<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaRegister>
                    newDefaultInstance(
                        com.alipay.sofa.registry.common.model.client.pb.AppDiscoveryMetaPb
                            .internal_static_GetRevisionsResponse_RevisionsEntry_descriptor,
                        com.google.protobuf.WireFormat.FieldType.STRING,
                        "",
                        com.google.protobuf.WireFormat.FieldType.MESSAGE,
                        com.alipay.sofa.registry.common.model.client.pb.MetaRegister
                            .getDefaultInstance());
  }

  private com.google.protobuf.MapField<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaRegister>
      revisions_;

  private com.google.protobuf.MapField<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaRegister>
      internalGetRevisions() {
    if (revisions_ == null) {
      return com.google.protobuf.MapField.emptyMapField(RevisionsDefaultEntryHolder.defaultEntry);
    }
    return revisions_;
  }

  public int getRevisionsCount() {
    return internalGetRevisions().getMap().size();
  }
  /** <code>map&lt;string, .MetaRegister&gt; revisions = 1;</code> */
  public boolean containsRevisions(java.lang.String key) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    return internalGetRevisions().getMap().containsKey(key);
  }
  /** Use {@link #getRevisionsMap()} instead. */
  @java.lang.Deprecated
  public java.util.Map<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaRegister>
      getRevisions() {
    return getRevisionsMap();
  }
  /** <code>map&lt;string, .MetaRegister&gt; revisions = 1;</code> */
  public java.util.Map<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaRegister>
      getRevisionsMap() {
    return internalGetRevisions().getMap();
  }
  /** <code>map&lt;string, .MetaRegister&gt; revisions = 1;</code> */
  public com.alipay.sofa.registry.common.model.client.pb.MetaRegister getRevisionsOrDefault(
      java.lang.String key,
      com.alipay.sofa.registry.common.model.client.pb.MetaRegister defaultValue) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaRegister>
        map = internalGetRevisions().getMap();
    return map.containsKey(key) ? map.get(key) : defaultValue;
  }
  /** <code>map&lt;string, .MetaRegister&gt; revisions = 1;</code> */
  public com.alipay.sofa.registry.common.model.client.pb.MetaRegister getRevisionsOrThrow(
      java.lang.String key) {
    if (key == null) {
      throw new java.lang.NullPointerException();
    }
    java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaRegister>
        map = internalGetRevisions().getMap();
    if (!map.containsKey(key)) {
      throw new java.lang.IllegalArgumentException();
    }
    return map.get(key);
  }

  public static final int STATUSCODE_FIELD_NUMBER = 2;
  private int statusCode_;
  /** <code>int32 statusCode = 2;</code> */
  public int getStatusCode() {
    return statusCode_;
  }

  public static final int MESSAGE_FIELD_NUMBER = 3;
  private volatile java.lang.Object message_;
  /** <code>string message = 3;</code> */
  public java.lang.String getMessage() {
    java.lang.Object ref = message_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      message_ = s;
      return s;
    }
  }
  /** <code>string message = 3;</code> */
  public com.google.protobuf.ByteString getMessageBytes() {
    java.lang.Object ref = message_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b =
          com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
      message_ = b;
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
    com.google.protobuf.GeneratedMessageV3.serializeStringMapTo(
        output, internalGetRevisions(), RevisionsDefaultEntryHolder.defaultEntry, 1);
    if (statusCode_ != 0) {
      output.writeInt32(2, statusCode_);
    }
    if (!getMessageBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 3, message_);
    }
    unknownFields.writeTo(output);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    for (java.util.Map.Entry<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaRegister>
        entry : internalGetRevisions().getMap().entrySet()) {
      com.google.protobuf.MapEntry<
              java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaRegister>
          revisions__ =
              RevisionsDefaultEntryHolder.defaultEntry
                  .newBuilderForType()
                  .setKey(entry.getKey())
                  .setValue(entry.getValue())
                  .build();
      size += com.google.protobuf.CodedOutputStream.computeMessageSize(1, revisions__);
    }
    if (statusCode_ != 0) {
      size += com.google.protobuf.CodedOutputStream.computeInt32Size(2, statusCode_);
    }
    if (!getMessageBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(3, message_);
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
    if (!(obj instanceof com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse)) {
      return super.equals(obj);
    }
    com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse other =
        (com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse) obj;

    boolean result = true;
    result = result && internalGetRevisions().equals(other.internalGetRevisions());
    result = result && (getStatusCode() == other.getStatusCode());
    result = result && getMessage().equals(other.getMessage());
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
    if (!internalGetRevisions().getMap().isEmpty()) {
      hash = (37 * hash) + REVISIONS_FIELD_NUMBER;
      hash = (53 * hash) + internalGetRevisions().hashCode();
    }
    hash = (37 * hash) + STATUSCODE_FIELD_NUMBER;
    hash = (53 * hash) + getStatusCode();
    hash = (37 * hash) + MESSAGE_FIELD_NUMBER;
    hash = (53 * hash) + getMessage().hashCode();
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse parseFrom(
      java.nio.ByteBuffer data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse parseFrom(
      java.nio.ByteBuffer data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse parseFrom(
      byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse parseFrom(
      byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse parseFrom(
      java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse parseFrom(
      java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse
      parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse
      parseDelimitedFrom(
          java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse parseFrom(
      com.google.protobuf.CodedInputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse parseFrom(
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
      com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse prototype) {
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
  /** Protobuf type {@code GetRevisionsResponse} */
  public static final class Builder extends com.google.protobuf.GeneratedMessageV3.Builder<Builder>
      implements
      // @@protoc_insertion_point(builder_implements:GetRevisionsResponse)
      com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponseOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
      return com.alipay.sofa.registry.common.model.client.pb.AppDiscoveryMetaPb
          .internal_static_GetRevisionsResponse_descriptor;
    }

    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapField internalGetMapField(int number) {
      switch (number) {
        case 1:
          return internalGetRevisions();
        default:
          throw new RuntimeException("Invalid map field number: " + number);
      }
    }

    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapField internalGetMutableMapField(int number) {
      switch (number) {
        case 1:
          return internalGetMutableRevisions();
        default:
          throw new RuntimeException("Invalid map field number: " + number);
      }
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.alipay.sofa.registry.common.model.client.pb.AppDiscoveryMetaPb
          .internal_static_GetRevisionsResponse_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse.class,
              com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse.Builder.class);
    }

    // Construct using
    // com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse.newBuilder()
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
      internalGetMutableRevisions().clear();
      statusCode_ = 0;

      message_ = "";

      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
      return com.alipay.sofa.registry.common.model.client.pb.AppDiscoveryMetaPb
          .internal_static_GetRevisionsResponse_descriptor;
    }

    public com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse
        getDefaultInstanceForType() {
      return com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse
          .getDefaultInstance();
    }

    public com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse build() {
      com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse buildPartial() {
      com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse result =
          new com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse(this);
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      result.revisions_ = internalGetRevisions();
      result.revisions_.makeImmutable();
      result.statusCode_ = statusCode_;
      result.message_ = message_;
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
      if (other instanceof com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse) {
        return mergeFrom(
            (com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse) other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(
        com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse other) {
      if (other
          == com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse
              .getDefaultInstance()) return this;
      internalGetMutableRevisions().mergeFrom(other.internalGetRevisions());
      if (other.getStatusCode() != 0) {
        setStatusCode(other.getStatusCode());
      }
      if (!other.getMessage().isEmpty()) {
        message_ = other.message_;
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
      com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage =
            (com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse)
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
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaRegister>
        revisions_;

    private com.google.protobuf.MapField<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaRegister>
        internalGetRevisions() {
      if (revisions_ == null) {
        return com.google.protobuf.MapField.emptyMapField(RevisionsDefaultEntryHolder.defaultEntry);
      }
      return revisions_;
    }

    private com.google.protobuf.MapField<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaRegister>
        internalGetMutableRevisions() {
      onChanged();
      ;
      if (revisions_ == null) {
        revisions_ =
            com.google.protobuf.MapField.newMapField(RevisionsDefaultEntryHolder.defaultEntry);
      }
      if (!revisions_.isMutable()) {
        revisions_ = revisions_.copy();
      }
      return revisions_;
    }

    public int getRevisionsCount() {
      return internalGetRevisions().getMap().size();
    }
    /** <code>map&lt;string, .MetaRegister&gt; revisions = 1;</code> */
    public boolean containsRevisions(java.lang.String key) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      return internalGetRevisions().getMap().containsKey(key);
    }
    /** Use {@link #getRevisionsMap()} instead. */
    @java.lang.Deprecated
    public java.util.Map<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaRegister>
        getRevisions() {
      return getRevisionsMap();
    }
    /** <code>map&lt;string, .MetaRegister&gt; revisions = 1;</code> */
    public java.util.Map<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaRegister>
        getRevisionsMap() {
      return internalGetRevisions().getMap();
    }
    /** <code>map&lt;string, .MetaRegister&gt; revisions = 1;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.MetaRegister getRevisionsOrDefault(
        java.lang.String key,
        com.alipay.sofa.registry.common.model.client.pb.MetaRegister defaultValue) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaRegister>
          map = internalGetRevisions().getMap();
      return map.containsKey(key) ? map.get(key) : defaultValue;
    }
    /** <code>map&lt;string, .MetaRegister&gt; revisions = 1;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.MetaRegister getRevisionsOrThrow(
        java.lang.String key) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaRegister>
          map = internalGetRevisions().getMap();
      if (!map.containsKey(key)) {
        throw new java.lang.IllegalArgumentException();
      }
      return map.get(key);
    }

    public Builder clearRevisions() {
      internalGetMutableRevisions().getMutableMap().clear();
      return this;
    }
    /** <code>map&lt;string, .MetaRegister&gt; revisions = 1;</code> */
    public Builder removeRevisions(java.lang.String key) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      internalGetMutableRevisions().getMutableMap().remove(key);
      return this;
    }
    /** Use alternate mutation accessors instead. */
    @java.lang.Deprecated
    public java.util.Map<
            java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaRegister>
        getMutableRevisions() {
      return internalGetMutableRevisions().getMutableMap();
    }
    /** <code>map&lt;string, .MetaRegister&gt; revisions = 1;</code> */
    public Builder putRevisions(
        java.lang.String key, com.alipay.sofa.registry.common.model.client.pb.MetaRegister value) {
      if (key == null) {
        throw new java.lang.NullPointerException();
      }
      if (value == null) {
        throw new java.lang.NullPointerException();
      }
      internalGetMutableRevisions().getMutableMap().put(key, value);
      return this;
    }
    /** <code>map&lt;string, .MetaRegister&gt; revisions = 1;</code> */
    public Builder putAllRevisions(
        java.util.Map<
                java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaRegister>
            values) {
      internalGetMutableRevisions().getMutableMap().putAll(values);
      return this;
    }

    private int statusCode_;
    /** <code>int32 statusCode = 2;</code> */
    public int getStatusCode() {
      return statusCode_;
    }
    /** <code>int32 statusCode = 2;</code> */
    public Builder setStatusCode(int value) {

      statusCode_ = value;
      onChanged();
      return this;
    }
    /** <code>int32 statusCode = 2;</code> */
    public Builder clearStatusCode() {

      statusCode_ = 0;
      onChanged();
      return this;
    }

    private java.lang.Object message_ = "";
    /** <code>string message = 3;</code> */
    public java.lang.String getMessage() {
      java.lang.Object ref = message_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        message_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /** <code>string message = 3;</code> */
    public com.google.protobuf.ByteString getMessageBytes() {
      java.lang.Object ref = message_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
            com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
        message_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /** <code>string message = 3;</code> */
    public Builder setMessage(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      message_ = value;
      onChanged();
      return this;
    }
    /** <code>string message = 3;</code> */
    public Builder clearMessage() {

      message_ = getDefaultInstance().getMessage();
      onChanged();
      return this;
    }
    /** <code>string message = 3;</code> */
    public Builder setMessageBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);

      message_ = value;
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

    // @@protoc_insertion_point(builder_scope:GetRevisionsResponse)
  }

  // @@protoc_insertion_point(class_scope:GetRevisionsResponse)
  private static final com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse
      DEFAULT_INSTANCE;

  static {
    DEFAULT_INSTANCE = new com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse();
  }

  public static com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse
      getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<GetRevisionsResponse> PARSER =
      new com.google.protobuf.AbstractParser<GetRevisionsResponse>() {
        public GetRevisionsResponse parsePartialFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
          return new GetRevisionsResponse(input, extensionRegistry);
        }
      };

  public static com.google.protobuf.Parser<GetRevisionsResponse> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<GetRevisionsResponse> getParserForType() {
    return PARSER;
  }

  public com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse
      getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }
}
