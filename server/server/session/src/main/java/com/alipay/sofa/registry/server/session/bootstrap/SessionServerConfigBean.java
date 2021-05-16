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

import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.util.OsUtils;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The type Session server config bean.
 *
 * @author shangyu.wh
 * @version $Id : SessionServerConfigBean.java, v 0.1 2017-11-14 11:49 synex Exp $
 */
@ConfigurationProperties(prefix = SessionServerConfigBean.PREFIX)
public class SessionServerConfigBean implements SessionServerConfig {

  /** The constant PREFIX. */
  public static final String PREFIX = "session.server";

  private int serverPort = 9600;

  private int syncSessionPort = 9602;

  private int syncSessionIOLowWaterMark = 1024 * 128;

  private int syncSessionIOHighWaterMark = 1024 * 256;

  private int metaServerPort = 9610;

  private int dataServerPort = 9620;

  private int dataServerNotifyPort = 9623;

  private int httpServerPort = 9603;

  private int schedulerHeartbeatIntervalSecs = 1;

  private int subscriberRegisterFetchRetryTimes = 3;

  private int accessDataExecutorMinPoolSize = OsUtils.getCpuCount() * 10;

  private int accessDataExecutorMaxPoolSize = OsUtils.getCpuCount() * 20;

  private int accessDataExecutorQueueSize = 10000;

  private long accessDataExecutorKeepAliveTime = 60;

  private int dataChangeExecutorMinPoolSize = OsUtils.getCpuCount() * 2;

  private int dataChangeExecutorMaxPoolSize = OsUtils.getCpuCount() * 3;

  private int dataChangeExecutorQueueSize = 20000;

  private long dataChangeExecutorKeepAliveTime = 60;

  private int connectClientExecutorMinPoolSize = OsUtils.getCpuCount();

  private int connectClientExecutorMaxPoolSize = OsUtils.getCpuCount();

  private int connectClientExecutorQueueSize = 2000;

  private int dataChangeFetchTaskMaxBufferSize = 30000;

  private int dataChangeFetchTaskWorkerSize = OsUtils.getCpuCount() * 5;

  private int subscriberRegisterTaskMaxBufferSize = 500000;

  private int subscriberRegisterTaskWorkerSize = OsUtils.getCpuCount() * 8;

  private int dataChangeDebouncingMillis = 1000;
  private int dataChangeMaxDebouncingMillis = 3000;

  private int slotSyncMaxBufferSize = 5000;

  private int slotSyncWorkerSize = OsUtils.getCpuCount() * 4;

  private int metaNodeBufferSize = 2000;

  private int metaNodeWorkerSize = OsUtils.getCpuCount() * 4;

  private int accessMetadataMaxBufferSize = 10000;

  private int accessMetadataWorkerSize = OsUtils.getCpuCount() * 4;

  private int clientNodeExchangeTimeoutMillis = 2000;

  private int dataNodeExchangeTimeoutMillis = 3000;

  private int dataNodeExchangeForFetchDatumTimeoutMillis = 5000;

  private int metaNodeExchangeTimeoutMillis = 3000;

  private int pushTaskExecutorPoolSize = OsUtils.getCpuCount() * 3;

  private int pushTaskExecutorQueueSize = pushTaskExecutorPoolSize * 3000;

  private int pushDataTaskRetryFirstDelayMillis = 500;

  private int pushDataTaskRetryIncrementDelayMillis = 500;

  private int pushDataTaskDebouncingMillis = 500;

  private int pushTaskRetryTimes = 3;

  private int dataNodeExecutorWorkerSize = OsUtils.getCpuCount() * 8;

  private int dataNodeExecutorQueueSize = 20000;

  private int dataNodeRetryBackoffMillis = 1000;

  private int dataNodeRetryTimes = 5;

  private int dataNodeRetryQueueSize = 1000;

  private int dataNodeMaxBatchSize = 100;

  private int schedulerScanVersionIntervalMillis = 1000 * 5;

  private double accessLimitRate = 100000.0;

  private String sessionServerRegion;

  private String sessionServerDataCenter;

  private volatile boolean stopPushSwitch = false;

  // begin config for enterprise version

  /** forever close push zone，such as:RZBETA */
  private String invalidForeverZones = "";
  /** config regex,exception to the rule of forever close push zone */
  private String invalidIgnoreDataidRegex = "";

  private volatile Set<String> invalidForeverZonesSet;

  private volatile Pattern invalidIgnoreDataIdPattern = null;

  private String blacklistPubDataIdRegex = "";

  private String blacklistSubDataIdRegex = "";

  private int dataClientConnNum = 10;
  private int dataNotifyClientConnNum = 2;
  private int sessionSchedulerPoolSize = OsUtils.getCpuCount();

  private int slotSyncPublisherMaxNum = 512;

  private int cacheDigestIntervalMinutes = 15;

  private int cacheCountIntervalSecs = 30;

  private int cacheDatumMaxWeight = 1000*1000*1000;

  private int cacheDatumExpireSecs = 60 * 3;

  // metadata config start

  private int heartbeatCacheCheckerInitialDelaySecs = 60;

  private int heartbeatCacheCheckerSecs = 60;

  private int revisionHeartbeatInitialDelayMinutes = 10;

  private int revisionHeartbeatMinutes = 10;

  // metadata config end

  // end config for enterprise version

  private CommonConfig commonConfig;

