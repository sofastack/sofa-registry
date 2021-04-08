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
package com.alipay.sofa.registry.server.meta.lease.filter;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import com.alipay.sofa.registry.common.model.metaserver.Lease;
import com.alipay.sofa.registry.exception.SofaRegistryRuntimeException;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.cluster.node.NodeModifiedTest;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DefaultRegistryForbiddenServerManagerTest extends AbstractMetaServerTestBase {

  private RegistryForbiddenServerManager registryForbiddenServerManager;

  private ProvideDataService provideDataService;

  @Before
  public void beforeDefaultRegistryForbiddenServerManagerTest() {
    provideDataService = spy(new InMemoryProvideDataRepo());
    registryForbiddenServerManager = new DefaultForbiddenServerManager(provideDataService);
  }

  @Test
  public void testNormalCase() {
    registryForbiddenServerManager.addToBlacklist("127.0.0.1");
    Assert.assertFalse(
        registryForbiddenServerManager.allowSelect(
            new Lease<>(new NodeModifiedTest.SimpleNode("127.0.0.1"), System.currentTimeMillis())));
    registryForbiddenServerManager.removeFromBlacklist("127.0.0.1");
    Assert.assertTrue(
        registryForbiddenServerManager.allowSelect(
            new Lease<>(new NodeModifiedTest.SimpleNode("127.0.0.1"), System.currentTimeMillis())));
  }

  @Test(expected = SofaRegistryRuntimeException.class)
  public void testException() {
    Assert.assertTrue(
        registryForbiddenServerManager.allowSelect(
            new Lease<>(new NodeModifiedTest.SimpleNode("127.0.0.1"), System.currentTimeMillis())));
    provideDataService = mock(ProvideDataService.class);
    when(provideDataService.queryProvideData(anyString()))
        .thenThrow(new SofaRegistryRuntimeException("expected io exception"));
    registryForbiddenServerManager = new DefaultForbiddenServerManager(provideDataService);
    Assert.assertTrue(
        registryForbiddenServerManager.allowSelect(
            new Lease<>(new NodeModifiedTest.SimpleNode("127.0.0.1"), System.currentTimeMillis())));
    registryForbiddenServerManager.addToBlacklist("127.0.0.1");
  }
}
