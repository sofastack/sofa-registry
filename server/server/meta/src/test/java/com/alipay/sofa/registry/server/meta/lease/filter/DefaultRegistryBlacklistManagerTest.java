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
import com.alipay.sofa.registry.store.api.meta.ProvideDataRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DefaultRegistryBlacklistManagerTest extends AbstractMetaServerTestBase {

  private RegistryBlacklistManager blacklistManager;

  private ProvideDataRepository repository;

  @Before
  public void beforeDefaultRegistryBlacklistManagerTest() {
    repository = spy(new InMemoryProvideDataRepo());
    blacklistManager = new DefaultRegistryBlacklistManager(repository);
  }

  @Test
  public void testNormalCase() {
    blacklistManager.addToBlacklist("127.0.0.1");
    Assert.assertFalse(
        blacklistManager.allowSelect(
            new Lease<>(new NodeModifiedTest.SimpleNode("127.0.0.1"), System.currentTimeMillis())));
    blacklistManager.removeFromBlacklist("127.0.0.1");
    Assert.assertTrue(
        blacklistManager.allowSelect(
            new Lease<>(new NodeModifiedTest.SimpleNode("127.0.0.1"), System.currentTimeMillis())));
  }

  @Test(expected = SofaRegistryRuntimeException.class)
  public void testException() {
    Assert.assertTrue(
        blacklistManager.allowSelect(
            new Lease<>(new NodeModifiedTest.SimpleNode("127.0.0.1"), System.currentTimeMillis())));
    repository = mock(ProvideDataRepository.class);
    when(repository.get(anyString()))
        .thenThrow(new SofaRegistryRuntimeException("expected io exception"));
    blacklistManager = new DefaultRegistryBlacklistManager(repository);
    Assert.assertTrue(
        blacklistManager.allowSelect(
            new Lease<>(new NodeModifiedTest.SimpleNode("127.0.0.1"), System.currentTimeMillis())));
    blacklistManager.addToBlacklist("127.0.0.1");
  }
}