  private volatile Collection<String> metaAddresses;

  /**
   * constructor
   *
   * @param commonConfig
   */
  public SessionServerConfigBean(CommonConfig commonConfig) {
    this.commonConfig = commonConfig;
  }

  /**
   * Getter method for property <tt>serverPort</tt>.
   *
   * @return property value of serverPort
   */
  @Override
  public int getServerPort() {
    return serverPort;
  }

  @Override
  public int getSyncSessionPort() {
    return syncSessionPort;
  }

  /**
   * Setter method for property <tt>serverPort</tt>.
   *
   * @param serverPort value to be assigned to property serverPort
   */
  public void setServerPort(int serverPort) {
    this.serverPort = serverPort;
  }

  /**
   * Getter method for property <tt>schedulerHeartbeatIntervalSec</tt>.
   *
   * @return property value of schedulerHeartbeatIntervalSec
   */
  @Override
  public int getSchedulerHeartbeatIntervalSecs() {
    return schedulerHeartbeatIntervalSecs;
  }

  /**
   * Setter method for property <tt>schedulerHeartbeatIntervalSec</tt>.
   *
   * @param schedulerHeartbeatIntervalSec value to be assigned to property
   *     schedulerHeartbeatIntervalSec
   */
  public void setSchedulerHeartbeatTimeout(int schedulerHeartbeatIntervalSec) {
    this.schedulerHeartbeatIntervalSecs = schedulerHeartbeatIntervalSec;
  }

  @Override
  public int getPushTaskRetryTimes() {
    return pushTaskRetryTimes;
  }

  public void setPushTaskRetryTimes(int pushTaskRetryTimes) {
    this.pushTaskRetryTimes = pushTaskRetryTimes;
  }

  /**
   * Getter method for property <tt>subscriberRegisterFetchRetryTimes</tt>.
   *
   * @return property value of subscriberRegisterFetchRetryTimes
   */
  @Override
  public int getSubscriberRegisterFetchRetryTimes() {
    return subscriberRegisterFetchRetryTimes;
  }

  /**
   * Setter method for property <tt>subscriberRegisterFetchRetryTimes</tt>.
   *
   * @param subscriberRegisterFetchRetryTimes value to be assigned to property
   *     subscriberRegisterFetchRetryTimes
   */
  public void setSubscriberRegisterFetchRetryTimes(int subscriberRegisterFetchRetryTimes) {
    this.subscriberRegisterFetchRetryTimes = subscriberRegisterFetchRetryTimes;
  }

  /**
   * Getter method for property <tt>clientNodeExchangeTimeOut</tt>.
   *
   * @return property value of clientNodeExchangeTimeOut
   */
  @Override
  public int getClientNodeExchangeTimeoutMillis() {
    return clientNodeExchangeTimeoutMillis;
  }

  /**
   * Setter method for property <tt>clientNodeExchangeTimeOut</tt>.
   *
   * @param clientNodeExchangeTimeoutMillis value to be assigned to property
   *     clientNodeExchangeTimeOut
   */
  public void setClientNodeExchangeTimeoutMillis(int clientNodeExchangeTimeoutMillis) {
    this.clientNodeExchangeTimeoutMillis = clientNodeExchangeTimeoutMillis;
  }

  /**
   * Getter method for property <tt>dataNodeExchangeTimeOut</tt>.
   *
   * @return property value of dataNodeExchangeTimeOut
   */
  @Override
  public int getDataNodeExchangeTimeoutMillis() {
    return dataNodeExchangeTimeoutMillis;
  }

  /**
   * Setter method for property <tt>dataNodeExchangeTimeOut</tt>.
   *
   * @param dataNodeExchangeTimeoutMillis value to be assigned to property dataNodeExchangeTimeOut
   */
  public void setDataNodeExchangeTimeoutMillis(int dataNodeExchangeTimeoutMillis) {
    this.dataNodeExchangeTimeoutMillis = dataNodeExchangeTimeoutMillis;
  }

  /**
   * Getter method for property <tt>metaServerPort</tt>.
   *
   * @return property value of metaServerPort
   */
  @Override
  public int getMetaServerPort() {
    return metaServerPort;
  }

  /**
   * Setter method for property <tt>metaServerPort</tt>.
   *
   * @param metaServerPort value to be assigned to property metaServerPort
   */
  public void setMetaServerPort(int metaServerPort) {
    this.metaServerPort = metaServerPort;
  }

  @Override
  public String getSessionServerRegion() {
    if (commonConfig != null) {
      String region = commonConfig.getLocalRegion();
      if (region != null && !region.isEmpty()) {
        return commonConfig.getLocalRegion().toUpperCase();
      }
    }

    if (sessionServerRegion != null) {
      sessionServerRegion = sessionServerRegion.toUpperCase();
    }
    return sessionServerRegion;
  }

  @Override
  public String getClientCell(String subscriberCell) {
    return this.getSessionServerRegion();
  }

  /**
   * Setter method for property <tt>sessionServerRegion</tt>.
   *
   * @param sessionServerRegion value to be assigned to property sessionServerRegion
   */
  public void setSessionServerRegion(String sessionServerRegion) {
    if (sessionServerRegion != null) {
      sessionServerRegion = sessionServerRegion.toUpperCase();
    }
    this.sessionServerRegion = sessionServerRegion;
  }

