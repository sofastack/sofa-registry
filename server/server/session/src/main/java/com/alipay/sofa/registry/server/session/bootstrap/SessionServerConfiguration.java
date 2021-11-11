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

import com.alipay.sofa.registry.jdbc.config.JdbcConfiguration;
import com.alipay.sofa.registry.jraft.config.RaftConfiguration;
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
import com.alipay.sofa.registry.server.session.circuit.breaker.CircuitBreakerService;
import com.alipay.sofa.registry.server.session.circuit.breaker.DefaultCircuitBreakerService;
import com.alipay.sofa.registry.server.session.connections.ConnectionsService;
import com.alipay.sofa.registry.server.session.filter.IPMatchStrategy;
import com.alipay.sofa.registry.server.session.filter.ProcessFilter;
import com.alipay.sofa.registry.server.session.filter.blacklist.BlacklistMatchProcessFilter;
import com.alipay.sofa.registry.server.session.filter.blacklist.DefaultIPMatchStrategy;
import com.alipay.sofa.registry.server.session.limit.AccessLimitService;
import com.alipay.sofa.registry.server.session.limit.AccessLimitServiceImpl;
import com.alipay.sofa.registry.server.session.mapper.ConnectionMapper;
import com.alipay.sofa.registry.server.session.metadata.AppRevisionCacheRegistry;
import com.alipay.sofa.registry.server.session.metadata.AppRevisionHeartbeatRegistry;
import com.alipay.sofa.registry.server.session.node.service.*;
import com.alipay.sofa.registry.server.session.providedata.*;
import com.alipay.sofa.registry.server.session.push.*;
import com.alipay.sofa.registry.server.session.registry.Registry;
import com.alipay.sofa.registry.server.session.registry.SessionRegistry;
import com.alipay.sofa.registry.server.session.remoting.ClientNodeExchanger;
import com.alipay.sofa.registry.server.session.remoting.DataNodeExchanger;
import com.alipay.sofa.registry.server.session.remoting.DataNodeNotifyExchanger;
import com.alipay.sofa.registry.server.session.remoting.console.SessionConsoleExchanger;
import com.alipay.sofa.registry.server.session.remoting.console.handler.*;
import com.alipay.sofa.registry.server.session.remoting.handler.*;
import com.alipay.sofa.registry.server.session.resource.*;
import com.alipay.sofa.registry.server.session.scheduler.timertask.CacheCountTask;
import com.alipay.sofa.registry.server.session.scheduler.timertask.SessionCacheDigestTask;
import com.alipay.sofa.registry.server.session.scheduler.timertask.SyncClientsHeartbeatTask;
import com.alipay.sofa.registry.server.session.slot.SlotTableCache;
import com.alipay.sofa.registry.server.session.slot.SlotTableCacheImpl;
import com.alipay.sofa.registry.server.session.store.*;
import com.alipay.sofa.registry.server.session.strategy.*;
import com.alipay.sofa.registry.server.session.strategy.impl.*;
import com.alipay.sofa.registry.server.session.wrapper.*;
import com.alipay.sofa.registry.server.shared.meta.MetaServerManager;
import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
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
 * @author shangyu.wh
 * @version $Id: SessionServerConfiguration.java, v 0.1 2017-11-14 11:39 synex Exp $
 */
