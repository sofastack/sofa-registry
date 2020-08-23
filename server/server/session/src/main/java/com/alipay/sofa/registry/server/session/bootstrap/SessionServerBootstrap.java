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

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Resource;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

import com.alipay.sofa.registry.server.session.cache.SessionCacheDigestTask;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.FetchProvideDataRequest;
import com.alipay.sofa.registry.common.model.metaserver.NodeChangeResult;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.common.model.metaserver.SessionNode;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.remoting.Client;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.remoting.exchange.NodeExchanger;
import com.alipay.sofa.registry.server.session.filter.blacklist.BlacklistManager;
import com.alipay.sofa.registry.server.session.node.NodeManager;
import com.alipay.sofa.registry.server.session.node.NodeManagerFactory;
import com.alipay.sofa.registry.server.session.node.RaftClientManager;
import com.alipay.sofa.registry.server.session.node.SessionProcessIdGenerator;
import com.alipay.sofa.registry.server.session.provideData.ProvideDataProcessor;
import com.alipay.sofa.registry.server.session.remoting.handler.AbstractServerHandler;
import com.alipay.sofa.registry.server.session.scheduler.ExecutorManager;
import com.alipay.sofa.registry.task.batcher.TaskDispatchers;

/**
 * The type Session server bootstrap.
 * @author shangyu.wh
 * @version $Id : SessionServerBootstrap.java, v 0.1 2017-11-14 11:44 synex Exp $
 */
public class SessionServerBootstrap {

    private static final Logger               LOGGER         = LoggerFactory
                                                                 .getLogger(SessionServerBootstrap.class);

    @Autowired
    private SessionServerConfig               sessionServerConfig;

    @Autowired
    private Exchange                          boltExchange;

    @Autowired
    private Exchange                          jerseyExchange;

    @Autowired
    private ExecutorManager                   executorManager;

    @Resource(name = "serverHandlers")
    private Collection<AbstractServerHandler> serverHandlers;

    @Autowired
    private NodeManager                       metaNodeManager;

    @Autowired
    protected NodeExchanger                   metaNodeExchanger;

    @Autowired
    private NodeExchanger                     dataNodeExchanger;

    @Autowired
    private ResourceConfig                    jerseyResourceConfig;

    @Autowired
    private ApplicationContext                applicationContext;

    @Autowired
    private RaftClientManager                 raftClientManager;

    @Autowired
    private BlacklistManager                  blacklistManager;

    @Autowired
    private ProvideDataProcessor              provideDataProcessorManager;

    @Autowired
    private SessionCacheDigestTask            sessionCacheDigestTask;

    private Server                            server;

    private Server                            httpServer;

    private Client                            metaClient;

    private AtomicBoolean                     metaStart      = new AtomicBoolean(false);

    private AtomicBoolean                     schedulerStart = new AtomicBoolean(false);

    private AtomicBoolean                     httpStart      = new AtomicBoolean(false);

    private AtomicBoolean                     serverStart    = new AtomicBoolean(false);

    private AtomicBoolean                     dataStart      = new AtomicBoolean(false);

    /**
     * Do initialized.
     */
    public void start() {
        try {
            LOGGER.info("the configuration items are as follows: " + sessionServerConfig.toString());

            initEnvironment();

            startRaftClient();

            connectMetaServer();

            startScheduler();

            openHttpServer();

            openSessionServer();

            connectDataServer();

            LOGGER.info("Initialized Session Server...");

            Runtime.getRuntime().addShutdownHook(new Thread(this::doStop));
        } catch (Throwable e) {
            LOGGER.error("Cannot bootstrap session server :", e);
            throw new RuntimeException("Cannot bootstrap session server :", e);
        }
    }

