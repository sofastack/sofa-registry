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

public interface ServiceAppMappingRequestOrBuilder
    extends
    // @@protoc_insertion_point(interface_extends:ServiceAppMappingRequest)
    com.google.protobuf.MessageOrBuilder {

  /** <code>repeated string serviceIds = 1;</code> */
  java.util.List<java.lang.String> getServiceIdsList();
  /** <code>repeated string serviceIds = 1;</code> */
  int getServiceIdsCount();
  /** <code>repeated string serviceIds = 1;</code> */
  java.lang.String getServiceIds(int index);
  /** <code>repeated string serviceIds = 1;</code> */
  com.google.protobuf.ByteString getServiceIdsBytes(int index);
}
