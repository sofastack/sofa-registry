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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.alipay.sofa.registry.client.api.RegistryClientConfig;
import com.alipay.sofa.registry.client.provider.DefaultRegistryClientConfigBuilder;
import org.junit.Before;
import org.junit.Test;

/**
 * @author zhuoyu.sjw
 * @version $Id: DefaultEventBusTest.java, v 0.1 2018-07-15 22:31 zhuoyu.sjw Exp $$
 */
public class DefaultEventBusTest {

  private DefaultEventBus eventBus;

  @Before
  public void setUp() {
    RegistryClientConfig config =
        DefaultRegistryClientConfigBuilder.start().setEventBusEnable(true).build();
    eventBus = new DefaultEventBus(config);
  }

  @Test
  public void isEnable0() {
    RegistryClientConfig config =
        DefaultRegistryClientConfigBuilder.start().setEventBusEnable(false).build();
    eventBus = new DefaultEventBus(config);
    assertFalse(eventBus.isEnable());
  }

  @Test
  public void isEnable1() {
    RegistryClientConfig config =
        DefaultRegistryClientConfigBuilder.start().setEventBusEnable(true).build();
    eventBus = new DefaultEventBus(config);
    assertTrue(eventBus.isEnable());
  }

  @Test
  public void register() {
    TestEventSubscriber testEventSubscriber = new TestEventSubscriber(true);

    try {
      assertFalse(eventBus.isEnable(TestEvent.class));
      eventBus.register(TestEvent.class, testEventSubscriber);
      assertTrue(eventBus.isEnable(TestEvent.class));
    } finally {
      eventBus.unRegister(TestEvent.class, testEventSubscriber);
    }
    assertFalse(eventBus.isEnable(TestEvent.class));
  }

  @Test
  public void post() {
    TestEventSubscriber testEventSubscriber = new TestEventSubscriber(true);

    try {
      assertFalse(eventBus.isEnable(TestEvent.class));
      eventBus.register(TestEvent.class, testEventSubscriber);
      assertTrue(eventBus.isEnable(TestEvent.class));
      final String data = "test-data";
      eventBus.post(new TestEvent(data));
      assertEquals(data, testEventSubscriber.getCache());
    } finally {
      eventBus.unRegister(TestEvent.class, testEventSubscriber);
    }
    assertFalse(eventBus.isEnable(TestEvent.class));
  }
}
