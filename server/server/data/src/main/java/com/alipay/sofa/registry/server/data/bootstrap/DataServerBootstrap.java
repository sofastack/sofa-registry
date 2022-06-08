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

import com.alipay.sofa.registry.common.model.ProcessId;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.common.model.slot.SlotTableStatusResponse;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.metrics.ReporterUtils;
import com.alipay.sofa.registry.metrics.TaskMetrics;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.remoting.jersey.JettyServer;
import com.alipay.sofa.registry.server.data.change.DataChangeEventCenter;
import com.alipay.sofa.registry.server.data.lease.SessionLeaseManager;
import com.alipay.sofa.registry.server.data.providedata.FetchStopPushService;
import com.alipay.sofa.registry.server.data.slot.SlotManager;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
import com.alipay.sofa.registry.server.shared.providedata.SystemPropertyProcessorManager;
import com.alipay.sofa.registry.server.shared.remoting.AbstractServerHandler;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.annotations.VisibleForTesting;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Resource;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

import com.google.common.collect.Lists;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;

/**
 * @author qian.lqlq
 * @version $Id: DataServerBootstrap.java, v 0.1 2017-12-06 20:50 qian.lqlq Exp $
 */
@EnableConfigurationProperties
public class DataServerBootstrap {
  private static final Logger LOGGER = LoggerFactory.getLogger(DataServerBootstrap.class);

  @Autowired private DataServerConfig dataServerConfig;

  @Autowired private MetaServerService metaServerService;

  @Autowired private ApplicationContext applicationContext;

  @Autowired private ResourceConfig dataJerseyConfig;

  @Autowired private Exchange jettyExchange;

  @Autowired private Exchange boltExchange;

  @Autowired private DataChangeEventCenter dataChangeEventCenter;

  @Autowired private SessionLeaseManager sessionLeaseManager;

  @Resource(name = "serverHandlers")
  private Collection<AbstractServerHandler> serverHandlers;

  @Resource(name = "serverSyncHandlers")
  private Collection<AbstractServerHandler> serverSyncHandlers;

  @Autowired private SystemPropertyProcessorManager systemPropertyProcessorManager;

  @Autowired private SlotManager slotManager;

  @Resource private FetchStopPushService fetchStopPushService;

  private Server server;

  private Server notifyServer;

  private Server dataSyncServer;

  private Server httpServer;

  private final AtomicBoolean httpServerStarted = new AtomicBoolean(false);

  private final AtomicBoolean schedulerStarted = new AtomicBoolean(false);

  private final AtomicBoolean serverForSessionStarted = new AtomicBoolean(false);

  private final AtomicBoolean serverForDataSyncStarted = new AtomicBoolean(false);

  private final Retryer<Boolean> startupRetryer =
      RetryerBuilder.<Boolean>newBuilder()
          .retryIfRuntimeException()
          .retryIfResult(input -> !input)
          .withWaitStrategy(WaitStrategies.exponentialWait(1000, 3000, TimeUnit.MILLISECONDS))
          .withStopStrategy(StopStrategies.stopAfterAttempt(10))
          .build();

  private final Retryer<Boolean> addBlacklistRetryer =
      RetryerBuilder.<Boolean>newBuilder()
          .retryIfException()
          .retryIfRuntimeException()
          .withWaitStrategy(WaitStrategies.exponentialWait(1000, 3000, TimeUnit.MILLISECONDS))
          .withStopStrategy(StopStrategies.stopAfterAttempt(3))
          .build();

  /** start dataserver */
  public void start() {
    try {
      LOGGER.info("begin start server");
      LOGGER.info("release properties: {}", ServerEnv.getReleaseProps());
      LOGGER.info("the configuration items are as follows: " + dataServerConfig.toString());

      ReporterUtils.enablePrometheusDefaultExports();

      openDataServer();

      openDataSyncServer();

      openHttpServer();

      renewNode();
      fetchProviderData();

      systemPropertyProcessorManager.startFetchMetaSystemProperty();

      startScheduler();

      TaskMetrics.getInstance().registerBolt();

      postStart();
      Runtime.getRuntime().addShutdownHook(new Thread(this::doStop));

      LOGGER.info("start server success");
    } catch (Throwable e) {
      throw new RuntimeException("start server error", e);
    }
  }

  private void postStart() throws Throwable {
    startupRetryer.call(
        () -> {
          LOGGER.info("successful start data server, remove self from blacklist");
          metaServerService.removeSelfFromMetaBlacklist();
          return true;
        });
  }

