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
package com.alipay.sofa.registry.server.session.registry;

import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.common.model.store.*;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.remoting.exchange.ExchangeCallback;
import com.alipay.sofa.registry.remoting.exchange.RequestChannelClosedException;
import com.alipay.sofa.registry.server.session.acceptor.ClientOffWriteDataRequest;
import com.alipay.sofa.registry.server.session.acceptor.PublisherWriteDataRequest;
import com.alipay.sofa.registry.server.session.acceptor.WriteDataAcceptor;
import com.alipay.sofa.registry.server.session.acceptor.WriteDataRequest;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.circuit.breaker.CircuitBreakerUtil;
import com.alipay.sofa.registry.server.session.loggers.Loggers;
import com.alipay.sofa.registry.server.session.node.service.DataNodeService;
import com.alipay.sofa.registry.server.session.push.FirePushService;
import com.alipay.sofa.registry.server.session.push.PushSwitchService;
import com.alipay.sofa.registry.server.session.push.TriggerPushContext;
import com.alipay.sofa.registry.server.session.slot.SlotTableCache;
import com.alipay.sofa.registry.server.session.store.DataStore;
import com.alipay.sofa.registry.server.session.store.Interests;
import com.alipay.sofa.registry.server.session.store.Watchers;
import com.alipay.sofa.registry.server.session.strategy.SessionRegistryStrategy;
import com.alipay.sofa.registry.server.session.wrapper.Wrapper;
import com.alipay.sofa.registry.server.session.wrapper.WrapperInterceptorManager;
import com.alipay.sofa.registry.server.session.wrapper.WrapperInvocation;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.LoopRunnable;
import com.alipay.sofa.registry.util.StringFormatter;
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.annotation.PostConstruct;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

/**
 * @author shangyu.wh
 * @version $Id: AbstractSessionRegistry.java, v 0.1 2017-11-30 18:13 shangyu.wh Exp $
 */
public class SessionRegistry implements Registry {

  private static final Logger LOGGER = LoggerFactory.getLogger(SessionRegistry.class);

  protected static final Logger SCAN_VER_LOGGER = LoggerFactory.getLogger("SCAN-VER");

  /** store subscribers */
  @Autowired private Interests sessionInterests;

  /** store watchers */
  @Autowired private Watchers sessionWatchers;

  /** store publishers */
  @Autowired private DataStore sessionDataStore;

  /** transfer data to DataNode */
  @Autowired private DataNodeService dataNodeService;

  /** trigger task com.alipay.sofa.registry.server.meta.listener process */
  @Autowired private TaskListenerManager taskListenerManager;

  @Autowired private SessionServerConfig sessionServerConfig;

  @Autowired private PushSwitchService pushSwitchService;

  @Autowired private Exchange boltExchange;

  @Autowired private SessionRegistryStrategy sessionRegistryStrategy;

  @Autowired private WrapperInterceptorManager wrapperInterceptorManager;

  @Autowired private WriteDataAcceptor writeDataAcceptor;

  @Autowired private SlotTableCache slotTableCache;

  @Autowired private FirePushService firePushService;

  private final VersionWatchDog versionWatchDog = new VersionWatchDog();

  @PostConstruct
  public void init() {
    ConcurrentUtils.createDaemonThread("SessionVerWatchDog", versionWatchDog).start();
    ConcurrentUtils.createDaemonThread("SessionClientWatchDog", new ClientWatchDog()).start();
  }

