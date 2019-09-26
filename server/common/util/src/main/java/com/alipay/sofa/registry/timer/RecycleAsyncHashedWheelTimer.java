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
package com.alipay.sofa.registry.timer;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import io.netty.util.Timeout;
import io.netty.util.TimerTask;

/**
 *
 * @author kezhu.wukz
 * @version $Id: RecycleAsyncHashedWheelTimer.java, v 0.1 2019-07-27 10:54 kezhu.wukz Exp $
 */
public class RecycleAsyncHashedWheelTimer extends AsyncHashedWheelTimer {

    public RecycleAsyncHashedWheelTimer(ThreadFactory threadFactory, long tickDuration,
                                        TimeUnit unit, int ticksPerWheel, int threadSize,
                                        int queueSize, ThreadFactory asyncThreadFactory,
                                        TaskFailedCallback taskFailedCallback) {
        super(threadFactory, tickDuration, unit, ticksPerWheel, threadSize, queueSize,
            asyncThreadFactory, taskFailedCallback);
    }

    public RecycleAsyncHashedWheelTimer(ThreadFactory threadFactory, long tickDuration,
                                        TimeUnit unit, int ticksPerWheel, Executor asyncExecutor,
                                        TaskFailedCallback taskFailedCallback) {
        super(threadFactory, tickDuration, unit, ticksPerWheel, asyncExecutor, taskFailedCallback);
    }

    public Timeout newTimeout(TimerTask task, long firstDelay, long recycleDelay, TimeUnit unit,
                              BooleanSupplier checkCondition) {

        return super.newTimeout(
            new RecycleAsyncTimerTask(task, recycleDelay, unit, checkCondition), firstDelay, unit);
    }

    public Timeout newTimeout(TimerTask task, long recycleDelay, TimeUnit unit,
                              BooleanSupplier checkCondition) {

        return super
            .newTimeout(new RecycleAsyncTimerTask(task, recycleDelay, unit, checkCondition),
                recycleDelay, unit);
    }

    /**
     *
     */
    class RecycleAsyncTimerTask implements TimerTask, Runnable {
        /**  */
        private final TimerTask       timerTask;

        private final long            recycleDelay;

        private final TimeUnit        delayUnit;

        private final BooleanSupplier checkCondition;

        /**  */
        private Timeout               timeout;

        /**
         * @param timerTask
         */
        public RecycleAsyncTimerTask(TimerTask timerTask, long recycleDelay, TimeUnit unit,
                                     BooleanSupplier checkCondition) {
            super();
            this.timerTask = timerTask;
            this.recycleDelay = recycleDelay;
            this.delayUnit = unit;
            this.checkCondition = checkCondition;
        }

        /**
         */
        @Override
        public void run(Timeout timeout) {
            this.timeout = timeout;
            try {
                RecycleAsyncHashedWheelTimer.super.executor.execute(this);
            } catch (RejectedExecutionException e) {
                taskFailedCallback.executionRejected(e);
            } catch (Throwable e) {
                taskFailedCallback.executionFailed(e);
            }
        }

        /**
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            try {
                this.timerTask.run(this.timeout);
            } catch (Throwable e) {
                taskFailedCallback.executionFailed(e);
            } finally {
                if (checkCondition.getAsBoolean()) {
                    RecycleAsyncHashedWheelTimer.this.newTimeout(timerTask, recycleDelay,
                        delayUnit, checkCondition);
                }
            }
        }

    }

}