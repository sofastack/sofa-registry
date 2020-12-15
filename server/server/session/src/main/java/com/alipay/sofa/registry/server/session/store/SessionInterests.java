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

import com.alipay.sofa.registry.common.model.ConnectId;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.common.model.store.WordCache;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.cache.SubscriberResult;
import com.alipay.sofa.registry.util.VersionsMapUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.*;

/**
 * @author shangyu.wh
 * @version $Id: AbstractSessionInterests.java, v 0.1 2017-11-30 20:42 shangyu.wh Exp $
 */
public class SessionInterests implements Interests, ReSubscribers {

    private static final Logger                                                                                   LOGGER            = LoggerFactory
                                                                                                                                        .getLogger(SessionInterests.class);

    private final ReentrantReadWriteLock                                                                          readWriteLock     = new ReentrantReadWriteLock();
    private final Lock                                                                                            read              = readWriteLock
                                                                                                                                        .readLock();
    private final Lock                                                                                            write             = readWriteLock
                                                                                                                                        .writeLock();

    @Autowired
    private SessionServerConfig                                                                                   sessionServerConfig;

    /**
     * store all register subscriber
     */
    private final ConcurrentHashMap<String/*dataInfoId*/, Map<String/*registerId*/, Subscriber>>                interests         = new ConcurrentHashMap<>();

    private final Map<ConnectId/*connectId*/, Map<String/*registerId*/, Subscriber>>                            connectIndex      = new ConcurrentHashMap<>();

    private final Map<SubscriberResult, Map<InetSocketAddress, Map<String, Subscriber>>>                          resultIndex       = new ConcurrentHashMap<>();

    /**
     * store subscriber interest dataInfo version belong one dataCenter
     */
    private final ConcurrentHashMap<String/*dataCenter*/, Map<String/*dataInfoId*/, Long /*dataInfoVersion*/>> interestVersions  = new ConcurrentHashMap<>();

    private final Map<String/*dataInfoId*/, Map<String/*registerId*/, Subscriber>>                              stopPushInterests = new ConcurrentHashMap<>();

    @Override
    public void add(Subscriber subscriber) {
        Subscriber.internSubscriber(subscriber);

        write.lock();
        try {
            Map<String, Subscriber> subscribers = interests.get(subscriber.getDataInfoId());

            if (subscribers == null) {
                Map<String, Subscriber> newMap = new ConcurrentHashMap<>();
                subscribers = interests.putIfAbsent(subscriber.getDataInfoId(), newMap);
                if (subscribers == null) {
                    subscribers = newMap;
                }
            }

            Subscriber existingSubscriber = subscribers.get(subscriber.getRegisterId());

            if (existingSubscriber != null) {
                LOGGER.warn("There is subscriber already exists,it will be overwrite! {}",
                    existingSubscriber);
                if (sessionServerConfig.isStopPushSwitch()) {
                    deleteReSubscriber(existingSubscriber);
                }
                invalidateIndex(existingSubscriber);
            }

            subscribers.put(subscriber.getRegisterId(), subscriber);

            addReSubscriber(subscriber);

            addIndex(subscriber);

        } finally {
            write.unlock();
        }

    }

    @Override
    public boolean deleteById(String registerId, String dataInfoId) {

        write.lock();
        try {

            Map<String, Subscriber> subscribers = interests.get(dataInfoId);

            if (subscribers == null) {
                LOGGER.error(
                    "Delete failed because subscriber is not registered for dataInfoId: {}",
                    dataInfoId);
                return false;
            } else {
                Subscriber subscriberTodelete = subscribers.remove(registerId);

                if (subscriberTodelete == null) {
                    LOGGER.error(
                        "Delete failed because subscriber is not registered for registerId: {}",
                        registerId);
                    return false;
                } else {
                    if (sessionServerConfig.isStopPushSwitch()) {
                        deleteReSubscriber(subscriberTodelete);
                    }
                    removeIndex(subscriberTodelete);

                    return true;
                }
            }
        } finally {
            write.unlock();
        }

    }

    @Override
    public boolean deleteByConnectId(ConnectId connectId) {
        write.lock();
        try {
            for (Map<String, Subscriber> map : interests.values()) {
                for (Iterator it = map.values().iterator(); it.hasNext();) {
                    Subscriber subscriber = (Subscriber) it.next();
                    if (connectId.equals(subscriber.connectId())) {
                        it.remove();
                        if (sessionServerConfig.isStopPushSwitch()) {
                            deleteReSubscriber(subscriber);
                        }

                        invalidateIndex(subscriber);
                    }
                }
            }
            //force remove connectId
            invalidateConnectIndex(connectId);
            return true;
        } catch (Exception e) {
            LOGGER.error("Delete subscriber by connectId {} error!", connectId, e);
            return false;
        } finally {
            write.unlock();
        }
    }

