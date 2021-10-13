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

public interface ReceivedDataBodyPbOrBuilder
    extends
    // @@protoc_insertion_point(interface_extends:ReceivedDataBodyPb)
    com.google.protobuf.MessageOrBuilder {

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
}
