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
import com.alipay.sofa.registry.common.model.Tuple;
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
import com.alipay.sofa.registry.server.session.loggers.Loggers;
import com.alipay.sofa.registry.server.session.node.service.DataNodeService;
import com.alipay.sofa.registry.server.session.providedata.ConfigProvideDataWatcher;
import com.alipay.sofa.registry.server.session.push.FirePushService;
import com.alipay.sofa.registry.server.session.push.PushSwitchService;
import com.alipay.sofa.registry.server.session.push.TriggerPushContext;
import com.alipay.sofa.registry.server.session.slot.SlotTableCache;
import com.alipay.sofa.registry.server.session.store.DataStore;
import com.alipay.sofa.registry.server.session.store.Interests;
import com.alipay.sofa.registry.server.session.store.Watchers;
import com.alipay.sofa.registry.server.session.strategy.SessionRegistryStrategy;
import com.alipay.sofa.registry.server.session.wrapper.RegisterInvokeData;
import com.alipay.sofa.registry.server.session.wrapper.Wrapper;
import com.alipay.sofa.registry.server.session.wrapper.WrapperInterceptorManager;
import com.alipay.sofa.registry.server.session.wrapper.WrapperInvocation;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
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

  protected static final Logger LOGGER = LoggerFactory.getLogger(SessionRegistry.class);

  protected static final Logger SCAN_VER_LOGGER = LoggerFactory.getLogger("SCAN-VER");

  /** store subscribers */
  @Autowired protected Interests sessionInterests;

  /** store watchers */
  @Autowired protected Watchers sessionWatchers;

  /** store publishers */
  @Autowired protected DataStore sessionDataStore;

  /** transfer data to DataNode */
  @Autowired protected DataNodeService dataNodeService;

  @Autowired protected SessionServerConfig sessionServerConfig;

  @Autowired protected PushSwitchService pushSwitchService;

  @Autowired protected Exchange boltExchange;

  @Autowired protected SessionRegistryStrategy sessionRegistryStrategy;

  @Autowired protected WrapperInterceptorManager wrapperInterceptorManager;

  @Autowired protected WriteDataAcceptor writeDataAcceptor;

  @Autowired protected SlotTableCache slotTableCache;

  @Autowired protected FirePushService firePushService;

  @Autowired protected ConfigProvideDataWatcher configProvideDataWatcher;

  private final VersionWatchDog versionWatchDog = new VersionWatchDog();

  @PostConstruct
  public void init() {
    ConcurrentUtils.createDaemonThread("SessionVerWatchDog", versionWatchDog).start();
    ConcurrentUtils.createDaemonThread("SessionClientWatchDog", new ClientWatchDog()).start();
  }

  @Override
  public void register(StoreData storeData, Channel channel) {

    WrapperInvocation<RegisterInvokeData, Boolean> wrapperInvocation =
        new WrapperInvocation(
            new Wrapper<RegisterInvokeData, Boolean>() {
              @Override
              public Boolean call() {

                switch (storeData.getDataType()) {
                  case PUBLISHER:
                    Publisher publisher = (Publisher) storeData;
                    publisher.setSessionProcessId(ServerEnv.PROCESS_ID);
                    if (!sessionDataStore.add(publisher)) {
                      break;
                    }
                    // All write operations to DataServer (pub/unPub/clientoff/renew/snapshot)
                    // are handed over to WriteDataAcceptor
                    writeDataAcceptor.accept(
                        new PublisherWriteDataRequest(
                            publisher, WriteDataRequest.WriteDataRequestType.PUBLISHER));

                    sessionRegistryStrategy.afterPublisherRegister(publisher);
                    break;
                  case SUBSCRIBER:
                    Subscriber subscriber = (Subscriber) storeData;

                    if (!sessionInterests.add(subscriber)) {
                      break;
                    }

                    sessionRegistryStrategy.afterSubscriberRegister(subscriber);
                    break;
                  case WATCHER:
                    Watcher watcher = (Watcher) storeData;

                    if (!sessionWatchers.add(watcher)) {
                      break;
                    }

                    sessionRegistryStrategy.afterWatcherRegister(watcher);
                    break;
                  default:
                    break;
                }
                return null;
              }

              @Override
              public Supplier<RegisterInvokeData> getParameterSupplier() {
                return () -> new RegisterInvokeData(storeData, channel);
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
        // no need to check whether the pub exist, make sure the unpub send to data
        sessionDataStore.deleteById(storeData.getId(), publisher.getDataInfoId());
        // All write operations to DataServer (pub/unPub/clientoff)
        // are handed over to WriteDataAcceptor
        writeDataAcceptor.accept(
            new PublisherWriteDataRequest(
                publisher, WriteDataRequest.WriteDataRequestType.UN_PUBLISHER));

        sessionRegistryStrategy.afterPublisherUnRegister(publisher);
        break;

      case SUBSCRIBER:
        Subscriber subscriber = (Subscriber) storeData;
        if (sessionInterests.deleteById(storeData.getId(), subscriber.getDataInfoId()) == null) {
          break;
        }
        sessionRegistryStrategy.afterSubscriberUnRegister(subscriber);
        break;

      case WATCHER:
        Watcher watcher = (Watcher) storeData;

        if (sessionWatchers.deleteById(watcher.getId(), watcher.getDataInfoId()) == null) {
          break;
        }
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
    ClientManagerMetric.CLIENT_OFF_COUNTER.inc(connectIds.size());
    disableConnect(connectIds, false, true);
  }

  @Override
  public void clientOffWithTimestampCheck(Map<ConnectId, Long> connectIds) {
    ClientManagerMetric.CLIENT_OFF_COUNTER.inc(connectIds.size());
    disableConnect(connectIds.keySet(), false, true, connectIds);
  }

  @Override
  public void blacklist(List<ConnectId> connectIds) {
    disableConnect(connectIds, true, true);
  }

  private void disableConnect(
      List<ConnectId> connectIds, boolean removeSubAndWat, boolean checkSub) {
    Set<ConnectId> connectIdSet = Collections.unmodifiableSet(Sets.newHashSet(connectIds));
    disableConnect(connectIdSet, removeSubAndWat, checkSub, Collections.emptyMap());
  }

  private void disableConnect(
      Set<ConnectId> connectIdSet,
      boolean removeSubAndWat,
      boolean checkSub,
      Map<ConnectId, Long> connectIdVersions) {
    if (CollectionUtils.isEmpty(connectIdSet)) {
      return;
    }
    Loggers.CLIENT_DISABLE_LOG.info(
        "disable connectId={}, removeSubAndWat={}, checkSub={}, {}",
        connectIdSet.size(),
        removeSubAndWat,
        checkSub,
        connectIdSet);

    final String dataCenter = getDataCenterWhenPushEmpty();

    if (checkSub) {
      Map<ConnectId, Map<String, Subscriber>> subMap =
          sessionInterests.queryByConnectIds(connectIdSet);
      for (Entry<ConnectId, Map<String, Subscriber>> subEntry : subMap.entrySet()) {
        int subEmptyCount = 0;
        for (Subscriber sub : subEntry.getValue().values()) {
          if (isPushEmpty(sub)) {
            Long clientOffVersion = connectIdVersions.get(subEntry.getKey());
            if (clientOffVersion != null && clientOffVersion < sub.getRegisterTimestamp()) {
              Loggers.CLIENT_DISABLE_LOG.error(
                  "[ClientOffVersionError]subEmpty,{},{},{}, clientOffVersion={} is smaller than subRegisterTimestamp={}",
                  sub.getDataInfoId(),
                  dataCenter,
                  subEntry.getKey(),
                  clientOffVersion,
                  sub.getRegisterTimestamp());
              continue;
            }

            subEmptyCount++;
            firePushService.fireOnPushEmpty(sub, dataCenter);
            Loggers.CLIENT_DISABLE_LOG.info(
                "subEmpty,{},{},{}", sub.getDataInfoId(), dataCenter, subEntry.getKey());
          }
        }
        Loggers.CLIENT_DISABLE_LOG.info(
            "connectId={}, subEmpty={}", subEntry.getKey(), subEmptyCount);
      }
    }

    Map<ConnectId, List<Publisher>> pubMap = removeFromSession(connectIdSet, removeSubAndWat);
    for (Entry<ConnectId, List<Publisher>> pubEntry : pubMap.entrySet()) {
      clientOffToDataNode(pubEntry.getKey(), pubEntry.getValue());
      Loggers.CLIENT_DISABLE_LOG.info(
          "connectId={}, pubRemove={}", pubEntry.getKey(), pubEntry.getValue().size());
    }
  }

  public boolean isPushEmpty(Subscriber subscriber) {
    // mostly, do not need to push empty
    return false;
  }

  private Map<ConnectId, List<Publisher>> removeFromSession(
      Set<ConnectId> connectIds, boolean removeSubAndWat) {
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
      ConcurrentUtils.sleepUninterruptibly(5, TimeUnit.SECONDS);
    }
  }

  private final class VersionWatchDog extends WakeUpLoopRunnable {
    boolean prevStopPushSwitch;
    long scanRound;
    long lastScanTimestamp;

    @Override
    public void runUnthrowable() {
      try {
        final int intervalMillis = sessionServerConfig.getScanSubscriberIntervalMillis();
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
              scanSubscribers(scanRound++);
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

  private Tuple<Map<String, DatumVersion>, List<Subscriber>> selectSubscribers(
      long round, String dataCenter) {
    final long start = System.currentTimeMillis();
    Tuple<Map<String, DatumVersion>, List<Subscriber>> tuple =
        sessionInterests.selectSubscribers(dataCenter);
    SCAN_VER_LOGGER.info(
        "[select]round={}, interestSize={}, pushEmptySize={}, span={}",
        round,
        tuple.o1.size(),
        tuple.o2.size(),
        System.currentTimeMillis() - start);
    return tuple;
  }

  private void scanSubscribers(long round) {
    final String dataCenter = sessionServerConfig.getSessionServerDataCenter();
    final Tuple<Map<String, DatumVersion>, List<Subscriber>> tuple =
        selectSubscribers(round, dataCenter);
    final Map<String, DatumVersion> interestVersions = tuple.o1;
    final List<Subscriber> toPushEmptySubscribers = tuple.o2;
    try {
      scanVersions(round, dataCenter, interestVersions);
    } catch (Throwable e) {
      SCAN_VER_LOGGER.error("failed to scan version", e);
    }
    handlePushEmptySubscribers(toPushEmptySubscribers);
  }

  private void scanVersions(
      long round, String dataCenter, Map<String, DatumVersion> interestVersions) {
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
            "round={}, failed to fetch versions slotId={}, size={}",
            round,
            slotId,
            group.getValue().size(),
            e);
      }
    }
    final int timeoutMillis = sessionServerConfig.getDataNodeExchangeTimeoutMillis();
    final long waitDeadline = System.currentTimeMillis() + timeoutMillis + 2000;
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
    handleFetchResult(round, dataCenter, resultMap);
    if (!resultMap.isEmpty()) {
      SCAN_VER_LOGGER.error(
          "[fetchSlotVerTimeout]round={},callbacks={},{}", round, resultMap.size(), resultMap);
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

  private void handlePushEmptySubscribers(List<Subscriber> pushEmptySubscribers) {
    final String dataCenter = getDataCenterWhenPushEmpty();
    for (Subscriber subscriber : pushEmptySubscribers) {
      try {
        if (subscriber.needPushEmpty(dataCenter)) {
          firePushService.fireOnPushEmpty(subscriber, dataCenter);
        }
      } catch (Throwable e) {
        SCAN_VER_LOGGER.error("failed to scan subscribers, {}", subscriber.shortDesc(), e);
      }
    }
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
              slotTableCache.slotOf(dataInfoId), k -> Maps.newHashMapWithExpectedSize(256));
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
      SCAN_VER_LOGGER.error("[NoLeader]slotId={}, round={}", slotId, round);
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
                "[fetchSlotVer]round={},{},{},leader={},interests={},gets={},success={}",
                round,
                slotId,
                dataCenter,
                leader,
                interestVersions.size(),
                message.size(),
                "Y");
          }

          @Override
          public void onException(Channel channel, Throwable e) {
            result.callback = new FetchVersionCallback(null);
            SCAN_VER_LOGGER.info(
                "[fetchSlotVer]round={},{},{},leader={},interests={},gets={},success={}",
                round,
                slotId,
                dataCenter,
                leader,
                interestVersions.size(),
                0,
                "N");

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
    Server sessionServer = boltExchange.getServer(sessionServerConfig.getServerPort());
    if (sessionServer == null) {
      LOGGER.warn("server not init when clean connect: {}", sessionServerConfig.getServerPort());
      return;
    }

    Set<ConnectId> connectIndexes = Sets.newHashSetWithExpectedSize(1024 * 8);
    connectIndexes.addAll(sessionDataStore.getConnectIds());
    connectIndexes.addAll(sessionInterests.getConnectIds());
    connectIndexes.addAll(sessionWatchers.getConnectIds());

    List<ConnectId> connectIds = new ArrayList<>(64);
    for (ConnectId connectId : connectIndexes) {
      Channel channel =
          sessionServer.getChannel(
              new URL(connectId.getClientHostAddress(), connectId.getClientPort()));
      if (channel == null) {
        connectIds.add(connectId);
      }
    }
    clean(connectIds);
  }
}
