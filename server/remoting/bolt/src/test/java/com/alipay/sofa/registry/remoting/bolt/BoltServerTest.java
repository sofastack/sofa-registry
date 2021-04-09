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
package com.alipay.sofa.registry.remoting.bolt;

import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.remoting.Channel;
import java.net.InetSocketAddress;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author shangyu.wh
 * @version $Id: BoltServerTest.java, v 0.1 2018-05-14 19:34 shangyu.wh Exp $
 */
public class BoltServerTest {
  private static URL url = new URL("0.0.0.0", 12345);
  private static BoltServer server = new BoltServer(url, null);

  @BeforeClass
  public static void before() {
    server.startServer();
  }

  @AfterClass
  public static void after() {
    server.close();
  }

  @Test
  public void testChannel() throws Exception {
    BoltClient client1 = new BoltClient(2);
    client1.connect(url);

    BoltClient client2 = new BoltClient(3);
    client2.connect(url);
    Thread.sleep(1000);
    Assert.assertEquals(5, server.getChannelCount());
    Assert.assertEquals(5, server.getChannels().size());
    Map<String, Channel> map = server.selectAvailableChannelsForHostAddress();
    Assert.assertEquals(1, map.size());
    Channel chn = map.values().iterator().next();
    Assert.assertTrue(chn.isConnected());
    Assert.assertEquals(
        chn.getLocalAddress().getAddress().getHostAddress(), map.keySet().iterator().next());

    Channel channel = server.getChannel(new InetSocketAddress("192.168.1.1", 9000));
    Assert.assertNull(channel);

    channel = server.getChannel(chn.getRemoteAddress());
    Assert.assertNotNull(channel);
    Assert.assertTrue(channel.isConnected());

    channel =
        server.getChannel(
            new URL(
                chn.getRemoteAddress().getAddress().getHostAddress(),
                chn.getRemoteAddress().getPort()));
    Assert.assertNotNull(channel);
    Assert.assertTrue(channel.isConnected());
  }
}
