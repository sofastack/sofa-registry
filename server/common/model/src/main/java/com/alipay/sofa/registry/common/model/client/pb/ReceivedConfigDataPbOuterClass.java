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

public final class ReceivedConfigDataPbOuterClass {
  private ReceivedConfigDataPbOuterClass() {}

  public static void registerAllExtensions(com.google.protobuf.ExtensionRegistryLite registry) {}

  public static void registerAllExtensions(com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions((com.google.protobuf.ExtensionRegistryLite) registry);
  }

  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_ReceivedConfigDataPb_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ReceivedConfigDataPb_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor getDescriptor() {
    return descriptor;
  }

  private static com.google.protobuf.Descriptors.FileDescriptor descriptor;

  static {
    java.lang.String[] descriptorData = {
      "\n\032ReceivedConfigDataPb.proto\032\017DataBoxPb."
          + "proto\"\226\001\n\024ReceivedConfigDataPb\022\016\n\006dataId"
          + "\030\001 \001(\t\022\r\n\005group\030\002 \001(\t\022\022\n\ninstanceId\030\003 \001("
          + "\t\022\035\n\025configuratorRegistIds\030\004 \003(\t\022\033\n\007data"
          + "Box\030\005 \001(\0132\n.DataBoxPb\022\017\n\007version\030\006 \001(\003B7"
          + "\n/com.alipay.sofa.registry.common.model."
          + "client.pbP\001Z\002pbb\006proto3"
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
          com.alipay.sofa.registry.common.model.client.pb.DataBoxPbOuterClass.getDescriptor(),
        },
        assigner);
    internal_static_ReceivedConfigDataPb_descriptor = getDescriptor().getMessageTypes().get(0);
    internal_static_ReceivedConfigDataPb_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_ReceivedConfigDataPb_descriptor,
            new java.lang.String[] {
              "DataId", "Group", "InstanceId", "ConfiguratorRegistIds", "DataBox", "Version",
            });
    com.alipay.sofa.registry.common.model.client.pb.DataBoxPbOuterClass.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
