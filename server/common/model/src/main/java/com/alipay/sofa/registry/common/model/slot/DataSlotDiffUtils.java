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

import com.alipay.sofa.registry.common.model.PublisherVersion;
import com.alipay.sofa.registry.common.model.dataserver.DatumSummary;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.log.Logger;
import org.apache.commons.collections.MapUtils;

import java.util.*;

/**
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-24 15:46 yuzhi.lyz Exp $
 */
public final class DataSlotDiffUtils {
    private DataSlotDiffUtils() {
    }

    public static DataSlotDiffSyncResult diffDataInfoIdsResult(Set<String> targetDataInfoIds,
                                                               Map<String, Map<String, Publisher>> sourcePublishers,
                                                               int publisherMaxNum) {
        Map<String, List<Publisher>> adds = new HashMap<>();
        for (Map.Entry<String, Map<String, Publisher>> e : sourcePublishers.entrySet()) {
            final String dataInfoId = e.getKey();
            if (targetDataInfoIds.contains(dataInfoId)) {
                continue;
            }
            if (MapUtils.isEmpty(e.getValue())) {
                continue;
            }
            adds.put(dataInfoId, new ArrayList<>(e.getValue().values()));
        }

        Map<String, List<Publisher>> updateds = new HashMap<>(sourcePublishers.size());
        int publisherCount = 0;

        for (Map.Entry<String, List<Publisher>> add : adds.entrySet()) {
            publisherCount += add.getValue().size();

            if (updateds.isEmpty()) {
                // add at lease one
                updateds.put(add.getKey(), add.getValue());
                continue;
            }
            if (publisherCount >= publisherMaxNum) {
                // too many publishers, mark has remain
                break;
            }
            updateds.put(add.getKey(), add.getValue());
        }

        final boolean hasRemain = updateds.size() != adds.size();
        // find the removed dataInfoIds
        List<String> removeds = new ArrayList<>();
        for (String dataInfoId : targetDataInfoIds) {
            if (!sourcePublishers.containsKey(dataInfoId)) {
                removeds.add(dataInfoId);
            }
        }
        DataSlotDiffSyncResult result = new DataSlotDiffSyncResult(hasRemain, updateds, removeds,
            Collections.emptyMap());
        return result;
    }

    public static DataSlotDiffSyncResult diffPublishersResult(Map<String, DatumSummary> targetDatumSummarys,
                                                              Map<String, Map<String, Publisher>> sourcePublishers,
                                                              int publisherMaxNum) {
        Map<String, List<Publisher>> updateds = new HashMap<>(targetDatumSummarys.size());
        Map<String, List<String>> removedPublishers = new HashMap<>();
        List<String> removedDataInfoIds = new ArrayList<>();

        int publisherCount = 0;
        int checkRound = 0;
        for (Map.Entry<String, DatumSummary> summary : targetDatumSummarys.entrySet()) {
            checkRound++;
            final String dataInfoId = summary.getKey();
            Map<String, Publisher> publisherMap = sourcePublishers.get(dataInfoId);
            if (publisherMap == null) {
                // the dataInfoId has removed
                removedDataInfoIds.add(dataInfoId);
                continue;
            }
            Set<String> registerIds = summary.getValue().getPublisherVersions().keySet();
            for (String registerId : registerIds) {
                if (!publisherMap.containsKey(registerId)) {
                    List<String> list = removedPublishers.computeIfAbsent(dataInfoId, k -> new ArrayList<>());
                    list.add(registerId);
                }
            }
            List<Publisher> publishers = new ArrayList<>();
            Map<String, PublisherVersion> versions = summary.getValue().getPublisherVersions();
            for (Map.Entry<String, Publisher> p : publisherMap.entrySet()) {
                final String registerId = p.getKey();
                if (!versions.containsKey(registerId)) {
                    publishers.add(p.getValue());
                    continue;
                }
                // compare version
                if (p.getValue().publisherVersion().equals(versions.get(registerId))) {
                    // the same
                    continue;
                }
                publishers.add(p.getValue());
            }
            if (!publishers.isEmpty()) {
                publisherCount += publishers.size();
                updateds.put(dataInfoId, publishers);
            }
            if (publisherCount >= publisherMaxNum) {
                // too many publishers, mark has remain
                break;
            }
        }
        // the iter has break
        final boolean hasRemian = checkRound != targetDatumSummarys.size();
        DataSlotDiffSyncResult result = new DataSlotDiffSyncResult(hasRemian, updateds, removedDataInfoIds,
                removedPublishers);
        return result;
    }

    public static void logDiffResult(DataSlotDiffSyncResult result, int slotId, Logger log) {
        if (!result.getUpdatedPublishers().isEmpty()) {
            log.info("DiffSync, update dataInfoIds for slot={}, remain={}, dataInfoIds={}/{}, {}",
                slotId, result.isHasRemain(), result.getUpdatedPublishers().size(),
                result.getUpdatedPublishersCount(), result.getUpdatedPublishers().keySet());
        }
        if (!result.getRemovedPublishers().isEmpty()) {
            log.info("DiffSync, remove publishers for slot={}, dataInfoId={}/{}, {}", slotId,
                result.getRemovedPublishers().size(), result.getRemovedPublishersCount(), result
                    .getRemovedPublishers().keySet());
        }
        if (!result.getRemovedDataInfoIds().isEmpty()) {
            log.info("DiffSync, remove dataInfoIds for slot={}, dataInfoId={}, {}", slotId, result
                .getRemovedDataInfoIds().size(), result.getRemovedDataInfoIds());
        }
    }
}
