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
package com.alipay.sofa.registry.server.data.timer;

import com.alipay.sofa.registry.common.model.DataUtils;
import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumStorageDelegate;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.LoopRunnable;
import com.alipay.sofa.registry.util.NamedThreadFactory;
import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author qian.lqlq
 * @version $Id: CacheDigestTask.java, v 0.1 2018－04－27 17:40 qian.lqlq Exp $
 */
public class CacheCountTask {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(CacheCountTask.class, "[CacheCountTask]");
  private static final Logger COUNT_LOGGER = LoggerFactory.getLogger("CACHE-COUNT");

  @Autowired private DatumStorageDelegate datumStorageDelegate;

  @Autowired private DataServerConfig dataServerConfig;

  @PostConstruct
  public boolean init() {
    final int intervalSec = dataServerConfig.getCacheCountIntervalSecs();
    if (intervalSec <= 0) {
      LOGGER.info("cache count off with intervalSecs={}", intervalSec);
      return false;
    }
    ScheduledExecutorService executor =
        new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("CacheCountTask"));
    executor.scheduleAtFixedRate(this::count, intervalSec, intervalSec, TimeUnit.SECONDS);
    ConcurrentUtils.createDaemonThread("cache-printer", new PrintTotal()).start();
    return true;
  }

  final class PrintTotal extends LoopRunnable {

    @Override
    public void runUnthrowable() {
      printTotal();
    }

    @Override
    public void waitingUnthrowable() {
      // support minute-level statistics
      ConcurrentUtils.sleepUninterruptibly(60, TimeUnit.SECONDS);
    }
  }

  boolean printTotal() {
    Map<String, Map<String, List<Publisher>>> allMap = datumStorageDelegate.getLocalAllPublisher();
    Map<String, List<Publisher>> pubs = allMap.get(dataServerConfig.getLocalDataCenter());
    if (pubs.isEmpty()) {
      COUNT_LOGGER.info(
          "[Total]{},pubs={},dataIds={}", dataServerConfig.getLocalDataCenter(), 0, 0);
      return false;
    }
    int pubCount = pubs.values().stream().mapToInt(p -> p.size()).sum();
    COUNT_LOGGER.info(
        "[Total]{},pubs={},dataIds={}",
        dataServerConfig.getLocalDataCenter(),
        pubCount,
        pubs.size());
    return true;
  }

  boolean count() {
    try {
      Map<String, Map<String, List<Publisher>>> allMap =
          datumStorageDelegate.getLocalAllPublisher();
      if (!allMap.isEmpty()) {
        for (Entry<String, Map<String, List<Publisher>>> dataCenterEntry : allMap.entrySet()) {
          final String dataCenter = dataCenterEntry.getKey();
          List<Publisher> all = new ArrayList<>(512);
          for (List<Publisher> publishers : dataCenterEntry.getValue().values()) {
            all.addAll(publishers);
          }
          Map<String, Map<String, Tuple<Integer, Integer>>> groupCounts =
              DataUtils.countGroupByInstanceIdGroup(all);
          printGroupCount(dataCenter, groupCounts);

          Map<String, Map<String, Map<String, Tuple<Integer, Integer>>>> counts =
              DataUtils.countGroupByInstanceIdGroupApp(all);
          printGroupAppCount(dataCenter, counts);
        }
      } else {
        LOGGER.info("datum cache is empty");
      }
      return true;
    } catch (Throwable t) {
      LOGGER.error("cache count error", t);
    }
    return false;
  }

  private static void printGroupAppCount(
      String dataCenter, Map<String, Map<String, Map<String, Tuple<Integer, Integer>>>> counts) {
    for (Entry<String, Map<String, Map<String, Tuple<Integer, Integer>>>> count :
        counts.entrySet()) {
      final String instanceId = count.getKey();
      for (Entry<String, Map<String, Tuple<Integer, Integer>>> groupCounts :
          count.getValue().entrySet()) {
        final String group = groupCounts.getKey();
        for (Entry<String, Tuple<Integer, Integer>> apps : groupCounts.getValue().entrySet()) {
          final String app = apps.getKey();
          Tuple<Integer, Integer> tupleCount = apps.getValue();
          COUNT_LOGGER.info(
              "[Pub]{},{},{},{},{},{}",
              dataCenter,
              instanceId,
              group,
              app,
              tupleCount.o1,
              tupleCount.o2);
        }
        ConcurrentUtils.sleepUninterruptibly(2, TimeUnit.MILLISECONDS);
      }
    }
  }

  private static void printGroupCount(
      String dataCenter, Map<String, Map<String, Tuple<Integer, Integer>>> counts) {
    Metrics.PUB_GAUGE.clear();
    Metrics.PUB_DATA_ID_GAUGE.clear();
    for (Map.Entry<String, Map<String, Tuple<Integer, Integer>>> count : counts.entrySet()) {
      final String instanceId = count.getKey();
      Map<String, Tuple<Integer, Integer>> groupCounts = count.getValue();
      for (Entry<String, Tuple<Integer, Integer>> groupCount : groupCounts.entrySet()) {
        final String group = groupCount.getKey();
        Tuple<Integer, Integer> tupleCount = groupCount.getValue();
        Metrics.PUB_GAUGE.labels(dataCenter, instanceId, group).set(tupleCount.o1);
        Metrics.PUB_DATA_ID_GAUGE.labels(dataCenter, instanceId, group).set(tupleCount.o2);
        COUNT_LOGGER.info(
            "[PubGroup]{},{},{},{},{}",
            dataCenter,
            instanceId,
            group,
            tupleCount.o1,
            tupleCount.o2);
      }
    }
  }

  @VisibleForTesting
  void setDatumCache(DatumStorageDelegate datumStorageDelegate) {
    this.datumStorageDelegate = datumStorageDelegate;
  }

  @VisibleForTesting
  void setDataServerConfig(DataServerConfig dataServerConfig) {
    this.dataServerConfig = dataServerConfig;
  }
}