    /**
     * Destroy.
     */
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
        } catch (Throwable e) {
            LOGGER.error("Shutting down Session Server error!", e);
        }
        LOGGER.info("{} Session server is now shutdown...", new Date().toString());
    }

    private void initEnvironment() {
        LOGGER.info("Session server Environment: DataCenter {},Region {},ProcessId {}",
            sessionServerConfig.getSessionServerDataCenter(),
            sessionServerConfig.getSessionServerRegion(),
            SessionProcessIdGenerator.getSessionProcessId());
    }

    private void startScheduler() {

        try {
            if (schedulerStart.compareAndSet(false, true)) {
                executorManager.startScheduler();

                //start dump session store
                sessionCacheDigestTask.start();
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
                server = boltExchange.open(new URL(NetUtil.getLocalAddress().getHostAddress(),
                    sessionServerConfig.getServerPort()), serverHandlers
                    .toArray(new ChannelHandler[serverHandlers.size()]));

                LOGGER.info("Session server started! port:{}", sessionServerConfig.getServerPort());
            }
        } catch (Exception e) {
            serverStart.set(false);
            LOGGER.error("Session server start error! port:{}",
                sessionServerConfig.getServerPort(), e);
            throw new RuntimeException("Session server start error!", e);
        }
    }

    private void connectDataServer() {
        try {
            if (dataStart.compareAndSet(false, true)) {
                dataNodeExchanger.connectServer();
            }
        } catch (Exception e) {
            dataStart.set(false);
            LOGGER.error("Data server connected server error! port:{}",
                sessionServerConfig.getDataServerPort(), e);
            throw new RuntimeException("Data server connected server error!", e);
        }
    }

    private void startRaftClient() {
        raftClientManager.startRaftClient();
        LOGGER.info("Raft Client started! Leader:{}", raftClientManager.getLeader());
    }

    private void connectMetaServer() {
        try {
            if (metaStart.compareAndSet(false, true)) {
                metaClient = metaNodeExchanger.connectServer();

                URL leaderUrl = new URL(raftClientManager.getLeader().getIp(),
                    sessionServerConfig.getMetaServerPort());

                registerSessionNode(leaderUrl);

                getAllDataCenter();

                fetchStopPushSwitch(leaderUrl);

                fetchEnableDataRenewSnapshot(leaderUrl);

                fetchBlackList();

                LOGGER.info("MetaServer connected meta server! Port:{}",
                    sessionServerConfig.getMetaServerPort());
            }
        } catch (Exception e) {
            metaStart.set(false);
            LOGGER.error("MetaServer connected server error! Port:{}",
                sessionServerConfig.getMetaServerPort(), e);
            throw new RuntimeException("MetaServer connected server error!", e);
        }
    }

    private void registerSessionNode(URL leaderUrl) {
        URL clientUrl = new URL(NetUtil.getLocalAddress().getHostAddress(), 0);
        SessionNode sessionNode = new SessionNode(clientUrl,
            sessionServerConfig.getSessionServerRegion());
        Object ret = sendMetaRequest(sessionNode, leaderUrl);
        if (ret instanceof NodeChangeResult) {
            NodeChangeResult nodeChangeResult = (NodeChangeResult) ret;
            NodeManager nodeManager = NodeManagerFactory.getNodeManager(nodeChangeResult
                .getNodeType());
            //update data node info
            nodeManager.updateNodes(nodeChangeResult);
            LOGGER.info("Register MetaServer Session Node success!get data node list {}",
                nodeChangeResult.getNodes());
        }
    }

    private void fetchStopPushSwitch(URL leaderUrl) {
        FetchProvideDataRequest fetchProvideDataRequest = new FetchProvideDataRequest(
            ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID);
        Object ret = sendMetaRequest(fetchProvideDataRequest, leaderUrl);
        if (ret instanceof ProvideData) {
            ProvideData provideData = (ProvideData) ret;
            provideDataProcessorManager.fetchDataProcess(provideData);
        } else {
            LOGGER.info("Fetch session stop push switch data null,config not change!");
        }
    }

    private void fetchEnableDataRenewSnapshot(URL leaderUrl) {
        FetchProvideDataRequest fetchProvideDataRequest = new FetchProvideDataRequest(
            ValueConstants.ENABLE_DATA_RENEW_SNAPSHOT);
        Object data = sendMetaRequest(fetchProvideDataRequest, leaderUrl);
        if (data instanceof ProvideData) {
            ProvideData provideData = (ProvideData) data;
            provideDataProcessorManager.fetchDataProcess(provideData);
        }
    }

    private void fetchBlackList() {
        blacklistManager.load();
    }

    private Object sendMetaRequest(Object request, URL leaderUrl) {
        Object ret;
        try {
            ret = metaClient.sendSync(leaderUrl, request,
                sessionServerConfig.getMetaNodeExchangeTimeOut());
        } catch (Exception e) {
            URL leaderUrlNew = new URL(raftClientManager.refreshLeader().getIp(),
                sessionServerConfig.getMetaServerPort());
            LOGGER.warn("request send error!It will be retry once to new leader {}!", leaderUrlNew);
            ret = metaClient.sendSync(leaderUrlNew, request,
                sessionServerConfig.getMetaNodeExchangeTimeOut());
        }
        return ret;
    }

    private void getAllDataCenter() {
        //get meta node info
        metaNodeManager.getAllDataCenterNodes();
        LOGGER.info("Get all dataCenter from meta Server success!");
    }

    private void openHttpServer() {
        try {
            if (httpStart.compareAndSet(false, true)) {
                bindResourceConfig();
                httpServer = jerseyExchange.open(
                    new URL(NetUtil.getLocalAddress().getHostAddress(), sessionServerConfig
                        .getHttpServerPort()), new ResourceConfig[] { jerseyResourceConfig });
                LOGGER.info("Open http server port {} success!",
                    sessionServerConfig.getHttpServerPort());
            }
        } catch (Exception e) {
            LOGGER.error("Open http server port {} error!",
                sessionServerConfig.getHttpServerPort(), e);
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
            beans.forEach((beanName, bean) -> {
                jerseyResourceConfig.registerInstances(bean);
                jerseyResourceConfig.register(bean.getClass());
            });
        }
    }

    private void stopServer() {
        if (server != null && server.isOpen()) {
            server.close();
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
    public AtomicBoolean getMetaStart() {
        return metaStart;
    }

    /**
     * Getter method for property <tt>schedulerStart</tt>.
     *
     * @return property value of schedulerStart
     */
    public AtomicBoolean getSchedulerStart() {
        return schedulerStart;
    }

    /**
     * Getter method for property <tt>httpStart</tt>.
     *
     * @return property value of httpStart
     */
    public AtomicBoolean getHttpStart() {
        return httpStart;
    }

    /**
     * Getter method for property <tt>serverStart</tt>.
     *
     * @return property value of serverStart
     */
    public AtomicBoolean getServerStart() {
        return serverStart;
    }

    /**
     * Getter method for property <tt>dataStart</tt>.
     *
     * @return property value of dataStart
     */
    public AtomicBoolean getDataStart() {
        return dataStart;
    }
}