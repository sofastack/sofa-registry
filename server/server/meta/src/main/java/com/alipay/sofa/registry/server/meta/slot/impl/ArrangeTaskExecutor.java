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

import com.alipay.sofa.registry.exception.DisposeException;
import com.alipay.sofa.registry.exception.InitializeException;
import com.alipay.sofa.registry.exception.SofaRegistryRuntimeException;
import com.alipay.sofa.registry.lifecycle.impl.AbstractLifecycle;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.server.meta.slot.RebalanceTask;
import com.alipay.sofa.registry.util.DefaultExecutorFactory;
import com.alipay.sofa.registry.util.OsUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author chen.zhu
 * <p>
 * Nov 25, 2020
 */

public class ArrangeTaskExecutor extends AbstractLifecycle {

    private volatile ExecutorService              executors;

    private BlockingQueue<RebalanceTask> tasks      = new LinkedBlockingQueue<>();

    private final AtomicLong             totalTasks = new AtomicLong();

    private volatile RebalanceTask       currentTask;

    private AtomicBoolean                isRunning  = new AtomicBoolean(false);

    @PostConstruct
    public void postConstruct() throws Exception {
        LifecycleHelper.initializeIfPossible(this);
        LifecycleHelper.startIfPossible(this);
    }

    @PreDestroy
    public void preDestroy() throws Exception {
        LifecycleHelper.stopIfPossible(this);
        LifecycleHelper.disposeIfPossible(this);
    }

    @Override
    protected void doInitialize() throws InitializeException {
        super.doInitialize();
    }

    @Override
    protected void doDispose() throws DisposeException {
        Queue<RebalanceTask> taskQueue = null;
        synchronized (this) {
            taskQueue = tasks;
            tasks = new LinkedBlockingQueue<>();
        }
        if(taskQueue != null) {
            taskQueue.forEach(task->logger.warn("[dispose][wont execute] {}", task));
        }
        super.doDispose();
    }

    public void offer(RebalanceTask task) {
        logger.info("[offer]{}", task);
        boolean offered = false;
        synchronized (this) {
            if(getLifecycleState().isDisposing() || getLifecycleState().isDisposed()) {
                throw new SofaRegistryRuntimeException("new input tasks are not accepted");
            }
            offered = tasks.offer(task);
        }
        if (offered) {
            totalTasks.incrementAndGet();
        } else {
            logger.error("[offer][fail]{}", task);
        }

        startTaskThread();
    }

    private void startTaskThread() {
        if (isRunning.get()) {
            return;
        }
        ExecutorService executorService = executors;
        if (executorService == null) {
            synchronized (this) {
                executorService = executors;
                if (executorService == null) {
                    executors = DefaultExecutorFactory.createAllowCoreTimeout(getClass().getSimpleName(),
                            Math.max(2, OsUtils.getCpuCount())).create();
                    doExecute();
                }
            }
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
                task = tasks.poll(10, TimeUnit.MILLISECONDS);
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
                            return;
                        }

                        doExecute();
                    }
                }, executors);
            } catch (InterruptedException e) {
                logger.debug("[Task]", e);
            } catch (Exception e) {
                logger.error("[Task]", e);
            }
        }

    }
}
