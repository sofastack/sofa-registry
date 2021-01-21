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
package com.alipay.sofa.registry.server.meta.slot.tasks;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotConfig;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.exception.InitializeException;
import com.alipay.sofa.registry.server.meta.AbstractTest;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.lease.data.DefaultDataServerManager;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.alipay.sofa.registry.server.meta.slot.arrange.ScheduledSlotArranger;
import com.alipay.sofa.registry.server.meta.slot.assigner.DefaultSlotAssigner;
import com.alipay.sofa.registry.server.meta.slot.balance.DefaultSlotBalancer;
import com.alipay.sofa.registry.server.meta.slot.manager.DefaultSlotManager;
import com.alipay.sofa.registry.server.meta.slot.manager.LocalSlotManager;
import com.alipay.sofa.registry.util.FileUtils;
import com.alipay.sofa.registry.util.JsonUtils;
import com.alipay.sofa.registry.util.NamedThreadFactory;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.Mockito.*;

/**
 * @author chen.zhu
 * <p>
 * Jan 14, 2021
 */
public class SlotMigrationIntegrationTest extends AbstractTest {

    private DefaultSlotManager       defaultSlotManager;

    private SlotManager              raftSlotManager;

    private LocalSlotManager         localSlotManager;

    @Mock
    private DefaultDataServerManager dataServerManager;

    @BeforeClass
    public static void beforeSlotMigrationIntegrationTestClass() {
        System.setProperty("slot.frozen.milli", "1");
    }

    @Before
    public void beforeSlotMigrationIntegrationTest() throws Exception {
        MockitoAnnotations.initMocks(this);
        List<DataNode> dataNodes = Lists.newArrayList(new DataNode(randomURL(randomIp()), getDc()),
            new DataNode(randomURL(randomIp()), getDc()), new DataNode(randomURL(randomIp()),
                getDc()), new DataNode(randomURL(randomIp()), getDc()));
        when(dataServerManager.getClusterMembers()).thenReturn(dataNodes);
        NodeConfig nodeConfig = mock(NodeConfig.class);
        when(nodeConfig.getLocalDataCenter()).thenReturn(getDc());
        raftSlotManager = localSlotManager = new LocalSlotManager(nodeConfig);
        defaultSlotManager = new DefaultSlotManager(localSlotManager, raftSlotManager);
    }

