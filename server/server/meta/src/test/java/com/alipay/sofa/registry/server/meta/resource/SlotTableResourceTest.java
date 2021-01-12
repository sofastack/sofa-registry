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
package com.alipay.sofa.registry.server.meta.resource;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.server.meta.AbstractTest;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.lease.data.DataServerManager;
import com.alipay.sofa.registry.server.meta.lease.data.DefaultDataServerManager;
import com.alipay.sofa.registry.server.meta.slot.impl.DefaultSlotManager;
import com.alipay.sofa.registry.server.meta.slot.impl.LocalSlotManager;
import com.google.common.collect.Maps;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SlotTableResourceTest extends AbstractTest {

    private LocalSlotManager         slotManager;

    private DefaultSlotManager       defaultSlotManager;

    private DefaultDataServerManager dataServerManager;

    private SlotTableResource        resource;

    @Before
    public void beforeSlotTableResourceTest() {
        NodeConfig nodeConfig = mock(NodeConfig.class);
        when(nodeConfig.getLocalDataCenter()).thenReturn(getDc());
        slotManager = new LocalSlotManager(nodeConfig);
        defaultSlotManager = mock(DefaultSlotManager.class);
        when(defaultSlotManager.getRaftSlotManager()).thenReturn(slotManager);
        dataServerManager = mock(DefaultDataServerManager.class);
        resource = new SlotTableResource(defaultSlotManager, slotManager, dataServerManager);
    }

    @Test
    public void testForceRefreshSlotTable() throws TimeoutException, InterruptedException {
        makeRaftLeader();
        List<DataNode> dataNodes = Lists.newArrayList(new DataNode(randomURL(randomIp()), getDc()),
            new DataNode(randomURL(randomIp()), getDc()), new DataNode(randomURL(randomIp()),
                getDc()));
        when(dataServerManager.getClusterMembers()).thenReturn(dataNodes);
        SlotTable slotTable = new SlotTableGenerator(dataNodes).createLeaderUnBalancedSlotTable();
        printSlotTable(slotTable);
        slotManager.refresh(slotTable);
        Assert.assertFalse(isSlotTableBalanced(slotManager.getSlotTable(), dataNodes));

        SlotTable current = resource.forceRefreshSlotTable();
        printSlotTable(current);
        Assert.assertTrue(isSlotTableBalanced(slotManager.getSlotTable(), dataNodes));
    }

}