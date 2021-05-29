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

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;

public class WakeUpLoopRunnableTest {
  @Test
  public void test() throws Exception {
    MockLoop loop = new MockLoop();
    loop.suspend();
    Thread t = ConcurrentUtils.createDaemonThread("loop-test", loop);
    t.start();
    loop.wakeup();
    Thread.sleep(100);
    Assert.assertEquals(0, loop.count.get());
    Assert.assertTrue(loop.isSuspended());
    loop.resume();
    loop.wakeup();
    Assert.assertFalse(loop.isSuspended());
    Thread.sleep(100);
    int loopCount = loop.count.get();
    Assert.assertTrue("loopCount:" + loopCount, loopCount != 0);

    loop.runException = true;
    loop.wakeup();
    Thread.sleep(100);
    Assert.assertTrue(t.isAlive());

    loop.close();
    Assert.assertTrue(loop.isClosed());
    Thread.sleep(200);

    Assert.assertFalse(t.isAlive());
  }

  private static final class MockLoop extends WakeUpLoopRunnable {
    final AtomicInteger count = new AtomicInteger();
    boolean runException;

    @Override
    public void runUnthrowable() {
      count.incrementAndGet();
      if (runException) {
        throw new RuntimeException();
      }
    }

    @Override
    public int getWaitingMillis() {
      return 10;
    }
  }
}
