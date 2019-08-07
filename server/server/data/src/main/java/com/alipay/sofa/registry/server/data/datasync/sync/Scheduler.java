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
package com.alipay.sofa.registry.server.data.datasync.sync;

import com.alipay.sofa.registry.server.data.datasync.AcceptorStore;
import com.alipay.sofa.registry.task.scheduler.TimedSupervisorTask;
import com.alipay.sofa.registry.util.NamedThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author shangyu.wh
 * @version $Id: Scheduler.java, v 0.1 2018-03-07 16:07 shangyu.wh Exp $
 */
public class Scheduler {

    public final ExecutorService           versionCheckExecutor;
    private final ScheduledExecutorService scheduler;
    private final ThreadPoolExecutor       expireCheckExecutor;

    @Autowired
    private AcceptorStore                  localAcceptorStore;

    /**
     * constructor
     */
    public Scheduler() {
        scheduler = new ScheduledThreadPoolExecutor(4, new NamedThreadFactory("SyncDataScheduler"));

        expireCheckExecutor = new ThreadPoolExecutor(1, 3, 0, TimeUnit.SECONDS,
            new SynchronousQueue<>(), new NamedThreadFactory("SyncDataScheduler-expireChangeCheck"));

        versionCheckExecutor = new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(), new NamedThreadFactory(
                "SyncDataScheduler-versionChangeCheck"));

    }

    /**
     * start scheduler
     */
    public void startScheduler() {

        scheduler.schedule(
                new TimedSupervisorTask("FetchDataLocal", scheduler, expireCheckExecutor, 3,
                        TimeUnit.SECONDS, 10, () -> localAcceptorStore.checkAcceptorsChangAndExpired()),
                30, TimeUnit.SECONDS);


        versionCheckExecutor.execute(() -> localAcceptorStore.changeDataCheck());

    }

    /**
     * stop scheduler
     */
    public void stopScheduler() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
        if (versionCheckExecutor != null && !versionCheckExecutor.isShutdown()) {
            versionCheckExecutor.shutdown();
        }
    }
}