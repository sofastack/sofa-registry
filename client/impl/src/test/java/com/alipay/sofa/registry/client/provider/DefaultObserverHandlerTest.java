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

import static org.mockito.Mockito.times;

import com.alipay.sofa.registry.client.api.*;
import com.alipay.sofa.registry.client.event.DefaultEventBus;
import org.junit.Test;
import org.mockito.Mockito;

public class DefaultObserverHandlerTest {
  private RegistryClientConfig config;
  private EventBus eventBus;
  private DefaultObserverHandler handler;

  void init() {
    config = DefaultRegistryClientConfigBuilder.start().build();
    eventBus = new DefaultEventBus(config);
    handler = new DefaultObserverHandler(config, eventBus);
  }

  @Test
  public void testSubscriber() throws Exception {
    init();
    handler.notify((Subscriber) null);
    Subscriber exceptionSubscriber = Mockito.mock(Subscriber.class);
    Mockito.when(exceptionSubscriber.getDataObserver()).thenThrow(new RuntimeException());
    handler.notify(exceptionSubscriber);
    Subscriber mockSubscriber = Mockito.mock(Subscriber.class);
    Mockito.when(mockSubscriber.getDataObserver())
        .thenReturn(Mockito.mock(SubscriberDataObserver.class));
    handler.notify(mockSubscriber);
    Thread.sleep(100);
    Mockito.verify(mockSubscriber, times(1)).getDataObserver();
  }

  @Test
  public void testConfigurator() throws Exception {
    init();
    handler.notify((Configurator) null);
    Configurator exceptionConfigurator = Mockito.mock(Configurator.class);
    Mockito.when(exceptionConfigurator.getDataObserver()).thenThrow(new RuntimeException());
    handler.notify(exceptionConfigurator);
    Configurator mockConfigurator = Mockito.mock(Configurator.class);
    Mockito.when(mockConfigurator.getDataObserver())
        .thenReturn(Mockito.mock(ConfigDataObserver.class));
    handler.notify(mockConfigurator);
    Thread.sleep(100);
    Mockito.verify(mockConfigurator, times(1)).getDataObserver();
  }
}
