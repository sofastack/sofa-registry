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
package com.alipay.sofa.registry.server.data.bootstrap;

/**
 * @author xiaojian.xj
 * @version : MultiClusterDataServerConfig.java, v 0.1 2022年05月09日 17:44 xiaojian.xj Exp $
 */
public interface MultiClusterDataServerConfig {

  int getSyncRemoteSlotLeaderIntervalSecs();

  int getSyncRemoteSlotLeaderTimeoutMillis();

  int getSyncRemoteSlotLeaderPort();

  int getSyncRemoteSlotLeaderConnNum();

  int getRemoteSyncSlotLeaderExecutorThreadSize();

  int getRemoteSyncSlotLeaderExecutorQueueSize();

  int getSyncSlotLowWaterMark();

  int getSyncSlotHighWaterMark();

  int getRemoteSlotSyncRequestExecutorMinPoolSize();

  int getRemoteSlotSyncRequestExecutorMaxPoolSize();

  int getRemoteSlotSyncRequestExecutorQueueSize();

  int getRemoteDataChangeExecutorThreadSize();

  int getRemoteDataChangeExecutorQueueSize();

  int getRemoteSyncDataIdExecutorThreadSize();

  int getRemoteSyncDataIdExecutorQueueSize();

  int getMultiClusterConfigReloadMillis();
}
