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
package com.alipay.sofa.registry.server.session.limit;

import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.google.common.util.concurrent.RateLimiter;

/**
 * @author shangyu.wh
 * @version 1.0: AccessLimitServiceImpl.java, v 0.1 2019-08-26 20:37 shangyu.wh Exp $
 */
public class AccessLimitServiceImpl implements AccessLimitService {

  private RateLimiter rateLimiter;

  public AccessLimitServiceImpl(SessionServerConfig sessionServerConfig) {
    rateLimiter = RateLimiter.create(sessionServerConfig.getAccessLimitRate());
  }

  @Override
  public boolean tryAcquire() {
    return rateLimiter.tryAcquire();
  }
}
