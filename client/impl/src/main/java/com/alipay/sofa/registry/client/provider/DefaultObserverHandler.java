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
package com.alipay.sofa.registry.client.provider;

import com.alipay.sofa.registry.client.api.ConfigDataObserver;
import com.alipay.sofa.registry.client.api.Configurator;
import com.alipay.sofa.registry.client.api.EventBus;
import com.alipay.sofa.registry.client.api.RegistryClientConfig;
import com.alipay.sofa.registry.client.api.Subscriber;
import com.alipay.sofa.registry.client.api.SubscriberDataObserver;
import com.alipay.sofa.registry.client.event.ConfiguratorProcessEvent;
import com.alipay.sofa.registry.client.event.SubscriberProcessEvent;
import com.alipay.sofa.registry.client.factory.NamedThreadFactory;
import com.alipay.sofa.registry.client.log.LoggerFactory;
import com.alipay.sofa.registry.client.task.ObserverHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;

/**
 * The type Default observer handler.
 *
 * @author zhuoyu.sjw
 * @version $Id : DefaultObserverHandler.java, v 0.1 2018-03-15 12:02 zhuoyu.sjw Exp $$
 */
public class DefaultObserverHandler implements ObserverHandler {

  /** LOGGER */
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultObserverHandler.class);

  private static final int KEEP_ALIVE_TIME = 60;

  private ExecutorService executor;

  private EventBus eventBus;

  private RegistryClientConfig config;

  /**
   * Constructor.
   *
   * @param config the config
   */
  public DefaultObserverHandler(RegistryClientConfig config, EventBus eventBus) {
    this.config = config;
    this.executor =
        new ThreadPoolExecutor(
            config.getObserverThreadCoreSize(),
            config.getObserverThreadMaxSize(),
            KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            new LinkedBlockingDeque<Runnable>(config.getObserverThreadQueueLength()),
            new NamedThreadFactory("ObserverNotifyThread"));
    this.eventBus = eventBus;
  }

  /** @see ObserverHandler#notify(Subscriber) */
  @Override
  public void notify(Subscriber subscriber) {
    executor.submit(new SubscriberNotifyTask(subscriber));
  }

  /** @see ObserverHandler#notify(Configurator) */
  @Override
  public void notify(Configurator configurator) {
    executor.submit(new ConfiguratorNotifyTask(configurator));
  }

  /** The type Observer notify task. */
  public class SubscriberNotifyTask implements Runnable {

    private Subscriber subscriber;

    /**
     * Constructor.
     *
     * @param subscriber the subscriber
     */
    public SubscriberNotifyTask(Subscriber subscriber) {
      this.subscriber = subscriber;
    }

    /** @see Runnable#run() */
    @Override
    public void run() {
      // ignore empty task
      if (null == subscriber) {
        return;
      }

      SubscriberProcessEvent event = new SubscriberProcessEvent();
      long start = System.currentTimeMillis();
      event.setStart(start);
      event.setConfig(config);
      event.setSubscriber(subscriber);
      try {
        SubscriberDataObserver dataObserver = subscriber.getDataObserver();
        if (null != dataObserver) {
          dataObserver.handleData(subscriber.getDataId(), subscriber.peekData());
        }
        LOGGER.info(
            "[notify] notify subscriber success, dataId: {}, registId:{}, cost: {}ms",
            subscriber.getDataId(),
            subscriber.getRegistId(),
            (System.currentTimeMillis() - start));
      } catch (Exception e) {
        LOGGER.error(
            "[notify] SubscriberNotifyTask execute error, dataId: {}", subscriber.getDataId(), e);
        event.setThrowable(e);
      } finally {
        event.setEnd(System.currentTimeMillis());
        if (null != eventBus) {
          eventBus.post(event);
        }
      }
    }
  }

  /** The type Configurator notify task. */
  public class ConfiguratorNotifyTask implements Runnable {

    private Configurator configurator;

    /**
     * Instantiates a new Configurator notify task.
     *
     * @param configurator the configurator
     */
    public ConfiguratorNotifyTask(Configurator configurator) {
      this.configurator = configurator;
    }

    /** @see Runnable#run() */
    @Override
    public void run() {
      if (null == configurator) {
        return;
      }

      ConfiguratorProcessEvent event = new ConfiguratorProcessEvent();
      long start = System.currentTimeMillis();
      event.setStart(start);
      event.setConfig(config);
      event.setConfigurator(configurator);
      try {
        ConfigDataObserver dataObserver = configurator.getDataObserver();
        if (null != dataObserver) {
          dataObserver.handleData(configurator.getDataId(), configurator.peekData());
        }

        LOGGER.info(
            "[notify] notify configurator success, dataId: {}, registId:{}, cost: {}ms",
            configurator.getDataId(),
            configurator.getRegistId(),
            (System.currentTimeMillis() - start));
      } catch (Exception e) {
        LOGGER.error(
            "[notify] ConfiguratorNotifyTask execute error, dataId: {}",
            configurator.getDataId(),
            e);
        event.setThrowable(e);
      } finally {
        event.setEnd(System.currentTimeMillis());
        if (null != eventBus) {
          eventBus.post(event);
        }
      }
    }
  }
}
