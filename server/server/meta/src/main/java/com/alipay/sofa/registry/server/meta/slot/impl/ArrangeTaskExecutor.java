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

import com.alipay.sofa.registry.lifecycle.impl.AbstractLifecycle;
import com.alipay.sofa.registry.server.meta.slot.RebalanceTask;
import com.alipay.sofa.registry.util.DefaultExecutorFactory;
import com.alipay.sofa.registry.util.OsUtils;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author chen.zhu
 * <p>
 * Nov 25, 2020
 */
@Component
public class ArrangeTaskExecutor extends AbstractLifecycle {

    private ExecutorService              executors;

    private BlockingQueue<RebalanceTask> tasks      = new LinkedBlockingQueue<>();

    private final AtomicLong             totalTasks = new AtomicLong();

    private volatile RebalanceTask       currentTask;

    private AtomicBoolean                isRunning  = new AtomicBoolean(false);

    public void offer(RebalanceTask task) {
        logger.info("[offer]{}", task);
        if (tasks.offer(task)) {
            totalTasks.incrementAndGet();
        } else {
            logger.error("[offset][fail]{}", task);
        }

        startTaskThread();
    }

    private void startTaskThread() {
        if (isRunning.get()) {
            return;
        }
        if (executors == null) {
            executors = DefaultExecutorFactory.createAllowCoreTimeout(getClass().getSimpleName(),
                Math.max(2, OsUtils.getCpuCount())).create();
            doExecute();
        }
    }

    private void doExecute() {
        executors.execute(new Task());
    }

    public class Task implements Runnable {

        @Override
        public void run() {
            RebalanceTask task = null;
            if (!isRunning.compareAndSet(false, true)) {
                logger.debug("[run][already run]{}", this);
                return;
            }
            try {
                task = tasks.poll();
                if (task == null) {
                    isRunning.compareAndSet(true, false);
                    return;
                }
                currentTask = task;

                CompletableFuture<?> future = CompletableFuture.runAsync(currentTask, executors);
                future.thenRunAsync(new Runnable() {
                    @Override
                    public void run() {
                        currentTask = null;
                        if (!isRunning.compareAndSet(true, false)) {
                            logger.error("[doRun][already exit]");
                        }

                        doExecute();
                    }
                }, executors);
            } catch (Exception e) {
                logger.error("[Task]", e);
            } finally {
                currentTask = null;
            }
        }

    }
}
