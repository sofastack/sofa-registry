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
package com.alipay.sofa.registry.common.model.metaserver.rpc;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.ProcessId;
import com.alipay.sofa.registry.common.model.metaserver.DataCenterNodes;
import com.alipay.sofa.registry.common.model.metaserver.nodes.SessionNode;
import com.alipay.sofa.registry.common.model.store.URL;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;

public class DataCenterNodesTest {
  @Test
  public void test() {
    ProcessId processId1 = new ProcessId("test", 1, 2, 3);
    final String dataId = "testDataId";
    DataCenterNodes request = new DataCenterNodes(Node.NodeType.CLIENT, 10, dataId);
    SessionNode sessionNode = new SessionNode(new URL("192.168.1.1", 8888), "testZone", processId1);
    request.setNodes(Collections.singletonMap("testKey", sessionNode));
    Assert.assertEquals(request.getDataCenterId(), dataId);
    Assert.assertEquals(request.getVersion(), 10);

    Assert.assertEquals(request.getNodes().size(), 1);
    Assert.assertEquals(request.getNodes().get("testKey"), sessionNode);

    Assert.assertTrue(request.toString(), request.toString().contains(dataId));
  }
}
