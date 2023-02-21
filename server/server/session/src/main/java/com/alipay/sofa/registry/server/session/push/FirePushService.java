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
import com.alipay.sofa.registry.server.session.metadata.MetadataCacheRegistry;
import com.alipay.sofa.registry.server.session.multi.cluster.DataCenterMetadataCache;
import com.alipay.sofa.registry.server.session.store.Interests;
import com.alipay.sofa.registry.server.shared.util.DatumUtils;
import com.alipay.sofa.registry.task.FastRejectedExecutionException;
import com.alipay.sofa.registry.task.KeyedThreadPoolExecutor;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

public class FirePushService {
  private static final Logger LOGGER = PushLog.LOGGER;

  @Autowired PushSwitchService pushSwitchService;
  final SessionServerConfig sessionServerConfig;

  @Resource CacheService sessionDatumCacheService;

  @Autowired Interests sessionInterests;

  @Autowired CircuitBreakerService circuitBreakerService;

  @Autowired MetadataCacheRegistry metadataCacheRegistry;

  private KeyedThreadPoolExecutor watchPushExecutor;

  @Autowired PushProcessor pushProcessor;
  @Autowired ChangeProcessor changeProcessor;
  @Autowired WatchProcessor watchProcessor;

  @Autowired DataCenterMetadataCache dataCenterMetadataCache;

  RegProcessor regProcessor;
  final ChangeHandler changeHandler = new ChangeHandler();

  private final Set<String> localDataCenter;

  public FirePushService(SessionServerConfig sessionServerConfig) {
    this.sessionServerConfig = sessionServerConfig;
    this.localDataCenter = Collections.singleton(sessionServerConfig.getSessionServerDataCenter());
  }

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

  public RegProcessor getRegProcessor() {
    return regProcessor;
  }