    @Override
    public long count() {
        AtomicLong count = new AtomicLong(0);
        for (Map<String, Subscriber> map : interests.values()) {
            count.addAndGet(map.size());
        }
        return count.get();
    }

    @Override
    public Map<String, Subscriber> queryByConnectId(ConnectId connectId) {
        return connectIndex.get(connectId);
    }

    public Subscriber queryById(String registerId, String dataInfoId) {

        Map<String, Subscriber> subscribers = interests.get(dataInfoId);

        if (subscribers == null) {
            return null;
        }
        return subscribers.get(registerId);
    }

    @Override
    public Collection<Subscriber> getInterests(String dataInfoId) {
        Map<String, Subscriber> subscribers = interests.get(dataInfoId);
        if (subscribers == null) {
            LOGGER.info("There is not registered subscriber for : {}", dataInfoId);
            return null;
        }
        return subscribers.values();
    }

    @Override
    public boolean checkInterestVersions(String dataCenter, String dataInfoId, Long version) {

        Map<String, Subscriber> subscribers = interests.get(dataInfoId);

        if (subscribers == null || subscribers.isEmpty()) {
            return false;
        }

        Map<String/*dataInfoId*/, Long/*version*/> dataInfoVersions = interestVersions
            .get(dataCenter);
        if (dataInfoVersions == null) {
            Map<String/*dataInfoId*/, Long/*version*/> newDataInfoVersions = new ConcurrentHashMap<>();
            dataInfoVersions = interestVersions.putIfAbsent(dataCenter, newDataInfoVersions);
            if (dataInfoVersions == null) {
                dataInfoVersions = newDataInfoVersions;
            }
        }

        Long oldValue = dataInfoVersions.get(dataInfoId);

        return oldValue == null || version > oldValue;

    }

    @Override
    public boolean checkAndUpdateInterestVersions(String dataCenter, String dataInfoId, Long version) {
        read.lock();
        try {
            dataInfoId = WordCache.getInstance().getWordCache(dataInfoId);

            Map<String, Subscriber> subscribers = interests.get(dataInfoId);

            if (subscribers == null || subscribers.isEmpty()) {
                LOGGER.info(
                    "There are not Subscriber Existed! Who are interest with dataInfoId {} !",
                    dataInfoId);
                return false;
            }

            Map<String/*dataInfoId*/, Long/*version*/> dataInfoVersions = interestVersions
                .get(dataCenter);
            if (dataInfoVersions == null) {
                Map<String/*dataInfoId*/, Long/*version*/> newDataInfoVersions = new ConcurrentHashMap<>();
                dataInfoVersions = interestVersions.putIfAbsent(dataCenter, newDataInfoVersions);
                if (dataInfoVersions == null) {
                    dataInfoVersions = newDataInfoVersions;
                }
            }
            //set zero
            if (version.longValue() == 0l) {
                return dataInfoVersions.put(dataInfoId, version) != null;
            }
            return VersionsMapUtils.checkAndUpdateVersions(dataInfoVersions, dataInfoId, version);
        } finally {
            read.unlock();
        }
    }

    public boolean checkAndUpdateInterestVersionZero(String dataCenter, String dataInfoId) {
        return checkAndUpdateInterestVersions(dataCenter, dataInfoId, 0l);
    }

    @Override
    public Collection<String> getInterestDataInfoIds() {
        return interests.keySet();
    }

    private void addIndex(Subscriber subscriber) {
        addConnectIndex(subscriber);
        addResultIndex(subscriber);
    }

    private void removeIndex(Subscriber subscriber) {
        removeConnectIndex(subscriber);
        removeResultIndex(subscriber);
    }

    private void invalidateIndex(Subscriber subscriber) {
        removeConnectIndex(subscriber);
        invalidateResultIndex(subscriber);
    }