  @Override
  public String getSessionServerDataCenter() {
    if (commonConfig != null) {
      String dataCenter = commonConfig.getLocalDataCenter();
      if (dataCenter != null && !dataCenter.isEmpty()) {
        return commonConfig.getLocalDataCenter();
      }
    }

    return sessionServerDataCenter;
  }

  /**
   * Setter method for property <tt>sessionServerDataCenter</tt>.
   *
   * @param sessionServerDataCenter value to be assigned to property sessionServerDataCenter
   */
  public void setSessionServerDataCenter(String sessionServerDataCenter) {
    this.sessionServerDataCenter = sessionServerDataCenter;
  }

  /**
   * Getter method for property <tt>metaNodeExchangeTimeOut</tt>.
   *
   * @return property value of metaNodeExchangeTimeOut
   */
  @Override
  public int getMetaNodeExchangeTimeoutMillis() {
    return metaNodeExchangeTimeoutMillis;
  }

  /**
   * Setter method for property <tt>metaNodeExchangeTimeOut</tt>.
   *
   * @param metaNodeExchangeTimeoutMillis value to be assigned to property metaNodeExchangeTimeOut
   */
  public void setMetaNodeExchangeTimeoutMillis(int metaNodeExchangeTimeoutMillis) {
    this.metaNodeExchangeTimeoutMillis = metaNodeExchangeTimeoutMillis;
  }

  /**
   * Getter method for property <tt>dataServerPort</tt>.
   *
   * @return property value of dataServerPort
   */
  @Override
  public int getDataServerPort() {
    return dataServerPort;
  }

  /**
   * Setter method for property <tt>dataServerPort</tt>.
   *
   * @param dataServerPort value to be assigned to property dataServerPort
   */
  public void setDataServerPort(int dataServerPort) {
    this.dataServerPort = dataServerPort;
  }

  @Override
  public int getDataServerNotifyPort() {
    return dataServerNotifyPort;
  }

  public void setDataServerNotifyPort(int dataServerNotifyPort) {
    this.dataServerNotifyPort = dataServerNotifyPort;
  }

  /**
   * Getter method for property <tt>httpServerPort</tt>.
   *
   * @return property value of httpServerPort
   */
  @Override
  public int getHttpServerPort() {
    return httpServerPort;
  }

  /**
   * Setter method for property <tt>httpServerPort</tt>.
   *
   * @param httpServerPort value to be assigned to property httpServerPort
   */
  public void setHttpServerPort(int httpServerPort) {
    this.httpServerPort = httpServerPort;
  }

  /**
   * Getter method for property <tt>stopPushSwitch</tt>.
   *
   * @return property value of stopPushSwitch
   */
  @Override
  public boolean isStopPushSwitch() {
    return stopPushSwitch;
  }

  /**
   * Setter method for property <tt>stopPushSwitch</tt>.
   *
   * @param stopPushSwitch value to be assigned to property stopPushSwitch
   */
  @Override
  public void setStopPushSwitch(boolean stopPushSwitch) {
    this.stopPushSwitch = stopPushSwitch;
  }

  public String getInvalidForeverZones() {
    return invalidForeverZones;
  }

  /**
   * Setter method for property <tt>invalidForeverZones</tt>.
   *
   * @param invalidForeverZones value to be assigned to property invalidForeverZones
   */
  public void setInvalidForeverZones(String invalidForeverZones) {
    this.invalidForeverZones = invalidForeverZones;
  }

  public String getInvalidIgnoreDataidRegex() {
    return invalidIgnoreDataidRegex;
  }

  /**
   * Setter method for property <tt>invalidIgnoreDataidRegex</tt>.
   *
   * @param invalidIgnoreDataidRegex value to be assigned to property invalidIgnoreDataidRegex
   */
  public void setInvalidIgnoreDataidRegex(String invalidIgnoreDataidRegex) {
    this.invalidIgnoreDataidRegex = invalidIgnoreDataidRegex;
  }

  @Override
  public int getAccessDataExecutorMinPoolSize() {
    return accessDataExecutorMinPoolSize;
  }

  /**
   * Setter method for property <tt>accessDataExecutorMinPoolSize</tt>.
   *
   * @param accessDataExecutorMinPoolSize value to be assigned to property
   *     accessDataExecutorMinPoolSize
   */
  public void setAccessDataExecutorMinPoolSize(int accessDataExecutorMinPoolSize) {
    this.accessDataExecutorMinPoolSize = accessDataExecutorMinPoolSize;
  }

  @Override
  public int getAccessDataExecutorMaxPoolSize() {
    return accessDataExecutorMaxPoolSize;
  }

  /**
   * Setter method for property <tt>accessDataExecutorMaxPoolSize</tt>.
   *
   * @param accessDataExecutorMaxPoolSize value to be assigned to property
   *     accessDataExecutorMaxPoolSize
   */
  public void setAccessDataExecutorMaxPoolSize(int accessDataExecutorMaxPoolSize) {
    this.accessDataExecutorMaxPoolSize = accessDataExecutorMaxPoolSize;
  }

  @Override
  public int getAccessDataExecutorQueueSize() {
    return accessDataExecutorQueueSize;
  }

  /**
   * Setter method for property <tt>accessDataExecutorQueueSize</tt>.
   *
   * @param accessDataExecutorQueueSize value to be assigned to property accessDataExecutorQueueSize
   */
  public void setAccessDataExecutorQueueSize(int accessDataExecutorQueueSize) {
    this.accessDataExecutorQueueSize = accessDataExecutorQueueSize;
  }