  public boolean fireOnChange(String dataInfoId, TriggerPushContext changeCtx) {
    try {
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
    PushCause cause =
        new PushCause(pushCtx, PushType.Empty, Collections.singletonMap(dataCenter, now));
    processPush(cause, MultiSubDatum.of(emptyDatum), Collections.singletonList(subscriber));
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
      if (!pushSwitchService.canIpPushLocal(watcher.getSourceAddress().getIpAddress())) {
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
      final PushCause cause =
          new PushCause(
              pushCtx,
              PushType.Temp,
              Collections.singletonMap(datum.getDataCenter(), datumTimestamp));
      processPush(cause, MultiSubDatum.of(datum), subscribers);
      PUSH_TEMP_COUNTER.inc();
      return true;
    } catch (Throwable e) {
      LOGGER.error("failed to fireOnDatum {}", datum, e);
      return false;
    }
  }

  boolean doExecuteOnChange(String changeDataInfoId, TriggerPushContext changeCtx) {
    final Map<String, Long> expectVersions = changeCtx.getExpectDatumVersion();
    final MultiSubDatum datum = getDatum(changeDataInfoId, expectVersions);
    if (datum == null) {
      // datum change, but get null datum, should not happen
      LOGGER.error("[changeNil] {},{}", changeDataInfoId, expectVersions);
      return false;
    }
    for (Entry<String, Long> entry : expectVersions.entrySet()) {
      SubDatum subDatum = datum.getSubDatum(entry.getKey());
      if (subDatum == null) {
        // datum change, but get null datum, should not happen
        LOGGER.error("[changeNil] {},{},{}", entry.getKey(), changeDataInfoId, entry.getValue());
        return false;
      }
      if (subDatum.getVersion() < entry.getValue()) {
        LOGGER.error(
            "[changeLessVer] {},{},{}<{}",
            entry.getKey(),
            changeDataInfoId,
            datum.getVersion(),
            entry.getValue());
        return false;
      }
    }

    onDatumChange(changeCtx, datum);
    return true;
  }

  private void onDatumChange(TriggerPushContext changeCtx, MultiSubDatum datum) {
    Map<ScopeEnum, List<Subscriber>> scopes =
        SubscriberUtils.groupByScope(sessionInterests.getDatas(datum.getDataInfoId()));

    final Map<String, Long> datumTimestamp =
        Maps.newHashMapWithExpectedSize(datum.getDatumMap().size());
    for (Entry<String, SubDatum> entry : datum.getDatumMap().entrySet()) {
      datumTimestamp.put(entry.getKey(), PushTrace.getTriggerPushTimestamp(entry.getValue()));
    }
    final PushCause cause = new PushCause(changeCtx, PushType.Sub, datumTimestamp);
    for (Map.Entry<ScopeEnum, List<Subscriber>> scope : scopes.entrySet()) {
      processPush(cause, datum, scope.getValue());
    }
  }

  private boolean processPush(
      PushCause pushCause, MultiSubDatum datum, Collection<Subscriber> subscriberList) {
    if (!pushSwitchService.canLocalDataCenterPush()
        || !pushSwitchService.canPushMulti(datum.dataCenters())) {
      return false;
    }
    if (subscriberList.isEmpty()) {
      return false;
    }
    // if pushEmpty, do not check the version
    if (pushCause.pushType != PushType.Empty) {
      subscriberList = subscribersPushCheck(pushCause, datum.getDatumMap(), subscriberList);
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

  MultiSubDatum getDatum(String dataInfoId, Map<String, Long> expectVersions) {
    ParaCheckUtil.checkNotEmpty(expectVersions, "expectVersions");
    Key key = new Key(DatumKey.class.getName(), new DatumKey(dataInfoId, expectVersions.keySet()));
    Value value = sessionDatumCacheService.getValueIfPresent(key);
    if (value == null) {
      return miss(key);
    }
    MultiSubDatum datum = (MultiSubDatum) value.getPayload();

    if (datum == null || !expectVersions.keySet().equals(datum.dataCenters())) {
      return miss(key);
    }
    for (Entry<String, Long> entry : expectVersions.entrySet()) {
      SubDatum subDatum = datum.getSubDatum(entry.getKey());
      if (subDatum != null && subDatum.getVersion() < entry.getValue()) {
        return miss(key);
      }
    }
    // the expect version got
    CACHE_HIT_COUNTER.inc();
    return datum;
  }

  private MultiSubDatum miss(Key key) {
    Value value;
    CACHE_MISS_COUNTER.inc();
    // the cache is too old
    sessionDatumCacheService.invalidate(key);
    value = sessionDatumCacheService.getValue(key);
    return value == null ? null : (MultiSubDatum) value.getPayload();
  }

  private List<Subscriber> subscribersPushCheck(
      PushCause pushCause, Map<String, SubDatum> datumMap, Collection<Subscriber> subscribers) {
    List<Subscriber> subscribersSend = Lists.newArrayList();
    for (Subscriber subscriber : subscribers) {
      CircuitBreakerStatistic statistic = subscriber.getStatistic();
      if (circuitBreakerService.pushCircuitBreaker(statistic, subscriber.hasPushed())) {
        LOGGER.info(
            "[CircuitBreaker]subscriber:{} push check circuit break, statistic:{}",
            subscriber.shortDesc(),
            statistic);
        continue;
      }

      Map<String, Long> versions = Maps.newHashMapWithExpectedSize(datumMap.size());
      for (Entry<String, SubDatum> entry : datumMap.entrySet()) {
        versions.put(entry.getKey(), entry.getValue().getVersion());
      }

      if (subscriber.acceptMulti()) {
        // when sub is acceptMulti and has not push,
        // only PushType.Reg can trigger push multi datacenter together
        if (!subscriber.hasPushed() && pushCause.pushType != PushType.Reg) {
          continue;
        }

        // sub accept multi, check multi datacenter version,
        // return true if one of any dataCenter need to update
        if (subscriber.checkVersion(versions)) {
          subscribersSend.add(subscriber);
        }
      } else {
        // sub only accept local datacenter
        if (localDataCenter.equals(versions.keySet()) && subscriber.checkVersion(versions)) {
          subscribersSend.add(subscriber);
        }
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

    Map<Boolean, List<Subscriber>> acceptMultiMap = SubscriberUtils.groupByMulti(subscribers);

    Set<String> syncEnableDataCenters = metadataCacheRegistry.getPushEnableDataCenters();
    Set<String> getDatumDataCenters =
        Sets.newHashSetWithExpectedSize(syncEnableDataCenters.size() + 1);
    getDatumDataCenters.add(sessionServerConfig.getSessionServerDataCenter());
    for (String remote : dataCenterMetadataCache.getSyncDataCenters()) {
      if (syncEnableDataCenters.contains(remote)) {
        getDatumDataCenters.add(remote);
      }
    }

    // accept multi sub register
    doExecuteOnReg(dataInfoId, acceptMultiMap.get(true), getDatumDataCenters);

    // not accept multi sub register
    doExecuteOnReg(
        dataInfoId,
        acceptMultiMap.get(false),
        Collections.singleton(sessionServerConfig.getSessionServerDataCenter()));
    return true;
  }

  private void doExecuteOnReg(
      String dataInfoId, List<Subscriber> subscribers, Set<String> dataCenters) {
    if (CollectionUtils.isEmpty(subscribers)) {
      return;
    }
    Map<String, Long> getDatumVersions = Maps.newHashMapWithExpectedSize(dataCenters.size());
    Map<String, Long> expectDatumVersions = Maps.newHashMapWithExpectedSize(dataCenters.size());
    Map<String, Long> datumTimestamp = Maps.newHashMapWithExpectedSize(dataCenters.size());
    for (String dataCenter : dataCenters) {
      getDatumVersions.put(dataCenter, Long.MIN_VALUE);
      expectDatumVersions.put(dataCenter, 0L);
      datumTimestamp.put(dataCenter, System.currentTimeMillis());
    }

    MultiSubDatum datum = getDatum(dataInfoId, getDatumVersions);
    if (datum == null || CollectionUtils.isEmpty(datum.getDatumMap())) {
      Subscriber first = subscribers.get(0);
      datum =
          DatumUtils.newEmptyMultiSubDatum(
              first, dataCenters, ValueConstants.DEFAULT_NO_DATUM_VERSION);
      LOGGER.warn(
          "[registerEmptyPush]{},{},subNum={},{}",
          dataInfoId,
          dataCenters,
          subscribers.size(),
          first.shortDesc());
    } else if (!getDatumVersions.keySet().equals(datum.dataCenters())) {
      Set<String> tobeAdd = Sets.difference(getDatumVersions.keySet(), datum.dataCenters());
      Subscriber first = subscribers.get(0);
      for (String dataCenter : tobeAdd) {
        LOGGER.warn(
            "[registerLakeDatumPush]{},{},subNum={},{}",
            dataInfoId,
            dataCenters,
            subscribers.size(),
            first.shortDesc());
        datum.putDatum(
            dataCenter,
            DatumUtils.newEmptySubDatum(
                first, dataCenter, ValueConstants.DEFAULT_NO_DATUM_VERSION));
      }
    }
    TriggerPushContext pushCtx =
        new TriggerPushContext(
            expectDatumVersions, null, SubscriberUtils.getMinRegisterTimestamp(subscribers));
    PushCause cause = new PushCause(pushCtx, PushType.Reg, datumTimestamp);
    Map<ScopeEnum, List<Subscriber>> scopes = SubscriberUtils.groupByScope(subscribers);
    for (List<Subscriber> scopeList : scopes.values()) {
      processPush(cause, datum, scopeList);
    }
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
