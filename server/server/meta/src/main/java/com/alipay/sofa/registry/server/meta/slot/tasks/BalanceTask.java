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
package com.alipay.sofa.registry.server.meta.slot.tasks;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.lease.data.DefaultDataServerManager;
import com.alipay.sofa.registry.server.meta.slot.RebalanceTask;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.alipay.sofa.registry.server.meta.slot.manager.LocalSlotManager;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author chen.zhu
 * <p>
 * Dec 01, 2020
 */
public class BalanceTask implements RebalanceTask {

    private static final Logger            logger            = LoggerFactory
                                                                 .getLogger(BalanceTask.class);

    private final LocalSlotManager         localSlotManager;

    private final SlotManager              raftSlotManager;

    private final DefaultDataServerManager dataServerManager;

    private long                           nextEpoch;

    private final AtomicInteger            nextLeaderIndex   = new AtomicInteger();

    private final AtomicInteger            nextFollowerIndex = new AtomicInteger(1);

    private List<DataNode>                 dataNodes;

    public BalanceTask(LocalSlotManager localSlotManager, SlotManager raftSlotManager,
                       DefaultDataServerManager dataServerManager) {
        this.localSlotManager = localSlotManager;
        this.raftSlotManager = raftSlotManager;
        this.dataServerManager = dataServerManager;
    }

    @Override
    public void run() {
        initParameters();
        if (dataNodes == null || dataNodes.isEmpty()) {
            return;
        }
        if (logger.isInfoEnabled()) {
            logger.info("[run] candidates({}): {}", dataNodes.size(), dataNodes);
        }

        SlotTable slotTable = createSlotTable();
        if (logger.isInfoEnabled()) {
            logger.info("[run] end to init slot table");
        }

        raftSlotManager.refresh(slotTable);
        if (logger.isInfoEnabled()) {
            logger.info("[run] raft refreshed slot-table");
        }
    }

    private void initParameters() {
        dataNodes = dataServerManager.getClusterMembers();
        if (dataNodes.isEmpty()) {
            if (logger.isInfoEnabled()) {
                logger.info("[run] empty candidate, quit");
            }
            return;
        }
        nextLeaderIndex.set(0);
        nextFollowerIndex.set(dataNodes.size() - 1);
        nextEpoch = DatumVersionUtil.nextId();
    }

    public SlotTable createSlotTable() {
        Map<Integer, Slot> slotMap = generateSlotMap();
        return new SlotTable(nextEpoch, slotMap.values());
    }

    private Map<Integer, Slot> generateSlotMap() {
        Map<Integer, Slot> slotMap = Maps.newHashMap();
        for (int i = 0; i < localSlotManager.getSlotNums(); i++) {
            long epoch = DatumVersionUtil.nextId();
            String leader = getNextLeader().getIp();
            List<String> followers = Lists.newArrayList();
            for (int j = 0; j < localSlotManager.getSlotReplicaNums() - 1; j++) {
                followers.add(getNextFollower().getIp());
            }
            Slot slot = new Slot(i, leader, epoch, followers);
            slotMap.put(i, slot);
        }
        return slotMap;
    }

    private DataNode getNextLeader() {
        return dataNodes.get(nextLeaderIndex.getAndIncrement() % dataNodes.size());
    }

    private DataNode getNextFollower() {
        return dataNodes.get(nextFollowerIndex.getAndIncrement() % dataNodes.size());
    }
}
