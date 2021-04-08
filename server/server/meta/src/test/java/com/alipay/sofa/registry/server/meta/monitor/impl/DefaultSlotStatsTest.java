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
package com.alipay.sofa.registry.server.meta.monitor.impl;

import com.alipay.sofa.registry.common.model.slot.BaseSlotStatus;
import com.alipay.sofa.registry.common.model.slot.FollowerSlotStatus;
import com.alipay.sofa.registry.common.model.slot.LeaderSlotStatus;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.server.meta.monitor.SlotStats;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author zhuchen
 * @date Mar 2, 2021, 11:10:58 AM
 */
public class DefaultSlotStatsTest {

  private DefaultSlotStats slotStats;

  private Slot slot;

  @Before
  public void beforeDefaultSlotStatsTest() {
    slot =
        new Slot(
            1, "10.0.0.1", System.currentTimeMillis(), Lists.newArrayList("10.0.0.2", "10.0.0.3"));
    slotStats = new DefaultSlotStats(slot, 1000 * 60 * 3);
  }

  @Test
  public void testGetSlot() {
    Assert.assertEquals(slot, slotStats.getSlot());
  }

  @Test
  public void testIsLeaderStable() {
    Assert.assertFalse(slotStats.isLeaderStable());
    slotStats.updateLeaderState(
        new LeaderSlotStatus(
            1, System.currentTimeMillis(), "10.0.0.1", BaseSlotStatus.LeaderStatus.HEALTHY));
    Assert.assertTrue(slotStats.isLeaderStable());
  }

  @Test
  public void testIsFollowerStable() {
    Assert.assertFalse(slotStats.isFollowerStable(null));
    Assert.assertFalse(slotStats.isFollowerStable(""));
    Assert.assertFalse(slotStats.isFollowerStable("10.0.0.2"));
    slotStats.updateFollowerState(
        new FollowerSlotStatus(1, System.currentTimeMillis(), "10.0.0.2", -1, -1));
    Assert.assertFalse(slotStats.isFollowerStable("10.0.0.2"));
    slotStats.updateFollowerState(
        new FollowerSlotStatus(
            1,
            System.currentTimeMillis(),
            "10.0.0.2",
            System.currentTimeMillis(),
            System.currentTimeMillis() - 3000));
    Assert.assertTrue(slotStats.isFollowerStable("10.0.0.2"));

    Assert.assertFalse(slotStats.isFollowersStable());

    slotStats.updateFollowerState(
        new FollowerSlotStatus(
            1,
            System.currentTimeMillis(),
            "10.0.0.3",
            System.currentTimeMillis(),
            System.currentTimeMillis() - 3000));
    Assert.assertTrue(slotStats.isFollowersStable());
  }

  @Test
  public void testUpdateFollowerState() {
    slotStats.updateFollowerState(
        new FollowerSlotStatus(1, System.currentTimeMillis(), "10.0.0.2", -1, -1));
    Assert.assertFalse(slotStats.isFollowerStable("10.0.0.2"));
  }

  @Test
  public void testEuqals() {
    SlotStats slotStatsCopy = new DefaultSlotStats(slotStats.getSlot(), 3000);
    slotStats.updateFollowerState(
        new FollowerSlotStatus(1, System.currentTimeMillis(), "10.0.0.2", -1, -1));
    Assert.assertNotEquals(slotStatsCopy, slotStats);
  }
}
