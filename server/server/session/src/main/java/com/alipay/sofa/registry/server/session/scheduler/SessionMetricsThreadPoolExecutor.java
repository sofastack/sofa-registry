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
package com.alipay.sofa.registry.server.session.scheduler;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.metrics.TaskMetrics;

/**
 *
 * @author shangyu.wh
 * @version $Id: ThreadPoolExecutorSession.java, v 0.1 2018-10-11 19:07 shangyu.wh Exp $
 */
public class SessionMetricsThreadPoolExecutor extends ThreadPoolExecutor {

    private static final Logger LOGGER = LoggerFactory
                                           .getLogger(SessionMetricsThreadPoolExecutor.class);

    private String              executorName;

    public SessionMetricsThreadPoolExecutor(String executorName, int corePoolSize,
                                            int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                            BlockingQueue<Runnable> workQueue,
                                            ThreadFactory threadFactory,
                                            RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        this.executorName = "Session-" + executorName;
        registerTaskMetrics();
        this.setRejectedExecutionHandler(handler);
    }

    public SessionMetricsThreadPoolExecutor(String executorName, int corePoolSize,
                                            int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                            BlockingQueue<Runnable> workQueue,
                                            ThreadFactory threadFactory) {
        this(executorName, corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
            threadFactory, (r, executor) -> {
                String msg = String.format(
                    "Task(%s) %s rejected from %s, throw RejectedExecutionException.", r.getClass(),
                    r, executor);
                LOGGER.error(msg);
                throw new RejectedExecutionException(msg);
            });
    }

    private void registerTaskMetrics() {
        TaskMetrics.getInstance().registerThreadExecutor(executorName, this);
    }

    @Override
    public String toString() {
        return (new StringBuilder(executorName).append(" ").append(super.toString())).toString();
    }

}