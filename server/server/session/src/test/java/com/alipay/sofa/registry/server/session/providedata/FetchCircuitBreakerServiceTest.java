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

import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.console.CircuitBreakerData;
import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.console.PersistenceDataBuilder;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.server.session.AbstractSessionServerTestBase;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.JsonUtils;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.util.CollectionUtils;

/**
 * @author xiaojian.xj
 * @version : FetchCircuitBreakerServiceTest.java, v 0.1 2022年01月21日 14:32 xiaojian.xj Exp $
 */
@RunWith(MockitoJUnitRunner.class)
public class FetchCircuitBreakerServiceTest extends AbstractSessionServerTestBase {
  @InjectMocks private FetchCircuitBreakerService fetchCircuitBreakerService;

  @Mock private SessionServerConfig sessionServerConfig;

  @Spy private InMemoryProvideDataRepository provideDataRepository;

  private static final int SYSTEM_PROPERTY_INTERVAL_MILLIS = 100;

  private static final int MAX_WAIT = SYSTEM_PROPERTY_INTERVAL_MILLIS * 5;

  @Before
  public void before() {
    when(sessionServerConfig.getSystemPropertyIntervalMillis())
        .thenReturn(SYSTEM_PROPERTY_INTERVAL_MILLIS);
  }

  @Test
  public void testFetchCircuitBreaker() throws InterruptedException, TimeoutException {
    // 1.init
    boolean ret = fetchCircuitBreakerService.start();
    ConcurrentUtils.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
    Assert.assertTrue(ret);
    waitConditionUntilTimeOut(() -> !fetchCircuitBreakerService.isSwitchOpen(), MAX_WAIT);
    waitConditionUntilTimeOut(
        () -> CollectionUtils.isEmpty(fetchCircuitBreakerService.getStopPushCircuitBreaker()),
        MAX_WAIT);

    // 2.switch open
    PersistenceData persistenceData =
        PersistenceDataBuilder.createPersistenceData(
            ValueConstants.CIRCUIT_BREAKER_DATA_ID,
            JsonUtils.writeValueAsString(new CircuitBreakerData(true, Collections.EMPTY_SET)));

    boolean put = provideDataRepository.put(persistenceData);
    Assert.assertTrue(put);
    waitConditionUntilTimeOut(() -> fetchCircuitBreakerService.isSwitchOpen(), MAX_WAIT);
    waitConditionUntilTimeOut(
        () -> CollectionUtils.isEmpty(fetchCircuitBreakerService.getStopPushCircuitBreaker()),
        MAX_WAIT);

    // 3.add
    HashSet<String> address = Sets.newHashSet("1.1.1.1", "2.2.2.2");
    persistenceData =
        PersistenceDataBuilder.createPersistenceData(
            ValueConstants.CIRCUIT_BREAKER_DATA_ID,
            JsonUtils.writeValueAsString(new CircuitBreakerData(true, address)));
    put = provideDataRepository.put(persistenceData);
    Assert.assertTrue(put);
    waitConditionUntilTimeOut(() -> fetchCircuitBreakerService.isSwitchOpen(), MAX_WAIT);
    waitConditionUntilTimeOut(
        () -> fetchCircuitBreakerService.getStopPushCircuitBreaker().size() == 2, MAX_WAIT);
    waitConditionUntilTimeOut(
        () -> address.equals(fetchCircuitBreakerService.getStopPushCircuitBreaker()), MAX_WAIT);

    // 4.remove
    address.remove("1.1.1.1");
    persistenceData =
        PersistenceDataBuilder.createPersistenceData(
            ValueConstants.CIRCUIT_BREAKER_DATA_ID,
            JsonUtils.writeValueAsString(new CircuitBreakerData(true, address)));
    put = provideDataRepository.put(persistenceData);
    Assert.assertTrue(put);
    waitConditionUntilTimeOut(() -> fetchCircuitBreakerService.isSwitchOpen(), MAX_WAIT);
    waitConditionUntilTimeOut(
        () -> fetchCircuitBreakerService.getStopPushCircuitBreaker().size() == 1, MAX_WAIT);
    waitConditionUntilTimeOut(
        () -> address.equals(fetchCircuitBreakerService.getStopPushCircuitBreaker()), MAX_WAIT);

    // 5.switch close
    persistenceData =
        PersistenceDataBuilder.createPersistenceData(
            ValueConstants.CIRCUIT_BREAKER_DATA_ID,
            JsonUtils.writeValueAsString(new CircuitBreakerData(false, address)));
    put = provideDataRepository.put(persistenceData);
    Assert.assertTrue(put);
    waitConditionUntilTimeOut(() -> !fetchCircuitBreakerService.isSwitchOpen(), MAX_WAIT);
    waitConditionUntilTimeOut(
        () -> fetchCircuitBreakerService.getStopPushCircuitBreaker().size() == 1, MAX_WAIT);
    waitConditionUntilTimeOut(
        () -> address.equals(fetchCircuitBreakerService.getStopPushCircuitBreaker()), MAX_WAIT);
  }
}
