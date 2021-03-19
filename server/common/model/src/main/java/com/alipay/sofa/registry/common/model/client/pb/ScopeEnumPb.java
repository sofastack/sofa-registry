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

/** Protobuf enum {@code ScopeEnumPb} */
public enum ScopeEnumPb implements com.google.protobuf.ProtocolMessageEnum {
  /** <code>zone = 0;</code> */
  zone(0),
  /** <code>dataCenter = 1;</code> */
  dataCenter(1),
  /** <code>global = 2;</code> */
  global(2),
  UNRECOGNIZED(-1),
  ;

  /** <code>zone = 0;</code> */
  public static final int zone_VALUE = 0;
  /** <code>dataCenter = 1;</code> */
  public static final int dataCenter_VALUE = 1;
  /** <code>global = 2;</code> */
  public static final int global_VALUE = 2;

  public final int getNumber() {
    if (this == UNRECOGNIZED) {
      throw new java.lang.IllegalArgumentException(
          "Can't get the number of an unknown enum value.");
    }
    return value;
  }

  /** @deprecated Use {@link #forNumber(int)} instead. */
  @java.lang.Deprecated
  public static ScopeEnumPb valueOf(int value) {
    return forNumber(value);
  }

  public static ScopeEnumPb forNumber(int value) {
    switch (value) {
      case 0:
        return zone;
      case 1:
        return dataCenter;
      case 2:
        return global;
      default:
        return null;
    }
  }

  public static com.google.protobuf.Internal.EnumLiteMap<ScopeEnumPb> internalGetValueMap() {
    return internalValueMap;
  }

  private static final com.google.protobuf.Internal.EnumLiteMap<ScopeEnumPb> internalValueMap =
      new com.google.protobuf.Internal.EnumLiteMap<ScopeEnumPb>() {
        public ScopeEnumPb findValueByNumber(int number) {
          return ScopeEnumPb.forNumber(number);
        }
      };

  public final com.google.protobuf.Descriptors.EnumValueDescriptor getValueDescriptor() {
    return getDescriptor().getValues().get(ordinal());
  }

  public final com.google.protobuf.Descriptors.EnumDescriptor getDescriptorForType() {
    return getDescriptor();
  }

  public static final com.google.protobuf.Descriptors.EnumDescriptor getDescriptor() {
    return com.alipay.sofa.registry.common.model.client.pb.ScopeEnumPbOuterClass.getDescriptor()
        .getEnumTypes()
        .get(0);
  }

  private static final ScopeEnumPb[] VALUES = values();

  public static ScopeEnumPb valueOf(com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
    if (desc.getType() != getDescriptor()) {
      throw new java.lang.IllegalArgumentException("EnumValueDescriptor is not for this type.");
    }
    if (desc.getIndex() == -1) {
      return UNRECOGNIZED;
    }
    return VALUES[desc.getIndex()];
  }

  private final int value;

  private ScopeEnumPb(int value) {
    this.value = value;
  }

  // @@protoc_insertion_point(enum_scope:ScopeEnumPb)
}
