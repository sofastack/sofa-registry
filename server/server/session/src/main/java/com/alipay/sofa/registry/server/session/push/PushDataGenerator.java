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

import com.alipay.sofa.registry.common.model.SubscriberUtils;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.store.*;
import com.alipay.sofa.registry.core.model.ReceivedData;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.cache.AppRevisionCacheRegistry;
import com.alipay.sofa.registry.server.session.converter.AppPublisherConverter;
import com.alipay.sofa.registry.server.session.converter.ReceivedDataConverter;
import com.alipay.sofa.registry.server.session.converter.pb.ReceivedDataConvertor;
import com.alipay.sofa.registry.server.session.predicate.ZonePredicate;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.function.Predicate;

public class PushDataGenerator {
    private static final Logger      LOGGER = LoggerFactory.getLogger(PushDataGenerator.class);

    @Autowired
    private SessionServerConfig      sessionServerConfig;

    @Autowired
    private AppRevisionCacheRegistry appRevisionCacheRegistry;

    public Object createPushData(Datum datum, Map<String, Subscriber> subscriberMap, long version) {
        SubscriberUtils.getAndAssertHasSameScope(subscriberMap.values());
        final Subscriber subscriber = subscriberMap.values().iterator().next();
        BaseInfo.ClientVersion clientVersion = subscriber.getClientVersion();
        // only supported 4.x
        if (BaseInfo.ClientVersion.StoreData != clientVersion) {
            throw new IllegalArgumentException("unsupported clientVersion:" + clientVersion);
        }
        String dataId = datum.getDataId();
        String clientCell = sessionServerConfig.getClientCell(subscriber.getCell());
        Predicate<String> zonePredicate = ZonePredicate.zonePredicate(dataId, clientCell,
            subscriber.getScope(), sessionServerConfig);

        ReceivedData receivedData = ReceivedDataConverter.getReceivedDataMulti(datum,
            subscriber.getScope(), Lists.newArrayList(subscriberMap.keySet()), clientCell,
            zonePredicate);
        receivedData.setVersion(version);
        final Byte serializerIndex = subscriber.getSourceAddress().getSerializerIndex();
        if (serializerIndex != null && URL.PROTOBUF == serializerIndex.byteValue()) {
            return ReceivedDataConvertor.convert2Pb(receivedData);
        }
        return receivedData;
    }

    public Datum mergeDatum(Subscriber subscriber, String dataCenter, Map<String, Datum> datumMap) {
        DataInfo dataInfo = DataInfo.valueOf(subscriber.getDataInfoId());
        Datum ret = new Datum();
        ret.setDataInfoId(dataInfo.getDataInfoId());
        ret.setDataCenter(dataCenter);
        ret.setDataId(dataInfo.getDataId());
        ret.setInstanceId(dataInfo.getInstanceId());
        ret.setGroup(dataInfo.getDataType());
        for (Datum datum : datumMap.values()) {
            for (Publisher publisher : datum.getPubMap().values()) {
                if (publisher instanceof AppPublisher) {
                    AppPublisher appPublisher = (AppPublisher) publisher;
                    Publisher newPublisher = AppPublisherConverter.convert(appPublisher,
                        appRevisionCacheRegistry, dataInfo);
                    if (newPublisher.getDataList().size() > 0) {
                        ret.addPublisher(newPublisher);
                    }
                } else {
                    ret.addPublisher(publisher);
                }
            }
        }
        LOGGER.info("merged {}, from {}", ret, datumMap);
        return ret;
    }
}
