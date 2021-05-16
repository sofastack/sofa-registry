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

import com.alipay.remoting.CustomSerializerManager;
import com.alipay.remoting.serialization.SerializerManager;
import com.alipay.sofa.registry.common.model.client.pb.*;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.metrics.ReporterUtils;
import com.alipay.sofa.registry.metrics.TaskMetrics;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.bolt.serializer.ProtobufCustomSerializer;
import com.alipay.sofa.registry.remoting.bolt.serializer.ProtobufSerializer;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.remoting.exchange.NodeExchanger;
import com.alipay.sofa.registry.server.session.filter.blacklist.BlacklistManager;
import com.alipay.sofa.registry.server.session.metadata.AppRevisionCacheRegistry;
import com.alipay.sofa.registry.server.session.provideData.ProvideDataProcessor;
import com.alipay.sofa.registry.server.session.registry.SessionRegistry;
import com.alipay.sofa.registry.server.session.slot.SlotTableCache;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
import com.alipay.sofa.registry.server.shared.remoting.AbstractServerHandler;
import com.alipay.sofa.registry.task.batcher.TaskDispatchers;
import com.github.rholder.retry.*;
import com.google.common.base.Predicate;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Resource;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * The type Session server bootstrap.
 *
 * @author shangyu.wh
 * @version $Id : SessionServerBootstrap.java, v 0.1 2017-11-14 11:44 synex Exp $
 */
public class SessionServerBootstrap {

  private static final Logger LOGGER = LoggerFactory.getLogger(SessionServerBootstrap.class);

  @Autowired private SessionServerConfig sessionServerConfig;

  @Autowired private Exchange boltExchange;

  @Autowired private Exchange jerseyExchange;

  @Autowired private ExecutorManager executorManager;

  @Autowired private SessionRegistry sessionRegistry;

  @Resource(name = "serverHandlers")
  private Collection<AbstractServerHandler> serverHandlers;

  @Autowired protected MetaServerService metaNodeService;
  @Autowired private NodeExchanger dataNodeExchanger;
  @Autowired private NodeExchanger dataNodeNotifyExchanger;

  @Autowired private ResourceConfig jerseyResourceConfig;

  @Autowired private ApplicationContext applicationContext;

  @Autowired private BlacklistManager blacklistManager;

  @Autowired private ProvideDataProcessor provideDataProcessorManager;

  @Autowired private SlotTableCache slotTableCache;

  private Server server;

  private Server dataSyncServer;

  @Resource(name = "sessionSyncHandlers")
  private Collection<AbstractServerHandler> sessionSyncHandlers;

  @Autowired private AppRevisionCacheRegistry appRevisionCacheRegistry;

  private Server httpServer;

  private final AtomicBoolean metaStart = new AtomicBoolean(false);

  private final AtomicBoolean schedulerStart = new AtomicBoolean(false);

  private final AtomicBoolean httpStart = new AtomicBoolean(false);

  private final AtomicBoolean serverStart = new AtomicBoolean(false);

  private final AtomicBoolean dataStart = new AtomicBoolean(false);

  private final AtomicBoolean serverForSessionSyncStart = new AtomicBoolean(false);

  private final Retryer<Boolean> retryer =
      RetryerBuilder.<Boolean>newBuilder()
          .retryIfRuntimeException()
          .retryIfResult(
              new Predicate<Boolean>() {
                @Override
                public boolean apply(Boolean input) {
                  return !input;
                }
              })
          .withWaitStrategy(WaitStrategies.exponentialWait(1000, 10000, TimeUnit.MILLISECONDS))
          .withStopStrategy(StopStrategies.stopAfterAttempt(10))
          .build();

  /** Do initialized. */
  public void start() {
    try {
      LOGGER.info("release properties: {}", ServerEnv.getReleaseProps());
      LOGGER.info("the configuration items are as follows: " + sessionServerConfig.toString());

      initEnvironment();
      ReporterUtils.enablePrometheusDefaultExports();

      openSessionSyncServer();

      retryer.call(
          () -> {
            connectMetaServer();
            return true;
          });

      // wait until slot table is get
      retryer.call(
          () -> slotTableCache.getCurrentSlotTable().getEpoch() != SlotTable.INIT.getEpoch());

      // load metadata
      retryer.call(
          () -> {
            appRevisionCacheRegistry.loadMetadata();
            return true;
          });

      startScheduler();

      openHttpServer();

      retryer.call(
          () -> {
            connectDataServer();
            return true;
          });

      registerSerializer();

      openSessionServer();

      TaskMetrics.getInstance().registerBolt();

      LOGGER.info("Initialized Session Server...");

      Runtime.getRuntime().addShutdownHook(new Thread(this::doStop));
    } catch (Throwable e) {
      LOGGER.error("Cannot bootstrap session server :", e);
      throw new RuntimeException("Cannot bootstrap session server :", e);
    }
  }

