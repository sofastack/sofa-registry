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
package com.alipay.sofa.registry.server.meta.listener;

import com.alipay.sofa.registry.server.meta.bootstrap.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.node.DataNodeService;
import com.alipay.sofa.registry.server.meta.node.NodeService;
import com.alipay.sofa.registry.server.meta.task.MetaServerTask;
import com.alipay.sofa.registry.server.meta.task.ReceiveStatusConfirmNotifyTask;
import com.alipay.sofa.registry.task.batcher.TaskDispatcher;
import com.alipay.sofa.registry.task.batcher.TaskDispatchers;
import com.alipay.sofa.registry.task.batcher.TaskProcessor;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.task.listener.TaskEvent.TaskType;
import com.alipay.sofa.registry.task.listener.TaskListener;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author shangyu.wh
 * @version $Id: ReceiveStatusConfirmNotifyTaskListener.java, v 0.1 2018-03-24 17:34 shangyu.wh Exp $
 */
public class ReceiveStatusConfirmNotifyTaskListener implements TaskListener {

    @Autowired
    NodeService                                    dataNodeService;
    @Autowired
    private MetaServerConfig                       metaServerConfig;
    private TaskDispatcher<String, MetaServerTask> singleTaskDispatcher;

    /**
     * constructor
     * @param dataNodeSingleTaskProcessor
     */
    public ReceiveStatusConfirmNotifyTaskListener(TaskProcessor dataNodeSingleTaskProcessor) {

        singleTaskDispatcher = TaskDispatchers.createDefaultSingleTaskDispatcher(
            TaskType.RECEIVE_STATUS_CONFIRM_NOTIFY_TASK.getName(), dataNodeSingleTaskProcessor);
    }

    @Override
    public boolean support(TaskEvent event) {
        return TaskType.RECEIVE_STATUS_CONFIRM_NOTIFY_TASK.equals(event.getTaskType());
    }

    @Override
    public void handleEvent(TaskEvent event) {
        MetaServerTask receiveStatusConfirmNotifyTask = new ReceiveStatusConfirmNotifyTask(
            (DataNodeService) dataNodeService, metaServerConfig);
        receiveStatusConfirmNotifyTask.setTaskEvent(event);
        singleTaskDispatcher.dispatch(receiveStatusConfirmNotifyTask.getTaskId(),
            receiveStatusConfirmNotifyTask, receiveStatusConfirmNotifyTask.getExpiryTime());
    }
}