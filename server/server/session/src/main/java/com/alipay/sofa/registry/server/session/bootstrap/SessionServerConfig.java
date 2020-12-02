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

/**
 * SessionServerConfig Interface
 * @author shangyu.wh
 * @version $Id: SessionServerConfig.java, v 0.1 2017-11-14 11:47 synex Exp $
 */
public interface SessionServerConfig {

    int getServerPort();

    int getMetaServerPort();

    int getDataServerPort();

    int getHttpServerPort();

    int getSchedulerHeartbeatTimeout();

    int getSchedulerHeartbeatFirstDelay();

    int getSchedulerHeartbeatExpBackOffBound();

    int getSchedulerGetSessionNodeTimeout();

    int getSchedulerGetSessionNodeFirstDelay();

    int getSchedulerGetSessionNodeExpBackOffBound();

    int getSchedulerFetchDataTimeout();

    int getSchedulerFetchDataFirstDelay();

    int getSchedulerFetchDataExpBackOffBound();

    int getClientNodeExchangeTimeOut();

    int getDataNodeExchangeTimeOut();

    int getDataNodeExchangeForFetchDatumTimeOut();

    int getMetaNodeExchangeTimeOut();

    String getSessionServerRegion();

    String getClientCell(String clientCell);

    String getSessionServerDataCenter();

    int getReceivedDataMultiPushTaskRetryTimes();

    int getCancelDataTaskRetryTimes();

    long getCancelDataTaskRetryFirstDelay();

    long getCancelDataTaskRetryIncrementDelay();

    int getPublishDataTaskRetryTimes();

    long getPublishDataTaskRetryFirstDelay();

    long getPublishDataTaskRetryIncrementDelay();

    int getUnPublishDataTaskRetryTimes();

    long getUnPublishDataTaskRetryFirstDelay();

    long getUnPublishDataTaskRetryIncrementDelay();

    int getDatumSnapshotTaskRetryTimes();

    long getDatumSnapshotTaskRetryFirstDelay();

    long getDatumSnapshotTaskRetryIncrementDelay();

    int getRenewDatumTaskRetryTimes();

    int getDataChangeFetchTaskRetryTimes();

    int getSubscriberRegisterFetchRetryTimes();

    int getSessionRegisterDataServerTaskRetryTimes();

    int getSchedulerConnectMetaTimeout();

    int getSchedulerConnectMetaFirstDelay();

    int getSchedulerConnectMetaExpBackOffBound();

    int getSchedulerConnectDataTimeout();

    int getSchedulerConnectDataFirstDelay();

    int getSchedulerConnectDataExpBackOffBound();

    int getSchedulerCleanInvalidClientTimeOut();

    int getSchedulerCleanInvalidClientFirstDelay();

    int getSchedulerCleanInvalidClientBackOffBound();

    int getAccessDataExecutorMinPoolSize();

    int getAccessDataExecutorMaxPoolSize();

    int getAccessDataExecutorQueueSize();

    long getAccessDataExecutorKeepAliveTime();

    int getDataChangeExecutorMinPoolSize();

    int getDataChangeExecutorMaxPoolSize();

    int getDataChangeExecutorQueueSize();

    long getDataChangeExecutorKeepAliveTime();

    int getPushTaskExecutorMinPoolSize();

    int getPushTaskExecutorMaxPoolSize();

    int getPushTaskExecutorQueueSize();

    long getPushTaskExecutorKeepAliveTime();

    int getConnectClientExecutorMinPoolSize();

    int getConnectClientExecutorMaxPoolSize();

    int getConnectClientExecutorQueueSize();

    int getDataChangeFetchTaskMaxBufferSize();

    int getDataChangeFetchTaskWorkerSize();

    int getUserDataPushRetryWheelTicksSize();

    int getUserDataPushRetryWheelTicksDuration();

    int getUserDataPushRetryExecutorQueueSize();

    int getUserDataPushRetryExecutorThreadSize();

    int getPushDataTaskRetryFirstDelay();

    long getPushDataTaskRetryIncrementDelay();

    int getRenewDatumWheelTicksSize();

    int getRenewDatumWheelTicksDuration();

    int getRenewDatumWheelTaskDelaySec();

    int getRenewDatumWheelTaskRandomFirstDelaySec();

    String getBlacklistPubDataIdRegex();

    String getBlacklistSubDataIdRegex();

    int getNumberOfReplicas();

    boolean isStopPushSwitch();

    void setStopPushSwitch(boolean stopPushSwitch);

    boolean isBeginDataFetchTask();

    void setBeginDataFetchTask(boolean beginDataFetchTask);

    boolean isInvalidForeverZone(String zoneId);

    boolean isInvalidIgnored(String dataId);

    int getRenewAndSnapshotSilentPeriodSec();

    int getWriteDataAcceptorQueueSize();

    int getDataNodeRetryExecutorQueueSize();

    int getDataNodeRetryExecutorThreadSize();

    int getRenewDatumWheelThreadSize();

    int getRenewDatumWheelQueueSize();

    long getPushTaskConfirmWaitTimeout();

    int getPushTaskConfirmCheckWheelTicksSize();

    int getPushTaskConfirmCheckWheelTicksDuration();

    int getPushTaskConfirmCheckExecutorQueueSize();

    int getPushTaskConfirmCheckExecutorThreadSize();

    int getPublishDataExecutorMinPoolSize();

    int getPublishDataExecutorMaxPoolSize();

    int getPublishDataExecutorQueueSize();

    long getPublishDataExecutorKeepAliveTime();

    double getAccessLimitRate();

    int getDataClientConnNum();

    int getSessionSchedulerPoolSize();

    boolean isEnableSessionLoadbalancePolicy();

    int getDefaultRequestExecutorMinPoolSize();

    int getDefaultRequestExecutorMaxPoolSize();

    int getDefaultRequestExecutorQueueSize();

    long getDefaultRequestExecutorKeepAliveTime();
}