  /** Destroy. */
  public void destroy() {
    doStop();
  }

  private void doStop() {
    try {
      LOGGER.info("{} Shutting down Session Server..", new Date().toString());

      executorManager.stopScheduler();
      TaskDispatchers.stopDefaultSingleTaskDispatcher();
      stopHttpServer();
      stopServer();
      stopDataSyncServer();
    } catch (Throwable e) {
      LOGGER.error("Shutting down Session Server error!", e);
    }
    LOGGER.info("{} Session server is now shutdown...", new Date().toString());
  }

  private void initEnvironment() {
    LOGGER.info(
        "Session server Environment: DataCenter {},Region {},ProcessId {}",
        sessionServerConfig.getSessionServerDataCenter(),
        sessionServerConfig.getSessionServerRegion(),
        ServerEnv.PROCESS_ID);
  }

  private void startScheduler() {

    try {
      if (schedulerStart.compareAndSet(false, true)) {
        executorManager.startScheduler();
        LOGGER.info("Session Scheduler started!");
      }
    } catch (Exception e) {
      schedulerStart.set(false);
      LOGGER.error("Session Scheduler start error!", e);
      throw new RuntimeException("Session Scheduler start error!", e);
    }
  }

  private void openSessionServer() {
    try {
      if (serverStart.compareAndSet(false, true)) {
        server =
            boltExchange.open(
                new URL(
                    NetUtil.getLocalAddress().getHostAddress(),
                    sessionServerConfig.getServerPort()),
                serverHandlers.toArray(new ChannelHandler[serverHandlers.size()]));
        LOGGER.info("Session server started! port:{}", sessionServerConfig.getServerPort());
      }
    } catch (Exception e) {
      serverStart.set(false);
      LOGGER.error("Session server start error! port:{}", sessionServerConfig.getServerPort(), e);
      throw new RuntimeException("Session server start error!", e);
    }
  }

  private void openSessionSyncServer() {
    try {
      if (serverForSessionSyncStart.compareAndSet(false, true)) {
        dataSyncServer =
            boltExchange.open(
                new URL(
                    NetUtil.getLocalAddress().getHostAddress(),
                    sessionServerConfig.getSyncSessionPort()),
                sessionServerConfig.getSyncSessionIOLowWaterMark(),
                sessionServerConfig.getSyncSessionIOHighWaterMark(),
                sessionSyncHandlers.toArray(new ChannelHandler[sessionSyncHandlers.size()]));
        LOGGER.info(
            "Data server for sync started! port:{}", sessionServerConfig.getSyncSessionPort());
      }
    } catch (Exception e) {
      serverForSessionSyncStart.set(false);
      LOGGER.error(
          "Data sync server start error! port:{}", sessionServerConfig.getSyncSessionPort(), e);
      throw new RuntimeException("Data sync server start error!", e);
    }
  }

  private void connectDataServer() {
    try {
      dataNodeNotifyExchanger.connectServer();
      dataNodeExchanger.connectServer();
      dataStart.set(true);
    } catch (Exception e) {
      dataStart.set(false);
      LOGGER.error(
          "Data server connected server error! port:{}",
          sessionServerConfig.getDataServerPort(),
          e);
      throw new RuntimeException("Data server connected server error!", e);
    }
  }

  private void connectMetaServer() {
    try {
      // register node as renew node
      metaNodeService.renewNode();
      // start sched renew
      metaNodeService.startRenewer();
      fetchStopPushSwitch();

      fetchBlackList();
      metaStart.set(true);

      LOGGER.info(
          "MetaServer connected meta server! Port:{}", sessionServerConfig.getMetaServerPort());
    } catch (Exception e) {
      metaStart.set(false);
      LOGGER.error(
          "MetaServer connected server error! Port:{}", sessionServerConfig.getMetaServerPort(), e);
      throw new RuntimeException("MetaServer connected server error!", e);
    }
  }

