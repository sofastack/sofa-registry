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
package com.alipay.sofa.registry.common.model.slot;

import com.alipay.sofa.registry.common.model.PublisherDigestUtil;
import com.alipay.sofa.registry.common.model.PublisherUtils;
import com.alipay.sofa.registry.common.model.RegisterVersion;
import com.alipay.sofa.registry.common.model.dataserver.DatumDigest;
import com.alipay.sofa.registry.common.model.dataserver.DatumSummary;
import com.alipay.sofa.registry.common.model.slot.filter.SyncAcceptorRequest;
import com.alipay.sofa.registry.common.model.slot.filter.SyncSlotAcceptorManager;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.log.Logger;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.*;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-24 15:46 yuzhi.lyz Exp $
 */
public final class DataSlotDiffUtils {

  private DataSlotDiffUtils() {}

  public static DataSlotDiffDigestResult diffDigestResult(
      Map<String, DatumDigest> targetDigestMap,
      Map<String, Map<String, Publisher>> sourcePublishers,
      SyncSlotAcceptorManager acceptorManager) {
    Map<String, DatumSummary> sourceSummaryMap =
        PublisherUtils.getDatumSummary(sourcePublishers, acceptorManager);
    Map<String, DatumDigest> digestMap = PublisherDigestUtil.digest(sourceSummaryMap);
    return diffDigest(targetDigestMap, digestMap);
  }

  public static DataSlotDiffDigestResult diffDigest(
      Map<String, DatumDigest> targetDigestMap, Map<String, DatumDigest> sourceDigestMap) {
    List<String> adds = Lists.newArrayList();
    List<String> updates = Lists.newArrayList();
    for (Map.Entry<String, DatumDigest> e : sourceDigestMap.entrySet()) {
      final String dataInfoId = e.getKey();
      DatumDigest targetDigest = targetDigestMap.get(dataInfoId);
      if (targetDigest == null) {
        adds.add(dataInfoId);
        continue;
      }
      if (!targetDigest.equals(e.getValue())) {
        updates.add(dataInfoId);
      }
    }

    // find the removed dataInfoIds
    List<String> removes = new ArrayList<>();
    for (String dataInfoId : targetDigestMap.keySet()) {
      if (!sourceDigestMap.containsKey(dataInfoId)) {
        removes.add(dataInfoId);
      }
    }
    DataSlotDiffDigestResult result = new DataSlotDiffDigestResult(updates, adds, removes);
    return result;
  }

  public static DataSlotDiffPublisherResult diffPublishersResult(
      Collection<DatumSummary> targetDatumSummaries,
      Map<String, Map<String, Publisher>> sourcePublishers,
      int publisherMaxNum,
      SyncSlotAcceptorManager acceptorManager) {
    Map<String, List<Publisher>> updatePublishers =
        Maps.newHashMapWithExpectedSize(targetDatumSummaries.size());
    Map<String, List<String>> removedPublishers = new HashMap<>();

    int publisherCount = 0;
    int checkRound = 0;
    for (DatumSummary summary : targetDatumSummaries) {
      checkRound++;
      final String dataInfoId = summary.getDataInfoId();
      Map<String, Publisher> publisherMap = sourcePublishers.get(dataInfoId);
      if (publisherMap == null) {
        // the dataInfoId has removed, do not handle it, diffDataInfoIds will handle it
        continue;
      }
      Set<String> registerIds = summary.getPublisherVersions().keySet();
      for (String registerId : registerIds) {
        if (!publisherMap.containsKey(registerId)) {
          List<String> list = removedPublishers.computeIfAbsent(dataInfoId, k -> new ArrayList<>());
          list.add(registerId);
        }
      }
      List<Publisher> publishers = new ArrayList<>();
      Map<String, RegisterVersion> versions = summary.getPublisherVersions();
      for (Map.Entry<String, Publisher> p : publisherMap.entrySet()) {

        // filter publishers
        if (!acceptorManager.accept(
            SyncAcceptorRequest.buildRequest(dataInfoId, p.getValue().getPublishSource()))) {
          continue;
        }
        final String registerId = p.getKey();
        if (!versions.containsKey(registerId)) {
          publishers.add(p.getValue());
          continue;
        }
        // compare version
        if (p.getValue().registerVersion().equals(versions.get(registerId))) {
          // the same
          continue;
        }
        publishers.add(p.getValue());
      }
      if (!publishers.isEmpty()) {
        publisherCount += publishers.size();
        updatePublishers.put(dataInfoId, publishers);
      }
      if (publisherCount >= publisherMaxNum) {
        // too many publishers, mark has remain
        break;
      }
    }
    // the iter has break
    final boolean hasRemian = checkRound != targetDatumSummaries.size();
    DataSlotDiffPublisherResult result =
        new DataSlotDiffPublisherResult(hasRemian, updatePublishers, removedPublishers);
    return result;
  }

  public static void logDiffResult(
      String requestDataCenter, DataSlotDiffPublisherResult result, int slotId, Logger logger) {
    if (!result.isEmpty()) {

      logger.info(
          "DiffPublisher, requestDataCenter={}, slotId={}, remain={}, update={}/{}, remove={}/{}, removes={}",
          requestDataCenter,
          slotId,
          result.isHasRemain(),
          result.getUpdatedPublishers().size(),
          result.getUpdatedPublishersCount(),
          result.getRemovedPublishers().size(),
          result.getRemovedPublishersCount(),
          result.getRemovedPublishers().keySet());
    }
  }

  public static void logDiffResult(
      String requestDataCenter, DataSlotDiffDigestResult result, int slotId, Logger logger) {
    if (!result.isEmpty()) {
      logger.info(
          "DiffDigest, requestDataCenter={}, slotId={}, update={}, add={}, remove={}, adds={}, removes={}",
          requestDataCenter,
          slotId,
          result.getUpdatedDataInfoIds().size(),
          result.getAddedDataInfoIds().size(),
          result.getRemovedDataInfoIds().size(),
          result.getAddedDataInfoIds(),
          result.getRemovedDataInfoIds());
    }
  }
}
