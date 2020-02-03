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
package com.alipay.sofa.registry.server.session.listener;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.cache.CacheService;
import com.alipay.sofa.registry.server.session.scheduler.task.SessionTask;
import com.alipay.sofa.registry.server.session.scheduler.task.SubscriberMultiFetchTask;
import com.alipay.sofa.registry.server.session.strategy.SubscriberMultiFetchTaskStrategy;
import com.alipay.sofa.registry.task.batcher.TaskDispatcher;
import com.alipay.sofa.registry.task.batcher.TaskDispatchers;
import com.alipay.sofa.registry.task.batcher.TaskProcessor;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.task.listener.TaskEvent.TaskType;
import com.alipay.sofa.registry.task.listener.TaskListener;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;

/**
 *
 * @author shangyu.wh
 * @version $Id: SubscriberRegisterFetchTaskListener.java, v 0.1 2017-12-07 19:53 shangyu.wh Exp $
 */
public class SubscriberMultiFetchTaskListener implements TaskListener {

    @Autowired
    private SessionServerConfig                 sessionServerConfig;

    /**
     * trigger task com.alipay.sofa.registry.server.meta.listener process
     */
    @Autowired
    private TaskListenerManager                 taskListenerManager;

    @Autowired
    private CacheService                        sessionCacheService;

    @Autowired
    private SubscriberMultiFetchTaskStrategy    subscriberMultiFetchTaskStrategy;

    private TaskDispatcher<String, SessionTask> singleTaskDispatcher;

    private TaskProcessor                       dataNodeSingleTaskProcessor;

    public SubscriberMultiFetchTaskListener(TaskProcessor dataNodeSingleTaskProcessor) {

        this.dataNodeSingleTaskProcessor = dataNodeSingleTaskProcessor;
    }

    @PostConstruct
    public void init() {
        singleTaskDispatcher = TaskDispatchers.createSingleTaskDispatcher(
            TaskDispatchers.getDispatcherName(TaskType.SUBSCRIBER_MULTI_FETCH_TASK.getName()),
            100000, 80, 0, 0, dataNodeSingleTaskProcessor);
    }

    @Override
    public TaskType support() {
        return TaskType.SUBSCRIBER_MULTI_FETCH_TASK;
    }

    @Override
    public void handleEvent(TaskEvent event) {

        SessionTask subscriberMultiFetchTask = new SubscriberMultiFetchTask(sessionServerConfig,
            taskListenerManager, sessionCacheService, subscriberMultiFetchTaskStrategy);

        subscriberMultiFetchTask.setTaskEvent(event);

        singleTaskDispatcher.dispatch(subscriberMultiFetchTask.getTaskId(),
            subscriberMultiFetchTask, subscriberMultiFetchTask.getExpiryTime());
    }

}