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
package com.alipay.sofa.registry.server.session.strategy.impl;

import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.common.model.store.BaseInfo;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.common.model.store.Watcher;
import com.alipay.sofa.registry.core.model.ReceivedConfigData;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.converter.ReceivedDataConverter;
import com.alipay.sofa.registry.server.session.providedata.ConfigProvideDataWatcher;
import com.alipay.sofa.registry.server.session.push.FirePushService;
import com.alipay.sofa.registry.server.session.push.PushSwitchService;
import com.alipay.sofa.registry.server.session.store.Watchers;
import com.alipay.sofa.registry.server.session.strategy.SessionRegistryStrategy;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.LoopRunnable;
import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections.CollectionUtils;
import org.glassfish.jersey.internal.guava.Sets;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author kezhu.wukz
 * @author xuanbei
 * @since 2019/2/15
 */
public class DefaultSessionRegistryStrategy implements SessionRegistryStrategy {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(DefaultSessionRegistryStrategy.class);

  @Autowired protected FirePushService firePushService;

  @Autowired protected SessionServerConfig sessionServerConfig;

  @Autowired protected PushSwitchService pushSwitchService;

  @Autowired protected ConfigProvideDataWatcher configProvideDataWatcher;

  @Autowired protected Watchers sessionWatchers;

  protected final WatcherScanDog watcherScanDog = new WatcherScanDog();
  private final Thread t = ConcurrentUtils.createDaemonThread("watcher-scan-dog", watcherScanDog);

  @Override
  public void start() {
    t.start();
  }

  @Override
  public void afterPublisherRegister(Publisher publisher) {}

  @Override
  public void afterSubscriberRegister(Subscriber subscriber) {
    if (pushSwitchService.canIpPushLocal(subscriber.getSourceAddress().getIpAddress())) {
      firePushService.fireOnRegister(subscriber);
    }
  }

  private boolean checkWatcherVersion(Watcher watcher) {
    return watcher.getClientVersion() == BaseInfo.ClientVersion.StoreData;
  }

  @Override
  public void afterWatcherRegister(Watcher watcher) {
    if (!checkWatcherVersion(watcher)) {
      LOGGER.warn(
          "unsupported watch:{}, clientVersion={}",
          watcher.getDataInfoId(),
          watcher.getClientVersion());
      return;
    }
    if (sessionServerConfig.isWatchConfigEnable()) {
      configProvideDataWatcher.watch(watcher.getDataInfoId());
    }
    if (!pushSwitchService.canIpPushLocal(watcher.getSourceAddress().getIpAddress())) {
      return;
    }
    ProvideData provideData = null;
    if (sessionServerConfig.isWatchConfigEnable()) {
      // the provideData maybe exist
      provideData = configProvideDataWatcher.get(watcher.getDataInfoId());
    } else {
      // just push null content provideData when disable
      provideData = new ProvideData(null, watcher.getDataInfoId(), null);
    }
    if (provideData != null) {
      ReceivedConfigData data =
          ReceivedDataConverter.createReceivedConfigData(watcher, provideData);
      firePushService.fireOnWatcher(watcher, data);
    }
  }

  boolean processWatchWhenWatchConfigDisable(Watcher w) {
    if (w.hasPushed()) {
      return false;
    }
    ProvideData provideData = new ProvideData(null, w.getDataInfoId(), null);
    ReceivedConfigData data = ReceivedDataConverter.createReceivedConfigData(w, provideData);
    firePushService.fireOnWatcher(w, data);
    return true;
  }

  boolean processWatchWhenWatchConfigEnable(Watcher w) {
    final String dataInfoId = w.getDataInfoId();
    final ProvideData provideData = configProvideDataWatcher.get(dataInfoId);
    if (provideData == null) {
      LOGGER.warn("[WatcherNoData]{}", dataInfoId);
      return false;
    }
    ReceivedConfigData data = null;
    if (!w.hasPushed()) {
      // first push
      data = ReceivedDataConverter.createReceivedConfigData(w, provideData);
    } else {
      Long v = provideData.getVersion();
      if (v != null && w.getPushedVersion() < v) {
        data = ReceivedDataConverter.createReceivedConfigData(w, provideData);
      }
    }
    if (data != null) {
      firePushService.fireOnWatcher(w, data);
      return true;
    }
    return false;
  }

  boolean processWatch(Watcher w, boolean watchEnable) {
    return watchEnable
        ? processWatchWhenWatchConfigEnable(w)
        : processWatchWhenWatchConfigDisable(w);
  }

  Tuple<Set<String>, List<Watcher>> filter() {
    List<Watcher> watchers = Lists.newLinkedList(sessionWatchers.getDataList());
    if (CollectionUtils.isEmpty(watchers)) {
      return null;
    }
    Set<String> dataInfoIds = Sets.newHashSetWithExpectedSize(1024);
    Iterator<Watcher> iter = watchers.iterator();
    while (iter.hasNext()) {
      Watcher w = iter.next();
      if (checkWatcherVersion(w)) {
        dataInfoIds.add(w.getDataInfoId());
      } else {
        iter.remove();
      }
    }
    return Tuple.of(dataInfoIds, watchers);
  }

  final class WatcherScanDog extends LoopRunnable {

    @Override
    public void runUnthrowable() {
      Tuple<Set<String>, List<Watcher>> filtered = filter();
      if (filtered == null) {
        return;
      }
      final boolean watchConfigEnable = sessionServerConfig.isWatchConfigEnable();
      if (watchConfigEnable) {
        configProvideDataWatcher.refreshWatch(filtered.o1);
      }

      int count = 0;
      for (Watcher w : filtered.o2) {
        if (processWatch(w, watchConfigEnable)) {
          count++;
        }
      }
      LOGGER.info("fire watchers:{}", count);
    }

    @Override
    public void waitingUnthrowable() {
      ConcurrentUtils.sleepUninterruptibly(
          sessionServerConfig.getScanWatcherIntervalMillis(), TimeUnit.MILLISECONDS);
    }
  }

  @Override
  public void afterPublisherUnRegister(Publisher publisher) {}

  @Override
  public void afterSubscriberUnRegister(Subscriber subscriber) {}

  @Override
  public void afterWatcherUnRegister(Watcher watcher) {}
}
