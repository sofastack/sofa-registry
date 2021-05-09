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
package com.alipay.sofa.registry.common.model.metaserver.nodes;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.ProcessId;
import com.alipay.sofa.registry.common.model.store.URL;
import org.junit.Assert;
import org.junit.Test;

public class NodeTest {
  private String dc = "testDc";
  private String region = "testRegion";
  private URL url1 = new URL("192.168.1.1", 6666);
  private URL url2 = new URL("192.168.1.2", 6666);

  @Test
  public void testDataNode() {
    DataNode dataNode1 = new DataNode(url1, dc);
    DataNode dataNode2 = new DataNode(url2, dc);
    DataNode dataNode3 = new DataNode(url1, dc);
    Assert.assertEquals(dataNode1, dataNode3);
    Assert.assertEquals(dataNode1.hashCode(), dataNode3.hashCode());
    Assert.assertEquals(dataNode1.toString(), dataNode3.toString());

    Assert.assertNotEquals(dataNode1, dataNode2);
    Assert.assertNotEquals(dataNode1.toString(), dataNode2.toString());

    Assert.assertEquals(dataNode1.getNodeType(), Node.NodeType.DATA);
    Assert.assertEquals(dataNode1.getDataCenter(), dc);
    dataNode1.setRegistrationTimestamp(100);
    Assert.assertEquals(100, dataNode1.getRegistrationTimestamp());
    Assert.assertNotEquals(dataNode1, dataNode3);
  }

  @Test
  public void testSessionNode() {
    ProcessId processId1 = new ProcessId("test", 1, 2, 3);
    ProcessId processId2 = new ProcessId("test1", 1, 2, 3);
    SessionNode node1 = new SessionNode(url1, region, processId1);
    SessionNode node2 = new SessionNode(url2, region, processId2);
    SessionNode node3 = new SessionNode(url1, region, processId2);
    Assert.assertEquals(node1, node3);
    Assert.assertEquals(node1.hashCode(), node3.hashCode());
    Assert.assertEquals(node1.toString(), node3.toString());

    Assert.assertNotEquals(node1, node2);
    Assert.assertNotEquals(node1.toString(), node2.toString());

    Assert.assertEquals(node1.getNodeType(), Node.NodeType.SESSION);
    Assert.assertEquals(node1.getDataCenter(), null);
    Assert.assertEquals(node1.getRegionId(), region);

    // processId not effect the equals

    Assert.assertEquals(node1.getProcessId(), processId1);
    Assert.assertEquals(node1, node3);
  }

  @Test
  public void testMetaNode() {
    MetaNode node1 = new MetaNode(url1, dc);
    MetaNode node2 = new MetaNode(url2, dc);
    MetaNode node3 = new MetaNode(url1, dc);
    Assert.assertEquals(node1, node3);
    Assert.assertEquals(node1.hashCode(), node3.hashCode());
    Assert.assertEquals(node1.toString(), node3.toString());

    Assert.assertNotEquals(node1, node2);
    Assert.assertNotEquals(node1.toString(), node2.toString());

    Assert.assertEquals(node1.getNodeType(), Node.NodeType.META);
    Assert.assertEquals(node1.getDataCenter(), dc);
  }
}
