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

import static org.mockito.Mockito.*;

import com.alipay.remoting.Connection;
import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.ElementType;
import com.alipay.sofa.registry.common.model.PublishSource;
import com.alipay.sofa.registry.common.model.ServerDataBox;
import com.alipay.sofa.registry.common.model.slot.func.SlotFunctionRegistry;
import com.alipay.sofa.registry.common.model.store.*;
import com.alipay.sofa.registry.core.model.BaseRegister;
import com.alipay.sofa.registry.core.model.MultiReceivedData;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.remoting.bolt.BoltChannel;
import com.alipay.sofa.registry.remoting.bolt.exchange.BoltExchange;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfigBean;
import com.alipay.sofa.registry.server.session.converter.ReceivedDataConverter;
import com.alipay.sofa.registry.server.session.metadata.MetadataCacheRegistry;
import com.alipay.sofa.registry.server.session.multi.cluster.DataCenterMetadataCacheImpl;
import com.alipay.sofa.registry.server.session.predicate.ZonePredicate;
import com.alipay.sofa.registry.server.session.providedata.FetchGrayPushSwitchService;
import com.alipay.sofa.registry.server.session.providedata.FetchStopPushService;
import com.alipay.sofa.registry.server.session.push.PushSwitchService;
import com.alipay.sofa.registry.server.session.remoting.console.SessionConsoleExchanger;
import com.alipay.sofa.registry.server.shared.config.CommonConfig;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.alipay.sofa.registry.util.StringFormatter;
import com.google.common.collect.Lists;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import org.assertj.core.util.Maps;
import org.assertj.core.util.Sets;
import org.junit.Assert;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class TestUtils {
  private static final AtomicLong REGISTER_ID_SEQ = new AtomicLong();
  public static final String GROUP = "testGroup";
  public static final String INSTANCE = "testInstance";

  private static final AtomicLong CLIENT_VERSION = new AtomicLong();

  public static final String TEST_DATA_ID = "testDataId";
  public static final String TEST_DATA_INFO_ID;

  private static final String TEST_REGISTER_ID = "testRegisterId";

  private static final AtomicLong DATA_ID_SEQ = new AtomicLong();

  static {
    Publisher p = createTestPublisher(TEST_DATA_ID);
    TEST_DATA_INFO_ID = p.getDataInfoId();
  }

  public static SessionServerConfigBean newSessionConfig(String dataCenter) {
    CommonConfig commonConfig = mock(CommonConfig.class);
    when(commonConfig.getLocalDataCenter()).thenReturn(dataCenter);
    when(commonConfig.getLocalRegion()).thenReturn("DEF_ZONE");
    SessionServerConfigBean configBean = new SessionServerConfigBean(commonConfig);
    configBean.setCacheDatumMaxWeight(1000000);
    return configBean;
  }

  public static SessionServerConfigBean newSessionConfig(String dataCenter, String region) {
    CommonConfig commonConfig = mock(CommonConfig.class);
    when(commonConfig.getLocalDataCenter()).thenReturn(dataCenter);
    when(commonConfig.getLocalRegion()).thenReturn(region);
    SessionServerConfigBean configBean = new SessionServerConfigBean(commonConfig);
    return configBean;
  }

  public static Publisher createTestPublisher(String dataId) {
    Publisher publisher = new Publisher();
    DataInfo dataInfo = DataInfo.valueOf(DataInfo.toDataInfoId(dataId, INSTANCE, GROUP));
    publisher.setDataInfoId(dataInfo.getDataInfoId());
    publisher.setDataId(dataInfo.getDataId());
    publisher.setInstanceId(dataInfo.getInstanceId());
    publisher.setGroup(dataInfo.getGroup());
    publisher.setRegisterId(TEST_REGISTER_ID + REGISTER_ID_SEQ.incrementAndGet());
    publisher.setSessionProcessId(ServerEnv.PROCESS_ID);
    publisher.setProcessId("process-" + REGISTER_ID_SEQ.get());
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

  public static Publisher cloneBase(Publisher publisher) {
    Publisher clone = createTestPublisher(publisher.getDataId());
    clone.setRegisterId(publisher.getRegisterId());
    clone.setVersion(publisher.getVersion());
    clone.setRegisterTimestamp(publisher.getRegisterTimestamp());
    clone.setSessionProcessId(publisher.getSessionProcessId());
    return clone;
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

  public static DataCenterMetadataCacheImpl newDataCenterMetaCache(SessionServerConfig config) {
    DataCenterMetadataCacheImpl dataCenterMetadataCache = new DataCenterMetadataCacheImpl();
    dataCenterMetadataCache.setSessionServerConfig(config);

    return dataCenterMetadataCache;
  }

  public static DataCenterMetadataCacheImpl newDataCenterMetaCache(String dataCenter) {
    DataCenterMetadataCacheImpl dataCenterMetadataCache = new DataCenterMetadataCacheImpl();
    dataCenterMetadataCache.setSessionServerConfig(newSessionConfig(dataCenter));

    return dataCenterMetadataCache;
  }

  public static PushSwitchService newPushSwitchService(SessionServerConfigBean serverConfigBean) {
    PushSwitchService pushSwitchService = new PushSwitchService();
    pushSwitchService
        .setFetchGrayPushSwitchService(new FetchGrayPushSwitchService())
        .setSessionServerConfig(serverConfigBean)
        .setFetchStopPushService(new FetchStopPushService())
        .setMetadataCacheRegistry(new MetadataCacheRegistry());

    return pushSwitchService;
  }

  public static PushSwitchService newPushSwitchService(String testDc) {
    SessionServerConfigBean sessionServerConfigBean = newSessionConfig(testDc);
    PushSwitchService pushSwitchService = new PushSwitchService();
    pushSwitchService
        .setFetchGrayPushSwitchService(new FetchGrayPushSwitchService())
        .setSessionServerConfig(sessionServerConfigBean)
        .setFetchStopPushService(new FetchStopPushService())
        .setMetadataCacheRegistry(new MetadataCacheRegistry());

    return pushSwitchService;
  }

  public static MultiSubDatum newMultiSubDatum(
      String dataCenter, String dataId, long version, List<SubPublisher> publishers) {
    SubDatum subDatum = TestUtils.newSubDatum(dataCenter, dataId, version, publishers);
    return MultiSubDatum.of(subDatum);
  }

  public static MultiSubDatum newMultiSubDatum(
      String dataId, long version, List<SubPublisher> publishers) {
    SubDatum subDatum = TestUtils.newSubDatum(dataId, version, publishers);
    return MultiSubDatum.of(subDatum);
  }

  public static MultiSubDatum newMultiSubDatum(String dataId, int dataCenterCount, int pubCount) {
    ParaCheckUtil.assertTrue(dataCenterCount > 0, "dataCenterCount");
    ParaCheckUtil.assertTrue(pubCount > 0, "pubCount");
    String dataInfoId = "";
    Map<String, SubDatum> datumMap =
        com.google.common.collect.Maps.newHashMapWithExpectedSize(dataCenterCount);

    for (int i = 0; i < dataCenterCount; i++) {
      String dataCenter = "dataCenter-" + i;
      List<SubPublisher> publishers = Lists.newArrayListWithExpectedSize(pubCount);
      for (int j = 0; j < pubCount; j++) {
        SubPublisher pub =
            TestUtils.newSubPublisher(
                System.nanoTime(),
                System.nanoTime(),
                dataCenter + StringFormatter.format("-cell-{}", j));
        publishers.add(pub);
      }

      SubDatum subDatum = TestUtils.newSubDatum(dataCenter, dataId, System.nanoTime(), publishers);
      dataInfoId = subDatum.getDataInfoId();
      datumMap.put(dataCenter, subDatum);
    }

    MultiSubDatum multiSubDatum = new MultiSubDatum(dataInfoId, datumMap);
    return multiSubDatum;
  }

  public static PushData<MultiReceivedData> createPushData(
      String dataId, int dataCenterCount, int pubCount) {
    MultiSubDatum multiSubDatum = TestUtils.newMultiSubDatum(dataId, dataCenterCount, pubCount);
    Entry<String, SubDatum> first =
        multiSubDatum.getDatumMap().entrySet().stream().findFirst().get();
    String localDataCenter = first.getKey();
    String localZone = "localZone";

    String invalidForeverZones =
        first.getValue().mustGetPublishers().stream().findFirst().get().getCell();
    SessionServerConfigBean sessionServerConfig =
        TestUtils.newSessionConfig(localDataCenter, localZone);
    sessionServerConfig.setInvalidForeverZones(invalidForeverZones);
    sessionServerConfig.setInvalidIgnoreDataidRegex("^Zone_Servers_xxx$");

    Predicate<String> pushDataPredicate =
        ZonePredicate.pushDataPredicate(
            multiSubDatum.getDataId(), localZone, ScopeEnum.global, sessionServerConfig);
    Map<String, Set<String>> segmentZones = com.google.common.collect.Maps.newHashMap();
    for (Entry<String, SubDatum> entry : multiSubDatum.getDatumMap().entrySet()) {
      Set<String> cells = Sets.newHashSet();
      for (SubPublisher pub : entry.getValue().mustGetPublishers()) {
        cells.add(pub.getCell());
      }
      segmentZones.put(entry.getKey(), cells);
    }

    PushData<MultiReceivedData> pushData =
        ReceivedDataConverter.getMultiReceivedData(
            multiSubDatum,
            ScopeEnum.global,
            com.google.common.collect.Lists.newArrayList("aaa"),
            localZone,
            localDataCenter,
            pushDataPredicate,
            segmentZones);
    return pushData;
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
    Assert.assertTrue(StringFormatter.format("v={}, low={}", v, low), v >= low);
    Assert.assertTrue(StringFormatter.format("v={}, high={}", v, high), v <= high);
  }

  public static SubPublisher newSubPublisher(long version, long timestamp) {
    return newSubPublisher(version, timestamp, "testCell");
  }

  public static SubPublisher newSubPublisher(long version, long timestamp, String cell) {
    String registerId = "testRegisterId-" + REGISTER_ID_SEQ.incrementAndGet();
    List<ServerDataBox> dataList = Lists.newArrayList();
    dataList.add(new ServerDataBox("testDataBox"));
    SubPublisher publisher =
        new SubPublisher(
            registerId,
            cell,
            dataList,
            "testClient",
            version,
            "192.168.0.1:8888",
            timestamp,
            PublishSource.CLIENT);
    return publisher;
  }

  public static String newDataInfoId(String dataId) {
    String dataInfoId = DataInfo.toDataInfoId(dataId, INSTANCE, GROUP);
    return dataInfoId;
  }

  public static SubDatum newSubDatum(String dataId, long version, List<SubPublisher> publishers) {
    return newSubDatum("dataCenter", dataId, version, publishers);
  }

  public static SubDatum newSubDatum(
      String dataCenter, String dataId, long version, List<SubPublisher> publishers) {
    String dataInfo = DataInfo.toDataInfoId(dataId, INSTANCE, GROUP);
    SubDatum subDatum =
        SubDatum.normalOf(
            dataInfo,
            dataCenter,
            version,
            publishers,
            dataId,
            "testInstance",
            "testGroup",
            Lists.newArrayList(System.currentTimeMillis()));
    return subDatum;
  }

  public static Subscriber newZonePbSubscriber(String cell) {
    Subscriber subscriber = newZoneSubscriber(cell);
    subscriber.setSourceAddress(new URL(URL.ProtocolType.BOLT, "192.168.1.1", 8888, URL.PROTOBUF));
    return subscriber;
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

  public static Watcher newWatcher(String dataId) {
    Watcher wat = new Watcher();
    wat.setDataId(dataId);
    wat.setRegisterId("test-Watcher-" + REGISTER_ID_SEQ.incrementAndGet());
    wat.setClientVersion(BaseInfo.ClientVersion.StoreData);
    wat.setGroup(GROUP);
    wat.setInstanceId(INSTANCE);
    wat.setSourceAddress(new URL("192.168.1.1", 8888));
    String dataInfo = DataInfo.toDataInfoId(dataId, wat.getInstanceId(), wat.getGroup());
    wat.setDataInfoId(dataInfo);
    return wat;
  }

  public static Subscriber newZoneSubscriber(String dataId, String cell) {
    Subscriber subscriber = newZoneSubscriber(cell);
    subscriber.setDataId(dataId);
    String dataInfoId =
        DataInfo.toDataInfoId(dataId, subscriber.getInstanceId(), subscriber.getGroup());
    subscriber.setDataInfoId(dataInfoId);
    return subscriber;
  }

  public static void setField(BaseRegister register) {
    register.setAppName("testApp");
    register.setClientId("testClientId");
    register.setDataId("testDataId");
    register.setEventType("testEventType");
    register.setGroup("testGroup");
    register.setInstanceId("testInstanceId");
    String dataInfoId =
        DataInfo.toDataInfoId(register.getDataId(), register.getInstanceId(), register.getGroup());
    register.setDataInfoId(dataInfoId);
    register.setIp("192.168.1.1");
    register.setPort(8888);
    register.setProcessId("testProcessId");
    register.setRegistId("testRegisterId");
    register.setVersion(100L);
    register.setTimestamp(System.currentTimeMillis());
    register.setZone("testZone");
    register.setAttributes(Maps.newHashMap("testAttrKey", "testAttrVal"));
  }

  public static void assertEquals(BaseRegister left, BaseRegister right) {
    Assert.assertEquals(left.getAppName(), right.getAppName());
    Assert.assertEquals(left.getClientId(), right.getClientId());
    Assert.assertEquals(left.getDataId(), right.getDataId());
    Assert.assertEquals(left.getDataInfoId(), right.getDataInfoId());
    Assert.assertEquals(left.getEventType(), right.getEventType());
    Assert.assertEquals(left.getGroup(), right.getGroup());
    Assert.assertEquals(left.getInstanceId(), right.getInstanceId());
    Assert.assertEquals(left.getIp(), right.getIp());
    Assert.assertEquals(left.getPort(), right.getPort());
    Assert.assertEquals(left.getProcessId(), right.getProcessId());
    Assert.assertEquals(left.getRegistId(), right.getRegistId());
    Assert.assertEquals(left.getVersion(), right.getVersion());
    Assert.assertEquals(left.getTimestamp(), right.getTimestamp());
    Assert.assertEquals(left.getZone(), right.getZone());
    Assert.assertEquals(left.getAttributes(), right.getAttributes());
  }

  public static void assertEquals(BaseRegister left, BaseInfo right) {
    Assert.assertEquals(left.getAppName(), right.getAppName());
    Assert.assertEquals(left.getClientId(), right.getClientId());
    Assert.assertEquals(left.getDataId(), right.getDataId());
    Assert.assertEquals(left.getDataInfoId(), right.getDataInfoId());
    Assert.assertEquals(left.getGroup(), right.getGroup());
    Assert.assertEquals(left.getInstanceId(), right.getInstanceId());
    Assert.assertEquals(left.getProcessId(), right.getProcessId());
    Assert.assertEquals(left.getRegistId(), right.getRegisterId());
    Assert.assertEquals(left.getVersion().longValue(), right.getVersion());
    Assert.assertEquals(left.getTimestamp().longValue(), right.getClientRegisterTimestamp());
    Assert.assertEquals(left.getZone(), right.getCell());
    Assert.assertEquals(left.getAttributes(), right.getAttributes());
    Assert.assertEquals(left.getIp(), right.getSourceAddress().getIpAddress());
    Assert.assertEquals(left.getPort().intValue(), right.getSourceAddress().getPort());
    Assert.assertEquals(right.getClientVersion(), BaseInfo.ClientVersion.StoreData);
  }

  public static SessionConsoleExchanger newSessionConsoleExchanger(
      SessionServerConfigBean configBean) {
    SessionConsoleExchanger exchanger = new SessionConsoleExchanger();
    exchanger.setSessionServerConfig(configBean);
    BoltExchange boltExchange = new BoltExchange();
    exchanger.setBoltExchange(boltExchange);

    Assert.assertEquals(exchanger.getConnNum(), 2);
    Assert.assertEquals(exchanger.getServerPort(), configBean.getConsolePort());
    Assert.assertEquals(exchanger.getRpcTimeoutMillis(), 3000);
    Assert.assertNull(exchanger.getClient());
    Assert.assertNull(exchanger.connectServer());
    Assert.assertEquals(exchanger.getServerIps().size(), 0);
    Assert.assertEquals(exchanger.getConnections().size(), 0);

    return exchanger;
  }
}
