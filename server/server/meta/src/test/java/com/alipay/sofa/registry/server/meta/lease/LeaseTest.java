package com.alipay.sofa.registry.server.meta.lease;

import com.alipay.sofa.registry.server.meta.AbstractTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class LeaseTest extends AbstractTest {

    private Lease<Object> lease;

    @Test
    public void testIsExpired() throws InterruptedException {
        lease = new Lease<>(new Object(), 1, TimeUnit.MILLISECONDS);
        Thread.sleep(2);
        Assert.assertTrue(lease.isExpired());
        lease.renew(1);
        Assert.assertFalse(lease.isExpired());
    }

    @Test
    public void testGetBeginTimestamp() throws InterruptedException {
        lease = new Lease<>(new Object(), 1);
        long begin = lease.getBeginTimestamp();
        lease.renew();
        Thread.sleep(5);
        lease.renew();
        Assert.assertEquals(begin, lease.getBeginTimestamp());
    }

    @Test
    public void testGetLastUpdateTimestamp() throws InterruptedException {
        lease = new Lease<>(new Object(), 1);
        long firstUpdate = lease.getLastUpdateTimestamp();
        lease.renew();
        Thread.sleep(5);
        lease.renew();
        Assert.assertNotEquals(firstUpdate, lease.getLastUpdateTimestamp());
    }
}