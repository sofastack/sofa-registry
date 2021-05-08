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
package com.alipay.sofa.registry.server.meta.remoting.notifier;

import static org.mockito.Mockito.*;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.remoting.exchange.NodeExchanger;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.remoting.connection.NodeConnectManager;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AbstractNotifierTest extends AbstractMetaServerTestBase {

  @Mock private NodeExchanger exchanger;

  private AbstractNotifier notifier;

  @Mock private NodeConnectManager nodeConnectManager;

  @Before
  public void beforeAbstractNotifierTest() {
    MockitoAnnotations.initMocks(this);
    List<DataNode> nodes = randomDataNodes(2);
    when(nodeConnectManager.getConnections(any()))
        .thenReturn(
            Lists.newArrayList(
                new InetSocketAddress(nodes.get(0).getIp(), 8080),
                new InetSocketAddress(nodes.get(1).getIp(), 8080)));
    notifier =
        new AbstractNotifier<DataNode>() {
          @Override
          protected NodeExchanger getNodeExchanger() {
            return exchanger;
          }

          @Override
          protected List<DataNode> getNodes() {
            return nodes;
          }

          @Override
          protected NodeConnectManager getNodeConnectManager() {
            return nodeConnectManager;
          }
        };
    notifier.setMetaLeaderService(metaLeaderService);
  }

  @Test
  public void testNotifySlotTableChange() throws InterruptedException, TimeoutException {
    makeMetaNonLeader();
    notifier.notifySlotTableChange(randomSlotTable());
    verify(exchanger, never()).request(any());
    makeMetaLeader();
    notifier.notifySlotTableChange(randomSlotTable());
    Thread.sleep(100);
    verify(exchanger, atLeast(1)).request(any());
  }
}
