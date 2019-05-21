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

import com.alipay.sofa.registry.common.model.PushDataRetryRequest;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.core.model.DataBox;
import com.alipay.sofa.registry.core.model.ReceivedData;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.CallbackHandler;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.node.service.ClientNodeService;
import com.alipay.sofa.registry.server.session.scheduler.ExecutorManager;
import com.alipay.sofa.registry.server.session.strategy.ReceivedDataMultiPushTaskStrategy;
import com.alipay.sofa.registry.task.Task;
import com.alipay.sofa.registry.task.TaskClosure;
import com.alipay.sofa.registry.task.batcher.TaskProcessor.ProcessingResult;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.timer.AsyncHashedWheelTimer;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author shangyu.wh
 * @version $Id: SubscriberRegisterPushTask.java, v 0.1 2017-12-11 20:57 shangyu.wh Exp $
 */
public class ReceivedDataMultiPushTask extends AbstractSessionTask implements TaskClosure {

    private static final Logger               LOGGER = LoggerFactory.getLogger("SESSION-PUSH",
                                                         "[Receive]");

    private final SessionServerConfig         sessionServerConfig;
    private final ClientNodeService           clientNodeService;
    private final ExecutorManager             executorManager;
    private final Exchange                    boltExchange;
    private ReceivedData                      receivedData;
    private URL                               url;
    private TaskClosure                       taskClosure;
    private Collection<Subscriber>            subscribers;
    private ReceivedDataMultiPushTaskStrategy receivedDataMultiPushTaskStrategy;
    private AsyncHashedWheelTimer             asyncHashedWheelTimer;

    private String                            dataPush;

    public ReceivedDataMultiPushTask(SessionServerConfig sessionServerConfig,
                                     ClientNodeService clientNodeService,
                                     ExecutorManager executorManager,
                                     Exchange boltExchange,
                                     ReceivedDataMultiPushTaskStrategy receivedDataMultiPushTaskStrategy,
                                     AsyncHashedWheelTimer asyncHashedWheelTimer) {
        this.sessionServerConfig = sessionServerConfig;
        this.clientNodeService = clientNodeService;
        this.executorManager = executorManager;
        this.boltExchange = boltExchange;
        this.receivedDataMultiPushTaskStrategy = receivedDataMultiPushTaskStrategy;
        this.asyncHashedWheelTimer = asyncHashedWheelTimer;
    }

    @Override
    public void execute() {

        if (sessionServerConfig.isStopPushSwitch()) {
            LOGGER
                .info(
                    "Stop Push ReceivedData with switch on! dataId: {},group: {},Instance: {}, url: {}",
                    receivedData.getDataId(), receivedData.getGroup(),
                    receivedData.getInstanceId(), url);
            return;
        }

        Object receivedDataPush = receivedDataMultiPushTaskStrategy.convert2PushData(receivedData,
            url);

        CallbackHandler callbackHandler = new CallbackHandler() {
            @Override
            public void onCallback(Channel channel, Object message) {
                LOGGER
                    .info(
                        "Push ReceivedData success! dataId:{},group:{},Instance:{},version:{},url: {},dataPush:{}",
                        receivedData.getDataId(), receivedData.getGroup(),
                        receivedData.getInstanceId(), receivedData.getVersion(), url, dataPush);

                if (taskClosure != null) {
                    confirmCallBack(true);
                }
            }

            @Override
            public void onException(Channel channel, Throwable exception) {
                LOGGER
                    .error(
                        "Push ReceivedData error! dataId:{},group:{},Instance:{},version:{},url: {},dataPush:{}",
                        receivedData.getDataId(), receivedData.getGroup(),
                        receivedData.getInstanceId(), receivedData.getVersion(), url, dataPush,
                        exception);

                if (taskClosure != null) {
                    confirmCallBack(false);
                    throw new RuntimeException("Push ReceivedData got exception from callback!");
                } else {
                    retrySendReceiveData(new PushDataRetryRequest(receivedDataPush, url));
                }
            }
        };

        try {
            clientNodeService.pushWithCallback(receivedDataPush, url, callbackHandler);
        } catch (Exception e) {
            if (taskClosure != null) {
                confirmCallBack(false);
                throw e;
            } else {
                retrySendReceiveData(new PushDataRetryRequest(receivedDataPush, url));
            }
        }
    }

