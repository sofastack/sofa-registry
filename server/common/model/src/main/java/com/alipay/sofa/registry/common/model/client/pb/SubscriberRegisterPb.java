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

/** Protobuf type {@code SubscriberRegisterPb} */
public final class SubscriberRegisterPb extends com.google.protobuf.GeneratedMessageV3
    implements
    // @@protoc_insertion_point(message_implements:SubscriberRegisterPb)
    SubscriberRegisterPbOrBuilder {
  private static final long serialVersionUID = 0L;
  // Use SubscriberRegisterPb.newBuilder() to construct.
  private SubscriberRegisterPb(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }

  private SubscriberRegisterPb() {
    scope_ = "";
    acceptEncoding_ = "";
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
    return this.unknownFields;
  }

  private SubscriberRegisterPb(
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

              scope_ = s;
              break;
            }
          case 18:
            {
              com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb.Builder subBuilder =
                  null;
              if (baseRegister_ != null) {
                subBuilder = baseRegister_.toBuilder();
              }
              baseRegister_ =
                  input.readMessage(
                      com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb.parser(),
                      extensionRegistry);
              if (subBuilder != null) {
                subBuilder.mergeFrom(baseRegister_);
                baseRegister_ = subBuilder.buildPartial();
              }

              break;
            }
          case 26:
            {
              java.lang.String s = input.readStringRequireUtf8();

              acceptEncoding_ = s;
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
    return com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPbOuterClass
        .internal_static_SubscriberRegisterPb_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPbOuterClass
        .internal_static_SubscriberRegisterPb_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb.class,
            com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb.Builder.class);
  }

  public static final int SCOPE_FIELD_NUMBER = 1;
  private volatile java.lang.Object scope_;
  /** <code>string scope = 1;</code> */
  public java.lang.String getScope() {
    java.lang.Object ref = scope_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      scope_ = s;
      return s;
    }
  }
  /** <code>string scope = 1;</code> */
  public com.google.protobuf.ByteString getScopeBytes() {
    java.lang.Object ref = scope_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b =
          com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
      scope_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int BASEREGISTER_FIELD_NUMBER = 2;
  private com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb baseRegister_;
  /** <code>.BaseRegisterPb baseRegister = 2;</code> */
  public boolean hasBaseRegister() {
    return baseRegister_ != null;
  }
  /** <code>.BaseRegisterPb baseRegister = 2;</code> */
  public com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb getBaseRegister() {
    return baseRegister_ == null
        ? com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb.getDefaultInstance()
        : baseRegister_;
  }
  /** <code>.BaseRegisterPb baseRegister = 2;</code> */
  public com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPbOrBuilder
      getBaseRegisterOrBuilder() {
    return getBaseRegister();
  }

  public static final int ACCEPTENCODING_FIELD_NUMBER = 3;
  private volatile java.lang.Object acceptEncoding_;
  /** <code>string acceptEncoding = 3;</code> */
  public java.lang.String getAcceptEncoding() {
    java.lang.Object ref = acceptEncoding_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      acceptEncoding_ = s;
      return s;
    }
  }
  /** <code>string acceptEncoding = 3;</code> */
  public com.google.protobuf.ByteString getAcceptEncodingBytes() {
    java.lang.Object ref = acceptEncoding_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b =
          com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
      acceptEncoding_ = b;
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
    if (!getScopeBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, scope_);
    }
    if (baseRegister_ != null) {
      output.writeMessage(2, getBaseRegister());
    }
    if (!getAcceptEncodingBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 3, acceptEncoding_);
    }
    unknownFields.writeTo(output);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!getScopeBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, scope_);
    }
    if (baseRegister_ != null) {
      size += com.google.protobuf.CodedOutputStream.computeMessageSize(2, getBaseRegister());
    }
    if (!getAcceptEncodingBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(3, acceptEncoding_);
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
    if (!(obj instanceof com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb)) {
      return super.equals(obj);
    }
    com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb other =
        (com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb) obj;

    boolean result = true;
    result = result && getScope().equals(other.getScope());
    result = result && (hasBaseRegister() == other.hasBaseRegister());
    if (hasBaseRegister()) {
      result = result && getBaseRegister().equals(other.getBaseRegister());
    }
    result = result && getAcceptEncoding().equals(other.getAcceptEncoding());
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
    hash = (37 * hash) + SCOPE_FIELD_NUMBER;
    hash = (53 * hash) + getScope().hashCode();
    if (hasBaseRegister()) {
      hash = (37 * hash) + BASEREGISTER_FIELD_NUMBER;
      hash = (53 * hash) + getBaseRegister().hashCode();
    }
    hash = (37 * hash) + ACCEPTENCODING_FIELD_NUMBER;
    hash = (53 * hash) + getAcceptEncoding().hashCode();
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb parseFrom(
      java.nio.ByteBuffer data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb parseFrom(
      java.nio.ByteBuffer data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb parseFrom(
      byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb parseFrom(
      byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb parseFrom(
      java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb parseFrom(
      java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb
      parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb
      parseDelimitedFrom(
          java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(
        PARSER, input, extensionRegistry);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb parseFrom(
      com.google.protobuf.CodedInputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }

  public static com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb parseFrom(
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
      com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb prototype) {
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
  /** Protobuf type {@code SubscriberRegisterPb} */
  public static final class Builder extends com.google.protobuf.GeneratedMessageV3.Builder<Builder>
      implements
      // @@protoc_insertion_point(builder_implements:SubscriberRegisterPb)
      com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPbOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
      return com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPbOuterClass
          .internal_static_SubscriberRegisterPb_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPbOuterClass
          .internal_static_SubscriberRegisterPb_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb.class,
              com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb.Builder.class);
    }

    // Construct using
    // com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb.newBuilder()
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
      scope_ = "";

      if (baseRegisterBuilder_ == null) {
        baseRegister_ = null;
      } else {
        baseRegister_ = null;
        baseRegisterBuilder_ = null;
      }
      acceptEncoding_ = "";

      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
      return com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPbOuterClass
          .internal_static_SubscriberRegisterPb_descriptor;
    }

    public com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb
        getDefaultInstanceForType() {
      return com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb
          .getDefaultInstance();
    }

    public com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb build() {
      com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb buildPartial() {
      com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb result =
          new com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb(this);
      result.scope_ = scope_;
      if (baseRegisterBuilder_ == null) {
        result.baseRegister_ = baseRegister_;
      } else {
        result.baseRegister_ = baseRegisterBuilder_.build();
      }
      result.acceptEncoding_ = acceptEncoding_;
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
      if (other instanceof com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb) {
        return mergeFrom(
            (com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb) other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(
        com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb other) {
      if (other
          == com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb
              .getDefaultInstance()) return this;
      if (!other.getScope().isEmpty()) {
        scope_ = other.scope_;
        onChanged();
      }
      if (other.hasBaseRegister()) {
        mergeBaseRegister(other.getBaseRegister());
      }
      if (!other.getAcceptEncoding().isEmpty()) {
        acceptEncoding_ = other.acceptEncoding_;
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
      com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage =
            (com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb)
                e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private java.lang.Object scope_ = "";
    /** <code>string scope = 1;</code> */
    public java.lang.String getScope() {
      java.lang.Object ref = scope_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        scope_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /** <code>string scope = 1;</code> */
    public com.google.protobuf.ByteString getScopeBytes() {
      java.lang.Object ref = scope_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
            com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
        scope_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /** <code>string scope = 1;</code> */
    public Builder setScope(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      scope_ = value;
      onChanged();
      return this;
    }
    /** <code>string scope = 1;</code> */
    public Builder clearScope() {

      scope_ = getDefaultInstance().getScope();
      onChanged();
      return this;
    }
    /** <code>string scope = 1;</code> */
    public Builder setScopeBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);

      scope_ = value;
      onChanged();
      return this;
    }

    private com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb baseRegister_ = null;
    private com.google.protobuf.SingleFieldBuilderV3<
            com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb,
            com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb.Builder,
            com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPbOrBuilder>
        baseRegisterBuilder_;
    /** <code>.BaseRegisterPb baseRegister = 2;</code> */
    public boolean hasBaseRegister() {
      return baseRegisterBuilder_ != null || baseRegister_ != null;
    }
    /** <code>.BaseRegisterPb baseRegister = 2;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb getBaseRegister() {
      if (baseRegisterBuilder_ == null) {
        return baseRegister_ == null
            ? com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb.getDefaultInstance()
            : baseRegister_;
      } else {
        return baseRegisterBuilder_.getMessage();
      }
    }
    /** <code>.BaseRegisterPb baseRegister = 2;</code> */
    public Builder setBaseRegister(
        com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb value) {
      if (baseRegisterBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        baseRegister_ = value;
        onChanged();
      } else {
        baseRegisterBuilder_.setMessage(value);
      }

      return this;
    }
    /** <code>.BaseRegisterPb baseRegister = 2;</code> */
    public Builder setBaseRegister(
        com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb.Builder builderForValue) {
      if (baseRegisterBuilder_ == null) {
        baseRegister_ = builderForValue.build();
        onChanged();
      } else {
        baseRegisterBuilder_.setMessage(builderForValue.build());
      }

      return this;
    }
    /** <code>.BaseRegisterPb baseRegister = 2;</code> */
    public Builder mergeBaseRegister(
        com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb value) {
      if (baseRegisterBuilder_ == null) {
        if (baseRegister_ != null) {
          baseRegister_ =
              com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb.newBuilder(
                      baseRegister_)
                  .mergeFrom(value)
                  .buildPartial();
        } else {
          baseRegister_ = value;
        }
        onChanged();
      } else {
        baseRegisterBuilder_.mergeFrom(value);
      }

      return this;
    }
    /** <code>.BaseRegisterPb baseRegister = 2;</code> */
    public Builder clearBaseRegister() {
      if (baseRegisterBuilder_ == null) {
        baseRegister_ = null;
        onChanged();
      } else {
        baseRegister_ = null;
        baseRegisterBuilder_ = null;
      }

      return this;
    }
    /** <code>.BaseRegisterPb baseRegister = 2;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb.Builder
        getBaseRegisterBuilder() {

      onChanged();
      return getBaseRegisterFieldBuilder().getBuilder();
    }
    /** <code>.BaseRegisterPb baseRegister = 2;</code> */
    public com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPbOrBuilder
        getBaseRegisterOrBuilder() {
      if (baseRegisterBuilder_ != null) {
        return baseRegisterBuilder_.getMessageOrBuilder();
      } else {
        return baseRegister_ == null
            ? com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb.getDefaultInstance()
            : baseRegister_;
      }
    }
    /** <code>.BaseRegisterPb baseRegister = 2;</code> */
    private com.google.protobuf.SingleFieldBuilderV3<
            com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb,
            com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb.Builder,
            com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPbOrBuilder>
        getBaseRegisterFieldBuilder() {
      if (baseRegisterBuilder_ == null) {
        baseRegisterBuilder_ =
            new com.google.protobuf.SingleFieldBuilderV3<
                com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb,
                com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb.Builder,
                com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPbOrBuilder>(
                getBaseRegister(), getParentForChildren(), isClean());
        baseRegister_ = null;
      }
      return baseRegisterBuilder_;
    }

    private java.lang.Object acceptEncoding_ = "";
    /** <code>string acceptEncoding = 3;</code> */
    public java.lang.String getAcceptEncoding() {
      java.lang.Object ref = acceptEncoding_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        acceptEncoding_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /** <code>string acceptEncoding = 3;</code> */
    public com.google.protobuf.ByteString getAcceptEncodingBytes() {
      java.lang.Object ref = acceptEncoding_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
            com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
        acceptEncoding_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /** <code>string acceptEncoding = 3;</code> */
    public Builder setAcceptEncoding(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      acceptEncoding_ = value;
      onChanged();
      return this;
    }
    /** <code>string acceptEncoding = 3;</code> */
    public Builder clearAcceptEncoding() {

      acceptEncoding_ = getDefaultInstance().getAcceptEncoding();
      onChanged();
      return this;
    }
    /** <code>string acceptEncoding = 3;</code> */
    public Builder setAcceptEncodingBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);

      acceptEncoding_ = value;
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

    // @@protoc_insertion_point(builder_scope:SubscriberRegisterPb)
  }

  // @@protoc_insertion_point(class_scope:SubscriberRegisterPb)
  private static final com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb
      DEFAULT_INSTANCE;

  static {
    DEFAULT_INSTANCE = new com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb();
  }

  public static com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb
      getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<SubscriberRegisterPb> PARSER =
      new com.google.protobuf.AbstractParser<SubscriberRegisterPb>() {
        public SubscriberRegisterPb parsePartialFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
          return new SubscriberRegisterPb(input, extensionRegistry);
        }
      };

  public static com.google.protobuf.Parser<SubscriberRegisterPb> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<SubscriberRegisterPb> getParserForType() {
    return PARSER;
  }

  public com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb
      getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }
}
