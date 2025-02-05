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
package com.alipay.sofa.registry.jdbc.config;

import com.alipay.sofa.registry.util.OsUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author xiaojian.xj
 * @version $Id: MetadataConfigBean.java, v 0.1 2021年02月24日 15:21 xiaojian.xj Exp $
 */
@ConfigurationProperties(prefix = MetadataConfigBean.PRE_FIX)
public class MetadataConfigBean implements MetadataConfig {

  public static final String PRE_FIX = "metadata.server";

  private int revisionRenewIntervalMinutes = 60 * 3;
  private int interfaceAppsIndexRenewIntervalMinutes = 60 * 3;

  private int interfaceAppsExecutorPoolSize = OsUtils.getCpuCount() * 3;
  private int interfaceAppsExecutorQueueSize = 1000;

  private int clientManagerExecutorPoolSize = OsUtils.getCpuCount() * 6;
  private int clientManagerExecutorQueueSize = 3000;

  public int getRevisionRenewIntervalMinutes() {
    return revisionRenewIntervalMinutes;
  }

  public void setRevisionRenewIntervalMinutes(int revisionRenewIntervalMinutes) {
    this.revisionRenewIntervalMinutes = revisionRenewIntervalMinutes;
  }

  public int getInterfaceAppsIndexRenewIntervalMinutes() {
    return interfaceAppsIndexRenewIntervalMinutes;
  }

  public void setInterfaceAppsIndexRenewIntervalMinutes(
      int interfaceAppsIndexRenewIntervalMinutes) {
    this.interfaceAppsIndexRenewIntervalMinutes = interfaceAppsIndexRenewIntervalMinutes;
  }

  @Override
  public int getInterfaceAppsExecutorPoolSize() {
    return interfaceAppsExecutorPoolSize;
  }

  @Override
  public int getInterfaceAppsExecutorQueueSize() {
    return interfaceAppsExecutorQueueSize;
  }

  @Override
  public int getClientManagerExecutorPoolSize() {
    return clientManagerExecutorPoolSize;
  }

  @Override
  public int getClientManagerExecutorQueueSize() {
    return clientManagerExecutorQueueSize;
  }

  /**
   * Setter method for property <tt>clientManagerExecutorQueueSize</tt>.
   *
   * @param clientManagerExecutorQueueSize value to be assigned to property
   *     clientManagerExecutorQueueSize
   */
  public void setClientManagerExecutorQueueSize(int clientManagerExecutorQueueSize) {
    this.clientManagerExecutorQueueSize = clientManagerExecutorQueueSize;
  }

  /**
   * Setter method for property <tt>interfaceAppsExecutorPoolSize</tt>.
   *
   * @param interfaceAppsExecutorPoolSize value to be assigned to property
   *     interfaceAppsExecutorPoolSize
   */
  public void setInterfaceAppsExecutorPoolSize(int interfaceAppsExecutorPoolSize) {
    this.interfaceAppsExecutorPoolSize = interfaceAppsExecutorPoolSize;
  }

  /**
   * Setter method for property <tt>interfaceAppsExecutorQueueSize</tt>.
   *
   * @param interfaceAppsExecutorQueueSize value to be assigned to property
   *     interfaceAppsExecutorQueueSize
   */
  public void setInterfaceAppsExecutorQueueSize(int interfaceAppsExecutorQueueSize) {
    this.interfaceAppsExecutorQueueSize = interfaceAppsExecutorQueueSize;
  }

  /**
   * Setter method for property <tt>clientManagerExecutorPoolSize</tt>.
   *
   * @param clientManagerExecutorPoolSize value to be assigned to property
   *     clientManagerExecutorPoolSize
   */
  public void setClientManagerExecutorPoolSize(int clientManagerExecutorPoolSize) {
    this.clientManagerExecutorPoolSize = clientManagerExecutorPoolSize;
  }
}
