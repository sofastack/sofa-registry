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

import com.alipay.sofa.registry.common.model.metaserver.Lease;
import com.alipay.sofa.registry.server.shared.config.CommonConfig;
import com.alipay.sofa.registry.util.OsUtils;
import com.alipay.sofa.registry.util.SystemUtils;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author shangyu.wh
 * @version $Id: MetaServerConfigBean.java, v 0.1 2018-01-16 11:01 shangyu.wh Exp $
 */
@ConfigurationProperties(prefix = MetaServerConfigBean.PREFIX)
public class MetaServerConfigBean implements MetaServerConfig {

  public static final String PREFIX = "meta.server";

  private int sessionServerPort = 9610;

  private int dataServerPort = 9611;

  private int metaServerPort = 9612;

  private int httpServerPort = 9615;

  private int schedulerHeartbeatIntervalSecs = 1;

  private int dataNodeExchangeTimeoutMillis = 3000;

  private int sessionNodeExchangeTimeoutMillis = 3000;

  private int metaNodeExchangeTimeoutMillis = 3000;

  private int defaultRequestExecutorMinSize = OsUtils.getCpuCount() * 5;
  private int defaultRequestExecutorMaxSize = OsUtils.getCpuCount() * 10;
  private int defaultRequestExecutorQueueSize = 500;

  private int expireCheckIntervalMillis = 1000;

  private int revisionGcSilenceHour = 24;

  private int revisionGcInitialDelaySecs = 60;

  private int revisionGcSecs = 60;

  private int clientManagerWatchMillis = 1000;

  private int clientManagerRefreshMillis = 300 * 1000;

  private int clientManagerRefreshLimit = 5000;

  private int clientManagerCleanSecs = 300;

  private int clientManagerExpireDays = 1;

  private int appRevisionMaxRemove = 2000;
  private int appRevisionCountAlarmThreshold = 20;

  private long metaLeaderWarmupMillis =
      SystemUtils.getSystemLong(
          "registry.elector.warm.up.millis",
          TimeUnit.SECONDS.toMillis(Lease.DEFAULT_DURATION_SECS * 3 / 2));

  private long dataReplicateMaxGapMillis =
      SystemUtils.getSystemLong("registry.data.replicate.max.gap.millis", 3 * 60 * 1000);

  private int metaSchedulerPoolSize = OsUtils.getCpuCount();

  // <=0 means no protection
  private int dataNodeProtectionNum =
      SystemUtils.getSystemInteger("registry.data.protection.num", 0);

  private CommonConfig commonConfig;

  public MetaServerConfigBean(CommonConfig commonConfig) {
    this.commonConfig = commonConfig;
  }

  @Override
  public String getLocalDataCenter() {
    return commonConfig.getLocalDataCenter();
  }

  @Override
  public boolean isLocalDataCenter(String dataCenter) {
    return commonConfig.getLocalDataCenter().equals(dataCenter);
  }

  @Override
  public Set<String> getLocalDataCenterZones() {
    return commonConfig.getLocalSegmentRegions();
  }

  /**
   * Gets get session server port.
   *
   * @return the get session server port
   */
  @Override
  public int getSessionServerPort() {
    return sessionServerPort;
  }

  /**
   * Setter method for property <tt>sessionServerPort</tt>.
   *
   * @param sessionServerPort value to be assigned to property sessionServerPort
   */
  public void setSessionServerPort(int sessionServerPort) {
    this.sessionServerPort = sessionServerPort;
  }

  /**
   * Gets get data server port.
   *
   * @return the get data server port
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

  /**
   * Gets get http server port.
   *
   * @return the get http server port
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
   * Getter method for property <tt>schedulerHeartbeatExpBackOffBound</tt>.
   *
   * @return property value of schedulerHeartbeatExpBackOffBound
   */
  @Override
  public int getExpireCheckIntervalMillis() {
    return expireCheckIntervalMillis;
  }

