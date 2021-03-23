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

public interface SyncConfigResponsePbOrBuilder
    extends
    // @@protoc_insertion_point(interface_extends:SyncConfigResponsePb)
    com.google.protobuf.MessageOrBuilder {

  /** <code>.ResultPb result = 1;</code> */
  boolean hasResult();
  /** <code>.ResultPb result = 1;</code> */
  com.alipay.sofa.registry.common.model.client.pb.ResultPb getResult();
  /** <code>.ResultPb result = 1;</code> */
  com.alipay.sofa.registry.common.model.client.pb.ResultPbOrBuilder getResultOrBuilder();

  /** <code>repeated string availableSegments = 2;</code> */
  java.util.List<java.lang.String> getAvailableSegmentsList();
  /** <code>repeated string availableSegments = 2;</code> */
  int getAvailableSegmentsCount();
  /** <code>repeated string availableSegments = 2;</code> */
  java.lang.String getAvailableSegments(int index);
  /** <code>repeated string availableSegments = 2;</code> */
  com.google.protobuf.ByteString getAvailableSegmentsBytes(int index);

  /** <code>int32 retryInterval = 3;</code> */
  int getRetryInterval();
}
