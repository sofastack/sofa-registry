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

import com.alipay.sofa.registry.client.api.Configurator;
import com.alipay.sofa.registry.client.api.Publisher;
import com.alipay.sofa.registry.client.api.Subscriber;
import com.alipay.sofa.registry.client.util.StringUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The type Register cache.
 *
 * @author zhuoyu.sjw
 * @version $Id : RegisterCache.java, v 0.1 2017-11-30 18:18 zhuoyu.sjw Exp $$
 */
public class RegisterCache {

  /** publisher register cache Map<registId,Publisher> */
  private Map<String, Publisher> publisherMap = new ConcurrentHashMap<String, Publisher>();
  /** subscriber register cache Map<registId,Register> */
  private Map<String, Subscriber> subscriberMap = new ConcurrentHashMap<String, Subscriber>();
  /** configurator register cache Map<registId,Register> */
  private Map<String, Configurator> configuratorMap = new ConcurrentHashMap<String, Configurator>();

  /**
   * Add register.
   *
   * @param publisher the publisher
   */
  public void addRegister(Publisher publisher) {
    if (null == publisher || StringUtils.isEmpty(publisher.getDataId())) {
      return;
    }

    publisherMap.put(publisher.getRegistId(), publisher);
  }

  /**
   * Add register.
   *
   * @param subscriber the subscriber
   */
  public void addRegister(Subscriber subscriber) {
    if (null == subscriber || StringUtils.isEmpty(subscriber.getDataId())) {
      return;
    }

    subscriberMap.put(subscriber.getRegistId(), subscriber);
  }

  /**
   * Add register.
   *
   * @param configurator the configurator
   */
  public void addRegister(Configurator configurator) {
    if (null == configurator || StringUtils.isEmpty(configurator.getDataId())) {
      return;
    }

    configuratorMap.put(configurator.getRegistId(), configurator);
  }

  /**
   * Remove.
   *
   * @param registId the regist id
   */
  public void remove(String registId) {
    if (publisherMap.remove(registId) == null) {
      if (subscriberMap.remove(registId) == null) {
        configuratorMap.remove(registId);
      }
    }
  }

  /**
   * Gets publisher by regist id.
   *
   * @param registId the regist id
   * @return the publisher by regist id
   */
  public Publisher getPublisherByRegistId(String registId) {
    return publisherMap.get(registId);
  }

  /**
   * Gets subscriber by regist id.
   *
   * @param registId the regist id
   * @return the subscriber by regist id
   */
  public Subscriber getSubscriberByRegistId(String registId) {
    return subscriberMap.get(registId);
  }

  /**
   * Gets configurator by data id.
   *
   * @param registId the regist id
   * @return the configurator by data id
   */
  public Configurator getConfiguratorByRegistId(String registId) {
    return configuratorMap.get(registId);
  }

  /**
   * Gets all publishers.
   *
   * @return the all publishers
   */
  public Collection<Publisher> getAllPublishers() {
    return new ArrayList<Publisher>(publisherMap.values());
  }

  /**
   * Gets all subscribers.
   *
   * @return the all subscribers
   */
  public Collection<Subscriber> getAllSubscribers() {
    return new ArrayList<Subscriber>(subscriberMap.values());
  }

  /**
   * Gets all configurator.
   *
   * @return the all configurator
   */
  public Collection<Configurator> getAllConfigurator() {
    return new ArrayList<Configurator>(configuratorMap.values());
  }
}
