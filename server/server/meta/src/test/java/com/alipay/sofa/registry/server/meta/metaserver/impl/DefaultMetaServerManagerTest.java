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

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.metaserver.cluster.VersionedList;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.lease.data.DataServerManager;
import com.alipay.sofa.registry.server.meta.lease.session.SessionServerManager;
import com.alipay.sofa.registry.server.meta.metaserver.CurrentDcMetaServer;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DefaultMetaServerManagerTest extends AbstractMetaServerTestBase {

  @Mock private CurrentDcMetaServer currentDcMetaServer;

  @Mock private SessionServerManager sessionServerManager;

  @Mock private DataServerManager dataServerManager;

  @Mock private NodeConfig nodeConfig;

  private DefaultMetaServerManager manager;

  @Before
  public void beforeDefaultMetaServerManagerTest() {
    MockitoAnnotations.initMocks(this);
    manager = new DefaultMetaServerManager();
    manager
        .setCurrentDcMetaServer(currentDcMetaServer)
        .setSessionManager(sessionServerManager)
        .setDataServerManager(dataServerManager)
        .setNodeConfig(nodeConfig);
  }

  @Test
  public void testGetSummary() {
    when(dataServerManager.getDataServerMetaInfo())
        .thenReturn(new VersionedList<>(DatumVersionUtil.nextId(), Lists.newArrayList()));
    manager.getSummary(Node.NodeType.DATA);
    verify(dataServerManager, times(1)).getDataServerMetaInfo();
    verify(sessionServerManager, never()).getSessionServerMetaInfo();
  }

  @Test
  public void testGetSummary2() {
    when(sessionServerManager.getSessionServerMetaInfo())
        .thenReturn(new VersionedList<>(DatumVersionUtil.nextId(), Lists.newArrayList()));
    manager.getSummary(Node.NodeType.SESSION);
    //        verify(sessionManager, times(1)).getClusterMembers();
    verify(dataServerManager, never()).getDataServerMetaInfo();
  }

  @Test
  public void testGetSummary3() {
    manager.getSummary(Node.NodeType.META);
    verify(currentDcMetaServer, times(1)).getClusterMembers();
    verify(sessionServerManager, never()).getSessionServerMetaInfo();
  }
}
