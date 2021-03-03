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
import com.alipay.sofa.registry.common.model.slot.*;
import com.alipay.sofa.registry.exception.InitializeException;
import com.alipay.sofa.registry.server.meta.AbstractTest;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.slot.manager.LocalSlotManager;
import com.alipay.sofa.registry.server.shared.util.NodeUtils;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.alipay.sofa.registry.server.meta.monitor.impl.DefaultSlotStats.MAX_SYNC_GAP;
import static org.mockito.Mockito.*;

public class DefaultSlotTableStatsTest extends AbstractTest {

    private LocalSlotManager      slotManager;

    private List<DataNode>        dataNodes;

    private DefaultSlotTableStats slotTableStats;

    private NodeConfig            nodeConfig;

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
        Assert.assertFalse(slotTableStats.isSlotLeadersStable());
        SlotTable slotTable = slotManager.getSlotTable();
        for(DataNode dataNode : dataNodes) {
            List<BaseSlotStatus> slotStatuses = Lists.newArrayList();
            DataNodeSlot dataNodeSlot = slotTable.transfer(dataNode.getIp(), false).get(0);
            dataNodeSlot.getLeaders().forEach(slotId -> {
                slotStatuses.add(new LeaderSlotStatus(slotId, slotTable.getSlot(slotId).getLeaderEpoch(),
                        dataNode.getIp(), BaseSlotStatus.LeaderStatus.HEALTHY));
            });
            slotTableStats.checkSlotStatuses(dataNode, slotStatuses);
        }
        Assert.assertTrue(slotTableStats.isSlotLeadersStable());
    }

    @Test
    public void testCheckSlotStatuses() {
        Assert.assertFalse(slotTableStats.isSlotLeadersStable());
        SlotTable slotTable = slotManager.getSlotTable();
        for(DataNode dataNode : dataNodes) {
            List<BaseSlotStatus> slotStatuses = Lists.newArrayList();
            DataNodeSlot dataNodeSlot = slotTable.transfer(dataNode.getIp(), false).get(0);
            dataNodeSlot.getLeaders().forEach(slotId -> {
                slotStatuses.add(new LeaderSlotStatus(slotId, slotTable.getSlot(slotId).getLeaderEpoch() - 1,
                        dataNode.getIp(), BaseSlotStatus.LeaderStatus.HEALTHY));
            });
            slotTableStats.checkSlotStatuses(dataNode, slotStatuses);
        }
        Assert.assertFalse(slotTableStats.isSlotLeadersStable());
    }

    @Test
    public void testUpdateSlotTable() {
        Assert.assertFalse(slotTableStats.isSlotLeadersStable());
        SlotTable slotTable = slotManager.getSlotTable();
        for(DataNode dataNode : dataNodes) {
            List<BaseSlotStatus> slotStatuses = Lists.newArrayList();
            DataNodeSlot dataNodeSlot = slotTable.transfer(dataNode.getIp(), false).get(0);
            dataNodeSlot.getLeaders().forEach(slotId -> {
                slotStatuses.add(new LeaderSlotStatus(slotId, slotTable.getSlot(slotId).getLeaderEpoch(),
                        dataNode.getIp(), BaseSlotStatus.LeaderStatus.HEALTHY));
            });
            slotTableStats.checkSlotStatuses(dataNode, slotStatuses);
        }
        Assert.assertTrue(slotTableStats.isSlotLeadersStable());

        slotManager.refresh(new SlotTableGenerator(dataNodes).createSlotTable());
        slotTableStats.updateSlotTable(slotManager.getSlotTable());
        Assert.assertFalse(slotTableStats.isSlotLeadersStable());
    }

    @Test
    public void testDataReportHeartbeatWhenInit() throws InitializeException {
        slotManager = new LocalSlotManager(nodeConfig);
        slotTableStats = new DefaultSlotTableStats(slotManager);
        slotTableStats.initialize();
        List<BaseSlotStatus> slotStatuses = Lists.newArrayList();
        for (int slotId = 0; slotId < SlotConfig.SLOT_NUM; slotId++) {
            slotStatuses.add(new LeaderSlotStatus(slotId, 0, randomIp(),
                BaseSlotStatus.LeaderStatus.UNHEALTHY));
        }
        slotTableStats
            .checkSlotStatuses(new DataNode(randomURL(randomIp()), getDc()), slotStatuses);
    }

    @Test
    public void testFollowerStatus() {
        Assert.assertFalse(slotTableStats.isSlotFollowersStable());
        DataNode dataNode = new DataNode(randomURL(randomIp()), getDc());
        List<BaseSlotStatus> slotStatuses = Lists.newArrayList();
        for (int slotId = 0; slotId < SlotConfig.SLOT_NUM; slotId++) {
            slotStatuses.add(new FollowerSlotStatus(slotId, System.currentTimeMillis(), dataNode
                .getIp(), System.currentTimeMillis(), -1));
        }
        slotTableStats.checkSlotStatuses(dataNode, slotStatuses);
        Assert.assertFalse(slotTableStats.isSlotFollowersStable());

        slotStatuses = Lists.newArrayList();
        for (int slotId = 0; slotId < SlotConfig.SLOT_NUM; slotId++) {
            slotStatuses.add(new FollowerSlotStatus(slotId, System.currentTimeMillis(), dataNode
                .getIp(), System.currentTimeMillis(), System.currentTimeMillis() - MAX_SYNC_GAP));
        }
        slotTableStats.checkSlotStatuses(dataNode, slotStatuses);
        Assert.assertFalse(slotTableStats.isSlotFollowersStable());

        for (DataNode node : dataNodes) {
            slotStatuses = Lists.newArrayList();
            for (int slotId = 0; slotId < SlotConfig.SLOT_NUM; slotId++) {
                slotStatuses.add(new FollowerSlotStatus(slotId, System.currentTimeMillis(), node
                    .getIp(), System.currentTimeMillis(), System.currentTimeMillis() - 1000));
            }
            slotTableStats.checkSlotStatuses(node, slotStatuses);
        }
        Assert.assertTrue(slotTableStats.isSlotFollowersStable());
    }

    @Test
    public void testUpdateSlotTableWhenFollowerChangeOnly() {
        Assert.assertFalse(slotTableStats.isSlotFollowersStable());
        List<BaseSlotStatus> slotStatuses = Lists.newArrayList();
        for (DataNode node : dataNodes) {
            slotStatuses = Lists.newArrayList();
            for (int slotId = 0; slotId < SlotConfig.SLOT_NUM; slotId++) {
                slotStatuses.add(new FollowerSlotStatus(slotId, System.currentTimeMillis(), node
                        .getIp(), System.currentTimeMillis(), System.currentTimeMillis() - 1000));
            }
            slotTableStats.checkSlotStatuses(node, slotStatuses);
        }
        Assert.assertTrue(slotTableStats.isSlotFollowersStable());

        SlotTable prev = slotManager.getSlotTable();
        Map<Integer, Slot> slotMap = prev.getSlotMap();
        Slot prevSlot = slotMap.get(1);
        Set<String> followers = Sets.newHashSet(prevSlot.getFollowers());
        List<String> newFollowers = NodeUtils.transferNodeToIpList(dataNodes);
        newFollowers.removeAll(followers);
        slotMap.put(1, new Slot(1, prevSlot.getLeader(), prevSlot.getLeaderEpoch(), newFollowers));
        SlotTable slotTable = new SlotTable(prev.getEpoch() + 1, slotMap.values());
        slotManager.refresh(slotTable);
        slotTableStats.updateSlotTable(slotTable);
        Assert.assertFalse(slotTableStats.isSlotFollowersStable());

    }
}