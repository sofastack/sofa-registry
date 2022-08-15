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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.console.CircuitBreakerData;
import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.console.PersistenceDataBuilder;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.server.session.AbstractSessionServerTestBase;
import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.providedata.FetchCircuitBreakerService;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.JsonUtils;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
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
 * @version : CircuitBreakerServiceTest.java, v 0.1 2022年01月21日 14:31 xiaojian.xj Exp $
 */
@RunWith(MockitoJUnitRunner.class)
public class CircuitBreakerServiceTest extends AbstractSessionServerTestBase {

  @InjectMocks private DefaultCircuitBreakerService circuitBreakerService;

  @InjectMocks private FetchCircuitBreakerService fetchCircuitBreakerService;

  @Mock private SessionServerConfig sessionServerConfig;

  @Spy private InMemoryProvideDataRepository provideDataRepository;

  private static final String TEST_CELL = "testCell";

  private static final String TEST_DATA_CENTER = "testDataCenter";

  private static final int CIRCUIT_BREAKER_THRESHOLD = 10;

  private static final int PUSH_CIRCUIT_BREAKER_SLEEP_MILLIS = 200;

  private static final int PUSH_CONSECUTIVE_SUCCESS = 3;

  private static final String DATA_INFO_ID = "CircuitBreakerServiceTest";

  private static final AtomicLong PUSH_VERSION = new AtomicLong(0);

  private static final int SYSTEM_PROPERTY_INTERVAL_MILLIS = 100;

  private static final int MAX_WAIT = SYSTEM_PROPERTY_INTERVAL_MILLIS * 5;

  @Before
  public void before() {
    when(sessionServerConfig.getSystemPropertyIntervalMillis())
        .thenReturn(SYSTEM_PROPERTY_INTERVAL_MILLIS);
    when(sessionServerConfig.getPushCircuitBreakerSilenceMillis())
        .thenReturn(PUSH_CIRCUIT_BREAKER_SLEEP_MILLIS);
    when(sessionServerConfig.getPushCircuitBreakerThreshold())
        .thenReturn(CIRCUIT_BREAKER_THRESHOLD);
    when(sessionServerConfig.getPushAddressCircuitBreakerThreshold())
        .thenReturn(2 * CIRCUIT_BREAKER_THRESHOLD);
    when(sessionServerConfig.getPushConsecutiveSuccess()).thenReturn(PUSH_CONSECUTIVE_SUCCESS);
    circuitBreakerService.setFetchCircuitBreakerService(fetchCircuitBreakerService);
  }

  @Test
  public void testPushCircuitBreaker() {

    Subscriber subscriber = TestUtils.newZoneSubscriber(DATA_INFO_ID, TEST_CELL);
    circuitBreakerService.onPushSuccess(
        Collections.singletonMap(TEST_DATA_CENTER, PUSH_VERSION.incrementAndGet()),
        Collections.singletonMap(TEST_DATA_CENTER, 1),
        subscriber);

    // 1.init switch
    boolean ret = fetchCircuitBreakerService.start();
    ConcurrentUtils.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
    Assert.assertTrue(ret);

    ret = circuitBreakerService.pushCircuitBreaker(null, true);
    Assert.assertFalse(ret);

    // 2.subscriber circuit breaker
    ret = circuitBreakerService.pushCircuitBreaker(subscriber.getStatistic(), true);
    Assert.assertFalse(ret);
    verify(sessionServerConfig, times(0)).getPushAddressCircuitBreakerThreshold();

    for (int i = 0; i < CIRCUIT_BREAKER_THRESHOLD; i++) {
      circuitBreakerService.onPushFail(
          Collections.singletonMap(TEST_DATA_CENTER, PUSH_VERSION.incrementAndGet()), subscriber);
    }
    ret = circuitBreakerService.pushCircuitBreaker(subscriber.getStatistic(), true);
    Assert.assertFalse(ret);
    verify(sessionServerConfig, times(0)).getPushAddressCircuitBreakerThreshold();

    circuitBreakerService.onPushFail(
        Collections.singletonMap(TEST_DATA_CENTER, PUSH_VERSION.incrementAndGet()), subscriber);
    ret = circuitBreakerService.pushCircuitBreaker(subscriber.getStatistic(), true);
    Assert.assertTrue(ret);
    verify(sessionServerConfig, times(0)).getPushAddressCircuitBreakerThreshold();

    // 3.after circuit breaker silence
    ConcurrentUtils.sleepUninterruptibly(PUSH_CIRCUIT_BREAKER_SLEEP_MILLIS, TimeUnit.MILLISECONDS);
    ret = circuitBreakerService.pushCircuitBreaker(subscriber.getStatistic(), true);
    Assert.assertFalse(ret);
    verify(sessionServerConfig, times(0)).getPushAddressCircuitBreakerThreshold();

    // 4.subscriber circuit breaker
    circuitBreakerService.onPushFail(
        Collections.singletonMap(TEST_DATA_CENTER, PUSH_VERSION.incrementAndGet()), subscriber);
    ret = circuitBreakerService.pushCircuitBreaker(subscriber.getStatistic(), true);
    Assert.assertTrue(ret);
    verify(sessionServerConfig, times(0)).getPushAddressCircuitBreakerThreshold();

    // 5.after success
    circuitBreakerService.onPushSuccess(
        Collections.singletonMap(TEST_DATA_CENTER, PUSH_VERSION.incrementAndGet()),
        Collections.singletonMap(TEST_DATA_CENTER, 1),
        subscriber);
    for (int i = 0; i < CIRCUIT_BREAKER_THRESHOLD; i++) {
      ret =
          circuitBreakerService.onPushFail(
              Collections.singletonMap(TEST_DATA_CENTER, PUSH_VERSION.incrementAndGet()),
              subscriber);
      Assert.assertTrue(ret);
      ret = circuitBreakerService.pushCircuitBreaker(subscriber.getStatistic(), true);
      Assert.assertFalse(ret);
      verify(sessionServerConfig, times(0)).getPushAddressCircuitBreakerThreshold();
    }

    circuitBreakerService.onPushFail(
        Collections.singletonMap(TEST_DATA_CENTER, PUSH_VERSION.incrementAndGet()), subscriber);
    ret = circuitBreakerService.pushCircuitBreaker(subscriber.getStatistic(), true);
    Assert.assertTrue(ret);
    verify(sessionServerConfig, times(0)).getPushAddressCircuitBreakerThreshold();
  }

