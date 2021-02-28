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
package com.alipay.sofa.registry.server.meta.bootstrap;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.remoting.RaftExchanger;
import com.alipay.sofa.registry.util.NamedThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.*;

/**
 * @author shangyu.wh
 * @version $Id: ExecutorManager.java, v 0.1 2018-01-16 15:51 shangyu.wh Exp $
 */
public class ExecutorManager {
    private static final Logger      LOGGER = LoggerFactory.getLogger(ExecutorManager.class);
    private ScheduledExecutorService scheduler;

    private ThreadPoolExecutor       connectMetaServerExecutor;

    private ThreadPoolExecutor       raftClientRefreshExecutor;

    private final MetaServerConfig   metaServerConfig;

    @Autowired
    private RaftExchanger            raftExchanger;

    /**
     * constructor
     *
     * @param metaServerConfig
     */
    public ExecutorManager(MetaServerConfig metaServerConfig) {
        this.metaServerConfig = metaServerConfig;
    }

    public void init() {

        scheduler = new ScheduledThreadPoolExecutor(metaServerConfig.getMetaSchedulerPoolSize(),
            new NamedThreadFactory("MetaScheduler"));

        connectMetaServerExecutor = new ThreadPoolExecutor(
            metaServerConfig.getConnectMetaServerExecutorMinSize(),
            metaServerConfig.getConnectMetaServerExecutorMaxSize(), 300, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(metaServerConfig.getConnectMetaServerExecutorQueueSize()),
            new NamedThreadFactory("MetaScheduler-ConnectMetaServer"));
        connectMetaServerExecutor.allowCoreThreadTimeOut(true);

        raftClientRefreshExecutor = new ThreadPoolExecutor(
            metaServerConfig.getRaftClientRefreshExecutorMinSize(),
            metaServerConfig.getRaftClientRefreshExecutorMaxSize(), 300, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(metaServerConfig.getRaftClientRefreshExecutorQueueSize()),
            new NamedThreadFactory("MetaScheduler-RaftClientRefresh"));
        raftClientRefreshExecutor.allowCoreThreadTimeOut(true);

    }

    public void startScheduler() {

        init();

        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    raftExchanger.refreshRaftClient();
                } catch (Throwable e) {
                    LOGGER.error("failed to refresh raft client", e);
                }
            }
        }, metaServerConfig.getSchedulerCheckNodeListChangePushFirstDelay(),
            metaServerConfig.getSchedulerCheckNodeListChangePushTimeout(), TimeUnit.SECONDS);
    }

    public void stopScheduler() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }

        if (connectMetaServerExecutor != null && !connectMetaServerExecutor.isShutdown()) {
            connectMetaServerExecutor.shutdown();
        }

        if (raftClientRefreshExecutor != null) {
            raftClientRefreshExecutor.isShutdown();
        }
    }
}