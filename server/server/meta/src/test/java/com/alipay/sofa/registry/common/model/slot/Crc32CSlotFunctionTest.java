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

import com.alipay.sofa.registry.common.model.slot.func.Crc32cSlotFunction;
import com.alipay.sofa.registry.util.JsonUtils;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class Crc32CSlotFunctionTest extends BaseSlotFunctionTest {

  private Crc32cSlotFunction slotFunction;

  @Before
  public void beforeCRC32SlotFunctionTest() {
    slotFunction = new Crc32cSlotFunction();
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
      executors.execute(
          new Runnable() {
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

  @Test
  public void testBalance() throws IOException {
    String[] dataInfoIds = getDataInfoIds();
    int slotNums = 256;
    Map<Integer, Integer> counter = Maps.newHashMap();
    for (int i = 0; i < slotNums; i++) {
      counter.put(i, 0);
    }
    slotFunction = new Crc32cSlotFunction(slotNums);
    logger.info("[length] {}", dataInfoIds.length);
    for (String dataInfoId : dataInfoIds) {
      int slotId = slotFunction.slotOf(dataInfoId);
      counter.put(slotId, counter.get(slotId) + 1);
    }
    logger.info(
        "[result]\n{}",
        JsonUtils.getJacksonObjectMapper()
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(counter));
  }
}
