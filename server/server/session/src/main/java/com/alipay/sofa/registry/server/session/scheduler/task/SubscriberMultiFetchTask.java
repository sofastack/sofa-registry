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
package com.alipay.sofa.registry.server.session.scheduler.task;

import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.cache.CacheService;
import com.alipay.sofa.registry.server.session.strategy.SubscriberMultiFetchTaskStrategy;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;

import java.util.Collection;

/**
 *
 * @author shangyu.wh
 * @version $Id: SubscriberMultiFetchTask.java, v 0.1 2018-11-08 11:54 shangyu.wh Exp $
 */
public class SubscriberMultiFetchTask extends AbstractSessionTask {
    private static final Logger              LOGGER = LoggerFactory
                                                        .getLogger(SubscriberMultiFetchTask.class);

    private final SessionServerConfig        sessionServerConfig;
    /**
     * trigger task com.alipay.sofa.registry.server.meta.listener process
     */
    private final TaskListenerManager        taskListenerManager;

    private final CacheService               sessionCacheService;

    private String                           fetchDataInfoId;

    private Collection<Subscriber>           subscribers;

    private SubscriberMultiFetchTaskStrategy subscriberMultiFetchTaskStrategy;

    public SubscriberMultiFetchTask(SessionServerConfig sessionServerConfig,
                                    TaskListenerManager taskListenerManager,
                                    CacheService sessionCacheService,
                                    SubscriberMultiFetchTaskStrategy subscriberMultiFetchTaskStrategy) {
        this.sessionServerConfig = sessionServerConfig;
        this.taskListenerManager = taskListenerManager;
        this.sessionCacheService = sessionCacheService;
        this.subscriberMultiFetchTaskStrategy = subscriberMultiFetchTaskStrategy;
    }

    @Override
    public void setTaskEvent(TaskEvent taskEvent) {

        //taskId create from event
        if (taskEvent.getTaskId() != null) {
            setTaskId(taskEvent.getTaskId());
        }

        Object obj = taskEvent.getEventObj();

        if (!(obj instanceof String)) {
            throw new IllegalArgumentException("Input task event object error!");
        }

        this.fetchDataInfoId = (String) obj;

        subscribers = (Collection<Subscriber>) taskEvent
            .getAttribute(Constant.PUSH_CLIENT_SUBSCRIBERS);

        if (subscribers.isEmpty()) {
            LOGGER.error("Subscriber MultiFetchTask subscribers is empty!");
            throw new RuntimeException(
                "Subscriber MultiFetchTask got exception!send subscribers is empty");
        }
    }

    @Override
    public void execute() {
        subscriberMultiFetchTaskStrategy.doSubscriberMultiFetchTask(sessionServerConfig,
            taskListenerManager, sessionCacheService, fetchDataInfoId, subscribers);
    }

    @Override
    public boolean checkRetryTimes() {
        return checkRetryTimes(sessionServerConfig.getDataChangeFetchTaskRetryTimes());
    }

    @Override
    public String toString() {
        return "SUBSCRIBER_MULTI_FETCH_TASK {" + "taskId='" + getTaskId() + '\''
               + ", fetchDataInfoId=" + fetchDataInfoId + ", expiryTime='" + getExpiryTime() + '\''
               + '}';
    }
}