package com.alipay.sofa.registry.server.meta.lease.impl;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.server.meta.AbstractTest;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Objects;

import static org.junit.Assert.*;

public class DefaultLeaseManagerTest extends AbstractTest {

    private DefaultLeaseManager<SimpleNode> leaseManager;

    @Before
    public void beforeDefaultLeaseManagerTest() {
        leaseManager = new DefaultLeaseManager<>();
        leaseManager.setLogger(logger);
    }

    @Test
    public void testRegister() throws InterruptedException {
        String ip = randomIp();
        leaseManager.register(new SimpleNode(ip), 1);
        Assert.assertNotNull(leaseManager.repo.get(ip));
    }

    @Test
    public void testCancel() {
        String ip = randomIp();
        leaseManager.register(new SimpleNode(ip), 1);
        Assert.assertNotNull(leaseManager.repo.get(ip));
        leaseManager.cancel(new SimpleNode(ip));
        Assert.assertNull(leaseManager.repo.get(ip));
    }

    @Test
    public void testRenew() {
        String ip = randomIp();
        leaseManager.register(new SimpleNode(ip), 1);
        Assert.assertNotNull(leaseManager.repo.get(ip));
        leaseManager.cancel(new SimpleNode(ip));
        Assert.assertNull(leaseManager.repo.get(ip));
        leaseManager.renew(new SimpleNode(ip), 1);
        Assert.assertNotNull(leaseManager.repo.get(ip));
    }

    @Test
    public void testEvict() throws InterruptedException {
        String ip = randomIp();
        leaseManager.register(new SimpleNode(ip), 1);
        Assert.assertNotNull(leaseManager.repo.get(ip));
        Thread.sleep(1000);
        logger.info("[expired] {}", leaseManager.repo.get(ip).isExpired());
        leaseManager.evict();
        Assert.assertNull(leaseManager.repo.get(ip));
    }

    @Test
    public void testGetEpoch() {
        Assert.assertEquals(0, leaseManager.getEpoch());
    }

    @Test
    public void testRefreshEpoch() {
        Assert.assertEquals(0, leaseManager.getEpoch());
        leaseManager.refreshEpoch(DatumVersionUtil.nextId());
        Assert.assertNotEquals(0, leaseManager.getEpoch());
    }

    public static class SimpleNode implements Node {

        private final URL url;

        public SimpleNode(URL url) {
            this.url = url;
        }

        public SimpleNode(String ip) {
            this.url = new URL(ip);
        }

        @Override
        public NodeType getNodeType() {
            return NodeType.CLIENT;
        }

        @Override
        public URL getNodeUrl() {
            return url;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SimpleNode that = (SimpleNode) o;
            return Objects.equals(url, that.url);
        }

        @Override
        public int hashCode() {
            return Objects.hash(url);
        }
    }
}