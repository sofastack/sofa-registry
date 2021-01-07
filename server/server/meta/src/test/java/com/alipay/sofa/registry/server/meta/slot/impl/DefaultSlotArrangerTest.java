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
package com.alipay.sofa.registry.server.meta.slot.impl;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.server.meta.AbstractTest;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.cluster.node.NodeAdded;
import com.alipay.sofa.registry.server.meta.lease.data.DataLeaseManager;
import com.alipay.sofa.registry.server.meta.lease.data.DefaultDataServerManager;
import com.alipay.sofa.registry.server.meta.slot.ArrangeTaskDispatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeoutException;

import static org.mockito.Mockito.*;

public class DefaultSlotArrangerTest extends AbstractTest {

    private DefaultSlotArranger             slotArranger;

    private DefaultDataServerManager        dataServerManager;

    private ArrangeTaskDispatcher<DataNode> arrangeTaskDispatcher;

    @Before
    public void beforeDefaultSlotArrangerTest() throws Exception {
        slotArranger = spy(new DefaultSlotArranger());
        DataLeaseManager leaseManager = new DataLeaseManager();
        MetaServerConfig metaServerConfig = mock(MetaServerConfig.class);
        when(metaServerConfig.getSchedulerHeartbeatExpBackOffBound()).thenReturn(60);
        dataServerManager = spy(new DefaultDataServerManager(leaseManager, leaseManager,
            metaServerConfig));
        arrangeTaskDispatcher = mock(DataServerArrangeTaskDispatcher.class);
        slotArranger.setArrangeTaskDispatcher(arrangeTaskDispatcher).setDataServerManager(
            dataServerManager);

        //        dataServerManager.postConstruct();
        slotArranger.postConstruct();
    }

    @After
    public void afterDefaultSlotArrangerTest() throws Exception {
        //        dataServerManager.preDestory();
        slotArranger.preDestroy();
    }

    @Test
    public void testUpdate() throws TimeoutException, InterruptedException {
        makeRaftLeader();
        dataServerManager.renew(new DataNode(randomURL(randomIp()), getDc()), 100);
        verify(slotArranger, times(1)).update(any(), any(NodeAdded.class));
        verify(slotArranger, times(1)).onServerAdded(any(DataNode.class));
    }

    @Test
    public void testOnServerAdded() throws TimeoutException, InterruptedException {
        dataServerManager.renew(new DataNode(randomURL(randomIp()), getDc()), 100);
        verify(slotArranger, times(1)).update(any(), any(NodeAdded.class));
        verify(slotArranger, times(1)).onServerAdded(any(DataNode.class));
        verify(arrangeTaskDispatcher, times(1)).serverAlive(any(DataNode.class));
    }

    @Test
    public void testOnServerRemoved() {
        DataNode dataNode = new DataNode(randomURL(randomIp()), getDc());
        dataServerManager.renew(dataNode, 100);
        dataServerManager.cancel(dataServerManager.getLease(dataNode).prepareCancel());

        verify(slotArranger, times(2)).update(any(), any());
        verify(slotArranger, times(1)).onServerRemoved(any(DataNode.class));
        verify(arrangeTaskDispatcher, times(1)).serverDead(any(DataNode.class));
    }
}