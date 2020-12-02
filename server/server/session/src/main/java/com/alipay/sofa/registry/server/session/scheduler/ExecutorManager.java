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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.exchange.NodeExchanger;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.node.NodeManager;
import com.alipay.sofa.registry.server.session.registry.Registry;
import com.alipay.sofa.registry.task.scheduler.TimedSupervisorTask;
import com.alipay.sofa.registry.timer.AsyncHashedWheelTimer;
import com.alipay.sofa.registry.timer.AsyncHashedWheelTimer.TaskFailedCallback;
import com.alipay.sofa.registry.util.NamedThreadFactory;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 *
 * @author shangyu.wh
 * @version $Id: ExecutorManager.java, v 0.1 2017-11-28 14:41 shangyu.wh Exp $
 */
public class ExecutorManager {

    private static final Logger               LOGGER                                     = LoggerFactory
                                                                                             .getLogger(ExecutorManager.class);

    private final ScheduledThreadPoolExecutor scheduler;

    private final ThreadPoolExecutor          defaultRequestExecutor;
    private final ThreadPoolExecutor          fetchDataExecutor;
    private final ThreadPoolExecutor          standaloneCheckVersionExecutor;
    private final ThreadPoolExecutor          renNewDataExecutor;
    private final ThreadPoolExecutor          getSessionNodeExecutor;
    private final ThreadPoolExecutor          connectMetaExecutor;
    private final ThreadPoolExecutor          connectDataExecutor;

    private final ExecutorService             checkPushExecutor;
    private final ThreadPoolExecutor          accessDataExecutor;
    private final ThreadPoolExecutor          dataChangeRequestExecutor;
    private final ThreadPoolExecutor          pushTaskExecutor;
    private final ThreadPoolExecutor          connectClientExecutor;
    private final ThreadPoolExecutor          publishDataExecutor;
    private final ThreadPoolExecutor          cleanInvalidClientExecutor;

    private final AsyncHashedWheelTimer       pushTaskCheckAsyncHashedWheelTimer;

    private SessionServerConfig               sessionServerConfig;

    @Autowired
    private Registry                          sessionRegistry;

    @Autowired
    private NodeManager                       sessionNodeManager;

    @Autowired
    private NodeManager                       dataNodeManager;

    @Autowired
    private NodeManager                       metaNodeManager;

    @Autowired
    protected NodeExchanger                   metaNodeExchanger;

    @Autowired
    private NodeExchanger                     dataNodeExchanger;

    private static final String               DEFAULT_REQUEST_EXECUTOR                   = "DefaultRequestExecutor";

    private static final String               PUSH_TASK_EXECUTOR                         = "PushTaskExecutor";

    private static final String               ACCESS_DATA_EXECUTOR                       = "AccessDataExecutor";

    private static final String               DATA_CHANGE_REQUEST_EXECUTOR               = "DataChangeRequestExecutor";

    private static final String               USER_DATA_ELEMENT_PUSH_TASK_CHECK_EXECUTOR = "UserDataElementPushCheckExecutor";

    private static final String               PUSH_TASK_CLOSURE_CHECK_EXECUTOR           = "PushTaskClosureCheckExecutor";

    private static final String               CONNECT_CLIENT_EXECUTOR                    = "ConnectClientExecutor";

    private static final String               PUBLISH_DATA_EXECUTOR                      = "PublishDataExecutor";

