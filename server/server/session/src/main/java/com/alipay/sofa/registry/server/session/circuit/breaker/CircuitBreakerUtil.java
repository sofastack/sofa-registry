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
package com.alipay.sofa.registry.server.session.circuit.breaker;

import com.alipay.sofa.registry.common.model.store.CircuitBreakerStatistic;

/**
 * @author xiaojian.xj
 * @version $Id: CircuitBreakerUtil.java, v 0.1 2021年06月15日 21:43 xiaojian.xj Exp $
 */
public final class CircuitBreakerUtil {

  private CircuitBreakerUtil(){}

  public static boolean circuitBreaker(
      CircuitBreakerStatistic statistic, int counterThreshold, int sleepMillis) {
    if (statistic == null) {
      return false;
    }
    return statistic.getFailCount() > counterThreshold
        && System.currentTimeMillis() < statistic.getLastFailTimeStamp() + sleepMillis;
  }
}
