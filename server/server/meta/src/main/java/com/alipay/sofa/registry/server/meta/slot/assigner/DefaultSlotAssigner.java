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
package com.alipay.sofa.registry.server.meta.slot.assigner;

import com.alipay.sofa.registry.common.model.slot.DataNodeSlot;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.exception.SofaRegistryRuntimeException;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.monitor.Metrics;
import com.alipay.sofa.registry.server.meta.slot.SlotAssigner;
import com.alipay.sofa.registry.server.meta.slot.balance.BalancePolicy;
import com.alipay.sofa.registry.server.meta.slot.balance.NaiveBalancePolicy;
import com.alipay.sofa.registry.server.meta.slot.util.MigrateSlotGroup;
import com.alipay.sofa.registry.server.meta.slot.util.builder.SlotBuilder;
import com.alipay.sofa.registry.server.meta.slot.util.builder.SlotTableBuilder;
import com.alipay.sofa.registry.server.meta.slot.util.comparator.Comparators;
import com.alipay.sofa.registry.server.meta.slot.util.comparator.SortType;
import com.alipay.sofa.registry.server.meta.slot.util.selector.Selectors;
import com.alipay.sofa.registry.util.MathUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author chen.zhu
 *     <p>Jan 15, 2021
 */
public class DefaultSlotAssigner implements SlotAssigner {

  private static final Logger logger = LoggerFactory.getLogger(DefaultSlotAssigner.class);

  private final Set<String> currentDataServers;

  protected final SlotTableBuilder slotTableBuilder;
  private final MigrateSlotGroup migrateSlotGroup;

  public DefaultSlotAssigner(
      SlotTableBuilder slotTableBuilder, Collection<String> currentDataServers) {
    this.slotTableBuilder = slotTableBuilder;
    this.currentDataServers = Collections.unmodifiableSet(Sets.newTreeSet(currentDataServers));
    this.migrateSlotGroup = slotTableBuilder.getNoAssignedSlots();
  }

  @Override
  public SlotTable assign() {
    if (migrateSlotGroup.isEmpty() || currentDataServers.isEmpty()) {
      throw new SofaRegistryRuntimeException(
          "no pending slot or available dataServers for reassignment");
    }
    BalancePolicy balancePolicy = new NaiveBalancePolicy();
    final int ceilAvg =
        MathUtils.divideCeil(slotTableBuilder.getSlotNums(), currentDataServers.size());
    final int high = balancePolicy.getHighWaterMarkSlotLeaderNums(ceilAvg);
    logger.info(
        "[assign][assignLeaderSlots] begin, dataServers={}, avg={}, high={}, migrate={}",
        currentDataServers.size(),
        ceilAvg,
        high,
        migrateSlotGroup);
    if (tryAssignLeaderSlots(high)) {
      logger.info("[assign][after assignLeaderSlots] end -- leader changed");
      slotTableBuilder.incrEpoch();
    } else {
      logger.info("[assign][after assignLeaderSlots] end -- no changes");
    }

    logger.info("[assign][assignFollowerSlots] begin");
    if (assignFollowerSlots()) {
      logger.info("[assign][after assignFollowerSlots] end -- follower changed");
      slotTableBuilder.incrEpoch();
    } else {
      logger.info("[assign][assignFollowerSlots] end -- no changes");
    }
    return slotTableBuilder.build();
  }

