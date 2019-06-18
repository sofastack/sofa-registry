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

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;

import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.data.cache.CacheDigestTask;
import com.alipay.sofa.registry.server.data.datasync.sync.Scheduler;
import com.alipay.sofa.registry.server.data.event.EventCenter;
import com.alipay.sofa.registry.server.data.event.MetaServerChangeEvent;
import com.alipay.sofa.registry.server.data.event.StartTaskEvent;
import com.alipay.sofa.registry.server.data.event.StartTaskTypeEnum;
import com.alipay.sofa.registry.server.data.remoting.handler.AbstractServerHandler;
import com.alipay.sofa.registry.server.data.remoting.metaserver.IMetaServerService;

/**
 *
 *
 * @author qian.lqlq
 * @version $Id: DataServerBootstrap.java, v 0.1 2017-12-06 20:50 qian.lqlq Exp $
 */
@EnableConfigurationProperties
public class DataServerBootstrap {
    private static final Logger               LOGGER                   = LoggerFactory
                                                                           .getLogger(DataServerBootstrap.class);

    @Autowired
    private DataServerConfig                  dataServerBootstrapConfig;

    @Autowired
    private IMetaServerService                metaServerService;

    @Autowired
    private Scheduler                         syncDataScheduler;

    @Autowired
    private ApplicationContext                applicationContext;

    @Autowired
    private ResourceConfig                    jerseyResourceConfig;

    @Autowired
    private Exchange                          jerseyExchange;

    @Autowired
    private Exchange                          boltExchange;

    @Autowired
    private EventCenter                       eventCenter;

    @Autowired
    private CacheDigestTask                   cacheDigestTask;

    @Resource(name = "serverHandlers")
    private Collection<AbstractServerHandler> serverHandlers;

    @Resource(name = "serverSyncHandlers")
    private Collection<AbstractServerHandler> serverSyncHandlers;

    private Server                            server;

    private Server                            dataSyncServer;

    private Server                            httpServer;

    private AtomicBoolean                     httpServerStarted        = new AtomicBoolean(false);

    private AtomicBoolean                     schedulerStarted         = new AtomicBoolean(false);

    private AtomicBoolean                     serverForSessionStarted  = new AtomicBoolean(false);

    private AtomicBoolean                     serverForDataSyncStarted = new AtomicBoolean(false);

    /**
     * start dataserver
     */
    public void start() {
        try {
            LOGGER.info("[DataServerBootstrap] begin start server");

            openDataServer();

            openDataSyncServer();

            openHttpServer();

            startRaftClient();

            startScheduler();

            Runtime.getRuntime().addShutdownHook(new Thread(this::doStop));

            LOGGER.info("[DataServerBootstrap] start server success");
        } catch (Exception e) {
            throw new RuntimeException("[DataServerBootstrap] start server error", e);
        }
    }

    private void openDataServer() {
        try {
            if (serverForSessionStarted.compareAndSet(false, true)) {
                server = boltExchange.open(new URL(NetUtil.getLocalAddress().getHostAddress(),
                    dataServerBootstrapConfig.getPort()), serverHandlers
                    .toArray(new ChannelHandler[serverHandlers.size()]));
                LOGGER.info("Data server for session started! port:{}",
                    dataServerBootstrapConfig.getPort());
            }
        } catch (Exception e) {
            serverForSessionStarted.set(false);
            LOGGER
                .error("Data server start error! port:{}", dataServerBootstrapConfig.getPort(), e);
            throw new RuntimeException("Data server start error!", e);
        }
    }

    private void openDataSyncServer() {
        try {
            if (serverForDataSyncStarted.compareAndSet(false, true)) {
                dataSyncServer = boltExchange.open(new URL(NetUtil.getLocalAddress()
                    .getHostAddress(), dataServerBootstrapConfig.getSyncDataPort()),
                    serverSyncHandlers.toArray(new ChannelHandler[serverSyncHandlers.size()]));
                LOGGER.info("Data server for sync started! port:{}",
                    dataServerBootstrapConfig.getSyncDataPort());
            }
        } catch (Exception e) {
            serverForDataSyncStarted.set(false);
            LOGGER.error("Data sync server start error! port:{}",
                dataServerBootstrapConfig.getSyncDataPort(), e);
            throw new RuntimeException("Data sync server start error!", e);
        }
    }

    private void openHttpServer() {
        try {
            if (httpServerStarted.compareAndSet(false, true)) {
                bindResourceConfig();
                httpServer = jerseyExchange.open(
                    new URL(NetUtil.getLocalAddress().getHostAddress(), dataServerBootstrapConfig
                        .getHttpServerPort()), new ResourceConfig[] { jerseyResourceConfig });
                LOGGER.info("Open http server port {} success!",
                    dataServerBootstrapConfig.getHttpServerPort());
            }
        } catch (Exception e) {
            httpServerStarted.set(false);
            LOGGER.error("Open http server port {} error!",
                dataServerBootstrapConfig.getHttpServerPort(), e);
            throw new RuntimeException("Open http server  error!", e);
        }
    }

    private void startRaftClient() {
        metaServerService.startRaftClient();
        eventCenter.post(new MetaServerChangeEvent(metaServerService.getMetaServerMap()));
        LOGGER.info("[DataServerBootstrap] raft client started!Leader is {}",
            metaServerService.getLeader());
    }

    private void startScheduler() {
        try {
            if (schedulerStarted.compareAndSet(false, true)) {
                syncDataScheduler.startScheduler();
                // start all startTask except correction task
                eventCenter.post(new StartTaskEvent(
                        Arrays.stream(StartTaskTypeEnum.values()).filter(type -> type != StartTaskTypeEnum.RENEW)
                                .collect(Collectors.toSet())));

                //start dump log
                cacheDigestTask.start();
            }
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

            if (httpServer != null && httpServer.isOpen()) {
                httpServer.close();
            }

            if (server != null && server.isOpen()) {
                server.close();
            }

            if (dataSyncServer != null && dataSyncServer.isOpen()) {
                dataSyncServer.close();
            }

            if (syncDataScheduler != null) {
                syncDataScheduler.stopScheduler();
            }
        } catch (Throwable e) {
            LOGGER.error("Shutting down Data Server error!", e);
        }
        LOGGER.info("{} Data server is now shutdown...", new Date().toString());
    }

    private void bindResourceConfig() {
        registerInstances(Path.class);
        registerInstances(Provider.class);
    }

    private void registerInstances(Class<? extends Annotation> annotationType) {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(annotationType);
        if (beans != null && !beans.isEmpty()) {
            beans.forEach((beanName, bean) -> jerseyResourceConfig.registerInstances(bean));
        }
    }

    public AtomicBoolean getHttpServerStarted() {
        return httpServerStarted;
    }

    public AtomicBoolean getSchedulerStarted() {
        return schedulerStarted;
    }

    public AtomicBoolean getServerForSessionStarted() {
        return serverForSessionStarted;
    }

    public AtomicBoolean getServerForDataSyncStarted() {
        return serverForDataSyncStarted;
    }
}
