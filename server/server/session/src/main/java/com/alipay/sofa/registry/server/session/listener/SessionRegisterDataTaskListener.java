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

import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.scheduler.task.SessionRegisterDataTask;
import com.alipay.sofa.registry.server.session.scheduler.task.SessionTask;
import com.alipay.sofa.registry.task.batcher.TaskDispatcher;
import com.alipay.sofa.registry.task.batcher.TaskDispatchers;
import com.alipay.sofa.registry.task.batcher.TaskProcessor;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.task.listener.TaskEvent.TaskType;
import com.alipay.sofa.registry.task.listener.TaskListener;

/**
 *
 * @author shangyu.wh
 * @version $Id: SessionRegisterDataTaskListener.java, v 0.1 2018-04-16 16:30 shangyu.wh Exp $
 */
public class SessionRegisterDataTaskListener implements TaskListener {

    @Autowired
    private Exchange                            boltExchange;

    @Autowired
    private SessionServerConfig                 sessionServerConfig;

    private TaskDispatcher<String, SessionTask> singleTaskDispatcher;

    private TaskProcessor                       dataNodeSingleTaskProcessor;

    public SessionRegisterDataTaskListener(TaskProcessor dataNodeSingleTaskProcessor) {

        this.dataNodeSingleTaskProcessor = dataNodeSingleTaskProcessor;
    }

    public TaskDispatcher<String, SessionTask> getSingleTaskDispatcher() {
        if (singleTaskDispatcher == null) {
            singleTaskDispatcher = TaskDispatchers.createSingleTaskDispatcher(
                TaskDispatchers.getDispatcherName(TaskType.SESSION_REGISTER_DATA_TASK.getName()),
                60, 5, 1000, 100, dataNodeSingleTaskProcessor);
        }
        return singleTaskDispatcher;
    }

    @Override
    public boolean support(TaskEvent event) {
        return TaskType.SESSION_REGISTER_DATA_TASK.equals(event.getTaskType());
    }

    @Override
    public void handleEvent(TaskEvent event) {
        SessionTask sessionRegisterDataTask = new SessionRegisterDataTask(boltExchange,
            sessionServerConfig);
        sessionRegisterDataTask.setTaskEvent(event);
        getSingleTaskDispatcher().dispatch(sessionRegisterDataTask.getTaskId(),
            sessionRegisterDataTask, sessionRegisterDataTask.getExpiryTime());
    }
}