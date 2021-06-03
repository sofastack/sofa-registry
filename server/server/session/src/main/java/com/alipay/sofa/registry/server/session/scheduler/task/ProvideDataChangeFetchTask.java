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
package com.alipay.sofa.registry.server.session.scheduler.task;

import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.common.model.metaserver.ProvideDataChangeEvent;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.common.model.store.Watcher;
import com.alipay.sofa.registry.core.model.ReceivedConfigData;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.converter.ReceivedDataConverter;
import com.alipay.sofa.registry.server.session.store.Watchers;
import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
import com.alipay.sofa.registry.server.shared.providedata.ProvideDataProcessor;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.task.listener.TaskEvent.TaskType;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author shangyu.wh
 * @version $Id: SubscriberRegisterFetchTask.java, v 0.1 2017-12-07 16:23 shangyu.wh Exp $
 */
public class ProvideDataChangeFetchTask extends AbstractSessionTask {

  private static final Logger TASK_LOGGER =
      LoggerFactory.getLogger(ProvideDataChangeFetchTask.class, "[Task]");

  private static final Logger LOGGER = LoggerFactory.getLogger(ProvideDataChangeFetchTask.class);

  private final SessionServerConfig sessionServerConfig;
  /** trigger push client process */
  private final TaskListenerManager taskListenerManager;
  /** Meta Node service */
  private final MetaServerService metaServerService;

  private final Watchers sessionWatchers;

  private final Exchange boltExchange;

  private ProvideDataChangeEvent provideDataChangeEvent;

  private ProvideDataProcessor provideDataProcessorManager;

  public ProvideDataChangeFetchTask(
      SessionServerConfig sessionServerConfig,
      TaskListenerManager taskListenerManager,
      MetaServerService metaServerService,
      Watchers sessionWatchers,
      Exchange boltExchange,
      ProvideDataProcessor provideDataProcessorManager) {
    this.sessionServerConfig = sessionServerConfig;
    this.taskListenerManager = taskListenerManager;
    this.metaServerService = metaServerService;
    this.sessionWatchers = sessionWatchers;
    this.boltExchange = boltExchange;
    this.provideDataProcessorManager = provideDataProcessorManager;
  }

  @Override
  public void setTaskEvent(TaskEvent taskEvent) {
    // taskId create from event
    if (taskEvent.getTaskId() != null) {
      setTaskId(taskEvent.getTaskId());
    }

    Object obj = taskEvent.getEventObj();

    if (!(obj instanceof ProvideDataChangeEvent)) {
      throw new IllegalArgumentException("Input task event object error!");
    }

    this.provideDataChangeEvent = (ProvideDataChangeEvent) obj;
  }

  @Override
  public void execute() {

    ProvideData provideData = null;
    String dataInfoId = provideDataChangeEvent.getDataInfoId();
    provideData = metaServerService.fetchData(dataInfoId);

    if (provideData == null) {
      LOGGER.warn(
          "Notify provider data Change request {} fetch no provider data!", provideDataChangeEvent);
      return;
    }
    provideDataProcessorManager.processorData(provideData);

    DataInfo dataInfo = DataInfo.valueOf(dataInfoId);

    Server sessionServer = boltExchange.getServer(sessionServerConfig.getServerPort());
    if (sessionServer != null) {
      for (Channel channel : sessionServer.getChannels()) {

        // filter all connect client has watcher registerId
        ConnectId connectId = ConnectId.of(channel.getRemoteAddress(), channel.getLocalAddress());
        Map<String, Watcher> map = getCache(connectId);
        List<String> registerIds = new ArrayList<>();
        map.forEach(
            (registerId, watchers) -> {
              if (watchers != null && watchers.getDataInfoId().equals(dataInfoId)) {
                registerIds.add(registerId);
              }
            });
        if (!registerIds.isEmpty()) {
          ReceivedConfigData receivedConfigData;
          receivedConfigData =
              ReceivedDataConverter.getReceivedConfigData(
                  provideData.getProvideData(), dataInfo, provideData.getVersion());
          receivedConfigData.setConfiguratorRegistIds(registerIds);
          firePushTask(receivedConfigData, new URL(channel.getRemoteAddress()));
        }
      }
    }
  }

  private void firePushTask(ReceivedConfigData receivedConfigData, URL clientUrl) {

    Map<ReceivedConfigData, URL> parameter = new HashMap<>();
    parameter.put(receivedConfigData, clientUrl);
    TaskEvent taskEvent = new TaskEvent(parameter, TaskType.RECEIVED_DATA_CONFIG_PUSH_TASK);
    TASK_LOGGER.info("send " + taskEvent.getTaskType() + " taskEvent:{}", taskEvent);
    taskListenerManager.sendTaskEvent(taskEvent);
  }

  private Map<String /*registerId*/, Watcher> getCache(ConnectId connectId) {
    Map<String /*registerId*/, Watcher> map = sessionWatchers.queryByConnectId(connectId);
    return map == null ? new ConcurrentHashMap<>() : map;
  }

  @Override
  public boolean checkRetryTimes() {
    return checkRetryTimes(sessionServerConfig.getSubscriberRegisterFetchRetryTimes());
  }

  @Override
  public String toString() {
    return "PROVIDE_DATA_CHANGE_FETCH_TASK{"
        + "taskId='"
        + getTaskId()
        + '\''
        + ", notifyProvideDataChange="
        + provideDataChangeEvent
        + ", retryTimes='"
        + sessionServerConfig.getSubscriberRegisterFetchRetryTimes()
        + '\''
        + '}';
  }
}
