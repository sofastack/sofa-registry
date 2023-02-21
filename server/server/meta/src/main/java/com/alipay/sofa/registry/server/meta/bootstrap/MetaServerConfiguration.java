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

import com.alipay.sofa.registry.jdbc.config.JdbcConfiguration;
import com.alipay.sofa.registry.jdbc.config.JdbcElectorConfiguration;
import com.alipay.sofa.registry.jraft.config.RaftConfiguration;
import com.alipay.sofa.registry.remoting.bolt.exchange.BoltExchange;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.remoting.jersey.exchange.JerseyExchange;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfigBean;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MultiClusterMetaServerConfig;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfigBeanProperty;
import com.alipay.sofa.registry.server.meta.cleaner.AppRevisionCleaner;
import com.alipay.sofa.registry.server.meta.cleaner.InterfaceAppsIndexCleaner;
import com.alipay.sofa.registry.server.meta.lease.filter.DefaultForbiddenServerManager;
import com.alipay.sofa.registry.server.meta.lease.filter.RegistryForbiddenServerManager;
import com.alipay.sofa.registry.server.meta.provide.data.DefaultClientManagerService;
import com.alipay.sofa.registry.server.meta.provide.data.DefaultProvideDataService;
import com.alipay.sofa.registry.server.meta.provide.data.FetchStopPushService;
import com.alipay.sofa.registry.server.meta.provide.data.NodeOperatingService;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataService;
import com.alipay.sofa.registry.server.meta.remoting.DataNodeExchanger;
import com.alipay.sofa.registry.server.meta.remoting.MetaServerExchanger;
import com.alipay.sofa.registry.server.meta.remoting.SessionNodeExchanger;
import com.alipay.sofa.registry.server.meta.remoting.connection.DataConnectionManager;
import com.alipay.sofa.registry.server.meta.remoting.connection.MetaConnectionManager;
import com.alipay.sofa.registry.server.meta.remoting.connection.SessionConnectionManager;
import com.alipay.sofa.registry.server.meta.remoting.handler.FetchProvideDataRequestHandler;
import com.alipay.sofa.registry.server.meta.remoting.handler.FetchSystemPropertyRequestHandler;
import com.alipay.sofa.registry.server.meta.remoting.handler.GetSlotTableStatusRequestHandler;
import com.alipay.sofa.registry.server.meta.remoting.handler.HeartbeatRequestHandler;
import com.alipay.sofa.registry.server.meta.remoting.handler.RegistryForbiddenServerHandler;
import com.alipay.sofa.registry.server.meta.remoting.meta.LocalMetaExchanger;
import com.alipay.sofa.registry.server.meta.remoting.meta.MetaServerRenewService;
import com.alipay.sofa.registry.server.meta.resource.BlacklistDataResource;
import com.alipay.sofa.registry.server.meta.resource.CircuitBreakerResources;
import com.alipay.sofa.registry.server.meta.resource.ClientManagerResource;
import com.alipay.sofa.registry.server.meta.resource.CompressResource;
import com.alipay.sofa.registry.server.meta.resource.HealthResource;
import com.alipay.sofa.registry.server.meta.resource.MetaCenterResource;
import com.alipay.sofa.registry.server.meta.resource.MetaDigestResource;
import com.alipay.sofa.registry.server.meta.resource.MetaLeaderResource;
import com.alipay.sofa.registry.server.meta.resource.ProvideDataResource;
import com.alipay.sofa.registry.server.meta.resource.RecoverConfigResource;
import com.alipay.sofa.registry.server.meta.resource.RegistryCoreOpsResource;
import com.alipay.sofa.registry.server.meta.resource.ShutdownSwitchResource;
import com.alipay.sofa.registry.server.meta.resource.SlotSyncResource;
import com.alipay.sofa.registry.server.meta.resource.SlotTableResource;
import com.alipay.sofa.registry.server.meta.resource.StopPushDataResource;
import com.alipay.sofa.registry.server.meta.resource.filter.LeaderAwareFilter;
import com.alipay.sofa.registry.server.meta.slot.status.SlotTableStatusService;
import com.alipay.sofa.registry.server.shared.config.CommonConfig;
import com.alipay.sofa.registry.server.shared.remoting.AbstractServerHandler;
import com.alipay.sofa.registry.server.shared.resource.MetricsResource;
import com.alipay.sofa.registry.server.shared.resource.SlotGenericResource;
import com.alipay.sofa.registry.server.shared.resource.VersionResource;
import com.alipay.sofa.registry.store.api.config.StoreApiConfiguration;
import com.alipay.sofa.registry.task.MetricsableThreadPoolExecutor;
import com.alipay.sofa.registry.util.DefaultExecutorFactory;
import com.alipay.sofa.registry.util.NamedThreadFactory;
import com.alipay.sofa.registry.util.OsUtils;
import com.alipay.sofa.registry.util.PropertySplitter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
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
 * @author shangyu.wh
 * @version $Id: MetaServerConfiguration.java, v 0.1 2018-01-12 14:53 shangyu.wh Exp $
 */
