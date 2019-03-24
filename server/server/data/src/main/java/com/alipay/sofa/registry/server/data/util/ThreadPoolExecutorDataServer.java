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
package com.alipay.sofa.registry.server.data.util;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author shangyu.wh
 * @version $Id: ThreadPoolExecutorDataServer.java, v 0.1 2018-10-25 20:40 shangyu.wh Exp $
 */
public class ThreadPoolExecutorDataServer extends ThreadPoolExecutor {

    private static final Logger LOGGER = LoggerFactory
                                           .getLogger(ThreadPoolExecutorDataServer.class);

    private String              executorName;

    public ThreadPoolExecutorDataServer(String executorName, int corePoolSize, int maximumPoolSize,
                                        long keepAliveTime, TimeUnit unit,
                                        BlockingQueue<Runnable> workQueue,
                                        ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        this.executorName = executorName;
    }

    @Override
    public String toString() {
        return super.toString() + executorName;
    }

    @Override
    public void execute(Runnable command) {
        try {
            super.execute(command);
        } catch (RejectedExecutionException e) {
            LOGGER.error("Processor session executor {} Rejected Execution!command {}", this,
                command.getClass(), e);
        }
    }
}