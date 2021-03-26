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
package com.alipay.sofa.registry.server.meta.slot.util;

import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.exception.SofaRegistryRuntimeException;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.slot.util.builder.SlotBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author chen.zhu
 *     <p>Jan 15, 2021
 */
public class SlotBuilderTest extends AbstractMetaServerTestBase {

  private SlotBuilder slotBuilder;

  @Before
  public void beforeSlotBuilderTest() {
    slotBuilder = new SlotBuilder(1, 1);
  }

  @Test
  public void testSetLeader() {
    slotBuilder = new SlotBuilder(1, 1, "10.0.0.1", 100);
    slotBuilder.setLeader("10.0.0.2");
    Assert.assertEquals("10.0.0.2", slotBuilder.getLeader());
    Assert.assertEquals(1, slotBuilder.getSlotId());
  }

  @Test
  public void testForceSetLeader() {
    slotBuilder = new SlotBuilder(1, 1, "10.0.0.1", 100);
    long epoch = slotBuilder.getEpoch();
    slotBuilder.setLeader("10.0.0.2");
    Assert.assertEquals("10.0.0.2", slotBuilder.getLeader());
    Assert.assertNotEquals(epoch, slotBuilder.getEpoch());
    Assert.assertEquals(1, slotBuilder.getSlotId());
  }

  @Test
  public void testAddFollower() {
    slotBuilder = new SlotBuilder(1, 1, "10.0.0.1", 100);
    long epoch = slotBuilder.getEpoch();
    slotBuilder.addFollower("10.0.0.2");
    Assert.assertEquals(epoch, slotBuilder.getEpoch());
    Assert.assertTrue(slotBuilder.getFollowers().contains("10.0.0.2"));
    try {
      slotBuilder.addFollower("10.0.0.3");
    } catch (Exception e) {
      e.printStackTrace();
    }
    Assert.assertEquals(epoch, slotBuilder.getEpoch());
    Assert.assertFalse(slotBuilder.getFollowers().contains("10.0.0.3"));
  }

  @Test
  public void testRemoveFollower() {
    slotBuilder = new SlotBuilder(1, 1, "10.0.0.1", 100);
    long epoch = slotBuilder.getEpoch();
    slotBuilder.addFollower("10.0.0.2");
    Assert.assertEquals(epoch, slotBuilder.getEpoch());
    Assert.assertTrue(slotBuilder.getFollowers().contains("10.0.0.2"));

    slotBuilder.removeFollower("10.0.0.2");
    slotBuilder.addFollower("10.0.0.3");
    Assert.assertEquals(epoch, slotBuilder.getEpoch());
    Assert.assertTrue(slotBuilder.getFollowers().contains("10.0.0.3"));
  }

  @Test(expected = SofaRegistryRuntimeException.class)
  public void testBuild() {
    slotBuilder = new SlotBuilder(1, 1, "10.0.0.1", 100);
    slotBuilder.setLeader(null);
    slotBuilder.build();
  }

  @Test
  public void testBuildNormal() {
    slotBuilder = new SlotBuilder(1, 1, "10.0.0.1", 100);
    slotBuilder.setLeader("20.0.0.2");
    Slot slot = slotBuilder.build();
    Assert.assertEquals("20.0.0.2", slot.getLeader());
    logger.info("[slot-builder] {}", slotBuilder.toString());
    logger.info("[slot] {}", slot.toString());
  }
}
