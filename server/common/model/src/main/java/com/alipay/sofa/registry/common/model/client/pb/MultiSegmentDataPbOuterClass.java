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

public final class MultiSegmentDataPbOuterClass {
  private MultiSegmentDataPbOuterClass() {}

  public static void registerAllExtensions(com.google.protobuf.ExtensionRegistryLite registry) {}

  public static void registerAllExtensions(com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions((com.google.protobuf.ExtensionRegistryLite) registry);
  }

  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_MultiSegmentDataPb_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_MultiSegmentDataPb_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_MultiSegmentDataPb_UnzipDataEntry_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_MultiSegmentDataPb_UnzipDataEntry_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_MultiSegmentDataPb_PushDataCountEntry_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_MultiSegmentDataPb_PushDataCountEntry_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor getDescriptor() {
    return descriptor;
  }

  private static com.google.protobuf.Descriptors.FileDescriptor descriptor;

  static {
    java.lang.String[] descriptorData = {
      "\n\030MultiSegmentDataPb.proto\032\021DataBoxesPb."
          + "proto\"\305\002\n\022MultiSegmentDataPb\022\017\n\007segment\030"
          + "\001 \001(\t\022\017\n\007zipData\030\002 \001(\014\0225\n\tunzipData\030\003 \003("
          + "\0132\".MultiSegmentDataPb.UnzipDataEntry\022\020\n"
          + "\010encoding\030\004 \001(\t\022\017\n\007version\030\005 \001(\003\022=\n\rpush"
          + "DataCount\030\006 \003(\0132&.MultiSegmentDataPb.Pus"
          + "hDataCountEntry\032>\n\016UnzipDataEntry\022\013\n\003key"
          + "\030\001 \001(\t\022\033\n\005value\030\002 \001(\0132\014.DataBoxesPb:\0028\001\032"
          + "4\n\022PushDataCountEntry\022\013\n\003key\030\001 \001(\t\022\r\n\005va"
          + "lue\030\002 \001(\005:\0028\001B:\n/com.alipay.sofa.registr"
          + "y.common.model.client.pbP\001Z\005protob\006proto"
          + "3"
    };
    descriptor =
        com.google.protobuf.Descriptors.FileDescriptor.internalBuildGeneratedFileFrom(
            descriptorData,
            new com.google.protobuf.Descriptors.FileDescriptor[] {
              com.alipay.sofa.registry.common.model.client.pb.DataBoxesPbOuterClass.getDescriptor(),
            });
    internal_static_MultiSegmentDataPb_descriptor = getDescriptor().getMessageTypes().get(0);
    internal_static_MultiSegmentDataPb_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_MultiSegmentDataPb_descriptor,
            new java.lang.String[] {
              "Segment", "ZipData", "UnzipData", "Encoding", "Version", "PushDataCount",
            });
    internal_static_MultiSegmentDataPb_UnzipDataEntry_descriptor =
        internal_static_MultiSegmentDataPb_descriptor.getNestedTypes().get(0);
    internal_static_MultiSegmentDataPb_UnzipDataEntry_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_MultiSegmentDataPb_UnzipDataEntry_descriptor,
            new java.lang.String[] {
              "Key", "Value",
            });
    internal_static_MultiSegmentDataPb_PushDataCountEntry_descriptor =
        internal_static_MultiSegmentDataPb_descriptor.getNestedTypes().get(1);
    internal_static_MultiSegmentDataPb_PushDataCountEntry_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_MultiSegmentDataPb_PushDataCountEntry_descriptor,
            new java.lang.String[] {
              "Key", "Value",
            });
    com.alipay.sofa.registry.common.model.client.pb.DataBoxesPbOuterClass.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