    private void retrySendReceiveData(PushDataRetryRequest pushDataRetryRequest) {
        ///taskClosure null means send task need not confirm
        if (taskClosure == null) {

            Object infoPackage = pushDataRetryRequest.getPushObj();

            int retryTimes = pushDataRetryRequest.getRetryTimes().incrementAndGet();

            URL targetUrl = pushDataRetryRequest.getUrl();

            if (checkRetryTimes(retryTimes)) {
                Server sessionServer = boltExchange.getServer(sessionServerConfig.getServerPort());

                Channel channel = sessionServer.getChannel(targetUrl);

                if (channel != null && channel.isConnected()) {

                    asyncHashedWheelTimer.newTimeout(timeout ->  {
                        try {
                            clientNodeService.pushWithCallback(infoPackage, targetUrl, new CallbackHandler() {
                                @Override
                                public void onCallback(Channel channel, Object message) {
                                    LOGGER.info("Retry Push ReceivedData success! dataId:{}, group:{},url:{},taskId:{},dataPush:{},retryTimes:{}",
                                            receivedData.getDataId(), receivedData.getGroup(), targetUrl,getTaskId(),dataPush,retryTimes);
                                }

                                @Override
                                public void onException(Channel channel, Throwable exception) {
                                    LOGGER.error("Retry Push ReceivedData callback error! url:{}, dataId:{}, group:{},taskId:{},dataPush:{},retryTimes:{}", targetUrl,
                                            receivedData.getDataId(), receivedData.getGroup(),getTaskId(),dataPush,retryTimes);
                                    retrySendReceiveData(pushDataRetryRequest);
                                }
                            });

                        } catch (Exception e) {
                            LOGGER.error("Retry Push ReceivedData error! url:{}, dataId:{}, group:{},taskId:{},dataPush:{},retryTimes:{}", targetUrl,
                                    receivedData.getDataId(), receivedData.getGroup(),getTaskId(),dataPush,retryTimes);
                            retrySendReceiveData(pushDataRetryRequest);
                        }
                    },getBlockTime(retryTimes),TimeUnit.MILLISECONDS);
                } else {
                    LOGGER.error("Retry Push ReceivedData error, connect be null or disconnected,stop retry!dataId:{}, group:{},url:{},taskId:{},dataPush:{},retryTimes:{}",
                            receivedData.getDataId(), receivedData.getGroup(), targetUrl,getTaskId(),dataPush,retryTimes);
                }
            } else {
                LOGGER.error("Retry Push ReceivedData times have exceeded!dataId:{}, group:{},url:{},taskId:{},dataPush:{},retryTimes:{}",
                        receivedData.getDataId(), receivedData.getGroup(), targetUrl,getTaskId(),dataPush,retryTimes);
            }
        }
    }

    @Override
    public long getExpiryTime() {
        //TODO CONFIG
        return -1;
    }

    @Override
    public void setTaskEvent(TaskEvent taskEvent) {
        Object obj = taskEvent.getEventObj();

        if (obj instanceof Map) {

            Map<ReceivedData, URL> parameter = (Map<ReceivedData, URL>) obj;

            if (parameter.size() == 1) {

                Entry<ReceivedData, URL> entry = (parameter.entrySet()).iterator().next();
                ReceivedData receivedData = entry.getKey();
                URL url = entry.getValue();

                this.receivedData = receivedData;
                this.url = url;
            } else {
                throw new IllegalArgumentException("Input task event object error!");
            }
        }

        if (receivedData != null && receivedData.getData() != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            Map<String, List<DataBox>> map = receivedData.getData();
            if (!map.isEmpty()) {

                for (Map.Entry<String, List<DataBox>> entry1 : map.entrySet()) {
                    sb.append(entry1.getKey()).append("=");
                    int size = entry1.getValue() != null ? entry1.getValue().size() : 0;
                    sb.append(size).append(",");
                }
            }
            sb.append("]");
            dataPush = sb.toString();
        }

        taskClosure = taskEvent.getTaskClosure();

        if (taskClosure instanceof PushTaskClosure) {
            ((PushTaskClosure) taskClosure).addTask(this);
        }

        subscribers = (Collection<Subscriber>) taskEvent
            .getAttribute(Constant.PUSH_CLIENT_SUBSCRIBERS);
        //taskClosure must confirm all subscriber push success
        if (taskClosure != null && subscribers.isEmpty()) {
            LOGGER.error("send Receive data subscribers is empty!");
            throw new RuntimeException("Push Receive data got exception!send subscribers is empty");
        }
    }

    private void confirmCallBack(final boolean result) {

        if (taskClosure != null) {

            executorManager.getCheckPushExecutor().execute(() -> {

                if (result) {
                    //change all subscribers push version
                    subscribers.forEach(subscriber -> subscriber
                            .checkAndUpdateVersion(receivedData.getSegment(), receivedData.getVersion()));

                    taskClosure.run(ProcessingResult.Success, ReceivedDataMultiPushTask.this);
                } else {
                    taskClosure.run(ProcessingResult.PermanentError, ReceivedDataMultiPushTask.this);
                }

            });
        }
    }

    @Override
    public String toString() {
        return "RECEIVED_DATA_MULTI_PUSH_TASK{" + "taskId='" + getTaskId() + '\''
               + ", receivedData=" + receivedData + ", url=" + url + ", expiryTime='"
               + getExpiryTime() + '\'' + '}';
    }

    @Override
    protected boolean checkRetryTimes(int retryTimes) {
        int configTimes = sessionServerConfig.getReceivedDataMultiPushTaskRetryTimes();
        if (configTimes > 0) {
            return retryTimes <= configTimes;
        }
        return false;
    }

    private long getBlockTime(int retry) {
        long initialSleepTime = TimeUnit.MILLISECONDS.toMillis(sessionServerConfig
            .getPushDataTaskRetryFirstDelay());
        long increment = TimeUnit.MILLISECONDS.toMillis(sessionServerConfig
            .getPushDataTaskRetryIncrementDelay());
        long result = initialSleepTime + (increment * (retry - 1));
        return result >= 0L ? result : 0L;
    }

    @Override
    public boolean checkRetryTimes() {
        return checkRetryTimes(sessionServerConfig.getReceivedDataMultiPushTaskRetryTimes());
    }

    @Override
    public void run(ProcessingResult processingResult, Task task) {
        if (taskClosure != null) {
            taskClosure.run(processingResult, task);
        }
    }
}