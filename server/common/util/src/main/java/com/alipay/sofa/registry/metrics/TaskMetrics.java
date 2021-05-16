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

import com.alipay.remoting.ProtocolCode;
import com.alipay.remoting.ProtocolManager;
import com.alipay.remoting.rpc.protocol.RpcProtocol;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author shangyu.wh
 * @version $Id: ThreadMetrics.java, v 0.1 2018-11-18 15:19 shangyu.wh Exp $
 */
public class TaskMetrics {
  private static final Logger LOGGER = LoggerFactory.getLogger("THREAD-POOL-METRICS");

  private final MetricRegistry metrics = new MetricRegistry();

  private final Set<String> executors = Collections.newSetFromMap(new ConcurrentHashMap<>());

  private static final TaskMetrics instance = new TaskMetrics();

  private static boolean boltRegistered = false;

  private TaskMetrics() {
    ExecutorMetricsWatchDog executorMetricsWatchDog = new ExecutorMetricsWatchDog();
    ConcurrentUtils.createDaemonThread("executorMetrics", executorMetricsWatchDog).start();
  }

  public static TaskMetrics getInstance() {
    return instance;
  }

  public MetricRegistry getMetricRegistry() {
    return this.metrics;
  }

  public synchronized void registerBolt() {
    if (boltRegistered) {
      return;
    }
    ThreadPoolExecutor boltDefaultExecutor =
        (ThreadPoolExecutor)
            ProtocolManager.getProtocol(ProtocolCode.fromBytes(RpcProtocol.PROTOCOL_CODE))
                .getCommandHandler()
                .getDefaultExecutor();
    registerThreadExecutor("BoltDefaultExecutor", boltDefaultExecutor);
    boltRegistered = true;
  }

  public void registerThreadExecutor(String executorName, ThreadPoolExecutor executor) {
    registerIfAbsent(
        MetricRegistry.name(executorName, "queue"),
        (Gauge<Integer>) () -> executor.getQueue().size());

    registerIfAbsent(
        MetricRegistry.name(executorName, "poolSize"), (Gauge<Integer>) executor::getPoolSize);

    registerIfAbsent(
        MetricRegistry.name(executorName, "active"), (Gauge<Integer>) executor::getActiveCount);

    registerIfAbsent(
        MetricRegistry.name(executorName, "completed"),
        (Gauge<Long>) executor::getCompletedTaskCount);

    registerIfAbsent(
        MetricRegistry.name(executorName, "task"), (Gauge<Long>) executor::getTaskCount);
    executors.add(executorName);
  }

  private boolean registerIfAbsent(String metricName, Metric metric) {
    Map<String, Metric> metricMap = metrics.getMetrics();
    if (metricMap.containsKey(metricName)) {
      LOGGER.warn("executor.metric exists {}", metricName);
      return false;
    }
    metrics.register(metricName, metric);
    return true;
  }

  class ExecutorMetricsWatchDog extends WakeUpLoopRunnable {

    @Override
    public void runUnthrowable() {
      for (String executorName : executors) {
        Map<String, Gauge> map = metrics.getGauges((name, value) -> name.startsWith(executorName));
        StringBuilder sb = new StringBuilder();
        sb.append(executorName);
        map.forEach(
            (key, gauge) -> {
              String name = key.substring(executorName.length() + 1);
              sb.append(", ").append(name).append(":").append(gauge.getValue());
            });
        LOGGER.info(sb.toString());
      }
    }

    @Override
    public int getWaitingMillis() {
      return 5000;
    }
  }
}
