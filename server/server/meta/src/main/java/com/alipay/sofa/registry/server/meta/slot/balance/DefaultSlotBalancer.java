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

import com.alipay.sofa.registry.common.model.slot.DataNodeSlot;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.exception.SofaRegistryRuntimeException;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.lease.data.DataServerManager;
import com.alipay.sofa.registry.server.meta.slot.SlotBalancer;
import com.alipay.sofa.registry.server.meta.slot.manager.LocalSlotManager;
import com.alipay.sofa.registry.server.meta.slot.util.builder.SlotBuilder;
import com.alipay.sofa.registry.server.meta.slot.util.builder.SlotTableBuilder;
import com.alipay.sofa.registry.server.meta.slot.util.comparator.Comparators;
import com.alipay.sofa.registry.server.meta.slot.util.filter.Filters;
import com.alipay.sofa.registry.server.meta.slot.util.selector.Selectors;
import com.alipay.sofa.registry.server.shared.slot.SlotTableUtils;
import com.alipay.sofa.registry.server.shared.util.NodeUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author chen.zhu
 * <p>
 * Jan 15, 2021
 */
public class DefaultSlotBalancer implements SlotBalancer {

    private static final Logger      logger        = LoggerFactory
                                                       .getLogger(DefaultSlotBalancer.class);

    private final LocalSlotManager   slotManager;

    private final SlotTable          prevSlotTable;

    private final List<String>       currentDataServers;

    protected final SlotTableBuilder slotTableBuilder;

    private BalancePolicy            balancePolicy = new NaiveBalancePolicy();

    public DefaultSlotBalancer(LocalSlotManager slotManager, DataServerManager dataServerManager) {
        this.slotManager = slotManager;
        this.prevSlotTable = slotManager.getSlotTable();
        this.currentDataServers = NodeUtils.transferNodeToIpList(dataServerManager
            .getClusterMembers());
        this.slotTableBuilder = new SlotTableBuilder(slotManager.getSlotNums(),
            slotManager.getSlotReplicaNums());
    }

    @Override
    public SlotTable balance() {
        if (currentDataServers.isEmpty()) {
            logger.error("[no available data-servers] quit");
            throw new SofaRegistryRuntimeException(
                "no available data-servers for slot-table reassignment");
        }
        slotTableBuilder.init(prevSlotTable, currentDataServers);
        if (slotManager.getSlotReplicaNums() < 2) {
            logger.warn("[balance] slot replica[{}] means no followers, balance leader only",
                slotManager.getSlotReplicaNums());
            return new LeaderOnlyBalancer(slotTableBuilder, currentDataServers, slotManager)
                .balance();
        }

        if (tryBalanceLeaderSlots()) {
            logger.info("[assignLeaderSlots] end");
            slotTableBuilder.incrEpoch();
        } else if (tryBalanceFollowerSlots()) {
            logger.info("[balanceFollowerSlots] end");
            slotTableBuilder.incrEpoch();
        }
        return slotTableBuilder.build();
    }

    private boolean tryBalanceLeaderSlots() {
        boolean result = false;
        int avg = slotManager.getSlotNums() / currentDataServers.size();
        List<String> targetDataServers = findDataServersNeedLeaderSlots(avg);
        if (targetDataServers.isEmpty()) {
            logger.info("[balanceLeaderSlots]no leader slots need to balance, quit");
            return false;
        } else {
            logger.info("[balanceLeaderSlots] data-servers: {}, need to assign slot leaders",
                targetDataServers);
            logger.info("[balanceLeaderSlots][begin] slot table leader stat: {}",
                SlotTableUtils.getSlotTableLeaderCount(prevSlotTable));
            logger.info("[balanceLeaderSlots][begin] slot table slots stat: {}",
                SlotTableUtils.getSlotTableSlotCount(prevSlotTable));
        }
        for (String dataServer : targetDataServers) {
            // we flip the target data follower slot to be leader of the slot
            DataNodeSlot dataNodeSlot = slotTableBuilder.getDataNodeSlot(dataServer);
            // we use maxMove to limit the maximum movement slots each time
            // it's now the min(2, avg - [current leader nums], [candidate available nums])
            int maxMove = balancePolicy
                .getMaxMoveLeaderSlots(avg, dataNodeSlot.getLeaders().size());//avg - dataNodeSlot.getLeaders().size();
            maxMove = Math.min(maxMove, dataNodeSlot.getFollowers().size());
            List<Integer> targetSlots = Filters.balanceLeaderFilter(this, targetDataServers)
                .filter(dataNodeSlot.getFollowers());
            if (targetSlots.isEmpty()) {
                logger
                    .info(
                        "[findTargetLeaderSlots] data-server[{}], no slots be able from follower to become leader",
                        dataNodeSlot.getDataNode());
                continue;
            }
            while (maxMove-- > 0) {
                targetSlots.sort(Comparators.slotLeaderHasMostLeaderSlots(slotTableBuilder));
                Integer slotId = targetSlots.get(0);
                String prevLeader = slotTableBuilder.getOrCreate(slotId).getLeader();
                logger.info("[balanceLeaderSlots] slot[{}] leader balance from [{}] to [{}]",
                    slotId, prevLeader, dataServer);
                slotTableBuilder.replaceLeader(slotId, dataServer);
                targetSlots.remove(slotId);
                result = true;
            }
        }
        if (result) {
            SlotTable slotTable = slotTableBuilder.build();
            logger.info("[balanceLeaderSlots][end] slot table leader stat: {}",
                SlotTableUtils.getSlotTableLeaderCount(slotTable));
            logger.info("[balanceLeaderSlots][end] slot table slots stat: {}",
                SlotTableUtils.getSlotTableSlotCount(slotTable));
        }
        return result;
    }

