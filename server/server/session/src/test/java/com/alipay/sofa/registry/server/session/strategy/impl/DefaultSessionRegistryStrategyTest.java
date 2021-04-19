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
package com.alipay.sofa.registry.server.session.strategy.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.server.session.AbstractSessionServerTestBase;
import com.alipay.sofa.registry.server.session.push.FirePushService;
import com.alipay.sofa.registry.task.listener.DefaultTaskListenerManager;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;
import org.junit.Before;
import org.junit.Test;

public class DefaultSessionRegistryStrategyTest extends AbstractSessionServerTestBase {

  private DefaultSessionRegistryStrategy strategy = new DefaultSessionRegistryStrategy();

  private FirePushService firePushService;

  private TaskListenerManager taskListenerManager;

  @Before
  public void beoforeDefaultSessionRegistryStrategyTest() {
    firePushService = mock(FirePushService.class);
    taskListenerManager = new DefaultTaskListenerManager();
    strategy
        .setSessionServerConfig(sessionServerConfig)
        .setFirePushService(firePushService)
        .setTaskListenerManager(taskListenerManager);
  }

  @Test
  public void testAfterPublisherRegister() {
    strategy.afterPublisherRegister(new Publisher());
  }

  @Test
  public void testAfterSubscriberRegister() {}

  @Test
  public void testAfterWatcherRegister() {
    strategy.afterWatcherRegister(randomWatcher());
  }

  @Test
  public void testAfterPublisherUnRegister() {}

  @Test
  public void testAfterSubscriberUnRegister() {}

  @Test
  public void testAfterWatcherUnRegister() {}
}
