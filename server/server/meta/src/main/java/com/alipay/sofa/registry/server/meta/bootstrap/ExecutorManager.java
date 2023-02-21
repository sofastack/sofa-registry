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
package com.alipay.sofa.registry.server.meta.bootstrap;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MultiClusterMetaServerConfig;
import com.alipay.sofa.registry.task.KeyedThreadPoolExecutor;
import com.alipay.sofa.registry.task.MetricsableThreadPoolExecutor;
import com.alipay.sofa.registry.util.NamedThreadFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author xiaojian.xj
 * @version $Id: ExecutorManager.java, v 0.1 2021年04月20日 14:15 xiaojian.xj Exp $
 */
public class ExecutorManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorManager.class);

  private final ScheduledThreadPoolExecutor scheduler;

  private final ThreadPoolExecutor multiClusterConfigReloadExecutor;
  private final ThreadPoolExecutor remoteClusterHandlerExecutor;

  private final KeyedThreadPoolExecutor remoteSlotSyncerExecutor;

  private static final String MULTI_CLUSTER_CONFIG_RELOAD_EXECUTOR =
      "MULTI_CLUSTER_CONFIG_RELOAD_EXECUTOR";

  private static final String REMOTE_SLOT_SYNCER_EXECUTOR = "REMOTE_SLOT_SYNCER_EXECUTOR";

  private static final String REMOTE_CLUSTER_HANDLER_EXECUTOR = "REMOTE_CLUSTER_HANDLER_EXECUTOR";

  private Map<String, ThreadPoolExecutor> reportExecutors = new HashMap<>();

  private Map<String, KeyedThreadPoolExecutor> keyExecutors = new HashMap<>();

  public ExecutorManager(
      MetaServerConfig metaServerConfig,
      MultiClusterMetaServerConfig multiClusterMetaServerConfig) {
    scheduler =
        new ScheduledThreadPoolExecutor(
            metaServerConfig.getMetaSchedulerPoolSize(), new NamedThreadFactory("MetaScheduler"));

    multiClusterConfigReloadExecutor =
        reportExecutors.computeIfAbsent(
            MULTI_CLUSTER_CONFIG_RELOAD_EXECUTOR,
            k ->
                new MetricsableThreadPoolExecutor(
                    MULTI_CLUSTER_CONFIG_RELOAD_EXECUTOR,
                    multiClusterMetaServerConfig.getMultiClusterConfigReloadWorkerSize(),
                    multiClusterMetaServerConfig.getMultiClusterConfigReloadWorkerSize(),
                    60,
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(
                        multiClusterMetaServerConfig.getMultiClusterConfigReloadMaxBufferSize()),
                    new NamedThreadFactory(MULTI_CLUSTER_CONFIG_RELOAD_EXECUTOR, true),
                    (r, executor) -> {
                      String msg =
                          String.format(
                              "Multi cluster reload task(%s) %s rejected from %s, just ignore it.",
                              r.getClass(), r, executor);
                      LOGGER.error(msg);
                    }));

    remoteSlotSyncerExecutor =
        keyExecutors.computeIfAbsent(
            REMOTE_SLOT_SYNCER_EXECUTOR,
            k ->
                new KeyedThreadPoolExecutor(
                    REMOTE_SLOT_SYNCER_EXECUTOR,
                    multiClusterMetaServerConfig.getRemoteSlotSyncerExecutorPoolSize(),
                    multiClusterMetaServerConfig.getRemoteSlotSyncerExecutorQueueSize()));

    remoteClusterHandlerExecutor =
        reportExecutors.computeIfAbsent(
            REMOTE_CLUSTER_HANDLER_EXECUTOR,
            k ->
                new MetricsableThreadPoolExecutor(
                    REMOTE_CLUSTER_HANDLER_EXECUTOR,
                    multiClusterMetaServerConfig.getRemoteClusterHandlerCoreSize(),
                    multiClusterMetaServerConfig.getRemoteClusterHandlerMaxSize(),
                    60,
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(
                        multiClusterMetaServerConfig.getRemoteClusterHandlerMaxBufferSize()),
                    new NamedThreadFactory(REMOTE_CLUSTER_HANDLER_EXECUTOR, true)));
  }

  public void startScheduler() {}

  public void stopScheduler() {
    if (scheduler != null && !scheduler.isShutdown()) {
      scheduler.shutdown();
    }
  }

  /**
   * Getter method for property <tt>multiClusterConfigReloadExecutor</tt>.
   *
   * @return property value of multiClusterConfigReloadExecutor
   */
  public ThreadPoolExecutor getMultiClusterConfigReloadExecutor() {
    return multiClusterConfigReloadExecutor;
  }

  /**
   * Getter method for property <tt>remoteSlotSyncerExecutor</tt>.
   *
   * @return property value of remoteSlotSyncerExecutor
   */
  public KeyedThreadPoolExecutor getRemoteSlotSyncerExecutor() {
    return remoteSlotSyncerExecutor;
  }

  /**
   * Getter method for property <tt>remoteClusterHandlerExecutor</tt>.
   *
   * @return property value of remoteClusterHandlerExecutor
   */
  public ThreadPoolExecutor getRemoteClusterHandlerExecutor() {
    return remoteClusterHandlerExecutor;
  }
}
