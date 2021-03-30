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
package com.alipay.sofa.registry.server.meta.slot.balance;

import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.slot.DataNodeSlot;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.slot.SlotBalancer;
import com.alipay.sofa.registry.server.meta.slot.util.builder.SlotTableBuilder;
import com.alipay.sofa.registry.server.meta.slot.util.comparator.Comparators;
import com.alipay.sofa.registry.server.shared.slot.SlotTableUtils;
import com.alipay.sofa.registry.util.MathUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.*;

/**
 * @author chen.zhu
 *     <p>Jan 21, 2021
 */
public class LeaderOnlyBalancer implements SlotBalancer {

  private static final Logger logger = LoggerFactory.getLogger(LeaderOnlyBalancer.class);

  public static final int TRIGGER_THESHOLD = 2;

  private final SlotTableBuilder slotTableBuilder;

  private final Set<String> currentDataServers;

  private final NaiveBalancePolicy balancePolicy = new NaiveBalancePolicy();

  public LeaderOnlyBalancer(
      SlotTableBuilder slotTableBuilder, Collection<String> currentDataServers) {
    this.slotTableBuilder = slotTableBuilder;
    this.currentDataServers = Collections.unmodifiableSet(Sets.newTreeSet(currentDataServers));
  }

  @Override
  public SlotTable balance() {
    final int leaderCeilAvg =
        MathUtils.divideCeil(slotTableBuilder.getSlotNums(), currentDataServers.size());
    final int maxMove = balancePolicy.getMaxMoveLeaderSlots();
    int balanced = 0;
    while (balanced < maxMove) {
      List<String> targetHighDataServers = findDataServersLeaderHighWaterMark(leaderCeilAvg);
      if (targetHighDataServers.isEmpty()) {
        logger.info("[balance]no data-servers needs leader balance");
        // no balance, return null
        break;
      }
      final Set<String> excludes = Sets.newHashSet(targetHighDataServers);
      // exclude the dataNode which could not add any leader
      excludes.addAll(findDataServersLeaderHighWaterMark(leaderCeilAvg - 1));

      for (String target : targetHighDataServers) {
        Tuple<String, Integer> candidate = selectLeader(target, excludes);
        if (candidate == null) {
          continue;
        }
        final String newLeader = candidate.o1;
        final int slotId = candidate.o2;
        slotTableBuilder.replaceLeader(slotId, newLeader);
        balanced++;
        break;
      }
    }
    if (balanced != 0) {
      SlotTable slotTable = slotTableBuilder.build();
      logger.info(
          "[balance][end] slot table leader stat: {}",
          SlotTableUtils.getSlotTableLeaderCount(slotTable));
      logger.info(
          "[balance][end] slot table slots stat: {}",
          SlotTableUtils.getSlotTableSlotCount(slotTable));
      return slotTable;
    }
    return null;
  }

  private Tuple<String, Integer> selectLeader(String leaderDataServer, Set<String> excludes) {
    final DataNodeSlot dataNodeSlot = slotTableBuilder.getDataNodeSlot(leaderDataServer);
    List<String> candidates =
        getCandidateDataServers(
            excludes, Comparators.leastLeadersFirst(slotTableBuilder), currentDataServers);
    logger.info(
        "[selectLeader] target={}, leaders={}, candidates={}",
        leaderDataServer,
        dataNodeSlot.getLeaders().size(),
        candidates);
    for (int leader : dataNodeSlot.getLeaders()) {
      for (String candidate : candidates) {
        return Tuple.of(candidate, leader);
      }
    }
    return null;
  }

  private List<String> findDataServersLeaderHighWaterMark(int avg) {
    int threshold = balancePolicy.getHighWaterMarkSlotLeaderNums(avg);
    List<DataNodeSlot> dataNodeSlots = slotTableBuilder.getDataNodeSlotsLeaderBeyond(threshold);
    List<String> dataServers = DataNodeSlot.collectDataNodes(dataNodeSlots);
    dataServers.sort(Comparators.mostLeadersFirst(slotTableBuilder));
    logger.info(
        "[LeaderHighWaterMark] avg={}, threshold={}, dataServers={}", avg, threshold, dataServers);
    return dataServers;
  }

  private List<String> getCandidateDataServers(
      Collection<String> excludes,
      Comparator<String> comp,
      Collection<String> candidateDataServers) {
    Set<String> candidates = Sets.newHashSet(candidateDataServers);
    candidates.removeAll(excludes);
    List<String> ret = Lists.newArrayList(candidates);
    if (comp != null) {
      ret.sort(comp);
    }
    return ret;
  }

  @VisibleForTesting
  public LeaderOnlyBalancer setMaxMoveLeaderSlots(int max) {
    this.balancePolicy.setMaxMoveLeaderSlots(max);
    return this;
  }
}
