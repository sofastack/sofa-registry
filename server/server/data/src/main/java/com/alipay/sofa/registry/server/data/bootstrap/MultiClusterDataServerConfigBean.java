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

import com.alipay.sofa.registry.util.OsUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author xiaojian.xj
 * @version : MultiClusterDataServerConfigBean.java, v 0.1 2022年05月09日 17:37 xiaojian.xj Exp $
 */
@ConfigurationProperties(prefix = MultiClusterDataServerConfigBean.PREFIX)
public class MultiClusterDataServerConfigBean implements MultiClusterDataServerConfig {

  public static final String PREFIX = "data.remote.server";

  private volatile int syncRemoteSlotLeaderIntervalSecs = 6;

  private volatile int syncRemoteSlotLeaderTimeoutMillis = 3000;

  private volatile int syncRemoteSlotLeaderPort = 9627;

  private volatile int syncRemoteSlotLeaderConnNum = 3;

  private volatile int remoteSyncSlotLeaderExecutorThreadSize = OsUtils.getCpuCount() * 10;

  private volatile int remoteSyncSlotLeaderExecutorQueueSize = 10000;

  private volatile int remoteSyncDataIdExecutorThreadSize = OsUtils.getCpuCount() * 3;

  private volatile int remoteSyncDataIdExecutorQueueSize = 1000;

  private volatile int remoteDataChangeExecutorThreadSize = OsUtils.getCpuCount() * 3;

  private volatile int remoteDataChangeExecutorQueueSize = 3000;

  private volatile int syncSlotLowWaterMark = 1024 * 256;
  private volatile int syncSlotHighWaterMark = 1024 * 320;

  private volatile int remoteSlotSyncRequestExecutorMinPoolSize = OsUtils.getCpuCount() * 3;

  private volatile int remoteSlotSyncRequestExecutorMaxPoolSize = OsUtils.getCpuCount() * 5;

  private volatile int remoteSlotSyncRequestExecutorQueueSize = 1000;

  private volatile int multiClusterConfigReloadMillis = 60 * 1000;

  @Override
  public int getSyncRemoteSlotLeaderIntervalSecs() {
    return syncRemoteSlotLeaderIntervalSecs;
  }

  @Override
  public int getSyncRemoteSlotLeaderTimeoutMillis() {
    return syncRemoteSlotLeaderTimeoutMillis;
  }

  @Override
  public int getSyncRemoteSlotLeaderPort() {
    return syncRemoteSlotLeaderPort;
  }

  @Override
  public int getSyncRemoteSlotLeaderConnNum() {
    return syncRemoteSlotLeaderConnNum;
  }

  @Override
  public int getRemoteSyncSlotLeaderExecutorThreadSize() {
    return remoteSyncSlotLeaderExecutorThreadSize;
  }

  @Override
  public int getRemoteSyncSlotLeaderExecutorQueueSize() {
    return remoteSyncSlotLeaderExecutorQueueSize;
  }

  /**
   * Setter method for property <tt>syncRemoteSlotLeaderIntervalSecs</tt>.
   *
   * @param syncRemoteSlotLeaderIntervalSecs value to be assigned to property
   *     syncRemoteSlotLeaderIntervalSecs
   */
  public void setSyncRemoteSlotLeaderIntervalSecs(int syncRemoteSlotLeaderIntervalSecs) {
    this.syncRemoteSlotLeaderIntervalSecs = syncRemoteSlotLeaderIntervalSecs;
  }

  /**
   * Setter method for property <tt>syncRemoteSlotLeaderTimeoutMillis</tt>.
   *
   * @param syncRemoteSlotLeaderTimeoutMillis value to be assigned to property
   *     syncRemoteSlotLeaderTimeoutMillis
   */
  public void setSyncRemoteSlotLeaderTimeoutMillis(int syncRemoteSlotLeaderTimeoutMillis) {
    this.syncRemoteSlotLeaderTimeoutMillis = syncRemoteSlotLeaderTimeoutMillis;
  }

