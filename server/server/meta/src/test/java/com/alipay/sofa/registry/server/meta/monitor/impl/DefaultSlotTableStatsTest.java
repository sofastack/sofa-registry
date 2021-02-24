package com.alipay.sofa.registry.server.meta.monitor.impl;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.DataNodeSlot;
import com.alipay.sofa.registry.common.model.slot.SlotStatus;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
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

    private LocalSlotManager slotManager;

    private List<DataNode> dataNodes;

    private DefaultSlotTableStats slotTableStats;

    @Before
    public void beforeDefaultSlotTableMonitorTest() throws Exception {
        NodeConfig nodeConfig = mock(NodeConfig.class);
        when(nodeConfig.getLocalDataCenter()).thenReturn(getDc());
        slotManager = new LocalSlotManager(nodeConfig);
        slotTableStats = new DefaultSlotTableStats(slotManager);
        dataNodes = Lists.newArrayList(new DataNode(randomURL(randomIp()), getDc()),
                new DataNode(randomURL(randomIp()), getDc()), new DataNode(randomURL(randomIp()),
                        getDc()));
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
}