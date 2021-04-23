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
import com.alipay.sofa.registry.common.model.ElementType;
import com.alipay.sofa.registry.common.model.PublishSource;
import com.alipay.sofa.registry.common.model.store.*;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.remoting.bolt.BoltChannel;
import com.alipay.sofa.registry.server.session.bootstrap.CommonConfig;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfigBean;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.util.StringFormatter;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Assert;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class TestUtils {
  private static final AtomicLong REGISTER_ID_SEQ = new AtomicLong();
  private static final String GROUP = "testGroup";
  private static final String INSTANCE = "testInstance";

  public static SessionServerConfigBean newSessionConfig(String dataCenter) {
    CommonConfig commonConfig = mock(CommonConfig.class);
    when(commonConfig.getLocalDataCenter()).thenReturn(dataCenter);
    when(commonConfig.getLocalRegion()).thenReturn("DEF_ZONE");
    SessionServerConfigBean configBean = new SessionServerConfigBean(commonConfig);
    return configBean;
  }

  public static SessionServerConfigBean newSessionConfig(String dataCenter, String region) {
    CommonConfig commonConfig = mock(CommonConfig.class);
    when(commonConfig.getLocalDataCenter()).thenReturn(dataCenter);
    when(commonConfig.getLocalRegion()).thenReturn(region);
    SessionServerConfigBean configBean = new SessionServerConfigBean(commonConfig);
    return configBean;
  }

  public static void assertRunException(Class<? extends Throwable> eclazz, RunError runnable) {
    try {
      runnable.run();
      Assert.fail();
    } catch (Throwable exception) {
      if (!eclazz.equals(exception.getClass())) {
        exception.printStackTrace();
        Assert.fail();
      }
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

  public static void assertBetween(long v, long low, long high) {
    Assert.assertTrue(StringFormatter.format("v={}, low={}"), v >= low);
    Assert.assertTrue(StringFormatter.format("v={}, high={}"), v <= high);
  }

  public static SubPublisher newSubPublisher(long version, long timestamp) {
    String registerId = "testRegisterId-" + REGISTER_ID_SEQ.incrementAndGet();
    SubPublisher publisher =
        new SubPublisher(
            registerId,
            "testCell",
            Collections.emptyList(),
            "testClient",
            version,
            "192.168.0.1:8888",
            timestamp,
            PublishSource.CLIENT);
    return publisher;
  }

  public static SubDatum newSubDatum(String dataId, long version, List<SubPublisher> publishers) {
    String dataInfo = DataInfo.toDataInfoId(dataId, INSTANCE, GROUP);
    SubDatum subDatum =
        new SubDatum(
            dataInfo, "dataCenter", version, publishers, dataId, "testInstance", "testGroup");
    return subDatum;
  }

  public static Subscriber newZoneSubscriber(String cell) {
    Subscriber subscriber = new Subscriber();
    subscriber.setRegisterId("test-Subscriber-" + REGISTER_ID_SEQ.incrementAndGet());
    subscriber.setScope(ScopeEnum.zone);
    subscriber.setElementType(ElementType.SUBSCRIBER);
    subscriber.setClientVersion(BaseInfo.ClientVersion.StoreData);
    subscriber.setCell(cell);
    subscriber.setGroup(GROUP);
    subscriber.setInstanceId(INSTANCE);
    subscriber.setSourceAddress(new URL("192.168.1.1", 8888));
    return subscriber;
  }

  public static Subscriber newZoneSubscriber(String dataId, String cell) {
    Subscriber subscriber = newZoneSubscriber(cell);
    subscriber.setDataId(dataId);
    String dataInfoId =
        DataInfo.toDataInfoId(dataId, subscriber.getInstanceId(), subscriber.getGroup());
    subscriber.setDataInfoId(dataInfoId);
    return subscriber;
  }
}
