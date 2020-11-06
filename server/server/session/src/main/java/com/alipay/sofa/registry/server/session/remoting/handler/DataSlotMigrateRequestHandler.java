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

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.sessionserver.DataChangeRequest;
import com.alipay.sofa.registry.common.model.sessionserver.DataSlotMigrateRequest;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.cache.CacheService;
import com.alipay.sofa.registry.server.session.cache.DatumKey;
import com.alipay.sofa.registry.server.session.cache.Key;
import com.alipay.sofa.registry.server.session.registry.Registry;
import com.alipay.sofa.registry.server.session.scheduler.ExecutorManager;
import com.alipay.sofa.registry.server.session.store.DataStore;
import com.alipay.sofa.registry.server.session.store.SlotSessionDataStore;
import org.springframework.beans.factory.annotation.Autowired;

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
    private SlotSessionDataStore sessionDataStore;

    @Override
    public Object reply(Channel channel, Object message) {
        DataSlotMigrateRequest dataChangeRequest = (DataSlotMigrateRequest) message;
        final
        dataChangeRequest.setDataCenter(dataChangeRequest.getDataCenter());
        dataChangeRequest.setDataInfoId(dataChangeRequest.getDataInfoId());

        //update cache when change
        sessionCacheService.invalidate(new Key(Key.KeyType.OBJ, DatumKey.class.getName(), new DatumKey(
                dataChangeRequest.getDataInfoId(), dataChangeRequest.getDataCenter())));

        if (sessionServerConfig.isStopPushSwitch()) {
            return null;
        }

        try {
            boolean result = sessionInterests.checkInterestVersions(
                    dataChangeRequest.getDataCenter(), dataChangeRequest.getDataInfoId(),
                    dataChangeRequest.getVersion());

            if (!result) {
                return null;
            }

            EXCHANGE_LOGGER.info(
                    "Data version has change,and will fetch to update!Request={},URL={}",
                    dataChangeRequest, channel.getRemoteAddress());

            fireChangFetch(dataChangeRequest);

        } catch (Exception e) {
            LOGGER.error("DataChange Request error!", e);
            throw new RuntimeException("DataChangeRequest Request error!", e);
        }

        return null;
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
