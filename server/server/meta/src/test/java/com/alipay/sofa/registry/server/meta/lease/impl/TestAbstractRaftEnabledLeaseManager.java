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
package com.alipay.sofa.registry.server.meta.lease.impl;

import com.alipay.sofa.registry.common.model.metaserver.nodes.MetaNode;
import com.alipay.sofa.registry.exception.DisposeException;
import com.alipay.sofa.registry.exception.InitializeException;
import com.alipay.sofa.registry.exception.StartException;
import com.alipay.sofa.registry.exception.StopException;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.server.meta.AbstractTest;
import com.alipay.sofa.registry.server.meta.lease.Lease;
import com.alipay.sofa.registry.server.meta.remoting.RaftExchanger;
import com.alipay.sofa.registry.util.FileUtils;
import org.apache.commons.lang.reflect.FieldUtils;
import org.junit.*;
import org.rocksdb.DBOptions;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class TestAbstractRaftEnabledLeaseManager extends AbstractTest {

    private String                                    serviceId = "TEST-SERVICE-ID";

    private AbstractRaftEnabledLeaseManager<MetaNode> manager   = new AbstractRaftEnabledLeaseManager<MetaNode>() {

                                                                    @Override
                                                                    protected String getServiceId() {
                                                                        return serviceId;
                                                                    }
                                                                };

    private RaftExchanger                             raftExchanger;

    @Before
    public void beforeTestAbstractRaftEnabledLeaseManager() throws InitializeException,
                                                           StartException {
        raftExchanger = mock(RaftExchanger.class);
        manager.setScheduled(scheduled).setRaftExchanger(raftExchanger).setExecutors(executors);
        LifecycleHelper.initializeIfPossible(manager);
        LifecycleHelper.startIfPossible(manager);
        manager.setRaftLeaseManager(manager.new DefaultRaftLeaseManager<>());
    }

    @After
    public void afterTestAbstractRaftEnabledLeaseManager() throws StopException, DisposeException {
        LifecycleHelper.stopIfPossible(manager);
        LifecycleHelper.disposeIfPossible(manager);
    }

    @Test
    public void raftWholeProcess() throws Exception {
        manager = new AbstractRaftEnabledLeaseManager<MetaNode>() {

            @Override
            protected String getServiceId() {
                return serviceId;
            }
        };
        manager.setScheduled(scheduled).setExecutors(executors);
        RaftExchanger raftExchanger = startRaftExchanger();
        manager.setRaftExchanger(raftExchanger);
        LifecycleHelper.initializeIfPossible(manager);
        LifecycleHelper.startIfPossible(manager);
        int size = manager.getLeaseStore().size();

        MetaNode node = new MetaNode(randomURL(randomIp()), getDc());
        manager.renew(node, 10);
        waitConditionUntilTimeOut(()->manager.getLeaseStore().get(node.getIp()) != null, 1000);
        Assert.assertEquals(node, manager.getLeaseStore().get(node.getIp()).getRenewal());
        Assert.assertFalse(manager.getLeaseStore().get(node.getIp()).isExpired());
        Assert.assertTrue(manager.getLeaseStore().size() > size);
        manager.cancel(node);
        waitConditionUntilTimeOut(()->manager.getLeaseStore().size() == size, 1000);
        Assert.assertEquals(size, manager.getLeaseStore().size());
    }

    @Test
//    @Ignore
    public void manuallyTestRaftMechanism() throws Exception {
        manager = new AbstractRaftEnabledLeaseManager<MetaNode>() {

            @Override
            protected String getServiceId() {
                return serviceId;
            }
        };
        manager.setScheduled(scheduled).setExecutors(executors);
        RaftExchanger raftExchanger = startRaftExchanger();
        manager.setRaftExchanger(raftExchanger);
        LifecycleHelper.initializeIfPossible(manager);
        LifecycleHelper.startIfPossible(manager);

        int tasks = 1000;
        CyclicBarrier barrier = new CyclicBarrier(tasks / 10);
        CountDownLatch latch = new CountDownLatch(tasks);
        for(int i = 0; i < tasks; i++) {
            executors.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        barrier.await();
                    } catch (Exception ignore) {}
                    manager.renew(new MetaNode(randomURL(randomIp()), getDc()), 10);
                    latch.countDown();
                }
            });
        }
        latch.await();
        Assert.assertEquals(tasks, manager.getLeaseStore().size());
    }

    @Test
    public void testRegister() {
        manager.register(new MetaNode(randomURL(randomIp()), getDc()), 10);
        Assert.assertEquals(1, manager.getLeaseStore().size());
        Lease<MetaNode> lease = manager.getLeaseStore().entrySet().iterator().next().getValue();
        Assert.assertFalse(lease.isExpired());
    }

    @Test
    public void testCancel() {
        MetaNode node = new MetaNode(randomURL(randomIp()), getDc());
        manager.register(node, 10);
        Assert.assertEquals(1, manager.getLeaseStore().size());
        manager.cancel(node);
        Assert.assertEquals(0, manager.getLeaseStore().size());
    }

    @Test
    public void testRenew() throws InterruptedException {
        MetaNode node = new MetaNode(randomURL(randomIp()), getDc());
        manager.renew(node, 1);
        Assert.assertEquals(1, manager.getLeaseStore().size());
        Lease<MetaNode> lease = manager.getLeaseStore().get(node.getIp());
        long prevLastUpdateTime = lease.getLastUpdateTimestamp();
        long prevBeginTime = lease.getBeginTimestamp();
        // let time pass, so last update time could be diff
        Thread.sleep(5);
        manager.renew(node, 10);
        lease = manager.getLeaseStore().get(node.getIp());
        Assert.assertEquals(prevBeginTime, lease.getBeginTimestamp());
        Assert.assertNotEquals(prevLastUpdateTime, lease.getLastUpdateTimestamp());
    }

    @Test
    public void testEvict() {
        manager.evict();
    }

    @Test
    public void testGetServiceId() {
        Assert.assertEquals(serviceId, manager.getServiceId());
    }
}