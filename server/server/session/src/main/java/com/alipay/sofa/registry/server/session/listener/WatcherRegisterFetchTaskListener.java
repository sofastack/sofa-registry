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

import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.scheduler.task.SessionTask;
import com.alipay.sofa.registry.server.session.scheduler.task.WatcherRegisterFetchTask;
import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
import com.alipay.sofa.registry.task.batcher.TaskDispatcher;
import com.alipay.sofa.registry.task.batcher.TaskDispatchers;
import com.alipay.sofa.registry.task.batcher.TaskProcessor;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.task.listener.TaskEvent.TaskType;
import com.alipay.sofa.registry.task.listener.TaskListener;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author shangyu.wh
 * @version $Id: SubscriberRegisterFetchTaskListener.java, v 0.1 2017-12-07 19:53 shangyu.wh Exp $
 */
public class WatcherRegisterFetchTaskListener implements TaskListener {

    @Autowired
    private SessionServerConfig                          sessionServerConfig;

    /**
     * trigger push client process
     */
    @Autowired
    private TaskListenerManager                          taskListenerManager;

    /**
     * MetaNode service
     */
    @Autowired
    private MetaServerService                            metaServerService;

    private volatile TaskDispatcher<String, SessionTask> singleTaskDispatcher;

    private TaskProcessor                                dataNodeSingleTaskProcessor;

    public WatcherRegisterFetchTaskListener(TaskProcessor dataNodeSingleTaskProcessor) {
        this.dataNodeSingleTaskProcessor = dataNodeSingleTaskProcessor;
    }

    public TaskDispatcher<String, SessionTask> getSingleTaskDispatcher() {
        if (singleTaskDispatcher == null) {
            synchronized (this) {
                if (singleTaskDispatcher == null) {
                    singleTaskDispatcher = TaskDispatchers
                        .createDefaultSingleTaskDispatcher(
                            TaskType.WATCHER_REGISTER_FETCH_TASK.getName(),
                            dataNodeSingleTaskProcessor);
                }
            }
        }
        return singleTaskDispatcher;
    }

    @Override
    public TaskType support() {
        return TaskType.WATCHER_REGISTER_FETCH_TASK;
    }

    @Override
    public void handleEvent(TaskEvent event) {

        SessionTask watcherRegisterFetchTask = new WatcherRegisterFetchTask(sessionServerConfig,
            taskListenerManager, metaServerService);

        watcherRegisterFetchTask.setTaskEvent(event);

        getSingleTaskDispatcher().dispatch(watcherRegisterFetchTask.getTaskId(),
            watcherRegisterFetchTask, watcherRegisterFetchTask.getExpiryTime());
    }

}