@Configuration
@Import({
  SessionServerInitializer.class,
  StoreApiConfiguration.class,
  JdbcConfiguration.class,
  RaftConfiguration.class
})
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

    @Bean
    public DiskSlotTableRecorder diskSlotTableRecorder() {
      return new DiskSlotTableRecorder();
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
    public NodeExchanger sessionConsoleExchanger() {
      return new SessionConsoleExchanger();
    }

    @Bean
    public DataNodeExchanger dataNodeExchanger() {
      return new DataNodeExchanger();
    }

    @Bean
    public DataNodeNotifyExchanger dataNodeNotifyExchanger() {
      return new DataNodeNotifyExchanger();
    }

    @Bean
    public MetaServerManager metaServerManager() {
      return new SessionMetaServerManager();
    }

    @Bean
    public SlotTableCache slotTableCache() {
      return new SlotTableCacheImpl();
    }

    @Bean(name = "serverHandlers")
    public Collection<AbstractServerHandler> serverHandlers() {
      Collection<AbstractServerHandler> list = new ArrayList<>();
      list.add(publisherHandler());
      list.add(subscriberHandler());
      list.add(watcherHandler());
      list.add(clientNodeConnectionHandler());
      list.add(syncConfigHandler());
      list.add(publisherPbHandler());
      list.add(subscriberPbHandler());
      list.add(syncConfigPbHandler());
      list.add(metadataRegisterPbHandler());
      list.add(serviceAppMappingHandler());
      list.add(metaRevisionHeartbeatHandler());
      list.add(getRevisionHandler());

      return list;
    }

    @Bean(name = "sessionSyncHandlers")
    public Collection<AbstractServerHandler> serverSyncHandlers() {
      Collection<AbstractServerHandler> list = new ArrayList<>();
      list.add(dataSlotDiffDigestRequestHandler());
      list.add(dataSlotDiffPublisherRequestHandler());
      return list;
    }

    @Bean(name = "consoleHandlers")
    public Collection<AbstractServerHandler> consoleHandlers() {
      Collection<AbstractServerHandler> list = new ArrayList<>();
      list.add(querySubscriberRequestHandler());
      list.add(clientOffRequestHandler());
      list.add(clientOnRequestHandler());
      list.add(getClientManagerRequestHandler());
      list.add(pubSubDataInfoIdRequestHandler());
      list.add(filterSubscriberIPsHandler());
      list.add(stopPushRequestHandler());
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
    public AbstractServerHandler querySubscriberRequestHandler() {
      return new QuerySubscriberRequestHandler();
    }

    @Bean
    public AbstractServerHandler filterSubscriberIPsHandler() {
      return new FilterSubscriberIPsHandler();
    }

    @Bean
    public AbstractServerHandler clientOffRequestHandler() {
      return new ClientOffRequestHandler();
    }

    @Bean
    public AbstractServerHandler clientOnRequestHandler() {
      return new ClientOnRequestHandler();
    }

    @Bean
    public AbstractServerHandler getClientManagerRequestHandler() {
      return new GetClientManagerRequestHandler();
    }

    @Bean
    public AbstractServerHandler stopPushRequestHandler() {
      return new StopPushRequestHandler();
    }

    @Bean
    public AbstractServerHandler pubSubDataInfoIdRequestHandler() {
      return new PubSubDataInfoIdRequestHandler();
    }

    @Bean
    public AbstractServerHandler dataSlotDiffDigestRequestHandler() {
      return new DataSlotDiffDigestRequestHandler();
    }

    @Bean
    public AbstractServerHandler dataSlotDiffPublisherRequestHandler() {
      return new DataSlotDiffPublisherRequestHandler();
    }

    @Bean
    public AbstractServerHandler publisherPbHandler() {
      return new PublisherPbHandler();
    }

    @Bean
    public AbstractServerHandler metadataRegisterPbHandler() {
      return new MetadataRegisterPbHandler();
    }

    @Bean
    public AbstractServerHandler serviceAppMappingHandler() {
      return new ServiceAppMappingPbHandler();
    }

    @Bean
    public AbstractServerHandler getRevisionHandler() {
      return new GetRevisionPbHandler();
    }

    @Bean
    public AbstractServerHandler metaRevisionHeartbeatHandler() {
      return new MetaRevisionHeartbeatPbHandler();
    }

    @Bean
    public AbstractServerHandler subscriberPbHandler() {
      return new SubscriberPbHandler();
    }

    @Bean
    public AbstractServerHandler syncConfigPbHandler() {
      return new SyncConfigPbHandler();
    }

    @Bean
    @ConditionalOnMissingBean(name = "circuitBreakerService")
    public CircuitBreakerService circuitBreakerService() {
      return new DefaultCircuitBreakerService();
    }

    @Bean(name = "dataNotifyClientHandlers")
    public Collection<AbstractClientHandler> dataClientHandlers() {
      Collection<AbstractClientHandler> list = new ArrayList<>();
      list.add(dataChangeRequestHandler());
      list.add(dataPushRequestHandler());
      return list;
    }

    @Bean(name = "metaClientHandlers")
    public Collection<AbstractClientHandler> metaClientHandlers() {
      Collection<AbstractClientHandler> list = new ArrayList<>();
      list.add(notifyProvideDataChangeHandler());
      list.add(slotTableChangeEventHandler());
      list.add(appRevisionSliceHandler());
      return list;
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
    public AbstractClientHandler notifyProvideDataChangeHandler() {
      return new NotifyProvideDataChangeHandler();
    }

    @Bean
    public AbstractClientHandler appRevisionSliceHandler() {
      return new AppRevisionSliceHandler();
    }

    @Bean
    public SlotTableChangeEventHandler slotTableChangeEventHandler() {
      return new SlotTableChangeEventHandler();
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
    public CompressResource compressResource() {
      return new CompressResource();
    }

    @Bean
    public ClientsOpenResource clientsOpenResource() {
      return new ClientsOpenResource();
    }

    @Bean
    public ConnectionsResource connectionsResource() {
      return new ConnectionsResource();
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
    @ConditionalOnMissingBean
    public VersionResource versionResource() {
      return new VersionResource();
    }

    @Bean
    public RegistryOpsResource opsResource() {
      return new RegistryOpsResource();
    }

    @Bean
    public ClientManagerResource clientManagerResource() {
      return new ClientManagerResource();
    }

    @Bean
    public SlotTableStatusResource slotTableStatusResource() {
      return new SlotTableStatusResource();
    }

    @Bean
    public EmergencyApiResource emergencyApiResource() {
      return new EmergencyApiResource();
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
    @ConditionalOnMissingBean
    public MetaServerService metaServerService() {
      return new MetaServerServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public ClientNodeService clientNodeService() {
      return new ClientNodeServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public FirePushService firePushService() {
      return new FirePushService();
    }

    @Bean
    @ConditionalOnMissingBean
    public PushProcessor pushProcessor() {
      return new PushProcessor();
    }

    @Bean
    @ConditionalOnMissingBean
    public WatchProcessor watchProcessor() {
      return new WatchProcessor();
    }

    @Bean
    @ConditionalOnMissingBean
    public ChangeProcessor changeProcessor() {
      return new ChangeProcessor();
    }

    @Bean
    @ConditionalOnMissingBean
    public PushDataGenerator pushDataGenerator() {
      return new PushDataGenerator();
    }

    @Bean
    public PushSwitchService pushSwitchService() {
      return new PushSwitchService();
    }

    @Bean
    @ConditionalOnMissingBean
    public FetchPubSubDataInfoIdService fetchPubSubDataInfoIdService() {
      return new FetchPubSubDataInfoIdService();
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

    @Bean
    public AppRevisionCacheRegistry appRevisionCacheRegistry() {
      return new AppRevisionCacheRegistry();
    }

    @Bean
    public AppRevisionHeartbeatRegistry appRevisionHeartbeatRegistry() {
      return new AppRevisionHeartbeatRegistry();
    }
  }

  @Configuration
  public static class ExecutorConfiguration {

    @Bean(name = "metaNodeExecutor")
    public ThreadPoolExecutor metaNodeExecutor(SessionServerConfig sessionServerConfig) {
      return new MetricsableThreadPoolExecutor(
          "metaNodeInSessionExecutor",
          sessionServerConfig.getMetaNodeWorkerSize(),
          sessionServerConfig.getMetaNodeWorkerSize(),
          300,
          TimeUnit.SECONDS,
          new ArrayBlockingQueue<>(sessionServerConfig.getMetaNodeBufferSize()),
          new NamedThreadFactory("metaNodeInSessionExecutor", true));
    }

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

    @Bean
    public SessionCacheDigestTask sessionCacheDigestTask() {
      return new SessionCacheDigestTask();
    }

    @Bean
    public CacheCountTask cacheCountTask() {
      return new CacheCountTask();
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
    public SyncConfigHandlerStrategy syncConfigHandlerStrategy() {
      return new DefaultSyncConfigHandlerStrategy();
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
    public AppRevisionHandlerStrategy appRevisionHandlerStrategy() {
      return new DefaultAppRevisionHandlerStrategy();
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
    @ConditionalOnMissingBean
    public ProcessFilter blacklistMatchProcessFilter() {
      return new BlacklistMatchProcessFilter();
    }

    @Bean
    public WrapperInterceptorManager wrapperInterceptorManager() {
      WrapperInterceptorManager mgr = new WrapperInterceptorManager();
      mgr.addInterceptor(clientCheckWrapperInterceptor());
      mgr.addInterceptor(blacklistWrapperInterceptor());
      mgr.addInterceptor(accessLimitWrapperInterceptor());
      mgr.addInterceptor(clientOffWrapperInterceptor());
      return mgr;
    }

    @Bean
    public WrapperInterceptor clientCheckWrapperInterceptor() {
      return new ClientCheckWrapperInterceptor();
    }

    @Bean
    public WrapperInterceptor blacklistWrapperInterceptor() {
      return new BlacklistWrapperInterceptor();
    }

    @Bean
    public WrapperInterceptor clientOffWrapperInterceptor() {
      return new ClientOffWrapperInterceptor();
    }

    @Bean
    public WrapperInterceptor accessLimitWrapperInterceptor() {
      return new AccessLimitWrapperInterceptor();
    }
  }

  @Configuration
  public static class SessionRenewDatumConfiguration {

    @Bean
    public WriteDataAcceptor writeDataAcceptor() {
      return new WriteDataAcceptorImpl();
    }
  }

  @Configuration
  public static class SessionConnectionsConfiguration {
    @Bean
    public ConnectionsService connectionsService() {
      return new ConnectionsService();
    }

    @Bean
    public ConnectionMapper connectionMapper() {
      return new ConnectionMapper();
    }
  }

  @Configuration
  public static class SessionProvideDataConfiguration {

    @Bean
    public SystemPropertyProcessorManager systemPropertyProcessorManager() {
      return new SystemPropertyProcessorManager();
    }

    @Bean
    public ProvideDataProcessor provideDataProcessorManager() {
      return new ProvideDataProcessorManager();
    }

    @Bean
    public FetchSystemPropertyService fetchBlackListService(
        SystemPropertyProcessorManager systemPropertyProcessorManager) {
      FetchBlackListService fetchBlackListService = new FetchBlackListService();
      systemPropertyProcessorManager.addSystemDataProcessor(fetchBlackListService);
      return fetchBlackListService;
    }

    @Bean
    public FetchSystemPropertyService fetchStopPushService(
        SystemPropertyProcessorManager systemPropertyProcessorManager) {
      FetchStopPushService fetchStopPushService = new FetchStopPushService();
      systemPropertyProcessorManager.addSystemDataPersistenceProcessor(fetchStopPushService);

      return fetchStopPushService;
    }

    @Bean
    public FetchSystemPropertyService fetchClientOffAddressService(
        SystemPropertyProcessorManager systemPropertyProcessorManager) {
      FetchClientOffAddressService fetchClientOffAddressService =
          new FetchClientOffAddressService();
      systemPropertyProcessorManager.addSystemDataPersistenceProcessor(
          fetchClientOffAddressService);

      return fetchClientOffAddressService;
    }

    @Bean
    public FetchSystemPropertyService fetchGrayPushSwitchService(
        SystemPropertyProcessorManager systemPropertyProcessorManager) {
      FetchGrayPushSwitchService fetchGrayPushSwitchService = new FetchGrayPushSwitchService();
      systemPropertyProcessorManager.addSystemDataProcessor(fetchGrayPushSwitchService);

      return fetchGrayPushSwitchService;
    }

    @Bean
    public FetchSystemPropertyService compressPushService(
        SystemPropertyProcessorManager systemPropertyProcessorManager) {
      CompressPushService compressPushService = new CompressPushService();
      systemPropertyProcessorManager.addSystemDataProcessor(compressPushService);
      return compressPushService;
    }

    @Bean
    public FetchSystemPropertyService fetchShutdownService(
        SystemPropertyProcessorManager systemPropertyProcessorManager) {
      FetchShutdownService fetchShutdownService = new FetchShutdownService();
      systemPropertyProcessorManager.addSystemDataPersistenceProcessor(fetchShutdownService);

      return fetchShutdownService;
    }

    @Bean
    public ConfigProvideDataWatcher configProvideDataWatcher() {
      return new ConfigProvideDataWatcher();
    }
  }
}
