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

import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.server.meta.bootstrap.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.registry.Registry;
import com.alipay.sofa.registry.server.meta.remoting.MetaClientExchanger;
import com.alipay.sofa.registry.server.meta.remoting.RaftExchanger;
import com.alipay.sofa.registry.task.scheduler.TimedSupervisorTask;
import com.alipay.sofa.registry.util.NamedThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
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
    private Registry                 metaServerRegistry;

    @Autowired
    private MetaClientExchanger      metaClientExchanger;

    @Autowired
    private RaftExchanger            raftExchanger;

    /**
     * constructor
     * @param metaServerConfig
     */
    public ExecutorManager(MetaServerConfig metaServerConfig) {
        this.metaServerConfig = metaServerConfig;
    }

    public void init() {

        scheduler = new ScheduledThreadPoolExecutor(6, new NamedThreadFactory("MetaScheduler"));

        heartbeatCheckExecutor = new ThreadPoolExecutor(1, 2, 0, TimeUnit.SECONDS,
            new SynchronousQueue<>(), new NamedThreadFactory("MetaScheduler-HeartbeatCheck"));

        checkDataChangeExecutor = new ThreadPoolExecutor(1, 2, 0, TimeUnit.SECONDS,
            new SynchronousQueue<>(), new NamedThreadFactory("MetaScheduler-CheckDataChange"));

        getOtherDataCenterChangeExecutor = new ThreadPoolExecutor(1, 2, 0, TimeUnit.SECONDS,
            new SynchronousQueue<>(), new NamedThreadFactory(
                "MetaScheduler-GetOtherDataCenterChange"));

        connectMetaServerExecutor = new ThreadPoolExecutor(1, 2, 0, TimeUnit.SECONDS,
            new SynchronousQueue<>(), new NamedThreadFactory("MetaScheduler-ConnectMetaServer"));

        checkNodeListChangePushExecutor = new ThreadPoolExecutor(1, 4, 0, TimeUnit.SECONDS,
            new SynchronousQueue<>(), new NamedThreadFactory(
                "MetaScheduler-CheckNodeListChangePush"));

        raftClientRefreshExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS,
            new SynchronousQueue<>(), new NamedThreadFactory("MetaScheduler-RaftClientRefresh"));
    }

    public void startScheduler() {

        init();

        scheduler.schedule(
                new TimedSupervisorTask("HeartbeatCheck", scheduler, heartbeatCheckExecutor,
                        metaServerConfig.getSchedulerHeartbeatTimeout(), TimeUnit.SECONDS,
                        metaServerConfig.getSchedulerHeartbeatExpBackOffBound(),
                        () -> metaServerRegistry.evict()),
                metaServerConfig.getSchedulerHeartbeatFirstDelay(), TimeUnit.SECONDS);

        scheduler.schedule(new TimedSupervisorTask("GetOtherDataCenterChange", scheduler,
                        getOtherDataCenterChangeExecutor, metaServerConfig.getSchedulerGetDataChangeTimeout(),
                        TimeUnit.SECONDS, metaServerConfig.getSchedulerGetDataChangeExpBackOffBound(),
                        () -> {
                            metaServerRegistry.getOtherDataCenterNodeAndUpdate(NodeType.DATA);
                            metaServerRegistry.getOtherDataCenterNodeAndUpdate(NodeType.META);
                        }),
                metaServerConfig.getSchedulerGetDataChangeFirstDelay(), TimeUnit.SECONDS);

        scheduler.schedule(
                new TimedSupervisorTask("ConnectMetaServer", scheduler, connectMetaServerExecutor,
                        metaServerConfig.getSchedulerConnectMetaServerTimeout(), TimeUnit.SECONDS,
                        metaServerConfig.getSchedulerConnectMetaServerExpBackOffBound(),
                        () -> metaClientExchanger.connectServer()),
                metaServerConfig.getSchedulerConnectMetaServerFirstDelay(), TimeUnit.SECONDS);

        scheduler.schedule(
                new TimedSupervisorTask("CheckSessionNodeListChangePush", scheduler,
                        checkNodeListChangePushExecutor,
                        metaServerConfig.getSchedulerCheckNodeListChangePushTimeout(), TimeUnit.SECONDS,
                        metaServerConfig.getSchedulerCheckNodeListChangePushExpBackOffBound(),
                        () -> metaServerRegistry.pushNodeListChange(NodeType.SESSION)),
                metaServerConfig.getSchedulerCheckNodeListChangePushFirstDelay(), TimeUnit.SECONDS);

        scheduler.schedule(
                new TimedSupervisorTask("CheckDataNodeListChangePush", scheduler,
                        checkNodeListChangePushExecutor,
                        metaServerConfig.getSchedulerCheckNodeListChangePushTimeout(), TimeUnit.SECONDS,
                        metaServerConfig.getSchedulerCheckNodeListChangePushExpBackOffBound(),
                        () -> metaServerRegistry.pushNodeListChange(NodeType.DATA)),
                metaServerConfig.getSchedulerCheckNodeListChangePushFirstDelay(), TimeUnit.SECONDS);

        scheduler.schedule(
                new TimedSupervisorTask("RaftClientRefresh", scheduler,
                        raftClientRefreshExecutor,
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