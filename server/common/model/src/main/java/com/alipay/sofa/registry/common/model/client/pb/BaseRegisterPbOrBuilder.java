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

public interface BaseRegisterPbOrBuilder
    extends
    // @@protoc_insertion_point(interface_extends:BaseRegisterPb)
    com.google.protobuf.MessageOrBuilder {

  /** <code>string instanceId = 1;</code> */
  java.lang.String getInstanceId();
  /** <code>string instanceId = 1;</code> */
  com.google.protobuf.ByteString getInstanceIdBytes();

  /** <code>string zone = 2;</code> */
  java.lang.String getZone();
  /** <code>string zone = 2;</code> */
  com.google.protobuf.ByteString getZoneBytes();

  /** <code>string appName = 3;</code> */
  java.lang.String getAppName();
  /** <code>string appName = 3;</code> */
  com.google.protobuf.ByteString getAppNameBytes();

  /** <code>string dataId = 4;</code> */
  java.lang.String getDataId();
  /** <code>string dataId = 4;</code> */
  com.google.protobuf.ByteString getDataIdBytes();

  /** <code>string group = 5;</code> */
  java.lang.String getGroup();
  /** <code>string group = 5;</code> */
  com.google.protobuf.ByteString getGroupBytes();

  /** <code>string processId = 6;</code> */
  java.lang.String getProcessId();
  /** <code>string processId = 6;</code> */
  com.google.protobuf.ByteString getProcessIdBytes();

  /** <code>string registId = 7;</code> */
  java.lang.String getRegistId();
  /** <code>string registId = 7;</code> */
  com.google.protobuf.ByteString getRegistIdBytes();

  /** <code>string clientId = 8;</code> */
  java.lang.String getClientId();
  /** <code>string clientId = 8;</code> */
  com.google.protobuf.ByteString getClientIdBytes();

  /** <code>string dataInfoId = 9;</code> */
  java.lang.String getDataInfoId();
  /** <code>string dataInfoId = 9;</code> */
  com.google.protobuf.ByteString getDataInfoIdBytes();

  /** <code>string ip = 10;</code> */
  java.lang.String getIp();
  /** <code>string ip = 10;</code> */
  com.google.protobuf.ByteString getIpBytes();

  /** <code>int32 port = 11;</code> */
  int getPort();

  /** <code>string eventType = 12;</code> */
  java.lang.String getEventType();
  /** <code>string eventType = 12;</code> */
  com.google.protobuf.ByteString getEventTypeBytes();

  /** <code>int64 version = 13;</code> */
  long getVersion();

  /** <code>int64 timestamp = 14;</code> */
  long getTimestamp();

  /** <code>map&lt;string, string&gt; attributes = 15;</code> */
  int getAttributesCount();
  /** <code>map&lt;string, string&gt; attributes = 15;</code> */
  boolean containsAttributes(java.lang.String key);
  /** Use {@link #getAttributesMap()} instead. */
  @java.lang.Deprecated
  java.util.Map<java.lang.String, java.lang.String> getAttributes();
  /** <code>map&lt;string, string&gt; attributes = 15;</code> */
  java.util.Map<java.lang.String, java.lang.String> getAttributesMap();
  /** <code>map&lt;string, string&gt; attributes = 15;</code> */
  java.lang.String getAttributesOrDefault(java.lang.String key, java.lang.String defaultValue);
  /** <code>map&lt;string, string&gt; attributes = 15;</code> */
  java.lang.String getAttributesOrThrow(java.lang.String key);
}
