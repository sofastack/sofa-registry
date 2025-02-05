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

import com.alipay.sofa.registry.common.model.elector.LeaderInfo;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.metrics.ReporterUtils;
import com.alipay.sofa.registry.metrics.TaskMetrics;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MultiClusterMetaServerConfig;
import com.alipay.sofa.registry.server.meta.remoting.meta.LocalMetaExchanger;
import com.alipay.sofa.registry.server.meta.remoting.meta.MetaServerRenewService;
import com.alipay.sofa.registry.server.shared.client.manager.ClientManagerService;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.server.shared.remoting.AbstractServerHandler;
import com.alipay.sofa.registry.store.api.elector.AbstractLeaderElector;
import com.alipay.sofa.registry.store.api.elector.LeaderElector;
import com.alipay.sofa.registry.store.api.meta.RecoverConfigRepository;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
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
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * @author shangyu.wh
 * @version $Id: MetaServerBootstrap.java, v 0.1 2018-01-16 11:28 shangyu.wh Exp $
 */
public class MetaServerBootstrap {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetaServerBootstrap.class);

  @Autowired private MetaServerConfig metaServerConfig;

  @Autowired private MultiClusterMetaServerConfig multiClusterMetaServerConfig;

  @Autowired private Exchange boltExchange;

  @Autowired private Exchange jerseyExchange;

  @Autowired private ExecutorManager executorManager;

  @Resource(name = "sessionServerHandlers")
  private Collection<AbstractServerHandler> sessionServerHandlers;

  @Resource(name = "dataServerHandlers")
  private Collection<AbstractServerHandler> dataServerHandlers;

  @Resource(name = "metaServerHandlers")
  private Collection<AbstractServerHandler> metaServerHandlers;

  @Resource(name = "remoteMetaServerHandlers")
  private Collection<AbstractServerHandler> remoteMetaServerHandlers;

  @Autowired private ResourceConfig jerseyResourceConfig;

  @Autowired private ApplicationContext applicationContext;

  @Autowired private LeaderElector leaderElector;

  @Autowired private MetaServerRenewService metaServerRenewService;

  @Autowired private LocalMetaExchanger localMetaExchanger;

  @Resource private ClientManagerService clientManagerService;

  @Autowired private RecoverConfigRepository recoverConfigRepository;

  private Server sessionServer;

  private Server dataServer;

  private Server metaServer;

  private Server httpServer;

  private Server remoteMetaServer;

  private final AtomicBoolean rpcServerForSessionStarted = new AtomicBoolean(false);

  private final AtomicBoolean rpcServerForDataStarted = new AtomicBoolean(false);

  private final AtomicBoolean rpcServerForMetaStarted = new AtomicBoolean(false);

  private final AtomicBoolean httpServerStarted = new AtomicBoolean(false);

  private final AtomicBoolean rpcServerForRemoteMetaStarted = new AtomicBoolean(false);

  private final AtomicBoolean schedulerStart = new AtomicBoolean(false);

  private final Retryer<Boolean> retryer =
      RetryerBuilder.<Boolean>newBuilder()
          .retryIfException()
          .retryIfResult(
              new Predicate<Boolean>() {
                @Override
                public boolean apply(Boolean input) {
                  return !input;
                }
              })
          .withWaitStrategy(WaitStrategies.exponentialWait(1000, 3000, TimeUnit.MILLISECONDS))
          .withStopStrategy(StopStrategies.stopAfterAttempt(10))
          .build();
  /** Do initialized. */
  public void start() {
    try {
      LOGGER.info("release properties: {}", ServerEnv.getReleaseProps());
      LOGGER.info("the configuration items are as follows: " + metaServerConfig.toString());
      ReporterUtils.enablePrometheusDefaultExports();

      recoverConfigRepository.waitSynced();
      clientManagerService.waitSynced();
      openSessionRegisterServer();

      openDataRegisterServer();

      openMetaRegisterServer();

      openHttpServer();

      openRemoteMetaServer();

      // meta start loop to elector leader
      startElectorLoop();

      retryer.call(
          () -> {
            AbstractLeaderElector.LeaderInfo leaderInfo = leaderElector.getLeaderInfo();
            LOGGER.info(
                "[MetaBootstrap] retry elector meta leader: {}, epoch:{}",
                leaderInfo.getLeader(),
                leaderInfo.getEpoch());
            return !StringUtils.isEmpty(leaderInfo.getLeader());
          });

      startScheduler();

      // start renew node
      renewNode();
      retryer.call(
          () -> {
            LeaderInfo leader = localMetaExchanger.getLeader(metaServerConfig.getLocalDataCenter());
            LOGGER.info(
                "[MetaBootstrap] retry connect to meta leader: {}, client:{}",
                leader.getLeader(),
                localMetaExchanger.getClient());
            return StringUtils.isNotEmpty(leader.getLeader())
                && localMetaExchanger.getClient() != null;
          });

      TaskMetrics.getInstance().registerBolt();
      AbstractLeaderElector.LeaderInfo leaderInfo = leaderElector.getLeaderInfo();

      LOGGER.info(
          "[MetaBootstrap] leader info: {}, [{}]", leaderInfo.getLeader(), leaderInfo.getEpoch());
      Runtime.getRuntime().addShutdownHook(new Thread(this::doStop));
    } catch (Throwable e) {
      LOGGER.error("Bootstrap Meta Server got error!", e);
      throw new RuntimeException("Bootstrap Meta Server got error!", e);
    }
  }

  private void startElectorLoop() {
    leaderElector.change2Follow();
  }

  private void startScheduler() {

    try {
      if (schedulerStart.compareAndSet(false, true)) {
        executorManager.startScheduler();
        LOGGER.info("Meta Scheduler started!");
      }
    } catch (Exception e) {
      schedulerStart.set(false);
      LOGGER.error("Meta Scheduler start error!", e);
      throw new RuntimeException("Meta Scheduler start error!", e);
    }
  }

  public void destroy() {
    doStop();
  }

  private void doStop() {
    try {
      LOGGER.info("{} Shutting down Meta Server..", new Date().toString());

      executorManager.stopScheduler();
      stopServer();

    } catch (Throwable e) {
      LOGGER.error("Shutting down Meta Server error!", e);
    }
    LOGGER.info("{} Meta server is now shutdown...", new Date().toString());
  }

  private void renewNode() {
    metaServerRenewService.startRenewer(
        metaServerConfig.getSchedulerHeartbeatIntervalSecs() * 1000);
  }

  private void openSessionRegisterServer() {
    try {
      if (rpcServerForSessionStarted.compareAndSet(false, true)) {
        sessionServer =
            boltExchange.open(
                new URL(
                    NetUtil.getLocalAddress().getHostAddress(),
                    metaServerConfig.getSessionServerPort()),
                sessionServerHandlers.toArray(new ChannelHandler[sessionServerHandlers.size()]));

        LOGGER.info(
            "Open session node register server port {} success!",
            metaServerConfig.getSessionServerPort());
      }
    } catch (Exception e) {
      rpcServerForSessionStarted.set(false);
      LOGGER.error(
          "Open session node register server port {} error!",
          metaServerConfig.getSessionServerPort(),
          e);
      throw new RuntimeException("Open session node register server error!", e);
    }
  }

  private void openDataRegisterServer() {
    try {
      if (rpcServerForDataStarted.compareAndSet(false, true)) {
        dataServer =
            boltExchange.open(
                new URL(
                    NetUtil.getLocalAddress().getHostAddress(),
                    metaServerConfig.getDataServerPort()),
                dataServerHandlers.toArray(new ChannelHandler[dataServerHandlers.size()]));

        LOGGER.info(
            "Open data node register server port {} success!",
            metaServerConfig.getDataServerPort());
      }
    } catch (Exception e) {
      rpcServerForDataStarted.set(false);
      LOGGER.error(
          "Open data node register server port {} error!", metaServerConfig.getDataServerPort(), e);
      throw new RuntimeException("Open data node register server error!", e);
    }
  }

  private void openMetaRegisterServer() {
    try {
      if (rpcServerForMetaStarted.compareAndSet(false, true)) {
        metaServer =
            boltExchange.open(
                new URL(
                    NetUtil.getLocalAddress().getHostAddress(),
                    metaServerConfig.getMetaServerPort()),
                metaServerHandlers.toArray(new ChannelHandler[metaServerHandlers.size()]));

        LOGGER.info("Open meta server port {} success!", metaServerConfig.getMetaServerPort());
      }
    } catch (Exception e) {
      rpcServerForMetaStarted.set(false);
      LOGGER.error("Open meta server port {} error!", metaServerConfig.getMetaServerPort(), e);
      throw new RuntimeException("Open meta server error!", e);
    }
  }

  private void openHttpServer() {
    try {
      if (httpServerStarted.compareAndSet(false, true)) {
        bindResourceConfig();
        httpServer =
            jerseyExchange.open(
                new URL(
                    NetUtil.getLocalAddress().getHostAddress(),
                    metaServerConfig.getHttpServerPort()),
                new ResourceConfig[] {jerseyResourceConfig});
        LOGGER.info("Open http server port {} success!", metaServerConfig.getHttpServerPort());
      }
    } catch (Exception e) {
      httpServerStarted.set(false);
      LOGGER.error("Open http server port {} error!", metaServerConfig.getHttpServerPort(), e);
      throw new RuntimeException("Open http server error!", e);
    }
  }

  private void openRemoteMetaServer() {
    try {
      if (rpcServerForRemoteMetaStarted.compareAndSet(false, true)) {
        remoteMetaServer =
            boltExchange.open(
                new URL(
                    NetUtil.getLocalAddress().getHostAddress(),
                    multiClusterMetaServerConfig.getRemoteMetaServerPort()),
                remoteMetaServerHandlers.toArray(
                    new ChannelHandler[remoteMetaServerHandlers.size()]));

        LOGGER.info(
            "Open remote meta server port {} success!",
            multiClusterMetaServerConfig.getRemoteMetaServerPort());
      }
    } catch (Exception e) {
      rpcServerForRemoteMetaStarted.set(false);
      LOGGER.error(
          "Open remote meta server port {} error!",
          multiClusterMetaServerConfig.getRemoteMetaServerPort(),
          e);
      throw new RuntimeException("Open remote meta server error!", e);
    }
  }

  private void bindResourceConfig() {
    registerInstances(Path.class);
    registerInstances(Provider.class);
  }

  private void registerInstances(Class<? extends Annotation> annotationType) {
    Map<String, Object> beans = applicationContext.getBeansWithAnnotation(annotationType);
    if (beans != null && beans.size() > 0) {
      beans.forEach((beanName, bean) -> jerseyResourceConfig.registerInstances(bean));
    }
  }

  private void stopServer() {
    if (sessionServer != null && sessionServer.isOpen()) {
      sessionServer.close();
    }
    if (dataServer != null && dataServer.isOpen()) {
      dataServer.close();
    }
    if (metaServer != null && metaServer.isOpen()) {
      metaServer.close();
    }
    if (httpServer != null && httpServer.isOpen()) {
      httpServer.close();
    }

    if (remoteMetaServer != null && remoteMetaServer.isOpen()) {
      remoteMetaServer.isOpen();
    }
  }

  /**
   * Getter method for property <tt>sessionStart</tt>.
   *
   * @return property value of sessionStart
   */
  public boolean isRpcServerForSessionStarted() {
    return rpcServerForSessionStarted.get();
  }

  /**
   * Getter method for property <tt>dataStart</tt>.
   *
   * @return property value of dataStart
   */
  public boolean isRpcServerForDataStarted() {
    return rpcServerForDataStarted.get();
  }

  /**
   * Getter method for property <tt>metaStart</tt>.
   *
   * @return property value of metaStart
   */
  public boolean isRpcServerForMetaStarted() {
    return rpcServerForMetaStarted.get();
  }

  /**
   * Getter method for property <tt>httpStart</tt>.
   *
   * @return property value of httpStart
   */
  public boolean isHttpServerStarted() {
    return httpServerStarted.get();
  }

  public boolean isRpcServerForRemoteMetaStarted() {
    return rpcServerForRemoteMetaStarted.get();
  }
}
