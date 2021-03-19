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

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.client.api.Configurator;
import com.alipay.sofa.registry.client.api.Subscriber;
import com.alipay.sofa.registry.client.provider.DefaultRegistryClientConfigBuilder;
import org.junit.Test;

/**
 * @author zhuoyu.sjw
 * @version $Id: LookoutSubscriberTest.java, v 0.1 2018-07-15 23:04 zhuoyu.sjw Exp $$
 */
public class LookoutSubscriberTest {

  private LookoutSubscriber lookoutSubscriber;

  @Test
  public void isSync() {
    lookoutSubscriber = new LookoutSubscriber();
    assertFalse(lookoutSubscriber.isSync());
  }

  @Test
  public void onSubscriberProcessEvent() {
    lookoutSubscriber = new LookoutSubscriber();

    SubscriberProcessEvent event = new SubscriberProcessEvent();
    Subscriber subscriber = mock(Subscriber.class);
    event.setSubscriber(subscriber);
    event.setConfig(DefaultRegistryClientConfigBuilder.start().setInstanceId("000001").build());
    event.setStart(System.currentTimeMillis());
    event.setEnd(System.currentTimeMillis() + 1);
    event.setThrowable(null);

    when(subscriber.getDataId()).thenReturn("test-dataId");

    lookoutSubscriber.onEvent(event);

    verify(subscriber, times(1)).getDataId();
  }

  @Test
  public void onConfiguratorProcessEvent() {
    lookoutSubscriber = new LookoutSubscriber();

    ConfiguratorProcessEvent event = new ConfiguratorProcessEvent();
    Configurator configurator = mock(Configurator.class);
    event.setConfigurator(configurator);
    event.setConfig(DefaultRegistryClientConfigBuilder.start().setInstanceId("000001").build());
    event.setStart(System.currentTimeMillis());
    event.setEnd(System.currentTimeMillis() + 1);
    event.setThrowable(null);

    when(configurator.getDataId()).thenReturn("test-dataId");

    lookoutSubscriber.onEvent(event);

    verify(configurator, times(1)).getDataId();
  }
}
