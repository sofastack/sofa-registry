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

import com.alipay.sofa.registry.server.shared.config.ServerShareConfig;
import java.util.Collection;
import java.util.Set;

/**
 * SessionServerConfig Interface
 *
 * @author shangyu.wh
 * @version $Id: SessionServerConfig.java, v 0.1 2017-11-14 11:47 synex Exp $
 */
public interface SessionServerConfig extends ServerShareConfig {

  int getServerPort();

  int getSyncSessionPort();

  int getConsolePort();

  int getSyncSessionIOLowWaterMark();

  int getSyncSessionIOHighWaterMark();

  int getClientIOLowWaterMark();

  int getClientIOHighWaterMark();

  int getMetaServerPort();

  int getDataServerPort();

  int getDataServerNotifyPort();

  int getHttpServerPort();

  int getSchedulerHeartbeatIntervalSecs();

  int getScanSubscriberIntervalMillis();

  int getClientNodeExchangeTimeoutMillis();

  int getClientNodePushConcurrencyLevel();

  int getDataNodeExchangeTimeoutMillis();

  int getDataNodeExchangeForFetchDatumTimeoutMillis();

  int getMetaNodeExchangeTimeoutMillis();

  String getSessionServerRegion();

  Set<String> getLocalDataCenterZones();

  String getClientCell(String clientCell);

  boolean isLocalDataCenter(String dataCenter);

  String getSessionServerDataCenter();

  int getAccessDataExecutorPoolSize();

  int getAccessDataExecutorQueueSize();

  int getAccessSubDataExecutorPoolSize();

  int getAccessSubDataExecutorQueueSize();

  int getConsoleExecutorPoolSize();

  int getConsoleExecutorQueueSize();

  int getDataChangeExecutorPoolSize();

  int getDataChangeExecutorQueueSize();

  int getDataChangeDebouncingMillis();

  int getDataChangeMaxDebouncingMillis();

  int getPushTaskExecutorPoolSize();

  int getPushTaskExecutorQueueSize();

  int getPushTaskBufferBucketSize();

  int getPushTaskRetryTimes();

  int getPushDataTaskRetryFirstDelayMillis();

  int getPushDataTaskRetryIncrementDelayMillis();

  int getPushDataTaskDebouncingMillis();

  int getDataChangeFetchTaskWorkerSize();

  int getSubscriberRegisterTaskWorkerSize();

  int getWatchPushTaskWorkerSize();

  int getWatchPushTaskMaxBufferSize();

  boolean isInvalidForeverZone(String zoneId);

  boolean isInvalidIgnored(String dataId);

  int getDataNodeRetryQueueSize();

  int getDataNodeRetryTimes();

  int getDataNodeRetryBackoffMillis();

  int getDataNodeExecutorWorkerSize();

  int getDataNodeExecutorQueueSize();

  int getDataNodeMaxBatchSize();

  double getAccessLimitRate();

  int getDataClientConnNum();

  int getDataNotifyClientConnNum();

  int getSessionSchedulerPoolSize();

  int getSlotSyncPublisherMaxNum();

  Collection<String> getMetaServerAddresses();

  int getSlotSyncMaxBufferSize();

  int getSlotSyncWorkerSize();

  int getMetaNodeBufferSize();

  int getMetaNodeWorkerSize();

  int getAccessMetadataMaxBufferSize();

  int getAccessMetadataWorkerSize();

  int getCacheDigestIntervalMinutes();

  int getCacheCountIntervalSecs();

  int getCacheDatumMaxWeight();

  int getCacheDatumExpireSecs();

  int getHeartbeatCacheCheckerInitialDelaySecs();

  int getHeartbeatCacheCheckerSecs();

  int getRevisionHeartbeatInitialDelayMinutes();

  int getRevisionHeartbeatMinutes();

  int getClientManagerIntervalMillis();

  int getClientOpenIntervalSecs();

  int getPushCircuitBreakerThreshold();

  int getPushCircuitBreakerSilenceMillis();

  int getSkipPushEmptySilentMillis();

  int getClientManagerAddressIntervalMillis();

  int getWatchConfigFetchBatchSize();

  int getWatchConfigFetchIntervalMillis();

  int getWatchConfigFetchLeaseSecs();

  boolean isWatchConfigEnable();

  int getScanWatcherIntervalMillis();

  boolean isGracefulShutdown();

  int getPushAddressCircuitBreakerThreshold();

  int getPushConsecutiveSuccess();

  int getMetadataRegisterExecutorPoolSize();

  int getMetadataRegisterExecutorQueueSize();

  int getScanExecutorPoolSize();

  int getScanExecutorQueueSize();

  long getScanTimeoutMills();
}
