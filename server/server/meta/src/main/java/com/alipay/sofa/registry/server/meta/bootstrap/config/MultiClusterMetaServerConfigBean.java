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

import com.alipay.sofa.registry.util.OsUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author xiaojian.xj
 * @version : MultiClusterMetaServerConfigBean.java, v 0.1 2022年04月21日 22:05 xiaojian.xj Exp $
 */
@ConfigurationProperties(prefix = MultiClusterMetaServerConfigBean.PREFIX)
public class MultiClusterMetaServerConfigBean implements MultiClusterMetaServerConfig {
  public static final String PREFIX = "meta.remote.server";

  private volatile int remoteMetaServerPort = 9617;

  private volatile int remoteClusterRpcTimeoutMillis = 3000;

  private volatile int multiClusterConfigReloadMillis = 60 * 1000;

  private volatile int multiClusterConfigReloadWorkerSize = 1;

  private volatile int multiClusterConfigReloadMaxBufferSize = 5;

  private volatile int remoteSlotSyncerMillis = 1000;

  private volatile int remoteSlotSyncerExecutorPoolSize = 10;

  private volatile int remoteSlotSyncerExecutorQueueSize = 100;

  private volatile int remoteClusterHandlerCoreSize = OsUtils.getCpuCount() * 3;

  private volatile int remoteClusterHandlerMaxSize = OsUtils.getCpuCount() * 3;

  private volatile int remoteClusterHandlerMaxBufferSize = 100;

  private volatile int clientManagerCoreSize = OsUtils.getCpuCount() * 3;

  private volatile int clientManagerMaxSize = OsUtils.getCpuCount() * 3;

  private volatile int clientManagerMaxBufferSize = 1000;

  @Override
  public int getRemoteSlotSyncerMillis() {
    return remoteSlotSyncerMillis;
  }

  /**
   * Getter method for property <tt>remoteClusterRpcTimeoutMillis</tt>.
   *
   * @return property value of remoteClusterRpcTimeoutMillis
   */
  @Override
  public int getRemoteClusterRpcTimeoutMillis() {
    return remoteClusterRpcTimeoutMillis;
  }

  /**
   * Setter method for property <tt>remoteClusterRpcTimeoutMillis</tt>.
   *
   * @param remoteClusterRpcTimeoutMillis value to be assigned to property
   *     remoteClusterRpcTimeoutMillis
   */
  public void setRemoteClusterRpcTimeoutMillis(int remoteClusterRpcTimeoutMillis) {
    this.remoteClusterRpcTimeoutMillis = remoteClusterRpcTimeoutMillis;
  }

  /**
   * Getter method for property <tt>RemoteMetaServerPort</tt>.
   *
   * @return property value of RemoteMetaServerPort
   */
  @Override
  public int getRemoteMetaServerPort() {
    return remoteMetaServerPort;
  }

  @Override
  public int getMultiClusterConfigReloadMillis() {
    return multiClusterConfigReloadMillis;
  }

  @Override
  public int getMultiClusterConfigReloadWorkerSize() {
    return multiClusterConfigReloadWorkerSize;
  }

  @Override
  public int getMultiClusterConfigReloadMaxBufferSize() {
    return multiClusterConfigReloadMaxBufferSize;
  }

  @Override
  public int getRemoteClusterHandlerCoreSize() {
    return remoteClusterHandlerCoreSize;
  }

  @Override
  public int getRemoteClusterHandlerMaxSize() {
    return remoteClusterHandlerMaxSize;
  }

  @Override
  public int getRemoteClusterHandlerMaxBufferSize() {
    return remoteClusterHandlerMaxBufferSize;
  }

  @Override
  public int getRemoteSlotSyncerExecutorPoolSize() {
    return remoteSlotSyncerExecutorPoolSize;
  }

  @Override
  public int getRemoteSlotSyncerExecutorQueueSize() {
    return remoteSlotSyncerExecutorQueueSize;
  }

  /**
   * Setter method for property <tt>remoteMetaServerPort</tt>.
   *
   * @param remoteMetaServerPort value to be assigned to property RemoteMetaServerPort
   */
  public void setRemoteMetaServerPort(int remoteMetaServerPort) {
    this.remoteMetaServerPort = remoteMetaServerPort;
  }

  /**
   * Setter method for property <tt>remoteSlotSyncerExecutorPoolSize</tt>.
   *
   * @param remoteSlotSyncerExecutorPoolSize value to be assigned to property
   *     remoteSlotSyncerExecutorPoolSize
   */
  public void setRemoteSlotSyncerExecutorPoolSize(int remoteSlotSyncerExecutorPoolSize) {
    this.remoteSlotSyncerExecutorPoolSize = remoteSlotSyncerExecutorPoolSize;
  }

