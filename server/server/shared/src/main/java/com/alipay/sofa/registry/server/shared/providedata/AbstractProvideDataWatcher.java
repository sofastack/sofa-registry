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
package com.alipay.sofa.registry.server.shared.providedata;

import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractProvideDataWatcher {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractProvideDataWatcher.class);
  private final Map<String, DataWatcher> watcherMap = new ConcurrentHashMap<>(1024);
  @Autowired protected MetaServerService metaNodeService;

  protected final WatcherDog watcherDog = new WatcherDog();

  protected final String name;
  protected final Thread t;

  protected AbstractProvideDataWatcher(String name) {
    this.name = name;
    t = ConcurrentUtils.createDaemonThread("provide-data-watcher-" + name, watcherDog);
  }

  public void start() {
    t.start();
  }

  public boolean watch(String dataInfoId) {
    DataWatcher watcher = watcherMap.get(dataInfoId);
    if (watcher != null) {
      watcher.watchTimestamp = System.currentTimeMillis();
      return true;
    }
    if (watcherMap.putIfAbsent(dataInfoId, new DataWatcher(dataInfoId)) == null) {
      watcherDog.wakeup();
      return true;
    }
    return false;
  }

  public int refreshWatch(Collection<String> watchers) {
    int count = 0;
    for (String dataID : watchers) {
      if (watch(dataID)) {
        count++;
      }
    }
    return count;
  }

  protected final class WatcherDog extends WakeUpLoopRunnable {

    @Override
    public void runUnthrowable() {
      clean();
      // TODO need to avoid fetch too frequently by wakeup
      fetch();
    }

    @Override
    public int getWaitingMillis() {
      return fetchIntervalMillis();
    }
  }

  void clean() {
    final long expireTs = System.currentTimeMillis() - watcherLeaseSecs() * 1000;
    for (DataWatcher w : watcherMap.values()) {
      if (w.watchTimestamp < expireTs) {
        watcherMap.remove(w.dataInfoId);
        LOGGER.info("clean watcher {}, watchTs", w.dataInfoId, new Date(w.watchTimestamp));
      }
    }
  }

  void fetch() {
    List<Map<String, Long>> queryList = transferQuery(watcherMap.values(), fetchBatchSize());
    for (Map<String, Long> query : queryList) {
      try {
        Map<String, ProvideData> datas = metaNodeService.fetchData(query);
        updateFetchData(datas);
      } catch (Throwable e) {
        LOGGER.error("failed to fetch data:{}", query.keySet(), e);
      }
    }
  }

  void updateFetchData(Map<String, ProvideData> datas) {
    for (Map.Entry<String, ProvideData> data : datas.entrySet()) {
      final DataWatcher w = watcherMap.get(data.getKey());
      if (w == null) {
        LOGGER.info("DataWatcher not found when update: {}", data.getKey());
        continue;
      }
      ProvideData d = data.getValue();
      if (w.provideData == null) {
        w.provideData = data.getValue();
        LOGGER.info("DataWatcher init provideData: {}, {}", data.getKey(), d);
        continue;
      }
      if (w.provideData.getVersion() >= d.getVersion()) {
        LOGGER.warn(
            "DataWatcher skip provideData: {}, current={}, fetch={}",
            data.getKey(),
            w.provideData.getVersion(),
            d.getVersion());
        continue;
      }
      w.provideData = d;
      LOGGER.info("DataWatcher update provideData: {}, {}", data.getKey(), d);
    }
  }

  static List<Map<String, Long>> transferQuery(Collection<DataWatcher> watcherMap, int batchSize) {
    if (watcherMap.isEmpty()) {
      return Collections.emptyList();
    }
    List<List<DataWatcher>> lists = Lists.partition(Lists.newArrayList(watcherMap), batchSize);
    List<Map<String, Long>> ret = Lists.newArrayListWithCapacity(lists.size());
    for (List<DataWatcher> list : lists) {
      Map<String, Long> m = Maps.newHashMapWithExpectedSize(list.size());
      for (DataWatcher w : list) {
        m.put(w.dataInfoId, w.provideData == null ? -1 : w.provideData.getVersion());
      }
      ret.add(m);
    }
    return ret;
  }

  static final class DataWatcher {
    final String dataInfoId;
    volatile long watchTimestamp = System.currentTimeMillis();
    volatile ProvideData provideData;

    DataWatcher(String dataInfoId) {
      this.dataInfoId = dataInfoId;
    }
  }

  public ProvideData get(String dataInfoId) {
    DataWatcher watcher = watcherMap.get(dataInfoId);
    return watcher != null ? watcher.provideData : null;
  }

  protected abstract int fetchBatchSize();

  protected abstract int fetchIntervalMillis();

  protected abstract int watcherLeaseSecs();

  @VisibleForTesting
  DataWatcher getDataWatcher(String dataInfoId) {
    return watcherMap.get(dataInfoId);
  }
}
