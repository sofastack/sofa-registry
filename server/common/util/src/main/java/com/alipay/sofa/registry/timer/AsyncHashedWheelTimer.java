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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

/**
 * based on HashedWheelTimer, add function:  exec TimerTask async
 *
 * @author kezhu.wukz
 * @version $Id: AsyncHashedWheelTimer.java, v 0.1 2019-01-11 10:54 AM kezhu.wukz Exp $
 */
public class AsyncHashedWheelTimer extends HashedWheelTimer {
    /**  */
    protected final Executor           executor;

    /**  */
    protected final TaskFailedCallback taskFailedCallback;

    /**
     * @param threadFactory
     * @param tickDuration
     * @param unit
     * @param ticksPerWheel
     * @param threadSize
     * @param queueSize
     * @param asyncThreadFactory
     */
    public AsyncHashedWheelTimer(ThreadFactory threadFactory, long tickDuration, TimeUnit unit,
                                 int ticksPerWheel, int threadSize, int queueSize,
                                 ThreadFactory asyncThreadFactory,
                                 TaskFailedCallback taskFailedCallback) {
        super(threadFactory, tickDuration, unit, ticksPerWheel);

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(threadSize, threadSize,
            300L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(queueSize), asyncThreadFactory);
        threadPoolExecutor.allowCoreThreadTimeOut(true);
        this.executor = threadPoolExecutor;
        this.taskFailedCallback = taskFailedCallback;
    }

    /**
     *
     * @param threadFactory
     * @param tickDuration
     * @param unit
     * @param ticksPerWheel
     * @param asyncExecutor
     */
    public AsyncHashedWheelTimer(ThreadFactory threadFactory, long tickDuration, TimeUnit unit,
                                 int ticksPerWheel, Executor asyncExecutor,
                                 TaskFailedCallback taskFailedCallback) {
        super(threadFactory, tickDuration, unit, ticksPerWheel);

        this.executor = asyncExecutor;
        this.taskFailedCallback = taskFailedCallback;
    }

    /**
     */
    @Override
    public Timeout newTimeout(TimerTask task, long delay, TimeUnit unit) {
        return super.newTimeout(new AsyncTimerTask(task), delay, unit);
    }

    /**
     *
     */
    class AsyncTimerTask implements TimerTask, Runnable {
        /**  */
        TimerTask timerTask;
        /**  */
        Timeout   timeout;

        /**
         * @param timerTask
         */
        public AsyncTimerTask(TimerTask timerTask) {
            super();
            this.timerTask = timerTask;
        }

        /**
         */
        @Override
        public void run(Timeout timeout) {
            this.timeout = timeout;
            try {
                AsyncHashedWheelTimer.this.executor.execute(this);
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
            }
        }

    }

    public interface TaskFailedCallback {

        void executionRejected(Throwable e);

        void executionFailed(Throwable e);

    }

}
