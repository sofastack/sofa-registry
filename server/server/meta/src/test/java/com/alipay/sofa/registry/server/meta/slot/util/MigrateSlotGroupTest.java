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

import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.slot.assigner.ScoreStrategy;
import com.alipay.sofa.registry.server.meta.slot.util.comparator.SortType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class MigrateSlotGroupTest extends AbstractMetaServerTestBase {

  private MigrateSlotGroup migrateSlotGroup = new MigrateSlotGroup();

  @Test
  public void testAddLeader() {
    migrateSlotGroup.addLeader(1);
    migrateSlotGroup.addLeader(2);
    Assert.assertEquals(Sets.newHashSet(1, 2), migrateSlotGroup.getLeaders());
  }

  @Test
  public void testAddFollower() {
    migrateSlotGroup.addFollower(1);
    migrateSlotGroup.addFollower(2);
    Assert.assertEquals(ImmutableMap.of(1, 1, 2, 1), migrateSlotGroup.getLackFollowers());
  }

  @Test
  public void testTestAddFollower() {
    migrateSlotGroup.addFollower(1, 2);
    migrateSlotGroup.addFollower(2, 3);
    Assert.assertEquals(ImmutableMap.of(1, 2, 2, 3), migrateSlotGroup.getLackFollowers());
    logger.info("[migrate slot group] {}", migrateSlotGroup.toString());
  }

  @Test
  public void testGetLeadersByScore() {
    migrateSlotGroup.addLeader(1).addLeader(2).addLeader(3).addLeader(4).addLeader(5);
    List<Integer> result =
        migrateSlotGroup.getLeadersByScore(
            new ScoreStrategy<Integer>() {
              @Override
              public int score(Integer slotId) {
                return SortType.DES.getScore(slotId);
              }
            });
    Assert.assertEquals(Lists.newArrayList(5, 4, 3, 2, 1), result);
  }

  @Test
  public void testGetFollowersByScore() {
    migrateSlotGroup.addFollower(1, 2).addFollower(2, 3).addFollower(3, 4).addFollower(4, 5);
    List<MigrateSlotGroup.FollowerToAssign> result =
        migrateSlotGroup.getFollowersByScore(
            new ScoreStrategy<MigrateSlotGroup.FollowerToAssign>() {
              @Override
              public int score(MigrateSlotGroup.FollowerToAssign followerToAssign) {
                return SortType.ASC.getScore(followerToAssign.getAssigneeNums());
              }
            });
    Assert.assertEquals(
        Lists.newArrayList(
            new MigrateSlotGroup.FollowerToAssign(1, 2),
            new MigrateSlotGroup.FollowerToAssign(2, 3),
            new MigrateSlotGroup.FollowerToAssign(3, 4),
            new MigrateSlotGroup.FollowerToAssign(4, 5)),
        result);
  }
}
