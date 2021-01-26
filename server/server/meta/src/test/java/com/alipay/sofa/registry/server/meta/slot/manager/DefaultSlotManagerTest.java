package com.alipay.sofa.registry.server.meta.slot.manager;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.SlotConfig;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.server.meta.AbstractTest;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DefaultSlotManagerTest extends AbstractTest {

    private DefaultSlotManager defaultSlotManager;

    private LocalSlotManager localSlotManager;

    private SlotManager slotManager;

    @Before
    public void beforeDefaultSlotManagerTest() {
        NodeConfig nodeConfig = mock(NodeConfig.class);
        when(nodeConfig.getLocalDataCenter()).thenReturn(getDc());
        localSlotManager = new LocalSlotManager(nodeConfig);
        slotManager = spy(localSlotManager);
        defaultSlotManager = new DefaultSlotManager(localSlotManager, slotManager);
    }

    @Test
    public void testRefresh() {
        Assert.assertEquals(SlotTable.INIT, defaultSlotManager.getSlotTable());
        defaultSlotManager.refresh(randomSlotTable());
        Assert.assertNotEquals(SlotTable.INIT, defaultSlotManager.getSlotTable());
    }

    @Test
    public void testGetRaftSlotManager() {
        Assert.assertEquals(slotManager, defaultSlotManager.getRaftSlotManager());
    }

    @Test
    public void testRaftCallLocal() throws TimeoutException, InterruptedException {
        makeRaftNonLeader();
        Assert.assertEquals(SlotConfig.SLOT_NUM, defaultSlotManager.getSlotNums());
        verify(slotManager, times(1)).getSlotNums();
        Assert.assertEquals(SlotConfig.SLOT_REPLICAS, defaultSlotManager.getSlotReplicaNums());
        verify(slotManager, times(1)).getSlotReplicaNums();

        makeRaftLeader();
        Assert.assertEquals(SlotConfig.SLOT_NUM, defaultSlotManager.getSlotNums());
        verify(slotManager, times(1)).getSlotNums();
        Assert.assertEquals(SlotConfig.SLOT_REPLICAS, defaultSlotManager.getSlotReplicaNums());
        verify(slotManager, times(1)).getSlotReplicaNums();

        List<DataNode> dataNodes = randomDataNodes(3);
        defaultSlotManager.refresh(randomSlotTable(dataNodes));
        verify(slotManager, times(1)).refresh(any(SlotTable.class));

        defaultSlotManager.getDataNodeManagedSlot(dataNodes.get(0), false);
        verify(slotManager, never()).getDataNodeManagedSlot(any(), anyBoolean());

        defaultSlotManager.getSlotTable();
        verify(slotManager, never()).getSlotTable();
    }
}