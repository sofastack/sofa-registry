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

public interface MultiReceivedDataPbOrBuilder
    extends
    // @@protoc_insertion_point(interface_extends:MultiReceivedDataPb)
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
   * <code>string scope = 4;</code>
   *
   * @return The scope.
   */
  java.lang.String getScope();
  /**
   * <code>string scope = 4;</code>
   *
   * @return The bytes for scope.
   */
  com.google.protobuf.ByteString getScopeBytes();

  /**
   * <code>repeated string subscriberRegistIds = 5;</code>
   *
   * @return A list containing the subscriberRegistIds.
   */
  java.util.List<java.lang.String> getSubscriberRegistIdsList();
  /**
   * <code>repeated string subscriberRegistIds = 5;</code>
   *
   * @return The count of subscriberRegistIds.
   */
  int getSubscriberRegistIdsCount();
  /**
   * <code>repeated string subscriberRegistIds = 5;</code>
   *
   * @param index The index of the element to return.
   * @return The subscriberRegistIds at the given index.
   */
  java.lang.String getSubscriberRegistIds(int index);
  /**
   * <code>repeated string subscriberRegistIds = 5;</code>
   *
   * @param index The index of the value to return.
   * @return The bytes of the subscriberRegistIds at the given index.
   */
  com.google.protobuf.ByteString getSubscriberRegistIdsBytes(int index);

  /**
   * <code>string localSegment = 6;</code>
   *
   * @return The localSegment.
   */
  java.lang.String getLocalSegment();
  /**
   * <code>string localSegment = 6;</code>
   *
   * @return The bytes for localSegment.
   */
  com.google.protobuf.ByteString getLocalSegmentBytes();

  /**
   * <code>string localZone = 7;</code>
   *
   * @return The localZone.
   */
  java.lang.String getLocalZone();
  /**
   * <code>string localZone = 7;</code>
   *
   * @return The bytes for localZone.
   */
  com.google.protobuf.ByteString getLocalZoneBytes();

  /** <code>map&lt;string, .MultiSegmentDataPb&gt; multiData = 8;</code> */
  int getMultiDataCount();
  /** <code>map&lt;string, .MultiSegmentDataPb&gt; multiData = 8;</code> */
  boolean containsMultiData(java.lang.String key);
  /** Use {@link #getMultiDataMap()} instead. */
  @java.lang.Deprecated
  java.util.Map<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb>
      getMultiData();
  /** <code>map&lt;string, .MultiSegmentDataPb&gt; multiData = 8;</code> */
  java.util.Map<
          java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb>
      getMultiDataMap();
  /** <code>map&lt;string, .MultiSegmentDataPb&gt; multiData = 8;</code> */
  com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb getMultiDataOrDefault(
      java.lang.String key,
      com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb defaultValue);
  /** <code>map&lt;string, .MultiSegmentDataPb&gt; multiData = 8;</code> */
  com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb getMultiDataOrThrow(
      java.lang.String key);
}
