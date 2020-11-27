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

import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.exception.SofaRegistryRuntimeException;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.remoting.CallbackHandler;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.Client;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfigBean;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.executor.ExecutorManager;
import com.alipay.sofa.registry.server.meta.metaserver.CurrentDcMetaServer;
import com.alipay.sofa.registry.server.meta.remoting.RaftExchanger;
import com.alipay.sofa.registry.util.FileUtils;
import com.alipay.sofa.registry.util.NamedThreadFactory;
import com.alipay.sofa.registry.util.ObjectFactory;
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
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

import static org.mockito.Mockito.mock;

/**
 * @author chen.zhu
 * <p>
 * Nov 18, 2020
 */
public class AbstractTest {

    protected Logger                   logger = LoggerFactory.getLogger(AbstractTest.class);

    protected ExecutorService          executors;

    protected ScheduledExecutorService scheduled;

    @Rule
    public TestName                    name   = new TestName();

    public static final Random random = new Random();

    private AtomicReference<RaftExchanger> raftExchangerRef = new AtomicReference<>();

    @Before
    public void beforeAbstractTest() throws Exception {

        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
        Thread.interrupted();//clear interrupt

        executors = Executors.newCachedThreadPool(new NamedThreadFactory(name.getMethodName()));
        scheduled = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors(),
            new NamedThreadFactory("sched-" + name.getMethodName()));
        FileUtils.cleanDirectory(new File(System.getProperty("user.home") + File.separator + "raftData"));
        logger.info(remarkableMessage("[begin test][{}]"), name.getMethodName());
    }

    @After
    public void afterAbstractTest() throws IOException {
        if(raftExchangerRef.get() != null) {
            raftExchangerRef.get().shutdown();
        }
        executors.shutdownNow();
        scheduled.shutdownNow();
        FileUtils.cleanDirectory(new File(System.getProperty("user.home") + File.separator + "raftData"));

        logger.info(remarkableMessage("[end test][{}]"), name.getMethodName());
    }

    public RaftExchanger startRaftExchanger() {
        if(!raftExchangerRef.compareAndSet(null, new RaftExchanger())) {
            return raftExchangerRef.get();
        }
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
        CurrentDcMetaServer metaServer = mock(CurrentDcMetaServer.class);
        raftExchanger.setMetaServerConfig(config).setNodeConfig(nodeConfig).setCurrentDcMetaServer(metaServer);
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

    protected void waitConditionUntilTimeOut(BooleanSupplier booleanSupplier, int waitTimeMilli)
                                                                                                throws TimeoutException,
                                                                                                InterruptedException {

        waitConditionUntilTimeOut(booleanSupplier, waitTimeMilli, 2);
    }

    protected void waitConditionUntilTimeOut(BooleanSupplier booleanSupplier, int waitTimeMilli,
                                             int intervalMilli) throws TimeoutException,
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
        return String.format("%d.%d.%d.%d", (Math.abs(random.nextInt()) % netmask + 1), (Math.abs(random.nextInt()) % netmask + 1),
                (Math.abs(random.nextInt()) % netmask + 1), (Math.abs(random.nextInt()) % netmask + 1));
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

    public static Client getRpcClient(ScheduledExecutorService scheduled, int responseDelayMilli, Object responseObj) {
        return new MockRpcClient().setScheduled(scheduled).setResponseDelayMilli(responseDelayMilli).setResponseObj(responseObj);
    }

    public static Client getRpcClient(ScheduledExecutorService scheduled, int responseDelayMilli, Throwable th) {
        return new MockRpcClient().setScheduled(scheduled)
                .setResponseDelayMilli(responseDelayMilli).setPositive(false).setThrowable(th);
    }

    public static Client getRpcClient(Object response, int responseDelayMilli) {
        return new MockRpcClient().setResponseObj(response)
                .setResponseDelayMilli(responseDelayMilli);
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
        public void sendCallback(URL url, Object message, CallbackHandler callbackHandler, int timeoutMillis) {
            if(isPositive) {
                scheduled.schedule(() -> callbackHandler.onCallback(channel, response.create()), responseDelayMilli, TimeUnit.MILLISECONDS);
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
            if(!isPositive) {
                if(throwable != null) {
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
}