    public ExecutorManager(SessionServerConfig sessionServerConfig) {

        this.sessionServerConfig = sessionServerConfig;

        scheduler = new ScheduledThreadPoolExecutor(
            sessionServerConfig.getSessionSchedulerPoolSize(),
            new NamedThreadFactory("SessionScheduler"));

        fetchDataExecutor = new ThreadPoolExecutor(1, 2/*CONFIG*/, 0, TimeUnit.SECONDS,
            new SynchronousQueue<>(), new NamedThreadFactory("SessionScheduler-fetchData"));

        renNewDataExecutor = new ThreadPoolExecutor(1, 2/*CONFIG*/, 0, TimeUnit.SECONDS,
            new SynchronousQueue<>(), new NamedThreadFactory("SessionScheduler-renewData"));

        getSessionNodeExecutor = new ThreadPoolExecutor(1, 2/*CONFIG*/, 0, TimeUnit.SECONDS,
            new SynchronousQueue<>(), new NamedThreadFactory("SessionScheduler-getSessionNode"));

        standaloneCheckVersionExecutor = new ThreadPoolExecutor(1, 2/*CONFIG*/, 0, TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            new NamedThreadFactory("SessionScheduler-standaloneCheckVersion"));

        connectMetaExecutor = new ThreadPoolExecutor(1, 2/*CONFIG*/, 0, TimeUnit.SECONDS,
            new SynchronousQueue<>(), new NamedThreadFactory("SessionScheduler-connectMetaServer"));

        connectDataExecutor = new ThreadPoolExecutor(1, 2/*CONFIG*/, 0, TimeUnit.SECONDS,
            new SynchronousQueue<>(), new NamedThreadFactory("SessionScheduler-connectDataServer"));

        cleanInvalidClientExecutor = new ThreadPoolExecutor(1, 2/*CONFIG*/, 0, TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            new NamedThreadFactory("SessionScheduler-cleanInvalidClient"));

        defaultRequestExecutor = new SessionMetricsThreadPoolExecutor(DEFAULT_REQUEST_EXECUTOR,
            sessionServerConfig.getDefaultRequestExecutorMinPoolSize(),
            sessionServerConfig.getDefaultRequestExecutorMaxPoolSize(),
            sessionServerConfig.getDefaultRequestExecutorKeepAliveTime(), TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(sessionServerConfig.getDefaultRequestExecutorQueueSize()),
            new NamedThreadFactory(DEFAULT_REQUEST_EXECUTOR, true));

        accessDataExecutor = new SessionMetricsThreadPoolExecutor(ACCESS_DATA_EXECUTOR,
            sessionServerConfig.getAccessDataExecutorMinPoolSize(),
            sessionServerConfig.getAccessDataExecutorMaxPoolSize(),
            sessionServerConfig.getAccessDataExecutorKeepAliveTime(), TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(sessionServerConfig.getAccessDataExecutorQueueSize()),
            new NamedThreadFactory("AccessData-executor", true), (r, executor) -> {
                String msg = String.format(
                    "Task(%s) %s rejected from %s, just ignore it to let client timeout.",
                    r.getClass(), r, executor);
                LOGGER.error(msg);
            });

        pushTaskExecutor = new SessionMetricsThreadPoolExecutor(PUSH_TASK_EXECUTOR,
            sessionServerConfig.getPushTaskExecutorMinPoolSize(),
            sessionServerConfig.getPushTaskExecutorMaxPoolSize(),
            sessionServerConfig.getPushTaskExecutorKeepAliveTime(), TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(sessionServerConfig.getPushTaskExecutorQueueSize()),
            new NamedThreadFactory("PushTask-executor", true),
            new ThreadPoolExecutor.AbortPolicy());

        dataChangeRequestExecutor = new SessionMetricsThreadPoolExecutor(
            DATA_CHANGE_REQUEST_EXECUTOR, sessionServerConfig.getDataChangeExecutorMinPoolSize(),
            sessionServerConfig.getDataChangeExecutorMaxPoolSize(),
            sessionServerConfig.getDataChangeExecutorKeepAliveTime(), TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(sessionServerConfig.getDataChangeExecutorQueueSize()),
            new NamedThreadFactory("DataChangeRequestHandler-executor", true));

        checkPushExecutor = new SessionMetricsThreadPoolExecutor(
            USER_DATA_ELEMENT_PUSH_TASK_CHECK_EXECUTOR, 100, 600, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue(150000),
            new NamedThreadFactory("UserDataElementPushCheck-executor", true));

        connectClientExecutor = new SessionMetricsThreadPoolExecutor(CONNECT_CLIENT_EXECUTOR,
            sessionServerConfig.getConnectClientExecutorMinPoolSize(),
            sessionServerConfig.getConnectClientExecutorMaxPoolSize(), 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue(sessionServerConfig.getConnectClientExecutorQueueSize()),
            new NamedThreadFactory("DisconnectClientExecutor", true));

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
        publishDataExecutor = new SessionMetricsThreadPoolExecutor(PUBLISH_DATA_EXECUTOR,
            sessionServerConfig.getPublishDataExecutorMinPoolSize(),
            sessionServerConfig.getPublishDataExecutorMaxPoolSize(),
            sessionServerConfig.getPublishDataExecutorKeepAliveTime(), TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(sessionServerConfig.getPublishDataExecutorQueueSize()),
            new NamedThreadFactory("PublishData-executor", true));
    }

