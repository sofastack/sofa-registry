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
import com.alipay.sofa.registry.metrics.TaskMetrics;
import com.alipay.sofa.registry.remoting.exchange.NodeExchanger;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.registry.Registry;
import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
import com.alipay.sofa.registry.task.MetricsableThreadPoolExecutor;
import com.alipay.sofa.registry.timer.AsyncHashedWheelTimer;
import com.alipay.sofa.registry.timer.AsyncHashedWheelTimer.TaskFailedCallback;
import com.alipay.sofa.registry.util.NamedThreadFactory;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
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

    private static final Logger               LOGGER                                     = LoggerFactory
                                                                                             .getLogger(ExecutorManager.class);

    private final ScheduledThreadPoolExecutor scheduler;

    private final ThreadPoolExecutor          fetchDataExecutor;

    private final ExecutorService             checkPushExecutor;
    private final ThreadPoolExecutor          accessDataExecutor;
    private final ThreadPoolExecutor          dataChangeRequestExecutor;
    private final ThreadPoolExecutor          dataSlotSyncRequestExecutor;
    private final ThreadPoolExecutor          pushTaskExecutor;
    private final ThreadPoolExecutor          connectClientExecutor;
    private final ThreadPoolExecutor          publishDataExecutor;

    private final AsyncHashedWheelTimer       pushTaskCheckAsyncHashedWheelTimer;

    private SessionServerConfig               sessionServerConfig;

    @Autowired
    private Registry                          sessionRegistry;

    @Autowired
    protected MetaServerService               metaServerService;

    @Autowired
    private NodeExchanger                     dataNodeExchanger;

    private Map<String, ThreadPoolExecutor>   reportExecutors                            = new HashMap<>();

    private static final String               PUSH_TASK_EXECUTOR                         = "PushTaskExecutor";

    private static final String               ACCESS_DATA_EXECUTOR                       = "AccessDataExecutor";

    private static final String               DATA_CHANGE_REQUEST_EXECUTOR               = "DataChangeRequestExecutor";

    private static final String               DATA_SLOT_MIGRATE_REQUEST_EXECUTOR         = "DataSlotMigrateRequestExecutor";

    private static final String               USER_DATA_ELEMENT_PUSH_TASK_CHECK_EXECUTOR = "UserDataElementPushCheckExecutor";

    private static final String               PUSH_TASK_CLOSURE_CHECK_EXECUTOR           = "PushTaskClosureCheckExecutor";

    private static final String               CONNECT_CLIENT_EXECUTOR                    = "ConnectClientExecutor";

    private static final String               PUBLISH_DATA_EXECUTOR                      = "PublishDataExecutor";

    public ExecutorManager(SessionServerConfig sessionServerConfig) {

        this.sessionServerConfig = sessionServerConfig;

        scheduler = new ScheduledThreadPoolExecutor(sessionServerConfig.getSessionSchedulerPoolSize(),
                new NamedThreadFactory("SessionScheduler"));

        fetchDataExecutor = new ThreadPoolExecutor(1, 2/*CONFIG*/, 0, TimeUnit.SECONDS, new SynchronousQueue<>(),
                new NamedThreadFactory("SessionScheduler-fetchData"));

        accessDataExecutor = reportExecutors.computeIfAbsent(ACCESS_DATA_EXECUTOR,
                k -> new MetricsableThreadPoolExecutor(ACCESS_DATA_EXECUTOR,
                        sessionServerConfig.getAccessDataExecutorMinPoolSize(),
                        sessionServerConfig.getAccessDataExecutorMaxPoolSize(),
                        sessionServerConfig.getAccessDataExecutorKeepAliveTime(), TimeUnit.SECONDS,
                        new ArrayBlockingQueue<>(sessionServerConfig.getAccessDataExecutorQueueSize()),
                        new NamedThreadFactory("AccessData-executor", true), (r, executor) -> {
                    String msg = String
                            .format("Task(%s) %s rejected from %s, just ignore it to let client timeout.", r.getClass(),
                                    r, executor);
                    LOGGER.error(msg);
                }));

        pushTaskExecutor = reportExecutors.computeIfAbsent(PUSH_TASK_EXECUTOR,
                k -> new ThreadPoolExecutor(sessionServerConfig.getPushTaskExecutorMinPoolSize(),
                        sessionServerConfig.getPushTaskExecutorMaxPoolSize(),
                        sessionServerConfig.getPushTaskExecutorKeepAliveTime(), TimeUnit.SECONDS,
                        new LinkedBlockingQueue<>(sessionServerConfig.getPushTaskExecutorQueueSize()),
                        new NamedThreadFactory("PushTask-executor", true)));

        TaskMetrics.getInstance().registerThreadExecutor(PUSH_TASK_EXECUTOR, pushTaskExecutor);

        dataChangeRequestExecutor = reportExecutors.computeIfAbsent(DATA_CHANGE_REQUEST_EXECUTOR,
                k -> new MetricsableThreadPoolExecutor(DATA_CHANGE_REQUEST_EXECUTOR,
                        sessionServerConfig.getDataChangeExecutorMinPoolSize(),
                        sessionServerConfig.getDataChangeExecutorMaxPoolSize(),
                        sessionServerConfig.getDataChangeExecutorKeepAliveTime(), TimeUnit.SECONDS,
                        new ArrayBlockingQueue<>(100000),
                        new NamedThreadFactory("DataChangeRequestHandler-executor", true)));

        dataSlotSyncRequestExecutor = reportExecutors.computeIfAbsent(DATA_SLOT_MIGRATE_REQUEST_EXECUTOR,
                k -> new MetricsableThreadPoolExecutor(DATA_SLOT_MIGRATE_REQUEST_EXECUTOR,
                        12,
                        24,
                        60, TimeUnit.SECONDS,
                        new ArrayBlockingQueue<>(sessionServerConfig.getDataChangeExecutorQueueSize()),
                        new NamedThreadFactory("DataSlotSyncRequestHandler-executor", true)));

        checkPushExecutor = reportExecutors.computeIfAbsent(USER_DATA_ELEMENT_PUSH_TASK_CHECK_EXECUTOR,
                k -> new MetricsableThreadPoolExecutor(USER_DATA_ELEMENT_PUSH_TASK_CHECK_EXECUTOR, 100, 600, 60L,
                        TimeUnit.SECONDS, new LinkedBlockingQueue(150000),
                        new NamedThreadFactory("UserDataElementPushCheck-executor", true)));

        connectClientExecutor = reportExecutors.computeIfAbsent(CONNECT_CLIENT_EXECUTOR,
                k -> new MetricsableThreadPoolExecutor(CONNECT_CLIENT_EXECUTOR,
                        sessionServerConfig.getConnectClientExecutorMinPoolSize(),
                        sessionServerConfig.getConnectClientExecutorMaxPoolSize(), 60L, TimeUnit.SECONDS,
                        new LinkedBlockingQueue(sessionServerConfig.getConnectClientExecutorQueueSize()),
                        new NamedThreadFactory("DisconnectClientExecutor", true)));

        pushTaskCheckAsyncHashedWheelTimer = new AsyncHashedWheelTimer(
                new NamedThreadFactory("PushTaskConfirmCheck-executor", true),
                sessionServerConfig.getPushTaskConfirmCheckWheelTicksDuration(), TimeUnit.MILLISECONDS,
                sessionServerConfig.getPushTaskConfirmCheckWheelTicksSize(),
                sessionServerConfig.getPushTaskConfirmCheckExecutorThreadSize(),
                sessionServerConfig.getPushTaskConfirmCheckExecutorQueueSize(),
                new ThreadFactoryBuilder().setNameFormat("PushTaskConfirmCheck-executor-%d").build(),
                new TaskFailedCallback() {
                    @Override
                    public void executionRejected(Throwable e) {
                        LOGGER.error("executionRejected: " + e.getMessage(), e);
                    }

                    @Override
                    public void executionFailed(Throwable e) {
                        LOGGER.error("executionFailed: " + e.getMessage(), e);
                    }
                });
        publishDataExecutor = reportExecutors.computeIfAbsent(PUBLISH_DATA_EXECUTOR,
                k -> new MetricsableThreadPoolExecutor(PUBLISH_DATA_EXECUTOR,
                        sessionServerConfig.getPublishDataExecutorMinPoolSize(),
                        sessionServerConfig.getPublishDataExecutorMaxPoolSize(),
                        sessionServerConfig.getPublishDataExecutorKeepAliveTime(), TimeUnit.SECONDS,
                        new ArrayBlockingQueue<>(sessionServerConfig.getPublishDataExecutorQueueSize()),
                        new NamedThreadFactory("PublishData-executor", true)));
    }

    public void startScheduler() {
    }

    public void stopScheduler() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }

        if (fetchDataExecutor != null && !fetchDataExecutor.isShutdown()) {
            fetchDataExecutor.shutdown();
        }

        if (accessDataExecutor != null && !accessDataExecutor.isShutdown()) {
            accessDataExecutor.shutdown();
        }

        if (pushTaskExecutor != null && !pushTaskExecutor.isShutdown()) {
            pushTaskExecutor.shutdown();
        }

        if (checkPushExecutor != null && !checkPushExecutor.isShutdown()) {
            checkPushExecutor.shutdown();
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

    public ThreadPoolExecutor getPushTaskExecutor() {
        return pushTaskExecutor;
    }

    public ExecutorService getCheckPushExecutor() {
        return checkPushExecutor;
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

    public AsyncHashedWheelTimer getPushTaskCheckAsyncHashedWheelTimer() {
        return pushTaskCheckAsyncHashedWheelTimer;
    }

    public ThreadPoolExecutor getPublishDataExecutor() {
        return publishDataExecutor;
    }
}