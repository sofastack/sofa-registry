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
package com.alipay.sofa.registry.server.session.slot;

import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.common.model.slot.func.SlotFunctionRegistry;
import com.alipay.sofa.registry.server.session.AbstractSessionServerTestBase;
import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.shared.slot.SlotTableRecorder;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SlotTableCacheImplTest extends AbstractSessionServerTestBase {

  private static final String DATACENTER = "testdc";

  private SlotTableCacheImpl slotTableCache = new SlotTableCacheImpl();

  private SessionServerConfig sessionServerConfig = TestUtils.newSessionConfig(DATACENTER);

  @Before
  public void init() {
    slotTableCache.setSessionServerConfig(sessionServerConfig);
  }

  @Test
  public void testSlotOf() {

    String dataInfoId = randomString();
    int slotId = slotTableCache.slotOf(dataInfoId);
    Assert.assertEquals(SlotFunctionRegistry.getFunc().slotOf(dataInfoId), slotId);
    AtomicBoolean concurrentResult = new AtomicBoolean(true);
    new ConcurrentExecutor(10, executors)
        .execute(
            new Runnable() {
              @Override
              public void run() {
                String dataInfoId = randomString();
                if (SlotFunctionRegistry.getFunc().slotOf(dataInfoId)
                    != slotTableCache.slotOf(dataInfoId)) {
                  concurrentResult.set(false);
                }
              }
            });
    Assert.assertTrue(concurrentResult.get());
  }

  @Test
  public void testGetInfo() {
    slotTableCache.updateLocalSlotTable(randomSlotTable());
    String dataInfoId = randomString();
    Assert.assertEquals(
        slotTableCache.getSlot(DATACENTER, SlotFunctionRegistry.getFunc().slotOf(dataInfoId)),
        slotTableCache.getSlot(DATACENTER, dataInfoId));
    Assert.assertSame(
        slotTableCache.getSlot(DATACENTER, SlotFunctionRegistry.getFunc().slotOf(dataInfoId)),
        slotTableCache.getSlot(DATACENTER, dataInfoId));
    Assert.assertNull(slotTableCache.getLeader(DATACENTER, 13685));
    Assert.assertNotNull(slotTableCache.getLeader(DATACENTER, 3));
  }

  @Test
  public void testRecorders() throws InterruptedException {
    SlotTable slotTable = randomSlotTable();
    AtomicReference<SlotTable> ref = new AtomicReference<>();
    slotTableCache.setRecorders(
        Lists.newArrayList(
            new SlotTableRecorder() {
              @Override
              public void record(SlotTable slotTable) {
                ref.set(slotTable);
              }
            }));
    CountDownLatch latch = new CountDownLatch(1);
    executors.execute(
        new Runnable() {
          @Override
          public void run() {
            slotTableCache.updateLocalSlotTable(slotTable);
            latch.countDown();
          }
        });
    latch.await();
    Assert.assertNotNull(ref.get());
    Assert.assertEquals(slotTable, ref.get());
  }

  @Test
  public void testUpdateSlotTable() {
    Set<Long> epoches = Sets.newConcurrentHashSet();
    AtomicBoolean result = new AtomicBoolean(true);
    ConsumerProducer.builder()
        .consumerNum(10)
        .consumer(
            new Runnable() {
              @Override
              public void run() {
                try {
                  epoches.add(slotTableCache.getEpoch(DATACENTER));
                } catch (Throwable th) {
                  result.set(false);
                }
              }
            })
        .producerNum(1)
        .producer(
            new Runnable() {
              @Override
              public void run() {
                try {
                  slotTableCache.updateLocalSlotTable(randomSlotTable());
                } catch (Throwable th) {
                  result.set(false);
                }
              }
            })
        .executor(executors)
        .build()
        .execute(null);
    Assert.assertTrue(result.get());
    Assert.assertTrue(epoches.size() > 0);
  }

  @Test
  public void testCurrentSlotTable() throws InterruptedException {
    slotTableCache.updateLocalSlotTable(randomSlotTable());
    Assert.assertNotSame(slotTableCache.getLocalSlotTable(), slotTableCache.getLocalSlotTable());
    Assert.assertEquals(slotTableCache.getLocalSlotTable(), slotTableCache.getLocalSlotTable());
    SlotTable prev = slotTableCache.getLocalSlotTable();
    CountDownLatch latch = new CountDownLatch(1);
    executors.execute(
        new Runnable() {
          @Override
          public void run() {
            slotTableCache.updateLocalSlotTable(randomSlotTable());
            latch.countDown();
          }
        });
    latch.await();
    Assert.assertNotEquals(prev, slotTableCache.getLocalSlotTable());
  }

  @Test
  public void testWillNotUpdateLowerEpoch() {
    slotTableCache.updateLocalSlotTable(randomSlotTable());
    slotTableCache.updateLocalSlotTable(new SlotTable(123, randomSlotTable().getSlots()));
    Assert.assertNotEquals(123, slotTableCache.getLocalSlotTable().getEpoch());
  }

  @Test
  public void testBlankSlot() {
    SlotTable correct = randomSlotTable();
    List<Slot> incorrect = correct.getSlots();
    incorrect.add(new Slot(13684, "", DatumVersionUtil.nextId(), Lists.newArrayList()));
    SlotTable slotTable = new SlotTable(correct.getEpoch(), incorrect);
    slotTableCache.updateLocalSlotTable(slotTable);
  }
}