  /**
   * Setter method for property <tt>syncRemoteSlotLeaderPort</tt>.
   *
   * @param syncRemoteSlotLeaderPort value to be assigned to property syncRemoteSlotLeaderPort
   */
  public void setSyncRemoteSlotLeaderPort(int syncRemoteSlotLeaderPort) {
    this.syncRemoteSlotLeaderPort = syncRemoteSlotLeaderPort;
  }

  /**
   * Setter method for property <tt>syncRemoteSlotLeaderConnNum</tt>.
   *
   * @param syncRemoteSlotLeaderConnNum value to be assigned to property syncRemoteSlotLeaderConnNum
   */
  public void setSyncRemoteSlotLeaderConnNum(int syncRemoteSlotLeaderConnNum) {
    this.syncRemoteSlotLeaderConnNum = syncRemoteSlotLeaderConnNum;
  }

  /**
   * Setter method for property <tt>remoteSyncSlotLeaderExecutorThreadSize</tt>.
   *
   * @param remoteSyncSlotLeaderExecutorThreadSize value to be assigned to property
   *     remoteSyncSlotLeaderExecutorThreadSize
   */
  public void setRemoteSyncSlotLeaderExecutorThreadSize(
      int remoteSyncSlotLeaderExecutorThreadSize) {
    this.remoteSyncSlotLeaderExecutorThreadSize = remoteSyncSlotLeaderExecutorThreadSize;
  }

  /**
   * Setter method for property <tt>remoteSyncSlotLeaderExecutorQueueSize</tt>.
   *
   * @param remoteSyncSlotLeaderExecutorQueueSize value to be assigned to property
   *     remoteSyncSlotLeaderExecutorQueueSize
   */
  public void setRemoteSyncSlotLeaderExecutorQueueSize(int remoteSyncSlotLeaderExecutorQueueSize) {
    this.remoteSyncSlotLeaderExecutorQueueSize = remoteSyncSlotLeaderExecutorQueueSize;
  }

  /**
   * Getter method for property <tt>syncSlotLowWaterMark</tt>.
   *
   * @return property value of syncSlotLowWaterMark
   */
  @Override
  public int getSyncSlotLowWaterMark() {
    return syncSlotLowWaterMark;
  }

  /**
   * Setter method for property <tt>syncSlotLowWaterMark</tt>.
   *
   * @param syncSlotLowWaterMark value to be assigned to property syncSlotLowWaterMark
   */
  public void setSyncSlotLowWaterMark(int syncSlotLowWaterMark) {
    this.syncSlotLowWaterMark = syncSlotLowWaterMark;
  }

  /**
   * Getter method for property <tt>syncSlotHighWaterMark</tt>.
   *
   * @return property value of syncSlotHighWaterMark
   */
  @Override
  public int getSyncSlotHighWaterMark() {
    return syncSlotHighWaterMark;
  }

  @Override
  public int getRemoteSlotSyncRequestExecutorMinPoolSize() {
    return remoteSlotSyncRequestExecutorMinPoolSize;
  }

  @Override
  public int getRemoteSlotSyncRequestExecutorMaxPoolSize() {
    return remoteSlotSyncRequestExecutorMaxPoolSize;
  }

  @Override
  public int getRemoteSlotSyncRequestExecutorQueueSize() {
    return remoteSlotSyncRequestExecutorQueueSize;
  }

  @Override
  public int getRemoteDataChangeExecutorThreadSize() {
    return remoteDataChangeExecutorThreadSize;
  }

  @Override
  public int getRemoteDataChangeExecutorQueueSize() {
    return remoteDataChangeExecutorQueueSize;
  }

  @Override
  public int getRemoteSyncDataIdExecutorThreadSize() {
    return remoteSyncDataIdExecutorThreadSize;
  }

  @Override
  public int getRemoteSyncDataIdExecutorQueueSize() {
    return remoteSyncDataIdExecutorQueueSize;
  }

  @Override
  public int getMultiClusterConfigReloadMillis() {
    return multiClusterConfigReloadMillis;
  }

  /**
   * Setter method for property <tt>multiClusterConfigReloadMillis</tt>.
   *
   * @param multiClusterConfigReloadMillis value to be assigned to property
   *     multiClusterConfigReloadMillis
   */
  public void setMultiClusterConfigReloadMillis(int multiClusterConfigReloadMillis) {
    this.multiClusterConfigReloadMillis = multiClusterConfigReloadMillis;
  }

