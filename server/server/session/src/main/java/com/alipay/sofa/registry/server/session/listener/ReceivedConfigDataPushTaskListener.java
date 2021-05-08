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
import com.alipay.sofa.registry.server.session.node.service.ClientNodeService;
import com.alipay.sofa.registry.server.session.push.PushSwitchService;
import com.alipay.sofa.registry.server.session.scheduler.task.ReceivedConfigDataPushTask;
import com.alipay.sofa.registry.server.session.scheduler.task.SessionTask;
import com.alipay.sofa.registry.server.session.strategy.ReceivedConfigDataPushTaskStrategy;
import com.alipay.sofa.registry.task.batcher.TaskDispatcher;
import com.alipay.sofa.registry.task.batcher.TaskDispatchers;
import com.alipay.sofa.registry.task.batcher.TaskProcessor;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.task.listener.TaskEvent.TaskType;
import com.alipay.sofa.registry.task.listener.TaskListener;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author shangyu.wh
 * @version $Id: SubscriberRegisterPushTaskListener.java, v 0.1 2017-12-11 20:44 shangyu.wh Exp $
 */
public class ReceivedConfigDataPushTaskListener implements TaskListener {

  @Autowired private SessionServerConfig sessionServerConfig;

  @Autowired private PushSwitchService pushSwitchService;

  @Autowired private ClientNodeService clientNodeService;

  @Autowired private ReceivedConfigDataPushTaskStrategy receivedConfigDataPushTaskStrategy;

  private volatile TaskDispatcher<String, SessionTask> singleTaskDispatcher;

  private TaskProcessor clientNodeSingleTaskProcessor;

  public ReceivedConfigDataPushTaskListener(TaskProcessor clientNodeSingleTaskProcessor) {

    this.clientNodeSingleTaskProcessor = clientNodeSingleTaskProcessor;
  }

  public TaskDispatcher<String, SessionTask> getSingleTaskDispatcher() {
    if (singleTaskDispatcher == null) {
      synchronized (this) {
        if (singleTaskDispatcher == null) {
          singleTaskDispatcher =
              TaskDispatchers.createDefaultSingleTaskDispatcher(
                  TaskType.RECEIVED_DATA_CONFIG_PUSH_TASK.getName(), clientNodeSingleTaskProcessor);
        }
      }
    }
    return singleTaskDispatcher;
  }

  @Override
  public TaskType support() {
    return TaskType.RECEIVED_DATA_CONFIG_PUSH_TASK;
  }

  @Override
  public void handleEvent(TaskEvent event) {

    SessionTask receivedConfigDataPushTask =
        new ReceivedConfigDataPushTask(
            sessionServerConfig,
            pushSwitchService,
            clientNodeService,
            receivedConfigDataPushTaskStrategy);
    receivedConfigDataPushTask.setTaskEvent(event);
    getSingleTaskDispatcher()
        .dispatch(
            receivedConfigDataPushTask.getTaskId(),
            receivedConfigDataPushTask,
            receivedConfigDataPushTask.getExpiryTime());
  }
}
