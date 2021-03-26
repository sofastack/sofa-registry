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

import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;

public class TestAbstractRaftEnabledLeaseManager extends AbstractMetaServerTestBase {

  private String snapshotFile = "TEST-SERVICE-ID";

  // private final AbstractCachablePersistenceLeaseManager<MetaNode>
  // localLeaseManager = spy(new AbstractCachablePersistenceLeaseManager<>(
  // snapshotFile));
  //
  // private final AtomicReference<LeaseManager<MetaNode>> raftLeaseManager =
  // new AtomicReference<>();
  //
  // private final AtomicInteger evitTimeMilli = new AtomicInteger(
  // 1000 * 60);
  //
  // private final AtomicBoolean leaderNotify = new AtomicBoolean(
  // true);
  //
  // private AbstractRaftEnabledLeaseManager<MetaNode> manager = new
  // AbstractRaftEnabledLeaseManager<MetaNode>() {
  // @Override
  // protected long getIntervalMilli() {
  // return evitTimeMilli
  // .get();
  // }
  //
  // @Override
  // protected AbstractCachablePersistenceLeaseManager<MetaNode>
  // getLocalLeaseManager() {
  // return localLeaseManager;
  // }
  //
  // @Override
  // protected LeaseManager<MetaNode> getRaftLeaseManager() {
  // return raftLeaseManager
  // .get();
  // }
  //
  // @Override
  // protected long getEvictBetweenMilli() {
  // return evitTimeMilli
  // .get();
  // }
  //
  // @Override
  // protected boolean isRaftLeader() {
  // return leaderNotify
  // .get();
  // }
  // };
  //
  // private RaftExchanger raftExchanger;
  //
  // @Before
  // public void beforeTestAbstractRaftEnabledLeaseManager() throws
  // InitializeException,
  // StartException {
  // raftExchanger = mock(RaftExchanger.class);
  // raftLeaseManager.set((LeaseManager<MetaNode>)
  // Proxy.newProxyInstance(Thread.currentThread()
  // .getContextClassLoader(), new Class[] { LeaseManager.class }, new
  // InvocationHandler() {
  // @Override
  // public Object invoke(Object proxy, Method method, Object[] args) throws
  // Throwable {
  // return method.invoke(localLeaseManager, args);
  // }
  // }));
  // manager.setScheduled(scheduled).setExecutors(executors);
  // LifecycleHelper.initializeIfPossible(manager);
  // LifecycleHelper.startIfPossible(manager);
  // }
  //
  // @After
  // public void afterTestAbstractRaftEnabledLeaseManager() throws
  // StopException, DisposeException {
  // LifecycleHelper.stopIfPossible(manager);
  // LifecycleHelper.disposeIfPossible(manager);
  // }
  //
  // @Test
  // public void testRunRaftProxy() throws Exception {
  // leaderNotify.set(false);
  // int size = manager.getClusterMembers().size();
  // MetaNode node = new MetaNode(randomURL(randomIp()), getDc());
  // manager.renew(node, 10);
  // waitConditionUntilTimeOut(()->manager.getLease(node) != null, 1000);
  // Assert.assertEquals(node, manager.getLease(node).getRenewal());
  // Assert.assertFalse(manager.getLease(node).isExpired());
  // Assert.assertTrue(manager.getClusterMembers().size() > size);
  // manager.cancel(manager.getLease(node).prepareCancel());
  // waitConditionUntilTimeOut(()->manager.getClusterMembers().size() == size,
  // 1000);
  // Assert.assertEquals(size, manager.getClusterMembers().size());
  // }
  //
  // @Test
  // public void testRaftBatchInsert() throws Exception {
  // leaderNotify.set(false);
  // int tasks = 100;
  // CyclicBarrier barrier = new CyclicBarrier(tasks / 10);
  // CountDownLatch latch = new CountDownLatch(tasks);
  // for (int i = 0; i < tasks; i++) {
  // executors.execute(new Runnable() {
  // @Override
  // public void run() {
  // try {
  // barrier.await();
  // } catch (Exception ignore) {
  // }
  // manager.renew(new MetaNode(randomURL(randomIp()), getDc()), 10);
  // latch.countDown();
  // }
  // });
  // }
  // logger.info("[count] {}", latch.getCount());
  // TimeUnit.MILLISECONDS.sleep(1000);
  // logger.info("[count] {}", latch.getCount());
  // latch.await(3, TimeUnit.SECONDS);
  // Assert.assertEquals(tasks, manager.getClusterMembers().size());
  // }
  //
  // @Test
  // public void testRegister() {
  // manager.renew(new MetaNode(randomURL(randomIp()), getDc()), 10);
  // Assert.assertEquals(1, manager.getClusterMembers().size());
  // Lease<MetaNode> lease =
  // manager.getLease(manager.getClusterMembers().get(0));
  // Assert.assertFalse(lease.isExpired());
  // }
  //
  // @Test(expected = UnsupportedOperationException.class)
  // public void testDirectRegister() {
  // manager.register(new Lease<MetaNode>(new MetaNode(randomURL(), getDc()),
  // 1));
  // }
  //
  // @Test
  // public void testGetEpoch() throws TimeoutException, InterruptedException
  // {
  // raftLeaseManager.set(spy(new
  // AbstractCachablePersistenceLeaseManager<>(snapshotFile)));
  // leaderNotify.set(true);
  // manager.getEpoch();
  // verify(raftLeaseManager.get(), never()).getEpoch();
  //
  // leaderNotify.set(false);
  // manager.getEpoch();
  // verify(raftLeaseManager.get(), times(1)).getEpoch();
  // }
  //
  // @Test
  // public void testCancel() {
  // MetaNode node = new MetaNode(randomURL(randomIp()), getDc());
  // manager.renew(node, 10);
  // Assert.assertEquals(1, manager.getClusterMembers().size());
  // manager.cancel(manager.getLease(node).prepareCancel());
  // Assert.assertEquals(0, manager.getClusterMembers().size());
  // }
  //
  // @Test
  // public void testRenew() throws InterruptedException {
  // MetaNode node = new MetaNode(randomURL(randomIp()), getDc());
  // manager.renew(node, 1);
  // Assert.assertEquals(1, manager.getClusterMembers().size());
  // Lease<MetaNode> lease = manager.getLease(node);
  // long prevLastUpdateTime = lease.getLastUpdateTimestamp();
  // long prevBeginTime = lease.getBeginTimestamp();
  // // let time pass, so last update time could be diff
  // Thread.sleep(5);
  // manager.renew(node, 10);
  // lease = manager.getLease(node);
  // Assert.assertEquals(prevBeginTime, lease.getBeginTimestamp());
  // Assert.assertNotEquals(prevLastUpdateTime,
  // lease.getLastUpdateTimestamp());
  // }
  //
  // @Test
  // public void testEvict() throws InterruptedException {
  // MetaNode node = new MetaNode(randomURL(randomIp()), getDc());
  // manager.renew(node, 1);
  // Assert.assertEquals(1, manager.getClusterMembers().size());
  // TimeUnit.SECONDS.sleep(1);
  // TimeUnit.MILLISECONDS.sleep(100);
  // manager.evict();
  // Assert.assertEquals(0, manager.getClusterMembers().size());
  // }
  //
  // @Test
  // public void testEvictTooQuick() throws InterruptedException {
  // manager = spy(manager);
  // evitTimeMilli.set(3000);
  // MetaNode node = new MetaNode(randomURL(randomIp()), getDc());
  // manager.renew(node, 1);
  // Assert.assertEquals(1, manager.getClusterMembers().size());
  // manager.evict();
  // manager.evict();
  // manager.evict();
  // verify(localLeaseManager, times(1)).getExpiredLeases();
  // }
  //
  // @Test
  // public void testEvitTooQuickThreadSafe() throws InterruptedException {
  // manager = spy(manager);
  // evitTimeMilli.set(1000);
  // MetaNode node = new MetaNode(randomURL(randomIp()), getDc());
  // manager.renew(node, 1);
  // Assert.assertEquals(1, manager.getClusterMembers().size());
  // int tasks = 100;
  // CyclicBarrier barrier = new CyclicBarrier(tasks);
  // CountDownLatch latch = new CountDownLatch(tasks);
  // for (int i = 0; i < tasks; i++) {
  // executors.execute(new Runnable() {
  // @Override
  // public void run() {
  // try {
  // barrier.await();
  // } catch (Exception ignore) {
  // }
  // manager.evict();
  // latch.countDown();
  // }
  // });
  // }
  // latch.await();
  // verify(localLeaseManager, times(1)).getExpiredLeases();
  // }
  //
  // @Test
  // public void testEvictConcurrentModificateWithRenew() {
  // MetaNode node = new MetaNode(randomURL(randomIp()), getDc());
  // manager.renew(node, 10);
  // verify(localLeaseManager, times(1)).getLease(node);
  //
  // Assert.assertEquals(1, manager.getClusterMembers().size());
  // when(localLeaseManager.getExpiredLeases()).thenReturn(
  // Lists.newArrayList(new Lease<MetaNode>(node, 1000)));
  // manager.evict();
  // verify(localLeaseManager, times(2)).getLease(node);
  // Assert.assertEquals(1, manager.getClusterMembers().size());
  // }

}
