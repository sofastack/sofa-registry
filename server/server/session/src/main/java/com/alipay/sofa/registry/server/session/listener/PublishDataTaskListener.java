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

import com.alipay.sofa.registry.server.session.node.service.DataNodeService;
import com.alipay.sofa.registry.server.session.scheduler.ExecutorManager;
import com.alipay.sofa.registry.server.session.scheduler.task.PublishDataTask;
import com.alipay.sofa.registry.server.session.scheduler.task.SessionTask;
import com.alipay.sofa.registry.task.batcher.TaskProcessor;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.task.listener.TaskEvent.TaskType;
import com.alipay.sofa.registry.task.listener.TaskListener;

/**
 *
 * @author kezhu.wukz
 * @version $Id: PublishDataTaskListener.java, v 0.1 2019-06-17 12:02 kezhu.wukz Exp $
 */
public class PublishDataTaskListener implements TaskListener {

    @Autowired
    private DataNodeService dataNodeService;

    @Autowired
    private TaskProcessor   dataNodeSingleTaskProcessor;

    @Autowired
    private ExecutorManager executorManager;

    @Override
    public TaskType support() {
        return TaskType.PUBLISH_DATA_TASK;
    }

    @Override
    public void handleEvent(TaskEvent event) {

        SessionTask publishDataTask = new PublishDataTask(dataNodeService);

        publishDataTask.setTaskEvent(event);

        executorManager.getPublishDataExecutor().execute(()-> dataNodeSingleTaskProcessor.process(publishDataTask));
    }
}