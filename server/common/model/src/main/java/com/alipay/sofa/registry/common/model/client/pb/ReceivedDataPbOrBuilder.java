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

  /**
   * <code>string dataId = 1;</code>
   *
   * @return The dataId.
   */
  java.lang.String getDataId();
  /**
   * <code>string dataId = 1;</code>
   *
   * @return The bytes for dataId.
   */
  com.google.protobuf.ByteString getDataIdBytes();

  /**
   * <code>string group = 2;</code>
   *
   * @return The group.
   */
  java.lang.String getGroup();
  /**
   * <code>string group = 2;</code>
   *
   * @return The bytes for group.
   */
  com.google.protobuf.ByteString getGroupBytes();

  /**
   * <code>string instanceId = 3;</code>
   *
   * @return The instanceId.
   */
  java.lang.String getInstanceId();
  /**
   * <code>string instanceId = 3;</code>
   *
   * @return The bytes for instanceId.
   */
  com.google.protobuf.ByteString getInstanceIdBytes();

  /**
   * <code>string segment = 4;</code>
   *
   * @return The segment.
   */
  java.lang.String getSegment();
  /**
   * <code>string segment = 4;</code>
   *
   * @return The bytes for segment.
   */
  com.google.protobuf.ByteString getSegmentBytes();

  /**
   * <code>string scope = 5;</code>
   *
   * @return The scope.
   */
  java.lang.String getScope();
  /**
   * <code>string scope = 5;</code>
   *
   * @return The bytes for scope.
   */
  com.google.protobuf.ByteString getScopeBytes();

  /**
   * <code>repeated string subscriberRegistIds = 6;</code>
   *
   * @return A list containing the subscriberRegistIds.
   */
  java.util.List<java.lang.String> getSubscriberRegistIdsList();
  /**
   * <code>repeated string subscriberRegistIds = 6;</code>
   *
   * @return The count of subscriberRegistIds.
   */
  int getSubscriberRegistIdsCount();
  /**
   * <code>repeated string subscriberRegistIds = 6;</code>
   *
   * @param index The index of the element to return.
   * @return The subscriberRegistIds at the given index.
   */
  java.lang.String getSubscriberRegistIds(int index);
  /**
   * <code>repeated string subscriberRegistIds = 6;</code>
   *
   * @param index The index of the value to return.
   * @return The bytes of the subscriberRegistIds at the given index.
   */
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

  /**
   * <code>int64 version = 8;</code>
   *
   * @return The version.
   */
  long getVersion();

  /**
   * <code>string localZone = 9;</code>
   *
   * @return The localZone.
   */
  java.lang.String getLocalZone();
  /**
   * <code>string localZone = 9;</code>
   *
   * @return The bytes for localZone.
   */
  com.google.protobuf.ByteString getLocalZoneBytes();

  /**
   * <code>string encoding = 10;</code>
   *
   * @return The encoding.
   */
  java.lang.String getEncoding();
  /**
   * <code>string encoding = 10;</code>
   *
   * @return The bytes for encoding.
   */
  com.google.protobuf.ByteString getEncodingBytes();

  /**
   * <code>bytes body = 11;</code>
   *
   * @return The body.
   */
  com.google.protobuf.ByteString getBody();

  /**
   * <code>int32 originBodySize = 12;</code>
   *
   * @return The originBodySize.
   */
  int getOriginBodySize();

  /** <code>map&lt;string, int32&gt; pushDataCount = 13;</code> */
  int getPushDataCountCount();
  /** <code>map&lt;string, int32&gt; pushDataCount = 13;</code> */
  boolean containsPushDataCount(java.lang.String key);
  /** Use {@link #getPushDataCountMap()} instead. */
  @java.lang.Deprecated
  java.util.Map<java.lang.String, java.lang.Integer> getPushDataCount();
  /** <code>map&lt;string, int32&gt; pushDataCount = 13;</code> */
  java.util.Map<java.lang.String, java.lang.Integer> getPushDataCountMap();
  /** <code>map&lt;string, int32&gt; pushDataCount = 13;</code> */
  int getPushDataCountOrDefault(java.lang.String key, int defaultValue);
  /** <code>map&lt;string, int32&gt; pushDataCount = 13;</code> */
  int getPushDataCountOrThrow(java.lang.String key);
}
