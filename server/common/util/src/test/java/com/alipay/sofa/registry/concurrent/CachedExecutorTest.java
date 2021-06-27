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
package com.alipay.sofa.registry.concurrent;

import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.google.common.collect.Sets;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;

public class CachedExecutorTest {
  @Test
  public void testExecute() {
    AtomicInteger i = new AtomicInteger();

    CachedExecutor cachedExecutor = new CachedExecutor(100);
    ThreadPoolExecutor e =
        new ThreadPoolExecutor(5, 5, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    new ConcurrentUtils.SafeParaLoop(e, Sets.newHashSet("1", "1", "1", "1", "1")) {
      @Override
      protected void doRun0(Object o) throws Exception {
        cachedExecutor.execute("1", i::incrementAndGet);
      }
    }.runAndWait(1000);

    Assert.assertEquals(1, i.get());
  }
}
