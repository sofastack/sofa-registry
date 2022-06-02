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

import com.alipay.remoting.exception.RemotingException;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.remoting.Channel;
import java.net.InetSocketAddress;
import java.util.Collections;
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
  private static URL url = new URL("127.0.0.1", 12345);
  private static URL listenURL = new URL("0.0.0.0", 12345);
  private static BoltServer server = new BoltServer(listenURL, Collections.emptyList());

  @BeforeClass
  public static void before() {
    server.configWaterMark(1024 * 32, 1024 * 64);
    server.initServer();
    Assert.assertFalse(server.isOpen());
    Assert.assertTrue(server.isClosed());
    server.startServer();
    Assert.assertTrue(server.isOpen());
    Assert.assertFalse(server.isClosed());
  }

  @AfterClass
  public static void after() {
    server.close();
  }

  @Test
  public void testChannel() throws Exception {
    // newInvokeContext do nothing
    Assert.assertNull(server.newInvokeContext(null));

    Assert.assertNotNull(server.getRpcServer());
    Assert.assertEquals(server.getChannels().size(), 0);
    Assert.assertEquals(server.selectAvailableChannelsForHostAddress().size(), 0);
    Assert.assertEquals(server.getLocalAddress().getPort(), 12345);
    server.close(null);

    BoltClient client1 = new BoltClient(2);
    Assert.assertEquals(client1.getConnections().size(), 0);
    TestUtils.assertException(IllegalArgumentException.class, () -> client1.connect(null));
    Assert.assertFalse(client1.isClosed());
    client1.connect(url);
    Assert.assertEquals(client1.getConnections().size(), 1);
    Assert.assertEquals(client1.getConnections().values().iterator().next().size(), 2);
    Assert.assertNotNull(client1.getLocalAddress());

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
    server.close(channel);
    client1.close();
    Thread.sleep(100);
    Assert.assertFalse(channel.isConnected());
    Assert.assertTrue(client1.isClosed());
  }

  @Test
  public void testException() {
    Assert.assertEquals(
        BoltUtil.handleException(null, null, new RemotingException("t"), "t").getCause().getClass(),
        RemotingException.class);
    Assert.assertEquals(
        BoltUtil.handleException(null, null, new InterruptedException("t"), "t")
            .getCause()
            .getClass(),
        InterruptedException.class);
    Assert.assertEquals(
        BoltUtil.handleException(null, null, new IllegalArgumentException("t"), "t")
            .getCause()
            .getClass(),
        IllegalArgumentException.class);
  }
}
