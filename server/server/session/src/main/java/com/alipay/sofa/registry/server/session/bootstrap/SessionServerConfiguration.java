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
package com.alipay.sofa.registry.server.session.bootstrap;

import java.util.ArrayList;
import java.util.Collection;

import com.alipay.sofa.registry.server.session.connections.ConnectionsService;
import com.alipay.sofa.registry.server.session.remoting.handler.*;
import com.alipay.sofa.registry.server.session.resource.*;
import com.alipay.sofa.registry.server.session.cache.SessionCacheDigestTask;
import com.alipay.sofa.registry.server.session.resource.SessionStopPushResource;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.alipay.sofa.registry.remoting.bolt.exchange.BoltExchange;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.remoting.exchange.NodeExchanger;
import com.alipay.sofa.registry.remoting.jersey.exchange.JerseyExchange;
import com.alipay.sofa.registry.server.session.acceptor.WriteDataAcceptor;
import com.alipay.sofa.registry.server.session.acceptor.WriteDataAcceptorImpl;
import com.alipay.sofa.registry.server.session.cache.CacheGenerator;
import com.alipay.sofa.registry.server.session.cache.CacheService;
import com.alipay.sofa.registry.server.session.cache.DatumCacheGenerator;
import com.alipay.sofa.registry.server.session.cache.SessionCacheService;
import com.alipay.sofa.registry.server.session.filter.DataIdMatchStrategy;
import com.alipay.sofa.registry.server.session.filter.IPMatchStrategy;
import com.alipay.sofa.registry.server.session.filter.ProcessFilter;
import com.alipay.sofa.registry.server.session.filter.blacklist.BlacklistManager;
import com.alipay.sofa.registry.server.session.filter.blacklist.BlacklistManagerImpl;
import com.alipay.sofa.registry.server.session.filter.blacklist.BlacklistMatchProcessFilter;
import com.alipay.sofa.registry.server.session.filter.blacklist.DefaultDataIdMatchStrategy;
import com.alipay.sofa.registry.server.session.filter.blacklist.DefaultIPMatchStrategy;
import com.alipay.sofa.registry.server.session.limit.AccessLimitService;
import com.alipay.sofa.registry.server.session.limit.AccessLimitServiceImpl;
import com.alipay.sofa.registry.server.session.listener.CancelDataTaskListener;
import com.alipay.sofa.registry.server.session.listener.DataChangeFetchCloudTaskListener;
import com.alipay.sofa.registry.server.session.listener.DataChangeFetchTaskListener;
import com.alipay.sofa.registry.server.session.listener.DataPushTaskListener;
import com.alipay.sofa.registry.server.session.listener.DatumSnapshotTaskListener;
import com.alipay.sofa.registry.server.session.listener.ProvideDataChangeFetchTaskListener;
import com.alipay.sofa.registry.server.session.listener.PublishDataTaskListener;
import com.alipay.sofa.registry.server.session.listener.ReceivedConfigDataPushTaskListener;
import com.alipay.sofa.registry.server.session.listener.ReceivedDataMultiPushTaskListener;
import com.alipay.sofa.registry.server.session.listener.RenewDatumTaskListener;
import com.alipay.sofa.registry.server.session.listener.SessionRegisterDataTaskListener;
import com.alipay.sofa.registry.server.session.listener.SubscriberMultiFetchTaskListener;
import com.alipay.sofa.registry.server.session.listener.SubscriberPushEmptyTaskListener;
import com.alipay.sofa.registry.server.session.listener.SubscriberRegisterFetchTaskListener;
import com.alipay.sofa.registry.server.session.listener.UnPublishDataTaskListener;
import com.alipay.sofa.registry.server.session.listener.WatcherRegisterFetchTaskListener;
import com.alipay.sofa.registry.server.session.node.DataNodeManager;
import com.alipay.sofa.registry.server.session.node.MetaNodeManager;
import com.alipay.sofa.registry.server.session.node.NodeManager;
import com.alipay.sofa.registry.server.session.node.NodeManagerFactory;
import com.alipay.sofa.registry.server.session.node.RaftClientManager;
import com.alipay.sofa.registry.server.session.node.SessionNodeManager;
import com.alipay.sofa.registry.server.session.node.processor.ClientNodeSingleTaskProcessor;
import com.alipay.sofa.registry.server.session.node.processor.ConsoleSyncSingleTaskProcessor;
import com.alipay.sofa.registry.server.session.node.processor.DataNodeSingleTaskProcessor;
import com.alipay.sofa.registry.server.session.node.processor.MetaNodeSingleTaskProcessor;
import com.alipay.sofa.registry.server.session.node.service.ClientNodeService;
import com.alipay.sofa.registry.server.session.node.service.ClientNodeServiceImpl;
import com.alipay.sofa.registry.server.session.node.service.DataNodeService;
import com.alipay.sofa.registry.server.session.node.service.DataNodeServiceImpl;
import com.alipay.sofa.registry.server.session.node.service.MetaNodeService;
import com.alipay.sofa.registry.server.session.node.service.MetaNodeServiceImpl;
import com.alipay.sofa.registry.server.session.provideData.ProvideDataProcessor;
import com.alipay.sofa.registry.server.session.provideData.ProvideDataProcessorManager;
import com.alipay.sofa.registry.server.session.provideData.processor.BlackListProvideDataProcessor;
import com.alipay.sofa.registry.server.session.provideData.processor.RenewSnapshotProvideDataProcessor;
import com.alipay.sofa.registry.server.session.provideData.processor.StopPushProvideDataProcessor;
import com.alipay.sofa.registry.server.session.registry.Registry;
import com.alipay.sofa.registry.server.session.registry.SessionRegistry;
import com.alipay.sofa.registry.server.session.remoting.ClientNodeExchanger;
import com.alipay.sofa.registry.server.session.remoting.DataNodeExchanger;
import com.alipay.sofa.registry.server.session.remoting.MetaNodeExchanger;
import com.alipay.sofa.registry.server.session.renew.DefaultRenewService;
import com.alipay.sofa.registry.server.session.renew.RenewService;
import com.alipay.sofa.registry.server.session.scheduler.ExecutorManager;
import com.alipay.sofa.registry.server.session.scheduler.timertask.SyncClientsHeartbeatTask;
import com.alipay.sofa.registry.server.session.store.DataStore;
import com.alipay.sofa.registry.server.session.store.Interests;
import com.alipay.sofa.registry.server.session.store.SessionDataStore;
import com.alipay.sofa.registry.server.session.store.SessionInterests;
import com.alipay.sofa.registry.server.session.store.SessionWatchers;
import com.alipay.sofa.registry.server.session.store.Watchers;
import com.alipay.sofa.registry.server.session.strategy.DataChangeRequestHandlerStrategy;
import com.alipay.sofa.registry.server.session.strategy.PublisherHandlerStrategy;
import com.alipay.sofa.registry.server.session.strategy.ReceivedConfigDataPushTaskStrategy;
import com.alipay.sofa.registry.server.session.strategy.ReceivedDataMultiPushTaskStrategy;
import com.alipay.sofa.registry.server.session.strategy.SessionRegistryStrategy;
import com.alipay.sofa.registry.server.session.strategy.SubscriberHandlerStrategy;
import com.alipay.sofa.registry.server.session.strategy.SubscriberMultiFetchTaskStrategy;
import com.alipay.sofa.registry.server.session.strategy.SubscriberRegisterFetchTaskStrategy;
import com.alipay.sofa.registry.server.session.strategy.SyncConfigHandlerStrategy;
import com.alipay.sofa.registry.server.session.strategy.TaskMergeProcessorStrategy;
import com.alipay.sofa.registry.server.session.strategy.WatcherHandlerStrategy;
import com.alipay.sofa.registry.server.session.strategy.impl.DefaultDataChangeRequestHandlerStrategy;
import com.alipay.sofa.registry.server.session.strategy.impl.DefaultPublisherHandlerStrategy;
import com.alipay.sofa.registry.server.session.strategy.impl.DefaultPushTaskMergeProcessor;
import com.alipay.sofa.registry.server.session.strategy.impl.DefaultReceivedConfigDataPushTaskStrategy;
import com.alipay.sofa.registry.server.session.strategy.impl.DefaultReceivedDataMultiPushTaskStrategy;
import com.alipay.sofa.registry.server.session.strategy.impl.DefaultSessionRegistryStrategy;
import com.alipay.sofa.registry.server.session.strategy.impl.DefaultSubscriberHandlerStrategy;
import com.alipay.sofa.registry.server.session.strategy.impl.DefaultSubscriberMultiFetchTaskStrategy;
import com.alipay.sofa.registry.server.session.strategy.impl.DefaultSubscriberRegisterFetchTaskStrategy;
import com.alipay.sofa.registry.server.session.strategy.impl.DefaultSyncConfigHandlerStrategy;
import com.alipay.sofa.registry.server.session.strategy.impl.DefaultWatcherHandlerStrategy;
import com.alipay.sofa.registry.server.session.wrapper.AccessLimitWrapperInterceptor;
import com.alipay.sofa.registry.server.session.wrapper.BlacklistWrapperInterceptor;
import com.alipay.sofa.registry.server.session.wrapper.ClientCheckWrapperInterceptor;
import com.alipay.sofa.registry.server.session.wrapper.WrapperInterceptor;
import com.alipay.sofa.registry.server.session.wrapper.WrapperInterceptorManager;
import com.alipay.sofa.registry.task.batcher.TaskProcessor;
import com.alipay.sofa.registry.task.listener.DefaultTaskListenerManager;
import com.alipay.sofa.registry.task.listener.TaskListener;
import com.alipay.sofa.registry.task.listener.TaskListenerManager;
import com.alipay.sofa.registry.util.PropertySplitter;

