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

import com.alipay.sofa.registry.common.model.slot.filter.SyncSlotAcceptAllManager;
import com.alipay.sofa.registry.common.model.slot.filter.SyncSlotAcceptorManager;
import com.alipay.sofa.registry.jdbc.config.JdbcConfiguration;
import com.alipay.sofa.registry.remoting.bolt.exchange.BoltExchange;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.remoting.jersey.exchange.JerseyExchange;
import com.alipay.sofa.registry.server.data.cache.DatumStorageDelegate;
import com.alipay.sofa.registry.server.data.change.DataChangeEventCenter;
import com.alipay.sofa.registry.server.data.lease.SessionLeaseManager;
import com.alipay.sofa.registry.server.data.multi.cluster.sync.info.FetchMultiSyncService;
import com.alipay.sofa.registry.server.data.providedata.CompressDatumService;
import com.alipay.sofa.registry.server.data.providedata.FetchStopPushService;
import com.alipay.sofa.registry.server.data.remoting.DataMetaServerManager;
import com.alipay.sofa.registry.server.data.remoting.DataNodeExchanger;
import com.alipay.sofa.registry.server.data.remoting.SessionNodeExchanger;
import com.alipay.sofa.registry.server.data.remoting.dataserver.handler.SlotFollowerDiffDigestRequestHandler;
import com.alipay.sofa.registry.server.data.remoting.dataserver.handler.SlotFollowerDiffPublisherRequestHandler;
import com.alipay.sofa.registry.server.data.remoting.metaserver.MetaServerServiceImpl;
import com.alipay.sofa.registry.server.data.remoting.metaserver.handler.NotifyProvideDataChangeHandler;
import com.alipay.sofa.registry.server.data.remoting.metaserver.handler.RemoteDatumClearEventHandler;
import com.alipay.sofa.registry.server.data.remoting.metaserver.provideData.ProvideDataProcessorManager;
import com.alipay.sofa.registry.server.data.remoting.metaserver.provideData.processor.SessionLeaseProvideDataProcessor;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.handler.*;
import com.alipay.sofa.registry.server.data.resource.DataDigestResource;
import com.alipay.sofa.registry.server.data.resource.DatumApiResource;
import com.alipay.sofa.registry.server.data.resource.HealthResource;
import com.alipay.sofa.registry.server.data.resource.SlotTableStatusResource;
import com.alipay.sofa.registry.server.data.slot.SlotAccessorDelegate;
import com.alipay.sofa.registry.server.data.slot.SlotChangeListenerManager;
import com.alipay.sofa.registry.server.data.slot.SlotManager;
import com.alipay.sofa.registry.server.data.slot.SlotManagerImpl;
import com.alipay.sofa.registry.server.data.timer.CacheCountTask;
import com.alipay.sofa.registry.server.data.timer.CacheDigestTask;
import com.alipay.sofa.registry.server.shared.config.CommonConfig;
import com.alipay.sofa.registry.server.shared.meta.MetaLeaderExchanger;
import com.alipay.sofa.registry.server.shared.providedata.FetchSystemPropertyService;
import com.alipay.sofa.registry.server.shared.providedata.ProvideDataProcessor;
import com.alipay.sofa.registry.server.shared.providedata.SystemPropertyProcessorManager;
import com.alipay.sofa.registry.server.shared.remoting.AbstractClientHandler;
import com.alipay.sofa.registry.server.shared.remoting.AbstractServerHandler;
import com.alipay.sofa.registry.server.shared.remoting.SlotTableChangeEventHandler;
import com.alipay.sofa.registry.server.shared.resource.MetricsResource;
import com.alipay.sofa.registry.server.shared.resource.RegistryOpsResource;
import com.alipay.sofa.registry.server.shared.resource.SlotGenericResource;
import com.alipay.sofa.registry.server.shared.resource.VersionResource;
import com.alipay.sofa.registry.server.shared.slot.DiskSlotTableRecorder;
import com.alipay.sofa.registry.store.api.config.StoreApiConfiguration;
import com.alipay.sofa.registry.task.MetricsableThreadPoolExecutor;
import com.alipay.sofa.registry.util.NamedThreadFactory;
import com.alipay.sofa.registry.util.PropertySplitter;
import java.util.ArrayList;
import java.util.Collection;
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

