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
package com.alipay.sofa.registry.server.meta.metaserver.impl;

import static org.mockito.Mockito.*;

import com.alipay.sofa.registry.common.model.metaserver.cluster.VersionedList;
import com.alipay.sofa.registry.common.model.metaserver.nodes.MetaNode;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.lease.data.DataServerManager;
import com.alipay.sofa.registry.server.meta.lease.session.SessionServerManager;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.alipay.sofa.registry.server.meta.slot.manager.SimpleSlotManager;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class LocalMetaServerTest extends AbstractMetaServerTestBase {

  private LocalMetaServer metaServer;

  private SlotManager slotManager;

  @Mock private DataServerManager dataServerManager;

  @Mock private SessionServerManager sessionServerManager;

  @Before
  public void beforeDefaultLocalMetaServerTest() throws Exception {
    MockitoAnnotations.initMocks(this);
    slotManager = spy(new SimpleSlotManager());
    metaServer = spy(new LocalMetaServer(slotManager, dataServerManager, sessionServerManager));
    LifecycleHelper.initializeIfPossible(metaServer);
    LifecycleHelper.startIfPossible(metaServer);
  }

  @After
  public void afterDefaultLocalMetaServerTest() throws Exception {
    LifecycleHelper.stopIfPossible(metaServer);
    LifecycleHelper.disposeIfPossible(metaServer);
  }

  @Test
  public void testGetSlotTable() {
    when(slotManager.getSlotTable()).thenReturn(new SlotTable(0L, Collections.emptyList()));
    metaServer.getSlotTable();
    verify(slotManager, times(1)).getSlotTable();
  }

  @Test
  public void testGetClusterMembers() throws TimeoutException, InterruptedException {
    List<MetaNode> metaNodeList =
        Lists.newArrayList(
            new MetaNode(randomURL(randomIp()), getDc()),
            new MetaNode(randomURL(randomIp()), getDc()),
            new MetaNode(randomURL(randomIp()), getDc()));
    metaServer.updateClusterMembers(new VersionedList<>(DatumVersionUtil.nextId(), metaNodeList));
    waitConditionUntilTimeOut(() -> !metaServer.getClusterMembers().isEmpty(), 100);
    metaNodeList.sort(new NodeComparator());
    List<MetaNode> actual = metaServer.getClusterMembers();
    actual.sort(new NodeComparator());
    Assert.assertEquals(metaNodeList, actual);
  }

  @Test
  public void testUpdateClusterMembers() throws InterruptedException {
    int tasks = 1000;
    CyclicBarrier barrier = new CyclicBarrier(tasks);
    CountDownLatch latch = new CountDownLatch(tasks);
    for (int i = 0; i < tasks; i++) {
      executors.execute(
          new Runnable() {
            @Override
            public void run() {
              try {
                barrier.await();
              } catch (Exception ignore) {
              }

              metaServer.updateClusterMembers(
                  new VersionedList<>(
                      DatumVersionUtil.nextId(),
                      Lists.newArrayList(
                          new MetaNode(randomURL(), getDc()),
                          new MetaNode(randomURL(), getDc()),
                          new MetaNode(randomURL(), getDc()))));
              latch.countDown();
            }
          });
    }
    latch.await(2, TimeUnit.SECONDS);
    verify(metaServer, times(tasks)).updateClusterMembers(any());
  }

  @Test
  public void testRenew() throws InterruptedException {
    int tasks = 1000;
    CyclicBarrier barrier = new CyclicBarrier(tasks);
    CountDownLatch latch = new CountDownLatch(tasks);
    for (int i = 0; i < tasks; i++) {
      executors.execute(
          new Runnable() {
            @Override
            public void run() {
              try {
                barrier.await();
              } catch (Exception ignore) {
              }
              metaServer.renew(new MetaNode(randomURL(), getDc()));
              latch.countDown();
            }
          });
    }
    latch.await(2, TimeUnit.SECONDS);
    verify(metaServer, times(tasks)).renew(any());
  }

  @Test
  public void testCancel() throws InterruptedException {
    int tasks = 1000;
    CyclicBarrier barrier = new CyclicBarrier(tasks);
    CountDownLatch latch = new CountDownLatch(tasks);
    for (int i = 0; i < tasks; i++) {
      executors.execute(
          new Runnable() {
            @Override
            public void run() {
              try {
                barrier.await();
              } catch (Exception ignore) {
              }
              metaServer.renew(new MetaNode(randomURL(), getDc()));
              latch.countDown();
            }
          });
    }
    latch.await(2, TimeUnit.SECONDS);
    verify(metaServer, times(tasks)).renew(any());

    CyclicBarrier barrier2 = new CyclicBarrier(tasks);
    CountDownLatch latch2 = new CountDownLatch(tasks);
    for (int i = 0; i < tasks; i++) {
      executors.execute(
          new Runnable() {
            @Override
            public void run() {
              try {
                barrier2.await();
              } catch (Exception ignore) {
              }
              metaServer.cancel(new MetaNode(randomURL(), getDc()));
              latch2.countDown();
            }
          });
    }
    latch2.await(2, TimeUnit.SECONDS);
    Thread.sleep(50);
    verify(metaServer, times(tasks)).cancel(any());
  }
}
