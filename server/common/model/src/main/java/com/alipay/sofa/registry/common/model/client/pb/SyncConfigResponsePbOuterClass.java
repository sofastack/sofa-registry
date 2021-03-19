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

public final class SyncConfigResponsePbOuterClass {
  private SyncConfigResponsePbOuterClass() {}

  public static void registerAllExtensions(com.google.protobuf.ExtensionRegistryLite registry) {}

  public static void registerAllExtensions(com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions((com.google.protobuf.ExtensionRegistryLite) registry);
  }

  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_SyncConfigResponsePb_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_SyncConfigResponsePb_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor getDescriptor() {
    return descriptor;
  }

  private static com.google.protobuf.Descriptors.FileDescriptor descriptor;

  static {
    java.lang.String[] descriptorData = {
      "\n\032SyncConfigResponsePb.proto\032\016ResultPb.p"
          + "roto\"c\n\024SyncConfigResponsePb\022\031\n\006result\030\001"
          + " \001(\0132\t.ResultPb\022\031\n\021availableSegments\030\002 \003"
          + "(\t\022\025\n\rretryInterval\030\003 \001(\005B7\n/com.alipay."
          + "sofa.registry.common.model.client.pbP\001Z\002"
          + "pbb\006proto3"
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
          com.alipay.sofa.registry.common.model.client.pb.ResultPbOuterClass.getDescriptor(),
        },
        assigner);
    internal_static_SyncConfigResponsePb_descriptor = getDescriptor().getMessageTypes().get(0);
    internal_static_SyncConfigResponsePb_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_SyncConfigResponsePb_descriptor,
            new java.lang.String[] {
              "Result", "AvailableSegments", "RetryInterval",
            });
    com.alipay.sofa.registry.common.model.client.pb.ResultPbOuterClass.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
