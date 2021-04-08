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
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SimpleLeaseManagerTest extends AbstractMetaServerTestBase {

  private SimpleLeaseManager<SimpleNode> leaseManager;

  @Before
  public void beforeSimpleLeaseManagerTest() {
    leaseManager = new SimpleLeaseManager<>();
  }

  @Test
  public void testRegister() {
    SimpleNode node = new SimpleNode(randomIp());
    leaseManager.register(new Lease<>(node, 1000));
    Assert.assertFalse(leaseManager.localRepo.isEmpty());
    Assert.assertEquals(node, leaseManager.getLease(node).getRenewal());
  }

  @Test
  public void testCancel() {
    SimpleNode node = new SimpleNode(randomIp());
    leaseManager.register(new Lease<>(node, 1000));
    leaseManager.renew(new SimpleNode(node.getNodeUrl().getIpAddress()), 10);
    Assert.assertEquals(node, leaseManager.getLease(node).getRenewal());
    Assert.assertEquals(1, leaseManager.localRepo.size());
    leaseManager.cancel(leaseManager.getLease(node));
    Assert.assertTrue(leaseManager.localRepo.isEmpty());
  }

  @Test
  public void testRenew() {
    SimpleNode node = new SimpleNode(randomIp());
    leaseManager.register(new Lease<>(node, 1000));
    leaseManager.renew(new SimpleNode(node.getNodeUrl().getIpAddress()), 10);
    Assert.assertEquals(node, leaseManager.getLease(node).getRenewal());
    Assert.assertEquals(1, leaseManager.localRepo.size());
  }

  @Test
  public void testRefreshEpoch() {
    Assert.assertTrue(leaseManager.refreshEpoch(DatumVersionUtil.nextId()));
    Assert.assertFalse(leaseManager.refreshEpoch(0L));
  }
}
