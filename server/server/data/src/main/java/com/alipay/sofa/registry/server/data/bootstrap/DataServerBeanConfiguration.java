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
package com.alipay.sofa.registry.server.data.bootstrap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.alipay.sofa.registry.remoting.bolt.exchange.BoltExchange;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.remoting.jersey.exchange.JerseyExchange;
import com.alipay.sofa.registry.server.data.cache.CacheDigestTask;
import com.alipay.sofa.registry.server.data.cache.DataServerCache;
import com.alipay.sofa.registry.server.data.cache.DatumCache;
import com.alipay.sofa.registry.server.data.cache.LocalDatumStorage;
import com.alipay.sofa.registry.server.data.change.DataChangeHandler;
import com.alipay.sofa.registry.server.data.change.event.DataChangeEventCenter;
import com.alipay.sofa.registry.server.data.change.notify.BackUpNotifier;
import com.alipay.sofa.registry.server.data.change.notify.IDataChangeNotifier;
import com.alipay.sofa.registry.server.data.change.notify.SessionServerNotifier;
import com.alipay.sofa.registry.server.data.change.notify.SnapshotBackUpNotifier;
import com.alipay.sofa.registry.server.data.change.notify.TempPublisherNotifier;
import com.alipay.sofa.registry.server.data.datasync.AcceptorStore;
import com.alipay.sofa.registry.server.data.datasync.SyncDataService;
import com.alipay.sofa.registry.server.data.datasync.sync.LocalAcceptorStore;
import com.alipay.sofa.registry.server.data.datasync.sync.Scheduler;
import com.alipay.sofa.registry.server.data.datasync.sync.StoreServiceFactory;
import com.alipay.sofa.registry.server.data.datasync.sync.SyncDataServiceImpl;
import com.alipay.sofa.registry.server.data.event.AfterWorkingProcess;
import com.alipay.sofa.registry.server.data.event.EventCenter;
import com.alipay.sofa.registry.server.data.event.handler.AfterWorkingProcessHandler;
import com.alipay.sofa.registry.server.data.event.handler.DataServerChangeEventHandler;
import com.alipay.sofa.registry.server.data.event.handler.LocalDataServerChangeEventHandler;
import com.alipay.sofa.registry.server.data.event.handler.MetaServerChangeEventHandler;
import com.alipay.sofa.registry.server.data.event.handler.StartTaskEventHandler;
import com.alipay.sofa.registry.server.data.node.DataNodeStatus;
import com.alipay.sofa.registry.server.data.remoting.DataNodeExchanger;
import com.alipay.sofa.registry.server.data.remoting.MetaNodeExchanger;
import com.alipay.sofa.registry.server.data.remoting.dataserver.DataServerConnectionFactory;
import com.alipay.sofa.registry.server.data.remoting.dataserver.GetSyncDataHandler;
import com.alipay.sofa.registry.server.data.remoting.dataserver.handler.DataSyncServerConnectionHandler;
import com.alipay.sofa.registry.server.data.remoting.dataserver.handler.FetchDataHandler;
import com.alipay.sofa.registry.server.data.remoting.dataserver.handler.NotifyDataSyncHandler;
import com.alipay.sofa.registry.server.data.remoting.dataserver.handler.NotifyFetchDatumHandler;
import com.alipay.sofa.registry.server.data.remoting.dataserver.handler.NotifyOnlineHandler;
import com.alipay.sofa.registry.server.data.remoting.dataserver.handler.SyncDataHandler;
import com.alipay.sofa.registry.server.data.remoting.dataserver.task.AbstractTask;
import com.alipay.sofa.registry.server.data.remoting.dataserver.task.ConnectionRefreshTask;
import com.alipay.sofa.registry.server.data.remoting.dataserver.task.RenewNodeTask;
import com.alipay.sofa.registry.server.data.remoting.handler.AbstractClientHandler;
import com.alipay.sofa.registry.server.data.remoting.handler.AbstractServerHandler;
import com.alipay.sofa.registry.server.data.remoting.metaserver.DefaultMetaServiceImpl;
import com.alipay.sofa.registry.server.data.remoting.metaserver.IMetaServerService;
import com.alipay.sofa.registry.server.data.remoting.metaserver.MetaServerConnectionFactory;
import com.alipay.sofa.registry.server.data.remoting.metaserver.handler.NotifyProvideDataChangeHandler;
import com.alipay.sofa.registry.server.data.remoting.metaserver.handler.ServerChangeHandler;
import com.alipay.sofa.registry.server.data.remoting.metaserver.handler.StatusConfirmHandler;
import com.alipay.sofa.registry.server.data.remoting.metaserver.provideData.ProvideDataProcessor;
import com.alipay.sofa.registry.server.data.remoting.metaserver.provideData.ProvideDataProcessorManager;
import com.alipay.sofa.registry.server.data.remoting.metaserver.provideData.processor.DatumExpireProvideDataProcessor;
import com.alipay.sofa.registry.server.data.remoting.metaserver.task.ConnectionRefreshMetaTask;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.SessionServerConnectionFactory;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.disconnect.DisconnectEventHandler;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.forward.ForwardService;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.forward.ForwardServiceImpl;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.handler.ClientOffHandler;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.handler.DataServerConnectionHandler;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.handler.DatumSnapshotHandler;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.handler.GetDataHandler;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.handler.GetDataVersionsHandler;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.handler.PublishDataHandler;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.handler.RenewDatumHandler;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.handler.SessionServerRegisterHandler;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.handler.UnPublishDataHandler;
import com.alipay.sofa.registry.server.data.renew.DatumLeaseManager;
import com.alipay.sofa.registry.server.data.renew.LocalDataServerCleanHandler;
import com.alipay.sofa.registry.server.data.resource.DataDigestResource;
import com.alipay.sofa.registry.server.data.resource.HealthResource;
import com.alipay.sofa.registry.server.data.util.ThreadPoolExecutorDataServer;
import com.alipay.sofa.registry.util.NamedThreadFactory;
import com.alipay.sofa.registry.util.PropertySplitter;