  @Override
  public void register(StoreData storeData) {

    WrapperInvocation<StoreData, Boolean> wrapperInvocation =
        new WrapperInvocation(
            new Wrapper<StoreData, Boolean>() {
              @Override
              public Boolean call() {

                switch (storeData.getDataType()) {
                  case PUBLISHER:
                    Publisher publisher = (Publisher) storeData;
                    publisher.setSessionProcessId(ServerEnv.PROCESS_ID);
                    sessionDataStore.add(publisher);

                    // All write operations to DataServer (pub/unPub/clientoff/renew/snapshot)
                    // are handed over to WriteDataAcceptor
                    writeDataAcceptor.accept(
                        new PublisherWriteDataRequest(
                            publisher, WriteDataRequest.WriteDataRequestType.PUBLISHER));

                    sessionRegistryStrategy.afterPublisherRegister(publisher);
                    break;
                  case SUBSCRIBER:
                    Subscriber subscriber = (Subscriber) storeData;

                    sessionInterests.add(subscriber);

                    sessionRegistryStrategy.afterSubscriberRegister(subscriber);
                    break;
                  case WATCHER:
                    Watcher watcher = (Watcher) storeData;

                    sessionWatchers.add(watcher);

                    sessionRegistryStrategy.afterWatcherRegister(watcher);
                    break;
                  default:
                    break;
                }
                return null;
              }

              @Override
              public Supplier<StoreData> getParameterSupplier() {
                return () -> storeData;
              }
            },
            wrapperInterceptorManager);

    try {
      wrapperInvocation.proceed();
    } catch (RequestChannelClosedException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Proceed register error!", e);
    }
  }

  @Override
  public void unRegister(StoreData<String> storeData) {

    switch (storeData.getDataType()) {
      case PUBLISHER:
        Publisher publisher = (Publisher) storeData;
        publisher.setSessionProcessId(ServerEnv.PROCESS_ID);

        sessionDataStore.deleteById(storeData.getId(), publisher.getDataInfoId());

        // All write operations to DataServer (pub/unPub/clientoff/renew/snapshot)
        // are handed over to WriteDataAcceptor
        writeDataAcceptor.accept(
            new PublisherWriteDataRequest(
                publisher, WriteDataRequest.WriteDataRequestType.UN_PUBLISHER));

        sessionRegistryStrategy.afterPublisherUnRegister(publisher);
        break;

      case SUBSCRIBER:
        Subscriber subscriber = (Subscriber) storeData;
        sessionInterests.deleteById(storeData.getId(), subscriber.getDataInfoId());
        sessionRegistryStrategy.afterSubscriberUnRegister(subscriber);
        break;

      case WATCHER:
        Watcher watcher = (Watcher) storeData;

        sessionWatchers.deleteById(watcher.getId(), watcher.getDataInfoId());

        sessionRegistryStrategy.afterWatcherUnRegister(watcher);
        break;
      default:
        break;
    }
  }

  @Override
  public void clean(List<ConnectId> connectIds) {
    disableConnect(connectIds, true, false);
  }

  @Override
  public void clientOff(List<ConnectId> connectIds) {
    disableConnect(connectIds, false, true);
  }

  @Override
  public void blacklist(List<ConnectId> connectIds) {
    disableConnect(connectIds, true, true);
  }

  private void disableConnect(
      List<ConnectId> connectIds, boolean removeSubAndWat, boolean checkSub) {
    final String dataCenter = getDataCenterWhenPushEmpty();

    if (checkSub) {
      Map<ConnectId, Map<String, Subscriber>> subMap =
          sessionInterests.queryByConnectIds(connectIds);
      for (Entry<ConnectId, Map<String, Subscriber>> subEntry : subMap.entrySet()) {
        int subEmptyCount = 0;
        for (Subscriber sub : subEntry.getValue().values()) {
          if (isPushEmpty(sub)) {
            subEmptyCount++;
            firePushService.fireOnPushEmpty(sub, dataCenter);
            Loggers.CLIENT_OFF_LOG.info(
                "subEmpty,{},{},{}", sub.getDataInfoId(), dataCenter, subEntry.getKey());
          }
        }
        Loggers.CLIENT_OFF_LOG.info("connectId={}, subEmpty={}", subEntry.getKey(), subEmptyCount);
      }
    }

    Map<ConnectId, List<Publisher>> pubMap = removeFromSession(connectIds, removeSubAndWat);
    for (Entry<ConnectId, List<Publisher>> pubEntry : pubMap.entrySet()) {
      clientOffToDataNode(pubEntry.getKey(), pubEntry.getValue());
      Loggers.CLIENT_OFF_LOG.info(
          "connectId={}, pubRemove={}", pubEntry.getKey(), pubEntry.getValue().size());
    }
  }