  /**
   * Sets set expire check interval milli.
   *
   * @param expireCheckIntervalMillis the expire check interval milli
   * @return the set expire check interval milli
   */
  public MetaServerConfigBean setExpireCheckIntervalMillis(int expireCheckIntervalMillis) {
    this.expireCheckIntervalMillis = expireCheckIntervalMillis;
    return this;
  }

  /**
   * Getter method for property <tt>dataNodeExchangeTimeout</tt>.
   *
   * @return property value of dataNodeExchangeTimeout
   */
  @Override
  public int getDataNodeExchangeTimeoutMillis() {
    return dataNodeExchangeTimeoutMillis;
  }

  /**
   * Setter method for property <tt>dataNodeExchangeTimeout</tt>.
   *
   * @param dataNodeExchangeTimeoutMillis value to be assigned to property dataNodeExchangeTimeout
   */
  public void setDataNodeExchangeTimeoutMillis(int dataNodeExchangeTimeoutMillis) {
    this.dataNodeExchangeTimeoutMillis = dataNodeExchangeTimeoutMillis;
  }

  /**
   * Getter method for property <tt>sessionNodeExchangeTimeout</tt>.
   *
   * @return property value of sessionNodeExchangeTimeout
   */
  @Override
  public int getSessionNodeExchangeTimeoutMillis() {
    return sessionNodeExchangeTimeoutMillis;
  }

