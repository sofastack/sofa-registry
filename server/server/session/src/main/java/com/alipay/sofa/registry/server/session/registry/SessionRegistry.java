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
import com.alipay.sofa.registry.remoting.exchange.RequestChannelClosedException;
import com.alipay.sofa.registry.server.session.acceptor.ClientOffWriteDataRequest;
import com.alipay.sofa.registry.server.session.acceptor.PublisherWriteDataRequest;
import com.alipay.sofa.registry.server.session.acceptor.WriteDataAcceptor;
import com.alipay.sofa.registry.server.session.acceptor.WriteDataRequest;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.filter.DataIdMatchStrategy;
import com.alipay.sofa.registry.server.session.node.service.DataNodeService;
import com.alipay.sofa.registry.server.session.push.FirePushService;
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
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.*;
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

  @Autowired private Exchange boltExchange;

  @Autowired private SessionRegistryStrategy sessionRegistryStrategy;

  @Autowired private WrapperInterceptorManager wrapperInterceptorManager;

  @Autowired private DataIdMatchStrategy dataIdMatchStrategy;

  @Autowired private WriteDataAcceptor writeDataAcceptor;

  @Autowired private SlotTableCache slotTableCache;

  @Autowired private FirePushService firePushService;

  private final VersionWatchDog versionWatchDog = new VersionWatchDog();

  private volatile boolean scanVerEnable = false;

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
  public void cancel(List<ConnectId> connectIds) {
    // update local firstly, data node send error depend on renew check
    for (ConnectId connectId : connectIds) {
      List<Publisher> removes = removeFromSession(connectId);
      if (!removes.isEmpty()) {
        // clientOff to dataNode async
        clientOffToDataNode(connectId, removes);
      }
    }
  }

  private List<Publisher> removeFromSession(ConnectId connectId) {
    Map<String, Publisher> publisherMap = sessionDataStore.deleteByConnectId(connectId);
    sessionInterests.deleteByConnectId(connectId);
    sessionWatchers.deleteByConnectId(connectId);
    return Lists.newArrayList(publisherMap.values());
  }

  private void clientOffToDataNode(ConnectId connectId, List<Publisher> clientOffPublishers) {
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

    @Override
    public void runUnthrowable() {
      try {
        final boolean stop = sessionServerConfig.isStopPushSwitch();
        // could not start scan ver at begin
        // 1. stopPush.val = false in session.default
        // 2. stopPush.val = true in meta
        // 3. scanVerEnable=true after session start and config the stopPush.val
        if (!stop && scanVerEnable) {
          scanVersions();
          triggerSubscriberRegister();
          if (prevStopPushSwitch) {
            SCAN_VER_LOGGER.info("[ReSub] resub after stopPushSwitch closed");
          }
        }
        prevStopPushSwitch = stop;
      } catch (Throwable e) {
        SCAN_VER_LOGGER.error("WatchDog failed fetch versions", e);
      }
    }

    @Override
    public int getWaitingMillis() {
      return sessionServerConfig.getSchedulerScanVersionIntervalMillis();
    }
  }

  private void scanVersions() {
    // TODO not support multi cluster
    Map<String, DatumVersion> interestVersions =
        sessionInterests.getInterestVersions(sessionServerConfig.getSessionServerDataCenter());
    Map<Integer, Map<String, DatumVersion>> interestVersionsGroup = groupBySlot(interestVersions);
    for (Map.Entry<Integer, Map<String, DatumVersion>> group : interestVersionsGroup.entrySet()) {
      final int slotId = group.getKey();
      try {
        fetchChangDataProcess(
            sessionServerConfig.getSessionServerDataCenter(), slotId, group.getValue());
      } catch (Throwable e) {
        SCAN_VER_LOGGER.error(
            "failed to fetch versions slotId={}, size={}", slotId, group.getValue().size(), e);
      }
    }
  }

  private void triggerSubscriberRegister() {
    Collection<Subscriber> subscribers = sessionInterests.getInterestsNeverPushed();
    if (!subscribers.isEmpty()) {
      SCAN_VER_LOGGER.info("find never pushed subscribers:{}", subscribers.size());
      for (Subscriber subscriber : subscribers) {
        try {
          firePushService.fireOnRegister(subscriber);
        } catch (Throwable e) {
          SCAN_VER_LOGGER.error("failed to register subscriber, {}", subscriber, e);
        }
      }
    }
  }

  @Override
  public void fetchChangDataProcess() {
    scanVerEnable = true;
    versionWatchDog.wakeup();
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

  private void fetchChangDataProcess(
      String dataCenter, int slotId, Map<String, DatumVersion> interestVersions) {
    final String leader = slotTableCache.getLeader(slotId);
    if (StringUtils.isBlank(leader)) {
      SCAN_VER_LOGGER.warn("slot not assigned, {}", slotId);
      return;
    }

    Map<String /*datainfoid*/, DatumVersion> dataVersions =
        dataNodeService.fetchDataVersion(dataCenter, slotId, interestVersions);

    SCAN_VER_LOGGER.info(
        "[fetchSlotVer]{},{},{},interests={},gets={}",
        slotId,
        dataCenter,
        leader,
        interestVersions.size(),
        dataVersions.size());
    Map<String, DatumVersion> mergedVersions = new HashMap<>(interestVersions);
    mergedVersions.putAll(dataVersions);
    final long now = System.currentTimeMillis();
    for (Map.Entry<String, DatumVersion> version : mergedVersions.entrySet()) {
      final String dataInfoId = version.getKey();
      final long verVal = version.getValue().getValue();
      if (sessionInterests.checkInterestVersion(dataCenter, dataInfoId, verVal).interested) {
        TriggerPushContext ctx = new TriggerPushContext(dataCenter, verVal, leader, now);
        firePushService.fireOnChange(dataInfoId, ctx);
        SCAN_VER_LOGGER.info(
            "[fetchSlotVerNotify]{},{},{},{}", slotId, dataInfoId, dataCenter, verVal);
      }
    }
  }

  public void remove(List<ConnectId> connectIds) {
    List<ConnectId> connectIdsAll = new ArrayList<>();
    connectIds.forEach(
        connectId -> {
          Map pubMap = getSessionDataStore().queryByConnectId(connectId);
          boolean pubExisted = pubMap != null && !pubMap.isEmpty();

          Map<String, Subscriber> subMap = getSessionInterests().queryByConnectId(connectId);
          boolean subExisted = false;
          if (subMap != null && !subMap.isEmpty()) {
            subExisted = true;

            subMap.forEach(
                (registerId, sub) -> {
                  if (isFireSubscriberPushEmptyTask(sub.getDataId())) {
                    fireSubscriberPushEmptyTask(sub);
                  }
                });
          }

          if (pubExisted || subExisted) {
            connectIdsAll.add(connectId);
          }
        });
    cancel(connectIdsAll);
  }

  protected boolean isFireSubscriberPushEmptyTask(String dataId) {
    return dataIdMatchStrategy.match(
        dataId, () -> sessionServerConfig.getBlacklistSubDataIdRegex());
  }

  private void fireSubscriberPushEmptyTask(Subscriber subscriber) {
    firePushService.fireOnPushEmpty(subscriber);
  }

  public void cleanClientConnect() {
    Set<ConnectId> connectIndexes = new HashSet<>();
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
        LOGGER.warn("Client connect has not existed!it must be remove!connectId:{}", connectId);
      }
    }
    if (!connectIds.isEmpty()) {
      cancel(connectIds);
    }
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
