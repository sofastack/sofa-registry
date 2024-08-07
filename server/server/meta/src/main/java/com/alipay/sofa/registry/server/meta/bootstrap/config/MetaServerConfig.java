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
package com.alipay.sofa.registry.server.meta.bootstrap.config;

import java.util.Set;

/**
 * @author shangyu.wh
 * @version $Id: MetaServerConfig.java, v 0.1 2018-01-16 10:58 shangyu.wh Exp $
 */
public interface MetaServerConfig {
  String getLocalDataCenter();

  boolean isLocalDataCenter(String dataCenter);

  Set<String> getLocalDataCenterZones();

  int getSessionServerPort();

  int getDataServerPort();

  int getMetaServerPort();

  int getHttpServerPort();

  int getExpireCheckIntervalMillis();

  int getDataNodeExchangeTimeoutMillis();

  int getSessionNodeExchangeTimeoutMillis();

  int getMetaNodeExchangeTimeoutMillis();

  int getDefaultRequestExecutorMinSize();

  int getDefaultRequestExecutorMaxSize();

  int getDefaultRequestExecutorQueueSize();

  long getMetaLeaderWarmupMillis();

  int getSchedulerHeartbeatIntervalSecs();

  long getDataReplicateMaxGapMillis();

  int getRevisionGcSilenceHour();

  int getRevisionGcInitialDelaySecs();

  int getRevisionGcSecs();

  int getMetaSchedulerPoolSize();

  int getDataNodeProtectionNum();

  int getClientManagerRefreshMillis();

  int getClientManagerWatchMillis();

  int getClientManagerRefreshLimit();

  int getClientManagerCleanSecs();

  int getClientManagerExpireDays();

  int getAppRevisionMaxRemove();

  int getInterfaceMaxRemove();

  int getAppRevisionCountAlarmThreshold();
}
