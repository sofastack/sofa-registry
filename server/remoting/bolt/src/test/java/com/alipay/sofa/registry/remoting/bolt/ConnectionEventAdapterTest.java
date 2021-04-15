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

import com.alipay.remoting.Connection;
import com.alipay.remoting.ConnectionEventType;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import org.junit.Test;
import org.mockito.Mockito;

public class ConnectionEventAdapterTest {
  @Test
  public void test() throws Exception {
    ChannelHandler handler = Mockito.mock(ChannelHandler.class);
    Connection conn = Mockito.mock(Connection.class);
    ConnectionEventAdapter adapter =
        new ConnectionEventAdapter(ConnectionEventType.CONNECT, handler);
    adapter.onEvent("test", conn);
    Mockito.verify(handler, Mockito.times(1)).connected(Mockito.anyObject());
    Mockito.verify(handler, Mockito.times(0)).disconnected(Mockito.anyObject());
    Mockito.verify(handler, Mockito.times(0))
        .caught(Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject());

    handler = Mockito.mock(ChannelHandler.class);
    adapter = new ConnectionEventAdapter(ConnectionEventType.CONNECT_FAILED, handler);
    adapter.onEvent("test", conn);
    Mockito.verify(handler, Mockito.times(0)).connected(Mockito.anyObject());
    Mockito.verify(handler, Mockito.times(0)).disconnected(Mockito.anyObject());
    Mockito.verify(handler, Mockito.times(0))
        .caught(Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject());

    handler = Mockito.mock(ChannelHandler.class);
    adapter = new ConnectionEventAdapter(ConnectionEventType.CLOSE, handler);
    adapter.onEvent("test", conn);
    Mockito.verify(handler, Mockito.times(0)).connected(Mockito.anyObject());
    Mockito.verify(handler, Mockito.times(1)).disconnected(Mockito.anyObject());
    Mockito.verify(handler, Mockito.times(0))
        .caught(Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject());

    handler = Mockito.mock(ChannelHandler.class);
    adapter = new ConnectionEventAdapter(ConnectionEventType.EXCEPTION, handler);
    adapter.onEvent("test", conn);
    Mockito.verify(handler, Mockito.times(0)).connected(Mockito.anyObject());
    Mockito.verify(handler, Mockito.times(0)).disconnected(Mockito.anyObject());
    Mockito.verify(handler, Mockito.times(1))
        .caught(Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject());

    ConnectionEventAdapter exceptionAdapter =
        new ConnectionEventAdapter(ConnectionEventType.EXCEPTION, handler);
    Mockito.when(conn.getLocalPort()).thenThrow(new RuntimeException());
    TestUtils.assertException(RuntimeException.class, () -> exceptionAdapter.onEvent("test", conn));
  }
}
