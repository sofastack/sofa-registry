package com.alipay.sofa.registry.server.meta.lease.impl;

import com.alipay.sofa.registry.common.model.metaserver.Lease;
import com.alipay.sofa.registry.exception.SofaRegistryMetaLeaderException;
import com.alipay.sofa.registry.server.meta.AbstractH2DbTestBase;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

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