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

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.session.bootstrap.ExecutorManager;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.store.PublisherStore;
import com.alipay.sofa.registry.server.session.store.SubscriberStore;
import com.alipay.sofa.registry.server.session.store.WatcherStore;
import com.alipay.sofa.registry.server.session.store.engine.StoreEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * The type Sync clients heartbeat task.
 *
 * @author zhuoyu.sjw
 * @version $Id : SyncClientsHeartbeatTask.java, v 0.1 2018-03-31 16:07 zhuoyu.sjw Exp $$
 */
public class SyncClientsHeartbeatTask {
  private static final Logger CONSOLE_COUNT_LOGGER =
      LoggerFactory.getLogger("SESSION-CONSOLE", "[Count]");

  @Autowired Exchange boltExchange;

  @Autowired SessionServerConfig sessionServerConfig;

  @Autowired PublisherStore publisherStore;
  @Autowired SubscriberStore subscriberStore;
  @Autowired WatcherStore watcherStore;

  @Autowired ExecutorManager executorManager;

  @Scheduled(initialDelay = 60000, fixedRate = 60000)
  public void syncCount() {
    StoreEngine.StoreStat countSub = subscriberStore.stat();
    StoreEngine.StoreStat countPub = publisherStore.stat();
    StoreEngine.StoreStat countSubW = watcherStore.stat();

    int channelCount = 0;
    Server sessionServer = boltExchange.getServer(sessionServerConfig.getServerPort());
    if (sessionServer != null) {
      channelCount = sessionServer.getChannelCount();
    }

    Metrics.PUB_SUM.set(countPub.size());
    Metrics.SUB_SUM.set(countSub.size());
    Metrics.WAT_SUM.set(countSubW.size());
    Metrics.CHANNEL_SUM.set(channelCount);

    CONSOLE_COUNT_LOGGER.info(
        "Subscriber count: {}, Publisher count: {}, Watcher count: {}, Connection count: {}, SubDataId={}, PubDataId={}, WatDataId={}",
        countSub.size(),
        countPub.size(),
        countSubW.size(),
        channelCount,
        countSub.nonEmptyDataIdSize(),
        countPub.nonEmptyDataIdSize(),
        countSubW.nonEmptyDataIdSize());
  }
}
