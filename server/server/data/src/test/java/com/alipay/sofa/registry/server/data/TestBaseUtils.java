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
package com.alipay.sofa.registry.server.data;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.alipay.remoting.Connection;
import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.RegisterVersion;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.dataserver.DatumSummary;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotAccess;
import com.alipay.sofa.registry.common.model.slot.SlotConfig;
import com.alipay.sofa.registry.common.model.slot.func.SlotFunctionRegistry;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.remoting.bolt.BoltChannel;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumStorageDelegate;
import com.alipay.sofa.registry.server.data.cache.LocalDatumStorage;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.task.FastRejectedExecutionException;
import com.alipay.sofa.registry.task.KeyedThreadPoolExecutor;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Assert;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public final class TestBaseUtils {
  private static final AtomicLong REGISTER_ID_SEQ = new AtomicLong();
  private static final AtomicLong CLIENT_VERSION = new AtomicLong();

  public static final String TEST_DATA_ID = "testDataId";
  public static final String TEST_DATA_INFO_ID;

  private static final String TEST_REGISTER_ID = "testRegisterId";

  private static final AtomicLong DATA_ID_SEQ = new AtomicLong();

  static {
    Publisher p = TestBaseUtils.createTestPublisher(TEST_DATA_ID);
    TEST_DATA_INFO_ID = p.getDataInfoId();
  }

  private TestBaseUtils() {}

  public static Publisher createTestPublisher(String dataId) {
    Publisher publisher = new Publisher();
    DataInfo dataInfo = DataInfo.valueOf(DataInfo.toDataInfoId(dataId, "I", "G"));
    publisher.setDataInfoId(dataInfo.getDataInfoId());
    publisher.setDataId(dataInfo.getDataId());
    publisher.setInstanceId(dataInfo.getInstanceId());
    publisher.setGroup(dataInfo.getGroup());
    publisher.setRegisterId(TEST_REGISTER_ID + REGISTER_ID_SEQ.incrementAndGet());
    publisher.setSessionProcessId(ServerEnv.PROCESS_ID);
    publisher.setRegisterTimestamp(System.currentTimeMillis());
    publisher.setVersion(100 + CLIENT_VERSION.incrementAndGet());
    ConnectId connectId =
        ConnectId.of(
            ServerEnv.PROCESS_ID.getHostAddress() + ":9999",
            ServerEnv.PROCESS_ID.getHostAddress() + ":9998");
    publisher.setSourceAddress(URL.valueOf(connectId.clientAddress()));
    publisher.setTargetAddress(URL.valueOf(connectId.sessionAddress()));
    return publisher;
  }

  public static Publisher cloneBase(Publisher publisher) {
    Publisher clone = TestBaseUtils.createTestPublisher(publisher.getDataId());
    clone.setRegisterId(publisher.getRegisterId());
    clone.setVersion(publisher.getVersion());
    clone.setRegisterTimestamp(publisher.getRegisterTimestamp());
    clone.setSessionProcessId(publisher.getSessionProcessId());
    return clone;
  }

  public static void assertEquals(Datum datum, Publisher publisher) {
    Assert.assertEquals(publisher.getDataInfoId(), datum.getDataInfoId());
    Assert.assertEquals(publisher.getDataId(), datum.getDataId());
    Assert.assertEquals(publisher.getInstanceId(), datum.getInstanceId());
    Assert.assertEquals(publisher.getGroup(), datum.getGroup());
    Assert.assertTrue(datum.getPubMap().containsKey(publisher.getRegisterId()));
  }

  public static ConnectId notExistConnectId() {
    return ConnectId.of("notExist:9999", "notExist:9998");
  }

  public static LocalDatumStorage newLocalStorage(String dataCenter, boolean init) {
    DataServerConfig dataServerConfig = newDataConfig(dataCenter);
    LocalDatumStorage storage = new LocalDatumStorage();
    storage.setDataServerConfig(dataServerConfig);
    if (init) {
      for (int i = 0; i < SlotConfig.SLOT_NUM; i++) {
        storage.getSlotChangeListener().onSlotAdd(i, Slot.Role.Leader);
      }
    }
    return storage;
  }

  public static DataServerConfig newDataConfig(String dataCenter) {
    CommonConfig commonConfig = mock(CommonConfig.class);
    when(commonConfig.getLocalDataCenter()).thenReturn(dataCenter);
    return new DataServerConfig(commonConfig);
  }

  public static DatumStorageDelegate newLocalDatumCache(String localDataCenter, boolean init) {
    DatumStorageDelegate cache = new DatumStorageDelegate();
    LocalDatumStorage storage = TestBaseUtils.newLocalStorage(localDataCenter, init);
    cache.setLocalDatumStorage(storage);
    cache.setDataServerConfig(storage.getDataServerConfig());
    return cache;
  }

  public static DatumSummary newDatumSummary(int pubCount) {
    final String dataId = TEST_DATA_ID + "-" + DATA_ID_SEQ.incrementAndGet();
    return newDatumSummary(pubCount, dataId);
  }

  public static DatumSummary newDatumSummary(int pubCount, String dataInfoId) {
    Map<String, RegisterVersion> versions = Maps.newHashMap();
    for (int i = 0; i < pubCount; i++) {
      final String registerId = TEST_REGISTER_ID + "-" + REGISTER_ID_SEQ.incrementAndGet();
      versions.put(
          registerId,
          RegisterVersion.of(CLIENT_VERSION.incrementAndGet(), System.currentTimeMillis()));
    }
    return new DatumSummary(dataInfoId, versions);
  }

  public static List<Publisher> createTestPublishers(int slotId, int count) {
    List<Publisher> list = Lists.newArrayListWithCapacity(count);
    for (int i = 0; i < Integer.MAX_VALUE; i++) {
      Publisher p =
          createTestPublisher(TEST_DATA_ID + "-" + DATA_ID_SEQ.incrementAndGet() + "-" + i);
      int id = SlotFunctionRegistry.getFunc().slotOf(p.getDataInfoId());
      if (id == slotId) {
        // find the slotId
        list.add(p);
        for (int j = 1; j < count; j++) {
          list.add(createTestPublisher(p.getDataId()));
        }
        break;
      }
    }
    return list;
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

  public static SlotAccess moved() {
    return new SlotAccess(1, 1, SlotAccess.Status.Moved, 1);
  }

  public static SlotAccess accept() {
    return accept(1, 1, 1);
  }

  public static SlotAccess accept(int slotId, long tableEpoch, long leaderEpoch) {
    return new SlotAccess(slotId, 1, SlotAccess.Status.Accept, leaderEpoch);
  }

  public static SlotAccess migrating() {
    return migrating(1, 1, 1);
  }

  public static SlotAccess migrating(int slotId, long tableEpoch, long leaderEpoch) {
    return new SlotAccess(slotId, 1, SlotAccess.Status.Migrating, leaderEpoch);
  }

  public static SlotAccess misMatch() {
    return new SlotAccess(1, 1, SlotAccess.Status.MisMatch, 1);
  }

  public static void assertException(Class<? extends Throwable> eclazz, Runnable runnable) {
    try {
      runnable.run();
      Assert.fail("except exception");
    } catch (Throwable exception) {
      Assert.assertEquals(exception.getClass(), eclazz);
    }
  }

  public static KeyedThreadPoolExecutor rejectExecutor() {
    KeyedThreadPoolExecutor executor = Mockito.mock(KeyedThreadPoolExecutor.class);
    Mockito.when(executor.execute(Mockito.anyObject(), Mockito.anyObject()))
        .thenThrow(new FastRejectedExecutionException("reject"));
    return executor;
  }
}
