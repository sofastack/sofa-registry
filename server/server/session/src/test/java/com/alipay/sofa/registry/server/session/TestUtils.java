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
package com.alipay.sofa.registry.server.session;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.alipay.remoting.Connection;
import com.alipay.sofa.registry.remoting.bolt.BoltChannel;
import com.alipay.sofa.registry.server.session.bootstrap.CommonConfig;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfigBean;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Assert;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class TestUtils {

  public static SessionServerConfigBean newSessionConfig(String dataCenter) {
    CommonConfig commonConfig = mock(CommonConfig.class);
    when(commonConfig.getLocalDataCenter()).thenReturn(dataCenter);
    SessionServerConfigBean configBean = new SessionServerConfigBean(commonConfig);
    return configBean;
  }

  public static void assertRunException(Class<? extends Throwable> eclazz, RunError runnable) {
    try {
      runnable.run();
      Assert.fail();
    } catch (Throwable exception) {
      Assert.assertEquals(eclazz, exception.getClass());
    }
  }

  public interface RunError {
    void run() throws Exception;
  }

  public static MockBlotChannel newChannel(int localPort, String remoteAddress, int remotePort) {
    return new MockBlotChannel(localPort, remoteAddress, remotePort);
  }

  public static final class MockBlotChannel extends BoltChannel {
    final InetSocketAddress remote;
    final InetSocketAddress local;
    public volatile boolean connected = true;
    private final AtomicBoolean active = new AtomicBoolean(true);

    public final Connection conn;

    public MockBlotChannel(int localPort, String remoteAddress, int remotePort) {
      super(new Connection(createChn()));
      this.conn = getConnection();
      this.local = new InetSocketAddress(ServerEnv.IP, localPort);
      this.remote = new InetSocketAddress(remoteAddress, remotePort);
      Channel chn = conn.getChannel();
      Mockito.when(chn.remoteAddress()).thenReturn(remote);
      Mockito.when(chn.isActive())
          .thenAnswer(
              new Answer<Boolean>() {
                public Boolean answer(InvocationOnMock var1) throws Throwable {
                  return active.get();
                }
              });
    }

    private static Channel createChn() {
      Channel chn = Mockito.mock(io.netty.channel.Channel.class);
      Mockito.when(chn.attr(Mockito.any(AttributeKey.class)))
          .thenReturn(Mockito.mock(Attribute.class));
      return chn;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
      return remote;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
      return local;
    }

    @Override
    public boolean isConnected() {
      return active.get();
    }

    public void setActive(boolean b) {
      active.set(b);
    }
  }
}
