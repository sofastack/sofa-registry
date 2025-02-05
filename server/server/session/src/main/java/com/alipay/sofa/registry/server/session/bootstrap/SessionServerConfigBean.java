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

import com.alipay.sofa.registry.server.shared.config.CommonConfig;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.util.OsUtils;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
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

  private int consolePort = 9604;

  private int syncSessionIOLowWaterMark = 1024 * 288;

  private int syncSessionIOHighWaterMark = 1024 * 320;

  private int clientIOLowWaterMark = 1024 * 384;

  private int clientIOHighWaterMark = 1024 * 512;

  private int metaServerPort = 9610;

  private int dataServerPort = 9620;

  private int dataServerNotifyPort = 9623;

  private int httpServerPort = 9603;

  private int schedulerHeartbeatIntervalSecs = 1;

  private int accessDataExecutorPoolSize = OsUtils.getCpuCount() * 4;

  private int accessDataExecutorQueueSize = 10000;

  private int accessSubDataExecutorPoolSize = OsUtils.getCpuCount() * 4;

  private int accessSubDataExecutorQueueSize = 40000;

  private int dataChangeExecutorPoolSize = OsUtils.getCpuCount() * 3;

  private int dataChangeExecutorQueueSize = 20000;

  private int dataChangeFetchTaskWorkerSize = OsUtils.getCpuCount() * 6;

  private int subscriberRegisterTaskWorkerSize = OsUtils.getCpuCount() * 4;

  private int metadataRegisterExecutorPoolSize = OsUtils.getCpuCount() * 3;

  private int metadataRegisterExecutorQueueSize = 1000;

  private int scanExecutorPoolSize = OsUtils.getCpuCount() * 3;

  private int scanExecutorQueueSize = 100;

  private int dataChangeDebouncingMillis = 1000;
  private int dataChangeMaxDebouncingMillis = 3000;

  private int slotSyncMaxBufferSize = 5000;

  private int slotSyncWorkerSize = OsUtils.getCpuCount() * 3;

  private int metaNodeBufferSize = 2000;

  private int metaNodeWorkerSize = OsUtils.getCpuCount() * 4;

  private int accessMetadataMaxBufferSize = 10000;

  private int accessMetadataWorkerSize = OsUtils.getCpuCount() * 4;

  private int consoleExecutorPoolSize = OsUtils.getCpuCount() * 3;

  private int consoleExecutorQueueSize = 2000;

  private int clientNodeExchangeTimeoutMillis = 2000;
  private int clientNodePushConcurrencyLevel = 1;
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

  private int scanSubscriberIntervalMillis = 1000 * 5;

  private double accessLimitRate = 30000.0;

  private String sessionServerRegion;

  private String sessionServerDataCenter;

  // begin config for enterprise version

  /** forever close push zone，such as:RZBETA */
  private String invalidForeverZones = "";
  /** config regex,exception to the rule of forever close push zone */
  private String invalidIgnoreDataidRegex = "";

  private volatile Set<String> invalidForeverZonesSet;

  private volatile Optional<Pattern> invalidIgnoreDataIdPattern = null;

  private int dataClientConnNum = 10;
  private int dataNotifyClientConnNum = 5;
  private int sessionSchedulerPoolSize = OsUtils.getCpuCount();

  private int slotSyncPublisherMaxNum = 512;

  private int cacheDigestIntervalMinutes = 15;

  private int cacheCountIntervalSecs = 30;

  private int cacheDatumMaxWeight = 1024 * 1024 * 256;

  private int cacheDatumExpireSecs = 5;

  // metadata config start

  private int heartbeatCacheCheckerInitialDelaySecs = 60;

  private int heartbeatCacheCheckerSecs = 60;

  private int revisionHeartbeatInitialDelayMinutes = 10;

  private int revisionHeartbeatMinutes = 10;

  private int clientManagerIntervalMillis = 1000;

  private int clientOpenIntervalSecs = 5;

  // metadata config end

  // end config for enterprise version

  private CommonConfig commonConfig;

  private volatile Collection<String> metaAddresses;

  private int clientManagerAddressIntervalMillis = 1000;

  private int systemPropertyIntervalMillis = 3000;

  private int pushTaskBufferBucketSize = 4;

  private int pushCircuitBreakerThreshold = 10;

  private int pushCircuitBreakerSilenceMillis = 60 * 1000;

  private int pushAddressCircuitBreakerThreshold = 50;

  private int pushConsecutiveSuccess = 1;

  private int skipPushEmptySilentMillis = 30 * 1000;

  private int scanWatcherIntervalMillis = 1000 * 3;

  private int watchConfigFetchBatchSize = 100;
  private int watchConfigFetchIntervalMillis = 1000 * 30;
  private int watchConfigFetchLeaseSecs = 30;
  private boolean watchConfigEnable = false;

  private int watchPushTaskWorkerSize = OsUtils.getCpuCount() * 2;

  private int watchPushTaskMaxBufferSize = 10000;

  private boolean gracefulShutdown = false;

  private int scanTimeoutMills = 10 * 1000;

  /**
   * constructor
   *
   * @param commonConfig commonConfig
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

  public void setSyncSessionPort(int syncSessionPort) {
    this.syncSessionPort = syncSessionPort;
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
  public Set<String> getLocalDataCenterZones() {
    return commonConfig.getLocalSegmentRegions();
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
  public boolean isLocalDataCenter(String dataCenter) {
    return StringUtils.equals(getSessionServerDataCenter(), dataCenter);
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
  public int getAccessDataExecutorPoolSize() {
    return accessDataExecutorPoolSize;
  }

  /**
   * Setter method for property <tt>accessDataExecutorPoolSize</tt>.
   *
   * @param accessDataExecutorPoolSize value to be assigned to property accessDataExecutorPoolSize
   */
  public void setAccessDataExecutorPoolSize(int accessDataExecutorPoolSize) {
    this.accessDataExecutorPoolSize = accessDataExecutorPoolSize;
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
  public int getAccessSubDataExecutorPoolSize() {
    return accessSubDataExecutorPoolSize;
  }

  public void setAccessSubDataExecutorPoolSize(int accessSubDataExecutorPoolSize) {
    this.accessSubDataExecutorPoolSize = accessSubDataExecutorPoolSize;
  }

  @Override
  public int getAccessSubDataExecutorQueueSize() {
    return accessSubDataExecutorQueueSize;
  }

  public void setAccessSubDataExecutorQueueSize(int accessSubDataExecutorQueueSize) {
    this.accessSubDataExecutorQueueSize = accessSubDataExecutorQueueSize;
  }

  /**
   * Getter method for property <tt>dataChangeExecutorPoolSize</tt>.
   *
   * @return property value of dataChangeExecutorPoolSize
   */
  @Override
  public int getDataChangeExecutorPoolSize() {
    return dataChangeExecutorPoolSize;
  }

  public void setDataChangeExecutorPoolSize(int dataChangeExecutorPoolSize) {
    this.dataChangeExecutorPoolSize = dataChangeExecutorPoolSize;
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
   * Setter method for property <tt>dataChangeExecutorQueueSize</tt>.
   *
   * @param dataChangeExecutorQueueSize value to be assigned to property dataChangeExecutorQueueSize
   */
  public void setDataChangeExecutorQueueSize(int dataChangeExecutorQueueSize) {
    this.dataChangeExecutorQueueSize = dataChangeExecutorQueueSize;
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
    if (invalidIgnoreDataIdPattern == null) {
      String invalidIgnoreDataidRegex = getInvalidIgnoreDataidRegex();
      if (StringUtils.isBlank(invalidIgnoreDataidRegex)) {
        invalidIgnoreDataIdPattern = Optional.empty();
      } else {
        invalidIgnoreDataIdPattern = Optional.of(Pattern.compile(invalidIgnoreDataidRegex));
      }
    }
    final Pattern p = invalidIgnoreDataIdPattern.get();
    return p != null && p.matcher(dataId).find();
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
  public int getScanSubscriberIntervalMillis() {
    return scanSubscriberIntervalMillis;
  }

  /**
   * Setter method for property <tt>schedulerScanVersionIntervalMillis</tt>.
   *
   * @param scanSubscriberIntervalMillis value to be assigned to property
   *     schedulerScanVersionIntervalMs
   */
  public void setScanSubscriberIntervalMillis(int scanSubscriberIntervalMillis) {
    this.scanSubscriberIntervalMillis = scanSubscriberIntervalMillis;
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
  public int getClientManagerIntervalMillis() {
    return clientManagerIntervalMillis;
  }

  @Override
  public int getClientOpenIntervalSecs() {
    return clientOpenIntervalSecs;
  }

  /**
   * Setter method for property <tt>clientOpenIntervalSecs</tt>.
   *
   * @param clientOpenIntervalSecs value to be assigned to property clientOpenIntervalSecs
   */
  public void setClientOpenIntervalSecs(int clientOpenIntervalSecs) {
    this.clientOpenIntervalSecs = clientOpenIntervalSecs;
  }

  public void setClientManagerIntervalMillis(int clientManagerIntervalMillis) {
    this.clientManagerIntervalMillis = clientManagerIntervalMillis;
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

  public int getSubscriberRegisterTaskWorkerSize() {
    return subscriberRegisterTaskWorkerSize;
  }

  public void setSubscriberRegisterTaskWorkerSize(int subscriberRegisterTaskWorkerSize) {
    this.subscriberRegisterTaskWorkerSize = subscriberRegisterTaskWorkerSize;
  }

  @Override
  public int getClientIOLowWaterMark() {
    return clientIOLowWaterMark;
  }

  public void setClientIOLowWaterMark(int clientIOLowWaterMark) {
    this.clientIOLowWaterMark = clientIOLowWaterMark;
  }

  @Override
  public int getClientIOHighWaterMark() {
    return clientIOHighWaterMark;
  }

  public void setClientIOHighWaterMark(int clientIOHighWaterMark) {
    this.clientIOHighWaterMark = clientIOHighWaterMark;
  }

  @Override
  public int getSystemPropertyIntervalMillis() {
    return systemPropertyIntervalMillis;
  }

  public void setSystemPropertyIntervalMillis(int systemPropertyIntervalMillis) {
    this.systemPropertyIntervalMillis = systemPropertyIntervalMillis;
  }

  @Override
  public int getClientManagerAddressIntervalMillis() {
    return clientManagerAddressIntervalMillis;
  }

  public void setClientManagerAddressIntervalMillis(int clientManagerAddressIntervalMillis) {
    this.clientManagerAddressIntervalMillis = clientManagerAddressIntervalMillis;
  }

  @Override
  public int getConsolePort() {
    return consolePort;
  }

  public void setConsolePort(int consolePort) {
    this.consolePort = consolePort;
  }

  @Override
  public int getConsoleExecutorPoolSize() {
    return consoleExecutorPoolSize;
  }

  public void setConsoleExecutorPoolSize(int consoleExecutorPoolSize) {
    this.consoleExecutorPoolSize = consoleExecutorPoolSize;
  }

  @Override
  public int getConsoleExecutorQueueSize() {
    return consoleExecutorQueueSize;
  }

  public void setConsoleExecutorQueueSize(int consoleExecutorQueueSize) {
    this.consoleExecutorQueueSize = consoleExecutorQueueSize;
  }

  @Override
  public int getPushTaskBufferBucketSize() {
    return pushTaskBufferBucketSize;
  }

  public void setPushTaskBufferBucketSize(int pushTaskBufferBucketSize) {
    this.pushTaskBufferBucketSize = pushTaskBufferBucketSize;
  }

  /**
   * Getter method for property <tt>pushCircuitBreakerThreshold</tt>.
   *
   * @return property value of pushCircuitBreakerThreshold
   */
  @Override
  public int getPushCircuitBreakerThreshold() {
    return pushCircuitBreakerThreshold;
  }

  /**
   * Setter method for property <tt>pushCircuitBreakerThreshold</tt>.
   *
   * @param pushCircuitBreakerThreshold value to be assigned to property pushCircuitBreakerThreshold
   */
  public void setPushCircuitBreakerThreshold(int pushCircuitBreakerThreshold) {
    this.pushCircuitBreakerThreshold = pushCircuitBreakerThreshold;
  }

  /**
   * Getter method for property <tt>pushCircuitBreakerSilenceMillis</tt>.
   *
   * @return property value of pushCircuitBreakerSilenceMillis
   */
  @Override
  public int getPushCircuitBreakerSilenceMillis() {
    return pushCircuitBreakerSilenceMillis;
  }

  /**
   * Setter method for property <tt>pushCircuitBreakerSilenceMillis</tt>.
   *
   * @param pushCircuitBreakerSilenceMillis value to be assigned to property
   *     pushCircuitBreakerSilenceMillis
   */
  public void setPushCircuitBreakerSilenceMillis(int pushCircuitBreakerSilenceMillis) {
    this.pushCircuitBreakerSilenceMillis = pushCircuitBreakerSilenceMillis;
  }

  public int getSkipPushEmptySilentMillis() {
    return skipPushEmptySilentMillis;
  }

  public void setSkipPushEmptySilentMillis(int skipPushEmptySilentMillis) {
    this.skipPushEmptySilentMillis = skipPushEmptySilentMillis;
  }

  @Override
  public int getClientNodePushConcurrencyLevel() {
    return clientNodePushConcurrencyLevel;
  }

  public void setClientNodePushConcurrencyLevel(int clientNodePushConcurrencyLevel) {
    this.clientNodePushConcurrencyLevel = clientNodePushConcurrencyLevel;
  }

  @Override
  public int getScanWatcherIntervalMillis() {
    return scanWatcherIntervalMillis;
  }

  @Override
  public boolean isGracefulShutdown() {
    return gracefulShutdown;
  }

  public void setGracefulShutdown(boolean gracefulShutdown) {
    this.gracefulShutdown = gracefulShutdown;
  }

  public void setScanWatcherIntervalMillis(int scanWatcherIntervalMillis) {
    this.scanWatcherIntervalMillis = scanWatcherIntervalMillis;
  }

  @Override
  public int getWatchConfigFetchBatchSize() {
    return watchConfigFetchBatchSize;
  }

  public void setWatchConfigFetchBatchSize(int watchConfigFetchBatchSize) {
    this.watchConfigFetchBatchSize = watchConfigFetchBatchSize;
  }

  @Override
  public int getWatchConfigFetchIntervalMillis() {
    return watchConfigFetchIntervalMillis;
  }

  public void setWatchConfigFetchIntervalMillis(int watchConfigFetchIntervalMillis) {
    this.watchConfigFetchIntervalMillis = watchConfigFetchIntervalMillis;
  }

  @Override
  public int getWatchConfigFetchLeaseSecs() {
    return watchConfigFetchLeaseSecs;
  }

  public void setWatchConfigFetchLeaseSecs(int watchConfigFetchLeaseSecs) {
    this.watchConfigFetchLeaseSecs = watchConfigFetchLeaseSecs;
  }

  @Override
  public boolean isWatchConfigEnable() {
    return watchConfigEnable;
  }

  public void setWatchConfigEnable(boolean watchConfigEnable) {
    this.watchConfigEnable = watchConfigEnable;
  }

  @Override
  public int getWatchPushTaskWorkerSize() {
    return watchPushTaskWorkerSize;
  }

  public void setWatchPushTaskWorkerSize(int watchPushTaskWorkerSize) {
    this.watchPushTaskWorkerSize = watchPushTaskWorkerSize;
  }

  @Override
  public int getWatchPushTaskMaxBufferSize() {
    return watchPushTaskMaxBufferSize;
  }

  public void setWatchPushTaskMaxBufferSize(int watchPushTaskMaxBufferSize) {
    this.watchPushTaskMaxBufferSize = watchPushTaskMaxBufferSize;
  }

  /**
   * Getter method for property <tt>pushConsecutiveSuccess</tt>.
   *
   * @return property value of pushConsecutiveSuccess
   */
  @Override
  public int getPushConsecutiveSuccess() {
    return pushConsecutiveSuccess;
  }

  /**
   * Setter method for property <tt>pushConsecutiveSuccess</tt>.
   *
   * @param pushConsecutiveSuccess value to be assigned to property pushConsecutiveSuccess
   */
  public void setPushConsecutiveSuccess(int pushConsecutiveSuccess) {
    this.pushConsecutiveSuccess = pushConsecutiveSuccess;
  }

  /**
   * Getter method for property <tt>pushAddressCircuitBreakerThreshold</tt>.
   *
   * @return property value of pushAddressCircuitBreakerThreshold
   */
  @Override
  public int getPushAddressCircuitBreakerThreshold() {
    return pushAddressCircuitBreakerThreshold;
  }

  /**
   * Setter method for property <tt>pushAddressCircuitBreakerThreshold</tt>.
   *
   * @param pushAddressCircuitBreakerThreshold value to be assigned to property
   *     pushAddressCircuitBreakerThreshold
   */
  public void setPushAddressCircuitBreakerThreshold(int pushAddressCircuitBreakerThreshold) {
    this.pushAddressCircuitBreakerThreshold = pushAddressCircuitBreakerThreshold;
  }

  @Override
  public int getMetadataRegisterExecutorPoolSize() {
    return metadataRegisterExecutorPoolSize;
  }

  @Override
  public int getMetadataRegisterExecutorQueueSize() {
    return metadataRegisterExecutorQueueSize;
  }

  @Override
  public int getScanExecutorPoolSize() {
    return scanExecutorPoolSize;
  }

  @Override
  public int getScanExecutorQueueSize() {
    return scanExecutorQueueSize;
  }

  @Override
  public long getScanTimeoutMills() {
    return scanTimeoutMills;
  }

  /**
   * Setter method for property <tt>metadataRegisterExecutorPoolSize</tt>.
   *
   * @param metadataRegisterExecutorPoolSize value to be assigned to property
   *     metadataRegisterExecutorPoolSize
   */
  public void setMetadataRegisterExecutorPoolSize(int metadataRegisterExecutorPoolSize) {
    this.metadataRegisterExecutorPoolSize = metadataRegisterExecutorPoolSize;
  }

  /**
   * Setter method for property <tt>metadataRegisterExecutorQueueSize</tt>.
   *
   * @param metadataRegisterExecutorQueueSize value to be assigned to property
   *     metadataRegisterExecutorQueueSize
   */
  public void setMetadataRegisterExecutorQueueSize(int metadataRegisterExecutorQueueSize) {
    this.metadataRegisterExecutorQueueSize = metadataRegisterExecutorQueueSize;
  }

  /**
   * Setter method for property <tt>scanExecutorPoolSize</tt>.
   *
   * @param scanExecutorPoolSize value to be assigned to property scanExecutorPoolSize
   */
  public void setScanExecutorPoolSize(int scanExecutorPoolSize) {
    this.scanExecutorPoolSize = scanExecutorPoolSize;
  }

  /**
   * Setter method for property <tt>scanExecutorQueueSize</tt>.
   *
   * @param scanExecutorQueueSize value to be assigned to property scanExecutorQueueSize
   */
  public void setScanExecutorQueueSize(int scanExecutorQueueSize) {
    this.scanExecutorQueueSize = scanExecutorQueueSize;
  }

  /**
   * Setter method for property <tt>scanTimeoutMills</tt>.
   *
   * @param scanTimeoutMills value to be assigned to property scanTimeoutMills
   */
  public void setScanTimeoutMills(int scanTimeoutMills) {
    this.scanTimeoutMills = scanTimeoutMills;
  }
}
