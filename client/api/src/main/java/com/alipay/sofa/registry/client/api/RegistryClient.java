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

import com.alipay.sofa.registry.client.api.model.RegistryType;
import com.alipay.sofa.registry.client.api.registration.ConfiguratorRegistration;
import com.alipay.sofa.registry.client.api.registration.PublisherRegistration;
import com.alipay.sofa.registry.client.api.registration.SubscriberRegistration;

/**
 * The interface Registry client.
 *
 * @author zhuoyu.sjw
 * @version $Id : RegistryClient.java, v 0.1 2017-11-20 18:16 zhuoyu.sjw Exp $$
 */
public interface RegistryClient {

  /**
   * Register publisher.
   *
   * @param registration the registration
   * @param data the data
   * @return the publisher
   */
  Publisher register(PublisherRegistration registration, String... data);

  /**
   * Register multi subscriber multi.
   *
   * @param registration the registration
   * @return the subscriber multi
   */
  Subscriber register(SubscriberRegistration registration);

  /**
   * Register configurator.
   *
   * @param registration the registration
   * @return the configurator
   */
  Configurator register(ConfiguratorRegistration registration);

  /**
   * Unregister all publishers or subscribers belong to dataId.
   *
   * @param dataId the data id
   * @param group registration group, use default group if null
   * @param registryType the registry type, publisher or subscriber
   * @return unregister total registers
   */
  int unregister(String dataId, String group, RegistryType registryType);
}
