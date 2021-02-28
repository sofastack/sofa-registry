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
import com.alipay.sofa.registry.common.model.PublisherDigestUtil;
import com.alipay.sofa.registry.common.model.dataserver.DatumDigest;
import com.alipay.sofa.registry.common.model.dataserver.DatumSummary;
import com.alipay.sofa.registry.common.model.slot.*;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.exchange.RequestException;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumStorage;
import com.alipay.sofa.registry.server.data.change.DataChangeEventCenter;
import com.alipay.sofa.registry.server.data.lease.SessionLeaseManager;
import com.alipay.sofa.registry.server.data.remoting.DataNodeExchanger;
import com.alipay.sofa.registry.server.data.remoting.SessionNodeExchanger;
import com.alipay.sofa.registry.server.shared.remoting.ClientSideExchanger;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import static com.alipay.sofa.registry.server.data.slot.SlotMetrics.*;

import java.util.*;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-20 13:56 yuzhi.lyz Exp $
 */
public final class SlotDiffSyncer {
    private static final Logger         LOGGER      = LoggerFactory.getLogger(SlotDiffSyncer.class);
    private static final Logger         DIFF_LOGGER = LoggerFactory.getLogger("SYNC-DIFF");
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

    private DataSlotDiffPublisherResult processSyncPublisherResp(int slotId,
                                                                 GenericResponse<DataSlotDiffPublisherResult> resp,
                                                                 String targetAddress, Map<String, DatumSummary> summaryMap) {
        if (resp == null || !resp.isSuccess()) {
            LOGGER.error("DiffPublisherFailed, slotId={} from {}, resp={}", slotId, targetAddress, resp);
            return null;
        }
        DataSlotDiffPublisherResult result = resp.getData();

        // sync from session
        final ProcessId sessionProcessId = result.getSessionProcessId();
        if (sessionProcessId != null) {
            sessionLeaseManager.renewSession(sessionProcessId);
        }

        if (result.isEmpty()) {
            DIFF_LOGGER.info("DiffPublisherEmpty, slotId={} from {}", slotId, targetAddress);
            return result;
        }

        final Set<String> changeDataIds = Sets.newHashSet();
        result.getUpdatedPublishers().forEach((k, list) -> {
            if (datumStorage.update(k, list) != null) {
                changeDataIds.add(k);
            }
        });

        // for sync publishers
        result.getRemovedPublishers().forEach((k, list) -> {
            if (datumStorage.remove(k, sessionProcessId, summaryMap.get(k).getPublisherVersions(list)) != null) {
                changeDataIds.add(k);
            }
        });
        if (sessionProcessId != null && !changeDataIds.isEmpty()) {
            // only trigger event when sync from session
            dataChangeEventCenter.onChange(changeDataIds, dataServerConfig.getLocalDataCenter());
        }
        DIFF_LOGGER.info("DiffPublisher, slotId={} from {}, updatedP {}:{}, removedP {}:{}", slotId, targetAddress,
                result.getUpdatedPublishers().size(), result.getUpdatedPublishersCount(),
                result.getRemovedPublishersCount(), result.getRemovedPublishers().size());
        return result;
    }

    public boolean syncPublishers(int slotId, String targetAddress, ClientSideExchanger exchanger, long slotTableEpoch,
                                  Map<String, DatumSummary> summaryMap, int maxPublishers,
                                  SyncContinues continues, boolean syncSession) {
        // need the empty dataInfoId to add updatePublisher
        Map<String, DatumSummary> round = pickSummaries(summaryMap, maxPublishers);
        // sync for the existing dataInfoIds.publisher
        while (!summaryMap.isEmpty()) {
            if (!continues.continues()) {
                LOGGER.info("syncing publishers break, slotId={} from {}", slotId, targetAddress);
                return true;
            }
            // maybe to many publishers, spit round
            if (syncSession) {
                SyncSession.observeSyncSessionPub(slotId, DatumSummary.countPublisherSize(round.values()));
            } else {
                SyncLeader.observeSyncLeaderPub(slotId, DatumSummary.countPublisherSize(round.values()));
            }
            DataSlotDiffPublisherRequest request = new DataSlotDiffPublisherRequest(slotTableEpoch, slotId, round.values());

            GenericResponse<DataSlotDiffPublisherResult> resp = (GenericResponse<DataSlotDiffPublisherResult>) exchanger
                    .requestRaw(targetAddress, request).getResult();
            DataSlotDiffPublisherResult result = processSyncPublisherResp(slotId, resp, targetAddress, round);
            if (result == null) {
                return false;
            }
            if (!result.isHasRemain()) {
                // the sync round has finish, enter next round
                round.keySet().forEach(d -> summaryMap.remove(d));
                round = pickSummaries(summaryMap, maxPublishers);
            } else {
                // has remain, remove the synced dataInfoIds, enter next round
                Set<String> synced = result.syncDataInfoIds();
                for (String dataInfoId : synced) {
                    round.remove(dataInfoId);
                    summaryMap.remove(dataInfoId);
                }
            }
        }
        return true;
    }

