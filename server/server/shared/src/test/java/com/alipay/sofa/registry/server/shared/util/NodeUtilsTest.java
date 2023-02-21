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
package com.alipay.sofa.registry.server.shared.util;

import com.alipay.sofa.registry.common.model.metaserver.nodes.SessionNode;
import com.alipay.sofa.registry.common.model.store.URL;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class NodeUtilsTest {
  @Test
  public void test() {
    Assert.assertTrue(NodeUtils.transferNodeToIpList(Collections.EMPTY_LIST).isEmpty());
    SessionNode node1 = new SessionNode(new URL("xx", 12), "test", null);
    SessionNode node2 = new SessionNode(new URL("xyz", 34), "test", null);
    List<String> list = NodeUtils.transferNodeToIpList(Lists.newArrayList(node1, node2));
    Assert.assertEquals(list.get(0), "xx");
    Assert.assertEquals(list.get(1), "xyz");
  }
}
