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
package com.alipay.sofa.registry.server.session.strategy.impl;

import com.alipay.sofa.registry.common.model.sessionserver.DataChangeRequest;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.remoting.handler.DataChangeRequestHandler;
import com.alipay.sofa.registry.server.session.strategy.DataChangeRequestHandlerStrategy;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xuanbei
 * @since 2019/2/15
 */
public class DefaultDataChangeRequestHandlerStrategy implements DataChangeRequestHandlerStrategy {
    private static final Logger taskLogger = LoggerFactory.getLogger(
                                               DataChangeRequestHandler.class, "[Task]");

    @Autowired
    private TaskListenerManager taskListenerManager;

    @Override
    public void doFireChangFetch(DataChangeRequest dataChangeRequest) {
        TaskEvent taskEvent = new TaskEvent(dataChangeRequest,
            TaskEvent.TaskType.DATA_CHANGE_FETCH_TASK);
        taskLogger.info("send " + taskEvent.getTaskType() + " taskEvent:{}", taskEvent);
        taskListenerManager.sendTaskEvent(taskEvent);
    }

}
