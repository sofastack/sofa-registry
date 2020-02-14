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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;

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
import com.alipay.sofa.registry.server.data.event.AfterWorkingProcess;
import com.alipay.sofa.registry.server.data.executor.ExecutorFactory;
import com.alipay.sofa.registry.server.data.node.DataNodeStatus;
import com.alipay.sofa.registry.server.data.remoting.dataserver.GetSyncDataHandler;
import com.alipay.sofa.registry.server.data.remoting.dataserver.SyncDataCallback;
import com.alipay.sofa.registry.server.data.remoting.handler.AbstractClientHandler;
import com.alipay.sofa.registry.server.data.util.LocalServerStatusEnum;
import com.alipay.sofa.registry.server.data.util.ThreadPoolExecutorDataServer;
import com.alipay.sofa.registry.util.NamedThreadFactory;
import com.alipay.sofa.registry.util.ParaCheckUtil;

/**
 *
 * @author qian.lqlq
 * @version $Id: NotifyDataSyncProcessor.java, v 0.1 2018-03-06 20:04 qian.lqlq Exp $
 */
public class NotifyDataSyncHandler extends AbstractClientHandler<NotifyDataSyncRequest> implements
                                                                                       AfterWorkingProcess {

    private static final Logger                                   LOGGER      = LoggerFactory
                                                                                  .getLogger(NotifyDataSyncHandler.class);

    @Autowired
    private DataServerConfig                                      dataServerConfig;

    @Autowired
    private GetSyncDataHandler                                    getSyncDataHandler;

    @Autowired
    private DataChangeEventCenter                                 dataChangeEventCenter;

    private Executor                                              executor    = ExecutorFactory
                                                                                  .newFixedThreadPool(
                                                                                      10,
                                                                                      NotifyDataSyncHandler.class
                                                                                          .getSimpleName());

    private ThreadPoolExecutor                                    notifyExecutor;

    @Autowired
    private DataNodeStatus                                        dataNodeStatus;

    @Autowired
    private DatumCache                                            datumCache;

    private static final BlockingQueue<SyncDataRequestForWorking> noWorkQueue = new LinkedBlockingQueue<>();

    @Override
    public void checkParam(NotifyDataSyncRequest request) throws RuntimeException {
        ParaCheckUtil.checkNotBlank(request.getDataInfoId(), "request.dataInfoId");
    }

    @Override
    public Object doHandle(Channel channel, NotifyDataSyncRequest request) {
        final Connection connection = ((BoltChannel) channel).getConnection();
        if (dataNodeStatus.getStatus() != LocalServerStatusEnum.WORKING) {
            LOGGER.info("receive notifyDataSync request,but data server not working!");
            noWorkQueue.add(new SyncDataRequestForWorking(connection, request));
            return CommonResponse.buildSuccessResponse();
        }
        executorRequest(connection, request);
        return CommonResponse.buildSuccessResponse();
    }

    private void executorRequest(Connection connection, NotifyDataSyncRequest request) {
        executor.execute(() -> {
            fetchSyncData(connection, request);
        });
    }

    protected void fetchSyncData(Connection connection, NotifyDataSyncRequest request) {
        String dataInfoId = request.getDataInfoId();
        String dataCenter = request.getDataCenter();
        Datum datum = datumCache.get(dataCenter, dataInfoId);
        Long version = (datum == null) ? null : datum.getVersion();
        Long requestVersion = request.getVersion();

        if (version == null || requestVersion == 0L || version < requestVersion) {
            LOGGER.info(
                "[NotifyDataSyncProcessor] begin get sync data, currentVersion={},request={}",
                version, request);
            getSyncDataHandler.syncData(new SyncDataCallback(getSyncDataHandler, connection,
                new SyncDataRequest(dataInfoId, dataCenter, version, request.getDataSourceType()),
                dataChangeEventCenter));
        } else {
            LOGGER.info(
                "[NotifyDataSyncHandler] not need to sync data, currentVersion={},request={}",
                version, request);
        }
    }

    @Override
    public void afterWorkingProcess() {
        try {
            while (!noWorkQueue.isEmpty()) {
                SyncDataRequestForWorking event = noWorkQueue.poll(1, TimeUnit.SECONDS);
                if (event != null) {
                    executorRequest(event.getConnection(), event.getRequest());
                }
            }
        } catch (InterruptedException e) {
            LOGGER.error("receive disconnect event after working interrupted!", e);
        }
    }

    @Override
    public int getOrder() {
        return 1;
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
                dataServerConfig.getNotifyDataSyncExecutorMinPoolSize(),
                dataServerConfig.getNotifyDataSyncExecutorMaxPoolSize(),
                dataServerConfig.getNotifyDataSyncExecutorKeepAliveTime(), TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(dataServerConfig.getNotifyDataSyncExecutorQueueSize()),
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

    private class SyncDataRequestForWorking {

        private final Connection            connection;

        private final NotifyDataSyncRequest request;

        public SyncDataRequestForWorking(Connection connection, NotifyDataSyncRequest request) {
            this.connection = connection;
            this.request = request;
        }

        /**
         * Getter method for property <tt>connection</tt>.
         *
         * @return property value of connection
         */
        public Connection getConnection() {
            return connection;
        }

        /**
         * Getter method for property <tt>request</tt>.
         *
         * @return property value of request
         */
        public NotifyDataSyncRequest getRequest() {
            return request;
        }

    }
}