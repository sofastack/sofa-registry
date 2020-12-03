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
import com.alipay.sofa.registry.common.model.dataserver.DatumSummary;
import com.alipay.sofa.registry.common.model.slot.DataSlotDiffPublisherRequest;
import com.alipay.sofa.registry.common.model.slot.DataSlotDiffSyncResult;
import com.alipay.sofa.registry.common.model.slot.DataSlotDiffUtils;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.scheduler.ExecutorManager;
import com.alipay.sofa.registry.server.session.slot.SlotTableCache;
import com.alipay.sofa.registry.server.session.store.DataStore;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.concurrent.Executor;

/**
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-06 15:41 yuzhi.lyz Exp $
 */
public class DataSlotDiffPublisherRequestHandler extends
                                                AbstractServerHandler<DataSlotDiffPublisherRequest> {

    private static final Logger LOGGER = LoggerFactory
                                           .getLogger(DataSlotDiffPublisherRequestHandler.class);

    @Autowired
    private SessionServerConfig sessionServerConfig;

    @Autowired
    private ExecutorManager     executorManager;

    @Autowired
    private DataStore           sessionDataStore;

    @Autowired
    private SlotTableCache      slotTableCache;

    @Override
    public Object reply(Channel channel, DataSlotDiffPublisherRequest request) {
        try {
            slotTableCache.triggerUpdateSlotTable(request.getSlotTableEpoch());
            final int slotId = request.getSlotId();
            DataSlotDiffSyncResult result = calcDiffResult(slotId, request.getDatumSummarys(),
                sessionDataStore.getDataInfoIdPublishers(slotId));
            result.setSlotTableEpoch(slotTableCache.getEpoch());
            result.setSessionProcessId(ServerEnv.PROCESS_ID);
            return new GenericResponse().fillSucceed(result);
        } catch (Throwable e) {
            LOGGER.error("DiffSync publisher Request error for slot {}", request.getSlotId(), e);
            throw new RuntimeException("DiffSync Request error!", e);
        }
    }

    private DataSlotDiffSyncResult calcDiffResult(int targetSlot,
                                                  Map<String, DatumSummary> datumSummarys,
                                                  Map<String, Map<String, Publisher>> existingPublishers) {
        DataSlotDiffSyncResult result = DataSlotDiffUtils.diffPublishersResult(datumSummarys,
            existingPublishers, sessionServerConfig.getSlotSyncPublisherMaxNum());
        DataSlotDiffUtils.logDiffResult(result, targetSlot, LOGGER);
        return result;
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
        return executorManager.getDataSlotSyncRequestExecutor();
    }

    @Override
    public Class interest() {
        return DataSlotDiffPublisherRequest.class;
    }
}
