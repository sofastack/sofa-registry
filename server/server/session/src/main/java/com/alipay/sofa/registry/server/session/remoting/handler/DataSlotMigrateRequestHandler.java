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
package com.alipay.sofa.registry.server.session.remoting.handler;

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.PublisherDigestUtil;
import com.alipay.sofa.registry.common.model.dataserver.DatumSummary;
import com.alipay.sofa.registry.common.model.sessionserver.DataSlotMigrateRequest;
import com.alipay.sofa.registry.common.model.sessionserver.DataSlotMigrateResult;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.scheduler.ExecutorManager;
import com.alipay.sofa.registry.server.session.store.DataStore;
import com.alipay.sofa.registry.server.session.store.SlotTableCache;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.Executor;

/**
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-06 15:41 yuzhi.lyz Exp $
 */
public class DataSlotMigrateRequestHandler extends AbstractClientHandler {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(DataSlotMigrateRequestHandler.class);

    @Autowired
    private SessionServerConfig sessionServerConfig;

    @Autowired
    private ExecutorManager executorManager;

    @Autowired
    private DataStore sessionDataStore;

    @Autowired
    private SlotTableCache slotTableCache;

    @Override
    public Object reply(Channel channel, Object message) {
        DataSlotMigrateRequest dataChangeRequest = (DataSlotMigrateRequest) message;
        try {
            slotTableCache.triggerUpdateSlotTable(dataChangeRequest.getSlotTableEpoch());
            DataSlotMigrateResult result = calcMigrateResult(dataChangeRequest.getSlotId(),
                    dataChangeRequest.getDatumSummarys(), sessionDataStore.getDataInfoIdPublishers());
            result.setSlotTableEpoch(slotTableCache.getEpoch());
            return new GenericResponse().fillSucceed(result);
        } catch (Throwable e) {
            LOGGER.error("Migrate Request error for slot {}", dataChangeRequest.getSlotId(), e);
            throw new RuntimeException("Migrate Request error!", e);
        }
    }

    private DataSlotMigrateResult calcMigrateResult(int targetSlot, Map<String, DatumSummary> datumSummarys,
                                                    Map<String, Map<String, Publisher>> existingPublishers) {
        Map<String, List<Publisher>> updateds = new HashMap<>(existingPublishers.size());
        for (Map.Entry<String, Map<String, Publisher>> e : existingPublishers.entrySet()) {
            final String dataInfoId = e.getKey();
            final long slot = slotTableCache.slotOf(dataInfoId);
            if (slot != targetSlot) {
                continue;
            }
            final DatumSummary summary = datumSummarys.get(dataInfoId);
            if (summary == null) {
                updateds.put(dataInfoId, new ArrayList<>(e.getValue().values()));
                LOGGER.info("Migrating, add new dataInfoId for slot={}, {}", targetSlot, dataInfoId);
                continue;
            }
            List<Publisher> publishers = new ArrayList<>(e.getValue().size());
            updateds.put(dataInfoId, publishers);
            Map<String, Long> digests = summary.getPublisherDigests();
            for (Map.Entry<String, Publisher> p : e.getValue().entrySet()) {
                final String registerId = p.getKey();
                if (!digests.containsKey(registerId)) {
                    publishers.add(p.getValue());
                    LOGGER.info("Migrating, add new registerId for slot={}, dataInfoId={}, {}", targetSlot, dataInfoId,
                            registerId);
                    continue;
                }
                // compare digest
                final long digest = PublisherDigestUtil.getDigestValue(p.getValue());
                if (digest == digests.get(registerId)) {
                    // the same
                    continue;
                }
                publishers.add(p.getValue());
                LOGGER.info("Migrating, add updated registerId for slot={}, dataInfoId={}, {}", targetSlot, dataInfoId,
                        registerId);
            }
        }

        // find the removed publisher
        Map<String, List<String>> removeds = new HashMap<>();
        for (Map.Entry<String, DatumSummary> summarys : datumSummarys.entrySet()) {
            final String dataInfoId = summarys.getKey();
            Map<String, Publisher> publisherMap = existingPublishers.get(dataInfoId);
            if (publisherMap == null) {
                // empty list means remove all
                removeds.put(dataInfoId, Collections.emptyList());
                LOGGER.info("Migrating, remove dataInfoId for slot={}, {}", targetSlot, dataInfoId);
                continue;
            }
            Set<String> registerIds = summarys.getValue().getPublisherDigests().keySet();
            for (String registerId : registerIds) {
                if (!publisherMap.containsKey(registerId)) {
                    List<String> list = removeds.computeIfAbsent(registerId, k -> new ArrayList<>());
                    list.add(registerId);
                }
            }
            LOGGER.info("Migrating, remove registerId for slot={}, dataInfoId={}, {}", targetSlot, dataInfoId,
                    removeds.get(dataInfoId));
        }
        return new DataSlotMigrateResult(updateds, removeds);
    }

    @Override
    public HandlerType getType() {
        return HandlerType.PROCESSER;
    }

    @Override
    protected Node.NodeType getConnectNodeType() {
        return Node.NodeType.DATA;
    }

    @Override
    public Executor getExecutor() {
        return executorManager.getDataSlotMigrateRequestExecutor();
    }

    @Override
    public Class interest() {
        return DataSlotMigrateRequest.class;
    }
}
