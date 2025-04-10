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

public interface DeltaReceivedDataBodyPbOrBuilder
    extends
    // @@protoc_insertion_point(interface_extends:DeltaReceivedDataBodyPb)
    com.google.protobuf.MessageOrBuilder {

  /** <code>map&lt;string, .PushResourcesPb&gt; addPublishers = 1;</code> */
  int getAddPublishersCount();
  /** <code>map&lt;string, .PushResourcesPb&gt; addPublishers = 1;</code> */
  boolean containsAddPublishers(java.lang.String key);
  /** Use {@link #getAddPublishersMap()} instead. */
  @java.lang.Deprecated
  java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
      getAddPublishers();
  /** <code>map&lt;string, .PushResourcesPb&gt; addPublishers = 1;</code> */
  java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb>
      getAddPublishersMap();
  /** <code>map&lt;string, .PushResourcesPb&gt; addPublishers = 1;</code> */
  com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb getAddPublishersOrDefault(
      java.lang.String key,
      com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb defaultValue);
  /** <code>map&lt;string, .PushResourcesPb&gt; addPublishers = 1;</code> */
  com.alipay.sofa.registry.common.model.client.pb.PushResourcesPb getAddPublishersOrThrow(
      java.lang.String key);

  /** <code>map&lt;string, .RegisterIdsPb&gt; deletePublisherRegisterIds = 2;</code> */
  int getDeletePublisherRegisterIdsCount();
  /** <code>map&lt;string, .RegisterIdsPb&gt; deletePublisherRegisterIds = 2;</code> */
  boolean containsDeletePublisherRegisterIds(java.lang.String key);
  /** Use {@link #getDeletePublisherRegisterIdsMap()} instead. */
  @java.lang.Deprecated
  java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb>
      getDeletePublisherRegisterIds();
  /** <code>map&lt;string, .RegisterIdsPb&gt; deletePublisherRegisterIds = 2;</code> */
  java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb>
      getDeletePublisherRegisterIdsMap();
  /** <code>map&lt;string, .RegisterIdsPb&gt; deletePublisherRegisterIds = 2;</code> */
  com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb
      getDeletePublisherRegisterIdsOrDefault(
          java.lang.String key,
          com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb defaultValue);
  /** <code>map&lt;string, .RegisterIdsPb&gt; deletePublisherRegisterIds = 2;</code> */
  com.alipay.sofa.registry.common.model.client.pb.RegisterIdsPb
      getDeletePublisherRegisterIdsOrThrow(java.lang.String key);

  /** <code>map&lt;string, .DeltaDigestPb&gt; zoneDigests = 3;</code> */
  int getZoneDigestsCount();
  /** <code>map&lt;string, .DeltaDigestPb&gt; zoneDigests = 3;</code> */
  boolean containsZoneDigests(java.lang.String key);
  /** Use {@link #getZoneDigestsMap()} instead. */
  @java.lang.Deprecated
  java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb>
      getZoneDigests();
  /** <code>map&lt;string, .DeltaDigestPb&gt; zoneDigests = 3;</code> */
  java.util.Map<java.lang.String, com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb>
      getZoneDigestsMap();
  /** <code>map&lt;string, .DeltaDigestPb&gt; zoneDigests = 3;</code> */
  com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb getZoneDigestsOrDefault(
      java.lang.String key,
      com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb defaultValue);
  /** <code>map&lt;string, .DeltaDigestPb&gt; zoneDigests = 3;</code> */
  com.alipay.sofa.registry.common.model.client.pb.DeltaDigestPb getZoneDigestsOrThrow(
      java.lang.String key);
}
