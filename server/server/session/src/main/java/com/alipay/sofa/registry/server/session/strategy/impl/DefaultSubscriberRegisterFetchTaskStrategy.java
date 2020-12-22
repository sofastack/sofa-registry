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
package com.alipay.sofa.registry.server.session.strategy.impl;

import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.store.BaseInfo;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.core.model.AssembleType;
import com.alipay.sofa.registry.server.session.assemble.SubscriberAssembleStrategy;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.cache.CacheService;
import com.alipay.sofa.registry.server.session.node.service.DataNodeService;
import com.alipay.sofa.registry.server.session.push.FirePushService;
import com.alipay.sofa.registry.server.session.strategy.SubscriberRegisterFetchTaskStrategy;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * @author xuanbei
 * @since 2019/2/15
 */
public class DefaultSubscriberRegisterFetchTaskStrategy implements
                                                       SubscriberRegisterFetchTaskStrategy {

    @Autowired
    private SubscriberAssembleStrategy subscriberAssembleStrategy;
    @Autowired
    private FirePushService            firePushService;

    @Override
    public void doSubscriberRegisterFetchTask(SessionServerConfig sessionServerConfig,
                                              TaskListenerManager taskListenerManager,
                                              DataNodeService dataNodeService,
                                              CacheService sessionCacheService,
                                              Subscriber subscriber) {
        if (subscriber == null) {
            throw new IllegalArgumentException("Subscriber can not be null!");
        }

        AssembleType assembleType = subscriber.getAssembleType();

        Map<String, Datum> datumMap = subscriberAssembleStrategy.assembleDatum(assembleType,
            subscriber);
        boolean isOldVersion = !BaseInfo.ClientVersion.StoreData.equals(subscriber
            .getClientVersion());

        switch (subscriber.getScope()) {
            case zone:
            case dataCenter:
                Datum datum = datumMap.get(sessionServerConfig.getSessionServerDataCenter());

                if (isOldVersion) {
                    firePushService.fireUserDataElementPushTask(subscriber, datum);
                } else {
                    fireReceivedDataPushTask(datum, subscriber);
                }

                break;
            case global:
                if (datumMap != null && datumMap.size() > 0) {
                    for (Map.Entry<String, Datum> entry : datumMap.entrySet()) {
                        fireReceivedDataPushTask(entry.getValue(), subscriber);
                    }
                } else {
                    fireReceivedDataPushTask(null, subscriber);
                }
                break;
            default:
                break;
        }
    }

    private void fireReceivedDataPushTask(Datum datum, Subscriber subscriber) {
        if (datum != null) {

            firePushService.fireReceivedDataMultiPushTask(datum, subscriber);
        }
        //first fetch no pub,push no datum
        else {
            firePushService.fireReceivedDataMultiPushTask(subscriber);
        }
    }
}
