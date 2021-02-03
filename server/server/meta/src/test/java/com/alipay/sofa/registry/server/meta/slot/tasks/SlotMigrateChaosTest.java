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
import com.alipay.sofa.registry.common.model.slot.SlotConfig;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.server.meta.AbstractTest;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.lease.data.DefaultDataServerManager;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.alipay.sofa.registry.server.meta.slot.arrange.ScheduledSlotArranger;
import com.alipay.sofa.registry.server.meta.slot.assigner.DefaultSlotAssigner;
import com.alipay.sofa.registry.server.meta.slot.manager.DefaultSlotManager;
import com.alipay.sofa.registry.server.meta.slot.manager.LocalSlotManager;
import com.alipay.sofa.registry.server.shared.slot.SlotTableUtils;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author chen.zhu
 * <p>
 * Jan 21, 2021
 */
public class SlotMigrateChaosTest extends AbstractTest {

    protected DefaultSlotManager       defaultSlotManager;

    protected SlotManager              raftSlotManager;

    protected LocalSlotManager         localSlotManager;

    @Mock
    protected DefaultDataServerManager dataServerManager;

    @BeforeClass
    public static void beforeSlotMigrateChaosTestClass() {
        int slotNums = 16;
        do {
            slotNums = random.nextInt(1024);
        } while (slotNums < 32);
        System.setProperty("data.slot.num", slotNums + "");
        System.setProperty("data.slot.replicas", "2");
        System.setProperty("slot.frozen.milli", "1");
        System.setProperty("slot.leader.max.move", "2");
    }

    @Before
    public void beforeSlotMigrateChaosTest() throws Exception {
        MockitoAnnotations.initMocks(this);
        List<DataNode> dataNodes = Lists.newArrayList(new DataNode(randomURL(randomIp()), getDc()),
            new DataNode(randomURL(randomIp()), getDc()), new DataNode(randomURL(randomIp()),
                getDc()), new DataNode(randomURL(randomIp()), getDc()));
        when(dataServerManager.getClusterMembers()).thenReturn(dataNodes);
        NodeConfig nodeConfig = mock(NodeConfig.class);
        when(nodeConfig.getLocalDataCenter()).thenReturn(getDc());
        raftSlotManager = localSlotManager = new LocalSlotManager(nodeConfig);
        defaultSlotManager = new DefaultSlotManager(localSlotManager, raftSlotManager);
    }

    @Test
    public void testChaosAllocateSlots() throws Exception {
        int dataNodeNum = random.nextInt(1024) % 10 + 10;
        logger.info("[data-node] size: {}", dataNodeNum);
        logger.info("[slot] size: {}", localSlotManager.getSlotNums());
        List<DataNode> dataNodes = randomDataNodes(dataNodeNum);
        SlotTable slotTable = randomUnBalancedSlotTable(dataNodes);
        localSlotManager.refresh(slotTable);
        logger.info("[leader stats] {}", SlotTableUtils.getSlotTableLeaderCount(slotTable));
        logger.info("[slot stats] {}", SlotTableUtils.getSlotTableSlotCount(slotTable));

        when(dataServerManager.getClusterMembers()).thenReturn(dataNodes);
        ScheduledSlotArranger arranger = new ScheduledSlotArranger(dataServerManager,
            localSlotManager, defaultSlotManager);
        makeRaftLeader();

        SlotTable prev = slotTable;
        arranger.getTask().runUnthrowable();
        SlotTable current = localSlotManager.getSlotTable();
        int count = 1;
        while (prev.getEpoch() < current.getEpoch()) {
            Assert.assertTrue(isMoreBalanced(prev, current, dataNodes));
            prev = localSlotManager.getSlotTable();
            arranger.getTask().runUnthrowable();
            current = localSlotManager.getSlotTable();
            count++;
        }
        logger.info("[count] {}", count);
        Assert.assertTrue(isSlotTableBalanced(current, dataNodes));
        Assert.assertTrue(isSlotTableLeaderBalanced(current, dataNodes));
    }
}