  @Override
  public long getAccessDataExecutorKeepAliveTime() {
    return accessDataExecutorKeepAliveTime;
  }

  /**
   * Setter method for property <tt>accessDataExecutorKeepAliveTime</tt>.
   *
   * @param accessDataExecutorKeepAliveTime value to be assigned to property
   *     accessDataExecutorKeepAliveTime
   */
  public void setAccessDataExecutorKeepAliveTime(long accessDataExecutorKeepAliveTime) {
    this.accessDataExecutorKeepAliveTime = accessDataExecutorKeepAliveTime;
  }

  /**
   * Getter method for property <tt>dataChangeExecutorMinPoolSize</tt>.
   *
   * @return property value of dataChangeExecutorMinPoolSize
   */
  @Override
  public int getDataChangeExecutorMinPoolSize() {
    return dataChangeExecutorMinPoolSize;
  }

  /**
   * Getter method for property <tt>dataChangeExecutorMaxPoolSize</tt>.
   *
   * @return property value of dataChangeExecutorMaxPoolSize
   */
  @Override
  public int getDataChangeExecutorMaxPoolSize() {
    return dataChangeExecutorMaxPoolSize;
  }

  /**
   * Getter method for property <tt>dataChangeExecutorQueueSize</tt>.
   *
   * @return property value of dataChangeExecutorQueueSize
   */
  @Override
  public int getDataChangeExecutorQueueSize() {
    return dataChangeExecutorQueueSize;
  }

  /**
   * Getter method for property <tt>dataChangeExecutorKeepAliveTime</tt>.
   *
   * @return property value of dataChangeExecutorKeepAliveTime
   */
  @Override
  public long getDataChangeExecutorKeepAliveTime() {
    return dataChangeExecutorKeepAliveTime;
  }

  @Override
  public int getDataChangeDebouncingMillis() {
    return dataChangeDebouncingMillis;
  }

  public void setDataChangeDebouncingMillis(int dataChangeDebouncingMillis) {
    this.dataChangeDebouncingMillis = dataChangeDebouncingMillis;
  }

  @Override
  public int getDataChangeMaxDebouncingMillis() {
    return dataChangeMaxDebouncingMillis;
  }

  public void setDataChangeMaxDebouncingMillis(int dataChangeMaxDebouncingMillis) {
    this.dataChangeMaxDebouncingMillis = dataChangeMaxDebouncingMillis;
  }

  /**
   * Setter method for property <tt>dataChangeExecutorMinPoolSize</tt>.
   *
   * @param dataChangeExecutorMinPoolSize value to be assigned to property
   *     dataChangeExecutorMinPoolSize
   */
  public void setDataChangeExecutorMinPoolSize(int dataChangeExecutorMinPoolSize) {
    this.dataChangeExecutorMinPoolSize = dataChangeExecutorMinPoolSize;
  }

  /**
   * Setter method for property <tt>dataChangeExecutorMaxPoolSize</tt>.
   *
   * @param dataChangeExecutorMaxPoolSize value to be assigned to property
   *     dataChangeExecutorMaxPoolSize
   */
  public void setDataChangeExecutorMaxPoolSize(int dataChangeExecutorMaxPoolSize) {
    this.dataChangeExecutorMaxPoolSize = dataChangeExecutorMaxPoolSize;
  }

  /**
   * Setter method for property <tt>dataChangeExecutorQueueSize</tt>.
   *
   * @param dataChangeExecutorQueueSize value to be assigned to property dataChangeExecutorQueueSize
   */
  public void setDataChangeExecutorQueueSize(int dataChangeExecutorQueueSize) {
    this.dataChangeExecutorQueueSize = dataChangeExecutorQueueSize;
  }

  /**
   * Setter method for property <tt>dataChangeExecutorKeepAliveTime</tt>.
   *
   * @param dataChangeExecutorKeepAliveTime value to be assigned to property
   *     dataChangeExecutorKeepAliveTime
   */
  public void setDataChangeExecutorKeepAliveTime(long dataChangeExecutorKeepAliveTime) {
    this.dataChangeExecutorKeepAliveTime = dataChangeExecutorKeepAliveTime;
  }

  /**
   * Getter method for property <tt>pushTaskExecutorMaxPoolSize</tt>.
   *
   * @return property value of pushTaskExecutorMaxPoolSize
   */
  @Override
  public int getPushTaskExecutorPoolSize() {
    return pushTaskExecutorPoolSize;
  }

  /**
   * Getter method for property <tt>pushTaskExecutorQueueSize</tt>.
   *
   * @return property value of pushTaskExecutorQueueSize
   */
  @Override
  public int getPushTaskExecutorQueueSize() {
    return pushTaskExecutorQueueSize;
  }

  @Override
  public int getPushDataTaskDebouncingMillis() {
    return pushDataTaskDebouncingMillis;
  }

  public void setPushDataTaskDebouncingMillis(int pushDataTaskDebouncingMillis) {
    this.pushDataTaskDebouncingMillis = pushDataTaskDebouncingMillis;
  }

  /**
   * Setter method for property <tt>pushTaskExecutorPoolSize</tt>.
   *
   * @param pushTaskExecutorPoolSize value to be assigned to property pushTaskExecutorPoolSize
   */
  public void setPushTaskExecutorPoolSize(int pushTaskExecutorPoolSize) {
    this.pushTaskExecutorPoolSize = pushTaskExecutorPoolSize;
  }

