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
package com.alipay.sofa.registry.util;

import static org.junit.Assert.*;

import com.alipay.sofa.registry.AbstractTestBase;
import java.util.concurrent.*;
import org.junit.Assert;
import org.junit.Test;

public class DefaultExecutorFactoryTest extends AbstractTestBase {

  @Test
  public void testCreateCachedThreadPoolFactory() throws InterruptedException, TimeoutException {
    ThreadPoolExecutor executor =
        (ThreadPoolExecutor)
            DefaultExecutorFactory.createCachedThreadPoolFactory(
                    "test", 1, 10, TimeUnit.MILLISECONDS)
                .create();
    Assert.assertTrue(executor.getThreadFactory() instanceof NamedThreadFactory);
    Assert.assertTrue(
        ((NamedThreadFactory) executor.getThreadFactory()).getNamePrefix().startsWith("test"));
    Assert.assertTrue(executor.getQueue() instanceof SynchronousQueue);
    int tasks = 10;
    new ConcurrentExecutor(tasks, executor)
        .execute(
            new Runnable() {
              @Override
              public void run() {
                try {
                  Thread.sleep(10);
                } catch (InterruptedException e) {

                }
              }
            });
    waitConditionUntilTimeOut(() -> executor.getCompletedTaskCount() >= tasks, 1000);
    waitConditionUntilTimeOut(() -> executor.getCorePoolSize() < 2, 110);
  }

  @Test
  public void testBuilderWithQueueFactory() {
    DefaultExecutorFactory factory =
        DefaultExecutorFactory.builder()
            .workQueueFactory(
                new ObjectFactory<BlockingQueue<Runnable>>() {
                  @Override
                  public BlockingQueue<Runnable> create() {
                    return new LinkedBlockingDeque<Runnable>(10);
                  }
                })
            .build();
    ThreadPoolExecutor executor1 = (ThreadPoolExecutor) factory.create();
    ThreadPoolExecutor executor2 = (ThreadPoolExecutor) factory.create();
    Assert.assertNotSame(executor1.getQueue(), executor2.getQueue());
  }

  @Test
  public void testBuilderWithQueueSize() {
    DefaultExecutorFactory factory = DefaultExecutorFactory.builder().queueSize(2).build();
    ThreadPoolExecutor executor1 = (ThreadPoolExecutor) factory.create();
    ThreadPoolExecutor executor2 = (ThreadPoolExecutor) factory.create();
    Assert.assertNotSame(executor1.getQueue(), executor2.getQueue());
  }

  @Test
  public void testBuilder() {
    DefaultExecutorFactory factory =
        DefaultExecutorFactory.builder().queueSize(2).allowCoreThreadTimeOut(false).build();
    Assert.assertFalse(((ThreadPoolExecutor) factory.create()).allowsCoreThreadTimeOut());
    factory =
        DefaultExecutorFactory.builder()
            .queueSize(2)
            .allowCoreThreadTimeOut(false)
            .threadNamePrefix("test")
            .corePoolSize(100)
            .corePoolTimeAlive(1000)
            .corePoolTimeAliveUnit(TimeUnit.MILLISECONDS)
            .rejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy())
            .maxPoolSize(200)
            .build();
    ThreadPoolExecutor executor = (ThreadPoolExecutor) factory.create();
    Assert.assertEquals(100, executor.getCorePoolSize());
    Assert.assertEquals(200, executor.getMaximumPoolSize());
    Assert.assertEquals(1000, executor.getKeepAliveTime(TimeUnit.MILLISECONDS));
    Assert.assertTrue(
        executor.getRejectedExecutionHandler() instanceof ThreadPoolExecutor.AbortPolicy);

    factory =
        DefaultExecutorFactory.builder()
            .queueSize(2)
            .allowCoreThreadTimeOut(false)
            .threadNamePrefix("test")
            .threadFactory(
                new ThreadFactory() {
                  @Override
                  public Thread newThread(Runnable r) {
                    return new Thread(r);
                  }
                })
            .corePoolSize(100)
            .corePoolTimeAlive(1000)
            .corePoolTimeAliveUnit(TimeUnit.MILLISECONDS)
            .rejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy())
            .maxPoolSize(200)
            .build();
    executor = (ThreadPoolExecutor) factory.create();
    Assert.assertFalse(executor.getThreadFactory() instanceof NamedThreadFactory);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testThreadQueueSize() {
    DefaultExecutorFactory factory =
        DefaultExecutorFactory.builder()
            .allowCoreThreadTimeOut(false)
            .threadNamePrefix("test")
            .corePoolSize(100)
            .corePoolTimeAlive(1000)
            .corePoolTimeAliveUnit(TimeUnit.MILLISECONDS)
            .rejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy())
            .maxPoolSize(200)
            .workQueueFactory(
                new ObjectFactory<BlockingQueue<Runnable>>() {
                  @Override
                  public BlockingQueue<Runnable> create() {
                    return new LinkedBlockingDeque<>();
                  }
                })
            .queueSize(2)
            .build();
  }
}
