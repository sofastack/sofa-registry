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
package com.alipay.sofa.registry.server.data.executor;

import com.alipay.sofa.registry.util.NamedThreadFactory;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * the factory to create executor
 *
 * @author qian.lqlq
 * @version $Id: ExecutorFactory.java, v 0.1 2018-03-08 14:50 qian.lqlq Exp $
 */
public class ExecutorFactory {

  /**
   * new thread pool
   *
   * @param size
   * @param name
   * @return
   */
  public static Executor newFixedThreadPool(int size, String name) {
    return new ThreadPoolExecutor(
        size,
        size,
        0L,
        TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<>(),
        new NamedThreadFactory(name));
  }
  /**
   * new scheduled thread pool
   *
   * @param size
   * @param name
   * @return
   */
  public static ScheduledExecutorService newScheduledThreadPool(int size, String name) {
    return new ScheduledThreadPoolExecutor(size, new NamedThreadFactory(name));
  }

  /**
   * new single thread executor
   *
   * @param name
   * @return
   */
  public static Executor newSingleThreadExecutor(String name) {
    return new ThreadPoolExecutor(
        1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new NamedThreadFactory(name));
  }
}