  /**
   * Setter method for property <tt>pushTaskExecutorQueueSize</tt>.
   *
   * @param pushTaskExecutorQueueSize value to be assigned to property pushTaskExecutorQueueSize
   */
  public void setPushTaskExecutorQueueSize(int pushTaskExecutorQueueSize) {
    this.pushTaskExecutorQueueSize = pushTaskExecutorQueueSize;
  }

  /**
   * Getter method for property <tt>connectClientExecutorMinPoolSize</tt>.
   *
   * @return property value of connectClientExecutorMinPoolSize
   */
  public int getConnectClientExecutorMinPoolSize() {
    return connectClientExecutorMinPoolSize;
  }

  /**
   * Getter method for property <tt>connectClientExecutorMaxPoolSize</tt>.
   *
   * @return property value of connectClientExecutorMaxPoolSize
   */
  public int getConnectClientExecutorMaxPoolSize() {
    return connectClientExecutorMaxPoolSize;
  }

  /**
   * Getter method for property <tt>connectClientExecutorQueueSize</tt>.
   *
   * @return property value of connectClientExecutorQueueSize
   */
  public int getConnectClientExecutorQueueSize() {
    return connectClientExecutorQueueSize;
  }

  /**
   * Setter method for property <tt>connectClientExecutorMinPoolSize</tt>.
   *
   * @param connectClientExecutorMinPoolSize value to be assigned to property
   *     connectClientExecutorMinPoolSize
   */
  public void setConnectClientExecutorMinPoolSize(int connectClientExecutorMinPoolSize) {
    this.connectClientExecutorMinPoolSize = connectClientExecutorMinPoolSize;
  }

  /**
   * Setter method for property <tt>connectClientExecutorMaxPoolSize</tt>.
   *
   * @param connectClientExecutorMaxPoolSize value to be assigned to property
   *     connectClientExecutorMaxPoolSize
   */
  public void setConnectClientExecutorMaxPoolSize(int connectClientExecutorMaxPoolSize) {
    this.connectClientExecutorMaxPoolSize = connectClientExecutorMaxPoolSize;
  }

  /**
   * Setter method for property <tt>connectClientExecutorQueueSize</tt>.
   *
   * @param connectClientExecutorQueueSize value to be assigned to property
   *     connectClientExecutorQueueSize
   */
  public void setConnectClientExecutorQueueSize(int connectClientExecutorQueueSize) {
    this.connectClientExecutorQueueSize = connectClientExecutorQueueSize;
  }

  /**
   * Getter method for property <tt>dataChangeFetchTaskMaxBufferSize</tt>.
   *
   * @return property value of dataChangeFetchTaskMaxBufferSize
   */
  @Override
  public int getDataChangeFetchTaskMaxBufferSize() {
    return dataChangeFetchTaskMaxBufferSize;
  }

  /**
   * Setter method for property <tt>dataChangeFetchTaskMaxBufferSize</tt>.
   *
   * @param dataChangeFetchTaskMaxBufferSize value to be assigned to property
   *     dataChangeFetchTaskMaxBufferSize
   */
  public void setDataChangeFetchTaskMaxBufferSize(int dataChangeFetchTaskMaxBufferSize) {
    this.dataChangeFetchTaskMaxBufferSize = dataChangeFetchTaskMaxBufferSize;
  }

  /**
   * Getter method for property <tt>dataChangeFetchTaskWorkerSize</tt>.
   *
   * @return property value of dataChangeFetchTaskWorkerSize
   */
  @Override
  public int getDataChangeFetchTaskWorkerSize() {
    return dataChangeFetchTaskWorkerSize;
  }

  /**
   * Setter method for property <tt>dataChangeFetchTaskWorkerSize</tt>.
   *
   * @param dataChangeFetchTaskWorkerSize value to be assigned to property
   *     dataChangeFetchTaskWorkerSize
   */
  public void setDataChangeFetchTaskWorkerSize(int dataChangeFetchTaskWorkerSize) {
    this.dataChangeFetchTaskWorkerSize = dataChangeFetchTaskWorkerSize;
  }

  /**
   * Getter method for property <tt>pushDataTaskRetryFirstDelayMillis</tt>.
   *
   * @return property value of pushDataTaskRetryFirstDelayMillis
   */
  @Override
  public int getPushDataTaskRetryFirstDelayMillis() {
    return pushDataTaskRetryFirstDelayMillis;
  }

  /**
   * Setter method for property <tt>pushDataTaskRetryFirstDelayMillis</tt>.
   *
   * @param pushDataTaskRetryFirstDelayMillis value to be assigned to property
   *     pushDataTaskRetryFirstDelayMillis
   */
  public void setPushDataTaskRetryFirstDelayMillis(int pushDataTaskRetryFirstDelayMillis) {
    this.pushDataTaskRetryFirstDelayMillis = pushDataTaskRetryFirstDelayMillis;
  }

  /**
   * Getter method for property <tt>pushDataTaskRetryIncrementDelayMillis</tt>.
   *
   * @return property value of pushDataTaskRetryIncrementDelayMillis
   */
  @Override
  public int getPushDataTaskRetryIncrementDelayMillis() {
    return pushDataTaskRetryIncrementDelayMillis;
  }

