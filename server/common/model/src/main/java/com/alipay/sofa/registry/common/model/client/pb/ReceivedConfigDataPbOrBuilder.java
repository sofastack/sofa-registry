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

public interface ReceivedConfigDataPbOrBuilder
    extends
    // @@protoc_insertion_point(interface_extends:ReceivedConfigDataPb)
    com.google.protobuf.MessageOrBuilder {

  /** <code>string dataId = 1;</code> */
  java.lang.String getDataId();
  /** <code>string dataId = 1;</code> */
  com.google.protobuf.ByteString getDataIdBytes();

  /** <code>string group = 2;</code> */
  java.lang.String getGroup();
  /** <code>string group = 2;</code> */
  com.google.protobuf.ByteString getGroupBytes();

  /** <code>string instanceId = 3;</code> */
  java.lang.String getInstanceId();
  /** <code>string instanceId = 3;</code> */
  com.google.protobuf.ByteString getInstanceIdBytes();

  /** <code>repeated string configuratorRegistIds = 4;</code> */
  java.util.List<java.lang.String> getConfiguratorRegistIdsList();
  /** <code>repeated string configuratorRegistIds = 4;</code> */
  int getConfiguratorRegistIdsCount();
  /** <code>repeated string configuratorRegistIds = 4;</code> */
  java.lang.String getConfiguratorRegistIds(int index);
  /** <code>repeated string configuratorRegistIds = 4;</code> */
  com.google.protobuf.ByteString getConfiguratorRegistIdsBytes(int index);

  /** <code>.DataBoxPb dataBox = 5;</code> */
  boolean hasDataBox();
  /** <code>.DataBoxPb dataBox = 5;</code> */
  com.alipay.sofa.registry.common.model.client.pb.DataBoxPb getDataBox();
  /** <code>.DataBoxPb dataBox = 5;</code> */
  com.alipay.sofa.registry.common.model.client.pb.DataBoxPbOrBuilder getDataBoxOrBuilder();

  /** <code>int64 version = 6;</code> */
  long getVersion();
}
