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
package com.alipay.sofa.registry.metrics;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author shangyu.wh
 * @version $Id: ThreadMetrics.java, v 0.1 2018-11-18 15:19 shangyu.wh Exp $
 */
public class TaskMetrics {
  private static final Logger LOGGER = LoggerFactory.getLogger(TaskMetrics.class);

  private final MetricRegistry metrics = new MetricRegistry();

  private TaskMetrics() {}

  private static final TaskMetrics instance = new TaskMetrics();

  public static TaskMetrics getInstance() {
    return instance;
  }

  public MetricRegistry getMetricRegistry() {
    return this.metrics;
  }

  public void registerThreadExecutor(String executorName, ThreadPoolExecutor executor) {

    registerIfAbsent(
        MetricRegistry.name(executorName, "queue"),
        (Gauge<Integer>) () -> executor.getQueue().size());

    registerIfAbsent(
        MetricRegistry.name(executorName, "current"), (Gauge<Integer>) executor::getPoolSize);

    registerIfAbsent(
        MetricRegistry.name(executorName, "active"), (Gauge<Integer>) executor::getActiveCount);

    registerIfAbsent(
        MetricRegistry.name(executorName, "completed"),
        (Gauge<Long>) executor::getCompletedTaskCount);

    registerIfAbsent(
        MetricRegistry.name(executorName, "task"), (Gauge<Long>) executor::getTaskCount);
  }

  private boolean registerIfAbsent(String executorName, Metric metric) {
    Map<String, Metric> metricMap = metrics.getMetrics();
    if (metricMap.containsKey(executorName)) {
      LOGGER.warn("executor.metric exists {}", executorName);
      return false;
    }
    metrics.register(executorName, metric);
    return true;
  }
}
