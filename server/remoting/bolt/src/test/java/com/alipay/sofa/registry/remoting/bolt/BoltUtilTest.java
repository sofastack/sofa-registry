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

import com.alipay.remoting.*;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.remoting.exchange.RequestChannelClosedException;
import com.alipay.sofa.registry.remoting.exchange.RequestException;
import com.google.common.collect.Lists;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author xuanbei
 * @since 2019/3/26
 */
public class BoltUtilTest {
  @Test
  public void testGetBoltCustomSerializer() {
    Assert.assertNull(BoltUtil.getBoltCustomSerializer(new MockChannel()));
    BoltChannel boltChannel = new BoltChannel(null);
    InvokeContext invokeContext = new InvokeContext();
    invokeContext.put(InvokeContext.BOLT_CUSTOM_SERIALIZER, new Object());
    RemotingContext remotingContext =
        new RemotingContext(
            new MockChannelHandlerContext(), invokeContext, false, new ConcurrentHashMap<>());
    BizContext bizContext = new DefaultBizContext(remotingContext);
    boltChannel.setBizContext(bizContext);
    boolean isException = false;
    try {
      BoltUtil.getBoltCustomSerializer(boltChannel);
    } catch (Throwable r) {
      isException = true;
    }
    Assert.assertTrue(isException);
    invokeContext.put(InvokeContext.BOLT_CUSTOM_SERIALIZER, new Byte("3"));
    Assert.assertEquals(new Byte("3"), BoltUtil.getBoltCustomSerializer(boltChannel));
  }

  @Test
  public void testBase() {
    Connection conn = Mockito.mock(Connection.class);
    BoltChannel boltChannel = new BoltChannel(conn);

    AsyncContext asyncContext = Mockito.mock(AsyncContext.class);
    boltChannel.setAsyncContext(asyncContext);
    Assert.assertTrue(asyncContext == boltChannel.getAsyncContext());

    BizContext bizContext = Mockito.mock(BizContext.class);
    boltChannel.setBizContext(bizContext);

    Assert.assertTrue(bizContext == boltChannel.getBizContext());

    Assert.assertNull(boltChannel.getWebTarget());
    boltChannel.close();
    Mockito.verify(conn, Mockito.times(1)).close();

    Assert.assertNull(boltChannel.getAttribute("key"));
    boltChannel.setAttribute("key", "val");
    Assert.assertEquals(boltChannel.getAttribute("key"), "val");
    boltChannel.setAttribute("key", null);
    Assert.assertNull(boltChannel.getAttribute("key"));
  }

  @Test
  public void testCheckChannelConnected() {
    TestUtils.assertException(RequestException.class, () -> BoltUtil.checkChannelConnected(null));
    Channel channel = Mockito.mock(Channel.class);
    Mockito.when(channel.isConnected()).thenReturn(false);
    TestUtils.assertException(
        RequestChannelClosedException.class, () -> BoltUtil.checkChannelConnected(channel));
  }

  @Test
  public void testCreateTargetBoltUrl() {
    Channel channel = Mockito.mock(Channel.class);
    Mockito.when(channel.getRemoteAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 9999));
    Url url = BoltUtil.createTargetUrl(channel);
    Assert.assertEquals(url.getPort(), 9999);
    Assert.assertEquals(url.getIp(), "127.0.0.1");
  }

  @Test
  public void testGetListenerHandlers() {
    Assert.assertNull(BoltUtil.getListenerHandlers(Collections.emptyList()));
    ChannelHandler handler1 = Mockito.mock(ChannelHandler.class);
    Mockito.when(handler1.getType()).thenReturn(ChannelHandler.HandlerType.LISTENER);
    ChannelHandler handler2 = Mockito.mock(ChannelHandler.class);
    Mockito.when(handler2.getType()).thenReturn(ChannelHandler.HandlerType.PROCESSER);

    Assert.assertEquals(
        BoltUtil.getListenerHandlers(Lists.newArrayList(handler1, handler2)), handler1);

    TestUtils.assertException(
        IllegalArgumentException.class,
        () -> BoltUtil.getListenerHandlers(Lists.newArrayList(handler1, handler1)));
  }
}
