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
package com.alipay.sofa.registry.common.model.slot;

import com.alipay.sofa.registry.common.model.slot.func.CRC16SlotFunction;
import com.alipay.sofa.registry.server.meta.AbstractTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class CRC16SlotFunctionTest extends AbstractTest {

    private CRC16SlotFunction slotFunction;

    @Before
    public void beforeCRC32SlotFunctionTest() {
        slotFunction = new CRC16SlotFunction();
    }

    @Test
    public void testMaxSlots() {
        Assert.assertEquals(SlotConfig.SLOT_NUM, slotFunction.maxSlots());
    }

    @Test
    public void testSlotOfMultiThreadSafe() throws InterruptedException {
        int tasks = 100;
        final String mark = randomString();
        int expected = slotFunction.slotOf(mark);
        CyclicBarrier barrier = new CyclicBarrier(tasks);
        CountDownLatch latch = new CountDownLatch(tasks);
        for (int i = 0; i < tasks; i++) {
            executors.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        barrier.await();
                    } catch (Exception e) {
                    }
                    Assert.assertEquals(expected, slotFunction.slotOf(mark));
                    latch.countDown();
                }
            });
        }
        latch.await();
    }
}