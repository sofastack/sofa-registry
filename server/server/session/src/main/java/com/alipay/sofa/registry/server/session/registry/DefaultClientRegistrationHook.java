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

import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.common.model.store.BaseInfo;
import com.alipay.sofa.registry.common.model.store.StoreData;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.common.model.store.Watcher;
import com.alipay.sofa.registry.core.model.ReceivedConfigData;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.converter.ReceivedDataConverter;
import com.alipay.sofa.registry.server.session.providedata.ConfigProvideDataWatcher;
import com.alipay.sofa.registry.server.session.push.FirePushService;
import com.alipay.sofa.registry.server.session.push.PushSwitchService;
import com.alipay.sofa.registry.server.session.store.Watchers;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.LoopRunnable;
import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.apache.commons.collections.CollectionUtils;
import org.glassfish.jersey.internal.guava.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/** Default implementation of {@link ClientRegistrationHook}. */
public class DefaultClientRegistrationHook implements ClientRegistrationHook {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultClientRegistrationHook.class);

  @Autowired protected SessionServerConfig sessionServerConfig;
  @Autowired protected FirePushService firePushService;
  @Autowired protected PushSwitchService pushSwitchService;
  @Autowired protected ConfigProvideDataWatcher configProvideDataWatcher;
  @Autowired protected Watchers sessionWatchers;

  @PostConstruct
  public void init() {
    ConcurrentUtils.createDaemonThread(
            "watcher-scan-dog",
            new LoopRunnable() {
              @Override
              public void runUnthrowable() {
                processWatch();
              }

              @Override
              public void waitingUnthrowable() {
                ConcurrentUtils.sleepUninterruptibly(
                    sessionServerConfig.getScanWatcherIntervalMillis(), TimeUnit.MILLISECONDS);
              }
            })
        .start();
  }

  @Override
  public void afterClientRegister(StoreData<?> storeData) {
    switch (storeData.getDataType()) {
      case PUBLISHER:
        // NOOP
        break;
      case SUBSCRIBER:
        afterSubscriberRegister((Subscriber) storeData);
        break;
      case WATCHER:
        afterWatcherRegister((Watcher) storeData);
        break;
      default:
        throw new IllegalArgumentException(
            "unsupported data type: " + storeData.getDataType().name());
    }
  }

  @Override
  public void afterClientUnregister(StoreData<?> storeData) {
    // NOOP
  }

  private void afterSubscriberRegister(Subscriber subscriber) {
    if (pushSwitchService.canIpPush(subscriber.getSourceAddress().getIpAddress())) {
      firePushService.fireOnRegister(subscriber);
    }
  }

  private void afterWatcherRegister(Watcher watcher) {
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
    if (!pushSwitchService.canIpPush(watcher.getSourceAddress().getIpAddress())) {
      return;
    }
    ProvideData provideData;
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

  private boolean checkWatcherVersion(Watcher watcher) {
    return watcher.getClientVersion() == BaseInfo.ClientVersion.StoreData;
  }

  public boolean processWatchWhenWatchConfigDisable(Watcher w) {
    if (w.hasPushed()) {
      return false;
    }
    ProvideData provideData = new ProvideData(null, w.getDataInfoId(), null);
    ReceivedConfigData data = ReceivedDataConverter.createReceivedConfigData(w, provideData);
    firePushService.fireOnWatcher(w, data);
    return true;
  }

  public boolean processWatchWhenWatchConfigEnable(Watcher w) {
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

  public boolean processWatch(Watcher w, boolean watchEnable) {
    return watchEnable
        ? processWatchWhenWatchConfigEnable(w)
        : processWatchWhenWatchConfigDisable(w);
  }

  public Tuple<Set<String>, List<Watcher>> filter() {
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

  public void processWatch() {
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
}
