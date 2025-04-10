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

public interface FullReceivedDataBodyPbOrBuilder
    extends
    // @@protoc_insertion_point(interface_extends:FullReceivedDataBodyPb)
    com.google.protobuf.MessageOrBuilder {

  /** <code>string encoding = 1;</code> */
  java.lang.String getEncoding();
  /** <code>string encoding = 1;</code> */
  com.google.protobuf.ByteString getEncodingBytes();

  /** <code>bytes body = 2;</code> */
  com.google.protobuf.ByteString getBody();

  /** <code>int32 originBodySize = 3;</code> */
  int getOriginBodySize();

  /** <code>map&lt;string, .PushResourcesPb&gt; data = 4;</code> */
  int getDataCount();
  /** <code>map&lt;string, .PushResourcesPb&gt; data = 4;</code> */
  boolean containsData(java.lang.String key);
  /** Use {@link #getDataMap()} instead. */
  @java.lang.Deprecated
  java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
      getData();
  /** <code>map&lt;string, .PushResourcesPb&gt; data = 4;</code> */
  java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
      getDataMap();
  /** <code>map&lt;string, .PushResourcesPb&gt; data = 4;</code> */
  com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb getDataOrDefault(
      java.lang.String key,
      com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb defaultValue);
  /** <code>map&lt;string, .PushResourcesPb&gt; data = 4;</code> */
  com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb getDataOrThrow(
      java.lang.String key);
}
