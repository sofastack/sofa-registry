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
package com.alipay.sofa.registry.server.meta;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.console.PersistenceDataBuilder;
import com.alipay.sofa.registry.common.model.metaserver.ClientManagerAddress;
import com.alipay.sofa.registry.common.model.metaserver.ClientManagerAddress.AddressVersion;
import com.alipay.sofa.registry.common.model.metaserver.ClientManagerResult;
import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotConfig;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.exception.SofaRegistryRuntimeException;
import com.alipay.sofa.registry.jdbc.domain.ProvideDataDomain;
import com.alipay.sofa.registry.jdbc.mapper.ProvideDataMapper;
import com.alipay.sofa.registry.remoting.CallbackHandler;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.Client;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.provide.data.NodeOperatingService;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataService;
import com.alipay.sofa.registry.server.meta.slot.balance.BalancePolicy;
import com.alipay.sofa.registry.server.meta.slot.balance.NaiveBalancePolicy;
import com.alipay.sofa.registry.server.shared.client.manager.ClientManagerService;
import com.alipay.sofa.registry.server.shared.config.CommonConfig;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.OperationStatus;
import com.alipay.sofa.registry.store.api.elector.AbstractLeaderElector.LeaderInfo;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.alipay.sofa.registry.util.JsonUtils;
import com.alipay.sofa.registry.util.MathUtils;
import com.alipay.sofa.registry.util.ObjectFactory;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.springframework.util.CollectionUtils;

/**
 * @author chen.zhu
 *     <p>Nov 18, 2020
 */
public class AbstractMetaServerTestBase extends AbstractTestBase {

  protected MetaLeaderService metaLeaderService;

  protected MetaServerConfig metaServerConfig;

  protected NodeConfig nodeConfig;

  protected CommonConfig commonConfig;

  @Rule public TestName name = new TestName();

  private BalancePolicy balancePolicy = new NaiveBalancePolicy();

  protected LeaderInfo leaderInfo =
      new LeaderInfo(
          System.currentTimeMillis(), "127.0.0.1", System.currentTimeMillis() + 10 * 1000);

  protected PersistenceData mockPersistenceData() {
    String dataInfoId =
        DataInfo.toDataInfoId("dataId" + System.currentTimeMillis(), "DEFAULT", "DEFAULT");
    return PersistenceDataBuilder.createPersistenceData(
        dataInfoId, "val" + System.currentTimeMillis());
  }

  @Before
  public void beforeAbstractMetaServerTest() {
    commonConfig = mock(CommonConfig.class);
    when(commonConfig.getLocalDataCenter()).thenReturn(getDc());

    metaLeaderService = mock(MetaLeaderService.class);
    nodeConfig = mock(NodeConfig.class);
    metaServerConfig = mock(MetaServerConfig.class);
    when(nodeConfig.getLocalDataCenter()).thenReturn(getDc());
  }

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

  public void makeMetaLeader() throws TimeoutException, InterruptedException {
    when(metaLeaderService.amILeader()).thenReturn(true);
    when(metaLeaderService.amIStableAsLeader()).thenReturn(true);
  }

