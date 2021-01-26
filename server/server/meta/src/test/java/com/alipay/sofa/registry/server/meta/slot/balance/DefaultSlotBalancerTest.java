package com.alipay.sofa.registry.server.meta.slot.balance;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.server.meta.AbstractTest;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.lease.data.DataServerManager;
import com.alipay.sofa.registry.server.meta.slot.manager.LocalSlotManager;
import com.alipay.sofa.registry.server.meta.slot.util.SlotBuilder;
import com.alipay.sofa.registry.server.meta.slot.util.SlotTableBuilder;
import com.alipay.sofa.registry.server.shared.util.NodeUtils;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultSlotBalancerTest extends AbstractTest {

    private DefaultSlotBalancer slotBalancer;

    private LocalSlotManager slotManager;

    private DataServerManager dataServerManager;

    @Before
    public void beforeDefaultSlotBalancerTest() {
        NodeConfig nodeConfig = mock(NodeConfig.class);
        when(nodeConfig.getLocalDataCenter()).thenReturn(getDc());
        slotManager = new LocalSlotManager(nodeConfig);
        dataServerManager = mock(DataServerManager.class);
    }

    @Test
    public void testBalance() {
        List<DataNode> dataNodes = Lists.newArrayList(
                new DataNode(randomURL("10.0.0.1"), getDc()),
                new DataNode(randomURL("10.0.0.2"), getDc()),
                new DataNode(randomURL("10.0.0.3"), getDc()));
        when(dataServerManager.getClusterMembers()).thenReturn(dataNodes);

        SlotTable slotTable = randomSlotTable(Lists.newArrayList(
                new DataNode(randomURL("10.0.0.1"), getDc()),
                new DataNode(randomURL("10.0.0.2"), getDc())));
        SlotTableBuilder slotTableBuilder = new SlotTableBuilder(16, 2);
        slotTableBuilder.init(slotTable, NodeUtils.transferNodeToIpList(dataNodes));

        SlotBuilder sb = slotTableBuilder.getBuildingSlots().get(0);
        sb.getFollowers().clear();
        sb.getFollowers().add("10.0.0.3");
        sb = slotTableBuilder.getBuildingSlots().get(1);
        sb.getFollowers().clear();
        sb.getFollowers().add("10.0.0.3");
        sb = slotTableBuilder.getBuildingSlots().get(2);
        sb.getFollowers().clear();
        sb.getFollowers().add("10.0.0.3");

        slotManager.refresh(slotTableBuilder.build());
        slotBalancer = new DefaultSlotBalancer(slotManager, dataServerManager);
        for(int i = 0; i < 10; i++) {
            SlotTable slotTable1 = slotBalancer.balance();
            assertSlotTableNoDupLeaderFollower(slotTable1);
        }
    }
}