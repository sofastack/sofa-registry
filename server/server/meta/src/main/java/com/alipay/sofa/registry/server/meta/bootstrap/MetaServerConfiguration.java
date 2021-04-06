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
import com.alipay.sofa.registry.jraft.config.RaftConfiguration;
import com.alipay.sofa.registry.remoting.bolt.exchange.BoltExchange;
import com.alipay.sofa.registry.remoting.jersey.exchange.JerseyExchange;
import com.alipay.sofa.registry.server.meta.MetaLeaderService.MetaLeaderElectorListener;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfigBean;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfigBeanProperty;
import com.alipay.sofa.registry.server.meta.provide.data.DefaultProvideDataService;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataService;
import com.alipay.sofa.registry.server.meta.remoting.DataNodeExchanger;
import com.alipay.sofa.registry.server.meta.remoting.MetaServerExchanger;
import com.alipay.sofa.registry.server.meta.remoting.SessionNodeExchanger;
import com.alipay.sofa.registry.server.meta.remoting.connection.DataConnectionHandler;
import com.alipay.sofa.registry.server.meta.remoting.connection.MetaConnectionHandler;
import com.alipay.sofa.registry.server.meta.remoting.connection.SessionConnectionHandler;
import com.alipay.sofa.registry.server.meta.remoting.handler.FetchProvideDataRequestHandler;
import com.alipay.sofa.registry.server.meta.remoting.handler.HeartbeatRequestHandler;
import com.alipay.sofa.registry.server.meta.remoting.handler.RegistryBlacklistHandler;
import com.alipay.sofa.registry.server.meta.remoting.meta.MetaNodeExchange;
import com.alipay.sofa.registry.server.meta.remoting.meta.MetaServerRenewService;
import com.alipay.sofa.registry.server.meta.resource.*;
import com.alipay.sofa.registry.server.meta.resource.filter.LeaderAwareFilter;
import com.alipay.sofa.registry.server.shared.remoting.AbstractServerHandler;
import com.alipay.sofa.registry.server.shared.resource.MetricsResource;
import com.alipay.sofa.registry.server.shared.resource.SlotGenericResource;
import com.alipay.sofa.registry.store.api.driver.RepositoryConfig;
import com.alipay.sofa.registry.store.api.spring.SpringContext;
import com.alipay.sofa.registry.task.MetricsableThreadPoolExecutor;
import com.alipay.sofa.registry.util.DefaultExecutorFactory;
import com.alipay.sofa.registry.util.NamedThreadFactory;
import com.alipay.sofa.registry.util.OsUtils;
import com.alipay.sofa.registry.util.PropertySplitter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.*;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

/**
 * @author shangyu.wh
 * @version $Id: MetaServerConfiguration.java, v 0.1 2018-01-12 14:53 shangyu.wh Exp $
 */
@Configuration
@Import({
  MetaServerInitializerConfiguration.class,
  JdbcConfiguration.class,
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
  public static class ThreadPoolResourceConfiguration {
    @Bean(name = GLOBAL_EXECUTOR)
    public ExecutorService getGlobalExecutorService() {
      int corePoolSize = Math.min(OsUtils.getCpuCount() * 2, 8);
      int maxPoolSize = 50 * OsUtils.getCpuCount();
      DefaultExecutorFactory executorFactory =
          new DefaultExecutorFactory(
              GLOBAL_EXECUTOR, corePoolSize, maxPoolSize, new ThreadPoolExecutor.AbortPolicy());
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
    public BoltExchange boltExchange() {
      return new BoltExchange();
    }

    @Bean
    public JerseyExchange jerseyExchange() {
      return new JerseyExchange();
    }

    @Bean(name = "sessionServerHandlers")
    public Collection<AbstractServerHandler> sessionServerHandlers() {
      Collection<AbstractServerHandler> list = new ArrayList<>();
      list.add(sessionConnectionHandler());
      list.add(heartbeatRequestHandler());
      list.add(fetchProvideDataRequestHandler());
      list.add(registryBlacklistHandler());
      return list;
    }

    @Bean(name = "dataServerHandlers")
    public Collection<AbstractServerHandler> dataServerHandlers() {
      Collection<AbstractServerHandler> list = new ArrayList<>();
      list.add(dataConnectionHandler());
      list.add(heartbeatRequestHandler());
      list.add(fetchProvideDataRequestHandler());
      list.add(registryBlacklistHandler());
      return list;
    }

    @Bean(name = "metaServerHandlers")
    public Collection<AbstractServerHandler> metaServerHandlers() {
      Collection<AbstractServerHandler> list = new ArrayList<>();
      list.add(metaConnectionHandler());
      list.add(heartbeatRequestHandler());
      return list;
    }

    @Bean(name = "metaLeaderListeners")
    public Collection<MetaLeaderElectorListener> metaLeaderListeners() {
      Collection<MetaLeaderElectorListener> list = new ArrayList<>();
      list.add(provideDataService());
      return list;
    }

    @Bean
    public SessionConnectionHandler sessionConnectionHandler() {
      return new SessionConnectionHandler();
    }

    @Bean
    public DataConnectionHandler dataConnectionHandler() {
      return new DataConnectionHandler();
    }

    @Bean
    public MetaConnectionHandler metaConnectionHandler() {
      return new MetaConnectionHandler();
    }

    @Bean
    public HeartbeatRequestHandler heartbeatRequestHandler() {
      return new HeartbeatRequestHandler();
    }

    @Bean
    public FetchProvideDataRequestHandler fetchProvideDataRequestHandler() {
      return new FetchProvideDataRequestHandler();
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
    public RegistryBlacklistHandler registryBlacklistHandler() {
      return new RegistryBlacklistHandler();
    }

    @Bean
    public MetaNodeExchange metaNodeExchange() {
      return new MetaNodeExchange();
    }

    @Bean
    public MetaServerRenewService metaServerRenewService() {
      return new MetaServerRenewService();
    }

    @Bean
    public ProvideDataService provideDataService() {
      return new DefaultProvideDataService();
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
    public BlacklistDataResource blacklistDataResource() {
      return new BlacklistDataResource();
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
  }

  @Configuration
  public static class MetaPersistenceConfiguration {

    @Bean
    public RepositoryConfig repositoryConfig() {
      return new RepositoryConfig();
    }

    @Bean
    @Profile(SpringContext.META_STORE_API_JDBC)
    public JdbcConfiguration jdbcConfiguration() {
      return new JdbcConfiguration();
    }

    @Bean
    @Profile(SpringContext.META_STORE_API_RAFT)
    public RaftConfiguration raftConfiguration() {
      return new RaftConfiguration();
    }
  }
}
