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
import com.alipay.sofa.registry.exception.InitializeException;
import com.alipay.sofa.registry.exception.SofaRegistryRuntimeException;
import com.alipay.sofa.registry.exception.StartException;
import com.alipay.sofa.registry.exception.StopException;
import com.alipay.sofa.registry.server.meta.AbstractTest;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.lease.data.DefaultDataServerManager;
import com.alipay.sofa.registry.server.meta.monitor.SlotTableMonitor;
import com.alipay.sofa.registry.server.meta.slot.arrange.ScheduledSlotArranger;
import com.alipay.sofa.registry.server.meta.slot.manager.DefaultSlotManager;
import com.alipay.sofa.registry.server.meta.slot.manager.LocalSlotManager;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.mockito.Mockito.*;

/**
 * @author zhuchen
 * @date Mar 2, 2021, 11:48:41 AM
 */
public class SlotTableResourceTest extends AbstractTest {

    private LocalSlotManager         slotManager;

    private DefaultSlotManager       defaultSlotManager;

    private DefaultDataServerManager dataServerManager;

    private SlotTableResource        resource;

    private ScheduledSlotArranger    slotArranger;

    private SlotTableMonitor         slotTableMonitor;

    @Before
    public void beforeSlotTableResourceTest() {
        NodeConfig nodeConfig = mock(NodeConfig.class);
        when(nodeConfig.getLocalDataCenter()).thenReturn(getDc());
        slotManager = new LocalSlotManager(nodeConfig);
        defaultSlotManager = mock(DefaultSlotManager.class);
        when(defaultSlotManager.getRaftSlotManager()).thenReturn(slotManager);
        dataServerManager = mock(DefaultDataServerManager.class);
        slotTableMonitor = mock(SlotTableMonitor.class);
        slotArranger = spy(new ScheduledSlotArranger(dataServerManager, slotManager,
            defaultSlotManager, slotTableMonitor));
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
        slotManager.refresh(slotTable);

        when(slotArranger.tryLock()).thenReturn(true);
        GenericResponse<SlotTable> current = resource.forceRefreshSlotTable();
        printSlotTable(current.getData());
        Assert.assertTrue(isSlotTableBalanced(slotManager.getSlotTable(), dataNodes));
    }

    @Test
    public void testStartReconcile() throws StartException, InitializeException {
        slotArranger.initialize();
        resource.startSlotTableReconcile();
        verify(slotArranger, atLeast(1)).start();
    }

    @Test
    public void testStopReconcile() throws Exception {
        slotArranger.postConstruct();
        resource.stopSlotTableReconcile();
        verify(slotArranger, atLeast(1)).stop();
    }

    @Test
    public void testGetDataStats() {
        when(dataServerManager.getDataServersStats()).thenThrow(
            new SofaRegistryRuntimeException("expected exception"));
        GenericResponse<Object> response = resource.getDataSlotStatuses();
        Assert.assertEquals("expected exception", response.getMessage());
    }

}