  private void fetchStopPushSwitch() {
    ProvideData data = metaNodeService.fetchData(ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID);
    if (data != null && data.getProvideData() != null) {
      provideDataProcessorManager.fetchDataProcess(data);
    } else {
      LOGGER.info("Fetch session stop push switch data null,config not change!");
    }
    // start fetch change data after got the switch
    sessionRegistry.fetchChangDataProcess();
  }

  private void fetchBlackList() {
    blacklistManager.load();
  }

  private void openHttpServer() {
    try {
      if (httpStart.compareAndSet(false, true)) {
        bindResourceConfig();
        httpServer =
            jerseyExchange.open(
                new URL(
                    NetUtil.getLocalAddress().getHostAddress(),
                    sessionServerConfig.getHttpServerPort()),
                new ResourceConfig[] {jerseyResourceConfig});
        LOGGER.info("Open http server port {} success!", sessionServerConfig.getHttpServerPort());
      }
    } catch (Exception e) {
      LOGGER.error("Open http server port {} error!", sessionServerConfig.getHttpServerPort(), e);
      httpStart.set(false);
      throw new RuntimeException("Open http server error!", e);
    }
  }

  private void bindResourceConfig() {
    registerInstances(Path.class);
    registerInstances(Provider.class);
  }

  private void registerInstances(Class<? extends Annotation> annotationType) {
    Map<String, Object> beans = applicationContext.getBeansWithAnnotation(annotationType);
    if (beans != null && !beans.isEmpty()) {
      beans.forEach(
          (beanName, bean) -> {
            jerseyResourceConfig.registerInstances(bean);
            jerseyResourceConfig.register(bean.getClass());
          });
    }
  }

  private void registerSerializer() {
    ProtobufCustomSerializer serializer = new ProtobufCustomSerializer();
    CustomSerializerManager.registerCustomSerializer(
        PublisherRegisterPb.class.getName(), serializer);
    CustomSerializerManager.registerCustomSerializer(
        SubscriberRegisterPb.class.getName(), serializer);
    CustomSerializerManager.registerCustomSerializer(
        SyncConfigRequestPb.class.getName(), serializer);
    CustomSerializerManager.registerCustomSerializer(
        SyncConfigResponsePb.class.getName(), serializer);
    CustomSerializerManager.registerCustomSerializer(
        RegisterResponsePb.class.getName(), serializer);
    CustomSerializerManager.registerCustomSerializer(ResultPb.class.getName(), serializer);
    CustomSerializerManager.registerCustomSerializer(ReceivedDataPb.class.getName(), serializer);
    CustomSerializerManager.registerCustomSerializer(
        ReceivedConfigDataPb.class.getName(), serializer);

    SerializerManager.addSerializer(
        ProtobufSerializer.PROTOCOL_PROTOBUF, ProtobufSerializer.getInstance());
  }

  private void stopServer() {
    if (server != null && server.isOpen()) {
      server.close();
    }
  }

  private void stopDataSyncServer() {
    if (dataSyncServer != null && dataSyncServer.isOpen()) {
      dataSyncServer.close();
    }
  }

  private void stopHttpServer() {
    if (httpServer != null && httpServer.isOpen()) {
      httpServer.close();
    }
  }

  /**
   * Getter method for property <tt>metaStart</tt>.
   *
   * @return property value of metaStart
   */
  public boolean getMetaStart() {
    return metaStart.get();
  }

  /**
   * Getter method for property <tt>schedulerStart</tt>.
   *
   * @return property value of schedulerStart
   */
  public boolean getSchedulerStart() {
    return schedulerStart.get();
  }

  /**
   * Getter method for property <tt>httpStart</tt>.
   *
   * @return property value of httpStart
   */
  public boolean getHttpStart() {
    return httpStart.get();
  }

  /**
   * Getter method for property <tt>serverStart</tt>.
   *
   * @return property value of serverStart
   */
  public boolean getServerStart() {
    return serverStart.get();
  }

  /**
   * Getter method for property <tt>dataStart</tt>.
   *
   * @return property value of dataStart
   */
  public boolean getDataStart() {
    return dataStart.get();
  }

  public boolean getServerForSessionSyncStart() {
    return serverForSessionSyncStart.get();
  }
}
