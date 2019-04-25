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

import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.sessionserver.DataPushRequest;
import com.alipay.sofa.registry.common.model.store.BaseInfo.ClientVersion;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.core.model.ReceivedData;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.converter.ReceivedDataConverter;
import com.alipay.sofa.registry.server.session.scheduler.ExecutorManager;
import com.alipay.sofa.registry.server.session.store.Interests;
import com.alipay.sofa.registry.task.batcher.TaskProcessor.ProcessingResult;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.task.listener.TaskEvent.TaskType;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

/**
 *
 * @author shangyu.wh
 * @version $Id: DataPushTask.java, v 0.1 2018-08-29 19:46 shangyu.wh Exp $
 */
public class DataPushTask extends AbstractSessionTask {

    private final static Logger       LOGGER     = LoggerFactory.getLogger(DataPushTask.class);

    private static final Logger       taskLogger = LoggerFactory.getLogger(DataPushTask.class,
                                                     "[Task]");

    private final SessionServerConfig sessionServerConfig;

    private final Interests           sessionInterests;

    private final ExecutorManager     executorManager;

    /**
     * trigger task com.alipay.sofa.registry.server.meta.listener process
     */
    private final TaskListenerManager taskListenerManager;

    private DataPushRequest           dataPushRequest;

    public DataPushTask(Interests sessionInterests, SessionServerConfig sessionServerConfig,
                        ExecutorManager executorManager, TaskListenerManager taskListenerManager) {
        this.sessionInterests = sessionInterests;
        this.sessionServerConfig = sessionServerConfig;
        this.executorManager = executorManager;
        this.taskListenerManager = taskListenerManager;
    }

    @Override
    public void execute() {

        String localDataCenterID = sessionServerConfig.getSessionServerDataCenter();

        Datum datum = dataPushRequest.getDatum();

        if (datum != null) {

            boolean ifLocalDataCenter = localDataCenterID.equals(datum.getDataCenter());

            String dataInfoId = datum.getDataInfoId();

            for (ScopeEnum scopeEnum : ScopeEnum.values()) {
                Map<InetSocketAddress, Map<String, Subscriber>> map = getCache(scopeEnum,
                    dataInfoId);
                if (map != null && !map.isEmpty()) {
                    for (Entry<InetSocketAddress, Map<String, Subscriber>> entry : map.entrySet()) {
                        Map<String, Subscriber> subscriberMap = entry.getValue();
                        if (subscriberMap != null && !subscriberMap.isEmpty()) {
                            List<String> subscriberRegisterIdList = new ArrayList<>(
                                subscriberMap.keySet());

                            Subscriber subscriber = subscriberMap.values().iterator().next();
                            boolean isOldVersion = !ClientVersion.StoreData.equals(subscriber
                                .getClientVersion());

                            Collection<Subscriber> subscribersSend = new ArrayList<>(
                                subscriberMap.values());

                            switch (scopeEnum) {
                                case zone:
                                    if (ifLocalDataCenter) {
                                        if (isOldVersion) {
                                            fireUserDataElementPushTask(entry.getKey(), datum,
                                                subscribersSend);
                                        } else {
                                            fireReceivedDataMultiPushTask(datum,
                                                subscriberRegisterIdList, ScopeEnum.zone,
                                                subscriber, subscriberMap);
                                        }
                                    }
                                    break;
                                case dataCenter:
                                    if (ifLocalDataCenter) {
                                        if (isOldVersion) {
                                            fireUserDataElementMultiPushTask(entry.getKey(), datum,
                                                subscribersSend);
                                        } else {
                                            fireReceivedDataMultiPushTask(datum,
                                                subscriberRegisterIdList, scopeEnum, subscriber,
                                                subscriberMap);
                                        }
                                    }
                                    break;
                                case global:
                                    fireReceivedDataMultiPushTask(datum, subscriberRegisterIdList,
                                        scopeEnum, subscriber, subscriberMap);
                                    break;
                                default:
                                    LOGGER.warn("unknown scope, {}", subscriber);
                            }
                        }
                    }
                }
            }
        }
    }