  @Test
  public void testAddressPushCircuitBreaker() throws InterruptedException, TimeoutException {

    // 1.switch open
    boolean ret = fetchCircuitBreakerService.start();
    Assert.assertTrue(ret);
    ConcurrentUtils.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);

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

    Subscriber subscriber = TestUtils.newZoneSubscriber(DATA_INFO_ID, TEST_CELL);
    circuitBreakerService.onPushSuccess(
        Collections.singletonMap(TEST_DATA_CENTER, PUSH_VERSION.incrementAndGet()),
        Collections.singletonMap(TEST_DATA_CENTER, 1),
        subscriber);

    ret = circuitBreakerService.pushCircuitBreaker(subscriber.getStatistic(), true);
    Assert.assertFalse(ret);

    // 2.set circuit address
    HashSet<String> address = Sets.newHashSet(subscriber.getSourceAddress().getIpAddress());
    persistenceData =
        PersistenceDataBuilder.createPersistenceData(
            ValueConstants.CIRCUIT_BREAKER_DATA_ID,
            JsonUtils.writeValueAsString(new CircuitBreakerData(true, address)));
    put = provideDataRepository.put(persistenceData);

    Assert.assertTrue(put);
    waitConditionUntilTimeOut(() -> fetchCircuitBreakerService.isSwitchOpen(), MAX_WAIT);
    waitConditionUntilTimeOut(
        () -> address.equals(fetchCircuitBreakerService.getStopPushCircuitBreaker()), MAX_WAIT);

    ret = circuitBreakerService.pushCircuitBreaker(subscriber.getStatistic(), true);
    Assert.assertTrue(ret);

    // 3.remove address
    persistenceData =
        PersistenceDataBuilder.createPersistenceData(
            ValueConstants.CIRCUIT_BREAKER_DATA_ID,
            JsonUtils.writeValueAsString(new CircuitBreakerData(true, Collections.EMPTY_SET)));