  /**
   * Setter method for property <tt>pushDataTaskRetryIncrementDelayMillis</tt>.
   *
   * @param pushDataTaskRetryIncrementDelayMillis value to be assigned to property
   *     pushDataTaskRetryIncrementDelay
   */
  public void setPushDataTaskRetryIncrementDelayMillis(int pushDataTaskRetryIncrementDelayMillis) {
    this.pushDataTaskRetryIncrementDelayMillis = pushDataTaskRetryIncrementDelayMillis;
  }

  /**
   * Getter method for property <tt>blacklistPubDataIdRegex</tt>.
   *
   * @return property value of blacklistPubDataIdRegex
   */
  public String getBlacklistPubDataIdRegex() {
    return blacklistPubDataIdRegex;
  }

  /**
   * Getter method for property <tt>blacklistSubDataIdRegex</tt>.
   *
   * @return property value of blacklistSubDataIdRegex
   */
  public String getBlacklistSubDataIdRegex() {
    return blacklistSubDataIdRegex;
  }

  /**
   * Setter method for property <tt>blacklistPubDataIdRegex</tt>.
   *
   * @param blacklistPubDataIdRegex value to be assigned to property blacklistPubDataIdRegex
   */
  public void setBlacklistPubDataIdRegex(String blacklistPubDataIdRegex) {
    this.blacklistPubDataIdRegex = blacklistPubDataIdRegex;
  }

  /**
   * Setter method for property <tt>blacklistSubDataIdRegex</tt>.
   *
   * @param blacklistSubDataIdRegex value to be assigned to property blacklistSubDataIdRegex
   */
  public void setBlacklistSubDataIdRegex(String blacklistSubDataIdRegex) {
    this.blacklistSubDataIdRegex = blacklistSubDataIdRegex;
  }

  /**
   * Getter method for property <tt>accessLimitRate</tt>.
   *
   * @return property value of accessLimitRate
   */
  public double getAccessLimitRate() {
    return accessLimitRate;
  }

  /**
   * Setter method for property <tt>accessLimitRate</tt>.
   *
   * @param accessLimitRate value to be assigned to property accessLimitRate
   */
  public void setAccessLimitRate(double accessLimitRate) {
    this.accessLimitRate = accessLimitRate;
  }

  /**
   * Getter method for property <tt>dataClientConnNum</tt>.
   *
   * @return property value of dataClientConnNum
   */
  @Override
  public int getDataClientConnNum() {
    return dataClientConnNum;
  }

  /**
   * Setter method for property <tt>dataClientConnNum </tt>.
   *
   * @param dataClientConnNum value to be assigned to property dataClientConnNum
   */
  public void setDataClientConnNum(int dataClientConnNum) {
    this.dataClientConnNum = dataClientConnNum;
  }

  @Override
  public int getDataNotifyClientConnNum() {
    return dataNotifyClientConnNum;
  }

  public void setDataNotifyClientConnNum(int dataNotifyClientConnNum) {
    this.dataNotifyClientConnNum = dataNotifyClientConnNum;
  }

  @Override
  public boolean isInvalidForeverZone(String zoneId) {
    if (invalidForeverZonesSet == null) {
      String[] zoneNameArr = getInvalidForeverZones().split(";");
      Set<String> set = new HashSet<>();
      for (String str : zoneNameArr) {
        str = str.trim();
        if (str.length() != 0) {
          set.add(str);
        }
      }
      invalidForeverZonesSet = set;
    }
    return invalidForeverZonesSet.contains(zoneId);
  }

  @Override
  public boolean isInvalidIgnored(String dataId) {

    String invalidIgnoreDataidRegex = getInvalidIgnoreDataidRegex();
    if (null != invalidIgnoreDataidRegex && !invalidIgnoreDataidRegex.isEmpty()) {
      invalidIgnoreDataIdPattern = Pattern.compile(invalidIgnoreDataidRegex);
    }

    return null != invalidIgnoreDataIdPattern && invalidIgnoreDataIdPattern.matcher(dataId).find();
  }

  /**
   * Getter method for property <tt>sessionSchedulerPoolSize</tt>.
   *
   * @return property value of sessionSchedulerPoolSize
   */
  @Override
  public int getSessionSchedulerPoolSize() {
    return sessionSchedulerPoolSize;
  }

  /**
   * Setter method for property <tt>sessionSchedulerPoolSize </tt>.
   *
   * @param sessionSchedulerPoolSize value to be assigned to property sessionSchedulerPoolSize
   */
  public void setSessionSchedulerPoolSize(int sessionSchedulerPoolSize) {
    this.sessionSchedulerPoolSize = sessionSchedulerPoolSize;
  }

  /**
   * Getter method for property <tt>dataNodeExchangeForFetchDatumTimeOut</tt>.
   *
   * @return property value of dataNodeExchangeForFetchDatumTimeOut
   */
  public int getDataNodeExchangeForFetchDatumTimeoutMillis() {
    return dataNodeExchangeForFetchDatumTimeoutMillis;
  }

  /**
   * Setter method for property <tt>dataNodeExchangeForFetchDatumTimeOut </tt>.
   *
   * @param dataNodeExchangeForFetchDatumTimeoutMillis value to be assigned to property
   *     dataNodeExchangeForFetchDatumTimeOut
   */
  public void setDataNodeExchangeForFetchDatumTimeoutMillis(
      int dataNodeExchangeForFetchDatumTimeoutMillis) {
    this.dataNodeExchangeForFetchDatumTimeoutMillis = dataNodeExchangeForFetchDatumTimeoutMillis;
  }

