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
package com.alipay.sofa.registry.server.data.event.handler;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.data.cache.CacheDigestTask;
import com.alipay.sofa.registry.server.data.event.StartTaskEvent;
import com.alipay.sofa.registry.server.data.executor.ExecutorFactory;
import com.alipay.sofa.registry.server.data.remoting.dataserver.task.AbstractTask;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

/**
 *
 * @author qian.lqlq
 * @version $Id: StartTaskEventHandler.java, v 0.1 2018-03-07 21:21 qian.lqlq Exp $
 */
public class StartTaskEventHandler extends AbstractEventHandler<StartTaskEvent> {

    private static final Logger      LOGGER   = LoggerFactory
                                                  .getLogger(StartTaskEventHandler.class);

    @Resource(name = "tasks")
    private List<AbstractTask>       tasks;

    private ScheduledExecutorService executor = null;

    @Override
    public Class interest() {
        return StartTaskEvent.class;
    }

    @Override
    public void doHandle(StartTaskEvent event) {
        if (executor == null || executor.isShutdown()) {
            executor = ExecutorFactory.newScheduledThreadPool(tasks.size(), this.getClass()
                .getSimpleName());
            for (AbstractTask task : tasks) {
                LOGGER.info("[StartTaskEventHandler] start task:{}", task.getName());
                executor.scheduleWithFixedDelay(task, task.getInitialDelay(), task.getDelay(),
                    task.getTimeUnit());
                LOGGER.info("[StartTaskEventHandler] start task:{} success", task.getName());
            }
            new CacheDigestTask().start();
        }
    }

}