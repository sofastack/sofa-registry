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
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.SubDatum;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.cache.*;
import com.alipay.sofa.registry.server.session.store.Interests;
import com.alipay.sofa.registry.server.shared.util.DatumUtils;
import com.alipay.sofa.registry.task.KeyedThreadPoolExecutor;

import static com.alipay.sofa.registry.server.session.push.PushMetrics.Fetch.*;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.RejectedExecutionException;

public class FirePushService {
    public static final long        EXCEPT_MIN_VERSION = Long.MIN_VALUE;
    private static final Logger     LOGGER             = LoggerFactory
                                                           .getLogger(FirePushService.class);

    @Autowired
    private SessionServerConfig     sessionServerConfig;

    @Autowired
    private CacheService            sessionCacheService;

    @Autowired
    private Interests               sessionInterests;

    private KeyedThreadPoolExecutor registerFetchExecutor;

    @Autowired
    private PushProcessor           pushProcessor;
    @Autowired
    private ChangeProcessor         changeProcessor;
    private final ChangeHandler     changeHandler      = new ChangeHandler();

    @PostConstruct
    public void init() {
        registerFetchExecutor = new KeyedThreadPoolExecutor("RegisterFetchExecutor",
            sessionServerConfig.getDataChangeFetchTaskWorkerSize(),
            sessionServerConfig.getDataChangeFetchTaskMaxBufferSize());
    }

    public boolean fireOnChange(String dataCenter, String dataInfoId, long expectVersion) {
        try {
            // TODO only supported local dataCenter
            changeProcessor.fireChange(dataCenter, dataInfoId, changeHandler, expectVersion);
            CHANGE_TASK_COUNTER.inc();
            return true;
        } catch (Throwable e) {
            LOGGER.error("failed to exec ChangeHandler {}, dataCenter={}, expectVer={}, {}",
                dataInfoId, dataCenter, expectVersion, e);
            return false;
        }
    }

    public boolean fireOnPushEmpty(Subscriber subscriber) {
        SubDatum emptyDatum = DatumUtils.newEmptySubDatum(subscriber, getDataCenterWhenPushEmpty());
        processPush(true, emptyDatum, Collections.singletonList(subscriber));
        PUSH_EMPTY_COUNTER.inc();
        LOGGER.info("firePushEmpty, {}", subscriber);
        return true;
    }

    public boolean fireOnRegister(Subscriber subscriber) {
        try {
            registerFetchExecutor.execute(subscriber.getDataInfoId(), new RegisterTask(subscriber));
            REGISTER_TASK_COUNTER.inc();
            return true;
        } catch (RejectedExecutionException e) {
            LOGGER.error("failed to exec SubscriberTask {}, {}, {}", subscriber.getDataInfoId(),
                subscriber, e.getMessage());
            return false;
        } catch (Throwable e) {
            LOGGER.error("failed to exec SubscriberTask {}, {}", subscriber.getDataInfoId(),
                subscriber, e);
            return false;
        }
    }

    public boolean fireOnDatum(SubDatum datum) {
        DataInfo dataInfo = DataInfo.valueOf(datum.getDataInfoId());
        Collection<Subscriber> subscribers = sessionInterests.getInterestOfDatum(dataInfo
            .getDataInfoId());
        processPush(true, datum, subscribers);
        PUSH_TEMP_COUNTER.inc();
        return true;
    }

    protected String getDataCenterWhenPushEmpty() {
        // TODO cloud mode use default.datacenter?
        return sessionServerConfig.getSessionServerDataCenter();
    }

    private void doExecuteOnChange(String dataCenter, String changeDataInfoId, long expectVersion) {
        final SubDatum datum = getDatum(dataCenter, changeDataInfoId, expectVersion);
        if (expectVersion != EXCEPT_MIN_VERSION) {
            if (datum == null) {
                // datum change, but get null datum, should not happen
                LOGGER.error("[changeNilDatum] {},{},{}", dataCenter, changeDataInfoId,
                    expectVersion);
                return;
            }
            if (datum.getVersion() < expectVersion) {
                LOGGER.error("[lessVer] {},{},{}<{}", dataCenter, changeDataInfoId,
                    datum.getVersion(), expectVersion);
                return;
            }
        } else {
            if (datum == null) {
                LOGGER
                    .info("[fetchNilDatum] {},{},{}", dataCenter, changeDataInfoId, expectVersion);
            }
        }

        DataInfo dataInfo = DataInfo.valueOf(changeDataInfoId);
        onDatumChange(dataInfo, datum, dataCenter);
    }

