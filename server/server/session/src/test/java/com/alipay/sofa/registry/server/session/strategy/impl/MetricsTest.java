package com.alipay.sofa.registry.server.session.strategy.impl;

import org.junit.Test;

public class MetricsTest {
    @Test
    public void testMetrics(){
        Metrics.Access.REGISTRY_CLIENT_PUB_SIZE.observe(1);
    }
}
