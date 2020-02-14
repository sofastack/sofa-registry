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

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.Resource;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.data.event.StartTaskEvent;
import com.alipay.sofa.registry.server.data.executor.ExecutorFactory;
import com.alipay.sofa.registry.server.data.remoting.dataserver.task.AbstractTask;
import com.google.common.collect.Lists;

/**
 *
 * @author qian.lqlq
 * @version $Id: StartTaskEventHandler.java, v 0.1 2018-03-07 21:21 qian.lqlq Exp $
 */
public class StartTaskEventHandler extends AbstractEventHandler<StartTaskEvent> {

    private static final Logger      LOGGER       = LoggerFactory
                                                      .getLogger(StartTaskEventHandler.class);

    private static final Logger      LOGGER_START = LoggerFactory.getLogger("DATA-START-LOGS");

    @Resource(name = "tasks")
    private List<AbstractTask>       tasks;

    private ScheduledExecutorService executor     = null;

    @Override
    public List<Class<? extends StartTaskEvent>> interest() {
        return Lists.newArrayList(StartTaskEvent.class);
    }

    @Override
    public void doHandle(StartTaskEvent event) {
        if (executor == null || executor.isShutdown()) {
            getExecutor();
        }

        for (AbstractTask task : tasks) {
            if (event.getSuitableTypes().contains(task.getStartTaskTypeEnum())) {
                executor.scheduleWithFixedDelay(task, task.getInitialDelay(), task.getDelay(),
                    task.getTimeUnit());
                LOGGER_START.info("[StartTaskEventHandler] start task:{} success", task.getName());
            }
        }
    }

    private void getExecutor() {
        executor = ExecutorFactory.newScheduledThreadPool(tasks.size(), this.getClass()
            .getSimpleName());
    }

}