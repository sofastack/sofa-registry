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

public final class BaseRegisterPbOuterClass {
  private BaseRegisterPbOuterClass() {}

  public static void registerAllExtensions(com.google.protobuf.ExtensionRegistryLite registry) {}

  public static void registerAllExtensions(com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions((com.google.protobuf.ExtensionRegistryLite) registry);
  }

  static final com.google.protobuf.Descriptors.Descriptor internal_static_BaseRegisterPb_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_BaseRegisterPb_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_BaseRegisterPb_AttributesEntry_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_BaseRegisterPb_AttributesEntry_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor getDescriptor() {
    return descriptor;
  }

  private static com.google.protobuf.Descriptors.FileDescriptor descriptor;

  static {
    java.lang.String[] descriptorData = {
      "\n\024BaseRegisterPb.proto\"\346\002\n\016BaseRegisterP"
          + "b\022\022\n\ninstanceId\030\001 \001(\t\022\014\n\004zone\030\002 \001(\t\022\017\n\007a"
          + "ppName\030\003 \001(\t\022\016\n\006dataId\030\004 \001(\t\022\r\n\005group\030\005 "
          + "\001(\t\022\021\n\tprocessId\030\006 \001(\t\022\020\n\010registId\030\007 \001(\t"
          + "\022\020\n\010clientId\030\010 \001(\t\022\022\n\ndataInfoId\030\t \001(\t\022\n"
          + "\n\002ip\030\n \001(\t\022\014\n\004port\030\013 \001(\005\022\021\n\teventType\030\014 "
          + "\001(\t\022\017\n\007version\030\r \001(\003\022\021\n\ttimestamp\030\016 \001(\003\022"
          + "3\n\nattributes\030\017 \003(\0132\037.BaseRegisterPb.Att"
          + "ributesEntry\0321\n\017AttributesEntry\022\013\n\003key\030\001"
          + " \001(\t\022\r\n\005value\030\002 \001(\t:\0028\001B7\n/com.alipay.so"
          + "fa.registry.common.model.client.pbP\001Z\002pb"
          + "b\006proto3"
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
        descriptorData, new com.google.protobuf.Descriptors.FileDescriptor[] {}, assigner);
    internal_static_BaseRegisterPb_descriptor = getDescriptor().getMessageTypes().get(0);
    internal_static_BaseRegisterPb_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_BaseRegisterPb_descriptor,
            new java.lang.String[] {
              "InstanceId",
              "Zone",
              "AppName",
              "DataId",
              "Group",
              "ProcessId",
              "RegistId",
              "ClientId",
              "DataInfoId",
              "Ip",
              "Port",
              "EventType",
              "Version",
              "Timestamp",
              "Attributes",
            });
    internal_static_BaseRegisterPb_AttributesEntry_descriptor =
        internal_static_BaseRegisterPb_descriptor.getNestedTypes().get(0);
    internal_static_BaseRegisterPb_AttributesEntry_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_BaseRegisterPb_AttributesEntry_descriptor,
            new java.lang.String[] {
              "Key", "Value",
            });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
