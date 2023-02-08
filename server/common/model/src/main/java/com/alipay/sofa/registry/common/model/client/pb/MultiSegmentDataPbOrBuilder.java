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

public interface MultiSegmentDataPbOrBuilder
    extends
    // @@protoc_insertion_point(interface_extends:MultiSegmentDataPb)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string segment = 1;</code>
   *
   * @return The segment.
   */
  java.lang.String getSegment();
  /**
   * <code>string segment = 1;</code>
   *
   * @return The bytes for segment.
   */
  com.google.protobuf.ByteString getSegmentBytes();

  /**
   * <code>bytes zipData = 2;</code>
   *
   * @return The zipData.
   */
  com.google.protobuf.ByteString getZipData();

  /** <code>map&lt;string, .DataBoxesPb&gt; unzipData = 3;</code> */
  int getUnzipDataCount();
  /** <code>map&lt;string, .DataBoxesPb&gt; unzipData = 3;</code> */
  boolean containsUnzipData(java.lang.String key);
  /** Use {@link #getUnzipDataMap()} instead. */
  @java.lang.Deprecated
  java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
      getUnzipData();
  /** <code>map&lt;string, .DataBoxesPb&gt; unzipData = 3;</code> */
  java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb>
      getUnzipDataMap();
  /** <code>map&lt;string, .DataBoxesPb&gt; unzipData = 3;</code> */
  com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb getUnzipDataOrDefault(
      java.lang.String key,
      com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb defaultValue);
  /** <code>map&lt;string, .DataBoxesPb&gt; unzipData = 3;</code> */
  com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb getUnzipDataOrThrow(
      java.lang.String key);

  /**
   * <code>string encoding = 4;</code>
   *
   * @return The encoding.
   */
  java.lang.String getEncoding();
  /**
   * <code>string encoding = 4;</code>
   *
   * @return The bytes for encoding.
   */
  com.google.protobuf.ByteString getEncodingBytes();

  /**
   * <code>int64 version = 5;</code>
   *
   * @return The version.
   */
  long getVersion();

  /** <code>map&lt;string, int32&gt; pushDataCount = 6;</code> */
  int getPushDataCountCount();
  /** <code>map&lt;string, int32&gt; pushDataCount = 6;</code> */
  boolean containsPushDataCount(java.lang.String key);
  /** Use {@link #getPushDataCountMap()} instead. */
  @java.lang.Deprecated
  java.util.Map<java.lang.String, java.lang.Integer> getPushDataCount();
  /** <code>map&lt;string, int32&gt; pushDataCount = 6;</code> */
  java.util.Map<java.lang.String, java.lang.Integer> getPushDataCountMap();
  /** <code>map&lt;string, int32&gt; pushDataCount = 6;</code> */
  int getPushDataCountOrDefault(java.lang.String key, int defaultValue);
  /** <code>map&lt;string, int32&gt; pushDataCount = 6;</code> */
  int getPushDataCountOrThrow(java.lang.String key);
}
