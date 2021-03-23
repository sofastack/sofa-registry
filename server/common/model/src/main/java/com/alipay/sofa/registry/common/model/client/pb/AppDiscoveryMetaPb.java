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

public final class AppDiscoveryMetaPb {
  private AppDiscoveryMetaPb() {}

  public static void registerAllExtensions(com.google.protobuf.ExtensionRegistryLite registry) {}

  public static void registerAllExtensions(com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions((com.google.protobuf.ExtensionRegistryLite) registry);
  }

  static final com.google.protobuf.Descriptors.Descriptor internal_static_MetaRegister_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_MetaRegister_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_MetaRegister_BaseParamsEntry_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_MetaRegister_BaseParamsEntry_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_MetaRegister_ServicesEntry_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_MetaRegister_ServicesEntry_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor internal_static_MetaService_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_MetaService_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_MetaService_ParamsEntry_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_MetaService_ParamsEntry_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor internal_static_StringList_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_StringList_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor internal_static_AppList_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_AppList_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_ServiceAppMappingRequest_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ServiceAppMappingRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_ServiceAppMappingResponse_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ServiceAppMappingResponse_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_ServiceAppMappingResponse_ServiceAppMappingEntry_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ServiceAppMappingResponse_ServiceAppMappingEntry_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_GetRevisionsRequest_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_GetRevisionsRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_GetRevisionsResponse_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_GetRevisionsResponse_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_GetRevisionsResponse_RevisionsEntry_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_GetRevisionsResponse_RevisionsEntry_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_MetaHeartbeatRequest_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_MetaHeartbeatRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_MetaHeartbeatResponse_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_MetaHeartbeatResponse_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor getDescriptor() {
    return descriptor;
  }

  private static com.google.protobuf.Descriptors.FileDescriptor descriptor;

