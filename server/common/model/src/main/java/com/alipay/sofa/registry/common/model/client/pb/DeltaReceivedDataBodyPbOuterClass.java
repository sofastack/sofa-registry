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

public final class DeltaReceivedDataBodyPbOuterClass {
  private DeltaReceivedDataBodyPbOuterClass() {}

  public static void registerAllExtensions(com.google.protobuf.ExtensionRegistryLite registry) {}

  public static void registerAllExtensions(com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions((com.google.protobuf.ExtensionRegistryLite) registry);
  }

  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_DeltaReceivedDataBodyPb_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_DeltaReceivedDataBodyPb_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_DeltaReceivedDataBodyPb_AddPublishersEntry_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_DeltaReceivedDataBodyPb_AddPublishersEntry_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_DeltaReceivedDataBodyPb_DeletePublisherRegisterIdsEntry_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_DeltaReceivedDataBodyPb_DeletePublisherRegisterIdsEntry_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_DeltaReceivedDataBodyPb_ZoneDigestsEntry_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_DeltaReceivedDataBodyPb_ZoneDigestsEntry_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor getDescriptor() {
    return descriptor;
  }

  private static com.google.protobuf.Descriptors.FileDescriptor descriptor;

  static {
    java.lang.String[] descriptorData = {
      "\n\035DeltaReceivedDataBodyPb.proto\032\025PushRes"
          + "ourcesPb.proto\032\023RegisterIdsPb.proto\032\023Del"
          + "taDigestPb.proto\"\332\003\n\027DeltaReceivedDataBo"
          + "dyPb\022B\n\raddPublishers\030\001 \003(\0132+.DeltaRecei"
          + "vedDataBodyPb.AddPublishersEntry\022\\\n\032dele"
          + "tePublisherRegisterIds\030\002 \003(\01328.DeltaRece"
          + "ivedDataBodyPb.DeletePublisherRegisterId"
          + "sEntry\022>\n\013zoneDigests\030\003 \003(\0132).DeltaRecei"
          + "vedDataBodyPb.ZoneDigestsEntry\032F\n\022AddPub"
          + "lishersEntry\022\013\n\003key\030\001 \001(\t\022\037\n\005value\030\002 \001(\013"
          + "2\020.PushResourcesPb:\0028\001\032Q\n\037DeletePublishe"
          + "rRegisterIdsEntry\022\013\n\003key\030\001 \001(\t\022\035\n\005value\030"
          + "\002 \001(\0132\016.RegisterIdsPb:\0028\001\032B\n\020ZoneDigests"
          + "Entry\022\013\n\003key\030\001 \001(\t\022\035\n\005value\030\002 \001(\0132\016.Delt"
          + "aDigestPb:\0028\001B:\n/com.alipay.sofa.registr"
          + "y.common.model.client.pbP\001Z\005protob\006proto"
          + "3"
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
          com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPbOuterClass.getDescriptor(),
          com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPbOuterClass.getDescriptor(),
        },
        assigner);
    internal_static_DeltaReceivedDataBodyPb_descriptor = getDescriptor().getMessageTypes().get(0);
    internal_static_DeltaReceivedDataBodyPb_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_DeltaReceivedDataBodyPb_descriptor,
            new java.lang.String[] {
              "AddPublishers", "DeletePublisherRegisterIds", "ZoneDigests",
            });
    internal_static_DeltaReceivedDataBodyPb_AddPublishersEntry_descriptor =
        internal_static_DeltaReceivedDataBodyPb_descriptor.getNestedTypes().get(0);
    internal_static_DeltaReceivedDataBodyPb_AddPublishersEntry_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_DeltaReceivedDataBodyPb_AddPublishersEntry_descriptor,
            new java.lang.String[] {
              "Key", "Value",
            });
    internal_static_DeltaReceivedDataBodyPb_DeletePublisherRegisterIdsEntry_descriptor =
        internal_static_DeltaReceivedDataBodyPb_descriptor.getNestedTypes().get(1);
    internal_static_DeltaReceivedDataBodyPb_DeletePublisherRegisterIdsEntry_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_DeltaReceivedDataBodyPb_DeletePublisherRegisterIdsEntry_descriptor,
            new java.lang.String[] {
              "Key", "Value",
            });
    internal_static_DeltaReceivedDataBodyPb_ZoneDigestsEntry_descriptor =
        internal_static_DeltaReceivedDataBodyPb_descriptor.getNestedTypes().get(2);
    internal_static_DeltaReceivedDataBodyPb_ZoneDigestsEntry_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_DeltaReceivedDataBodyPb_ZoneDigestsEntry_descriptor,
            new java.lang.String[] {
              "Key", "Value",
            });
    com.alipay.sofa.registry.common.model.client.pb.PushResourcesPbOuterClass.getDescriptor();
    com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPbOuterClass.getDescriptor();
    com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPbOuterClass.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
