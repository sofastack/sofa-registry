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

/** Protobuf enum {@code EventTypePb} */
public enum EventTypePb implements com.google.protobuf.ProtocolMessageEnum {
  /** <code>REGISTER = 0;</code> */
  REGISTER(0),
  /** <code>UNREGISTER = 1;</code> */
  UNREGISTER(1),
  UNRECOGNIZED(-1),
  ;

  /** <code>REGISTER = 0;</code> */
  public static final int REGISTER_VALUE = 0;
  /** <code>UNREGISTER = 1;</code> */
  public static final int UNREGISTER_VALUE = 1;

  public final int getNumber() {
    if (this == UNRECOGNIZED) {
      throw new java.lang.IllegalArgumentException(
          "Can't get the number of an unknown enum value.");
    }
    return value;
  }

  /** @deprecated Use {@link #forNumber(int)} instead. */
  @java.lang.Deprecated
  public static EventTypePb valueOf(int value) {
    return forNumber(value);
  }

  public static EventTypePb forNumber(int value) {
    switch (value) {
      case 0:
        return REGISTER;
      case 1:
        return UNREGISTER;
      default:
        return null;
    }
  }

  public static com.google.protobuf.Internal.EnumLiteMap<EventTypePb> internalGetValueMap() {
    return internalValueMap;
  }

  private static final com.google.protobuf.Internal.EnumLiteMap<EventTypePb> internalValueMap =
      new com.google.protobuf.Internal.EnumLiteMap<EventTypePb>() {
        public EventTypePb findValueByNumber(int number) {
          return EventTypePb.forNumber(number);
        }
      };

  public final com.google.protobuf.Descriptors.EnumValueDescriptor getValueDescriptor() {
    return getDescriptor().getValues().get(ordinal());
  }

  public final com.google.protobuf.Descriptors.EnumDescriptor getDescriptorForType() {
    return getDescriptor();
  }

  public static final com.google.protobuf.Descriptors.EnumDescriptor getDescriptor() {
    return com.alipay.sofa.registry.common.model.client.pb.EventTypePbOuterClass.getDescriptor()
        .getEnumTypes()
        .get(0);
  }

  private static final EventTypePb[] VALUES = values();

  public static EventTypePb valueOf(com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
    if (desc.getType() != getDescriptor()) {
      throw new java.lang.IllegalArgumentException("EnumValueDescriptor is not for this type.");
    }
    if (desc.getIndex() == -1) {
      return UNRECOGNIZED;
    }
    return VALUES[desc.getIndex()];
  }

  private final int value;

  private EventTypePb(int value) {
    this.value = value;
  }

  // @@protoc_insertion_point(enum_scope:EventTypePb)
}
