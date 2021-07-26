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

public final class ReceivedDataBodyPbOuterClass {
  private ReceivedDataBodyPbOuterClass() {}

  public static void registerAllExtensions(com.google.protobuf.ExtensionRegistryLite registry) {}

  public static void registerAllExtensions(com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions((com.google.protobuf.ExtensionRegistryLite) registry);
  }

  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_ReceivedDataBodyPb_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ReceivedDataBodyPb_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_ReceivedDataBodyPb_DataEntry_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ReceivedDataBodyPb_DataEntry_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor getDescriptor() {
    return descriptor;
  }

  private static com.google.protobuf.Descriptors.FileDescriptor descriptor;

  static {
    java.lang.String[] descriptorData = {
      "\n\030ReceivedDataBodyPb.proto\032\021DataBoxesPb."
          + "proto\"|\n\022ReceivedDataBodyPb\022+\n\004data\030\007 \003("
          + "\0132\035.ReceivedDataBodyPb.DataEntry\0329\n\tData"
          + "Entry\022\013\n\003key\030\001 \001(\t\022\033\n\005value\030\002 \001(\0132\014.Data"
          + "BoxesPb:\0028\001B:\n/com.alipay.sofa.registry."
          + "common.model.client.pbP\001Z\005protob\006proto3"
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
          com.alipay.sofa.registry.common.model.client.pb.DataBoxesPbOuterClass.getDescriptor(),
        },
        assigner);
    internal_static_ReceivedDataBodyPb_descriptor = getDescriptor().getMessageTypes().get(0);
    internal_static_ReceivedDataBodyPb_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_ReceivedDataBodyPb_descriptor,
            new java.lang.String[] {
              "Data",
            });
    internal_static_ReceivedDataBodyPb_DataEntry_descriptor =
        internal_static_ReceivedDataBodyPb_descriptor.getNestedTypes().get(0);
    internal_static_ReceivedDataBodyPb_DataEntry_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_ReceivedDataBodyPb_DataEntry_descriptor,
            new java.lang.String[] {
              "Key", "Value",
            });
    com.alipay.sofa.registry.common.model.client.pb.DataBoxesPbOuterClass.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
