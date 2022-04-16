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

public final class MultiReceivedDataPbOuterClass {
  private MultiReceivedDataPbOuterClass() {}

  public static void registerAllExtensions(com.google.protobuf.ExtensionRegistryLite registry) {}

  public static void registerAllExtensions(com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions((com.google.protobuf.ExtensionRegistryLite) registry);
  }

  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_MultiReceivedDataPb_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_MultiReceivedDataPb_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_MultiReceivedDataPb_MultiDataEntry_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_MultiReceivedDataPb_MultiDataEntry_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor getDescriptor() {
    return descriptor;
  }

  private static com.google.protobuf.Descriptors.FileDescriptor descriptor;

  static {
    java.lang.String[] descriptorData = {
      "\n\031MultiReceivedDataPb.proto\032\030MultiSegmen"
          + "tDataPb.proto\"\234\002\n\023MultiReceivedDataPb\022\016\n"
          + "\006dataId\030\001 \001(\t\022\r\n\005group\030\002 \001(\t\022\022\n\ninstance"
          + "Id\030\003 \001(\t\022\r\n\005scope\030\004 \001(\t\022\033\n\023subscriberReg"
          + "istIds\030\005 \003(\t\022\024\n\014localSegment\030\006 \001(\t\022\021\n\tlo"
          + "calZone\030\007 \001(\t\0226\n\tmultiData\030\010 \003(\0132#.Multi"
          + "ReceivedDataPb.MultiDataEntry\032E\n\016MultiDa"
          + "taEntry\022\013\n\003key\030\001 \001(\t\022\"\n\005value\030\002 \001(\0132\023.Mu"
          + "ltiSegmentDataPb:\0028\001B:\n/com.alipay.sofa."
          + "registry.common.model.client.pbP\001Z\005proto"
          + "b\006proto3"
    };
    descriptor =
        com.google.protobuf.Descriptors.FileDescriptor.internalBuildGeneratedFileFrom(
            descriptorData,
            new com.google.protobuf.Descriptors.FileDescriptor[] {
              com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPbOuterClass
                  .getDescriptor(),
            });
    internal_static_MultiReceivedDataPb_descriptor = getDescriptor().getMessageTypes().get(0);
    internal_static_MultiReceivedDataPb_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_MultiReceivedDataPb_descriptor,
            new java.lang.String[] {
              "DataId",
              "Group",
              "InstanceId",
              "Scope",
              "SubscriberRegistIds",
              "LocalSegment",
              "LocalZone",
              "MultiData",
            });
    internal_static_MultiReceivedDataPb_MultiDataEntry_descriptor =
        internal_static_MultiReceivedDataPb_descriptor.getNestedTypes().get(0);
    internal_static_MultiReceivedDataPb_MultiDataEntry_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_MultiReceivedDataPb_MultiDataEntry_descriptor,
            new java.lang.String[] {
              "Key", "Value",
            });
    com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPbOuterClass.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