    private void onDatumChange(DataInfo dataInfo, SubDatum datum, String dataCenter) {
        Map<ScopeEnum, List<Subscriber>> scopes = SubscriberUtils.groupByScope(sessionInterests
            .getDatas(dataInfo.getDataInfoId()));
        if (datum == null) {
            datum = DatumUtils.newEmptySubDatum(dataInfo, dataCenter);
            LOGGER.warn("empty push {}, dataCenter={}", dataInfo.getDataInfoId(), dataCenter);
        }
        for (Map.Entry<ScopeEnum, List<Subscriber>> scope : scopes.entrySet()) {
            processPush(false, datum, scope.getValue());
        }
    }

    private void processPush(boolean noDelay, SubDatum datum, Collection<Subscriber> subscriberList) {
        if (subscriberList.isEmpty()) {
            return;
        }
        subscriberList = subscribersPushCheck(datum.getDataCenter(), datum.getVersion(),
            subscriberList);
        if (CollectionUtils.isEmpty(subscriberList)) {
            return;
        }
        Map<InetSocketAddress, Map<String, Subscriber>> group = SubscriberUtils
            .groupBySourceAddress(subscriberList);
        for (Map.Entry<InetSocketAddress, Map<String, Subscriber>> e : group.entrySet()) {
            final InetSocketAddress addr = e.getKey();
            final Map<String, Subscriber> subscriberMap = e.getValue();
            pushProcessor.firePush(noDelay, addr, subscriberMap, datum);
        }
    }

    private SubDatum getDatum(String dataCenter, String dataInfoId, long expectVersion) {
        Key key = new Key(DatumKey.class.getName(), new DatumKey(dataInfoId, dataCenter));
        Value value = sessionCacheService.getValueIfPresent(key);
        if (value != null) {
            SubDatum datum = (SubDatum) value.getPayload();
            if (datum != null && datum.getVersion() >= expectVersion) {
                // the expect version got
                CACHE_HIT_COUNTER.inc();
                return datum;
            }
        }
        CACHE_MISS_COUNTER.inc();
        // the cache is too old
        sessionCacheService.invalidate(key);
        value = sessionCacheService.getValue(key);
        return value == null ? null : (SubDatum) value.getPayload();
    }

    private List<Subscriber> subscribersPushCheck(String dataCenter, Long version,
                                                  Collection<Subscriber> subscribers) {
        List<Subscriber> subscribersSend = Lists.newArrayList();
        for (Subscriber subscriber : subscribers) {
            if (subscriber.checkVersion(dataCenter, version)) {
                subscribersSend.add(subscriber);
            }
        }
        return subscribersSend;
    }

    private final class ChangeHandler implements ChangeProcessor.ChangeHandler {

        @Override
        public void onChange(String dataCenter, String dataInfoId, long expectDatumVersion) {
            try {
                CHANGE_TASK_EXEC_COUNTER.inc();
                doExecuteOnChange(dataCenter, dataInfoId, expectDatumVersion);
            } catch (Throwable e) {
                LOGGER.error("failed to do change Task, {}, dataCenter={}, expectVersion={}",
                    dataInfoId, dataCenter, expectDatumVersion, e);
            }
        }
    }

    private void doExecuteOnSubscriber(String dataCenter, Subscriber subscriber) {
        final String subDataInfoId = subscriber.getDataInfoId();
        SubDatum datum = getDatum(dataCenter, subDataInfoId, Long.MIN_VALUE);
        if (datum == null) {
            datum = DatumUtils.newEmptySubDatum(subscriber, dataCenter);
            LOGGER.warn("[registerEmptyPush] {},{},{}", subDataInfoId, dataCenter, subscriber);
        }
        if (subscriber.hasPushed()) {
            return;
        }
        processPush(true, datum, Collections.singletonList(subscriber));
    }

    private final class RegisterTask implements Runnable {
        final Subscriber subscriber;

        RegisterTask(Subscriber subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void run() {
            final String dataCenter = sessionServerConfig.getSessionServerDataCenter();
            try {
                if (subscriber.hasPushed()) {
                    return;
                }
                doExecuteOnSubscriber(dataCenter, subscriber);
            } catch (Throwable e) {
                LOGGER.error("failed to do register Task, dataCenter={}, {}", dataCenter,
                    subscriber, e);
            }
        }
    }
}
