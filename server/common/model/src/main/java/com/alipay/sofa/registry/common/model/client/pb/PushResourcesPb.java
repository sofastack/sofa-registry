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

/** Protobuf type {@code PushResourcesPb} */
public final class PushResourcesPb extends com.google.protobuf.GeneratedMessageV3
    implements
    // @@protoc_insertion_point(message_implements:PushResourcesPb)
    PushResourcesPbOrBuilder {
  private static final long serialVersionUID = 0L;
  // Use PushResourcesPb.newBuilder() to construct.
  private PushResourcesPb(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }

  private PushResourcesPb() {
    resources_ = java.util.Collections.emptyList();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
    return this.unknownFields;
  }

  private PushResourcesPb(
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
                resources_ =
                    new java.util.ArrayList<
                        com.alipay.sofa.registry.common.model.client.pb.PushResourcePb>();
                mutable_bitField0_ |= 0x00000001;
              }
              resources_.add(
                  input.readMessage(
                      com.alipay.sofa.registry.common.model.client.pb.PushResourcePb.parser(),
                      extensionRegistry));
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
        resources_ = java.util.Collections.unmodifiableList(resources_);
      }
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }

  public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
    return com.alipay.sofa.registry.common.model.client.pb.PushResourcesPbOuterClass
        .internal_static_PushResourcesPb_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.alipay.sofa.registry.common.model.client.pb.PushResourcesPbOuterClass
        .internal_static_PushResourcesPb_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb.class,
            com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb.Builder.class);
  }

  public static final int RESOURCES_FIELD_NUMBER = 1;
  private java.util.List<com.alipay.sofa.registry.common.model.client.pb.PushResourcePb> resources_;
  /** <code>repeated .PushResourcePb resources = 1;</code> */
  public java.util.List<com.alipay.sofa.registry.common.model.client.pb.PushResourcePb>
      getResourcesList() {
    return resources_;
  }
  /** <code>repeated .PushResourcePb resources = 1;</code> */
  public java.util.List<
          ? extends com.alipay.sofa.registry.common.model.client.pb.PushResourcePbOrBuilder>
      getResourcesOrBuilderList() {
    return resources_;
  }
  /** <code>repeated .PushResourcePb resources = 1;</code> */
  public int getResourcesCount() {
    return resources_.size();
  }
  /** <code>repeated .PushResourcePb resources = 1;</code> */
  public com.alipay.sofa.registry.common.model.client.pb.PushResourcePb getResources(int index) {
    return resources_.get(index);
  }
  /** <code>repeated .PushResourcePb resources = 1;</code> */
  public com.alipay.sofa.registry.common.model.client.pb.PushResourcePbOrBuilder
      getResourcesOrBuilder(int index) {
    return resources_.get(index);
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
    for (int i = 0; i < resources_.size(); i++) {
      output.writeMessage(1, resources_.get(i));
    }
    unknownFields.writeTo(output);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    for (int i = 0; i < resources_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream.computeMessageSize(1, resources_.get(i));
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
    if (!(obj instanceof com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb)) {
      return super.equals(obj);
    }
    com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb other =
        (com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb) obj;

    boolean result = true;
    result = result && getResourcesList().equals(other.getResourcesList());
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
    if (getResourcesCount() > 0) {
      hash = (37 * hash) + RESOURCES_FIELD_NUMBER;
      hash = (53 * hash) + getResourcesList().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb parseFrom(
      java.nio.ByteBuffer data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb parseFrom(
      java.nio.ByteBuffer data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb parseFrom(
      byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb parseFrom(
      byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb parseFrom(
      java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb parseFrom(
      java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb parseDelimitedFrom(
      java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb parseDelimitedFrom(
      java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb parseFrom(
      com.google.protobuf.CodedInputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb parseFrom(
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
      com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb prototype) {
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
  /** Protobuf type {@code PushResourcesPb} */
  public static final class Builder extends com.google.protobuf.GeneratedMessageV3.Builder<Builder>
      implements
      // @@protoc_insertion_point(builder_implements:PushResourcesPb)
      com.alipay.sofa.registry.common.model.client.pb.PushResourcesPbOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
      return com.alipay.sofa.registry.common.model.client.pb.PushResourcesPbOuterClass
          .internal_static_PushResourcesPb_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.alipay.sofa.registry.common.model.client.pb.PushResourcesPbOuterClass
          .internal_static_PushResourcesPb_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb.class,
              com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb.Builder.class);
    }

    // Construct using com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }

    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders) {
        getResourcesFieldBuilder();
      }
    }

    public Builder clear() {
      super.clear();
      if (resourcesBuilder_ == null) {
        resources_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000001);
      } else {
        resourcesBuilder_.clear();
      }
      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
      return com.alipay.sofa.registry.common.model.client.pb.PushResourcesPbOuterClass
          .internal_static_PushResourcesPb_descriptor;
    }

    public com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb
        getDefaultInstanceForType() {
      return com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb.getDefaultInstance();
    }

    public com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb build() {
      com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb buildPartial() {
      com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb result =
          new com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb(this);
      int from_bitField0_ = bitField0_;
      if (resourcesBuilder_ == null) {
        if (((bitField0_ & 0x00000001) == 0x00000001)) {
          resources_ = java.util.Collections.unmodifiableList(resources_);
          bitField0_ = (bitField0_ & ~0x00000001);
        }
        result.resources_ = resources_;
      } else {
        result.resources_ = resourcesBuilder_.build();
      }
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
      if (other instanceof com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb) {
        return mergeFrom((com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb) other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(
        com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb other) {
      if (other
          == com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb.getDefaultInstance())
        return this;
      if (resourcesBuilder_ == null) {
        if (!other.resources_.isEmpty()) {
          if (resources_.isEmpty()) {
            resources_ = other.resources_;
            bitField0_ = (bitField0_ & ~0x00000001);
          } else {
            ensureResourcesIsMutable();
            resources_.addAll(other.resources_);
          }
          onChanged();
        }
      } else {
        if (!other.resources_.isEmpty()) {
          if (resourcesBuilder_.isEmpty()) {
            resourcesBuilder_.dispose();
            resourcesBuilder_ = null;
            resources_ = other.resources_;
            bitField0_ = (bitField0_ & ~0x00000001);
            resourcesBuilder_ =
                com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders
                    ? getResourcesFieldBuilder()
                    : null;
          } else {
            resourcesBuilder_.addAllMessages(other.resources_);
          }
        }
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
      com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage =
            (com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb)
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

    private java.util.List<com.alipay.sofa.registry.common.model.client.pb.PushResourcePb>
        resources_ = java.util.Collections.emptyList();

    private void ensureResourcesIsMutable() {
      if (!((bitField0_ & 0x00000001) == 0x00000001)) {
        resources_ =
            new java.util.ArrayList<com.alipay.sofa.registry.common.model.client.pb.PushResourcePb>(
                resources_);
        bitField0_ |= 0x00000001;
      }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
            com.alipay.sofa.registry.common.model.client.pb.PushResourcePb,
            com.alipay.sofa.registry.common.model.client.pb.PushResourcePb.Builder,
            com.alipay.sofa.registry.common.model.client.pb.PushResourcePbOrBuilder>
        resourcesBuilder_;

    /** <code>repeated .PushResourcePb resources = 1;</code> */
    public java.util.List<com.alipay.sofa.registry.common.model.client.pb.PushResourcePb>
        getResourcesList() {
      if (resourcesBuilder_ == null) {
        return java.util.Collections.unmodifiableList(resources_);
      } else {
        return resourcesBuilder_.getMessageList();
      }
    }
    /** <code>repeated .PushResourcePb resources = 1;</code> */
    public int getResourcesCount() {
      if (resourcesBuilder_ == null) {
        return resources_.size();
      } else {
        return resourcesBuilder_.getCount();
      }
    }
    /** <code>repeated .PushResourcePb resources = 1;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.PushResourcePb getResources(int index) {
      if (resourcesBuilder_ == null) {
        return resources_.get(index);
      } else {
        return resourcesBuilder_.getMessage(index);
      }
    }
    /** <code>repeated .PushResourcePb resources = 1;</code> */
    public Builder setResources(
        int index, com.alipay.sofa.registry.common.model.client.pb.PushResourcePb value) {
      if (resourcesBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureResourcesIsMutable();
        resources_.set(index, value);
        onChanged();
      } else {
        resourcesBuilder_.setMessage(index, value);
      }
      return this;
    }
    /** <code>repeated .PushResourcePb resources = 1;</code> */
    public Builder setResources(
        int index,
        com.alipay.sofa.registry.common.model.client.pb.PushResourcePb.Builder builderForValue) {
      if (resourcesBuilder_ == null) {
        ensureResourcesIsMutable();
        resources_.set(index, builderForValue.build());
        onChanged();
      } else {
        resourcesBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /** <code>repeated .PushResourcePb resources = 1;</code> */
    public Builder addResources(
        com.alipay.sofa.registry.common.model.client.pb.PushResourcePb value) {
      if (resourcesBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureResourcesIsMutable();
        resources_.add(value);
        onChanged();
      } else {
        resourcesBuilder_.addMessage(value);
      }
      return this;
    }
    /** <code>repeated .PushResourcePb resources = 1;</code> */
    public Builder addResources(
        int index, com.alipay.sofa.registry.common.model.client.pb.PushResourcePb value) {
      if (resourcesBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureResourcesIsMutable();
        resources_.add(index, value);
        onChanged();
      } else {
        resourcesBuilder_.addMessage(index, value);
      }
      return this;
    }
    /** <code>repeated .PushResourcePb resources = 1;</code> */
    public Builder addResources(
        com.alipay.sofa.registry.common.model.client.pb.PushResourcePb.Builder builderForValue) {
      if (resourcesBuilder_ == null) {
        ensureResourcesIsMutable();
        resources_.add(builderForValue.build());
        onChanged();
      } else {
        resourcesBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /** <code>repeated .PushResourcePb resources = 1;</code> */
    public Builder addResources(
        int index,
        com.alipay.sofa.registry.common.model.client.pb.PushResourcePb.Builder builderForValue) {
      if (resourcesBuilder_ == null) {
        ensureResourcesIsMutable();
        resources_.add(index, builderForValue.build());
        onChanged();
      } else {
        resourcesBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /** <code>repeated .PushResourcePb resources = 1;</code> */
    public Builder addAllResources(
        java.lang.Iterable<? extends com.alipay.sofa.registry.common.model.client.pb.PushResourcePb>
            values) {
      if (resourcesBuilder_ == null) {
        ensureResourcesIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(values, resources_);
        onChanged();
      } else {
        resourcesBuilder_.addAllMessages(values);
      }
      return this;
    }
    /** <code>repeated .PushResourcePb resources = 1;</code> */
    public Builder clearResources() {
      if (resourcesBuilder_ == null) {
        resources_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000001);
        onChanged();
      } else {
        resourcesBuilder_.clear();
      }
      return this;
    }
    /** <code>repeated .PushResourcePb resources = 1;</code> */
    public Builder removeResources(int index) {
      if (resourcesBuilder_ == null) {
        ensureResourcesIsMutable();
        resources_.remove(index);
        onChanged();
      } else {
        resourcesBuilder_.remove(index);
      }
      return this;
    }
    /** <code>repeated .PushResourcePb resources = 1;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.PushResourcePb.Builder
        getResourcesBuilder(int index) {
      return getResourcesFieldBuilder().getBuilder(index);
    }
    /** <code>repeated .PushResourcePb resources = 1;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.PushResourcePbOrBuilder
        getResourcesOrBuilder(int index) {
      if (resourcesBuilder_ == null) {
        return resources_.get(index);
      } else {
        return resourcesBuilder_.getMessageOrBuilder(index);
      }
    }
    /** <code>repeated .PushResourcePb resources = 1;</code> */
    public java.util.List<
            ? extends com.alipay.sofa.registry.common.model.client.pb.PushResourcePbOrBuilder>
        getResourcesOrBuilderList() {
      if (resourcesBuilder_ != null) {
        return resourcesBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(resources_);
      }
    }
    /** <code>repeated .PushResourcePb resources = 1;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.PushResourcePb.Builder
        addResourcesBuilder() {
      return getResourcesFieldBuilder()
          .addBuilder(
              com.alipay.sofa.registry.common.model.client.pb.PushResourcePb.getDefaultInstance());
    }
    /** <code>repeated .PushResourcePb resources = 1;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.PushResourcePb.Builder
        addResourcesBuilder(int index) {
      return getResourcesFieldBuilder()
          .addBuilder(
              index,
              com.alipay.sofa.registry.common.model.client.pb.PushResourcePb.getDefaultInstance());
    }
    /** <code>repeated .PushResourcePb resources = 1;</code> */
    public java.util.List<com.alipay.sofa.registry.common.model.client.pb.PushResourcePb.Builder>
        getResourcesBuilderList() {
      return getResourcesFieldBuilder().getBuilderList();
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
            com.alipay.sofa.registry.common.model.client.pb.PushResourcePb,
            com.alipay.sofa.registry.common.model.client.pb.PushResourcePb.Builder,
            com.alipay.sofa.registry.common.model.client.pb.PushResourcePbOrBuilder>
        getResourcesFieldBuilder() {
      if (resourcesBuilder_ == null) {
        resourcesBuilder_ =
            new com.google.protobuf.RepeatedFieldBuilderV3<
                com.alipay.sofa.registry.common.model.client.pb.PushResourcePb,
                com.alipay.sofa.registry.common.model.client.pb.PushResourcePb.Builder,
                com.alipay.sofa.registry.common.model.client.pb.PushResourcePbOrBuilder>(
                resources_,
                ((bitField0_ & 0x00000001) == 0x00000001),
                getParentForChildren(),
                isClean());
        resources_ = null;
      }
      return resourcesBuilder_;
    }

    public final Builder setUnknownFields(final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFieldsProto3(unknownFields);
    }

    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }

    // @@protoc_insertion_point(builder_scope:PushResourcesPb)
  }

  // @@protoc_insertion_point(class_scope:PushResourcesPb)
  private static final com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb
      DEFAULT_INSTANCE;

  static {
    DEFAULT_INSTANCE = new com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb();
  }

  public static com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb
      getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<PushResourcesPb> PARSER =
      new com.google.protobuf.AbstractParser<PushResourcesPb>() {
        public PushResourcesPb parsePartialFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
          return new PushResourcesPb(input, extensionRegistry);
        }
      };

  public static com.google.protobuf.Parser<PushResourcesPb> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<PushResourcesPb> getParserForType() {
    return PARSER;
  }

  public com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb
      getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }
}
