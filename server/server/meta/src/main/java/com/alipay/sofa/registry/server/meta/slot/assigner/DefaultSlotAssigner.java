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

import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.exception.SofaRegistryRuntimeException;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.lease.data.DataServerManager;
import com.alipay.sofa.registry.server.meta.slot.SlotAssigner;
import com.alipay.sofa.registry.server.meta.slot.manager.LocalSlotManager;
import com.alipay.sofa.registry.server.meta.slot.util.MigrateSlotGroup;
import com.alipay.sofa.registry.server.meta.slot.util.builder.SlotBuilder;
import com.alipay.sofa.registry.server.meta.slot.util.builder.SlotTableBuilder;
import com.alipay.sofa.registry.server.meta.slot.util.comparator.DataNodeComparator;
import com.alipay.sofa.registry.server.meta.slot.util.comparator.SortType;
import com.alipay.sofa.registry.server.meta.slot.util.selector.Selector;
import com.alipay.sofa.registry.server.meta.slot.util.selector.Selectors;
import com.alipay.sofa.registry.server.shared.util.NodeUtils;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * @author chen.zhu
 * <p>
 * Jan 15, 2021
 */
public class DefaultSlotAssigner implements SlotAssigner {

    private static final Logger      logger = LoggerFactory.getLogger(DefaultSlotAssigner.class);

    private final LocalSlotManager   slotManager;

    private final SlotTable          prevSlotTable;

    private final List<String>       currentDataServers;

    private final DataNodeComparator comparator;

    protected final SlotTableBuilder slotTableBuilder;

    protected MigrateSlotGroup       migrateSlotGroup;

    public DefaultSlotAssigner(LocalSlotManager slotManager, DataServerManager dataServerManager) {
        this.slotManager = slotManager;
        this.prevSlotTable = slotManager.getSlotTable();
        this.currentDataServers = NodeUtils.transferNodeToIpList(dataServerManager
            .getClusterMembers());
        this.comparator = new DataNodeComparator(prevSlotTable.getDataServers(), currentDataServers);
        this.slotTableBuilder = new SlotTableBuilder(slotManager.getSlotNums(),
            slotManager.getSlotReplicaNums());
    }

    @Override
    public SlotTable assign() {

        findUnassignedSlots();

        logger.info("[assign][assignLeaderSlots] begin");
        if (tryAssignLeaderSlots()) {
            logger.info("[assign][after assignLeaderSlots] end -- leader changed");
            slotTableBuilder.incrEpoch();
        } else {
            logger.info("[assign][after assignLeaderSlots] end -- no changes");
        }

        logger.info("[assign][assignLeaderSlots] begin");
        if (tryAssignFollowerSlots()) {
            logger.info("[assign][after assignLeaderSlots] end -- follower changed");
            slotTableBuilder.incrEpoch();
        } else {
            logger.info("[assign][assignLeaderSlots] end -- no changes");
        }
        return slotTableBuilder.build();
    }

    private void findUnassignedSlots() {
        if (currentDataServers.isEmpty()) {
            logger.error("[no available data-servers] quit");
            throw new SofaRegistryRuntimeException(
                    "no available data-servers for slot-table reassignment");
        }
        slotTableBuilder.init(prevSlotTable, currentDataServers);
        comparator.getRemoved().forEach(slotTableBuilder::removeDataServerSlots);
        migrateSlotGroup = slotTableBuilder.getNoAssignedSlots();
    }

