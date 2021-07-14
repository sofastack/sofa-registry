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

import com.alipay.sofa.registry.common.model.ElementType;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.ProcessId;
import com.alipay.sofa.registry.common.model.PublishType;
import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotConfig;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.common.model.store.*;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.exception.SofaRegistryRuntimeException;
import com.alipay.sofa.registry.remoting.CallbackHandler;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.Client;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfigBean;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.alipay.sofa.registry.util.JsonUtils;
import com.alipay.sofa.registry.util.ObjectFactory;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

/**
 * @author chen.zhu
 *     <p>Nov 18, 2020
 */
public class AbstractSessionServerTestBase extends AbstractTestBase {

  @Rule public TestName name = new TestName();

  protected SessionServerConfigBean sessionServerConfig = TestUtils.newSessionConfig(getDc());

  @Before
  public void beforeAbstractMetaServerTest() {}

  public static void setFinalStatic(Field field, Object newValue) throws Exception {
    field.setAccessible(true);

    Field modifiersField = Field.class.getDeclaredField("modifiers");
    modifiersField.setAccessible(true);
    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

    field.set(null, newValue);
  }

  protected static void waitConditionUntilTimeOut(
      BooleanSupplier booleanSupplier, int waitTimeMilli)
      throws TimeoutException, InterruptedException {

    waitConditionUntilTimeOut(booleanSupplier, waitTimeMilli, 2);
  }

  protected static void waitConditionUntilTimeOut(
      BooleanSupplier booleanSupplier, int waitTimeMilli, int intervalMilli)
      throws TimeoutException, InterruptedException {

    long maxTime = System.currentTimeMillis() + waitTimeMilli;

    while (true) {
      boolean result = booleanSupplier.getAsBoolean();
      if (result) {
        return;
      }
      if (System.currentTimeMillis() >= maxTime) {
        throw new TimeoutException("timeout still false:" + waitTimeMilli);
      }
      Thread.sleep(intervalMilli);
    }
  }

  public static URL randomURL() {
    return randomURL("127.0.0.1");
  }

  public static URL randomURL(String ip) {
    return new URL(ip, randomPort());
  }

  public static int randomPort() {
    return randomPort(10000, 20000, null);
  }

  public static int randomPort(int min, int max, Set<Integer> different) {

    Random random = new Random();

    for (int i = min; i <= max; i++) {
      int port = min + random.nextInt(max - min + 1);
      if ((different == null || !different.contains(port)) && isUsable(port)) {
        return port;
      }
    }

    throw new IllegalStateException(String.format("random port not found:(%d, %d)", min, max));
  }

  public static int netmask = (1 << 8) - 1;

  public static String randomIp() {
    return String.format(
        "%d.%d.%d.%d",
        (Math.abs(random.nextInt()) % netmask + 1),
        (Math.abs(random.nextInt()) % netmask + 1),
        (Math.abs(random.nextInt()) % netmask + 1),
        (Math.abs(random.nextInt()) % netmask + 1));
  }

  protected static boolean isUsable(int port) {

    try (ServerSocket s = new ServerSocket()) {
      s.bind(new InetSocketAddress(port));
      return true;
    } catch (IOException e) {
    }
    return false;
  }

  public static String getDc() {
    return "DEFAULT_DC";
  }

  public static String randomString() {

    return randomString(1 << 10);
  }

  public static String randomString(int length) {

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < length; i++) {
      sb.append((char) ('a' + (int) (26 * Math.random())));
    }

