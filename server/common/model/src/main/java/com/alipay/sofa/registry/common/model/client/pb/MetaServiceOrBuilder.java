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

public interface MetaServiceOrBuilder
    extends
    // @@protoc_insertion_point(interface_extends:MetaService)
    com.google.protobuf.MessageOrBuilder {

  /** <code>string id = 1;</code> */
  java.lang.String getId();
  /** <code>string id = 1;</code> */
  com.google.protobuf.ByteString getIdBytes();

  /** <code>map&lt;string, .StringList&gt; params = 3;</code> */
  int getParamsCount();
  /** <code>map&lt;string, .StringList&gt; params = 3;</code> */
  boolean containsParams(java.lang.String key);
  /** Use {@link #getParamsMap()} instead. */
  @java.lang.Deprecated
  java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
      getParams();
  /** <code>map&lt;string, .StringList&gt; params = 3;</code> */
  java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
      getParamsMap();
  /** <code>map&lt;string, .StringList&gt; params = 3;</code> */
  com.alipay.sofa.registry.common.model.client.pb.StringList getParamsOrDefault(
      java.lang.String key,
      com.alipay.sofa.registry.common.model.client.pb.StringList defaultValue);
  /** <code>map&lt;string, .StringList&gt; params = 3;</code> */
  com.alipay.sofa.registry.common.model.client.pb.StringList getParamsOrThrow(java.lang.String key);
}