  public void makeMetaNonLeader() throws TimeoutException, InterruptedException {
    when(metaLeaderService.amILeader()).thenReturn(false);
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

  protected boolean isSlotTableBalanced(SlotTable slotTable, List<DataNode> dataNodes) {
    Map<String, Integer> leaderCounter = Maps.newHashMap();
    for (Slot slot : slotTable.getSlots()) {
      counterIncr(leaderCounter, slot.getLeader());
    }
    Map<String, Integer> followerCounter = Maps.newHashMap();
    for (Slot slot : slotTable.getSlots()) {
      for (String follower : slot.getFollowers()) {
        counterIncr(followerCounter, follower);
      }
    }
    AtomicInteger leaderTotal = new AtomicInteger();
    leaderCounter.values().forEach(count -> leaderTotal.addAndGet(count));
    int leaderAverage = MathUtils.divideCeil(leaderTotal.get(), dataNodes.size());
    int leaderHighWaterMark = balancePolicy.getHighWaterMarkSlotLeaderNums(leaderAverage);

    AtomicInteger followerTotal = new AtomicInteger();
    followerCounter.values().forEach(count -> followerTotal.addAndGet(count));
    int followerAverage = MathUtils.divideCeil(followerTotal.get(), dataNodes.size());
    int followerHighWaterMark = balancePolicy.getHighWaterMarkSlotFollowerNums(followerAverage);

    logger.info("[isSlotTableBalanced][leaderCounter] {}", leaderCounter);
    logger.info("[isSlotTableBalanced][followerCounter] {}", followerCounter);
    for (DataNode dataNode : dataNodes) {
      if (leaderCounter.get(dataNode.getIp()) == null) {
        return false;
      }
      if (leaderCounter.get(dataNode.getIp()) > leaderHighWaterMark) {
        logger.info(
            "[highLeader] {}, {}, {}",
            dataNode.getIp(),
            leaderCounter.get(dataNode.getIp()),
            leaderHighWaterMark);
        return false;
      }

      if (followerCounter.get(dataNode.getIp()) > followerHighWaterMark) {
        logger.info(
            "[highFollower] {}, {}, {}",
            dataNode.getIp(),
            followerCounter.get(dataNode.getIp()),
            followerHighWaterMark);
        return false;
      }
    }
    return true;
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

  protected boolean isSlotTableLeaderBalanced(SlotTable slotTable, List<DataNode> dataNodes) {
    Map<String, Integer> counter = Maps.newHashMap();
    for (Slot slot : slotTable.getSlots()) {
      counterIncr(counter, slot.getLeader());
    }

    int average = slotTable.getSlotMap().size() / dataNodes.size();
    int lowWaterMark = balancePolicy.getLowWaterMarkSlotLeaderNums(average);

    logger.info("[isSlotTableLeaderBalanced][counter] {}", counter);
    for (DataNode dataNode : dataNodes) {
      if (counter.get(dataNode.getIp()) == null) {
        return false;
      }
      if (counter.get(dataNode.getIp()) < lowWaterMark) {
        return false;
      }
      //            if (counter.get(dataNode.getIp()) > highWaterMark) {
      //                return false;
      //            }
    }
    return true;
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

  public static class NodeComparator implements Comparator<Node> {

    @Override
    public int compare(Node o1, Node o2) {
      return o1.getNodeUrl().getIpAddress().compareTo(o2.getNodeUrl().getIpAddress());
    }
  }

  public static class InMemoryProvideDataRepo implements ProvideDataService {

    private Map<String, PersistenceData> localRepo = new ConcurrentHashMap<>();

    @Override
    public boolean saveProvideData(PersistenceData data) {
      localRepo.put(PersistenceDataBuilder.getDataInfoId(data), data);
      return true;
    }

    @Override
    public boolean saveProvideData(PersistenceData data, long expectVersion) {

      PersistenceData exist = localRepo.get(PersistenceDataBuilder.getDataInfoId(data));
      if (exist == null) {
        localRepo.put(PersistenceDataBuilder.getDataInfoId(data), data);
        return true;
      } else if (exist.getVersion() == expectVersion) {
        localRepo.put(PersistenceDataBuilder.getDataInfoId(data), data);
        return true;
      }
      return false;
    }

    @Override
    public DBResponse<PersistenceData> queryProvideData(String key) {
      PersistenceData value = localRepo.get(key);
      if (value != null) {
        return new DBResponse(value, OperationStatus.SUCCESS);
      } else {
        return new DBResponse(null, OperationStatus.NOTFOUND);
      }
    }

    @Override
    public boolean removeProvideData(String key) {
      PersistenceData remove = localRepo.remove(key);
      return remove != null;
    }

    @Override
    public void becomeLeader() {}

    @Override
    public void loseLeader() {}
  }

  public static class InMemoryNodeOperatingService extends NodeOperatingService {

    public InMemoryNodeOperatingService() {}

    public InMemoryNodeOperatingService(ProvideDataService provideDataService) {
      super(provideDataService);
    }
  }

  public class InMemoryClientManagerServiceRepo implements ClientManagerService {

    private final AtomicLong version = new AtomicLong(0L);

    private final AtomicReference<KeySetView> cache =
        new AtomicReference<>(new ConcurrentHashMap<>().newKeySet());

    @Override
    public ClientManagerResult clientOpen(Set<String> ipSet) {
      cache.get().removeAll(ipSet);
      return ClientManagerResult.buildSuccess(version.incrementAndGet());
    }

    @Override
    public ClientManagerResult clientOff(Set<String> ipSet) {
      cache.get().addAll(ipSet);
      return ClientManagerResult.buildSuccess(version.incrementAndGet());
    }

    @Override
    public ClientManagerResult clientOffWithSub(Set<AddressVersion> address) {

      cache
          .get()
          .addAll(address.stream().map(AddressVersion::getAddress).collect(Collectors.toSet()));
      return ClientManagerResult.buildSuccess(version.incrementAndGet());
    }

    @Override
    public DBResponse<ClientManagerAddress> queryClientOffAddress() {
      Map<String, AddressVersion> clientOffAddress =
          Maps.newHashMapWithExpectedSize(cache.get().size());
      for (Object address : cache.get()) {
        clientOffAddress.put(
            (String) address,
            new AddressVersion(System.currentTimeMillis(), (String) address, true));
      }

      ClientManagerAddress resp =
          new ClientManagerAddress(version.get(), clientOffAddress, Collections.EMPTY_SET);
      return DBResponse.ok(resp).build();
    }

    @Override
    public ClientManagerResult reduce(Set<String> ipSet) {
      cache.get().removeAll(ipSet);
      return ClientManagerResult.buildSuccess(version.incrementAndGet());
    }

    @Override
    public void waitSynced() {}
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

  public static class InMemoryProvideDataMapper implements ProvideDataMapper {

    private Map<String /*dataCenter*/, Map<String /*dataKey*/, ProvideDataDomain>> localRepo =
        new ConcurrentHashMap<>();

    @Override
    public synchronized int save(ProvideDataDomain data) {
      Date date = new Date();
      data.setGmtCreate(date);
      data.setGmtModified(date);

      Map<String, ProvideDataDomain> map =
          localRepo.computeIfAbsent(data.getDataCenter(), k -> Maps.newConcurrentMap());
      map.put(data.getDataKey(), data);
      return 1;
    }

    @Override
    public synchronized int update(ProvideDataDomain data, long exceptVersion) {
      Map<String, ProvideDataDomain> map = localRepo.get(data.getDataCenter());
      if (CollectionUtils.isEmpty(map)) {
        return 0;
      }
      ProvideDataDomain provideDataDomain = map.get(data.getDataKey());
      if (provideDataDomain == null || provideDataDomain.getDataVersion() != exceptVersion) {
        return 0;
      }
      provideDataDomain.setDataValue(data.getDataValue());
      provideDataDomain.setDataVersion(data.getDataVersion());
      provideDataDomain.setGmtModified(new Date());
      return 1;
    }

    @Override
    public synchronized ProvideDataDomain query(String dataCenter, String dataKey) {
      Map<String, ProvideDataDomain> map = localRepo.get(dataCenter);
      return CollectionUtils.isEmpty(map) ? null : map.get(dataKey);
    }

    @Override
    public synchronized int remove(String dataCenter, String dataKey, long dataVersion) {
      Map<String, ProvideDataDomain> map = localRepo.get(dataCenter);
      if (CollectionUtils.isEmpty(map)) {
        return 0;
      }
      ProvideDataDomain provideDataDomain = map.get(dataKey);
      if (provideDataDomain == null || provideDataDomain.getDataVersion() != dataVersion) {
        return 0;
      }
      map.remove(dataKey);
      return 1;
    }

    @Override
    public synchronized List<ProvideDataDomain> queryByPage(
        String dataCenter, int start, int limit) {
      // not implement
      return null;
    }

    @Override
    public synchronized int selectTotalCount(String dataCenter) {
      Map<String, ProvideDataDomain> map = localRepo.get(dataCenter);
      return CollectionUtils.isEmpty(map) ? 0 : map.size();
    }
  }
}
