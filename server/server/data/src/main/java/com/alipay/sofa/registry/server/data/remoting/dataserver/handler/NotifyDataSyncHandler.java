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

import com.alipay.remoting.Connection;
import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.dataserver.NotifyDataSyncRequest;
import com.alipay.sofa.registry.common.model.dataserver.SyncDataRequest;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.bolt.BoltChannel;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumCache;
import com.alipay.sofa.registry.server.data.change.event.DataChangeEventCenter;
import com.alipay.sofa.registry.server.data.executor.ExecutorFactory;
import com.alipay.sofa.registry.server.data.remoting.dataserver.GetSyncDataHandler;
import com.alipay.sofa.registry.server.data.remoting.dataserver.SyncDataCallback;
import com.alipay.sofa.registry.server.data.remoting.handler.AbstractClientHandler;
import com.alipay.sofa.registry.server.data.util.ThreadPoolExecutorDataServer;
import com.alipay.sofa.registry.util.NamedThreadFactory;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author qian.lqlq
 * @version $Id: NotifyDataSyncProcessor.java, v 0.1 2018-03-06 20:04 qian.lqlq Exp $
 */
public class NotifyDataSyncHandler extends AbstractClientHandler<NotifyDataSyncRequest> {

    private static final Logger   LOGGER   = LoggerFactory.getLogger(NotifyDataSyncHandler.class);

    @Autowired
    private DataServerConfig      dataServerBootstrapConfig;

    @Autowired
    private GetSyncDataHandler    getSyncDataHandler;

    @Autowired
    private DataChangeEventCenter dataChangeEventCenter;

    private Executor              executor = ExecutorFactory.newFixedThreadPool(10,
                                               NotifyDataSyncHandler.class.getSimpleName());

    private ThreadPoolExecutor    notifyExecutor;

    @Override
    public void checkParam(NotifyDataSyncRequest request) throws RuntimeException {
        ParaCheckUtil.checkNotBlank(request.getDataInfoId(), "request.dataInfoId");
    }

    @Override
    public Object doHandle(Channel channel, NotifyDataSyncRequest request) {
        final Connection connection = ((BoltChannel) channel).getConnection();
        executor.execute(() -> {
            String dataInfoId = request.getDataInfoId();
            String dataCenter = request.getDataCenter();
            Datum datum = DatumCache.get(dataCenter, dataInfoId);
            Long version = (datum == null) ? null : datum.getVersion();
            Long requestVersion = request.getVersion();
            if (version == null || requestVersion == 0L || version < requestVersion) {
                LOGGER.info(
                        "[NotifyDataSyncProcessor] begin get sync data, currentVersion={},request={}", version,
                        request);
                getSyncDataHandler
                        .syncData(new SyncDataCallback(getSyncDataHandler, connection, new SyncDataRequest(dataInfoId,
                                dataCenter, version, request.getDataSourceType()), dataChangeEventCenter));
            } else {
                LOGGER.info(
                        "[NotifyDataSyncHandler] not need to sync data, version={}", version);
            }
        });
        return CommonResponse.buildSuccessResponse();
    }

    @Override
    public CommonResponse buildFailedResponse(String msg) {
        return CommonResponse.buildFailedResponse(msg);
    }

    @Override
    public Class interest() {
        return NotifyDataSyncRequest.class;
    }

    @Override
    public Executor getExecutor() {
        if (notifyExecutor == null) {
            notifyExecutor = new ThreadPoolExecutorDataServer("NotifyDataSyncProcessorExecutor",
                dataServerBootstrapConfig.getNotifyDataSyncExecutorMinPoolSize(),
                dataServerBootstrapConfig.getNotifyDataSyncExecutorMaxPoolSize(),
                dataServerBootstrapConfig.getNotifyDataSyncExecutorKeepAliveTime(),
                TimeUnit.SECONDS, new ArrayBlockingQueue<>(
                    dataServerBootstrapConfig.getNotifyDataSyncExecutorQueueSize()),
                new NamedThreadFactory("DataServer-NotifyDataSyncProcessor-executor", true));
        }
        return notifyExecutor;
    }

    @Override
    public HandlerType getType() {
        return HandlerType.PROCESSER;
    }

    @Override
    protected Node.NodeType getConnectNodeType() {
        return Node.NodeType.DATA;
    }
}