    return sb.toString();
  }

  public static Client getRpcClient(
      ScheduledExecutorService scheduled, int responseDelayMilli, Object responseObj) {
    return new MockRpcClient()
        .setScheduled(scheduled)
        .setResponseDelayMilli(responseDelayMilli)
        .setResponseObj(responseObj);
  }

  public static Client getRpcClient(
      ScheduledExecutorService scheduled, int responseDelayMilli, Throwable th) {
    return new MockRpcClient()
        .setScheduled(scheduled)
        .setResponseDelayMilli(responseDelayMilli)
        .setPositive(false)
        .setThrowable(th);
  }

  public static Client getRpcClient(Object response, int responseDelayMilli) {
    return new MockRpcClient().setResponseObj(response).setResponseDelayMilli(responseDelayMilli);
  }

  public void printSlotTable(SlotTable slotTable) {
    try {
      logger.warn(
          "{}",
          JsonUtils.getJacksonObjectMapper()
              .writerWithDefaultPrettyPrinter()
              .writeValueAsString(slotTable));
    } catch (Exception ignore) {
    }
  }

  protected void assertSlotTableNoDupLeaderFollower(SlotTable slotTable) {
    slotTable
        .getSlotMap()
        .forEach(
            (slotId, slot) -> {
              Assert.assertFalse(slot.getFollowers().contains(slot.getLeader()));
            });
  }

  private void counterIncr(Map<String, Integer> counter, String ip) {
    Integer prev = counter.get(ip);
    counter.put(ip, prev == null ? 1 : prev + 1);
  }

  public static class MockRpcClient implements Client {

    private ObjectFactory<Object> response;

    private int responseDelayMilli;

    private ScheduledExecutorService scheduled;

    private Channel channel;

    private boolean isPositive = true;

    private Throwable throwable;

    @Override
    public Channel getChannel(URL url) {
      return null;
    }

    @Override
    public Channel connect(URL url) {
      return null;
    }

    @Override
    public Object sendSync(URL url, Object message, int timeoutMillis) {
      try {
        Thread.sleep(responseDelayMilli);
        throwIfNegative();
      } catch (Throwable e) {
        throw new SofaRegistryRuntimeException(e);
      }
      return response.create();
    }

    @Override
    public Object sendSync(Channel channel, Object message, int timeoutMillis) {
      try {
        Thread.sleep(responseDelayMilli);
        throwIfNegative();
      } catch (Throwable e) {
        throw new SofaRegistryRuntimeException(e);
      }
      return response.create();
    }

    @Override
    public void sendCallback(
        URL url, Object message, CallbackHandler callbackHandler, int timeoutMillis) {
      if (isPositive) {
        scheduled.schedule(
            () -> callbackHandler.onCallback(channel, response.create()),
            responseDelayMilli,
            TimeUnit.MILLISECONDS);
      } else {
        scheduled.schedule(
            () -> callbackHandler.onException(channel, throwable),
            responseDelayMilli,
            TimeUnit.MILLISECONDS);
      }
    }

    @Override
    public InetSocketAddress getLocalAddress() {
      return null;
    }

    @Override
    public void close() {}

    @Override
    public boolean isClosed() {
      return false;
    }

    private void throwIfNegative() throws Throwable {
      if (!isPositive) {
        if (throwable != null) {
          throw throwable;
        } else {
          throw new SofaRegistryRuntimeException("expected exception");
        }
      }
    }

    public MockRpcClient setResponse(ObjectFactory response) {
      this.response = response;
      return this;
    }

    public MockRpcClient setResponseObj(Object responseObj) {
      this.response =
          new ObjectFactory<Object>() {
            @Override
            public Object create() {
              return responseObj;
            }
          };
      return this;
    }

    public MockRpcClient setResponseDelayMilli(int responseDelayMilli) {
      this.responseDelayMilli = responseDelayMilli;
      return this;
    }

    public MockRpcClient setScheduled(ScheduledExecutorService scheduled) {
      this.scheduled = scheduled;
      return this;
    }

    public MockRpcClient setChannel(Channel channel) {
      this.channel = channel;
      return this;
    }

    public MockRpcClient setPositive(boolean positive) {
      isPositive = positive;
      return this;
    }

    public MockRpcClient setThrowable(Throwable throwable) {
      this.throwable = throwable;
      return this;
    }
  }

  protected static List<DataNode> randomDataNodes(int num) {
    List<DataNode> result = Lists.newArrayList();
    for (int i = 0; i < num; i++) {
      result.add(new DataNode(randomURL(randomIp()), getDc()));
    }
    return result;
  }

  public static SlotTable randomSlotTable() {
    return new SlotTableGenerator(randomDataNodes(3)).createSlotTable();
  }

  public static SlotTable randomSlotTable(List<DataNode> dataNodes) {
    return randomSlotTable(dataNodes, SlotConfig.SLOT_NUM, SlotConfig.SLOT_REPLICAS);
  }

  public static SlotTable randomSlotTable(List<DataNode> dataNodes, int slotNum, int slotReplicas) {
    return new SlotTableGenerator(dataNodes).createSlotTable(slotNum, slotReplicas);
  }

  protected SlotTable randomUnBalancedSlotTable(List<DataNode> dataNodes) {
    return new SlotTableGenerator(dataNodes).createLeaderUnBalancedSlotTable();
  }

  protected boolean isMoreBalanced(SlotTable before, SlotTable current, List<DataNode> dataNodes) {
    //        Map<String, Integer> beforeLeaderCount =
    // SlotTableUtils.getSlotTableLeaderCount(before);
    //        Map<String, Integer> currentLeaderCount =
    // SlotTableUtils.getSlotTableLeaderCount(current);
    //
    //        Map<String, Integer> beforeSlotsCount = SlotTableUtils.getSlotTableSlotCount(before);
    //        Map<String, Integer> currentSlotsCount =
    // SlotTableUtils.getSlotTableSlotCount(current);

    int maxLeaderGapBefore = maxLeaderGap(before, dataNodes);
    int maxLeaderGapCurrent = maxLeaderGap(current, dataNodes);
    int maxSlotsGapBefore = maxSlotGap(before, dataNodes);
    int maxSlotsGapCurrent = maxSlotGap(current, dataNodes);
    logger.info("[before leader gap] {}", maxLeaderGapBefore);
    logger.info("[current leader gap] {}", maxLeaderGapCurrent);
    logger.info("[before slots gap] {}", maxSlotsGapBefore);
    logger.info("[current slots gap] {}", maxSlotsGapCurrent);
    return maxLeaderGapBefore > maxLeaderGapCurrent || maxSlotsGapBefore > maxSlotsGapCurrent;
  }

  public static int maxLeaderGap(SlotTable slotTable, List<DataNode> dataNodes) {
    Map<String, Integer> counter = new HashMap<>(dataNodes.size());
    dataNodes.forEach(dataNode -> counter.put(dataNode.getIp(), 0));
    for (Map.Entry<Integer, Slot> entry : slotTable.getSlotMap().entrySet()) {
      int count = counter.get(entry.getValue().getLeader());
      counter.put(entry.getValue().getLeader(), count + 1);
    }
    return maxGap(counter);
  }

  public static int maxSlotGap(SlotTable slotTable, List<DataNode> dataNodes) {
    Map<String, Integer> counter = new HashMap<>(dataNodes.size());
    dataNodes.forEach(dataNode -> counter.put(dataNode.getIp(), 0));
    for (Map.Entry<Integer, Slot> entry : slotTable.getSlotMap().entrySet()) {
      int count = counter.get(entry.getValue().getLeader());
      counter.put(entry.getValue().getLeader(), count + 1);
      for (String dataServer : entry.getValue().getFollowers()) {
        count = counter.get(dataServer);
        counter.put(dataServer, count + 1);
      }
    }
    return maxGap(counter);
  }

  public static int maxGap(Map<String, Integer> stats) {
    int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
    for (int count : stats.values()) {
      min = Math.min(count, min);
      max = Math.max(count, max);
    }
    return max - min;
  }

  public static class SlotTableGenerator {

    private final List<DataNode> dataNodes;

    public SlotTableGenerator(List<DataNode> dataNodes) {
      this.dataNodes = dataNodes;
    }

    public SlotTable createSlotTable(int slotNum, int slotReplicas) {
      long epoch = DatumVersionUtil.nextId();
      Map<Integer, Slot> slotMap = generateSlotMap(slotNum, slotReplicas);
      return new SlotTable(epoch, slotMap.values());
    }

    public SlotTable createSlotTable() {
      return createSlotTable(SlotConfig.SLOT_NUM, SlotConfig.SLOT_REPLICAS);
    }

    public SlotTable createLeaderUnBalancedSlotTable() {
      long epoch = DatumVersionUtil.nextId();
      Map<Integer, Slot> slotMap = generateUnBalancedSlotMap();
      return new SlotTable(epoch, slotMap.values());
    }

    private Map<Integer, Slot> generateSlotMap(int slotNum, int slotReplicas) {
      Map<Integer, Slot> slotMap = Maps.newHashMap();
      for (int i = 0; i < slotNum; i++) {
        long epoch = DatumVersionUtil.nextId();
        String leader = getNextLeader().getIp();
        List<String> followers = Lists.newArrayList();
        for (int j = 0; j < slotReplicas - 1; j++) {
          followers.add(getNextFollower().getIp());
        }
        Slot slot = new Slot(i, leader, epoch, followers);
        slotMap.put(i, slot);
      }
      return slotMap;
    }

    private Map<Integer, Slot> generateUnBalancedSlotMap() {
      Map<Integer, Slot> slotMap = Maps.newHashMap();
      int leaderIndex = Math.abs(random.nextInt()) % dataNodes.size();
      String leader = dataNodes.get(leaderIndex).getIp();
      for (int i = 0; i < SlotConfig.SLOT_NUM; i++) {
        long epoch = DatumVersionUtil.nextId();
        List<String> followers = Lists.newArrayList();
        for (int j = 0; j < SlotConfig.SLOT_REPLICAS - 1; j++) {
          DataNode follower = getNextFollower();
          while (follower.getIp().equalsIgnoreCase(leader) || followers.contains(follower)) {
            follower = getNextUnbalancedFollower();
          }
          followers.add(follower.getIp());
        }
        Slot slot = new Slot(i, leader, epoch, followers);
        slotMap.put(i, slot);
      }
      return slotMap;
    }

    private AtomicInteger nextLeader = new AtomicInteger();

    private AtomicInteger nextFollower = new AtomicInteger(1);

    private DataNode getNextLeader() {
      return dataNodes.get(nextLeader.getAndIncrement() % dataNodes.size());
    }

    private DataNode getNextFollower() {
      return dataNodes.get(nextFollower.getAndIncrement() % dataNodes.size());
    }

    private DataNode getNextUnbalancedFollower() {
      return dataNodes.get(nextFollower.getAndIncrement() % dataNodes.size());
    }

    public SlotTableGenerator setNextLeader(int nextLeader) {
      this.nextLeader.set(nextLeader);
      return this;
    }

    public SlotTableGenerator setNextFollower(int nextFollower) {
      this.nextFollower.set(nextFollower);
      return this;
    }
  }

  public static int randomInt() {
    return random.nextInt();
  }

  public static int randomInt(int bound) {
    return random.nextInt(bound);
  }

  public Watcher randomWatcher() {
    Watcher watcher = new Watcher();
    initBaseInfo(watcher);
    return watcher;
  }

  public Publisher randomPublisher() {
    Publisher publisher = new Publisher();
    initBaseInfo(publisher);
    publisher.setPublishType(PublishType.NORMAL);
    publisher.setSessionProcessId(
        new ProcessId(randomIp(), System.currentTimeMillis(), randomInt(1024), randomInt()));
    return publisher;
  }

  public Subscriber randomSubscriber() {
    Subscriber subscriber = new Subscriber();
    initBaseInfo(subscriber);
    subscriber.setScope(ScopeEnum.dataCenter);
    subscriber.setElementType(ElementType.SUBSCRIBER);
    subscriber.setSourceAddress(new URL("192.168.0.1", new Random().nextInt(50000)));
    subscriber.setTargetAddress(new URL("192.168.0.2", new Random().nextInt(50000)));
    return subscriber;
  }

  public Subscriber randomSubscriber(String dataInfo, String instanceId) {
    Subscriber subscriber = randomSubscriber();
    subscriber.setDataId(dataInfo);
    subscriber.setInstanceId(instanceId);
    subscriber.setDataInfoId(
        DataInfo.toDataInfoId(
            subscriber.getDataId(), subscriber.getInstanceId(), subscriber.getGroup()));
    return subscriber;
  }

  public void initBaseInfo(BaseInfo baseInfo) {
    baseInfo.setAppName("app-" + randomInt(1024));
    baseInfo.setGroup("default-group");
    baseInfo.setClientRegisterTimestamp(System.currentTimeMillis());
    baseInfo.setClientVersion(BaseInfo.ClientVersion.StoreData);
    baseInfo.setDataId(randomString(20));
    baseInfo.setInstanceId(randomString(10));
    baseInfo.setRegisterId(randomIp());
    baseInfo.setProcessId(randomInt(1024) + "");
    baseInfo.setDataInfoId(
        DataInfo.toDataInfoId(baseInfo.getDataId(), baseInfo.getInstanceId(), baseInfo.getGroup()));
  }

  public static class SimpleNode implements Node {

    private String ip;

    public SimpleNode(String ip) {
      this.ip = ip;
    }

    @Override
    public NodeType getNodeType() {
      return NodeType.DATA;
    }

    @Override
    public URL getNodeUrl() {
      return new URL(ip);
    }
  }
}
