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

import com.alipay.sofa.registry.concurrent.SingleFlight;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Assert;
import org.junit.Test;

public class SingleFlightTest {
  @Test
  public void test() throws Exception {
    final SingleFlight single = new SingleFlight();
    MockFlight f1 = new MockFlight();
    MockFlight f2 = new MockFlight();
    ExecutorService executors = Executors.newFixedThreadPool(2);
    executors.submit(() -> single.execute("test", f1));
    Thread.sleep(50);
    executors.submit(() -> single.execute("test", f2));
    Thread.sleep(50);
    Assert.assertFalse(f1.run);
    Assert.assertFalse(f2.run);

    f2.sleep = false;
    Thread.sleep(50);
    f1.sleep = false;

    Thread.sleep(50);
    Assert.assertTrue(f1.run);
    Assert.assertFalse(f2.run);
    executors.shutdown();
  }

  private final class MockFlight implements Callable {
    volatile boolean sleep = true;
    volatile boolean run;

    @Override
    public Object call() throws Exception {
      while (sleep) {
        Thread.sleep(10);
      }
      run = true;
      return null;
    }
  }
}
