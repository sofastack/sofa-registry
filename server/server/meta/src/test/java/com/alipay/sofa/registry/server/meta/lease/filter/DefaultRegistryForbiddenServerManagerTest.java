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

import static org.mockito.Mockito.*;

import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.metaserver.DataOperation;
import com.alipay.sofa.registry.common.model.metaserver.blacklist.RegistryForbiddenServerRequest;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.provide.data.NodeOperatingService;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DefaultRegistryForbiddenServerManagerTest extends AbstractMetaServerTestBase {

  private RegistryForbiddenServerManager registryForbiddenServerManager;

  private ProvideDataService provideDataService;

  private NodeOperatingService nodeOperatingService;

  @Before
  public void beforeDefaultRegistryForbiddenServerManagerTest() {
    provideDataService = spy(new InMemoryProvideDataRepo());
    nodeOperatingService = mock(NodeOperatingService.class);
    when(nodeOperatingService.queryOperateInfoAndVersion()).thenReturn(new Tuple<>(0l, null));
    registryForbiddenServerManager =
        new DefaultForbiddenServerManager(provideDataService, nodeOperatingService);
  }

  @Test
  public void testNormalCase() {
    RegistryForbiddenServerRequest add =
        new RegistryForbiddenServerRequest(
            DataOperation.ADD, NodeType.DATA, "127.0.0.1", "testCell");
    boolean success = registryForbiddenServerManager.addToBlacklist(add);
    Assert.assertTrue(success);
  }
}
