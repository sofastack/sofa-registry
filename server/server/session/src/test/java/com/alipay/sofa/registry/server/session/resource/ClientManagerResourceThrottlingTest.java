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
package com.alipay.sofa.registry.server.session.resource;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.metaserver.limit.FlowOperationThrottlingStatus;
import com.alipay.sofa.registry.server.session.connections.ConnectionsService;
import com.alipay.sofa.registry.server.session.limit.FlowOperationThrottlingObserver;
import com.alipay.sofa.registry.server.session.registry.SessionRegistry;
import java.lang.reflect.Field;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ClientManagerResourceThrottlingTest {

  private ClientManagerResource resource;
  private FlowOperationThrottlingObserver mockObserver;
  private ConnectionsService mockConnectionsService;
  private SessionRegistry mockSessionRegistry;

  @Before
  public void setUp() throws Exception {
    resource = new ClientManagerResource();
    mockObserver = Mockito.mock(FlowOperationThrottlingObserver.class);
    mockConnectionsService = Mockito.mock(ConnectionsService.class);
    mockSessionRegistry = Mockito.mock(SessionRegistry.class);

    // Inject mocks using reflection
    setField(resource, "flowOperationThrottlingObserver", mockObserver);
    setField(resource, "connectionsService", mockConnectionsService);
    setField(resource, "sessionRegistry", mockSessionRegistry);

    // Default mock behavior
    Mockito.when(mockConnectionsService.getIpConnects(Mockito.anySet()))
        .thenReturn(Collections.emptyList());
    Mockito.when(mockConnectionsService.closeIpConnects(Mockito.anyList()))
        .thenReturn(Collections.emptyList());
  }

  private void setField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }

  @Test
  public void testClientOffNotThrottled() {
    Mockito.when(mockObserver.shouldThrottle()).thenReturn(false);

    CommonResponse response = resource.clientOff("192.168.1.1");

    Assert.assertTrue(response.isSuccess());
    Mockito.verify(mockConnectionsService).getIpConnects(Mockito.anySet());
    Mockito.verify(mockSessionRegistry).clientOff(Mockito.anyList());
  }

  @Test
  public void testClientOffThrottled() {
    Mockito.when(mockObserver.shouldThrottle()).thenReturn(true);

    CommonResponse response = resource.clientOff("192.168.1.1");

    Assert.assertFalse(response.isSuccess());
    Assert.assertEquals("too many request", response.getMessage());
    // Should not call business logic when throttled
    Mockito.verify(mockConnectionsService, Mockito.never()).getIpConnects(Mockito.anySet());
    Mockito.verify(mockSessionRegistry, Mockito.never()).clientOff(Mockito.anyList());
  }

  @Test
  public void testClientOnNotThrottled() {
    Mockito.when(mockObserver.shouldThrottle()).thenReturn(false);

    CommonResponse response = resource.clientOn("192.168.1.1");

    Assert.assertTrue(response.isSuccess());
    Mockito.verify(mockConnectionsService).closeIpConnects(Mockito.anyList());
  }

  @Test
  public void testClientOnThrottled() {
    Mockito.when(mockObserver.shouldThrottle()).thenReturn(true);

    CommonResponse response = resource.clientOn("192.168.1.1");

    Assert.assertFalse(response.isSuccess());
    Assert.assertEquals("too many request", response.getMessage());
    // Should not call business logic when throttled
    Mockito.verify(mockConnectionsService, Mockito.never()).closeIpConnects(Mockito.anyList());
  }

  @Test
  public void testClientOffEmptyIps() {
    CommonResponse response = resource.clientOff("");

    Assert.assertFalse(response.isSuccess());
    Assert.assertEquals("ips is empty", response.getMessage());
    // Throttle check should not be called for empty input
    Mockito.verify(mockObserver, Mockito.never()).shouldThrottle();
  }

  @Test
  public void testClientOnEmptyIps() {
    CommonResponse response = resource.clientOn("");

    Assert.assertFalse(response.isSuccess());
    Assert.assertEquals("ips is empty", response.getMessage());
    // Throttle check should not be called for empty input
    Mockito.verify(mockObserver, Mockito.never()).shouldThrottle();
  }

  @Test
  public void testThrottlingConsistency() {
    // Test that throttling check is only called once per request
    // This verifies the fix for H4 (double throttling issue)

    // First call returns false (not throttled), second would return true
    // But since we fixed the issue, second call should never happen
    Mockito.when(mockObserver.shouldThrottle()).thenReturn(false);

    resource.clientOff("192.168.1.1");

    // Verify shouldThrottle is called exactly once
    Mockito.verify(mockObserver, Mockito.times(1)).shouldThrottle();
  }

  @Test
  public void testProbabilisticThrottlingBehavior() {
    // Simulate probabilistic throttling (50% rate)
    FlowOperationThrottlingObserver realObserver = new FlowOperationThrottlingObserver();
    realObserver.updateClusterThrottlingStatus(FlowOperationThrottlingStatus.enabled(50.0));

    try {
      setField(resource, "flowOperationThrottlingObserver", realObserver);
    } catch (Exception e) {
      Assert.fail("Failed to inject real observer");
    }

    int throttledCount = 0;
    int totalRuns = 1000;

    for (int i = 0; i < totalRuns; i++) {
      CommonResponse response = resource.clientOff("192.168.1." + i);
      if (!response.isSuccess() && "too many request".equals(response.getMessage())) {
        throttledCount++;
      }
    }

    // Verify throttle rate is roughly 50% with 15% margin
    double throttleRate = (double) throttledCount / totalRuns;
    Assert.assertTrue(
        "Throttle rate should be around 50%, but was " + (throttleRate * 100) + "%",
        throttleRate > 0.35 && throttleRate < 0.65);
  }
}
