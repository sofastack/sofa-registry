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

import com.alipay.sofa.registry.remoting.bolt.exchange.BoltExchange;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.remoting.jersey.exchange.JerseyExchange;
import com.alipay.sofa.registry.server.data.cache.CacheDigestTask;
import com.alipay.sofa.registry.server.data.cache.DatumCache;
import com.alipay.sofa.registry.server.data.cache.DatumStorage;
import com.alipay.sofa.registry.server.data.cache.LocalDatumStorage;
import com.alipay.sofa.registry.server.data.change.DataChangeHandler;
import com.alipay.sofa.registry.server.data.change.event.DataChangeEventCenter;
import com.alipay.sofa.registry.server.data.change.notify.IDataChangeNotifier;
import com.alipay.sofa.registry.server.data.change.notify.SessionServerNotifier;
import com.alipay.sofa.registry.server.data.change.notify.TempPublisherNotifier;
import com.alipay.sofa.registry.server.data.event.EventCenter;
import com.alipay.sofa.registry.server.data.lease.SessionLeaseManager;
import com.alipay.sofa.registry.server.data.remoting.DataNodeExchanger;
import com.alipay.sofa.registry.server.data.remoting.MetaNodeExchanger;
import com.alipay.sofa.registry.server.data.remoting.SessionNodeExchanger;
import com.alipay.sofa.registry.server.data.remoting.dataserver.handler.SlotFollowerDiffDataInfoIdRequestHandler;
import com.alipay.sofa.registry.server.data.remoting.dataserver.handler.SlotFollowerDiffPublisherRequestHandler;
import com.alipay.sofa.registry.server.data.remoting.handler.AbstractClientHandler;
import com.alipay.sofa.registry.server.data.remoting.handler.AbstractServerHandler;
import com.alipay.sofa.registry.server.data.remoting.metaserver.MetaServerServiceImpl;
import com.alipay.sofa.registry.server.data.remoting.metaserver.RaftClientManager;
import com.alipay.sofa.registry.server.data.remoting.metaserver.handler.NotifyProvideDataChangeHandler;
import com.alipay.sofa.registry.server.data.remoting.metaserver.provideData.ProvideDataProcessor;
import com.alipay.sofa.registry.server.data.remoting.metaserver.provideData.ProvideDataProcessorManager;
import com.alipay.sofa.registry.server.data.remoting.metaserver.provideData.processor.DatumExpireProvideDataProcessor;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.SessionServerConnectionFactory;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.handler.*;
import com.alipay.sofa.registry.server.data.resource.DataDigestResource;
import com.alipay.sofa.registry.server.data.resource.HealthResource;
import com.alipay.sofa.registry.server.data.slot.SlotManager;
import com.alipay.sofa.registry.server.data.slot.SlotManagerImpl;
import com.alipay.sofa.registry.server.data.util.ThreadPoolExecutorDataServer;
import com.alipay.sofa.registry.util.NamedThreadFactory;
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
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
        public DatumStorage localDatumStorage() {
            return new LocalDatumStorage();
        }

        @Bean
        @ConditionalOnMissingBean
        public SlotManager slotManager() {
            return new SlotManagerImpl();
        }

        @Bean
        @ConditionalOnMissingBean
        public SessionLeaseManager sessionLeaseManager() {
            return new SessionLeaseManager();
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
        public DataNodeExchanger dataNodeExchanger() {
            return new DataNodeExchanger();
        }

        @Bean
        public SessionNodeExchanger sessionNodeExchanger() {
            return new SessionNodeExchanger();
        }

        @Bean
        public SessionServerConnectionFactory sessionServerConnectionFactory() {
            return new SessionServerConnectionFactory();
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
            return list;
        }

        @Bean(name = "serverSyncHandlers")
        public Collection<AbstractServerHandler> serverSyncHandlers() {
            Collection<AbstractServerHandler> list = new ArrayList<>();
            list.add(slotFollowerDiffDataInfoIdRequestHandler());
            list.add(slotFollowerDiffPublisherRequestHandler());
            return list;
        }

        @Bean(name = "metaClientHandlers")
        public Collection<AbstractClientHandler> metaClientHandlers() {
            Collection<AbstractClientHandler> list = new ArrayList<>();
            list.add(notifyProvideDataChangeHandler());
            return list;
        }

        @Bean
        public AbstractServerHandler dataServerConnectionHandler() {
            return new DataServerConnectionHandler();
        }

        @Bean
        public AbstractServerHandler getDataHandler() {
            return new GetDataHandler();
        }

        @Bean
        public AbstractServerHandler slotFollowerDiffDataInfoIdRequestHandler() {
            return new SlotFollowerDiffDataInfoIdRequestHandler();
        }

        @Bean
        public AbstractServerHandler slotFollowerDiffPublisherRequestHandler() {
            return new SlotFollowerDiffPublisherRequestHandler();
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
        public NotifyProvideDataChangeHandler notifyProvideDataChangeHandler() {
            return new NotifyProvideDataChangeHandler();
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

        @Bean(name = "dataChangeNotifiers")
        public List<IDataChangeNotifier> dataChangeNotifiers() {
            List<IDataChangeNotifier> list = new ArrayList<>();
            list.add(sessionServerNotifier());
            list.add(tempPublisherNotifier());
            return list;
        }

    }

    @Configuration
    public static class DataServerEventBeanConfiguration {

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
        public RaftClientManager raftClientManager() {
            return new RaftClientManager();
        }

        @Bean
        public MetaNodeExchanger metaNodeExchanger() {
            return new MetaNodeExchanger();
        }

        @Bean
        public MetaServerServiceImpl metaServerService() {
            return new MetaServerServiceImpl();
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

        @Bean(name = "getDataProcessorExecutor")
        public ThreadPoolExecutor getDataProcessorExecutor(DataServerConfig dataServerConfig) {
            return new ThreadPoolExecutorDataServer("GetDataProcessorExecutor",
                dataServerConfig.getGetDataExecutorMinPoolSize(),
                dataServerConfig.getGetDataExecutorMaxPoolSize(),
                dataServerConfig.getGetDataExecutorKeepAliveTime(), TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(dataServerConfig.getGetDataExecutorQueueSize()),
                new NamedThreadFactory("DataServer-GetDataProcessor-executor", true));
        }

        @Bean(name = "slotSyncRequestProcessorExecutor")
        public ThreadPoolExecutor slotSyncRequestProcessorExecutor(DataServerConfig dataServerConfig) {
            return new ThreadPoolExecutorDataServer("SlotSyncRequestProcessorExecutor",
                dataServerConfig.getSlotSyncRequestExecutorMinPoolSize(),
                dataServerConfig.getSlotSyncRequestExecutorMaxPoolSize(), 300, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(dataServerConfig.getSlotSyncRequestExecutorQueueSize()),
                new NamedThreadFactory("DataServer-SlotSyncRequestProcessor-executor", true));
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