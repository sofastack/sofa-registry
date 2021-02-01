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

    private LocalSlotManager   localSlotManager;

    private SlotManager        slotManager;

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