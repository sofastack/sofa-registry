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

import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumStorageDelegate;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.NamedThreadFactory;
import com.alipay.sofa.registry.util.StringFormatter;
import com.google.common.annotations.VisibleForTesting;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

/**
 * @author qian.lqlq
 * @version $Id: CacheDigestTask.java, v 0.1 2018－04－27 17:40 qian.lqlq Exp $
 */
public class CacheDigestTask {
  private static final Logger LOGGER = LoggerFactory.getLogger("CACHE-DIGEST");

  @Autowired private DatumStorageDelegate datumStorageDelegate;

  @Autowired private DataServerConfig dataServerConfig;

  private final ScheduledExecutorService executorService =
      new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("CacheDigestTask"));

  @PostConstruct
  public boolean init() {
    final int intervalMinutes = dataServerConfig.getCacheDigestIntervalMinutes();
    if (intervalMinutes <= 0) {
      LOGGER.info("cache digest off with intervalMinutes={}", intervalMinutes);
      return false;
    }
    Date firstDate = new Date();
    firstDate = DateUtils.round(firstDate, Calendar.MINUTE);
    firstDate.setMinutes(
        firstDate.getMinutes() / intervalMinutes * intervalMinutes + intervalMinutes);
    long firstDelay = firstDate.getTime() - System.currentTimeMillis();
    executorService.scheduleAtFixedRate(
        this::dump, firstDelay, (long) intervalMinutes * 60 * 1000, TimeUnit.MILLISECONDS);
    return true;
  }

  boolean dump() {
    try {
      Map<String, Map<String, Datum>> allMap = datumStorageDelegate.getLocalAll();
      if (!allMap.isEmpty()) {
        for (Entry<String, Map<String, Datum>> dataCenterEntry : allMap.entrySet()) {
          String dataCenter = dataCenterEntry.getKey();
          Map<String, Datum> datumMap = dataCenterEntry.getValue();
          LOGGER.info("size of datum in {} is {}", dataCenter, datumMap.size());
          for (Entry<String, Datum> dataInfoEntry : datumMap.entrySet()) {
            String dataInfoId = dataInfoEntry.getKey();
            Datum data = dataInfoEntry.getValue();
            Map<String, Publisher> pubMap = data.getPubMap();
            StringBuilder pubStr = new StringBuilder(4096);
            if (!CollectionUtils.isEmpty(pubMap)) {
              for (Publisher publisher : pubMap.values()) {
                pubStr.append(logPublisher(publisher)).append(";");
              }
            }
            LOGGER.info(
                "[Datum]{},{},{},[{}]",
                dataInfoId,
                data.getVersion(),
                dataCenter,
                pubStr.toString());
            // avoid io is busy
            ConcurrentUtils.sleepUninterruptibly(2, TimeUnit.MILLISECONDS);
          }
          int pubCount = datumMap.values().stream().mapToInt(Datum::publisherSize).sum();
          LOGGER.info("size of publisher in {} is {}", dataCenter, pubCount);
        }
      } else {
        LOGGER.info("datum cache is empty");
      }
      return true;
    } catch (Throwable t) {
      LOGGER.safeError("cache digest error", (Throwable) t);
    }
    return false;
  }

  private String logPublisher(Publisher publisher) {
    if (publisher != null) {
      URL url = publisher.getSourceAddress();
      String urlStr = url != null ? url.buildAddressString() : "null";
      return StringFormatter.format(
          "{},{},{},{}",
          publisher.getRegisterId(),
          publisher.getRegisterTimestamp(),
          urlStr,
          publisher.getVersion());
    }
    return "";
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
