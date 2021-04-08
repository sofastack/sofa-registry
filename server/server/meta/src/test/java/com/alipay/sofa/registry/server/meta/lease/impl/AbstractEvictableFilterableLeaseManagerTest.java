package com.alipay.sofa.registry.server.meta.lease.impl;

import com.alipay.sofa.registry.common.model.metaserver.Lease;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.lease.LeaseFilter;
import com.alipay.sofa.registry.server.meta.lease.filter.DefaultRegistryBlacklistManager;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

public class AbstractEvictableFilterableLeaseManagerTest extends AbstractMetaServerTestBase {

  private AbstractEvictableFilterableLeaseManager leaseManager;

  private DefaultRegistryBlacklistManager blacklistManager = new DefaultRegistryBlacklistManager(new InMemoryProvideDataRepo());

  @Before
  public void beforeAbstractEvictableFilterableLeaseManagerTest() throws TimeoutException, InterruptedException {
    leaseManager = new AbstractEvictableFilterableLeaseManager() {
      @Override
      protected long getEvictBetweenMilli() {
        return 10;
      }

      @Override
      protected long getIntervalMilli() {
        return 1000;
      }
    };
    leaseManager.metaLeaderService = metaLeaderService;
    leaseManager.setLeaseFilters(Lists.newArrayList(blacklistManager));
    makeMetaLeader();
  }

  @Test
  public void testGetLeaseMeta() {
    int nodeNum = 100;
    for (int i = 0; i < nodeNum; i++) {
      leaseManager.renew(new SimpleNode(randomIp()), 100);
    }
    SimpleNode node = new SimpleNode(randomIp());
    leaseManager.renew(node, 100);
    Assert.assertEquals(nodeNum + 1, leaseManager.getLeaseMeta().getClusterMembers().size());
    blacklistManager.addToBlacklist(node.getNodeUrl().getIpAddress());
    Assert.assertEquals(nodeNum, leaseManager.getLeaseMeta().getClusterMembers().size());
  }

  @Test
  public void testFilterOut() {
    int nodeNum = 100;
    for (int i = 1; i < nodeNum; i++) {
      leaseManager.renew(new SimpleNode("10.0.0." + i), 100);
    }
    leaseManager.renew(new SimpleNode("127.0.0.1"), 100);
    List<Lease> leases = leaseManager.filterOut(Lists.newArrayList(leaseManager.localRepo.values()), new LeaseFilter<SimpleNode>() {
      @Override
      public boolean allowSelect(Lease<SimpleNode> lease) {
        return !lease.getRenewal().getNodeUrl().getIpAddress().startsWith("10.0.0");
      }
    });
    Assert.assertEquals(1, leases.size());
  }
}