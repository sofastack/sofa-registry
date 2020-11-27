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
package com.alipay.sofa.registry.server.meta.lease.impl;

import com.alipay.sofa.registry.exception.*;
import com.alipay.sofa.registry.jraft.LeaderAware;
import com.alipay.sofa.registry.lifecycle.SmartSpringLifecycle;
import com.alipay.sofa.registry.lifecycle.impl.AbstractLifecycle;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.remoting.bolt.exchange.BoltExchange;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.lease.MetaServerManager;
import com.alipay.sofa.registry.server.meta.metaserver.CrossDcMetaServer;
import com.alipay.sofa.registry.server.meta.metaserver.impl.DefaultCrossDcMetaServer;
import com.alipay.sofa.registry.server.meta.remoting.RaftExchanger;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import static com.alipay.sofa.registry.server.meta.bootstrap.MetaServerConfiguration.GLOBAL_EXECUTOR;
import static com.alipay.sofa.registry.server.meta.bootstrap.MetaServerConfiguration.SCHEDULED_EXECUTOR;

/**
 * @author chen.zhu
 * <p>
 * Nov 23, 2020
 */
/**
 * Unique entrance for all "Cross-Dc-MetaServer" instances
 * As while as a LeaderAware object, which watches leader event and trigger cross-dc-metaservers' refresh job when leader term
 * and stop all of them when it's not a raft-cluster leader
 * */
@Component
@SmartSpringLifecycle
public class CrossDcMetaServerManager extends AbstractLifecycle implements MetaServerManager,
                                                               LeaderAware {

    /**
     * Map[DataCenter(String), CrossDcMetaServer]
     * */
    private ConcurrentMap<String, CrossDcMetaServer> crossDcMetaServers = Maps.newConcurrentMap();

    @Autowired
    private NodeConfig                               nodeConfig;

    @Autowired
    private MetaServerConfig                         metaServerConfig;

    @Autowired
    private RaftExchanger                            raftExchanger;

    @Resource(name = SCHEDULED_EXECUTOR)
    private ScheduledExecutorService                 scheduled;

    @Resource(name = GLOBAL_EXECUTOR)
    private ExecutorService                          executors;

    @Resource(type = BoltExchange.class)
    private Exchange                                 boltExchange;

    @Override
    public CrossDcMetaServer getOrCreate(String dcName) {
        CrossDcMetaServer metaServer = crossDcMetaServers.get(dcName);
        if (metaServer == null) {
            synchronized (this) {
                metaServer = crossDcMetaServers.get(dcName);
                if (metaServer == null) {
                    metaServer = new DefaultCrossDcMetaServer(dcName,
                        nodeConfig.getDataCenterMetaServers(dcName), scheduled, boltExchange,
                        raftExchanger, metaServerConfig);
                    try {
                        LifecycleHelper.initializeIfPossible(metaServer);
                    } catch (Exception e) {
                        logger
                            .error(
                                "[getOrCreate][{}]Cross-Dc-MetaServer create err, stop register to map",
                                dcName, e);
                        throw new SofaRegistryRuntimeException(e);
                    }
                    crossDcMetaServers.put(dcName, metaServer);
                }
            }
        }
        return metaServer;
    }

    @Override
    public void remove(String dc) {
        CrossDcMetaServer metaServer = crossDcMetaServers.remove(dc);
        if (metaServer == null) {
            return;
        }
        try {
            LifecycleHelper.stopIfPossible(metaServer);
            LifecycleHelper.disposeIfPossible(metaServer);
        } catch (Exception e) {
            logger.error("[remove][{}]", dc, e);
        }
    }

    @Override
    protected void doInitialize() throws InitializeException {
        super.doInitialize();
        for (Map.Entry<String, Collection<String>> entry : nodeConfig.getMetaNodeIP().entrySet()) {
            getOrCreate(entry.getKey());
        }
    }

    @Override
    protected void doStart() throws StartException {
        super.doStart();
        new ConcurrentUtils.SafeParaLoop<CrossDcMetaServer>(executors, crossDcMetaServers.values()) {
            @Override
            protected void doRun0(CrossDcMetaServer metaServer) throws Exception {
                LifecycleHelper.startIfPossible(metaServer);
            }
        }.run();
    }

    @Override
    protected void doStop() throws StopException {
        new ConcurrentUtils.SafeParaLoop<CrossDcMetaServer>(executors, crossDcMetaServers.values()) {
            @Override
            protected void doRun0(CrossDcMetaServer metaServer) throws Exception {
                LifecycleHelper.stopIfPossible(metaServer);
            }
        }.run();
        super.doStop();
    }

    @Override
    protected void doDispose() throws DisposeException {
        this.crossDcMetaServers = Maps.newConcurrentMap();
        super.doDispose();
    }

    @Override
    public void isLeader() {
        try {
            LifecycleHelper.startIfPossible(this);
        } catch (Exception e) {
            logger.error("[isLeader][start refresh job]", e);
        }
    }

    @Override
    public void notLeader() {
        try {
            LifecycleHelper.stopIfPossible(this);
        } catch (Exception e) {
            logger.error("[notLeader][stop refresh job]", e);
        }
    }

}
