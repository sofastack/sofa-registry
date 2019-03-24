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
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.cache.CacheService;
import com.alipay.sofa.registry.server.session.node.service.DataNodeService;
import com.alipay.sofa.registry.server.session.strategy.SubscriberRegisterFetchTaskStrategy;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;

/**
 *
 * @author shangyu.wh
 * @version $Id: SubscriberRegisterFetchTask.java, v 0.1 2017-12-07 16:23 shangyu.wh Exp $
 */
public class SubscriberRegisterFetchTask extends AbstractSessionTask {

    private final SessionServerConfig                 sessionServerConfig;
    /**
     * trigger task com.alipay.sofa.registry.server.meta.listener process
     */
    private final TaskListenerManager                 taskListenerManager;
    /**
     * DataNode service
     */
    private final DataNodeService                     dataNodeService;

    private final CacheService                        sessionCacheService;

    private final SubscriberRegisterFetchTaskStrategy subscriberRegisterFetchTaskStrategy;
    private Subscriber                                subscriber;

    public SubscriberRegisterFetchTask(SessionServerConfig sessionServerConfig,
                                       TaskListenerManager taskListenerManager,
                                       DataNodeService dataNodeService,
                                       CacheService sessionCacheService,
                                       SubscriberRegisterFetchTaskStrategy subscriberRegisterFetchTaskStrategy) {
        this.sessionServerConfig = sessionServerConfig;
        this.taskListenerManager = taskListenerManager;
        this.dataNodeService = dataNodeService;
        this.sessionCacheService = sessionCacheService;
        this.subscriberRegisterFetchTaskStrategy = subscriberRegisterFetchTaskStrategy;
    }

    @Override
    public long getExpiryTime() {
        //TODO CONFIG
        return -1;
    }

    @Override
    public void setTaskEvent(TaskEvent taskEvent) {
        Object obj = taskEvent.getEventObj();

        if (!(obj instanceof Subscriber)) {
            throw new IllegalArgumentException("Input task event object error!");
        }

        this.subscriber = (Subscriber) obj;
    }

    @Override
    public void execute() {
        subscriberRegisterFetchTaskStrategy.doSubscriberRegisterFetchTask(sessionServerConfig,
            taskListenerManager, dataNodeService, sessionCacheService, subscriber);
    }

    @Override
    public boolean checkRetryTimes() {
        return checkRetryTimes(sessionServerConfig.getSubscriberRegisterFetchRetryTimes());
    }

    @Override
    public String toString() {
        return "SUBSCRIBER_REGISTER_FETCH_TASK{" + "taskId='" + getTaskId() + '\''
               + ", subscriber=" + subscriber + ", expiryTime='" + getExpiryTime() + '\'' + '}';
    }
}