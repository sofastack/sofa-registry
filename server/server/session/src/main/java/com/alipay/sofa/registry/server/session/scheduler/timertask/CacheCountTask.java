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
package com.alipay.sofa.registry.server.session.scheduler.timertask;

import com.alipay.sofa.registry.common.model.DataUtils;
import com.alipay.sofa.registry.common.model.InterestGroup;
import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.common.model.store.Watcher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.store.DataStore;
import com.alipay.sofa.registry.server.session.store.Interests;
import com.alipay.sofa.registry.server.session.store.Watchers;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.NamedThreadFactory;
import com.google.common.collect.Lists;
import io.prometheus.client.Gauge;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

/**
 * @author qian.lqlq
 * @version $Id: CacheDigestTask.java, v 0.1 2018－04－27 17:40 qian.lqlq Exp $
 */
public class CacheCountTask {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(CacheCountTask.class, "[CacheCountTask]");
  private static final Logger COUNT_LOGGER = LoggerFactory.getLogger("CACHE-COUNT");

  @Autowired DataStore sessionDataStore;

  @Autowired Interests sessionInterests;

  @Autowired Watchers sessionWatchers;

  @Autowired SessionServerConfig sessionServerConfig;

  @PostConstruct
  public boolean init() {
    final int intervalSec = sessionServerConfig.getCacheCountIntervalSecs();
    if (intervalSec <= 0) {
      LOGGER.info("cache count off with intervalSecs={}", intervalSec);
      return false;
    }
    ScheduledExecutorService executor =
        new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("CacheCountTask"));
    executor.scheduleAtFixedRate(this::syncCount, intervalSec, intervalSec, TimeUnit.SECONDS);
    return true;
  }

  boolean syncCount() {
    try {
      List<Publisher> pubs = sessionDataStore.getDataList();
      List<Subscriber> subs = sessionInterests.getDataList();
      List<Watcher> wats = sessionWatchers.getDataList();

      Map<String, Map<String, Tuple<Integer, Integer>>> pubGroupCounts =
          DataUtils.countGroupByInstanceIdGroup(pubs);
      printInstanceIdGroupCount(
          "[PubGroup]", pubGroupCounts, Metrics.PUB_GAUGE, Metrics.PUB_DATA_ID_GAUGE);

      Tuple<List<Subscriber>, List<Subscriber>> multiSubs = splitMultiSub(subs);

      Map<String, Map<String, Tuple<Integer, Integer>>> notMultiSubGroupCounts =
          DataUtils.countGroupByInstanceIdGroup(multiSubs.o1);
      Map<String, Map<String, Tuple<Integer, Integer>>> multiSubGroupCounts =
          DataUtils.countGroupByInstanceIdGroup(multiSubs.o2);
      printInstanceIdGroupCount(
          "[NotMultiSubGroup]",
          notMultiSubGroupCounts,
          Metrics.NOT_MULTI_SUB_GAUGE,
          Metrics.NOT_MULTI_SUB_DATA_ID_GAUGE);
      printInstanceIdGroupCount(
          "[MultiSubGroup]",
          multiSubGroupCounts,
          Metrics.MULTI_SUB_GAUGE,
          Metrics.MULTI_SUB_DATA_ID_GAUGE);

      Map<String, Map<String, Tuple<Integer, Integer>>> watGroupCounts =
          DataUtils.countGroupByInstanceIdGroup(wats);
      printInstanceIdGroupCount(
          "[WatGroup]", watGroupCounts, Metrics.WAT_COUNTER, Metrics.WAT_DATA_ID_COUNTER);

      Map<String, Map<String, Map<String, Tuple<Integer, Integer>>>> pubCounts =
          DataUtils.countGroupByInstanceIdGroupApp(pubs);
      printInstanceIdGroupAppCount("[Pub]", pubCounts);

      Map<String, Map<String, Map<String, Tuple<Integer, Integer>>>> subCounts =
          DataUtils.countGroupByInstanceIdGroupApp(subs);
      printInstanceIdGroupAppCount("[Sub]", subCounts);

      Map<String, Map<String, Map<String, Tuple<Integer, Integer>>>> watCounts =
          DataUtils.countGroupByInstanceIdGroupApp(wats);
      printInstanceIdGroupAppCount("[Wat]", watCounts);
      return true;
    } catch (Throwable e) {
      LOGGER.safeError("cache count error", e);
      return false;
    }
  }

  static Tuple<List<Subscriber>, List<Subscriber>> splitMultiSub(List<Subscriber> subs) {
    if (CollectionUtils.isEmpty(subs)) {
      return new Tuple<>(Collections.emptyList(), Collections.emptyList());
    }

    int initSize = subs.size() / 2;
    List<Subscriber> notMulti = Lists.newArrayListWithExpectedSize(initSize);
    List<Subscriber> multi = Lists.newArrayListWithExpectedSize(initSize);

    for (Subscriber sub : subs) {
      if (sub.acceptMulti()) {
        multi.add(sub);
      } else {
        notMulti.add(sub);
      }
    }
    return new Tuple<>(notMulti, multi);
  }

  private static void printInstanceIdGroupAppCount(
      String prefix, Map<String, Map<String, Map<String, Tuple<Integer, Integer>>>> counts) {
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
              "{}{},{},{},{},{}", prefix, instanceId, group, app, tupleCount.o1, tupleCount.o2);
        }
        ConcurrentUtils.sleepUninterruptibly(2, TimeUnit.MILLISECONDS);
      }
    }
  }

  private static void printInstanceIdGroupCount(
      String prefix,
      Map<String, Map<String, Tuple<Integer, Integer>>> counts,
      Gauge gauge,
      Gauge dataIDGauge) {
    gauge.clear();
    dataIDGauge.clear();
    for (Entry<String, Map<String, Tuple<Integer, Integer>>> count : counts.entrySet()) {
      final String instanceId = count.getKey();
      for (Entry<String, Tuple<Integer, Integer>> groups : count.getValue().entrySet()) {
        final String group = groups.getKey();
        Tuple<Integer, Integer> tupleCount = groups.getValue();
        gauge.labels(instanceId, InterestGroup.normalizeGroup(group)).set(tupleCount.o1);
        dataIDGauge.labels(instanceId, InterestGroup.normalizeGroup(group)).set(tupleCount.o2);
        COUNT_LOGGER.info("{}{},{},{},{}", prefix, instanceId, group, tupleCount.o1, tupleCount.o2);
      }
    }
  }
}