    private boolean tryAssignLeaderSlots() {
        /** our strategy(assign leader) is to swap follower to leader when follower is enabled
         * if no followers, we select a new data-server to assign, that's simple and low prioritized
         * so, leaders are resolved in an order that who has least followers first
         * (as we wish to satisfy these nodes first)
         * leaders with no follower is lowest priority, as whatever we did, it will pick up a candidate
         * that is not its follower
        */
        List<Integer> leaders = migrateSlotGroup.getLeadersByScore(new FewerFellowerFirstStrategy(
            slotTableBuilder));
        boolean result = false;
        if (leaders.isEmpty()) {
            logger.info("[assignLeaderSlots] no slot leader needs assign, quit");
            return false;
        }
        for (Integer slotId : leaders) {
            List<String> currentDataNodes = Lists.newArrayList(currentDataServers);
            String nextLeader = Selectors.slotLeaderSelector(slotTableBuilder, slotId).select(
                currentDataNodes);
            logger.info("[assignLeaderSlots]assign slot[{}] leader as [{}]", slotId, nextLeader);
            boolean nextLeaderWasFollower = isNextLeaderFollowerOfSlot(slotId, nextLeader);
            slotTableBuilder.replaceLeader(slotId, nextLeader);
            if (nextLeaderWasFollower) {
                migrateSlotGroup.addFollower(slotId);
            }
            result = true;
        }
        return result;
    }

    private boolean isNextLeaderFollowerOfSlot(int slotId, String nextLeader) {
        return slotTableBuilder.getOrCreate(slotId).getFollowers().contains(nextLeader);
    }

    private boolean tryAssignFollowerSlots() {
        List<MigrateSlotGroup.FollowerToAssign> followerToAssigns = migrateSlotGroup
            .getFollowersByScore(new FollowerEmergentScoreJury());
        boolean result = false;
        if (followerToAssigns.isEmpty()) {
            logger.info("[assignFollowerSlots] need assign follower slots set empty, quit");
            return false;
        }
        for (MigrateSlotGroup.FollowerToAssign followerToAssign : followerToAssigns) {
            List<String> candidates = Lists.newArrayList(currentDataServers);
            SlotBuilder slotBuilder = slotTableBuilder.getOrCreate(followerToAssign.getSlotId());
            candidates.removeAll(slotBuilder.getFollowers());
            candidates.remove(slotBuilder.getLeader());
            if (candidates.isEmpty()) {
                logger
                    .info(
                        "[assignFollowerSlots] empty candidate set for slot[{}] followers assign, quit",
                        followerToAssign.getSlotId());
                continue;
            }
            Selector<String> selector = Selectors.leastSlotsFirst(slotTableBuilder);
            for (int i = 0; i < followerToAssign.getAssigneeNums(); i++) {
                String candidate = selector.select(candidates);
                if (StringUtils.isEmpty(candidate)) {
                    logger.warn("[tryAssignFollowerSlots] candidate is null");
                    continue;
                }
                if (candidate.equals(slotBuilder.getLeader())
                    || slotBuilder.getFollowers().contains(candidate)) {
                    logger.error(
                        "[tryAssignFollowerSlots] candidate follower data [{}] for slot[{}] equals "
                                + "with leader or already a follower", candidate,
                        followerToAssign.getSlotId());
                    continue;
                }
                logger.info("[assignFollowerSlots]assign slot[{}] add follower as [{}]",
                    followerToAssign.getSlotId(), candidate);
                result = true;
                slotBuilder.addFollower(candidate);
                candidates.remove(candidate);
            }
        }
        return result;
    }

    /**
     * ==================================  Getters ====================================== *
     * */
    public LocalSlotManager getSlotManager() {
        return slotManager;
    }

    public List<String> getCurrentDataServers() {
        return currentDataServers;
    }

    public SlotTableBuilder getSlotTableBuilder() {
        return slotTableBuilder;
    }

    public MigrateSlotGroup getMigrateSlotGroup() {
        return migrateSlotGroup;
    }

    /**
     * ==================================  Classes ====================================== *
     * */

    public static class FewerFellowerFirstStrategy implements ScoreStrategy<Integer> {

        private SlotTableBuilder slotTableBuilder;

        public FewerFellowerFirstStrategy(SlotTableBuilder slotTableBuilder) {
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

    public static class FollowerEmergentScoreJury implements
                                                 ScoreStrategy<MigrateSlotGroup.FollowerToAssign> {

        @Override
        public int score(MigrateSlotGroup.FollowerToAssign followerToAssign) {
            return SortType.DES.getScore(followerToAssign.getAssigneeNums());
        }
    }
}
