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

import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.meta.executor.ExecutorManager;
import com.alipay.sofa.registry.server.meta.remoting.RaftExchanger;
import com.alipay.sofa.registry.server.meta.remoting.handler.AbstractServerHandler;
import com.alipay.sofa.registry.task.batcher.TaskDispatchers;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
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
 * @author shangyu.wh
 * @version $Id: MetaServerBootstrap.java, v 0.1 2018-01-16 11:28 shangyu.wh Exp $
 */
public class MetaServerBootstrap {

    private static final Logger               LOGGER       = LoggerFactory
                                                               .getLogger(MetaServerBootstrap.class);

    @Autowired
    private MetaServerConfig                  metaServerConfig;

    @Autowired
    private Exchange                          boltExchange;

    @Autowired
    private Exchange                          jerseyExchange;

    @Autowired
    private ExecutorManager                   executorManager;

    @Resource(name = "sessionServerHandlers")
    private Collection<AbstractServerHandler> sessionServerHandlers;

    @Resource(name = "dataServerHandlers")
    private Collection<AbstractServerHandler> dataServerHandlers;

    @Resource(name = "metaServerHandlers")
    private Collection<AbstractServerHandler> metaServerHandlers;

    @Autowired
    private ResourceConfig                    jerseyResourceConfig;

    @Autowired
    private ApplicationContext                applicationContext;

    @Autowired
    private RaftExchanger                     raftExchanger;

    private Server                            sessionServer;

    private Server                            dataServer;

    private Server                            metaServer;

    private Server                            httpServer;

    private AtomicBoolean                     sessionStart = new AtomicBoolean(false);

    private AtomicBoolean                     dataStart    = new AtomicBoolean(false);

    private AtomicBoolean                     metaStart    = new AtomicBoolean(false);

    private AtomicBoolean                     httpStart    = new AtomicBoolean(false);

    /**
     * Do initialized.
     */
    public void doInitialized() {
        try {
            openSessionRegisterServer();

            openDataRegisterServer();

            openMetaRegisterServer();

            openHttpServer();

            initRaft();

            Runtime.getRuntime().addShutdownHook(new Thread(this::doStop));
        } catch (Throwable e) {
            LOGGER.error("Bootstrap Meta Server got error!", e);
            throw new RuntimeException("Bootstrap Meta Server got error!", e);
        }
    }

    public void destroy() {
        doStop();
    }

    private void doStop() {
        try {
            LOGGER.info("{} Shutting down Meta Server..", new Date().toString());

            executorManager.stopScheduler();

            TaskDispatchers.stopDefaultSingleTaskDispatcher();

            stopServer();

        } catch (Throwable e) {
            LOGGER.error("Shutting down Meta Server error!", e);
        }
        LOGGER.info("{} Meta server is now shutdown...", new Date().toString());
    }

    private void openSessionRegisterServer() {
        try {
            if (sessionStart.compareAndSet(false, true)) {
                sessionServer = boltExchange
                    .open(
                        new URL(NetUtil.getLocalAddress().getHostAddress(), metaServerConfig
                            .getSessionServerPort()), sessionServerHandlers
                            .toArray(new ChannelHandler[sessionServerHandlers.size()]));

                LOGGER.info("Open session node register server port {} success!",
                    metaServerConfig.getSessionServerPort());
            }
        } catch (Exception e) {
            sessionStart.set(false);
            LOGGER.error("Open session node register server port {} error!",
                metaServerConfig.getSessionServerPort(), e);
            throw new RuntimeException("Open session node register server error!", e);
        }
    }

    private void openDataRegisterServer() {
        try {
            if (dataStart.compareAndSet(false, true)) {
                dataServer = boltExchange.open(new URL(NetUtil.getLocalAddress().getHostAddress(),
                    metaServerConfig.getDataServerPort()), dataServerHandlers
                    .toArray(new ChannelHandler[dataServerHandlers.size()]));

                LOGGER.info("Open data node register server port {} success!",
                    metaServerConfig.getDataServerPort());
            }
        } catch (Exception e) {
            dataStart.set(false);
            LOGGER.error("Open data node register server port {} error!",
                metaServerConfig.getDataServerPort(), e);
            throw new RuntimeException("Open data node register server error!", e);
        }
    }

    private void openMetaRegisterServer() {
        try {
            if (metaStart.compareAndSet(false, true)) {
                metaServer = boltExchange.open(new URL(NetUtil.getLocalAddress().getHostAddress(),
                    metaServerConfig.getMetaServerPort()), metaServerHandlers
                    .toArray(new ChannelHandler[metaServerHandlers.size()]));

                LOGGER.info("Open meta server port {} success!",
                    metaServerConfig.getMetaServerPort());
            }
        } catch (Exception e) {
            metaStart.set(false);
            LOGGER
                .error("Open meta server port {} error!", metaServerConfig.getMetaServerPort(), e);
            throw new RuntimeException("Open meta server error!", e);
        }
    }

    private void openHttpServer() {
        try {
            if (httpStart.compareAndSet(false, true)) {
                bindResourceConfig();
                httpServer = jerseyExchange.open(
                    new URL(NetUtil.getLocalAddress().getHostAddress(), metaServerConfig
                        .getHttpServerPort()), new ResourceConfig[] { jerseyResourceConfig });
                LOGGER.info("Open http server port {} success!",
                    metaServerConfig.getHttpServerPort());
            }
        } catch (Exception e) {
            httpStart.set(false);
            LOGGER
                .error("Open http server port {} error!", metaServerConfig.getHttpServerPort(), e);
            throw new RuntimeException("Open http server error!", e);
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

    private void initRaft() {
        raftExchanger.startRaftServer(executorManager);
        LOGGER.info("Raft server port {} start success!group {}",
            metaServerConfig.getRaftServerPort(), metaServerConfig.getRaftGroup());

        raftExchanger.startRaftClient();
        LOGGER.info("Raft client connect success!");

        raftExchanger.startCliService();
        LOGGER.info("Raft start CliService success!");

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
        if (raftExchanger != null) {
            raftExchanger.shutdown();
        }
    }

    /**
     * Getter method for property <tt>sessionStart</tt>.
     *
     * @return property value of sessionStart
     */
    public AtomicBoolean getSessionStart() {
        return sessionStart;
    }

    /**
     * Getter method for property <tt>dataStart</tt>.
     *
     * @return property value of dataStart
     */
    public AtomicBoolean getDataStart() {
        return dataStart;
    }

    /**
     * Getter method for property <tt>metaStart</tt>.
     *
     * @return property value of metaStart
     */
    public AtomicBoolean getMetaStart() {
        return metaStart;
    }

    /**
     * Getter method for property <tt>httpStart</tt>.
     *
     * @return property value of httpStart
     */
    public AtomicBoolean getHttpStart() {
        return httpStart;
    }
}