@Configuration
@Import({
  MetaServerInitializerConfiguration.class,
  StoreApiConfiguration.class,
  JdbcConfiguration.class,
  JdbcElectorConfiguration.class,
  RaftConfiguration.class
})
@EnableConfigurationProperties
public class MetaServerConfiguration {

  public static final String SHARED_SCHEDULE_EXECUTOR = "sharedScheduleExecutor";
  public static final String GLOBAL_EXECUTOR = "globalExecutor";

  @Bean
  @ConditionalOnMissingBean
  public MetaServerBootstrap metaServerBootstrap() {
    return new MetaServerBootstrap();
  }

  @Configuration
  protected static class MetaServerConfigBeanConfiguration {

    @Bean
    public CommonConfig commonConfig() {
      return new CommonConfig();
    }

    @Bean
    @ConditionalOnMissingBean
    public MetaServerConfig metaServerConfig(CommonConfig commonConfig) {
      return new MetaServerConfigBean(commonConfig);
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
  public static class ThreadPoolResourceConfiguration {
    @Bean(name = GLOBAL_EXECUTOR)
    public ExecutorService getGlobalExecutorService() {
      int corePoolSize = Math.min(OsUtils.getCpuCount() * 2, 8);
      int maxPoolSize = 50 * OsUtils.getCpuCount();
      DefaultExecutorFactory executorFactory =
          DefaultExecutorFactory.builder()
              .threadNamePrefix(GLOBAL_EXECUTOR)
              .corePoolSize(corePoolSize)
              .maxPoolSize(maxPoolSize)
              .rejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy())
              .build();
      return executorFactory.create();
    }

    @Bean(name = SHARED_SCHEDULE_EXECUTOR)
    public ScheduledExecutorService getScheduledService() {
      return new ScheduledThreadPoolExecutor(
          Math.min(OsUtils.getCpuCount() * 2, 12),
          new NamedThreadFactory("MetaServerGlobalScheduler"));
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
      list.add(heartbeatRequestHandler());
      list.add(fetchProvideDataRequestHandler());
      list.add(fetchSystemPropertyRequestHandler());
      list.add(registryForbiddenServerHandler());
      list.add(getSlotTableStatusRequestHandler());
      return list;
    }

    @Bean(name = "dataServerHandlers")
    public Collection<AbstractServerHandler> dataServerHandlers() {
      Collection<AbstractServerHandler> list = new ArrayList<>();
      list.add(heartbeatRequestHandler());
      list.add(fetchProvideDataRequestHandler());
      list.add(registryForbiddenServerHandler());
      list.add(fetchSystemPropertyRequestHandler());
      list.add(getSlotTableStatusRequestHandler());
      return list;
    }

    @Bean(name = "metaServerHandlers")
    public Collection<AbstractServerHandler> metaServerHandlers() {
      Collection<AbstractServerHandler> list = new ArrayList<>();
      list.add(heartbeatRequestHandler());
      return list;
    }

    @Bean
    public SessionConnectionManager sessionConnectionManager() {
      return new SessionConnectionManager();
    }

    @Bean
    public DataConnectionManager dataConnectionManager() {
      return new DataConnectionManager();
    }

    @Bean
    public MetaConnectionManager metaConnectionManager() {
      return new MetaConnectionManager();
    }

    @Bean
    public HeartbeatRequestHandler heartbeatRequestHandler() {
      return new HeartbeatRequestHandler();
    }

    @Bean
    public GetSlotTableStatusRequestHandler getSlotTableStatusRequestHandler() {
      return new GetSlotTableStatusRequestHandler();
    }

    @Bean
    public FetchProvideDataRequestHandler fetchProvideDataRequestHandler() {
      return new FetchProvideDataRequestHandler();
    }

    @Bean
    public FetchSystemPropertyRequestHandler fetchSystemPropertyRequestHandler() {
      return new FetchSystemPropertyRequestHandler();
    }

    @Bean
    public SessionNodeExchanger sessionNodeExchanger() {
      return new SessionNodeExchanger();
    }

    @Bean
    public DataNodeExchanger dataNodeExchanger() {
      return new DataNodeExchanger();
    }

    @Bean
    public MetaServerExchanger metaServerExchanger() {
      return new MetaServerExchanger();
    }

    @Bean
    public RegistryForbiddenServerHandler registryForbiddenServerHandler() {
      return new RegistryForbiddenServerHandler();
    }

    @Bean
    public LocalMetaExchanger localMetaExchanger() {
      return new LocalMetaExchanger();
    }

    @Bean
    public MetaServerRenewService metaServerRenewService() {
      return new MetaServerRenewService();
    }

    @Bean
    public SlotTableStatusService slotTableStatusService() {
      return new SlotTableStatusService();
    }

    @Bean
    @ConditionalOnMissingBean(name = "registryForbiddenServerManager")
    public RegistryForbiddenServerManager registryForbiddenServerManager() {
      return new DefaultForbiddenServerManager();
    }
  }