  static {
    java.lang.String[] descriptorData = {
      "\n\030AppDiscoveryMetaPb.proto\"\255\002\n\014MetaRegis"
          + "ter\022\023\n\013application\030\001 \001(\t\022\020\n\010revision\030\002 \001"
          + "(\t\022\025\n\rclientVersion\030\003 \001(\t\0221\n\nbaseParams\030"
          + "\004 \003(\0132\035.MetaRegister.BaseParamsEntry\022-\n\010"
          + "services\030\005 \003(\0132\033.MetaRegister.ServicesEn"
          + "try\032>\n\017BaseParamsEntry\022\013\n\003key\030\001 \001(\t\022\032\n\005v"
          + "alue\030\002 \001(\0132\013.StringList:\0028\001\032=\n\rServicesE"
          + "ntry\022\013\n\003key\030\001 \001(\t\022\033\n\005value\030\002 \001(\0132\014.MetaS"
          + "ervice:\0028\001\"\177\n\013MetaService\022\n\n\002id\030\001 \001(\t\022(\n"
          + "\006params\030\003 \003(\0132\030.MetaService.ParamsEntry\032"
          + ":\n\013ParamsEntry\022\013\n\003key\030\001 \001(\t\022\032\n\005value\030\002 \001"
          + "(\0132\013.StringList:\0028\001\"\034\n\nStringList\022\016\n\006val"
          + "ues\030\001 \003(\t\"(\n\007AppList\022\017\n\007version\030\001 \001(\003\022\014\n"
          + "\004apps\030\002 \003(\t\".\n\030ServiceAppMappingRequest\022"
          + "\022\n\nserviceIds\030\001 \003(\t\"\322\001\n\031ServiceAppMappin"
          + "gResponse\022L\n\021serviceAppMapping\030\001 \003(\01321.S"
          + "erviceAppMappingResponse.ServiceAppMappi"
          + "ngEntry\022\022\n\nstatusCode\030\002 \001(\005\022\017\n\007message\030\003"
          + " \001(\t\032B\n\026ServiceAppMappingEntry\022\013\n\003key\030\001 "
          + "\001(\t\022\027\n\005value\030\002 \001(\0132\010.AppList:\0028\001\"(\n\023GetR"
          + "evisionsRequest\022\021\n\trevisions\030\001 \003(\t\"\265\001\n\024G"
          + "etRevisionsResponse\0227\n\trevisions\030\001 \003(\0132$"
          + ".GetRevisionsResponse.RevisionsEntry\022\022\n\n"
          + "statusCode\030\002 \001(\005\022\017\n\007message\030\003 \001(\t\032?\n\016Rev"
          + "isionsEntry\022\013\n\003key\030\001 \001(\t\022\034\n\005value\030\002 \001(\0132"
          + "\r.MetaRegister:\0028\001\")\n\024MetaHeartbeatReque"
          + "st\022\021\n\trevisions\030\001 \003(\t\"<\n\025MetaHeartbeatRe"
          + "sponse\022\022\n\nstatusCode\030\001 \001(\005\022\017\n\007message\030\002 "
          + "\001(\tB:\n/com.alipay.sofa.registry.common.m"
          + "odel.client.pbP\001Z\005protob\006proto3"
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
    internal_static_MetaRegister_descriptor = getDescriptor().getMessageTypes().get(0);
    internal_static_MetaRegister_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_MetaRegister_descriptor,
            new java.lang.String[] {
              "Application", "Revision", "ClientVersion", "BaseParams", "Services",
            });
    internal_static_MetaRegister_BaseParamsEntry_descriptor =
        internal_static_MetaRegister_descriptor.getNestedTypes().get(0);
    internal_static_MetaRegister_BaseParamsEntry_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_MetaRegister_BaseParamsEntry_descriptor,
            new java.lang.String[] {
              "Key", "Value",
            });
    internal_static_MetaRegister_ServicesEntry_descriptor =
        internal_static_MetaRegister_descriptor.getNestedTypes().get(1);
    internal_static_MetaRegister_ServicesEntry_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_MetaRegister_ServicesEntry_descriptor,
            new java.lang.String[] {
              "Key", "Value",
            });
    internal_static_MetaService_descriptor = getDescriptor().getMessageTypes().get(1);
    internal_static_MetaService_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_MetaService_descriptor,
            new java.lang.String[] {
              "Id", "Params",
            });
    internal_static_MetaService_ParamsEntry_descriptor =
        internal_static_MetaService_descriptor.getNestedTypes().get(0);
    internal_static_MetaService_ParamsEntry_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_MetaService_ParamsEntry_descriptor,
            new java.lang.String[] {
              "Key", "Value",
            });
    internal_static_StringList_descriptor = getDescriptor().getMessageTypes().get(2);
    internal_static_StringList_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_StringList_descriptor,
            new java.lang.String[] {
              "Values",
            });
    internal_static_AppList_descriptor = getDescriptor().getMessageTypes().get(3);
    internal_static_AppList_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_AppList_descriptor,
            new java.lang.String[] {
              "Version", "Apps",
            });
    internal_static_ServiceAppMappingRequest_descriptor = getDescriptor().getMessageTypes().get(4);
    internal_static_ServiceAppMappingRequest_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_ServiceAppMappingRequest_descriptor,
            new java.lang.String[] {
              "ServiceIds",
            });
    internal_static_ServiceAppMappingResponse_descriptor = getDescriptor().getMessageTypes().get(5);
    internal_static_ServiceAppMappingResponse_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_ServiceAppMappingResponse_descriptor,
            new java.lang.String[] {
              "ServiceAppMapping", "StatusCode", "Message",
            });
    internal_static_ServiceAppMappingResponse_ServiceAppMappingEntry_descriptor =
        internal_static_ServiceAppMappingResponse_descriptor.getNestedTypes().get(0);
    internal_static_ServiceAppMappingResponse_ServiceAppMappingEntry_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_ServiceAppMappingResponse_ServiceAppMappingEntry_descriptor,
            new java.lang.String[] {
              "Key", "Value",
            });
    internal_static_GetRevisionsRequest_descriptor = getDescriptor().getMessageTypes().get(6);
    internal_static_GetRevisionsRequest_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_GetRevisionsRequest_descriptor,
            new java.lang.String[] {
              "Revisions",
            });
    internal_static_GetRevisionsResponse_descriptor = getDescriptor().getMessageTypes().get(7);
    internal_static_GetRevisionsResponse_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_GetRevisionsResponse_descriptor,
            new java.lang.String[] {
              "Revisions", "StatusCode", "Message",
            });
    internal_static_GetRevisionsResponse_RevisionsEntry_descriptor =
        internal_static_GetRevisionsResponse_descriptor.getNestedTypes().get(0);
    internal_static_GetRevisionsResponse_RevisionsEntry_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_GetRevisionsResponse_RevisionsEntry_descriptor,
            new java.lang.String[] {
              "Key", "Value",
            });
    internal_static_MetaHeartbeatRequest_descriptor = getDescriptor().getMessageTypes().get(8);
    internal_static_MetaHeartbeatRequest_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_MetaHeartbeatRequest_descriptor,
            new java.lang.String[] {
              "Revisions",
            });
    internal_static_MetaHeartbeatResponse_descriptor = getDescriptor().getMessageTypes().get(9);
    internal_static_MetaHeartbeatResponse_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_MetaHeartbeatResponse_descriptor,
            new java.lang.String[] {
              "StatusCode", "Message",
            });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
