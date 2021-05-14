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

import static com.alipay.sofa.registry.server.session.push.PushMetrics.Fetch.*;

import com.alipay.sofa.registry.common.model.SubscriberUtils;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.SubDatum;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.cache.CacheService;
import com.alipay.sofa.registry.server.session.cache.DatumKey;
import com.alipay.sofa.registry.server.session.cache.Key;
import com.alipay.sofa.registry.server.session.cache.Value;
import com.alipay.sofa.registry.server.session.store.Interests;
import com.alipay.sofa.registry.server.shared.util.DatumUtils;
import com.alipay.sofa.registry.task.FastRejectedExecutionException;
import com.alipay.sofa.registry.task.KeyedThreadPoolExecutor;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.google.common.collect.Lists;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

public class FirePushService {
  private static final Logger LOGGER = LoggerFactory.getLogger(FirePushService.class);

  @Autowired SessionServerConfig sessionServerConfig;

  @Autowired CacheService sessionCacheService;

  @Autowired Interests sessionInterests;

  private KeyedThreadPoolExecutor registerFetchExecutor;

  @Autowired PushProcessor pushProcessor;
  @Autowired ChangeProcessor changeProcessor;
  final ChangeHandler changeHandler = new ChangeHandler();

  @PostConstruct
  public void init() {
    registerFetchExecutor =
        new KeyedThreadPoolExecutor(
            "RegisterFetchExecutor",
            sessionServerConfig.getDataChangeFetchTaskWorkerSize(),
            sessionServerConfig.getDataChangeFetchTaskMaxBufferSize());
  }

  public boolean fireOnChange(String dataInfoId, TriggerPushContext changeCtx) {
    try {
      // TODO only supported local dataCenter
      changeProcessor.fireChange(dataInfoId, changeHandler, changeCtx);
      CHANGE_TASK_COUNTER.inc();
      return true;
    } catch (Throwable e) {
      LOGGER.error("failed to fireOnChange {}, changeCtx={}", dataInfoId, changeCtx, e);
      return false;
    }
  }

  public boolean fireOnPushEmpty(Subscriber subscriber) {
    final String dataCenter = getDataCenterWhenPushEmpty();
    SubDatum emptyDatum =
        DatumUtils.newEmptySubDatum(subscriber, dataCenter, DatumVersionUtil.nextId());
    final long now = System.currentTimeMillis();
    TriggerPushContext pushCtx =
        new TriggerPushContext(dataCenter, emptyDatum.getVersion(), null, now);
    PushCause cause = new PushCause(pushCtx, PushType.Empty, now);
    processPush(cause, emptyDatum, Collections.singletonList(subscriber));
    PUSH_EMPTY_COUNTER.inc();
    LOGGER.info("firePushEmpty, {}", subscriber);
    return true;
  }

  public boolean fireOnRegister(Subscriber subscriber) {
    try {
      registerFetchExecutor.execute(subscriber.getDataInfoId(), new RegisterTask(subscriber));
      REGISTER_TASK_COUNTER.inc();
      return true;
    } catch (Throwable e) {
      handleFireOnRegisterException(subscriber, e);
      return false;
    }
  }

  static void handleFireOnRegisterException(Subscriber subscriber, Throwable e) {
    if (e instanceof FastRejectedExecutionException) {
      LOGGER.error(
          "failed to fireOnRegister {}, {}, {}",
          subscriber.getDataInfoId(),
          subscriber,
          e.getMessage());
      return;
    }
    LOGGER.error("failed to fireOnRegister {}, {}", subscriber.getDataInfoId(), subscriber, e);
  }

  public boolean fireOnDatum(SubDatum datum, String dataNode) {
    try {
      DataInfo dataInfo = DataInfo.valueOf(datum.getDataInfoId());
      Collection<Subscriber> subscribers = sessionInterests.getInterests(dataInfo.getDataInfoId());
      final long now = System.currentTimeMillis();
      TriggerPushContext pushCtx =
          new TriggerPushContext(datum.getDataCenter(), datum.getVersion(), dataNode, now);
      final long datumTimestamp = PushTrace.getTriggerPushTimestamp(datum);
      final PushCause cause = new PushCause(pushCtx, PushType.Temp, datumTimestamp);
      processPush(cause, datum, subscribers);
      PUSH_TEMP_COUNTER.inc();
      return true;
    } catch (Throwable e) {
      LOGGER.error("failed to fireOnDatum {}", datum, e);
      return false;
    }
  }

  protected String getDataCenterWhenPushEmpty() {
    // TODO cloud mode use default.datacenter?
    return sessionServerConfig.getSessionServerDataCenter();
  }

