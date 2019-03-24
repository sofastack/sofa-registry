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
import com.alipay.sofa.registry.server.session.scheduler.ExecutorManager;
import com.alipay.sofa.registry.server.session.scheduler.task.DataPushTask;
import com.alipay.sofa.registry.server.session.scheduler.task.SessionTask;
import com.alipay.sofa.registry.server.session.store.Interests;
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
 * @version $Id: DataChangeFetchTaskListener.java, v 0.1 2017-12-13 11:58 shangyu.wh Exp $
 */
public class DataPushTaskListener implements TaskListener {

    @Autowired
    private SessionServerConfig                 sessionServerConfig;

    @Autowired
    private Interests                           sessionInterests;

    /**
     * trigger task com.alipay.sofa.registry.server.meta.listener process
     */
    @Autowired
    private TaskListenerManager                 taskListenerManager;

    @Autowired
    private ExecutorManager                     executorManager;

    private TaskDispatcher<String, SessionTask> singleTaskDispatcher;

    private TaskProcessor                       dataNodeSingleTaskProcessor;

    public DataPushTaskListener(TaskProcessor dataNodeSingleTaskProcessor) {

        this.dataNodeSingleTaskProcessor = dataNodeSingleTaskProcessor;
    }

    public TaskDispatcher<String, SessionTask> getSingleTaskDispatcher() {
        if (singleTaskDispatcher == null) {
            singleTaskDispatcher = TaskDispatchers.createDefaultSingleTaskDispatcher(
                TaskType.DATA_PUSH_TASK.getName(), dataNodeSingleTaskProcessor);
        }
        return singleTaskDispatcher;
    }

    @Override
    public boolean support(TaskEvent event) {
        return TaskType.DATA_PUSH_TASK.equals(event.getTaskType());
    }

    @Override
    public void handleEvent(TaskEvent event) {
        SessionTask dataPushTask = new DataPushTask(sessionInterests, sessionServerConfig,
            executorManager, taskListenerManager);
        dataPushTask.setTaskEvent(event);
        getSingleTaskDispatcher().dispatch(dataPushTask.getTaskId(), dataPushTask,
            dataPushTask.getExpiryTime());
    }

}