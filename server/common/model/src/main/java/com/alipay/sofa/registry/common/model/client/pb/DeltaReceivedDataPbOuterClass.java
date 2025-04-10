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

public final class DeltaReceivedDataPbOuterClass {
  private DeltaReceivedDataPbOuterClass() {}

  public static void registerAllExtensions(com.google.protobuf.ExtensionRegistryLite registry) {}

  public static void registerAllExtensions(com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions((com.google.protobuf.ExtensionRegistryLite) registry);
  }

  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_DeltaReceivedDataPb_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_DeltaReceivedDataPb_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor getDescriptor() {
    return descriptor;
  }

  private static com.google.protobuf.Descriptors.FileDescriptor descriptor;

  static {
    java.lang.String[] descriptorData = {
      "\n\031DeltaReceivedDataPb.proto\032\035DeltaReceiv"
          + "edDataBodyPb.proto\032\034FullReceivedDataBody"
          + "Pb.proto\"\224\002\n\023DeltaReceivedDataPb\022\016\n\006data"
          + "Id\030\001 \001(\t\022\r\n\005group\030\002 \001(\t\022\022\n\ninstanceId\030\003 "
          + "\001(\t\022\017\n\007segment\030\004 \001(\t\022\r\n\005scope\030\005 \001(\t\022\035\n\025s"
          + "ubscriberRegisterIds\030\006 \003(\t\022\017\n\007version\030\007 "
          + "\001(\003\022\021\n\tlocalZone\030\010 \001(\t\022\017\n\007isDelta\030\t \001(\010\022"
          + "+\n\tdeltaBody\030\n \001(\0132\030.DeltaReceivedDataBo"
          + "dyPb\022)\n\010fullBody\030\013 \001(\0132\027.FullReceivedDat"
          + "aBodyPbB:\n/com.alipay.sofa.registry.comm"
          + "on.model.client.pbP\001Z\005protob\006proto3"
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
          com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPbOuterClass
              .getDescriptor(),
          com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPbOuterClass
              .getDescriptor(),
        },
        assigner);
    internal_static_DeltaReceivedDataPb_descriptor = getDescriptor().getMessageTypes().get(0);
    internal_static_DeltaReceivedDataPb_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_DeltaReceivedDataPb_descriptor,
            new java.lang.String[] {
              "DataId",
              "Group",
              "InstanceId",
              "Segment",
              "Scope",
              "SubscriberRegisterIds",
              "Version",
              "LocalZone",
              "IsDelta",
              "DeltaBody",
              "FullBody",
            });
    com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPbOuterClass
        .getDescriptor();
    com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPbOuterClass
        .getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
