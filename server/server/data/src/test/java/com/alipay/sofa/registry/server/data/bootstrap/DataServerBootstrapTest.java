package com.alipay.sofa.registry.server.data.bootstrap;

import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.metrics.TaskMetrics;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.data.change.DataChangeEventCenter;
import com.alipay.sofa.registry.server.data.lease.SessionLeaseManager;
import com.alipay.sofa.registry.server.data.slot.SlotManager;
import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
import com.google.common.collect.Lists;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DataServerBootstrapTest {

    private static final Logger logger = LoggerFactory.getLogger(DataServerBootstrapTest.class);
    @Mock
    private DataServerConfig dataServerConfig;

    @Mock private MetaServerService metaServerService;

    @Mock private ApplicationContext applicationContext;

    @Mock private ResourceConfig jerseyResourceConfig;

    @Mock private SlotManager slotManager;

    @Mock private Exchange jerseyExchange;

    @Mock private Exchange boltExchange;

    @Mock private DataChangeEventCenter dataChangeEventCenter;

//    @Mock private SessionLeaseManager sessionLeaseManager;

    @InjectMocks
    private DataServerBootstrap bootstrap = spy(new DataServerBootstrap());

    @Before
    public void beforeDataServerBootstrapTest() {
        MockitoAnnotations.initMocks(this);
        bootstrap.setServerHandlers(Lists.newArrayList())
                .setServerSyncHandlers(Lists.newArrayList());
    }

    @Test
    public void testStart() throws InterruptedException {
        when(slotManager.getSlotTableEpoch()).thenReturn(SlotTable.INIT.getEpoch());
        ExecutorService executors = Executors.newSingleThreadExecutor();
        CountDownLatch latch = new CountDownLatch(1);
        TaskMetrics.setBoltRegistered(true);
        executors.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    bootstrap.start();
                    latch.countDown();
                } catch (Throwable throwable) {
                    logger.error("[testStart]", throwable);
                }
            }
        });
        latch.await(2, TimeUnit.SECONDS);
        Assert.assertEquals(0, latch.getCount());
    }
}