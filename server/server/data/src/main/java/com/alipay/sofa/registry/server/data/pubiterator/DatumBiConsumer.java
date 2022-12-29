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
package com.alipay.sofa.registry.server.data.pubiterator;

import com.alipay.sofa.registry.common.model.RegisterVersion;
import com.alipay.sofa.registry.common.model.dataserver.DatumSummary;
import com.alipay.sofa.registry.common.model.slot.filter.SyncAcceptorRequest;
import com.alipay.sofa.registry.common.model.slot.filter.SyncSlotAcceptorManager;
import com.alipay.sofa.registry.server.data.cache.PublisherEnvelope;
import com.alipay.sofa.registry.server.data.cache.PublisherGroup;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * @author xiaojian.xj
 * @version : DatumBiConsumer.java, v 0.1 2022年07月23日 12:42 xiaojian.xj Exp $
 */
public class DatumBiConsumer {

  public static BiConsumer<String, PublisherGroup> publisherGroupsBiConsumer(
      Map<String, Map<String, DatumSummary>> summaries,
      Set<String> sessions,
      SyncSlotAcceptorManager syncSlotAcceptorManager) {
    for (String sessionIp : sessions) {
      summaries.computeIfAbsent(sessionIp, v -> Maps.newHashMapWithExpectedSize(64));
    }

    return (dataInfoId, publisherGroup) -> {
      Map<String /*sessionIp*/, Map<String /*registerId*/, RegisterVersion>> publisherVersions =
          Maps.newHashMapWithExpectedSize(sessions.size());

      if (!syncSlotAcceptorManager.accept(SyncAcceptorRequest.buildRequest(dataInfoId))) {
        return;
      }

      publisherGroup.foreach(
          publisherGroupBiConsumer(
              dataInfoId, publisherVersions, sessions, syncSlotAcceptorManager));
      Map<String, DatumSummary> sessionSummary = Maps.newHashMapWithExpectedSize(sessions.size());

      for (Entry<String, Map<String, RegisterVersion>> entry : publisherVersions.entrySet()) {
        sessionSummary.put(entry.getKey(), new DatumSummary(dataInfoId, entry.getValue()));
      }

      for (Entry<String, DatumSummary> entry : sessionSummary.entrySet()) {
        if (entry.getValue().isEmpty()) {
          continue;
        }
        Map<String, DatumSummary> summaryMap = summaries.get(entry.getKey());
        summaryMap.put(dataInfoId, entry.getValue());
      }
    };
  }

  public static BiConsumer<String, PublisherEnvelope> publisherGroupBiConsumer(
      String dataInfoId,
      Map<String, Map<String, RegisterVersion>> publisherVersions,
      Set<String> sessions,
      SyncSlotAcceptorManager syncSlotAcceptorManager) {

    for (String sessionIp : sessions) {
      publisherVersions.computeIfAbsent(sessionIp, k -> Maps.newHashMapWithExpectedSize(64));
    }

    return (registerId, envelope) -> {
      RegisterVersion v = envelope.getVersionIfPub();
      // v = null when envelope is unpub
      if (v == null
          || !syncSlotAcceptorManager.accept(
              SyncAcceptorRequest.buildRequest(
                  dataInfoId, envelope.getPublisher().getPublishSource()))) {
        return;
      }

      if (sessions.contains(envelope.getSessionProcessId().getHostAddress())) {
        publisherVersions.get(envelope.getSessionProcessId().getHostAddress()).put(registerId, v);
      }
    };
  }

  public static BiConsumer<String, PublisherGroup> publisherGroupsBiConsumer(
      Map<String, DatumSummary> summaries, SyncSlotAcceptorManager syncSlotAcceptorManager) {
    return (dataInfoId, publisherGroup) -> {
      if (!syncSlotAcceptorManager.accept(SyncAcceptorRequest.buildRequest(dataInfoId))) {
        return;
      }

      Map<String /*registerId*/, RegisterVersion> publisherVersions =
          Maps.newHashMapWithExpectedSize(publisherGroup.pubSize());
      publisherGroup.foreach(
          publisherGroupBiConsumer(dataInfoId, publisherVersions, syncSlotAcceptorManager));
      DatumSummary summary = new DatumSummary(dataInfoId, publisherVersions);
      summaries.put(dataInfoId, summary);
    };
  }

  public static BiConsumer<String, PublisherEnvelope> publisherGroupBiConsumer(
      String dataInfoId,
      Map<String, RegisterVersion> publisherVersions,
      SyncSlotAcceptorManager syncSlotAcceptorManager) {
    return (registerId, envelope) -> {
      RegisterVersion v = envelope.getVersionIfPub();
      // v = null when envelope is unpub
      if (v == null
          || !syncSlotAcceptorManager.accept(
              SyncAcceptorRequest.buildRequest(
                  dataInfoId, envelope.getPublisher().getPublishSource()))) {
        return;
      }
      publisherVersions.put(registerId, v);
    };
  }
}
