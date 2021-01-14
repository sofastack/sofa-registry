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

import java.util.Set;

/**
 * SessionServerConfig Interface
 * @author shangyu.wh
 * @version $Id: SessionServerConfig.java, v 0.1 2017-11-14 11:47 synex Exp $
 */
public interface SessionServerConfig {

    int getServerPort();

    int getSyncSessionPort();

    int getMetaServerPort();

    int getDataServerPort();

    int getHttpServerPort();

    int getSchedulerHeartbeatIntervalSec();

    int getSchedulerFetchDataVersionIntervalMs();

    int getClientNodeExchangeTimeOut();

    int getDataNodeExchangeTimeOut();

    int getDataNodeExchangeForFetchDatumTimeOut();

    int getMetaNodeExchangeTimeOut();

    String getSessionServerRegion();

    String getClientCell(String clientCell);

    String getSessionServerDataCenter();

    int getCancelDataTaskRetryTimes();

    long getCancelDataTaskRetryFirstDelay();

    long getCancelDataTaskRetryIncrementDelay();

    int getPublishDataTaskRetryTimes();

    long getPublishDataTaskRetryFirstDelay();

    long getPublishDataTaskRetryIncrementDelay();

    int getUnPublishDataTaskRetryTimes();

    long getUnPublishDataTaskRetryFirstDelay();

    long getUnPublishDataTaskRetryIncrementDelay();

    int getSubscriberRegisterFetchRetryTimes();

    int getAccessDataExecutorMinPoolSize();

    int getAccessDataExecutorMaxPoolSize();

    int getAccessDataExecutorQueueSize();

    long getAccessDataExecutorKeepAliveTime();

    int getDataChangeExecutorMinPoolSize();

    int getDataChangeExecutorMaxPoolSize();

    int getDataChangeExecutorQueueSize();

    long getDataChangeExecutorKeepAliveTime();

    int getPushTaskExecutorPoolSize();

    int getPushTaskExecutorQueueSize();

    int getPushTaskRetryTimes();

    int getPushDataTaskRetryFirstDelayMillis();

    int getPushDataTaskRetryIncrementDelayMillis();

    int getPushDataTaskDebouncingMillis();

    int getConnectClientExecutorMinPoolSize();

    int getConnectClientExecutorMaxPoolSize();

    int getConnectClientExecutorQueueSize();

    int getDataChangeFetchTaskMaxBufferSize();

    int getDataChangeFetchTaskWorkerSize();

    String getBlacklistPubDataIdRegex();

    String getBlacklistSubDataIdRegex();

    boolean isStopPushSwitch();

    void setStopPushSwitch(boolean stopPushSwitch);

    boolean isInvalidForeverZone(String zoneId);

    boolean isInvalidIgnored(String dataId);

    int getDataNodeRetryExecutorQueueSize();

    int getDataNodeRetryExecutorThreadSize();

    int getPublishDataExecutorMinPoolSize();

    int getPublishDataExecutorMaxPoolSize();

    int getPublishDataExecutorQueueSize();

    long getPublishDataExecutorKeepAliveTime();

    double getAccessLimitRate();

    int getDataClientConnNum();

    int getSessionSchedulerPoolSize();

    int getSlotSyncPublisherMaxNum();

    Set<String> getMetaServerIpAddresses();

    boolean isEnableSessionLoadbalancePolicy();

    int getSlotSyncMaxBufferSize();

    int getSlotSyncWorkerSize();

    int getCacheDigestFixDelaySecs();
}