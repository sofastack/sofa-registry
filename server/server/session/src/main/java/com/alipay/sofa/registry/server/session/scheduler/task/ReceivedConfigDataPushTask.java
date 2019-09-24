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

import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.core.model.ReceivedConfigData;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.CallbackHandler;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.node.service.ClientNodeService;
import com.alipay.sofa.registry.server.session.strategy.ReceivedConfigDataPushTaskStrategy;
import com.alipay.sofa.registry.task.listener.TaskEvent;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;

/**
 *
 * @author shangyu.wh
 * @version $Id: SubscriberRegisterPushTask.java, v 0.1 2017-12-11 20:57 shangyu.wh Exp $
 */
public class ReceivedConfigDataPushTask extends AbstractSessionTask {

    private static final Logger                LOGGER = LoggerFactory.getLogger(
                                                          ReceivedConfigDataPushTask.class,
                                                          "[Task]");

    private final SessionServerConfig          sessionServerConfig;
    private final ClientNodeService            clientNodeService;
    private ReceivedConfigData                 receivedConfigData;
    private URL                                url;
    private ReceivedConfigDataPushTaskStrategy receivedConfigDataPushTaskStrategy;

    public ReceivedConfigDataPushTask(SessionServerConfig sessionServerConfig,
                                      ClientNodeService clientNodeService,
                                      ReceivedConfigDataPushTaskStrategy receivedConfigDataPushTaskStrategy) {
        this.sessionServerConfig = sessionServerConfig;
        this.clientNodeService = clientNodeService;
        this.receivedConfigDataPushTaskStrategy = receivedConfigDataPushTaskStrategy;
    }

    @Override
    public void execute() {

        if (sessionServerConfig.isStopPushSwitch()) {
            LOGGER
                .info(
                    "Stop Push receivedConfigData with switch on! dataId: {},group: {},Instance: {}, url: {}",
                    receivedConfigData.getDataId(), receivedConfigData.getGroup(),
                    receivedConfigData.getInstanceId(), url);
            return;
        }

        CallbackHandler callbackHandler = new CallbackHandler() {
            @Override
            public void onCallback(Channel channel, Object message) {
                LOGGER.info(
                    "Push receivedConfigData success! dataId: {},group: {},Instance: {}, url: {}",
                    receivedConfigData.getDataId(), receivedConfigData.getGroup(),
                    receivedConfigData.getInstanceId(), url);
            }

            @Override
            public void onException(Channel channel, Throwable exception) {
                LOGGER.error(
                    "Push receivedConfigData error! dataId: {},group: {},Instance: {}, url: {}",
                    receivedConfigData.getDataId(), receivedConfigData.getGroup(),
                    receivedConfigData.getInstanceId(), url);
            }

            @Override
            public Executor getExecutor() {
                return null;
            }
        };

        clientNodeService.pushWithCallback(
            receivedConfigDataPushTaskStrategy.convert2PushData(receivedConfigData, url), url,
            callbackHandler);
    }

    @Override
    public long getExpiryTime() {
        //TODO CONFIG
        return -1;
    }

    @Override
    public void setTaskEvent(TaskEvent taskEvent) {

        //taskId create from event
        if (taskEvent.getTaskId() != null) {
            setTaskId(taskEvent.getTaskId());
        }

        Object obj = taskEvent.getEventObj();

        if (obj instanceof Map) {

            Map<ReceivedConfigData, URL> parameter = (Map<ReceivedConfigData, URL>) obj;

            if (parameter.size() == 1) {

                Entry<ReceivedConfigData, URL> entry = (parameter.entrySet()).iterator().next();
                ReceivedConfigData receivedData = entry.getKey();
                URL url = entry.getValue();

                this.receivedConfigData = receivedData;
                this.url = url;
            } else {
                throw new IllegalArgumentException("Input task event object error!");
            }
        }
    }

    @Override
    public String toString() {
        return "RECEIVED_DATA_CONFIG_PUSH_TASK{" + "taskId='" + getTaskId() + '\''
               + ", receivedConfigData=" + receivedConfigData + ", url=" + url + ", retry='"
               + sessionServerConfig.getReceivedDataMultiPushTaskRetryTimes() + '\'' + '}';
    }

    @Override
    public boolean checkRetryTimes() {
        return checkRetryTimes(sessionServerConfig.getReceivedDataMultiPushTaskRetryTimes());
    }
}