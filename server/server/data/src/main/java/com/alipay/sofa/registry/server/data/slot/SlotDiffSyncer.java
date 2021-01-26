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
import com.alipay.sofa.registry.server.data.change.DataChangeEventCenter;
import com.alipay.sofa.registry.server.data.lease.SessionLeaseManager;
import com.alipay.sofa.registry.server.data.remoting.DataNodeExchanger;
import com.alipay.sofa.registry.server.data.remoting.SessionNodeExchanger;
import com.alipay.sofa.registry.server.shared.remoting.ClientSideExchanger;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import static com.alipay.sofa.registry.server.data.slot.SlotMetrics.Sync.*;
import java.util.*;

/**
 *
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
        }

        if (result.isEmpty()) {
            DIFF_LOGGER.info("sync slot={} from {}, empty", slotId, targetAddress);
            return result;
        }

        result.getAddedDataInfoIds().forEach(k -> {
            // TODO only support localDataCenter
            datumStorage.createEmptyDatumIfAbsent(k, dataServerConfig.getLocalDataCenter());
        });
        final Set<String> changeDataIds = Sets.newHashSet();
        result.getRemovedDataInfoIds().forEach(k->{
            if (datumStorage.remove(k, sessionProcessId, summarys.get(k).getPublisherVersions()) != null) {
                changeDataIds.add(k);
            }
        });

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
        if (sessionProcessId != null && !changeDataIds.isEmpty()) {
            // only trigger event when sync from session
            dataChangeEventCenter.onChange(changeDataIds, dataServerConfig.getLocalDataCenter());
        }
        DIFF_LOGGER
                .info(
                        "sync slot={} from {}, updatedP {}:{}, addD {}, removedD {}, removedP {}:{}, addD {}, removedD {}",
                        slotId, targetAddress, result.getUpdatedPublishers().size(), result.getUpdatedPublishersCount(),
                        result.getAddedDataInfoIds().size(), result.getRemovedDataInfoIds().size(),
                        result.getRemovedPublishersCount(), result.getRemovedPublishers().size(),
                        result.getAddedDataInfoIds(), result.getRemovedDataInfoIds());
        return result;
    }

    public boolean syncDataInfoIds(int slotId, String targetAddress, ClientSideExchanger exchanger,
                                   long slotTableEpoch, String summaryTargetIp,
                                   SyncContinues continues) {
        if (!continues.continues()) {
            LOGGER.info("syncing dataInfoIds break, slot={} from {}", slotId, targetAddress);
            return true;
        }
        // no need the empty dataInfoId,
        Map<String, DatumSummary> summaryMap = datumStorage.getDatumSummary(slotId,
            summaryTargetIp, false);
        observeSyncSessionId(slotId, summaryMap.size());
        DataSlotDiffDataInfoIdRequest request = new DataSlotDiffDataInfoIdRequest(slotTableEpoch,
            slotId, new HashSet<>(summaryMap.keySet()));
        GenericResponse<DataSlotDiffSyncResult> resp = (GenericResponse<DataSlotDiffSyncResult>) exchanger
            .requestRaw(targetAddress, request).getResult();
        DataSlotDiffSyncResult result = processSyncResp(slotId, resp, targetAddress, summaryMap);
        if (result == null) {
            return false;
        }
        return true;
    }

    public boolean syncPublishers(int slotId, String targetAddress, ClientSideExchanger exchanger, long slotTableEpoch,
                                  String summaryTargetIp, int maxPublishers, SyncContinues continues) {
        // need the empty dataInfoId to add updatePublisher
        Map<String, DatumSummary> summaryMap = datumStorage.getDatumSummary(slotId, summaryTargetIp, true);
        Map<String, DatumSummary> round = pickSummaries(summaryMap, maxPublishers);
        // sync for the existing dataInfoIds.publisher
        while (!summaryMap.isEmpty()) {
            if(!continues.continues()){
                LOGGER.info("syncing publishers break, slot={} from {}", slotId, targetAddress);
                return true;
            }
            // maybe to many publishers, spit round
            observeSyncSessionPub(slotId, DatumSummary.countPublisherSize(round.values()));
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
                round = pickSummaries(summaryMap, maxPublishers);
            }else{
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

    public boolean syncSession(int slotId, String sessionIp, SessionNodeExchanger exchanger,
                               long slotTableEpoch, SyncContinues continues)
                                                                            throws RequestException {
        boolean syncDataInfoIds = syncDataInfoIds(slotId, sessionIp, exchanger, slotTableEpoch,
            sessionIp, continues);
        boolean syncPublishers = syncPublishers(slotId, sessionIp, exchanger, slotTableEpoch,
            sessionIp, dataServerConfig.getSlotSyncPublisherDigestMaxNum(), continues);
        return syncDataInfoIds && syncPublishers;
    }

    public boolean syncSlotLeader(int slotId, String slotLeaderIp, DataNodeExchanger exchanger,
                                  long slotTableEpoch, SyncContinues continues)
                                                                               throws RequestException {
        boolean syncDataInfoIds = syncDataInfoIds(slotId, slotLeaderIp, exchanger, slotTableEpoch,
            null, continues);
        boolean syncPublishers = syncPublishers(slotId, slotLeaderIp, exchanger, slotTableEpoch,
            null, dataServerConfig.getSlotSyncPublisherDigestMaxNum(), continues);
        return syncDataInfoIds && syncPublishers;
    }

    private Map<String, DatumSummary> pickSummaries(Map<String, DatumSummary> syncSummaries, int n) {
        Map<String, DatumSummary> m = new HashMap<>();
        for (Map.Entry<String, DatumSummary> e : syncSummaries.entrySet()) {
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
