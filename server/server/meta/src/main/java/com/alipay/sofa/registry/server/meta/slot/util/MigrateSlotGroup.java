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

import com.alipay.sofa.registry.server.meta.slot.assigner.ScoreStrategy;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.*;
import org.glassfish.jersey.internal.guava.Sets;

/**
 * @author chen.zhu
 *     <p>Jan 12, 2021
 *     <p>MigrateSlotGroup is used as a model object to cache all slots we wish to migrate Separated
 *     through different roles(Leader/Follower)
 */
public class MigrateSlotGroup {

  /** slotId */
  private final Set<Integer> leaders = Sets.newHashSet();
  /** key: slotId, value: counter (one slot could has multi followers to migrate) */
  private final Map<Integer, Integer> lackFollowers = Maps.newHashMap();

  /**
   * Add leader migrate slot group.
   *
   * @param slotId the slot id
   * @return the migrate slot group
   */
  public MigrateSlotGroup addLeader(int slotId) {
    leaders.add(slotId);
    return this;
  }

  public boolean isEmpty() {
    return leaders.isEmpty() && lackFollowers.isEmpty();
  }

  /**
   * Add follower migrate slot group.
   *
   * @param slotId the slot id
   * @return the migrate slot group
   */
  public MigrateSlotGroup addFollower(int slotId) {
    lackFollowers.put(slotId, lackFollowers.getOrDefault(slotId, 0) + 1);
    return this;
  }

  public MigrateSlotGroup addFollower(int slotId, int num) {
    lackFollowers.put(slotId, lackFollowers.getOrDefault(slotId, 0) + num);
    return this;
  }

  public List<Integer> getLeadersByScore(ScoreStrategy<Integer> scoreStrategy) {
    List<Integer> leaders = Lists.newArrayList(this.leaders);
    leaders.sort(
        new Comparator<Integer>() {
          @Override
          public int compare(Integer slotId1, Integer slotId2) {
            return scoreStrategy.score(slotId1) - scoreStrategy.score(slotId2);
          }
        });
    return leaders;
  }

  public List<FollowerToAssign> getFollowersByScore(ScoreStrategy<FollowerToAssign> scoreStrategy) {
    List<FollowerToAssign> assignees = Lists.newArrayList();
    lackFollowers.forEach((slotId, nums) -> assignees.add(new FollowerToAssign(slotId, nums)));
    assignees.sort(
        new Comparator<FollowerToAssign>() {
          @Override
          public int compare(FollowerToAssign o1, FollowerToAssign o2) {
            return scoreStrategy.score(o1) - scoreStrategy.score(o2);
          }
        });
    return assignees;
  }

  /**
   * Gets get leaders.
   *
   * @return the get leaders
   */
  public Set<Integer> getLeaders() {
    return Collections.unmodifiableSet(leaders);
  }

  /**
   * Gets get followers.
   *
   * @return the get followers
   */
  public Map<Integer, Integer> getLackFollowers() {
    return Collections.unmodifiableMap(lackFollowers);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MigrateSlotGroup that = (MigrateSlotGroup) o;
    return Objects.equals(leaders, that.leaders)
        && Objects.equals(lackFollowers, that.lackFollowers);
  }

  @Override
  public int hashCode() {
    return Objects.hash(leaders, lackFollowers);
  }

  @Override
  public String toString() {
    return "MigrateSlotGroup{" + "leaders=" + leaders + ", lackFollowers=" + lackFollowers + '}';
  }

  public static class FollowerToAssign {
    private final int slotId;
    private final int assigneeNums;

    public FollowerToAssign(int slotId, int assigneeNums) {
      this.slotId = slotId;
      this.assigneeNums = assigneeNums;
    }

    public int getSlotId() {
      return slotId;
    }

    public int getAssigneeNums() {
      return assigneeNums;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      FollowerToAssign that = (FollowerToAssign) o;
      return slotId == that.slotId && assigneeNums == that.assigneeNums;
    }

    @Override
    public int hashCode() {
      return Objects.hash(slotId, assigneeNums);
    }
  }
}
