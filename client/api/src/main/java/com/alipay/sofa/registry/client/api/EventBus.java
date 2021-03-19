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
package com.alipay.sofa.registry.client.api;

import com.alipay.sofa.registry.client.api.model.Event;

/**
 * The interface Event bus.
 *
 * @author zhuoyu.sjw
 * @version $Id : EventBus.java, v 0.1 2018-07-12 21:11 zhuoyu.sjw Exp $$
 */
public interface EventBus {

  /**
   * Is enable boolean.
   *
   * @return the boolean
   */
  boolean isEnable();

  /**
   * Is enable boolean.
   *
   * @param eventClass the event class
   * @return the boolean
   */
  boolean isEnable(Class<? extends Event> eventClass);

  /**
   * Register.
   *
   * @param eventClass the event class
   * @param eventSubscriber the event subscriber
   */
  void register(Class<? extends Event> eventClass, EventSubscriber eventSubscriber);

  /**
   * Un register.
   *
   * @param eventClass the event class
   * @param eventSubscriber the event subscriber
   */
  void unRegister(Class<? extends Event> eventClass, EventSubscriber eventSubscriber);

  /**
   * Post event.
   *
   * @param event the event
   */
  void post(final Event event);
}
