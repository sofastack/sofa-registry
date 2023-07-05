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

import com.alipay.sofa.registry.cache.CacheCleaner;
import com.alipay.sofa.registry.common.model.store.CircuitBreakerStatistic;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.providedata.FetchCircuitBreakerService;
import com.alipay.sofa.registry.server.session.push.PushLog;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version $Id: DefaultCircuitBreakerService.java, v 0.1 2021年06月15日 21:43 xiaojian.xj Exp $
 */
public class DefaultCircuitBreakerService implements CircuitBreakerService {

  private static final Logger LOGGER = PushLog.LOGGER;

  @Autowired private SessionServerConfig sessionServerConfig;

  @Autowired protected FetchCircuitBreakerService fetchCircuitBreakerService;

  private final Cache<String, CircuitBreakerStatistic> circuitBreakerAddress =
      CacheBuilder.newBuilder().maximumSize(2000L).expireAfterAccess(10, TimeUnit.MINUTES).build();

  public DefaultCircuitBreakerService() {
    CacheCleaner.autoClean(circuitBreakerAddress, 1000 * 60 * 10);
  }

  /**
   * check if statistic should be circuit break
   *
   * @param statistic statistic
   * @param hasPushed hasPushed
   * @return boolean
   */
  @Override
  public boolean pushCircuitBreaker(CircuitBreakerStatistic statistic, boolean hasPushed) {
    if (statistic == null) {
      return false;
    }

    // not circuit break on sub.register
    if (!hasPushed) {
      return false;
    }

    if (fetchCircuitBreakerService.isSwitchOpen()) {
      return addressCircuitBreak(statistic);
    }
    int silenceMillis = sessionServerConfig.getPushCircuitBreakerSilenceMillis();
    return statistic.circuitBreak(
        sessionServerConfig.getPushCircuitBreakerThreshold(), silenceMillis);
  }

  protected boolean addressCircuitBreak(CircuitBreakerStatistic statistic) {
    if (fetchCircuitBreakerService.getStopPushCircuitBreaker().contains(statistic.getIp())) {
      LOGGER.info("[ArtificialCircuitBreaker]push check circuit break, statistic:{}", statistic);
      return true;
    }
    int silenceMillis = sessionServerConfig.getPushCircuitBreakerSilenceMillis();
    CircuitBreakerStatistic addressStatistic =
        circuitBreakerAddress.getIfPresent(statistic.getAddress());
    if (addressStatistic != null) {
      boolean addressCircuitBreak =
          addressStatistic.circuitBreak(
              sessionServerConfig.getPushAddressCircuitBreakerThreshold(), silenceMillis);
      boolean subCircuitBreak =
          statistic.circuitBreak(
              sessionServerConfig.getPushCircuitBreakerThreshold(), silenceMillis);
      LOGGER.info(
          "[addressCircuitBreak]addressCircuitBreak: {}, addressStatistic:{}, subCircuitBreak:{}, subStatistic:{}",
          addressCircuitBreak,
          addressStatistic,
          subCircuitBreak,
          statistic);
      return addressCircuitBreak || subCircuitBreak;
    }

    return statistic.circuitBreak(
        sessionServerConfig.getPushCircuitBreakerThreshold(), silenceMillis);
  }

  /**
   * statistic when push fail
   *
   * @param versions versions
   * @param subscriber subscriber
   * @return boolean
   */
  public boolean onPushFail(Map<String, Long> versions, Subscriber subscriber) {

    if (!subscriber.hasPushed()) {
      // push fail on register, not circuit breaker;
      return true;
    }
    String ip = subscriber.getSourceAddress().getIpAddress();
    String address = subscriber.getSourceAddress().buildAddressString();
    if (!subscriber.onPushFail(versions)) {
      LOGGER.info("PushN, failed to do onPushFail, {}, {}", subscriber.getDataInfoId(), address);
      return false;
    }

    try {
      CircuitBreakerStatistic statistic =
          circuitBreakerAddress.get(
              address, () -> new CircuitBreakerStatistic(subscriber.getGroup(), ip, address));
      statistic.fail();
      LOGGER.info(
          "PushN, dataInfoId={}, inc circuit statistic={}", subscriber.getDataInfoId(), statistic);
      return true;
    } catch (Throwable e) {
      LOGGER.error("[onPushFail]get from circuitBreakerAddress error.", e);
      return false;
    }
  }

  /**
   * statistic when push success
   *
   * @param versions dataCenter version
   * @param pushNums dataCenter pushNum
   * @param subscriber subscriber
   * @return boolean
   */
  public boolean onPushSuccess(
      Map<String, Long> versions, Map<String, Integer> pushNums, Subscriber subscriber) {
    String address = subscriber.getSourceAddress().buildAddressString();

    CircuitBreakerStatistic statistic = circuitBreakerAddress.getIfPresent(address);
    if (statistic != null) {
      int threshold = sessionServerConfig.getPushConsecutiveSuccess();
      statistic.success(threshold);
      if (statistic.getConsecutiveSuccess() >= threshold) {
        circuitBreakerAddress.invalidate(address);
        LOGGER.info("PushY, invalidate circuit statistic: {}", statistic);
      }
    }

    if (subscriber.checkAndUpdateCtx(versions, pushNums)) {
      LOGGER.info(
          "PushY, checkAndUpdateCtx onPushSuccess, {}, {}, versions={}",
          subscriber.getDataInfoId(),
          address,
          versions);
      return true;
    } else {
      LOGGER.info(
          "PushN, failed to checkAndUpdateCtx onPushSuccess, {}, {}",
          subscriber.getDataInfoId(),
          address);
      return false;
    }
  }

  /**
   * Setter method for property <tt>fetchCircuitBreakerService</tt>.
   *
   * @param fetchCircuitBreakerService value to be assigned to property fetchCircuitBreakerService
   */
  @VisibleForTesting
  public void setFetchCircuitBreakerService(FetchCircuitBreakerService fetchCircuitBreakerService) {
    this.fetchCircuitBreakerService = fetchCircuitBreakerService;
  }
}
