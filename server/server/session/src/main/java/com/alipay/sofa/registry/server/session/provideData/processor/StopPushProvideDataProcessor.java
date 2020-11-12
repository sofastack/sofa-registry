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
package com.alipay.sofa.registry.server.session.provideData.processor;

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.provideData.ProvideDataProcessor;
import com.alipay.sofa.registry.server.session.registry.Registry;
import com.alipay.sofa.registry.server.session.scheduler.task.Constant;
import com.alipay.sofa.registry.server.session.store.Interests;
import com.alipay.sofa.registry.server.session.store.ReSubscribers;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import com.alipay.sofa.registry.task.listener.TaskEvent.TaskType;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author shangyu.wh
 * @version 1.0: StopPushProvideDataProcessor.java, v 0.1 2019-10-09 18:53 shangyu.wh Exp $
 */
public class StopPushProvideDataProcessor implements ProvideDataProcessor {

    private static final Logger TASK_LOGGER = LoggerFactory.getLogger(
                                                StopPushProvideDataProcessor.class, "[Task]");

    private static final Logger LOGGER      = LoggerFactory
                                                .getLogger(StopPushProvideDataProcessor.class);

    @Autowired
    private SessionServerConfig sessionServerConfig;

    @Autowired
    private Registry            sessionRegistry;

    @Autowired
    private Interests           sessionInterests;

    @Autowired
    private TaskListenerManager taskListenerManager;

    @Override
    public void changeDataProcess(ProvideData provideData) {
        if (provideData == null) {
            LOGGER.info("Fetch session stopPushSwitch null");
            return;
        }

        //push stop switch
        final Boolean switchData = ProvideData.toBool(provideData);
        if (switchData == null) {
            LOGGER.info("Fetch session stopPushSwitch content null");
            return;
        }
        LOGGER.info("Fetch session stopPushSwitch={}, current={}", switchData,
            sessionServerConfig.isStopPushSwitch());
        boolean ifChange = sessionServerConfig.isStopPushSwitch() != switchData;
        sessionServerConfig.setStopPushSwitch(switchData);
        if (!switchData) {
            //avoid duplicate false receive
            if (ifChange) {
                fireReSubscriber();
            }
        } else {
            //stop push and stop fetch data task
            sessionServerConfig.setBeginDataFetchTask(false);
        }
    }

    /**
     * open push switch to push all reSubscribers
     */
    private void fireReSubscriber() {

        //try catch avoid to error cancel beginDataFetchTask switch on
        try {
            //begin push fire data fetch task first,avoid reSubscriber push duplicate
            sessionRegistry.fetchChangDataProcess();
        } catch (Throwable e) {
            LOGGER.error("Open push switch first fetch task execute error", e);
        }

        try {
            //wait 1 MINUTES for dataFetch task evict duplicate subscriber push
            TimeUnit.MINUTES.sleep(1);
        } catch (InterruptedException e) {
            LOGGER.error("Wait for dataFetch Task Interrupted!");
        }

        //fetch task process 1 minutes,can schedule execute fetch task
        sessionServerConfig.setBeginDataFetchTask(true);

        if (sessionInterests instanceof ReSubscribers) {
            ReSubscribers reSubscriber = (ReSubscribers) sessionInterests;

            Map<String/*dataInfoId*/, Map<String/*registerId*/, Subscriber>> reSubscribers = reSubscriber
                    .getReSubscribers();

            if (reSubscribers != null && !reSubscribers.isEmpty()) {
                reSubscribers.forEach(
                        (dataInfoId, subscribers) -> fireSubscriberMultiFetchTask(dataInfoId, subscribers.values()));
                reSubscriber.clearReSubscribers();
            }
        }
    }

    private void fireSubscriberMultiFetchTask(String dataInfoId, Collection<Subscriber> subscribers) {
        //trigger fetch data for subscriber,and push to client node
        if (!CollectionUtils.isEmpty(subscribers)) {
            TaskEvent taskEvent = new TaskEvent(dataInfoId, TaskType.SUBSCRIBER_MULTI_FETCH_TASK);
            taskEvent.setAttribute(Constant.PUSH_CLIENT_SUBSCRIBERS, subscribers);
            TASK_LOGGER.info("send " + taskEvent.getTaskType()
                             + " subscribersSize:{},dataInfoId:{}", subscribers.size(), dataInfoId);
            taskListenerManager.sendTaskEvent(taskEvent);
        }
    }

    @Override
    public void fetchDataProcess(ProvideData provideData) {
        Boolean v = ProvideData.toBool(provideData);
        if (v == null) {
            LOGGER.info("Fetch session stop push switch no data existed,config not change!");
            return;
        }

        String data = (String) provideData.getProvideData().getObject();
        sessionServerConfig.setStopPushSwitch(Boolean.valueOf(data));
        if (data != null) {
            if (!Boolean.valueOf(data)) {
                //stop push init on,then begin fetch data schedule task
                sessionServerConfig.setBeginDataFetchTask(true);
            } else {
                sessionServerConfig.setBeginDataFetchTask(false);
            }
        }
        LOGGER.info("Fetch session stop push data switch {} success!", v);
    }

    @Override
    public boolean support(ProvideData provideData) {
        return ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID.equals(provideData.getDataInfoId());
    }
}