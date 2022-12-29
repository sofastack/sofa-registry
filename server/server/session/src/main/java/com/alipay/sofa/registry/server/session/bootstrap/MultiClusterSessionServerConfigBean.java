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

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author xiaojian.xj
 * @version : MultiClusterSessionServerConfigBean.java, v 0.1 2022年07月29日 17:13 xiaojian.xj Exp $
 */
@ConfigurationProperties(prefix = MultiClusterSessionServerConfigBean.PREFIX)
public class MultiClusterSessionServerConfigBean implements MultiClusterSessionServerConfig {

  /** The constant PREFIX. */
  public static final String PREFIX = "session.remote.server";

  private volatile int multiClusterConfigReloadSecs = 60;

  @Override
  public long getMultiClusterConfigReloadSecs() {
    return multiClusterConfigReloadSecs;
  }

  /**
   * Setter method for property <tt>multiClusterConfigReloadSecs</tt>.
   *
   * @param multiClusterConfigReloadSecs value to be assigned to property
   *     multiClusterConfigReloadSecs
   */
  public void setMultiClusterConfigReloadSecs(int multiClusterConfigReloadSecs) {
    this.multiClusterConfigReloadSecs = multiClusterConfigReloadSecs;
  }
}
