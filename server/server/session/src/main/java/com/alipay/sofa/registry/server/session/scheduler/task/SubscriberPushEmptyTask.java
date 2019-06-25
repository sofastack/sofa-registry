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

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.store.BaseInfo.ClientVersion;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.core.model.ReceivedData;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.converter.ReceivedDataConverter;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.task.listener.TaskEvent.TaskType;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author shangyu.wh
 * @version $Id: SubscriberRegisterFetchTask.java, v 0.1 2017-12-07 16:23 shangyu.wh Exp $
 */
public class SubscriberPushEmptyTask extends AbstractSessionTask {

    private static final Logger       taskLogger = LoggerFactory.getLogger(
                                                     SubscriberPushEmptyTask.class, "[Task]");

    private final SessionServerConfig sessionServerConfig;
    /**
     * trigger task com.alipay.sofa.registry.server.meta.listener process
     */
    private final TaskListenerManager taskListenerManager;
    private Subscriber                subscriber;

    public SubscriberPushEmptyTask(SessionServerConfig sessionServerConfig,
                                   TaskListenerManager taskListenerManager) {
        this.sessionServerConfig = sessionServerConfig;
        this.taskListenerManager = taskListenerManager;
    }

    @Override
    public long getExpiryTime() {
        return -1;
    }

    @Override
    public void setTaskEvent(TaskEvent taskEvent) {
        //taskId create from event
        if (taskEvent.getTaskId() != null) {
            setTaskId(taskEvent.getTaskId());
        }

        Object obj = taskEvent.getEventObj();

        if (!(obj instanceof Subscriber)) {
            throw new IllegalArgumentException("Input task event object error!");
        }

        this.subscriber = (Subscriber) obj;
    }

    @Override
    public void execute() {
        executeTask();
    }

    private void executeTask() {

        if (subscriber == null) {
            throw new IllegalArgumentException("Subscriber can not be null!");
        }

        List<String> subscriberRegisterIdList = Collections.singletonList(subscriber
            .getRegisterId());

        boolean isOldVersion = !ClientVersion.StoreData.equals(subscriber.getClientVersion());

        switch (subscriber.getScope()) {
            case zone:
                if (isOldVersion) {
                    fireUserDataElementPushTask();
                } else {
                    fireReceivedDataPushTask(subscriberRegisterIdList, ScopeEnum.zone);
                }
                break;

            case dataCenter:
                if (isOldVersion) {
                    fireUserDataElementMultiPushTask();
                } else {
                    fireReceivedDataPushTask(subscriberRegisterIdList, ScopeEnum.dataCenter);
                }
                break;

            case global:

                fireReceivedDataPushTask(subscriberRegisterIdList, ScopeEnum.global);

                break;
            default:
                break;
        }

    }

    private void fireReceivedDataPushTask(List<String> subscriberRegisterIdList, ScopeEnum scopeEnum) {
        ReceivedData receivedData = ReceivedDataConverter.getReceivedDataMulti(
            subscriber.getDataId(), subscriber.getGroup(), subscriber.getInstanceId(),
            sessionServerConfig.getSessionServerDataCenter(), scopeEnum, subscriberRegisterIdList,
            sessionServerConfig.getSessionServerRegion());
        //no datum set version current timestamp
        receivedData.setVersion(System.currentTimeMillis());
        firePush(receivedData);
    }

    private void firePush(ReceivedData receivedData) {
        //trigger push to client node
        Map<ReceivedData, URL> parameter = new HashMap<>();
        parameter.put(receivedData, subscriber.getSourceAddress());
        TaskEvent taskEvent = new TaskEvent(parameter, TaskType.RECEIVED_DATA_MULTI_PUSH_TASK);
        taskLogger.info("send {} taskURL:{},taskScope", taskEvent.getTaskType(),
            subscriber.getSourceAddress(), receivedData.getScope());
        taskListenerManager.sendTaskEvent(taskEvent);
    }

    private void fireUserDataElementPushTask() {

        //no datum
        Datum datum = new Datum();
        datum.setDataId(subscriber.getDataId());
        datum.setInstanceId(subscriber.getInstanceId());
        datum.setGroup(subscriber.getGroup());
        //no datum set version current timestamp
        datum.setVersion(System.currentTimeMillis());
        datum.setPubMap(new HashMap<>());
        datum.setDataCenter(ValueConstants.DEFAULT_DATA_CENTER);

        Collection<Subscriber> subscribers = new ArrayList<>();
        subscribers.add(subscriber);

        TaskEvent taskEvent = new TaskEvent(subscriber, TaskType.USER_DATA_ELEMENT_PUSH_TASK);

        taskEvent.setAttribute(Constant.PUSH_CLIENT_SUBSCRIBERS, subscribers);
        taskEvent.setAttribute(Constant.PUSH_CLIENT_DATUM, datum);
        taskEvent.setAttribute(Constant.PUSH_CLIENT_URL, subscriber.getSourceAddress());

        taskLogger.info("send {} taskURL:{}", taskEvent.getTaskType(),
            subscriber.getSourceAddress());
        taskListenerManager.sendTaskEvent(taskEvent);
    }

    private void fireUserDataElementMultiPushTask() {

        //no datum
        Datum datum = new Datum();
        datum.setDataId(subscriber.getDataId());
        datum.setInstanceId(subscriber.getInstanceId());
        datum.setGroup(subscriber.getGroup());
        //no datum set version as mini as
        datum.setVersion(System.currentTimeMillis());
        datum.setPubMap(new HashMap<>());
        datum.setDataCenter(ValueConstants.DEFAULT_DATA_CENTER);

        Collection<Subscriber> subscribers = new ArrayList<>();
        subscribers.add(subscriber);

        TaskEvent taskEvent = new TaskEvent(subscriber, TaskType.USER_DATA_ELEMENT_MULTI_PUSH_TASK);
        taskEvent.setAttribute(Constant.PUSH_CLIENT_SUBSCRIBERS, subscribers);
        taskEvent.setAttribute(Constant.PUSH_CLIENT_DATUM, datum);
        taskEvent.setAttribute(Constant.PUSH_CLIENT_URL, subscriber.getSourceAddress());

        taskLogger.info("send {} taskURL:{}", taskEvent.getTaskType(),
            subscriber.getSourceAddress());
        taskListenerManager.sendTaskEvent(taskEvent);
    }

    @Override
    public boolean checkRetryTimes() {
        return checkRetryTimes(sessionServerConfig.getSubscriberRegisterFetchRetryTimes());
    }

    @Override
    public String toString() {
        return "SubscriberPushEmptyTask{" + "taskId='" + getTaskId() + '\'' + ", subscriber="
               + subscriber + ", expiryTime='" + getExpiryTime() + '\'' + '}';
    }
}