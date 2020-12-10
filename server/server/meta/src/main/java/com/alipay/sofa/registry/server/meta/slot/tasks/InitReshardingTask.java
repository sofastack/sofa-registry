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
import com.alipay.sofa.registry.jraft.bootstrap.ServiceStateMachine;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.lease.impl.DefaultDataServerManager;
import com.alipay.sofa.registry.server.meta.slot.RebalanceTask;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.alipay.sofa.registry.server.meta.slot.impl.LocalSlotManager;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.glassfish.jersey.internal.guava.Sets;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * @author chen.zhu
 * <p>
 * Dec 01, 2020
 */
public class InitReshardingTask implements RebalanceTask {

    private static final Logger            logger = LoggerFactory
                                                      .getLogger(InitReshardingTask.class);

    private final LocalSlotManager         localSlotManager;

    private final SlotManager              raftSlotManager;

    private final DefaultDataServerManager dataServerManager;

    private final Random                   random = new Random();

    private long                           nextEpoch;

    public InitReshardingTask(LocalSlotManager localSlotManager, SlotManager raftSlotManager,
                              DefaultDataServerManager dataServerManager) {
        this.localSlotManager = localSlotManager;
        this.raftSlotManager = raftSlotManager;
        this.dataServerManager = dataServerManager;
    }

    @Override
    public void run() {
        if(!ServiceStateMachine.getInstance().isLeader()) {
            if(logger.isInfoEnabled()) {
                logger.info("[run] not leader now, quit");
            }
            return;
        } else {
            if(logger.isInfoEnabled()) {
                logger.info("[run] start to init slot table");
            }
        }
        List<DataNode> dataNodes = dataServerManager.getLocalClusterMembers();
        if(dataNodes.isEmpty()) {
            if(logger.isInfoEnabled()) {
                logger.info("[run] empty candidate, quit");
            }
            return;
        }
        List<String> candidates = Lists.newArrayListWithCapacity(dataNodes.size());
        if(logger.isInfoEnabled()) {
            logger.info("[run] candidates({}): {}", dataNodes.size(), dataNodes);
        }
        dataNodes.forEach(dataNode -> candidates.add(dataNode.getIp()));
        Map<Integer, Slot> slotMap = Maps.newHashMap();
        for(int slotId = 0; slotId < localSlotManager.getSlotNums(); slotId++) {
            Set<String> selected = Sets.newHashSet();
            nextEpoch = DatumVersionUtil.nextId();
            String leader = randomSelect(candidates, selected);
            List<String> followers = Lists.newArrayListWithCapacity(localSlotManager.getSlotReplicaNums());
            for(int replica = 0; replica < localSlotManager.getSlotReplicaNums() - 1; replica++) {
                followers.add(randomSelect(candidates, selected));
            }
            slotMap.put(slotId, new Slot(slotId, leader, nextEpoch, followers));
        }
        if(!ServiceStateMachine.getInstance().isLeader()) {
            if(logger.isWarnEnabled()) {
                logger.warn("[run] not leader, won't update slot table");
            }
            return;
        }
        if(logger.isInfoEnabled()) {
            logger.info("[run] end to init slot table");
        }
        raftSlotManager.refresh(new SlotTable(nextEpoch, slotMap));
    }

    private String randomSelect(List<String> candidates, Set<String> selected) {
        int randomIndex = Math.abs(random.nextInt()) % candidates.size();
        String result = candidates.get(randomIndex);
        while (selected.contains(result)) {
            randomIndex = Math.abs(random.nextInt()) % candidates.size();
            result = candidates.get(randomIndex);
        }
        return result;
    }
}
