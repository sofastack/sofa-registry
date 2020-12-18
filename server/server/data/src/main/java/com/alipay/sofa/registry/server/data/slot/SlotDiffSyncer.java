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
package com.alipay.sofa.registry.server.data.slot;

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.ProcessId;
import com.alipay.sofa.registry.common.model.dataserver.DatumSummary;
import com.alipay.sofa.registry.common.model.slot.DataSlotDiffDataInfoIdRequest;
import com.alipay.sofa.registry.common.model.slot.DataSlotDiffPublisherRequest;
import com.alipay.sofa.registry.common.model.slot.DataSlotDiffSyncResult;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.exchange.RequestException;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumStorage;
import com.alipay.sofa.registry.server.data.change.event.DataChangeEventCenter;
import com.alipay.sofa.registry.server.data.lease.SessionLeaseManager;
import com.alipay.sofa.registry.server.data.remoting.DataNodeExchanger;
import com.alipay.sofa.registry.server.data.remoting.SessionNodeExchanger;
import com.alipay.sofa.registry.server.shared.remoting.ClientSideExchanger;
import org.glassfish.jersey.internal.guava.Sets;

import java.util.*;

/**
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-20 13:56 yuzhi.lyz Exp $
 */
public final class SlotDiffSyncer {
    private static final Logger         LOGGER = LoggerFactory.getLogger(SlotDiffSyncer.class);
    private final DataServerConfig      dataServerConfig;

    private final DatumStorage          datumStorage;
    private final DataChangeEventCenter dataChangeEventCenter;
    private final SessionLeaseManager   sessionLeaseManager;

    SlotDiffSyncer(DataServerConfig dataServerConfig, DatumStorage datumStorage,
                   DataChangeEventCenter dataChangeEventCenter,
                   SessionLeaseManager sessionLeaseManager) {
        this.dataServerConfig = dataServerConfig;
        this.datumStorage = datumStorage;
        this.dataChangeEventCenter = dataChangeEventCenter;
        this.sessionLeaseManager = sessionLeaseManager;
    }

    private List<String> processSyncRemovedDataInfoId(List<String> removeds,
                                                      ProcessId sessionProcessId) {
        List<String> ids = new ArrayList<>(removeds.size());
        for (String removed : removeds) {
            if (datumStorage.remove(removed, sessionProcessId) != null) {
                ids.add(removed);
            }
        }
        return ids;
    }

    private DataSlotDiffSyncResult processSyncResp(int slotId,
                                                   GenericResponse<DataSlotDiffSyncResult> resp,
                                                   String targetAddress, Map<String, DatumSummary> summarys) {
        if (resp == null || !resp.isSuccess()) {
            LOGGER.error("response failed when sync from {}, slot={}, resp={}", targetAddress,
                    slotId, resp);
            return null;
        }
        DataSlotDiffSyncResult result = resp.getData();

        // sync from session
        final ProcessId sessionProcessId = result.getSessionProcessId();
        if (sessionProcessId != null) {
            sessionLeaseManager.renewSession(sessionProcessId);
            LOGGER.info("renew session by sync {}", sessionProcessId);
        }

        if (result.isEmpty()) {
            LOGGER.info("sync slot={} from {}, empty", targetAddress, slotId);
            return result;
        }

        final Set<String> changeDataIds = Sets.newHashSet();
        changeDataIds.addAll(processSyncRemovedDataInfoId(result.getRemovedDataInfoIds(), sessionProcessId));

        result.getUpdatedPublishers().forEach((k, list) -> {
            if (datumStorage.update(k, list) != null) {
                changeDataIds.add(k);
            }
        });

        // for sync publishers
        result.getRemovedPublishers().forEach((k, list) -> {
            if (datumStorage.remove(k, sessionProcessId, summarys.get(k).getPublisherVersions(list)) != null) {
                changeDataIds.add(k);
            }
        });
        if (sessionProcessId != null) {
            // only trigger event when sync from session
            dataChangeEventCenter.onChange(changeDataIds, dataServerConfig.getLocalDataCenter());
        }
        LOGGER
                .info(
                        "sync slot={} from {}, updatedP {}:{}, removedD {}, removedP {}:{}, updatedD {}, removedD {}",
                        slotId, targetAddress, result.getUpdatedPublishers().size(), result
                                .getUpdatedPublishersCount(), result.getRemovedDataInfoIds().size(), result
                                .getRemovedPublishersCount(), result.getRemovedPublishers().size(), result
                                .getUpdatedPublishers().keySet(), result.getRemovedDataInfoIds());
        return result;
    }

