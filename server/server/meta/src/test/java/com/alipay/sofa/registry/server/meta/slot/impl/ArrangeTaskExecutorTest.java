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
package com.alipay.sofa.registry.server.meta.slot.impl;

import com.alipay.sofa.registry.exception.SofaRegistryRuntimeException;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.server.meta.AbstractTest;
import com.alipay.sofa.registry.server.meta.slot.RebalanceTask;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

public class ArrangeTaskExecutorTest extends AbstractTest {

    private ArrangeTaskExecutor executor;

    @Before
    public void beforeArrangeTaskExecutorTest() throws Exception {
        executor = new ArrangeTaskExecutor();
        LifecycleHelper.initializeIfPossible(executor);
        LifecycleHelper.startIfPossible(executor);
    }

    @After
    public void afterArrangeTaskExecutorTest() throws Exception {
        LifecycleHelper.stopIfPossible(executor);
        LifecycleHelper.disposeIfPossible(executor);
    }

    @Test
    public void testSequence() throws InterruptedException {
        int count = 100;
        Queue<Integer> list = new ConcurrentLinkedQueue<>();
        CountDownLatch latch = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            int finalI = i;
            executor.offer(new RebalanceTask() {
                @Override
                public void run() {
                    try {
                        logger.debug("{}", this);
                        list.offer(finalI);
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }

        Assert.assertTrue(latch.await(5, TimeUnit.SECONDS));
        Assert.assertEquals(count, list.size());
        int previous = -1;
        while (true) {
            Integer current = list.poll();
            if (current == null) {
                break;
            }

            Assert.assertTrue(current > previous);
            previous = current;
        }
    }

    @Test(expected = SofaRegistryRuntimeException.class)
    public void testOfferWhileDispose() throws Exception {
        int count = 100;
        Queue<Integer> list = new ConcurrentLinkedQueue<>();
        CountDownLatch latch = new CountDownLatch(count);
        CyclicBarrier barrier = new CyclicBarrier(count + 1);
        scheduled.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    LifecycleHelper.stopIfPossible(executor);
                    LifecycleHelper.disposeIfPossible(executor);
                } catch (Exception ignore) {
                }
            }
        }, 2, TimeUnit.MILLISECONDS);
        for (int i = 0; i < count; i++) {
            int finalI = i;
            Thread.sleep(1);
            executor.offer(new RebalanceTask() {
                @Override
                public void run() {
                    try {
                        list.offer(finalI);
                    } catch (Exception ignore) {

                    } finally {
                        latch.countDown();
                    }
                }
            });
        }
    }

    @Test
    public void testWaitLong() throws InterruptedException {
        int count = 3;
        Queue<Integer> list = new ConcurrentLinkedQueue<>();
        CountDownLatch latch = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            int finalI = i;
            Thread.sleep(100);
            executor.offer(new RebalanceTask() {
                @Override
                public void run() {
                    try {
                        logger.debug("{}", this);
                        list.offer(finalI);
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }

        Assert.assertTrue(latch.await(5, TimeUnit.SECONDS));
        Assert.assertEquals(count, list.size());
        int previous = -1;
        while (true) {
            Integer current = list.poll();
            if (current == null) {
                break;
            }

            Assert.assertTrue(current > previous);
            previous = current;
        }
    }

    @Test
    public void testThrowException() throws InterruptedException {
        int count = 3;
        Queue<Integer> list = new ConcurrentLinkedQueue<>();
        CountDownLatch latch = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            int finalI = i;
            Thread.sleep(10);
            executor.offer(new RebalanceTask() {
                @Override
                public void run() {
                    try {
                        logger.debug("{}", this);
                        list.offer(finalI);
                        throw new SofaRegistryRuntimeException("expected exception");
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }

        Assert.assertTrue(latch.await(5, TimeUnit.SECONDS));
        Assert.assertEquals(count, list.size());
    }
}