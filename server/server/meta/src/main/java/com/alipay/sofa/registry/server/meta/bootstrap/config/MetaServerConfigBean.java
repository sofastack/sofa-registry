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

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.Lease;
import com.alipay.sofa.registry.util.OsUtils;
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

  private int revisionGcInitialDelayMillis = 60 * 1000;

  private int revisionGcMillis = 60 * 1000;

  private long metaLeaderWarmupMillis =
      Long.getLong(
          "registry.elector.warm.up.millis",
          TimeUnit.SECONDS.toMillis(Lease.DEFAULT_DURATION_SECS * 3 / 2));

  private long dataReplicateMaxGapMillis =
      Long.getLong("registry.data.replicate.max.gap.millis", 3 * 60 * 1000);



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
   * Gets get cross dc meta sync interval milli.
   *
   * @return the get cross dc meta sync interval milli
   */
  @Override
  public int getCrossDcMetaSyncIntervalMillis() {
    return ValueConstants.CROSS_DC_META_SYNC_INTERVAL_MILLI;
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
   * Getter method for property <tt>revisionGcInitialDelayMillis</tt>.
   *
   * @return property value of revisionGcInitialDelayMillis
   */
  public int getRevisionGcInitialDelayMillis() {
    return revisionGcInitialDelayMillis;
  }

  /**
   * Setter method for property <tt>revisionGcInitialDelayMillis</tt>.
   *
   * @param revisionGcInitialDelayMillis value to be assigned to property revisionGcInitialDelayMillis
   */
  public void setRevisionGcInitialDelayMillis(int revisionGcInitialDelayMillis) {
    this.revisionGcInitialDelayMillis = revisionGcInitialDelayMillis;
  }

  /**
   * Getter method for property <tt>revisionGcMillis</tt>.
   *
   * @return property value of revisionGcMillis
   */
  public int getRevisionGcMillis() {
    return revisionGcMillis;
  }

  /**
   * Setter method for property <tt>revisionGcMillis</tt>.
   *
   * @param revisionGcMillis value to be assigned to property revisionGcMillis
   */
  public void setRevisionGcMillis(int revisionGcMillis) {
    this.revisionGcMillis = revisionGcMillis;
  }
}
