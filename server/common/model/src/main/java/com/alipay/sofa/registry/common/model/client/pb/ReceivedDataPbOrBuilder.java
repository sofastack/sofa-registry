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

public interface ReceivedDataPbOrBuilder
    extends
    // @@protoc_insertion_point(interface_extends:ReceivedDataPb)
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

  /** <code>string segment = 4;</code> */
  java.lang.String getSegment();
  /** <code>string segment = 4;</code> */
  com.google.protobuf.ByteString getSegmentBytes();

  /** <code>string scope = 5;</code> */
  java.lang.String getScope();
  /** <code>string scope = 5;</code> */
  com.google.protobuf.ByteString getScopeBytes();

  /** <code>repeated string subscriberRegistIds = 6;</code> */
  java.util.List<java.lang.String> getSubscriberRegistIdsList();
  /** <code>repeated string subscriberRegistIds = 6;</code> */
  int getSubscriberRegistIdsCount();
  /** <code>repeated string subscriberRegistIds = 6;</code> */
  java.lang.String getSubscriberRegistIds(int index);
  /** <code>repeated string subscriberRegistIds = 6;</code> */
  com.google.protobuf.ByteString getSubscriberRegistIdsBytes(int index);

  /** <code>map&lt;string, .DataBoxesPb&gt; data = 7;</code> */
  int getDataCount();
  /** <code>map&lt;string, .DataBoxesPb&gt; data = 7;</code> */
  boolean containsData(java.lang.String key);
  /** Use {@link #getDataMap()} instead. */
  @java.lang.Deprecated
  java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
      getData();
  /** <code>map&lt;string, .DataBoxesPb&gt; data = 7;</code> */
  java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
      getDataMap();
  /** <code>map&lt;string, .DataBoxesPb&gt; data = 7;</code> */
  com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb getDataOrDefault(
      java.lang.String key,
      com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb defaultValue);
  /** <code>map&lt;string, .DataBoxesPb&gt; data = 7;</code> */
  com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb getDataOrThrow(java.lang.String key);

  /** <code>int64 version = 8;</code> */
  long getVersion();

  /** <code>string localZone = 9;</code> */
  java.lang.String getLocalZone();
  /** <code>string localZone = 9;</code> */
  com.google.protobuf.ByteString getLocalZoneBytes();

  /** <code>string encoding = 10;</code> */
  java.lang.String getEncoding();
  /** <code>string encoding = 10;</code> */
  com.google.protobuf.ByteString getEncodingBytes();

  /** <code>bytes body = 11;</code> */
  com.google.protobuf.ByteString getBody();

  /** <code>int32 originBodySize = 12;</code> */
  int getOriginBodySize();
}
