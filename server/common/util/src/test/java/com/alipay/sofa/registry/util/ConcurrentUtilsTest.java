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

import com.google.common.collect.Lists;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class ConcurrentUtilsTest {

  @Test
  public void testInterrupt() {
    // init env
    ConcurrentUtils.sleepUninterruptibly(1, TimeUnit.MILLISECONDS);

    Thread.currentThread().interrupt();
    ConcurrentUtils.sleepUninterruptibly(1, TimeUnit.MILLISECONDS);

    Thread.currentThread().interrupt();
    Object sync = new Object();
    synchronized (sync) {
      ConcurrentUtils.objectWaitUninterruptibly(sync, 1);
    }

    Thread.currentThread().interrupt();
    ConcurrentUtils.pollUninterruptibly(new SynchronousQueue(), 1, TimeUnit.MILLISECONDS);
  }

  @Test
  public void testSafeParaLoop() throws InterruptedException {
    ThreadPoolExecutor executor =
        new ThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    ConcurrentUtils.SafeParaLoop loop =
        new ConcurrentUtils.SafeParaLoop<String>(executor, Lists.newArrayList("1", "2")) {
          @Override
          protected void doRun0(String s) throws Exception {
            throw new RuntimeException(s);
          }
        };
    loop.run();
    loop.runAndWait(1000);
  }
}