    @Test
    public void testDataServerLifecycle() throws Exception {
        System.setProperty("slot.leader.max.move", SlotConfig.SLOT_NUM + "");
        makeRaftLeader();
        List<DataNode> dataNodes = Lists.newArrayList(new DataNode(new URL("100.88.142.36"),
            getDc()), new DataNode(new URL("100.88.142.32"), getDc()), new DataNode(new URL(
            "100.88.142.19"), getDc()));
        when(dataServerManager.getClusterMembers()).thenReturn(dataNodes);
        InitReshardingTask slotTask = new InitReshardingTask(localSlotManager, raftSlotManager,
            dataServerManager);
        slotTask.run();
//        logger.info(JsonUtils.getJacksonObjectMapper().writerWithDefaultPrettyPrinter()
//            .writeValueAsString(localSlotManager.getSlotTable()));
        Assert.assertTrue(isSlotTableBalanced(localSlotManager.getSlotTable(),
            dataServerManager.getClusterMembers()));
        Assert.assertTrue(isSlotTableLeaderBalanced(localSlotManager.getSlotTable(),
            dataServerManager.getClusterMembers()));
        assertSlotTableNoDupLeaderFollower(localSlotManager.getSlotTable());

        dataNodes = Lists.newArrayList(new DataNode(new URL("100.88.142.36"), getDc()),
            new DataNode(new URL("100.88.142.32"), getDc()));
        when(dataServerManager.getClusterMembers()).thenReturn(dataNodes);
        DefaultSlotAssigner assigner = new DefaultSlotAssigner(localSlotManager, dataServerManager);
        localSlotManager.refresh(assigner.assign());
        Assert.assertTrue(isSlotTableBalanced(localSlotManager.getSlotTable(),
            dataServerManager.getClusterMembers()));
        Assert.assertTrue(isSlotTableLeaderBalanced(localSlotManager.getSlotTable(),
            dataServerManager.getClusterMembers()));
        assertSlotTableNoDupLeaderFollower(localSlotManager.getSlotTable());

        Thread.sleep(2);
        DefaultSlotBalancer slotBalancer = new DefaultSlotBalancer(localSlotManager,
            dataServerManager);
        localSlotManager.refresh(slotBalancer.balance());
//        logger.info(JsonUtils.getJacksonObjectMapper().writerWithDefaultPrettyPrinter()
//            .writeValueAsString(localSlotManager.getSlotTable()));
        Assert.assertTrue(isSlotTableBalanced(localSlotManager.getSlotTable(),
            dataServerManager.getClusterMembers()));
        Assert.assertTrue(isSlotTableLeaderBalanced(localSlotManager.getSlotTable(),
            dataServerManager.getClusterMembers()));
        assertSlotTableNoDupLeaderFollower(localSlotManager.getSlotTable());

        dataNodes = Lists.newArrayList(new DataNode(new URL("100.88.142.36"), getDc()));
        when(dataServerManager.getClusterMembers()).thenReturn(dataNodes);
        assigner = new DefaultSlotAssigner(localSlotManager, dataServerManager);
        localSlotManager.refresh(assigner.assign());
//        logger.info(JsonUtils.getJacksonObjectMapper().writerWithDefaultPrettyPrinter()
//            .writeValueAsString(localSlotManager.getSlotTable()));

        dataNodes = Lists.newArrayList(new DataNode(new URL("100.88.142.36"), getDc()),
            new DataNode(new URL("100.88.142.32"), getDc()));
        when(dataServerManager.getClusterMembers()).thenReturn(dataNodes);
        assigner = new DefaultSlotAssigner(localSlotManager, dataServerManager);
        localSlotManager.refresh(assigner.assign());
//        logger.info(JsonUtils.getJacksonObjectMapper().writerWithDefaultPrettyPrinter()
//            .writeValueAsString(localSlotManager.getSlotTable()));
        Assert.assertTrue(isSlotTableBalanced(localSlotManager.getSlotTable(),
            dataServerManager.getClusterMembers()));
        assertSlotTableNoDupLeaderFollower(localSlotManager.getSlotTable());

        Thread.sleep(2);
        slotBalancer = new DefaultSlotBalancer(localSlotManager, dataServerManager);
        localSlotManager.refresh(slotBalancer.balance());
//        logger.info(JsonUtils.getJacksonObjectMapper().writerWithDefaultPrettyPrinter()
//            .writeValueAsString(localSlotManager.getSlotTable()));
        Assert.assertTrue(isSlotTableBalanced(localSlotManager.getSlotTable(),
            dataServerManager.getClusterMembers()));
        Assert.assertTrue(isSlotTableLeaderBalanced(localSlotManager.getSlotTable(),
            dataServerManager.getClusterMembers()));
        assertSlotTableNoDupLeaderFollower(localSlotManager.getSlotTable());

        Thread.sleep(2);
        dataNodes = Lists.newArrayList(new DataNode(new URL("100.88.142.36"), getDc()),
            new DataNode(new URL("100.88.142.32"), getDc()), new DataNode(new URL("100.88.142.19"),
                getDc()));
        when(dataServerManager.getClusterMembers()).thenReturn(dataNodes);
        slotBalancer = new DefaultSlotBalancer(localSlotManager, dataServerManager);
        localSlotManager.refresh(slotBalancer.balance());
//        logger.info(JsonUtils.getJacksonObjectMapper().writerWithDefaultPrettyPrinter()
//            .writeValueAsString(localSlotManager.getSlotTable()));
        Assert.assertTrue(isSlotTableBalanced(localSlotManager.getSlotTable(),
            dataServerManager.getClusterMembers()));
        assertSlotTableNoDupLeaderFollower(localSlotManager.getSlotTable());

        Thread.sleep(2);
        dataNodes = Lists.newArrayList(new DataNode(new URL("100.88.142.36"), getDc()),
            new DataNode(new URL("100.88.142.32"), getDc()), new DataNode(new URL("100.88.142.19"),
                getDc()));
        when(dataServerManager.getClusterMembers()).thenReturn(dataNodes);
        slotBalancer = new DefaultSlotBalancer(localSlotManager, dataServerManager);
        localSlotManager.refresh(slotBalancer.balance());
//        logger.info(JsonUtils.getJacksonObjectMapper().writerWithDefaultPrettyPrinter()
//            .writeValueAsString(localSlotManager.getSlotTable()));
        Assert.assertTrue(isSlotTableBalanced(localSlotManager.getSlotTable(),
            dataServerManager.getClusterMembers()));
        Assert.assertTrue(isSlotTableLeaderBalanced(localSlotManager.getSlotTable(),
            dataServerManager.getClusterMembers()));
        assertSlotTableNoDupLeaderFollower(localSlotManager.getSlotTable());
    }

