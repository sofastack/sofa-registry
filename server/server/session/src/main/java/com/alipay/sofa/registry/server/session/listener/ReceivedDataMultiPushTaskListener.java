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

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.node.service.ClientNodeService;
import com.alipay.sofa.registry.server.session.scheduler.ExecutorManager;
import com.alipay.sofa.registry.server.session.scheduler.task.ReceivedDataMultiPushTask;
import com.alipay.sofa.registry.server.session.scheduler.task.SessionTask;
import com.alipay.sofa.registry.server.session.strategy.ReceivedDataMultiPushTaskStrategy;
import com.alipay.sofa.registry.server.session.strategy.TaskMergeProcessorStrategy;
import com.alipay.sofa.registry.task.batcher.TaskProcessor;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.task.listener.TaskEvent.TaskType;
import com.alipay.sofa.registry.task.listener.TaskListener;
import com.alipay.sofa.registry.timer.AsyncHashedWheelTimer;
import com.alipay.sofa.registry.timer.AsyncHashedWheelTimer.TaskFailedCallback;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author shangyu.wh
 * @version $Id: SubscriberRegisterPushTaskListener.java, v 0.1 2017-12-11 20:44 shangyu.wh Exp $
 */
public class ReceivedDataMultiPushTaskListener implements TaskListener, PushTaskSender {

    private static final Logger               LOGGER = LoggerFactory
                                                         .getLogger(ReceivedDataMultiPushTaskListener.class);

    @Autowired
    private SessionServerConfig               sessionServerConfig;

    @Autowired
    private ClientNodeService                 clientNodeService;

    @Autowired
    private ExecutorManager                   executorManager;

    @Autowired
    private Exchange                          boltExchange;

    @Autowired
    private ReceivedDataMultiPushTaskStrategy receivedDataMultiPushTaskStrategy;

    private TaskMergeProcessorStrategy        receiveDataTaskMergeProcessorStrategy;

    private TaskProcessor                     clientNodeSingleTaskProcessor;

    private AsyncHashedWheelTimer             asyncHashedWheelTimer;

    public ReceivedDataMultiPushTaskListener(TaskProcessor clientNodeSingleTaskProcessor,
                                             TaskMergeProcessorStrategy receiveDataTaskMergeProcessorStrategy,
                                             SessionServerConfig sessionServerConfig) {
        this.clientNodeSingleTaskProcessor = clientNodeSingleTaskProcessor;

        ThreadFactoryBuilder threadFactoryBuilder = new ThreadFactoryBuilder();
        threadFactoryBuilder.setDaemon(true);
        asyncHashedWheelTimer = new AsyncHashedWheelTimer(threadFactoryBuilder.setNameFormat(
            "Registry-ReceivedDataPushTask-WheelTimer").build(),
            sessionServerConfig.getUserDataPushRetryWheelTicksDuration(), TimeUnit.MILLISECONDS,
            sessionServerConfig.getUserDataPushRetryWheelTicksSize(), threadFactoryBuilder
                .setNameFormat("Registry-ReceivedDataPushTask-WheelExecutor-%d").build(),
            new TaskFailedCallback() {
                @Override
                public void executionRejected(Throwable e) {
                    LOGGER.error("executionRejected: " + e.getMessage(), e);
                }

                @Override
                public void executionFailed(Throwable e) {
                    LOGGER.error("executionFailed: " + e.getMessage(), e);
                }
            });

        receiveDataTaskMergeProcessorStrategy.init(this);
        this.receiveDataTaskMergeProcessorStrategy = receiveDataTaskMergeProcessorStrategy;
    }

    @Override
    public boolean support(TaskEvent event) {
        return TaskType.RECEIVED_DATA_MULTI_PUSH_TASK.equals(event.getTaskType());
    }

    @Override
    public void handleEvent(TaskEvent event) {
        receiveDataTaskMergeProcessorStrategy.handleEvent(event);
    }

    @Override
    public void executePushAsync(TaskEvent event) {

        SessionTask receivedDataMultiPushTask = new ReceivedDataMultiPushTask(sessionServerConfig, clientNodeService,
                executorManager, boltExchange, receivedDataMultiPushTaskStrategy,asyncHashedWheelTimer);
        receivedDataMultiPushTask.setTaskEvent(event);

        executorManager.getPushTaskExecutor()
                .execute(() -> clientNodeSingleTaskProcessor.process(receivedDataMultiPushTask));
    }

    @Override
    public PushDataType getPushDataType() {
        return PushDataType.RECEIVE_DATA;
    }

    /**
     * Getter method for property <tt>taskMergeProcessorStrategy</tt>.
     *
     * @return property value of taskMergeProcessorStrategy
     */
    public TaskMergeProcessorStrategy getTaskMergeProcessorStrategy() {
        return receiveDataTaskMergeProcessorStrategy;
    }
}