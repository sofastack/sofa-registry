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

import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.scheduler.task.ProvideDataChangeFetchTask;
import com.alipay.sofa.registry.server.session.scheduler.task.SessionTask;
import com.alipay.sofa.registry.server.session.store.Watchers;
import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
import com.alipay.sofa.registry.server.shared.providedata.ProvideDataProcessor;
import com.alipay.sofa.registry.task.batcher.TaskDispatcher;
import com.alipay.sofa.registry.task.batcher.TaskDispatchers;
import com.alipay.sofa.registry.task.batcher.TaskProcessor;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.task.listener.TaskEvent.TaskType;
import com.alipay.sofa.registry.task.listener.TaskListener;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author shangyu.wh
 * @version $Id: SubscriberRegisterFetchTaskListener.java, v 0.1 2017-12-07 19:53 shangyu.wh Exp $
 */
public class ProvideDataChangeFetchTaskListener implements TaskListener {

  @Autowired SessionServerConfig sessionServerConfig;

  /** trigger push client process */
  @Autowired TaskListenerManager taskListenerManager;

  /** MetaNode service */
  @Autowired MetaServerService metaServerService;

  @Autowired Exchange boltExchange;

  @Autowired Watchers sessionWatchers;

  @Autowired ProvideDataProcessor provideDataProcessorManager;

  private TaskDispatcher<String, SessionTask> singleTaskDispatcher;

  private final TaskProcessor dataNodeSingleTaskProcessor;

  public ProvideDataChangeFetchTaskListener(TaskProcessor dataNodeSingleTaskProcessor) {
    this.dataNodeSingleTaskProcessor = dataNodeSingleTaskProcessor;
  }

  @PostConstruct
  public void init() {
    singleTaskDispatcher =
        TaskDispatchers.createDefaultSingleTaskDispatcher(
            TaskType.PROVIDE_DATA_CHANGE_FETCH_TASK.getName(), dataNodeSingleTaskProcessor);
  }

  @Override
  public TaskType support() {
    return TaskType.PROVIDE_DATA_CHANGE_FETCH_TASK;
  }

  @Override
  public void handleEvent(TaskEvent event) {

    SessionTask provideDataChangeFetchTask =
        new ProvideDataChangeFetchTask(
            sessionServerConfig,
            taskListenerManager,
            metaServerService,
            sessionWatchers,
            boltExchange,
            provideDataProcessorManager);

    provideDataChangeFetchTask.setTaskEvent(event);

    singleTaskDispatcher.dispatch(
        provideDataChangeFetchTask.getTaskId(),
        provideDataChangeFetchTask,
        provideDataChangeFetchTask.getExpiryTime());
  }
}