    @Test
    public void testDataServerAddedOneByOne() throws Exception {
        System.setProperty("slot.leader.max.move", SlotConfig.SLOT_NUM + "");
        ScheduledSlotArranger assigner = new ScheduledSlotArranger(dataServerManager,
            localSlotManager, defaultSlotManager);
        List<DataNode> dataNodes = Lists.newArrayList(new DataNode(new URL("100.88.142.32"),
            getDc()));
        when(dataServerManager.getClusterMembers()).thenReturn(dataNodes);
        makeRaftLeader();
        assigner.getTask().runUnthrowable();
        logger.info(JsonUtils.getJacksonObjectMapper().writerWithDefaultPrettyPrinter()
            .writeValueAsString(localSlotManager.getSlotTable()));

        dataNodes = Lists.newArrayList(new DataNode(new URL("100.88.142.32"), getDc()),
            new DataNode(new URL("100.88.142.36"), getDc()));
        when(dataServerManager.getClusterMembers()).thenReturn(dataNodes);
        assigner.getTask().runUnthrowable();
        logger.info(JsonUtils.getJacksonObjectMapper().writerWithDefaultPrettyPrinter()
            .writeValueAsString(localSlotManager.getSlotTable()));
        Assert.assertTrue(isSlotTableBalanced(localSlotManager.getSlotTable(),
            dataServerManager.getClusterMembers()));
        assertSlotTableNoDupLeaderFollower(localSlotManager.getSlotTable());

        Thread.sleep(2);
        assigner.getTask().runUnthrowable();
        logger.info(JsonUtils.getJacksonObjectMapper().writerWithDefaultPrettyPrinter()
            .writeValueAsString(localSlotManager.getSlotTable()));
        Assert.assertTrue(isSlotTableBalanced(localSlotManager.getSlotTable(),
            dataServerManager.getClusterMembers()));
        Assert.assertTrue(isSlotTableLeaderBalanced(localSlotManager.getSlotTable(),
            dataServerManager.getClusterMembers()));
        assertSlotTableNoDupLeaderFollower(localSlotManager.getSlotTable());

        Thread.sleep(2);
        dataNodes = Lists.newArrayList(new DataNode(new URL("100.88.142.32"), getDc()),
            new DataNode(new URL("100.88.142.36"), getDc()), new DataNode(new URL("100.88.142.19"),
                getDc()));
        when(dataServerManager.getClusterMembers()).thenReturn(dataNodes);
        assigner.getTask().runUnthrowable();
        logger.info(JsonUtils.getJacksonObjectMapper().writerWithDefaultPrettyPrinter()
            .writeValueAsString(localSlotManager.getSlotTable()));
        Assert.assertTrue(isSlotTableBalanced(localSlotManager.getSlotTable(),
            dataServerManager.getClusterMembers()));
        assertSlotTableNoDupLeaderFollower(localSlotManager.getSlotTable());

        Thread.sleep(2);
        dataNodes = Lists.newArrayList(new DataNode(new URL("100.88.142.32"), getDc()),
            new DataNode(new URL("100.88.142.36"), getDc()), new DataNode(new URL("100.88.142.19"),
                getDc()));
        when(dataServerManager.getClusterMembers()).thenReturn(dataNodes);
        assigner.getTask().runUnthrowable();
        logger.info(JsonUtils.getJacksonObjectMapper().writerWithDefaultPrettyPrinter()
            .writeValueAsString(localSlotManager.getSlotTable()));
        Assert.assertTrue(isSlotTableBalanced(localSlotManager.getSlotTable(),
            dataServerManager.getClusterMembers()));
        Assert.assertTrue(isSlotTableLeaderBalanced(localSlotManager.getSlotTable(),
            dataServerManager.getClusterMembers()));
        assertSlotTableNoDupLeaderFollower(localSlotManager.getSlotTable());
    }

