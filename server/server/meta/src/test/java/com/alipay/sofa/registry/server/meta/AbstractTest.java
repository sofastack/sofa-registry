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

import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.storage.impl.RocksDBLogStorage;
import com.alipay.sofa.jraft.util.StorageOptionsFactory;
import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotConfig;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.exception.SofaRegistryRuntimeException;
import com.alipay.sofa.registry.jraft.bootstrap.ServiceStateMachine;
import com.alipay.sofa.registry.jraft.processor.LeaderProcessListener;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.observer.Observable;
import com.alipay.sofa.registry.observer.Observer;
import com.alipay.sofa.registry.remoting.CallbackHandler;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.Client;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfigBean;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.executor.ExecutorManager;
import com.alipay.sofa.registry.server.meta.metaserver.impl.DefaultCurrentDcMetaServer;
import com.alipay.sofa.registry.server.meta.remoting.RaftExchanger;
import com.alipay.sofa.registry.util.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Maps;
import io.netty.util.ResourceLeakDetector;
import org.apache.commons.lang.reflect.FieldUtils;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

import static org.mockito.Mockito.mock;

/**
 * @author chen.zhu
 * <p>
 * Nov 18, 2020
 */
public class AbstractTest {

    protected final Logger                 logger           = LoggerFactory
                                                                .getLogger(getClass());

    protected ExecutorService              executors;

    protected ScheduledExecutorService     scheduled;

    @Rule
    public TestName                        name             = new TestName();

    public static final Random             random           = new Random();

    private AtomicReference<RaftExchanger> raftExchangerRef = new AtomicReference<>();

    private String                         raftMiddlePath;

