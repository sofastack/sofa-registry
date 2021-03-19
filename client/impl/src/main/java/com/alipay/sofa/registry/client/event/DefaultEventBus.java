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
package com.alipay.sofa.registry.client.event;

import com.alipay.sofa.registry.client.api.EventBus;
import com.alipay.sofa.registry.client.api.EventSubscriber;
import com.alipay.sofa.registry.client.api.RegistryClientConfig;
import com.alipay.sofa.registry.client.api.model.Event;
import com.alipay.sofa.registry.client.factory.NamedThreadFactory;
import com.alipay.sofa.registry.client.log.LoggerFactory;
import com.alipay.sofa.registry.client.util.CommonUtils;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;

/**
 * The type Default event bus.
 *
 * @author zhanggeng.zg
 * @author zhuoyu.sjw
 * @version $Id : DefaultEventBus.java, v 0.1 2018-07-13 10:56 zhuoyu.sjw Exp $$
 */
public class DefaultEventBus implements EventBus {

  /** LOGGER */
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEventBus.class);

  private RegistryClientConfig config;

  private ConcurrentMap<Class<? extends Event>, CopyOnWriteArraySet<EventSubscriber>>
      eventSubscriberMap =
          new ConcurrentHashMap<Class<? extends Event>, CopyOnWriteArraySet<EventSubscriber>>();

  private Executor executor;

  /**
   * Instantiates a new Default event bus.
   *
   * @param config the config
   */
  public DefaultEventBus(RegistryClientConfig config) {
    this.config = config;
    this.executor =
        new ThreadPoolExecutor(
            config.getObserverThreadCoreSize(),
            config.getObserverThreadMaxSize(),
            0,
            TimeUnit.SECONDS,
            new LinkedBlockingDeque<Runnable>(config.getObserverThreadQueueLength()),
            new NamedThreadFactory("DefaultEventBusThread"));
  }

  /** @see EventBus#register(Class, EventSubscriber) */
  @Override
  public void register(Class<? extends Event> eventClass, EventSubscriber eventSubscriber) {
    CopyOnWriteArraySet<EventSubscriber> set = eventSubscriberMap.get(eventClass);
    if (set == null) {
      set = new CopyOnWriteArraySet<EventSubscriber>();
      CopyOnWriteArraySet<EventSubscriber> old = eventSubscriberMap.putIfAbsent(eventClass, set);
      if (old != null) {
        set = old;
      }
    }
    set.add(eventSubscriber);
    LOGGER.debug("Register subscriber: {} of event: {}.", eventSubscriber, eventClass);
  }

  /** @see EventBus#unRegister(Class, EventSubscriber) */
  @Override
  public void unRegister(Class<? extends Event> eventClass, EventSubscriber eventSubscriber) {
    CopyOnWriteArraySet<EventSubscriber> set = eventSubscriberMap.get(eventClass);
    if (set != null) {
      set.remove(eventSubscriber);
      LOGGER.debug("UnRegister subscriber: {} of event: {}.", eventSubscriber, eventClass);
    }
  }

  /**
   * Post.
   *
   * @param event the event
   */
  @Override
  public void post(final Event event) {
    if (!isEnable()) {
      return;
    }
    CopyOnWriteArraySet<EventSubscriber> subscribers = eventSubscriberMap.get(event.getClass());
    if (null != subscribers && !subscribers.isEmpty()) {
      for (final EventSubscriber subscriber : subscribers) {
        if (subscriber.isSync()) {
          handleEvent(subscriber, event);
        } else { // 异步
          executor.execute(
              new Runnable() {
                @Override
                public void run() {
                  handleEvent(subscriber, event);
                }
              });
        }
      }
    }
  }

  /**
   * Is enable boolean.
   *
   * @return the boolean
   */
  @Override
  public boolean isEnable() {
    return null != config && config.isEventBusEnable();
  }

  /**
   * Is enable boolean.
   *
   * @param eventClass the event class
   * @return the boolean
   */
  @Override
  public boolean isEnable(Class<? extends Event> eventClass) {
    return isEnable() && CommonUtils.isNotEmpty(eventSubscriberMap.get(eventClass));
  }

  private void handleEvent(final EventSubscriber subscriber, final Event event) {
    try {
      subscriber.onEvent(event);
    } catch (Throwable e) {
      LOGGER.warn("Handle {} error", event.getClass(), e);
    }
  }
}
