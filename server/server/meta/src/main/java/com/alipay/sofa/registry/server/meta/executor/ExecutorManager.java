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
package com.alipay.sofa.registry.server.meta.executor;

import java.util.concurrent.*;

import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.remoting.RaftExchanger;
import com.alipay.sofa.registry.task.scheduler.TimedSupervisorTask;
import com.alipay.sofa.registry.util.NamedThreadFactory;

/**
 * @author shangyu.wh
 * @version $Id: ExecutorManager.java, v 0.1 2018-01-16 15:51 shangyu.wh Exp $
 */
public class ExecutorManager {

    private ScheduledExecutorService scheduler;

    private ThreadPoolExecutor       heartbeatCheckExecutor;

    private ThreadPoolExecutor       checkDataChangeExecutor;

    private ThreadPoolExecutor       getOtherDataCenterChangeExecutor;

    private ThreadPoolExecutor       connectMetaServerExecutor;

    private ThreadPoolExecutor       checkNodeListChangePushExecutor;

    private ThreadPoolExecutor       raftClientRefreshExecutor;

    private MetaServerConfig         metaServerConfig;

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

//        heartbeatCheckExecutor = new ThreadPoolExecutor(
//            metaServerConfig.getHeartbeatCheckExecutorMinSize(),
//            metaServerConfig.getHeartbeatCheckExecutorMaxSize(), 300, TimeUnit.SECONDS,
//            new LinkedBlockingQueue<>(metaServerConfig.getHeartbeatCheckExecutorQueueSize()),
//            new NamedThreadFactory("MetaScheduler-HeartbeatCheck"));
//        heartbeatCheckExecutor.allowCoreThreadTimeOut(true);
//
//        checkDataChangeExecutor = new ThreadPoolExecutor(
//            metaServerConfig.getCheckDataChangeExecutorMinSize(),
//            metaServerConfig.getCheckDataChangeExecutorMaxSize(), 300, TimeUnit.SECONDS,
//            new LinkedBlockingQueue<>(metaServerConfig.getCheckDataChangeExecutorQueueSize()),
//            new NamedThreadFactory("MetaScheduler-CheckDataChange"));
//        checkDataChangeExecutor.allowCoreThreadTimeOut(true);
//
//        getOtherDataCenterChangeExecutor = new ThreadPoolExecutor(
//            metaServerConfig.getGetOtherDataCenterChangeExecutorMinSize(),
//            metaServerConfig.getGetOtherDataCenterChangeExecutorMaxSize(), 300, TimeUnit.SECONDS,
//            new LinkedBlockingQueue<>(metaServerConfig
//                .getGetOtherDataCenterChangeExecutorQueueSize()), new NamedThreadFactory(
//                "MetaScheduler-GetOtherDataCenterChange"));
//        getOtherDataCenterChangeExecutor.allowCoreThreadTimeOut(true);

        connectMetaServerExecutor = new ThreadPoolExecutor(
            metaServerConfig.getConnectMetaServerExecutorMinSize(),
            metaServerConfig.getConnectMetaServerExecutorMaxSize(), 300, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(metaServerConfig.getConnectMetaServerExecutorQueueSize()),
            new NamedThreadFactory("MetaScheduler-ConnectMetaServer"));
        connectMetaServerExecutor.allowCoreThreadTimeOut(true);

        checkNodeListChangePushExecutor = new ThreadPoolExecutor(
            metaServerConfig.getCheckNodeListChangePushExecutorMinSize(),
            metaServerConfig.getCheckNodeListChangePushExecutorMaxSize(), 300, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(metaServerConfig.getCheckDataChangeExecutorQueueSize()),
            new NamedThreadFactory("MetaScheduler-CheckNodeListChangePush"));
        checkNodeListChangePushExecutor.allowCoreThreadTimeOut(true);

        raftClientRefreshExecutor = new ThreadPoolExecutor(
            metaServerConfig.getRaftClientRefreshExecutorMinSize(),
            metaServerConfig.getRaftClientRefreshExecutorMaxSize(), 300, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(metaServerConfig.getRaftClientRefreshExecutorQueueSize()),
            new NamedThreadFactory("MetaScheduler-RaftClientRefresh"));
        raftClientRefreshExecutor.allowCoreThreadTimeOut(true);

    }

    public void startScheduler() {

        init();

//        scheduler.schedule(new TimedSupervisorTask("HeartbeatCheck", scheduler, heartbeatCheckExecutor,
//                        metaServerConfig.getSchedulerHeartbeatTimeout(), TimeUnit.SECONDS,
//                        metaServerConfig.getSchedulerHeartbeatExpBackOffBound(), () -> metaServerRegistry.evict()),
//                metaServerConfig.getSchedulerHeartbeatFirstDelay(), TimeUnit.SECONDS);
//
//        scheduler.schedule(
//                new TimedSupervisorTask("GetOtherDataCenterChange", scheduler, getOtherDataCenterChangeExecutor,
//                        metaServerConfig.getSchedulerGetDataChangeTimeout(), TimeUnit.SECONDS,
//                        metaServerConfig.getSchedulerGetDataChangeExpBackOffBound(), () -> {
//                    metaServerRegistry.getOtherDataCenterNodeAndUpdate(NodeType.META);
//                }), metaServerConfig.getSchedulerGetDataChangeFirstDelay(), TimeUnit.SECONDS);

//        scheduler.schedule(new TimedSupervisorTask("ConnectMetaServer", scheduler, connectMetaServerExecutor,
//                        metaServerConfig.getSchedulerConnectMetaServerTimeout(), TimeUnit.SECONDS,
//                        metaServerConfig.getSchedulerConnectMetaServerExpBackOffBound(),
//                        () -> metaClientExchanger.connectServer()), metaServerConfig.getSchedulerConnectMetaServerFirstDelay(),
//                TimeUnit.SECONDS);

        scheduler.schedule(new TimedSupervisorTask("RaftClientRefresh", scheduler, raftClientRefreshExecutor,
                        metaServerConfig.getSchedulerCheckNodeListChangePushTimeout(), TimeUnit.SECONDS,
                        metaServerConfig.getSchedulerCheckNodeListChangePushExpBackOffBound(),
                        () -> raftExchanger.refreshRaftClient()),
                metaServerConfig.getSchedulerCheckNodeListChangePushFirstDelay(), TimeUnit.SECONDS);
   }

    public void stopScheduler() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }

        if (heartbeatCheckExecutor != null && !heartbeatCheckExecutor.isShutdown()) {
            heartbeatCheckExecutor.shutdown();
        }

        if (checkDataChangeExecutor != null && !checkDataChangeExecutor.isShutdown()) {
            checkDataChangeExecutor.shutdown();
        }

        if (connectMetaServerExecutor != null && !connectMetaServerExecutor.isShutdown()) {
            connectMetaServerExecutor.shutdown();
        }

        if (getOtherDataCenterChangeExecutor != null
            && !getOtherDataCenterChangeExecutor.isShutdown()) {
            getOtherDataCenterChangeExecutor.shutdown();
        }

        if (checkNodeListChangePushExecutor != null) {
            checkNodeListChangePushExecutor.isShutdown();
        }

        if (raftClientRefreshExecutor != null) {
            raftClientRefreshExecutor.isShutdown();
        }
    }
}