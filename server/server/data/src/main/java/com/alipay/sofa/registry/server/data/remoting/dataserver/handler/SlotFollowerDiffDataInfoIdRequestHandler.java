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
package com.alipay.sofa.registry.server.data.remoting.dataserver.handler;

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.slot.DataSlotDiffDataInfoIdRequest;
import com.alipay.sofa.registry.common.model.slot.DataSlotDiffSyncResult;
import com.alipay.sofa.registry.common.model.slot.DataSlotDiffUtils;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumStorage;
import com.alipay.sofa.registry.server.data.slot.SlotManager;
import com.alipay.sofa.registry.server.shared.remoting.AbstractServerHandler;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-06 15:41 yuzhi.lyz Exp $
 */
public class SlotFollowerDiffDataInfoIdRequestHandler
                                                     extends
                                                     AbstractServerHandler<DataSlotDiffDataInfoIdRequest> {
    private static final Logger LOGGER = LoggerFactory
                                           .getLogger(SlotFollowerDiffDataInfoIdRequestHandler.class);

    @Autowired
    private ThreadPoolExecutor  slotSyncRequestProcessorExecutor;

    @Autowired
    private DataServerConfig    dataServerConfig;

    @Autowired
    private DatumStorage        localDatumStorage;

    @Autowired
    private SlotManager         slotManager;

    @Override
    public Object doHandle(Channel channel, DataSlotDiffDataInfoIdRequest request) {
        try {
            slotManager.triggerUpdateSlotTable(request.getSlotTableEpoch());
            final int slotId = request.getSlotId();
            if (!slotManager.isLeader(slotId)) {
                LOGGER.warn("not leader of {}", slotId);
                return new GenericResponse().fillFailed("not leader of " + slotId);
            }
            DataSlotDiffSyncResult result = calcDiffResult(slotId, request.getAllDataInfoIds(),
                localDatumStorage.getPublishers(request.getSlotId()));
            result.setSlotTableEpoch(slotManager.getSlotTableEpoch());
            return new GenericResponse().fillSucceed(result);
        } catch (Throwable e) {
            LOGGER.error("DiffSync dataInfoIds Request error for slot {}", request.getSlotId(), e);
            throw new RuntimeException("DiffSync Request error!", e);
        }
    }

    private DataSlotDiffSyncResult calcDiffResult(int targetSlot,
                                                  Set<String> dataInfoIds,
                                                  Map<String, Map<String, Publisher>> existingPublishers) {
        DataSlotDiffSyncResult result = DataSlotDiffUtils.diffDataInfoIdsResult(dataInfoIds,
            existingPublishers, dataServerConfig.getSlotSyncPublisherMaxNum());
        DataSlotDiffUtils.logDiffResult(result, targetSlot, LOGGER);
        return result;
    }

    @Override
    protected Node.NodeType getConnectNodeType() {
        return Node.NodeType.DATA;
    }

    @Override
    public Class interest() {
        return DataSlotDiffDataInfoIdRequest.class;
    }

    @Override
    public void checkParam(DataSlotDiffDataInfoIdRequest request) throws RuntimeException {
        ParaCheckUtil.checkNonNegative(request.getSlotId(), "request.slotId");
    }

    @Override
    public Object buildFailedResponse(String msg) {
        return new GenericResponse().fillFailed(msg);
    }

    @Override
    public Executor getExecutor() {
        return slotSyncRequestProcessorExecutor;
    }
}
