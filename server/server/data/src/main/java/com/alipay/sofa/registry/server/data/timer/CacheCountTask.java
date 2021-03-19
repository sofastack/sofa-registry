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
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumCache;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.NamedThreadFactory;
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

  @Autowired private DatumCache datumCache;

  @Autowired private DataServerConfig dataServerConfig;

  @PostConstruct
  public void init() {
    final int intervalSec = dataServerConfig.getCacheCountIntervalSecs();
    if (intervalSec <= 0) {
      LOGGER.info("cache count off with intervalSecs={}", intervalSec);
      return;
    }
    ScheduledExecutorService executor =
        new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("CacheCountTask"));
    executor.scheduleWithFixedDelay(
        () -> {
          try {
            Map<String, Map<String, List<Publisher>>> allMap = datumCache.getAllPublisher();
            if (!allMap.isEmpty()) {
              for (Entry<String, Map<String, List<Publisher>>> dataCenterEntry :
                  allMap.entrySet()) {
                final String dataCenter = dataCenterEntry.getKey();
                List<Publisher> all = new ArrayList<>(512);
                for (List<Publisher> publishers : dataCenterEntry.getValue().values()) {
                  all.addAll(publishers);
                }
                Map<String, Map<String, Integer>> groupCounts =
                    DataUtils.countGroupByInstanceIdGroup(all);
                printGroupCount(dataCenter, groupCounts);

                Map<String, Map<String, Map<String, Integer>>> counts =
                    DataUtils.countGroupByInstanceIdGroupApp(all);
                printCount(dataCenter, counts);
              }
            } else {
              LOGGER.info("datum cache is empty");
            }

          } catch (Throwable t) {
            LOGGER.error("cache count error", t);
          }
        },
        intervalSec,
        intervalSec,
        TimeUnit.SECONDS);
  }

  private static void printCount(
      String dataCenter, Map<String, Map<String, Map<String, Integer>>> counts) {
    for (Entry<String, Map<String, Map<String, Integer>>> count : counts.entrySet()) {
      final String instanceId = count.getKey();
      for (Entry<String, Map<String, Integer>> groupCounts : count.getValue().entrySet()) {
        final String group = groupCounts.getKey();
        for (Entry<String, Integer> apps : groupCounts.getValue().entrySet()) {
          final String app = apps.getKey();
          COUNT_LOGGER.info(
              "[Pub]{},{},{},{},{}", dataCenter, instanceId, group, app, apps.getValue());
        }
        ConcurrentUtils.sleepUninterruptibly(100, TimeUnit.MILLISECONDS);
      }
    }
  }

  private static void printGroupCount(String dataCenter, Map<String, Map<String, Integer>> counts) {
    for (Map.Entry<String, Map<String, Integer>> count : counts.entrySet()) {
      final String instanceId = count.getKey();
      Map<String, Integer> groupCounts = count.getValue();
      for (Entry<String, Integer> groupCount : groupCounts.entrySet()) {
        final String group = groupCount.getKey();
        COUNT_LOGGER.info(
            "[PubGroup]{},{},{},{}", dataCenter, instanceId, group, groupCount.getValue());
      }
    }
  }
}