/**
 *
 * @author shangyu.wh
 * @version $Id: SessionServerConfiguration.java, v 0.1 2017-11-14 11:39 synex Exp $
 */
@Configuration
@Import(SessionServerInitializer.class)
@EnableConfigurationProperties
public class SessionServerConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SessionServerBootstrap sessionServerBootstrap() {
        return new SessionServerBootstrap();
    }

    @Configuration
    public static class SessionServerConfigBeanConfiguration {
        @Bean
        public CommonConfig commonConfig() {
            return new CommonConfig();
        }

        @Bean
        @ConditionalOnMissingBean
        public SessionServerConfig sessionServerConfig(CommonConfig commonConfig) {
            return new SessionServerConfigBean(commonConfig);
        }

        @Bean(name = "PropertySplitter")
        public PropertySplitter propertySplitter() {
            return new PropertySplitter();
        }

    }

    @Configuration
    public static class SessionRemotingConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "boltExchange")
        public Exchange boltExchange() {
            return new BoltExchange();
        }

        @Bean
        public Exchange jerseyExchange() {
            return new JerseyExchange();
        }

        @Bean
        public NodeExchanger clientNodeExchanger() {
            return new ClientNodeExchanger();
        }

        @Bean
        public NodeExchanger dataNodeExchanger() {
            return new DataNodeExchanger();
        }

        @Bean
        public NodeExchanger metaNodeExchanger() {
            return new MetaNodeExchanger();
        }

        @Bean(name = "serverHandlers")
        public Collection<AbstractServerHandler> serverHandlers() {
            Collection<AbstractServerHandler> list = new ArrayList<>();
            list.add(publisherHandler());
            list.add(subscriberHandler());
            list.add(watcherHandler());
            list.add(clientNodeConnectionHandler());
            list.add(cancelAddressRequestHandler());
            list.add(syncConfigHandler());
            return list;
        }

        @Bean
        public AbstractServerHandler publisherHandler() {
            return new PublisherHandler();
        }

        @Bean
        public AbstractServerHandler syncConfigHandler() {
            return new SyncConfigHandler();
        }

        @Bean
        public AbstractServerHandler subscriberHandler() {
            return new SubscriberHandler();
        }

        @Bean
        public AbstractServerHandler watcherHandler() {
            return new WatcherHandler();
        }

        @Bean
        public AbstractServerHandler clientNodeConnectionHandler() {
            return new ClientNodeConnectionHandler();
        }

        @Bean
        public AbstractServerHandler cancelAddressRequestHandler() {
            return new CancelAddressRequestHandler();
        }

        @Bean(name = "dataClientHandlers")
        public Collection<AbstractClientHandler> dataClientHandlers() {
            Collection<AbstractClientHandler> list = new ArrayList<>();
            list.add(dataNodeConnectionHandler());
            list.add(dataChangeRequestHandler());
            list.add(dataPushRequestHandler());
            return list;
        }

        @Bean(name = "metaClientHandlers")
        public Collection<AbstractClientHandler> metaClientHandlers() {
            Collection<AbstractClientHandler> list = new ArrayList<>();
            list.add(metaNodeConnectionHandler());
            list.add(nodeChangeResultHandler());
            list.add(notifyProvideDataChangeHandler());
            list.add(loadbalanceMetricsHandler());
            list.add(configureLoadbalanceHandler());
            return list;
        }

        @Bean
        public AbstractClientHandler metaNodeConnectionHandler() {
            return new MetaNodeConnectionHandler();
        }

        @Bean
        public AbstractClientHandler dataNodeConnectionHandler() {
            return new DataNodeConnectionHandler();
        }

        @Bean
        public AbstractClientHandler dataChangeRequestHandler() {
            return new DataChangeRequestHandler();
        }

        @Bean
        public AbstractClientHandler dataPushRequestHandler() {
            return new DataPushRequestHandler();
        }

        @Bean
        public AbstractClientHandler nodeChangeResultHandler() {
            return new NodeChangeResultHandler();
        }

        @Bean
        public AbstractClientHandler notifyProvideDataChangeHandler() {
            return new NotifyProvideDataChangeHandler();
        }

        @Bean
        public AbstractClientHandler loadbalanceMetricsHandler() {
            return new LoadbalanceMetricsHandler();
        }

        @Bean
        public AbstractClientHandler configureLoadbalanceHandler() {
            return new ConfigureLoadbalanceHandler();
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
        @ConditionalOnMissingBean(name = "sessionOpenResource")
        public SessionOpenResource sessionOpenResource() {
            return new SessionOpenResource();
        }

        @Bean
        public SessionDigestResource sessionDigestResource() {
            return new SessionDigestResource();
        }

        @Bean
        @ConditionalOnMissingBean(name = "healthResource")
        public HealthResource healthResource() {
            return new HealthResource();
        }

        @Bean
        public ClientsOpenResource clientsOpenResource() {
            return new ClientsOpenResource();
        }

        @Bean
        @ConditionalOnMissingBean(name = "sessionStopPushResource")
        public SessionStopPushResource sessionStopPushResource() {
            return new SessionStopPushResource();
        }

        @Bean
        public ConnectionsResource connectionsResource() {
            return new ConnectionsResource();
        }
    }

    @Configuration
    public static class SessionRegistryConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "sessionRegistry")
        public Registry sessionRegistry() {
            return new SessionRegistry();
        }

        @Bean
        @ConditionalOnMissingBean
        public Interests sessionInterests() {
            return new SessionInterests();
        }

        @Bean
        @ConditionalOnMissingBean
        public Watchers sessionWatchers() {
            return new SessionWatchers();
        }

        @Bean
        @ConditionalOnMissingBean
        public DataStore sessionDataStore() {
            return new SessionDataStore();
        }
    }

    @Configuration
    public static class SessionNodeConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public DataNodeService dataNodeService() {
            return new DataNodeServiceImpl();
        }

        @Bean
        public MetaNodeService mataNodeService() {
            return new MetaNodeServiceImpl();
        }

        @Bean
        @ConditionalOnMissingBean
        public ClientNodeService clientNodeService() {
            return new ClientNodeServiceImpl();
        }

        @Bean
        public NodeManager dataNodeManager() {
            return new DataNodeManager();
        }

        @Bean
        public NodeManager sessionNodeManager() {
            return new SessionNodeManager();
        }

        @Bean
        public NodeManager metaNodeManager() {
            return new MetaNodeManager();
        }

        @Bean
        public RaftClientManager raftClientManager() {
            return new RaftClientManager();
        }

        @Bean
        public NodeManagerFactory nodeManagerFactory() {
            return new NodeManagerFactory();
        }
    }

    @Configuration
    public static class SessionCacheConfiguration {

        @Bean
        public CacheService sessionCacheService() {
            return new SessionCacheService();
        }

        @Bean(name = "com.alipay.sofa.registry.server.session.cache.DatumKey")
        public CacheGenerator datumCacheGenerator() {
            return new DatumCacheGenerator();
        }
    }

    @Configuration
    public static class SessionTaskConfiguration {

        @Bean
        public TaskProcessor dataNodeSingleTaskProcessor() {
            return new DataNodeSingleTaskProcessor();
        }

        @Bean
        public TaskProcessor metaNodeSingleTaskProcessor() {
            return new MetaNodeSingleTaskProcessor();
        }

        @Bean
        public TaskProcessor clientNodeSingleTaskProcessor() {
            return new ClientNodeSingleTaskProcessor();
        }

        @Bean
        public TaskProcessor consoleSyncSingleTaskProcessor() {
            return new ConsoleSyncSingleTaskProcessor();
        }

        @Bean
        public TaskListener subscriberRegisterFetchTaskListener(TaskListenerManager taskListenerManager) {
            TaskListener taskListener = new SubscriberRegisterFetchTaskListener(
                dataNodeSingleTaskProcessor());
            taskListenerManager.addTaskListener(taskListener);
            return taskListener;
        }

        @Bean
        public TaskListener subscriberMultiFetchTaskListener(TaskListenerManager taskListenerManager) {
            TaskListener taskListener = new SubscriberMultiFetchTaskListener(
                dataNodeSingleTaskProcessor());
            taskListenerManager.addTaskListener(taskListener);
            return taskListener;
        }

        @Bean
        public TaskListener watcherRegisterFetchTaskListener(TaskListenerManager taskListenerManager) {
            TaskListener taskListener = new WatcherRegisterFetchTaskListener(
                metaNodeSingleTaskProcessor());
            taskListenerManager.addTaskListener(taskListener);
            return taskListener;
        }

        @Bean
        public TaskListener provideDataChangeFetchTaskListener(TaskListenerManager taskListenerManager) {
            TaskListener taskListener = new ProvideDataChangeFetchTaskListener(
                metaNodeSingleTaskProcessor());
            taskListenerManager.addTaskListener(taskListener);
            return taskListener;
        }

        @Bean
        public TaskListener dataChangeFetchTaskListener(TaskListenerManager taskListenerManager) {
            TaskListener taskListener = new DataChangeFetchTaskListener(
                dataNodeSingleTaskProcessor());
            taskListenerManager.addTaskListener(taskListener);
            return taskListener;
        }

        @Bean
        public TaskListener dataPushTaskListener(TaskListenerManager taskListenerManager) {
            TaskListener taskListener = new DataPushTaskListener(dataNodeSingleTaskProcessor());
            taskListenerManager.addTaskListener(taskListener);
            return taskListener;
        }

        @Bean
        public TaskListener dataChangeFetchCloudTaskListener(TaskListenerManager taskListenerManager) {
            TaskListener taskListener = new DataChangeFetchCloudTaskListener(
                dataNodeSingleTaskProcessor());
            taskListenerManager.addTaskListener(taskListener);
            return taskListener;
        }

        @Bean
        public TaskListener sessionRegisterDataTaskListener(TaskListenerManager taskListenerManager) {
            TaskListener taskListener = new SessionRegisterDataTaskListener(
                dataNodeSingleTaskProcessor());
            taskListenerManager.addTaskListener(taskListener);
            return taskListener;
        }

        @Bean
        public TaskListener receivedDataMultiPushTaskListener(TaskListenerManager taskListenerManager,
                                                              TaskMergeProcessorStrategy receiveDataTaskMergeProcessorStrategy,
                                                              SessionServerConfig sessionServerConfig) {
            TaskListener taskListener = new ReceivedDataMultiPushTaskListener(
                clientNodeSingleTaskProcessor(), receiveDataTaskMergeProcessorStrategy,
                sessionServerConfig);
            taskListenerManager.addTaskListener(taskListener);
            return taskListener;
        }

        @Bean
        public TaskListener receivedConfigDataPushTaskListener(TaskListenerManager taskListenerManager) {
            TaskListener taskListener = new ReceivedConfigDataPushTaskListener(
                clientNodeSingleTaskProcessor());
            taskListenerManager.addTaskListener(taskListener);
            return taskListener;
        }

        @Bean
        @ConditionalOnMissingBean(name = "cancelDataTaskListener")
        public TaskListener cancelDataTaskListener(TaskListenerManager taskListenerManager) {
            TaskListener taskListener = new CancelDataTaskListener();
            taskListenerManager.addTaskListener(taskListener);
            return taskListener;
        }

        @Bean
        public TaskListener datumSnapshotTaskListener(TaskListenerManager taskListenerManager) {
            TaskListener taskListener = new DatumSnapshotTaskListener();
            taskListenerManager.addTaskListener(taskListener);
            return taskListener;
        }

        @Bean
        public TaskListener publishDataTaskListener(TaskListenerManager taskListenerManager) {
            TaskListener taskListener = new PublishDataTaskListener();
            taskListenerManager.addTaskListener(taskListener);
            return taskListener;
        }

        @Bean
        public TaskListener renewDatumTaskListener(TaskListenerManager taskListenerManager) {
            TaskListener taskListener = new RenewDatumTaskListener();
            taskListenerManager.addTaskListener(taskListener);
            return taskListener;
        }

        @Bean
        public TaskListener unPublishDataTaskListener(TaskListenerManager taskListenerManager) {
            TaskListener taskListener = new UnPublishDataTaskListener();
            taskListenerManager.addTaskListener(taskListener);
            return taskListener;
        }

        @Bean
        @ConditionalOnMissingBean(name = "subscriberPushEmptyTaskListener")
        public TaskListener subscriberPushEmptyTaskListener(TaskListenerManager taskListenerManager) {
            TaskListener taskListener = new SubscriberPushEmptyTaskListener();
            taskListenerManager.addTaskListener(taskListener);
            return taskListener;
        }

        @Bean
        public TaskListenerManager taskListenerManager() {
            return new DefaultTaskListenerManager();
        }

    }

    @Configuration
    public static class ExecutorConfiguration {

        @Bean
        public ExecutorManager executorManager(SessionServerConfig sessionServerConfig) {
            return new ExecutorManager(sessionServerConfig);
        }

    }

    @Configuration
    public static class SessionTimerTaskConfiguration {

        @Bean
        public SyncClientsHeartbeatTask syncClientsHeartbeatTask() {
            return new SyncClientsHeartbeatTask();
        }
    }

    @Configuration
    public static class SessionStrategyConfiguration {
        @Bean
        @ConditionalOnMissingBean
        public SessionRegistryStrategy sessionRegistryStrategy() {
            return new DefaultSessionRegistryStrategy();
        }

        @Bean
        @ConditionalOnMissingBean
        public DataChangeRequestHandlerStrategy dataChangeRequestHandlerStrategy() {
            return new DefaultDataChangeRequestHandlerStrategy();
        }

        @Bean
        @ConditionalOnMissingBean
        public SyncConfigHandlerStrategy syncConfigHandlerStrategy() {
            return new DefaultSyncConfigHandlerStrategy();
        }

        @Bean
        @ConditionalOnMissingBean
        public SubscriberRegisterFetchTaskStrategy subscriberRegisterFetchTaskStrategy() {
            return new DefaultSubscriberRegisterFetchTaskStrategy();
        }

        @Bean
        @ConditionalOnMissingBean
        public SubscriberMultiFetchTaskStrategy subscriberMultiFetchTaskStrategy() {
            return new DefaultSubscriberMultiFetchTaskStrategy();
        }

        @Bean
        @ConditionalOnMissingBean
        public PublisherHandlerStrategy publisherHandlerStrategy() {
            return new DefaultPublisherHandlerStrategy();
        }

        @Bean
        @ConditionalOnMissingBean
        public SubscriberHandlerStrategy subscriberHandlerStrategy() {
            return new DefaultSubscriberHandlerStrategy();
        }

        @Bean
        @ConditionalOnMissingBean
        public WatcherHandlerStrategy watcherHandlerStrategy() {
            return new DefaultWatcherHandlerStrategy();
        }

        @Bean
        @ConditionalOnMissingBean
        public TaskMergeProcessorStrategy receiveDataTaskMergeProcessorStrategy() {
            return new DefaultPushTaskMergeProcessor();
        }

        @Bean
        @ConditionalOnMissingBean
        public ReceivedDataMultiPushTaskStrategy receivedDataMultiPushTaskStrategy() {
            return new DefaultReceivedDataMultiPushTaskStrategy();
        }

        @Bean
        @ConditionalOnMissingBean
        public ReceivedConfigDataPushTaskStrategy receivedConfigDataPushTaskStrategy() {
            return new DefaultReceivedConfigDataPushTaskStrategy();
        }
    }

    @Configuration
    public static class AccessLimitServiceConfiguration {
        @Bean
        public AccessLimitService accessLimitService(SessionServerConfig sessionServerConfig) {
            return new AccessLimitServiceImpl(sessionServerConfig);
        }
    }

    @Configuration
    public static class SessionFilterConfiguration {

        @Bean
        public IPMatchStrategy ipMatchStrategy() {
            return new DefaultIPMatchStrategy();
        }

        @Bean
        public DataIdMatchStrategy dataIdMatchStrategy() {
            return new DefaultDataIdMatchStrategy();
        }

        @Bean
        @ConditionalOnMissingBean
        public ProcessFilter processFilter() {
            return new BlacklistMatchProcessFilter();
        }

        @Bean
        public BlacklistManager blacklistManager() {
            return new BlacklistManagerImpl();
        }

        @Bean
        public WrapperInterceptorManager wrapperInterceptorManager() {
            return new WrapperInterceptorManager();
        }

        @Bean
        public WrapperInterceptor clientCheckWrapperInterceptor(WrapperInterceptorManager wrapperInterceptorManager) {
            ClientCheckWrapperInterceptor clientCheckWrapperInterceptor = new ClientCheckWrapperInterceptor();
            wrapperInterceptorManager.addInterceptor(clientCheckWrapperInterceptor);
            return clientCheckWrapperInterceptor;
        }

        @Bean
        public WrapperInterceptor blacklistWrapperInterceptor(WrapperInterceptorManager wrapperInterceptorManager) {
            BlacklistWrapperInterceptor blacklistWrapperInterceptor = new BlacklistWrapperInterceptor();
            wrapperInterceptorManager.addInterceptor(blacklistWrapperInterceptor);
            return blacklistWrapperInterceptor;
        }

        @Bean
        public WrapperInterceptor accessLimitWrapperInterceptor(WrapperInterceptorManager wrapperInterceptorManager) {
            AccessLimitWrapperInterceptor accessLimitWrapperInterceptor = new AccessLimitWrapperInterceptor();
            wrapperInterceptorManager.addInterceptor(accessLimitWrapperInterceptor);
            return accessLimitWrapperInterceptor;
        }

    }

    @Configuration
    public static class SessionRenewDatumConfiguration {

        @Bean
        public WriteDataAcceptor writeDataAcceptor() {
            return new WriteDataAcceptorImpl();
        }

        @Bean
        public RenewService renewService() {
            return new DefaultRenewService();
        }
    }

    @Configuration
    public static class SessionConnectionsConfiguration {
        @Bean
        public ConnectionsService connectionsService() {
            return new ConnectionsService();
        }
    }

    @Configuration
    public static class SessionProvideDataConfiguration {

        @Bean
        public ProvideDataProcessor provideDataProcessorManager() {
            return new ProvideDataProcessorManager();
        }

        @Bean
        public ProvideDataProcessor blackListProvideDataProcessor(ProvideDataProcessor provideDataProcessorManager) {
            ProvideDataProcessor blackListProvideDataProcessor = new BlackListProvideDataProcessor();
            ((ProvideDataProcessorManager) provideDataProcessorManager)
                .addProvideDataProcessor(blackListProvideDataProcessor);
            return blackListProvideDataProcessor;
        }

        @Bean
        public ProvideDataProcessor renewSnapshotProvideDataProcessor(ProvideDataProcessor provideDataProcessorManager) {
            ProvideDataProcessor renewSnapshotProvideDataProcessor = new RenewSnapshotProvideDataProcessor();
            ((ProvideDataProcessorManager) provideDataProcessorManager)
                .addProvideDataProcessor(renewSnapshotProvideDataProcessor);
            return renewSnapshotProvideDataProcessor;
        }

        @Bean
        public ProvideDataProcessor stopPushProvideDataProcessor(ProvideDataProcessor provideDataProcessorManager) {
            ProvideDataProcessor stopPushProvideDataProcessor = new StopPushProvideDataProcessor();
            ((ProvideDataProcessorManager) provideDataProcessorManager)
                .addProvideDataProcessor(stopPushProvideDataProcessor);
            return stopPushProvideDataProcessor;
        }
    }

    @Configuration
    public static class LogTaskConfigConfiguration {

        @Bean
        public SessionCacheDigestTask sessionCacheDigestTask() {
            return new SessionCacheDigestTask();
        }

    }
}