  /**
   * Setter method for property <tt>remoteSlotSyncerExecutorQueueSize</tt>.
   *
   * @param remoteSlotSyncerExecutorQueueSize value to be assigned to property
   *     remoteSlotSyncerExecutorQueueSize
   */
  public void setRemoteSlotSyncerExecutorQueueSize(int remoteSlotSyncerExecutorQueueSize) {
    this.remoteSlotSyncerExecutorQueueSize = remoteSlotSyncerExecutorQueueSize;
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
   * Setter method for property <tt>multiClusterConfigReloadWorkerSize</tt>.
   *
   * @param multiClusterConfigReloadWorkerSize value to be assigned to property
   *     multiClusterConfigReloadWorkerSize
   */
  public void setMultiClusterConfigReloadWorkerSize(int multiClusterConfigReloadWorkerSize) {
    this.multiClusterConfigReloadWorkerSize = multiClusterConfigReloadWorkerSize;
  }

  /**
   * Setter method for property <tt>multiClusterConfigReloadMaxBufferSize</tt>.
   *
   * @param multiClusterConfigReloadMaxBufferSize value to be assigned to property
   *     multiClusterConfigReloadMaxBufferSize
   */
  public void setMultiClusterConfigReloadMaxBufferSize(int multiClusterConfigReloadMaxBufferSize) {
    this.multiClusterConfigReloadMaxBufferSize = multiClusterConfigReloadMaxBufferSize;
  }

  /**
   * Setter method for property <tt>remoteSlotSyncerMillis</tt>.
   *
   * @param remoteSlotSyncerMillis value to be assigned to property remoteSlotSyncerMillis
   */
  public void setRemoteSlotSyncerMillis(int remoteSlotSyncerMillis) {
    this.remoteSlotSyncerMillis = remoteSlotSyncerMillis;
  }

  /**
   * Setter method for property <tt>remoteClusterHandlerCoreSize</tt>.
   *
   * @param remoteClusterHandlerCoreSize value to be assigned to property
   *     remoteClusterHandlerCoreSize
   */
  public void setRemoteClusterHandlerCoreSize(int remoteClusterHandlerCoreSize) {
    this.remoteClusterHandlerCoreSize = remoteClusterHandlerCoreSize;
  }

  /**
   * Setter method for property <tt>remoteClusterHandlerMaxSize</tt>.
   *
   * @param remoteClusterHandlerMaxSize value to be assigned to property remoteClusterHandlerMaxSize
   */
  public void setRemoteClusterHandlerMaxSize(int remoteClusterHandlerMaxSize) {
    this.remoteClusterHandlerMaxSize = remoteClusterHandlerMaxSize;
  }

  /**
   * Setter method for property <tt>remoteClusterHandlerMaxBufferSize</tt>.
   *
   * @param remoteClusterHandlerMaxBufferSize value to be assigned to property
   *     remoteClusterHandlerMaxBufferSize
   */
  public void setRemoteClusterHandlerMaxBufferSize(int remoteClusterHandlerMaxBufferSize) {
    this.remoteClusterHandlerMaxBufferSize = remoteClusterHandlerMaxBufferSize;
  }

  /**
   * Getter method for property <tt>clientManagerCoreSize</tt>.
   *
   * @return property value of clientManagerCoreSize
   */
  @Override
  public int getClientManagerCoreSize() {
    return clientManagerCoreSize;
  }

  /**
   * Setter method for property <tt>clientManagerCoreSize</tt>.
   *
   * @param clientManagerCoreSize value to be assigned to property clientManagerCoreSize
   */
  public void setClientManagerCoreSize(int clientManagerCoreSize) {
    this.clientManagerCoreSize = clientManagerCoreSize;
  }

  /**
   * Getter method for property <tt>clientManagerMaxSize</tt>.
   *
   * @return property value of clientManagerMaxSize
   */
  @Override
  public int getClientManagerMaxSize() {
    return clientManagerMaxSize;
  }

  /**
   * Setter method for property <tt>clientManagerMaxSize</tt>.
   *
   * @param clientManagerMaxSize value to be assigned to property clientManagerMaxSize
   */
  public void setClientManagerMaxSize(int clientManagerMaxSize) {
    this.clientManagerMaxSize = clientManagerMaxSize;
  }

  /**
   * Getter method for property <tt>clientManagerMaxBufferSize</tt>.
   *
   * @return property value of clientManagerMaxBufferSize
   */
  @Override
  public int getClientManagerMaxBufferSize() {
    return clientManagerMaxBufferSize;
  }

  /**
   * Setter method for property <tt>clientManagerMaxBufferSize</tt>.
   *
   * @param clientManagerMaxBufferSize value to be assigned to property clientManagerMaxBufferSize
   */
  public void setClientManagerMaxBufferSize(int clientManagerMaxBufferSize) {
    this.clientManagerMaxBufferSize = clientManagerMaxBufferSize;
  }
}
