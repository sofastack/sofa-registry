package com.alipay.sofa.registry.server.meta.lease.impl;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.metaserver.Lease;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

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