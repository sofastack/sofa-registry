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
package com.alipay.sofa.registry.server.meta.slot.arrange;

import com.alipay.sofa.registry.server.meta.AbstractTest;
import com.alipay.sofa.registry.server.meta.lease.data.DefaultDataServerManager;
import com.alipay.sofa.registry.server.meta.monitor.SlotTableMonitor;
import com.alipay.sofa.registry.server.meta.resource.SlotTableResource;
import com.alipay.sofa.registry.server.meta.slot.manager.DefaultSlotManager;
import com.alipay.sofa.registry.server.meta.slot.manager.LocalSlotManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class ScheduledSlotArrangerTest extends AbstractTest {

    private ScheduledSlotArranger    slotArranger;

    private SlotTableResource        slotTableResource;

    @Mock
    private DefaultSlotManager       defaultSlotManager;

    @Mock
    private DefaultDataServerManager dataServerManager;

    @Mock
    private LocalSlotManager         localSlotManager;

    @Mock
    private SlotTableMonitor         slotTableMonitor;

    @Before
    public void beforeScheduledSlotArrangerTest() {
        MockitoAnnotations.initMocks(this);
        slotArranger = spy(new ScheduledSlotArranger(dataServerManager, localSlotManager,
            defaultSlotManager, slotTableMonitor));
    }

    @Test
    public void testTryLock() throws InterruptedException {
        Assert.assertTrue(slotArranger.tryLock());
        slotArranger.unlock();
        Assert.assertTrue(slotArranger.tryLock());
        slotArranger.unlock();
        AtomicInteger counter = new AtomicInteger();
        int tasks = 100;
        CyclicBarrier barrier = new CyclicBarrier(tasks);
        CountDownLatch latch = new CountDownLatch(tasks);
        for (int i = 0; i < tasks; i++) {
            executors.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        barrier.await();
                        if (slotArranger.tryLock()) {
                            counter.incrementAndGet();
                        }
                    } catch (Throwable th) {

                    } finally {
                        latch.countDown();
                    }
                }
            });
        }
        latch.await();
        Assert.assertEquals(1, counter.get());
    }

    @Test
    public void testStopStartReconcile() throws Exception {
        slotTableResource = new SlotTableResource(defaultSlotManager, localSlotManager,
            dataServerManager, slotArranger);
        slotArranger.postConstruct();
        Assert.assertEquals("running", slotTableResource.getReconcileStatus().getMessage());

        slotTableResource.stopSlotTableReconcile();
        Assert.assertEquals("stopped", slotTableResource.getReconcileStatus().getMessage());

        slotTableResource.startSlotTableReconcile();
        Assert.assertEquals("running", slotTableResource.getReconcileStatus().getMessage());
    }

    //    @Test
    // TODO
    public void testLongTermStopped() throws Exception {
        slotTableResource = new SlotTableResource(defaultSlotManager, localSlotManager,
            dataServerManager, slotArranger);
        slotArranger.postConstruct();
        Assert.assertEquals("running", slotTableResource.getReconcileStatus().getMessage());

        slotTableResource.stopSlotTableReconcile();
        Assert.assertEquals("stopped", slotTableResource.getReconcileStatus().getMessage());

        for (int i = 0; i < 100; i++) {
            Thread.sleep(1000);
            Assert.assertEquals("stopped", slotTableResource.getReconcileStatus().getMessage());
        }

    }

}