  boolean doExecuteOnChange(String changeDataInfoId, TriggerPushContext changeCtx) {
    final long expectVersion = changeCtx.getExpectDatumVersion();
    final SubDatum datum = getDatum(changeCtx.dataCenter, changeDataInfoId, expectVersion);
    if (datum == null) {
      // datum change, but get null datum, should not happen
      LOGGER.error("[changeNil] {},{},{}", changeCtx.dataCenter, changeDataInfoId, expectVersion);
      return false;
    }
    if (datum.getVersion() < expectVersion) {
      LOGGER.error(
          "[changeLessVer] {},{},{}<{}",
          changeCtx.dataCenter,
          changeDataInfoId,
          datum.getVersion(),
          expectVersion);
      return false;
    }
    onDatumChange(changeCtx, datum);
    return true;
  }

  private void onDatumChange(TriggerPushContext changeCtx, SubDatum datum) {
    Map<ScopeEnum, List<Subscriber>> scopes =
        SubscriberUtils.groupByScope(sessionInterests.getDatas(datum.getDataInfoId()));
    final long datumTimestamp = PushTrace.getTriggerPushTimestamp(datum);
    final PushCause cause = new PushCause(changeCtx, PushType.Sub, datumTimestamp);
    for (Map.Entry<ScopeEnum, List<Subscriber>> scope : scopes.entrySet()) {
      processPush(cause, datum, scope.getValue());
    }
  }

  private boolean processPush(
      PushCause pushCause, SubDatum datum, Collection<Subscriber> subscriberList) {
    if (sessionServerConfig.isStopPushSwitch()) {
      return false;
    }
    if (subscriberList.isEmpty()) {
      return false;
    }
    subscriberList =
        subscribersPushCheck(datum.getDataCenter(), datum.getVersion(), subscriberList);
    if (CollectionUtils.isEmpty(subscriberList)) {
      return false;
    }
    Map<InetSocketAddress, Map<String, Subscriber>> group =
        SubscriberUtils.groupBySourceAddress(subscriberList);
    for (Map.Entry<InetSocketAddress, Map<String, Subscriber>> e : group.entrySet()) {
      final InetSocketAddress addr = e.getKey();
      final Map<String, Subscriber> subscriberMap = e.getValue();
      pushProcessor.firePush(pushCause, addr, subscriberMap, datum);
    }
    return true;
  }

  SubDatum getDatum(String dataCenter, String dataInfoId, long expectVersion) {
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

  private List<Subscriber> subscribersPushCheck(
      String dataCenter, Long version, Collection<Subscriber> subscribers) {
    List<Subscriber> subscribersSend = Lists.newArrayList();
    for (Subscriber subscriber : subscribers) {
      if (subscriber.checkVersion(dataCenter, version)) {
        subscribersSend.add(subscriber);
      }
    }
    return subscribersSend;
  }

  final class ChangeHandler implements ChangeProcessor.ChangeHandler {

    @Override
    public boolean onChange(String dataInfoId, TriggerPushContext changeCtx) {
      try {
        CHANGE_TASK_EXEC_COUNTER.inc();
        doExecuteOnChange(dataInfoId, changeCtx);
        return true;
      } catch (Throwable e) {
        LOGGER.error("failed to do change Task, {}, {}", dataInfoId, changeCtx, e);
        return false;
      }
    }
  }

  boolean doExecuteOnSubscriber(String dataCenter, Subscriber subscriber) {
    if (subscriber.hasPushed()) {
      return false;
    }
    final String subDataInfoId = subscriber.getDataInfoId();
    SubDatum datum = getDatum(dataCenter, subDataInfoId, Long.MIN_VALUE);
    if (datum == null) {
      datum =
          DatumUtils.newEmptySubDatum(
              subscriber, dataCenter, ValueConstants.DEFAULT_NO_DATUM_VERSION);
      LOGGER.warn("[registerEmptyPush] {},{},{}", subDataInfoId, dataCenter, subscriber);
    }
    TriggerPushContext pushCtx =
        new TriggerPushContext(dataCenter, 0, null, subscriber.getRegisterTimestamp());
    PushCause cause = new PushCause(pushCtx, PushType.Reg, System.currentTimeMillis());
    processPush(cause, datum, Collections.singletonList(subscriber));
    return true;
  }

  final class RegisterTask implements Runnable {
    final Subscriber subscriber;

    RegisterTask(Subscriber subscriber) {
      this.subscriber = subscriber;
    }

    @Override
    public void run() {
      final String dataCenter = sessionServerConfig.getSessionServerDataCenter();
      try {
        doExecuteOnSubscriber(dataCenter, subscriber);
      } catch (Throwable e) {
        LOGGER.error("failed to do register Task, dataCenter={}, {}", dataCenter, subscriber, e);
      }
    }
  }
}
