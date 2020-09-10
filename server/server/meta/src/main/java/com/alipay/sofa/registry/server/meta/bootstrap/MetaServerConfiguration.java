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

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.alipay.sofa.registry.server.meta.remoting.handler.*;
import com.alipay.sofa.registry.util.NamedThreadFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.alipay.sofa.registry.jraft.service.PersistenceDataDBService;
import com.alipay.sofa.registry.remoting.bolt.exchange.BoltExchange;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.remoting.exchange.NodeExchanger;
import com.alipay.sofa.registry.remoting.jersey.exchange.JerseyExchange;
import com.alipay.sofa.registry.server.meta.executor.ExecutorManager;
import com.alipay.sofa.registry.server.meta.listener.DataNodeChangePushTaskListener;
import com.alipay.sofa.registry.server.meta.listener.PersistenceDataChangeNotifyTaskListener;
import com.alipay.sofa.registry.server.meta.listener.ReceiveStatusConfirmNotifyTaskListener;
import com.alipay.sofa.registry.server.meta.listener.SessionNodeChangePushTaskListener;
import com.alipay.sofa.registry.server.meta.node.NodeService;
import com.alipay.sofa.registry.server.meta.node.impl.DataNodeServiceImpl;
import com.alipay.sofa.registry.server.meta.node.impl.MetaNodeServiceImpl;
import com.alipay.sofa.registry.server.meta.node.impl.SessionNodeServiceImpl;
import com.alipay.sofa.registry.server.meta.registry.MetaServerRegistry;
import com.alipay.sofa.registry.server.meta.registry.Registry;
import com.alipay.sofa.registry.server.meta.remoting.DataNodeExchanger;
import com.alipay.sofa.registry.server.meta.remoting.MetaClientExchanger;
import com.alipay.sofa.registry.server.meta.remoting.MetaServerExchanger;
import com.alipay.sofa.registry.server.meta.remoting.RaftExchanger;
import com.alipay.sofa.registry.server.meta.remoting.SessionNodeExchanger;
import com.alipay.sofa.registry.server.meta.remoting.connection.DataConnectionHandler;
import com.alipay.sofa.registry.server.meta.remoting.connection.MetaConnectionHandler;
import com.alipay.sofa.registry.server.meta.remoting.connection.SessionConnectionHandler;
import com.alipay.sofa.registry.server.meta.repository.NodeConfirmStatusService;
import com.alipay.sofa.registry.server.meta.repository.RepositoryService;
import com.alipay.sofa.registry.server.meta.repository.VersionRepositoryService;
import com.alipay.sofa.registry.server.meta.repository.annotation.RaftAnnotationBeanPostProcessor;
import com.alipay.sofa.registry.server.meta.repository.service.DataConfirmStatusService;
import com.alipay.sofa.registry.server.meta.repository.service.DataRepositoryService;
import com.alipay.sofa.registry.server.meta.repository.service.MetaRepositoryService;
import com.alipay.sofa.registry.server.meta.repository.service.SessionConfirmStatusService;
import com.alipay.sofa.registry.server.meta.repository.service.SessionRepositoryService;
import com.alipay.sofa.registry.server.meta.repository.service.SessionVersionRepositoryService;
import com.alipay.sofa.registry.server.meta.resource.BlacklistDataResource;
import com.alipay.sofa.registry.server.meta.resource.DecisionModeResource;
import com.alipay.sofa.registry.server.meta.resource.HealthResource;
import com.alipay.sofa.registry.server.meta.resource.MetaDigestResource;
import com.alipay.sofa.registry.server.meta.resource.MetaStoreResource;
import com.alipay.sofa.registry.server.meta.resource.PersistentDataResource;
import com.alipay.sofa.registry.server.meta.resource.RenewSwitchResource;
import com.alipay.sofa.registry.server.meta.resource.StopPushDataResource;
import com.alipay.sofa.registry.server.meta.store.DataStoreService;
import com.alipay.sofa.registry.server.meta.store.MetaStoreService;
import com.alipay.sofa.registry.server.meta.store.SessionStoreService;
import com.alipay.sofa.registry.server.meta.store.StoreService;
import com.alipay.sofa.registry.server.meta.task.processor.DataNodeSingleTaskProcessor;
import com.alipay.sofa.registry.server.meta.task.processor.MetaNodeSingleTaskProcessor;
import com.alipay.sofa.registry.server.meta.task.processor.SessionNodeSingleTaskProcessor;
import com.alipay.sofa.registry.store.api.DBService;
import com.alipay.sofa.registry.task.batcher.TaskProcessor;
import com.alipay.sofa.registry.task.listener.DefaultTaskListenerManager;
import com.alipay.sofa.registry.task.listener.TaskListener;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;
import com.alipay.sofa.registry.util.PropertySplitter;

/**
 *
 * @author shangyu.wh
 * @version $Id: MetaServerConfiguration.java, v 0.1 2018-01-12 14:53 shangyu.wh Exp $
 */
@Configuration
@Import(MetaServerInitializerConfiguration.class)
@EnableConfigurationProperties
public class MetaServerConfiguration {

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
    public static class MetaServerServiceConfiguration {

        @Bean
        public Registry metaServerRegistry() {
            return new MetaServerRegistry();
        }

