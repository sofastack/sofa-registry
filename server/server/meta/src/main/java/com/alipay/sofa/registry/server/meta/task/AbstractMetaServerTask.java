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
package com.alipay.sofa.registry.server.meta.task;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.task.Retryable;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author shangyu.wh
 * @version $Id: AbstractMetaServerTask.java, v 0.1 2018-01-15 16:10 shangyu.wh Exp $
 */
public abstract class AbstractMetaServerTask implements MetaServerTask, Retryable {

    private final static Logger LOGGER    = LoggerFactory.getLogger(AbstractMetaServerTask.class,
                                              "[Task]");

    protected volatile String   taskId;

    private AtomicInteger       execCount = new AtomicInteger(1);

    @Override
    public synchronized String getTaskId() {
        if (taskId == null) {
            taskId = UUID.randomUUID().toString();
        }

        return taskId;
    }

    @Override
    public long getExpiryTime() {
        return -1;
    }

    protected boolean checkRetryTimes(int configTimes) {
        if (configTimes > 0) {
            if (execCount.incrementAndGet() > configTimes) {
                LOGGER.info("retry times more than {},info:{}", configTimes, this);
                return false;
            } else {
                return true;
            }
        }
        return false;
    }
}