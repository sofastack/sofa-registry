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
package com.alipay.sofa.registry.server.meta.slot.impl;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.exception.DisposeException;
import com.alipay.sofa.registry.exception.InitializeException;
import com.alipay.sofa.registry.lifecycle.SmartSpringLifecycle;
import com.alipay.sofa.registry.observer.impl.AbstractLifecycleObservable;
import com.alipay.sofa.registry.server.meta.lease.DataServerManager;
import com.alipay.sofa.registry.server.meta.slot.ArrangeTaskDispatcher;
import com.alipay.sofa.registry.server.meta.slot.tasks.ServerAddRebalanceWork;
import com.alipay.sofa.registry.server.meta.slot.tasks.ServerDeadRebalanceWork;
import com.alipay.sofa.registry.util.NamedThreadFactory;
import com.alipay.sofa.registry.util.OsUtils;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

/**
 * @author chen.zhu
 * <p>
 * Nov 25, 2020
 */
@SmartSpringLifecycle
public class DataServerArrangeTaskDispatcher extends AbstractLifecycleObservable
                                                                                implements
                                                                                ArrangeTaskDispatcher<DataNode> {

    private final ConcurrentMap<DataNode, DeadServerAction> deadServerActions = Maps
                                                                                  .newConcurrentMap();

    @Autowired
    private ArrangeTaskExecutor                             arrangeTaskExecutor;

    @Autowired
    private DataServerManager                               dataServerManager;

    @Autowired
    private DefaultSlotManager                              slotManager;

    private ScheduledExecutorService                        scheduled;

    @Override
    protected void doInitialize() throws InitializeException {
        super.doInitialize();
        scheduled = new ScheduledThreadPoolExecutor(Math.min(OsUtils.getCpuCount(), 8),
            new NamedThreadFactory("DataServerArrangeTaskDispatcher"));
    }

    @Override
    protected void doDispose() throws DisposeException {
        scheduled.shutdownNow();
        super.doDispose();
    }

    @Override
    public void serverAlive(DataNode dataNode) {
        if (logger.isInfoEnabled()) {
            logger.info("[serverAlive]{}", dataNode);
        }
        DeadServerAction deadServerAction = deadServerActions.get(dataNode);
        if (deadServerAction == null) {
            arrangeTaskExecutor.offer(new ServerAddRebalanceWork());
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("[serverAlive][dead server alive]{}", dataNode);
            }
            deadServerAction.serverAlive();
        }
    }

    @Override
    public void serverDead(DataNode dataNode) {
        if (logger.isInfoEnabled()) {
            logger.info("[serverDead]{}", dataNode.getIp());
        }
        deadServerActions.putIfAbsent(dataNode, new DeadServerAction(dataNode));
    }

    private int getWaitForRestartMilli() {
        return 15 * 1000;
    }

    public class DeadServerAction implements Runnable {

        private DataNode           dataNode;
        private ScheduledFuture<?> future;

        public DeadServerAction(DataNode dataNode) {
            this.dataNode = dataNode;
            this.future = scheduled.schedule(this, getWaitForRestartMilli(), TimeUnit.MILLISECONDS);
        }

        @Override
        public void run() {
            cleanCache();
            arrangeTaskExecutor.offer(new ServerDeadRebalanceWork(slotManager, dataServerManager,
                dataNode));
        }

        public void serverAlive() {
            future.cancel(true);
            cleanCache();
        }

        private void cleanCache() {
            deadServerActions.remove(dataNode);
        }
    }
}
