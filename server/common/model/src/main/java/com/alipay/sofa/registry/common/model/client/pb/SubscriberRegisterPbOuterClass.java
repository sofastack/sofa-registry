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

public final class SubscriberRegisterPbOuterClass {
  private SubscriberRegisterPbOuterClass() {}

  public static void registerAllExtensions(com.google.protobuf.ExtensionRegistryLite registry) {}

  public static void registerAllExtensions(com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions((com.google.protobuf.ExtensionRegistryLite) registry);
  }

  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_SubscriberRegisterPb_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_SubscriberRegisterPb_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor getDescriptor() {
    return descriptor;
  }

  private static com.google.protobuf.Descriptors.FileDescriptor descriptor;

  static {
    java.lang.String[] descriptorData = {
      "\n\032SubscriberRegisterPb.proto\032\024BaseRegist"
          + "erPb.proto\"y\n\024SubscriberRegisterPb\022\r\n\005sc"
          + "ope\030\001 \001(\t\022%\n\014baseRegister\030\002 \001(\0132\017.BaseRe"
          + "gisterPb\022\026\n\016acceptEncoding\030\003 \001(\t\022\023\n\013acce"
          + "ptMulti\030\004 \001(\010B:\n/com.alipay.sofa.registr"
          + "y.common.model.client.pbP\001Z\005protob\006proto"
          + "3"
    };
    descriptor =
        com.google.protobuf.Descriptors.FileDescriptor.internalBuildGeneratedFileFrom(
            descriptorData,
            new com.google.protobuf.Descriptors.FileDescriptor[] {
              com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPbOuterClass
                  .getDescriptor(),
            });
    internal_static_SubscriberRegisterPb_descriptor = getDescriptor().getMessageTypes().get(0);
    internal_static_SubscriberRegisterPb_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_SubscriberRegisterPb_descriptor,
            new java.lang.String[] {
              "Scope", "BaseRegister", "AcceptEncoding", "AcceptMulti",
            });
    com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPbOuterClass.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
