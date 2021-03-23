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

public interface MetaRegisterOrBuilder
    extends
    // @@protoc_insertion_point(interface_extends:MetaRegister)
    com.google.protobuf.MessageOrBuilder {

  /** <code>string application = 1;</code> */
  java.lang.String getApplication();
  /** <code>string application = 1;</code> */
  com.google.protobuf.ByteString getApplicationBytes();

  /** <code>string revision = 2;</code> */
  java.lang.String getRevision();
  /** <code>string revision = 2;</code> */
  com.google.protobuf.ByteString getRevisionBytes();

  /** <code>string clientVersion = 3;</code> */
  java.lang.String getClientVersion();
  /** <code>string clientVersion = 3;</code> */
  com.google.protobuf.ByteString getClientVersionBytes();

  /** <code>map&lt;string, .StringList&gt; baseParams = 4;</code> */
  int getBaseParamsCount();
  /** <code>map&lt;string, .StringList&gt; baseParams = 4;</code> */
  boolean containsBaseParams(java.lang.String key);
  /** Use {@link #getBaseParamsMap()} instead. */
  @java.lang.Deprecated
  java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
      getBaseParams();
  /** <code>map&lt;string, .StringList&gt; baseParams = 4;</code> */
  java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.StringList>
      getBaseParamsMap();
  /** <code>map&lt;string, .StringList&gt; baseParams = 4;</code> */
  com.alipay.sofa.registry.common.model.client.pb.StringList getBaseParamsOrDefault(
      java.lang.String key,
      com.alipay.sofa.registry.common.model.client.pb.StringList defaultValue);
  /** <code>map&lt;string, .StringList&gt; baseParams = 4;</code> */
  com.alipay.sofa.registry.common.model.client.pb.StringList getBaseParamsOrThrow(
      java.lang.String key);

  /** <code>map&lt;string, .MetaService&gt; services = 5;</code> */
  int getServicesCount();
  /** <code>map&lt;string, .MetaService&gt; services = 5;</code> */
  boolean containsServices(java.lang.String key);
  /** Use {@link #getServicesMap()} instead. */
  @java.lang.Deprecated
  java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaService>
      getServices();
  /** <code>map&lt;string, .MetaService&gt; services = 5;</code> */
  java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.MetaService>
      getServicesMap();
  /** <code>map&lt;string, .MetaService&gt; services = 5;</code> */
  com.alipay.sofa.registry.common.model.client.pb.MetaService getServicesOrDefault(
      java.lang.String key,
      com.alipay.sofa.registry.common.model.client.pb.MetaService defaultValue);
  /** <code>map&lt;string, .MetaService&gt; services = 5;</code> */
  com.alipay.sofa.registry.common.model.client.pb.MetaService getServicesOrThrow(
      java.lang.String key);
}
