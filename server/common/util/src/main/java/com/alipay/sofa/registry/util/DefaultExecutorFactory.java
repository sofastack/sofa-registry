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
package com.alipay.sofa.registry.util;

import java.util.concurrent.*;

/**
 * @author chen.zhu
 * <p>
 * Nov 23, 2020
 */
public class DefaultExecutorFactory implements ObjectFactory<ExecutorService> {

    private static final int                      DEFAULT_MAX_QUEUE_SIZE            = 1 << 20;
    private static final RejectedExecutionHandler DEFAULT_HANDLER                   = new ThreadPoolExecutor.CallerRunsPolicy();
    private static final int                      DEFAULT_CORE_POOL_SIZE            = OsUtils
                                                                                        .getCpuCount();
    private static final int                      DEFAULT_MAX_POOL_SIZE             = 4 * OsUtils
                                                                                        .getCpuCount();
    private static final int                      DEFAULT_KEEPER_ALIVE_TIME_SECONDS = 60;
    private static final boolean                  DEFAULT_ALLOW_CORE_THREAD_TIMEOUT = true;
    private static final String                   DEFAULT_THREAD_PREFIX             = "SofaRegistry";

    private int                                   corePoolSize                      = DEFAULT_CORE_POOL_SIZE;
    private int                                   maxPoolSize                       = DEFAULT_MAX_POOL_SIZE;
    private long                                  keepAliveTime                     = DEFAULT_KEEPER_ALIVE_TIME_SECONDS;
    private BlockingQueue<Runnable>               workQueue;
    private TimeUnit                              keepAliveTimeUnit                 = TimeUnit.SECONDS;
    private ThreadFactory                         threadFactory;
    private String                                threadNamePrefix                  = DEFAULT_THREAD_PREFIX;
    private RejectedExecutionHandler              rejectedExecutionHandler          = DEFAULT_HANDLER;
    private boolean                               allowCoreThreadTimeOut            = DEFAULT_ALLOW_CORE_THREAD_TIMEOUT;

    public static DefaultExecutorFactory createAllowCoreTimeout(String threadNamePrefix) {
        return new DefaultExecutorFactory(threadNamePrefix, DEFAULT_CORE_POOL_SIZE, true);
    }

    public static DefaultExecutorFactory createAllowCoreTimeout(String threadNamePrefix,
                                                                int corePoolSize) {
        return new DefaultExecutorFactory(threadNamePrefix, corePoolSize, true);
    }

    public static DefaultExecutorFactory createCachedThreadPoolFactory(String threadNamePrefix,
                                                                       int corePoolSize,
                                                                       int corePoolTimeAlive,
                                                                       TimeUnit corePoolTimeAliveUnit) {
        return new DefaultExecutorFactory(threadNamePrefix, corePoolSize, true, Integer.MAX_VALUE,
            new SynchronousQueue<Runnable>(), corePoolTimeAlive, corePoolTimeAliveUnit,
            DEFAULT_HANDLER);
    }

    public static DefaultExecutorFactory createCachedThreadPoolFactory(String threadNamePrefix,
                                                                       int corePoolSize,
                                                                       int corePoolTimeAlive,
                                                                       TimeUnit corePoolTimeAliveUnit,
                                                                       RejectedExecutionHandler rejectedExecutionHandler) {
        return new DefaultExecutorFactory(threadNamePrefix, corePoolSize, true, Integer.MAX_VALUE,
            new SynchronousQueue<Runnable>(), corePoolTimeAlive, corePoolTimeAliveUnit,
            rejectedExecutionHandler);
    }

    public DefaultExecutorFactory(String threadNamePrefix, int corePoolSize, int maxPoolSize) {
        this(threadNamePrefix, corePoolSize, DEFAULT_ALLOW_CORE_THREAD_TIMEOUT, maxPoolSize,
            DEFAULT_MAX_QUEUE_SIZE, 60, TimeUnit.SECONDS, DEFAULT_HANDLER);
    }

    public DefaultExecutorFactory(String threadNamePrefix, int corePoolSize, int maxPoolSize,
                                  RejectedExecutionHandler rejectedExecutionHandler) {
        this(threadNamePrefix, corePoolSize, DEFAULT_ALLOW_CORE_THREAD_TIMEOUT, maxPoolSize,
            DEFAULT_MAX_QUEUE_SIZE, 60, TimeUnit.SECONDS, rejectedExecutionHandler);
    }

    public DefaultExecutorFactory(String threadNamePrefix, int corePoolSize,
                                  boolean allowCoreThreadTimeOut) {
        this(threadNamePrefix, corePoolSize, allowCoreThreadTimeOut, DEFAULT_MAX_POOL_SIZE,
            DEFAULT_MAX_QUEUE_SIZE, 60, TimeUnit.SECONDS, DEFAULT_HANDLER);
    }

    public DefaultExecutorFactory(String threadNamePrefix, int corePoolSize,
                                  boolean allowCoreThreadTimeOut, int maxPoolSize,
                                  int maxQueueSize, int keepAliveTime, TimeUnit keepAliveTimeUnit,
                                  RejectedExecutionHandler rejectedExecutionHandler) {
        this.threadNamePrefix = threadNamePrefix;
        this.corePoolSize = corePoolSize;
        this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.keepAliveTimeUnit = keepAliveTimeUnit;
        this.workQueue = new LinkedBlockingQueue<>(maxQueueSize);
        this.rejectedExecutionHandler = rejectedExecutionHandler;
    }

    public DefaultExecutorFactory(String threadNamePrefix, int corePoolSize,
                                  boolean allowCoreThreadTimeOut, int maxPoolSize,
                                  BlockingQueue<Runnable> queue, int keepAliveTime,
                                  TimeUnit keepAliveTimeUnit,
                                  RejectedExecutionHandler rejectedExecutionHandler) {
        this.threadNamePrefix = threadNamePrefix;
        this.corePoolSize = corePoolSize;
        this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.keepAliveTimeUnit = keepAliveTimeUnit;
        this.workQueue = queue;
        this.rejectedExecutionHandler = rejectedExecutionHandler;
    }

    @Override
    public ExecutorService create() {
        // core pool size must be less or equal to max size
        int useMaxPoolSize = Math.max(corePoolSize, maxPoolSize);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, useMaxPoolSize,
            keepAliveTime, keepAliveTimeUnit, workQueue, threadFactory != null ? threadFactory
                : new NamedThreadFactory(threadNamePrefix), rejectedExecutionHandler);

        executor.allowCoreThreadTimeOut(allowCoreThreadTimeOut);
        return executor;
    }
}