/**
 * @author qian.lqlq
 * @version $Id: DataServerBeanConfiguration.java, v 0.1 2018-01-11 15:08 qian.lqlq Exp $
 */
@Configuration
@Import({
  DataServerInitializer.class,
  StoreApiConfiguration.class,
  JdbcConfiguration.class,
})
@EnableConfigurationProperties
public class DataServerBeanConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public DataServerBootstrap dataServerBootstrap() {
    return new DataServerBootstrap();
  }

  @Configuration
  public static class DataServerBootstrapConfigConfiguration {

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
    public DatumStorageDelegate datumStorageDelegate(DataServerConfig dataServerConfig) {
      return new DatumStorageDelegate(dataServerConfig);
    }

    @Bean
    @ConditionalOnMissingBean
    public SlotManager slotManager() {
      return new SlotManagerImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public SlotChangeListenerManager slotChangeListenerManager() {
      return new SlotChangeListenerManager();
    }

    @Bean
    @ConditionalOnMissingBean
    public SlotAccessorDelegate slotAccessorDelegate() {
      return new SlotAccessorDelegate();
    }

    @Bean
    public SyncSlotAcceptorManager syncSlotAcceptAllManager() {
      return new SyncSlotAcceptAllManager();
    }

    @Bean
    @ConditionalOnMissingBean
    public SessionLeaseManager sessionLeaseManager() {
      return new SessionLeaseManager();
    }

    @Bean
    public DiskSlotTableRecorder diskSlotTableRecorder() {
      return new DiskSlotTableRecorder();
    }
  }

  @Configuration
  public static class LogTaskConfigConfiguration {

    @Bean
    public CacheDigestTask cacheDigestTask() {
      return new CacheDigestTask();
    }

    @Bean
    public CacheCountTask cacheCountTask() {
      return new CacheCountTask();
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

    @Bean(name = "serverHandlers")
    public Collection<AbstractServerHandler> serverHandlers() {
      Collection<AbstractServerHandler> list = new ArrayList<>();
      list.add(getDataHandler());
      list.add(getMultiDataHandler());
      list.add(batchPutDataHandler());
      list.add(getDataVersionsHandler());
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
      list.add(slotTableChangeEventHandler());
      list.add(remoteDatumClearEventHandler());
      return list;
    }

    @Bean
    public AbstractServerHandler getDataHandler() {
      return new GetDataHandler();
    }

    @Bean
    public AbstractServerHandler getMultiDataHandler() {
      return new GetMultiDataHandler();
    }

    @Bean
    public AbstractServerHandler slotFollowerDiffDataInfoIdRequestHandler() {
      return new SlotFollowerDiffDigestRequestHandler();
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
    public AbstractServerHandler batchPutDataHandler() {
      return new BatchPutDataHandler();
    }

    @Bean
    public NotifyProvideDataChangeHandler notifyProvideDataChangeHandler() {
      return new NotifyProvideDataChangeHandler();
    }

    @Bean
    public SlotTableChangeEventHandler slotTableChangeEventHandler() {
      return new SlotTableChangeEventHandler();
    }

    @Bean
    public RemoteDatumClearEventHandler remoteDatumClearEventHandler() {
      return new RemoteDatumClearEventHandler();
    }
  }

  @Configuration
  public static class DataServerEventBeanConfiguration {

    @Bean
    public DataChangeEventCenter dataChangeEventCenter() {
      return new DataChangeEventCenter();
    }
  }

  @Configuration
  public static class DataServerRemotingBeanConfiguration {

    @Bean
    public MetaLeaderExchanger metaLeaderExchanger() {
      return new DataMetaServerManager();
    }

    @Bean
    public MetaServerServiceImpl metaServerService() {
      return new MetaServerServiceImpl();
    }

    @Bean
    public FetchMultiSyncService fetchMultiSyncService() {
      return new FetchMultiSyncService();
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
    public DatumApiResource datumApiResource() {
      return new DatumApiResource();
    }

    @Bean
    @ConditionalOnMissingBean
    public DataDigestResource dataDigestResource() {
      return new DataDigestResource();
    }

    @Bean
    public SlotGenericResource slotGenericResource() {
      return new SlotGenericResource();
    }

    @Bean
    public MetricsResource metricsResource() {
      return new MetricsResource();
    }

    @Bean
    public RegistryOpsResource opsResource() {
      return new RegistryOpsResource();
    }

    @Bean
    @ConditionalOnMissingBean
    public VersionResource versionResource() {
      return new VersionResource();
    }

    @Bean
    public SlotTableStatusResource slotTableResource() {
      return new SlotTableStatusResource();
    }
  }

  @Configuration
  public static class ExecutorConfiguration {

    @Bean(name = "publishProcessorExecutor")
    public ThreadPoolExecutor publishProcessorExecutor(DataServerConfig dataServerConfig) {
      return new MetricsableThreadPoolExecutor(
          "PublishProcessorExecutor",
          dataServerConfig.getPublishExecutorMinPoolSize(),
          dataServerConfig.getPublishExecutorMaxPoolSize(),
          300,
          TimeUnit.SECONDS,
          new ArrayBlockingQueue<>(dataServerConfig.getPublishExecutorQueueSize()),
          new NamedThreadFactory("PutExecutor", true));
    }

    @Bean(name = "getDataProcessorExecutor")
    public ThreadPoolExecutor getDataProcessorExecutor(DataServerConfig dataServerConfig) {
      return new MetricsableThreadPoolExecutor(
          "GetDataProcessorExecutor",
          dataServerConfig.getGetDataExecutorMinPoolSize(),
          dataServerConfig.getGetDataExecutorMaxPoolSize(),
          dataServerConfig.getGetDataExecutorKeepAliveTime(),
          TimeUnit.SECONDS,
          new ArrayBlockingQueue<>(dataServerConfig.getGetDataExecutorQueueSize()),
          new NamedThreadFactory("GetExecutor", true));
    }

    @Bean(name = "slotSyncRequestProcessorExecutor")
    public ThreadPoolExecutor slotSyncRequestProcessorExecutor(DataServerConfig dataServerConfig) {
      return new MetricsableThreadPoolExecutor(
          "SlotSyncRequestProcessorExecutor",
          dataServerConfig.getSlotSyncRequestExecutorMinPoolSize(),
          dataServerConfig.getSlotSyncRequestExecutorMaxPoolSize(),
          300,
          TimeUnit.SECONDS,
          new ArrayBlockingQueue<>(dataServerConfig.getSlotSyncRequestExecutorQueueSize()),
          new NamedThreadFactory("SyncExecutor", true));
    }

    @Bean(name = "metaNodeExecutor")
    public ThreadPoolExecutor metaNodeExecutor(DataServerConfig dataServerConfig) {
      return new MetricsableThreadPoolExecutor(
          "metaNodeInDataExecutor",
          dataServerConfig.getMetaNodeExecutorPoolSize(),
          dataServerConfig.getMetaNodeExecutorPoolSize(),
          300,
          TimeUnit.SECONDS,
          new ArrayBlockingQueue<>(dataServerConfig.getMetaNodeExecutorQueueSize()),
          new NamedThreadFactory("metaNodeInDataExecutor", true));
    }
  }

  @Configuration
  public static class DataProvideDataConfiguration {

    @Bean
    public ProvideDataProcessor provideDataProcessorManager() {
      return new ProvideDataProcessorManager();
    }

    @Bean
    public ProvideDataProcessor sessionLeaseProvideDataProcessor(
        ProvideDataProcessor provideDataProcessorManager) {
      ProvideDataProcessor sessionLeaseProvideDataProcessor =
          new SessionLeaseProvideDataProcessor();
      ((ProvideDataProcessorManager) provideDataProcessorManager)
          .addProvideDataProcessor(sessionLeaseProvideDataProcessor);
      return sessionLeaseProvideDataProcessor;
    }

    @Bean
    public SystemPropertyProcessorManager systemPropertyProcessorManager() {
      return new SystemPropertyProcessorManager();
    }

    @Bean
    public FetchSystemPropertyService compressDatumService(
        SystemPropertyProcessorManager systemPropertyProcessorManager) {
      CompressDatumService compressDatumService = new CompressDatumService();
      systemPropertyProcessorManager.addSystemDataProcessor(compressDatumService);
      return compressDatumService;
    }

    @Bean
    public FetchSystemPropertyService fetchStopPushService(
        SystemPropertyProcessorManager systemPropertyProcessorManager) {
      FetchStopPushService fetchStopPushService = new FetchStopPushService();
      systemPropertyProcessorManager.addSystemDataPersistenceProcessor(fetchStopPushService);
      return fetchStopPushService;
    }
  }
}
