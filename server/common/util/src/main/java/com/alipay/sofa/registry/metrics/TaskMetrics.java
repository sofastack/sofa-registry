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

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Sets;
import io.netty.util.internal.ConcurrentSet;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *
 * @author shangyu.wh
 * @version $Id: ThreadMetrics.java, v 0.1 2018-11-18 15:19 shangyu.wh Exp $
 */
public class TaskMetrics {

    private final MetricRegistry metrics;
    private final Set<String>    executorNames = Sets.newConcurrentHashSet();

    private TaskMetrics() {
        this.metrics = new MetricRegistry();
    }

    private volatile static TaskMetrics instance;

    public static TaskMetrics getInstance() {
        if (instance == null) {
            synchronized (TaskMetrics.class) {
                if (instance == null) {
                    instance = new TaskMetrics();
                }
            }
        }

        return instance;
    }

    public MetricRegistry getMetricRegistry() {
        return this.metrics;
    }

    public void registerThreadExecutor(String executorName, ThreadPoolExecutor executor) {
        executorNames.add(executorName);

        metrics.register(MetricRegistry.name(executorName, "queue"),
            (Gauge<Integer>) () -> executor.getQueue().size());

        metrics.register(MetricRegistry.name(executorName, "current"),
            (Gauge<Integer>) executor::getPoolSize);

        metrics.register(MetricRegistry.name(executorName, "active"),
            (Gauge<Integer>) executor::getActiveCount);

        metrics.register(MetricRegistry.name(executorName, "completed"),
            (Gauge<Long>) executor::getCompletedTaskCount);

        metrics.register(MetricRegistry.name(executorName, "task"),
            (Gauge<Long>) executor::getTaskCount);
    }

    public Set<String> getExecutorNames() {
        return executorNames;
    }

    public String metricsString() {
        final String SYMBOLIC = "  └─ ";
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append("ExecutorMetrics").append(" >>>>>>>>");
        sb.append("\n");
        for (String executorName : getExecutorNames()) {
            MetricRegistry metricRegistry = getMetricRegistry();
            Map<String, Gauge> map = metricRegistry
                .getGauges((name, value) -> name.startsWith(executorName));

            sb.append(SYMBOLIC).append(executorName);
            map.forEach((key, gauge) -> {
                String name = key.substring(executorName.length() + 1);
                sb.append(", ").append(name).append(":").append(gauge.getValue());
            });
            sb.append("\n");
        }
        return sb.toString();
    }
}