    put = provideDataRepository.put(persistenceData);
    Assert.assertTrue(put);
    waitConditionUntilTimeOut(() -> fetchCircuitBreakerService.isSwitchOpen(), MAX_WAIT);
    waitConditionUntilTimeOut(
        () -> CollectionUtils.isEmpty(fetchCircuitBreakerService.getStopPushCircuitBreaker()),
        MAX_WAIT);
    ret = circuitBreakerService.pushCircuitBreaker(subscriber.getStatistic(), true);
    Assert.assertFalse(ret);
  }

  @Test
  public void testCalculateAddressPushCircuitBreaker()
      throws InterruptedException, TimeoutException {
    // 1.switch open
    boolean ret = fetchCircuitBreakerService.start();
    Assert.assertTrue(ret);
    ConcurrentUtils.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);

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

    Subscriber subscriber1 = TestUtils.newZoneSubscriber(DATA_INFO_ID, TEST_CELL);
    Subscriber subscriber2 = TestUtils.newZoneSubscriber(DATA_INFO_ID, TEST_CELL);

    circuitBreakerService.onPushSuccess(
        Collections.singletonMap(TEST_DATA_CENTER, PUSH_VERSION.incrementAndGet()),
        Collections.singletonMap(TEST_DATA_CENTER, 1),
        subscriber1);
    circuitBreakerService.onPushSuccess(
        Collections.singletonMap(TEST_DATA_CENTER, PUSH_VERSION.incrementAndGet()),
        Collections.singletonMap(TEST_DATA_CENTER, 1),
        subscriber2);

    // 2.su1 fail and sub2 fail, address fail count = CIRCUIT_BREAKER_THRESHOLD
    for (int i = 0; i < CIRCUIT_BREAKER_THRESHOLD / 2; i++) {
      ret =
          circuitBreakerService.onPushFail(
              Collections.singletonMap(TEST_DATA_CENTER, PUSH_VERSION.incrementAndGet()),
              subscriber1);
      Assert.assertTrue(ret);
      ret = circuitBreakerService.pushCircuitBreaker(subscriber1.getStatistic(), true);
      Assert.assertFalse(ret);

      ret =
          circuitBreakerService.onPushFail(
              Collections.singletonMap(TEST_DATA_CENTER, PUSH_VERSION.incrementAndGet()),
              subscriber2);
      Assert.assertTrue(ret);
      ret = circuitBreakerService.pushCircuitBreaker(subscriber2.getStatistic(), true);
      Assert.assertFalse(ret);
    }

    // 3.sub2 fail, address fail count = CIRCUIT_BREAKER_THRESHOLD * 1.5
    for (int i = 0; i < CIRCUIT_BREAKER_THRESHOLD / 2; i++) {
      ret =
          circuitBreakerService.onPushFail(
              Collections.singletonMap(TEST_DATA_CENTER, PUSH_VERSION.incrementAndGet()),
              subscriber2);
      Assert.assertTrue(ret);
      ret = circuitBreakerService.pushCircuitBreaker(subscriber1.getStatistic(), true);
      Assert.assertFalse(ret);
      ret = circuitBreakerService.pushCircuitBreaker(subscriber2.getStatistic(), true);
      Assert.assertFalse(ret);
    }

    // 4.sub2 fail, address fail count = CIRCUIT_BREAKER_THRESHOLD * 2
    for (int i = 0; i < CIRCUIT_BREAKER_THRESHOLD / 2; i++) {
      ret =
          circuitBreakerService.onPushFail(
              Collections.singletonMap(TEST_DATA_CENTER, PUSH_VERSION.incrementAndGet()),
              subscriber2);
      Assert.assertTrue(ret);
      ret = circuitBreakerService.pushCircuitBreaker(subscriber1.getStatistic(), true);
      Assert.assertFalse(ret);
      ret = circuitBreakerService.pushCircuitBreaker(subscriber2.getStatistic(), true);
      Assert.assertTrue(ret);
    }

    // 5.sub1 fail, address fail count = CIRCUIT_BREAKER_THRESHOLD * 2 + 1; exceed threshold
    ret =
        circuitBreakerService.onPushFail(
            Collections.singletonMap(TEST_DATA_CENTER, PUSH_VERSION.incrementAndGet()),
            subscriber1);
    Assert.assertTrue(ret);
    ret = circuitBreakerService.pushCircuitBreaker(subscriber1.getStatistic(), true);
    Assert.assertTrue(ret);
    ret = circuitBreakerService.pushCircuitBreaker(subscriber2.getStatistic(), true);
    Assert.assertTrue(ret);

    // 6.after circuit breaker silence
    ConcurrentUtils.sleepUninterruptibly(PUSH_CIRCUIT_BREAKER_SLEEP_MILLIS, TimeUnit.MILLISECONDS);
    ret = circuitBreakerService.pushCircuitBreaker(subscriber1.getStatistic(), true);
    Assert.assertFalse(ret);
    ret = circuitBreakerService.pushCircuitBreaker(subscriber2.getStatistic(), true);
    Assert.assertFalse(ret);

    // 7.sub1 fail, address fail count = CIRCUIT_BREAKER_THRESHOLD * 2 + 2; exceed threshold
    ret =
        circuitBreakerService.onPushFail(
            Collections.singletonMap(TEST_DATA_CENTER, PUSH_VERSION.incrementAndGet()),
            subscriber1);
    Assert.assertTrue(ret);
    ret = circuitBreakerService.pushCircuitBreaker(subscriber1.getStatistic(), true);
    Assert.assertTrue(ret);
    ret = circuitBreakerService.pushCircuitBreaker(subscriber2.getStatistic(), true);
    Assert.assertTrue(ret);

    // 8.sub1 consecutive success, address fail count = 0;
    for (int i = 0; i < PUSH_CONSECUTIVE_SUCCESS - 1; i++) {
      ret =
          circuitBreakerService.onPushSuccess(
              Collections.singletonMap(TEST_DATA_CENTER, PUSH_VERSION.incrementAndGet()),
              Collections.singletonMap(TEST_DATA_CENTER, 1),
              subscriber1);
      Assert.assertTrue(ret);
      ret = circuitBreakerService.pushCircuitBreaker(subscriber1.getStatistic(), true);
      Assert.assertTrue(ret);
      ret = circuitBreakerService.pushCircuitBreaker(subscriber2.getStatistic(), true);
      Assert.assertTrue(ret);
    }
    ret =
        circuitBreakerService.onPushSuccess(
            Collections.singletonMap(TEST_DATA_CENTER, PUSH_VERSION.incrementAndGet()),
            Collections.singletonMap(TEST_DATA_CENTER, 1),
            subscriber1);
    Assert.assertTrue(ret);
    ret = circuitBreakerService.pushCircuitBreaker(subscriber1.getStatistic(), true);
    Assert.assertFalse(ret);
    ret = circuitBreakerService.pushCircuitBreaker(subscriber2.getStatistic(), true);
    Assert.assertFalse(ret);
  }
}
