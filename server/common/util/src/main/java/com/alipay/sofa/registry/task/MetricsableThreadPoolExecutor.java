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

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.metrics.TaskMetrics;
import com.alipay.sofa.registry.util.NamedThreadFactory;
import com.alipay.sofa.registry.util.StringFormatter;
import java.util.concurrent.*;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-12-15 11:50 yuzhi.lyz Exp $
 */
public class MetricsableThreadPoolExecutor extends ThreadPoolExecutor {
  private static final Logger LOGGER = LoggerFactory.getLogger(MetricsableThreadPoolExecutor.class);

  protected final String executorName;

  public MetricsableThreadPoolExecutor(
      String executorName,
      int corePoolSize,
      int maximumPoolSize,
      long keepAliveTime,
      TimeUnit unit,
      BlockingQueue<Runnable> workQueue,
      ThreadFactory threadFactory,
      RejectedExecutionHandler handler) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    this.executorName = executorName;
    registerTaskMetrics();
    this.setRejectedExecutionHandler(handler);
  }

  public MetricsableThreadPoolExecutor(
      String executorName,
      int corePoolSize,
      int maximumPoolSize,
      long keepAliveTime,
      TimeUnit unit,
      BlockingQueue<Runnable> workQueue,
      ThreadFactory threadFactory) {
    this(
        executorName,
        corePoolSize,
        maximumPoolSize,
        keepAliveTime,
        unit,
        workQueue,
        threadFactory,
        new RejectedLogErrorHandler(LOGGER, true));
  }

  private void registerTaskMetrics() {
    TaskMetrics.getInstance().registerThreadExecutor(executorName, this);
  }

  @Override
  public String toString() {
    return StringFormatter.format("{}:{}", executorName, super.toString());
  }

  public static MetricsableThreadPoolExecutor newExecutor(
      String executorName, int corePoolSize, int size, RejectedExecutionHandler handler) {
    return new MetricsableThreadPoolExecutor(
        executorName,
        corePoolSize,
        corePoolSize,
        60,
        TimeUnit.SECONDS,
        size <= 1024 * 4 ? new ArrayBlockingQueue<>(size) : new LinkedBlockingQueue<>(size),
        new NamedThreadFactory(executorName, true),
        handler);
  }

  public static MetricsableThreadPoolExecutor newExecutor(
      String executorName, int corePoolSize, int size) {
    return newExecutor(executorName, corePoolSize, size, new RejectedLogErrorHandler(LOGGER, true));
  }
}
