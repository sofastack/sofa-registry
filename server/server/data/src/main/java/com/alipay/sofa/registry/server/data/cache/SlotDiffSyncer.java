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
package com.alipay.sofa.registry.server.data.cache;

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.dataserver.DatumSummary;
import com.alipay.sofa.registry.common.model.slot.DataSlotDiffDataInfoIdRequest;
import com.alipay.sofa.registry.common.model.slot.DataSlotDiffPublisherRequest;
import com.alipay.sofa.registry.common.model.slot.DataSlotDiffSyncResult;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.exchange.RequestException;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.remoting.DataNodeExchanger;
import com.alipay.sofa.registry.server.data.remoting.SessionNodeExchanger;
import com.alipay.sofa.registry.server.shared.remoting.ClientExchanger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-20 13:56 yuzhi.lyz Exp $
 */
public final class SlotDiffSyncer {
    private static final Logger           LOGGER = LoggerFactory
            .getLogger(SlotManagerImpl.class);
    private final        DataServerConfig dataServerConfig;

    private final SlotManager.SlotDatumStorageProvider storageProvider;

    SlotDiffSyncer(DataServerConfig dataServerConfig, SlotManager.SlotDatumStorageProvider provider) {
        this.dataServerConfig = dataServerConfig;
        this.storageProvider = provider;
    }

    private DataSlotDiffSyncResult processSyncResp(int slotId,
                                                   GenericResponse<DataSlotDiffSyncResult> resp,
                                                   String targetAddress) {
        if (resp == null || !resp.isSuccess()) {
            LOGGER.error("response failed when sync from {}, slot={}, resp={}", targetAddress,
                    slotId, resp);
            return null;
        }
        DataSlotDiffSyncResult result = resp.getData();
        if (result.isEmpty()) {
            LOGGER.info("sync slot={} from {}, empty", targetAddress, slotId);
            return result;
        }
        // TODO need to renew the session ProcessId
        storageProvider.merge(slotId, result.getUpdatedPublishers(),
                result.getRemovedDataInfoIds(), result.getRemovedPublishers());
        LOGGER
                .info(
                        "sync slot={} from {}, updatedP {}:{}, removedD {}, removedP {}:{}, updatedD {}, removedD {}",
                        slotId, targetAddress, result.getUpdatedPublishers().size(), result
                                .getUpdatedPublishersCount(), result.getRemovedDataInfoIds().size(), result
                                .getRemovedPublishersCount(), result.getRemovedPublishers().size(), result
                                .getUpdatedPublishers().keySet(), result.getRemovedDataInfoIds());
        return result;
    }

    public boolean syncDataInfoIds(int slotId, String targetAddress, ClientExchanger exchanger,
                                   long slotTableEpoch, String summaryTargetIp)
            throws RequestException {
        for (; ; ) {
            Map<String, DatumSummary> summaryMap = storageProvider.getDatumSummary(slotId,
                    summaryTargetIp);
            DataSlotDiffDataInfoIdRequest request = new DataSlotDiffDataInfoIdRequest(
                    slotTableEpoch, slotId, new HashSet<>(summaryMap.keySet()));
            GenericResponse<DataSlotDiffSyncResult> resp = (GenericResponse<DataSlotDiffSyncResult>) exchanger
                    .requestRaw(targetAddress, request).getResult();
            DataSlotDiffSyncResult result = processSyncResp(slotId, resp, targetAddress);
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

    public boolean syncPublishers(int slotId, String targetAddress, ClientExchanger exchanger, long slotTableEpoch,
                                  String summaryTargetIp, int maxPublishers) throws RequestException {
        Map<String, DatumSummary> summaryMap = storageProvider.getDatumSummary(slotId, summaryTargetIp);
        Map<String, DatumSummary> round = pickSummarys(summaryMap, maxPublishers);
        // sync for the existing dataInfoIds.publisher
        while (!summaryMap.isEmpty()) {
            // maybe to many publishers
            DataSlotDiffPublisherRequest request = new DataSlotDiffPublisherRequest(slotTableEpoch, slotId, round);

            GenericResponse<DataSlotDiffSyncResult> resp = (GenericResponse<DataSlotDiffSyncResult>) exchanger
                    .requestRaw(targetAddress, request).getResult();
            DataSlotDiffSyncResult result = processSyncResp(slotId, resp, targetAddress);
            if (result == null) {
                return false;
            }
            if (!result.isHasRemain()) {
                // the sync has finish, enter next round
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
            n -= e.getValue().getPublisherDigests().size();
            if (n <= 0) {
                break;
            }
        }
        return m;
    }
}
