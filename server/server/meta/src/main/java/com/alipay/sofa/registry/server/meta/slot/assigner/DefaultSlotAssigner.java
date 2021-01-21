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
import com.alipay.sofa.registry.server.meta.slot.util.DataNodeComparator;
import com.alipay.sofa.registry.server.meta.slot.util.MigrateSlotGroup;
import com.alipay.sofa.registry.server.meta.slot.util.SlotBuilder;
import com.alipay.sofa.registry.server.meta.slot.util.SlotTableBuilder;
import com.alipay.sofa.registry.server.shared.util.NodeUtils;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author chen.zhu
 * <p>
 * Jan 15, 2021
 */
public class DefaultSlotAssigner implements SlotAssigner {

    private static final Logger      logger = LoggerFactory.getLogger(DefaultSlotAssigner.class);

    private final LocalSlotManager   slotManager;

    private final DataServerManager  dataServerManager;

    private final SlotTable          prevSlotTable;

    private final List<String>       currentDataServers;

    private final DataNodeComparator comparator;

    protected final SlotTableBuilder slotTableBuilder;

    protected MigrateSlotGroup       migrateSlotGroup;

    public DefaultSlotAssigner(LocalSlotManager slotManager, DataServerManager dataServerManager) {
        this.slotManager = slotManager;
        this.dataServerManager = dataServerManager;
        this.prevSlotTable = slotManager.getSlotTable();
        this.currentDataServers = NodeUtils.transferNodeToIpList(dataServerManager
            .getClusterMembers());
        this.comparator = new DataNodeComparator(prevSlotTable.getDataServers(), currentDataServers);
        this.slotTableBuilder = new SlotTableBuilder(slotManager.getSlotNums(),
            slotManager.getSlotReplicaNums());
    }

    @Override
    public SlotTable assign() {
        if (currentDataServers.isEmpty()) {
            logger.error("[no available data-servers] quit");
            throw new SofaRegistryRuntimeException(
                "no available data-servers for slot-table reassignment");
        }
        slotTableBuilder.init(prevSlotTable, currentDataServers);

        findUnassignedSlots();

        logger.info("[assign][assignLeaderSlots] begin");
        if (assignLeaderSlots()) {
            logger.info("[assign][after assignLeaderSlots] leader changed");
        }

        logger.info("[assign][assignLeaderSlots] begin");
        if (assignFollowerSlots()) {
            logger.info("[assign][after assignLeaderSlots] follower changed");
            slotTableBuilder.incrEpoch();
        }
        return slotTableBuilder.build();
    }

    private void findUnassignedSlots() {
        comparator.getRemoved().forEach(slotTableBuilder::removeDataServerSlots);
        migrateSlotGroup = slotTableBuilder.getNoAssignedSlots();
    }

    private boolean assignLeaderSlots() {
        List<Integer> leaders = migrateSlotGroup
            .getLeadersByScore(new LeaderEmergentScoreJury(this));
        boolean result = false;
        if (leaders.isEmpty()) {
            logger.info("[assignLeaderSlots] need assign leader slots set empty, quit");
            return false;
        }
        for (Integer slotId : leaders) {
            List<String> currentDataNodes = Lists.newArrayList(currentDataServers);
            SlotBuilder slotBuilder = slotTableBuilder.getOrCreate(slotId);
            Selector<String> selector = new DefaultSlotLeaderSelector(this, slotId);
            String dataNode = selector.select(currentDataNodes);
            logger.info("[assignLeaderSlots]assign slot[{}] leader as [{}]", slotId, dataNode);
            slotBuilder.setLeader(dataNode);
            // if remove follower is true, it means we just slip a follower to a leader
            // in this circumstances, we need to re-assign the follower role to a new datanode
            if (slotBuilder.removeFollower(dataNode)) {
                logger.info("[assignLeaderSlots] follower is added to be assign slot[{}]", slotId);
                result = true;
                migrateSlotGroup.addFollower(slotId);
            }
        }
        return result;
    }

    private boolean assignFollowerSlots() {
        List<FollowerToAssign> followerToAssigns = migrateSlotGroup
            .getFollowersByScore(new FollowerEmergentScoreJury());
        boolean result = false;
        if (followerToAssigns.isEmpty()) {
            logger.info("[assignFollowerSlots] need assign follower slots set empty, quit");
            return false;
        }
        for (FollowerToAssign followerToAssign : followerToAssigns) {
            List<String> currentDataNodes = Lists.newArrayList(currentDataServers);
            SlotBuilder slotBuilder = slotTableBuilder.getOrCreate(followerToAssign.getSlotId());
            currentDataNodes.removeAll(slotBuilder.getFollowers());
            currentDataNodes.remove(slotBuilder.getLeader());
            if (currentDataNodes.isEmpty()) {
                logger
                    .info(
                        "[assignFollowerSlots] empty candidate set for slot[{}] followers assign, quit",
                        followerToAssign.getSlotId());
                continue;
            }
            Selector<String> selector = new DefaultSlotFollowerSelector(this);
            for (int i = 0; i < followerToAssign.getAssigneeNums(); i++) {
                String dataNode = selector.select(currentDataNodes);
                logger.info("[assignFollowerSlots]assign slot[{}] add follower as [{}]",
                    followerToAssign.getSlotId(), dataNode);
                result = true;
                slotBuilder.addFollower(dataNode);
                currentDataNodes.remove(dataNode);
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

    public DataServerManager getDataServerManager() {
        return dataServerManager;
    }

    public SlotTable getPrevSlotTable() {
        return prevSlotTable;
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
}
