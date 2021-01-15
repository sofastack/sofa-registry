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
package com.alipay.sofa.registry.server.session.scheduler;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
import com.alipay.sofa.registry.task.MetricsableThreadPoolExecutor;
import com.alipay.sofa.registry.util.NamedThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 *
 * @author shangyu.wh
 * @version $Id: ExecutorManager.java, v 0.1 2017-11-28 14:41 shangyu.wh Exp $
 */
public class ExecutorManager {

    private static final Logger               LOGGER                             = LoggerFactory
                                                                                     .getLogger(ExecutorManager.class);

    private final ScheduledThreadPoolExecutor scheduler;

    private final ThreadPoolExecutor          accessDataExecutor;
    private final ThreadPoolExecutor          dataChangeRequestExecutor;
    private final ThreadPoolExecutor          dataSlotSyncRequestExecutor;
    private final ThreadPoolExecutor          connectClientExecutor;
    private final ThreadPoolExecutor          publishDataExecutor;

    @Autowired
    protected MetaServerService               metaServerService;

    private Map<String, ThreadPoolExecutor>   reportExecutors                    = new HashMap<>();

    private static final String               ACCESS_DATA_EXECUTOR               = "AccessDataExecutor";

    private static final String               DATA_CHANGE_REQUEST_EXECUTOR       = "DataChangeRequestExecutor";

    private static final String               DATA_SLOT_MIGRATE_REQUEST_EXECUTOR = "DataSlotMigrateRequestExecutor";

    private static final String               CONNECT_CLIENT_EXECUTOR            = "ConnectClientExecutor";

    private static final String               PUBLISH_DATA_EXECUTOR              = "PublishDataExecutor";

    public ExecutorManager(SessionServerConfig sessionServerConfig) {
        scheduler = new ScheduledThreadPoolExecutor(sessionServerConfig.getSessionSchedulerPoolSize(),
                new NamedThreadFactory("SessionScheduler"));

        accessDataExecutor = reportExecutors.computeIfAbsent(ACCESS_DATA_EXECUTOR,
                k -> new MetricsableThreadPoolExecutor(ACCESS_DATA_EXECUTOR,
                        sessionServerConfig.getAccessDataExecutorMinPoolSize(),
                        sessionServerConfig.getAccessDataExecutorMaxPoolSize(),
                        sessionServerConfig.getAccessDataExecutorKeepAliveTime(), TimeUnit.SECONDS,
                        new ArrayBlockingQueue<>(sessionServerConfig.getAccessDataExecutorQueueSize()),
                        new NamedThreadFactory("AccessExecutor", true), (r, executor) -> {
                    String msg = String
                            .format("Task(%s) %s rejected from %s, just ignore it to let client timeout.", r.getClass(),
                                    r, executor);
                    LOGGER.error(msg);
                }));

        dataChangeRequestExecutor = reportExecutors.computeIfAbsent(DATA_CHANGE_REQUEST_EXECUTOR,
                k -> new MetricsableThreadPoolExecutor(DATA_CHANGE_REQUEST_EXECUTOR,
                        sessionServerConfig.getDataChangeExecutorMinPoolSize(),
                        sessionServerConfig.getDataChangeExecutorMaxPoolSize(),
                        sessionServerConfig.getDataChangeExecutorKeepAliveTime(), TimeUnit.SECONDS,
                        new ArrayBlockingQueue<>(sessionServerConfig.getDataChangeExecutorQueueSize()),
                        new NamedThreadFactory("DataChangeExecutor", true)));

        dataSlotSyncRequestExecutor = reportExecutors.computeIfAbsent(DATA_SLOT_MIGRATE_REQUEST_EXECUTOR,
                k -> new MetricsableThreadPoolExecutor(DATA_SLOT_MIGRATE_REQUEST_EXECUTOR,
                        sessionServerConfig.getSlotSyncWorkerSize(),
                        sessionServerConfig.getSlotSyncWorkerSize(),
                        60, TimeUnit.SECONDS,
                        new ArrayBlockingQueue<>(sessionServerConfig.getSlotSyncMaxBufferSize()),
                        new NamedThreadFactory("SlotSyncExecutor", true)));

        connectClientExecutor = reportExecutors.computeIfAbsent(CONNECT_CLIENT_EXECUTOR,
                k -> new MetricsableThreadPoolExecutor(CONNECT_CLIENT_EXECUTOR,
                        sessionServerConfig.getConnectClientExecutorMinPoolSize(),
                        sessionServerConfig.getConnectClientExecutorMaxPoolSize(), 60L, TimeUnit.SECONDS,
                        new LinkedBlockingQueue(sessionServerConfig.getConnectClientExecutorQueueSize()),
                        new NamedThreadFactory("DisconnectClientExecutor", true)));

        publishDataExecutor = reportExecutors.computeIfAbsent(PUBLISH_DATA_EXECUTOR,
                k -> new MetricsableThreadPoolExecutor(PUBLISH_DATA_EXECUTOR,
                        sessionServerConfig.getPublishDataExecutorMinPoolSize(),
                        sessionServerConfig.getPublishDataExecutorMaxPoolSize(),
                        sessionServerConfig.getPublishDataExecutorKeepAliveTime(), TimeUnit.SECONDS,
                        new ArrayBlockingQueue<>(sessionServerConfig.getPublishDataExecutorQueueSize()),
                        new NamedThreadFactory("PublishExecutor", true)));
    }

    public void startScheduler() {
    }

    public void stopScheduler() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }

        if (accessDataExecutor != null && !accessDataExecutor.isShutdown()) {
            accessDataExecutor.shutdown();
        }

        if (dataChangeRequestExecutor != null && !dataChangeRequestExecutor.isShutdown()) {
            dataChangeRequestExecutor.shutdown();
        }

        if (dataSlotSyncRequestExecutor != null && !dataSlotSyncRequestExecutor.isShutdown()) {
            dataSlotSyncRequestExecutor.shutdown();
        }

        if (connectClientExecutor != null && !connectClientExecutor.isShutdown()) {
            connectClientExecutor.shutdown();
        }

        if (publishDataExecutor != null && !publishDataExecutor.isShutdown()) {
            publishDataExecutor.shutdown();
        }
    }

    public Map<String, ThreadPoolExecutor> getReportExecutors() {
        return reportExecutors;
    }

    public ThreadPoolExecutor getAccessDataExecutor() {
        return accessDataExecutor;
    }

    public ThreadPoolExecutor getDataChangeRequestExecutor() {
        return dataChangeRequestExecutor;
    }

    public ThreadPoolExecutor getDataSlotSyncRequestExecutor() {
        return dataSlotSyncRequestExecutor;
    }

    public ThreadPoolExecutor getConnectClientExecutor() {
        return connectClientExecutor;
    }

    public ThreadPoolExecutor getPublishDataExecutor() {
        return publishDataExecutor;
    }
}