  /**
   * Setter method for property <tt>sessionNodeExchangeTimeout</tt>.
   *
   * @param sessionNodeExchangeTimeoutMillis value to be assigned to property
   *     sessionNodeExchangeTimeout
   */
  public void setSessionNodeExchangeTimeoutMillis(int sessionNodeExchangeTimeoutMillis) {
    this.sessionNodeExchangeTimeoutMillis = sessionNodeExchangeTimeoutMillis;
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

  /**
   * Getter method for property <tt>metaNodeExchangeTimeout</tt>.
   *
   * @return property value of metaNodeExchangeTimeout
   */
  @Override
  public int getMetaNodeExchangeTimeoutMillis() {
    return metaNodeExchangeTimeoutMillis;
  }

  /**
   * Setter method for property <tt>metaNodeExchangeTimeout</tt>.
   *
   * @param metaNodeExchangeTimeoutMillis value to be assigned to property metaNodeExchangeTimeout
   */
  public void setMetaNodeExchangeTimeoutMillis(int metaNodeExchangeTimeoutMillis) {
    this.metaNodeExchangeTimeoutMillis = metaNodeExchangeTimeoutMillis;
  }

  /**
   * To string string.
   *
   * @return the string
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }

  /**
   * Gets get default request executor min size.
   *
   * @return the get default request executor min size
   */
  @Override
  public int getDefaultRequestExecutorMinSize() {
    return defaultRequestExecutorMinSize;
  }

  /**
   * Sets set default request executor min size.
   *
   * @param defaultRequestExecutorMinSize the default request executor min size
   */
  public void setDefaultRequestExecutorMinSize(int defaultRequestExecutorMinSize) {
    this.defaultRequestExecutorMinSize = defaultRequestExecutorMinSize;
  }

  /**
   * Gets get default request executor max size.
   *
   * @return the get default request executor max size
   */
  @Override
  public int getDefaultRequestExecutorMaxSize() {
    return defaultRequestExecutorMaxSize;
  }

  /**
   * Sets set default request executor max size.
   *
   * @param defaultRequestExecutorMaxSize the default request executor max size
   */
  public void setDefaultRequestExecutorMaxSize(int defaultRequestExecutorMaxSize) {
    this.defaultRequestExecutorMaxSize = defaultRequestExecutorMaxSize;
  }

  /**
   * Gets get default request executor queue size.
   *
   * @return the get default request executor queue size
   */
  @Override
  public int getDefaultRequestExecutorQueueSize() {
    return defaultRequestExecutorQueueSize;
  }

  /**
   * Sets set default request executor queue size.
   *
   * @param defaultRequestExecutorQueueSize the default request executor queue size
   */
  public void setDefaultRequestExecutorQueueSize(int defaultRequestExecutorQueueSize) {
    this.defaultRequestExecutorQueueSize = defaultRequestExecutorQueueSize;
  }

  @Override
  public long getMetaLeaderWarmupMillis() {
    return metaLeaderWarmupMillis;
  }

  public void setMetaLeaderWarmupMillis(long metaLeaderWarmupMillis) {
    this.metaLeaderWarmupMillis = metaLeaderWarmupMillis;
  }

  /**
   * Getter method for property <tt>schedulerHeartbeatIntervalSecs</tt>.
   *
   * @return property value of schedulerHeartbeatIntervalSecs
   */
  @Override
  public int getSchedulerHeartbeatIntervalSecs() {
    return schedulerHeartbeatIntervalSecs;
  }

  /**
   * Setter method for property <tt>schedulerHeartbeatIntervalSecs</tt>.
   *
   * @param schedulerHeartbeatIntervalSecs value to be assigned to property
   *     schedulerHeartbeatIntervalSecs
   */
  public void setSchedulerHeartbeatIntervalSecs(int schedulerHeartbeatIntervalSecs) {
    this.schedulerHeartbeatIntervalSecs = schedulerHeartbeatIntervalSecs;
  }

  public void setDataReplicateMaxGapMillis(long dataReplicateMaxGapMillis) {
    this.dataReplicateMaxGapMillis = dataReplicateMaxGapMillis;
  }

  @Override
  public long getDataReplicateMaxGapMillis() {
    return dataReplicateMaxGapMillis;
  }

  /**
   * Getter method for property <tt>revisionGcSilenceHour</tt>.
   *
   * @return property value of revisionGcSilenceHour
   */
  public int getRevisionGcSilenceHour() {
    return revisionGcSilenceHour;
  }

  /**
   * Setter method for property <tt>revisionGcSilenceHour</tt>.
   *
   * @param revisionGcSilenceHour value to be assigned to property revisionGcSilenceHour
   */
  public void setRevisionGcSilenceHour(int revisionGcSilenceHour) {
    this.revisionGcSilenceHour = revisionGcSilenceHour;
  }

  /**
   * Getter method for property <tt>revisionGcInitialDelaySecs</tt>.
   *
   * @return property value of revisionGcInitialDelaySecs
   */
  @Override
  public int getRevisionGcInitialDelaySecs() {
    return revisionGcInitialDelaySecs;
  }

  /**
   * Setter method for property <tt>revisionGcInitialDelaySecs</tt>.
   *
   * @param revisionGcInitialDelaySecs value to be assigned to property revisionGcInitialDelaySecs
   */
  public void setRevisionGcInitialDelaySecs(int revisionGcInitialDelaySecs) {
    this.revisionGcInitialDelaySecs = revisionGcInitialDelaySecs;
  }

  /**
   * Getter method for property <tt>revisionGcSecs</tt>.
   *
   * @return property value of revisionGcSecs
   */
  @Override
  public int getRevisionGcSecs() {
    return revisionGcSecs;
  }

  /**
   * Setter method for property <tt>revisionGcSecs</tt>.
   *
   * @param revisionGcSecs value to be assigned to property revisionGcSecs
   */
  public void setRevisionGcSecs(int revisionGcSecs) {
    this.revisionGcSecs = revisionGcSecs;
  }

  /**
   * Getter method for property <tt>metaSchedulerPoolSize</tt>.
   *
   * @return property value of metaSchedulerPoolSize
   */
  @Override
  public int getMetaSchedulerPoolSize() {
    return metaSchedulerPoolSize;
  }

  /**
   * Setter method for property <tt>metaSchedulerPoolSize</tt>.
   *
   * @param metaSchedulerPoolSize value to be assigned to property metaSchedulerPoolSize
   */
  public void setMetaSchedulerPoolSize(int metaSchedulerPoolSize) {
    this.metaSchedulerPoolSize = metaSchedulerPoolSize;
  }

  @Override
  public int getDataNodeProtectionNum() {
    return dataNodeProtectionNum;
  }

  public void setDataNodeProtectionNum(int dataNodeProtectionNum) {
    this.dataNodeProtectionNum = dataNodeProtectionNum;
  }

  /**
   * Getter method for property <tt>clientManagerWatchMillis</tt>.
   *
   * @return property value of clientManagerWatchMillis
   */
  @Override
  public int getClientManagerWatchMillis() {
    return clientManagerWatchMillis;
  }

  /**
   * Setter method for property <tt>clientManagerWatchMillis</tt>.
   *
   * @param clientManagerWatchMillis value to be assigned to property clientManagerWatchMillis
   */
  public void setClientManagerWatchMillis(int clientManagerWatchMillis) {
    this.clientManagerWatchMillis = clientManagerWatchMillis;
  }

  /**
   * Getter method for property <tt>clientManagerRefreshMillis</tt>.
   *
   * @return property value of clientManagerRefreshMillis
   */
  @Override
  public int getClientManagerRefreshMillis() {
    return clientManagerRefreshMillis;
  }

  /**
   * Setter method for property <tt>clientManagerRefreshMillis</tt>.
   *
   * @param clientManagerRefreshMillis value to be assigned to property clientManagerRefreshMillis
   */
  public void setClientManagerRefreshMillis(int clientManagerRefreshMillis) {
    this.clientManagerRefreshMillis = clientManagerRefreshMillis;
  }

  /**
   * Getter method for property <tt>clientManagerRefreshLimit</tt>.
   *
   * @return property value of clientManagerRefreshLimit
   */
  @Override
  public int getClientManagerRefreshLimit() {
    return clientManagerRefreshLimit;
  }

  @Override
  public int getClientManagerCleanSecs() {
    return clientManagerCleanSecs;
  }

  @Override
  public int getClientManagerExpireDays() {
    return clientManagerExpireDays;
  }

  @Override
  public int getAppRevisionMaxRemove() {
    return appRevisionMaxRemove;
  }

  public void setAppRevisionMaxRemove(int appRevisionMaxRemove) {
    this.appRevisionMaxRemove = appRevisionMaxRemove;
  }
  /**
   * Setter method for property <tt>clientManagerCleanSecs</tt>.
   *
   * @param clientManagerCleanSecs value to be assigned to property clientManagerCleanSecs
   */
  public void setClientManagerCleanSecs(int clientManagerCleanSecs) {
    this.clientManagerCleanSecs = clientManagerCleanSecs;
  }

  /**
   * Setter method for property <tt>clientManagerExpireDays</tt>.
   *
   * @param clientManagerExpireDays value to be assigned to property clientManagerExpireDays
   */
  public void setClientManagerExpireDays(int clientManagerExpireDays) {
    this.clientManagerExpireDays = clientManagerExpireDays;
  }

  /**
   * Setter method for property <tt>clientManagerRefreshLimit</tt>.
   *
   * @param clientManagerRefreshLimit value to be assigned to property clientManagerRefreshLimit
   */
  public void setClientManagerRefreshLimit(int clientManagerRefreshLimit) {
    this.clientManagerRefreshLimit = clientManagerRefreshLimit;
  }

  @Override
  public int getAppRevisionCountAlarmThreshold() {
    return appRevisionCountAlarmThreshold;
  }

  public void setAppRevisionCountAlarmThreshold(int appRevisionCountAlarmThreshold) {
    this.appRevisionCountAlarmThreshold = appRevisionCountAlarmThreshold;
  }
}
