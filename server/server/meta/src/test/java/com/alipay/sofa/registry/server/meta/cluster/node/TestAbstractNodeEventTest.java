package com.alipay.sofa.registry.server.meta.cluster.node;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.lease.impl.DefaultLeaseManagerTest;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestAbstractNodeEventTest {

    private Logger logger = LoggerFactory.getLogger(TestAbstractNodeEventTest.class);

    @Test
    public void testGetNode() {
        DefaultLeaseManagerTest.SimpleNode simpleNode = new DefaultLeaseManagerTest.SimpleNode("127.0.0.1");
        AbstractNodeEvent event = new AbstractNodeEvent(simpleNode) {};
        Assert.assertEquals(simpleNode, event.getNode());
        logger.info("[testGetNode] {}", event);
    }
}