  @Configuration
  public static class MetadataConfiguration {

    @Bean
    public ProvideDataService provideDataService() {
      return new DefaultProvideDataService();
    }

    @Bean
    public NodeOperatingService nodeOperatingService() {
      return new NodeOperatingService();
    }

    @Bean
    public DefaultClientManagerService clientManagerService() {
      return new DefaultClientManagerService();
    }

    @Bean
    public AppRevisionCleaner appRevisionCleaner() {
      return new AppRevisionCleaner();
    }

    @Bean
    @ConditionalOnMissingBean
    public InterfaceAppsIndexCleaner interfaceAppsIndexCleaner() {
      return new InterfaceAppsIndexCleaner();
    }

    @Bean
    public FetchStopPushService fetchStopPushService() {
      return new FetchStopPushService();
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
    public LeaderAwareFilter leaderAwareFilter() {
      return new LeaderAwareFilter();
    }

    @Bean
    public ProvideDataResource provideDataResource() {
      return new ProvideDataResource();
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
    public MetaLeaderResource metaLeaderResource() {
      return new MetaLeaderResource();
    }

    @Bean
    @ConditionalOnMissingBean
    public StopPushDataResource stopPushDataResource() {
      return new StopPushDataResource();
    }

    @Bean
    public ShutdownSwitchResource shutdownSwitchResource() {
      return new ShutdownSwitchResource();
    }

    @Bean
    @ConditionalOnMissingBean
    public VersionResource versionResource() {
      return new VersionResource();
    }

    @Bean
    public CompressResource compressedResource() {
      return new CompressResource();
    }

    @Bean
    @ConditionalOnMissingBean
    public MetaCenterResource cleanerResource() {
      return new MetaCenterResource();
    }

    @Bean
    public BlacklistDataResource blacklistDataResource() {
      return new BlacklistDataResource();
    }

    @Bean
    public ClientManagerResource clientManagerResource() {
      return new ClientManagerResource();
    }

    @Bean
    public CircuitBreakerResources circuitBreakerResources() {
      return new CircuitBreakerResources();
    }

    @Bean
    public RecoverConfigResource recoverConfigResource() {
      return new RecoverConfigResource();
    }

    @Bean
    public SlotSyncResource renewSwitchResource() {
      return new SlotSyncResource();
    }

    @Bean
    public SlotTableResource slotTableResource() {
      return new SlotTableResource();
    }

    @Bean
    public SlotGenericResource slotResource() {
      return new SlotGenericResource();
    }

    @Bean
    public MetricsResource metricsResource() {
      return new MetricsResource();
    }

    @Bean
    public RegistryCoreOpsResource registryCoreOpsResource() {
      return new RegistryCoreOpsResource();
    }
  }

  @Configuration
  public static class ExecutorConfiguation {

    @Bean
    public ThreadPoolExecutor defaultRequestExecutor(MetaServerConfig metaServerConfig) {
      ThreadPoolExecutor defaultRequestExecutor =
          new MetricsableThreadPoolExecutor(
              "MetaHandlerDefaultExecutor",
              metaServerConfig.getDefaultRequestExecutorMinSize(),
              metaServerConfig.getDefaultRequestExecutorMaxSize(),
              300,
              TimeUnit.SECONDS,
              new LinkedBlockingQueue<>(metaServerConfig.getDefaultRequestExecutorQueueSize()),
              new NamedThreadFactory("MetaHandler-DefaultRequest"));
      defaultRequestExecutor.allowCoreThreadTimeOut(true);
      return defaultRequestExecutor;
    }

    @Bean
    public ExecutorManager executorManager(
        MetaServerConfig metaServerConfig,
        MultiClusterMetaServerConfig multiClusterMetaServerConfig) {
      return new ExecutorManager(metaServerConfig, multiClusterMetaServerConfig);
    }
  }
}