    @Before
    public void beforeAbstractTest() throws Exception {

        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
        Thread.interrupted();//clear interrupt

        executors = Executors.newCachedThreadPool(new NamedThreadFactory(name.getMethodName()));
        scheduled = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors(),
            new NamedThreadFactory("sched-" + name.getMethodName()));
        if (logger.isInfoEnabled()) {
            logger.info(remarkableMessage("[begin test][{}]"), name.getMethodName());
        }
    }

    @After
    public void afterAbstractTest() throws IOException, NoSuchMethodException,
                                   InvocationTargetException, IllegalAccessException {
        if (raftExchangerRef.get() != null) {
            raftExchangerRef.get().getRaftServer().shutdown();
            raftExchangerRef.get().getRaftClient().shutdown();
            raftExchangerRef.get().shutdown();
            com.alipay.sofa.jraft.Node node = raftExchangerRef.get().getRaftServer().getNode();
            Field field = FieldUtils.getDeclaredField(node.getClass(), "logStorage", true);
            RocksDBLogStorage storage = (RocksDBLogStorage) field.get(node);
            storage.reset(Math.abs(random.nextInt()));
            node.shutdown();
            Method method = node.getClass().getDeclaredMethod("afterShutdown");
            method.setAccessible(true);
            method.invoke(node);

            FileUtils.forceDelete(new File(System.getProperty("user.home") + File.separator
                                           + "raftData"));
        }
        executors.shutdownNow();
        scheduled.shutdownNow();

        logger.info(remarkableMessage("[end test][{}]"), name.getMethodName());
    }

    public static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, newValue);
    }

    public RaftExchanger startRaftExchanger() throws Exception {

        FileUtils.deleteDirectory(new File(System.getProperty("user.home") + File.separator
                                           + "raftData"));
        Field field = FieldUtils.getField(StorageOptionsFactory.class, "tableFormatConfigTable",
            true);
        field.setAccessible(true);
        setFinalStatic(field, Maps.newConcurrentMap());

        raftExchangerRef.set(new RaftExchanger());

        RaftExchanger raftExchanger = raftExchangerRef.get();
        MetaServerConfig config = new MetaServerConfigBean();

        Map<String, Collection<String>> metaServers = Maps.newHashMap();
        metaServers.put(getDc(), Lists.newArrayList(NetUtil.getLocalAddress().getHostAddress()));
        NodeConfig nodeConfig = new NodeConfig() {
            @Override
            public Map<String, Collection<String>> getMetaNode() {
                return metaServers;
            }

            @Override
            public Map<String, Collection<String>> getMetaNodeIP() {
                return metaServers;
            }

            @Override
            public String getLocalDataCenter() {
                return getDc();
            }

            @Override
            public String getMetaDataCenter(String metaIpAddress) {
                return getDc();
            }

            @Override
            public Set<String> getDataCenterMetaServers(String dataCenter) {
                return Sets.newHashSet(metaServers.get(dataCenter));
            }
        };
        DefaultCurrentDcMetaServer metaServer = mock(DefaultCurrentDcMetaServer.class);
        raftExchanger.setMetaServerConfig(config).setNodeConfig(nodeConfig)
            .setCurrentDcMetaServer(metaServer);
        ExecutorManager executorManager = mock(ExecutorManager.class);
        raftExchanger.startRaftClient();
        raftExchanger.startRaftServer(executorManager);
        return raftExchanger;
    }

    protected String remarkableMessage(String msg) {
        return String.format("--------------------------%s--------------------------\r\n", msg);
    }

    protected void waitForAnyKeyToExit() throws IOException {
        logger.info("type any key to exit..................");
        waitForAnyKey();
    }

    protected void waitForAnyKey() throws IOException {
        System.in.read();
    }

    protected void waitConditionUntilTimeOut(BooleanSupplier booleanSupplier)
                                                                             throws TimeoutException,
                                                                             InterruptedException {

        waitConditionUntilTimeOut(booleanSupplier, 5000, 2);
    }

    protected static void waitConditionUntilTimeOut(BooleanSupplier booleanSupplier,
                                                    int waitTimeMilli) throws TimeoutException,
                                                                      InterruptedException {

        waitConditionUntilTimeOut(booleanSupplier, waitTimeMilli, 2);
    }

    protected static void waitConditionUntilTimeOut(BooleanSupplier booleanSupplier,
                                                    int waitTimeMilli, int intervalMilli)
                                                                                         throws TimeoutException,
                                                                                         InterruptedException {

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

    protected URL randomURL() {
        return randomURL("127.0.0.1");
    }

    protected URL randomURL(String ip) {
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
        return String.format("%d.%d.%d.%d", (Math.abs(random.nextInt()) % netmask + 1),
            (Math.abs(random.nextInt()) % netmask + 1), (Math.abs(random.nextInt()) % netmask + 1),
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

    public static Client getRpcClient(ScheduledExecutorService scheduled, int responseDelayMilli,
                                      Object responseObj) {
        return new MockRpcClient().setScheduled(scheduled)
            .setResponseDelayMilli(responseDelayMilli).setResponseObj(responseObj);
    }

    public static Client getRpcClient(ScheduledExecutorService scheduled, int responseDelayMilli,
                                      Throwable th) {
        return new MockRpcClient().setScheduled(scheduled)
            .setResponseDelayMilli(responseDelayMilli).setPositive(false).setThrowable(th);
    }

    public static Client getRpcClient(Object response, int responseDelayMilli) {
        return new MockRpcClient().setResponseObj(response).setResponseDelayMilli(
            responseDelayMilli);
    }

    public void makeRaftLeader() throws TimeoutException, InterruptedException {
        ServiceStateMachine.getInstance().setExecutor((ThreadPoolExecutor) executors);
        ServiceStateMachine.getInstance().setLeaderProcessListener(new LeaderProcessListener() {
            @Override
            public void startProcess() {

            }

            @Override
            public void stopProcess() {

            }
        });
        ServiceStateMachine.getInstance().onLeaderStart(100);
        waitConditionUntilTimeOut(()-> ServiceStateMachine.getInstance().isLeader(), 500);
    }

    public void makeRaftNonLeader() throws TimeoutException, InterruptedException {
        ServiceStateMachine.getInstance().setExecutor((ThreadPoolExecutor) executors);
        ServiceStateMachine.getInstance().setLeaderProcessListener(new LeaderProcessListener() {
            @Override
            public void startProcess() {

            }

            @Override
            public void stopProcess() {

            }
        });
        ServiceStateMachine.getInstance().onLeaderStop(Status.OK());
        waitConditionUntilTimeOut(()-> !ServiceStateMachine.getInstance().isLeader(), 500);
    }

    public void printSlotTable(SlotTable slotTable) {
        try {
            logger.warn("{}", JsonUtils.getJacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(slotTable));
        } catch (Exception ignore) {}
    }

    protected boolean isSlotTableBalanced(SlotTable slotTable, List<DataNode> dataNodes) {
        Map<String, Integer> counter = Maps.newHashMap();
        for(Slot slot : slotTable.getSlots()) {
            counterIncr(counter, slot.getLeader());
            for(String follower : slot.getFollowers()) {
                counterIncr(counter, follower);
            }
        }
        AtomicInteger total = new AtomicInteger();
        counter.values().forEach(count -> total.addAndGet(count));
        int average = total.get() / dataNodes.size();
        int lowWaterMark = average * 1 / 2;
        int highWaterMark = average * 3 / 2;

        logger.info("[counter] {}", counter);
        for(DataNode dataNode : dataNodes) {
            if(counter.get(dataNode.getIp()) == null) {
                return false;
            }
            if(counter.get(dataNode.getIp()) < lowWaterMark) {
                logger.info("[lower] {}, {}, {}", dataNode.getIp(), counter.get(dataNode.getIp()), lowWaterMark);
                return false;
            }
            if(counter.get(dataNode.getIp()) > highWaterMark) {
                return false;
            }
        }
        return true;
    }

    private void counterIncr(Map<String, Integer> counter, String ip) {
        Integer prev = counter.get(ip);
        counter.put(ip, prev == null ? 1 : prev + 1);
    }

    protected boolean isSlotTableLeaderBalanced(SlotTable slotTable, List<DataNode> dataNodes) {
        Map<String, Integer> counter = Maps.newHashMap();
        for(Slot slot : slotTable.getSlots()) {
            counterIncr(counter, slot.getLeader());
        }

        int average = slotTable.getSlotMap().size() / dataNodes.size();
        int lowWaterMark = average * 1 / 2;
        int highWaterMark = average * 3 / 2;

        for(DataNode dataNode : dataNodes) {
            if(counter.get(dataNode.getIp()) == null) {
                return false;
            }
            if(counter.get(dataNode.getIp()) < lowWaterMark) {
                return false;
            }
            if(counter.get(dataNode.getIp()) > highWaterMark) {
                return false;
            }
        }
        return true;
    }

    public static class MockRpcClient implements Client {

        private ObjectFactory<Object>    response;

        private int                      responseDelayMilli;

        private ScheduledExecutorService scheduled;

        private Channel                  channel;

        private boolean                  isPositive = true;

        private Throwable                throwable;

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
        public void sendCallback(URL url, Object message, CallbackHandler callbackHandler, int timeoutMillis) {
            if(isPositive) {
                scheduled.schedule(() ->
                                callbackHandler.onCallback(channel,
                        response.create()),
                        responseDelayMilli,
                        TimeUnit.MILLISECONDS);
            } else {
                scheduled.schedule(() -> callbackHandler.onException(channel, throwable), responseDelayMilli, TimeUnit.MILLISECONDS);
            }
        }

        @Override
        public InetSocketAddress getLocalAddress() {
            return null;
        }

        @Override
        public void close() {

        }

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
            this.response = new ObjectFactory<Object>() {
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

    public static class NotifyObserversCounter implements Observer {

        private final AtomicInteger counter = new AtomicInteger(0);

        @Override
        public void update(Observable source, Object message) {
            counter.getAndIncrement();
        }

        public int getCounter() {
            return counter.get();
        }
    }

    public static class ConcurrentExecutor implements Executor {

        private final int             tasks;

        private final CyclicBarrier   barrier;

        private final CountDownLatch  latch;

        private final ExecutorService executors;

        public ConcurrentExecutor(int tasks, ExecutorService executors) {
            this.tasks = tasks;
            this.barrier = new CyclicBarrier(tasks);
            this.latch = new CountDownLatch(tasks);
            this.executors = executors;
        }

        @Override
        public void execute(final Runnable command) {
            for (int i = 0; i < tasks; i++) {
                executors.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            barrier.await();
                            command.run();
                        } catch (Exception ignore) {
                        }

                        latch.countDown();
                    }
                });
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected SlotTable randomSlotTable() {
        List<DataNode> dataNodes = Lists.newArrayList(new DataNode(randomURL(randomIp()), getDc()),
            new DataNode(randomURL(randomIp()), getDc()), new DataNode(randomURL(randomIp()),
                getDc()));
        return new SlotTableGenerator(dataNodes).createSlotTable();
    }

    public static class SlotTableGenerator {

        private final List<DataNode> dataNodes;

        public SlotTableGenerator(List<DataNode> dataNodes) {
            this.dataNodes = dataNodes;
        }

        public SlotTable createSlotTable() {
            long epoch = DatumVersionUtil.nextId();
            Map<Integer, Slot> slotMap = generateSlotMap();
            return new SlotTable(epoch, slotMap);
        }

        public SlotTable createLeaderUnBalancedSlotTable() {
            long epoch = DatumVersionUtil.nextId();
            Map<Integer, Slot> slotMap = generateUnBalancedSlotMap();
            return new SlotTable(epoch, slotMap);
        }

        private Map<Integer, Slot> generateSlotMap() {
            Map<Integer, Slot> slotMap = Maps.newHashMap();
            for (int i = 0; i < SlotConfig.SLOT_NUM; i++) {
                long epoch = System.currentTimeMillis();
                String leader = getNextLeader().getIp();
                List<String> followers = Lists.newArrayList();
                for (int j = 0; j < SlotConfig.SLOT_REPLICAS; j++) {
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
            for (int i = 0; i < SlotConfig.SLOT_NUM; i++) {
                long epoch = System.currentTimeMillis();
                String leader = dataNodes.get(leaderIndex).getIp();
                List<String> followers = Lists.newArrayList();
                for (int j = 0; j < SlotConfig.SLOT_REPLICAS; j++) {
                    DataNode follower = getNextFollower();
                    while (follower.getIp().equalsIgnoreCase(dataNodes.get(leaderIndex).getIp())) {
                        follower = getNextFollower();
                    }
                    followers.add(follower.getIp());
                }
                Slot slot = new Slot(i, leader, epoch, followers);
                slotMap.put(i, slot);
            }
            return slotMap;
        }

        private AtomicInteger nextLeader   = new AtomicInteger();

        private AtomicInteger nextFollower = new AtomicInteger(1);

        private DataNode getNextLeader() {
            return dataNodes.get(nextLeader.getAndIncrement() % dataNodes.size());
        }

        private DataNode getNextFollower() {
            return dataNodes.get(nextFollower.getAndIncrement() % dataNodes.size());
        }
    }
}
