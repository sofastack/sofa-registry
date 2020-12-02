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
package com.alipay.sofa.registry.server.meta.bootstrap;

import com.alipay.sofa.registry.jraft.service.PersistenceDataDBService;
import com.alipay.sofa.registry.remoting.bolt.exchange.BoltExchange;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.remoting.exchange.NodeExchanger;
import com.alipay.sofa.registry.remoting.jersey.exchange.JerseyExchange;
import com.alipay.sofa.registry.server.meta.bootstrap.bean.lifecycle.RaftAnnotationBeanPostProcessor;
import com.alipay.sofa.registry.server.meta.bootstrap.bean.lifecycle.SmartSpringLifecycleController;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfigBean;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfigBeanProperty;
import com.alipay.sofa.registry.server.meta.executor.ExecutorManager;
import com.alipay.sofa.registry.server.meta.lease.impl.CrossDcMetaServerManager;
import com.alipay.sofa.registry.server.meta.lease.impl.DefaultDataServerManager;
import com.alipay.sofa.registry.server.meta.lease.impl.DefaultMetaServerManager;
import com.alipay.sofa.registry.server.meta.lease.impl.DefaultSessionManager;
import com.alipay.sofa.registry.server.meta.metaserver.CurrentDcMetaServer;
import com.alipay.sofa.registry.server.meta.metaserver.impl.DefaultCurrentDcMetaServer;
import com.alipay.sofa.registry.server.meta.remoting.DataNodeExchanger;
import com.alipay.sofa.registry.server.meta.remoting.RaftExchanger;
import com.alipay.sofa.registry.server.meta.remoting.SessionNodeExchanger;
import com.alipay.sofa.registry.server.meta.remoting.connection.DataConnectionHandler;
import com.alipay.sofa.registry.server.meta.remoting.connection.MetaConnectionHandler;
import com.alipay.sofa.registry.server.meta.remoting.connection.SessionConnectionHandler;
import com.alipay.sofa.registry.server.meta.remoting.handler.AbstractServerHandler;
import com.alipay.sofa.registry.server.meta.remoting.handler.FetchProvideDataRequestHandler;
import com.alipay.sofa.registry.server.meta.remoting.handler.HeartbeatRequestHandler;
import com.alipay.sofa.registry.server.meta.resource.*;
import com.alipay.sofa.registry.server.meta.slot.impl.ArrangeTaskExecutor;
import com.alipay.sofa.registry.server.meta.slot.impl.DataServerArrangeTaskDispatcher;
import com.alipay.sofa.registry.server.meta.slot.impl.DefaultSlotArranger;
import com.alipay.sofa.registry.server.meta.slot.impl.DefaultSlotManager;
import com.alipay.sofa.registry.store.api.DBService;
import com.alipay.sofa.registry.task.listener.DefaultTaskListenerManager;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;
import com.alipay.sofa.registry.util.DefaultExecutorFactory;
import com.alipay.sofa.registry.util.NamedThreadFactory;
import com.alipay.sofa.registry.util.OsUtils;
import com.alipay.sofa.registry.util.PropertySplitter;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.*;

/**
 *
 * @author shangyu.wh
 * @version $Id: MetaServerConfiguration.java, v 0.1 2018-01-12 14:53 shangyu.wh Exp $
 */
@Configuration
@Import(MetaServerInitializerConfiguration.class)
@EnableConfigurationProperties
public class MetaServerConfiguration {

    public static final String SCHEDULED_EXECUTOR       = "scheduledExecutor";
    public static final String GLOBAL_EXECUTOR          = "globalExecutor";
    public static final int    maxScheduledCorePoolSize = 8;
    public static final int    THREAD_POOL_TIME_OUT     = 5;
    public static final int    GLOBAL_THREAD_MULTI_CORE = 100;
    public static final int    GLOBAL_THREAD_MAX        = 100;

    @Bean
    @ConditionalOnMissingBean
    public MetaServerBootstrap metaServerBootstrap() {
        return new MetaServerBootstrap();
    }

