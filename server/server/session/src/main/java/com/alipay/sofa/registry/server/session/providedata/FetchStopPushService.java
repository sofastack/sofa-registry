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
package com.alipay.sofa.registry.server.session.providedata;

import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.shared.providedata.BaseStopPushService;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version $Id: FetchStopPushService.java, v 0.1 2021年05月16日 17:48 xiaojian.xj Exp $
 */
public class FetchStopPushService extends BaseStopPushService {

  @Autowired private SessionServerConfig sessionServerConfig;

  @Override
  protected int getSystemPropertyIntervalMillis() {
    return sessionServerConfig.getSystemPropertyIntervalMillis();
  }

  @Override
  protected void afterProcess(StopPushStorage storage) {}

  /**
   * Setter method for property <tt>sessionServerConfig</tt>.
   *
   * @param sessionServerConfig value to be assigned to property sessionServerConfig
   * @return FetchStopPushService
   */
  @VisibleForTesting
  protected FetchStopPushService setSessionServerConfig(SessionServerConfig sessionServerConfig) {
    this.sessionServerConfig = sessionServerConfig;
    return this;
  }
}