    private void fireReceivedDataMultiPushTask(Datum datum, List<String> subscriberRegisterIdList,
                                               ScopeEnum scopeEnum, Subscriber subscriber, Map<String, Subscriber> subscriberMap) {
        Collection<Subscriber> subscribers = new ArrayList<>(subscriberMap.values());
        String dataId = datum.getDataId();
        Predicate<String> zonePredicate = (zone) -> {
            if (!sessionServerConfig.getSessionServerRegion().equals(zone)) {
                if (ScopeEnum.zone == scopeEnum) {
                    // zone scope subscribe only return zone list
                    return true;

                } else if (ScopeEnum.dataCenter == scopeEnum) {
                    // disable zone config
                    if (sessionServerConfig.isInvalidForeverZone(zone)
                            && !sessionServerConfig.isInvalidIgnored(dataId)) {
                        return true;
                    }
                }
            }
            return false;
        };
        LOGGER.info("Datum push={}",datum);
        ReceivedData receivedData = ReceivedDataConverter.getReceivedDataMulti(datum, scopeEnum,
                subscriberRegisterIdList, sessionServerConfig.getSessionServerRegion(), zonePredicate);

        //trigger push to client node
        Map<ReceivedData, URL> parameter = new HashMap<>();
        parameter.put(receivedData, subscriber.getSourceAddress());
        TaskEvent taskEvent = new TaskEvent(parameter, TaskType.RECEIVED_DATA_MULTI_PUSH_TASK);
        taskEvent.setAttribute(Constant.PUSH_CLIENT_SUBSCRIBERS, subscribers);
        taskLogger.info("send {} taskURL:{},taskScope:{},version:{}", taskEvent.getTaskType(), subscriber.getSourceAddress(),
                scopeEnum,receivedData.getVersion());
        taskListenerManager.sendTaskEvent(taskEvent);
    }

    private Map<InetSocketAddress, Map<String, Subscriber>> getCache(ScopeEnum scopeEnum,
                                                                     String dataInfoId) {
        return sessionInterests.querySubscriberIndex(dataInfoId, scopeEnum);
    }

    private void fireUserDataElementPushTask(InetSocketAddress address, Datum datum,
                                             Collection<Subscriber> subscribers) {

        TaskEvent taskEvent = new TaskEvent(TaskType.USER_DATA_ELEMENT_PUSH_TASK);

        taskEvent.setAttribute(Constant.PUSH_CLIENT_SUBSCRIBERS, subscribers);
        taskEvent.setAttribute(Constant.PUSH_CLIENT_DATUM, datum);
        taskEvent.setAttribute(Constant.PUSH_CLIENT_URL, new URL(address));

        int size = datum.getPubMap() != null ? datum.getPubMap().size() : 0;

        taskLogger.info("send {} taskURL:{},dataInfoId={},dataCenter={},pubSize={},subSize={}",
            taskEvent.getTaskType(), address, datum.getDataInfoId(), datum.getDataCenter(), size,
            subscribers.size());
        taskListenerManager.sendTaskEvent(taskEvent);
    }

    private void fireUserDataElementMultiPushTask(InetSocketAddress address, Datum datum,
                                                  Collection<Subscriber> subscribers) {

        TaskEvent taskEvent = new TaskEvent(TaskType.USER_DATA_ELEMENT_MULTI_PUSH_TASK);

        taskEvent.setAttribute(Constant.PUSH_CLIENT_SUBSCRIBERS, subscribers);
        taskEvent.setAttribute(Constant.PUSH_CLIENT_DATUM, datum);
        taskEvent.setAttribute(Constant.PUSH_CLIENT_URL, new URL(address));

        int size = datum.getPubMap() != null ? datum.getPubMap().size() : 0;

        taskLogger.info("send {} taskURL:{},dataInfoId={},dataCenter={},pubSize={},subSize={}",
            taskEvent.getTaskType(), address, datum.getDataInfoId(), datum.getDataCenter(), size,
            subscribers.size());
        taskListenerManager.sendTaskEvent(taskEvent);
    }

    @Override
    public long getExpiryTime() {
        return -1;
    }

    @Override
    public void setTaskEvent(TaskEvent taskEvent) {
        Object obj = taskEvent.getEventObj();

        if (!(obj instanceof DataPushRequest)) {
            throw new IllegalArgumentException("Input task event object error!");
        }

        this.dataPushRequest = (DataPushRequest) obj;
    }

    @Override
    public boolean checkRetryTimes() {
        return checkRetryTimes(sessionServerConfig.getDataChangeFetchTaskRetryTimes());
    }

    @Override
    public String toString() {
        return "DATA_PUSH_TASK{" + "taskId='" + getTaskId() + '\'' + ", dataPushRequest="
               + dataPushRequest + ", expiryTime='" + getExpiryTime() + '\'' + '}';
    }
}