    @Configuration
    protected static class MetaServerConfigBeanConfiguration {
        @Bean
        @ConditionalOnMissingBean
        public MetaServerConfig metaServerConfig() {
            return new MetaServerConfigBean();
        }

        @Bean
        public NodeConfig nodeConfig() {
            return new NodeConfigBeanProperty();
        }

        @Bean(name = "PropertySplitter")
        public PropertySplitter propertySplitter() {
            return new PropertySplitter();
        }
    }

    @Configuration
    public static class MetaServerRepositoryConfiguration {

        @Bean
        public RaftExchanger raftExchanger() {
            return new RaftExchanger();
        }

        @Bean
        public RaftAnnotationBeanPostProcessor raftAnnotationBeanPostProcessor() {
            return new RaftAnnotationBeanPostProcessor();
        }

        @Bean
        public SmartSpringLifecycleController smartSpringLifecycleController() {
            return new SmartSpringLifecycleController();
        }
    }

    @Configuration
    public static class ThreadPoolResourceConfiguration {
        @Bean(name = GLOBAL_EXECUTOR)
        public ExecutorService getGlobalExecutorService() {
            int corePoolSize = Math.min(OsUtils.getCpuCount() * 2, 8);
            int maxPoolSize = 50 * OsUtils.getCpuCount();
            DefaultExecutorFactory executorFactory = new DefaultExecutorFactory(GLOBAL_EXECUTOR,
                corePoolSize, maxPoolSize, new ThreadPoolExecutor.AbortPolicy());
            return executorFactory.create();
        }

        @Bean(name = SCHEDULED_EXECUTOR)
        public ScheduledExecutorService getScheduledService() {
            return new ScheduledThreadPoolExecutor(Math.min(OsUtils.getCpuCount() * 2, 12),
                new NamedThreadFactory("MetaServerGlobalScheduler"));
        }
    }

    @Configuration
    public static class MetaServerClusterConfiguration {
        @Bean
        public CrossDcMetaServerManager crossDcMetaServerManager() {
            return new CrossDcMetaServerManager();
        }

        @Bean
        public DefaultMetaServerManager defaultMetaServerManager() {
            return new DefaultMetaServerManager();
        }

        @Bean
        public DefaultDataServerManager dataDataServerManager() {
            return new DefaultDataServerManager();
        }

        @Bean
        public DefaultSessionManager defaultSessionManager() {
            return new DefaultSessionManager();
        }

        @Bean
        public CurrentDcMetaServer currentDcMetaServer() throws Exception {
            CurrentDcMetaServer currentDcMetaServer = new DefaultCurrentDcMetaServer();
            return currentDcMetaServer;
        }

    }

    @Configuration
    public static class SlotManagementConfiguration {

        @Bean
        public ArrangeTaskExecutor arrangeTaskExecutor() {
            return new ArrangeTaskExecutor();
        }

        @Bean
        public DataServerArrangeTaskDispatcher dataArrangeTaskDispathcher() {
            return new DataServerArrangeTaskDispatcher();
        }

        @Bean
        public DefaultSlotArranger slotArranger() {
            return new DefaultSlotArranger();
        }

        @Bean
        public LocalSlotManager slotManager() {
            return new LocalSlotManager();
        }

        @Bean
        public DefaultSlotManager defaultSlotManager() {
            return new DefaultSlotManager();
        }
    }

    @Configuration
    public static class MetaServerTaskConfiguration {
        @Bean
        public TaskListenerManager taskListenerManager() {
            return new DefaultTaskListenerManager();
        }
    }

    @Configuration
    public static class MetaServerRemotingConfiguration {

        @Bean
        public Exchange boltExchange() {
            return new BoltExchange();
        }

        @Bean
        public Exchange jerseyExchange() {
            return new JerseyExchange();
        }

