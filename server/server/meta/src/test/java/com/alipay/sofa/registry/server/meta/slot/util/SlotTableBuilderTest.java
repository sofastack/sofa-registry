package com.alipay.sofa.registry.server.meta.slot.util;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.DataNodeSlot;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.server.meta.AbstractTest;
import com.alipay.sofa.registry.server.shared.util.NodeUtils;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

import static org.junit.Assert.*;

public class SlotTableBuilderTest extends AbstractTest {

    private SlotTableBuilder slotTableBuilder;

    @Before
    public void beforeSlotTableBuilderTest() throws Exception {
        slotTableBuilder = new SlotTableBuilder(16, 2);
    }

    @Test
    public void testGetOrCreate() throws InterruptedException {
        int slotId = random.nextInt(16);
        int tasks = 10;
        CyclicBarrier barrier = new CyclicBarrier(tasks);
        CountDownLatch latch = new CountDownLatch(tasks);
        List<SlotBuilder> result = new ArrayList<>(tasks);
        for(int i = 0; i < tasks; i++) {
            final int index = i;
            executors.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        barrier.await();
                        result.add(index, slotTableBuilder.getOrCreate(slotId));
                    } catch (Exception ignore) {

                    } finally {
                        latch.countDown();
                    }
                }
            });
        }
        latch.await();
        SlotBuilder sample = slotTableBuilder.getOrCreate(slotId);
        for(SlotBuilder sb : result) {
            assertSame(sb, sample);
        }
    }

    @Test
    public void testInit() {
        List<DataNode> dataNodes = randomDataNodes(6);
        SlotTable slotTable = randomSlotTable(dataNodes);
        DataNode alternative = new DataNode(randomURL(randomIp()), getDc());
        dataNodes.add(alternative);
        slotTableBuilder.init(slotTable, NodeUtils.transferNodeToIpList(dataNodes));
        Assert.assertNotNull(slotTableBuilder.getDataNodeSlot(alternative.getIp()));

        for(int slotId = 0; slotId < slotTable.getSlots().size(); slotId++) {
            Assert.assertEquals(slotTableBuilder.getOrCreate(slotId).getLeader(), slotTable.getSlot(slotId).getLeader());
            Assert.assertEquals(slotTableBuilder.getOrCreate(slotId).getEpoch(), slotTable.getSlot(slotId).getLeaderEpoch());
            Assert.assertEquals(slotTableBuilder.getOrCreate(slotId).getFollowers(), slotTable.getSlot(slotId).getFollowers());
        }


        for(DataNode dataNode : dataNodes) {
            List<DataNodeSlot> samples = slotTable.transfer(dataNode.getIp(), false);
            if(!samples.isEmpty()) {
                Assert.assertEquals(samples.get(0), slotTableBuilder.getDataNodeSlot(dataNode.getIp()));
            }
        }
    }

    @Test
    public void testSetLeader() {
        List<DataNode> dataNodes = randomDataNodes(3);
        SlotTable slotTable = randomSlotTable(dataNodes);
        slotTableBuilder.init(slotTable, NodeUtils.transferNodeToIpList(dataNodes));
        int slotId = random.nextInt(16);
        SlotBuilder sb = slotTableBuilder.getOrCreate(slotId);
        String prevLeader = sb.getLeader();
        long prevEpoch = sb.getEpoch();
        String nextLeader = null;
        int index = 0;
        do {
            nextLeader = dataNodes.get(index++).getIp();
        } while (nextLeader.equals(prevLeader));
        logger.info("[prev leader] {}", prevLeader);
        logger.info("[next leader] {}", nextLeader);
        slotTableBuilder.flipLeaderTo(slotId, nextLeader);
        Assert.assertNotEquals(sb.getLeader(), prevLeader);
        logger.info("[followers] {}", sb.getFollowers());
        Assert.assertTrue(sb.getEpoch() > prevEpoch);

        Assert.assertTrue(slotTableBuilder.getDataNodeSlot(nextLeader).getLeaders().contains(slotId));
    }

    @Test
    public void testRemoveFollower() {
        List<DataNode> dataNodes = randomDataNodes(6);
        SlotTable slotTable = randomSlotTable(dataNodes);
        slotTableBuilder.init(slotTable, NodeUtils.transferNodeToIpList(dataNodes));
        int slotId = random.nextInt(16);
        String follower = slotTable.getSlot(slotId).getFollowers().iterator().next();
        slotTableBuilder.removeFollower(slotId, follower);
        Assert.assertFalse(slotTableBuilder.getOrCreate(slotId).getFollowers().contains(follower));
        Assert.assertFalse(slotTableBuilder.getDataNodeSlot(follower).getFollowers().contains(slotId));
    }

    @Test
    public void testAddFollower() {
        List<DataNode> dataNodes = randomDataNodes(6);
        SlotTable slotTable = randomSlotTable(dataNodes);
        slotTableBuilder.init(slotTable, NodeUtils.transferNodeToIpList(dataNodes));
        int slotId = random.nextInt(16);
        SlotBuilder sb  = slotTableBuilder.getOrCreate(slotId);
        String prevFollower = sb.getFollowers().iterator().next();

        long prevEpoch = sb.getEpoch();
        String nextFollower = prevFollower;
        int index = 0;
        do {
            nextFollower = dataNodes.get(index++).getIp();
        } while (nextFollower.equals(prevFollower) || nextFollower.equals(sb.getLeader()));

        slotTableBuilder.addFollower(slotId, nextFollower);
        Assert.assertFalse(slotTableBuilder.getOrCreate(slotId).getFollowers().contains(nextFollower));
        Assert.assertFalse(slotTableBuilder.getDataNodeSlot(nextFollower).getFollowers().contains(slotId));

        sb.removeFollower(prevFollower);
        slotTableBuilder.addFollower(slotId, nextFollower);
        Assert.assertTrue(slotTableBuilder.getOrCreate(slotId).getFollowers().contains(nextFollower));
        Assert.assertTrue(slotTableBuilder.getDataNodeSlot(nextFollower).getFollowers().contains(slotId));
    }

    @Test
    public void testHasNoAssignedSlots() {
        List<DataNode> dataNodes = randomDataNodes(6);
        SlotTable slotTable = randomSlotTable(dataNodes);
        slotTableBuilder.init(slotTable, NodeUtils.transferNodeToIpList(dataNodes));
        Assert.assertFalse(slotTableBuilder.hasNoAssignedSlots());

        slotTableBuilder = new SlotTableBuilder(16, 2);
        Assert.assertTrue(slotTableBuilder.hasNoAssignedSlots());

        dataNodes.add(new DataNode(randomURL(randomIp()), getDc()));
        slotTableBuilder.init(slotTable, NodeUtils.transferNodeToIpList(dataNodes));
        Assert.assertFalse(slotTableBuilder.hasNoAssignedSlots());
    }

    @Test
    public void testRemoveDataServerSlots() {
        List<DataNode> dataNodes = randomDataNodes(6);
        SlotTable slotTable = randomSlotTable(dataNodes);
        slotTableBuilder.init(slotTable, NodeUtils.transferNodeToIpList(dataNodes));
        Assert.assertFalse(slotTableBuilder.hasNoAssignedSlots());

        slotTableBuilder.removeDataServerSlots(dataNodes.get(0).getIp());
        Assert.assertTrue(slotTableBuilder.hasNoAssignedSlots());

        Assert.assertTrue(slotTableBuilder.getDataNodeSlot(dataNodes.get(0).getIp()).getFollowers().isEmpty());
        Assert.assertTrue(slotTableBuilder.getDataNodeSlot(dataNodes.get(0).getIp()).getLeaders().isEmpty());

    }

    @Test
    public void testGetNoAssignedSlots() {
        List<DataNode> dataNodes = randomDataNodes(6);
        SlotTable slotTable = randomSlotTable(dataNodes);
        slotTableBuilder.init(slotTable, NodeUtils.transferNodeToIpList(dataNodes));
        Assert.assertFalse(slotTableBuilder.hasNoAssignedSlots());
        DataNodeSlot dataNodeSlot = slotTableBuilder.getDataNodeSlot(dataNodes.get(0).getIp());
        MigrateSlotGroup expected = new MigrateSlotGroup();
        dataNodeSlot.getLeaders().forEach(expected::addLeader);
        dataNodeSlot.getFollowers().forEach(expected::addFollower);

        slotTableBuilder.removeDataServerSlots(dataNodes.get(0).getIp());
        Assert.assertTrue(slotTableBuilder.hasNoAssignedSlots());

        MigrateSlotGroup actual = slotTableBuilder.getNoAssignedSlots();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetDataNodeSlotsLeaderBeyond() {
        slotTableBuilder = new SlotTableBuilder(6, 2);
        List<DataNode> dataNodes = randomDataNodes(3);
        String leader = dataNodes.get(0).getIp();
        for(int i = 0; i < 6; i++) {
            slotTableBuilder.getOrCreate(i);
            slotTableBuilder.flipLeaderTo(i, leader);
        }
        Assert.assertEquals(Lists.newArrayList(slotTableBuilder.getDataNodeSlot(leader)),
                slotTableBuilder.getDataNodeSlotsLeaderBeyond(6/3));
    }

    @Test
    public void testGetDataNodeSlotsLeaderBelow() {
        slotTableBuilder = new SlotTableBuilder(6, 2);
        List<DataNode> dataNodes = randomDataNodes(3);
        slotTableBuilder.init(null, NodeUtils.transferNodeToIpList(dataNodes));
        String leader = dataNodes.get(0).getIp();
        for(int i = 0; i < 6; i++) {
            slotTableBuilder.getOrCreate(i);
            slotTableBuilder.flipLeaderTo(i, leader);
        }
        List<DataNodeSlot> expected = Lists.newArrayList(slotTableBuilder.getDataNodeSlot(dataNodes.get(1).getIp()),
                slotTableBuilder.getDataNodeSlot(dataNodes.get(2).getIp()));
        expected.sort(new Comparator<DataNodeSlot>() {
            @Override
            public int compare(DataNodeSlot o1, DataNodeSlot o2) {
                return o1.getDataNode().compareTo(o2.getDataNode());
            }
        });
        List<DataNodeSlot> actual = slotTableBuilder.getDataNodeSlotsLeaderBelow(6/3);
        actual.sort(new Comparator<DataNodeSlot>() {
            @Override
            public int compare(DataNodeSlot o1, DataNodeSlot o2) {
                return o1.getDataNode().compareTo(o2.getDataNode());
            }
        });
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetDataNodeTotalSlotsBeyond() {
        slotTableBuilder = new SlotTableBuilder(6, 2);
        List<DataNode> dataNodes = randomDataNodes(3);
        String leader = dataNodes.get(0).getIp();
        for(int i = 0; i < 6; i++) {
            slotTableBuilder.getOrCreate(i);
            slotTableBuilder.flipLeaderTo(i, leader);
            slotTableBuilder.addFollower(i, dataNodes.get(1).getIp());
        }
        List<DataNodeSlot> expected = Lists.newArrayList(slotTableBuilder.getDataNodeSlot(leader),
                slotTableBuilder.getDataNodeSlot(dataNodes.get(1).getIp()));
        expected.sort(new Comparator<DataNodeSlot>() {
            @Override
            public int compare(DataNodeSlot o1, DataNodeSlot o2) {
                return o1.getDataNode().compareTo(o2.getDataNode());
            }
        });
        List<DataNodeSlot> actual = slotTableBuilder.getDataNodeTotalSlotsBeyond(6 / 3);
        actual.sort(new Comparator<DataNodeSlot>() {
            @Override
            public int compare(DataNodeSlot o1, DataNodeSlot o2) {
                return o1.getDataNode().compareTo(o2.getDataNode());
            }
        });
        Assert.assertArrayEquals(expected.toArray(), actual.toArray());
    }

    @Test
    public void testGetDataNodeTotalSlotsBelow() {
        slotTableBuilder = new SlotTableBuilder(6, 2);
        List<DataNode> dataNodes = randomDataNodes(3);
        slotTableBuilder.init(null, NodeUtils.transferNodeToIpList(dataNodes));
        String leader = dataNodes.get(0).getIp();
        for(int i = 0; i < 6; i++) {
            slotTableBuilder.getOrCreate(i);
            slotTableBuilder.flipLeaderTo(i, leader);
            slotTableBuilder.addFollower(i, dataNodes.get(1).getIp());
        }
        Assert.assertEquals(Lists.newArrayList(slotTableBuilder.getDataNodeSlot(dataNodes.get(2).getIp())),
                slotTableBuilder.getDataNodeTotalSlotsBelow(6 / 3));
    }

    @Test
    public void testIncrEpoch() {
        List<DataNode> dataNodes = randomDataNodes(6);
        SlotTable slotTable = randomSlotTable(dataNodes);
        slotTableBuilder.init(slotTable, NodeUtils.transferNodeToIpList(dataNodes));
        long prevEpoch = slotTableBuilder.build().getEpoch();
        slotTableBuilder.incrEpoch();
        Assert.assertTrue(slotTableBuilder.build().getEpoch() > prevEpoch);
    }

    @Test
    public void testBuild() {
        List<DataNode> dataNodes = randomDataNodes(6);
        SlotTable slotTable = randomSlotTable(dataNodes);
        slotTableBuilder.init(slotTable, NodeUtils.transferNodeToIpList(dataNodes));
        long prevEpoch = slotTableBuilder.build().getEpoch();
        slotTableBuilder.incrEpoch();
        SlotTable slotTable1 = slotTableBuilder.build();
        assertSlotTableNoDupLeaderFollower(slotTable1);
    }

}