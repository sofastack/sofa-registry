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

public final class FullReceivedDataBodyPbOuterClass {
  private FullReceivedDataBodyPbOuterClass() {}

  public static void registerAllExtensions(com.google.protobuf.ExtensionRegistryLite registry) {}

  public static void registerAllExtensions(com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions((com.google.protobuf.ExtensionRegistryLite) registry);
  }

  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_FullReceivedDataBodyPb_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_FullReceivedDataBodyPb_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_FullReceivedDataBodyPb_DataEntry_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_FullReceivedDataBodyPb_DataEntry_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor getDescriptor() {
    return descriptor;
  }

  private static com.google.protobuf.Descriptors.FileDescriptor descriptor;

  static {
    java.lang.String[] descriptorData = {
      "\n\034FullReceivedDataBodyPb.proto\032\025PushReso"
          + "urcesPb.proto\"\300\001\n\026FullReceivedDataBodyPb"
          + "\022\020\n\010encoding\030\001 \001(\t\022\014\n\004body\030\002 \001(\014\022\026\n\016orig"
          + "inBodySize\030\003 \001(\005\022/\n\004data\030\004 \003(\0132!.FullRec"
          + "eivedDataBodyPb.DataEntry\032=\n\tDataEntry\022\013"
          + "\n\003key\030\001 \001(\t\022\037\n\005value\030\002 \001(\0132\020.PushResourc"
          + "esPb:\0028\001B:\n/com.alipay.sofa.registry.com"
          + "mon.model.client.pbP\001Z\005protob\006proto3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor.internalBuildGeneratedFileFrom(
        descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.alipay.sofa.registry.common.model.client.pb.PushResourcesPbOuterClass.getDescriptor(),
        },
        assigner);
    internal_static_FullReceivedDataBodyPb_descriptor = getDescriptor().getMessageTypes().get(0);
    internal_static_FullReceivedDataBodyPb_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_FullReceivedDataBodyPb_descriptor,
            new java.lang.String[] {
              "Encoding", "Body", "OriginBodySize", "Data",
            });
    internal_static_FullReceivedDataBodyPb_DataEntry_descriptor =
        internal_static_FullReceivedDataBodyPb_descriptor.getNestedTypes().get(0);
    internal_static_FullReceivedDataBodyPb_DataEntry_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_FullReceivedDataBodyPb_DataEntry_descriptor,
            new java.lang.String[] {
              "Key", "Value",
            });
    com.alipay.sofa.registry.common.model.client.pb.PushResourcesPbOuterClass.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
