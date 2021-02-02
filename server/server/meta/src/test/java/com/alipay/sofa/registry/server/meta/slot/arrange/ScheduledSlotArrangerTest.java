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

import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.exception.SofaRegistrySlotTableException;
import com.alipay.sofa.registry.server.meta.AbstractTest;
import com.alipay.sofa.registry.server.meta.slot.SlotAssigner;
import com.alipay.sofa.registry.server.meta.slot.SlotBalancer;
import com.alipay.sofa.registry.server.meta.slot.util.builder.SlotTableBuilder;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class ScheduledSlotArrangerTest extends AbstractTest {

    private ScheduledSlotArranger slotArranger;

    @Before
    public void beforeScheduledSlotArrangerTest() {
        slotArranger = spy(new ScheduledSlotArranger());
    }

    @Test
    public void testTryLock() throws InterruptedException {
        Assert.assertTrue(slotArranger.tryLock());
        Assert.assertFalse(slotArranger.tryLock());
        Assert.assertFalse(slotArranger.tryLock());
        Assert.assertFalse(slotArranger.tryLock());
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

    //    @Test(expected = SofaRegistrySlotTableException.class)
    //    public void testAssignSlots() {
    //        slotArranger = new ScheduledSlotArranger() {
    //            @Override
    //            protected SlotAssigner createSlotAssigner() {
    //                return new SlotAssigner() {
    //                    @Override
    //                    public SlotTable assign() {
    //                        SlotTable slotTable = randomSlotTable();
    //                        SlotTableBuilder stb = new SlotTableBuilder(16, 2);
    //                        stb.init(slotTable, Lists.newArrayList(slotTable.getDataServers()));
    //                        stb.getOrCreate(0).getFollowers().clear();
    //                        stb.getOrCreate(0).getFollowers().add(slotTable.getSlot(0).getLeader());
    //                        return stb.build();
    //                    }
    //                };
    //            }
    //        };
    //        slotArranger.assignSlots();
    //    }
    //
    //    @Test(expected = SofaRegistrySlotTableException.class)
    //    public void testBalanceSlots() {
    //        slotArranger = new ScheduledSlotArranger() {
    //            @Override
    //            protected SlotBalancer createSlotBalancer() {
    //                return new SlotBalancer() {
    //                    @Override
    //                    public SlotTable balance() {
    //                        SlotTable slotTable = randomSlotTable();
    //                        SlotTableBuilder stb = new SlotTableBuilder(16, 2);
    //                        stb.init(slotTable, Lists.newArrayList(slotTable.getDataServers()));
    //                        stb.getOrCreate(0).getFollowers().clear();
    //                        stb.getOrCreate(0).getFollowers().add(slotTable.getSlot(0).getLeader());
    //                        return stb.build();
    //                    }
    //                };
    //            }
    //        };
    //        slotArranger.balanceSlots();
    //    }

}