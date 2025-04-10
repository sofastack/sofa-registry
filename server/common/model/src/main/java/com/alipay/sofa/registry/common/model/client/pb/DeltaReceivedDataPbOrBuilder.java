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

public interface DeltaReceivedDataPbOrBuilder
    extends
    // @@protoc_insertion_point(interface_extends:DeltaReceivedDataPb)
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

  /** <code>repeated string subscriberRegisterIds = 6;</code> */
  java.util.List<java.lang.String> getSubscriberRegisterIdsList();
  /** <code>repeated string subscriberRegisterIds = 6;</code> */
  int getSubscriberRegisterIdsCount();
  /** <code>repeated string subscriberRegisterIds = 6;</code> */
  java.lang.String getSubscriberRegisterIds(int index);
  /** <code>repeated string subscriberRegisterIds = 6;</code> */
  com.google.protobuf.ByteString getSubscriberRegisterIdsBytes(int index);

  /** <code>int64 version = 7;</code> */
  long getVersion();

  /** <code>string localZone = 8;</code> */
  java.lang.String getLocalZone();
  /** <code>string localZone = 8;</code> */
  com.google.protobuf.ByteString getLocalZoneBytes();

  /** <code>bool isDelta = 9;</code> */
  boolean getIsDelta();

  /** <code>.DeltaReceivedDataBodyPb deltaBody = 10;</code> */
  boolean hasDeltaBody();
  /** <code>.DeltaReceivedDataBodyPb deltaBody = 10;</code> */
  com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPb getDeltaBody();
  /** <code>.DeltaReceivedDataBodyPb deltaBody = 10;</code> */
  com.alipay.sofa.registry.common.model.client.pb.DeltaReceivedDataBodyPbOrBuilder
      getDeltaBodyOrBuilder();

  /** <code>.FullReceivedDataBodyPb fullBody = 11;</code> */
  boolean hasFullBody();
  /** <code>.FullReceivedDataBodyPb fullBody = 11;</code> */
  com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPb getFullBody();
  /** <code>.FullReceivedDataBodyPb fullBody = 11;</code> */
  com.alipay.sofa.registry.common.model.client.pb.FullReceivedDataBodyPbOrBuilder
      getFullBodyOrBuilder();
}
