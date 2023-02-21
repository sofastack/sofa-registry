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
import com.alipay.sofa.registry.common.model.appmeta.InterfaceMapping;
import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.console.PersistenceDataBuilder;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotConfig;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.common.model.store.*;
import com.alipay.sofa.registry.core.model.AppRevisionInterface;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.exception.SofaRegistryRuntimeException;
import com.alipay.sofa.registry.remoting.CallbackHandler;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.Client;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfigBean;
import com.alipay.sofa.registry.server.session.circuit.breaker.CircuitBreakerService;
import com.alipay.sofa.registry.store.api.meta.ProvideDataRepository;
import com.alipay.sofa.registry.store.api.repository.AppRevisionRepository;
import com.alipay.sofa.registry.store.api.repository.InterfaceAppsRepository;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.alipay.sofa.registry.util.JsonUtils;
import com.alipay.sofa.registry.util.ObjectFactory;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
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
import org.springframework.util.CollectionUtils;

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

  public List<AppRevision> buildAppRevisions(int size, String itfPrefix) {
    List<AppRevision> appRevisionList = new ArrayList<>();
    for (int i = 1; i <= size; i++) {
      long l = System.currentTimeMillis();
      String suffix = l + "-" + i;

      String appname = "foo" + suffix;
      String revision = "1111" + suffix;

      AppRevision appRevision = new AppRevision();
      appRevision.setAppName(appname);
      appRevision.setRevision(revision);
      appRevision.setClientVersion("1.0");

      Map<String, List<String>> baseParams = Maps.newHashMap();
      baseParams.put(
          "metaBaseParam1", com.google.common.collect.Lists.newArrayList("metaBaseValue1"));
      appRevision.setBaseParams(baseParams);

      Map<String, AppRevisionInterface> interfaceMap = Maps.newHashMap();
      String dataInfo1 =
          DataInfo.toDataInfoId(
              itfPrefix + "func1" + suffix,
              ValueConstants.DEFAULT_GROUP,
              ValueConstants.DEFAULT_INSTANCE_ID);
      String dataInfo2 =
          DataInfo.toDataInfoId(
              itfPrefix + "func2" + suffix,
              ValueConstants.DEFAULT_GROUP,
              ValueConstants.DEFAULT_INSTANCE_ID);

      AppRevisionInterface inf1 = new AppRevisionInterface();
      AppRevisionInterface inf2 = new AppRevisionInterface();
      interfaceMap.put(dataInfo1, inf1);
      interfaceMap.put(dataInfo2, inf2);
      appRevision.setInterfaceMap(interfaceMap);

      inf1.setId("1");
      Map<String, List<String>> serviceParams1 = new HashMap<String, List<String>>();
      serviceParams1.put("metaParam2", com.google.common.collect.Lists.newArrayList("metaValue2"));
      inf1.setServiceParams(serviceParams1);

      inf2.setId("2");
      Map<String, List<String>> serviceParams2 = new HashMap<String, List<String>>();
      serviceParams1.put("metaParam3", com.google.common.collect.Lists.newArrayList("metaValues3"));
      inf1.setServiceParams(serviceParams2);

      appRevisionList.add(appRevision);
    }
    return appRevisionList;
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

  public static class InMemoryCircuitBreakerService implements CircuitBreakerService {

    @Override
    public boolean pushCircuitBreaker(CircuitBreakerStatistic statistic, boolean hasPushed) {
      return false;
    }

    /**
     * statistic when push success
     *
     * @param versions dataCenter -> version
     * @param pushNums dataCenter -> pushNum
     * @param subscriber
     * @return
     */
    @Override
    public boolean onPushSuccess(
        Map<String, Long> versions, Map<String, Integer> pushNums, Subscriber subscriber) {
      return subscriber.checkAndUpdateCtx(versions, pushNums);
    }

    /**
     * statistic when push fail
     *
     * @param versions
     * @param subscriber
     * @return
     */
    @Override
    public boolean onPushFail(Map<String, Long> versions, Subscriber subscriber) {
      return subscriber.onPushFail(versions);
    }
  }

  public static class InMemoryProvideDataRepository implements ProvideDataRepository {

    private Map<String, PersistenceData> localRepo = new ConcurrentHashMap<>();

    @Override
    public boolean put(PersistenceData persistenceData) {
      localRepo.put(PersistenceDataBuilder.getDataInfoId(persistenceData), persistenceData);
      return true;
    }

    @Override
    public boolean put(PersistenceData data, long expectVersion) {
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
    public PersistenceData get(String key) {
      return localRepo.get(key);
    }

    @Override
    public boolean remove(String key, long version) {
      PersistenceData remove = localRepo.remove(key);
      return remove != null;
    }

    @Override
    public Map<String, PersistenceData> getAll() {
      return Maps.newHashMap(localRepo);
    }
  }

  public static class InMemoryAppRevisionRepository implements AppRevisionRepository {

    private static final Map<String, AppRevision> revisions = Maps.newConcurrentMap();

    private Set<String> dataCenters = Sets.newHashSet();

    private InterfaceAppsRepository interfaceAppsRepository;

    /**
     * Setter method for property <tt>interfaceAppsRepository</tt>.
     *
     * @param interfaceAppsRepository value to be assigned to property interfaceAppsRepository
     */
    public void setInterfaceAppsRepository(InterfaceAppsRepository interfaceAppsRepository) {
      this.interfaceAppsRepository = interfaceAppsRepository;
    }

    /**
     * persistence appRevision
     *
     * @param appRevision
     */
    @Override
    public void register(AppRevision appRevision) throws Exception {
      interfaceAppsRepository.register(
          appRevision.getAppName(), appRevision.getInterfaceMap().keySet());
      revisions.putIfAbsent(appRevision.getRevision(), appRevision);
    }

    /**
     * check if revisionId exist
     *
     * @param revisionId
     * @return
     */
    @Override
    public boolean exist(String revisionId) {
      return false;
    }

    /**
     * get AppRevision
     *
     * @param revision
     * @return
     */
    @Override
    public AppRevision queryRevision(String revision) {
      return revisions.get(revision);
    }

    @Override
    public boolean heartbeat(String revision) {
      return true;
    }

    @Override
    public boolean heartbeatDB(String revision) {
      return false;
    }

    @Override
    public Collection<String> availableRevisions() {
      return revisions.keySet();
    }

    @Override
    public List<AppRevision> listFromStorage(long start, int limit) {
      return Lists.newArrayList(revisions.values());
    }

    @Override
    public void startSynced() {}

    @Override
    public void waitSynced() {}

    @Override
    public List<AppRevision> getExpired(Date beforeTime, int limit) {
      return Collections.emptyList();
    }

    @Override
    public void replace(AppRevision appRevision) {}

    @Override
    public int cleanDeleted(Date beforeTime, int limit) {
      return 0;
    }

    @Override
    public Map<String, Integer> countByApp() {
      Map<String, Integer> countMap = Maps.newHashMap();
      for (Entry<String, AppRevision> entry : revisions.entrySet()) {
        Integer count =
            countMap.computeIfAbsent(entry.getValue().getAppName(), k -> new Integer(0));
        count++;
        countMap.put(entry.getValue().getAppName(), count);
      }
      return countMap;
    }

    @Override
    public Set<String> allRevisionIds() {
      return revisions.keySet();
    }

    @Override
    public Set<String> dataCenters() {
      return dataCenters;
    }

    @Override
    public void setDataCenters(Set<String> dataCenters) {
      this.dataCenters = dataCenters;
    }
  }

  public static class InMemoryInterfaceAppsRepository implements InterfaceAppsRepository {
    public static final String LOCAL_DATACENTER = "LOCAL_DATACENTER";

    private Set<String> dataCenters = Sets.newHashSet();

    // <dataInfoId, <dataCenter, InterfaceMapping>>
    private static final Map<String, Map<String, InterfaceMapping>> mapping = Maps.newHashMap();
    /**
     * get revisions by interfaceName
     *
     * @param dataInfoId
     * @return return <appName, revisions>
     */
    @Override
    public synchronized InterfaceMapping getAppNames(String dataInfoId) {
      Map<String, InterfaceMapping> mappings = mapping.get(dataInfoId);

      if (CollectionUtils.isEmpty(mappings)) {
        return new InterfaceMapping(-1);
      }

      long maxVersion = -1L;
      Set<String> apps = Sets.newHashSet();
      for (InterfaceMapping value : mappings.values()) {
        if (value.getNanosVersion() > maxVersion) {
          maxVersion = value.getNanosVersion();
        }
        apps.addAll(value.getApps());
      }
      InterfaceMapping ret = new InterfaceMapping(maxVersion, apps);
      return ret;
    }

    @Override
    public synchronized void register(String appName, Set<String> interfaceNames) {
      for (String interfaceName : interfaceNames) {
        Map<String, InterfaceMapping> map =
            mapping.computeIfAbsent(interfaceName, k -> Maps.newHashMap());

        InterfaceMapping interfaceMapping =
            map.computeIfAbsent(LOCAL_DATACENTER, k -> new InterfaceMapping(System.nanoTime()));
        map.put(LOCAL_DATACENTER, interfaceMapping.addApp(System.nanoTime(), appName));
      }
    }

    @Override
    public void renew(String interfaceName, String appName) {}

    @Override
    public void startSynced() {}

    @Override
    public void waitSynced() {}

    @Override
    public long getDataVersion() {
      return 0;
    }

    @Override
    public Map<String, Map<String, InterfaceMapping>> allServiceMapping() {
      return Maps.newHashMap(mapping);
    }

    @Override
    public Set<String> dataCenters() {
      return dataCenters;
    }

    @Override
    public void setDataCenters(Set<String> dataCenters) {
      this.dataCenters = dataCenters;
    }
  }
}
