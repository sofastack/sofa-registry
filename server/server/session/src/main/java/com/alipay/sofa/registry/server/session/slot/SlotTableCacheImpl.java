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
package com.alipay.sofa.registry.server.session.slot;

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.metaserver.GetSlotTableRequest;
import com.alipay.sofa.registry.common.model.metaserver.GetSlotTableResult;
import com.alipay.sofa.registry.common.model.slot.*;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.exchange.NodeExchanger;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.node.RaftClientManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

/**
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-11 10:07 yuzhi.lyz Exp $
 */
public final class SlotTableCacheImpl implements SlotTableCache {
    private static final Logger LOGGER       = LoggerFactory.getLogger(SlotTableCacheImpl.class);
    @Autowired
    protected NodeExchanger     metaNodeExchanger;

    @Autowired
    private SessionServerConfig sessionServerConfig;

    @Autowired
    protected RaftClientManager raftClientManager;

    private final SlotFunction  slotFunction = SlotFunctionRegistry.getFunc();
    private volatile SlotTable  slotTable;

    @Override
    public int slotOf(String dataInfoId) {
        return slotFunction.slotOf(dataInfoId);
    }

    @Override
    public Slot getSlot(String dataInfoId) {
        int slotId = slotOf(dataInfoId);
        //        return slotTable.getSlot(slotId);
        // TODO mocks
        return new Slot(1, "10.15.233.13", 1, Collections.emptySet());
    }

    @Override
    public void triggerUpdateSlotTable(long epoch) {
        // TODO
    }

    @Override
    public long getEpoch() {
        final SlotTable table = slotTable;
        return table == null ? -1 : table.getEpoch();
    }

    private GetSlotTableResult sendGetSlotTableRequest(long epoch) {
        try {
            Request<GetSlotTableRequest> request = new Request<GetSlotTableRequest>() {
                @Override
                public GetSlotTableRequest getRequestBody() {
                    // session not need the followers
                    return new GetSlotTableRequest(epoch, null, true);
                }

                @Override
                public URL getRequestUrl() {
                    return new URL(raftClientManager.getLeader().getIp(),
                        sessionServerConfig.getMetaServerPort());
                }
            };

            GenericResponse<GetSlotTableResult> response = (GenericResponse<GetSlotTableResult>) metaNodeExchanger
                .request(request).getResult();

            if (response != null && response.isSuccess()) {
                return response.getData();
            } else {
                LOGGER.error("Get slot table error! No response receive, epoch={}, {}", epoch,
                    response);
                throw new RuntimeException("Get slot table error! No response receive");
            }
        } catch (Throwable e) {
            LOGGER.error("Get slot table error!", e);
            throw new RuntimeException("Get slot table error!", e);
        }
    }

}
