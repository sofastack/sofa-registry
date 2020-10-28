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

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Executor;

import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.server.session.cache.Value;
import com.alipay.remoting.util.StringUtils;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.server.session.cache.AppRevisionCacheRegistry;
import com.alipay.sofa.registry.server.session.cache.SessionDatumCacheDecorator;
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
import com.alipay.sofa.registry.server.shared.remoting.AbstractClientHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.Executor;

/**
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

    @Autowired
    private SessionDatumCacheDecorator       sessionDatumCacheDecorator;

    @Autowired
    private AppRevisionCacheRegistry         appRevisionCacheRegistry;

    @Override
    protected NodeType getConnectNodeType() {
        return NodeType.DATA;
    }

    @Override
    public Executor getExecutor() {
        return executorManager.getDataChangeRequestExecutor();
    }

    @Override
    public Object doHandle(Channel channel, DataChangeRequest dataChangeRequest) {
        dataChangeRequest.setDataCenter(dataChangeRequest.getDataCenter());
        dataChangeRequest.setDataInfoId(dataChangeRequest.getDataInfoId());

        final Key key = new Key(KeyType.OBJ, DatumKey.class.getName(), new DatumKey(
            dataChangeRequest.getDataInfoId(), dataChangeRequest.getDataCenter()));
        // TODO check version to invalidate?
        sessionCacheService.invalidate(key);
        if (sessionServerConfig.isStopPushSwitch()) {
            return null;
        }

        try {
            DataInfo dataInfo = DataInfo.valueOf(dataChangeRequest.getDataInfoId());
            refreshMeta(dataChangeRequest.getRevisions());

            if (StringUtils.equals(ValueConstants.SOFA_APP, dataInfo.getDataType())) {

                //dataInfoId is app, get relate interfaces dataInfoId from cache
                Set<String> interfaces = appRevisionCacheRegistry.getInterfaces(dataInfo
                    .getDataId());
                for (String interfaceDataInfoId : interfaces) {
                    DataChangeRequest request = new DataChangeRequest();
                    request.setDataInfoId(interfaceDataInfoId);
                    request.setChangedDataInfoId(dataChangeRequest.getDataInfoId());
                    request.setDataCenter(dataChangeRequest.getDataCenter());
                    request.setVersion(dataChangeRequest.getVersion());
                    fireChangFetch(request);
                }
            } else {
                dataChangeRequest.setChangedDataInfoId(dataChangeRequest.getDataInfoId());
                fireChangFetch(dataChangeRequest);
            }
            EXCHANGE_LOGGER.info(
                "Data version has change,and will fetch to update!Request={},URL={}",
                dataChangeRequest, channel.getRemoteAddress());
        } catch (Exception e) {
            LOGGER.error("DataChange Request error!", e);
            throw new RuntimeException("DataChangeRequest Request error!", e);
        }

        return null;
    }

    private void refreshMeta(Collection<String> revisions) {
        for (String revision : revisions) {
            appRevisionCacheRegistry.getRevision(revision);
        }
    }

    /**
     * @param dataChangeRequest
     */
    private void fireChangFetch(DataChangeRequest dataChangeRequest) {
        boolean result = sessionInterests.checkInterestVersions(dataChangeRequest.getDataCenter(),
            dataChangeRequest.getDataInfoId(), dataChangeRequest.getVersion());

        if (!result) {
            return;
        }
        dataChangeRequestHandlerStrategy.doFireChangFetch(dataChangeRequest);
    }

    @Override
    public Class interest() {
        return DataChangeRequest.class;
    }
}