  private boolean tryAssignLeaderSlots(int highWatermark) {
    /**
     * our strategy(assign leader) is to swap follower to leader when follower is enabled if no
     * followers, we select a new data-server to assign, that's simple and low prioritized so,
     * leaders are resolved in an order that who has least followers first (as we wish to satisfy
     * these nodes first) leaders with no follower is lowest priority, as whatever we did, it will
     * pick up a candidate that is not its follower
     */
    List<Integer> leaders =
        migrateSlotGroup.getLeadersByScore(new FewerFollowerFirstStrategy(slotTableBuilder));
    if (leaders.isEmpty()) {
      logger.info("[assignLeaderSlots] no slot leader needs assign, quit");
      return false;
    }
    for (int slotId : leaders) {
      List<String> currentDataNodes = Lists.newArrayList(currentDataServers);
      String nextLeader =
          Selectors.slotLeaderSelector(highWatermark, slotTableBuilder, slotId)
              .select(currentDataNodes);
      boolean nextLeaderWasFollower = isNextLeaderFollowerOfSlot(slotId, nextLeader);
      logger.info(
          "[assignLeaderSlots]assign slot[{}] leader as [{}], upgrade={}, dataServers={}",
          slotId,
          nextLeader,
          nextLeaderWasFollower,
          currentDataServers.size());
      slotTableBuilder.replaceLeader(slotId, nextLeader);
      Metrics.SlotAssign.onSlotLeaderAssign(nextLeader, slotId);
      if (nextLeaderWasFollower) {
        migrateSlotGroup.addFollower(slotId);
      }
    }
    return true;
  }

  private boolean isNextLeaderFollowerOfSlot(int slotId, String nextLeader) {
    return slotTableBuilder.getOrCreate(slotId).containsFollower(nextLeader);
  }

  private boolean assignFollowerSlots() {
    List<MigrateSlotGroup.FollowerToAssign> followerToAssigns =
        migrateSlotGroup.getFollowersByScore(new FollowerEmergentScoreJury());
    if (followerToAssigns.isEmpty()) {
      logger.info("[assignFollowerSlots] no follower slots need to assign, quit");
      return false;
    }
    int assignCount = 0;
    for (MigrateSlotGroup.FollowerToAssign followerToAssign : followerToAssigns) {
      final int slotId = followerToAssign.getSlotId();
      for (int i = 0; i < followerToAssign.getAssigneeNums(); i++) {
        List<String> candidates = Lists.newArrayList(currentDataServers);
        candidates.sort(Comparators.leastFollowersFirst(slotTableBuilder));
        boolean assigned = false;
        for (String candidate : candidates) {
          DataNodeSlot dataNodeSlot = slotTableBuilder.getDataNodeSlot(candidate);
          if (dataNodeSlot.containsFollower(slotId) || dataNodeSlot.containsLeader(slotId)) {
            continue;
          }
          slotTableBuilder.addFollower(slotId, candidate);
          Metrics.SlotAssign.onSlotFollowerAssign(candidate, slotId);
          assigned = true;
          assignCount++;
          logger.info(
              "[assignFollowerSlots]assign slot[{}] add follower as [{}], dataServers={}",
              slotId,
              candidate,
              currentDataServers.size());
          break;
        }
        if (!assigned) {
          logger.warn(
              "[assignFollowerSlots]assign slot[{}] no dataServer to assign, dataServers={}",
              slotId,
              currentDataServers.size());
        }
      }
    }
    return assignCount != 0;
  }

  public Set<String> getCurrentDataServers() {
    return currentDataServers;
  }

  public SlotTableBuilder getSlotTableBuilder() {
    return slotTableBuilder;
  }

  public MigrateSlotGroup getMigrateSlotGroup() {
    return migrateSlotGroup;
  }

  static class FewerFollowerFirstStrategy implements ScoreStrategy<Integer> {

    final SlotTableBuilder slotTableBuilder;

    FewerFollowerFirstStrategy(SlotTableBuilder slotTableBuilder) {
      this.slotTableBuilder = slotTableBuilder;
    }

    @Override
    public int score(Integer slotId) {
      SlotBuilder slotBuilder = slotTableBuilder.getOrCreate(slotId);
      int followerNums = slotBuilder.getFollowers().size();
      // if no followers, we leave it the least priority
      // because our strategy(assign leader) is to swap follower to leader when follower is enabled
      // if no followers, we select a new data-server to assign, that's simple and low prioritized
      if (followerNums == 0) {
        return Integer.MAX_VALUE;
      }
      return SortType.ASC.getScore(slotBuilder.getFollowers().size());
    }
  }

  public static class FollowerEmergentScoreJury
      implements ScoreStrategy<MigrateSlotGroup.FollowerToAssign> {

    @Override
    public int score(MigrateSlotGroup.FollowerToAssign followerToAssign) {
      return SortType.DES.getScore(followerToAssign.getAssigneeNums());
    }
  }
}
