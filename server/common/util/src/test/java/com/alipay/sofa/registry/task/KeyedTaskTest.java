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
package com.alipay.sofa.registry.task;

import com.alipay.sofa.registry.TestUtils;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;

public class KeyedTaskTest {

  @Test
  public void testRun() {
    KeyedTask task =
        new KeyedTask(
            "test",
            () -> {
              throw new RuntimeException();
            });
    task.run();
    Assert.assertTrue(task.isFinished());
    Assert.assertFalse(task.isSuccess());
    Assert.assertTrue(task.isFailed());

    task =
        new KeyedTask(
            "test",
            () -> {
              throw new TaskErrorSilenceException();
            });
    task.run();
    Assert.assertTrue(task.isFinished());
    Assert.assertFalse(task.isSuccess());
    Assert.assertTrue(task.isFailed());

    Assert.assertFalse(task.isOverAfter(1000000));
    Assert.assertTrue(task.isOverAfter(0));
  }

  @Test
  public void testCancel() {
    final AtomicInteger count = new AtomicInteger();
    long now = System.currentTimeMillis();
    KeyedTask task =
        new KeyedTask(
            "test",
            () -> {
              count.incrementAndGet();
            });
    TestUtils.assertBetween(task.getCreateTime(), now, System.currentTimeMillis());
    Assert.assertEquals(task.getEndTime(), 0);
    Assert.assertEquals(task.getStartTime(), 0);
    Assert.assertEquals(task.key(), "test");
    Assert.assertNotNull(task.getRunnable());
    task.cancel();

    // has not run, not finish
    Assert.assertFalse(task.isFinished());
    Assert.assertFalse(task.isSuccess());
    Assert.assertFalse(task.isFailed());

    Assert.assertTrue(task.canceled);
    // not finish
    Assert.assertFalse(task.isOverAfter(1));
    task.run();
    // cancel always return true
    Assert.assertTrue(task.isOverAfter(1000000));
    // has cancel, not exec
    Assert.assertEquals(count.get(), 0);
    Assert.assertTrue(task.isFinished());
    Assert.assertTrue(task.isSuccess());
    Assert.assertFalse(task.isFailed());

    Assert.assertNotNull(task.toString());
  }
}
