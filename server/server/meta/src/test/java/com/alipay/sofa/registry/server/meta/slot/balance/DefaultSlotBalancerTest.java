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
package com.alipay.sofa.registry.server.meta.slot.balance;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.server.meta.AbstractTest;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.lease.data.DataServerManager;
import com.alipay.sofa.registry.server.meta.slot.manager.LocalSlotManager;
import com.alipay.sofa.registry.server.meta.slot.util.builder.SlotBuilder;
import com.alipay.sofa.registry.server.meta.slot.util.builder.SlotTableBuilder;
import com.alipay.sofa.registry.server.shared.util.NodeUtils;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultSlotBalancerTest extends AbstractTest {

    private DefaultSlotBalancer slotBalancer;

    private LocalSlotManager    slotManager;

    private DataServerManager   dataServerManager;

    @Before
    public void beforeDefaultSlotBalancerTest() {
        NodeConfig nodeConfig = mock(NodeConfig.class);
        when(nodeConfig.getLocalDataCenter()).thenReturn(getDc());
        slotManager = new LocalSlotManager(nodeConfig);
        dataServerManager = mock(DataServerManager.class);
    }

    @Test
    public void testBalance() {
        List<DataNode> dataNodes = Lists.newArrayList(new DataNode(randomURL("10.0.0.1"), getDc()),
            new DataNode(randomURL("10.0.0.2"), getDc()), new DataNode(randomURL("10.0.0.3"),
                getDc()));
        when(dataServerManager.getClusterMembers()).thenReturn(dataNodes);

        SlotTable slotTable = randomSlotTable(Lists.newArrayList(new DataNode(
            randomURL("10.0.0.1"), getDc()), new DataNode(randomURL("10.0.0.2"), getDc())));
        SlotTableBuilder slotTableBuilder = new SlotTableBuilder(slotTable, 16, 2);
        slotTableBuilder.init(NodeUtils.transferNodeToIpList(dataNodes));

        SlotBuilder sb = slotTableBuilder.getOrCreate(0);
        replaceFollower(sb, "10.0.0.3");
        sb = slotTableBuilder.getOrCreate(1);
        replaceFollower(sb, "10.0.0.3");
        sb = slotTableBuilder.getOrCreate(2);
        replaceFollower(sb, "10.0.0.3");

        slotManager.refresh(slotTableBuilder.build());
        slotBalancer = new DefaultSlotBalancer(slotTableBuilder, Lists.newArrayList("10.0.0.1",
            "10.0.0.2", "10.0.0.3"));
        for (int i = 0; i < 10; i++) {
            SlotTable slotTable1 = slotBalancer.balance();
            if (i == 0) {
                Assert.assertNotNull(slotTable1);
            }
            if (slotTable1 == null) {
                break;
            }
            logger.info("balance {}: {}", i, slotTable1);
            assertSlotTableNoDupLeaderFollower(slotTable1);
        }
    }

    private static void replaceFollower(SlotBuilder sb, String follower) {
        for (String f : sb.getFollowers()) {
            sb.removeFollower(f);
        }
        sb.addFollower(follower);
    }
}
