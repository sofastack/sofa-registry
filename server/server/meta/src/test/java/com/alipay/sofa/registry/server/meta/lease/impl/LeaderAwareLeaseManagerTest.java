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
package com.alipay.sofa.registry.server.meta.lease.impl;

import static org.junit.Assert.*;

import com.alipay.sofa.registry.common.model.metaserver.Lease;
import com.alipay.sofa.registry.exception.SofaRegistryMetaLeaderException;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import java.util.concurrent.TimeoutException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LeaderAwareLeaseManagerTest extends AbstractMetaServerTestBase {

  private LeaderAwareLeaseManager leaseManager;

  @Before
  public void beforeLeaderAwareLeaseManagerTest() {
    leaseManager = new LeaderAwareLeaseManager();
    leaseManager.metaLeaderService = metaLeaderService;
  }

  @Test(expected = SofaRegistryMetaLeaderException.class)
  public void testRegister() throws TimeoutException, InterruptedException {
    makeMetaLeader();
    leaseManager.register(new Lease(new SimpleNode(randomIp()), 10));
    makeMetaNonLeader();
    leaseManager.register(new Lease(new SimpleNode(randomIp()), 10));
  }

  @Test(expected = SofaRegistryMetaLeaderException.class)
  public void testCancel() throws TimeoutException, InterruptedException {
    makeMetaLeader();
    leaseManager.register(new Lease(new SimpleNode(randomIp()), 10));
    makeMetaNonLeader();
    leaseManager.cancel(new Lease(new SimpleNode(randomIp()), 10));
  }

  @Test(expected = SofaRegistryMetaLeaderException.class)
  public void testRenew() throws TimeoutException, InterruptedException {
    makeMetaNonLeader();
    leaseManager.renew(new SimpleNode(randomIp()), 1000);
  }

  @Test
  public void testAmILeader() throws TimeoutException, InterruptedException {
    makeMetaNonLeader();
    Assert.assertFalse(leaseManager.amILeader());
    makeMetaLeader();
    Assert.assertTrue(leaseManager.amILeader());
  }
}
