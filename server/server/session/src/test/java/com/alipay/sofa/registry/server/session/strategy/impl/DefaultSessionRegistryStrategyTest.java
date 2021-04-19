package com.alipay.sofa.registry.server.session.strategy.impl;

import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.Watcher;
import com.alipay.sofa.registry.server.session.AbstractSessionServerTestBase;
import com.alipay.sofa.registry.server.session.push.FirePushService;
import com.alipay.sofa.registry.task.listener.DefaultTaskListenerManager;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class DefaultSessionRegistryStrategyTest extends AbstractSessionServerTestBase {

  private DefaultSessionRegistryStrategy strategy = new DefaultSessionRegistryStrategy();

  private FirePushService firePushService;

  private TaskListenerManager taskListenerManager;

  @Before
  public void beoforeDefaultSessionRegistryStrategyTest() {
    firePushService = mock(FirePushService.class);
    taskListenerManager = new DefaultTaskListenerManager();
    strategy.setSessionServerConfig(sessionServerConfig)
            .setFirePushService(firePushService)
            .setTaskListenerManager(taskListenerManager);
  }

  @Test
  public void testAfterPublisherRegister() {
    strategy.afterPublisherRegister(new Publisher());
  }

  @Test
  public void testAfterSubscriberRegister() {
  }

  @Test
  public void testAfterWatcherRegister() {
    strategy.afterWatcherRegister(randomWatcher());

  }

  @Test
  public void testAfterPublisherUnRegister() {
  }

  @Test
  public void testAfterSubscriberUnRegister() {
  }

  @Test
  public void testAfterWatcherUnRegister() {
  }
}