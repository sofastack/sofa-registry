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

public final class DeltaDigestPbOuterClass {
  private DeltaDigestPbOuterClass() {}

  public static void registerAllExtensions(com.google.protobuf.ExtensionRegistryLite registry) {}

  public static void registerAllExtensions(com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions((com.google.protobuf.ExtensionRegistryLite) registry);
  }

  static final com.google.protobuf.Descriptors.Descriptor internal_static_DeltaDigestPb_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_DeltaDigestPb_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor getDescriptor() {
    return descriptor;
  }

  private static com.google.protobuf.Descriptors.FileDescriptor descriptor;

  static {
    java.lang.String[] descriptorData = {
      "\n\023DeltaDigestPb.proto\"\254\001\n\rDeltaDigestPb\022"
          + "\026\n\016publisherCount\030\001 \001(\003\022\021\n\tdataCount\030\002 \001"
          + "(\003\022\031\n\021registerIdHashSum\030\003 \001(\003\022\031\n\021registe"
          + "rIdHashXOR\030\004 \001(\003\022\034\n\024registerTimestampSum"
          + "\030\005 \001(\003\022\034\n\024registerTimestampXOR\030\006 \001(\003B:\n/"
          + "com.alipay.sofa.registry.common.model.cl"
          + "ient.pbP\001Z\005protob\006proto3"
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
    internal_static_DeltaDigestPb_descriptor = getDescriptor().getMessageTypes().get(0);
    internal_static_DeltaDigestPb_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_DeltaDigestPb_descriptor,
            new java.lang.String[] {
              "PublisherCount",
              "DataCount",
              "RegisterIdHashSum",
              "RegisterIdHashXOR",
              "RegisterTimestampSum",
              "RegisterTimestampXOR",
            });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
