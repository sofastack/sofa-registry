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
package com.alipay.sofa.registry.server.meta.monitor.impl;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.DataNodeSlot;
import com.alipay.sofa.registry.common.model.slot.SlotConfig;
import com.alipay.sofa.registry.common.model.slot.SlotStatus;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.exception.InitializeException;
import com.alipay.sofa.registry.server.meta.AbstractTest;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.slot.manager.LocalSlotManager;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.mockito.Mockito.*;

public class DefaultSlotTableStatsTest extends AbstractTest {

    private LocalSlotManager      slotManager;

    private List<DataNode>        dataNodes;

    private DefaultSlotTableStats slotTableStats;

    private NodeConfig nodeConfig;

    @Before
    public void beforeDefaultSlotTableMonitorTest() throws Exception {
        nodeConfig = mock(NodeConfig.class);
        when(nodeConfig.getLocalDataCenter()).thenReturn(getDc());
        slotManager = new LocalSlotManager(nodeConfig);
        slotTableStats = new DefaultSlotTableStats(slotManager);
        dataNodes = Lists.newArrayList(new DataNode(randomURL(randomIp()), getDc()), new DataNode(
            randomURL(randomIp()), getDc()), new DataNode(randomURL(randomIp()), getDc()));
        slotManager.refresh(new SlotTableGenerator(dataNodes).createSlotTable());
        slotManager.postConstruct();
        slotTableStats.initialize();
    }

    @Test
    public void testIsSlotTableStable() {
        Assert.assertFalse(slotTableStats.isSlotTableStable());
        SlotTable slotTable = slotManager.getSlotTable();
        for(DataNode dataNode : dataNodes) {
            List<SlotStatus> slotStatuses = Lists.newArrayList();
            DataNodeSlot dataNodeSlot = slotTable.transfer(dataNode.getIp(), false).get(0);
            dataNodeSlot.getLeaders().forEach(slotId -> {
                slotStatuses.add(new SlotStatus(slotId, slotTable.getSlot(slotId).getLeaderEpoch(), SlotStatus.LeaderStatus.HEALTHY));
            });
            slotTableStats.checkSlotStatuses(slotStatuses);
        }
        Assert.assertTrue(slotTableStats.isSlotTableStable());
    }

    @Test
    public void testCheckSlotStatuses() {
        Assert.assertFalse(slotTableStats.isSlotTableStable());
        SlotTable slotTable = slotManager.getSlotTable();
        for(DataNode dataNode : dataNodes) {
            List<SlotStatus> slotStatuses = Lists.newArrayList();
            DataNodeSlot dataNodeSlot = slotTable.transfer(dataNode.getIp(), false).get(0);
            dataNodeSlot.getLeaders().forEach(slotId -> {
                slotStatuses.add(new SlotStatus(slotId, slotTable.getSlot(slotId).getLeaderEpoch() - 1, SlotStatus.LeaderStatus.HEALTHY));
            });
            slotTableStats.checkSlotStatuses(slotStatuses);
        }
        Assert.assertFalse(slotTableStats.isSlotTableStable());
    }

    @Test
    public void testUpdateSlotTable() {
        Assert.assertFalse(slotTableStats.isSlotTableStable());
        SlotTable slotTable = slotManager.getSlotTable();
        for(DataNode dataNode : dataNodes) {
            List<SlotStatus> slotStatuses = Lists.newArrayList();
            DataNodeSlot dataNodeSlot = slotTable.transfer(dataNode.getIp(), false).get(0);
            dataNodeSlot.getLeaders().forEach(slotId -> {
                slotStatuses.add(new SlotStatus(slotId, slotTable.getSlot(slotId).getLeaderEpoch(), SlotStatus.LeaderStatus.HEALTHY));
            });
            slotTableStats.checkSlotStatuses(slotStatuses);
        }
        Assert.assertTrue(slotTableStats.isSlotTableStable());

        slotManager.refresh(new SlotTableGenerator(dataNodes).createSlotTable());
        slotTableStats.updateSlotTable(slotManager.getSlotTable());
        Assert.assertFalse(slotTableStats.isSlotTableStable());
    }

    @Test
    public void testDataReportWhenInit() throws InitializeException {
        slotManager = new LocalSlotManager(nodeConfig);
        slotTableStats = new DefaultSlotTableStats(slotManager);
        slotTableStats.initialize();
        List<SlotStatus> slotStatuses = Lists.newArrayList();
        for(int slotId = 0; slotId < SlotConfig.SLOT_NUM; slotId++) {
            slotStatuses.add(new SlotStatus(slotId,
                    0,
                    SlotStatus.LeaderStatus.UNHEALTHY));
        }
        slotTableStats.checkSlotStatuses(slotStatuses);
    }
}