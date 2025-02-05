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

import com.alipay.sofa.registry.common.model.console.CircuitBreakerData;
import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.providedata.FetchCircuitBreakerService.CircuitBreakerStorage;
import com.alipay.sofa.registry.server.shared.providedata.AbstractFetchPersistenceSystemProperty;
import com.alipay.sofa.registry.server.shared.providedata.SystemDataStorage;
import com.alipay.sofa.registry.store.api.meta.ProvideDataRepository;
import com.alipay.sofa.registry.util.JsonUtils;
import java.util.Collections;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version : FetchCircuitBreakerService.java, v 0.1 2022年01月21日 14:30 xiaojian.xj Exp $
 */
public class FetchCircuitBreakerService
    extends AbstractFetchPersistenceSystemProperty<CircuitBreakerStorage, CircuitBreakerStorage> {

  private static final Logger LOGGER = LoggerFactory.getLogger(FetchCircuitBreakerService.class);

  @Autowired private SessionServerConfig sessionServerConfig;

  @Autowired private ProvideDataRepository provideDataRepository;

  private static final CircuitBreakerStorage INIT =
      new CircuitBreakerStorage(
          INIT_VERSION, false, Collections.emptySet(), Collections.emptySet());

  public FetchCircuitBreakerService() {
    super(ValueConstants.CIRCUIT_BREAKER_DATA_ID, INIT);
  }

  @Override
  protected int getSystemPropertyIntervalMillis() {
    return sessionServerConfig.getSystemPropertyIntervalMillis();
  }

  @Override
  protected CircuitBreakerStorage fetchFromPersistence() {
    PersistenceData persistenceData =
        provideDataRepository.get(ValueConstants.CIRCUIT_BREAKER_DATA_ID);
    if (persistenceData == null) {
      return INIT;
    }
    CircuitBreakerData read = JsonUtils.read(persistenceData.getData(), CircuitBreakerData.class);
    return new CircuitBreakerStorage(
        persistenceData.getVersion(),
        read.addressSwitch(INIT.addressSwitch),
        read.getAddress(),
        read.getOverflowAddress());
  }

  @Override
  protected boolean doProcess(CircuitBreakerStorage expect, CircuitBreakerStorage update) {

    try {
      if (!compareAndSet(expect, update)) {
        LOGGER.error("update circuit breaker address:{} fail.", update);
        return false;
      }
      LOGGER.info("Fetch circuit breaker data, prev={}, current={}", expect, update);
    } catch (Throwable t) {
      LOGGER.error("update circuit breaker address:{} error.", update, t);
    }
    return true;
  }

  public static class CircuitBreakerStorage extends SystemDataStorage {

    final boolean addressSwitch;
    final Set<String> address;
    final Set<String> overflowAddress;

    public CircuitBreakerStorage(
        long version, boolean addressSwitch, Set<String> address, Set<String> overflowAddress) {
      super(version);
      this.addressSwitch = addressSwitch;
      this.address = address;
      this.overflowAddress = overflowAddress;
    }

    @Override
    public String toString() {
      return "CircuitBreakerStorage{"
          + "addressSwitch="
          + addressSwitch
          + ", address="
          + address
          + ", overflowAddress="
          + overflowAddress
          + '}';
    }
  }

  public Set<String> getStopPushCircuitBreaker() {
    return this.storage.get().address;
  }

  public Set<String> getOverflowAddress() {
    return this.storage.get().overflowAddress;
  }

  public boolean isSwitchOpen() {
    return this.storage.get().addressSwitch;
  }
}
