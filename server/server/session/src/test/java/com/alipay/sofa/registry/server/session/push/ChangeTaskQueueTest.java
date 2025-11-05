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
package com.alipay.sofa.registry.server.session.push;

import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author huicha
 * @date 2025/10/27
 */
public class ChangeTaskQueueTest {

  @Test
  public void testPushTask() {
    MockChangeTask taskOne = new MockChangeTask("Key1", 1000L);
    MockChangeTask taskTwo = new MockChangeTask("Key2", 2000L);

    ChangeTaskQueue<String, MockChangeTask> queue = new ChangeTaskQueue<>();
    queue.pushTask(taskOne);
    queue.pushTask(taskTwo);

    MockChangeTask taskOneFromQueue = queue.findTask("Key1");
    Assert.assertNotNull(taskOneFromQueue);
    Assert.assertSame(taskOne, taskOneFromQueue);

    MockChangeTask taskTwoFromQueue = queue.findTask("Key2");
    Assert.assertNotNull(taskTwoFromQueue);
    Assert.assertSame(taskTwo, taskTwoFromQueue);
  }

  @Test
  public void testPopTimeoutTasks() throws InterruptedException {
    long now = System.currentTimeMillis();
    long timeoutOne = now + 1000L;
    long timeoutTwo = now + 2000L;
    long timeoutThree = now + 3000L;

    MockChangeTask taskOne = new MockChangeTask("Key1", timeoutOne);
    MockChangeTask taskTwo = new MockChangeTask("Key2", timeoutTwo);
    MockChangeTask taskThree = new MockChangeTask("Key3", timeoutThree);

    ChangeTaskQueue<String, MockChangeTask> queue = new ChangeTaskQueue<>();
    queue.pushTask(taskOne);
    queue.pushTask(taskTwo);
    queue.pushTask(taskThree);

    Thread.sleep(TimeUnit.SECONDS.toMillis(1L));

    // 第一次获取超时任务
    List<MockChangeTask> timeoutTasks = queue.popTimeoutTasks(System.currentTimeMillis());
    Assert.assertEquals(1, timeoutTasks.size());
    Assert.assertEquals(taskOne, timeoutTasks.get(0));

    MockChangeTask taskOneFromQueue = queue.findTask("Key1");
    Assert.assertNull(taskOneFromQueue);

    MockChangeTask taskTwoFromQueue = queue.findTask("Key2");
    Assert.assertNotNull(taskTwoFromQueue);
    Assert.assertSame(taskTwo, taskTwoFromQueue);

    MockChangeTask taskThreeFromQueue = queue.findTask("Key3");
    Assert.assertNotNull(taskThreeFromQueue);
    Assert.assertSame(taskThree, taskThreeFromQueue);

    Thread.sleep(TimeUnit.SECONDS.toMillis(1L));

    // 第二次获取超时任务
    timeoutTasks = queue.popTimeoutTasks(System.currentTimeMillis());
    Assert.assertEquals(1, timeoutTasks.size());
    Assert.assertEquals(taskTwo, timeoutTasks.get(0));

    taskOneFromQueue = queue.findTask("Key1");
    Assert.assertNull(taskOneFromQueue);

    taskTwoFromQueue = queue.findTask("Key2");
    Assert.assertNull(taskTwoFromQueue);

    taskThreeFromQueue = queue.findTask("Key3");
    Assert.assertNotNull(taskThreeFromQueue);
    Assert.assertSame(taskThree, taskThreeFromQueue);

    Thread.sleep(TimeUnit.SECONDS.toMillis(1L));

    // 第三次获取超时任务
    timeoutTasks = queue.popTimeoutTasks(System.currentTimeMillis());
    Assert.assertEquals(1, timeoutTasks.size());
    Assert.assertEquals(taskThree, timeoutTasks.get(0));

    timeoutTasks = queue.popTimeoutTasks(System.currentTimeMillis());
    Assert.assertEquals(0, timeoutTasks.size());

    taskOneFromQueue = queue.findTask("Key1");
    Assert.assertNull(taskOneFromQueue);

    taskTwoFromQueue = queue.findTask("Key2");
    Assert.assertNull(taskTwoFromQueue);

    taskThreeFromQueue = queue.findTask("Key3");
    Assert.assertNull(taskThreeFromQueue);
  }
}

class MockChangeTask implements ChangeTask<String> {

  private String key;

  private long deadline;

  public MockChangeTask(String key, long deadline) {
    this.key = key;
    this.deadline = deadline;
  }

  @Override
  public String key() {
    return this.key;
  }

  @Override
  public long deadline() {
    return this.deadline;
  }
}
