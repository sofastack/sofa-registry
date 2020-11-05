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
package com.alipay.sofa.registry.server.data.executor;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.util.NamedThreadFactory;

/**
 * the factory to create executor
 *
 * @author qian.lqlq
 * @version $Id: ExecutorFactory.java, v 0.1 2018-03-08 14:50 qian.lqlq Exp $
 */
public class ExecutorFactory {

    public static final ThreadPoolExecutor EXECUTOR;
    public static final ThreadPoolExecutor NOTIFY_SESSION_CALLBACK_EXECUTOR;
    public static final ThreadPoolExecutor MIGRATING_SESSION_CALLBACK_EXECUTOR;
    private static final Logger            LOGGER = LoggerFactory.getLogger(ExecutorFactory.class);

    static {
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(10);
        EXECUTOR = new ThreadPoolExecutor(20, 300, 1, TimeUnit.HOURS, workQueue,
            new NamedThreadFactory("CommonExecutor")) {

            /**
             * @see ThreadPoolExecutor#afterExecute(Runnable, Throwable)
             */
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                if (t != null) {
                    LOGGER.error("[CommonThreadPool] ThreadPoolUncaughtException:{}",
                        t.getMessage(), t);
                }
            }
        };

        NOTIFY_SESSION_CALLBACK_EXECUTOR = new ThreadPoolExecutor(10, 20, 300, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100000), new NamedThreadFactory(
                "NotifySessionCallback-executor", true));

        MIGRATING_SESSION_CALLBACK_EXECUTOR = new ThreadPoolExecutor(10, 20, 300, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100000), new NamedThreadFactory(
                "MigratingSessionCallback-executor", true));
    }

    /**
     * get executor
     * @return
     */
    public static ThreadPoolExecutor getCommonExecutor() {
        return EXECUTOR;
    }

    /**
     * new thread pool
     * @param size
     * @param name
     * @return
     */
    public static Executor newFixedThreadPool(int size, String name) {
        return new ThreadPoolExecutor(size, size, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(), new NamedThreadFactory(name));
    }

    /**
     * new scheduled thread pool
     * @param size
     * @param name
     * @return
     */
    public static ScheduledExecutorService newScheduledThreadPool(int size, String name) {
        return new ScheduledThreadPoolExecutor(size, new NamedThreadFactory(name));
    }

    /**
     * new single thread executor
     * @param name
     * @return
     */
    public static Executor newSingleThreadExecutor(String name) {
        return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
            new NamedThreadFactory(name));
    }
}