  public boolean isPushEmpty(Subscriber subscriber) {
    // mostly, do not need to push empty
    return false;
  }

  private Map<ConnectId, List<Publisher>> removeFromSession(
      List<ConnectId> connectIds, boolean removeSubAndWat) {
    Map<ConnectId, Map<String, Publisher>> publisherMap =
        sessionDataStore.deleteByConnectIds(connectIds);

    if (removeSubAndWat) {
      sessionInterests.deleteByConnectIds(connectIds);
      sessionWatchers.deleteByConnectIds(connectIds);
    }

    Map<ConnectId, List<Publisher>> ret = Maps.newHashMap();
    for (Entry<ConnectId, Map<String, Publisher>> entry : publisherMap.entrySet()) {
      ret.put(entry.getKey(), Lists.newArrayList(entry.getValue().values()));
    }
    return ret;
  }

  private void clientOffToDataNode(ConnectId connectId, List<Publisher> clientOffPublishers) {
    if (CollectionUtils.isEmpty(clientOffPublishers)) {
      return;
    }
    writeDataAcceptor.accept(new ClientOffWriteDataRequest(connectId, clientOffPublishers));
  }

  private final class ClientWatchDog extends LoopRunnable {

    @Override
    public void runUnthrowable() {
      try {
        cleanClientConnect();
      } catch (Throwable e) {
        LOGGER.error("WatchDog failed to cleanClientConnect", e);
      }
    }

    @Override
    public void waitingUnthrowable() {
      ConcurrentUtils.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }
  }

  private final class VersionWatchDog extends WakeUpLoopRunnable {
    boolean prevStopPushSwitch;
    long scanRound;
    long lastScanTimestamp;

    @Override
    public void runUnthrowable() {
      try {
        final int intervalMillis = sessionServerConfig.getSchedulerScanVersionIntervalMillis();
        final boolean stop = !pushSwitchService.canPush();
        // could not start scan ver at begin
        // 1. stopPush.val = true default in session.default
        if (stop) {
          SCAN_VER_LOGGER.info("[stopPush]");
        } else {
          final long now = System.currentTimeMillis();
          // abs avoid the clock attack
          if (Math.abs(now - lastScanTimestamp) >= intervalMillis || prevStopPushSwitch) {
            try {
              scanVersions(scanRound++);
              scanSubscribers();
            } finally {
              lastScanTimestamp = System.currentTimeMillis();
            }
          }
        }
        prevStopPushSwitch = stop;
      } catch (Throwable e) {
        SCAN_VER_LOGGER.error("WatchDog failed fetch versions", e);
      }
    }

    @Override
    public int getWaitingMillis() {
      return 1000;
    }
  }

  private void scanVersions(long round) {
    // TODO not support multi cluster
    final String dataCenter = sessionServerConfig.getSessionServerDataCenter();
    Map<String, DatumVersion> interestVersions =
        sessionInterests.getInterestVersions(sessionServerConfig.getSessionServerDataCenter());
    Map<Integer, Map<String, DatumVersion>> interestVersionsGroup = groupBySlot(interestVersions);

    Map<Integer, FetchVersionResult> resultMap =
        Maps.newHashMapWithExpectedSize(interestVersions.size());
    for (Map.Entry<Integer, Map<String, DatumVersion>> group : interestVersionsGroup.entrySet()) {
      final Integer slotId = group.getKey();
      try {
        final FetchVersionResult result =
            fetchDataVersionAsync(dataCenter, slotId, group.getValue(), round);
        if (result != null) {
          resultMap.put(slotId, result);
        }
      } catch (Throwable e) {
        SCAN_VER_LOGGER.error(
            "failed to fetch versions slotId={}, size={}", slotId, group.getValue().size(), e);
      }
    }
    final int timeoutMillis = sessionServerConfig.getDataNodeExchangeTimeoutMillis();
    final long waitDeadline = System.currentTimeMillis() + timeoutMillis + 1000;
    // wait async finish
    ConcurrentUtils.sleepUninterruptibly(100, TimeUnit.MILLISECONDS);
    // check callback result, use for.count to avoid the clock skew
    for (int i = 0; i < (timeoutMillis * 2) / 50; i++) {
      handleFetchResult(round, dataCenter, resultMap);
      if (resultMap.isEmpty() || System.currentTimeMillis() > waitDeadline) {
        break;
      }
      ConcurrentUtils.sleepUninterruptibly(50, TimeUnit.MILLISECONDS);
    }
    if (!resultMap.isEmpty()) {
      SCAN_VER_LOGGER.error("[fetchSlotVerTimeout]callbacks={},{}", resultMap.size(), resultMap);
    }
  }

