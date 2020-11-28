package com.alipay.sofa.registry.server.meta.metaserver.impl;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.metaserver.DataCenterNodes;
import com.alipay.sofa.registry.common.model.metaserver.nodes.MetaNode;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.jraft.bootstrap.ServiceStateMachine;
import com.alipay.sofa.registry.lifecycle.Lifecycle;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.meta.AbstractTest;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.remoting.RaftExchanger;
import com.alipay.sofa.registry.server.meta.slot.SlotAllocator;
import com.alipay.sofa.registry.server.meta.slot.impl.CrossDcSlotAllocator;
import com.google.common.collect.ImmutableMap;
import org.assertj.core.util.Lists;
import org.junit.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;
import java.util.concurrent.TimeoutException;

import static org.mockito.Mockito.*;

public class DefaultCrossDcMetaServerTest extends AbstractTest {

    private DefaultCrossDcMetaServer server;

    @Mock
    private Exchange<MetaNode> exchange;

    @Mock
    private RaftExchanger raftExchanger;

    @Mock
    private MetaServerConfig metaServerConfig;

    private ServiceStateMachine machine;

    @Before
    public void beforeDefaultCrossDcMetaServerTest() {
        MockitoAnnotations.initMocks(this);
        when(metaServerConfig.getCrossDcMetaSyncIntervalMilli()).thenReturn(60 * 1000);
        Collection<String> collection = Lists.newArrayList("10.0.0.1", "10.0.0.2");
        machine = spy(ServiceStateMachine.getInstance());
        server = spy(new DefaultCrossDcMetaServer(getDc(), collection, scheduled, exchange, raftExchanger, metaServerConfig));
    }

    @After
    public void afterDefaultCrossDcMetaServerTest() throws Exception {
        LifecycleHelper.startIfPossible(server);
        LifecycleHelper.disposeIfPossible(server);
    }

    @Test
    public void testDoInitialize() throws Exception {
        LifecycleHelper.initializeIfPossible(server);
        Assert.assertTrue(server.getLifecycleState().canStart());
    }

    @Test
    public void testDoStart() throws Exception {
        LifecycleHelper.initializeIfPossible(server);
        LifecycleHelper.startIfPossible(server);
        Assert.assertTrue(server.getLifecycleState().canStop());
    }

    @Test
    public void testDoStop() throws Exception {
        LifecycleHelper.initializeIfPossible(server);
        LifecycleHelper.startIfPossible(server);
        LifecycleHelper.stopIfPossible(server);
        Assert.assertTrue(server.getLifecycleState().canStart());
    }

    @Test
    public void testDoDispose() throws Exception {
        LifecycleHelper.disposeIfPossible(server);
        Assert.assertTrue(server.getLifecycleState().canInitialize());
    }

    @Test
    public void testTestGetDc() {
        Assert.assertEquals(getDc(), server.getDc());
    }

    @Test(expected = IllegalStateException.class)
    public void testGetSlotTable() {
        server.getSlotTable();
    }

    @Test
    public void testRefreshSlotTableWithResult() throws Exception {
        LifecycleHelper.initializeIfPossible(server);
        LifecycleHelper.startIfPossible(server);
        Assert.assertEquals(2, server.getClusterMembers().size());
        DataCenterNodes<MetaNode> message = new DataCenterNodes<MetaNode>(Node.NodeType.META, System.currentTimeMillis(), getDc());
        Map<String,MetaNode> nodes = ImmutableMap.of("10.0.0.1",new MetaNode(randomURL("10.0.0.1"), getDc()),
                "10.0.0.2",new MetaNode(randomURL("10.0.0.2"), getDc()),
                "10.0.0.3",new MetaNode(randomURL("10.0.0.3"), getDc()));
        message.setNodes(nodes);
        when(exchange.getClient(Exchange.META_SERVER_TYPE)).thenReturn(getRpcClient(scheduled, 1, message));
        server.setRaftStorage(server.new RaftMetaServerListStorage());
        server.doRefresh(0);
        waitConditionUntilTimeOut(()->server.getClusterMembers().size() > 2, 1000);
        Assert.assertEquals(Lists.newArrayList(message.getNodes().values()), server.getClusterMembers());
    }

    @Test
    public void testDoRefreshWithFirstTimeout() throws Exception {
        LifecycleHelper.initializeIfPossible(server);
        LifecycleHelper.startIfPossible(server);
        Assert.assertEquals(2, server.getClusterMembers().size());
        DataCenterNodes<MetaNode> message = new DataCenterNodes<MetaNode>(Node.NodeType.META, System.currentTimeMillis(), getDc());
        Map<String,MetaNode> nodes = ImmutableMap.of("10.0.0.1",new MetaNode(randomURL("10.0.0.1"), getDc()),
                "10.0.0.2",new MetaNode(randomURL("10.0.0.2"), getDc()),
                "10.0.0.3",new MetaNode(randomURL("10.0.0.3"), getDc()));
        message.setNodes(nodes);
        when(exchange.getClient(Exchange.META_SERVER_TYPE))
                .thenReturn(getRpcClient(scheduled, 3, new TimeoutException()))
                .thenReturn(getRpcClient(scheduled, 1, message));
        server.setRaftStorage(server.new RaftMetaServerListStorage());
        server.doRefresh(0);
        waitConditionUntilTimeOut(()->server.getClusterMembers().size() > 2, 1000);
        Assert.assertEquals(Lists.newArrayList(message.getNodes().values()), server.getClusterMembers());
    }

