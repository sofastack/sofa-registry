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
package com.alipay.sofa.registry.server.session.push;

import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.core.model.ReceivedData;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.converter.ReceivedDataConverter;
import com.alipay.sofa.registry.server.session.predicate.ZonePredicate;
import com.alipay.sofa.registry.server.session.scheduler.task.Constant;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.task.listener.TaskEvent.TaskType;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.function.Predicate;

/**
 *
 * @author xiaojian.xj
 * @version $Id: FirePushService.java, v 0.1 2020年11月12日 21:41 xiaojian.xj Exp $
 */
public class FirePushService {

    private static final Logger taskLogger = LoggerFactory.getLogger(FirePushService.class);

    @Autowired
    private SessionServerConfig sessionServerConfig;

    @Autowired
    private TaskListenerManager taskListenerManager;

    public void fireUserDataElementPushTask(Subscriber subscriber, Datum datum) {

        List<Subscriber> subscribers = Collections.singletonList(subscriber);
        this.fireUserDataElementPushTask(subscriber.getSourceAddress(), datum, subscribers,
            subscriber.getScope());
    }

    public void fireUserDataElementPushTask(URL clientUrl, Datum datum,
                                            Collection<Subscriber> subscribers, ScopeEnum scopeEnum) {
        TaskEvent taskEvent;
        if (scopeEnum == ScopeEnum.zone) {
            taskEvent = new TaskEvent(TaskType.USER_DATA_ELEMENT_PUSH_TASK);
        } else if (scopeEnum == ScopeEnum.dataCenter) {
            taskEvent = new TaskEvent(TaskType.USER_DATA_ELEMENT_MULTI_PUSH_TASK);
        } else {
            return;
        }
        if (datum == null) {
            datum = emptyDatum(subscribers.stream().findAny().get());
        }
        taskEvent.setSendTimeStamp(DatumVersionUtil.nextId());
        taskEvent.setAttribute(Constant.PUSH_CLIENT_SUBSCRIBERS, subscribers);
        taskEvent.setAttribute(Constant.PUSH_CLIENT_DATUM, datum);
        taskEvent.setAttribute(Constant.PUSH_CLIENT_URL, clientUrl);

        int size = datum != null ? datum.publisherSize() : 0;

        taskLogger.info(
            "send {} taskURL:{},dataInfoId={},dataCenter={},pubSize={},subSize={},taskId={}",
            taskEvent.getTaskType(), clientUrl, datum.getDataInfoId(), datum.getDataCenter(), size,
            subscribers.size(), taskEvent.getTaskId());
        taskListenerManager.sendTaskEvent(taskEvent);
    }

    public void fireReceivedDataMultiPushTask(Datum datum, Subscriber subscriber) {
        List<String> subscriberRegisterIdList = Collections.singletonList(subscriber
            .getRegisterId());

        List<Subscriber> subscribers = Collections.singletonList(subscriber);
        this.fireReceivedDataMultiPushTask(datum, subscriberRegisterIdList, subscribers,
            subscriber.getScope(), subscriber);
    }

    public void fireReceivedDataMultiPushTask(Datum datum, List<String> subscriberRegisterIdList,
                                              Collection<Subscriber> subscribers,
                                              ScopeEnum scopeEnum, Subscriber subscriber) {
        String dataId = datum.getDataId();
        String clientCell = sessionServerConfig.getClientCell(subscriber.getCell());
        Predicate<String> zonePredicate = ZonePredicate.zonePredicate(dataId, clientCell,
            scopeEnum, sessionServerConfig);

        ReceivedData receivedData = ReceivedDataConverter.getReceivedDataMulti(datum, scopeEnum,
            subscriberRegisterIdList, clientCell, zonePredicate);

        //trigger push to client node
        Map<ReceivedData, URL> parameter = new HashMap<>();
        parameter.put(receivedData, subscriber.getSourceAddress());
        TaskEvent taskEvent = new TaskEvent(parameter, TaskType.RECEIVED_DATA_MULTI_PUSH_TASK);
        taskEvent.setAttribute(Constant.PUSH_CLIENT_SUBSCRIBERS, subscribers);
        taskLogger.info("send {} taskURL:{},taskScope:{},,taskId={}", taskEvent.getTaskType(),
            subscriber.getSourceAddress(), scopeEnum, taskEvent.getTaskId());
        taskListenerManager.sendTaskEvent(taskEvent);
    }

    public void fireReceivedDataMultiPushTask(Subscriber subscriber) {

        List<String> subscriberRegisterIdList = Collections.singletonList(subscriber
            .getRegisterId());
        String clientCell = sessionServerConfig.getClientCell(subscriber.getCell());
        ReceivedData receivedData = ReceivedDataConverter.getReceivedDataMulti(
            subscriber.getDataId(), subscriber.getGroup(), subscriber.getInstanceId(),
            sessionServerConfig.getSessionServerDataCenter(), subscriber.getScope(),
            subscriberRegisterIdList, clientCell);

        //trigger push to client node
        Map<ReceivedData, URL> parameter = new HashMap<>();
        parameter.put(receivedData, subscriber.getSourceAddress());
        TaskEvent taskEvent = new TaskEvent(parameter,
            TaskEvent.TaskType.RECEIVED_DATA_MULTI_PUSH_TASK);
        taskEvent.setAttribute(Constant.PUSH_CLIENT_SUBSCRIBERS,
            Collections.singletonList(subscriber));
        taskLogger.info("send {} taskURL:{},taskScope", taskEvent.getTaskType(),
            subscriber.getSourceAddress(), receivedData.getScope());
        taskListenerManager.sendTaskEvent(taskEvent);
    }

    private Datum emptyDatum(Subscriber subscriber) {
        Datum datum = new Datum();
        datum.setDataInfoId(subscriber.getDataId());
        datum.setDataId(subscriber.getDataId());
        datum.setInstanceId(subscriber.getInstanceId());
        datum.setGroup(subscriber.getGroup());
        datum.setVersion(DatumVersionUtil.nextId());
        datum.setPubMap(new HashMap<>());
        datum.setDataCenter(sessionServerConfig.getSessionServerDataCenter());
        return datum;
    }
}