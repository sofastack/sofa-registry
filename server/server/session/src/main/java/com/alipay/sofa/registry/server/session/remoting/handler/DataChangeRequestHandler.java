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

import java.util.concurrent.Executor;

import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.server.session.cache.Value;
import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.sessionserver.DataChangeRequest;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.cache.CacheService;
import com.alipay.sofa.registry.server.session.cache.DatumKey;
import com.alipay.sofa.registry.server.session.cache.Key;
import com.alipay.sofa.registry.server.session.cache.Key.KeyType;
import com.alipay.sofa.registry.server.session.scheduler.ExecutorManager;
import com.alipay.sofa.registry.server.session.store.Interests;
import com.alipay.sofa.registry.server.session.strategy.DataChangeRequestHandlerStrategy;

/**
 *
 * @author kezhu.wukz
 * @author shangyu.wh
 * @version $Id: DataChangeRequestHandler.java, v 0.1 2017-12-12 15:09 shangyu.wh Exp $
 */
public class DataChangeRequestHandler extends AbstractClientHandler<DataChangeRequest> {

    private static final Logger              LOGGER          = LoggerFactory
                                                                 .getLogger(DataChangeRequestHandler.class);

    private static final Logger              EXCHANGE_LOGGER = LoggerFactory
                                                                 .getLogger("SESSION-EXCHANGE");

    /**
     * store subscribers
     */
    @Autowired
    private Interests                        sessionInterests;

    @Autowired
    private SessionServerConfig              sessionServerConfig;

    @Autowired
    private ExecutorManager                  executorManager;

    @Autowired
    private CacheService                     sessionCacheService;

    @Autowired
    private DataChangeRequestHandlerStrategy dataChangeRequestHandlerStrategy;

    @Override
    public HandlerType getType() {
        return HandlerType.PROCESSER;
    }

    @Override
    protected NodeType getConnectNodeType() {
        return NodeType.DATA;
    }

    @Override
    public Executor getExecutor() {
        return executorManager.getDataChangeRequestExecutor();
    }

    @Override
    public Object reply(Channel channel, DataChangeRequest dataChangeRequest) {
        dataChangeRequest.setDataCenter(dataChangeRequest.getDataCenter());
        dataChangeRequest.setDataInfoId(dataChangeRequest.getDataInfoId());

        final Key key = new Key(KeyType.OBJ, DatumKey.class.getName(), new DatumKey(
            dataChangeRequest.getDataInfoId(), dataChangeRequest.getDataCenter()));
        Value<Datum> value = sessionCacheService.getValueIfPresent(key);
        if (value != null) {
            Datum datum = value.getPayload();
            //update cache when change
            if (datum != null && datum.getVersion() < dataChangeRequest.getVersion()) {
                sessionCacheService.invalidate();
            }
        }

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

    /**
     *
     * @param dataChangeRequest
     */
    private void fireChangFetch(DataChangeRequest dataChangeRequest) {
        dataChangeRequestHandlerStrategy.doFireChangFetch(dataChangeRequest);
    }

    @Override
    public Class interest() {
        return DataChangeRequest.class;
    }
}