    @Test
    public void testDoRefreshWithOverRetryTimes() throws Exception {
        LifecycleHelper.initializeIfPossible(server);
        LifecycleHelper.startIfPossible(server);
        Assert.assertEquals(2, server.getClusterMembers().size());

        when(exchange.getClient(Exchange.META_SERVER_TYPE))
                .thenReturn(getRpcClient(scheduled, 1, new TimeoutException()));
        server.setRaftStorage(server.new RaftMetaServerListStorage());
        server.doRefresh(0);
        Thread.sleep(20);
        verify(server, times(4)).doRefresh(anyInt());
    }

    //run manually
    @Test
    @Ignore
    public void testRaftMechanismWorks() throws Exception {
        RaftExchanger raftExchanger = startRaftExchanger();

        server = new DefaultCrossDcMetaServer(getDc(), Lists.newArrayList(NetUtil.getLocalAddress().getHostAddress()),
                scheduled, exchange, raftExchanger, metaServerConfig);
        DataCenterNodes<MetaNode> message = new DataCenterNodes<MetaNode>(Node.NodeType.META, System.currentTimeMillis(), getDc());
        Map<String,MetaNode> nodes = ImmutableMap.of(
                "10.0.0.1", new MetaNode(randomURL("10.0.0.1"), getDc()),
                "10.0.0.2", new MetaNode(randomURL("10.0.0.2"), getDc()),
                "10.0.0.3", new MetaNode(randomURL("10.0.0.3"), getDc()),
                "10.0.0.4", new MetaNode(randomURL("10.0.0.4"), getDc()),
                "10.0.0.5", new MetaNode(randomURL("10.0.0.5"), getDc())
                );
        message.setNodes(nodes);
        when(exchange.getClient(Exchange.META_SERVER_TYPE))
//                .thenReturn(getRpcClient(scheduled, 3, new TimeoutException()))
                .thenReturn(getRpcClient(scheduled, 1, message));
        LifecycleHelper.initializeIfPossible(server);
        LifecycleHelper.startIfPossible(server);

        server.doRefresh(0);
        waitConditionUntilTimeOut(()->server.getClusterMembers().size() > 4, 30000);
        List<MetaNode> expected = Lists.newArrayList(message.getNodes().values());
        Collections.sort(expected, new Comparator<MetaNode>() {
            @Override
            public int compare(MetaNode o1, MetaNode o2) {
                return o1.getIp().compareTo(o2.getIp());
            }
        });
        List<MetaNode> real = server.getClusterMembers();
        Collections.sort(real, new Comparator<MetaNode>() {
            @Override
            public int compare(MetaNode o1, MetaNode o2) {
                return o1.getIp().compareTo(o2.getIp());
            }
        });
        Assert.assertEquals(expected.size(), real.size());
        // wait for rpc safe quit
        Thread.sleep(100);
    }

    @Test
    public void testRefresh() {
        server.refresh();
        verify(server, never()).doRefresh(anyInt());
    }

    @Test
    public void testGetRefreshCount() throws Exception {
        LifecycleHelper.initializeIfPossible(server);
        LifecycleHelper.startIfPossible(server);
        when(machine.isLeader()).thenReturn(false);
        when(exchange.getClient(Exchange.META_SERVER_TYPE))
                .thenReturn(getRpcClient(scheduled, 1, new TimeoutException()));
        server.refresh();
        server.refresh();
        server.refresh();
        Assert.assertEquals(3, server.getRefreshCount());
    }

    @Test
    public void testGetLastRefreshTime() {
    }

    @Test
    public void testGetSlotTable2() throws Exception {
        LifecycleHelper.initializeIfPossible(server);
        LifecycleHelper.startIfPossible(server);
        SlotAllocator allocator = mock(SlotAllocator.class);
        server.setRaftStorage(server.new RaftMetaServerListStorage())
                .setAllocator(allocator);
        server.getSlotTable();
        verify(allocator, times(1)).getSlotTable();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRaftMetaServerListStorage() {
        DefaultCrossDcMetaServer.RaftMetaServerListStorage storage = server.new RaftMetaServerListStorage();
        storage.tryUpdateRemoteDcMetaServerList(new DataCenterNodes<>(Node.NodeType.META, System.currentTimeMillis(),
                "unknown"));
    }

    @Test
    public void testRaftMetaServerListStorageWithLowerEpoch() {
        DefaultCrossDcMetaServer.RaftMetaServerListStorage storage = server.new RaftMetaServerListStorage();
        storage.tryUpdateRemoteDcMetaServerList(new DataCenterNodes<>(Node.NodeType.META, 20L,
                getDc()));
        storage.tryUpdateRemoteDcMetaServerList(new DataCenterNodes<>(Node.NodeType.META, 1L,
                getDc()));
        Assert.assertEquals(20L, server.getEpoch());
    }

}