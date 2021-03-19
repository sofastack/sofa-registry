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
package com.alipay.sofa.registry.server.session.strategy.impl;

import com.alipay.sofa.registry.core.model.BaseRegister;
import com.alipay.sofa.registry.core.model.RegisterResponse;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.exchange.RequestChannelClosedException;

public final class RegisterLogs {
  public static final Logger REGISTER_LOGGER = LoggerFactory.getLogger("REGISTER");

  private RegisterLogs() {}

  public static void logError(
      BaseRegister register, String name, RegisterResponse registerResponse, Throwable e) {
    if (e instanceof RequestChannelClosedException) {
      REGISTER_LOGGER.warn(
          "{} register error, Type {}, {}", name, register.getEventType(), e.getMessage());
    } else {
      REGISTER_LOGGER.error("{} register error, Type {}", name, register.getEventType(), e);
    }
    registerResponse.setSuccess(false);
    registerResponse.setMessage(name + " register failed!Type:" + register.getEventType());
  }
}