  private void openDataServer() {
    try {
      if (serverForSessionStarted.compareAndSet(false, true)) {
        // open notify port first
        notifyServer =
            boltExchange.open(
                new URL(
                    NetUtil.getLocalAddress().getHostAddress(), dataServerConfig.getNotifyPort()),
                dataServerConfig.getLowWaterMark(),
                dataServerConfig.getHighWaterMark(),
                new ChannelHandler[0]);
        server =
            boltExchange.open(
                new URL(NetUtil.getLocalAddress().getHostAddress(), dataServerConfig.getPort()),
                dataServerConfig.getLowWaterMark(),
                dataServerConfig.getHighWaterMark(),
                serverHandlers.toArray(new ChannelHandler[serverHandlers.size()]));
        dataChangeEventCenter.init();
        LOGGER.info(
            "Data server for session started! port:{}, notifyPort:{}",
            dataServerConfig.getPort(),
            dataServerConfig.getNotifyPort());
      }
    } catch (Exception e) {
      serverForSessionStarted.set(false);
      LOGGER.error("Data server start error! port:{}", dataServerConfig.getPort(), e);
      throw new RuntimeException("Data server start error!", e);
    }
  }

  private void openDataSyncServer() {
    try {
      if (serverForDataSyncStarted.compareAndSet(false, true)) {
        dataSyncServer =
            boltExchange.open(
                new URL(
                    NetUtil.getLocalAddress().getHostAddress(), dataServerConfig.getSyncDataPort()),
                dataServerConfig.getLowWaterMark(),
                dataServerConfig.getHighWaterMark(),
                serverSyncHandlers.toArray(new ChannelHandler[serverSyncHandlers.size()]));
        LOGGER.info("Data server for sync started! port:{}", dataServerConfig.getSyncDataPort());
      }
    } catch (Exception e) {
      serverForDataSyncStarted.set(false);
      LOGGER.error("Data sync server start error! port:{}", dataServerConfig.getSyncDataPort(), e);
      throw new RuntimeException("Data sync server start error!", e);
    }
  }
  private Handler createHandler(){
    HandlerList handlerList = new HandlerList();
    List<Handler> hs = Lists.newArrayList();
    if(dataServerConfig.isSwaggerEnabled()){
      hs.add(JettyServer.createSwaggerHandler());
    }
    hs.add(JettyServer.createHandler(dataJerseyConfig));
    handlerList.setHandlers(hs.toArray(new Handler[]{}));
    return handlerList;
  }


  private void openHttpServer() {
    try {
      if (httpServerStarted.compareAndSet(false, true)) {
        bindResourceConfig();
        httpServer =
            jettyExchange.open(
                new URL(
                    NetUtil.getLocalAddress().getHostAddress(),
                    dataServerConfig.getHttpServerPort()),
                    new Handler[]{createHandler()});
        LOGGER.info("Open http server port {} success!", dataServerConfig.getHttpServerPort());
      }
    } catch (Exception e) {
      httpServerStarted.set(false);
      LOGGER.error("Open http server port {} error!", dataServerConfig.getHttpServerPort(), e);
      throw new RuntimeException("Open http server  error!", e);
    }
  }

  private void renewNode() {
    metaServerService.renewNode();
    // init session lease with first renew
    for (ProcessId processId : metaServerService.getSessionProcessIds()) {
      sessionLeaseManager.renewSession(processId);
    }
    metaServerService.startRenewer();
  }

  private void fetchProviderData() {
    ProvideData provideData = metaServerService.fetchData(ValueConstants.DATA_SESSION_LEASE_SEC);
    Integer expireSec = ProvideData.toInteger(provideData);
    if (expireSec != null) {
      dataServerConfig.setSessionLeaseSecs(expireSec);
      LOGGER.info(
          "Fetch {}={}, update current config", ValueConstants.DATA_SESSION_LEASE_SEC, expireSec);
    }

    provideData = metaServerService.fetchData(ValueConstants.DATA_DATUM_SYNC_SESSION_INTERVAL_SEC);
    Integer syncSessionIntervalSec = ProvideData.toInteger(provideData);
    if (syncSessionIntervalSec != null) {
      dataServerConfig.setSlotLeaderSyncSessionIntervalSecs(syncSessionIntervalSec);
      LOGGER.info(
          "Fetch {}={}, update current config",
          ValueConstants.DATA_DATUM_SYNC_SESSION_INTERVAL_SEC,
          syncSessionIntervalSec);
    }
  }