        @Bean
        public NodeService sessionNodeService() {
            return new SessionNodeServiceImpl();
        }

        @Bean
        public NodeService dataNodeService() {
            return new DataNodeServiceImpl();
        }

        @Bean
        public NodeService metaNodeService() {
            return new MetaNodeServiceImpl();
        }

        @Bean
        public ServiceFactory storeServiceFactory() {
            return new ServiceFactory();
        }

        @Bean
        public StoreService sessionStoreService() {
            return new SessionStoreService();
        }

        @Bean
        public StoreService dataStoreService() {
            return new DataStoreService();
        }

        @Bean
        public StoreService metaStoreService() {
            return new MetaStoreService();
        }

    }

    @Configuration
    public static class MetaServerRepositoryConfiguration {
        @Bean
        public RepositoryService dataRepositoryService() {
            return new DataRepositoryService();
        }

        @Bean
        public RepositoryService metaRepositoryService() {
            return new MetaRepositoryService();
        }

        @Bean
        public NodeConfirmStatusService dataConfirmStatusService() {
            return new DataConfirmStatusService();
        }

        @Bean
        public RepositoryService sessionRepositoryService() {
            return new SessionRepositoryService();
        }

        @Bean
        public VersionRepositoryService sessionVersionRepositoryService() {
            return new SessionVersionRepositoryService();
        }

        @Bean
        public NodeConfirmStatusService sessionConfirmStatusService() {
            return new SessionConfirmStatusService();
        }

        @Bean
        public RaftExchanger raftExchanger() {
            return new RaftExchanger();
        }

        @Bean
        public RaftAnnotationBeanPostProcessor raftAnnotationBeanPostProcessor() {
            return new RaftAnnotationBeanPostProcessor();
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
            list.add(sessionNodeHandler());
            list.add(renewNodesRequestHandler());
            list.add(getNodesRequestHandler());
            list.add(fetchProvideDataRequestHandler());
            return list;
        }

        @Bean(name = "dataServerHandlers")
        public Collection<AbstractServerHandler> dataServerHandlers() {
            Collection<AbstractServerHandler> list = new ArrayList<>();
            list.add(dataConnectionHandler());
            list.add(getNodesRequestHandler());
            list.add(dataNodeHandler());
            list.add(renewNodesRequestHandler());
            list.add(fetchProvideDataRequestHandler());
            return list;
        }

        @Bean(name = "metaServerHandlers")
        public Collection<AbstractServerHandler> metaServerHandlers() {
            Collection<AbstractServerHandler> list = new ArrayList<>();
            list.add(metaConnectionHandler());
            list.add(getNodesRequestHandler());
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
        public AbstractServerHandler getNodesRequestHandler() {
            return new GetNodesRequestHandler();
        }

        @Bean
        public AbstractServerHandler sessionNodeHandler() {
            return new SessionNodeHandler();
        }

        @Bean
        public AbstractServerHandler renewNodesRequestHandler() {
            return new RenewNodesRequestHandler();
        }

        @Bean
        public AbstractServerHandler dataNodeHandler() {
            return new DataNodeHandler();
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

        @Bean
        public MetaClientExchanger metaClientExchanger() {
            return new MetaClientExchanger();
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
        public DecisionModeResource decisionModeResource() {
            return new DecisionModeResource();
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
        public RenewSwitchResource renewSwitchResource() {
            return new RenewSwitchResource();
        }
    }

    @Configuration
    public static class MetaServerTaskConfiguration {

        @Bean
        public TaskProcessor dataNodeSingleTaskProcessor() {
            return new DataNodeSingleTaskProcessor();
        }

        @Bean
        public TaskProcessor metaNodeSingleTaskProcessor() {
            return new MetaNodeSingleTaskProcessor();
        }

        @Bean
        public TaskProcessor sessionNodeSingleTaskProcessor() {
            return new SessionNodeSingleTaskProcessor();
        }

        @Bean
        public TaskListener sessionNodeChangePushTaskListener(TaskListenerManager taskListenerManager) {
            TaskListener taskListener = new SessionNodeChangePushTaskListener(
                sessionNodeSingleTaskProcessor());
            taskListenerManager.addTaskListener(taskListener);
            return taskListener;
        }

        @Bean
        public TaskListener dataNodeChangePushTaskListener(TaskListenerManager taskListenerManager) {
            TaskListener taskListener = new DataNodeChangePushTaskListener(
                dataNodeSingleTaskProcessor());
            taskListenerManager.addTaskListener(taskListener);
            return taskListener;
        }

        @Bean
        public TaskListener receiveStatusConfirmNotifyTaskListener(TaskListenerManager taskListenerManager) {
            TaskListener taskListener = new ReceiveStatusConfirmNotifyTaskListener(
                dataNodeSingleTaskProcessor());
            taskListenerManager.addTaskListener(taskListener);
            return taskListener;
        }

        @Bean
        public TaskListener persistenceDataChangeNotifyTaskListener(TaskListenerManager taskListenerManager) {
            TaskListener taskListener = new PersistenceDataChangeNotifyTaskListener(
                sessionNodeSingleTaskProcessor());
            taskListenerManager.addTaskListener(taskListener);
            return taskListener;
        }

        @Bean
        public TaskListenerManager taskListenerManager() {
            return new DefaultTaskListenerManager();
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