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

import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.common.model.store.BaseInfo.ClientVersion;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.common.model.store.Watcher;
import com.alipay.sofa.registry.core.model.ReceivedConfigData;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.converter.ReceivedDataConverter;
import com.alipay.sofa.registry.server.session.node.service.MetaNodeService;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.task.listener.TaskEvent.TaskType;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author shangyu.wh
 * @version $Id: SubscriberRegisterFetchTask.java, v 0.1 2017-12-07 16:23 shangyu.wh Exp $
 */
public class WatcherRegisterFetchTask extends AbstractSessionTask {

    private static final Logger       taskLogger = LoggerFactory.getLogger(
                                                     WatcherRegisterFetchTask.class, "[Task]");

    private final SessionServerConfig sessionServerConfig;
    /**
     * trigger push client process
     */
    private final TaskListenerManager taskListenerManager;
    /**
     * Meta Node service
     */
    private final MetaNodeService     metaNodeService;

    private Watcher                   watcher;

    private static final int          TRY_COUNT  = 3;

    public WatcherRegisterFetchTask(SessionServerConfig sessionServerConfig,
                                    TaskListenerManager taskListenerManager,
                                    MetaNodeService metaNodeService) {
        this.sessionServerConfig = sessionServerConfig;
        this.taskListenerManager = taskListenerManager;
        this.metaNodeService = metaNodeService;
    }

    @Override
    public void setTaskEvent(TaskEvent taskEvent) {
        Object obj = taskEvent.getEventObj();

        if (!(obj instanceof Watcher)) {
            throw new IllegalArgumentException("Input task event object error!");
        }

        this.watcher = (Watcher) obj;
    }

    @Override
    public void execute() {

        if (watcher == null) {
            throw new IllegalArgumentException("watcher can not be null!");
        }

        List<String> subscriberRegisterIdList = Collections.singletonList(watcher.getRegisterId());

        boolean isOldVersion = !ClientVersion.StoreData.equals(watcher.getClientVersion());

        ProvideData provideData = null;

        for (int tryCount = 0; tryCount < TRY_COUNT; tryCount++) {
            try {
                provideData = metaNodeService.fetchData(watcher.getDataInfoId());
                break;
            } catch (Exception e) {
                randomDelay(3000);
            }
        }

        if (provideData == null) {
            taskLogger.error("Fetch provider data error,set null value return.dataInfoId={}", watcher.getDataId());
            provideData = new ProvideData(null, watcher.getDataInfoId(), null);
        }

        if (!isOldVersion) {
            DataInfo dataInfo = DataInfo.valueOf(provideData.getDataInfoId());
            ReceivedConfigData receivedConfigData = ReceivedDataConverter
                .getReceivedConfigData(provideData.getProvideData(), dataInfo,
                    provideData.getVersion());
            receivedConfigData.setConfiguratorRegistIds(subscriberRegisterIdList);
            firePushTask(receivedConfigData);
        }
    }

    private void firePushTask(ReceivedConfigData receivedConfigData) {
        Map<ReceivedConfigData, URL> parameter = new HashMap<>();
        parameter.put(receivedConfigData, watcher.getSourceAddress());
        TaskEvent taskEvent = new TaskEvent(parameter, TaskType.RECEIVED_DATA_CONFIG_PUSH_TASK);
        taskLogger.info("send " + taskEvent.getTaskType() + " taskEvent:{}", taskEvent);
        taskListenerManager.sendTaskEvent(taskEvent);
    }

    @Override
    public boolean checkRetryTimes() {
        return checkRetryTimes(sessionServerConfig.getSubscriberRegisterFetchRetryTimes());
    }

    private void randomDelay(int max) {
        Random random = new Random();
        int randomNum = random.nextInt(max);
        try {
            Thread.sleep(randomNum);
        } catch (InterruptedException e) {
            taskLogger.error("[TimeUtil] random delay error", e);
        }
    }

    @Override
    public String toString() {
        return "WATCHER_REGISTER_FETCH_TASK{" + "taskId='" + getTaskId() + '\'' + ", watcher="
               + watcher + ", retryTimes='"
               + sessionServerConfig.getSubscriberRegisterFetchRetryTimes() + '\'' + '}';
    }
}