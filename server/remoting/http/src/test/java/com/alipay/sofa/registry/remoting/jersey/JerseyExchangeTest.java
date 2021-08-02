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
package com.alipay.sofa.registry.remoting.jersey;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.remoting.CallbackHandler;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.jersey.exchange.JerseyExchange;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author xuanbei
 * @since 2019/3/27
 */
public class JerseyExchangeTest {
  private static final int JERSEY_TEST_PORT = 9662;

  @Test
  public void doTest() {
    ResourceConfig resourceConfig = new ResourceConfig();
    resourceConfig.register(JacksonFeature.class);
    resourceConfig.registerInstances(new TestHttpResource());

    CallbackHandler callbackHandler =
        new CallbackHandler() {
          @Override
          public void onCallback(Channel channel, Object message) {}

          @Override
          public void onException(Channel channel, Throwable exception) {}

          @Override
          public Executor getExecutor() {
            return null;
          }
        };

    JerseyExchange jerseyExchange = new JerseyExchange();
    URL url = new URL(NetUtil.getLocalAddress().getHostAddress(), JERSEY_TEST_PORT);
    JerseyJettyServer jerseyJettyServer =
        (JerseyJettyServer) jerseyExchange.open(url, new ResourceConfig[] {resourceConfig});
    testJerseyJettyServer(url, jerseyJettyServer, jerseyExchange, callbackHandler);

    JerseyClient jerseyClient1 = (JerseyClient) jerseyExchange.getClient("jersey");
    JerseyClient jerseyClient2 = (JerseyClient) jerseyExchange.connect("jersey", url);
    Assert.assertEquals(jerseyClient1, jerseyClient2);
    testJerseyClient(url, jerseyClient1, callbackHandler);

    JerseyChannel jerseyChannel = (JerseyChannel) jerseyClient1.connect(url);
    testJerseyChannel(jerseyChannel);
    String result =
        jerseyChannel.getWebTarget().path("test").request(APPLICATION_JSON).get(String.class);
    Assert.assertEquals("TestResource", result);
    jerseyJettyServer.close();
  }

  @Test
  public void testServer() {
    ResourceConfig resourceConfig = new ResourceConfig();
    JerseyJettyServer server = new JerseyJettyServer(resourceConfig, null);

    assertException(RuntimeException.class, () -> server.startServer());
    Assert.assertFalse(server.isOpen());
    Assert.assertTrue(server.isClosed());

    assertException(RuntimeException.class, () -> server.close());

    assertException(
        UnsupportedOperationException.class, () -> server.selectAvailableChannelsForHostAddress());
    assertException(UnsupportedOperationException.class, () -> server.close(null));

    Assert.assertEquals(server.getChannelCount(), server.getChannels().size());
  }

  private void testJerseyJettyServer(
      URL url,
      JerseyJettyServer jerseyJettyServer,
      JerseyExchange jerseyExchange,
      CallbackHandler callbackHandler) {
    Assert.assertEquals(jerseyJettyServer, jerseyExchange.getServer(JERSEY_TEST_PORT));
    Assert.assertTrue(jerseyJettyServer.isOpen());
    Assert.assertEquals(jerseyJettyServer.getChannels().size(), 0);
    Assert.assertNull(jerseyJettyServer.getChannel(new InetSocketAddress(9663)));
    Assert.assertNull(jerseyJettyServer.getChannel(url));
    Assert.assertEquals(
        new InetSocketAddress(JERSEY_TEST_PORT), jerseyJettyServer.getLocalAddress());
    Assert.assertFalse(jerseyJettyServer.isClosed());

    jerseyJettyServer.sendCallback(
        new JerseyChannel(null, null), new Object(), callbackHandler, 1000);
    Assert.assertNull(
        jerseyJettyServer.sendSync(new JerseyChannel(null, null), new Object(), 1000));
  }

  private void testJerseyClient(
      URL url, JerseyClient jerseyClient, CallbackHandler callbackHandler) {
    Assert.assertEquals(NetUtil.getLocalSocketAddress(), jerseyClient.getLocalAddress());
    Assert.assertFalse(jerseyClient.isClosed());
    Assert.assertNull(jerseyClient.getChannel(url));
    Assert.assertNull(jerseyClient.sendSync(new URL(), new Object(), 1000));
    jerseyClient.close();
    jerseyClient.sendCallback(new URL(), new Object(), callbackHandler, 1000);
  }

  private void testJerseyChannel(JerseyChannel jerseyChannel) {
    Assert.assertEquals(
        new InetSocketAddress(NetUtil.getLocalAddress(), 9662), jerseyChannel.getRemoteAddress());
    Assert.assertEquals(NetUtil.getLocalSocketAddress(), jerseyChannel.getLocalAddress());
    Assert.assertTrue(jerseyChannel.isConnected());
  }

  public static void assertException(Class<? extends Throwable> eclazz, Runnable runnable) {
    try {
      runnable.run();
      Assert.assertTrue(false);
    } catch (Throwable exception) {
      Assert.assertEquals(exception.getClass(), eclazz);
    }
  }
}
