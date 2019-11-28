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
package com.alipay.sofa.registry.server.meta.test.confirm;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.test.confirm.DataServerConfirmTest.NodeTest;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.task.listener.TaskEvent.TaskType;
import com.alipay.sofa.registry.task.listener.TaskListener;

/**
 *
 * @author shangyu.wh
 * @version $Id: ReceiveStatusConfirmNotifyTaskMock.java, v 0.1 2018-03-28 16:38 shangyu.wh Exp $
 */
public class ReceiveStatusConfirmNotifyTaskMock implements TaskListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(
                                           ReceiveStatusConfirmNotifyTaskMock.class, "[Task]");

    @Override
    public TaskType support() {
        return TaskType.RECEIVE_STATUS_CONFIRM_NOTIFY_TASK;
    }

    @Override
    public void handleEvent(TaskEvent event) {
        Object obj = event.getEventObj();

        if (obj instanceof NodeTest) {
            ((NodeTest) obj).getAllDone().countDown();
            LOGGER.info("Notify:{}", ((NodeTest) obj));
        } else {
            throw new IllegalArgumentException("Input task event object error!");
        }
    }
}