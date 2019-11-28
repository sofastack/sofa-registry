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

import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.cache.CacheService;
import com.alipay.sofa.registry.server.session.scheduler.ExecutorManager;
import com.alipay.sofa.registry.server.session.scheduler.task.DataChangeFetchTask;
import com.alipay.sofa.registry.server.session.scheduler.task.SessionTask;
import com.alipay.sofa.registry.server.session.store.Interests;
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
 * @version $Id: DataChangeFetchTaskListener.java, v 0.1 2017-12-13 11:58 shangyu.wh Exp $
 */
public class DataChangeFetchTaskListener implements TaskListener {

    private final static Logger                          LOGGER = LoggerFactory
                                                                    .getLogger(DataChangeFetchTaskListener.class);

    @Autowired
    private SessionServerConfig                          sessionServerConfig;

    @Autowired
    private Interests                                    sessionInterests;

    @Autowired
    private ExecutorManager                              executorManager;

    @Autowired
    private CacheService                                 sessionCacheService;

    /**
     * trigger task com.alipay.sofa.registry.server.meta.listener process
     */
    @Autowired
    private TaskListenerManager                          taskListenerManager;

    private volatile TaskDispatcher<String, SessionTask> singleTaskDispatcher;

    private TaskProcessor                                dataNodeSingleTaskProcessor;

    public DataChangeFetchTaskListener(TaskProcessor dataNodeSingleTaskProcessor) {
        this.dataNodeSingleTaskProcessor = dataNodeSingleTaskProcessor;
    }

    public TaskDispatcher<String, SessionTask> getSingleTaskDispatcher() {
        if (singleTaskDispatcher == null) {
            synchronized (this) {
                if (singleTaskDispatcher == null) {
                    singleTaskDispatcher = TaskDispatchers
                        .createSingleTaskDispatcher(TaskDispatchers
                            .getDispatcherName(TaskType.DATA_CHANGE_FETCH_TASK.getName()),
                            sessionServerConfig.getDataChangeFetchTaskMaxBufferSize(),
                            sessionServerConfig.getDataChangeFetchTaskWorkerSize(), 1000, 100,
                            dataNodeSingleTaskProcessor);
                }
            }
        }
        return singleTaskDispatcher;
    }

    @Override
    public TaskType support() {
        return TaskType.DATA_CHANGE_FETCH_TASK;
    }

    @Override
    public void handleEvent(TaskEvent event) {
        SessionTask dataChangeFetchTask = new DataChangeFetchTask(sessionServerConfig,
            taskListenerManager, executorManager, sessionInterests, sessionCacheService);
        dataChangeFetchTask.setTaskEvent(event);

        getSingleTaskDispatcher().dispatch(dataChangeFetchTask.getTaskId(), dataChangeFetchTask,
            dataChangeFetchTask.getExpiryTime());

    }

}