    public void startScheduler() {
        scheduler.schedule(
            new TimedSupervisorTask("FetchData", scheduler, fetchDataExecutor,
                sessionServerConfig.getSchedulerFetchDataTimeout(), TimeUnit.MINUTES,
                sessionServerConfig.getSchedulerFetchDataExpBackOffBound(),
                () -> sessionRegistry.fetchChangData()),
            sessionServerConfig.getSchedulerFetchDataFirstDelay(), TimeUnit.SECONDS);

        scheduler.schedule(
            new TimedSupervisorTask("RenewData", scheduler, renNewDataExecutor,
                sessionServerConfig.getSchedulerHeartbeatTimeout(), TimeUnit.SECONDS,
                sessionServerConfig.getSchedulerHeartbeatExpBackOffBound(),
                () -> sessionNodeManager.renewNode()),
            sessionServerConfig.getSchedulerHeartbeatFirstDelay(), TimeUnit.SECONDS);

        scheduler
            .schedule(new TimedSupervisorTask("GetSessionNode", scheduler, getSessionNodeExecutor,
                sessionServerConfig.getSchedulerGetSessionNodeTimeout(), TimeUnit.SECONDS,
                sessionServerConfig.getSchedulerGetSessionNodeExpBackOffBound(), () -> {
                    sessionNodeManager.getAllDataCenterNodes();
                    dataNodeManager.getAllDataCenterNodes();
                    metaNodeManager.getAllDataCenterNodes();
                }), sessionServerConfig.getSchedulerGetSessionNodeFirstDelay(), TimeUnit.SECONDS);

        scheduler.schedule(
            new TimedSupervisorTask("ConnectMetaServer", scheduler, connectMetaExecutor,
                sessionServerConfig.getSchedulerConnectMetaTimeout(), TimeUnit.SECONDS,
                sessionServerConfig.getSchedulerConnectMetaExpBackOffBound(),
                () -> metaNodeExchanger.connectServer()),
            sessionServerConfig.getSchedulerConnectMetaFirstDelay(), TimeUnit.SECONDS);

        scheduler.schedule(
            new TimedSupervisorTask("ConnectDataServer", scheduler, connectDataExecutor,
                sessionServerConfig.getSchedulerConnectDataTimeout(), TimeUnit.SECONDS,
                sessionServerConfig.getSchedulerConnectDataExpBackOffBound(),
                () -> dataNodeExchanger.connectServer()),
            sessionServerConfig.getSchedulerConnectDataFirstDelay(), TimeUnit.SECONDS);

        scheduler.schedule(
            new TimedSupervisorTask("CleanInvalidClient", scheduler, cleanInvalidClientExecutor,
                sessionServerConfig.getSchedulerCleanInvalidClientTimeOut(), TimeUnit.MINUTES,
                sessionServerConfig.getSchedulerCleanInvalidClientBackOffBound(),
                () -> sessionRegistry.cleanClientConnect()),
            sessionServerConfig.getSchedulerCleanInvalidClientFirstDelay(), TimeUnit.MINUTES);
    }

    public void stopScheduler() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }

        if (standaloneCheckVersionExecutor != null && !standaloneCheckVersionExecutor.isShutdown()) {
            standaloneCheckVersionExecutor.shutdown();
        }

        if (renNewDataExecutor != null && !renNewDataExecutor.isShutdown()) {
            renNewDataExecutor.shutdown();
        }
        if (fetchDataExecutor != null && !fetchDataExecutor.isShutdown()) {
            fetchDataExecutor.shutdown();
        }

        if (getSessionNodeExecutor != null && !getSessionNodeExecutor.isShutdown()) {
            getSessionNodeExecutor.shutdown();
        }

        if (connectMetaExecutor != null && !connectMetaExecutor.isShutdown()) {
            connectMetaExecutor.shutdown();
        }

        if (connectDataExecutor != null && !connectDataExecutor.isShutdown()) {
            connectDataExecutor.shutdown();
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

        if (connectClientExecutor != null && !connectClientExecutor.isShutdown()) {
            connectClientExecutor.shutdown();
        }

        if (publishDataExecutor != null && !publishDataExecutor.isShutdown()) {
            publishDataExecutor.shutdown();
        }
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

    public ThreadPoolExecutor getConnectClientExecutor() {
        return connectClientExecutor;
    }

    public AsyncHashedWheelTimer getPushTaskCheckAsyncHashedWheelTimer() {
        return pushTaskCheckAsyncHashedWheelTimer;
    }

    public ThreadPoolExecutor getPublishDataExecutor() {
        return publishDataExecutor;
    }

    public ThreadPoolExecutor getDefaultRequestExecutor() {
        return defaultRequestExecutor;
    }
}