        @Bean(name = "sessionServerHandlers")
        public Collection<AbstractServerHandler> sessionServerHandlers() {
            Collection<AbstractServerHandler> list = new ArrayList<>();
            list.add(sessionConnectionHandler());
            list.add(renewNodesRequestHandler());
            list.add(fetchProvideDataRequestHandler());
            return list;
        }

        @Bean(name = "dataServerHandlers")
        public Collection<AbstractServerHandler> dataServerHandlers() {
            Collection<AbstractServerHandler> list = new ArrayList<>();
            list.add(dataConnectionHandler());
            list.add(renewNodesRequestHandler());
            list.add(fetchProvideDataRequestHandler());
            return list;
        }

        @Bean(name = "metaServerHandlers")
        public Collection<AbstractServerHandler> metaServerHandlers() {
            Collection<AbstractServerHandler> list = new ArrayList<>();
            list.add(metaConnectionHandler());
            return list;
        }

        @Bean
        public AbstractServerHandler sessionConnectionHandler() {
            return new SessionConnectionHandler();
        }

        @Bean
        public AbstractServerHandler dataConnectionHandler() {
            return new DataConnectionHandler();
        }

        @Bean
        public AbstractServerHandler metaConnectionHandler() {
            return new MetaConnectionHandler();
        }

        @Bean
        public AbstractServerHandler renewNodesRequestHandler() {
            return new HeartbeatRequestHandler();
        }

        @Bean
        public AbstractServerHandler fetchProvideDataRequestHandler() {
            return new FetchProvideDataRequestHandler();
        }

        @Bean
        public NodeExchanger sessionNodeExchanger() {
            return new SessionNodeExchanger();
        }

        @Bean
        public NodeExchanger dataNodeExchanger() {
            return new DataNodeExchanger();
        }

        @Bean
        public NodeExchanger metaServerExchanger() {
            return new MetaServerExchanger();
        }

    }

    @Configuration
    public static class ResourceConfiguration {

        @Bean
        public ResourceConfig jerseyResourceConfig() {
            ResourceConfig resourceConfig = new ResourceConfig();
            resourceConfig.register(JacksonFeature.class);
            return resourceConfig;
        }

        @Bean
        public PersistentDataResource persistentDataResource() {
            return new PersistentDataResource();
        }

        @Bean
        public MetaDigestResource metaDigestResource() {
            return new MetaDigestResource();
        }

        @Bean
        public HealthResource healthResource() {
            return new HealthResource();
        }

        @Bean
        public MetaStoreResource metaStoreResource() {
            return new MetaStoreResource();
        }

        @Bean
        @ConditionalOnMissingBean
        public StopPushDataResource stopPushDataResource() {
            return new StopPushDataResource();
        }

        @Bean
        public BlacklistDataResource blacklistDataResource() {

            return new BlacklistDataResource();
        }

        @Bean
        public SlotSyncResource renewSwitchResource() {
            return new SlotSyncResource();
        }

        @Bean
        public SessionLoadbalanceResource sessionLoadbalanceSwitchResource() {
            return new SessionLoadbalanceResource();
        }
    }

    @Configuration
    public static class ExecutorConfiguation {

        @Bean
        public ExecutorManager executorManager(MetaServerConfig metaServerConfig) {
            return new ExecutorManager(metaServerConfig);
        }

        @Bean
        public ThreadPoolExecutor defaultRequestExecutor(MetaServerConfig metaServerConfig) {
            ThreadPoolExecutor defaultRequestExecutor = new ThreadPoolExecutor(
                metaServerConfig.getDefaultRequestExecutorMinSize(),
                metaServerConfig.getDefaultRequestExecutorMaxSize(), 300, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(metaServerConfig.getDefaultRequestExecutorQueueSize()),
                new NamedThreadFactory("MetaHandler-DefaultRequest"));
            defaultRequestExecutor.allowCoreThreadTimeOut(true);
            return defaultRequestExecutor;
        }

    }

    @Configuration
    public static class MetaDBConfiguration {
        @Bean
        public DBService persistenceDataDBService() {
            return new PersistenceDataDBService();
        }
    }
}