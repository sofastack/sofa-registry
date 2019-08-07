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
package com.alipay.sofa.registry.task.scheduler;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;

import java.util.TimerTask;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A supervisor task that schedules subtasks while enforce a timeout.
 * Wrapped subtasks must be thread safe.
 *
 * @author David Qiang Liu
 *
 *
 * @author shangyu.wh modify
 * @version $Id: TimedSupervisorTask.java, v 0.1 2017-11-28 15:28 shangyu.wh Exp $
 */
public class TimedSupervisorTask extends TimerTask {

    private static final Logger            LOGGER = LoggerFactory
                                                      .getLogger(TimedSupervisorTask.class);

    private final ScheduledExecutorService scheduler;
    private final ThreadPoolExecutor       executor;
    private final long                     timeoutMillis;
    private final Runnable                 task;

    private String                         name;

    private final AtomicLong               delay;
    private final long                     maxDelay;

    /**
     * constructor
     * @param name
     * @param scheduler
     * @param executor
     * @param timeout
     * @param timeUnit
     * @param expBackOffBound
     * @param task
     */
    public TimedSupervisorTask(String name, ScheduledExecutorService scheduler,
                               ThreadPoolExecutor executor, int timeout, TimeUnit timeUnit,
                               int expBackOffBound, Runnable task) {
        this.name = name;
        this.scheduler = scheduler;
        this.executor = executor;
        this.timeoutMillis = timeUnit.toMillis(timeout);
        this.task = task;
        this.delay = new AtomicLong(timeoutMillis);
        this.maxDelay = timeoutMillis * expBackOffBound;

    }

    @Override
    public void run() {
        Future future = null;
        try {
            future = executor.submit(task);
            // block until done or timeout
            future.get(timeoutMillis, TimeUnit.MILLISECONDS);
            delay.set(timeoutMillis);
        } catch (TimeoutException e) {
            LOGGER.error("{} task supervisor timed out", name, e);

            long currentDelay = delay.get();
            long newDelay = Math.min(maxDelay, currentDelay * 2);
            delay.compareAndSet(currentDelay, newDelay);

        } catch (RejectedExecutionException e) {
            LOGGER.error("{} task supervisor rejected the task: {}", name, task, e);
        } catch (Throwable e) {
            LOGGER.error("{} task supervisor threw an exception", name, e);
        } finally {
            if (future != null) {
                future.cancel(true);
            }
            scheduler.schedule(this, delay.get(), TimeUnit.MILLISECONDS);
        }
    }
}