  /**
   * Getter method for property <tt>slotSyncPublisherMaxNum</tt>.
   *
   * @return property value of slotSyncPublisherMaxNum
   */
  @Override
  public int getSlotSyncPublisherMaxNum() {
    return slotSyncPublisherMaxNum;
  }

  @Override
  public Collection<String> getMetaServerAddresses() {
    final Collection<String> addresses = metaAddresses;
    if (addresses != null) {
      return addresses;
    }
    metaAddresses =
        ServerEnv.getMetaAddresses(commonConfig.getMetaNode(), commonConfig.getLocalDataCenter());
    return metaAddresses;
  }

  /**
   * Setter method for property <tt>slotSyncPublisherMaxNum</tt>.
   *
   * @param slotSyncPublisherMaxNum value to be assigned to property slotSyncPublisherMaxNum
   */
  public void setSlotSyncPublisherMaxNum(int slotSyncPublisherMaxNum) {
    this.slotSyncPublisherMaxNum = slotSyncPublisherMaxNum;
  }

  /**
   * Getter method for property <tt>schedulerScanVersionIntervalMs</tt>.
   *
   * @return property value of schedulerScanVersionIntervalMs
   */
  @Override
  public int getSchedulerScanVersionIntervalMillis() {
    return schedulerScanVersionIntervalMillis;
  }

  /**
   * Setter method for property <tt>schedulerScanVersionIntervalMs</tt>.
   *
   * @param schedulerScanVersionIntervalMs value to be assigned to property
   *     schedulerScanVersionIntervalMs
   */
  public void setSchedulerFetchDataVersionIntervalMs(int schedulerScanVersionIntervalMs) {
    this.schedulerScanVersionIntervalMillis = schedulerScanVersionIntervalMs;
  }

  public int getSlotSyncMaxBufferSize() {
    return slotSyncMaxBufferSize;
  }

  public void setSlotSyncMaxBufferSize(int slotSyncMaxBufferSize) {
    this.slotSyncMaxBufferSize = slotSyncMaxBufferSize;
  }

  public int getSlotSyncWorkerSize() {
    return slotSyncWorkerSize;
  }

  @Override
  public int getCacheDigestIntervalMinutes() {
    return cacheDigestIntervalMinutes;
  }

  public void setCacheDigestIntervalMinutes(int cacheDigestIntervalMinutes) {
    this.cacheDigestIntervalMinutes = cacheDigestIntervalMinutes;
  }

  public void setSlotSyncWorkerSize(int slotSyncWorkerSize) {
    this.slotSyncWorkerSize = slotSyncWorkerSize;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }

  @Override
  public int getDataNodeExecutorWorkerSize() {
    return dataNodeExecutorWorkerSize;
  }

  public void setDataNodeExecutorWorkerSize(int dataNodeExecutorWorkerSize) {
    this.dataNodeExecutorWorkerSize = dataNodeExecutorWorkerSize;
  }

  @Override
  public int getDataNodeExecutorQueueSize() {
    return dataNodeExecutorQueueSize;
  }

  public void setDataNodeExecutorQueueSize(int dataNodeExecutorQueueSize) {
    this.dataNodeExecutorQueueSize = dataNodeExecutorQueueSize;
  }

  @Override
  public int getDataNodeRetryBackoffMillis() {
    return dataNodeRetryBackoffMillis;
  }

  public void setDataNodeRetryBackoffMillis(int dataNodeRetryBackoffMillis) {
    this.dataNodeRetryBackoffMillis = dataNodeRetryBackoffMillis;
  }

  @Override
  public int getDataNodeRetryTimes() {
    return dataNodeRetryTimes;
  }

  public void setDataNodeRetryTimes(int dataNodeRetryTimes) {
    this.dataNodeRetryTimes = dataNodeRetryTimes;
  }

  @Override
  public int getDataNodeRetryQueueSize() {
    return dataNodeRetryQueueSize;
  }

  public void setDataNodeRetryQueueSize(int dataNodeRetryQueueSize) {
    this.dataNodeRetryQueueSize = dataNodeRetryQueueSize;
  }

  @Override
  public int getDataNodeMaxBatchSize() {
    return dataNodeMaxBatchSize;
  }

  public void setDataNodeMaxBatchSize(int dataNodeMaxBatchSize) {
    this.dataNodeMaxBatchSize = dataNodeMaxBatchSize;
  }

  @Override
  public int getCacheCountIntervalSecs() {
    return cacheCountIntervalSecs;
  }

  public void setCacheCountIntervalSecs(int cacheCountIntervalSecs) {
    this.cacheCountIntervalSecs = cacheCountIntervalSecs;
  }

  @Override
  public int getCacheDatumMaxWeight() {
    return cacheDatumMaxWeight;
  }

  public void setCacheDatumMaxWeight(int cacheDatumMaxWeight) {
    this.cacheDatumMaxWeight = cacheDatumMaxWeight;
  }

  @Override
  public int getCacheDatumExpireSecs() {
    return cacheDatumExpireSecs;
  }

  public void setCacheDatumExpireSecs(int cacheDatumExpireSecs) {
    this.cacheDatumExpireSecs = cacheDatumExpireSecs;
  }

  @Override
  public int getSyncSessionIOLowWaterMark() {
    return syncSessionIOLowWaterMark;
  }