    private List<String> findDataServersNeedLeaderSlots(int averageLeaderSlots) {
        int threshold = balancePolicy.getLowWaterMarkSlotLeaderNums(averageLeaderSlots);
        List<DataNodeSlot> dataNodeSlots = slotTableBuilder.getDataNodeSlotsLeaderBelow(threshold);
        List<String> dataServers = new ArrayList<>(dataNodeSlots.size());
        dataNodeSlots.forEach(dataNodeSlot -> dataServers.add(dataNodeSlot.getDataNode()));
        dataServers.sort(Comparators.leastLeadersFirst(slotTableBuilder));
        logger.info("[findDataServersNeedLeaderSlots] avg: [{}], threshold: [{}], data-servers: {}", averageLeaderSlots,
                threshold, dataServers);
        return dataServers;
    }

    private boolean tryBalanceFollowerSlots() {
        boolean result = false;
        int avgSlotNum = slotManager.getSlotNums() * slotManager.getSlotReplicaNums()
                         / currentDataServers.size();
        List<String> targetDataServers = findTargetDataServers(avgSlotNum);
        logger.info("[balanceFollowerSlots] data-servers need more follower slots: {}",
            targetDataServers);

        for (String dataServer : targetDataServers) {
            DataNodeSlot dataNodeSlot = slotTableBuilder.getDataNodeSlot(dataServer);
            int maxMove = balancePolicy.getMaxMoveFollowerSlots(avgSlotNum,
                dataNodeSlot.totalSlotNum());
            if (maxMove < 1) {
                logger.warn(
                    "[findTargetFollowerSlots]no followers to move, avg: [{}], actual: [{}]",
                    avgSlotNum, dataNodeSlot.getFollowers().size());
                continue;
            }
            List<String> candidates = Filters.candidateDataServerFilter(this,
                new HashSet<>(targetDataServers), avgSlotNum).filter(currentDataServers);
            if (candidates.isEmpty()) {
                logger.warn(
                    "[findTargetFollowerSlots] not available slots to migrate for data-server[{}], "
                            + "leader-slots: [{}], follower-slots: [{}]", dataNodeSlot
                        .getDataNode(), dataNodeSlot.getLeaders().size(), dataNodeSlot
                        .getFollowers().size());
                continue;
            }
            while (maxMove > 0) {
                if (candidates.isEmpty()) {
                    break;
                }
                String candidate = Selectors.mostFollowerFirst(slotTableBuilder).select(candidates);
                List<Integer> candidateSlots = slotTableBuilder.getDataNodeSlot(candidate)
                    .getFollowers();
                candidateSlots = Filters.balanceFollowerFilter(this, targetDataServers).filter(
                    candidateSlots);
                for (Integer slotId : candidateSlots) {
                    SlotBuilder slotBuilder = slotTableBuilder.getOrCreate(slotId);
                    if (dataServer.equals(slotBuilder.getLeader())
                        || slotBuilder.getFollowers().contains(dataServer)) {
                        continue;
                    }
                    logger.info(
                        "[balanceFollowerSlots] slot[{}] remove follower [{}], add follower [{}]",
                        slotId, candidate, dataServer);
                    result = true;
                    slotTableBuilder.removeFollower(slotId, candidate);
                    slotTableBuilder.addFollower(slotId, dataServer);
                    maxMove--;
                    break;
                }
            }
        }
        if (result) {
            SlotTable slotTable = slotTableBuilder.build();
            logger.info("[balanceFollowerSlots][end] slot table leader stat: {}",
                SlotTableUtils.getSlotTableLeaderCount(slotTable));
            logger.info("[balanceFollowerSlots][end] slot table slots stat: {}",
                SlotTableUtils.getSlotTableSlotCount(slotTable));
        }
        return result;
    }

    private List<String> findTargetDataServers(int avg) {
        int threshold = balancePolicy.getLowWaterMarkSlotNums(avg);
        logger.info("[findTargetFollowerSlots] avg: [{}], threshold: [{}]", avg, threshold);
        List<DataNodeSlot> dataNodeSlots = slotTableBuilder.getDataNodeTotalSlotsBelow(threshold);
        List<String> targetDataServers = new ArrayList<>();
        dataNodeSlots.forEach(dataNodeSlot -> targetDataServers.add(dataNodeSlot.getDataNode()));
        return targetDataServers;
    }

    public SlotTable getPrevSlotTable() {
        return prevSlotTable;
    }

    public SlotTableBuilder getSlotTableBuilder() {
        return slotTableBuilder;
    }

}