  /**
   * Setter method for property <tt>remoteSyncDataIdExecutorThreadSize</tt>.
   *
   * @param remoteSyncDataIdExecutorThreadSize value to be assigned to property
   *     remoteSyncDataIdExecutorThreadSize
   */
  public void setRemoteSyncDataIdExecutorThreadSize(int remoteSyncDataIdExecutorThreadSize) {
    this.remoteSyncDataIdExecutorThreadSize = remoteSyncDataIdExecutorThreadSize;
  }

  /**
   * Setter method for property <tt>remoteSyncDataIdExecutorQueueSize</tt>.
   *
   * @param remoteSyncDataIdExecutorQueueSize value to be assigned to property
   *     remoteSyncDataIdExecutorQueueSize
   */
  public void setRemoteSyncDataIdExecutorQueueSize(int remoteSyncDataIdExecutorQueueSize) {
    this.remoteSyncDataIdExecutorQueueSize = remoteSyncDataIdExecutorQueueSize;
  }

  /**
   * Setter method for property <tt>remoteDataChangeExecutorThreadSize</tt>.
   *
   * @param remoteDataChangeExecutorThreadSize value to be assigned to property
   *     remoteDataChangeExecutorThreadSize
   */
  public void setRemoteDataChangeExecutorThreadSize(int remoteDataChangeExecutorThreadSize) {
    this.remoteDataChangeExecutorThreadSize = remoteDataChangeExecutorThreadSize;
  }

  /**
   * Setter method for property <tt>remoteDataChangeExecutorQueueSize</tt>.
   *
   * @param remoteDataChangeExecutorQueueSize value to be assigned to property
   *     remoteDataChangeExecutorQueueSize
   */
  public void setRemoteDataChangeExecutorQueueSize(int remoteDataChangeExecutorQueueSize) {
    this.remoteDataChangeExecutorQueueSize = remoteDataChangeExecutorQueueSize;
  }

  /**
   * Setter method for property <tt>remoteSlotSyncRequestExecutorMinPoolSize</tt>.
   *
   * @param remoteSlotSyncRequestExecutorMinPoolSize value to be assigned to property
   *     remoteSlotSyncRequestExecutorMinPoolSize
   */
  public void setRemoteSlotSyncRequestExecutorMinPoolSize(
      int remoteSlotSyncRequestExecutorMinPoolSize) {
    this.remoteSlotSyncRequestExecutorMinPoolSize = remoteSlotSyncRequestExecutorMinPoolSize;
  }

  /**
   * Setter method for property <tt>remoteSlotSyncRequestExecutorMaxPoolSize</tt>.
   *
   * @param remoteSlotSyncRequestExecutorMaxPoolSize value to be assigned to property
   *     remoteSlotSyncRequestExecutorMaxPoolSize
   */
  public void setRemoteSlotSyncRequestExecutorMaxPoolSize(
      int remoteSlotSyncRequestExecutorMaxPoolSize) {
    this.remoteSlotSyncRequestExecutorMaxPoolSize = remoteSlotSyncRequestExecutorMaxPoolSize;
  }

  /**
   * Setter method for property <tt>remoteSlotSyncRequestExecutorQueueSize</tt>.
   *
   * @param remoteSlotSyncRequestExecutorQueueSize value to be assigned to property
   *     remoteSlotSyncRequestExecutorQueueSize
   */
  public void setRemoteSlotSyncRequestExecutorQueueSize(
      int remoteSlotSyncRequestExecutorQueueSize) {
    this.remoteSlotSyncRequestExecutorQueueSize = remoteSlotSyncRequestExecutorQueueSize;
  }

  /**
   * Setter method for property <tt>syncSlotHighWaterMark</tt>.
   *
   * @param syncSlotHighWaterMark value to be assigned to property syncSlotHighWaterMark
   */
  public void setSyncSlotHighWaterMark(int syncSlotHighWaterMark) {
    this.syncSlotHighWaterMark = syncSlotHighWaterMark;
  }
}