    private void addConnectIndex(Subscriber subscriber) {
        ConnectId connectId = subscriber.connectId();

        Map<String/*registerId*/, Subscriber> subscriberMap = connectIndex.get(connectId);
        if (subscriberMap == null) {
            Map<String/*registerId*/, Subscriber> newSubscriberMap = new ConcurrentHashMap<>();
            subscriberMap = connectIndex.putIfAbsent(connectId, newSubscriberMap);
            if (subscriberMap == null) {
                subscriberMap = newSubscriberMap;
            }
        }

        subscriberMap.put(subscriber.getRegisterId(), subscriber);
    }

    private void addResultIndex(Subscriber subscriber) {

        SubscriberResult subscriberResult = new SubscriberResult(subscriber.getDataInfoId(),
            subscriber.getScope());
        Map<InetSocketAddress, Map<String, Subscriber>> mapSub = resultIndex.get(subscriberResult);
        if (mapSub == null) {
            Map<InetSocketAddress, Map<String, Subscriber>> newMap = new ConcurrentHashMap<>();
            mapSub = resultIndex.putIfAbsent(subscriberResult, newMap);
            if (mapSub == null) {
                mapSub = newMap;
            }
        }

        InetSocketAddress address = new InetSocketAddress(subscriber.getSourceAddress()
            .getIpAddress(), subscriber.getSourceAddress().getPort());

        Map<String, Subscriber> subscribers = mapSub.get(address);
        if (subscribers == null) {
            Map<String, Subscriber> newSubs = new ConcurrentHashMap<>();
            subscribers = mapSub.putIfAbsent(address, newSubs);
            if (subscribers == null) {
                subscribers = newSubs;
            }
        }

        subscribers.put(subscriber.getRegisterId(), subscriber);
    }

    private void removeConnectIndex(Subscriber subscriber) {
        ConnectId connectId = subscriber.connectId();
        Map<String/*registerId*/, Subscriber> subscriberMap = connectIndex.get(connectId);
        if (subscriberMap != null) {
            subscriberMap.remove(subscriber.getRegisterId());
        } else {
            LOGGER.warn("ConnectId {} not existed in Index to remove!", connectId);
        }
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
                LOGGER.warn("InetSocketAddress {} not existed in Index to remove!", address);
            }

        } else {
            LOGGER.warn("SubscriberResult {} not existed in Index to remove!", subscriberResult);
        }
    }

    private void invalidateConnectIndex(ConnectId connectId) {
        connectIndex.remove(connectId);
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
        read.lock();
        try {
            SubscriberResult subscriberResult = new SubscriberResult(dataInfoId, scope);
            Map<InetSocketAddress, Map<String, Subscriber>> map = resultIndex.get(subscriberResult);
            if (map != null && !map.isEmpty()) {
                return new ConcurrentHashMap<>(map);
            } else {
                return new ConcurrentHashMap<>();
            }
        } finally {
            read.unlock();
        }

    }

    @Override
    public void addReSubscriber(Subscriber subscriber) {
        if (sessionServerConfig.isStopPushSwitch()) {

            String dataInfoId = subscriber.getDataInfoId();

            Map<String, Subscriber> subscriberMap = stopPushInterests.get(dataInfoId);
            if (subscriberMap == null) {
                Map<String, Subscriber> newMap = new ConcurrentHashMap<>();
                subscriberMap = stopPushInterests.putIfAbsent(dataInfoId, newMap);
                if (subscriberMap == null) {
                    subscriberMap = newMap;
                }
            }
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
        return stopPushInterests;
    }

    @Override
    public void clearReSubscribers() {
        stopPushInterests.clear();
    }

    @Override
    public Set<ConnectId> getConnectIds() {
        return Sets.newHashSet(connectIndex.keySet());
    }

    public SessionServerConfig getSessionServerConfig() {
        return sessionServerConfig;
    }

    /**
     * Setter method for property <tt>sessionServerConfig</tt>.
     *
     * @param sessionServerConfig value to be assigned to property sessionServerConfig
     */
    public void setSessionServerConfig(SessionServerConfig sessionServerConfig) {
        this.sessionServerConfig = sessionServerConfig;
    }

    @Override
    public List<String> getDataCenters() {
        return Lists.newArrayList(interestVersions.keySet());
    }

    @Override
    public Set<String> getSubscriberProcessIds() {
        HashSet<String> processIds = Sets.newHashSet();
        for (Map<String, Subscriber> subscribers : interests.values()) {
            for (Subscriber subscriber : subscribers.values()) {
                if (subscriber.getProcessId() != null) {
                    processIds.add(subscriber.getProcessId());
                }
            }
        }
        return processIds;
    }

}