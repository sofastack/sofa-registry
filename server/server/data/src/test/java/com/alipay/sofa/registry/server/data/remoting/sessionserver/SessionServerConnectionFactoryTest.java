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
package com.alipay.sofa.registry.server.data.remoting.sessionserver;

import com.alipay.sofa.registry.common.model.ProcessId;
import com.alipay.sofa.registry.remoting.bolt.BoltChannel;
import com.alipay.sofa.registry.server.data.TestBaseUtils;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import org.junit.Assert;
import org.junit.Test;

public class SessionServerConnectionFactoryTest {

  @Test
  public void testIllegal() {
    SessionServerConnectionFactory factory = new SessionServerConnectionFactory();
    Assert.assertFalse(factory.registerSession(ServerEnv.PROCESS_ID, null));
    Assert.assertFalse(factory.registerSession(ServerEnv.PROCESS_ID, new BoltChannel()));
    TestBaseUtils.MockBlotChannel channel1 =
        new TestBaseUtils.MockBlotChannel(9602, "66.66.66.66", 5550);
    channel1.connected = false;
    Assert.assertFalse(factory.registerSession(ServerEnv.PROCESS_ID, channel1));
  }

  @Test
  public void testConnection() throws Exception {
    SessionServerConnectionFactory factory = new SessionServerConnectionFactory();
    final String IP1 = "66.66.66.66";
    final String IP2 = "66.66.66.65";

    TestBaseUtils.MockBlotChannel channel1 = new TestBaseUtils.MockBlotChannel(9602, IP1, 5550);
    Assert.assertTrue(factory.registerSession(ServerEnv.PROCESS_ID, channel1));
    Assert.assertFalse(factory.registerSession(ServerEnv.PROCESS_ID, channel1));

    Assert.assertTrue(factory.containsConnection(ServerEnv.PROCESS_ID));
    Assert.assertEquals(1, factory.getSessionConnections().size());
    Assert.assertEquals(1, factory.getProcessIds().size());
    Assert.assertTrue(factory.getSessionConnectionMap().containsKey(IP1));

    TestBaseUtils.MockBlotChannel channel2 = new TestBaseUtils.MockBlotChannel(9602, IP1, 5551);
    factory.registerSession(ServerEnv.PROCESS_ID, channel2);

    Assert.assertTrue(factory.containsConnection(ServerEnv.PROCESS_ID));
    Assert.assertEquals(1, factory.getSessionConnections().size());
    Assert.assertEquals(1, factory.getProcessIds().size());
    Assert.assertTrue(factory.getSessionConnectionMap().containsKey(IP1));

    ProcessId processId2 = new ProcessId("unknown", System.currentTimeMillis(), 100, 100);
    TestBaseUtils.MockBlotChannel channel3 = new TestBaseUtils.MockBlotChannel(9602, IP2, 5551);
    factory.registerSession(processId2, channel3);

    Assert.assertTrue(factory.containsConnection(ServerEnv.PROCESS_ID));
    Assert.assertTrue(factory.containsConnection(processId2));
    Assert.assertEquals(2, factory.getProcessIds().size());
    Assert.assertTrue(factory.getSessionConnectionMap().containsKey(IP1));
    Assert.assertTrue(factory.getSessionConnectionMap().containsKey(IP2));

    channel1.connected = false;
    factory.sessionDisconnected(channel1);

    Assert.assertTrue(factory.containsConnection(ServerEnv.PROCESS_ID));
    Assert.assertTrue(factory.containsConnection(processId2));
    Assert.assertEquals(2, factory.getProcessIds().size());
    Assert.assertTrue(factory.getSessionConnectionMap().containsKey(IP1));
    Assert.assertTrue(factory.getSessionConnectionMap().containsKey(IP2));
    Assert.assertEquals(1, factory.getAllSessionConnections().get(IP1).size());
    Assert.assertEquals(1, factory.getAllSessionConnections().get(IP2).size());

    factory.sessionDisconnected(channel3);

    Assert.assertTrue(factory.containsConnection(ServerEnv.PROCESS_ID));
    Assert.assertTrue(!factory.containsConnection(processId2));
    Assert.assertEquals(1, factory.getProcessIds().size());
    Assert.assertTrue(factory.getSessionConnectionMap().containsKey(IP1));
    Assert.assertTrue(!factory.getSessionConnectionMap().containsKey(IP2));
    Assert.assertEquals(1, factory.getAllSessionConnections().get(IP1).size());
    Assert.assertEquals(0, factory.getAllSessionConnections().get(IP2).size());
  }
}
