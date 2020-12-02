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

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.data.cache.CacheDigestTask;
import com.alipay.sofa.registry.server.data.event.EventCenter;
import com.alipay.sofa.registry.server.data.remoting.handler.AbstractServerHandler;
import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;

import javax.annotation.Resource;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private DataServerConfig                  dataServerConfig;

    @Autowired
    private MetaServerService                 metaServerService;

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

    private final AtomicBoolean               httpServerStarted        = new AtomicBoolean(false);

    private final AtomicBoolean               schedulerStarted         = new AtomicBoolean(false);

    private final AtomicBoolean               serverForSessionStarted  = new AtomicBoolean(false);

    private final AtomicBoolean               serverForDataSyncStarted = new AtomicBoolean(false);

    /**
     * start dataserver
     */
    public void start() {
        try {
            LOGGER.info("begin start server");

            LOGGER.info("the configuration items are as follows: " + dataServerConfig.toString());


            openDataServer();

            openDataSyncServer();

            openHttpServer();

            startRaftClient();

            renewNode();

            fetchProviderData();

            startScheduler();

            Runtime.getRuntime().addShutdownHook(new Thread(this::doStop));

            LOGGER.info("start server success");
        } catch (Exception e) {
            throw new RuntimeException("start server error", e);
        }
    }

    private void openDataServer() {
        try {
            if (serverForSessionStarted.compareAndSet(false, true)) {
                server = boltExchange.open(new URL(NetUtil.getLocalAddress().getHostAddress(),
                    dataServerConfig.getPort()), serverHandlers
                    .toArray(new ChannelHandler[serverHandlers.size()]));
                LOGGER.info("Data server for session started! port:{}", dataServerConfig.getPort());
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
                dataSyncServer = boltExchange.open(new URL(NetUtil.getLocalAddress()
                    .getHostAddress(), dataServerConfig.getSyncDataPort()), serverSyncHandlers
                    .toArray(new ChannelHandler[serverSyncHandlers.size()]));
                LOGGER.info("Data server for sync started! port:{}",
                    dataServerConfig.getSyncDataPort());
            }
        } catch (Exception e) {
            serverForDataSyncStarted.set(false);
            LOGGER.error("Data sync server start error! port:{}",
                dataServerConfig.getSyncDataPort(), e);
            throw new RuntimeException("Data sync server start error!", e);
        }
    }

    private void openHttpServer() {
        try {
            if (httpServerStarted.compareAndSet(false, true)) {
                bindResourceConfig();
                httpServer = jerseyExchange.open(
                    new URL(NetUtil.getLocalAddress().getHostAddress(), dataServerConfig
                        .getHttpServerPort()), new ResourceConfig[] { jerseyResourceConfig });
                LOGGER.info("Open http server port {} success!",
                    dataServerConfig.getHttpServerPort());
            }
        } catch (Exception e) {
            httpServerStarted.set(false);
            LOGGER
                .error("Open http server port {} error!", dataServerConfig.getHttpServerPort(), e);
            throw new RuntimeException("Open http server  error!", e);
        }
    }

    private void startRaftClient() {
        metaServerService.startRaftClient();
        LOGGER.info("raft client started!Leader is {}", metaServerService.getLeader());
    }

    private void renewNode() {
        metaServerService.renewNode();
        metaServerService.startRenewer(dataServerConfig.getSchedulerHeartbeatIntervalSec() * 1000);

    }

    private void fetchProviderData() {
        ProvideData provideData = metaServerService.fetchData(ValueConstants.DATA_DATUM_EXPIRE_SEC);
        Integer expireSec = ProvideData.toInteger(provideData);
        if (expireSec != null) {
            dataServerConfig.setSlotLeaderDatumExpireSec(expireSec);
            LOGGER.info("Fetch {}={}, update current config", ValueConstants.DATA_DATUM_EXPIRE_SEC,
                expireSec);
        }

        provideData = metaServerService
            .fetchData(ValueConstants.DATA_DATUM_SYNC_SESSION_INTERVAL_SEC);
        Integer syncSessionIntervalSec = ProvideData.toInteger(provideData);
        if (syncSessionIntervalSec != null) {
            dataServerConfig.setSlotLeaderSyncSessionIntervalSec(syncSessionIntervalSec);
            LOGGER.info("Fetch {}={}, update current config",
                ValueConstants.DATA_DATUM_SYNC_SESSION_INTERVAL_SEC, syncSessionIntervalSec);
        }
    }

    private void startScheduler() {
        try {
            if (schedulerStarted.compareAndSet(false, true)) {
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
}
