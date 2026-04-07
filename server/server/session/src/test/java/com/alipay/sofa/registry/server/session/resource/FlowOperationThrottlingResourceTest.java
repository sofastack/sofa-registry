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

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.metaserver.limit.FlowOperationThrottlingStatus;
import com.alipay.sofa.registry.server.session.limit.FlowOperationThrottlingObserver;
import java.lang.reflect.Field;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class FlowOperationThrottlingResourceTest {

  private FlowOperationThrottlingResource resource;
  private FlowOperationThrottlingObserver mockObserver;

  @Before
  public void setUp() throws Exception {
    resource = new FlowOperationThrottlingResource();
    mockObserver = Mockito.mock(FlowOperationThrottlingObserver.class);

    // Inject mock observer using reflection
    Field observerField =
        FlowOperationThrottlingResource.class.getDeclaredField("flowOperationThrottlingObserver");
    observerField.setAccessible(true);
    observerField.set(resource, mockObserver);
  }

  @Test
  public void testGetStatusBothDisabled() {
    FlowOperationThrottlingStatus localStatus = FlowOperationThrottlingStatus.disabled();
    FlowOperationThrottlingStatus clusterStatus = FlowOperationThrottlingStatus.disabled();

    Mockito.when(mockObserver.getLocalThrottlingStatus()).thenReturn(localStatus);
    Mockito.when(mockObserver.getClusterThrottlingStatus()).thenReturn(clusterStatus);

    GenericResponse<Map<String, FlowOperationThrottlingStatus>> response = resource.getStatus();

    Assert.assertTrue(response.isSuccess());
    Assert.assertNotNull(response.getData());
    Assert.assertEquals(localStatus, response.getData().get("localStatus"));
    Assert.assertEquals(clusterStatus, response.getData().get("clusterStatus"));
  }

  @Test
  public void testGetStatusLocalEnabled() {
    FlowOperationThrottlingStatus localStatus = FlowOperationThrottlingStatus.enabled(100.0);
    FlowOperationThrottlingStatus clusterStatus = FlowOperationThrottlingStatus.disabled();

    Mockito.when(mockObserver.getLocalThrottlingStatus()).thenReturn(localStatus);
    Mockito.when(mockObserver.getClusterThrottlingStatus()).thenReturn(clusterStatus);

    GenericResponse<Map<String, FlowOperationThrottlingStatus>> response = resource.getStatus();

    Assert.assertTrue(response.isSuccess());
    Assert.assertNotNull(response.getData());

    FlowOperationThrottlingStatus returnedLocal = response.getData().get("localStatus");
    Assert.assertTrue(returnedLocal.isEnabled());
    Assert.assertEquals(100.0, returnedLocal.getThrottlePercent(), 0.01);

    FlowOperationThrottlingStatus returnedCluster = response.getData().get("clusterStatus");
    Assert.assertFalse(returnedCluster.isEnabled());
  }

  @Test
  public void testGetStatusClusterEnabled() {
    FlowOperationThrottlingStatus localStatus = FlowOperationThrottlingStatus.disabled();
    FlowOperationThrottlingStatus clusterStatus = FlowOperationThrottlingStatus.enabled(75.0);

    Mockito.when(mockObserver.getLocalThrottlingStatus()).thenReturn(localStatus);
    Mockito.when(mockObserver.getClusterThrottlingStatus()).thenReturn(clusterStatus);

    GenericResponse<Map<String, FlowOperationThrottlingStatus>> response = resource.getStatus();

    Assert.assertTrue(response.isSuccess());
    Assert.assertNotNull(response.getData());

    FlowOperationThrottlingStatus returnedLocal = response.getData().get("localStatus");
    Assert.assertFalse(returnedLocal.isEnabled());

    FlowOperationThrottlingStatus returnedCluster = response.getData().get("clusterStatus");
    Assert.assertTrue(returnedCluster.isEnabled());
    Assert.assertEquals(75.0, returnedCluster.getThrottlePercent(), 0.01);
  }

  @Test
  public void testGetStatusBothEnabled() {
    FlowOperationThrottlingStatus localStatus = FlowOperationThrottlingStatus.enabled(100.0);
    FlowOperationThrottlingStatus clusterStatus = FlowOperationThrottlingStatus.enabled(50.0);

    Mockito.when(mockObserver.getLocalThrottlingStatus()).thenReturn(localStatus);
    Mockito.when(mockObserver.getClusterThrottlingStatus()).thenReturn(clusterStatus);

    GenericResponse<Map<String, FlowOperationThrottlingStatus>> response = resource.getStatus();

    Assert.assertTrue(response.isSuccess());
    Assert.assertNotNull(response.getData());

    FlowOperationThrottlingStatus returnedLocal = response.getData().get("localStatus");
    Assert.assertTrue(returnedLocal.isEnabled());
    Assert.assertEquals(100.0, returnedLocal.getThrottlePercent(), 0.01);

    FlowOperationThrottlingStatus returnedCluster = response.getData().get("clusterStatus");
    Assert.assertTrue(returnedCluster.isEnabled());
    Assert.assertEquals(50.0, returnedCluster.getThrottlePercent(), 0.01);
  }

  @Test
  public void testGetStatusException() {
    Mockito.when(mockObserver.getLocalThrottlingStatus())
        .thenThrow(new RuntimeException("Test exception"));

    GenericResponse<Map<String, FlowOperationThrottlingStatus>> response = resource.getStatus();

    Assert.assertFalse(response.isSuccess());
  }
}