  private void startScheduler() {
    try {
      schedulerStarted.compareAndSet(false, true);
    } catch (Exception e) {
      schedulerStarted.set(false);
      LOGGER.error("Data Scheduler start error!", e);
      throw new RuntimeException("Data Scheduler start error!", e);
    }
  }

  public void destroy() {
    doStop();
  }

  private void doStop() {
    try {
      LOGGER.info("{} Shutting down Data Server..", new Date().toString());

      gracefulShutdown();

      stopHttpServer();
      stopServer();
      stopDataSyncServer();
      stopNotifyServer();
    } catch (Throwable e) {
      LOGGER.error("Shutting down Data Server error!", e);
    }
    LOGGER.info("{} Data server is now shutdown...", new Date().toString());
  }

  private void gracefulShutdown() {
    if (!dataServerConfig.isGracefulShutdown()) {
      LOGGER.info("disable graceful shutdown, skip add blacklist");
      return;
    }

    try {
      addBlacklistRetryer.call(
          () -> {
            LOGGER.info("[GracefulShutdown] add self to blacklist");
            metaServerService.addSelfToMetaBlacklist();
            return true;
          });
      addBlacklistRetryer.call(
          () -> {
            if (fetchStopPushService.isStopPushSwitch()) {
              return true;
            }
            SlotTableStatusResponse statusResponse = metaServerService.getSlotTableStatus();
            if (statusResponse.isProtectionMode()) {
              return true;
            }
            LOGGER.info("[GracefulShutdown] wait no slot");
            if (slotManager.hasSlot()) {
              throw new RuntimeException("current data server still own slot, waiting...");
            }
            return true;
          });
      LOGGER.info("add data self to blacklist successfully");
    } catch (Throwable e) {
      LOGGER.error("add blacklist failed:", e);
    }
  }

  private void stopHttpServer() {
    if (httpServer != null && httpServer.isOpen()) {
      httpServer.close();
    }
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

  private void stopNotifyServer() {
    if (notifyServer != null && notifyServer.isOpen()) {
      notifyServer.close();
    }
  }

  private void bindResourceConfig() {
    registerInstances(Path.class);
    registerInstances(Provider.class);
  }

  private void registerInstances(Class<? extends Annotation> annotationType) {
    Map<String, Object> beans = applicationContext.getBeansWithAnnotation(annotationType);
    if (beans != null && !beans.isEmpty()) {
      beans.forEach((beanName, bean) -> dataJerseyConfig.registerInstances(bean));
    }
  }

  public boolean getHttpServerStarted() {
    return httpServerStarted.get();
  }

  public boolean getSchedulerStarted() {
    return schedulerStarted.get();
  }

  public boolean getServerForSessionStarted() {
    return serverForSessionStarted.get();
  }

  public boolean getServerForDataSyncStarted() {
    return serverForDataSyncStarted.get();
  }

  @VisibleForTesting
  DataServerBootstrap setDataServerConfig(DataServerConfig dataServerConfig) {
    this.dataServerConfig = dataServerConfig;
    return this;
  }

  @VisibleForTesting
  DataServerBootstrap setMetaServerService(MetaServerService metaServerService) {
    this.metaServerService = metaServerService;
    return this;
  }

  @VisibleForTesting
  DataServerBootstrap setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
    return this;
  }

  @VisibleForTesting
  DataServerBootstrap setJerseyResourceConfig(ResourceConfig jerseyResourceConfig) {
    this.dataJerseyConfig = jerseyResourceConfig;
    return this;
  }

  @VisibleForTesting
  DataServerBootstrap setJettyExchange(Exchange jettyExchange) {
    this.jettyExchange = jettyExchange;
    return this;
  }

  @VisibleForTesting
  DataServerBootstrap setBoltExchange(Exchange boltExchange) {
    this.boltExchange = boltExchange;
    return this;
  }

  @VisibleForTesting
  DataServerBootstrap setDataChangeEventCenter(DataChangeEventCenter dataChangeEventCenter) {
    this.dataChangeEventCenter = dataChangeEventCenter;
    return this;
  }

  @VisibleForTesting
  DataServerBootstrap setServerHandlers(Collection<AbstractServerHandler> serverHandlers) {
    this.serverHandlers = serverHandlers;
    return this;
  }

  @VisibleForTesting
  DataServerBootstrap setServerSyncHandlers(Collection<AbstractServerHandler> serverSyncHandlers) {
    this.serverSyncHandlers = serverSyncHandlers;
    return this;
  }

  @VisibleForTesting
  DataServerBootstrap setSystemPropertyProcessorManager(SystemPropertyProcessorManager manager) {
    this.systemPropertyProcessorManager = manager;
    return this;
  }
}
