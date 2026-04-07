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
package com.alipay.sofa.registry.server.meta.resource;

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.metaserver.limit.FlowOperationThrottlingStatus;
import com.alipay.sofa.registry.core.model.Result;
import com.alipay.sofa.registry.server.meta.limit.AdaptiveFlowOperationLimiter;
import java.lang.reflect.Field;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class FlowOperationThrottlingResourceTest {

  private FlowOperationThrottlingResource resource;
  private AdaptiveFlowOperationLimiter mockLimiter;

  @Before
  public void setUp() throws Exception {
    resource = new FlowOperationThrottlingResource();
    mockLimiter = Mockito.mock(AdaptiveFlowOperationLimiter.class);

    // Inject mock limiter using reflection
    Field limiterField =
        FlowOperationThrottlingResource.class.getDeclaredField("adaptiveFlowOperationLimiter");
    limiterField.setAccessible(true);
    limiterField.set(resource, mockLimiter);
  }

  @Test
  public void testEnableEmergencyThrottling() {
    Result result = resource.enableEmergencyThrottling();

    Assert.assertTrue(result.isSuccess());
    Mockito.verify(mockLimiter).setEmergencyThrottlingEnabled(true);
  }

  @Test
  public void testEnableEmergencyThrottlingException() {
    Mockito.doThrow(new RuntimeException("Test exception"))
        .when(mockLimiter)
        .setEmergencyThrottlingEnabled(true);

    Result result = resource.enableEmergencyThrottling();

    Assert.assertFalse(result.isSuccess());
  }

  @Test
  public void testDisableEmergencyThrottling() {
    Result result = resource.disableEmergencyThrottling();

    Assert.assertTrue(result.isSuccess());
    Mockito.verify(mockLimiter).setEmergencyThrottlingEnabled(false);
  }

  @Test
  public void testDisableEmergencyThrottlingException() {
    Mockito.doThrow(new RuntimeException("Test exception"))
        .when(mockLimiter)
        .setEmergencyThrottlingEnabled(false);

    Result result = resource.disableEmergencyThrottling();

    Assert.assertFalse(result.isSuccess());
  }

  @Test
  public void testGetThrottlingStatus() {
    FlowOperationThrottlingStatus expectedStatus = FlowOperationThrottlingStatus.enabled(50.0);
    Mockito.when(mockLimiter.getFlowOperationThrottlingStatus()).thenReturn(expectedStatus);

    GenericResponse<FlowOperationThrottlingStatus> response = resource.getThrottlingStatus();

    Assert.assertTrue(response.isSuccess());
    Assert.assertEquals(expectedStatus, response.getData());
  }

  @Test
  public void testGetThrottlingStatusException() {
    Mockito.when(mockLimiter.getFlowOperationThrottlingStatus())
        .thenThrow(new RuntimeException("Test exception"));

    GenericResponse<FlowOperationThrottlingStatus> response = resource.getThrottlingStatus();

    Assert.assertFalse(response.isSuccess());
  }

  @Test
  public void testGetEmergencyStatusEnabled() {
    Mockito.when(mockLimiter.isEmergencyThrottlingEnabled()).thenReturn(true);

    GenericResponse<Map<String, Boolean>> response = resource.getEmergencyStatus();

    Assert.assertTrue(response.isSuccess());
    Assert.assertTrue(response.getData().get("emergencyEnabled"));
  }

  @Test
  public void testGetEmergencyStatusDisabled() {
    Mockito.when(mockLimiter.isEmergencyThrottlingEnabled()).thenReturn(false);

    GenericResponse<Map<String, Boolean>> response = resource.getEmergencyStatus();

    Assert.assertTrue(response.isSuccess());
    Assert.assertFalse(response.getData().get("emergencyEnabled"));
  }

  @Test
  public void testGetEmergencyStatusException() {
    Mockito.when(mockLimiter.isEmergencyThrottlingEnabled())
        .thenThrow(new RuntimeException("Test exception"));

    GenericResponse<Map<String, Boolean>> response = resource.getEmergencyStatus();

    Assert.assertFalse(response.isSuccess());
  }
}
