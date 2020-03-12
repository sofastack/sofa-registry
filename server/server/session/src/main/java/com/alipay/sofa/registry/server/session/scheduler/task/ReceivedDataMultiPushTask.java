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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import com.alipay.sofa.registry.common.model.PushDataRetryRequest;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.store.DataInfo;
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
import com.alipay.sofa.registry.task.TaskClosure;
import com.alipay.sofa.registry.task.batcher.TaskProcessor.ProcessingResult;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.timer.AsyncHashedWheelTimer;

/**
 *
 * @author shangyu.wh
 * @version $Id: SubscriberRegisterPushTask.java, v 0.1 2017-12-11 20:57 shangyu.wh Exp $
 */
public class ReceivedDataMultiPushTask extends AbstractSessionTask {

    private static final Logger               LOGGER                  = LoggerFactory.getLogger(
                                                                          "SESSION-PUSH",
                                                                          "[Receive]");

    private static final String               RETRY_DATUM_INFO_FORMAT = ValueConstants.DATUM_INFO_FORMAT
                                                                        + ", retryTimes:%s";

    private final SessionServerConfig         sessionServerConfig;
    private final ClientNodeService           clientNodeService;
    private final ExecutorManager             executorManager;
    private final Exchange                    boltExchange;
    private ReceivedData                      receivedData;
    private String                            dataInfoId;
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
        Object receivedDataPush = null;
        try {
            String msgFormat = "Push ReceivedData %s! "
                               + String.format(ValueConstants.DATUM_INFO_FORMAT,
                                   receivedData.getSegment(), dataInfoId,
                                   receivedData.getVersion(), url, dataPush, getTaskId());

            if (sessionServerConfig.isStopPushSwitch()) {
                LOGGER.info(String.format(msgFormat, "but StopPushSwitch is on"));
                return;
            }

            receivedDataPush = receivedDataMultiPushTaskStrategy
                .convert2PushData(receivedData, url);

            final Object finalReceivedDataPush = receivedDataPush;
            CallbackHandler callbackHandler = new CallbackHandler() {
                @Override
                public void onCallback(Channel channel, Object message) {
                    confirmCallBack(true);
                    LOGGER.info(String.format(msgFormat, "success"));
                }

                @Override
                public void onException(Channel channel, Throwable throwable) {
                    LOGGER.error(String.format(msgFormat, "error"), throwable);
                    retrySendReceiveData(new PushDataRetryRequest(finalReceivedDataPush, url));
                }

                @Override
                public Executor getExecutor() {
                    return null;
                }
            };

            clientNodeService.pushWithCallback(receivedDataPush, url, callbackHandler);
        } catch (Throwable e) {
            retrySendReceiveData(new PushDataRetryRequest(receivedDataPush, url));
        }

    }

    private void retrySendReceiveData(PushDataRetryRequest pushDataRetryRequest) {
        Object infoPackage = pushDataRetryRequest.getPushObj();

        int retryTimes = pushDataRetryRequest.getRetryTimes().incrementAndGet();

        URL targetUrl = pushDataRetryRequest.getUrl();

        String msgFormat = "Retry push ReceivedData %s! " + String
                .format(RETRY_DATUM_INFO_FORMAT, receivedData.getSegment(), dataInfoId, receivedData.getVersion(), url,
                        dataPush, getTaskId(), retryTimes);

        if (checkRetryTimes(retryTimes)) {
            Server sessionServer = boltExchange.getServer(sessionServerConfig.getServerPort());

            Channel channel = sessionServer.getChannel(targetUrl);

            if (channel != null && channel.isConnected()) {

                asyncHashedWheelTimer.newTimeout(timeout -> {
                    try {
                        clientNodeService.pushWithCallback(infoPackage, targetUrl, new CallbackHandler() {
                            @Override
                            public void onCallback(Channel channel, Object message) {
                                confirmCallBack(true);
                                LOGGER.info(String.format(msgFormat, "success"));
                            }

                            @Override
                            public void onException(Channel channel, Throwable throwable) {
                                LOGGER.error(String.format(msgFormat, "error"), throwable);
                                retrySendReceiveData(pushDataRetryRequest);
                            }

                            @Override
                            public Executor getExecutor() {
                                return null;
                            }
                        });

                    } catch (Throwable throwable) {
                        LOGGER.error(String.format(msgFormat, "error"), throwable);
                        retrySendReceiveData(pushDataRetryRequest);
                    }
                }, getBlockTime(retryTimes), TimeUnit.MILLISECONDS);
            } else {
                // channel loss, no need to confirmCallBack
                LOGGER.error(String.format(msgFormat, "error, connect be null or disconnected"));
            }
        } else {
            confirmCallBack(false);
            LOGGER.warn(String.format(msgFormat, "times have exceeded, confirmCallBack false"));
        }
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
        if (!(obj instanceof Map) || ((Map<ReceivedData, URL>) obj).size() != 1) {
            throw new IllegalArgumentException("Input task event object error: " + obj);
        }

        Map<ReceivedData, URL> parameter = (Map<ReceivedData, URL>) obj;
        Entry<ReceivedData, URL> entry = (parameter.entrySet()).iterator().next();
        ReceivedData receivedData = entry.getKey();
        URL url = entry.getValue();
        this.receivedData = receivedData;
        this.dataInfoId = new DataInfo(receivedData.getInstanceId(), receivedData.getDataId(),
            receivedData.getGroup()).getDataInfoId();
        this.url = url;

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

        subscribers = (Collection<Subscriber>) taskEvent
            .getAttribute(Constant.PUSH_CLIENT_SUBSCRIBERS);
        //taskClosure must confirm all subscriber push success
        if (taskClosure != null && subscribers.isEmpty()) {
            LOGGER.error("send Receive data subscribers is empty!");
            throw new RuntimeException("Push Receive data got exception!send subscribers is empty");
        }
    }

    private void confirmCallBack(final boolean result) {
        //taskClosure is null at: "temp push", "push empty of clientOff"
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

    private long getBlockTime(int retry) {
        long initialSleepTime = TimeUnit.MILLISECONDS.toMillis(sessionServerConfig
            .getPushDataTaskRetryFirstDelay());
        long increment = TimeUnit.MILLISECONDS.toMillis(sessionServerConfig
            .getPushDataTaskRetryIncrementDelay());
        long result = initialSleepTime + (increment * (retry - 1));
        return result >= 0L ? result : 0L;
    }

    //no used, see ClientNodeSingleTaskProcessor
    @Override
    public boolean checkRetryTimes() {
        return false;
    }

}