  public void setSyncSessionIOLowWaterMark(int syncSessionIOLowWaterMark) {
    this.syncSessionIOLowWaterMark = syncSessionIOLowWaterMark;
  }

  @Override
  public int getSyncSessionIOHighWaterMark() {
    return syncSessionIOHighWaterMark;
  }

  public void setSyncSessionIOHighWaterMark(int syncSessionIOHighWaterMark) {
    this.syncSessionIOHighWaterMark = syncSessionIOHighWaterMark;
  }

  /**
   * Getter method for property <tt>heartbeatCacheCheckerInitialDelaySecs</tt>.
   *
   * @return property value of heartbeatCacheCheckerInitialDelaySecs
   */
  public int getHeartbeatCacheCheckerInitialDelaySecs() {
    return heartbeatCacheCheckerInitialDelaySecs;
  }

  /**
   * Setter method for property <tt>heartbeatCacheCheckerInitialDelaySecs</tt>.
   *
   * @param heartbeatCacheCheckerInitialDelaySecs value to be assigned to property
   *     heartbeatCacheCheckerInitialDelaySecs
   */
  public void setHeartbeatCacheCheckerInitialDelaySecs(int heartbeatCacheCheckerInitialDelaySecs) {
    this.heartbeatCacheCheckerInitialDelaySecs = heartbeatCacheCheckerInitialDelaySecs;
  }

  /**
   * Getter method for property <tt>heartbeatCacheCheckerSecs</tt>.
   *
   * @return property value of heartbeatCacheCheckerSecs
   */
  public int getHeartbeatCacheCheckerSecs() {
    return heartbeatCacheCheckerSecs;
  }

  /**
   * Setter method for property <tt>heartbeatCacheCheckerSecs</tt>.
   *
   * @param heartbeatCacheCheckerSecs value to be assigned to property heartbeatCacheCheckerSecs
   */
  public void setHeartbeatCacheCheckerSecs(int heartbeatCacheCheckerSecs) {
    this.heartbeatCacheCheckerSecs = heartbeatCacheCheckerSecs;
  }

  /**
   * Getter method for property <tt>revisionHeartbeatInitialDelayMinutes</tt>.
   *
   * @return property value of revisionHeartbeatInitialDelayMinutes
   */
  public int getRevisionHeartbeatInitialDelayMinutes() {
    return revisionHeartbeatInitialDelayMinutes;
  }

  /**
   * Setter method for property <tt>revisionHeartbeatInitialDelayMinutes</tt>.
   *
   * @param revisionHeartbeatInitialDelayMinutes value to be assigned to property
   *     revisionHeartbeatInitialDelayMinutes
   */
  public void setRevisionHeartbeatInitialDelayMinutes(int revisionHeartbeatInitialDelayMinutes) {
    this.revisionHeartbeatInitialDelayMinutes = revisionHeartbeatInitialDelayMinutes;
  }

  /**
   * Getter method for property <tt>revisionHeartbeatMinutes</tt>.
   *
   * @return property value of revisionHeartbeatMinutes
   */
  public int getRevisionHeartbeatMinutes() {
    return revisionHeartbeatMinutes;
  }

  /**
   * Setter method for property <tt>revisionHeartbeatMinutes</tt>.
   *
   * @param revisionHeartbeatMinutes value to be assigned to property revisionHeartbeatMinutes
   */
  public void setRevisionHeartbeatMinutes(int revisionHeartbeatMinutes) {
    this.revisionHeartbeatMinutes = revisionHeartbeatMinutes;
  }

  @Override
  public int getAccessMetadataMaxBufferSize() {
    return accessMetadataMaxBufferSize;
  }

  public void setAccessMetadataMaxBufferSize(int accessMetadataMaxBufferSize) {
    this.accessMetadataMaxBufferSize = accessMetadataMaxBufferSize;
  }

  @Override
  public int getAccessMetadataWorkerSize() {
    return accessMetadataWorkerSize;
  }

  public void setAccessMetadataWorkerSize(int accessMetadataWorkerSize) {
    this.accessMetadataWorkerSize = accessMetadataWorkerSize;
  }

  @Override
  public int getMetaNodeBufferSize() {
    return metaNodeBufferSize;
  }

  public void setMetaNodeBufferSize(int metaNodeBufferSize) {
    this.metaNodeBufferSize = metaNodeBufferSize;
  }

  @Override
  public int getMetaNodeWorkerSize() {
    return metaNodeWorkerSize;
  }

  public void setMetaNodeWorkerSize(int metaNodeWorkerSize) {
    this.metaNodeWorkerSize = metaNodeWorkerSize;
  }

  public int getSubscriberRegisterTaskMaxBufferSize() {
    return subscriberRegisterTaskMaxBufferSize;
  }

  public void setSubscriberRegisterTaskMaxBufferSize(int subscriberRegisterTaskMaxBufferSize) {
    this.subscriberRegisterTaskMaxBufferSize = subscriberRegisterTaskMaxBufferSize;
  }

  public int getSubscriberRegisterTaskWorkerSize() {
    return subscriberRegisterTaskWorkerSize;
  }

  public void setSubscriberRegisterTaskWorkerSize(int subscriberRegisterTaskWorkerSize) {
    this.subscriberRegisterTaskWorkerSize = subscriberRegisterTaskWorkerSize;
  }
}
