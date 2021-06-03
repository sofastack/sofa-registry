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
package com.alipay.sofa.registry.server.meta.provide.data;

import com.alipay.sofa.registry.common.model.ServerDataBox;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ClientManagerPods;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.common.model.metaserver.ProvideDataChangeEvent;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.resource.ClientManagerResource;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.meta.ClientManagerPodsRepository;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.LoopRunnable;
import com.alipay.sofa.registry.util.MathUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

/**
 * @author xiaojian.xj
 * @version $Id: DefaultClientManagerService.java, v 0.1 2021年05月12日 15:16 xiaojian.xj Exp $
 */
public class DefaultClientManagerService implements ClientManagerService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultClientManagerService.class);

  private static final Logger taskLogger =
      LoggerFactory.getLogger(ClientManagerResource.class, "[Task]");

  protected ReadWriteLock lock = new ReentrantReadWriteLock();

  /** The Read lock. */
  protected Lock readLock = lock.readLock();

  /** The Write lock. */
  protected Lock writeLock = lock.writeLock();

  private final AtomicLong version = new AtomicLong();

  private final AtomicReference<ConcurrentHashMap.KeySetView> cache = new AtomicReference<>();

  private ClientManagerWatcher watcher = new ClientManagerWatcher();

  private ClientManagerRefresher refresher = new ClientManagerRefresher();

  private final AtomicBoolean refreshFinish = new AtomicBoolean(false);

  @Autowired private ClientManagerPodsRepository clientManagerPodsRepository;

  @Autowired private DefaultProvideDataNotifier provideDataNotifier;

  @Autowired private MetaServerConfig metaServerConfig;

  @Autowired private MetaLeaderService metaLeaderService;

  private int refreshLimit;

  public void init() {
    version.set(-1L);
    cache.set(new ConcurrentHashMap<>().newKeySet());
    refreshFinish.set(false);
  }

  @PostConstruct
  public void postConstruct() {
    init();

    ConcurrentUtils.createDaemonThread("clientManager_watcher", watcher).start();
    ConcurrentUtils.createDaemonThread("clientManager_refresher", refresher).start();

    refreshLimit = metaServerConfig.getClientManagerRefreshLimit();
  }

  /**
   * client open
   *
   * @param ipSet
   * @return
   */
  @Override
  public boolean clientOpen(Set<String> ipSet) {
    return clientManagerPodsRepository.clientOpen(ipSet);
  }

  /**
   * client off
   *
   * @param ipSet
   * @return
   */
  @Override
  public boolean clientOff(Set<String> ipSet) {
    return clientManagerPodsRepository.clientOff(ipSet);
  }

  /**
   * query client off ips
   *
   * @return
   */
  @Override
  public DBResponse<ProvideData> queryClientOffSet() {
    if (!refreshFinish.get()) {
      LOGGER.warn("query client manager cache before refreshFinish");
      return DBResponse.notfound().build();
    }

    readLock.lock();
    try {
      ProvideData provideData =
          new ProvideData(
              new ServerDataBox(cache.get()),
              ValueConstants.CLIENT_OFF_PODS_DATA_ID,
              version.get());
      return DBResponse.ok(provideData).build();
    } catch (Throwable t) {
      LOGGER.error("query client manager cache error.", t);
      return DBResponse.notfound().build();
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public void becomeLeader() {
    refresher.runUnthrowable();
  }

  @Override
  public void loseLeader() {}

  private final class ClientManagerRefresher extends LoopRunnable {

    @Override
    public void runUnthrowable() {
      if (!metaLeaderService.amILeader()) {
        return;
      }

      init();

      final int total = clientManagerPodsRepository.queryTotalCount();

      // add 10, query the new records which inserted when scanning
      final int refreshCount = MathUtils.divideCeil(total, refreshLimit) + 10;
      LOGGER.info("begin load clientManager, total count {}, rounds={}", total, refreshCount);

      long maxTemp = -1;
      int refreshTotal = 0;
      List<ClientManagerPods> totalRet = new ArrayList<>();
      for (int i = 0; i < refreshCount; i++) {
        List<ClientManagerPods> clientManagerPods =
            clientManagerPodsRepository.queryAfterThan(maxTemp, refreshLimit);
        final int num = clientManagerPods.size();
        LOGGER.info("load clientManager in round={}, num={}", i, num);
        if (num == 0) {
          break;
        }

        refreshTotal += num;
        maxTemp = clientManagerPods.get(clientManagerPods.size() - 1).getId();
        totalRet.addAll(clientManagerPods);
      }
      LOGGER.info("finish load clientManager, total={}, maxId={}", refreshTotal, maxTemp);

      ClientManagerAggregation aggregation = aggregate(totalRet);

      if (aggregation == EMPTY_AGGREGATION || doRefresh(aggregation)) {
        refreshFinish.set(true);
        LOGGER.info("finish load clientManager, refreshFinish:{}", refreshFinish.get());
        fireClientManagerChangeNotify(version.get(), ValueConstants.CLIENT_OFF_PODS_DATA_ID);
      }
    }

    @Override
    public void waitingUnthrowable() {
      ConcurrentUtils.sleepUninterruptibly(
          metaServerConfig.getClientManagerRefreshSecs(), TimeUnit.SECONDS);
    }
  }

  private final class ClientManagerWatcher extends LoopRunnable {

    @Override
    public void runUnthrowable() {
      if (!metaLeaderService.amILeader()) {
        return;
      }

      List<ClientManagerPods> clientManagerPods =
          clientManagerPodsRepository.queryAfterThan(version.get());

      if (CollectionUtils.isEmpty(clientManagerPods)) {
        return;
      }

      ClientManagerAggregation aggregation = aggregate(clientManagerPods);

      LOGGER.info("client manager watcher aggregation:{}", aggregation);
      if (doRefresh(aggregation)) {
        fireClientManagerChangeNotify(version.get(), ValueConstants.CLIENT_OFF_PODS_DATA_ID);
      }
    }

    @Override
    public void waitingUnthrowable() {
      ConcurrentUtils.sleepUninterruptibly(
          metaServerConfig.getClientManagerWatchMillis(), TimeUnit.MILLISECONDS);
    }
  }

  private ClientManagerAggregation aggregate(List<ClientManagerPods> clientManagerPods) {
    if (CollectionUtils.isEmpty(clientManagerPods)) {
      return EMPTY_AGGREGATION;
    }

    long max = clientManagerPods.get(clientManagerPods.size() - 1).getId();
    Set<String> clientOffPods = new HashSet<>();
    Set<String> clientOpenPods = new HashSet<>();
    for (ClientManagerPods clientManagerPod : clientManagerPods) {
      switch (clientManagerPod.getOperation()) {
        case ValueConstants.CLIENT_OFF:
          clientOffPods.add(clientManagerPod.getAddress());
          clientOpenPods.remove(clientManagerPod.getAddress());
          break;
        case ValueConstants.CLIENT_OPEN:
          clientOpenPods.add(clientManagerPod.getAddress());
          clientOffPods.remove(clientManagerPod.getAddress());
          break;
        default:
          LOGGER.error("error operation type: {}", clientManagerPod);
          break;
      }
    }
    return new ClientManagerAggregation(max, clientOffPods, clientOpenPods);
  }

  private boolean doRefresh(ClientManagerAggregation aggregation) {
    long before;
    writeLock.lock();
    try {
      before = version.get();
      if (before >= aggregation.max) {
        return false;
      }
      version.set(aggregation.max);
      cache.get().addAll(aggregation.clientOffPods);
      cache.get().removeAll(aggregation.clientOpenPods);
    } catch (Throwable t) {
      LOGGER.error("refresh client manager cache error.", t);
      return false;
    } finally {
      writeLock.unlock();
    }
    LOGGER.info(
        "doRefresh success, before:{}, after:{}, clientOff:{}, clientOpen:{}",
        before,
        aggregation.max,
        aggregation.clientOffPods,
        aggregation.clientOpenPods);
    return true;
  }

  private void fireClientManagerChangeNotify(Long version, String dataInfoId) {

    ProvideDataChangeEvent provideDataChangeEvent = new ProvideDataChangeEvent(dataInfoId, version);

    if (taskLogger.isInfoEnabled()) {
      taskLogger.info(
          "send CLIENT_MANAGER_CHANGE_NOTIFY_TASK notifyClientManagerChange: {}",
          provideDataChangeEvent);
    }
    provideDataNotifier.notifyProvideDataChange(provideDataChangeEvent);
  }

  private final ClientManagerAggregation EMPTY_AGGREGATION =
      new ClientManagerAggregation(-1L, Sets.newHashSet(), Sets.newHashSet());

  final class ClientManagerAggregation {
    final long max;

    final Set<String> clientOffPods;

    final Set<String> clientOpenPods;

    public ClientManagerAggregation(
        long max, Set<String> clientOffPods, Set<String> clientOpenPods) {
      this.max = max;
      this.clientOffPods = clientOffPods;
      this.clientOpenPods = clientOpenPods;
    }

    @Override
    public String toString() {
      return "ClientManagerAggregation{"
          + "max="
          + max
          + ", clientOffPods="
          + clientOffPods
          + ", clientOpenPods="
          + clientOpenPods
          + '}';
    }
  }

  /**
   * Setter method for property <tt>clientManagerPodsRepository</tt>.
   *
   * @param clientManagerPodsRepository value to be assigned to property clientManagerPodsRepository
   */
  @VisibleForTesting
  public void setClientManagerPodsRepository(
      ClientManagerPodsRepository clientManagerPodsRepository) {
    this.clientManagerPodsRepository = clientManagerPodsRepository;
  }
}