    public boolean sync(int slotId, String targetAddress, ClientSideExchanger exchanger,
                        long slotTableEpoch, String summaryTargetIp, int maxPublishers,
                        SyncContinues continues) {
        Map<String, DatumSummary> summaryMap = datumStorage
            .getDatumSummary(slotId, summaryTargetIp);
        final boolean syncSession = summaryTargetIp != null;
        if (syncSession) {
            SyncSession.observeSyncSessionId(slotId, summaryMap.size());
        } else {
            SyncLeader.observeSyncLeaderId(slotId, summaryMap.size());
        }
        Map<String, DatumDigest> digestMap = PublisherDigestUtil.digest(summaryMap);
        DataSlotDiffDigestRequest request = new DataSlotDiffDigestRequest(slotTableEpoch, slotId,
            digestMap);
        GenericResponse<DataSlotDiffDigestResult> resp = (GenericResponse<DataSlotDiffDigestResult>) exchanger
            .requestRaw(targetAddress, request).getResult();
        DataSlotDiffDigestResult result = processSyncDigestResp(slotId, resp, targetAddress,
            summaryMap);
        if (result == null) {
            return false;
        }
        if (result.isEmpty()) {
            // no change
            return true;
        }
        final Map<String, DatumSummary> newSummaryMap = Maps.newHashMap();
        for (String add : result.getAddedDataInfoIds()) {
            newSummaryMap.put(add, new DatumSummary(add));
        }
        for (String update : result.getUpdatedDataInfoIds()) {
            newSummaryMap.put(update, summaryMap.get(update));
        }

        return syncPublishers(slotId, targetAddress, exchanger, slotTableEpoch, newSummaryMap,
            maxPublishers, continues, syncSession);
    }

    private DataSlotDiffDigestResult processSyncDigestResp(int slotId,
                                                           GenericResponse<DataSlotDiffDigestResult> resp,
                                                           String targetAddress, Map<String, DatumSummary> summaryMap) {
        if (resp == null || !resp.isSuccess()) {
            LOGGER.error("DiffDigestFailed, slotId={} from {}, resp={}", slotId, targetAddress, resp);
            return null;
        }
        DataSlotDiffDigestResult result = resp.getData();

        // sync from session
        final ProcessId sessionProcessId = result.getSessionProcessId();
        if (sessionProcessId != null) {
            sessionLeaseManager.renewSession(sessionProcessId);
        }

        if (result.isEmpty()) {
            DIFF_LOGGER.info("DiffDigestEmpty, slotId={} from {}", slotId, targetAddress);
            return result;
        }
        // do nothing with added dataInfoId, the added publishers would sync and update by sync.publisher
        // if we create a new empty datum when absent, it's dangerous.
        // if some not expect error occurs when sync publisher and no publisher write to datum,
        // it maybe trigger a empty push with bigger datum.version which created by new empty
        final Set<String> changeDataIds = Sets.newHashSet();
        result.getRemovedDataInfoIds().forEach(k -> {
            if (datumStorage.remove(k, sessionProcessId, summaryMap.get(k).getPublisherVersions()) != null) {
                changeDataIds.add(k);
            }
        });

        if (sessionProcessId != null && !changeDataIds.isEmpty()) {
            // only trigger event when sync from session
            dataChangeEventCenter.onChange(changeDataIds, dataServerConfig.getLocalDataCenter());
        }
        DIFF_LOGGER.info("DiffDigest, slotId={} from {}, update={}, add={}, remove={}, adds={}, removes={}",
                slotId, targetAddress,
                result.getUpdatedDataInfoIds().size(), result.getAddedDataInfoIds().size(), result.getRemovedDataInfoIds().size(),
                result.getAddedDataInfoIds(), result.getRemovedDataInfoIds());
        return result;
    }

    public boolean syncSession(int slotId, String sessionIp, SessionNodeExchanger exchanger,
                               long slotTableEpoch, SyncContinues continues)
                                                                            throws RequestException {
        return sync(slotId, sessionIp, exchanger, slotTableEpoch, sessionIp,
            dataServerConfig.getSlotSyncPublisherDigestMaxNum(), continues);
    }

    public boolean syncSlotLeader(int slotId, String slotLeaderIp, DataNodeExchanger exchanger,
                                  long slotTableEpoch, SyncContinues continues)
                                                                               throws RequestException {
        return sync(slotId, slotLeaderIp, exchanger, slotTableEpoch, null,
            dataServerConfig.getSlotSyncPublisherDigestMaxNum(), continues);
    }

    private Map<String, DatumSummary> pickSummaries(Map<String, DatumSummary> syncSummaries, int n) {
        Map<String, DatumSummary> m = new HashMap<>();
        for (Map.Entry<String, DatumSummary> e : syncSummaries.entrySet()) {
            // at least pick one
            m.put(e.getKey(), e.getValue());
            int versionSize = e.getValue().getPublisherVersions().size();
            if (versionSize == 0) {
                // is empty, budget=1
                versionSize = 1;
            }
            n -= versionSize;
            if (n <= 0) {
                break;
            }
        }
        return m;
    }

}
