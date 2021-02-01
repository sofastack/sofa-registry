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

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.server.meta.AbstractTest;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.lease.data.DefaultDataServerManager;
import com.alipay.sofa.registry.server.meta.slot.arrange.ScheduledSlotArranger;
import com.alipay.sofa.registry.server.meta.slot.manager.DefaultSlotManager;
import com.alipay.sofa.registry.server.meta.slot.manager.LocalSlotManager;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SlotTableResourceTest extends AbstractTest {

    private LocalSlotManager         slotManager;

    private DefaultSlotManager       defaultSlotManager;

    private DefaultDataServerManager dataServerManager;

    private SlotTableResource        resource;

    private ScheduledSlotArranger    slotArranger;

    @Before
    public void beforeSlotTableResourceTest() {
        NodeConfig nodeConfig = mock(NodeConfig.class);
        when(nodeConfig.getLocalDataCenter()).thenReturn(getDc());
        slotManager = new LocalSlotManager(nodeConfig);
        defaultSlotManager = mock(DefaultSlotManager.class);
        when(defaultSlotManager.getRaftSlotManager()).thenReturn(slotManager);
        dataServerManager = mock(DefaultDataServerManager.class);
        slotArranger = mock(ScheduledSlotArranger.class);
        resource = new SlotTableResource(defaultSlotManager, slotManager, dataServerManager,
            slotArranger);
    }

    @Test
    public void testForceRefreshSlotTable() throws TimeoutException, InterruptedException {
        makeRaftLeader();
        List<DataNode> dataNodes = Lists.newArrayList(new DataNode(randomURL(randomIp()), getDc()),
            new DataNode(randomURL(randomIp()), getDc()), new DataNode(randomURL(randomIp()),
                getDc()));
        when(dataServerManager.getClusterMembers()).thenReturn(dataNodes);
        SlotTable slotTable = new SlotTableGenerator(dataNodes).createLeaderUnBalancedSlotTable();
        //        printSlotTable(slotTable);
        slotManager.refresh(slotTable);
        //        Assert.assertFalse(isSlotTableBalanced(slotManager.getSlotTable(), dataNodes));

        when(slotArranger.tryLock()).thenReturn(true);
        GenericResponse<SlotTable> current = resource.forceRefreshSlotTable();
        printSlotTable(current.getData());
        Assert.assertTrue(isSlotTableBalanced(slotManager.getSlotTable(), dataNodes));
    }

}