  int handleFetchResult(long round, String dataCenter, Map<Integer, FetchVersionResult> resultMap) {
    int count = 0;
    final Iterator<Map.Entry<Integer, FetchVersionResult>> it = resultMap.entrySet().iterator();
    while (it.hasNext()) {
      final Map.Entry<Integer, FetchVersionResult> e = it.next();
      FetchVersionResult result = e.getValue();
      if (result.callback == null) {
        // not finish
        continue;
      }
      it.remove();
      count++;
      // success
      if (result.callback.versions != null) {
        final long now = System.currentTimeMillis();
        for (Map.Entry<String, DatumVersion> version : result.callback.versions.entrySet()) {
          final String dataInfoId = version.getKey();
          final long verVal = version.getValue().getValue();
          if (sessionInterests.checkInterestVersion(dataCenter, dataInfoId, verVal).interested) {
            TriggerPushContext ctx = new TriggerPushContext(dataCenter, verVal, result.leader, now);
            firePushService.fireOnChange(dataInfoId, ctx);
            SCAN_VER_LOGGER.info(
                "[fetchSlotVerNotify]round={},{},{},{},{}",
                round,
                e.getKey(),
                dataInfoId,
                dataCenter,
                verVal);
          }
        }
      }
    }
    return count;
  }

  public String getDataCenterWhenPushEmpty() {
    // TODO cloud mode use default.datacenter?
    return sessionServerConfig.getSessionServerDataCenter();
  }

  private void scanSubscribers() {
    List<Subscriber> subscribers = sessionInterests.getDataList();
    int regCount = 0;
    int emptyCount = 0;
    int circuitBreaker = 0;
    final String dataCenter = getDataCenterWhenPushEmpty();
    for (Subscriber subscriber : subscribers) {
      try {
        CircuitBreakerStatistic statistic = subscriber.getStatistic(dataCenter);

        if (CircuitBreakerUtil.circuitBreaker(
            statistic,
            sessionServerConfig.getPushCircuitBreakerThreshold(),
            sessionServerConfig.getPushCircuitBreakerSleepMillis())) {
          circuitBreaker++;
          continue;
        }

        if (subscriber.needPushEmpty(dataCenter)) {
          firePushService.fireOnPushEmpty(subscriber, dataCenter);
          emptyCount++;
          continue;
        }
        if (!subscriber.hasPushed()) {
          firePushService.fireOnRegister(subscriber);
          regCount++;
        }
      } catch (Throwable e) {
        SCAN_VER_LOGGER.error("failed to scan subscribers, {}", subscriber, e);
      }
    }
    SCAN_VER_LOGGER.info(
        "scan subscribers, total={}, reg={}, empty={}, circuitBreaker={}",
        subscribers.size(),
        regCount,
        emptyCount,
        circuitBreaker);
  }