    public boolean syncDataInfoIds(int slotId, String targetAddress, ClientSideExchanger exchanger,
                                   long slotTableEpoch, String summaryTargetIp) {
        for (;;) {
            Map<String, DatumSummary> summaryMap = datumStorage.getDatumSummary(slotId,
                summaryTargetIp);
            DataSlotDiffDataInfoIdRequest request = new DataSlotDiffDataInfoIdRequest(
                slotTableEpoch, slotId, new HashSet<>(summaryMap.keySet()));
            GenericResponse<DataSlotDiffSyncResult> resp = (GenericResponse<DataSlotDiffSyncResult>) exchanger
                .requestRaw(targetAddress, request).getResult();
            DataSlotDiffSyncResult result = processSyncResp(slotId, resp, targetAddress,
                Collections.emptyMap());
            if (result == null) {
                return false;
            }
            if (!result.isHasRemain()) {
                // the sync has finish
                return true;
            }
            // has remain, the next round
        }
    }

    public boolean syncPublishers(int slotId, String targetAddress, ClientSideExchanger exchanger, long slotTableEpoch,
                                  String summaryTargetIp, int maxPublishers) {
        Map<String, DatumSummary> summaryMap = datumStorage.getDatumSummary(slotId, summaryTargetIp);
        Map<String, DatumSummary> round = pickSummarys(summaryMap, maxPublishers);
        // sync for the existing dataInfoIds.publisher
        while (!summaryMap.isEmpty()) {
            // maybe to many publishers
            DataSlotDiffPublisherRequest request = new DataSlotDiffPublisherRequest(slotTableEpoch, slotId, round);

            GenericResponse<DataSlotDiffSyncResult> resp = (GenericResponse<DataSlotDiffSyncResult>) exchanger
                    .requestRaw(targetAddress, request).getResult();
            DataSlotDiffSyncResult result = processSyncResp(slotId, resp, targetAddress, round);
            if (result == null) {
                return false;
            }
            if (!result.isHasRemain()) {
                // the sync round has finish, enter next round
                round.keySet().forEach(d -> summaryMap.remove(d));
                round = pickSummarys(summaryMap, maxPublishers);
            }
            // has remain, resync the roud
        }
        return true;
    }

    public boolean syncSession(int slotId, String sessionIp, SessionNodeExchanger exchanger,
                               long slotTableEpoch) throws RequestException {
        boolean syncDataInfoIds = syncDataInfoIds(slotId, sessionIp, exchanger, slotTableEpoch,
            sessionIp);
        boolean syncPublishers = syncPublishers(slotId, sessionIp, exchanger, slotTableEpoch,
            sessionIp, dataServerConfig.getSlotSyncPublisherDigestMaxNum());
        return syncDataInfoIds && syncPublishers;
    }

    public boolean syncSlotLeader(int slotId, String slotLeaderIp, DataNodeExchanger exchanger,
                                  long slotTableEpoch) throws RequestException {
        boolean syncDataInfoIds = syncDataInfoIds(slotId, slotLeaderIp, exchanger, slotTableEpoch,
            null);
        boolean syncPublishers = syncPublishers(slotId, slotLeaderIp, exchanger, slotTableEpoch,
            null, dataServerConfig.getSlotSyncPublisherDigestMaxNum());
        return syncDataInfoIds && syncPublishers;
    }

    private Map<String, DatumSummary> pickSummarys(Map<String, DatumSummary> syncSummarys, int n) {
        Map<String, DatumSummary> m = new HashMap<>();
        for (Map.Entry<String, DatumSummary> e : syncSummarys.entrySet()) {
            // at least pick one
            m.put(e.getKey(), e.getValue());
            n -= e.getValue().getPublisherVersions().size();
            if (n <= 0) {
                break;
            }
        }
        return m;
    }
}
