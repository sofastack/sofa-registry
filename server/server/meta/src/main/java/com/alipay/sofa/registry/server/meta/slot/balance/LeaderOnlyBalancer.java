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
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.slot.SlotBalancer;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.alipay.sofa.registry.server.meta.slot.util.builder.SlotTableBuilder;
import com.alipay.sofa.registry.server.meta.slot.util.selector.Selectors;
import com.alipay.sofa.registry.server.shared.slot.SlotTableUtils;
import com.alipay.sofa.registry.server.shared.util.NodeUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author chen.zhu
 * <p>
 * Jan 21, 2021
 */
public class LeaderOnlyBalancer implements SlotBalancer {

    private static final Logger    logger        = LoggerFactory
                                                     .getLogger(LeaderOnlyBalancer.class);

    private final SlotTableBuilder slotTableBuilder;

    private final List<String>     currentDataServers;

    private final SlotManager      slotManager;

    private final BalancePolicy    balancePolicy = new NaiveBalancePolicy();

    public LeaderOnlyBalancer(SlotTableBuilder slotTableBuilder, List<String> currentDataServers,
                              SlotManager slotManager) {
        this.slotTableBuilder = slotTableBuilder;
        this.currentDataServers = currentDataServers;
        this.slotManager = slotManager;
    }

    @Override
    public SlotTable balance() {
        int avg = slotManager.getSlotNums() / currentDataServers.size();
        Set<String> targetDataServers = findDataServersNeedLeaderSlots(avg);
        if (targetDataServers.isEmpty()) {
            logger.warn("[balance]no data-servers needs leader balance");
            return slotManager.getSlotTable();
        } else {
            logger.info("[balance]try balance(add) leader slots to {}", targetDataServers);
            SlotTable prevSlotTable = slotManager.getSlotTable();
            logger.info("[balance][begin] slot table leader stat: {}",
                SlotTableUtils.getSlotTableLeaderCount(prevSlotTable));
            logger.info("[balance][begin] slot table slots stat: {}",
                SlotTableUtils.getSlotTableSlotCount(prevSlotTable));
        }
        for (String target : targetDataServers) {
            DataNodeSlot dataNodeSlot = slotTableBuilder.getDataNodeSlot(target);
            int maxMove = balancePolicy
                .getMaxMoveLeaderSlots(avg, dataNodeSlot.getLeaders().size());
            List<DataNodeSlot> candidateDataNodeSlots = slotTableBuilder
                .getDataNodeSlotsLeaderBeyond(avg);
            if (candidateDataNodeSlots.isEmpty()) {
                logger
                    .warn("[balance] no candidates available(no data-server's leader is above avg)");
                continue;
            }
            List<String> candidates = NodeUtils
                .transferDataNodeSlotToIpList(candidateDataNodeSlots);
            String candidate = Selectors.mostLeaderFirst(slotTableBuilder).select(candidates);
            DataNodeSlot candidateDataNodeSlot = slotTableBuilder.getDataNodeSlot(candidate);
            maxMove = Math.min(maxMove, candidateDataNodeSlot.getLeaders().size() - avg);
            while (maxMove-- > 0) {
                Integer slotId = candidateDataNodeSlot.getLeaders().get(maxMove);
                slotTableBuilder.replaceLeader(slotId, target);
                logger.info("[balance] slot[{}] leader change from [{}] to [{}]", slotId,
                    candidate, target);
            }
        }
        SlotTable slotTable = slotTableBuilder.build();
        logger.info("[balance][end] slot table leader stat: {}",
            SlotTableUtils.getSlotTableLeaderCount(slotTable));
        logger.info("[balance][end] slot table slots stat: {}",
            SlotTableUtils.getSlotTableSlotCount(slotTable));
        return slotTable;
    }

    protected Set<String> findDataServersNeedLeaderSlots(int avg) {
        int threshold = balancePolicy.getLowWaterMarkSlotLeaderNums(avg);
        List<DataNodeSlot> dataNodeSlots = slotTableBuilder.getDataNodeSlotsLeaderBelow(threshold);
        Set<String> dataServers = new HashSet<>(dataNodeSlots.size());
        dataNodeSlots.forEach(dataNodeSlot -> dataServers.add(dataNodeSlot.getDataNode()));
        return dataServers;
    }
}