    @Test
    public void testDataServerAddedAndDeleted() throws Exception {
        System.setProperty("slot.leader.max.move", SlotConfig.SLOT_NUM + "");
        ScheduledSlotArranger assigner = new ScheduledSlotArranger(dataServerManager,
                localSlotManager, defaultSlotManager);
        List<DataNode> dataNodes = Lists.newArrayList(new DataNode(new URL("100.88.142.32"),
                getDc()));
        when(dataServerManager.getClusterMembers()).thenReturn(dataNodes);
        makeRaftLeader();
        assigner.getTask().runUnthrowable();
        logger.info(JsonUtils.getJacksonObjectMapper().writerWithDefaultPrettyPrinter()
                .writeValueAsString(localSlotManager.getSlotTable()));

        dataNodes = Lists.newArrayList(new DataNode(new URL("100.88.142.32"), getDc()),
                new DataNode(new URL("100.88.142.36"), getDc()));
        when(dataServerManager.getClusterMembers()).thenReturn(dataNodes);
        assigner.getTask().runUnthrowable();
        logger.info(JsonUtils.getJacksonObjectMapper().writerWithDefaultPrettyPrinter()
                .writeValueAsString(localSlotManager.getSlotTable()));
        Assert.assertTrue(isSlotTableBalanced(localSlotManager.getSlotTable(),
                dataServerManager.getClusterMembers()));
        assertSlotTableNoDupLeaderFollower(localSlotManager.getSlotTable());

        Thread.sleep(2);
        assigner.getTask().runUnthrowable();
        logger.info(JsonUtils.getJacksonObjectMapper().writerWithDefaultPrettyPrinter()
                .writeValueAsString(localSlotManager.getSlotTable()));
        Assert.assertTrue(isSlotTableBalanced(localSlotManager.getSlotTable(),
                dataServerManager.getClusterMembers()));
        Assert.assertTrue(isSlotTableLeaderBalanced(localSlotManager.getSlotTable(),
                dataServerManager.getClusterMembers()));
        assertSlotTableNoDupLeaderFollower(localSlotManager.getSlotTable());

        Thread.sleep(2);
        dataNodes = Lists.newArrayList(new DataNode(new URL("100.88.142.32"), getDc()),
                new DataNode(new URL("100.88.142.36"), getDc()), new DataNode(new URL("100.88.142.19"),
                        getDc()));
        when(dataServerManager.getClusterMembers()).thenReturn(dataNodes);
        assigner.getTask().runUnthrowable();
        logger.info(JsonUtils.getJacksonObjectMapper().writerWithDefaultPrettyPrinter()
                .writeValueAsString(localSlotManager.getSlotTable()));
        Assert.assertTrue(isSlotTableBalanced(localSlotManager.getSlotTable(),
                dataServerManager.getClusterMembers()));
        assertSlotTableNoDupLeaderFollower(localSlotManager.getSlotTable());

        Thread.sleep(2);
        dataNodes = Lists.newArrayList(new DataNode(new URL("100.88.142.32"), getDc()),
                new DataNode(new URL("100.88.142.36"), getDc()), new DataNode(new URL("100.88.142.19"),
                        getDc()));
        when(dataServerManager.getClusterMembers()).thenReturn(dataNodes);
        assigner.getTask().runUnthrowable();
        logger.info(JsonUtils.getJacksonObjectMapper().writerWithDefaultPrettyPrinter()
                .writeValueAsString(localSlotManager.getSlotTable()));
        Assert.assertTrue(isSlotTableBalanced(localSlotManager.getSlotTable(),
                dataServerManager.getClusterMembers()));
        Assert.assertTrue(isSlotTableLeaderBalanced(localSlotManager.getSlotTable(),
                dataServerManager.getClusterMembers()));
        assertSlotTableNoDupLeaderFollower(localSlotManager.getSlotTable());

        Thread.sleep(2);
        dataNodes = Lists.newArrayList(new DataNode(new URL("100.88.142.32"), getDc()),
                new DataNode(new URL("100.88.142.36"), getDc()));
        when(dataServerManager.getClusterMembers()).thenReturn(dataNodes);
        assigner.getTask().runUnthrowable();
        logger.info(JsonUtils.getJacksonObjectMapper().writerWithDefaultPrettyPrinter()
                .writeValueAsString(localSlotManager.getSlotTable()));
        Assert.assertTrue(isSlotTableBalanced(localSlotManager.getSlotTable(),
                dataServerManager.getClusterMembers()));
        Assert.assertTrue(isSlotTableLeaderBalanced(localSlotManager.getSlotTable(),
                dataServerManager.getClusterMembers()));
        assertSlotTableNoDupLeaderFollower(localSlotManager.getSlotTable());

        Thread.sleep(2);
        dataNodes = Lists.newArrayList(new DataNode(new URL("100.88.142.32"), getDc()),
                new DataNode(new URL("100.88.142.36"), getDc()), new DataNode(new URL("100.88.142.19"),
                        getDc()));
        when(dataServerManager.getClusterMembers()).thenReturn(dataNodes);
        assigner.getTask().runUnthrowable();
        logger.info(JsonUtils.getJacksonObjectMapper().writerWithDefaultPrettyPrinter()
                .writeValueAsString(localSlotManager.getSlotTable()));
        Assert.assertTrue(isSlotTableBalanced(localSlotManager.getSlotTable(),
                dataServerManager.getClusterMembers()));
        assertSlotTableNoDupLeaderFollower(localSlotManager.getSlotTable());

        Thread.sleep(2);
        dataNodes = Lists.newArrayList(new DataNode(new URL("100.88.142.32"), getDc()),
                new DataNode(new URL("100.88.142.36"), getDc()), new DataNode(new URL("100.88.142.19"),
                        getDc()));
        when(dataServerManager.getClusterMembers()).thenReturn(dataNodes);
        assigner.getTask().runUnthrowable();
        logger.info(JsonUtils.getJacksonObjectMapper().writerWithDefaultPrettyPrinter()
                .writeValueAsString(localSlotManager.getSlotTable()));
        Assert.assertTrue(isSlotTableBalanced(localSlotManager.getSlotTable(),
                dataServerManager.getClusterMembers()));
        Assert.assertTrue(isSlotTableLeaderBalanced(localSlotManager.getSlotTable(),
                dataServerManager.getClusterMembers()));
        assertSlotTableNoDupLeaderFollower(localSlotManager.getSlotTable());
    }