  private Map<Integer, Map<String, DatumVersion>> groupBySlot(
      Map<String, DatumVersion> interestVersions) {
    if (CollectionUtils.isEmpty(interestVersions)) {
      return Collections.emptyMap();
    }
    TreeMap<Integer, Map<String, DatumVersion>> ret = Maps.newTreeMap();
    for (Map.Entry<String, DatumVersion> interestVersion : interestVersions.entrySet()) {
      final String dataInfoId = interestVersion.getKey();
      Map<String, DatumVersion> map =
          ret.computeIfAbsent(
              slotTableCache.slotOf(dataInfoId), k -> Maps.newHashMapWithExpectedSize(128));
      map.put(dataInfoId, interestVersion.getValue());
    }
    return ret;
  }

  private static final class FetchVersionResult {
    final String leader;
    final int slotId;
    volatile FetchVersionCallback callback;

    FetchVersionResult(int slotId, String leader) {
      this.leader = leader;
      this.slotId = slotId;
    }

    @Override
    public String toString() {
      return StringFormatter.format(
          "FetchResult{slotId={},{},finish={}}", slotId, leader, callback != null);
    }
  }

  private static final class FetchVersionCallback {
    final Map<String, DatumVersion> versions;

    FetchVersionCallback(Map<String, DatumVersion> versions) {
      this.versions = versions;
    }
  }

  FetchVersionResult fetchDataVersionAsync(
      String dataCenter, int slotId, Map<String, DatumVersion> interestVersions, long round) {
    final String leader = slotTableCache.getLeader(slotId);
    if (StringUtils.isBlank(leader)) {
      SCAN_VER_LOGGER.warn("round={}, slot not assigned, {}", round, slotId);
      return null;
    }
    final FetchVersionResult result = new FetchVersionResult(slotId, leader);
    dataNodeService.fetchDataVersion(
        dataCenter,
        slotId,
        interestVersions,
        new ExchangeCallback<Map<String, DatumVersion>>() {
          @Override
          public void onCallback(Channel channel, Map<String, DatumVersion> message) {
            // merge the version
            Map<String, DatumVersion> mergedVersions = new HashMap<>(interestVersions);
            mergedVersions.putAll(message);
            result.callback = new FetchVersionCallback(mergedVersions);
            SCAN_VER_LOGGER.info(
                "[fetchSlotVer]round={},{},{},leader={},interests={},gets={}",
                round,
                slotId,
                dataCenter,
                leader,
                interestVersions.size(),
                message.size());
          }

          @Override
          public void onException(Channel channel, Throwable e) {
            result.callback = new FetchVersionCallback(null);
            SCAN_VER_LOGGER.error(
                "round={},failed to fetch versions,slotId={},leader={},size={}",
                round,
                slotId,
                leader,
                interestVersions.size(),
                e);
          }
        });
    return result;
  }

  public void cleanClientConnect() {
    Set<ConnectId> connectIndexes = Sets.newHashSetWithExpectedSize(1024);
    connectIndexes.addAll(sessionDataStore.getConnectIds());
    connectIndexes.addAll(sessionInterests.getConnectIds());
    connectIndexes.addAll(sessionWatchers.getConnectIds());

    Server sessionServer = boltExchange.getServer(sessionServerConfig.getServerPort());

    List<ConnectId> connectIds = new ArrayList<>();
    for (ConnectId connectId : connectIndexes) {
      Channel channel =
          sessionServer.getChannel(
              new URL(connectId.getClientHostAddress(), connectId.getClientPort()));
      if (channel == null) {
        connectIds.add(connectId);
        LOGGER.warn("Client connect has not existed! connectId:{}", connectId);
      }
    }
    clean(connectIds);
  }

  /**
   * Getter method for property <tt>sessionInterests</tt>.
   *
   * @return property value of sessionInterests
   */
  protected Interests getSessionInterests() {
    return sessionInterests;
  }

  /**
   * Getter method for property <tt>sessionDataStore</tt>.
   *
   * @return property value of sessionDataStore
   */
  protected DataStore getSessionDataStore() {
    return sessionDataStore;
  }

  /**
   * Getter method for property <tt>taskListenerManager</tt>.
   *
   * @return property value of taskListenerManager
   */
  protected TaskListenerManager getTaskListenerManager() {
    return taskListenerManager;
  }
}
