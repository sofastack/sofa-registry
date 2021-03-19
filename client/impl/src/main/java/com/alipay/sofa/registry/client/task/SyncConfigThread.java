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
package com.alipay.sofa.registry.client.task;

import com.alipay.sofa.registry.client.api.RegistryClientConfig;
import com.alipay.sofa.registry.client.api.Subscriber;
import com.alipay.sofa.registry.client.log.LoggerFactory;
import com.alipay.sofa.registry.client.provider.DefaultSubscriber;
import com.alipay.sofa.registry.client.provider.RegisterCache;
import com.alipay.sofa.registry.client.remoting.Client;
import com.alipay.sofa.registry.core.model.SyncConfigRequest;
import com.alipay.sofa.registry.core.model.SyncConfigResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;

/**
 * @author zhuoyu.sjw
 * @version $Id: SyncConfigThread.java, v 0.1 2018-03-14 23:36 zhuoyu.sjw Exp $$
 */
public class SyncConfigThread extends Thread {
  private static final Logger LOGGER = LoggerFactory.getLogger(SyncConfigThread.class);

  private Client client;

  private RegisterCache registerCache;

  private RegistryClientConfig config;

  private ObserverHandler observerHandler;

  public SyncConfigThread(
      Client client,
      RegisterCache registerCache,
      RegistryClientConfig config,
      ObserverHandler observerHandler) {
    super("SyncConfigThread");
    this.setDaemon(true);
    this.client = client;
    this.registerCache = registerCache;
    this.config = config;
    this.observerHandler = observerHandler;
  }

  @Override
  public void run() {
    int retryInterval = config.getSyncConfigRetryInterval();
    //noinspection InfiniteLoopStatement
    while (true) {
      try {
        Thread.sleep(retryInterval);

        if (!client.isConnected()) {
          continue;
        }

        SyncConfigRequest request = new SyncConfigRequest();
        request.setDataCenter(config.getDataCenter());
        request.setZone(config.getZone());
        Object result = client.invokeSync(request);

        if (!(result instanceof SyncConfigResponse)) {
          LOGGER.warn("[syncConfig] unknown response type, {}", result);
          continue;
        }

        SyncConfigResponse response = (SyncConfigResponse) result;
        if (!response.isSuccess()) {
          LOGGER.warn("[syncConfig] request failed, {}", response);
          continue;
        }

        int interval = response.getRetryInterval();
        retryInterval = Math.max(retryInterval, interval);

        List<String> availableSegments = response.getAvailableSegments();

        Collection<Subscriber> allSubscribers = registerCache.getAllSubscribers();
        for (Subscriber subscriber : allSubscribers) {
          try {
            if (!(subscriber instanceof DefaultSubscriber)) {
              continue;
            }

            DefaultSubscriber defaultSubscriber = (DefaultSubscriber) subscriber;

            if (!defaultSubscriber.isInited()) {
              LOGGER.info(
                  "[syncConfig] DefaultSubscriber not init, {}", defaultSubscriber.getRegistId());
              continue;
            }
            List<String> nowAvailableSegments = defaultSubscriber.getAvailableSegments();

            if (isEqualCollections(availableSegments, nowAvailableSegments)) {
              continue;
            }

            defaultSubscriber.setAvailableSegments(availableSegments);

            observerHandler.notify(defaultSubscriber);
          } catch (Exception e) {
            LOGGER.error(
                "[syncConfig] try notify subscriber error, registId: {}, availableSegments: {}",
                subscriber.getRegistId(),
                availableSegments,
                e);
          }
        }
      } catch (Throwable e) {
        LOGGER.error("[syncConfig] sync config error, retryInterval: {}", retryInterval, e);
      }
    }
  }

  private boolean isEqualCollections(Collection<String> a, Collection<String> b) {
    if (null == a) {
      a = new ArrayList<String>();
    }

    if (null == b) {
      b = new ArrayList<String>();
    }

    return a.size() == b.size() && a.equals(b);
  }
}