    @Test
    public void testDataLeaderBalance() throws Exception {
        ScheduledSlotArranger assigner = new ScheduledSlotArranger(dataServerManager,
                localSlotManager, defaultSlotManager);
        byte[] bytes = FileUtils.readFileToByteArray(new File("src/test/resources/test/slot-table.json"));
        SlotTable prevSlotTable = JsonUtils.getJacksonObjectMapper().readValue(bytes, InnerSlotTable.class).toSlotTable();
        localSlotManager.refresh(prevSlotTable);
        List<DataNode> dataNodes = Lists.newArrayList(new DataNode(new URL("100.88.142.32"), getDc()),
                new DataNode(new URL("100.88.142.36"), getDc()), new DataNode(new URL("100.88.142.19"),
                        getDc()));
        when(dataServerManager.getClusterMembers()).thenReturn(dataNodes);
        makeRaftLeader();
        assigner.getTask().runUnthrowable();

        Thread.sleep(2);
        assigner.getTask().runUnthrowable();

        Thread.sleep(2);
        assigner.getTask().runUnthrowable();

        Thread.sleep(2);
        assigner.getTask().runUnthrowable();

        logger.info(JsonUtils.getJacksonObjectMapper().writerWithDefaultPrettyPrinter()
                .writeValueAsString(localSlotManager.getSlotTable()));
        Assert.assertTrue(isSlotTableLeaderBalanced(localSlotManager.getSlotTable(),
                dataServerManager.getClusterMembers()));
        assertSlotTableNoDupLeaderFollower(localSlotManager.getSlotTable());
    }


    public static class InnerSlotTable {
        private long epoch;
        private List<Slot> slots;
        private List<String> dataServers;

        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public InnerSlotTable(@JsonProperty("id")long epoch,
                              @JsonProperty("slots")List<Slot> slots,
                              @JsonProperty("dataServers")List<String> dataServers) {
            this.epoch = epoch;
            this.slots = slots;
            this.dataServers = dataServers;
        }

        public long getEpoch() {
            return epoch;
        }

        public List<Slot> getSlots() {
            return slots;
        }

        public List<String> getDataServers() {
            return dataServers;
        }

        public SlotTable toSlotTable() {
            Map<Integer, Slot> slotMap = Maps.newHashMap();
            slots.forEach(slot -> slotMap.put(slot.getId(), slot));
            return new SlotTable(epoch, slotMap);
        }
    }
}
