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
package com.alipay.sofa.registry.server.session.acceptor;

import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.task.listener.TaskEvent.TaskType;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;

/**
 *
 * @author kezhu.wukz
 * @author shangyu.wh
 * @version 1.0: WriteDataProcessor.java, v 0.1 2019-06-06 12:50 shangyu.wh Exp $
 */
public class WriteDataProcessor {

    private static final Logger       LOGGER = LoggerFactory.getLogger(WriteDataProcessor.class);

    private final TaskListenerManager taskListenerManager;

    private final ConnectId           connectId;

    public WriteDataProcessor(ConnectId connectId, TaskListenerManager taskListenerManager) {
        this.connectId = connectId;
        this.taskListenerManager = taskListenerManager;
    }

    public void process(WriteDataRequest request) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("process: connectId={}, requestType={}, requestBody={}", connectId,
                request.getRequestType(), request.getRequestBody());
        }
        doHandle(request);
    }

    private void doHandle(WriteDataRequest request) {
        switch (request.getRequestType()) {
            case PUBLISHER: {
                doPublishAsync(request);
            }
                break;
            case UN_PUBLISHER: {
                doUnPublishAsync(request);
            }
                break;
            case CLIENT_OFF: {
                doClientOffAsync(request);
            }
                break;
            default:
                LOGGER.warn("Unknown request type, connectId={}, requestType={}, requestBody={}",
                    connectId, request.getRequestType(), request.getRequestBody());

        }
    }

    private void doClientOffAsync(WriteDataRequest request) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("doClientOffAsync: connectId={}, requestType={}, requestBody={}",
                connectId, request.getRequestType(), request.getRequestBody());
        }

        sendEvent(request.getRequestBody(), TaskType.CANCEL_DATA_TASK);
    }

    private void doUnPublishAsync(WriteDataRequest request) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("doUnPublishAsync: connectId={}, requestType={}, requestBody={}",
                connectId, request.getRequestType(), request.getRequestBody());
        }

        sendEvent(request.getRequestBody(), TaskType.UN_PUBLISH_DATA_TASK);
    }

    private void doPublishAsync(WriteDataRequest request) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("doPublishAsync: connectId={}, requestType={}, requestBody={}", connectId,
                request.getRequestType(), request.getRequestBody());
        }

        sendEvent(request.getRequestBody(), TaskType.PUBLISH_DATA_TASK);
    }

    private void sendEvent(Object eventObj, TaskType taskType) {
        TaskEvent taskEvent = new TaskEvent(eventObj, taskType);
        taskListenerManager.sendTaskEvent(taskEvent);
    }

}