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
package com.alipay.sofa.registry.server.session.bootstrap;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
import com.alipay.sofa.registry.task.MetricsableThreadPoolExecutor;
import com.alipay.sofa.registry.util.NamedThreadFactory;
import com.alipay.sofa.registry.util.OsUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author shangyu.wh
 * @version $Id: ExecutorManager.java, v 0.1 2017-11-28 14:41 shangyu.wh Exp $
 */
public class ExecutorManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorManager.class);

  private final ScheduledThreadPoolExecutor scheduler;

  private final ThreadPoolExecutor accessDataExecutor;
  private final ThreadPoolExecutor accessSubExecutor;
  private final ThreadPoolExecutor dataChangeRequestExecutor;
  private final ThreadPoolExecutor dataSlotSyncRequestExecutor;
  private final ThreadPoolExecutor accessMetadataExecutor;
  private final ThreadPoolExecutor consoleExecutor;
  private final ThreadPoolExecutor zoneSdkExecutor;
  private final ThreadPoolExecutor clientManagerCheckExecutor;
  private final ThreadPoolExecutor scanExecutor;

  @Autowired protected MetaServerService metaServerService;

  private final ThreadPoolExecutor appRevisionRegisterExecutor;

  private Map<String, ThreadPoolExecutor> reportExecutors = new HashMap<>();

  private static final String ACCESS_DATA_EXECUTOR = "AccessDataExecutor";

  private static final String ACCESS_SUB_EXECUTOR = "AccessSubExecutor";

  private static final String DATA_CHANGE_REQUEST_EXECUTOR = "DataChangeExecutor";

  private static final String DATA_SLOT_SYNC_REQUEST_EXECUTOR = "SlotSyncExecutor";

  private static final String ACCESS_METADATA_EXECUTOR = "AccessMetadataExecutor";

  private static final String CONSOLE_EXECUTOR = "ConsoleExecutor";

  private static final String ZONE_SDK_EXECUTOR = "ZoneSdkExecutor";

  private static final String CLIENT_MANAGER_CHECK_EXECUTOR = "ClientManagerCheckExecutor";

  private static final String APP_REVISION_REGISTER_EXECUTOR = "AppRevisionRegisterExecutor";

  private static final String SCAN_EXECUTOR = "ScanExecutor";

  public ExecutorManager(SessionServerConfig sessionServerConfig) {
    scheduler =
        new ScheduledThreadPoolExecutor(
            sessionServerConfig.getSessionSchedulerPoolSize(),
            new NamedThreadFactory("SessionScheduler"));

    accessDataExecutor =
        reportExecutors.computeIfAbsent(
            ACCESS_DATA_EXECUTOR,
            k ->
                new MetricsableThreadPoolExecutor(
                    ACCESS_DATA_EXECUTOR,
                    sessionServerConfig.getAccessDataExecutorPoolSize(),
                    sessionServerConfig.getAccessDataExecutorPoolSize(),
                    60,
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(sessionServerConfig.getAccessDataExecutorQueueSize()),
                    new NamedThreadFactory(ACCESS_DATA_EXECUTOR, true),
                    (r, executor) -> {
                      String msg =
                          String.format(
                              "Task(%s) %s rejected from %s, just ignore it to let client timeout.",
                              r.getClass(), r, executor);
                      LOGGER.error(msg);
                    }));

    accessSubExecutor =
        reportExecutors.computeIfAbsent(
            ACCESS_SUB_EXECUTOR,
            k ->
                new MetricsableThreadPoolExecutor(
                    ACCESS_SUB_EXECUTOR,
                    sessionServerConfig.getAccessSubDataExecutorPoolSize(),
                    sessionServerConfig.getAccessSubDataExecutorPoolSize(),
                    60,
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(
                        sessionServerConfig.getAccessSubDataExecutorQueueSize()),
                    new NamedThreadFactory(ACCESS_SUB_EXECUTOR, true),
                    (r, executor) -> {
                      String msg =
                          String.format(
                              "Task(%s) %s rejected from %s, just ignore it to let client timeout.",
                              r.getClass(), r, executor);
                      LOGGER.error(msg);
                    }));
    dataChangeRequestExecutor =
        reportExecutors.computeIfAbsent(
            DATA_CHANGE_REQUEST_EXECUTOR,
            k ->
                new MetricsableThreadPoolExecutor(
                    DATA_CHANGE_REQUEST_EXECUTOR,
                    sessionServerConfig.getDataChangeExecutorPoolSize(),
                    sessionServerConfig.getDataChangeExecutorPoolSize(),
                    60,
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(sessionServerConfig.getDataChangeExecutorQueueSize()),
                    new NamedThreadFactory(DATA_CHANGE_REQUEST_EXECUTOR, true)));

    dataSlotSyncRequestExecutor =
        reportExecutors.computeIfAbsent(
            DATA_SLOT_SYNC_REQUEST_EXECUTOR,
            k ->
                new MetricsableThreadPoolExecutor(
                    DATA_SLOT_SYNC_REQUEST_EXECUTOR,
                    sessionServerConfig.getSlotSyncWorkerSize(),
                    sessionServerConfig.getSlotSyncWorkerSize(),
                    60,
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(sessionServerConfig.getSlotSyncMaxBufferSize()),
                    new NamedThreadFactory(DATA_SLOT_SYNC_REQUEST_EXECUTOR, true)));
    accessMetadataExecutor =
        reportExecutors.computeIfAbsent(
            ACCESS_METADATA_EXECUTOR,
            k ->
                new MetricsableThreadPoolExecutor(
                    ACCESS_METADATA_EXECUTOR,
                    sessionServerConfig.getAccessMetadataWorkerSize(),
                    sessionServerConfig.getAccessMetadataWorkerSize(),
                    60,
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(sessionServerConfig.getAccessMetadataMaxBufferSize()),
                    new NamedThreadFactory(ACCESS_METADATA_EXECUTOR, true)));

    consoleExecutor =
        reportExecutors.computeIfAbsent(
            CONSOLE_EXECUTOR,
            k ->
                new MetricsableThreadPoolExecutor(
                    CONSOLE_EXECUTOR,
                    sessionServerConfig.getConsoleExecutorPoolSize(),
                    sessionServerConfig.getConsoleExecutorPoolSize(),
                    60L,
                    TimeUnit.SECONDS,
                    new LinkedBlockingQueue(sessionServerConfig.getConsoleExecutorQueueSize()),
                    new NamedThreadFactory(CONSOLE_EXECUTOR, true)));

    zoneSdkExecutor =
        reportExecutors.computeIfAbsent(
            ZONE_SDK_EXECUTOR,
            k ->
                MetricsableThreadPoolExecutor.newExecutor(
                    ZONE_SDK_EXECUTOR,
                    OsUtils.getCpuCount() * 5,
                    100,
                    new ThreadPoolExecutor.CallerRunsPolicy()));

    clientManagerCheckExecutor =
        reportExecutors.computeIfAbsent(
            CLIENT_MANAGER_CHECK_EXECUTOR,
            k ->
                MetricsableThreadPoolExecutor.newExecutor(
                    CLIENT_MANAGER_CHECK_EXECUTOR,
                    OsUtils.getCpuCount() * 5,
                    100,
                    new ThreadPoolExecutor.CallerRunsPolicy()));

    appRevisionRegisterExecutor =
        reportExecutors.computeIfAbsent(
            APP_REVISION_REGISTER_EXECUTOR,
            k ->
                new MetricsableThreadPoolExecutor(
                    APP_REVISION_REGISTER_EXECUTOR,
                    sessionServerConfig.getMetadataRegisterExecutorPoolSize(),
                    sessionServerConfig.getMetadataRegisterExecutorPoolSize(),
                    60,
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(
                        sessionServerConfig.getMetadataRegisterExecutorQueueSize()),
                    new NamedThreadFactory(APP_REVISION_REGISTER_EXECUTOR, true),
                    new ThreadPoolExecutor.CallerRunsPolicy()));

    scanExecutor =
        reportExecutors.computeIfAbsent(
            SCAN_EXECUTOR,
            k ->
                new MetricsableThreadPoolExecutor(
                    SCAN_EXECUTOR,
                    sessionServerConfig.getScanExecutorPoolSize(),
                    sessionServerConfig.getScanExecutorPoolSize(),
                    60,
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(sessionServerConfig.getScanExecutorQueueSize()),
                    new NamedThreadFactory(SCAN_EXECUTOR, true),
                    new ThreadPoolExecutor.CallerRunsPolicy()));
  }

  public void startScheduler() {}

  public void stopScheduler() {
    scheduler.shutdown();

    accessDataExecutor.shutdown();

    dataChangeRequestExecutor.shutdown();

    dataSlotSyncRequestExecutor.shutdown();
  }

  public Map<String, ThreadPoolExecutor> getReportExecutors() {
    return reportExecutors;
  }

  public ThreadPoolExecutor getAccessDataExecutor() {
    return accessDataExecutor;
  }

  public ThreadPoolExecutor getAccessSubExecutor() {
    return accessSubExecutor;
  }

  public ThreadPoolExecutor getDataChangeRequestExecutor() {
    return dataChangeRequestExecutor;
  }

  public ThreadPoolExecutor getDataSlotSyncRequestExecutor() {
    return dataSlotSyncRequestExecutor;
  }

  public ThreadPoolExecutor getAccessMetadataExecutor() {
    return accessMetadataExecutor;
  }

  public ThreadPoolExecutor getConsoleExecutor() {
    return consoleExecutor;
  }

  public ThreadPoolExecutor getZoneSdkExecutor() {
    return zoneSdkExecutor;
  }

  /**
   * Getter method for property <tt>appRevisionRegisterExecutor</tt>.
   *
   * @return property value of appRevisionRegisterExecutor
   */
  public ThreadPoolExecutor getAppRevisionRegisterExecutor() {
    return appRevisionRegisterExecutor;
  }

  /**
   * Getter method for property <tt>clientManagerCheckExecutor</tt>.
   *
   * @return property value of clientManagerCheckExecutor
   */
  public ThreadPoolExecutor getClientManagerCheckExecutor() {
    return clientManagerCheckExecutor;
  }

  public ThreadPoolExecutor getScanExecutor() {
    return scanExecutor;
  }
}
