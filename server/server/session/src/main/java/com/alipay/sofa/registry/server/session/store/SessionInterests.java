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
package com.alipay.sofa.registry.server.session.store;

import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.google.common.collect.Maps;
import org.apache.commons.collections.MapUtils;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author shangyu.wh
 * @version $Id: AbstractSessionInterests.java, v 0.1 2017-11-30 20:42 shangyu.wh Exp $
 */
public class SessionInterests extends AbstractDataManager<Subscriber> implements Interests,
                                                                     ReSubscribers {

    private static final Logger                                                          LOGGER            = LoggerFactory
                                                                                                               .getLogger(SessionInterests.class);

    private final Map<SubscriberResult, Map<InetSocketAddress, Map<String, Subscriber>>> resultIndex       = new ConcurrentHashMap<>();

    private final Map<String/*dataInfoId*/, Map<String/*registerId*/, Subscriber>>     stopPushInterests = new ConcurrentHashMap<>();

    public SessionInterests() {
        super(LOGGER);
    }

    @Override
    public boolean add(Subscriber subscriber) {
        Subscriber.internSubscriber(subscriber);

        Map<String, Subscriber> subscribers = stores.computeIfAbsent(subscriber.getDataInfoId(),
                k -> Maps.newConcurrentMap());

        Subscriber existingSubscriber = null;
        write.lock();
        try {
            existingSubscriber = subscribers.get(subscriber.getRegisterId());

            if (existingSubscriber != null) {
                if (sessionServerConfig.isStopPushSwitch()) {
                    deleteReSubscriber(existingSubscriber);
                }
                invalidateResultIndex(existingSubscriber);
            }

            subscribers.put(subscriber.getRegisterId(), subscriber);

            addReSubscriber(subscriber);
            addResultIndex(subscriber);
        } finally {
            write.unlock();
        }
        // log without lock
        if (existingSubscriber != null) {
            LOGGER.warn("There is subscriber already exists,it will be overwrite! {}",
                    existingSubscriber);
        }
        return true;
    }

    @Override
    protected void postDelete(Subscriber data) {
        if (sessionServerConfig.isStopPushSwitch()) {
            deleteReSubscriber(data);
        }
        removeResultIndex(data);
    }

    @Override
    public boolean checkInterestVersions(String dataCenter, String dataInfoId, long version) {
        Map<String, Subscriber> subscribers = stores.get(dataInfoId);
        if (MapUtils.isEmpty(subscribers)) {
            return false;
        }
        for (Subscriber subscriber : subscribers.values()) {
            long ver = subscriber.getLastPushVersion(dataCenter);
            if (ver < version) {
                return true;
            }
        }
        return false;
    }

    private void addResultIndex(Subscriber subscriber) {
        SubscriberResult subscriberResult = new SubscriberResult(subscriber.getDataInfoId(),
                subscriber.getScope());
        final Map<InetSocketAddress, Map<String, Subscriber>> mapSub = resultIndex
                .computeIfAbsent(subscriberResult, k -> Maps.newConcurrentMap());

        InetSocketAddress address = new InetSocketAddress(subscriber.getSourceAddress().getIpAddress(), subscriber.getSourceAddress().getPort());

        Map<String, Subscriber> subscribers = mapSub.computeIfAbsent(address, k -> Maps.newConcurrentMap());
        subscribers.put(subscriber.getRegisterId(), subscriber);
    }

    private void removeResultIndex(Subscriber subscriber) {
        SubscriberResult subscriberResult = new SubscriberResult(subscriber.getDataInfoId(),
            subscriber.getScope());
        Map<InetSocketAddress, Map<String, Subscriber>> mapSub = resultIndex.get(subscriberResult);
        if (mapSub != null) {
            InetSocketAddress address = new InetSocketAddress(subscriber.getSourceAddress()
                .getIpAddress(), subscriber.getSourceAddress().getPort());
            Map<String, Subscriber> subscribers = mapSub.get(address);
            if (subscribers != null) {
                subscribers.remove(subscriber.getRegisterId());
            } else {
                LOGGER.warn("Address {} not existed in Index to remove!", address);
            }

        } else {
            LOGGER.warn("SubscriberResult {} not existed in Index to remove!", subscriberResult);
        }
    }

    private void invalidateResultIndex(Subscriber subscriber) {
        SubscriberResult subscriberResult = new SubscriberResult(subscriber.getDataInfoId(),
            subscriber.getScope());
        Map<InetSocketAddress, Map<String, Subscriber>> mapSub = resultIndex.get(subscriberResult);
        if (mapSub != null) {
            InetSocketAddress address = new InetSocketAddress(subscriber.getSourceAddress()
                .getIpAddress(), subscriber.getSourceAddress().getPort());
            mapSub.remove(address);
        } else {
            LOGGER.warn("SubscriberResult {} not existed in Index to remove!", subscriberResult);
        }
    }

    @Override
    public Map<InetSocketAddress, Map<String, Subscriber>> querySubscriberIndex(String dataInfoId,
                                                                                ScopeEnum scope) {
        final SubscriberResult subscriberResult = new SubscriberResult(dataInfoId, scope);
        read.lock();
        try {
            Map<InetSocketAddress, Map<String, Subscriber>> map = resultIndex.get(subscriberResult);
            if (!MapUtils.isEmpty(map)) {
                return StoreHelpers.copyMap((Map) map);
            } else {
                return Collections.emptyMap();
            }
        } finally {
            read.unlock();
        }
    }

    @Override
    public void addReSubscriber(Subscriber subscriber) {
        if (sessionServerConfig.isStopPushSwitch()) {

            String dataInfoId = subscriber.getDataInfoId();

            Map<String, Subscriber> subscriberMap = stopPushInterests
                    .computeIfAbsent(dataInfoId, k -> Maps.newConcurrentMap());
            subscriberMap.put(subscriber.getRegisterId(), subscriber);
        }
    }

    @Override
    public boolean deleteReSubscriber(Subscriber subscriber) {
        Map<String, Subscriber> subscribers = stopPushInterests.get(subscriber.getDataInfoId());

        if (subscribers == null) {
            return false;
        } else {
            return subscribers.remove(subscriber.getRegisterId()) != null;
        }

    }

    @Override
    public Map<String/*dataInfoId*/, Map<String/*registerId*/, Subscriber>> getReSubscribers() {
        return StoreHelpers.copyMap((Map) stopPushInterests);
    }

    @Override
    public void clearReSubscribers() {
        stopPushInterests.clear();
    }

    private static final class SubscriberResult {
        final String    dataInfoId;
        final ScopeEnum scope;

        SubscriberResult(String dataInfoId, ScopeEnum scope) {
            this.dataInfoId = dataInfoId;
            this.scope = scope;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof SubscriberResult))
                return false;
            SubscriberResult that = (SubscriberResult) o;
            return Objects.equals(dataInfoId, that.dataInfoId) && scope == that.scope;
        }

        @Override
        public int hashCode() {
            return Objects.hash(dataInfoId, scope);
        }

        @Override
        public String toString() {
            return "SubscriberResult{" + "dataInfoId='" + dataInfoId + '\'' + ", scope=" + scope
                   + '}';
        }
    }

}