/**
 *
 * @author qian.lqlq
 * @version $Id: DataServerBeanConfiguration.java, v 0.1 2018-01-11 15:08 qian.lqlq Exp $
 */
@Configuration
@Import(DataServerInitializer.class)
@EnableConfigurationProperties
public class DataServerBeanConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DataServerBootstrap dataServerBootstrap() {
        return new DataServerBootstrap();
    }

    @Configuration
    protected static class DataServerBootstrapConfigConfiguration {

        @Bean
        public CommonConfig commonConfig() {
            return new CommonConfig();
        }

        @Bean
        @ConditionalOnMissingBean
        public DataServerConfig dataServerConfig(CommonConfig commonConfig) {
            return new DataServerConfig(commonConfig);
        }

        @Bean
        public DataNodeStatus dataNodeStatus() {
            return new DataNodeStatus();
        }

        @Bean(name = "PropertySplitter")
        public PropertySplitter propertySplitter() {
            return new PropertySplitter();
        }

    }

    @Configuration
    public static class DataServerStorageConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public DatumCache datumCache() {
            return new DatumCache();
        }

        @Bean
        @ConditionalOnMissingBean
        public LocalDatumStorage localDatumStorage() {
            return new LocalDatumStorage();
        }

    }

    @Configuration
    public static class LogTaskConfigConfiguration {

        @Bean
        public CacheDigestTask cacheDigestTask() {
            return new CacheDigestTask();
        }

    }

    @Configuration
    public static class SessionRemotingConfiguration {

        @Bean
        public Exchange jerseyExchange() {
            return new JerseyExchange();
        }

        @Bean
        public Exchange boltExchange() {
            return new BoltExchange();
        }

        @Bean
        public MetaNodeExchanger metaNodeExchanger() {
            return new MetaNodeExchanger();
        }

        @Bean
        public DataNodeExchanger dataNodeExchanger() {
            return new DataNodeExchanger();
        }

        @Bean
        public DataServerCache dataServerCache() {
            return new DataServerCache();
        }

        @Bean
        public ForwardService forwardService() {
            return new ForwardServiceImpl();
        }

        @Bean
        public SessionServerConnectionFactory sessionServerConnectionFactory() {
            return new SessionServerConnectionFactory();
        }

        @Bean
        public DataServerConnectionFactory dataServerConnectionFactory() {
            return new DataServerConnectionFactory();
        }

        @Bean
        public MetaServerConnectionFactory metaServerConnectionFactory() {
            return new MetaServerConnectionFactory();
        }

        @Bean(name = "serverHandlers")
        public Collection<AbstractServerHandler> serverHandlers() {
            Collection<AbstractServerHandler> list = new ArrayList<>();
            list.add(getDataHandler());
            list.add(clientOffHandler());
            list.add(getDataVersionsHandler());
            list.add(publishDataProcessor());
            list.add(sessionServerRegisterHandler());
            list.add(unPublishDataHandler());
            list.add(dataServerConnectionHandler());
            list.add(renewDatumHandler());
            list.add(datumSnapshotHandler());
            return list;
        }

        @Bean(name = "serverSyncHandlers")
        public Collection<AbstractServerHandler> serverSyncHandlers() {
            Collection<AbstractServerHandler> list = new ArrayList<>();
            list.add(getDataHandler());
            list.add(publishDataProcessor());
            list.add(unPublishDataHandler());
            list.add(notifyFetchDatumHandler());
            list.add(notifyOnlineHandler());
            list.add(syncDataHandler());
            list.add(dataSyncServerConnectionHandler());
            return list;
        }

        @Bean(name = "dataClientHandlers")
        public Collection<AbstractClientHandler> dataClientHandlers() {
            Collection<AbstractClientHandler> list = new ArrayList<>();
            list.add(notifyDataSyncHandler());
            list.add(fetchDataHandler());
            return list;
        }

        @Bean(name = "metaClientHandlers")
        public Collection<AbstractClientHandler> metaClientHandlers() {
            Collection<AbstractClientHandler> list = new ArrayList<>();
            list.add(serverChangeHandler());
            list.add(statusConfirmHandler());
            list.add(notifyProvideDataChangeHandler());
            return list;
        }

        @Bean
        public AbstractServerHandler dataServerConnectionHandler() {
            return new DataServerConnectionHandler();
        }

        @Bean
        public AbstractServerHandler dataSyncServerConnectionHandler() {
            return new DataSyncServerConnectionHandler();
        }

        @Bean
        public AbstractServerHandler getDataHandler() {
            return new GetDataHandler();
        }

        @Bean
        public AbstractServerHandler getDataVersionsHandler() {
            return new GetDataVersionsHandler();
        }

        @Bean
        public AbstractServerHandler clientOffHandler() {
            return new ClientOffHandler();
        }

        @Bean
        public AbstractServerHandler datumSnapshotHandler() {
            return new DatumSnapshotHandler();
        }

        @Bean
        public RenewDatumHandler renewDatumHandler() {
            return new RenewDatumHandler();
        }

        @Bean
        public AbstractServerHandler publishDataProcessor() {
            return new PublishDataHandler();
        }

        @Bean
        public AbstractServerHandler sessionServerRegisterHandler() {
            return new SessionServerRegisterHandler();
        }

        @Bean
        public AbstractServerHandler unPublishDataHandler() {
            return new UnPublishDataHandler();
        }

        @Bean
        public AbstractServerHandler notifyFetchDatumHandler() {
            return new NotifyFetchDatumHandler();
        }

        @Bean
        public AbstractServerHandler notifyOnlineHandler() {
            return new NotifyOnlineHandler();
        }

        @Bean
        public AbstractServerHandler syncDataHandler() {
            return new SyncDataHandler();
        }

        @Bean
        @ConditionalOnMissingBean
        public NotifyDataSyncHandler notifyDataSyncHandler() {
            return new NotifyDataSyncHandler();
        }

        @Bean
        public AbstractClientHandler fetchDataHandler() {
            return new FetchDataHandler();
        }

        @Bean
        public AbstractClientHandler serverChangeHandler() {
            return new ServerChangeHandler();
        }

        @Bean
        public AbstractClientHandler statusConfirmHandler() {
            return new StatusConfirmHandler();
        }

        @Bean
        public NotifyProvideDataChangeHandler notifyProvideDataChangeHandler() {
            return new NotifyProvideDataChangeHandler();
        }

        @Bean(name = "afterWorkProcessors")
        public List<AfterWorkingProcess> afterWorkingProcessors() {
            List<AfterWorkingProcess> list = new ArrayList<>();
            list.add(renewDatumHandler());
            list.add(datumLeaseManager());
            list.add(disconnectEventHandler());
            list.add(notifyDataSyncHandler());
            return list;
        }

        @Bean
        public AfterWorkingProcessHandler afterWorkingProcessHandler() {
            return new AfterWorkingProcessHandler();
        }

        @Bean
        public DatumLeaseManager datumLeaseManager() {
            return new DatumLeaseManager();
        }

        @Bean
        public DisconnectEventHandler disconnectEventHandler() {
            return new DisconnectEventHandler();
        }

    }

    @Configuration
    public static class DataServerNotifyBeanConfiguration {
        @Bean
        public DataChangeHandler dataChangeHandler() {
            return new DataChangeHandler();
        }

        @Bean
        public SessionServerNotifier sessionServerNotifier() {
            return new SessionServerNotifier();
        }

        @Bean
        public TempPublisherNotifier tempPublisherNotifier() {
            return new TempPublisherNotifier();
        }

        @Bean
        public BackUpNotifier backUpNotifier() {
            return new BackUpNotifier();
        }

        @Bean
        public SnapshotBackUpNotifier snapshotBackUpNotifier() {
            return new SnapshotBackUpNotifier();
        }

        @Bean(name = "dataChangeNotifiers")
        public List<IDataChangeNotifier> dataChangeNotifiers() {
            List<IDataChangeNotifier> list = new ArrayList<>();
            list.add(sessionServerNotifier());
            list.add(tempPublisherNotifier());
            list.add(backUpNotifier());
            list.add(snapshotBackUpNotifier());
            return list;
        }

    }

    @Configuration
    public static class DataServerSyncBeanConfiguration {

        @Bean
        public SyncDataService syncDataService() {
            return new SyncDataServiceImpl();
        }

        @Bean
        public AcceptorStore localAcceptorStore() {
            return new LocalAcceptorStore();
        }

        @Bean
        public Scheduler syncDataScheduler() {
            return new Scheduler();
        }

        @Bean
        public StoreServiceFactory storeServiceFactory() {
            return new StoreServiceFactory();
        }

    }

    @Configuration
    public static class DataServerEventBeanConfiguration {

        @Bean
        public DataServerChangeEventHandler dataServerChangeEventHandler() {
            return new DataServerChangeEventHandler();
        }

        @Bean
        public LocalDataServerChangeEventHandler localDataServerChangeEventHandler() {
            return new LocalDataServerChangeEventHandler();
        }

        @Bean
        public MetaServerChangeEventHandler metaServerChangeEventHandler() {
            return new MetaServerChangeEventHandler();
        }

        @Bean
        public StartTaskEventHandler startTaskEventHandler() {
            return new StartTaskEventHandler();
        }

        @Bean
        public LocalDataServerCleanHandler localDataServerCleanHandler() {
            return new LocalDataServerCleanHandler();
        }

        @Bean
        public GetSyncDataHandler getSyncDataHandler() {
            return new GetSyncDataHandler();
        }

        @Bean
        public EventCenter eventCenter() {
            return new EventCenter();
        }

        @Bean
        public DataChangeEventCenter dataChangeEventCenter() {
            return new DataChangeEventCenter();
        }

    }

    @Configuration
    public static class DataServerRemotingBeanConfiguration {

        @Bean
        public ConnectionRefreshTask connectionRefreshTask() {
            return new ConnectionRefreshTask();
        }

        @Bean
        public ConnectionRefreshMetaTask connectionRefreshMetaTask() {
            return new ConnectionRefreshMetaTask();
        }

        @Bean
        public RenewNodeTask renewNodeTask() {
            return new RenewNodeTask();
        }

        @Bean(name = "tasks")
        public List<AbstractTask> tasks() {
            List<AbstractTask> list = new ArrayList<>();
            list.add(connectionRefreshTask());
            list.add(connectionRefreshMetaTask());
            list.add(renewNodeTask());
            return list;
        }

        @Bean
        public IMetaServerService metaServerService() {
            return new DefaultMetaServiceImpl();
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
        public HealthResource healthResource() {
            return new HealthResource();
        }

        @Bean
        @ConditionalOnMissingBean
        public DataDigestResource dataDigestResource() {
            return new DataDigestResource();
        }

    }

    @Configuration
    public static class ExecutorConfiguration {

        @Bean(name = "publishProcessorExecutor")
        public ThreadPoolExecutor publishProcessorExecutor(DataServerConfig dataServerConfig) {
            return new ThreadPoolExecutorDataServer("PublishProcessorExecutor",
                dataServerConfig.getPublishExecutorMinPoolSize(),
                dataServerConfig.getPublishExecutorMaxPoolSize(), 300, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(dataServerConfig.getPublishExecutorQueueSize()),
                new NamedThreadFactory("DataServer-PublishProcessor-executor", true));
        }

        @Bean(name = "renewDatumProcessorExecutor")
        public ThreadPoolExecutor renewDatumProcessorExecutor(DataServerConfig dataServerConfig) {
            return new ThreadPoolExecutorDataServer("RenewDatumProcessorExecutor",
                dataServerConfig.getRenewDatumExecutorMinPoolSize(),
                dataServerConfig.getRenewDatumExecutorMaxPoolSize(), 300, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(dataServerConfig.getRenewDatumExecutorQueueSize()),
                new NamedThreadFactory("DataServer-RenewDatumProcessor-executor", true));
        }

        @Bean(name = "getDataProcessorExecutor")
        public ThreadPoolExecutor getDataProcessorExecutor(DataServerConfig dataServerConfig) {
            return new ThreadPoolExecutorDataServer("GetDataProcessorExecutor",
                dataServerConfig.getGetDataExecutorMinPoolSize(),
                dataServerConfig.getGetDataExecutorMaxPoolSize(),
                dataServerConfig.getGetDataExecutorKeepAliveTime(), TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(dataServerConfig.getGetDataExecutorQueueSize()),
                new NamedThreadFactory("DataServer-GetDataProcessor-executor", true));
        }

    }

    @Configuration
    public static class DataProvideDataConfiguration {

        @Bean
        public ProvideDataProcessor provideDataProcessorManager() {
            return new ProvideDataProcessorManager();
        }

        @Bean
        public ProvideDataProcessor datumExpireProvideDataProcessor(ProvideDataProcessor provideDataProcessorManager) {
            ProvideDataProcessor datumExpireProvideDataProcessor = new DatumExpireProvideDataProcessor();
            ((ProvideDataProcessorManager) provideDataProcessorManager)
                .addProvideDataProcessor(datumExpireProvideDataProcessor);
            return datumExpireProvideDataProcessor;
        }

    }

}