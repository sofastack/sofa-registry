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

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.Lease;
import com.alipay.sofa.registry.exception.SofaRegistryRuntimeException;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.lease.filter.DefaultForbiddenServerManager;
import com.alipay.sofa.registry.server.meta.lease.filter.RegistryForbiddenServerManager;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataService;
import com.alipay.sofa.registry.store.api.DBResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RegistryCoreOpsResourceTest extends AbstractMetaServerTestBase {

  private RegistryForbiddenServerManager registryForbiddenServerManager;

  private RegistryCoreOpsResource resource;

  private ProvideDataService provideDataService = spy(new InMemoryProvideDataRepo());

  @Before
  public void before() {
    registryForbiddenServerManager = new DefaultForbiddenServerManager(provideDataService);
    resource =
        new RegistryCoreOpsResource()
            .setRegistryForbiddenServerManager(registryForbiddenServerManager);
  }

  @Test
  public void testKickoffServer() {
    CommonResponse response = resource.kickoffServer("fakeip");
    Assert.assertFalse(response.isSuccess());
    Assert.assertEquals("invalid ip address: fakeip", response.getMessage());

    response = resource.kickoffServer("127.0.0.1");
    Assert.assertTrue(response.isSuccess());
    Assert.assertFalse(
        registryForbiddenServerManager.allowSelect(new Lease<>(new SimpleNode("127.0.0.1"), 100)));
  }

  @Test
  public void testKickoffServerException() {
    ProvideDataService provideDataService = mock(ProvideDataService.class);
    registryForbiddenServerManager = new DefaultForbiddenServerManager(provideDataService);
    resource =
        new RegistryCoreOpsResource()
            .setRegistryForbiddenServerManager(registryForbiddenServerManager);

    when(provideDataService.queryProvideData(ValueConstants.REGISTRY_SERVER_BLACK_LIST_DATA_ID))
        .thenReturn(DBResponse.notfound().build());

    doThrow(new SofaRegistryRuntimeException("expected"))
        .when(provideDataService)
        .saveProvideData(mockPersistenceData(), System.currentTimeMillis());
    CommonResponse response = resource.kickoffServer("127.0.0.1");
    Assert.assertFalse(response.isSuccess());
  }

  @Test
  public void testRejoinServerGroup() {
    CommonResponse response = resource.rejoinServerGroup("fakeip");
    Assert.assertFalse(response.isSuccess());
    Assert.assertEquals("invalid ip address: fakeip", response.getMessage());

    Assert.assertTrue(
        registryForbiddenServerManager.allowSelect(new Lease<>(new SimpleNode("127.0.0.1"), 100)));

    response = resource.kickoffServer("127.0.0.1");
    Assert.assertTrue(response.isSuccess());
    Assert.assertFalse(
        registryForbiddenServerManager.allowSelect(new Lease<>(new SimpleNode("127.0.0.1"), 100)));

    response = resource.rejoinServerGroup("127.0.0.1");
    Assert.assertTrue(response.isSuccess());
    Assert.assertTrue(
        registryForbiddenServerManager.allowSelect(new Lease<>(new SimpleNode("127.0.0.1"), 100)));
  }
}
