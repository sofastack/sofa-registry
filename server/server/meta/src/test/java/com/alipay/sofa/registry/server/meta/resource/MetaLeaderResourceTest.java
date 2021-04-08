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

import static org.mockito.Mockito.*;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.elector.LeaderInfo;
import com.alipay.sofa.registry.exception.SofaRegistryRuntimeException;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.store.api.elector.LeaderElector;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MetaLeaderResourceTest {

  private MetaLeaderResource resource;

  @Mock private MetaLeaderService metaLeaderService;

  @Mock private LeaderElector leaderElector;

  @Before
  public void beforeMetaLeaderResourceTest() {
    MockitoAnnotations.initMocks(this);
    resource =
        new MetaLeaderResource()
            .setLeaderElector(leaderElector)
            .setMetaLeaderService(metaLeaderService);
  }

  @Test
  public void testQueryLeader() {
    when(metaLeaderService.getLeader()).thenReturn("127.0.0.1");
    GenericResponse<LeaderInfo> response = resource.queryLeader();
    Assert.assertTrue(response.isSuccess());
    Assert.assertEquals("127.0.0.1", response.getData().getLeader());

    when(metaLeaderService.getLeader())
        .thenThrow(new SofaRegistryRuntimeException("expected exception"));
    response = resource.queryLeader();
    Assert.assertFalse(response.isSuccess());
  }

  @Test
  public void testQuitLeader() {
    CommonResponse response = resource.quitElection();
    Assert.assertTrue(response.isSuccess());
    verify(leaderElector, times(1)).change2Observer();

    doThrow(new SofaRegistryRuntimeException("expected exception"))
        .when(leaderElector)
        .change2Observer();
    response = resource.quitElection();
    Assert.assertFalse(response.isSuccess());
  }
}
