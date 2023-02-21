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

import com.alipay.sofa.registry.common.model.store.BaseInfo;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.store.DataStore;
import com.alipay.sofa.registry.server.session.store.Interests;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.NamedThreadFactory;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version $Id: SessionCacheDigestTask.java, v 0.1 2020年08月03日 14:37 xiaojian.xj Exp $
 */
public class SessionCacheDigestTask {

  private static final Logger LOGGER = LoggerFactory.getLogger("CACHE-DIGEST");

  @Autowired DataStore sessionDataStore;

  @Autowired Interests sessionInterests;

  @Autowired SessionServerConfig sessionServerConfig;

  private final ScheduledExecutorService executorService =
      new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("CacheDigestTask"));

  @PostConstruct
  public boolean init() {
    final int intervalMinutes = sessionServerConfig.getCacheDigestIntervalMinutes();
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
      Collection<String> storeDataInfoIds = sessionDataStore.getDataInfoIds();
      Collection<String> interestDataInfoIds = sessionInterests.getDataInfoIds();
      Set<String> dataInfoIds = new HashSet<>(storeDataInfoIds.size() + interestDataInfoIds.size());

      dataInfoIds.addAll(storeDataInfoIds);
      dataInfoIds.addAll(interestDataInfoIds);

      for (String dataInfoId : dataInfoIds) {
        Collection<Publisher> publishers = sessionDataStore.getDatas(dataInfoId);
        Collection<Subscriber> subscribers = sessionInterests.getDatas(dataInfoId);

        LOGGER.info(
            "[dataInfo] {}; {}; {}; {}; [{}]; [{}]",
            sessionServerConfig.getSessionServerDataCenter(),
            dataInfoId,
            publishers.size(),
            subscribers.size(),
            logPubOrSub(publishers),
            logPubOrSub(subscribers));
        // avoid io is too busy
        ConcurrentUtils.sleepUninterruptibly(1, TimeUnit.MILLISECONDS);
      }
      return true;
    } catch (Throwable t) {
      LOGGER.safeError("[CacheDigestTask] cache digest error", (Throwable) t);
      return false;
    }
  }

  private String logPubOrSub(Collection<? extends BaseInfo> infos) {

    return Optional.ofNullable(infos).orElse(new ArrayList<>()).stream()
        .filter(info -> info != null)
        .map(info -> logUrl(info.getSourceAddress()))
        .collect(Collectors.joining(","));
  }

  private String logUrl(URL url) {
    return url == null ? "null" : url.buildAddressString();
  }
}
