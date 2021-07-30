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
import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.store.*;
import com.alipay.sofa.registry.core.model.ReceivedConfigData;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.cache.CacheService;
import com.alipay.sofa.registry.server.session.cache.DatumKey;
import com.alipay.sofa.registry.server.session.cache.Key;
import com.alipay.sofa.registry.server.session.cache.Value;
import com.alipay.sofa.registry.server.session.circuit.breaker.CircuitBreakerService;
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
  private static final Logger LOGGER = PushLog.LOGGER;

  @Autowired PushSwitchService pushSwitchService;
  @Autowired SessionServerConfig sessionServerConfig;

  @Autowired CacheService sessionCacheService;

  @Autowired Interests sessionInterests;

  @Autowired CircuitBreakerService circuitBreakerService;

  private KeyedThreadPoolExecutor watchPushExecutor;

  @Autowired PushProcessor pushProcessor;
  @Autowired ChangeProcessor changeProcessor;
  @Autowired WatchProcessor watchProcessor;
  RegProcessor regProcessor;
  final ChangeHandler changeHandler = new ChangeHandler();

  @PostConstruct
  public void init() {
    watchPushExecutor =
        new KeyedThreadPoolExecutor(
            "WatchPushExecutor",
            sessionServerConfig.getWatchPushTaskWorkerSize(),
            sessionServerConfig.getWatchPushTaskMaxBufferSize());
    this.regProcessor =
        new RegProcessor(
            sessionServerConfig.getSubscriberRegisterTaskWorkerSize(), new RegHandler());
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

  public boolean fireOnPushEmpty(Subscriber subscriber, String dataCenter) {
    long version = DatumVersionUtil.nextId();
    if (DatumVersionUtil.useConfregVersionGen()) {
      version = DatumVersionUtil.confregNextId(0);
    }
    return fireOnPushEmpty(subscriber, dataCenter, version);
  }

  public boolean fireOnPushEmpty(Subscriber subscriber, String dataCenter, long version) {
    long pushVersion = subscriber.markPushEmpty(dataCenter, version);

    SubDatum emptyDatum = DatumUtils.newEmptySubDatum(subscriber, dataCenter, pushVersion);
    final long now = System.currentTimeMillis();
    TriggerPushContext pushCtx =
        new TriggerPushContext(dataCenter, emptyDatum.getVersion(), null, now);
    PushCause cause = new PushCause(pushCtx, PushType.Empty, now);
    processPush(cause, emptyDatum, Collections.singletonList(subscriber));
    PUSH_EMPTY_COUNTER.inc();
    return true;
  }

  public boolean fireOnRegister(Subscriber subscriber) {
    try {
      REGISTER_TASK_COUNTER.inc();
      return regProcessor.fireOnReg(subscriber);
    } catch (Throwable e) {
      LOGGER.error("failed to fireOnRegister {}", subscriber.shortDesc(), e);
      return false;
    }
  }

  public boolean fireOnWatcher(Watcher watcher, ReceivedConfigData configData) {
    try {
      if (!pushSwitchService.canIpPush(watcher.getSourceAddress().getIpAddress())) {
        return false;
      }
      int level = sessionServerConfig.getClientNodePushConcurrencyLevel();
      Tuple key =
          Tuple.of(
              watcher.getSourceAddress().buildAddressString(),
              watcher.getDataInfoId().hashCode() % level);
      watchPushExecutor.execute(key, new WatchTask(watcher, configData));
      WATCH_TASK_COUNTER.inc();
      return true;
    } catch (Throwable e) {
      handleFireOnWatchException(watcher, e);
      return false;
    }
  }

  static void handleFireOnWatchException(Watcher watcher, Throwable e) {
    if (e instanceof FastRejectedExecutionException) {
      LOGGER.error("failed to fireOnWatch {}, {}", watcher.shortDesc(), e.getMessage());
      return;
    }
    LOGGER.error("failed to fireOnWatch {}", watcher.shortDesc(), e);
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
    if (!pushSwitchService.canPush()) {
      return false;
    }
    if (subscriberList.isEmpty()) {
      return false;
    }
    // if pushEmpty, do not check the version
    if (pushCause.pushType != PushType.Empty) {
      subscriberList =
          subscribersPushCheck(datum.getDataCenter(), datum.getVersion(), subscriberList);
      if (CollectionUtils.isEmpty(subscriberList)) {
        return false;
      }
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
      CircuitBreakerStatistic statistic = subscriber.getStatistic(dataCenter);
      if (circuitBreakerService.pushCircuitBreaker(statistic)) {
        LOGGER.info(
            "[CircuitBreaker]subscriber:{} push check circuit break, statistic:{}",
            subscriber.shortDesc(),
            statistic);
        continue;
      }

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

  final class RegHandler implements RegProcessor.RegHandler {

    @Override
    public boolean onReg(String dataInfoId, List<Subscriber> subscribers) {
      return doExecuteOnReg(dataInfoId, subscribers);
    }
  }

  boolean doExecuteOnReg(String dataInfoId, List<Subscriber> subscribers) {
    // TODO multi datacenter
    final String dataCenter = sessionServerConfig.getSessionServerDataCenter();
    SubDatum datum = getDatum(dataCenter, dataInfoId, Long.MIN_VALUE);
    if (datum == null) {
      Subscriber first = subscribers.get(0);
      datum =
          DatumUtils.newEmptySubDatum(first, dataCenter, ValueConstants.DEFAULT_NO_DATUM_VERSION);
      LOGGER.warn(
          "[registerEmptyPush]{},{},subNum={},{}",
          dataInfoId,
          dataCenter,
          subscribers.size(),
          first.shortDesc());
    }
    TriggerPushContext pushCtx =
        new TriggerPushContext(
            dataCenter, 0, null, SubscriberUtils.getMinRegisterTimestamp(subscribers));
    PushCause cause = new PushCause(pushCtx, PushType.Reg, System.currentTimeMillis());
    Map<ScopeEnum, List<Subscriber>> scopes = SubscriberUtils.groupByScope(subscribers);
    for (List<Subscriber> scopeList : scopes.values()) {
      processPush(cause, datum, scopeList);
    }
    return true;
  }

  final class WatchTask implements Runnable {
    final long createTimestamp = System.currentTimeMillis();
    final Watcher watcher;
    final ReceivedConfigData data;

    WatchTask(Watcher watcher, ReceivedConfigData data) {
      this.watcher = watcher;
      this.data = data;
    }

    @Override
    public void run() {
      try {
        watchProcessor.doExecuteOnWatch(watcher, data, createTimestamp);
      } catch (Throwable e) {
        LOGGER.error(
            "failed to do watch Task, {}, version={}", watcher.shortDesc(), data.getVersion(), e);
      }
    }
  }
}
